/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.externalapp;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import sapphire.SapphireException;
import sapphire.servlet.BaseExternalHandler;
import sapphire.xml.PropertyList;

public class JunitTestCommandHandler
extends BaseExternalHandler
implements SDMSConstants {
    public static final String COMMAND_CREATESAMPLES = "COMMAND_CREATESAMPLES";

    @Override
    public PropertyList processCommand(String command, PropertyList commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        if (command.equals(COMMAND_CREATESAMPLES)) {
            String copies = commandRequest.getProperty("copies");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("copies", copies);
            this.getActionProcessor().processAction("AddSDI", "1", props);
            commandResponse.setProperty("newkeyid1", props.getProperty("newkeyid1"));
        }
        return commandResponse;
    }
}

