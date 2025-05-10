package olex.physiocareapifx.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import olex.physiocareapifx.utils.SceneLoader;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

/**
 * Controller for the main menu view.
 * Handles navigation to the patient and physio management views.
 */
public class MenuController {


    public MFXButton btnPatients;
    public MFXButton btnPhysios;
    public MFXButton btnAppointments;
    public MFXButton btnProfile;
    public ImageView imageView;

    /**
     * Initializes the menu view and sets up button actions.
     */
    @FXML
    public void initialize() {
        btnPatients.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("patient-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnPhysios.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("physio-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnAppointments.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("appointment-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
