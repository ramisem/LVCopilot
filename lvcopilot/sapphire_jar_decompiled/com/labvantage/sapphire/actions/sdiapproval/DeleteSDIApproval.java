/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSDIApproval
extends BaseAction
implements sapphire.action.DeleteSDIApproval {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        if (properties.getProperty("approvaltypeid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Type specified.");
        }
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        boolean hasSteps = properties.getProperty("approvalstep").length() > 0;
        DataSet props = new DataSet();
        props.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), delimeter);
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), delimeter);
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), delimeter, "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), delimeter, "(null)");
        props.addColumnValues("approvaltypeid", 0, properties.getProperty("approvaltypeid"), delimeter);
        if (hasSteps) {
            props.addColumnValues("approvalstep", 0, properties.getProperty("approvalstep"), delimeter);
            props.addColumnValues("approvalstepinstance", 1, properties.getProperty("approvalstepinstance"), delimeter, "1");
        }
        props.padColumns();
        for (int i = 0; i < props.size(); ++i) {
            String sdcid = props.getValue(i, "sdcid");
            String keyid1 = props.getValue(i, "keyid1");
            String keyid2 = props.getValue(i, "keyid2");
            String keyid3 = props.getValue(i, "keyid3");
            String approvaltypeid = props.getValue(i, "approvaltypeid");
            if (hasSteps) {
                String approvalstep = props.getValue(i, "approvalstep");
                String approvalstepinstance = props.getValue(i, "approvalstepinstance");
                String deleteApprovalStepSql = "DELETE FROM sdiapprovalstep WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=? and approvalstep=? and approvalstepinstance=?";
                this.database.executePreparedUpdate(deleteApprovalStepSql, new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid, approvalstep, approvalstepinstance});
                String checkExists = "SELECT 1 FROM sdiapprovalstep WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?";
                this.database.createPreparedResultSet(checkExists, new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
                if (this.database.getNext()) continue;
                String deleteApprovalSql = "DELETE FROM sdiapproval WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?";
                this.database.executePreparedUpdate(deleteApprovalSql, new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
                continue;
            }
            String deleteApprovalSql = "DELETE FROM sdiapproval WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?";
            String deleteApprovalStepSql = "DELETE FROM sdiapprovalstep WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?";
            this.database.executePreparedUpdate(deleteApprovalStepSql, new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
            this.database.executePreparedUpdate(deleteApprovalSql, new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
        }
    }
}

