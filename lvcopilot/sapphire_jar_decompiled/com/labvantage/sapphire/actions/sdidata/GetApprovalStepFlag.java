/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class GetApprovalStepFlag
extends BaseAction
implements sapphire.action.GetApprovalStepFlag {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SDI sdi = new SDI(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        String approvalstep = properties.getProperty("approvalstep");
        if (!sdi.isValid()) {
            throw new SapphireException("NO_SDCID", "No sdcid specified");
        }
        if (paramlistid == null || paramlistid.length() == 0) {
            throw new SapphireException("NO_PARAMLISTID", "No paramlistid specified");
        }
        if (paramlistversionid == null || paramlistversionid.length() == 0) {
            throw new SapphireException("NO_PARAMLISTVERID", "No paramlistversionid specified");
        }
        if (variantid == null || variantid.length() == 0) {
            throw new SapphireException("NO_VARIANTID", "No variantid specified");
        }
        if (dataset == null || dataset.length() == 0) {
            throw new SapphireException("NO_DATASET", "No dataset specified");
        }
        if (approvalstep == null || approvalstep.length() == 0) {
            throw new SapphireException("NO_APPROVALSTEP", "No approval step specified");
        }
        properties.setProperty("approvalflag", "Undetermined");
        String select = "SELECT\tapprovalflag FROM\tsdidataapproval WHERE\tsdcid = ? AND \t\tkeyid1 = ? AND \t\tkeyid2 = ? AND \t\tkeyid3 = ? AND \t\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ? AND \t\tdataset = ? AND \t\tapprovalstep = ?";
        try {
            this.database.createPreparedResultSet(select, new Object[]{sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3(), paramlistid, paramlistversionid, variantid, dataset, approvalstep});
        }
        catch (SapphireException sapphireException) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to get result set. Reason: " + select, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
        }
        if (this.database.getNext()) {
            String approvalflag = this.database.getString("approvalflag");
            if (approvalflag == null || approvalflag.length() == 0) {
                properties.setProperty("approvalflag", "Undetermined");
            } else if (approvalflag.equals("P")) {
                properties.setProperty("approvalflag", "Pass");
            } else if (approvalflag.equals("F")) {
                properties.setProperty("approvalflag", "Fail");
            }
        } else {
            throw new SapphireException("EMPTY_RESULTSET", "No rows returned in approval step query: " + select);
        }
    }
}

