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

public class LoadRefTypeValues
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadRefTypeValues(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        DataSet refvalues = qp.getRefTypeDataSet(requestData.getProperty("reftypeid"));
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", refvalues != null ? JSONUtil.toJSONString(refvalues) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, refvalues);
        return responseData;
    }
}

