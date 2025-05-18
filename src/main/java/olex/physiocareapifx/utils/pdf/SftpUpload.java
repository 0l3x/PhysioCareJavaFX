package olex.physiocareapifx.utils.pdf;

import com.jcraft.jsch.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SftpUpload {
    public static void uploadFile(String username, String password, String host, String localFile, String remoteDir) {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        try {
            // Establecer la sesión SSH
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);

            // Configurar parámetros de conexión (evitar advertencias de autenticación)
            session.setConfig("StrictHostKeyChecking", "no");

            // Conectar a la sesión
            session.connect();

            // Abrir canal SFTP
            channel = session.openChannel("sftp");
            sftpChannel = (ChannelSftp) channel;

            // Conectar al canal SFTP
            sftpChannel.connect();

            // Subir el archivo local al directorio remoto
            sftpChannel.put(new FileInputStream(localFile), remoteDir);

            System.out.println("Archivo subido con éxito a: " + remoteDir);

        } catch (JSchException | SftpException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            // Cerrar conexiones SFTP y SSH
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.exit();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        String username = "olexftp";
        String password = "contra";
        String host = "olexanderg.net";
        String localFile = "C:/Users/Olex/Desktop/PhysioCareJavaFX/resources/records/123456789.pdf";
        String remoteDir = "home/olexftp/";

        uploadFile(username, password, host, localFile, remoteDir);
    }
}

