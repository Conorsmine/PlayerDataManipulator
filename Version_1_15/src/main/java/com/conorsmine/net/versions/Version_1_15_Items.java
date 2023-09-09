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

import java.util.Collections;
import java.util.List;

public class Version_1_15_Items implements VersionItems {

    /**
     * 1.13+ items might consume 2 arguments:
     * <br> - The item id; Required
     * <br> - The item count; Optional
     */
    @Override
    public ContextResolver<@Nullable VersionItemWrapper, BukkitCommandExecutionContext> versionItemContextResolver(@NotNull JavaPlugin pl) {
        return c -> {
            final String itemID = c.popFirstArg();
            final String itemCountStr = c.popFirstArg();

            int count = 1;
            try { count = Integer.parseInt(itemCountStr); }
            catch (Exception ignored) { }
            return new Version_1_15_Item_Wrapper(itemID, count);
        };
    }

    @Override
    public String versionItemCmdFormat() {
        return "<itemMaterial> [amount]";
    }

    @Override
    public String getVersionSearchCmdFormat() {
        return "<itemID>";
    }

    @Override
    public List<String> nbtToVersionLoreData(@NotNull NBTCompound itemNBT) {
        return Collections.singletonList(String.format("§7Item id:   §9%s", itemNBT.getString(NBTItemTags.ID.getTagName())));
    }

    @Override
    public String consoleItemVersionDataFromNBT(@NotNull NBTCompound itemNBT) {
        return String.format(
                "§7  >> §9%s §b%s",
                itemNBT.getString(NBTItemTags.ID.getTagName()),
                itemNBT.getInteger(NBTItemTags.SLOT.getTagName())
        );
    }

    @Override
    public String[] playerItemVersionDataFromNBT(@NotNull List<NBTCompound> itemNBTs) {
        final int longestNameWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getString(NBTItemTags.ID.getTagName())) + 8;

        final String[] result = new String[itemNBTs.size()];

        for (int i = 0; i < itemNBTs.size(); i++) {
            final NBTCompound nbt = itemNBTs.get(i);
            final String itemId = nbt.getString(NBTItemTags.ID.getTagName());
            final String itemDamage = nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString();


            final String spacesToSlot = APIMsgFormatter.getEmptyStringFromWidth(longestNameWidth - APIMsgFormatter.getWidth(itemId, true));

            result[i] = String.format(
                    "§7  >> §9%s%s§b%s",
                    itemId,         spacesToSlot,
                    nbt.getInteger(NBTItemTags.SLOT.getTagName())
            );
        }

        return result;
    }

    @Override
    public String consoleItemDataHeader() {
        return "§9Items §bSlot";
    }

    @Override
    public String playerItemDataHeader(@NotNull List<NBTCompound> itemNBTs) {
        final int longestNameWidth = APIMsgFormatter.getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getString(NBTItemTags.ID.getTagName())) + 8;

        final String toItemSpaces = APIMsgFormatter.getEmptyStringFromWidth(APIMsgFormatter.getWidth("  >> ", false));
        final String toSlotSpaces = APIMsgFormatter.getEmptyStringFromWidth(longestNameWidth - APIMsgFormatter.getWidth("Item", false));

        return String.format("%s§9Items%s§bSlot", toItemSpaces, toSlotSpaces);
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean partialMath(@NotNull NBTCompound itemNBT, String itemID, short dataVal) {
        if (!itemNBT.getString(NBTItemTags.ID.getTagName()).equals(itemID)) return false;
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
                    nbtItem.getInteger(NBTItemTags.COUNT.getTagName())
            );

            return new Result<>(itemComp);
        }
        catch (Exception e) { return new Result<>(null, false, "§7Unable to parse NBT. Something must be malformed."); }
    }

    @Override
    public Result<Boolean> isValidItemNBT(@NotNull NBTCompound nbt) {
        if (!nbt.hasKey(NBTItemTags.ID.getTagName())) return new Result<>(false, false, "§7NBT is missing §6ID §7tag!");
        if (!nbt.hasKey(NBTItemTags.COUNT.getTagName())) return new Result<>(false, false, "§7NBT is missing §6COUNT §7tag!");

        return new Result<>(true);
    }

    @Override
    public void addDataToCreatedItem(@NotNull NBTCompound createdNBT, @NotNull NBTCompound dataComp) {
        createdNBT.setString(NBTItemTags.ID.getTagName(), dataComp.getString(NBTItemTags.ID.getTagName()));
        createdNBT.setInteger(NBTItemTags.COUNT.getTagName(), dataComp.getInteger(NBTItemTags.COUNT.getTagName()));
    }

    static NBTCompound createItemNBT(String itemID, int count) {
        final NBTContainer nbtContainer = new NBTContainer();
        nbtContainer.setString(NBTItemTags.ID.getTagName(), itemID);
        nbtContainer.setInteger(NBTItemTags.COUNT.getTagName(), count);

        return nbtContainer;
    }
}
