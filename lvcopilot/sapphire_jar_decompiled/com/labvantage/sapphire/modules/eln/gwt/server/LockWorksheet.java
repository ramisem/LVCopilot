/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class LockWorksheet
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] rsetdetails = this.lockRSet(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), properties.getProperty("rsetid"));
        properties.setProperty("rsetid", rsetdetails[0]);
        if (rsetdetails.length > 1) {
            properties.setProperty("lockedby", rsetdetails[1]);
        }
        if (rsetdetails.length > 2) {
            properties.setProperty("checkedoutbyuserid", rsetdetails[2]);
        }
        if (rsetdetails.length > 3) {
            properties.setProperty("checkedoutbydepartmentid", rsetdetails[3]);
        }
    }
}

