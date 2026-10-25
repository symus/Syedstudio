package com.craftora.user;

import com.craftora.auth.AuthService;
import com.craftora.common.ApiException;
import com.craftora.inquiry.InquiryRepository;
import com.craftora.inquiry.InquiryService;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.media.ImageUploads;
import com.craftora.product.ProductMapper;
import com.craftora.product.ProductRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** GDPR workflows: data export (Art. 15/20) and account erasure (Art. 17). */
@Service
public class AccountService {

    private final UserRepository users;
    private final MakerProfileRepository makers;
    private final ProductRepository products;
    private final InquiryRepository inquiries;
    private final InquiryService inquiryService;
    private final AuthService auth;
    private final ImageUploads uploads;
    private final JdbcClient jdbc;
    private final EntityManager entityManager;

    public AccountService(UserRepository users, MakerProfileRepository makers, ProductRepository products,
                          InquiryRepository inquiries, InquiryService inquiryService, AuthService auth,
                          ImageUploads uploads, JdbcClient jdbc,
                          EntityManager entityManager) {
        this.users = users;
        this.makers = makers;
        this.products = products;
        this.inquiries = inquiries;
        this.inquiryService = inquiryService;
        this.auth = auth;
        this.uploads = uploads;
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    @Transactional
    public void becomeMaker(UUID userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        u.addRole(Role.MAKER);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> export(UUID userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("exportedAt", Instant.now());
        out.put("account", Map.of("id", u.getId(), "email", u.getEmail(), "displayName", u.getDisplayName(),
                "preferredLocale", u.getPreferredLocale(), "roles", u.getRoles(), "createdAt", u.getCreatedAt(),
                "emailVerified", u.isEmailVerified()));
        out.put("inquiriesSent", inquiryService.forCustomer(userId));
        makers.findByUserId(userId).ifPresent(m -> {
            out.put("makerProfile", com.craftora.maker.MakerService.toPublic(m, false));
            out.put("products", products.findByMakerIdOrderByUpdatedAtDesc(m.getId(), Pageable.unpaged())
                    .map(ProductMapper::owner).getContent());
            List<Object> received = new ArrayList<>();
            inquiries.findByMakerIdOrderByCreatedAtDesc(m.getId(), Pageable.unpaged())
                    .forEach(i -> received.add(inquiryService.get(userId, i.getId())));
            out.put("inquiriesReceived", received);
        });
        return out;
    }

    /**
     * Deletes the account and everything the user owns (maker profile, products, storefront, received inquiries,
     * stored images). Inquiries the user sent to other makers remain with those makers but are unlinked.
     */
    @Transactional
    public void delete(UUID userId, String password) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (!auth.passwordMatches(u, password)) {
            throw ApiException.badRequest("invalid_password", "Password is incorrect");
        }
        if (u.hasRole(Role.ADMIN)) {
            throw ApiException.badRequest("admin_account", "Administrator accounts must be removed by another admin");
        }
        List<String> keys = new ArrayList<>();
        makers.findByUserId(userId).ifPresent(m -> collectKeys(m, keys));
        // Owned rows (maker profile, products, storefront, received inquiries, sessions...) go via ON DELETE CASCADE.
        entityManager.flush();
        entityManager.clear();
        jdbc.sql("delete from app_user where id = :id").param("id", userId).update();
        keys.forEach(uploads::deleteQuietly);
    }

    private void collectKeys(MakerProfile m, List<String> keys) {
        keys.addAll(products.findImageKeysByMaker(m.getId()));
        keys.addAll(inquiries.findAttachmentKeysByMaker(m.getId()));
        if (m.getLogoKey() != null) {
            keys.add(m.getLogoKey());
        }
        if (m.getCoverKey() != null) {
            keys.add(m.getCoverKey());
        }
    }
}
