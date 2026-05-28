/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import java.util.HashMap;
import sapphire.xml.PropertyListCollection;

public interface BindingMap {
    public HashMap<String, Object> toHashMap();

    public PropertyListCollection getTokenValues();
}

