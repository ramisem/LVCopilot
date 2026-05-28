/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.SapphireConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import sapphire.attachment.Attachment;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public interface RemoteAccessManagement {
    public ActionBlock processActionBlock(String var1, ActionBlock var2, boolean var3, boolean var4) throws RemoteException, ManagerException;

    public Attachment getSDIAttachment(String var1, Attachment var2, Attachment.ThumbnailGeneration var3) throws RemoteException, ManagerException;

    public Attachment getSDIAttachment(String var1, Attachment var2, int var3, Attachment.ThumbnailGeneration var4) throws RemoteException, ManagerException;

    public Attachment getTempAttachment(String var1, Attachment var2, Attachment.ThumbnailGeneration var3) throws RemoteException, ManagerException;

    public HashMap addSDIAttachment(String var1, HashMap var2, byte[] var3) throws RemoteException, ManagerException;

    public Attachment addSDIAttachment(String var1, Attachment var2, boolean var3, boolean var4, String var5) throws RemoteException, ManagerException;

    public HashMap editSDIAttachment(String var1, HashMap var2, byte[] var3) throws RemoteException, ManagerException;

    public Attachment editSDIAttachment(String var1, Attachment var2, boolean var3, boolean var4, String var5) throws RemoteException, ManagerException;

    public void deleteSDIAttachment(String var1, Attachment var2, boolean var3, String var4) throws RemoteException, ManagerException;

    public String getConnectionId(SapphireConnection var1) throws RemoteException, ManagerException;

    public void clearConnection(String var1, String var2) throws RemoteException, ManagerException;

    public SapphireConnection getSapphireConnection(String var1) throws RemoteException, ManagerException;

    public void prepareToDeleteConnection(String var1, String var2) throws RemoteException, ManagerException;

    public void changePassword(String var1, String var2, String var3, String var4) throws RemoteException, ManagerException;

    public boolean isValidPassword(String var1, String var2, String var3) throws RemoteException, ManagerException;

    public boolean checkUser(String var1, String var2, String var3) throws RemoteException, ManagerException;

    public boolean checkConnection(String var1, boolean var2) throws RemoteException, ManagerException;

    public void clearRSets(String var1, String var2) throws RemoteException, ManagerException;

    public String getLicenseProperty(String var1, String var2) throws RemoteException, ManagerException;

    public void disableUser(String var1, String var2, String var3) throws RemoteException, ManagerException;

    public void enableUser(String var1, String var2) throws RemoteException, ManagerException;

    public void forcePasswordChange(String var1, String var2) throws RemoteException, ManagerException;

    public List getSapphireDatabases() throws RemoteException, ManagerException;

    public String getConfigProperty(String var1, String var2, String var3) throws RemoteException, ManagerException;

    public String getProfileProperty(String var1, String var2, String var3, String var4) throws RemoteException, ManagerException;

    public void clearRSet(String var1, RSet var2) throws RemoteException, ManagerException;

    public void touchRSet(String var1, RSet var2) throws RemoteException, ManagerException;

    public RSet createRSet(String var1, String var2, String var3, String var4, String var5) throws RemoteException, ManagerException;

    public RSet createRSet(String var1, String var2, String var3, String var4, String var5, boolean var6, int var7) throws RemoteException, ManagerException;

    public RSet createRSetQ(String var1, String var2, String var3, String[] var4) throws RemoteException, ManagerException;

    public RSet createLockedRSet(String var1, String var2, String var3, String var4, String var5, String var6) throws RemoteException, ManagerException;

    public RSet createRSetDS(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10, boolean var11) throws RemoteException, ManagerException;

    public RSet createRSetDSNP(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10, boolean var11) throws RemoteException, ManagerException;

    public RSet createLockedRSetDS(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws RemoteException, ManagerException;

    public RSet createLockedRSetDSNP(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws RemoteException, ManagerException;

    public RSet createRSetWI(String var1, String var2, String var3, String var4, String var5, String var6, String var7, boolean var8) throws RemoteException, ManagerException;

    public RSet lockRSet(String var1, RSet var2, String var3, int var4) throws RemoteException, ManagerException;

    public SDIList checkSDIAccess(String var1, String var2, String var3, String var4, String var5, boolean var6, String var7) throws RemoteException, ManagerException;

    public boolean setGlobalLock(String var1, boolean var2) throws RemoteException, ManagerException;

    public boolean isGlobalLock(String var1) throws RemoteException, ManagerException;

    public DataSet getSQLDataSet(String var1, String var2, String var3, boolean var4, int var5, boolean var6) throws RemoteException, ManagerException;

    public DataSet getSQLDataSet(String var1, String var2, String var3, boolean var4, int var5) throws RemoteException, ManagerException;

    public DataSet getSQLDataSet(String var1, int var2, Object[] var3, boolean var4) throws RemoteException, ManagerException;

    public DataSet getPreparedSqlDataSet(String var1, String var2, String var3, Object[] var4, boolean var5, int var6) throws RemoteException, ManagerException;

    public DataSet getPreparedSqlDataSet(String var1, int var2, Object[] var3, boolean var4) throws RemoteException, ManagerException;

    public DataSet getRefTypeDataSet(String var1, String var2) throws RemoteException, ManagerException;

    public String getQueryKeyid1List(String var1, String var2, String var3, String[] var4) throws RemoteException, ManagerException;

    public int execPreparedUpdate(String var1, String var2, Object[] var3) throws RemoteException, ManagerException;

    public int execSQL(String var1, String var2) throws RemoteException, ManagerException;

    public int execSQL(String var1, int var2, Object[] var3) throws RemoteException, ManagerException;

    public PropertyList getSDCProperties(String var1, String var2) throws RemoteException, ManagerException;

    public DataSet getReverseLinksData(String var1, String var2) throws RemoteException, ManagerException;

    public PropertyListCollection getTableColumns(String var1, String var2) throws RemoteException, ManagerException;

    public SDIData getSDIData(String var1, SDIRequest var2) throws RemoteException, ManagerException;

    public int getSDICount(String var1, SDIRequest var2, boolean var3) throws RemoteException, ManagerException;

    public int getSDICount(String var1, SDIRequest var2) throws RemoteException, ManagerException;

    public int getSequence(String var1, String var2, String var3, int var4, int var5) throws RemoteException, ManagerException;

    public String getUUID(String var1) throws RemoteException, ManagerException;

    public void saveTranslation(String var1, String var2, String var3, String var4, String var5) throws RemoteException, ManagerException;

    public PropertyList translateTable(String var1, String var2, PropertyList var3) throws RemoteException, ManagerException;

    public boolean isAutoFillTempAllowed(String var1) throws RemoteException, ManagerException;

    public void addToTransmasterTemp(String var1, String var2, String var3) throws RemoteException, ManagerException;

    public PropertyList getWebTranslations(String var1, String var2) throws RemoteException, ManagerException;

    public HashMap processCommand(HashMap<String, Object> var1) throws RemoteException, ManagerException;

    public void processCommands(ArrayList<HashMap<String, Object>> var1) throws RemoteException, ManagerException;

    public String getPropertyTreeDef(String var1, String var2) throws RemoteException, ManagerException;

    public String getPropertyTreeValue(String var1, String var2) throws RemoteException, ManagerException;

    public Set<String> getInactiveRoleList(String var1) throws RemoteException, ManagerException;

    public PropertyList processRequest(String var1, String var2, PropertyList var3) throws RemoteException, ManagerException;

    public String getSecurityFilterWhere(String var1, String var2) throws RemoteException, ManagerException;
}

