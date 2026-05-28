/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RecentItemsGizmo
extends MenuGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String IMAGE = "FlatWhiteTimeClock";

    @Override
    public boolean init() {
        super.init();
        return true;
    }

    @Override
    public String getTitle() {
        return this.imageTitle;
    }

    private PropertyListCollection getRecentItems() {
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        SapphireConnection sapphireConnection = connectionProcessor.getSapphireConnection();
        int items = 7;
        StringBuffer select = new StringBuffer("SELECT\twt.webpagelogid, wt.title \"text\", wt.tip, w.webpagerequest FROM\twebpagelogtitle wt,webpagelog w WHERE\tw.webpagelogid IN ( ");
        if (sapphireConnection.getDbms().equals("MSS")) {
            select.append("( SELECT TOP ").append(items).append(" webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) ");
        } else {
            select.append("( SELECT webpagelogid FROM ( SELECT webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) WHERE rownum < ").append(items).append(" ) ");
        }
        select.append(") AND w.webpagelogid = wt.webpagelogid ORDER BY w.requestdt DESC");
        DataSet recentitems = this.getQueryProcessor().getPreparedSqlDataSet(select.toString(), new Object[]{sapphireConnection.getSysuserId()});
        PropertyListCollection data = new PropertyListCollection();
        for (int i = 0; i < recentitems.getRowCount(); ++i) {
            PropertyList row = new PropertyList("row_" + i);
            row.setProperty("webpagelogid", recentitems.getString(i, "webpagelogid"));
            row.setProperty("title", recentitems.getString(i, "text"));
            row.setProperty("tip", recentitems.getString(i, "tip"));
            data.add(row);
        }
        return data;
    }

    @Override
    protected void setUpProperties() {
        this.imageTitle = "Recent Items";
        if (this.element == null) {
            this.element = new PropertyList();
        }
        this.element.setProperty("image", IMAGE);
        PropertyListCollection links = new PropertyListCollection();
        PropertyListCollection plcRecentItems = new PropertyListCollection();
        plcRecentItems = this.pageContext != null && this.pageContext.getAttribute("recentitems", 2) != null ? (PropertyListCollection)this.pageContext.getAttribute("recentitems", 2) : this.getRecentItems();
        for (int i = 0; i < plcRecentItems.size(); ++i) {
            PropertyList recentItem = plcRecentItems.getPropertyList(i);
            String url = "rc?command=history&history=" + recentItem.getProperty("webpagelogid");
            PropertyList newlink = new PropertyList();
            newlink.setProperty("id", this.elementid + i);
            newlink.setProperty("text", recentItem.getProperty("title"));
            newlink.setProperty("releaselocks", "Y");
            newlink.setProperty("link", url);
            links.add(newlink);
        }
        this.element.setProperty("type", "Single Item");
        this.element.setProperty("menu", links);
    }
}

