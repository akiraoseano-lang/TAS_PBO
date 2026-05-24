package com.project.tas_pbo.model;

import javafx.beans.property.*;

public class Product {

    private final IntegerProperty ID;
    private final StringProperty namaProduct;
    private final StringProperty description;
    private final IntegerProperty stok;
    private final DoubleProperty price;

    public Product(Integer ID, String namaProduct, String description, Integer stok, Double price) {
        this.ID = new SimpleIntegerProperty(ID);
        this.namaProduct = new SimpleStringProperty(namaProduct);
        this.description = new SimpleStringProperty(description);
        this.stok = new SimpleIntegerProperty(stok);
        this.price = new SimpleDoubleProperty(price);
    }

    public IntegerProperty idProperty() {
        return ID;
    }

    public StringProperty nameProperty() {
        return namaProduct;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public IntegerProperty stokProperty() {
        return stok;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public int getId() {
        return ID.get();
    }

    public void setId(int id) {
        this.ID.set(id);
    }

    public String getNameProduct() {
        return namaProduct.get();
    }

    public void setNameProduct(String namaProduct) {
        this.namaProduct.set(namaProduct);
    }

    public String getDescription() {
        return description.get();
    }
    
    public Integer getStok() {
        return stok.get();
    }

    public void setStok(Integer stok) {
        this.stok.set(stok);
    }

    public Double getPrice() {
        return price.get();
    }

    public void setPrice(Double price) {
        this.price.set(price);
    }

}
