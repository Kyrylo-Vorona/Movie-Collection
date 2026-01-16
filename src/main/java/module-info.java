module dk.easv.moviecollection {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;
    requires java.desktop;
    requires java.naming;
    requires com.microsoft.sqlserver.jdbc;
    requires javafx.base;

    opens dk.easv.moviecollection.gui to javafx.fxml;
    opens dk.easv.moviecollection.be to javafx.base;
    exports dk.easv.moviecollection.gui;
}

