package olex.physiocareapifx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.kordamp.bootstrapfx.BootstrapFX;

/**
 * Controller for the main menu view.
 * Handles navigation to the patient and physio management views.
 */
public class MenuController {

    @FXML private Button btnPatients;
    @FXML private Button btnPhysios;

    /**
     * Initializes the menu view and sets up button actions.
     */
    @FXML
    public void initialize() {
        btnPatients.getStyleClass().setAll("btn","btn-danger");
        btnPatients.setOnAction(e -> openView("patient-view.fxml"));
        btnPhysios.setOnAction(e -> openView("physio-view.fxml"));
    }

    /**
     * Loads the specified FXML view and replaces the current scene.
     *
     * @param fxml The name of the FXML file to load (e.g., "patient-view.fxml").
     */
    private void openView(String fxml) {
        try {
            Stage stage = (Stage) btnPatients.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/olex/physiocareapifx/" + fxml));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
