/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.AutomationManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface AutomationManagerHomeLocal
extends EJBLocalHome {
    public AutomationManagerLocal create() throws CreateException;
}

