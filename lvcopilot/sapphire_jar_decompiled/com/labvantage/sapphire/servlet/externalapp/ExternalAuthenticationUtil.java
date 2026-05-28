/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.externalapp;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ExternalAuthenticationUtil
implements ExternalAppConstants,
CacheNames {
    private static String systemPassword;

    public static String createTokenRequest(String authorizationCode, String reason, String hostname, String externalUserid) throws SapphireException, ExternalAppException {
        String tokenvalue;
        if (authorizationCode == null || authorizationCode.length() == 0) {
            throw new SapphireException("You must supply a authorizationcode=xxx with your request. If you do not have a authorization code, please contact your System Administrator");
        }
        reason = reason == null ? "" : reason;
        String authorizationCodeD = EncryptDecrypt.decrypt(authorizationCode);
        String connectionid = null;
        int pos = authorizationCodeD.indexOf("|");
        if (pos > 0) {
            String databaseid = authorizationCodeD.substring(0, pos);
            connectionid = SapphireService.getInternalConnectionid(databaseid);
        }
        if (connectionid == null || connectionid.length() == 0) {
            throw new ExternalAppException(401, "External App not found", "Unable to recognize authorization code " + authorizationCode + ". Please contact your System Administrator for assistance");
        }
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet externalappDS = qp.getPreparedSqlDataSet("SELECT * FROM externalapp WHERE authorizationcode=?", (Object[])new String[]{authorizationCode});
        if (externalappDS.size() == 0) {
            throw new SapphireException("Unable to authorize authorization code " + authorizationCode + ". Please contact your System Administrator for assistance");
        }
        if (!externalappDS.getValue(0, "externalappstatus").equals("Active")) {
            throw new ExternalAppException(401, "External App is not Active", "Unable to authorize authorization code " + authorizationCode + ". Application is disabled. Please contact your System Administrator for assistance");
        }
        PropertyList addToken = new PropertyList();
        addToken.setProperty("sdcid", "LV_AuthToken");
        addToken.setProperty("externalappid", externalappDS.getValue(0, "externalappid"));
        addToken.setProperty("requestreason", reason);
        if (externalUserid != null && externalUserid.length() > 0) {
            addToken.setProperty("externaluserid", externalUserid);
        }
        if (hostname != null && hostname.length() > 0) {
            addToken.setProperty("clienthostname", hostname);
        }
        try {
            ActionProcessor ap = new ActionProcessor(connectionid);
            ap.processAction("AddSDI", "1", addToken);
            String tokenid = addToken.getProperty("newkeyid1");
            DataSet tokenDS = qp.getPreparedSqlDataSet("SELECT * FROM authtoken WHERE authtokenid=?", (Object[])new String[]{tokenid});
            tokenvalue = tokenDS.getValue(0, "tokenvalue");
            if (tokenvalue.length() == 0) {
                throw new Exception("No token value found.");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Token generation failed: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionid)));
        }
        return tokenvalue;
    }

    public static boolean isTokenActive(String token) throws SapphireException {
        String connectionid;
        if (token == null || token.length() == 0) {
            throw new SapphireException("You must supply a token in order to check to see if its valid.");
        }
        try {
            connectionid = SapphireService.getInternalConnectionIdForToken(token);
        }
        catch (SapphireException e) {
            return false;
        }
        Trace.startThreadMDCByConnectionid(connectionid, "SC");
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet tokenDS = qp.getPreparedSqlDataSet("SELECT tokenstatus FROM authtoken WHERE tokenvalue=?", (Object[])new String[]{token});
        return tokenDS.getValue(0, "tokenstatus").equals("Active");
    }

    public static String getExternalConnectionId(String username, String password, String databaseid) throws SapphireException {
        String connectionid = "";
        if (username != null && password != null && databaseid != null) {
            String systemConnectionid = SapphireService.getInternalConnectionid(databaseid);
            DataSet user = new QueryProcessor(systemConnectionid).getPreparedSqlDataSet("SELECT nameduserflag,externalappid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{username});
            String usertype = user.getValue(0, "nameduserflag");
            if (usertype.equals("A")) {
                String externalappid = user.getValue(0, "externalappid");
                ConnectionProcessor cp = new ConnectionProcessor();
                HashMap<String, String> options = new HashMap<String, String>();
                options.put("tool", "SapphireController");
                options.put("deviceid", "WebService");
                options.put("externalappid", externalappid);
                connectionid = cp.getConnectionid(databaseid, username, password);
            } else {
                throw new SapphireException("The user needs to be an External App User to be used through the SapphireController.");
            }
        }
        return connectionid;
    }

    public static DataSet getCachedTokenDataSet(String tokenValue, String databaseid, QueryProcessor queryProcessor) {
        DataSet tokenDS = (DataSet)CacheUtil.get(databaseid, "AuthToken", tokenValue);
        if (tokenDS == null) {
            tokenDS = queryProcessor.getPreparedSqlDataSet("SELECT * FROM authtoken WHERE authtoken.tokenvalue=?", (Object[])new String[]{tokenValue});
            CacheUtil.put(databaseid, "AuthToken", tokenValue, tokenDS);
        }
        return tokenDS;
    }

    public static DataSet getCachedExternalAppDS(String externalappid, String databaseid, QueryProcessor queryProcessor) {
        DataSet externalAppDS = (DataSet)CacheUtil.get(databaseid, "ExternalApp", externalappid);
        if (externalAppDS == null) {
            externalAppDS = queryProcessor.getPreparedSqlDataSet("SELECT * FROM externalapp WHERE externalappid=?", (Object[])new String[]{externalappid});
            CacheUtil.put(databaseid, "ExternalApp", externalappid, externalAppDS);
        }
        return externalAppDS;
    }

    public static String getConnectionidForExternalUser(String authtokenid, String tokenValue, String databaseid, String externalappid, String externaluserid, SapphireConnection internalSapphireConnection, String tool) throws ExternalAppException {
        ConnectionProcessor vanillaConnnectionProcessor = new ConnectionProcessor();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("tool", tool);
        options.put("deviceid", "WebService");
        options.put("externalappid", externalappid);
        options.put("authtokenid", authtokenid);
        options.put("dbms", internalSapphireConnection.getDbms());
        options.put("connectiontypeflag", "S");
        options.put("jobtype", "(null)");
        options.put("ignoreexpirywarning", "true");
        String connectionid = vanillaConnnectionProcessor.getConnectionid(databaseid, externaluserid, systemPassword, options);
        CacheUtil.put(databaseid, "AuthTokenConnectionid", tokenValue + ";" + externaluserid, connectionid);
        return connectionid;
    }

    public static void validateProcessAsUser(QueryProcessor internalQP, DataSet tokenDS, String processAsUser, String processAsUserType) throws ExternalAppException {
        if (processAsUser != null && processAsUser.length() > 0) {
            String roleid = tokenDS.getValue(0, "processasroleid");
            String departmentid = tokenDS.getValue(0, "processasdepartmentid");
            if (processAsUserType.equals("R") && roleid.length() > 0) {
                int count;
                try {
                    count = internalQP.getPreparedCount("SELECT count(*) FROM sysuserrole WHERE sysuserid=? AND roleid=?", new String[]{processAsUser, roleid});
                }
                catch (SapphireException e) {
                    throw new ExternalAppException(401, "Unable to execute ProcessAs user", "User " + processAsUser + " not authorized to use this token");
                }
                if (count == 0) {
                    throw new ExternalAppException(401, "Unable to execute ProcessAs user", "User " + processAsUser + " not authorized to use this token");
                }
            } else if (processAsUserType.equals("D")) {
                int count;
                try {
                    count = internalQP.getPreparedCount("SELECT count(*) FROM departmentsysuser WHERE sysuserid=? AND departmentid=?", new String[]{processAsUser, departmentid});
                }
                catch (SapphireException e) {
                    throw new ExternalAppException(401, "Unable to execute ProcessAs user", "User " + processAsUser + " not authorized to use this token");
                }
                if (count == 0) {
                    throw new ExternalAppException(401, "Unable to execute ProcessAs user", "User " + processAsUser + " not authorized to use this token");
                }
            }
        }
    }

    public static void setSystemPassword(String systemPswd) {
        systemPassword = systemPswd;
    }

    public static boolean isToken(String tokenValue) {
        if (tokenValue == null || tokenValue.length() == 0) {
            return false;
        }
        String tokend = EncryptDecrypt.decrypt(tokenValue);
        return tokend.contains("|at;");
    }

    public static void clearTokenConnections(String authtokenid, QueryProcessor internalQP, ConnectionProcessor internalCP) {
        DataSet connections = internalQP.getPreparedSqlDataSet("SELECT connectionid FROM connection WHERE authtokenid=?", (Object[])new String[]{authtokenid});
        for (int i = 0; i < connections.size(); ++i) {
            String connectionid = connections.getValue(i, "connectionid");
            internalCP.clearConnection(connectionid);
        }
    }
}

