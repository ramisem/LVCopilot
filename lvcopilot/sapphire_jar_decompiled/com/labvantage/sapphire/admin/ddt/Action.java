/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Action
extends BaseSDCRules
implements CacheNames {
    static final String LABVANTAGE_CVS_ID = "$Revision: 69834 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String actiontype;
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        String string = actiontype = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "S" : "U";
        if (compcode.length() > 0) {
            actiontype = "S";
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            primary.setString(i, "actiontype", actiontype);
            if (primary.getValue(i, "actionlanguage") == null || primary.getValue(i, "actionlanguage").length() == 0) {
                primary.setString(i, "actionlanguage", "java");
            }
            primary.setString(i, "spmflag", "N");
            primary.setString(i, "parsingfunctionflag", "N");
            primary.setString(i, "coreflag", "N");
            primary.setString(i, "clientflag", "N");
            primary.setString(i, "clientobjectflag", "S");
            primary.setString(i, "synchronousflag", "Y");
            String actionid = primary.getValue(i, "actionid");
            this.clearActionCache(actionid);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.database.createPreparedResultSet("SELECT actionid, actiontype, compcode FROM action, rsetitems WHERE action.actionid = rsetitems.keyid1 AND action.actionversionid = rsetitems.keyid2 AND rsetid = ?", new Object[]{rsetid});
            while (this.database.getNext()) {
                String actionid = this.database.getString("actionid");
                if (compcode.length() == 0 && this.database.getString("actiontype") != null && (this.database.getString("actiontype").equals("S") || this.database.getString("actiontype").equals("C"))) {
                    throw new SapphireException("You cannot delete 'Core' or 'System' actions");
                }
                this.clearActionCache(actionid);
            }
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String actionid = primary.getValue(i, "actionid");
            this.clearActionCache(actionid);
        }
    }

    private void clearActionCache(String actionid) {
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "ActionPropertyList", actionid.toLowerCase());
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "ActionDefinition", actionid.toLowerCase());
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "ActionAttachments", actionid.toLowerCase());
    }
}

