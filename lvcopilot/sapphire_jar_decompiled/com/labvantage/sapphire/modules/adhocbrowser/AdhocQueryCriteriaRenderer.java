/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.AttributeCriteriaEditor;
import com.labvantage.sapphire.modules.adhocbrowser.DataEntryCriteriaEditor;
import com.labvantage.sapphire.modules.adhocbrowser.DefaultCriteriaEditor;
import com.labvantage.sapphire.modules.adhocbrowser.FieldEntryCriteriaEditor;
import com.labvantage.sapphire.modules.adhocbrowser.SearchableColumn;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.xml.PropertyList;

public class AdhocQueryCriteriaRenderer {
    PropertyList pagedata = null;
    HttpServletRequest request = null;
    private String sdcid = "";
    private String columnid = "";
    private String title = "";
    private SDCProcessor sdcProcessor = null;
    private TranslationProcessor tp = null;
    private SDITagUtil sdiTagUtil = null;
    AdhocMetaData adhocmetadata = null;
    String adhocqueryid = null;

    public AdhocQueryCriteriaRenderer(PageContext pageContext) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        this.pagedata = requestContext.getPropertyList().getPropertyList("pagedata");
        this.request = (HttpServletRequest)pageContext.getRequest();
        this.request.getSession().setAttribute("Adhocquery_pagedata", (Object)this.pagedata);
        this.sdcid = AdhocQueryPageUtil.getDefaultSdcId((ServletRequest)this.request, new QueryProcessor(pageContext));
        this.sdcProcessor = new SDCProcessor(requestContext.getConnectionId());
        this.tp = new TranslationProcessor(pageContext);
        this.sdiTagUtil = SDITagUtil.getInstance(pageContext);
        this.adhocmetadata = AdhocMetaData.getInstance(pageContext);
        this.columnid = this.request.getParameter("columnid");
        this.title = this.request.getParameter("title");
        this.adhocqueryid = this.request.getParameter("adhocqueryid");
    }

    public AdhocQueryCriteriaRenderer(HttpServletRequest request) throws SapphireException {
        this.request = request;
        this.pagedata = (PropertyList)request.getSession().getAttribute("Adhocquery_pagedata");
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        this.sdcProcessor = new SDCProcessor(requestContext.getConnectionId());
        this.tp = new TranslationProcessor(requestContext.getConnectionId());
        this.sdiTagUtil = new SDITagUtil(requestContext.getConnectionId());
        this.sdiTagUtil.setLanguage(requestContext.getPropertyList().getProperty("language"));
        this.adhocmetadata = AdhocMetaData.getInstance(request);
        this.columnid = request.getParameter("columnid");
        this.title = request.getParameter("title");
        this.sdcid = AdhocQueryPageUtil.getDefaultSdcId((ServletRequest)request, new QueryProcessor(requestContext.getConnectionId()));
        this.adhocqueryid = request.getParameter("adhocqueryid");
    }

    public PropertyList getSingleCriteriaPropertyList() throws SapphireException {
        String sdcid;
        PropertyList column = AdhocQueryPageUtil.getColumnPropertyList(this.sdcid, this.columnid, this.pagedata, this.sdcProcessor.getColumns(this.sdcid), this.tp);
        if (column == null) {
            column = new PropertyList();
        }
        String reverselinksdcid = "";
        String detailtableid = "";
        boolean isDetail = false;
        boolean isReverseFKDetail = false;
        SearchableColumn searchableCol = this.adhocmetadata.getSearchableColumn(this.adhocmetadata.getTableid(this.sdcid), this.columnid);
        if ((searchableCol == null || searchableCol.getColumndefinition() == null) && this.columnid.indexOf(".") > 0) {
            String prefix;
            detailtableid = prefix = this.columnid.substring(0, this.columnid.indexOf("."));
            if ("sdialias".equals(prefix)) {
                isDetail = true;
            }
            if ((reverselinksdcid = this.adhocmetadata.getSdcId(prefix)) != null && reverselinksdcid.length() > 0) {
                isReverseFKDetail = true;
                column.setProperty("isdetail", "true");
                if (!("DataSet".equals(reverselinksdcid) || "DataItem".equals(reverselinksdcid) || "SDIWorkItem".equals(reverselinksdcid) || "TrackItemSDC".equals(reverselinksdcid))) {
                    this.columnid = this.columnid.substring(this.columnid.indexOf(".") + 1);
                }
            }
        }
        column.setProperty("columnid", this.columnid);
        if (this.title != null && this.title.length() > 0) {
            column.setProperty("title", this.title);
        }
        if (this.columnid.indexOf("sdidataitem[") >= 0) {
            sdcid = this.sdcid;
            if (this.columnid.indexOf(".sdidataitem[") > 0) {
                String t = this.adhocmetadata.getTableid(sdcid);
                String c = this.columnid.substring(0, this.columnid.indexOf(".sdidataitem["));
                String reftableid = AdhocMetaData.getReferenceEntityName(this.sdcProcessor.getConnectionid(), t, c);
                sdcid = this.adhocmetadata.getSdcId(reftableid);
                if (sdcid == null && reftableid.indexOf("_") > 0) {
                    sdcid = reftableid.substring(reftableid.lastIndexOf("_") + 1);
                }
            }
            return new DataEntryCriteriaEditor().getEditorProperty(sdcid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
        }
        if (this.columnid.indexOf("field[") >= 0 || this.columnid.indexOf("worksheetitemfield[") >= 0) {
            sdcid = this.sdcid;
            if (this.columnid.indexOf(".field[") > 0) {
                String reftableid = AdhocMetaData.getReferenceEntityName(this.sdcProcessor.getConnectionid(), this.adhocmetadata.getTableid(sdcid), this.columnid.substring(0, this.columnid.lastIndexOf(".")));
                sdcid = this.adhocmetadata.getSdcId(reftableid);
            }
            return new FieldEntryCriteriaEditor().getEditorProperty(sdcid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
        }
        if (this.columnid.indexOf("attribute[") >= 0) {
            sdcid = this.sdcid;
            if (this.columnid.indexOf(".attribute[") > 0) {
                String reftableid = AdhocMetaData.getReferenceEntityName(this.sdcProcessor.getConnectionid(), this.adhocmetadata.getTableid(sdcid), this.columnid.substring(0, this.columnid.lastIndexOf(".")));
                sdcid = this.adhocmetadata.getSdcId(reftableid);
            }
            return new AttributeCriteriaEditor().getEditorProperty(sdcid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
        }
        if (isReverseFKDetail) {
            return new DefaultCriteriaEditor().getEditorProperty(reverselinksdcid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
        }
        if (isDetail) {
            return new DefaultCriteriaEditor().getEditorProperty(detailtableid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
        }
        return new DefaultCriteriaEditor().getEditorProperty(this.sdcid, column, this.adhocmetadata, this.sdcProcessor, this.sdiTagUtil, this.tp, this.pagedata);
    }

    public static DataSet getQueryidDataSet(String basedonsdcid, HttpServletRequest request) {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        QueryProcessor qp = new QueryProcessor(requestContext.getConnectionId());
        String sysuserid = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId()).getSysuserId();
        return qp.getPreparedSqlDataSet("select adhocqueryid, adhocquerydesc, shareableflag from adhocquery where basedonsdcid=? and ( createby=? or shareableflag='Y' ) order by adhocquerydesc", new Object[]{basedonsdcid, sysuserid});
    }

    public static String getQueryidDropDown(String basedonsdcid, String adhocqueryid, HttpServletRequest request, TranslationProcessor tp, DataSet adhocqueryDataSet) {
        StringBuffer adhocquerydropdown = new StringBuffer();
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        if (adhocqueryDataSet == null) {
            adhocqueryDataSet = AdhocQueryCriteriaRenderer.getQueryidDataSet(basedonsdcid, request);
        }
        if ("page".equals(requestContext.getProperty("command"))) {
            adhocquerydropdown.append("<select id=\"queryidselect\" onchange=\"sapphire.page.navigate( 'rc?command=page&page=" + requestContext.getProperty("page"));
        } else if ("file".equals(requestContext.getProperty("command"))) {
            adhocquerydropdown.append("<select id=\"queryidselect\" onchange=\"sapphire.page.navigate( 'rc?command=file&file=" + requestContext.getProperty("file"));
        } else if ("ajax".equals(requestContext.getProperty("command"))) {
            adhocquerydropdown.append("<select id=\"queryidselect\" onchange=\"sapphire.page.navigate( '" + request.getParameter("requesturl"));
        }
        adhocquerydropdown.append("&fromsearch=" + ("Y".equals(request.getParameter("fromsearch")) ? "Y" : "N") + "&sdcid=" + basedonsdcid + "&adhocqueryid=' + this.value )\">");
        if (adhocqueryDataSet.getRowCount() > 0) {
            adhocquerydropdown.append("<option>(" + tp.translate("New Query") + ")</option>");
        } else {
            adhocquerydropdown.append("<option>" + tp.translate("No Saved Queries") + "</option>");
        }
        for (int i = 0; i < adhocqueryDataSet.getRowCount(); ++i) {
            adhocquerydropdown.append("<option value=\"" + adhocqueryDataSet.getValue(i, "adhocqueryid") + "\" " + (adhocqueryDataSet.getValue(i, "adhocqueryid").equals(adhocqueryid) ? " selected" : "") + ">" + tp.translate(adhocqueryDataSet.getValue(i, "adhocquerydesc")) + ("Y".equals(adhocqueryDataSet.getValue(i, "shareableflag")) ? "(" + tp.translate("Shared") + ")" : "(" + tp.translate("My") + ")") + "</option>");
        }
        adhocquerydropdown.append("</select>");
        return adhocquerydropdown.toString();
    }

    public static String getClientValidationJS(PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        html.append("var sapdateformat = " + RegexConverter.getSapDateFormat(pageContext) + ";\n");
        FormatUtil formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(pageContext));
        html.append("var decimalSeparator = \"" + formatUtil.getDecimalSeparator() + "\";\n");
        html.append("var groupingSeparator = \"" + formatUtil.getGroupingSeparator() + "\";\n");
        return html.toString();
    }
}

