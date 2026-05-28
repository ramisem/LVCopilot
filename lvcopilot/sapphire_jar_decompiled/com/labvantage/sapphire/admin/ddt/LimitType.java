/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.xml.PropertyList;

public class LimitType
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String paramlistCheck = "SELECT paramlimits.paramlistid, paramlimits.paramlistversionid, paramlimits.variantid FROM   paramlimits, rsetitems WHERE  rsetitems.rsetid = ? AND    paramlimits.limittypeid = rsetitems.keyid1 ORDER BY 1, 2, 3";
        this.database.createPreparedResultSet(paramlistCheck, new Object[]{rsetid});
        StringBuffer paramlistRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            paramlistRefs.append("<br/>").append(this.database.getString("paramlistid")).append(": ").append(this.database.getString("paramlistversionid")).append(": ").append(this.database.getString("variantid"));
        }
        if (paramlistRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("LimitTypeUsed", "VALIDATION", "Limit Type(s) cannot be deleted because of " + (more ? "at least" : "") + " the following ParamList references:" + paramlistRefs + (more ? "<br/>..." : ""));
        }
    }
}

