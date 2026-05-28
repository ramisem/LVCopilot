/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SetBulletinStatusEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.StringHolder;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SetBulletinStatus
extends BaseAction
implements sapphire.action.SetBulletinStatus {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringHolder rsetidHolder;
        String bulletinid = properties.getProperty("bulletinid");
        String sysuserid = properties.getProperty("sysuserid");
        String status = properties.getProperty("status");
        DAMProcessor dam = this.getDAMProcessor();
        if (dam.createRSet("Bulletin", bulletinid, "", "", rsetidHolder = new StringHolder()) == 1) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            if (status.equalsIgnoreCase("read")) {
                sql.append("UPDATE bulletinsysuser SET readflag = 'Y', readdt = ").append(safeSQL.addVar(DateTimeUtil.getNowTimestamp())).append("");
            } else if (status.equalsIgnoreCase("unread")) {
                sql.append("UPDATE bulletinsysuser SET readflag = 'N', readdt = null");
            } else if (status.equalsIgnoreCase("deleted")) {
                sql.append("UPDATE bulletinsysuser SET deletedflag = 'Y'");
            }
            sql.append(" WHERE bulletinid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ").append(safeSQL.addVar(rsetidHolder.value)).append(") ");
            if (sysuserid.length() > 0) {
                sql.append(" AND sysuserid = ").append(safeSQL.addVar(sysuserid));
            }
            this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
            EventManager.generateEvent(new SapphireConnection(this.database.getConnection(), this.connectionInfo), null, new SetBulletinStatusEventObject(sysuserid, status));
        }
    }
}

