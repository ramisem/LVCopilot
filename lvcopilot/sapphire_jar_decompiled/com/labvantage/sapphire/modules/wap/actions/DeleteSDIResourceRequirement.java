/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.actions;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSDIResourceRequirement
extends BaseAction {
    public static final String PROPERTY_RESOURCENUM = "resourcenum";
    public static final String PROPERTY_RSETID = "rsetid";
    public static final String PROPERTY_APPLYLOCK = "applylock";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SEPARATOR = "separator";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = (String)properties.get(PROPERTY_SDCID);
        if (sdcid.length() == 0) {
            throw new SapphireException("Unable to delete SDIResourceRequirement: No SDC specified.");
        }
        DataSet ds = new DataSet();
        ds.addColumnValues(PROPERTY_KEYID1, 0, (String)properties.get(PROPERTY_KEYID1), ";");
        ds.addColumnValues(PROPERTY_KEYID2, 0, (String)properties.get(PROPERTY_KEYID2), ";", "(null)");
        ds.addColumnValues(PROPERTY_KEYID3, 0, (String)properties.get(PROPERTY_KEYID3), ";", "(null)");
        ds.addColumnValues(PROPERTY_RESOURCENUM, 0, (String)properties.get(PROPERTY_RESOURCENUM), ";");
        ds.padColumns();
        String sql = "DELETE FROM sdiresourcerequirement WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND resourcenum = ?";
        try {
            PreparedStatement statement = this.database.prepareStatement("Delete SDIResourceRequirement", sql);
            statement.setString(1, sdcid);
            for (int i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getValue(i, PROPERTY_KEYID1);
                String keyid2 = ds.getValue(i, PROPERTY_KEYID2);
                String keyid3 = ds.getValue(i, PROPERTY_KEYID3);
                String resourcenum = ds.getValue(i, PROPERTY_RESOURCENUM);
                if (keyid1.length() <= 0 || resourcenum.length() <= 0) continue;
                statement.setString(2, keyid1);
                statement.setString(3, keyid2);
                statement.setString(4, keyid3);
                statement.setString(5, resourcenum);
                statement.executeUpdate();
            }
            this.database.closeStatement("Delete SDIResourceRequirement");
        }
        catch (SQLException e) {
            throw new SapphireException("Unable to delete SDIResourceRequirement. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

