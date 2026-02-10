package com.malcolm.ecomai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McpSession {

    private static final Logger logger = LoggerFactory.getLogger(McpSession.class);
    private static final Duration ENDPOINT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);

    private final URI baseUri;
    private final URI sseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicReference<CompletableFuture<URI>> endpointFutureRef;
    private final ConcurrentMap<String, CompletableFuture<String>> pending;

    public McpSession(String baseUrl) {
        this.baseUri = URI.create(Objects.requireNonNull(baseUrl));
        this.sseUri = this.baseUri.resolve("/sse");
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
        this.endpointFutureRef = new AtomicReference<>(new CompletableFuture<>());
        this.pending = new ConcurrentHashMap<>();
        startSseListener(endpointFutureRef.get());
    }

    public String call(Map<String, Object> request) {
        Objects.requireNonNull(request, "request");

        Object idValue = request.get("id");
        if (idValue == null) {
            throw new IllegalArgumentException("MCP request must include an id");
        }

        String id = String.valueOf(idValue);
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        pending.put(id, responseFuture);

        try {
            URI endpoint = ensureEndpoint().get(ENDPOINT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            String json = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        CompletableFuture<String> pendingFuture = pending.remove(id);
                        if (pendingFuture != null) {
                            pendingFuture.completeExceptionally(ex);
                        }
                        return null;
                    });

            return responseFuture.get(RESPONSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pending.remove(id);
            throw new RuntimeException("MCP call timed out after " + RESPONSE_TIMEOUT.toSeconds() + "s", e);
        } catch (Exception e) {
            pending.remove(id);
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new RuntimeException("MCP call failed: " + message, e);
        }
    }

    private CompletableFuture<URI> ensureEndpoint() {
        CompletableFuture<URI> current = endpointFutureRef.get();
        if (current.isCompletedExceptionally()) {
            CompletableFuture<URI> fresh = new CompletableFuture<>();
            if (endpointFutureRef.compareAndSet(current, fresh)) {
                startSseListener(fresh);
            }
            current = endpointFutureRef.get();
        }
        return current;
    }

    private void startSseListener(CompletableFuture<URI> endpointFuture) {
        HttpRequest request = HttpRequest.newBuilder(sseUri)
                .GET()
                .header("Accept", "text/event-stream")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    if (response.statusCode() >= 400) {
                        logger.warn("SSE connection failed with status {}", response.statusCode());
                        endpointFuture.completeExceptionally(
                                new IllegalStateException("SSE connection failed with status " + response.statusCode()));
                        return;
                    }
                    logger.info("Connected to MCP SSE {}", sseUri);
                    Thread.startVirtualThread(() -> readSseStream(response.body()));
                })
                .exceptionally(ex -> {
                    logger.warn("SSE connection error", ex);
                    endpointFuture.completeExceptionally(ex);
                    return null;
                });
    }

    private void readSseStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            String event = null;
            StringBuilder data = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    dispatchEvent(event, data.toString());
                    event = null;
                    data.setLength(0);
                    continue;
                }

                if (line.startsWith("event:")) {
                    event = line.substring("event:".length()).trim();
                    continue;
                }

                if (line.startsWith("data:")) {
                    if (data.length() > 0) {
                        data.append('\n');
                    }
                    data.append(line.substring("data:".length()).trim());
                }
            }
            logger.warn("SSE stream closed by server {}", sseUri);
            endpointFutureRef.get().completeExceptionally(
                    new IllegalStateException("SSE stream closed"));
        } catch (Exception ex) {
            logger.warn("SSE stream error", ex);
            endpointFutureRef.get().completeExceptionally(ex);
        }
    }

    private void dispatchEvent(String event, String data) {
        if (data == null || data.isBlank()) {
            return;
        }

        if ("endpoint".equals(event)) {
            URI endpoint = baseUri.resolve(data.trim());
            logger.info("Received MCP endpoint {}", endpoint);
            endpointFutureRef.get().complete(endpoint);
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(data);
            JsonNode idNode = node.get("id");
            if (idNode == null || idNode.isNull()) {
                return;
            }

            String id = idNode.asText();
            CompletableFuture<String> responseFuture = pending.remove(id);
            if (responseFuture != null) {
                logger.debug("Received MCP response for id={}", id);
                responseFuture.complete(data);
            }
        } catch (Exception ignored) {
            // Ignore malformed messages to keep the SSE loop alive.
        }
    }
}
