/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.servlet.rest.RestConstants;

public class RestException
extends Exception
implements RestConstants {
    private int httpCode = 200;
    private String error;

    public RestException(int httpCode, String error, String message) {
        super(message);
        this.httpCode = httpCode;
        this.error = error;
    }

    public RestException(int httpCode, String error, String message, Throwable e) {
        super(message, e);
        this.httpCode = httpCode;
        this.error = error;
    }

    public int getHttpCode() {
        return this.httpCode;
    }

    public String getError() {
        return this.error;
    }
}

