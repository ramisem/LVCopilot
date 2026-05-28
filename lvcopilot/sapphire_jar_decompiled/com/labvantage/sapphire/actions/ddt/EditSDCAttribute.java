/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class EditSDCAttribute
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "";
    public static final String PROPERTY_ATTRIBUTEID = "attributeid";
    public static final String PROPERTY_ATTRIBUTEDEFID = "attributedefid";
    public static final String PROPERTY_BASEDONID = "basedonid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String attributeid;
        String sdcid = properties.getProperty("sdcid", properties.getProperty(PROPERTY_BASEDONID, LABVANTAGE_CVS_ID));
        if (sdcid.length() > 0) {
            attributeid = properties.getProperty(PROPERTY_ATTRIBUTEID, properties.getProperty(PROPERTY_ATTRIBUTEDEFID, LABVANTAGE_CVS_ID));
            if (attributeid.length() <= 0) {
                throw new SapphireException("No Attribute Id or Attributedef Id provided.");
            }
        } else {
            throw new SapphireException("No SDC Id provided or BasedOnId provided.");
        }
        properties.remove("sdcid");
        properties.remove("keyid1");
        properties.remove("keyid2");
        properties.remove("keyid3");
        properties.remove(PROPERTY_ATTRIBUTEID);
        properties.setProperty("sdcid", "LV_AttributeDef");
        properties.setProperty(PROPERTY_BASEDONID, sdcid);
        properties.setProperty(PROPERTY_ATTRIBUTEDEFID, attributeid);
        this.getActionProcessor().processAction("EditSDI", "1", properties, false);
    }
}

