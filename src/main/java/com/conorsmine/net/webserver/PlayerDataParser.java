package com.conorsmine.net.webserver;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.files.ParserFile;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.MojangsonUtils;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.data.NBTData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static de.tr7zw.nbtapi.NBTType.*;

@SuppressWarnings("unchecked")
public class PlayerDataParser {

    public static CompletableFuture<UUID> parsePlayerData(final OfflinePlayer player) {
        final CompletableFuture<UUID> future = new CompletableFuture<>();

        final PlayerDataManipulator plugin = PlayerDataManipulator.getINSTANCE();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final UUID fileUUID = UUID.randomUUID();
                final File file = WebsiteFile.createTempParsedFile(fileUUID.toString());
                WebsiteFile.createTempChangeFile(fileUUID.toString());

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

    private static JSONObject getFinalParsedJson(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(ParserTag.META_DATA.tag, createMetadata(player));

        jsonObject.put(ParserTag.PLAYER_DATA.tag, parse(NBTData.getOfflinePlayerData(player.getUniqueId()).getCompound(), new JSONObject(), ""));

        return jsonObject;
    }

    private static JSONObject createMetadata(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(ParserTag.PLUGIN_VERSION.tag, PlayerDataManipulator.getINSTANCE().getDescription().getVersion());

        jsonObject.put(ParserTag.PLAYER_NAME.tag, player.getName());
        jsonObject.put(ParserTag.PLAYER_UUID.tag, player.getUniqueId().toString());

        return jsonObject;
    }

    private static JSONObject parse(final NBTCompound nbt, final JSONObject json, final String path) {
        for (String key : nbt.getKeys()) {
            final NBTType type = nbt.getType(key);
            final String newPath = (StringUtils.isBlank(path)) ? key : String.format("%s%s%s", path, ParserFile.getSeparator(), key);
            final NBTType listType = nbt.getListType(key);

            if (type == NBTTagCompound)
                json.put(key, evaluateCompoundTag(nbt.getCompound(key), key, newPath));

            else if (listType == NBTTagCompound)
                json.put(key, evaluateCompoundList(nbt.getCompoundList(key), key, newPath));

            else if (listType != null)
                json.put(key, evaluateSimpleList(nbt, key, newPath));

            else if (type == NBTTagIntArray || type == NBTTagByteArray)
                json.put(key, evaluateCompoundArray(nbt, key, newPath));

            else
                json.put(key, evaluateSimpleCompound(nbt, key, newPath));
        }

        return json;
    }

    private static JSONObject evaluateSimpleCompound(final NBTCompound compound, String key, String path) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(ParserTag.TYPE.tag, DataType.getType(compound.getType(key)).name());
        jsonObject.put(ParserTag.PATH.tag, path);
        jsonObject.put(ParserTag.VALUE.tag, MojangsonUtils.getSimpleDataFromCompound(compound, key));

        return jsonObject;
    }

    private static JSONObject evaluateCompoundTag(final NBTCompound compound, String key, String path) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(ParserTag.TYPE.tag, DataType.MAP.name());
        jsonObject.put(ParserTag.PATH.tag, path);
        jsonObject.put(ParserTag.VALUE.tag, parse(compound, new JSONObject(), path));

