package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.DAO.PenjualanDAO;
import com.project.tas_pbo.DAO.MemberDAO;
import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.ProdukTerlaris;
import com.project.tas_pbo.model.Member;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import java.text.NumberFormat;
import java.util.Locale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import com.project.tas_pbo.util.Session;

public class DashboardManagerController {

    @FXML private ScrollPane dashboardView;
    @FXML private VBox penjualanView;
    @FXML private VBox produkView;
    @FXML private VBox pelangganView;
    @FXML private VBox laporanView;

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnPenjualan;
    @FXML private Button btnProduk;
    @FXML private Button btnMember;
    @FXML private Button btnLaporan;

    // Summary Cards Labels
    @FXML private Label lblTotalPenjualan;
    @FXML private Label lblTotalProduk;
    @FXML private Label lblTotalStok;
    @FXML private Label lblTotalPelanggan;

    @FXML private TableView<Produk> tableStok;
    @FXML private TableView<Penjualan> tablePenjualan;
    @FXML private TableView<ProdukTerlaris> tableTerlaris;
    @FXML private TableView<Penjualan> tablePenjualanAll;
    @FXML private TableView<Produk> tableProduk;
    @FXML private TableView<Member> tablePelanggan;

    // tableProduk columns
    @FXML private TableColumn<Produk, Integer> colNo;
    @FXML private TableColumn<Produk, String> colNamaProduk;
    @FXML private TableColumn<Produk, String> colKategori;
    @FXML private TableColumn<Produk, Double> colHarga;
    @FXML private TableColumn<Produk, Integer> colStok;
    @FXML private TableColumn<Produk, String> colSatuan;

    // tableProduk columns
    @FXML private TableColumn<Produk, Integer> colStokNo;
    @FXML private TableColumn<Produk, String> colStokNama;
    @FXML private TableColumn<Produk, Integer> colStokQty;
    @FXML private TableColumn<Produk, String> colStokSatuan;
    @FXML private TableColumn<Produk, String> colStokStatus;

    // tablePenjualan columns
    @FXML private TableColumn<Penjualan, Integer> colPenjualanNo;
    @FXML private TableColumn<Penjualan, String> colPenjualanNoTrx;
    @FXML private TableColumn<Penjualan, String> colPenjualanMember;
    @FXML private TableColumn<Penjualan, Double> colPenjualanTotal;
    @FXML private TableColumn<Penjualan, String> colPenjualanWaktu;

    // tableTerlaris columns
    @FXML private TableColumn<ProdukTerlaris, Integer> colTerlarisNo;
    @FXML private TableColumn<ProdukTerlaris, String> colTerlarisNama;
    @FXML private TableColumn<ProdukTerlaris, Integer> colTerlarisJumlah;
    @FXML private TableColumn<ProdukTerlaris, String> colTerlarisSatuan;

    // tablePenjualanAll columns
    @FXML private TableColumn<Penjualan, Integer> colPenjualanAllNo;
    @FXML private TableColumn<Penjualan, String> colPenjualanAllNoTrx;
    @FXML private TableColumn<Penjualan, String> colPenjualanAllMember;
    @FXML private TableColumn<Penjualan, Double> colPenjualanAllTotal;
    @FXML private TableColumn<Penjualan, String> colPenjualanAllWaktu;

    // tablePelanggan columns
    @FXML private TableColumn<Member, Integer> colMemberNo;
    @FXML private TableColumn<Member, String> colMemberKode;
    @FXML private TableColumn<Member, String> colMemberNama;
    @FXML private TableColumn<Member, String> colMemberTelepon;
    @FXML private TableColumn<Member, String> colMemberAlamat;

//    @FXML private TableColumn

    private Node[] allViews;
    private Button[] allButtons;

    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    private ObservableList<Produk> produkData = FXCollections.observableArrayList();
    private List<Produk> allProdukData = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 10;

    @FXML private Button btnPrevProduk;
    @FXML private Button btnNextProduk;
    @FXML private Label lblPageProduk;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");



    @FXML
    public void initialize() {
        allViews = new Node[] {
                dashboardView, penjualanView, produkView,
                pelangganView, laporanView
        };
        allButtons = new Button[] {
                btnDashboard, btnPenjualan, btnProduk,
                btnMember, btnLaporan
        };

        setupProdukTable();
        loadProdukData();
        setupDashboardTables();
        setupPernjualanTable();
        setupMemberTable();
        startClock();
        loadDashboardStats();
    }

