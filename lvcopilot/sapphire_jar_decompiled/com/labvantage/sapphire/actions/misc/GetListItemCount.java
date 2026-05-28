/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import java.util.StringTokenizer;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class GetListItemCount
extends BaseAction
implements sapphire.action.GetListItemCount {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        String list = properties.getProperty("list");
        String separator = properties.getProperty("separator");
        if (separator.length() == 0) {
            separator = ";";
        }
        StringTokenizer st = new StringTokenizer(list, separator);
        int i = st.countTokens();
        properties.setProperty("listitemcount", Integer.toString(i));
        if (i == 0) {
            properties.setProperty("listitemsize", "None");
        } else if (i == 1) {
            properties.setProperty("listitemsize", "One");
        } else if (i > 1) {
            properties.setProperty("listitemsize", "Many");
        }
    }
}

