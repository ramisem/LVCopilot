/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.tagext.SDITag;
import java.util.ArrayList;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListGizmo
extends sapphire.pageelements.BaseGizmo {
    public static final String PAGEID_PROPERTY = "pageid";
    public static final String WEBPAGEID_PROPERTY = "webpageid";
    public static final String ELEMENTID_PROPERTY = "elementid";
    public static final String SELECTORTYPE_PROPERTY = "selectortype";
    public static final String INITIALSELECTALL_PROPERTY = "initselectall";
    public static final String INITEXPANDALL_PROPERTY = "initexpandall";
    public static final String INITALGROUPED_PROPERTY = "initialgrouped";
    public static final String COLUMNS_PROPERTY = "columns";
    public static final String TITLE_PROPERTY = "title";
    public static final String MODE_PROPERTY = "mode";

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(SELECTORTYPE_PROPERTY, "Y");
        up.setProperty(INITIALSELECTALL_PROPERTY, "Y");
        up.setProperty(INITEXPANDALL_PROPERTY, "Y");
        up.setProperty(INITALGROUPED_PROPERTY, "Y");
        PropertyList up_prop = new PropertyList();
        up_prop.setProperty(TITLE_PROPERTY, "N");
        up_prop.setProperty(MODE_PROPERTY, "Y");
        up.setProperty(COLUMNS_PROPERTY, up_prop);
        return up;
    }

    @Override
    public boolean init() {
        this.setRefreshOnResize(false);
        this.setResizable(true);
        this.setTimeout(-1);
        return true;
    }

    public static DataSet buildSQLDataSet(String sql, String connectionId, ConnectionProcessor connectionProcessor, QueryProcessor qp) {
        String currentuser = connectionProcessor.getConnectionInfo(connectionId).getSysuserId();
        sql = StringUtil.replaceAll(StringUtil.replaceAll(sql, "[%currentuser%]", currentuser, false), "[currentuser]", currentuser, false);
        return qp.getSqlDataSet(sql);
    }

    public static String buildSDIRequest(SDIRequest sdi, String sdcId, PropertyList element, String elementid, RequestContext rc) {
        String restrictiveWhere = "";
        String queryid = "";
        String queryfrom = "";
        String querywhere = "";
        String queryorderby = "";
        int retriveLimit = 1000;
        String[] params = new String[12];
        StringBuffer keyid1 = new StringBuffer();
        StringBuffer keyid2 = new StringBuffer();
        StringBuffer keyid3 = new StringBuffer();
        if (rc != null) {
            String pageno;
            PropertyList pagedata = rc.getPropertyList();
            if (pagedata.containsKey("keyid1")) {
                keyid1.append(pagedata.getProperty("keyid1"));
                keyid2.append(pagedata.getProperty("keyid2"));
                keyid3.append(pagedata.getProperty("keyid3"));
            }
            if ((pageno = pagedata.getProperty("pageno", "")).length() == 0 || pageno.equalsIgnoreCase("1")) {
                queryid = element.getPropertyList("defaultquery").getProperty("queryid", "");
                queryfrom = element.getPropertyList("defaultquery").getProperty("queryfrom", "(default)");
                querywhere = element.getPropertyList("defaultquery").getProperty("querywhere", "");
                try {
                    retriveLimit = Integer.parseInt(element.getProperty("retrievelimit", "1000"));
                }
                catch (Exception e) {
                    retriveLimit = 1000;
                }
                restrictiveWhere = element.getPropertyList("defaultquery").getProperty("restrictivewhere", "");
                for (int index = 0; index < element.getPropertyList("defaultquery").getCollectionNotNull("params").size(); ++index) {
                    PropertyList param = element.getPropertyList("defaultquery").getCollection("params").getPropertyList(index);
                    params[index] = param.getProperty("value", "");
                }
            } else {
                querywhere = element.getPropertyList("defaultquery").getProperty("querywhere", "");
                SDITag.processPageRequest(keyid1, keyid2, keyid3, pageno, pagedata.getProperty("rowsperpage"));
            }
            rc.setProperty(elementid, element);
        }
        queryorderby = element.getPropertyListNotNull("defaultquery").getProperty("queryorderby");
        String mergequerywhere = "N";
        if (restrictiveWhere.length() > 0) {
            mergequerywhere = "Y";
            if (querywhere == null) {
                querywhere = "";
            }
            if (querywhere.length() > 0) {
                querywhere = "(" + querywhere + ") AND ";
            }
            querywhere = querywhere + "(" + restrictiveWhere + ")";
        }
        String versionstatusfilter = "";
        String showTemplates = "false";
        sdi.setSDCid(sdcId);
        sdi.setQueryid(queryid);
        sdi.setQueryParams(params);
        sdi.setQueryFrom(queryfrom);
        sdi.setQueryWhere(querywhere);
        sdi.setQueryOrderBy(queryorderby);
        sdi.setKeyid1List(keyid1.toString());
        sdi.setKeyid2List(keyid2.toString());
        sdi.setKeyid3List(keyid3.toString());
        sdi.setVersionStatus(versionstatusfilter);
        sdi.setShowTemplates(showTemplates);
        sdi.setRetrieveLimit(retriveLimit);
        ArrayList linkcolist = new ArrayList();
        PropertyListCollection columns = element.getCollectionNotNull(COLUMNS_PROPERTY);
        ListGizmo.getRequestItemCols(columns, linkcolist);
        String requestItem = "primary";
        if (linkcolist.size() > 0) {
            requestItem = requestItem.replaceFirst("primary", "primary[" + ListGizmo.getColumnList(linkcolist) + "]");
        }
        sdi.setRequestItem(requestItem);
        return mergequerywhere;
    }

    @Override
    public String getScript() {
        StringBuffer script = new StringBuffer();
        if (this.getGizmoStyle() == BaseGizmo.GizmoStyle.FULL) {
            script.append("document.getElementById('").append(this.elementid).append("_form').submit();");
        }
        return script.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            this.setGizmoStyle(BaseGizmo.GizmoStyle.FULL);
            ArrayList<String> ignore = new ArrayList<String>();
            ignore.add("href");
            PropertyList elementsql = new PropertyList();
            if (this.element.containsKey("sql")) {
                elementsql.setProperty("sql", this.element.getProperty("sql"));
                BaseGizmo.evaluateExpression(this.getGizmoDefId(), elementsql, BaseGizmo.I18NFormat.DATABASE, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore);
            }
            BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore);
            if (this.element.containsKey("sql")) {
                this.element.replace("sql", elementsql.getProperty("sql", this.element.getProperty("sql")));
            }
            String onload = this.element.getProperty("iframeload", "");
            html.append("<iframe style=\"width:100%;height:100%;\" src=\"");
            html.append(this.browser.getBlankSrc());
            html.append("\" frameborder=0 name=\"").append(this.elementid).append("_iframe\" id=\"").append(this.elementid).append("_iframe\"").append(onload.length() > 0 ? " onload=\"" + onload + "\"" : "").append("></iframe>");
            html.append("<form style=\"display:none;\" id=\"").append(this.elementid).append("_form\"\" action=\"").append("rc?command=file&file=WEB-CORE/modules/dashboard/listviewconvertor.jsp").append("\" method=\"POST\" target=\"").append(this.elementid).append("_iframe\">");
            html.append("<input type=\"hidden\" name=\"").append(ELEMENTID_PROPERTY).append("\" value=\"").append(this.elementid).append("\">");
            if (this.getGizmoLocation() == BaseGizmo.GizmoLocation.DASHBOARD) {
                html.append("<input type=\"hidden\" name=\"").append("scrolling").append("\" value=\"").append("N").append("\">");
            }
            html.append("<input type=\"hidden\" name=\"").append("gizmolocation").append("\" value=\"").append(this.getGizmoLocation().toString()).append("\">");
            html.append("<input type=\"hidden\" name=\"").append("gizmodefid").append("\" value=\"").append(this.getGizmoDefId()).append("\">");
            if (this.element.getProperty(WEBPAGEID_PROPERTY, "").length() > 0) {
                html.append("<input type=\"hidden\" name=\"").append(PAGEID_PROPERTY).append("\" value=\"").append(this.element.getProperty(WEBPAGEID_PROPERTY, "")).append("\">");
            } else {
                html.append("<textarea name=\"").append("gizmoproperties").append("\">").append(HttpUtil.encodeURIComponent(this.element.toJSONString(false))).append("</textarea>");
            }
            html.append("<textarea name=\"").append("parameters").append("\">").append(this.getParameters().toJSONString(false).toString()).append("</textarea>");
            html.append("</form>");
        }
        return html.toString();
    }

    @Override
    public String getIcon() {
        return this.getImage("List Gizmo", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "FlatWhiteTable";
    }

    @Override
    public String getIconHtml() {
        int size = 16;
        BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
        String h = this.getHelpText();
        h = SafeHTML.encodeForHTML(h, true);
        StringBuffer html = new StringBuffer();
        ArrayList<String> ignore = new ArrayList<String>();
        ignore.add("href");
        PropertyList elementsql = new PropertyList();
        if (this.element.containsKey("sql")) {
            elementsql.setProperty("sql", this.element.getProperty("sql"));
            BaseGizmo.evaluateExpression(this.getGizmoDefId(), elementsql, BaseGizmo.I18NFormat.DATABASE, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore);
        }
        BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore);
        if (this.element.containsKey("sql")) {
            this.element.replace("sql", elementsql.getProperty("sql", this.element.getProperty("sql")));
        }
        if (gizmoStyle.showImage) {
            html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : "").append(">");
            html.append(this.getIcon());
            if (gizmoStyle != BaseGizmo.GizmoStyle.SMALLTEXT) {
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS() + ";event.cancelBubble=true;", this.elementid));
            }
            html.append("</span>");
        }
        if (gizmoStyle.showTitle) {
            String t = this.getTitle();
            t = SafeHTML.encodeForHTML(t, true);
            String titleColor = this.getTitleColor();
            html.append("<span title=\"").append(h).append("\" id=\"").append(this.elementid).append("_text\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : "");
            html.append(titleColor.length() > 0 ? " style=\"color:" + titleColor + "\"" : "").append(">");
            html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(t).append("</span>");
            if (gizmoStyle == BaseGizmo.GizmoStyle.SMALLTEXT) {
                html.append("</span>");
                html.append("<span").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_notify\"" : "").append(">");
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                html.append("</span>");
            } else {
                if (!gizmoStyle.showImage || gizmoStyle == BaseGizmo.GizmoStyle.SMALLTEXT) {
                    html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                }
                html.append("</span>");
            }
        }
        return html.toString();
    }

    @Override
    public String getURL() {
        StringBuffer url = new StringBuffer();
        url.append(this.element.getProperty("listpage", ""));
        if (url.length() > 0) {
            String queryid = this.element.getPropertyList("defaultquery").getProperty("queryid", "");
            String queryfrom = this.element.getPropertyList("defaultquery").getProperty("queryfrom", "(default)");
            String querywhere = this.element.getPropertyList("defaultquery").getProperty("querywhere", "");
            if (queryid.length() > 0) {
                url.append("&queryid=").append(queryid);
                for (int index = 0; index < this.element.getPropertyList("defaultquery").getCollection("params").size(); ++index) {
                    PropertyList param = this.element.getPropertyList("defaultquery").getCollection("params").getPropertyList(index);
                    url.append("&param=").append(index).append(param.getProperty("value", ""));
                }
            }
            if (queryfrom.length() > 0) {
                url.append("&queryfrom=").append(queryfrom);
            }
            if (querywhere.length() > 0) {
                url.append("&querywhere=").append(querywhere);
            }
            return url.toString();
        }
        return "";
    }

    @Override
    public int getCount() {
        int rows = 0;
        String sdcId = this.element.getProperty("sdcid", "");
        String sql = this.element.getProperty("sql", "");
        if (sdcId.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            String mergequerywhere = ListGizmo.buildSDIRequest(sdiRequest, sdcId, this.element, this.elementid, RequestContext.getRequestContext(this.pageContext));
            String[] params = sdiRequest.getQueryParams();
            SDIData sdi = this.getSDIProcessor().getSDIData(sdiRequest);
            if (sdi != null && sdi.getDataset("primary") != null) {
                rows = sdi.getDataset("primary").getRowCount();
            }
        } else if (sql.length() > 0) {
            DataSet ds = ListGizmo.buildSQLDataSet(sql, this.getConnectionId(), this.getConnectionProcessor(), this.getQueryProcessor());
            rows = ds != null ? ds.getRowCount() : 0;
        }
        return rows;
    }

    private static void getRequestItemCols(PropertyListCollection columns, ArrayList columnlist) {
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                String colid;
                if ("Do Not Retrieve".equals(columns.getPropertyList(i).getProperty(MODE_PROPERTY)) || (colid = columns.getPropertyList(i).getProperty("columnid")).length() <= 0 || columnlist.indexOf(colid) >= 0) continue;
                columnlist.add(colid);
            }
        }
    }

    private static String getColumnList(ArrayList columnlist) {
        StringBuffer cols = new StringBuffer();
        for (int i = 0; i < columnlist.size(); ++i) {
            if (i == 0) {
                cols.append((String)columnlist.get(i));
                continue;
            }
            cols.append("," + (String)columnlist.get(i));
        }
        return cols.toString();
    }
}

