/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package sapphire.servlet;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public abstract class BaseExternalHandler
extends BaseCustom
implements CacheNames,
ExternalAppConstants {
    public static final String GETCONNECTIONID = "getconnectionid";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String DATABASE = "database";
    public static final String CONNECTIONID = "connectionid";
    private String externalAppid;
    private String externalUserid;
    private String processAsUserId;
    private String databaseId;

    public JSONObject processCommand(String command, JSONObject commandRequest) throws SapphireException {
        return null;
    }

    public PropertyList processCommand(String command, PropertyList commandRequest) throws SapphireException {
        return null;
    }

    public JSONObject processFileCommand(String command, Path file, JSONObject commandRequest) throws SapphireException {
        return null;
    }

    public JSONObject processFileCommand(String command, String filename, InputStream inputStream, JSONObject commandRequest) throws SapphireException {
        return null;
    }

    public File processFileDownloadCommand(String command, JSONObject commandRequest) throws SapphireException {
        return null;
    }

    public String getDatabaseId() {
        return this.databaseId;
    }

    public String getExternalAppid() {
        return this.externalAppid;
    }

    public String getExternalUserid() {
        return this.externalUserid;
    }

    public String getProcessAsUserId() {
        return this.processAsUserId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BaseExternalHandler getInstance(HttpServletRequest request, HttpServletResponse response) throws ExternalAppException {
        String connectionid = "";
        String tokenValue = BaseExternalHandler.getAuthenticationValue("token", request, response);
        if (!ExternalAuthenticationUtil.isToken(tokenValue)) {
            connectionid = BaseExternalHandler.getAuthenticationValue(CONNECTIONID, request, response);
        }
        String databaseid = "";
        String externalappid = "";
        String externaluserid = "";
        ConnectionProcessor vanillaConnnectionProcessor = new ConnectionProcessor();
        if (connectionid != null && connectionid.length() > 0) {
            if (!vanillaConnnectionProcessor.checkConnection(connectionid)) {
                throw new ExternalAppException(404, "Missing, invalid or timed out connectionid", "Connectionid + " + connectionid + " is invalid.");
            }
            SapphireConnection sapphireConnection = new ConnectionProcessor(connectionid).getSapphireConnection();
            databaseid = sapphireConnection.getDatabaseId();
            externaluserid = sapphireConnection.getSysuserId();
            externalappid = sapphireConnection.getExternalAppId();
            if (externalappid.length() == 0) {
                throw new ExternalAppException(404, "External App not found", "Unable to establish an externalappid in the deviceid for the provided connectionid.");
            }
        } else {
            if (tokenValue.length() > 0) {
                String internalConnectionid = null;
                try {
                    internalConnectionid = SapphireService.getInternalConnectionIdForToken(tokenValue);
                }
                catch (SapphireException e) {
                    throw new ExternalAppException(404, "Failed to process token", "Unable to validate token " + tokenValue);
                }
                SapphireConnection internalSapphireConnection = new ConnectionProcessor(internalConnectionid).getSapphireConnection();
                databaseid = internalSapphireConnection.getDatabaseId();
                QueryProcessor internalQP = new QueryProcessor(internalConnectionid);
                DataSet tokenDS = ExternalAuthenticationUtil.getCachedTokenDataSet(tokenValue, databaseid, internalQP);
                String authtokenid = tokenDS.getValue(0, "authtokenid");
                if (!tokenDS.getValue(0, "tokenstatus").equals("Active")) {
                    throw new ExternalAppException(401, "Failed to process token", "Token " + tokenValue + " in not active.");
                }
                Calendar expiryDate = tokenDS.getCalendar(0, "expirydt");
                if (expiryDate != null && expiryDate.before(Calendar.getInstance())) {
                    throw new ExternalAppException(401, "Token has expired", "The supplied token has expired. Please request a new or renewed token.");
                }
                externalappid = tokenDS.getValue(0, "externalappid");
                externaluserid = tokenDS.getValue(0, "externaluserid");
                String requestedProcessAsUserid = "";
                String processAsUserType = tokenDS.getValue(0, "processastypeflag", "N");
                if (!processAsUserType.equals("N")) {
                    if (processAsUserType.equals("U")) {
                        externaluserid = tokenDS.getValue(0, "processasuserid", externaluserid);
                    } else {
                        requestedProcessAsUserid = request.getParameter("processas");
                        if (requestedProcessAsUserid == null || requestedProcessAsUserid.length() == 0) {
                            requestedProcessAsUserid = request.getHeader("ProcessAs");
                        }
                        if (requestedProcessAsUserid != null && requestedProcessAsUserid.length() > 0) {
                            externaluserid = requestedProcessAsUserid;
                        }
                        ExternalAuthenticationUtil.validateProcessAsUser(internalQP, tokenDS, requestedProcessAsUserid, processAsUserType);
                    }
                }
                Class<BaseExternalHandler> clazz = BaseExternalHandler.class;
                synchronized (BaseExternalHandler.class) {
                    connectionid = (String)CacheUtil.get(databaseid, "AuthTokenConnectionid", tokenValue + ";" + externaluserid);
                    if (connectionid != null && connectionid.length() > 0 && !vanillaConnnectionProcessor.checkConnection(connectionid)) {
                        connectionid = "";
                    }
                    if (connectionid == null || connectionid.length() == 0) {
                        connectionid = ExternalAuthenticationUtil.getConnectionidForExternalUser(authtokenid, tokenValue, databaseid, externalappid, externaluserid, internalSapphireConnection, "SapphireController");
                    }
                    // ** MonitorExit[var16_18] (shouldn't be in output)
                }
            }
            throw new ExternalAppException(401, "Invalid request method", "You must supply either a connection or token credentials with each command reqeust.");
        }
        {
            BaseExternalHandler commandHandler = null;
            if (connectionid.length() > 0 && externalappid.length() > 0 && databaseid.length() > 0) {
                DataSet externalAppDS = ExternalAuthenticationUtil.getCachedExternalAppDS(externalappid, databaseid, new QueryProcessor(connectionid));
                if (!externalAppDS.getValue(0, "externalappstatus").equals("Active")) {
                    throw new ExternalAppException(401, "Failed to process token", "Token " + tokenValue + " is not valid for an active external app.");
                }
                if (!externalAppDS.getValue(0, "servletflag").equals("Y")) {
                    throw new ExternalAppException(401, "Failed to process token", "Token " + tokenValue + " is not authorized to use a SapphireController entry point.");
                }
                String servletHandlerClass = externalAppDS.getValue(0, "servlethandlerclass");
                if (servletHandlerClass.length() == 0) {
                    throw new ExternalAppException(400, "Invalid Handler class", "Token " + tokenValue + " is not mapped to a Handler Class.");
                }
                try {
                    Class<?> c = Class.forName(servletHandlerClass);
                    commandHandler = (BaseExternalHandler)c.newInstance();
                    commandHandler.setConnectionId(connectionid);
                    commandHandler.setDatabaseId(databaseid);
                    commandHandler.setExternalAppid(externalappid);
                    commandHandler.setExternalUserid(externaluserid);
                    commandHandler.setProcessAsUserId(externaluserid);
                }
                catch (Exception e) {
                    throw new ExternalAppException(400, "Invalid Handler class", "Unable to create a Serlvet Handler (" + servletHandlerClass + ")");
                }
            }
            return commandHandler;
        }
    }

    public static String getAuthenticationValue(String authenticationParam, HttpServletRequest request, HttpServletResponse response) {
        String authorization;
        String authenticationValue = request.getParameter(authenticationParam);
        if ((authenticationValue == null || authenticationValue.length() == 0) && (authorization = request.getHeader("Authorization")) != null && authorization.toLowerCase().startsWith("token ")) {
            authenticationValue = authorization.substring(6);
        }
        if (authenticationValue == null || authenticationValue.length() == 0) {
            HttpUtil httpUtil = new HttpUtil(request, response);
            authenticationValue = httpUtil.getCookieValue(authenticationParam);
        }
        return authenticationValue;
    }

    public void setExternalAppid(String externalAppid) {
        this.externalAppid = externalAppid;
    }

    public void setExternalUserid(String externalUserid) {
        this.externalUserid = externalUserid;
    }

    public void setProcessAsUserId(String processAsUserId) {
        this.processAsUserId = processAsUserId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
}

