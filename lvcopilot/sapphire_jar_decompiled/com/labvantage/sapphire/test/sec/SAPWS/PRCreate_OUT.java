/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRCreate;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PRCreate_OUT
extends Remote {
    public Result PRCreate_OUT(PRCreate var1) throws RemoteException;
}

