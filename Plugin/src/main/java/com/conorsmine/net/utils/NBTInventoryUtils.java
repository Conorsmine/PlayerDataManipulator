package com.conorsmine.net.utils;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.mojangson.path.NBTPath;
import com.conorsmine.net.items.NBTItemTags;
import com.conorsmine.net.mojangson.NBTQueryResult;
import com.conorsmine.net.mojangson.data.ICompoundData;
import com.conorsmine.net.mojangson.data.NBTCompoundData;
import com.conorsmine.net.mojangson.data.NBTCompoundListData;
import com.google.common.base.Functions;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NBTInventoryUtils {

    private final PlayerDataManipulator pl;

    public NBTInventoryUtils(PlayerDataManipulator pl) {
        this.pl = pl;
    }

    public List<NBTCompound> getItemNBTsFromPath(final NBTCompound compound, final NBTPath path) {
        final NBTQueryResult result = pl.MOJANGSON.getDataFromPathSneakyThrow(new NBTCompoundData(compound), path);
        if (result == null) return new LinkedList<>();

        if (result.getData() == null) return new LinkedList<>();
        switch (result.getData().getType()) {
            case COMPOUND:
                final NBTCompound itemNBT = ((NBTCompoundData) result.getData()).getData();
                if (!isItem(itemNBT)) break;
                return Collections.singletonList(itemNBT);

            case COMPOUND_LIST:
                return ((NBTCompoundListData) result.getData()).getData().stream()
                        .filter(NBTInventoryUtils::isItem)
                        .collect(Collectors.toList());
        }

        return new LinkedList<>();
    }

    public CompletableFuture<List<NBTCompound>> getItemNBTsFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<List<NBTCompound>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemNBTsFromPath(compound, path)));
        return future;
    }

    public Map<Integer, NBTCompound> getItemNBTsMapFromPath(final NBTCompound compound, final NBTPath path) {
        return getItemNBTsFromPath(compound, path).stream()
                .collect(Collectors.toMap(NBTInventoryUtils::getSlotFromItemNBT, Functions.identity()));
    }

    public CompletableFuture<Map<Integer, NBTCompound>> getItemNBTsMapFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<Map<Integer, NBTCompound>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemNBTsMapFromPath(compound, path)));
        return future;
    }

    public List<ItemStack> getItemFromPath(final NBTCompound compound, final NBTPath path) {
        final List<NBTCompound> itemNBTsFromPath = getItemNBTsFromPath(compound, path);
        final List<ItemStack> itemList = new LinkedList<>();

        for (NBTCompound nbtCompound : itemNBTsFromPath) { itemList.add(NBTItem.convertNBTtoItem(nbtCompound)); }

        return itemList;
    }

    public CompletableFuture<List<ItemStack>> getItemFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<List<ItemStack>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemFromPath(compound, path)));
        return future;
    }

    public Map<Integer, ItemStack> getItemMapFromPath(final NBTCompound compound, final NBTPath path) {
        return getItemNBTsFromPath(compound, path).stream()
                .collect(Collectors.toMap(NBTInventoryUtils::getSlotFromItemNBT, NBTItem::convertNBTtoItem));
    }

    public CompletableFuture<Map<Integer, ItemStack>> getItemMapFromPathAsync(final NBTCompound compound, final NBTPath path) {
        final CompletableFuture<Map<Integer, ItemStack>> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(getItemMapFromPath(compound, path)) );
        return future;
    }

    @Nullable
    public NBTCompound removeNBT(final NBTCompound compound, final NBTPath path, final Predicate<NBTCompound> removePredicate) {
        final NBTQueryResult result = pl.MOJANGSON.getDataFromPathSneakyThrow(new NBTCompoundData(compound), path);
        if (result == null) return null;

        final ICompoundData<?> nbtCompound = ((ICompoundData<?>) result.getData());

        if (nbtCompound == null) return null;
        switch (nbtCompound.getType()) {
            case COMPOUND:
                final NBTCompound nbt = ((NBTCompoundData) nbtCompound).getData();
                if (removePredicate.test(nbt)) {
                    nbt.removeKey(path.getLastKey().getKeyValue());
                    return nbt;
                }
                break;

            case COMPOUND_LIST:
                final NBTCompoundList nbtList = ((NBTCompoundListData) nbtCompound).getData();
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

    public CompletableFuture<@Nullable NBTCompound> removeNBTAsync(final NBTCompound compound, final NBTPath path, final Predicate<NBTCompound> remove) {
        final CompletableFuture<NBTCompound> future = new CompletableFuture<>();

        pl.getServer().getScheduler().runTaskAsynchronously(pl, () -> future.complete(removeNBT(compound, path, remove)) );
        return future;
    }

    private static boolean isItem(final NBTCompound compound) {
        return PlayerDataManipulator.VERSION_SUPPORT.getVersionItemAPI().isValidItemNBT(compound).wasSuccessful();
    }

    private static int getSlotFromItemNBT(final NBTCompound compound) {
        return compound.getInteger(NBTItemTags.SLOT.getTagName());
    }
}
