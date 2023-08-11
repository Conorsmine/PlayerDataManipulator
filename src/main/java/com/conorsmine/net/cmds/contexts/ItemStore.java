package com.conorsmine.net.cmds.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.files.NBTStoreFile;

import java.io.*;

public class ItemStore {

    private final String storedNBT;

    private ItemStore(String storedNBT) {
        this.storedNBT = storedNBT;
    }

    public String getStoredNBT() {
        return storedNBT;
    }



    public static ContextResolver<ItemStore, BukkitCommandExecutionContext> getContextResolver() {
        return c -> {
            final String s = c.popFirstArg();

            final File[] files = NBTStoreFile.storeFiles();
            for (File file : files) {
                if (file.getName().replaceAll("\\.txt$", "").equalsIgnoreCase(s))
                    return parseStore(file);
            }

            return null;
        };
    }

    private static ItemStore parseStore(final File file) {
        try {
            final StringBuilder nbt = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new FileReader(file));

            String in = reader.readLine();
            while (in != null) { nbt.append(in); in = reader.readLine(); }

            reader.close();
            return new ItemStore(nbt.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
