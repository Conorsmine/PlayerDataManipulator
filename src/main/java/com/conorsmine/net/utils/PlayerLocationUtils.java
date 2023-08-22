package com.conorsmine.net.utils;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.conorsmine.net.mojangson.MojangsonUtilsBuilder;
import com.conorsmine.net.mojangson.NBTQueryResult;
import com.conorsmine.net.mojangson.data.NBTCompoundData;
import com.conorsmine.net.mojangson.data.NBTDataType;
import com.conorsmine.net.mojangson.data.NBTPrimitiveData;
import com.conorsmine.net.mojangson.path.NBTPath;
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
import org.jetbrains.annotations.Nullable;

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
        final NBTCompound nbtCompound = playerData.getCompound();
        PlayerLocPaths.setPositionX(nbtCompound, tpLoc.getX());
        PlayerLocPaths.setPositionY(nbtCompound, tpLoc.getY());
        PlayerLocPaths.setPositionZ(nbtCompound, tpLoc.getZ());
        PlayerLocPaths.setPitch(nbtCompound, tpLoc.getPitch());
        PlayerLocPaths.setYaw(nbtCompound, tpLoc.getYaw());
        PlayerLocPaths.setWorld(nbtCompound, tpLoc.getWorld());

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

        private final static MojangsonUtils mojangson = new MojangsonUtilsBuilder("##").create();
        private final String path;

        PlayerLocPaths(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        static Double getPositionX(final NBTCompound nbt) {
            final NBTQueryResult queryResult = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(POS_X.path).create());
            if (queryResult.getData().getType() != NBTDataType.PRIMITIVE) return 0d;
            
            return ((Double) ((NBTPrimitiveData) queryResult.getData()).getData());
        }

        static Double getPositionY(final NBTCompound nbt) {
            final NBTQueryResult queryResult = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(POS_Y.path).create());
            if (queryResult.getData().getType() != NBTDataType.PRIMITIVE) return 0d;
            
            return ((Double) ((NBTPrimitiveData) queryResult.getData()).getData());
        }

        static Double getPositionZ(final NBTCompound nbt) {
            final NBTQueryResult queryResult = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(POS_Z.path).create());
            if (queryResult.getData().getType() != NBTDataType.PRIMITIVE) return 0d;
            
            return ((Double) ((NBTPrimitiveData) queryResult.getData()).getData());
        }

        static Float getPitch(final NBTCompound nbt) {
            final NBTQueryResult queryResult = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(ROT_PITCH.path).create());
            if (queryResult.getData().getType() != NBTDataType.PRIMITIVE) return 0f;

            return ((Float) ((NBTPrimitiveData) queryResult.getData()).getData());
        }

        static Float getYaw(final NBTCompound nbt) {
            final NBTQueryResult queryResult = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(ROT_YAW.path).create());
            if (queryResult.getData().getType() != NBTDataType.PRIMITIVE) return 0f;

            return ((Float) ((NBTPrimitiveData) queryResult.getData()).getData());
        }

        @Nullable
        static World getWorld(final NBTCompound nbt) {
            final NBTQueryResult queryResultMost = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(WORLD_UUID_MOST.path).create());
            final NBTQueryResult queryResultLeast = mojangson.getDataFromPath(new NBTCompoundData(nbt), new NBTPathBuilder(mojangson).parseString(WORLD_UUID_LEAST.path).create());
            if (queryResultMost.getData().getType() != NBTDataType.PRIMITIVE || 
                    queryResultLeast.getData().getType() != NBTDataType.PRIMITIVE) return null;
            
            
            try { return Bukkit.getServer().getWorld(new UUID(((Long) queryResultMost.getData().getData()), ((Long) queryResultLeast.getData().getData()))); }
            catch (Exception e) {
                throw new RuntimeException(
                        String.format("There was an error retrieving the world from the player!%n\"%s\"%nWorld_ID: %s  Player_ID: %s  UUIDMost: %s  UUIDLeast: %s",
                                e.getClass().getName(), WORLD_DIM_ID.path, PLAYER_DIM_ID.path, WORLD_UUID_MOST.path, WORLD_UUID_LEAST.path)
                );
            }
        }



        static void setPositionX(final NBTCompound nbt, final double x) {
            final NBTPath keys = new NBTPathBuilder(mojangson).parseString(POS_X.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, x, double.class);
        }

        static void setPositionY(final NBTCompound nbt, final double y) {
            final NBTPath keys = new NBTPathBuilder(mojangson).parseString(POS_Y.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, y, double.class);
        }

        static void setPositionZ(final NBTCompound nbt, final double z) {
            final NBTPath keys = new NBTPathBuilder(mojangson).parseString(POS_Z.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, z, double.class);
        }

        static void setYaw(final NBTCompound nbt, final float yaw) {
            final NBTPath keys = new NBTPathBuilder(mojangson).parseString(ROT_YAW.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, yaw, float.class);
        }

        static void setPitch(final NBTCompound nbt, final float pitch) {
            final NBTPath keys = new NBTPathBuilder(mojangson).parseString(ROT_PITCH.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, pitch, float.class);
        }

        static void setWorld(final NBTCompound nbt, final World world) {
            NBTPath keys = new NBTPathBuilder(mojangson).parseString(WORLD_UUID_MOST.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, world.getUID().getMostSignificantBits(), Long.class);

            keys = new NBTPathBuilder(mojangson).parseString(WORLD_UUID_LEAST.path).create();
            mojangson.setDataToPath(new NBTCompoundData(nbt), keys, world.getUID().getLeastSignificantBits(), Long.class);
        }
    }
}
