/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.rmi.PortableRemoteObject
 */
package com.labvantage.sapphire.util.jndi;

import com.labvantage.sapphire.RemoteAccessKey;
import com.labvantage.sapphire.ejb.APQManagerHomeLocal;
import com.labvantage.sapphire.ejb.APQManagerLocal;
import com.labvantage.sapphire.ejb.ActionManagerHomeLocal;
import com.labvantage.sapphire.ejb.ActionManagerLocal;
import com.labvantage.sapphire.ejb.ActionTransManagerHomeLocal;
import com.labvantage.sapphire.ejb.ActionTransManagerLocal;
import com.labvantage.sapphire.ejb.AttachmentManagerHomeLocal;
import com.labvantage.sapphire.ejb.AttachmentManagerLocal;
import com.labvantage.sapphire.ejb.AutomationManagerHomeLocal;
import com.labvantage.sapphire.ejb.AutomationManagerLocal;
import com.labvantage.sapphire.ejb.ConfigurationManagerHomeLocal;
import com.labvantage.sapphire.ejb.ConfigurationManagerLocal;
import com.labvantage.sapphire.ejb.DDTManagerHomeLocal;
import com.labvantage.sapphire.ejb.DDTManagerLocal;
import com.labvantage.sapphire.ejb.DataAccessManagerHomeLocal;
import com.labvantage.sapphire.ejb.DataAccessManagerLocal;
import com.labvantage.sapphire.ejb.DataLockManagerHomeLocal;
import com.labvantage.sapphire.ejb.DataLockManagerLocal;
import com.labvantage.sapphire.ejb.I18NManagerHomeLocal;
import com.labvantage.sapphire.ejb.I18NManagerLocal;
import com.labvantage.sapphire.ejb.InstallManagerHomeLocal;
import com.labvantage.sapphire.ejb.InstallManagerLocal;
import com.labvantage.sapphire.ejb.InstrumentManagerHomeLocal;
import com.labvantage.sapphire.ejb.InstrumentManagerLocal;
import com.labvantage.sapphire.ejb.LocalAccessManagerHomeLocal;
import com.labvantage.sapphire.ejb.LocalAccessManagerLocal;
import com.labvantage.sapphire.ejb.QueryManagerHomeLocal;
import com.labvantage.sapphire.ejb.QueryManagerLocal;
import com.labvantage.sapphire.ejb.RemoteAccessManager;
import com.labvantage.sapphire.ejb.RemoteAccessManagerHome;
import com.labvantage.sapphire.ejb.RequestManagerHomeLocal;
import com.labvantage.sapphire.ejb.RequestManagerLocal;
import com.labvantage.sapphire.ejb.SapphireManagerHomeLocal;
import com.labvantage.sapphire.ejb.SapphireManagerLocal;
import com.labvantage.sapphire.ejb.SecurityManagerHomeLocal;
import com.labvantage.sapphire.ejb.SecurityManagerLocal;
import com.labvantage.sapphire.ejb.SequenceManagerHomeLocal;
import com.labvantage.sapphire.ejb.SequenceManagerLocal;
import com.labvantage.sapphire.ejb.StatusManagerHomeLocal;
import com.labvantage.sapphire.ejb.StatusManagerLocal;
import com.labvantage.sapphire.ejb.TDQManagerHomeLocal;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.ejb.WebAdminManagerHomeLocal;
import com.labvantage.sapphire.ejb.WebAdminManagerLocal;
import com.labvantage.sapphire.platform.Configuration;
import java.util.HashMap;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class ServiceLocator {
    private static String systemPassword;
    private static ServiceLocator serviceLocator;
    private static Context initialContext;
    private static HashMap<String, RemoteAccessManager> ramCache;
    private String dataSourcePrefix = "";
    private String jndiPrefix = "";
    private String localEJBPrefix = "";

    private ServiceLocator() throws SapphireException {
        try {
            initialContext = new InitialContext();
        }
        catch (NamingException e) {
            throw new SapphireException("Failed to locate initial context", e);
        }
    }

    public static ServiceLocator getInstance() throws SapphireException {
        return ServiceLocator.getInstance(Configuration.getInstance().getJNDIDataSourcePrefix(), Configuration.getInstance().getJNDILocalEJBPrefix(), Configuration.getInstance().getJNDIEJBPrefix());
    }

    public static ServiceLocator getInstance(String jndiDataSourcePrefix, String jndiLocalEJBPrefix, String jndiPrefix) throws SapphireException {
        ServiceLocator serviceLocator = new ServiceLocator();
        serviceLocator.dataSourcePrefix = jndiDataSourcePrefix;
        serviceLocator.localEJBPrefix = jndiLocalEJBPrefix;
        serviceLocator.jndiPrefix = jndiPrefix;
        if (!serviceLocator.jndiPrefix.endsWith("/")) {
            serviceLocator.jndiPrefix = serviceLocator.jndiPrefix + "/";
        }
        return serviceLocator;
    }

    public static void clearInstance() {
        serviceLocator = null;
    }

    public static RemoteAccessManager getRemoteAccessManager(RemoteAccessKey remoteAccessKey) throws Exception {
        return ServiceLocator.getRemoteAccessManager(remoteAccessKey.getProperty("java.naming.factory.initial"), remoteAccessKey.getProperty("java.naming.provider.url"), remoteAccessKey.getProperty("java.naming.security.principal"), remoteAccessKey.getProperty("java.naming.security.credentials"), remoteAccessKey.getProperty("sapphire.jndi.prefix"), remoteAccessKey.getProperty("sapphire.applicationear"));
    }

    public static synchronized RemoteAccessManager getRemoteAccessManager(String contextFactory, String providerURL, String securityPrincipal, String securityCredentials, String jndiPrefix, String earName) throws Exception {
        String cacheKey = contextFactory + ";" + providerURL + ";" + securityPrincipal + ";" + securityCredentials + ";" + jndiPrefix + ";" + earName;
        if (ramCache.get(cacheKey) != null) {
            return ramCache.get(cacheKey);
        }
        Properties props = new Properties();
        try {
            props.put("java.naming.factory.initial", contextFactory);
            props.put("java.naming.provider.url", providerURL);
            props.put("java.naming.security.principal", securityPrincipal);
            props.put("java.naming.security.credentials", securityCredentials);
            props.put("java.naming.factory.initial", contextFactory);
            if (jndiPrefix.length() == 0) {
                jndiPrefix = "com/labvantage/sapphire";
            }
            if (!jndiPrefix.endsWith("/")) {
                jndiPrefix = jndiPrefix + "/";
            }
            if ("org.jboss.naming.remote.client.InitialContextFactory".equals(contextFactory)) {
                props.put("java.naming.factory.url.pkgs", "org.jboss.ejb.client.naming");
                props.put("jboss.naming.client.ejb.context", (Object)true);
                initialContext = new InitialContext(props);
                ramCache.put(cacheKey, (RemoteAccessManager)new InitialContext(props).lookup(earName + "/sapphire/RemoteAccessManager!com.labvantage.sapphire.ejb.RemoteAccessManager"));
            } else if ("org.wildfly.naming.client.WildFlyInitialContextFactory".equals(contextFactory)) {
                InitialContext initialContext = new InitialContext(props);
                RemoteAccessManager remoteAccessManager = (RemoteAccessManager)initialContext.lookup("ejb:" + earName + "/sapphire/RemoteAccessManager!com.labvantage.sapphire.ejb.RemoteAccessManager");
                ramCache.put(cacheKey, remoteAccessManager);
            } else {
                initialContext = new InitialContext(props);
                Object objRef = initialContext.lookup(jndiPrefix + "ejb/RemoteAccessManager");
                RemoteAccessManagerHome home = (RemoteAccessManagerHome)PortableRemoteObject.narrow((Object)objRef, RemoteAccessManagerHome.class);
                ramCache.put(cacheKey, home.create());
            }
            return ramCache.get(cacheKey);
        }
        catch (Exception e) {
            System.out.println("Failed to gain access to Remote Access Manager: " + e.getMessage());
            System.out.println("INITIAL_CONTEXT_FACTORY: " + props.get("java.naming.factory.initial"));
            System.out.println("PROVIDER_URL: " + props.get("java.naming.provider.url"));
            System.out.println("SECURITY_PRINCIPAL: " + props.get("java.naming.security.principal"));
            e.printStackTrace();
            throw e;
        }
    }

    public DataSource getDataSource(String databaseid) throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.dataSourcePrefix + databaseid);
            if (objRef == null) {
                throw new SapphireException("Null object reference returned from datasource lookup for databaseid '" + databaseid + "'");
            }
            return (DataSource)objRef;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate datasource for databaseid '" + databaseid + "'", e);
        }
    }

    public ActionManagerLocal getActionManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("ActionManager"));
            if (objRef instanceof ActionManagerHomeLocal) {
                ActionManagerHomeLocal home = (ActionManagerHomeLocal)objRef;
                return home.create();
            }
            ActionManagerLocal local = (ActionManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate ActionManager", e);
        }
    }

    public ActionTransManagerLocal getActionTransManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("ActionTransManager"));
            if (objRef instanceof ActionTransManagerHomeLocal) {
                ActionTransManagerHomeLocal home = (ActionTransManagerHomeLocal)objRef;
                return home.create();
            }
            ActionTransManagerLocal local = (ActionTransManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate ActionTransManager", e);
        }
    }

    public AttachmentManagerLocal getAttachmentManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("AttachmentManager"));
            if (objRef instanceof AttachmentManagerHomeLocal) {
                AttachmentManagerHomeLocal home = (AttachmentManagerHomeLocal)objRef;
                return home.create();
            }
            AttachmentManagerLocal local = (AttachmentManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate AttachmentManager", e);
        }
    }

    public AutomationManagerLocal getAutomationManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("AutomationManager"));
            if (objRef instanceof AutomationManagerHomeLocal) {
                AutomationManagerHomeLocal home = (AutomationManagerHomeLocal)objRef;
                return home.create();
            }
            AutomationManagerLocal local = (AutomationManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate AutomationManager", e);
        }
    }

    public ConfigurationManagerLocal getConfigurationManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("ConfigurationManager"));
            if (objRef instanceof ConfigurationManagerHomeLocal) {
                ConfigurationManagerHomeLocal home = (ConfigurationManagerHomeLocal)objRef;
                return home.create();
            }
            ConfigurationManagerLocal local = (ConfigurationManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate ConfigurationManager", e);
        }
    }

    public DataAccessManagerLocal getDataAccessManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("DataAccessManager"));
            if (objRef instanceof DataAccessManagerHomeLocal) {
                DataAccessManagerHomeLocal home = (DataAccessManagerHomeLocal)objRef;
                return home.create();
            }
            DataAccessManagerLocal local = (DataAccessManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate DataAccessManager", e);
        }
    }

    public DataLockManagerLocal getDataLockManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("DataLockManager"));
            if (objRef instanceof DataLockManagerHomeLocal) {
                DataLockManagerHomeLocal home = (DataLockManagerHomeLocal)objRef;
                return home.create();
            }
            DataLockManagerLocal local = (DataLockManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate DataLockManager", e);
        }
    }

    public DDTManagerLocal getDDTManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("DDTManager"));
            if (objRef instanceof DDTManagerHomeLocal) {
                DDTManagerHomeLocal home = (DDTManagerHomeLocal)objRef;
                return home.create();
            }
            DDTManagerLocal local = (DDTManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate DDTManager", e);
        }
    }

    public I18NManagerLocal getI18NManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("I18NManager"));
            if (objRef instanceof I18NManagerHomeLocal) {
                I18NManagerHomeLocal home = (I18NManagerHomeLocal)objRef;
                return home.create();
            }
            I18NManagerLocal local = (I18NManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate I18NManager", e);
        }
    }

    public InstallManagerLocal getInstallManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("InstallManager"));
            if (objRef instanceof InstallManagerHomeLocal) {
                InstallManagerHomeLocal home = (InstallManagerHomeLocal)objRef;
                return home.create();
            }
            InstallManagerLocal local = (InstallManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate InstallManager", e);
        }
    }

    public InstrumentManagerLocal getInstrumentManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("InstrumentManager"));
            if (objRef instanceof InstrumentManagerHomeLocal) {
                InstrumentManagerHomeLocal home = (InstrumentManagerHomeLocal)objRef;
                return home.create();
            }
            InstrumentManagerLocal local = (InstrumentManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate InstrumentManager", e);
        }
    }

    public QueryManagerLocal getQueryManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("QueryManager"));
            if (objRef instanceof QueryManagerHomeLocal) {
                QueryManagerHomeLocal home = (QueryManagerHomeLocal)objRef;
                return home.create();
            }
            QueryManagerLocal local = (QueryManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate QueryManager", e);
        }
    }

    public RequestManagerLocal getRequestManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("RequestManager"));
            if (objRef instanceof RequestManagerHomeLocal) {
                RequestManagerHomeLocal home = (RequestManagerHomeLocal)objRef;
                return home.create();
            }
            RequestManagerLocal local = (RequestManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate RequestManager", e);
        }
    }

    public SapphireManagerLocal getSapphireManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("SapphireManager"));
            if (objRef instanceof SapphireManagerHomeLocal) {
                SapphireManagerHomeLocal home = (SapphireManagerHomeLocal)objRef;
                return home.create();
            }
            SapphireManagerLocal local = (SapphireManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate SapphireManager", e);
        }
    }

    public SecurityManagerLocal getSecurityManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("SecurityManager"));
            if (objRef instanceof SecurityManagerHomeLocal) {
                SecurityManagerHomeLocal home = (SecurityManagerHomeLocal)objRef;
                return home.create();
            }
            SecurityManagerLocal local = (SecurityManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate SecurityManager", e);
        }
    }

    public SequenceManagerLocal getSequenceManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("SequenceManager"));
            if (objRef instanceof SequenceManagerHomeLocal) {
                SequenceManagerHomeLocal home = (SequenceManagerHomeLocal)objRef;
                return home.create();
            }
            SequenceManagerLocal local = (SequenceManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate SequenceManager", e);
        }
    }

    public StatusManagerLocal getStatusManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("StatusManager"));
            if (objRef instanceof StatusManagerHomeLocal) {
                StatusManagerHomeLocal home = (StatusManagerHomeLocal)objRef;
                return home.create();
            }
            StatusManagerLocal local = (StatusManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate StatusManager", e);
        }
    }

    public WebAdminManagerLocal getWebAdminManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("WebAdminManager"));
            if (objRef instanceof WebAdminManagerHomeLocal) {
                WebAdminManagerHomeLocal home = (WebAdminManagerHomeLocal)objRef;
                return home.create();
            }
            WebAdminManagerLocal local = (WebAdminManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate WebAdminManager", e);
        }
    }

    public APQManagerLocal getAPQManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("APQManager"));
            if (objRef instanceof APQManagerHomeLocal) {
                APQManagerHomeLocal home = (APQManagerHomeLocal)objRef;
                return home.create();
            }
            APQManagerLocal local = (APQManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate APQManager", e);
        }
    }

    public TDQManagerLocal getTDQManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("TDQManager"));
            if (objRef instanceof TDQManagerHomeLocal) {
                TDQManagerHomeLocal home = (TDQManagerHomeLocal)objRef;
                return home.create();
            }
            TDQManagerLocal local = (TDQManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate TDQManager", e);
        }
    }

    public LocalAccessManagerLocal getLocalAccessManager() throws SapphireException {
        try {
            Object objRef = initialContext.lookup(this.getEjbJndiUrl("LocalAccessManager"));
            if (objRef instanceof LocalAccessManagerHomeLocal) {
                LocalAccessManagerHomeLocal home = (LocalAccessManagerHomeLocal)objRef;
                return home.create();
            }
            LocalAccessManagerLocal local = (LocalAccessManagerLocal)objRef;
            return local;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to locate LocalAccessManager", e);
        }
    }

    public static void setSystemPassword(String systemPassword) {
        ServiceLocator.systemPassword = systemPassword;
    }

    private String getEjbJndiUrl(String ejbName) {
        if (this.localEJBPrefix.indexOf("[ejbname]") < 0) {
            return this.localEJBPrefix + this.jndiPrefix + "ejb/" + ejbName;
        }
        return StringUtil.replaceAll(this.localEJBPrefix, "[ejbname]", ejbName);
    }

    static {
        ramCache = new HashMap();
    }
}

