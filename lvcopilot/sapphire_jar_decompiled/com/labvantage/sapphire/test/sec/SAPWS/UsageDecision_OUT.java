/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.Result;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UsageDecision_OUT
extends Remote {
    public Result usageDecision_OUT(UsageDecision var1) throws RemoteException;
}

