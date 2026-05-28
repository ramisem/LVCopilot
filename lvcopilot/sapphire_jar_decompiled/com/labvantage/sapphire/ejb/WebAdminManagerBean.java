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
import com.labvantage.sapphire.ejb.WebAdminManagement;
import com.labvantage.sapphire.services.RequestService;
import com.labvantage.sapphire.services.WebAdminService;
import java.util.ArrayList;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.xml.PropertyList;

public class WebAdminManagerBean
extends BaseManager
implements SessionBean,
WebAdminManagement {
    public WebAdminManagerBean() {
        this.logName = "WebAdminManager";
    }

    @Override
    public String getPropertyTreeDef(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "getPropertyTreeDef";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            String string = webAdminService.getPropertyTreeDef(propertytreeid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get propertytree definition for propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setPropertyTreeDef(String connectionid, String propertytreeid, String definition) throws ManagerException {
        String methodName = "setPropertyTreeDef";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setPropertyTreeDef(propertytreeid, definition);
        }
        catch (Exception e) {
            this.logError("Failed to set propertytree definition for propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getPropertyTreeValue(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "getPropertyTreeValue";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            String string = webAdminService.getPropertyTreeValue(propertytreeid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get propertytree value tree for propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setPropertyTreeValue(String connectionid, String propertytreeid, String propertytreevalue) throws ManagerException {
        String methodName = "setPropertyTreeValue";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setPropertyTreeValue(propertytreeid, propertytreevalue);
        }
        catch (Exception e) {
            this.logError("Failed to set propertytree value for propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getPropertyTreeObject(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "getPropertyTreeObject";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            String string = webAdminService.getPropertyTreeObject(propertytreeid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get propertytree object for propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void savePropertyTree(String connectionid, String propertytreeid, String propertytreevalue, String nodeid, String[] deletePropertyList, String[] renameFrom, String[] renameTo) throws ManagerException {
        String methodName = "savePropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.savePropertyTree(propertytreeid, propertytreevalue, nodeid, deletePropertyList, renameFrom, renameTo);
        }
        catch (Exception e) {
            this.logError("Failed to save propertytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getPageValueTree(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid) throws ManagerException {
        String methodName = "getPageValueTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.getPageValueTree(webpageid, productedition, propertytreeid, elementid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get page value tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getPageProductValueTree(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid) throws ManagerException {
        String methodName = "getPageProductValueTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            String string = requestService.getPageProductValueTree(webpageid, productedition, propertytreeid, elementid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get page proudctvalue tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setPageValueTree(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid, String valuetree, ArrayList deleteList) throws ManagerException {
        String methodName = "setPageValueTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setPageValueTree(webpageid, productedition, propertytreeid, elementid, valuetree, deleteList);
        }
        catch (Exception e) {
            this.logError("Failed to set page value tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean isProductPage(String connectionid, String webpageid, String productedition) throws ManagerException {
        String methodName = "setPageValueTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            boolean bl = webAdminService.isProductPage(webpageid, productedition);
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to set page value tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setUserOverrides(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid, PropertyList valuetree) throws ManagerException {
        String methodName = "setUserOverrides";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setUserOverrides(webpageid, productedition, propertytreeid, elementid, valuetree);
        }
        catch (Exception e) {
            this.logError("Failed to save user overrides for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void clearUserOverrides(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid) throws ManagerException {
        String methodName = "clearUserOverrides";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.clearUserOverrides(webpageid, productedition, propertytreeid, elementid);
        }
        catch (Exception e) {
            this.logError("Failed to clear user overrides for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void addPropertyTree(String connectionid, String propertytreeid, String propertytreetype, String propertytreedesc, String objectname) throws ManagerException {
        String methodName = "addPropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid + ";" + propertytreetype + ";" + propertytreedesc + ";" + objectname);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.addPropertyTree(propertytreeid, propertytreetype, propertytreedesc, objectname);
        }
        catch (Exception e) {
            this.logError("Failed to add property tree '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void editPropertyTree(String connectionid, String propertytreeid, String propertytreetype, String propertytreedesc, String objectname) throws ManagerException {
        String methodName = "editPropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid + ";" + propertytreetype + ";" + propertytreedesc + ";" + objectname);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.editPropertyTree(propertytreeid, propertytreetype, propertytreedesc, objectname);
        }
        catch (Exception e) {
            this.logError("Failed to edit property tree '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setPropertyTreeCategories(String connectionid, String propertytreeid, String categorylist) throws ManagerException {
        String methodName = "setPropertyTreeCategories";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid + ";" + categorylist);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setPropertyTreeCategories(propertytreeid, categorylist);
        }
        catch (Exception e) {
            this.logError("Failed to set property tree categories for properytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setPropertyTreeRoles(String connectionid, String propertytreeid, String rolelist) throws ManagerException {
        String methodName = "setPropertyTreeRoles";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid + ";" + rolelist);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setPropertyTreeRoles(propertytreeid, rolelist);
        }
        catch (Exception e) {
            this.logError("Failed to set property tree roles for properytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setWebPage(String connectionid, String webpageid, String productedition, String webpagedesc, String jspfile, String virtualpageflag, String expresspageflag, String extendwebpageid, String extendproductedition) throws ManagerException {
        String methodName = "setWebPage";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + webpagedesc + ";" + jspfile + ";" + virtualpageflag + ";" + expresspageflag);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setWebPage(webpageid, productedition, webpagedesc, jspfile, virtualpageflag, expresspageflag, extendwebpageid, extendproductedition);
        }
        catch (Exception e) {
            this.logError("Failed to set web page '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setWebPageDefaultFlag(String connectionid, String webpageid, String defaultedition) throws ManagerException {
        String methodName = "setWebPage";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + defaultedition);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setWebPageDefaultFlag(webpageid, defaultedition);
        }
        catch (Exception e) {
            this.logError("Failed to set web page '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setWebPageProperties(String connectionid, String webpageid, String productedition, PropertyList propertylist) throws ManagerException {
        String methodName = "setWebPageProperties";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setWebPageProperties(webpageid, productedition, propertylist);
        }
        catch (Exception e) {
            this.logError("Failed to set web page properties for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setWebPageCategories(String connectionid, String webpageid, String categorylist) throws ManagerException {
        String methodName = "setWebPageCategories";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + categorylist);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setWebPageCategories(webpageid, categorylist);
        }
        catch (Exception e) {
            this.logError("Failed to set web page categories for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setWebPageRoles(String connectionid, String webpageid, String rolelist) throws ManagerException {
        String methodName = "setWebPageRoles";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + rolelist);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setWebPageRoles(webpageid, rolelist);
        }
        catch (Exception e) {
            this.logError("Failed to set web page roles for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void addWebPagePropertyTree(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid, String extendnodeid, int sequence) throws ManagerException {
        String methodName = "addWebPagePropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid + ";" + extendnodeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.addWebPagePropertyTree(webpageid, productedition, propertytreeid, elementid, extendnodeid, sequence);
        }
        catch (Exception e) {
            this.logError("Failed to add web page property tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void deleteWebPagePropertyTree(String connectionid, String webpageid, String productedition, String elementid, String propertytreeid) throws ManagerException {
        String methodName = "deleteWebPagePropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + propertytreeid + ";" + elementid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.deleteWebPagePropertyTree(webpageid, productedition, elementid, propertytreeid);
        }
        catch (Exception e) {
            this.logError("Failed to delete web page property tree for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void renameWebPagePropertyTreeNode(String connectionid, String propertytreeid, String oldnodeid, String newnodeid) throws ManagerException {
        String methodName = "renameWebPagePropertyTreeNode";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid + ";" + oldnodeid + ";" + newnodeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.renameWebPagePropertyTreeNode(propertytreeid, oldnodeid, newnodeid);
        }
        catch (Exception e) {
            this.logError("Failed to rename web page property tree node for properytreeid '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void moveWebPagePropertyTreePage(String connectionid, String webpageid, String productedition, String elementid, String propertytreeid, String newnodeid) throws ManagerException {
        String methodName = "moveWebPagePropertyTreePage";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + elementid + ";" + propertytreeid + ";" + newnodeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.moveWebPagePropertyTreePage(webpageid, productedition, elementid, propertytreeid, newnodeid);
        }
        catch (Exception e) {
            this.logError("Failed to move web page property tree page for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void setComponentPageOverride(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid, String compCode, String properties) throws ManagerException {
        String methodName = "setComponentPageOverride";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + elementid + ";" + propertytreeid + ";" + compCode);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.setComponentPageOverride(webpageid, productedition, propertytreeid, elementid, compCode, properties);
        }
        catch (Exception e) {
            this.logError("Failed to add component " + compCode + " page override webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void removeComponentPageOverride(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid, String compCode) throws ManagerException {
        String methodName = "removeComponentPageOverride";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + elementid + ";" + propertytreeid + ";" + compCode);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.removeComponentPageOverride(webpageid, productedition, propertytreeid, elementid, compCode);
        }
        catch (Exception e) {
            this.logError("Failed to remove component " + compCode + " page override webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getComponentPageOverrides(String connectionid, String webpageid, String productedition, String propertytreeid, String elementid) throws ManagerException {
        String methodName = "getComponentPageOverrides";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, webpageid + ";" + productedition + ";" + elementid + ";" + propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            String string = webAdminService.getComponentPageOverrides(webpageid, productedition, propertytreeid, elementid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to move web page property tree page for webpageid '" + webpageid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void deletePropertyTree(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "deletePropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.deletePropertyTree(propertytreeid);
        }
        catch (Exception e) {
            this.logError("Failed to delete property tree '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void synchronizePropertyTree(String connectionid, String propertytreeid) throws ManagerException {
        String methodName = "synchronizePropertyTree";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, propertytreeid);
            this.startMethod(methodName, connectionid);
            WebAdminService webAdminService = new WebAdminService(this.sapphireConnection);
            webAdminService.synchronizePropertyTree(propertytreeid);
        }
        catch (Exception e) {
            this.logError("Failed to synchronize property tree '" + propertytreeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

