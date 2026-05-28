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
import com.labvantage.sapphire.ejb.DDTManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.DDTService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DDTManagerBean
extends BaseManager
implements SessionBean,
DDTManagement {
    public DDTManagerBean() {
        this.logName = "DDTManager";
    }

    @Override
    public PropertyList getSDCProperties(String connectionid, String sdcid) throws ManagerException {
        String methodName = "getSDCProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid);
            this.startMethod(methodName, true, connectionid);
            DDTService ddtService = new DDTService(this.sapphireConnection);
            PropertyList propertyList = ddtService.getSDCProperties(sdcid);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get sdc properties for sdcid '" + sdcid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getReverseLinksData(String connectionid, String sdcid) throws ManagerException {
        String methodName = "getReverseLinksData";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid);
            this.startMethod(methodName, true, connectionid);
            DDTService ddtService = new DDTService(this.sapphireConnection);
            DataSet dataSet = ddtService.getReverseLinksData(sdcid);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get reverse link data for sdcid '" + sdcid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyListCollection getTableColumns(String connectionid, String tableid) throws ManagerException {
        String methodName = "getTableColumns";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, tableid);
            this.startMethod(methodName, true, connectionid);
            DDTService ddtService = new DDTService(this.sapphireConnection);
            PropertyListCollection propertyListCollection = ddtService.getTableColumns(tableid);
            return propertyListCollection;
        }
        catch (Exception e) {
            this.logError("Failed to get columns for tableid '" + tableid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

