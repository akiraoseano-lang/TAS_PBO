package com.project.tas_pbo.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Model untuk laporan penjualan harian
public class LaporanHarian {

    private LocalDate tanggal;
    private int jumlahTransaksi;
    private double totalPenjualan;
    private double rataRata;

    public LaporanHarian() {}

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public int getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(int jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }

    public double getTotalPenjualan() { return totalPenjualan; }
    public void setTotalPenjualan(double totalPenjualan) { this.totalPenjualan = totalPenjualan; }

    public double getRataRata() { return rataRata; }
    public void setRataRata(double rataRata) { this.rataRata = rataRata; }

    public String getTanggalFormatted() {
        if (tanggal == null) return "";
        return tanggal.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    public String getTanggalFull() {
        if (tanggal == null) return "";
        return tanggal.format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                new java.util.Locale("id", "ID")));
    }
}