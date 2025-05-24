package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Patients.PatientListResponse;
import olex.physiocareapifx.model.Patients.PatientResponse;
import olex.physiocareapifx.model.Records.RecordResponse;
import olex.physiocareapifx.services.AppointmentService;
import olex.physiocareapifx.services.PatientService;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static olex.physiocareapifx.utils.Utils.userId;
import static olex.physiocareapifx.utils.Utils.userPhysio;

public class PatientDetailController implements Initializable {
    public Button btn_Add;
    public Button btn_back;
    public TextField lbl_name;
    public TextField lbl_surname;
    public TextField lbl_addres;
    public TextField lbl_email;
    public TextField lbl_insuranceNumber;
    public DatePicker lbl_date;
    public TextField lbl_medical;
    public TableView<Appointment> tableViewAppointment;
    @FXML
    public TableColumn<Appointment,String> colDate;
    @FXML
    public TableColumn<Appointment,String> colDiagnosis;
    @FXML
    public TableColumn<Appointment,String> colObservations;
    @FXML
    public TableColumn<Appointment,String> colPhysio;
    @FXML
    public TableColumn<Appointment,String> colTreatment;
    @FXML
    public TableColumn<Appointment,String> colStatus;
    public ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    public Button btn_download;
    public Patient patient;
    public Gson gson = new Gson();
    //// Get Appointments
    /**
     * Get all appointments
     */
    public void getAppointment(){
        tableViewAppointment.getItems().clear();
        String url ="";
        System.out.println(userId);
        System.out.println(Utils.isPhysio);
        url = ServiceUtils.API_URL + "/records/appointments/patients/" + userPhysio;
        genericalyGetAppointment(url);
    }
    /**
     * Fucntion generic to get appointments by url
     * @param url url to get appointments
     */
    public void genericalyGetAppointment(String url){
        AppointmentService.getAppointments(url)
                .thenAccept(appointments -> {
                    Platform.runLater(() -> {
                        this.appointments.clear();
                        this.appointments.addAll(appointments);
                        tableViewAppointment.setItems(this.appointments);
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        MessageUtils.showError("Error", "Failed to get appointments");
                    });
                    return null;
                });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btn_back.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("menu.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (Exception e) {
                MessageUtils.showError("Error", "Failed to load menu");
            }
        });
        btn_Add.setOnAction(actionEvent -> {
            updatePatient();
        });
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPhysio.setCellValueFactory(new PropertyValueFactory<>("physio"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
       // getAppointment();
        getRecords();
        loadPatient();


    }

    private void getRecords() {
        String url = ServiceUtils.API_URL + "/records/patient/" + userPhysio;
        CompletableFuture<RecordResponse> future = AppointmentService.getRecords(url);
        future.thenAccept(recordResponse -> {
            if (recordResponse.isOk()) {
                Platform.runLater(() -> {
                    lbl_medical.setText(recordResponse.getRecord().getMedicalRecord());
                    appointments.clear();
                    appointments.addAll(recordResponse.getRecord().getAppointments());
                    tableViewAppointment.setItems(appointments);
                });
            } else {
                Platform.runLater(() -> {
                    MessageUtils.showError("Error", "No se pudo cargar el historial médico del paciente.");
                });
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> {
                MessageUtils.showError("Error", "Fallo al obtener el historial médico: " + ex.getMessage());
            });
            return null;
        });




    }


    private void loadPatient() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL +"/patients/"+userPhysio, null, "GET");
                PatientResponse response = gson.fromJson(json, PatientResponse.class);
                if (response.isOk()) {
                    patient = response.getPatient();
                    javafx.application.Platform.runLater(() ->
                        {
                            if (patient != null) {
                                lbl_name.setText(patient.getName());
                                lbl_surname.setText(patient.getSurname());
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                                LocalDate parsedDate = LocalDate.parse(patient.getBirthDate(), formatter);
                                lbl_date.setValue(parsedDate);
                                lbl_addres.setText(patient.getAddress());
                                lbl_insuranceNumber.setText(patient.getInsuranceNumber());
                                lbl_email.setText(patient.getEmail() != null ? patient.getEmail() : "");
                            } else {
                                MessageUtils.showError("Error", "No se encontró el paciente.");
                            }
                        }
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
     * Updates a selected patient with the form data.
     */
    private void updatePatient() {
        Patient selected = patient;
        if (selected == null) {
            MessageUtils.showError("Error", "Selecciona un paciente para editar.");
            return;
        }
        System.out.println("Selected patient: " + selected.getId());
        Patient updated = getPatientFromFields();
        updated.setId(selected.getId());
        System.out.println("Updated patient: " + updated.getId());
        if (updated.equals(selected)) {
            MessageUtils.showMessage("Aviso", "No se han hecho cambios.");
            return;
        }

        PatientService service = new PatientService();
        service.configureFor(PatientService.Method.PUT, updated);
        service.start();

        service.setOnSucceeded(e -> {
            BaseResponse response = service.getValue();
            if (response.isOk()) {
                MessageUtils.showMessage("Éxito", "Paciente actualizado correctamente.");
                // Actualizar la vista o realizar otras acciones necesarias
                loadPatient(); // Recargar los datos del paciente
            } else {
                MessageUtils.showError("Error", response.getError());
            }
        });

        service.setOnFailed(e -> MessageUtils.showError("Error", "Fallo al actualizar el paciente."));
    }

    private Patient getPatientFromFields() {
        if (lbl_name == null || lbl_surname == null || lbl_addres == null || lbl_insuranceNumber == null || lbl_email == null) {
            MessageUtils.showError("Error", "Hay campos no inicializados. Verifica el FXML.");
            return null;
        }

        String name = lbl_name != null ? lbl_name.getText().trim() : "";
        String surname = lbl_surname != null ? lbl_surname.getText().trim() : "";
//        String birthDate = birthdateField != null ? birthdateField.getText().trim() : "";
        LocalDate birthDate = lbl_date.getValue();
        String address = lbl_addres != null ? lbl_addres.getText().trim() : "";
        String insurance = lbl_insuranceNumber != null ? lbl_insuranceNumber.getText().trim() : "";
        String email = lbl_email != null && lbl_email.getText() != null ? lbl_email.getText().trim() : "";

        // Validaciones
        if (name.isEmpty() || name.length() < 2 || name.length() > 50) {
            MessageUtils.showError("Error", "El nombre debe tener entre 2 y 50 caracteres.");
            return null;
        }

        if (surname.isEmpty() || surname.length() < 2 || surname.length() > 50) {
            MessageUtils.showError("Error", "El apellido debe tener entre 2 y 50 caracteres.");
            return null;
        }


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



}
