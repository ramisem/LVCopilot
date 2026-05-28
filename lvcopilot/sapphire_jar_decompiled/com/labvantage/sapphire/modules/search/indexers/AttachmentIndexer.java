/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Cell
 *  com.aspose.cells.Workbook
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.TextAbsorber
 *  com.aspose.slides.AutoShape
 *  com.aspose.slides.IAutoShape
 *  com.aspose.slides.IParagraph
 *  com.aspose.slides.IPortion
 *  com.aspose.slides.ISmartArt
 *  com.aspose.slides.ISmartArtNode
 *  com.aspose.slides.ITable
 *  com.aspose.slides.ITextFrame
 *  com.aspose.slides.LegacyDiagram
 *  com.aspose.slides.Presentation
 *  com.aspose.slides.SmartArt
 *  com.aspose.slides.Table
 *  com.aspose.words.Document
 *  org.apache.lucene.document.Document
 *  org.apache.lucene.document.Field$Store
 *  org.apache.lucene.document.StringField
 *  org.apache.lucene.document.TextField
 *  org.apache.lucene.index.IndexWriter
 *  org.apache.lucene.index.IndexableField
 *  org.apache.lucene.index.Term
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.search.indexers;

import com.aspose.cells.Cell;
import com.aspose.cells.Workbook;
import com.aspose.pdf.TextAbsorber;
import com.aspose.slides.AutoShape;
import com.aspose.slides.IAutoShape;
import com.aspose.slides.IParagraph;
import com.aspose.slides.IPortion;
import com.aspose.slides.ISmartArt;
import com.aspose.slides.ISmartArtNode;
import com.aspose.slides.ITable;
import com.aspose.slides.ITextFrame;
import com.aspose.slides.LegacyDiagram;
import com.aspose.slides.Presentation;
import com.aspose.slides.SmartArt;
import com.aspose.slides.Table;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileType;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;

