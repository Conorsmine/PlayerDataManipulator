package com.conorsmine.net;

import de.tr7zw.nbtapi.NBTType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static de.tr7zw.nbtapi.NBTType.*;

public enum ParserDataTypes {

    ARRAY(Properties.DATATYPE_ARRAY, null, (s) -> null),
    MAP(Properties.DATATYPE_MAP, null, (s) -> null),

    BYTE(Properties.DATATYPE_BYTE, Byte.class, Byte::parseByte),
    SHORT(Properties.DATATYPE_SHORT, Short.class, Short::parseShort),
    INT(Properties.DATATYPE_INT, Integer.class, Integer::parseInt),
    LONG(Properties.DATATYPE_LONG, Long.class, Long::parseLong),
    FLOAT(Properties.DATATYPE_FLOAT, Float.class, Float::parseFloat),
    DOUBLE(Properties.DATATYPE_DOUBLE, Double.class, Double::parseDouble),
    STRING(Properties.DATATYPE_STR, String.class, (s) -> s)
    ;

    private final Properties property;
    private final Class<?> castClazz;

    private final Function<String, Object> convertFunc;

    ParserDataTypes(Properties property, Class<?> castClazz, Function<String, Object> convertFunc) {
        this.property = property;
        this.castClazz = castClazz;
        this.convertFunc = convertFunc;
    }

    @Nullable
    public static ParserDataTypes getDataTypeFromName(final @NotNull String dataName) {
        final Properties property = Properties.getPropertyFromPropertyString(dataName);
        for (ParserDataTypes value : values()) {
            if (value.property == property) return value;
        }

        return null;
    }

    @Nullable
    public Object getValue(final @NotNull Object o) {
        return convertFunc.apply(o.toString());
    }

    public Properties getProperty() {
        return this.property;
    }

    @Nullable
    public Class getClassFromDataType() {
        return this.castClazz;
    }

    private final static Map<NBTType, ParserDataTypes> NBT_CONV_MAP = new HashMap<NBTType, ParserDataTypes>() {{
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
        put(NBTTagString, STRING);
    }};

    public static ParserDataTypes getType(final NBTType type) {
        return NBT_CONV_MAP.get(type);
    }

    @Override
    public String toString() {
        return this.property.toString();
    }
}
