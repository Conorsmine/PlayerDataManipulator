package com.conorsmine.net.mojangson;

import com.conorsmine.net.mojangson.path.NBTArrayKey;
import com.conorsmine.net.mojangson.path.NBTKey;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTListCompound;
import de.tr7zw.nbtapi.NBTType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MojangsonUtils {

    public static final Set<NBTType> SIMPLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            NBTType.NBTTagByte, NBTType.NBTTagDouble, NBTType.NBTTagFloat, NBTType.NBTTagInt, NBTType.NBTTagLong, NBTType.NBTTagShort, NBTType.NBTTagString
    )));
    public static final Set<NBTType> NON_SIMPLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            NBTType.NBTTagByteArray, NBTType.NBTTagCompound, NBTType.NBTTagIntArray, NBTType.NBTTagList
    )));

    String separator = null;

    ChatColor valCol = ChatColor.GREEN;
    ChatColor valTypeCol = ChatColor.GREEN;
    ChatColor tagCol = ChatColor.GOLD;
    ChatColor objCol = ChatColor.AQUA;
    ChatColor arrCol = ChatColor.LIGHT_PURPLE;
    ChatColor specCol = ChatColor.RED;

    boolean clickable = false;
    boolean hoverable = true;

    String cmdFormat = null;
    final Set<NBTType> clickTypes = new HashSet<>(Arrays.asList(NBTType.values()));
    final Set<NBTPath> specialColorPaths = new HashSet<>();

    MojangsonUtils() { }

    ///////////////////////////////////////////////////////////////
    // Formatted, colored and interactable Mojangson
    ///////////////////////////////////////////////////////////////

    public BaseComponent[] getInteractiveMojangson(final NBTCompound compound, final NBTPath optionalPath) {
        final ComponentBuilder prettyString = new ComponentBuilder(objCol + "{§f");
        recursive(optionalPath, compound, prettyString).append(objCol + "}§f");

        return prettyString.create();
    }

    private ComponentBuilder recursive(NBTPath nbtPath, final NBTCompound compound, ComponentBuilder prettyString) {
        final Iterator<String> compoundIterator = compound.getKeys().iterator();
        while (compoundIterator.hasNext()) {
            final String key = compoundIterator.next();
            final NBTType type = compound.getType(key);
            final NBTPath newPath = new NBTPathBuilder(this).addNBTPath(nbtPath).parseString(key).create();
            boolean shouldColor = isColoring(newPath);

            if (type == NBTType.NBTTagCompound)
                evaluateCompoundTag(compound, key, nbtPath, prettyString, !compoundIterator.hasNext(), shouldColor);

            else if (type == NBTType.NBTTagList)
                evaluateCompoundList(compound, key, newPath, prettyString, !compoundIterator.hasNext(), shouldColor);

            else
                evaluateSimpleCompound(compound, newPath, prettyString, !compoundIterator.hasNext(), shouldColor);
        }

        return prettyString;
    }

    private void evaluateSimpleCompound(final NBTCompound compound, NBTPath nbtPath, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        final String key = nbtPath.getLastKey().getKeyValue();
        String evaluatedString;
        if (shouldColor) {
            evaluatedString = String.format(specCol + "%s: %s§f", key, evaluateSimpleCompoundToString(compound, key));
        } else evaluatedString = String.format(tagCol + "%s: " + valCol + "%s§f", key, evaluateSimpleCompoundToString(compound, key));
        if (!lastCompound) evaluatedString += ", ";

        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        addClickAndHover(pathDisplay, compound.getType(key), nbtPath);
        prettyString.append(pathDisplay);
    }

    private void evaluateCompoundList(final NBTCompound compound, String key, NBTPath nbtPath, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        String evaluatedString;
        if (shouldColor) evaluatedString = String.format("%s%s[§f", specCol, key);
        else evaluatedString = String.format("%s%s:%s[§f", tagCol, key, arrCol);

        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        addClickAndHover(pathDisplay, compound.getType(key), nbtPath);
        prettyString.append(pathDisplay);

        final Iterator<NBTListCompound> compoundListIterator = compound.getCompoundList(key).iterator();
        int i = 0;
        while (compoundListIterator.hasNext()) {
            final NBTListCompound readWriteNBT = compoundListIterator.next();
            final NBTPath newArrPath = new NBTPathBuilder(this).addNBTPath(nbtPath).parseString(String.format("[%d]", i)).create();
            boolean colorArrObj = isColoring(newArrPath);

            if (colorArrObj) prettyString.append(String.format("%s{§f", specCol));
            else prettyString.append(String.format("%s{§f", objCol));
            recursive(newArrPath, readWriteNBT, prettyString);

            if (colorArrObj) prettyString.append(String.format("%s}§f", specCol));
            else prettyString.append(String.format("%s}§f", objCol));
            if (compoundListIterator.hasNext()) prettyString.append(", §f");

            i++;
        }


        if (shouldColor) prettyString.append(String.format("%s]§f", specCol));
        else prettyString.append(String.format("%s]§f", arrCol));
        if (!lastCompound) prettyString.append(", §f");
    }

    private void evaluateCompoundTag(final NBTCompound compound, String key, NBTPath nbtPath, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        String evaluatedString;
        if (shouldColor) evaluatedString = String.format("%s%s:%s{", specCol, key, specCol);
        else evaluatedString = String.format("%s%s:%s{", tagCol, key, objCol);

        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        final NBTPath newPath = new NBTPathBuilder(this).addNBTPath(nbtPath).parseString(key).create();

        addClickAndHover(pathDisplay, compound.getType(key), newPath);
        prettyString.append(pathDisplay);

        recursive(newPath, compound.getCompound(key), prettyString);

        if (shouldColor) prettyString.append(specCol + "}§f");
        else prettyString.append(objCol + "}§f");
        if (!lastCompound) prettyString.append(", ");
    }

    private String evaluateSimpleCompoundToString(final NBTCompound compound, String key) {
        final NBTType type = compound.getType(key);

        if (type == NBTType.NBTTagInt) return String.valueOf(compound.getInteger(key));
        else if (type == NBTType.NBTTagLong) return compound.getLong(key) + "l§f";
        else if (type == NBTType.NBTTagByte) return compound.getByte(key) + "b§f";
        else if (type == NBTType.NBTTagFloat) return compound.getFloat(key) + "f§f";
        else if (type == NBTType.NBTTagShort) return compound.getShort(key) + "s§f";
        else if (type == NBTType.NBTTagDouble) return compound.getDouble(key) + "d§f";
        else if (type == NBTType.NBTTagString) return  "\"" + compound.getString(key) + "\"§f";
        else return "§cSOMETHING WENT WRONG§f";
    }

    private boolean isColoring(NBTPath nbtPath) {
        return this.specialColorPaths.contains(nbtPath);
    }

    private void addClickAndHover(TextComponent pathDisplay, NBTType nbtType, NBTPath nbtPath) {
        boolean isClickType = this.clickTypes.contains(nbtType);
        if (hoverable) pathDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(((isClickType) ? "§a" : "§c") + nbtPath).create()));
        if (clickable && isClickType) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(cmdFormat, nbtPath)));
    }


    ///////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////

    @Nullable
    public NBTResult getCompoundFromPathSneakyThrow(final NBTCompound compound, final NBTPath path) {
        try {
            return getCompoundFromPath(compound, path);
        } catch (Exception ignored) { }

        return null;
    }

    public NBTResult getCompoundFromPath(final NBTCompound compound, final NBTPath path) {
        if (path.isEmpty()) return new NBTResult(compound, path, path.getLastKey());

        // Returning a NBTResult, as the data could be in a NBTList instead
        return new NBTResult(recursiveCompoundFromPath(compound, path), path, path.getLastKey());
    }

    @SuppressWarnings("unchecked")
    public <T> T getSimpleDataFromCompound(final Class<T> clazzCast, final NBTResult nbtResult) {
        final String key = nbtResult.getFinalKey().getKeyValue();
        final NBTType type = nbtResult.getCompound().getType(key);
        final NBTCompound compound = nbtResult.getCompound();

        if (!SIMPLE_TYPES.contains(type))
            new Exception(String.format("\"%s\" is NOT a simple NBTType!", nbtResult.getFinalKey())).printStackTrace();

        if (Long.class.equals(clazzCast)) return ((T) compound.getLong(key));
        else if (Integer.class.equals(clazzCast)) return ((T) compound.getInteger(key));
        else if (Short.class.equals(clazzCast)) return ((T) compound.getShort(key));
        else if (Byte.class.equals(clazzCast)) return ((T) compound.getByte(key));
        else if (Float.class.equals(clazzCast)) return ((T) compound.getFloat(key));
        else if (Double.class.equals(clazzCast)) return ((T) compound.getDouble(key));
        else if (String.class.equals(clazzCast)) return ((T) compound.getString(key));
        else new Exception(String.format("\"%s\" is NOT supported!", clazzCast.getName())).printStackTrace();

        return null;
    }

    @Nullable
    private NBTCompound recursiveCompoundFromPath(final NBTCompound compound, final NBTPath path) {
        final NBTKey key = path.next();


        if (key.isArr()) {
            return recursiveCompoundFromPath(compound.getCompoundList(key.getKeyValue())
                    .get(((NBTArrayKey) key).getIndex()), path);
        }
        if (path.isLastKey()) return compound;        // Todo: is this right?
//        if (!compound.getCompound(key.getKeyValue()).hasKey(newPath)) return null;
        return recursiveCompoundFromPath(compound.getCompound(key.getKeyValue()), path);
    }

    // Returns null if the type of the key is not simple
    public static Object getSimpleDataFromCompound(final NBTCompound compound, NBTKey nbtKey) {
        final String key = nbtKey.getKeyValue();
        switch (compound.getType(key)) {
            case NBTTagInt: return compound.getInteger(key);
            case NBTTagLong: return compound.getLong(key);
            case NBTTagByte: return compound.getByte(key);
            case NBTTagFloat: return compound.getFloat(key);
            case NBTTagShort: return compound.getShort(key);
            case NBTTagDouble: return compound.getDouble(key);
            case NBTTagString: return  compound.getString(key);

            default: return null;
        }
    }

    public static void setSimpleDataFromKey(NBTCompound compound, NBTKey nbtKey, final Object data) {
        final String key = nbtKey.getKeyValue();

        if (data instanceof Byte) compound.setByte(key, ((Byte) data));
        else if (data instanceof Short) compound.setShort(key, ((Short) data));
        else if (data instanceof Integer) compound.setInteger(key, ((Integer) data));
        else if (data instanceof Long) compound.setLong(key, ((Long) data));
        else if (data instanceof Float) compound.setFloat(key, ((Float) data));
        else if (data instanceof Double) compound.setDouble(key, ((Double) data));
        else if (data instanceof String) compound.setString(key, ((String) data));
        else if (data instanceof UUID) compound.setUUID(key, ((UUID) data));

        else new UnsupportedOperationException(String.format("Object is not simple: \"%s\"", data)).printStackTrace();
    }

    /*
     * This will represent the NBTCompound and the final key to access the data
     * */
    public static class NBTResult {
        private final NBTCompound compound;
        private final NBTPath path;
        private final NBTKey finalKey;

        public NBTResult(NBTCompound compound, NBTPath path, NBTKey finalKey) {
            this.compound = compound;
            this.path = path;
            this.finalKey = finalKey;
        }

        public NBTCompound getCompound() {
            return compound;
        }

        public NBTPath getPath() {
            return path;
        }

        public NBTKey getFinalKey() {
            return finalKey;
        }
    }

    public String getSeparator() {
        return separator;
    }
}
