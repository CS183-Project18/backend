package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateStoreRequest;
import com.storefinds.uniquefindsbackend.dto.StoreResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateStoreRequest;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.StoreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/stores")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose admin store maintenance endpoints for structured content management.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminStoreController {

    private final StoreService storeService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject store service for admin store maintenance endpoints.
     * Params:
     * - storeService: store business service
     * Returns: None
     * Throws: None
     */
    public AdminStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one store record through the admin management console.
     * Params:
     * - request: create store payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<StoreResponse>: created store detail
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<StoreResponse> createStore(@RequestBody @Valid CreateStoreRequest request,
                                             Authentication authentication) {
        return storeService.createStore(requireCurrentUser(authentication).userId(), request);
    }

    @PutMapping("/{storeId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one store's structured business information.
     * Params:
     * - storeId: target store id
     * - request: update store payload
     * Returns:
     * - Result<StoreResponse>: updated store detail
     * Throws: None
     */
    public Result<StoreResponse> updateStore(@PathVariable @Min(1) Long storeId,
                                             @RequestBody @Valid UpdateStoreRequest request) {
        return storeService.updateStore(storeId, request);
    }

    @PutMapping("/{storeId}/status")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one store's visibility lifecycle status.
     * Params:
     * - storeId: target store id
     * - status: target store status
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    public Result<Void> updateStoreStatus(@PathVariable @Min(1) Long storeId,
                                          @RequestParam String status) {
        return storeService.updateStoreStatus(storeId, status);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Extract current authenticated admin user from spring security context.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - CurrentUser: authenticated principal wrapper
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }
}
