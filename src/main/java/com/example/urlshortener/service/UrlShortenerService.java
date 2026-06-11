package com.example.urlshortener.service;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.exception.BadRequestException;
import com.example.urlshortener.exception.ConflictException;
import com.example.urlshortener.exception.NotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Service
public class UrlShortenerService {

    private static final String ALIAS_PATTERN = "^[A-Za-z0-9]{3,32}$";

    private final UrlMappingRepository repository;
    private final String baseUrl;

    public UrlShortenerService(UrlMappingRepository repository,
                               @Value("${app.base-url:http://localhost:8080/}") String baseUrl) {
        this.repository = repository;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        String longUrl = normalizeAndValidateLongUrl(request.getLongUrl());
        String customAlias = normalizeAlias(request.getCustomAlias());

        if (customAlias != null) {
            return shortenWithCustomAlias(longUrl, customAlias);
        }

        UrlMapping existing = repository.findByLongUrl(longUrl).orElse(null);
        if (existing != null) {
            if (customAlias != null && !customAlias.equals(existing.getCode())) {
                throw new ConflictException("URL already shortened with a different code");
            }
            ensureCodePresent(existing);
            return toResponse(existing, false);
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl);
        repository.saveAndFlush(mapping);

        String generatedCode = Base62Encoder.encode(mapping.getId());
        mapping.setCode(generatedCode);
        repository.saveAndFlush(mapping);

        return toResponse(mapping, true);
    }

    @Transactional(readOnly = true)
    public String resolve(String code) {
        String normalizedCode = normalizeCode(code);
        return repository.findByCode(normalizedCode)
                .map(UrlMapping::getLongUrl)
                .orElseThrow(() -> new NotFoundException("Unknown short code: " + normalizedCode));
    }

    private ShortenResponse shortenWithCustomAlias(String longUrl, String customAlias) {
        UrlMapping byAlias = repository.findByCode(customAlias).orElse(null);
        if (byAlias != null) {
            if (byAlias.getLongUrl().equals(longUrl)) {
                return toResponse(byAlias, false);
            }
            throw new ConflictException("Custom alias is already in use: " + customAlias);
        }

        UrlMapping existing = repository.findByLongUrl(longUrl).orElse(null);
        if (existing != null) {
            if (customAlias != null && !customAlias.equals(existing.getCode())) {
                throw new ConflictException("URL already shortened with a different code");
            }
            ensureCodePresent(existing);
            return toResponse(existing, false);
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl);
        mapping.setCode(customAlias);
        repository.saveAndFlush(mapping);
        return toResponse(mapping, true);
    }

    private void ensureCodePresent(UrlMapping mapping) {
        if (mapping.getCode() == null || mapping.getCode().isBlank()) {
            String generatedCode = Base62Encoder.encode(mapping.getId());
            mapping.setCode(generatedCode);
            repository.saveAndFlush(mapping);
        }
    }

    private ShortenResponse toResponse(UrlMapping mapping, boolean created) {
        return new ShortenResponse(mapping.getCode(), buildShortUrl(mapping.getCode()), mapping.getLongUrl(), created);
    }

    private String buildShortUrl(String code) {
        return baseUrl + code;
    }

    private String normalizeAndValidateLongUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new BadRequestException("longUrl is required");
        }
        String trimmed = rawUrl.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("Only http and https URLs are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new BadRequestException("URL must include a valid host");
            }
            return trimmed;
        } catch (URISyntaxException e) {
            throw new BadRequestException("Invalid URL format");
        }
    }

    private String normalizeAlias(String customAlias) {
        if (customAlias == null || customAlias.isBlank()) {
            return null;
        }
        String trimmed = customAlias.trim();
        if (!trimmed.matches(ALIAS_PATTERN)) {
            throw new BadRequestException("customAlias must be 3-32 characters of A-Z, a-z, or 0-9");
        }
        return trimmed;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Short code is required");
        }
        return code.trim();
    }
}
