package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.model.Produk;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
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

    private ObservableList<Produk> produkData = FXCollections.observableArrayList();
    private List<Produk> allProdukData = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 10;

    @FXML private Button btnPrevProduk;
    @FXML private Button btnNextProduk;
    @FXML private Label lblPageProduk;

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
        if (allProdukData.isEmpty()) {
            loadProdukData();
        }
    }

    private void loadProdukData() {
        Task<List<Produk>> task = new Task<>() {
            @Override
            protected List<Produk> call() {
                return produkDAO.getAllProduk();
            }
        };
        task.setOnSucceeded(e -> {
            allProdukData = task.getValue();
            currentPage = 0;
            showPage(currentPage);
        });
    }

    private void showPage(int page) {
        int fromIndex = page * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allProdukData.size());
        int totalPage = (int) Math.ceil((double) allProdukData.size() / PAGE_SIZE);

        produkData.setAll(allProdukData.subList(fromIndex, toIndex));
        tableProduk.setItems(produkData);

        lblPageProduk.setText("Halaman " + (page + 1) + " dari " + totalPage);
        btnPrevProduk.setDisable(page == 0);
        btnNextProduk.setDisable(toIndex >= allProdukData.size());
    }

    @FXML
    private void nextPageProduk() {
        currentPage++;
        showPage(currentPage);
    }

    @FXML
    private void prevPageProduk() {
        currentPage--;
        showPage(currentPage);
    }

    public void afterInsertProduk(Produk newProduk) {
        allProdukData.add(newProduk);
        showPage(currentPage);
    }

    public void afterDeleteProduk(Produk produk) {
        allProdukData.remove(produk);
        if (currentPage > 0 && currentPage * PAGE_SIZE >= allProdukData.size()) {
            currentPage--;
        }
        showPage(currentPage);
    }

    public void afterUpdateProduk(Produk updated) {
        int index = allProdukData.indexOf(updated);
        if (index >= 0) {
            allProdukData.set(index, updated);
            showPage(currentPage);
        }
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