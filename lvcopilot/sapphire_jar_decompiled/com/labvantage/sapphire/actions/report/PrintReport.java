/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.actions.report.CrystalClearPrintReport;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class PrintReport
extends BaseReportAction
implements sapphire.action.PrintReport {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String printer;
        String reportid = properties.getProperty("reportid");
        if (reportid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "The method was called with an invalid report identifier.");
        }
        this.logger.info("Processing report: " + reportid + ", properties: " + properties.toString());
        boolean email = false;
        if (properties.getProperty("destination").equalsIgnoreCase("email")) {
            try {
                properties.setProperty("destination", "file");
                File temp = File.createTempFile("rep", "");
                properties.setProperty("tempfilename", temp.getPath());
                temp.deleteOnExit();
                temp.delete();
                email = true;
            }
            catch (IOException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (properties.getProperty("destination").equalsIgnoreCase("printer") && (printer = this.getPrinter(properties.getProperty("addressid"), properties.getProperty("addresstype"))).length() > 0) {
            properties.setProperty("printerurl", printer);
        }
        this.database.createPreparedResultSet("SELECT reporttypeflag, librarydir, objectname FROM report WHERE lower( reportid ) = ?", new Object[]{reportid.toLowerCase()});
        if (this.database.getNext()) {
            char reporttype = this.database.getString("reporttypeflag").charAt(0);
            switch (reporttype) {
                case 'I': {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not process the Informaker report '" + reportid + "'. Action not supported on this platform.");
                }
                case 'C': {
                    CrystalClearPrintReport pcr;
                    properties.setProperty("librarydir", this.database.getString("librarydir"));
                    if (!(properties.getProperty("destination").equalsIgnoreCase("printer") ? (pcr = new CrystalClearPrintReport()).printCyrstal(properties, this.connectionInfo) != 1 : properties.getProperty("destination").equalsIgnoreCase("file") && (pcr = new CrystalClearPrintReport()).exportCrystalReports(properties, this.connectionInfo) != 1)) break;
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not process the Crystal Report '" + reportid + "': " + properties.getProperty("ccerror"));
                }
                case 'B': {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not process the Businness Objects report '" + reportid + "'. Action not supported on this platform.");
                }
            }
            if (email) {
                PropertyList actionProps = new PropertyList();
                actionProps.put("from", properties.getProperty("emailfrom"));
                actionProps.put("to", properties.getProperty("emailtolist"));
                actionProps.put("cc", properties.getProperty("emailcclist"));
                actionProps.put("subject", properties.getProperty("emailsubject"));
                actionProps.put("message", properties.getProperty("emailmessage"));
                actionProps.put("filename", properties.getProperty("filename"));
                ActionService ac = new ActionService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                try {
                    ac.processAction("SendMail", "1", actionProps);
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to send generated report", e);
                }
            }
        } else {
            throw new SapphireException("INVALID_PROPERTY", "The reportid '" + reportid + "' is invalid.");
        }
    }
}

