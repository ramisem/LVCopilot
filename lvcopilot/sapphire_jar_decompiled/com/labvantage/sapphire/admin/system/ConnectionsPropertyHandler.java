/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.modules.eventmanager.Notify;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConnectionsPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String connectionidlist;
        String mode = (String)props.get("dothis");
        String string = connectionidlist = props.get("connectionidlist") != null ? (String)props.get("connectionidlist") : "";
        if ("delete".equalsIgnoreCase(mode)) {
            try {
                SecurityService securityService = new SecurityService(this.sapphireConnection);
                String[] connections = StringUtil.split(connectionidlist, "|!!!|");
                for (int i = 0; i < connections.length; ++i) {
                    String connectionid = connections[i];
                    if (connectionid == null) continue;
                    securityService.clearConnection(connectionid, false);
                }
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to " + mode + " connectionid list '" + connectionidlist + "'", e);
            }
        } else if ("alert".equalsIgnoreCase(mode)) {
            HashSet<String> subscribers = new HashSet<String>();
            String[] connections = StringUtil.split(connectionidlist, "|!!!|");
            for (int i = 0; i < connections.length; ++i) {
                ConnectionProcessor cp = new ConnectionProcessor(connections[i]);
                SapphireConnection userConnection = cp.getSapphireConnection();
                if (userConnection == null) continue;
                subscribers.add(userConnection.getConnectionId());
            }
            ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
            PropertyList alertProps = new PropertyList();
            alertProps.setProperty("type", "A");
            alertProps.setProperty("message", (String)props.get("alertmessage"));
            for (String subscriber : subscribers) {
                alertProps.setProperty("subscriberid", subscriber);
                actionProcessor.processActionClass(Notify.class.getName(), alertProps);
            }
        }
    }
}

