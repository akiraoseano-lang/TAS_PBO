package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.MemberDAO;
import com.project.tas_pbo.DAO.PenjualanDAO;
import com.project.tas_pbo.DAO.ProdukDAO;
import com.project.tas_pbo.model.Member;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;
import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.service.DiscountService;
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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class POSController {

    @FXML private BorderPane rootPane;
    @FXML private Label kasirLabel;
    @FXML private Label memberLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

    @FXML private TableView<Produk> produkTable;
    @FXML private TableColumn<Produk, Integer> colProdukNo;
    @FXML private TableColumn<Produk, String> colProdukBarcode;
    @FXML private TableColumn<Produk, String> colProdukNama;
    @FXML private TableColumn<Produk, String> colProdukHarga;
    @FXML private TableColumn<Produk, Integer> colProdukStok;
    @FXML private TableColumn<Produk, String> colProdukSatuan;
    @FXML private TableColumn<Produk, Void> colProdukAksi;
    @FXML private TextField searchField;

    @FXML private TableView<PenjualanDetail> cartTable;
    @FXML private TableColumn<PenjualanDetail, Integer> colNo;
    @FXML private TableColumn<PenjualanDetail, String> colBarcode;
    @FXML private TableColumn<PenjualanDetail, String> colNama;
    @FXML private TableColumn<PenjualanDetail, Integer> colQty;
    @FXML private TableColumn<PenjualanDetail, String> colSatuan;
    @FXML private TableColumn<PenjualanDetail, String> colHarga;
    @FXML private TableColumn<PenjualanDetail, String> colTotal;

    @FXML private Label grandTotalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label potonganLabel;
    @FXML private Label totalTagihanLabel;

    @FXML private TextField payField;
    @FXML private Button bayarButton;

    private final ObservableList<PenjualanDetail> cartItems = FXCollections.observableArrayList();
    private final ObservableList<Produk> produkItems = FXCollections.observableArrayList();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final DiscountService discountService = new DiscountService();
    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private double subtotal = 0;
    private double totalTagihan = 0;
    private Member currentMember = null;

    private Penjualan lastPenjualan = null;
    private List<PenjualanDetail> lastCartItems = null;

    @FXML
    public void initialize() {
        setupProdukTable();
        setupCartTable();
        startClock();

        kasirLabel.setText("Kasir: " + Session.getCurrentUsername());
        memberLabel.setText("Member: -");
        payField.setText("0");

        loadProdukData();
        updateTotals();

        Platform.runLater(() -> searchField.requestFocus());
    }

    private void setupProdukTable() {
        colProdukNo.setCellValueFactory(cd -> new SimpleIntegerProperty(
                produkTable.getItems().indexOf(cd.getValue()) + 1).asObject());
        colProdukBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProdukNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colProdukHarga.setCellValueFactory(cd -> new SimpleStringProperty(
                "Rp " + rupiahFormat.format(cd.getValue().getHarga())));
        colProdukStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colProdukSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));

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

    private void loadProdukData() {
        Task<List<Produk>> task = new Task<>() {
            @Override protected List<Produk> call() {
                return produkDAO.getAllProduk();
            }
        };
        task.setOnSucceeded(e -> produkItems.setAll(task.getValue()));
        new Thread(task).start();
    }

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

    @FXML
    private void handleResetSearch() {
        searchField.clear();
        loadProdukData();
        searchField.requestFocus();
    }

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
                produk.getBarcode(),
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
    private void handleReset() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Kosong", "Keranjang sudah kosong.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Transaksi");
        confirm.setHeaderText("Yakin ingin mereset transaksi?");
        confirm.setContentText("Semua item di keranjang akan dihapus dan member akan di-reset.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            resetTransaction();
        }
    }

    @FXML
    private void handleCekMember() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cek Member");
        dialog.setHeaderText("Masukkan Kode Member");
        dialog.setContentText("Kode : ");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(kode -> {
            Member member = memberDAO.getByKode(kode.trim());
            if (member == null) {
                currentMember = null;
                memberLabel.setText("Member: -");
                showAlert(Alert.AlertType.ERROR, "Gagal", "Member tidak ditemukan");
            } else {
                currentMember = member;
                memberLabel.setText("Member: " + member.getNamaMember());
                updateTotals();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "Member: " + member.getNamaMember() + "\nPoin: " + member.getPoin());
            }
        });
    }

    @FXML
    private void handleCekHarga() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cek Harga");
        dialog.setHeaderText("Scan barcode atau masukkan nama produk");
        dialog.setContentText("Cari:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(keyword -> {
            List<Produk> found = produkDAO.searchProduk(keyword.trim());
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

    @FXML
    private void handlePending() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Keranjang kosong", "Tidak ada item untuk di-pending.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Pending", "Transaksi ditahan sementara (fitur penuh menyusul).");
    }

    @FXML
    private void handleDiskon() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Diskon / Promo");
        dialog.setHeaderText("Masukkan diskon manual (%)");
        dialog.setContentText("Diskon %:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                double pct = Double.parseDouble(input.trim());
                if (pct < 0 || pct > 100) {
                    showAlert(Alert.AlertType.ERROR, "Tidak valid", "Diskon harus antara 0-100%");
                    return;
                }
                showAlert(Alert.AlertType.INFORMATION, "Diskon",
                        "Diskon " + pct + "% diterapkan (fitur manual diskon akan ditambahkan).");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input tidak valid", "Masukkan angka yang valid.");
            }
        });
    }

    private void updateTotals() {
        subtotal = cartItems.stream().mapToDouble(PenjualanDetail::getSubtotal).sum();

        double discountRate = discountService.getDiscountRate(currentMember, subtotal);
        double potongan = discountService.getDiscountAmount(currentMember, subtotal);
        totalTagihan = subtotal - potongan;

        subtotalLabel.setText(rupiahFormat.format(subtotal));
        discountLabel.setText((int)(discountRate * 100) + "%");
        potonganLabel.setText("Rp " + rupiahFormat.format(potongan));
        totalTagihanLabel.setText(rupiahFormat.format(totalTagihan));
        grandTotalLabel.setText(rupiahFormat.format(totalTagihan));

        updateKembalian();
    }

    private void updateKembalian() {

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
        if (digit.equals("×") || digit.equals("−")) return;
        String current = payField.getText();
        if (current.equals("0")) current = "";
        payField.setText(current + digit);
        updateKembalian();
    }

    @FXML
    private void handleClearPay() {
        payField.setText("0");
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
                node.getStyleClass().remove("pay-method-active"));
        source.getStyleClass().add("pay-method-active");
    }

    @FXML
    private void handleBayar() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang kosong",
                    "Tambahkan produk terlebih dahulu sebelum membayar.");
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
        double potongan = discountService.getDiscountAmount(currentMember, subtotal);
        double discountRate = discountService.getDiscountRate(currentMember, subtotal);

        Penjualan penjualan = new Penjualan();
        penjualan.setNoTransaksi(penjualanDAO.generateNoTransaksi());
        penjualan.setIdMember(currentMember != null ? currentMember.getIdMember() : null);
        penjualan.setIdUser(Session.getCurrentUserId());
        penjualan.setTotalBelanja(totalTagihan);
        penjualan.setBayar(bayar);
        penjualan.setKembalian(kembalian);

        lastCartItems = List.copyOf(cartItems);
        lastPenjualan = penjualan;

        int generatedId = penjualanDAO.saveTransaction(penjualan, cartItems);

        if (generatedId > 0) {
            String receipt = ReceiptPrinter.generateReceipt(
                    penjualan, lastCartItems, currentMember, discountRate, potongan);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Pembayaran Berhasil");
            successAlert.setHeaderText("Transaksi " + penjualan.getNoTransaksi() + " berhasil!");
            successAlert.setContentText("Kembalian: Rp " + rupiahFormat.format(kembalian) +
                    "\n\nKlik OK untuk melihat struk, atau tutup untuk lanjut.");
            successAlert.showAndWait();

            ReceiptPrinter.showReceiptDialog(
                    penjualan, lastCartItems, currentMember, discountRate, potongan);
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Transaksi gagal disimpan. Silakan coba lagi.");
        }
    }

    @FXML
    private void handleCetakStruk() {
        if (lastPenjualan != null && lastCartItems != null) {
            double potongan = discountService.getDiscountAmount(currentMember, lastPenjualan.getTotalBelanja());
            double discountRate = discountService.getDiscountRate(currentMember, lastPenjualan.getTotalBelanja());
            ReceiptPrinter.printToPrinter(lastPenjualan, lastCartItems, currentMember, discountRate, potongan);
        } else if (!cartItems.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Info",
                    "Struk hanya tersedia setelah transaksi selesai. Klik BAYAR dulu.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Tidak ada struk",
                    "Tidak ada transaksi yang bisa dicetak.");
        }
    }

    private void resetTransaction() {
        resetTransactionInternal();
    }

    private void resetTransactionInternal() {
        cartItems.clear();
        payField.setText("0");
        searchField.clear();

        currentMember = null;
        memberLabel.setText("Member: -");

        updateTotals();
        Platform.runLater(() -> searchField.requestFocus());
    }

    @FXML
    public void startClock() {
        updateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    public void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        if (timeLabel != null) timeLabel.setText(now.format(TIME_FORMAT));
        if (dateLabel != null) dateLabel.setText(now.format(DATE_FORMAT));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}