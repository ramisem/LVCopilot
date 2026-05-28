/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.opaldatabase;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BioBankSQLServerUpdate
extends BaseDiagnostic {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91438 $";

    public BioBankSQLServerUpdate(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    public BioBankSQLServerUpdate(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Check the BioBank configuration for SQL Server";
    }

    @Override
    public String getDescription() {
        return "Checks and repairs the BioBank configuration on a SQL Server installation";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        if (this.database.isSqlServer()) {
            String columnid;
            PropertyList column;
            int i;
            PropertyListCollection columns;
            PropertyList node;
            int errorcount = 0;
            ArrayList<String> faillist = new ArrayList<String>();
            try {
                PropertyTree list = PropertyTreeUtil.getPropertyTree(this.database, "list");
                node = list.getNodePropertyList("SMS Sample Product", true);
                if (node != null && (columns = node.getCollectionNotNull("columns")).size() > 0) {
                    for (i = 0; i < columns.size(); ++i) {
                        column = columns.getPropertyList(i);
                        columnid = column.getProperty("columnid");
                        if (!columnid.toLowerCase().contains("smsquery")) continue;
                        ++errorcount;
                        faillist.add("Element \"list\" in node \"SMS Sample Product\"");
                    }
                }
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
            try {
                PropertyTree maint = PropertyTreeUtil.getPropertyTree(this.database, "maint");
                node = maint.getNodePropertyList("SMS Sample Product", true);
                if (node != null && (columns = node.getCollectionNotNull("columns")).size() > 0) {
                    for (i = 0; i < columns.size(); ++i) {
                        column = columns.getPropertyList(i);
                        columnid = column.getProperty("columnid");
                        if (!columnid.toLowerCase().contains("smsquery")) continue;
                        ++errorcount;
                        faillist.add("Element \"maint\" in node \"SMS Sample Product\"");
                    }
                }
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
            try {
                errorcount += this.validateTreeListWebPage("LV_TissueLookup", "tissuefullname", "tissuedesc tissuefullname");
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
            try {
                errorcount += this.validateTreeListWebPage("LV_ClinicalLookup", "clinicaldiagfullname", "clinicaldiagdesc clinicaldiagfullname");
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
            try {
                errorcount += this.validateTreeListWebPage("LV_DiseaseLookup", "diseasefullname", "diseasedesc diseasefullname");
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
            if (errorcount > 0) {
                throw new DiagnosticException(1, "Found " + errorcount + " issues to be repaired");
            }
        }
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        if (this.database.isSqlServer()) {
            String columnid;
            PropertyList column;
            int i;
            PropertyListCollection columns;
            PropertyList node;
            ArrayList<PropertyList> remove = new ArrayList<PropertyList>();
            try {
                PropertyTree list = PropertyTreeUtil.getPropertyTree(this.database, "list");
                node = list.getNodePropertyList("SMS Sample Product", true);
                if (node != null && (columns = node.getCollectionNotNull("columns")) != null) {
                    for (i = 0; i < columns.size(); ++i) {
                        column = columns.getPropertyList(i);
                        columnid = column.getProperty("columnid");
                        if (!columnid.toLowerCase().contains("smsquery")) continue;
                        remove.add(column);
                    }
                    if (remove.size() > 0) {
                        Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing SMS Sample Product on list...");
                        for (PropertyList aRemove : remove) {
                            columns.remove(aRemove);
                            Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Removed column: Column ID=" + aRemove.getProperty("columnid") + ", Title=" + aRemove.getProperty("title"));
                        }
                        list.getNode("SMS Sample Product").setPropertyList(node);
                        PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "list", list.toXMLString());
                        Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixed SMS Sample Product on list.");
                    }
                }
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                remove.clear();
                PropertyTree maint = PropertyTreeUtil.getPropertyTree(this.database, "maint");
                node = maint.getNodePropertyList("SMS Sample Product", true);
                if (node != null && (columns = node.getCollectionNotNull("columns")).size() > 0) {
                    Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing SMS Sample Product on maint...");
                    for (i = 0; i < columns.size(); ++i) {
                        column = columns.getPropertyList(i);
                        columnid = column.getProperty("columnid");
                        if (columnid.toLowerCase().contains("nvl2")) {
                            Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Modifying column: Column ID=" + column.getProperty("columnid") + ", Title=" + column.getProperty("title"));
                            columnid = "case s_sampleid when null then '' else '' end title" + i;
                            column.setProperty("columnid", columnid);
                            String title = column.getProperty("title");
                            if (title != null && !title.trim().startsWith("<b>")) {
                                title = "&nbsp;";
                                column.setProperty("title", title);
                            }
                            Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Modified column to: Column ID=" + column.getProperty("columnid") + ", Title=" + column.getProperty("title"));
                            continue;
                        }
                        if (!columnid.toLowerCase().contains("smsquery")) continue;
                        remove.add(column);
                    }
                    if (remove.size() > 0) {
                        for (PropertyList aRemove : remove) {
                            columns.remove(aRemove);
                            Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Removed column: Column ID=" + aRemove.getProperty("columnid") + ", Title=" + aRemove.getProperty("title"));
                        }
                    }
                    maint.getNode("SMS Sample Product").setPropertyList(node);
                    PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "maint", maint.toXMLString());
                    Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixed SMS Sample Product on maint.");
                }
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing tissuefullname...");
                this.repairTreeListWebPage("LV_TissueLookup", "tissuefullname", "tissuedesc tissuefullname");
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing clinicaldiagfullname...");
                this.repairTreeListWebPage("LV_ClinicalLookup", "clinicaldiagfullname", "clinicaldiagdesc clinicaldiagfullname");
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing diseasefullname...");
                this.repairTreeListWebPage("LV_DiseaseLookup", "diseasefullname", "diseasedesc diseasefullname");
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                this.database.executeUpdate("update report set librarydir = 'BBMasterSampleHistory_mss.jrxml' where reportid = 'BBMasterSampleHistor'");
                this.database.executeUpdate("update report set librarydir = 'BBSampleDetail_mss.jrxml' where reportid = 'BBSampleDetail'");
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            try {
                StringBuilder sql = new StringBuilder();
                PropertyTree sqlview = PropertyTreeUtil.getPropertyTree(this.database, "sqlview");
                Node node2 = sqlview.getNode("StorageRestrictions Product");
                if (node2 != null) {
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = '[keyid1]'\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag, (case when sr.storageunitid = '[keyid1]' then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                if ((node2 = sqlview.getNode("Box Restrictions Product")) != null) {
                    sql.setLength(0);
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'LV_Box' and s.linkkeyid1 = '[keyid1]' )\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag,");
                    sql.append(" (case when sr.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'LV_Box' and s.linkkeyid1 = '[keyid1]' ) then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                if ((node2 = sqlview.getNode("Plate Restrictions Product")) != null) {
                    sql.setLength(0);
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'Plate' and s.linkkeyid1 = '[keyid1]' )\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag,");
                    sql.append(" (case when sr.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'Plate' and s.linkkeyid1 = '[keyid1]' ) then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                if ((node2 = sqlview.getNode("PhysicalStore Restrictions Product")) != null) {
                    sql.setLength(0);
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'PhysicalStore' and s.linkkeyid1 = '[keyid1]' )\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag,");
                    sql.append(" (case when sr.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'PhysicalStore' and s.linkkeyid1 = '[keyid1]' ) then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                if ((node2 = sqlview.getNode("Package Restrictions Product")) != null) {
                    sql.setLength(0);
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'LV_Package' and s.linkkeyid1 = '[keyid1]' )\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag,");
                    sql.append(" (case when sr.storageunitid = ( select s.storageunitid from storageunit s where s.linksdcid = 'LV_Package' and s.linkkeyid1 = '[keyid1]' ) then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                if ((node2 = sqlview.getNode("Manage Restrictions Product")) != null) {
                    sql.setLength(0);
                    sql.append("WITH StorageUnitTree (storageunitid, parentid, labelpath, linksdcid, linkkeyid1, Level)\n");
                    sql.append("AS (\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, 0 AS Level\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    WHERE su.storageunitid = '[keyid1]'\n");
                    sql.append("    UNION ALL\n");
                    sql.append("    SELECT su.storageunitid, su.parentid, su.labelpath, su.linksdcid, su.linkkeyid1, Level + 1\n");
                    sql.append("    FROM storageunit AS su\n");
                    sql.append("    INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid\n");
                    sql.append(")\n");
                    sql.append("select sr.storageunitid, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage msg, activeflag, (case when sr.storageunitid = '[keyid1]' then 'N' else 'Y' end) inherited\n");
                    sql.append("  from storagerestriction sr\n");
                    sql.append("  where sr.storageunitid in (SELECT storageunitid FROM StorageUnitTree WHERE Level >= 0)\n");
                    sql.append("  and sr.storageunitid != '[keyid1]'\n");
                    sql.append("  order by sr.storageunitid");
                    node2.getPropertyList().setProperty("sql", sql.toString());
                }
                Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] Fixing Storage Restrictions on SQLView...");
                PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "sqlview", sqlview.toXMLString());
            }
            catch (SapphireException e) {
                Trace.logError("[ Diagnostics: BioBankSQLServerUpdate ] Diagnostic error message: " + e.getMessage());
            }
            Trace.log("[ Diagnostics: BioBankSQLServerUpdate ] System is now SQL Server compatible");
        }
        return "System is now SQL Server compatible";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void repairTreeListWebPage(String webpageid, String columntitle, String columnvalue) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT webpageid, propertytreeid, elementid, productedition");
        sql.append(" FROM webpagepropertytree");
        sql.append(" WHERE webpageid=?");
        sql.append(" AND propertytreeid='treelist'");
        sql.append(" AND elementid='treelist'");
        HashSet<String> set = new HashSet<String>();
        String RESULTSET = this.database.newName();
        try {
            this.database.createPreparedResultSet(RESULTSET, sql.toString(), new String[]{webpageid});
            while (this.database.getNext(RESULTSET)) {
                set.add(this.database.getString(RESULTSET, "productedition"));
            }
        }
        finally {
            this.database.closeResultSet(RESULTSET);
        }
        for (Object e : set) {
            PropertyListCollection columns;
            String productedition = (String)e;
            PropertyList lookuplist = PropertyTreeUtil.getWebPagePropertyTreePropertyList(this.database, webpageid, "treelist", "treelist", productedition);
            if (lookuplist == null || (columns = lookuplist.getCollection("columns")) == null) continue;
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                String title = column.getProperty("title");
                if (title == null || title.trim().length() <= 0 || !columntitle.equalsIgnoreCase(title)) continue;
                column.setProperty("columnid", columnvalue);
            }
            PropertyTreeUtil.setWebPagePropertyTreeValue(this.database, webpageid, "treelist", "treelist", productedition, lookuplist.toXMLString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int validateTreeListWebPage(String webpageid, String columntitle, String columnvalue) throws SapphireException {
        int result = 0;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT webpageid, propertytreeid, elementid, productedition");
        sql.append(" FROM webpagepropertytree");
        sql.append(" WHERE webpageid=?");
        sql.append(" AND propertytreeid='treelist'");
        sql.append(" AND elementid='treelist'");
        HashSet<String> set = new HashSet<String>();
        String RESULTSET = this.database.newName();
        try {
            this.database.createPreparedResultSet(RESULTSET, sql.toString(), new String[]{webpageid});
            while (this.database.getNext(RESULTSET)) {
                set.add(this.database.getString(RESULTSET, "productedition"));
            }
        }
        finally {
            this.database.closeResultSet(RESULTSET);
        }
        for (Object e : set) {
            PropertyListCollection columns;
            String productedition = (String)e;
            PropertyList lookuplist = PropertyTreeUtil.getWebPagePropertyTreePropertyList(this.database, webpageid, "treelist", "treelist", productedition);
            if (lookuplist == null || (columns = lookuplist.getCollection("columns")) == null) continue;
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                String title = column.getProperty("title");
                if (title == null || title.trim().length() <= 0 || !columntitle.equalsIgnoreCase(title) || columnvalue.equals(column.getProperty("columnid"))) continue;
                ++result;
            }
        }
        return result;
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

