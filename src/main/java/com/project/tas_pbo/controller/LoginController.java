package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    @FXML
    public void initialize() {
        // Agar gambar background responsif mengikuti ukuran window
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Isi dulu semua field-nya njir!");
            return;
        }

        // TODO: Koneksikan ke DAO kamu di sini
        System.out.println("Mencoba login untuk: " + user);

        // Contoh logika sederhana
        if (user.equals("admin") && pass.equals("123")) {
            System.out.println("Login Berhasil!");
            // Lanjut pindah halaman...
        } else {
            showAlert("Gagal", "Username atau Password salah!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        System.out.println("Pindah ke halaman Register...");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}