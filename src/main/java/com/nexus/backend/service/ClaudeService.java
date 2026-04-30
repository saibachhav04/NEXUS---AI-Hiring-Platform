package com.nexus.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {

    @Value("${groq.api.key}")
    private String apiKey;
    

    @Value("${groq.model}")
    private String model;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ClaudeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String ask(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", 1500,
                    "temperature", 0.1,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are a precise JSON generator. Return ONLY valid JSON. No explanation. No markdown. No code blocks."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    )
            );

            JsonNode response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            return response
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

        } catch (WebClientResponseException e) {
            throw new RuntimeException(
                    "Groq error: " + e.getStatusCode()
                            + " body: " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Groq API call failed: "
                    + e.getMessage());
        }
    }

    public <T> T askForObject(String prompt, Class<T> responseType) {
        try {
            String raw = ask(prompt);
            String cleaned = raw
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, responseType);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AI response: " + e.getMessage());
        }
    }
}