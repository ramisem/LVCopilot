/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBLocalHome
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ConfigurationManagerLocal;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ConfigurationManagerHomeLocal
extends EJBLocalHome {
    public ConfigurationManagerLocal create() throws CreateException;
}

