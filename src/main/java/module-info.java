module olex.physiocareapifx {
    requires com.google.gson;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.jshell;
    requires MaterialFX;
    requires kernel;
    requires layout;
    requires jsch;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.services.gmail;
    requires com.google.api.client.extensions.java6.auth;
    requires jakarta.mail;
    requires com.google.api.client.extensions.jetty.auth;
    requires jdk.httpserver;
    requires java.dotenv;
    requires io;

    opens olex.physiocareapifx to javafx.fxml;
    opens olex.physiocareapifx.controller to javafx.fxml;
    opens olex.physiocareapifx.model to com.google.gson;

    exports olex.physiocareapifx;
    exports olex.physiocareapifx.controller;
    exports olex.physiocareapifx.model;
    exports olex.physiocareapifx.services;
    exports olex.physiocareapifx.utils;
    exports olex.physiocareapifx.utils.pdf;
    exports olex.physiocareapifx.model.Appointments;
    opens olex.physiocareapifx.model.Appointments to com.google.gson;
    exports olex.physiocareapifx.model.Patients;
    opens olex.physiocareapifx.model.Patients to com.google.gson;
    exports olex.physiocareapifx.model.Physios;
    opens olex.physiocareapifx.model.Physios to com.google.gson;
    exports olex.physiocareapifx.model.Records;
    opens olex.physiocareapifx.model.Records to com.google.gson;
    exports olex.physiocareapifx.model.User;
    opens olex.physiocareapifx.model.User to com.google.gson;
}
