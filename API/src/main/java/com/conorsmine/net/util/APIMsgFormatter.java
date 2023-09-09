package com.conorsmine.net.util;

import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class APIMsgFormatter {

    private static final Map<Character, Integer> fontWidthMap = new HashMap<>();

    static {
        try {
            final InputStream resourceStream = APIMsgFormatter.class.getResourceAsStream("/fontWidths.txt");
            if (resourceStream == null)
                throw new IOException("Could not retrieve \"fontWidths.txt\" from plugin resource path.");

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
        for (char c : msg.toCharArray()) {
            width += fontWidthMap.getOrDefault(c, 6);
        }
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

    public static int getLongestItemDataWidth(final List<NBTCompound> itemNBTs, final Function<NBTCompound, String> nbtDataFunction) {
        int width = 0;

        for (NBTCompound itemNBT : itemNBTs) {
            final int idWidth = getWidth(nbtDataFunction.apply(itemNBT), true);
            if (idWidth > width) width = idWidth;
        }

        return width;
    }
}
