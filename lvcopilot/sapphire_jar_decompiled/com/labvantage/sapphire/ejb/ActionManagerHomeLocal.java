/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ActionManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ActionManagerHomeLocal
extends EJBLocalHome {
    public ActionManagerLocal create() throws CreateException;
}

