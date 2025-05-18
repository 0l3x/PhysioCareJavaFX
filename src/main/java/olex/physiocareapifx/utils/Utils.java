package olex.physiocareapifx.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Utils {
    public static boolean isPhysio = false;
    public static String userId = "";
    public static String userPhysio="";

    public static String encodeImageToBase64(String imagePath) throws IOException {
        // 1. Lee toda la imagen en un array de bytes
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        // 2. Codifica ese array a Base64 y devuelve el resultado
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
