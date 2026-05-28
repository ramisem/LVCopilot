/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalRequestInfo;
import com.labvantage.opal.util.PLDataSet;
import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class OpalUtil {
    static String LABVANTAGE_CVS_ID = "$Revision: 98320 $";
    public static final String DELIMITER_COLUMN = "|";
    public static final String DELIMITER_ROW = ";";

    public static String getTab(String id, String name, String width, boolean select) {
        StringBuffer sb = new StringBuffer();
        if (select) {
            sb.append("<td class='tab_select' width='").append(width).append("'");
            sb.append(" id='tab_").append(id).append(" onClick=\"tabSelect(this, '").append(name);
            sb.append("');\">");
            sb.append("<font color='#000000'>").append(name).append("</font></td>");
        } else {
            sb.append("<td class='tab_unselect' width='").append(width).append("'");
            sb.append(" id='tab_").append(id).append(" onClick=\"tabSelect(this, '").append(name);
            sb.append("');\">");
            sb.append("<font color='#000000'>").append(name).append("</font></td>");
        }
        return sb.toString();
    }

    public static String getDynamicTab(String id, String name, boolean select, String path) {
        StringBuffer sb = new StringBuffer();
        if (select) {
            sb.append("<td class='tab_tableft' background='").append(path).append("/tab_main_sel_left.gif' width='5'");
            sb.append(" id='tab_").append(id).append("_left'>&nbsp;</td>");
            sb.append("<td class='tab_tabcenter' background='").append(path).append("/tab_main_sel_center.gif' width='75'");
            sb.append(" id='tab_").append(id).append("' onClick=\"tabSelect(this, '").append(name);
            sb.append("');\" style='cursor: pointer;'>");
            sb.append("<font color='#000000'>").append(name).append("</font></td>");
            sb.append("<td class='tab_tabright' background='").append(path).append("/tab_main_sel_right.gif' width='5'");
            sb.append(" id='tab_").append(id).append("_right'>&nbsp;</td>");
        } else {
            sb.append("<td class='tab_tableft' background='").append(path).append("/tab_main_notsel_left.gif' width='5'");
            sb.append(" id='tab_").append(id).append("_left'>&nbsp;</td>");
            sb.append("<td class='tab_tabcenter' background='").append(path).append("/tab_main_notsel_center.gif' width='75'");
            sb.append(" id='tab_").append(id).append("' onClick=\"tabSelect(this, '").append(name);
            sb.append("');\" style='cursor: pointer;'>");
            sb.append("<font color='#EEEEEE'>").append(name).append("</font></td>");
            sb.append("<td class='tab_tabright' background='").append(path).append("/tab_main_notsel_right.gif' width='5'");
            sb.append(" id='tab_").append(id).append("_right'>&nbsp;</td>");
        }
        return sb.toString();
    }

    public static String getNextSequence(String sdcid, SequenceProcessor sp) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(6);
        int year = calendar.get(1);
        String seq = Integer.toString(day) + year + "-";
        return seq + sp.getSequence(sdcid, seq);
    }

    public static String getDataSetString(String datasetKey, String headingPattern) {
        String datasetString = headingPattern;
        String[] arr = StringUtil.split(datasetKey, DELIMITER_COLUMN);
        try {
            datasetString = StringUtil.replaceAll(datasetString, "[paramlistid]", arr[0]);
            datasetString = StringUtil.replaceAll(datasetString, "[paramlistversionid]", arr[1]);
            datasetString = StringUtil.replaceAll(datasetString, "[variantid]", arr[2]);
            datasetString = StringUtil.replaceAll(datasetString, "[dataset]", arr[3]);
        }
        catch (Exception ex) {
            datasetString = "All Datasets";
        }
        return datasetString;
    }

    public static String getSqlWhereClause(String where) {
        return OpalUtil.getSqlWhereClause(where, false);
    }

    public static String getSqlWhereClause(String where, boolean unicode) {
        return (unicode ? "N'" : "'") + where.replaceAll(DELIMITER_ROW, unicode ? "',N'" : "','") + "'";
    }

    public static boolean allRequiredInputsOk(PageContext pageContext, String sdcId, String keyid1, String queryid, String queryfrom, String querywhere, StringBuffer exMsg) {
        boolean ret = true;
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String errMsg = tp.translatePartial("{{Either of the following combinations is required}}..<br>sdcId + keyid1, sdcId + queryid, queryfrom + querywhere");
        if (sdcId.equalsIgnoreCase("") && keyid1.equalsIgnoreCase("")) {
            exMsg.append("<br>").append(tp.translate("Did not find the SDCId and keyid1 (primary key) for which to retrieve data.")).append("<br>");
            ret = false;
        } else if (sdcId.equalsIgnoreCase("") && queryid.equalsIgnoreCase("")) {
            exMsg.append("<br>").append(tp.translate("Did not find the SDCId and QueryId for which to retrieve data.")).append("<br>");
            ret = false;
        }
        exMsg.append(errMsg);
        return ret;
    }

    public static String getSecurityByPass(PageContext pageContext) {
        return "D".equals(new SDCProcessor(pageContext).getProperty("DataSet", "accesscontrolledflag")) ? "2" : "0";
    }

    public static List stringArrayToList(String[] str) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length; ++i) {
            list.add(i, str[i]);
        }
        return list;
    }

    public static String parseRequestString(PageContext pageContext, String str) {
        StringBuffer sb = new StringBuffer(str);
        ServletRequest req = pageContext.getRequest();
        int i = 0;
        int start = 0;
        while (sb.indexOf("[", start) != -1) {
            int end;
            start = sb.indexOf("[");
            String param = sb.substring(start + 1, end = sb.indexOf("]"));
            String paramvalue = req.getParameter(param);
            if (paramvalue != null && paramvalue.trim().length() > 0) {
                sb.replace(start, ++end, paramvalue);
                start += paramvalue.length();
            }
            if (++i <= 20) continue;
            break;
        }
        return sb.toString();
    }

    public static String parseRequestString(RequestContext requestContext, String str) {
        return OpalUtil.parseRequestString(requestContext, str, false, true);
    }

    public static String parseRequestString(RequestContext requestContext, String str, boolean isForQueryWhere, boolean isOracle) {
        StringBuffer sb = new StringBuffer(str);
        int i = 0;
        int start = 0;
        while (sb.indexOf("[", start) != -1) {
            int end;
            start = sb.indexOf("[");
            String param = sb.substring(start + 1, end = sb.indexOf("]"));
            String paramvalue = requestContext.getProperty(param);
            if (paramvalue != null && paramvalue.trim().length() > 0) {
                sb.replace(start, ++end, isForQueryWhere ? SafeSQL.encodeForSQL(paramvalue, isOracle) : paramvalue);
                start += paramvalue.length();
            }
            if (++i <= 20) continue;
            break;
        }
        return sb.toString();
    }

    public static ArrayList getKeywordTokens(String input) {
        ArrayList<String> alTokens = new ArrayList<String>();
        int start = 0;
        int end = 0;
        while (end < input.length() && (start = input.indexOf("[", start)) >= 0) {
            end = input.indexOf("]", start);
            alTokens.add(input.substring(start + 1, end));
            start = end;
        }
        return alTokens;
    }

    public static String searchAndReplaceTokens(String input, List<String> alTokens, Map<String, String> hmValues, boolean blnIgnoreBlank) {
        return OpalUtil.searchAndReplaceTokens(input, alTokens, hmValues, blnIgnoreBlank, false, true);
    }

    public static String searchAndReplaceTokens(String input, List<String> alTokens, Map<String, String> hmValues, boolean blnIgnoreBlank, boolean isForQueryWhere, boolean isOracle) {
        for (String alToken : alTokens) {
            String token = alToken;
            if (hmValues.get(token) == null) continue;
            if (blnIgnoreBlank) {
                if (hmValues.get(token).equalsIgnoreCase("")) continue;
                input = StringUtil.replaceAll(input, "[" + token + "]", isForQueryWhere ? SafeSQL.encodeForSQL(hmValues.get(token), isOracle) : hmValues.get(token));
                continue;
            }
            input = StringUtil.replaceAll(input, "[" + token + "]", isForQueryWhere ? SafeSQL.encodeForSQL(hmValues.get(token), isOracle) : hmValues.get(token));
        }
        return input;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static HashMap<String, String> getRequestParameters(PropertyList plPagedata) {
        HashMap<String, String> hmRequest = new HashMap<String, String>();
        PropertyList propertyList = plPagedata;
        synchronized (propertyList) {
            for (Object o : plPagedata.keySet()) {
                String propName = (String)o;
                try {
                    String propValue = (String)plPagedata.get(propName);
                    hmRequest.put(propName, propValue);
                }
                catch (Exception exception) {}
            }
        }
        return hmRequest;
    }

    public static TreeSet getUniqueTreeSetOfColumns(ArrayList alCols, DataSet ds) {
        TreeSet<String> tsValues = new TreeSet<String>();
        try {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String colValue = ds.getValue(i, (String)alCols.get(0));
                for (int j = 1; j < alCols.size(); ++j) {
                    colValue = colValue + DELIMITER_COLUMN + ds.getValue(i, (String)alCols.get(j));
                }
                tsValues.add(colValue);
            }
        }
        catch (Exception ex) {
            Logger.logError("Exception at OpalUtil.getUniqueTreeSetOfColumns-> " + ex, ex);
        }
        return tsValues;
    }

    public static String getDatabase(String connectionid) {
        int end;
        String database = null;
        if (connectionid != null && connectionid.length() > 0 && (end = connectionid.indexOf(DELIMITER_COLUMN)) != -1) {
            database = connectionid.substring(0, end);
        }
        return database;
    }

    public static HashMap<String, String> getColumnDataTypeMap(String tableid, QueryProcessor qp) {
        HashMap<String, String> map = null;
        try {
            map = new HashMap<String, String>();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COLUMNID, DATATYPE, COLUMNLENGTH, COLUMNTYPE, PKFLAG, NNFLAG");
            sql.append(" FROM SYSCOLUMN WHERE TABLEID = ").append("?").append("");
            DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), new Object[]{tableid.toLowerCase()});
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    map.put(ds.getString(i, "COLUMNID"), ds.getString(i, "DATATYPE"));
                }
            } else {
                DataSet mapds = qp.getPreparedSqlDataSet("select * from syssdcmap where tableidmap = ?", new Object[]{tableid.toLowerCase()});
                if (mapds != null && mapds.size() > 0) {
                    String newtableid = mapds.getString(0, "tableid", "");
                    String newkey1 = mapds.getString(0, "key1", "");
                    String newkey2 = mapds.getString(0, "key2", "");
                    String newkey3 = mapds.getString(0, "key3", "");
                    String oldkey1 = mapds.getString(0, "keymap1", "");
                    String oldkey2 = mapds.getString(0, "keymap2", "");
                    String oldkey3 = mapds.getString(0, "keymap3", "");
                    sql.setLength(0);
                    sql.append("SELECT COLUMNID, DATATYPE, COLUMNLENGTH, COLUMNTYPE, PKFLAG, NNFLAG");
                    sql.append(" FROM SYSCOLUMN WHERE TABLEID = ").append("?").append("");
                    DataSet _md = qp.getPreparedSqlDataSet(sql.toString(), new Object[]{newtableid});
                    if (_md != null) {
                        for (int i = 0; i < _md.size(); ++i) {
                            String columnid = _md.getValue(i, "COLUMNID", "");
                            if (columnid.equals(newkey1)) {
                                columnid = oldkey1;
                            }
                            if (columnid.equals(newkey2)) {
                                columnid = oldkey2;
                            }
                            if (columnid.equals(newkey3)) {
                                columnid = oldkey3;
                            }
                            map.put(columnid, _md.getValue(i, "DATATYPE"));
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return map;
    }

    private static int arrayListContains(ArrayList al, String value) {
        for (int i = 0; i < al.size(); ++i) {
            if (!(al.get(i) instanceof String)) continue;
            String compare = (String)al.get(i);
            if (value.equals(compare)) {
                return i;
            }
            boolean sw = compare.startsWith("*");
            boolean ew = compare.endsWith("*");
            if (!sw && !ew || !(sw && ew ? value.indexOf(compare.substring(1, compare.length() - 1)) > -1 : (sw ? value.endsWith(compare.substring(1, compare.length())) : ew && value.startsWith(compare.substring(0, compare.length() - 1))))) continue;
            return i;
        }
        return -1;
    }

    public static String getActionButtonFormFields(Map hmValues, boolean blnClearable, String type, ArrayList alExcludeFields) {
        StringBuilder sbHtml = new StringBuilder("\n");
        Set set = hmValues.keySet();
        type = type.equalsIgnoreCase("") ? "hidden" : type;
        String clearable = blnClearable ? "yes" : "no";
        ArrayList<String> added = new ArrayList<String>();
        for (Object aSet : set) {
            String val;
            String fieldName = (String)aSet;
            String fieldValue = (String)hmValues.get(fieldName);
            if (fieldName.equalsIgnoreCase("tracelogid")) continue;
            if (OpalUtil.isEmpty(fieldValue) || fieldValue.equals("undefined")) {
                String string = fieldValue = OpalUtil.isNotEmpty((String)hmValues.get("action_" + fieldName)) ? (String)hmValues.get("action_" + fieldName) : fieldValue;
            }
            if (OpalUtil.isNotEmpty(fieldValue) && ("workitemid".equals(fieldName) || "workiteminstance".equals(fieldName) || "paramlistid".equals(fieldName) || "paramlistversionid".equals(fieldName) || "variantid".equals(fieldName) || "dataset".equals(fieldName))) {
                sbHtml.append("<input name='").append(fieldName).append("' type='hidden' value=\"").append(SafeHTML.encodeForHTMLAttribute(fieldValue)).append("\">\n");
                sbHtml.append("<input name='action_").append(fieldName).append("' type='hidden' value=\"").append(SafeHTML.encodeForHTMLAttribute(fieldValue)).append("\">\n");
                continue;
            }
            String searchName = fieldName;
            if (fieldName.startsWith("action_")) {
                searchName = fieldName.substring(7);
            }
            if (OpalUtil.arrayListContains(alExcludeFields, searchName) != -1 || added.size() != 0 && added.contains(fieldName)) continue;
            added.add(fieldName);
            String string = val = fieldValue == null ? "" : fieldValue;
            if (type.equalsIgnoreCase("hidden") && (val.startsWith("{") || val.endsWith("}") || val.contains("\""))) {
                if (fieldName.startsWith("action_")) {
                    sbHtml.append("<textarea style=\"display:none;\" id=\"").append(fieldName).append("\" name=\"").append(fieldName).append("\" clearable=\"").append(clearable).append("\">");
                } else {
                    sbHtml.append("<textarea style=\"display:none;\" id=\"action_").append(fieldName).append("\" name=\"action_").append(fieldName).append("\" clearable=\"").append(clearable).append("\">");
                }
                sbHtml.append(SafeHTML.encodeForHTML(val));
                sbHtml.append("</textarea>");
                continue;
            }
            if (fieldName.startsWith("action_")) {
                sbHtml.append("<input id=\"").append(fieldName).append("\" name=\"").append(fieldName).append("\" type=\"").append(type).append("\" clearable=\"").append(clearable).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(val)).append("\">\n");
                continue;
            }
            sbHtml.append("<input id=\"action_").append(fieldName).append("\" name=\"action_").append(fieldName).append("\" type=\"").append(type).append("\" clearable=\"").append(clearable).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(val)).append("\">\n");
        }
        return sbHtml.toString();
    }

    public static String getActionButtonFormFields(PageContext pageContext, PropertyList pagedata, boolean clearable, String[] excludePatterns) {
        HashMap<String, String> request;
        ArrayList excludes;
        if (pageContext != null) {
            RequestContext rc;
            Object cachedExludes = pageContext.getServletContext().getAttribute("opalutil_actionbuttonfieldstoexclude");
            if (cachedExludes != null && cachedExludes instanceof ArrayList) {
                excludes = (ArrayList)((ArrayList)cachedExludes).clone();
            } else {
                excludes = OpalUtil.getActionButtonFieldsToExcludeFromRequestParams();
                pageContext.getServletContext().setAttribute("opalutil_actionbuttonfieldstoexclude", (Object)excludes);
            }
            request = pagedata == null ? ((rc = RequestContext.getRequestContext(pageContext)) != null ? (rc.getPropertyList("pagedata") != null ? OpalUtil.getRequestParameters(rc.getPropertyList("pagedata")) : new HashMap()) : new HashMap()) : OpalUtil.getRequestParameters(pagedata);
        } else if (pagedata != null) {
            excludes = OpalUtil.getActionButtonFieldsToExcludeFromRequestParams();
            request = OpalUtil.getRequestParameters(pagedata);
        } else {
            excludes = null;
            request = null;
        }
        if (request != null && excludes != null) {
            String[] elements;
            String ds;
            if (excludePatterns != null && excludePatterns.length > 0) {
                excludes.addAll(Arrays.asList(excludePatterns));
            }
            String string = ds = request.containsKey("__abexcludelist") ? (String)request.get("__abexcludelist") : "";
            if (ds.length() > 0 && (elements = StringUtil.split(ds, DELIMITER_ROW)).length > 0) {
                excludes.addAll(Arrays.asList(elements));
            }
            excludes.add("___abexcludelist");
            return OpalUtil.getActionButtonFormFields(request, clearable, "hidden", excludes);
        }
        return "";
    }

    public static String getActionButtonFormFields(PropertyList plPagedata, String formName) {
        StringBuffer sbHtml = new StringBuffer();
        ArrayList alExcludeFields = OpalUtil.getActionButtonFieldsToExcludeFromRequestParams();
        HashMap<String, String> hmRequest = OpalUtil.getRequestParameters(plPagedata);
        Pattern p = Pattern.compile("pr[0-9]+_");
        Set set = hmRequest.keySet();
        sbHtml.append("\n<script>\n\n");
        for (Object aSet : set) {
            String fieldName = (String)aSet;
            String fieldValue = (String)hmRequest.get(fieldName);
            if (fieldName.contains("restrictivewhere") || fieldName.contains("__request")) {
                fieldValue = EncryptDecrypt.obfsql(fieldValue);
            }
            fieldValue = SafeHTML.encodeForJavaScript(fieldValue);
            if (alExcludeFields.contains(fieldName) || fieldName.startsWith("_") || p.matcher(fieldName).find() || fieldName.indexOf("[__row]") >= 0) continue;
            if (fieldName.startsWith("action_")) {
                sbHtml.append("\tif (typeof(document.").append(formName).append(".").append(fieldName).append(") == 'undefined'){\n");
                sbHtml.append("\t    addField (document.getElementById('").append(formName).append("'), 'hidden', '").append(fieldName).append("', '").append(fieldValue).append("', '").append(fieldName).append("');\n");
                sbHtml.append("\t}\n");
                sbHtml.append("\telse{\n");
                sbHtml.append("\t    document.").append(formName).append(".").append(fieldName).append(".value = '").append(fieldValue).append("';\n");
                sbHtml.append("\t}\n\n");
                continue;
            }
            if ("gwt.codesvr".equals(fieldName)) continue;
            sbHtml.append("\tif (typeof(document.").append(formName).append(".action_").append(fieldName).append(") == 'undefined'){\n");
            sbHtml.append("\t    addField (document.getElementById('").append(formName).append("'), 'hidden', 'action_").append(fieldName).append("', '").append(fieldValue).append("', 'action_").append(fieldName).append("');\n");
            sbHtml.append("\t}\n");
            sbHtml.append("\telse{\n");
            sbHtml.append("\t    document.").append(formName).append(".action_").append(fieldName).append(".value = '").append(fieldValue).append("';\n");
            sbHtml.append("\t}\n\n");
        }
        sbHtml.append("\n</script>\n");
        return sbHtml.toString();
    }

    public static ArrayList getActionButtonFieldsToExcludeFromRequestParams() {
        ArrayList<String> alExcludeFields = new ArrayList<String>();
        alExcludeFields.add("sdcid");
        alExcludeFields.add("keyid1");
        alExcludeFields.add("keyid2");
        alExcludeFields.add("keyid3");
        alExcludeFields.add("currentuser");
        alExcludeFields.add("paramid");
        alExcludeFields.add("paramtype");
        alExcludeFields.add("replicateid");
        alExcludeFields.add("defaultview");
        alExcludeFields.add("showdataentryviews");
        alExcludeFields.add("detailkeyid1");
        alExcludeFields.add("detailkeyid2");
        alExcludeFields.add("detailkeyid3");
        alExcludeFields.add("detailkeyid4");
        alExcludeFields.add("listpage");
        alExcludeFields.add("returntolistpage");
        alExcludeFields.add("gotopage");
        alExcludeFields.add("sortby");
        alExcludeFields.add("popup");
        alExcludeFields.add("groupbyindex");
        alExcludeFields.add("groupbycolumn");
        alExcludeFields.add("wizardcompletetarget");
        alExcludeFields.add("wizardcanceltarget");
        alExcludeFields.add("wizardcomplete");
        alExcludeFields.add("wizardcancel");
        alExcludeFields.add("queryid");
        alExcludeFields.add("queryfrom");
        alExcludeFields.add("querywhere");
        alExcludeFields.add("param1");
        alExcludeFields.add("param2");
        alExcludeFields.add("param3");
        alExcludeFields.add("param4");
        alExcludeFields.add("param5");
        alExcludeFields.add("param6");
        alExcludeFields.add("param7");
        alExcludeFields.add("param8");
        alExcludeFields.add("param9");
        alExcludeFields.add("param10");
        alExcludeFields.add("param11");
        alExcludeFields.add("param12");
        alExcludeFields.add("filtersdcid");
        alExcludeFields.add("filterkeyid1list");
        alExcludeFields.add("mode");
        alExcludeFields.add("approvalstatus");
        alExcludeFields.add("maintstyle");
        alExcludeFields.add("command");
        alExcludeFields.add("propertytreeid");
        alExcludeFields.add("propertytreetype");
        alExcludeFields.add("webpageid");
        alExcludeFields.add("page");
        alExcludeFields.add("objectname");
        alExcludeFields.add("elementid");
        alExcludeFields.add("nodeid");
        alExcludeFields.add("title");
        alExcludeFields.add("searchbardock");
        alExcludeFields.add("searchbarenabled");
        alExcludeFields.add("noOfButtonsInOneLine");
        alExcludeFields.add("showsearchbar");
        alExcludeFields.add("allowgrouping");
        alExcludeFields.add("includetemplates");
        alExcludeFields.add("showgroupby");
        alExcludeFields.add("actionbuttonid");
        alExcludeFields.add("(poll)");
        alExcludeFields.add("(return)");
        return alExcludeFields;
    }

    private static void appendKey(StringBuffer keylist, String key) {
        if (keylist != null) {
            if (keylist.length() > 0) {
                if (key != null && key.trim().length() > 0) {
                    keylist.append(DELIMITER_ROW).append(key);
                }
            } else if (key != null && key.trim().length() > 0) {
                keylist.append(key);
            }
        }
    }

    public static void concatenateKeys(PageContext pageContext) {
        PropertyList pagedata = (PropertyList)pageContext.getAttribute("pagedata", 2);
        PropertyList requestContext = (PropertyList)JstlUtil.evaluateExpression("${requestdata}", pageContext);
        Set keyset = pagedata.keySet();
        StringBuffer key1 = new StringBuffer();
        StringBuffer key2 = new StringBuffer();
        StringBuffer key3 = new StringBuffer();
        String keyid1 = pagedata.getProperty("keyid1");
        String keyid2 = pagedata.getProperty("keyid2");
        String keyid3 = pagedata.getProperty("keyid3");
        String newkeyid1 = pagedata.getProperty("newkeyid1");
        String newkeyid2 = pagedata.getProperty("newkeyid3");
        String newkeyid3 = pagedata.getProperty("newkeyid3");
        String editkeyid1 = pagedata.getProperty("editkeyid1");
        String editkeyid2 = pagedata.getProperty("editkeyid2");
        String editkeyid3 = pagedata.getProperty("editkeyid3");
        OpalUtil.appendKey(key1, keyid1);
        OpalUtil.appendKey(key2, keyid2);
        OpalUtil.appendKey(key3, keyid3);
        OpalUtil.appendKey(key1, newkeyid1);
        OpalUtil.appendKey(key2, newkeyid2);
        OpalUtil.appendKey(key3, newkeyid3);
        OpalUtil.appendKey(key1, editkeyid1);
        OpalUtil.appendKey(key2, editkeyid2);
        OpalUtil.appendKey(key3, editkeyid3);
        pagedata.setProperty("keyid1", key1.toString());
        pagedata.setProperty("keyid2", key2.toString());
        pagedata.setProperty("keyid3", key3.toString());
        OpalUtil.setKeyProperty("action_keyid1", key1.toString(), pagedata, keyset);
        OpalUtil.setKeyProperty("action_keyid2", key2.toString(), pagedata, keyset);
        OpalUtil.setKeyProperty("action_keyid3", key3.toString(), pagedata, keyset);
        OpalUtil.setKeyProperty("action_newkeyid1", newkeyid1, pagedata, keyset);
        OpalUtil.setKeyProperty("action_newkeyid2", newkeyid2, pagedata, keyset);
        OpalUtil.setKeyProperty("action_newkeyid3", newkeyid3, pagedata, keyset);
        OpalUtil.setKeyProperty("action_editkeyid1", editkeyid1, pagedata, keyset);
        OpalUtil.setKeyProperty("action_editkeyid2", editkeyid2, pagedata, keyset);
        OpalUtil.setKeyProperty("action_editkeyid3", editkeyid3, pagedata, keyset);
        OpalUtil.copyKeyValuestoRequestContext(pagedata, requestContext);
    }

    private static void setKeyProperty(String key, String value, PropertyList pagedata, Set keyset) {
        if (key == null || pagedata == null || value == null || key.trim().length() == 0 || value.trim().length() == 0) {
            return;
        }
        String currentValue = pagedata.getProperty(key);
        if (!keyset.contains(key) || currentValue == null || currentValue.trim().length() == 0) {
            pagedata.setProperty(key, value);
        }
    }

    private static void copyKeyValuestoRequestContext(PropertyList pagedata, PropertyList requestContext) {
        if (pagedata == null || requestContext == null) {
            return;
        }
        requestContext.setProperty("keyid1", pagedata.getProperty("keyid1"));
        requestContext.setProperty("keyid2", pagedata.getProperty("keyid2"));
        requestContext.setProperty("keyid3", pagedata.getProperty("keyid3"));
        requestContext.setProperty("action_keyid1", pagedata.getProperty("action_keyid1"));
        requestContext.setProperty("action_keyid2", pagedata.getProperty("action_keyid2"));
        requestContext.setProperty("action_keyid3", pagedata.getProperty("action_keyid3"));
        requestContext.setProperty("newkeyid1", pagedata.getProperty("newkeyid1"));
        requestContext.setProperty("newkeyid2", pagedata.getProperty("newkeyid2"));
        requestContext.setProperty("newkeyid3", pagedata.getProperty("newkeyid3"));
        requestContext.setProperty("editkeyid1", pagedata.getProperty("editkeyid1"));
        requestContext.setProperty("editkeyid2", pagedata.getProperty("editkeyid2"));
        requestContext.setProperty("editkeyid3", pagedata.getProperty("editkeyid3"));
    }

    public static String convertToBuffer(int number, String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < number; ++i) {
            sb.append(i).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public static List getUniqueValueList(String key, String delimiter) {
        ArrayList<String> list = new ArrayList<String>();
        String[] keyarray = key.split(delimiter);
        for (int i = 0; i < keyarray.length; ++i) {
            String value = keyarray[i];
            if (list.contains(value)) continue;
            list.add(value);
        }
        return list;
    }

    public static String getUniqueValues(String str, String delimiter) {
        List list = OpalUtil.getUniqueValueList(str, delimiter);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); ++i) {
            sb.append((String)list.get(i)).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public static List getDataSetList(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String delimiter, QueryProcessor queryProcessor) {
        if (sdcid == null || keyid1 == null || paramlistid == null || paramlistversionid == null || variantid == null || dataset == null) {
            Logger.logWarn("[OpalUtil.getDataSetList] One of the input is null.");
            return null;
        }
        if (delimiter == null) {
            delimiter = DELIMITER_ROW;
        }
        String[] a1 = StringUtil.split(keyid1, delimiter);
        String[] a4 = StringUtil.split(paramlistid, delimiter);
        String[] a5 = StringUtil.split(paramlistversionid, delimiter);
        String[] a6 = StringUtil.split(variantid, delimiter);
        String[] a7 = StringUtil.split(dataset, delimiter);
        if (a1.length != a4.length || a4.length != a5.length || a5.length != a6.length || a6.length != a7.length) {
            Logger.logWarn("[OpalUtil.getDataSetList] Inputs are not in same number.");
            return null;
        }
        ArrayList<SDIDataSet> list = new ArrayList<SDIDataSet>();
        for (int i = 0; i < a1.length; ++i) {
            SDI sdi = new SDI(sdcid, a1[i], null, null);
            SDIDataSet ds = new SDIDataSet(sdi, a4[i], a5[i], a6[i], a7[i], queryProcessor);
            list.add(ds);
        }
        return list;
    }

    public static List getPLDataSetList(String paramlistid, String paramlistversionid, String variantid, String dataset) throws SapphireException {
        ArrayList<PLDataSet> plDatasetList = new ArrayList<PLDataSet>();
        if (paramlistid == null || paramlistversionid == null || variantid == null || dataset == null) {
            throw new SapphireException("Invalid input(s). One or more of the input(s) is/are null.");
        }
        String[] s1 = StringUtil.split(paramlistid, DELIMITER_ROW);
        String[] s2 = StringUtil.split(paramlistversionid, DELIMITER_ROW);
        String[] s3 = StringUtil.split(variantid, DELIMITER_ROW);
        String[] s4 = StringUtil.split(dataset, DELIMITER_ROW);
        if (s1.length == s2.length && s2.length == s3.length && s3.length == s4.length) {
            for (int i = 0; i < s1.length; ++i) {
                plDatasetList.add(new PLDataSet(s1[i], s2[i], s3[i], s4[i]));
            }
        } else {
            throw new SapphireException("The input count is not same");
        }
        return plDatasetList;
    }

    public static List getSDIList(String sdcid, String keyid1, String keyid2, String keyid3, String delimiter) throws SapphireException {
        ArrayList<SDI> list = new ArrayList<SDI>();
        boolean skipKey2 = true;
        boolean skipKey3 = true;
        String[] key1 = null;
        String[] key2 = null;
        String[] key3 = null;
        sdcid = OpalUtil.getUniqueValues(sdcid, delimiter);
        if (!OpalUtil.isEmpty(keyid1)) {
            keyid1 = OpalUtil.getUniqueValues(keyid1, delimiter);
            key1 = StringUtil.split(keyid1, delimiter);
        }
        if (!OpalUtil.isEmpty(keyid2)) {
            keyid2 = OpalUtil.getUniqueValues(keyid2, delimiter);
            key2 = StringUtil.split(keyid2, delimiter);
            skipKey2 = false;
        }
        if (!OpalUtil.isEmpty(keyid3)) {
            keyid3 = OpalUtil.getUniqueValues(keyid3, delimiter);
            key3 = StringUtil.split(keyid3, delimiter);
            skipKey3 = false;
        }
        for (int i = 0; i < key1.length; ++i) {
            SDI sdi;
            if (skipKey3) {
                sdi = skipKey2 ? new SDI(sdcid, key1[i], null, null) : new SDI(sdcid, key1[i], key2[i], null);
            } else {
                if (skipKey2) {
                    throw new SapphireException("Illegal SDI key (keyid2 cannot be blank)");
                }
                sdi = new SDI(sdcid, key1[i], key2[i], key3[i]);
            }
            if (!sdi.isValid()) {
                throw new SapphireException("Illegal SDI key: " + sdi);
            }
            list.add(sdi);
        }
        return list;
    }

    public boolean exists(String tableid, String[] column, String[] value, QueryProcessor queryProcessor) {
        return false;
    }

    public static String parseDisplayValue(String value, String displayValue) {
        String replacedValue = value;
        String token2 = null;
        if (displayValue != null && value != null && displayValue.trim().length() > 0 && value.trim().length() > 0) {
            String[] tokens = StringUtil.split(displayValue, DELIMITER_ROW);
            for (int count = 0; count < tokens.length; ++count) {
                String token = tokens[count];
                int equalIndex = token.indexOf(61);
                if (equalIndex <= 0) continue;
                String token1 = token.substring(0, equalIndex);
                if (token.length() > equalIndex) {
                    token2 = token.substring(equalIndex + 1);
                }
                if (!token1.equals(value)) continue;
                replacedValue = token2;
            }
        }
        return replacedValue;
    }

    public static boolean isUniqueFromAnalyst(PageContext pageContext, HashMap hmProps) {
        boolean retFlag = false;
        DataSet displayedDataItems = (DataSet)pageContext.getAttribute("displayedDataItems");
        if (displayedDataItems != null) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.putAll(hmProps);
            String currentUser = (String)pageContext.getAttribute("currentuser");
            props.put("s_analystid", currentUser);
            DataSet filteredDataItems = displayedDataItems.getFilteredDataSet(props);
            if (filteredDataItems != null && filteredDataItems.getRowCount() == 0) {
                retFlag = true;
            }
        }
        return retFlag;
    }

    public static boolean hasApprovedAnyAprSteps(PageContext pageContext, HashMap hmProps, String userid) {
        boolean retFlag = false;
        DataSet dataApprovals = (DataSet)pageContext.getAttribute("displayedApprovals");
        if (dataApprovals != null) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.putAll(hmProps);
            props.put("modby", userid);
            DataSet filteredDataApprovals = dataApprovals.getFilteredDataSet(props);
            if (filteredDataApprovals != null) {
                for (int count = 0; count < filteredDataApprovals.getRowCount(); ++count) {
                    String currentAprValue = filteredDataApprovals.getValue(count, "approvalflag");
                    if (currentAprValue == null || currentAprValue.equalsIgnoreCase("U")) continue;
                    retFlag = true;
                }
            }
        }
        return retFlag;
    }

    public static void setKeyFromRSetItems(PageContext pageContext, SDIRequest sdiRequest) throws Exception {
        PropertyList pagedata = (PropertyList)pageContext.getAttribute("pagedata", 2);
        String keyid1 = pagedata.getProperty("keyid1");
        if (keyid1 == null || keyid1.trim().length() == 0) {
            OpalRequestInfo opalRequestInfo = new OpalRequestInfo();
            String rsetid = sdiRequest.getRsetid();
            if (rsetid != null && rsetid.trim().length() > 0) {
                opalRequestInfo.populateKeysFromRSetItems(pageContext, rsetid);
            } else {
                opalRequestInfo.populateKeysFromSDIRequest(sdiRequest);
            }
            keyid1 = opalRequestInfo.getKeyid1();
            pagedata.setProperty("keyid1", keyid1);
            pagedata.setProperty("keyid2", opalRequestInfo.getKeyId2());
            pagedata.setProperty("keyid3", opalRequestInfo.getKeyId3());
        }
    }

    public static List<String> toList(String str, String delimiter) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(StringUtil.split(str, delimiter)));
        return list;
    }

    public static List<String> toUniqueList(String str, String delimiter) {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(StringUtil.split(str, delimiter)));
        return new ArrayList<String>(set);
    }

    public static String toUniqueString(String str, String delimiter) {
        LinkedHashSet set = new LinkedHashSet();
        Collections.addAll(set, StringUtil.split(str, delimiter));
        return OpalUtil.toDelimitedString(set, delimiter);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(DataSet ds) {
        return !OpalUtil.isEmpty(ds);
    }

    public static boolean isEmpty(DataSet ds) {
        return ds == null || ds.size() == 0;
    }

    public static boolean isNotEmpty(Collection collection) {
        return !OpalUtil.isEmpty(collection);
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !OpalUtil.isEmpty(s);
    }

    public static boolean isSapphireNull(String value) {
        return value == null || value.trim().length() == 0 || value.equals("(null)");
    }

    public static String toDelimitedString(Collection collection, String delimiter) {
        if (OpalUtil.isEmpty(collection)) {
            return "";
        }
        Iterator iterator = collection.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append(iterator.next());
        while (iterator.hasNext()) {
            sb.append(delimiter).append(iterator.next());
        }
        return sb.toString();
    }

    public static String toDelimitedString(String text, int repeatCount, String delimiter) {
        if (delimiter == null) {
            delimiter = "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeatCount; ++i) {
            sb.append(text).append(delimiter);
        }
        if (sb.length() > delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public static String toLogString(Object o) {
        StringBuffer sb;
        block3: {
            block2: {
                sb = new StringBuffer();
                sb.append("[").append(o.getClass().getName()).append("]");
                if (!(o instanceof Map)) break block2;
                Map map = (Map)o;
                Set keySet = map.keySet();
                for (String key : keySet) {
                    sb.append("\n").append(key).append("=").append(map.get(key));
                }
                break block3;
            }
            if (!(o instanceof List)) break block3;
            List list = (List)o;
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                sb.append("\n").append(iterator.next());
            }
        }
        return sb.toString();
    }

    public static String map2String(Map map) {
        return OpalUtil.map2String(map, DELIMITER_ROW, DELIMITER_COLUMN);
    }

    public static Map string2Map(String str) {
        return OpalUtil.string2Map(str, DELIMITER_ROW, DELIMITER_COLUMN);
    }

    public static String map2String(Map map, String rowDelimiter, String columnDelimiter) {
        StringBuffer sb = new StringBuffer();
        Set keySet = map.keySet();
        for (String key : keySet) {
            sb.append(key).append(columnDelimiter).append(map.get(key)).append(rowDelimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - rowDelimiter.length());
        }
        return sb.toString();
    }

    public static Map string2Map(String str, String rowDelimiter, String columnDelimiter) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (str != null && str.length() > 0) {
            String[] parts;
            for (String part : parts = StringUtil.split(str, rowDelimiter)) {
                try {
                    int index = part.indexOf(columnDelimiter);
                    String key = part.substring(0, index);
                    String value = part.substring(index + columnDelimiter.length());
                    map.put(key, value);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return map;
    }

    public static HashMap getTableColumnMap(QueryProcessor qp, String tableid) {
        HashMap<String, String> tableColumnsMap = new HashMap<String, String>();
        String sql = "SELECT COLUMNID, DATATYPE FROM SYSCOLUMN WHERE TABLEID = ?";
        DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{tableid});
        for (int i = 0; i < ds.size(); ++i) {
            tableColumnsMap.put(ds.getValue(i, "COLUMNID"), ds.getValue(i, "DATATYPE"));
        }
        return tableColumnsMap;
    }

    public static HashMap parseExtraProps(String extraProps) {
        HashMap<String, String> props = new HashMap<String, String>();
        if (extraProps != null && extraProps.length() > 0) {
            String[] s;
            for (String _s : s = StringUtil.split(extraProps, DELIMITER_ROW)) {
                String[] __s;
                if (_s == null || _s.length() <= 0 || (__s = StringUtil.split(_s, "="))[0] == null || __s[0].length() <= 0) continue;
                if (__s.length == 2 && __s[1] != null && __s[1].length() > 0) {
                    props.put(__s[0], __s[1]);
                    continue;
                }
                props.put(__s[0], "");
            }
        }
        return props;
    }

    public static String replaceAllTokens(String input, String[] tokens, HashMap values, boolean ignoreBlank) {
        for (String token : tokens) {
            if (values.get(token) == null) continue;
            if (ignoreBlank) {
                if (((String)values.get(token)).equalsIgnoreCase("")) continue;
                input = StringUtil.replaceAll(input, "[" + token + "]", (String)values.get(token));
                continue;
            }
            input = StringUtil.replaceAll(input, "[" + token + "]", (String)values.get(token));
        }
        return input;
    }

    public static Map toMap(String[] keyvaluepair) {
        HashMap<String, String> props = new HashMap<String, String>();
        if (keyvaluepair != null) {
            for (int i = 0; i < keyvaluepair.length; ++i) {
                String[] keyvalueArr;
                String keyvalue = keyvaluepair[i];
                if (keyvalue == null || keyvalue.length() <= 0 || (keyvalueArr = StringUtil.split(keyvalue, "="))[0] == null || keyvalueArr[0].length() <= 0) continue;
                if (keyvalueArr[1] != null && keyvalueArr[1].length() > 0) {
                    props.put(keyvalueArr[0], keyvalueArr[1]);
                    continue;
                }
                props.put(keyvalueArr[0], "");
            }
        }
        return props;
    }

    public static HashMap getTabText(PageContext pageContext, String sdcid) {
        HashMap<String, String> props = new HashMap<String, String>();
        String singularSDIText = "";
        String pluralSDIText = "";
        if (sdcid != null && sdcid.trim().length() > 0) {
            TranslationProcessor tp = new TranslationProcessor(pageContext);
            tp.setTextType(sdcid);
            SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
            HashMap sdcProps = sdcProcessor.getSDCProperties(sdcid);
            singularSDIText = (String)sdcProps.get("singular");
            pluralSDIText = (String)sdcProps.get("plural");
            if (singularSDIText == null || singularSDIText.trim().length() == 0) {
                singularSDIText = sdcid;
            }
            if (pluralSDIText == null || pluralSDIText.trim().length() == 0) {
                pluralSDIText = sdcid + "(s)";
            }
            singularSDIText = tp.translate(singularSDIText);
            pluralSDIText = tp.translate(pluralSDIText);
            singularSDIText = OpalUtil.capitalizeFirstLetter(singularSDIText);
            pluralSDIText = OpalUtil.capitalizeFirstLetter(pluralSDIText);
        }
        props.put("singular", singularSDIText);
        props.put("plural", pluralSDIText);
        return props;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str != null && str.trim().length() > 0) {
            str = str.trim();
            String firstLetter = str.substring(0, 1);
            String capitalized = firstLetter.toUpperCase();
            if (str.length() > 1) {
                capitalized = capitalized + str.substring(1);
            }
            str = capitalized;
        }
        return str;
    }

    public static String disableButtons(boolean hasLockedRows, String prefix, ArrayList buttonIdList) {
        StringBuffer html = new StringBuffer();
        String tempPrefix = "";
        if (prefix != null && prefix.trim().length() > 0) {
            tempPrefix = prefix.trim();
        }
        if (hasLockedRows && buttonIdList != null && buttonIdList.size() > 0) {
            html.append("<script text='text/javascript' langage='javascript' >");
            for (int buttonCount = 0; buttonCount < buttonIdList.size(); ++buttonCount) {
                String buttonId = (String)buttonIdList.get(buttonCount);
                if (buttonId == null || buttonId.trim().length() <= 0) continue;
                html.append("if ( typeof( ");
                if (tempPrefix.length() > 0) {
                    html.append(tempPrefix).append(".");
                }
                html.append(buttonId).append(" ) != 'undefined' ) { ");
                if (tempPrefix.length() > 0) {
                    html.append(tempPrefix).append(".");
                }
                html.append(buttonId).append(".disabled = true; ");
                html.append("} ");
            }
            html.append("</script>");
        }
        return html.toString();
    }

    public static String getMFDisableButtonsScript(boolean hasLockedRows, String prefix, int rowCount) {
        String html = "";
        if (rowCount > 1) {
            return html;
        }
        ArrayList<String> buttonIdList = new ArrayList<String>();
        buttonIdList.add("Save");
        html = OpalUtil.disableButtons(hasLockedRows, prefix, buttonIdList);
        return html;
    }

    public static String getNextURLForm(PageContext pageContext, String formname) {
        StringBuffer form = new StringBuffer();
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
        ServletRequest request = pageContext.getRequest();
        Enumeration paramNames = request.getParameterNames();
        if (formname == null || formname.trim().length() == 0) {
            formname = "nextURLForm";
        }
        form.append("<form id='nextURLForm' name='").append(formname).append("' target='_top' action='");
        form.append(pagedata.getProperty("nexturl"));
        form.append("' method='post'>");
        while (paramNames.hasMoreElements()) {
            String key = (String)paramNames.nextElement();
            if (key.equals("_iframename") || key.equals("nexturl") || key.equals("objectname") || key.startsWith("__") || key.equalsIgnoreCase("mode") && request.getParameter(key).equalsIgnoreCase("add")) continue;
            form.append("<input type=hidden name='").append(key).append("' value=\"").append(request.getParameter(key)).append("\" id='").append(key).append("' />\n");
        }
        form.append("</form>");
        form.append("<script>");
        form.append("var __ouform = document.getElementById('").append(formname).append("');");
        form.append("var __outn = sapphire.navigator.util.getTopName();");
        form.append("if ( __outn=='pageframe' ) { __ouform.action +='&layout=navigator' + '&indexofsetdisplay=").append(HttpUtil.encodeURIComponent(pagedata.getProperty("action_indexofsetdisplay"))).append("'; __ouform.target=__outn; }");
        form.append("sapphire.page.submit(__ouform);");
        form.append("</script>");
        return form.toString();
    }

    public static String getColumnValue(QueryProcessor qp, String tableid, String columnid, String whereclause, String[] bindVariables) {
        DataSet ds = qp.getPreparedSqlDataSet("select " + columnid + " from " + tableid + " where " + whereclause, (Object[])bindVariables);
        return ds != null && ds.size() > 0 ? ds.getValue(0, columnid, "") : "";
    }

    public static String getColumnValues(QueryProcessor qp, String tableid, String columnid, String whereclause, String[] bindVariables) {
        String value = "";
        DataSet ds = qp.getPreparedSqlDataSet("select " + columnid + " from " + tableid + " where " + whereclause, (Object[])bindVariables);
        if (ds != null && ds.size() > 0) {
            value = ds.getColumnValues(columnid, DELIMITER_ROW);
        }
        return value == null ? "" : value;
    }

    public static Map<String, String> getMultiColumnValue(QueryProcessor qp, String tableid, String columnids, String whereclause, String[] bindVariables) {
        HashMap<String, String> map = new HashMap<String, String>();
        DataSet ds = qp.getPreparedSqlDataSet("select " + columnids + " from " + tableid + " where " + whereclause, (Object[])bindVariables);
        if (OpalUtil.isNotEmpty(ds)) {
            for (int col = 0; col < ds.getColumnCount(); ++col) {
                String columnid = ds.getColumnId(col);
                map.put(columnid, ds.getColumnValues(columnid, DELIMITER_ROW));
            }
        }
        return map;
    }

    public static String getColumnValue(QueryProcessor qp, String tableid, String columnid, String whereclause) {
        String value = "";
        DataSet ds = qp.getSqlDataSet("select " + columnid + " from " + tableid + " where " + whereclause);
        if (ds != null && ds.size() > 0) {
            value = ds.getValue(0, columnid);
        }
        return value == null ? "" : value;
    }

    public static String getColumnValues(QueryProcessor qp, String tableid, String columnid, String whereclause) {
        String value = "";
        DataSet ds = qp.getSqlDataSet("select " + columnid + " from " + tableid + " where " + whereclause);
        if (ds != null && ds.size() > 0) {
            value = ds.getColumnValues(columnid, DELIMITER_ROW);
        }
        return value == null ? "" : value;
    }

    public static String getSDCName(String sdcid) {
        if (sdcid != null) {
            if (sdcid.startsWith("LV_")) {
                return sdcid.substring(3);
            }
            if (sdcid.endsWith("SDC")) {
                return sdcid.substring(0, sdcid.length() - 3);
            }
        }
        return sdcid;
    }

    public static PropertyList getSpecInterpretationMap(ConfigurationProcessor cfg) throws SapphireException {
        PropertyList specIntpMap = new PropertyList();
        PropertyListCollection specInterpretation = cfg.getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
        if (specInterpretation != null) {
            for (PropertyList pl : specInterpretation) {
                if (pl == null) continue;
                String specCond = pl.getProperty("SpecCond");
                String interpretedValue = pl.getProperty("interpretation");
                specIntpMap.setProperty(specCond, interpretedValue);
            }
        }
        return specIntpMap;
    }

    public static String getSpecCondition(PropertyList specInterpretationMap, String specInterpretation) {
        String specCond = "";
        Set s = specInterpretationMap.keySet();
        for (Object value : s) {
            specCond = (String)value;
            if (!specInterpretationMap.getProperty(specCond).equalsIgnoreCase(specInterpretation)) continue;
            return specCond;
        }
        return specCond;
    }

    public static String getSDILockedBy(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        return OpalUtil.getSDILockedBy(queryProcessor, sdcid, keyid1, null, null);
    }

    public static String getSDILockedBy(QueryProcessor queryProcessor, String sdcid, String keyid1, String keyid2, String keyid3) {
        String connid;
        String connectionid = SecurityService.decryptConnectionId(queryProcessor.getConnectionid());
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select rset.connectionid");
        sql.append(" from rset, rsetitems r");
        sql.append(" where r.sdcid = ").append(safeSQL.addVar(sdcid)).append("");
        sql.append(" and r.keyid1 = ").append(safeSQL.addVar(keyid1)).append("");
        if (StringUtil.getLen(keyid2) > 0L) {
            sql.append(" and r.keyid2 = ").append(safeSQL.addVar(keyid2)).append("");
        }
        if (StringUtil.getLen(keyid3) > 0L) {
            sql.append(" and r.keyid3 = ").append(safeSQL.addVar(keyid3)).append("");
        }
        sql.append(" and r.lockstate = 2");
        sql.append(" and r.rsetid = rset.rsetid");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0 && !connectionid.equals(connid = SecurityService.decryptConnectionId(ds.getValue(0, "connectionid", "")))) {
            if (StringUtil.getLen(connid) > 0L && connid.indexOf(DELIMITER_COLUMN) != -1) {
                ConnectionProcessor cp = new ConnectionProcessor(ds.getValue(0, "connectionid", ""));
                String name = cp.getSapphireConnection().getSysuserName();
                return StringUtil.getLen(name) > 0L ? name : cp.getSapphireConnection().getSysuserId();
            }
            return "other user";
        }
        return "";
    }

    public static String getSDILockedByUserIdWithName(QueryProcessor queryProcessor, String sdcid, String keyid1, String keyid2, String keyid3) {
        String connectionid = SecurityService.decryptConnectionId(queryProcessor.getConnectionid());
        if (connectionid.startsWith("#")) {
            connectionid = EncryptDecrypt.decrypt(connectionid.substring(1));
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select rset.connectionid");
        sql.append(" from rset, rsetitems r");
        sql.append(" where r.sdcid = '").append(sdcid).append("'");
        sql.append(" and r.keyid1 = '").append(keyid1).append("'");
        if (StringUtil.getLen(keyid2) > 0L) {
            sql.append(" and r.keyid2 = '").append(keyid2).append("'");
        }
        if (StringUtil.getLen(keyid3) > 0L) {
            sql.append(" and r.keyid3 = '").append(keyid3).append("'");
        }
        sql.append(" and r.lockstate = 2");
        sql.append(" and r.rsetid = rset.rsetid");
        DataSet ds = queryProcessor.getSqlDataSet(sql.toString());
        if (ds != null && ds.size() > 0) {
            String connid = SecurityService.decryptConnectionId(ds.getValue(0, "connectionid", ""));
            if (connid.startsWith("#")) {
                connid = EncryptDecrypt.decrypt(connid.substring(1));
            }
            if (!connectionid.equals(connid)) {
                if (StringUtil.getLen(connid) > 0L && connid.indexOf(DELIMITER_COLUMN) != -1) {
                    ConnectionProcessor cp = new ConnectionProcessor(ds.getValue(0, "connectionid", ""));
                    String userId = cp.getSapphireConnection().getSysuserId();
                    String userName = cp.getSapphireConnection().getSysuserName();
                    if (userName == null) {
                        userName = "";
                    }
                    return userId + DELIMITER_ROW + userName;
                }
                return "other user";
            }
        }
        return "";
    }

    public static List getUserDepartments(QueryProcessor qp, String userid) {
        ArrayList<String> userDeptList = new ArrayList<String>();
        DataSet ds = qp.getPreparedSqlDataSet(new StringBuffer().append("SELECT DEPARTMENTID FROM DEPARTMENTSYSUSER WHERE SYSUSERID = ").append("?").append("").toString(), new Object[]{userid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                userDeptList.add(ds.getValue(i, "DEPARTMENTID"));
            }
        }
        return userDeptList;
    }

    public static boolean isSDIEditable(QueryProcessor qp, String userid, String sdcid, String keyid1, boolean noCustodianAllowed) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select custodialuserid, custodialdepartmentid");
        sql.append(" from trackitem");
        sql.append(" where linksdcid = ").append("?").append("");
        sql.append(" and linkkeyid1 = ").append("?").append("");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), new Object[]{sdcid, keyid1});
        if (ds != null && ds.size() > 0) {
            List userDepList;
            String custodialuserid = ds.getValue(0, "custodialuserid");
            if (custodialuserid == null || custodialuserid.trim().length() == 0 ? !noCustodianAllowed && !userid.equals(custodialuserid) : !userid.equals(custodialuserid)) {
                throw new SapphireException("User is not the custodian of selected item (" + keyid1 + ")");
            }
            String custodialdepartmentid = ds.getValue(0, "custodialdepartmentid");
            if (custodialdepartmentid != null && custodialdepartmentid.trim().length() > 0 && !(userDepList = OpalUtil.getUserDepartments(qp, userid)).contains(custodialdepartmentid)) {
                throw new SapphireException("User is not a member of Selected item's (" + keyid1 + ") Custodial Domain");
            }
        }
        return true;
    }

    public static String reverse(String str, String delimiter) {
        if (str != null && str.length() > 0) {
            String[] temp = StringUtil.split(str, delimiter);
            StringBuffer sb = new StringBuffer();
            for (int i = temp.length - 1; i >= 0; --i) {
                sb.append(temp[i]).append(DELIMITER_ROW);
            }
            if (sb.length() > 0) {
                str = sb.substring(0, sb.length() - 1);
            }
        }
        return str;
    }

    public static DataSet getSQLDataSet(QueryProcessor queryProcessor, DAMProcessor damProcessor, String sdcid, String sql, String keyid1) throws SapphireException {
        DataSet ds;
        SafeSQL safeSQL = new SafeSQL();
        if (StringUtil.split(keyid1, DELIMITER_ROW).length > 750) {
            String rsetid = damProcessor.createRSet(sdcid, keyid1, null, null);
            sql = StringUtil.replaceAll(sql, "[]", "select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid));
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            damProcessor.clearRSet(rsetid);
        } else {
            sql = StringUtil.replaceAll(sql, "[]", safeSQL.addIn(keyid1, DELIMITER_ROW));
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        return ds;
    }

    public static DataSet getSQLDataSet(QueryProcessor queryProcessor, DAMProcessor damProcessor, String sdcid, String sql, Collection keyCollection) throws SapphireException {
        DataSet ds;
        SafeSQL safeSQL = new SafeSQL();
        if (keyCollection.size() > 1000) {
            String rsetid = damProcessor.createRSet(sdcid, OpalUtil.toDelimitedString(keyCollection, DELIMITER_ROW), null, null);
            sql = StringUtil.replaceAll(sql, "[]", "select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid));
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            damProcessor.clearRSet(rsetid);
        } else {
            sql = StringUtil.replaceAll(sql, "[]", safeSQL.addIn(keyCollection));
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        return ds;
    }

    public static boolean isDeptSecurityEnabled(SDCProcessor sdcProc, String sdcid) {
        try {
            return OpalUtil.isNotEmpty(sdcid) && "D".equals(sdcProc.getSDCProperties(sdcid).get("accesscontrolledflag"));
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isSDISecurityEnabled(SDCProcessor sdcProc, String sdcid) {
        try {
            return OpalUtil.isNotEmpty(sdcid) && "S".equals(sdcProc.getSDCProperties(sdcid).get("accesscontrolledflag"));
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void updateStatus(JSONArray jsonArray, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        try {
            DataSet dsUpdateDs = new DataSet();
            DataSet dsUpdateWI = new DataSet();
            DataSet dsUpdateQC = new DataSet();
            String sdcid = "";
            for (int row = 0; row < jsonArray.length(); ++row) {
                DataSet ds;
                String batchId;
                int r;
                JSONObject jsonObject = jsonArray.getJSONObject(row);
                sdcid = jsonObject.getString("sdcid");
                String keyid1 = jsonObject.getString("keyid1");
                String keyid2 = jsonObject.getString("keyid2");
                String keyid3 = jsonObject.getString("keyid3");
                String paramlistid = jsonObject.getString("paramlistid");
                String paramlistversionid = jsonObject.getString("paramlistversionid");
                String variantid = jsonObject.getString("variantid");
                String dataset = jsonObject.getString("dataset");
                String workitemid = jsonObject.getString("workitemid");
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("keyid1", keyid1);
                findMap.put("keyid2", keyid2);
                findMap.put("keyid3", keyid3);
                if (dsUpdateDs.findRow(findMap) < 0) {
                    r = dsUpdateDs.addRow();
                    dsUpdateDs.setString(r, "keyid1", keyid1);
                    dsUpdateDs.setString(r, "keyid2", keyid2);
                    dsUpdateDs.setString(r, "keyid3", keyid3);
                }
                if (workitemid != null && workitemid.length() > 0 && dsUpdateWI.findRow(findMap) < 0) {
                    r = dsUpdateWI.addRow();
                    dsUpdateWI.setString(r, "keyid1", keyid1);
                    dsUpdateWI.setString(r, "keyid2", keyid2);
                    dsUpdateWI.setString(r, "keyid3", keyid3);
                }
                if ((batchId = (ds = qp.getPreparedSqlDataSet("getqcbatchid", "select s_qcbatchid from sdidata where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and paramlistid = ? and paramlistversionid =? and variantid = ? and dataset = ?", new String[]{sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset})).getValue(0, "s_qcbatchid")).length() <= 0 || dsUpdateQC.findRow("s_qcbatchid", batchId) >= 0) continue;
                int r2 = dsUpdateQC.addRow();
                dsUpdateQC.setString(r2, "s_qcbatchid", batchId);
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", dsUpdateDs.getColumnValues("keyid1", DELIMITER_ROW));
            props.setProperty("keyid2", dsUpdateDs.getColumnValues("keyid2", DELIMITER_ROW));
            props.setProperty("keyid3", dsUpdateDs.getColumnValues("keyid3", DELIMITER_ROW));
            ap.processAction("UpdateDatasetStatus", "1", props);
            if (dsUpdateWI.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dsUpdateWI.getColumnValues("keyid1", DELIMITER_ROW));
                props.setProperty("keyid2", dsUpdateWI.getColumnValues("keyid2", DELIMITER_ROW));
                props.setProperty("keyid3", dsUpdateWI.getColumnValues("keyid3", DELIMITER_ROW));
                ap.processAction("SyncSDIWIStatus", "1", props);
            }
            props.clear();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", dsUpdateDs.getColumnValues("keyid1", DELIMITER_ROW));
            props.setProperty("keyid2", dsUpdateDs.getColumnValues("keyid2", DELIMITER_ROW));
            props.setProperty("keyid3", dsUpdateDs.getColumnValues("keyid3", DELIMITER_ROW));
            ap.processAction("SyncSDIDataSetStatus", "1", props);
            if (dsUpdateQC.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", "QCBatch");
                props.setProperty("keyid1", dsUpdateQC.getColumnValues("s_qcbatchid", DELIMITER_ROW));
                props.put("postdataentry", "Y");
                ap.processAction("UpdateQCBatchStatus", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static void updateAttributeSourceStatus(String sdcId, DataSet attributes, DBAccess db, ActionProcessor ap) throws SapphireException {
        String sql;
        String string = "SDIWorkItem".equalsIgnoreCase(sdcId) ? "select sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance from sdiworkitem where sdiworkitemid = ?" : (sql = "DataSet".equalsIgnoreCase(sdcId) ? "select sdcid, keyid1, keyid2, keyid3, s_qcbatchid from sdidata where sdidataid = ?" : "");
        if (sql.length() == 0) {
            return;
        }
        PreparedStatement getParentDetail = db.prepareStatement("getSourceDetail", sql);
        DataSet dsSource = new DataSet();
        DataSet dsUpdateQC = new DataSet();
        try {
            for (int i = 0; i < attributes.getRowCount(); ++i) {
                String id = attributes.getValue(i, "keyid1");
                getParentDetail.setString(1, id);
                DataSet ds = new DataSet(getParentDetail.executeQuery());
                if (ds.getRowCount() <= 0) continue;
                String sourceSdcId = ds.getValue(0, "sdcid");
                String keyid1 = ds.getValue(0, "keyid1");
                String keyid2 = ds.getValue(0, "keyid2");
                String keyid3 = ds.getValue(0, "keyid3");
                String qcbatchId = ds.getValue(0, "s_qcbatchid");
                if (qcbatchId.length() > 0 && dsUpdateQC.findRow("s_qcbatchid", qcbatchId) < 0) {
                    int r = dsUpdateQC.addRow();
                    dsUpdateQC.setString(r, "s_qcbatchid", qcbatchId);
                }
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("sdcid", sourceSdcId);
                find.put("keyid1", keyid1);
                find.put("keyid2", keyid2);
                find.put("keyid3", keyid3);
                if ("SDIWorkItem".equalsIgnoreCase(sdcId)) {
                    find.put("workitemid", ds.getValue(0, "workitemid"));
                    find.put("workiteminstance", ds.getValue(0, "workiteminstance"));
                }
                if (dsSource.findRow(find) >= 0) continue;
                int r = dsSource.addRow();
                dsSource.setString(r, "sdcid", sourceSdcId);
                dsSource.setString(r, "keyid1", keyid1);
                dsSource.setString(r, "keyid2", keyid2);
                dsSource.setString(r, "keyid3", keyid3);
                if (!"SDIWorkItem".equalsIgnoreCase(sdcId)) continue;
                dsSource.setString(r, "workitemid", ds.getValue(0, "workitemid"));
                dsSource.setString(r, "workiteminstance", ds.getValue(0, "workiteminstance"));
            }
            PropertyList props = new PropertyList();
            if (dsSource.getRowCount() > 0) {
                if ("DataSet".equalsIgnoreCase(sdcId)) {
                    props.setProperty("sdcid", dsSource.getValue(0, "sdcid"));
                    props.setProperty("keyid1", dsSource.getColumnValues("keyid1", DELIMITER_ROW));
                    props.setProperty("keyid2", dsSource.getColumnValues("keyid2", DELIMITER_ROW));
                    props.setProperty("keyid3", dsSource.getColumnValues("keyid3", DELIMITER_ROW));
                    ap.processAction("UpdateDatasetStatus", "1", props);
                }
                props.clear();
                props.setProperty("sdcid", dsSource.getValue(0, "sdcid"));
                props.setProperty("keyid1", dsSource.getColumnValues("keyid1", DELIMITER_ROW));
                props.setProperty("keyid2", dsSource.getColumnValues("keyid2", DELIMITER_ROW));
                props.setProperty("keyid3", dsSource.getColumnValues("keyid3", DELIMITER_ROW));
                props.setProperty("workitemid", dsSource.getColumnValues("workitemid", DELIMITER_ROW));
                props.setProperty("workiteminstance", dsSource.getColumnValues("workiteminstance", DELIMITER_ROW));
                ap.processAction("SyncSDIWIStatus", "1", props);
                if ("Sample".equals(dsSource.getValue(0, "sdcid"))) {
                    props.clear();
                    props.setProperty("sdcid", dsSource.getValue(0, "sdcid"));
                    props.setProperty("keyid1", dsSource.getColumnValues("keyid1", DELIMITER_ROW));
                    props.setProperty("keyid2", dsSource.getColumnValues("keyid2", DELIMITER_ROW));
                    props.setProperty("keyid3", dsSource.getColumnValues("keyid3", DELIMITER_ROW));
                    ap.processAction("SyncSDIDataSetStatus", "1", props);
                }
                if (dsUpdateQC.getRowCount() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "QCBatch");
                    props.setProperty("keyid1", dsUpdateQC.getColumnValues("s_qcbatchid", DELIMITER_ROW));
                    props.put("postdataentry", "Y");
                    ap.processAction("UpdateQCBatchStatus", "1", props);
                }
            }
        }
        catch (SQLException e) {
            throw new SapphireException(e);
        }
        finally {
            db.closeStatement("getSourceDetail");
        }
    }

    public static String getUniqueID() {
        return StringUtil.replaceAll(UUID.randomUUID().toString(), "-", "");
    }

    public static String getSiteIdFromUserDefaultDepartment(ConnectionInfo connectionInfo, DBAccess database) throws SapphireException {
        DataSet departmentdef;
        String userDefaultDepartmentId = connectionInfo.getDefaultDepartment();
        String siteId = "";
        if (userDefaultDepartmentId != null && userDefaultDepartmentId.length() > 0 && (departmentdef = WorkItemUtil.getDepartmentDefFromCache(database, connectionInfo, userDefaultDepartmentId)).getRowCount() > 0) {
            if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "sitedepartmentflag"))) {
                siteId = userDefaultDepartmentId;
            } else if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "testingflag")) && departmentdef.getValue(0, "parentdepartmentid").length() > 0) {
                siteId = departmentdef.getValue(0, "parentdepartmentid");
            }
        }
        return siteId;
    }

    public static String getSiteIdFromUserDefaultTestingLab(ConnectionInfo connectionInfo, DBAccess database) throws SapphireException {
        DataSet departmentdef;
        String userDefaultTestLab;
        String siteId = "";
        database.createPreparedResultSet("getuserdefaulttestlab", "select departmentid from departmentsysuser where sysuserid = ? and defaulttestinglabflag = 'Y'", new String[]{connectionInfo.getSysuserId()});
        DataSet dsUserDefaultTestingLab = new DataSet(database.getResultSet("getuserdefaulttestlab"));
        if (dsUserDefaultTestingLab.getRowCount() > 0 && (userDefaultTestLab = dsUserDefaultTestingLab.getValue(0, "departmentid")) != null && userDefaultTestLab.length() > 0 && (departmentdef = WorkItemUtil.getDepartmentDefFromCache(database, connectionInfo, userDefaultTestLab)).getRowCount() > 0) {
            if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "sitedepartmentflag"))) {
                siteId = userDefaultTestLab;
            } else if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "testingflag")) && departmentdef.getValue(0, "parentdepartmentid").length() > 0) {
                siteId = departmentdef.getValue(0, "parentdepartmentid");
            }
        }
        return siteId;
    }

    public static BigDecimal convertUnit(ConnectionInfo connectionInfo, BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        if (fromUnit != null && fromUnit.length() > 0 && toUnit != null && toUnit.length() > 0 && !fromUnit.equals(toUnit)) {
            return UnitsUtil.basicUnitConv(connectionInfo.getDatabaseId(), value, fromUnit, toUnit);
        }
        return value;
    }
}

