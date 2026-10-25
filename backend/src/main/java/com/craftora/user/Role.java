package com.craftora.user;

/** Account roles. Visitors are unauthenticated; a user may hold several roles (e.g. CUSTOMER and MAKER). */
public enum Role {
    CUSTOMER,
    MAKER,
    ADMIN
}
