/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.capa;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.IncidentUtil;
import com.labvantage.sapphire.DataSetUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RecordIncident
extends BaseAction
implements sapphire.action.RecordIncident {
    private ActionProcessor ap;

    private String doAddSdi(DataSet ds, PropertyList pl, String keyColumn, String linkColumn) throws ActionException {
        String newkeyid1 = "";
        for (int i = 0; i < ds.size(); ++i) {
            pl.setProperty("newkeyid1", "");
            pl.setProperty("templateid", ds.getValue(i, keyColumn));
            if (linkColumn.length() > 0) {
                pl.setProperty(linkColumn, ds.getValue(i, linkColumn));
            }
            this.ap.processAction("AddSDI", "1", pl);
            newkeyid1 = newkeyid1 + ";" + pl.getProperty("newkeyid1");
        }
        return this.stripFirstSemiColon(newkeyid1);
    }

    private DataSet replaceColumnValue(DataSet dsModofied, String colId, String[] allOldIncident_actionPlans, String[] allNewIncident_actionPlans) {
        for (int i = 0; i < dsModofied.getRowCount(); ++i) {
            for (int j = 0; j < dsModofied.getColumnCount(); ++j) {
                String colName = dsModofied.getColumnId(j);
                if (!colName.equals(colId)) continue;
                String colValue = dsModofied.getValue(i, colName);
                for (int k = 0; k < allOldIncident_actionPlans.length; ++k) {
                    String tempOldVal = allOldIncident_actionPlans[k];
                    if (!tempOldVal.equals(colValue)) continue;
                    String tempNewVal = allNewIncident_actionPlans[k];
                    dsModofied.setValue(i, colName, tempNewVal);
                }
            }
        }
        return dsModofied;
    }

    private String stripFirstSemiColon(String input) {
        if (input.startsWith(";")) {
            input = input.substring(1);
        }
        return input;
    }

    @Override
    public void processAction(PropertyList property) throws SapphireException {
        String incidentId;
        HashMap hmDatasets;
        TranslationProcessor tp;
        String causalObjectFlag;
        String replicatedId;
        String paramType;
        String paramid;
        String dataset;
        String variantId;
        String paramlistVersionId;
        String paramlistId;
        String sourceKeyId3;
        String sourceKeyId2;
        String sourceKeyId1;
        String sourcesdcid;
        block33: {
            String incidentTemplateId = property.getProperty("templateid");
            String newIncidentTemplateId = property.getProperty("newtemplateid");
            String incidentCategory = property.getProperty("incidentcategory");
            sourcesdcid = property.getProperty("sourcesdcid");
            sourceKeyId1 = property.getProperty("sourcekeyid1");
            sourceKeyId2 = property.getProperty("sourcekeyid2");
            sourceKeyId3 = property.getProperty("sourcekeyid3");
            paramlistId = property.getProperty("paramlistid");
            paramlistVersionId = property.getProperty("paramlistversionid");
            variantId = property.getProperty("variantid");
            dataset = property.getProperty("dataset");
            paramid = property.getProperty("paramid");
            paramType = property.getProperty("paramtype");
            replicatedId = property.getProperty("replicateid");
            causalObjectFlag = property.getProperty("causalobjectflag", "N");
            tp = this.getTranslationProcessor();
            this.ap = this.getActionProcessor();
            QueryProcessor qp = this.getQueryProcessor();
            if (incidentTemplateId.length() == 0 && incidentCategory.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incident Category and templateid both not specified. Cannot create incident. At least one of these must be specified"));
            }
            hmDatasets = IncidentUtil.getIncidentDetailDatasets(incidentTemplateId, qp);
            if (newIncidentTemplateId.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                String cntsql = "select count(*) CNT from incident where incidentid = " + safeSQL.addVar(newIncidentTemplateId);
                DataSet ds = qp.getPreparedSqlDataSet(cntsql, safeSQL.getValues());
                int cnt = Integer.parseInt(ds.getValue(0, "CNT", "0"));
                if (cnt > 0) {
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("templateid", newIncidentTemplateId);
                    throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Template Id '[templateid]' already exists.", valueMap));
                }
            }
            try {
                property.setProperty("sdcid", "LV_Incdt");
                if (incidentTemplateId.length() > 0) {
                    property.setProperty("templateid", incidentTemplateId);
                }
                if (newIncidentTemplateId.length() > 0) {
                    property.setProperty("overrideautokey", "Y");
                    property.setProperty("keyid1", newIncidentTemplateId);
                    property.setProperty("templateflag", "Y");
                    property.setProperty("incidentcategory", incidentCategory);
                    property.setProperty("excludeincidentitem", "Y");
                }
                this.ap.processAction("AddSDI", "1", property);
                this.logger.info("RecordIncident: Created the Incident sdi: " + property.getProperty("newkeyid1"));
            }
            catch (SapphireException se) {
                throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an incident. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
            }
            incidentId = property.getProperty("newkeyid1");
            String incidentFindId = "";
            String actionPlanIdForIncident = "";
            String actionPlanIdForFindings = "";
            this.logger.info("RecordIncident: Created new Incident sdi = " + incidentId);
            HashMap<String, String> hmIncdtFindKeys = new HashMap<String, String>();
            if (incidentTemplateId.length() > 0) {
                try {
                    this.logger.info("RecordIncident: Getting Incident Finding details for the incident template = " + incidentTemplateId);
                    DataSet dsIncident_Finding = (DataSet)hmDatasets.get("Incident_Finding");
                    String incident_findings = "'" + dsIncident_Finding.getColumnValues("incidentfindid", "','") + "'";
                    int noOfFindings = dsIncident_Finding.getRowCount();
                    hmIncdtFindKeys.put("oldIncdtFindKeys", dsIncident_Finding.getColumnValues("incidentfindid", ";"));
                    try {
                        PropertyList plIncdtFind = new PropertyList();
                        plIncdtFind.setProperty("sdcid", "LV_IncdtFind");
                        plIncdtFind.setProperty("incidentid", incidentId);
                        incidentFindId = this.doAddSdi(dsIncident_Finding, plIncdtFind, "incidentfindid", "");
                        hmIncdtFindKeys.put("newIncdtFindKeys", incidentFindId);
                        this.logger.info("RecordIncident: Created the finding sdi: " + incidentFindId);
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an incident finding for the incident. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                    }
                    this.logger.info("RecordIncident: Getting Incident ActionPlan details for the incident template (and finding is null) = " + incidentTemplateId);
                    DataSet dsIncident_ActionPlan = (DataSet)hmDatasets.get("Incident_ActionPlan");
                    String incident_actionPlan = dsIncident_ActionPlan.getColumnValues("actionplanid", ",");
                    String incident_actionPlans = "'" + dsIncident_ActionPlan.getColumnValues("actionplanid", "','") + "'";
                    try {
                        PropertyList plActionPlan = new PropertyList();
                        plActionPlan.setProperty("sdcid", "LV_ActionPlan");
                        plActionPlan.setProperty("incidentid", incidentId);
                        plActionPlan.setProperty("incidentfindid", "(null)");
                        actionPlanIdForIncident = this.doAddSdi(dsIncident_ActionPlan, plActionPlan, "actionplanid", "");
                        this.logger.info("RecordIncident: Created the ActionPlan sdi for the incident: " + actionPlanIdForIncident);
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an action plan for the incident. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                    }
                    this.logger.info("RecordIncident: Getting Incident Workorder details for the incident template = " + incidentTemplateId);
                    DataSet dsIncident_Workorder = (DataSet)hmDatasets.get("Incident_Workorder");
                    String incidentWorkorders = "";
                    try {
                        PropertyList plWorkOrder = new PropertyList();
                        plWorkOrder.setProperty("sdcid", "WorkOrderSDC");
                        plWorkOrder.setProperty("sourcekeyid1", incidentId);
                        incidentWorkorders = this.doAddSdi(dsIncident_Workorder, plWorkOrder, "workorderid", "");
                        this.logger.info("RecordIncident: Created the Workorder sdi for the incident: " + incidentWorkorders);
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create a work order for the incident. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                    }
                    this.logger.info("RecordIncident: Getting action plans for the findings");
                    String[] allOldIncdtFindKeys = StringUtil.split(hmIncdtFindKeys.get("oldIncdtFindKeys").toString(), ";");
                    String[] allNewIncdtFindKeys = StringUtil.split(hmIncdtFindKeys.get("newIncdtFindKeys").toString(), ";");
                    DataSet dsFinding_ActionPlan = (DataSet)hmDatasets.get("Finding_ActionPlan");
                    String finding_actionPlans = "'" + dsFinding_ActionPlan.getColumnValues("actionplanid", "','") + "'";
                    String finding_actionPlan = dsFinding_ActionPlan.getColumnValues("actionplanid", ",");
                    if (dsFinding_ActionPlan.getRowCount() > 0) {
                        try {
                            PropertyList plActionPlan1 = new PropertyList();
                            plActionPlan1.setProperty("sdcid", "LV_ActionPlan");
                            dsFinding_ActionPlan = this.replaceColumnValue(dsFinding_ActionPlan, "incidentfindid", allOldIncdtFindKeys, allNewIncdtFindKeys);
                            plActionPlan1.setProperty("incidentid", incidentId);
                            actionPlanIdForFindings = this.doAddSdi(dsFinding_ActionPlan, plActionPlan1, "actionplanid", "incidentfindid");
                            this.logger.info("RecordIncident: Created the ActionPlan sdi for the Finding: " + actionPlanIdForFindings);
                        }
                        catch (SapphireException se) {
                            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an action plan for the incident findings. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                        }
                    }
                    this.logger.info("RecordIncident: Getting workorders for the findings");
                    DataSet dsFinding_Workorder = (DataSet)hmDatasets.get("Finding_Workorder");
                    if (dsFinding_Workorder.getRowCount() > 0) {
                        try {
                            String workorderId = "";
                            PropertyList plWorkOrder2 = new PropertyList();
                            plWorkOrder2.setProperty("sdcid", "WorkOrderSDC");
                            dsFinding_Workorder = this.replaceColumnValue(dsFinding_Workorder, "sourcekeyid1", allOldIncdtFindKeys, allNewIncdtFindKeys);
                            workorderId = this.doAddSdi(dsFinding_Workorder, plWorkOrder2, "workorderid", "sourcekeyid1");
                            this.logger.info("RecordIncident: Created the Workorders for the findings: " + workorderId);
                        }
                        catch (SapphireException se) {
                            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an work order for the incident findings. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                        }
                    }
                    this.logger.info("RecordIncident: Getting workorders for the action plans for the findings");
                    String[] allOldActionPlanIdForFindings = StringUtil.split(finding_actionPlan, ",");
                    String[] allNewActionPlanIdForFindings = StringUtil.split(actionPlanIdForFindings, ";");
                    DataSet dsFinding_ActionPlan_Workorder = (DataSet)hmDatasets.get("Finding_ActionPlan_Workorder");
                    if (dsFinding_ActionPlan_Workorder.getRowCount() > 0) {
                        try {
                            String workorderId = "";
                            PropertyList plWorkOrder3 = new PropertyList();
                            plWorkOrder3.setProperty("sdcid", "WorkOrderSDC");
                            dsFinding_ActionPlan_Workorder = this.replaceColumnValue(dsFinding_ActionPlan_Workorder, "sourcekeyid1", allOldActionPlanIdForFindings, allNewActionPlanIdForFindings);
                            workorderId = this.doAddSdi(dsFinding_ActionPlan_Workorder, plWorkOrder3, "workorderid", "sourcekeyid1");
                            this.logger.info("RecordIncident: Created the Workorders for the actionplans for the findings: " + workorderId);
                        }
                        catch (SapphireException se) {
                            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an work order for the actionplans (for the findings). Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                        }
                    }
                    this.logger.info("RecordIncident: Getting workorders for the action plans for the incident");
                    String[] allOldIncident_actionPlans = StringUtil.split(incident_actionPlan, ",");
                    String[] allNewIncident_actionPlans = StringUtil.split(actionPlanIdForIncident, ";");
                    DataSet dsIncident_ActionPlan_Workorder = (DataSet)hmDatasets.get("Incident_ActionPlan_Workorder");
                    if (dsIncident_ActionPlan_Workorder.getRowCount() <= 0) break block33;
                    try {
                        String workorderId = "";
                        PropertyList plWorkOrder4 = new PropertyList();
                        plWorkOrder4.setProperty("sdcid", "WorkOrderSDC");
                        dsIncident_ActionPlan_Workorder = this.replaceColumnValue(dsIncident_ActionPlan_Workorder, "sourcekeyid1", allOldIncident_actionPlans, allNewIncident_actionPlans);
                        workorderId = this.doAddSdi(dsIncident_ActionPlan_Workorder, plWorkOrder4, "workorderid", "sourcekeyid1");
                        this.logger.info("RecordIncident: Created the Workorders for the actionplans for the incidents: " + workorderId);
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create an work order for the actionplans (for the incident). Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Error : Failed to retrieve data for the Incident template. Got an exception :" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            }
        }
        if (sourceKeyId1.length() > 0) {
            String[] arrSourceKeyid1 = StringUtil.split(sourceKeyId1, ";");
            int numIncidentItems = arrSourceKeyid1.length;
            StringBuffer sbIncidentItemid = new StringBuffer();
            for (int k = 0; k < numIncidentItems; ++k) {
                String sequence = String.valueOf(k + 1);
                int obtainedLength = sequence.length();
                for (int seqlength = 5; seqlength > obtainedLength; --seqlength) {
                    sequence = "0" + sequence;
                }
                String generatedSequence = sequence;
                sbIncidentItemid.append(";").append(incidentId).append("-").append(generatedSequence);
            }
            DataSet dsInsert = new DataSet();
            dsInsert.addColumnValues("incidentitemid", 0, sbIncidentItemid.toString().substring(1), ";");
            dsInsert.addColumnValues("incidentid", 0, incidentId, ";");
            dsInsert.addColumnValues("sourcesdcid", 0, sourcesdcid, ";");
            dsInsert.addColumnValues("sourcekeyid1", 0, sourceKeyId1, ";");
            dsInsert.addColumnValues("sourcekeyid2", 0, sourceKeyId2, ";");
            dsInsert.addColumnValues("sourcekeyid3", 0, sourceKeyId3, ";");
            dsInsert.addColumnValues("paramid", 0, paramid, ";");
            dsInsert.addColumnValues("paramlistid", 0, paramlistId, ";");
            dsInsert.addColumnValues("paramlistversionid", 0, paramlistVersionId, ";");
            dsInsert.addColumnValues("paramtype", 0, paramType, ";");
            dsInsert.addColumnValues("dataset", 1, dataset, ";");
            dsInsert.addColumnValues("variantid", 0, variantId, ";");
            dsInsert.addColumnValues("replicateid", 0, replicatedId, ";");
            dsInsert.addColumnValues("causalobjectflag", 0, causalObjectFlag, ";");
            dsInsert.padColumns();
            dsInsert.setSequence("usersequence");
            try {
                DataSetUtil.insert(this.database, dsInsert, "IncidentItem");
                String initialStatusProp = property.getProperty("initialstatus", "");
                if (initialStatusProp.length() == 0) {
                    DataSet incidentinfo = (DataSet)hmDatasets.get("Incident");
                    initialStatusProp = incidentinfo != null && incidentinfo.getRowCount() > 0 ? incidentinfo.getString(0, "incidentstatus", "Initial") : "Initial";
                }
                this.setInitialIncidentStatus(incidentId, initialStatusProp);
            }
            catch (SapphireException se) {
                throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Failed to create incident items. Got an exception:") + " " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
            }
        }
    }

    private void setInitialIncidentStatus(String incidentId, String initialStatus) {
        ActionProcessor ap = this.getActionProcessor();
        PropertyList property = new PropertyList();
        property.put("sdcid", "LV_Incdt");
        property.put("keyid1", incidentId);
        property.setProperty("incidentstatus", initialStatus);
        try {
            ap.processAction("EditSDI", "1", property);
        }
        catch (ActionException e) {
            this.logger.error("Error running EditSDI. Could not set the initial status on the incident.", e);
        }
    }
}

