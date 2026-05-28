/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub;

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.ActionBlockTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.AttachmentTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.BaseSECMessage;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyListTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SDIDataTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SDIRequestTransport;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SapphireWS_PortType
extends Remote {
    public String[] getDatabaseList() throws RemoteException;

    public String translateTable(String var1, String var2, String var3) throws RemoteException;

    public PropertyListTransport translateTable(String var1, String var2, PropertyListTransport var3) throws RemoteException;

    public void addSDIAttachment(String var1, String var2, String var3, String var4, String var5, AttachmentTransport var6) throws RemoteException;

    public int execSQL(String var1, int var2, Object[] var3) throws RemoteException;

    public int execSQL(String var1, int var2) throws RemoteException;

    public int execSQL(String var1, String var2) throws RemoteException;

    public PropertyListTransport getSDCProperties(String var1, String var2) throws RemoteException;

    public AttachmentTransport getSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6, boolean var7) throws RemoteException;

    public PropertyListTransport processAction(String var1, String var2, String var3, PropertyListTransport var4) throws RemoteException;

    public String processAction(String var1, String var2, String var3, String var4) throws RemoteException;

    public BaseSECMessage processMessage(String var1, BaseSECMessage var2, String var3) throws RemoteException;

    public String processMessage(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

    public DataSetTransport getSqlDataSet(String var1, int var2, Object[] var3, boolean var4) throws RemoteException;

    public String getSqlDataSet(String var1, int var2) throws RemoteException;

    public String getSqlDataSet(String var1, int var2, Object[] var3) throws RemoteException;

    public DataSetTransport getSqlDataSet(String var1, String var2, boolean var3) throws RemoteException;

    public DataSetTransport getSqlDataSet(String var1, int var2, boolean var3) throws RemoteException;

    public String getSqlDataSet(String var1, String var2) throws RemoteException;

    public String processApplicationCommand(String var1, String var2) throws RemoteException;

    public SDIDataTransport getSDIData(String var1, SDIRequestTransport var2) throws RemoteException;

    public ActionBlockTransport processActionBlock(String var1, ActionBlockTransport var2) throws RemoteException;

    public void editSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6, AttachmentTransport var7) throws RemoteException;

    public void deleteSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6) throws RemoteException;

    public String getPublicKey() throws RemoteException;

    public String getVersion() throws RemoteException;

    public int getSequence(String var1, String var2, String var3, int var4, int var5) throws RemoteException;

    public String getConnectionId(String var1, String var2, String var3) throws RemoteException;

    public boolean checkConnection(String var1) throws RemoteException;

    public void clearConnection(String var1) throws RemoteException;
}

