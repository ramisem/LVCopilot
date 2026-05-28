/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.actions.storageunit.SyncStorageLastNodeFlag;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.RequestService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.filter.RejectRequest;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.util.Browser;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WebAdminService
extends BaseService
implements CacheNames {
    public static final String LOGNAME = "WebAdminService";
    public static final String CACHE_WEB_PAGE_DESIGNER = "WebPageDesigner";
    public static final String CACHE_PAGE_TREE_ELEMENT = "PageTreeElement";
    public static final String CACHE_PAGE_ACCESS = "PageAccess";
    public static final String CACHE_PAGE = "Page";
    public static final String CACHE_TREE = "Tree";
    public static final int TYPE_PRODUCTVALUETREE = 0;
    public static final int TYPE_COMPONENTVALUETREE = 1;
    public static final int TYPE_VALUETREE = 2;

    public WebAdminService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public String getPropertyTreeDef(String propertytreeid) throws ServiceException {
        this.logDebug("Getting property tree definition for propertytreeid '" + propertytreeid + "'");
        String definition = null;
        String definitonFolder = ConfigService.getConfigProperty("com.labvantage.sapphire.server.propertytreedefinitionfolder");
        if (definitonFolder != null && definitonFolder.length() > 0) {
            try {
                File file = new File(definitonFolder + "/" + propertytreeid + ".xml");
                definition = PropertyTreeUtil.getPropertyTreeDefinition(file, propertytreeid, true);
            }
            catch (SapphireException e) {
                this.logError("Unable to load propertyrtree definition from " + definitonFolder + "/" + propertytreeid + ". Trying to read from database", e);
            }
        }
        if (definition == null || definition.length() == 0) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                definition = PropertyTreeUtil.getPropertyTreeDefinition(db, propertytreeid, true);
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the definitiontree for propertytreeid '" + propertytreeid + "'", se);
            }
            finally {
                db.reset();
            }
        }
        return definition;
    }

    public void setPropertyTreeDef(String propertytreeid, String definition) throws ServiceException {
        this.logInfo("Setting property tree definition for propertytreeid '" + propertytreeid + "'");
        definition = StringUtil.replaceAll(definition, " && ", " AND ");
        definition = StringUtil.replaceAll(definition, "&&", " AND ");
        definition = StringUtil.replaceAll(definition, " || ", " OR ");
        definition = StringUtil.replaceAll(definition, "||", " OR ");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String definitonFolder = ConfigService.getConfigProperty("com.labvantage.sapphire.server.propertytreedefinitionfolder");
            if (definitonFolder != null && definitonFolder.length() > 0) {
                try {
                    File file = new File(definitonFolder + "/" + propertytreeid + ".xml");
                    if (file.exists()) {
                        PropertyTreeUtil.setPropertyTreeDefinition(file, propertytreeid, definition);
                    }
                }
                catch (SapphireException e) {
                    throw new ServiceException("DB_ACTION_FAILED", "Unable to save changes: " + e.getMessage(), e);
                }
            }
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String sql = "UPDATE propertytree SET definitiontree = ? , modby = ?, moddt = ? WHERE propertytreeid = ? ";
            PreparedStatement ps = db.getConnection().prepareStatement(sql);
            ps.setCharacterStream(1, (Reader)new StringReader(definition), definition.length());
            ps.setString(2, this.connectionInfo.getSysuserId());
            ps.setTimestamp(3, now);
            ps.setString(4, propertytreeid);
            ps.executeUpdate();
            ps.close();
            this.resetCache(CACHE_TREE, propertytreeid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not set property tree def: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public String getPropertyTreeValue(String propertytreeid) throws ServiceException {
        this.logDebug("Getting property tree value tree for propertytreeid '" + propertytreeid + "'");
        try {
            RequestService requestService = new RequestService(this.sapphireConnection);
            return requestService.getPropertyTreeValue(propertytreeid);
        }
        catch (ServiceException e) {
            throw new ServiceException("REQUEST_SERVICE_ERROR", "Failed to get propertytree value tree", e);
        }
    }

    public String getPropertyTreeObject(String propertytreeid) throws ServiceException {
        this.logDebug("Getting property tree object for propertytreeid '" + propertytreeid + "'");
        try {
            RequestService requestService = new RequestService(this.sapphireConnection);
            return requestService.getPropertyTreeObject(propertytreeid);
        }
        catch (ServiceException e) {
            throw new ServiceException("REQUEST_SERVICE_ERROR", "Failed to get propertytree object");
        }
    }

    public void savePropertyTree(String propertytreeid, String propertytreevalue, String nodeid, String[] deletePropertyList, String[] renameFrom, String[] renameTo) throws ServiceException {
        this.logInfo("Saving propertytreeid '" + propertytreeid + "'");
        if (propertytreevalue != null && propertytreevalue.length() > 0) {
            this.setPropertyTreeValue(propertytreeid, propertytreevalue);
        }
        if (deletePropertyList.length > 0 || renameFrom.length > 0) {
            try {
                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                db.setConnection(this.sapphireConnection);
                Document doc = DOMUtil.getNewDocument(this.getPropertyTreeValue(propertytreeid));
                Element node = DOMUtil.findNode(doc, nodeid);
                ArrayList allNodes = DOMUtil.getAllNodes(node);
                allNodes.add(node.getAttribute("id"));
                for (String nextNodeid : allNodes) {
                    this.resolveWebPagePropertyListids(db, propertytreeid, nextNodeid, deletePropertyList, renameFrom, renameTo);
                }
            }
            catch (Exception e) {
                this.logger.stackTrace(e);
            }
        }
    }

    public void setPropertyTreeValue(String propertytreeid, String propertytreevalue) throws ServiceException {
        this.logInfo("Setting property tree value tree for propertytreeid '" + propertytreeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String propertytreetype = PropertyTreeUtil.getPropertyTreeType(db, propertytreeid);
            if (("Policy".equals(propertytreetype) || "Password Validator".equals(propertytreetype) || "FileRepository".equals(propertytreetype) || "Authentication".equals(propertytreetype)) && !this.sapphireConnection.hasRole("Administrator") && !this.sapphireConnection.hasRole("WebPage_Admin") && !this.sapphireConnection.hasModule("Security")) {
                throw new ServiceException("Not Authorized!");
            }
            PropertyTreeUtil.setPropertyTreeValue(db, this.connectionInfo.getSysuserId(), propertytreeid, propertytreevalue);
            this.resetCache(CACHE_TREE, propertytreeid);
            db.createPreparedResultSet("SELECT webpageid FROM webpagepropertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
            StringBuffer pageList = new StringBuffer();
            while (db.getNext()) {
                pageList.append(";").append(db.getString("webpageid"));
            }
            db.closeResultSet();
            if (pageList.length() > 0) {
                this.resetCache(CACHE_PAGE, pageList.substring(1));
            }
            if (propertytreetype.equalsIgnoreCase("gizmo")) {
                this.resetCache("Gizmo", propertytreeid);
                db.createPreparedResultSet("SELECT gizmodefid FROM gizmodef WHERE propertytreeid = ?", new Object[]{propertytreeid});
                StringBuffer gizmoList = new StringBuffer();
                while (db.getNext()) {
                    gizmoList.append(";").append(db.getString("gizmodefid"));
                }
                db.closeResultSet();
                if (gizmoList.length() > 0) {
                    this.resetCache("GizmoDef", gizmoList.substring(1));
                }
            } else if (propertytreetype.equalsIgnoreCase("step")) {
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepProps");
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepTypeProps");
            }
            if (propertytreeid.equals("SecurityPolicy")) {
                SecurityPolicyUtil.setDatabaseSecurityPolicy(this.sapphireConnection.getConnectionId(), this.sapphireConnection.getDatabaseId());
                BaseNameSpaceHandler.setDatabaseRESTPolicy(this.sapphireConnection.getConnectionId(), this.sapphireConnection.getDatabaseId());
                RejectRequest.resetDatabaseFilterOptions(this.sapphireConnection.getDatabaseId());
                NotifyManager.resetDatabaseFilterOptions(this.sapphireConnection.getDatabaseId());
                GroovyUtil.clearScriptCache();
            } else if (propertytreetype.equals("CollectorType")) {
                PropertyList checkRebootProps = new PropertyList();
                checkRebootProps.setProperty("propertytreeid", propertytreeid);
                this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
            } else if (propertytreeid.equals("RESTPolicy")) {
                BaseNameSpaceHandler.setDatabaseRESTPolicy(this.sapphireConnection.getConnectionId(), this.sapphireConnection.getDatabaseId());
            } else if (propertytreeid.equals("CMTPolicy")) {
                CMTPolicy.resetCache(this.sapphireConnection.getDatabaseId());
            } else if (propertytreeid.equals("GUIPolicy")) {
                this.resetCache("GUIModes", "");
                ArrayList<Browser.GUIMode> guiModes = Browser.getGUIModes(new ConfigurationProcessor(this.getConnectionId()));
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "GUIModes", "GUIModes", guiModes);
            } else if (propertytreeid.equals("Circular") || propertytreeid.equals("Grid") || propertytreeid.equals("Linear") || propertytreeid.equals("No Layout")) {
                PropertyList props;
                List<String> oldLastNodeList = StorageUnitTypeDef.getInstance().getLastNodeList(this.getConnectionProcessor().getSapphireConnection());
                PropertyList oldGridTypePropertyList = "Grid".equals(propertytreeid) ? StorageUnitTypeDef.getInstance().getTypePropertyList(this.getQueryProcessor(), "Grid") : new PropertyList();
                String databaseid = StorageUnitTypeDef.getInstance().refreshDefinition(this.getQueryProcessor(), true);
                boolean lastNodeListModified = false;
                List<String> newLastNodeList = StorageUnitTypeDef.getInstance().getLastNodeList(this.getConnectionProcessor().getSapphireConnection());
                if (newLastNodeList.size() != oldLastNodeList.size()) {
                    lastNodeListModified = true;
                } else {
                    for (String s : newLastNodeList) {
                        if (oldLastNodeList.contains(s)) continue;
                        lastNodeListModified = true;
                        break;
                    }
                }
                if (lastNodeListModified) {
                    props = new PropertyList();
                    props.setProperty("__resetlastnode", "Y");
                    this.getActionProcessor().processActionClass(SyncStorageLastNodeFlag.class.getName(), props);
                }
                if ("Grid".equals(propertytreeid)) {
                    props = new PropertyList();
                    props.setProperty("actionid", "SyncASLArrayType");
                    props.setProperty("actionversionid", "1");
                    props.setProperty("__oldgridtypelist", oldGridTypePropertyList.toXMLString());
                    this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props);
                }
            } else if (propertytreeid.equals("DataEntryPolicy")) {
                if (!WebAdminService.isSpecConditionInSync(this.sapphireConnection.getConnectionId(), db)) {
                    PropertyList dataentrypolicy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
                    PropertyListCollection specConditions = dataentrypolicy.getCollection("SpecConditions");
                    db.createPreparedResultSet("refvalues", "SELECT * FROM refvalue where reftypeid=? order by usersequence", new Object[]{"Spec Condition"});
                    DataSet refvalues = new DataSet(db.getResultSet("refvalues"));
                    db.closeResultSet("refvalues");
                    db.executePreparedUpdate("DELETE from refvalue WHERE reftypeid=?", new Object[]{"Spec Condition"});
                    refvalues.clear();
                    for (int i = 0; i < specConditions.size(); ++i) {
                        if (specConditions.getPropertyList(i).getProperty("SpecCond").length() <= 0 || !"Y".equals(specConditions.getPropertyList(i).getProperty("synctoreftype"))) continue;
                        db.executePreparedUpdate("INSERT INTO refvalue ( reftypeid, refvalueid, refdisplayicon, usersequence, moddt, modby, modtool ) VALUES ( 'Spec Condition', ?, ?, ?, ?, ?, ? )", new Object[]{specConditions.getPropertyList(i).getProperty("SpecCond"), specConditions.getPropertyList(i).getProperty("icon"), new BigDecimal(i), DateTimeUtil.getNowTimestamp(), this.sapphireConnection.getSysuserId(), "DataEntryPolicy"});
                    }
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "isSpecConditionInSync", "isSpecConditionInSync", "Y");
                } else {
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "isSpecConditionInSync", "isSpecConditionInSync", "Y");
                }
            }
            if (propertytreeid != null && propertytreeid.indexOf("Policy") > 0) {
                GroovyPolicyUtil.resetCache();
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not set property tree value: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public static boolean isSpecConditionInSync(String connectionid, DBAccess db) {
        try {
            PropertyList dataentrypolicy = new ConfigurationProcessor(connectionid).getPolicy("DataEntryPolicy", "Sapphire Custom");
            PropertyListCollection specConditions = dataentrypolicy.getCollection("SpecConditions");
            db.createPreparedResultSet("refvalues", "SELECT * FROM refvalue where reftypeid=? order by usersequence", new Object[]{"Spec Condition"});
            DataSet refvalues = new DataSet(db.getResultSet("refvalues"));
            db.closeResultSet("refvalues");
            boolean inSync = true;
            int refvalueseq = 0;
            block2: for (int i = 0; i < specConditions.size(); ++i) {
                if ("Y".equals(specConditions.getPropertyList(i).getProperty("synctoreftype"))) {
                    if (!specConditions.getPropertyList(i).getProperty("SpecCond").equals(refvalues.getValue(refvalueseq, "refvalueid")) || "N".equals(specConditions.getPropertyList(i).getProperty("synctoreftype")) || !specConditions.getPropertyList(i).getProperty("icon").equals(refvalues.getValue(refvalueseq, "refdisplayicon"))) {
                        inSync = false;
                        break;
                    }
                    ++refvalueseq;
                    continue;
                }
                if (!"N".equals(specConditions.getPropertyList(i).getProperty("synctoreftype"))) continue;
                String NoSynccondition = specConditions.getPropertyList(i).getProperty("SpecCond");
                for (int r = 0; r < refvalues.getRowCount(); ++r) {
                    if (!NoSynccondition.equals(refvalues.getValue(refvalueseq, "refvalueid"))) continue;
                    inSync = false;
                    break block2;
                }
            }
            return inSync;
        }
        catch (SapphireException se) {
            Trace.logError("Problem sync spec condition from policy to reftype", se);
            return false;
        }
    }

    private void resolveWebPagePropertyListids(DBUtil db, String propertytreeid, String nodeid, String[] delete, String[] renameFrom, String[] renameTo) throws SapphireException {
        db.createPreparedResultSet("SELECT webpageid, productedition, elementid FROM webpagepropertytree WHERE propertytreeid = ? AND extendnodeid = ?", new Object[]{propertytreeid, nodeid});
        while (db.getNext()) {
            String webpageid = null;
            try {
                webpageid = db.getString("webpageid");
                String elementid = db.getString("elementid");
                String productedition = db.getString("productedition");
                RequestService requestService = new RequestService(this.sapphireConnection);
                String valuetree = requestService.getPageValueTree(webpageid, productedition, propertytreeid, elementid);
                this.resolveValueTree(valuetree, delete, renameFrom, renameTo, webpageid, productedition, propertytreeid, elementid, 2);
                String componentOverrides = this.getComponentPageOverrides(webpageid, productedition, propertytreeid, elementid);
                if (componentOverrides != null && componentOverrides.length() > 0) {
                    this.resolveValueTree(componentOverrides, delete, renameFrom, renameTo, webpageid, productedition, propertytreeid, elementid, 1);
                }
                String productvaluetree = requestService.getPageProductValueTree(webpageid, productedition, propertytreeid, elementid);
                this.resolveValueTree(productvaluetree, delete, renameFrom, renameTo, webpageid, productedition, propertytreeid, elementid, 0);
            }
            catch (Exception e) {
                this.logError("Problem trying to remove or rename propertylist on node " + nodeid + " from page " + webpageid + ". Skipping and continueing");
            }
        }
        db.closeResultSet();
    }

    private void resolveValueTree(String valuetree, String[] delete, String[] renameFrom, String[] renameTo, String webpageid, String productedition, String propertytreeid, String elementid, int valueTreeType) throws SapphireException, TransformerException, ServiceException {
        Element pagePropertyList;
        Document doc = DOMUtil.getNewDocument(valuetree);
        if (doc != null && (pagePropertyList = doc.getDocumentElement()) != null) {
            Node propertylist;
            int j;
            NodeList list;
            int i;
            boolean changesMade = false;
            for (i = 0; i < delete.length; ++i) {
                list = XPathAPI.selectNodeList((Node)pagePropertyList, (String)("//propertylist[@id='" + delete[i] + "']"));
                for (j = 0; j < list.getLength(); ++j) {
                    propertylist = list.item(j);
                    propertylist.getParentNode().removeChild(propertylist);
                    changesMade = true;
                }
            }
            if (renameFrom != null) {
                for (i = 0; i < renameFrom.length; ++i) {
                    list = XPathAPI.selectNodeList((Node)pagePropertyList, (String)("//propertylist[@id='" + renameFrom[i] + "']"));
                    for (j = 0; j < list.getLength(); ++j) {
                        propertylist = (Element)list.item(j);
                        propertylist.setAttribute("id", renameTo[i]);
                        changesMade = true;
                    }
                }
            }
            if (changesMade) {
                StringBuffer output = new StringBuffer();
                output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(pagePropertyList));
                this.setPageValueTree(webpageid, productedition, propertytreeid, elementid, output.toString(), false, valueTreeType, null);
            }
        }
    }

    public boolean isProductPage(String webpageid, String productedition) throws ServiceException {
        ConfigService configService = new ConfigService(this.sapphireConnection);
        boolean isDevMode = "Y".equals(configService.getSysConfigProperty("devmode"));
        String compCode = isDevMode ? "" : configService.getSysConfigProperty("compcode");
        boolean isProductPage = isDevMode;
        if (compCode.length() > 0) {
            try {
                String pageCompCode;
                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("SELECT compcode FROM webpage WHERE webpageid=? and productedition=?", new Object[]{webpageid, productedition});
                if (db.getNext() && (pageCompCode = db.getString("compcode")) != null && pageCompCode.length() > 0 && pageCompCode.equals(compCode)) {
                    isProductPage = true;
                }
                db.closeResultSet();
            }
            catch (SapphireException e) {
                throw new ServiceException("Unable to resolve page comp code", e);
            }
        }
        return isProductPage;
    }

    public void setPageValueTree(String webpageid, String productedition, String propertytreeid, String elementid, String valuetree, ArrayList deleteList) throws ServiceException {
        boolean isProductPage = this.isProductPage(webpageid, productedition);
        this.setPageValueTree(webpageid, productedition, propertytreeid, elementid, valuetree, true, isProductPage ? 0 : 2, deleteList);
    }

    private void setPageValueTree(String webpageid, String productedition, String propertytreeid, String elementid, String valuetree, boolean resetCache, int valueTreeType, ArrayList deleteList) throws ServiceException {
        block11: {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                String columnid;
                db.setConnection(this.sapphireConnection);
                Timestamp now = DateTimeUtil.getNowTimestamp();
                db.executePreparedUpdate("UPDATE webpage SET modby = ?, moddt = ? WHERE webpageid = ? AND productedition= ?", new Object[]{this.connectionInfo.getSysuserId(), now, webpageid, productedition});
                String string = valueTreeType == 2 ? "valuetree" : (columnid = valueTreeType == 0 ? "productvaluetree" : "componentvaluetree");
                if (this.connectionInfo.isOracle()) {
                    db.updateClob("webpagepropertytree", columnid, valuetree, new String[]{"webpageid", "productedition", "propertytreeid", "elementid"}, new String[]{webpageid, productedition, propertytreeid, elementid});
                } else {
                    String sql = "UPDATE webpagepropertytree SET " + columnid + "= ? WHERE webpageid = ? and productedition = ? and propertytreeid = ? and elementid=?";
                    PreparedStatement ps = db.getConnection().prepareStatement(sql);
                    ps.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                    ps.setString(2, webpageid);
                    ps.setString(3, productedition);
                    ps.setString(4, propertytreeid);
                    ps.setString(5, elementid);
                    ps.executeUpdate();
                    ps.close();
                }
                if (resetCache) {
                    this.resetCache(CACHE_PAGE, webpageid);
                    this.resetCache(CACHE_TREE, propertytreeid);
                }
                if (deleteList == null || deleteList.size() <= 0) break block11;
                try {
                    String[] deletes = deleteList.toArray(new String[0]);
                    db.createPreparedResultSet("SELECT webpagepropertytree.webpageid, webpagepropertytree.productedition, webpagepropertytree.elementid FROM webpagepropertytree, webpage WHERE webpagepropertytree.propertytreeid=? and webpagepropertytree.elementid=? AND webpage.webpageid=webpagepropertytree.webpageid AND webpage.productedition=webpagepropertytree.productedition AND webpage.extendwebpageid=? AND webpage.extendproductedition=?", new Object[]{propertytreeid, elementid, webpageid, productedition});
                    while (db.getNext()) {
                        String childwebpageid = db.getString("webpageid");
                        String childproductedition = db.getString("productedition");
                        String childelementid = db.getString("elementid");
                        RequestService requestService = new RequestService(this.sapphireConnection);
                        String childvaluetree = requestService.getPageValueTree(childwebpageid, childproductedition, propertytreeid, childelementid);
                        this.resolveValueTree(childvaluetree, deletes, null, null, childwebpageid, childproductedition, propertytreeid, childelementid, 2);
                        String childproductvaluetree = requestService.getPageProductValueTree(childwebpageid, childproductedition, propertytreeid, childelementid);
                        this.resolveValueTree(childproductvaluetree, deletes, null, null, childwebpageid, childproductedition, propertytreeid, childelementid, 0);
                    }
                }
                catch (Exception e) {
                    this.logError("Problem trying to remove propertylists child child pages. Skipping and continuing");
                }
            }
            catch (Exception e) {
                throw new ServiceException("DB_ACTION_FAILED", "Could not set page value tree: " + e.getMessage(), e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void setUserOverrides(String webpageid, String productedition, String propertytreeid, String elementid, PropertyList valuetree) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sysuserid = this.sapphireConnection.getSysuserId();
            if (valuetree.size() == 0) {
                db.executePreparedUpdate("DELETE FROM webpageuseroverride WHERE sysuserid=? AND webpageid=? AND productedition=? AND propertytreeid=? AND elementid=?", new String[]{sysuserid, webpageid, productedition, propertytreeid, elementid});
            } else {
                db.createPreparedResultSet("SELECT 1 FROM webpageuseroverride WHERE sysuserid=? AND webpageid=? AND productedition=? AND propertytreeid=? and elementid=?", new String[]{sysuserid, webpageid, productedition, propertytreeid, elementid});
                if (!db.getNext()) {
                    db.executePreparedUpdate("INSERT INTO webpageuseroverride (sysuserid, webpageid, productedition, propertytreeid, elementid) values (?,?,?,?,?)", new String[]{sysuserid, webpageid, productedition, propertytreeid, elementid});
                }
                db.updateClob("webpageuseroverride", "valuetree", valuetree.toXMLString(), new String[]{"sysuserid", "webpageid", "productedition", "propertytreeid", "elementid"}, new String[]{sysuserid, webpageid, productedition, propertytreeid, elementid});
            }
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "WebPageUserOverrides");
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not save user overrides: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void clearUserOverrides(String webpageid, String productedition, String propertytreeid, String elementid) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sysuserid = this.sapphireConnection.getSysuserId();
            if (propertytreeid != null && propertytreeid.length() > 0 && elementid != null && elementid.length() > 0) {
                db.executePreparedUpdate("DELETE FROM webpageuseroverride WHERE sysuserid=? AND webpageid=? AND productedition=? AND propertytreeid=? AND elementid=?", new String[]{sysuserid, webpageid, productedition, propertytreeid, elementid});
            } else if (webpageid != null && webpageid.length() > 0 && productedition != null && productedition.length() > 0) {
                db.executePreparedUpdate("DELETE FROM webpageuseroverride WHERE sysuserid=? AND webpageid=? AND productedition=?", new String[]{sysuserid, webpageid, productedition});
            } else {
                db.executePreparedUpdate("DELETE FROM webpageuseroverride WHERE sysuserid=?", new String[]{sysuserid});
            }
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "WebPageUserOverrides");
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not save user overrides: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void addPropertyTree(String propertytreeid, String propertytreetype, String propertytreedesc, String objectname) throws ServiceException {
        this.logInfo("Adding property tree '" + propertytreeid + "' of type '" + propertytreetype + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String compcode = Configuration.getCompcode(this.sapphireConnection.getDatabaseId());
            String inheritanceflag = propertytreetype.equals("WorksheetItem") ? "N" : null;
            String sql = "INSERT INTO propertytree ( propertytreeid, propertytreetype, propertytreedesc, objectname, coreflag, compcode, inheritanceflag ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
            if (db.executePreparedUpdate(sql, new String[]{propertytreeid, propertytreetype, propertytreedesc, objectname, "N", compcode, inheritanceflag}) != 1) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to insert propertytreerecord");
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add property tree: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void editPropertyTree(String propertytreeid, String propertytreetype, String propertytreedesc, String objectname) throws ServiceException {
        this.logInfo("Editing property tree '" + propertytreeid + "' type '" + propertytreetype + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            SafeSQL safeSQL = new SafeSQL();
            String sql = "UPDATE propertytree SET propertytreetype = " + safeSQL.addVar(propertytreetype) + ", propertytreedesc = " + safeSQL.addVar(propertytreedesc) + ", objectname = " + safeSQL.addVar(objectname) + ", modby=" + safeSQL.addVar(this.connectionInfo.getSysuserId()) + ", moddt = " + safeSQL.addVar(now) + "  WHERE propertytreeid=" + safeSQL.addVar(propertytreeid);
            db.executePreparedUpdate(sql, safeSQL.getValues());
            this.resetCache(CACHE_TREE, propertytreeid);
            if (propertytreetype.equalsIgnoreCase("step")) {
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepProps");
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepTypeProps");
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add property tree: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setPropertyTreeCategories(String propertytreeid, String categorylist) throws ServiceException {
        this.logInfo("Setting property tree categories for propertytreeid '" + propertytreeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String[] categoryid;
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("DELETE FROM categoryitem WHERE sdcid = 'PropertyTree' AND keyid1 = ?", new Object[]{propertytreeid});
            if (categorylist != null && categorylist.length() > 0 && (categoryid = StringUtil.split(categorylist, ";")).length > 0) {
                PreparedStatement ps = db.prepareStatement("INSERT INTO categoryitem ( categoryid, sdcid, keyid1 ) VALUES( ?, 'PropertyTree', ? )");
                ps.setString(2, propertytreeid);
                for (int i = 0; i < categoryid.length; ++i) {
                    ps.setString(1, categoryid[i]);
                    ps.executeUpdate();
                }
                db.closeStatement();
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add categories " + SafeHTML.encodeForHTML(categorylist) + " to " + propertytreeid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setPropertyTreeRoles(String propertytreeid, String rolelist) throws ServiceException {
        this.logInfo("Setting property tree roles for propertytreeid '" + propertytreeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String[] roleid;
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("DELETE FROM sdirole WHERE sdcid='PropertyTree' AND keyid1=? and privid='list'", new Object[]{propertytreeid});
            if (rolelist != null && rolelist.length() > 0 && (roleid = StringUtil.split(rolelist, ";")).length > 0) {
                PreparedStatement ps = db.prepareStatement("INSERT INTO sdirole ( sdcid, keyid1, privid, roleid ) VALUES( 'PropertyTree', ?, 'list', ? )");
                ps.setString(1, propertytreeid);
                for (int i = 0; i < roleid.length; ++i) {
                    ps.setString(2, roleid[i]);
                    ps.executeUpdate();
                }
                db.closeStatement();
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add roles " + SafeHTML.encodeForHTML(rolelist) + " to " + propertytreeid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setWebPage(String webpageid, String productedition, String webpagedesc, String jspfile, String virtualpageflag, String expresspageflag, String extendwebpageid, String extendproductedition) throws ServiceException {
        this.logInfo("Setting web page '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int licvirtualcount;
            int virtualcount;
            int pos;
            db.setConnection(this.sapphireConnection);
            String filename = jspfile;
            String location = null;
            if (jspfile != null && jspfile.length() > 0 && (pos = (jspfile = StringUtil.replaceAll(jspfile, "\\", "/")).lastIndexOf(47)) > 0 && pos < jspfile.length() - 1) {
                location = jspfile.substring(0, pos + 1);
                filename = jspfile.substring(pos + 1);
            }
            Timestamp now = DateTimeUtil.getNowTimestamp();
            extendwebpageid = extendwebpageid == null ? "" : extendwebpageid;
            extendproductedition = extendproductedition == null ? "" : extendproductedition;
            String expresspage = "";
            if (expresspageflag.equals("Y")) {
                expresspage = EncryptDecrypt.encodePageid(webpageid + productedition);
            }
            SafeSQL safeSQL = new SafeSQL();
            String sql = "UPDATE webpage SET webpagedesc = " + safeSQL.addVar(webpagedesc) + ", location = " + safeSQL.addVar(location) + ", filename = " + safeSQL.addVar(filename) + ", modby=" + safeSQL.addVar(this.connectionInfo.getSysuserId()) + ", moddt = " + safeSQL.addVar(now) + ", virtualpageflag=" + safeSQL.addVar(virtualpageflag) + ", expresspage=" + safeSQL.addVar(expresspage) + ", extendwebpageid=" + safeSQL.addVar(extendwebpageid) + ", extendproductedition=" + safeSQL.addVar(extendproductedition) + " WHERE webpageid=" + safeSQL.addVar(webpageid) + " AND productedition=" + safeSQL.addVar(productedition);
            if (db.executePreparedUpdate(sql, safeSQL.getValues()) == 0) {
                String compcode = Configuration.getCompcode(this.sapphireConnection.getDatabaseId());
                safeSQL.reset();
                sql = "INSERT INTO webpage ( webpageid, productedition, webpagedesc, webpagetypeflag, location, filename, createby, createdt, modby, moddt, virtualpageflag, expresspage, compcode, extendwebpageid, extendproductedition ) VALUES ( " + safeSQL.addVar(webpageid) + "," + safeSQL.addVar(productedition) + ", " + safeSQL.addVar(webpagedesc) + ", 'U', " + safeSQL.addVar(location) + ", " + safeSQL.addVar(filename) + ", " + safeSQL.addVar(this.connectionInfo.getSysuserId()) + ", " + safeSQL.addVar(now) + "," + safeSQL.addVar(this.connectionInfo.getSysuserId()) + "," + safeSQL.addVar(now) + ", " + safeSQL.addVar(virtualpageflag) + ", " + safeSQL.addVar(expresspage) + ", " + safeSQL.addVar(compcode) + ", " + safeSQL.addVar(extendwebpageid) + ", " + safeSQL.addVar(extendproductedition) + " )";
                db.executePreparedUpdate(sql, safeSQL.getValues());
                if (extendwebpageid.length() > 0) {
                    sql = "INSERT INTO webpagepropertytree( webpageid, productedition, propertytreeid, elementid, extendnodeid, usersequence ) SELECT ?, ?, propertytreeid, elementid, extendnodeid, usersequence FROM webpagepropertytree WHERE webpageid=? AND productedition=?";
                    PreparedStatement ps = db.prepareStatement("insertprops", sql);
                    ps.setString(1, webpageid);
                    ps.setString(2, productedition);
                    ps.setString(3, extendwebpageid);
                    ps.setString(4, extendproductedition);
                    ps.execute();
                    db.closeStatement(ps);
                }
            }
            if (virtualpageflag.equals("Y") && (virtualcount = db.getCount("SELECT count(*) FROM webpage WHERE virtualpageflag='Y'")) > (licvirtualcount = Configuration.getInstance().getLicense(this.sapphireConnection.getDatabaseId()).getVirtualPageCount())) {
                this.logWarn("Too many virtual pages detected on save so resetting current page to (N) on virtual");
                sql = "UPDATE webpage SET virtualpageflag='N' WHERE webpageid=? AND productedition=?";
                db.executePreparedUpdate(sql, new Object[]{webpageid, productedition});
            }
            this.resetCache(CACHE_PAGE, webpageid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add or update Web Page: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setWebPageDefaultFlag(String webpageid, String defaultedition) throws ServiceException {
        this.logInfo("Setting defualtedition to " + defaultedition + "for web page '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sql = "UPDATE webpage SET defaultpageflag = '' WHERE webpageid=? AND productedition != ?";
            db.executePreparedUpdate(sql, new Object[]{webpageid, defaultedition});
            sql = "UPDATE webpage SET defaultpageflag = 'Y' WHERE webpageid=? AND productedition=?";
            db.executePreparedUpdate(sql, new Object[]{webpageid, defaultedition});
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "DefaultPageEdition", webpageid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add or update Web Page: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setWebPageProperties(String webpageid, String productedition, PropertyList propertylist) throws ServiceException {
        this.logInfo("Setting web page properties '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("DELETE FROM webpageproperty WHERE webpageid = ? AND productedition=?", new Object[]{webpageid, productedition});
            PreparedStatement ps = db.prepareStatement("INSERT INTO webpageproperty ( webpageid, productedition, propertyid, propertyvalue ) VALUES( ?, ?, ?, ? )");
            ps.setString(1, webpageid);
            ps.setString(2, productedition);
            Set keyset = propertylist.keySet();
            for (String propertyid : keyset) {
                String propertyvalue = propertylist.getProperty(propertyid);
                ps.setString(3, propertyid);
                ps.setString(4, propertyvalue);
                ps.executeUpdate();
            }
            db.closeStatement();
            this.resetCache(CACHE_PAGE, webpageid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not set webpage properties: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setWebPageCategories(String webpageid, String categorylist) throws ServiceException {
        this.logInfo("Setting web page categories for webpageid '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String[] categoryid;
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("DELETE FROM categoryitem WHERE sdcid = 'WebPage' AND keyid1 = ?", new Object[]{webpageid});
            if (categorylist != null && categorylist.length() > 0 && (categoryid = StringUtil.split(categorylist, ";")).length > 0) {
                PreparedStatement ps = db.prepareStatement("INSERT INTO categoryitem ( categoryid, sdcid, keyid1 ) VALUES( ?, 'WebPage', ? )");
                ps.setString(2, webpageid);
                for (int i = 0; i < categoryid.length; ++i) {
                    ps.setString(1, categoryid[i]);
                    ps.executeUpdate();
                }
                db.closeStatement();
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add categories " + SafeHTML.encodeForHTML(categorylist) + " to " + webpageid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void setWebPageRoles(String webpageid, String rolelist) throws ServiceException {
        this.logInfo("Setting web page roles for webpageid '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String[] roleid;
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("DELETE FROM sdirole WHERE sdcid='WebPage' AND keyid1=? and privid='list'", new Object[]{webpageid});
            if (rolelist != null && rolelist.length() > 0 && (roleid = StringUtil.split(rolelist, ";")).length > 0) {
                PreparedStatement ps = db.prepareStatement("INSERT INTO sdirole ( sdcid, keyid1, privid, roleid ) VALUES( 'WebPage', ?, 'list', ? )");
                ps.setString(1, webpageid);
                for (int i = 0; i < roleid.length; ++i) {
                    ps.setString(2, roleid[i]);
                    ps.executeUpdate();
                }
                db.closeStatement();
            }
            this.resetCache(CACHE_PAGE_ACCESS, webpageid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add roles " + SafeHTML.encodeForHTML(rolelist) + " to " + webpageid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void addWebPagePropertyTree(String webpageid, String productedition, String propertytreeid, String elementid, String extendnodeid, int sequence) throws ServiceException {
        this.logInfo("Adding web page property tree '" + propertytreeid + "' for webpageid '" + webpageid + "' edition '" + productedition + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sql = "INSERT INTO webpagepropertytree( webpageid, productedition, propertytreeid, elementid, extendnodeid, usersequence ) VALUES( ?, ?, ?, ?, ?, ? )";
            PreparedStatement ps = db.prepareStatement("inserttree", sql);
            ps.setString(1, webpageid);
            ps.setString(2, productedition);
            ps.setString(3, propertytreeid);
            ps.setString(4, elementid);
            ps.setString(5, extendnodeid);
            ps.setInt(6, sequence);
            if (ps.executeUpdate() != 1) {
                this.logError("No rows inserted");
            }
            db.createPreparedResultSet("selectchildren", "SELECT webpageid, productedition FROM webpage WHERE extendwebpageid=? and extendproductedition=?", new String[]{webpageid, productedition});
            while (db.getNext("selectchildren")) {
                String childwebpageid = db.getString("selectchildren", "webpageid");
                String childproductedition = db.getString("selectchildren", "productedition");
                if (db.getPreparedCount("SELECT count(*) FROM webpagepropertytree WHERE webpageid=? AND productedition=? AND propertytreeid=? AND elementid=?", new String[]{childwebpageid, childproductedition, propertytreeid, elementid}) != 0) continue;
                ps.setString(1, childwebpageid);
                ps.setString(2, childproductedition);
                ps.setString(3, propertytreeid);
                ps.setString(4, elementid);
                ps.setString(5, extendnodeid);
                ps.setInt(6, sequence);
                ps.executeUpdate();
                this.resetCache(CACHE_PAGE, childwebpageid);
            }
            db.closeResultSet("selectchildren");
            db.closeStatement("inserttree");
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add property tree: " + se.getMessage(), se);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not add property tree: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void deleteWebPagePropertyTree(String webpageid, String productedition, String elementid, String propertytreeid) throws ServiceException {
        this.logInfo("Deleting web page property tree '" + propertytreeid + " for webpageid '" + webpageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sql = "DELETE FROM webpagepropertytree WHERE webpageid = ? AND productedition=? AND elementid = ? AND propertytreeid = ?";
            db.executePreparedUpdate(sql, new Object[]{webpageid, productedition, elementid, propertytreeid});
            this.resetCache(CACHE_PAGE, webpageid);
            this.resetCache(CACHE_PAGE_TREE_ELEMENT, webpageid + "__" + propertytreeid + "__" + elementid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not remove page: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void renameWebPagePropertyTreeNode(String propertytreeid, String oldnodeid, String newnodeid) throws ServiceException {
        this.logInfo("Renaming node in propertytreeid '" + propertytreeid + "' from '" + oldnodeid + "' to '" + newnodeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT propertytreetype FROM propertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
            if (db.getNext()) {
                String propertyTreeType = db.getString("propertytreetype");
                String sql = "";
                SafeSQL safeSQL = new SafeSQL();
                if ("Element".equals(propertyTreeType) || "Layout".equals(propertyTreeType) || "Page Type".equals(propertyTreeType)) {
                    sql = "UPDATE webpagepropertytree SET extendnodeid = " + safeSQL.addVar(newnodeid) + " WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("Gizmo".equals(propertyTreeType)) {
                    sql = "UPDATE gizmodef SET extendnodeid = " + safeSQL.addVar(newnodeid) + " WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("Step".equals(propertyTreeType)) {
                    sql = "UPDATE taskdefstep SET extendnodeid = " + safeSQL.addVar(newnodeid) + " WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("CollectorType".equals(propertyTreeType)) {
                    sql = "UPDATE instrument SET collectorextendnodeid = " + safeSQL.addVar(newnodeid) + " WHERE collectorpropertytreeid = " + safeSQL.addVar(propertytreeid) + " AND collectorextendnodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                    safeSQL = new SafeSQL();
                    sql = "UPDATE instrumentmodel SET collectorextendnodeid = " + safeSQL.addVar(newnodeid) + " WHERE collectorpropertytreeid = " + safeSQL.addVar(propertytreeid) + " AND collectorextendnodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("WorksheetItem".equals(propertyTreeType)) {
                    sql = "UPDATE worksheetitem SET sourcenodeid = " + safeSQL.addVar(newnodeid) + " WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND sourcenodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("FileRepository".equals(propertyTreeType)) {
                    sql = "UPDATE sdiattachment SET attachmentrepositorynodeid = " + safeSQL.addVar(newnodeid) + " WHERE attachmentrepositoryid = " + safeSQL.addVar(propertytreeid) + " AND attachmentrepositorynodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                } else if ("ScheduleTask".equals(propertyTreeType)) {
                    sql = "UPDATE scheduleplanitem SET scheduletasknodeid = " + safeSQL.addVar(newnodeid) + " WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND scheduletasknodeid = " + safeSQL.addVar(oldnodeid);
                    db.executePreparedUpdate(sql, safeSQL.getValues());
                }
            }
            this.resetCache(CACHE_TREE, propertytreeid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not rename node: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void moveWebPagePropertyTreePage(String webpageid, String productedition, String elementid, String propertytreeid, String newnodeid) throws ServiceException {
        this.logInfo("Moving page webpageid '" + webpageid + "' in propertytreeid '" + propertytreeid + "' to '" + newnodeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String sql = "UPDATE webpagepropertytree SET extendnodeid = ? WHERE webpageid = ? AND productedition=? AND elementid = ? AND propertytreeid = ?";
            db.executePreparedUpdate(sql, new Object[]{newnodeid, webpageid, productedition, elementid, propertytreeid});
            Timestamp now = DateTimeUtil.getNowTimestamp();
            db.executePreparedUpdate("UPDATE webpage SET modby = ?, moddt = ? WHERE webpageid = ? AND productedition=?", new Object[]{this.connectionInfo.getSysuserId(), now, webpageid, productedition});
            this.resetCache(CACHE_PAGE, webpageid);
            this.resetCache(CACHE_PAGE_TREE_ELEMENT, webpageid + "__" + propertytreeid + "__" + elementid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not rename node: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void deletePropertyTree(String propertytreeid) throws ServiceException {
        this.logInfo("Deleting properytreeid '" + propertytreeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            PropertyList pl = new PropertyList();
            pl.setProperty("sdcid", "PropertyTree");
            pl.setProperty("keyid1", propertytreeid);
            ActionService actionService = new ActionService(this.sapphireConnection);
            actionService.processAction("DeleteSDI", "1", pl);
            this.resetCache(CACHE_TREE, propertytreeid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not remove propertytree: " + propertytreeid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void synchronizePropertyTree(String propertytreeid) throws ServiceException {
        this.logInfo("Synchronizing properytreeid '" + propertytreeid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String tree = this.getPropertyTreeDef(propertytreeid);
            Document doc = DOMUtil.getNewDocument(tree);
            Node ptreenode = WebAdminProcessor.getPropertyTreeNode(doc);
            Element propertydeplist = (Element)XPathAPI.selectSingleNode((Node)ptreenode, (String)"propertydeplist");
            NodeList propertydeps = XPathAPI.selectNodeList((Node)propertydeplist, (String)"propertydep");
            for (int i = 0; i < propertydeps.getLength(); ++i) {
                Element propertydep = (Element)propertydeps.item(i);
                String elementpropertytreeid = propertydep.getAttribute("propertytreeid");
                String elementid = propertydep.getAttribute("elementid");
                if (propertydep.getAttribute("mandatory").equals("false")) continue;
                SafeSQL safeSQL = new SafeSQL();
                String sql = "INSERT INTO webpagepropertytree( webpageid, productedition, propertytreeid, elementid, extendnodeid, usersequence ) SELECT distinct webpageid, productedition,'" + elementpropertytreeid + "', '" + elementid + "', '__root', " + 2 + i + " FROM webpagepropertytree WHERE ( webpageid, productedition ) in ( \tSELECT\twebpageid, productedition \tFROM \twebpagepropertytree \tWHERE \tpropertytreeid = " + safeSQL.addVar(propertytreeid) + " ) AND ( webpageid, productedition ) not in ( \tSELECT \twebpageid, productedition \tFROM \twebpagepropertytree \tWHERE\tpropertytreeid = " + safeSQL.addVar(elementpropertytreeid) + " \tAND \telementid = " + safeSQL.addVar(elementid) + " ) ";
                db.executePreparedUpdate(sql, safeSQL.getValues());
            }
            String whereClause = "";
            String propdepClause = "";
            for (int i = 0; i < propertydeps.getLength(); ++i) {
                Element propertydep = (Element)propertydeps.item(i);
                String elementpropertytreeid = propertydep.getAttribute("propertytreeid");
                String elementid = propertydep.getAttribute("elementid");
                propdepClause = propdepClause + " AND ( elementid <> '" + elementid + "' or webpagepropertytree.propertytreeid <> '" + elementpropertytreeid + "' ) ";
            }
            if (this.connectionInfo.getDbms().equals("ORA")) {
                whereClause = "( webpageid, productedition, propertytreeid, elementid ) in  ( SELECT\twebpageid, productedition, webpagepropertytree.propertytreeid, webpagepropertytree.elementid \tFROM \twebpagepropertytree, propertytree \tWHERE \twebpagepropertytree.propertytreeid = propertytree.propertytreeid \tAND \tpropertytreetype = 'Element' \tAND ( webpageid in \t\t    ( \t\t\tSELECT\twebpageid \t\t\tFROM webpagepropertytree \t\t\tWHERE propertytreeid='" + propertytreeid + "'\t\t    )        )   AND ( productedition in \t\t    ( \t\t\tSELECT\tproductedition \t\t\tFROM webpagepropertytree \t\t\tWHERE propertytreeid='" + propertytreeid + "'\t\t    )        )" + propdepClause + " )";
            } else {
                String commonClause = "\tFROM \twebpagepropertytree, propertytree \tWHERE \twebpagepropertytree.propertytreeid = propertytree.propertytreeid \tAND \tpropertytreetype = 'Element' \tAND ( webpageid in \t\t    ( \t\t\tSELECT\twebpageid \t\t\tFROM webpagepropertytree \t\t\tWHERE propertytreeid='" + propertytreeid + "'\t\t    )        )   AND ( productedition in \t\t    ( \t\t\tSELECT\tproductedition \t\t\tFROM webpagepropertytree \t\t\tWHERE propertytreeid='" + propertytreeid + "'\t\t    )        )" + propdepClause;
                String webpageidClause = "webpageid in  ( SELECT\twebpageid " + commonClause + ")";
                String producteditionClause = "productedition in  ( SELECT productedition " + commonClause + ")";
                String propertytreeidClause = "propertytreeid in  ( SELECT\twebpagepropertytree.propertytreeid " + commonClause + ")";
                String elementidClause = "elementid in  ( \tSELECT\twebpagepropertytree.elementid " + commonClause + ")";
                whereClause = webpageidClause + " AND " + producteditionClause + " AND " + propertytreeidClause + " AND " + elementidClause;
            }
            String deletesql = "DELETE FROM webpagepropertytree WHERE " + whereClause;
            db.executeUpdate(deletesql);
            this.resetCache(CACHE_WEB_PAGE_DESIGNER, "");
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not remove propertytree: " + propertytreeid + ": " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void resetCache(String cachename, String cacheItemList) {
        Trace.logInfo(LOGNAME, "Resetting caches");
        String[] cacheItem = StringUtil.split(cacheItemList, ";");
        String databaseid = this.sapphireConnection.getDatabaseId();
        if (cachename.equalsIgnoreCase(CACHE_WEB_PAGE_DESIGNER)) {
            CacheUtil.clear(databaseid, "WebPageAccess");
            CacheUtil.clear(databaseid, "WebPageProperties");
            CacheUtil.clear(databaseid, "WebPageFilename");
            CacheUtil.clear(databaseid, "WebPagePropertyTree");
            CacheUtil.clear(databaseid, "WebPageTreeElement");
            CacheUtil.clear(databaseid, "PropertyTreeDefinition");
            CacheUtil.clear(databaseid, "PropertyTree");
            CacheUtil.clear(databaseid, "PropertyTreeNode");
        } else if (cachename.equalsIgnoreCase(CACHE_PAGE_TREE_ELEMENT)) {
            for (int i = 0; i < cacheItem.length; ++i) {
                CacheUtil.removeAllStartWith(databaseid, "WebPageTreeElement", cacheItem[i]);
            }
        } else if (cachename.equalsIgnoreCase(CACHE_PAGE_ACCESS)) {
            CacheUtil.clear(databaseid, "WebPageAccess");
        } else if (cachename.equalsIgnoreCase(CACHE_PAGE)) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
            for (int i = 0; i < cacheItem.length; ++i) {
                try {
                    db.createPreparedResultSet("SELECT distinct webpageid FROM webpage WHERE extendwebpageid=?", cacheItem[i]);
                    while (db.getNext()) {
                        String childWebpageid = db.getString("webpageid");
                        CacheUtil.removeAllStartWith(databaseid, "WebPageProperties", childWebpageid, false);
                        CacheUtil.removeAllStartWith(databaseid, "WebPageFilename", childWebpageid, false);
                        CacheUtil.removeAllStartWith(databaseid, "WebPagePropertyTree", childWebpageid, false);
                        CacheUtil.removeAllStartWith(databaseid, "WebPageTreeElement", childWebpageid, false);
                    }
                    db.closeResultSet();
                }
                catch (SapphireException childWebpageid) {
                    // empty catch block
                }
                String startwith = cacheItem[i];
                CacheUtil.removeAllStartWith(databaseid, "WebPageProperties", startwith, i == cacheItem.length - 1);
                CacheUtil.removeAllStartWith(databaseid, "WebPageFilename", startwith, i == cacheItem.length - 1);
                CacheUtil.removeAllStartWith(databaseid, "WebPagePropertyTree", startwith, i == cacheItem.length - 1);
                CacheUtil.removeAllStartWith(databaseid, "WebPageTreeElement", startwith, i == cacheItem.length - 1);
            }
            CacheUtil.clear(databaseid, "WebPageAccess");
        } else if (cachename.equalsIgnoreCase("Gizmo")) {
            for (int i = 0; i < cacheItem.length; ++i) {
                String ptreeid = cacheItem[i];
                CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "Gizmo", ptreeid + ";");
            }
        } else if (cachename.equalsIgnoreCase("GUIModes")) {
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "GUIModes", "GUIModes");
        } else if (cachename.equalsIgnoreCase("GizmoDef")) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
            for (int i = 0; i < cacheItem.length; ++i) {
                String gizmoid = cacheItem[i];
                CacheUtil.remove(this.connectionInfo.getDatabaseId(), "GizmoDef", gizmoid);
                CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefSecured", gizmoid + ";");
                CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefUser", gizmoid + ";");
                CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefAccess", gizmoid + ";");
                CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefAssets", gizmoid);
            }
            CacheUtil.clear(databaseid, "WebPageAccess");
        } else if (cachename.equalsIgnoreCase(CACHE_TREE)) {
            for (int i = 0; i < cacheItem.length; ++i) {
                CacheUtil.remove(databaseid, "PropertyTreeDefinition", cacheItem[i], i == cacheItem.length - 1);
                CacheUtil.removeAllStartWith(databaseid, "PropertyTree", cacheItem[i], i == cacheItem.length - 1);
                CacheUtil.removeAllStartWith(databaseid, "PropertyTreeNode", cacheItem[i], i == cacheItem.length - 1);
                if (!cacheItem[i].equals("SearchPolicy")) continue;
                try {
                    Indexer.refreshInstance(databaseid);
                    continue;
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
            }
            CacheUtil.clear(databaseid, "ScheduleTaskNodeProperties");
            CacheUtil.clear(databaseid, "ScheduleTaskPropertyTree");
            CacheUtil.clear(databaseid, "ScheduleGrid");
            CacheUtil.clear(databaseid, "WebPageFilename");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getUserOverridesAsPropertyList(String sysuserId, String webpageid, String productedition) {
        StringBuffer output = new StringBuffer();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT elementid, valuetree FROM webpageuseroverride WHERE sysuserid=? AND webpageid=? AND productedition=?", new String[]{sysuserId, webpageid, productedition});
            output.append("<propertylist>");
            while (db.getNext()) {
                String elementid = db.getString("elementid");
                String valuetree = db.getClob("valuetree");
                output.append("<property id=\"").append(elementid).append("\" type=\"propertylist\">");
                output.append(valuetree);
                output.append("</property>");
            }
            output.append("</propertylist>");
        }
        catch (SapphireException e) {
            this.logger.stackTrace(e);
        }
        finally {
            db.reset();
        }
        return output.toString();
    }

    public void setComponentPageOverride(String webpageid, String productedition, String propertytreeid, String elementid, String compCode, String properties) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            db.executePreparedUpdate("UPDATE webpage SET modby = ?, moddt = ? WHERE webpageid = ? AND productedition= ?", new Object[]{this.connectionInfo.getSysuserId(), now, webpageid, productedition});
            PropertyList propertiesPL = new PropertyList();
            propertiesPL.setPropertyList(properties);
            String oldComponentValueTree = this.getComponentPageOverrides(webpageid, productedition, propertytreeid, elementid);
            PropertyList componentValueTreePL = new PropertyList();
            componentValueTreePL.setPropertyList(oldComponentValueTree);
            PropertyListCollection components = componentValueTreePL.getCollectionNotNull("components");
            PropertyList component = components.find("compcode", compCode);
            if (component == null) {
                component = new PropertyList();
                component.setProperty("compcode", compCode);
                components.add(component);
            }
            component.setProperty("value", propertiesPL);
            db.updateClob("webpagepropertytree", "componentvaluetree", componentValueTreePL.toXMLString(), new String[]{"webpageid", "productedition", "propertytreeid", "elementid"}, new String[]{webpageid, productedition, propertytreeid, elementid});
            this.resetCache(CACHE_PAGE, webpageid);
            this.resetCache(CACHE_TREE, propertytreeid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not set page value tree: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void removeComponentPageOverride(String webpageid, String productedition, String propertytreeid, String elementid, String compCode) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            db.executePreparedUpdate("UPDATE webpage SET modby = ?, moddt = ? WHERE webpageid = ? AND productedition= ?", new Object[]{this.connectionInfo.getSysuserId(), now, webpageid, productedition});
            String oldComponentValueTree = this.getComponentPageOverrides(webpageid, productedition, propertytreeid, elementid);
            PropertyList componentValueTreePL = new PropertyList();
            componentValueTreePL.setPropertyList(oldComponentValueTree);
            PropertyListCollection components = componentValueTreePL.getCollectionNotNull("components");
            PropertyList component = components.find("compcode", compCode);
            if (component != null) {
                components.remove(component);
            }
            db.updateClob("webpagepropertytree", "componentvaluetree", componentValueTreePL.toXMLString(), new String[]{"webpageid", "productedition", "propertytreeid", "elementid"}, new String[]{webpageid, productedition, propertytreeid, elementid});
            this.resetCache(CACHE_PAGE, webpageid);
            this.resetCache(CACHE_TREE, propertytreeid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not set page value tree: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getComponentPageOverrides(String webpageid, String productedition, String propertytreeid, String elementid) {
        String valuetree = "<propertylist/>";
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT componentvaluetree FROM webpagepropertytree WHERE webpageid=? AND productedition=? AND elementid=? AND propertytreeid=?", new String[]{webpageid, productedition, elementid, propertytreeid});
            if (db.getNext()) {
                valuetree = db.getClob("componentvaluetree");
            }
        }
        catch (SapphireException e) {
            this.logger.stackTrace(e);
        }
        finally {
            db.reset();
        }
        return valuetree;
    }
}

