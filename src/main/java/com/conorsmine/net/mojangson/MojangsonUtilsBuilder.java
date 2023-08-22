package com.conorsmine.net.mojangson;

import com.conorsmine.net.mojangson.path.NBTPath;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTType;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MojangsonUtilsBuilder {

    private final Map<Changes, Consumer<MojangsonUtils>> changesMap = new HashMap<>();

    public MojangsonUtilsBuilder(@NotNull final String separator) {
        changesMap.put(Changes.SEP, (utils) -> utils.separator = StringUtils.toNonRegex(separator));
    }

    /**
     * <br> default {@link ChatColor#GREEN}
     * @param color {@link ChatColor} to be set for the values of primitive NBT.
     */
    public MojangsonUtilsBuilder setValCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.VAL_COL, (utils) -> utils.valCol = color);
        return this;
    }

    /**
     * <br> Example: "0.2f"; The 'f' is the data type of the primitive NBT.
     * <br> default {@link ChatColor#GREEN}
     * @param color {@link ChatColor} to be set for the data type of primitive NBT.
     */
    public MojangsonUtilsBuilder setValTypeCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.VAL_TYPE_COL, (utils) -> utils.valTypeCol = color);
        return this;
    }

    /**
     * <br> default {@link ChatColor#GOLD}
     * @param color {@link ChatColor} to be set for the keys of the NBT.
     */
    public MojangsonUtilsBuilder setTagCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.TAG_COL, (utils) -> utils.tagCol = color);
        return this;
    }

    /**
     * <br> default {@link ChatColor#AQUA}
     * @param color {@link ChatColor} to be set for the brackets around {@link de.tr7zw.nbtapi.NBTCompound}.
     */
    public MojangsonUtilsBuilder setCompCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.OBJ_COL, (utils) -> utils.objCol = color);
        return this;
    }

    /**
     * <br> default {@link ChatColor#LIGHT_PURPLE}
     * @param color {@link ChatColor} to be set for the brackets around {@link de.tr7zw.nbtapi.NBTList}.
     */
    public MojangsonUtilsBuilder setArrCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.ARR_COL, (utils) -> utils.arrCol = color);
        return this;
    }

    /**
     * <br> default {@link ChatColor#RED}
     * @param color {@link ChatColor} to be set for {@link #addSpecialPaths(NBTPath...)}.
     */
    public MojangsonUtilsBuilder setSpecCol(@NotNull final ChatColor color) {
        changesMap.put(Changes.SPEC_COL, (utils) -> utils.specCol = color);
        return this;
    }

    /**
     * @param specialPaths {@link com.conorsmine.net.mojangson.path.NBTPath} which will be highlighted.
     */
    public MojangsonUtilsBuilder addSpecialPaths(final NBTPath... specialPaths) {
        changesMap.put(Changes.SPEC_PATHS, (utils) -> utils.specialColorPaths.addAll(Arrays.asList(specialPaths)));
        return this;
    }

    /**
     * <br> Hovering over the text created by {@link MojangsonUtils#getInteractiveMojangson(NBTCompound, NBTPath)} will show the {@link NBTPath}.
     * <br>
     * <br> default {@link Boolean#TRUE}
     */
    public MojangsonUtilsBuilder setHoverable(final boolean bool) {
        changesMap.put(Changes.HOV, (utils) -> utils.hoverable = bool);
        return this;
    }

    /**
     * <br> Clicking on the text created by {@link MojangsonUtils#getInteractiveMojangson(NBTCompound, NBTPath)} will execute a command with the format from {@link #setClickCmdFormat(String)}.
     * <br>
     * <br> default {@link Boolean#FALSE}
     */
    public MojangsonUtilsBuilder setClickable(final boolean bool) {
        changesMap.put(Changes.CLICK, (utils) -> utils.clickable = bool);
        return this;
    }

    /**
     * <br> Only these specified {@link NBTType} can be clicked on and run the command with the format from {@link #setClickCmdFormat(String)}
     * <br>
     * <br> default {@link NBTType#values()}
     * @param clickables All {@link NBTType} that may be clicked on
     */
    public MojangsonUtilsBuilder setClickTypes(final Collection<NBTType> clickables) {
        changesMap.put(Changes.CLICKABLES, (utils) -> {
            utils.clickTypes.clear();
            utils.clickTypes.addAll(clickables);
        });
        return this;
    }

    /**
     * {@link #setClickTypes(Collection)}
     */
    public MojangsonUtilsBuilder setClickTypes(final NBTType... clickables) {
        return setClickTypes(Arrays.asList(clickables));
    }

    /**
     * <br> The command will be run once NBT is clicked from the {@link MojangsonUtils#getInteractiveMojangson(NBTCompound, NBTPath)}.
     * <br> "%s" can be inserted to use the {@link NBTPath} of the NBT in the command.
     * <br>
     * <br> default null
     * @param cmdFormat Format of the command that will be run once the NBT is clicked on.
     */
    public MojangsonUtilsBuilder setClickCmdFormat(@NotNull final String cmdFormat) {
        changesMap.put(Changes.CMD_FORMAT, (utils) -> utils.cmdFormat = (cmdFormat.charAt(0) == '/') ? cmdFormat : "/" + cmdFormat);
        return this;
    }

    /**
     * @return Configured {@link MojangsonUtils}
     */
    public MojangsonUtils create() {
        final MojangsonUtils utils = new MojangsonUtils();
        changesMap.forEach((k, v) -> v.accept(utils));

        return utils;
    }


    private enum Changes {
        SEP,
        VAL_COL,
        VAL_TYPE_COL,
        TAG_COL,
        OBJ_COL,
        ARR_COL,
        SPEC_COL,

        SPEC_PATHS,
        HOV,
        CLICK,
        CLICKABLES,
        CMD_FORMAT,
    }
}
