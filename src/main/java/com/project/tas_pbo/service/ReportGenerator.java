package com.project.tas_pbo.service;

import com.project.tas_pbo.model.LaporanHarian;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    private static final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    // =========================================================
    // GENERATE REPORT TEXT
    // =========================================================
    public static String generateReportText(
            List<LaporanHarian> dailyData,
            LaporanHarian summary,
            List<String[]> topProduk,
            int periodDays
    ) {
        StringBuilder sb = new StringBuilder();
        String sep = "=".repeat(55);
        String line = "-".repeat(55);

        sb.append(sep).append("\n");
        sb.append(center("LAPORAN KEUANGAN", 55)).append("\n");
        sb.append(center("TOKO KELONTONG", 55)).append("\n");
        sb.append(sep).append("\n");

        String period = periodDays == 7 ? "7 Hari Terakhir" : "30 Hari Terakhir";
        sb.append("Periode  : ").append(period).append("\n");
        sb.append("Dicetak  : ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ).append("\n");
        sb.append(line).append("\n");

        // Summary
        sb.append("\nRINGKASAN\n");
        sb.append(line).append("\n");
        sb.append(row("Total Transaksi", String.valueOf(summary.getJumlahTransaksi()), 55)).append("\n");
        sb.append(row("Total Penjualan", "Rp " + rupiahFormat.format((long) summary.getTotalPenjualan()), 55)).append("\n");
        sb.append(row("Rata-rata/Hari", "Rp " + rupiahFormat.format((long) summary.getRataRata()), 55)).append("\n");

        // Daily breakdown
        sb.append("\nDETAIL PENJUALAN HARIAN\n");
        sb.append(line).append("\n");
        sb.append(String.format("%-12s %10s %20s%n", "Tanggal", "Transaksi", "Total (Rp)"));
        sb.append(line).append("\n");

        if (dailyData.isEmpty()) {
            sb.append("Tidak ada data penjualan pada periode ini.\n");
        } else {
            for (LaporanHarian h : dailyData) {
                sb.append(String.format("%-12s %10d %20s%n",
                        h.getTanggalFormatted(),
                        h.getJumlahTransaksi(),
                        rupiahFormat.format((long) h.getTotalPenjualan())
                ));
            }
        }

        // Top products
        if (!topProduk.isEmpty()) {
            sb.append("\nPRODUK TERLARIS\n");
            sb.append(line).append("\n");
            sb.append(String.format("%-3s %-25s %8s %14s%n", "No", "Produk", "Terjual", "Pendapatan (Rp)"));
            sb.append(line).append("\n");

            int no = 1;
            for (String[] p : topProduk) {
                sb.append(String.format("%-3d %-25s %8s %14s%n",
                        no++,
                        p[0].length() > 24 ? p[0].substring(0, 24) : p[0],
                        p[1],
                        rupiahFormat.format((long) Double.parseDouble(p[2]))
                ));
            }
        }

        sb.append("\n").append(sep).append("\n");
        sb.append(center("--- Akhir Laporan ---", 55)).append("\n");
        sb.append(sep).append("\n");

        return sb.toString();
    }

    // =========================================================
    // SAVE AS PDF (via print to PDF)
    // =========================================================
    public static void saveAsPdf(
            String reportText,
            Stage ownerStage
    ) {
        // Save as .txt first (Windows PDF printer will handle the rest)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan");
        fileChooser.setInitialFileName("Laporan_Keuangan_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(reportText);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Laporan berhasil disimpan ke:\n" + file.getAbsolutePath() +
                                "\n\nUntuk PDF: buka file → Print → pilih 'Microsoft Print to PDF'");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan laporan: " + e.getMessage());
            }
        }
    }

    // =========================================================
    // PRINT DIRECTLY
    // =========================================================
    public static void printReport(String reportText) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada printer yang tersedia.");
            return;
        }

        // Build printable node
        TextArea textArea = new TextArea(reportText);
        textArea.setFont(Font.font("Courier New", 10));
        textArea.setEditable(false);
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(800);
        textArea.setWrapText(false);

        boolean showDialog = job.showPrintDialog(null);
        if (showDialog) {
            PageLayout pageLayout = job.getPrinter().createPageLayout(
                    Paper.A4,
                    PageOrientation.PORTRAIT,
                    Printer.MarginType.DEFAULT
            );

            boolean printed = job.printPage(pageLayout, textArea);
            if (printed) {
                job.endJob();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Laporan berhasil dicetak.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal mencetak laporan.");
            }
        }
    }

    // =========================================================
    // SHOW PREVIEW DIALOG
    // =========================================================
    public static void showPreview(String reportText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preview Laporan");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(reportText);
        textArea.setEditable(false);
        textArea.setFont(Font.font("Courier New", 11));
        textArea.setPrefSize(600, 500);
        textArea.setWrapText(false);

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(650, 560);
        alert.showAndWait();
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private static String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private static String row(String left, String right, int width) {
        int space = width - left.length() - right.length();
        if (space < 1) space = 1;
        return left + " ".repeat(space) + right;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}