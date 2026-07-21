package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.DAO.UserDAO;
import com.project.tas_pbo.service.ReAuthService;
import com.project.tas_pbo.database.DBconnection;
import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.model.User;
import com.project.tas_pbo.util.Session;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    // ===== Sidebar buttons =====
    @FXML private Button btnDashboard;
    @FXML private Button btnProduk;
    @FXML private Button btnStok;
    @FXML private Button btnUser;

    // ===== Views =====
    @FXML private ScrollPane dashboardView;
    @FXML private VBox produkView;
    @FXML private VBox stokView;
    @FXML private VBox userView;

    // ===== Produk filter buttons & sections =====
    @FXML private Button btnFilterActive;
    @FXML private Button btnFilterDeleted;
    @FXML private Button btnFilterLowStock;
    @FXML private VBox activeProdukSection;
    @FXML private VBox deletedProdukSection;
    @FXML private VBox lowStockSection;

    // ===== Top bar =====
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    // ===== Dashboard cards =====
    @FXML private Label cardTotalProduk;
    @FXML private Label cardTotalStok;
    @FXML private Label cardStokMenipis;

    // ===== Dashboard stok menipis table =====
    @FXML private TableView<Produk> tableStokMenipis;
    @FXML private TableColumn<Produk, Integer> colStokNo;
    @FXML private TableColumn<Produk, String> colStokNama;
    @FXML private TableColumn<Produk, Integer> colStokJumlah;
    @FXML private TableColumn<Produk, Integer> colStokMin;
    @FXML private TableColumn<Produk, String> colStokSatuan;
    @FXML private TableColumn<Produk, String> colStokStatus;

    // ===== Produk view =====
    @FXML private TableView<Produk> tableProduk;
    @FXML private TableColumn<Produk, Integer> colProdukNo;
    @FXML private TableColumn<Produk, String> colProdukBarcode;
    @FXML private TableColumn<Produk, String> colProdukNama;
    @FXML private TableColumn<Produk, String> colProdukKategori;
    @FXML private TableColumn<Produk, String> colProdukHarga;
    @FXML private TableColumn<Produk, Integer> colProdukStok;
    @FXML private TableColumn<Produk, String> colProdukSatuan;
    @FXML private TableColumn<Produk, String> colProdukStatus;
    @FXML private TextField searchProdukField;
    @FXML private Button btnPrevProduk;
    @FXML private Button btnNextProduk;
    @FXML private Label lblPageProduk;

    // ===== Stok view =====
    @FXML private TableView<Produk> tableStok;
    @FXML private TableColumn<Produk, Integer> colStokAllNo;
    @FXML private TableColumn<Produk, String> colStokAllNama;
    @FXML private TableColumn<Produk, Integer> colStokAllJumlah;
    @FXML private TableColumn<Produk, Integer> colStokAllMin;
    @FXML private TableColumn<Produk, String> colStokAllSatuan;
    @FXML private TableColumn<Produk, String> colStokAllStatus;

    // ===== Deleted Produk view =====
    @FXML private TableView<Produk> tableDeletedProduk;
    @FXML private TableColumn<Produk, Integer> colDltNo;
    @FXML private TableColumn<Produk, String> colDltBarcode;
    @FXML private TableColumn<Produk, String> colDltNama;
    @FXML private TableColumn<Produk, String> colDltKategori;
    @FXML private TableColumn<Produk, String> colDltHarga;
    @FXML private TableColumn<Produk, String> colDltSatuan;
    @FXML private TableColumn<Produk, String> colDltStatus;

    // ===== Low Stock view =====
    @FXML private TableView<Produk> tableLowStock;
    @FXML private TableColumn<Produk, Integer> colLsNo;
    @FXML private TableColumn<Produk, String> colLsNama;
    @FXML private TableColumn<Produk, Integer> colLsStok;
    @FXML private TableColumn<Produk, Integer> colLsMin;
    @FXML private TableColumn<Produk, String> colLsSatuan;
    @FXML private TableColumn<Produk, String> colLsStatus;

    // ===== User view =====
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

    // ===== Deleted User table =====
    @FXML private TableView<User> tableDeletedUser;
    @FXML private TableColumn<User, Integer> colDltUserNo;
    @FXML private TableColumn<User, String> colDltUserUsername;
    @FXML private TableColumn<User, String> colDltUserNama;
    @FXML private TableColumn<User, String> colDltUserRole;

    // ===== DAOs =====
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    // ===== Pagination =====
    private List<Produk> allProdukData = new ArrayList<>();
    private final ObservableList<Produk> produkPageData = FXCollections.observableArrayList();
    private int currentProdukPage = 0;
    private static final int PAGE_SIZE = 10;

    // ===== Nav state =====
    private Node[] allViews;
    private Button[] allButtons;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    @FXML
    public void initialize() {
        allViews = new Node[]{dashboardView, produkView, stokView, userView};
        allButtons = new Button[]{btnDashboard, btnProduk, btnStok, btnUser};

        setupProdukTable();
        setupStokTable();
        setupDeletedProdukTable();
        setupLowStockTable();
        setupUserTable();
        setupDeletedUserTable();
        setupStokMenipisTable();
        startClock();

        if (Session.getCurrentUser() != null) {
            welcomeLabel.setText("Selamat datang, " + Session.getCurrentUsername() + "!");
            userNameLabel.setText(Session.getCurrentUsername());
        }

        loadDashboardData();
    }

    @FXML private void showDashboardView() {
        switchTo(dashboardView, btnDashboard);
        loadDashboardData();
    }

    @FXML private void showProdukView() {
        switchTo(produkView, btnProduk);
        showFilterActive();
    }

    @FXML private void showStokView() {
        switchTo(stokView, btnStok);
        loadStokData();
    }

    // ===== Produk filter navigation =====
    @FXML private void showFilterActive() {
        setFilterStyle(btnFilterActive);
        activeProdukSection.setVisible(true);
        activeProdukSection.setManaged(true);
        deletedProdukSection.setVisible(false);
        deletedProdukSection.setManaged(false);
        lowStockSection.setVisible(false);
        lowStockSection.setManaged(false);
        if (allProdukData.isEmpty()) loadProdukData();
    }

    @FXML private void showFilterDeleted() {
        setFilterStyle(btnFilterDeleted);
        activeProdukSection.setVisible(false);
        activeProdukSection.setManaged(false);
        deletedProdukSection.setVisible(true);
        deletedProdukSection.setManaged(true);
        lowStockSection.setVisible(false);
        lowStockSection.setManaged(false);
        loadDeletedProdukData();
    }

    @FXML private void showFilterLowStock() {
        setFilterStyle(btnFilterLowStock);
        activeProdukSection.setVisible(false);
        activeProdukSection.setManaged(false);
        deletedProdukSection.setVisible(false);
        deletedProdukSection.setManaged(false);
        lowStockSection.setVisible(true);
        lowStockSection.setManaged(true);
        loadLowStockData();
    }

    private void setFilterStyle(Button active) {
        for (Button b : new Button[]{btnFilterActive, btnFilterDeleted, btnFilterLowStock}) {
            b.setStyle(b == active ? "-fx-font-weight: bold;" : "");
        }
    }

    @FXML private void showUserView() {
        switchTo(userView, btnUser);
        showFilterActiveUser();
    }

    @FXML private void showFilterActiveUser() {
        setUserFilterStyle(btnFilterActiveUser);
        activeUserSection.setVisible(true); activeUserSection.setManaged(true);
        deletedUserSection.setVisible(false); deletedUserSection.setManaged(false);
        loadUserData();
    }

    @FXML private void showFilterDeletedUser() {
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
            boolean active = (v == activeView);
            v.setVisible(active);
            v.setManaged(active);
        }
        for (Button b : allButtons) {
            b.getStyleClass().setAll(b == activeBtn ? "nav-btn-active" : "nav-btn");
        }
    }

    // =========================================================
    // DASHBOARD DATA
    // =========================================================
    private void loadDashboardData() {
        Task<List<Produk>> task = new Task<>() {
            @Override protected List<Produk> call() { return produkDAO.getAllProduk(); }
        };

        task.setOnSucceeded(e -> {
            List<Produk> all = task.getValue();
            int totalStok = all.stream().mapToInt(Produk::getStok).sum();
            long menipis = all.stream().filter(p -> p.getStok() <= p.getStokMinimum()).count();
            List<Produk> stokMenipisList = all.stream()
                    .filter(p -> p.getStok() <= p.getStokMinimum()).toList();

            cardTotalProduk.setText(String.valueOf(all.size()));
            cardTotalStok.setText(String.valueOf(totalStok));
            cardStokMenipis.setText(String.valueOf(menipis));

            tableStokMenipis.setItems(FXCollections.observableArrayList(stokMenipisList));
            tableStokMenipis.refresh();
        });

        new Thread(task).start();
    }

    // PRODUK TABLE SETUP + DATA
    private void setupProdukTable() {
        colProdukNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableProduk.getItems().indexOf(cd.getValue()) + 1).asObject());
        colProdukBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProdukNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colProdukKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colProdukHarga.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getHarga())));
        colProdukStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colProdukSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colProdukStatus.setCellValueFactory(cd -> {
            Produk p = cd.getValue();
            if (p.getStatus() == 0) return new SimpleStringProperty("Dihapus");
            if (p.getStok() <= 0) return new SimpleStringProperty("Habis");
            return new SimpleStringProperty("Tersedia");
        });
        tableProduk.setItems(produkPageData);
    }

    private void loadProdukData() {
        btnNextProduk.setDisable(true);
        btnPrevProduk.setDisable(true);
        lblPageProduk.setText("Memuat...");

        Task<List<Produk>> task = new Task<>() {
            @Override protected List<Produk> call() { return produkDAO.getAllProduk(); }
        };
        task.setOnSucceeded(e -> {
            allProdukData = new ArrayList<>(task.getValue());
            currentProdukPage = 0;
            showProdukPage(currentProdukPage);
        });
        new Thread(task).start();
    }

    private void showProdukPage(int page) {
        if (allProdukData.isEmpty()) return;
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allProdukData.size());
        int total = (int) Math.ceil((double) allProdukData.size() / PAGE_SIZE);

        produkPageData.setAll(allProdukData.subList(from, to));
        lblPageProduk.setText("Halaman " + (page + 1) + " dari " + total);
        btnPrevProduk.setDisable(page == 0);
        btnNextProduk.setDisable(to >= allProdukData.size());
    }

    @FXML private void prevPageProduk() { currentProdukPage--; showProdukPage(currentProdukPage); }
    @FXML private void nextPageProduk() { currentProdukPage++; showProdukPage(currentProdukPage); }

    @FXML
    private void handleSearchProduk() {
        String keyword = searchProdukField.getText().trim();
        if (keyword.isEmpty()) {
            loadProdukData();
            return;
        }
        List<Produk> results = produkDAO.searchProduk(keyword);
        allProdukData = new ArrayList<>(results);
        currentProdukPage = 0;
        showProdukPage(0);
    }

    @FXML
    private void handleTambahProduk() {
        if (!ReAuthService.requireReAuth()) return;
        Dialog<Produk> dialog = new Dialog<>();
        dialog.setTitle("Tambah Produk");
        dialog.setHeaderText("Isi data produk baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fBarcode = new TextField(); fBarcode.setPromptText("Barcode");
        TextField fNama = new TextField(); fNama.setPromptText("Nama Produk");
        TextField fKategori = new TextField(); fKategori.setPromptText("Kategori");
        TextField fHarga = new TextField(); fHarga.setPromptText("Harga");
        TextField fStok = new TextField(); fStok.setPromptText("Stok");
        TextField fSatuan = new TextField(); fSatuan.setPromptText("Satuan");
        TextField fStokMin = new TextField(); fStokMin.setPromptText("Stok Minimum");

        VBox form = new VBox(8, fBarcode, fNama, fKategori, fHarga, fStok, fSatuan, fStokMin);
        dialog.getDialogPane().setContent(form);
        Platform.runLater(fBarcode::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Produk p = new Produk();
                    p.setBarcode(fBarcode.getText().trim());
                    p.setNamaProduk(fNama.getText().trim());
                    p.setKategori(fKategori.getText().trim());
                    p.setHarga(Double.parseDouble(fHarga.getText().trim()));
                    p.setStok(Integer.parseInt(fStok.getText().trim()));
                    p.setSatuan(fSatuan.getText().trim());
                    p.setStokMinimum(Integer.parseInt(fStokMin.getText().trim()));
                    return p;
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Input salah", "Harga, Stok, dan Stok Minimum harus berupa angka.");
                }
            }
            return null;
        });

        Optional<Produk> result = dialog.showAndWait();
        result.ifPresent(p -> {
            boolean success = produkDAO.addProduk(p);
            if (success) {
                allProdukData.add(p);
                showProdukPage(currentProdukPage);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Produk berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Produk gagal ditambahkan.");
            }
        });
    }

    @FXML
    private void handleEditProduk() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableProduk.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang ingin diedit.");
            return;
        }

        Dialog<Produk> dialog = new Dialog<>();
        dialog.setTitle("Edit Produk");
        dialog.setHeaderText("Edit data: " + selected.getNamaProduk());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fBarcode = new TextField(selected.getBarcode());
        TextField fNama = new TextField(selected.getNamaProduk());
        TextField fKategori = new TextField(selected.getKategori());
        TextField fHarga = new TextField(String.valueOf(selected.getHarga()));
        TextField fStok = new TextField(String.valueOf(selected.getStok()));
        TextField fSatuan = new TextField(selected.getSatuan());
        TextField fStokMin = new TextField(String.valueOf(selected.getStokMinimum()));

        VBox form = new VBox(8, fBarcode, fNama, fKategori, fHarga, fStok, fSatuan, fStokMin);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    selected.setBarcode(fBarcode.getText().trim());
                    selected.setNamaProduk(fNama.getText().trim());
                    selected.setKategori(fKategori.getText().trim());
                    selected.setHarga(Double.parseDouble(fHarga.getText().trim()));
                    selected.setStok(Integer.parseInt(fStok.getText().trim()));
                    selected.setSatuan(fSatuan.getText().trim());
                    selected.setStokMinimum(Integer.parseInt(fStokMin.getText().trim()));
                    return selected;
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Input salah", "Harga, Stok, dan Stok Minimum harus berupa angka.");
                }
            }
            return null;
        });

        Optional<Produk> result = dialog.showAndWait();
        result.ifPresent(p -> {
            boolean success = produkDAO.updateProduk(p);
            if (success) {
                tableProduk.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Produk berhasil diupdate.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Produk gagal diupdate.");
            }
        });
    }

    @FXML
    private void handleHapusProduk() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableProduk.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang ingin dihapus.");
            return;
        }

        if (selected.getStatus() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Sudah Dihapus", "Produk ini sudah dihapus sebelumnya.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Produk");
        confirm.setHeaderText("Yakin ingin menghapus " + selected.getNamaProduk() + "?");
        confirm.setContentText("Stok produk akan diatur menjadi 0 dan produk akan ditandai sebagai dihapus.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = produkDAO.deleteProduk(selected.getIdProduk());
                if (success) {
                    allProdukData.remove(selected);
                    if (currentProdukPage > 0 && currentProdukPage * PAGE_SIZE >= allProdukData.size()) {
                        currentProdukPage--;
                    }
                    showProdukPage(currentProdukPage);
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Produk berhasil dihapus.");
                }
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal",
                        "Produk tidak dapat dihapus karena masih memiliki data penjualan terkait.\n\n" +
                        "Hapus atau edit data penjualan yang menggunakan produk ini terlebih dahulu.");
            }
        }
    }

    // STOK TABLE SETUP + DATA
    private void setupStokTable() {
        colStokAllNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableStok.getItems().indexOf(cd.getValue()) + 1).asObject());
        colStokAllNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colStokAllJumlah.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colStokAllMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colStokAllSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colStokAllStatus.setCellValueFactory(cd -> {
            Produk p = cd.getValue();
            String status = p.getStok() <= 0 ? "Habis" :
                    p.getStok() <= p.getStokMinimum() ? "Menipis" : "Aman";
            return new SimpleStringProperty(status);
        });
    }

    private void loadStokData() {
        List<Produk> list = produkDAO.getAllProduk();
        tableStok.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleTambahStok() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableStok.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang stoknya ingin ditambah.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Tambah Stok");
        dialog.setHeaderText("Tambah stok untuk: " + selected.getNamaProduk());
        dialog.setContentText("Jumlah tambah:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int tambah = Integer.parseInt(input.trim());
                selected.setStok(selected.getStok() + tambah);
                produkDAO.updateProduk(selected);
                tableStok.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Stok " + selected.getNamaProduk() + " berhasil ditambah " + tambah + ".");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input salah", "Masukkan angka yang valid.");
            }
        });
    }

    @FXML
    private void handleKurangiStok() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableStok.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang stoknya ingin dikurangi.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Kurangi Stok");
        dialog.setHeaderText("Kurangi stok untuk: " + selected.getNamaProduk());
        dialog.setContentText("Jumlah kurangi:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int kurangi = Integer.parseInt(input.trim());
                if (kurangi > selected.getStok()) {
                    showAlert(Alert.AlertType.WARNING, "Stok tidak cukup",
                            "Stok hanya tersisa " + selected.getStok());
                    return;
                }
                selected.setStok(selected.getStok() - kurangi);
                produkDAO.updateProduk(selected);
                tableStok.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Stok " + selected.getNamaProduk() + " berhasil dikurangi " + kurangi + ".");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input salah", "Masukkan angka yang valid.");
            }
        });
    }

    @FXML
    private void handleUpdateStok() {
        loadStokData();
        showAlert(Alert.AlertType.INFORMATION, "Refresh", "Data stok berhasil diperbarui.");
    }

    // STOK MENIPIS TABLE (dashboard)
    private void setupStokMenipisTable() {
        colStokNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableStokMenipis.getItems().indexOf(cd.getValue()) + 1).asObject());
        colStokNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colStokJumlah.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colStokMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colStokSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colStokStatus.setCellValueFactory(cd -> {
            Produk p = cd.getValue();
            String status = p.getStok() <= 0 ? "Habis" : "Menipis";
            return new SimpleStringProperty(status);
        });
    }

    // DELETED PRODUK TABLE SETUP + DATA
    private void setupDeletedProdukTable() {
        colDltNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableDeletedProduk.getItems().indexOf(cd.getValue()) + 1).asObject());
        colDltBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colDltNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colDltKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colDltHarga.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getHarga())));
        colDltSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colDltStatus.setCellValueFactory(cd -> new SimpleStringProperty("Dihapus"));
    }

    private void loadDeletedProdukData() {
        List<Produk> list = produkDAO.getDeletedProduk();
        tableDeletedProduk.setItems(FXCollections.observableArrayList(list));
    }

    // LOW STOCK TABLE SETUP + DATA
    private void setupLowStockTable() {
        colLsNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableLowStock.getItems().indexOf(cd.getValue()) + 1).asObject());
        colLsNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colLsStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colLsMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colLsSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colLsStatus.setCellValueFactory(cd -> {
            Produk p = cd.getValue();
            String status = p.getStok() <= 0 ? "Habis" :
                    p.getStok() <= p.getStokMinimum() ? "Menipis" : "Aman";
            return new SimpleStringProperty(status);
        });
    }

    private void loadLowStockData() {
        List<Produk> list = produkDAO.getProdukMenipis();
        tableLowStock.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleTambahStokLowStock() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableLowStock.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang stoknya ingin ditambah.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Tambah Stok");
        dialog.setHeaderText("Tambah stok untuk: " + selected.getNamaProduk());
        dialog.setContentText("Jumlah tambah:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int tambah = Integer.parseInt(input.trim());
                selected.setStok(selected.getStok() + tambah);
                produkDAO.updateProduk(selected);
                loadLowStockData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Stok " + selected.getNamaProduk() + " berhasil ditambah " + tambah + ".");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input salah", "Masukkan angka yang valid.");
            }
        });
    }

    @FXML
    private void handlePulihkanProduk() {
        if (!ReAuthService.requireReAuth()) return;
        Produk selected = tableDeletedProduk.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang ingin dipulihkan.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Pulihkan Produk");
        dialog.setHeaderText("Pulihkan: " + selected.getNamaProduk());
        dialog.setContentText("Masukkan stok awal:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int stokBaru = Integer.parseInt(input.trim());
                String sql = "UPDATE produk SET status = 1, stok = ? WHERE id_produk = ?";
                try (Connection conn = DBconnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, stokBaru);
                    stmt.setInt(2, selected.getIdProduk());
                    if (stmt.executeUpdate() > 0) {
                        selected.setStatus(1);
                        selected.setStok(stokBaru);
                        allProdukData.clear();
                        loadDeletedProdukData();
                        showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                                "Produk " + selected.getNamaProduk() + " berhasil dipulihkan dengan stok " + stokBaru + ".");
                    }
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input salah", "Masukkan angka yang valid.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memulihkan produk.");
            }
        });
    }

    // USER TABLE SETUP + DATA
    private void setupUserTable() {
        colUserNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableUser.getItems().indexOf(cd.getValue()) + 1).asObject());
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserNama.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadUserData() {
        List<User> list = userDAO.getAllUsers().stream()
                .filter(u -> "Kasir".equals(u.getRole()))
                .toList();
        tableUser.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleSearchUser() {
        String keyword = searchUserField.getText().trim();
        List<User> results = (keyword.isEmpty()
                ? userDAO.getAllUsers()
                : userDAO.searchUser(keyword)).stream()
                .filter(u -> "Kasir".equals(u.getRole()))
                .toList();
        tableUser.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleTambahUser() {
        if (!ReAuthService.requireReAuth()) return;
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Tambah Pengguna");
        dialog.setHeaderText("Isi data pengguna baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fUsername = new TextField(); fUsername.setPromptText("Username");
        PasswordField fPassword = new PasswordField(); fPassword.setPromptText("Password");
        TextField fNama = new TextField(); fNama.setPromptText("Nama Lengkap");
        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("Kasir");
        fRole.setValue("Kasir");

        VBox form = new VBox(8, fUsername, fPassword, fNama, fRole);
        dialog.getDialogPane().setContent(form);
        Platform.runLater(fUsername::requestFocus);

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
        if (!ReAuthService.requireReAuth()) return;
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
        fRole.getItems().addAll("Kasir");
        fRole.setValue("Kasir");

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
        if (!ReAuthService.requireReAuth()) return;
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
        Platform.runLater(fPassword::requestFocus);

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
        if (!ReAuthService.requireReAuth()) return;
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

    // DELETED USER TABLE SETUP + DATA
    private void setupDeletedUserTable() {
        colDltUserNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableDeletedUser.getItems().indexOf(cd.getValue()) + 1).asObject());
        colDltUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDltUserNama.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        colDltUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadDeletedUserData() {
        List<User> list = userDAO.getDeletedUsers().stream()
                .filter(u -> "Kasir".equals(u.getRole()))
                .toList();
        tableDeletedUser.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handlePulihkanUser() {
        if (!ReAuthService.requireReAuth()) return;
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

    // LOGOUT
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Yakin ingin logout?");
        confirm.setContentText("Anda akan kembali ke halaman login.");
        java.util.Optional<ButtonType> result = confirm.showAndWait();
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

    // CLOCK
    private void startClock() {
        updateClock();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        if (timeLabel != null) timeLabel.setText(now.format(TIME_FORMAT));
        if (dateLabel != null) dateLabel.setText(now.format(DATE_FORMAT));
    }

    // HELPERS
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}