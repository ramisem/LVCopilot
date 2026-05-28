/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.xml.PropertyList;

public class LV_Document
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String check = "SELECT sdidocument.sdcid, sdidocument.keyid1, sdidocument.keyid2, sdidocument.keyid3 FROM   sdidocument, rsetitems WHERE  rsetitems.rsetid = ? AND    sdidocument.documentid = rsetitems.keyid1 AND    sdidocument.documentversionid = rsetitems.keyid2 ORDER BY 1, 2, 3, 4";
        this.database.createPreparedResultSet(check, new Object[]{rsetid});
        StringBuffer refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            refs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("DocumentUsed", "VALIDATION", "Document(s) cannot be deleted because of " + (more ? "at least" : "") + " the following references:" + refs + (more ? "<br/>..." : ""));
        }
    }
}

