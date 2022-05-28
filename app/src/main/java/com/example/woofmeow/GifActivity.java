package com.example.woofmeow;


import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class GifActivity extends AppCompatActivity {

    private static final String API_KEY = "YZ7I01G6NJ1F";
    private static final String LogTag = "TenorTest";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread() {
            @Override
            public void run() {

                final String searchTerm = "excited";

                // make initial search request for the first 8 items
                JSONObject searchResult = getSearchResults(searchTerm, 8);

                // load the results for the user
                Log.v(LogTag, "Search Results: " + searchResult.toString());

            }
        }.start();
    }

    /**
     * Get Search Result GIFs
     */
    public static JSONObject getSearchResults(String searchTerm, int limit) {

        // make search request - using default locale of EN_US

        final String url = String.format("https://g.tenor.com/v1/search?q=%1$s&key=%2$s&limit=%3$s",
                searchTerm, API_KEY, limit);
        try {
            return get(url);
        } catch (IOException | JSONException ignored) {
        }
        return null;
    }

    /**
     * Construct and run a GET request
     */
    private static JSONObject get(String url) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            // Get request
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("HTTP Code: '%1$s' from '%2$s'", statusCode, url);
                throw new ConnectException(error);
            }

            // Parse response
            return parser(connection);
        } catch (Exception ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONObject("");
    }

    /**
     * Parse the response into JSONObject
     */
    private static JSONObject parser(HttpURLConnection connection) throws JSONException {
        char[] buffer = new char[1024 * 4];
        int n;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(connection.getInputStream());
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            return new JSONObject(writer.toString());
        } catch (IOException ignored) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new JSONObject("");
    }
}
