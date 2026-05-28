/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.opaldatabase;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class InstrumentListNodeDiagnostic
extends BaseDiagnostic {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85988 $";
    private String dbmstype = "";

    public InstrumentListNodeDiagnostic(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    public InstrumentListNodeDiagnostic(DBAccess database, ConnectionInfo conenctionInfo, String dbmstype) {
        super(database, conenctionInfo);
        this.dbmstype = dbmstype;
    }

    public InstrumentListNodeDiagnostic(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Check the Instrument List Node configuration for SQL Server";
    }

    @Override
    public String getDescription() {
        return "Checks and repairs the Instrument List Node configuration on a SQL Server installation";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "Fix required for SQL Server to replace Oracle specific syntax.";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        if (this.database.isSqlServer() || "MSS".equalsIgnoreCase(this.dbmstype)) {
            try {
                PropertyListCollection columns;
                PropertyTree list = PropertyTreeUtil.getPropertyTree(this.database, "list");
                PropertyList node = list.getNodePropertyList("Instrument2 Product", true);
                if (node != null && (columns = node.getCollectionNotNull("columns")).size() > 0) {
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList column = columns.getPropertyList(i);
                        String columnid = column.getProperty("columnid");
                        String title = column.getProperty("title");
                        if (!title.equalsIgnoreCase("calib status") && !title.equalsIgnoreCase("maint status") || !columnid.contains("sysdate")) continue;
                        columnid = columnid.replaceAll("sysdate", "getdate()");
                        column.setProperty("columnid", columnid);
                    }
                }
                list.getNode("Instrument2 Product").setPropertyList(node);
                Trace.log("[ Diagnostics: Instrument List Node ] Fixing Oracle specific syntax...");
                PropertyTreeUtil.setPropertyTreeValue(this.database, "(system)", "list", list.toXMLString());
            }
            catch (SapphireException e) {
                Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            }
        }
        return "Fixed the Oracle specific syntax in instrument list node.";
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

