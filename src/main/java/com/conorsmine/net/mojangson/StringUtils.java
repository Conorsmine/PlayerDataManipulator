package com.conorsmine.net.mojangson;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class StringUtils {

    private static final List<Character> SIMPLE_REGEX = Collections.unmodifiableList(Arrays.asList('.', '*', '+', '|', '^', '?'));

    public static boolean isNothingString(final String s) {
        if (s == null || s.isEmpty()) return true;
        return s.matches("\\s*");
    }

    public static String toNonRegex(String s) {
        if (isNothingString(s)) return s;

        for (Character c : SIMPLE_REGEX) {
            final String rep = String.format("\\%s", c);
            s = s.replaceAll(rep, String.format("\\\\\\\\%s", c));
        }
        return s;
    }

    public static boolean isArrayKey(final String key) {
        return key.matches("\\[\\d+]");
    }

    public static String repeatString(@NotNull final String repeat, int amount) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) builder.append(repeat);
        return builder.toString();
    }

    public static String[] fancyErrorLines(@NotNull final String errorString, @NotNull final String[] splitErrorString,
                                           @NotNull final List<ErrorPlace> errors) {
        final String[] formattedErrorString = new String[errors.size() + 1];

        formattedErrorString[0] = errorString;
        final List<ErrorPlace> sortedErrors = errors.stream().sorted((o1, o2) -> o1.compare(o1, o2)).collect(Collectors.toList());

        for (int i = 0; i < sortedErrors.size(); i++) {
            final ErrorPlace errorPlace = sortedErrors.get(i);
            int lenToErr = getLengthToError(errorPlace.index, splitErrorString);
            int lenUnderline = splitErrorString[errorPlace.index].length();

            formattedErrorString[i + 1] = repeatString(" ", lenToErr) + repeatString("~", lenUnderline) + errorPlace.msg;
        }

        return formattedErrorString;
    }

    private static int getLengthToError(int index, final String[] splitErrorString) {
        int len = 0;
        for (int i = 0; i < index; i++) {
            len += splitErrorString[i].length();
        }

        return len;
    }



    public static class ErrorPlace implements Comparator<ErrorPlace> {

        private final int index;
        private final String msg;

        public ErrorPlace(int index, @NotNull final String msg) {
            this.index = index;
            this.msg = msg;
        }


        @Override
        public int compare(ErrorPlace o1, ErrorPlace o2) {
            return (o1.index <= o2.index) ? -1 : 1;
        }
    }
}
