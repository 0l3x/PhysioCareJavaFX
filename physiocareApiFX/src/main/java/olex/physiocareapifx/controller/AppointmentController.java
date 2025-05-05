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
import olex.physiocareapifx.model.Appointment;
import olex.physiocareapifx.model.AppointmentListResponse;
import olex.physiocareapifx.model.PatientResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.SceneLoader;
import olex.physiocareapifx.utils.ServiceUtils;
import olex.physiocareapifx.utils.Utils;

import java.io.IOException;
import java.net.URL;
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
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDiganosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPhysio.setCellValueFactory(new PropertyValueFactory<>("physio"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        getAppointment();
    }

    public void getAppointment(){
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

    private void loadAppointments() {
        new Thread(() -> {
            try {
                String url="";
                System.out.println(Utils.userId);
                System.out.println(Utils.isPhysio);
                if(Utils.isPhysio){
                    url = ServiceUtils.SERVER + "/appointments/physio/" + Utils.userId;
                }else{
                    url = ServiceUtils.SERVER  + "/appointmentAdmin";
                }
                String json = ServiceUtils.getResponse(url, null, "GET");
                AppointmentListResponse response = gson.fromJson(json, AppointmentListResponse.class);
                if (response.isOk()) {
                    javafx.application.Platform.runLater(() ->
                            tableViewAppointment.getItems().setAll(response.getAppointments())
                    );
                } else {
                    MessageUtils.showError("Error", "No se pudo cargar la lista de pacientes");
                }
            } catch (Exception e) {
                MessageUtils.showError("Error", e.getMessage());
            }
        }).start();
    }



}

