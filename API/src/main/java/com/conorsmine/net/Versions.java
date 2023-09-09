package com.conorsmine.net;

import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum Versions {

    V1_12_2 ("com.conorsmine.net.versions.Version_1_12_2", (v) -> lowerOrEqualsNumbers(versionNumsToList(v), 1, 12, 2)),
    V1_15   ("com.conorsmine.net.versions.Version_1_15", (v) -> lowerOrEqualsNumbers(versionNumsToList(v), 1, 15));

    private final String className;
    private final Predicate<String> versionPredicate;

    Versions(String className, Predicate<String> versionPredicate) {
        this.className = className;
        this.versionPredicate = versionPredicate;
    }

    public Versionify createVersionify() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class<? extends Versionify> versionifyClass = (Class<? extends Versionify>) Class.forName(className);
        return versionifyClass.newInstance();
    }

    public static Versions determineVersion(@NotNull final Server server) {
        final String versionString = server.getBukkitVersion().replaceAll("-.+", "");
        return Arrays.stream(values())
                .filter((v) -> v.versionPredicate.test(versionString))
                .findFirst()
                .orElseGet(() -> values()[values().length - 1]);    // Always get the last, in case something goes wrong.
    }

    private static List<Integer> versionNumsToList(String version) {
        return Arrays.stream(version.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());
    }

    private static boolean lowerOrEqualsNumbers(List<Integer> versionNums, int... smallerOrEqualsNums) {
        for (int i = 0; i < Math.min(versionNums.size(), smallerOrEqualsNums.length); i++) {
            int vNum = versionNums.get(i);
            int sOrE = smallerOrEqualsNums[i];

            if (vNum > sOrE) return false;
        }

        return true;
    }

}
