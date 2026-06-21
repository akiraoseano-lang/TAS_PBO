package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.PenjualanDAO;
import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;
import com.project.tas_pbo.model.Produk;
//import com.project.tas_pbo.util.Session;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class POSController {

    @FXML private BorderPane rootPane;
    @FXML private Label kasirLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    @FXML private TableView<PenjualanDetail> cartTable;
    @FXML private TableColumn<PenjualanDetail, Integer> colNo;
    @FXML private TableColumn<PenjualanDetail, Integer> colBarcode;
    @FXML private TableColumn<PenjualanDetail, String> colNama;
    @FXML private TableColumn<PenjualanDetail, Integer> colQty;
    @FXML private TableColumn<PenjualanDetail, String> colSatuan;
    @FXML private TableColumn<PenjualanDetail, String> colTotal;

    @FXML private TextField searchField;

    @FXML private Label grandTotalLabel;
    @FXML private Label subtotalLabel;
    @FXML private TextField discountField;
    @FXML private Label totalTagihanLabel;

    @FXML private TextField payField;
    @FXML private Button bayarButton;

    private final ObservableList<PenjualanDetail> cartItems = FXCollections.observableArrayList();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private double subtotal = 0;
    private double diskon = 0;
    private double totalTagihan = 0;

    private void setupCartTable() {
        colNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cartTable.getItems().indexOf(cellData.getValue()) + 1
        ).asObject());

        colBarcode.setCellValueFactory(new PropertyValueFactory<>("idProduk"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));

        colSatuan.setCellValueFactory(cellData -> new SimpleStringProperty("Pcs"));

        colTotal.setCellValueFactory(cellData -> new SimpleStringProperty(
                rupiahFormat.format(cellData.getValue().getSubtotal())
        ));

        cartTable.setItems(cartItems);
        cartTable.setPlaceholder(new Label("Belum ada item, scan atau cari produk"));
    }

    @FXML
    public void initialize() {
        setupCartTable();
        setupSearch();
        startClock();
        setupPayField();

        kasirLabel.setText("Kasir: " );//Session.getCurrentUsername());
        discountField.setText("0");
        payField.setText("0");

        updateTotals();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;
             handleLiveSearch(newVal.trim());
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearchEnter(searchField.getText().trim());
            }
        });
    }

    private void handleLiveSearch(String keyword) {
        List<Produk> results = produkDAO.searchProduk(keyword);
    }

    private void handleSearchEnter(String keyword) {
        if (keyword.isEmpty()) return;

        List<Produk> results = produkDAO.searchProduk(keyword);

        if (results.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Produk tidak ditemukan", "Tidak ada produk yang cocok dengan \"" + keyword + "\".");
            return;
        }

        if (results.size() == 1) {
            addToCart(results.get(0), 1);
            searchField.clear();
        } else {
            ChoiceDialog<Produk> dialog = new ChoiceDialog<>(results.get(0), results);
            dialog.setTitle("Pilih Produk");
            dialog.setHeaderText("Beberapa produk cocok dengan pencarian Anda");
            dialog.setContentText("Produk:");

            Optional<Produk> selected = dialog.showAndWait();
            selected.ifPresent(produk -> {
                addToCart(produk, 1);
                searchField.clear();
            });
        }
    }

    private void addToCart(Produk produk, int qty) {
        if (produk.getStok() < qty) {
            showAlert(Alert.AlertType.WARNING, "Stok tidak cukup",
                    "Stok " + produk.getNamaProduk() + " hanya tersisa " + produk.getStok());
            return;
        }

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

        PenjualanDetail detail = new PenjualanDetail(
                produk.getIdProduk(),
                produk.getNamaProduk(),
                produk.getHarga(),
                qty
        );
        cartItems.add(detail);
        updateTotals();
    }

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

    @FXML
    private void handleScanBarcode() {
        searchField.requestFocus();
        searchField.clear();
    }

    @FXML
    private void handleCekMember() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cek Member");
        dialog.setHeaderText("Masukkan kode member");
        dialog.setContentText("Kode Member:");
        dialog.showAndWait();
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur cek member akan terhubung ke tabel member.");
    }

    @FXML
    private void handleCekHarga() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cek Harga");
        dialog.setHeaderText("Cari produk untuk melihat harga");
        dialog.setContentText("Nama/ID Produk:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(keyword -> {
            List<Produk> found = produkDAO.searchProduk(keyword.trim());
            if (found.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Tidak ditemukan", "Produk tidak ditemukan.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Produk p : found) {
                    sb.append(p.getNamaProduk()).append(" : Rp ")
                            .append(rupiahFormat.format(p.getHarga())).append("\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Harga Produk", sb.toString());
            }
        });
    }

    @FXML
    private void handlePending() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Keranjang kosong", "Tidak ada item untuk di-pending.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Pending", "Transaksi ditahan sementara (fitur penuh menyusul).");
    }

    @FXML
    private void handleCetakStruk() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Keranjang kosong", "Tidak ada transaksi untuk dicetak.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Cetak Struk", "Struk berhasil dikirim ke printer (simulasi).");
    }

    @FXML
    private void handleDiskon() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf((int) diskon));
        dialog.setTitle("Diskon / Promo");
        dialog.setHeaderText("Masukkan jumlah diskon (Rp)");
        dialog.setContentText("Diskon:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                double value = Double.parseDouble(input.trim());
                discountField.setText(String.valueOf(value));
                updateTotals();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input tidak valid", "Masukkan angka yang valid.");
            }
        });
    }

    private void updateTotals() {
        subtotal = cartItems.stream().mapToDouble(PenjualanDetail::getSubtotal).sum();

        try {
            diskon = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException e) {
            diskon = 0;
        }

        totalTagihan = Math.max(0, subtotal - diskon);

        subtotalLabel.setText(rupiahFormat.format(subtotal));
        totalTagihanLabel.setText(rupiahFormat.format(totalTagihan));
        grandTotalLabel.setText(rupiahFormat.format(totalTagihan));

        updateKembalian();
    }

    private void updateKembalian() {
        double bayar = parsePayField();
        double kembalian = bayar - totalTagihan;
    }

    private void setupPayField() {
        discountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
    }

    private double parsePayField() {
        try {
            return Double.parseDouble(payField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FXML
    private void handleNumpad(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        String digit = source.getText();

        if (digit.equals("×") || digit.equals("−")) {
            return;
        }

        String current = payField.getText();
        if (current.equals("0")) {
            current = "";
        }

        payField.setText(current + digit);
        updateKembalian();
    }

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

    @FXML
    private void handlePaymentMethod(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        selectedPaymentMethod = source.getText().replaceAll("[^a-zA-Z ]", "").trim();

        source.getParent().getChildrenUnmodifiable().forEach(node ->
                node.getStyleClass().remove("pay-method-active")
        );
        source.getStyleClass().add("pay-method-active");
    }

    @FXML
    private void handleBayar() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang kosong", "Tambahkan produk terlebih dahulu sebelum membayar.");
            return;
        }

        double bayar = parsePayField();

        if (bayar < totalTagihan) {
            showAlert(Alert.AlertType.WARNING, "Pembayaran kurang",
                    "Jumlah bayar (Rp " + rupiahFormat.format(bayar) +
                            ") kurang dari total tagihan (Rp " + rupiahFormat.format(totalTagihan) + ").");
            return;
        }

        double kembalian = bayar - totalTagihan;

        Penjualan penjualan = new Penjualan();
        penjualan.setNoTransaksi(penjualanDAO.generateNoTransaksi());
        penjualan.setIdMember(null);
        penjualan.setTotalBelanja(totalTagihan);
        penjualan.setBayar(bayar);
        penjualan.setKembalian(kembalian);

        int generatedId = penjualanDAO.saveTransaction(penjualan, cartItems);

        if (generatedId > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Pembayaran Berhasil",
                    "Transaksi " + penjualan.getNoTransaksi() + " berhasil.\n" +
                            "Kembalian: Rp " + rupiahFormat.format(kembalian));
            resetTransaction();
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Transaksi gagal disimpan. Silakan coba lagi.");
        }
    }

    private void resetTransaction() {
        cartItems.clear();
        discountField.setText("0");
        payField.setText("0");
        searchField.clear();
        updateTotals();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}