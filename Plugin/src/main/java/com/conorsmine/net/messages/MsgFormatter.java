package com.conorsmine.net.messages;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.items.NBTItemTags;
import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MsgFormatter {
    
    public static void sendFormattedListMsg(final PlayerDataManipulator pl, final CommandSender sender, final List<NBTCompound> itemNBTs) {
        if (sender instanceof Player) sendPlayerFormattedMsg(pl, sender, itemNBTs);
        else sendConsoleFormattedMsg(pl, sender, itemNBTs);
    }

    private static void sendConsoleFormattedMsg(final PlayerDataManipulator pl, final CommandSender sender, final List<NBTCompound> itemNBTs) {
        pl.sendMsg(sender, PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().consoleItemDataHeader());

        for (NBTCompound nbt : itemNBTs) { pl.sendMsg(sender, PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().consoleItemVersionDataFromNBT(nbt)); }
    }

    private static String sendPlayerHeader(final CommandSender sender, final List<NBTCompound> itemNBTs) {
        return PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().playerItemDataHeader(itemNBTs);
    }

    private static void sendPlayerFormattedMsg(final PlayerDataManipulator pl, final CommandSender sender, final List<NBTCompound> itemNBTs) {
        pl.sendMsg(sender, PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().playerItemDataHeader(itemNBTs));
        pl.sendMsg(sender, PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().playerItemVersionDataFromNBT(itemNBTs));
    }
}
