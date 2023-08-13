package com.conorsmine.net;

import co.aikar.commands.BukkitCommandManager;
import com.conorsmine.net.cmds.*;
import com.conorsmine.net.cmds.contexts.LocationContext;
import com.conorsmine.net.files.ConfigFile;
import com.conorsmine.net.cmds.contexts.ItemStore;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.inventory.InventoryListener;
import com.conorsmine.net.cmds.contexts.PathWrapper;
import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.MojangsonUtilsBuilder;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import com.conorsmine.net.webserver.WebServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class PlayerDataManipulator extends JavaPlugin {

    private BukkitCommandManager commandManager;
    public final ConfigFile CONF = new ConfigFile(this);
    public final MojangsonUtils MOJANGSON = new MojangsonUtilsBuilder(CONF.getSeparator()).create();

    public static PlayerDataManipulator INSTANCE;
    public static WebServer webServer;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        reloadConfig();

        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        webServer = new WebServer(WebsiteFile.staticGetPort());
        webServer.initServer();

        commandManager = new BukkitCommandManager(this);
        setupCommands();

        staticSendMsg(getFancyLogo());
    }

    private void setupCommands() {
        commandManager.registerDependency(PlayerDataManipulator.class, "plugin", this);
        commandManager.getCommandContexts().registerContext(PathWrapper.class, PathWrapper.getContextResolver());
        commandManager.getCommandContexts().registerContext(ItemStore.class, ItemStore.getContextResolver());
        commandManager.getCommandContexts().registerContext(Location.class, LocationContext.getContextResolver(this));
        CmdCompletions.registerCompletions();

        commandManager.registerCommand(new GeneralCmds());
        commandManager.registerCommand(new PlayerCmds());
        commandManager.registerCommand(new InvCmds());
        commandManager.registerCommand(new WebCmds());
    }

    @Override
    public void onDisable() {
        webServer.stop();
        WebsiteFile.clearTempCache();

        PlayerCmds.Marker.removeAllMarkers();
    }

    public void reloadPlugin(@NotNull final CommandSender sender) {
        CONF.reload(sender);
    }

    @Deprecated
    public static PlayerDataManipulator getINSTANCE() {
        return INSTANCE;
    }

    public void sendCmdHeader(final CommandSender sender, String cmdHeader) {
        sender.sendMessage(String.format("%s§e§m---=== §6[§9%s§6] §e§m===---§r", getPrefix(), cmdHeader));
    }

    public void sendMsg(String... msg) {
        sendMsg(this.getServer().getConsoleSender(), msg);
    }

    public void sendMsg(CommandSender sender, String... msg) {
        for (String s : msg) { sender.sendMessage(getPrefix() + s); }
    }

    @Deprecated
    public static void staticSendMsg(String... msg) { staticSendMsg(Bukkit.getConsoleSender(), msg); }

    @Deprecated
    public static void staticSendMsg(final CommandSender sender, String... msg) {
        for (String s : msg) { sender.sendMessage(getPrefix() + s); }
    }

    @Deprecated
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
                "  §8██§b██§f██§b██████████████§8██              §7Powered by §6NBT-API",
                "  §8██§b██§f██§b██████████████§8██         ",
                "  §8██§b██████████████████§8██        ",
                "  §8  ██§b██████████████§8██           ",
                "  §8    ██§b██████████§8████             §7Use §9/pdm help §7for all commands.",
                "  §8      ██████████    ████         §7Use §9/pdm editor §7for using the webeditor.",
                "  §8                    ██████       §7Check the §6config.yml §7for more info about \"§6inventories§7\"",
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
