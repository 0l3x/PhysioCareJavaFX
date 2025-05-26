package olex.physiocareapifx.utils.pdf;

import com.jcraft.jsch.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SftpUpload {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static void uploadFile(String username, String password, String host,
                                  String localFile, String remotePath) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftp = null;
        try {
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            // Aquí remotePath = "/home/olexftp/records/123456789.pdf"
            sftp.put(new FileInputStream(localFile), remotePath);
            System.out.println("✔ Subida correcta: " + remotePath);

        } catch (JSchException | SftpException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (sftp != null && sftp.isConnected()) sftp.exit();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    public static void main(String[] args) {
//        String username = dotenv.get("SFTP_USERNAME");
//        String password = dotenv.get("SFTP_PASSWORD");
//        String host = dotenv.get("SFTP_HOST");
//        String localFile = "./resources/records/123456789.pdf";
//        String remoteDir = "/home/olexftp/records/" + new File(localFile).getName();
//
//        System.out.println("Local file exists? " + new File(localFile).exists() + " -> " + localFile);
//
//        uploadFile(username, password, host, localFile, remoteDir);
    }
}

