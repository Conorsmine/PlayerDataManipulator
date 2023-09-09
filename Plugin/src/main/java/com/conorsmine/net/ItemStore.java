package com.conorsmine.net;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;

import java.io.*;

public class ItemStore {

    private final String storedNBT;

    public ItemStore(String storedNBT) {
        this.storedNBT = storedNBT;
    }

    public String getStoredNBT() {
        return storedNBT;
    }
}
