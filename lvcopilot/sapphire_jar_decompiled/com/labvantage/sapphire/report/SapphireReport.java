/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Color
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.IDocument
 *  com.aspose.pdf.Stamp
 *  com.aspose.pdf.TextStamp
 *  com.aspose.pdf.facades.FormattedText
 *  com.aspose.pdf.facades.PdfContentEditor
 *  com.aspose.pdf.facades.StampInfo
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.report;

import com.aspose.pdf.Color;
import com.aspose.pdf.Document;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.Stamp;
import com.aspose.pdf.TextStamp;
import com.aspose.pdf.facades.FormattedText;
import com.aspose.pdf.facades.PdfContentEditor;
import com.aspose.pdf.facades.StampInfo;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.report.ReportConstants;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.bo.SapphireBOReport;
import com.labvantage.sapphire.report.collated.SapphireCollatedReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJavaTalendReport;
import com.labvantage.sapphire.report.nwa.SapphireNWAReport;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.sql.DataSource;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class SapphireReport
implements ReportConstants {
    protected String reportid;
    protected String reportdesc;
    protected String reportversionid;
    protected String controlledflag;
    protected String reporttypeflag;
    protected String genvieweventflag;
    protected String genreprinteventflag;
    protected String gensdilogflag;
    protected String sdcidvalue;
    protected String keyid1value;
    protected String keyid2value;
    protected String keyid3value;
    protected String initialdisposition;
    protected String reporteventdescrule;
    protected String watermarkflag;
    protected String signingmode;
    protected String signingprovider;
    protected String signingprovidernode;
    protected int signaturexpos;
    protected int signatureypos;
    protected int signaturewidth;
    protected int signatureheight;
    protected String signaturereason;
    protected int signaturepage;
    protected boolean deliverrunfile = false;
    protected SDIData reportData = null;
    protected QueryProcessor qp;
    protected TranslationProcessor tp;
    protected DAMProcessor dp;
    protected String rset;
    protected DataSet primaryds;
    protected DataSet paramds;
    protected SapphireConnection sapphireConnection;
    protected static final String PARAM_TYPE_STRING = "string";
    protected static final String PARAM_TYPE_NUMBER = "number";
    protected static final String PARAM_TYPE_DATE = "absreldt";
    protected static final String PARAM_TYPE_DATEONLY = "dateonly";
    protected static final String PARAM_TYPE_SDC_LOOKUP = "sdc";
    protected static final String PARAM_TYPE_SDC_DROPDOWN = "ddsdc";
    protected static final String PARAM_TYPE_SQL_LOOKUP = "sqllookup";
    protected static final String PARAM_TYPE_SQL_DROPDOWN = "ddsql";
    protected static final String PARAM_TYPE_SQL_DROPDOWN_COMBO = "ddsqlcombo";
    protected static final String PARAM_TYPE_REFTYPE = "reftype";
    protected static final String PARAM_TYPE_MULTIPLE_LIST = "multiplelist";
    protected static final String PARAM_TYPE_HIDDEN = "hidden";
    protected static final String PARAM_TYPE_RSET = "rsetid";
    protected List supportedDisplayTypes = new ArrayList();
    protected String defaultDisplayType = "";
    protected ConnectionInfo connectionInfo;
    protected List supportedLanguages = new ArrayList();
    protected String languageid = "";
    protected String displayType = "";
    protected boolean isFile = false;
    protected List<String> supportedTimezones = new ArrayList<String>();
    protected String collatedReportTitle;
    protected String childReportTitle;
    protected boolean includeTOCFlag;
    protected String addressid;

    public String getAddressid() {
        return this.addressid;
    }

    public void setAddressid(String addressid) {
        this.addressid = addressid;
    }

    public String getCollatedReportTitle() {
        return this.collatedReportTitle;
    }

    public void setCollatedReportTitle(String collatedReportTitle) {
        this.collatedReportTitle = collatedReportTitle;
    }

    public String getChildReportTitle() {
        return this.childReportTitle;
    }

    public void setChildReportTitle(String childReportTitle) {
        this.childReportTitle = childReportTitle;
    }

    public boolean isIncludeTOCFlag() {
        return this.includeTOCFlag;
    }

    public void setIncludeTOCFlag(boolean includeTOCFlag) {
        this.includeTOCFlag = includeTOCFlag;
    }

    public String getReportid() {
        return this.reportid;
    }

    public String getReportdesc() {
        return this.reportdesc;
    }

    public void setReportdesc(String reportdesc) {
        this.reportdesc = reportdesc;
    }

    public void setReportid(String reportid) {
        this.reportid = reportid;
    }

    public String getReportversionid() {
        return this.reportversionid;
    }

    public void setReportversionid(String reportversionid) {
        this.reportversionid = reportversionid;
    }

    public void setReporttypeflag(String reporttypeflag) {
        this.reporttypeflag = reporttypeflag;
    }

    public abstract void runReportToWeb(HashMap var1, String var2, HttpServletRequest var3, HttpServletResponse var4, String var5, boolean var6) throws SapphireException;

    public abstract void sendReportToEmail(HashMap var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) throws SapphireException;

    public abstract void sendReportToFile(HashMap var1, String var2, String var3) throws SapphireException;

    public abstract void sendReportToPrinter(HashMap var1, String var2, String var3, String var4, String var5) throws SapphireException;

    public void setSapphireConnection(SapphireConnection sc) {
        this.sapphireConnection = sc;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public SapphireReport(String reporttypeflag) {
        this.reporttypeflag = reporttypeflag;
    }

    private SapphireReportEvent getReportEvent(HashMap paramMap, String displaytype) throws SapphireException {
        if (this.isControlledReport()) {
            SapphireReportEvent event = null;
            SapphireConnection sapphireConnection = null;
            if (paramMap.get("regenerate") != null && paramMap.get("regenerate").equals("Y") || paramMap.get("createNewVersion") != null && paramMap.get("createNewVersion").equals("Y")) {
                DBUtil dbu = new DBUtil();
                SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(this.connectionInfo.getDatabaseId());
                DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
                try {
                    dbu.setConnection(database.getDbms(), dataSource.getConnection());
                    sapphireConnection = new SapphireConnection(dbu.getConnection(), this.connectionInfo);
                }
                catch (SQLException e) {
                    throw new SapphireException("Failed to set sapphire connection.");
                }
                if (sapphireConnection != null) {
                    event = new SapphireReportEvent(this, paramMap, sapphireConnection);
                }
            } else {
                event = new SapphireReportEvent(this);
            }
            if (displaytype == null || displaytype.length() == 0) {
                displaytype = this.getDefaultDisplayType();
            }
            event.setDisplaytype(displaytype);
            event.setLanguageid(paramMap.get("SAPPHIRE_REPORT_LANGUAGE") != null ? paramMap.get("SAPPHIRE_REPORT_LANGUAGE").toString() : "");
            event.setTimezone(paramMap.get("SAPPHIRE_REPORT_TIMEZONE") != null ? paramMap.get("SAPPHIRE_REPORT_TIMEZONE").toString() : "");
            event.setParamMap(paramMap);
            String reporteventid = event.getReporteventid();
            paramMap.put("SAPPHIRE_ReportEventID", reporteventid);
            paramMap.put("SAPPHIRE_BaseReportEventID", reporteventid.contains("-") ? reporteventid.substring(0, reporteventid.indexOf("-")) : reporteventid);
            paramMap.put("SAPPHIRE_ReportEventVersionID", event.getReporteventversionid());
            paramMap.put("SAPPHIRE_PriorReportEventID", event.getPriorReportEventID());
            event.getAttachment().setFilename(this.reportid + "." + event.getDisplaytype());
            return event;
        }
        return null;
    }

    public SapphireReportEvent runReportToWebEvent(HashMap paramMap, String displaytype, HttpServletResponse response, boolean isFile) throws SapphireException {
        SapphireReportEvent event = this.getReportEvent(paramMap, displaytype);
        if (event != null && isFile) {
            event.setEventtype("Export");
        } else if (event != null && this.isGenViewEvent()) {
            event.setEventtype("View");
        } else {
            return null;
        }
        return event;
    }

    public SapphireReportEvent sendReportToEmailEvent(HashMap paramMap, String displaytype, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage) throws SapphireException {
        SapphireReportEvent event = this.getReportEvent(paramMap, displaytype);
        if (event != null) {
            event.setEventtype("Email");
            event.setEmailfrom(emailfrom);
            event.setEmailto(emailtolist);
            event.setEmailsubject(emailsubject);
            event.setEmailmessage(emailmessage);
            event.setEmailcc(emailcclist);
        }
        return event;
    }

    public SapphireReportEvent sendReportToFileEvent(HashMap paramMap, String displaytype, String fileName) throws SapphireException {
        SapphireReportEvent event = this.getReportEvent(paramMap, displaytype);
        if (event != null) {
            if (paramMap.containsKey("lvsexporterflag") && "A".equalsIgnoreCase((String)paramMap.get("lvsexporterflag"))) {
                event.setExportUsingGenerateReportAction(true);
            }
            event.setEventtype("Export");
            event.setFilename(fileName);
        }
        return event;
    }

    public SapphireReportEvent sendReportToPrinterEvent(HashMap paramMap, String displaytype, String printername, String addressid, String addresstype) throws SapphireException {
        SapphireReportEvent event = this.getReportEvent(paramMap, displaytype);
        if (event != null) {
            event.setEventtype("Print");
            event.setAddressid(addressid);
            event.setAddresstype(addresstype);
        }
        return event;
    }

    public static SapphireReport getIntanceReportEventid(String reporteventid, ConnectionInfo connectionInfo) throws SapphireException {
        QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT reportid,reportversionid,languageid,displaytype FROM reportevent WHERE reporteventid=" + safeSQL.addVar(reporteventid);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String reportid = ds.getString(0, "reportid", "");
            String reportversionid = ds.getString(0, "reportversionid", "");
            String languageid = ds.getString(0, "languageid", "");
            String displayType = ds.getString(0, "displaytype", "");
            return SapphireReport.getIntance(reportid, reportversionid, connectionInfo, languageid, false, displayType);
        }
        return null;
    }

    public static SapphireReport getIntance(String reportid, String reportversionid, ConnectionInfo connectionInfo, String languageid, boolean isFile, String displayType) throws SapphireException {
        return SapphireReport.getIntance(reportid, reportversionid, connectionInfo, false, languageid, isFile, displayType);
    }

    public static SapphireReport getIntance(String reportid, String reportversionid, ConnectionInfo connectionInfo, boolean isFile, String displayType) throws SapphireException {
        return SapphireReport.getIntance(reportid, reportversionid, connectionInfo, false, "", isFile, displayType);
    }

    public static SapphireReport getIntance(String reportid, String reportversionid, ConnectionInfo connectionInfo, boolean deliverrunfile, String languageid, boolean isFile, String displayType) throws SapphireException {
        String reporttypeflag;
        DataSet reportds;
        SapphireReport sr = null;
        SDIData reportData = SapphireReport.getReportData(reportid, reportversionid, "", connectionInfo);
        DataSet dataSet = reportds = reportData != null ? reportData.getDataset("primary") : null;
        if (reportds == null || reportds.getRowCount() == 0) {
            if (reportversionid != null && reportversionid.length() > 0) {
                throw new SapphireException("Report:" + reportid + " Versionid:" + reportversionid + " not registered.");
            }
            throw new SapphireException("No current version of the report:" + reportid + ". Please approve a version of the report first before allow viewing.");
        }
        switch (reporttypeflag = reportds.getString(0, "reporttypeflag")) {
            case "J": 
            case "K": {
                sr = new SapphireJasperReport(reporttypeflag);
                sr.languageid = languageid != null ? languageid : "";
                sr.displayType = OpalUtil.isNotEmpty(displayType) ? displayType : "";
                sr.isFile = isFile;
                break;
            }
            case "C": 
            case "T": {
                sr = new SapphireJavaTalendReport(reporttypeflag);
                break;
            }
            case "X": {
                sr = new SapphireBOReport(reporttypeflag);
                break;
            }
            case "N": {
                sr = new SapphireNWAReport(reporttypeflag);
                break;
            }
            case "E": {
                sr = new SapphireCollatedReport(reporttypeflag);
                sr.languageid = languageid != null ? languageid : "";
                break;
            }
            default: {
                throw new SapphireException("Report:  " + reportid + " Neither a BOXI Report nor Jasper Report");
            }
        }
        sr.connectionInfo = connectionInfo;
        sr.reportData = reportData;
        sr.reportid = reportid;
        sr.reportversionid = reportds.getString(0, "reportversionid");
        sr.reportdesc = reportds.getString(0, "reportdesc");
        sr.controlledflag = reportds.getString(0, "controlledflag");
        sr.genvieweventflag = reportds.getString(0, "genvieweventflag");
        sr.genreprinteventflag = reportds.getString(0, "genreprinteventflag");
        sr.gensdilogflag = reportds.getString(0, "gensdilogflag");
        sr.sdcidvalue = reportds.getString(0, "sdcidvalue");
        sr.keyid1value = reportds.getString(0, "keyid1value");
        sr.keyid2value = reportds.getString(0, "keyid2value");
        sr.keyid3value = reportds.getString(0, "keyid3value");
        sr.initialdisposition = reportds.getString(0, "initialdisposition", "Pending");
        sr.reporteventdescrule = reportds.getValue(0, "reporteventdescrule");
        sr.watermarkflag = reportds.getString(0, "watermarkflag", "");
        ConnectionProcessor cn = new ConnectionProcessor(connectionInfo.getConnectionId());
        sr.signingmode = reportds.getString(0, "signingmode", cn.getSysConfigProperty("signingmode"));
        sr.signingprovider = reportds.getString(0, "signingprovider", cn.getSysConfigProperty("signingprovider"));
        sr.signingprovidernode = reportds.getString(0, "signingprovidernode", cn.getSysConfigProperty("signingprovidernode"));
        sr.signaturexpos = reportds.getInt(0, "signaturexpos", 5);
        sr.signatureypos = reportds.getInt(0, "signatureypos", 5);
        sr.signaturewidth = reportds.getInt(0, "signaturewidth", 125);
        sr.signatureheight = reportds.getInt(0, "signatureheight", 75);
        sr.signaturereason = reportds.getString(0, "signaturereason", "Document approved");
        sr.signaturepage = reportds.getInt(0, "signaturepage", 1);
        sr.primaryds = reportds;
        sr.deliverrunfile = deliverrunfile;
        QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT * FROM reportparam WHERE reportid=" + safeSQL.addVar(reportid) + " and reportversionid=" + safeSQL.addVar(sr.reportversionid) + " order by usersequence";
        sr.paramds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (sr.paramds != null) {
            for (int i = 0; i < sr.paramds.getRowCount(); ++i) {
                if (sr.paramds.getString(i, "paramtypeflag", "").length() != 0) continue;
                sr.paramds.setString(i, "paramtypeflag", "I");
            }
        }
        sr.collatedReportTitle = reportds.getString(0, "titlepagereference");
        sr.childReportTitle = reportds.getString(0, "separatorpagereference");
        sr.includeTOCFlag = OpalUtil.isNotEmpty(reportds.getString(0, "includetocflag")) && reportds.getString(0, "includetocflag").equalsIgnoreCase("Y");
        sr.addressid = reportds.getString(0, "reportbyaddressid");
        sr.init();
        return sr;
    }

    public boolean isControlledReport() {
        return "Y".equals(this.controlledflag);
    }

    public boolean isGenViewEvent() {
        return !"N".equals(this.genvieweventflag);
    }

    public static SDIData getReportData(String reportid, String reportversionid, String categoryid, ConnectionInfo connectionInfo) {
        String sqlwhere = "";
        SDIProcessor sdiProcessor = new SDIProcessor(connectionInfo.getConnectionId());
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Report");
        sdiRequest.setRequestItem("attachment");
        if (reportid != null && reportid.length() > 0) {
            sqlwhere = "reportid='" + SafeSQL.encodeForSQL(reportid, connectionInfo.isOracle()) + "'";
            sqlwhere = reportversionid != null && reportversionid.length() > 0 && !"C".equals(reportversionid) ? sqlwhere + " and reportversionid='" + SafeSQL.encodeForSQL(reportversionid, connectionInfo.isOracle()) + "'" : sqlwhere + " and ( versionstatus='C' or versionstatus='P' ) ";
            sdiRequest.setQueryFrom("report");
            sdiRequest.setQueryWhere(sqlwhere);
            sdiRequest.setRequestItem("primary[reportdesc, reporteventdescrule, controlledflag, genvieweventflag, genreprinteventflag, gensdilogflag, sdcidvalue, keyid1value, keyid2value, initialdisposition, keyid3value, reporttypeflag, librarydir,objectname, reportversionid, versionstatus, checksumvalue, watermarkflag, signingmode, signingprovider, signingprovidernode, signaturexpos, signatureypos, signaturewidth, signatureheight, signaturereason, signaturepage, appresourceid, titlepagereference,separatorpagereference, includetocflag, reportbyaddressid]");
            sdiRequest.setQueryOrderBy("versionstatus, cast( reportversionid as integer ) desc");
        } else if (categoryid != null && categoryid.length() > 0) {
            sdiRequest.setQueryFrom("report, categoryitem");
            sdiRequest.setQueryWhere("(report.versionstatus='C' or report.versionstatus='P') and categoryitem.sdcid='Report' and report.reportid=categoryitem.keyid1 and categoryitem.categoryid='" + SafeSQL.encodeForSQL(categoryid, connectionInfo.isOracle()) + "'");
            sdiRequest.setRequestItem("primary[reportid, reportversionid, reportdesc, versionstatus, appresource]");
            sdiRequest.setQueryOrderBy("reportdesc");
        } else {
            Trace.logError("No reportid or categoryid, cannot retrieve report.");
        }
        return sdiProcessor.getSDIData(sdiRequest);
    }

    public static DataSet getReportDataSet(String reportid, String reportversionid, String categoryid, ConnectionInfo connectionInfo) {
        SDIData ds = SapphireReport.getReportData(reportid, reportversionid, categoryid, connectionInfo);
        return ds != null ? ds.getDataset("primary") : null;
    }

    public void init() throws SapphireException {
    }

    public boolean isPromptRequired(HttpServletRequest request) {
        boolean isPromptRequired = false;
        if ("submitarg".equals(request.getParameter("mode"))) {
            return false;
        }
        if (this.paramds.getRowCount() > 0) {
            for (int i = 0; i < this.paramds.getRowCount(); ++i) {
                if (this.paramds.getValue(i, "paramtype").equals(PARAM_TYPE_HIDDEN)) continue;
                isPromptRequired = true;
                break;
            }
        }
        return isPromptRequired;
    }

    public boolean isPromptRequiredWithHiddenValue(HttpServletRequest request) {
        boolean required = true;
        if ("submitarg".equals(request.getParameter("mode"))) {
            required = false;
        }
        return required;
    }

    public String getPrompts(HttpServletRequest request, PageContext pageContext) throws SapphireException {
        return this.getPrompts(request, pageContext, false, null);
    }

    public String getPrompts(HttpServletRequest request, PageContext pageContext, boolean runFileMode, PropertyList requestProps) throws SapphireException {
        int i;
        String paraminto;
        int j;
        String prefix = runFileMode ? "inputparam_" : "";
        StringBuffer html = new StringBuffer();
        ArrayList<String> paramidList = new ArrayList<String>();
        HashMap dependentMap = new HashMap();
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        if (this.tp == null) {
            this.tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        }
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(pageContext);
        sdiTagUtil.setLanguage(requestContext.getPropertyList().getProperty("language"));
        html.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        html.append("<form action=\"rc?command=viewreport&mode=submitarg\" id=\"submitarg\" name=\"submitarg\" method=\"post\">\n");
        if (!runFileMode) {
            html.append("<input type=\"hidden\" id=\"reportid\" name=\"reportid\" value=\"").append(this.reportid).append("\">\n");
            html.append("<input type=\"hidden\" id=\"reportversionid\" name=\"reportversionid\" value=\"").append(this.reportversionid).append("\">\n");
            html.append("<input type=\"hidden\" id=\"displaytype\" name=\"displaytype\" value=\"").append(request.getParameter("displaytype") == null ? "" : request.getParameter("displaytype")).append("\">\n");
            Map requestParamMap = request.getParameterMap();
            for (String paramname : requestParamMap.keySet()) {
                if ("reportid".equals(paramname) || "reportversionid".equals(paramname) || "displaytype".equals(paramname)) continue;
                html.append("<input type=\"hidden\"").append(" id=\"").append(paramname).append("\" name=\"").append(paramname).append("\" value=\"").append(request.getParameter(paramname) == null ? "" : request.getParameter(paramname)).append("\">\n");
            }
        }
        html.append("<table id=\"reportargid\" cellpadding=2 cellspacing=0 style=\"vertical-align:top;\" class=\"maintform_table\">\n");
        StringBuffer validations = new StringBuffer();
        HashMap<String, String> sqlMap = new HashMap<String, String>();
        HashMap<String, String> idIntoMap = new HashMap<String, String>();
        for (j = 0; j < this.paramds.getRowCount(); ++j) {
            String paramid = this.paramds.getString(j, "paramid");
            paraminto = this.paramds.getValue(j, "paraminto").length() == 0 ? paramid : this.paramds.getValue(j, "paraminto");
            idIntoMap.put(paramid, paraminto);
        }
        for (j = 0; j < this.paramds.getRowCount(); ++j) {
            String paramId = this.paramds.getString(j, "paramid");
            paraminto = this.paramds.getValue(j, "paraminto").length() == 0 ? paramId : this.paramds.getValue(j, "paraminto");
            paramidList.add("[" + paraminto + "]");
            String paramtype = this.paramds.getString(j, "paramtype");
            if (!PARAM_TYPE_SQL_DROPDOWN.equals(paramtype) && !PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype) && !PARAM_TYPE_MULTIPLE_LIST.equals(paramtype) && !PARAM_TYPE_SDC_DROPDOWN.equals(paramtype) && !PARAM_TYPE_SDC_LOOKUP.equals(paramtype)) continue;
            String sql = this.paramds.getString(j, "paramdata") == null ? "" : this.paramds.getString(j, "paramdata").replaceAll("\r\n", " ");
            sql = this.getReplacedSQL(sql, idIntoMap);
            sqlMap.put(paraminto, sql);
            ArrayList<String> dependentList = new ArrayList<String>();
            for (int k = 0; k < paramidList.size(); ++k) {
                String prevParamId = paramidList.get(k).toString();
                if (sql.indexOf(prevParamId) > 0) {
                    prevParamId = prevParamId.substring(1, prevParamId.length() - 1);
                    dependentList.add(prevParamId);
                    continue;
                }
                String obfsqlparam = EncryptDecrypt.obfsql(prevParamId).substring(3);
                if (sql.indexOf(obfsqlparam) <= 0) continue;
                sql = sql.replaceAll(obfsqlparam, prevParamId);
                sqlMap.put(paraminto, sql);
                prevParamId = prevParamId.substring(1, prevParamId.length() - 1);
                dependentList.add(prevParamId);
            }
            if (dependentList.isEmpty()) continue;
            dependentMap.put(paraminto, dependentList);
        }
        HashMap dependencyMap = new HashMap();
        Set newSet = dependentMap.entrySet();
        for (int j2 = 0; j2 < this.paramds.getRowCount(); ++j2) {
            ArrayList dependencyList = new ArrayList();
            String paramId = this.paramds.getString(j2, "paramid");
            String paraminto2 = this.paramds.getValue(j2, "paraminto").length() == 0 ? paramId : this.paramds.getValue(j2, "paraminto");
            for (Map.Entry entry : newSet) {
                ArrayList newList = (ArrayList)entry.getValue();
                if (!newList.contains(paraminto2)) continue;
                dependencyList.add(entry.getKey());
            }
            if (dependencyList.isEmpty()) continue;
            dependencyMap.put(paraminto2, dependencyList);
        }
        HashSet set = new HashSet();
        ArrayList consolidatedList = new ArrayList(dependentMap.values());
        for (i = 0; i < consolidatedList.size(); ++i) {
            set.addAll((ArrayList)consolidatedList.get(i));
        }
        for (i = 0; i < this.paramds.getRowCount(); ++i) {
            String value;
            String paramtype = this.paramds.getString(i, "paramtype");
            if (PARAM_TYPE_RSET.equals(paramtype)) continue;
            PropertyList column = new PropertyList();
            String paramid = this.paramds.getString(i, "paramid");
            String paraminto3 = this.paramds.getValue(i, "paraminto").length() == 0 ? paramid : this.paramds.getValue(i, "paraminto");
            column.setProperty("columnid", prefix + paraminto3);
            column.setProperty("name", prefix + paraminto3);
            String title = this.paramds.getString(i, "paramdesc") == null || this.paramds.getString(i, "paramdesc").length() == 0 ? paraminto3 : this.paramds.getString(i, "paramdesc");
            column.setProperty("title", title);
            String paramvalue = "";
            if (runFileMode) {
                if (requestProps != null && requestProps.size() > 0 && requestProps.getProperty(paramid, "").length() > 0) {
                    paramvalue = requestProps.getProperty(paramid, "");
                } else {
                    paramvalue = this.paramds.getString(i, "paramvalue");
                    paramvalue = this.substituteValue(paramvalue);
                }
            } else {
                paramvalue = this.paramds.getString(i, "paramvalue");
                paramvalue = this.substituteValue(paramvalue);
            }
            column.setProperty("value", paramvalue);
            String sdcid = this.paramds.getString(i, "sdcid");
            column.setProperty("sdcid", sdcid);
            String usersequence = this.paramds.getValue(i, "usersequence");
            String mandatoryflag = this.paramds.getString(i, "mandatoryflag");
            String reftypeid = this.paramds.getString(i, "reftypeid");
            String paramdata = this.paramds.getString(i, "paramdata");
            column.setProperty("reftypeid", reftypeid);
            column.setProperty("sql", paramdata);
            String webLookupUrl = this.paramds.getString(i, "weblookupurl");
            if (webLookupUrl != null && webLookupUrl.trim().length() > 0) {
                PropertyList lookUpLink = new PropertyList();
                lookUpLink.setProperty("href", webLookupUrl);
                column.setProperty("lookuplink", lookUpLink);
            }
            if (PARAM_TYPE_HIDDEN.equals(paramtype)) {
                if (paraminto3 != null && paraminto3.length() > 0) {
                    String tempParamInto = paraminto3.toUpperCase();
                    if (tempParamInto.equals("KEYID1")) {
                        if (sdcid == null || sdcid.trim().length() == 0) {
                            int rowid = this.paramds.findRow("paraminto", "sdcid");
                            if (rowid > -1) {
                                sdcid = this.paramds.getString(rowid, "paramvalue", "");
                            }
                            column.setProperty("sdcid", sdcid);
                        }
                        html.append("\n<tr>");
                        if (!runFileMode) {
                            html.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
                        }
                        html.append("<td class=\"maintform_fieldtitle\">").append(SafeHTML.encodeForHTML(this.tp.translate(title))).append("</td>");
                        html.append("<td class=\"maintform_field\">");
                        if ("Y".equals(mandatoryflag)) {
                            column.setProperty("validation", "Mandatory");
                            if (validations.length() == 0) {
                                validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n");
                                validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
                                validations.append("\n<script> function validate() {\n");
                            }
                            validations.append(" invalidfields = validateHiddenField( document.getElementById( '").append(prefix + paraminto3).append("' ) );\n");
                            validations.append("    if ( invalidfields.length == 0 ) { \n        document.getElementById( '").append(prefix + paraminto3).append("' ).className='validationfail';sapphire.ui.dialog.alert('").append(this.tp.translate("Input required for:") + SafeHTML.encodeForHTML(title)).append("')\n        return false;\n\t}\n");
                        }
                        column.setProperty("mode", "multilookup");
                        column.setProperty("readonly", "true");
                        column.setProperty("img", "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg");
                        if (column.getProperty("lookuplink") == null || column.getProperty("lookuplink").trim().length() == 0) {
                            PropertyList lookUpLink = new PropertyList();
                            lookUpLink.setProperty("selectortype", "checkbox");
                            if (paramdata != null && paramdata.trim().length() > 0) {
                                lookUpLink.setProperty("defaultquery", paramdata.trim());
                            }
                            column.setProperty("lookuplink", lookUpLink);
                        }
                    } else {
                        column.setProperty("mode", PARAM_TYPE_HIDDEN);
                    }
                }
            } else {
                html.append("\n<tr>");
                if (!runFileMode) {
                    html.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
                }
                html.append("<td class=\"maintform_fieldtitle\">").append(SafeHTML.encodeForHTML(this.tp.translate(title))).append("</td>");
                html.append("<td class=\"maintform_field\">");
                if ("Y".equals(mandatoryflag)) {
                    column.setProperty("validation", "Mandatory");
                    if (validations.length() == 0) {
                        validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n");
                        validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
                        validations.append("\n<script> function validate() {\n");
                    }
                    validations.append(" invalidfields = mandatoryValidation( document.getElementById( '").append(prefix + paraminto3).append("' ) );\n");
                    validations.append("    if ( invalidfields.length > 0 ) { \n        document.getElementById( '").append(prefix + paraminto3).append("' ).className='validationfail';sapphire.ui.dialog.alert('").append(this.tp.translate("Input required for:") + title).append("')\n        return false;\n\t}\n");
                }
                if (PARAM_TYPE_STRING.equals(paramtype)) {
                    column.setProperty("mode", "input");
                } else if (PARAM_TYPE_DATE.equals(paramtype) || PARAM_TYPE_DATEONLY.equals(paramtype)) {
                    column.setProperty("mode", "datelookup");
                    String defaultdate = column.getProperty("value");
                    DataSet ds = null;
                    if (defaultdate.length() > 0) {
                        ds = new DataSet(this.connectionInfo);
                        ds.addColumn("adate", 2);
                        ds.addRow();
                    }
                    if (PARAM_TYPE_DATEONLY.equals(paramtype)) {
                        if (ds != null) {
                            ds.setDateDisplayFormat("adate", new M18NUtil(this.connectionInfo).getDefaultDateOnlyFormat(false));
                            ds.setValue(0, "adate", defaultdate);
                            column.setProperty("value", ds.getValue(0, "adate"));
                        }
                        column.setProperty("format", "O");
                    } else if (ds != null) {
                        ds.setValue(0, "adate", defaultdate);
                        column.setProperty("value", ds.getValue(0, "adate"));
                    }
                    column.setProperty("size", "20");
                    column.setProperty("img", "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg");
                } else if (PARAM_TYPE_NUMBER.equals(paramtype)) {
                    column.setProperty("mode", "input");
                    column.setProperty("size", "14");
                } else if (PARAM_TYPE_REFTYPE.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SDC_DROPDOWN.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SDC_LOOKUP.equals(paramtype)) {
                    column.setProperty("mode", "lookup");
                    column.setProperty("mode", "lookup");
                    column.setProperty("img", "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg");
                    if (column.getPropertyList("lookuplink") != null) {
                        String href = column.getPropertyList("lookuplink").getProperty("href");
                        if (href.length() == 0) {
                            column.deleteProperty("lookuplink");
                        } else {
                            StringBuffer dependantParams = new StringBuffer();
                            href = this.getReplacedURL(href, idIntoMap, dependantParams);
                            PropertyList lookUpLink = new PropertyList();
                            lookUpLink.setProperty("dialogtype", "Sapphire Dialog");
                            lookUpLink.setProperty("href", "javascript:openURL('" + href + "','" + prefix + paraminto3 + "','" + dependantParams.toString() + "');");
                            column.setProperty("lookuplink", lookUpLink);
                        }
                    }
                } else if (PARAM_TYPE_SQL_DROPDOWN.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype)) {
                    column.setProperty("mode", "dropdowncombo");
                } else if (PARAM_TYPE_SQL_LOOKUP.equals(paramtype)) {
                    column.setProperty("mode", "lookup");
                } else if (PARAM_TYPE_MULTIPLE_LIST.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                    column.setProperty("size", "6");
                    column.setProperty("multiple", "true");
                }
            }
            if (set.contains(paraminto3)) {
                String jSonForDependency = this.getJSon(dependencyMap);
                String jSonSqlMap = this.getJSon(sqlMap);
                String params = this.getParams(sqlMap);
                String jSonDependent = this.getJSon(dependentMap);
                column.setProperty("onchange", "checkDependents(this," + jSonForDependency + "," + jSonSqlMap + "," + jSonDependent + ",'" + params + "')");
            } else if (column.getProperty("mode").equals("multilookup")) {
                column.setProperty("onchange", "setTextAreaValue(this)");
            } else if (column.getProperty("columnid").equalsIgnoreCase("incdetail")) {
                column.setProperty("onchange", "includeAllChildParams();");
            } else {
                column.setProperty("onchange", "if(false){}");
            }
            if (column.getProperty("mode").equals("multilookup")) {
                value = this.getMultiLookupHtml(column, requestContext);
            } else if (PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype)) {
                SDITagInfo sdiTagInfo = new SDITagInfo(new HashMap());
                value = sdiTagUtil.getInputHtml(column, sdiTagInfo);
                sdiTagUtil.collectDropDownComboInfo(column, sdiTagInfo, pageContext);
            } else {
                value = sdiTagUtil.getInputHtml(column, new SDITagInfo(new HashMap()));
            }
            html.append(value);
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("\n</table>");
        html.append("<br/>");
        html.append("\n</form>");
        html.append(validations.length() > 0 ? validations.append("\nreturn true;}\n</script>\n").toString() : "");
        if (pageContext.getAttribute("dd_dropdownvalues") != null) {
            html.append("\n<script>");
            html.append(pageContext.getAttribute("dd_dropdownvalues"));
            html.append("\n</script>\n");
        }
        return html.toString();
    }

    private String getMultiLookupHtml(PropertyList column, RequestContext requestContext) throws SapphireException {
        String data = column.getProperty("data", "primary");
        String displayStyle = column.getProperty("released").equals("true") ? "none" : "inline";
        StringBuffer html = new StringBuffer();
        html.append("<textarea edit=\"lookup\" ").append("rows=\"3\" cols=\"30\" readonly ").append(SDITagUtil.getInputCommonAttributes(column));
        html.append(" onkeydown=\"if ( event.keyCode == 46 ) { this.value='';sdiSetRowUpdate(event);} \" ");
        html.append("></textarea>");
        html.append(this.getLookupIconHTML(column, new SDITagInfo(new HashMap()), data, displayStyle, requestContext));
        return html.toString();
    }

    private String getLookupIconHTML(PropertyList attributes, SDITagInfo sdiInfo, String data, String displayStyle, RequestContext requestContext) throws SapphireException {
        PropertyList lookuplink;
        String currentcolid = attributes.getProperty("columnid", "");
        String pdid = StringUtil.replaceAll(currentcolid, ".", "_");
        StringBuffer html = new StringBuffer();
        String img = attributes.getProperty("img", "WEB-CORE/elements/images/lookup.gif");
        String imgtext = attributes.getProperty("imgtext");
        boolean usesapphirelookup = false;
        String sdcid = attributes.getProperty("sdcid", "");
        String lookupurl = attributes.getProperty("lookuppageid");
        if (lookupurl.length() > 0 && lookupurl.indexOf("rc?") == -1) {
            lookupurl = "rc?command=page&page=" + lookupurl;
        }
        if ((lookuplink = attributes.getPropertyList("lookuplink")) != null) {
            String hreftrim;
            String href = lookuplink.getProperty("href", "");
            if (href.length() > 0) {
                href = ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, currentcolid, href, sdiInfo);
            }
            if (!((hreftrim = href.trim().toLowerCase()).length() <= 0 || hreftrim.startsWith("rc?") || hreftrim.startsWith("http:") || hreftrim.startsWith("https:") || hreftrim.startsWith("javascript:"))) {
                href = "javascript:" + href;
            }
            lookupurl = href;
            if (sdcid.length() == 0) {
                sdcid = lookuplink.getProperty("sdcid", "");
            }
        }
        if (sdcid.length() > 0 || lookupurl.length() > 0) {
            PropertyList pagedirectives;
            String multiselect;
            String datasetcode = SDIData.getDatasetCode(data);
            attributes.setProperty("lookuppagedirectives", "oLUPD_" + datasetcode + "_" + pdid);
            ArrayList columns = null;
            String selectortype = lookuplink == null ? "" : lookuplink.getProperty("selectortype", "");
            String restrictiveWhere = "";
            String defaultQuery = "";
            String queryFrom = "";
            String queryWhere = "";
            if (lookuplink != null) {
                sdcid = lookuplink.getProperty("sdcid", sdcid);
                String tip = lookuplink.getProperty("tip");
                if (tip.length() > 0) {
                    imgtext = tip;
                }
                if (lookuplink.getProperty("dialogtype", "Sapphire Dialog").equalsIgnoreCase("Sapphire Dialog")) {
                    usesapphirelookup = true;
                }
                columns = lookuplink.getCollection("columns");
                selectortype = lookuplink.getProperty("selectortype", "");
                multiselect = selectortype.length() > 0 && selectortype.equalsIgnoreCase("checkbox") ? "Y" : "N";
                restrictiveWhere = lookuplink.getProperty("restrictivewhere", "");
                defaultQuery = lookuplink.getProperty("defaultquery", "");
                queryFrom = lookuplink.getProperty("queryfrom", "");
                queryWhere = lookuplink.getProperty("querywhere", "");
            } else {
                multiselect = "N";
            }
            boolean versioned = attributes.getProperty("versionedflag", "N").equals("Y");
            PropertyListCollection pd_columns = columns != null && columns.size() > 0 ? (PropertyListCollection)columns.clone() : new PropertyListCollection();
            StringBuffer mapcolumnid = new StringBuffer();
            StringBuffer fieldid = new StringBuffer();
            String curr_name = attributes.getProperty("name", "");
            String prefix = attributes.getProperty("_prefix") + datasetcode + attributes.getProperty("rowindex") + "_";
            String curr_version = attributes.getProperty("sdccolumnid2", "").length() > 0 ? prefix + attributes.getProperty("sdccolumnid2") : "";
            SDCProcessor sdcProcessor = new SDCProcessor(requestContext.getConnectionId());
            if (lookupurl.length() == 0) {
                if (selectortype.trim().length() == 0) {
                    selectortype = "none";
                }
                lookupurl = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
                pagedirectives = SDITagUtil.getLookupPageDirectives(sdcid, curr_name, curr_version, versioned, selectortype, usesapphirelookup, restrictiveWhere, defaultQuery, queryFrom, queryWhere, prefix, pd_columns, true, mapcolumnid, fieldid, this.tp, sdcProcessor);
            } else {
                pagedirectives = SDITagUtil.getLookupPageDirectives(sdcid, curr_name, curr_version, versioned, selectortype, usesapphirelookup, restrictiveWhere, defaultQuery, queryFrom, queryWhere, prefix, pd_columns, false, mapcolumnid, fieldid, this.tp, sdcProcessor);
            }
            attributes.setProperty("lookupfieldid", fieldid.toString());
            if (pagedirectives == null) {
                return "";
            }
            JSONObject job = pagedirectives.toJSONObject(false);
            job.remove("__propertylistid");
            job.remove("__propertylistsequence");
            html.append("<script>");
            html.append("var ").append(attributes.getProperty("lookuppagedirectives")).append("=").append(job.toString()).append(";");
            html.append("</script>");
            html.append("<a style='display:").append(displayStyle).append(";' id=\"").append(attributes.getProperty("name")).append("_img\" href=\"/").append(imgtext.length() > 0 ? imgtext : "Lookup").append("\" onClick=\"");
            if (!lookupurl.trim().toLowerCase().startsWith("javascript:")) {
                html.append("lookupfield('").append(fieldid.toString()).append("','").append(sdcid).append("','','").append(multiselect).append("','','','','','");
                html.append(datasetcode).append("','").append(attributes.getProperty("rowindex")).append("'");
                html.append(",'").append(attributes.getProperty("columnid")).append("','").append(lookupurl).append("'");
                String keyColumns = sdcProcessor.getProperty(sdcid, "keycolumns");
                if (keyColumns == null || keyColumns.length() <= 0) {
                    Logger.logWarn("sdcid is not valid.");
                    throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
                }
                int keycolscount = Integer.parseInt(keyColumns);
                String mapColumns = keycolscount == 1 ? "keycolid1" : (keycolscount == 2 ? "keycolid1;keycolid2" : "keycolid1;keycolid2;keycolid3");
                html.append(",'").append(mapColumns).append("'");
                html.append(",").append(usesapphirelookup).append("");
                html.append(",").append(attributes.getProperty("lookuppagedirectives")).append(");");
                html.append("return false;");
            } else {
                html.append(lookupurl).append(";return false;");
            }
        } else {
            Logger.logWarn("No sdcid, reftypeid or lookup page/url found for lookup . Therefore cannot render lookup icon.");
            return "";
        }
        html.append("\" tabindex=\"0\">");
        html.append("<img title=\"").append(imgtext.length() > 0 ? imgtext : "Lookup").append("\" border=\"0\" src=\"").append(img).append("\">");
        html.append("</a>");
        return html.toString();
    }

    private String getParams(HashMap sqlMap) {
        Set paramSet = sqlMap.keySet();
        Iterator iter = paramSet.iterator();
        StringBuffer params = new StringBuffer();
        while (iter.hasNext()) {
            params.append("|").append(iter.next());
        }
        return params.substring(1);
    }

    private String getJSon(HashMap mapForJSon) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        Set set = mapForJSon.entrySet();
        for (Map.Entry entry : set) {
            buffer.append("'").append(entry.getKey()).append("'").append(":");
            if (entry.getValue() instanceof ArrayList) {
                ArrayList list = (ArrayList)entry.getValue();
                buffer.append("[");
                String x = "";
                for (int i = 0; i < list.size(); ++i) {
                    x = x + ",'" + list.get(i).toString() + "'";
                }
                x = x.substring(1);
                buffer.append(x).append("]").append(",");
                continue;
            }
            if (!(entry.getValue() instanceof String)) continue;
            String val = entry.getValue().toString();
            val = StringUtil.replaceAll(val, "'[", "[");
            val = StringUtil.replaceAll(val, "]'", "]");
            val = StringUtil.replaceAll(val, "'", "|");
            buffer.append("'").append(val).append("'").append(",");
        }
        buffer = new StringBuffer(buffer.substring(0, buffer.length() - 1));
        buffer.append("}");
        return buffer.toString();
    }

    public String getPromptHtml(PageContext pageContext, HttpServletRequest request, ServletContext servletContext) {
        String paraminto;
        String paramid;
        int j;
        StringBuffer html = new StringBuffer();
        ArrayList<String> paramidList = new ArrayList<String>();
        HashMap dependentMap = new HashMap();
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        if (this.tp == null) {
            this.tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        }
        String enhancedReportName = this.reportid + "_" + this.connectionInfo.getSysuserId() + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date());
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(pageContext);
        sdiTagUtil.setLanguage(requestContext.getPropertyList().getProperty("language"));
        html.append("<script language=\"JavaScript\" src=\"WEB-OPAL/pagetypes/reports/scripts/reportoption.js\"></script>");
        html.append(HttpUtil.getSapphireCoreJSHTML(pageContext, RequestContext.getInstance(request)));
        html.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        html.append("<form action=\"rc/" + enhancedReportName + "?command=viewreport&mode=submitarg\" id=\"submitarg\" name=\"submitarg\" method=\"post\">\n");
        html.append("<input type=\"hidden\" name=\"reportid\" value=\"").append(this.reportid).append("\">\n");
        html.append("<input type=\"hidden\" name=\"reportversionid\" value=\"").append(this.reportversionid).append("\">\n");
        String createNewVersion = request.getParameter("createNewVersion");
        if (createNewVersion != null && createNewVersion.length() > 0) {
            html.append("<input type=\"hidden\" name=\"reporteventid\" value=\"").append(request.getParameter("reporteventid") == null ? "" : request.getParameter("reporteventid")).append("\">\n");
            html.append("<input type=\"hidden\" name=\"createNewVersion\" value=\"").append(createNewVersion).append("\">\n");
        }
        html.append("<input type=\"hidden\" name=\"displaytype\" value=\"").append(request.getParameter("displaytype") == null ? "" : request.getParameter("displaytype")).append("\">\n");
        Map requestParamMap = request.getParameterMap();
        for (String paramname : requestParamMap.keySet()) {
            if ("reportid".equals(paramname) || "reportversionid".equals(paramname) || "displaytype".equals(paramname)) continue;
            html.append("<input type=\"hidden\"").append(" id=\"").append(paramname).append("\" name=\"").append(paramname).append("\" value=\"").append(request.getParameter(paramname) == null ? "" : request.getParameter(paramname)).append("\">\n");
        }
        HashMap<String, String> sqlMap = new HashMap<String, String>();
        HashMap<String, String> idIntoMap = new HashMap<String, String>();
        for (j = 0; j < this.paramds.getRowCount(); ++j) {
            paramid = this.paramds.getString(j, "paramid");
            paraminto = this.paramds.getValue(j, "paraminto").length() == 0 ? paramid : this.paramds.getValue(j, "paraminto");
            idIntoMap.put(paramid, paraminto);
        }
        for (j = 0; j < this.paramds.getRowCount(); ++j) {
            paramid = this.paramds.getString(j, "paramid");
            paraminto = this.paramds.getValue(j, "paraminto").length() == 0 ? paramid : this.paramds.getValue(j, "paraminto");
            paramidList.add("[" + paraminto + "]");
            String paramtype = this.paramds.getString(j, "paramtype");
            if (!PARAM_TYPE_SQL_DROPDOWN.equals(paramtype) && !PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype) && !PARAM_TYPE_MULTIPLE_LIST.equals(paramtype) && !PARAM_TYPE_SDC_DROPDOWN.equals(paramtype) && !PARAM_TYPE_SDC_LOOKUP.equals(paramtype)) continue;
            String sql = this.paramds.getString(j, "paramdata") == null ? "" : this.paramds.getString(j, "paramdata").replaceAll("\r\n", " ");
            sql = this.getReplacedSQL(sql, idIntoMap);
            sqlMap.put(paraminto, sql);
            ArrayList<String> dependentList = new ArrayList<String>();
            for (int k = 0; k < paramidList.size(); ++k) {
                String prevParamId = paramidList.get(k).toString();
                if (sql.indexOf(prevParamId) > 0) {
                    prevParamId = prevParamId.substring(1, prevParamId.length() - 1);
                    dependentList.add(prevParamId);
                    continue;
                }
                String obfsqlparam = EncryptDecrypt.obfsql(prevParamId).substring(3);
                if (sql.indexOf(obfsqlparam) <= 0) continue;
                sql = sql.replaceAll(obfsqlparam, prevParamId);
                sqlMap.put(paraminto, sql);
                prevParamId = prevParamId.substring(1, prevParamId.length() - 1);
                dependentList.add(prevParamId);
            }
            if (dependentList.isEmpty()) continue;
            dependentMap.put(paraminto, dependentList);
        }
        HashMap dependencyMap = new HashMap();
        Set newSet = dependentMap.entrySet();
        for (int j2 = 0; j2 < this.paramds.getRowCount(); ++j2) {
            ArrayList dependencyList = new ArrayList();
            String paraminto2 = this.paramds.getValue(j2, "paraminto").length() == 0 ? this.paramds.getValue(j2, "paramid") : this.paramds.getValue(j2, "paraminto");
            for (Map.Entry entry : newSet) {
                ArrayList newList = (ArrayList)entry.getValue();
                if (!newList.contains(paraminto2)) continue;
                dependencyList.add(entry.getKey());
            }
            if (dependencyList.isEmpty()) continue;
            dependencyMap.put(paraminto2, dependencyList);
        }
        HashSet set = new HashSet();
        ArrayList consolidatedList = new ArrayList(dependentMap.values());
        for (int i = 0; i < consolidatedList.size(); ++i) {
            set.addAll((ArrayList)consolidatedList.get(i));
        }
        StringBuffer validations = new StringBuffer();
        StringBuffer hiddenGroup = new StringBuffer();
        String defaultgroupid = this.tp.translate("Please Specify Inputs");
        HashMap<String, StringBuffer> groupMap = new HashMap<String, StringBuffer>();
        ArrayList<String> groupList = new ArrayList<String>();
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramtypeflag", "I");
        DataSet inputParams = this.paramds.getFilteredDataSet(filter);
        int currentgrpcount = 0;
        filter.put("paramtype", PARAM_TYPE_HIDDEN);
        DataSet hiddenitems = this.paramds.getFilteredDataSet(filter);
        int hiddenitemcount = 0;
        if (hiddenitems != null && hiddenitems.getRowCount() > 0) {
            hiddenitemcount = hiddenitems.getRowCount();
        }
        boolean showTwoCols = inputParams.getRowCount() - hiddenitemcount > 10;
        for (int i = 0; i < inputParams.getRowCount(); ++i) {
            String value;
            String paramtype = inputParams.getString(i, "paramtype");
            if (PARAM_TYPE_RSET.equals(paramtype)) continue;
            PropertyList column = new PropertyList();
            String paramid2 = inputParams.getString(i, "paramid");
            String paraminto3 = inputParams.getValue(i, "paraminto").length() == 0 ? paramid2 : inputParams.getValue(i, "paraminto");
            column.setProperty("name", paraminto3);
            column.setProperty("columnid", paraminto3);
            String title = inputParams.getString(i, "paramdesc") == null || inputParams.getString(i, "paramdesc").length() == 0 ? paraminto3 : this.paramds.getString(i, "paramdesc");
            column.setProperty("title", title);
            String paramvalue = inputParams.getString(i, "paramvalue");
            paramvalue = this.substituteValue(paramvalue);
            if (paramvalue != null && paramvalue.length() > 0 && paramvalue.indexOf("[") > 0) {
                ArrayList list = (ArrayList)dependentMap.get(paraminto3);
                if (list != null) {
                    for (int indx = 0; indx < list.size(); ++indx) {
                        String key = list.get(indx).toString();
                        String value2 = request.getParameter(key);
                        if (value2 == null || value2.length() <= 0) continue;
                        value2 = StringUtil.replaceAll(value2, ";", "','");
                        paramvalue = StringUtil.replaceAll(paramvalue, "[" + key + "]", value2);
                    }
                }
                column.setProperty("value", paramvalue);
            } else {
                column.setProperty("value", paramvalue);
            }
            String sdcid = inputParams.getString(i, "sdcid");
            column.setProperty("sdcid", sdcid);
            String usersequence = inputParams.getValue(i, "usersequence");
            String mandatoryflag = inputParams.getString(i, "mandatoryflag");
            String reftypeid = inputParams.getString(i, "reftypeid");
            String paramdata = inputParams.getString(i, "paramdata");
            column.setProperty("reftypeid", reftypeid);
            if (paramdata != null && paramdata.length() > 0 && paramdata.indexOf("[") > 0) {
                ArrayList list = (ArrayList)dependentMap.get(paraminto3);
                if (list != null) {
                    for (int indx = 0; indx < list.size(); ++indx) {
                        String key = list.get(indx).toString();
                        String value3 = request.getParameter(key);
                        if (value3 == null || value3.length() <= 0) continue;
                        value3 = StringUtil.replaceAll(value3, ";", "','");
                        paramdata = StringUtil.replaceAll(paramdata, "[" + key + "]", value3);
                    }
                }
                column.setProperty("sql", paramdata);
            } else {
                column.setProperty("sql", paramdata);
            }
            String webLookupUrl = inputParams.getString(i, "weblookupurl");
            if (webLookupUrl != null && webLookupUrl.trim().length() > 0) {
                PropertyList lookUpLink = new PropertyList();
                lookUpLink.setProperty("href", webLookupUrl);
                column.setProperty("lookuplink", lookUpLink);
            }
            StringBuffer currentGroup = null;
            if (PARAM_TYPE_HIDDEN.equals(paramtype)) {
                column.setProperty("mode", PARAM_TYPE_HIDDEN);
                currentGroup = hiddenGroup;
            } else {
                String groupid;
                String string = groupid = inputParams.getValue(i, "groupid").length() == 0 ? defaultgroupid : inputParams.getValue(i, "groupid");
                if (groupMap.get(groupid) == null) {
                    currentGroup = new StringBuffer("<table cellpadding=\"2\" cellspacing=\"0\" style=\"\">\n");
                    currentgrpcount = 0;
                    groupMap.put(groupid, currentGroup);
                    groupList.add(groupid);
                }
                if (currentGroup == null) {
                    currentGroup = (StringBuffer)groupMap.get(groupid);
                }
                if (!showTwoCols || currentgrpcount % 2 == 0) {
                    currentGroup.append("\n<tr>");
                }
                currentGroup.append("<td class=\"maintform_fieldtitle\">").append(this.tp.translate(title)).append("</td>");
                currentGroup.append("<td class=\"maintform_field\">");
                if ("Y".equals(mandatoryflag)) {
                    column.setProperty("validation", "Mandatory");
                    if (validations.length() == 0) {
                        validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n");
                        validations.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
                        validations.append("\n<script> function validate() {\n");
                    }
                    validations.append(" invalidfields = mandatoryValidation( document.getElementById( '").append(paraminto3).append("' ) );\n");
                    validations.append("    if ( invalidfields.length > 0 ) { \n        document.getElementById( '").append(paraminto3).append("' ).className='validationfail';alert('Input required!');\n        return false;\n\t}\n");
                }
                if (PARAM_TYPE_STRING.equals(paramtype)) {
                    column.setProperty("mode", "input");
                } else if (PARAM_TYPE_DATE.equals(paramtype) || PARAM_TYPE_DATEONLY.equals(paramtype)) {
                    column.setProperty("mode", "datelookup");
                    String defaultdate = column.getProperty("value");
                    DataSet ds = null;
                    if (defaultdate.length() > 0) {
                        ds = new DataSet(this.connectionInfo);
                        ds.addColumn("adate", 2);
                        ds.addRow();
                    }
                    if (PARAM_TYPE_DATEONLY.equals(paramtype)) {
                        if (ds != null) {
                            ds.setDateDisplayFormat("adate", new M18NUtil(this.connectionInfo).getDefaultDateOnlyFormat(false));
                            ds.setValue(0, "adate", defaultdate);
                            column.setProperty("value", ds.getValue(0, "adate"));
                        }
                        column.setProperty("format", "O");
                    } else if (ds != null) {
                        ds.setValue(0, "adate", defaultdate);
                        column.setProperty("value", ds.getValue(0, "adate"));
                    }
                    column.setProperty("size", "20");
                    column.setProperty("img", "WEB-CORE/elements/images/lookup_date.gif");
                } else if (PARAM_TYPE_NUMBER.equals(paramtype)) {
                    column.setProperty("mode", "input");
                } else if (PARAM_TYPE_REFTYPE.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SDC_DROPDOWN.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SDC_LOOKUP.equals(paramtype)) {
                    column.setProperty("mode", "lookup");
                    column.setProperty("mode", "lookup");
                    column.setProperty("img", "WEB-CORE/elements/images/lookup.gif");
                    if (column.getPropertyList("lookuplink") != null) {
                        String href = column.getPropertyList("lookuplink").getProperty("href");
                        if (href.length() == 0) {
                            column.deleteProperty("lookuplink");
                        } else {
                            StringBuffer dependantParams = new StringBuffer();
                            href = this.getReplacedURL(href, idIntoMap, dependantParams);
                            PropertyList lookUpLink = new PropertyList();
                            lookUpLink.setProperty("href", "javascript:openURL('" + href + "','" + paraminto3 + "','" + dependantParams.toString() + "');");
                            lookUpLink.setProperty("dialogtype", "Sapphire Dialog");
                            column.setProperty("lookuplink", lookUpLink);
                        }
                    }
                } else if (PARAM_TYPE_SQL_DROPDOWN.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                } else if (PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype)) {
                    column.setProperty("mode", "dropdowncombo");
                } else if (PARAM_TYPE_SQL_LOOKUP.equals(paramtype)) {
                    column.setProperty("mode", "lookup");
                } else if (PARAM_TYPE_MULTIPLE_LIST.equals(paramtype)) {
                    column.setProperty("mode", "dropdownlist");
                    column.setProperty("size", "6");
                    column.setProperty("multiple", "true");
                }
            }
            if (set.contains(paraminto3)) {
                String jSonForDependency = this.getJSon(dependencyMap);
                String jSonSqlMap = this.getJSon(sqlMap);
                String params = this.getParams(sqlMap);
                String jSonDependent = this.getJSon(dependentMap);
                if (PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype)) {
                    column.setProperty("onchange", "sdiSetRowUpdate(event);checkDependents(this," + jSonForDependency + "," + jSonSqlMap + "," + jSonDependent + ",'" + params + "')");
                    SDITagInfo sdiTagInfo = new SDITagInfo(new HashMap());
                    value = sdiTagUtil.getInputHtml(column, sdiTagInfo);
                    sdiTagUtil.collectDropDownComboInfo(column, sdiTagInfo, pageContext);
                } else {
                    column.setProperty("onchange", "checkDependents(this," + jSonForDependency + "," + jSonSqlMap + "," + jSonDependent + ",'" + params + "')");
                    value = sdiTagUtil.getInputHtml(column, new SDITagInfo(new HashMap()));
                }
            } else if (PARAM_TYPE_SQL_DROPDOWN_COMBO.equals(paramtype)) {
                column.setProperty("onchange", "sdiSetRowUpdate(event);");
                SDITagInfo sdiTagInfo = new SDITagInfo(new HashMap());
                value = sdiTagUtil.getInputHtml(column, sdiTagInfo);
                sdiTagUtil.collectDropDownComboInfo(column, sdiTagInfo, pageContext);
            } else {
                column.setProperty("onchange", "if(false){}");
                value = sdiTagUtil.getInputHtml(column, new SDITagInfo(new HashMap()));
            }
            currentGroup.append(value);
            if (PARAM_TYPE_HIDDEN.equals(paramtype)) continue;
            currentGroup.append("</td>");
            if (currentgrpcount % 2 == 1 || !showTwoCols) {
                currentGroup.append("</tr>");
            }
            ++currentgrpcount;
        }
        for (int t = 0; t < groupList.size(); ++t) {
            String groupid = (String)groupList.get(t);
            Tab tab = new Tab();
            tab.setId(groupid);
            tab.setText(this.tp.translate(groupid));
            tab.setContent(groupMap.get(groupid) + "\n</table>");
            html.append(tab.getHtml());
            html.append("<br/>");
        }
        html.append(hiddenGroup);
        html.append("<br/>");
        html.append(this.getStandardButtons(pageContext));
        html.append("\n</form>");
        html.append(validations.length() > 0 ? validations.append("\nreturn true;}\n</script>\n").toString() : "");
        if (pageContext.getAttribute("dd_dropdownvalues") != null) {
            html.append("\n<script>");
            html.append(pageContext.getAttribute("dd_dropdownvalues"));
            html.append("\n</script>\n");
        }
        html.append("<script>var csrftoken ='" + pageContext.getSession().getAttribute("csrftoken") + "';</script>");
        return SapphireReport.getPromptPage(html.toString(), this.tp.translate(this.reportdesc != null && this.reportdesc.length() > 0 ? this.reportdesc : this.reportid), this.tp.translate("Enter Inputs For Report:") + " " + this.tp.translate(this.reportdesc != null && this.reportdesc.length() > 0 ? this.reportdesc : this.reportid), this.connectionInfo, request, servletContext) + "\n" + this.getJs();
    }

    private String getReplacedSQL(String sql, HashMap idIntoMap) {
        ArrayList newList = new ArrayList(idIntoMap.keySet());
        for (int i = 0; i < newList.size(); ++i) {
            if (sql.indexOf(newList.get(i).toString()) <= -1) continue;
            sql = StringUtil.replaceAll(sql, newList.get(i).toString(), idIntoMap.get(newList.get(i).toString()).toString());
        }
        return EncryptDecrypt.obfsql(sql);
    }

    private String getReplacedURL(String url, HashMap idIntoMap, StringBuffer dependantParams) {
        ArrayList newList = new ArrayList(idIntoMap.keySet());
        boolean first = true;
        for (int i = 0; i < newList.size(); ++i) {
            String id = "[" + newList.get(i) + "]";
            String idInto = "[" + idIntoMap.get(newList.get(i)) + "]";
            String idInto1 = idIntoMap.get(newList.get(i)).toString();
            if (!url.contains(id)) continue;
            url = StringUtil.replaceAll(url, id, idInto);
            if (first) {
                dependantParams.append(idInto1);
                first = false;
                continue;
            }
            dependantParams.append(";").append(idInto1);
        }
        url = StringUtil.replaceAll(url, "[", "||");
        url = StringUtil.replaceAll(url, "]", "||");
        return url;
    }

    private String getJs() {
        return "\n<script>\nfunction argValueChanged( e ){ }\n</script>";
    }

    private String getStandardButtons(PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        Button btn = new Button(pageContext);
        btn.setText(this.tp.translate("OK"));
        btn.setImg("WEB-CORE/images/gif/Confirm.gif");
        btn.setId("ok");
        btn.setTip("Run Report");
        btn.setAction("if ( window.validate == null || validate()){ sapphire.ui.button.setDisabled( document.getElementById( 'ok' ), true ); document.body.style.cursor = 'progress'; submitarg.submit();}");
        html.append(btn.getHtml());
        html.append("&nbsp;&nbsp;");
        btn = new Button(pageContext);
        btn.setText(this.tp.translate("Cancel"));
        btn.setImg("WEB-CORE/images/gif/Cancel.gif");
        btn.setId("cancel");
        btn.setTip("Cancel Run");
        btn.setAction("window.close()");
        html.append(btn.getHtml());
        return html.toString();
    }

    private static String getPromptPage(String formstr, String title, String prompt, ConnectionInfo conInfo, HttpServletRequest request, ServletContext servletContext) {
        TranslationProcessor tp = new TranslationProcessor(conInfo.getConnectionId());
        return "<html>\n<head>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n\t<title>" + tp.translate("Report Input Page") + "</title>\n" + JavaScriptAPITag.getJavaScriptAPI(servletContext, request, RequestContext.getInstance(request), conInfo) + "<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>" + HttpUtil.getCoreStyleSheets(false, request) + "\t<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/configreport/stylesheets/configreport.css", request) + "\" type=\"text/css\">\n</head><body>\n<div id=\"maintab\" style=\"overflow: scroll;\"><table style=\"overflow: scroll;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" height=\"100%\"  id=\"layout_maintable\">\n\n\n\n<tr class=\"layout_mainheader\" height=\"30\"  id=\"layout_mainheader\">\n\n    <td class=\"layout_borderbar_top\" style=\"display:none;\" id=\"borderbar_topcell\"><img src=\"WEB-CORE/images/blank.gif\" class=\"layout_borderbar_filler\"></td>\n\n\n    \n\n    <td valign=top>\n\n\n\t<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" width=\"100%\">\n\t\t<tr>\n\n            <td align=\"left\" valign=\"middle\" width=10 id=\"logo_cell\" style=\"padding-left:13px;\" >\n\n                <table cellpadding=0 cellspacing=0 border=0 class=\"logo_table\">\n<tr>\n<td class=\"logo_image\" valign=middle>\n<img src=\"WEB-OPAL/layouts/images/logo_labvantage.png\">\n</td>\n</tr></table>\n\n\n            </td><td align=\"left\" valign=\"middle\" width=10 nowrap id=\"applogo_cell\">\n\n                <table cellpadding=0 cellspacing=0 border=0 class=\"applogo_table\">\n<tr>\n<td class=\"applogo_text\" nowrap valign=middle>\n" + title + "</td>\n</tr></table>\n\n            </td>\n\n\n            <td nowrap align=\"left\" valign=\"middle\" class=\"layout_userinfo\">&nbsp;\n            </td>\n            <td align=\"right\" valign=\"middle\">&nbsp;</td>\n            <td width=100>\n                <img src=\"WEB-CORE/images/blank.gif\" height=0 width=100>\n            </td>\n\n        </tr>\n    </table>\n\n    \n\n\n<tr><td><table border=\"0\" width=\"100%\" height=\"100%\">\n\n\t<tr>\n\t\t<td>&nbsp;</td>\n\t\t<td valign=\"top\" align=\"center\">\n" + formstr + "\t\t\t<font color=\"red\"></font>\n\t\t</td>\n\t\t<td>&nbsp;</td>\n\t</tr>\n</table>  </td></tr></table></div></body></html>";
    }

    protected void createRSetInParamMap(Map paramsMap) {
        String sdcid = (String)paramsMap.get("SAPPHIRE_SDCID");
        String keyid1 = (String)paramsMap.get("SAPPHIRE_KEYID1");
        String keyid2 = (String)paramsMap.get("SAPPHIRE_KEYID2");
        String keyid3 = (String)paramsMap.get("SAPPHIRE_KEYID3");
        if (sdcid == null || sdcid.length() <= 0 || keyid1 == null || keyid1.length() <= 0) {
            throw new RuntimeException("Error: Report has SAPPHIRE_RSETID parameter but no sdcid, keyid1 inputs for the report!");
        }
        this.dp = new DAMProcessor(this.connectionInfo.getConnectionId());
        StringHolder sh = new StringHolder();
        this.dp.createRSet(sdcid, keyid1, keyid2, keyid3, sh);
        this.rset = sh.value;
        paramsMap.put("SAPPHIRE_RSETID", this.rset);
    }

    protected void setMultiListParam(Map paramsMap, HttpServletRequest request, String delimeter) {
        if (delimeter == null || delimeter.trim().length() == 0) {
            delimeter = ";";
        }
        for (int i = 0; i < this.paramds.getRowCount(); ++i) {
            String[] listvalue;
            String paramtype = this.paramds.getString(i, "paramtype");
            String paraminto = this.paramds.getString(i, "paraminto");
            if (!PARAM_TYPE_MULTIPLE_LIST.equals(paramtype) || (listvalue = request.getParameterValues(paraminto)) == null) continue;
            StringBuffer list = new StringBuffer();
            for (int v = 0; v < listvalue.length; ++v) {
                if (v == listvalue.length - 1) {
                    list.append(listvalue[v]);
                    continue;
                }
                list.append(listvalue[v]).append(delimeter);
            }
            paramsMap.put(paraminto, list.toString());
        }
    }

    protected void setInClauseParam(Map paramsMap, HttpServletRequest request) {
        for (int i = 0; i < this.paramds.getRowCount(); ++i) {
            String paramtype = this.paramds.getString(i, "paramtype");
            String paraminto = this.paramds.getString(i, "paraminto");
            if (!PARAM_TYPE_MULTIPLE_LIST.equals(paramtype)) continue;
            String[] listvalue = request.getParameterValues(paraminto);
            if (listvalue != null) {
                if (listvalue.length == 1 && listvalue[0].indexOf(";") > -1) {
                    listvalue = StringUtil.split(listvalue[0], ";");
                }
                paramsMap.put(paraminto, this.getInclause(listvalue));
                continue;
            }
            DataSet eventparamDS = (DataSet)request.getAttribute("paramvalueds");
            if (eventparamDS == null) continue;
            for (int count = 0; count < eventparamDS.size(); ++count) {
                if (!eventparamDS.getString(count, "paramid").equalsIgnoreCase(paraminto)) continue;
                listvalue = StringUtil.split(eventparamDS.getString(count, "paramvalueclob"), ",");
                paramsMap.put(paraminto, this.getInclause(listvalue));
            }
        }
    }

    protected void setInClauseParam(Map paramsMap, HashMap actionprops) {
        for (int i = 0; i < this.paramds.getRowCount(); ++i) {
            String[] listvalue;
            String paramtype = this.paramds.getString(i, "paramtype");
            String paraminto = this.paramds.getString(i, "paraminto");
            if (!PARAM_TYPE_MULTIPLE_LIST.equals(paramtype) || (listvalue = StringUtil.split((String)actionprops.get(paraminto), ";")) == null) continue;
            paramsMap.put(paraminto, this.getInclause(listvalue));
        }
    }

    private String getInclause(String[] listvalue) {
        StringBuffer list = new StringBuffer("'");
        for (int v = 0; v < listvalue.length; ++v) {
            if (v == listvalue.length - 1) {
                list.append(listvalue[v]).append("'");
                continue;
            }
            list.append(listvalue[v]).append("','");
        }
        return list.toString();
    }

    protected static String getKeyid1ListSelect(String rsetid) {
        return "SELECT keyid1 from rsetitems where rsetid='" + rsetid + "'";
    }

    public void reset() {
        if (this.rset != null && this.dp != null) {
            this.dp.clearRSet(this.rset);
        }
    }

    public static String getReportLookupHtml(DataSet ds, HttpServletRequest request, ServletContext servletContext) {
        ConnectionInfo ci = null;
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        if (connectionid != null) {
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            ci = cp.getConnectionInfo(connectionid);
        }
        return SapphireReport.getReportLookupHtml(ds, request, ci, servletContext);
    }

    public static String getReportLookupHtml(DataSet ds, HttpServletRequest request, ConnectionInfo connectionInfo, ServletContext servletContext) {
        StringBuffer html = new StringBuffer();
        html.append("<form action=\"rc?command=viewreport&mode=submitselectedreport\" id=\"submitselectedreport\" name=\"submitselectedreport\" method=\"post\">\n");
        html.append("<input type=\"hidden\" name=\"displaytype\" value=\"");
        html.append(request.getParameter("displaytype") == null ? "pdf" : request.getParameter("displaytype"));
        html.append("\">\n");
        html.append("<input type=\"hidden\" id=\"reportid\" name=\"reportid\" value=\"\">\n");
        html.append("<input type=\"hidden\" id=\"reportversionid\" name=\"reportversionid\" value=\"\">\n");
        Map requestParamMap = request.getParameterMap();
        for (String paramname : requestParamMap.keySet()) {
            if ("category".equals(paramname)) continue;
            html.append("<input type=\"hidden\" name=\"").append(paramname).append("\" value=\"").append(request.getParameter(paramname)).append("\">\n");
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String reportid = ds.getString(i, "reportid");
            String reportversionid = ds.getString(i, "reportversionid");
            ds.setString(i, "reportid", "<a href=\"Javascript:submitselectedreport.reportid.value='" + reportid + "';submitselectedreport.reportversionid.value='';submitselectedreport.submit()\">" + reportid + "</a>");
        }
        TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
        html.append(SapphireReport.renderList(ds, tp));
        html.append("\n</form>");
        return SapphireReport.getPromptPage(html.toString(), tp.translate("Report Lookup"), tp.translate("Please select a report") + ":", connectionInfo, request, servletContext);
    }

    public DataSet getParamds() {
        return this.paramds;
    }

    private static StringBuffer renderList(DataSet ds, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        html.append("<table class=\"list_grouptable\" cellspacing=\"0\"> ");
        html.append("<div id=\"data\" height=\"100pt\" style=\"display:block; margin-top: 0em\"> \n");
        html.append("<table id=\"list_list\" class=\"list_table\" cellspacing=\"0\">\n ");
        String[] columns = ds.getColumns();
        html.append("<tr class=\"list_tablehead\">\n ");
        for (int i = 0; i < columns.length; ++i) {
            if (columns[i].startsWith("__")) continue;
            html.append("<td  class=\"list_tableheadcell\" >" + tp.translate(SapphireReport.getTitle(columns[i])) + "</td> ");
        }
        html.append("</tr>");
        for (int row = 0; row < ds.getRowCount(); ++row) {
            if (row % 2 == 0) {
                html.append("<tr name=\"list_tablerow\" class=\"list_tableroweven\"> ");
            } else {
                html.append("<tr name=\"list_tablerow\" class=\"list_tablerowodd\"> ");
            }
            for (int column = 0; column < columns.length; ++column) {
                if (columns[column].startsWith("__")) continue;
                html.append("<td class=\"list_tablebodycell\">" + ds.getValue(row, columns[column], "") + "</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("</div>");
        html.append("</table>");
        return html;
    }

    private static String getTitle(String columnid) {
        if (columnid.equals("reportid")) {
            return "ID";
        }
        if (columnid.equals("reportdesc")) {
            return "Description";
        }
        if (columnid.equals("reportversionid")) {
            return "Version";
        }
        if (columnid.equals("versionstatus")) {
            return "Status";
        }
        return columnid;
    }

    private String substituteValue(String value) {
        if (value != null && value.contains("[currentuser]")) {
            value = StringUtil.replaceAll(value, "[currentuser]", this.connectionInfo.getSysuserId());
        }
        return value;
    }

    public static byte[] addWatermark(byte[] pdfBytes, String watermarkText) throws SapphireException {
        try {
            Document document = new Document((InputStream)new ByteArrayInputStream(pdfBytes));
            FormattedText txtFormatted = new FormattedText();
            txtFormatted.addNewLineText(watermarkText, 2.0f);
            TextStamp textStamp = new TextStamp(txtFormatted);
            textStamp.setBackground(true);
            textStamp.setBottomMargin(50.0);
            textStamp.setVerticalAlignment(3);
            textStamp.setYIndent(0.0);
            textStamp.setXIndent(100.0);
            textStamp.getTextState().setFontSize(10.0f);
            textStamp.setHeight(100.0);
            textStamp.setWidth(650.0);
            textStamp.setOpacity(0.8);
            textStamp.getTextState().setForegroundColor(Color.getLightGray());
            textStamp.setRotateAngle(50.0);
            textStamp.setStampId(111);
            for (int i = 1; i <= document.getPages().size(); ++i) {
                document.getPages().get_Item(i).addStamp((Stamp)textStamp);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            document.save((OutputStream)bos, 0);
            pdfBytes = bos.toByteArray();
            document.close();
            document.dispose();
            bos.flush();
            bos.close();
        }
        catch (Exception e) {
            throw new SapphireException("Error in rendering watermark in report:" + e.getMessage(), e);
        }
        return pdfBytes;
    }

    public static byte[] removeWatermark(byte[] pdfBytes) throws SapphireException {
        try {
            if (pdfBytes != null && pdfBytes.length > 0) {
                PdfContentEditor contentEditor = new PdfContentEditor();
                contentEditor.bindPdf((InputStream)new ByteArrayInputStream(pdfBytes));
                IDocument document = contentEditor.getDocument();
                int totalPage = document.getPages().size();
                for (int i = 1; i <= totalPage; ++i) {
                    StampInfo[] stampInfo = contentEditor.getStamps(i);
                    for (int j = 0; j < stampInfo.length; ++j) {
                        if (stampInfo[j].getStampId() != 111) continue;
                        contentEditor.deleteStampById(stampInfo[j].getStampId());
                    }
                }
                document = contentEditor.getDocument();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                document.save((OutputStream)bos, 0);
                pdfBytes = bos.toByteArray();
                document.dispose();
                bos.flush();
                bos.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Error in removing watermark from report:" + e.getMessage(), e);
        }
        return pdfBytes;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static InputStream removeWatermark(InputStream pdfStream) throws SapphireException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
            if (pdfStream == null || pdfStream.available() <= 0) {
                InputStream inputStream = pdfStream;
                return inputStream;
            }
            PdfContentEditor contentEditor = new PdfContentEditor();
            contentEditor.bindPdf(pdfStream);
            contentEditor.deleteStampById(111);
            IDocument document = contentEditor.getDocument();
            document.saveIncrementally((OutputStream)bos);
            bos.flush();
            contentEditor.close();
            document.dispose();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bos.toByteArray());
            return byteArrayInputStream;
        }
        catch (IOException e) {
            throw new SapphireException("Error in removing watermark from report:" + e.getMessage(), e);
        }
    }

    public List getSupportedDisplayTypes() {
        return this.supportedDisplayTypes;
    }

    public String getDefaultDisplayType() {
        return this.defaultDisplayType;
    }

    public String getReporttypeflag() {
        return this.reporttypeflag;
    }

    public HashMap getSimpleRequestParamsMap(HttpServletRequest request) {
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        String regenerateflag = request.getParameter("regenerate");
        if (regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
            DataSet ds = (DataSet)request.getAttribute("paramvalueds");
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String paramType = "";
                    String paramid = ds.getString(i, "paramid");
                    String paramvalue = ds.getClob(i, "paramvalueclob", "");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("paramid", paramid);
                    int findRow = this.paramds.findRow(filterMap);
                    if (findRow > -1) {
                        paramType = this.paramds.getString(findRow, "paramtype");
                    }
                    if (!paramType.equalsIgnoreCase(PARAM_TYPE_DATEONLY) && paramvalue != null && paramvalue.length() > 0 && paramvalue.indexOf(",") >= 0) {
                        paramvalue = paramvalue.replaceAll(",", ";");
                    }
                    paramsMap.put(paramid, paramvalue);
                }
            }
            paramsMap.put("regenerate", regenerateflag);
            paramsMap.put("parentreporteventid", request.getParameter("reporteventid"));
        } else {
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramKey = (String)e.nextElement();
                paramsMap.put(paramKey, request.getParameter(paramKey));
            }
        }
        return paramsMap;
    }

    public boolean canPrint() {
        return true;
    }

    public String getDisplayFormatHtml(List supportedDisplayTypes, String defaultdisplayType, String alternatedisplaytype) {
        StringBuffer html = new StringBuffer();
        if (defaultdisplayType.length() > 0 || alternatedisplaytype.length() > 0) {
            if (defaultdisplayType.length() > 0) {
                html.append(this.buildOptionHtml(defaultdisplayType, defaultdisplayType, "formatoption", "checked", true));
            }
            if (alternatedisplaytype.length() > 0) {
                html.append(this.buildOptionHtml(alternatedisplaytype, alternatedisplaytype, "formatoption", "", true));
            }
        } else {
            for (int i = 0; i < supportedDisplayTypes.size(); ++i) {
                html.append(this.buildOptionHtml((String)supportedDisplayTypes.get(i), (String)supportedDisplayTypes.get(i), "formatoption", ((String)supportedDisplayTypes.get(i)).equalsIgnoreCase("pdf") ? "Checked" : "", true));
            }
        }
        return html.toString();
    }

    private boolean languageChecked(String language) {
        boolean checked = false;
        if (language.equalsIgnoreCase(this.connectionInfo.getLanguage())) {
            checked = true;
        }
        return checked;
    }

    private boolean timezoneChecked(String timezone) {
        boolean checked = false;
        String timezoneid = "";
        if (OpalUtil.isEmpty(this.connectionInfo.getTimeZone())) {
            TimeZone tz = I18nUtil.getConnectionTimeZone(this.connectionInfo);
            boolean isDayLightSaving = tz.inDaylightTime(new Date());
            timezoneid = tz.getDisplayName(isDayLightSaving, 0);
        } else {
            timezoneid = this.connectionInfo.getTimeZone();
        }
        if (timezone.equalsIgnoreCase(timezoneid)) {
            checked = true;
        }
        return checked;
    }

    private String buildOptionHtml(String optionLabel, String optionValue, String name, String checked, boolean isCaptial) {
        if (this.tp == null) {
            this.tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        }
        StringBuffer html = new StringBuffer();
        html.append("<td><input type=\"radio\" name=\"").append(name).append("\" id=\"").append(SafeHTML.encodeForHTMLAttribute(optionLabel)).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(optionValue)).append("\" ").append(checked).append("></td>");
        html.append("<td class=\"maintform_fieldtitle\"><label for=\"").append(SafeHTML.encodeForHTMLAttribute(optionValue)).append("\">").append(this.tp.translate(isCaptial ? SafeHTML.encodeForHTML(optionLabel.toUpperCase()) : SafeHTML.encodeForHTML(optionLabel))).append("</label></td>");
        return html.toString();
    }

    public List getSupportedLanguages() throws Exception {
        DBUtil dbUtil = new DBUtil();
        SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(this.connectionInfo.getDatabaseId());
        DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
        dbUtil.setConnection(database.getDbms(), dataSource.getConnection());
        dbUtil.createPreparedResultSet("SELECT LANGUAGEID FROM language", new Object[0]);
        while (dbUtil.getNext()) {
            this.supportedLanguages.add(dbUtil.getString("LANGUAGEID"));
        }
        dbUtil.reset();
        dbUtil.releaseConnection();
        return this.supportedLanguages;
    }

    /*
     * Exception decompiling
     */
    public byte[] signReport(byte[] pdfbytes) throws ServiceException, SapphireException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public void signReport(String fileName) throws ServiceException, IOException, SapphireException {
        Path pdfPath = Paths.get(fileName, new String[0]);
        byte[] pdfbytes = Files.readAllBytes(pdfPath);
        byte[] signedArray = this.signReport(pdfbytes);
        if (signedArray != null) {
            try (FileOutputStream out = new FileOutputStream(fileName);){
                ((OutputStream)out).write(signedArray);
            }
        }
    }

    public String getTimezoneHtml(List<String> supportedTimezones) {
        StringBuilder html = new StringBuilder();
        html.append("<SELECT name=\"timezonedropdown\" id=\"timezonedropdown\">");
        html.append("<option VALUE=\"\"></option>");
        for (int i = 0; i < supportedTimezones.size(); ++i) {
            String selected = this.timezoneChecked(supportedTimezones.get(i)) ? "selected" : "";
            html.append("<option VALUE=" + SafeHTML.encodeForHTMLAttribute(supportedTimezones.get(i)) + " " + selected + " >" + SafeHTML.encodeForHTML(supportedTimezones.get(i)) + " </option>");
        }
        return html.toString();
    }

    public List<String> getSupportedTimeZones() throws Exception {
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        DataSet dsRefData = qp.getRefTypeDataSet("Time Zone");
        if (OpalUtil.isNotEmpty(dsRefData)) {
            for (int count = 0; count < dsRefData.size(); ++count) {
                this.supportedTimezones.add(dsRefData.getString(count, "refvalueid"));
            }
        }
        return this.supportedTimezones;
    }

    public String getLanguageHtml(List<String> supportedLanguage) {
        StringBuilder html = new StringBuilder();
        html.append("<SELECT name=\"languagedropdown\" id=\"languagedropdown\">");
        html.append("<option VALUE=\"\"></option>");
        for (int i = 0; i < supportedLanguage.size(); ++i) {
            String selected = this.languageChecked(supportedLanguage.get(i)) ? "selected" : "";
            html.append("<option VALUE=" + supportedLanguage.get(i) + " " + selected + " >" + supportedLanguage.get(i) + " </option>");
        }
        return html.toString();
    }

    protected DataSet getReportParam(String reportid, String reportversionid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        if (OpalUtil.isEmpty(reportversionid)) {
            reportversionid = this.getLatestReportVersionid(reportid);
        }
        query.append(" SELECT * from reportparam").append(" WHERE reportid = ").append(safeSQL.addVar(reportid)).append(" AND reportversionid = ").append(safeSQL.addVar(reportversionid)).append(" order by usersequence");
        return qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
    }

    private String getLatestReportVersionid(String reportid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        query.append(" SELECT * from report").append(" WHERE reportid = ").append(safeSQL.addVar(reportid)).append(" And ( versionstatus='C' or versionstatus='P' )").append(" order by reportid,versionstatus, cast( reportversionid as integer ) desc");
        DataSet ds = qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
        ArrayList<DataSet> groupedReports = ds.getGroupedDataSets("reportid");
        DataSet reportDS = new DataSet();
        reportDS.addColumn("reportid", 0);
        for (int g = 0; g < groupedReports.size(); ++g) {
            DataSet report = groupedReports.get(g);
            int r = reportDS.addRow();
            reportDS.setString(r, "reportid", report.getString(0, "reportid"));
            reportDS.setString(r, "reportversionid", report.getString(0, "reportversionid"));
        }
        return reportDS.getString(0, "reportversionid");
    }

    private DataSet getChildReports(String reportid, String reportVersionid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        query.append(" SELECT * from reportitem").append(" WHERE reportid = ").append(safeSQL.addVar(reportid)).append(" AND reportversionid = ").append(safeSQL.addVar(reportVersionid)).append(" order by usersequence");
        return qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
    }

    public void populateMissingParams(HttpServletRequest request, Map paramsMap, DataSet paramds) {
        for (int p = 0; p < paramds.getRowCount(); ++p) {
            Enumeration e = request.getParameterNames();
            String paraminto = paramds.getString(p, "paraminto");
            String paramid = paramds.getString(p, "paramid");
            while (e.hasMoreElements()) {
                String paramKey = (String)e.nextElement();
                if (paramKey.equals(paramid)) {
                    paramsMap.put(paraminto, request.getParameter(paramKey));
                }
                if (!paramKey.equals(paraminto)) continue;
                paramsMap.put(paraminto, request.getParameter(paramKey));
            }
        }
    }

    protected String resolvedFileName(String fileName) {
        String resolvedFileName = fileName;
        if (fileName.contains(File.separator) && fileName.contains(".")) {
            resolvedFileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.lastIndexOf("."));
        } else if (fileName.contains(File.separator)) {
            resolvedFileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        } else if (fileName.contains(".")) {
            resolvedFileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return resolvedFileName;
    }

    protected SDIData getReportDetails() {
        String sqlWhere = "reportid = '" + this.getReportid() + "' and reportversionid = '" + this.getReportversionid() + "' ";
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("reportitem");
        sdiRequest.setRequestItem("reportparammap");
        sdiRequest.setRequestItem("reportparam");
        sdiRequest.setSDCid("Report");
        sdiRequest.setQueryFrom("report");
        sdiRequest.setQueryWhere(sqlWhere);
        return new SDIProcessor(this.connectionInfo.getConnectionId()).getSDIData(sdiRequest);
    }

    protected SDIData populatedAddressInfo(String addressid) {
        SDIProcessor sdiProcessor = new SDIProcessor(this.connectionInfo.getConnectionId());
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachment");
        sdiRequest.setSDCid("Address");
        sdiRequest.setQueryFrom("address");
        sdiRequest.setQueryWhere("addressid =  '" + addressid + "' AND addresstype = 'ReportAddress'");
        return sdiProcessor.getSDIData(sdiRequest);
    }
}

