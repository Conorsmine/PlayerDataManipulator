package com.conorsmine.net.inventory;

import com.conorsmine.net.PlayerDataManipulator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryListener implements Listener {

    private final PlayerDataManipulator pl;

    public InventoryListener(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() == -999) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        final HumanEntity player = event.getView().getPlayer();
        if (!EditorInventory.playerHasEditorOpen((Player) player)) return;
        final EditorInventory editorInventory = EditorInventory.getPlayersEditor((Player) player);
        final int slotId = editorInventory.idAtSlot(event.getSlot());

        if (event.isRightClick()) {
            if (slotId == -1) { player.sendMessage(String.format("%s§7Ignore this slot!", PlayerDataManipulator.getPrefix())); }
            else { player.sendMessage(String.format("%s§7You clicked slot §e%s§7.", PlayerDataManipulator.getPrefix(), slotId)); }
            event.setCancelled(true);
        }

        if (slotId == -1) { event.setCancelled(true); }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        pl.getServer().getScheduler().runTask(
                pl,
                () -> EditorInventory.playerCloseEditor((Player) event.getPlayer(), event.getInventory(), pl.MOJANGSON)
        );
    }


}
