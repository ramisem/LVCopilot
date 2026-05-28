/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared;

import com.labvantage.sapphire.gwt.shared.JSONable;

public class JSONableString
implements JSONable {
    private String string;

    public JSONableString(String string) {
        this.string = string;
    }

    @Override
    public String toJSONString() {
        return this.string;
    }

    public String toString() {
        return this.string;
    }
}

