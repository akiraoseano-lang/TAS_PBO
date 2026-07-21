package com.project.tas_pbo.model;

// Model untuk tabel produk
public class Produk {

    private int idProduk;
    private String barcode;
    private String namaProduk;
    private String kategori;
    private double harga;
    private int stok;
    private String satuan;
    private int stokMinimum;
    private int status;

    public Produk() {
    }

    public Produk(int idProduk, String barcode, String namaProduk, String kategori, double harga, int stok, String satuan, int stokMinimum) {
        this.idProduk = idProduk;
        this.barcode = barcode;
        this.namaProduk = namaProduk;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
        this.satuan = satuan;
        this.stokMinimum = stokMinimum;
    }

    public int getIdProduk() {
        return idProduk;
    }

    public void setIdProduk(int idProduk) {
        this.idProduk = idProduk;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public int getStokMinimum() {
        return stokMinimum;
    }

    public void setStokMinimum(int stokMinimum) {
        this.stokMinimum = stokMinimum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}