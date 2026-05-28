/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIWorkflowRule
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] workflowdefidprop = StringUtil.split(properties.getProperty("workflowdefid"), ";");
        String[] workflowdefversionidprop = StringUtil.split(properties.getProperty("workflowdefversionid"), ";");
        String[] workflowdefvariantidprop = StringUtil.split(properties.getProperty("workflowdefvariantid"), ";");
        String[] taskdefitemidprop = StringUtil.split(properties.getProperty("taskdefitemid"), ";");
        String[] ioitemidprop = StringUtil.split(properties.getProperty("ioitemid"), ";");
        String[] workflowexecidprop = StringUtil.split(properties.getProperty("workflowexecid"), ";");
        if (keyid1prop.length == workflowdefidprop.length && workflowdefidprop.length == workflowdefversionidprop.length) {
            try {
                PreparedStatement delete = this.database.prepareStatement("DELETE FROM sdiworkflowrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ? AND taskdefitemid = ? AND ioitemid = ? AND workflowexecid = ?");
                for (int i = 0; i < keyid1prop.length; ++i) {
                    delete.setString(1, sdcid);
                    delete.setString(2, keyid1prop[i]);
                    delete.setString(3, keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                    delete.setString(4, keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                    delete.setString(5, workflowdefidprop[i]);
                    delete.setString(6, workflowdefversionidprop[i]);
                    delete.setString(7, workflowdefvariantidprop[i]);
                    delete.setString(8, taskdefitemidprop[i]);
                    delete.setString(9, ioitemidprop[i]);
                    delete.setString(10, workflowexecidprop[i]);
                    delete.execute();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to delete sdiworkflowrule records. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

