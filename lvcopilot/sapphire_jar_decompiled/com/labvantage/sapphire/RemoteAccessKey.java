/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import sapphire.SapphireException;

public class RemoteAccessKey {
    public static final String INITIAL_CONTEXT_FACTORY = "java.naming.factory.initial";
    public static final String PROVIDER_URL = "java.naming.provider.url";
    public static final String SECURITY_PRINCIPAL = "java.naming.security.principal";
    public static final String SECURITY_CREDENTIALS = "java.naming.security.credentials";
    public static final String JNDI_PREFIX = "sapphire.jndi.prefix";
    public static final String DATABASEID = "sapphire.databaseid";
    public static final String APPLICATIONID = "sapphire.applicationid";
    public static final String APPLICATIONEAR = "sapphire.applicationear";
    public static final String PLATFORMNAME = "sapphire.platform.name";
    private Properties rakProps = new Properties();

    public RemoteAccessKey(String databaseid) throws SapphireException {
        this.loadRemoteAccessKey(null, databaseid);
    }

    public RemoteAccessKey(File remoteAccessKeyFile) throws SapphireException {
        this.loadRemoteAccessKey(remoteAccessKeyFile, null);
    }

    public String getProperty(String propertyid) {
        String value = this.rakProps.getProperty(propertyid);
        if (propertyid.equals(APPLICATIONEAR) && (value == null || value.length() == 0)) {
            value = this.rakProps.getProperty(APPLICATIONID);
        }
        if (propertyid.equals(SECURITY_CREDENTIALS) && (value == null || value.length() == 0)) {
            value = EncryptDecrypt.decrypt(value);
        }
        return value != null ? value : "";
    }

    public static void generateRemoteAccessKey(File rak, String databaseid, String serverURL, String username, String password) throws SapphireException {
        String jndiPrefix;
        try {
            jndiPrefix = ConfigService.getConfigProperty(Configuration.getInstance().getJNDIEJBPrefix());
        }
        catch (ServiceException e) {
            jndiPrefix = "";
        }
        String platformName = Configuration.getPlatformName(Configuration.getInstance().getPlatform());
        String initialContextFactory = Configuration.getInstance().getInitialContextFactory();
        String applicationid = Configuration.getInstance().getApplicationid();
        String applicationear = Configuration.getInstance().getApplicationEARName();
        RemoteAccessKey.generateRemoteAccessKey(rak, platformName, initialContextFactory, applicationid, applicationear, databaseid, serverURL, username, password, jndiPrefix);
    }

    public static void generateRemoteAccessKey(File rak, String platformName, String initialContextFactory, String applicationid, String applicationear, String databaseid, String serverURL, String username, String password, String jndiPrefix) throws SapphireException {
        Properties rakProps = new Properties();
        rakProps.setProperty(INITIAL_CONTEXT_FACTORY, initialContextFactory);
        rakProps.setProperty(PROVIDER_URL, serverURL);
        rakProps.setProperty(SECURITY_PRINCIPAL, username);
        rakProps.setProperty(SECURITY_CREDENTIALS, EncryptDecrypt.encrypt(password));
        rakProps.setProperty(JNDI_PREFIX, jndiPrefix);
        rakProps.setProperty(APPLICATIONID, applicationid);
        rakProps.setProperty(APPLICATIONEAR, applicationear.toLowerCase().indexOf(".ear") == -1 ? applicationear : applicationear.substring(0, applicationear.toLowerCase().indexOf(".ear")));
        rakProps.setProperty(DATABASEID, databaseid);
        rakProps.setProperty(PLATFORMNAME, platformName);
        try {
            FileOutputStream fos = new FileOutputStream(rak);
            rakProps.store(fos, "Remote Access Key for application: " + applicationid);
            fos.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to save remote access key", e);
        }
    }

    private void loadRemoteAccessKey(File rakFile, String databaseid) throws SapphireException {
        try {
            if (rakFile == null && System.getProperty("SAPPHIRE_RAKFILE") != null) {
                rakFile = new File(System.getProperty("SAPPHIRE_RAKFILE"));
            }
            if (rakFile == null) {
                String sapphireHome = System.getProperty("SAPPHIRE_HOME");
                rakFile = new File((sapphireHome == null ? "" : sapphireHome) + "/" + databaseid + ".rak");
            }
            if (rakFile.exists()) {
                int len = new Long(rakFile.length()).intValue();
                FileReader fileReader = new FileReader(rakFile);
                char[] buf = new char[len];
                fileReader.read(buf, 0, len);
                String rakfile = new String(buf);
                ByteArrayInputStream rakPropsStream = new ByteArrayInputStream(rakfile.getBytes());
                this.rakProps.load(rakPropsStream);
            } else {
                int len;
                byte[] buf;
                InputStream inStream = this.getClass().getResourceAsStream("/sapphire/" + databaseid + ".rak");
                if (inStream.read(buf = new byte[len = inStream.available()]) != len) {
                    throw new IOException("Read bytes does not match file size");
                }
                ByteArrayInputStream rakPropsStream = new ByteArrayInputStream(buf);
                this.rakProps.load(rakPropsStream);
            }
        }
        catch (IOException ioe) {
            throw new SapphireException(ioe.getMessage());
        }
    }
}

