/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.jws.WebService
 *  javax.jws.soap.SOAPBinding
 *  javax.jws.soap.SOAPBinding$ParameterStyle
 *  javax.xml.soap.SOAPException
 */
package com.labvantage.sapphire.webservices;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.webservices.messages.BaseSECMessage;
import com.labvantage.sapphire.webservices.messages.XMLMessage;
import java.util.Calendar;
import java.util.HashMap;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.soap.SOAPException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

@WebService(serviceName="SapphireBasicWS", endpointInterface="com.labvantage.sapphire.webservices.SapphireBasicWSEndpoint")
@SOAPBinding(parameterStyle=SOAPBinding.ParameterStyle.BARE)
public class SapphireBasicWS
implements ExternalAppConstants,
CacheNames {
    private static final String TOOL = "SapphireWS";
    private static final String DEVICEID = "WebService";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    protected String startSOAPMethod(String tokenOrConnectionId) throws SOAPException {
        String outConnectionId = tokenOrConnectionId;
        String internalConnectionId = tokenOrConnectionId;
        boolean isToken = ExternalAuthenticationUtil.isToken(tokenOrConnectionId);
        if (isToken) {
            try {
                internalConnectionId = SapphireService.getInternalConnectionIdForToken(tokenOrConnectionId);
            }
            catch (SapphireException e) {
                throw new SOAPException((Throwable)e);
            }
            SapphireConnection internalSapphireConnection = new ConnectionProcessor(internalConnectionId).getSapphireConnection();
            String databaseid = internalSapphireConnection.getDatabaseId();
            QueryProcessor internalQP = new QueryProcessor(internalConnectionId);
            DataSet tokenDS = ExternalAuthenticationUtil.getCachedTokenDataSet(tokenOrConnectionId, databaseid, internalQP);
            String authtokenid = tokenDS.getValue(0, "authtokenid");
            if (!tokenDS.getValue(0, "tokenstatus").equals("Active")) {
                throw new SOAPException("Provided Token " + tokenOrConnectionId + " in not active.");
            }
            Calendar expiryDate = tokenDS.getCalendar(0, "expirydt");
            if (expiryDate != null && expiryDate.before(Calendar.getInstance())) {
                throw new SOAPException("The supplied token has expired. Please request a new or renewed token.");
            }
            String externalappid = tokenDS.getValue(0, "externalappid");
            if (externalappid.length() <= 0) throw new SOAPException("No External App for SOAP Webservices provided");
            DataSet externalAppDS = ExternalAuthenticationUtil.getCachedExternalAppDS(externalappid, databaseid, internalQP);
            if (externalAppDS.size() <= 0) throw new SOAPException("External App " + externalappid + " could not be authenticated to use SOAP Webservices");
            if (externalAppDS.getValue(0, "soapflag", "N").equals("N")) {
                throw new SOAPException("External App " + externalappid + " is not authenticated to use SOAP Webservices");
            }
            String externaluserid = tokenDS.getValue(0, "externaluserid");
            String requestedProcessAsUserid = "";
            String processAsUserType = tokenDS.getValue(0, "processastypeflag", "N");
            if (processAsUserType.equals("U")) {
                externaluserid = tokenDS.getValue(0, "processasuserid", externaluserid);
            } else {
                outConnectionId = internalConnectionId;
            }
            Class<SapphireBasicWS> clazz = SapphireBasicWS.class;
            // MONITORENTER : com.labvantage.sapphire.webservices.SapphireBasicWS.class
            ConnectionProcessor vanillaConnnectionProcessor = new ConnectionProcessor();
            String connectionid = (String)CacheUtil.get(databaseid, "AuthTokenConnectionid", tokenOrConnectionId + ";" + externaluserid);
            if (connectionid != null && connectionid.length() > 0 && !vanillaConnnectionProcessor.checkConnection(connectionid)) {
                connectionid = "";
            }
            if (connectionid == null || connectionid.length() == 0) {
                try {
                    connectionid = ExternalAuthenticationUtil.getConnectionidForExternalUser(authtokenid, tokenOrConnectionId, databaseid, externalappid, externaluserid, internalSapphireConnection, TOOL);
                }
                catch (Exception e) {
                    throw new SOAPException("Unable to authenticate.", (Throwable)e);
                }
            }
            outConnectionId = connectionid;
            // MONITOREXIT : clazz
        }
        Trace.startThreadMDCByConnectionid(outConnectionId, "SOAP");
        SecurityPolicyUtil.checkWebServicesEnabled(internalConnectionId, isToken);
        return outConnectionId;
    }

    public String getVersion() {
        Trace.startThreadMDCBlank("SOAP");
        return "V" + Build.getVersion() + " (Build " + Build.getBuild() + " Patch " + Build.getPatch() + ")";
    }

    public String getConnectionId(String databaseid, String userid, String password) throws SOAPException {
        Trace.startThreadMDCByDatabaseid(databaseid, "SOAP");
        try {
            SecurityPolicyUtil.isWebServicesEnabled(databaseid);
            ConnectionProcessor cp = new ConnectionProcessor();
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("tool", TOOL);
            options.put("deviceid", DEVICEID);
            String connectionid = cp.getConnectionid(databaseid, userid, password, options);
            if (connectionid.length() > 0) {
                String string = connectionid;
                return string;
            }
            try {
                throw new SOAPException(cp.getLastErrorMessage());
            }
            catch (Exception e) {
                throw new SOAPException("Failed to get connectionid. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    public void clearConnection(String connectionid) throws SOAPException {
        Trace.startThreadMDCByConnectionid(connectionid, "SOAP");
        try {
            SecurityPolicyUtil.checkWebServicesEnabled(connectionid);
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            cp.clearConnection(connectionid);
        }
        catch (Exception e) {
            throw new SOAPException("Failed to clear connection '" + connectionid + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    public String processAction(String connectionid, String actionid, String actionversionid, String propertyListXML) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            if (propertyListXML != null && propertyListXML.length() > 0) {
                PropertyList propertyList = new PropertyList();
                propertyList.setPropertyList(propertyListXML);
                HashMap props = new HashMap(propertyList);
                HashMap retProps = this.processAction(connectionid, actionid, actionversionid, props);
                String string = retProps != null ? new PropertyList(retProps).toXMLString() : "";
                return string;
            }
            try {
                throw new SOAPException("No property XML provided.");
            }
            catch (SapphireException e) {
                throw new SOAPException("Failed to process action '" + actionid + "'. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected HashMap processAction(String connectionid, String actionid, String actionversionid, HashMap propertyMap) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            block8: {
                if (!SecurityPolicyUtil.isActionPermitted(connectionid, "webservices", "actionprocessing", actionid, propertyMap)) throw new SOAPException("Failed to process action '" + actionid + "'. Reason: Action execution not permitted by security policy.");
                if (connectionid == null || connectionid.length() <= 0) throw new SOAPException("No connection id provided.");
                if (actionid == null || actionversionid == null || actionid.length() <= 0 || actionversionid.length() <= 0) break block8;
                ActionProcessor ap = new ActionProcessor(connectionid);
                ap.processAction(actionid, actionversionid, propertyMap);
                HashMap hashMap = propertyMap;
                return hashMap;
            }
            try {
                throw new SOAPException("No action id and/or action version provided.");
            }
            catch (ActionException e) {
                ErrorHandler errorHandler = e.getErrorHandler();
                if (errorHandler == null || errorHandler.size() <= 0) throw new SOAPException("Failed to process action '" + actionid + "'. Reason: " + e.getMessage(), (Throwable)e);
                ErrorDetail error = (ErrorDetail)errorHandler.get(errorHandler.size() - 1);
                throw new SOAPException(error.getMessage(), (Throwable)e);
            }
            catch (Exception e) {
                throw new SOAPException("Failed to process action '" + actionid + "'. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    public String getSqlDataSet(String connectionid, String sql) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid, "webservices", "getSqlDataSet", sql)) {
                QueryProcessor qp;
                block8: {
                    try {
                        qp = new QueryProcessor(connectionid);
                        DataSet ds = qp.getSqlDataSet(sql);
                        if (ds == null) break block8;
                        String string = ds.toXML();
                        return string;
                    }
                    catch (Exception e) {
                        throw new SOAPException("Failed to getSqlDataSet with sql '" + sql + "'. Reason: " + e.getMessage(), (Throwable)e);
                    }
                }
                throw new SOAPException(qp.getLastErrorMessage());
            }
            throw new SOAPException("Failed to perform web service request. Reason: getSqlDataSet using unregistered SQL disabled in security policy");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean checkConnection(String connectionid) throws SOAPException {
        Trace.startThreadMDCByConnectionid(connectionid, "SOAP");
        try {
            boolean ok;
            SecurityPolicyUtil.checkWebServicesEnabled(connectionid);
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            boolean bl = ok = cp.checkConnection(connectionid);
            return bl;
        }
        catch (Exception e) {
            boolean bl = false;
            return bl;
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    public String getPublicKey() throws SOAPException {
        Trace.startThreadMDCBlank("SOAP");
        try {
            String string = EncryptDecrypt.getPublicKey();
            return string;
        }
        catch (Exception e) {
            throw new SOAPException("Failed to obtain key. Reason: " + e.getMessage(), (Throwable)e);
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    public int getSequence(String connectionid, String sdcid, String sequenceid, int startsequencenumber, int incrementby) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            if (connectionid != null && connectionid.length() > 0) {
                try {
                    int seq;
                    SequenceProcessor sp = new SequenceProcessor(connectionid);
                    int n = seq = sp.getSequence(sdcid, sequenceid, startsequencenumber, incrementby);
                    return n;
                }
                catch (Exception e) {
                    throw new SOAPException(e.getMessage());
                }
            }
            throw new SOAPException("No connection id provided.");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected BaseSECMessage processMessage(String connectionid, BaseSECMessage message, String processingMode) throws SOAPException {
        try {
            Trace.startThreadMDCByConnectionid(connectionid, "SOAP");
            PropertyList props = new PropertyList();
            props.setProperty("message", message.toMessage());
            props.setProperty("messagetag", message.getId());
            props.setProperty("messagetypeid", message.getTypeId());
            if (processingMode.equalsIgnoreCase("A") || processingMode.toLowerCase().startsWith("async")) {
                props.setProperty("processactionmode", "A");
            } else if (processingMode.equalsIgnoreCase("M") || processingMode.toLowerCase().startsWith("man")) {
                props.setProperty("processactionmode", "M");
            } else {
                props.setProperty("processactionmode", "S");
            }
            this.processAction(connectionid, "ProcessInMessage", "1", props);
            String res = props.getProperty("responsemessage", "");
            String stat = props.getProperty("status", "");
            String err = props.getProperty("error", "");
            String log = props.getProperty("log", "");
            if (res.length() > 0) {
                message.fromMessage(res);
            }
            message.setStatus(stat);
            message.setError(err);
            message.setLog(log);
            BaseSECMessage baseSECMessage = message;
            return baseSECMessage;
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String processMessage(String connectionid, String id, String typeId, String message, String processingMode) throws SOAPException {
        try {
            connectionid = this.startSOAPMethod(connectionid);
            XMLMessage secmessage = new XMLMessage();
            secmessage.setTypeId(typeId);
            secmessage.setId(id);
            secmessage.fromMessage(message);
            BaseSECMessage outMsg = this.processMessage(connectionid, secmessage, processingMode);
            String string = outMsg.toMessage();
            return string;
        }
        finally {
            Trace.clearThreadMDC();
        }
    }
}

