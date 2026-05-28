/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.ServiceException
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUT;
import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

public interface PRDeleteLineItem_OUTService
extends Service {
    public String getPRDeleteLineItem_OUTPortAddress();

    public PRDeleteLineItem_OUT getPRDeleteLineItem_OUTPort() throws ServiceException;

    public PRDeleteLineItem_OUT getPRDeleteLineItem_OUTPort(URL var1) throws ServiceException;
}

