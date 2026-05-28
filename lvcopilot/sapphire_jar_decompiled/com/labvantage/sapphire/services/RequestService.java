/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Stats;
import com.labvantage.sapphire.admin.system.TranslationUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RequestService
extends BaseService
implements CacheNames {
    public static final String LOGNAME = "RequestService";
    public static final String WEBPAGE_FILENAME = "__filename";
    public static final String WEBPAGE_PRODUCTEDITION = "__productedition";
    public static final String FAVORITES_WEBPAGEID = "__webpageid";
    public static final String FAVORITES_URL = "__url";
    private static HashMap<String, Set<String>> inactiveRoleListDataBase = new HashMap();
    public static Object inactiveRoleListLock = new Object();

    public RequestService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public PropertyList getConnectionProperties() throws ServiceException {
        Stats.setStart("getConnectionProperties");
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("connectionid", this.sapphireConnection.getConnectionId());
        propertyList.setProperty("sysuserid", this.sapphireConnection.getSysuserId());
        propertyList.setProperty("sysusername", this.sapphireConnection.getSysuserName());
        propertyList.setProperty("dbms", this.sapphireConnection.getDbms());
        propertyList.setProperty("databaseid", this.sapphireConnection.getDatabaseId());
        propertyList.setProperty("tool", this.sapphireConnection.getTool());
        propertyList.setProperty("deviceid", this.sapphireConnection.getDeviceId());
        propertyList.setProperty("externalappid", this.sapphireConnection.getExternalAppId());
        propertyList.setProperty("authtokenid", this.sapphireConnection.getAuthTokenId());
        propertyList.setProperty("guimode", this.sapphireConnection.getGuiMode());
        propertyList.setProperty("rolelist", this.sapphireConnection.getRoleList());
        propertyList.setProperty("modulelist", this.sapphireConnection.getModuleList());
        propertyList.setProperty("language", this.sapphireConnection.getLanguage());
        propertyList.setProperty("locale", this.sapphireConnection.getLocale());
        propertyList.setProperty("timezone", this.sapphireConnection.getTimeZone());
        propertyList.setProperty("defaultdepartment", this.sapphireConnection.getDefaultDepartment());
        PropertyList props = (PropertyList)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "Connection Properties", this.sapphireConnection.getConnectionId());
        if (props == null) {
            props = new PropertyList();
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                String propertySelect = "SELECT\tpropertyid,sysuserid,propertyvalue FROM\tprofileproperty WHERE\tprofileid = 'System' AND \t\tsysuserid in ('(system)',?) AND \t\tpropertyid in ( 'bousername', 'bouniverse', 'bodomain', 'bourl' ) AND \t\t((profileid) NOT IN \t\t(SELECT a.profileid \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid <> '(system)' AND \t\t\t\tb.sysuserid = ?) OR \t\t(propertyid) NOT IN \t\t(SELECT a.propertyid \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid <> '(system)' AND \t\t\t\tb.sysuserid = ?) OR \t\t(sysuserid) NOT IN \t\t(SELECT '(system)' \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid <> '(system)' AND \t\t\t\tb.sysuserid = ?))";
                Object[] params = new Object[]{this.connectionInfo.getSysuserId(), this.connectionInfo.getSysuserId(), this.connectionInfo.getSysuserId(), this.connectionInfo.getSysuserId()};
                db.createPreparedResultSet(propertySelect, params);
                while (db.getNext()) {
                    String value = db.getString("propertyvalue");
                    props.setProperty(db.getString("propertyid"), value);
                }
                PropertyList modules = new PropertyList();
                String[] moduleList = StringUtil.split(this.sapphireConnection.getModuleList(), ";");
                for (int i = 0; i < moduleList.length; ++i) {
                    modules.setProperty(moduleList[i], "true");
                }
                props.setProperty("hasModule", modules);
                db.createPreparedResultSet("SELECT sysuserdesc, initials FROM sysuser WHERE sysuserid=?", this.sapphireConnection.getSysuserId());
                if (db.getNext()) {
                    String desc = db.getString("sysuserdesc");
                    props.setProperty("sysuserdesc", desc);
                    String ini = db.getString("initials");
                    props.setProperty("sysuserinitals", ini != null ? ini : "");
                } else {
                    props.setProperty("sysuserdesc", "(system)");
                    props.setProperty("sysuserinitals", "");
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "Connection Properties", this.sapphireConnection.getConnectionId(), props);
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get session properties for " + this.sapphireConnection.getSysuserId(), se);
            }
            finally {
                db.reset();
            }
        }
        for (String propertyId : props.keySet()) {
            if (props.isSimple(propertyId)) {
                propertyList.setProperty(propertyId, props.getProperty(propertyId));
                continue;
            }
            if (!props.isPropertyList(propertyId)) continue;
            propertyList.setProperty(propertyId, props.getPropertyList(propertyId));
        }
        Stats.setEnd("getConnectionProperties");
        return propertyList;
    }

    public PropertyList getWebPageProperties(String webpageid, String productedition, PropertyList requestProps) throws ServiceException {
        return this.getWebPageProperties(webpageid, productedition, requestProps, true);
    }

    /*
     * WARNING - void declaration
     */
    public PropertyList getWebPageProperties(String webpageid, String productedition, PropertyList requestProps, boolean filterProperties) throws ServiceException {
        this.logDebug("Getting web page properties for webpageid '" + webpageid + "' productedition '" + productedition + "'");
        if (webpageid == null || webpageid.length() == 0) {
            if ("Y".equals(requestProps.getProperty("findmissingtransmastertext"))) {
                return new PropertyList();
            }
            throw new ServiceException("INVALID_PARAMETER", "Webpageid not specified");
        }
        if (productedition == null || productedition.length() == 0) {
            productedition = "R4";
        }
        PropertyList webPageProps = new PropertyList();
        webPageProps.putAll(requestProps);
        String filename = null;
        TranslationProcessor translationProcessor = "Y".equals(requestProps.getProperty("findmissingtransmastertext")) ? TranslationUtil.getPropertyTranslationProcessor(this.sapphireConnection.getConnectionId(), webpageid) : new TranslationProcessor(this.sapphireConnection.getConnectionId());
        translationProcessor.setTextType(webpageid);
        String languageid = this.connectionInfo.getLanguage();
        if (languageid == null || languageid.length() == 0) {
            languageid = "(null)";
        }
        WebAdminService webadmin = new WebAdminService(this.sapphireConnection);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            void var22_33;
            ArrayList props;
            this.validateUserWebPageAccess(webpageid, productedition);
            db.setConnection(this.sapphireConnection);
            ArrayList allPropertyTrees = (ArrayList)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "WebPagePropertyTree", webpageid + ";" + productedition);
            if (allPropertyTrees == null) {
                allPropertyTrees = new ArrayList();
                String propertyTreeSelect = "SELECT\twp.extendwebpageid, wp.extendproductedition, wppt.propertytreeid, wppt.elementid, wppt.extendnodeid, pt.propertytreetype, pt.objectname FROM\twebpagepropertytree wppt, propertytree pt, webpage wp WHERE\twp.webpageid = wppt.webpageid AND \twp.productedition = wppt.productedition AND \twppt.propertytreeid = pt.propertytreeid AND wp.webpageid = ? AND wp.productedition = ? order by wppt.usersequence";
                db.createPreparedResultSet(propertyTreeSelect, new Object[]{webpageid, productedition});
                while (db.getNext()) {
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("extendwebpageid", db.getString("extendwebpageid"));
                    m.put("extendproductedition", db.getString("extendproductedition"));
                    m.put("propertytreeid", db.getString("propertytreeid"));
                    m.put("elementid", db.getString("elementid"));
                    m.put("extendnodeid", db.getString("extendnodeid"));
                    m.put("propertytreetype", db.getString("propertytreetype"));
                    m.put("objectname", db.getString("objectname"));
                    if (OpalUtil.isNotEmpty(db.getString("objectname")) && db.getString("objectname").startsWith("GWT:")) {
                        m.put("gwtpagetype", db.getString("objectname").substring(4));
                        m.put("objectname", "WEB-CORE/pagetypes/gwtmaint/gwtpagetype.jsp");
                    }
                    allPropertyTrees.add(m);
                }
                CacheUtil.put(this.connectionInfo.getDatabaseId(), "WebPagePropertyTree", webpageid + ";" + productedition, allPropertyTrees);
            }
            ArrayList propertyTrees = new ArrayList();
            String currentLayout = requestProps.getProperty("currentlayout");
            String currentLayoutNode = requestProps.getProperty("currentlayoutnode");
            String currentLayoutReplace = requestProps.getProperty("currentlayoutreplace");
            String layout = currentLayout;
            String layoutnode = currentLayoutNode;
            String layoutelementid = "layout";
            String layoutobjectname = "";
            boolean firstLayout = true;
            for (HashMap hashMap : allPropertyTrees) {
                if ("Layout".equals(hashMap.get("propertytreetype"))) {
                    String propertytreeid = (String)hashMap.get("propertytreeid");
                    String extendnodeid = (String)hashMap.get("extendnodeid");
                    String elementid = (String)hashMap.get("elementid");
                    String objectname = (String)hashMap.get("objectname");
                    if (!firstLayout && (!propertytreeid.equals(currentLayout) || !extendnodeid.equals(currentLayoutNode)) && (currentLayout.length() <= 0 || currentLayoutNode.length() <= 0 || !currentLayoutReplace.equals(propertytreeid))) continue;
                    if (currentLayoutReplace.equals(propertytreeid)) {
                        layout = currentLayout;
                        layoutnode = currentLayoutNode;
                        layoutelementid = elementid;
                        layoutobjectname = "";
                    } else {
                        layout = propertytreeid;
                        layoutnode = extendnodeid;
                        layoutelementid = elementid;
                        layoutobjectname = objectname;
                    }
                    firstLayout = false;
                    continue;
                }
                propertyTrees.add(hashMap);
            }
            if (layout.length() > 0) {
                HashMap<String, String> propertyTree = new HashMap<String, String>();
                propertyTree.put("propertytreeid", layout);
                propertyTree.put("elementid", layoutelementid);
                propertyTree.put("extendnodeid", layoutnode);
                propertyTree.put("propertytreetype", "Layout");
                if (layoutobjectname.equals("")) {
                    db.createPreparedResultSet("SELECT\tobjectname FROM\tpropertytree WHERE propertytreeid=?", layout);
                    if (db.getNext()) {
                        layoutobjectname = db.getString("objectname");
                    }
                }
                propertyTree.put("objectname", layoutobjectname);
                propertyTrees.add(0, propertyTree);
            }
            for (HashMap hashMap : propertyTrees) {
                String extendwebpageid = (String)hashMap.get("extendwebpageid");
                String extendproductedition = (String)hashMap.get("extendproductedition");
                String propertytreeid = (String)hashMap.get("propertytreeid");
                String elementid = (String)hashMap.get("elementid");
                String extendnodeid = (String)hashMap.get("extendnodeid");
                String propertytreetype = (String)hashMap.get("propertytreetype");
                String objectname = (String)hashMap.get("objectname");
                String gwtPageType = (String)hashMap.get("gwtpagetype");
                PropertyList elementPropertyList = (PropertyList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "WebPageTreeElement", webpageid + "__" + productedition + "__" + propertytreeid + "__" + elementid);
                if (elementPropertyList == null) {
                    this.logDebug("Adding property tree '" + propertytreeid + "' identified by '" + elementid + "' from database...");
                    boolean isExtendPage = extendwebpageid != null && extendwebpageid.length() > 0;
                    String productPageOverrideValues = null;
                    productPageOverrideValues = this.getPageProductValueTree(webpageid, productedition, propertytreeid, elementid);
                    String pageOverrideValues = this.getPageValueTree(webpageid, productedition, propertytreeid, elementid);
                    String componentOverrides = webadmin.getComponentPageOverrides(webpageid, productedition, propertytreeid, elementid);
                    boolean hasPageOverrides = pageOverrideValues != null && pageOverrideValues.length() > 0 || productPageOverrideValues != null && productPageOverrideValues.length() > 0 || componentOverrides != null && componentOverrides.length() > 0;
                    String propertyTreeXML = this.getPropertyTreeValueCached(propertytreeid);
                    PropertyDefinitionList propertyTreeDefinitionList = this.getPropertyDefinitionListCached(propertytreeid);
                    PropertyList propertyTreeNode = (PropertyList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "PropertyTreeNode", propertytreeid + ";" + extendnodeid);
                    if (propertyTreeNode == null) {
                        this.logDebug("Getting property tree '" + propertytreeid + "' from database...");
                        propertyTreeNode = new PropertyList();
                        propertyTreeNode.setDbms(this.sapphireConnection.getDbms());
                        propertyTreeNode.setDatabaseid(this.sapphireConnection.getDatabaseId());
                        if (propertyTreeXML != null && extendnodeid != null && !extendnodeid.equals("__root")) {
                            propertyTreeNode.setPropertyTree(propertyTreeXML, extendnodeid, !hasPageOverrides && !isExtendPage, false, propertyTreeDefinitionList);
                            CacheUtil.put(this.connectionInfo.getDatabaseId(), "PropertyTreeNode", propertytreeid + ";" + extendnodeid, propertyTreeNode);
                        }
                    } else {
                        this.logDebug("Getting property tree '" + propertytreeid + "' from cache...");
                    }
                    propertyTreeNode = propertyTreeNode.copy();
                    propertyTreeNode.setId(elementid);
                    if (isExtendPage) {
                        String parentProductValueTree = this.getPageProductValueTree(extendwebpageid, extendproductedition, propertytreeid, elementid);
                        propertyTreeNode.setPropertyList(parentProductValueTree, true, false);
                        String parentComponentOverrides = webadmin.getComponentPageOverrides(extendwebpageid, extendproductedition, propertytreeid, elementid);
                        if (parentComponentOverrides != null && parentComponentOverrides.length() > 0) {
                            PropertyList coPL = new PropertyList();
                            coPL.setPropertyList(parentComponentOverrides);
                            PropertyListCollection components = coPL.getCollectionNotNull("components");
                            for (int i = 0; i < components.size(); ++i) {
                                PropertyList pl = components.getPropertyList(i);
                                PropertyList componentOverridesPL = pl.getPropertyListNotNull("value");
                                propertyTreeNode.setPropertyList(componentOverridesPL.toXMLString(), true, false);
                            }
                        }
                        String parentPageOverride = this.getPageValueTree(extendwebpageid, extendproductedition, propertytreeid, elementid);
                        propertyTreeNode.setPropertyList(parentPageOverride, true, false);
                    }
                    if (productPageOverrideValues != null) {
                        propertyTreeNode.setPropertyList(productPageOverrideValues, true, false);
                    }
                    if (componentOverrides != null && componentOverrides.length() > 0) {
                        PropertyList coPL = new PropertyList();
                        coPL.setPropertyList(componentOverrides);
                        PropertyListCollection components = coPL.getCollectionNotNull("components");
                        for (int i = 0; i < components.size(); ++i) {
                            PropertyList pl = components.getPropertyList(i);
                            PropertyList componentOverridesPL = pl.getPropertyListNotNull("value");
                            propertyTreeNode.setPropertyList(componentOverridesPL.toXMLString(), true, false);
                        }
                    }
                    if (pageOverrideValues != null) {
                        propertyTreeNode.setPropertyList(pageOverrideValues, true, false);
                    }
                    if ((hasPageOverrides || isExtendPage) && propertyTreeXML != null) {
                        propertyTreeNode.setLanguage(languageid);
                        propertyTreeNode.setTranslationProcessor(translationProcessor);
                        propertyTreeNode.setPropertyTreeDefaults(propertyTreeXML, propertyTreeDefinitionList);
                    }
                    propertyTreeNode.setProperty("webpageid", webpageid);
                    propertyTreeNode.setProperty("propertytreeid", propertytreeid);
                    propertyTreeNode.setProperty("elementid", elementid);
                    propertyTreeNode.setProperty("nodeid", extendnodeid);
                    propertyTreeNode.setProperty("propertytreetype", propertytreetype);
                    propertyTreeNode.setProperty("objectname", objectname);
                    propertyTreeNode.setProperty("gwtpagetype", gwtPageType);
                    propertyTreeNode.setDbms(this.sapphireConnection.getDbms());
                    propertyTreeNode.setDatabaseid(this.sapphireConnection.getDatabaseId());
                    CacheUtil.put(this.connectionInfo.getDatabaseId(), "WebPageTreeElement", webpageid + "__" + productedition + "__" + propertytreeid + "__" + elementid, propertyTreeNode);
                    elementPropertyList = propertyTreeNode;
                } else {
                    this.logInfo("Adding property tree '" + propertytreeid + "' identified by '" + elementid + "' from cache...");
                }
                webPageProps.setProperty(propertytreetype.equals("Layout") ? "layout" : elementid, elementPropertyList);
                if (!propertytreetype.equals("Page Type")) continue;
                filename = objectname;
            }
            if ((filename == null || filename.length() == 0) && (filename = (String)CacheUtil.get(this.connectionInfo.getDatabaseId(), "WebPageFilename", webpageid + ";" + productedition)) == null) {
                String pageSelect = "SELECT\tfilename, location FROM\twebpage WHERE\twebpageid = ? and productedition= ?";
                db.createPreparedResultSet(pageSelect, new Object[]{webpageid, productedition});
                filename = "";
                if (db.getNext()) {
                    String string = db.getString("location");
                    if (string != null && string.length() > 0) {
                        filename = string;
                    }
                    filename = filename + db.getString("filename");
                    CacheUtil.put(this.connectionInfo.getDatabaseId(), "WebPageFilename", webpageid + ";" + productedition, filename);
                }
            }
            if ((props = (ArrayList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "WebPageProperties", webpageid + ";" + productedition)) == null) {
                props = new ArrayList();
                String string = "SELECT\tpropertyid, propertyvalue FROM\twebpageproperty WHERE\twebpageid = ? AND productedition = ? ";
                db.createPreparedResultSet(string, new Object[]{webpageid, productedition});
                while (db.getNext()) {
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("propertyid", db.getString("propertyid"));
                    m.put("propertyvalue", db.getString("propertyvalue"));
                    props.add(m);
                }
                CacheUtil.put(this.connectionInfo.getDatabaseId(), "WebPageProperties", webpageid + ";" + productedition, props);
            }
            boolean bl = false;
            while (var22_33 < props.size()) {
                HashMap prop = (HashMap)props.get((int)var22_33);
                webPageProps.setProperty((String)prop.get("propertyid"), (String)prop.get("propertyvalue"));
                ++var22_33;
            }
            this.logInfo("Accessing web page '" + webpageid + "(" + productedition + ")' with reference: " + filename);
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get webpage properties. Exception: " + se.getMessage(), se);
        }
        catch (Exception e) {
            throw new ServiceException("COMPONENT_ACCESS_FAILURE", "Failed to access webadmin component. Exception: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
        webPageProps.setProperty(WEBPAGE_FILENAME, filename);
        webPageProps.setProperty(WEBPAGE_PRODUCTEDITION, productedition);
        webPageProps.setDbms(this.sapphireConnection.getDbms());
        webPageProps.setDatabaseid(this.sapphireConnection.getDatabaseId());
        PropertyList returnProps = filterProperties ? webPageProps.copy(languageid, translationProcessor, this.sapphireConnection.getRoleList(), this.sapphireConnection.getModuleList(), this.getInactiveRoles(), this.sapphireConnection.getGuiMode(), productedition.equalsIgnoreCase("stellar")) : webPageProps.copy(languageid, translationProcessor, "<ALL>", "<ALL>", null, this.sapphireConnection.getGuiMode(), productedition.equalsIgnoreCase("stellar"));
        try {
            String userid = this.sapphireConnection.getSysuserId();
            String userOverrides = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "WebPageUserOverrides", webpageid + ";" + productedition + ";" + userid);
            if (userOverrides == null) {
                userOverrides = webadmin.getUserOverridesAsPropertyList(userid, webpageid, productedition);
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageUserOverrides", webpageid + ";" + productedition + ";" + userid, userOverrides);
            }
            returnProps.setPropertyList(userOverrides, true);
        }
        catch (Exception e) {
            this.logError("Failed to apply user overrides to page " + webpageid + ". Ignoring error and continuing", e);
        }
        return returnProps;
    }

    public String getPropertyTreeValueCached(String propertytreeid) throws ServiceException {
        String propertyTreeXML = (String)CacheUtil.get(this.connectionInfo.getDatabaseId(), "PropertyTree", propertytreeid);
        if (propertyTreeXML == null || propertyTreeXML.length() == 0) {
            this.logDebug("Getting property tree '" + propertytreeid + "' from database...");
            propertyTreeXML = this.getPropertyTreeValue(propertytreeid);
            CacheUtil.put(this.connectionInfo.getDatabaseId(), "PropertyTree", propertytreeid, propertyTreeXML);
        }
        return propertyTreeXML;
    }

    public PropertyDefinitionList getPropertyDefinitionListCached(String propertytreeid) throws ServiceException {
        PropertyDefinitionList propertyDefinitionList = (PropertyDefinitionList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "PropertyTreeDefinition", propertytreeid);
        if (propertyDefinitionList == null) {
            this.logDebug("Getting property tree definition '" + propertytreeid + "' from database...");
            propertyDefinitionList = this.getPropertyTreeDefinitionList(propertytreeid);
            CacheUtil.put(this.connectionInfo.getDatabaseId(), "PropertyTreeDefinition", propertytreeid, propertyDefinitionList);
        }
        return propertyDefinitionList;
    }

    public String getDefaultWebPageEdition(String webpageid) throws ServiceException {
        String productedition = "";
        String sysProductEdition = new ConfigService(this.sapphireConnection).getSysConfigProperty("productedition");
        String databaseid = this.sapphireConnection.getDatabaseId();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            if (CacheUtil.get(databaseid, "DefaultPageEdition", webpageid) == null) {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("SELECT webpageid, productedition, defaultpageflag, (SELECT count(*) FROM webpage w WHERE w.webpageid=p.webpageid and w.productedition<>'Stellar') pagecount FROM webpage p WHERE webpageid=? and productedition<>'Stellar' order by webpageid, productedition", webpageid);
                DataSet pageDs = new DataSet(db.getResultSet());
                if (pageDs.getRowCount() > 0) {
                    for (int i = 0; i < pageDs.getRowCount(); ++i) {
                        String id = pageDs.getString(i, "webpageid");
                        String edition = pageDs.getString(i, "productedition");
                        int pagecount = pageDs.getInt(i, "pagecount");
                        if (pagecount > 1) {
                            if ("Y".equals(pageDs.getString(i, "defaultpageflag"))) {
                                CacheUtil.put(databaseid, "DefaultPageEdition", id, edition);
                                continue;
                            }
                            if (CacheUtil.get(databaseid, "DefaultPageEdition", webpageid) != null || !sysProductEdition.equals(edition)) continue;
                            CacheUtil.put(databaseid, "DefaultPageEdition", id, edition);
                            continue;
                        }
                        CacheUtil.put(databaseid, "DefaultPageEdition", id, edition);
                    }
                }
            }
            if ((productedition = (String)CacheUtil.get(databaseid, "DefaultPageEdition", webpageid)) == null) {
                productedition = "R5";
                CacheUtil.put(databaseid, "DefaultPageEdition", webpageid, "R5");
            }
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        finally {
            db.reset();
        }
        return productedition;
    }

    public PropertyList getWebPageProperties(String webpageid, PropertyList requestProps) throws ServiceException {
        return this.getWebPageProperties(webpageid, this.getDefaultWebPageEdition(webpageid), requestProps, true);
    }

    public PropertyList getBulletinProperties(String webpageid, String bulletinid, PropertyList requestProps) throws ServiceException {
        this.logDebug("Getting bulletin properties for bulletin '" + bulletinid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("UPDATE bulletinsysuser SET readflag = 'Y', readdt = ? WHERE bulletinid = ? AND sysuserid = ?", new Object[]{DateTimeUtil.getNowTimestamp(), bulletinid, this.sapphireConnection.getSysuserId()});
            PropertyList propertyList = this.getWebPageProperties(webpageid, requestProps);
            return propertyList;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to set bulletin " + bulletinid + " to read for " + this.sapphireConnection.getSysuserId() + ". Exception: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public PropertyList getHistoryProperties(String webpagelogid, PropertyList requestProps) throws ServiceException {
        this.logDebug("Getting history page properties '" + webpagelogid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT webpagerequest, propertyclob FROM webpagelog WHERE webpagelogid = ?", webpagelogid);
            if (!db.getNext()) throw new ServiceException("INVALID_PARAMETER", "Failed to find WebPageRequest for webpagelogid '" + webpagelogid + "'");
            String[] parts = StringUtil.split(db.getString("webpagerequest"), "&");
            String[] request = StringUtil.split(parts[0], "=");
            if (request != null && request.length == 2) {
                PropertyList historyProps;
                String properties = db.getClob("propertyclob");
                PropertyList logProps = new PropertyList();
                if (properties != null && properties.length() > 0) {
                    logProps.setPropertyList(properties);
                }
                if (logProps.getProperty("currentlayout").length() > 0) {
                    requestProps.setProperty("currentlayout", logProps.getProperty("currentlayout"));
                    requestProps.setProperty("currentlayoutnode", logProps.getProperty("currentlayoutnode"));
                }
                if (request[0].equals("page")) {
                    historyProps = this.getWebPageProperties(request[1], requestProps);
                    historyProps.setProperty("page", request[1]);
                    historyProps.setProperty("registeredpage", "true");
                } else {
                    historyProps = this.getConnectionProperties();
                    historyProps.setProperty("file", request[1]);
                }
                for (int i = 1; i < parts.length; ++i) {
                    String[] partParts = StringUtil.split(parts[i], "=");
                    if (partParts.length != 2) continue;
                    historyProps.setProperty(partParts[0], partParts[1]);
                }
                PropertyList pageData = historyProps.getPropertyList("pagedata");
                Set keyset = logProps.keySet();
                for (String propertyid : keyset) {
                    String value = logProps.getProperty(propertyid);
                    historyProps.setProperty(propertyid, value);
                    if (pageData == null) continue;
                    pageData.setProperty(propertyid, value);
                }
                PropertyList propertyList = historyProps;
                return propertyList;
            }
            try {
                throw new ServiceException("INVALID_PARAMETER", "WebPageRequest for webpagelogid '" + webpagelogid + "' is invalid");
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get history page. Exception: " + se.getMessage(), se);
            }
        }
        finally {
            db.reset();
        }
    }

    public PropertyList getFavoriteProperties(String favoriteid, PropertyList requestProps) throws ServiceException {
        this.logDebug("Getting favorite page properties '" + favoriteid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT typeflag, webpageid, url FROM sysuserfavorite WHERE sysuserid = ? AND favoriteid = ?", new Object[]{this.connectionInfo.getSysuserId(), favoriteid});
            if (db.getNext()) {
                String type = db.getString("typeflag");
                if (type.equals("W")) {
                    String webpageid = db.getString("webpageid");
                    PropertyList favoriteProps = this.getWebPageProperties(webpageid, requestProps);
                    favoriteProps.setProperty(FAVORITES_WEBPAGEID, webpageid);
                    PropertyList propertyList = favoriteProps;
                    return propertyList;
                }
                PropertyList favoriteProps = new PropertyList();
                favoriteProps.setProperty(FAVORITES_URL, db.getString("url"));
                PropertyList propertyList = favoriteProps;
                return propertyList;
            }
            try {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to find favorite '" + favoriteid + "'");
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get properties for favorite '" + favoriteid + "'", se);
            }
        }
        finally {
            db.reset();
        }
    }

    public PropertyList addPropertyData(PropertyList requestProps) throws ServiceException {
        this.logDebug("Adding property data");
        PropertyList additionalProps = new PropertyList();
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            PropertyList row;
            PropertyListCollection data;
            int rowCount;
            CharSequence select;
            dbu.setConnection(this.sapphireConnection);
            String sdcid = requestProps.getProperty("sdcid");
            if (sdcid.length() > 0) {
                DDTService ddt = new DDTService(this.sapphireConnection);
                additionalProps = ddt.getSDCProperties(sdcid);
            }
            if (requestProps.getProperty("showbulletins").equals("Y")) {
                select = this.connectionInfo.getDbms().equals("MSS") ? "SELECT TOP 10 bulletin.bulletinid, bulletin.bulletindesc, bulletin.priorityflag, bulletinsysuser.readflag FROM\tbulletin, bulletinsysuser WHERE\tbulletin.bulletinid = bulletinsysuser.bulletinid AND \t\tbulletinsysuser.readflag = 'N' AND \t\tbulletinsysuser.deletedflag = 'N' AND \t\tbulletinsysuser.sysuserid = ? ORDER BY bulletinsysuser.readflag, bulletin.priorityflag DESC, bulletin.createdt DESC" : "SELECT * FROM (SELECT bulletin.bulletinid, bulletin.bulletindesc, bulletin.priorityflag, bulletinsysuser.readflag FROM\tbulletin, bulletinsysuser WHERE\tbulletin.bulletinid = bulletinsysuser.bulletinid AND \t\tbulletinsysuser.readflag = 'N' AND \t\tbulletinsysuser.deletedflag = 'N' AND \t\tbulletinsysuser.sysuserid = ? ORDER BY bulletinsysuser.readflag, bulletin.priorityflag DESC, bulletin.createdt DESC) WHERE ROWNUM < 11 ";
                dbu.createPreparedResultSet((String)select, this.connectionInfo.getSysuserId());
                rowCount = 0;
                data = new PropertyListCollection();
                while (dbu.getNext()) {
                    row = new PropertyList("row_" + rowCount++);
                    row.setProperty("bulletinid", dbu.getString("bulletinid"));
                    row.setProperty("bulletindesc", dbu.getString("bulletindesc"));
                    row.setProperty("priorityflag", dbu.getString("priorityflag"));
                    row.setProperty("readflag", dbu.getString("readflag"));
                    data.add(row);
                }
                additionalProps.setProperty("bulletins", data);
            }
            if (requestProps.getProperty("showfavorites").equals("Y")) {
                select = "SELECT\tfavoriteid, favoritedesc FROM\tsysuserfavorite WHERE\tsysuserid = ? " + (this.connectionInfo.getDbms().equals("MSS") ? "" : "AND ROWNUM < 11 ") + "ORDER BY usersequence, favoriteid";
                dbu.createPreparedResultSet((String)select, this.connectionInfo.getSysuserId());
                rowCount = 0;
                data = new PropertyListCollection();
                while (dbu.getNext()) {
                    row = new PropertyList("row_" + rowCount++);
                    row.setProperty("favoriteid", dbu.getString("favoriteid"));
                    row.setProperty("favoritedesc", dbu.getString("favoritedesc"));
                    data.add(row);
                }
                additionalProps.setProperty("favorites", data);
            }
            if (requestProps.getProperty("showhistory").equals("Y")) {
                select = new StringBuffer("SELECT\twt.webpagelogid, wt.title, wt.tip FROM\twebpagelogtitle wt,webpagelog w WHERE\tw.webpagelogid IN ( ");
                if (this.connectionInfo.getDbms().equals("MSS")) {
                    ((StringBuffer)select).append("( SELECT TOP 10 webpagelogid   FROM   webpagelog   WHERE  sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) ");
                } else {
                    ((StringBuffer)select).append("( SELECT webpagelogid   FROM ( SELECT webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC )   WHERE rownum < 11 ) ");
                }
                ((StringBuffer)select).append(") AND w.webpagelogid = wt.webpagelogid ORDER BY w.requestdt DESC");
                dbu.createPreparedResultSet(((StringBuffer)select).toString(), this.connectionInfo.getSysuserId());
                rowCount = 0;
                data = new PropertyListCollection();
                while (dbu.getNext()) {
                    row = new PropertyList("row_" + rowCount++);
                    row.setProperty("webpagelogid", dbu.getString("webpagelogid"));
                    row.setProperty("title", dbu.getString("title"));
                    row.setProperty("tip", dbu.getString("tip"));
                    data.add(row);
                }
                additionalProps.setProperty("recentitems", data);
            }
            PropertyList propertyList = additionalProps;
            return propertyList;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Error adding property data: " + se.getMessage(), se);
        }
        finally {
            dbu.reset();
        }
    }

    public String getPageValueTree(String webpageid, String productedition, String ptreeid, String elementid) throws ServiceException {
        this.logDebug("Getting webpage value tree");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT valuetree FROM webpagepropertytree WHERE webpageid = ? AND productedition = ? AND propertytreeid = ? AND elementid = ?", new Object[]{webpageid, productedition, ptreeid, elementid});
            String valueTree = null;
            if (db.getNext()) {
                valueTree = db.getClob("valuetree");
            }
            String string = valueTree == null || valueTree.trim().length() == 0 ? "<propertylist/>" : valueTree;
            return string;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the valuetree: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public String getPageProductValueTree(String webpageid, String productedition, String ptreeid, String elementid) throws ServiceException {
        this.logDebug("Getting webpage product value tree");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT productvaluetree FROM webpagepropertytree WHERE webpageid = ? AND productedition = ? AND propertytreeid = ? AND elementid = ?", new Object[]{webpageid, productedition, ptreeid, elementid});
            String valueTree = null;
            if (db.getNext()) {
                valueTree = db.getClob("productvaluetree");
            }
            String string = valueTree == null || valueTree.trim().length() == 0 ? "<propertylist/>" : valueTree;
            return string;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the valuetree: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public String getPropertyTreeValue(String propertytreeid) throws ServiceException {
        this.logDebug("Getting propertytree value tree for " + propertytreeid);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String string = PropertyTreeUtil.getPropertyTreeValue(db, propertytreeid, true);
            return string;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the valuetree: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public PropertyDefinitionList getPropertyTreeDefinitionList(String propertytreeid) throws ServiceException {
        this.logDebug("Getting propertytree definition tree for " + propertytreeid);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            PropertyDefinitionList propertyDefinitionList = PropertyTreeUtil.getPropertyTreeDefinitionList(db, propertytreeid);
            return propertyDefinitionList;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the definitiontree: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public String getPropertyTreeObject(String propertytreeid) throws ServiceException {
        this.logDebug("Getting propertytree object for " + propertytreeid);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT objectname FROM propertytree WHERE propertytreeid = ?", propertytreeid);
            if (db.getNext()) {
                String string = db.getString("objectname") != null ? db.getString("objectname") : "";
                return string;
            }
            try {
                throw new ServiceException("INVALID_PARAMETER", "Failed to find propertytree '" + propertytreeid + "'");
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Could not lookup the property tree object: " + se.getMessage(), se);
            }
        }
        finally {
            db.reset();
        }
    }

    public HashMap processRequest(String requestHandler, HashMap requestProps) throws ServiceException {
        this.logInfo("Processing request using request handler '" + requestHandler + "'");
        if (requestHandler == null || requestHandler.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Request handler class not specified.");
        }
        try {
            Class<?> c = Class.forName(requestHandler);
            PropertyHandler handler = (PropertyHandler)c.newInstance();
            handler.setSapphireConnection(this.sapphireConnection);
            handler.processProperties(requestProps);
            return requestProps;
        }
        catch (Exception e) {
            throw new ServiceException("GENERAL_ERROR", "Failed to process property handler " + requestHandler, e);
        }
    }

    public void logPageAccess(String request, String title, String tip, HashMap requestProps) throws ServiceException {
    }

    public String logPageAccess(String request, String title, String tip, HashMap requestProps, boolean state) throws ServiceException {
        this.logDebug("Logging page access for request '" + request + "' with title '" + title + "'");
        String out = "";
        if (request == null || request.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Request not specified");
        }
        if (title == null || title.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Title not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            if (tip == null || tip.length() == 0) {
                tip = title;
            }
            boolean doupdate = false;
            if (!state) {
                String pageSelect = "SELECT\twebpagelog.webpagelogid, propertyclob FROM\twebpagelog, webpagelogtitle WHERE\twebpagelog.webpagelogid = webpagelogtitle.webpagelogid AND webpagelog.sysuserid = ? AND \t\ttitle = ? AND \t\ttip = ?";
                db.createPreparedResultSet(pageSelect, new String[]{this.connectionInfo.getSysuserId(), title, tip});
                doupdate = db.getNext();
            }
            if (doupdate) {
                String historyPropertiesStr = db.getString("propertyclob");
                out = db.getString("webpagelogid");
                String update = "UPDATE webpagelog SET \trequestcount = requestcount + 1, \trequestdt = ?    WHERE webpagelogid = ?";
                this.logDebug("Executing: " + update);
                db.executePreparedUpdate(update, new Object[]{now, out});
                PropertyList props = new PropertyList();
                props.putAll(requestProps);
                String properties = props.toXMLString();
                if (historyPropertiesStr != null && historyPropertiesStr.length() > 0) {
                    PropertyList historyProps = new PropertyList();
                    historyProps.setPropertyList(historyPropertiesStr);
                    String historyCommand = historyProps.getProperty("command");
                    String historyCommandValue = historyProps.getProperty(historyCommand);
                    if (!historyCommandValue.equals(props.getProperty(historyCommand))) {
                        db.updateClob("webpagelog", "propertyclob", properties, new String[]{"webpagelogid"}, new String[]{out});
                    }
                } else {
                    db.updateClob("webpagelog", "propertyclob", properties, new String[]{"webpagelogid"}, new String[]{out});
                }
            } else {
                SequenceProcessor seq = this.getSequenceProcessor();
                int seqnum = seq.getSequence("WebPage", "recentitems");
                if (seqnum > -1) {
                    this.logDebug("Inserting new webpagelog records...");
                    PropertyList props = new PropertyList();
                    props.putAll(requestProps);
                    String properties = props.toXMLString();
                    out = String.valueOf(seqnum);
                    db.executePreparedUpdate("INSERT INTO webpagelog ( webpagelogid, webpagerequest, sysuserid, requestcount, requestdt, logtypeflag, connectionid ) VALUES ( ?, ?, ?, 1, ?, 'N', ? )", new Object[]{out, request, this.connectionInfo.getSysuserId(), now, this.connectionInfo.getConnectionId()});
                    db.updateClob("webpagelog", "propertyclob", properties, new String[]{"webpagelogid"}, new String[]{String.valueOf(seqnum)});
                    db.executePreparedUpdate("INSERT INTO webpagelogtitle ( webpagelogid, sysuserid, title, tip ) VALUES ( ?, ?, ?, ? )", new Object[]{String.valueOf(seqnum), this.connectionInfo.getSysuserId(), title, tip});
                }
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to log page access for request '" + request + "' and title '" + title + "'", e);
        }
        finally {
            db.reset();
        }
        return out;
    }

    public void loadWebPageCache() throws ServiceException {
        this.logDebug("Loading web page cache");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            WebAdminService was = new WebAdminService(this.sapphireConnection);
            was.resetCache("WebPageDesigner", "");
            db.createResultSet("loadwebpages", "SELECT webpageid FROM webpage order by webpageid");
            PropertyList props = new PropertyList();
            props.setProperty("findmissingtransmastertext", "Y");
            while (db.getNext("loadwebpages")) {
                this.getWebPageProperties(db.getString("loadwebpages", "webpageid"), props);
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to load web page property cache", e);
        }
        finally {
            db.closeResultSet("loadwebpages");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Set<String> getInactiveRoles() throws ServiceException {
        Set<String> inactiveRoleList = inactiveRoleListDataBase.get(this.connectionInfo.getDatabaseId());
        if (inactiveRoleList == null) {
            Object object = inactiveRoleListLock;
            synchronized (object) {
                inactiveRoleList = new HashSet<String>();
                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                try {
                    db.setConnection(this.sapphireConnection);
                    db.createResultSet("loadinactiveroles", "SELECT roleid FROM role WHERE activeflag='N' order by roleid");
                    while (db.getNext("loadinactiveroles")) {
                        inactiveRoleList.add(db.getString("loadinactiveroles", "roleid"));
                    }
                }
                catch (Exception e) {
                    throw new ServiceException("DB_ACTION_FAILED", "Failed to load web page property cache", e);
                }
                finally {
                    db.closeResultSet("loadinactiveroles");
                }
                inactiveRoleListDataBase.put(this.connectionInfo.getDatabaseId(), inactiveRoleList);
            }
        }
        return inactiveRoleList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flushInactiveRols(String databaseid) {
        Object object = inactiveRoleListLock;
        synchronized (object) {
            inactiveRoleListDataBase.put(databaseid, null);
        }
    }

    public String processFileCommand(String fileName) throws ServiceException {
        String webpage;
        HashMap<String, String> registeredPageMap;
        if (fileName != null && fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        if ((registeredPageMap = (HashMap<String, String>)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "FileRegisteredPageCache", "FileRegisteredPageCacheMap")) == null) {
            registeredPageMap = new HashMap<String, String>();
            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "FileRegisteredPageCache", "FileRegisteredPageCacheMap", registeredPageMap);
        }
        if (!registeredPageMap.containsKey(fileName)) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createResultSet("loadjspfilepages", "SELECT webpageid, productedition, location, filename FROM webpage WHERE location is not null and filename is not null");
                while (db.getNext("loadjspfilepages")) {
                    String file = db.getString("loadjspfilepages", "location") + db.getString("loadjspfilepages", "filename");
                    if (registeredPageMap.containsKey(file)) continue;
                    registeredPageMap.put(file, db.getString("loadjspfilepages", "webpageid") + ";" + db.getString("loadjspfilepages", "productedition"));
                }
            }
            catch (Exception e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to load web page property cache", e);
            }
            finally {
                db.closeResultSet("loadjspfilepages");
            }
        }
        if ((webpage = registeredPageMap.getOrDefault(fileName, "")).length() == 0) {
            throw new ServiceException("The file must be a registered Page");
        }
        String[] s = StringUtil.split(webpage, ";");
        String webpageid = s[0];
        String productedition = s[1];
        this.validateUserWebPageAccess(webpageid, productedition);
        return webpageid;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void validateUserWebPageAccess(String webpageid, String productedition) throws ServiceException {
        if ("Stellar".equals(productedition)) {
            return;
        }
        String accesserror = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition);
        if (accesserror != null) {
            if (accesserror.length() <= 0) return;
            throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - " + accesserror);
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            block16: {
                db.setConnection(this.sapphireConnection);
                SDIRequest webpageRequest = new SDIRequest();
                webpageRequest.setSDIList("WebPage", webpageid, productedition, "");
                webpageRequest.setRequestItem("primary");
                QueryService qs = new QueryService(this.sapphireConnection);
                SDIData sdidata = qs.getSDIData(webpageRequest);
                if (sdidata == null) {
                    throw new SapphireException("Could not fetch sdidata for webpageid:" + webpageid);
                }
                DataSet webpage = sdidata.getDataset("primary");
                if (webpage.size() == 0) {
                    db.createPreparedResultSet("dbwebpageid", "SELECT webpageid FROM webpage WHERE webpageid=? AND productedition=?", new Object[]{webpageid, productedition});
                    if (!db.getResultSet("dbwebpageid").next()) {
                        CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "page does not exist");
                        throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - page does not exist");
                    }
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "insufficient privileges to access page");
                    throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - insufficient privileges to access page");
                }
                if (this.sapphireConnection.getUserType().equals("V") || this.sapphireConnection.getUserType().equals("I")) {
                    if (!webpage.getValue(0, "virtualpageflag").equals("Y")) {
                        CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "this is not a virtual page and is not available to you");
                        throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - this is not a virtual page and is not available to you");
                    }
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "");
                } else {
                    if (this.sapphireConnection.getUserType().equals("E") || this.sapphireConnection.getUserType().equals("X")) {
                        String expresspage = webpage.getValue(0, "expresspage");
                        if (expresspage.length() > 0 && expresspage.equals(EncryptDecrypt.encodePageid(webpageid + productedition))) {
                            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "");
                            break block16;
                        } else {
                            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "this is not an Express page and is not available to you");
                            throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - this is not an Express page and is not available to you");
                        }
                    }
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "");
                }
            }
            db.createPreparedResultSet("dbsdimodule", "SELECT webpageid FROM webpage WHERE webpageid = ? AND productedition = ? AND ( EXISTS ( SELECT null FROM sdimodule WHERE sdimodule.sdcid='WebPage' AND sdimodule.keyid1 = ? AND sdimodule.keyid2 = ? AND  sdimodule.moduleid in ( SELECT moduleid FROM modulesysuser WHERE sysuserid = ? UNION SELECT moduleid FROM module WHERE maxusers = 'S' ) ) OR NOT EXISTS( SELECT null FROM sdimodule WHERE sdimodule.sdcid='WebPage' and sdimodule.keyid1 = ? and sdimodule.keyid2 = ? ) )", new Object[]{webpageid, productedition, webpageid, productedition, this.sapphireConnection.getSysuserId(), webpageid, productedition});
            if (db.getNext("dbsdimodule")) return;
            CacheUtil.put(this.sapphireConnection.getDatabaseId(), "WebPageAccess", this.sapphireConnection.getConnectionId() + ";" + webpageid + ";" + productedition, "you do not have access to the module of this page");
            throw new ServiceException("PAGE_ACCESS_FAILURE", "Page access denied - you do not have access to the module of this page: " + webpageid + " edition: " + productedition);
        }
        catch (Exception e) {
            throw new ServiceException("REQUEST_SERVICE_ERROR", "Failed to determine access rights to webpageid '" + webpageid + "'", e);
        }
        finally {
            db.closeResultSet("dbwebpageid");
            db.closeResultSet("dbsdimodule");
        }
    }
}

