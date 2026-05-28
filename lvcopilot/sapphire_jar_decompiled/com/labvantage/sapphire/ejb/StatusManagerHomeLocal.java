/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.StatusManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface StatusManagerHomeLocal
extends EJBLocalHome {
    public StatusManagerLocal create() throws CreateException;
}

