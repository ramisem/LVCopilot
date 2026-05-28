/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.SaveFormat
 */
package com.labvantage.sapphire.actions.eln;

import com.aspose.words.SaveFormat;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.modules.eln.WordWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ExportWorksheet
extends BaseReportAction
implements sapphire.action.ExportWorksheet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid", "1");
        if (worksheetid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No worksheetid specified");
        }
        String destination = properties.getProperty("destination");
        if (destination.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No destination specified - requires file, email or printer");
        }
        String filetype = properties.getProperty("filetype", "PDF");
        PropertyList exportOptions = new PropertyList();
        exportOptions.setProperty("pagesize", properties.getProperty("pagesize"));
        exportOptions.setProperty("orientation", properties.getProperty("orientation"));
        exportOptions.setProperty("notes", properties.getProperty("shownotes"));
        exportOptions.setProperty("imagemarkup", properties.getProperty("showimagemarkup"));
        exportOptions.setProperty("metadata", properties.getProperty("showmetadata"));
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        WordWorksheet export = null;
        try {
            exportOptions.setProperty("exportto", filetype);
            export = new WordWorksheet(sapphireConnection, worksheetid, worksheetversionid, exportOptions);
            export.createDocument();
            if (destination.equals("file") || destination.equals("email") || destination.equals("sdi")) {
                PropertyList actionProps;
                byte[] documentByte;
                File tempDir;
                HashMap<String, String> transMap = new HashMap<String, String>();
                transMap.put("worksheetid", worksheetid);
                transMap.put("worksheetversionid", worksheetversionid);
                transMap.put("worksheetname", export.getWorksheet().getValue(0, "worksheetname"));
                String filename = properties.getProperty("filename", export.getExportFilename());
                filename = FileUtil.sanitizeStringForFilename(filename, "");
                String emailtolist = properties.getProperty("emailtolist");
                String emailcclist = properties.getProperty("emailcclist");
                String emailfrom = properties.getProperty("emailfrom");
                String emailsubject = properties.getProperty("emailsubject", this.getTranslationProcessor().translate("Emailing worksheet: [worksheetname]", transMap));
                String emailmessage = properties.getProperty("emailmessage", this.getTranslationProcessor().translate("This email contains a " + (filetype.equalsIgnoreCase("Word") ? "Word" : "PDF") + " file for worksheet \"[worksheetname]\" ([worksheetid]/[worksheetversionid])", transMap));
                String sdisdcid = properties.getProperty("sdisdcid");
                String sdikeyid1 = properties.getProperty("sdikeyid1");
                String sdikeyid2 = properties.getProperty("sdikeyid2");
                String sdikeyid3 = properties.getProperty("sdikeyid3");
                String sdidesc = properties.getProperty("sdiattachdesc", this.getTranslationProcessor().translate("Worksheet: [worksheetname]", transMap));
                String sdibyref = properties.getProperty("sdiattachbyref", "Y");
                if (destination.equals("file") && filename.length() == 0) {
                    throw new SapphireException("INVALID_PROPERTY", "No filename specified");
                }
                if (destination.equals("email")) {
                    if (emailtolist.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", "No emailto specified");
                    }
                    if (emailfrom.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", "No emailfrom specified");
                    }
                    tempDir = new File(System.getProperty("java.io.tmpdir"));
                    filename = new File(tempDir, worksheetid + "_" + worksheetversionid + "." + (filetype.equalsIgnoreCase("Word") ? "docx" : "pdf")).getAbsolutePath();
                }
                if (destination.equals("sdi")) {
                    if (sdisdcid.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", "No SDI SDCID specified");
                    }
                    if (sdikeyid1.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", "No SDI KEYID1 specified");
                    }
                    if (filename.length() == 0) {
                        if (sdibyref.equals("Y")) {
                            throw new SapphireException("INVALID_PROPERTY", "No SDI filename specified for ByRef attachment");
                        }
                        tempDir = new File(System.getProperty("java.io.tmpdir"));
                        filename = new File(tempDir, worksheetid + "_" + worksheetversionid + "." + (filetype.equalsIgnoreCase("Word") ? "docx" : "pdf")).getAbsolutePath();
                    }
                }
                filename = FileUtil.substituteConfigurationPaths(filename);
                File file = new File(filename);
                FileOutputStream fos = new FileOutputStream(file);
                if (filetype.equalsIgnoreCase("Word")) {
                    documentByte = export.streamToWord(fos);
                    String fileName = file.getName();
                    HashMap<String, Object> eventProps = this.populateEventProperties(SaveFormat.getName((int)20).toLowerCase(), fileName, documentByte);
                    String reporteventid = this.getReportEventid(worksheetid, worksheetversionid);
                    if (OpalUtil.isEmpty(reporteventid)) {
                        eventProps.put("eventType", "Publish");
                    } else {
                        eventProps.put("eventType", "RePublish");
                        eventProps.put("parentreporteventid", reporteventid);
                    }
                    reporteventid = this.performPublishEvent(export, eventProps);
                    this.logActivity(worksheetid, worksheetversionid, reporteventid, "Word");
                } else if (filetype.equalsIgnoreCase("PDF")) {
                    documentByte = export.streamToPdf(fos);
                    String fileName = file.getName();
                    HashMap<String, Object> eventProps = this.populateEventProperties(SaveFormat.getName((int)40).toLowerCase(), fileName, documentByte);
                    String reporteventid = this.getReportEventid(worksheetid, worksheetversionid);
                    if (OpalUtil.isEmpty(reporteventid)) {
                        eventProps.put("eventType", "Publish");
                    } else {
                        eventProps.put("eventType", "RePublish");
                        eventProps.put("parentreporteventid", reporteventid);
                    }
                    reporteventid = this.performPublishEvent(export, eventProps);
                    this.logActivity(worksheetid, worksheetversionid, reporteventid, "PDF");
                }
                fos.close();
                if (destination.equals("email")) {
                    actionProps = new PropertyList();
                    actionProps.put("from", emailfrom);
                    actionProps.put("to", emailtolist);
                    actionProps.put("cc", emailcclist);
                    actionProps.put("subject", emailsubject);
                    actionProps.put("message", emailmessage);
                    actionProps.put("filename", filename);
                    this.getActionProcessor().processAction("SendMail", "1", actionProps);
                    file.delete();
                } else if (destination.equals("sdi")) {
                    actionProps = new PropertyList();
                    actionProps.put("sdcid", sdisdcid);
                    actionProps.put("keyid1", sdikeyid1);
                    actionProps.put("keyid2", sdikeyid2);
                    actionProps.put("keyid3", sdikeyid3);
                    actionProps.put("description", sdidesc);
                    if (sdibyref.equals("Y")) {
                        actionProps.put("type", "R");
                    } else {
                        actionProps.put("type", "F");
                    }
                    actionProps.put("filename", filename);
                    this.getActionProcessor().processAction("AddSDIAttachment", "1", actionProps);
                    if (sdibyref.equals("N")) {
                        file.delete();
                    }
                }
            } else {
                String addressid = properties.getProperty("addressid");
                String addresstype = properties.getProperty("addresstype");
                String printername = this.getPrinter(addressid, addresstype);
                export.print(printername);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public String performPublishEvent(WordWorksheet wordWorksheet, HashMap<String, Object> eventProps) throws SapphireException {
        SapphireReportEvent event = wordWorksheet.getEventForWorkSheet(eventProps);
        String reporteventid = "";
        byte[] documentByte = (byte[])eventProps.get("documentByte");
        if (event != null) {
            event.setFilename(eventProps.get("filename").toString());
            event.setEventtype(eventProps.get("eventType").toString());
            event.saveEvent(documentByte, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            reporteventid = event.getReporteventid();
        }
        return reporteventid;
    }

    private HashMap<String, Object> populateEventProperties(String format, String filename, byte[] documentByte) {
        HashMap<String, Object> eventProps = new HashMap<String, Object>();
        eventProps.put("documentByte", documentByte);
        eventProps.put("documentFormat", format);
        eventProps.put("filename", filename);
        return eventProps;
    }

    public String getReportEventid(String worksheetid, String worksheetversionid) throws Exception {
        String reporteventitem = "";
        QueryProcessor qp = new QueryProcessor(this.getConnectionProcessor().getSapphireConnection().getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT reporteventid FROM reporteventitem WHERE ITEMSDCID='LV_Worksheet' and itemkeyid1= " + safeSQL.addVar(worksheetid) + " and itemkeyid2 = " + safeSQL.addVar(worksheetversionid) + " order by reporteventid";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            reporteventitem = ds.getString(0, "reporteventid");
        }
        return reporteventitem;
    }

    private void logActivity(String worksheetid, String worksheetversionid, String reporteventid, String exportto) throws ActionException {
        PropertyList activityProps = new PropertyList();
        activityProps.setProperty("worksheetid", worksheetid);
        activityProps.setProperty("worksheetversionid", worksheetversionid);
        activityProps.setProperty("reporteventid", reporteventid);
        activityProps.setProperty("activitytype", "Export");
        activityProps.setProperty("activitylog", "Exported worksheet to " + exportto);
        ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
        actionProcessor.processActionClass(AddWorksheetActivity.class.getName(), activityProps);
    }
}

