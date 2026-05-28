/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SendBOReport
extends BaseReportAction
implements sapphire.action.SendBOReport {
    public static final String ID = "SendBOReport";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block23: {
            String reportid = properties.getProperty("reportid");
            String reportversionid = properties.getProperty("reportversionid", "");
            if (reportid.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", "Reportid not specified");
            }
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT reporttypeflag FROM report ");
            sql.append(" WHERE reportid=").append(safeSQL.addVar(reportid));
            if (reportversionid.length() > 0 && !"C".equals(reportversionid)) {
                sql.append(" AND reportversionid=").append(safeSQL.addVar(reportversionid));
            } else {
                sql.append(" AND ( versionstatus='C' or versionstatus='P' ) ");
            }
            String reportflag = "";
            try {
                this.database.createPreparedResultSet("GetReportFlag", sql.toString(), safeSQL.getValues());
                if (this.database.getNext("GetReportFlag")) {
                    reportflag = this.database.getString("GetReportFlag", "reporttypeflag");
                }
            }
            catch (SapphireException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not get the report flag '" + reportid + "': " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            finally {
                this.database.closeResultSet("GetReportFlag");
            }
            if (reportflag == null || reportflag.trim().length() == 0 || reportflag.equalsIgnoreCase("B")) {
                properties.setProperty("reportid", reportid);
                String destination = properties.getProperty("destination");
                if (destination.length() == 0) {
                    destination = "printer";
                    properties.setProperty("destination", destination);
                }
                if (destination.equalsIgnoreCase("file") && properties.getProperty("filename").length() == 0) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not process the BO report '" + reportid + "': The filename property is empty when the destination is file");
                }
                if (!(destination.equalsIgnoreCase("printer") || destination.equalsIgnoreCase("email") || destination.equalsIgnoreCase("file"))) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not process the BO report '" + reportid + "': Incorrect destination specified");
                }
                String debuglog = properties.getProperty("debuglog").toLowerCase();
                boolean debug = debuglog.length() == 0 ? Trace.on : debuglog.equals("true");
                properties.setProperty("debug", String.valueOf(debug));
                try {
                    ConfigService configService = new ConfigService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    String bousername = configService.getProfileProperty(this.connectionInfo.getSysuserId(), "bousername");
                    String bopassword = configService.getProfileProperty(this.connectionInfo.getSysuserId(), "bopassword");
                    String bodocumentdomain = configService.getProfileProperty(this.connectionInfo.getSysuserId(), "bodocumentdomain");
                    String boexchangemode = configService.getProfileProperty(this.connectionInfo.getSysuserId(), "boexchangemode");
                    String bourl = configService.getProfileProperty(this.connectionInfo.getSysuserId(), "bourl");
                    if (destination.equalsIgnoreCase("printer")) {
                        String printer = this.getPrinter(properties.getProperty("addressid"), properties.getProperty("addresstype"));
                        if (printer.length() > 0) {
                            properties.setProperty("printerurl", printer);
                        }
                        throw new SapphireException("PROCESSACTION_FAILED", "Could not send the Business Objects Report '" + reportid + "' to destination: '" + printer + "'. Action not supported on this platform.");
                    }
                    if (destination.equalsIgnoreCase("email")) {
                        properties.setProperty("mailhost", this.getSMTPHost());
                    }
                    try {
                        StringBuffer queryString = new StringBuffer();
                        queryString.append("?bousername=").append(HttpUtil.encodeURIComponent(bousername)).append("&bouserpassword=").append(EncryptDecrypt.decrypt(HttpUtil.encodeURIComponent(bopassword)));
                        queryString.append("&boexchangemode=").append(boexchangemode).append("&bodocumentdomain=").append(bodocumentdomain);
                        queryString.append(this.buildQueryString(properties));
                        this.processReportViaWebApp(bourl + "/sapphirebo/SendBOReport.jsp", queryString.substring(1));
                        break block23;
                    }
                    catch (Exception e) {
                        throw new SapphireException("PROCESSACTION_FAILED", "Could not send the report '" + reportid + "': " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                }
                catch (ServiceException e) {
                    throw new SapphireException("CONFIGURATION_ERROR", "Failed to get BO configuration properties", e);
                }
            }
            ActionProcessor ap = this.getActionProcessor();
            try {
                ap.processAction("GenerateReport", VERSION, properties);
            }
            catch (SapphireException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Failed to process GenerateReport action", e);
            }
        }
    }
}

