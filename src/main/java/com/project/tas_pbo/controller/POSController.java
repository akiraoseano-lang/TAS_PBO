package com.project.tas_pbo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import javafx.event.ActionEvent;


public class POSController {


    @FXML private Label dateLabel;
    @FXML private Label timeLabel;

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

}
