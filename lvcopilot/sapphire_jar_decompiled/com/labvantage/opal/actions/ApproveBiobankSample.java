/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ApproveBiobankSample
extends BaseAction
implements sapphire.action.ApproveBiobankSample {
    final String LABVANTAGE_CVS_ID = "$Revision: 65948 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        DataSet ds;
        TranslationProcessor tp = this.getTranslationProcessor();
        String sampleid = props.getProperty("sampleid");
        String approvaltype = props.getProperty("approvaltype", "Full");
        boolean raiseerror = "Y".equals(props.getProperty("raiseerror", "N"));
        boolean fullapproval = "Full".equals(approvaltype);
        boolean verification = "Verification".equals(approvaltype);
        String errorformat = props.getProperty("errorformat", "HTML").toUpperCase();
        if (StringUtil.getLen(sampleid) <= 0L) throw new SapphireException(this.getTranslationProcessor().translate("Approval Rules"), "VALIDATION", this.getTranslationProcessor().translate("No samples found to be approved."));
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s.s_sampleid, s.sstudyid, sa.rulename, s.storagestatus, sa.fullapprovalflag, sa.conditionalapprovalflag,");
        sql.append(" (select sf.conditionalapprovalflag from s_samplefamily sf where sf.s_samplefamilyid = s.samplefamilyid) sampleconditionalapprovalflag ");
        sql.append(" from s_sample s left OUTER JOIN s_studysampleapproval sa on sa.s_studyid = s.sstudyid");
        if (StringUtil.split(sampleid, ";").length > 750) {
            String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("Unable to create RSET for samples while executing action ApproveBiobankSample.");
            sql.append(" where s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        HashSet<String> ruleSuccessSet = new HashSet<String>();
        if (ds != null && ds.size() > 0) {
            int i;
            HashSet<String> ruleFailureSet = new HashSet<String>();
            String conditionErrorMsg = "";
            PropertyList rules = this.getAllApprovalRules();
            HashMap cm = new HashMap();
            boolean noApprovalRulesFlag = true;
            block4: for (i = 0; i < ds.size(); ++i) {
                String msg;
                sampleid = ds.getValue(i, "s_sampleid");
                String fullapprovalflag = ds.getValue(i, "fullapprovalflag");
                String conditionalapprovalflag = ds.getValue(i, "conditionalapprovalflag");
                String sampleconditionalapprovalflag = ds.getValue(i, "sampleconditionalapprovalflag");
                String storagestatus = ds.getValue(i, "storagestatus");
                if ("Allocated".equals(storagestatus)) {
                    msg = "Only Received or Temporary In Lab samples can be Approved";
                    if (!cm.containsKey(sampleid)) {
                        cm.put(sampleid, new HashSet());
                    }
                    ((Set)cm.get(sampleid)).add(msg);
                    ruleFailureSet.add(sampleid);
                    if (!ruleSuccessSet.contains(sampleid)) continue;
                    ruleSuccessSet.remove(sampleid);
                    continue;
                }
                if ("In Circulation".equals(storagestatus)) continue;
                if (verification) {
                    boolean bl = fullapproval = !"Y".equals(sampleconditionalapprovalflag);
                }
                if (fullapproval && !"Y".equals(fullapprovalflag) || !fullapproval && !"Y".equals(conditionalapprovalflag)) continue;
                noApprovalRulesFlag = false;
                if (verification && !"Verification Needed".equals(storagestatus)) {
                    msg = "Sample must be in storage status of 'Verification Needed' to undergo verification.";
                    if (!cm.containsKey(sampleid)) {
                        cm.put(sampleid, new HashSet());
                    }
                    ((Set)cm.get(sampleid)).add(msg);
                    ruleFailureSet.add(sampleid);
                    if (ruleSuccessSet.contains(sampleid)) {
                        ruleSuccessSet.remove(sampleid);
                    }
                } else if (!("Received".equals(storagestatus) || "Verification Needed".equals(storagestatus) || "Temporary In Lab".equals(storagestatus))) {
                    msg = "Only Received or Temporary In Lab samples can be Approved";
                    if (!cm.containsKey(sampleid)) {
                        cm.put(sampleid, new HashSet());
                    }
                    ((Set)cm.get(sampleid)).add(msg);
                    ruleFailureSet.add(sampleid);
                    if (ruleSuccessSet.contains(sampleid)) {
                        ruleSuccessSet.remove(sampleid);
                    }
                }
                String rulename = ds.getValue(i, "rulename");
                PropertyListCollection conditions = rules.getCollectionNotNull(rulename);
                for (Object condition1 : conditions) {
                    boolean conditionMet;
                    PropertyList condition = (PropertyList)condition1;
                    try {
                        conditionMet = this.hasConditionMet(condition, sampleid);
                    }
                    catch (SapphireException e) {
                        conditionErrorMsg = e.getMessage();
                        continue block4;
                    }
                    if (!conditionMet) {
                        String msg2 = condition.getProperty("errormessage", this.getTranslationProcessor().translate("Condition not met"));
                        if (!cm.containsKey(sampleid)) {
                            cm.put(sampleid, new HashSet());
                        }
                        ((Set)cm.get(sampleid)).add(msg2);
                        ruleFailureSet.add(sampleid);
                        if (!ruleSuccessSet.contains(sampleid)) continue;
                        ruleSuccessSet.remove(sampleid);
                        continue;
                    }
                    if (ruleFailureSet.contains(sampleid)) continue;
                    ruleSuccessSet.add(sampleid);
                }
            }
            if (noApprovalRulesFlag) {
                for (i = 0; i < ds.size(); ++i) {
                    String _sampleid = ds.getValue(i, "s_sampleid");
                    if (ruleFailureSet.contains(_sampleid)) continue;
                    ruleSuccessSet.add(ds.getValue(i, "s_sampleid"));
                }
            }
            if ("Verification".equals(approvaltype)) {
                safeSQL.reset();
                String sysuserid = this.connectionInfo.getSysuserId();
                sql.setLength(0);
                sql.append("select s.s_sampleid, s.sstudyid, study.verifiedby, study.verifiedbyrole");
                sql.append(" from s_study study, s_sample s ");
                sql.append(" where study.s_studyid = s.sstudyid");
                sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(ruleSuccessSet, ";"), ";")).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null) {
                    for (int i2 = 0; i2 < ds.size(); ++i2) {
                        String verifiedby = ds.getValue(i2, "verifiedby");
                        String verifiedbyrole = ds.getValue(i2, "verifiedbyrole");
                        if (StringUtil.getLen(verifiedby) == 0L && StringUtil.getLen(verifiedbyrole) == 0L) {
                            conditionErrorMsg = this.getTranslationProcessor().translate("No user or role has been assigned to verify samples for Study") + " " + ds.getValue(i2, "sstudyid");
                            break;
                        }
                        if (StringUtil.getLen(verifiedby) > 0L && !sysuserid.equals(verifiedby)) {
                            conditionErrorMsg = this.getTranslationProcessor().translate("Selected samples can only be verified by") + " " + verifiedby;
                            break;
                        }
                        if (StringUtil.getLen(verifiedbyrole) <= 0L || this.connectionInfo.hasRole(verifiedbyrole)) continue;
                        conditionErrorMsg = this.getTranslationProcessor().translate("User does not have a valid role to verify these samples");
                        break;
                    }
                }
            }
            props.setProperty("sampleapproved", "N");
            if (cm.size() > 0) {
                String errormessage = "";
                if ("HTML".equals(errorformat)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table><tr><td>");
                    sb.append("<span style='padding:10px'>").append(this.getTranslationProcessor().translate("Following samples could not be approved")).append("</span><hr>");
                    sb.append("<div style='width:100%;height:200px;overflow:auto'>");
                    boolean even = true;
                    for (Object o : cm.keySet()) {
                        String sample = (String)o;
                        sb.append("<div style='padding:4px;background:").append(even ? "white" : "#efefef").append("'>");
                        sb.append("<b>").append(this.getTranslationProcessor().translate("Sample")).append(" ").append(sample).append("</b>");
                        sb.append("<ul>");
                        Set list = (Set)cm.get(sample);
                        for (String aList : list) {
                            sb.append("<li>").append(this.getTranslationProcessor().translate(aList)).append("</li>");
                        }
                        sb.append("</ul></div>");
                        even = !even;
                    }
                    sb.append("</div>");
                    sb.append("</td></tr></table>");
                    errormessage = sb.toString();
                } else if ("JSON".equals(errorformat)) {
                    JSONObject object = new JSONObject();
                    try {
                        for (Object o : cm.keySet()) {
                            String sample = (String)o;
                            Set set = (Set)cm.get(sample);
                            JSONArray errors = new JSONArray();
                            for (String aList : set) {
                                errors.put(this.getTranslationProcessor().translate(aList));
                            }
                            object.put(sample, errors);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    errormessage = object.toString();
                }
                props.setProperty("noapprove", errormessage);
            }
            if (StringUtil.getLen(conditionErrorMsg) > 0L) {
                if (raiseerror) {
                    throw new SapphireException("Verification".equals(approvaltype) ? tp.translate("Unable to Verify Sample") : tp.translate("Unable to Approve Sample"), "VALIDATION", conditionErrorMsg);
                }
                props.setProperty("ConditionErrorMsg", conditionErrorMsg);
            } else {
                props.setProperty("ConditionErrorMsg", "");
                if (ruleSuccessSet.size() > 0) {
                    this.approveSamples(props, ruleSuccessSet, approvaltype, fullapproval, raiseerror);
                }
            }
            if (cm.size() <= 0) return;
            StringBuilder sb = new StringBuilder();
            for (Object o : cm.keySet()) {
                String s = (String)o;
                sb.append("{{Sample}} ").append(s).append(" {{could not be Approved because}}");
                Set list = (Set)cm.get(s);
                sb.append("<ul>");
                for (String aList : list) {
                    sb.append("<li>").append(aList).append("</li>");
                }
                sb.append("</ul>");
            }
            this.setError(this.getTranslationProcessor().translatePartial("<font color=red>{{Unable to approve}} " + cm.size() + " {{samples}}</font>"), raiseerror ? "FAILURE" : "INFORMATION", this.getTranslationProcessor().translatePartial(sb.toString()));
            return;
        }
        safeSQL.reset();
        sql.setLength(0);
        sql.append("select s.s_sampleid, s.sstudyid, s.storagestatus");
        sql.append(" from s_sample s");
        sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                sampleid = ds.getValue(i, "s_sampleid");
                String storagestatus = ds.getValue(i, "storagestatus");
                if (verification && !"Verification Needed".equals(storagestatus)) {
                    this.setError("Validation", raiseerror ? "VALIDATION" : "INFORMATION", this.getTranslationProcessor().translate("Sample must be in storage status of 'Verification Needed' to undergo verification."));
                    continue;
                }
                if (!"Received".equals(storagestatus)) {
                    this.setError("Validation", raiseerror ? "VALIDATION" : "INFORMATION", this.getTranslationProcessor().translate("Sample must be in storage status of 'Received' to Approval."));
                    continue;
                }
                ruleSuccessSet.add(sampleid);
            }
        }
        props.setProperty("ConditionErrorMsg", "");
        if (ruleSuccessSet.size() <= 0) return;
        this.approveSamples(props, ruleSuccessSet, approvaltype, fullapproval, raiseerror);
    }

    private void approveSamples(PropertyList props, Set<String> ruleSuccessSet, String approvaltype, boolean fullapproval, boolean raiseerror) {
        props.setProperty("sampleapproved", "Y");
        try {
            String sysuserid = this.connectionInfo.getSysuserId();
            ArrayList child = new ArrayList();
            ApproveBiobankSample.populateTempInLabChildList(this.getQueryProcessor(), OpalUtil.toDelimitedString(ruleSuccessSet, ";"), child);
            ruleSuccessSet.addAll(child);
            DataSet data = this.populateSampleFamilyAndVerification(ruleSuccessSet, approvaltype);
            if (data.size() > 0) {
                String samplefamilyid;
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "Sample");
                actionProps.setProperty("keyid1", data.getColumnValues("sampleid", ";"));
                actionProps.setProperty("auditreason", props.getProperty("auditreason"));
                actionProps.setProperty("auditactivity", props.getProperty("auditactivity"));
                actionProps.setProperty("auditsignedflag", props.getProperty("auditsignedflag", "N"));
                actionProps.setProperty("storagestatus", data.getColumnValues("storagestatus", ";"));
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(errorHandler);
                }
                if (StringUtil.getLen(samplefamilyid = data.getColumnValues("samplefamilyid", ";")) > 0L) {
                    actionProps.clear();
                    actionProps.setProperty("sdcid", "LV_SampleFamily");
                    actionProps.setProperty("keyid1", samplefamilyid);
                    actionProps.setProperty("approvedby", sysuserid);
                    actionProps.setProperty("approveddt", "n");
                    if ("Verification Needed".equals(data.getValue(0, "oldstoragestatus"))) {
                        actionProps.setProperty("verifiedby", sysuserid);
                        actionProps.setProperty("verifieddt", "n");
                    }
                    actionProps.setProperty("conditionalapprovalreason", fullapproval ? "(null)" : props.getProperty("auditreason"));
                    actionProps.setProperty("conditionalapprovalflag", fullapproval ? "N" : "Y");
                    this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                    errorHandler = this.getActionProcessor().getErrorHandler();
                    if (errorHandler != null && errorHandler.hasInfoErrors()) {
                        this.setErrors(errorHandler);
                    }
                }
            }
        }
        catch (ActionException e) {
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler != null) {
                if (raiseerror) {
                    this.setErrors(e.getErrorHandler());
                } else {
                    ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(0);
                    if (errorDetail != null) {
                        props.setProperty("ConditionErrorMsg", errorDetail.getMessage());
                    } else {
                        props.setProperty("ConditionErrorMsg", e.getMessage());
                    }
                }
            } else {
                props.setProperty("ConditionErrorMsg", e.getMessage());
                if (raiseerror) {
                    this.setError(this.getTranslationProcessor().translate("Action Failure"), "FAILURE", e.getMessage());
                }
            }
            this.logger.error("Action Error", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DataSet populateSampleFamilyAndVerification(Set<String> samples, String approvaltype) {
        ArrayList ds;
        DataSet data;
        block9: {
            StringBuilder sql;
            SafeSQL safeSQL;
            block10: {
                data = new DataSet();
                data.addColumn("sampleid", 0);
                data.addColumn("samplefamilyid", 0);
                data.addColumn("storagestatus", 0);
                data.addColumn("oldstoragestatus", 0);
                safeSQL = new SafeSQL();
                ds = null;
                sql = new StringBuilder();
                sql.append("select s.s_sampleid, s.samplefamilyid, s.storagestatus,");
                sql.append(" ( select rc.verificationrequiredflag from s_restrictclass rc ");
                sql.append(" where rc.s_restrictclassid = ( select sf.restrictclassid from s_samplefamily sf where sf.s_samplefamilyid = s.samplefamilyid ) ) verificationrequiredflag");
                if (samples.size() <= 1000) break block10;
                String rsetid = null;
                try {
                    rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(samples, ";"), null, null);
                    sql.append(" from s_sample s, rsetitems r");
                    sql.append(" where r.sdcid = 'Sample'");
                    sql.append(" and r.keyid1 = s.s_sampleid");
                    sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (rsetid == null) break block9;
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    try {
                        this.logger.error("Action Error", e);
                        if (rsetid == null) break block9;
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                    catch (Throwable throwable) {
                        if (rsetid != null) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                        throw throwable;
                    }
                    break block9;
                }
                break block9;
            }
            sql.append(" from s_sample s");
            sql.append(" where s.s_sampleid in ( ").append(safeSQL.addIn(samples)).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                int row = data.addRow();
                data.setValue(row, "sampleid", ((DataSet)ds).getValue(i, "s_sampleid"));
                data.setValue(row, "samplefamilyid", ((DataSet)ds).getValue(i, "samplefamilyid"));
                data.setValue(row, "oldstoragestatus", ((DataSet)ds).getValue(i, "storagestatus"));
                if ("Full".equals(approvaltype) || "Conditional".equals(approvaltype)) {
                    if ("Y".equals(((DataSet)ds).getValue(i, "verificationrequiredflag"))) {
                        data.setValue(row, "storagestatus", "Verification Needed");
                        continue;
                    }
                    data.setValue(row, "storagestatus", "In Circulation");
                    continue;
                }
                data.setValue(row, "storagestatus", "In Circulation");
            }
        }
        return data;
    }

    public PropertyList getAllApprovalRules() throws SapphireException {
        PropertyList rules = new PropertyList();
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection approvalrules = policy.getCollectionNotNull("sampleapprovalrules");
            for (int i = 0; i < approvalrules.size(); ++i) {
                PropertyList approvalrule = approvalrules.getPropertyList(i);
                String rulename = approvalrule.getProperty("rulename");
                PropertyListCollection conditions = approvalrule.getCollectionNotNull("conditions");
                rules.setProperty(rulename, conditions);
            }
        }
        return rules;
    }

    public boolean hasConditionMet(PropertyList condition, String sampleid) throws SapphireException {
        String tableid = condition.getProperty("tableid");
        String columnid = condition.getProperty("columnid");
        String operator = condition.getProperty("operator");
        String value = condition.getProperty("value");
        if (StringUtil.getLen(tableid) > 0L && StringUtil.getLen(columnid) > 0L && StringUtil.getLen(operator) > 0L) {
            String sql = "";
            SafeSQL safeSQL = new SafeSQL();
            if ("s_sample".equals(tableid)) {
                sql = "select count( " + columnid + " ) cnt from " + tableid + " where s_sampleid = " + safeSQL.addVar(sampleid);
            } else if ("trackitem".equals(tableid)) {
                sql = "select count( " + columnid + " ) cnt from " + tableid + " where linksdcid = 'Sample' and linkkeyid1 = " + safeSQL.addVar(sampleid);
            } else if ("s_study".equals(tableid)) {
                sql = "select count( " + columnid + " ) cnt from " + tableid + " where s_studyid = ( select s_sample.sstudyid from s_sample where s_sample.s_sampleid = " + safeSQL.addVar(sampleid) + " )";
            } else if ("s_samplefamily".equals(tableid)) {
                sql = "select count( " + columnid + " ) cnt from " + tableid + " where s_samplefamilyid = ( select s_sample.samplefamilyid from s_sample where s_sample.s_sampleid = " + safeSQL.addIn(sampleid) + " )";
            }
            if ("is Mandatory".equals(operator)) {
                if (this.getConnectionProcessor().isOra()) {
                    sql = sql + " and nvl(length(" + columnid + "), 0) != 0";
                } else if (this.getConnectionProcessor().isMSS()) {
                    sql = sql + " and COALESCE(len(" + columnid + "), 0) != 0";
                }
            } else if ("equals".equals(operator)) {
                sql = sql + " and " + columnid + " = " + safeSQL.addVar(value);
            } else if ("less than".equals(operator)) {
                sql = sql + " and " + columnid + " < " + safeSQL.addVar(value);
            } else if ("less than or equals".equals(operator)) {
                sql = sql + " and " + columnid + " <= " + safeSQL.addVar(value);
            } else if ("greater than".equals(operator)) {
                sql = sql + " and " + columnid + " > " + safeSQL.addVar(value);
            } else if ("greater than or equals".equals(operator)) {
                sql = sql + " and " + columnid + " >= " + safeSQL.addVar(value);
            } else if ("like".equals(operator)) {
                sql = sql + " and " + columnid + " like " + safeSQL.addVar("%" + value + "%");
            } else if ("in".equals(operator)) {
                sql = sql + " and " + columnid + " in ( " + safeSQL.addIn(value, ";") + " )";
            }
            return this.database.getPreparedCount(sql, safeSQL.getValues()) > 0;
        }
        return true;
    }

    public static void populateTempInLabChildList(QueryProcessor queryProcessor, String sampleid, List child) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s_samplemap.destsampleid");
        sql.append(" from s_samplemap");
        sql.append(" where s_samplemap.sourcesampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
        sql.append(" and (select s_sample.storagestatus from s_sample where s_sample.s_sampleid = s_samplemap.sourcesampleid ) = 'Temporary In Lab'");
        sql.append(" and (select s_sample.storagestatus from s_sample where s_sample.s_sampleid = s_samplemap.destsampleid ) = 'Temporary In Lab'");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String childid = ds.getValue(i, "destsampleid");
                child.add(childid);
                ApproveBiobankSample.populateTempInLabChildList(queryProcessor, childid, child);
            }
        }
    }
}

