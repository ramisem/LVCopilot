/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AutoCompleteStudies
extends BaseAction
implements sapphire.action.AutoCompleteStudies {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block20: {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT DISTINCT st.studyid, stpl.scheduleplanid, sc.scheduleconditionid, spi.scheduletimeruleid,");
            sql.append(" spi.scheduleplanitemid, pr.objectname");
            sql.append(" FROM study st, study_scheduleplan stpl, scheduleevent  se, schedulecondition sc, propertytree pr,");
            sql.append(" scheduleplanitem spi WHERE  stpl.studyid = st.studyid AND se.scheduleplanid = stpl.scheduleplanid ");
            sql.append(" AND sc.scheduleplanid = stpl.scheduleplanid  AND spi.scheduleplanid = stpl.scheduleplanid ");
            sql.append(" AND spi.scheduleconditionid = sc.scheduleconditionid AND st.studystatus = 'A'");
            sql.append(" AND pr.propertytreeid = spi.propertytreeid AND pr.propertytreetype = 'ScheduleTask'");
            sql.append(" AND ( st.disableautocompleteflag is null OR st.disableautocompleteflag != 'Y' )");
            sql.append(" AND se.eventstatus = 'D' AND sc.conditionstatus = 'R' AND   NOT EXISTS(SELECT 1 FROM scheduleevent WHERE scheduleplanid = se.scheduleplanid AND eventstatus='S')");
            sql.append(" ORDER BY st.studyid, stpl.scheduleplanid, sc.scheduleconditionid, spi.scheduletimeruleid desc");
            try {
                QueryProcessor qp = this.getQueryProcessor();
                DataSet dsStudy = qp.getSqlDataSet(sql.toString());
                if (dsStudy == null || dsStudy.getRowCount() <= 0) break block20;
                String currentStudy = "";
                String currentSchedulePlan = "";
                String currentSchedulePlanItem = "";
                String currentCondition = "";
                String objectname = "";
                boolean isComplete = true;
                ArrayList<String> alStudyIncomp = new ArrayList<String>();
                StringBuffer sbStudyId = new StringBuffer();
                String updateScheduleConditionSQL = "UPDATE schedulecondition SET conditionstatus = 'C', moddt = ?, modby = ?, modtool = '" + this.connectionInfo.getTool() + "'  WHERE scheduleplanid = ? and scheduleconditionid = ?";
                PreparedStatement updateScheduleConditionPS = this.database.prepareStatement("updateschedulecondition", updateScheduleConditionSQL);
                int i = 0;
                while (i < dsStudy.size()) {
                    currentStudy = dsStudy.getValue(i, "studyid", "");
                    currentSchedulePlan = dsStudy.getValue(i, "scheduleplanid", "");
                    currentCondition = dsStudy.getValue(i, "scheduleconditionid", "");
                    currentSchedulePlanItem = dsStudy.getValue(i, "scheduleplanitemid", "");
                    objectname = dsStudy.getValue(i, "objectname", "");
                    BaseScheduleTask task = null;
                    try {
                        Class<?> c = Class.forName(objectname);
                        Object o = c.newInstance();
                        task = (BaseScheduleTask)o;
                    }
                    catch (Exception e) {
                        Trace.logError("Error creating object: " + objectname);
                    }
                    isComplete = task.isComplete(currentSchedulePlan, currentSchedulePlanItem, this.database);
                    if (!isComplete) {
                        alStudyIncomp.add(currentStudy);
                        ++i;
                        while (i < dsStudy.size() && dsStudy.getValue(i, "studyid", "").equals(currentStudy) && dsStudy.getValue(i, "scheduleconditionid", "").equals(currentCondition)) {
                            ++i;
                        }
                        continue;
                    }
                    if (i + 1 == dsStudy.size() || !dsStudy.getValue(i + 1, "studyid", "").equals(currentStudy)) {
                        updateScheduleConditionPS.setTimestamp(1, new Timestamp(DateTimeUtil.getNowCalendar().getTime().getTime()));
                        updateScheduleConditionPS.setString(2, this.connectionInfo.getSysuserId());
                        updateScheduleConditionPS.setString(3, currentSchedulePlan);
                        updateScheduleConditionPS.setString(4, currentCondition);
                        try {
                            updateScheduleConditionPS.executeUpdate();
                        }
                        catch (Exception e) {
                            throw new SapphireException("PROCESSACTION_FAILED", "Exception generated on trying to update status ScheduleCondition: AutoCompleteStudies=>" + e.getMessage(), e);
                        }
                        if (!alStudyIncomp.contains(currentStudy)) {
                            sbStudyId.append(currentStudy + ";");
                        }
                    } else if (!dsStudy.getValue(i + 1, "scheduleconditionid", "").equals(currentCondition) || !dsStudy.getValue(i + 1, "scheduleplanid", "").equals(currentSchedulePlan)) {
                        updateScheduleConditionPS.setTimestamp(1, new Timestamp(DateTimeUtil.getNowCalendar().getTime().getTime()));
                        updateScheduleConditionPS.setString(2, this.connectionInfo.getSysuserId());
                        updateScheduleConditionPS.setString(3, currentSchedulePlan);
                        updateScheduleConditionPS.setString(4, currentCondition);
                        try {
                            updateScheduleConditionPS.executeUpdate();
                        }
                        catch (Exception e) {
                            throw new SapphireException("PROCESSACTION_FAILED", "Exception generated trying on to update status of ScheduleCondition: AutoCompleteStudies=>" + e.getMessage(), e);
                        }
                    }
                    ++i;
                }
                if (sbStudyId.length() > 0) {
                    sbStudyId.setLength(sbStudyId.length() - 1);
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("sdcid", "StudySDC");
                    hm.put("keyid1", sbStudyId.toString());
                    hm.put("studystatus", "C");
                    this.getActionProcessor().processAction("EditSDI", "1", hm);
                    if (Trace.on) {
                        Trace.log("Completed following Studies :" + sbStudyId.toString());
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not process action: AutoCompleteStudies=>" + e.getMessage(), e);
            }
            finally {
                this.database.closeStatement("updateschedulecondition");
            }
        }
    }
}

