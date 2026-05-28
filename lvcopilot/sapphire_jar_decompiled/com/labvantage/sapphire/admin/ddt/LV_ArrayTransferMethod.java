/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_ArrayTransferMethod
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        PropertyList transferrule = new PropertyList();
        PropertyListCollection collection = new PropertyListCollection();
        transferrule.setProperty("contenttransferrule", collection);
        PropertyList fullarrayzone = new PropertyList();
        fullarrayzone.setProperty("zone", "(FullArray)");
        fullarrayzone.setProperty("definition", "true");
        fullarrayzone.setProperty("content", "true");
        fullarrayzone.setProperty("propagateunknown", "true");
        fullarrayzone.setProperty("propagateControl", "true");
        fullarrayzone.setProperty("propagatetransfer", "false");
        fullarrayzone.setProperty("propagatetreatment", "false");
        fullarrayzone.setProperty("propagateoperation", "false");
        fullarrayzone.setProperty("propagateMM", "false");
        collection.add(fullarrayzone);
        String transferRule = transferrule.toXMLString();
        PropertyList editProps = new PropertyList();
        editProps.setProperty("sdcid", "LV_ArrayTransferMethod");
        editProps.setProperty("keyid1", actionProps.getProperty("keyid1", ";"));
        editProps.setProperty("keyid2", actionProps.getProperty("keyid2", ";"));
        editProps.setProperty("contenttransferrule", transferRule);
        this.getActionProcessor().processAction("EditSDI", "1", editProps);
    }
}

