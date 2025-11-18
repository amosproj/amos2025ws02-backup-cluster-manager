package com.bcm.shared.service;

import java.security.SecureRandom;

public class NodeIdGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static long nextId() {
        return Math.abs(random.nextLong());
    }
}
