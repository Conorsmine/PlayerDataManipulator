package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFiles {

    public static final String DIR_NAME = "logs", CHANGES_PATH = "changes_logs.json";

    private static final File dirFile;
    private static final File changesLogFile;
    static {
        dirFile = new File(PlayerDataManipulator.getINSTANCE().getDataFolder(), DIR_NAME);
        dirFile.mkdirs();

        changesLogFile = new File(dirFile, CHANGES_PATH);
        try { changesLogFile.createNewFile(); }
        catch (IOException e) { PlayerDataManipulator.sendMsg(String.format("§cUNABLE TO CREATE \"%s\" file!", CHANGES_PATH)); }
    }

    public static void createErrorFile(String fileName, final JSONObject logData) {
        if (!fileName.endsWith(".json")) fileName += ".json";

        final File errorLog = new File(dirFile, fileName);
        try { errorLog.createNewFile(); }
        catch (IOException e) { PlayerDataManipulator.sendMsg(String.format("§cUNABLE TO CREATE \"%s\" file!", fileName)); }

        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            final FileWriter writer = new FileWriter(errorLog);
            writer.write(gson.toJson(logData));
            writer.flush();
            writer.close();
        }
        catch (IOException e) { PlayerDataManipulator.sendMsg(String.format("§cUNABLE TO WRITE TO \"%s\" file!", fileName)); }
    }
}
