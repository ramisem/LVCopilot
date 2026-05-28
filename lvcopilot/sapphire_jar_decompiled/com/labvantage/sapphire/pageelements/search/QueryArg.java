/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.search;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QueryArg
extends BaseElement {
    private HashMap argMap;
    HashMap dependMap = null;
    private String lookupimg;
    private String lookupdateimg;
    private DateTimeUtil dtu;
    private TranslationProcessor translator = null;

    public QueryArg(PageContext pageContext, String connectionid, HashMap argMap) {
        this.pageContext = pageContext;
        this.argMap = argMap;
        this.setConnectionId(connectionid);
        this.lookupimg = this.element.getProperty("fieldlookupimg");
        if (this.lookupimg == null || this.lookupimg.length() == 0) {
            this.lookupimg = "WEB-CORE/elements/images/lookup.gif";
        }
        this.lookupdateimg = this.element.getProperty("datelookupimg");
        if (this.lookupdateimg == null || this.lookupdateimg.length() == 0) {
            this.lookupdateimg = "WEB-CORE/elements/images/lookup_date.gif";
        }
        this.dtu = new DateTimeUtil(HttpUtil.getConnectionInfo(pageContext));
        this.translator = new TranslationProcessor(pageContext);
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.argMap != null && this.argMap.size() > 0) {
            Set keyset = this.argMap.keySet();
            Iterator itr = keyset.iterator();
            StringBuffer queryidlist = new StringBuffer();
            while (itr.hasNext()) {
                String queryid = (String)itr.next();
                HashMap queryprops = (HashMap)this.argMap.get(queryid);
                html.append(this.getArgDivHtml(queryid, queryprops));
                if (queryidlist.length() == 0) {
                    queryidlist.append("['" + queryid);
                    continue;
                }
                queryidlist.append("','" + queryid);
            }
            html.append("<script> var __queryidlist = " + queryidlist + "'];</script>\n");
        }
        return html.toString();
    }

    public String getArgDivHtml(String queryId, HashMap queryprops) {
        StringBuffer htmlhead = new StringBuffer();
        StringBuffer html = new StringBuffer();
        HashMap<String, String> tokenMap = null;
        boolean iscascaded = queryprops.get("cascadedargflag") != null && queryprops.get("cascadedargflag").equals("Y");
        String[] argvalues = null;
        HashSet dependOnCurrent = null;
        ArrayList arglist = (ArrayList)queryprops.get("arglist");
        if (arglist != null && arglist.size() > 0) {
            HashMap argProps;
            if (iscascaded) {
                String arginto;
                this.pageContext.setAttribute("cascade", (Object)"Y");
                ServletRequest request = this.pageContext.getRequest();
                String cascadequeryid = request.getParameter("cascadequeryid");
                tokenMap = new HashMap<String, String>();
                this.dependMap = new HashMap();
                int current = 0;
                if (request.getParameter("currentarg") != null) {
                    String currentarg = request.getParameter("currentarg");
                    current = Integer.parseInt(currentarg.substring(currentarg.length() - 1, currentarg.length()));
                }
                for (int k = 0; k < arglist.size(); ++k) {
                    HashSet<Integer> dependset = new HashSet<Integer>();
                    arginto = (String)((HashMap)arglist.get(k)).get("arginto");
                    for (int i = 0; i < arglist.size(); ++i) {
                        String argdata = (String)((HashMap)arglist.get(i)).get("argdata");
                        String[] tokens = StringUtil.getTokens(argdata);
                        for (int j = 0; j < tokens.length; ++j) {
                            if (!arginto.equals("[" + tokens[j] + "]")) continue;
                            dependset.add(new Integer(i + 1));
                        }
                    }
                    if (dependset.size() <= 0) continue;
                    this.dependMap.put(new Integer(k + 1), dependset);
                }
                if (request.getParameter("argvaluelist") != null && queryId.equals(cascadequeryid)) {
                    dependOnCurrent = this.getDependSet(new HashSet(), new Integer(current));
                    argvalues = StringUtil.split(request.getParameter("argvaluelist"), ";");
                    for (int i = 0; i < arglist.size(); ++i) {
                        argProps = (HashMap)arglist.get(i);
                        arginto = (String)argProps.get("arginto");
                        if (dependOnCurrent.contains(new Integer(i + 1)) || argvalues[i].equals("disabled")) continue;
                        tokenMap.put(arginto, argvalues[i]);
                    }
                }
            }
            boolean hasdisabledfield = false;
            for (int i = 0; i < arglist.size(); ++i) {
                int argNumber = i + 1;
                String id = queryId + "_arg" + argNumber;
                argProps = (HashMap)arglist.get(i);
                String argdesc = (String)argProps.get("argdesc");
                String argfromsdcid = (String)argProps.get("sdcid");
                String displayname = argdesc != null && argdesc.length() > 0 ? argdesc : (String)argProps.get("argid");
                String defaultvalue = (String)argProps.get("defaultvalue");
                boolean hasDependents = false;
                if (iscascaded) {
                    if (argvalues != null && argvalues.length == arglist.size() && !dependOnCurrent.contains(new Integer(argNumber))) {
                        defaultvalue = argvalues[argNumber - 1];
                    }
                    hasDependents = this.hasDepend(argNumber);
                }
                html.append("<tr><td class=\"search_queryargtitle\" align=\"left\">" + displayname + "</td></tr>");
                html.append("<tr><td class=\"search_queryargfield\">");
                String argType = (String)argProps.get("argtype");
                if (argType == null) {
                    argType = "";
                }
                if (argType.equals("sdc")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                    html.append("<a href=\"\" onClick=\"JavaScript:lookupfield( '" + id + "', '" + argfromsdcid + "', '', '', 'lookupsdc' );return false;\"><img src=\"" + this.lookupimg + "\" title=\"Lookup a SDC\" border=\"0\"></a>\n");
                } else if (argType.equals("ddsdc")) {
                    DataSet ds = (DataSet)this.pageContext.getAttribute(argfromsdcid + "_ddsdc");
                    if (ds == null) {
                        PropertyList sdc = (PropertyList)this.pageContext.getAttribute(argfromsdcid + "_props");
                        if (sdc == null) {
                            sdc = this.getSDCProcessor().getPropertyList(argfromsdcid);
                            this.pageContext.setAttribute(argfromsdcid + "_props", (Object)sdc);
                        }
                        if (sdc == null) {
                            return "Can not find sdc definition for sdc:" + argfromsdcid;
                        }
                        String tableid = sdc.getProperty("tableid");
                        String keycol1 = sdc.getProperty("keycolid1");
                        String desccol = sdc.getProperty("desccol");
                        String sql = "select " + keycol1 + ", " + desccol + " from " + tableid + " order by " + keycol1;
                        ds = this.getQueryProcessor().getSqlDataSet(sql);
                        this.pageContext.setAttribute(argfromsdcid + "_ddsdc", (Object)ds);
                    }
                    html.append(this.getSelectFromDataSet(id, ds, defaultvalue, hasDependents));
                } else if (argType.equals("reftype")) {
                    String reftypeid = (String)argProps.get("reftypeid");
                    DataSet ds = (DataSet)this.pageContext.getAttribute(reftypeid + "_reftype");
                    if (ds == null) {
                        ds = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
                        this.pageContext.setAttribute(reftypeid + "_reftype", (Object)ds);
                    }
                    html.append(this.getSelectFromDataSet(id, ds, defaultvalue, hasDependents));
                } else if (argType.equals("string")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                } else if (argType.equals("number")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                } else if (argType.equals("absreldt")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                    if (this.dtu != null && this.dtu.isDefaultDateFormatDefined()) {
                        html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("' );return false;\"><img src=\"" + this.lookupdateimg + "\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
                    } else {
                        html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("','','','','','S M' );return false;\"><img src=\"" + this.lookupdateimg + "\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
                    }
                } else if (argType.equals("dateonly")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                    if (this.dtu != null && this.dtu.isDefaultDateOnlyFormatDefined()) {
                        html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("','','','','','O' );return false;\"><img src=\"" + this.lookupdateimg + "\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
                    } else {
                        html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("','','','','','OM' );return false;\"><img src=\"" + this.lookupdateimg + "\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
                    }
                } else if (argType.equals("sql")) {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                } else if (argType.equals("ddsql")) {
                    String sql = (String)argProps.get("argdata");
                    if (iscascaded && tokenMap != null && tokenMap.size() > 0 && sql.indexOf("[") > 0) {
                        Set keyset = tokenMap.keySet();
                        for (String token : keyset) {
                            String value = (String)tokenMap.get(token);
                            sql = StringUtil.replaceAll(sql, token, value);
                        }
                    }
                    if (sql.indexOf("[") < 0) {
                        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
                        html.append(this.getSelectFromDataSet(id, ds, defaultvalue, hasDependents));
                    } else {
                        html.append(this.getDisabledSelect(id));
                        hasdisabledfield = true;
                    }
                } else if (argType.equals("ddlb")) {
                    html.append("<select id=\"" + id + "\" name=\"" + id + "\"");
                    if (hasDependents) {
                        html.append((hasDependents ? " hasdepend=\"Y\"" : "") + "  onchange=\"argvaluechanged( this )\"");
                    }
                    html.append(">");
                    String[] listitems = StringUtil.split((String)argProps.get("argdata"), ";");
                    for (int j = 0; j < listitems.length; ++j) {
                        html.append(this.getOptionRow(listitems[j], null, defaultvalue));
                    }
                    html.append("</select>");
                } else {
                    html.append(this.getDefault(id, defaultvalue, hasDependents));
                }
                html.append("</td></tr>");
            }
            String queryargmessage = ElementUtil.getText(this.element, "queryargmessage", "Please supply the following additional information and click ");
            String cascadeargmessage = ElementUtil.getText(this.element, "cascadeargmessage", "Please supply the following additional information:");
            String searchnow = ElementUtil.getText(this.element, "searchnow", "Search Now");
            String argdivheight = this.element.getPropertyList("querysearch") != null ? this.element.getPropertyList("querysearch").getProperty("argdivheight") : "350";
            htmlhead.append("<div class=\"search_queryargdiv\" id=\"argsdiv_" + queryId + "\" style=\"display:none;overflow:auto;width:170px;height:" + (argdivheight.length() > 0 ? argdivheight : "350") + "px\">\n");
            htmlhead.append(queryprops.get("querydesc") == null ? "" : (String)queryprops.get("querydesc") + "<br/><br/>");
            if (hasdisabledfield) {
                htmlhead.append(cascadeargmessage);
            } else {
                htmlhead.append("" + queryargmessage + "  <a href=\"javascript:submitQueryArgs();\">" + searchnow + "</a>\n");
            }
            htmlhead.append("<br/><br/>\n");
            htmlhead.append("<table class=\"search_queryargtable\" border=\"0\" cellspacing=\"0\" cellborder=\"0\">\n");
            htmlhead.append(html.toString());
            htmlhead.append("</table>\n");
            if (!hasdisabledfield) {
                htmlhead.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:submitQueryArgs();\">" + searchnow + "</a>\n");
            }
            htmlhead.append("</div>\n");
        }
        return htmlhead.toString();
    }

    private String getDefault(String id, String value, boolean hasdepend) {
        StringBuffer html = new StringBuffer();
        html.append("<input class=\"search_queryarginput\" type=\"text\" size=\"18\" id=\"" + id + "\" name=\"" + id + "\"");
        html.append(value != null && value.length() > 0 ? " value=\"" + value + "\" " : "");
        if (hasdepend) {
            html.append((hasdepend ? " hasdepend=\"Y\"" : "") + " onkeydown=\"if ( event.keyCode == 13 ) javascript:argvaluechanged( this );\" onchange=\"argvaluechanged( this )\"");
        }
        html.append(">");
        return html.toString();
    }

    private String getSelectFromDataSet(String id, DataSet ds, String defaultvalue, boolean hasdepend) {
        StringBuffer html = new StringBuffer();
        html.append("<select class=\"search_queryargselect\" id=\"" + id + "\" name=\"" + id + "\"");
        if (hasdepend) {
            html.append((hasdepend ? " hasdepend=\"Y\"" : "") + " onchange=\"argvaluechanged( this )\"");
        }
        html.append(">");
        if (ds != null) {
            int rows = ds.getRowCount();
            html.append("<option>&nbsp;</option>");
            for (int i = 0; i < rows; ++i) {
                html.append(this.getOptionRow(ds.getValue(i, ds.getColumnId(0)), ds.getValue(i, "refdisplayvalue"), defaultvalue));
            }
        }
        html.append("</select>");
        return html.toString();
    }

    private String getDisabledSelect(String id) {
        return "<select class=\"search_queryargselect\" id=\"" + id + "\" name=\"" + id + "\" disabled>&nbsp;</select>";
    }

    private String getOptionRow(String value, String displayvalue, String defaultvalue) {
        StringBuffer html = new StringBuffer();
        String display = value;
        if (displayvalue != null && displayvalue.length() > 0) {
            display = displayvalue;
        }
        if (value != null && value.equals(defaultvalue)) {
            html.append("<option value=\"" + value + "\" selected>" + display + "</option>");
        } else {
            html.append("<option value=\"" + value + "\">" + display + "</option>");
        }
        return html.toString();
    }

    private HashSet getDependSet(HashSet set, Integer argNumber) {
        HashSet nextlevelset;
        if (this.dependMap != null && (nextlevelset = (HashSet)this.dependMap.get(argNumber)) != null && nextlevelset.size() > 0) {
            set.addAll(nextlevelset);
            Iterator itr = nextlevelset.iterator();
            while (itr.hasNext()) {
                this.getDependSet(set, (Integer)itr.next());
            }
        }
        return set;
    }

    private boolean hasDepend(int argNumber) {
        if (this.dependMap == null) {
            return false;
        }
        HashSet set = (HashSet)this.dependMap.get(new Integer(argNumber));
        return set != null && set.size() > 0;
    }
}

