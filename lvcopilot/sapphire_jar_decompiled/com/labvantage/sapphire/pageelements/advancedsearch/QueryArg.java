/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.advancedsearch.SearchByQuery;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QueryArg
extends BaseAjaxRequest {
    private HashMap argMap;
    HashMap dependMap = null;
    private static final String lookupimg = "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg";
    private static final String lookupdateimg = "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg";
    private PageContext pageContext;
    private boolean isCascadeArgCall = false;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private DateTimeUtil dtu;
    private boolean isLastSearchType;
    private TranslationProcessor translator = null;
    public static final int MODE_QUERYARG = 1;
    public static final int MODE_PROMPT = 2;
    public static final String ESC_SINGLEQUOTE = "{esc_singlequote}";
    private static Cache sqlCache = new Cache("Query Arg SQL", 10000);
    private Browser browser = null;

    public QueryArg() {
    }

    public QueryArg(PageContext pageContext, boolean isLastSearchType, HashMap argMap) {
        this.isLastSearchType = isLastSearchType;
        this.pageContext = pageContext;
        this.browser = new Browser(pageContext);
        this.argMap = argMap;
        this.translator = new TranslationProcessor(pageContext);
        this.dtu = new DateTimeUtil(HttpUtil.getConnectionInfo(pageContext));
    }

    public static String getSQL(String sqlcode) {
        return (String)sqlCache.get(sqlcode);
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        this.isCascadeArgCall = true;
        if (this.browser == null) {
            this.browser = new Browser(request);
        }
        String sdcid = request.getParameter("sdcid");
        String queryId = request.getParameter("cascadequeryid");
        String cookiekey = request.getParameter("cookiekey");
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        String sysuser = connectionInfo.getSysuserId();
        this.translator = this.getTranslationProcessor();
        this.dtu = new DateTimeUtil(connectionInfo);
        this.argMap = SearchByQuery.getQueryArgMap(SearchByQuery.getQueryArgDataSet(this.getQueryProcessor(), sdcid, queryId, this.translator));
        String html = this.getArgDivHtml(queryId, (HashMap)this.argMap.get(queryId), sdcid, cookiekey, sysuser, 1);
        this.write(html);
    }

    public String getHtml(String sdcid, String currentUser, String cookieKey) {
        StringBuffer html = new StringBuffer();
        if (this.argMap != null && this.argMap.size() > 0) {
            Set keyset = this.argMap.keySet();
            Iterator itr = keyset.iterator();
            StringBuffer queryidlist = new StringBuffer();
            while (itr.hasNext()) {
                String queryid = (String)itr.next();
                HashMap queryprops = (HashMap)this.argMap.get(queryid);
                html.append(this.getArgDivHtml(queryid, queryprops, sdcid, cookieKey, currentUser, 1));
                if (queryidlist.length() == 0) {
                    queryidlist.append("['").append(SafeHTML.encodeForJavaScript(queryid));
                    continue;
                }
                queryidlist.append("','").append(SafeHTML.encodeForJavaScript(queryid));
            }
            html.append("<script> var __queryidlist = ").append(queryidlist).append("'];</script>\n");
        }
        return html.toString();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getArgDivHtml(String queryId, HashMap queryprops, String sdcid, String cookieKey, String currentUser, int mode) {
        boolean isPrompt = false;
        if (queryId != null && queryId.equalsIgnoreCase("prompt_[1p2r3o4m5p6t7]")) {
            queryId = "prompt";
            isPrompt = true;
        }
        if (this.request == null) {
            this.request = (HttpServletRequest)this.pageContext.getRequest();
        }
        RequestContext requestContext = (RequestContext)this.request.getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String loadqueryid = userConfig.getProperty("as_searchid_" + cookieKey);
        QueryProcessor qp = new QueryProcessor(requestContext.getConnectionId());
        StringBuilder htmlhead = new StringBuilder();
        StringBuilder html = new StringBuilder();
        HashMap tokenMap = null;
        boolean iscascaded = "Y".equals(queryprops.get("cascadedargflag"));
        String[] argvalues = null;
        HashSet dependOnCurrent = null;
        ArrayList arglist = (ArrayList)queryprops.get("arglist");
        if (arglist == null) return htmlhead.toString();
        if (arglist.size() <= 0) return htmlhead.toString();
        if (iscascaded) {
            HashMap argProps;
            int i;
            String arginto;
            if (this.pageContext != null) {
                this.pageContext.setAttribute("cascade", (Object)"Y");
            }
            this.request.setAttribute("cascade", (Object)"Y");
            String cascadequeryid = queryId;
            tokenMap = new HashMap();
            this.dependMap = new HashMap();
            int current = 0;
            if (this.request.getParameter("currentarg") != null) {
                String currentarg = this.request.getParameter("currentarg");
                current = Integer.parseInt(currentarg.substring(currentarg.length() - 1, currentarg.length()));
            }
            for (int k = 0; k < arglist.size(); ++k) {
                HashSet<Integer> dependset = new HashSet<Integer>();
                arginto = (String)((HashMap)arglist.get(k)).get("arginto");
                for (int i2 = 0; i2 < arglist.size(); ++i2) {
                    String[] tokens;
                    String argdata = (String)((HashMap)arglist.get(i2)).get("argdata");
                    for (String token : tokens = StringUtil.getTokens(argdata)) {
                        if (!arginto.equals("[" + token + "]")) continue;
                        dependset.add(new Integer(i2 + 1));
                    }
                }
                if (dependset.size() <= 0) continue;
                this.dependMap.put(new Integer(k + 1), dependset);
            }
            if (this.request.getParameter("argvaluelist") != null && queryId.equals(cascadequeryid)) {
                dependOnCurrent = this.getDependSet(new HashSet(), new Integer(current));
                argvalues = StringUtil.split(this.request.getParameter("argvaluelist"), ";");
                for (i = 0; i < arglist.size(); ++i) {
                    argProps = (HashMap)arglist.get(i);
                    arginto = (String)argProps.get("arginto");
                    if (dependOnCurrent.contains(new Integer(i + 1)) || argvalues[i].equals("disabled")) continue;
                    tokenMap.put(arginto, argvalues[i]);
                }
            } else {
                for (i = 0; i < arglist.size(); ++i) {
                    String defaultvalue;
                    argProps = (HashMap)arglist.get(i);
                    String string = defaultvalue = this.isLastSearchType && queryId.equals(loadqueryid) ? userConfig.getProperty("as_param" + i + "_" + cookieKey) : "";
                    if (!this.isLastSearchType && this.request.getParameter("param" + (i + 1)) != null) {
                        defaultvalue = this.request.getParameter("param" + (i + 1));
                    }
                    tokenMap.put(argProps.get("arginto"), defaultvalue);
                }
            }
        }
        boolean hasdisabledfield = false;
        for (int i = 0; i < arglist.size(); ++i) {
            String[] listitems;
            DataSet ds;
            boolean useeditorstyle;
            String argIdentifier;
            String defaultvalue;
            HashMap argProps = (HashMap)arglist.get(i);
            boolean lastItem = i == arglist.size() - 1;
            int argNumber = i + 1;
            String id = queryId + "_arg" + argNumber;
            String string = defaultvalue = this.isLastSearchType && queryId.equals(loadqueryid) ? userConfig.getProperty("as_param" + i + "_" + cookieKey) : "";
            if (iscascaded && tokenMap.size() == 0) {
                tokenMap.put(argProps.get("arginto"), defaultvalue);
            }
            if ((argIdentifier = (String)argProps.get("argid")).length() == 0) {
                argIdentifier = id;
            }
            String argdesc = (String)argProps.get("argdesc");
            boolean isMandatory = "Y".equals(argProps.get("mandatoryflag"));
            String argfromsdcid = (String)argProps.get("sdcid");
            if (defaultvalue.equals("")) {
                defaultvalue = (String)argProps.get("defaultvalue");
            }
            boolean hasDependents = false;
            if (iscascaded) {
                if (argvalues != null && argvalues.length == arglist.size() && !dependOnCurrent.contains(new Integer(argNumber))) {
                    defaultvalue = argvalues[argNumber - 1];
                }
                hasDependents = this.hasDepend(argNumber);
            }
            html.append("<tr><td class=\"modern_search_queryargtitle\" style=\"align:left;vertical-align:top\">").append(isPrompt ? argdesc : SafeHTML.encodeForHTML(argdesc)).append("</td>");
            if (mode == 1) {
                html.append("</tr><tr>");
            }
            html.append("<td class=\"search_queryargfield\" style=\"align:left;vertical-align:top;white-space:nowrap\">");
            String argId = (String)argProps.get("argid");
            String argType = (String)argProps.get("argtype");
            String editorstyleid = (String)argProps.get("editorstyleid");
            boolean bl = useeditorstyle = !"N".equals(argProps.get("useeditorstyleflag"));
            if (argType == null) {
                argType = "";
            }
            if (editorstyleid != null && editorstyleid.length() > 0 && useeditorstyle) {
                try {
                    if (this.pageContext == null && this.request != null) {
                        AjaxResponse ajaxResponse = new AjaxResponse(this.request, this.response);
                        this.pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), this.servletContext, this.request, this.response);
                    }
                    EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
                    editorStyleField.setEditorStyleId(editorstyleid);
                    editorStyleField.setFieldName(id);
                    PropertyList column = new PropertyList();
                    column.setProperty("columnid", id);
                    String cssclass = isMandatory ? "modern_search_mandatoryfield" : "modern_search_queryarginput";
                    column.setProperty("class", cssclass);
                    PropertyListCollection events = new PropertyListCollection();
                    PropertyList oninput = new PropertyList();
                    oninput.setProperty("event", "oninput");
                    oninput.setProperty("js", "this.value=this.value");
                    events.add(oninput);
                    column.setProperty("events", events);
                    editorStyleField.setReadonly(false);
                    editorStyleField.setChangeEvent("if(this.value.indexOf('|')>=0){this.value=this.value.substring(0, this.value.indexOf('|'))};" + (hasDependents ? "argvaluechanged(this);" : ""));
                    editorStyleField.setColumn(column);
                    if (defaultvalue != null) {
                        editorStyleField.setFieldValue(defaultvalue);
                    }
                    html.append(editorStyleField.getHtml());
                    if (hasDependents) {
                        html.append("<script>sapphire.util.dom.setAttribute( document.getElementById('" + id + "'), 'hasdepend', 'Y' );</script>");
                    }
                }
                catch (SapphireException se) {
                    html.append("<span>Error rendering editorstyle:" + editorstyleid + "." + se.getMessage() + "</span>");
                }
            } else if (argType.equals("sdc") || argType.equals("sqllookup") || argType.equals("sql")) {
                html.append("<table cellspacing=1 border=0><tr><td>");
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
                html.append("</td><td>");
                String weblookupurl = (String)argProps.get("weblookupurl");
                if (weblookupurl != null && weblookupurl.length() > 0) {
                    if (argType.equals("sdc")) {
                        html.append("<a href=\"\" onClick=\"JavaScript:lookupfield( '").append(id).append("', '").append(argfromsdcid).append("', '', '', 'lookupsdc','','','','','','','").append(weblookupurl).append("' );return false;\"><img src=\"WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg\" class=\"search_lookup_img\" title=\"").append(this.translator.translate("Lookup a SDC")).append("\" border=\"0\"></a>\n");
                    } else {
                        if (argProps.get("argdata") == null) return "Error: No Sql defined for query: " + queryId + " arg:" + argIdentifier;
                        html.append("<a href=\"\" onClick=\"JavaScript:sqllookup( '").append(id).append("', '', '', '").append(weblookupurl).append("', '" + queryId + "', '" + sdcid + "', '" + argId + "' );return false;\"><img src=\"" + lookupimg + "\" class=\"search_lookup_img\" title=\"").append(this.translator.translate("Lookup a Value")).append("\" border=\"0\"></a>\n");
                    }
                } else if (argType.equals("sdc")) {
                    html.append("<a href=\"\" onClick=\"JavaScript:lookupfield( '").append(id).append("', '").append(argfromsdcid).append("', '', '', 'lookupsdc' );return false;\"><img src=\"WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg\" class=\"search_lookup_img\" title=\"").append(this.translator.translate("Lookup a SDC")).append("\" border=\"0\"></a>\n");
                } else {
                    if (argProps.get("argdata") == null) return "Error: No Sql defined for query: " + queryId + " arg:" + argIdentifier;
                    if (isPrompt && argType.equals("sqllookup")) {
                        String sqlString = (String)argProps.get("argdata");
                        sqlString = StringUtil.replaceAll(sqlString, "'", ESC_SINGLEQUOTE);
                        String sqlcode = "" + sqlString.hashCode();
                        sqlCache.put(sqlcode, sqlString);
                        html.append("<a href=\"\" onClick=\"JavaScript:sqllookup( '").append(id).append("', '" + sqlcode + "', '', '', '', '', '' );return false;\"><img src=\"" + lookupimg + "\" class=\"search_lookup_img\" title=\"").append(this.translator.translate("Lookup a Value")).append("\" border=\"0\"></a>\n");
                    } else {
                        html.append("<a href=\"\" onClick=\"JavaScript:sqllookup( '").append(id).append("', '', '', '', '" + queryId + "', '" + sdcid + "', '" + argId + "' );return false;\"><img src=\"" + lookupimg + "\" class=\"search_lookup_img\" title=\"").append(this.translator.translate("Lookup a SDC")).append("\" border=\"0\"></a>\n");
                    }
                }
                html.append("</td></tr></table>");
            } else if (argType.equals("ddsdc")) {
                DataSet ds2 = (DataSet)this.request.getAttribute(argfromsdcid + "_ddsdc");
                if (ds2 == null) {
                    PropertyList sdc = (PropertyList)this.request.getAttribute(argfromsdcid + "_props");
                    if (sdc == null && argfromsdcid != null) {
                        sdc = new SDCProcessor(requestContext.getConnectionId()).getPropertyList(argfromsdcid);
                        this.request.setAttribute(argfromsdcid + "_props", (Object)sdc);
                    }
                    if (sdc == null) {
                        return "Can not find sdc definition for sdc:" + argfromsdcid;
                    }
                    String tableid = sdc.getProperty("tableid");
                    String keycol1 = sdc.getProperty("keycolid1");
                    String desccol = sdc.getProperty("desccol");
                    String sql = "select " + keycol1 + ", " + desccol + " from " + tableid + " WHERE ( activeflag='Y' OR activeflag is null ) order by usersequence, " + keycol1;
                    ds2 = qp.getSqlDataSet(sql);
                    this.request.setAttribute(argfromsdcid + "_ddsdc", (Object)ds2);
                }
                html.append(this.getSelectFromDataSet(id, argIdentifier, argdesc, ds2, defaultvalue, hasDependents, lastItem, isMandatory));
            } else if (argType.equals("reftype")) {
                String reftypeid = (String)argProps.get("reftypeid");
                ds = (DataSet)this.request.getAttribute(reftypeid + "_reftype");
                if (ds == null) {
                    ds = qp.getRefTypeDataSet(reftypeid);
                    this.request.setAttribute(reftypeid + "_reftype", (Object)ds);
                }
                html.append(this.getSelectFromDataSet(id, argIdentifier, argdesc, ds, defaultvalue, hasDependents, lastItem, isMandatory));
            } else if (argType.equals("string")) {
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
            } else if (argType.equals("number")) {
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
            } else if (argType.equals("absreldt")) {
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
                html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("' );return false;\"><img class=\"datelookup_img\" src=\"WEB-CORE/imageref/flat/16/flat_black_calendar2.svg\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
            } else if (argType.equals("dateonly")) {
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
                html.append("<a href=\"\" onClick=\"JavaScript:lookupdate( '").append(id).append("','','','','','O' );return false;\"><img class=\"datelookup_img\" src=\"WEB-CORE/imageref/flat/16/flat_black_calendar2.svg\" title=\"").append(this.translator.translate("Lookup a Date")).append("\" border=\"0\"></a>\n");
            } else if (argType.equals("ddsql")) {
                String sql = (String)argProps.get("argdata");
                if (iscascaded && tokenMap != null && tokenMap.size() > 0 && sql.indexOf("[") > 0) {
                    Set keyset = tokenMap.keySet();
                    for (String token : keyset) {
                        String tokenvalue = (String)tokenMap.get(token);
                        sql = StringUtil.replaceAll(sql, token, tokenvalue);
                    }
                }
                if ((sql = this.substituteTokens(sql, sdcid, currentUser)) != null && sql.indexOf("[") < 0) {
                    ds = new QueryProcessor(requestContext.getConnectionId()).getSqlDataSet(sql);
                    html.append(this.getSelectFromDataSet(id, argIdentifier, argdesc, ds, defaultvalue, hasDependents, lastItem, isMandatory));
                } else {
                    html.append(this.getDisabledSelect(id, argIdentifier));
                    hasdisabledfield = true;
                }
            } else if (argType.equals("ddlb")) {
                html.append("<select id=\"").append(id).append("\" name=\"").append(id).append("\" promptid=\"").append(argIdentifier).append("\" ");
                html.append(" fieldlabel=\"").append(DOMUtil.convertChars(argdesc)).append("\" ");
                if (hasDependents) {
                    html.append(hasDependents ? " hasdepend=\"Y\"" : "").append("  onchange=\"argvaluechanged( this )\"");
                }
                html.append(">");
                for (String listitem : listitems = StringUtil.split((String)argProps.get("argdata"), ";")) {
                    html.append(this.getOptionRow(listitem, "", defaultvalue));
                }
                html.append("</select>");
            } else if (argType.equals("dddef")) {
                if (argProps == null || !argProps.containsKey("argdata")) {
                    html.append("<span style='color:red;'>").append(this.getTranslationProcessor().translate("ERROR: Data retrieved exceeded global query limit."));
                    html.append("<br>").append(this.getTranslationProcessor().translate("Modify configuration or raise global RSET limit.")).append("</span>");
                } else {
                    html.append("<select id=\"").append(id).append("\" name=\"").append(id).append("\" promptid=\"").append(argIdentifier).append("\" fieldlabel=\"").append(DOMUtil.convertChars(argdesc)).append("\">");
                    if (((String)argProps.get("argdata")).length() > 0) {
                        listitems = StringUtil.split((String)argProps.get("argdata"), ";");
                        html.append("<option value=\"\"></option>");
                        for (String listitem : listitems) {
                            String[] s = StringUtil.split(listitem, "|");
                            html.append(this.getOptionRow(s[0], s[1], defaultvalue));
                        }
                    }
                    html.append("</select>");
                }
            } else {
                html.append(this.getDefault(id, defaultvalue, argIdentifier, argdesc, hasDependents, lastItem, isMandatory));
            }
            html.append("</td></tr>");
        }
        String cascadeargmessage = this.translator.translate("Please supply the following additional information:");
        if (mode == 1) {
            String tip;
            Button okButton = new Button(this.pageContext);
            okButton.setMargin("none");
            okButton.setAction("submitQueryArgs()");
            okButton.setImg("WEB-CORE/imageref/flat/16/flat_black_search.svg");
            okButton.setAppearance("ribbonsmall");
            okButton.setStyle("height:22px;width:27px; opacity:0.6; border: 1px solid rgba(26,26,26, 0.35); border-radius:3px; padding:2px 2px 2px 2px");
            String string = tip = queryprops.get("querydesc") == null ? "" : (String)queryprops.get("querydesc");
            if (!this.isCascadeArgCall) {
                htmlhead.append("<div title=\"").append(this.translator.translate(tip)).append("\" class=\"search_queryargdiv\" id=\"argsdiv_").append(queryId).append("\" style=\"display:none;width:100%;\">\n");
            }
            htmlhead.append("<br><fieldset class=\"search_fieldset\"><legend>").append(this.translator.translate(queryId)).append("</legend>");
            if (hasdisabledfield) {
                htmlhead.append(cascadeargmessage);
            }
            htmlhead.append("<table class=\"search_queryargtable\" border=\"0\" cellspacing=\"0\" cellborder=\"0\" width=\"98%\">\n");
            htmlhead.append(html.toString());
            if (!hasdisabledfield) {
                htmlhead.append("<tr><td style=\"padding-right:4px; padding-left:1px;\" align=\"right\">").append(okButton.getHtml()).append("</td></tr>");
            }
            htmlhead.append("</table>\n");
            htmlhead.append("</fieldset>\n");
            if (this.isCascadeArgCall) return htmlhead.toString();
            htmlhead.append("</div>\n");
            return htmlhead.toString();
        } else {
            htmlhead.append(html.toString());
        }
        return htmlhead.toString();
    }

    private String substituteTokens(String sql, String sdcid, String currentUser) {
        sql = StringUtil.replaceAll(sql, "[%currentuser%]", currentUser);
        sql = StringUtil.replaceAll(sql, "[%CURRENTUSER%]", currentUser);
        sql = StringUtil.replaceAll(sql, "[%CurrentUser%]", currentUser);
        sql = StringUtil.replaceAll(sql, "[%sdcid%]", sdcid);
        sql = StringUtil.replaceAll(sql, "[%SdcId%]", sdcid);
        sql = StringUtil.replaceAll(sql, "[%SDCID%]", sdcid);
        return sql;
    }

    private String getDefault(String id, String value, String argIdentifier, String argDesc, boolean hasdepend, boolean lastItem, boolean isMandatory) {
        Trace.log("id=" + id + " value= " + value);
        StringBuilder html = new StringBuilder();
        String cssclass = isMandatory ? "modern_search_mandatoryfield" : "modern_search_queryarginput";
        html.append("<input class=\"" + cssclass + "\" type=\"text\" size=\"17\" id=\"").append(id).append("\" name=\"").append(id).append("\" promptid=\"").append(argIdentifier).append("\" ");
        html.append(" fieldlabel=\"" + DOMUtil.convertChars(argDesc) + "\" ");
        html.append(value != null && value.length() > 0 ? " value=\"" + value + "\" " : "");
        if (hasdepend) {
            html.append(" hasdepend=\"Y\" onkeydown=\"if( event.keyCode==13 )javascript:argvaluechanged(this);\" onchange=\"argvaluechanged(this)\"");
        } else {
            html.append(" onkeydown=\"if(event.keyCode==13)javascript:submitQueryArgs();\" ");
        }
        html.append(">");
        return html.toString();
    }

    private String getSelectFromDataSet(String id, String argIdentifier, String argDesc, DataSet ds, String defaultvalue, boolean hasdepend, boolean lastItem, boolean isMandatory) {
        StringBuilder html = new StringBuilder();
        String cssclass = isMandatory ? "modern_search_mandatoryfield" : "modern_search_queryargselect";
        html.append("<select class=\"" + cssclass + "\" id=\"").append(id).append("\" name=\"").append(id).append("\" promptid=\"").append(argIdentifier).append("\" ");
        html.append(" fieldlabel=\"" + DOMUtil.convertChars(argDesc) + "\" ");
        if (hasdepend) {
            html.append("hasdepend=\"Y\" onchange=\"argvaluechanged( this )\"");
        }
        if (lastItem && !hasdepend) {
            html.append(" onkeydown=\"if ( event.keyCode == 13 ) javascript:submitQueryArgs();\"");
        }
        html.append(">");
        if (ds != null) {
            int rows = ds.getRowCount();
            html.append("<option value=\"\"></option>");
            boolean hasRefDisplayColumn = ds.isValidColumn("refdisplayvalue");
            boolean isRefType = ds.isValidColumn("reftypeid") && ds.isValidColumn("refvalueid");
            boolean hasTwoColumns = ds.getColumnCount() > 1;
            for (int i = 0; i < rows; ++i) {
                String value;
                String string = value = isRefType ? ds.getValue(i, "refvalueid") : ds.getValue(i, ds.getColumnId(0));
                html.append(this.getOptionRow(value, hasRefDisplayColumn ? ds.getValue(i, "refdisplayvalue") : (hasTwoColumns ? ds.getValue(i, ds.getColumnId(1)) : value), defaultvalue));
            }
        }
        html.append("</select>");
        return html.toString();
    }

    private String getDisabledSelect(String id, String argIdentifier) {
        return "<select class=\"modern_search_queryargselect\" id=\"" + id + "\" name=\"" + id + "\" promptid=\"" + argIdentifier + "\" disabled>&nbsp;</select>";
    }

    private String getOptionRow(String value, String displayvalue, String defaultvalue) {
        StringBuilder html = new StringBuilder();
        String display = value;
        if (displayvalue != null && displayvalue.length() > 0) {
            display = displayvalue;
        }
        if (value != null && value.equals(defaultvalue)) {
            html.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\" selected>").append(SafeHTML.encodeForHTML(this.translator.translate(display))).append("</option>");
        } else {
            html.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\">").append(SafeHTML.encodeForHTML(this.translator.translate(display))).append("</option>");
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

