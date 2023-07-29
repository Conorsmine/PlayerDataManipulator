package com.conorsmine.net.webserver;

import java.util.UUID;

public class UUIDParser {

    static final byte[] DIGITS = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1,
            -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, 10, 11, 12,
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
            33, 34, 35, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    public static String cmdCodeFromUUID(final String uuid) {
        return bytesToCmdCode(calcAllBytes(uuid));
    }

    private static String bytesToCmdCode(final long uuidBytes) {
        final StringBuilder result = new StringBuilder();

        final char[] str = Long.toHexString(uuidBytes).toCharArray();
        for (int i = 0; i < str.length - 1; i += 2) {
            final long  l1 = parseLong(Character.toString(str[i]), 0, 1, 16);
            final long  l2 = parseLong(Character.toString(str[i + 1]), 0, 1, 16);

            result.append(Character.toChars((int) (l1 + l2 + 48)));
        }

        return result.toString();
    }

    private static long calcAllBytes(final String uuid) {
        long val = 0x800000000000L;
        final String[] strArr = uuid.split("-");
        for (final String s : strArr) {
            val += parseLong(s, 0, s.length(), 16);
        }
        return val;
    }

    private static long parseLong(final String str, int begin, int end, int radix) {
        boolean negative = false;
        int i = begin;
        long limit = -0x7fffffffffffffffL;

        if (i < end) {
            char firstChar = str.toCharArray()[i];
            if (firstChar < '0') {
                if (firstChar == '-') {
                    negative = true;
                    limit = 0x8000000000000000L;
                }
                i++;
            }

            long result = 0;
            while (i < end) {
                long digit = calcDigit(str.codePointAt(i), radix);
                result *= radix;
                i++;
                result -= digit;
            }
            return (negative) ? result : -result;
        }

        return 0;
    }

    private static long calcDigit(int codePoint, int radix) {
        long value = DIGITS[codePoint];
        return (value >= 0 && value < radix && radix >= 2
                && radix <= 36) ? value : -1;
    }


}
