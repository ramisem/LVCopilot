/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.stability;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.stability.ScheduleGrid;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ScheduleConditionPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        block75: {
            if (Trace.on) {
                Trace.log("Processing properties for studyedit scheduleconditions...");
            }
            DBUtil dbu = new DBUtil();
            try {
                String planRows;
                String refitemRows;
                int rows;
                String tracelogId;
                dbu.setConnection(this.sapphireConnection);
                String studyid = (String)props.get("__studyid");
                String schedPlanSDCId = "SchedulePlan";
                SDCProcessor sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
                DateFormat df = DateFormat.getDateTimeInstance(3, 3, I18nUtil.getConnectionLocale(this.connectionInfo));
                df.setTimeZone(I18nUtil.getConnectionTimeZone(this.connectionInfo));
                boolean isAuditTableExist = !sdcProcessor.getProperty(schedPlanSDCId, "auditedflag").equalsIgnoreCase("N");
                TranslationProcessor tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
                HashMap extraPropsMap = new HashMap();
                String reason = "";
                String activity = "";
                String signedflag = "";
                if (props.get("__pr_extraprops") != null && !((String)props.get("__pr_extraprops")).equals("")) {
                    extraPropsMap = this.parseExtraProps((String)props.get("__pr_extraprops"));
                    if (extraPropsMap.containsKey("auditreason")) {
                        reason = (String)extraPropsMap.get("auditreason");
                    }
                    if (extraPropsMap.containsKey("auditactivity")) {
                        activity = (String)extraPropsMap.get("auditactivity");
                    }
                    if (extraPropsMap.containsKey("auditsignedflag")) {
                        signedflag = (String)extraPropsMap.get("auditsignedflag");
                    }
                }
                if ((tracelogId = (String)props.get("tracelogid")) == null || tracelogId.trim().length() == 0) {
                    if (reason == null || reason.trim().length() == 0) {
                        reason = "Updating Study " + studyid;
                    }
                    String sdcid = "StudySDC";
                    AuditService audit = new AuditService(this.sapphireConnection);
                    try {
                        Integer tracelogid = new Integer(audit.addSDITraceLogEntry(sdcid, studyid, "", "", reason, activity, signedflag == null ? "N" : signedflag, "now", "Save", true));
                        tracelogId = tracelogid.toString();
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("Failed to add audit records", e);
                    }
                }
                String scheduleconditionRows = (String)props.get("__schedulecondition_rows");
                PreparedStatement checkTrackItems = dbu.prepareStatement("select 1 from schedulecondition_trackitem where scheduleplanid = ? and scheduleconditionid = ?");
                String containerTypeId = (String)props.get("pr0_containertypeid");
                if (scheduleconditionRows != null && scheduleconditionRows.length() > 0) {
                    String studyStatus;
                    String sqlGetNxtCondLength;
                    QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
                    DataSet dsStudy = qp.getPreparedSqlDataSet("SELECT studystatus, s_startdt, s_manufacturedt, createdt, moddt FROM study WHERE studyid=?", new Object[]{studyid});
                    if (Trace.on) {
                        Trace.log(scheduleconditionRows + " schedulecondition rows found");
                    }
                    rows = Integer.parseInt(scheduleconditionRows);
                    PreparedStatement update = dbu.prepareStatement("updateschedulecondition", "UPDATE schedulecondition SET startdt = ?, startcriteria = ?, conditionstatus = ?, nextconditionstatus = ?, orientation = ?, qtypull = ?, qtypullunits=?, qtypulltype=?, moddt = ?, modby = ?, modtool = ?, tracelogid = ? WHERE scheduleplanid = ? AND scheduleconditionid = ?");
                    ScheduleService scheduler = new ScheduleService(new SapphireConnection(dbu.getConnection(), this.connectionInfo));
                    boolean setRunningApproval = props.get("__setrunningapproval") != null && ((String)props.get("__setrunningapproval")).equals("Y");
                    boolean setSuspendedApproval = props.get("__setsuspendedapproval") != null && ((String)props.get("__setsuspendedapproval")).equals("Y");
                    boolean setCompleteApproval = props.get("__setcompleteapproval") != null && ((String)props.get("__setcompleteapproval")).equals("Y");
                    boolean setCancelledApproval = props.get("__setcancelledapproval") != null && ((String)props.get("__setcancelledapproval")).equals("Y");
                    boolean approvalRequired = false;
                    boolean statusChange = false;
                    int total = 0;
                    int notstarted = 0;
                    int running = 0;
                    int suspended = 0;
                    int complete = 0;
                    int cancelled = 0;
                    String status = "";
                    ScheduleGrid grid = null;
                    for (int i = 0; i < rows; ++i) {
                        String currentStatus = (String)props.get("schedulecondition" + i + "_conditionstatus");
                        if (currentStatus == null) continue;
                        if (((String)props.get("schedulecondition" + i + "_rowstatus")).equals("U")) {
                            Calendar dt;
                            String planid = (String)props.get("schedulecondition" + i + "_scheduleplanid");
                            String conditionid = (String)props.get("schedulecondition" + i + "_scheduleconditionid");
                            String nextStatus = (String)props.get("schedulecondition" + i + "_nextconditionstatus");
                            String nextStatusApproval = (String)props.get("schedulecondition" + i + "_nextconditionstatus_approval");
                            String startCriteria = (String)props.get("schedulecondition" + i + "_startcriteria");
                            String startDate = (String)props.get("schedulecondition" + i + "_startdt");
                            statusChange = false;
                            if (!(startDate != null && startDate.length() != 0 || "UserDefined".equalsIgnoreCase(startCriteria) || dsStudy.getRowCount() <= 0 || (dt = dsStudy.getCalendar(0, startCriteria)) == null)) {
                                startDate = df.format(dt.getTime());
                            }
                            if ((startDate == null || startDate.length() == 0) && ("UserDefined".equalsIgnoreCase(startCriteria) || "User Defined".equalsIgnoreCase(startCriteria))) {
                                if (nextStatus != null && nextStatus.equals("R")) {
                                    throw new SapphireException(tp.translate("Please provide condition \"Start Date\" to proceed."));
                                }
                                update.setTimestamp(1, null);
                            } else {
                                update.setTimestamp(1, new DateTimeUtil(this.sapphireConnection).getTimestamp(startDate));
                            }
                            if (props.get("schedulecondition" + i + "_startcriteria") == null || ((String)props.get("schedulecondition" + i + "_startcriteria")).length() == 0 || ((String)props.get("schedulecondition" + i + "_startcriteria")).equals("UserDefined")) {
                                update.setNull(2, 12);
                            } else {
                                update.setString(2, (String)props.get("schedulecondition" + i + "_startcriteria"));
                            }
                            if (nextStatus != null) {
                                if (nextStatusApproval != null && nextStatusApproval.length() > 0) {
                                    if (nextStatusApproval.equals("Approve")) {
                                        update.setString(3, nextStatus);
                                        update.setNull(4, 12);
                                        status = nextStatus;
                                        statusChange = true;
                                    } else {
                                        update.setString(3, currentStatus);
                                        update.setNull(4, 12);
                                        status = currentStatus;
                                    }
                                } else if (nextStatus.equals("R") && !setRunningApproval || nextStatus.equals("S") && !setSuspendedApproval || nextStatus.equals("C") && !setCompleteApproval || nextStatus.equals("X") && !setCancelledApproval) {
                                    update.setString(3, nextStatus);
                                    update.setNull(4, 12);
                                    status = nextStatus;
                                    statusChange = true;
                                } else {
                                    update.setString(3, currentStatus);
                                    update.setString(4, nextStatus);
                                    status = currentStatus;
                                }
                            } else {
                                update.setString(3, currentStatus);
                                update.setNull(4, 12);
                                status = currentStatus;
                            }
                            update.setString(5, (String)props.get("schedulecondition" + i + "_orientation"));
                            String type = (String)props.get("schedulecondition" + i + "_qtypulltype");
                            String amount = (String)props.get("schedulecondition" + i + "_qtypull");
                            String orientation = (String)props.get("schedulecondition" + i + "_orientation");
                            String qtyPullUnit = (String)props.get("schedulecondition" + i + "_qtypullunits");
                            update.setBigDecimal(6, new BigDecimal((String)props.get("schedulecondition" + i + "_qtypull")));
                            update.setString(7, (String)props.get("schedulecondition" + i + "_qtypullunits"));
                            update.setString(8, (String)props.get("schedulecondition" + i + "_qtypulltype"));
                            update.setTimestamp(9, new Timestamp(DateTimeUtil.getNowCalendar().getTime().getTime()));
                            update.setString(10, this.connectionInfo.getSysuserId());
                            update.setString(11, this.connectionInfo.getTool());
                            update.setString(12, tracelogId);
                            update.setString(13, planid);
                            update.setString(14, conditionid);
                            update.executeUpdate();
                            if (Trace.on) {
                                Trace.log("Updating schedulecondition (" + planid + ", " + conditionid + ") setting startcriteria = " + (String)props.get("schedulecondition" + i + "_startcriteria") + ", startdt = " + (String)props.get("schedulecondition" + i + "_startdt") + ", orientation = " + (String)props.get("schedulecondition" + i + "_orientation"));
                            }
                            if (statusChange) {
                                if (nextStatus.equals("R")) {
                                    if (grid == null || !grid.planid.equals(planid)) {
                                        grid = new ScheduleGrid(this.connectionInfo.getConnectionId(), tracelogId);
                                        grid.retrieve(planid);
                                    }
                                    String xml = grid.conditionAxis.toXML(conditionid);
                                    dbu.updateClob("schedulecondition", "auditxml", xml, new String[]{"scheduleplanid", "scheduleconditionid"}, new String[]{planid, conditionid});
                                    scheduler.scheduleEvents(planid, conditionid, tracelogId);
                                    checkTrackItems.setString(1, planid);
                                    checkTrackItems.setString(2, conditionid);
                                    DataSet trackItems = new DataSet(checkTrackItems.executeQuery());
                                    if (trackItems.getRowCount() < 1) {
                                        this.createTrackItem(planid, conditionid, amount, qtyPullUnit, containerTypeId, orientation);
                                    }
                                } else if (nextStatus.equals("S")) {
                                    scheduler.deleteEvents(planid, conditionid, tracelogId);
                                } else if (nextStatus.equals("C")) {
                                    scheduler.deleteEvents(planid, conditionid, tracelogId);
                                } else if (nextStatus.equals("X")) {
                                    scheduler.deleteEvents(planid, conditionid, tracelogId);
                                }
                            }
                        } else {
                            status = currentStatus;
                        }
                        ++total;
                        if (status.equals("N")) {
                            ++notstarted;
                            continue;
                        }
                        if (status.equals("R")) {
                            ++running;
                            continue;
                        }
                        if (status.equals("S")) {
                            ++suspended;
                            continue;
                        }
                        if (status.equals("C")) {
                            ++complete;
                            continue;
                        }
                        if (!status.equals("X")) continue;
                        ++cancelled;
                    }
                    String string = sqlGetNxtCondLength = dbu.isOracle() ? " nvl( length( schedulecondition.nextconditionstatus  ), 0 ) > 0 " : " coalesce( len( schedulecondition.nextconditionstatus ), 0) > 0";
                    if (dbu.getPreparedCount("SELECT count(*) FROM schedulecondition, study_scheduleplan WHERE schedulecondition.scheduleplanid = study_scheduleplan.scheduleplanid AND study_scheduleplan.studyid = ? AND " + sqlGetNxtCondLength, new Object[]{studyid}) > 0) {
                        approvalRequired = true;
                    }
                    String string2 = studyStatus = dsStudy.size() > 0 ? dsStudy.getValue(0, "studystatus", "A") : "A";
                    if (!(studyStatus.equalsIgnoreCase("Q") || studyStatus.equalsIgnoreCase("P") || studyStatus.equalsIgnoreCase("R"))) {
                        studyStatus = "A";
                        if (total != 0 && total == cancelled) {
                            studyStatus = "X";
                        } else {
                            int noncancelled = total - cancelled;
                            if (noncancelled == notstarted) {
                                studyStatus = "N";
                            } else if (noncancelled == suspended) {
                                studyStatus = "S";
                            } else if (noncancelled == complete) {
                                studyStatus = "C";
                            }
                        }
                    }
                    HashMap<String, String> hmEdit = new HashMap<String, String>();
                    hmEdit.put("sdcid", "StudySDC");
                    hmEdit.put("keyid1", studyid);
                    hmEdit.put("studystatus", studyStatus);
                    hmEdit.put("approvalrequiredflag", approvalRequired ? "Y" : "N");
                    hmEdit.put("auditreason", reason);
                    hmEdit.put("auditactivity", activity);
                    hmEdit.put("auditsignedflag", signedflag);
                    ActionProcessor ap = new ActionProcessor(this.getConnectionInfo().getConnectionId());
                    ap.processAction("EditSdi", "1", hmEdit);
                    if (Trace.on) {
                        Trace.log("Study status updated to '" + studyStatus + "' for study " + studyid);
                    }
                }
                if ((refitemRows = (String)props.get("__refitem_rows")) != null && refitemRows.length() > 0) {
                    if (Trace.on) {
                        Trace.log(refitemRows + " scheduleconditionrefitem rows found");
                    }
                    int rows2 = Integer.parseInt(refitemRows);
                    PreparedStatement insert = dbu.prepareStatement("insertrefitems", "INSERT INTO scheduleconditionrefitem ( scheduleplanid, scheduleconditionid, refitemsdcid, refitemkeyid1, refitemkeyid2, refitemkeyid3, usersequence, createdt, createby, createtool, tracelogid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    PreparedStatement delete = dbu.prepareStatement("deleterefitems", "DELETE FROM scheduleconditionrefitem WHERE scheduleplanid = ? AND scheduleconditionid = ? AND refitemsdcid=? AND refitemkeyid1=? AND refitemkeyid2=? AND refitemkeyid3=?");
                    StringBuffer updateAuditSql = new StringBuffer();
                    PreparedStatement updateAudit = null;
                    if (isAuditTableExist) {
                        updateAuditSql.append("UPDATE a_scheduleconditionrefitem SET modby = ?, modtool = 'DELETE', moddt = {ts '" + DateTimeUtil.getNowTimestamp() + "'}, tracelogid = '" + tracelogId + "'").append(" WHERE scheduleplanid = ? AND scheduleconditionid = ? AND refitemsdcid=? AND refitemkeyid1=? AND refitemkeyid2=? AND refitemkeyid3=? AND tracelogid = 'DELETED'  ").append(" AND auditsequence = ( SELECT max( auditsequence ) FROM a_scheduleconditionrefitem WHERE scheduleplanid = ? AND scheduleconditionid = ? AND refitemsdcid=? AND refitemkeyid1=? AND refitemkeyid2=? AND refitemkeyid3=?  AND tracelogid = 'DELETED')");
                        updateAudit = dbu.prepareStatement("updateaudit", updateAuditSql.toString());
                    }
                    for (int i = 0; i < rows2; ++i) {
                        if (props.get("ri" + i + "_scheduleplanid") == null) continue;
                        String planid = (String)props.get("ri" + i + "_scheduleplanid");
                        String conditionid = (String)props.get("ri" + i + "_scheduleconditionid");
                        String refitemsdcid = (String)props.get("ri" + i + "_refitemsdcid");
                        String refitemkeyid1 = (String)props.get("ri" + i + "_refitemkeyid1");
                        String refitemkeyid2 = "(null)";
                        String refitemkeyid3 = "(null)";
                        int usersequence = 0;
                        try {
                            usersequence = Integer.parseInt((String)props.get("ri" + i + "_usersequence"));
                        }
                        catch (Exception suspended) {
                            // empty catch block
                        }
                        String status = (String)props.get("ri" + i + "_rowstatus");
                        if ("I".equals(status)) {
                            insert.setString(1, planid);
                            insert.setString(2, conditionid);
                            insert.setString(3, refitemsdcid);
                            insert.setString(4, refitemkeyid1);
                            insert.setString(5, refitemkeyid2);
                            insert.setString(6, refitemkeyid3);
                            insert.setInt(7, usersequence);
                            insert.setTimestamp(8, DateTimeUtil.getNowTimestamp());
                            insert.setString(9, this.connectionInfo.getSysuserId());
                            insert.setString(10, this.connectionInfo.getTool());
                            insert.setString(11, tracelogId);
                            insert.executeUpdate();
                            if (!Trace.on) continue;
                            Trace.log("Inserting into scheduleconditionrefitems ( scheduleplanid = " + planid + ", scheduleconditionid = " + conditionid + ", refitem = " + refitemkeyid1 + ")");
                            continue;
                        }
                        if (!"D".equals(status)) continue;
                        delete.setString(1, planid);
                        delete.setString(2, conditionid);
                        delete.setString(3, refitemsdcid);
                        delete.setString(4, refitemkeyid1);
                        delete.setString(5, refitemkeyid2);
                        delete.setString(6, refitemkeyid3);
                        delete.executeUpdate();
                        if (!isAuditTableExist) continue;
                        updateAudit.setString(1, this.connectionInfo.getSysuserId());
                        updateAudit.setString(2, planid);
                        updateAudit.setString(3, conditionid);
                        updateAudit.setString(4, refitemsdcid);
                        updateAudit.setString(5, refitemkeyid1);
                        updateAudit.setString(6, refitemkeyid2);
                        updateAudit.setString(7, refitemkeyid3);
                        updateAudit.setString(8, planid);
                        updateAudit.setString(9, conditionid);
                        updateAudit.setString(10, refitemsdcid);
                        updateAudit.setString(11, refitemkeyid1);
                        updateAudit.setString(12, refitemkeyid2);
                        updateAudit.setString(13, refitemkeyid3);
                        try {
                            updateAudit.executeUpdate();
                            continue;
                        }
                        catch (SQLException ex) {
                            int errorCode = ex.getErrorCode();
                            if (errorCode == (dbu.isOracle() ? 942 : 240)) continue;
                            throw new SapphireException("DB_UPDATE_FAILED", ex);
                        }
                    }
                }
                if ((planRows = (String)props.get("__study_scheduleplan_rows")) == null || planRows.length() <= 0) break block75;
                if (Trace.on) {
                    Trace.log(planRows + " study_scheduleplan rows found");
                }
                rows = Integer.parseInt(planRows);
                StringBuffer schedulePlans = new StringBuffer();
                for (int i = 0; i < rows; ++i) {
                    if (props.get("__study_scheduleplan" + i + "_rs") == null || !((String)props.get("__study_scheduleplan" + i + "_rs")).equals("D")) continue;
                    String[] keys = StringUtil.split((String)props.get("__study_scheduleplan" + i + "_key"), ";");
                    schedulePlans.append(";" + keys[1]);
                    if (schedulePlans.length() > 0) {
                        ActionProcessor ap = new ActionProcessor(this.connectionInfo.getConnectionId());
                        HashMap<String, String> actionProps = new HashMap<String, String>();
                        actionProps.put("sdcid", "SchedulePlan");
                        actionProps.put("keyid1", schedulePlans.substring(1));
                        ap.processAction("DeleteSDI", "1", actionProps);
                    }
                    if (!Trace.on) continue;
                    Trace.log("Deleted plan " + keys[1] + " for study " + studyid);
                }
            }
            catch (ServiceException e) {
                throw new SapphireException("SCHEDULE_SERVICE_FAILED", "Failed to schedule events", e);
            }
            catch (SapphireException se) {
                throw se;
            }
            catch (NumberFormatException nfe) {
                throw new SapphireException("Invalid property __[dataset]_rows");
            }
            catch (SQLException e) {
                throw new SapphireException("SQLException from update. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
            }
            finally {
                dbu.reset();
            }
        }
    }

    public HashMap parseExtraProps(String extraProps) {
        HashMap<String, String> props = new HashMap<String, String>();
        if (extraProps != null && extraProps.length() > 0) {
            String[] s = StringUtil.split(extraProps, ";");
            for (int i = 0; i < s.length; ++i) {
                String[] __s;
                String _s = s[i];
                if (_s == null || _s.length() <= 0 || (__s = StringUtil.split(_s, "="))[0] == null || __s[0].length() <= 0) continue;
                if (__s[1] != null && __s[1].length() > 0) {
                    props.put(__s[0], __s[1]);
                    continue;
                }
                props.put(__s[0], "");
            }
        }
        return props;
    }

    private void createTrackItem(String planId, String conditionId, String amount, String qtyPullUnit, String containerTypeId, String orientation) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "TrackItemSDC");
        props.setProperty("qtycurrent", amount);
        String qtyCurrentType = qtyPullUnit == null || qtyPullUnit.length() == 0 || "C".equals(qtyPullUnit) ? "C" : "U";
        props.setProperty("qtycurrenttype", qtyCurrentType);
        props.setProperty("qtyunits", qtyPullUnit);
        props.setProperty("containertypeid", containerTypeId);
        props.setProperty("cocrequiredflag", "Y");
        props.setProperty("controlsubstanceflag", "N");
        props.setProperty("orientation", orientation);
        props.setProperty("scheduleplanid", planId);
        props.setProperty("scheduleconditionid", conditionId);
        ActionProcessor ap = new ActionProcessor(this.connectionInfo.getConnectionId());
        ap.processAction("AddSDI", "1", props);
    }
}

