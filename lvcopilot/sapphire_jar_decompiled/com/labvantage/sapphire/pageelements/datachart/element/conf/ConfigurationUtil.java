/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import java.util.ArrayList;
import java.util.List;

public final class ConfigurationUtil {
    private ConfigurationUtil() {
    }

    public static <C extends ConfigurationListItem> C getConf(List<C> confList, String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Identifier is null or empty: " + id);
        }
        if (confList == null) {
            throw new IllegalArgumentException("Configuration list is null");
        }
        ConfigurationListItem returnConf = null;
        for (ConfigurationListItem conf : confList) {
            String itemId = conf.getId();
            if (!itemId.equals(id)) continue;
            returnConf = conf;
            break;
        }
        return (C)returnConf;
    }

    public static <C extends ConfigurationListItem> int getAnonymousConf(List<C> confList, int relativeIndex) {
        if (confList == null) {
            throw new IllegalArgumentException("Configuration list is null");
        }
        if (relativeIndex < 0) {
            throw new IllegalArgumentException("Relative index is negative: " + relativeIndex);
        }
        ArrayList<Integer> emptyConfIndexList = new ArrayList<Integer>();
        for (int i = 0; i < confList.size(); ++i) {
            ConfigurationListItem conf = (ConfigurationListItem)confList.get(i);
            String aId = conf.getId();
            if (!aId.isEmpty()) continue;
            emptyConfIndexList.add(i);
        }
        if (emptyConfIndexList.size() == 0) {
            throw new IllegalArgumentException("Configuration list does not contain any item with empty ID: " + confList.toString());
        }
        int actualIndex = relativeIndex % emptyConfIndexList.size();
        return (Integer)emptyConfIndexList.get(actualIndex);
    }

    public static <C extends ConfigurationListItem> int countAnonymousConfigurations(List<C> confList) {
        if (confList == null) {
            throw new IllegalArgumentException("Configuration list is null");
        }
        ArrayList<Integer> emptyConfIndexList = new ArrayList<Integer>();
        for (int i = 0; i < confList.size(); ++i) {
            ConfigurationListItem conf = (ConfigurationListItem)confList.get(i);
            String aId = conf.getId();
            if (!aId.isEmpty()) continue;
            emptyConfIndexList.add(i);
        }
        return emptyConfIndexList.size();
    }
}

