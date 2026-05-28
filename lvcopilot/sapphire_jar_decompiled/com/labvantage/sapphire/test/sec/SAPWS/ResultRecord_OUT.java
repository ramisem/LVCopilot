/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.Result;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResultRecord_OUT
extends Remote {
    public Result resultRecord_OUT(ResultRecord var1) throws RemoteException;
}

