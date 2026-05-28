/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddListItem
extends BaseAction
implements sapphire.action.AddListItem {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        int index;
        String list = properties.getProperty("list");
        String separator = properties.getProperty("separator");
        String item = properties.getProperty("listitem");
        try {
            index = Integer.parseInt(properties.getProperty("index"));
        }
        catch (Exception e) {
            index = 99999999;
        }
        if (separator.length() == 0) {
            separator = ";";
        }
        String[] items = StringUtil.split(list, separator);
        if (index == 0 || index > items.length) {
            properties.setProperty("outputlist", list + separator + item);
        } else if (index == 1) {
            properties.setProperty("outputlist", item + separator + list);
        } else {
            StringBuffer outputlist = new StringBuffer();
            for (int i = 0; i < items.length; ++i) {
                if (i + 1 == index) {
                    outputlist.append(separator).append(item);
                }
                outputlist.append(separator).append(items[i]);
            }
            properties.setProperty("outputlist", outputlist.substring(separator.length()));
        }
    }
}

