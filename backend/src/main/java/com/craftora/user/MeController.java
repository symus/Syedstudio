package com.craftora.user;

import com.craftora.auth.AuthDtos.ChangePasswordRequest;
import com.craftora.auth.AuthDtos.DeleteAccountRequest;
import com.craftora.auth.AuthDtos.MeResponse;
import com.craftora.auth.AuthDtos.UpdateMeRequest;
import com.craftora.auth.AuthService;
import com.craftora.auth.SessionService;
import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Account")
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AuthService auth;
    private final AccountService accounts;
    private final SessionService sessions;
    private final UserRepository users;
    private final AppProperties props;

    public MeController(AuthService auth, AccountService accounts, SessionService sessions, UserRepository users,
                        AppProperties props) {
        this.auth = auth;
        this.accounts = accounts;
        this.sessions = sessions;
        this.users = users;
        this.props = props;
    }

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal AuthUser user) {
        return auth.me(user.id());
    }

    @PatchMapping
    @Transactional
    public MeResponse update(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody UpdateMeRequest req) {
        User u = users.findById(user.id()).orElseThrow(() -> ApiException.notFound("User"));
        u.setDisplayName(req.displayName().trim());
        if (req.preferredLocale() != null) {
            u.setPreferredLocale(props.normalizeLocale(req.preferredLocale()));
        }
        return auth.me(user.id());
    }

    /** Changing the password signs out every session, including this one. */
    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ChangePasswordRequest req,
                               HttpServletResponse response) {
        auth.changePassword(user.id(), req.currentPassword(), req.newPassword());
        response.addHeader(HttpHeaders.SET_COOKIE, sessions.clearedCookie().toString());
    }

    @PostMapping("/become-maker")
    public MeResponse becomeMaker(@AuthenticationPrincipal AuthUser user) {
        accounts.becomeMaker(user.id());
        return auth.me(user.id());
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> export(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"craftora-data-export.json\"")
                .body(accounts.export(user.id()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody DeleteAccountRequest req,
                       HttpServletResponse response) {
        accounts.delete(user.id(), req.password());
        response.addHeader(HttpHeaders.SET_COOKIE, sessions.clearedCookie().toString());
    }
}
