package com.conorsmine.net.utils;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.MojangsonUtilsBuilder;
import com.conorsmine.net.mojangson.path.NBTArrayKey;
import com.conorsmine.net.mojangson.path.NBTKey;
import com.conorsmine.net.mojangson.path.NBTPathBuilder;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.data.NBTData;
import de.tr7zw.nbtapi.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLocationUtils {

    public static Location getPlayerPos(@NotNull final OfflinePlayer player) {
        if (player.isOnline()) return player.getPlayer().getLocation();

        final NBTCompound playerData = NBTData.getOfflinePlayerData(player.getUniqueId()).getCompound();
        return new Location(
                PlayerLocPaths.getWorld(playerData),
                PlayerLocPaths.getPositionX(playerData),
                PlayerLocPaths.getPositionY(playerData),
                PlayerLocPaths.getPositionZ(playerData),
                PlayerLocPaths.getYaw(playerData),
                PlayerLocPaths.getPitch(playerData)
                );
    }

    public static void teleportPlayer(@NotNull final OfflinePlayer player, @NotNull final Location tpLoc) {
        if (player.isOnline()) { ((Player) player).teleport(tpLoc); }

        final PlayerData playerData = NBTData.getOfflinePlayerData(player.getUniqueId());
        PlayerLocPaths.setPositionX(playerData, tpLoc.getX());
        PlayerLocPaths.setPositionY(playerData, tpLoc.getY());
        PlayerLocPaths.setPositionZ(playerData, tpLoc.getZ());
        PlayerLocPaths.setPitch(playerData, tpLoc.getPitch());
        PlayerLocPaths.setYaw(playerData, tpLoc.getYaw());
        PlayerLocPaths.setWorld(playerData, tpLoc.getWorld());

        playerData.saveChanges();
    }




    public enum PlayerLocPaths {
        POS_X               ("Pos[0]"),
        POS_Y               ("Pos[1]"),
        POS_Z               ("Pos[2]"),

        ROT_YAW             ("Rotation[0]"),
        ROT_PITCH           ("Rotation[1]"),

        PLAYER_DIM_ID       ("Dimension"),
        WORLD_DIM_ID        ("dimension"),
        WORLD_UUID_MOST     ("WorldUUIDMost"),
        WORLD_UUID_LEAST    ("WorldUUIDLeast");

        private final static MojangsonUtils mojangson = new MojangsonUtilsBuilder(".").create();
        private final String path;

        PlayerLocPaths(String path) {
            this.path = path;
        }

        static Double getPositionX(final NBTCompound nbt) {
            return getDataFromArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(POS_X.path).create()));
        }

        static Double getPositionY(final NBTCompound nbt) {
            return getDataFromArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(POS_Y.path).create()));
        }

        static Double getPositionZ(final NBTCompound nbt) {
            return getDataFromArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(POS_Z.path).create()));
        }

        static Float getPitch(final NBTCompound nbt) {
            return getDataFromArrayCompound(mojangson, Float.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(ROT_PITCH.path).create()));
        }

        static Float getYaw(final NBTCompound nbt) {
            return getDataFromArrayCompound(mojangson, Float.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(ROT_YAW.path).create()));
        }

        static World getWorld(final NBTCompound nbt) {
            final Long uuidMost = mojangson.getSimpleDataFromCompound(Long.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(WORLD_UUID_MOST.path).create()));
            final Long uuidLeast = mojangson.getSimpleDataFromCompound(Long.class, mojangson.getCompoundFromPath(nbt, new NBTPathBuilder(mojangson).parseString(WORLD_UUID_LEAST.path).create()));

            return Bukkit.getServer().getWorld(new UUID(uuidMost, uuidLeast));
        }



        static void setPositionX(final PlayerData playerData, final double x) {
            setDataToArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(playerData.getCompound(), new NBTPathBuilder(mojangson).parseString(POS_X.path).create()), x);
        }

        static void setPositionY(final PlayerData playerData, final double y) {
            setDataToArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(playerData.getCompound(), new NBTPathBuilder(mojangson).parseString(POS_Y.path).create()), y);
        }

        static void setPositionZ(final PlayerData playerData, final double z) {
            setDataToArrayCompound(mojangson, Double.class, mojangson.getCompoundFromPath(playerData.getCompound(), new NBTPathBuilder(mojangson).parseString(POS_Z.path).create()), z);
        }

        static void setYaw(final PlayerData playerData, final float yaw) {
            setDataToArrayCompound(mojangson, Float.class, mojangson.getCompoundFromPath(playerData.getCompound(), new NBTPathBuilder(mojangson).parseString(ROT_YAW.path).create()), yaw);
        }

        static void setPitch(final PlayerData playerData, final float pitch) {
            setDataToArrayCompound(mojangson, Float.class, mojangson.getCompoundFromPath(playerData.getCompound(), new NBTPathBuilder(mojangson).parseString(ROT_PITCH.path).create()), pitch);
        }

        static void setWorld(final PlayerData playerData, final World world) {
            playerData.getCompound().setLong(WORLD_UUID_MOST.path, world.getUID().getMostSignificantBits());
            playerData.getCompound().setLong(WORLD_UUID_LEAST.path, world.getUID().getLeastSignificantBits());
            playerData.getCompound().setLong(PLAYER_DIM_ID.path, NBTData.getWorldData(world).getCompound().getLong(WORLD_DIM_ID.path));
        }







        @SuppressWarnings("unchecked")
        private static <T> T getDataFromArrayCompound(final MojangsonUtils mojangson, final Class<T> clazzCast, final MojangsonUtils.NBTResult result) {
            final NBTArrayKey arrKey = (NBTArrayKey) result.getFinalKey();

            if (clazzCast.equals(Double.class)) return (T) result.getCompound().getDoubleList(arrKey.getKeyValue()).get(arrKey.getIndex());
            else return (T) result.getCompound().getFloatList(arrKey.getKeyValue()).get(arrKey.getIndex());
        }

        private static <T> void setDataToArrayCompound(final MojangsonUtils mojangson, final Class<?> clazzCast, final MojangsonUtils.NBTResult result, T val) {
            final NBTArrayKey arrKey = (NBTArrayKey) result.getFinalKey();

            if (clazzCast.equals(Double.class)) result.getCompound().getDoubleList(arrKey.getKeyValue()).set(arrKey.getIndex(), (Double) val);
            else result.getCompound().getFloatList(arrKey.getKeyValue()).set(arrKey.getIndex(), (Float) val);
        }
    }
}
