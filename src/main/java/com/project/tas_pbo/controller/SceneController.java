package com.project.tas_pbo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Controller untuk berpindah scene/halaman
public class SceneController {
    // Berpindah ke halaman FXML yang ditentukan
    public static void switchTo(String fxmlPath, ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Parent root = FXMLLoader.load(SceneController.class.getResource(fxmlPath));

        boolean wasFullScreen = stage.isFullScreen();

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }

        if (wasFullScreen) {
            stage.setFullScreen(true);
        }
    }
}
