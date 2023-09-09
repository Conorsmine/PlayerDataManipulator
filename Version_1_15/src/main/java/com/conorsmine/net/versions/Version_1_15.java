package com.conorsmine.net.versions;

import com.conorsmine.net.items.VersionItems;
import com.conorsmine.net.Versionify;
import com.conorsmine.net.Versions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Version_1_15 implements Versionify {

    private final VersionItems VERSION_ITEMS = new Version_1_15_Items();

    @Override
    public Versions getVersion() {
        return Versions.V1_15;
    }

    @Override
    public VersionItems getVersionItemAPI() {
        return VERSION_ITEMS;
    }

    @Override
    public ItemStack getInventoryEditorIgnoreItem() {
        return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    }
}
