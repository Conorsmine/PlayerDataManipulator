package com.conorsmine.net.messages;

import java.util.function.Function;

public enum PluginMsgs {

    MISSING_PATH_WRAPPER    ("§cInvalid §7<inventoryPath>§c!"),
    MISSING_ITEM_ID         ("§cInvalid §7<itemId>§c!"),
    ITEM_NOT_FOUND          ("§b%d §7is either an empty slot or not a valid inventory slot!"),
    INV_FORMAT              ("§6%s§7's §b%s§7's inventory"),
    INVALID_ITEM_MATERIAL   ("§7The provided item §ccould not §7be created.")
    ;

    private final String msg;

    PluginMsgs(String msg) { this.msg = msg; }

    public String getMsg() {
        return msg;
    }
}
