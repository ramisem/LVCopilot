/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pages.prodvariantruleeditor;

import com.labvantage.sapphire.pages.prodvariantruleeditor.RuleTypeProcessor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class BatchCountLevelRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append("<input type=\"text\" size=\"3\" name=\"batch\" >");
        rowEditor.append(tp.translate("(th) item "));
        return rowEditor.toString();
    }

    @Override
    public String getSummary(String ruledefinition, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        StringBuffer summarytext = new StringBuffer();
        try {
            JSONObject json = new JSONObject(ruledefinition);
            String ruleno = json.getString("ruleno");
            String level = json.getString("level");
            String ruletype = json.getString("ruletype");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'LevelRuleTypes' and refvalueid = " + safeSQL.addVar(ruletype.replaceAll("'", "''"));
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            JSONObject data = json.getJSONObject("data");
            String batch = data.getString("batch");
            safeSQL.reset();
            String sql1 = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'ProdVariantRuleLevel' and refvalueid = " + safeSQL.addVar(level);
            DataSet ds1 = qp.getPreparedSqlDataSet(sql1, safeSQL.getValues());
            String levelDisplayValue = ds1.getValue(0, "refdisplayvalue");
            summarytext.append(tp.translate(". Set Level to ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(levelDisplayValue)).append(" ");
            if (ds.getValue(0, "refdisplayvalue").length() > 0) {
                summarytext.append(tp.translate(ds.getValue(0, "refdisplayvalue")));
            } else {
                summarytext.append(tp.translate(ruletype));
            }
            summarytext.append("</font>").append("&nbsp;").append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(batch)).append("</font>").append(tp.translate("(th) item "));
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return summarytext.toString();
    }

    @Override
    public boolean evaluateRule(String levelid, String ruledefinition, String sortingCol, String tableid, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValHM, Calendar lastTransitionDate, String dbms) throws SapphireException {
        boolean rulePassed = true;
        try {
            JSONObject rule = new JSONObject(ruledefinition);
            String batch = rule.getString("batch");
            String DATE_FORMAT = "M/dd/yy HH:mm:ss";
            if ("MSS".equals(dbms)) {
                DATE_FORMAT = "M/dd/yy HH:mm:ss.SSS";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            ConfigurationProcessor cp = new ConfigurationProcessor(qp.getConnectionid());
            PropertyList policy = cp.getPolicy("BatchSamplePolicy", "Sapphire Custom");
            boolean firstBatchShouldPass = "Y".equalsIgnoreCase(policy.getProperty("batchcountlevelrulepassforfirstbatch", "Y"));
            int batchCt = 0;
            try {
                batchCt = Integer.parseInt(batch);
            }
            catch (NumberFormatException nfe) {
                throw new SapphireException(tp.translate("Unable to retrieve rule") + " " + nfe.getMessage());
            }
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" SELECT Max (").append(sortingCol).append(") maxcreatedt FROM ").append(tableid).append(" WHERE levelid = ").append(safeSQL.addVar(levelid)).append(" ");
            for (String prodVarCol : prodVarColValHM.keySet()) {
                String val = (String)prodVarColValHM.get(prodVarCol);
                if (val != null && val.length() > 0) {
                    sql.append(" AND ").append(prodVarCol).append(" =").append(safeSQL.addVar(val)).append(" ");
                    continue;
                }
                sql.append(" AND (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
            }
            sql.append(" AND batchstatus != 'Cancelled' ");
            DataSet dsMaxCreateDt = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            Calendar maxCreateDt = dsMaxCreateDt.getCalendar(0, "maxcreatedt");
            safeSQL.reset();
            sql.setLength(0);
            sql.append("SELECT count(*) FROM ").append(tableid).append(" WHERE ");
            if (firstBatchShouldPass) {
                sql.append(sortingCol).append(" > ");
                sql.append("(  SELECT Max (").append(sortingCol).append(") FROM ").append(tableid).append(" WHERE levelid = ").append(safeSQL.addVar(levelid)).append(" ");
                for (String prodVarCol : prodVarColValHM.keySet()) {
                    String val = (String)prodVarColValHM.get(prodVarCol);
                    if (val != null && val.length() > 0) {
                        sql.append(" AND ").append(prodVarCol).append(" =").append(safeSQL.addVar(val)).append(" ");
                        continue;
                    }
                    sql.append(" AND (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
                }
                sql.append(" AND batchstatus != 'Cancelled' ) ");
            } else if (maxCreateDt != null) {
                sql.append(sortingCol);
                if ("ORA".equals(dbms)) {
                    sql.append(" > to_date(").append(safeSQL.addVar(sdf.format(maxCreateDt.getTime()))).append(", 'MM/DD/YY HH24:mi:ss') ");
                } else if ("MSS".equals(dbms)) {
                    sql.append(" > convert( datetime, " + safeSQL.addVar(sdf.format(maxCreateDt.getTime())) + ", 1 )");
                }
            }
            if (lastTransitionDate != null) {
                sql.append(firstBatchShouldPass || maxCreateDt != null ? " AND " : " ").append(sortingCol);
                if ("ORA".equals(dbms)) {
                    sql.append(" > to_date(").append(safeSQL.addVar(sdf.format(lastTransitionDate.getTime()))).append(", 'MM/DD/YY HH24:mi:ss') ");
                } else if ("MSS".equals(dbms)) {
                    sql.append(" > convert( datetime, " + safeSQL.addVar(sdf.format(lastTransitionDate.getTime())) + ", 1 )");
                }
            }
            Iterator it1 = prodVarColValHM.keySet().iterator();
            int loopCtr = 0;
            while (it1.hasNext()) {
                String prodVarCol = (String)it1.next();
                String val = (String)prodVarColValHM.get(prodVarCol);
                sql.append(firstBatchShouldPass || maxCreateDt != null || lastTransitionDate != null || loopCtr > 0 ? " AND " : " ");
                if (val != null && val.length() > 0) {
                    sql.append(prodVarCol).append(" =").append(safeSQL.addVar(val)).append(" ");
                } else {
                    sql.append("(").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
                }
                ++loopCtr;
            }
            sql = sql.append(" AND (batchmode is null OR batchmode = '' OR batchmode = 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
            sql.append(" AND levelid is not null");
            sql = sql.append(" AND " + sortingCol + " is not null");
            int count = qp.getPreparedCount(sql.toString(), safeSQL.getValues());
            rulePassed = (count + 1) % batchCt == 0 || count == 0 && maxCreateDt == null && firstBatchShouldPass;
        }
        catch (JSONException e) {
            throw new SapphireException(e.getMessage());
        }
        return rulePassed;
    }

    private static class UserMessages {
        static final String SETLEVELTO = ". Set Level to ";
        static final String BATCH = "(th) item ";

        private UserMessages() {
        }
    }
}

