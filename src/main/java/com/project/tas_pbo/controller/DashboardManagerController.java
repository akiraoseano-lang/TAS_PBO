package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.model.Produk;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardManagerController {

    // ===== Views (StackPane children) =====
    @FXML private ScrollPane dashboardView;
    @FXML private VBox penjualanView;
    @FXML private VBox produkView;
    @FXML private VBox stokView;
    @FXML private VBox pelangganView;
    @FXML private VBox laporanView;
    @FXML private VBox pengaturanView;

    @FXML private Button btnDashboard;
    @FXML private Button btnPenjualan;
    @FXML private Button btnProduk;
    @FXML private Button btnStok;
    @FXML private Button btnPelanggan;
    @FXML private Button btnLaporan;
    @FXML private Button btnPengaturan;

    @FXML private TableView<?> tableStok;
    @FXML private TableView<?> tablePenjualan;
    @FXML private TableView<?> tableTerlaris;
    @FXML private TableView<?> tablePenjualanAll;
    @FXML private TableView<Produk> tableProduk;
    @FXML private TableView<?> tableStokAll;
    @FXML private TableView<?> tablePelanggan;

    @FXML private TableColumn<Produk, Integer> colNo;
    @FXML private TableColumn<Produk, String> colNamaProduk;
    @FXML private TableColumn<Produk, String> colKategori;
    @FXML private TableColumn<Produk, Double> colHarga;
    @FXML private TableColumn<Produk, Integer> colStok;
    @FXML private TableColumn<Produk, String> colSatuan;

    private Node[] allViews;
    private Button[] allButtons;

    private final ProdukDAO produkDAO = new ProdukDAO();

    @FXML
    public void initialize() {
        allViews = new Node[] {
                dashboardView, penjualanView, produkView, stokView,
                pelangganView, laporanView, pengaturanView
        };
        allButtons = new Button[] {
                btnDashboard, btnPenjualan, btnProduk, btnStok,
                btnPelanggan, btnLaporan, btnPengaturan
        };

        setupProdukTable();
    }

    private void setupProdukTable() {
        colNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tableProduk.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());

        colNamaProduk.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
    }

    private void loadProdukData() {
        List<Produk> produkList = produkDAO.getAllProduk();
        ObservableList<Produk> data = FXCollections.observableArrayList(produkList);
        tableProduk.setItems(data);
    }

    @FXML
    private void showDashboardView() {
        switchTo(dashboardView, btnDashboard);
    }

    @FXML
    private void showPenjualanView() {
        switchTo(penjualanView, btnPenjualan);
    }

    @FXML
    private void showProdukView() {
        switchTo(produkView, btnProduk);
        loadProdukData();
    }

    @FXML
    private void showStokView() {
        switchTo(stokView, btnStok);
    }

    @FXML
    private void showPelangganView() {
        switchTo(pelangganView, btnPelanggan);
    }

    @FXML
    private void showLaporanView() {
        switchTo(laporanView, btnLaporan);
    }

    @FXML
    private void showPengaturanView() {
        switchTo(pengaturanView, btnPengaturan);
    }

    private void switchTo(Node activeView, Button activeBtn) {
        for (Node v : allViews) {
            boolean isActive = (v == activeView);
            v.setVisible(isActive);
            v.setManaged(isActive);
        }
        for (Button b : allButtons) {
            b.getStyleClass().setAll(b == activeBtn ? "nav-btn-active" : "nav-btn");
        }
    }
}