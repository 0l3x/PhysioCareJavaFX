package olex.physiocareapifx.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.function.Consumer;

public class Utils {
    public static boolean isPhysio = false;
    public static String userId = "";
    public static String userPhysio="";

    public static String encodeImageToBase64(String imagePath) throws IOException {
        // 1. Lee toda la imagen en un array de bytes
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        // 2. Codifica ese array a Base64 y devuelve el resultado
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static <T> TableColumn<T, Void> createDeleteColumn(ObservableList<T> items, Consumer<T> deleteAction) {
        TableColumn<T, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory((Callback<TableColumn<T, Void>, TableCell<T, Void>>) column ->
                new TableCell<>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setOnAction(e -> {
                            T item = getTableView().getItems().get(getIndex());
                            System.out.println("Item to delete: " + item);
                            deleteAction.accept(item);
                        });
                    }
                    @Override protected void updateItem(Void v, boolean empty) {
                        super.updateItem(v, empty);
                        setGraphic(empty ? null : btn);
                    }
                }
        );
        return colAcciones;
    }

}
