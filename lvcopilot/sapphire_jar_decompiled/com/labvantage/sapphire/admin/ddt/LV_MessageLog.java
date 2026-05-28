/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DBUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_MessageLog
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DBUtil dbutil = (DBUtil)this.database;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String messageBody = primary.getClob(i, "messagebody");
            String propList = primary.getClob(i, "propertylist");
            String responseBody = primary.getClob(i, "responsebody");
            String messageLogId = primary.getString(i, "messagelogid");
            if (messageBody != null && messageBody.length() != 0) {
                dbutil.updateClob("messagelog", "messagebody", messageBody, new String[]{"messagelogid"}, new String[]{messageLogId});
            }
            if (propList != null && propList.length() != 0) {
                dbutil.updateClob("messagelog", "propertylist", propList, new String[]{"messagelogid"}, new String[]{messageLogId});
            }
            if (responseBody == null || responseBody.length() == 0) continue;
            dbutil.updateClob("messagelog", "responsebody", responseBody, new String[]{"messagelogid"}, new String[]{messageLogId});
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DBUtil dbutil = (DBUtil)this.database;
        String responsebodyList = propertyList.getProperty("responsebody");
        String messagebodyList = propertyList.getProperty("messagebody");
        String proplistList = propertyList.getProperty("propertylist");
        String[] messagebody = null;
        String[] responsebody = null;
        String[] proplist = null;
        if (messagebodyList != null && messagebodyList.length() > 0) {
            messagebody = StringUtil.split(messagebodyList, ";");
        }
        if (responsebodyList != null && responsebodyList.length() > 0) {
            responsebody = StringUtil.split(responsebodyList, ";");
        }
        if (proplistList != null && proplistList.length() > 0) {
            proplist = StringUtil.split(proplistList, ";");
        }
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String messageLogId = primary.getString(i, "messagelogid");
            if (messagebody != null && messagebody.length > i) {
                dbutil.updateClob("messagelog", "messagebody", messagebody[i], new String[]{"messagelogid"}, new String[]{messageLogId});
            }
            if (proplist != null && proplist.length > i) {
                dbutil.updateClob("messagelog", "propertylist", proplist[i], new String[]{"messagelogid"}, new String[]{messageLogId});
            }
            if (responsebody == null || responsebody.length <= i) continue;
            dbutil.updateClob("messagelog", "responsebody", responsebody[i], new String[]{"messagelogid"}, new String[]{messageLogId});
        }
    }
}

