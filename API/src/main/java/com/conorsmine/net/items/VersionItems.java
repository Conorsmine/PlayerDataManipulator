package com.conorsmine.net.items;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.conorsmine.net.util.Result;
import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VersionItems {

    ContextResolver<@Nullable VersionItemWrapper, BukkitCommandExecutionContext> versionItemContextResolver(@NotNull final JavaPlugin pl);

    /*
    * The format that will be placed in the cmd syntax;
    * e.g: <itemMaterial> [damage] [data]
    */
    String versionItemCmdFormat();

    String getVersionSearchCmdFormat();

    List<String> nbtToVersionLoreData(final @NotNull NBTCompound itemNBT);

    String consoleItemVersionDataFromNBT(final @NotNull NBTCompound itemNBT);

    String[] playerItemVersionDataFromNBT(final @NotNull List<NBTCompound> itemNBTs);

    String consoleItemDataHeader();

    String playerItemDataHeader(final @NotNull List<NBTCompound> itemNBTs);


    /*
     * Used for the search command.
     */
    boolean partialMath(final @NotNull NBTCompound itemNBT, String itemID, short dataVal);

    Result<@Nullable NBTCompound> itemNBTToItem(final @NotNull String nbt);

    Result<Boolean> isValidItemNBT(final @NotNull NBTCompound nbt);

    void addDataToCreatedItem(final @NotNull NBTCompound createdNBT, final @NotNull NBTCompound dataComp);

}
