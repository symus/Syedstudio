package com.craftora.security;

import com.craftora.auth.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Resolves the session cookie into an {@link AuthUser}. Unknown, expired or suspended sessions stay anonymous. */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessions;

    public SessionAuthenticationFilter(SessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        sessions.readToken(request).flatMap(sessions::authenticate).ifPresent(user -> {
            var authorities = user.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();
            var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
        chain.doFilter(request, response);
    }
}
