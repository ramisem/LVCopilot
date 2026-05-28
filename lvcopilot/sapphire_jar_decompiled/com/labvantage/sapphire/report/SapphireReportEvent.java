/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JasperPrint
 */
package com.labvantage.sapphire.report;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.WordWorksheet;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.bo.SapphireBOReport;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import com.labvantage.sapphire.report.jasper.JasperReportPropertyHandler;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.report.jasper.SapphireJavaTalendReport;
import com.labvantage.sapphire.report.nwa.NWAReportPropertyHandler;
import com.labvantage.sapphire.report.nwa.SapphireNWAReport;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SapphireReportEvent
implements Serializable {
    public static final String TYPE_VIEW = "View";
    public static final String TYPE_PRINT = "Print";
    public static final String TYPE_EMAIL = "Email";
    public static final String TYPE_EXPORT = "Export";
    public static final String TYPE_REPRINT = "Reprint";
    public static final String ReportEvent_CLASS = "ReportEvent";
    public static final String SDCID = "ReportEvent";
    private byte[] reportByte = null;
    private transient Object reportObject = null;
    private transient ByteArrayInputStream reportStream = null;
    private HashMap paramMap = null;
    private String reporteventid;
    private String priorReportEventID;
    private int reporteventversionid;
    private String reportid;
    private String reportversionid;
    private String reporttypeflag;
    private String displaytype;
    private String eventtype;
    private boolean exportUsingGenerateReportAction = false;
    private String parentreporteventid;
    private String addressid = "";
    private String addresstype = "";
    private String emailfrom = "";
    private String emailto = "";
    private String emailcc = "";
    private String emailsubject = "";
    private String emailmessage = "";
    private String filename = "";
    private String digitallysignedflag = "";
    private String languageid;
    private String timezone;
    private String worksheetid;
    private String worksheetversionid;
    private sapphire.attachment.Attachment attachment;
    private transient SapphireReport sapphireReport;
    private transient WordWorksheet wordWorksheet;
    private boolean saveEvent = true;
    private String auditReason;
    private String auditActivity;
    private String auditSignedFlag;

    public String getReporttypeflag() {
        return this.reporttypeflag;
    }

    public void setReporttypeflag(String reporttypeflag) {
        this.reporttypeflag = reporttypeflag;
    }

    public String getAddressid() {
        return this.addressid;
    }

    public void setAddressid(String addressid) {
        this.addressid = addressid;
    }

    public String getAddresstype() {
        return this.addresstype;
    }

    public void setAddresstype(String addresstype) {
        this.addresstype = addresstype;
    }

    public String getEmailfrom() {
        return this.emailfrom;
    }

    public void setEmailfrom(String emailfrom) {
        this.emailfrom = emailfrom;
    }

    public String getEmailcc() {
        return this.emailcc;
    }

    public void setEmailcc(String emailcc) {
        this.emailcc = emailcc;
    }

    public String getEmailsubject() {
        return this.emailsubject;
    }

    public void setEmailsubject(String emailsubject) {
        this.emailsubject = emailsubject;
    }

    public String getEmailmessage() {
        return this.emailmessage;
    }

    public void setEmailmessage(String emailmessage) {
        this.emailmessage = emailmessage;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getReporteventid() {
        return this.reporteventid;
    }

    public int getReporteventversionid() {
        return this.reporteventversionid;
    }

    public String getPriorReportEventID() {
        return this.priorReportEventID;
    }

    public String getEventtype() {
        return this.eventtype;
    }

    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    public boolean isExportUsingGenerateReportAction() {
        return this.exportUsingGenerateReportAction;
    }

    public void setExportUsingGenerateReportAction(boolean exportUsingGenerateReportAction) {
        this.exportUsingGenerateReportAction = exportUsingGenerateReportAction;
    }

    public HashMap getParamMap() {
        return this.paramMap;
    }

    public void setParamMap(HashMap paramMap) {
        this.paramMap = paramMap;
    }

    public byte[] getReportByte() {
        return this.reportByte;
    }

    public void setReportByte(byte[] reportByte) {
        this.reportByte = reportByte;
    }

    public Object getReportObject() {
        return this.reportObject;
    }

    public void setReportObject(Object reportObject) {
        this.reportObject = reportObject;
    }

    public ByteArrayInputStream getReportStream() {
        return this.reportStream;
    }

    public void setReportStream(ByteArrayInputStream reportStream) {
        this.reportStream = reportStream;
    }

    public String getDisplaytype() {
        return this.displaytype;
    }

    public void setDisplaytype(String displaytype) {
        this.displaytype = displaytype;
    }

    public String getEmailto() {
        return this.emailto;
    }

    public void setEmailto(String emailto) {
        this.emailto = emailto;
    }

    public String getLanguageid() {
        return this.languageid;
    }

    public void setLanguageid(String languageid) {
        this.languageid = languageid;
    }

    public sapphire.attachment.Attachment getAttachment() {
        return this.attachment;
    }

    public void setAttachment(sapphire.attachment.Attachment attachment) {
        this.attachment = attachment;
    }

    public String getWorksheetid() {
        return this.worksheetid;
    }

    public void setWorksheetid(String worksheetid) {
        this.worksheetid = worksheetid;
    }

    public String getWorksheetversionid() {
        return this.worksheetversionid;
    }

    public void setWorksheetversionid(String worksheetversionid) {
        this.worksheetversionid = worksheetversionid;
    }

    public String isDigitallysigned() {
        return this.digitallysignedflag;
    }

    public void setDigitallysigned(String digitallysignedflag) {
        this.digitallysignedflag = digitallysignedflag;
    }

    public String getAuditReason() {
        return this.auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public String getAuditActivity() {
        return this.auditActivity;
    }

    public void setAuditActivity(String auditActivity) {
        this.auditActivity = auditActivity;
    }

    public String getAuditSignedFlag() {
        return this.auditSignedFlag;
    }

    public void setAuditSignedFlag(String auditSignedFlag) {
        this.auditSignedFlag = auditSignedFlag;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public SapphireReportEvent(SapphireReport sapphireReport) {
        this.sapphireReport = sapphireReport;
        this.reportid = sapphireReport.reportid;
        this.reportversionid = sapphireReport.reportversionid;
        this.reporttypeflag = sapphireReport.getReporttypeflag();
        SequenceProcessor seqProcessor = new SequenceProcessor(sapphireReport.connectionInfo.getConnectionId());
        int seq = seqProcessor.getSequence("ReportEvent", "reporteventid", 1000000, 1);
        this.reporteventid = "" + seq + "-1";
        this.reporteventversionid = 1;
        this.inititateAttachment();
    }

    public SapphireReportEvent(WordWorksheet wordWorksheet) {
        this.wordWorksheet = wordWorksheet;
        this.worksheetid = wordWorksheet.getWorksheetid();
        this.worksheetversionid = wordWorksheet.getWorksheetversionid();
        SequenceProcessor seqProcessor = new SequenceProcessor(wordWorksheet.getConnectionid());
        int seq = seqProcessor.getSequence("ReportEvent", "reporteventid", 1000000, 1);
        this.reporteventid = "" + seq + "-1";
        this.reporteventversionid = 1;
        this.inititateAttachment();
    }

    private void inititateAttachment() {
        this.attachment = sapphire.attachment.Attachment.getAttachment("ReportEvent", this.reporteventid, null, null);
        this.attachment.setAttachmentClass("ReportEvent");
        this.attachment.setAttachmentType(Attachment.AttachmentType.FILE);
    }

    public SapphireReportEvent(SapphireReport sapphireReport, HashMap paramMap, SapphireConnection sapphireConnection) throws SapphireException {
        this.sapphireReport = sapphireReport;
        this.reportid = sapphireReport.reportid;
        this.reportversionid = sapphireReport.reportversionid;
        this.reporttypeflag = sapphireReport.getReporttypeflag();
        this.createEvent(paramMap, sapphireConnection);
        this.inititateAttachment();
    }

    public SapphireReportEvent(WordWorksheet wordWorksheet, HashMap paramMap, SapphireConnection sapphireConnection) throws SapphireException {
        this.wordWorksheet = wordWorksheet;
        this.worksheetid = wordWorksheet.getWorksheetid();
        this.worksheetversionid = wordWorksheet.getWorksheetversionid();
        this.createEvent(paramMap, sapphireConnection);
        this.inititateAttachment();
    }

    private void createEvent(HashMap paramMap, SapphireConnection sapphireConnection) throws SapphireException {
        if (paramMap.get("createNewVersion") != null && paramMap.get("createNewVersion").equals("Y")) {
            String reportEventID = (String)paramMap.get("reporteventid");
            if ((reportEventID = SapphireReportEvent.getLatestReportEventId(reportEventID, sapphireConnection)).contains("-")) {
                this.reporteventversionid = Integer.parseInt(reportEventID.substring(reportEventID.indexOf(45) + 1)) + 1;
                this.reporteventid = reportEventID.substring(0, reportEventID.indexOf(45) + 1) + this.reporteventversionid;
                this.parentreporteventid = this.priorReportEventID = reportEventID.substring(0, reportEventID.indexOf(45) + 1) + (this.reporteventversionid - 1);
            } else {
                this.reporteventversionid = 1;
                this.reporteventid = reportEventID + "-" + this.reporteventversionid;
                this.parentreporteventid = this.priorReportEventID = reportEventID;
            }
        } else if (paramMap.get("regenerate") != null && paramMap.get("regenerate").equals("Y") || paramMap.get("eventType") != null && paramMap.get("eventType").equals("RePublish")) {
            String selectedReportEventId = (String)paramMap.get("parentreporteventid");
            String lastReportEventId = SapphireReportEvent.getLatestReportEventId(selectedReportEventId, sapphireConnection);
            if (lastReportEventId.contains("-")) {
                this.parentreporteventid = selectedReportEventId.substring(0, selectedReportEventId.indexOf("-") + 1) + 1;
                this.reporteventversionid = Integer.parseInt(lastReportEventId.substring(lastReportEventId.indexOf(45) + 1)) + 1;
                this.reporteventid = lastReportEventId.substring(0, lastReportEventId.indexOf(45) + 1) + this.reporteventversionid;
                this.priorReportEventID = selectedReportEventId;
            } else {
                this.parentreporteventid = lastReportEventId;
                this.reporteventversionid = 1;
                this.reporteventid = lastReportEventId + "-" + this.reporteventversionid;
                this.priorReportEventID = lastReportEventId;
            }
        }
    }

    public SapphireReportEvent(String reporteventid, String sdcid, SapphireConnection sapphireConnection) throws SapphireException {
        switch (sdcid) {
            case "Report": {
                this.getEventForReport(reporteventid, sapphireConnection);
                break;
            }
            case "LV_Worksheet": {
                this.getEventForWorksheet(reporteventid, sdcid, sapphireConnection);
                break;
            }
        }
    }

    public void getEventForReport(String reporteventid, SapphireConnection sapphireConnection) throws SapphireException {
        Trace.logInfo("Getting ReportEvent:" + reporteventid);
        DBUtil db = new DBUtil();
        db.setConnection(sapphireConnection);
        db.createPreparedResultSet("SELECT report.reportid, report.reportversionid, report.reporttypeflag, report.genreprinteventflag,reportevent.reportcontent, reportevent.displaytype, reportevent.eventtype, reportevent.emailfrom, reportevent.emailto, reportevent.emailcc, reportevent.emailsubject, reportevent.emailmessage, reportevent.filename, reportevent.addressid, reportevent.addresstype, reportevent.filename, reportevent.digitallysignedflag,reportevent.languageid, reportevent.timezone FROM reportevent, report WHERE report.reportid=reportevent.reportid AND report.reportversionid=reportevent.reportversionid AND reporteventid = ?", new Object[]{reporteventid});
        Blob reportprint = null;
        if (db.getNext()) {
            reportprint = db.getBlob("reportcontent");
            this.parentreporteventid = reporteventid;
            this.reportid = db.getString("reportid");
            this.reportversionid = "" + db.getInt("reportversionid");
            this.reporttypeflag = db.getString("reporttypeflag");
            this.eventtype = db.getString("eventtype");
            String filename = db.getString("filename");
            this.exportUsingGenerateReportAction = filename != null && filename.length() > 0;
            this.saveEvent = !"N".equals(db.getString("genreprinteventflag"));
            this.addressid = db.getString("addressid");
            this.addresstype = db.getString("addresstype");
            this.filename = db.getString("filename");
            this.emailto = db.getString("emailto");
            this.emailfrom = db.getString("emailfrom");
            this.emailcc = db.getString("emailcc");
            this.emailsubject = db.getString("emailsubject");
            this.emailmessage = db.getString("emailmessage");
            this.digitallysignedflag = db.getString("digitallysignedflag");
            this.languageid = db.getString("languageid");
            this.timezone = db.getString("timezone");
            SequenceProcessor sp = new SequenceProcessor(sapphireConnection.getConnectionId());
            try {
                this.reporteventid = "" + sp.getSequence("ReportEvent", "reporteventid", 1000000, 1);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            this.setDisplaytype(db.getString("displaytype"));
            try {
                if (reportprint != null) {
                    if (this.displaytype.equalsIgnoreCase("pdf") && (TYPE_VIEW.equals(this.eventtype) || TYPE_EXPORT.equals(this.eventtype))) {
                        int blobLength = (int)reportprint.length();
                        this.setReportByte(reportprint.getBytes(1L, blobLength));
                    } else {
                        InputStream in = reportprint.getBinaryStream();
                        ObjectInputStream objIn = new ObjectInputStream(in);
                        Object printObj = objIn.readObject();
                        this.setReportObject(printObj);
                    }
                }
                this.setReportByte(this.getStoredReportBytesFromAttachment(reporteventid, sapphireConnection.getConnectionId()));
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Could not retrieve the specified attachment.", e);
            }
            finally {
                db.reset();
            }
        }
        db.reset();
        throw new SapphireException("Report Event not found. You may not have access to the report.");
    }

    public void getEventForWorksheet(String reporteventid, String sdcid, SapphireConnection sapphireConnection) throws SapphireException {
        Trace.logInfo("Getting ReportEvent:" + reporteventid);
        DBUtil db = new DBUtil();
        try {
            SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(sapphireConnection.getDatabaseId());
            DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
            db.setConnection(database.getDbms(), dataSource.getConnection());
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to set sapphire connection.");
        }
        db.createPreparedResultSet("SELECT reporteventitem.itemkeyid1, reporteventitem.itemkeyid2, reportevent.displaytype, reportevent.eventtype, reportevent.emailfrom, reportevent.emailto, reportevent.emailcc, reportevent.emailsubject, reportevent.emailmessage, reportevent.filename, reportevent.addressid, reportevent.addresstype,reportevent.filename FROM reportevent, reporteventitem WHERE reportevent.reporteventid = reporteventitem.reporteventid AND reporteventitem.itemsdcid=? AND reporteventitem.reporteventid = ?", new Object[]{sdcid, reporteventid});
        if (!db.getNext()) {
            throw new SapphireException("Worksheet publish event not found. You may not have access to the report.");
        }
        this.parentreporteventid = reporteventid;
        this.worksheetid = db.getString("itemkeyid1");
        this.worksheetversionid = "" + db.getInt("itemkeyid2");
        this.eventtype = db.getString("eventtype");
        this.filename = db.getString("filename");
        this.displaytype = db.getString("displaytype");
        this.getStoredDocumentBytesFromAttachment(reporteventid, sapphireConnection.getConnectionId());
    }

    public byte[] getStoredReportBytes() {
        return this.reportByte;
    }

    public Object getStoredReportObject() {
        return this.reportObject;
    }

    public void saveEvent(byte[] bytes, ConnectionInfo connectionInfo) throws SapphireException {
        if (this.saveEvent) {
            this.setReportByte(bytes);
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("mode", "viewreportevent");
            props.put("viewreportevent", this);
            HashMap hashMap = new RequestProcessor(connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), props);
        }
    }

    public void saveEvent(Object jasperPrint, ConnectionInfo connectionInfo) throws SapphireException {
        if (this.saveEvent) {
            this.setReportObject(jasperPrint);
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("mode", "viewreportevent");
            props.put("viewreportevent", this);
            HashMap hashMap = new RequestProcessor(connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), props);
        }
    }

    public void saveEvent(ConnectionInfo connectionInfo, ByteArrayInputStream byteArrayInputStream) throws SapphireException {
        if (this.saveEvent) {
            this.setReportStream(byteArrayInputStream);
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("mode", "viewreportevent");
            props.put("viewreportevent", this);
            HashMap hashMap = new RequestProcessor(connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), props);
        }
    }

    public void saveEvent(byte[] chartBytes, ConnectionInfo connectionInfo, PropertyList nwaStats) throws SapphireException {
        if (this.saveEvent) {
            this.setReportByte(chartBytes);
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("mode", "viewreportevent");
            props.put("viewreportevent", this);
            props.put("stats", nwaStats);
            HashMap hashMap = new RequestProcessor(connectionInfo.getConnectionId()).processRequest(NWAReportPropertyHandler.class.getName(), props);
        }
    }

    public String saveEvent(SapphireConnection sapphireConnection) throws SapphireException {
        if (this.saveEvent) {
            String[] tokens;
            DBUtil db = new DBUtil();
            db.setConnection(sapphireConnection);
            ActionService as = new ActionService(sapphireConnection);
            PropertyList props = new PropertyList();
            props.put("sdcid", "ReportEvent");
            props.put("reportid", this.reportid);
            props.put("reportversionid", this.reportversionid);
            String reporteventdescrule = "";
            if (this.sapphireReport != null) {
                reporteventdescrule = this.sapphireReport.reporteventdescrule;
            }
            if (reporteventdescrule != null && reporteventdescrule.length() > 0 && (tokens = StringUtil.getTokens(reporteventdescrule)) != null && tokens.length > 0) {
                for (int i = 0; i < tokens.length; ++i) {
                    if (this.paramMap.get(tokens[i]) == null) continue;
                    reporteventdescrule = StringUtil.replaceAll(reporteventdescrule, "[" + tokens[i] + "]", (String)this.paramMap.get(tokens[i]));
                }
            }
            String regenerate = "N";
            if (this.paramMap != null && this.paramMap.containsKey("regenerate")) {
                regenerate = (String)this.paramMap.get("regenerate");
            }
            if (this.paramMap != null && this.paramMap.containsKey("eventType")) {
                regenerate = this.paramMap.get("eventType").toString().equalsIgnoreCase("RePublish") ? "Y" : "N";
            }
            String createNewVersion = "N";
            if (this.paramMap != null && this.paramMap.containsKey("createNewVersion")) {
                createNewVersion = (String)this.paramMap.get("createNewVersion");
            }
            if ("Y".equalsIgnoreCase(regenerate) || "Y".equalsIgnoreCase(createNewVersion)) {
                if (!"Y".equalsIgnoreCase(createNewVersion)) {
                    this.parentreporteventid = (String)this.paramMap.get("parentreporteventid");
                }
                props.put("parentreporteventid", this.parentreporteventid);
                props.put("displaytype", this.displaytype);
                props.put("eventtype", this.eventtype);
                props.put("languageid", this.languageid);
                props.put("timezone", this.timezone);
            } else if (this.parentreporteventid != null) {
                props.put("eventtype", TYPE_REPRINT);
            } else {
                props.put("displaytype", this.displaytype);
                props.put("eventtype", this.eventtype);
                props.put("languageid", this.languageid);
                props.put("timezone", this.timezone);
            }
            if (this.sapphireReport != null) {
                props.put("reporteventdesc", reporteventdescrule != null && reporteventdescrule.length() > 0 ? reporteventdescrule : this.reportid + "(ver " + this.reportversionid + ")");
            }
            if (this.wordWorksheet != null) {
                props.put("reporteventdesc", this.worksheetid + "(ver " + this.worksheetversionid + ")");
            }
            props.put("keyid1", this.reporteventid);
            if (this.sapphireReport != null && this.sapphireReport.initialdisposition != null && this.sapphireReport.initialdisposition.length() > 0) {
                props.put("disposition", this.sapphireReport.initialdisposition);
            }
            if (this.sapphireReport != null && OpalUtil.isNotEmpty(this.sapphireReport.initialdisposition)) {
                if (this.sapphireReport.initialdisposition.equalsIgnoreCase("Confirmed")) {
                    props.put("reviewedby", sapphireConnection.getSysuserId());
                    props.put("revieweddt", "NOW");
                }
            } else if (this.wordWorksheet != null) {
                props.put("eventtype", "RePublish");
            } else {
                props.put("disposition", "Pending");
            }
            props.put("parenteventid", this.parentreporteventid);
            props.put("overrideautokey", "Y");
            props.put("addressid", this.addressid);
            props.put("addresstype", this.addresstype);
            props.put("emailto", this.emailto);
            props.put("emailfrom", this.emailfrom);
            props.put("emailcc", this.emailcc);
            props.put("emailsubject", this.emailsubject);
            props.put("emailmessage", this.emailmessage);
            props.put("filename", this.filename);
            props.put("digitallysignedflag", this.digitallysignedflag);
            props.put("languageid", this.languageid);
            props.put("timezone", this.timezone);
            if (this.sapphireReport != null) {
                props.put("auditreason", this.getAuditReason());
                props.put("auditactivity", this.getAuditActivity());
                props.put("auditsignedflag", this.getAuditSignedFlag());
            }
            try {
                as.processAction("AddSDI", "1", props);
                this.reporteventid = props.getProperty("newkeyid1");
                if (this.parentreporteventid != null && this.parentreporteventid.length() > 0 && !props.getProperty("eventtype", "").equalsIgnoreCase(TYPE_REPRINT)) {
                    props = new PropertyList();
                    props.setProperty("sdcid", "ReportEvent");
                    props.setProperty("keyid1", this.parentreporteventid);
                    props.setProperty("disposition", "Replaced");
                    as.processAction("EditSDI", "1", props);
                }
                if (this.parentreporteventid == null || "Y".equalsIgnoreCase(regenerate) || "Y".equalsIgnoreCase(createNewVersion)) {
                    if (this.sapphireReport != null) {
                        this.saveReportEventItem(db, !"N".equals(this.sapphireReport.gensdilogflag), this.sapphireReport.sdcidvalue, this.sapphireReport.keyid1value, this.sapphireReport.keyid2value, this.sapphireReport.keyid3value);
                        this.saveReportEventParam(db, as, sapphireConnection);
                    }
                    if (this.wordWorksheet != null) {
                        this.saveReportEventItem(db, true, "LV_Worksheet", this.wordWorksheet.getWorksheetid(), this.wordWorksheet.getWorksheetversionid(), "");
                        if (this.reportStream == null && this.getReportByte() != null && this.getReportByte().length > 0) {
                            this.setReportStream(new ByteArrayInputStream(this.getReportByte()));
                        }
                    }
                    this.handleAttachment(sapphireConnection.getConnectionId(), this.getReportStream());
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            return this.reporteventid;
        }
        return "";
    }

    public void handleAttachment(String connectionid, ByteArrayInputStream reportContent) throws SapphireException {
        this.attachment.setInputStream(reportContent);
        AttachmentProcessor ap = new AttachmentProcessor(connectionid);
        ap.addSDIAttachment(this.attachment, false, false, "Sapphire Custom");
    }

    private void saveReportEventParam(DBUtil db, ActionService as, SapphireConnection sapphireConnection) throws ServiceException, SapphireException {
        StringBuffer paramidStr = new StringBuffer();
        StringBuffer paramvalueStr = new StringBuffer();
        String paramkeyid1valueStr = "";
        String paramkeyid2valueStr = "";
        String paramkeyid3valueStr = "";
        StringBuffer paramdescStr = new StringBuffer();
        StringBuffer paramtypeflagStr = new StringBuffer();
        DataSet paramds = this.sapphireReport.getParamds();
        boolean keyidFoundInReportParams = false;
        boolean sdcidFoundInReportParams = false;
        for (int i = 0; i < paramds.getRowCount(); ++i) {
            String value;
            if ("O".equals(paramds.getValue(i, "paramtypeflag"))) continue;
            String paramid = paramds.getValue(i, "paramid");
            String paraminto = paramds.getValue(i, "paraminto");
            if ("sdcid".equalsIgnoreCase(paramid)) {
                sdcidFoundInReportParams = true;
            }
            if ("keyid1".equalsIgnoreCase(paramid)) {
                keyidFoundInReportParams = true;
            }
            if ((value = this.paramMap.containsKey(paramid) ? (this.paramMap.get(paramid) != null ? this.paramMap.get(paramid).toString() : "") : (this.paramMap.containsKey(paraminto) ? this.getParamValue(paramds, i, sapphireConnection) : (String)this.paramMap.get("SAPPHIRE_" + paramid.toUpperCase()))) != null && value.length() > 0) {
                if ((value = value.replaceAll(";", ",").replaceAll("','", ",")).startsWith("'")) {
                    value = value.substring(1, value.length());
                }
                if (value.endsWith("'")) {
                    value = value.substring(0, value.length() - 1);
                }
            }
            paramidStr.append(";" + paramid);
            paramvalueStr.append(";" + value);
            paramdescStr.append(";" + paramds.getValue(i, "paramdesc"));
            paramtypeflagStr.append(";I");
        }
        if (!keyidFoundInReportParams) {
            paramkeyid1valueStr = this.paramMap.containsKey("keyid1") ? (String)this.paramMap.get("keyid1") : (String)this.paramMap.get("SAPPHIRE_KEYID1");
            paramkeyid2valueStr = this.paramMap.containsKey("keyid2") ? (String)this.paramMap.get("keyid2") : (String)this.paramMap.get("SAPPHIRE_KEYID2");
            paramkeyid3valueStr = this.paramMap.containsKey("keyid3") ? (String)this.paramMap.get("keyid3") : (String)this.paramMap.get("SAPPHIRE_KEYID3");
            if (paramkeyid1valueStr != null && paramkeyid1valueStr.length() > 0) {
                paramidStr.append(";keyid1");
                paramvalueStr.append(";" + paramkeyid1valueStr.replaceAll(";", ",").replaceAll("','", ","));
                paramdescStr.append(";");
                paramtypeflagStr.append(";");
            }
            if (paramkeyid2valueStr != null && paramkeyid2valueStr.length() > 0) {
                paramidStr.append(";keyid2");
                paramvalueStr.append(";" + paramkeyid2valueStr.replaceAll(";", ",").replaceAll("','", ","));
                paramdescStr.append(";");
                paramtypeflagStr.append(";");
            }
            if (paramkeyid3valueStr != null && paramkeyid3valueStr.length() > 0) {
                paramidStr.append(";keyid3");
                paramvalueStr.append(";" + paramkeyid3valueStr.replaceAll(";", ",").replaceAll("','", ","));
                paramdescStr.append(";");
                paramtypeflagStr.append(";");
            }
        }
        if (!sdcidFoundInReportParams) {
            String value = "";
            value = this.paramMap.containsKey("sdcid") ? (String)this.paramMap.get("sdcid") : (String)this.paramMap.get("SAPPHIRE_SDCID");
            paramidStr.append(";sdcid");
            paramvalueStr.append(";" + value);
            paramdescStr.append(";");
            paramtypeflagStr.append(";");
        }
        if (paramidStr.length() > 1) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "ReportEvent");
            props.setProperty("keyid1", this.reporteventid);
            props.setProperty("linkid", "ReportEvent Params");
            props.setProperty("paramid", paramidStr.substring(1));
            props.setProperty("paramvalue", paramvalueStr.substring(1));
            props.setProperty("paramdesc", paramdescStr.substring(1));
            props.setProperty("paramtypeflag", paramtypeflagStr.substring(1));
            props.setProperty("language", this.getLanguageid());
            props.setProperty("timezone", this.getTimezone());
            as.processAction("AddSDIDetail", "1", props);
            String[] paramidArr = paramidStr.substring(1).split(";");
            String[] paramvalueArr = paramvalueStr.substring(1).split(";");
            for (int i = 0; i < paramidArr.length; ++i) {
                String paramid = paramidArr[i];
                String paramvalue = paramvalueArr[i];
                db.updateClob("reporteventparam", "paramvalueclob", paramvalue, new String[]{"reporteventid", "paramid"}, new String[]{this.reporteventid, paramid});
            }
        }
    }

    private String getParamValue(DataSet paramds, int row, SapphireConnection sapphireConnection) {
        M18NUtil m18n = new M18NUtil(sapphireConnection);
        String paramval = "";
        String paraminto = paramds.getValue(row, "paraminto");
        if ("dateonly".equals(paramds.getString(row, "paramtype"))) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date)this.paramMap.get(paraminto));
            paramval = m18n.formatDateOnly(calendar);
        } else {
            paramval = this.paramMap.get(paraminto).toString();
        }
        return paramval;
    }

    private void saveReportEventItem(DBUtil db, boolean gensdilogflag, String sdcidvalue, String keyid1value, String keyid2value, String keyid3value) throws SapphireException {
        if (gensdilogflag) {
            DataSet evtitemds = new DataSet();
            evtitemds.addColumnValues("reporteventid", 0, this.reporteventid, ";", "");
            evtitemds.addColumn("reporteventitemid", 1);
            String logsdcid = SapphireReportEvent.getSDILogValue(sdcidvalue, this.paramMap, "SAPPHIRE_SDCID");
            if (logsdcid == null || logsdcid.length() == 0) {
                throw new SapphireException("No sdcid found, cannot create SDI log for the report event.");
            }
            evtitemds.addColumnValues("itemsdcid", 0, logsdcid, ";", "");
            String logkeyid1 = SapphireReportEvent.getSDILogValue(keyid1value, this.paramMap, "SAPPHIRE_KEYID1");
            String logkeyid2 = SapphireReportEvent.getSDILogValue(keyid2value, this.paramMap, "SAPPHIRE_KEYID2");
            String logkeyid3 = SapphireReportEvent.getSDILogValue(keyid3value, this.paramMap, "SAPPHIRE_KEYID3");
            evtitemds.addColumnValues("itemkeyid1", 0, logkeyid1, ";", "");
            evtitemds.addColumnValues("itemkeyid2", 0, logkeyid2, ";", "(null)");
            evtitemds.addColumnValues("itemkeyid3", 0, logkeyid3, ";", "(null)");
            evtitemds.padColumn("reporteventid");
            evtitemds.padColumn("itemsdcid");
            evtitemds.padColumn("itemkeyid2");
            evtitemds.padColumn("itemkeyid3");
            for (int i = 0; i < evtitemds.getRowCount(); ++i) {
                evtitemds.setNumber(i, "reporteventitemid", i);
            }
            DataSetUtil.insert(db, evtitemds, "reporteventitem");
        }
    }

    public String saveEvent(byte[] bytes, SapphireConnection sapphireConnection, PropertyList nwaStats) throws SapphireException {
        PropertyList pl = new PropertyList();
        return this.saveEvent(bytes, sapphireConnection, pl);
    }

    public String saveEvent(SapphireConnection sapphireConnection, String boprinteventtype) throws SapphireException {
        if (this.saveEvent) {
            block38: {
                DBUtil db = new DBUtil();
                db.setConnection(sapphireConnection);
                ActionService as = new ActionService(sapphireConnection);
                PropertyList props = new PropertyList();
                props.put("sdcid", "ReportEvent");
                props.put("reportid", this.reportid);
                props.put("reportversionid", this.reportversionid);
                if (this.parentreporteventid != null) {
                    props.put("reporteventdesc", "Reprint of " + this.parentreporteventid);
                    props.put("eventtype", TYPE_REPRINT);
                } else {
                    props.put("reporteventdesc", this.reportid + "(ver " + this.reportversionid + ")");
                    props.put("displaytype", this.displaytype);
                    props.put("eventtype", boprinteventtype);
                }
                props.put("keyid1", this.reporteventid);
                if (this.sapphireReport != null && this.sapphireReport.initialdisposition != null && this.sapphireReport.initialdisposition.length() > 0) {
                    props.put("disposition", this.sapphireReport.initialdisposition);
                } else {
                    props.put("disposition", "Pending");
                }
                props.put("parenteventid", this.parentreporteventid);
                props.put("overrideautokey", "Y");
                props.put("addressid", this.addressid);
                props.put("addresstype", this.addresstype);
                props.put("emailto", this.emailto);
                props.put("emailfrom", this.emailfrom);
                props.put("emailcc", this.emailcc);
                props.put("emailsubject", this.emailsubject);
                props.put("emailmessage", this.emailmessage);
                props.put("filename", this.filename);
                try {
                    as.processAction("AddSDI", "1", props);
                    this.reporteventid = props.getProperty("newkeyid1");
                    if (this.parentreporteventid != null) break block38;
                    byte[] array = this.reportByte;
                    if ("J".equals(this.reporttypeflag) && (!"pdf".equals(this.displaytype) && TYPE_VIEW.equals(this.eventtype) || TYPE_EMAIL.equals(this.eventtype) || TYPE_PRINT.equals(this.eventtype) || TYPE_EXPORT.equals(this.eventtype))) {
                        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);){
                            objectStream.writeObject(this.reportObject);
                            array = byteStream.toByteArray();
                        }
                    }
                    this.updateReportContent(db, array);
                    if (!"N".equals(this.sapphireReport.gensdilogflag)) {
                        DataSet evtitemds = new DataSet();
                        evtitemds.addColumnValues("reporteventid", 0, this.reporteventid, ";", "");
                        evtitemds.addColumn("reporteventitemid", 1);
                        String logsdcid = SapphireReportEvent.getSDILogValue(this.sapphireReport.sdcidvalue, this.paramMap, "SAPPHIRE_SDCID");
                        if (logsdcid == null || logsdcid.length() == 0) {
                            throw new SapphireException("No sdcid found, cannot create SDI log for the report event.");
                        }
                        evtitemds.addColumnValues("itemsdcid", 0, logsdcid, ";", "");
                        String logkeyid1 = SapphireReportEvent.getSDILogValue(this.sapphireReport.keyid1value, this.paramMap, "SAPPHIRE_KEYID1");
                        String logkeyid2 = SapphireReportEvent.getSDILogValue(this.sapphireReport.keyid2value, this.paramMap, "SAPPHIRE_KEYID2");
                        String logkeyid3 = SapphireReportEvent.getSDILogValue(this.sapphireReport.keyid3value, this.paramMap, "SAPPHIRE_KEYID3");
                        evtitemds.addColumnValues("itemkeyid1", 0, logkeyid1, ";", "");
                        evtitemds.addColumnValues("itemkeyid2", 0, logkeyid2, ";", "(null)");
                        evtitemds.addColumnValues("itemkeyid3", 0, logkeyid3, ";", "(null)");
                        evtitemds.padColumn("reporteventid");
                        evtitemds.padColumn("itemsdcid");
                        evtitemds.padColumn("itemkeyid2");
                        evtitemds.padColumn("itemkeyid3");
                        for (int i = 0; i < evtitemds.getRowCount(); ++i) {
                            evtitemds.setNumber(i, "reporteventitemid", i);
                        }
                        DataSetUtil.insert(db, evtitemds, "reporteventitem");
                    }
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
                finally {
                    db.reset();
                    db.releaseConnection();
                }
            }
            return this.reporteventid;
        }
        return "";
    }

    public void redoEvent(ConnectionInfo connectionInfo, HttpServletRequest request, HttpServletResponse response, String reportid, SapphireReport sr) throws SapphireException {
        if (sr instanceof SapphireJasperReport) {
            try {
                switch (this.displaytype) {
                    case "pdf": {
                        if (TYPE_VIEW.equals(this.getEventtype()) || TYPE_EXPORT.equals(this.getEventtype()) || TYPE_EMAIL.equals(this.getEventtype())) {
                            SafeSQL safeSQL = new SafeSQL();
                            String sql = "SELECT disposition, digitallysignedflag FROM reportevent  WHERE reportid = " + safeSQL.addVar(reportid) + " AND reportversionid = " + safeSQL.addVar(this.reportversionid) + " AND reporteventid = " + safeSQL.addVar(this.parentreporteventid);
                            DataSet reportds = new QueryProcessor(connectionInfo.getConnectionId()).getPreparedSqlDataSet(sql, safeSQL.getValues());
                            String disposition = reportds.getValue(0, "disposition");
                            String digitallySigned = reportds.getString(0, "digitallysignedflag", "");
                            if (!(digitallySigned.equalsIgnoreCase("Y") || this.isDigitallysigned() != null && this.isDigitallysigned().equalsIgnoreCase("Y") || !disposition.equals("Confirmed") || this.getReportByte() == null)) {
                                this.setReportByte(SapphireReport.removeWatermark(this.getReportByte()));
                            }
                            SapphireJasperUtil.runReportToWebPdf(this.getReportByte(), response, reportid, connectionInfo, "", false);
                            break;
                        }
                        SapphireJasperUtil.runReportToWebPdf(this.getReportByte(), response, reportid, connectionInfo, "", false);
                        break;
                    }
                    case "xlsx": 
                    case "xls": {
                        SapphireJasperUtil.runReportToWebExcel(this.getReportByte(), response, reportid, connectionInfo, "", false, this.getDisplaytype());
                        break;
                    }
                    case "doc": 
                    case "docx": {
                        SapphireJasperUtil.runReportToWebWord(this.getReportByte(), response, reportid, connectionInfo, "", false, this.getDisplaytype());
                        break;
                    }
                    case "html": {
                        SapphireJasperUtil.runReportToWebHtml(this.getReportByte(), response, reportid, connectionInfo, "", false);
                        break;
                    }
                    case "csv": {
                        SapphireJasperUtil.runReportToWebCSV(this.getReportByte(), response, reportid, connectionInfo, "", false);
                        break;
                    }
                    case "rtf": {
                        SapphireJasperUtil.runReportToWebRTF(this.getReportByte(), response, reportid, connectionInfo, "", false);
                        break;
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        if (sr instanceof SapphireJavaTalendReport) {
            byte[] bytes = this.getStoredReportBytes();
            String defaultFilename = this.filename == null || this.filename.length() == 0 ? ((SapphireJavaTalendReport)sr).getLogicalFileName("") : this.filename;
            String mimetype = ((SapphireJavaTalendReport)sr).getMimeType(defaultFilename);
            response.setContentType(mimetype);
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment; filename=" + HttpUtil.encodeURIComponent(defaultFilename));
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(bytes, 0, bytes.length);
                outputStream.flush();
                outputStream.close();
            }
            catch (IOException e) {
                throw new SapphireException(e);
            }
        }
        if (sr instanceof SapphireBOReport) {
            SapphireBOUtil boUtil = new SapphireBOUtil();
            if ("pdf".equals(this.getDisplaytype()) && (TYPE_VIEW.equals(this.eventtype) || TYPE_EXPORT.equals(this.eventtype) && !this.exportUsingGenerateReportAction)) {
                byte[] reportBytes = this.getStoredReportBytesFromAttachment(this.parentreporteventid, connectionInfo.getConnectionId());
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT disposition FROM reportevent  WHERE reportid = " + safeSQL.addVar(reportid) + " AND reportversionid = " + safeSQL.addVar(this.reportversionid) + " AND reporteventid = " + safeSQL.addVar(this.parentreporteventid);
                DataSet reportds = new QueryProcessor(connectionInfo.getConnectionId()).getPreparedSqlDataSet(sql, safeSQL.getValues());
                String disposition = reportds.getValue(0, "disposition");
                if (disposition.equals("Confirmed") && reportBytes != null) {
                    reportBytes = SapphireReport.removeWatermark(reportBytes);
                }
                boUtil.runReportToWeb(reportBytes, response, this.getDisplaytype());
            } else {
                boUtil.runReportToWeb(this.getStoredReportBytesFromAttachment(this.parentreporteventid, connectionInfo.getConnectionId()), response, this.getDisplaytype());
            }
        } else if (sr instanceof SapphireNWAReport) {
            byte[] image = this.getStoredReportBytes();
            response.setContentType("image/png");
            response.setContentLength(image.length);
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(image, 0, image.length);
                outputStream.flush();
                outputStream.close();
            }
            catch (IOException e) {
                throw new SapphireException(e);
            }
        }
        this.saveEvent(this.getReportObject(), connectionInfo);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void redoEvent(SapphireConnection sapphireConnection) throws SapphireException {
        block34: {
            block35: {
                block36: {
                    Trace.logInfo("Start Redo reportevent:" + this.reporteventid);
                    if (!"J".equals(this.reporttypeflag)) break block35;
                    if (TYPE_VIEW.equals(this.eventtype) || TYPE_EXPORT.equals(this.eventtype) || TYPE_EMAIL.equals(this.getEventtype())) {
                        throw new SapphireException("Redo a View/Export event should be called throw a different API with response object.");
                    }
                    if (TYPE_PRINT.equals(this.eventtype)) {
                        try {
                            SafeSQL safeSQL = new SafeSQL();
                            String sql = "SELECT printerid FROM address WHERE addressid =" + safeSQL.addVar(this.addressid) + " AND addresstype =" + safeSQL.addVar(this.addresstype);
                            new QueryProcessor(sapphireConnection.getConnectionId()).getPreparedSqlDataSet(sql, safeSQL.getValues());
                            String printer = new QueryProcessor(sapphireConnection.getConnectionId()).getPreparedSqlDataSet(sql, safeSQL.getValues()).getString(0, "printerid");
                            SapphireJasperUtil.exportReportToPrinter((JasperPrint)this.reportObject, printer);
                        }
                        catch (Exception e) {
                            throw new SapphireException(e);
                        }
                    }
                    if (!TYPE_EMAIL.equals(this.eventtype)) break block36;
                    PropertyList actionProps = new PropertyList();
                    actionProps.put("from", this.emailfrom);
                    actionProps.put("to", this.emailto);
                    actionProps.put("cc", this.emailcc);
                    actionProps.put("subject", this.emailsubject);
                    actionProps.put("message", this.emailmessage);
                    String ext = "";
                    File temp = null;
                    ActionProcessor ap = new ActionProcessor(sapphireConnection.getConnectionId());
                    try {
                        if (this.displaytype == null || this.displaytype.length() == 0 || "pdf".equalsIgnoreCase(this.displaytype)) {
                            ext = ".pdf";
                        } else if (this.displaytype.equals("excel")) {
                            ext = ".xls";
                        }
                        temp = FileUtil.createTempFile(this.reportid, ext).toFile();
                        SapphireJasperUtil.exportReportToFile((JasperPrint)this.reportObject, this.displaytype, temp.getPath());
                        actionProps.put("filename", temp.getPath());
                        ap.processAction("SendMail", "1", actionProps);
                    }
                    catch (IOException e) {
                        try {
                            throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                        }
                        catch (Throwable throwable) {
                            SapphireJasperUtil.deleteFile(temp);
                            throw throwable;
                        }
                    }
                    SapphireJasperUtil.deleteFile(temp);
                    break block34;
                }
                if (TYPE_EXPORT.equals(this.eventtype)) {
                    try {
                        SapphireJasperUtil.exportReportToFile((JasperPrint)this.reportObject, this.displaytype, this.filename);
                    }
                    catch (Exception e) {
                        throw new SapphireException(e);
                    }
                }
                throw new SapphireException("Unknown Event Type:" + this.eventtype);
            }
            if ("X".equals(this.reporttypeflag)) {
                if (TYPE_EMAIL.equals(this.eventtype)) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("from", this.emailfrom);
                    actionProps.setProperty("to", this.emailto);
                    actionProps.setProperty("cc", this.emailcc);
                    actionProps.setProperty("subject", this.emailsubject);
                    actionProps.setProperty("message", this.emailmessage);
                    String ext = "";
                    try {
                        if (this.displaytype == null || this.displaytype.length() == 0 || "pdf".equalsIgnoreCase(this.displaytype)) {
                            ext = ".pdf";
                        } else if (this.displaytype.equals("excel")) {
                            ext = ".xls";
                        }
                        File temp = File.createTempFile(this.reportid, ext);
                        temp.deleteOnExit();
                        try (FileOutputStream fout = null;){
                            fout = new FileOutputStream(temp);
                            fout.write(this.reportByte);
                        }
                        actionProps.setProperty("filename", temp.getPath());
                    }
                    catch (IOException e) {
                        throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                    }
                    ActionProcessor ap = new ActionProcessor(sapphireConnection.getConnectionId());
                    ap.processAction("SendMail", "1", actionProps);
                } else if (TYPE_EXPORT.equals(this.eventtype)) {
                    try {
                        File file = new File(this.filename.replaceAll("\\\\", "/"));
                        file.deleteOnExit();
                        try (FileOutputStream fout = null;){
                            fout = new FileOutputStream(file);
                            fout.write(this.reportByte);
                        }
                    }
                    catch (Exception e) {
                        throw new SapphireException(e);
                    }
                }
            }
        }
        this.saveEvent(sapphireConnection);
        Trace.logInfo("Done redo reportevent:" + this.reporteventid);
    }

    private static String getSDILogValue(String input, HashMap paramMap, String defaultkey) {
        String logid = null;
        String paramname = "";
        if (input != null && input.length() > 0) {
            if (input.indexOf("[") != 0) {
                logid = input;
            } else {
                try {
                    paramname = StringUtil.getTokens(input)[0];
                    if (paramMap.get(paramname) != null) {
                        logid = paramMap.get(paramname).toString();
                    } else if (paramMap.get(paramname.toUpperCase()) != null) {
                        logid = paramMap.get(paramname.toUpperCase()).toString();
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        if (logid == null || logid.equalsIgnoreCase("null") || logid.contains("null;") || logid.contains(";null")) {
            logid = (String)paramMap.get(defaultkey);
        }
        if (logid != null && logid.length() > 0 && logid.contains("','")) {
            logid = StringUtil.replaceAll(logid, "','", ";");
        }
        if (OpalUtil.isNotEmpty(logid)) {
            if (logid.startsWith("'")) {
                logid = logid.substring(1, logid.length());
            }
            if (logid.endsWith("'")) {
                logid = logid.substring(0, logid.length() - 1);
            }
        }
        return logid;
    }

    public static DataSet getEventDataSet(String reporteventid, QueryProcessor qp) {
        String whereclause = "";
        SafeSQL safeSQL = new SafeSQL();
        if (reporteventid != null && reporteventid.length() > 0) {
            whereclause = " WHERE reportevent.reporteventid in (" + safeSQL.addIn(reporteventid, ";") + ")";
            whereclause = " WHERE reportevent.reporteventid in (" + safeSQL.addIn(reporteventid, ";") + ")";
        }
        return SapphireReportEvent.getEventDataSetCommon(whereclause, qp, safeSQL);
    }

    private static DataSet getEventDataSetCommon(String whereclause, QueryProcessor qp, SafeSQL safeSQL) {
        String sql = "SELECT reportevent.reportid, reportevent.reportversionid, reportevent.reporteventid, reporteventdesc, parenteventid, disposition, reportevent.createby, reportevent.createdt, reviewedby, revieweddt, displaytype, eventtype, reporteventitem.itemkeyid1, reporteventitem.itemsdcid, reportevent.languageid, reportevent.timezone FROM reportevent LEFT OUTER JOIN reporteventitem ON reportevent.reporteventid=reporteventitem.reporteventid ";
        sql = sql + whereclause;
        sql = sql + " OR reportevent.parenteventid in ( SELECT reportevent.reporteventid FROM reportevent LEFT OUTER JOIN reporteventitem ON reportevent.reporteventid=reporteventitem.reporteventid " + whereclause + " ) order by reportevent.createdt";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < ds.getRowCount(); ++i) {
            int parentrow;
            String parenteventid = ds.getString(i, "parenteventid");
            if (parenteventid == null || parenteventid.length() <= 0 || (parentrow = ds.findRow("reporteventid", parenteventid)) < 0) continue;
            ds.setString(i, "itemsdcid", ds.getString(parentrow, "itemsdcid"));
            ds.setString(i, "itemkeyid1", ds.getString(parentrow, "itemkeyid1"));
            ds.setString(i, "itemkeyid2", ds.getString(parentrow, "itemkeyid2"));
            ds.setString(i, "itemkeyid3", ds.getString(parentrow, "itemkeyid3"));
        }
        return ds;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DataSet getEventDataSet(String sdcid, String keyid1, String keyid2, String keyid3, QueryProcessor qp) {
        DAMProcessor dam = new DAMProcessor(qp.getConnectionid());
        StringHolder rsetHolder = new StringHolder();
        dam.createRSet(sdcid, keyid1, keyid2, keyid3, rsetHolder);
        String rsetid = rsetHolder.value;
        String whereclause = " WHERE reportevent.reporteventid in ( SELECT i.reporteventid FROM reporteventitem i INNER JOIN rsetitems r ON i.itemsdcid=r.sdcid AND i.itemkeyid1=r.keyid1 AND i.itemkeyid2=r.keyid2 AND i.itemkeyid3=r.keyid3 AND r.rsetid='" + rsetid + "' )";
        DataSet ds = null;
        try {
            SafeSQL safeSQL = new SafeSQL();
            ds = SapphireReportEvent.getEventDataSetCommon(whereclause, qp, safeSQL);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        finally {
            dam.clearRSet(rsetid);
        }
        return ds;
    }

    private void updateReportContent(DBUtil db, byte[] contentValue) throws Exception {
        try {
            PreparedStatement pStatement = db.prepareStatement("UPDATE reportevent set reportcontent=? where reporteventid=? ");
            pStatement.setBytes(1, contentValue);
            pStatement.setString(2, this.reporteventid);
            pStatement.executeUpdate();
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            db.reset();
        }
    }

    private String getStatValue(String paramid, DataSet outputParams, PropertyList stats) {
        String ret = "";
        Object[] keys = stats.keySet().toArray();
        for (int i = 0; i < keys.length; ++i) {
            String statid = keys[i].toString();
            if (statid.length() >= 40) {
                statid = statid.substring(0, 40);
            }
            if (!paramid.equals(statid)) continue;
            return stats.getProperty(keys[i].toString());
        }
        return null;
    }

    public void editStats(PropertyList stats, SapphireConnection sapphireConnection) throws SapphireException {
        try {
            DBUtil db = new DBUtil();
            db.setConnection(sapphireConnection);
            DataSet paramInfo = this.sapphireReport.getParamds();
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("paramtypeflag", "O");
            DataSet outputParams = paramInfo.getFilteredDataSet(filter);
            for (int i = 0; i < outputParams.getRowCount(); ++i) {
                String paramid = outputParams.getValue(i, "paramid");
                String paramval = this.getStatValue(paramid, outputParams, stats);
                if (paramval == null) continue;
                outputParams.setString(i, "paramvalue", paramval);
            }
            if (outputParams.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "ReportEvent");
                props.setProperty("keyid1", this.reporteventid);
                props.setProperty("linkid", "ReportEvent Params");
                props.setProperty("paramid", outputParams.getColumnValues("paramid", ";"));
                props.setProperty("paramvalue", outputParams.getColumnValues("paramvalue", ";"));
                props.setProperty("paramtypeflag", outputParams.getColumnValues("paramtypeflag", ";"));
                props.setProperty("paramdesc", outputParams.getColumnValues("paramdesc", ";"));
                new ActionProcessor(sapphireConnection.getConnectionId()).processAction("AddSDIDetail", "1", props);
                String[] paramidArr = StringUtil.split(outputParams.getColumnValues("paramid", ";"), ";");
                String[] paramvalueArr = StringUtil.split(outputParams.getColumnValues("paramvalue", ";"), ";");
                for (int i = 0; i < paramidArr.length; ++i) {
                    String paramid = paramidArr[i];
                    String paramvalue = paramvalueArr[i];
                    db.updateClob("reporteventparam", "paramvalueclob", paramvalue, new String[]{"reporteventid", "paramid"}, new String[]{this.reporteventid, paramid});
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to update stats in report event." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())));
        }
    }

    private static String getLatestReportEventId(String selectedReportEventID, SapphireConnection sapphireConnection) throws SapphireException {
        String reportEventID = "";
        DBUtil dbUtil = new DBUtil();
        dbUtil.setConnection(sapphireConnection);
        SafeSQL safeSQL = new SafeSQL();
        selectedReportEventID = selectedReportEventID.contains("-") ? selectedReportEventID.substring(0, selectedReportEventID.indexOf(45) + 1) : selectedReportEventID;
        String sql = "SELECT reporteventid FROM (SELECT ROW_NUMBER() OVER (Order by createdt desc) AS rnum, reporteventid FROM reportevent WHERE reporteventid like " + safeSQL.addVar(selectedReportEventID + "%") + ") reportevent WHERE reportevent.rnum = 1";
        try {
            dbUtil.createPreparedResultSet(sql, safeSQL.getValues());
            if (dbUtil.getNext()) {
                reportEventID = dbUtil.getString("reporteventid");
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            dbUtil.reset();
            dbUtil.releaseConnection();
        }
        return reportEventID;
    }

    public byte[] getStoredReportBytesFromAttachment(String reportEventId, String connectionid) throws SapphireException {
        byte[] reportData = null;
        AttachmentProcessor ap = new AttachmentProcessor(connectionid);
        Attachment storedAttachment = ap.getSDIAttachment("ReportEvent", reportEventId, "", "", 1);
        if (storedAttachment != null && storedAttachment.hasData()) {
            reportData = storedAttachment.getData();
        } else {
            try {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("displayType", this.getDisplaytype());
                reportData = this.displaytype.equalsIgnoreCase("pdf") && (TYPE_VIEW.equals(this.eventtype) || TYPE_EXPORT.equals(this.eventtype)) ? this.getReportByte() : SapphireJasperUtil.getReportBytes(props, (JasperPrint)this.getReportObject());
            }
            catch (JRException jrException) {
                throw new SapphireException(jrException);
            }
        }
        return reportData;
    }

    public void getStoredDocumentBytesFromAttachment(String reportEventId, String connectionid) throws SapphireException {
        QueryProcessor qp = new QueryProcessor(connectionid);
        sapphire.attachment.Attachment reportAttachment = null;
        String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'ReportEvent' AND keyid1 = ? AND attachmentclass = 'ReportEvent'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{reportEventId});
        if (ds.getRowCount() > 0) {
            reportAttachment = sapphire.attachment.Attachment.getAttachment("ReportEvent", reportEventId, null, null, ds.getInt(0, "attachmentnum"));
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionid);
            reportAttachment = attachmentProcessor.getSDIAttachment(reportAttachment, Attachment.ThumbnailGeneration.DISABLED);
        }
        if (reportAttachment != null) {
            this.setReportByte(reportAttachment.getData());
        }
    }
}

