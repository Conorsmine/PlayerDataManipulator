package com.conorsmine.net;

import com.conorsmine.net.utils.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Properties {

    UUID_REGEX ("uuid_regex.regexp"),

    URL_REQUEST_PREFIX ("url_request_prefix"),
    URL_CHANGES_PREFIX ("url_changes_prefix"),
    URL_PATH ("url_path"),
    URL_ID ("url_id"),

    PARSED_META_DATA ("parsed_meta_data"),
    PARSED_PLAYER_DATA ("parsed_player_data"),
    PARSED_PLAYER_NAME ("parsed_meta_name"),
    PARSED_PLAYER_UUID ("parsed_meta_uuid"),
    PARSED_PLUGIN_VERSION ("parsed_meta_version"),
    PARSED_TYPE ("parsed_player_type"),
    PARSED_PATH ("parsed_player_path"),
    PARSED_VALUE ("parsed_player_value"),

    DATATYPE_ARRAY ("datatype_ARRAY"),
    DATATYPE_MAP  ("datatype_MAP"),
    DATATYPE_BYTE ("datatype_BYTE"),
    DATATYPE_SHORT ("datatype_SHORT"),
    DATATYPE_INT ("datatype_INT"),
    DATATYPE_LONG ("datatype_LONG"),
    DATATYPE_FLOAT ("datatype_FLOAT"),
    DATATYPE_DOUBLE ("datatype_DOUBLE"),
    DATATYPE_STR ("datatype_STR"),
    ;

    private static final Lazy<java.util.Properties> PROP = new Lazy<>(com.conorsmine.net.Properties::loadProperties);
    private static java.util.Properties loadProperties() {
        final java.util.Properties properties = new java.util.Properties();
        try { properties.load(Properties.class.getResourceAsStream("/replacements.properties")); }
        catch (Exception e) {
            PlayerDataManipulator.staticSendMsg("§cCouldn't load the properties file! Some features might be affected by this!");
            e.printStackTrace();
        }

        return properties;
    }


    private String propPath;
    Properties(String propPath) {
        this.propPath = propPath;
    }

    @Override
    public String toString() {
        return getProperty(this.propPath);
    }

    @Nullable
    private static String getProperty(@NotNull final String name) {
        return PROP.get().getProperty(name);
    }
}
