/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.documents.BaseAddDocumentAction;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddDocument
extends BaseAddDocumentAction
implements sapphire.action.AddDocument,
DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String formid = properties.getProperty("formid");
        String formversionid = properties.getProperty("formversionid");
        String documentid = properties.getProperty("documentid");
        String documentversionid = properties.getProperty("documentversionid");
        if (formid.length() == 0 && documentid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Formid not specified!");
        }
        try {
            this.logger.info("Adding new document in draft mode");
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            Form form = null;
            Document document = null;
            PropertyList inputFieldValues = null;
            if (formid.length() > 0) {
                form = Form.getInstance(sapphireConnection, formid, formversionid);
                inputFieldValues = this.getInputFieldValues(form, properties);
            } else {
                document = Document.getInstance(sapphireConnection, documentid, documentversionid, "", false, false);
                form = document.getForm();
                properties.setProperty("overlayinputvalues", properties.getProperty("copyfieldvalues", "Y"));
                PropertyListCollection fieldValues = document.getFieldValues();
                inputFieldValues = new PropertyList();
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    inputFieldValues.setProperty(fieldValue.getProperty("fieldid"), fieldValue.getCollection("instances"));
                }
            }
            this.addDocument(sapphireConnection, formid, formversionid, properties, null, inputFieldValues);
        }
        catch (Exception e) {
            throw new SapphireException("Error creating new document. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

