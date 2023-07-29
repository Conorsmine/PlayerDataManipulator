package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.webserver.UUIDParser;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.io.*;

public class WebsiteFile {

    public static final String PATH = "web", CSS_PATH = "styles", TEMP_CACHE = "temp_cache", TEMP_CHANGE_PREFIX = "change-";

    public static int getPort() {
        return PlayerDataManipulator.INSTANCE.getConfig().getInt(String.format("%s.%s", ConfigSections.PATH.path, ConfigSections.PORT.path));
    }

    public static File getTempCacheDir() {
        final File dir = new File(PlayerDataManipulator.getINSTANCE().getDataFolder(), TEMP_CACHE);
        dir.mkdirs();

        return dir;
    }

    public static File createTempParsedFile(final String fileID) throws IOException {
        final File file = new File(WebsiteFile.getTempCacheDir(), String.format("%s.json", fileID));
        file.createNewFile();

        return file;
    }

    public static File createTempChangeFile(final String fileID) throws IOException {
        final File file = new File(WebsiteFile.getTempCacheDir(), String.format("%s%s.json", TEMP_CHANGE_PREFIX, fileID));
        file.createNewFile();

        final FileWriter writer = new FileWriter(file);
        writer.write("{}");
        writer.flush();
        writer.close();

        return file;
    }

    public static void setJSONOfChangeFile(final String fileID, final JSONObject json) {
        final File changeFile = getChangeFileFromID(fileID);

        FileUtils.saveJsonToFile(changeFile, json);
    }

    @Nullable
    public static File getChangeFileFromCommandCode(final String cmdCode) {
        final File[] files = getTempCacheDir().listFiles((dir, name) -> name.startsWith(TEMP_CHANGE_PREFIX));

        for (File file : files) {
            final String fileUUID = file.getName().replaceAll(TEMP_CHANGE_PREFIX, "").replaceAll(".json", "");

            if (UUIDParser.cmdCodeFromUUID(fileUUID).equals(cmdCode)) return file;
        }

        return null;
    }

    @Nullable
    public static File getParsedFileFromID(final String fileID) {
        return getFileFromID(fileID, "");
    }

    @Nullable
    public static File getChangeFileFromID(final String fileID) {
        return getFileFromID(fileID, TEMP_CHANGE_PREFIX);
    }

    @Nullable
    public static InputStream getAsStream(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.charAt(0) == '/') path = path.substring(1);

        return WebsiteFile.class.getResourceAsStream(String.format("/%s/%s", PATH, path));
    }

    public static byte[] getResourceAsBytes(final String path) throws IOException {
        final InputStream is = getAsStream(path);
        if (is == null) return new byte[0];

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[4];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }



    @Nullable
    private static File getFileFromID(final String fileID, final String filePrefix) {
        final File[] files = getTempCacheDir().listFiles((dir, name) -> name.startsWith(String.format("%s%s", filePrefix, fileID)));
        if (files == null || files.length == 0) return null;
        else if (files.length > 1) PlayerDataManipulator.sendMsg("Unexpected many results for parsed file ID: \"" + fileID + "\".");

        return files[0];
    }

    private enum ConfigSections {
        PATH ("web_server"),
        PORT ("port");

        final String path;
        ConfigSections(String path) {
            this.path = path;
        }
    }
}
