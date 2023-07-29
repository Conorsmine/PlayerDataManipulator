package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.ParserFile;
import com.conorsmine.net.files.FileUtils;
import com.conorsmine.net.files.LogFiles;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.MojangsonUtils;
import com.conorsmine.net.webserver.PlayerDataParser;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.data.NBTData;
import de.tr7zw.nbtapi.data.PlayerData;
import net.md_5.bungee.api.chat.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@CommandAlias("pdm")
public class WebCmds extends BaseCommand {

    @Dependency("plugin")
    private PlayerDataManipulator pl;

    @Subcommand("editor")
    @Description("Provides a link to the web editor")
    @CommandCompletion(CmdCompletions.OFFLINE_PLAYERS)
    @CommandPermission("pdm.web.editor")
    private void webEditorCmd(final CommandSender sender, final OfflinePlayer target) {
        PlayerDataManipulator.sendMsg(sender, "§7Preparing web editor...");
        if (target.isOnline()) ((Player) target).saveData();

        PlayerDataParser.parsePlayerData(target)
                .whenComplete((uuid, e) -> {
                    try {
                        PlayerDataManipulator.sendMsg(sender, "§2Webeditor link:");

                        final String link = String.format("http://%s:%d/editor?id=%s",
                                InetAddress.getLocalHost().getHostAddress(),
                                WebsiteFile.getPort(),
                                uuid );
                        final TextComponent linkComponent = new TextComponent(String.format("%s§6%s", PlayerDataManipulator.getPrefix(), link));
                        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));

                        sender.spigot().sendMessage(linkComponent);
                    } catch (UnknownHostException ex) { ex.printStackTrace(); }
                });
    }

    @Subcommand("apply")
    @Description("Applies the changes done via the web editor")
    @CommandPermission("pdm.web.apply")
    private void applyChangesCmd(final CommandSender sender, @Single final String cmdCode) {
        if (cmdCode == null || cmdCode.isEmpty()) { PlayerDataManipulator.sendMsg(sender, "§cYou must provide a cmd code to execute this command!"); return; }

        final File fileFromCode = WebsiteFile.getChangeFileFromCommandCode(cmdCode);
        if (fileFromCode == null) {
            sendErrorReportInteract(sender, cmdCode);
            return;
        }

        final JSONObject applyJson = (JSONObject) FileUtils.parseFileToJson(fileFromCode);
        final UUID playerUUID = UUID.fromString(((String) ((JSONObject) applyJson.get("meta_data")).get("uuid")));
        final JSONArray changes = ((JSONArray) applyJson.get("changes"));

        PlayerDataManipulator.sendHeader(sender, "Applied Changes");
        applyChanges(sender, changes, playerUUID);
    }

    private static void sendErrorReportInteract(final CommandSender sender, final String cmdCode) {
        PlayerDataManipulator.sendMsg(
                sender,
                String.format("§cNo changes were found for cmd code \"%s\".", cmdCode),
                "§7Make sure the cmd code is the same as the one provided by the §9web editor§7.",
                "§7If they are, use the following command:",
                String.format("§7>> §9%s", String.format(GeneralCmds.reportChangesCmdFormat, cmdCode))
        );

        if (sender instanceof Player) {
            final TextComponent reportButton = new TextComponent(PlayerDataManipulator.getPrefix() + "   §6[§aReport§6]§r");
            reportButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent("§7Report the issue")).create()));
            reportButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(GeneralCmds.reportChangesCmdFormat, cmdCode)));

            sender.spigot().sendMessage(reportButton);
        }

        PlayerDataManipulator.sendMsg(
                sender,
                String.format("§7After executing the command, a report file will be created in the \"§9%s§7\" folder.", LogFiles.DIR_NAME),
                "§7Send this file to Conorsmine aka. SCP-999"
        );
    }

    private static void applyChanges(final CommandSender sender, final JSONArray changes, final UUID targetUUID) {
        final PlayerData playerData = NBTData.getOfflinePlayerData(targetUUID);
        if (playerData == null) {
            PlayerDataManipulator.sendMsg(sender, String.format("§cNo player was found with the uuid: \"%s\".", targetUUID));
            return;
        }

        final NBTCompound compound = playerData.getCompound();

        for (Object o : changes) {
            final JSONObject change = (JSONObject) o;
            final String path = (String) change.get("path");
            final Object value = change.get("value");

            final MojangsonUtils.NBTResult result = new MojangsonUtils().setSeparator(ParserFile.getSeparator()).getCompoundFromPath(compound, path);

            final NBTCompound nbtCompound = result.getCompound();
            final Object prevValue = MojangsonUtils.getSimpleDataFromCompound(nbtCompound, result.getFinalKey());
            MojangsonUtils.setSimpleDataFromKey(nbtCompound, result.getFinalKey(), value);

            PlayerDataManipulator.sendMsg(sender, String.format("§6~ §9%s§7: From §1%s §7-> §9%s", path, prevValue, value));
        }

        playerData.saveChanges();
        final OfflinePlayer offlinePlayer = PlayerDataManipulator.getINSTANCE().getServer().getOfflinePlayer(targetUUID);
        if (offlinePlayer.isOnline()) {
            ((Player) offlinePlayer).loadData();
            ((Player) offlinePlayer).saveData();
        }
    }
}
