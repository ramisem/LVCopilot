/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EvaluateExpression {
    private static final boolean __Debug = false;

    public static String evaluate(String expression, String typeOfPage, PageContext pageContext) {
        String finalText = "";
        String sdcid = "";
        String mode = "";
        String queryid = "";
        String categoryid = "";
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        String replicateId = "";
        String paramType = "";
        String paramId = "";
        String dataset = "";
        String variantId = "";
        String paramlistVersionId = "";
        String paramlistId = "";
        String currentUser = "";
        PropertyList plPagedata = null;
        PropertyList plElement = null;
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        SDCProcessor sdcp = new SDCProcessor(pageContext);
        HashMap<Object, Object> hmRequest = new HashMap();
        try {
            plPagedata = (PropertyList)pageContext.getAttribute("pagedata", 2);
            mode = plPagedata.getProperty("mode");
            sdcid = plPagedata.getProperty("sdcid");
            queryid = plPagedata.getProperty("queryid");
            categoryid = plPagedata.getProperty("categoryid");
            keyid1 = plPagedata.getProperty("keyid1");
            keyid2 = plPagedata.getProperty("keyid2");
            keyid3 = plPagedata.getProperty("keyid3");
            plPagedata = (PropertyList)pageContext.getAttribute("pagedata", 2);
            hmRequest = OpalUtil.getRequestParameters(plPagedata);
            keyid1 = EvaluateExpression.evaluateKey(keyid1);
            keyid2 = EvaluateExpression.evaluateKey(keyid2);
            keyid3 = EvaluateExpression.evaluateKey(keyid3);
            hmRequest.put("keyid1", keyid1);
            hmRequest.put("keyid2", keyid2);
            hmRequest.put("keyid3", keyid3);
            hmRequest.put("keycolid1", keyid1);
            hmRequest.put("keycolid2", keyid2);
            hmRequest.put("keycolid3", keyid3);
            if (typeOfPage.equalsIgnoreCase("list")) {
                if (expression.equalsIgnoreCase("")) {
                    expression = "[sdcid] List";
                }
                if (pageContext.getAttribute("list", 2) != null) {
                    plElement = (PropertyList)pageContext.getAttribute("list", 2);
                    sdcid = plElement.getProperty("sdcid");
                }
            } else if (typeOfPage.equalsIgnoreCase("maint")) {
                if (expression.equalsIgnoreCase("")) {
                    expression = "[mode] [sdcid]";
                }
                if (mode.equalsIgnoreCase("")) {
                    mode = "Edit";
                }
                if (pageContext.getAttribute("maint", 2) != null) {
                    plElement = (PropertyList)pageContext.getAttribute("maint", 2);
                    sdcid = plElement.getProperty("sdcid");
                }
            }
            if (queryid.equalsIgnoreCase("") && pageContext.getAttribute("advancedsearch", 2) != null && ((PropertyList)pageContext.getAttribute("advancedsearch", 2)).getPropertyList("querysearch") != null) {
                queryid = ((PropertyList)pageContext.getAttribute("advancedsearch", 2)).getPropertyList("querysearch").getProperty("default");
            }
            if (categoryid.equalsIgnoreCase("") && pageContext.getAttribute("advancedsearch", 2) != null && ((PropertyList)pageContext.getAttribute("advancedsearch", 2)).getPropertyList("categorysearch") != null) {
                categoryid = ((PropertyList)pageContext.getAttribute("advancedsearch", 2)).getPropertyList("categorysearch").getProperty("default");
            }
            if (sdcid != null && sdcid.length() > 0 && !sdcid.equalsIgnoreCase("[sdcid]")) {
                PropertyList plSdcProps = sdcp.getProperties(sdcid);
                hmRequest.put("singular", tp.translate(StringUtil.initCaps(plSdcProps.getProperty("singular"))));
                hmRequest.put("plural", tp.translate(StringUtil.initCaps(plSdcProps.getProperty("plural"))));
            }
            finalText = expression;
            if (!mode.equalsIgnoreCase("")) {
                hmRequest.put("mode", tp.translate(mode));
            }
            if (!sdcid.equalsIgnoreCase("")) {
                hmRequest.put("sdcid", tp.translate(sdcid));
            }
            if (!queryid.equalsIgnoreCase("")) {
                hmRequest.put("queryid", tp.translate(queryid));
            }
            if (!categoryid.equalsIgnoreCase("")) {
                hmRequest.put("categoryid", tp.translate(categoryid));
            }
        }
        catch (Exception ex) {
            Trace.logDebug("OPAL_ERR: EvaluateExpression.evaluate -> exception thrown: " + ex);
        }
        finalText = tp.translatePartial(finalText);
        finalText = tp.translate(finalText);
        if (hmRequest != null) {
            for (String key : hmRequest.keySet()) {
                finalText = StringUtil.replaceAll(finalText, "[" + key + "]", (String)hmRequest.get(key));
            }
        }
        return finalText;
    }

    public static void evaluateKeyColumns(PageContext pageContext, String elementid) {
        if (pageContext == null || elementid == null) {
            return;
        }
        QueryProcessor qp = new QueryProcessor(pageContext);
        SDCProcessor sdcProc = new SDCProcessor(pageContext);
        PropertyList element = (PropertyList)JstlUtil.evaluateExpression("${" + elementid + "}", pageContext);
        String descColId = "";
        String columnId = "";
        String newColumnId = "";
        if (element == null) {
            return;
        }
        String sdcId = element.getProperty("sdcid");
        ArrayList primaryKeyColumns = SdcInfo.getPrimaryKeys(sdcId, qp);
        if (sdcProc.getSDCProperties(sdcId) != null) {
            descColId = (String)sdcProc.getSDCProperties(sdcId).get("desccol");
        }
        if (primaryKeyColumns == null) {
            return;
        }
        int primaryColumnCount = primaryKeyColumns.size();
        for (int count = 0; count < primaryColumnCount; ++count) {
            pageContext.setAttribute("keycolid" + (count + 1), (Object)((String)primaryKeyColumns.get(count)));
        }
        PropertyListCollection elementColumns = element.getCollection("columns");
        if (elementColumns != null) {
            for (int columnCount = 0; columnCount < elementColumns.size(); ++columnCount) {
                PropertyList elementColumn = elementColumns.getPropertyList(columnCount);
                if (elementColumn == null || (columnId = elementColumn.getProperty("columnid")) == null) continue;
                if (columnId.equals("keycolid1") || columnId.equals("[keycolid1]")) {
                    newColumnId = (String)pageContext.getAttribute("keycolid1");
                    if (newColumnId == null) continue;
                    elementColumn.setProperty("columnid", newColumnId);
                    continue;
                }
                if (columnId.equals("keycolid2") || columnId.equals("[keycolid2]")) {
                    newColumnId = (String)pageContext.getAttribute("keycolid2");
                    if (newColumnId == null) continue;
                    elementColumn.setProperty("columnid", newColumnId);
                    continue;
                }
                if (columnId.equals("keycolid3") || columnId.equals("[keycolid3]")) {
                    newColumnId = (String)pageContext.getAttribute("keycolid3");
                    if (newColumnId == null) continue;
                    elementColumn.setProperty("columnid", newColumnId);
                    continue;
                }
                if (!columnId.equals("desccol") && !columnId.equals("[desccol]") || descColId == null) continue;
                elementColumn.setProperty("columnid", descColId);
            }
        }
    }

    public static String evaluateKey(String key) {
        Pattern pattern = Pattern.compile("\\(auto_keyid1_(\\d)*\\)");
        key = key == null || key.trim().length() == 0 || pattern.matcher(key).matches() || key.split(";").length == 0 ? "" : (key.split(";").length == 1 ? key : key.split(";")[0] + ";...");
        return key;
    }
}

