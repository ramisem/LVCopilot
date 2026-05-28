/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArgGroup;
import java.util.ArrayList;

public class AdhocCriteria
extends ArrayList {
    public static final String AND = "and";
    public static final String OR = "or";

    public void addCriteriaArgCroup(AdhocCriteriaArgGroup group) {
        this.add(group);
    }
}

