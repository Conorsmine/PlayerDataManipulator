package com.conorsmine.net.webserver;

import com.conorsmine.net.ParserDataTypes;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.Properties;
import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.path.NBTArrayKey;
import com.conorsmine.net.mojangson.path.NBTKey;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.data.NBTData;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static de.tr7zw.nbtapi.NBTType.*;

@SuppressWarnings("unchecked")
public class PlayerDataParser {

    private final PlayerDataManipulator pl;

    public PlayerDataParser(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    public CompletableFuture<UUID> parsePlayerData(final OfflinePlayer player) {
        final CompletableFuture<UUID> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> {
            try {
                final UUID fileUUID = UUID.randomUUID();
                final File file = pl.WEBSITE_CONF.createTempParsedFile(fileUUID.toString());
                pl.WEBSITE_CONF.createTempChangeFile(fileUUID.toString());

                final String json = new GsonBuilder().setPrettyPrinting().create().toJson(getFinalParsedJson(player));

                final FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(json.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();

                future.complete(fileUUID);
            } catch (IOException e) { future.completeExceptionally(e); }
        });

        return future;
    }

    private JSONObject getFinalParsedJson(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(Properties.PARSED_META_DATA, createMetadata(player));

        jsonObject.put(Properties.PARSED_PLAYER_DATA, parse(NBTData.getOfflinePlayerData(player.getUniqueId()).getCompound(), new JSONObject(), new NBTPathBuilder(pl.MOJANGSON).create()));

        return jsonObject;
    }

    private JSONObject createMetadata(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(Properties.PARSED_PLUGIN_VERSION, pl.getDescription().getVersion());

        jsonObject.put(Properties.PARSED_PLAYER_NAME, player.getName());
        jsonObject.put(Properties.PARSED_PLAYER_UUID, player.getUniqueId().toString());
        jsonObject.put(Properties.PARSED_SEPARATOR, pl.CONF.getSeparator());

        return jsonObject;
    }

    private JSONObject parse(final NBTCompound nbt, final JSONObject json, final NBTPath path) {
        for (String key : nbt.getKeys()) {
            final NBTKey nbtKey = NBTKey.parseStringToKey(pl.MOJANGSON, key);
            final NBTPath newPath = new NBTPathBuilder(pl.MOJANGSON).addNBTPath(path).addNBTKey(nbtKey).create();

            final NBTType type = nbt.getType(key);
            final NBTType listType = nbt.getListType(key);

            if (type == NBTTagCompound)
                json.put(key, evaluateCompoundTag(nbt.getCompound(key), newPath));

            else if (listType == NBTTagCompound)
                json.put(key, evaluateCompoundList(nbt.getCompoundList(key), newPath));

            else if (listType != null)
                json.put(key, evaluateSimpleList(nbt, nbtKey, newPath));

            else if (type == NBTTagIntArray || type == NBTTagByteArray)
                json.put(key, evaluateCompoundArray(nbt, nbtKey, newPath));

            else
                json.put(key, evaluateSimpleCompound(nbt, nbtKey, newPath));
        }

        return json;
    }

    private JSONObject evaluateSimpleCompound(final NBTCompound compound, NBTKey key, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        final Object simpleDataFromCompound = MojangsonUtils.getSimpleDataFromCompound(compound, key);

        jsonObject.put(Properties.PARSED_TYPE, ParserDataTypes.getType(compound.getType(key.getKeyValue())).toString());
        jsonObject.put(Properties.PARSED_PATH, path.toString());
        jsonObject.put(Properties.PARSED_VALUE, (simpleDataFromCompound == null) ? null : simpleDataFromCompound.toString());

        return jsonObject;
    }

    private JSONObject evaluateCompoundTag(final NBTCompound compound, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(Properties.PARSED_TYPE, ParserDataTypes.MAP.toString());
        jsonObject.put(Properties.PARSED_PATH, path.toString());
        jsonObject.put(Properties.PARSED_VALUE, parse(compound, new JSONObject(), path));

        return jsonObject;
    }

    private JSONObject evaluateSimpleList(final NBTCompound compound, NBTKey key, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        final NBTType listType = compound.getListType(key.getKeyValue());
        final ParserDataTypes type = ParserDataTypes.getType(listType);

        final NBTList<?> list;
        if (listType == NBTTagEnd) list = NBTEmptyList.EMPTY_LIST;
        else list = NBTReflectionUtil.getList(compound, key.getKeyValue(), listType, ParserDataTypes.getType(listType).getClassFromDataType());

        for (int i = 0; i < list.size(); i++) {
            final JSONObject valJson = new JSONObject();
            valJson.put(Properties.PARSED_TYPE, type.toString());
            valJson.put(Properties.PARSED_PATH, String.format("%s[%d]", path, i));
            valJson.put(Properties.PARSED_VALUE, list.get(i).toString());

            jsonArray.add(valJson);
        }

        jsonObject.put(Properties.PARSED_TYPE, ParserDataTypes.ARRAY.toString());
        jsonObject.put(Properties.PARSED_PATH, path.toString());
        jsonObject.put(Properties.PARSED_VALUE, jsonArray);
        return jsonObject;
    }

    private JSONObject evaluateCompoundList(final NBTCompoundList compoundList, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < compoundList.size(); i++) {
            final NBTListCompound compound = compoundList.get(i);
            final NBTPath newNBTPath = new NBTPathBuilder(pl.MOJANGSON).addNBTPath(path).addNBTKey(NBTArrayKey.parseToArrayKey(i)).create();

            final JSONObject json = new JSONObject();
            json.put(Properties.PARSED_TYPE, ParserDataTypes.getType(compoundList.getType()).toString());
            json.put(Properties.PARSED_PATH, newNBTPath.toString());
            json.put(Properties.PARSED_VALUE, parse(compound, new JSONObject(), newNBTPath));

            jsonArray.add(json);
        }

        jsonObject.put(Properties.PARSED_TYPE, ParserDataTypes.ARRAY.toString());
        jsonObject.put(Properties.PARSED_PATH, path.toString());
        jsonObject.put(Properties.PARSED_VALUE, jsonArray);
        return jsonObject;
    }

