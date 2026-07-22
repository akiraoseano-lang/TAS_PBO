package com.project.tas_pbo.model;

// Model untuk tabel penjualan_detail (item transaksi)
public class PenjualanDetail extends BaseModel {

    private int idDetail;
    private int idPenjualan;
    private int idProduk;
    private String barcode;
    private String namaProduk;
    private double hargaSatuan;
    private int jumlah;
    private double subtotal;

    public PenjualanDetail() {
    }

    public PenjualanDetail(int idProduk, String barcode, String namaProduk, double hargaSatuan, int jumlah) {
        this.idProduk = idProduk;
        this.barcode = barcode;
        this.namaProduk = namaProduk;
        this.hargaSatuan = hargaSatuan;
        this.jumlah = jumlah;
        this.subtotal = hargaSatuan * jumlah;
    }

    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }

    public int getIdPenjualan() { return idPenjualan; }
    public void setIdPenjualan(int idPenjualan) { this.idPenjualan = idPenjualan; }

    public int getIdProduk() { return idProduk; }
    public void setIdProduk(int idProduk) { this.idProduk = idProduk; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(double hargaSatuan) { this.hargaSatuan = hargaSatuan; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
        this.subtotal = this.hargaSatuan * jumlah;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public int getId() { return idDetail; }

    @Override
    public String getDisplayInfo() { return namaProduk + " x" + jumlah + " = Rp " + subtotal; }
}