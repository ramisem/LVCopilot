/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.pageelements.BaseGizmo;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GizmoTargetList
extends BaseElement {
    public static final String SESSION_VAR = "__gizmotarget_data";
    private String fieldid = "";
    private String notifymethod = "";
    private Mode mode = Mode.LOOKUP;
    private String category = "";
    private String gizmoType = "";
    private String searchString = "";
    private boolean allowFilterChange = true;
    private boolean showSearchArea = true;
    private DataSet gizmoTargets = null;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setGizmoType(String gizmoType) {
        this.gizmoType = gizmoType;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public GizmoTargetList() {
    }

    public GizmoTargetList(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public String getFieldId() {
        return this.fieldid;
    }

    public String getNotifyMethod() {
        return this.notifymethod;
    }

    public String getCategories() {
        DataSet cats;
        StringBuffer out = new StringBuffer();
        out.append("<option value=\"\"></option>");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Category");
        sdiRequest.setQueryOrderBy("categoryid");
        sdiRequest.setQueryFrom("category");
        sdiRequest.setQueryWhere("sdcid='LV_GizmoDef'");
        sdiRequest.setRequestItem("primary");
        SDIProcessor sdi = new SDIProcessor(this.pageContext);
        SDIData temp = sdi.getSDIData(sdiRequest);
        if (temp != null && (cats = temp.getDataset("primary")) != null && cats.size() > 0) {
            ArrayList<String> found = new ArrayList<String>();
            for (int c = 0; c < cats.getRowCount(); ++c) {
                String catId = cats.getValue(c, "categoryid", "");
                if (found.contains(catId)) continue;
                found.add(catId);
                if (catId.equalsIgnoreCase("sapphireconfig")) continue;
                out.append("<option").append(catId.equalsIgnoreCase(this.category) ? " SELECTED" : "").append(" value=\"").append(catId + "\">").append(catId).append("</option>");
            }
        }
        return out.toString();
    }

    public String getTypes() {
        DataSet cats;
        StringBuffer out = new StringBuffer();
        out.append("<option value=\"\"></option>");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("PropertyTree");
        sdiRequest.setQueryOrderBy("propertytreeid");
        sdiRequest.setQueryFrom("propertytree");
        sdiRequest.setQueryWhere("propertytreetype='Gizmo'");
        sdiRequest.setRequestItem("primary");
        SDIProcessor sdi = new SDIProcessor(this.pageContext);
        SDIData temp = sdi.getSDIData(sdiRequest);
        if (temp != null && (cats = temp.getDataset("primary")) != null && cats.size() > 0) {
            ArrayList<String> found = new ArrayList<String>();
            for (int c = 0; c < cats.getRowCount(); ++c) {
                String propertytreeid = cats.getValue(c, "propertytreeid", "");
                if (found.contains(propertytreeid)) continue;
                found.add(propertytreeid);
                out.append("<option").append(propertytreeid.equalsIgnoreCase(this.gizmoType) ? " SELECTED" : "").append(" value=\"").append(propertytreeid + "\">").append(propertytreeid).append("</option>");
            }
        }
        return out.toString();
    }

    public boolean showSearchArea() {
        return this.showSearchArea;
    }

    public String getSearchArea() {
        if (this.showSearchArea) {
            StringBuffer html = new StringBuffer();
            TranslationProcessor tp = this.getTranslationProcessor();
            html.append(tp.translate("Search")).append(": <input type=text id=\"search\" style=\"width:100px;\" onkeyup=\"gizmoTargetList.searchKey(this,event)\">");
            if (this.allowFilterChange) {
                html.append("&nbsp;&nbsp;");
                html.append(tp.translate("Type:"));
                html.append(" <select id=\"gizmotype\" style=\"width:100px;\" onchange=\"gizmoTargetList.filter(this,event);\"").append("").append(">");
                html.append(this.getTypes());
                html.append("</select>");
                html.append("&nbsp;&nbsp;");
                html.append(tp.translate("Category:"));
                html.append(" <select id=\"category\" style=\"width:100px;\" onchange=\"gizmoTargetList.filter(this,event);\">");
                html.append(this.getCategories());
                html.append("</select>");
            }
            return html.toString();
        }
        return "";
    }

    public static DataSet getGizmoTargets(String category, String gizmoType, PageContext pageContext) {
        DataSet gizmoTargets = null;
        if (pageContext != null) {
            SDIRequest sdiReq = new SDIRequest();
            sdiReq.setSDCid("LV_GizmoDef");
            if (category.length() > 0) {
                sdiReq.setQueryFrom("gizmodef, categoryitem");
            } else {
                sdiReq.setQueryFrom("gizmodef");
            }
            StringBuffer where = new StringBuffer();
            boolean isOracle = new ConnectionProcessor(pageContext).isOra();
            if (category.length() > 0) {
                where.append("gizmodefid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_GizmoDef' AND categoryitem.categoryid='").append(SafeSQL.encodeForSQL(category, isOracle)).append("')");
                if (gizmoType.length() > 0) {
                    where.append(" AND ");
                }
            }
            if (gizmoType.length() > 0) {
                where.append("propertytreeid='").append(SafeSQL.encodeForSQL(gizmoType, isOracle)).append("'");
            }
            if (where.length() > 0) {
                sdiReq.setQueryWhere(where.toString());
            }
            sdiReq.setRequestItem("primary");
            sdiReq.setQueryOrderBy("gizmodefid");
            sdiReq.setExtendedDataTypes(true);
            SDIData sdi = new SDIProcessor(pageContext).getSDIData(sdiReq);
            if (sdi != null) {
                gizmoTargets = sdi.getDataset("primary");
            }
        }
        return gizmoTargets;
    }

    public DataSet getGizmoTargets() {
        if (this.pageContext != null) {
            DataSet cached = (DataSet)this.pageContext.getSession().getAttribute(SESSION_VAR);
            if (cached == null) {
                this.gizmoTargets = GizmoTargetList.getGizmoTargets(this.category, this.gizmoType, this.pageContext);
                if (this.gizmoTargets != null) {
                    this.pageContext.getSession().setAttribute(SESSION_VAR, (Object)this.gizmoTargets);
                }
            } else {
                this.gizmoTargets = cached;
            }
        }
        return this.gizmoTargets;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void clearCache() {
        if (this.pageContext != null) {
            this.pageContext.getSession().removeAttribute(SESSION_VAR);
        }
    }

    public void loadProperties(PageTagInfo pageinfo) {
        this.fieldid = pageinfo.getProperty("fieldid");
        this.notifymethod = pageinfo.getProperty("notifymethod");
        this.category = pageinfo.getProperty("category");
        this.allowFilterChange = !pageinfo.getProperty("filter").equalsIgnoreCase("N");
        this.showSearchArea = !pageinfo.getProperty("search").equalsIgnoreCase("N");
        this.gizmoType = pageinfo.getProperty("gizmotype");
        if (pageinfo.getProperty("command").equalsIgnoreCase("page")) {
            PropertyList pd = pageinfo.getPropertyList("pagedata");
            if (pd != null) {
                try {
                    this.mode = Mode.valueOf(pd.getProperty("mode", Mode.LIST.toString()).toUpperCase());
                }
                catch (Exception e) {
                    this.mode = Mode.LIST;
                }
            } else {
                this.mode = Mode.LOOKUP;
            }
        }
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.gizmoTargets != null) {
            WebAdminProcessor wap = new WebAdminProcessor(this.pageContext);
            SapphireConnection sapphireConnection = new ConnectionProcessor(this.pageContext).getSapphireConnection();
            String search = this.searchString.toLowerCase();
            if ((search.startsWith("%") || search.startsWith("*")) && search.length() > 1) {
                search = search.substring(1);
            }
            if ((search.endsWith("%") || search.endsWith("*")) && search.length() > 1) {
                search = search.substring(0, search.length() - 1);
            }
            for (int i = 0; i < this.gizmoTargets.getRowCount(); ++i) {
                String gizmotargetid = this.gizmoTargets.getValue(i, "gizmodefid", "");
                String gizmotargetdesc = this.gizmoTargets.getValue(i, "gizmodefdesc", "");
                if (gizmotargetid.length() <= 0 || search.length() != 0 && !gizmotargetid.toLowerCase().contains(search) && !gizmotargetdesc.toLowerCase().contains(search)) continue;
                String ptreeid = this.gizmoTargets.getValue(i, "propertytreeid");
                String extendnodeid = this.gizmoTargets.getValue(i, "extendnodeid");
                if (ptreeid.length() <= 0 || extendnodeid.length() <= 0) continue;
                boolean group = ptreeid.equalsIgnoreCase("groupgizmo");
                BaseGizmo gizmoEl = BaseGizmo.getInstance(this.pageContext, gizmotargetid, true);
                if (gizmoEl == null) continue;
                gizmoEl.setConnectionId(this.connectionInfo.getConnectionId());
                gizmoEl.setElementid("gizmo_" + i);
                gizmoEl.setPreviewMode(true);
                html.append("<div class=\"ws_gizmotarget_holder").append(group ? " groupgizmo_container_large" : "").append("\" gizmodefid=\"").append(gizmotargetid).append("\" gizmodefdesc=\"").append(this.gizmoTargets.getValue(i, "gizmodefdesc", "")).append("\" propertytreeid=\"").append(this.gizmoTargets.getValue(i, "propertytreeid", "")).append("\" extendnodeid=\"").append(this.gizmoTargets.getValue(i, "extendnodeid", "")).append("\" onmouseout=\"gizmoTargetList.mouseOut(this,event);\" onclick=\"gizmoTargetList.click(this,event);\" onmouseover=\"gizmoTargetList.mouseOver(this,event);\">");
                html.append("<div class=\"ws_gizmotarget").append("\" id=\"ws_gizmotarget_").append(i).append("\">");
                gizmoEl.setGizmoStyle(BaseGizmo.GizmoStyle.LARGE);
                html.append(gizmoEl.getIconHtml());
                html.append("</div>");
                html.append("<div class=\"ws_gizmotarget_cover\"></div>");
                html.append("<div class=\"ws_gizmotarget_text\">");
                html.append(gizmotargetid);
                html.append("</div>");
                html.append("</div>");
            }
        }
        return html.toString();
    }

    public static enum Mode {
        LOOKUP,
        LIST;

    }
}

