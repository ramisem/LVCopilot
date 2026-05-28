/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteWorksheetSDI
extends BaseELNAction
implements sapphire.action.DeleteWorksheetSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"))) {
            String sdcid = properties.getProperty("sdcid");
            if (properties.getProperty("deleteunassigned", "N").equals("Y")) {
                if (this.database.isOracle()) {
                    Object[] objectArray;
                    String string = "DELETE FROM worksheetsdi WHERE (worksheetid, worksheetversionid, sdcid, keyid1, keyid2, keyid3) NOT IN (SELECT worksheetitem.worksheetid, worksheetitem.worksheetversionid, worksheetitemsdi.sdcid, worksheetitemsdi.keyid1, worksheetitemsdi.keyid2, worksheetitemsdi.keyid3 FROM   worksheetitemsdi, worksheetitem WHERE  worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid   AND  worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?) AND worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? " + (sdcid.length() > 0 ? " AND worksheetsdi.sdcid = ?" : "");
                    if (sdcid.length() > 0) {
                        Object[] objectArray2 = new Object[5];
                        objectArray2[0] = worksheetid;
                        objectArray2[1] = worksheetversionid;
                        objectArray2[2] = worksheetid;
                        objectArray2[3] = worksheetversionid;
                        objectArray = objectArray2;
                        objectArray2[4] = sdcid;
                    } else {
                        Object[] objectArray3 = new Object[4];
                        objectArray3[0] = worksheetid;
                        objectArray3[1] = worksheetversionid;
                        objectArray3[2] = worksheetid;
                        objectArray = objectArray3;
                        objectArray3[3] = worksheetversionid;
                    }
                    this.database.executePreparedUpdate(string, objectArray);
                } else {
                    Object[] objectArray;
                    String string = "DELETE FROM worksheetsdi WHERE (worksheetid + ';' + worksheetversionid + ';' + sdcid + ';' + keyid1 + ';' + keyid2 + ';' + keyid3) NOT IN (SELECT worksheetitem.worksheetid + ';' + worksheetitem.worksheetversionid + ';' + worksheetitemsdi.sdcid + ';' + worksheetitemsdi.keyid1 + ';' + worksheetitemsdi.keyid2 + ';' + worksheetitemsdi.keyid3 FROM   worksheetitemsdi, worksheetitem WHERE  worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid   AND  worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?) AND worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? " + (sdcid.length() > 0 ? " AND worksheetsdi.sdcid = ?" : "");
                    if (sdcid.length() > 0) {
                        Object[] objectArray4 = new Object[5];
                        objectArray4[0] = worksheetid;
                        objectArray4[1] = worksheetversionid;
                        objectArray4[2] = worksheetid;
                        objectArray4[3] = worksheetversionid;
                        objectArray = objectArray4;
                        objectArray4[4] = sdcid;
                    } else {
                        Object[] objectArray5 = new Object[4];
                        objectArray5[0] = worksheetid;
                        objectArray5[1] = worksheetversionid;
                        objectArray5[2] = worksheetid;
                        objectArray = objectArray5;
                        objectArray5[3] = worksheetversionid;
                    }
                    this.database.executePreparedUpdate(string, objectArray);
                }
                this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_Worksheet", worksheetid, worksheetversionid, "Deleted unassigned items");
            } else if (sdcid.length() > 0) {
                try {
                    if (properties.getProperty("keyid1").length() == 0) {
                        if (this.database.isOracle()) {
                            this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemsdi.sdcid=? AND ( worksheetitemid || worksheetitemversionid IN ( SELECT worksheetitemid || worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?) )", new Object[]{sdcid, worksheetid, worksheetversionid});
                        } else {
                            this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemsdi.sdcid=? AND  (worksheetitemid + worksheetitemversionid IN ( SELECT worksheetitemid + worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?) )", new Object[]{sdcid, worksheetid, worksheetversionid});
                        }
                        this.database.executePreparedUpdate("DELETE FROM worksheetsdi WHERE sdcid=? AND worksheetid = ? AND worksheetversionid = ?", new Object[]{sdcid, worksheetid, worksheetversionid});
                    } else {
                        int i;
                        int keycols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                        String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
                        String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
                        String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
                        DataSet worksheetitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, sdcid FROM worksheetitem, worksheetitemsdi WHERE worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?   AND worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid AND sdcid = ?", new Object[]{worksheetid, worksheetversionid, sdcid});
                        properties.setProperty("worksheetitemid", worksheetitems.getColumnValues("worksheetitemid", ";"));
                        properties.setProperty("worksheetitemversionid", worksheetitems.getColumnValues("worksheetitemversionid", ";"));
                        PreparedStatement stmt = this.database.prepareStatement("DELETE FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND sdcid = ? and keyid1 = ?" + (keycols > 1 ? " AND keyid2 = ? " : "") + (keycols > 2 ? " AND keyid3 = ? " : ""));
                        for (i = 0; i < worksheetitems.size(); ++i) {
                            for (int j = 0; j < keyid1.length; ++j) {
                                stmt.setString(1, worksheetitems.getValue(i, "worksheetitemid"));
                                stmt.setString(2, worksheetitems.getValue(i, "worksheetitemversionid"));
                                stmt.setString(3, sdcid);
                                stmt.setString(4, keyid1[j]);
                                if (keycols > 1) {
                                    stmt.setString(5, keyid2[j]);
                                }
                                if (keycols > 2) {
                                    stmt.setString(6, keyid3[j]);
                                }
                                stmt.executeUpdate();
                            }
                        }
                        stmt = this.database.prepareStatement("DELETE FROM worksheetsdi WHERE worksheetid = ? AND worksheetversionid = ? AND sdcid = ? AND keyid1 = ?" + (keycols > 1 ? " AND keyid2 = ? " : "") + (keycols > 2 ? " AND keyid3 = ? " : ""));
                        for (i = 0; i < keyid1.length; ++i) {
                            stmt.setString(1, worksheetid);
                            stmt.setString(2, worksheetversionid);
                            stmt.setString(3, sdcid);
                            stmt.setString(4, keyid1[i]);
                            if (keycols > 1) {
                                stmt.setString(5, keyid2[i]);
                            }
                            if (keycols > 2) {
                                stmt.setString(6, keyid3[i]);
                            }
                            stmt.executeUpdate();
                        }
                    }
                }
                catch (SQLException e) {
                    throw new SapphireException("Failed to delete worksheet SDIs for worksheet " + DeleteWorksheetSDI.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
                SDIList sdiList = new SDIList();
                sdiList.addSDIList(properties.getProperty("keyid1"), properties.getProperty("keyid3"), properties.getProperty("keyid3"));
                this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_Worksheet", worksheetid, worksheetversionid, "Deleted " + this.getSDCProcessor().getProperty(sdcid, "plural") + ": " + sdiList.toText());
            } else {
                if (this.database.isOracle()) {
                    this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemid || worksheetitemversionid IN ( SELECT worksheetitemid || worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?)", new Object[]{worksheetid, worksheetversionid});
                } else {
                    this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemid + worksheetitemversionid IN ( SELECT worksheetitemid + worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?)", new Object[]{worksheetid, worksheetversionid});
                }
                this.database.executePreparedUpdate("DELETE FROM worksheetsdi WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
                this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_Worksheet", worksheetid, worksheetversionid, "Deleted all LIMS data");
            }
        }
    }
}

