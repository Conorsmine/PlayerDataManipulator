package com.conorsmine.net.files;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ReloadableFile {

    void reload(@NotNull final CommandSender sender);
}
