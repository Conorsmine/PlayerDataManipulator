package com.conorsmine.net.cmds.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.PlayerDataManipulator;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class PathWrapper {

    public static final Map<String, PathWrapper> inventoryPaths = new HashMap<>();

    static {
        final ConfigurationSection config = PlayerDataManipulator.getINSTANCE().getConfig().getConfigurationSection("inventory_paths");
        if (config != null) {
            for (String key : config.getKeys(false)) {
                final String path = config.getString(String.format("%s.path", key));
                final int size = config.getInt(String.format("%s.size", key));

                inventoryPaths.put(key, new PathWrapper(path, key, size));
            }
        }
    }

    private final String path, sectionName;
    private final int size;

    public PathWrapper(String path, String sectionName, int size) {
        this.path = path;
        this.sectionName = sectionName;
        this.size = size;
    }

    public static ContextResolver<PathWrapper, BukkitCommandExecutionContext> getContextResolver() {
        return c -> {
            final String s = c.popFirstArg();

            return inventoryPaths.get(s);
        };
    }

    public String getPath() {
        return path;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "PathWrapper{" +
                "path='" + path + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", size=" + size +
                '}';
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
