package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.LogFiles;
import com.conorsmine.net.utils.PlayerLocationUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@CommandAlias("pdm")
public final class PlayerCmds extends BaseCommand {

    @Dependency("plugin")
    private PlayerDataManipulator pl;

    @Subcommand("where")
    @Description("Provides information about the location of the player.")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS)
    @CommandPermission("pdm.player.where")
    private void whereCmd(final CommandSender sender, final OfflinePlayer target) {
        final Location playerLoc = PlayerLocationUtils.getPlayerPos(target);

        pl.sendCmdHeader(sender, "Where");
        if (playerLoc == null) {
            pl.sendMsg(
                    "§7There seems to be an §cerror§7!",
                    String.format("§7The location of \"§6%s§7\" could not be retrieved...", target.getName()),
                    String.format("§7Use §9/pdm errorReport location %s", target.getName()),
                    String.format("§7After executing the command, a report file will be created in the \"§9%s§7\" folder.", LogFiles.DIR_NAME),
                    "§7Send this file to Conorsmine aka. SCP-999"
            );
            return;
        }

        pl.sendMsg(sender,
                String.format("§7\"§6%s§7\"'s location:", target.getName()),
                String.format("§9Dimension §7>> §a%s", (playerLoc.getWorld() == null) ? "null?!" : playerLoc.getWorld().getName()),
                String.format("§9Position  §7>> §9x§7: §a%.2f  §9y§7: §a%.2f  §9z§7: §a%.2f", playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()),
                String.format("§9Rotation  §7>> §9pitch§7: §a%.2f°  §9y§7: §a%.2f°", playerLoc.getPitch(), playerLoc.getYaw())
        );

        if (sender instanceof Player) {
            if (playerLoc.getWorld() == null) {
                pl.sendMsg(sender, "§7Sorry, but the other features of this command can't be used since \"§6world§7\" is null.");
                return;
            }

            final TextComponent teleportTo = new TextComponent(String.format("%s §6[§9Teleport§6]", PlayerDataManipulator.getPrefix()));
            teleportTo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
                    "/pdm whereTeleport %s", locationToString(playerLoc)
            ).replaceAll(",", ".")));

            pl.sendMsg(sender, "§7Do you want to teleport to the player?");
            ((Player) sender).spigot().sendMessage(teleportTo);


            final TextComponent spawnShowStand = new TextComponent(String.format("%s §6[§9Spawn§6]", PlayerDataManipulator.getPrefix()));
            spawnShowStand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
                    "/pdm whereShow %s %s", target.getName(), locationToString(playerLoc)
            ).replaceAll(",", ".")));

            pl.sendMsg(sender, "§7Do you want to spawn an armour stand to show the position of the player?");
            ((Player) sender).spigot().sendMessage(spawnShowStand);
        }
    }

    @Subcommand("tphere")
    @Description("Teleports a player to your position.")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS)
    @CommandPermission("pdm.teleport.here")
    private void teleportHereCmd(final Player player, final OfflinePlayer target) {
        if (target.isOnline()) { ((Player) target).teleport(player); }

        pl.sendCmdHeader(player, "Teleport-Here");
        PlayerLocationUtils.teleportPlayer(target, PlayerLocationUtils.getPlayerPos(player));
        pl.sendMsg(player, String.format("§7Teleported \"§6%s§7\" to you!", target.getName()));
    }

    @Subcommand("tp")
    @Description("Teleports a player to a sepcified position.")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS + CmdCompletions.WORLDS + "x y z")
    @CommandPermission("pdm.teleport.position")
    private void teleportPlayerCmd(final CommandSender sender, final OfflinePlayer target, final Location loc) {
        pl.sendCmdHeader(sender, "Teleport");
        if (loc.getWorld() == null) { pl.sendMsg(sender, "§7Couldn't teleport you, because \"§6world§7\" is §cnull§7!"); return; }

        pl.sendMsg(sender,
                "§7Teleporting you too...",
                String.format("§9Dimension §7>> §a%s", loc.getWorld().getName()),
                String.format("§9Position  §7>> §9x§7: §a%.2f  §9y§7: §a%.2f  §9z§7: §a%.2f", loc.getX(), loc.getY(), loc.getZ())
        );

        PlayerLocationUtils.teleportPlayer(target, loc);
    }




    // Hidden commands

    @Private
    @Subcommand("whereShow")
    @CommandPermission("pdm.player.where")
    private void whereShowHiddenCmd(final Player sender, final String targetName, final String worldUUID, final double x, final double y, final double z, final float pitch, final float yaw) {
        final World world = pl.getServer().getWorld(UUID.fromString(worldUUID));
        Marker.spawnMarker(pl, new Location(world, x, y, z, yaw, pitch), targetName);

        pl.sendMsg(sender, "§7Spawned a marker at their position.");
    }

    @Private
    @Subcommand("whereTeleport")
    @CommandPermission("pdm.player.where")
    private void whereTeleportHiddenCmd(final Player sender, final String worldUUID, final double x, final double y, final double z, final float pitch, final float yaw) {
        final World world = pl.getServer().getWorld(UUID.fromString(worldUUID));
        sender.teleport(new Location(world, x, y, z, yaw, pitch));

        pl.sendMsg(sender, "§7Teleported!");
    }



    private static String locationToString(final Location loc) {
        return String.format("%s %f %f %f %f %f",
                loc.getWorld().getUID().toString(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getPitch(), loc.getYaw()
        );
    }

    public static final class Marker {

        private static final Queue<Marker> MARKER_QUEUE = new ConcurrentLinkedQueue<>();
        private static final long TIME_UNTIL_REMOVE = 30;
        private static BukkitTask REMOVER_TASK;

        private final ArmorStand marker;
        private final long creationTime = Instant.now().getEpochSecond();

        private Marker(final @NotNull JavaPlugin pl, final @NotNull ArmorStand marker) {
            this.marker = marker;
            MARKER_QUEUE.add(this);

            if (REMOVER_TASK != null) return;
            REMOVER_TASK = pl.getServer().getScheduler().runTaskTimerAsynchronously(pl, Marker::removeMarkersIfTimePassed, 0L, 20L);
        }

        private static void removeMarkersIfTimePassed() {
            final long newTime = Instant.now().getEpochSecond();

            for (Marker marker : new LinkedList<>(MARKER_QUEUE)) {
                if ((marker.creationTime + TIME_UNTIL_REMOVE) <= newTime) {
                    MARKER_QUEUE.remove();
                    marker.marker.remove();
                }
                else break;
            }
        }

        public static void removeAllMarkers() {
            MARKER_QUEUE.forEach((m) -> m.marker.remove());
        }

        public static ArmorStand spawnMarker(@NotNull JavaPlugin pl, @NotNull final Location loc, final String targetName) {
            final ArmorStand marker = (ArmorStand) loc.getWorld().spawn(loc, EntityType.ARMOR_STAND.getEntityClass(), (e) -> {
                final ArmorStand entity = ((ArmorStand) e);

                entity.setCustomName(targetName);
                entity.setCustomNameVisible(true);

                entity.setGlowing(true);
                entity.setVisible(true);
                entity.setPortalCooldown(Integer.MAX_VALUE);
                entity.setGravity(false);

                entity.setInvulnerable(true);
            });

            new Marker(pl, marker);
            return marker;
        }
    }
}
