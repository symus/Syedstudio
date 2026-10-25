package com.craftora.security;

import com.craftora.auth.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SessionService sessions) throws Exception {
        http
                // Cookie-based auth => CSRF protection. The SPA reads XSRF-TOKEN and echoes it in X-XSRF-TOKEN.
                .csrf(csrf -> csrf.spa())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                                        + "script-src 'self' 'unsafe-inline'; frame-ancestors 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .addFilterBefore(new SessionAuthenticationFilter(sessions), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/api/auth/**", "/api/docs/**", "/api/dev/**",
                                "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/media/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/maker/**").hasRole("MAKER")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeProblem(res, HttpStatus.UNAUTHORIZED,
                                "unauthorized", "Authentication required"))
                        .accessDeniedHandler((req, res, ex) -> writeProblem(res, HttpStatus.FORBIDDEN,
                                "forbidden", "You do not have access to this resource")));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private static void writeProblem(jakarta.servlet.http.HttpServletResponse res, HttpStatus status, String code,
                                     String detail) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.getWriter().write("{\"status\":%d,\"title\":\"%s\",\"detail\":\"%s\",\"code\":\"%s\"}"
                .formatted(status.value(), status.getReasonPhrase(), detail, code));
    }
}
