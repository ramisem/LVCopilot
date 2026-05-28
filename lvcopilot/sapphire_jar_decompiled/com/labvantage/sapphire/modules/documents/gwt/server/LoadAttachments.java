/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class LoadAttachments
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadAttachments(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        DataSet attachments = qp.getPreparedSqlDataSet("SELECT * FROM sdiattachment WHERE sdcid = 'LV_Document' AND keyid1 = ? AND keyid2 = ?", new Object[]{requestData.getProperty("documentid"), requestData.getProperty("documentversionid")});
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", attachments != null ? JSONUtil.toJSONString(attachments) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, attachments);
        return responseData;
    }
}

