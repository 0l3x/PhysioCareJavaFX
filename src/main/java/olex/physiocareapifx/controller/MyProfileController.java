package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import olex.physiocareapifx.model.PhysioListResponse;
import olex.physiocareapifx.model.PhysioResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MyProfileController implements Initializable {

    public Text lblNombre;
    public Text lblApellidos;
    public Text lblLicense;
    public ImageView imageView;
    public MFXButton btnVolver;
    public Text txtNoAvatar;
    public Text lblEmail;
    Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadPhysioData();
        btnVolver.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("menu.fxml", (Stage) btnVolver.getScene().getWindow());
            } catch (Exception e) {
                MessageUtils.showError("Error", "Failed to load menu");
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
                        lblNombre.setText("Nombre: " + response.getPhysio().getName());
                        lblApellidos.setText("Apellidos: " + response.getPhysio().getSurname());
                        lblLicense.setText("License Number: " + response.getPhysio().getLicenseNumber());
                        lblEmail.setText("Email: " + response.getPhysio().getEmail());
                        if (response.getPhysio().getAvatar() != null){
                            imageView.setImage(new Image(response.getPhysio().getAvatar(),200, 0,true, true, true));
                            txtNoAvatar.setVisible(false);
                        }else{
                            imageView.setImage(null);
                            txtNoAvatar.setVisible(true);
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
