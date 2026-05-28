/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.services.ConnectionInfo;
import java.awt.Frame;
import sapphire.xml.PropertyList;

public class CrystalClearPrintReport
extends Frame {
    public String initialize(PropertyList propertylist, ConnectionInfo connectionInfo) {
        return "";
    }

    public int printCyrstal(PropertyList propertylist, ConnectionInfo connectionInfo) {
        int rc = 1;
        try {
            String reportURL = this.initialize(propertylist, connectionInfo);
            String ccerror = propertylist.getProperty("ccerror");
            rc = ccerror != null && ccerror.length() > 0 ? 2 : (propertylist.getProperty("printerurl").length() > 0 ? this.customPrinter(propertylist, reportURL) : this.defaultprinter(propertylist, null));
        }
        catch (Exception e) {
            propertylist.setProperty("ccerror", e.getMessage());
        }
        return rc;
    }

    public int exportCrystalReports(PropertyList propertylist, ConnectionInfo connectionInfo) {
        int rc = 1;
        return rc;
    }

    public int customPrinter(PropertyList propertylist, String reportURL) {
        int rc = 1;
        return rc;
    }

    public int defaultprinter(PropertyList propertylist, Object proxy) {
        int rc = 1;
        return rc;
    }
}

