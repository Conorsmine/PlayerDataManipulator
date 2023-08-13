package com.conorsmine.net.inventory;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.files.ConfigFile;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.google.common.base.Functions;
import de.tr7zw.nbtapi.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NBTInventoryUtils {

    private static final JavaPlugin pl = PlayerDataManipulator.getINSTANCE();

    public static List<NBTCompound> getItemNBTsFromPath(final NBTCompound compound, final NBTPath path) {
        final MojangsonUtils.NBTResult result = PlayerDataManipulator.INSTANCE.MOJANGSON.getCompoundFromPathSneakyThrow(compound, path);
        if (result == null) return new LinkedList<>();

        final NBTCompound nbtCompound = result.getCompound();
        final String finalKey = result.getFinalKey().getKeyValue();

        if (nbtCompound == null) return new LinkedList<>();
        switch (nbtCompound.getType(finalKey)) {
            case NBTTagCompound:
                final NBTCompound itemNBT = nbtCompound.getCompound(finalKey);
                if (!isItem(itemNBT)) break;
                return Collections.singletonList(itemNBT);

            case NBTTagList:
                return compound.getCompoundList(finalKey).stream()
                        .filter(NBTInventoryUtils::isItem)
                        .collect(Collectors.toList());
        }

        return new LinkedList<>();
    }

    public static CompletableFuture<List<NBTCompound>> getItemNBTsFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<List<NBTCompound>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemNBTsFromPath(compound, path)));
        return future;
    }

    public static Map<Integer, NBTCompound> getItemNBTsMapFromPath(final NBTCompound compound, final NBTPath path) {
        return getItemNBTsFromPath(compound, path).stream()
                .collect(Collectors.toMap(NBTInventoryUtils::getSlotFromItemNBT, Functions.identity()));
    }

    public static CompletableFuture<Map<Integer, NBTCompound>> getItemNBTsMapFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<Map<Integer, NBTCompound>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemNBTsMapFromPath(compound, path)));
        return future;
    }

    public static List<ItemStack> getItemFromPath(final NBTCompound compound, final NBTPath path) {
        final List<NBTCompound> itemNBTsFromPath = getItemNBTsFromPath(compound, path);
        final List<ItemStack> itemList = new LinkedList<>();

        for (NBTCompound nbtCompound : itemNBTsFromPath) { itemList.add(NBTItem.convertNBTtoItem(nbtCompound)); }

        return itemList;
    }

    public static CompletableFuture<List<ItemStack>> getItemFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<List<ItemStack>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemFromPath(compound, path)));
        return future;
    }

    public static Map<Integer, ItemStack> getItemMapFromPath(final NBTCompound compound, final NBTPath path) {
        return getItemNBTsFromPath(compound, path).stream()
                .collect(Collectors.toMap(NBTInventoryUtils::getSlotFromItemNBT, NBTItem::convertNBTtoItem));
    }

    public static CompletableFuture<Map<Integer, ItemStack>> getItemMapFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<Map<Integer, ItemStack>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemMapFromPath(compound, path)) );
        return future;
    }

    @Nullable
    public static NBTCompound removeNBT(final NBTCompound compound, final NBTPath path, final Predicate<NBTCompound> removePredicate) {
        final MojangsonUtils.NBTResult result = PlayerDataManipulator.INSTANCE.MOJANGSON.getCompoundFromPathSneakyThrow(compound, path);
        if (result == null) return null;

        final NBTCompound nbtCompound = result.getCompound();
        final String finalKey = result.getFinalKey().getKeyValue();

        if (nbtCompound == null) return null;
        switch (nbtCompound.getType(finalKey)) {
            case NBTTagCompound:
                final NBTCompound nbt = nbtCompound.getCompound(finalKey);
                if (removePredicate.test(nbt)) {
                    nbtCompound.removeKey(finalKey);
                    nbt.removeKey(finalKey);
                    return nbt;
                }
                break;

            case NBTTagList:
                final NBTCompoundList nbtList = nbtCompound.getCompoundList(finalKey);
                for (int i = 0; i < nbtList.size(); i++) {
                    final NBTListCompound nbt1 = nbtList.get(i);
                    if (removePredicate.test(nbt1)) {
                        nbtList.remove(nbt1);
                        return nbt1;
                    }
                }
                break;
        }

        return null;
    }

    public static CompletableFuture<@Nullable NBTCompound> removeNBTAsync(final NBTCompound compound, final NBTPath path, final Predicate<NBTCompound> remove) {
        final CompletableFuture<NBTCompound> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(removeNBT(compound, path, remove)) );
        return future;
    }

    private static boolean isItem(final NBTCompound compound) {
        return (compound.hasKey(NBTItemTags.ID.getTagName())
                && compound.hasKey(NBTItemTags.DAMAGE.getTagName())
                && compound.hasKey(NBTItemTags.COUNT.getTagName())
                && compound.hasKey(NBTItemTags.SLOT.getTagName()));
    }

    private static int getSlotFromItemNBT(final NBTCompound compound) {
        return compound.getInteger(NBTItemTags.SLOT.getTagName());
    }

    private static void runSync(final Consumer<NBTCompound> syncTask, final NBTCompound nbt) {
        final PlayerDataManipulator pl = PlayerDataManipulator.getINSTANCE();
        pl.getServer().getScheduler().runTask(pl, () -> syncTask.accept(nbt));
    }
}
