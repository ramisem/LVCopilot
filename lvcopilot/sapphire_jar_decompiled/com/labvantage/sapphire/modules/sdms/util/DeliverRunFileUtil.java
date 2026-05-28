/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.util;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCommandHandler;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.services.SecurityService;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.PageTagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DeliverRunFileUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String paramlistid = "";
    private String paramlistversionid = "";
    private String variantid = "";
    private String dataset = "";
    private String instrumentid = "";
    private String contentmode = "";
    private String fixedcontent = "";
    private String reportid = "";
    private String reportversionid = "";
    private String assigninstrument = "";
    private String instrumenttypeid = "";
    private String instrumentmodelid = "";
    private String extraParamsList = "";
    private String dothis;
    private String connectionid;
    private String databaseid;
    private String deliveryFileName = "";
    private ActionProcessor ap;
    private QueryProcessor qp;
    private ConnectionInfo connectionInfo;
    private HashMap<String, String> extraParamMap;
    private PageContext pageContext;
    private HttpServletRequest request;
    private PageTagInfo pageinfo;
    private PropertyList pagedata;
    private TranslationProcessor tp;
    private final String css_rowContentStyle = "border:1px solid #BDCCD4;background-color:white;";
    private final String css_maintFormTable = "maintform_table";
    private final String css_rowHeader = "maintform_fieldtitle";
    private String lookupImage = "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg";
    private final String MODE_PreAssigned = "Pre-Assigned";
    private final String MODE_AssignAndReport = "Assign & Report";
    private final String MODE_AdHoc = "Ad-Hoc";
    private String mode = "Assign & Report";
    private boolean allowoverridereport = true;
    private String reportcategory = "RunFile";
    private boolean isOracle = true;
    private String SDC_QCBatch = "QCBatch";
    private String SDC_Array = "LV_Array";
    private String SDC_SdiData = "DataSet";
    boolean reportSeleceted = false;
    private String languageid = "";

    public DeliverRunFileUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request) {
        this.pageContext = pageContext;
        this.request = request;
        this.pageinfo = pageinfo;
        this.pagedata = pageinfo.getPropertyList("pagedata");
        this.mode = this.pagedata.getProperty("mode", "Assign & Report");
        this.allowoverridereport = this.pagedata.getProperty("allowoverridereport", "Y").equalsIgnoreCase("Y");
        this.reportcategory = this.pagedata.getProperty("reportcategory", "RunFile");
        this.sdcid = request.getParameter("sdcid");
        this.keyid1 = request.getParameter("keyid1");
        this.keyid2 = request.getParameter("keyid2");
        this.keyid3 = request.getParameter("keyid3");
        this.paramlistid = request.getParameter("paramlistid") == null ? "" : request.getParameter("paramlistid");
        this.paramlistversionid = request.getParameter("paramlistversionid") == null ? "" : request.getParameter("paramlistversionid");
        this.variantid = request.getParameter("variantid") == null ? "" : request.getParameter("variantid");
        this.dataset = request.getParameter("dataset") == null ? "" : request.getParameter("dataset");
        this.instrumentid = request.getParameter("instrumentid");
        this.contentmode = request.getParameter("contentmode");
        this.fixedcontent = request.getParameter("fixedcontent");
        this.reportid = request.getParameter("reportid");
        this.reportversionid = request.getParameter("reportversionid");
        this.reportversionid = this.reportversionid == null || this.reportversionid.length() == 0 ? "C" : this.reportversionid;
        this.assigninstrument = request.getParameter("assigninstrument");
        this.extraParamsList = request.getParameter("extraparamslist");
        this.dothis = request.getParameter("dothis");
        this.languageid = request.getParameter("languageid");
        this.deliveryFileName = request.getParameter("deliveryfilename");
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        this.connectionid = requestContext.getConnectionid();
        this.databaseid = SecurityService.getDatabaseId(requestContext.getConnectionid());
        this.isOracle = ((RequestContext)pageContext.getRequest().getAttribute("RequestContext")).getProperty("dbms").equals("ORA");
        this.ap = new ActionProcessor(pageContext);
        this.qp = new QueryProcessor(pageContext);
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        this.connectionInfo = cp.getConnectionInfo(this.connectionid);
        this.tp = new TranslationProcessor(pageContext);
        this.extraParamMap = new HashMap();
        if (this.extraParamsList != null && this.extraParamsList.length() > 0) {
            String[] param = StringUtil.split(this.extraParamsList, ";");
            for (int i = 0; i < param.length; ++i) {
                this.extraParamMap.put(param[i], request.getParameter("param_" + param[i]));
            }
        }
    }

    public String getInstrumentId() {
        return this.instrumentid;
    }

    public String getAssignedInstrumentId() {
        return this.assigninstrument;
    }

    public String getReportId() {
        return this.reportid;
    }

    public String getContentMode() {
        return this.contentmode;
    }

    public String getFixedContent() {
        return this.fixedcontent;
    }

    public String getDeliveryFileName() {
        return this.deliveryFileName;
    }

    public String getReportVersionId() {
        return this.reportversionid == null || this.reportversionid.length() == 0 ? "C" : this.reportversionid;
    }

    public String getExtraParamsList() {
        return this.extraParamsList;
    }

    public String getHtml() throws SapphireException {
        StringBuffer html = new StringBuffer();
        if ("deliverfile".equals(this.dothis)) {
            html.append(this.deliverRunFile());
        } else {
            boolean hasCollector = false;
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = null;
            if (this.sdcid.equals(this.SDC_QCBatch)) {
                safeSQL.reset();
                sql = new StringBuffer("SELECT b.instrumentid, m.instrumenttypeid, m.instrumentmodelid ");
                sql.append(" FROM s_qcbatch b LEFT OUTER JOIN s_qcmethod m ");
                sql.append(" ON b.qcmethodid = m.s_qcmethodid AND b.qcmethodversionid = m.s_qcmethodversionid ");
                sql.append(" WHERE b.s_qcbatchid in ( ").append(safeSQL.addIn(this.keyid1, ";")).append(")");
            } else if (this.sdcid.equals(this.SDC_Array)) {
                safeSQL.reset();
                sql = new StringBuffer("SELECT ai.instrumentid, am.instrumenttypeid, am.instrumentmodelid ");
                sql.append(" FROM arrayarraymethoditem ai LEFT OUTER JOIN arraymethod am ");
                sql.append(" ON ai.arraymethodid = am.arraymethodid AND ai.arraymethodversionid = am.arraymethodversionid ");
                sql.append(" WHERE ai.arrayid in ( ").append(safeSQL.addIn(this.keyid1, ";")).append(")");
            } else if (this.sdcid.equals(this.SDC_SdiData)) {
                safeSQL.reset();
                sql = new StringBuffer("SELECT sdi.s_instrumentid instrumentid, pl.s_instrumenttype instrumenttypeid, pl.s_instrumentmodel instrumentmodelid ");
                sql.append(" FROM sdidata sdi LEFT OUTER JOIN paramlist pl ");
                sql.append(" ON sdi.paramlistid = pl.paramlistid AND sdi.paramlistversionid = pl.paramlistversionid AND sdi.variantid = pl.variantid ");
                sql.append(" where sdi.sdidataid in (").append(safeSQL.addIn(this.keyid1, ";")).append(")");
            }
            if (sql != null && sql.length() > 0) {
                DataSet ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.setInstrumentDetails(ds);
            }
            this.instrumentid = this.instrumentid == null ? "" : this.instrumentid;
            html.append("<div style = \"padding:10px\" >");
            html.append("<table cellpadding = \"5px\" cellspacing = \"0\" class=\"maintform_table\" >");
            if (this.mode.equalsIgnoreCase("Pre-Assigned") && this.instrumentid.length() == 0) {
                String warning = "In the PRE-ASSIGNED MODE, SDI(s) must be pre-assigned to an instrument.";
                html.append("<table><tr><td style=\"vertical-align:top\"><span style=\"color:red\">" + this.tp.translate("WARNING") + "</span></td><td>" + this.tp.translate(warning) + "</td></tr></table>");
                return html.toString();
            }
            html.append("<tr>");
            html.append("<td class = \"maintform_fieldtitle\" style=\"vertical-align:top\">" + this.tp.translate("Instrument") + " </td >");
            html.append("<td style = \"border:1px solid #BDCCD4;background-color:white;\">");
            html.append(this.getInstrumentFieldHtml(this.instrumenttypeid, this.instrumentmodelid, this.assigninstrument));
            html.append("</td >");
            html.append("</tr >");
            if (this.instrumentid.length() > 0) {
                DataSet collectorDS = this.qp.getPreparedSqlDataSet("SELECT s.*,i.instrumenttype,i.instrumentmodelid,i.instrumentstatus FROM  instrument i left join sdmscollector s on s.sdmscollectorid=i.sdmscollectorid where i.instrumentid=?", (Object[])new String[]{this.instrumentid});
                String type = "";
                String model = "";
                String instrumentstatus = "";
                String collectorid = "";
                if (collectorDS.size() > 0) {
                    type = collectorDS.getString(0, "instrumenttype", "");
                    model = collectorDS.getString(0, "instrumentmodelid", "");
                    instrumentstatus = collectorDS.getString(0, "instrumentstatus", "");
                    collectorid = collectorDS.getString(0, "sdmscollectorid", "");
                }
                html.append("<tr>");
                html.append("<td class=\"maintform_fieldtitle\" style=\"vertical-align:top\" nowrap>" + this.tp.translate("Instrument Type") + "</td >");
                html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">" + SafeHTML.encodeForHTML(type) + "</td >");
                html.append("</tr >");
                html.append("<tr>");
                html.append("<td class=\"maintform_fieldtitle\" style=\"vertical-align:top\" nowrap>" + this.tp.translate("Instrument Model") + "</td >");
                html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">" + model + "</td >");
                html.append("</tr >");
                html.append("<tr>");
                html.append("<td class=\"maintform_fieldtitle\" style=\"vertical-align:top\" nowrap>" + this.tp.translate("Instrument Status") + "</td >");
                html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">" + instrumentstatus + "</td >");
                html.append("</tr >");
                html.append("<tr>");
                html.append("<td class=\"maintform_fieldtitle\" style=\"vertical-align:top\">" + this.tp.translate("Collector") + "</td>");
                html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">");
                if (collectorid.length() > 0) {
                    long since;
                    html.append("<table>");
                    html.append("<tr><td nowrap>" + collectorDS.getValue(0, "sdmscollectorid") + "</td>");
                    String status = "";
                    String warning = "";
                    Calendar lastPing = collectorDS.getCalendar(0, "lastpingdt");
                    Calendar now = Calendar.getInstance();
                    long l = since = lastPing == null ? 999999999L : now.getTimeInMillis() - lastPing.getTimeInMillis();
                    if ("Y".equals(collectorDS.getValue(0, "disabled"))) {
                        status = "Disabled";
                        warning = "The collector is disabled. Your file will not be delivered until it is enabled.";
                    } else if ("Y".equals(collectorDS.getValue(0, "paused"))) {
                        status = "Paused";
                        warning = "The collector is paused. Your file will not be delivered until it is resumed.";
                    } else if (since > 63000L) {
                        status = "(" + this.tp.translate("Not Found") + ")";
                        warning = "The collector has not responded for over a minute. Your file might not get delivered straight away.";
                    } else {
                        status = "Running";
                    }
                    html.append("<td style=\"padding-left:10px\">" + this.tp.translate("Status:") + this.tp.translate(status) + "</td></tr>");
                    if (warning.length() > 0) {
                        html.append("<tr><td style=\"vertical-align:top\"><span style=\"color:red\">" + this.tp.translate("WARNING") + "</span></td><td>" + this.tp.translate(warning) + "</td></tr>");
                    }
                    hasCollector = true;
                    html.append("</table>");
                } else {
                    html.append(this.tp.translate("No SDMS Collector found"));
                }
                html.append("</td></tr>");
                if (hasCollector) {
                    DataSet instrumentDS = DeliverRunFileUtil.getInstrumentDetails(this.instrumentid, this.connectionid);
                    this.setContentModeAndFixedContent(instrumentDS);
                    if (this.contentmode.equalsIgnoreCase("Report")) {
                        html.append("<tr ><td class=\"maintform_fieldtitle\" style=\"vertical-align:top\">" + this.tp.translate("Report") + "</td>");
                        html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">");
                        html.append("<table>");
                        html.append("<tr><td>");
                        List<String> reports = DeliverRunFileUtil.getReportsForSDC(instrumentDS, this.sdcid);
                        if ((this.reportid == null || this.reportid.length() == 0) && reports != null && reports.size() == 1) {
                            this.reportid = reports.get(0).split(";")[0];
                            this.reportversionid = reports.get(0).split(";")[1];
                        }
                        if (reports == null || reports.size() == 0 || this.allowoverridereport) {
                            html.append(this.getReportHtml());
                        } else if (reports.size() == 1) {
                            this.reportSeleceted = true;
                            String r = reports.get(0).split(";")[0];
                            String v = reports.get(0).split(";")[1];
                            html.append(r + " (Ver: " + v + ")");
                            html.append("<input type=\"hidden\" id=\"reportlist\" value=\"" + r + ";" + v + "\">");
                        } else {
                            html.append("<select id=\"reportlist\" onchange=\"doRefresh( 'r')\">");
                            html.append("<option value=\"\"/>");
                            String tempReportVersion = "C".equalsIgnoreCase(this.reportversionid) ? DeliverRunFileUtil.getCVersion(this.qp, this.reportid) : this.reportversionid;
                            for (int i = 0; i < reports.size(); ++i) {
                                String val = reports.get(i);
                                String r = val.split(";")[0];
                                String v = val.split(";")[1];
                                if (!this.reportSeleceted && r.equals(this.reportid) && v.equals(tempReportVersion)) {
                                    this.reportSeleceted = true;
                                }
                                html.append("<option value=\"" + val + "\"" + (r.equals(this.reportid) && v.equals(tempReportVersion) ? " selected" : "") + ">");
                                html.append(r + " (Ver:" + v + ")");
                                html.append("</option>");
                            }
                            html.append("</select>");
                        }
                        html.append("</td></tr>");
                        html.append(this.getReportInputHtml());
                        html.append("</td></tr></table>");
                        html.append("</td>");
                        html.append("</tr>");
                    } else if (this.contentmode.equalsIgnoreCase("Fixed")) {
                        html.append("<tr ><td class=\"maintform_fieldtitle\" style=\"vertical-align:top\">" + this.tp.translate("Fixed Content") + "</td>");
                        html.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\">");
                        html.append("<table><tr><td>");
                        html.append("<textarea rows=\"3\" cols=\"30\" onchange=\"updateFixedContent(this.value)\" oninput=\"updateFixedContent(this.value)\" name=\"fixedcontent\" id=\"fixedcontent\">" + this.fixedcontent + "</textarea>");
                        html.append("</td></tr></table>");
                        html.append("</td>");
                        html.append("</tr>");
                    }
                }
            }
            html.append("</table >");
            html.append(" <br >");
            if (this.instrumentid != null && this.instrumentid.length() > 0 && hasCollector && ("Report".equalsIgnoreCase(this.contentmode) && this.reportSeleceted || "Fixed".equalsIgnoreCase(this.contentmode))) {
                html.append("<div id = \"sendbutton\" >");
                html.append(this.getButton("Deliver File", "deliverFile()", "rc?command=image&image=FlatBlackBulletList4", "deliverfile", "Deliver File"));
                html.append("</div >");
            }
            html.append("</div >");
        }
        return html.toString();
    }

    private void setInstrumentDetails(DataSet ds) {
        if (ds != null) {
            boolean sameInstr = true;
            boolean sameType = true;
            boolean sameModel = true;
            if (ds.size() > 1) {
                sameInstr = DeliverRunFileUtil.areAllSame(ds, "instrumentid");
                sameType = DeliverRunFileUtil.areAllSame(ds, "instrumenttypeid");
                sameModel = DeliverRunFileUtil.areAllSame(ds, "instrumentmodelid");
            }
            if (sameInstr) {
                String assignedInstrumentid = ds.getValue(0, "instrumentid", "");
                if (this.assigninstrument == null || this.assigninstrument.length() == 0) {
                    this.assigninstrument = assignedInstrumentid;
                }
                if (this.instrumentid == null || this.instrumentid.length() == 0) {
                    this.instrumentid = assignedInstrumentid;
                }
            }
            if (sameType) {
                this.instrumenttypeid = ds.getValue(0, "instrumenttypeid", "");
            }
            if (sameModel) {
                this.instrumentmodelid = ds.getValue(0, "instrumentmodelid", "");
            }
        }
    }

    public static boolean areAllSame(DataSet ds, String fieldname) {
        boolean same = true;
        String ins = ds.getString(0, fieldname, "");
        for (int i = 1; i < ds.size(); ++i) {
            if (ins.equalsIgnoreCase(ds.getString(i, fieldname, ""))) continue;
            same = false;
            break;
        }
        return same;
    }

    private String getReportHtml() throws SapphireException {
        StringBuffer reportHtml = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select report.reportid,report.reportversionid from report,categoryitem ci");
        sql.append(" where ci.categoryid=").append(safeSQL.addVar(this.reportcategory));
        sql.append(" and ci.sdcid='Report' and ci.keyid1=report.reportid");
        sql.append(" and report.sdcidvalue=").append(safeSQL.addVar(this.sdcid));
        String tempReportVersion = "C".equalsIgnoreCase(this.reportversionid) ? DeliverRunFileUtil.getCVersion(this.qp, this.reportid) : this.reportversionid;
        DataSet reportDs = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (reportDs != null && reportDs.size() > 0) {
            reportHtml.append("<select id=\"reportlist\" onchange=\"doRefresh( 'r')\">");
            reportHtml.append("<option value=\"\"/>");
            for (int i = 0; i < reportDs.size(); ++i) {
                String r = reportDs.getString(i, "reportid", "");
                String v = reportDs.getString(i, "reportversionid", "");
                String val = r + ";" + v;
                if (!this.reportSeleceted && r.equals(this.reportid) && v.equals(tempReportVersion)) {
                    this.reportSeleceted = true;
                }
                reportHtml.append("<option value=\"" + val + "\"" + (r.equals(this.reportid) && v.equals(tempReportVersion) ? " selected" : "") + ">");
                reportHtml.append(r + " (Ver:" + v + ")");
                reportHtml.append("</option>");
            }
            reportHtml.append("</select>");
        } else {
            this.reportid = "";
            this.reportversionid = "";
            reportHtml.append(this.tp.translate("No suitable reports found. Contact your system administrator for assistance."));
        }
        return reportHtml.toString();
    }

    private String getReportInputHtml() throws SapphireException {
        StringBuffer reportInputHtml = new StringBuffer();
        if (this.reportid != null && this.reportid.length() > 0 && this.reportSeleceted) {
            DataSet instrumentDS = DeliverRunFileUtil.getInstrumentDetails(this.instrumentid, this.connectionid);
            PropertyList collectorTypeProps = new PropertyList();
            collectorTypeProps.setPropertyList(instrumentDS.getValue(0, "_collectorrules"));
            PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
            PropertyListCollection reportRules = deliveryProps.getCollectionNotNull("reportrules");
            if (reportRules.size() > 0) {
                for (int i = 0; i < reportRules.size(); ++i) {
                    PropertyList report = reportRules.getPropertyList(i);
                    if (!report.getProperty("sdcid", this.sdcid).equals(this.sdcid) || !report.getProperty("reportid", this.reportid).equals(this.reportid) || !report.getProperty("reportversionid", this.reportversionid).equals(this.reportversionid)) continue;
                    this.deliveryFileName = report.getProperty("deliveryfilename", "");
                    break;
                }
            } else {
                this.deliveryFileName = deliveryProps.getProperty("deliveryfilename", "");
            }
            String tempReportVersion = "C".equalsIgnoreCase(this.reportversionid) ? DeliverRunFileUtil.getCVersion(this.qp, this.reportid) : this.reportversionid;
            SapphireReport sr = SapphireReport.getIntance(this.reportid, tempReportVersion, this.connectionInfo, this.languageid, false, "");
            DataSet paramsDS = sr.getParamds();
            this.removeParam(paramsDS, "sdcid");
            this.removeParam(paramsDS, "keyid1");
            this.removeParam(paramsDS, "keyid2");
            this.removeParam(paramsDS, "keyid3");
            this.extraParamsList = paramsDS.getColumnValues("paraminto", ";");
            if (paramsDS.size() > 0) {
                reportInputHtml.append("<tr><td>&nbsp;</td></tr>");
                reportInputHtml.append("<tr><td>");
                PropertyList propertyList = new PropertyList();
                if (this.paramlistid.length() > 0) {
                    propertyList.setProperty("paramlistid", this.paramlistid);
                }
                if (this.paramlistversionid.length() > 0) {
                    propertyList.setProperty("paramlistversionid", this.paramlistversionid);
                }
                if (this.variantid.length() > 0) {
                    propertyList.setProperty("variantid", this.variantid);
                }
                if (this.dataset.length() > 0) {
                    propertyList.setProperty("dataset", this.dataset);
                }
                String promptHtml = sr.getPrompts(this.request, this.pageContext, true, propertyList);
                reportInputHtml.append(promptHtml);
                reportInputHtml.append("</td></tr>");
            }
        }
        return reportInputHtml.toString();
    }

    private String deliverRunFile() throws SapphireException {
        PropertyList props;
        StringBuffer deliverHtml = new StringBuffer();
        if (this.mode.equalsIgnoreCase("Assign & Report")) {
            DataSet ds;
            StringBuffer sql;
            props = new PropertyList();
            SafeSQL safeSQL = new SafeSQL();
            if (this.sdcid.equals(this.SDC_QCBatch)) {
                props.setProperty("sdcid", this.sdcid);
                props.setProperty("keyid1", this.keyid1);
                props.setProperty("instrumentid", this.instrumentid);
                props.setProperty("deliveredrunfileflag", "Y");
                this.ap.processAction("EditSDI", "1", props);
                sql = new StringBuffer("select * from sdidata");
                sql.append(" where s_qcbatchid in (").append(safeSQL.addIn(this.keyid1, ";")).append(")");
                ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.updateSDIDataInstrument(ds);
            } else if (this.sdcid.equals(this.SDC_Array)) {
                props.setProperty("arrayid", this.keyid1);
                props.setProperty("instrumentid", this.instrumentid);
                this.ap.processAction("SendToInstrument", "1", props);
            } else if (this.sdcid.equals(this.SDC_SdiData)) {
                sql = new StringBuffer("select * from sdidata");
                sql.append(" where sdidataid in (").append(safeSQL.addIn(this.keyid1, ";")).append(")");
                ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.updateSDIDataInstrument(ds);
            } else if (this.sdcid.equals("Sample")) {
                ds = new DataSet();
                String[] keyid1Arr = null;
                String[] paramListIdArr = null;
                String[] paramListVersionIdArr = null;
                String[] variantIdArr = null;
                String[] datasetArr = null;
                if (this.keyid1.length() > 0) {
                    keyid1Arr = StringUtil.split(this.keyid1, ";");
                }
                if (this.paramlistid.length() > 0) {
                    paramListIdArr = StringUtil.split(this.paramlistid, ";");
                }
                if (this.paramlistversionid.length() > 0) {
                    paramListVersionIdArr = StringUtil.split(this.paramlistversionid, ";");
                }
                if (this.variantid.length() > 0) {
                    variantIdArr = StringUtil.split(this.variantid, ";");
                }
                if (this.dataset.length() > 0) {
                    datasetArr = StringUtil.split(this.dataset, ";");
                }
                for (int i = 0; i < paramListIdArr.length; ++i) {
                    int row = ds.addRow();
                    ds.setString(row, "sdcid", "Sample");
                    ds.setString(row, "keyid1", keyid1Arr[i]);
                    ds.setString(row, "paramlistid", paramListIdArr[i]);
                    ds.setString(row, "paramlistversionid", paramListVersionIdArr[i]);
                    ds.setString(row, "variantid", variantIdArr[i]);
                    ds.setString(row, "dataset", datasetArr[i]);
                }
                this.updateSDIDataInstrument(ds);
            }
        }
        props = new PropertyList();
        props.setProperty("instrumentid", this.instrumentid);
        props.setProperty("contentmode", this.contentmode);
        props.setProperty("fixedcontent", this.fixedcontent);
        props.setProperty("reportid", this.reportid);
        props.setProperty("reportversionid", this.reportversionid);
        props.setProperty("sdcid", this.sdcid);
        props.setProperty("keyid1", this.keyid1);
        props.setProperty("keyid2", this.keyid2);
        props.setProperty("keyid3", this.keyid3);
        if (this.deliveryFileName != null && this.deliveryFileName.trim().length() > 0) {
            String fileName = this.deliveryFileName;
            props.setProperty("filename", fileName);
        }
        String[] extraParams = StringUtil.split(this.extraParamsList, ";");
        for (int i = 0; i < extraParams.length; ++i) {
            props.setProperty(extraParams[i], this.extraParamMap.get(extraParams[i]));
        }
        if (this.paramlistid.length() > 0) {
            props.setProperty("paramlistid", this.paramlistid);
        }
        if (this.paramlistversionid.length() > 0) {
            props.setProperty("paramlistversionid", this.paramlistversionid);
        }
        if (this.variantid.length() > 0) {
            props.setProperty("variantid", this.variantid);
        }
        if (this.dataset.length() > 0) {
            props.setProperty("dataset", this.dataset);
        }
        try {
            this.ap.processAction("DeliverRunFile", "1", props);
            String collectorid = props.getProperty("collectorid");
            String collectorCommandid = props.getProperty("collectorcommandid");
            String filename = props.getProperty("logicalfilename");
            deliverHtml.append("<div style=\"padding:5px\">");
            deliverHtml.append(this.tp.translate("RunFile ") + filename + this.tp.translate(" has been queued for delivery.") + "<br><br>");
            deliverHtml.append(this.tp.translate("You can close this dialog or wait for confirmation that the file has been delivered sucessfully."));
            deliverHtml.append("<br><br><div id=\"checktimerdiv\">" + this.tp.translate("Checking") + "...</div>");
            deliverHtml.append("</div>");
            deliverHtml.append("<script>");
            deliverHtml.append("var collectorid='" + collectorid + "';");
            deliverHtml.append("var collectorcommandid='" + collectorCommandid + "';");
            deliverHtml.append("var checkMessageInterval;");
            deliverHtml.append("window.setTimeout( function() { startCheckTimer() }, 1000 );");
            deliverHtml.append("</script>");
        }
        catch (Exception e) {
            deliverHtml.append(this.tp.translate("Failed to send the file: ") + e.getMessage());
        }
        return deliverHtml.toString();
    }

    private void updateSDIDataInstrument(DataSet ds) throws SapphireException {
        PropertyList props = new PropertyList();
        if (ds != null && ds.size() > 0) {
            props.setProperty("sdcid", ds.getString(0, "sdcid", ";"));
            props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
            props.setProperty("s_instrumentid", this.instrumentid);
            props.setProperty("deliveredrunfileflag", "Y");
            props.setProperty("propsmatch", "Y");
            this.ap.processAction("EditDataSet", "1", props);
        }
    }

    public static DataSet getInstrumentDetails(String instrumentid, String connectionid) throws SapphireException {
        try {
            String databaseid = SecurityService.getDatabaseId(connectionid);
            SDMSCommandHandler sdmsCommandHandler = SDMSCommandHandler.getInstance(connectionid, databaseid);
            PropertyList commandRequest = new PropertyList();
            commandRequest.setProperty("instrumentid", instrumentid);
            PropertyList configProps = sdmsCommandHandler.processCommand("COMMAND_GETINSTRUMENTCONFIGPROPS", commandRequest);
            return new DataSet(new JSONObject(configProps.getProperty("instruments_dataset")));
        }
        catch (Exception e) {
            throw new SapphireException("Unable to determine instrument details: " + e.getMessage());
        }
    }

    public static List<String> getReportsForSDC(DataSet instrumentDS, String sdcid) throws SapphireException {
        try {
            PropertyList collectorTypeProps = new PropertyList();
            collectorTypeProps.setPropertyList(instrumentDS.getValue(0, "_collectorrules"));
            String objectname = instrumentDS.getValue(0, "_objectname");
            if (objectname.length() == 0) {
                return null;
            }
            BaseCollectorType collector = (BaseCollectorType)Class.forName(objectname).newInstance();
            return collector.getReportsForSDC(collectorTypeProps, sdcid);
        }
        catch (Exception e) {
            throw new SapphireException("Unable to determine list of reports: " + e.getMessage());
        }
    }

    private void removeParam(DataSet params, String paramInto) {
        int row = params.findRow("paraminto", paramInto);
        if (row >= 0) {
            params.deleteRow(row);
        }
    }

    protected String getButton(String text, String js, String img, String buttonid, String tip) {
        Button buttonObj = new Button(this.pageContext);
        buttonObj.setText(this.tp.translate(text));
        buttonObj.setAction(js);
        buttonObj.setTip(tip);
        if (img.length() > 0) {
            buttonObj.setImg(img);
        }
        buttonObj.setId(buttonid);
        return buttonObj.getHtml();
    }

    private String getInstrumentFieldHtml(String instrumenttypeid, String instrumentmodelid, String assignedInstrumentid) {
        DataSet instruments;
        StringBuffer insHtml = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT instrumentid,instrumenttype,instrumentmodelid FROM instrument";
        String where = "";
        if (instrumenttypeid.length() > 0) {
            sql = sql + " where instrumenttype=" + safeSQL.addVar(instrumenttypeid);
            where = " instrumenttype='" + instrumenttypeid + "'";
        }
        if (instrumentmodelid.length() > 0) {
            sql = sql + (where.length() == 0 ? " WHERE " : " AND ") + " instrumentmodelid=" + safeSQL.addVar(instrumentmodelid);
            where = where + (where.length() == 0 ? "" : " AND ") + " instrumentmodelid='" + instrumentmodelid + "'";
        }
        if ((instruments = this.qp.getPreparedSqlDataSet(sql, safeSQL.getValues())).size() > 0) {
            if (this.mode.equalsIgnoreCase("Ad-Hoc") || this.mode.equalsIgnoreCase("Assign & Report")) {
                insHtml.append("<input style=\"border:1px solid green\" onchange=\"doRefresh( 'i')\" readonly=\"\" edit=\"lookup\" type=\"text\" class=\"input_field\" onkeyup=\";showSuggestion()\"  value=\"" + this.instrumentid + "\" name=\"instrumentid\" id=\"instrumentid\"  onfocus=\"\" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
                insHtml.append(this.getJSScriptForDDD("instrumentid", where));
                if (this.mode.equalsIgnoreCase("Ad-Hoc")) {
                    insHtml.append("<a style=\"display:inline;\" id=\"instrumentid\" href=\"/Lookup\" onClick=\"openInstrumentLookup('InstrumentDRFOLookup','AllInstrumentsForRunFile','','');return false\" tabindex=\"0\"><img title=\"Instrument Lookup\" border=\"0\" src=\"" + this.lookupImage + "\" class=\"lookup_img\"></a>");
                } else {
                    insHtml.append("<a style=\"display:inline;\" id=\"instrumentid\" href=\"/Lookup\" onClick=\"openInstrumentLookup('InstrumentDRFOLookup','CertifiedInstrumentsForRunFile','" + instrumenttypeid + "','" + instrumentmodelid + "');return false\" tabindex=\"0\"><img title=\"Instrument Lookup\" border=\"0\" src=\"" + this.lookupImage + "\" class=\"lookup_img\"></a>");
                }
            } else if (this.mode.equalsIgnoreCase("Pre-Assigned")) {
                insHtml.append(this.instrumentid);
            } else {
                insHtml.append("<select id=\"instrumentid\" onchange=\"doRefresh( 'i')\">");
                insHtml.append("<option value=\"\"/>");
                for (int i = 0; i < instruments.size(); ++i) {
                    String instid = instruments.getValue(i, "instrumentid");
                    insHtml.append("<option value=\"" + instid + "\"" + (instid.equals(this.instrumentid) ? " selected" : "") + ">");
                    insHtml.append(instid.equals(assignedInstrumentid) ? instid + " (Assigned)" : instid);
                    insHtml.append("</option>");
                }
                insHtml.append("</select>");
            }
        } else {
            insHtml.append(this.tp.translate("Unable to find a valid, active instrument"));
        }
        return insHtml.toString();
    }

    private String getJSScriptForDDD(String id, String restrictivewhere) {
        String sdcid = "Instrument";
        return "<script type=\"text/javascript\">var oLUPD_" + id + " = {\"selectortype\":\"\",\"sdcid\":\"" + sdcid + "\", restrictivewhere: \"" + this.getInstrumentCertWhereClause(restrictivewhere, this.isOracle) + "\"};</script>";
    }

    private String getInstrumentCertWhereClause(String queryWhere, boolean isOracle) {
        if (this.mode.equalsIgnoreCase("Ad-Hoc")) {
            return "";
        }
        return (queryWhere != null && queryWhere.length() > 0 ? "(" + queryWhere + ") AND " : "") + "  (instrument.inserviceflag is null OR instrument.inserviceflag!='N') AND ( instrument.certificationreqflag is null OR instrument.certificationreqflag='N' OR (instrument.certificationreqflag='P' AND instrument.instrumentstatus='Available') OR EXISTS (SELECT S_SDICERTIFICATION.RESOURCEKEYID1 FROM s_sdicertification   WHERE S_SDICERTIFICATION.CERTIFICATIONTYPE = 'Instrument'     AND S_SDICERTIFICATION.CERTIFICATIONSTATUS = 'Valid'     AND S_SDICERTIFICATION.RESOURCESDCID = 'Instrument'      AND S_SDICERTIFICATION.RESOURCEKEYID1 = INSTRUMENT.INSTRUMENTID      AND (S_SDICERTIFICATION.EXPIRATIONDT IS NULL          OR " + (isOracle ? "( SYSDATE < DECODE(S_SDICERTIFICATION.GRACEPERIODUNITS, 'Days', S_SDICERTIFICATION.EXPIRATIONDT+NVL(S_SDICERTIFICATION.GRACEPERIOD,0),                         'Weeks', S_SDICERTIFICATION.EXPIRATIONDT+7*NVL(S_SDICERTIFICATION.GRACEPERIOD,0),                        'Months', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, NVL(S_SDICERTIFICATION.GRACEPERIOD,0)),                         'Years', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, 12*NVL(S_SDICERTIFICATION.GRACEPERIOD,0)),                        S_SDICERTIFICATION.EXPIRATIONDT)            )" : "            ( GETDATE() <  CASE  S_SDICERTIFICATION.GRACEPERIODUNITS                                WHEN 'Days' THEN DATEADD( DAY, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Weeks' THEN DATEADD( WEEK, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Months' THEN DATEADD( MONTH, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Years' THEN DATEADD( YEAR, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    ELSE S_SDICERTIFICATION.EXPIRATIONDT                                   END                        )    ") + "    ) ) )";
    }

    public static String getCVersion(QueryProcessor qp, String reportid) {
        String reportversionid;
        DataSet ds = qp.getPreparedSqlDataSet("SELECT reportversionid, versionstatus FROM report WHERE reportid=? order by reportversionid", (Object[])new String[]{reportid});
        int row = ds.findRow("versionstatus", "C");
        if (row >= 0) {
            reportversionid = ds.getValue(row, "reportversionid");
        } else {
            ds.sort("reportversionid d");
            row = ds.findRow("versionstatus", "P");
            reportversionid = ds.getValue(row >= 0 ? row : 0, "reportversionid");
        }
        return reportversionid;
    }

    private void setContentModeAndFixedContent(DataSet instrumentDS) throws SapphireException {
        try {
            PropertyList collectorTypeProps = new PropertyList();
            collectorTypeProps.setPropertyList(instrumentDS.getValue(0, "_collectorrules"));
            PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
            this.contentmode = deliveryProps.getProperty("contentmode", "");
            this.fixedcontent = deliveryProps.getProperty("fixedcontent", "");
            this.deliveryFileName = deliveryProps.getProperty("deliveryfilename", "");
            if (this.fixedcontent.length() > 0) {
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[keyid1]", this.keyid1);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[keyid2]", this.keyid2);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[keyid3]", this.keyid3);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[sdcid]", this.sdcid);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[currentuser]", this.connectionInfo.getSysuserId());
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[paramlistid]", this.paramlistid);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[paramlistversionid]", this.paramlistversionid);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[variantid]", this.variantid);
                this.fixedcontent = StringUtil.replaceAll(this.fixedcontent, "[dataset]", this.dataset);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Unable to determine content mode: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }
}

