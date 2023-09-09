package com.conorsmine.net.cmds.util;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.conorsmine.net.InventoryPath;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.items.NBTItemTags;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CmdCompletions {

    public static final String OFFLINE_PLAYERS = "@offlinePlayers ";
    public static final String INVENTORY_PATHS = "@inventoryPaths ";
    public static final String ITEM_IDS = "@itemIds ";
    public static final String ITEM_STORES = "@itemStores ";
    public static final String WORLDS = "@all_worlds ";

    private final PlayerDataManipulator pl;

    public CmdCompletions(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    public void registerCompletions() {
        final CommandCompletions<BukkitCommandCompletionContext> commandCompletions = pl.getCommandManager().getCommandCompletions();

        for (CompletionsEnum completions : CompletionsEnum.values()) {
            commandCompletions.registerCompletion(completions.completionName, completions.getCompletion(pl));
        }
    }


    private static final String CLEAN_COMP = "@|\\s";

    private static final List<String> CACHED_ITEM_IDS = Arrays.stream(Material.values())
            .map((mat) -> NBTItem.convertItemtoNBT(new ItemStack(mat)))
            .map((nbtItem) -> nbtItem.getString(NBTItemTags.ID.getTagName()))
            .collect(Collectors.toList());

    private enum CompletionsEnum {
        @SuppressWarnings({"ComparatorMethodParameterNotUsed", "DataFlowIssue"})
        OFFLINE_PLAYERS(CmdCompletions.OFFLINE_PLAYERS.replaceAll(CLEAN_COMP, ""), pl ->
                Arrays.stream(pl.getServer().getOfflinePlayers())
                .sorted((p1, p2) -> (p1.isOnline()) ? -1 : 1)
                .map(OfflinePlayer::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList())),
        INVENTORY_PATHS(CmdCompletions.INVENTORY_PATHS.replaceAll(CLEAN_COMP, ""), pl -> {
            return pl.CONF.getInventoryPaths().stream().map(InventoryPath::getSectionName).collect(Collectors.toList());
        }),
        ITEM_IDS(CmdCompletions.ITEM_IDS.replaceAll(CLEAN_COMP, ""), c -> CACHED_ITEM_IDS),
        ITEM_STORES(CmdCompletions.ITEM_STORES.replaceAll(CLEAN_COMP, ""), pl -> {
            return Arrays.stream(pl.ITEM_STORES.storeFiles())
                    .map((f) -> f.getName().replaceAll("\\.txt$", ""))
                    .collect(Collectors.toSet());
        }),
        WORLDS(CmdCompletions.WORLDS.replaceAll(CLEAN_COMP, ""), pl -> {
            return pl.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toSet());
        });

        private final String completionName;

        private final Function<PlayerDataManipulator, Collection<String>> handlerSupplier;

        CompletionsEnum(String completionName, Function<PlayerDataManipulator, Collection<String>> handlerSupplier) {
            this.completionName = completionName;
            this.handlerSupplier = handlerSupplier;
        }

        CommandCompletions.CommandCompletionHandler<BukkitCommandCompletionContext> getCompletion(final PlayerDataManipulator pl) {
            return (c) -> handlerSupplier.apply(pl);
        }
    }
}
