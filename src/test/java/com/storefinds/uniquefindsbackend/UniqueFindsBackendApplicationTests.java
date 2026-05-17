package com.storefinds.uniquefindsbackend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UniqueFindsBackendApplicationTests {

    @Test
    void applicationClassIsLoadable() {
        assertEquals("com.storefinds.uniquefindsbackend.UniqueFindsBackendApplication",
                UniqueFindsBackendApplication.class.getName());
    }
}
