package com.project.tas_pbo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneController {
    public static void switchTo(String fxmlPath, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(SceneController.class.getResource(fxmlPath));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
