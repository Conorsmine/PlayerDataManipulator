package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;
import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NBTStoreFile implements ReloadableFile {

    public static final String DIR_NAME = "store";
    public static final String FILE_FORMAT = "{player_name}-{date_day}-{date_time}.txt";    // Eg. "Conorsmine-08.06.2023-14:17:25"
    private static final DateTimeFormatter DATE_DAY = DateTimeFormatter.ofPattern("dd_MM_yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH_mm_ss");

    private File dirFile;

    private final PlayerDataManipulator pl;

    public NBTStoreFile(PlayerDataManipulator pl) {
        this.pl = pl;

        reload(pl.getServer().getConsoleSender());
    }

    @Nullable
    public String storeNBT(@Nullable final CommandSender sender, final OfflinePlayer player, final NBTCompound nbt) {
        final String child = formatFileName(player);
        final File storeFile = new File(dirFile, child);

        try {
            if (!storeFile.createNewFile()) { createFileErr(sender, player); return null; }
            final FileWriter writer = new FileWriter(storeFile);

            writer.write(nbt.toString());
            writer.close();
        } catch (IOException e) {
            createFileErr(sender, player);
            e.printStackTrace();
            return null;
        }

        return child;
    }

    public File[] storeFiles() {
        final File[] files = dirFile.listFiles((file) -> (file.isFile() && file.getName().matches(".+\\.txt$")));
        return (files == null) ? new File[0] : files;
    }

    private static String formatFileName(final OfflinePlayer player) {
        final LocalDateTime now = LocalDateTime.now();

        return FILE_FORMAT
                .replaceAll("\\{player_name}", player.getName())
                .replaceAll("\\{date_day}", DATE_DAY.format(now))
                .replaceAll("\\{date_time}", DATE_TIME.format(now));
    }

    private static void createFileErr(CommandSender sender, final OfflinePlayer player) {
        if (sender == null) sender = Bukkit.getConsoleSender();

        throw new RuntimeException(String.format("§cCould not save §6%s§c's nbt data to file.", player.getName()));
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        this.dirFile = new File(pl.getDataFolder(), DIR_NAME);
        this.dirFile.mkdirs();
    }
}
