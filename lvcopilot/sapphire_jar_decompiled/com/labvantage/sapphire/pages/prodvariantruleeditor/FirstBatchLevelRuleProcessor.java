/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pages.prodvariantruleeditor;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pages.prodvariantruleeditor.RuleTypeProcessor;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class FirstBatchLevelRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private int quaterStartIndexForCurrentMonth;

    public int getQuaterStartIndexForCurrentMonth() {
        return this.quaterStartIndexForCurrentMonth;
    }

    public void setQuaterStartIndexForCurrentMonth(int quaterStartIndexForCurrentMonth) {
        this.quaterStartIndexForCurrentMonth = quaterStartIndexForCurrentMonth;
    }

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append("<input type='text' name='number_of_batches' id='number_of_batches'  value='1' onkeypress='return prodvariantruleeditor.isNumberKey(event)' onblur='prodvariantruleeditor.checkNumber(this)' style='width:25px'/>");
        rowEditor.append(tp.translate(" item(s) of the "));
        DataSet ds = qp.getRefTypeDataSet("FiscalYearUnits");
        rowEditor.append("<select name=\"unit\" id=\"unit\"  onchange=\"prodvariantruleeditor.onChangeOfUnitDefault(this)\">");
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
        StringBuffer unitOptionsHtml = new StringBuffer();
        for (int i = 1; i <= 28; ++i) {
            unitOptionsHtml.append("<option value='").append(i).append("'>").append(i).append("</option>");
        }
        rowEditor.append("<span name='starting_from' id='starting_from'>" + tp.translate(" starting from ") + "<span name='day_text' id='day_text'>" + tp.translate(" day ") + "</span><select name='unit_options' id='unit_options' >" + unitOptionsHtml + "</select><span  name='month_text' id='month_text' style='display:none;'>" + tp.translate(" month ") + "</span></span>");
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
            String calendarunits = data.getString("unit");
            String numberOfBatches = null;
            String unitOptions = null;
            try {
                numberOfBatches = data.getString("number_of_batches");
            }
            catch (JSONException e) {
                numberOfBatches = "1";
            }
            ConfigurationProcessor cp = new ConfigurationProcessor(qp.getConnectionid());
            if (calendarunits != null && !calendarunits.isEmpty()) {
                if (calendarunits.toLowerCase().startsWith("month")) {
                    try {
                        unitOptions = tp.translate(" day ") + data.getString("unit_options");
                    }
                    catch (JSONException e) {
                        unitOptions = tp.translate(" day ") + "1";
                    }
                } else if (calendarunits.toLowerCase().startsWith("quarter")) {
                    try {
                        unitOptions = data.getString("unit_options");
                        if (unitOptions != null && !unitOptions.isEmpty() && unitOptions.trim().equals("0")) {
                            unitOptions = tp.translate("1st") + tp.translate(" month ");
                        } else if (unitOptions != null && !unitOptions.isEmpty() && unitOptions.trim().equals("1")) {
                            unitOptions = tp.translate("2nd") + tp.translate(" month ");
                        } else if (unitOptions != null && !unitOptions.isEmpty() && unitOptions.trim().equals("2")) {
                            unitOptions = tp.translate("3rd") + tp.translate(" month ");
                        }
                    }
                    catch (JSONException e) {
                        unitOptions = tp.translate("1st") + tp.translate(" month ");
                    }
                } else if (calendarunits.toLowerCase().startsWith("year")) {
                    int monthIndex = -1;
                    try {
                        unitOptions = data.getString("unit_options");
                        if (unitOptions != null && !unitOptions.isEmpty()) {
                            monthIndex = Integer.parseInt(unitOptions);
                        }
                    }
                    catch (JSONException e) {
                        String fiscalYearStartsIn = "";
                        PropertyList policy = cp.getPolicy("SamplingPlanPolicy", "Sapphire Custom");
                        if (policy != null) {
                            fiscalYearStartsIn = policy.getProperty("financialyear");
                        }
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(fiscalYearStartsIn);
                        }
                        catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        Calendar cal = DateTimeUtil.getNowCalendar();
                        cal.setTime(date);
                        monthIndex = cal.get(2);
                    }
                    DateFormatSymbols dateFormatSymbols = null;
                    ConnectionProcessor connectionProcessor = new ConnectionProcessor(cp.getConnectionid());
                    ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(cp.getConnectionid());
                    if (connectionInfo.getLocale() != null && !connectionInfo.getLocale().isEmpty()) {
                        dateFormatSymbols = new DateFormatSymbols(new Locale(connectionInfo.getLocale().split("_")[0], connectionInfo.getLocale()));
                    } else {
                        Locale defaultLocale = Locale.getDefault();
                        dateFormatSymbols = new DateFormatSymbols(defaultLocale);
                    }
                    unitOptions = dateFormatSymbols.getMonths()[monthIndex];
                }
            }
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
            summarytext.append(" " + numberOfBatches + " ");
            summarytext.append("</font>").append("").append(tp.translate(" item(s) of the ")).append("").append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(" ").append(tp.translate(calendarunits)).append("</font>");
            summarytext.append(tp.translate(" starting from "));
            summarytext.append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(unitOptions).append("</font>");
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return summarytext.toString();
    }

    @Override
    public boolean evaluateRule(String levelid, String ruledefinition, String sortingColumn, String tableid, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValHM, Calendar lastTransitionDate, String dbms) throws SapphireException {
        boolean isRulePassed = true;
        try {
            JSONObject rule = new JSONObject(ruledefinition);
            String unit = rule.getString("unit");
            String numberOfBatches = null;
            String unitOptions = null;
            try {
                unitOptions = rule.getString("unit_options");
            }
            catch (JSONException e) {
                unitOptions = null;
            }
            String fiscalYrStartMonth = FirstBatchLevelRuleProcessor.getFiscalFromPolicy(new ConfigurationProcessor(qp.getConnectionid()));
            int indexOfFiscalStartMonth = this.getMonthIndex(fiscalYrStartMonth);
            Calendar rightNow = Calendar.getInstance();
            int currentMonthIndex = rightNow.get(2);
            int month = indexOfFiscalStartMonth;
            if (month < 0) {
                throw new SapphireException(tp.translate("Fiscal Year not defined in policy"));
            }
            if (unit.equalsIgnoreCase("Years")) {
                int indexOfStartMonthOfYear = indexOfFiscalStartMonth;
                try {
                    if (unitOptions != null && !unitOptions.trim().isEmpty()) {
                        indexOfStartMonthOfYear = Integer.parseInt(unitOptions.trim());
                    }
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                rightNow.set(5, 1);
                rightNow.set(2, indexOfStartMonthOfYear);
                rightNow.add(1, currentMonthIndex >= indexOfStartMonthOfYear ? 0 : -1);
                rightNow.set(1, rightNow.get(1));
            } else if (unit.equalsIgnoreCase("Months")) {
                int day = 1;
                if (unitOptions != null && !unitOptions.trim().isEmpty()) {
                    try {
                        day = Integer.parseInt(unitOptions);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                rightNow.set(rightNow.get(1), rightNow.get(2), day);
            } else if (unit.equalsIgnoreCase("Quarter")) {
                int quaterStartIndexForCurrentMonth = 0;
                int startOfQuaterIndex = indexOfFiscalStartMonth;
                int[] quater1 = new int[3];
                startOfQuaterIndex = this.addMonthsToQuater(quater1, startOfQuaterIndex, currentMonthIndex, quaterStartIndexForCurrentMonth);
                int[] quater2 = new int[3];
                startOfQuaterIndex = this.addMonthsToQuater(quater2, startOfQuaterIndex, currentMonthIndex, quaterStartIndexForCurrentMonth);
                int[] quater3 = new int[3];
                startOfQuaterIndex = this.addMonthsToQuater(quater3, startOfQuaterIndex, currentMonthIndex, quaterStartIndexForCurrentMonth);
                int[] quater4 = new int[3];
                startOfQuaterIndex = this.addMonthsToQuater(quater4, startOfQuaterIndex, currentMonthIndex, quaterStartIndexForCurrentMonth);
                int quaterStartIndex = this.getQuaterStartIndexForCurrentMonth();
                int quaterEndIndex = (quaterStartIndex + 2) % 12;
                int monthOfQuarter = quaterStartIndex;
                if (unitOptions != null && !unitOptions.trim().isEmpty()) {
                    try {
                        monthOfQuarter = (quaterStartIndex + Integer.parseInt(unitOptions)) % 12;
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                this.determineDateForQuarter(rightNow, quaterStartIndex, quaterEndIndex, monthOfQuarter);
                rightNow.set(2, monthOfQuarter);
                rightNow.set(5, 1);
            }
            String DATE_FORMAT = "M/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT count(*) FROM ").append(tableid).append(" WHERE ").append(sortingColumn);
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
            sql.append(" AND (batchmode is null OR batchmode = '' OR batchmode= 'Primary' OR batchmode = 'Formulation') AND batchstatus != 'Cancelled'");
            sql.append(" AND levelid is not null");
            int count = qp.getPreparedCount(sql.toString(), safeSQL.getValues());
            int batches = 1;
            try {
                numberOfBatches = rule.getString("number_of_batches");
                if (numberOfBatches != null && !numberOfBatches.trim().isEmpty()) {
                    batches = Integer.parseInt(numberOfBatches);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            isRulePassed = count + 1 > 0 && count + 1 <= batches;
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return isRulePassed;
    }

    private void determineDateForQuarter(Calendar rightNow, int quaterStartIndex, int quaterEndIndex, int monthOfQuarter) {
        int currMonthIndex = rightNow.get(2);
        if (quaterStartIndex > quaterEndIndex) {
            if (currMonthIndex < 2 && monthOfQuarter > 9) {
                rightNow.add(1, -1);
            } else if (currMonthIndex > 9 && monthOfQuarter < 2) {
                rightNow.add(1, 1);
            }
        }
    }

    private int addMonthsToQuater(int[] quater, int startOfQuaterIndex, int currentMonthIndex, int quaterStartIndexForCurrentMonth) {
        for (int i = 0; i < 3; ++i) {
            if (startOfQuaterIndex > 11) {
                startOfQuaterIndex = 0;
            }
            quater[i] = startOfQuaterIndex;
            if (currentMonthIndex == startOfQuaterIndex) {
                this.setQuaterStartIndexForCurrentMonth(quater[0]);
            }
            ++startOfQuaterIndex;
        }
        return startOfQuaterIndex;
    }

    private int getMonthIndex(String fiscalYrStartMonth) {
        int index = 0;
        String[] monthsArr = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < monthsArr.length; ++i) {
            if (!monthsArr[i].equalsIgnoreCase(fiscalYrStartMonth)) continue;
            index = i;
            break;
        }
        return index;
    }

    public static String getFiscalFromPolicy(ConfigurationProcessor cp) throws SapphireException {
        String fiscalyear = "";
        PropertyList policy = cp.getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            fiscalyear = policy.getProperty("financialyear");
        }
        return fiscalyear;
    }

    private static class UserMessages {
        static final String SETLEVELTO = ". Set Level to ";
        static final String BATCH = " item(s) of the ";
        static final String INTHELAST = "in the last ";
        static final String STARTING_FROM = " starting from ";
        static final String DAY_TEXT = " day ";
        static final String MONTH_TEXT = " month ";

        private UserMessages() {
        }
    }
}

