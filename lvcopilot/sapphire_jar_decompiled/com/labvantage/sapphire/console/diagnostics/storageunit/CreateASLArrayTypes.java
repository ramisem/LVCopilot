/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.storageunit;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CreateASLArrayTypes
extends BaseDiagnostic {
    public CreateASLArrayTypes(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    @Override
    public String getTitle() {
        return "Create Array Types for ASL Grids";
    }

    @Override
    public String getDescription() {
        return "Create Array Types for ASL Grids";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        throw new DiagnosticException(1, "Create Array Types for existing Grid storage unit type LV version prior to LV 8.4");
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        String returnMessage = "No ASL Array types created";
        try {
            ArrayList<String> aslArrayTypeList = new ArrayList<String>();
            this.database.createResultSet("ASLArrayTypeRS", "select arraytypeid from arraytype where aslflag = 'Y'");
            DataSet ds = new DataSet();
            ds.setResultSet(this.database.getResultSet("ASLArrayTypeRS"));
            for (int i = 0; i < ds.size(); ++i) {
                aslArrayTypeList.add(ds.getString(i, "arraytypeid"));
            }
            DataSet dsInsert = new DataSet();
            PropertyTree propertyTree = PropertyTreeUtil.getPropertyTree(this.database, "Grid");
            ArrayList allNodes = propertyTree.getAllNodes();
            for (Node node : allNodes) {
                String nodeid = node.getNodeId();
                if (node.isProduct() || node.isCustom() || aslArrayTypeList.contains("ASL " + nodeid)) continue;
                PropertyList gridProperty = propertyTree.getNodePropertyList(nodeid, true);
                int row = dsInsert.addRow();
                dsInsert.setString(row, "arraytypeid", "ASL " + nodeid);
                dsInsert.setString(row, "arraytypeversionid", "1");
                dsInsert.setString(row, "arraytypedesc", "Grid Type " + nodeid);
                dsInsert.setString(row, "classification", "ASL");
                dsInsert.setNumber(row, "numrows", gridProperty.getProperty("rows"));
                dsInsert.setNumber(row, "numcolumns", gridProperty.getProperty("columns"));
                dsInsert.setString(row, "horizontallabeltype", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("type"));
                dsInsert.setString(row, "horizontallabelstart", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("startat"));
                dsInsert.setString(row, "horizontallabeldirection", gridProperty.getPropertyListNotNull("indexorder").getProperty("horizontal").equals("Left->Right") ? "LR" : "RL");
                dsInsert.setString(row, "verticallabeltype", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("type"));
                dsInsert.setString(row, "verticallabelstart", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("startat"));
                dsInsert.setString(row, "verticallabeldirection", gridProperty.getPropertyListNotNull("indexorder").getProperty("vertical").equals("Top->Bottom") ? "TB" : "BT");
                dsInsert.setString(row, "activeflag", "Y");
                dsInsert.setString(row, "versionstatus", "C");
                dsInsert.setString(row, "aslflag", "Y");
            }
            if (dsInsert.size() > 0) {
                dsInsert.setString(-1, "createby", "(upgrade)");
                dsInsert.setString(-1, "createtool", "Diagnostics");
                dsInsert.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                dsInsert.setString(-1, "modby", "(upgrade)");
                dsInsert.setString(-1, "modtool", "Diagnostics");
                dsInsert.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                DataSetUtil.insert(this.database, dsInsert, "arraytype");
                returnMessage = "Successfully created " + dsInsert.size() + " ASL Array Types";
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return returnMessage;
    }
}

