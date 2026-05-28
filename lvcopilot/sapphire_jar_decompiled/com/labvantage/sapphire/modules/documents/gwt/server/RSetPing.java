/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.accessor.DAMProcessor;
import sapphire.xml.PropertyList;

public class RSetPing
extends BaseDocumentCommand
implements DocumentCommand {
    public RSetPing(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String rsetid = requestData.getProperty("rsetid");
        DAMProcessor dam = new DAMProcessor(this.sapphireConnection.getConnectionId());
        dam.touchRSet(rsetid);
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", "rsetid");
        return responseData;
    }
}

