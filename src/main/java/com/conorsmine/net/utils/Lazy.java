package com.conorsmine.net.utils;

import java.util.function.Supplier;

public class Lazy<T> {

    private T val;
    private final Supplier<T> provider;

    public Lazy(Supplier<T> provider) {
        this.provider = provider;
    }

    public T get() {
        if (val == null) val = provider.get();
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }
}
