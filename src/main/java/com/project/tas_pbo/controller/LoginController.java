package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

import java.io.IOException;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    @FXML
    public void initialize() {
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

        System.out.println("Mencoba login untuk: " + user);

        if (user.equals("admin") && pass.equals("123")) {
            System.out.println("Login Berhasil!");
        } else {
            showAlert("Gagal", "Username atau Password salah!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            SceneController.switchTo("/com/project/tas_pbo/view/register-view.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("Gagal pindah ke halaman register");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}