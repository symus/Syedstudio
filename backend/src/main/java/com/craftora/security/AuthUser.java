package com.craftora.security;

import com.craftora.user.Role;
import java.util.Set;
import java.util.UUID;

/** Authenticated principal placed in the security context. Contains no secrets. */
public record AuthUser(UUID id, String email, Set<Role> roles) {

    public boolean has(Role role) {
        return roles.contains(role);
    }
}
