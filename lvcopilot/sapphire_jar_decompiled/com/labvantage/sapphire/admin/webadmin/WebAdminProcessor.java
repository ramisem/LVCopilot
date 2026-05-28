/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;
import com.labvantage.sapphire.ejb.WebAdminManagerLocal;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WebAdminProcessor
extends BaseAccessor
implements CacheNames {
    File rakFile = null;

    public WebAdminProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public WebAdminProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        this.rakFile = rakFile;
    }

    public WebAdminProcessor(String connectionid) {
        super(connectionid);
    }

    public void deletePropertyTree(String ptreeid) throws Exception {
        this.getWebAdminManager().deletePropertyTree(this.getConnectionid(), ptreeid);
    }

    public void synchronizePropertyTree(String propertytreeid) throws Exception {
        this.getWebAdminManager().synchronizePropertyTree(this.getConnectionid(), propertytreeid);
    }

    public org.w3c.dom.Node loadPropertyTreeValues(String ptreeid) throws Exception {
        return WebAdminProcessor.getPropertyTreeNode(DOMUtil.getNewDocument(this.getWebAdminManager().getPropertyTreeValue(this.getConnectionid(), ptreeid), false));
    }

    public String getPropertyTreeValues(String ptreeid) throws Exception {
        String xml;
        String string = xml = local ? this.getLocalAccessManager().getPropertyTreeValue(this.getConnectionid(), ptreeid) : this.getRemoteAccessManager().getPropertyTreeValue(this.getConnectionid(), ptreeid);
        if (xml != null && xml.length() > 0) {
            return xml;
        }
        throw new SapphireException("Unable to locate propertytree " + ptreeid);
    }

    public Set<String> getInactiveRoleList() throws Exception {
        return local ? this.getLocalAccessManager().getInactiveRoleList(this.getConnectionid()) : this.getRemoteAccessManager().getInactiveRoleList(this.getConnectionid());
    }

    public String getPropertyTreeObject(String ptreeid) throws Exception {
        return this.getWebAdminManager().getPropertyTreeObject(this.getConnectionid(), ptreeid);
    }

    public org.w3c.dom.Node loadPropertyTreeDefinition(String ptreeid) throws Exception {
        return WebAdminProcessor.getPropertyTreeNode(DOMUtil.getNewDocument(this.getPropertyTreeDefinition(ptreeid), true));
    }

    public String getPropertyTreeDefinition(String ptreeid) throws Exception {
        return this.getWebAdminManager().getPropertyTreeDef(this.getConnectionid(), ptreeid);
    }

    public PropertyDefinitionList getPropertyDefinitionList(String propertyTreeid) throws Exception {
        ConnectionProcessor cp = this.rakFile == null ? new ConnectionProcessor(this.getConnectionid()) : new ConnectionProcessor(this.rakFile, this.getConnectionid());
        ConnectionInfo connectionInfo = cp.getConnectionInfo(this.getConnectionid());
        PropertyDefinitionList propertyDefinitionList = (PropertyDefinitionList)CacheUtil.get(connectionInfo.getDatabaseId(), "PropertyTreeDefinition", propertyTreeid);
        if (propertyDefinitionList == null) {
            String definitionXml = local ? this.getLocalAccessManager().getPropertyTreeDef(this.getConnectionid(), propertyTreeid) : this.getRemoteAccessManager().getPropertyTreeDef(this.getConnectionid(), propertyTreeid);
            PropertyTree tree = new PropertyTree();
            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
            handler.setXMLString(definitionXml);
            handler.setPrintStream(null);
            SaxUtil.parseString(handler);
            propertyDefinitionList = tree.getPropertyDefinitionList();
            CacheUtil.put(connectionInfo.getDatabaseId(), "PropertyTreeDefinition", propertyTreeid, propertyDefinitionList);
        }
        return propertyDefinitionList;
    }

    public Element loadPageValues(String pageid, String productedition, String ptreeid, String elementid) throws Exception {
        Document d = DOMUtil.getNewDocument(this.getWebAdminManager().getPageValueTree(this.getConnectionid(), pageid, productedition, ptreeid, elementid), false);
        return d == null ? null : d.getDocumentElement();
    }

    public boolean isProductPage(String pageid, String productedition) throws Exception {
        return this.getWebAdminManager().isProductPage(this.getConnectionid(), pageid, productedition);
    }

    public Element loadPageProductValues(String pageid, String productedition, String ptreeid, String elementid) throws Exception {
        Document d = DOMUtil.getNewDocument(this.getWebAdminManager().getPageProductValueTree(this.getConnectionid(), pageid, productedition, ptreeid, elementid), false);
        return d == null ? null : d.getDocumentElement();
    }

    public void savePropertyTreeValues(String ptreeid, org.w3c.dom.Node ptreenode) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(ptreenode));
        this.getWebAdminManager().setPropertyTreeValue(this.getConnectionid(), ptreeid, output.toString());
    }

    public void savePropertyTreeValues(String ptreeid, String xml) throws Exception {
        this.getWebAdminManager().setPropertyTreeValue(this.getConnectionid(), ptreeid, xml);
    }

    public void savePropertyTree(String ptreeid, org.w3c.dom.Node ptreenode, String nodeid, ArrayList deleteList, HashMap renameList) throws Exception {
        StringBuffer output = new StringBuffer("");
        if (ptreenode != null) {
            output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(ptreenode));
        }
        String[] deletes = new String[]{};
        if (deleteList != null) {
            deletes = deleteList.toArray(new String[0]);
        }
        String[] froms = new String[]{};
        String[] tos = new String[froms.length];
        if (renameList != null) {
            froms = renameList.keySet().toArray(new String[0]);
            tos = new String[froms.length];
            for (int i = 0; i < froms.length; ++i) {
                tos[i] = (String)renameList.get(froms[i]);
            }
        }
        this.getWebAdminManager().savePropertyTree(this.getConnectionid(), ptreeid, output.toString(), nodeid, deletes, froms, tos);
    }

    public void savePropertyTreeDefinition(String propertytreeid, org.w3c.dom.Node ptreenode) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(ptreenode));
        this.getWebAdminManager().setPropertyTreeDef(this.getConnectionid(), propertytreeid, output.toString());
    }

    public void savePropertyTreeDefinition(String propertytreeid, String xml) throws Exception {
        this.getWebAdminManager().setPropertyTreeDef(this.getConnectionid(), propertytreeid, xml);
    }

    public void savePageValues(String pageid, String productedition, String ptreeid, String elementid, org.w3c.dom.Node propertylist, ArrayList deleteList) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(propertylist));
        this.getWebAdminManager().setPageValueTree(this.getConnectionid(), pageid, productedition, ptreeid, elementid, output.toString(), deleteList);
    }

    public void savePageValues(String pageid, String productedition, String ptreeid, String elementid, PropertyList propertylist, ArrayList deleteList) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(propertylist.toXMLString());
        this.getWebAdminManager().setPageValueTree(this.getConnectionid(), pageid, productedition, ptreeid, elementid, output.toString(), deleteList);
    }

    public void saveUserOverrides(String pageid, String productedition, String ptreeid, String elementid, PropertyList propertylist) throws Exception {
        this.getWebAdminManager().setUserOverrides(this.getConnectionid(), pageid, productedition, ptreeid, elementid, propertylist);
    }

    public void clearUserOverrides() throws Exception {
        this.getWebAdminManager().clearUserOverrides(this.getConnectionid(), "", "", "", "");
    }

    public void clearUserOverrides(String pageid, String productedition) throws Exception {
        this.getWebAdminManager().clearUserOverrides(this.getConnectionid(), pageid, productedition, "", "");
    }

    public void clearUserOverrides(String pageid, String productedition, String ptreeid, String elementid) throws Exception {
        this.getWebAdminManager().clearUserOverrides(this.getConnectionid(), pageid, productedition, ptreeid, elementid);
    }

    public void moveWebPagePropertyTreePage(String pageid, String productedition, String elementid, String propertyTreeid, String newNodeid) throws Exception {
        this.getWebAdminManager().moveWebPagePropertyTreePage(this.getConnectionid(), pageid, productedition, elementid, propertyTreeid, newNodeid);
    }

    public void setComponentPageOverride(String pageid, String productedition, String propertyTreeid, String elementid, String compCode, String properties) throws Exception {
        this.getWebAdminManager().setComponentPageOverride(this.getConnectionid(), pageid, productedition, propertyTreeid, elementid, compCode, properties);
    }

    public void removeComponentPageOverride(String pageid, String productedition, String propertyTreeid, String elementid, String compCode) throws Exception {
        this.getWebAdminManager().removeComponentPageOverride(this.getConnectionid(), pageid, productedition, propertyTreeid, elementid, compCode);
    }

    public PropertyListCollection getComponentPageOverrides(String webpageid, String productedition, String propertyTreeid, String elementid) throws Exception {
        String values = this.getWebAdminManager().getComponentPageOverrides(this.getConnectionid(), webpageid, productedition, propertyTreeid, elementid);
        PropertyList ret = new PropertyList();
        if (values != null && values.length() > 0) {
            ret.setPropertyList(values);
        }
        return ret.getCollectionNotNull("components");
    }

    public void renameWebPagePropertyTreeNode(String propertytreeid, String oldnodeid, String newnodeid) throws Exception {
        this.getWebAdminManager().renameWebPagePropertyTreeNode(this.getConnectionid(), propertytreeid, oldnodeid, newnodeid);
    }

    public static org.w3c.dom.Node getPropertyTreeNode(Document document) throws TransformerException {
        Element documentelement;
        org.w3c.dom.Node ptreenode = null;
        if (document != null && (documentelement = document.getDocumentElement()) != null && (ptreenode = XPathAPI.selectSingleNode((org.w3c.dom.Node)documentelement, (String)"descendant-or-self::propertytree")) == null) {
            ptreenode = document.createElement("propertytree");
        }
        return ptreenode.cloneNode(true);
    }

    public void copyWebPage(String fromWebpageid, String toWebpageid) {
    }

    public boolean lockPage(PageContext pageContext, String pageid, String edition) {
        boolean lockok = true;
        QueryProcessor qp = new QueryProcessor(pageContext);
        String connectionid = qp.getConnectionid();
        DataSet locks = qp.getPreparedSqlDataSet("SELECT rset.rsetid FROM rset, rsetitems WHERE rset.rsetid=rsetitems.rsetid AND rset.connectionid=? AND rsetitems.lockstate=2 AND rsetitems.sdcid='WebPage' AND rsetitems.keyid1=? AND rsetitems.keyid2=?", (Object[])new String[]{connectionid, pageid, edition});
        if (locks.size() == 0) {
            DAMProcessor dam = new DAMProcessor(pageContext);
            StringHolder rsetidHolder = new StringHolder();
            int status = dam.createLockedRSet("WebPage", pageid, edition, "", rsetidHolder);
            if (status == 1) {
                pageContext.setAttribute("rsetid", (Object)rsetidHolder.value);
            }
            lockok = status == 1;
            pageContext.setAttribute("lockok", (Object)lockok);
        } else {
            String rsetid = locks.getString(0, "rsetid");
            pageContext.setAttribute("rsetid", (Object)rsetid);
            pageContext.setAttribute("lockok", (Object)true);
        }
        return lockok;
    }

    public boolean lockPropertyTree(PageContext pageContext, String propertytreeid) {
        DAMProcessor dam = new DAMProcessor(pageContext);
        StringHolder rsetidHolder = new StringHolder();
        int status = dam.createLockedRSet("PropertyTree", propertytreeid, "", "", rsetidHolder);
        if (status == 1) {
            pageContext.setAttribute("rsetid", (Object)rsetidHolder.value);
        }
        pageContext.setAttribute("lockok", (Object)(status == 1 ? 1 : 0));
        return status == 1;
    }

    public boolean isCore(String ptreeid) {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = new QueryProcessor(this.rakFile, this.getConnectionid());
        DataSet dsptree = qp.getPreparedSqlDataSet("select coreflag from propertytree where propertytreeid=" + safeSQL.addVar(ptreeid), safeSQL.getValues());
        boolean coreflag = false;
        if (dsptree.getRowCount() == 1) {
            coreflag = dsptree.getValue(0, "coreflag").equals("Y");
        }
        return coreflag;
    }

    public String getCompCode(String ptreeid) {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = new QueryProcessor(this.rakFile, this.getConnectionid());
        DataSet dsptree = qp.getPreparedSqlDataSet("select compcode from propertytree where propertytreeid=" + safeSQL.addVar(ptreeid), safeSQL.getValues());
        if (dsptree.getRowCount() == 1) {
            return dsptree.getValue(0, "compcode");
        }
        return "";
    }

    public boolean supportsInheritance(String ptreeid) {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = new QueryProcessor(this.rakFile, this.getConnectionid());
        DataSet dsptree = qp.getPreparedSqlDataSet("select inheritanceflag from propertytree where propertytreeid=" + safeSQL.addVar(ptreeid), safeSQL.getValues());
        boolean inheritanceflag = true;
        if (dsptree.getRowCount() == 1) {
            inheritanceflag = !dsptree.getValue(0, "inheritanceflag").equals("N");
        }
        return inheritanceflag;
    }

    public void createPropertyTree(String ptreeid, String ptreetype, String ptreedesc, String objectname) throws Exception {
        WebAdminManagerLocal wa = this.getWebAdminManager();
        wa.addPropertyTree(this.getConnectionid(), ptreeid, ptreetype, ptreedesc, objectname);
        if (!ptreetype.equalsIgnoreCase("Step")) {
            String startupDefinition = ptreetype.equals("Gizmo") ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?><propertytree>\n  <propertydeplist />\n  <propertydeflist>\n    <propertydef editor=\"PropertyListEditor\" help=\"\" id=\"gizmoprops\" showif=\"\" title=\"Common Properties\" type=\"propertylist\">\n      <propertydeflist>\n        <propertydef editor=\"StringEditor\" help=\"Title of Gizmo\" id=\"title\" showif=\"\" title=\"Title\" translate=\"Y\" type=\"simple\" />\n        <propertydef editor=\"StringEditor\" help=\"Tool tip for Gizmo\" id=\"helptext\" showif=\"\" title=\"Help Text\" translate=\"Y\" type=\"simple\" />\n        <propertydef editor=\"ColorEditor\" help=\"Color of the title text\" id=\"titlecolor\" showif=\"\" title=\"Title Color\" expression=\"Y\" type=\"simple\" />\n        <propertydef editor=\"ImageLookupEditor\" help=\"Image for gizmo\" id=\"image\" showif=\"\" title=\"Image\" expression=\"Y\" type=\"simple\" />\n        <propertydef editor=\"YesNoEditor\" help=\"Set to No to hide the Gizmo\" id=\"visible\" showif=\"\" title=\"Show Gizmo\" type=\"simple\" />\n        <propertydef editor=\"StringEditor\" help=\"Set a refresh rate in seconds\" id=\"refreshevery\" regex=\"/(^-?\\d\\d*$)/\" regexerror=\"Please enter a number for minutes between refreshes\" showif=\"\" title=\"Refresh on Timer (secs)\" type=\"simple\" />\n        <propertydef editor=\"NotificationsEditor\" help=\"\" id=\"notifications\" showif=\"\" title=\"Refresh on Event\" type=\"simple\" />\n        <propertydef editor=\"ColorEditor\" help=\"Sets the color of the counter notification\" id=\"notifycolor\" showif=\"\" title=\"Count Color\" expression=\"Y\" type=\"simple\" />\n        <propertydef editor=\"YesNoEditor\" help=\"Sets the image to flash when an update orrurs\" id=\"flashonupdate\" showif=\"\" title=\"Flash on Update\" type=\"simple\" />\n      </propertydeflist>\n    </propertydef>\n  </propertydeflist>\n</propertytree>" : "<?xml version=\"1.0\" encoding=\"UTF-8\"?><propertytree><propertydeplist/><propertydeflist/></propertytree>";
            wa.setPropertyTreeDef(this.getConnectionid(), ptreeid, startupDefinition);
            String valuetree = ptreetype.equals("WorksheetItem") ? "<propertytree>\n  <nodelist>\n    <node id=\"Sapphire Product\" locked=\"Y\">\n      <propertylist id=\"root\" />\n      <nodelist>\n        <node id=\"Sapphire Custom\">\n          <propertylist id=\"root\" />\n        </node>\n      </nodelist>\n    </node>\n  </nodelist>\n</propertytree>" : "<?xml version=\"1.0\" encoding=\"UTF-8\"?><propertytree><nodelist/></propertytree>";
            wa.setPropertyTreeValue(this.getConnectionid(), ptreeid, valuetree);
        }
    }

    public void deleteWebPagePropertyTree(String pageid, String productedition, String selectedelementid, String selectedptreeid) throws Exception {
        this.getWebAdminManager().deleteWebPagePropertyTree(this.getConnectionid(), pageid, productedition, selectedelementid, selectedptreeid);
    }

    public int saveWebPage(String pageid, String productedition, String parameter, String parameter1, String timeruleid, String expresspageflag, String extendwebpageid, String extendproductedition) throws Exception {
        this.getWebAdminManager().setWebPage(this.getConnectionid(), pageid, productedition, parameter, parameter1, timeruleid, expresspageflag, extendwebpageid, extendproductedition);
        return 1;
    }

    public int saveWebPageDefaultFlag(String pageid, String defaultedition) throws Exception {
        this.getWebAdminManager().setWebPageDefaultFlag(this.getConnectionid(), pageid, defaultedition);
        return 1;
    }

    public void saveWebPageCategories(String pageid, String parameter) throws Exception {
        this.getWebAdminManager().setWebPageCategories(this.getConnectionid(), pageid, parameter);
    }

    public void saveWebPageRoles(String pageid, String parameter) throws Exception {
        this.getWebAdminManager().setWebPageRoles(this.getConnectionid(), pageid, parameter);
    }

    public void saveWebPageProperties(String pageid, String productedition, PropertyList pl) throws Exception {
        this.getWebAdminManager().setWebPageProperties(this.getConnectionid(), pageid, productedition, pl);
    }

    public void addWebPagePropertyTree(String pageid, String productedition, String layoutptreeid, String timeruleid, String layoutnodeid, int index) throws Exception {
        this.getWebAdminManager().addWebPagePropertyTree(this.getConnectionid(), pageid, productedition, layoutptreeid, timeruleid, layoutnodeid, index);
    }

    public int editPropertyTree(String ptreeid, String ptreetype, String description, String objectname) throws Exception {
        this.getWebAdminManager().editPropertyTree(this.getConnectionid(), ptreeid, ptreetype, description, objectname);
        return 1;
    }

    public void setPropertyTreeCategories(String ptreeid, String categorylist) throws Exception {
        this.getWebAdminManager().setPropertyTreeCategories(this.getConnectionid(), ptreeid, categorylist);
    }

    public PropertyTree getPropertyTree(String propertytreeid) throws Exception {
        PropertyTree propertyTree = new PropertyTree(propertytreeid);
        String xml = this.getPropertyTreeValues(propertytreeid);
        propertyTree.setValueXML(xml);
        propertyTree.setPropertyDefinitionList(this.getPropertyDefinitionList(propertytreeid));
        return propertyTree;
    }

    public void savePropertyTree(PropertyTree propertyTree) throws Exception {
        String propertyTreeid = propertyTree.getId();
        String valueXML = propertyTree.toXMLString();
        this.savePropertyTreeValues(propertyTreeid, valueXML);
        for (Node node : propertyTree.getAllNodes()) {
            if (node.isRenamed()) {
                this.renameWebPagePropertyTreeNode(propertyTree.getId(), node.getOriginalNodeId(), node.getId());
            }
            if (!node.hasPropertyListChanges()) continue;
            this.savePropertyTree(propertyTree.getId(), null, node.getId(), null, node.getPropertyListRenames());
        }
    }

    public HashMap generateConfigReport(PageContext pageContext, HashMap properties) throws Exception {
        properties.put("imageroot", HttpUtil.getWebAppRoot(pageContext.getServletContext()));
        HashMap returnMap = this.getRequestManager().processRequest(this.getConnectionid(), ConfigReportRequestHandler.class.getName(), properties);
        return returnMap;
    }

    public String getDefaultPageEdition(String webpageid) throws Exception {
        return this.getRequestManager().getDefaultWebPageEdition(this.getConnectionid(), webpageid);
    }
}

