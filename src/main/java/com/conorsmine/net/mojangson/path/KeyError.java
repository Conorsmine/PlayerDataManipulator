package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.StringUtils;

enum KeyError {

    NOTHING                 (null),
    STRING_EMPTY            (" The key may not be null or empty!"),
    CONTAINS_WHITESPACES    (" The key may not contain whitespaces!"),
    INVALID_ARRAY           (" The array key is malformed; Must look like this: \"[<positive integer>]\"");

    final String errorMsg;
    KeyError(String errorMsg) { this.errorMsg = errorMsg; }

    static KeyError isValidKey(final String key) {
        if (StringUtils.isNothingString(key)) return KeyError.STRING_EMPTY;
        if (key.matches(".*\\s.*")) return KeyError.CONTAINS_WHITESPACES;       // Key contains whitespaces
        if (key.matches("\\[\\d*\\D+.*]")) return KeyError.INVALID_ARRAY;       // Is valid array key
        return KeyError.NOTHING;
    }
}
