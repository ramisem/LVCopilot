/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ActionForTestWebAdminProcessor
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) {
        String dothis = properties.getProperty("dothis");
        String props = properties.getProperty("propertylist");
        String webpageid = properties.getProperty("webpageid");
        String productedition = properties.getProperty("productedition");
        String propertytreeid = properties.getProperty("propertytreeid");
        String elementid = properties.getProperty("elementid");
        WebAdminProcessor wp = new WebAdminProcessor(this.connectionInfo.getConnectionId());
        if (dothis.equals("reset")) {
            try {
                wp.clearUserOverrides(webpageid, productedition, propertytreeid, elementid);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                PropertyList pl = new PropertyList();
                if (props.length() > 0) {
                    pl.setPropertyList(props);
                }
                wp.saveUserOverrides(webpageid, productedition, propertytreeid, elementid, pl);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

