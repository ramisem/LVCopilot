/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SaveReviewItems
extends BaseDocumentCommand
implements DocumentCommand {
    public SaveReviewItems(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String documentid = requestData.getProperty("documentid");
        String documentversionid = requestData.getProperty("documentversionid", "1");
        String reviewitemobjectid = requestData.getProperty("reviewitemobjectid");
        String reviewiteminstance = requestData.getProperty("reviewiteminstance");
        PropertyListCollection reviewItems = requestData.getCollection("reviewitems");
        HashMap<String, String> responseData = new HashMap<String, String>();
        try {
            if (documentid.length() <= 0 && reviewitemobjectid.length() <= 0 && reviewItems == null) {
                throw new SapphireException("Document id or reviewitemobjectid not specified or reviewitems null");
            }
            Document document = Document.getInstance(this.sapphireConnection, documentid, documentversionid, this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere"))), false, this.debug);
            boolean saved = document.saveReviewItems(reviewitemobjectid, reviewiteminstance, reviewItems);
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("saved", saved ? "Y" : "N");
            documentObjects.setProperty("reviewitemobjectid", reviewitemobjectid);
            documentObjects.setProperty("reviewitems", reviewItems);
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        catch (Exception e) {
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", "E");
            documentObjects.setProperty("returnmessage", "Failed to process reviewitem save - saveReviewItems. Exception: " + e.getMessage());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        return responseData;
    }
}

