/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pages.prodvariantruleeditor;

import com.labvantage.sapphire.pages.prodvariantruleeditor.RuleTypeProcessor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class DateRangeTransitionRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append("<input type=\"text\" size=\"3\" name=\"number\" > ");
        DataSet ds = qp.getRefTypeDataSet("AgeUnit");
        rowEditor.append("<select name=\"ageunit\"> ");
        for (int i = 0; i < ds.size(); ++i) {
            rowEditor.append("<option value=\"").append(ds.getValue(i, "refvalueid")).append("\">");
            if (ds.getValue(i, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(ds.getValue(i, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(ds.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option> ");
        }
        rowEditor.append("</select> ");
        return rowEditor.toString();
    }

    @Override
    public String getSummary(String ruledefinition, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        StringBuffer summarytext = new StringBuffer();
        try {
            JSONObject json = new JSONObject(ruledefinition);
            SafeSQL safeSQL = new SafeSQL();
            String ruleno = json.getString("ruleno");
            String transitionid = json.getString("state");
            String sql4TransitionSt = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'ProdVariantState' and refvalueid = " + safeSQL.addVar(transitionid.replaceAll("'", "''"));
            DataSet transitionStDS = qp.getPreparedSqlDataSet(sql4TransitionSt, safeSQL.getValues());
            if (transitionStDS.size() > 0 && transitionStDS.getValue(0, "refdisplayvalue", "").length() > 0) {
                transitionid = transitionStDS.getValue(0, "refdisplayvalue", "");
            }
            String ruletype = json.getString("ruletype");
            String autoflag = json.getString("autoflag");
            safeSQL.reset();
            String sql = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'TransitionRuleTypes' and refvalueid = " + safeSQL.addVar(ruletype.replaceAll("'", "''"));
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            safeSQL.reset();
            String sql4AutoFlag = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'StateTransitionFlag' and refvalueid = " + safeSQL.addVar(autoflag.replaceAll("'", "''"));
            DataSet autoflagds = qp.getPreparedSqlDataSet(sql4AutoFlag, safeSQL.getValues());
            JSONObject data = json.getJSONObject("data");
            String batchCount = data.getString("batchCount");
            String batchStatus = data.getString("batchStatus");
            String number = data.getString("number");
            String ageunit = data.getString("ageunit");
            String consecutive = "";
            if (data.has("consecutiveItems")) {
                consecutive = "Y".equalsIgnoreCase(data.getString("consecutiveItems")) ? "consecutively" : "";
            }
            summarytext.append(tp.translate(". Set new state to ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(transitionid)).append(" ");
            if (autoflagds.getValue(0, "refdisplayvalue").length() > 0) {
                if (autoflag.equalsIgnoreCase("Y")) {
                    summarytext.append("");
                } else {
                    summarytext.append(tp.translate(autoflagds.getValue(0, "refdisplayvalue")));
                }
            } else {
                if (autoflag.equalsIgnoreCase("Y")) {
                    autoflag = "";
                }
                summarytext.append(tp.translate(autoflag));
            }
            summarytext.append("</font>").append(tp.translate(" on ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(batchCount)).append(" ").append(consecutive).append(" ").append(tp.translate(batchStatus)).append("</font>").append(tp.translate(" item(s) ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">");
            if (ds.getValue(0, "refdisplayvalue").length() > 0) {
                summarytext.append(tp.translate(ds.getValue(0, "refdisplayvalue")));
            } else {
                summarytext.append(tp.translate(ruletype));
            }
            summarytext.append(" ").append(tp.translate(number)).append(" ").append(tp.translate(ageunit)).append("</font>");
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return summarytext.toString();
    }

    @Override
    public boolean evaluateRule(String levelid, String ruledefinition, String sortingColumn, String tableid, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValHM, Calendar lastTransitionDate, String dbms) throws SapphireException {
        boolean rulePassed = true;
        try {
            JSONObject rule = new JSONObject(ruledefinition);
            String batchStatus = rule.getString("batchStatus");
            String calendarunit = rule.getString("ageunit");
            String batchcount = rule.getString("batchCount");
            String number = rule.getString("number");
            boolean consecutive = false;
            if (rule.has("consecutiveItems")) {
                consecutive = "Y".equalsIgnoreCase(rule.getString("consecutiveItems"));
            }
            int n = 0;
            int batchCt = 0;
            try {
                n = Integer.parseInt(number);
                batchCt = Integer.parseInt(batchcount);
            }
            catch (NumberFormatException nfe) {
                throw new SapphireException(tp.translate("Unable to retrieve rule") + " " + nfe.getMessage());
            }
            Calendar rightNow = Calendar.getInstance();
            if (calendarunit.equalsIgnoreCase("Days")) {
                rightNow.add(5, -n);
            } else if (calendarunit.equalsIgnoreCase("Months")) {
                rightNow.add(2, -n);
            } else if (calendarunit.equalsIgnoreCase("Years")) {
                rightNow.add(1, -n);
            }
            String DATE_FORMAT = "M/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT disposition, releaseddt  FROM ").append(tableid);
            sql.append(" WHERE ");
            sql.append(sortingColumn);
            if ("ORA".equals(dbms)) {
                sql.append(" > to_date(").append(safeSQL.addVar(sdf.format(rightNow.getTime()))).append(", 'MM/DD/YY') ");
            } else if ("MSS".equals(dbms)) {
                sql.append(" > convert( datetime, " + safeSQL.addVar(sdf.format(rightNow.getTime())) + ", 1 )");
            }
            for (String prodVarCol : prodVarColValHM.keySet()) {
                String val = (String)prodVarColValHM.get(prodVarCol);
                if (val != null && val.length() > 0) {
                    sql.append(" AND ").append(prodVarCol).append(" = ").append(safeSQL.addVar(val)).append(" ");
                    continue;
                }
                sql.append(" AND (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
            }
            if (lastTransitionDate != null) {
                sql.append(" AND " + sortingColumn);
                if ("ORA".equals(dbms)) {
                    sql.append(" > to_date(").append(safeSQL.addVar(new SimpleDateFormat("M/dd/yy HH:mm:ss").format(lastTransitionDate.getTime()))).append(", 'MM/DD/YY HH24:mi:ss') ");
                } else if ("MSS".equals(dbms)) {
                    sql.append(" > convert( datetime, " + safeSQL.addVar(new SimpleDateFormat("M/dd/yy HH:mm:ss").format(lastTransitionDate.getTime())) + ", 1 )");
                }
            }
            sql.append(" AND (batchmode is null OR batchmode = '' OR batchmode = 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
            sql.append(" AND disposition is not null ORDER BY " + sortingColumn + " DESC");
            DataSet dsBatch = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (consecutive) {
                dsBatch.sort("releaseddt D");
            }
            int count = 0;
            if (batchStatus.length() > 0) {
                for (int i = 0; i < dsBatch.size(); ++i) {
                    String batchDisposition = dsBatch.getValue(i, "disposition");
                    if (batchDisposition.equals(batchStatus)) {
                        ++count;
                        continue;
                    }
                    if (!consecutive) {
                        continue;
                    }
                    break;
                }
            } else {
                count = dsBatch.size();
            }
            rulePassed = count == batchCt;
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return rulePassed;
    }

    private static class UserMessages {
        static final String SETSTATETO = ". Set new state to ";
        static final String ON = " on ";
        static final String BATCHES = " item(s) ";

        private UserMessages() {
        }
    }
}

