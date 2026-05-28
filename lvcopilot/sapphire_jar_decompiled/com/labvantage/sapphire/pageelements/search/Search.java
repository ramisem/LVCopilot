/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.search;

import com.labvantage.sapphire.pageelements.search.QueryArg;
import com.labvantage.sapphire.pageelements.search.SearchByBasic;
import com.labvantage.sapphire.pageelements.search.SearchByCategory;
import com.labvantage.sapphire.pageelements.search.SearchByQuery;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Search
extends BaseElement {
    private HashMap argMap = null;
    private SDCProcessor sdcProcessor;
    private String sdcid;
    private String defaultquery = null;
    private String defaultcategory = null;

    public Search() {
    }

    public Search(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    @Override
    public String getHtml() {
        String casqueryid;
        String title;
        if (this.element == null) {
            return "ERROR: No data found for the sdisearch element.";
        }
        if (this.sdcid == null || this.sdcid.length() == 0) {
            this.sdcid = this.element.getProperty("sdcid");
            if (this.sdcid == null || this.sdcid.length() == 0) {
                return "ERROR: No sdcid defined for the sdisearch element.";
            }
        } else {
            this.element.setProperty("sdcid", this.sdcid);
        }
        this.sdcProcessor = this.getSDCProcessor();
        StringBuffer html = new StringBuffer();
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/search.js\"></script>\n");
        StringBuffer basicHtml = new StringBuffer();
        String position = "";
        PropertyList option = this.element.getPropertyList("basicsearch");
        if (option == null || option != null && !option.getProperty("show").equals("N")) {
            SearchByBasic basic = new SearchByBasic(this.pageContext, this.getConnectionId());
            basic.setElementProperties(this.element);
            basicHtml.append("<table class=\"search_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
            basicHtml.append(basic.getHtml());
            basicHtml.append("</table>\n");
            String string = position = option != null ? option.getProperty("position") : "";
            if (option == null || position == null || position.length() == 0 || position.equals("top")) {
                html.append(basicHtml.toString());
            }
        }
        if ((option = this.element.getPropertyList("querysearch")) == null || option != null && !option.getProperty("show").equals("N")) {
            SearchByQuery query = new SearchByQuery(this.pageContext, this.getConnectionId());
            query.setElementProperties(this.element);
            String queryidrows = query.getHtml();
            if (queryidrows.length() > 0) {
                title = this.element.getPropertyList("querysearch") != null ? this.element.getPropertyList("querysearch").getProperty("title", "Search By Query:") : "Search By Query:";
                html.append("<p class=\"search_header\"><b>" + title + "</b></p>");
                html.append(this.addScrollDiv("queryidDiv", option, queryidrows));
                this.argMap = query.getQueryArgMap();
                if (option != null) {
                    this.defaultquery = option.getProperty("default");
                }
            }
        }
        if ((option = this.element.getPropertyList("categorysearch")) == null || option != null && !option.getProperty("show").equals("N")) {
            if (option != null) {
                this.defaultcategory = option.getProperty("default");
            }
            if (!(this.defaultquery != null && this.defaultquery.length() != 0 || this.defaultcategory != null && this.defaultcategory.length() != 0)) {
                this.element.setProperty("hasdefault", "N");
            }
            SearchByCategory category = new SearchByCategory(this.pageContext, this.getConnectionId());
            category.setElementProperties(this.element);
            String categoryidrows = category.getHtml();
            if (categoryidrows.length() > 0) {
                title = this.element.getPropertyList("categorysearch") != null ? this.element.getPropertyList("categorysearch").getProperty("title", "Search By Category:") : "Search By Category:";
                html.append("<p class=\"search_header\"><b>" + title + "</b></p>");
                html.append(this.addScrollDiv("categoryidDiv", option, categoryidrows));
            }
        }
        if (position != null && position.equals("bottom")) {
            html.append(basicHtml.toString());
        }
        html.append("<br />");
        String callbackfunction = this.element.getProperty("callback");
        if (callbackfunction == null || callbackfunction.length() == 0) {
            callbackfunction = "parent.showResult";
        }
        html.append(this.getJavaScript(callbackfunction));
        if (this.argMap != null) {
            QueryArg queryArg = new QueryArg(this.pageContext, this.getConnectionId(), this.argMap);
            queryArg.setElementProperties(this.element);
            html.append(queryArg.getHtml());
        }
        if (this.pageContext.getAttribute("cascade") != null && this.pageContext.getAttribute("cascade").equals("Y")) {
            html.append(this.getCascadeJSandForm());
        }
        if ((casqueryid = this.pageContext.getRequest().getParameter("cascadequeryid")) != null && casqueryid.length() > 0) {
            html.append("<script>\n");
            html.append("startSearch( 'query','" + casqueryid + "' );\n");
            html.append("</script>\n");
        }
        return html.toString();
    }

    private String addScrollDiv(String divId, PropertyList option, String html) {
        boolean useDiv;
        StringBuffer divhtml = new StringBuffer();
        boolean bl = useDiv = option != null && option.getProperty("height") != null && option.getProperty("height").trim().length() != 0;
        if (useDiv) {
            divhtml.append("<div id=\"" + divId + "\" style=\"display:block;overflow:auto;width:170px;height:" + option.getProperty("height") + "px\">\n");
        }
        divhtml.append("<table class=\"search_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        divhtml.append(html);
        divhtml.append("</table>\n");
        if (useDiv) {
            divhtml.append("</div>\n");
        }
        return divhtml.toString();
    }

    private String getJavaScript(String callback) {
        String querywhere;
        String string = querywhere = this.element.getPropertyList("basicsearch") != null ? this.element.getPropertyList("basicsearch").getProperty("whereclause") : "";
        if (querywhere.length() > 0) {
            querywhere = "'" + StringUtil.replaceAll(querywhere, "'", "\\'") + "'";
            querywhere = StringUtil.replaceAll(querywhere, "[]", "' + searchid + '");
        } else {
            querywhere = "'lower( " + this.sdcProcessor.getProperty(this.sdcid, "tableid") + "." + this.sdcProcessor.getProperty(this.sdcid, "keycolid1") + " ) like lower( \\'%' + searchid + '%\\' )                   or lower( " + this.sdcProcessor.getProperty(this.sdcid, "desccol") + " ) like lower( \\'%' + searchid + '%\\' )'";
        }
        return "<script>\nfunction startSearch( searchtype, queryid ) {\n    if ( searchtype == 'basic' || searchtype == 'text' ) {\n        if ( current != null ) current.src = null;\n        if ( document.getElementById( 'categoryidselect' ) != undefined ) document.getElementById( 'categoryidselect' ).value='';\n        if ( document.getElementById( 'queryidselect' ) != undefined ) document.getElementById( 'queryidselect' ).value='';\n\t     if ( currentArgDiv != null ) document.getElementById( currentArgDiv ).style.display = 'none';\n        searchid = replaceSingeQuot( document.getElementById('searchtext').value );\n        querywhere =" + querywhere + ";\n        " + callback + "( searchtype, searchid, '" + this.sdcid + "','" + this.sdcProcessor.getProperty(this.sdcid, "tableid") + "', querywhere );\n    }\n    else if ( queryid != undefined && queryid.length > 0 ) {\n        if ( current != null ) current.src='WEB-CORE/elements/images/transgif.gif';\n        if ( document.getElementById(queryid) != undefined ) {\n\t     document.getElementById(queryid).src = 'WEB-CORE/elements/images/arwrt.gif';\n\t     current=document.getElementById( queryid );\n        }\n        if ( searchtype == 'query' ) {\n            if ( document.getElementById( 'categoryidselect' ) != undefined ) document.getElementById( 'categoryidselect' ).value='';\n\t     \tif ( displayArgDiv( 'argsdiv_' + queryid ) ){//has args\n\t\t\t\tqueryidcach = queryid;\n\t     \t}\n\t     else {\n\t         " + callback + "(  searchtype, queryid, '" + this.sdcid + "' );\n\t     }\n        }\n        else if ( searchtype == 'category' ) {\n            if ( document.getElementById( 'queryidselect' ) != undefined ) document.getElementById( 'queryidselect' ).value='';\n\t         if ( currentArgDiv != null ) document.getElementById( currentArgDiv ).style.display = 'none';\n            querywhere = 'categoryid=\\'' + queryid + '\\' and categoryitem.sdcid=\\'" + this.sdcid + "\\' and categoryitem.keyid1=" + this.sdcProcessor.getProperty(this.sdcid, "tableid") + "." + this.sdcProcessor.getProperty(this.sdcid, "keycolid1") + "';\n\t     " + callback + "( searchtype,  queryid, '" + this.sdcid + "','" + this.sdcProcessor.getProperty(this.sdcid, "tableid") + ", categoryitem', querywhere );\n        }\n    }\n}\nfunction submitQueryArgs() {\n    var arglist = '';\n    for ( i = 1; i < 20; i++ ) {//maximum 20 params should be plenty\n\tif ( document.getElementById( queryidcach + '_arg'+ i ) != undefined ) {\n\t    arglist += ';' + document.getElementById( queryidcach + '_arg'+ i ).value;\n\t}\n    }\n    if ( arglist.length > 0 ) arglist = arglist.substring( 1 ).split( ';' );\n    " + callback + "( 'query', queryidcach, '" + this.sdcid + "', '" + this.sdcProcessor.getProperty(this.sdcid, "tableid") + "', '', arglist );\n}\n</script>\n";
    }

    private String getCascadeJSandForm() {
        StringBuffer html = new StringBuffer();
        html.append("<form id=\"submitargs\" name=\"submitargs\"  method=\"post\" action=\"rc?command=");
        String command = this.pageContext.getRequest().getParameter("command");
        String pageorfile = "";
        pageorfile = command != null && command.equals("page") ? this.pageContext.getRequest().getParameter("page") : this.pageContext.getRequest().getParameter("file");
        html.append(command + "&" + command + "=" + pageorfile);
        if (this.pageContext.getRequest().getParameter("_iframename") != null) {
            html.append("&_iframename=" + this.pageContext.getRequest().getParameter("_iframename"));
        }
        html.append("\">\n");
        html.append("<input name=\"cascadequeryid\" type=\"hidden\" />\n<input name=\"argvaluelist\" type=\"hidden\" />\n<input name=\"currentarg\" type=\"hidden\"/>\n<input name=\"fieldid\" type=\"hidden\"/><input name=\"lookupcallback\" type=\"hidden\"/><input name=\"row\" type=\"hidden\"/></form>\n");
        return html.toString();
    }
}

