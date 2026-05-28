/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.xml.PropertyList;

public class LoadValues
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadValues(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        PropertyList fieldValues = new PropertyList();
        fieldValues.setProperty("fieldid", requestData.getProperty("fieldid"));
        fieldValues.setProperty("fieldinstance", requestData.getProperty("fieldinstance"));
        fieldValues.setProperty("values", Form.defineValues(this.sapphireConnection, requestData.getProperty("sdcid"), requestData.getProperty("sql"), requestData.getProperty("reftypeid"), requestData.getProperty("values"), requestData.getProperty("valuesqueryfrom"), requestData.getProperty("valuesquerywhere")));
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", fieldValues.toJSONString(false));
        this.debugReturn(requestData, fieldValues);
        return responseData;
    }
}

