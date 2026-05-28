/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.finance;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.finance.BaseFinanceAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeletePriceListItem
extends BaseFinanceAction
implements sapphire.action.DeletePriceListItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            this.initRSet("PriceList", properties.getProperty("pricelistid"), properties.getProperty("pricelistitemid"), properties);
            this.deleteItem();
        }
        catch (SapphireException sapphireException) {
            throw new SapphireException("PROCESSACTION_FAILED", "Action failed : " + ErrorUtil.extractMessageFromException(sapphireException, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
        }
    }
}

