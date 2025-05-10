package olex.physiocareapifx.controller;

import com.google.gson.Gson;
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
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.TokenManager;
import olex.physiocareapifx.utils.Utils;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
    public MFXButton btnLogout;
    Gson gson = new Gson();

    /**
     * Initializes the menu view and sets up button actions.
     */
    @FXML
    public void initialize() throws IOException {
        if(!Utils.isPhysio){
            btnProfile.setVisible(false);
        }else{
            btnProfile.setVisible(true);
        }
        // Load image from resources, first convert it to base64 an then to set it as image
        byte[] imageBytes = Files.readAllBytes(Paths.get("resources/logo.png"));

        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:image/png;base64," + base64;
        Image logo = new Image(dataUri);
        imageView.setImage(logo);

        btnLogout.setOnAction(actionEvent -> {
            try {
                TokenManager.setToken(null);
                SceneLoader.loadScreen("login-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        btnProfile.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("myprofile-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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
