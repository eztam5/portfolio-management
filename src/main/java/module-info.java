module com.example.portfoliomanagement {
    requires java.naming;
    requires java.net.http;
    requires java.sql;

    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires liquibase.core;
    requires org.hibernate.orm.core;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.dlsc.fxmlkit;

    opens com.example.portfoliomanagement.ui to javafx.fxml;
    opens com.example.portfoliomanagement.persistence to org.hibernate.orm.core;
    exports com.example.portfoliomanagement;
}
