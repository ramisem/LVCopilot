/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

public class BaseException
extends Exception {
    private String errorid = "GENERAL_ERROR";

    public BaseException(Throwable e) {
        super(e);
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable e) {
        super(message, e);
    }

    public BaseException(String errorid, String message, Throwable e) {
        super(message, e);
        this.errorid = errorid;
    }

    public BaseException(String errorid, String message) {
        super(message);
        this.errorid = errorid;
    }

    public String getErrorid() {
        return this.errorid;
    }
}

