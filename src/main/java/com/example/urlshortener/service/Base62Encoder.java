package com.example.urlshortener.service;

public final class Base62Encoder {
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private Base62Encoder() {
    }

    public static String encode(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        StringBuilder builder = new StringBuilder();
        long current = value;
        while (current > 0) {
            int remainder = (int) (current % 62);
            builder.append(ALPHABET[remainder]);
            current /= 62;
        }
        return builder.reverse().toString();
    }
}
