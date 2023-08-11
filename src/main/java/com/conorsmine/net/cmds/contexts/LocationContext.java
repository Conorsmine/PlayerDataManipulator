package com.conorsmine.net.cmds.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.files.NBTStoreFile;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LocationContext {

    public static ContextResolver<Location, BukkitCommandExecutionContext> getContextResolver(@NotNull final JavaPlugin pl) {
        return c -> {
            try {
                final String worldName = c.popFirstArg();
                final double x = Double.parseDouble(c.popFirstArg());
                final double y = Double.parseDouble(c.popFirstArg());
                final double z = Double.parseDouble(c.popFirstArg());

                return new Location(pl.getServer().getWorld(worldName), x, y, z);
            }
            catch (NumberFormatException ignored) { ; }

            return null;
        };
    }
}
