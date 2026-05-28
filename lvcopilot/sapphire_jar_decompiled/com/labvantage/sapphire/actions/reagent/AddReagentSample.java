/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.reagent;

import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddReagentSample
extends BaseAction
implements sapphire.action.AddReagentSample {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String specversionid;
        String reagentlotid = properties.getProperty("reagentlotid");
        String accessControlledFlag = this.getSDCProcessor().getProperty("LV_ReagentLot", "accesscontrolledflag");
        String sqlReagentType = "SELECT reagentTypeid, reagentTypeVersionid ";
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(accessControlledFlag);
        if (deptSecurityEnabled) {
            sqlReagentType = sqlReagentType + ", securitydepartment";
        }
        sqlReagentType = sqlReagentType + " FROM reagentlot WHERE reagentlotid = ?";
        this.database.createPreparedResultSet(sqlReagentType, new Object[]{reagentlotid});
        if (!this.database.getNext()) {
            throw new SapphireException("For Quality Sample Reagent Type Needs to be Defined");
        }
        String reagentTypeid = this.database.getValue("reagentTypeid");
        String reagentTypeidVersionid = this.database.getValue("reagentTypeVersionid");
        String securityDepartment = deptSecurityEnabled ? this.database.getValue("securitydepartment") : "";
        String sampleStatus = properties.getProperty("samplestatus");
        String sampleTypeid = properties.getProperty("sampletypeid");
        String qualitysampleTemplateid = properties.getProperty("sampletemplateid");
        DataSet workitems = this.getWorkItem(reagentTypeid, reagentTypeidVersionid);
        this.database = ReagentUtil.getReagentTypeDetail(this.database, reagentTypeid, reagentTypeidVersionid);
        String specid = this.database.getValue("specid");
        String string = specversionid = this.database.getValue("specversionid").trim().length() > 0 ? this.database.getValue("specversionid") : "C";
        if (qualitysampleTemplateid.trim().length() == 0) {
            qualitysampleTemplateid = this.database.getValue("qualitysampleid");
        }
        PropertyList sampleProps = new PropertyList();
        sampleProps.setProperty("sdcid", "Sample");
        sampleProps.setProperty("sampledesc", "Consumable Sample for " + reagentTypeid);
        sampleProps.setProperty("reagentlotid", reagentlotid);
        if (securityDepartment.length() > 0) {
            sampleProps.setProperty("securitydepartment", securityDepartment);
        }
        sampleProps.setProperty("qcsampletype", "Reagent Quality");
        if (sampleTypeid.length() > 0) {
            sampleProps.setProperty("sampletypeid", sampleTypeid);
        }
        if (qualitysampleTemplateid != null && qualitysampleTemplateid.trim().length() > 0) {
            sampleProps.setProperty("templateid", qualitysampleTemplateid);
        } else {
            sampleProps.setProperty("samplestatus", sampleStatus);
        }
        sampleProps.setProperty("applyworkitems", "Y");
        try {
            sampleProps.put("wapstatus", "Never");
            this.getActionProcessor().processAction("AddSDI", "1", sampleProps);
            String newKeyid1 = sampleProps.getProperty("newkeyid1");
            properties.setProperty("newkeyid1", newKeyid1);
            this.logger.info("Start attaching workitems to the Reagent Quality Sample");
            this.setWorkItem(newKeyid1, workitems, reagentTypeid, reagentTypeidVersionid);
            this.logger.info("End attaching workitems to the Reagent Quality Sample");
            this.logger.info("Start attaching Specifications to the Reagent Quality Sample");
            this.setSpecification(newKeyid1, specid, specversionid);
            this.logger.info("Start attaching Specifications to the Reagent Quality Sample");
        }
        catch (Exception e) {
            throw new SapphireException("Not able to Add Quality Sample" + e);
        }
    }

    private DataSet getWorkItem(String reagentTypeid, String reagentTypeVersionid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sqlWorkitem = new StringBuffer("SELECT workitemid, workitemversionid, workitemtypeflag, reflexrule FROM sdiworkitem");
        sqlWorkitem.append(" WHERE keyid1=").append(safeSQL.addVar(reagentTypeid));
        sqlWorkitem.append(" AND keyid2=").append(safeSQL.addVar(reagentTypeVersionid));
        sqlWorkitem.append(" AND sdcid='LV_ReagentType'");
        sqlWorkitem.append(" AND ( workitemtypeflag='P' OR ( workitemtypeflag='W' AND groupid IS NULL ))");
        sqlWorkitem.append(" ORDER BY usersequence");
        return this.getQueryProcessor().getPreparedSqlDataSet(sqlWorkitem.toString(), safeSQL.getValues());
    }

    private void setWorkItem(String keyid1, DataSet workitems, String reagentTypeId, String reagentTypeVersionId) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        PropertyList plWorkItemProps = new PropertyList();
        if (keyid1.trim().length() > 0 && workitems.getRowCount() > 0) {
            plWorkItemProps.setProperty("sdcid", "Sample");
            for (int i = 0; i < workitems.getRowCount(); ++i) {
                String workitemVersion = workitems.getValue(i, "workitemversionid");
                if (workitemVersion.trim().length() != 0) continue;
                workitems.setString(i, "workitemversionid", "C");
            }
            HashMap<String, String> filterWI = new HashMap<String, String>();
            filterWI.put("workitemtypeflag", "P");
            DataSet pkgSdiWorkitems = workitems.getFilteredDataSet(filterWI);
            filterWI.put("workitemtypeflag", "W");
            DataSet nonpkgSdiWorkitems = workitems.getFilteredDataSet(filterWI);
            boolean nonPkgWIRuleExists = false;
            for (int n = 0; n < nonpkgSdiWorkitems.getRowCount(); ++n) {
                String reflexRule = nonpkgSdiWorkitems.getValue(n, "reflexrule");
                if (reflexRule.length() <= 0 || "null".equalsIgnoreCase(reflexRule)) continue;
                nonPkgWIRuleExists = true;
                break;
            }
            StringBuffer workItemIds = new StringBuffer();
            StringBuffer workItemVersionIds = new StringBuffer();
            if (nonPkgWIRuleExists) {
                workItemIds.append("_Reflex");
                workItemVersionIds.append("1");
                workItemIds.append(pkgSdiWorkitems.getRowCount() > 0 ? ";" + pkgSdiWorkitems.getColumnValues("workitemid", ";") : "");
                workItemVersionIds.append(pkgSdiWorkitems.getRowCount() > 0 ? ";" + pkgSdiWorkitems.getColumnValues("workitemversionid", ";") : "");
                plWorkItemProps.setProperty("__sourcesdcid", "LV_ReagentType");
                plWorkItemProps.setProperty("__sourcekeyids", reagentTypeId + ";" + reagentTypeVersionId + ";(null)");
            } else {
                workItemIds.append(workitems.getColumnValues("workitemid", ";"));
                workItemVersionIds.append(workitems.getColumnValues("workitemversionid", ";"));
            }
            plWorkItemProps.setProperty("workitemid", workItemIds.toString());
            plWorkItemProps.setProperty("workitemversionid", workItemVersionIds.toString());
            plWorkItemProps.setProperty("keyid1", keyid1);
            plWorkItemProps.setProperty("keyid2", "");
            plWorkItemProps.setProperty("keyid3", "");
            plWorkItemProps.setProperty("applyworkitem", "Y");
            try {
                plWorkItemProps.setProperty("wapstatus", "Never");
                ap.processAction("AddSDIWorkItem", "1", plWorkItemProps);
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Not able to Add Workitem", e);
            }
        }
    }

    private void setSpecification(String keyid1, String specids, String speciVersionds) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        PropertyList plSpecProps = new PropertyList();
        if (keyid1.trim().length() > 0 && specids.trim().length() > 0) {
            plSpecProps.setProperty("sdcid", "Sample");
            plSpecProps.setProperty("specid", specids);
            plSpecProps.setProperty("specversionid", speciVersionds);
            plSpecProps.setProperty("keyid1", keyid1);
            plSpecProps.setProperty("keyid2", "");
            plSpecProps.setProperty("keyid3", "");
            plSpecProps.setProperty("keyid3", "");
            try {
                ap.processAction("AddSDISpec", "1", plSpecProps);
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Not able to Add Specification", e);
            }
        }
    }
}

