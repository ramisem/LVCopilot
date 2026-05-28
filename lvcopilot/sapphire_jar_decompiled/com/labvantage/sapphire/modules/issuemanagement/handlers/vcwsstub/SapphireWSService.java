/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub;

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SapphireWS_PortType;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface SapphireWSService
extends Service {
    public String getSapphireWSAddress();

    public SapphireWS_PortType getSapphireWS() throws ServiceException;

    public SapphireWS_PortType getSapphireWS(URL var1) throws ServiceException;
}

