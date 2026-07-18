module com.project.tas_pbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.net.http;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires itext;

    opens com.project.tas_pbo to javafx.fxml;
    exports com.project.tas_pbo;
    exports com.project.tas_pbo.controller;
    opens com.project.tas_pbo.controller to javafx.fxml;
    exports com.project.tas_pbo.model;
    opens com.project.tas_pbo.model to javafx.fxml;
    exports com.project.tas_pbo.util;
    opens com.project.tas_pbo.util to javafx.fxml;
    exports com.project.tas_pbo.service;
    opens com.project.tas_pbo.service to javafx.fxml;
}