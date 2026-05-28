/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.opaldatabase;

import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SMSDataEntry
extends BaseDiagnostic {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static List pageList = new ArrayList();

    public SMSDataEntry(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    public SMSDataEntry(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Fix the clinicalevent column in SMS Data Entry pages for R4 pages";
    }

    @Override
    public String getDescription() {
        return "Fixes the clinicalevent column in SMS Data Entry pages for R4 pages";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "Requires fix";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        Iterator iterator = pageList.iterator();
        while (iterator.hasNext()) {
            try {
                PropertyListCollection columns;
                boolean update = false;
                String webpageid = (String)iterator.next();
                PropertyList propertylist = PropertyTreeUtil.getWebPagePropertyTreePropertyList(this.database, webpageid, "SMSDataEntry", "pagedata", "R4");
                if (propertylist != null && (columns = propertylist.getCollection("tab1")) != null) {
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList column = columns.getPropertyList(i);
                        String sColName = column.getProperty("sColName");
                        if (!"clinicaleventid".equalsIgnoreCase(sColName)) continue;
                        column.setProperty("sColName", "clinicalevent");
                        column.setProperty("sColId", "clinicalevent");
                        update = true;
                    }
                }
                if (!update) continue;
                PropertyTreeUtil.setWebPagePropertyTreeValue(this.database, webpageid, "SMSDataEntry", "pagedata", "R4", propertylist.toXMLString());
            }
            catch (SapphireException sapphireException) {}
        }
        return "Fixed clinical event columns in SMS Data Entry pages";
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }

    static {
        pageList.add("LV_LabDataEntry");
        pageList.add("LV_SMSAVDataEntry");
        pageList.add("LV_SMSDataEntry");
        pageList.add("LV_SMSDataEntrySing");
    }
}

