package com.conorsmine.net;

import co.aikar.commands.BukkitCommandManager;
import com.conorsmine.net.items.VersionItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Versionify {

    Versions getVersion();

    VersionItems getVersionItemAPI();

    /**
     * {@see EditorInventory}
     * @return Item to be used for "ignored" slots
     */
    ItemStack getInventoryEditorIgnoreItem();
}
