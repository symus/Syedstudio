package com.craftora.auth;

import com.craftora.common.AppProperties;
import com.craftora.common.Tokens;
import com.craftora.security.AuthUser;
import com.craftora.user.User;
import com.craftora.user.UserSession;
import com.craftora.user.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Database-backed sessions: revocable, survive restarts and work across multiple backend instances. */
@Service
public class SessionService {

    private static final Duration TOUCH_INTERVAL = Duration.ofMinutes(10);

    private final UserSessionRepository repository;
    private final AppProperties.Session config;

    public SessionService(UserSessionRepository repository, AppProperties props) {
        this.repository = repository;
        this.config = props.session();
    }

    @Transactional
    public String create(User user) {
        String token = Tokens.newToken();
        repository.save(new UserSession(user, Tokens.hash(token), Instant.now().plus(config.ttl())));
        return token;
    }

    @Transactional
    public Optional<AuthUser> authenticate(String token) {
        Instant now = Instant.now();
        return repository.findByTokenHash(Tokens.hash(token))
                .filter(s -> s.getExpiresAt().isAfter(now) && s.getUser().isActive())
                .map(s -> {
                    if (s.getLastSeenAt().isBefore(now.minus(TOUCH_INTERVAL))) {
                        s.touch(now);
                    }
                    User u = s.getUser();
                    return new AuthUser(u.getId(), u.getEmail(), u.getRoles());
                });
    }

    @Transactional
    public void revoke(String token) {
        repository.deleteByTokenHash(Tokens.hash(token));
    }

    @Transactional
    public void revokeAll(UUID userId) {
        repository.deleteAllForUser(userId);
    }

    public Optional<String> readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> config.cookieName().equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank())
                .map(Cookie::getValue)
                .findFirst();
    }

    public ResponseCookie sessionCookie(String token) {
        return cookie(token, config.ttl());
    }

    public ResponseCookie clearedCookie() {
        return cookie("", Duration.ZERO);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(config.cookieName(), value)
                .httpOnly(true)
                .secure(config.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(Instant.now());
    }
}
