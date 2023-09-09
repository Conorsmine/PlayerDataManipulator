package com.conorsmine.net.cmds.util;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.InventoryPath;
import com.conorsmine.net.ItemStore;
import com.conorsmine.net.PlayerDataManipulator;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class CmdContexts {

    public static ContextResolver<Location, BukkitCommandExecutionContext> getLocationContextResolver(@NotNull final JavaPlugin pl) {
        return c -> {
            try {
                final String worldName = c.popFirstArg();
                final double x = Double.parseDouble(c.popFirstArg());
                final double y = Double.parseDouble(c.popFirstArg());
                final double z = Double.parseDouble(c.popFirstArg());

                return new Location(pl.getServer().getWorld(worldName), x, y, z);
            }
            catch (NumberFormatException ignored) { }

            return null;
        };
    }

    public static ContextResolver<InventoryPath, BukkitCommandExecutionContext> getInventoryPathContextResolver(@NotNull final PlayerDataManipulator pl) {
        return c -> pl.CONF.getInventoryPath(c.popFirstArg());
    }

    public static ContextResolver<ItemStore, BukkitCommandExecutionContext> getItemStoreContextResolver(@NotNull final PlayerDataManipulator pl) {
        return c -> {
            final String s = c.popFirstArg();

            final File[] files = pl.ITEM_STORES.storeFiles();
            for (File file : files) {
                if (file.getName().replaceAll("\\.txt$", "").equalsIgnoreCase(s)) return parseStore(file);
            }

            return null;
        };
    }

    private static ItemStore parseStore(final File file) {
        try {
            final StringBuilder nbt = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new FileReader(file));

            String in = reader.readLine();
            while (in != null) { nbt.append(in); in = reader.readLine(); }

            reader.close();
            return new ItemStore(nbt.toString());
        } catch (IOException ignore) { }

        return null;
    }
}
