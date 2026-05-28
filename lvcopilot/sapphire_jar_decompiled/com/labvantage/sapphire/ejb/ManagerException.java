/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

public class ManagerException
extends RuntimeException {
    public ManagerException() {
    }

    public ManagerException(Throwable e) {
        super(e);
    }

    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(String message, Throwable e) {
        super(message, e);
    }
}

