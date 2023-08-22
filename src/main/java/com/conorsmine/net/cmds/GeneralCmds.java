package com.conorsmine.net.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.utils.Lazy;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandAlias("pdm")
public class GeneralCmds extends BaseCommand {

    private final Lazy<List<CmdInfo>> SORTED_HELP_CMD = new Lazy<>(this::sortedSupplier);

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

        for (CmdInfo cmdInfo : SORTED_HELP_CMD.get()) {
            final String cmdName = String.format("  §7>> §9/%s %s", getName(), cmdInfo.cmd);
            if (!(sender instanceof Player)) { pl.sendMsg(sender, cmdName); continue; }

            final TextComponent cmdMsg = new TextComponent(cmdName);
            cmdMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(cmdInfo.desc).create()));
            ((Player) sender).spigot().sendMessage(cmdMsg);
        }
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
    private List<CmdInfo> sortedSupplier() {
        return pl.getCommandManager()
                .getRootCommand(getName()).getSubCommands().entries().stream()
                .filter((entry) -> !entry.getValue().isPrivate())
                .filter((entry) -> entry.getValue().getAnnotation(Subcommand.class) != null)
                .map((entry) -> new CmdInfo(
                            ((Subcommand) entry.getValue().getAnnotation(Subcommand.class)).value(), entry.getValue().getHelpText()
                ))
                .sorted((e1, e2) -> (e1.cmd.length() <= e2.cmd.length()) ? -1 : 1)
                .distinct()
                .collect(Collectors.toList());
    }



    private static final class CmdInfo {
        private final String cmd;
        private final String desc;

        public CmdInfo(String cmd, String desc) {
            this.cmd = cmd.replaceAll("\\s*\\|\\s*", " | ");
            this.desc = desc;
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
