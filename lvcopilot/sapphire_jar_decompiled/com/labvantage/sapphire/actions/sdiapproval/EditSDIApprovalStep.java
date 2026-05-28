/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EditSDIApprovalStep
extends BaseAction
implements sapphire.action.EditSDIApprovalStep {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        if (properties.getProperty("approvaltypeid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Type specified.");
        }
        if (properties.getProperty("approvalstep").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Step specified.");
        }
        Calendar now = DateTimeUtil.getNowCalendar();
        DataSet props = new DataSet();
        props.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), delimeter);
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), delimeter);
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), delimeter, "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), delimeter, "(null)");
        props.addColumnValues("approvaltypeid", 0, properties.getProperty("approvaltypeid"), delimeter);
        props.addColumnValues("approvalstep", 0, properties.getProperty("approvalstep"), delimeter);
        props.padColumns();
        props.addColumnValues("approvalstepinstance", 1, properties.getProperty("approvalstepinstance"), delimeter, "1");
        props.addColumnValues("roleid", 0, properties.getProperty("roleid"), delimeter);
        props.addColumnValues("assignedto", 0, properties.getProperty("assignedto"), delimeter);
        props.addColumnValues("mandatoryflag", 0, properties.getProperty("mandatoryflag"), delimeter);
        props.addColumnValues("usersequence", 0, properties.getProperty("usersequence"), delimeter);
        props.setDate(-1, "createdt", now);
        props.setString(-1, "createtool", this.connectionInfo.getTool());
        props.setString(-1, "createby", this.connectionInfo.getSysuserId());
        DataSetUtil.update(this.database, props, "sdiapprovalstep", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid", "approvalstep", "approvalstepinstance"});
    }
}

