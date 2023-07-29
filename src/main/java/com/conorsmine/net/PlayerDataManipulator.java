package com.conorsmine.net;

import co.aikar.commands.BukkitCommandManager;
import com.conorsmine.net.cmds.CmdCompletions;
import com.conorsmine.net.cmds.GeneralCmds;
import com.conorsmine.net.cmds.InvCmds;
import com.conorsmine.net.cmds.WebCmds;
import com.conorsmine.net.files.ItemStore;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.inventory.InventoryListener;
import com.conorsmine.net.inventory.PathWrapper;
import com.conorsmine.net.webserver.WebServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@SuppressWarnings("unused")
public final class PlayerDataManipulator extends JavaPlugin{

    private BukkitCommandManager commandManager;
    public static PlayerDataManipulator INSTANCE;
    public static WebServer webServer;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        reloadConfig();

        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        webServer = new WebServer(WebsiteFile.getPort());
        webServer.initServer();

        commandManager = new BukkitCommandManager(this);
        setupCommands();

        sendMsg(getFancyLogo());
    }

    private void setupCommands() {
        /* Todo:
        *   open;       Opens an inventory defined in the config                                                ✔
        *   clear;      Clears the players inventory                                                            ✔
        *   add;        Adds an item to the players inv                                                         ✔
        *   remove;     Removes an item from the players inv                                                    ✔
        *   list;       Lists the players items in text, in case the inv can't be opened safely                 ✔
        *   search;     Search for an item in all players inventories                                           ✔
        *   check;      Goes through players inventories and checks if the items are "safe" / legal             ✖
        *   info;       Provides the nbt-data of the item                                                       ✔
        *   addnbt;     Parses the nbt to an item and adds it to the players inv                                ✔
        *   store;      Stores the item's nbt to a file                                                         ✔
        *   addstore;   Adds the stored item to the player                                                      ✔
        *   editor;     Opens the online editor of the player                                                   ✔
        * */

        commandManager.registerDependency(PlayerDataManipulator.class, "plugin", this);
        commandManager.getCommandContexts().registerContext(PathWrapper.class, PathWrapper.getContextResolver());
        commandManager.getCommandContexts().registerContext(ItemStore.class, ItemStore.getContextResolver());
        CmdCompletions.registerCompletions();

        commandManager.registerCommand(new GeneralCmds());
        commandManager.registerCommand(new InvCmds());
        commandManager.registerCommand(new WebCmds());
    }

    @Override
    public void onDisable() {
        webServer.stop();

        // Cleanup temp cache
        final File[] files = WebsiteFile.getTempCacheDir().listFiles();
        if (files == null)return;

        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static PlayerDataManipulator getINSTANCE() {
        return INSTANCE;
    }

    public static void sendMsg(String... msg) { sendMsg(Bukkit.getConsoleSender(), msg); }

    public static void sendMsg(final CommandSender sender, String... msg) {
        for (String s : msg) { sender.sendMessage(getPrefix() + s); }
    }

    public static void sendHeader(final CommandSender sender, String cmdHeader) {
        sender.sendMessage(String.format("%s§e§m---=== §6[§9%s§6] §e§m===---§r", getPrefix(), cmdHeader));
    }

    public static String getPrefix() {
        return "§6[§9PDM§6] §r";
    }

    private String[] getFancyLogo() {
        return new String[] {
                "",
                "  §8      ██████████               ",
                "  §8    ██§b██████████§8██             ",
                "  §8  ██§b████§f██████§b████§8██             §9PlayerDataManipulator§7-§6v" + this.getDescription().getVersion(),
                "  §8██§b████§f██§b████████████§8██             §7Created by §aConorsmine",
                "  §8██§b██§f██§b██████████████§8██             §7Also known as §aSCP-999",
                "  §8██§b██§f██§b██████████████§8██               §7Powered by §6NBT-API",
                "  §8██§b██§f██§b██████████████§8██         ",
                "  §8██§b██████████████████§8██        ",
                "  §8  ██§b██████████████§8██           ",
                "  §8    ██§b██████████§8████             §7Use §9/pdm help §7for all commands.",
                "  §8      ██████████    ████         §7Use §9/pdm editor §7for using the webeditor.",
                "  §8                    ██████       §7Check the §6config.yml §7for more info about §6\"inventories\"",
                "  §8                      ██████     §7Can support §8almost §7any and all §6inventories",
                "  §8                        ██████ ",
                "  §8                          ████ ",
                ""
        };
    }

    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }
}
