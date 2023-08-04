package com.conorsmine.net;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Properties {

    private static final java.util.Properties PROP = new java.util.Properties();
    static {
        try { PROP.load(Properties.class.getResourceAsStream("replacements.properties")); }
        catch (IOException e) {
            e.printStackTrace();
            PlayerDataManipulator.sendMsg("§cCouldn't load the properties file! Some features might be affected by this!");
        }
    }

    @Nullable
    public static String getProperty(@NotNull final String name) {
        return PROP.getProperty(name);
    }
}
