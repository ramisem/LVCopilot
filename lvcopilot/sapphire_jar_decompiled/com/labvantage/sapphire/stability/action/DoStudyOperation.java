/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class DoStudyOperation
extends BaseAction {
    private TranslationProcessor Tp;
    public static final String PROPERTY_STUDYID = "studyid";

    @Override
    public int processAction(String actionId, String versionId, HashMap props) {
        this.Tp = this.getTranslationProcessor();
        String studyIds = (String)props.get(PROPERTY_STUDYID);
        String requestedStatus = "";
        if (studyIds.length() == 0) {
            this.logError(this.Tp.translate("ERROR:StudyId not specified."));
            return 2;
        }
        if (actionId.equalsIgnoreCase("StartStudy")) {
            requestedStatus = "Start";
        } else if (actionId.equalsIgnoreCase("SuspendStudy")) {
            requestedStatus = "Suspend";
        } else if (actionId.equalsIgnoreCase("ResumeStudy")) {
            requestedStatus = "Resume";
        } else if (actionId.equalsIgnoreCase("CancelStudy")) {
            requestedStatus = "Cancel";
        } else if (actionId.equalsIgnoreCase("CompleteStudy")) {
            requestedStatus = "Complete";
        }
        try {
            this.doOperation(studyIds, requestedStatus, props);
        }
        catch (Exception ex) {
            this.logError(this.Tp.translate("Failed to process action") + " " + actionId);
            return 2;
        }
        return 1;
    }

    private void doOperation(String studyids, String requestedState, HashMap props) throws SapphireException {
        this.Tp = this.getTranslationProcessor();
        String message = "";
        String newStudyStatus = "";
        String nextCondStatus = "";
        String currentCondStatus = "";
        if (requestedState.equalsIgnoreCase("Start")) {
            currentCondStatus = "R";
            nextCondStatus = "";
            newStudyStatus = "A";
        }
        if (requestedState.equalsIgnoreCase("Suspend")) {
            currentCondStatus = "S";
            nextCondStatus = "";
            newStudyStatus = "S";
        }
        if (requestedState.equalsIgnoreCase("Resume")) {
            currentCondStatus = "R";
            nextCondStatus = "";
            newStudyStatus = "A";
        }
        if (requestedState.equalsIgnoreCase("Cancel")) {
            currentCondStatus = "X";
            nextCondStatus = "";
            newStudyStatus = "X";
        }
        if (requestedState.equalsIgnoreCase("Complete")) {
            currentCondStatus = "C";
            nextCondStatus = "";
            newStudyStatus = "C";
        }
        try {
            ScheduleService scheduler = new ScheduleService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            SafeSQL safeSQL = new SafeSQL();
            String inClauseStudyString = this.database.isOracle() ? OpalUtil.getSqlWhereClause(studyids) : OpalUtil.getSqlWhereClause(studyids, true);
            String sqlSchedulePlanId = "select scheduleplanid,scheduleconditionid,startdt from schedulecondition  where scheduleplanid in (select scheduleplanid from study_scheduleplan where studyid in (" + safeSQL.addIn(inClauseStudyString) + ")) order by usersequence";
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String sqlStartDate = "UPDATE schedulecondition set startdt = ?, moddt = ?, modby = ?, modtool=?  where scheduleplanid = ? and scheduleconditionid = ? ";
            PreparedStatement insertStartDate = this.database.prepareStatement("insertstartdate", sqlStartDate);
            QueryProcessor qp = this.getQueryProcessor();
            String scheduleplanid = "";
            String scheduleconditionid = "";
            String startdt = "";
            DataSet dsSchedulePlanId = qp.getPreparedSqlDataSet(sqlSchedulePlanId, safeSQL.getValues());
            if (dsSchedulePlanId.size() > 0) {
                for (int i = 0; i < dsSchedulePlanId.size(); ++i) {
                    startdt = dsSchedulePlanId.getValue(i, "startdt", "");
                    scheduleplanid = dsSchedulePlanId.getValue(i, "scheduleplanid", "");
                    scheduleconditionid = dsSchedulePlanId.getValue(i, "scheduleconditionid", "");
                    if (startdt.length() > 0) continue;
                    insertStartDate.setTimestamp(1, now);
                    insertStartDate.setTimestamp(2, now);
                    insertStartDate.setString(3, this.connectionInfo.getSysuserId());
                    insertStartDate.setString(4, this.connectionInfo.getTool());
                    insertStartDate.setString(5, scheduleplanid);
                    insertStartDate.setString(6, scheduleconditionid);
                    insertStartDate.executeUpdate();
                }
            }
            this.database.closeStatement("insertstartdate");
            String updateScheduleCondition = "UPDATE schedulecondition set nextconditionstatus = ?, conditionstatus = ?, moddt = ?, modby = ?, modtool=? where scheduleplanid in ( select scheduleplanid from study_scheduleplan where studyid in (" + inClauseStudyString + "))";
            PreparedStatement statement = this.database.prepareStatement("Update ScheduleCondition", updateScheduleCondition);
            statement.setString(1, nextCondStatus);
            statement.setString(2, currentCondStatus);
            statement.setTimestamp(3, now);
            statement.setString(4, this.connectionInfo.getSysuserId());
            statement.setString(5, this.connectionInfo.getTool());
            statement.executeUpdate();
            this.database.closeStatement("Update ScheduleCondition");
            if (dsSchedulePlanId.size() > 0) {
                for (int i = 0; i < dsSchedulePlanId.size(); ++i) {
                    scheduleplanid = dsSchedulePlanId.getValue(i, "scheduleplanid", "");
                    scheduleconditionid = dsSchedulePlanId.getValue(i, "scheduleconditionid", "");
                    if (currentCondStatus.equals("R")) {
                        scheduler.scheduleEvents(scheduleplanid, scheduleconditionid, null);
                        continue;
                    }
                    if (currentCondStatus.equals("S")) {
                        scheduler.deleteEvents(scheduleplanid, scheduleconditionid, null);
                        continue;
                    }
                    if (currentCondStatus.equals("C")) {
                        scheduler.deleteEvents(scheduleplanid, scheduleconditionid, null);
                        continue;
                    }
                    if (!currentCondStatus.equals("X")) continue;
                    scheduler.deleteEvents(scheduleplanid, scheduleconditionid, null);
                }
            }
            PropertyList actionProps = new PropertyList();
            actionProps.put("sdcid", "StudySDC");
            actionProps.put("keyid1", studyids);
            actionProps.put("approvalrequiredflag", "N");
            actionProps.put("studystatus", newStudyStatus);
            String auditreason = (String)props.get("auditreason");
            String auditactivity = (String)props.get("auditactivity");
            String auditsignedflag = (String)props.get("auditsignedflag");
            actionProps.put("auditreason", auditreason != null && auditreason.length() > 0 ? auditreason : "");
            actionProps.put("auditactivity", auditactivity != null && auditactivity.length() > 0 ? auditactivity : "");
            actionProps.put("auditsignedflag", auditsignedflag != null && auditsignedflag.length() > 0 ? auditsignedflag : "N");
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", e);
        }
    }
}

