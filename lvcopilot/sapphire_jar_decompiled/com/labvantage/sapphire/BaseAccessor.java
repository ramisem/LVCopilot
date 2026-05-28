/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.RemoteAccessKey;
import com.labvantage.sapphire.ejb.AttachmentManagerLocal;
import com.labvantage.sapphire.ejb.AutomationManagerLocal;
import com.labvantage.sapphire.ejb.ConfigurationManagerLocal;
import com.labvantage.sapphire.ejb.LocalAccessManagerLocal;
import com.labvantage.sapphire.ejb.QueryManagerLocal;
import com.labvantage.sapphire.ejb.RemoteAccessManager;
import com.labvantage.sapphire.ejb.RequestManagerLocal;
import com.labvantage.sapphire.ejb.StatusManagerLocal;
import com.labvantage.sapphire.ejb.WebAdminManagerLocal;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.File;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.HttpUtil;

public abstract class BaseAccessor
extends BaseClass {
    private String connectionid = "";
    private ArrayList errorcodes = new ArrayList();
    private ArrayList errors = new ArrayList();
    private String errorString;
    private String infoErrorString;
    private ErrorHandler errorHandler;
    private Throwable lastException;
    protected RemoteAccessKey remoteAccessKey = null;
    protected static boolean local = false;
    protected static String jndiPrefix = "com/labvantage/sapphire";
    protected static String localJNDIPrefix = "";

    public BaseAccessor() {
    }

    public BaseAccessor(String connectionid) {
        this.connectionid = connectionid;
        try {
            if (!local) {
                this.createRemoteAccessKey(SecurityService.getDatabaseId(connectionid));
            }
        }
        catch (SapphireException e) {
            this.setError(e.getMessage());
        }
    }

    public BaseAccessor(File rakFile, String connectionid) {
        this.connectionid = connectionid;
        try {
            if (!local) {
                this.createRemoteAccessKey(rakFile);
            }
        }
        catch (SapphireException e) {
            this.setError(e.getMessage());
        }
    }

    public BaseAccessor(PageContext pageContext) {
        this.connectionid = HttpUtil.getConnectionId(pageContext);
        try {
            if (!local) {
                this.createRemoteAccessKey(SecurityService.getDatabaseId(this.connectionid));
            }
        }
        catch (SapphireException e) {
            this.setError(e.getMessage());
        }
    }

    public void resetConnectionid() {
        this.connectionid = "";
    }

    public void setConnectionDetails(String nameserverlist, String databaseid, String username, String password) {
    }

    public void setRakFile(File rakFile) throws SapphireException {
        this.createRemoteAccessKey(rakFile);
    }

    public void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    public String getNameserverlist() {
        return "";
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    protected String parseServiceExceptionMsg(Exception e, String defaultPrefix) {
        String parsedMessage;
        String exceptionMsg = e.getMessage();
        int index = exceptionMsg.indexOf(ServiceException.class.getName());
        if (index != -1) {
            int end = exceptionMsg.indexOf(";", index += ServiceException.class.getName().length() + 2);
            parsedMessage = end != -1 ? exceptionMsg.substring(index, end) : exceptionMsg.substring(index);
        } else {
            int end;
            if (e.getCause() != null) {
                exceptionMsg = e.getCause().getMessage();
                if (exceptionMsg == null) {
                    exceptionMsg = "";
                }
                index = exceptionMsg.indexOf(ServiceException.class.getName());
            }
            parsedMessage = index != -1 ? ((end = exceptionMsg.indexOf(";", index += ServiceException.class.getName().length() + 2)) != -1 ? exceptionMsg.substring(index, end) : exceptionMsg.substring(index)) : defaultPrefix;
        }
        return parsedMessage;
    }

    protected void setError(String errormsg) {
        this.errorcodes.add("ACCESSOR_ERROR");
        this.errors.add(errormsg);
    }

    protected void setError(String errormsg, Exception e) {
        this.errorcodes.add("ACCESSOR_ERROR");
        this.errors.add(errormsg);
        this.lastException = e;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public String getErrorString() {
        return this.errorString != null ? this.errorString : "";
    }

    public boolean hasErrors() {
        return this.getErrorString().length() > 0;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
        this.errorHandler = new ErrorHandler(errorString);
    }

    public String getInfoErrorString() {
        return this.infoErrorString != null ? this.infoErrorString : "";
    }

    public boolean hasInfoErrors() {
        return this.getInfoErrorString().length() > 0;
    }

    public void setInfoErrorString(String infoErrorString) {
        this.infoErrorString = infoErrorString;
        this.errorHandler = new ErrorHandler(infoErrorString);
    }

    public String getErrorCodeList() {
        StringBuffer value = new StringBuffer();
        for (int i = 0; i < this.errorcodes.size(); ++i) {
            value.append(";").append(this.errorcodes);
        }
        return value.toString();
    }

    public String getLastError() {
        int errors = this.errors.size() - 1;
        return errors < 0 ? "" : (String)this.errorcodes.get(errors) + ": " + (String)this.errors.get(errors);
    }

    public String getLastErrorCode() {
        int errors = this.errors.size() - 1;
        return errors < 0 ? "" : (String)this.errorcodes.get(errors);
    }

    public String getLastErrorMessage() {
        int errors = this.errors.size() - 1;
        return errors < 0 ? "" : (String)this.errors.get(errors);
    }

    public ArrayList getErrorCodeStack() {
        return this.errorcodes;
    }

    public ArrayList getErrorStack() {
        return this.errors;
    }

    public String getErrorStack(String endlinestring) {
        StringBuffer errorstack = new StringBuffer();
        int errors = this.errors.size();
        for (int i = errors - 1; i >= 0; --i) {
            errorstack.append((String)this.errorcodes.get(i)).append(":").append((String)this.errors.get(i)).append(endlinestring);
        }
        return errorstack.toString();
    }

    public void resetErrorStack() {
        this.errors = new ArrayList();
        this.errorcodes = new ArrayList();
    }

    public Throwable getLastException() {
        return this.lastException;
    }

    public static void setLocal(boolean local) {
        BaseAccessor.local = local;
    }

    public static void setJNDIPrefix(String jndiPrefix) {
        BaseAccessor.jndiPrefix = jndiPrefix.endsWith("/") ? jndiPrefix : jndiPrefix + "/";
    }

    public static void setLocalJNDIPrefix(String localJNDIPrefix) {
        BaseAccessor.localJNDIPrefix = localJNDIPrefix;
    }

    protected LocalAccessManagerLocal getLocalAccessManager() throws Exception {
        return ServiceLocator.getInstance().getLocalAccessManager();
    }

    protected RemoteAccessManager getRemoteAccessManager() throws Exception {
        if (this.remoteAccessKey == null) {
            throw new SapphireException("Remote Access Key properties not loaded correctly.");
        }
        return ServiceLocator.getRemoteAccessManager(this.remoteAccessKey);
    }

    protected void createRemoteAccessKey(String databaseid) throws SapphireException {
        try {
            if (this.remoteAccessKey == null) {
                this.remoteAccessKey = new RemoteAccessKey(databaseid);
            } else if (!this.remoteAccessKey.getProperty("sapphire.databaseid").equals(databaseid)) {
                throw new SapphireException(databaseid + " does not match the database in your Remote Access Key.");
            }
        }
        catch (SapphireException e) {
            this.remoteAccessKey = null;
            e.printStackTrace();
            throw e;
        }
    }

    private void createRemoteAccessKey(File rakFile) throws SapphireException {
        try {
            this.remoteAccessKey = new RemoteAccessKey(rakFile);
        }
        catch (SapphireException e) {
            this.remoteAccessKey = null;
            e.printStackTrace();
            throw e;
        }
    }

    protected AutomationManagerLocal getAutomationManager() throws Exception {
        return ServiceLocator.getInstance().getAutomationManager();
    }

    protected AttachmentManagerLocal getAttachmentManager() throws Exception {
        return ServiceLocator.getInstance().getAttachmentManager();
    }

    protected ConfigurationManagerLocal getConfigurationManager() throws Exception {
        return ServiceLocator.getInstance().getConfigurationManager();
    }

    protected QueryManagerLocal getQueryManager() throws Exception {
        return ServiceLocator.getInstance().getQueryManager();
    }

    protected RequestManagerLocal getRequestManager() throws Exception {
        return ServiceLocator.getInstance().getRequestManager();
    }

    protected StatusManagerLocal getStatusManager() throws Exception {
        return ServiceLocator.getInstance().getStatusManager();
    }

    protected WebAdminManagerLocal getWebAdminManager() throws Exception {
        return ServiceLocator.getInstance().getWebAdminManager();
    }
}

