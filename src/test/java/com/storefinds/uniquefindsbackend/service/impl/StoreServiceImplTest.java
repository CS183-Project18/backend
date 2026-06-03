package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.entity.Store;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.StoreMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Verify store management behavior used in structured discovery flows.
 */
@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private IndexSyncService indexSyncService;

    @InjectMocks
    private StoreServiceImpl storeService;

    @Test
    void updateStoreStatusRejectsInvalidStatus() {
        Store store = new Store();
        store.setId(5L);
        store.setStatus("ACTIVE");
        when(storeMapper.selectById(5L)).thenReturn(store);

        BusinessException ex = assertThrows(BusinessException.class, () -> storeService.updateStoreStatus(5L, "ARCHIVED"));

        assertEquals("status is invalid", ex.getMessage());
    }

    @Test
    void getStoreByIdHidesNonPublicStoreFromGuestReads() {
        Store store = new Store();
        store.setId(5L);
        store.setStatus("HIDDEN");
        when(storeMapper.selectById(5L)).thenReturn(store);

        BusinessException ex = assertThrows(BusinessException.class, () -> storeService.getStoreById(5L, true));

        assertEquals("store not found", ex.getMessage());
    }
}
