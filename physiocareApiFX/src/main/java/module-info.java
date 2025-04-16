module olex.physiocareapifx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens olex.physiocareapifx to javafx.fxml;
    opens olex.physiocareapifx.controller to javafx.fxml;
    opens olex.physiocareapifx.model to com.google.gson;

    exports olex.physiocareapifx;
    exports olex.physiocareapifx.controller;
    exports olex.physiocareapifx.model;
    exports olex.physiocareapifx.services;
    exports olex.physiocareapifx.utils;
}
