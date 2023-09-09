package com.conorsmine.net.versions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.items.NBTItemTags;
import com.conorsmine.net.items.VersionItemWrapper;
import com.conorsmine.net.items.VersionItems;
import com.conorsmine.net.util.APIMsgFormatter;
import com.conorsmine.net.util.Result;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Version_1_12_2_Items implements VersionItems {

    /**
     * Issue is, 1.12 items might consume 3 arguments:
     * <br> - The item id; Required
     * <br> - The item damage/data; Optional
     * <br> - The item count; Optional
     */
    @Override
    public ContextResolver<@Nullable VersionItemWrapper, BukkitCommandExecutionContext> versionItemContextResolver(@NotNull JavaPlugin pl) {
        return c -> {
            final String itemID = c.popFirstArg();
            final String itemCountStr = c.popFirstArg();
            final String itemDataStr = c.popFirstArg();

            int count = 1;
            try { count = Integer.parseInt(itemCountStr); }
            catch (Exception ignored) { }

            short damage = 0;
            try { damage = Short.parseShort(itemDataStr); }
            catch (Exception ignored) { }

            return new Version_1_12_2_Item_Wrapper(itemID, count, damage);
        };
    }

    @Override
    public String versionItemCmdFormat() {
        return "<itemMaterial> [damage] [amount]";
    }

    @Override
    public String getVersionSearchCmdFormat() {
        return "<itemID> [data]";
    }

    @Override
    public List<String> nbtToVersionLoreData(@NotNull NBTCompound itemNBT) {
        return Arrays.asList(
                String.format("§7Item id:   §9%s", itemNBT.getString(NBTItemTags.ID.getTagName())),
                String.format("§7Item data: §6%s", itemNBT.getShort(NBTItemTags.DAMAGE.getTagName()))
        );
    }

    @Override
    public String consoleItemVersionDataFromNBT(@NotNull NBTCompound itemNBT) {
        return String.format(
                "§7  >> §9%s §6%s §b%s",
                itemNBT.getString(NBTItemTags.ID.getTagName()),
                itemNBT.getShort(NBTItemTags.DAMAGE.getTagName()),
                itemNBT.getInteger(NBTItemTags.SLOT.getTagName())
        );
    }

    @Override
    public String[] playerItemVersionDataFromNBT(@NotNull List<NBTCompound> itemNBTs) {
        final int longestNameWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getString(NBTItemTags.ID.getTagName())) + 8;
        final int longestDamageWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString()) + 24;

        final String[] result = new String[itemNBTs.size()];

        for (int i = 0; i < itemNBTs.size(); i++) {
            final NBTCompound nbt = itemNBTs.get(i);
            final String itemId = nbt.getString(NBTItemTags.ID.getTagName());
            final String itemDamage = nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString();


            final String spacesToDamageData = APIMsgFormatter.getEmptyStringFromWidth(longestNameWidth - APIMsgFormatter.getWidth(itemId, true));
            final String spacesToSlot = APIMsgFormatter.getEmptyStringFromWidth(longestDamageWidth - APIMsgFormatter.getWidth(itemDamage, true));

            result[i] = String.format(
                    "§7  >> §9%s%s§6%s%s§b%s",
                    itemId,         spacesToDamageData,
                    itemDamage,     spacesToSlot,
                    nbt.getInteger(NBTItemTags.SLOT.getTagName())
            );
        }

        return result;
    }

    @Override
    public String consoleItemDataHeader() {
        return "§9Items §6Data §bSlot";
    }

    @Override
    public String playerItemDataHeader(@NotNull List<NBTCompound> itemNBTs) {
        final int longestNameWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getString(NBTItemTags.ID.getTagName())) + 8;
        final int longestDamageWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString()) + 24;

        final String toItemSpaces = APIMsgFormatter.getEmptyStringFromWidth(APIMsgFormatter.getWidth("  >> ", false));
        final String toDataSpaces = APIMsgFormatter.getEmptyStringFromWidth(longestNameWidth - APIMsgFormatter.getWidth("Item", false));
        final String toSlotSpaces = APIMsgFormatter.getEmptyStringFromWidth(longestDamageWidth - APIMsgFormatter.getWidth("Data", false));

        return String.format("%s§9Items%s§6Data%s§bSlot", toItemSpaces, toDataSpaces, toSlotSpaces);
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean partialMath(@NotNull NBTCompound itemNBT, String itemID, short dataVal) {
        if (!itemNBT.getString(NBTItemTags.ID.getTagName()).equals(itemID)) return false;
        if (dataVal >= 0 && itemNBT.getShort(NBTItemTags.DAMAGE.getTagName()) != dataVal) return false;
        return true;
    }

    @Override
    public Result<@Nullable NBTCompound> itemNBTToItem(@NotNull String nbt) {
        try {
            final NBTContainer nbtItem = new NBTContainer(nbt);
            final Result<Boolean> validItemNBT = isValidItemNBT(nbtItem);
            if (!validItemNBT.wasSuccessful()) return new Result<>(null, false, validItemNBT.getResultMsg());

            final NBTCompound itemComp = createItemNBT(
                    nbtItem.getString(NBTItemTags.ID.getTagName()),
                    nbtItem.getInteger(NBTItemTags.COUNT.getTagName()),
                    nbtItem.getShort(NBTItemTags.DAMAGE.getTagName())
            );

            return new Result<>(itemComp);
        }
        catch (Exception e) { return new Result<>(null, false, "§7Unable to parse NBT. Something must be malformed."); }
    }

    @Override
    public Result<Boolean> isValidItemNBT(@NotNull NBTCompound nbt) {
        if (!nbt.hasKey(NBTItemTags.ID.getTagName())) return new Result<>(false, false, "§7NBT is missing §6ID §7tag!");
        if (!nbt.hasKey(NBTItemTags.DAMAGE.getTagName())) return new Result<>(false, false, "§7NBT is missing §6DAMAGE §7tag!");
        if (!nbt.hasKey(NBTItemTags.COUNT.getTagName())) return new Result<>(false, false, "§7NBT is missing §6COUNT §7tag!");

        return new Result<>(true);
    }

    @Override
    public void addDataToCreatedItem(@NotNull NBTCompound createdNBT, @NotNull NBTCompound dataComp) {
        createdNBT.setString(NBTItemTags.ID.getTagName(), dataComp.getString(NBTItemTags.ID.getTagName()));
        createdNBT.setInteger(NBTItemTags.COUNT.getTagName(), dataComp.getInteger(NBTItemTags.COUNT.getTagName()));
        createdNBT.setShort(NBTItemTags.DAMAGE.getTagName(), dataComp.getShort(NBTItemTags.DAMAGE.getTagName()));
    }

    static NBTCompound createItemNBT(String itemID, int count, short data) {
        final NBTContainer nbtContainer = new NBTContainer();
        nbtContainer.setString(NBTItemTags.ID.getTagName(), itemID);
        nbtContainer.setInteger(NBTItemTags.COUNT.getTagName(), count);
        nbtContainer.setShort(NBTItemTags.DAMAGE.getTagName(), data);

        return nbtContainer;
    }
}
