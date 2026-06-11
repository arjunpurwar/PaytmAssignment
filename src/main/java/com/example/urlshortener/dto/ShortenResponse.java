package com.example.urlshortener.dto;

public class ShortenResponse {
    private final String code;
    private final String shortUrl;
    private final String longUrl;
    private final boolean created;

    public ShortenResponse(String code, String shortUrl, String longUrl, boolean created) {
        this.code = code;
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.created = created;
    }

    public String getCode() {
        return code;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public boolean isCreated() {
        return created;
    }
}
