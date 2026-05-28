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

public class DateRangeLevelRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        int i;
        DataSet batchStatusDS = qp.getRefTypeDataSet("Disposition");
        DataSet levelDS = qp.getRefTypeDataSet("ProdVariantRuleLevel");
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append(tp.translate(" less than "));
        rowEditor.append("<input type=\"text\" size=\"3\" name=\"batchcount\">");
        rowEditor.append(tp.translate(" items(s) "));
        rowEditor.append("<select name=\"batchStatus\" >");
        rowEditor.append("<option value=\"\"></option>");
        for (i = 0; i < batchStatusDS.size(); ++i) {
            rowEditor.append("<option value=\"").append(batchStatusDS.getValue(i, "refvalueid")).append("\">");
            if (batchStatusDS.getValue(i, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(batchStatusDS.getValue(i, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(batchStatusDS.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select>");
        rowEditor.append(" <select name=\"batchLevel\" >");
        rowEditor.append("<option value=\"\"></option>");
        for (i = 0; i < levelDS.size(); ++i) {
            rowEditor.append("<option value=\"").append(levelDS.getValue(i, "refvalueid")).append("\">");
            if (levelDS.getValue(i, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(levelDS.getValue(i, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(levelDS.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select> ");
        rowEditor.append("<select name=\"qualifier\" id=\"qualifier\" onChange=\"prodvariantruleeditor.getEdiorforDateRange(this)\"> ");
        rowEditor.append("<option value=\"\">&nbsp;</option>");
        rowEditor.append("<option value=\"within\"> ");
        rowEditor.append("within");
        rowEditor.append("</option> ");
        rowEditor.append("</select> ");
        rowEditor.append("<span name=\"daterangediv\"  id=\"daterangediv\" style=\"visibility:hidden\"> ");
        rowEditor.append("<input type=\"text\" size=\"3\" name=\"number\">&nbsp;");
        DataSet ds = qp.getRefTypeDataSet("AgeUnit");
        rowEditor.append("<select name=\"calendarunit\"> ");
        for (int i2 = 0; i2 < ds.size(); ++i2) {
            rowEditor.append("<option value=\"").append(ds.getValue(i2, "refvalueid")).append("\">");
            if (ds.getValue(i2, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(ds.getValue(i2, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(ds.getValue(i2, "refvalueid")));
            }
            rowEditor.append("</option> ");
        }
        rowEditor.append("</select> ");
        rowEditor.append("</span> ");
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
            String batchcount = data.getString("batchcount");
            String number = data.getString("number");
            String batchStatus = data.getString("batchStatus");
            String batchLevel = data.getString("batchLevel");
            String calendarunits = data.getString("calendarunit");
            String qualifier = data.getString("qualifier");
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
            summarytext.append(tp.translate(" less than "));
            summarytext.append("</font>").append("&nbsp;").append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(batchcount)).append("</font>").append(tp.translate(" items(s) ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(batchStatus)).append("&nbsp;").append(tp.translate(batchLevel)).append("</font> ");
            if (qualifier != null && qualifier.length() > 0) {
                summarytext.append(tp.translate("within ")).append("").append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(number)).append(" ").append(tp.translate(calendarunits)).append("</font>");
            }
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return summarytext.toString();
    }

    @Override
    public boolean evaluateRule(String levelid, String ruledefinition, String sortingColumn, String sdcid, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValHM, Calendar lastTransitionDate, String dbms) throws SapphireException {
        boolean rulePassed = true;
        try {
            JSONObject rule = new JSONObject(ruledefinition);
            String batchStatus = rule.getString("batchStatus");
            String batchLevel = rule.getString("batchLevel");
            String calendarunit = rule.getString("calendarunit");
            String batchcount = rule.getString("batchcount");
            String number = rule.getString("number");
            String qualifier = rule.getString("qualifier");
            int n = 0;
            int batchCt = 0;
            if (number != null && number.length() > 0) {
                try {
                    n = Integer.parseInt(number);
                }
                catch (NumberFormatException nfe) {
                    throw new SapphireException(tp.translate("Unable to retrieve rule") + " " + nfe.getMessage());
                }
            }
            try {
                batchCt = Integer.parseInt(batchcount);
            }
            catch (NumberFormatException nfe) {
                throw new SapphireException(tp.translate("Unable to retrieve rule") + " " + nfe.getMessage());
            }
            if (qualifier != null && qualifier.equalsIgnoreCase("within")) {
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT disposition, levelid  FROM ").append(sdcid);
                sql.append(" WHERE ").append(sortingColumn);
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
                if ("ORA".equals(dbms)) {
                    sql.append(" > to_date(").append(safeSQL.addVar(sdf.format(rightNow.getTime()))).append(", 'MM/DD/YY') ");
                } else if ("MSS".equals(dbms)) {
                    sql.append(" > convert( datetime, " + safeSQL.addVar(sdf.format(rightNow.getTime())) + ", 1 )");
                }
                for (String prodVarCol : prodVarColValHM.keySet()) {
                    String val = (String)prodVarColValHM.get(prodVarCol);
                    if (val != null && val.length() > 0) {
                        sql.append(" AND ").append(prodVarCol).append(" =").append(safeSQL.addVar(val)).append(" ");
                        continue;
                    }
                    sql.append(" AND (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
                }
                sql.append(" AND (batchmode is null or batchmode = '' or batchmode = 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
                sql.append(" ORDER BY " + sortingColumn + " DESC");
                Logger.logInfo("SQL to retieve batch count based on level rule " + sql.toString());
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                int counter = 0;
                for (int i = 1; i < ds.size(); ++i) {
                    if (batchStatus.length() > 0 && batchLevel.length() > 0) {
                        if (!ds.getValue(i, "disposition").equals(batchStatus) || !ds.getValue(i, "levelid").equals(batchLevel)) continue;
                        ++counter;
                        continue;
                    }
                    if (batchStatus.length() > 0 && batchLevel.length() == 0) {
                        if (!ds.getValue(i, "disposition").equals(batchStatus)) continue;
                        ++counter;
                        continue;
                    }
                    if (batchStatus.length() == 0 && batchLevel.length() > 0) {
                        if (!ds.getValue(i, "levelid").equals(batchLevel)) continue;
                        ++counter;
                        continue;
                    }
                    ++counter;
                }
                rulePassed = counter < batchCt;
            } else if (qualifier == null || qualifier.length() == 0) {
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                if ("ORA".equals(dbms)) {
                    sql.append(" SELECT * FROM ( ");
                    sql.append("SELECT disposition, levelid  FROM ").append(sdcid);
                }
                if ("MSS".equals(dbms)) {
                    sql.append(" SELECT  TOP " + (batchCt + 1) + "   disposition, levelid   FROM ").append(sdcid);
                }
                sql.append(" WHERE ");
                int length = sql.length();
                if (lastTransitionDate != null) {
                    sql.append(sortingColumn);
                    if ("ORA".equals(dbms)) {
                        sql.append(" > to_date(").append(safeSQL.addVar(new SimpleDateFormat("M/dd/yy HH:mm:ss").format(lastTransitionDate.getTime()))).append(", 'MM/DD/YY HH24:mi:ss') ");
                    } else if ("MSS".equals(dbms)) {
                        sql.append(" > convert( datetime, " + safeSQL.addVar(new SimpleDateFormat("M/dd/yy HH:mm:ss").format(lastTransitionDate.getTime())) + ", 1 )");
                    }
                }
                for (String prodVarCol : prodVarColValHM.keySet()) {
                    String val = (String)prodVarColValHM.get(prodVarCol);
                    if (sql.length() > length) {
                        sql.append(" AND ");
                    }
                    if (val != null && val.length() > 0) {
                        sql.append("  ").append(prodVarCol).append(" =").append(safeSQL.addVar(val)).append(" ");
                        continue;
                    }
                    sql.append("  (").append(prodVarCol).append(" is null ").append(" OR ").append(prodVarCol).append(" = '' )");
                }
                sql.append(" AND (batchmode is null OR batchmode ='' or batchmode = 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
                sql.append(" ORDER BY " + sortingColumn + " DESC");
                if ("ORA".equals(dbms)) {
                    sql.append(" ) WHERE rownum <= " + safeSQL.addVar(batchCt + 1));
                }
                Logger.logInfo("SQL to retieve batch count based on level rule " + sql.toString());
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                int counter = 0;
                for (int i = 1; i < ds.size(); ++i) {
                    if (batchStatus.length() > 0 && batchLevel.length() > 0) {
                        if (ds.getValue(i, "disposition").equals(batchStatus) && ds.getValue(i, "levelid").equals(batchLevel)) {
                            ++counter;
                            continue;
                        }
                        counter = -1;
                        break;
                    }
                    if (batchStatus.length() > 0 && batchLevel.length() == 0) {
                        if (ds.getValue(i, "disposition").equals(batchStatus)) {
                            ++counter;
                            continue;
                        }
                        counter = -1;
                        break;
                    }
                    if (batchStatus.length() == 0 && batchLevel.length() > 0) {
                        if (ds.getValue(i, "levelid").equals(batchLevel)) {
                            if (++counter != batchCt - 1) continue;
                            break;
                        }
                        counter = -1;
                        break;
                    }
                    ++counter;
                }
                rulePassed = counter != -1 && counter < batchCt;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return rulePassed;
    }

    private static class UserMessages {
        static final String SETLEVELTO = ". Set Level to ";
        static final String ON = " on ";
        static final String BATCHES = " items(s) ";
        static final String INTHELAST = "within ";
        static final String LESSTHAN = " less than ";

        private UserMessages() {
        }
    }
}

