/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AutoLinkDocument
extends BaseAction
implements DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String documentid = properties.getProperty("documentid");
        String documentversionid = properties.getProperty("documentversionid");
        String[] fieldid = StringUtil.split(properties.getProperty("fieldid"), ";");
        String[] sdcid = StringUtil.split(properties.getProperty("sdcid"), ";");
        String[] sdckeycols = StringUtil.split(properties.getProperty("sdckeycols"), ";");
        String[] action = StringUtil.split(properties.getProperty("action"), ";");
        String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] oldkeyid1 = StringUtil.split(properties.getProperty("oldkeyid1"), ";");
        String[] oldkeyid2 = StringUtil.split(properties.getProperty("oldkeyid2"), ";");
        String[] oldkeyid3 = StringUtil.split(properties.getProperty("oldkeyid3"), ";");
        for (int i = 0; i < fieldid.length; ++i) {
            int keycols = Integer.parseInt(sdckeycols[i]);
            if (action[i].equals("U")) {
                this.logger.info("Updating autolink on field '" + fieldid[i] + "' with '" + sdcid[i]);
                if (this.database.executePreparedUpdate("UPDATE sdidocument SET keyid1 = ?, keyid2 = ?, keyid3 = ?, modby='" + this.connectionInfo.getSysuserId() + "', moddt = {ts '" + DateTimeUtil.getNowTimestamp() + "'} WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND documentid = ? AND documentversionid = ?", new Object[]{keyid1[i], keycols > 1 ? keyid2[i] : "(null)", keycols > 2 ? keyid3[i] : "(null)", sdcid[i], oldkeyid1[i], keycols > 1 ? oldkeyid2[i] : "(null)", keycols > 2 ? oldkeyid3[i] : "(null)", documentid, documentversionid}) == 1) continue;
                this.logger.info("Inserting autolink on field '" + fieldid[i] + "' with '" + sdcid[i]);
                this.database.executePreparedUpdate("INSERT INTO sdidocument ( sdcid, keyid1, keyid2, keyid3, documentid, documentversionid, createby, createdt ) VALUES ( ?, ?, ?, ?, ?, ? , '" + this.connectionInfo.getSysuserId() + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'})", new Object[]{sdcid[i], keyid1[i], keycols > 1 ? keyid2[i] : "(null)", keycols > 2 ? keyid3[i] : "(null)", documentid, documentversionid});
                continue;
            }
            if (action[i].equals("I")) {
                this.logger.info("Inserting autolink on field '" + fieldid[i] + "' with '" + sdcid[i]);
                this.database.executePreparedUpdate("INSERT INTO sdidocument ( sdcid, keyid1, keyid2, keyid3, documentid, documentversionid, createby, createdt ) VALUES ( ?, ?, ?, ?, ?, ? , '" + this.connectionInfo.getSysuserId() + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'})", new Object[]{sdcid[i], keyid1[i], keycols > 1 ? keyid2[i] : "(null)", keycols > 2 ? keyid3[i] : "(null)", documentid, documentversionid});
                continue;
            }
            if (!action[i].equals("D")) continue;
            this.logger.info("Removing autolink on field '" + fieldid[i] + "' with '" + sdcid[i]);
            this.database.executePreparedUpdate("DELETE FROM sdidocument WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND documentid = ? AND documentversionid = ?", new Object[]{sdcid[i], oldkeyid1[i], keycols > 1 ? oldkeyid2[i] : "(null)", keycols > 2 ? oldkeyid3[i] : "(null)", documentid, documentversionid});
        }
    }
}

