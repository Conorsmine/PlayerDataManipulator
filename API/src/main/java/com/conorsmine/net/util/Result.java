package com.conorsmine.net.util;

public final class Result<T> {

    private final T result;

    private final boolean successful;
    private final String resultMsg;

    public Result(T result, boolean successful, String resultMsg) {
        this.result = result;
        this.successful = successful;
        this.resultMsg = resultMsg;
    }

    public Result(T result) {
        this(result, true, "");
    }

    public T getResult() {
        return result;
    }

    public boolean wasSuccessful() {
        return successful;
    }

    public String getResultMsg() {
        return resultMsg;
    }
}
