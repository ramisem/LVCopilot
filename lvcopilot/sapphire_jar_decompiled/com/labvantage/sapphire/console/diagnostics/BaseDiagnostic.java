/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics;

import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import java.util.ArrayList;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public abstract class BaseDiagnostic {
    public DBAccess database;
    public ConnectionInfo connectionInfo;
    public String webappid;

    public BaseDiagnostic(DBAccess database, ConnectionInfo conenctionInfo) {
        this.database = database;
        this.connectionInfo = conenctionInfo;
    }

    public BaseDiagnostic(String webappid, ConnectionInfo connectionInfo) {
        this.webappid = webappid;
        this.connectionInfo = connectionInfo;
    }

    public String getId() {
        String classname = this.getClass().getName();
        return classname.substring(classname.lastIndexOf(".") + 1);
    }

    public abstract String getTitle();

    public abstract String getDescription();

    public ArrayList getRunProperties() {
        return new ArrayList();
    }

    public abstract String runDiagnostic(PropertyList var1) throws DiagnosticException;

    public abstract boolean canBeRepaired();

    public boolean canAutoRepair() {
        return false;
    }

    public ArrayList getRepairProperties() {
        return new ArrayList();
    }

    public String runRepair(PropertyList properties) throws DiagnosticException {
        throw new DiagnosticException(2, "Repair method not implemented.");
    }
}

