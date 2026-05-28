/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class StudySDC
extends BaseSDCRules {
    private static final String STUDYID = "studyid";
    private static final String STUDYSUITEID = "studysuiteid";
    private static final String STUDYSTATUS = "studystatus";
    private static final String STUDYSUITESTATUS = "studysuitestatus";
    private static final String STUDYSUITESDC = "LV_StudySuiteSDC";
    private static final String PROPERTY_SDCID = "sdcid";
    private static final String PROPERTY_KEYID1 = "keyid1";
    private static final boolean DEBUG = false;
    private static final String COMPLETEDDT = "completeddt";

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet plans = sdiData.getDataset("study_scheduleplan");
        if (plans != null) {
            try {
                for (int i = 0; i < plans.size(); ++i) {
                    String planid = plans.getString(i, "scheduleplanid");
                    String newplanid = ScheduleGridUtil.copyPlan(this.getConnectionId(), planid, "Y");
                    plans.setString(i, "scheduleplanid", newplanid);
                }
            }
            catch (Exception e) {
                this.throwError("PasswordValidate", "VALIDATION", "Unable to validate password format. Exception thrown: " + e.getMessage());
            }
        }
        this.resolveProductVersion(sdiData.getDataset("primary"));
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT scheduleplanid FROM study_scheduleplan WHERE studyid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() > 0) {
            StringBuffer planlist = new StringBuffer();
            for (int i = 0; i < ds.size(); ++i) {
                planlist.append(";").append(ds.getString(i, "scheduleplanid"));
            }
            HashMap<String, String> props = new HashMap<String, String>();
            props.put(PROPERTY_SDCID, "SchedulePlan");
            props.put(PROPERTY_KEYID1, planlist.substring(1));
            props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
        this.updateStudySuiteStatus(actionProps);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkIfStudyExistsInSuite(sdiData);
        this.updateStudySuiteStatus(actionProps);
        this.updateStudyCompleteDt(sdiData);
        if (!actionProps.containsKey("__sdcruleconfirm")) {
            this.checkIfApprovalExists(sdiData.getDataset("primary"));
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.updateStudySuiteStatus(sdiData);
        QueryProcessor qp = this.getQueryProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            DataSet dsTrackItems;
            String studyId = primary.getString(i, STUDYID, "");
            String oldContainer = oldPrimary.getValue(i, "containertypeid");
            String newContainer = primary.getValue(i, "containertypeid");
            String oldStudyStatus = oldPrimary.getValue(i, STUDYSTATUS);
            String newStudyStatus = primary.getValue(i, STUDYSTATUS);
            if ((newContainer.length() <= 0 || newContainer.equals(oldContainer)) && (newStudyStatus.equals(oldStudyStatus) || !"A".equals(newStudyStatus))) continue;
            DataSet dsPlans = this.getStudyPlans(studyId);
            if (dsPlans != null && dsPlans.getRowCount() > 0) {
                this.validatePlanUnits(dsPlans, qp, tp);
            }
            if ((dsTrackItems = this.getStudyTrackItems(studyId)) == null || dsTrackItems.getRowCount() <= 0) continue;
            StabilityUtil.validateTrackItemUnits(dsTrackItems, qp, tp);
            if (newContainer.length() <= 0 || newContainer.equals(oldContainer)) continue;
            this.updateTrackItemContainer(dsTrackItems.getColumnValues("trackitemid", ";"), newContainer);
        }
    }

    private void updateTrackItemContainer(String trackItems, String containerTypeId) throws SapphireException {
        PropertyList props = new PropertyList();
        props.put(PROPERTY_SDCID, "TrackItemSDC");
        props.put(PROPERTY_KEYID1, trackItems);
        props.put("containertypeid", containerTypeId);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsPlans = sdiData.getDataset("study_scheduleplan");
        if (dsPlans != null && dsPlans.getRowCount() > 0) {
            this.validatePlanUnits(dsPlans, this.getQueryProcessor(), this.getTranslationProcessor());
        }
    }

    private void checkIfStudyExistsInSuite(SDIData sdiData) {
        DataSet initialPrimary = this.getBeforeEditImage().getDataset("primary");
        DataSet primary = sdiData.getDataset("primary");
        String ruleMsg = "";
        String initStudySuiteId = "";
        String newStudySuiteId = "";
        String initStudyId = "";
        TranslationProcessor tp = this.getTranslationProcessor();
        if (initialPrimary != null && initialPrimary.size() > 0) {
            for (int i = 0; i < initialPrimary.size(); ++i) {
                initStudySuiteId = initialPrimary.getValue(i, STUDYSUITEID);
                initStudyId = initialPrimary.getValue(i, STUDYID);
                newStudySuiteId = primary.getValue(i, STUDYSUITEID);
                if (initStudySuiteId == null || initStudySuiteId.length() <= 0 || newStudySuiteId == null || newStudySuiteId.length() <= 0 || initStudySuiteId.equalsIgnoreCase(newStudySuiteId)) continue;
                ruleMsg = "ERROR:The Study:" + initStudyId + " is a part of another Study Suite:" + initStudySuiteId + ".So you can not associate this Study with more than one Study Suite.";
                this.setError("ERROR:In StudySDC Rule->checkIfStudyExistsInSuite( SDIData sdiData )->", "VALIDATION", tp.translate(ruleMsg));
            }
        }
    }

    private void updateStudySuiteStatus(PropertyList actionProps) throws SapphireException {
        String keyid = actionProps.getProperty(PROPERTY_KEYID1, "");
        QueryProcessor qp = this.getQueryProcessor();
        if (keyid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select distinct studysuiteid from study where studyid in (" + safeSQL.addIn(keyid, ";") + ")";
            DataSet dsStudy = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (dsStudy.size() > 0) {
                for (int i = 0; i < dsStudy.size(); ++i) {
                    String studySuiteId = dsStudy.getValue(i, STUDYSUITEID, "");
                    this.updateStudySuiteStatus(studySuiteId, StringUtil.split(keyid, ";"));
                }
            }
        }
    }

    private void updateStudySuiteStatus(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        QueryProcessor qp = this.getQueryProcessor();
        String sql = "";
        DataSet dsStudy = null;
        String initialStudySuiteId = "";
        String initialStudyStatus = "";
        String primaryStudyId = "";
        String primaryStudySuiteId = "";
        String primaryStudyStatus = "";
        if (primary != null && primary.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                primaryStudyId = primary.getValue(i, STUDYID);
                primaryStudySuiteId = primary.getValue(i, STUDYSUITEID);
                primaryStudyStatus = primary.getValue(i, STUDYSTATUS);
                SafeSQL safeSQL = new SafeSQL();
                sql = "select studysuiteid, studystatus from study where studyid=" + safeSQL.addVar(primaryStudyId);
                dsStudy = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (dsStudy.size() <= 0) continue;
                initialStudySuiteId = dsStudy.getValue(0, STUDYSUITEID, "");
                initialStudyStatus = dsStudy.getValue(0, STUDYSTATUS, "");
                if ((initialStudySuiteId == null || initialStudySuiteId.length() <= 0 || initialStudySuiteId.equalsIgnoreCase(primaryStudySuiteId)) && initialStudyStatus.equalsIgnoreCase(primaryStudyStatus)) continue;
                try {
                    this.updateStudySuiteStatus(initialStudySuiteId, null);
                    continue;
                }
                catch (SapphireException e) {
                    this.logger.stackTrace(e);
                }
            }
        }
    }

    private void updateStudySuiteStatus(String studysuiteid, String[] selectedStudyIds) throws SapphireException {
        String sql = "";
        ArrayList<String> alStudyId = new ArrayList<String>();
        String message = "";
        if (selectedStudyIds != null && selectedStudyIds.length > 0) {
            for (int i = 0; i < selectedStudyIds.length; ++i) {
                alStudyId.add(selectedStudyIds[i]);
            }
        }
        if (studysuiteid != null && studysuiteid.length() > 0) {
            String currentStudySuiteStatus;
            SafeSQL safeSQL = new SafeSQL();
            sql = "select studyid ,studystatus, studysuitestatus from study, studysuite where studysuite.studysuiteid = study.studysuiteid and studysuite.studysuiteid =" + safeSQL.addVar(studysuiteid);
            QueryProcessor qp = this.getQueryProcessor();
            DataSet dsStudy = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (!(dsStudy.size() <= 0 || (currentStudySuiteStatus = dsStudy.getValue(0, STUDYSUITESTATUS)).equalsIgnoreCase("Q") || currentStudySuiteStatus.equalsIgnoreCase("P") || currentStudySuiteStatus.equalsIgnoreCase("R"))) {
                String studySuiteStatus = "";
                String studyId = "";
                String studyStatus = "";
                int notStartedStatus = 0;
                int activeStatus = 0;
                int suspendedStatus = 0;
                int cancelledStatus = 0;
                int completedStatus = 0;
                int totalStudyCount = 0;
                for (int i = 0; i < dsStudy.size(); ++i) {
                    studyStatus = dsStudy.getValue(i, STUDYSTATUS, "");
                    if (selectedStudyIds != null && selectedStudyIds.length > 0 && alStudyId.contains(studyId = dsStudy.getValue(i, STUDYID, ""))) continue;
                    ++totalStudyCount;
                    if (studyStatus.equalsIgnoreCase("N")) {
                        ++notStartedStatus;
                        continue;
                    }
                    if (studyStatus.equalsIgnoreCase("A")) {
                        ++activeStatus;
                        continue;
                    }
                    if (studyStatus.equalsIgnoreCase("S")) {
                        ++suspendedStatus;
                        continue;
                    }
                    if (studyStatus.equalsIgnoreCase("X")) {
                        ++cancelledStatus;
                        continue;
                    }
                    if (!studyStatus.equalsIgnoreCase("C")) continue;
                    ++completedStatus;
                }
                studySuiteStatus = "A";
                if (totalStudyCount == notStartedStatus) {
                    studySuiteStatus = "N";
                } else if (totalStudyCount == suspendedStatus) {
                    studySuiteStatus = "S";
                } else if (totalStudyCount == cancelledStatus) {
                    studySuiteStatus = "X";
                } else if (totalStudyCount == completedStatus) {
                    studySuiteStatus = "C";
                } else if (activeStatus >= 1 || suspendedStatus >= 1) {
                    studySuiteStatus = "A";
                } else if (completedStatus > 0 && cancelledStatus > 0 && totalStudyCount == completedStatus + cancelledStatus) {
                    studySuiteStatus = "C";
                }
                try {
                    PropertyList props = new PropertyList();
                    props.put(PROPERTY_SDCID, STUDYSUITESDC);
                    props.put(PROPERTY_KEYID1, studysuiteid);
                    props.put(STUDYSUITESTATUS, studySuiteStatus);
                    ActionProcessor ap = this.getActionProcessor();
                    ActionBlock ab = new ActionBlock();
                    ab.setAction("updatesuitestatus", "EditSDI", "1");
                    ab.setActionProperties("updatesuitestatus", props);
                    ap.processActionBlock(ab);
                }
                catch (Exception e) {
                    message = "ERROR:Exception generated in invoking EditSDI=>" + e.getMessage();
                    throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate(message), e);
                }
            }
        }
    }

    @Override
    public void postApprove(DataSet dsApproval) throws SapphireException {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String studyStatus = "Pass".equalsIgnoreCase(approvalFlag) ? "N" : "R";
                dsProp.setString(newRow, PROPERTY_KEYID1, approvedDS.getValue(i, PROPERTY_KEYID1));
                dsProp.setString(newRow, STUDYSTATUS, studyStatus);
                dsProp.setString(newRow, "tracelogid", approvedDS.getValue(i, "tracelogid"));
            }
            if (dsProp.size() > 0) {
                String sdcId = "StudySDC";
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.put(PROPERTY_SDCID, sdcId);
                props.put(PROPERTY_KEYID1, dsProp.getColumnValues(PROPERTY_KEYID1, ";"));
                props.put(STUDYSTATUS, dsProp.getColumnValues(STUDYSTATUS, ";"));
                props.put("tracelogid", dsProp.getValue(0, "tracelogid"));
                actionProcessor.processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    private void resolveProductVersion(DataSet primary) throws SapphireException {
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < primary.size(); ++i) {
            String productId = primary.getString(i, "s_productid", "");
            String prodVersion = primary.getString(i, "s_productversionid", "");
            if (productId.length() <= 0 || prodVersion.length() != 0) continue;
            if (!map.containsKey(productId)) {
                prodVersion = SdiInfo.getCurrentVersion("Product", productId, null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                map.put(productId, prodVersion);
            }
            primary.setString(i, "s_productversionid", (String)map.get(productId));
        }
    }

    private void validatePlanUnits(DataSet dsPlans, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        HashMap<String, String> mapStudyContainer = new HashMap<String, String>();
        for (int i = 0; i < dsPlans.getRowCount(); ++i) {
            DataSet dsWorkItems;
            DataSet dsPlanItems;
            String planId = dsPlans.getValue(i, "scheduleplanid");
            String studyId = dsPlans.getValue(i, STUDYID);
            String containerUnit = "";
            if (!mapStudyContainer.containsKey(studyId)) {
                containerUnit = this.getStudyContainerUnit(studyId);
                mapStudyContainer.put(studyId, containerUnit);
            } else {
                containerUnit = (String)mapStudyContainer.get(studyId);
            }
            if (containerUnit.length() == 0) continue;
            DataSet dsPlanConditions = StabilityUtil.getPlanConditions(planId, qp);
            if (dsPlanConditions.getRowCount() > 0) {
                StabilityUtil.validatePlanConditionUnits(dsPlanConditions, containerUnit, qp, tp);
            }
            if ((dsPlanItems = StabilityUtil.getPlanItems(planId, qp)).getRowCount() > 0) {
                StabilityUtil.validatePlanItemPullAmountUnits(dsPlanItems, containerUnit, qp, tp, "Container");
            }
            if ((dsWorkItems = StabilityUtil.getPlanItemWorkItems(planId, qp)).getRowCount() <= 0) continue;
            StabilityUtil.validatePlanItemWorkItemUnits(dsWorkItems, containerUnit, qp, tp);
        }
    }

    private DataSet getStudyPlans(String studyId) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("getStudyPlans", "Select distinct studyid, scheduleplanid from study_scheduleplan where studyid=?", new Object[]{studyId});
        return ds;
    }

    private String getStudyContainerUnit(String studyId) throws SapphireException {
        String sizeUnit = "";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("getStudyContainerSize", "select c.sizeunits  FROM study s, containertype c  where c.containertypeid = s.containertypeid and s.studyid=?", new Object[]{studyId});
        if (ds != null && ds.getRowCount() > 0) {
            sizeUnit = ds.getValue(0, "sizeunits");
        }
        return sizeUnit;
    }

    private DataSet getStudyTrackItems(String studyId) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("getStudyPlans", "Select t.trackitemid,t.qtyunits FROM trackitem t, schedulecondition_trackitem sct, study_scheduleplan sp WHERE t.trackitemid = sct.trackitemid and sct.scheduleplanid = sp.scheduleplanid and sp.studyid=?", new Object[]{studyId});
        return ds;
    }

    private void checkIfApprovalExists(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String studyId;
            if (!this.hasPrimaryValueChanged(primary, i, STUDYSTATUS) || !"P".equalsIgnoreCase(primary.getValue(i, STUDYSTATUS, "")) || this.database.getPreparedCount("select count(1) from sdiapproval where sdcid = 'StudySDC' and keyid1 = ?", new Object[]{studyId = primary.getValue(i, STUDYID, "")}) != 0) continue;
            throw new SapphireException("Confirm Submit For Approval", "CONFIRM", this.getTranslationProcessor().translate("No approval type is associated with the Study. Continue anyway?"));
        }
    }

    private void updateStudyCompleteDt(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null && primary.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, STUDYSTATUS)) continue;
                if (primary.getValue(i, STUDYSTATUS, "").equalsIgnoreCase("C")) {
                    primary.setDate(i, COMPLETEDDT, "n");
                    continue;
                }
                primary.setDate(i, COMPLETEDDT, "(null)");
            }
        }
    }
}

