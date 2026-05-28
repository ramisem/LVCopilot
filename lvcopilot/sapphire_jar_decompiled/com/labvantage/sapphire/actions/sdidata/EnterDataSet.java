/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EnterDataSet
extends BaseSDIDataEntryAction
implements sapphire.action.EnterDataSet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int params;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        if (keyid1.length() == 0) {
            keyid1 = "(null)";
        }
        if (keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        StringBuffer keyid1list = new StringBuffer();
        StringBuffer keyid2list = new StringBuffer();
        StringBuffer keyid3list = new StringBuffer();
        StringBuffer paramlistidlist = new StringBuffer();
        StringBuffer paramlistversionidlist = new StringBuffer();
        StringBuffer variantidlist = new StringBuffer();
        StringBuffer datasetlist = new StringBuffer();
        StringBuffer paramidlist = new StringBuffer();
        StringBuffer paramtypelist = new StringBuffer();
        StringBuffer replicateidlist = new StringBuffer();
        StringBuffer enteredtextlist = new StringBuffer();
        try {
            params = Integer.parseInt(properties.getProperty("params"));
        }
        catch (NumberFormatException e) {
            throw new SapphireException("INVALID_PROPERTY", "Params property is an invalid number", e);
        }
        for (int param = 1; param <= params; ++param) {
            String sparam = String.valueOf(param);
            keyid1list.append(";").append(keyid1);
            keyid2list.append(";").append(keyid2);
            keyid3list.append(";").append(keyid3);
            paramlistidlist.append(";").append(paramlistid);
            paramlistversionidlist.append(";").append(paramlistversionid);
            variantidlist.append(";").append(variantid);
            datasetlist.append(";").append(dataset);
            paramidlist.append(";").append(properties.getProperty("paramid" + sparam));
            paramtypelist.append(";").append(properties.getProperty("paramtype" + sparam));
            replicateidlist.append(";").append(properties.getProperty("replicateid" + sparam));
            enteredtextlist.append(";").append(properties.getProperty("enteredtext" + sparam));
        }
        PropertyList newprops = new PropertyList();
        newprops.setProperty("sdcid", sdcid);
        newprops.setProperty("keyid1", keyid1list.substring(1));
        newprops.setProperty("keyid2", keyid2list.substring(1));
        newprops.setProperty("keyid3", keyid3list.substring(1));
        newprops.setProperty("paramlistid", paramlistidlist.substring(1));
        newprops.setProperty("paramlistversionid", paramlistversionidlist.substring(1));
        newprops.setProperty("variantid", variantidlist.substring(1));
        newprops.setProperty("dataset", datasetlist.substring(1));
        newprops.setProperty("paramid", paramidlist.substring(1));
        newprops.setProperty("paramtype", paramtypelist.substring(1));
        newprops.setProperty("replicateid", replicateidlist.substring(1));
        newprops.setProperty("enteredtext", enteredtextlist.substring(1));
        this.dataEntry(newprops, false, false);
    }
}

