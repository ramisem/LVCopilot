/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Annotation
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.FileAttachmentAnnotation
 *  com.aspose.pdf.FileSpecification
 *  com.aspose.pdf.MarkupParagraph
 *  com.aspose.pdf.MarkupSection
 *  com.aspose.pdf.Matrix
 *  com.aspose.pdf.Operator
 *  com.aspose.pdf.Page
 *  com.aspose.pdf.PageCollection
 *  com.aspose.pdf.PageMarkup
 *  com.aspose.pdf.ParagraphAbsorber
 *  com.aspose.pdf.Rectangle
 *  com.aspose.pdf.TextFragment
 *  com.aspose.pdf.TextFragmentAbsorber
 *  com.aspose.pdf.TextFragmentCollection
 *  com.aspose.pdf.TextSearchOptions
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
 */
package com.labvantage.sapphire.modules.eln;

import com.aspose.pdf.Annotation;
import com.aspose.pdf.FileAttachmentAnnotation;
import com.aspose.pdf.FileSpecification;
import com.aspose.pdf.MarkupParagraph;
import com.aspose.pdf.MarkupSection;
import com.aspose.pdf.Matrix;
import com.aspose.pdf.Operator;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageCollection;
import com.aspose.pdf.PageMarkup;
import com.aspose.pdf.ParagraphAbsorber;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.aspose.pdf.TextFragmentCollection;
import com.aspose.pdf.TextSearchOptions;
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
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.DigitalSignatureRectangle;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import com.labvantage.sapphire.report.digitalsignature.services.PDFSigningService;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class DocumentUtil
implements ELNConstants {
    public static final String PDF_ADD_ATTACHMENT_PREFIX = "::ADDATTACHMENT::";

    public static void replaceTextOnAllPages(Object document, String textToReplace, String replacedText) throws Exception {
        switch (document.getClass().getCanonicalName()) {
            case "com.aspose.pdf.Document": {
                DocumentUtil.replaceTextOnAllPagesForPDF((com.aspose.pdf.Document)document, textToReplace, replacedText);
                break;
            }
            case "com.aspose.words.Document": {
                DocumentUtil.replaceTextOnAllPagesForWord((Document)document, textToReplace, replacedText);
                break;
            }
        }
    }

    private static void replaceTextOnAllPagesForPDF(com.aspose.pdf.Document document, String textToReplace, String replacedText) {
        TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber(textToReplace);
        document.getPages().accept(textFragmentAbsorber);
        TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
        for (TextFragment textFragment : textFragmentCollection) {
            textFragment.setText(replacedText);
        }
    }

    private static void replaceTextOnAllPagesForWord(Document document, String textToReplace, String replacedText) throws Exception {
        document.getRange().replace(textToReplace, replacedText, new FindReplaceOptions());
    }

    public static void replaceTextWithImage(Object document, String stringToReplace, InputStream image) throws Exception {
        switch (document.getClass().getCanonicalName()) {
            case "com.aspose.pdf.Document": {
                DocumentUtil.replaceTextWithImageForPDF((com.aspose.pdf.Document)document, stringToReplace, image);
                break;
            }
            case "com.aspose.words.Document": {
                DocumentUtil.replaceTextWithImageForWord((Document)document, stringToReplace, image);
                break;
            }
        }
    }

    private static void replaceTextWithImageForPDF(com.aspose.pdf.Document document, String stringToReplace, InputStream image) {
        for (Page page : document.getPages()) {
            page.getContents().add((Operator)new GSave());
            TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber(stringToReplace);
            page.accept(textFragmentAbsorber);
            TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
            for (TextFragment textFragment : textFragmentCollection) {
                page.getResources().getImages().add(image);
                Rectangle rectangle = textFragment.getRectangle();
                XImage ximage = page.getResources().getImages().get_Item(page.getResources().getImages().size());
                Matrix matrix = new Matrix(new double[]{150.0, 0.0, 0.0, 40.0, rectangle.getLLX(), rectangle.getLLY()});
                page.getContents().add((Operator)new ConcatenateMatrix(matrix));
                page.getContents().add((Operator)new Do(ximage.getName()));
                page.getContents().add((Operator)new GRestore());
                textFragment.setText("");
            }
        }
    }

    private static void replaceTextWithImageForWord(Document document, String stringToReplace, InputStream image) throws Exception {
        FindReplaceOptions options = new FindReplaceOptions();
        options.setReplacingCallback((IReplacingCallback)new ReplaceWithHtmlEvaluator(image));
        document.getRange().replace(stringToReplace, "", options);
    }

    public static byte[] signReport(String signingProvider, String signingProviderNode, List<DigitalSignatureRectangle> digitalSignatureRectangleList, SapphireConnection sapphireConnection, com.aspose.pdf.Document pdfDocument) throws ServiceException, SapphireException {
        byte[] pdfbytes;
        block41: {
            pdfbytes = null;
            if (OpalUtil.isNotEmpty(signingProviderNode)) {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
                    pdfDocument.save((OutputStream)byteArrayOutputStream);
                    pdfbytes = byteArrayOutputStream.toByteArray();
                    if (pdfbytes != null) {
                        QueryProcessor qp = new QueryProcessor(sapphireConnection.getConnectionId());
                        for (DigitalSignatureRectangle digitalSignatureRectangle : digitalSignatureRectangleList) {
                            sapphire.attachment.Attachment signatureAttachment = DocumentUtil.getSignatureAttachment(sapphireConnection, qp);
                            SignatureData signatureData = DocumentUtil.getSignatureData(sapphireConnection, qp);
                            signatureData.setDigitalSignatureRectangle(digitalSignatureRectangle);
                            signatureData.setSignatureShape(DocumentUtil.getRectangle2D(digitalSignatureRectangle));
                            signatureData.setSignaturePage(Integer.parseInt(digitalSignatureRectangle.getPage()));
                            PDFSigningService signer = new PDFSigningService(sapphireConnection, signingProvider, signingProviderNode);
                            try {
                                ByteArrayInputStream input = new ByteArrayInputStream(pdfbytes);
                                Throwable throwable = null;
                                try {
                                    InputStream signatureImage = signatureAttachment != null ? signatureAttachment.getInputStream() : null;
                                    Throwable throwable2 = null;
                                    try {
                                        if (signatureImage != null) {
                                            byte[] targetArray = new byte[signatureImage.available()];
                                            signatureImage.read(targetArray);
                                            signatureData.setSignatureImage(targetArray);
                                        }
                                        InputStream signedDocument = signer.signDocument(input, signatureData);
                                        byte[] signedArray = null;
                                        if (signedDocument != null) {
                                            signedArray = new byte[signedDocument.available()];
                                            signedDocument.read(signedArray);
                                        }
                                        pdfbytes = signedArray;
                                    }
                                    catch (Throwable throwable3) {
                                        throwable2 = throwable3;
                                        throw throwable3;
                                    }
                                    finally {
                                        if (signatureImage == null) continue;
                                        if (throwable2 != null) {
                                            try {
                                                signatureImage.close();
                                            }
                                            catch (Throwable throwable4) {
                                                throwable2.addSuppressed(throwable4);
                                            }
                                            continue;
                                        }
                                        signatureImage.close();
                                    }
                                }
                                catch (Throwable throwable5) {
                                    throwable = throwable5;
                                    throw throwable5;
                                }
                                finally {
                                    if (input == null) continue;
                                    if (throwable != null) {
                                        try {
                                            ((InputStream)input).close();
                                        }
                                        catch (Throwable throwable6) {
                                            throwable.addSuppressed(throwable6);
                                        }
                                        continue;
                                    }
                                    ((InputStream)input).close();
                                }
                            }
                            catch (IOException e) {
                                throw new SapphireException("IOException when handling document signature", e);
                            }
                        }
                        break block41;
                    }
                    throw new SapphireException("document data is null");
                }
                catch (Exception e) {
                    throw new SapphireException("Unable to complete worksheet digital signature", e);
                }
            }
        }
        return pdfbytes;
    }

    public static void signReport(String signingProvider, String signingProviderNode, List<DigitalSignatureRectangle> digitalSignatureRectangleList, SapphireConnection sapphireConnection, sapphire.attachment.Attachment pdf) throws SapphireException, ServiceException, IOException {
        if (OpalUtil.isNotEmpty(signingProviderNode)) {
            QueryProcessor qp = new QueryProcessor(sapphireConnection.getConnectionId());
            for (DigitalSignatureRectangle digitalSignatureRectangle : digitalSignatureRectangleList) {
                sapphire.attachment.Attachment signatureAttachment = DocumentUtil.getSignatureAttachment(sapphireConnection, qp);
                SignatureData signatureData = DocumentUtil.getSignatureData(sapphireConnection, qp);
                signatureData.setDigitalSignatureRectangle(digitalSignatureRectangle);
                signatureData.setSignatureShape(DocumentUtil.getRectangle2D(digitalSignatureRectangle));
                signatureData.setSignaturePage(Integer.parseInt(digitalSignatureRectangle.getPage()));
                PDFSigningService signer = new PDFSigningService(sapphireConnection, signingProvider, signingProviderNode);
                signer.signDocument(pdf, signatureData, signatureAttachment);
            }
        }
    }

    private static Rectangle2D getRectangle2D(DigitalSignatureRectangle digitalSignatureRectangle) {
        Rectangle2D.Double signatureShape = new Rectangle2D.Double();
        signatureShape.setFrame(Double.parseDouble(digitalSignatureRectangle.getLowerLeftX()), Double.parseDouble(digitalSignatureRectangle.getUpperRightY()), Double.parseDouble(digitalSignatureRectangle.getWidth()), Double.parseDouble(digitalSignatureRectangle.getHeight()));
        return signatureShape;
    }

    private static SignatureData getSignatureData(SapphireConnection sapphireConnection, QueryProcessor qp) {
        String sqlSignatureInfo = "SELECT u.sysuserdesc, d.departmentdesc FROM sysuser u LEFT JOIN department d ON u.basedepartment = d.departmentid WHERE u.sysuserid = ?";
        DataSet dsSignatureInfo = qp.getPreparedSqlDataSet(sqlSignatureInfo, (Object[])new String[]{sapphireConnection.getSysuserId()});
        String signatureName = sapphireConnection.getSysuserId();
        String location = "";
        if (!dsSignatureInfo.isEmpty()) {
            location = dsSignatureInfo.getString(0, "departmentdesc", "");
            signatureName = dsSignatureInfo.getString(0, "sysuserdesc", sapphireConnection.getSysuserId());
        }
        SignatureData signatureData = new SignatureData();
        signatureData.setSignatureName(signatureName);
        signatureData.setSignatureLocation(location);
        if (sapphireConnection.getLocale() != null && !sapphireConnection.getLocale().isEmpty()) {
            signatureData.setSignatureLocale(new Locale(sapphireConnection.getLocale().split("_")[0], sapphireConnection.getLocale()));
        }
        return signatureData;
    }

    private static sapphire.attachment.Attachment getSignatureAttachment(SapphireConnection sapphireConnection, QueryProcessor qp) {
        sapphire.attachment.Attachment signatureAttachment = null;
        String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'User' AND keyid1 = ? AND attachmentclass = 'ReportSignature'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{sapphireConnection.getSysuserId()});
        if (ds.getRowCount() > 0) {
            signatureAttachment = sapphire.attachment.Attachment.getAttachment("User", sapphireConnection.getSysuserId(), null, null, ds.getInt(0, "attachmentnum"));
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(sapphireConnection.getConnectionId());
            signatureAttachment = attachmentProcessor.getSDIAttachment(signatureAttachment, Attachment.ThumbnailGeneration.DISABLED);
        }
        return signatureAttachment;
    }

    public static void injectPdfAttachmentIntoPDF(com.aspose.pdf.Document pdfDocument, HashMap<String, com.aspose.pdf.Document> pdfAttachmentCache, String connectionid) throws Exception {
        TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber("::INJECT_PDFATTACHMENT::(.*?)::");
        TextSearchOptions textSearchOptions = new TextSearchOptions(true);
        textFragmentAbsorber.setTextSearchOptions(textSearchOptions);
        pdfDocument.getPages().accept(textFragmentAbsorber);
        TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
        for (TextFragment textFragment : textFragmentCollection) {
            String url = StringUtil.replaceAll(StringUtil.replaceAll(textFragment.getText(), "::INJECT_PDFATTACHMENT::", ""), "::", "");
            String[] parts = StringUtil.split(url, ";");
            AttachmentProcessor arp = new AttachmentProcessor(connectionid);
            String attachmentKey = parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
            com.aspose.pdf.Document nestedPDF = pdfAttachmentCache.get(attachmentKey);
            if (nestedPDF == null) {
                Attachment attachment = arp.getSDIAttachment(parts[0], parts[1], parts[2], parts[3], Integer.parseInt(parts[4]));
                if (attachment != null) {
                    byte[] data = attachment.getData();
                    if (data != null) {
                        PdfFileDetails pdfFileDetails = new PdfFileDetails();
                        pdfFileDetails.setFromPage(DocumentUtil.getInt(parts[5], 1));
                        pdfFileDetails.setToPage(DocumentUtil.getInt(parts[6], 10));
                        pdfFileDetails.setMaxAllowed(10000);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        nestedPDF = FileManager.getPdfDocumentFromBis(bis, pdfFileDetails, null);
                    }
                } else {
                    textFragment.setText("ERROR: Could not find attachment");
                }
            }
            if (nestedPDF == null) continue;
            PageCollection pages = pdfDocument.getPages();
            Page p = textFragment.getPage();
            int pageNumber = p.getNumber();
            pages.delete(pageNumber);
            pages.insert(pageNumber, (Iterable)nestedPDF.getPages());
        }
    }

    public static void addAttachmentIntoPDF(com.aspose.pdf.Document pdfDocument, HashMap<String, String> pdfCaption, String connectionid) {
        TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber("::ADDATTACHMENT::(.*)::");
        TextSearchOptions textSearchOptions = new TextSearchOptions(true);
        textFragmentAbsorber.setTextSearchOptions(textSearchOptions);
        pdfDocument.getPages().accept(textFragmentAbsorber);
        TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
        Iterator iterator = textFragmentCollection.iterator();
        while (iterator.hasNext()) {
            AttachmentProcessor arp = new AttachmentProcessor(connectionid);
            TextFragment textFragment = (TextFragment)iterator.next();
            String url = StringUtil.replaceAll(StringUtil.replaceAll(textFragment.getText(), PDF_ADD_ATTACHMENT_PREFIX, ""), "::", "");
            String[] parts = StringUtil.split(url, ";");
            Attachment attachment = arp.getSDIAttachment(parts[0], parts[1], parts[2], parts[3], Integer.parseInt(parts[4]));
            if (attachment == null) continue;
            String attachmentKey = parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
            String caption = pdfCaption.get(attachmentKey);
            caption = StringUtil.replaceAll(caption, "[filename]", attachment.getSourceFilename());
            byte[] data = attachment.getData();
            if (data == null) continue;
            FileSpecification fileSpecification = new FileSpecification((InputStream)new ByteArrayInputStream(data), attachment.getDescription());
            fileSpecification.setMIMEType(FileType.getFileTypeByFileName(attachment.getFilename(), connectionid).getMime());
            textFragment.setText(caption);
            Page pdfPage = pdfDocument.getPages().get_Item(1);
            FileAttachmentAnnotation fileAttachment = new FileAttachmentAnnotation(pdfPage, new Rectangle(0.0, 0.0, 16.0, 16.0), fileSpecification);
            Rectangle r = textFragment.getRectangle();
            int x = (int)(r.getLLX() + r.getWidth() + 5.0);
            fileAttachment.setRect(new Rectangle((double)x, r.getLLY(), (double)(x + 16), r.getLLY() + 16.0));
            fileAttachment.setIcon(2);
            textFragment.getPage().getAnnotations().add((Annotation)fileAttachment);
        }
    }

    public static void removeFlaggedPages(com.aspose.pdf.Document pdfDocument) {
        TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber("::REMOVE_THIS_PAGE::");
        pdfDocument.getPages().accept(textFragmentAbsorber);
        TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
        PageCollection pages = pdfDocument.getPages();
        for (TextFragment textFragment : textFragmentCollection) {
            int pageNumber = textFragment.getPage().getNumber();
            pages.delete(pageNumber);
        }
    }

    protected static int getInt(String value, int def) {
        int ret;
        try {
            ret = Integer.parseInt(value);
        }
        catch (Exception e) {
            ret = def;
        }
        return ret;
    }

    public static ArrayList getDigitalSignaturePosition(com.aspose.pdf.Document pdfDocument) throws Exception {
        ArrayList imagePositionList = new ArrayList();
        ParagraphAbsorber absorber = new ParagraphAbsorber();
        absorber.visit(pdfDocument);
        for (PageMarkup markup : absorber.getPageMarkups()) {
            int i = 1;
            for (MarkupSection section : markup.getSections()) {
                int j = 1;
                for (MarkupParagraph paragraph : section.getParagraphs()) {
                    for (List line : paragraph.getLines()) {
                        for (TextFragment fragment : line) {
                            if (!fragment.getText().contains("[Confirmation_")) continue;
                            HashMap<String, String> imagePosition = new HashMap<String, String>();
                            imagePosition.put("LowerLeftX", String.valueOf(fragment.getRectangle().getLLX()));
                            imagePosition.put("UpperRightX", String.valueOf(fragment.getRectangle().getLLX() + 120.0));
                            imagePosition.put("LowerLeftY", String.valueOf(fragment.getRectangle().getLLY() - 50.0));
                            imagePosition.put("UpperRightY", String.valueOf(fragment.getRectangle().getURY()));
                            imagePosition.put("Height", "50");
                            imagePosition.put("Width", "120");
                            imagePosition.put("Page", String.valueOf(markup.getNumber()));
                            imagePositionList.add(imagePosition);
                        }
                    }
                    ++j;
                }
                ++i;
            }
        }
        DocumentUtil.replaceTextOnAllPages(pdfDocument, "Confirmation_Pending", "");
        DocumentUtil.replaceTextOnAllPages(pdfDocument, "[", "");
        DocumentUtil.replaceTextOnAllPages(pdfDocument, "]", "");
        return imagePositionList;
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

