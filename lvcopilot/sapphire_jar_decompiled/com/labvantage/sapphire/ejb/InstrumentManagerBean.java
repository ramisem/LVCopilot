/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.InstrumentManagement;
import com.labvantage.sapphire.services.InstrumentService;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.xml.PropertyList;

public class InstrumentManagerBean
extends BaseManager
implements SessionBean,
InstrumentManagement {
    public InstrumentManagerBean() {
        this.logName = "InstrumentManager";
    }

    @Override
    public HashMap executeCommand(String connectionid, PropertyList commandProps) {
        String methodName = "executeCommand";
        try {
            this.startMethod(methodName, connectionid);
            InstrumentService instrService = new InstrumentService(this.sapphireConnection);
            HashMap hashMap = instrService.executeCommand(commandProps);
            return hashMap;
        }
        catch (Exception e) {
            this.logError("Failed to process executeCommand", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

