package com.conorsmine.net.versions;

import com.conorsmine.net.items.VersionItems;
import com.conorsmine.net.Versionify;
import com.conorsmine.net.Versions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Version_1_12_2 implements Versionify {

    private final VersionItems VERSION_ITEMS = new Version_1_12_2_Items();

    @Override
    public Versions getVersion() {
        return Versions.V1_12_2;
    }

    @Override
    public VersionItems getVersionItemAPI() {
        return VERSION_ITEMS;
    }

    @Override
    public ItemStack getInventoryEditorIgnoreItem() {
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, ((byte) 7));
    }
}
