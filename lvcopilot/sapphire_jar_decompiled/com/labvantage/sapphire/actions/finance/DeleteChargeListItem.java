/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.finance;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.finance.BaseFinanceAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeleteChargeListItem
extends BaseFinanceAction
implements sapphire.action.DeleteChargeListItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            this.initRSet("ChargeList", properties.getProperty("chargelistid"), properties.getProperty("chargelistitemid"), properties);
            this.deleteItem();
        }
        catch (SapphireException sapphireException) {
            throw new SapphireException("PROCESSACTION_FAILED", "Action failed : " + ErrorUtil.extractMessageFromException(sapphireException, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
        }
    }
}

