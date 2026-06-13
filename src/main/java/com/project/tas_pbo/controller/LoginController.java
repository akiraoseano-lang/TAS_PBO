package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

        if (timeLabel != null) {
            timeLabel.setText(now.format(TIME_FORMAT));
        }
        if (dateLabel != null) {
            dateLabel.setText(now.format(DATE_FORMAT));
        }
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

}