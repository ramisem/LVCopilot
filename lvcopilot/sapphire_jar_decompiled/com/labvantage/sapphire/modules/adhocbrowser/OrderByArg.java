/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocArgument;

public class OrderByArg
extends AdhocArgument {
    public static final String A = "asc";
    public static final String B = "desc";
    private String direction = "asc";

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String dir) {
        if (dir.length() >= 1) {
            this.direction = dir.charAt(0) == 'a' || dir.charAt(0) == 'A' ? A : B;
        }
    }
}

