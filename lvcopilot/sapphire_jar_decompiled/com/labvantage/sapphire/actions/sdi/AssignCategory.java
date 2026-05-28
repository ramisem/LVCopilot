/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import sapphire.SapphireException;
import sapphire.action.AddCategoryItem;
import sapphire.action.BaseAction;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AssignCategory
extends BaseAction
implements AddCategoryItem {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String categoryid = properties.getProperty("categoryid");
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        if (categoryid.length() <= 0 || sdcid.length() <= 0) throw new SapphireException("No category and/or sdcid provided");
        SDIRequest request = new SDIRequest();
        request.setSDCid("Category");
        request.setRequestItem("primary");
        request.setQueryFrom("category");
        request.setQueryWhere("categoryid='" + SafeSQL.encodeForSQL(categoryid, this.database.isOracle()) + "'");
        SDIData sdi = this.getSDIProcessor().getSDIData(request);
        if (sdi == null || sdi.getDataset("primary") == null || sdi.getDataset("primary").getRowCount() == 0) {
            PropertyList addcatprops = new PropertyList();
            addcatprops.setProperty("sdcid", "Category");
            addcatprops.setProperty("keyid1", categoryid);
            addcatprops.setProperty("keyid2", sdcid);
            this.getActionProcessor().processAction("AddSDI", "1", addcatprops);
        }
        if (keyid1.length() <= 0) throw new SapphireException("No keyid1 provided");
        PropertyList additemprops = new PropertyList();
        additemprops.setProperty("propsmatch", "Y");
        String[] key1 = StringUtil.split(keyid1, ";");
        String[] key2 = keyid2.length() > 0 ? StringUtil.split(keyid2, ";") : null;
        String[] key3 = keyid3.length() > 0 ? StringUtil.split(keyid3, ";") : null;
        StringBuffer categoryidBuff = new StringBuffer();
        StringBuffer keyid1Buff = new StringBuffer();
        StringBuffer keyid2Buff = new StringBuffer();
        StringBuffer keyid3Buff = new StringBuffer();
        for (int i = 0; i < key1.length; ++i) {
            if (categoryidBuff.length() > 0) {
                categoryidBuff.append(";");
                keyid1Buff.append(";");
                if (key2 != null) {
                    keyid2Buff.append(";");
                    if (keyid3 != null) {
                        keyid3Buff.append(";");
                    }
                }
            }
            categoryidBuff.append(categoryid);
            keyid1Buff.append(key1[i]);
            if (key2 == null) continue;
            keyid2Buff.append(key2[i]);
            if (keyid3 == null) continue;
            keyid3Buff.append(key3[i]);
        }
        additemprops.setProperty("sdcid", sdcid);
        additemprops.setProperty("categoryid", categoryidBuff.toString());
        additemprops.setProperty("keyid1", keyid1Buff.toString());
        if (keyid2Buff.length() > 0) {
            additemprops.setProperty("keyid2", keyid2Buff.toString());
            if (keyid3Buff.length() > 0) {
                additemprops.setProperty("keyid3", keyid3Buff.toString());
            }
        }
        this.getActionProcessor().processAction("AddCategoryItem", "1", additemprops);
    }
}

