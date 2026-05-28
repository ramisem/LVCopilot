/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRRuntimeException
 */
package com.labvantage.sapphire.report.nwa;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.report.SapphireReportEvent;
import java.sql.Connection;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRRuntimeException;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class NWAReportPropertyHandler
extends PropertyHandler {
    public static final String CHARTBYTES_KEY = "chartbytes";
    public static final String NWA_PRINT_KEY = "nwaprint";
    public static final String VIEW_REPORT_EVENT = "viewreportevent";
    public static final String STORED_PRINT = "storedreportprint";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String mode = (String)props.get("mode");
        PropertyList stats = (PropertyList)props.get("stats");
        Connection conn = this.sapphireConnection.getConnection();
        try {
            if (VIEW_REPORT_EVENT.equals(mode)) {
                SapphireReportEvent event = (SapphireReportEvent)props.get(VIEW_REPORT_EVENT);
                event.saveEvent(this.sapphireConnection);
                if (stats.size() > 0) {
                    event.editStats(stats, this.sapphireConnection);
                }
            }
        }
        catch (JRRuntimeException jre) {
            this.logError("Error processing jasper report request:<br/>", jre);
            props.put("errormessage", jre.getCause().getMessage() + "<br/>" + jre.getCause().getCause().getMessage());
        }
        catch (Exception e) {
            this.logError("Error processing jasper report request\n", e);
            props.put("errormessage", e.getMessage());
        }
    }
}

