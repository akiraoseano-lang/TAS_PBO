package com.project.tas_pbo.controller;

import com.project.tas_pbo.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Selamat datang, " + Session.getCurrentUsername() + "!");
        }
    }
}