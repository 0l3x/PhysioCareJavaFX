package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.jshell.execution.Util;
import olex.physiocareapifx.model.*;
import olex.physiocareapifx.model.Record;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static olex.physiocareapifx.utils.Utils.userId;
import static olex.physiocareapifx.utils.Utils.userPhysio;

public class PhysioDetailController implements Initializable {
    public TextField txtName;
    public ComboBox<String> cmbSpecialty;
    public TextField txtLicenseNumber;
    public TextField txtEmail;
    public Button getImage;
    public Button btnEdit;
    public Button btnBack;
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
    public ComboBox cmbRecords;
    public DatePicker datePicker;
    public TextField diagnosisField;
    public TextField observationsField;
    public TextField treatmentField;
    public ComboBox cmbStatus;
    public Button addBtn;
    public Button editBtn;
    public Button btnClean;
    public Gson gson = new Gson();
    public ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    public TextField txtSurname;
    public Physio physio = new Physio();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbSpecialty.getItems().clear();
        cmbSpecialty.getItems().addAll("Sports", "Neurological", "Pediatric", "Geriatric", "Oncological");

        getImage.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            fileChooser.setTitle("Select Image");
            File file = fileChooser.showOpenDialog(getImage.getScene().getWindow());
            if (file != null) {
                String imagePath = file.getAbsolutePath();
                try {
                    String base64Image = Utils.encodeImageToBase64(imagePath);
                    System.out.println("Imagen en Base64: " + base64Image);
                    MessageUtils.showMessage("Imagen codificada","Accept to encode image to base64");
                } catch (IOException e) {
                    MessageUtils.showError("Error", "Failed to encode image to Base64");
                }


            } else {
                MessageUtils.showError("No Image Selected", "Please select an image.");
            }
        });
        btnBack.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadScreen("menu.fxml",(Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
            } catch (Exception e) {
                MessageUtils.showError("Error", "Failed to load menu");
            }
        });
        cmbStatus.getItems().addAll("pending", "completed", "cancelled");
        cmbStatus.getSelectionModel().select("pending");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPhysio.setCellValueFactory(new PropertyValueFactory<>("physio"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Appointment, Void> colAcciones = createDeleteColumn(appointments);
        colAcciones.setPrefWidth(100);
        tableViewAppointment.getColumns().add(colAcciones);
        getAppointment();
       // getRecord();

        ContextMenu contextMenu = new ContextMenu();
        addBtn.setOnAction(e->addAppointment());
        editBtn.setOnAction(e->updateAppointment());
        tableViewAppointment.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFieldsFromAppointment(newVal);
                addBtn.setDisable(true);
                cmbRecords.setDisable(true);
            }
        });
        btnClean.setOnAction(e->{
            cleanForm();
        });
        cmbRecords.setOnMouseEntered(e->{
            if(cmbRecords.getSelectionModel().getSelectedItem() == null){
                System.out.println("No hay valor seleccionado, no muestro menú");
                contextMenu.getItems().clear();
                contextMenu.hide();
                return;
            }
            CompletableFuture<Record> record = getRecordById();
            if(record != null){
                try{
                    if (record.get().getPatient() != null) {
                        System.out.println("No hay valor seleccionado, no muestro menú");
                        contextMenu.getItems().clear();
                        contextMenu.getItems().addAll(
                                new MenuItem("Record ID: " + record.get().getId()),
                                new MenuItem("Patient: " + record.get().getPatient()),
                                new MenuItem("Medical Record: " + record.get().getMedicalRecord())
                        );
                        contextMenu.show(cmbRecords, Side.BOTTOM, 0,0);

                    }
                }catch (Exception ex){
                    MessageUtils.showError("Error","Failed to get record");
                }
            }

        });

        cmbRecords.setOnMouseExited(e->{
            contextMenu.hide();
        });
        getPhysioById();
    }

    public void getPhysioById(){
        String physioId = userPhysio;
        String url = ServiceUtils.SERVER + "/physios/" + physioId;
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json->gson.fromJson(json, PhysioResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()->{
                            txtName.setText(response.getPhysio().getName());
                            txtLicenseNumber.setText(response.getPhysio().getLicenseNumber());
                            txtEmail.setText(response.getPhysio().getEmail());
                            cmbSpecialty.setValue(response.getPhysio().getSpecialty());
                            txtSurname.setText(response.getPhysio().getSurname());
                            physio.setAvatar(response.getPhysio().getAvatar());
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


    public void getRecord(){
        String url = ServiceUtils.SERVER + "/records";
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json-> gson.fromJson(json, RecordListResponse.class))
                .thenAccept(response->{
                    for(int i = 0; i < response.getRecords().size(); i++){
                        cmbRecords.getItems().add(response.getRecords().get(i).getId());
                        appointments.addAll(response.getRecords().get(i).getAppointments());
                        tableViewAppointment.setItems(appointments);
                    }
                }).exceptionally(ex->{
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post patient");
                    });
                    return null;
                });
    }

    public void addAppointment(){
        String recordId = (String) cmbRecords.getSelectionModel().getSelectedItem();
        String date = String.valueOf(datePicker.getValue());
        String diagnosis = diagnosisField.getText();
        String observations = observationsField.getText();
        String physio = userId;
        String treatment = treatmentField.getText();
        String status = (String) cmbStatus.getSelectionModel().getSelectedItem();
        Appointment appointment = new Appointment(date,physio,diagnosis,treatment,observations,status);
        String url = ServiceUtils.SERVER + "/records/appointments/" + recordId;
        String jsonRequest = gson.toJson(appointment);
        ServiceUtils.getResponseAsync(url,jsonRequest,"POST")
                .thenApply(json->gson.fromJson(json, RecordResponse.class))
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
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }
    public void genericalyGetAppointment(String url){
        tableViewAppointment.getItems().clear();
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json-> gson.fromJson(json, AppointmentListResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()-> {
                            appointments.setAll(response.getAppointments());
                            //añadir un botton en cada fila
                            tableViewAppointment.setItems(appointments);

                        });
                    }
                }).exceptionally(ex->{
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }


    public void getAppointment(){
        tableViewAppointment.getItems().clear();
        String url ="";
        System.out.println(userId);
        System.out.println(Utils.isPhysio);
        if(Utils.isPhysio){
            url = ServiceUtils.SERVER + "/records/appointments/physio/" + userId;
        }else{
            url = ServiceUtils.SERVER  + "/records/appointmentAdmin";
        }
        System.out.println(url);
        genericalyGetAppointment(url);
    }

    public CompletableFuture<olex.physiocareapifx.model.Record> getRecordById(){
        CompletableFuture<Record> future = new CompletableFuture<>();
        String recordId = (String) cmbRecords.getSelectionModel().getSelectedItem();
        String url = ServiceUtils.SERVER + "/records/" + recordId;
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json->gson.fromJson(json, RecordResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        future.complete(response.getRecord());
                        tableViewAppointment.getItems().clear();
                        appointments.addAll(response.getRecord().getAppointments());
                        tableViewAppointment.setItems(appointments);
                    }
                }).exceptionally(ex->{
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
        while (!future.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return future;
    }


    public void updateAppointment(){
        addBtn.setDisable(false);
        cmbRecords.setDisable(false);
        String recordId = (String) cmbRecords.getSelectionModel().getSelectedItem();
        String id = tableViewAppointment.getSelectionModel().getSelectedItem().getId();
        String date = datePicker.getValue().toString();
        String diagnosis = diagnosisField.getText();
        String observations = observationsField.getText();
        String physio = userId;
        String treatment = treatmentField.getText();
        String status = (String) cmbStatus.getSelectionModel().getSelectedItem();
        Appointment appointment = new Appointment(id,date,physio,diagnosis,treatment,observations,status);
        String url = ServiceUtils.SERVER + "/records/appointments/" + id;
        String jsonRequest = gson.toJson(appointment);
        ServiceUtils.getResponseAsync(url,jsonRequest,"PUT")
                .thenApply(json->gson.fromJson(json, RecordResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()->{
                            MessageUtils.showMessage("Success","Appointment updated");
                            getAppointment();
                            cleanForm();
                        });
                    }else{
                        Platform.runLater(()->{
                            MessageUtils.showError("Error",response.getError());
                        });
                    }
                }).exceptionally(ex->{
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }

    public void deleteAppointment(Appointment appointment){
        String url = ServiceUtils.SERVER + "/records/appointments/" + appointment.getId();
        ServiceUtils.getResponseAsync(url,null,"DELETE")
                .thenApply(json->gson.fromJson(json, RecordResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        Platform.runLater(()->{
                            MessageUtils.showMessage("Success","Appointment deleted");
                            getAppointment();
                            cleanForm();
                        });
                    }else{
                        Platform.runLater(()->{
                            MessageUtils.showError("Error",response.getError());
                        });
                    }
                }).exceptionally(ex->{
                    Platform.runLater(()->{
                        MessageUtils.showError("Error","Failed to post appointment" );
                    });
                    return null;
                });
    }

    public void fillFieldsFromAppointment(Appointment appointment){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDate parsedDate = LocalDate.parse(appointment.getDate(), formatter);
        datePicker.setValue(parsedDate);
        diagnosisField.setText(appointment.getDiagnosis());
        observationsField.setText(appointment.getObservations());
        treatmentField.setText(appointment.getTreatment());
        cmbStatus.getSelectionModel().select(appointment.getStatus());
    }
    public void cleanForm(){
        addBtn.setDisable(false);
        cmbRecords.setDisable(false);
        datePicker.setValue(null);
        diagnosisField.clear();
        treatmentField.clear();
        tableViewAppointment.getSelectionModel().clearSelection();
        observationsField.clear();
        cmbStatus.getSelectionModel().select("pending");
        cmbRecords.getSelectionModel().select(-1);
    }

    private <T> TableColumn<T, Void> createDeleteColumn(ObservableList<T> items) {
        TableColumn<T, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory((Callback<TableColumn<T, Void>, TableCell<T, Void>>) column ->
                new TableCell<>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setOnAction(e -> {
                            Appointment item = (Appointment) getTableView().getItems().get(getIndex());
                            System.out.println("Item to delete: " + item);
                            deleteAppointment(item);
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
