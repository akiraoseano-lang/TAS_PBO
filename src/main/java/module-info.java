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

    opens com.project.tas_pbo to javafx.fxml;
    exports com.project.tas_pbo;
    exports com.project.tas_pbo.controller;
    opens com.project.tas_pbo.controller to javafx.fxml;
    exports com.project.tas_pbo.model;
    opens com.project.tas_pbo.model to javafx.fxml;
}