package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import olex.physiocareapifx.model.PhysioResponse;
import olex.physiocareapifx.utils.*;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static olex.physiocareapifx.utils.Utils.isPhysio;

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
    public ImageView imageAvatar;
    public Text lblNombre;
    Gson gson = new Gson();

    /**
     * Initializes the menu view and sets up button actions.
     */
    @FXML
    public void initialize() throws IOException {
        if(!isPhysio){
            btnProfile.setDisable(true);
            lblNombre.setText("Admin");
            imageAvatar.setImage(null);
        }else{
            btnProfile.setDisable(false);
           loadPhysioData();
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
                System.out.println("Physio ID: " + Utils.userId);
                if(isPhysio){
                    Utils.userPhysio = Utils.userId;
                    SceneLoader.loadScreen("physio-detail-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
                }else{
                    SceneLoader.loadScreen("physio-view.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());

                }
            } catch (IOException e) {
                e.printStackTrace();
                MessageUtils.showError("Error", "Failed to load physio data"+e.getMessage());
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

    public void loadPhysioData() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios/" + Utils.userId, null, "GET");
                PhysioResponse response = gson.fromJson(json, PhysioResponse.class);
                if (response.isOk()) {
                    Platform.runLater(() -> {
                        lblNombre.setText(response.getPhysio().getName());
                        if(response.getPhysio().getAvatar() != null){
                            imageAvatar.setImage(new Image(response.getPhysio().getAvatar(),200, 0,true, true, true));
                        }

                    });
                } else {
                    Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physio data"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physio data"));
            }
        }).start();
    }

}
