/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 *  net.sf.jasperreports.engine.JRDataSource
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRField
 *  net.sf.jasperreports.engine.JasperCompileManager
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.data.JRMapCollectionDataSource
 *  net.sf.jasperreports.engine.design.JasperDesign
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.AutomationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.adhocbrowser.ReportGenerator;
import com.labvantage.sapphire.modules.datafile.ExcelFileReader;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.ListReportDesigner;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.servlet.filter.HTMLInputFilter;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TranslationUtil {
    private static ExecutorService executeService = Executors.newCachedThreadPool();
    static long starttime = 0L;
    public static Future results = null;
    public static boolean findIgnoreCase = false;
    private static List<String> ignoreList = new ArrayList<String>(){
        {
            this.add("&gt;");
            this.add("#");
            this.add("&gt;&gt;");
            this.add("&lt;");
            this.add("&lt;&lt;");
            this.add("&lt;&lt;");
            this.add("*");
            this.add("-");
            this.add("...");
            this.add("&nbsp;");
            this.add("( enabled ? \"Disable\" : \"Enable\" ) + \" indexing on database '[1]'\"");
            this.add("s");
            this.add("+1s");
            this.add("+2s");
            this.add("+3s");
            this.add("-1s");
            this.add("-2s");
            this.add("-3s");
            this.add(". Exclude Flag = Y");
            this.add("16x16");
            this.add("1s");
            this.add("32x32");
            this.add("3s");
            this.add("48x48");
            this.add("<br>");
            this.add("[applicationdesc]");
            this.add("[auditSequence]");
            this.add("[departmenttype]");
            this.add("[disabledreason]");
            this.add("[errormsg]");
            this.add("[eventdeflabel]");
            this.add("[firstname] [lastname]");
            this.add("[keycolid1]");
            this.add("[keyid1]");
            this.add("[measurecategory]");
            this.add("[message]");
            this.add("[mode]");
            this.add("[moretitle]");
            this.add("[msg]");
            this.add("[msg] {{Monitor group}} <a add(\"href=\"JavaScript:top.sapphire.page.navigate('rc?command=page&page=LV_MonitorGroupMaint&sdcid=LV_MonitorGroup&keyid1=[newkeyid1]');\">[newkeyid1]</a>");
            this.add("[paramlistid]");
            this.add("[paramlistid]/[paramid]");
            this.add("[productid]");
            this.add("[requesttext]");
            this.add("[s_sampleid]");
            this.add("[seriesid]");
            this.add("[sstudyid]");
            this.add("[stroutput]");
            this.add("[sysuserdesc] ([logonname])");
            this.add("[sysuserid]");
            this.add("[sysuserid] - [paramlistid]");
            this.add("[sysuserid] - [paramlistid]/[paramid]");
            this.add("[sysusername]");
            this.add("A \\");
            this.add("A4");
            this.add("A5");
            this.add("B5");
            this.add("CA");
            this.add("Cancel changes', 'Are you sure you want to cancel changes?', 'Yes', 'No");
            this.add("Cancel changes', 'Are you sure you want to cancel your changes?', 'Yes', 'No");
            this.add("CC");
            this.add("CL");
            this.add("DEV");
            this.add("e");
            this.add("F");
            this.add("M");
            this.add("N");
            this.add("S-130911*");
            this.add("Style<input type=\"hidden\" name=\"examplefilename\" id=\"examplefilename\" style=\"width: 400\" onchange=\"dataFileDefMaint.changeExampleFile()\" >");
            this.add("V");
            this.add("W");
            this.add("X");
            this.add("xxxxxxx");
            this.add("Y");
            this.add("{{[mode]");
            this.add("{{[sdcid]");
            this.add("{{[singular]");
            this.add("[worksheetname]");
            this.add("[workorderid]");
            this.add("[workitemid]");
            this.add("[workbookdesc]");
            this.add("[studyid]");
            this.add("[stagelabel]");
            this.add("[sourcesdcid] [sourcekeyid1]");
            this.add("[schedulerulelabel]");
            this.add("[s_requestid]");
            this.add("[qcbatchid] [qcsampletype]");
            this.add("[locationlabel] [excursionflag]");
            this.add("[locationlabel] [excursioncount]");
            this.add("[instrumenttypeid]");
            this.add("[incidentid]");
            this.add("[conditionlabel]");
            this.add("[actionplanid]");
        }
    };

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String importFromExcel(PageContext pageContext) {
        HttpServletRequest request;
        String message;
        block15: {
            message = "";
            request = (HttpServletRequest)pageContext.getRequest();
            if ("post".equals(request.getMethod().toLowerCase())) break block15;
            String string = "";
            return string;
        }
        try {
            FileItem fi;
            String fileName;
            FileUpload fu = new FileUpload();
            List fileItems = null;
            int maxsize = 10000000;
            fu.setSizeMax(maxsize);
            fu.setSizeThreshold(maxsize);
            fileItems = fu.parseRequest(request);
            Iterator i = fileItems.iterator();
            File tempFile = null;
            if (i.hasNext() && (fileName = (fi = (FileItem)i.next()).getName()) != null) {
                byte[] indata;
                int lastFileSepIndex = fileName.lastIndexOf(File.separator);
                if (lastFileSepIndex >= 0) {
                    fileName = fileName.substring(lastFileSepIndex + 1);
                }
                if ((indata = fi.get()).length > 0) {
                    tempFile = File.createTempFile(fileName, fileName.indexOf(".xlsx") > 0 ? ".xlsx" : ".xls");
                    tempFile.deleteOnExit();
                    FileOutputStream fileout = new FileOutputStream(tempFile);
                    fileout.write(indata);
                    fileout.close();
                }
            }
            if (tempFile != null) {
                int l;
                ConnectionProcessor connectionProcessor = new ConnectionProcessor(pageContext);
                ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
                ExcelFileReader excelFileReader = new ExcelFileReader(connectionInfo, tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf(File.separator)), tempFile.getName(), "Translation Export", 50000);
                excelFileReader.initialize();
                DataSet header = excelFileReader.getHeaderRow(1);
                excelFileReader.setSliceStart(2);
                DataSet ds = excelFileReader.getNextSlice();
                String textid = ds.getColumnValues(ds.getColumnId(0), ";");
                String texttype = ds.getColumnValues(ds.getColumnId(1), ";");
                TranslationProcessor tp = new TranslationProcessor(pageContext);
                QueryProcessor qp = new QueryProcessor(pageContext);
                boolean hasClientsideFlag = false;
                for (l = 0; l < ds.getColumnCount(); ++l) {
                    if (!"CF".equalsIgnoreCase(header.getValue(0, header.getColumnId(l)))) continue;
                    hasClientsideFlag = true;
                    break;
                }
                if (ds.getColumnCount() == 2 || hasClientsideFlag && ds.getColumnCount() == 3) {
                    tp.saveTranslation("", textid, "", texttype);
                } else {
                    int startcol;
                    for (int l2 = startcol = hasClientsideFlag ? 3 : 2; l2 < ds.getColumnCount(); ++l2) {
                        String languageid = header.getValue(0, header.getColumnId(l2));
                        String transtext = ds.getColumnValues(ds.getColumnId(l2), ";");
                        tp.saveTranslation(languageid, textid, transtext, texttype);
                    }
                }
                for (l = 0; l < ds.getColumnCount(); ++l) {
                    if (!"CF".equalsIgnoreCase(header.getValue(0, header.getColumnId(l)))) continue;
                    for (int r = 0; r < ds.getRowCount(); ++r) {
                        if (!"Y".equals(ds.getValue(r, ds.getColumnId(l)))) continue;
                        String sql = "UPDATE transmaster set clientsideflag='Y' WHERE textid=? AND texttype=?";
                        qp.execPreparedUpdate(sql, new Object[]{ds.getValue(r, ds.getColumnId(0)), ds.getValue(r, ds.getColumnId(1))});
                    }
                }
                message = ds.getRowCount() + " translations imported successfully";
            }
        }
        catch (Exception ex) {
            Logger.logError(ex.getMessage(), ex);
            message = ex.getMessage();
        }
        return message;
    }

    public static void exportToExcel(DataSet ds, PageContext pageContext) {
        try {
            PropertyList element = new PropertyList();
            PropertyListCollection displaycolumns = new PropertyListCollection();
            String languageid = pageContext.getRequest().getParameter("languageid");
            if (languageid != null && languageid.length() > 0) {
                String[] languages = StringUtil.split(languageid, ";");
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "textid");
                column.setProperty("title", "Textid");
                column.setProperty("width", "250");
                displaycolumns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "texttype");
                column.setProperty("title", "Context");
                column.setProperty("width", "100");
                displaycolumns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "clientsideflag");
                column.setProperty("title", "CF");
                column.setProperty("width", "20");
                displaycolumns.add(column);
                for (int c = 0; c < languages.length; ++c) {
                    column = new PropertyList();
                    column.setProperty("columnid", languages[c]);
                    column.setProperty("title", languages[c]);
                    column.setProperty("width", "300");
                    displaycolumns.add(column);
                }
                element.setProperty("columns", displaycolumns);
                ListReportDesigner reportDesigner = new ListReportDesigner("Translation Export", element);
                reportDesigner.setDisplayType("xls");
                JasperDesign jasperDesign = reportDesigner.createJasperDesign();
                SapphireJasperUtil.setJasperCompilerClassPatch();
                JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
                TranslationDataSource datasource = new TranslationDataSource(ds, languages);
                CommonParamMap paramsMap = new CommonParamMap(pageContext);
                JasperPrint jasperPrint = SapphireJasperUtil.getJasperPrint(jasperReport, (Map)paramsMap, datasource);
                ReportGenerator.setReportObject(pageContext.getSession(), jasperPrint);
                ReportGenerator.writeReportFrame(pageContext, "xls", TranslationUtil.getContentDispositionHeader("TranslationExport", "xls"), starttime);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public static void exportTransmasterTempToExcel(PageContext pageContext) {
        try {
            DataSet ds = new QueryProcessor(pageContext).getSqlDataSet("select textid, texttype, clientsideflag from transmastertemp");
            if (ds.getRowCount() > 0) {
                PropertyList element = new PropertyList();
                PropertyListCollection displaycolumns = new PropertyListCollection();
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "textid");
                column.setProperty("title", "Textid");
                column.setProperty("width", "250");
                displaycolumns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "texttype");
                column.setProperty("title", "Context");
                column.setProperty("width", "100");
                displaycolumns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "clientsideflag");
                column.setProperty("title", "CF");
                column.setProperty("width", "20");
                displaycolumns.add(column);
                element.setProperty("columns", displaycolumns);
                ListReportDesigner reportDesigner = new ListReportDesigner("Translation Export", element);
                reportDesigner.setDisplayType("xls");
                JasperDesign jasperDesign = reportDesigner.createJasperDesign();
                SapphireJasperUtil.setJasperCompilerClassPatch();
                JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
                JRMapCollectionDataSource datasource = new JRMapCollectionDataSource((Collection)ds);
                CommonParamMap paramsMap = new CommonParamMap(pageContext);
                JasperPrint jasperPrint = SapphireJasperUtil.getJasperPrint(jasperReport, (Map)paramsMap, (JRDataSource)datasource);
                ReportGenerator.setReportObject(pageContext.getSession(), jasperPrint);
                ReportGenerator.writeReportFrame(pageContext, "xls", TranslationUtil.getContentDispositionHeader("TranslationExport", "xls"), starttime);
            } else {
                pageContext.getOut().write("No rows found in the transmaster temp table to export.");
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String findConfigText(PageContext pageContext) {
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        final ConnectionInfo connInfo = cp.getConnectionInfo(cp.getConnectionid());
        String databaseid = connInfo.getDatabaseId();
        if (results == null && starttime == 0L) {
            starttime = System.currentTimeMillis();
            PropertyTranslationProcessor.processPages = new ArrayList();
            PropertyTranslationProcessor.textidSet = new HashSet();
            PropertyTranslationProcessor.textidTypeDataSet = new DataSet();
            results = executeService.submit(new Callable<String>(){

                @Override
                public String call() throws Exception {
                    AutomationProcessor automationProcessor = new AutomationProcessor(connInfo.getConnectionId());
                    automationProcessor.loadWebPageCache(connInfo.getConnectionId());
                    return "Done";
                }
            });
            return "Start Processing...";
        }
        if (results != null && results.isDone()) {
            DataSet ds = PropertyTranslationProcessor.textidTypeDataSet;
            Set textidSet = PropertyTranslationProcessor.textidSet;
            ds.addColumn("matchedtextid", 0);
            ds.addColumn("source", 0);
            QueryProcessor qp = new QueryProcessor(pageContext);
            DataSet masterDs = qp.getSqlDataSet("select textid from transmaster");
            masterDs.addColumn("lowertextid", 0);
            for (int i = 0; i < masterDs.getRowCount(); ++i) {
                masterDs.setValue(i, "lowertextid", masterDs.getValue(i, "textid").trim().toLowerCase());
            }
            ds.sort("textid, texttype");
            StringBuffer out = new StringBuffer();
            out.append("<table style=\"width:100%;border-collapse:collapse;border:2px solid gray\">");
            out.append("<tr><td class=\"txt\" style=\"font-weight:bold\">Text Id</td><td class=\"txttype\" style=\"font-weight:bold\">Context</td><td class=\"txt\" style=\"font-weight:bold;width:200px\">Found In Sources</td>");
            WebAdminProcessor wap = new WebAdminProcessor(pageContext);
            try {
                TranslationUtil.collectionTextsFromPropertyTree("Policy", connInfo, qp, wap);
                TranslationUtil.collectionTextsFromPropertyTree("Step", connInfo, qp, wap);
                TranslationUtil.collectionTextsFromPropertyTree("Gizmo", connInfo, qp, wap);
            }
            catch (Exception e) {
                out.append("Error:" + e.getMessage());
            }
            DataSet refvalueDs = qp.getSqlDataSet("SELECT distinct refvalueid, refdisplayvalue, reftypeid from refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid not in ('Time Zone','DisplayFormat')");
            for (int i = 0; i < refvalueDs.getRowCount(); ++i) {
                String textid = refvalueDs.getValue(i, "refdisplayvalue").length() > 0 ? refvalueDs.getValue(i, "refdisplayvalue") : refvalueDs.getValue(i, "refvalueid");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                int row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Reference Value:" + refvalueDs.getValue(i, "reftypeid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet queryDs = qp.getSqlDataSet("SELECT distinct queryid, querylabel, querydesc from query");
            for (int i = 0; i < queryDs.getRowCount(); ++i) {
                int row;
                String textid = queryDs.getValue(i, "querylabel").length() > 0 ? queryDs.getValue(i, "querylabel") : queryDs.getValue(i, "queryid");
                if (!textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) {
                    row = ds.addRow();
                    ds.setValue(row, "textid", textid);
                    ds.setValue(row, "texttype", "W");
                    ds.setValue(row, "cf", "N");
                    ds.setValue(row, "source", "Query Label:" + queryDs.getValue(i, "queryid"));
                    textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
                }
                if (queryDs.getValue(i, "querydesc").length() <= 0) continue;
                textid = queryDs.getValue(i, "querydesc");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Query Description:" + queryDs.getValue(i, "queryid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet queryargDs = qp.getSqlDataSet("SELECT distinct argid, argdesc, queryid from queryarg");
            for (int i = 0; i < queryargDs.getRowCount(); ++i) {
                String textid = queryargDs.getValue(i, "argdesc").length() > 0 ? queryargDs.getValue(i, "argdesc") : queryargDs.getValue(i, "argid");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                int row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Query Arg:" + queryargDs.getValue(i, "queryid") + ":" + queryargDs.getValue(i, "argid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet reportDs = qp.getSqlDataSet("SELECT distinct reportid, reportdesc from report");
            for (int i = 0; i < reportDs.getRowCount(); ++i) {
                String textid = reportDs.getValue(i, "reportdesc").length() > 0 ? reportDs.getValue(i, "reportdesc") : reportDs.getValue(i, "reportid");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                int row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Report ID/Desc:" + reportDs.getValue(i, "reportid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet reportargDs = qp.getSqlDataSet("SELECT distinct paramid, paramdesc, reportid from reportparam");
            for (int i = 0; i < reportargDs.getRowCount(); ++i) {
                String textid = reportargDs.getValue(i, "paramdesc").length() > 0 ? reportargDs.getValue(i, "paramdesc") : reportargDs.getValue(i, "paramid");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                int row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Report Param Desc:" + reportargDs.getValue(i, "reportid") + ":" + reportargDs.getValue(i, "paramid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet singularPluralDs = qp.getSqlDataSet("SELECT distinct singular, plural, sdcid from sdc");
            for (int i = 0; i < singularPluralDs.getRowCount(); ++i) {
                int row;
                String textid;
                if (singularPluralDs.getValue(i, "singular").length() > 0) {
                    textid = singularPluralDs.getValue(i, "singular");
                    if (!textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) {
                        row = ds.addRow();
                        ds.setValue(row, "textid", textid);
                        ds.setValue(row, "texttype", "W");
                        ds.setValue(row, "cf", "N");
                        ds.setValue(row, "source", "SDC Singular:" + singularPluralDs.getValue(i, "sdcid"));
                        textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
                    }
                }
                if (singularPluralDs.getValue(i, "plural").length() <= 0) continue;
                textid = singularPluralDs.getValue(i, "plural");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "SDC Plural:" + singularPluralDs.getValue(i, "sdcid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            DataSet columnlabelDs = qp.getSqlDataSet("SELECT distinct columnlabel, tableid from syscolumn");
            for (int i = 0; i < columnlabelDs.getRowCount(); ++i) {
                if (columnlabelDs.getValue(i, "columnlabel").length() <= 0) continue;
                String textid = columnlabelDs.getValue(i, "columnlabel");
                if (textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) continue;
                int row = ds.addRow();
                ds.setValue(row, "textid", textid);
                ds.setValue(row, "texttype", "W");
                ds.setValue(row, "cf", "N");
                ds.setValue(row, "source", "Column Label:" + columnlabelDs.getValue(i, "tableid"));
                textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
            }
            ds.sort("textid, texttype");
            OutputStreamWriter writer = null;
            int count = 0;
            try {
                String[] columns = new String[]{"textid", "texttype", "cf", "source"};
                count = TranslationUtil.getMissingRecords(out, masterDs, ds, (FileWriter)writer, columns);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (writer != null) {
                    try {
                        writer.close();
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
            out.append("</table>");
            out.append("Found " + count + "items. Done in " + (System.currentTimeMillis() - starttime) + " ms");
            starttime = 0L;
            results = null;
            PropertyTranslationProcessor.textidTypeDataSet = null;
            PropertyTranslationProcessor.textidSet = null;
            PropertyTranslationProcessor.processPages = null;
            return out.toString();
        }
        return "";
    }

    private static void collectionTextsFromPropertyTree(String propertyTreeType, ConnectionInfo connInfo, QueryProcessor qp, WebAdminProcessor wap) throws Exception {
        SafeSQL safeSQL = new SafeSQL();
        DataSet policyDs = qp.getPreparedSqlDataSet("select propertytreeid from propertytree where propertytreetype=" + safeSQL.addVar(propertyTreeType), safeSQL.getValues());
        for (int i = 0; i < policyDs.getRowCount(); ++i) {
            String policyid = policyDs.getValue(i, "propertytreeid");
            PropertyTree policy = wap.getPropertyTree(policyid);
            if (policy == null) continue;
            ArrayList nodes = policy.getAllNodes();
            for (int n = 0; n < nodes.size(); ++n) {
                Node node = (Node)nodes.get(n);
                String nodeid = node.getNodeId();
                PropertyList policyPropertyList = policy.getNodePropertyList(nodeid, true);
                String pageid = propertyTreeType + ":" + policyid + "-" + nodeid;
                String languageid = connInfo.getLanguage();
                policyPropertyList = policyPropertyList.copy(languageid, new PropertyTranslationProcessor(connInfo.getConnectionId(), pageid));
            }
        }
    }

    private static void appendRowHtml(StringBuffer out, int row, DataSet ds, String matchedtext, FileWriter writer, String[] columns) throws IOException {
        out.append("<tr><td class=\"txt\">" + HTMLInputFilter.htmlSpecialChars(ds.getValue(row, "textid")) + "</td><td class=\"txttype\">" + HTMLInputFilter.htmlSpecialChars(ds.getValue(row, "texttype")) + "</td><td class=\"txt\">" + (ds.getValue(row, "source").length() > 0 ? ds.getValue(row, "source") : "Web Pages") + "</td></tr>");
        if (writer != null) {
            for (int c = 0; c < columns.length; ++c) {
                String value;
                if (c > 0) {
                    writer.write(",");
                }
                writer.write((value = ds.getValue(row, columns[c]).replaceAll("\"", "\"\"")).indexOf(",") >= 0 || value.indexOf("\r") >= 0 || value.indexOf("\n") >= 0 ? "\"" + value + "\"" : value);
            }
            writer.write("\r\n");
        }
    }

    private static int getMissingRecords(StringBuffer out, DataSet masterDs, DataSet ds, FileWriter writer, String[] columns) throws IOException {
        HashMap<String, String> findMap = new HashMap<String, String>();
        HashSet<String> set = new HashSet<String>();
        int count = 0;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            boolean nomatchfound = true;
            String textid = ds.getValue(i, "textid");
            if (!OpalUtil.isNotEmpty(textid) || set.contains(textid) || ignoreList.contains(textid) || !textid.matches(".*[a-zA-Z]+.*")) continue;
            set.add(textid);
            findMap.put("textid", textid.trim());
            if (masterDs.findRow(findMap) >= 0 || textid.trim().length() <= 0) continue;
            String ptextid = textid.trim();
            findMap.put("textid", ptextid);
            if (masterDs.findRow(findMap) >= 0) {
                nomatchfound = false;
                TranslationUtil.appendRowHtml(out, i, ds, ptextid, writer, columns);
                ++count;
            }
            if (nomatchfound && ptextid.length() > 0) {
                char lastChar = ptextid.charAt(ptextid.length() - 1);
                if (lastChar == '.' || lastChar == ',' || lastChar == ':' || lastChar == ';' || lastChar == '?') {
                    ptextid = ptextid.substring(0, ptextid.length() - 1);
                    findMap.put("textid", ptextid);
                    if (masterDs.findRow(findMap) >= 0) {
                        nomatchfound = false;
                        TranslationUtil.appendRowHtml(out, i, ds, ptextid, writer, columns);
                        ++count;
                    } else {
                        findMap.put("textid", ptextid.toLowerCase());
                        if (masterDs.findRow(findMap) >= 0) {
                            nomatchfound = false;
                            TranslationUtil.appendRowHtml(out, i, ds, ptextid.toLowerCase(), writer, columns);
                            ++count;
                        }
                    }
                }
                if (nomatchfound && ds.getValue(i, "textid").trim().length() > 0) {
                    findMap.put("textid", ptextid.toLowerCase());
                    if (masterDs.findRow(findMap) >= 0) {
                        nomatchfound = false;
                        TranslationUtil.appendRowHtml(out, i, ds, ptextid.toLowerCase(), writer, columns);
                        ++count;
                    }
                }
            }
            if (!nomatchfound) continue;
            TranslationUtil.appendRowHtml(out, i, ds, "", writer, columns);
            ++count;
        }
        return count;
    }

    public static String saveTranslation(PageContext pageContext) throws SapphireException {
        String message;
        block9: {
            String[] languages;
            HttpServletRequest request;
            block10: {
                request = (HttpServletRequest)pageContext.getRequest();
                String languagelist = request.getParameter("languagelist");
                message = "";
                boolean change = false;
                if (request.getParameter("textdeletelist") != null && request.getParameter("textdeletelist").length() > 0) {
                    message = TranslationUtil.deleteTranslation(pageContext, request.getParameter("textdeletelist"), request.getParameter("texttypedeletelist"));
                    change = true;
                }
                if (request.getParameter("textidlist") != null && request.getParameter("textidlist").length() > 0) {
                    TranslationUtil.saveTranslation(pageContext, request.getParameter("textidlist"), request.getParameter("typelist"), request.getParameter("languagelist"), request.getParameter("texttranslist"));
                    change = true;
                } else if (languagelist != null && languagelist.length() > 0) {
                    String textid = request.getParameter("textid");
                    String texttype = request.getParameter("texttype") == null ? "W" : request.getParameter("texttype");
                    TranslationProcessor tp = new TranslationProcessor(pageContext);
                    String[] languages2 = StringUtil.split(languagelist, ";");
                    for (int i = 0; i < languages2.length; ++i) {
                        String transtext = request.getParameter("transtext_" + languages2[i]);
                        if (transtext == null || transtext.length() <= 0) continue;
                        tp.saveTranslation(languages2[i], textid, transtext, texttype);
                        change = true;
                    }
                }
                if (request.getParameter("clientflagtextidlist") != null && request.getParameter("clientflagtextidlist").length() > 0) {
                    DataSet savecfds = new DataSet();
                    String cftextidlist = pageContext.getRequest().getParameter("clientflagtextidlist");
                    String cftexttypelist = pageContext.getRequest().getParameter("clientflagtexttypelist");
                    String cflist = pageContext.getRequest().getParameter("clientflaglist");
                    savecfds.addColumnValues("textid", 0, cftextidlist, ";");
                    savecfds.addColumnValues("texttype", 0, cftexttypelist, ";");
                    savecfds.addColumnValues("clientsideflag", 0, cflist, ";");
                    QueryProcessor qp = new QueryProcessor(pageContext);
                    for (int i = 0; i < savecfds.getRowCount(); ++i) {
                        if (savecfds.getValue(i, "textid").length() <= 0) continue;
                        change = true;
                        qp.execPreparedUpdate("update transmaster set clientsideflag=? where textid=? and texttype=?", new Object[]{savecfds.getValue(i, "clientsideflag"), savecfds.getValue(i, "textid"), savecfds.getValue(i, "texttype")});
                    }
                }
                if (!change) break block9;
                if (languagelist.length() <= 0) break block10;
                languages = StringUtil.split(languagelist, ";");
                for (int i = 0; i < languages.length; ++i) {
                    TranslationUtil.resetCaches(pageContext, languages[i]);
                }
                break block9;
            }
            if (request.getParameter("languageid") == null || request.getParameter("languageid").length() <= 0) break block9;
            languages = StringUtil.split(request.getParameter("languageid"), ";");
            for (int i = 0; i < languages.length; ++i) {
                TranslationUtil.resetCaches(pageContext, languages[i]);
            }
        }
        return message;
    }

    private static void resetCaches(PageContext pageContext, String language) {
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        RequestContext requestContext = RequestContext.getRequestContext(pageContext);
        ConnectionInfo connectionInfo = cp.getConnectionInfo(requestContext.getConnectionId());
        CacheUtil.remove(connectionInfo.getDatabaseId(), "ClientTranslationCache", language);
    }

    private static void saveTranslation(PageContext pageContext, String textidlist, String typelist, String languageidlist, String transtextlist) throws SapphireException {
        DataSet saveds = new DataSet();
        saveds.addColumnValues("textid", 0, textidlist, ";");
        saveds.addColumnValues("texttype", 0, typelist, ";");
        saveds.addColumnValues("languageid", 0, languageidlist, ";");
        saveds.addColumnValues("transtext", 0, transtextlist, ";");
        DataSet languages = new QueryProcessor(pageContext).getSqlDataSet("select languageid from language");
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        for (int l = 0; l < languages.size(); ++l) {
            String savelanguageid = languages.getValue(l, "languageid");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("languageid", savelanguageid);
            DataSet filterDs = saveds.getFilteredDataSet(filterMap);
            if (filterDs.getRowCount() <= 0) continue;
            tp.saveTranslation(savelanguageid, filterDs.getColumnValues("textid", ";"), filterDs.getColumnValues("transtext", ";"), filterDs.getColumnValues("texttype", ";"));
        }
    }

    private static String deleteTranslation(PageContext pageContext, String textdeletelist, String texttypedeletelist) {
        QueryProcessor qp = new QueryProcessor(pageContext);
        String returnmessage = "";
        String[] textids = StringUtil.split(textdeletelist, "%3B");
        String[] texttypes = StringUtil.split(texttypedeletelist, "%3B");
        for (int i = 0; i < textids.length; ++i) {
            qp.execPreparedUpdate("delete from translanguage where transmasterid in ( select tm.transmasterid from transmaster tm where tm.texttype=? and tm.textid=?)", new Object[]{texttypes[i], textids[i]});
            qp.execPreparedUpdate("delete from transmaster where texttype=? and textid=?", new Object[]{texttypes[i], textids[i]});
        }
        DataSet languages = new QueryProcessor(pageContext).getSqlDataSet("select languageid from language");
        SapphireConnection sapphireConnection = new SapphireConnection();
        sapphireConnection.setConnectionInfo(new ConnectionProcessor(qp.getConnectionid()).getConnectionInfo(qp.getConnectionid()));
        for (int l = 0; l < languages.size(); ++l) {
            String languageid = languages.getValue(l, "languageid");
            CacheUtil.remove(sapphireConnection.getDatabaseId(), "Language", languageid);
        }
        return returnmessage;
    }

    public static DataSet searchTranslationText(PageContext pageContext) {
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        String languageid = request.getParameter("languageid");
        String style = request.getParameter("searchstyle");
        String searchtext = request.getParameter("searchtext");
        String searchtranstextstyle = request.getParameter("searchtranstextstyle");
        String searchtranstext = request.getParameter("searchtranstext");
        String casesensitive = request.getParameter("casesensitive");
        String texttype = request.getParameter("texttype");
        String clientsideflag = request.getParameter("clientsideflag");
        DataSet ds = null;
        QueryProcessor qp = new QueryProcessor(pageContext);
        if (languageid != null && languageid.length() > 0 && (searchtext != null || searchtranstext != null)) {
            String searchbytranstext;
            String searchby = searchtext != null ? TranslationUtil.getSearchClause(searchtext, casesensitive, style, "m.textid") : "";
            String string = searchbytranstext = searchtranstext != null ? TranslationUtil.getSearchClause(searchtranstext, casesensitive, searchtranstextstyle, "l.transtext") : "";
            if (searchtranstext != null && searchtranstext.length() > 0) {
                searchby = "(" + searchby + ") AND (" + searchbytranstext + ")";
            }
            if ("Y".equals(clientsideflag)) {
                searchby = searchby + " AND clientsideflag='Y' ";
            }
            StringBuffer sql = new StringBuffer("SELECT m.textid, m.texttype, m.clientsideflag, languageid, transtext FROM transmaster m LEFT OUTER JOIN translanguage l ON m.transmasterid=l.transmasterid WHERE " + ("All".equals(texttype) ? "" : " m.texttype='" + texttype.replaceAll("'", "''") + "' AND ")).append(searchby);
            ds = qp.getSqlDataSet(sql.toString());
            ds.sort("textid,texttype");
            pageContext.getSession().setAttribute("Translation_dataset", (Object)ds);
        }
        return ds;
    }

    private static String getSearchClause(String searchtext, String casesensitive, String style, String searchcolumnid) {
        String searchby = "";
        if (searchtext != null) {
            searchby = casesensitive != null ? (style != null && style.equals("startwith") ? searchby + " " + searchcolumnid + " like '" + searchtext.replaceAll("'", "''") + "%' " : (style != null && style.equals("endwith") ? searchby + " " + searchcolumnid + " like '%" + searchtext.replaceAll("'", "''") + "' " : searchby + " " + searchcolumnid + " like '%" + searchtext + "%' ")) : (style != null && style.equals("startwith") ? searchby + " lower(" + searchcolumnid + ") like '" + searchtext.toLowerCase().replaceAll("'", "''") + "%' " : (style != null && style.equals("endwith") ? searchby + " lower(" + searchcolumnid + ") like '%" + searchtext.toLowerCase().replaceAll("'", "''") + "' " : searchby + " lower(" + searchcolumnid + ") like '%" + searchtext.toLowerCase().replaceAll("'", "''") + "%' "));
        }
        return searchby;
    }

    public static String getContentDispositionHeader(String prefix, String fileext) {
        String filename = prefix + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date()) + "." + fileext;
        filename = HttpUtil.encodeURIComponent(filename);
        return ("pdf".equals(fileext) ? "inline" : "attachment") + "; filename=\"" + filename + "\"";
    }

    public static boolean isExcludedText(String textid) {
        return textid == null || textid.indexOf("$G{") == 0 || textid.indexOf("<script>") == 0 || textid.indexOf("<img ") == 0 || textid.indexOf("|") == 0 || textid.trim().length() == 0 || textid.length() > 2000 || textid.indexOf("____") > 0;
    }

    public static TranslationProcessor getPropertyTranslationProcessor(String connectionid, String pageid) {
        return new PropertyTranslationProcessor(connectionid, pageid);
    }

    public static class PropertyTranslationProcessor
    extends TranslationProcessor {
        public static Set textidSet = new HashSet();
        public static DataSet textidTypeDataSet = null;
        public static ArrayList<String> processPages = new ArrayList();
        private String currentPage = "";

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private PropertyTranslationProcessor(String connectionid, String pageid) {
            super(connectionid);
            this.currentPage = pageid;
            if (!processPages.contains(pageid)) {
                ArrayList<String> arrayList = processPages;
                synchronized (arrayList) {
                    processPages.add(pageid);
                }
            }
            if (textidTypeDataSet == null) {
                textidTypeDataSet = new DataSet();
            }
            textidTypeDataSet.addColumn("textid", 0);
            textidTypeDataSet.addColumn("texttype", 0);
            textidTypeDataSet.addColumn("cf", 0);
            textidTypeDataSet.addColumn("source", 0);
        }

        @Override
        public String translate(String textid, String languageid) {
            return this.translate(textid, languageid, this.texttype);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String translate(String textid, String languageid, String context) {
            if ((textid = textid.trim()).indexOf("{{") == 0 && textid.indexOf("}}") == textid.length() - 2) {
                textid = textid.substring(2, textid.length() - 2);
            }
            if (!TranslationUtil.isExcludedText(textid)) {
                String transtext = super.translate(textid, languageid, context);
                if (!textidSet.contains(findIgnoreCase ? textid.toLowerCase() : textid)) {
                    Collection collection = textidSet;
                    synchronized (collection) {
                        textidSet.add(findIgnoreCase ? textid.toLowerCase() : textid);
                    }
                    collection = textidTypeDataSet;
                    synchronized (collection) {
                        int row = textidTypeDataSet.addRow();
                        textidTypeDataSet.setValue(row, "textid", textid);
                        textidTypeDataSet.setValue(row, "texttype", context);
                        textidTypeDataSet.setValue(row, "cf", "N");
                        textidTypeDataSet.setValue(row, "source", this.currentPage + ":" + this.texttype);
                    }
                }
                return transtext;
            }
            return textid;
        }

        @Override
        public String translatePartial(String textid, String languageid) {
            return super.translatePartial(textid, languageid);
        }
    }

    public static class TranslationDataSource
    implements JRDataSource {
        private DataSet ds = null;
        private String[] languageids = null;
        private HashMap<String, String> currentRowMap = new HashMap();
        private int currentRow = 0;
        private String currentTextid = "";
        private String currentTextType = "";
        private boolean isDone = false;

        public TranslationDataSource(DataSet translationDataSet, String[] languageids) {
            this.ds = translationDataSet;
            this.languageids = languageids;
        }

        public boolean next() throws JRException {
            boolean r;
            if (this.isDone) {
                return false;
            }
            if (this.currentRow == 0) {
                this.currentTextid = this.ds.getValue(this.currentRow, "textid");
                this.currentTextType = this.ds.getValue(this.currentRow, "texttype");
            }
            this.currentRowMap.clear();
            this.currentRowMap.put("textid", this.ds.getValue(this.currentRow, "textid"));
            this.currentRowMap.put("texttype", this.ds.getValue(this.currentRow, "texttype"));
            this.currentRowMap.put("clientsideflag", this.ds.getValue(this.currentRow, "clientsideflag"));
            while (this.currentRow < this.ds.getRowCount() && this.ds.getValue(this.currentRow, "textid").equals(this.currentTextid) && this.ds.getValue(this.currentRow, "texttype").equals(this.currentTextType)) {
                this.currentRowMap.put(this.ds.getValue(this.currentRow, "languageid").toLowerCase(), this.ds.getValue(this.currentRow, "transtext"));
                ++this.currentRow;
            }
            this.currentTextid = this.ds.getValue(this.currentRow, "textid");
            this.currentTextType = this.ds.getValue(this.currentRow, "texttype");
            boolean bl = r = this.currentRow < this.ds.getRowCount();
            if (this.currentRow == this.ds.getRowCount()) {
                r = true;
                this.isDone = true;
            }
            return r;
        }

        public Object getFieldValue(JRField jrField) throws JRException {
            String fieldid = jrField.getName();
            return this.getFieldValue(fieldid);
        }

        public Object getFieldValue(String fieldid) {
            return this.currentRowMap.get(fieldid);
        }
    }
}

