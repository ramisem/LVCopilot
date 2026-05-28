/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditWorksheetItemParams
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetitemversionid;
        String worksheetitemid = properties.getProperty("worksheetitemid");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid = properties.getProperty("worksheetitemversionid"));
        if (!item.getValue(0, "itemstatus").equals("InProgress")) {
            throw new SapphireException("Blocked editing params on worksheetitem " + EditWorksheetItemParams.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
        }
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            String[] paramname = StringUtil.split(properties.getProperty("paramname"), ";");
            String[] paramtitle = StringUtil.split(properties.getProperty("paramtitle"), ";");
            String[] parameditorstyleid = StringUtil.split(properties.getProperty("parameditorstyleid"), ";");
            String[] paramvalue = StringUtil.split(properties.getProperty("paramvalue"), ";");
            String[] valuesdcid = StringUtil.split(properties.getProperty("valuesdcid"), ";");
            String[] valuekeyid1 = StringUtil.split(properties.getProperty("valuekeyid1"), ";");
            String[] valuekeyid2 = StringUtil.split(properties.getProperty("valuekeyid2"), ";");
            String[] valuetype = StringUtil.split(properties.getProperty("valuetype"), ";");
            String[] valuelabel = StringUtil.split(properties.getProperty("valuelabel"), ";");
            try {
                PreparedStatement stmt = this.database.prepareStatement("UPDATE worksheetitemparam SET paramtitle = ?, parameditorstyleid = ?, paramvalue = ?, valuesdcid = ?, valuekeyid1 = ?, valuekeyid2 = ?, valuetype = ?, valuelabel = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND paramname = ?");
                for (int i = 0; i < paramname.length; ++i) {
                    stmt.setString(1, paramtitle[i]);
                    stmt.setString(2, parameditorstyleid[i]);
                    stmt.setString(3, paramvalue[i]);
                    stmt.setString(4, valuesdcid[i]);
                    stmt.setString(5, valuekeyid1[i]);
                    stmt.setString(6, valuekeyid2[i]);
                    stmt.setString(7, valuetype[i]);
                    stmt.setString(8, valuelabel[i]);
                    stmt.setString(9, worksheetitemid);
                    stmt.setString(10, worksheetitemversionid);
                    stmt.setString(11, paramname[i]);
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to edit worksheetitem params", e);
            }
            this.addActivityLog(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Edit", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Edited parameter value");
        }
    }
}

