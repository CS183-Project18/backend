package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateStoreRequest;
import com.storefinds.uniquefindsbackend.dto.StoreResponse;
import com.storefinds.uniquefindsbackend.dto.StoreSummaryResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateStoreRequest;
import com.storefinds.uniquefindsbackend.entity.Store;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.StoreMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import com.storefinds.uniquefindsbackend.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Implement store query, selection validation, and admin maintenance flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final IndexSyncService indexSyncService;

    public StoreServiceImpl(StoreMapper storeMapper,
                            IndexSyncService indexSyncService) {
        this.storeMapper = storeMapper;
        this.indexSyncService = indexSyncService;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query stores for public discovery or admin management contexts.
     * Params:
     * - publicOnly: whether to keep only publicly visible stores
     * Returns:
     * - Result<List<StoreResponse>>: matched store list
     * Throws: None
     */
    public Result<List<StoreResponse>> getStores(boolean publicOnly) {
        List<Store> stores = publicOnly ? storeMapper.selectPublic() : storeMapper.selectAll();
        return Result.success(stores.stream().map(this::toResponse).toList());
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query one store detail with optional public-visibility enforcement.
     * Params:
     * - storeId: target store id
     * - publicOnly: whether hidden stores should be blocked
     * Returns:
     * - Result<StoreResponse>: matched store detail
     * Throws:
     * - BusinessException: when target store is not found or not publicly visible
     */
    public Result<StoreResponse> getStoreById(Long storeId, boolean publicOnly) {
        Store store = requireStore(storeId);
        if (publicOnly && !"ACTIVE".equalsIgnoreCase(store.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "store not found");
        }
        return Result.success(toResponse(store));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one structured store record for admin maintenance.
     * Params:
     * - adminUserId: creator admin id
     * - request: create store payload
     * Returns:
     * - Result<StoreResponse>: created store detail
     * Throws: None
     */
    public Result<StoreResponse> createStore(Long adminUserId, CreateStoreRequest request) {
        Store store = new Store();
        applyStoreRequest(store, request);
        store.setStatus("ACTIVE");
        store.setCreatedBy(adminUserId);
        storeMapper.insert(store);
        return Result.success("store created", toResponse(storeMapper.selectById(store.getId())));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one existing store's structured business information.
     * Params:
     * - storeId: target store id
     * - request: update store payload
     * Returns:
     * - Result<StoreResponse>: updated store detail
     * Throws:
     * - BusinessException: when target store does not exist
     */
    public Result<StoreResponse> updateStore(Long storeId, UpdateStoreRequest request) {
        Store store = requireStore(storeId);
        applyStoreRequest(store, request);
        storeMapper.updateById(store);
        indexSyncService.scheduleRebuild("store updated: storeId=" + storeId);
        return Result.success("store updated", toResponse(storeMapper.selectById(storeId)));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Change one store's public visibility lifecycle status.
     * Params:
     * - storeId: target store id
     * - status: target store status
     * Returns:
     * - Result<Void>: update result
     * Throws:
     * - BusinessException: when target store does not exist or status is invalid
     */
    public Result<Void> updateStoreStatus(Long storeId, String status) {
        requireStore(storeId);
        String normalizedStatus = normalizeRequiredText(status, "status is required").toUpperCase();
        if (!List.of("ACTIVE", "HIDDEN", "CLOSED").contains(normalizedStatus)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "status is invalid");
        }
        storeMapper.updateStatusById(storeId, normalizedStatus);
        indexSyncService.scheduleRebuild("store status updated: storeId=" + storeId);
        return Result.success("store status updated", null);
    }

    @Override
    public Map<Long, StoreSummaryResponse> getStoreSummaryMap(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, StoreSummaryResponse> summaryMap = new LinkedHashMap<>();
        for (Long storeId : storeIds) {
            if (storeId == null || summaryMap.containsKey(storeId)) {
                continue;
            }
            Store store = storeMapper.selectById(storeId);
            if (store != null) {
                summaryMap.put(storeId, toSummary(store));
            }
        }
        return summaryMap;
    }

    @Override
    public void validateStoreSelectable(Long storeId) {
        if (storeId == null) {
            return;
        }
        Store store = requireStore(storeId);
        if (!"ACTIVE".equalsIgnoreCase(store.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "store is not active");
        }
    }

    private Store requireStore(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "store not found");
        }
        return store;
    }

    private void applyStoreRequest(Store store, CreateStoreRequest request) {
        store.setName(normalizeRequiredText(request.getName(), "name is required"));
        store.setBranchName(normalizeOptionalText(request.getBranchName()));
        store.setCity(normalizeOptionalText(request.getCity()));
        store.setDistrict(normalizeOptionalText(request.getDistrict()));
        store.setAddress(normalizeOptionalText(request.getAddress()));
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setPhone(normalizeOptionalText(request.getPhone()));
        store.setBusinessHours(normalizeOptionalText(request.getBusinessHours()));
    }

    private void applyStoreRequest(Store store, UpdateStoreRequest request) {
        store.setName(normalizeRequiredText(request.getName(), "name is required"));
        store.setBranchName(normalizeOptionalText(request.getBranchName()));
        store.setCity(normalizeOptionalText(request.getCity()));
        store.setDistrict(normalizeOptionalText(request.getDistrict()));
        store.setAddress(normalizeOptionalText(request.getAddress()));
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setPhone(normalizeOptionalText(request.getPhone()));
        store.setBusinessHours(normalizeOptionalText(request.getBusinessHours()));
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, errorMessage);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private StoreResponse toResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setName(store.getName());
        response.setBranchName(store.getBranchName());
        response.setCity(store.getCity());
        response.setDistrict(store.getDistrict());
        response.setAddress(store.getAddress());
        response.setLatitude(store.getLatitude());
        response.setLongitude(store.getLongitude());
        response.setPhone(store.getPhone());
        response.setBusinessHours(store.getBusinessHours());
        response.setStatus(store.getStatus());
        response.setCreatedBy(store.getCreatedBy());
        response.setCreatedAt(store.getCreatedAt());
        response.setUpdatedAt(store.getUpdatedAt());
        return response;
    }

    private StoreSummaryResponse toSummary(Store store) {
        StoreSummaryResponse response = new StoreSummaryResponse();
        response.setId(store.getId());
        response.setName(store.getName());
        response.setBranchName(store.getBranchName());
        response.setCity(store.getCity());
        response.setDistrict(store.getDistrict());
        response.setStatus(store.getStatus());
        return response;
    }
}
