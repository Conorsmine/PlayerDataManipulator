package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NBTKey {

    private final String keyValue;
    private final boolean arr;

    protected NBTKey(String key) {
        this.keyValue = key;

        this.arr = StringUtils.isArrayKey(key);
    }

    public String getKeyValue() {
        return this.keyValue;
    }

    public boolean isArr() {
        return this.arr;
    }

    @Override
    public String toString() {
        return "NBTKey{" +
                "keyValue='" + keyValue + '\'' +
                ", arr=" + arr +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.keyValue.equals(((NBTKey) o).keyValue);
    }

    public static NBTKey parseStringToKey(@NotNull final MojangsonUtils utils, final String key) {
        final KeyError keyError = KeyError.isValidKey(key);
        if (keyError != KeyError.NOTHING) {
            final List<StringUtils.ErrorPlace> errors = Collections.singletonList(new StringUtils.ErrorPlace(0, keyError.errorMsg));
            throw new NBTKeyParseError(StringUtils.fancyErrorLines(key, new String[] { utils.getSeparator() }, errors));
        }

        return new NBTKey(key);
    }





    public static class NBTKeyParseError extends Error {

        public NBTKeyParseError(final String... errorMsg) {
            super(String.format("%n%s", String.join("\n", errorMsg)));
        }
    }
}
