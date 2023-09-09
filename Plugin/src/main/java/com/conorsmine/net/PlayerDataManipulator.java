package com.conorsmine.net;

import co.aikar.commands.BukkitCommandManager;
import com.conorsmine.net.cmds.*;
import com.conorsmine.net.cmds.util.CmdCompletions;
import com.conorsmine.net.cmds.util.CmdContexts;
import com.conorsmine.net.files.ConfigFile;
import com.conorsmine.net.files.LogFiles;
import com.conorsmine.net.files.NBTStoreFile;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.inventory.InventoryListener;
import com.conorsmine.net.items.VersionItemWrapper;
import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.MojangsonUtilsBuilder;
import com.conorsmine.net.mojangson.StringUtils;
import com.conorsmine.net.utils.NBTInventoryUtils;
import com.conorsmine.net.webserver.WebServer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class PlayerDataManipulator extends JavaPlugin {

    private static final int RESOURCE_ID = 112561;
    private static final String RESOURCE_LINK = "https://www.spigotmc.org/resources/player-data-manipulator." + RESOURCE_ID + "/";

    private BukkitCommandManager commandManager;
    public final ConfigFile CONF = new ConfigFile(this);
    public final WebsiteFile WEBSITE_CONF = new WebsiteFile(this);
    public final NBTStoreFile ITEM_STORES = new NBTStoreFile(this);
    public final LogFiles LOGS = new LogFiles(this);
    public MojangsonUtils MOJANGSON = new MojangsonUtilsBuilder(CONF.getSeparator()).create();
    public final NBTInventoryUtils INV_UTILS = new NBTInventoryUtils(this);
    public final WebServer WEB_SERVER = new WebServer(this, WEBSITE_CONF.getPort());
    public static Versionify VERSION_SUPPORT;

    @Override
    public void onEnable() {
        VERSION_SUPPORT = determineVersion(this);
        reloadPlugin(getServer().getConsoleSender());
        sendMsg("");

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        commandManager = new BukkitCommandManager(this);
        setupCommands();

        sendMsg(getFancyLogo());
        checkUpdate();
    }

    private void setupCommands() {
        commandManager.getCommandContexts().registerContext(InventoryPath.class, CmdContexts.getInventoryPathContextResolver(this));
        commandManager.getCommandContexts().registerContext(ItemStore.class, CmdContexts.getItemStoreContextResolver(this));
        commandManager.getCommandContexts().registerContext(Location.class, CmdContexts.getLocationContextResolver(this));
        commandManager.getCommandContexts().registerContext(VersionItemWrapper.class, VERSION_SUPPORT.getVersionItemAPI().versionItemContextResolver(this));
        new CmdCompletions(this).registerCompletions();

        commandManager.registerCommand(new GeneralCmds(this));
        commandManager.registerCommand(new ErrorReportCmds(this));
        commandManager.registerCommand(new PlayerCmds(this));
        commandManager.registerCommand(new InvCmds(this));
        commandManager.registerCommand(new WebCmds(this));
    }

    @Override
    public void onDisable() {
        WEB_SERVER.stop();
        WEBSITE_CONF.clearTempCache();

        PlayerCmds.Marker.removeAllMarkers();
    }

    public void reloadPlugin(@NotNull final CommandSender sender) {
        saveDefaultConfig();

        sendMsg(sender, "§7Reloading config file...");
        reloadConfig();
        CONF.reload(sender);
        WEBSITE_CONF.reload(sender);
        ITEM_STORES.reload(sender);
        LOGS.reload(sender);

        sendMsg(sender,
                String.format("§7>> §9%s: §6%s", com.conorsmine.net.Properties.CONFIG_SEPARATOR, CONF.getSeparator()),
                String.format("§7>> §9%s: §6%d", com.conorsmine.net.Properties.CONFIG_WORKERS, CONF.getMaxSearchExecutors()),
                String.format("§7>> §9%s: §6%s", com.conorsmine.net.Properties.CONFIG_WEBSITE_PORT, WEBSITE_CONF.getPort()),
                String.format("§7>> §9%s:", com.conorsmine.net.Properties.CONFIG_INV_PATH_SECTION)
        );

        final Iterator<InventoryPath> iter = CONF.getInventoryPaths().iterator();
        while (iter.hasNext()) {
            final InventoryPath inventoryPath = iter.next();
            sendMsg(sender,
                    String.format("    §7>> §9Name: §6%s", inventoryPath.getSectionName()),
                    String.format("    §7>> §9Path: §6%s", inventoryPath.getPath()),
                    String.format("    §7>> §9Size: §6%s", inventoryPath.getSize())
            );

            if (iter.hasNext()) sendMsg(sender, "");
        }

        sendMsg(sender, "§aConfig file reloaded.", "");


        if (!MOJANGSON.getSeparator().equals(CONF.getSeparator())) {
            sendMsg(sender, String.format("§7Rebuilding utils from \"§6%s§7\"...", com.conorsmine.net.Properties.CONFIG_SEPARATOR));
            sendSeparatorChangeError(sender, MOJANGSON.getSeparator(), CONF.getSeparator());
            MOJANGSON = new MojangsonUtilsBuilder(CONF.getSeparator()).create();
            sendMsg(sender, "§aRebuilt utils.");
        }

        sendMsg(sender);
        sendMsg(sender, "§7Restarting Webserver...");
        WEB_SERVER.stop();
        WEB_SERVER.initServer(WEBSITE_CONF.getPort());
        sendMsg(sender, String.format("§aRestarted Webserver §7on %s §6%s§7.", Properties.CONFIG_WEBSITE_PORT, WEBSITE_CONF.getPort()));

        sendMsg(sender, "", String.format("§7Enabled with version support for: %s", VERSION_SUPPORT.getClass().getSimpleName()));
    }

    private void sendSeparatorChangeError(final CommandSender sender, String prevSep, String newSep) {
        final String splitFormat = String.format("(?=%s)|(?<=%s)", prevSep, prevSep);

        final Map<String, List<StringUtils.ErrorPlace>> pathErrors = new HashMap<>();
        for (InventoryPath inventoryPath : CONF.getInventoryPaths()) {
            final String path = inventoryPath.getPath().toString();
            final String[] split = path.split(splitFormat);
            if (split.length == 0) continue;

            final List<StringUtils.ErrorPlace> errors = new LinkedList<>();
            for (int i = 0; i < split.length; i++) {
                if (!split[i].equals(prevSep)) continue;
                errors.add(new StringUtils.ErrorPlace(i, String.format(" Separator should be: \"%s\"!", newSep)));
            }

            if (!errors.isEmpty()) pathErrors.put(path, errors);
        }

        if (pathErrors.isEmpty()) return;
        sendCmdHeader(sender, "§eSeparator Warning");
        for (Map.Entry<String, List<StringUtils.ErrorPlace>> entry : pathErrors.entrySet()) {
            final String[] msg = StringUtils.fancyErrorLines(entry.getKey(), entry.getKey().split(splitFormat), entry.getValue());
            for (String s : msg) sendMsg(sender, String.format("§c%s", s));
        }
    }

    public void sendCmdHeader(final CommandSender sender, String cmdHeader) {
        sender.sendMessage(String.format("%s§e§m---=== §6[§9%s§6] §e§m===---§r", getPrefix(), cmdHeader));
    }

    public void sendMsg(String... msg) {
        sendMsg(this.getServer().getConsoleSender(), msg);
    }

    public void sendMsg(CommandSender sender, String... msg) {
        for (String s : msg) {
            sender.sendMessage(getPrefix() + s);
        }
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
                "  §8                    ██████       §7Check the §6config.yml §7for more info about \"§6inventories§7\".",
                "  §8                      ██████     §7Can support §8almost §7any and all §6inventories.",
                "  §8                        ██████ ",
                "  §8                          ████ ",
                ""
        };
    }

    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }

    private void getVersion(final Consumer<String> consumer) {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID + "/~").openStream(); Scanner scan = new Scanner(is)) {
                if (scan.hasNext()) {
                    consumer.accept(scan.next());
                }
            } catch (IOException e) {
                sendMsg("Unable to check for updates: \n" + e.getMessage());
            }
        });
    }

    private void checkUpdate() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            getVersion((newestVersion) -> {
                final String currentVersion = getDescription().getVersion();

                final String[] newVersionCodes = newestVersion.split("\\.");
                final String[] curVersionCodes = currentVersion.split("\\.");

                final int newMajVersion = Integer.parseInt(newVersionCodes[0]);
                final int newMinVersion = Integer.parseInt(newVersionCodes[1]);

                final int curMajVersion = Integer.parseInt(curVersionCodes[0]);
                final int curMinVersion = Integer.parseInt(curVersionCodes[1]);

                final String newVersionStr = String.format("§7A newer version of PDM is available: §6%s.%s§7! Check the Spigot page: %n%s", newMajVersion, newMinVersion, RESOURCE_LINK);
                if (newMajVersion > curMajVersion) { sendMsg(newVersionStr); return; }
                if (newMinVersion > curMinVersion) { sendMsg(newVersionStr); return; }

                sendMsg("§7You are on the latest version of PDM, yey!");
            });
        });
    }

    private static Versionify determineVersion(PlayerDataManipulator pl) {
        try { return Versions.determineVersion(pl.getServer()).createVersionify(); }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            pl.sendMsg(
                    "§cA CRITICAL ERROR HAS OCCURRED!!!",
                    "§cSTOPPING PLUGIN",
                    " §7>> §cNO VERSION SUPPORT COULD BE FOUND!!",
                    "    §cREPORT THIS ERROR TO THE PLUGIN DEV!!"
            );

            pl.onDisable();
            pl.getPluginLoader().disablePlugin(pl);
            return null;
        }
    }
}