public class AttachmentIndexer
extends BaseIndexer {
    private String sdcid;
    public static final String LOGNAME = "AttachmentIndexer";

    public AttachmentIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, String sdcid, String resultsetName) {
        super(indexer, dbu, indexWriter, null, resultsetName);
        this.sdcid = sdcid;
    }

    public AttachmentIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter) {
        super(indexer, dbu, indexWriter, null);
    }

    @Override
    public void indexSet() {
        Trace.logDebug(LOGNAME, " AttachmentIndexer.indexSet()");
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        String typeflag = "";
        StringBuffer contentInput = new StringBuffer();
        String fileType = "";
        long fileSize = 0L;
        try {
            String keyid1 = this.getValue("keyid1");
            String keyid2 = this.getValue("keyid2");
            String keyid3 = this.getValue("keyid3");
            int attachmentNum = this.getInt("attachmentnum");
            if (this.sdcConditionMatch(this.sdcid)) {
                String correctedKeyid1 = this.substituteChars(keyid1);
                String correctedKeyid2 = this.substituteChars(keyid2);
                String correctedKeyid3 = this.substituteChars(keyid3);
                id = "ATTACHMENT;" + this.sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3 + ";" + attachmentNum;
                mapId = "ATTACHMENT;" + this.sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + attachmentNum;
                String attachmentdesc = this.getValue("attachmentdesc");
                String filename = this.getValue("filename");
                typeflag = this.getValue("typeflag");
                org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                this.addKeyFields(id, "ATTACHMENT", this.sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                doc.add((IndexableField)new StringField("desccol", attachmentdesc.toLowerCase(), Field.Store.YES));
                doc.add((IndexableField)new StringField("filename", filename.toLowerCase(), Field.Store.YES));
                this.addAuditFields(doc);
                PropertyList sdcPolicy = this.indexer.getSDCPolicy(this.sdcid);
                if (sdcPolicy.getProperty("childsdc").equals("Y")) {
                    doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid1"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid1"))), Field.Store.YES));
                    if (sdcPolicy.getProperty("parentkeycolid2").length() > 0) {
                        doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid2"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid2"))), Field.Store.YES));
                    }
                    if (sdcPolicy.getProperty("parentkeycolid3").length() > 0) {
                        doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid3"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid3"))), Field.Store.YES));
                    }
                }
                contentInput.append(" ").append(attachmentdesc);
                contentInput.append(" ").append(filename).append(" ");
                boolean contentAdded = false;
                if (typeflag.equals("S") || typeflag.equals("F") || typeflag.equals("R") || typeflag.equals("U") || typeflag.equals("M") || typeflag.equals("P")) {
                    String connectionid = this.indexer.getConnectionid();
                    if (connectionid != null && connectionid.length() > 0) {
                        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionid);
                        sapphire.attachment.Attachment retrievedAttachment = sapphire.attachment.Attachment.getAttachment(this.sdcid, keyid1, keyid2, keyid3, attachmentNum);
                        retrievedAttachment = attachmentProcessor.getSDIAttachment(retrievedAttachment, Attachment.ThumbnailGeneration.DISABLED);
                        long maxAttachmentSizeAllowed = Long.valueOf(this.indexer.getIndexingPolicy().getProperty("maxattachmentsize", "100000000"));
                        if (retrievedAttachment != null) {
                            byte[] byteData = null;
                            if (typeflag.equals("M")) {
                                String clob = retrievedAttachment.getClob();
                                if (clob != null && clob.length() > 0) {
                                    Document jdoc = Jsoup.parse((String)clob);
                                    byteData = jdoc.body().text().getBytes();
                                    fileSize = byteData == null ? 0L : (long)byteData.length;
                                }
                            } else if (typeflag.equals("P")) {
                                String clob = retrievedAttachment.getClob();
                                if (clob != null && clob.length() > 0) {
                                    byteData = clob.getBytes();
                                    fileSize = byteData == null ? 0L : (long)byteData.length;
                                }
                            } else {
                                fileSize = retrievedAttachment.getSize();
                            }
                            if (fileSize == 0L) {
                                throw new SapphireException("Could not locate attachment or attachment is empty");
                            }
                            if (fileSize > maxAttachmentSizeAllowed) {
                                throw new Exception("File exceeds " + fileSize / 0x100000L + "MB indexing file size limit");
                            }
                            fileType = AttachmentIndexer.extractAttachmentContent((Attachment)retrievedAttachment, byteData, contentInput, connectionid);
                        } else {
                            Trace.logInfo(LOGNAME, "SDC " + this.sdcid + " has a NULL attachment object");
                            Trace.logInfo(LOGNAME, " keyid1: " + keyid1);
                            Trace.logInfo(LOGNAME, " keyid2: " + keyid2);
                            Trace.logInfo(LOGNAME, " keyid3: " + keyid3);
                            Trace.logInfo(LOGNAME, " attachmentnum: " + attachmentNum);
                        }
                    } else {
                        throw new Exception("Attempt to use old attachment API found. This has now been deprecated.");
                    }
                }
                if (!contentAdded) {
                    TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString()))));
                    doc.add((IndexableField)content);
                }
                this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                this.updateIndexMap(mapId, now, "ATTACHMENT", this.sdcid, contentInput.length(), typeflag, fileType, fileSize);
            } else {
                Trace.logDebug(LOGNAME, "SDC Condition not match for indexing. Remove Attachment from index.");
                this.delete(this.sdcid, keyid1, keyid2, keyid3, attachmentNum);
            }
        }
        catch (Throwable e) {
            try {
                String message = e.getMessage() == null ? "Indexing failed for unknown reason" : e.getMessage();
                Trace.logError(LOGNAME, (Object)("Failed to index sdiattachment '" + (id != null ? id : "N/A") + "'. Reason: " + message), e);
                this.updateIndexMap(mapId, now, "ATTACHMENT", this.sdcid, contentInput.length(), typeflag, fileType, fileSize, message);
            }
            catch (SapphireException se) {
                Trace.logError(LOGNAME, (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }

    public static String extractAttachmentContent(Attachment attachment, byte[] byteData, StringBuffer contentInput, String connectionid) throws Exception {
        String fileType = null;
        String type = attachment.getType();
        if (type.equals("M") || type.equals("P")) {
            if (byteData != null && byteData.length > 0) {
                fileType = AttachmentIndexer.extractAttachmentContent(attachment, contentInput, byteData, connectionid);
            }
        } else {
            fileType = AttachmentIndexer.extractAttachmentContent(attachment, contentInput, connectionid);
        }
        return fileType;
    }

    public void delete(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum) {
        try {
            String correctedKeyid1 = this.substituteChars(keyid1);
            String correctedKeyid2 = this.substituteChars(keyid2);
            String correctedKeyid3 = this.substituteChars(keyid3);
            String id = "ATTACHMENT;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3 + ";" + attachmentNum;
            String mapId = "ATTACHMENT;" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + attachmentNum;
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, "Unexpected error during delete attachment index");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static String extractAttachmentContent(Attachment attachment, StringBuffer contentInput, String connectionId) throws Exception {
        block30: {
            block31: {
                block29: {
                    inputStream = attachment.getInputStream();
                    filename = attachment.getFilename();
                    fileType = "";
                    v0 = extension = filename != null && filename.length() > 0 && filename.lastIndexOf(".") != -1 ? filename.substring(filename.lastIndexOf(".")).toLowerCase() : "";
                    if (extension.equalsIgnoreCase(FileType.getFileTypeByName("PDF", connectionId).getExtension())) {
                        fileType = "PDF";
                        document = null;
                        try {
                            document = new com.aspose.pdf.Document(inputStream);
                            textAbsorber = new TextAbsorber();
                            document.getPages().accept(textAbsorber);
                            text = textAbsorber.getText();
                            contentInput.append(" ").append(text);
                            document.close();
                        }
                        finally {
                            inputStream.close();
                            if (document != null) {
                                document.close();
                            }
                        }
                    }
                    if (extension.equalsIgnoreCase(FileType.getFileTypeByName("DOC", connectionId).getExtension()) || extension.equalsIgnoreCase(FileType.getFileTypeByName("DOCX", connectionId).getExtension())) {
                        fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("DOC", connectionId).getExtension()) != false ? "DOC" : "DOCX";
                        try {
                            doc = new com.aspose.words.Document(inputStream);
                            contentInput.append(" ").append(doc.toString(70));
                        }
                        finally {
                            inputStream.close();
                        }
                    }
                    if (!extension.equalsIgnoreCase(FileType.getFileTypeByName("TXT", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("CSV", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("LOG", connectionId).getExtension())) break block29;
                    fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("CSV", connectionId).getExtension()) != false ? "CSV" : "TXT";
                    contentInput.append(new String(attachment.getData(), "UTF-8"));
                    break block30;
                }
                if (extension.equalsIgnoreCase(FileType.getFileTypeByName("XLS", connectionId).getExtension()) || extension.equalsIgnoreCase(FileType.getFileTypeByName("XLSX", connectionId).getExtension())) {
                    fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("XLS", connectionId).getExtension()) != false ? "XLS" : "XLSX";
                    try {
                        workbook = new Workbook(inputStream);
                        for (i = 0; i < workbook.getWorksheets().getCount(); ++i) {
                            worksheet = workbook.getWorksheets().get(i);
                            cells = worksheet.getCells();
                            iterator = cells.iterator();
                            while (iterator.hasNext()) {
                                try {
                                    cell = (Cell)iterator.next();
                                    stringValue = cell.getStringValue();
                                    if (stringValue == null || stringValue.length() <= 0) continue;
                                    contentInput.append(" ").append(stringValue);
                                }
                                catch (Exception cell) {}
                            }
                        }
                    }
                    finally {
                        inputStream.close();
                    }
                }
                if (!extension.equalsIgnoreCase(FileType.getFileTypeByName("PPT", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("PPTX", connectionId).getExtension())) break block31;
                fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("PPT", connectionId).getExtension()) != false ? "PPT" : "PPTX";
                try {
                    presentation = new Presentation(inputStream);
                    out = new StringBuilder();
                    slides = presentation.getSlides();
                    slide = null;
                    shape = null;
                    for (i = 0; i < slides.size(); ++i) {
                        slide = slides.get_Item(i);
lbl71:
                        // 4 sources

                        try {
                            for (j = 0; j < slide.getShapes().size(); ++j) {
                                block33: {
                                    block32: {
                                        shape = slide.getShapes().get_Item(j);
                                        if (shape instanceof AutoShape) {
                                            if (((IAutoShape)shape).getTextFrame() == null) continue;
                                            AttachmentIndexer.extractShapeText(out, ((IAutoShape)shape).getTextFrame());
                                            continue;
                                        }
                                        if (!(shape instanceof LegacyDiagram)) break block32;
                                        legacy = (LegacyDiagram)shape;
                                        smart = legacy.convertToSmartArt();
                                        for (ISmartArtNode node : smart.getAllNodes()) {
                                            if (node.getTextFrame() == null) continue;
                                            AttachmentIndexer.extractShapeText(out, node.getTextFrame());
                                        }
                                        ** GOTO lbl71
                                    }
                                    if (!(shape instanceof SmartArt)) break block33;
                                    smart = (ISmartArt)shape;
                                    for (ISmartArtNode node : smart.getAllNodes()) {
                                        if (node.getTextFrame() == null) continue;
                                        AttachmentIndexer.extractShapeText(out, node.getTextFrame());
                                    }
                                    ** GOTO lbl71
                                }
                                if (!(shape instanceof Table)) continue;
                                table = (ITable)shape;
                                for (u = 0; u < table.getRows().size(); ++u) {
                                    for (v = 0; v < table.getColumns().size(); ++v) {
                                        cell = table.get_Item(v, u);
                                        if (cell.getTextFrame() == null) continue;
                                        AttachmentIndexer.extractShapeText(out, cell.getTextFrame());
                                    }
                                }
                                ** GOTO lbl71
                            }
                            continue;
                        }
                        catch (Exception var13_28) {
                            // empty catch block
                        }
                    }
                    contentInput.append(" ").append((CharSequence)out);
                }
                finally {
                    inputStream.close();
                }
            }
            fileType = filename.contains(".") != false ? filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : "UNKNOWN";
        }
        return fileType;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static String extractAttachmentContent(Attachment attachment, StringBuffer contentInput, byte[] byteData, String connectionId) throws Exception {
        block30: {
            block32: {
                block31: {
                    block29: {
                        type = attachment.getType();
                        filename = attachment.getFilename();
                        fileType = "";
                        v0 = extension = filename != null && filename.length() > 0 && filename.lastIndexOf(".") != -1 ? filename.substring(filename.lastIndexOf(".")).toLowerCase() : "";
                        if (!type.equals("M") && !type.equals("P")) break block29;
                        fileType = "";
                        contentInput.append(" ").append(new String(byteData, "UTF-8"));
                        break block30;
                    }
                    if (extension.equalsIgnoreCase(FileType.getFileTypeByName("PDF", connectionId).getExtension())) {
                        fileType = "PDF";
                        bais = new ByteArrayInputStream(byteData);
                        document = null;
                        try {
                            document = new com.aspose.pdf.Document((InputStream)bais);
                            textAbsorber = new TextAbsorber();
                            document.getPages().accept(textAbsorber);
                            text = textAbsorber.getText();
                            contentInput.append(" ").append(text);
                            document.close();
                        }
                        finally {
                            bais.close();
                            if (document != null) {
                                document.close();
                            }
                        }
                    }
                    if (extension.equalsIgnoreCase(FileType.getFileTypeByName("DOC", connectionId).getExtension()) || extension.equalsIgnoreCase(FileType.getFileTypeByName("DOCX", connectionId).getExtension())) {
                        fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("DOC", connectionId).getExtension()) != false ? "DOC" : "DOCX";
                        bais = new ByteArrayInputStream(byteData);
                        try {
                            doc = new com.aspose.words.Document((InputStream)bais);
                            contentInput.append(" ").append(doc.toString(70));
                        }
                        finally {
                            bais.close();
                        }
                    }
                    if (!extension.equalsIgnoreCase(FileType.getFileTypeByName("TXT", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("CSV", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("LOG", connectionId).getExtension())) break block31;
                    fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("CSV", connectionId).getExtension()) != false ? "CSV" : "TXT";
                    contentInput.append(new String(byteData, "UTF-8"));
                    break block30;
                }
                if (extension.equalsIgnoreCase(FileType.getFileTypeByName("XLS", connectionId).getExtension()) || extension.equalsIgnoreCase(FileType.getFileTypeByName("XLSX", connectionId).getExtension())) {
                    fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("XLS", connectionId).getExtension()) != false ? "XLS" : "XLSX";
                    bais = new ByteArrayInputStream(byteData);
                    try {
                        workbook = new Workbook((InputStream)bais);
                        for (i = 0; i < workbook.getWorksheets().getCount(); ++i) {
                            worksheet = workbook.getWorksheets().get(i);
                            cells = worksheet.getCells();
                            iterator = cells.iterator();
                            while (iterator.hasNext()) {
                                try {
                                    cell = (Cell)iterator.next();
                                    stringValue = cell.getStringValue();
                                    if (stringValue == null || stringValue.length() <= 0) continue;
                                    contentInput.append(" ").append(stringValue);
                                }
                                catch (Exception cell) {}
                            }
                        }
                    }
                    finally {
                        bais.close();
                    }
                }
                if (!extension.equalsIgnoreCase(FileType.getFileTypeByName("PPT", connectionId).getExtension()) && !extension.equalsIgnoreCase(FileType.getFileTypeByName("PPTX", connectionId).getExtension())) break block32;
                fileType = extension.equalsIgnoreCase(FileType.getFileTypeByName("PPT", connectionId).getExtension()) != false ? "PPT" : "PPTX";
                bais = new ByteArrayInputStream(byteData);
                try {
                    presentation = new Presentation((InputStream)bais);
                    out = new StringBuilder();
                    slides = presentation.getSlides();
                    slide = null;
                    shape = null;
                    for (i = 0; i < slides.size(); ++i) {
                        slide = slides.get_Item(i);
lbl81:
                        // 4 sources

                        try {
                            for (j = 0; j < slide.getShapes().size(); ++j) {
                                block34: {
                                    block33: {
                                        shape = slide.getShapes().get_Item(j);
                                        if (shape instanceof AutoShape) {
                                            if (((IAutoShape)shape).getTextFrame() == null) continue;
                                            AttachmentIndexer.extractShapeText(out, ((IAutoShape)shape).getTextFrame());
                                            continue;
                                        }
                                        if (!(shape instanceof LegacyDiagram)) break block33;
                                        legacy = (LegacyDiagram)shape;
                                        smart = legacy.convertToSmartArt();
                                        for (ISmartArtNode node : smart.getAllNodes()) {
                                            if (node.getTextFrame() == null) continue;
                                            AttachmentIndexer.extractShapeText(out, node.getTextFrame());
                                        }
                                        ** GOTO lbl81
                                    }
                                    if (!(shape instanceof SmartArt)) break block34;
                                    smart = (ISmartArt)shape;
                                    for (ISmartArtNode node : smart.getAllNodes()) {
                                        if (node.getTextFrame() == null) continue;
                                        AttachmentIndexer.extractShapeText(out, node.getTextFrame());
                                    }
                                    ** GOTO lbl81
                                }
                                if (!(shape instanceof Table)) continue;
                                table = (ITable)shape;
                                for (u = 0; u < table.getRows().size(); ++u) {
                                    for (v = 0; v < table.getColumns().size(); ++v) {
                                        cell = table.get_Item(v, u);
                                        if (cell.getTextFrame() == null) continue;
                                        AttachmentIndexer.extractShapeText(out, cell.getTextFrame());
                                    }
                                }
                                ** GOTO lbl81
                            }
                            continue;
                        }
                        catch (Exception var15_33) {
                            // empty catch block
                        }
                    }
                    contentInput.append(" ").append((CharSequence)out);
                }
                finally {
                    bais.close();
                }
            }
            fileType = filename.contains(".") != false ? filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : "UNKNOWN";
        }
        return fileType;
    }

    private static void extractShapeText(StringBuilder out, ITextFrame pptShape) {
        for (int k = 0; k < pptShape.getParagraphs().getCount(); ++k) {
            IParagraph paragraph = pptShape.getParagraphs().get_Item(k);
            for (int n = 0; n < paragraph.getPortions().getCount(); ++n) {
                IPortion portion = paragraph.getPortions().get_Item(n);
                out.append(" ").append(portion.getText());
            }
        }
    }
}

