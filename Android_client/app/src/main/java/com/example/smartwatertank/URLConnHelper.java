package com.example.automaticwatertank;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLConnHelper {

    private static final String LOG_TAG = URLConnHelper.class.getSimpleName();

    private static HttpURLConnection urlConnection = null;
    private static BufferedReader reader = null;

    // Function to establish network connection and make the login request:
    public static String sendLoginInfoToServer(String JSONdata) {


        try {

            Log.d("URLConn helper " , JSONdata);

            // Create the URL object:
            URL url = new URL("http://192.168.1.11:3000/api/users/login");

            // Open the url connection and set properties:
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type" , "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            // Confirm the connection: ??
            urlConnection.connect();

            // Write JSON data to the urlConnection using a Buffered writer:
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(JSONdata);
            writer.flush();

            // Read in the response:
            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

            // Go through the buffered reader and build a string:
            String temp;
            while ( (temp = reader.readLine()) != null){
                builder.append(temp);
            }

            return builder.toString();


        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
        finally {

            if(reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }


}
