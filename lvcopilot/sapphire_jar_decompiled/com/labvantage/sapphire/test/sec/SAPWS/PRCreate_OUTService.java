/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUT;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface PRCreate_OUTService
extends Service {
    public String getPRCreate_OUTPortAddress();

    public PRCreate_OUT getPRCreate_OUTPort() throws ServiceException;

    public PRCreate_OUT getPRCreate_OUTPort(URL var1) throws ServiceException;
}

