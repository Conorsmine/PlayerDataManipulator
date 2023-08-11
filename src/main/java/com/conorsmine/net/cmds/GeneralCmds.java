package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.RegisteredCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.LogFiles;
import com.conorsmine.net.files.WebsiteFile;
import de.tr7zw.nbtapi.data.NBTData;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unchecked")
@CommandAlias("pdm")
public class GeneralCmds extends BaseCommand {

    protected static final String reportChangesCmdFormat = "/pdm errorReport changes %s";
    private static final String errorChangesFileFormat = "errorChanges_%s", errorLocationFileFormat = "errorLocation_%s_%s";

    @Dependency("plugin")
    private PlayerDataManipulator pl;

    @CatchUnknown
    @Private
    private void passToHelpCmd(final CommandSender sender) {
        sendHelp(sender);
    }

    @Subcommand("help")
    @Description("Provides a list of all sub-commands")
    private void sendHelp(final CommandSender sender) {
        pl.sendCmdHeader(sender, "Help");
        for (Map.Entry<String, RegisteredCommand> entry : pl.getCommandManager().getRootCommand(getName()).getSubCommands().entries()) {
            if (entry.getValue().isPrivate()) continue;

            pl.sendMsg(sender, String.format("  §7>> §9/%s %s", getName(), entry.getKey()));
        }
    }

    @Private
    @Subcommand("errorReport changes")
    @Description("Creates a logfile with currently relevant data")
    private void createChangesErrorFile(final CommandSender sender, final String cmdCode) {
        if (cmdCode == null || cmdCode.isEmpty()) { pl.sendMsg(sender, "§cYou must provide a cmd code to execute this command!"); return; }

        final JSONArray array = new JSONArray();
        final String[] list = WebsiteFile.getTempCacheDir().list();
        if (list != null) array.addAll(Arrays.asList(list));

        final JSONObject logData = new JSONObject();
        logData.put("cmd_code", cmdCode);
        logData.put("state", array);

        addMetadata(logData);
        final String fileName = String.format(errorChangesFileFormat, Instant.now().getEpochSecond());
        LogFiles.createErrorFile(fileName, logData);
        pl.sendMsg(sender, String.format("§7Created error file called \"§9%s§7\"§7.", fileName));
    }

    @Private
    @Subcommand("errorReport location")
    @Description("Creates a logfile with currently relevant data")
    private void createPlayerLocErrorFile(final CommandSender sender, final OfflinePlayer target) {
        final JSONObject logData = new JSONObject();
        logData.put("player_data", NBTData.getOfflinePlayerData(target.getUniqueId()).getCompound().getCompound());

        addMetadata(logData);
        final String fileName = String.format(errorLocationFileFormat, target.getName(), Instant.now().getEpochSecond());
        LogFiles.createErrorFile(fileName, logData);

        pl.sendMsg(sender, String.format("§7Created error file called \"§9%s§7\"§7.", fileName));
    }


    private void addMetadata(JSONObject json) {
        final JSONObject metaData = new JSONObject();
        metaData.put("version", pl.getDescription().getVersion());

        json.put("meta_data", metaData);
    }
}
