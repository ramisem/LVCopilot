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
import com.labvantage.sapphire.ejb.ConfigurationManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SecurityService;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

public class ConfigurationManagerBean
extends BaseManager
implements SessionBean,
ConfigurationManagement {
    public ConfigurationManagerBean() {
        this.logName = "ConfigurationManager";
    }

    @Override
    public void logInfo(Object out) {
        Trace.logDebug(this.logName, out, this.logContext);
    }

    @Override
    public String getConfigProperty(String connectionid, String propertyid, String defaultvalue) throws ManagerException {
        String methodName = "getConfigProperty";
        try {
            this.startMethod(methodName, true, connectionid);
            String string = ConfigService.getConfigProperty(propertyid, defaultvalue);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get config property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void setConfigProperty(String connectionid, String propertyid, String value) throws ManagerException {
        String methodName = "setConfigProperty";
        try {
            this.startMethod(methodName, connectionid);
            ConfigService.setConfigProperty(propertyid, value);
        }
        catch (Exception e) {
            this.logError("Failed to set config property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String getSysConfigProperty(String connectionid, String propertyid, String defaultvalue) throws ManagerException {
        String methodName = "getSysConfigProperty";
        try {
            this.startMethod(methodName, true, connectionid);
            ConfigService configService = new ConfigService(this.sapphireConnection);
            String string = configService.getSysConfigProperty(propertyid, defaultvalue);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get sysconfig property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void setSysConfigProperty(String connectionid, String propertyid, String value) throws ManagerException {
        String methodName = "setSysConfigProperty";
        try {
            this.startMethod(methodName, connectionid);
            ConfigService configService = new ConfigService(this.sapphireConnection);
            configService.setSysConfigProperty(propertyid, value);
        }
        catch (Exception e) {
            this.logError("Failed to set sysconfig property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String getProfileProperty(String connectionid, String sysuserid, String propertyid, String defaultvalue) throws ManagerException {
        String methodName = "getProfileProperty";
        try {
            this.startMethod(methodName, true, connectionid);
            ConfigService configService = new ConfigService(this.sapphireConnection);
            String string = configService.getProfileProperty(sysuserid, propertyid, defaultvalue);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get profile property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void setProfileProperty(String connectionid, String sysuserid, String propertyid, String value) throws ManagerException {
        String methodName = "setProfileProperty";
        try {
            this.startMethod(methodName, connectionid);
            ConfigService configService = new ConfigService(this.sapphireConnection);
            configService.setProfileProperty(sysuserid, propertyid, value);
        }
        catch (Exception e) {
            this.logError("Failed to set profile property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String getLicenseProperty(String connectionid, String propertyid) throws ManagerException {
        String methodName = "getLicenseProperty";
        try {
            this.startMethod(methodName, true, connectionid);
            String databaseid = SecurityService.getDatabaseId(connectionid);
            String string = Configuration.getInstance().getLicense(databaseid).getProperty(propertyid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get license property '" + propertyid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public List<SapphireDatabase> getSapphireDatabases() throws ManagerException {
        String methodName = "getSapphireDatabases";
        try {
            this.startMethod(methodName);
            List<SapphireDatabase> list = Configuration.getInstance().getSapphireDatabases();
            return list;
        }
        catch (Exception e) {
            this.logError("Failed to get Sapphire Database list from configuration", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

