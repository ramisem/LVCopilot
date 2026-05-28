/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.jws.WebMethod
 *  javax.jws.WebService
 *  javax.xml.soap.SOAPException
 */
package com.labvantage.sapphire.webservices;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

@WebService
public interface SapphireBasicWSEndpoint {
    @WebMethod
    public String getVersion();

    @WebMethod
    public String getConnectionId(String var1, String var2, String var3);

    @WebMethod
    public void clearConnection(String var1);

    @WebMethod
    public String processAction(String var1, String var2, String var3, String var4);

    @WebMethod
    public String getSqlDataSet(String var1, String var2);

    @WebMethod
    public boolean checkConnection(String var1);

    @WebMethod
    public String getPublicKey() throws SOAPException;

    @WebMethod
    public int getSequence(String var1, String var2, String var3, int var4, int var5);

    @WebMethod
    public String processMessage(String var1, String var2, String var3, String var4, String var5) throws SOAPException;
}

