/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.xml.soap.MimeHeaders
 *  javax.xml.soap.SOAPBody
 *  javax.xml.soap.SOAPException
 *  org.apache.axis.AxisFault
 *  org.apache.axis.Message
 *  org.apache.axis.MessageContext
 *  org.apache.axis.components.logger.LogFactory
 *  org.apache.axis.handlers.BasicHandler
 *  org.apache.axis.message.RPCParam
 *  org.apache.commons.logging.Log
 *  org.apache.xml.security.Init
 */
package com.labvantage.sapphire.webservices;

import com.labvantage.sapphire.services.SecurityService;
import javax.servlet.ServletRequest;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.RPCParam;
import org.apache.commons.logging.Log;
import org.apache.xml.security.Init;
import org.w3c.dom.NodeList;
import sapphire.xml.PropertyList;

public class AxisConnectionHandler
extends BasicHandler {
    static Log log = LogFactory.getLog((String)AxisConnectionHandler.class.getName());

    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            ServletRequest request = (ServletRequest)msgContext.getProperty("transport.http.servletRequest");
            String host = request.getRemoteHost();
            String ip = request.getRemoteAddr();
            MimeHeaders mimeHeaders = msgContext.getRequestMessage().getMimeHeaders();
            String[] userAgent = mimeHeaders.getHeader("user-agent");
            String userAgentStr = "";
            if (userAgent != null && userAgent.length > 0) {
                userAgentStr = userAgent[0];
            }
            this.updateConnectionLog(msgContext.getResponseMessage(), userAgentStr, host, ip);
        }
        catch (Exception e) {
            throw AxisFault.makeFault((Exception)e);
        }
    }

    public void updateConnectionLog(Message msg, String useragent, String remotehostid, String remoteip) throws SOAPException {
        RPCParam returnNodeItem;
        String connectionid;
        if (msg == null) {
            return;
        }
        SOAPBody soapBody = msg.getSOAPBody();
        NodeList returnNode = soapBody.getElementsByTagName("getConnectionIdReturn");
        if (returnNode != null && returnNode.getLength() > 0 && (connectionid = (returnNodeItem = (RPCParam)returnNode.item(0)).getValue()) != null && connectionid.length() > 0) {
            PropertyList connectionLogProps = new PropertyList();
            connectionLogProps.setProperty("clientuseragent", useragent);
            connectionLogProps.setProperty("clientbrowser", useragent);
            connectionLogProps.setProperty("clienthostname", remotehostid);
            connectionLogProps.setProperty("clientipaddress", remoteip);
            connectionLogProps.setProperty("connectiontypeflag", "W");
            SecurityService.updateConnectionLog(connectionid, connectionLogProps);
        }
    }

    static {
        Init.init();
    }
}

