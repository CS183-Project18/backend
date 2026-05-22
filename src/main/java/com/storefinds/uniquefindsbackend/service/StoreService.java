package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateStoreRequest;
import com.storefinds.uniquefindsbackend.dto.StoreResponse;
import com.storefinds.uniquefindsbackend.dto.StoreSummaryResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateStoreRequest;

import java.util.List;
import java.util.Map;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Define public store query, selection validation, and admin maintenance capabilities.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface StoreService {

    Result<List<StoreResponse>> getStores(boolean publicOnly);

    Result<StoreResponse> getStoreById(Long storeId, boolean publicOnly);

    Result<StoreResponse> createStore(Long adminUserId, CreateStoreRequest request);

    Result<StoreResponse> updateStore(Long storeId, UpdateStoreRequest request);

    Result<Void> updateStoreStatus(Long storeId, String status);

    Map<Long, StoreSummaryResponse> getStoreSummaryMap(List<Long> storeIds);

    void validateStoreSelectable(Long storeId);
}
