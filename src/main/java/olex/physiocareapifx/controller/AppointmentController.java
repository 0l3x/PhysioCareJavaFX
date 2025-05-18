package olex.physiocareapifx.controller;

import com.google.gson.Gson;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Physios.PhysioListResponse;
import olex.physiocareapifx.model.Physios.PhysioResponse;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.model.Records.RecordListResponse;
import olex.physiocareapifx.model.Records.RecordResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static olex.physiocareapifx.utils.Utils.userId;

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
    public TableColumn<Appointment,String> colDiagnosis;
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
    public MFXLegacyComboBox<String> cmbFilter;
    public Physio physio;

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
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPhysio.setCellValueFactory(new PropertyValueFactory<>("physio"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Appointment, Void> colAcciones = createDeleteColumn(appointments);
        colAcciones.setPrefWidth(100);
        tableViewAppointment.getColumns().add(colAcciones);
        getRecord();
        getAppointment();
        loadPhysios();

        addBtn.setOnAction(e->addAppointment());
        editBtn.setOnAction(e->updateAppointment());
        btnClean.setOnAction(e->{
            cleanForm();
        });
        ContextMenu contextMenu = new ContextMenu();

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

        cmbPhysios.setOnMouseEntered(e->{
            if(cmbPhysios.getSelectionModel().getSelectedItem() == null){
                System.out.println("No hay valor seleccionado, no muestro menú");
                contextMenu.getItems().clear();
                contextMenu.hide();
                return;
            }
            //Completable future para esperar a que responda
            CompletableFuture<Physio> physio = getPhysioById();
            if(physio != null){
                try{
                    if (physio.get().getName() != null) {
                        System.out.println("No hay valor seleccionado, no muestro menú");
                        contextMenu.getItems().clear();
                        contextMenu.getItems().addAll(
                                new MenuItem("Physio ID: " + physio.get().getId()),
                                new MenuItem("Name: " + physio.get().getName()),
                                new MenuItem("Email: " + physio.get().getEmail()),
                                new MenuItem("LicenseNumber"+ physio.get().getLicenseNumber()),
                                new MenuItem("Specialty: " + physio.get().getSpecialty())
                        );
                        contextMenu.show(cmbPhysios, Side.BOTTOM, 0,0);

                    }
                }catch (Exception ex){
                    MessageUtils.showError("Error","Failed to get record");
                }
            }
        });

        cmbPhysios.setOnMouseExited(e->{
            contextMenu.hide();
        });



        cmbFilter.getItems().addAll("All","Completed","Future","Past");
        cmbFilter.getSelectionModel().select("All");
        cmbFilter.setOnAction(e->{
            String selected = (String) cmbFilter.getSelectionModel().getSelectedItem();
            switch (selected) {
                case "All" -> {
                    getAppointment();
                }
                case "Completed" -> {
                    getAppointmentComplete();
                }
                case "Future" -> {
                    getAppointmentPastOrFuture(true);
                }
                case "Past" -> {
                    getAppointmentPastOrFuture(false);
                }
            }
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
        System.out.println(userId);
        System.out.println(Utils.isPhysio);
        if(Utils.isPhysio){
           url = ServiceUtils.API_URL + "/records/appointments/physio/" + userId;
       }else{
           url = ServiceUtils.API_URL  + "/records/appointmentAdmin";
       }
       /* System.out.println(url);
        genericalyGetAppointment(url);*/

    }

    public void loadPhysioData() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios/" + Utils.userId, null, "GET");
                PhysioResponse response = gson.fromJson(json, PhysioResponse.class);
                if (response.isOk()) {
                    Platform.runLater(()    -> {
                        physio = response.getPhysio();
                    });
                } else {
                    Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physio data"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physio data"));
            }
        }).start();
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

    public void getAppointmentComplete(){
        tableViewAppointment.getItems().clear();
        String url = ServiceUtils.API_URL + "/records/appointmentComplete";
        genericalyGetAppointment(url);
    }

    public void getAppointmentPastOrFuture(Boolean future){
        tableViewAppointment.getItems().clear();
        String url ="";
        System.out.println(userId);
        System.out.println(Utils.isPhysio);
        if(future){
            url = ServiceUtils.API_URL  + "/records/appointmentsFuture";
        }else{
            url = ServiceUtils.API_URL + "/records/appointmentsPast";
        }
        genericalyGetAppointment(url);
    }

    private void loadPhysios() {
        new Thread(() -> {
            try {
                String json = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios", null, "GET");
                PhysioListResponse response = gson.fromJson(json, PhysioListResponse.class);
                if (response.isOk()) {
                   Platform.runLater(() ->{
                       for(int i = 0; i < response.getPhysios().size(); i++) {
                           cmbPhysios.getItems().add(response.getPhysios().get(i).getId());
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
        String url = ServiceUtils.API_URL + "/records";
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
        String recordId = cmbRecords.getSelectionModel().getSelectedItem();
        String date = String.valueOf(datePicker.getValue());
        String diagnosis = diagnosisField.getText();
        String observations = observationsField.getText();
        String physio = cmbPhysios.getSelectionModel().getSelectedItem();
        String treatment = treatmentField.getText();
        String status = cmbStatus.getSelectionModel().getSelectedItem();
        Appointment appointment = new Appointment(date,physio,diagnosis,treatment,observations,status);
        String url = ServiceUtils.API_URL + "/records/appointments/" + recordId;
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

    public CompletableFuture<Physio> getPhysioById(){
        CompletableFuture<Physio> future = new CompletableFuture<>();
        String physioId = cmbPhysios.getSelectionModel().getSelectedItem();
        String url = ServiceUtils.API_URL + "/physios/" + physioId;
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json->gson.fromJson(json, PhysioResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        future.complete(response.getPhysio());
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

    public CompletableFuture<Record> getRecordById(){
        CompletableFuture<Record> future = new CompletableFuture<>();
        String recordId = cmbRecords.getSelectionModel().getSelectedItem();
        String url = ServiceUtils.API_URL + "/records/" + recordId;
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
        String recordId = cmbRecords.getSelectionModel().getSelectedItem();
        String id = tableViewAppointment.getSelectionModel().getSelectedItem().getId();
        String date = datePicker.getValue().toString();
        String diagnosis = diagnosisField.getText();
        String observations = observationsField.getText();
        String physio = cmbPhysios.getSelectionModel().getSelectedItem();
        String treatment = treatmentField.getText();
        String status = cmbStatus.getSelectionModel().getSelectedItem();
        Appointment appointment = new Appointment(id,date,physio,diagnosis,treatment,observations,status);
        String url = ServiceUtils.API_URL + "/records/appointments/" + id;
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
        String url = ServiceUtils.API_URL + "/records/appointments/" + appointment.getId();
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
        cmbPhysios.getSelectionModel().select(appointment.getPhysio());
        treatmentField.setText(appointment.getTreatment());
        cmbStatus.getSelectionModel().select(appointment.getStatus());
    }
    public void cleanForm(){
        addBtn.setDisable(false);
        cmbRecords.setDisable(false);
        datePicker.setValue(null);
        diagnosisField.clear();
        cmbPhysios.getSelectionModel().select(userId);
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

