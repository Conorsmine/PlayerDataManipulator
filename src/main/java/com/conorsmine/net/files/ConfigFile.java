package com.conorsmine.net.files;

import com.conorsmine.net.InventoryPath;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.Properties;
import com.conorsmine.net.mojangson.StringUtils;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import com.conorsmine.net.utils.LazyConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigFile implements ReloadableFile {

    private final PlayerDataManipulator pl;
    private final LazyConfig<String> parserSeparator;
    private final LazyConfig<Integer> searchMaxExecutions;

    private final LazyConfig<Map<String, InventoryPath>> inventoryPaths;

    public ConfigFile(PlayerDataManipulator pl) {
        this.pl = pl;

        this.parserSeparator = new LazyConfig<>(
                () -> pl.getConfig().getString(Properties.CONFIG_SEPARATOR.toString()), "##",
                (val) -> "§7\"§6%s§7\" is §enot §7a valid separator. Please select something else."
        );

        this.searchMaxExecutions = new LazyConfig<>(
                () -> pl.getConfig().getInt(Properties.CONFIG_WORKERS.toString()), 1, (val) -> ((val <= 0) || (val > 100)),
                (val) -> "§7\"§6%d§7\" is §enot§7 a valid amount of workers. Please select something between 1 and 100."
        );

        this.inventoryPaths = new LazyConfig<>(
                this::configParseSupplier, new HashMap<>(), ConfigFile::checkInventoryPathConfigSection,
                (map) -> "Check the thrown error message for more information!"
        );
    }

    @Override
    public void reload(@NotNull final CommandSender sender) {
        this.parserSeparator.reset();
        this.searchMaxExecutions.reset();
        this.inventoryPaths.reset();
    }

    public String getSeparator() {
        return this.parserSeparator.get();
    }

    public int getMaxSearchExecutors() {
        return this.searchMaxExecutions.get();
    }

    public InventoryPath getInventoryPath(final String pathSection) {
        return inventoryPaths.get().get(pathSection);
    }

    public List<InventoryPath> getInventoryPaths() {
        return new ArrayList<>(inventoryPaths.get().values());
    }



    private Map<String, InventoryPath> configParseSupplier() {
        final Map<String, InventoryPath> pathMap = new HashMap<>();
        final ConfigurationSection config = pl.getConfig().getConfigurationSection("inventory_paths");
        if (config == null) return pathMap;
        for (String key : config.getKeys(false)) {
            final String path = config.getString(String.format("%s.%s", key, Properties.CONFIG_INV_PATH));
            final int size = config.getInt(String.format("%s.%s", key, Properties.CONFIG_INV_SIZE));

            pathMap.put(key, new InventoryPath(new NBTPathBuilder(pl.MOJANGSON).parseString(path).create(), key, size));
        }

        return pathMap;
    }
    private static boolean checkInventoryPathConfigSection(final Map<String, InventoryPath> map) {
        for (InventoryPath invPath : map.values()) {
            boolean isInvalid = false;
            final StringBuilder builder = new StringBuilder();

            builder.append(invPath.getSectionName());
            if (StringUtils.isNothingString(invPath.getSectionName())) {
                builder.append("    <-- The section name may not be empty or null!");
                isInvalid = true;
            }

            builder.append("\n").append(invPath.getPath());
            if (StringUtils.isNothingString(invPath.getPath().toString())) {
                builder.append("    <-- The path may not be empty or null!");
                isInvalid = true;
            }

            builder.append("\n").append(invPath.getSize());
            if (invPath.getSize() <= 0) {
                builder.append("    <-- The size of the inventory may not be negative!");
                isInvalid = true;
            }

            builder.append("\n");

            if (isInvalid) throw new ConfigParseError(builder.toString());
        }
        return false;
    }



    public static class ConfigParseError extends Error {
        public ConfigParseError(String message) {
            super(message);
        }
    }
}
