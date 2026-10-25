package com.craftora.auth;

import com.craftora.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

    public enum AccountType { CUSTOMER, MAKER }

    // bcrypt accepts at most 72 bytes; the service also rejects passwords whose UTF-8 form exceeds that.
    public record RegisterRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(max = 100) String displayName,
            @NotNull AccountType accountType,
            @Size(max = 5) String locale) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank @Size(max = 64) String password) {}

    public record TokenRequest(@NotBlank @Size(max = 100) String token) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}

    public record ResetPasswordRequest(@NotBlank @Size(max = 100) String token,
                                       @NotBlank @Size(min = 8, max = 64) String password) {}

    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 8, max = 64) String newPassword) {}

    public record UpdateMeRequest(@NotBlank @Size(max = 100) String displayName, @Size(max = 5) String preferredLocale) {}

    public record DeleteAccountRequest(@NotBlank String password) {}

    public record MeResponse(UUID id, String email, String displayName, String preferredLocale, boolean emailVerified,
                             Set<Role> roles, String makerSlug) {}
}
