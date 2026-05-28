/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.DataLockManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface DataLockManagerHomeLocal
extends EJBLocalHome {
    public DataLockManagerLocal create() throws CreateException;
}

