/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.sapphire.actions.sdi.EditSDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RollBackSDI
extends BaseAction
implements sapphire.action.RollBackSDI {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        DataSet dataSet;
        String sql;
        String changeLogID = actionProps.getProperty("changelogid");
        String sdcid = actionProps.getProperty("sdcid");
        String keyid1 = StringUtil.replaceAll(actionProps.getProperty("keyid1"), "%3B", ";");
        String keyid2 = actionProps.getProperty("keyid2");
        String keyid3 = actionProps.getProperty("keyid3");
        String propertytreenodeid = actionProps.getProperty("propertytreenodeid");
        if ("PropertyTree".equals(sdcid) && propertytreenodeid.length() == 0) {
            throw new SapphireException("RollBackSDI", "VALIDATION", this.getTranslationProcessor().translate("Missing action input - Property Tree Node."));
        }
        if (changeLogID.length() == 0) {
            if (sdcid.length() == 0 && keyid1.length() == 0) {
                throw new SapphireException("RollBackSDI", "VALIDATION", this.getTranslationProcessor().translate("Missing action input. Either Change Log ID or SDC/Keyid1 must be provided."));
            }
            String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            sql = "select cl.changelogid, cl.changelogstatus, cl.linksdcid, cl.linkkeyid1, cl.linkkeyid2, cl.linkkeyid3, cl.propertytreenodeid from changelog cl, rsetitems rs where cl.linksdcid = rs.sdcid and cl.linkkeyid1 = rs.keyid1 and cl.linkkeyid2 = rs.keyid2 and cl.linkkeyid3 = rs.keyid3 and rs.rsetid = ?";
            dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql = "select cl.changelogid, cl.changelogstatus, cl.linksdcid, cl.linkkeyid1, cl.linkkeyid2, cl.linkkeyid3, cl.propertytreenodeid from changelog cl where cl.changelogid in (" + safeSQL.addIn(changeLogID, ";") + ")";
            dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        if (dataSet != null && dataSet.size() > 0) {
            for (int i = 0; i < dataSet.size(); ++i) {
                String changeLogStatus = dataSet.getString(i, "changelogstatus", "");
                if ("Checked Out".equals(changeLogStatus)) continue;
                throw new SapphireException("RollBackSDI", "VALIDATION", this.getTranslationProcessor().translate("Only Checked Out Change Log can be rolled back."));
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ChangeLog");
            props.setProperty("keyid1", dataSet.getColumnValues("changelogid", ";"));
            props.setProperty("changelogstatus", "Rolled Back");
            props.setProperty("rolledbackby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            props.setProperty("rolledbackdt", "n");
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            props.setProperty("auditdt", actionProps.getProperty("auditdt"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }
}

