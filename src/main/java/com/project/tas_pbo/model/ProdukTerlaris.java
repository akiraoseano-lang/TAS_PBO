package com.project.tas_pbo.model;

// Model untuk daftar produk terlaris
public class ProdukTerlaris extends BaseModel {
    private String namaProduk;
    private int terjual;
    private String satuan;

    public ProdukTerlaris(String namaProduk, int terjual, String satuan) {
        this.namaProduk = namaProduk;
        this.terjual = terjual;
        this.satuan = satuan;
    }

    public String getNamaProduk() { return namaProduk; }
    public int getTerjual() { return terjual; }
    public String getSatuan() { return satuan; }

    @Override
    public int getId() { return namaProduk != null ? namaProduk.hashCode() : 0; }

    @Override
    public String getDisplayInfo() { return "Produk Terlaris: " + namaProduk + " (" + terjual + " " + satuan + ")"; }
}
