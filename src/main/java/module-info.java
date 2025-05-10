module olex.physiocareapifx {
    requires com.google.gson;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.jshell;
    requires MaterialFX;
    requires kernel;
    requires layout;
    requires html2pdf;

    opens olex.physiocareapifx to javafx.fxml;
    opens olex.physiocareapifx.controller to javafx.fxml;
    opens olex.physiocareapifx.model to com.google.gson;

    exports olex.physiocareapifx;
    exports olex.physiocareapifx.controller;
    exports olex.physiocareapifx.model;
    exports olex.physiocareapifx.services;
    exports olex.physiocareapifx.utils;
    exports olex.physiocareapifx.utils.pdf;
}
