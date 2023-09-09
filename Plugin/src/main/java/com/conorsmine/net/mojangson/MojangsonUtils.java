package com.conorsmine.net.mojangson;

import com.conorsmine.net.mojangson.data.*;
import com.conorsmine.net.mojangson.path.NBTArrayKey;
import com.conorsmine.net.mojangson.path.NBTKey;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import de.tr7zw.nbtapi.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
public final class MojangsonUtils {

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

    public BaseComponent[] getInteractiveMojangson(@NotNull final NBTCompound compound, @Nullable final NBTPath optionalPath) {
        final ComponentBuilder prettyString = new ComponentBuilder(objCol + "{§f");
        recursive((optionalPath == null) ? new NBTPathBuilder(this).create() : optionalPath, compound, prettyString).append(objCol + "}§f");

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
        } else evaluatedString = String.format(tagCol + "%s: %s§f", key, evaluateSimpleCompoundToString(compound, key));
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
            final NBTPath newArrPath = new NBTPathBuilder(this).addNBTPath(nbtPath).addNBTKey(NBTArrayKey.parseToArrayKey(i)).create();
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

        if (type == NBTType.NBTTagInt) return this.valCol.toString() + compound.getInteger(key) + this.valTypeCol.toString() + "§f";
        else if (type == NBTType.NBTTagLong) return this.valCol.toString() + compound.getLong(key) + this.valTypeCol.toString() + "l§f";
        else if (type == NBTType.NBTTagByte) return this.valCol.toString() + compound.getByte(key) + this.valTypeCol.toString() + "b§f";
        else if (type == NBTType.NBTTagFloat) return this.valCol.toString() + compound.getFloat(key) + this.valTypeCol.toString() + "f§f";
        else if (type == NBTType.NBTTagShort) return this.valCol.toString() + compound.getShort(key) + this.valTypeCol.toString() + "s§f";
        else if (type == NBTType.NBTTagDouble) return this.valCol.toString() + compound.getDouble(key) + this.valTypeCol.toString() + "d§f";
        else if (type == NBTType.NBTTagString) return  this.valTypeCol.toString() + "\"" + this.valCol.toString() + compound.getString(key) + this.valTypeCol.toString() + "\"§f";
        else return "§cSOMETHING WENT WRONG§f";
    }

    private boolean isColoring(NBTPath nbtPath) {
        return this.specialColorPaths.contains(nbtPath);
    }

    private void addClickAndHover(TextComponent pathDisplay, NBTType nbtType, NBTPath nbtPath) {
        boolean isClickType = this.clickTypes.contains(nbtType);
        if (hoverable) pathDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(((isClickType) ? "§a" : "§c") + nbtPath).create()));

        if (clickable && cmdFormat == null) throw new UnsupportedOperationException("The cmdFormat is null!");
        if (clickable && isClickType) pathDisplay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmdFormat.replaceAll("%s", nbtPath.toString())));
    }


    ///////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////

    @Nullable
    public NBTQueryResult getDataFromPathSneakyThrow(@NotNull final INBTData<?> data, @NotNull final NBTPath path) {
        try { return getDataFromPath(data, path); }
        catch (Exception ignored) { }

        return null;
    }

    public NBTQueryResult getDataFromPath(@NotNull final INBTData<?> data, @NotNull final NBTPath path) {
        final INBTData<?> retrievedData = recursiveCompoundFromPath(data, path, 0);
        if (retrievedData == null)
            throw new UnsupportedOperationException(String.format("An error has occurred retrieving the data from the following path:\n%s", path));

        return new NBTQueryResult(path, retrievedData);
    }

    // TODO: 18/08/2023 Add different  Exceptions, so they can be treated separately
    private INBTData<?> recursiveCompoundFromPath(final INBTData<?> compound, final NBTPath path, int index) {
        if (index >= path.size()) return compound;

        final NBTKey currentKey = path.get(index);
        if (compound.getType() == NBTDataType.PRIMITIVE)
            throw new UnsupportedOperationException(String.format("Cannot retrieve data with key: %s; From primitives!", currentKey.getKeyValue()));

        raiseExceptionIfInvalidListCall(compound, currentKey, path);

        final INBTData<?> data;
        if (currentKey.isArr()) data = recursiveCompoundFromList(compound, path, index);
        else data = recursiveCompoundFromCompound(compound, path, index);

        return recursiveCompoundFromPath(data, path, ++index);
    }

    private INBTData<?> recursiveCompoundFromList(final INBTData<?> compound, final NBTPath path, int index) {
        final NBTKey currentKey = path.get(index);
        final int i = ((NBTArrayKey) currentKey).getIndex();
        final INBTListData<?> listData = (INBTListData<?>) compound;
        if (i < 0 || i >= listData.getData().size())
            throw new ArrayIndexOutOfBoundsException(String.format("Index: %d is out of bounds for NBT data list: %n%s", i, listData.getData()));

        if (compound.getType() == NBTDataType.PRIMITIVE_LIST) return new NBTPrimitiveData(((NBTPrimitiveListData) compound).getData().get(i));

        final NBTCompoundList compoundList = ((NBTCompoundListData) compound).getData();
        final NBTListCompound listCompound = compoundList.get(i);

        if (compoundList.getType() == NBTType.NBTTagCompound) return new NBTCompoundData(listCompound);
        else if (compoundList.getType() == NBTType.NBTTagList) throw new UnsupportedOperationException("Inconvertible types for NBTTagList!");
        else return new NBTPrimitiveData(listCompound.getCompound());
    }

    private INBTData<?> recursiveCompoundFromCompound(final INBTData<?> compound, final NBTPath path, int index) {
        final NBTKey currentKey = path.get(index);
        final NBTCompound data = ((NBTCompoundData) compound).getData();
        final NBTType type = data.getType(currentKey.getKeyValue());

        if (type == NBTType.NBTTagCompound) return new NBTCompoundData(data.getCompound(currentKey.getKeyValue()));
        else if (type == NBTType.NBTTagList) {
            final NBTType listType = data.getListType(currentKey.getKeyValue());

            if (listType == NBTType.NBTTagCompound) return new NBTCompoundListData(data.getCompoundList(currentKey.getKeyValue()));
            else return new NBTPrimitiveListData((List<Object>) getDataListFromCompound(data, currentKey));
        }
        else return new NBTPrimitiveData(getSimpleDataFromCompound(data, currentKey));
    }


    public <T> void setDataToPath(@NotNull final ICompoundData<?> data, @NotNull final NBTPath path, @NotNull final T o, @NotNull final Class<T> castClazz) {
        if (path.isEmpty()) throw new UnsupportedOperationException("NBTPath cannot be empty!");
        if (path.getLastKey().isArr()) setIntoList(data, path, o, castClazz);
        else setIntoComp(data, path, o, castClazz);
    }

    private <T> void setIntoList(final ICompoundData<?> data, final @NotNull NBTPath path, final T o, Class<T> castClazz) {
        // Setting into a list has to be done like this, because I have to find the first instance of a "non-primitive" type
        // of INBTData to then set the data to; But since only the last NBT can be primitive; Or the second to last, if it's a list
        final int i = ((NBTArrayKey) path.getLastKey()).getIndex();
        INBTListData<?> listData = (INBTListData<?>) getDataFromPath(data, path.getSubSet(0, -1)).getData();

        if (listData.getType() == NBTDataType.COMPOUND_LIST) {
            ((NBTCompoundListData) listData).getData().set(i, (NBTListCompound) o);
            return;
        }

        final INBTData<?> inbtData = getDataFromPath(data, path.getSubSet(0, -2)).getData();
        final NBTKey prevKey = path.get(-2);
        final NBTCompound prevComp = ((NBTCompoundData) inbtData).getData();
        final NBTList<T> dataListFromCompound = (NBTList<T>) getDataListFromCompound(prevComp, prevKey);

        // Might be an array instead of a list!
        if (dataListFromCompound == null)
            switch (prevComp.getType(prevKey.getKeyValue())) {
                case NBTTagByteArray: { prevComp.getByteArray(prevKey.getKeyValue())[i] = (Byte) o; return; }
                case NBTTagIntArray: { prevComp.getIntArray(prevKey.getKeyValue())[i] = (Integer) o; return; }
                default: throw new UnsupportedOperationException(String.format(
                        "Could not retrieve a list from: %n%s %nWith NBTPath: %s", prevComp, prevKey));
            }


        dataListFromCompound.set(i, o);
    }

    private <T> void setIntoComp(final ICompoundData<?> data, final @NotNull NBTPath path, final T o, final Class<T> castClazz) {
        final NBTKey lastKey = path.getLastKey();
        final ICompoundData<?> result = (ICompoundData<?>) getDataFromPath(data, path.getSubSet(0, -1)).getData();

        final NBTCompound nbt;
        if (result.getType() == NBTDataType.COMPOUND) nbt = ((NBTCompoundData) result).getData();
        else nbt = ((NBTCompoundListData) result).getData().get(((NBTArrayKey) path.get(-2)).getIndex());

        setDataIntoComp(nbt, lastKey, o, castClazz);
    }

    private <T> void setDataIntoComp(final NBTCompound nbt, final NBTKey nbtKey, final T data, final Class<T> castClazz) {
        final String key = nbtKey.getKeyValue();
        if (castClazz.equals(Byte.class))           nbt.setByte(key, ((Byte) data));
        else if (castClazz.equals(Short.class))     nbt.setShort(key, ((Short) data));
        else if (castClazz.equals(Integer.class))       nbt.setInteger(key, ((Integer) data));
        else if (castClazz.equals(Long.class))      nbt.setLong(key, ((Long) data));
        else if (castClazz.equals(Float.class))     nbt.setFloat(key, ((Float) data));
        else if (castClazz.equals(Double.class))    nbt.setDouble(key, ((Double) data));
        else if (castClazz.equals(UUID.class))      nbt.setUUID(key, ((UUID) data));
        else if (castClazz.equals(String.class))    nbt.setString(key, (String) data);
        else                                        nbt.setObject(key, data);
    }

    private static void raiseExceptionIfInvalidListCall(INBTData<?> data, NBTKey key, NBTPath fullPath) {
        if (data instanceof INBTListData && !key.isArr())
            throw new UnsupportedOperationException(String.format("No index is provided for the NBT list: %s;%nCurrent key: %s; Current path: %s",
                    data.getData().toString(), key, fullPath));
        if ((!(data instanceof INBTListData)) && key.isArr())
            throw new UnsupportedOperationException(String.format("Cannot lookup index: %d; Because the NBT is not a list: %s%nCurrent key: %s; Current path: %s",
                    ((NBTArrayKey) data).getIndex(), data.getData().toString(), key, fullPath));
    }



    @Nullable
    public static NBTList<?> getDataListFromCompound(final @NotNull NBTCompound compound, final @NotNull NBTKey nbtKey) {
        final String key = nbtKey.getKeyValue();
        switch (compound.getListType(key)) {
            case NBTTagCompound: return compound.getCompoundList(key);
            case NBTTagInt: return compound.getIntegerList(key);
            case NBTTagLong: return compound.getLongList(key);
            case NBTTagFloat: return compound.getFloatList(key);
            case NBTTagDouble: return compound.getDoubleList(key);
            case NBTTagString: return  compound.getStringList(key);

            default: return null;
        }
    }

    @Nullable
    public static Object getSimpleDataFromCompound(final @NotNull NBTCompound compound, final @NotNull NBTKey nbtKey) {
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


    @Nullable
    public static NBTType inferType(@NotNull final Object data) {
        if (data instanceof Byte) return NBTType.NBTTagByte;
        else if (data instanceof Short) return NBTType.NBTTagShort;
        else if (data instanceof Integer) return NBTType.NBTTagInt;
        else if (data instanceof Long) return NBTType.NBTTagLong;
        else if (data instanceof Float) return NBTType.NBTTagFloat;
        else if (data instanceof Double) return NBTType.NBTTagDouble;
        else if (data instanceof String) return NBTType.NBTTagString;
        else return null;   // No Type could be inferred
    }



    public String getSeparator() {
        return separator;
    }
}
