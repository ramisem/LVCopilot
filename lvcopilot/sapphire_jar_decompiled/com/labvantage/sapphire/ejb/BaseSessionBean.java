/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.CreateException
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 *  javax.ejb.SessionContext
 */
package com.labvantage.sapphire.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public abstract class BaseSessionBean
implements SessionBean {
    SessionContext sessionContext;

    public void setSessionContext(SessionContext sessionContext) throws EJBException {
        this.sessionContext = sessionContext;
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbRemove() throws EJBException {
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }
}

