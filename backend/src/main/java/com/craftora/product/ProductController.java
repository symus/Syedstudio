package com.craftora.product;

import com.craftora.common.PageResponse;
import com.craftora.product.ProductDtos.ImageOrderRequest;
import com.craftora.product.ProductDtos.MakerProductResponse;
import com.craftora.product.ProductDtos.ProductRequest;
import com.craftora.product.ProductDtos.StatusRequest;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Maker products")
@RestController
@RequestMapping("/api/maker/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MakerProductResponse> list(@AuthenticationPrincipal AuthUser user,
                                                   @RequestParam(required = false) ProductEnums.Status status,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer size) {
        return service.list(user.id(), status, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MakerProductResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ProductRequest req) {
        return service.create(user.id(), req);
    }

    @GetMapping("/{id}")
    public MakerProductResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        return service.get(user.id(), id);
    }

    @PutMapping("/{id}")
    public MakerProductResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                       @Valid @RequestBody ProductRequest req) {
        return service.update(user.id(), id, req);
    }

    @PostMapping("/{id}/status")
    public MakerProductResponse status(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                       @Valid @RequestBody StatusRequest req) {
        return service.changeStatus(user.id(), id, req.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        service.delete(user.id(), id);
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MakerProductResponse addImage(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                         @RequestPart("file") MultipartFile file,
                                         @RequestParam(required = false) String altText) {
        return service.addImage(user.id(), id, file, altText);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public MakerProductResponse removeImage(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                            @PathVariable UUID imageId) {
        return service.removeImage(user.id(), id, imageId);
    }

    @PutMapping("/{id}/images/order")
    public MakerProductResponse reorder(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                        @Valid @RequestBody ImageOrderRequest req) {
        return service.reorderImages(user.id(), id, req.imageIds());
    }
}
