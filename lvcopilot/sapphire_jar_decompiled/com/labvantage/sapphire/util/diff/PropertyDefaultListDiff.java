/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.BaseDiff;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import java.util.ArrayList;
import java.util.Set;

public class PropertyDefaultListDiff
extends BaseDiff {
    int curLevel = 0;
    ArrayList sourceHierarchy = new ArrayList();
    ArrayList targetHierarchy = new ArrayList();
    public static final String PROPERTY_DEFAULT = "Property";
    public static final String PROPERTY_DEFAULT_ATTR = "Attrbute";

    PropertyDefaultListDiff() {
    }

    private String getSourceHierarchyPrefix(int curLevel) {
        String prefix = "(ROOT)";
        for (int i = 0; i < curLevel; ++i) {
            prefix = prefix + "/" + this.sourceHierarchy.get(i);
        }
        return prefix;
    }

    private String getTargetHierarchyPrefix(int curLevel) {
        String prefix = "(ROOT)";
        for (int i = 0; i < curLevel; ++i) {
            prefix = prefix + "/" + this.targetHierarchy.get(i);
        }
        return prefix;
    }

    public PropertyDefaultListDiff(PropertyDefaultList source, PropertyDefaultList target) {
        this();
        this.comparePropertyDefaultLists(source, target, this.curLevel);
    }

    void comparePropertyDefaultLists(PropertyDefaultList sourceDefaultList, PropertyDefaultList targetDefaultList, int curLevel) {
        Set keyes;
        if (sourceDefaultList == null && targetDefaultList == null) {
            return;
        }
        String sourcePrefix = this.getSourceHierarchyPrefix(curLevel);
        String targetPrefix = this.getTargetHierarchyPrefix(curLevel);
        if (sourceDefaultList != null) {
            keyes = sourceDefaultList.keySet();
            for (String key : keyes) {
                PropertyDefault originalPropertyDefault = sourceDefaultList.getPropertyDefault(key);
                this.sourceHierarchy.add(curLevel, originalPropertyDefault.getId());
                PropertyDefault modifiedPropertyDefault = null;
                if (targetDefaultList != null) {
                    modifiedPropertyDefault = targetDefaultList.getPropertyDefault(key);
                }
                if (modifiedPropertyDefault == null) {
                    this.addDiffResult(PROPERTY_DEFAULT, sourcePrefix + "/" + key, "Deleted", "", "", "PropertyDefault deleted (" + key + ")");
                    continue;
                }
                this.targetHierarchy.add(curLevel, modifiedPropertyDefault.getId());
                this.comparePropertyDefaults(originalPropertyDefault, modifiedPropertyDefault, ++curLevel);
                --curLevel;
            }
        }
        if (targetDefaultList != null) {
            keyes = targetDefaultList.keySet();
            for (String key : keyes) {
                PropertyDefault sourcePropertyDefault = null;
                if (sourceDefaultList != null) {
                    sourcePropertyDefault = sourceDefaultList.getPropertyDefault(key);
                }
                if (sourcePropertyDefault != null) continue;
                this.addDiffResult(PROPERTY_DEFAULT, targetPrefix + "/" + key, "Added", "", key, "PropertyDefault added (" + key + ")");
            }
        }
    }

    void comparePropertyDefaults(PropertyDefault source, PropertyDefault target, int curLevel) {
        String sourcePrefix = this.getSourceHierarchyPrefix(curLevel);
        String targetPrefix = this.getTargetHierarchyPrefix(curLevel);
        if (source.getType() == null && target.getType() != null) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Added", source.getType(), target.getType(), "Type added");
        } else if (source.getType() != null && target.getType() == null) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Deleted", source.getType(), target.getType(), "Type deleted");
        } else if (!source.getType().equals(target.getType())) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Changed", source.getType(), target.getType(), "Type changed");
        }
        if (!source.getTranslate().equals(target.getTranslate())) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Changed", source.getTranslate() + "", target.getTranslate() + "", "Translate changed");
        }
        if (source.getValue() == null && target.getValue() != null) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, targetPrefix, "Added", source.getValue(), target.getValue(), "Value added");
        } else if (source.getValue() != null && target.getValue() == null) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Deleted", source.getValue(), target.getValue(), "Value deleted");
        } else if (source.getValue() != null && target.getValue() != null && !source.getValue().equals(target.getValue())) {
            this.addDiffResult(PROPERTY_DEFAULT_ATTR, sourcePrefix, "Changed", source.getValue(), target.getValue(), "Value changed");
        }
        this.comparePropertyDefaultLists(source.getPropertyDefaultList(), target.getPropertyDefaultList(), curLevel);
    }
}

