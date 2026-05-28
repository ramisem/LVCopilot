/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.SequenceManagement;
import com.labvantage.sapphire.services.SequenceService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

public class SequenceManagerBean
extends BaseManager
implements SessionBean,
SequenceManagement {
    public SequenceManagerBean() {
        this.logName = "SequenceManager";
    }

    @Override
    public int getSequence(String connectionid, String sdcid, String sequenceid, int start, int incrementBy) throws ManagerException {
        String methodName = "getSequence";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + sequenceid + ";" + start + ";" + incrementBy);
            this.startMethod(methodName, incrementBy == 0, connectionid);
            SequenceService sequenceService = new SequenceService(this.sapphireConnection);
            int n = sequenceService.getSequence(sdcid, sequenceid, start, incrementBy);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to get sequence", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getUUID(String connectionid) {
        String methodName = "getUUID";
        try {
            this.startMethod(methodName, connectionid);
            SequenceService sequenceService = new SequenceService(this.sapphireConnection);
            String string = sequenceService.getUUID();
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get uuid", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

