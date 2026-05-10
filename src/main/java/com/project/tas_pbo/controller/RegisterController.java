package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import java.io.IOException;

public class RegisterController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtnoHp;
    @FXML private Label alertMsg;

    @FXML
    public void initialize() {

        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }


    @FXML
    public void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String password = txtPassword.getText();
        String noHp = txtnoHp.getText();

        if (user.isEmpty() || password.isEmpty() || noHp.isEmpty()) {
            alertMsg.setText("Username, password, no hp tidak boleh kosong!");
            alertMsg.setStyle("-fx-text-fill: red;");
        } else if (password.length() < 8) {
            alertMsg.setText("Password harus lebih dari 8 karakter");
            alertMsg.setStyle("-fx-text-fill: red;");
        } else {
            alertMsg.setText("Register berhasil silahkan login");
            alertMsg.setStyle("-fx-text-fill: light-green;");
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            SceneController.switchTo("/com/project/tas_pbo/view/login-view.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}