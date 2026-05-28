/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SendResponseToFile
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String path = propertyList.getProperty("outputpath", "c:\\SEC\\OUT");
        String messageType = propertyList.getProperty("messagetypeid");
        String message = propertyList.getProperty("responsemessage");
        if (message.length() == 0) {
            throw new ActionException("PropertyList does not have responsemessage: ");
        }
        if (!(path = path.replace('\\', '/')).endsWith("/")) {
            path = path + "/";
        }
        String filename = messageType + new Date().getTime() + ".xml";
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(path + filename), "UTF8"));
            out.write(message);
            ((Writer)out).flush();
            ((Writer)out).close();
        }
        catch (IOException e) {
            throw new ActionException("Failed to create message file: " + e.getMessage());
        }
    }
}

