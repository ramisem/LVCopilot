/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PRUpdate_OUT
extends Remote {
    public Result PRUpdate_OUT(PRUpdate var1) throws RemoteException;
}

