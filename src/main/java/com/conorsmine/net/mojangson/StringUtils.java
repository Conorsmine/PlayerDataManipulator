package com.conorsmine.net.mojangson;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class StringUtils {

    public static boolean isNothingString(final String s) {
        if (s == null) return true;
        if (s.length() == 0) return true;
        return s.matches("\\s*");
    }

    public static boolean isArrayKey(final String key) {
        return key.matches("\\[\\d+]");
    }

    public static String repeatString(@NotNull final String repeat, int amount) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) builder.append(repeat);
        return builder.toString();
    }

    public static String[] fancyErrorLines(@NotNull final String errorString, @NotNull final String separator, @NotNull final List<ErrorPlace> errors) {
        final String[] splitErrorString = errorString.split(separator);
        final String[] formattedErrorString = new String[errors.size() + 1];

        formattedErrorString[0] = errorString;
        final List<ErrorPlace> sortedErrors = errors.stream().sorted((o1, o2) -> (o1.index >= o2.index) ? 0 : -1).collect(Collectors.toList());

        for (int i = 0; i < sortedErrors.size(); i++) {
            formattedErrorString[i + 1] =
                    repeatString(" ", getLengthToError(sortedErrors.get(i).index, splitErrorString, separator)) +
                            repeatString("~", splitErrorString[sortedErrors.get(i).index].length()) +
                            sortedErrors.get(i).msg;
        }

        return formattedErrorString;
    }

    private static int getLengthToError(int index, final String[] splitErrorString, final String separator) {
        int len = (index * separator.length());
        for (int i = 0; i < index; i++) {
            len += splitErrorString[i].length();
        }

        return len;
    }



    public static class ErrorPlace {

        private final int index;
        private final String msg;

        public ErrorPlace(int index, @NotNull final String msg) {
            this.index = index;
            this.msg = msg;
        }
    }
}
