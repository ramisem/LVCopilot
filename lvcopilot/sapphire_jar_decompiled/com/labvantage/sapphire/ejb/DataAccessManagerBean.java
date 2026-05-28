/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.DataAccessManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.DataAccessService;
import java.util.Arrays;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.SDIList;

public class DataAccessManagerBean
extends BaseManager
implements SessionBean,
DataAccessManagement {
    public DataAccessManagerBean() {
        this.logName = "DataAccessManager";
    }

    @Override
    public void clearRSet(String connectionid, RSet rset) throws ManagerException {
        String methodName = "clearRSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, rset.toString());
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            das.clearRSet(rset);
        }
        catch (Exception e) {
            this.logError("Failed to clear rset '" + rset + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void clearRSets(String connectionid, String rsetlist) throws ManagerException {
        String methodName = "clearRSets";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, rsetlist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            das.clearRSets(rsetlist);
        }
        catch (Exception e) {
            this.logError("Failed to clear rset list '" + rsetlist + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void touchRSet(String connectionid, RSet rset) throws ManagerException {
        String methodName = "touchRSet";
        try {
            this.startMethod(methodName, connectionid, false);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            das.touchRSet(rset);
        }
        catch (Exception e) {
            this.logError("Failed to touch rset '" + rset + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public RSet createRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list) throws ManagerException {
        String methodName = "createRSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSet(sdcid, keyid1list, keyid2list, keyid3list);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, int bypassSecurityCode) throws ManagerException {
        String methodName = "createRSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + viewHiddenRecord + ";" + bypassSecurityCode);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSet(sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, bypassSecurityCode);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createRSetQ(String connectionid, String sdcid, String queryid, String[] params) throws ManagerException {
        String methodName = "createRSetQ";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + queryid + ";" + Arrays.toString(params));
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSetQ(sdcid, queryid, params, "", "", "", "", 0, false, false);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset for sdcid '" + sdcid + "', queryid '" + queryid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createLockedRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption) throws ManagerException {
        String methodName = "createLockedRSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + lockoption);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createLockedRSet(sdcid, keyid1list, keyid2list, keyid3list, lockoption);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create locked rset for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createRSetWI(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateBoth) throws ManagerException {
        String methodName = "createRSetWI";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + workitemidlist + ";" + workiteminstancelist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSetWI(sdcid, keyid1list, keyid2list, keyid3list, workitemidlist, workiteminstancelist, populateBoth);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset WI for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createRSetDS(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateBoth, boolean calcexpand) throws ManagerException {
        String methodName = "createRSetDS";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + paramlistidlist + ";" + paramlistversionidlist + ";" + variantidlist + ";" + datasetlist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset DS for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createRSetDSNP(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateBoth, boolean calcexpand) throws ManagerException {
        String methodName = "createRSetDSNP";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + paramlistidlist + ";" + paramlistversionidlist + ";" + variantidlist + ";" + datasetlist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createRSetDSNP(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand, false);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset DSNP for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createLockedRSetDS(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ManagerException {
        String methodName = "createLockedRSetDS";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + paramlistidlist + ";" + paramlistversionidlist + ";" + variantidlist + ";" + datasetlist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createLockedRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, lockoption, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create locked rset DS for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet createLockedRSetDSNP(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ManagerException {
        String methodName = "createLockedRSetDSNP";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + paramlistidlist + ";" + paramlistversionidlist + ";" + variantidlist + ";" + datasetlist);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.createLockedRSetDSNP(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, lockoption, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create locked rset DS for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public RSet lockRSet(String connectionid, RSet rset, String lockoption, int lockscope) throws ManagerException {
        String methodName = "lockRSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, rset.toString() + ";" + lockoption + ";" + lockscope);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rSet = das.lockRSet(rset, lockoption, lockscope);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to lock rset " + rset, e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public SDIList checkSDIAccess(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, String operation) throws ManagerException {
        String methodName = "checkSDIAccess";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list + ";" + viewHiddenRecord + ";" + operation);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            SDI sdi = new SDI(sdcid, keyid1list, keyid2list, keyid3list);
            das.checkSDIAccess(sdi, viewHiddenRecord, operation);
            SDIList sdiList = new SDIList();
            sdiList.setSdcid(sdcid);
            sdiList.addSDIList(sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3());
            SDIList sDIList = sdiList;
            return sDIList;
        }
        catch (Exception e) {
            this.logError("Failed to check SDI access for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean setGlobalLock(String connectionid, boolean lock) throws ManagerException {
        String methodName = "setGlobalLock";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, String.valueOf(lock));
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            boolean bl = das.setGlobalLock(lock);
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to set global lock", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean isGlobalLock(String connectionid) throws ManagerException {
        String methodName = "isGlobalLock";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            boolean bl = das.isGlobalLock();
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to get global lock", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean checkRESTAccess(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String restrictivewhere, String operation) throws ManagerException {
        String methodName = "restCheck";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + keyid1list + ";" + keyid2list + ";" + keyid3list);
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            boolean bl = das.checkRESTAccess(sdcid, keyid1list, keyid2list, keyid3list, restrictivewhere, operation);
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to check REST for " + new SDI(sdcid, keyid1list, keyid2list, keyid3list).getKeyText(), e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

