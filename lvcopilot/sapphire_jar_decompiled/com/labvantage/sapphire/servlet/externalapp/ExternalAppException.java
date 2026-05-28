/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.externalapp;

import com.labvantage.sapphire.servlet.rest.RestException;

public class ExternalAppException
extends RestException {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public ExternalAppException(int httpCode, String error, String message) {
        super(httpCode, error, message);
    }

    public ExternalAppException(int httpCode, String error, String message, Throwable e) {
        super(httpCode, error, message, e);
    }
}

