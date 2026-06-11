package com.example.urlshortener.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "url_mappings", indexes = {
        @Index(name = "idx_url_mappings_code", columnList = "code"),
        @Index(name = "idx_url_mappings_long_url", columnList = "longUrl")
})
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true, length = 64)
    private String code;

    @Column(nullable = false, unique = true, length = 2048)
    private String longUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
