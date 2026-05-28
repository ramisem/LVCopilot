/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditDataSet
extends BaseSDIDataAction
implements sapphire.action.EditDataSet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.editSDIData(properties, "sdidata");
        if (properties.getProperty("s_datasetstatus", "").contains("Released")) {
            ApprovalRuleUtil.checkDatasetAutoApprovalRule(properties, this.database, this.connectionInfo, this.logger, true);
        }
    }
}

