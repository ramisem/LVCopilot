/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.xml.PropertyList;

public class LoadThumbnail
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadThumbnail(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        HashMap<String, String> responseData = new HashMap<String, String>();
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireConnection);
            if (requestData.getProperty("documentid").length() > 0) {
                dbu.createPreparedResultSet("SELECT thumbnailhtml FROM document WHERE documentid = ? AND documentversionid = ?", new Object[]{requestData.getProperty("documentid"), requestData.getProperty("documentversionid")});
            } else {
                dbu.createPreparedResultSet("SELECT thumbnailhtml FROM form WHERE formid = ? AND formversionid = ?", new Object[]{requestData.getProperty("formid"), requestData.getProperty("formversionid")});
            }
            if (dbu.getNext()) {
                String thumbnailhtml = dbu.getClob("thumbnailhtml");
                responseData.put("jsonreturn", thumbnailhtml != null && thumbnailhtml.length() > 0 ? thumbnailhtml : Document.getDocumentThumbnail());
            }
        }
        catch (Exception e) {
            responseData.put("jsonreturn", Document.getDocumentThumbnail());
        }
        return responseData;
    }
}

