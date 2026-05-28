/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDCLink
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77332 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("linkid", 0, properties.getProperty("linkid"), ";");
        try {
            for (int i = 0; i < values.size(); ++i) {
                CallableStatement cs;
                String callstmt;
                String sdcid = values.getString(i, "sdcid");
                String[] linkkey = StringUtil.split(values.getString(i, "linkid"), "+");
                String linkid = linkkey[0];
                if (linkkey.length == 1) {
                    this.database.createPreparedResultSet("SELECT sdclink.linktype, sdclink.sdccolumnid, sdclink.linksdcid, sdclink.linktableid, sdc.tableid FROM sdc, sdclink WHERE sdc.sdcid = sdclink.sdcid AND sdc.sdcid = ? AND sdclink.linkid = ?", new Object[]{sdcid, linkid});
                    if (this.database.getNext()) {
                        String linktype = this.database.getString("linktype");
                        String sdctableid = this.database.getString("tableid");
                        String sdccolumnid = this.database.getString("sdccolumnid");
                        String linksdcid = this.database.getString("linksdcid");
                        String linktableid = this.database.getString("linktableid");
                        this.database.executePreparedUpdate("DELETE FROM sdclink WHERE sdcid = ? AND linkid = ?", new Object[]{sdcid, linkid});
                        if (linktype.equals("F") && this.connectionInfo.isOracle()) {
                            this.database.createPreparedResultSet("SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{linksdcid});
                            if (!this.database.getNext()) continue;
                            callstmt = "{call lv_clean.dropfkwithclear( ?, ?, ? ) }";
                            cs = this.database.prepareCall(callstmt);
                            cs.setString(1, sdctableid);
                            cs.setString(2, sdccolumnid);
                            cs.setString(3, this.database.getString("tableid"));
                            cs.executeUpdate();
                            continue;
                        }
                        if (!linktype.equals("M")) continue;
                        if (this.database.getCount("SELECT count(*) FROM " + linktableid) == 0) {
                            this.database.executeUpdate("DROP TABLE " + linktableid);
                            continue;
                        }
                        throw new SapphireException("Failed to delete many to many link '" + linkid + "' because data exists in the link table '" + linktableid + "'");
                    }
                    throw new SapphireException("Failed to find link '" + linkid + "' for sdc '" + sdcid + "'");
                }
                String detaillinkid = linkkey[1];
                this.database.createPreparedResultSet("SELECT sdcdetaillink.linktype, sdcdetaillink.sdccolumnid, sdcdetaillink.linksdcid, sdcdetaillink.linktableid FROM sdcdetaillink WHERE sdcdetaillink.sdcid = ? AND sdcdetaillink.linkid = ? AND sdcdetaillink.detaillinkid = ?", new Object[]{sdcid, linkid, detaillinkid});
                if (this.database.getNext()) {
                    String linktype = this.database.getString("linktype");
                    String tableid = this.database.getString("linktableid");
                    String sdccolumnid = this.database.getString("sdccolumnid");
                    String linksdcid = this.database.getString("linksdcid");
                    this.database.executePreparedUpdate("DELETE FROM sdcdetaillink WHERE sdcid = ? AND linkid = ? AND detaillinkid = ?", new Object[]{sdcid, linkid, detaillinkid});
                    if (!linktype.equals("F")) continue;
                    this.database.createPreparedResultSet("SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{linksdcid});
                    if (!this.database.getNext() || !this.connectionInfo.isOracle()) continue;
                    callstmt = "{call lv_clean.dropfkwithclear( ?, ?, ? ) }";
                    cs = this.database.prepareCall(callstmt);
                    cs.setString(1, tableid);
                    cs.setString(2, sdccolumnid);
                    cs.setString(3, this.database.getString("tableid"));
                    cs.executeUpdate();
                    continue;
                }
                throw new SapphireException("Failed to find link '" + linkid + "/" + detaillinkid + "' for sdc '" + sdcid + "'");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to delete links '" + properties.getProperty("linkid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

