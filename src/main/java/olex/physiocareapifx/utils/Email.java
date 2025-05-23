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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


public class Email {
    private static final String APPLICATION_NAME = "Physiocare";
    private static final JsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();

    private static final String SENDER = "hectorrodriguezplanelles@gmail.com";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "src/main/java/resources2/client_secret_45202089257-469ej7fbq4thf57ak66duqbhma638t31.apps.googleusercontent.com.json";

    public static void sendPatientsEmails(List<Patient> patients) {
        System.out.println("Sending emails to patients..."+patients.size());
        patients.stream().forEach(p -> {
            CompletableFuture<List<Appointment>> future = AppointmentService.getAppointments(ServiceUtils.API_URL  +"/records/appointments/patients/" + p.getId());
            while (!future.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            future.thenAccept(appointment -> {
                if (appointment != null) {
                    p.setAppointments(appointment);
                }
            }).exceptionally(e -> {
                System.out.println("Error fetching appointments for patient: " + p.getId());
                return null;
            });
        });
        patients.stream()
                .filter(p -> p.getAppointments().stream()
                        .filter(a-> Objects.equals(a.getStatus(), "completed"))
                        .toList()
                        .size() >= 8
                ).forEach(Email::sendPatientEmail);
        System.out.println("Sending emails to patients...");
       // System.out.println("Patients: " + patients.size());
        // patients.forEach(Email::sendPatientEmail);
    }

    public static void sendPatientEmail(Patient patient) {

        //File dest = PdfUtils.getPatientPdf(patient);
        //System.out.println(dest.getName());
        System.out.println(patient.getEmail());
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
             MimeMessage emailContent= createEmailWithAttachment3(
                    patient.getEmail(),
                    SENDER,
                    "Cohmpany Notice",
                    "Dear "+ patient.getName() + ",\n\n" +
                            "You have two appointments left in total:"+patient.getAppointments().size()+".\n" +
                            "You will not be able to schedule any more.\n" +
                            "Please contact support if you have any questions.\n\n" +
                            "Best regards,\n" +
                            "Cohmpany Team");
            // Send the email
            sendMessage(service, user, emailContent);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending patient email");
        }
    }

    /*public static void sendPhysiosEmails(List<Physio> physios){
        physios.forEach(Email::sendPhysioMail);
    }*/

   /* public static void sendPhysioMail(Physio physio) {

        File dest = PdfUtils.getPhysioPdf(physio);
        try {

            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Define the email parameters
            String user = "me";
            System.out.println(physio.getEmail());
            MimeMessage emailContent= createEmailWithAttachment(
                    physio.getEmail(),
                    SENDER,
                    "Salary",
                    "Hi " + physio.getName() + ", we have processed your salary for this current month.",
                    dest.getAbsolutePath());

            // Send the email
            sendMessage(service, user, emailContent);
        } catch (Exception e) {
            System.out.println("Error sending physio email");
        }
    }*/
    public static Credential getCredentials(
            final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        // Load client secrets.
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY,
                        new InputStreamReader(
                                new FileInputStream(CREDENTIALS_FILE_PATH)));
        // Build flow and trigger user authorization request.
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

        // Create email body
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");

        // Attach archivo
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new jakarta.activation.DataHandler(
                new jakarta.activation.FileDataSource(fileDir)));
        attachmentPart.setFileName(new java.io.File(fileDir).getName());

        // Create MIME structure
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

        // Create email body
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");


        // Create MIME structure
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        email.setContent(multipart, "multipart/mixed");

        return email;
    }

    public static void sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, java.io.IOException {
        Message message = createMessageWithEmail(emailContent);
        service.users().messages().send(userId, message).execute();
        System.out.println("Email sent successfully.");
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
