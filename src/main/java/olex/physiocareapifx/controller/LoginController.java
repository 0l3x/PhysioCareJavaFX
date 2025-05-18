package olex.physiocareapifx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import olex.physiocareapifx.model.User.LoginRequest;
import olex.physiocareapifx.utils.*;
import com.google.gson.Gson;
import olex.physiocareapifx.model.User.AuthResponse;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the login view.
 * Manages user authentication against the backend API and transitions to the main menu on success.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button loginBtn;

    /**
     * Initializes the login view and sets the button action.
     */
    @FXML
    public void initialize() {
        loginBtn.setOnAction(this::login);
    }

    /**
     * Handles the login process: sends credentials to the backend and processes the response.
     * On success, stores the token and loads the menu view.
     */
    private void login(ActionEvent actionEvent) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        try {
            String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/auth/login",
                    new Gson().toJson(new LoginRequest(user, pass)), "POST");

            AuthResponse response = new Gson().fromJson(json, AuthResponse.class);
            if (response.isOk()) {
                System.out.println("Login Successful");
                System.out.println("Token: " + response.getToken());
                TokenManager.setToken(response.getToken());

                if(Objects.equals(response.getRol(), "physio")){
                    Utils.isPhysio = true;
                    Utils.userId = response.getId();
                }else{
                    Utils.isPhysio = false;
                }
                System.out.println("Id del Usuario: " + Utils.userId);
                System.out.println("Puede ver perfil: " + Utils.isPhysio);
                SceneLoader.loadScreen("menu.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
                //loadMenuView();
            } else {
                MessageUtils.showError("Login failed", "Incorrect username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.showError("Connection Error", e.getMessage());
        }
    }

    /**
     * Loads the main menu view after successful login.
     *
     * @throws IOException if the FXML file for the menu cannot be loaded.
     */
    private void loadMenuView() throws IOException {
        Stage stage = (Stage) loginBtn.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/olex/physiocareapifx/menu.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(scene);
    }
}
