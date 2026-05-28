/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.TDQManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface TDQManagerHomeLocal
extends EJBLocalHome {
    public TDQManagerLocal create() throws CreateException;
}

