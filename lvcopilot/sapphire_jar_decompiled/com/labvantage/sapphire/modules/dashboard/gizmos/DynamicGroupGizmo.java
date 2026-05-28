/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupGizmo;
import java.util.ArrayList;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DynamicGroupGizmo
extends GroupGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    private String buildIN(String idCol, String ids) {
        StringBuilder out = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        String[] dc = StringUtil.split(ids, ";");
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < dc.length; ++i) {
            if (part.length() > 0) {
                part.append(",");
            }
            part.append("'").append(dc[i]).append("'");
            if (i < 900) continue;
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
            part = new StringBuilder();
        }
        if (part.length() > 0) {
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
        }
        return sql.toString();
    }

    @Override
    public boolean init() {
        this.dynamicgroup = true;
        BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), this.getGroovyBindMap(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), null, true);
        String parameterid = this.element.getProperty("parameterid", LABVANTAGE_CVS_ID);
        String sql = this.element.getProperty("sql", LABVANTAGE_CVS_ID);
        String sdcid = this.element.getProperty("sdcid", LABVANTAGE_CVS_ID);
        String gizmoid = this.element.getProperty("gizmoid", LABVANTAGE_CVS_ID);
        String gizmoprop = this.element.getProperty("gizmoproperty", LABVANTAGE_CVS_ID);
        String column = this.element.getProperty("columnid", LABVANTAGE_CVS_ID);
        boolean showheaders = this.element.getProperty("showdashboardheaders", "Y").equalsIgnoreCase("Y");
        try {
            this.pagenum = Integer.parseInt(this.element.getProperty("pagenum", this.getParameter(this.getGizmoDefId() + "_pagenum", "1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.pagesize = Integer.parseInt(this.element.getProperty("pagesize", "10"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        ArrayList dataSet = null;
        if (sdcid.length() > 0 && gizmoid.length() > 0 && gizmoprop.length() > 0 && column.length() > 0) {
            SDIData sdiData;
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            if (this.element.getProperty("querywhere").length() > 0) {
                sdiRequest.setQueryWhere(this.element.getProperty("querywhere"));
            }
            sdiRequest.setQueryFrom(this.getSDCProcessor().getProperty(sdcid, "tableid"));
            sdiRequest.setRequestItem("primary");
            if (this.element.getProperty("orderby").length() > 0) {
                sdiRequest.setQueryOrderBy(this.element.getProperty("orderby"));
            }
            if ((sdiData = this.getSDIProcessor().getSDIData(sdiRequest)) != null) {
                dataSet = sdiData.getDataset("primary");
            }
        } else if (sql.length() > 0 && gizmoid.length() > 0 && gizmoprop.length() > 0 && column.length() > 0) {
            dataSet = this.getQueryProcessor().getSqlDataSet(sql);
        }
        if (dataSet != null && dataSet.size() > 0) {
            this.getElementProperties().setProperty("showdashboardheaders", showheaders ? "Y" : "N");
            ArrayList<DataSet> pages = ((DataSet)dataSet).getSplitDataSets(this.pagesize);
            this.totalpages = pages.size();
            boolean pagenated = this.totalpages > 0;
            PropertyListCollection gizmosColl = this.element.getCollectionNotNull("gizmos");
            DataSet baseGizmo = BaseGizmo.getGizmoDef(gizmoid, this.connectionInfo.getDatabaseId(), this.connectionInfo.getSysuserId(), this.getConnectionId());
            if (baseGizmo != null && baseGizmo.size() == 1) {
                String gizmotypenode = baseGizmo.getValue(0, "extendnodeid");
                String gizmotypeid = baseGizmo.getValue(0, "propertytreeid");
                DataSet page = pages.get(pagenated ? this.pagenum - 1 : 0);
                int startRow = 0;
                int endRow = page.getRowCount();
                StringBuilder paramVal = new StringBuilder();
                for (int i = startRow; i < endRow; ++i) {
                    PropertyList gizmo = new PropertyList();
                    gizmo.setProperty("id", i + "-" + StringUtil.getRandomString(5));
                    gizmo.setProperty("gizmoid", gizmoid);
                    gizmo.setProperty("show", "Y");
                    gizmo.setProperty("ptreeid", gizmotypeid);
                    gizmo.setProperty("extendnodeid", gizmotypenode);
                    PropertyList grid = new PropertyList();
                    int x = 2;
                    int y = 2;
                    try {
                        x = Integer.parseInt(this.element.getProperty("dimensionx", LABVANTAGE_CVS_ID + x));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        y = Integer.parseInt(this.element.getProperty("dimensiony", LABVANTAGE_CVS_ID + y));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    grid.setProperty("size_x", LABVANTAGE_CVS_ID + x);
                    grid.setProperty("size_y", LABVANTAGE_CVS_ID + y);
                    gizmo.setProperty("grid", grid);
                    PropertyList customprops = new PropertyList();
                    customprops.setProperty(gizmoprop, page.getValue(i, column, LABVANTAGE_CVS_ID));
                    customprops.setProperty("_dynamicprops", "Y");
                    gizmo.setProperty("gizmoprops", customprops);
                    gizmosColl.add(gizmo);
                    if (paramVal.length() > 0) {
                        paramVal.append(";");
                    }
                    paramVal.append(page.getValue(i, column, LABVANTAGE_CVS_ID));
                }
                if (parameterid.length() > 0 && !this.getParameterSource(parameterid).equalsIgnoreCase("request")) {
                    this.setParameter(parameterid, paramVal.toString());
                }
            } else {
                this.logger.error("Failed to load gizmo.");
            }
        } else {
            this.logger.error("Required properties not set.");
        }
        return super.init();
    }

    @Override
    public String getHtml() {
        return super.getHtml();
    }

    @Override
    public String getIconHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<div style=\"height:100%;display: flex;justify-content: center; align-items: center;\">");
        s.append("<div style=\"flex: 0 0 auto;\">");
        s.append(this.getTranslationProcessor().translate("Enlarge To View Details"));
        s.append("</div>");
        s.append("</div>");
        return s.toString();
    }
}

