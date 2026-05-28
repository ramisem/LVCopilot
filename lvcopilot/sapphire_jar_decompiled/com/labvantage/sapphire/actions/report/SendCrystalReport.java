/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.platform.Configuration;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SendCrystalReport
extends BaseReportAction
implements sapphire.action.SendCrystalReport {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String printer;
        String filename;
        String debuglog = properties.getProperty("debuglog").toLowerCase();
        boolean debug = debuglog.trim().length() == 0 ? Trace.on : debuglog.equals("true");
        properties.setProperty("debug", String.valueOf(debug));
        String reportid = properties.getProperty("reportid");
        if (reportid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Reportid not specified");
        }
        properties.setProperty("reportid", reportid);
        String dest = properties.getProperty("destination");
        if (dest.length() == 0 || !dest.equalsIgnoreCase("printer") && !dest.equalsIgnoreCase("email") && !dest.equalsIgnoreCase("file")) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process the crystal report '" + reportid + "': The destination has empty, null or not a valid string");
        }
        properties.setProperty("dest", dest);
        if (debug) {
            this.logger.info("The destination is:" + dest);
        }
        if ((filename = properties.getProperty("filename")).length() == 0 && dest.equalsIgnoreCase("file")) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process the crystal report '" + reportid + "': The filename cannot be empty or null if the destination is file.");
        }
        properties.setProperty("filename", filename);
        String synchronous = properties.getProperty("synchronous", "yes").equalsIgnoreCase("yes") ? "yes" : "no";
        properties.setProperty("synchronous", synchronous);
        if (dest.equalsIgnoreCase("printer") && (printer = this.getPrinter(properties.getProperty("addressid"), properties.getProperty("addresstype"))).length() > 0) {
            properties.setProperty("printer", printer);
        }
        if (dest.equalsIgnoreCase("email")) {
            properties.setProperty("domain", this.getSMTPHost());
        }
        String filetype = properties.getProperty("filetype", "pdf");
        properties.setProperty("filetype", filetype);
        try {
            StringBuffer queryString = new StringBuffer();
            queryString.append(this.buildQueryString(properties));
            Configuration config = Configuration.getInstance();
            this.processReportViaWebApp("http://" + config.getServerHostName() + ":" + config.getHttpPort() + "/sapphirecrystal/SendCrystalReport.jsp", queryString.substring(1));
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

