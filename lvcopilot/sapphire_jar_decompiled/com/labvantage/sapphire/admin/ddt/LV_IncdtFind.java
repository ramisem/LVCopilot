/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_IncdtFind
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77330 $";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteIncidentFinding(rsetid, actionProps);
    }

    void deleteIncidentFinding(String rsetid, PropertyList actionProps) throws SapphireException {
        StringBuffer sbActionPlanId = new StringBuffer();
        DataSet dsIncidentFind = null;
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        String keyid1 = actionProps.getProperty("keyid1");
        String sdcid = actionProps.getProperty("sdcid");
        String[] keys = StringUtil.split(keyid1, ";");
        if (sdcid.equalsIgnoreCase("LV_IncdtFind")) {
            PropertyList plDelete = new PropertyList();
            plDelete.put("sdcid", "LV_ActionPlan");
            plDelete.put("keyid1", keyid1);
            try {
                ap.processAction("DeleteSDI", "1", plDelete);
            }
            catch (Exception ex) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Delete actionplans associated with Incident findings :" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
            }
        } else {
            for (int i = 0; i < keys.length - 1; ++i) {
                SafeSQL safeSQL = new SafeSQL();
                String sbIncidentFind = "select actionplanid from actionplan where incidentfindid = " + safeSQL.addVar(keys[i]);
                dsIncidentFind = qp.getPreparedSqlDataSet(sbIncidentFind, safeSQL.getValues());
                for (int k = 0; k < dsIncidentFind.getRowCount(); ++k) {
                    sbActionPlanId.append(";").append(dsIncidentFind.getString(k, "actionplanid"));
                }
                String tempActPlnId = "";
                tempActPlnId = sbActionPlanId.length() > 0 ? sbActionPlanId.toString().substring(1) : sbActionPlanId.toString();
                PropertyList plDelete = new PropertyList();
                plDelete.put("sdcid", "LV_ActionPlan");
                plDelete.put("keyid1", tempActPlnId);
                plDelete.put("copies", new Integer(dsIncidentFind.getRowCount()));
                try {
                    ap.processAction("DeleteSDI", "1", plDelete);
                    continue;
                }
                catch (Exception ex) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Delete actionplans associated with Incident findings :" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
            }
        }
    }
}

