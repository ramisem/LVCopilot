/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.HashMap;
import sapphire.SapphireException;

public class GroovyProcessingPropertyHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        StringBuffer log = (StringBuffer)props.get("log");
        try {
            ProcessingUtil.processScript(this.sapphireConnection, (String)props.get("script"), props, log, "DOCUMENTPROCESSING");
        }
        catch (Exception e) {
            log.append(e.getMessage());
            throw new SapphireException(e.getMessage());
        }
    }
}

