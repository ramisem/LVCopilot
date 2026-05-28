/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.advancedsearch.QueryArg;
import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import com.labvantage.sapphire.pageelements.search.SearchUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SearchByQuery
extends SearchContent {
    String contentName = "query";

    @Override
    public String getHtml() {
        StringBuffer output = new StringBuffer();
        String queryqueryfrom = "query";
        String queryquerywhere = "query.autoquoteflag='N' and query.basedonid='" + this.sdcid + "' ";
        String categorylist = this.contentProperties.getProperty("category");
        String filterlist = this.contentProperties.getProperty("filter");
        if (categorylist != null && categorylist.trim().length() > 0) {
            queryqueryfrom = queryqueryfrom + ",categoryitem";
            queryquerywhere = queryquerywhere + " and query.queryid=categoryitem.keyid1 and categoryitem.sdcid='Query' and categoryid in " + SearchUtil.toQueryInClause(categorylist);
        }
        if (filterlist != null && filterlist.trim().length() > 0) {
            queryquerywhere = queryquerywhere + " and query.queryid in " + SearchUtil.toQueryInClause(filterlist);
        }
        SDIProcessor sdip = new SDIProcessor(this.pageContext);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setQueryFrom(queryqueryfrom);
        sdiRequest.setQueryWhere(queryquerywhere);
        sdiRequest.setQueryOrderBy("query.usersequence, querylabel, queryid");
        sdiRequest.setRequestItem("primary[queryid,querydesc,querylabel]");
        sdiRequest.setSDCid("Query");
        sdiRequest.setRetrieve(true);
        PropertyList pageData = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
        DataSet queryids = null;
        if (pageData != null && pageData.getProperty("page").length() > 0) {
            String cachekey = pageData.getProperty("page") + "_CachedQueryDs";
            if (this.pageContext.getSession().getAttribute(cachekey) == null) {
                this.pageContext.setAttribute(cachekey, (Object)sdip.getSDIData(sdiRequest).getDataset("primary"));
            }
            queryids = (DataSet)this.pageContext.getAttribute(cachekey);
        } else {
            queryids = sdip.getSDIData(sdiRequest).getDataset("primary");
        }
        int rows = queryids.getRowCount();
        if (rows >= 1) {
            DataSet argDataSet;
            HashMap argMap;
            String queryList = "";
            String selectqueryid = "";
            boolean cascade = false;
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            PropertyList userConfig = requestContext.getPropertyList("userconfig");
            if (this.isLastSearchType) {
                selectqueryid = userConfig.getProperty("as_searchid_" + this.cookieKey);
            }
            if (selectqueryid == null || selectqueryid.length() == 0) {
                int findRow;
                String requestqueryid;
                selectqueryid = this.pageContext.getRequest().getParameter("cascadequeryid");
                boolean bl = cascade = selectqueryid != null && selectqueryid.length() > 0;
                if ((selectqueryid == null || selectqueryid.length() == 0) && (requestqueryid = this.pageContext.getRequest().getParameter("queryid")) != null && (findRow = queryids.findRow("queryid", requestqueryid)) >= 0) {
                    selectqueryid = requestqueryid;
                }
            }
            if (selectqueryid == null) {
                selectqueryid = "";
            }
            boolean isSelect = this.contentProperties.getProperty("style").equals("dropdownlist");
            boolean useDescription = this.contentProperties.getProperty("usedescription").equals("Y");
            boolean useLabel = !this.contentProperties.getProperty("uselabel").equals("N");
            output.append("<table id=\"querysearch_row\" style=\"padding-top:3px;padding-bottom:2px\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">");
            if (isSelect) {
                output.append("<tr>").append("<td><img style=\"margin:2px;visibility: hidden\" id=\"querypointer\" src=\"WEB-CORE/elements/images/selected_item.gif\"></td>").append("<td><select id=\"queryidselect\" class=\"search_inputfield search_selectfield\" onchange=\"openQuery( this.value )\">").append("<option value=\"\"></option>\n");
            }
            for (int i = 0; i < rows; ++i) {
                boolean isUsingLabel;
                String queryid = queryids.getString(i, "queryid");
                queryList = queryList + ";" + queryid;
                String querydesc = queryids.getValue(i, "querydesc");
                String querylabel = queryids.getValue(i, "querylabel");
                if (querydesc.length() == 0) {
                    querydesc = queryid;
                }
                boolean bl = isUsingLabel = useLabel && querylabel.length() > 0;
                String dvalue = isUsingLabel ? querylabel : (useDescription ? querydesc : queryid);
                dvalue = this.translator.translate(dvalue);
                querydesc = this.translator.translate(querydesc);
                queryids.setValue(i, "querylabel", dvalue);
                queryids.setValue(i, "querydesc", querydesc);
                if (isSelect) {
                    output.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(queryid)).append("\">").append(SafeHTML.encodeForHTML(dvalue)).append("</option>\n");
                    continue;
                }
                output.append("<tr><td nowrap id=\"td_").append(SafeHTML.encodeForHTMLAttribute(queryid)).append("\" ><img src=\"WEB-CORE/elements/images/selected_item.gif\" width=\"5\" height=\"9\" style=\"margin:2px;visibility: hidden\" id=\"querypointer_").append(SafeHTML.encodeForHTMLAttribute(queryid)).append("\"/>\n").append("<span class=\"modern_href\" ><a href=\"javascript:openQuery( '").append(SafeHTML.encodeForJavaScript(queryid)).append("' )\" title=\"").append(SafeHTML.encodeForHTMLAttribute(querydesc)).append("\">").append(SafeHTML.encodeForHTML(dvalue)).append("</a></span>&nbsp;").append("</td></tr>\n");
            }
            if (isSelect) {
                output.append("</select></td></tr>");
            }
            output.append("</table>");
            output.append("<script> var allqueriesJSON = " + queryids.toJSONString() + ";</script>\n");
            if (queryList.length() > 0) {
                queryList = queryList.substring(1);
            }
            if (requestContext != null && requestContext.getPropertyList("pagedata") != null) {
                requestContext.getPropertyList("pagedata").setProperty("searchbarquerylist", queryList);
            }
            if ((argMap = SearchByQuery.getQueryArgMap(argDataSet = SearchByQuery.getQueryArgDataSet(new QueryProcessor(this.pageContext), this.sdcid, queryList, this.translator))) != null) {
                QueryArg queryArg = new QueryArg(this.pageContext, this.isLastSearchType, argMap);
                output.append(queryArg.getHtml(this.sdcid, requestContext.getProperty("sysuserid"), this.cookieKey));
                for (int i = 0; i < argDataSet.size(); ++i) {
                    argDataSet.setValue(i, "argdata", EncryptDecrypt.obfsql(argDataSet.getValue(i, "argdata"), true));
                }
                output.append("<script> var allqueryArgsJSON = " + argDataSet.toJSONString() + ";</script>\n");
            }
            if (this.pageContext.getAttribute("cascade") != null && this.pageContext.getAttribute("cascade").equals("Y")) {
                output.append(this.getCascadeJSandForm());
            }
            String overridepageorder = "";
            try {
                int count = new QueryProcessor(this.pageContext).getPreparedCount("SELECT count(*) FROM query WHERE queryid=? and orderbyclause is NULL ", new Object[]{selectqueryid});
                if (count == 0 && "Y".equals(this.contentProperties.getProperty("usequeryorderby"))) {
                    overridepageorder = "Y";
                }
            }
            catch (SapphireException e) {
                Trace.logError("Unexpected exception when fetching the orderby clause." + e.getMessage());
            }
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            output.append("<script>\n").append("var onloadaction=\"" + this.element.getProperty("onloadaction") + "\";\n").append("var lastqueryid = \"\";\n").append("var defaultqueryid = \"" + this.contentProperties.getProperty("default") + "\";\n").append("function openQuery( queryid ) {\n").append("  if ( lastqueryid != queryid ) hideQueryPointer();\n").append("  lastqueryid = queryid;\n").append("  if ( document.getElementById( \"queryidselect\" ) != null ) { ").append("     document.getElementById( \"queryidselect\" ).value = queryid;\n").append("     document.getElementById( \"querypointer\" ).style.visibility = 'visible';\n").append("     document.getElementById( \"querysearch_row\" ).style.backgroundColor='#eff6fe';document.getElementById( \"querysearch_row\" ).style.border='1px solid #43a2d6';\n").append("  } ").append("  else { ").append("    var qpointer = document.getElementById( \"querypointer_\" + queryid );if ( qpointer != null ) { qpointer.style.visibility='visible';}\n").append("    var queryTD = document.getElementById( \"td_\" + queryid );\n").append("     if ( queryTD != null ) { queryTD.className ='modern_href selected'; }\n").append("  } ").append("\tif ( displayArgDiv( 'argsdiv_' + queryid ) ){\n").append("     if ( document.getElementById( queryid + '_arg1' ) != null ) document.getElementById( queryid + '_arg1' ).focus();\n");
            if (this.maxHeight > 0) {
                output.append(" sapphire.ui.util.scrollTo(__scrolldiv_").append(this.contentName).append(",document.getElementById('argsdiv_' + queryid),").append(this.maxHeight).append(");\n");
            }
            output.append("\t  queryidcach = queryid;\n").append("\t}\n").append("\telse {\n").append("  sapphire.userConfig.set( \"as_searchid_").append(this.cookieKey).append("\",  queryid )\n;").append("\t  doCallback( 'query', queryid, '").append(this.sdcid).append("', '','', '', '', '" + overridepageorder + "' );\n").append("\t  queryidcach = '';\n").append("\t}\n").append("}\n").append("function openSelectedQuery() {\n").append("  openQuery( \"").append(SafeHTML.encodeForJavaScript(selectqueryid)).append("\" );\n").append("  if ( queryidcach != \"\" ) submitQueryArgs( true );\n").append("}\n").append("function runDefaultQuery() {\n").append("  openQuery( \"").append(this.contentProperties.getProperty("default")).append("\" );\n").append("  if ( queryidcach != \"\" && document.getElementById( 'argsdiv_' + '").append(this.contentProperties.getProperty("default")).append("') == null ) submitQueryArgs( true );\n").append("}\n").append("function submitQueryArgs( isdefault, inarglist ) {\n").append("  var arglist = '';\n").append("  if ( typeof( inarglist ) == 'undefined' ) {\n").append("   for ( i = 1; i < 20; i++ ) {//\n").append("\t  if ( document.getElementById( queryidcach + '_arg'+ i ) != undefined ) { \n").append("      if ( document.getElementById( queryidcach + '_arg'+ i ).className == 'search_mandatoryfield' && document.getElementById( queryidcach + '_arg'+ i ).value =='' ) { if ( isdefault ) { return; } else { var diaObj = sapphire.alert( top.mandatoryNotFilled ); sapphire.ui.dialog.attachOnHideEvent( diaObj, function(){document.getElementById( queryidcach + \"_arg\"+ i ).focus();} ); return;} }\n").append("\t        arglist += ';' + document.getElementById( queryidcach + '_arg'+ i ).value;\n").append("\t  }\n").append("   }\n").append("  } else { arglist = inarglist; } \n").append("  if ( arglist.length > 0 ) { arglist = arglist.substring( 1 ).split( ';' );\n").append("   for ( i = 0; i < arglist.length; i++ ) {").append("      sapphire.userConfig.set( \"as_param\" + i + \"_").append(this.cookieKey).append("\", arglist[i], \"").append(this.cookieKey).append("\" );\n").append("      var index = i+1;\n").append("      document.getElementById( queryidcach +'_arg'+ index ).value=arglist[i];\n").append("   }\n").append("  }\n").append("  sapphire.userConfig.set( \"as_searchid_").append(this.cookieKey).append("\",  queryidcach )\n;").append("  doCallback( 'query', queryidcach, '").append(this.sdcid).append("', '").append(sdcProcessor.getProperty(this.sdcid, "tableid")).append("', '', arglist, '', '" + overridepageorder + "' );\n").append("}\n").append("function runCascadeQuery(){\n").append("  openQuery( '").append(selectqueryid).append("' );\n").append("  var arglist = '';\n").append("  for ( i = 1; i < 20; i++ ) {\n").append("\t  if ( document.getElementById( queryidcach + '_arg'+ i ) != undefined ) {\n").append("\t    arglist += ';' + document.getElementById( queryidcach + '_arg'+ i ).value;\n").append("      sapphire.userConfig.set( \"as_param\" + (i-1) + \"_").append(this.cookieKey).append("\", document.getElementById( queryidcach + '_arg'+ i ).value, \"").append(this.cookieKey).append("\" )\n;").append("\t  }\n").append("  }\n").append("  if ( arglist.length > 0 ) arglist = arglist.substring( 1 ).split( ';' );\n").append("  for ( var i=1; i<=arglist.length; i++ ) { \n").append("   if ( arglist[i-1] == '' || arglist[i-1] == undefined ) {\n").append("   arglist[i-1]= document.getElementById('param'+i).value;\n").append("   }\n").append("  document.getElementById('").append(selectqueryid).append("_arg").append("'+i).value=arglist[i-1];\n").append("  if(document.getElementById('").append(selectqueryid).append("_arg").append("'+i).getAttribute('hasdepend')!=undefined)\n").append("  argvaluechanged( document.getElementById('").append(selectqueryid).append("_arg").append("'+i) );\n").append("  submitargs.lookupcallback.value='setArgsValue';\n").append("  }\n").append("  doCallback( 'query', queryidcach, '").append(this.sdcid).append("', '").append(sdcProcessor.getProperty(this.sdcid, "tableid")).append("', '', arglist, '' ,'" + overridepageorder + "' );\n").append("}\n\n");
            if (selectqueryid.length() > 0) {
                output.append("sapphire.events.registerLoadListener(").append(cascade ? "runCascadeQuery" : "openSelectedQuery").append(",false,0);");
            }
            output.append("</script>");
        }
        if (this.maxHeight > 0) {
            ScrollPanel scrollPanel = new ScrollPanel(this.pageContext);
            scrollPanel.setId(this.contentName);
            scrollPanel.setMaxHeight(this.maxHeight);
            scrollPanel.setModernScroll(true);
            scrollPanel.setContent(output);
            return scrollPanel.getHtml();
        }
        return output.toString();
    }

    static DataSet getQueryArgDataSet(QueryProcessor qp, String sdcid, String queryidlist, TranslationProcessor tp) {
        SafeSQL safeSQL = new SafeSQL();
        String selectArgs = "SELECT query.queryid, query.basedonid, query.cascadedargflag, query.querydesc, queryarg.argid, queryarg.usersequence, queryarg.argdesc, queryarg.argtype, queryarg.sdcid, queryarg.reftypeid, queryarg.argdata, queryarg.defaultvalue, queryarg.arginto, queryarg.weblookupurl, queryarg.mandatoryflag, queryarg.editorstyleid, queryarg.useeditorstyleflag FROM query, queryarg WHERE query.queryid=queryarg.queryid  AND query.basedonid=queryarg.basedonid AND query.queryid in (" + safeSQL.addIn(queryidlist, ";") + ") and query.basedonid=" + safeSQL.addVar(sdcid) + " order by query.queryid, queryarg.usersequence";
        DataSet queryargs = qp.getPreparedSqlDataSet(selectArgs, safeSQL.getValues());
        for (int i = 0; i < queryargs.getRowCount(); ++i) {
            queryargs.setValue(i, "argdesc", tp.translate(queryargs.getValue(i, "argdesc").length() == 0 ? queryargs.getString(i, "argid") : queryargs.getValue(i, "argdesc")));
        }
        return queryargs;
    }

    static HashMap getQueryArgMap(DataSet queryargs) {
        HashMap argMap = new HashMap();
        int rows = queryargs.getRowCount();
        for (int i = 0; i < rows; ++i) {
            String queryid = queryargs.getString(i, "queryid");
            if (argMap.get(queryid) == null) {
                HashMap<String, Object> queryprops = new HashMap<String, Object>();
                queryprops.put("querydesc", queryargs.getString(i, "querydesc"));
                queryprops.put("cascadedargflag", queryargs.getString(i, "cascadedargflag"));
                ArrayList arglist = new ArrayList();
                queryprops.put("arglist", arglist);
                argMap.put(queryid, queryprops);
            }
            HashMap<String, String> argprops = new HashMap<String, String>();
            argprops.put("argid", queryargs.getString(i, "argid"));
            argprops.put("argdesc", queryargs.getValue(i, "argdesc"));
            argprops.put("argtype", queryargs.getString(i, "argtype"));
            argprops.put("sdcid", queryargs.getString(i, "sdcid"));
            argprops.put("reftypeid", queryargs.getString(i, "reftypeid"));
            argprops.put("argdata", queryargs.getString(i, "argdata"));
            argprops.put("defaultvalue", queryargs.getString(i, "defaultvalue"));
            argprops.put("arginto", queryargs.getString(i, "arginto"));
            argprops.put("weblookupurl", queryargs.getString(i, "weblookupurl"));
            argprops.put("mandatoryflag", queryargs.getString(i, "mandatoryflag"));
            argprops.put("editorstyleid", queryargs.getString(i, "editorstyleid"));
            argprops.put("useeditorstyleflag", queryargs.getString(i, "useeditorstyleflag"));
            ((ArrayList)((HashMap)argMap.get(queryid)).get("arglist")).add(argprops);
        }
        return argMap;
    }

    private StringBuffer getCascadeJSandForm() {
        StringBuffer html = new StringBuffer();
        html.append("<form id=\"submitargs\" name=\"submitargs\"  method=\"post\" action=\"rc?command=");
        String command = this.pageContext.getRequest().getParameter("command");
        String pageorfile = "";
        pageorfile = command != null && command.equals("page") ? this.pageContext.getRequest().getParameter("page") : this.pageContext.getRequest().getParameter("file");
        html.append(command).append("&").append(command).append("=").append(pageorfile);
        if (this.pageContext.getRequest().getParameter("_iframename") != null) {
            html.append("&_iframename=").append(this.pageContext.getRequest().getParameter("_iframename"));
        }
        html.append("\">\n");
        html.append("<input name=\"cascadequeryid\" type=\"hidden\" />\n<input name=\"argvaluelist\" type=\"hidden\" />\n<input name=\"currentarg\" type=\"hidden\"/>\n<input name=\"fieldid\" type=\"hidden\"/><input name=\"lookupcallback\" type=\"hidden\"/><input name=\"row\" type=\"hidden\"/><input name=\"sdcid\" type=\"hidden\" value=\"").append(this.sdcid).append("\"/><input name=\"cookiekey\" type=\"hidden\" value=\"").append(this.cookieKey).append("\"/></form>\n");
        return html;
    }
}

