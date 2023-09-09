package com.conorsmine.net.versions;

import com.conorsmine.net.items.NBTItemTags;
import com.conorsmine.net.items.VersionItemWrapper;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class Version_1_12_2_Item_Wrapper implements VersionItemWrapper {

    private final String itemID;
    private final NBTCompound itemNBT;
    private final ItemStack item;

    public Version_1_12_2_Item_Wrapper(String itemID, int count, short data) {
        this.itemID = itemID;
        this.itemNBT = Version_1_12_2_Items.createItemNBT(itemID, count, data);
        this.item = NBTItem.convertNBTtoItem(itemNBT);
    }

    @Override
    public String getItemID() {
        return itemID;
    }

    @Override
    public ItemStack getItem() {
        return item;
    }

    @Override
    public NBTCompound getItemNBT() {
        return itemNBT;
    }

    @Override
    public String toString() {
        return getItemNBT().toString();
    }
}
