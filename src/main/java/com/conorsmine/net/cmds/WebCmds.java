package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.FileUtils;
import com.conorsmine.net.files.LogFiles;
import com.conorsmine.net.mojangson.NBTQueryResult;
import com.conorsmine.net.mojangson.data.NBTCompoundData;
import com.conorsmine.net.mojangson.data.NBTPrimitiveData;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import com.conorsmine.net.webserver.PlayerDataParser;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.data.NBTData;
import de.tr7zw.nbtapi.data.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static com.conorsmine.net.Properties.URL_ID;
import static com.conorsmine.net.Properties.URL_PATH;

@CommandAlias("pdm")
public final class WebCmds extends BaseCommand {

    private final PlayerDataManipulator pl;

    public WebCmds(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    @Subcommand("editor")
    @Description("Provides a link to the web editor")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS)
    @CommandPermission("pdm.web.editor")
    private void webEditorCmd(final CommandSender sender, final OfflinePlayer target) {
        pl.sendMsg(sender, "§7Preparing web editor...");
        if (target.isOnline()) ((Player) target).saveData();

        new PlayerDataParser(pl).parsePlayerData(target)
                .whenComplete((uuid, e) -> {
                    try {
                        pl.sendMsg(sender, "§2Web editor link:");

                        final TextComponent linkComponent = getWebEditorLink(uuid);
                        sender.spigot().sendMessage(linkComponent);
                    }
                    catch (UnknownHostException ex) {
                        pl.sendMsg(sender, "§7Couldn't create a web-editor!");
                    }
                });
    }

    @Subcommand("apply")
    @Description("Applies the changes done via the web editor")
    @CommandPermission("pdm.web.apply")
    private void applyChangesCmd(final CommandSender sender, @Single final String cmdCode) {
        if (cmdCode == null || cmdCode.isEmpty()) { pl.sendMsg(sender, "§cYou must provide a cmd code to execute this command!"); return; }

        final File fileFromCode = pl.WEBSITE_CONF.getChangeFileFromCommandCode(cmdCode);
        if (fileFromCode == null) {
            sendErrorReportInteract(sender, cmdCode);
            return;
        }

        final JSONObject applyJson = (JSONObject) FileUtils.parseFileToJson(fileFromCode);
        final UUID playerUUID = UUID.fromString(((String) ((JSONObject) applyJson.get("meta_data")).get("uuid")));
        final JSONArray changes = ((JSONArray) applyJson.get("changes"));

        pl.sendCmdHeader(sender, "Applied Changes");
        applyChanges(sender, changes, playerUUID);
    }

    @NotNull
    private TextComponent getWebEditorLink(UUID uuid) throws UnknownHostException {
        final String link = String.format("http://%s:%d/%s?%s=%s",
                InetAddress.getLocalHost().getHostAddress(),
                pl.WEBSITE_CONF.getPort(),
                URL_PATH,
                URL_ID,
                uuid);
        final TextComponent linkComponent = new TextComponent(String.format("%s§6%s", PlayerDataManipulator.getPrefix(), link));
        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        return linkComponent;
    }

    private void sendErrorReportInteract(final CommandSender sender, final String cmdCode) {
        pl.sendMsg(
                sender,
                String.format("§cNo changes were found for cmd code \"%s\".", cmdCode),
                "§7Make sure the cmd code is the same as the one provided by the §9web editor§7.",
                "§7If they are, use the following command:",
                String.format("§7>> §9%s", String.format(ErrorReportCmds.REPORT_CHANGES_CMD_FORMAT, cmdCode))
        );

        if (sender instanceof Player) {
            final TextComponent reportButton = new TextComponent(PlayerDataManipulator.getPrefix() + "   §6[§aReport§6]§r");
            reportButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent("§7Report the issue")).create()));
            reportButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(ErrorReportCmds.REPORT_CHANGES_CMD_FORMAT, cmdCode)));

            sender.spigot().sendMessage(reportButton);
        }

        pl.sendMsg(
                sender,
                String.format("§7After executing the command, a report file will be created in the \"§9%s§7\" folder.", LogFiles.DIR_NAME),
                "§7Send this file to Conorsmine aka. SCP-999"
        );
    }

    private void applyChanges(final CommandSender sender, final JSONArray changes, final UUID targetUUID) {
        final PlayerData playerData = NBTData.getOfflinePlayerData(targetUUID);
        if (playerData == null) {
            pl.sendMsg(sender, String.format("§cNo player was found with the uuid: \"%s\".", targetUUID));
            return;
        }

        final NBTCompound compound = playerData.getCompound();

        for (Object o : changes) {
            final JSONObject change = (JSONObject) o;
            final String path = (String) change.get("path");
            final Object value = change.get("value");

            final NBTPath changesPath = new NBTPathBuilder(pl.MOJANGSON).parseString(path).create();
            final NBTQueryResult result = pl.MOJANGSON.getDataFromPath(new NBTCompoundData(compound), changesPath);

            final Object prevValue = ((NBTPrimitiveData) result.getData()).getData();
//            pl.MOJANGSON.setDataToPath(new NBTCompoundData(compound), changesPath, value, );    //Todo; Fix this!

            pl.sendMsg(sender, String.format("§6~ §9%s§7: From §1%s §7-> §9%s", path, prevValue, value));
        }

        playerData.saveChanges();
        final OfflinePlayer offlinePlayer = pl.getServer().getOfflinePlayer(targetUUID);
        if (offlinePlayer.isOnline()) {
            ((Player) offlinePlayer).loadData();
            ((Player) offlinePlayer).saveData();
        }
    }
}
