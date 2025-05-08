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
import javafx.stage.Stage;
import olex.physiocareapifx.model.*;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {
    @FXML
    public Button exitBtn;
    @FXML
    public TableView<Appointment> tableViewAppointment;
    @FXML
    public DatePicker datePicker;
    @FXML
    public TextField diagnosisField;
    @FXML
    public TextField observationsField;
    @FXML
    public TextField physioField;
    @FXML
    public TextField treatmentField;
    @FXML
    public ComboBox<String> cmbStatus;
    @FXML
    public Button addBtn;
    @FXML
    public Button editBtn;
    @FXML
    public Button deleteBtn;

    @FXML
    public TableColumn<Appointment,String> colDate;
    @FXML
    public TableColumn<Appointment,String> colDiganosis;
    @FXML
    public TableColumn<Appointment,String> colObservations;
    @FXML
    public TableColumn<Appointment,String> colPhysio;
    @FXML
    public TableColumn<Appointment,String> colTreatment;
    @FXML
    public TableColumn<Appointment,String> colStatus;
    public Gson gson = new Gson();
    public ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    public ComboBox<String> cmbRecords;
    public Button btnClean;
    public ComboBox<String> cmbPhysios;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exitBtn.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("menu.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        cmbStatus.getItems().addAll("pending", "completed", "cancelled");
        cmbStatus.getSelectionModel().select("pending");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDiganosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPhysio.setCellValueFactory(new PropertyValueFactory<>("physio"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        getRecord();
        getAppointment();
        loadPhysios();

        addBtn.setOnAction(e->addAppointment());
        editBtn.setOnAction(e->updateAppointment());
        deleteBtn.setOnAction(e->deleteAppointment());
        btnClean.setOnAction(e->{
            cleanForm();
        });

        tableViewAppointment.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFieldsFromAppointment(newVal);
                addBtn.setDisable(true);
                cmbRecords.setDisable(true);
            }
        });
    }

    public void getAppointment(){
        tableViewAppointment.getItems().clear();
        String url ="";
        System.out.println(Utils.userId);
        System.out.println(Utils.isPhysio);
        if(Utils.isPhysio){
           url = ServiceUtils.SERVER + "/records/appointments/physio/" + Utils.userId;
       }else{
           url = ServiceUtils.SERVER  + "/records/appointmentAdmin";
       }
        System.out.println(url);
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json-> gson.fromJson(json, AppointmentListResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()-> {
                            appointments.setAll(response.getAppointments());
                            tableViewAppointment.setItems(appointments);
                        });
                    }
                }).exceptionally(ex->{
                    ex.printStackTrace();
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }

    private void loadPhysios() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios", null, "GET");
                PhysioResponse response = gson.fromJson(json, PhysioResponse.class);
                if (response.isOk()) {
                   Platform.runLater(() ->{
                       for(int i = 0; i < response.getResultado().size(); i++) {
                           cmbPhysios.getItems().add(response.getResultado().get(i).getId() + "-" +
                                   response.getResultado().get(i).getName());
                       }});
                } else {
                    MessageUtils.showError("Error", "No se pudo cargar la lista de fisioterapeutas");
                }
            } catch (Exception e) {
                MessageUtils.showError("Error", e.getMessage());
            }
        }).start();
    }

    public void getRecord(){
        String url = ServiceUtils.SERVER + "/records";
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json-> gson.fromJson(json, RecordListResponse.class))
                .thenAccept(response->{
                    for(int i = 0; i < response.getRecords().size(); i++){
                        cmbRecords.getItems().add(response.getRecords().get(i).getId());
                    }
                }).exceptionally(ex->{
                    ex.printStackTrace();
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post patient");
                    });
                    return null;
                });
    }

    public void addAppointment(){
        String recordId = cmbRecords.getSelectionModel().getSelectedItem();
        String date = datePicker.getValue().toString();
        String diagnosis = diagnosisField.getText();
        String observations = observationsField.getText();
        String physio = physioField.getText();
        String treatment = treatmentField.getText();
        String status = cmbStatus.getSelectionModel().getSelectedItem();
        Appointment appointment = new Appointment(date,diagnosis,observations,physio,treatment,status);
        String url = ServiceUtils.SERVER + "/records/appointments/" + recordId;
        String jsonRequest = gson.toJson(appointment);
        ServiceUtils.getResponseAsync(url,jsonRequest,"POST")
                .thenApply(json->gson.fromJson(json, AppointmentListResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()->{
                            MessageUtils.showMessage("Success","Appointment added");
                            getAppointment();
                            cleanForm();
                        });
                    }else{
                        Platform.runLater(()->{
                            MessageUtils.showError("Error",response.getError());
                        });
                    }
                }).exceptionally(ex->{
                    ex.printStackTrace();
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }

    public void updateAppointment(){
        addBtn.setDisable(false);
        cmbRecords.setDisable(false);
    }

    public void deleteAppointment(){

    }

    public void fillFieldsFromAppointment(Appointment appointment){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDate parsedDate = LocalDate.parse(appointment.getDate(), formatter);
        datePicker.setValue(parsedDate);
        diagnosisField.setText(appointment.getDiagnosis());
        observationsField.setText(appointment.getObservations());
        physioField.setText(appointment.getPhysio());
        treatmentField.setText(appointment.getTreatment());
        cmbStatus.getSelectionModel().select(appointment.getStatus());
    }
    public void cleanForm(){
        addBtn.setDisable(false);
        cmbRecords.setDisable(false);
        datePicker.setValue(null);
        diagnosisField.clear();
        physioField.clear();
        treatmentField.clear();
        tableViewAppointment.getSelectionModel().clearSelection();
        observationsField.clear();
        cmbStatus.getSelectionModel().select("pending");
        cmbRecords.getSelectionModel().select(-1);
    }
}

