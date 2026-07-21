package com.project.tas_pbo.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.project.tas_pbo.model.LaporanHarian;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Service untuk membuat laporan PDF
public class ReportGenerator {

    private static final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    // =========================================================
    // SIMPAN SEBAGAI PDF
    // =========================================================
    // Menyimpan laporan ke file PDF
    public static void saveAsPdf(
            List<LaporanHarian> dailyData,
            LaporanHarian summary,
            List<String[]> topProduk,
            int periodDays,
            Stage ownerStage
    ) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan PDF");
        fileChooser.setInitialFileName("Laporan_Keuangan_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF File", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            buildPdfContent(document, writer, dailyData, summary, topProduk, periodDays);

            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                    "Laporan berhasil disimpan ke:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Menggambar grafik batang di PDF
    private static void drawBarChart(PdfTemplate template, List<LaporanHarian> data, float width, float height) {
        int n = data.size();
        if (n < 2) return;

        float chartLeft = 35f;
        float chartBottom = 20f;
        float chartWidth = width - chartLeft - 10f;
        float chartHeight = height - chartBottom - 10f;

        double maxPenjualan = data.stream().mapToDouble(LaporanHarian::getTotalPenjualan).max().orElse(1);

        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Sumbu grafik
            template.setColorStroke(new Color(80, 80, 80));
            template.setLineWidth(0.5f);
            template.moveTo(chartLeft, chartBottom);
            template.lineTo(chartLeft, chartBottom + chartHeight);
            template.lineTo(chartLeft + chartWidth, chartBottom + chartHeight);
            template.stroke();

            template.moveTo(chartLeft, chartBottom);
            template.lineTo(chartLeft + chartWidth, chartBottom);
            template.stroke();

            // Label sumbu Y
            template.setColorFill(new Color(60, 60, 60));
            template.beginText();
            template.setFontAndSize(bf, 7);
            template.showTextAligned(Element.ALIGN_RIGHT, "0", chartLeft - 3, chartBottom - 3, 0);
            template.showTextAligned(Element.ALIGN_RIGHT, "Rp " + rupiahFormat.format((long) maxPenjualan), chartLeft - 3, chartBottom + chartHeight - 4, 0);
            template.endText();

            // Garis grid
            template.setLineWidth(0.3f);
            template.setColorStroke(new Color(200, 200, 200));
            float yMid = chartBottom + chartHeight / 2;
            template.moveTo(chartLeft, yMid);
            template.lineTo(chartLeft + chartWidth, yMid);
            template.stroke();

            // Batang grafik
            float barArea = chartWidth / n;
            float barWidth = barArea * 0.65f;
            float barGap = barArea * 0.35f;

            for (int i = 0; i < n; i++) {
                LaporanHarian h = data.get(i);
                float barH = (float) (h.getTotalPenjualan() / maxPenjualan * chartHeight * 0.92f);
                float barX = chartLeft + i * barArea + barGap / 2;

                // Efek gradasi dengan dua batang yang tumpang tindih
                template.setColorFill(new Color(54, 162, 235));
                template.roundRectangle(barX, chartBottom, barWidth, barH, 2);
                template.fill();

                // Sorotan lebih terang di atas
                if (barH > 4) {
                    template.setColorFill(new Color(100, 190, 245));
                    template.roundRectangle(barX, chartBottom + barH - 4, barWidth, 4, 1);
                    template.fill();
                }

                // Label tanggal
                template.setColorFill(new Color(60, 60, 60));
                template.beginText();
                template.setFontAndSize(bf, 7);
                template.showTextAligned(Element.ALIGN_CENTER, h.getTanggalFormatted(), barX + barWidth / 2, chartBottom - 9, 0);
                template.endText();

                // Label nilai di atas batang
                template.setColorFill(new Color(54, 162, 235));
                template.beginText();
                template.setFontAndSize(bf, 7);
                template.showTextAligned(Element.ALIGN_CENTER, rupiahFormat.format((long) h.getTotalPenjualan()), barX + barWidth / 2, chartBottom + barH + 2, 0);
                template.endText();
            }

            // Legenda
            float legendY = chartBottom + chartHeight + 4;
            template.setColorFill(new Color(54, 162, 235));
            template.roundRectangle(chartLeft, legendY + 1, 10, 7, 1);
            template.fill();

            template.setColorFill(new Color(60, 60, 60));
            template.beginText();
            template.setFontAndSize(bf, 7);
            template.showTextAligned(Element.ALIGN_LEFT, "Total Penjualan (Rp)", chartLeft + 14, legendY, 0);
            template.endText();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Menambahkan sel ke tabel PDF
    private static void addTableCell(PdfPTable table, String text, com.lowagie.text.Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        table.addCell(cell);
    }

    // =========================================================
    // TAMPILKAN PRATINJAU (buat PDF sementara & buka dengan viewer)
    // =========================================================
    // Menampilkan pratinjau laporan PDF
    public static void showPreview(
            List<LaporanHarian> dailyData,
            LaporanHarian summary,
            List<String[]> topProduk,
            int periodDays
    ) {
        try {
            File tempFile = Files.createTempFile("preview_laporan_", ".pdf").toFile();
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tempFile));
            document.open();

            buildPdfContent(document, writer, dailyData, summary, topProduk, periodDays);

            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal membuka preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void buildPdfContent(
            Document document,
            PdfWriter writer,
            List<LaporanHarian> dailyData,
            LaporanHarian summary,
            List<String[]> topProduk,
            int periodDays
    ) throws Exception {
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font subtitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font sectionFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

        Paragraph title = new Paragraph("LAPORAN KEUANGAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("TOKO KELONTONG", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        document.add(new Paragraph(" "));

        String period = periodDays == 7 ? "7 Hari Terakhir" : "30 Hari Terakhir";
        document.add(new Paragraph("Periode  : " + period, normalFont));
        document.add(new Paragraph("Dicetak  : " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));

        document.add(new Paragraph(" "));

        Paragraph summaryTitle = new Paragraph("RINGKASAN", sectionFont);
        document.add(summaryTitle);

        document.add(new Paragraph("Total Transaksi  : " + summary.getJumlahTransaksi(), normalFont));
        document.add(new Paragraph("Total Penjualan  : Rp " + rupiahFormat.format((long) summary.getTotalPenjualan()), normalFont));
        document.add(new Paragraph("Rata-rata/Hari   : Rp " + rupiahFormat.format((long) summary.getRataRata()), normalFont));

        document.add(new Paragraph(" "));

        // Chart
        if (dailyData.size() >= 2) {
            Paragraph chartTitle = new Paragraph("GRAFIK PENJUALAN", sectionFont);
            document.add(chartTitle);
            document.add(new Paragraph(" "));

            float chartWidth = PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin();
            float chartHeight = 130f;
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate template = cb.createTemplate(chartWidth, chartHeight);
            drawBarChart(template, dailyData, chartWidth, chartHeight);

            Image chartImage = Image.getInstance(template);
            chartImage.setAlignment(Element.ALIGN_CENTER);
            document.add(chartImage);

            document.add(new Paragraph(" "));
        }

        Paragraph detailTitle = new Paragraph("DETAIL PENJUALAN HARIAN", sectionFont);
        document.add(detailTitle);
        document.add(new Paragraph(" "));

        PdfPTable dailyTable = new PdfPTable(3);
        dailyTable.setWidthPercentage(100);
        dailyTable.setWidths(new float[]{40f, 25f, 35f});

        addTableCell(dailyTable, "Tanggal", headerFont, Element.ALIGN_CENTER);
        addTableCell(dailyTable, "Transaksi", headerFont, Element.ALIGN_CENTER);
        addTableCell(dailyTable, "Total (Rp)", headerFont, Element.ALIGN_RIGHT);

        if (dailyData.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Paragraph("Tidak ada data penjualan pada periode ini.", normalFont));
            emptyCell.setColspan(3);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(5);
            dailyTable.addCell(emptyCell);
        } else {
            for (LaporanHarian h : dailyData) {
                addTableCell(dailyTable, h.getTanggalFormatted(), normalFont, Element.ALIGN_CENTER);
                addTableCell(dailyTable, String.valueOf(h.getJumlahTransaksi()), normalFont, Element.ALIGN_CENTER);
                addTableCell(dailyTable, "Rp " + rupiahFormat.format((long) h.getTotalPenjualan()), normalFont, Element.ALIGN_RIGHT);
            }
        }
        document.add(dailyTable);

        if (!topProduk.isEmpty()) {
            document.add(new Paragraph(" "));
            Paragraph topTitle = new Paragraph("PRODUK TERLARIS", sectionFont);
            document.add(topTitle);
            document.add(new Paragraph(" "));

            PdfPTable topTable = new PdfPTable(4);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{8f, 40f, 20f, 32f});

            addTableCell(topTable, "No", headerFont, Element.ALIGN_CENTER);
            addTableCell(topTable, "Produk", headerFont, Element.ALIGN_LEFT);
            addTableCell(topTable, "Terjual", headerFont, Element.ALIGN_CENTER);
            addTableCell(topTable, "Pendapatan (Rp)", headerFont, Element.ALIGN_RIGHT);

            int no = 1;
            for (String[] p : topProduk) {
                addTableCell(topTable, String.valueOf(no++), normalFont, Element.ALIGN_CENTER);
                addTableCell(topTable, p[0].length() > 30 ? p[0].substring(0, 30) : p[0], normalFont, Element.ALIGN_LEFT);
                addTableCell(topTable, p[1], normalFont, Element.ALIGN_CENTER);
                addTableCell(topTable, "Rp " + rupiahFormat.format((long) Double.parseDouble(p[2])), normalFont, Element.ALIGN_RIGHT);
            }
            document.add(topTable);
        }

        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("--- Akhir Laporan ---", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // =========================================================
    // FUNGSI BANTU
    // =========================================================
    // Menampilkan alert dialog
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}