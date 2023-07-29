package com.conorsmine.net.messages;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.inventory.NBTItemTags;
import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MsgFormatter {

    private static final Map<Character, Integer> fontWidthMap = new HashMap<>();

    static {
        try {
            final InputStream resourceStream = MsgFormatter.class.getResourceAsStream("/fontWidths.txt");
            if (resourceStream == null) throw new IOException("Could not retrieve \"fontWidths.txt\" from plugin resource path.");

            final BufferedInputStream stream = new BufferedInputStream(resourceStream);
            final InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);


            final BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while (line != null) {

                final char c = line.charAt(0);
                final int width = Integer.parseInt(line.substring(2));

                fontWidthMap.put(c, width);

                line = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static int getWidth(String msg, boolean removeColors) {
        if (removeColors) msg = removeColors(msg);

        int width = 0;
        for (char c : msg.toCharArray()) { width += fontWidthMap.getOrDefault(c, 6); }
        return width;
    }

    public static String getEmptyStringFromWidth(int width) {
        final int spaceWidth = fontWidthMap.getOrDefault(' ', 4);
        int length = Math.floorDiv(width, spaceWidth);
        if ((width % spaceWidth) != 0) length++;

        final char[] chars = new char[length];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public static String removeColors(final String msg) {
        return msg.replaceAll("§§[0-9|a-f|k-o|r]", "");
    }
    
    public static void sendFormattedListMsg(final CommandSender sender, final List<NBTCompound> itemNBTs) {
        if (sender instanceof Player) sendPlayerFormattedMsg(sender, itemNBTs);
        else sendConsoleFormattedMsg(sender, itemNBTs);
    }

    private static void sendConsoleFormattedMsg(final CommandSender sender, final List<NBTCompound> itemNBTs) {
        PlayerDataManipulator.getINSTANCE().sendMsg(sender, "§9Items §6Data §bSlot");

        for (NBTCompound nbt : itemNBTs) {
            PlayerDataManipulator.getINSTANCE().sendMsg(sender, String.format(
                    "§7  >> §9%s §6%s §b%s",
                    nbt.getString(NBTItemTags.ID.getTagName()),
                    nbt.getShort(NBTItemTags.DAMAGE.getTagName()),
                    nbt.getInteger(NBTItemTags.SLOT.getTagName())
            ));
        }
    }

    private static void sendPlayerFormattedMsg(final CommandSender sender, final List<NBTCompound> itemNBTs) {
        final int longestNameWidth = getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getString(NBTItemTags.ID.getTagName())) + 8;
        final int longestDamageWidth = getLongestItemDataWidth(itemNBTs, (nbt) -> nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString()) + 24;


        sendPlayerHeader(sender, longestNameWidth, longestDamageWidth);
        for (NBTCompound nbt : itemNBTs) {
            final String itemId = nbt.getString(NBTItemTags.ID.getTagName());
            final String itemDamage = nbt.getShort(NBTItemTags.DAMAGE.getTagName()).toString();


            final String spacesToDamageData = MsgFormatter.getEmptyStringFromWidth(longestNameWidth - MsgFormatter.getWidth(itemId, true));
            final String spacesToSlot = MsgFormatter.getEmptyStringFromWidth(longestDamageWidth - MsgFormatter.getWidth(itemDamage, true));

            PlayerDataManipulator.getINSTANCE().sendMsg(sender, String.format(
                    "§7  >> §9%s%s§6%s%s§b%s",
                    itemId,         spacesToDamageData,
                    itemDamage,     spacesToSlot,
                    nbt.getInteger(NBTItemTags.SLOT.getTagName())
            ));
        }
    }

    private static void sendPlayerHeader(final CommandSender sender, int longestName, int longestDamage) {
        final String toItemSpaces = MsgFormatter.getEmptyStringFromWidth(MsgFormatter.getWidth("  >> ", false));
        final String toDataSpaces = MsgFormatter.getEmptyStringFromWidth(longestName - MsgFormatter.getWidth("Item", false));
        final String toSlotSpaces = MsgFormatter.getEmptyStringFromWidth(longestDamage - MsgFormatter.getWidth("Data", false));

        PlayerDataManipulator.getINSTANCE().sendMsg(sender, String.format("%s§9Items%s§6Data%s§bSlot", toItemSpaces, toDataSpaces, toSlotSpaces));
    }

    private static int getLongestItemDataWidth(final List<NBTCompound> itemNBTs, final Function<NBTCompound, String> nbtDataFunction) {
        int width = 0;

        for (NBTCompound itemNBT : itemNBTs) {
            final int idWidth = MsgFormatter.getWidth(nbtDataFunction.apply(itemNBT), true);
            if (idWidth > width) width = idWidth;
        }

        return width;
    }
}
