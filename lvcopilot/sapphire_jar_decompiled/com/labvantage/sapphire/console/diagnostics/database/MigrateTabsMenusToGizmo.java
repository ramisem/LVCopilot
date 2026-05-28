/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.database;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MigrateTabsMenusToGizmo
extends BaseDiagnostic {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static List pageList = new ArrayList();

    public MigrateTabsMenusToGizmo(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    @Override
    public String getTitle() {
        return "Sitemap Migration To Gizmo";
    }

    @Override
    public String getDescription() {
        return "Move overrides and new sitemaps from Generic Layout to Menu Gizmo";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        try {
            if (this.database.getCount("SELECT count(*) FROM sysconfig WHERE propertyid='menutransfercomplete' AND propertyvalue='Y'") == 1) {
                return "Tabs and Menus have already been transferred";
            }
            this.createNewMenuGizmos();
            this.addMissingDashboardGizmos();
            this.database.executeUpdate("INSERT INTO sysconfig( propertyid, propertyvalue ) VALUES ( 'menutransfercomplete', 'Y' )");
        }
        catch (SapphireException e) {
            throw new DiagnosticException(e);
        }
        return "Tabs and Menus transferred";
    }

    private void createNewMenuGizmos() throws SapphireException {
        this.database.createResultSet("SELECT DISTINCT webpageid, extendnodeid FROM webpagepropertytree WHERE  webpageid in (SELECT DISTINCT webpageid FROM webpagepropertytree WHERE propertytreeid='TramlineSitemap')  AND propertytreeid='Generic' ");
        DataSet sitemapPages = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        String[] knownPages = new String[]{"LV_ReferenceSiteMap", "LabAdminTramline", "SystemAdminTramline", "Tramline Sitemap"};
        for (int i = 0; i < sitemapPages.size(); ++i) {
            String webpageid = sitemapPages.getString(i, "webpageid");
            String extendnodeid = sitemapPages.getString(i, "extendnodeid");
            if (Arrays.asList(knownPages).contains(webpageid)) continue;
            String gizmoid = webpageid;
            gizmoid = StringUtil.replaceAll(gizmoid, "Sitemap", "");
            gizmoid = StringUtil.replaceAll(gizmoid, "SiteMap", "");
            gizmoid = StringUtil.replaceAll(gizmoid, "Tramline", "");
            if ((gizmoid = StringUtil.replaceAll(gizmoid, " ", "")).length() == 0) {
                gizmoid = "New";
            }
            gizmoid = gizmoid + "Menu";
            if (this.database.getPreparedCount("SELECT count(*) FROM gizmodef WHERE gizmodefid=?", new Object[]{gizmoid}) == 1) {
                gizmoid = gizmoid + "_new";
            }
            if (gizmoid.length() > 40) {
                gizmoid = gizmoid.substring(gizmoid.length() - 40);
            }
            String sql = "INSERT INTO gizmodef ( gizmodefid, gizmodefdesc, propertytreeid, extendnodeid, valuetree, createdt, createtool, moddt, modtool ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            String propertylist = "<propertylist>\n\t<property id=\"showinpicker\" type=\"simple\" ><![CDATA[Y]]></property>\n\t<property id=\"gizmoprops\" type=\"propertylist\" expanded=\"Y\">\n\t\t<propertylist id=\"root_0\" >\n\t\t\t<property id=\"title\" type=\"simple\" ><![CDATA[" + gizmoid + "]]></property>\n\t\t</propertylist>\n\t</property>\n\t<property id=\"type\" type=\"simple\" ><![CDATA[Full Menu]]></property>\n</propertylist>";
            try {
                this.database.executePreparedUpdate(sql, new Object[]{gizmoid, "Migrated from Generic Layout", "menugizmo", extendnodeid, propertylist, DateTimeUtil.getNowTimestamp(), "Upgrade", DateTimeUtil.getNowTimestamp(), "Upgrade"});
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
            PropertyList sitemapProps = PropertyTreeUtil.getWebPagePropertyTreePropertyList(this.database, webpageid, "TramlineSitemap", "pagedata");
            sitemapProps.setProperty("gizmodefid", gizmoid);
            PropertyTreeUtil.setWebPagePropertyTreeValue(this.database, webpageid, "TramlineSitemap", "pagedata", sitemapProps.toXMLString());
        }
    }

    private void addMissingDashboardGizmos() throws SapphireException {
        this.database.createResultSet("SELECT DISTINCT webpageid FROM webpagepropertytree WHERE propertytreeid='Dashboard'");
        DataSet dashboardPages = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        String[] knownPages = new String[]{"ConnectionDashboard", "MyDashboard", "SampleDashboard", "TestingDashboard"};
        ArrayList<String> newGizmos = new ArrayList<String>();
        for (int i = 0; i < dashboardPages.size(); ++i) {
            String webpageid = dashboardPages.getString(i, "webpageid");
            if (Arrays.asList(knownPages).contains(webpageid)) continue;
            String gizmoid = webpageid;
            if (this.database.getPreparedCount("SELECT count(*) FROM gizmodef WHERE gizmodefid=?", new Object[]{gizmoid}) == 1) {
                gizmoid = gizmoid + "_new";
            }
            if (gizmoid.length() > 40) {
                gizmoid = gizmoid.substring(gizmoid.length() - 40);
            }
            String sql = "INSERT INTO gizmodef ( gizmodefid, gizmodefdesc, propertytreeid, extendnodeid, valuetree, createdt, createtool, moddt, modtool ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            String propertylist = "<propertylist >\n\t<property id=\"gizmoprops\" type=\"propertylist\" expanded=\"Y\">\n\t\t<propertylist id=\"root_0\" >\n\t\t\t<property id=\"title\" type=\"simple\" ><![CDATA[" + gizmoid + "]]></property>\n\t\t</propertylist>\n\t</property>\n\t<property id=\"image\" type=\"simple\" ><![CDATA[WEB-CORE/images/png/Dashboard.png]]></property>\n\t<property id=\"url\" type=\"simple\" ><![CDATA[rc?command=page&page=MyDashboard]]></property>\n</propertylist>";
            try {
                this.database.executePreparedUpdate(sql, new Object[]{gizmoid, "Added During Upgrade", "urlgizmo", "Sapphire Custom", propertylist, DateTimeUtil.getNowTimestamp(), "Upgrade", DateTimeUtil.getNowTimestamp(), "Upgrade"});
                newGizmos.add(gizmoid);
                continue;
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
        if (newGizmos.size() > 0) {
            String grouppl = "<propertylist >\n        \t<property id=\"gizmos\" type=\"collection\" selectedindex=\"0\" expanded=\"N\">\n        \t\t<collection>";
            int count = 20;
            long sequence = 100000000L;
            for (String gizmoid : newGizmos) {
                grouppl = grouppl + "        \t\t\t<propertylist id=\"p" + sequence + "\" sequence=\"" + sequence + "\">\n        \t\t\t\t<property id=\"id\" type=\"simple\" ><![CDATA[groupitem" + count + "]]></property>\n        \t\t\t\t<property id=\"gizmoid\" type=\"simple\" ><![CDATA[" + gizmoid + "]]></property>\n        \t\t\t</propertylist>\n";
                sequence += 10000000L;
                ++count;
            }
            grouppl = grouppl + "        \t\t</collection>\n        \t</property>\n        </propertylist>\n";
            try {
                String sql = "UPDATE gizmodef SET valuetree=? WHERE gizmodefid='DashboardGroup'";
                this.database.executePreparedUpdate(sql, new Object[]{grouppl});
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }
}

