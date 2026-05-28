/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.pdfbox.pdmodel.PDDocument
 *  org.apache.pdfbox.pdmodel.PDPage
 *  org.apache.pdfbox.pdmodel.PDPageContentStream
 *  org.apache.pdfbox.pdmodel.font.PDFont
 *  org.apache.pdfbox.pdmodel.font.PDType1Font
 */
package com.labvantage.sapphire.report.digitalsignature.ajax;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import com.labvantage.sapphire.report.digitalsignature.services.PDFSigningService;
import com.labvantage.sapphire.services.ServiceException;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class GetSignedTestPDF
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        String connectionid = this.getConnectionid();
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionid);
        String signingprovider = ar.getRequestParameter("signingprovider", "");
        int signaturexpos = Integer.parseInt(ar.getRequestParameter("signaturexpos", "200"));
        int signatureypos = Integer.parseInt(ar.getRequestParameter("signatureypos", "200"));
        int signaturewidth = Integer.parseInt(ar.getRequestParameter("signaturewidth", "125"));
        int signatureheight = Integer.parseInt(ar.getRequestParameter("signatureheight", "75"));
        String signaturereason = ar.getRequestParameter("signaturereason", "Document approved");
        int signaturepage = Integer.parseInt(ar.getRequestParameter("signaturepage", "1"));
        if (!signingprovider.isEmpty()) {
            PDFSigningService signer;
            Attachment signatureAttachment = null;
            String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'User' AND keyid1 = ? AND attachmentclass = 'ReportSignature'";
            DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{connectionInfo.getSysuserId()});
            if (ds.getRowCount() > 0) {
                signatureAttachment = Attachment.getAttachment("User", connectionInfo.getSysuserId(), null, null, ds.getInt(0, "attachmentnum"));
                AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionInfo.getConnectionId());
                signatureAttachment = attachmentProcessor.getSDIAttachment(signatureAttachment, Attachment.ThumbnailGeneration.DISABLED);
            }
            String sqlSignatureInfo = "SELECT u.sysuserdesc, d.departmentdesc FROM sysuser u LEFT JOIN department d ON u.basedepartment = d.departmentid WHERE u.sysuserid = ?";
            DataSet dsSignatureInfo = qp.getPreparedSqlDataSet(sqlSignatureInfo, (Object[])new String[]{connectionInfo.getSysuserId()});
            String signatureName = connectionInfo.getSysuserId();
            String location = "";
            if (!dsSignatureInfo.isEmpty()) {
                location = dsSignatureInfo.getString(0, "departmentdesc", "");
                signatureName = dsSignatureInfo.getString(0, "sysuserdesc", connectionInfo.getSysuserId());
            }
            try {
                signer = new PDFSigningService(cp.getSapphireConnection(), signingprovider.split("_")[0], signingprovider.split("_")[1]);
            }
            catch (ServiceException e) {
                throw new ServletException("Failed to set pdf signing service: " + e.getMessage());
            }
            SignatureData signatureData = new SignatureData();
            signatureData.setSignatureName(signatureName);
            signatureData.setSignatureLocation(location);
            signatureData.setSignatureReason(signaturereason);
            signatureData.setSignaturePage(signaturepage);
            if (connectionInfo.getLocale() != null && !connectionInfo.getLocale().isEmpty()) {
                signatureData.setSignatureLocale(new Locale(connectionInfo.getLocale().split("_")[0], connectionInfo.getLocale()));
            }
            Rectangle2D.Float signatureShape = new Rectangle2D.Float();
            signatureShape.setFrame(signaturexpos, signatureypos, signaturewidth, signatureheight);
            signatureData.setSignatureShape(signatureShape);
            try (ByteArrayInputStream input = new ByteArrayInputStream(this.getTestPDF());
                 InputStream signatureImage = signatureAttachment != null ? signatureAttachment.getInputStream() : null;){
                InputStream signedDocument;
                if (signatureImage != null) {
                    byte[] targetArray = new byte[signatureImage.available()];
                    signatureImage.read(targetArray);
                    signatureData.setSignatureImage(targetArray);
                }
                if ((signedDocument = signer.signDocument(input, signatureData)) != null) {
                    byte[] signedArray = new byte[signedDocument.available()];
                    signedDocument.read(signedArray);
                    ar.addCallbackArgument("data", Base64.getEncoder().encodeToString(signedArray));
                }
            }
            catch (ServiceException | IOException e) {
                ar.setError("Failed to sign PDF document: " + e.getMessage());
            }
        }
        ar.print();
    }

    private byte[] getTestPDF() throws IOException {
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        PDPage pdfPage = doc.getPage(0);
        try (PDPageContentStream contentStream = new PDPageContentStream(doc, pdfPage);){
            contentStream.beginText();
            contentStream.setFont((PDFont)PDType1Font.HELVETICA_BOLD, 12.0f);
            contentStream.newLineAtOffset(25.0f, 680.0f);
            contentStream.showText("This is a document for testing PDF digital signing");
            contentStream.endText();
        }
        pdfPage = doc.getPage(1);
        contentStream = new PDPageContentStream(doc, pdfPage);
        var4_4 = null;
        try {
            contentStream.beginText();
            contentStream.setFont((PDFont)PDType1Font.HELVETICA_BOLD, 12.0f);
            contentStream.newLineAtOffset(25.0f, 680.0f);
            contentStream.showText("This is the second page of the document for testing PDF digital signing");
            contentStream.endText();
        }
        catch (Throwable throwable) {
            var4_4 = throwable;
            throw throwable;
        }
        finally {
            if (contentStream != null) {
                if (var4_4 != null) {
                    try {
                        contentStream.close();
                    }
                    catch (Throwable throwable) {
                        var4_4.addSuppressed(throwable);
                    }
                } else {
                    contentStream.close();
                }
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save((OutputStream)baos);
        doc.close();
        return baos.toByteArray();
    }
}

