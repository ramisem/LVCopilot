/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.JasperReport
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReport;
import sapphire.SapphireException;

public class JasperReportPropertyHandler
extends PropertyHandler {
    public static final String JASPER_REPORT_KEY = "jasperreport";
    public static final String PDFBYTES_KEY = "pdfbytes";
    public static final String BYTES_KEY = "reportbytes";
    public static final String SIGN_BYTES = "signbytes";
    public static final String SIGN_REPORT = "signreport";
    public static final String JASPER_PRINT_KEY = "jasperprint";
    public static final String VIEW_REPORT_EVENT = "viewreportevent";
    public static final String STORED_PRINT = "storedreportprint";
    public static final String DISPLAY_TYPE = "displayType";
    public static final String REQUEST = "request";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String mode = (String)props.get("mode");
        Connection conn = this.sapphireConnection.getConnection();
        JasperReport jasperReport = (JasperReport)props.get(JASPER_REPORT_KEY);
        try {
            if (BYTES_KEY.equals(mode)) {
                props.put(BYTES_KEY, SapphireJasperUtil.getReportBytes(jasperReport, (Map)props, conn));
            } else if (SIGN_BYTES.equals(mode)) {
                byte[] bytes = (byte[])props.get(SIGN_BYTES);
                SapphireReport report = (SapphireReport)props.get(SIGN_REPORT);
                report.setSapphireConnection(this.sapphireConnection);
                props.put(SIGN_BYTES, report.signReport(bytes));
            } else if (JASPER_PRINT_KEY.equals(mode)) {
                props.put(JASPER_PRINT_KEY, SapphireJasperUtil.getJasperPrint(jasperReport, (Map)props, conn));
            } else if (VIEW_REPORT_EVENT.equals(mode)) {
                SapphireReportEvent event = (SapphireReportEvent)props.get(VIEW_REPORT_EVENT);
                event.saveEvent(this.sapphireConnection);
            } else if (STORED_PRINT.equals(mode)) {
                SapphireReportEvent event = new SapphireReportEvent((String)props.get("reporteventid"), "Report", this.sapphireConnection);
                props.put(STORED_PRINT, event);
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

