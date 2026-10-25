package com.craftora.auth;

import com.craftora.auth.AuthDtos.ForgotPasswordRequest;
import com.craftora.auth.AuthDtos.LoginRequest;
import com.craftora.auth.AuthDtos.MeResponse;
import com.craftora.auth.AuthDtos.RegisterRequest;
import com.craftora.auth.AuthDtos.ResetPasswordRequest;
import com.craftora.auth.AuthDtos.TokenRequest;
import com.craftora.common.AppProperties;
import com.craftora.common.ClientIp;
import com.craftora.common.RateLimiter;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final SessionService sessions;
    private final RateLimiter rateLimiter;
    private final AppProperties props;

    public AuthController(AuthService auth, SessionService sessions, RateLimiter rateLimiter, AppProperties props) {
        this.auth = auth;
        this.sessions = sessions;
        this.rateLimiter = rateLimiter;
        this.props = props;
    }

    /** Issues the XSRF-TOKEN cookie; clients call this once before their first state-changing request. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<MeResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest http) {
        limit("register", http);
        var user = auth.register(req);
        String token = sessions.create(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, sessions.sessionCookie(token).toString())
                .body(auth.me(user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<MeResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        limit("login", http);
        var user = auth.login(req.email(), req.password());
        String token = sessions.create(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessions.sessionCookie(token).toString())
                .body(auth.me(user.getId()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.readToken(request).ifPresent(sessions::revoke);
        response.addHeader(HttpHeaders.SET_COOKIE, sessions.clearedCookie().toString());
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody TokenRequest req, HttpServletRequest http) {
        limit("verify", http);
        auth.verifyEmail(req.token());
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerification(@AuthenticationPrincipal AuthUser user, HttpServletRequest http) {
        if (user == null) {
            throw new com.craftora.common.ApiException(HttpStatus.UNAUTHORIZED, "unauthorized", "Authentication required");
        }
        limit("resend", http);
        auth.resendVerification(user.id());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest http) {
        limit("forgot", http);
        auth.requestPasswordReset(req.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest req, HttpServletRequest http) {
        limit("reset", http);
        auth.resetPassword(req.token(), req.password());
    }

    private void limit(String action, HttpServletRequest http) {
        rateLimiter.check("auth:" + action + ":" + ClientIp.of(http), props.rateLimit().authPerMinute(),
                Duration.ofMinutes(1));
    }
}
