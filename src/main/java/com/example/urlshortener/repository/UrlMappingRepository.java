package com.example.urlshortener.repository;

import com.example.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByCode(String code);
    Optional<UrlMapping> findByLongUrl(String longUrl);
    boolean existsByCode(String code);
}
