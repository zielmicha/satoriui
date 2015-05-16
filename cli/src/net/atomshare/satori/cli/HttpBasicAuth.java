package net.atomshare.satori.cli;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpBasicAuth {

    public static void main(String[] args) {

        try {
            URL url = new URL ("https://satori.atomshare.net/page-info/2696917:8080/farqd"); // "http://ip:port/login"
            String encoding = ("korwinkrul");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}