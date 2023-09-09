package com.conorsmine.net.items;

import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper used to save itemID and more data about the item.
 */
public interface VersionItemWrapper {

    String getItemID();

    ItemStack getItem();

    NBTCompound getItemNBT();
}
