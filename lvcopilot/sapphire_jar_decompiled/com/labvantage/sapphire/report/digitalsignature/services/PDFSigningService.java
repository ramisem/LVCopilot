/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.digitalsignature.services;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.digitalsignature.api.PDFSigner;
import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PDFSigningService
extends BaseService {
    public static final String ID = "PDFSigningService";
    public static final String INVALID_PDF_SIGNER = "INVALID_PDF_SIGNER";
    private PDFSigner signer;
    private PropertyList properties = new PropertyList();

    public PDFSigningService(SapphireConnection sapphireConnection, String signerTree, String signerNode) throws ServiceException {
        super(sapphireConnection);
        this.logName = ID;
        String className = null;
        try {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = ? and propertytreetype = ?", (Object[])new String[]{signerTree, "SigningProvider"}, true);
            if (ds.isEmpty()) {
                throw new ServiceException(INVALID_PDF_SIGNER, String.format("Could not find PDF Signing Provider propertytree %s %s", signerTree, signerNode));
            }
            String valuetree = ds.getClob(0, "valuetree");
            PropertyDefinitionList propertyDefinitionList = null;
            try {
                propertyDefinitionList = new WebAdminProcessor(this.getConnectionid()).getPropertyDefinitionList(signerTree);
            }
            catch (Exception e) {
                this.logger.error("Unable to retrieve PropertyTree definition for PropertyTree: " + signerTree);
            }
            this.properties.setPropertyTree(valuetree, signerNode, propertyDefinitionList);
            LabVantageClassLoader labVantageClassLoader = null;
            String[] excludedJars = new String[]{"sapphire"};
            try {
                labVantageClassLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.APPRESOURCE, signerTree, this.properties.getProperty("appresourceid"), excludedJars, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
            }
            catch (SapphireException e) {
                Trace.logError("Unable to load class loader");
            }
            className = ds.getString(0, "objectname", "");
            this.signer = (PDFSigner)(labVantageClassLoader != null ? labVantageClassLoader.loadClass(className) : Class.forName(className)).newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new ServiceException(INVALID_PDF_SIGNER, String.format("Could not find PDF Signer class %s %s %s", signerTree, signerNode, className));
        }
        catch (InstantiationException e) {
            throw new ServiceException(INVALID_PDF_SIGNER, String.format("Could not instantiate PDF Signer %s %s %s", signerTree, signerNode, className));
        }
        catch (IllegalAccessException e) {
            throw new ServiceException(INVALID_PDF_SIGNER, String.format("Could not access PDF Signer %s %s %s", signerTree, signerNode, className));
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
    }

    public InputStream signDocument(InputStream input, SignatureData signatureData) throws ServiceException {
        try {
            return this.signer.signDocument(this.properties, input, signatureData);
        }
        catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    public void signDocument(Attachment attachment, SignatureData signatureData, Attachment signatureAttachment) throws IOException, SapphireException, ServiceException {
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
        Attachment fullAttachment = attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
        Attachment fullSignatureAttachment = signatureAttachment != null ? attachmentProcessor.getSDIAttachment(signatureAttachment, Attachment.ThumbnailGeneration.DISABLED) : null;
        String attachmentClob = fullAttachment.getClob();
        if (attachmentClob == null || attachmentClob.isEmpty()) {
            InputStream watermarkRemoved = SapphireReport.removeWatermark(fullAttachment.getInputStream());
            try (InputStream sign = fullSignatureAttachment != null ? fullSignatureAttachment.getInputStream() : null;){
                PDFSigningService.addImageToSignature(signatureData, sign);
                InputStream signedInput = this.signDocument(watermarkRemoved, signatureData);
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", fullAttachment.getSDCId());
                props.setProperty("keyid1", fullAttachment.getKeyId1());
                props.setProperty("keyid2", fullAttachment.getKeyId2());
                props.setProperty("keyid3", fullAttachment.getKeyId3());
                props.setProperty("attachmentnum", Integer.toString(fullAttachment.getAttachmentNum()));
                props.setProperty("filename", fullAttachment.getFilename());
                props.setProperty("attachmentclass", fullAttachment.getAttachmentClass());
                props.setProperty("attachmentclass", fullAttachment.getAttachmentClass());
                props.setProperty("datahash", "");
                attachmentProcessor.editSDIAttachment((HashMap)props, signedInput);
                if (attachmentProcessor.hasErrors()) {
                    throw new SapphireException(attachmentProcessor.getLastError());
                }
            }
        }
    }

    private static void addImageToSignature(SignatureData signatureData, InputStream sign) throws IOException {
        if (sign != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = sign.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] targetArray = byteArrayOutputStream.toByteArray();
                signatureData.setSignatureImage(targetArray);
            }
        }
    }
}

