package com.conorsmine.net.cmds;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.NBTStoreFile;
import com.conorsmine.net.inventory.NBTItemTags;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class CmdCompletions {

    public static final String OFFLINE_PLAYERS  = "@offlinePlayers ";
    public static final String INVENTORY_PATHS  = "@inventoryPaths ";
    public static final String ITEM_IDS         = "@itemIds ";
    public static final String ITEM_STORES      = "@itemStores ";
    public static final String WORLDS           = "@all_worlds ";

    public static void registerCompletions() {
        final CommandCompletions<BukkitCommandCompletionContext> commandCompletions = PlayerDataManipulator.getINSTANCE().getCommandManager().getCommandCompletions();

        for (CompletionsEnum completions : CompletionsEnum.values()) {
            commandCompletions.registerCompletion(completions.completionName, completions.handler);
        }
    }



    private static final String CLEAN_COMP = "@|\\s";
    public enum CompletionsEnum {
        OFFLINE_PLAYERS(CmdCompletions.OFFLINE_PLAYERS.replaceAll(CLEAN_COMP, ""), c -> {
            return Arrays.stream(PlayerDataManipulator.getINSTANCE().getServer().getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
        }),
        INVENTORY_PATHS(CmdCompletions.INVENTORY_PATHS.replaceAll(CLEAN_COMP, ""), c -> {
            return new LinkedList<>(PlayerDataManipulator.getINSTANCE().getConfig().getConfigurationSection("inventory_paths").getKeys(false));
        }),
        ITEM_IDS(CmdCompletions.ITEM_IDS.replaceAll(CLEAN_COMP, ""), c -> {
            return Arrays.stream(Material.values())
                    .map((mat) -> NBTItem.convertItemtoNBT(new ItemStack(mat)))
                    .map((nbtItem) -> nbtItem.getString(NBTItemTags.ID.getTagName()))
                    .collect(Collectors.toList());
        }),
        ITEM_STORES(CmdCompletions.ITEM_STORES.replaceAll(CLEAN_COMP, ""), c -> {
            return Arrays.stream(NBTStoreFile.storeFiles())
                    .map((f) -> f.getName().replaceAll("\\.txt$", ""))
                    .collect(Collectors.toSet());
        }),
        WORLDS(CmdCompletions.WORLDS.replaceAll(CLEAN_COMP, ""), c -> {
            return Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toSet());
        });

        private final String completionName;
        private final CommandCompletions.CommandCompletionHandler<BukkitCommandCompletionContext> handler;

        CompletionsEnum(String completionName, CommandCompletions.CommandCompletionHandler<BukkitCommandCompletionContext> handler) {
            this.completionName = completionName;
            this.handler = handler;
        }
    }
}
