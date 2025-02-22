package com.tyv.storageservice.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Bucket {
    PRODUCT("e45331f2-5941-4b35-baa0-10e01f016f1e"),
    COMMENT("9550a615-edd0-44b0-a2ca-507d6d6f5aeb"),
    USER("724c2a96-1e1b-4889-969c-f151c449f510");

    private final String uuid;

    public String getWithUUID() {
        return super.toString().toLowerCase() + "-" + uuid;
    }
}
