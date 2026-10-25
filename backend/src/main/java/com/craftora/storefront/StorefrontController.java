package com.craftora.storefront;

import com.craftora.security.AuthUser;
import com.craftora.storefront.StorefrontDtos.StorefrontStateResponse;
import com.craftora.storefront.StorefrontDtos.StorefrontView;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Storefronts")
@RestController
public class StorefrontController {

    private final StorefrontService service;

    public StorefrontController(StorefrontService service) {
        this.service = service;
    }

    @GetMapping("/api/public/storefronts/{slug}")
    public StorefrontView published(@PathVariable String slug) {
        return service.published(slug);
    }

    @GetMapping("/api/maker/storefront")
    public StorefrontStateResponse state(@AuthenticationPrincipal AuthUser user) {
        return service.state(user.id());
    }

    @PutMapping("/api/maker/storefront")
    public StorefrontStateResponse save(@AuthenticationPrincipal AuthUser user,
                                        @Valid @RequestBody StorefrontConfig config) {
        return service.saveDraft(user.id(), config);
    }

    @PostMapping(path = "/api/maker/storefront/hero-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StorefrontStateResponse hero(@AuthenticationPrincipal AuthUser user, @RequestPart("file") MultipartFile file) {
        return service.uploadHero(user.id(), file);
    }

    @PostMapping("/api/maker/storefront/publish")
    public StorefrontStateResponse publish(@AuthenticationPrincipal AuthUser user) {
        return service.publish(user.id());
    }

    @PostMapping("/api/maker/storefront/unpublish")
    public StorefrontStateResponse unpublish(@AuthenticationPrincipal AuthUser user) {
        return service.unpublish(user.id());
    }

    @GetMapping("/api/maker/storefront/preview")
    public StorefrontView preview(@AuthenticationPrincipal AuthUser user) {
        return service.preview(user.id());
    }
}
