/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PropertyListUtil {
    private static final String DELIM = "|!|";

    public static String concatenateProps(PropertyList properties) {
        StringBuffer props = new StringBuffer(100);
        for (String propertyid : properties.keySet()) {
            if (propertyid == null || propertyid.length() <= 0) continue;
            props.append(DELIM).append(propertyid).append("=").append(properties.getProperty(propertyid));
        }
        String s = props.toString();
        if (s.length() > 0) {
            s = s.substring(DELIM.length());
        }
        return s;
    }

    public static void setDelimiteredProps(PropertyList propertyList, String props) {
        String[] p;
        if (props != null && props.length() > 0 && (p = StringUtil.split(props, DELIM)).length > 0 && p[0].indexOf(61) != -1) {
            for (int i = 0; i < p.length; ++i) {
                int j = p[i].indexOf(61);
                if (j <= 0) continue;
                propertyList.setProperty(p[i].substring(0, j), p[i].substring(j + 1));
            }
        }
    }

    public static boolean isDelimeteredProps(String props) {
        String[] p = StringUtil.split(props, DELIM);
        return p != null && p.length > 0 && p[0].indexOf(61) != -1;
    }
}

