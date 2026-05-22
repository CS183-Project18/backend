package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.StoreResponse;
import com.storefinds.uniquefindsbackend.service.StoreService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/stores")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose public store query endpoints for structured discovery experiences.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class StoreController {

    private final StoreService storeService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject store service for public store query endpoints.
     * Params:
     * - storeService: store business service
     * Returns: None
     * Throws: None
     */
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query all publicly visible stores for discovery filters and detail links.
     * Params: None
     * Returns:
     * - Result<List<StoreResponse>>: visible store list
     * Throws: None
     */
    public Result<List<StoreResponse>> getStores() {
        return storeService.getStores(true);
    }

    @GetMapping("/{storeId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query one public store detail by primary key.
     * Params:
     * - storeId: target store id
     * Returns:
     * - Result<StoreResponse>: matched store detail
     * Throws: None
     */
    public Result<StoreResponse> getStore(@PathVariable @Min(value = 1, message = "storeId must be greater than 0") Long storeId) {
        return storeService.getStoreById(storeId, true);
    }
}
