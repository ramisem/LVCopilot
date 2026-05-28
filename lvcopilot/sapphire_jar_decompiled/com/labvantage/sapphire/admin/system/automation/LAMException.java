/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

import com.labvantage.sapphire.BaseException;

public class LAMException
extends BaseException {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public LAMException(Throwable e) {
        super(e);
    }

    public LAMException(String message) {
        super(message);
    }

    public LAMException(String message, Throwable e) {
        super(message, e);
    }
}

