/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUT;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface PRUpdate_OUTService
extends Service {
    public String getPRUpdate_OUTPortAddress();

    public PRUpdate_OUT getPRUpdate_OUTPort() throws ServiceException;

    public PRUpdate_OUT getPRUpdate_OUTPort(URL var1) throws ServiceException;
}

