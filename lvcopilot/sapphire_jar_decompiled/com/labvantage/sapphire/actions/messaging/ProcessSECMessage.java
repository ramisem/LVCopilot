/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.messaging.SECMessageHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ProcessSECMessage
extends BaseAction
implements sapphire.action.ProcessSECMessage {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringBuffer log = new StringBuffer();
        String message = properties.getProperty("message", "");
        SECMessageHandler handler = new SECMessageHandler();
        handler.setConnectionid(this.getConnectionId());
        handler.setXMLString(message);
        try {
            SaxUtil.parseString(handler, properties.getProperty("encoding", "UTF-8"));
        }
        catch (ActionException e) {
            String error = handler.getError();
            throw new SapphireException(error);
        }
        catch (SapphireException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        String responseMessage = handler.getResponseMessage();
        log.append(handler.getLog());
        properties.setProperty("log", log.toString());
        if (responseMessage != null && responseMessage.length() > 0) {
            properties.setProperty("responsemessage", responseMessage);
        }
    }
}

