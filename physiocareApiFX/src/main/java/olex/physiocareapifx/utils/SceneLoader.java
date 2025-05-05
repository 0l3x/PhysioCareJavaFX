package olex.physiocareapifx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import olex.physiocareapifx.PhysioCareApp;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

public class SceneLoader {
    public static void loadScreen(String viewPath, Stage stage)
            throws IOException
    {
        Parent root = FXMLLoader.load(PhysioCareApp.class.getResource(viewPath));
        Scene viewScene = new Scene(root);
        viewScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(viewScene);
        stage.show();
    }
    public static void loadScreenMOdality(String viewPath, Stage stage)
            throws IOException
    {
        Parent root = FXMLLoader.load(PhysioCareApp.class.getResource(viewPath));
        Scene viewScene = new Scene(root);
        viewScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(viewScene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(stage);
        stage.show();
    }
}