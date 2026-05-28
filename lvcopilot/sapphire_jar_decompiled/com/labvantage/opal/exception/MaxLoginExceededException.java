/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.exception;

public class MaxLoginExceededException
extends Exception {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public MaxLoginExceededException(String user) {
        super("Maximum number of login attempts have been exceeded for user '" + user + "'");
    }
}

