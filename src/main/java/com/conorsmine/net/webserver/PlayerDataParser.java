package com.conorsmine.net.webserver;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.Properties;
import com.conorsmine.net.files.ConfigFile;
import com.conorsmine.net.files.WebsiteFile;
import com.conorsmine.net.mojangson.StringUtils;
import com.conorsmine.net.mojangson.path.NBTArrayKey;
import com.conorsmine.net.mojangson.path.NBTKey;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.data.NBTData;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.tr7zw.nbtapi.NBTType.*;
import static com.conorsmine.net.Properties.*;

@SuppressWarnings("unchecked")
public class PlayerDataParser {

    private final PlayerDataManipulator pl;

    public PlayerDataParser(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    public CompletableFuture<UUID> parsePlayerData(final OfflinePlayer player) {
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

    private JSONObject getFinalParsedJson(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(PARSED_META_DATA, createMetadata(player));

        jsonObject.put(PARSED_PLAYER_DATA, parse(NBTData.getOfflinePlayerData(player.getUniqueId()).getCompound(), new JSONObject(), NBTPathBuilder.getEmptyNBTPath(pl.MOJANGSON)));

        return jsonObject;
    }

    private JSONObject createMetadata(final OfflinePlayer player) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(PARSED_PLUGIN_VERSION, PlayerDataManipulator.getINSTANCE().getDescription().getVersion());

        jsonObject.put(PARSED_PLAYER_NAME, player.getName());
        jsonObject.put(PARSED_PLAYER_UUID, player.getUniqueId().toString());

        return jsonObject;
    }

    private JSONObject parse(final NBTCompound nbt, final JSONObject json, final NBTPath path) {
        for (String key : nbt.getKeys()) {
            final NBTKey nbtKey = NBTKey.parseStringToKey(pl.MOJANGSON, key);
            final NBTPath newPath = new NBTPathBuilder(pl.MOJANGSON).addNBTPath(path).addNBTKey(nbtKey).create();

            final NBTType type = nbt.getType(key);
            final NBTType listType = nbt.getListType(key);

            if (type == NBTTagCompound)
                json.put(key, evaluateCompoundTag(nbt.getCompound(key), nbtKey, newPath));

            else if (listType == NBTTagCompound)
                json.put(key, evaluateCompoundList(nbt.getCompoundList(key), nbtKey, newPath));

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

        jsonObject.put(PARSED_TYPE, DataType.getType(compound.getType(key.getKeyValue())).name());
        jsonObject.put(PARSED_PATH, path);
        jsonObject.put(PARSED_VALUE, (simpleDataFromCompound == null) ? null : simpleDataFromCompound.toString());

        return jsonObject;
    }

    private JSONObject evaluateCompoundTag(final NBTCompound compound, NBTKey key, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(PARSED_TYPE, DataType.MAP.name());
        jsonObject.put(PARSED_PATH, path);
        jsonObject.put(PARSED_VALUE, parse(compound, new JSONObject(), path));

        return jsonObject;
    }

    private JSONObject evaluateSimpleList(final NBTCompound compound, NBTKey key, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        final NBTType listType = compound.getListType(key.getKeyValue());
        final DataType type = DataType.getType(listType);
        final NBTList<?> list = NBTReflectionUtil.getList(compound, key.getKeyValue(), listType, getClassFromType(listType));

        for (int i = 0; i < list.size(); i++) {
            final JSONObject valJson = new JSONObject();
            valJson.put(PARSED_TYPE, type.name());
            valJson.put(PARSED_PATH, String.format("%s[%d]", path, i));
            valJson.put(PARSED_VALUE, list.get(i).toString());

            jsonArray.add(valJson);
        }

        jsonObject.put(PARSED_TYPE, DataType.ARRAY.name());
        jsonObject.put(PARSED_PATH, path);
        jsonObject.put(PARSED_VALUE, jsonArray);
        return jsonObject;
    }

    private JSONObject evaluateCompoundList(final NBTCompoundList compoundList, NBTKey key, NBTPath path) {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < compoundList.size(); i++) {
            final NBTListCompound compound = compoundList.get(i);
            final NBTPath newNBTPath = new NBTPathBuilder(pl.MOJANGSON).addNBTPath(path).addNBTKey(NBTArrayKey.parseToArrayKey(pl.MOJANGSON, i)).create();

            final JSONObject json = new JSONObject();
            json.put(PARSED_TYPE, DataType.getType(compoundList.getType()));
            json.put(PARSED_PATH, newNBTPath);
            json.put(PARSED_VALUE, parse(compound, new JSONObject(), newNBTPath));

            jsonArray.add(json);
        }

        jsonObject.put(PARSED_TYPE, DataType.ARRAY.name());
        jsonObject.put(PARSED_PATH, path);
        jsonObject.put(PARSED_VALUE, jsonArray);
        return jsonObject;
    }

    private JSONObject evaluateCompoundArray(final NBTCompound compoundArray, NBTKey key, NBTPath path) {
        final boolean isInt = (compoundArray.getType(key.getKeyValue()) == NBTTagIntArray);

        final DataType valType = (isInt) ? DataType.INT : DataType.BYTE;

        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();
        jsonObject.put(PARSED_TYPE, DataType.ARRAY.name());
        jsonObject.put(PARSED_PATH, path);

        final Object[] arr = convertToArr(compoundArray, key);

        for (int i = 0; i < arr.length; i++) {
            final JSONObject valJson = new JSONObject();

            valJson.put(PARSED_TYPE, valType.name());
            valJson.put(PARSED_PATH, String.format("%s[%d]", path, i));
            valJson.put(PARSED_VALUE, arr[i]);

            jsonArray.add(valJson);
        }

        jsonObject.put(PARSED_VALUE, jsonArray);
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

    private Class<?> getClassFromType(final NBTType type) {
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




    private enum DataType {
        ARRAY (DATATYPE_ARRAY),
        FLOAT (DATATYPE_FLOAT),
        DOUBLE (DATATYPE_DOUBLE),
        BYTE (DATATYPE_BYTE),
        INT (DATATYPE_INT),
        LONG (DATATYPE_LONG),
        SHORT (DATATYPE_SHORT),
        MAP (DATATYPE_MAP),
        STR (DATATYPE_STR);

        private final Properties property;
        DataType(Properties property) {
            this.property = property;
        }

        @Override
        public String toString() {
            return this.property.toString();
        }

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
