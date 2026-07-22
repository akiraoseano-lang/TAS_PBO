package com.project.tas_pbo.model;

import java.sql.Timestamp;

// Model untuk tabel penjualan (header transaksi)
public class Penjualan extends BaseModel {

    private int idPenjualan;
    private String noTransaksi;
    private int idUser;
    private double totalBelanja;
    private double bayar;
    private double kembalian;
    private Timestamp waktuTransaksi;

    public Penjualan() {
    }

    public int getIdPenjualan() {
        return idPenjualan;
    }

    public void setIdPenjualan(int idPenjualan) {
        this.idPenjualan = idPenjualan;
    }

    public String getNoTransaksi() {
        return noTransaksi;
    }

    public void setNoTransaksi(String noTransaksi) {
        this.noTransaksi = noTransaksi;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public double getTotalBelanja() {
        return totalBelanja;
    }

    public void setTotalBelanja(double totalBelanja) {
        this.totalBelanja = totalBelanja;
    }

    public double getBayar() {
        return bayar;
    }

    public void setBayar(double bayar) {
        this.bayar = bayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }

    public Timestamp getWaktuTransaksi() {
        return waktuTransaksi;
    }

    public void setWaktuTransaksi(Timestamp waktuTransaksi) {
        this.waktuTransaksi = waktuTransaksi;
    }

    @Override
    public int getId() { return idPenjualan; }

    @Override
    public String getDisplayInfo() { return "Penjualan: " + noTransaksi + " - Rp " + totalBelanja; }
}