    private JSONObject evaluateCompoundArray(final NBTCompound compoundArray, NBTKey key, NBTPath path) {
        final boolean isInt = (compoundArray.getType(key.getKeyValue()) == NBTTagIntArray);

        final ParserDataTypes valType = (isInt) ? ParserDataTypes.INT : ParserDataTypes.BYTE;

        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();
        jsonObject.put(Properties.PARSED_TYPE, ParserDataTypes.ARRAY.toString());
        jsonObject.put(Properties.PARSED_PATH, path.toString());

        final Object[] arr = convertToArr(compoundArray, key);

        for (int i = 0; i < arr.length; i++) {
            final JSONObject valJson = new JSONObject();

            valJson.put(Properties.PARSED_TYPE, valType.toString());
            valJson.put(Properties.PARSED_PATH, new NBTPathBuilder(pl.MOJANGSON).addNBTPath(path).addNBTKey(NBTArrayKey.parseToArrayKey(i)).toString());
            valJson.put(Properties.PARSED_VALUE, arr[i]);

            jsonArray.add(valJson);
        }

        jsonObject.put(Properties.PARSED_VALUE, jsonArray);
        return jsonObject;
    }

    private Object[] convertToArr(final NBTCompound nbt, NBTKey key) {
        final boolean isInt = (nbt.getType(key.getKeyValue()) == NBTTagIntArray);

        if (isInt) {
            final int[] intArray = nbt.getIntArray(key.getKeyValue());
            final Object[] arr = new Object[intArray.length];
            for (int i = 0; i < intArray.length; i++) { arr[i] = intArray[i]; }
            return arr;
        }
        else {
            final byte[] bytes = nbt.getByteArray(key.getKeyValue());
            final Object[] arr = new Object[bytes.length];
            for (int i = 0; i < bytes.length; i++) { arr[i] = bytes[i]; }
            return arr;
        }
    }



    private static final class NBTEmptyList extends NBTList<Object> {

        public static final NBTEmptyList EMPTY_LIST = new NBTEmptyList();

        private NBTEmptyList() {
            super(null, null, null, null);
        }

        @Override
        protected Object asTag(Object o) {
            return o;
        }

        @Override
        @Nullable
        public Object get(int index) {
            return null;
        }

        @Override
        public boolean add(Object element) {
            return false;
        }

        @Override
        public void add(int index, Object element) {
        }

        @Override
        @Nullable
        public Object set(int index, Object element) {
            return null;
        }

        @Override
        @Nullable
        public Object remove(int i) {
            return null;
        }

        @Override
        public boolean addAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <E> E[] toArray(E[] a) {
            return (E[]) new Object[0];
        }

        @Override
        public int size() {
            return 0;
        }
    }
}
