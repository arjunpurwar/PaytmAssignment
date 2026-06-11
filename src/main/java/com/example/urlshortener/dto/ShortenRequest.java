package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public class ShortenRequest {

    @NotBlank(message = "longUrl is required")
    private String longUrl;

    private String customAlias;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }
}
