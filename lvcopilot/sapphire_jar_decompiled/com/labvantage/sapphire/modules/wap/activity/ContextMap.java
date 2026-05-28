/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import java.util.LinkedHashMap;
import java.util.Map;
import sapphire.util.StringUtil;

public class ContextMap {
    private Map<String, String> map = new LinkedHashMap<String, String>();

    public ContextMap() {
    }

    public ContextMap(String context) {
        if (context != null && context.length() > 0) {
            String[] parts = StringUtil.split(context, "|!|");
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                int pos = part.indexOf(":");
                if (pos <= 0) continue;
                String value = part.length() > pos ? part.substring(pos + 1) : "";
                this.map.put(part.substring(0, pos), value.equals("(none)") ? "" : value);
            }
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String key : this.map.keySet()) {
            out.append(out.length() > 0 ? "|!|" : "");
            out.append(key + ":");
            String value = this.map.get(key);
            out.append(value == null || value.length() == 0 ? "(none)" : value);
        }
        return out.toString();
    }

    public void put(String key, String value) {
        if (key != null && key.length() > 0) {
            this.map.put(key, value);
        }
    }

    public String get(String key) {
        return this.get(key, "");
    }

    public String get(String key, String defaultValue) {
        String value = this.map.get(key);
        return value == null || value.length() == 0 ? defaultValue : value;
    }
}

