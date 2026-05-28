/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRDataSource
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRField
 *  net.sf.jasperreports.engine.JasperCompileManager
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.data.JRMapCollectionDataSource
 *  net.sf.jasperreports.engine.design.JRDesignBand
 *  net.sf.jasperreports.engine.design.JRDesignElement
 *  net.sf.jasperreports.engine.design.JRDesignExpression
 *  net.sf.jasperreports.engine.design.JRDesignField
 *  net.sf.jasperreports.engine.design.JRDesignSection
 *  net.sf.jasperreports.engine.design.JRDesignTextField
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.type.HorizontalTextAlignEnum
 *  net.sf.jasperreports.engine.type.ModeEnum
 *  net.sf.jasperreports.engine.type.SplitTypeEnum
 *  net.sf.jasperreports.engine.type.StretchTypeEnum
 *  net.sf.jasperreports.engine.xml.JRXmlLoader
 *  org.apache.commons.io.output.FileWriterWithEncoding
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.ListReportDesigner;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.output.FileWriterWithEncoding;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReportGenerator {
    private static HashMap<String, SoftReference> jasperPrintHashMap = new HashMap();
    private static ArrayList<File> previousTempList = new ArrayList();

    public static DataSet copyDataValues(DataSet ds, PropertyList element, PageContext pageContext) {
        DataSet newds = new DataSet();
        newds.setColidCaseSensitive(true);
        int rows = ds.size();
        int cols = ds.getColumnCount();
        String[] columnids = new String[cols];
        for (int i = 0; i < cols; ++i) {
            String columnid;
            columnids[i] = columnid = ds.getColumnId(i);
            newds.addColumn(columnid, 0);
        }
        QueryProcessor qp = null;
        HashMap<String, String> displayvaluesMap = new HashMap<String, String>();
        PropertyListCollection columns = element.getCollection("columns");
        if (columns != null) {
            for (int i = 0; i < columns.size(); ++i) {
                DataSet displayvalueds;
                PropertyList column = columns.getPropertyList(i);
                if (column.getProperty("displayvalue") != null && column.getProperty("displayvalue").length() > 0) {
                    displayvaluesMap.put(column.getProperty("columnid"), column.getProperty("displayvalue"));
                    continue;
                }
                if (column.getProperty("sql") == null || column.getProperty("sql").length() <= 0) continue;
                if (qp == null) {
                    qp = new QueryProcessor(pageContext);
                }
                if ((displayvalueds = qp.getSqlDataSet(column.getProperty("sql"))) == null || displayvalueds.getColumns().length != 2) continue;
                String rcolid = displayvalueds.getColumns()[0];
                String dcolid = displayvalueds.getColumns()[1];
                StringBuffer svalues = new StringBuffer();
                for (int r = 0; r < displayvalueds.getRowCount(); ++r) {
                    String rvalue = displayvalueds.getValue(r, rcolid);
                    String dvalue = displayvalueds.getValue(r, dcolid);
                    if (dvalue == null || dvalue.length() <= 0) continue;
                    svalues.append(";" + rvalue + "=" + dvalue);
                }
                if (svalues.length() <= 1) continue;
                displayvaluesMap.put(column.getProperty("columnid"), svalues.substring(1));
            }
        }
        for (int row = 0; row < rows; ++row) {
            int newRow = newds.addRow();
            for (int i = 0; i < columnids.length; ++i) {
                String columnid = columnids[i];
                String displayvalue = ds.getValue(row, columnid);
                if (displayvaluesMap.get(columnid) != null) {
                    String displayvaluelist = (String)displayvaluesMap.get(columnid);
                    displayvalue = SDITagUtil.getDisplayValue(displayvalue, displayvaluelist);
                }
                newds.setObject(newRow, columnid, displayvalue);
            }
        }
        return newds;
    }

    public static void exportToPDF(DataSet ds, PropertyList element, PageContext pageContext) {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        ConnectionInfo connectionInfo = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionid());
        try {
            File file = new File(SapphireJasperUtil.REPORT_ROOT + "/OOB/sapphireListPage.jrxml");
            JasperDesign jasperDesign = null;
            if (file.exists()) {
                jasperDesign = JRXmlLoader.load((File)file);
                jasperDesign.setQuery(null);
            }
            SapphireJasperUtil.checkDynamicGroupBy(element, pageContext.getRequest());
            ListReportDesigner reportDesigner = new ListReportDesigner("Adhoc Query Result Report", element, jasperDesign);
            ds = ReportGenerator.copyDataValues(ds, element, pageContext);
            reportDesigner.setExampleData(ds);
            reportDesigner.setDisplayType("pdf");
            jasperDesign = reportDesigner.modifyJasperDesign();
            SapphireJasperUtil.setJasperCompilerClassPatch();
            JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
            JRMapCollectionDataSource datasource = new JRMapCollectionDataSource((Collection)ds);
            CommonParamMap paramsMap = new CommonParamMap(pageContext);
            paramsMap.put("displayType", "pdf");
            String contentHeader = ReportGenerator.getContentDispositionHeader("AdhocExport", "pdf");
            ((HttpServletResponse)pageContext.getResponse()).setHeader("Content-Disposition", contentHeader);
            SapphireJasperUtil.runReportToWebPdf(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), (HttpServletResponse)pageContext.getResponse(), "", connectionInfo, "", false);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public static void exportToExcel(DataSet ds, PropertyList element, PageContext pageContext) {
        try {
            long starttime = System.currentTimeMillis();
            ListReportDesigner reportDesigner = new ListReportDesigner("Adhoc Query Result Report", element);
            ds = ReportGenerator.copyDataValues(ds, element, pageContext);
            reportDesigner.setExampleData(ds);
            reportDesigner.setDisplayType("xls");
            JasperDesign jasperDesign = reportDesigner.createJasperDesign();
            SapphireJasperUtil.setJasperCompilerClassPatch();
            JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
            JRMapCollectionDataSource datasource = new JRMapCollectionDataSource((Collection)ds);
            CommonParamMap paramsMap = new CommonParamMap(pageContext);
            JasperPrint jasperPrint = SapphireJasperUtil.getJasperPrint(jasperReport, (Map)paramsMap, (JRDataSource)datasource);
            ReportGenerator.setReportObject(pageContext.getSession(), jasperPrint);
            ReportGenerator.writeReportFrame(pageContext, "xls", ReportGenerator.getContentDispositionHeader("AdhocExport", "xls"), starttime);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void exportToExcel(File adhocResultFile, PropertyList element, PageContext pageContext, HashMap props, long starttime) {
        try {
            ListReportDesigner reportDesigner = new ListReportDesigner("Adhoc Query Result Report", element);
            reportDesigner.setDisplayType("xls");
            JasperDesign jasperDesign = reportDesigner.createJasperDesign();
            SapphireJasperUtil.setJasperCompilerClassPatch();
            JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
            AdhocResultFileDataSource datasource = new AdhocResultFileDataSource(adhocResultFile);
            CommonParamMap paramsMap = new CommonParamMap(pageContext);
            Trace.logInfo("###!!Done creating and compile report in " + (System.currentTimeMillis() - starttime) + "ms");
            JasperPrint jasperPrint = SapphireJasperUtil.getJasperPrint(jasperReport, (Map)paramsMap, datasource);
            Trace.logInfo("###!!Done creating JasperPrint for file " + adhocResultFile.getAbsolutePath() + ", file size:" + adhocResultFile.length() + " in " + (System.currentTimeMillis() - starttime) + "ms");
            adhocResultFile.delete();
            Trace.logInfo("File deleted.");
            ReportGenerator.setReportObject(pageContext.getSession(), jasperPrint);
            ReportGenerator.writeReportFrame(pageContext, "xls", ReportGenerator.getContentDispositionHeader("ExportAll_" + props.get("adhocquerydesc"), "xls"), starttime);
            Trace.logInfo("Done export to Web from file");
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        finally {
            adhocResultFile.delete();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void exportToCSV(File adhocResultFile, PropertyList element, PageContext pageContext, HashMap props, long starttime) {
        File csvFile = null;
        FileWriterWithEncoding fos = null;
        try {
            int i;
            csvFile = File.createTempFile("adhocExportAll", ".csv");
            fos = new FileWriterWithEncoding(csvFile, "UTF-8");
            PropertyListCollection columns = element.getCollection("columns");
            AdhocResultFileDataSource datasource = new AdhocResultFileDataSource(adhocResultFile);
            for (i = 0; i < columns.size(); ++i) {
                String value = columns.getPropertyList(i).getProperty("title");
                fos.write((i > 0 ? "," : "") + (value.indexOf(",") >= 0 || value.indexOf("\r") >= 0 || value.indexOf("\n") >= 0 ? "\"" + value + "\"" : value));
            }
            fos.write("\r\n");
            while (datasource.next()) {
                for (i = 0; i < columns.size(); ++i) {
                    String fieldid = columns.getPropertyList(i).getProperty("columnid");
                    String value = datasource.getFieldValue(fieldid).toString();
                    fos.write((i > 0 ? "," : "") + (value.indexOf(",") >= 0 || value.indexOf("\r") >= 0 || value.indexOf("\n") >= 0 ? "\"" + value + "\"" : value));
                }
                fos.write("\r\n");
            }
            fos.close();
            ReportGenerator.setReportObject(pageContext.getSession(), csvFile);
            ReportGenerator.writeReportFrame(pageContext, "csv", ReportGenerator.getContentDispositionHeader("ExportAll_" + props.get("adhocquerydesc"), "csv"), starttime);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Throwable throwable) {}
            }
            adhocResultFile.delete();
        }
    }

    public static void exportDataEntryGridToExcel(String delimitedString, String delimiter, int colheaderRowCount, int rowheaderColumnCount, PageContext pageContext) {
        try {
            long starttime = System.currentTimeMillis();
            int colcount = StringUtil.split(delimitedString.substring(0, delimitedString.indexOf("\r")), delimiter).length;
            DataEntryGridDataSource datasource = new DataEntryGridDataSource(delimitedString, delimiter);
            JasperDesign jasperDesign = DataEntryGridDataSource.createDataEntryGridExcelDesign(colcount, colheaderRowCount, rowheaderColumnCount);
            SapphireJasperUtil.setJasperCompilerClassPatch();
            JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
            CommonParamMap paramsMap = new CommonParamMap(pageContext);
            JasperPrint jasperPrint = SapphireJasperUtil.getJasperPrint(jasperReport, (Map)paramsMap, datasource);
            Trace.logInfo("###!!Done creating JasperPrint for dataentrygrid in " + (System.currentTimeMillis() - starttime) + "ms");
            ReportGenerator.setReportObject(pageContext.getSession(), jasperPrint);
            ReportGenerator.writeReportFrame(pageContext, "xls", ReportGenerator.getContentDispositionHeader("DataEntryGridExport", "xls"), starttime);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public static synchronized void setReportObject(HttpSession session, Object reportObj) {
        Object obj = ReportGenerator.getReportObject(session);
        if (obj instanceof File) {
            File previousFile = (File)obj;
            if (previousFile != null && previousFile.exists()) {
                previousFile.delete();
            } else {
                previousTempList.add(previousFile);
            }
        }
        if (previousTempList.size() > 0) {
            for (int i = 0; i < previousTempList.size(); ++i) {
                if (!previousTempList.get(i).exists()) continue;
                previousTempList.get(i).delete();
            }
            if (previousTempList.size() > 10) {
                previousTempList.clear();
            }
        }
        jasperPrintHashMap.put(session.getId(), new SoftReference<Object>(reportObj));
    }

    public static Object getReportObject(HttpSession session) {
        return jasperPrintHashMap.get(session.getId()) != null ? jasperPrintHashMap.get(session.getId()).get() : null;
    }

    public static synchronized void removeReportObject(HttpSession session) {
        jasperPrintHashMap.remove(session.getId());
    }

    public static void writeReportFrame(PageContext pageContext, String displaytype, String contentDisposition, long starttime) throws IOException {
        JspWriter out = pageContext.getOut();
        out.write("<iframe id=\"reportframe\" name=\"reportframe\" frameborder=0 width=\"100%\" height=\"800\" src=\"WEB-CORE/blank.html\"></iframe>\n<form name=\"reportrequestform\" target=\"reportframe\" action=\"rc?command=viewreport&displaytype=" + displaytype + "\" method=\"post\">\n    <input name=\"test\" type=\"hidden\" value=\"\"/>\n    <input name=\"contentDisposition\" type=\"hidden\" value=\"" + contentDisposition.replaceAll("\"", "&quot;") + "\"/>\n    <input name=\"starttime\" type=\"hidden\" value=\"" + starttime + "\"/>\n</form>\n<script type=\"text/javascript\">\n    document.reportrequestform.submit();\n" + ("xls".equals(displaytype) ? "    //setTimeout( 'window.close()',2000);\n" : "") + "</script>");
    }

    public static String getContentDispositionHeader(String prefix, String fileext) {
        String filename = prefix + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date()) + "." + fileext;
        filename = HttpUtil.encodeURIComponent(filename);
        return ("pdf".equals(fileext) ? "inline" : "attachment") + "; filename=\"" + filename + "\"";
    }

    public static void setExportSessionStatus(HttpSession session, String status, boolean append) {
        if (append) {
            session.setAttribute("adhoc_exportAllStatus", (Object)(session.getAttribute("adhoc_exportAllStatus") + "<br/>" + new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().getTime()) + " " + status));
        } else {
            session.setAttribute("adhoc_exportAllStatus", (Object)"");
        }
    }

    public static class DataEntryGridDataSource
    extends AdhocResultFileDataSource {
        public DataEntryGridDataSource(String gridString, String delimiter) throws FileNotFoundException {
            this.reader = new BufferedReader(new StringReader(gridString));
            this.delimiter = delimiter;
        }

        @Override
        public boolean next() throws JRException {
            String line = "";
            try {
                line = this.reader.readLine();
                if (line != null && line.length() > 0) {
                    if (this.nameIndexMap.size() == 0) {
                        String[] columns = StringUtil.split(line, this.delimiter);
                        for (int i = 0; i < columns.length; ++i) {
                            this.nameIndexMap.put("col" + i, i);
                        }
                    }
                    this.getValueList(line);
                    return true;
                }
                this.reader.close();
                return false;
            }
            catch (Exception e) {
                Trace.logError("Could not parse line:" + line);
                try {
                    this.reader.close();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                throw new JRException((Throwable)e);
            }
        }

        public static JasperDesign createDataEntryGridExcelDesign(int columncount, int colheaderRowCount, int rowheaderColumnCount) {
            JasperDesign jasperDesign = new JasperDesign();
            jasperDesign.setTitle((JRBand)new JRDesignBand());
            jasperDesign.setName("DataEntryGridExport");
            jasperDesign.setIgnorePagination(true);
            jasperDesign.setColumnSpacing(0);
            jasperDesign.setFloatColumnFooter(false);
            jasperDesign.setLeftMargin(0);
            jasperDesign.setTopMargin(0);
            jasperDesign.setPageFooter(null);
            jasperDesign.setColumnFooter(null);
            jasperDesign.setBottomMargin(0);
            jasperDesign.setRightMargin(0);
            JRDesignBand detailBand = new JRDesignBand();
            detailBand.setHeight(15);
            detailBand.setSplitType(SplitTypeEnum.PREVENT);
            ((JRDesignSection)jasperDesign.getDetailSection()).addBand((JRBand)detailBand);
            JRDesignBand headerBand = new JRDesignBand();
            jasperDesign.setColumnHeader((JRBand)headerBand);
            int fieldx = 0;
            int fieldwidth = 130;
            for (int i = 0; i < columncount; ++i) {
                String columnid = "col" + i;
                int cellWidth = fieldwidth;
                if (i > 0) {
                    fieldx += cellWidth;
                }
                JRDesignField field = new JRDesignField();
                field.setName(columnid);
                try {
                    jasperDesign.addField((JRField)field);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, "$V{REPORT_COUNT}.intValue() <= " + colheaderRowCount, true, Color.LIGHT_GRAY, HorizontalTextAlignEnum.CENTER, false));
                if (i < rowheaderColumnCount) {
                    detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, "$V{REPORT_COUNT}.intValue()>" + colheaderRowCount, true, Color.lightGray, HorizontalTextAlignEnum.LEFT, false));
                    continue;
                }
                String gridcellExp = "($V{REPORT_COUNT}.intValue()>" + colheaderRowCount + ")";
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, gridcellExp + " && !$F{" + columnid + "}.equals(\"(null)\") && $F{" + columnid + "}.indexOf( \"R\" ) == 0 && $F{" + columnid + "}.indexOf( \"Y\" ) == 1", false, Color.WHITE, HorizontalTextAlignEnum.RIGHT, true));
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, gridcellExp + " && !$F{" + columnid + "}.equals(\"(null)\") && $F{" + columnid + "}.indexOf( \"R\" ) == 0 && $F{" + columnid + "}.indexOf( \"Y\" ) != 1", false, Color.WHITE, HorizontalTextAlignEnum.RIGHT, false));
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, gridcellExp + " && !$F{" + columnid + "}.equals(\"(null)\") && $F{" + columnid + "}.indexOf( \"L\" ) == 0 && $F{" + columnid + "}.indexOf( \"Y\" ) == 1 ", false, Color.WHITE, HorizontalTextAlignEnum.LEFT, true));
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, columnid, gridcellExp + " && !$F{" + columnid + "}.equals(\"(null)\") && $F{" + columnid + "}.indexOf( \"L\" ) == 0 && $F{" + columnid + "}.indexOf( \"Y\" ) != 1 ", false, Color.WHITE, HorizontalTextAlignEnum.LEFT, false));
                detailBand.addElement((JRDesignElement)DataEntryGridDataSource.getExcelTextField(fieldx, cellWidth, "", gridcellExp + "&& $F{" + columnid + "}.equals(\"(null)\")", false, Color.lightGray, HorizontalTextAlignEnum.RIGHT, false));
            }
            return jasperDesign;
        }

        private static JRDesignTextField getExcelTextField(int fieldx, int cellWidth, String columnid, String printWhenExp, boolean isBold, Color backColor, HorizontalTextAlignEnum alignEnum, boolean isItalic) {
            JRDesignTextField excelField = new JRDesignTextField();
            JRDesignExpression exp = new JRDesignExpression();
            exp.setValueClass(String.class);
            String fieldTextExp = columnid.length() > 0 ? "$F{" + columnid + "}.indexOf(\"|*|\")>0 ? $F{" + columnid + "}.substring( $F{" + columnid + "}.indexOf( \"|*|\" ) + 3 ) : $F{" + columnid + "}" : "";
            exp.setText(fieldTextExp);
            excelField.setExpression((JRExpression)exp);
            excelField.setX(fieldx);
            excelField.setY(0);
            excelField.setHeight(15);
            excelField.setWidth(cellWidth);
            excelField.getLineBox().getPen().setLineWidth(Float.valueOf(0.5f));
            excelField.getLineBox().getPen().setLineColor(Color.gray);
            excelField.setBold(Boolean.valueOf(isBold));
            excelField.setMode(ModeEnum.OPAQUE);
            excelField.setBackcolor(backColor);
            excelField.setHorizontalTextAlign(alignEnum);
            excelField.setItalic(Boolean.valueOf(isItalic));
            excelField.setStretchWithOverflow(true);
            excelField.setBlankWhenNull(true);
            excelField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
            JRDesignExpression printwhenexp = new JRDesignExpression();
            printwhenexp.setValueClass(Boolean.class);
            printwhenexp.setText("Boolean.valueOf(" + printWhenExp + ")");
            excelField.setPrintWhenExpression((JRExpression)printwhenexp);
            return excelField;
        }
    }

    public static class AdhocResultFileDataSource
    implements JRDataSource {
        protected BufferedReader reader = null;
        protected HashMap<String, Integer> nameIndexMap = new HashMap();
        protected ArrayList<String> currentRowList = new ArrayList();
        protected String delimiter = ",";

        public AdhocResultFileDataSource() {
        }

        public AdhocResultFileDataSource(File inputfile) throws FileNotFoundException {
            try {
                this.reader = Files.newBufferedReader(Paths.get(inputfile.toURI()), Charset.forName("UTF-8"));
            }
            catch (Exception e) {
                Trace.log("Failed to read inputfile");
            }
        }

        public boolean next() throws JRException {
            String line = "";
            try {
                line = this.reader.readLine();
                if (line != null && line.length() > 0) {
                    line = StringUtil.replaceAll(line, "Char(10)", "\r");
                    if ((line = StringUtil.replaceAll(line, "Char(13)", "\n")).indexOf("##header##") == 0) {
                        this.nameIndexMap.clear();
                        line = line.substring(10);
                        ArrayList columns = this.getValueList(line);
                        for (int i = 0; i < columns.size(); ++i) {
                            this.nameIndexMap.put((String)columns.get(i), i);
                        }
                        line = this.reader.readLine();
                        if (line == null || line.length() == 0) {
                            this.reader.close();
                            return false;
                        }
                        line = StringUtil.replaceAll(line, "Char(10)", "\r");
                        line = StringUtil.replaceAll(line, "Char(13)", "\n");
                    }
                    this.getValueList(line);
                    return true;
                }
            }
            catch (Exception e) {
                Trace.logError("Could not parse line:" + line);
                try {
                    this.reader.close();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                throw new JRException((Throwable)e);
            }
            try {
                this.reader.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return false;
        }

        public Object getFieldValue(JRField jrField) throws JRException {
            String fieldid = jrField.getName();
            return this.getFieldValue(fieldid);
        }

        public Object getFieldValue(String fieldid) {
            if (this.nameIndexMap.get(fieldid) != null) {
                try {
                    return this.currentRowList.get(this.nameIndexMap.get(fieldid));
                }
                catch (Exception e) {
                    return "";
                }
            }
            return "";
        }

        public ArrayList getValueList(String line) {
            this.currentRowList.clear();
            if (line != null && line.length() > 0) {
                int pos = line.indexOf(this.delimiter);
                if (pos == -1) {
                    this.currentRowList.add(line);
                } else {
                    String value;
                    int offset = 0;
                    while (pos > -1) {
                        value = line.substring(offset, pos);
                        if (value.indexOf("\"") == 0 && value.indexOf("\"\"") != 0) {
                            pos = line.indexOf("\"" + this.delimiter, offset);
                            if (pos < 0 && line.lastIndexOf("\"") == line.length() - 1) {
                                pos = line.length() - 1;
                            }
                            if (pos < 0) {
                                throw new RuntimeException("Cannot find closing double quote for " + line);
                            }
                            ++pos;
                            try {
                                value = line.substring(offset + 1, pos - 1);
                            }
                            catch (Exception e) {
                                Trace.log(e.getMessage());
                            }
                        }
                        value = value.replaceAll("\"\"", "\"");
                        this.currentRowList.add(value);
                        offset = pos + this.delimiter.length();
                        pos = line.indexOf(this.delimiter, offset);
                    }
                    if (offset <= line.length()) {
                        value = line.substring(offset, line.length());
                        if (value.indexOf("\"") == 0 && value.lastIndexOf("\"") == value.length() - 1) {
                            value = value.substring(1, value.length() - 1);
                        }
                        this.currentRowList.add(value);
                    }
                }
            }
            return this.currentRowList;
        }
    }
}