        return jsonObject;
    }

    private static JSONObject evaluateSimpleList(final NBTCompound compound, String key, String path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        final NBTType listType = compound.getListType(key);
        final DataType type = DataType.getType(listType);
        final NBTList<?> list = NBTReflectionUtil.getList(compound, key, listType, getClassFromType(listType));

        for (int i = 0; i < list.size(); i++) {
            final JSONObject valJson = new JSONObject();
            valJson.put(ParserTag.TYPE.tag, type.name());
            valJson.put(ParserTag.PATH.tag, String.format("%s[%d]", path, i));
            valJson.put(ParserTag.VALUE.tag, list.get(i));

            jsonArray.add(valJson);
        }

        jsonObject.put(ParserTag.PATH.tag, path);
        jsonObject.put(ParserTag.TYPE.tag, DataType.ARRAY.name());
        jsonObject.put(ParserTag.VALUE.tag, jsonArray);
        return jsonObject;
    }

    private static JSONObject evaluateCompoundList(final NBTCompoundList compoundList, String key, String path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < compoundList.size(); i++) {
            final NBTListCompound compound = compoundList.get(i);

            final JSONObject json = new JSONObject();
            json.put(ParserTag.TYPE.tag, DataType.getType(compoundList.getType()));
            json.put(ParserTag.PATH.tag, String.format("%s[%d]", path, i));
            json.put(ParserTag.VALUE.tag, parse(compound, new JSONObject(), String.format("%s[%d]", path, i)));

            jsonArray.add(json);
        }

        jsonObject.put(ParserTag.TYPE.tag, DataType.ARRAY.name());
        jsonObject.put(ParserTag.PATH.tag, path);
        jsonObject.put(ParserTag.VALUE.tag, jsonArray);
        return jsonObject;
    }

    private static JSONObject evaluateCompoundArray(final NBTCompound compoundArray, String key, String path) {
        final boolean isInt = (compoundArray.getType(key) == NBTTagIntArray);

        final DataType valType = (isInt) ? DataType.INT : DataType.BYTE;

        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();
        jsonObject.put(ParserTag.PATH.tag, path);
        jsonObject.put(ParserTag.TYPE.tag, DataType.ARRAY.name());

        final Object[] arr = convertToArr(compoundArray, key);

        for (int i = 0; i < arr.length; i++) {
            final JSONObject valJson = new JSONObject();

            valJson.put(ParserTag.TYPE.tag, valType.name());
            valJson.put(ParserTag.PATH.tag, String.format("%s[%d]", path, i));
            valJson.put(ParserTag.VALUE.tag, arr[i]);

            jsonArray.add(valJson);
        }

        jsonObject.put(ParserTag.VALUE.tag, jsonArray);
        return jsonObject;
    }

    private static Object[] convertToArr(final NBTCompound nbt, String key) {
        final boolean isInt = (nbt.getType(key) == NBTTagIntArray);

        if (isInt) {
            final int[] intArray = nbt.getIntArray(key);
            final Object[] arr = new Object[intArray.length];
            for (int i = 0; i < intArray.length; i++) { arr[i] = intArray[i]; }
            return arr;
        }
        else {
            final byte[] bytes = nbt.getByteArray(key);
            final Object[] arr = new Object[bytes.length];
            for (int i = 0; i < bytes.length; i++) { arr[i] = bytes[i]; }
            return arr;
        }
    }

    private static Class<?> getClassFromType(final NBTType type) {
        switch (type) {
            case NBTTagInt:
                return Integer.class;
            case NBTTagLong:
                return Long.class;
            case NBTTagShort:
                return Short.class;
            case NBTTagByte:
                return Byte.class;
            case NBTTagFloat:
                return Float.class;
            case NBTTagDouble:
                return Double.class;

            default:
                return String.class;
        }
    }





    private enum ParserTag {
        META_DATA ("meta_data"),
        PLAYER_DATA ("player_data"),

        PLAYER_NAME ("name"),
        PLAYER_UUID ("uuid"),
        PLUGIN_VERSION ("version"),

        TYPE ("type"),
        PATH ("absolute_path"),
        VALUE ("value");

        final String tag;

        ParserTag(String tag) {
            this.tag = tag;
        }
    }

    private enum DataType {
        ARRAY,
        FLOAT,
        DOUBLE,
        BYTE,
        INT,
        LONG,
        SHORT,
        MAP,
        STR;

        private final static Map<NBTType, DataType> map = new HashMap<NBTType, DataType>() {{
            put(NBTTagList, ARRAY);
            put(NBTTagIntArray, ARRAY);
            put(NBTTagByteArray, ARRAY);
            put(NBTTagFloat, FLOAT);
            put(NBTTagDouble, DOUBLE);
            put(NBTTagByte, BYTE);
            put(NBTTagInt, INT);
            put(NBTTagLong, LONG);
            put(NBTTagShort, SHORT);
            put(NBTTagCompound, MAP);
            put(NBTTagString, STR);
        }};

        public static DataType getType(final NBTType type) {
            return map.get(type);
        }
    }

}
