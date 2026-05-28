/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import com.labvantage.sapphire.report.digitalsignature.services.PDFSigningService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Locale;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SignPDFReport
extends BaseAction {
    public static final String ID = "SignPDFReport";
    public static final String VERSIONID = "1";
    private static final String SDCID = "ReportEvent";
    public static final String PROPERTY_REPORTEVENTID = "reporteventid";
    public static final String PROPERTY_ATTACHMENTNUM = "attachmentnum";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            DataSet dsSignature;
            String reportEventId = properties.getProperty(PROPERTY_REPORTEVENTID);
            int attachmentNumber = Integer.parseInt(properties.getProperty(PROPERTY_ATTACHMENTNUM));
            String currentUser = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId();
            Attachment attachment = Attachment.getAttachment(SDCID, reportEventId, null, null, attachmentNumber);
            String sqlSignatureInfo = "SELECT u.sysuserdesc, d.departmentdesc FROM sysuser u LEFT JOIN department d ON u.basedepartment = d.departmentid WHERE u.sysuserid = ?";
            DataSet dsSignatureInfo = this.getQueryProcessor().getPreparedSqlDataSet(sqlSignatureInfo, (Object[])new String[]{currentUser});
            String signatureName = currentUser;
            String location = "";
            if (!dsSignatureInfo.isEmpty()) {
                location = dsSignatureInfo.getString(0, "departmentdesc", "");
                signatureName = dsSignatureInfo.getString(0, "sysuserdesc", currentUser);
            }
            if ((dsSignature = this.getQueryProcessor().getPreparedSqlDataSet("SELECT r.signingprovider, r.signingprovidernode, r.signaturexpos, r.signatureypos, r.signaturewidth, r.signatureheight, r.signaturereason, r.signaturepage, re.digitallysignedflag FROM reportevent re INNER JOIN report r ON re.reportid = r.reportid AND re.reportversionid = r.reportversionid WHERE re.reporteventid = ? and re.displaytype = 'pdf'", (Object[])new String[]{reportEventId})).isEmpty()) {
                throw new SapphireException(String.format("Invalid reportevent id provided. %s", reportEventId));
            }
            if (dsSignature.getString(0, "digitallysignedflag", "").equalsIgnoreCase("Y")) {
                throw new SapphireException(String.format("Reportevent %s is already signed.", reportEventId));
            }
            String pdfSigner = dsSignature.getString(0, "signingprovider", this.getConnectionProcessor().getSysConfigProperty("signingprovider"));
            String pdfSignerNode = dsSignature.getString(0, "signingprovidernode", this.getConnectionProcessor().getSysConfigProperty("signingprovidernode"));
            if (pdfSigner.isEmpty() || pdfSignerNode.isEmpty()) {
                throw new SapphireException("PDF signer and node must be specified either in Report SDI or System Config!");
            }
            int x = dsSignature.getInt(0, "signaturexpos", 5);
            int y = dsSignature.getInt(0, "signatureypos", 5);
            int width = dsSignature.getInt(0, "signaturewidth", 125);
            int height = dsSignature.getInt(0, "signatureheight", 75);
            String reason = dsSignature.getString(0, "signaturereason", "Document approved");
            int page = dsSignature.getInt(0, "signaturepage", 1);
            PDFSigningService signer = new PDFSigningService(new SapphireConnection(this.database.getConnection(), this.connectionInfo), pdfSigner, pdfSignerNode);
            SignatureData signatureData = new SignatureData();
            signatureData.setSignatureName(signatureName);
            signatureData.setSignatureLocation(location);
            signatureData.setSignatureReason(reason);
            signatureData.setSignaturePage(page);
            if (this.connectionInfo.getLocale() != null && !this.connectionInfo.getLocale().isEmpty()) {
                signatureData.setSignatureLocale(new Locale(this.connectionInfo.getLocale().split("_")[0], this.connectionInfo.getLocale()));
            }
            Rectangle2D.Float signatureShape = new Rectangle2D.Float();
            signatureShape.setFrame(x, y, width, height);
            signatureData.setSignatureShape(signatureShape);
            Attachment signatureAttachment = this.getSignatureAttachment(currentUser);
            signer.signDocument(attachment, signatureData, signatureAttachment);
        }
        catch (NumberFormatException e) {
            throw new SapphireException("Invalid attachment number provided.", e);
        }
        catch (IOException e) {
            throw new SapphireException("IO Expeption while signing attachment.", e);
        }
        catch (ServiceException e) {
            throw new SapphireException("ServiceException while signing attachment.", e);
        }
    }

    private Attachment getSignatureAttachment(String currentUser) {
        Attachment signatureAttachment = null;
        String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'User' AND keyid1 = ? AND attachmentclass = 'ReportSignature'";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{currentUser});
        if (ds.getRowCount() > 0) {
            signatureAttachment = Attachment.getAttachment("User", currentUser, null, null, ds.getInt(0, PROPERTY_ATTACHMENTNUM));
        }
        return signatureAttachment;
    }
}

