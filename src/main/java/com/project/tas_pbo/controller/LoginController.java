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
    @FXML private Label loginTextAlert;

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
            loginTextAlert.setText("Username dan Password tidak boleh kosong!");
            loginTextAlert.setStyle("-fx-text-fill: red");
        }

        System.out.println("Mencoba login untuk: " + user);

        if (user.equals("Manager") && pass.equals("12345678")) {
            try {
                SceneController.switchTo("/com/project/tas_pbo/view/dashboard-manager-view.fxml", event);
            } catch (IOException e) {
                e.printStackTrace();

                System.out.println("Gagal login sebagai manager");
            }
        } else {
            loginTextAlert.setText("Gagal Username atau Password salah!");
            loginTextAlert.setStyle("-fx-text-fill: red");
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


}