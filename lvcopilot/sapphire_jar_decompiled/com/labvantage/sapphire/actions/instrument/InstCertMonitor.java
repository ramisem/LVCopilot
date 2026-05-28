/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.instrument;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class InstCertMonitor
extends BaseAction
implements sapphire.action.InstCertMonitor {
    static final String LABVANTAGE_CVS_ID = "$Revision: 80323 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sql = "SELECT wo.workorderid, wo.workorderstatus, wo.disposition, wo.sourcekeyid1, wo.duedt  FROM workorder wo WHERE " + (this.getConnectionProcessor().isOra() ? "SYSDATE > NVL(wo.windowenddt, wo.duedt) " : "GETDATE() > ISNULL(wo.windowenddt, wo.duedt) ") + " AND wo.workordertype = 'Certification' AND ( wo.outsidegraceperiodflag IS NULL OR wo.outsidegraceperiodflag != 'Y' )  AND ( wo.workorderstatus = 'Pending' OR (((wo.workorderstatus = 'Complete' AND wo.disposition = 'Rejected') OR  wo.workorderstatus = 'Cancelled' ) AND NOT EXISTS (select woi.workorderid from workorder woi where woi.SCHEDULEPLANID = wo.SCHEDULEPLANID and woi.SCHEDULEPLANITEMID = wo.SCHEDULEPLANITEMID and woi.createdt > wo.createdt and woi.workorderstatus = 'Complete' AND woi.disposition = 'Accepted' )))";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        if (ds.getRowCount() > 0) {
            String keyid1 = "";
            String outsidegraceperiodflag = "";
            keyid1 = ";" + ds.getColumnValues("workorderid", ";");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                outsidegraceperiodflag = outsidegraceperiodflag + ";Y";
            }
            if (keyid1.length() > 1) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "WorkOrderSDC");
                actionProps.setProperty("keyid1", keyid1.substring(1));
                actionProps.setProperty("outsidegraceperiodflag", outsidegraceperiodflag.substring(1));
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
            }
        }
    }
}

