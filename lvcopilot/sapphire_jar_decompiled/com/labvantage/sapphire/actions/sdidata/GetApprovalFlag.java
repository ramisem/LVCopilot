/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetApprovalFlag
extends BaseAction
implements sapphire.action.GetApprovalFlag {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SDI sdi = new SDI(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        if (sdi.isValid()) {
            String dataset;
            String variantid;
            String paramlistversionid;
            properties.setProperty("approvalflag", "Undetermined");
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer select = new StringBuffer("SELECT\tapprovalpassrule, mandatoryflag, sdidataapproval.approvalflag, approvalstep ");
            select.append("FROM sdidata, sdidataapproval ").append("WHERE\tsdidataapproval.sdcid = sdidata.sdcid AND ").append("\tsdidataapproval.keyid1 = sdidata.keyid1 AND ").append("\tsdidataapproval.keyid2 = sdidata.keyid2 AND ").append("\tsdidataapproval.keyid3 = sdidata.keyid3 AND ").append("\tsdidataapproval.paramlistid = sdidata.paramlistid AND ").append("\tsdidataapproval.paramlistversionid = sdidata.paramlistversionid AND ").append("\tsdidataapproval.variantid = sdidata.variantid AND ").append("\tsdidataapproval.dataset = sdidata.dataset AND ");
            String paramlistid = properties.getProperty("paramlistid");
            if (paramlistid != null && paramlistid.length() > 0) {
                select.append("sdidataapproval.paramlistid = ").append(safeSQL.addVar(paramlistid)).append(" AND ");
            }
            if ((paramlistversionid = properties.getProperty("paramlistversionid")) != null && paramlistversionid.length() > 0) {
                select.append("sdidataapproval.paramlistversionid = ").append(safeSQL.addVar(paramlistversionid)).append(" AND ");
            }
            if ((variantid = properties.getProperty("variantid")) != null && variantid.length() > 0) {
                select.append("sdidataapproval.variantid = ").append(safeSQL.addVar(variantid)).append(" AND ");
            }
            if ((dataset = properties.getProperty("dataset")) != null && dataset.length() > 0) {
                select.append("sdidataapproval.dataset = ").append(safeSQL.addVar(dataset)).append(" AND ");
            }
            select.append("sdidataapproval.sdcid = ").append(safeSQL.addVar(sdi.getSdcid())).append(" AND sdidataapproval.keyid1 = ").append(safeSQL.addVar(sdi.getKeyid1())).append(" AND sdidataapproval.keyid2 = ").append(safeSQL.addVar(sdi.getKeyid2())).append(" AND sdidataapproval.keyid3 = ").append(safeSQL.addVar(sdi.getKeyid3())).append("");
            try {
                this.database.createPreparedResultSet(select.toString() + " order by sdidataapproval.usersequence", safeSQL.getValues());
            }
            catch (SapphireException sapphireException) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to get result set for. Reason: " + select, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
            }
            DataSet ds = new DataSet(this.database.getResultSet(), this.connectionInfo);
            boolean honorAllMandatoryApprovalSteps = true;
            try {
                honorAllMandatoryApprovalSteps = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("honorallmandatoryapprovalsteps", "Y").equals("Y");
            }
            catch (Exception exception) {
                // empty catch block
            }
            boolean flagdetermined = false;
            boolean allStepsUndetermined = true;
            if (ds.getRowCount() > 0) {
                String approvalflag;
                int row;
                String passrule = ds.getValue(0, "approvalpassrule", "");
                if (passrule.equals("1P") || passrule.equals("1MP")) {
                    properties.setProperty("approvalflag", "Fail");
                } else {
                    properties.setProperty("approvalflag", "Pass");
                }
                for (row = 0; row < ds.getRowCount() && !flagdetermined; ++row) {
                    approvalflag = ds.getValue(row, "approvalflag", "U");
                    String approvalstep = ds.getValue(row, "approvalstep", "");
                    if (properties.getProperty(approvalstep).length() > 0) {
                        approvalflag = properties.getProperty(approvalstep);
                    }
                    if (approvalflag == null) {
                        approvalflag = "U";
                    }
                    ds.setValue(row, "approvalflag", approvalflag);
                    boolean isMandatory = ds.getValue(row, "mandatoryflag", "N").equals("Y");
                    if (honorAllMandatoryApprovalSteps && isMandatory && approvalflag.equals("U")) {
                        properties.setProperty("approvalflag", "Undetermined");
                        flagdetermined = true;
                    }
                    if (approvalflag.equals("U")) continue;
                    allStepsUndetermined = false;
                }
                if (!flagdetermined && allStepsUndetermined) {
                    properties.setProperty("approvalflag", "Undetermined");
                    flagdetermined = true;
                }
                for (row = 0; row < ds.getRowCount() && !flagdetermined; ++row) {
                    approvalflag = ds.getValue(row, "approvalflag", "U");
                    boolean isMandatory = ds.getValue(row, "mandatoryflag", "N").equals("Y");
                    if (passrule.equals("AP")) {
                        if (approvalflag.equals("F")) {
                            properties.setProperty("approvalflag", "Fail");
                            flagdetermined = true;
                            continue;
                        }
                        if (!approvalflag.equals("U")) continue;
                        properties.setProperty("approvalflag", "Undetermined");
                        continue;
                    }
                    if (passrule.equals("AMP")) {
                        if (isMandatory && approvalflag.equals("F")) {
                            properties.setProperty("approvalflag", "Fail");
                            flagdetermined = true;
                            continue;
                        }
                        if (!isMandatory || !approvalflag.equals("U")) continue;
                        properties.setProperty("approvalflag", "Undetermined");
                        continue;
                    }
                    if (passrule.equals("1P")) {
                        if (approvalflag.equals("P")) {
                            properties.setProperty("approvalflag", "Pass");
                            flagdetermined = true;
                            continue;
                        }
                        if (!isMandatory || !approvalflag.equals("U")) continue;
                        properties.setProperty("approvalflag", "Undetermined");
                        continue;
                    }
                    if (passrule.equals("1MP")) {
                        if (isMandatory && approvalflag.equals("P")) {
                            properties.setProperty("approvalflag", "Pass");
                            flagdetermined = true;
                            continue;
                        }
                        if (!isMandatory || !approvalflag.equals("U")) continue;
                        properties.setProperty("approvalflag", "Undetermined");
                        continue;
                    }
                    if (passrule.equals("NF")) {
                        if (approvalflag.equals("F")) {
                            properties.setProperty("approvalflag", "Fail");
                            flagdetermined = true;
                            continue;
                        }
                        if (!isMandatory || !approvalflag.equals("U")) continue;
                        properties.setProperty("approvalflag", "Undetermined");
                        continue;
                    }
                    if (!passrule.equals("NMF")) continue;
                    if (isMandatory && approvalflag.equals("F")) {
                        properties.setProperty("approvalflag", "Fail");
                        flagdetermined = true;
                        continue;
                    }
                    if (!isMandatory || !approvalflag.equals("U")) continue;
                    properties.setProperty("approvalflag", "Undetermined");
                }
            } else {
                properties.setProperty("approvalflag", "Pass");
            }
        } else {
            throw new SapphireException("NO_SDCID", "No sdcid specified.");
        }
    }
}

