package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.utils.Lazy;
import com.conorsmine.net.cmds.util.CommandSection;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("pdm")
public class GeneralCmds extends BaseCommand {
    private final Lazy<Map<String, List<CmdInfo>>> SORTED_SECTION_CMD = new Lazy<>(this::sortedSupplier);

    private final PlayerDataManipulator pl;

    public GeneralCmds(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    @CatchUnknown
    @Private
    private void passToHelp(final CommandSender sender) { sendHelp(sender); }

    @Subcommand("help")
    @Description("Provides a list of all sub-commands")
    private void sendHelp(final CommandSender sender) {
        pl.sendCmdHeader(sender, "Help");

        for (Map.Entry<String, List<CmdInfo>> entry : SORTED_SECTION_CMD.get().entrySet()) {
            final String cmdMsgFormat;
            if (entry.getKey() == null) cmdMsgFormat = " §7>> §9/%s %s";
            else cmdMsgFormat = "    §7>> §9/%s %s";

            if (entry.getKey() != null) pl.sendMsg(sender, String.format("§e%s§7:", entry.getKey()));
            for (CmdInfo cmdInfo : entry.getValue()) {
                final String cmdName = String.format(cmdMsgFormat, getName(), cmdInfo.cmd);
                if (!(sender instanceof Player)) { pl.sendMsg(sender, cmdName); continue; }

                final TextComponent cmdMsg = new TextComponent(String.format("%s%s", PlayerDataManipulator.getPrefix(), cmdName));
                cmdMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(cmdInfo.desc).create()));
                cmdMsg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s ", getName(), cmdInfo.cmd)));
                ((Player) sender).spigot().sendMessage(cmdMsg);
            }
        }

        SORTED_SECTION_CMD.reset();
    }

    @Subcommand("reload")
    @Description("Reloads the plugin")
    private void reload(final CommandSender sender) {
        pl.sendCmdHeader(sender, "Reload");
        pl.reloadPlugin(sender);
    }

    @Subcommand("version|v")
    @Description("The version of the plugin")
    private void sendVersionDetails(final CommandSender sender) {
        pl.sendCmdHeader(sender, "Version");
        pl.sendMsg(sender, String.format("§7>> §6%s", pl.getDescription().getVersion()));
    }



    @SuppressWarnings({"unchecked", "ComparatorMethodParameterNotUsed"})
    private Map<String, List<CmdInfo>> sortedSupplier() {
        final List<CmdInfo> cmdInfoList = pl.getCommandManager().getRootCommand(getName())
                .getSubCommands().entries().stream()
                .filter((entry) -> !entry.getValue().isPrivate())
                .filter((entry) -> entry.getValue().getAnnotation(Subcommand.class) != null)
                .map((entry) -> {
                    String sectionName;
                    try { sectionName = ((CommandSection) entry.getValue().getAnnotation(CommandSection.class)).value(); }
                    catch (NullPointerException e) { sectionName = null; }

                    return new CmdInfo(
                            ((Subcommand) entry.getValue().getAnnotation(Subcommand.class)).value(),
                            entry.getValue().getHelpText(),
                            sectionName
                    );
                })
                .sorted((e1, e2) -> (e1.cmd.length() <= e2.cmd.length()) ? -1 : 1)
                .distinct()
                .collect(Collectors.toList());

        final Map<String, List<CmdInfo>> infoMap = new HashMap<>();
        for (CmdInfo cmdInfo : cmdInfoList) {
            final List<CmdInfo> infoList = infoMap.getOrDefault(cmdInfo.section, new LinkedList<>());
            infoList.add(cmdInfo);
            infoMap.put(cmdInfo.section, infoList);
        }

        return infoMap;
    }



    private static final class CmdInfo {
        private final String cmd;
        private final String desc;
        private final String section;

        public CmdInfo(String cmd, String desc, String section) {
            this.cmd = cmd.replaceAll("\\s*\\|\\s*", " | ");
            this.desc = desc;
            this.section = section;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CmdInfo cmdInfo = (CmdInfo) o;
            return Objects.equals(cmd, cmdInfo.cmd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cmd);
        }
    }
}
