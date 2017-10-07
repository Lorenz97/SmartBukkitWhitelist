package at.lorenz.whitelist.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class InternetManager {

    public static String getSimpleURLContent(String url) {
        try {
            URLConnection connection = (new URL(url)).openConnection();
            InputStream strinputStreamam = connection.getInputStream();
            Scanner scanner = new Scanner(strinputStreamam);
            return scanner.next();
        } catch (IOException var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
