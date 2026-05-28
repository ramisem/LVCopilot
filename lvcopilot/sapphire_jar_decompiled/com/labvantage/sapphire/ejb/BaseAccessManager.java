/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.RemoteAccessManagement;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBException;
import sapphire.attachment.Attachment;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseAccessManager
extends BaseManager {
    public ActionBlock processActionBlock(String connectionid, ActionBlock actionBlock, boolean newTrans, boolean processAsync) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ActionBlock ab = ServiceLocator.getInstance().getActionManager().processActionBlock(connectionid, actionBlock, newTrans, processAsync);
            if (!newTrans && actionBlock.getErrorHandler() != null && actionBlock.getErrorHandler().hasErrors()) {
                this.sessionContext.setRollbackOnly();
            }
            ActionBlock actionBlock2 = ab;
            return actionBlock2;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Attachment getSDIAttachment(String connectionid, Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Attachment attachment2 = ServiceLocator.getInstance().getAttachmentManager().getSDIAttachment(connectionid, attachment, thumbnailGeneration);
            return attachment2;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Attachment getSDIAttachment(String connectionid, Attachment attachment, int auditlog, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Attachment attachment2 = ServiceLocator.getInstance().getAttachmentManager().getSDIAttachment(connectionid, attachment, auditlog, thumbnailGeneration);
            return attachment2;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Attachment getTempAttachment(String connectionid, Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Attachment attachment2 = ServiceLocator.getInstance().getAttachmentManager().getTempAttachment(connectionid, attachment, thumbnailGeneration);
            return attachment2;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public HashMap addSDIAttachment(String connectionid, HashMap columns, byte[] data) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            HashMap hashMap = ServiceLocator.getInstance().getAttachmentManager().addSDIAttachment(connectionid, columns, data);
            return hashMap;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Attachment addSDIAttachment(String connectionid, Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Attachment attachment2 = ServiceLocator.getInstance().getAttachmentManager().addSDIAttachment(connectionid, attachment, applyLock, index, attachmentPolicyNode);
            return attachment2;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public HashMap editSDIAttachment(String connectionid, HashMap columns, byte[] data) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            HashMap hashMap = ServiceLocator.getInstance().getAttachmentManager().editSDIAttachment(connectionid, columns, data);
            return hashMap;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Attachment editSDIAttachment(String connectionid, Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Attachment attachment2 = ServiceLocator.getInstance().getAttachmentManager().editSDIAttachment(connectionid, attachment, applyLock, index, attachmentPolicyNode);
            return attachment2;
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void deleteSDIAttachment(String connectionid, Attachment attachment, boolean applyLock, String attachmentPolicyNode) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getAttachmentManager().deleteSDIAttachment(connectionid, attachment, applyLock, attachmentPolicyNode);
        }
        catch (Exception e) {
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getConnectionId(SapphireConnection sapphireConnection) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(sapphireConnection.getConnectionId(), "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getSecurityManager().getConnectionId(sapphireConnection);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void clearConnection(String connectionid, String clearConnectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().clearConnection(connectionid, clearConnectionid);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public SapphireConnection getSapphireConnection(String connectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            SapphireConnection sapphireConnection = ServiceLocator.getInstance().getSecurityManager().getSapphireConnection(connectionid);
            return sapphireConnection;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void prepareToDeleteConnection(String connectionid, String deleteConnectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().prepareToDeleteConnection(connectionid, deleteConnectionid);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void changePassword(String databaseid, String sysuserid, String oldpassword, String newpassword) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByDatabaseid(databaseid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().changePassword(databaseid, sysuserid, oldpassword, newpassword);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean isValidPassword(String databaseid, String sysuserid, String password) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByDatabaseid(databaseid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getSecurityManager().isValidPassword(databaseid, sysuserid, password);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean checkUser(String connectionid, String sysuserid, String password) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getSecurityManager().checkUser(connectionid, sysuserid, password);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean checkConnection(String connectionid, boolean checkCacheFirst) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getSecurityManager().checkConnection(connectionid, checkCacheFirst);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void clearRSets(String connectionid, String rsetlist) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getDataAccessManager().clearRSets(connectionid, rsetlist);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getLicenseProperty(String connectionid, String propertyid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getConfigurationManager().getLicenseProperty(connectionid, propertyid);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void disableUser(String connectionid, String sysuserid, String disableReason) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().disableUser(connectionid, sysuserid, disableReason);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void enableUser(String connectionid, String sysuserid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().enableUser(connectionid, sysuserid);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void forcePasswordChange(String connectionid, String sysuserid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSecurityManager().forcePasswordChange(connectionid, sysuserid);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public List<SapphireDatabase> getSapphireDatabases() throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByDatabaseid("", "JavaAPI");
        }
        try {
            List list = ServiceLocator.getInstance().getConfigurationManager().getSapphireDatabases();
            return list;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getConfigProperty(String connectionid, String propertyid, String defaultvalue) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getConfigurationManager().getConfigProperty(connectionid, propertyid, defaultvalue);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getProfileProperty(String connectionid, String sysuserid, String propertyid, String defaultvalue) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getConfigurationManager().getProfileProperty(connectionid, sysuserid, propertyid, defaultvalue);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void clearRSet(String connectionid, RSet rset) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getDataAccessManager().clearRSet(connectionid, rset);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void touchRSet(String connectionid, RSet rset) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getDataAccessManager().touchRSet(connectionid, rset);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSet(connectionid, sdcid, keyid1list, keyid2list, keyid3list);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, int bypassSecurityCode) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSet(connectionid, sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, bypassSecurityCode);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSetQ(String connectionid, String sdcid, String queryid, String[] params) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSetQ(connectionid, sdcid, queryid, params);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createLockedRSet(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createLockedRSet(connectionid, sdcid, keyid1list, keyid2list, keyid3list, lockoption);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSetDS(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateBoth, boolean calcexpand) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSetDS(connectionid, sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSetWI(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateBoth) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSetWI(connectionid, sdcid, keyid1list, keyid2list, keyid3list, workitemidlist, workiteminstancelist, populateBoth);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createRSetDSNP(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateBoth, boolean calcexpand) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createRSetDSNP(connectionid, sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateBoth, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createLockedRSetDS(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createLockedRSetDS(connectionid, sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, lockoption, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet createLockedRSetDSNP(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().createLockedRSetDSNP(connectionid, sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, lockoption, calcexpand);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public RSet lockRSet(String connectionid, RSet rset, String lockoption, int lockscope) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            RSet rSet = ServiceLocator.getInstance().getDataAccessManager().lockRSet(connectionid, rset, lockoption, lockscope);
            return rSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public SDIList checkSDIAccess(String connectionid, String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, String operation) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            SDIList sDIList = ServiceLocator.getInstance().getDataAccessManager().checkSDIAccess(connectionid, sdcid, keyid1list, keyid2list, keyid3list, viewHiddenRecord, operation);
            return sDIList;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean setGlobalLock(String connectionid, boolean lock) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getDataAccessManager().setGlobalLock(connectionid, lock);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean isGlobalLock(String connectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getDataAccessManager().isGlobalLock(connectionid);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getSQLDataSet(String connectionid, String name, String sql, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getSQLDataSet(connectionid, name, sql, extendedDataTypes, queryTimeout);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getSQLDataSet(String connectionid, String name, String sql, boolean extendedDataTypes, int queryTimeout, boolean keepAlive) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getSQLDataSet(connectionid, name, sql, extendedDataTypes, queryTimeout, keepAlive);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getSQLDataSet(String connectionid, int sqlCode, Object[] bindVars, boolean extendedDataTypes) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getSQLDataSet(connectionid, sqlCode, bindVars, extendedDataTypes);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getPreparedSqlDataSet(String connectionid, String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getPreparedSqlDataSet(connectionid, name, sql, params, extendedDataTypes, queryTimeout);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getPreparedSqlDataSet(String connectionid, int sqlCode, Object[] params, boolean extendedDataTypes) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getPreparedSqlDataSet(connectionid, sqlCode, params, extendedDataTypes);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getRefTypeDataSet(String connectionid, String reftypeid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getQueryManager().getRefTypeDataSet(connectionid, reftypeid);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getQueryKeyid1List(String connectionid, String sdcid, String queryid, String[] params) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getQueryManager().getQueryKeyid1List(connectionid, sdcid, queryid, params);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int execPreparedUpdate(String connectionid, String sql, Object[] bindvars) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getQueryManager().execPreparedUpdate(connectionid, sql, bindvars);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int execSQL(String connectionid, String sql) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getQueryManager().execSQL(connectionid, sql);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int execSQL(String connectionid, int sqlCode, Object[] bindVars) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getQueryManager().execSQL(connectionid, sqlCode, bindVars);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public PropertyList getSDCProperties(String connectionid, String sdcid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            PropertyList propertyList = ServiceLocator.getInstance().getDDTManager().getSDCProperties(connectionid, sdcid);
            return propertyList;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public DataSet getReverseLinksData(String connectionid, String sdcid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            DataSet dataSet = ServiceLocator.getInstance().getDDTManager().getReverseLinksData(connectionid, sdcid);
            return dataSet;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public PropertyListCollection getTableColumns(String connectionid, String tableid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            PropertyListCollection propertyListCollection = ServiceLocator.getInstance().getDDTManager().getTableColumns(connectionid, tableid);
            return propertyListCollection;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public SDIData getSDIData(String connectionid, SDIRequest sdiRequest) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            SDIData sDIData = ServiceLocator.getInstance().getQueryManager().getSDIData(connectionid, sdiRequest);
            return sDIData;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int getSDICount(String connectionid, SDIRequest sdiRequest) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getQueryManager().getSDICount(connectionid, sdiRequest);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int getSDICount(String connectionid, SDIRequest sdiRequest, boolean keepAlive) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getQueryManager().getSDICount(connectionid, sdiRequest, keepAlive);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public int getSequence(String connectionid, String sdcid, String sequenceid, int start, int incrementBy) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            int n = ServiceLocator.getInstance().getSequenceManager().getSequence(connectionid, sdcid, sequenceid, start, incrementBy);
            return n;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getUUID(String connectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getSequenceManager().getUUID(connectionid);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void saveTranslation(String connectionid, String language, String textidlist, String transtextlist, String texttypelist) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getI18NManager().saveTranslation(connectionid, language, textidlist, transtextlist, texttypelist);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public PropertyList translateTable(String connectionid, String languageid, PropertyList transTable) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            PropertyList propertyList = ServiceLocator.getInstance().getI18NManager().translateTable(connectionid, languageid, transTable);
            return propertyList;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public boolean isAutoFillTempAllowed(String connectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            boolean bl = ServiceLocator.getInstance().getI18NManager().isAutoFillTempAllowed(connectionid);
            return bl;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void addToTransmasterTemp(String connectionid, String originaltext, String texttype) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getI18NManager().addToTransmasterTemp(connectionid, originaltext, texttype);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public PropertyList getWebTranslations(String connectionid, String languageid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            PropertyList propertyList = ServiceLocator.getInstance().getI18NManager().getWebTranslations(connectionid, languageid);
            return propertyList;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public HashMap processCommand(HashMap<String, Object> params) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByDatabaseid("", "JavaAPI");
        }
        try {
            HashMap hashMap = ServiceLocator.getInstance().getSapphireManager().processCommand(params);
            return hashMap;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public void processCommands(ArrayList<HashMap<String, Object>> params) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByDatabaseid("", "JavaAPI");
        }
        try {
            ServiceLocator.getInstance().getSapphireManager().processCommands(params);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getPropertyTreeDef(String connectionid, String propertytreeid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getWebAdminManager().getPropertyTreeDef(connectionid, propertytreeid);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getPropertyTreeValue(String connectionid, String propertytreeid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getRequestManager().getPropertyTreeValue(connectionid, propertytreeid);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public Set<String> getInactiveRoleList(String connectionid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            Set set = ServiceLocator.getInstance().getRequestManager().getInactiveRoleList(connectionid);
            return set;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public PropertyList processRequest(String connectionid, String requestHandler, PropertyList requestProps) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            PropertyList propertyList = new PropertyList(ServiceLocator.getInstance().getRequestManager().processRequest(connectionid, requestHandler, requestProps));
            return propertyList;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }

    public String getSecurityFilterWhere(String connectionid, String sdcid) throws ManagerException {
        if (this instanceof RemoteAccessManagement) {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
        }
        try {
            String string = ServiceLocator.getInstance().getQueryManager().getSecurityFilterWhere(connectionid, sdcid);
            return string;
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        finally {
            if (this instanceof RemoteAccessManagement) {
                Trace.clearThreadMDC();
            }
        }
    }
}

