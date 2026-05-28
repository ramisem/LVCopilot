/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.MigratePre85Properties;
import com.labvantage.sapphire.console.diagnostics.UpdateMaintenanceFormPropertyTree;
import com.labvantage.sapphire.console.diagnostics.database.MigrateScheduleExcludeToCalendar;
import com.labvantage.sapphire.console.diagnostics.database.MigrateTabsMenusToGizmo;
import com.labvantage.sapphire.console.diagnostics.database.ResequencePropertyTree;
import com.labvantage.sapphire.console.diagnostics.opaldatabase.BioBankSQLServerUpdate;
import com.labvantage.sapphire.console.diagnostics.opaldatabase.SMSDataEntry;
import com.labvantage.sapphire.console.diagnostics.storageunit.CreateASLArrayTypes;
import com.labvantage.sapphire.console.diagnostics.storageunit.ResetProductStorageUnitType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;

public class DiagnosticsUtils {
    public static final String GENERAL = "General";
    private static HashMap databaseCach = new HashMap();

    public static void reset() {
        Set keyset = databaseCach.keySet();
        for (String databaseid : keyset) {
            if (databaseCach.get(databaseid) == null) continue;
            ((DBUtil)databaseCach.get(databaseid)).reset();
        }
    }

    public static ArrayList getUpgradeDiagnostics(DBAccess database, ConnectionInfo connectionInfo, String category) {
        ArrayList<ResequencePropertyTree> diagnosticsList = new ArrayList<ResequencePropertyTree>();
        diagnosticsList.add(new ResequencePropertyTree(database, connectionInfo));
        return diagnosticsList;
    }

    public static ArrayList getDatabaseDiagnostics(DBAccess database, ConnectionInfo connectionInfo, String category) {
        ArrayList<BaseDiagnostic> diagnosticsList = new ArrayList<BaseDiagnostic>();
        if (category.equals(GENERAL)) {
            diagnosticsList.add(new BioBankSQLServerUpdate(database, connectionInfo));
            diagnosticsList.add(new SMSDataEntry(database, connectionInfo));
        }
        return diagnosticsList;
    }

    public static ArrayList getPostUpgradeDiagnostics(DBAccess database, ConnectionInfo connectionInfo, String category, String labvantagehome) {
        ArrayList<BaseDiagnostic> diagnosticsList = new ArrayList<BaseDiagnostic>();
        diagnosticsList.add(new UpdateMaintenanceFormPropertyTree(database, connectionInfo));
        diagnosticsList.add(new MigrateTabsMenusToGizmo(database, connectionInfo));
        diagnosticsList.add(new MigrateScheduleExcludeToCalendar(database, connectionInfo));
        diagnosticsList.add(new ResetProductStorageUnitType(database, connectionInfo));
        diagnosticsList.add(new CreateASLArrayTypes(database, connectionInfo));
        diagnosticsList.add(new MigratePre85Properties(database, connectionInfo, labvantagehome));
        return diagnosticsList;
    }
}

