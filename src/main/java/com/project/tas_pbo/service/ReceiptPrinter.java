package com.project.tas_pbo.service;

import com.project.tas_pbo.model.Member;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptPrinter {

    private static final DecimalFormat rupiahFormat = new DecimalFormat("#,###");
    private static final int WIDTH = 40; // characters wide

    public static String generateReceipt(
            Penjualan penjualan,
            List<PenjualanDetail> items,
            Member member,
            double diskonRate,
            double potongan
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append(center("TOKO KELONTONG", WIDTH)).append("\n");
        sb.append(center("Jl. Contoh No. 1, Jakarta", WIDTH)).append("\n");
        sb.append(center("Telp: 021-12345678", WIDTH)).append("\n");
        sb.append(line(WIDTH)).append("\n");

        sb.append("No  : ").append(penjualan.getNoTransaksi()).append("\n");
        sb.append("Tgl : ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ).append("\n");

        if (member != null) {
            sb.append("Member: ").append(member.getNamaMember())
                    .append(" (").append(member.getKodeMember()).append(")\n");
        }

        sb.append(line(WIDTH)).append("\n");

        for (PenjualanDetail item : items) {
            sb.append(item.getNamaProduk()).append("\n");
            sb.append("  ")
                    .append(item.getJumlah()).append(" x ")
                    .append("Rp ").append(rupiahFormat.format(item.getHargaSatuan()))
                    .append(padLeft("Rp " + rupiahFormat.format(item.getSubtotal()), WIDTH - 20))
                    .append("\n");
        }

        sb.append(line(WIDTH)).append("\n");

        sb.append(row("Subtotal", "Rp " + rupiahFormat.format(penjualan.getTotalBelanja() + potongan), WIDTH)).append("\n");

        if (diskonRate > 0) {
            sb.append(row("Diskon (" + (int)(diskonRate * 100) + "%)",
                    "- Rp " + rupiahFormat.format(potongan), WIDTH)).append("\n");
        }

        sb.append(row("TOTAL", "Rp " + rupiahFormat.format(penjualan.getTotalBelanja()), WIDTH)).append("\n");
        sb.append(row("Bayar", "Rp " + rupiahFormat.format(penjualan.getBayar()), WIDTH)).append("\n");
        sb.append(row("Kembalian", "Rp " + rupiahFormat.format(penjualan.getKembalian()), WIDTH)).append("\n");

        sb.append(line(WIDTH)).append("\n");
        sb.append(center("Terima kasih telah berbelanja!", WIDTH)).append("\n");
        sb.append(center("Simpan struk ini sebagai bukti", WIDTH)).append("\n");
        sb.append(line(WIDTH)).append("\n");

        return sb.toString();
    }

    public static void printToPrinter(
            Penjualan penjualan,
            List<PenjualanDetail> items,
            Member member,
            double diskonRate,
            double potongan
    ) {
        String receiptText = generateReceipt(penjualan, items, member, diskonRate, potongan);

        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();

        if (job == null) {
            showPrintFallback(receiptText);
            return;
        }

        javafx.scene.text.Text text = new javafx.scene.text.Text(receiptText);
        text.setFont(javafx.scene.text.Font.font("Courier New", 10));

        boolean proceed = job.showPrintDialog(null);
        if (proceed) {
            boolean printed = job.printPage(text);
            if (printed) {
                job.endJob();
            }
        }
    }

    public static void showReceiptDialog(
            Penjualan penjualan,
            List<PenjualanDetail> items,
            Member member,
            double diskonRate,
            double potongan
    ) {
        String receiptText = generateReceipt(penjualan, items, member, diskonRate, potongan);
        showPrintFallback(receiptText);
    }

    private static void showPrintFallback(String receiptText) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Struk Pembayaran");
        alert.setHeaderText(null);

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setFont(javafx.scene.text.Font.font("Courier New", 12));
        textArea.setPrefSize(400, 500);

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(450, 550);
        alert.showAndWait();
    }

    private static String center(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private static String line(int width) {
        return "-".repeat(width);
    }

    private static String padLeft(String text, int width) {
        if (text.length() >= width) return text;
        return " ".repeat(width - text.length()) + text;
    }

    private static String row(String left, String right, int width) {
        int space = width - left.length() - right.length();
        if (space < 1) space = 1;
        return left + " ".repeat(space) + right;
    }
}