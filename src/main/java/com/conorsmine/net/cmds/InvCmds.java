package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.ParserFile;
import com.conorsmine.net.files.ItemStore;
import com.conorsmine.net.files.NBTStoreFile;
import com.conorsmine.net.messages.MsgFormatter;
import com.conorsmine.net.messages.PluginMsgs;
import com.conorsmine.net.inventory.EditorInventory;
import com.conorsmine.net.inventory.NBTInventoryUtils;
import com.conorsmine.net.inventory.NBTItemTags;
import com.conorsmine.net.inventory.PathWrapper;
import com.conorsmine.net.MojangsonUtils;
import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.data.NBTData;
import de.tr7zw.nbtapi.data.PlayerData;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@CommandAlias("pdm")
public class InvCmds extends BaseCommand {

    private static final int EXECUTIONS_MAX = 5;
    private final AtomicInteger currentExecutions = new AtomicInteger(0);
    private final List<SearchResult> foundPlayers = Collections.synchronizedList(new LinkedList<>());

    @Dependency("plugin")
    private PlayerDataManipulator pl;

    @Subcommand("open")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS + " false|true")
    @Description("Opens a players inventory")
    @CommandPermission("pdm.inv.open")
    private void openInventory(final Player player, final @NotNull OfflinePlayer target, final PathWrapper inventoryPath, @Default("true") boolean openSafely) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(player, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }
        if (target.isOnline()) ((Player) target).saveData();

        EditorInventory.Builder.createInventory(target, inventoryPath).openInventory(player);
        PlayerDataManipulator.sendMsg(player, "§7Opening " + String.format(PluginMsgs.INV_FORMAT.getMsg(), target.getName(), inventoryPath.getSectionName()));
    }

    @Subcommand("list")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Lists the contents of the players inv")
    @CommandPermission("pdm.inv.list")
    private void listInventory(final CommandSender sender, final @NotNull OfflinePlayer target, final PathWrapper inventoryPath) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }
        if (target.isOnline()) ((Player) target).saveData();

        NBTInventoryUtils.getItemNBTsFromPathAsync(
                NBTData.getOfflinePlayerData(target.getUniqueId()).getCompound(),
                inventoryPath.getPath()
        ).whenComplete((itemNBTsFromPath, e) -> MsgFormatter.sendFormattedListMsg(sender, itemNBTsFromPath));
    }

    @Subcommand("info")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Provides the nbt of the item")
    @CommandPermission("pdm.inv.info")
    private void printInfo(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath, final int slot) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }

        NBTInventoryUtils.getItemNBTsMapFromPathAsync(
                NBTData.getOfflinePlayerData(target.getUniqueId()).getCompound(),
                inventoryPath.getPath()
        ).whenComplete((map, e) -> {
            final NBTCompound compound = map.get(slot);
            if (compound == null) {
                PlayerDataManipulator.sendMsg(sender, String.format(PluginMsgs.ITEM_NOT_FOUND.getMsg(), slot));
                return;
            }

            PlayerDataManipulator.sendHeader(sender, "Info");
            PlayerDataManipulator.sendMsg(
                    sender,
                    "§7Basic info:",
                    String.format(
                            "  §7>> id: §9%s §7data: §6%d §7count: §b%d",
                            compound.getString(NBTItemTags.ID.getTagName()),
                            compound.getInteger(NBTItemTags.DAMAGE.getTagName()),
                            compound.getInteger(NBTItemTags.COUNT.getTagName())
                    ),
                    "§7Item NBT:"
            );
            sendColoredNBT(sender, compound);
        });
    }

    @Subcommand("clear")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Clears the players inventory")
    @CommandPermission("pdm.inv.clear")
    private void clearInventory(final CommandSender sender, final @NotNull OfflinePlayer target, final PathWrapper inventoryPath) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }

        final PlayerData playerData = NBTData.getOfflinePlayerData(target.getUniqueId());
        final MojangsonUtils.NBTResult result = new MojangsonUtils().setSeparator(ParserFile.getSeparator()).getCompoundFromPath(playerData.getCompound(), inventoryPath.getPath());
        final NBTCompound compound = result.getCompound();
        final String finalKey = result.getFinalKey();

        switch (compound.getType(finalKey)) {
            case NBTTagCompound:
                compound.getCompound(finalKey).clearNBT();
                break;

            case NBTTagList:
                compound.getCompoundList(finalKey).clear();
                break;
        }

        reloadPlayerData(target, playerData);
        PlayerDataManipulator.sendMsg(sender, "§7Cleared " + String.format(PluginMsgs.INV_FORMAT.getMsg(), target.getName(), inventoryPath.getSectionName()));
    }

    @Subcommand("remove")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Removes the content of the players inventory at that slots")
    @CommandPermission("pdm.inv.remove")
    private void removeFromInventory(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath, final int slot) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }

        final PlayerData playerData = NBTData.getOfflinePlayerData(target.getUniqueId());
        NBTInventoryUtils.removeNBTAsync(
                playerData.getCompound(),
                inventoryPath.getPath(),
                (nbt) -> (nbt.getInteger(NBTItemTags.SLOT.getTagName()) == slot)
        ).whenComplete((nbt, e) -> {
            if (nbt == null) {
                PlayerDataManipulator.sendMsg(sender, String.format(PluginMsgs.ITEM_NOT_FOUND.getMsg(), slot));
                return;
            }

            PlayerDataManipulator.sendMsg(sender,
                    String.format(
                            "§7Removed §b%d§7x §9%s §7from §6%s§7's §b%s§7.",
                            nbt.getInteger(NBTItemTags.COUNT.getTagName()),
                            nbt.getString(NBTItemTags.ID.getTagName()),
                            target.getName(),
                            inventoryPath.getSectionName()
                    ));

            reloadPlayerData(target, playerData);
        });
    }

    @Subcommand("add")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS + CmdCompletions.ITEM_IDS + "@range:255 @range:64")
    @Description("Adds the item to the players inventory")
    @CommandPermission("pdm.inv.add")
    private void addToInventory(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath,
                                final String itemId, @Default("0") final short data, @Default("1") final int count) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }
        if (itemId == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_ITEM_ID.getMsg()); return; }
        if (target.isOnline()) ((Player) target).saveData();

        final PlayerData playerData = NBTData.getOfflinePlayerData(target.getUniqueId());
        final MojangsonUtils.NBTResult result = new MojangsonUtils().setSeparator(ParserFile.getSeparator()).getCompoundFromPath(playerData.getCompound(), inventoryPath.getPath());
        final NBTCompound invNBT = result.getCompound();
        final String finalKey = result.getFinalKey();

        switch (invNBT.getType(finalKey)) {
            case NBTTagCompound:
                throw new UnsupportedOperationException("Cannot add an item to a single inventory!");

            case NBTTagList:
                final NBTCompoundList invList = invNBT.getCompoundList(finalKey);
                final int freeInvSlot = getFreeInvSlot(invList, inventoryPath);
                if (freeInvSlot == -Integer.MAX_VALUE) { PlayerDataManipulator.sendMsg(sender, "§cCould not find a free inv slot."); return; }

                final NBTListCompound nbtItem = invList.addCompound();
                nbtItem.setString(NBTItemTags.ID.getTagName(), itemId);
                nbtItem.setShort(NBTItemTags.DAMAGE.getTagName(), data);
                nbtItem.setInteger(NBTItemTags.COUNT.getTagName(), count);
                nbtItem.setInteger(NBTItemTags.SLOT.getTagName(), freeInvSlot);
                break;
        }

        reloadPlayerData(target, playerData);
        PlayerDataManipulator.sendMsg(sender, String.format("§7Added §9%s §7to §6%s§7's §b%s§7.", itemId, target.getName(), inventoryPath.getSectionName()));
    }

    @Subcommand("addnbt")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Adds the item nbt to the players inventory")
    @CommandPermission("pdm.inv.addnbt")
    private void addNBTToInventory(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath, final String nbt) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }
        if (nbt == null || StringUtils.isEmpty(nbt)) { PlayerDataManipulator.sendMsg(sender, "§cItem NBT cannot be null!"); return; }

        try {
            final NBTContainer nbtItem = new NBTContainer(nbt);
            if (!isItem(sender, nbtItem)) return;

            final PlayerData playerData = NBTData.getOfflinePlayerData(target.getUniqueId());
            final MojangsonUtils.NBTResult result = new MojangsonUtils().setSeparator(ParserFile.getSeparator()).getCompoundFromPath(playerData.getCompound(), inventoryPath.getPath());
            final NBTCompound invNBT = result.getCompound();
            final String finalKey = result.getFinalKey();

            switch (invNBT.getType(finalKey)) {
                case NBTTagCompound:
                    throw new UnsupportedOperationException("Cannot add an item to a single inventory!");

                case NBTTagList:
                    final NBTCompoundList invList = invNBT.getCompoundList(finalKey);
                    final int freeInvSlot = getFreeInvSlot(invList, inventoryPath);
                    if (freeInvSlot == -Integer.MAX_VALUE) { PlayerDataManipulator.sendMsg(sender, "§cCould not find a free inv slot."); return; }

                    final NBTCompound nbtItemCompound = invList.addCompound(nbtItem);
                    nbtItemCompound.setInteger(NBTItemTags.SLOT.getTagName(), freeInvSlot);
                    break;
            }

            reloadPlayerData(target, playerData);
            PlayerDataManipulator.sendMsg(sender, String.format("§7Added to §6%s§7's §b%s§7:", target.getName(), inventoryPath.getSectionName()));
            sendColoredNBT(sender, nbtItem);
        }
        catch (Exception e) { PlayerDataManipulator.sendMsg(sender, "§7Unable to parse NBT. Something must be malformed."); }
    }

    @Subcommand("addstore")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS + CmdCompletions.ITEM_STORES)
    @Description("Adds the item from a stored to the players inventory")
    @CommandPermission("pdm.inv.addstore")
    private void addItemFromStore(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath, final ItemStore itemStore) {
        if (itemStore == null) { PlayerDataManipulator.sendMsg(sender, "§7Couldn't find store with that name."); return; }
        addNBTToInventory(sender, target, inventoryPath, itemStore.getStoredNBT());
    }

    @Subcommand("search")
    @CommandCompletion(CmdCompletions.ITEM_IDS + "@range:255")
    @Description("Returns players which have the specified item")
    @CommandPermission("pdm.inv.search")
    private void searchInventories(final CommandSender sender, final String itemId, @Default("0") final short data) {
        if (itemId == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_ITEM_ID.getMsg()); return; }
        if (currentExecutions.get() > 0) {
            PlayerDataManipulator.sendMsg(sender, "§cAlready searching for an item, please wait until the search is complete to begin a new one.");
            return;
        }

        PlayerDataManipulator.sendMsg(sender, "§7This might take a while...");

        final Queue<OfflinePlayer> offlinePlayers = new ConcurrentLinkedQueue<>(Arrays.asList(pl.getServer().getOfflinePlayers()));

        foundPlayers.clear();
        runSearchQueryAsync(offlinePlayers, itemId, data)
                .whenComplete((v, e) -> {
                    PlayerDataManipulator.sendMsg(sender,
                            String.format("§7The following players contain §9%s §6%s§7:", itemId, (data < 0) ? "" : data));

                    foundPlayers.forEach((foundPlayer) ->
                            PlayerDataManipulator.sendMsg(sender,
                            String.format("  §7>> §6%s §7in §b%s",
                            foundPlayer.getPlayer().getName(),
                            foundPlayer.getInventoryPath().getSectionName())));
                });
    }

    @Subcommand("store")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.INVENTORY_PATHS)
    @Description("Stores the items NBT in a file for later use")
    @CommandPermission("pdm.inv.store")
    private void storeItem(final CommandSender sender, final OfflinePlayer target, final PathWrapper inventoryPath, final int slot) {
        if (inventoryPath == null) { PlayerDataManipulator.sendMsg(sender, PluginMsgs.MISSING_PATH_WRAPPER.getMsg()); return; }

        NBTInventoryUtils.getItemNBTsMapFromPathAsync(
                NBTData.getOfflinePlayerData(target.getUniqueId()).getCompound(),
                inventoryPath.getPath()
        ).whenComplete((map, e) -> {
            final NBTCompound compound = map.get(slot);
            if (compound == null) { PlayerDataManipulator.sendMsg(sender, String.format(PluginMsgs.ITEM_NOT_FOUND.getMsg(), slot)); return; }

            final String fileName = NBTStoreFile.storeNBT(sender, target, compound);
            if (fileName == null) { PlayerDataManipulator.sendMsg(sender, "§cCouldn't save players item nbt to file."); }
            else {
                sendColoredNBT(sender, compound);
                PlayerDataManipulator.sendMsg(sender, String.format("§7Saved players item nbt to file §6%s§7.", fileName));
            }
        });
    }




    private void reloadPlayerData(final OfflinePlayer player, final PlayerData playerData) {
        playerData.saveChanges();
        if (player.isOnline()) ((Player) player).loadData();
    }

    private void sendColoredNBT(final CommandSender sender, final NBTCompound nbt) {
        PlayerDataManipulator.sendMsg(
                sender,
                TextComponent.toLegacyText(new MojangsonUtils().getInteractiveMojangson(nbt, ""))
        );
    }

    private boolean isItem(final CommandSender sender, final NBTCompound nbt) {
        boolean isItem = true;
        if (!nbt.hasKey(NBTItemTags.ID.getTagName())) { PlayerDataManipulator.sendMsg(sender, "§7NBT is missing §6ID §7tag!"); isItem = false; }
        if (!nbt.hasKey(NBTItemTags.DAMAGE.getTagName())) { PlayerDataManipulator.sendMsg(sender, "§7NBT is missing §6DAMAGE §7tag!"); isItem = false; }
        if (!nbt.hasKey(NBTItemTags.COUNT.getTagName())) { PlayerDataManipulator.sendMsg(sender, "§7NBT is missing §6COUNT §7tag!"); isItem = false; }

        return isItem;
    }

    private int getFreeInvSlot(final NBTCompoundList inv, final PathWrapper inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            int finalI = i;
            if (inv.stream().anyMatch((nbt) -> nbt.getInteger(NBTItemTags.SLOT.getTagName()) == finalI)) {
                continue;
            }

            return i;
        }

        return -Integer.MAX_VALUE;  // Indicating none was found
    }

    private CompletableFuture<Void> runSearchQueryAsync(final Queue<OfflinePlayer> queue, final String itemID, final short itemData) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        recursiveSearchQuery(future, queue, itemID, itemData);
        return future;
    }

    private void recursiveSearchQuery(final CompletableFuture<Void> future, final Queue<OfflinePlayer> queue, final String itemID, final short itemData) {
        if (queue.size() == 0) { currentExecutions.set(0); future.complete(null); return; }
        if (queue.size() <= EXECUTIONS_MAX && currentExecutions.get() > 0) return;
        if (currentExecutions.get() >= EXECUTIONS_MAX) return;
        currentExecutions.incrementAndGet();

        if (currentExecutions.get() < EXECUTIONS_MAX) recursiveSearchQuery(future, queue, itemID, itemData);

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> {
            final OfflinePlayer player = queue.poll();
            if (player == null) return;

            final PlayerData playerData = NBTData.getOfflinePlayerData(player.getUniqueId());

            for (PathWrapper path : PathWrapper.inventoryPaths.values()) {
                final List<NBTCompound> inventoryNBT = NBTInventoryUtils.getItemNBTsFromPath(
                        playerData.getCompound(),
                        path.getPath()
                );

                for (NBTCompound nbtItem : inventoryNBT) {

                    if (!nbtItem.getString(NBTItemTags.ID.getTagName()).equals(itemID)) continue;
                    if (itemData >= 0 && nbtItem.getShort(NBTItemTags.DAMAGE.getTagName()) != itemData) continue;

                    foundPlayers.add(new SearchResult(player, path));
                    break;
                }
            }


            currentExecutions.decrementAndGet();
            recursiveSearchQuery(future, queue, itemID, itemData);
        });
    }



    private static class SearchResult implements Comparator<String> {

        private final OfflinePlayer player;
        private final PathWrapper inventoryPath;

        public SearchResult(OfflinePlayer player, PathWrapper inventoryPath) {
            this.player = player;
            this.inventoryPath = inventoryPath;
        }

        public OfflinePlayer getPlayer() {
            return player;
        }

        public PathWrapper getInventoryPath() {
            return inventoryPath;
        }

        @Override
        public int compare(String o1, String o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
        }
    }
}
