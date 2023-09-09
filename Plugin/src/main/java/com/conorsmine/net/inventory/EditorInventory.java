package com.conorsmine.net.inventory;

import com.conorsmine.net.InventoryPath;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.items.NBTItemTags;
import com.conorsmine.net.messages.PluginMsgs;
import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.NBTQueryResult;
import com.conorsmine.net.mojangson.data.INBTData;
import com.conorsmine.net.mojangson.data.NBTCompoundData;
import com.conorsmine.net.mojangson.data.NBTCompoundListData;
import com.conorsmine.net.mojangson.data.NBTDataType;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.utils.NBTInventoryUtils;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.data.NBTData;
import de.tr7zw.nbtapi.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class EditorInventory {

    public static final int IGNORE_SLOT = -1;       // Slots with this int will be ignored
    private static final ItemStack IGNORE_ITEM;      // slots with the value of "IGNORE_SLOT" will be filled with this
    private static final ItemStack PLACEHOLDER_ITEM; // Item for when the actual item couldn't be loaded, because it's modded or has some weird data

    static {
        IGNORE_ITEM = PlayerDataManipulator.VERSION_SUPPORT.getInventoryEditorIgnoreItem();
        final ItemMeta ignoreItemItemMeta = IGNORE_ITEM.getItemMeta();
        ignoreItemItemMeta.setDisplayName("§cINACCESSIBLE");
        IGNORE_ITEM.setItemMeta(ignoreItemItemMeta);

        PLACEHOLDER_ITEM = new ItemStack(Material.BARRIER);
        final ItemMeta placeholderItemItemMeta = PLACEHOLDER_ITEM.getItemMeta();
        placeholderItemItemMeta.setDisplayName("§cPLACEHOLDER");
        PLACEHOLDER_ITEM.setItemMeta(placeholderItemItemMeta);
    }

    private static final Map<UUID, EditorInventory> currentEditors = new HashMap<>();

    private final OfflinePlayer player;
    private final InventoryPath inventoryPath;
    private final String title;
    private final int[] invSlots;
    private final Map<Integer, NBTCompound> itemNBTMap;

    public EditorInventory(@NotNull OfflinePlayer player, @NotNull InventoryPath inventoryPath, int[] invSlots, @NotNull final NBTInventoryUtils invUtils) {
        this.player = player;
        this.inventoryPath = inventoryPath;
        this.title = String.format(PluginMsgs.INV_FORMAT.getMsg(), player.getName(), inventoryPath.getSectionName());
        this.invSlots = invSlots;

        this.itemNBTMap = invUtils.getItemNBTsMapFromPath(
                NBTData.getOfflinePlayerData(player.getUniqueId()).getCompound(),
                inventoryPath.getPath()
        );
    }

    public int idAtSlot(int slot) {
        return invSlots[slot];
    }

    public void openInventory(final @NotNull Player player) {
        final Inventory inventory = Bukkit.createInventory(null, invSlots.length, title);
        populateInventory(inventory);

        player.openInventory(inventory);
        currentEditors.put(player.getUniqueId(), this);
    }

    private void populateInventory(final Inventory inventory) {
        for (int i = 0; i < invSlots.length; i++) {
            final int slotId = invSlots[i];
            if (slotId == -1) {
                inventory.setItem(i, IGNORE_ITEM);
                continue;
            }

            final NBTCompound comp = itemNBTMap.get(slotId);
            ItemStack item = null;
            if (comp != null) { item = NBTItem.convertNBTtoItem(comp); }

            if (item != null && item.getType() == Material.AIR) item = applyDataToPlaceholder(comp);
            inventory.setItem(i, item);
        }
    }

    private void applyChanges(final @NotNull Inventory newInv, final @NotNull MojangsonUtils utils) {
        for (int i = 0; i < newInv.getContents().length; i++) {
            final ItemStack item = newInv.getItem(i);
            final int slotId = invSlots[i];

            if (slotId == -1) continue;
            if (item == null || item.getType() == Material.AIR) {
                final NBTCompound compound = itemNBTMap.get(slotId);
                if (compound == null) continue;

                itemNBTMap.remove(slotId);
                continue;
            }
            if (isPlaceholder(item)) continue;

            itemNBTMap.put(slotId, NBTItem.convertItemtoNBT(item));
        }

        final PlayerData playerData = NBTData.getOfflinePlayerData(player.getUniqueId());
        final NBTCompound compound = playerData.getCompound();
        final NBTPath nbtPath = inventoryPath.getPath();
        final NBTQueryResult result = utils.getDataFromPathSneakyThrow(new NBTCompoundData(compound), nbtPath);
        if (result == null) return;

        final INBTData<?> invData = result.getData();
        if (!NBTDataType.isList(invData.getType()))
            throw new UnsupportedOperationException("The EditorInventory des not support single-item inventories!\nReport this to the plugin dev on GitHub!");

        final NBTCompoundList inventory = ((NBTCompoundListData) invData).getData();
        inventory.clear();

        itemNBTMap.forEach((slot, nbt) -> {
            nbt.setInteger(NBTItemTags.SLOT.getTagName(), slot);
            inventory.addCompound(nbt);
        });

        playerData.saveChanges();
        if (player.isOnline()) ((Player) player).loadData();
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public InventoryPath getInventoryPath() {
        return inventoryPath;
    }

    public static boolean playerHasEditorOpen(final Player player) {
        return currentEditors.containsKey(player.getUniqueId());
    }

    @Nullable
    public static EditorInventory getPlayersEditor(final Player player) {
        return currentEditors.get(player.getUniqueId());
    }

    public static void playerCloseEditor(final Player player, final Inventory inventory, final MojangsonUtils utils) {
        final EditorInventory editorInventory = currentEditors.get(player.getUniqueId());
        if (editorInventory == null) return;
        editorInventory.applyChanges(inventory, utils);

        currentEditors.remove(player.getUniqueId());
    }

    private static ItemStack applyDataToPlaceholder(final NBTCompound itemNBT) {
        final ItemStack placeholder = PLACEHOLDER_ITEM.clone();

        final ItemMeta itemMeta = placeholder.getItemMeta();
        itemMeta.setLore(PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().nbtToVersionLoreData(itemNBT));
        placeholder.setItemMeta(itemMeta);
        placeholder.setAmount(itemNBT.getInteger(NBTItemTags.COUNT.getTagName()));

        return placeholder;
    }

    private static boolean isPlaceholder(final ItemStack item) {
        if (item == null) return false;
        if (item == PLACEHOLDER_ITEM) return true;

        return (item.getType() == PLACEHOLDER_ITEM.getType());
    }





    public static class Builder {

        private final PlayerDataManipulator pl;

        public Builder(PlayerDataManipulator pl) {
            this.pl = pl;
        }

        public EditorInventory createInventory(final OfflinePlayer offlinePlayer, final InventoryPath inventoryPath) {
            final List<NBTCompound> itemNBTList = pl.INV_UTILS.getItemNBTsFromPath(
                    NBTData.getOfflinePlayerData(offlinePlayer.getUniqueId()).getCompound(),
                    inventoryPath.getPath()
            );

            return new EditorInventory(
                    offlinePlayer,
                    inventoryPath,
                    createSlotBoard(inventoryPath.getSize(), itemNBTList),
                    pl.INV_UTILS
            );
        }

        private static int[] createSlotBoard(int estimatedSize, final List<NBTCompound> itemNBTList) {
            final int invSize = getInvSize(estimatedSize, itemNBTList);
            final int[] slots = new int[invSize];
            Arrays.fill(slots, IGNORE_SLOT);

            final int regularSize = getFormattedInvSize(estimatedSize);
            final List<NBTCompound> abnormalSlots = getAbnormalItemSlotNBT(estimatedSize, itemNBTList);

            int pointer = 0;

            for (int i = pointer; i < estimatedSize; i++) { slots[i] = i; }
            pointer = regularSize;

            // Adding abnormal items to the inv size
            if (!abnormalSlots.isEmpty()) {
                pointer += 9;

                final List<Integer> sortedSlots = abnormalSlots.stream()
                        .map((nbt) -> nbt.getInteger(NBTItemTags.SLOT.getTagName()))
                        .sorted()
                        .collect(Collectors.toList());

                for (int i = pointer; i < pointer + sortedSlots.size(); i++) {
                    slots[i] = sortedSlots.get(i - pointer);
                }
            }

            return slots;
        }

        private static int getInvSize(int estimateSize, final List<NBTCompound> itemNBTList) {
            int size = getFormattedInvSize(estimateSize);
            final int abnormalItemSlotAmount = getAbnormalItemSlotNBT(size, itemNBTList).size();

            if (abnormalItemSlotAmount > 0) {
                size += getFormattedInvSize(abnormalItemSlotAmount);    // Might need to extend the inventory to compensate for items in "abnormal" slots
                size += 9;  // Adding another row for a separator
            }

            return size;
        }

        /**
         * @return An integer which is a multiple of 9
         */
        private static int getFormattedInvSize(int size) {
            if ((size % 9) == 0) return size;
            return (size + (9 - (size % 9)));
        }

        private static List<NBTCompound> getAbnormalItemSlotNBT(final int estimatedSize, final List<NBTCompound> itemNBTList) {
            return itemNBTList.stream()
                    .filter((nbt) -> {
                        final int slot = nbt.getInteger(NBTItemTags.SLOT.getTagName());
                        return (slot < 0 || slot >= estimatedSize);
                    })
                    .collect(Collectors.toList());
        }
    }
}
