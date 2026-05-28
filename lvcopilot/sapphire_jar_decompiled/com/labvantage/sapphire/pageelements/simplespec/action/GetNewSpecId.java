/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.pageelements.simplespec.action.GetNewIdHelper;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class GetNewSpecId
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String RETURN_NEWSPECID = "newspecid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty(PROPERTY_SDCID);
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        GetNewIdHelper getNewIdHelper = new GetNewIdHelper(this.getConnectionId());
        String newSpecId = getNewIdHelper.getNewId(sdcId, keyId1, "spec", "specid");
        properties.setProperty(RETURN_NEWSPECID, newSpecId);
    }
}

