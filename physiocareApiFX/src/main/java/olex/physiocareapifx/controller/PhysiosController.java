package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Physio;
import olex.physiocareapifx.model.PhysioResponse;
import olex.physiocareapifx.services.PhysioService;
import olex.physiocareapifx.services.PhysioService.Method;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.ServiceUtils;

import java.io.IOException;

/**
 * Controller for managing physiotherapists in the application.
 * Provides CRUD operations and communicates with the REST API.
 */
public class PhysiosController {

    @FXML private TextField nameField;
    @FXML private TextField surnameField;
//    @FXML private TextField specialtyField;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private TextField licenseNumberField;
    @FXML private TextField emailField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button exitBtn;

    @FXML private TableView<Physio> tableViewPhysio;
    @FXML private TableColumn<Physio, String> colName;
    @FXML private TableColumn<Physio, String> colSurname;
    @FXML private TableColumn<Physio, String> colSpecialty;
    @FXML private TableColumn<Physio, String> colLicenseNumber;
    @FXML private TableColumn<Physio, String> colEmail;

    private final Gson gson = new Gson();

    /**
     * Initializes the view, sets up table columns and event listeners.
     */
    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colLicenseNumber.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        specialtyCombo.getItems().addAll("Sports", "Neurological", "Pediatric", "Geriatric", "Oncological");

        loadPhysios();

        addBtn.setOnAction(e -> addPhysio());
        editBtn.setOnAction(e -> updatePhysio());
        deleteBtn.setOnAction(e -> deletePhysio());
        exitBtn.setOnAction(e -> goBackToMenu());

        tableViewPhysio.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillFieldsFromPhysio(newVal);
        });
    }

    /**
     * Loads all physiotherapists from the API and updates the TableView.
     */
    private void loadPhysios() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios", null, "GET");
                PhysioResponse response = gson.fromJson(json, PhysioResponse.class);
                if (response.isOk()) {
                    javafx.application.Platform.runLater(() ->
                            tableViewPhysio.getItems().setAll(response.getResultado())
                    );
                } else {
                    MessageUtils.showError("Error", "No se pudo cargar la lista de fisioterapeutas");
                }
            } catch (Exception e) {
                MessageUtils.showError("Error", e.getMessage());
            }
        }).start();
    }

    /**
     * Sends a new physio to the API.
     */
    private void addPhysio() {
        Physio physio = getPhysioFromFields();
        if (physio == null) return;
        PhysioService service = new PhysioService();
        service.configureFor(Method.POST, physio);
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPhysios();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al conectar con el servidor."));
    }

    /**
     * Updates the selected physio with form data.
     */
    private void updatePhysio() {
        Physio selected = tableViewPhysio.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageUtils.showError("Error", "Selecciona un fisio para editar.");
            return;
        }

        Physio updated = getPhysioFromFields();
        updated.setId(selected.getId());

        if (updated.equals(selected)) {
            MessageUtils.showMessage("Aviso", "No se han hecho cambios.");
            return;
        }

        PhysioService service = new PhysioService();
        service.configureFor(Method.PUT, updated);
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPhysios();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al actualizar el fisio."));
    }

    /**
     * Deletes the selected physio from the database.
     */
    private void deletePhysio() {
        Physio selected = tableViewPhysio.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageUtils.showError("Error", "Selecciona un fisio para eliminar.");
            return;
        }

        PhysioService service = new PhysioService();
        service.configureForDelete(selected.getId());
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPhysios();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al eliminar el fisio."));
    }

    /**
     * Reads and validates form inputs and builds a Physio object.
     * @return A valid Physio instance or null if validation fails.
     */
    private Physio getPhysioFromFields() {
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String specialty = specialtyCombo.getValue();
        String license = licenseNumberField.getText().trim();
        String email = emailField.getText() != null ? emailField.getText().trim() : "";

        if (name.isEmpty() || name.length() < 2 || name.length() > 50) {
            MessageUtils.showError("Error", "El nombre debe tener entre 2 y 50 caracteres.");
            return null;
        }

        if (surname.isEmpty() || surname.length() < 2 || surname.length() > 50) {
            MessageUtils.showError("Error", "El apellido debe tener entre 2 y 50 caracteres.");
            return null;
        }

        if (!specialty.matches("(?i)Sports|Neurological|Pediatric|Geriatric|Oncological")) {
            MessageUtils.showError("Error", "Especialidad no válida. Usa Sports, Neurological, Pediatric, Geriatric u Oncological.");
            return null;
        }

        if (!license.matches("^[a-zA-Z0-9]{8}$")) {
            MessageUtils.showError("Error", "El número de licencia debe tener exactamente 8 caracteres alfanuméricos.");
            return null;
        }

        return new Physio(null, name, surname, specialty, license, email);
    }

    /**
     * Populates form fields with the selected physio data.
     * @param p The selected Physio object.
     */
    private void fillFieldsFromPhysio(Physio p) {
        nameField.setText(p.getName());
        surnameField.setText(p.getSurname());
        specialtyCombo.setValue(p.getSpecialty());
        licenseNumberField.setText(p.getLicenseNumber());
        emailField.setText(p.getEmail());
    }

    /**
     * Clears form fields and selection in the table.
     */
    private void clearFields() {
        nameField.clear();
        surnameField.clear();
        specialtyCombo.setValue(null);
        licenseNumberField.clear();
        emailField.clear();
        tableViewPhysio.getSelectionModel().clearSelection();
    }

    /**
     * Navigates back to the main menu.
     */
    private void goBackToMenu() {
        try {
            Stage stage = (Stage) exitBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/olex/physiocareapifx/menu.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            MessageUtils.showError("Error", "No se pudo volver al menú.");
        }
    }
}
