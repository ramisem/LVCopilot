/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.util.HashMap;
import java.util.Set;

public class PropertyHandlerUtil {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static HashMap filterProps(String prefix, HashMap props) {
        HashMap requestParamMap = new HashMap();
        int index = prefix.length();
        Set keySet = props.keySet();
        for (String key : keySet) {
            if (!key.startsWith(prefix)) continue;
            requestParamMap.put(key.substring(index), props.get(key));
        }
        return requestParamMap;
    }
}

