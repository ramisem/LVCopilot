/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class DDTPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireConnection);
            for (String propertyid : props.keySet()) {
                CallableStatement cs;
                String callstmt;
                String tableid;
                if (propertyid.startsWith("sdcexport") && propertyid.endsWith("_exportscript") && propertyid.indexOf("[__row]") == -1) {
                    String row = propertyid.substring("sdcexport".length(), propertyid.lastIndexOf("_exportscript"));
                    String[] key = StringUtil.split((String)props.get("__sdcexport" + row + "_key"), ";");
                    String exportscript = (String)props.get("sdcexport" + row + "_exportscript");
                    dbu.updateClob("sdcexport", "exportscript", exportscript, new String[]{"sdcid", "exportid"}, new Object[]{key[0], key[1]});
                    continue;
                }
                if (propertyid.startsWith("syscolumnproperty")) {
                    String[] parts = StringUtil.split(propertyid, "__");
                    if (parts.length != 4 || dbu.executePreparedUpdate("UPDATE syscolumnproperty SET propertyvalue = ? WHERE tableid = ? AND columnid = ? AND propertyid = ?", new Object[]{(String)props.get(propertyid), parts[1], parts[2], parts[3]}) != 0) continue;
                    dbu.createPreparedResultSet("SELECT columnid FROM syscolumn WHERE tableid = ? AND columnid = ?", new Object[]{parts[1], parts[2]});
                    if (!dbu.getNext()) continue;
                    dbu.executePreparedUpdate("INSERT INTO syscolumnproperty ( tableid, columnid, propertyid, propertyvalue ) VALUES ( ?, ?, ?, ? )", new Object[]{parts[1], parts[2], parts[3], (String)props.get(propertyid)});
                    continue;
                }
                if (propertyid.startsWith("systable_")) {
                    if (propertyid.endsWith("_label")) {
                        tableid = propertyid.substring(9, propertyid.indexOf("_label"));
                        dbu.executePreparedUpdate("UPDATE systable SET tablelabel = ? WHERE tableid = ?", new Object[]{(String)props.get(propertyid), tableid});
                        continue;
                    }
                    if (propertyid.endsWith("_itemdisplay")) {
                        tableid = propertyid.substring(9, propertyid.indexOf("_itemdisplay"));
                        dbu.executePreparedUpdate("UPDATE systable SET itemdisplay = ? WHERE tableid = ?", new Object[]{(String)props.get(propertyid), tableid});
                        continue;
                    }
                    tableid = propertyid.substring(9);
                    dbu.executePreparedUpdate("UPDATE systable SET tabledoc = ? WHERE tableid = ?", new Object[]{(String)props.get(propertyid), tableid});
                    continue;
                }
                if (propertyid.equals("deletetablelist")) {
                    String tablelist = (String)props.get(propertyid);
                    if (tablelist.length() <= 0) continue;
                    String[] tables = StringUtil.split(tablelist, ";");
                    for (int i = 0; i < tables.length; ++i) {
                        callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "droptabledetail( ? ) }";
                        cs = dbu.prepareCall(callstmt);
                        cs.setString(1, tables[i]);
                        cs.executeUpdate();
                    }
                    continue;
                }
                if (propertyid.equals("deleteindexlist")) {
                    String indexlist = (String)props.get(propertyid);
                    if (indexlist.length() <= 0) continue;
                    String[] indexes = StringUtil.split(indexlist, ";");
                    for (int i = 0; i < indexes.length; ++i) {
                        callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "dropindex( ? ) }";
                        cs = dbu.prepareCall(callstmt);
                        cs.setString(1, indexes[i]);
                        cs.executeUpdate();
                    }
                    continue;
                }
                if (!propertyid.equals("addindextableid") || (tableid = (String)props.get(propertyid)).length() <= 0) continue;
                String columnids = (String)props.get("addindexcolumnids");
                String type = (String)props.get("addindextype");
                callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "adduserindexr( ?, ?, ?, ? ) }";
                cs = dbu.prepareCall(callstmt);
                cs.setString(1, tableid);
                cs.setString(2, columnids);
                cs.setString(3, type);
                cs.registerOutParameter(4, 12);
                cs.executeUpdate();
                String typeCreated = cs.getString(4);
                if (!type.equals("U") || !typeCreated.equals("I")) continue;
                this.getErrorHandler().add("", "", "Indexing", "INFORMATION", "Failed to create a unique index because of existing duplicate data - non-unique index added");
            }
            if (props.get("pr0_sdcid") != null) {
                CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to update sdc properties. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }
}

