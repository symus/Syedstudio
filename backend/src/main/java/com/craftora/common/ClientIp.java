package com.craftora.common;

import jakarta.servlet.http.HttpServletRequest;

/** Client address used only as a rate-limit key (never persisted). Proxy headers are resolved by the server. */
public final class ClientIp {

    private ClientIp() {}

    public static String of(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
