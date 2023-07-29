package com.conorsmine.net;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTListCompound;
import de.tr7zw.nbtapi.NBTType;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MojangsonUtils {

    public static final Set<NBTType> SIMPLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            NBTType.NBTTagByte, NBTType.NBTTagDouble, NBTType.NBTTagFloat, NBTType.NBTTagInt, NBTType.NBTTagLong, NBTType.NBTTagShort, NBTType.NBTTagString
    )));
    public static final Set<NBTType> NON_SIMPLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            NBTType.NBTTagByteArray, NBTType.NBTTagCompound, NBTType.NBTTagIntArray, NBTType.NBTTagList
    )));

    public String separator = ".";

    public ChatColor valCol = ChatColor.GREEN;
    public ChatColor valTypeCol = ChatColor.GREEN;
    public ChatColor tagCol = ChatColor.GOLD;
    public ChatColor objCol = ChatColor.AQUA;
    public ChatColor arrCol = ChatColor.LIGHT_PURPLE;
    public ChatColor specCol = ChatColor.RED;

    public boolean clickable = true;
    public boolean hoverable = true;
    private final Set<NBTType> clickTypes = new HashSet<>(Arrays.asList(NBTType.values()));

    public String cmdFormat = "/bn hidden_cmd %s";
    public String invalidClickTargetFormat = "/bn hidden_cmd INVALID %s";
    private String[] specialColorPaths = new String[0];

    ///////////////////////////////////////////////////////////////
    // Formatted, colored and interactable Mojangson
    ///////////////////////////////////////////////////////////////

    public BaseComponent[] getInteractiveMojangson(final NBTCompound compound, final String optionalPath) {
        final ComponentBuilder prettyString = new ComponentBuilder(objCol + "{§f");
        recursive(optionalPath, compound, prettyString).append(objCol + "}§f");

        return prettyString.create();
    }

    private ComponentBuilder recursive(String path, final NBTCompound compound, ComponentBuilder prettyString) {
        final Iterator<String> compoundIterator = compound.getKeys().iterator();
        while (compoundIterator.hasNext()) {
            final String key = compoundIterator.next();
            final NBTType type = compound.getType(key);
            final String newPath = (StringUtils.isBlank(path)) ? key : String.format("%s%s%s", path, separator, key);
            boolean shouldColor = isColoring(newPath);

            if (type == NBTType.NBTTagCompound)
                evaluateCompoundTag(compound, key, path, prettyString, !compoundIterator.hasNext(), shouldColor);

            else if (type == NBTType.NBTTagList)
                evaluateCompoundList(compound, key, newPath, prettyString, !compoundIterator.hasNext(), shouldColor);

            else
                evaluateSimpleCompound(compound, key, newPath, prettyString, !compoundIterator.hasNext(), shouldColor);
        }

        return prettyString;
    }

    private void evaluateSimpleCompound(final NBTCompound compound, String key, String path, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        String evaluatedString;
        if (shouldColor) evaluatedString = String.format(specCol + "%s: %s§f", key, evaluateSimpleCompoundToString(compound, key));
        else evaluatedString = String.format(tagCol + "%s: " + valCol + "%s§f", key, evaluateSimpleCompoundToString(compound, key));
        if (!lastCompound) evaluatedString += ", ";

        boolean isClickType = clickTypes.contains(compound.getType(key));
        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        if (hoverable) pathDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(((isClickType) ? "§a" : "§c") + path).create()));
        if (clickable && isClickType) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(cmdFormat, path)));
        else if (clickable) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(invalidClickTargetFormat, path)));
        prettyString.append(pathDisplay);
    }

    private void evaluateCompoundList(final NBTCompound compound, String key, String path, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        String evaluatedString;
        if (shouldColor) evaluatedString = String.format("%s%s[§f", specCol, key);
        else evaluatedString = String.format("%s%s:%s[§f", tagCol, key, arrCol);

        boolean isClickType = clickTypes.contains(compound.getType(key));
        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        if (hoverable) pathDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(((isClickType) ? "§a" : "§c") + path).create()));
        if (clickable && isClickType) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(cmdFormat, path)));
        else if (clickable) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(invalidClickTargetFormat, path)));
        prettyString.append(pathDisplay);

        final Iterator<NBTListCompound> compoundListIterator = compound.getCompoundList(key).iterator();
        int i = 0;
        while (compoundListIterator.hasNext()) {
            final NBTListCompound readWriteNBT = compoundListIterator.next();
            String newArrPath = String.format("%s[%d]", path, i);
            boolean colorArrObj = isColoring(newArrPath);

            if (colorArrObj) prettyString.append(String.format("%s{§f", specCol));
            else prettyString.append(String.format("%s{§f", objCol));
            recursive(newArrPath, (NBTCompound) readWriteNBT, prettyString);

            if (colorArrObj) prettyString.append(String.format("%s}§f", specCol));
            else prettyString.append(String.format("%s}§f", objCol));
            if (compoundListIterator.hasNext()) prettyString.append(", §f");

            i++;
        }


        if (shouldColor) prettyString.append(String.format("%s]§f", specCol));
        else prettyString.append(String.format("%s]§f", arrCol));
        if (!lastCompound) prettyString.append(", §f");
    }

    private void evaluateCompoundTag(final NBTCompound compound, String key, String path, ComponentBuilder prettyString, boolean lastCompound, boolean shouldColor) {
        String evaluatedString;
        if (shouldColor) evaluatedString = String.format("%s%s:%s{", specCol, key, specCol);
        else evaluatedString = String.format("%s%s:%s{", tagCol, key, objCol);

        boolean isClickType = clickTypes.contains(compound.getType(key));
        final TextComponent pathDisplay = new TextComponent(evaluatedString);
        String newPath = (StringUtils.isBlank(path)) ? key : String.format("%s%s%s", path, separator, key);
        if (hoverable) pathDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(((isClickType) ? "§a" : "§c") + newPath).create()));
        if (clickable && isClickType) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(cmdFormat, newPath)));
        else if (clickable) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(invalidClickTargetFormat, newPath)));
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

    private boolean isColoring(String newPath) {
        for (String colPath : specialColorPaths) {
            if (!removeArrIndexes(newPath).startsWith(removeArrIndexes(colPath))) continue;
            if (!arrIndexMatch(newPath, colPath)) continue;

            return true;
        }

        return false;
    }

    private boolean arrIndexMatch(String newPath, String colPath) {
        String[] pathKeyArr = pathToKeys(newPath);
        String[] colKeyArr = pathToKeys(colPath);

        for (int i = 0; i < colKeyArr.length; i++) {
            String pathKey = pathKeyArr[i];
            String colKey = colKeyArr[i];

            if (!isArr(pathKey)) continue;
            if (!isArr(colKey)) return false;
            if (isFullArray(colKey)) continue;
            if (getIndexOfArrayKey(pathKey) != getIndexOfArrayKey(colKey)) return false;
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////

    @Nullable
    public NBTResult getCompoundFromPathSneakyThrow(final NBTCompound compound, final String path) {
        try {
            return getCompoundFromPath(compound, path);
        } catch (Exception ignored) { }

        return null;
    }

    public NBTResult getCompoundFromPath(final NBTCompound compound, final String path) {
        if (path.isEmpty()) return new NBTResult(compound, path, getLastKey(path));

        // Returning a NBTResult, as the data could be in a NBTList instead
        return new NBTResult(recursiveCompoundFromPath(compound, path), path, getLastKey(path));
    }

    public Object getSimpleDataFromCompound(final NBTResult nbtResult) {
        String key = nbtResult.getFinalKey();
        NBTType type = nbtResult.getCompound().getType(key);
        NBTCompound compound = nbtResult.getCompound();
        if (!SIMPLE_TYPES.contains(type))
            new Exception(String.format("\"%s\" is NOT a simple NBTType!", nbtResult.getFinalKey())).printStackTrace();


        if (type == NBTType.NBTTagInt) return compound.getInteger(key);
        else if (type == NBTType.NBTTagLong) return compound.getLong(key);
        else if (type == NBTType.NBTTagByte) return compound.getByte(key);
        else if (type == NBTType.NBTTagFloat) return compound.getFloat(key);
        else if (type == NBTType.NBTTagShort) return compound.getShort(key);
        else if (type == NBTType.NBTTagDouble) return compound.getDouble(key);
        else if (type == NBTType.NBTTagString) return  compound.getString(key);
        else new Exception(String.format("\"%s\" was not parsable!", nbtResult.getFinalKey())).printStackTrace();
        return null;
    }

    public Set<String> getAllPaths(final NBTCompound compound) {
        return pathRecursion(compound, new HashSet<>(), "");
    }

    // Reduces the set provided by getAllPaths to only those
    public Set<String> getAllSimplePaths(final NBTCompound compound) {
        Set<String> simplePaths = new HashSet<>();
        for (String path : getAllPaths(compound)) {
            NBTResult nbtResult = getCompoundFromPath(compound, path);
            NBTType type = nbtResult.getCompound().getType(nbtResult.getFinalKey());

            if (!SIMPLE_TYPES.contains(type)) continue;
            simplePaths.add(path);
        }

        return simplePaths;
    }

    @Nullable
    private NBTCompound recursiveCompoundFromPath(final NBTCompound compound, final String path) {
        final String[] keys = pathToKeys(path);
        final String key = keys[0];
        final String newPath = removeFirstKey(path);
        boolean isFinalKey = (keys.length == 1);


        if (key.matches(".+\\[\\d]$")) {
            final NBTCompound newCompound = compound.getCompoundList(getArrayKeyValue(key))
                    .get(getIndexOfArrayKey(key));
            return recursiveCompoundFromPath(newCompound, newPath);
        }
        if (isFinalKey || isFullArray(key)) return compound;
        if (!compound.getCompound(key).hasKey(newPath)) return null;
        return recursiveCompoundFromPath(compound.getCompound(key), newPath);
    }

    private Set<String> pathRecursion(NBTCompound compound, Set<String> paths, String currentPath) {
        for (String key : compound.getKeys()) {
            final NBTType type = compound.getType(key);
            final String newPath = (StringUtils.isBlank(currentPath)) ? key : String.format("%s%s%s", currentPath, separator, key);
            paths.add(newPath);
            if (!(type == NBTType.NBTTagCompound || type == NBTType.NBTTagList)) continue;

            if (type == NBTType.NBTTagCompound) pathRecursionTag(compound.getCompound(key), paths, newPath);
            else pathRecursionList(compound.getCompoundList(key), paths, newPath);
        }

        return paths;
    }

    private void pathRecursionTag(NBTCompound compound, Set<String> paths, String currentPath) {
        paths.add(currentPath);
        pathRecursion(compound, paths, currentPath);
    }

    private void pathRecursionList(NBTCompoundList compoundList, Set<String> paths, String currentPath) {
        for (int i = 0; i < compoundList.size(); i++) {
            final NBTCompound compound = compoundList.get(i);
            String newPath = String.format("%s[%d]", currentPath, i);
            paths.add(newPath);

            pathRecursion(compound, paths, newPath);
        }
    }

    // Returns null if the type of the key is not simple
    public static Object getSimpleDataFromCompound(final NBTCompound compound, String key) {
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

    public static void setSimpleDataFromKey(NBTCompound compound, String key, final Object data) {
        if (attemptTypeDefference(compound, key, data)) return;

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

    // Tries to find out what the type is and if the data can be converted to that type
    private static boolean attemptTypeDefference(NBTCompound compound, String key, final Object data) {
        final String dataStr = data.toString();

        try {
            switch (compound.getType(key)) {
                case NBTTagInt: compound.setInteger(key, Integer.valueOf(dataStr)); break;
                case NBTTagLong: compound.setLong(key, Long.valueOf(dataStr)); break;
                case NBTTagByte: compound.setByte(key, Byte.valueOf(dataStr)); break;
                case NBTTagFloat: compound.setFloat(key, Float.valueOf(dataStr)); break;
                case NBTTagShort: compound.setShort(key, Short.valueOf(dataStr)); break;
                case NBTTagDouble: compound.setDouble(key, Double.valueOf(dataStr)); break;
                case NBTTagString:  compound.setString(key, dataStr); break;
            }
        }
        catch (Exception e) { return false; }

        return true;
    }




    // Items[0].tag.Items[..] -> {Items[0], tag, Items[..]}
    private String[] pathToKeys(final String path) {
        final String regex = String.format("(?<!\\.)\\%s(?!\\.)", separator);
        return path.split(regex);
    }

    // Items[0].tag.Items[..] -> Items[..]
    private String getLastKey(final String path) {
        final String regex = String.format(".+\\%s(?=\\w)", separator);
        return path.replaceAll(regex, "");
    }

    // Items[0].tag.Items[..] -> Items[0]
    private String getFirstKey(final String path) {
        final String regex = String.format("\\%s(?=\\w).+", separator);
        return path.replaceAll(regex, "");
    }

    // Items[0].tag.Items[..] -> tag.Items[..]
    private String removeFirstKey(final String path) {
        final String regex = String.format("\\%s(?=\\w)", separator);
        String[] out = path.split(regex, 2);
        return (out.length >= 2) ? out[1] : "";
    }

    // Items[0].tag.Items[..] -> Items[0].tag
    private String removeLastKey(final String path) {
        final String regex = String.format("\\%s\\w(?!.*\\%s\\w).*", separator, separator);
        String out = path.replaceAll(regex, "");
        return (out.length() == path.length()) ? "" : out;
    }

    // Items[0] -> true     id -> false
    private boolean isArr(final String key) {
        if (key.length() <= 3) return false;
        return key.charAt(key.length() - 1) == ']';
    }

    // Items[..] -> true    Items[0] -> false
    private boolean isFullArray(final String key) {
        return key.matches(".+\\.]$");
    }

    // Items[0] -> Items
    private String getArrayKeyValue(final String key) {
        return key.replaceAll("\\[\\d]$", "").replaceAll("\\[\\.{2}]$", "");
    }

    // Items[0] -> 0
    private int getIndexOfArrayKey(final String key) {
        return Integer.parseInt(key.replaceAll(".+\\[|]$", ""));
    }

    // Items[..] -> Items   tag.ench[0].custom[..] -> tag.ench[].custom[]
    private String removeArrIndexes(final String key) {
        return key.replaceAll("\\[((\\d+)|(\\.\\.))]", "[]");
    }


    /*
     * This will represent the NBTCompound and the final key to access the data
     * */
    public static class NBTResult {
        private final NBTCompound compound;
        private final String path;
        private final String finalKey;

        public NBTResult(NBTCompound compound, String path, String finalKey) {
            this.compound = compound;
            this.path = path;
            this.finalKey = finalKey;
        }

        public NBTCompound getCompound() {
            return compound;
        }

        public String getPath() {
            return path;
        }

        public String getFinalKey() {
            return finalKey;
        }
    }

    public MojangsonUtils setSeparator(String separator) {
        this.separator = separator;
        return this;
    }
}
