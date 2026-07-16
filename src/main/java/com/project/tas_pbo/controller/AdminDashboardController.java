package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.MemberDAO;
import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.DAO.UserDAO;
import com.project.tas_pbo.model.Member;
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
    @FXML private Button btnMember;

    // ===== Views =====
    @FXML private ScrollPane dashboardView;
    @FXML private VBox produkView;
    @FXML private VBox stokView;
    @FXML private VBox memberView;
    @FXML private VBox userView;

    // ===== Top bar =====
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    // ===== Dashboard cards =====
    @FXML private Label cardTotalProduk;
    @FXML private Label cardTotalStok;
    @FXML private Label cardTotalMember;
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

    // ===== Member view =====
    @FXML private TableView<Member> tableMember;
    @FXML private TableColumn<Member, Integer> colMemberNo;
    @FXML private TableColumn<Member, String> colMemberKode;
    @FXML private TableColumn<Member, String> colMemberNama;
    @FXML private TableColumn<Member, String> colMemberTelp;
    @FXML private TableColumn<Member, Integer> colMemberPoin;
    @FXML private TableColumn<Member, String> colMemberTotal;
    @FXML private TextField searchMemberField;

    // ===== User view =====
    @FXML private TableView<User> tableUser;
    @FXML private TableColumn<User, Integer> colUserNo;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserNama;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TextField searchUserField;

    // ===== DAOs =====
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final MemberDAO memberDAO = new MemberDAO();
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
        allViews = new Node[]{dashboardView, produkView, stokView, userView, memberView};
        allButtons = new Button[]{btnDashboard, btnProduk, btnStok, btnUser, btnMember};

        setupProdukTable();
        setupStokTable();
        setupMemberTable();
        setupUserTable();
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
        if (allProdukData.isEmpty()) loadProdukData();
    }

    @FXML private void showStokView() {
        switchTo(stokView, btnStok);
        loadStokData();
    }

    @FXML private void showMemberView() {
        switchTo(memberView, btnMember);
        loadMemberData();
    }

    @FXML private void showUserView() {
        switchTo(userView, btnUser);
        loadUserData();
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
            cardTotalMember.setText(String.valueOf(memberDAO.countMember()));

            tableStokMenipis.setItems(FXCollections.observableArrayList(stokMenipisList));
            tableStokMenipis.refresh();
        });

        new Thread(task).start();
    }

    // =========================================================
    // PRODUK TABLE SETUP + DATA
    // =========================================================
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
        // Dialog for adding produk
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
        Produk selected = tableProduk.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Produk", "Pilih produk yang ingin dihapus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Produk");
        confirm.setHeaderText("Yakin ingin menghapus " + selected.getNamaProduk() + "?");
        confirm.setContentText("Data yang dihapus tidak dapat dikembalikan.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = produkDAO.deleteProduk(selected.getIdProduk());
            if (success) {
                allProdukData.remove(selected);
                showProdukPage(currentProdukPage);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Produk berhasil dihapus.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Produk gagal dihapus.");
            }
        }
    }

    // =========================================================
    // STOK TABLE SETUP + DATA
    // =========================================================
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

    // =========================================================
    // STOK MENIPIS TABLE (dashboard)
    // =========================================================
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

    // =========================================================
    // MEMBER TABLE SETUP + DATA
    // =========================================================
    private void setupMemberTable() {
        colMemberNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                tableMember.getItems().indexOf(cd.getValue()) + 1).asObject());
        colMemberKode.setCellValueFactory(new PropertyValueFactory<>("kodeMember"));
        colMemberNama.setCellValueFactory(new PropertyValueFactory<>("namaMember"));
        colMemberTelp.setCellValueFactory(new PropertyValueFactory<>("noTelepon"));
        colMemberPoin.setCellValueFactory(new PropertyValueFactory<>("poin"));
        colMemberTotal.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getTotalBelanja())));
    }

    private void loadMemberData() {
        List<Member> list = memberDAO.getAllMember();
        tableMember.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleSearchMember() {
        String keyword = searchMemberField.getText().trim();
        List<Member> results = keyword.isEmpty()
                ? memberDAO.getAllMember()
                : memberDAO.searchMember(keyword);
        tableMember.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleTambahMember() {
        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("Tambah Member");
        dialog.setHeaderText("Isi data member baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fNama = new TextField(); fNama.setPromptText("Nama Member");
        TextField fTelp = new TextField(); fTelp.setPromptText("No. Telepon");
        TextField fAlamat = new TextField(); fAlamat.setPromptText("Alamat");

        String kode = memberDAO.generateKodeMember();
        Label lKode = new Label("Kode Member: " + kode);

        VBox form = new VBox(8, lKode, fNama, fTelp, fAlamat);
        dialog.getDialogPane().setContent(form);
        Platform.runLater(fNama::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Member m = new Member();
                m.setKodeMember(kode);
                m.setNamaMember(fNama.getText().trim());
                m.setNoTelepon(fTelp.getText().trim());
                m.setAlamat(fAlamat.getText().trim());
                m.setPoin(0);
                m.setTotalBelanja(0);
                return m;
            }
            return null;
        });

        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(m -> {
            boolean success = memberDAO.addMember(m);
            if (success) {
                loadMemberData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Member " + m.getNamaMember() + " berhasil ditambahkan.\nKode: " + m.getKodeMember());
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Member gagal ditambahkan.");
            }
        });
    }

    @FXML
    private void handleEditMember() {
        Member selected = tableMember.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Member", "Pilih member yang ingin diedit.");
            return;
        }

        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("Edit Member");
        dialog.setHeaderText("Edit data: " + selected.getNamaMember());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fNama = new TextField(selected.getNamaMember());
        TextField fTelp = new TextField(selected.getNoTelepon());
        TextField fAlamat = new TextField(selected.getAlamat());
        TextField fPoin = new TextField(String.valueOf(selected.getPoin()));

        VBox form = new VBox(8,
                new Label("Kode: " + selected.getKodeMember()),
                fNama, fTelp, fAlamat, fPoin);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    selected.setNamaMember(fNama.getText().trim());
                    selected.setNoTelepon(fTelp.getText().trim());
                    selected.setAlamat(fAlamat.getText().trim());
                    selected.setPoin(Integer.parseInt(fPoin.getText().trim()));
                    return selected;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input salah", "Poin harus berupa angka.");
                }
            }
            return null;
        });

        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(m -> {
            boolean success = memberDAO.updateMember(m);
            if (success) {
                tableMember.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Member berhasil diupdate.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Member gagal diupdate.");
            }
        });
    }

    @FXML
    private void handleHapusMember() {
        Member selected = tableMember.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih Member", "Pilih member yang ingin dihapus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Member");
        confirm.setHeaderText("Yakin ingin menghapus " + selected.getNamaMember() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = memberDAO.deleteMember(selected.getIdMember());
            if (success) {
                loadMemberData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Member berhasil dihapus.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Member gagal dihapus.");
            }
        }
    }

    // =========================================================
    // USER TABLE SETUP + DATA
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
    // LOGOUT
    // =========================================================
    @FXML
    private void handleLogout(ActionEvent event) {
        Session.clear();
        try {
            SceneController.switchTo("/com/project/tas_pbo/view/login-view.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // CLOCK
    // =========================================================
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

    // =========================================================
    // HELPERS
    // =========================================================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}