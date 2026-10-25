package com.craftora.auth;

import com.craftora.auth.AuthDtos.AccountType;
import com.craftora.auth.AuthDtos.MeResponse;
import com.craftora.auth.AuthDtos.RegisterRequest;
import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.common.Tokens;
import com.craftora.mail.EmailService;
import com.craftora.mail.MailTemplates;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.user.Role;
import com.craftora.user.User;
import com.craftora.user.UserRepository;
import com.craftora.user.UserToken;
import com.craftora.user.UserTokenRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Duration VERIFY_TTL = Duration.ofHours(48);
    private static final Duration RESET_TTL = Duration.ofHours(1);
    /** Pre-computed hash used to equalise timing when the e-mail does not exist. */
    private final String dummyHash;

    private final UserRepository users;
    private final UserTokenRepository tokens;
    private final MakerProfileRepository makers;
    private final PasswordEncoder encoder;
    private final SessionService sessions;
    private final EmailService email;
    private final MailTemplates templates;
    private final AppProperties props;

    public AuthService(UserRepository users, UserTokenRepository tokens, MakerProfileRepository makers,
                       PasswordEncoder encoder, SessionService sessions, EmailService email,
                       MailTemplates templates, AppProperties props) {
        this.users = users;
        this.tokens = tokens;
        this.makers = makers;
        this.encoder = encoder;
        this.sessions = sessions;
        this.email = email;
        this.templates = templates;
        this.props = props;
        this.dummyHash = encoder.encode("timing-equaliser-password");
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public User register(RegisterRequest req) {
        checkPasswordBytes(req.password());
        String emailAddr = normalizeEmail(req.email());
        if (users.existsByEmail(emailAddr)) {
            throw ApiException.conflict("email_taken", "An account with this email already exists");
        }
        User user = new User(emailAddr, encoder.encode(req.password()), req.displayName().trim(),
                props.normalizeLocale(req.locale()));
        user.addRole(Role.CUSTOMER);
        if (req.accountType() == AccountType.MAKER) {
            user.addRole(Role.MAKER);
        }
        users.save(user);
        sendVerification(user);
        return user;
    }

    @Transactional
    public User login(String rawEmail, String password) {
        User user = users.findByEmail(normalizeEmail(rawEmail)).orElse(null);
        if (user == null) {
            encoder.matches(password, dummyHash);
            throw invalidCredentials();
        }
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw invalidCredentials();
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "account_suspended", "This account is suspended");
        }
        return user;
    }

    @Transactional
    public void verifyEmail(String token) {
        UserToken t = consume(token, UserToken.Type.EMAIL_VERIFICATION);
        t.getUser().markEmailVerified();
    }

    @Transactional
    public void resendVerification(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (user.isEmailVerified()) {
            throw ApiException.badRequest("already_verified", "Email is already verified");
        }
        sendVerification(user);
    }

    /** Always succeeds from the caller's perspective to avoid revealing which e-mails are registered. */
    @Transactional
    public void requestPasswordReset(String rawEmail) {
        users.findByEmail(normalizeEmail(rawEmail)).filter(User::isActive).ifPresent(user -> {
            tokens.deleteAllForUser(user.getId(), UserToken.Type.PASSWORD_RESET);
            String token = Tokens.newToken();
            tokens.save(new UserToken(user, UserToken.Type.PASSWORD_RESET, Tokens.hash(token),
                    Instant.now().plus(RESET_TTL)));
            email.send(templates.passwordReset(user.getEmail(), user.getPreferredLocale(), token));
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        checkPasswordBytes(newPassword);
        UserToken t = consume(token, UserToken.Type.PASSWORD_RESET);
        User user = t.getUser();
        user.setPasswordHash(encoder.encode(newPassword));
        user.markEmailVerified(); // proving inbox ownership also verifies the address
        sessions.revokeAll(user.getId());
    }

    @Transactional
    public void changePassword(UUID userId, String current, String next) {
        checkPasswordBytes(next);
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (!encoder.matches(current, user.getPasswordHash())) {
            throw ApiException.badRequest("invalid_password", "Current password is incorrect");
        }
        user.setPasswordHash(encoder.encode(next));
        sessions.revokeAll(user.getId());
    }

    public boolean passwordMatches(User user, String password) {
        return encoder.matches(password, user.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        String makerSlug = makers.findByUserId(userId).map(m -> m.getSlug()).orElse(null);
        return new MeResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getPreferredLocale(),
                u.isEmailVerified(), u.getRoles(), makerSlug);
    }

    private void sendVerification(User user) {
        tokens.deleteAllForUser(user.getId(), UserToken.Type.EMAIL_VERIFICATION);
        String token = Tokens.newToken();
        tokens.save(new UserToken(user, UserToken.Type.EMAIL_VERIFICATION, Tokens.hash(token),
                Instant.now().plus(VERIFY_TTL)));
        email.send(templates.verifyEmail(user.getEmail(), user.getPreferredLocale(), user.getDisplayName(), token));
    }

    private UserToken consume(String token, UserToken.Type type) {
        UserToken t = tokens.findByTokenHash(Tokens.hash(token))
                .filter(x -> x.isUsable(type, Instant.now()))
                .orElseThrow(() -> ApiException.badRequest("invalid_token", "This link is invalid or has expired"));
        t.markUsed();
        return t;
    }

    private static void checkPasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.badRequest("password_too_long", "Password is too long");
        }
    }

    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password");
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgeExpiredTokens() {
        tokens.deleteExpired(Instant.now().minus(Duration.ofDays(1)));
    }
}
