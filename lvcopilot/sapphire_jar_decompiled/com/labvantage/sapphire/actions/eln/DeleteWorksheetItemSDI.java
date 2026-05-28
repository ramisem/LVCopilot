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

public class DeleteWorksheetItemSDI
extends BaseELNAction
implements sapphire.action.DeleteWorksheetItemSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetitemversionid;
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid = properties.getProperty("worksheetitemversionid"));
        if (!item.getValue(0, "itemstatus").equals("InProgress")) {
            throw new SapphireException("Blocked deleting SDIs from worksheetitem " + DeleteWorksheetItemSDI.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
        }
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            String activitylog;
            try {
                String sdcid = properties.getProperty("sdcid");
                if (sdcid.length() > 0) {
                    if (properties.getProperty("keyid1").length() > 0) {
                        int keycols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                        String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
                        String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
                        String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
                        PreparedStatement stmt = this.database.prepareStatement("DELETE FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND sdcid = ? AND keyid1 = ?" + (keycols > 1 ? " AND keyid2 = ? " : "") + (keycols > 2 ? " AND keyid3 = ? " : ""));
                        for (int i = 0; i < keyid1.length; ++i) {
                            stmt.setString(1, worksheetitemid);
                            stmt.setString(2, worksheetitemversionid);
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
                        SDIList sdiList = new SDIList();
                        sdiList.addSDIList(properties.getProperty("keyid1"), properties.getProperty("keyid3"), properties.getProperty("keyid3"));
                        activitylog = "Deleted " + this.getSDCProcessor().getProperty(sdcid, "plural") + ": " + sdiList.toText();
                    } else {
                        this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND sdcid = ?", new Object[]{worksheetitemid, worksheetitemversionid, sdcid});
                        activitylog = "Deleted all " + this.getSDCProcessor().getProperty(sdcid, "singular") + " SDIs";
                    }
                } else {
                    this.database.executePreparedUpdate("DELETE FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
                    activitylog = "Deleted all SDIs";
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to delete SDIs for worksheetitem " + DeleteWorksheetItemSDI.getIdVersionText(worksheetitemid, worksheetitemversionid) + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, activitylog);
        }
    }
}

