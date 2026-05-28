/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigurationConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import sapphire.SapphireException;
import sapphire.util.HttpUtil;

public class ConfigService
extends BaseService
implements ConfigurationConstants,
CacheNames {
    public static final String LOGNAME = "ConfigService";
    public static final String CACHE_CONFIG_PROPERTIES = "ConfigProperties";
    private static final Cache adminConfigCache = new Cache("ConfigProperties");

    public static String getConfigProperty(String propertyid) throws ServiceException {
        return ConfigService.getConfigProperty(propertyid, "");
    }

    public static boolean getConfigPropertyBoolean(String propertyid, boolean defaultValue) throws ServiceException {
        String property = ConfigService.getConfigProperty(propertyid, defaultValue ? "Y" : "N");
        return property.equals("Y") || property.equals("true");
    }

    public static String getConfigProperty(String propertyid, String defaultvalue) throws ServiceException {
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        String value = (String)adminConfigCache.get(propertyid);
        if (value == null) {
            try {
                String filevalue;
                Properties props = new Properties();
                File file = Configuration.getInstance().getConfigFile();
                if (file.exists()) {
                    props.load(new FileInputStream(file));
                }
                adminConfigCache.put(propertyid, (filevalue = props.getProperty(propertyid)) != null && filevalue.length() > 0 ? filevalue : "");
                value = (String)adminConfigCache.get(propertyid);
            }
            catch (Exception e) {
                throw new ServiceException("Unable to retrieve config property: " + propertyid, e);
            }
        }
        if (value == null || value.length() == 0) {
            value = defaultvalue;
        }
        Trace.logDebug(LOGNAME, "Returning config property " + propertyid + "=" + value);
        return value;
    }

    public static HashMap<String, String> getConfigPropertyNames(String propertyValue) throws ServiceException {
        HashMap<String, String> propertyNames = new HashMap<String, String>();
        try {
            Properties props = new Properties();
            File file = Configuration.getInstance().getConfigFile();
            if (file.exists()) {
                props.load(new FileInputStream(file));
            }
            props.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(k, v) -> {
                if (v != null && propertyValue.equalsIgnoreCase(v.toString())) {
                    propertyNames.put(k.toString(), v.toString());
                }
            }));
        }
        catch (Exception e) {
            throw new ServiceException("Unable to retrieve config property names for value: " + propertyValue, e);
        }
        return propertyNames;
    }

    public static void setConfigProperty(String propertyid, String propertyvalue) throws ServiceException {
        Trace.logInfo(LOGNAME, "Setting config property '" + propertyid + "'");
        try {
            File file = Configuration.getInstance().getConfigFile();
            Properties props = new Properties();
            if (file.exists()) {
                props.load(new FileInputStream(file));
            }
            if (propertyvalue == null) {
                propertyvalue = "";
            }
            props.put(propertyid, propertyvalue);
            props.store(new FileOutputStream(file), "Sapphire Config Properties");
            adminConfigCache.put(propertyid, propertyvalue);
        }
        catch (Exception e) {
            throw new ServiceException("Unable to store config property:" + propertyid, e);
        }
    }

    public ConfigService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public String getDevModeProperty(String propertyid, String defaultvalue) {
        try {
            if (this.getSysConfigProperty("devmode", "N").equals("Y")) {
                String filevalue;
                String value = defaultvalue;
                Properties props = new Properties();
                File file = new File(Configuration.getInstance().getSapphireHome() + File.separator + "devmode.props");
                if (file.exists()) {
                    props.load(new FileInputStream(file));
                }
                value = (filevalue = props.getProperty(propertyid)) != null && filevalue.length() > 0 ? filevalue : defaultvalue;
                return value;
            }
            return defaultvalue;
        }
        catch (Exception e) {
            Trace.logError("Unable to retrieve devmode property: " + propertyid, e);
            return defaultvalue;
        }
    }

    public String getSysConfigProperty(String propertyid) throws ServiceException {
        return this.getSysConfigProperty(propertyid, "");
    }

    public String getSysConfigProperty(String propertyid, String defaultvalue) throws ServiceException {
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        String value = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "SysConfigProperties", propertyid);
        if (value == null) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                dbu.setConnection(this.sapphireConnection);
                dbu.createPreparedResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = ?", propertyid);
                if (dbu.getNext()) {
                    value = dbu.getString("propertyvalue");
                } else if (dbu.isOracle()) {
                    dbu.createPreparedResultSet("SELECT propertyvalue FROM sysconfig WHERE upper( propertyid ) = ?", propertyid.toUpperCase());
                    if (dbu.getNext()) {
                        value = dbu.getString("propertyvalue");
                    } else {
                        this.logDebug("No sysconfig property found for propertyid '" + propertyid + "'");
                        value = "";
                    }
                } else {
                    this.logDebug("No sysconfig property found for propertyid '" + propertyid + "'");
                    value = "";
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "SysConfigProperties", propertyid, value == null ? "" : value);
            }
            catch (SapphireException e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to load sysconfig property '" + propertyid + "'", e);
            }
            finally {
                dbu.reset();
            }
        }
        if (value == null || value.length() == 0) {
            value = defaultvalue;
        }
        this.logDebug("Returning sysconfig property " + propertyid + "=" + value);
        return value;
    }

    public void setSysConfigProperty(String propertyid, String value) throws ServiceException {
        this.logInfo("Setting sysconfig property '" + propertyid + "'");
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            if (dbu.executePreparedUpdate("UPDATE sysconfig SET propertyvalue = ? WHERE propertyid = ?", new Object[]{value, propertyid}) != 1) {
                dbu.executePreparedUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( ?, ? )", new Object[]{propertyid, value});
            }
            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "SysConfigProperties", propertyid, value == null ? "" : value);
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to save sysconfig property '" + propertyid + "'", e);
        }
        finally {
            dbu.reset();
        }
    }

    public String getProfileProperty(String sysuserid, String propertyid) throws ServiceException {
        return this.getProfileProperty(sysuserid, propertyid, "");
    }

    public String getProfileProperty(String sysuserid, String propertyid, String defaultvalue) throws ServiceException {
        this.logDebug("Getting profile property '" + propertyid + "' for sysuserid '" + sysuserid + "'");
        if (sysuserid == null || sysuserid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        String value = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";" + propertyid);
        if (value == null) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                dbu.setConnection(this.sapphireConnection);
                dbu.createPreparedResultSet("SELECT sysuserid, propertyvalue FROM profileproperty WHERE profileid = ? AND propertyid = ? AND ( sysuserid = ? OR sysuserid = ? )ORDER BY 1 DESC", new Object[]{"System", propertyid, "(system)", sysuserid});
                boolean foundActualUserValue = false;
                if (dbu.getNext()) {
                    foundActualUserValue = dbu.getString("sysuserid").equals(sysuserid);
                    String profilesysuserid = dbu.getString("sysuserid");
                    value = dbu.getString("propertyvalue");
                    if (!foundActualUserValue && dbu.getNext()) {
                        foundActualUserValue = dbu.getString("sysuserid").equals(sysuserid);
                        value = dbu.getString("propertyvalue");
                    }
                }
                if (!foundActualUserValue && dbu.isOracle()) {
                    dbu.createPreparedResultSet("SELECT sysuserid, propertyvalue FROM profileproperty WHERE profileid = ? AND upper( propertyid ) = ? AND upper( sysuserid ) = ? ORDER BY 1 DESC", new Object[]{"System", propertyid.toUpperCase(), sysuserid.toUpperCase()});
                    if (dbu.getNext()) {
                        foundActualUserValue = dbu.getString("sysuserid").equals(sysuserid);
                        value = dbu.getString("propertyvalue");
                        if (!foundActualUserValue && dbu.getNext()) {
                            value = dbu.getString("propertyvalue");
                        }
                    }
                }
                if (value == null || value.isEmpty()) {
                    this.logDebug("No profile property found for propertyid '" + propertyid + "'");
                    value = "";
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";" + propertyid, value == null ? "" : value);
            }
            catch (SapphireException e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to load profile property '" + propertyid + "'", e);
            }
            finally {
                dbu.reset();
            }
        }
        if (value == null || value.length() == 0) {
            value = defaultvalue;
        }
        this.logDebug("Profile property found for sysuserid '" + sysuserid + "', propertyid '" + propertyid + "', value = '" + value + "'");
        return value;
    }

    public String[] loadProfilePropertiesStartingWith(String sysuserid, String startPropertyid) throws ServiceException {
        this.logDebug("Loading profile properties matching '" + startPropertyid + "' for sysuserid '" + sysuserid + "'");
        if (sysuserid == null || sysuserid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (startPropertyid == null || startPropertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "StartPropertyid not specified");
        }
        String[] properties = (String[])CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";__startingWith" + startPropertyid);
        if (properties == null) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            TreeMap<String, String> props = new TreeMap<String, String>();
            try {
                dbu.setConnection(this.sapphireConnection);
                dbu.createPreparedResultSet("SELECT sysuserid, propertyid, propertyvalue FROM profileproperty WHERE profileid = ? AND propertyid LIKE '" + startPropertyid + "%' AND ( sysuserid = ? OR upper( sysuserid ) = ? )ORDER BY 2, 1, 3", new Object[]{"System", "(system)", sysuserid.toUpperCase()});
                while (dbu.getNext()) {
                    props.put(dbu.getString("propertyid"), dbu.getString("propertyvalue"));
                }
                properties = new String[props.size()];
                int i = 0;
                for (String propertyid : props.keySet()) {
                    properties[i++] = propertyid;
                    String value = (String)props.get(propertyid);
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";" + propertyid, value == null ? "" : value);
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";__startingWith" + startPropertyid, properties);
            }
            catch (SapphireException e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to load profile properties matching '" + startPropertyid + "'", e);
            }
            finally {
                dbu.reset();
            }
        }
        return Arrays.copyOf(properties, properties.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setProfileProperty(String sysuserid, String propertyid, String value) throws ServiceException {
        this.logInfo("Setting profile property '" + propertyid + "' for sysuserid '" + sysuserid + "'");
        if (sysuserid == null || sysuserid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        propertyid = HttpUtil.decodeURIComponent(propertyid);
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            block12: {
                dbu.setConnection(this.sapphireConnection);
                if (SecurityService.isSystemUser(this.sapphireConnection.getDatabaseId(), sysuserid)) {
                    sysuserid = "(system)";
                }
                String insertValue = value;
                if (propertyid.startsWith("userconfig_") && value.length() > 4000) {
                    Trace.log("Cannot save profileproperty " + propertyid + ". The value is longer than 4000 characters");
                    insertValue = "";
                }
                try {
                    if (dbu.executePreparedUpdate("UPDATE profileproperty SET propertyvalue = ? WHERE profileid = ? AND propertyid = ? AND sysuserid = ?", new Object[]{insertValue, "System", propertyid, sysuserid}) == 1) break block12;
                    try {
                        dbu.executePreparedUpdate("INSERT INTO profileproperty ( profileid, propertyid, sysuserid, propertyvalue ) VALUES ( ?, ?, ?, ? )", new Object[]{"System", propertyid, sysuserid, insertValue});
                    }
                    catch (SapphireException e) {
                        if (!(this.sapphireConnection.isOracle() && e.getMessage().contains("ORA-00001") || this.sapphireConnection.isSqlServer() && e.getMessage().contains("duplicate key"))) {
                            throw e;
                        }
                    }
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
            }
            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";" + propertyid, value == null ? "" : value);
        }
        finally {
            dbu.reset();
        }
    }

    public void resetProfileProperty(String sysuserid, String propertyid) throws ServiceException {
        this.logInfo("Resetting profile property '" + propertyid + "' for sysuserid '" + sysuserid + "'");
        if (sysuserid == null || sysuserid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (propertyid == null || propertyid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Propertyid not specified");
        }
        if (SecurityService.isSystemUser(this.sapphireConnection.getDatabaseId(), sysuserid) || sysuserid.equals("(system)")) {
            throw new ServiceException("INVALID_PARAMETER", "System user profile property cannot be reset");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            dbu.executePreparedUpdate("DELETE FROM profileproperty WHERE profileid = ? AND propertyid = ? AND sysuserid = ?", new Object[]{"System", propertyid, sysuserid});
            CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "ProfileProperties", sysuserid + ";" + propertyid);
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to save profile property '" + propertyid + "'", e);
        }
        finally {
            dbu.reset();
        }
    }

    public static void resetCache() {
        adminConfigCache.clear();
    }
}

