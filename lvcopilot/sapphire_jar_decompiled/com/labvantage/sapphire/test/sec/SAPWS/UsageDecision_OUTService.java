/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUT;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface UsageDecision_OUTService
extends Service {
    public String getUsageDecision_OUTPortAddress();

    public UsageDecision_OUT getUsageDecision_OUTPort() throws ServiceException;

    public UsageDecision_OUT getUsageDecision_OUTPort(URL var1) throws ServiceException;
}

