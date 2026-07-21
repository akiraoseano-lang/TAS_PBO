package com.project.tas_pbo.service;

import com.project.tas_pbo.util.Session;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

// Service untuk verifikasi ulang password sebelum operasi sensitif
public class ReAuthService {

    // Meminta user memasukkan ulang password untuk verifikasi
    public static boolean requireReAuth() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Verifikasi Password");
        dialog.setHeaderText("Masukkan password Anda untuk melanjutkan");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password Anda");
        VBox content = new VBox(8, passwordField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return passwordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return false;

        String password = result.get().trim();
        var user = Session.getCurrentUser();
        if (user != null && user.getPassword().equals(password)) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Verifikasi Gagal");
        alert.setHeaderText(null);
        alert.setContentText("Password yang Anda masukkan salah.");
        alert.showAndWait();
        return false;
    }
}
