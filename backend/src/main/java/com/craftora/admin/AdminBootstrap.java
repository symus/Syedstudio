package com.craftora.admin;

import com.craftora.auth.AuthService;
import com.craftora.common.AppProperties;
import com.craftora.user.Role;
import com.craftora.user.User;
import com.craftora.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates (or promotes) the first administrator from APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD on startup. */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppProperties props;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminBootstrap(AppProperties props, UserRepository users, PasswordEncoder encoder) {
        this.props = props;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = props.bootstrap().adminEmail();
        String password = props.bootstrap().adminPassword();
        if (email == null || email.isBlank() || password == null || password.length() < 12) {
            return;
        }
        String normalized = AuthService.normalizeEmail(email);
        User admin = users.findByEmail(normalized).orElseGet(() -> {
            User u = new User(normalized, encoder.encode(password), "Administrator", props.defaultLocale());
            u.markEmailVerified();
            log.info("Bootstrap administrator account created");
            return users.save(u);
        });
        admin.addRole(Role.ADMIN);
    }
}
