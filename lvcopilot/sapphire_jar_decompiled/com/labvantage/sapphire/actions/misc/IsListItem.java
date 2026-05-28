/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import java.util.StringTokenizer;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class IsListItem
extends BaseAction
implements sapphire.action.IsListItem {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        String list = properties.getProperty("list");
        String separator = properties.getProperty("separator");
        String item = properties.getProperty("item");
        if (item.length() == 0) {
            item = properties.getProperty("listitem");
        }
        String islistitem = "No";
        if (separator.length() == 0) {
            separator = ";";
        }
        StringTokenizer st = new StringTokenizer(list, separator);
        while (st.hasMoreTokens()) {
            if (!item.equals(st.nextToken())) continue;
            islistitem = "Yes";
        }
        properties.setProperty("islistitem", islistitem);
    }
}

