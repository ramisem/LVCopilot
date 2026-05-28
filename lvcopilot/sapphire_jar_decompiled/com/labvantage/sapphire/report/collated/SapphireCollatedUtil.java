/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.BaseParagraph
 *  com.aspose.pdf.BorderInfo
 *  com.aspose.pdf.Color
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.GoToAction
 *  com.aspose.pdf.Heading
 *  com.aspose.pdf.Hyperlink
 *  com.aspose.pdf.LocalHyperlink
 *  com.aspose.pdf.Matrix
 *  com.aspose.pdf.Operator
 *  com.aspose.pdf.OutlineItemCollection
 *  com.aspose.pdf.Page
 *  com.aspose.pdf.PageNumberStamp
 *  com.aspose.pdf.PdfAction
 *  com.aspose.pdf.Rectangle
 *  com.aspose.pdf.Row
 *  com.aspose.pdf.Stamp
 *  com.aspose.pdf.Table
 *  com.aspose.pdf.TextFragment
 *  com.aspose.pdf.TextFragmentAbsorber
 *  com.aspose.pdf.TextFragmentCollection
 *  com.aspose.pdf.TextSegment
 *  com.aspose.pdf.TocInfo
 *  com.aspose.pdf.XImage
 *  com.aspose.pdf.operators.ConcatenateMatrix
 *  com.aspose.pdf.operators.Do
 *  com.aspose.pdf.operators.GRestore
 *  com.aspose.pdf.operators.GSave
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBuilder
 *  com.aspose.words.FindReplaceOptions
 *  com.aspose.words.IReplacingCallback
 *  com.aspose.words.Node
 *  com.aspose.words.ReplacingArgs
 *  javax.servlet.http.HttpServletRequest
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.report.collated;

