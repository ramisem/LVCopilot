/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics;

import sapphire.SapphireException;

public class DiagnosticException
extends SapphireException {
    public static final int TEST_FAILED = 1;
    public static final int ERROR = 2;
    private int status;

    public DiagnosticException(int status, String message) {
        super(message);
        this.status = status;
    }

    public DiagnosticException(Exception e) {
        super(e.getMessage());
        this.status = e instanceof DiagnosticException ? ((DiagnosticException)e).getStatus() : 2;
    }

    public int getStatus() {
        return this.status;
    }
}

