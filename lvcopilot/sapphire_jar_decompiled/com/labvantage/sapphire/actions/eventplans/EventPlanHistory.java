/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eventplans;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EventPlanHistory
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            String sdcid = properties.getProperty("sdcid");
            DataSet eventplanhistory = (DataSet)properties.get("eventplanhistory");
            String[] eventplanhistoryid = ((DBUtil)this.database).getTableSequence("eventplanhistory", eventplanhistory.size());
            String insert = "INSERT INTO eventplanhistory ( eventplanhistoryid, sdcid, keyid1, keyid2, keyid3, eventplanid, eventplanversionid, eventplaninstance, eventplanitemid, eventdt, createdt, createby, createtool, eventsummary, eventlog )VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = this.database.prepareStatement(insert);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            for (int i = 0; i < eventplanhistory.size(); ++i) {
                ps.setString(1, eventplanhistoryid[i]);
                ps.setString(2, sdcid);
                ps.setString(3, eventplanhistory.getValue(i, "keyid1"));
                ps.setString(4, eventplanhistory.getValue(i, "keyid2"));
                ps.setString(5, eventplanhistory.getValue(i, "keyid3"));
                ps.setString(6, eventplanhistory.getValue(i, "eventplanid"));
                ps.setString(7, eventplanhistory.getValue(i, "eventplanversionid"));
                ps.setInt(8, eventplanhistory.getInt(i, "eventplaninstance", 1));
                ps.setString(9, eventplanhistory.getValue(i, "eventplanitemid"));
                ps.setTimestamp(10, eventplanhistory.getTimestamp(i, "eventdt"));
                ps.setTimestamp(11, now);
                ps.setString(12, this.connectionInfo.getSysuserId());
                ps.setString(13, this.connectionInfo.getTool());
                ps.setString(14, eventplanhistory.getValue(i, "eventsummary"));
                ps.setString(15, eventplanhistory.getValue(i, "eventlog"));
                ps.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to log event plan events. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

