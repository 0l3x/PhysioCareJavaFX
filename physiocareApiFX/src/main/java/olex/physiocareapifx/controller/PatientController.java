package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import olex.physiocareapifx.model.Patient;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.PatientResponse;
import olex.physiocareapifx.services.PatientService;
import olex.physiocareapifx.services.PatientService.Method;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.ServiceUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the patient management view.
 * Enables CRUD operations on patients via REST API communication.
 */
public class PatientController {

    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField birthdateField;
    @FXML private DatePicker birthdatePicker;
    @FXML private TextField addressField;
    @FXML private TextField insuranceNumberField;
    @FXML private TextField emailField;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button exitBtn;

    @FXML private TableView<Patient> tableViewPatient;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colSurname;
    @FXML private TableColumn<Patient, String> colBirthdate;
    @FXML private TableColumn<Patient, String> colAddress;
    @FXML private TableColumn<Patient, String> colInsurance;
    @FXML private TableColumn<Patient, String> colEmail;

    private final Gson gson = new Gson();

    /**
     * Initializes the view and configures column bindings and button actions.
     */
    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colBirthdate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colInsurance.setCellValueFactory(new PropertyValueFactory<>("insuranceNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadPatients();

        addBtn.setOnAction(e -> addPatient());
        editBtn.setOnAction(e -> updatePatient());
        deleteBtn.setOnAction(e -> deletePatient());
        exitBtn.setOnAction(e -> goBackToMenu());

        tableViewPatient.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillFieldsFromPatient(newVal);
        });
    }

    /**
     * Loads the list of patients from the API.
     */
    private void loadPatients() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/patients", null, "GET");
                PatientResponse response = gson.fromJson(json, PatientResponse.class);
                if (response.isOk()) {
                    javafx.application.Platform.runLater(() ->
                            tableViewPatient.getItems().setAll(response.getResultado())
                    );
                } else {
                    MessageUtils.showError("Error", "No se pudo cargar la lista de pacientes");
                }
            } catch (Exception e) {
                MessageUtils.showError("Error", e.getMessage());
            }
        }).start();
    }

    /**
     * Sends a new patient to the API.
     */
    private void addPatient() {
        Patient patient = getPatientFromFields();
        if (patient == null) return;
        PatientService service = new PatientService();
        service.configureFor(Method.POST, patient);
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPatients();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al conectar con el servidor."));
    }

    /**
     * Updates a selected patient with the form data.
     */
    private void updatePatient() {
        Patient selected = tableViewPatient.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageUtils.showError("Error", "Selecciona un paciente para editar.");
            return;
        }

        Patient updated = getPatientFromFields();
        updated.setId(selected.getId());

        if (updated.equals(selected)) {
            MessageUtils.showMessage("Aviso", "No se han hecho cambios.");
            return;
        }

        PatientService service = new PatientService();
        service.configureFor(Method.PUT, updated);
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPatients();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al actualizar el paciente."));
    }

    /**
     * Deletes the selected patient.
     */
    private void deletePatient() {
        Patient selected = tableViewPatient.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageUtils.showError("Error", "Selecciona un paciente para eliminar.");
            return;
        }

        PatientService service = new PatientService();
        service.configureForDelete(selected.getId());
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                loadPatients();
                clearFields();
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al eliminar el paciente."));
    }

    /**
     * Validates form data and constructs a Patient object.
     * @return A Patient object if valid, null otherwise.
     */
    private Patient getPatientFromFields() {
        if (nameField == null || surnameField == null || addressField == null || insuranceNumberField == null || emailField == null) {
            MessageUtils.showError("Error", "Hay campos no inicializados. Verifica el FXML.");
            return null;
        }

        String name = nameField != null ? nameField.getText().trim() : "";
        String surname = surnameField != null ? surnameField.getText().trim() : "";
//        String birthDate = birthdateField != null ? birthdateField.getText().trim() : "";
        LocalDate birthDate = birthdatePicker.getValue();
        String address = addressField != null ? addressField.getText().trim() : "";
        String insurance = insuranceNumberField != null ? insuranceNumberField.getText().trim() : "";
        String email = emailField != null && emailField.getText() != null ? emailField.getText().trim() : "";

        // Validaciones
        if (name.isEmpty() || name.length() < 2 || name.length() > 50) {
            MessageUtils.showError("Error", "El nombre debe tener entre 2 y 50 caracteres.");
            return null;
        }

        if (surname.isEmpty() || surname.length() < 2 || surname.length() > 50) {
            MessageUtils.showError("Error", "El apellido debe tener entre 2 y 50 caracteres.");
            return null;
        }

//        if (birthDate.isEmpty()) {
//            MessageUtils.showError("Error", "La fecha de nacimiento es obligatoria.");
//            return null;
//        }

        if (birthDate == null) {
            MessageUtils.showError("Error", "La fecha de nacimiento es obligatoria.");
            return null;
        }

        if (address.isEmpty() || address.length() > 100) {
            MessageUtils.showError("Error", "La dirección no puede estar vacía ni superar los 100 caracteres.");
            return null;
        }

        if (!insurance.matches("^[a-zA-Z0-9]{9}$")) {
            MessageUtils.showError("Error", "El número de seguro debe tener exactamente 9 caracteres alfanuméricos.");
            return null;
        }

        //System.out.println("¿emailField es null?: " + (emailField == null));

//        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
//            MessageUtils.showError("Error", "El email no tiene un formato válido.");
//            return null;
//        }

        String birthDateStr = birthDate.toString(); // Formato: "2001-03-15"

        return new Patient(null, name, surname, birthDateStr, address, insurance, email);
    }

    /**
     * Fills form fields from the selected patient.
     * @param p The selected patient.
     */
    private void fillFieldsFromPatient(Patient p) {
        nameField.setText(p.getName());
        surnameField.setText(p.getSurname());
//        birthdateField.setText(p.getBirthDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDate parsedDate = LocalDate.parse(p.getBirthDate(), formatter);
        birthdatePicker.setValue(parsedDate);

        addressField.setText(p.getAddress());
        insuranceNumberField.setText(p.getInsuranceNumber());
        emailField.setText(p.getEmail());
    }

    /**
     * Clears all form fields and deselects the table.
     */
    private void clearFields() {
        nameField.clear();
        surnameField.clear();
//        birthdateField.clear();
        birthdatePicker.setValue(null);
        addressField.clear();
        insuranceNumberField.clear();
        emailField.clear();
        tableViewPatient.getSelectionModel().clearSelection();
    }

    /**
     * Navigates back to the main menu view.
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
