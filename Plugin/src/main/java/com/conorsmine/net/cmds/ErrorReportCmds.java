package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.utils.PlayerLocationUtils;
import com.conorsmine.net.cmds.util.CommandSection;
import de.tr7zw.nbtapi.data.NBTData;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.Arrays;

@SuppressWarnings("unchecked")
@CommandAlias("pdm")
@Subcommand("errorReport")
public class ErrorReportCmds extends BaseCommand {

    protected static final String REPORT_CHANGES_CMD_FORMAT = "/pdm errorReport changes %s";
    private static final String ERROR_CHANGES_FILE_FORMAT = "errorChanges_%s", ERROR_LOCATION_FILE_FORMAT = "errorLocation_%s_%s";

    private final PlayerDataManipulator pl;

    public ErrorReportCmds(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    @Private
    @Subcommand("changes")
    @Syntax("<target> <cmdCode>")
    @Description("Creates a logfile with currently relevant data")
    @CommandSection("reporting")
    private void createChangesErrorFile(final CommandSender sender, final String cmdCode) {
        if (cmdCode == null || cmdCode.isEmpty()) { pl.sendMsg(sender, "§cYou must provide a cmd code to execute this command!"); return; }

        final JSONArray array = new JSONArray();
        final String[] list = pl.WEBSITE_CONF.getTempCacheDir().list();
        if (list != null) array.addAll(Arrays.asList(list));

        final JSONObject logData = new JSONObject();
        logData.put("cmd_code", cmdCode);
        logData.put("state", array);

        addMetadata(logData);
        final String fileName = String.format(ERROR_CHANGES_FILE_FORMAT, Instant.now().getEpochSecond());
        pl.LOGS.createErrorFile(sender, fileName, logData);
        pl.sendMsg(sender, String.format("§7Created error file called \"§9%s§7\"§7.", fileName));
    }

    @Private
    @Subcommand("location")
    @Syntax("<target>")
    @Description("Creates a logfile with currently relevant data")
    @CommandSection("reporting")
    private void createPlayerLocErrorFile(final CommandSender sender, final OfflinePlayer target) {
        final JSONObject logData = new JSONObject();
        logData.put("player_data", NBTData.getOfflinePlayerData(target.getUniqueId()).getCompound().getCompound());

        final JSONObject pathJson = new JSONObject();
        pathJson.put("POS_X", PlayerLocationUtils.PlayerLocPaths.POS_X.getPath());
        pathJson.put("POS_Y", PlayerLocationUtils.PlayerLocPaths.POS_Y.getPath());
        pathJson.put("POS_Z", PlayerLocationUtils.PlayerLocPaths.POS_Z.getPath());

        pathJson.put("ROT_PITCH", PlayerLocationUtils.PlayerLocPaths.ROT_PITCH.getPath());
        pathJson.put("ROT_YAW", PlayerLocationUtils.PlayerLocPaths.ROT_YAW.getPath());

        pathJson.put("WORLD_UUID_LEAST", PlayerLocationUtils.PlayerLocPaths.WORLD_UUID_LEAST.getPath());
        pathJson.put("WORLD_UUID_MOST", PlayerLocationUtils.PlayerLocPaths.WORLD_UUID_MOST.getPath());

        pathJson.put("WORLD_DIM_ID", PlayerLocationUtils.PlayerLocPaths.WORLD_DIM_ID.getPath());
        pathJson.put("PLAYER_DIM_ID", PlayerLocationUtils.PlayerLocPaths.PLAYER_DIM_ID.getPath());

        logData.put("nbt_loc_paths", pathJson);
        addMetadata(logData);
        final String fileName = String.format(ERROR_LOCATION_FILE_FORMAT, target.getName(), Instant.now().getEpochSecond());
        pl.LOGS.createErrorFile(sender, fileName, logData);

        pl.sendMsg(sender, String.format("§7Created error file called \"§9%s§7\"§7.", fileName));
    }


    private void addMetadata(JSONObject json) {
        final JSONObject metaData = new JSONObject();
        metaData.put("version", pl.getDescription().getVersion());

        json.put("meta_data", metaData);
    }
}
