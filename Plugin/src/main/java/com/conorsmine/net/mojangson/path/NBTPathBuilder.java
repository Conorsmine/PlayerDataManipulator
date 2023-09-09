package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NBTPathBuilder {

    private final String sep;
    private final NBTPath nbtPath;

    protected NBTPathBuilder(String sep) {
        this.sep = sep;
        this.nbtPath  = new NBTPath(sep);
    }

    public NBTPathBuilder(@NotNull final MojangsonUtils utils) {
        this(utils.getSeparator());
    }

    public NBTPathBuilder parseString(final String nbtPathString) {
        if (StringUtils.isNothingString(nbtPathString)) return this;

        // Inventory[0]##item##id -> [ Inventory, [0], item, id ]
        final List<String> splitKeys = Arrays.stream(nbtPathString.split(this.sep))
                .flatMap((s) -> Arrays.stream(s.split("(?=\\[.*]$)")))
                .collect(Collectors.toList());

        final List<StringUtils.ErrorPlace> addErrors = new LinkedList<>(checkKeys(splitKeys));
        for (final String key : splitKeys) {
            this.nbtPath.addKey((StringUtils.isArrayKey(key) ? new NBTArrayKey(key) : new NBTKey(key)));
        }

        if (!addErrors.isEmpty()) {
            addErrors.sort((e1, e2) -> e1.compare(e1, e2));
            final String toString = String.join(this.sep, splitKeys);
            throw new NBTPathParseError(StringUtils.fancyErrorLines(toString, toString.split(String.format("(?=%s)|(?<=%s)", this.sep, this.sep)), addErrors));
        }

        return this;
    }

    public NBTPathBuilder addNBTKey(@NotNull final NBTKey nbtKey) {
        this.nbtPath.addKey(nbtKey);
        return this;
    }

    public NBTPathBuilder addNBTPath(@NotNull final NBTPath nbtPath) {
        nbtPath.forEach(this.nbtPath::addKey);
        return this;
    }

    public NBTPath create() {
        return this.nbtPath;
    }

    private List<StringUtils.ErrorPlace> checkKeys(final List<String> splitKeys) {

        final List<StringUtils.ErrorPlace> errorPlaces = new LinkedList<>();
        for (int i = 0; i < splitKeys.size(); i++) {
            final KeyError keyError = KeyError.isValidKey(splitKeys.get(i));
            if (keyError == KeyError.NOTHING) continue;

            errorPlaces.add(new StringUtils.ErrorPlace((i * 2), keyError.errorMsg));
        }

        return errorPlaces;
    }

    public static class NBTPathParseError extends Error {

        public NBTPathParseError(final String... errorMsg) {
            super(String.format("%n%s", String.join("\n", errorMsg)));
        }
    }
}
