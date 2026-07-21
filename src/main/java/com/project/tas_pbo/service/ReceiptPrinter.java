package com.project.tas_pbo.service;

import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;
import javafx.print.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Service untuk mencetak struk pembayaran
public class ReceiptPrinter {

    private static final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    // Lebar karakter menjadi 30 agar tidak keluar batas kertas 58mm
    private static final int CHAR_WIDTH = 30;

    // =========================================================
    // GENERATE TEKS STRUK
    // =========================================================
    // Menghasilkan teks struk pembayaran
    public static String generateReceipt(
            Penjualan penjualan,
            List<PenjualanDetail> items
    ) {
        StringBuilder sb = new StringBuilder();

        // Header toko
        sb.append(center("TOKO KELONTONG", CHAR_WIDTH)).append("\n");
        sb.append(center("Jl. Contoh No. 1", CHAR_WIDTH)).append("\n");
        sb.append(center("Telp: 021-12345678", CHAR_WIDTH)).append("\n");
        sb.append(line(CHAR_WIDTH)).append("\n");

        // Informasi transaksi
        sb.append("No  : ").append(penjualan.getNoTransaksi()).append("\n");
        sb.append("Tgl : ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"))
        ).append("\n");

        sb.append(line(CHAR_WIDTH)).append("\n");

        // Daftar item
        for (PenjualanDetail item : items) {
            String nama = item.getNamaProduk();
            if (nama.length() > CHAR_WIDTH) {
                nama = nama.substring(0, CHAR_WIDTH - 2) + "..";
            }
            sb.append(nama).append("\n");

            String left = "  " + item.getJumlah() + " x " +
                    rupiahFormat.format((long) item.getHargaSatuan());
            String right = "Rp " + rupiahFormat.format((long) item.getSubtotal());
            sb.append(rowFit(left, right, CHAR_WIDTH)).append("\n");
        }

        sb.append(line(CHAR_WIDTH)).append("\n");

        sb.append(rowFit("TOTAL",
                "Rp " + rupiahFormat.format((long) penjualan.getTotalBelanja()), CHAR_WIDTH)).append("\n");
        sb.append(rowFit("Bayar",
                "Rp " + rupiahFormat.format((long) penjualan.getBayar()), CHAR_WIDTH)).append("\n");
        sb.append(rowFit("Kembalian",
                "Rp " + rupiahFormat.format((long) penjualan.getKembalian()), CHAR_WIDTH)).append("\n");
        sb.append(line(CHAR_WIDTH)).append("\n");

        // Footer
        sb.append(center("Terima kasih!", CHAR_WIDTH)).append("\n");
        sb.append(center("Simpan struk ini", CHAR_WIDTH)).append("\n");
        sb.append(line(CHAR_WIDTH)).append("\n");

        return sb.toString();
    }

    // =========================================================
    // CETAK KE PRINTER THERMAL 58MM
    // =========================================================
    // Mencetak struk ke printer thermal 58mm
    public static void printToPrinter(
            Penjualan penjualan,
            List<PenjualanDetail> items
    ) {
        String receiptText = generateReceipt(penjualan, items);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showReceiptDialog(penjualan, items);
            return;
        }

        // Tampilkan dialog pemilih printer
        boolean proceed = job.showPrintDialog(null);
        if (!proceed) {
            showReceiptDialog(penjualan, items);
            return;
        }

        Printer printer = job.getPrinter();

        // Atur ukuran kertas 58mm
        Paper paper58mm = Paper.NA_LETTER; // fallback

        // Ubah margin (kiri, kanan, atas, bawah) menjadi 0
        PageLayout pageLayout = printer.createPageLayout(
                paper58mm,
                PageOrientation.PORTRAIT,
                0, 0, 0, 0
        );

        // Buat struk sebagai node Text
        Text receiptNode = new Text(receiptText);
        // Turunkan ukuran font menjadi 7.5 agar muat sempurna
        receiptNode.setFont(Font.font("Courier New", 7.5));
        receiptNode.setTextAlignment(TextAlignment.LEFT);

        // Bungkus dalam VBox untuk tata letak yang rapi
        VBox printBox = new VBox(receiptNode);
        // Hapus padding menjadi 0
        printBox.setPadding(new Insets(0));
        printBox.setPrefWidth(155);

        boolean printed = job.printPage(pageLayout, printBox);
        if (printed) {
            job.endJob();
        } else {
            showReceiptDialog(penjualan, items);
        }
    }

    // =========================================================
    // TAMPILKAN STRUK DALAM DIALOG (fallback / struk digital)
    // =========================================================
    // Menampilkan struk dalam dialog (fallback jika printer tidak tersedia)
    public static void showReceiptDialog(
            Penjualan penjualan,
            List<PenjualanDetail> items
    ) {
        String receiptText = generateReceipt(penjualan, items);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Struk Pembayaran");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setFont(Font.font("Courier New", 12));
        textArea.setPrefSize(320, 500);
        textArea.setWrapText(false);

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(370, 560);
        alert.showAndWait();
    }

    // =========================================================
    // FUNGSI BANTU
    // =========================================================
    // Membuat teks rata tengah
    private static String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    // Membuat garis pemisah
    private static String line(int width) {
        return "-".repeat(width);
    }

    // Mengatur teks kiri dan kanan dalam satu baris
    private static String rowFit(String left, String right, int width) {
        int space = width - left.length() - right.length();
        if (space >= 1) {
            return left + " ".repeat(space) + right;
        } else {
            return left + "\n" + " ".repeat(Math.max(0, width - right.length())) + right;
        }
    }
}