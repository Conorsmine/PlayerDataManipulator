package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.Properties;
import com.conorsmine.net.utils.LazyConfig;
import com.conorsmine.net.webserver.UUIDParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.io.*;

public class WebsiteFile implements ReloadableFile {

    public static final String HTML_PATH = "web", TEMP_CACHE = "temp_cache", TEMP_CHANGE_PREFIX = "change-";

    private final PlayerDataManipulator pl;
    private final LazyConfig<Integer> port;

    public WebsiteFile(PlayerDataManipulator pl) {
        this.pl = pl;

        this.port = new LazyConfig<>(
                () -> pl.getConfig().getInt(String.format("%s.%s", Properties.CONFIG_WEBSITE_SECTION, Properties.CONFIG_WEBSITE_PORT)), 8806,
                (val) -> (val == 0), (val) -> "§7\"§6%d§7\" is §enot §7a valid port for the web-editor. Please select something else."
        );
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        this.port.reset();
    }

    public int getPort() {
        return this.port.get();
    }

    public File getTempCacheDir() {
        final File dir = new File(pl.getDataFolder(), TEMP_CACHE);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();

        return dir;
    }

    public File createTempParsedFile(final String fileID) throws IOException {
        final File file = new File(getTempCacheDir(), String.format("%s.json", fileID));
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        return file;
    }

    @SuppressWarnings("UnusedReturnValue")
    public File createTempChangeFile(final String fileID) throws IOException {
        final File file = new File(getTempCacheDir(), String.format("%s%s.json", TEMP_CHANGE_PREFIX, fileID));
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        final FileWriter writer = new FileWriter(file);
        writer.write("{}");
        writer.flush();
        writer.close();

        return file;
    }

    public void clearTempCache() {
        final File[] files = getTempCacheDir().listFiles();
        if (files == null) return;

        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public void setJSONOfChangeFile(final String fileID, final JSONObject json) {
        final File changeFile = getChangeFileFromID(fileID);

        FileUtils.saveJsonToFile(changeFile, json);
    }

    @Nullable
    public File getChangeFileFromCommandCode(final String cmdCode) {
        final File[] files = getTempCacheDir().listFiles((dir, name) -> name.startsWith(TEMP_CHANGE_PREFIX));
        if (files == null) return null;

        for (File file : files) {
            final String fileUUID = file.getName().replaceAll(TEMP_CHANGE_PREFIX, "").replaceAll(".json", "");

            if (UUIDParser.cmdCodeFromUUID(fileUUID).equals(cmdCode)) return file;
        }

        return null;
    }

    @Nullable
    public File getParsedFileFromID(final String fileID) {
        return getFileFromID(fileID, "");
    }

    @Nullable
    public File getChangeFileFromID(final String fileID) {
        return getFileFromID(fileID, TEMP_CHANGE_PREFIX);
    }

    @Nullable
    private File getFileFromID(final String fileID, final String filePrefix) throws UnsupportedOperationException {
        final File[] files = getTempCacheDir().listFiles((dir, name) -> name.startsWith(String.format("%s%s", filePrefix, fileID)));
        if (files == null || files.length == 0) return null;
        else if (files.length > 1) pl.sendMsg("Unexpected many results for parsed file ID: \"" + fileID + "\".");

        return files[0];
    }

    @Nullable
    public static InputStream getAsStream(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.charAt(0) == '/') path = path.substring(1);

        return WebsiteFile.class.getResourceAsStream(String.format("/%s/%s", HTML_PATH, path));
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

}
