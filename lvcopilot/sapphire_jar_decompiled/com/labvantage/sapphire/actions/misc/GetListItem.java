/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.Trace;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetListItem
extends BaseAction
implements sapphire.action.GetListItem {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        int index;
        String list = properties.getProperty("list");
        String separator = properties.getProperty("separator");
        if (separator.length() == 0) {
            separator = ";";
        }
        try {
            index = Integer.parseInt(properties.getProperty("index"));
        }
        catch (Exception e) {
            index = -1;
        }
        String[] items = StringUtil.split(list, separator);
        if (index <= 0 || index > items.length) {
            index = items.length;
        }
        if (items.length > 0 && index <= items.length) {
            properties.setProperty("listitem", items[index - 1]);
            if (Trace.on) {
                Trace.log("Found value for index " + index + ": " + items[index - 1]);
            }
        }
    }
}

