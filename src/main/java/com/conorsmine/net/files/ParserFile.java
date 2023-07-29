package com.conorsmine.net.files;

import com.conorsmine.net.PlayerDataManipulator;

public class ParserFile {

    private static final String PATH = "data_parser";

    private static final String SEP = PlayerDataManipulator.INSTANCE.getConfig().getString(String.format("%s.separator", PATH), "§#§");
    public static String getSeparator() {
        return SEP;
    }
}
