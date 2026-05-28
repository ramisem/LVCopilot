/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class TextArgumentConfiguration
extends BaseCustom
implements Serializable {
    private final ArgumentConfiguration parent;

    public TextArgumentConfiguration(PropertyList textArgumentProps, String connectionId, ArgumentConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (textArgumentProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.setConnectionId(connectionId);
        this.parent = parent;
    }

    public ArgumentConfiguration getParent() {
        return this.parent;
    }
}

