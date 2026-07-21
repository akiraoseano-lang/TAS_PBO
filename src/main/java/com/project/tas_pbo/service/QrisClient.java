package com.project.tas_pbo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client yang digunakan POSController untuk berkomunikasi dengan server Spring Boot QRIS.
 * Server Spring Boot harus berjalan di localhost:8080 sebelum memanggil method ini.
 */
public class QrisClient {

    private static final String BASE_URL = "http://localhost:8080/api/qris";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Membuat tagihan QRIS baru untuk jumlah tertentu.
     * Mengembalikan URL yang bisa dibuka di HP (http://localhost:8080/qris).
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
     * Mengecek status pembayaran.
     * Mengembalikan: PENDING, SUCCESS, EXPIRED, CANCELLED, NO_SESSION, ERROR
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
     * Membatalkan sesi QRIS yang sedang aktif.
     * Dipanggil saat kasir mengganti metode pembayaran atau mereset transaksi.
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
            System.err.println("QRIS cancel gagal: " + e.getMessage());
        }
    }

    /**
     * Menandai sesi QRIS sebagai selesai setelah transaksi disimpan ke DB.
     * Membersihkan halaman mobile agar menampilkan status menunggu untuk pelanggan berikutnya.
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
            System.err.println("QRIS complete gagal: " + e.getMessage());
        }
    }

    // Pembungkus hasil QRIS
    public static class QrisResult {
        public final boolean success;
        public final String orderId;
        public final String viewUrl;  // URL untuk dibuka di HP kasir
        public final String error;

        public QrisResult(boolean success, String orderId, String viewUrl, String error) {
            this.success = success;
            this.orderId = orderId;
            this.viewUrl = viewUrl;
            this.error = error;
        }
    }
}