import com.aspose.pdf.BaseParagraph;
import com.aspose.pdf.BorderInfo;
import com.aspose.pdf.Color;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.Heading;
import com.aspose.pdf.Hyperlink;
import com.aspose.pdf.LocalHyperlink;
import com.aspose.pdf.Matrix;
import com.aspose.pdf.Operator;
import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageNumberStamp;
import com.aspose.pdf.PdfAction;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.Row;
import com.aspose.pdf.Stamp;
import com.aspose.pdf.Table;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.aspose.pdf.TextFragmentCollection;
import com.aspose.pdf.TextSegment;
import com.aspose.pdf.TocInfo;
import com.aspose.pdf.XImage;
import com.aspose.pdf.operators.ConcatenateMatrix;
import com.aspose.pdf.operators.Do;
import com.aspose.pdf.operators.GRestore;
import com.aspose.pdf.operators.GSave;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.IReplacingCallback;
import com.aspose.words.Node;
import com.aspose.words.ReplacingArgs;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SapphireCollatedUtil {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String validateReportGeneration(DataSet allreportItems, DataSet parentReportParam, DataSet reportParamMapInfo, HashMap<String, String> requestParamMap, String connectioid, List<String> reportitemList) {
        HashMap<String, String> filter = new HashMap<String, String>();
        StringBuilder message = new StringBuilder();
        if (OpalUtil.isNotEmpty(allreportItems)) {
            filter.put("itemtype", "Report");
            DataSet childreports = allreportItems.getFilteredDataSet(filter);
            HashMap requiredChildReportParamMap = new HashMap();
            Boolean includeAllReport = false;
            try {
                int i;
                ArrayList<String> parentReportParameters = new ArrayList<String>();
                for (i = 0; i < parentReportParam.getRowCount(); ++i) {
                    parentReportParameters.add(parentReportParam.getString(i, "paramid"));
                }
                for (i = 0; i < childreports.getRowCount(); ++i) {
                    if (!reportitemList.contains(allreportItems.getString(i, "reportitemid"))) continue;
                    ArrayList<String> childReportParamList = new ArrayList<String>();
                    String childReportid = childreports.getString(i, "childreportid");
                    String childReportVersionid = childreports.getString(i, "childreportversionid");
                    DataSet childReportParams = SapphireCollatedUtil.getReportParam(connectioid, childReportid, childReportVersionid);
                    for (int count = 0; count < childReportParams.getRowCount(); ++count) {
                        if (includeAllReport.booleanValue()) {
                            childReportParamList.add(childReportParams.getString(count, "paramid") + ";" + childReportParams.getString(count, "paraminto"));
                            continue;
                        }
                        if (!OpalUtil.isNotEmpty(childReportParams.getString(count, "mandatoryflag")) || !childReportParams.getString(count, "mandatoryflag").equalsIgnoreCase("Y")) continue;
                        childReportParamList.add(childReportParams.getString(count, "paramid") + ";" + childReportParams.getString(count, "paraminto"));
                    }
                    if (!OpalUtil.isNotEmpty(childReportParamList)) continue;
                    requiredChildReportParamMap.put(childReportid, childReportParamList);
                }
                HashMap reportParamMapping = new HashMap();
                if (OpalUtil.isNotEmpty(reportParamMapInfo)) {
                    for (int i2 = 0; i2 < reportParamMapInfo.getRowCount(); ++i2) {
                        if (reportParamMapping.containsKey(reportParamMapInfo.getString(i2, "childreportid"))) {
                            ((List)reportParamMapping.get(reportParamMapInfo.getString(i2, "childreportid"))).add(reportParamMapInfo.getString(i2, "childreportparamid"));
                            continue;
                        }
                        ArrayList<String> childparams = new ArrayList<String>();
                        childparams.add(reportParamMapInfo.getString(i2, "childreportparamid"));
                        reportParamMapping.put(reportParamMapInfo.getString(i2, "childreportid"), childparams);
                    }
                }
                HashMap missingParamMapConfiguration = new HashMap();
                HashMap missingValueForMandatoryParams = new HashMap();
                for (Map.Entry entry : requiredChildReportParamMap.entrySet()) {
                    String reportid = (String)entry.getKey();
                    List requiredChildParamList = (List)entry.getValue();
                    if (reportParamMapping.containsKey(reportid)) {
                        List mappedChildParamList = (List)reportParamMapping.get(reportid);
                        for (String paramidinfo : requiredChildParamList) {
                            String[] paraidarray = StringUtil.split(paramidinfo, ";");
                            String paramid = paraidarray[0];
                            String paraminto = paraidarray[1];
                            if (mappedChildParamList.contains(paramid)) continue;
                            if (!parentReportParameters.contains(paramid)) {
                                if (missingParamMapConfiguration.containsKey(reportid)) {
                                    ((List)missingParamMapConfiguration.get(reportid)).add(paramid);
                                    continue;
                                }
                                ArrayList<String> missingParams = new ArrayList<String>();
                                missingParams.add(paramid);
                                missingParamMapConfiguration.put(reportid, missingParams);
                                continue;
                            }
                            if (!OpalUtil.isEmpty(requestParamMap.get(paramid)) || !OpalUtil.isEmpty(requestParamMap.get(paraminto))) continue;
                            if (missingValueForMandatoryParams.containsKey(reportid)) {
                                ((List)missingValueForMandatoryParams.get(reportid)).add(paramid);
                                continue;
                            }
                            ArrayList<String> mandatoryParams = new ArrayList<String>();
                            mandatoryParams.add(paramid);
                            missingValueForMandatoryParams.put(reportid, mandatoryParams);
                        }
                        continue;
                    }
                    for (String paramidinfo : requiredChildParamList) {
                        String[] paraidarray = StringUtil.split(paramidinfo, ";");
                        String paramid = paraidarray[0];
                        String paraminto = paraidarray[1];
                        if (parentReportParameters.contains(paramid)) continue;
                        if (missingParamMapConfiguration.containsKey(reportid)) {
                            ((List)missingParamMapConfiguration.get(reportid)).add(paramid);
                            continue;
                        }
                        ArrayList<String> missingParams = new ArrayList<String>();
                        missingParams.add(paramid);
                        missingParamMapConfiguration.put(reportid, missingParams);
                    }
                }
                if (missingParamMapConfiguration.size() > 0) {
                    message.append("Parameter mapping is missing for below child reports:");
                    for (Map.Entry missingreportid : missingParamMapConfiguration.entrySet()) {
                        message.append("<ul>").append("<li><u>").append((String)missingreportid.getKey()).append("</u>").append(" - " + String.join((CharSequence)",", (Iterable)missingreportid.getValue()));
                        message.append("</li>").append("</ul>");
                    }
                }
                if (missingValueForMandatoryParams.size() <= 0) return message.toString();
                message.append("Parameter is mandatory for below child reports:");
                for (Map.Entry missingreportid : missingValueForMandatoryParams.entrySet()) {
                    message.append("<ul>").append("<li><u>").append((String)missingreportid.getKey()).append("</u>").append(" - " + String.join((CharSequence)",", (Iterable)missingreportid.getValue()));
                    message.append("</li>").append("</ul>");
                }
                return message.toString();
            }
            catch (Exception exp) {
                message = new StringBuilder();
                message.append("Failed to get report params. Exception: " + exp.getMessage());
                return message.toString();
            }
        }
        message.append("There are no child items to run this Collated Report");
        return message.toString();
    }

    public static String validateParentReport(String reportid, DataSet parentReportParam, HashMap<String, String> requestParamMap) {
        HashMap missingValueForMandatoryParams = new HashMap();
        StringBuilder message = new StringBuilder();
        for (int count = 0; count < parentReportParam.getRowCount(); ++count) {
            String paramid = parentReportParam.getString(count, "paramid");
            String paraminto = parentReportParam.getString(count, "paraminto");
            if (!OpalUtil.isEmpty(requestParamMap.get(paramid)) || !OpalUtil.isEmpty(requestParamMap.get(paraminto))) continue;
            if (missingValueForMandatoryParams.containsKey(reportid)) {
                ((List)missingValueForMandatoryParams.get(reportid)).add(paramid);
                continue;
            }
            ArrayList<String> mandatoryParams = new ArrayList<String>();
            mandatoryParams.add(paramid);
            missingValueForMandatoryParams.put(reportid, mandatoryParams);
        }
        if (missingValueForMandatoryParams.size() > 0) {
            message.append("Parameter is mandatory for below child reports:");
            for (Map.Entry missingreportid : missingValueForMandatoryParams.entrySet()) {
                message.append("<ul>").append("<li><u>").append((String)missingreportid.getKey()).append("</u>").append(" - " + String.join((CharSequence)",", (Iterable)missingreportid.getValue()));
                message.append("</li>").append("</ul>");
            }
        }
        return message.toString();
    }

    public static void setpageNumberForDoc(TranslationProcessor tp, Document docWithTOC) {
        DocumentBuilder builder = new DocumentBuilder(docWithTOC);
        builder.moveToHeaderFooter(3);
        builder.getCurrentParagraph().getParagraphFormat().setAlignment(2);
        builder.write(tp.translate("Page"));
        builder.write(tp.translate(" "));
        builder.insertField("PAGE", "");
        builder.write(tp.translate(" "));
        builder.write(tp.translate("of"));
        builder.write(tp.translate(" "));
        builder.insertField("NUMPAGES", "");
    }

    public static String parseExpression(String expression, HashMap parammap) {
        String[] tokens = StringUtil.getTokens(expression, "[", "]");
        String value = expression;
        for (String token : tokens) {
            value = (String)parammap.get(token);
        }
        return value;
    }

    public static void addBookmarks(String collatedReportid, String collatedReportDesc, Object document, Map<String, Integer> pageCount) {
        switch (document.getClass().getCanonicalName()) {
            case "com.aspose.pdf.Document": {
                ArrayList<OutlineItemCollection> childBookmarks = new ArrayList<OutlineItemCollection>();
                com.aspose.pdf.Document pdfDocument = (com.aspose.pdf.Document)document;
                for (Map.Entry<String, Integer> entry : pageCount.entrySet()) {
                    String reportid = entry.getKey().split("#")[0];
                    String reportDesc = entry.getKey().split("#")[1];
                    if (reportid.equalsIgnoreCase(collatedReportid)) continue;
                    OutlineItemCollection pdfOutline = new OutlineItemCollection(pdfDocument.getOutlines());
                    pdfOutline.setTitle(reportDesc);
                    pdfOutline.setItalic(true);
                    pdfOutline.setBold(true);
                    pdfOutline.setAction((PdfAction)new GoToAction(pdfDocument.getPages().get_Item(entry.getValue().intValue())));
                    childBookmarks.add(pdfOutline);
                }
                OutlineItemCollection parentBookmark = new OutlineItemCollection(pdfDocument.getOutlines());
                parentBookmark.setTitle(collatedReportid);
                parentBookmark.setItalic(true);
                parentBookmark.setBold(true);
                parentBookmark.setAction((PdfAction)new GoToAction(pdfDocument.getPages().get_Item(pageCount.get(collatedReportid + "#" + collatedReportDesc).intValue())));
                for (OutlineItemCollection childbookmark : childBookmarks) {
                    parentBookmark.add(childbookmark);
                }
                pdfDocument.getOutlines().add(parentBookmark);
                SapphireCollatedUtil.expandedBookmarks((com.aspose.pdf.Document)document);
                break;
            }
            case "com.aspose.words.Document": {
                Document document2 = (Document)document;
                DocumentBuilder builder = new DocumentBuilder(document2);
                builder.startBookmark("AsposeBookmark");
                builder.endBookmark("AsposeBookmark");
                break;
            }
        }
    }

    private static void expandedBookmarks(com.aspose.pdf.Document document) {
        document.setPageMode(1);
        for (int counter = 1; counter <= document.getOutlines().size(); ++counter) {
            document.getOutlines().get_Item(counter).setOpen(true);
        }
    }

    public static void replaceTokens(Map<String, String> replaceMap, Document document) {
        for (String key : replaceMap.keySet()) {
            try {
                if (replaceMap.get(key) == null) continue;
                document.getRange().replace(key, replaceMap.get(key));
            }
            catch (Exception exception) {}
        }
    }

    public static void replaceTextWithImageForPDF(com.aspose.pdf.Document document, String stringToReplace, InputStream image) {
        if (image != null) {
            for (Page page : document.getPages()) {
                page.getContents().add((Operator)new GSave());
                TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber(stringToReplace);
                page.accept(textFragmentAbsorber);
                TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
                for (TextFragment textFragment : textFragmentCollection) {
                    page.getResources().getImages().add(image);
                    Rectangle rectangle = textFragment.getRectangle();
                    XImage ximage = page.getResources().getImages().get_Item(page.getResources().getImages().size());
                    Matrix matrix = new Matrix(new double[]{120.0, 0.0, 0.0, 60.0, rectangle.getLLX(), rectangle.getLLY()});
                    page.getContents().add((Operator)new ConcatenateMatrix(matrix));
                    page.getContents().add((Operator)new Do(ximage.getName()));
                    page.getContents().add((Operator)new GRestore());
                    textFragment.setText("");
                }
            }
        }
    }

    public static void replaceTextWithImageForWord(Document document, String stringToReplace, InputStream image) throws Exception {
        document.getRange().replace(stringToReplace, "report.logo");
        FindReplaceOptions options = new FindReplaceOptions();
        options.setReplacingCallback((IReplacingCallback)new ReplaceWithHtmlEvaluator(image));
        document.getRange().replace("report.logo", "", options);
    }

    public static ByteArrayInputStream getLogo(HttpServletRequest request, String connectioid, String addressid) {
        String reportaddressid = "";
        if (request != null) {
            reportaddressid = request.getParameter("reportaddressid");
        }
        if (OpalUtil.isEmpty(reportaddressid)) {
            reportaddressid = OpalUtil.isNotEmpty(addressid) ? addressid : "Global";
        }
        SDIData address = SapphireCollatedUtil.populatedAddressInfo(connectioid, reportaddressid);
        DataSet logoattachment = address.getDataset("attachment");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("attachmentclass", "ReportLogo");
        DataSet logo = logoattachment.getFilteredDataSet(filter);
        FileManager.TempFile attachment = FileManager.getAttachment(logo.getString(0, "sdcid"), logo.getString(0, "keyid1"), logo.getString(0, "keyid2"), logo.getString(0, "keyid3"), logo.getString(0, "attachmentclass"), connectioid);
        return attachment != null ? SapphireCollatedUtil.getByteArrayInputStream(attachment, connectioid) : null;
    }

    public static ByteArrayInputStream getByteArrayInputStream(FileManager.TempFile attachment, String connectioid) {
        FileTypeGroup type = FileTypeGroup.getFileTypeGroupByType(attachment.getMimeType(), connectioid);
        byte[] bytes = null;
        if (type == FileTypeGroup.IMAGE) {
            bytes = attachment.getData().getData();
            return new ByteArrayInputStream(bytes);
        }
        FileManager.FileData preview = FileManager.generateThumbnail(attachment.getData(), -1, -1, new Logger(new LogContext()), connectioid);
        return new ByteArrayInputStream(Base64.decodeBase64((byte[])preview.getBase64().getBytes()));
    }

    public static void setPageNumber(com.aspose.pdf.Document document, String collatedReportTitle, TranslationProcessor tp) throws Exception {
        int i;
        int pageCount = 0;
        pageCount = document.getPages().size();
        PageNumberStamp pageNumberStamp = SapphireCollatedUtil.getPageNumberStamp(pageCount, tp);
        int n = i = OpalUtil.isNotEmpty(collatedReportTitle) ? 2 : 1;
        while (i <= pageCount) {
            document.getPages().get_Item(i).addStamp((Stamp)pageNumberStamp);
            ++i;
        }
    }

    public static PageNumberStamp getPageNumberStamp(int pageCount, TranslationProcessor tp) {
        PageNumberStamp pageNumberStamp = new PageNumberStamp();
        pageNumberStamp.setBackground(false);
        pageNumberStamp.setFormat(tp.translate("Page # of " + pageCount));
        pageNumberStamp.setBottomMargin(10.0);
        pageNumberStamp.setRightMargin(20.0);
        pageNumberStamp.setHorizontalAlignment(3);
        pageNumberStamp.setStartingNumber(1);
        pageNumberStamp.getTextState().setFontSize(10.0f);
        pageNumberStamp.getTextState().setFontStyle(1);
        return pageNumberStamp;
    }

    public static void createTOCPage(com.aspose.pdf.Document pdfDocument, Map<String, Integer> pageCount, String collatedreportid, String collatedReportTitle, TranslationProcessor tp) {
        Table table = new Table();
        table.setBorder(new BorderInfo(15, 0.0f, Color.getBlack()));
        table.setDefaultCellBorder(new BorderInfo(15, 0.0f, Color.getBlack()));
        Row row = table.getRows().add();
        for (int cellCount = 1; cellCount < 6; ++cellCount) {
            row.getCells().add("");
        }
        pdfDocument.processParagraphs();
        Page tocPage = pdfDocument.getPages().insert(OpalUtil.isNotEmpty(collatedReportTitle) ? 2 : 1);
        TocInfo tocInfo = new TocInfo();
        TextFragment title = new TextFragment(tp.translate("Table Of Contents"));
        title.getTextState().setFontSize(11.0f);
        tocInfo.setTitle(title);
        tocInfo.setShowPageNumbers(false);
        tocPage.setTocInfo(tocInfo);
        for (Map.Entry<String, Integer> entry : pageCount.entrySet()) {
            String reportid = entry.getKey().split("#")[0];
            String reportDesc = entry.getKey().split("#")[1];
            if (reportid.equalsIgnoreCase(collatedreportid)) continue;
            Heading heading = new Heading(1);
            TextSegment segment = new TextSegment();
            heading.setTocPage(tocPage);
            heading.getSegments().add(segment);
            LocalHyperlink link = new LocalHyperlink();
            link.setTargetPageNumber(entry.getValue() + 1);
            heading.setHyperlink((Hyperlink)link);
            int reportlen = reportDesc.length();
            int restofLen = 120 - reportlen;
            StringBuilder contentheading = new StringBuilder();
            for (int j = 0; j <= restofLen; ++j) {
                contentheading.append(".");
            }
            segment.setText(reportDesc + contentheading + (entry.getValue() + 1));
            tocPage.getParagraphs().add((BaseParagraph)heading);
        }
    }

    private static SDIData populatedAddressInfo(String connectioid, String addressid) {
        SDIProcessor sdiProcessor = new SDIProcessor(connectioid);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachment");
        sdiRequest.setSDCid("Address");
        sdiRequest.setQueryFrom("address");
        sdiRequest.setQueryWhere("addressid =  '" + addressid + "' AND addresstype = 'ReportAddress'");
        return sdiProcessor.getSDIData(sdiRequest);
    }

    private static DataSet getReportParam(String connectioid, String reportid, String reportversionid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
        QueryProcessor qp = new QueryProcessor(connectioid);
        if (OpalUtil.isEmpty(reportversionid)) {
            reportversionid = SapphireCollatedUtil.getLatestReportVersionid(qp, reportid);
        }
        query.append(" SELECT * from reportparam").append(" WHERE reportid = ").append(safeSQL.addVar(reportid)).append(" AND reportversionid = ").append(safeSQL.addVar(reportversionid)).append(" order by usersequence");
        return qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
    }

    private static String getLatestReportVersionid(QueryProcessor qp, String reportid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
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

    static class ReplaceWithHtmlEvaluator
    implements IReplacingCallback {
        InputStream image = null;

        public ReplaceWithHtmlEvaluator(InputStream image) {
            this.image = image;
        }

        public int replacing(ReplacingArgs e) throws Exception {
            Node currentNode = e.getMatchNode();
            DocumentBuilder builder = new DocumentBuilder((Document)e.getMatchNode().getDocument());
            builder.moveTo(currentNode);
            builder.insertImage(this.image, 150.0, 40.0);
            e.getReplacement();
            currentNode.remove();
            return 1;
        }
    }
}

