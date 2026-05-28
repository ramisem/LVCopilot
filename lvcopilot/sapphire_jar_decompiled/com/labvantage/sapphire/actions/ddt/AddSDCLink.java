/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSDCLink
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77311 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid;
        CallableStatement cs;
        String callstmt;
        String linktype;
        CallableStatement cs2;
        String callstmt2;
        String linkid;
        String sdcid2;
        boolean sdcdetaillink;
        int i;
        ConfigService config = new ConfigService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        String type = config.getDevModeProperty("sdc.type", "U");
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        DataSet values = new DataSet();
        DataSet sdcdetaillinks = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("linkid", 0, properties.getProperty("linkid"), ";");
        values.addColumnValues("__detaillinkid", 0, properties.getProperty("detaillinkid"), ";");
        values.addColumnValues("__tableid", 0, properties.getProperty("tableid"), ";");
        values.addColumnValues("linktype", 0, properties.getProperty("linktype"), ";");
        values.addColumnValues("linksdcid", 0, properties.getProperty("linksdcid"), ";");
        values.addColumnValues("linksdccolumnid", 0, properties.getProperty("linksdccolumnid"), ";");
        values.addColumnValues("linksequence", 1, properties.getProperty("linksequence"), ";");
        values.addColumnValues("sdccolumnid", 0, properties.getProperty("sdccolumnid"), ";");
        values.addColumnValues("sdccolumnid2", 0, properties.getProperty("sdccolumnid2"), ";");
        values.addColumnValues("sdccolumnid3", 0, properties.getProperty("sdccolumnid3"), ";");
        values.addColumnValues("linktableid", 0, properties.getProperty("linktableid"), ";");
        values.addColumnValues("__parenttableid", 0, properties.getProperty("parenttableid"), ";");
        values.addColumnValues("reftypeid", 0, properties.getProperty("reftypeid"), ";");
        values.addColumnValues("loadflag", 0, properties.getProperty("loadflag"), ";");
        values.addColumnValues("userflag", 0, properties.getProperty("userflag"), ";");
        values.addColumnValues("deleteflag", 0, properties.getProperty("deleteflag"), ";");
        for (i = 0; i < values.size(); ++i) {
            boolean bl = sdcdetaillink = values.getString(i, "__detaillinkid") != null && !values.getString(i, "__detaillinkid").equals("(null)") && values.getString(i, "__detaillinkid").length() > 0;
            if (values.getString(i, "deleteflag") == null || values.getString(i, "deleteflag").equals("(null)") || values.getString(i, "deleteflag").length() > 1) {
                values.setString(i, "deleteflag", "");
            }
            if (values.getString(i, "reftypeid") == null || values.getString(i, "reftypeid").equals("(null)")) {
                values.setString(i, "reftypeid", "");
            }
            if (values.getString(i, "linktableid") == null || values.getString(i, "linktableid").equals("(null)")) {
                values.setString(i, "linktableid", "");
            }
            if (values.getString(i, "sdccolumnid") == null || values.getString(i, "sdccolumnid").equals("(null)")) {
                values.setString(i, "sdccolumnid", "");
            }
            if (values.getString(i, "sdccolumnid2") == null || values.getString(i, "sdccolumnid2").equals("(null)")) {
                values.setString(i, "sdccolumnid2", "");
            }
            if (values.getString(i, "sdccolumnid3") == null || values.getString(i, "sdccolumnid3").equals("(null)")) {
                values.setString(i, "sdccolumnid3", "");
            }
            sdcid2 = values.getString(i, "sdcid");
            linkid = values.getString(i, "linkid");
            String detaillinkid = values.getString(i, "__detaillinkid");
            String linktype2 = values.getString(i, "linktype");
            String loadflag = values.getString(i, "loadflag");
            String linksdcid = values.getString(i, "linksdcid");
            this.database.createPreparedResultSet("SELECT sdcid, sdctype FROM sdc WHERE sdcid = ?", new Object[]{sdcid2});
            if (!this.database.getNext()) {
                throw new SapphireException("Invalid link data. SDC = '" + sdcid2 + "'");
            }
            String sdctype = this.database.getString("sdctype");
            if (linkid == null || linkid.length() == 0 || linkid.length() > 80) {
                throw new SapphireException("Invalid link data, Link name = '" + linkid + "'");
            }
            if (sdcdetaillink) {
                if (detaillinkid == null || detaillinkid.length() == 0 || detaillinkid.length() > 80) {
                    throw new SapphireException("Invalid detail link data, Detail Link name = '" + detaillinkid + "'");
                }
                this.database.createPreparedResultSet("SELECT detaillinkid FROM sdcdetaillink WHERE sdcid = ? AND linkid = ? AND detaillinkid = ?", new Object[]{sdcid2, linkid, detaillinkid});
            } else {
                this.database.createPreparedResultSet("SELECT linkid FROM sdclink WHERE sdcid = ? AND linkid = ?", new Object[]{sdcid2, linkid});
            }
            if (this.database.getNext()) {
                values.deleteRow(i);
            } else {
                if (loadflag == null || !loadflag.equals("Y") && !loadflag.equals("N")) {
                    throw new SapphireException("Invalid link data. Load flag = '" + loadflag + "'");
                }
                if (linktype2 == null || linktype2.length() == 0 || linktype2.length() > 1) {
                    throw new SapphireException("Invalid link data. Link type = '" + linktype2 + "'");
                }
                if (linktype2.equals("F")) {
                    this.isLinkableSDC(linksdcid);
                    this.isValidColumn(sdcid2, values.getString(i, "sdccolumnid"), false);
                    this.isValidColumn(sdcid2, values.getString(i, "sdccolumnid2"), true);
                    this.isValidColumn(sdcid2, values.getString(i, "sdccolumnid3"), true);
                    values.setString(i, "linksdccolumnid", "");
                    values.setString(i, "linktableid", sdcdetaillink ? values.getString(i, "__tableid") : "");
                    values.setString(i, "reftypeid", "");
                    values.setString(i, "loadflag", "N");
                } else if (linktype2.equals("M")) {
                    this.isLinkableSDC(linksdcid);
                    values.setString(i, "linksdccolumnid", "");
                    values.setString(i, "reftypeid", "");
                    values.setString(i, "loadflag", "Y");
                } else if (linktype2.equals("V") || linktype2.equals("R")) {
                    values.setString(i, "linksdcid", "RefType");
                    values.setString(i, "loadflag", "N");
                    values.setString(i, "linksdccolumnid", "");
                    values.setString(i, "linktableid", sdcdetaillink ? values.getString(i, "__tableid") : "");
                    this.isValidColumn(sdcid2, values.getString(i, "sdccolumnid"), false);
                    String reftypeid = values.getString(i, "reftypeid");
                    this.database.createPreparedResultSet("SELECT reftypeid FROM reftype WHERE reftypeid = ?", new Object[]{reftypeid});
                    if (!this.database.getNext()) {
                        throw new SapphireException("Invalid link data. Reftype = '" + reftypeid + "'");
                    }
                } else if (linktype2.equals("D")) {
                    values.setString(i, "loadflag", "Y");
                    try {
                        JSONObject tableDef = new JSONObject(values.getValue(i, "linktableid"));
                        String tableid = tableDef.getString("tableid").toLowerCase();
                        if (sdctype.equals("U") && !tableid.startsWith("u_")) {
                            tableid = "u_" + tableid;
                        }
                        values.setString(i, "linksdcid", "");
                        values.setString(i, "linktableid", tableid);
                        values.setString(i, "__linktablekeycount", tableDef.getString("keycount"));
                        values.setString(i, "__linktablekeyids", tableDef.getString("keyids"));
                        values.setString(i, "__linktabledatatypes", tableDef.getString("datatypes"));
                        values.setString(i, "__linktablecolumnlengths", tableDef.getString("columnlengths"));
                    }
                    catch (JSONException e) {
                        throw new SapphireException("Invalid table definition for detail table. Link id = '" + linkid + "'", e);
                    }
                }
                if (compcode.length() > 0) {
                    values.setString(i, "compcode", compcode);
                }
            }
            if (!sdcdetaillink) continue;
            int newrow = sdcdetaillinks.addRow();
            sdcdetaillinks.setString(newrow, "sdcid", sdcid2);
            sdcdetaillinks.setString(newrow, "linkid", linkid);
            sdcdetaillinks.setString(newrow, "detaillinkid", detaillinkid);
            sdcdetaillinks.setString(newrow, "linktype", linktype2);
            sdcdetaillinks.setString(newrow, "linksdcid", values.getString(i, "linksdcid"));
            sdcdetaillinks.setString(newrow, "reftypeid", values.getString(i, "reftypeid"));
            sdcdetaillinks.setString(newrow, "sdccolumnid", values.getString(i, "sdccolumnid"));
            sdcdetaillinks.setString(newrow, "sdccolumnid2", values.getString(i, "sdccolumnid2"));
            sdcdetaillinks.setString(newrow, "sdccolumnid3", values.getString(i, "sdccolumnid3"));
            sdcdetaillinks.setNumber(newrow, "linksequence", values.getInt(i, "linksequence"));
            sdcdetaillinks.setString(newrow, "userflag", values.getString(i, "userflag"));
            sdcdetaillinks.setString(newrow, "deleteflag", values.getString(i, "deleteflag"));
            sdcdetaillinks.setString(newrow, "parenttableid", values.getString(i, linktype2.equals("D") ? "__parenttableid" : ""));
            sdcdetaillinks.setString(newrow, "linktableid", values.getString(i, "linktableid"));
            sdcdetaillinks.setString(newrow, "__linktablekeycount", values.getString(i, "__linktablekeycount"));
            sdcdetaillinks.setString(newrow, "__linktablekeyids", values.getString(i, "__linktablekeyids"));
            sdcdetaillinks.setString(newrow, "__linktabledatatypes", values.getString(i, "__linktabledatatypes"));
            sdcdetaillinks.setString(newrow, "__linktablecolumnlengths", values.getString(i, "__linktablecolumnlengths"));
        }
        for (i = values.size() - 1; i >= 0; --i) {
            boolean bl = sdcdetaillink = values.getString(i, "__detaillinkid") != null && !values.getString(i, "__detaillinkid").equals("(null)") && values.getString(i, "__detaillinkid").length() > 0;
            if (!sdcdetaillink) continue;
            values.deleteRow(i);
        }
        boolean newtable = false;
        int sdclinkcol = 0;
        try {
            DataSetUtil.insert(this.database, values, "sdclink");
            for (sdclinkcol = 0; sdclinkcol < values.size(); ++sdclinkcol) {
                sdcid2 = values.getString(sdclinkcol, "sdcid");
                linkid = values.getString(sdclinkcol, "linkid");
                String linktype3 = values.getString(sdclinkcol, "linktype");
                if (linktype3.equals("F") || linktype3.equals("M")) {
                    callstmt2 = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addusersdclink  ( ?, ? ) }";
                    cs2 = this.database.prepareCall(callstmt2);
                    cs2.setString(1, sdcid2);
                    cs2.setString(2, linkid);
                    cs2.executeUpdate();
                    continue;
                }
                if (!linktype3.equals("D")) continue;
                callstmt2 = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addsdcdetail1  ( ?, ?, ?, ?, ?, ? ) }";
                cs2 = this.database.prepareCall(callstmt2);
                cs2.setString(1, sdcid2);
                cs2.setString(2, linkid);
                cs2.setString(3, values.getValue(sdclinkcol, "__linktablekeyids"));
                cs2.setString(4, values.getValue(sdclinkcol, "__linktabledatatypes"));
                cs2.setString(5, values.getValue(sdclinkcol, "__linktablecolumnlengths"));
                cs2.setString(6, type.equals("U") ? "N" : "Y");
                cs2.executeUpdate();
                newtable = true;
            }
        }
        catch (Exception e) {
            for (int i2 = 0; i2 < sdclinkcol; ++i2) {
                try {
                    String sdcid3 = values.getString(i2, "sdcid");
                    String linkid2 = values.getString(i2, "linkid");
                    linktype = values.getString(i2, "linktype");
                    this.database.executePreparedUpdate("DELETE FROM sdclink WHERE sdcid = ? AND linkid = ?", new Object[]{sdcid3, linkid2});
                    if (linktype.equals("F")) {
                        this.database.createPreparedResultSet("SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{values.getString(i2, "linksdcid")});
                        if (!this.database.getNext()) continue;
                        callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "dropfkwithclear( ?, ?, ? ) }";
                        cs = this.database.prepareCall(callstmt);
                        cs.setString(1, values.getString(i2, "linktableid"));
                        cs.setString(2, values.getString(i2, "sdccolumnid"));
                        cs.setString(3, this.database.getString("tableid"));
                        cs.executeUpdate();
                        continue;
                    }
                    if (!linktype.equals("M")) continue;
                    String linktableid = values.getString(i2, "linktableid");
                    String callstmt3 = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "droptablewithclear( ? ) }";
                    CallableStatement cs3 = this.database.prepareCall(callstmt3);
                    cs3.setString(1, linktableid);
                    cs3.executeUpdate();
                    continue;
                }
                catch (SQLException sdcid3) {
                    // empty catch block
                }
            }
            throw new SapphireException("Failed to add new links '" + properties.getProperty("linkid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
        }
        int sdcdetaillinkcol = 0;
        try {
            DataSetUtil.insert(this.database, sdcdetaillinks, "sdcdetaillink");
            for (sdcdetaillinkcol = 0; sdcdetaillinkcol < sdcdetaillinks.size(); ++sdcdetaillinkcol) {
                sdcid = sdcdetaillinks.getString(sdcdetaillinkcol, "sdcid");
                String linkid3 = sdcdetaillinks.getString(sdcdetaillinkcol, "linkid");
                String detaillinkid = sdcdetaillinks.getString(sdcdetaillinkcol, "detaillinkid");
                linktype = sdcdetaillinks.getString(sdcdetaillinkcol, "linktype");
                if (linktype.equals("F") || linktype.equals("M")) {
                    callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addsdcdetailfk  ( ?, ?, ? ) }";
                    cs = this.database.prepareCall(callstmt);
                    cs.setString(1, sdcid);
                    cs.setString(2, linkid3);
                    cs.setString(3, detaillinkid);
                    cs.executeUpdate();
                    continue;
                }
                if (!linktype.equals("D")) continue;
                callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addsdcdetail2n  ( ?, ?, ?, ?, ?, ?, ? ) }";
                cs = this.database.prepareCall(callstmt);
                cs.setString(1, sdcid);
                cs.setString(2, linkid3);
                cs.setString(3, detaillinkid);
                cs.setString(4, sdcdetaillinks.getValue(sdcdetaillinkcol, "__linktablekeyids"));
                cs.setString(5, sdcdetaillinks.getValue(sdcdetaillinkcol, "__linktabledatatypes"));
                cs.setString(6, sdcdetaillinks.getValue(sdcdetaillinkcol, "__linktablecolumnlengths"));
                cs.setString(7, type.equals("U") ? "N" : "Y");
                cs.executeUpdate();
                newtable = true;
            }
        }
        catch (Exception e) {
            for (int i3 = 0; i3 < sdcdetaillinkcol; ++i3) {
                try {
                    String sdcid4 = sdcdetaillinks.getString(i3, "sdcid");
                    String linkid4 = sdcdetaillinks.getString(i3, "linkid");
                    String detaillinkid = sdcdetaillinks.getString(i3, "detaillinkid");
                    String linktype4 = sdcdetaillinks.getString(i3, "linktype");
                    this.database.executePreparedUpdate("DELETE FROM sdcdetaillink WHERE sdcid = ? AND linkid = ? AND detaillink = ?", new Object[]{sdcid4, linkid4, detaillinkid});
                    if (linktype4.equals("F")) {
                        this.database.createPreparedResultSet("SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{sdcdetaillinks.getString(i3, "linksdcid")});
                        if (!this.database.getNext()) continue;
                        String callstmt4 = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "dropfkwithclear( ?, ?, ? ) }";
                        CallableStatement cs4 = this.database.prepareCall(callstmt4);
                        cs4.setString(1, sdcdetaillinks.getString(i3, "linktableid"));
                        cs4.setString(2, sdcdetaillinks.getString(i3, "sdccolumnid"));
                        cs4.setString(3, this.database.getString("tableid"));
                        cs4.executeUpdate();
                        continue;
                    }
                    if (!linktype4.equals("M")) continue;
                    String linktableid = sdcdetaillinks.getString(i3, "linktableid");
                    String callstmt5 = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "droptablewithclear( ? ) }";
                    CallableStatement cs5 = this.database.prepareCall(callstmt5);
                    cs5.setString(1, linktableid);
                    cs5.executeUpdate();
                    continue;
                }
                catch (SQLException sdcid4) {
                    // empty catch block
                }
            }
            throw new SapphireException("Failed to add new detail links '" + properties.getProperty("detaillinkid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
        }
        if (newtable) {
            sdcid = values.getString(0, "sdcid");
            try {
                SDCProcessor sdcProc = this.getSDCProcessor();
                if (!sdcProc.getProperty(sdcid, "auditedflag").equals("N")) {
                    callstmt2 = "{call lv_audit" + (this.connectionInfo.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                    cs2 = this.database.prepareCall(callstmt2);
                    cs2.setString(1, sdcid);
                    cs2.setString(2, "Both");
                    cs2.executeUpdate();
                } else {
                    callstmt2 = "{call lv_audit" + (this.connectionInfo.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                    cs2 = this.database.prepareCall(callstmt2);
                    cs2.setString(1, sdcid);
                    cs2.setString(2, "Off");
                    cs2.executeUpdate();
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to setup auditing options. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    private void isLinkableSDC(String sdcid) throws SapphireException {
        this.database.createPreparedResultSet("SELECT sdcid FROM sdc WHERE sdcid = ? AND linkableflag = 'Y'", new Object[]{sdcid});
        if (!this.database.getNext()) {
            throw new SapphireException("Invalid link data. Linked SDC = '" + sdcid + "'");
        }
    }

    private void isValidColumn(String sdcid, String columnid, boolean allowNull) throws SapphireException {
        if (allowNull && (columnid == null || columnid.equals("(null)") || columnid.length() == 0)) {
            return;
        }
        this.database.createPreparedResultSet("SELECT syscolumn.columnid FROM syscolumn, sdc WHERE sdc.tableid = syscolumn.tableid AND sdc.sdcid = ? AND syscolumn.columnid = ? UNION SELECT syscolumn.columnid FROM syscolumn, sdclink WHERE sdclink.linktableid = syscolumn.tableid AND sdclink.linktype IN ( 'D', 'M' ) AND sdclink.sdcid = ? AND syscolumn.columnid = ? UNION SELECT syscolumn.columnid FROM syscolumn, sdcdetaillink WHERE sdcdetaillink.linktableid = syscolumn.tableid AND sdcdetaillink.linktype = 'D' AND sdcdetaillink.sdcid = ? AND syscolumn.columnid = ?", new Object[]{sdcid, columnid, sdcid, columnid, sdcid, columnid});
        if (!this.database.getNext()) {
            throw new SapphireException("Invalid link data. Column = '" + columnid + "'");
        }
    }
}

