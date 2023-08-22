package com.conorsmine.net;

import com.conorsmine.net.utils.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum Properties {

    UUID_REGEX ("uuid_regex.regexp"),

    CONFIG_SEPARATOR("conf_sep"),
    CONFIG_WORKERS ("conf_workers"),
    CONFIG_WEBSITE_SECTION ("conf_web_editor"),
    CONFIG_WEBSITE_PORT ("conf_port"),
    CONFIG_INV_PATH_SECTION ("conf_all_inv_paths"),
    CONFIG_INV_PATH ("conf_inv_paths_path"),
    CONFIG_INV_SIZE ("conf_inv_paths_size"),

    URL_REQUEST_PREFIX ("url_request_prefix"),
    URL_CHANGES_PREFIX ("url_changes_prefix"),
    URL_PATH ("url_path"),
    URL_ID ("url_id"),

    PARSED_META_DATA ("parsed_meta_data"),
    PARSED_PLAYER_DATA ("parsed_player_data"),
    PARSED_PLAYER_NAME ("parsed_meta_name"),
    PARSED_PLAYER_UUID ("parsed_meta_uuid"),
    PARSED_PLUGIN_VERSION ("parsed_meta_version"),
    PARSED_SEPARATOR ("parsed_meta_separator"),
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
        catch (Exception e) { e.printStackTrace(); }

        return properties;
    }


    private final String propPath;
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


    private static final Map<Properties, Class<?>> classMap = new HashMap<Properties, Class<?>>() {{
        put(DATATYPE_BYTE, Byte.class);
        put(DATATYPE_SHORT, Short.class);
        put(DATATYPE_INT, Integer.class);
        put(DATATYPE_LONG, Long.class);
        put(DATATYPE_FLOAT, Float.class);
        put(DATATYPE_DOUBLE, Double.class);
        put(DATATYPE_STR, String.class);
    }};
    @Nullable
    public static Class<?> getClassFromDataType(final @NotNull Properties dataType) {
        return classMap.get(dataType);
    }
}
