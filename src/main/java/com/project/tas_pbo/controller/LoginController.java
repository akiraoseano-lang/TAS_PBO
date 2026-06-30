package com.project.tas_pbo.controller;

import com.project.tas_pbo.DAO.UserDAO;
import com.project.tas_pbo.model.User;
import com.project.tas_pbo.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import javafx.event.ActionEvent;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label loginTextAlert;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        startClock();
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
        if (timeLabel != null) timeLabel.setText(now.format(TIME_FORMAT));
        if (dateLabel != null) dateLabel.setText(now.format(DATE_FORMAT));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Username dan Password tidak boleh kosong!", true);
            return;
        }

        System.out.println("Mencoba login untuk: " + username);

        User user = userDAO.login(username, password);

        if (user == null) {
            showAlert("Username atau Password salah!", true);
            return;
        }

        Session.setCurrentUser(user);
        System.out.println("Login berhasil: " + user.getNamaLengkap() + " (" + user.getRole() + ")");

        try {
            switch (user.getRole()) {
                case "Manager" ->
                        SceneController.switchTo("/com/project/tas_pbo/view/dashboard-manager-view.fxml", event);

                case "Kasir" ->
                        SceneController.switchTo("/com/project/tas_pbo/view/POS-view.fxml", event);

                case "Admin" ->
                        SceneController.switchTo("/com/project/tas_pbo/view/admin-dashboard-view.fxml", event);

                default -> showAlert("Role tidak dikenali: " + user.getRole(), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Gagal membuka halaman. Coba lagi.", true);
        }
    }

    private void showAlert(String message, boolean isError) {
        loginTextAlert.setText(message);
        loginTextAlert.setStyle(isError
                ? "-fx-text-fill: red;"
                : "-fx-text-fill: green;");
    }
}