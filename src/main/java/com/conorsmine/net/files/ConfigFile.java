package com.conorsmine.net.files;

import com.conorsmine.net.utils.LazyConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ConfigFile implements ReloadableFile {

    private final JavaPlugin pl;
    private final LazyConfig<String> parserSeparator;
    private final LazyConfig<Integer> searchMaxExecutions;

    public ConfigFile(JavaPlugin pl) {
        this.pl = pl;

        this.parserSeparator = new LazyConfig<>(
                () -> pl.getConfig().getString("separator"), "##",
                (val) -> "§7\"§6%s§7\" is §enot §7a valid separator. Please select something else."
        );

        this.searchMaxExecutions = new LazyConfig<>(
                () -> pl.getConfig().getInt("search_workers"), 1, (val) -> ((val <= 0) || (val > 100)),
                (val) -> "§7\"§6%d§7\" is §enot§7 a valid amount of workers. Please select something between 1 and 100."
        );
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        this.parserSeparator.reset();
        this.searchMaxExecutions.reset();
    }

    public String getSeparator() {
        return this.parserSeparator.get();
    }

    public int getMaxSearchExecutors() {
        return this.searchMaxExecutions.get();
    }





    @Deprecated
    public static String staticGetSeparator() {
        return "##";
    }

    @Deprecated
    public static int staticGetMaxExecutions() {
        return 5;
    }
}