    @FXML
    public void startClock() {
        updateTime();
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTime())
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    public void updateTime() {
        LocalDateTime now = LocalDateTime.now();

        if (timeLabel != null) {
            timeLabel.setText(now.format(TIME_FORMAT));
        }
        if (dateLabel != null) {
            dateLabel.setText(now.format(DATE_FORMAT));
        }
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

    // 4. Setup tablePenjualanAll (Penjualan tab)
    public void setupPernjualanTable() {
        colPenjualanAllNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tablePenjualanAll.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colPenjualanAllNoTrx.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        colPenjualanAllMember.setCellValueFactory(cellData -> {
            Integer memberId = cellData.getValue().getIdMember();
            return new SimpleStringProperty(memberId == null ? "Umum" : "Member " + memberId);
        });
        colPenjualanAllTotal.setCellValueFactory(new PropertyValueFactory<>("totalBelanja"));
        colPenjualanAllWaktu.setCellValueFactory(cellData -> {
            java.sql.Timestamp waktu = cellData.getValue().getWaktuTransaksi();
            if (waktu != null) {
                return new SimpleStringProperty(waktu.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new SimpleStringProperty("-");
        });
    }

    public void setupMemberTable() {
        // 5. Setup tablePelanggan (Member tab)
        colMemberNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tablePelanggan.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colMemberKode.setCellValueFactory(new PropertyValueFactory<>("kodeMember"));
        colMemberNama.setCellValueFactory(new PropertyValueFactory<>("namaMember"));
        colMemberTelepon.setCellValueFactory(new PropertyValueFactory<>("noTelepon"));
        colMemberAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
    }

    private void setupDashboardTables() {
        // 1. Setup tableStok (Products Low/Menipis)
        colStokNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tableStok.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colStokNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colStokQty.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colStokSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colStokStatus.setCellValueFactory(cellData -> {
            Produk p = cellData.getValue();
            if (p.getStok() <= p.getStokMinimum()) {
                return new SimpleStringProperty("Menipis");
            } else {
                return new SimpleStringProperty("Aman");
            }
        });

        // 2. Setup tablePenjualan (Latest Penjualan on Dashboard)
        colPenjualanNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tablePenjualan.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colPenjualanNoTrx.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        colPenjualanMember.setCellValueFactory(cellData -> {
            Integer memberId = cellData.getValue().getIdMember();
            return new SimpleStringProperty(memberId == null ? "Umum" : "Member " + memberId);
        });
        colPenjualanTotal.setCellValueFactory(new PropertyValueFactory<>("totalBelanja"));
        colPenjualanWaktu.setCellValueFactory(cellData -> {
            java.sql.Timestamp waktu = cellData.getValue().getWaktuTransaksi();
            if (waktu != null) {
                return new SimpleStringProperty(waktu.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new SimpleStringProperty("-");
        });

        // 3. Setup tableTerlaris (Top Selling Products)
        colTerlarisNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tableTerlaris.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colTerlarisNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colTerlarisJumlah.setCellValueFactory(new PropertyValueFactory<>("terjual"));
        colTerlarisSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
    }

    private void loadDashboardStats() {
        Task<Void> task = new Task<>() {
            private double totalPenjualanVal;
            private int totalProdukVal;
            private int totalStokVal;
            private int totalPelangganVal;
            private List<Produk> produkMenipisList;
            private List<Penjualan> latestPenjualanList;
            private List<ProdukTerlaris> produkTerlarisList;
            private List<Penjualan> allPenjualanList;
            private List<Member> memberList;

            @Override
            protected Void call() {
                totalPenjualanVal = penjualanDAO.getTotalPenjualan();
                totalProdukVal = produkDAO.getTotalProduk();
                totalStokVal = produkDAO.getTotalStok();
                totalPelangganVal = penjualanDAO.getTotalPelanggan();

                produkMenipisList = produkDAO.getProdukMenipis();
                latestPenjualanList = penjualanDAO.getLatestPenjualan(5);
                produkTerlarisList = produkDAO.getProdukTerlaris(5);
                allPenjualanList = penjualanDAO.getAllPenjualan();
                memberList = memberDAO.getAllMember();
                return null;
            }

            @Override
            protected void succeeded() {
                lblTotalPenjualan.setText(rupiahFormat.format(totalPenjualanVal));
                lblTotalProduk.setText(String.valueOf(totalProdukVal));
                lblTotalStok.setText(String.valueOf(totalStokVal));
                lblTotalPelanggan.setText(String.valueOf(totalPelangganVal));

                tableStok.setItems(FXCollections.observableArrayList(produkMenipisList));
                tablePenjualan.setItems(FXCollections.observableArrayList(latestPenjualanList));
                tableTerlaris.setItems(FXCollections.observableArrayList(produkTerlarisList));
                tablePenjualanAll.setItems(FXCollections.observableArrayList(allPenjualanList));
                tablePelanggan.setItems(FXCollections.observableArrayList(memberList));

                tableStok.refresh();
                tablePenjualan.refresh();
                tableTerlaris.refresh();
                tablePenjualanAll.refresh();
                tablePelanggan.refresh();
            }

            @Override
            protected void failed() {
                if (getException() != null) {
                    getException().printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void showDashboardView() {
        switchTo(dashboardView, btnDashboard);
        loadDashboardStats();
    }

    @FXML
    private void showPenjualanView() {
        switchTo(penjualanView, btnPenjualan);
        loadDashboardStats();
    }

    @FXML
    private void showProdukView() {
        switchTo(produkView, btnProduk);
        if (allProdukData.isEmpty()) {
            loadProdukData();
        }
    }

    private void loadProdukData() {
        btnNextProduk.setDisable(true);
        btnPrevProduk.setDisable(true);
        lblPageProduk.setText("Memuat Data...");

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

        task.setOnFailed(e -> {
            lblPageProduk.setText("Gagal memuat data");
        });

        new Thread(task).start();
    }

    private void showPage(int page) {
        if (allProdukData.isEmpty()) return;

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

    @FXML
    private void showPelangganView() {
        switchTo(pelangganView, btnMember);
        loadDashboardStats();
    }

    @FXML
    private void showLaporanView() {
        switchTo(laporanView, btnLaporan);
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

    @FXML
    private void handleLogout(ActionEvent event) {
        Session.clear();
        try {
            SceneController.switchTo("/com/project/tas_pbo/view/login-view.fxml", event);
        } catch (IOException e) {
        e.printStackTrace();
        }
    }
}