package com.project.tas_pbo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client used by POSController to talk to the Spring Boot QRIS server.
 * Spring Boot must be running on localhost:8080 before calling these methods.
 */
public class QrisClient {

    private static final String BASE_URL = "http://localhost:8080/api/qris";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Creates a new QRIS charge for the given amount.
     * Returns the view URL to open on mobile (http://localhost:8080/qris).
     * Returns null if server is unreachable.
     */
    public static QrisResult createQris(long amount) {
        try {
            String body = "{\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());

            if (json.path("success").asBoolean()) {
                return new QrisResult(
                        true,
                        json.path("orderId").asText(),
                        json.path("viewUrl").asText(),
                        null
                );
            } else {
                return new QrisResult(false, null, null,
                        json.path("error").asText("Unknown error"));
            }

        } catch (Exception e) {
            return new QrisResult(false, null, null,
                    "Server QRIS tidak bisa dihubungi: " + e.getMessage());
        }
    }

    /**
     * Polls payment status.
     * Returns: PENDING, SUCCESS, EXPIRED, CANCELLED, NO_SESSION, ERROR
     */
    public static String checkStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/status"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            return json.path("status").asText("ERROR");

        } catch (Exception e) {
            return "ERROR";
        }
    }

    /**
     * Cancels the current QRIS session.
     * Call when cashier switches payment method or resets transaction.
     */
    public static void cancelQris() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cancel"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("QRIS cancel failed: " + e.getMessage());
        }
    }

    /**
     * Marks QRIS session as complete after saving transaction to DB.
     * Clears mobile page so it shows waiting state for next customer.
     */
    public static void completeQris() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/complete"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("QRIS complete failed: " + e.getMessage());
        }
    }

    // =========================================================
    // Result wrapper
    // =========================================================
    public static class QrisResult {
        public final boolean success;
        public final String orderId;
        public final String viewUrl;  // URL to show cashier: open on phone
        public final String error;

        public QrisResult(boolean success, String orderId, String viewUrl, String error) {
            this.success = success;
            this.orderId = orderId;
            this.viewUrl = viewUrl;
            this.error = error;
        }
    }
}