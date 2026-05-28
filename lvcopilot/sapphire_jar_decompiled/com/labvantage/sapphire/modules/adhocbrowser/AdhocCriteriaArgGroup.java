/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArg;
import java.util.ArrayList;

public class AdhocCriteriaArgGroup
extends ArrayList {
    private String criteriaRelation = "";
    private String groupName = "";

    public String getCriteriaRelation() {
        return this.criteriaRelation;
    }

    public void setCriteriaRelation(String criteriaRelation) {
        this.criteriaRelation = criteriaRelation.indexOf("a") == 0 ? "and" : "or";
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void addCriteriaArg(AdhocCriteriaArg arg) {
        this.add(arg);
    }
}

