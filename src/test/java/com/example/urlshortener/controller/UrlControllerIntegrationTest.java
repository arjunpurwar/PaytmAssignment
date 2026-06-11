package com.example.urlshortener.controller;

import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shortenAndRedirectRoundTripWorks() throws Exception {
        String payload = """
                {"longUrl":"https://example.com/articles/123"}
                """;

        String response = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/")))
                .andExpect(jsonPath("$.longUrl").value("https://example.com/articles/123"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String code = JsonPath.read(response, "$.code");

        mockMvc.perform(get("/" + code))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "https://example.com/articles/123"));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        mockMvc.perform(get("/doesnotexist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateUrlReturnsSameCode() throws Exception {
        String payload = """
                {"longUrl":"https://example.com/dup"}
                """;

        String first = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstCode = JsonPath.read(first, "$.code");
        String secondCode = JsonPath.read(second, "$.code");
        org.junit.jupiter.api.Assertions.assertEquals(firstCode, secondCode);
    }

    @Test
    void customAliasIsHonoredAndConflictIsReturnedOnReuse() throws Exception {
        String payload = """
                {"longUrl":"https://example.com/custom","customAlias":"MyAlias1"}
                """;

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("MyAlias1"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/MyAlias1"));

        String conflict = """
                {"longUrl":"https://example.com/other","customAlias":"MyAlias1"}
                """;

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflict))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidUrlReturnsBadRequest() throws Exception {
        String payload = """
                {"longUrl":"not-a-url"}
                """;

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
