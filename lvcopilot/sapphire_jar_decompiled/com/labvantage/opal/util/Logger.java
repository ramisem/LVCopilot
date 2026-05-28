/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.sapphire.Trace;

public class Logger {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final int DEBUG = 0;
    public static final int EXCEPTION = 1;
    public static final int INFO = 2;
    public static final int WARNING = 3;

    public static void logTrace(int type, String message) {
        if (type == 0 && Trace.on) {
            Trace.logDebug(message);
        } else if (type == 1) {
            Trace.logError("Exception caught. " + message);
        } else if (type == 2) {
            Trace.logInfo(message);
        } else if (type == 3) {
            Trace.logWarn("Warning. " + message);
        }
    }
}

