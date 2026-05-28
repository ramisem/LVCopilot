/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUT;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface ResultRecord_OUTService
extends Service {
    public String getResultRecord_OUTPortAddress();

    public ResultRecord_OUT getResultRecord_OUTPort() throws ServiceException;

    public ResultRecord_OUT getResultRecord_OUTPort(URL var1) throws ServiceException;
}

