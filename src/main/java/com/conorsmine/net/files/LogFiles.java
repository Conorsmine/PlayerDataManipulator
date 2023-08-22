package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFiles implements ReloadableFile {

    public static final String DIR_NAME = "logs", CHANGES_PATH = "changes_logs.json";

    private File dirFile;
    private File changesLogFile;

    private final PlayerDataManipulator pl;

    public LogFiles(PlayerDataManipulator pl) {
        this.pl = pl;

        reload(pl.getServer().getConsoleSender());
    }

    public void createErrorFile(final CommandSender sender, String fileName, final JSONObject logData) {
        if (!fileName.endsWith(".json")) fileName += ".json";

        final File errorLog = new File(dirFile, fileName);
        try { errorLog.createNewFile(); }
        catch (IOException e) { pl.sendMsg(sender, String.format("§cUNABLE TO CREATE \"%s\" file!", fileName)); }

        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            final FileWriter writer = new FileWriter(errorLog);
            writer.write(gson.toJson(logData));
            writer.flush();
            writer.close();
        }
        catch (IOException e) { pl.sendMsg(sender, String.format("§cUNABLE TO WRITE TO \"%s\" file!", fileName)); }
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        dirFile = new File(pl.getDataFolder(), DIR_NAME);
        dirFile.mkdirs();

        changesLogFile = new File(dirFile, CHANGES_PATH);
        try { changesLogFile.createNewFile(); }
        catch (IOException e) { pl.sendMsg(sender, String.format("§cUNABLE TO CREATE \"%s\" file!", CHANGES_PATH)); }
    }
}
