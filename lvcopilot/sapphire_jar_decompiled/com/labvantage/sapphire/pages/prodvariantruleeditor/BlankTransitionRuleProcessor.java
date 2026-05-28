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
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class BlankTransitionRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53871 $";

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        return "";
    }

    @Override
    public String getSummary(String ruledefinition, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        StringBuffer summarytext = new StringBuffer();
        try {
            JSONObject json = new JSONObject(ruledefinition);
            String transitionid = json.getString("state");
            SafeSQL safeSQL = new SafeSQL();
            String sql4TransitionSt = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'ProdVariantState' and refvalueid = " + safeSQL.addVar(transitionid.replaceAll("'", "''"));
            DataSet transitionStDS = qp.getPreparedSqlDataSet(sql4TransitionSt, safeSQL.getValues());
            if (transitionStDS.size() > 0 && transitionStDS.getValue(0, "refdisplayvalue", "").length() > 0) {
                transitionid = transitionStDS.getValue(0, "refdisplayvalue", "");
            }
            String autoflag = json.getString("autoflag");
            safeSQL.reset();
            String sql4AutoFlag = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'StateTransitionFlag' and refvalueid = " + safeSQL.addVar(autoflag.replaceAll("'", "''"));
            DataSet autoflagds = qp.getPreparedSqlDataSet(sql4AutoFlag, safeSQL.getValues());
            JSONObject data = json.getJSONObject("data");
            String batchCount = data.getString("batchCount");
            String batchStatus = data.getString("batchStatus");
            String consecutive = "";
            if (data.has("consecutiveItems")) {
                consecutive = "Y".equalsIgnoreCase(data.getString("consecutiveItems")) ? "consecutively" : "";
            }
            summarytext.append(tp.translate(". Set new state to ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(transitionid)).append(" ");
            if (autoflagds.size() > 0 && autoflagds.getValue(0, "refdisplayvalue").length() > 0) {
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
            summarytext.append("</font>").append(tp.translate(" on ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(batchCount)).append(" ").append(consecutive).append(" ").append(tp.translate(batchStatus)).append("</font>").append(tp.translate(" item(s) "));
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
            String batchcount = rule.getString("batchCount");
            boolean consecutive = false;
            if (rule.has("consecutiveItems")) {
                consecutive = "Y".equalsIgnoreCase(rule.getString("consecutiveItems"));
            }
            int batchCt = 0;
            try {
                batchCt = Integer.parseInt(batchcount);
            }
            catch (NumberFormatException nfe) {
                throw new SapphireException(tp.translate("Unable to retrieve rule") + " " + nfe.getMessage());
            }
            String DATE_FORMAT = "M/dd/yy HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT disposition, releaseddt  FROM ").append(tableid);
            sql.append(" WHERE ");
            int counter = 0;
            for (String prodVarCol : prodVarColValHM.keySet()) {
                String val = (String)prodVarColValHM.get(prodVarCol);
                if (counter > 0) {
                    sql.append(" AND ");
                }
                if (val != null && val.length() > 0) {
                    sql.append("  ").append(prodVarCol).append(" = ").append(safeSQL.addVar(val)).append(" ");
                } else {
                    sql.append("  (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
                }
                ++counter;
            }
            if (lastTransitionDate != null) {
                sql.append(" AND " + sortingColumn);
                if ("ORA".equals(dbms)) {
                    sql.append(" > to_date(").append(safeSQL.addVar(sdf.format(lastTransitionDate.getTime()))).append(", 'MM/DD/YY HH24:mi:ss') ");
                } else if ("MSS".equals(dbms)) {
                    sql.append(" > convert( datetime, " + safeSQL.addVar(sdf.format(lastTransitionDate.getTime())) + ", 1 )");
                }
            }
            sql.append(" AND (batchmode is null OR batchmode = '' OR batchmode = 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
            sql.append(" AND disposition is not null ORDER BY " + sortingColumn + " DESC");
            Logger.logInfo(" sql to count batches for transition rule evaluation : " + sql.toString());
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
            rulePassed = count >= batchCt;
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

