package com.project.tas_pbo.controller;

import com.project.tas_pbo.service.QrisClient;
import com.project.tas_pbo.DAO.PenjualanDAO;
import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;
import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.service.ReceiptPrinter;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class POSController {

    // ===== Komponen UI =====
    @FXML private BorderPane rootPane;
    @FXML private Label kasirLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    // ===== Tabel Produk =====
    @FXML private TableView<Produk> produkTable;
    @FXML private TableColumn<Produk, Integer> colProdukNo;
    @FXML private TableColumn<Produk, String> colProdukBarcode;
    @FXML private TableColumn<Produk, String> colProdukNama;
    @FXML private TableColumn<Produk, String> colProdukHarga;
    @FXML private TableColumn<Produk, Integer> colProdukStok;
    @FXML private TableColumn<Produk, String> colProdukSatuan;
    @FXML private TableColumn<Produk, Void> colProdukAksi;
    @FXML private TextField searchField;

    // ===== Tabel Keranjang =====
    @FXML private TableView<PenjualanDetail> cartTable;
    @FXML private TableColumn<PenjualanDetail, Integer> colNo;
    @FXML private TableColumn<PenjualanDetail, String> colBarcode;
    @FXML private TableColumn<PenjualanDetail, String> colNama;
    @FXML private TableColumn<PenjualanDetail, Integer> colQty;
    @FXML private TableColumn<PenjualanDetail, String> colSatuan;
    @FXML private TableColumn<PenjualanDetail, String> colHarga;
    @FXML private TableColumn<PenjualanDetail, String> colTotal;

    // ===== Label Total =====
    @FXML private Label grandTotalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalTagihanLabel;

    // ===== Input Pembayaran =====
    @FXML private TextField payField;
    @FXML private Button bayarButton;

    // ===== Data dan DAO =====
    private final ObservableList<PenjualanDetail> cartItems = FXCollections.observableArrayList();
    private final ObservableList<Produk> produkItems = FXCollections.observableArrayList();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private double subtotal = 0;
    private double totalTagihan = 0;

    // menyimpan transaksi terakhir untuk cetak ulang struk
    private Penjualan lastPenjualan = null;
    private List<PenjualanDetail> lastCartItems = null;

    @FXML
    public void initialize() {
        setupProdukTable();
        setupCartTable();
        startClock();

        kasirLabel.setText("Kasir: " + Session.getCurrentUsername());
        payField.setText("0");

        loadProdukData();
        updateTotals();

        Platform.runLater(() -> searchField.requestFocus());
    }

    // Mengatur kolom-kolom pada tabel produk
    private void setupProdukTable() {
        colProdukNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                produkTable.getItems().indexOf(cd.getValue()) + 1).asObject());
        colProdukBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProdukNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colProdukHarga.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getHarga())));
        colProdukStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colProdukSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));

        // Tombol tambah produk ke keranjang
        colProdukAksi.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+");

            {
                addBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 30px;");
                addBtn.setOnAction(e -> {
                    Produk produk = getTableView().getItems().get(getIndex());
                    addToCart(produk, 1);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addBtn);
            }
        });

        produkTable.setItems(produkItems);
        produkTable.setPlaceholder(new Label("Tidak ada produk ditemukan"));
    }

    // Memuat data produk dari database menggunakan background thread
    private void loadProdukData() {
        Task<List<Produk>> task = new Task<>() {
            @Override protected List<Produk> call() {
                return produkDAO.getAllProduk();
            }
        };
        task.setOnSucceeded(e -> produkItems.setAll(task.getValue()));
        new Thread(task).start();
    }

    // Mencari produk berdasarkan keyword
    @FXML
    private void handleSearchProduk() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProdukData();
            return;
        }
        List<Produk> results = produkDAO.searchProduk(keyword);
        produkItems.setAll(results);
    }

    // Mereset pencarian dan menampilkan semua produk
    @FXML
    private void handleResetSearch() {
        searchField.clear();
        loadProdukData();
        searchField.requestFocus();
    }

    // Mengatur kolom-kolom pada tabel keranjang
    private void setupCartTable() {
        colNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                cartTable.getItems().indexOf(cd.getValue()) + 1).asObject());
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colSatuan.setCellValueFactory(cd -> new SimpleStringProperty("Pcs"));
        colHarga.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getHargaSatuan())));
        colTotal.setCellValueFactory(cd -> new SimpleStringProperty(
                rupiahFormat.format(cd.getValue().getSubtotal())));

        cartTable.setItems(cartItems);
        cartTable.setPlaceholder(new Label("Keranjang kosong - klik + pada produk untuk menambahkan"));
    }

    // Menambahkan produk ke keranjang belanja
    private void addToCart(Produk produk, int qty) {
        if (produk.getStok() < qty) {
            showAlert(Alert.AlertType.WARNING, "Stok tidak cukup",
                    "Stok " + produk.getNamaProduk() + " hanya tersisa " + produk.getStok());
            return;
        }

        // Jika produk sudah ada di keranjang, tambah jumlahnya
        for (PenjualanDetail item : cartItems) {
            if (item.getIdProduk() == produk.getIdProduk()) {
                int newQty = item.getJumlah() + qty;
                if (newQty > produk.getStok()) {
                    showAlert(Alert.AlertType.WARNING, "Stok tidak cukup",
                            "Stok " + produk.getNamaProduk() + " hanya tersisa " + produk.getStok());
                    return;
                }
                item.setJumlah(newQty);
                cartTable.refresh();
                updateTotals();
                return;
            }
        }

        // Produk baru, buat item keranjang baru
        PenjualanDetail detail = new PenjualanDetail(
                produk.getIdProduk(),
                produk.getBarcode(),
                produk.getNamaProduk(),
                produk.getHarga(),
                qty
        );
        cartItems.add(detail);
        updateTotals();
    }

    // Mengubah jumlah item di keranjang
    @FXML
    private void handleEditQty() {
        PenjualanDetail selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih item", "Pilih item di keranjang terlebih dahulu.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getJumlah()));
        dialog.setTitle("Edit Qty");
        dialog.setHeaderText("Ubah jumlah untuk " + selected.getNamaProduk());
        dialog.setContentText("Qty baru:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int newQty = Integer.parseInt(input.trim());
                if (newQty <= 0) {
                    cartItems.remove(selected);
                } else {
                    Produk produk = produkDAO.getProdukById(selected.getIdProduk());
                    if (produk != null && newQty > produk.getStok()) {
                        showAlert(Alert.AlertType.WARNING, "Stok tidak cukup",
                                "Stok tersisa hanya " + produk.getStok());
                        return;
                    }
                    selected.setJumlah(newQty);
                    cartTable.refresh();
                }
                updateTotals();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input tidak valid", "Masukkan angka yang valid.");
            }
        });
    }

    // Menghapus item dari keranjang
    @FXML
    private void handleHapusItem() {
        PenjualanDetail selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Pilih item", "Pilih item di keranjang yang ingin dihapus.");
            return;
        }
        cartItems.remove(selected);
        updateTotals();
    }

    // Fokus ke field pencarian untuk scan barcode
    @FXML
    private void handleScanBarcode() {
        searchField.requestFocus();
        searchField.clear();
    }

    // Mereset seluruh transaksi (mengosongkan keranjang)
    @FXML
    private void handleReset() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Kosong", "Keranjang sudah kosong.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Transaksi");
        confirm.setHeaderText("Yakin ingin mereset transaksi?");
        confirm.setContentText("Semua item di keranjang akan dihapus.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            resetTransaction();
        }
    }

    // Mengecek harga produk berdasarkan nama
    @FXML
    private void handleCekHarga() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cek Harga");
        dialog.setHeaderText("Masukkan nama produk");
        dialog.setContentText("Nama produk:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(keyword -> {
            String trimmed = keyword.trim();
            if (trimmed.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input kosong", "Masukkan nama produk terlebih dahulu.");
                return;
            }
            List<Produk> found = produkDAO.searchByName(trimmed);
            if (found.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Tidak ditemukan", "Produk tidak ditemukan.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Produk p : found) {
                    sb.append(p.getNamaProduk())
                            .append(" : Rp ").append(rupiahFormat.format(p.getHarga()))
                            .append(" (Stok: ").append(p.getStok()).append(")\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Harga Produk", sb.toString());
            }
        });
    }

    // Logout dari sistem
    @FXML
    private void handleLogout(ActionEvent event) {
        String header = cartItems.isEmpty()
                ? "Yakin ingin logout?"
                : "Transaksi masih berlangsung";
        String content = cartItems.isEmpty()
                ? "Anda akan kembali ke halaman login."
                : "Ada item di keranjang. Yakin ingin logout?";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText(header);
        confirm.setContentText(content);
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

    // Memperbarui tampilan total belanja
    private void updateTotals() {
        subtotal = cartItems.stream().mapToDouble(PenjualanDetail::getSubtotal).sum();
        totalTagihan = subtotal;

        subtotalLabel.setText(rupiahFormat.format(subtotal));
        totalTagihanLabel.setText(rupiahFormat.format(totalTagihan));
        grandTotalLabel.setText(rupiahFormat.format(totalTagihan));

        updateKembalian();
    }

    // Menghitung kembalian (saat ini kosong, untuk pengembangan)
    private void updateKembalian() {

    }

    // Mengambil nilai input pembayaran
    private double parsePayField() {
        try {
            return Double.parseDouble(payField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Menangani input dari numpad
    @FXML
    private void handleNumpad(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        String digit = source.getText();
        if (digit.equals("×") || digit.equals("−")) return;
        String current = payField.getText();
        if (current.equals("0")) current = "";
        payField.setText(current + digit);
        updateKembalian();
    }

    // Mereset input pembayaran
    @FXML
    private void handleClearPay() {
        payField.setText("0");
        updateKembalian();
    }

    // Menghapus digit terakhir pada input pembayaran
    @FXML
    private void handleBackspace() {
        String current = payField.getText();
        if (current.length() > 1) {
            payField.setText(current.substring(0, current.length() - 1));
        } else {
            payField.setText("0");
        }
        updateKembalian();
    }

    private String selectedPaymentMethod = "Tunai";

    // Field untuk pembayaran QRIS
    private boolean isQrisPayment = false;
    private String activeQrisOrderId = null;
    private javafx.animation.Timeline qrisStatusPoller = null;

    // Menangani pemilihan metode pembayaran
    @FXML
    private void handlePaymentMethod(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        String method = source.getText().replaceAll("[^a-zA-Z\\-]", "").trim();

        // Batalkan QRIS jika berpindah dari metode QRIS
        if (isQrisPayment && !method.contains("QRIS")) {
            cancelQrisIfActive();
        }

        selectedPaymentMethod = method;
        isQrisPayment = method.contains("QRIS");

        // Perbarui gaya tombol metode pembayaran
        source.getParent().getChildrenUnmodifiable().forEach(node ->
                node.getStyleClass().remove("pay-method-active"));
        source.getStyleClass().add("pay-method-active");

        // Jika QRIS dipilih dan keranjang tidak kosong, buat QR segera
        if (isQrisPayment && !cartItems.isEmpty()) {
            initiateQrisPayment();
        }
    }

    // Memulai pembayaran QRIS
    private void initiateQrisPayment() {
        if (totalTagihan <= 0) {
            showAlert(Alert.AlertType.WARNING, "Keranjang kosong",
                    "Tambahkan produk sebelum membayar dengan QRIS.");
            return;
        }

        // Tampilkan alert loading
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("QRIS");
        loadingAlert.setHeaderText("Membuat QR Code...");
        loadingAlert.setContentText("Mohon tunggu sebentar.");
        loadingAlert.show();

        // Panggil server Spring Boot di background thread
        javafx.concurrent.Task<QrisClient.QrisResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected QrisClient.QrisResult call() {
                return QrisClient.createQris((long) totalTagihan);
            }
        };

        task.setOnSucceeded(e -> {
            loadingAlert.close();
            QrisClient.QrisResult result = task.getValue();

            if (result.success) {
                activeQrisOrderId = result.orderId;

                // Beritahu kasir untuk membuka QR di HP
                Alert qrisAlert = new Alert(Alert.AlertType.INFORMATION);
                qrisAlert.setTitle("QRIS Siap");
                qrisAlert.setHeaderText("QR Code berhasil dibuat!");
                qrisAlert.setContentText(
                        "Total: Rp " + rupiahFormat.format((long) totalTagihan) + "\n\n" +
                                "Tampilkan Qris ke pelanggan untuk scan.\n" +
                                "Menunggu pembayaran..."
                );
                qrisAlert.show();

                // Mulai polling status setiap 3 detik
                startQrisStatusPolling(qrisAlert);

            } else {
                showAlert(Alert.AlertType.ERROR, "QRIS Gagal",
                        result.error + "\n\nPastikan server QRIS berjalan di localhost:8080");
            }
        });

        task.setOnFailed(e -> {
            loadingAlert.close();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuat QRIS: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Memulai polling status pembayaran QRIS secara berkala
    private void startQrisStatusPolling(Alert qrisAlert) {
        if (qrisStatusPoller != null) {
            qrisStatusPoller.stop();
        }

        qrisStatusPoller = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(3),
                        e -> pollQrisStatus(qrisAlert)
                )
        );
        qrisStatusPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
        qrisStatusPoller.play();
    }

    // Mengecek status pembayaran QRIS
    private void pollQrisStatus(Alert qrisAlert) {
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                return QrisClient.checkStatus();
            }
        };

        task.setOnSucceeded(e -> {
            String status = task.getValue();

            if ("SUCCESS".equals(status)) {
                // Pembayaran berhasil
                if (qrisStatusPoller != null) qrisStatusPoller.stop();
                if (qrisAlert.isShowing()) qrisAlert.close();

                // Selesaikan transaksi
                finalizeBayar();

            } else if ("EXPIRED".equals(status) || "CANCELLED".equals(status)) {
                if (qrisStatusPoller != null) qrisStatusPoller.stop();
                if (qrisAlert.isShowing()) qrisAlert.close();
                showAlert(Alert.AlertType.WARNING, "QRIS Kadaluarsa",
                        "QR Code sudah kadaluarsa. Silakan coba lagi.");
                isQrisPayment = false;
            }
        });

        new Thread(task).start();
    }

    // Membatalkan QRIS jika aktif
    private void cancelQrisIfActive() {
        if (activeQrisOrderId != null) {
            if (qrisStatusPoller != null) qrisStatusPoller.stop();
            new Thread(QrisClient::cancelQris).start();
            activeQrisOrderId = null;
            isQrisPayment = false;
        }
    }

    // Menangani pembayaran (tunai atau QRIS)
    @FXML
    private void handleBayar() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang kosong",
                    "Tambahkan produk terlebih dahulu sebelum membayar.");
            return;
        }

        if (isQrisPayment) {
            // QRIS: inisiasi atau cek jika sudah aktif
            if (activeQrisOrderId == null) {
                initiateQrisPayment();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "QRIS Aktif",
                        "QR Code sudah aktif. Minta pelanggan scan QR di HP kasir.");
            }
        } else {
            // Tunai: alur normal
            double bayar = parsePayField();
            if (bayar < totalTagihan) {
                showAlert(Alert.AlertType.WARNING, "Pembayaran kurang",
                        "Jumlah bayar (Rp " + rupiahFormat.format((long) bayar) +
                                ") kurang dari total tagihan (Rp " + rupiahFormat.format((long) totalTagihan) + ").");
                return;
            }
            finalizeBayar();
        }
    }

    /**
     * Menyimpan transaksi ke database, mencetak struk, dan mereset.
     * Dipanggil untuk tunai dan QRIS (setelah QRIS dikonfirmasi).
     */
    private void finalizeBayar() {
        double bayar = isQrisPayment ? totalTagihan : parsePayField();
        double kembalian = isQrisPayment ? 0 : bayar - totalTagihan;

        Penjualan penjualan = new Penjualan();
        penjualan.setNoTransaksi(penjualanDAO.generateNoTransaksi());
        penjualan.setIdUser(Session.getCurrentUserId());
        penjualan.setTotalBelanja(totalTagihan);
        penjualan.setBayar(bayar);
        penjualan.setKembalian(kembalian);

        lastCartItems = List.copyOf(cartItems);
        lastPenjualan = penjualan;

        int generatedId = penjualanDAO.saveTransaction(penjualan, cartItems);

        if (generatedId > 0) {
            // Beri tahu Spring Boot untuk membersihkan halaman mobile
            if (isQrisPayment) {
                new Thread(QrisClient::completeQris).start();
            }

            Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
            successAlert.setTitle("Pembayaran Berhasil");
            successAlert.setHeaderText("Transaksi " + penjualan.getNoTransaksi() + " berhasil!");
            successAlert.setContentText(
                    (isQrisPayment ? "Metode: QRIS" :
                            "Kembalian: Rp " + rupiahFormat.format((long) kembalian)) +
                            "\n\nPilih aksi untuk struk:");
            ButtonType btnCetak = new ButtonType("🖨 Cetak Struk");
            ButtonType btnLihat = new ButtonType("👁 Lihat Struk");
            ButtonType btnTidak = new ButtonType("Tidak", ButtonBar.ButtonData.CANCEL_CLOSE);
            successAlert.getButtonTypes().setAll(btnCetak, btnLihat, btnTidak);

            Optional<ButtonType> result = successAlert.showAndWait();
            if (result.isPresent() && result.get() == btnCetak) {
                ReceiptPrinter.printToPrinter(penjualan, lastCartItems);
            } else if (result.isPresent() && result.get() == btnLihat) {
                ReceiptPrinter.showReceiptDialog(penjualan, lastCartItems);
            }

            // Reset state setelah transaksi
            isQrisPayment = false;
            activeQrisOrderId = null;
            if (qrisStatusPoller != null) qrisStatusPoller.stop();
            resetTransactionInternal();
            loadProdukData();

        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Transaksi gagal disimpan. Silakan coba lagi.");
        }
    }

    // Mencetak struk transaksi terakhir
    @FXML
    private void handleCetakStruk() {
        if (lastPenjualan != null && lastCartItems != null) {
            ReceiptPrinter.printToPrinter(lastPenjualan, lastCartItems);
        } else if (!cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Info",
                    "Struk hanya tersedia setelah transaksi selesai. Klik BAYAR dulu.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Tidak ada struk",
                    "Tidak ada transaksi yang bisa dicetak.");
        }
    }

    // Mereset transaksi
    private void resetTransaction() {
        resetTransactionInternal();
    }

    // Mereset internal keranjang dan field input
    private void resetTransactionInternal() {
        cartItems.clear();
        payField.setText("0");
        searchField.clear();

        updateTotals();
        Platform.runLater(() -> searchField.requestFocus());
    }

    // Memulai jam digital
    @FXML
    public void startClock() {
        updateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    // Memperbarui tampilan jam dan tanggal
    @FXML
    public void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        if (timeLabel != null) timeLabel.setText(now.format(TIME_FORMAT));
        if (dateLabel != null) dateLabel.setText(now.format(DATE_FORMAT));
    }

    // Menampilkan alert dialog
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}