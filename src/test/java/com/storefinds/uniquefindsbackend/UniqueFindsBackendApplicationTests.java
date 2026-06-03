package com.storefinds.uniquefindsbackend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Basic application bootstrap smoke test.
 */
class UniqueFindsBackendApplicationTests {

    @Test
    void applicationClassIsLoadable() {
        assertEquals("com.storefinds.uniquefindsbackend.UniqueFindsBackendApplication",
                UniqueFindsBackendApplication.class.getName());
    }
}
