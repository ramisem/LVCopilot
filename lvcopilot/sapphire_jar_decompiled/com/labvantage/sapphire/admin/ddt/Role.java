/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.services.RequestService;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Role
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String roleid = primary.getString(i, "roleid");
            if (!this.checkExist(roleid)) continue;
            this.throwError("CheckExists", "VALIDATION", "|Role Id '" + roleid + "' already exists. Please choose a different Id.|");
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        RequestService.flushInactiveRols(this.getDatabaseid());
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.executePreparedUpdate("UPDATE approvaltypestep SET roleid = NULL WHERE roleid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", new Object[]{rsetid});
    }

    private boolean checkExist(String roleid) throws SapphireException {
        this.database.createPreparedResultSet("SELECT\troleid FROM\trole WHERE\tLower( roleid ) = ?", new Object[]{roleid.toLowerCase()});
        return this.database.getNext();
    }
}

