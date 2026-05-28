/*
 * Decompiled with CFR 0.152.
 */
package org.json;

public class JSONException
extends Exception {
    private Throwable cause;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}

