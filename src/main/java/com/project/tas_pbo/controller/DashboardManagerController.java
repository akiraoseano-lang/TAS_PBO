package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.DAO.LaporanDAO;
import com.project.tas_pbo.DAO.PenjualanDAO;
import com.project.tas_pbo.DAO.UserDAO;
import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.model.LaporanHarian;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.ProdukTerlaris;
import com.project.tas_pbo.model.User;
import com.project.tas_pbo.service.ReportGenerator;
import com.project.tas_pbo.util.Session;
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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.text.NumberFormat;
import java.util.Locale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardManagerController {

    @FXML private ScrollPane dashboardView;
    @FXML private VBox penjualanView;
    @FXML private VBox produkView;
    @FXML private ScrollPane laporanView;
    @FXML private VBox userView;

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblGreeting;

    @FXML private Button btnDashboard;
    @FXML private Button btnPenjualan;
    @FXML private Button btnProduk;
    @FXML private Button btnLaporan;
    @FXML private Button btnUser;

    // Summary Cards Labels untuk Dashboard
    @FXML private Label lblTotalPenjualan;
    @FXML private Label lblTotalProduk;
    @FXML private Label lblTotalStok;

    @FXML private TableView<Produk> tableStok;
    @FXML private TableView<Penjualan> tablePenjualan;
    @FXML private TableView<ProdukTerlaris> tableTerlaris;
    @FXML private TableView<Penjualan> tablePenjualanAll;
    @FXML private TableView<Produk> tableProduk;

    // ===== Dashboard chart =====
    @FXML private AreaChart<String, Number> salesChart;
    @FXML private Button btnChart7Hari;
    @FXML private Button btnChart1Bulan;

    // tableProduk columns
    @FXML private TableColumn<Produk, Integer> colNo;
    @FXML private TableColumn<Produk, String> colNamaProduk;
    @FXML private TableColumn<Produk, String> colKategori;
    @FXML private TableColumn<Produk, Double> colHarga;
    @FXML private TableColumn<Produk, Integer> colStok;
    @FXML private TableColumn<Produk, String> colSatuan;

    // tableStok columns (Dashboard)
    @FXML private TableColumn<Produk, Integer> colStokNo;
    @FXML private TableColumn<Produk, String> colStokNama;
    @FXML private TableColumn<Produk, Integer> colStokQty;
    @FXML private TableColumn<Produk, String> colStokSatuan;
    @FXML private TableColumn<Produk, String> colStokStatus;

    // tablePenjualan columns (Dashboard)
    @FXML private TableColumn<Penjualan, Integer> colPenjualanNo;
    @FXML private TableColumn<Penjualan, String> colPenjualanNoTrx;
    @FXML private TableColumn<Penjualan, Double> colPenjualanTotal;
    @FXML private TableColumn<Penjualan, String> colPenjualanWaktu;

    // tableTerlaris columns
    @FXML private TableColumn<ProdukTerlaris, Integer> colTerlarisNo;
    @FXML private TableColumn<ProdukTerlaris, String> colTerlarisNama;
    @FXML private TableColumn<ProdukTerlaris, Integer> colTerlarisJumlah;
    @FXML private TableColumn<ProdukTerlaris, String> colTerlarisSatuan;

    // tablePenjualanAll columns (Penjualan Tab) [cite: 121, 122, 123, 124]
    @FXML private TableColumn<Penjualan, Integer> colPenjualanAllNo;
    @FXML private TableColumn<Penjualan, String> colPenjualanAllNoTrx;
    @FXML private TableColumn<Penjualan, Double> colPenjualanAllTotal;
    @FXML private TableColumn<Penjualan, String> colPenjualanAllWaktu;

    // user view
    @FXML private Button btnFilterActiveUser;
    @FXML private Button btnFilterDeletedUser;
    @FXML private VBox activeUserSection;
    @FXML private VBox deletedUserSection;
    @FXML private TableView<User> tableUser;
    @FXML private TableColumn<User, Integer> colUserNo;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserNama;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TextField searchUserField;

    // deleted user table
    @FXML private TableView<User> tableDeletedUser;
    @FXML private TableColumn<User, Integer> colDltUserNo;
    @FXML private TableColumn<User, String> colDltUserUsername;
    @FXML private TableColumn<User, String> colDltUserNama;
    @FXML private TableColumn<User, String> colDltUserRole;

    // laporan view
    @FXML private Button btnLaporan7Hari;
    @FXML private Button btnLaporan1Bulan;
    @FXML private AreaChart<String, Number> laporanChart;
    @FXML private Label lblTotalTransaksi;
    @FXML private Label lblTotalPenjualanLaporan; // Dibuat unik agar tidak bentrok dengan Dashboard
    @FXML private Label lblRataRata;
    @FXML private TableView<LaporanHarian> tableLaporan;
    @FXML private TableColumn<LaporanHarian, String> colLaporanTanggal;
    @FXML private TableColumn<LaporanHarian, Integer> colLaporanTransaksi;
    @FXML private TableColumn<LaporanHarian, String> colLaporanTotal;

    private Node[] allViews;
    private Button[] allButtons;

    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    private ObservableList<Produk> produkData = FXCollections.observableArrayList();
    private List<Produk> allProdukData = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 10;
    private int currentLaporanDays = 7;

    @FXML private Button btnPrevProduk;
    @FXML private Button btnNextProduk;
    @FXML private Label lblPageProduk;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    @FXML
    public void initialize() {
        allViews = new Node[] {
                dashboardView, penjualanView, produkView,
                laporanView, userView
        };
        allButtons = new Button[] {
                btnDashboard, btnPenjualan, btnProduk,
                btnLaporan, btnUser
        };

        setupProdukTable();
        loadProdukData();
        setupDashboardTables();
        setupPenjualanTable(); 
        setupUserTable();
        setupDeletedUserTable();
        setupLaporanTable();
        startClock();
        loadDashboardStats();
        setUserInfo();
        loadChart(7);
        if (btnChart7Hari != null) btnChart7Hari.setStyle("-fx-font-weight: bold;");
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

    private void setUserInfo() {
        var user = Session.getCurrentUser();
        if (user != null) {
            lblUserName.setText(user.getNamaLengkap());
            lblUserRole.setText(user.getRole());
            lblGreeting.setText("Selamat datang, " + user.getNamaLengkap() + "!");
        }
    }

    private void loadChart(int days) {
        Task<List<LaporanHarian>> task = new Task<>() {
            @Override
            protected List<LaporanHarian> call() {
                return LaporanDAO.getDailySales(days);
            }
        };

        task.setOnSucceeded(e -> {
            List<LaporanHarian> data = task.getValue();
            updateChart(salesChart, data);
        });

        new Thread(task).start();
    }

    private void loadLaporanChart(int days) {
        Task<List<LaporanHarian>> task = new Task<>() {
            @Override
            protected List<LaporanHarian> call() {
                return LaporanDAO.getDailySales(days);
            }
        };

        task.setOnSucceeded(e -> {
            List<LaporanHarian> data = task.getValue();
            if (laporanChart != null) updateChart(laporanChart, data);
        });

        new Thread(task).start();
    }

    private void updateChart(AreaChart<String, Number> chart, List<LaporanHarian> data) {
        chart.getData().clear();

        XYChart.Series<String, Number> seriesPenjualan = new XYChart.Series<>();
        seriesPenjualan.setName("Total Penjualan (Rp)");

        XYChart.Series<String, Number> seriesTransaksi = new XYChart.Series<>();
        seriesTransaksi.setName("Jumlah Transaksi");

        for (LaporanHarian h : data) {
            String label = h.getTanggalFormatted();
            seriesPenjualan.getData().add(
                    new XYChart.Data<>(label, h.getTotalPenjualan())
            );
            seriesTransaksi.getData().add(
                    new XYChart.Data<>(label, h.getJumlahTransaksi())
            );
        }

        chart.getData().addAll(seriesPenjualan, seriesTransaksi);
        chart.setTitle(data.isEmpty() ? "Tidak ada data" : "");
    }

    @FXML
    private void handleChart7Hari() {
        loadChart(7);
        if (btnChart7Hari != null) btnChart7Hari.setStyle("-fx-font-weight: bold;");
        if (btnChart1Bulan != null) btnChart1Bulan.setStyle("");
    }

    @FXML
    private void handleChart1Bulan() {
        loadChart(30);
        if (btnChart1Bulan != null) btnChart1Bulan.setStyle("-fx-font-weight: bold;");
        if (btnChart7Hari != null) btnChart7Hari.setStyle("");
    }

    private void setupLaporanTable() {
        if (tableLaporan == null) return;

        colLaporanTanggal.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTanggalFull()));
        colLaporanTransaksi.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getJumlahTransaksi()).asObject());
        colLaporanTotal.setCellValueFactory(cd ->
                new SimpleStringProperty("Rp " + rupiahFormat.format(
                        (long) cd.getValue().getTotalPenjualan())));
    }

    private void loadLaporanData(int days) {
        currentLaporanDays = days;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<LaporanHarian> daily = LaporanDAO.getDailySales(days);
                LaporanHarian summary = LaporanDAO.getSummary(days);

                javafx.application.Platform.runLater(() -> {
                    if (lblTotalTransaksi != null)
                        lblTotalTransaksi.setText(String.valueOf(summary.getJumlahTransaksi()));
                    if (lblTotalPenjualanLaporan != null)
                        lblTotalPenjualanLaporan.setText("Rp " + rupiahFormat.format(
                                (long) summary.getTotalPenjualan()));
                    if (lblRataRata != null)
                        lblRataRata.setText("Rp " + rupiahFormat.format(
                                (long) summary.getRataRata()));

                    if (tableLaporan != null)
                        tableLaporan.setItems(FXCollections.observableArrayList(daily));

                    loadLaporanChart(days);
                });

                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleLaporan7Hari() {
        loadLaporanData(7);
        if (btnLaporan7Hari != null) btnLaporan7Hari.setStyle("-fx-font-weight: bold;");
        if (btnLaporan1Bulan != null) btnLaporan1Bulan.setStyle("");
    }

    @FXML
    private void handleLaporan1Bulan() {
        loadLaporanData(30);
        if (btnLaporan1Bulan != null) btnLaporan1Bulan.setStyle("-fx-font-weight: bold;");
        if (btnLaporan7Hari != null) btnLaporan7Hari.setStyle("");
    }

    @FXML
    private void handleSimpanPdf() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<LaporanHarian> daily = LaporanDAO.getDailySales(currentLaporanDays);
                LaporanHarian summary = LaporanDAO.getSummary(currentLaporanDays);
                List<String[]> topProduk = LaporanDAO.getTopProduk(currentLaporanDays, 10);
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) laporanView.getScene().getWindow();
                    ReportGenerator.saveAsPdf(daily, summary, topProduk, currentLaporanDays, stage);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handlePreviewLaporan() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<LaporanHarian> daily = LaporanDAO.getDailySales(currentLaporanDays);
                LaporanHarian summary = LaporanDAO.getSummary(currentLaporanDays);
                List<String[]> topProduk = LaporanDAO.getTopProduk(currentLaporanDays, 10);
                javafx.application.Platform.runLater(() ->
                    ReportGenerator.showPreview(daily, summary, topProduk, currentLaporanDays)
                );
                return null;
            }
        };
        new Thread(task).start();
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

    public void setupPenjualanTable() {
        if (tablePenjualanAll == null) return;

        colPenjualanAllNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tablePenjualanAll.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colPenjualanAllNoTrx.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        colPenjualanAllTotal.setCellValueFactory(new PropertyValueFactory<>("totalBelanja"));
        colPenjualanAllWaktu.setCellValueFactory(cellData -> {
            java.sql.Timestamp waktu = cellData.getValue().getWaktuTransaksi();
            if (waktu != null) {
                return new SimpleStringProperty(waktu.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new SimpleStringProperty("-");
        });
    }

    private void setupDashboardTables() {
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

        colPenjualanNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                tablePenjualan.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());
        colPenjualanNoTrx.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        colPenjualanTotal.setCellValueFactory(new PropertyValueFactory<>("totalBelanja"));
        colPenjualanWaktu.setCellValueFactory(cellData -> {
            java.sql.Timestamp waktu = cellData.getValue().getWaktuTransaksi();
            if (waktu != null) {
                return new SimpleStringProperty(waktu.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new SimpleStringProperty("-");
        });

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
            private List<Produk> produkMenipisList;
            private List<Penjualan> latestPenjualanList;
            private List<ProdukTerlaris> produkTerlarisList;
            private List<Penjualan> allPenjualanList;

            @Override
            protected Void call() {
                totalPenjualanVal = penjualanDAO.getTotalPenjualan();
                totalProdukVal = produkDAO.getTotalProduk();
                totalStokVal = produkDAO.getTotalStok();

                produkMenipisList = produkDAO.getProdukMenipis();
                latestPenjualanList = penjualanDAO.getLatestPenjualan(5);
                produkTerlarisList = produkDAO.getProdukTerlaris(5);
                allPenjualanList = penjualanDAO.getAllPenjualan();
                return null;
            }

            @Override
            protected void succeeded() {
                lblTotalPenjualan.setText(rupiahFormat.format(totalPenjualanVal));
                lblTotalProduk.setText(String.valueOf(totalProdukVal));
                lblTotalStok.setText(String.valueOf(totalStokVal));

                tableStok.setItems(FXCollections.observableArrayList(produkMenipisList));
                tablePenjualan.setItems(FXCollections.observableArrayList(latestPenjualanList));
                tableTerlaris.setItems(FXCollections.observableArrayList(produkTerlarisList));

                if (tablePenjualanAll != null) {
                    tablePenjualanAll.setItems(FXCollections.observableArrayList(allPenjualanList));
                    tablePenjualanAll.refresh();
                }

                tableStok.refresh();
                tablePenjualan.refresh();
                tableTerlaris.refresh();
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
    private void showLaporanView() {
        switchTo(laporanView, btnLaporan);
        loadLaporanData(currentLaporanDays);
    }

    @FXML
    private void showUserView() {
        switchTo(userView, btnUser);
        showFilterActiveUser();
    }

    @FXML
    private void showFilterActiveUser() {
        setUserFilterStyle(btnFilterActiveUser);
        activeUserSection.setVisible(true); activeUserSection.setManaged(true);
        deletedUserSection.setVisible(false); deletedUserSection.setManaged(false);
        loadUserData();
    }

    @FXML
    private void showFilterDeletedUser() {
        setUserFilterStyle(btnFilterDeletedUser);
        activeUserSection.setVisible(false); activeUserSection.setManaged(false);
        deletedUserSection.setVisible(true); deletedUserSection.setManaged(true);
        loadDeletedUserData();
    }

    private void setUserFilterStyle(Button active) {
        for (Button b : new Button[]{btnFilterActiveUser, btnFilterDeletedUser}) {
            b.setStyle(b == active ? "-fx-font-weight: bold;" : "");
        }
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

    // =========================================================
    // USER MANAGEMENT (same as Admin)
    // =========================================================
    private void setupUserTable() {
        colUserNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableUser.getItems().indexOf(cd.getValue()) + 1).asObject());
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserNama.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadUserData() {
        List<User> list = userDAO.getAllUsers();
        tableUser.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleSearchUser() {
        String keyword = searchUserField.getText().trim();
        List<User> results = keyword.isEmpty()
                ? userDAO.getAllUsers()
                : userDAO.searchUser(keyword);
        tableUser.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleTambahUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Tambah Pengguna");
        dialog.setHeaderText("Isi data pengguna baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fUsername = new TextField(); fUsername.setPromptText("Username");
        PasswordField fPassword = new PasswordField(); fPassword.setPromptText("Password");
        TextField fNama = new TextField(); fNama.setPromptText("Nama Lengkap");
        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("Admin", "Kasir", "Manager");
        fRole.setValue("Kasir");

        VBox form = new VBox(8, fUsername, fPassword, fNama, fRole);
        dialog.getDialogPane().setContent(form);
        javafx.application.Platform.runLater(fUsername::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (fUsername.getText().trim().isEmpty() || fPassword.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input salah", "Username dan Password wajib diisi.");
                    return null;
                }
                User u = new User();
                u.setUsername(fUsername.getText().trim());
                u.setPassword(fPassword.getText().trim());
                u.setNamaLengkap(fNama.getText().trim());
                u.setRole(fRole.getValue());
                return u;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            boolean success = userDAO.addUser(u);
            if (success) {
                loadUserData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pengguna " + u.getNamaLengkap() + " berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Pengguna gagal ditambahkan.");
            }
        });
    }

    @FXML
    private void handleEditUser() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Pengguna", "Pilih pengguna yang ingin diedit.");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit Pengguna");
        dialog.setHeaderText("Edit data: " + selected.getNamaLengkap());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fUsername = new TextField(selected.getUsername());
        TextField fNama = new TextField(selected.getNamaLengkap());
        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("Admin", "Kasir", "Manager");
        fRole.setValue(selected.getRole());

        VBox form = new VBox(8,
                new Label("ID: " + selected.getIdUser()),
                fUsername, fNama, fRole);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                selected.setUsername(fUsername.getText().trim());
                selected.setNamaLengkap(fNama.getText().trim());
                selected.setRole(fRole.getValue());
                return selected;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            boolean success = userDAO.updateUser(u);
            if (success) {
                tableUser.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pengguna berhasil diupdate.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Pengguna gagal diupdate.");
            }
        });
    }

    @FXML
    private void handleGantiPasswordUser() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Pengguna", "Pilih pengguna yang ingin diganti passwordnya.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ganti Password");
        dialog.setHeaderText("Ganti password untuk: " + selected.getNamaLengkap());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField fPassword = new PasswordField(); fPassword.setPromptText("Password baru");

        VBox form = new VBox(8, fPassword);
        dialog.getDialogPane().setContent(form);
        javafx.application.Platform.runLater(fPassword::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String pw = fPassword.getText().trim();
                if (pw.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input salah", "Password tidak boleh kosong.");
                    return null;
                }
                return pw;
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pw -> {
            boolean success = userDAO.updateUserPassword(selected.getIdUser(), pw);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Password berhasil diganti.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Password gagal diganti.");
            }
        });
    }

    @FXML
    private void handleHapusUser() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Pengguna", "Pilih pengguna yang ingin dihapus.");
            return;
        }

        if (selected.getIdUser() == Session.getCurrentUserId()) {
            showAlert(Alert.AlertType.WARNING, "Tidak bisa dihapus", "Anda tidak bisa menghapus akun Anda sendiri.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Pengguna");
        confirm.setHeaderText("Yakin ingin menghapus " + selected.getNamaLengkap() + "?");
        confirm.setContentText("Data yang dihapus tidak dapat dikembalikan.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.deleteUser(selected.getIdUser());
            if (success) {
                loadUserData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pengguna berhasil dihapus.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Pengguna gagal dihapus.");
            }
        }
    }

    // =========================================================
    // DELETED USER TABLE SETUP + DATA
    // =========================================================
    private void setupDeletedUserTable() {
        colDltUserNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableDeletedUser.getItems().indexOf(cd.getValue()) + 1).asObject());
        colDltUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDltUserNama.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        colDltUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadDeletedUserData() {
        List<User> list = userDAO.getDeletedUsers();
        tableDeletedUser.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handlePulihkanUser() {
        User selected = tableDeletedUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Pengguna", "Pilih pengguna yang ingin dipulihkan.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Pulihkan Pengguna");
        confirm.setHeaderText("Yakin ingin memulihkan " + selected.getNamaLengkap() + "?");
        confirm.setContentText("Pengguna akan diaktifkan kembali.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.restoreUser(selected.getIdUser());
            if (success) {
                selected.setStatus(1);
                loadDeletedUserData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Pengguna " + selected.getNamaLengkap() + " berhasil dipulihkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memulihkan pengguna.");
            }
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Yakin ingin logout?");
        confirm.setContentText("Anda akan kembali ke halaman login.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        Session.clear();
        try {
            SceneController.switchTo("/com/project/tas_pbo/view/login-view.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}