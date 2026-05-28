/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.BaseException;

public class ServiceException
extends BaseException {
    public ServiceException(Throwable e) {
        super(e);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceException(String errorid, String message, Throwable e) {
        super(errorid, message, e);
    }

    public ServiceException(String errorid, String message) {
        super(errorid, message);
    }
}

