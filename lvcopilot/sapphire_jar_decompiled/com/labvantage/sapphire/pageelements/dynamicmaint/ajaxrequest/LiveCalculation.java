/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.ActionConstants;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class LiveCalculation
extends BaseAjaxRequest
implements ActionConstants {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        JSONObject sdidataitems;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sSdidataitems = ar.getRequestParameter("sdidataitem");
        try {
            sdidataitems = new JSONObject(sSdidataitems);
        }
        catch (Exception e) {
            this.logger.error("Exception while parsing: " + sSdidataitems, e);
            throw new ServletException("Failed to parse form data from JSON object", (Throwable)e);
        }
        HashMap<String, String> calcResults = new HashMap<String, String>();
        boolean resultChanged = true;
        int counter = 0;
        while (resultChanged && counter < 10) {
            resultChanged = false;
            ++counter;
            for (int i = 0; i < sdidataitems.length(); ++i) {
                boolean paramsFilled;
                JSONObject sdidataitemRow = sdidataitems.optJSONObject("" + i);
                String expr = sdidataitemRow.optString("calcrule", "");
                String transformRule = sdidataitemRow.optString("calcrule", "");
                String displayFormat = sdidataitemRow.optString("displayformat");
                HashMap<String, Object> expressionParams = this.extractParams(expr);
                if (expressionParams.isEmpty() || !(paramsFilled = this.fillParamValues(expressionParams, sdidataitems, sdidataitemRow))) continue;
                String oldDisplayvalue = sdidataitemRow.optString("displayvalue", "");
                String displayvalue = this.doCalculation(expr, expressionParams);
                if (displayvalue.equals(oldDisplayvalue)) continue;
                displayvalue = this.evalTransformRule(transformRule, displayvalue);
                displayvalue = this.evalDisplayFormat(displayFormat, displayvalue);
                String rowid = sdidataitemRow.optString("__rowid", "");
                calcResults.put(rowid, displayvalue);
                try {
                    sdidataitemRow.put("displayvalue", displayvalue);
                    resultChanged = true;
                    continue;
                }
                catch (JSONException je) {
                    this.logger.error("", je);
                }
            }
        }
        ar.addCallbackArgument("calcresults", calcResults);
        ar.print();
    }

    private String evalTransformRule(String transformRule, String displayvalue) {
        return displayvalue;
    }

    private String evalDisplayFormat(String displayFormat, String displayvalue) {
        return displayvalue;
    }

    private String doCalculation(String expr, HashMap<String, Object> expressionParams) {
        String retval;
        try {
            retval = ExpressionUtil.evaluate(expr, expressionParams);
        }
        catch (SapphireException ex) {
            this.logger.error("", ex);
            retval = "";
        }
        return retval;
    }

    private boolean fillParamValues(HashMap<String, Object> expressionParams, JSONObject sdidataitems, JSONObject currentRow) {
        boolean allParamsFilled = true;
        for (String expressionParam : expressionParams.keySet()) {
            Object paramValue;
            String paramlistversionid = "";
            String variantid = "";
            String dataset = "#";
            String paramtype = "";
            String replicateid = "";
            String paramlistPart = "";
            String paramPart = "";
            String[] splitParam = expressionParam.split("\\|");
            if (splitParam.length == 3) {
                allParamsFilled = false;
                break;
            }
            if (splitParam.length == 2) {
                paramlistPart = splitParam[0];
                paramPart = splitParam[1];
            } else if (splitParam.length == 1) {
                paramPart = splitParam[0];
            }
            String[] arrParamlist = paramlistPart.split(";");
            String paramlistid = arrParamlist[0];
            if (arrParamlist.length > 1) {
                paramlistversionid = arrParamlist[1];
            }
            if (arrParamlist.length > 1) {
                variantid = arrParamlist[2];
            }
            if (arrParamlist.length > 1) {
                dataset = arrParamlist[3];
            }
            String[] arrParam = paramPart.split(";");
            String paramid = arrParam[0];
            if (arrParam.length > 1) {
                paramtype = arrParam[1];
            }
            if (arrParam.length > 2) {
                replicateid = arrParam[2];
            }
            if ((paramValue = this.getParamValue(paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, currentRow, sdidataitems)) == null) {
                allParamsFilled = false;
                break;
            }
            expressionParams.put(expressionParam, paramValue);
        }
        return allParamsFilled;
    }

    private HashMap<String, Object> extractParams(String expr) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        char[] chars = expr.toCharArray();
        StringBuilder param = new StringBuilder();
        boolean isThisParam = false;
        for (char c : chars) {
            if (c == '[') {
                isThisParam = true;
                continue;
            }
            if (c == ']') {
                isThisParam = false;
                params.put(param.toString(), null);
                param = new StringBuilder();
                continue;
            }
            if (!isThisParam) continue;
            param.append(c);
        }
        return params;
    }

    private Object getParamValue(String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid, JSONObject currentRow, JSONObject sdidataitems) {
        ArrayList<String> results = new ArrayList<String>();
        boolean allNumeric = true;
        this.filterResultRows(sdidataitems, "paramlistid", paramlistid, currentRow.optString("paramlistid"));
        this.filterResultRows(sdidataitems, "paramlistversionid", paramlistversionid, currentRow.optString("paramlistversionid"));
        this.filterResultRows(sdidataitems, "variantid", variantid, currentRow.optString("variantid"));
        this.filterResultRows(sdidataitems, "dataset", dataset, currentRow.optString("dataset"));
        this.filterResultRows(sdidataitems, "paramid", paramid, currentRow.optString("paramid"));
        this.filterResultRows(sdidataitems, "paramtype", paramtype, currentRow.optString("paramtype"));
        this.filterResultRows(sdidataitems, "replicateid", replicateid, currentRow.optString("replicateid"));
        for (int i = 0; i < sdidataitems.length(); ++i) {
            JSONObject sdidataitemRow = sdidataitems.optJSONObject("" + i);
            String value = sdidataitemRow.optString("enteredtext__", null);
            if (value == null) {
                value = sdidataitemRow.optString("enteredtext", "");
            }
            if (value.equals("")) continue;
            results.add(value);
            try {
                Double.parseDouble(value.replace(',', '.'));
                continue;
            }
            catch (Exception e) {
                allNumeric = false;
            }
        }
        if (results.size() == 1) {
            if (allNumeric) {
                return new BigDecimal(((String)results.get(0)).replace(',', '.'));
            }
            return results.get(0);
        }
        if (results.size() > 0 && allNumeric) {
            BigDecimal[] retval = new BigDecimal[results.size()];
            for (int i = 0; i < results.size(); ++i) {
                retval[i] = new BigDecimal((String)results.get(i));
            }
            return retval;
        }
        return null;
    }

    private void filterResultRows(JSONObject sdidataitems, String columnid, String filterValue, String currentRowValue) {
        String rowValue;
        JSONObject sdidataitemRow;
        int i;
        if (filterValue.equals("") || filterValue.equals("*")) {
            return;
        }
        if (filterValue.equals("max")) {
            int maxValue = 0;
            for (i = sdidataitems.length() - 1; i >= 0; --i) {
                sdidataitemRow = sdidataitems.optJSONObject("" + i);
                rowValue = sdidataitemRow.optString(columnid);
                if (Utils.s2i(rowValue) <= maxValue) continue;
                maxValue = Utils.s2i(rowValue);
            }
            filterValue = "" + maxValue;
        }
        if (filterValue.equals("min")) {
            int minValue = 999;
            for (i = sdidataitems.length() - 1; i >= 0; --i) {
                sdidataitemRow = sdidataitems.optJSONObject("" + i);
                rowValue = sdidataitemRow.optString(columnid);
                if (Utils.s2i(rowValue) >= minValue) continue;
                minValue = Utils.s2i(rowValue);
            }
            filterValue = "" + minValue;
        }
        if (filterValue.equals("#")) {
            filterValue = currentRowValue;
        }
        for (int i2 = sdidataitems.length() - 1; i2 >= 0; --i2) {
            JSONObject sdidataitemRow2 = sdidataitems.optJSONObject("" + i2);
            String rowValue2 = sdidataitemRow2.optString(columnid);
            if (filterValue.equals(rowValue2)) continue;
            sdidataitems.remove("" + i2);
        }
    }
}

