/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.RemoteAccessManager;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface RemoteAccessManagerHome
extends EJBHome {
    public RemoteAccessManager create() throws RemoteException, CreateException;
}

