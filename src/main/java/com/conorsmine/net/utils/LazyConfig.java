package com.conorsmine.net.utils;

import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

public class LazyConfig<T> extends Lazy<T> {

    private final T defaultValue;
    private final Predicate<T> checker;
    private final Function<T, String> errorMsg;

    /**
     * @param checker Should return true if the value is invalid
     */
    public LazyConfig(Supplier<T> provider, T defaultValue, Predicate<T> checker, Function<T, String> errorMsg) {
        super(provider);

        this.defaultValue = defaultValue;
        this.checker = checker;
        this.errorMsg = errorMsg;
    }

    public LazyConfig(Supplier<T> provider, T defaultValue, Function<T, String> errorMsg) {
        this(provider, defaultValue, Objects::isNull, errorMsg);
    }

    @Override
    public T get() {
        final T val = super.get();
        if (checker.test(val)) { printErrorMsg(val); return this.defaultValue; }

        return val;
    }

    private void printErrorMsg(T val) {
        Bukkit.getLogger().log(Level.WARNING,errorMsg.apply(val));
        Bukkit.getLogger().log(Level.WARNING, String.format("§7Defaulted to: \"§6%s§7\"", this.defaultValue));
    }
}
