/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Document
 */
package com.labvantage.sapphire.modules.eln;

import com.aspose.pdf.Document;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.DigitalSignatureRectangle;
import com.labvantage.sapphire.modules.eln.DocumentUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.services.Attachment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class SignWorksheet
extends BaseAction
implements ELNConstants {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static final String SDCID = "ReportEvent";
    public static final String PROPERTY_ATTACHMENTNUM = "attachmentnum";
    private PropertyList options;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String message = "";
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
        try {
            String worksheetid = properties.getProperty("worksheetid");
            String worksheetversionid = properties.getProperty("worksheetversionid");
            String reporteventid = properties.getProperty("reporteventid");
            Attachment pdfAttachment = attachmentProcessor.getSDIAttachment(SDCID, reporteventid, null, null, 1);
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionProcessor().getSapphireConnection().getConnectionId());
            this.options = SignWorksheet.getOptions(new QueryProcessor(this.getConnectionProcessor().getSapphireConnection().getConnectionId()), worksheetid, worksheetversionid);
            PropertyList policy = configProcessor.getPolicy("ELNPolicy", this.options.getProperty("worksheetpolicynode", "Sapphire Custom"));
            PropertyList format = policy.getPropertyListNotNull("exportformat");
            PropertyList props = format.getPropertyListNotNull("digitalsigning");
            String signingmode = props.getProperty("signingmode");
            String signingprovider = props.getProperty("signingprovider");
            String signingprovidernode = props.getProperty("signingprovidernode", "Sapphire Custom");
            Document pdfDocument = new Document((InputStream)new ByteArrayInputStream(pdfAttachment.getData()));
            ArrayList imagePositionList = DocumentUtil.getDigitalSignaturePosition(pdfDocument);
            if (imagePositionList.isEmpty()) {
                throw new SapphireException("Show Confirmation signature Image secction is set to No");
            }
            ArrayList<DigitalSignatureRectangle> digitalSignatureRectangleList = new ArrayList<DigitalSignatureRectangle>();
            for (Object imagePosition : imagePositionList) {
                DigitalSignatureRectangle digitalSignatureRectangle = new DigitalSignatureRectangle((String)((Map)imagePosition).get("LowerLeftX"), (String)((Map)imagePosition).get("LowerLeftY"), (String)((Map)imagePosition).get("UpperRightX"), (String)((Map)imagePosition).get("UpperRightY"), (String)((Map)imagePosition).get("Width"), (String)((Map)imagePosition).get("Height"), (String)((Map)imagePosition).get("Page"));
                digitalSignatureRectangleList.add(digitalSignatureRectangle);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pdfDocument.save((OutputStream)byteArrayOutputStream);
            this.editAttachment(pdfAttachment, byteArrayOutputStream.toByteArray());
            if (!signingmode.equalsIgnoreCase("OnConfirmation")) {
                throw new SapphireException("Digital Signature not configured or SigningMode is not set as OnConfirmation in ELN Policy");
            }
            DocumentUtil.signReport(signingprovider, signingprovidernode, digitalSignatureRectangleList, this.getConnectionProcessor().getSapphireConnection(), pdfAttachment);
            PropertyList editProps = new PropertyList();
            editProps.setProperty("keyid1", reporteventid);
            editProps.setProperty("sdcid", SDCID);
            editProps.setProperty("digitallysignedflag", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException(e.getMessage());
        }
    }

    public static PropertyList getOptions(QueryProcessor queryProcessor, String worksheetid, String worksheetversionid) {
        PropertyList wsOptions = new PropertyList();
        DataSet worksheet = queryProcessor.getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
        if (worksheet.size() == 1) {
            try {
                wsOptions.setPropertyList(worksheet.getClob(0, "options", ""));
            }
            catch (Exception e) {
                Trace.logError("Failed to load worksheet options for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid), e);
            }
        }
        return wsOptions;
    }

    private void editAttachment(sapphire.attachment.Attachment attachment, byte[] pdfbyte) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", attachment.getSDCId());
        props.setProperty("keyid1", attachment.getKeyId1());
        props.setProperty("keyid2", attachment.getKeyId2());
        props.setProperty("keyid3", attachment.getKeyId3());
        props.setProperty(PROPERTY_ATTACHMENTNUM, Integer.toString(attachment.getAttachmentNum()));
        props.setProperty("filename", attachment.getFilename());
        props.setProperty("attachmentclass", attachment.getAttachmentClass());
        props.setProperty("attachmentclass", attachment.getAttachmentClass());
        props.setProperty("datahash", "");
        new sapphire.accessor.AttachmentProcessor(this.getConnectionId()).editSDIAttachment((HashMap)props, pdfbyte);
    }
}

