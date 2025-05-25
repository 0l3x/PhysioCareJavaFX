package olex.physiocareapifx.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.services.AppointmentService;
import olex.physiocareapifx.utils.pdf.PdfUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class Email {
    private static final String APPLICATION_NAME = "Physiocare";
    private static final JsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();

    private static final String SENDER = "olexanderg3@gmail.com";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "resources2/client_secret_522949174539-fdt6bni4hvong4930shc6qf7llfcskis.apps.googleusercontent.com.json";

    /**
     * Sends emails to patients who have reached the limit of available appointments.
     * It checks each patient's appointments and sends an email if they have 8 or more completed appointments.
     *
     * @param patients List of patients to check and send emails to.
     */
    public static void sendPatientsEmails(List<Patient> patients) {
        patients.stream().forEach(p -> { // For each patient, fetch their appointments asynchronously
            CompletableFuture<List<Appointment>> future = AppointmentService.getAppointments(ServiceUtils.API_URL  +"/records/appointments/patients/" + p.getId());
            while (!future.isDone()) {
                try {
                    Thread.sleep(100); // Wait for the future to complete
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            future.thenAccept(appointment -> { // Once the appointments are fetched, set them to the patient
                if (appointment != null) {
                    p.setAppointments(appointment);
                }
            }).exceptionally(e -> {
                System.out.println("Error fetching appointments for patient: " + p.getId());
                return null;
            });
        });
        patients.stream()
                .filter(p -> p.getAppointments().stream() // Filter patients who have 8 or more completed OR PENDING! appointments
                        .filter(a-> Objects.equals(a.getStatus(), "completed"))
                        .toList()
                        .size() >= 8
                ).forEach(Email::sendPatientEmail);

    }

    public static void sendPatientEmail(Patient patient) {

        File dest = PdfUtils.getPatientAppointmentsPdf(patient);
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Define the email parameters
            String user = "me";
           /* MimeMessage emailContent= createEmailWithAttachment(
                    patient.getEmail(),
                    SENDER,
                    "Physiocare Notice",
                    "You are about to reach the limit of available appointments. " +
                            "See the attached document for more details.",
                    dest.getAbsolutePath());*/
             MimeMessage emailContent= createEmailWithAttachment(
                    patient.getEmail(),
                    SENDER,
                    "Cohmpany Notice",
                    "Dear "+ patient.getName() + ",\n\n" +
                            "You have two appointments left in total:"+patient.getAppointments().size()+".\n" +
                            "You will not be able to schedule any more.\n" +
                            "Please contact support if you have any questions.\n\n" +
                            "Best regards,\n" +
                            "Cohmpany Team",
                    dest.getAbsolutePath()
                     );
            // Send the email
            sendMessage(service, user, emailContent);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending patient email");
        }
    }

    public static void sendPhysiosEmails(List<Physio> physios){
        physios.forEach(Email::sendPhysioMail);
    }

   public static void sendPhysioMail(Physio physio) {


        try {

            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String user = "me";
            System.out.println(physio.getEmail());
           /* MimeMessage emailContent= createEmailWithAttachment(
                    physio.getEmail(),
                    SENDER,
                    "COHMPANY Payslip for " + physio.getName(),
                    "Dear " + physio.getName() + ",\n\n" +
                            "I hope you are well.\n\n" +
                            "Please find attached the payroll statement for the period of May 1, 2025 – May 15, 2025. " +
                            "This document details your gross earnings, deductions (including social security and tax withholdings), and net pay for the specified pay period.\n\n" +
                            "Should you have any questions or require further clarification regarding your payslip, do not hesitate to contact the Payroll Department at  payroll@company.com or call extension 1234.\n\n" +
                            "Thank you for your continued dedication and hard work.\n\n" +
                            "Kind regards,\n\n" ,
                    dest.getAbsolutePath());*/
            MimeMessage emailContent= createEmailWithAttachment3(
                    physio.getEmail(),
                    SENDER,
                    "COHMPANY Payslip for " + physio.getName(),
                    "Dear " + physio.getName() + ",\n\n" +
                            "I hope you are well.\n\n" +
                            "Please find attached the payroll statement for the period of "+new Date() +"\n" +
                            "This document details your gross earnings, deductions (including social security and tax withholdings), and net pay for the specified pay period.\n\n" +
                            "Should you have any questions or require further clarification regarding your payslip, do not hesitate to contact the Payroll Department at  payroll@company.com or call extension 1234.\n\n" +
                            "Thank you for your continued dedication and hard work.\n\n" +
                            "Kind regards,\n\n"
                   );

            sendMessage(service, user, emailContent);
        } catch (Exception e) {
            System.out.println("Error sending physio email");
        }
    }

    public static Credential getCredentials(
            final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY,
                        new InputStreamReader(
                                new FileInputStream(CREDENTIALS_FILE_PATH)));
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                        Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM))
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline")
                        .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }
    public static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText, String fileDir) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new jakarta.activation.DataHandler(
                new jakarta.activation.FileDataSource(fileDir)));
        attachmentPart.setFileName(new java.io.File(fileDir).getName());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        email.setContent(multipart, "multipart/mixed");

        return email;
    }
    public static MimeMessage createEmailWithAttachment3(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");


        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        email.setContent(multipart, "multipart/mixed");

        return email;
    }

    public static void sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, java.io.IOException {
        Message message = createMessageWithEmail(emailContent);
        service.users().messages().send(userId, message).execute();
    }

    public static Message createMessageWithEmail(MimeMessage email) throws MessagingException, java.io.IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = java.util.Base64.getUrlEncoder()
                .encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

}
