/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import sapphire.SapphireException;
import sapphire.action.ActionConstants;
import sapphire.xml.PropertyList;

public class GetCalcReport
extends BaseSDIDataEntryAction
implements ActionConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        String paramid = properties.getProperty("paramid");
        String paramtype = properties.getProperty("paramtype");
        String replicateid = properties.getProperty("replicateid");
        PropertyList targetItems = new PropertyList();
        targetItems.setProperty("sdcid", sdcid);
        targetItems.setProperty("keyid1", keyid1);
        targetItems.setProperty("keyid2", keyid2);
        targetItems.setProperty("keyid3", keyid3);
        targetItems.setProperty("paramlistid", paramlistid);
        targetItems.setProperty("paramlistversionid", paramlistversionid);
        targetItems.setProperty("variantid", variantid);
        targetItems.setProperty("dataset", dataset);
        targetItems.setProperty("paramid", paramid);
        targetItems.setProperty("paramtype", paramtype);
        targetItems.setProperty("replicateid", replicateid);
        targetItems.setProperty("calcreportonly", "Y");
        try {
            this.dataEntry(targetItems, false, false, false);
            properties.setProperty("calcreport", targetItems.getProperty("calcreport"));
        }
        catch (Exception e) {
            this.logger.error("Unable to generate a calc report", e);
            properties.setProperty("calcreport", "<font color=red>An error occurred creating the calculation report.<br><br>See your administrator for more details</font>");
        }
    }
}

