package com.conorsmine.net.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class FileUtils {

    public static Object parseFileToJson(final File file) {
        final JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(file)) { return parser.parse(reader); }
        catch (IOException | ParseException e) { e.printStackTrace(); }

        return new JSONObject();
    }

    public static void saveJsonToFile(final File file, final Object json) {
        saveJsonToFile(file, json, true);
    }

    public static void saveJsonToFile(final File file, final Object json, boolean prettyPrinting) {
        try (FileWriter writer = new FileWriter(file)) {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(json));

            writer.flush();
        }
        catch (IOException e) { e.printStackTrace(); }
    }
}
