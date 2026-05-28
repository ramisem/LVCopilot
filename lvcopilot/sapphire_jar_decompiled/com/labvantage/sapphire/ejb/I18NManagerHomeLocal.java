/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.I18NManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface I18NManagerHomeLocal
extends EJBLocalHome {
    public I18NManagerLocal create() throws CreateException;
}

