/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Issue
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        primary.setString(-1, "issuestatus", "Initial");
        primary.setDate(-1, "submitdt", "");
        primary.setString(-1, "repositoryname", "");
        primary.setString(-1, "repositorynode", "");
        primary.setString(-1, "issueref", "");
    }
}

