/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.capa;

import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class DeleteIncident
extends BaseAction
implements sapphire.action.DeleteIncident {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 53943 $";
    public static final String PROPERTY_KEYID1 = "keyid1";

    @Override
    public void processAction(PropertyList property) throws SapphireException {
        String keyid1 = property.getProperty(PROPERTY_KEYID1);
        QueryProcessor qp = this.getQueryProcessor();
        ActionProcessor ap = this.getActionProcessor();
        DataSet dsFindings = qp.getPreparedSqlDataSet("select incidentfindid from incidentfind where incidentid=?", (Object[])new String[]{keyid1});
        DataSet dsActionPlans = qp.getPreparedSqlDataSet("select actionplanid from actionplan where incidentid=?", (Object[])new String[]{keyid1});
        String incidentFindings = dsFindings.getColumnValues("incidentfindid", "','");
        String actionPlans = dsActionPlans.getColumnValues("actionplanid", "','");
        this.database.executePreparedUpdate("delete from workorder where sourcesdcid = 'LV_Incdt' and sourcekeyid1=?", new String[]{keyid1});
        SafeSQL safeSQL = new SafeSQL();
        this.database.executePreparedUpdate("delete from workorder where sourcesdcid = 'LV_IncdtFind' and sourcekeyid1 in (" + safeSQL.addIn(incidentFindings) + ")", safeSQL.getValues());
        safeSQL.reset();
        this.database.executePreparedUpdate("delete from workorder where sourcesdcid = 'LV_ActionPlan' and sourcekeyid1 in (" + safeSQL.addIn(actionPlans) + ")", safeSQL.getValues());
        this.database.executePreparedUpdate("delete from actionplan where incidentid=?", new String[]{keyid1});
        this.database.executePreparedUpdate("delete from incidentfind where incidentid=?", new String[]{keyid1});
        property.setProperty("sdcid", "LV_Incdt");
        ap.processAction("DeleteSDI", "1", property);
    }
}

