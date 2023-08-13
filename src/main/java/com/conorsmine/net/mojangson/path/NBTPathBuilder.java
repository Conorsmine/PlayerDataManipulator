package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.StringUtils;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NBTPathBuilder {

    private final MojangsonUtils utils;
    private final NBTPath nbtPath;

    public NBTPathBuilder(@NotNull final MojangsonUtils utils) {
        this.utils = utils;
        this.nbtPath  = new NBTPath(utils);
    }

    public NBTPathBuilder parseString(final String nbtPathString) {
        if (StringUtils.isNothingString(nbtPathString)) return this;

        final List<String> splitKeys = Arrays.stream(nbtPathString.split(this.utils.getSeparator()))
                .flatMap((s) -> Arrays.stream(s.split("(?<=\\w)(?=\\[\\d+]$)")))
                .collect(Collectors.toList());

        if (checkKeys(splitKeys)) splitKeys.forEach((key) -> this.nbtPath.addKey((StringUtils.isArrayKey(key) ? new NBTArrayKey(utils, key) : new NBTKey(utils, key))));
        return this;
    }

    public NBTPathBuilder addNBTKey(@NotNull final NBTKey nbtKey) {
        if (checkKeys(Lists.newArrayList(nbtKey.getKeyValue()))) this.nbtPath.addKey(nbtKey);
        return this;
    }

    public NBTPathBuilder addNBTPath(@NotNull final NBTPath nbtPath) {
        NBTKey nbtKey = nbtPath.next();
        while (nbtKey != null) {
            this.nbtPath.addKey(nbtKey);
            nbtKey = nbtPath.next();
        }

        return this;
    }

    public NBTPath create() {
        return this.nbtPath;
    }

    private boolean checkKeys(final List<String> splitKeys) {
        boolean isValid = true;

        final List<StringUtils.ErrorPlace> errorPlaces = new LinkedList<>();
        for (int i = 0; i < splitKeys.size(); i++) {
            final KeyError keyError = KeyError.isValidKey(splitKeys.get(i));
            if (keyError == KeyError.NOTHING) continue;

            errorPlaces.add(new StringUtils.ErrorPlace(i, keyError.errorMsg));
            isValid = false;
        }

        if (!isValid) throw new NBTPathBuilder.NBTPathParseError(StringUtils.fancyErrorLines(String.join(this.utils.getSeparator(), splitKeys), this.utils.getSeparator(), errorPlaces));
        return isValid;
    }

    public static NBTPath getEmptyNBTPath(@NotNull final MojangsonUtils utils) {
        return new NBTPath(utils);
    }

    private static class NBTPathParseError extends Error {

        public NBTPathParseError(final String... errorMsg) {
            super(String.format("%n%s", String.join("\n", errorMsg)));
        }
    }
}
