package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

public class RegisterController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtnoHp;
    @FXML private Label alertMsg;

    @FXML
    public void initialize() {
        // Tambahkan pengecekan null biar gak crash saat run
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    // WAJIB PAKAI @FXML BIAR FXML BISA NEMU METHOD INI
    @FXML
    public void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String password = txtPassword.getText();
        String noHp = txtnoHp.getText();

        if (user.isEmpty() || password.isEmpty() || noHp.isEmpty()) {
            alertMsg.setText("Username, password, no hp tidak boleh kosong!");
            alertMsg.setStyle("-fx-text-fill: red;");
        } else {
            System.out.println("Berhasil Register: " + user);
        }
    }
}