/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class LoadSDIData
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadSDIData(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        boolean querywhere = requestData.getProperty("querywhere").length() > 0;
        boolean restrictivewhere = requestData.getProperty("restrictivewhere").length() > 0;
        DataSet primary = this.loadSDIData(requestData.getProperty("sdcid"), this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("queryfrom", "(default)"))), this.evalTokens(requestData, (restrictivewhere && querywhere ? "(" : "") + EncryptDecrypt.unobfsql(requestData.getProperty("querywhere")) + (restrictivewhere ? (querywhere ? ") AND " : "") + "(" + EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere")) + ")" : "")), this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("queryorderby"))), "primary", "", requestData.getProperty("extendedtypes", "false").equals("true"));
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", primary != null ? JSONUtil.toJSONString(primary) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, primary);
        return responseData;
    }
}

