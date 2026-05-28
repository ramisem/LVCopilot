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
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.RequestManagement;
import com.labvantage.sapphire.services.RequestService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class RequestManagerBean
extends BaseManager
implements SessionBean,
RequestManagement {
    public RequestManagerBean() {
        this.logName = "RequestManager";
    }

    @Override
    public PropertyList getConnectionProperties(String connectionid) throws ManagerException {
        String methodName = "getConnectionProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, connectionid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getConnectionProperties();
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get connection properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getWebPageProperties(String connectionid, String webpageid, PropertyList requestProps) throws ManagerException {
        String methodName = "getWebPageProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getWebPageProperties(webpageid, requestProps);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get web page properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getWebPageProperties(String connectionid, String webpageid, String productedition, PropertyList requestProps, boolean filterProperties) throws ManagerException {
        String methodName = "getWebPageProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getWebPageProperties(webpageid, productedition, requestProps, filterProperties);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get web page properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getDefaultWebPageEdition(String connectionid, String webpageid) throws ManagerException {
        String methodName = "getDefaultWebPageEdition";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.getDefaultWebPageEdition(webpageid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get default web page edition", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getBulletinProperties(String connectionid, String webpageid, String bulletin, PropertyList requestProps) throws ManagerException {
        String methodName = "getBulletinProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + bulletin);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getBulletinProperties(webpageid, bulletin, requestProps);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get bulletin properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getHistoryProperties(String connectionid, String history, PropertyList requestProps) throws ManagerException {
        String methodName = "getHistoryProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, history);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getHistoryProperties(history, requestProps);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get history properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getFavoriteProperties(String connectionid, String favorite, PropertyList requestProps) throws ManagerException {
        String methodName = "getFavoriteProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, favorite);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.getFavoriteProperties(favorite, requestProps);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get favorite properties", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList addPropertyData(String connectionid, PropertyList requestProps) throws ManagerException {
        String methodName = "addPropertyData";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList propertyList = requestService.addPropertyData(requestProps);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get add property data", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public HashMap processRequest(String connectionid, String requestHandler, HashMap requestProps) throws ManagerException {
        String methodName = "processRequest";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, requestHandler);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            HashMap returnProps = requestService.processRequest(requestHandler, requestProps);
            ErrorHandler errorHandler = (ErrorHandler)returnProps.get("ERRORHANDLER");
            if (errorHandler != null && errorHandler.hasErrors()) {
                this.sessionContext.setRollbackOnly();
            }
            HashMap hashMap = returnProps;
            return hashMap;
        }
        catch (Exception e) {
            this.logError("Failed to process request", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void logPageAccess(String connectionid, String request, String title, String tip, HashMap requestProps) throws ManagerException {
        String methodName = "logPageAccess";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, request + ";" + title);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            requestService.logPageAccess(request, title, tip, requestProps);
        }
        catch (Exception e) {
            this.logError("Failed to log page access", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String logPageAccess(String connectionid, String request, String title, String tip, HashMap requestProps, boolean state) throws ManagerException {
        String methodName = "logPageAccess";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, request + ";" + title);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.logPageAccess(request, title, tip, requestProps, state);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to log page access", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getPropertyTreeValue(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "getPropertyTreeValue";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.getPropertyTreeValueCached(propertytreeid);
            return string;
        }
        catch (Exception e) {
            String string = null;
            return string;
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Set<String> getInactiveRoleList(String connectionid) throws ManagerException {
        String methodName = "getInactiveRoleList";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            Set<String> set = requestService.getInactiveRoles();
            return set;
        }
        catch (Exception e) {
            HashSet<String> hashSet = new HashSet<String>();
            return hashSet;
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String processFileCommand(String connectionid, String file) throws ManagerException {
        String methodName = "processFileCommand";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.processFileCommand(file);
            return string;
        }
        catch (Exception e) {
            throw new ManagerException(e.getMessage());
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

