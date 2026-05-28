/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.Iterator;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QuickPageBuilder {
    private final PropertyList pageinfo;
    private final WebAdminProcessor wp;
    private final SDCProcessor sdcp;
    private final boolean devMode;
    private final String compcode;
    private final String sdcid;
    private final String tableid;
    private final SapphireConnection sapphireConnection;
    private final String databaseid;
    private final PropertyList sdcProps;
    private String changeRequestId = "";
    private String checkedOutToDeptId = "";
    private boolean isChangeControlled_WebPage = false;
    private boolean isChangeControlled_PropertyTree = false;

    public QuickPageBuilder(PropertyList pageinfo, WebAdminProcessor wp, SDCProcessor sdcp, boolean devMode, String compcode, SapphireConnection sapphireConnection) {
        this.pageinfo = pageinfo;
        this.wp = wp;
        this.sdcp = sdcp;
        this.devMode = devMode;
        this.compcode = compcode;
        this.sdcid = pageinfo.getProperty("sdcid");
        this.sapphireConnection = sapphireConnection;
        this.databaseid = sapphireConnection.getDatabaseId();
        this.sdcProps = sdcp.getPropertyList(this.sdcid);
        this.tableid = sdcp.getProperty(this.sdcid, "tableid");
        CMTPolicy cmtPolicy_WebPage = CMTPolicy.getPolicy(sapphireConnection.getConnectionId(), "WebPage");
        this.isChangeControlled_WebPage = "Y".equals(cmtPolicy_WebPage.getChangeControlledFlag());
        CMTPolicy cmtPolicy_Propertytree = CMTPolicy.getPolicy(sapphireConnection.getConnectionId(), "PropertyTree");
        this.isChangeControlled_PropertyTree = "Y".equals(cmtPolicy_Propertytree.getChangeControlledFlag());
    }

    public void createQuickPage() throws Exception {
        boolean createlistpage = this.pageinfo.getProperty("createlistpage").equals("Y");
        boolean createmaintpage = this.pageinfo.getProperty("createmaintpage").equals("Y");
        boolean createlookuppage = this.pageinfo.getProperty("createlookuppage").equals("Y");
        boolean createviewpage = this.pageinfo.getProperty("createviewpage").equals("Y");
        boolean createauditpage = this.pageinfo.getProperty("createauditpage").equals("Y");
        String listpageid = this.pageinfo.getProperty("listpageid");
        String maintpageid = this.pageinfo.getProperty("maintpageid");
        String lookuppageid = this.pageinfo.getProperty("lookuppageid");
        String viewpageid = this.pageinfo.getProperty("viewpageid");
        String auditpageid = this.pageinfo.getProperty("auditpageid");
        String layoutNodeid = this.pageinfo.getProperty("layoutnodeid");
        String layoutTabid = this.pageinfo.getProperty("layouttabid");
        String layoutMenuid = this.pageinfo.getProperty("layoutmenuid");
        String newTabTitle = this.pageinfo.getProperty("newtabtitle");
        String newMenuTitle = this.pageinfo.getProperty("newmenutitle");
        String categoryList = this.pageinfo.getProperty("categorylist");
        boolean multiSelect = this.pageinfo.getProperty("multiselect", "N").equals("Y");
        boolean newTabOrMenu = "__NEW__".equals(layoutTabid) || "__NEW__".equals(layoutMenuid);
        String extendNodeid = layoutNodeid;
        if (newTabOrMenu || createlistpage) {
            PropertyList tabi;
            PropertyListCollection tabsi;
            PropertyList layoutnodei;
            PropertyTree layout = this.wp.getPropertyTree("menugizmo");
            boolean nodeCreated = false;
            if (this.devMode && layoutNodeid.endsWith(" Custom")) {
                layoutNodeid = StringUtil.replaceAll(layoutNodeid, " Custom", " Product");
            } else if (this.compcode.length() > 0 && layoutNodeid.endsWith(" Custom")) {
                String customNodeid = layoutNodeid;
                Node compLayoutNode = layout.getNode(layoutNodeid = StringUtil.replaceAll(layoutNodeid, " Custom", " Comp " + this.compcode));
                if (compLayoutNode == null) {
                    Node customNode = layout.getNode(customNodeid);
                    Node parentNode = customNode.getParent();
                    compLayoutNode = layout.createNode(layoutNodeid, parentNode);
                    nodeCreated = true;
                    compLayoutNode.setLocked(true);
                    customNode.setParentNodeList(compLayoutNode.getNodeList());
                }
            }
            if (this.isChangeControlled_PropertyTree) {
                CMTUtil.checkOutSDI(this.sapphireConnection.getConnectionId(), "PropertyTree", "menugizmo", "", "", layoutNodeid, this.changeRequestId, this.checkedOutToDeptId, nodeCreated ? "add" : "edit", null);
            }
            PropertyList layoutnode = layout.getNodePropertyList(layoutNodeid, false);
            if (newTabOrMenu) {
                PropertyList tab;
                CacheUtil.clear(this.databaseid, "GizmoDef");
                CacheUtil.clear(this.databaseid, "GizmoDefAssets");
                layoutnodei = layout.getNodePropertyList(layoutNodeid, true);
                tabsi = layoutnodei.getCollectionNotNull("tabs");
                tabi = tabsi.find("id", layoutTabid);
                boolean createNewTab = tabi == null;
                PropertyListCollection tabs = layoutnode.getCollectionNotNull("tabs");
                if (createNewTab) {
                    tab = this.createNewTab(layout, listpageid, newTabTitle, tabsi, tabs);
                    layoutTabid = tab.getProperty("id");
                } else {
                    tab = this.findExistingTab(tabi, tabs);
                }
                layoutMenuid = this.createNewMenu(layout, createlistpage, listpageid, newMenuTitle, tabi, tab);
                layout.setId("menugizmo");
                this.wp.savePropertyTree(layout);
            } else if (createlistpage) {
                PropertyListCollection tabs;
                layoutnodei = layout.getNodePropertyList(layoutNodeid, true);
                tabsi = layoutnodei.getCollectionNotNull("tabs");
                tabi = tabsi.find("id", layoutTabid);
                PropertyList tab = this.findExistingTab(tabi, tabs = layoutnode.getCollectionNotNull("tabs"));
                PropertyListCollection menus = tab.getCollectionNotNull("menus");
                PropertyList menu = menus.find("id", layoutMenuid);
                if (menu != null) {
                    menu.setProperty("link", "rc?command=page&page=" + listpageid);
                }
                layout.setId("menugizmo");
            }
            this.wp.savePropertyTree(layout);
        }
        String edition = this.devMode ? "R5" : "U";
        StringBuffer allPageIds = new StringBuffer();
        StringBuffer allPageEditions = new StringBuffer();
        StringBuffer allPTreeIds = new StringBuffer();
        StringBuffer allNodes = new StringBuffer();
        if (createlistpage || createlookuppage) {
            PropertyTree listElement = this.wp.getPropertyTree("list");
            NodeList candidates = listElement.getNode("Sapphire Custom").getNodeList();
            String listNodeid = this.findSDCNode(candidates);
            if (listNodeid.length() == 0) {
                listNodeid = this.createListNode(listElement);
                allPTreeIds.append(";").append("list");
                allNodes.append(";").append(listNodeid);
                listElement.setId("list");
                this.wp.savePropertyTree(listElement);
                listElement = this.wp.getPropertyTree("list");
            }
            listNodeid = StringUtil.replaceAll(listNodeid, " Product", " Custom");
            if (createlistpage) {
                this.createListPage(listElement, listpageid, edition, listNodeid, extendNodeid, layoutTabid, layoutMenuid, categoryList, createmaintpage ? maintpageid : "", createviewpage ? viewpageid : "", createauditpage ? auditpageid : "");
                allPageIds.append(";").append(listpageid);
                allPageEditions.append(";").append(edition);
            }
            if (createlookuppage) {
                this.createLookupPage(listElement, lookuppageid, edition, listNodeid, multiSelect, categoryList);
                allPageIds.append(";").append(lookuppageid);
                allPageEditions.append(";").append(edition);
            }
        }
        if (createmaintpage || createviewpage) {
            PropertyTree maintElement = this.wp.getPropertyTree("maint");
            NodeList candidates = maintElement.getNode("Sapphire Custom").getNodeList();
            String maintNodeid = this.findSDCNode(candidates);
            if (maintNodeid.length() == 0) {
                maintNodeid = this.createMaintNode(maintElement);
                allPTreeIds.append(";").append("maint");
                allNodes.append(";").append(maintNodeid);
                maintElement.setId("maint");
                this.wp.savePropertyTree(maintElement);
                maintElement = this.wp.getPropertyTree("maint");
            }
            maintNodeid = StringUtil.replaceAll(maintNodeid, " Product", " Custom");
            if (createmaintpage) {
                this.createMaintPage(maintElement, maintpageid, edition, maintNodeid, extendNodeid, layoutTabid, layoutMenuid, categoryList);
                allPageIds.append(";").append(maintpageid);
                allPageEditions.append(";").append(edition);
            }
            if (createviewpage) {
                this.createViewPage(maintElement, viewpageid, edition, maintNodeid, extendNodeid, layoutTabid, layoutMenuid, categoryList);
                allPageIds.append(";").append(viewpageid);
                allPageEditions.append(";").append(edition);
            }
        }
        if (createauditpage) {
            PropertyTree auditElement = this.wp.getPropertyTree("auditdetails");
            NodeList candidates = auditElement.getNode("Sapphire Custom").getNodeList();
            String auditNodeid = this.findTableNode(candidates);
            if (auditNodeid.length() == 0) {
                auditNodeid = this.createAuditNode(auditElement);
                allPTreeIds.append(";").append("auditdetails");
                allNodes.append(";").append(auditNodeid);
                auditElement.setId("auditdetails");
                this.wp.savePropertyTree(auditElement);
                auditElement = this.wp.getPropertyTree("auditdetails");
            }
            auditNodeid = StringUtil.replaceAll(auditNodeid, " Product", " Custom");
            if (createauditpage) {
                this.createAuditPage(auditElement, auditpageid, edition, auditNodeid, extendNodeid, layoutTabid, layoutMenuid, categoryList);
                allPageIds.append(";").append(auditpageid);
                allPageEditions.append(";").append(edition);
            }
        }
        if (this.isChangeControlled_WebPage && allPageIds.length() > 0) {
            allPageIds.deleteCharAt(0);
            allPageEditions.deleteCharAt(0);
            CMTUtil.checkOutSDI(this.wp.getConnectionid(), "WebPage", allPageIds.toString(), allPageEditions.toString(), "", "", this.changeRequestId, this.checkedOutToDeptId, "add", null);
        }
        if (this.isChangeControlled_PropertyTree && allPTreeIds.length() > 0) {
            allPTreeIds.deleteCharAt(0);
            allNodes.deleteCharAt(0);
            CMTUtil.checkOutSDI(this.wp.getConnectionid(), "PropertyTree", allPTreeIds.toString(), "", "", allNodes.toString(), this.changeRequestId, this.checkedOutToDeptId, "add", null);
        }
    }

    private String findSDCNode(NodeList candidates) {
        String listNodeid = "";
        Iterator iterator = candidates.iterator();
        while (iterator.hasNext() && listNodeid.length() == 0) {
            Node node = (Node)iterator.next();
            if (!node.getPropertyList().getProperty("sdcid").equals(this.sdcid)) continue;
            listNodeid = node.getId();
        }
        return listNodeid;
    }

    private String findTableNode(NodeList candidates) {
        String listNodeid = "";
        Iterator iterator = candidates.iterator();
        while (iterator.hasNext() && listNodeid.length() == 0) {
            Node node = (Node)iterator.next();
            if (!node.getPropertyList().getProperty("tableid").equals(this.tableid)) continue;
            listNodeid = node.getId();
        }
        return listNodeid;
    }

    private void createListPage(PropertyTree listElement, String pageid, String edition, String listNodeid, String layoutNodeid, String layoutTabid, String layoutMenuid, String categoryList, String maintpageid, String viewpageid, String auditpageid) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " List Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "Generic", "layout", layoutNodeid, 1);
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintenanceList", "pagedata", "List Custom", 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "list", "list", listNodeid, 3);
            PropertyListCollection existingColumns = listElement.getNodePropertyList(listNodeid, true).getCollection("columns");
            PropertyListCollection columns = this.addMissingListColumns(listElement, existingColumns, true, false);
            if (columns.size() > 0) {
                PropertyList listProps = new PropertyList();
                listProps.setProperty("columns", columns);
                this.wp.savePageValues(pageid, edition, "list", "list", listProps, null);
            }
            PropertyList advancedSearch = new PropertyList();
            advancedSearch.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedsearch", "advancedsearch", "ListPage Custom", 4);
            this.wp.savePageValues(pageid, edition, "advancedsearch", "advancedsearch", advancedSearch, null);
            PropertyList advancedToolbar = new PropertyList();
            advancedToolbar.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedtoolbar", "advancedtoolbar", "List Custom", 5);
            PropertyTree toolbarElement = this.wp.getPropertyTree("advancedtoolbar");
            PropertyListCollection existingButtons = toolbarElement.getNodePropertyList("List Custom", true).getCollection("buttons");
            PropertyListCollection buttons = this.addMissingListButtons(existingButtons, maintpageid, viewpageid, auditpageid);
            if (buttons.size() > 0) {
                advancedToolbar.setProperty("buttons", buttons);
            }
            this.wp.savePageValues(pageid, edition, "advancedtoolbar", "advancedtoolbar", advancedToolbar, null);
        }
    }

    private void createLookupPage(PropertyTree listElement, String pageid, String edition, String listNodeid, boolean multiSelect, String categoryList) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " Lookup Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "GenericPopup", "layout", "Sapphire Custom", 1);
            String versionedFlag = this.sdcp.getProperty(this.sdcid, "versionedflag");
            String nodeid = versionedFlag.equals("Y") ? "VersionedLookup Custom" : "Lookup Custom";
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintenanceList", "pagedata", nodeid, 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "list", "list", this.devMode ? StringUtil.replaceAll(listNodeid, " Product", " Custom") : listNodeid, 3);
            PropertyListCollection existingColumns = listElement.getNodePropertyList(listNodeid, true).getCollection("columns");
            PropertyListCollection columns = this.addMissingListColumns(listElement, existingColumns, false, true);
            PropertyList listProps = new PropertyList();
            if (multiSelect) {
                listProps.setProperty("selectortype", "checkbox");
            } else {
                String keycolid1 = this.sdcp.getProperty(this.sdcid, "keycolid1");
                PropertyList sapphireCustom = listElement.getNodePropertyList("Sapphire Custom", true);
                PropertyListCollection sapphireCustomCols = sapphireCustom.getCollection("columns");
                String keyid1PropertyListid = sapphireCustomCols.find("columnid", "keycolid1").getId();
                PropertyList column = columns.getPropertyList(keyid1PropertyListid);
                if (column == null) {
                    column = new PropertyList();
                    columns.add(column);
                    column.setId(keyid1PropertyListid);
                }
                PropertyList link = new PropertyList();
                column.setProperty("link", link);
                link.setProperty("href", "JavaScript:parent.accept('[columnid=" + keycolid1 + "]')");
                link.setProperty("target", "_top");
            }
            if (columns.size() > 0) {
                listProps.setProperty("columns", columns);
            }
            if (listProps.size() > 0) {
                this.wp.savePageValues(pageid, edition, "list", "list", listProps, null);
            }
            PropertyList advancedSearch = new PropertyList();
            advancedSearch.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedsearch", "advancedsearch", "LookupPage Custom", 4);
            this.wp.savePageValues(pageid, edition, "advancedsearch", "advancedsearch", advancedSearch, null);
            PropertyList advancedToolbar = new PropertyList();
            advancedToolbar.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedtoolbar", "advancedtoolbar", multiSelect ? "Multiple Custom" : "Lookup Custom", 5);
            this.wp.savePageValues(pageid, edition, "advancedtoolbar", "advancedtoolbar", advancedToolbar, null);
        }
    }

    private void createMaintPage(PropertyTree maintElement, String pageid, String edition, String maintNodeid, String layoutNodeid, String layoutTabid, String layoutMenuid, String categoryList) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " Maintenance Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "Generic", "layout", layoutNodeid, 1);
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintenanceForm", "pagedata", "EditPage Custom", 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "maint", "maint", this.devMode ? StringUtil.replaceAll(maintNodeid, " Product", " Custom") : maintNodeid, 3);
            PropertyListCollection existingColumns = maintElement.getNodePropertyList(maintNodeid, true).getCollection("columns");
            PropertyListCollection columns = this.addMissingMaintColumns(maintElement, existingColumns, true, false);
            if (columns.size() > 0) {
                PropertyList maintProps = new PropertyList();
                maintProps.setProperty("columns", columns);
                this.wp.savePageValues(pageid, edition, "maint", "maint", maintProps, null);
            }
            PropertyList advancedToolbar = new PropertyList();
            advancedToolbar.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedtoolbar", "advancedtoolbar", "Other SDCs Edit Custom", 4);
            this.wp.savePageValues(pageid, edition, "advancedtoolbar", "advancedtoolbar", advancedToolbar, null);
        }
    }

    private void createAuditPage(PropertyTree maintElement, String pageid, String edition, String auditNodeid, String layoutNodeid, String layoutTabid, String layoutMenuid, String categoryList) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " Audit View Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "GenericPopup", "layout", "Sapphire Custom", 1);
            this.wp.addWebPagePropertyTree(pageid, edition, "AuditView", "pagedata", "Sapphire Custom", 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "auditdetails", "auditdetails1", this.devMode ? StringUtil.replaceAll(auditNodeid, " Product", " Custom") : auditNodeid, 3);
            PropertyList advancedToolbar = new PropertyList();
            advancedToolbar.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedtoolbar", "advancedtoolbar", "AuditView2 Custom", 4);
            this.wp.savePageValues(pageid, edition, "advancedtoolbar", "advancedtoolbar", advancedToolbar, null);
        }
    }

    private void createViewPage(PropertyTree maintElement, String pageid, String edition, String maintNodeid, String layoutNodeid, String layoutTabid, String layoutMenuid, String categoryList) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " View Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "GenericPopup", "layout", "Sapphire Custom", 1);
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintenanceForm", "pagedata", "ViewPage Custom", 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "maint", "maint", this.devMode ? StringUtil.replaceAll(maintNodeid, " Product", " Custom") : maintNodeid, 3);
            PropertyListCollection existingColumns = maintElement.getNodePropertyList(maintNodeid, true).getCollection("columns");
            PropertyListCollection columns = this.addMissingMaintColumns(maintElement, existingColumns, false, true);
            PropertyList maintProps = new PropertyList();
            maintProps.setProperty("viewonly", "Y");
            if (columns.size() > 0) {
                maintProps.setProperty("columns", columns);
            }
            this.wp.savePageValues(pageid, edition, "maint", "maint", maintProps, null);
            PropertyList advancedToolbar = new PropertyList();
            advancedToolbar.setProperty("sdcid", this.sdcid);
            this.wp.addWebPagePropertyTree(pageid, edition, "advancedtoolbar", "advancedtoolbar", "ViewSDI Custom", 4);
            this.wp.savePageValues(pageid, edition, "advancedtoolbar", "advancedtoolbar", advancedToolbar, null);
        }
    }

    private String createListNode(PropertyTree listElement) throws Exception {
        Node customNode;
        Node sapphireCustom = listElement.getNode("Sapphire Custom");
        PropertyListCollection existingColumns = listElement.getNodePropertyList("Sapphire Custom", true).getCollectionNotNull("columns");
        String newNodeid = this.getUniqueNodeId(listElement);
        Node newNode = new Node(newNodeid);
        sapphireCustom.getNodeList().add(newNode);
        PropertyList props = new PropertyList();
        if (this.devMode) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Product", " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        } else if (this.compcode.length() > 0) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Comp " + this.compcode, " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        }
        newNode.setPropertyList(props);
        props.setProperty("sdcid", this.sdcid);
        props.setProperty("selectortype", "radiobutton");
        PropertyListCollection columns = this.addMissingListColumns(listElement, existingColumns, true, true);
        if (columns.size() > 0) {
            props.setProperty("columns", columns);
        }
        return newNodeid;
    }

    private PropertyListCollection addMissingListColumns(PropertyTree listElement, PropertyListCollection existingColumns, boolean listPage, boolean lookupPage) {
        PropertyListCollection columns = new PropertyListCollection();
        long sequence = this.getMaxSequence(existingColumns);
        String keycolid1 = this.sdcp.getProperty(this.sdcid, "keycolid1");
        String desccol = this.sdcp.getProperty(this.sdcid, "desccol");
        DataSet columnids = this.sdcp.getColumnData(this.sdcid);
        long propertyListIdCount = System.currentTimeMillis();
        for (int i = 0; i < columnids.size(); ++i) {
            String columnid = columnids.getString(i, "columnid");
            boolean show = listPage && this.pageinfo.getProperty("list__" + columnid).equals("Y") || lookupPage && this.pageinfo.getProperty("lookup__" + columnid).equals("Y");
            String title = this.pageinfo.getProperty(columnid + "_title");
            String aliasColumnid = columnid.equals(keycolid1) ? "keycolid1" : (columnid.equals(desccol) ? "desccol" : columnid);
            PropertyList existingColumn = existingColumns.find("columnid", aliasColumnid);
            PropertyList overrideColumn = new PropertyList();
            if (show) {
                if (existingColumn == null) {
                    existingColumn = new PropertyList();
                    ++propertyListIdCount;
                    propertyListIdCount = listElement.getUniquePropertyListId(propertyListIdCount);
                    overrideColumn.setId(Long.toString(propertyListIdCount));
                    overrideColumn.setProperty("id", columnid);
                    overrideColumn.setSequence(sequence += 100000L);
                } else {
                    overrideColumn.setId(existingColumn.getId());
                }
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", aliasColumnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("mode", show ? "Display Text" : "Hidden Value", "Display Text", existingColumn, overrideColumn);
                if (columnid.equals(keycolid1) && show) {
                    PropertyList link;
                    boolean createmaintpage = this.pageinfo.getProperty("createmaintpage").equals("Y");
                    boolean createviewpage = this.pageinfo.getProperty("createviewpage").equals("Y");
                    if (createviewpage) {
                        String viewpageid = this.pageinfo.getProperty("viewpageid");
                        if (!viewpageid.equals(this.sdcid + "View")) {
                            link = new PropertyList();
                            this.setOverrideValue("href", "rc?command=page&page=" + viewpageid + "&keyid1=[columnid]&sdcid=[sdcid]&mode=View", "", existingColumn.getPropertyListNotNull("link"), link);
                            if (link.size() > 0) {
                                overrideColumn.setProperty("link", link);
                            }
                        }
                    } else if (createmaintpage) {
                        String maintpageid = this.pageinfo.getProperty("maintpageid");
                        link = new PropertyList();
                        link.setProperty("href", "rc?command=page&page=" + maintpageid + "&keyid1=[columnid]&sdcid=[sdcid]&mode=Edit");
                        overrideColumn.setProperty("link", link);
                    }
                }
            } else if (existingColumn != null && !existingColumn.getProperty("mode").equals("Hidden Value")) {
                overrideColumn.setId(existingColumn.getId());
                overrideColumn.setProperty("mode", "Hidden Value");
            }
            if (overrideColumn.size() <= 0) continue;
            columns.add(overrideColumn);
        }
        return columns;
    }

    private PropertyListCollection addMissingStellarListColumns(PropertyTree listElement, PropertyListCollection existingColumns, boolean listPage) {
        PropertyListCollection columns = new PropertyListCollection();
        columns.setId(existingColumns.getId());
        long sequence = this.getMaxSequence(existingColumns);
        String keycolid1 = this.sdcp.getProperty(this.sdcid, "keycolid1");
        String desccol = this.sdcp.getProperty(this.sdcid, "desccol");
        DataSet columnids = this.sdcp.getColumnData(this.sdcid);
        long propertyListIdCount = System.currentTimeMillis();
        for (int i = 0; i < columnids.size(); ++i) {
            String columnid = columnids.getString(i, "columnid");
            boolean show = listPage && this.pageinfo.getProperty("list__" + columnid).equals("Y");
            String title = this.pageinfo.getProperty(columnid + "_title");
            String aliasColumnid = columnid;
            PropertyList existingColumn = existingColumns.find("columnid", aliasColumnid);
            PropertyList overrideColumn = new PropertyList();
            if (show) {
                if (existingColumn == null) {
                    existingColumn = new PropertyList();
                    ++propertyListIdCount;
                    propertyListIdCount = listElement.getUniquePropertyListId(propertyListIdCount);
                    overrideColumn.setId(Long.toString(propertyListIdCount));
                    overrideColumn.setProperty("id", columnid);
                    overrideColumn.setSequence(sequence += 100000L);
                } else {
                    overrideColumn.setId(existingColumn.getId());
                }
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", aliasColumnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("show", show ? "Y" : "N", "Y", existingColumn, overrideColumn);
            } else if (existingColumn != null && !existingColumn.getProperty("show").equals("N")) {
                overrideColumn.setId(existingColumn.getId());
                overrideColumn.setProperty("show", "N");
            }
            if (overrideColumn.size() <= 0) continue;
            columns.add(overrideColumn);
        }
        return columns;
    }

    private PropertyListCollection addMissingListButtons(PropertyListCollection existingButtons, String maintpageid, String viewpageid, String auditpageid) {
        PropertyListCollection buttons = new PropertyListCollection();
        buttons.setId(existingButtons.getId());
        if (maintpageid.length() > 0) {
            PropertyList addButton = existingButtons.find("id", "Add");
            String page1 = addButton.getPropertyListNotNull("standardbuttonprops").getProperty("page");
            page1 = StringUtil.replaceAll(page1, "[sdcid]Maint", maintpageid);
            PropertyList newAddButton = new PropertyList();
            newAddButton.setId(addButton.getId());
            buttons.add(newAddButton);
            PropertyList standardbuttonprops1 = new PropertyList();
            standardbuttonprops1.setProperty("page", page1);
            newAddButton.setProperty("standardbuttonprops", standardbuttonprops1);
            PropertyList editButton = existingButtons.find("id", "Edit");
            PropertyList newEditButton = new PropertyList();
            String page2 = editButton.getPropertyListNotNull("standardbuttonprops").getProperty("page");
            page2 = StringUtil.replaceAll(page2, "[sdcid]Maint", maintpageid);
            buttons.add(newEditButton);
            newEditButton.setId(editButton.getId());
            PropertyList standardbuttonprops2 = new PropertyList();
            standardbuttonprops2.setProperty("page", page2);
            newEditButton.setProperty("standardbuttonprops", standardbuttonprops2);
        }
        if (viewpageid.length() > 0) {
            PropertyList viewButton = existingButtons.find("id", "View");
            PropertyList newViewButton = new PropertyList();
            String page3 = viewButton.getPropertyListNotNull("standardbuttonprops").getProperty("page");
            page3 = StringUtil.replaceAll(page3, "[sdcid]View", viewpageid);
            buttons.add(newViewButton);
            newViewButton.setId(viewButton.getId());
            PropertyList standardbuttonprops3 = new PropertyList();
            standardbuttonprops3.setProperty("page", page3);
            newViewButton.setProperty("standardbuttonprops", standardbuttonprops3);
        }
        if (auditpageid.length() > 0) {
            PropertyList auditButton = existingButtons.find("id", "AuditView");
            PropertyList newAuditButton = new PropertyList();
            buttons.add(newAuditButton);
            newAuditButton.setId(auditButton.getId());
            PropertyList commonProps = new PropertyList();
            commonProps.setProperty("show", "Y");
            newAuditButton.setProperty("commonprops", commonProps);
            if (!(this.sdcid + "AuditView").equals(auditpageid)) {
                String javascript = auditButton.getPropertyListNotNull("userbuttonprops").getProperty("action");
                javascript = StringUtil.replaceAll(javascript, "[sdcid]AuditView", auditpageid);
                PropertyList userbuttonprops = new PropertyList();
                userbuttonprops.setProperty("action", javascript);
                newAuditButton.setProperty("userbuttonprops", userbuttonprops);
            }
        }
        return buttons;
    }

    private PropertyListCollection addMissingMaintColumns(PropertyTree maintElement, PropertyListCollection existingColumns, boolean maintPage, boolean viewPage) {
        PropertyListCollection columns = new PropertyListCollection();
        long sequence = this.getMaxSequence(existingColumns);
        DataSet columnids = this.sdcp.getColumnData(this.sdcid);
        long propertyListIdCount = System.currentTimeMillis();
        for (int i = 0; i < columnids.size(); ++i) {
            String columnid = columnids.getString(i, "columnid");
            boolean primaryKey = columnids.getString(i, "pkflag").equals("Y");
            boolean show = maintPage && this.pageinfo.getProperty("maint__" + columnid).equals("Y") || viewPage && this.pageinfo.getProperty("view__" + columnid).equals("Y");
            String title = this.pageinfo.getProperty(columnid + "_title");
            PropertyList existingColumn = existingColumns.find("columnid", columnid);
            PropertyList overrideColumn = new PropertyList();
            if (show) {
                if (existingColumn == null) {
                    existingColumn = new PropertyList();
                    ++propertyListIdCount;
                    propertyListIdCount = maintElement.getUniquePropertyListId(propertyListIdCount);
                    overrideColumn.setId(Long.toString(propertyListIdCount));
                    overrideColumn.setProperty("id", columnid);
                    overrideColumn.setSequence(sequence += 100000L);
                } else {
                    overrideColumn.setId(existingColumn.getId());
                }
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", columnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("mode", show ? "default" : "hidden", "default", existingColumn, overrideColumn);
                if (primaryKey) {
                    this.setOverrideValue("validation", "Mandatory", "", existingColumn, overrideColumn);
                }
            } else if (existingColumn != null && !existingColumn.getProperty("mode").equals("hidden")) {
                overrideColumn.setId(existingColumn.getId());
                overrideColumn.setProperty("mode", "hidden");
            }
            if (overrideColumn.size() <= 0) continue;
            columns.add(overrideColumn);
        }
        return columns;
    }

    private PropertyListCollection addMissingAuditColumns(PropertyTree auditElement, PropertyListCollection existingColumns) {
        PropertyListCollection columns = new PropertyListCollection();
        long sequence = this.getMaxSequence(existingColumns);
        DataSet columnids = this.sdcp.getColumnData(this.sdcid);
        long propertyListIdCount = System.currentTimeMillis();
        for (int i = 0; i < columnids.size(); ++i) {
            String columnid = columnids.getString(i, "columnid");
            boolean show = this.pageinfo.getProperty("audit__" + columnid).equals("Y");
            String title = this.pageinfo.getProperty(columnid + "_title");
            PropertyList existingColumn = existingColumns.find("columnid", columnid);
            PropertyList overrideColumn = new PropertyList();
            if (show) {
                if (existingColumn == null) {
                    existingColumn = new PropertyList();
                    ++propertyListIdCount;
                    propertyListIdCount = auditElement.getUniquePropertyListId(propertyListIdCount);
                    overrideColumn.setId(Long.toString(propertyListIdCount));
                    overrideColumn.setProperty("id", columnid);
                    overrideColumn.setSequence(sequence += 100000L);
                } else {
                    overrideColumn.setId(existingColumn.getId());
                }
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", columnid, "", existingColumn, overrideColumn);
            }
            if (overrideColumn.size() <= 0) continue;
            columns.add(overrideColumn);
        }
        return columns;
    }

    private long getMaxSequence(PropertyListCollection existingColumns) {
        long sequence = 0L;
        if (existingColumns.size() > 0) {
            for (PropertyList propertyList : existingColumns) {
                if (propertyList.getSequence() <= sequence) continue;
                sequence = propertyList.getSequence();
            }
        }
        return sequence;
    }

    private void setOverrideValue(String propertyid, String value, String defaultValue, PropertyList existingColumn, PropertyList overrideColumn) {
        if (!existingColumn.getProperty(propertyid, defaultValue).equals(value) && value.length() > 0) {
            overrideColumn.setProperty(propertyid, value);
        }
    }

    private String createMaintNode(PropertyTree maintElement) throws Exception {
        Node customNode;
        Node sapphireCustom = maintElement.getNode("Sapphire Custom");
        PropertyListCollection existingColumns = maintElement.getNodePropertyList("Sapphire Custom", true).getCollectionNotNull("columns");
        String newNodeid = this.getUniqueNodeId(maintElement);
        Node newNode = new Node(newNodeid);
        sapphireCustom.getNodeList().add(newNode);
        PropertyList props = new PropertyList();
        if (this.devMode) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Product", " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        } else if (this.compcode.length() > 0) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Comp " + this.compcode, " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        }
        newNode.setPropertyList(props);
        props.setProperty("sdcid", this.sdcid);
        props.setProperty("style", "Form");
        props.setProperty("formcols", "2");
        props.setProperty("fixedcols", "1");
        PropertyListCollection columns = this.addMissingMaintColumns(maintElement, existingColumns, true, true);
        if (columns.size() > 0) {
            props.setProperty("columns", columns);
        }
        return newNodeid;
    }

    private String createAuditNode(PropertyTree auditElement) throws Exception {
        Node customNode;
        Node sapphireCustom = auditElement.getNode("Sapphire Custom");
        PropertyListCollection existingColumns = auditElement.getNodePropertyList("Sapphire Custom", true).getCollectionNotNull("columns");
        String newNodeid = this.getUniqueNodeId(auditElement);
        Node newNode = new Node(newNodeid);
        sapphireCustom.getNodeList().add(newNode);
        PropertyList props = new PropertyList();
        if (this.devMode) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Product", " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        } else if (this.compcode.length() > 0) {
            newNodeid = StringUtil.replaceAll(newNodeid, " Comp " + this.compcode, " Custom");
            customNode = new Node(newNodeid);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        }
        newNode.setPropertyList(props);
        props.setProperty("tableid", this.sdcProps.getProperty("tableid"));
        props.setProperty("title", this.sdcProps.getProperty("singular"));
        PropertyListCollection sdcCols = this.sdcProps.getCollectionNotNull("columns");
        PropertyList keyid1ColProps = sdcCols.find("columnid", this.sdcProps.getProperty("keycolid1"));
        if (keyid1ColProps == null) {
            props.setProperty("datarowtitle", "[" + this.sdcProps.getProperty("keycolid1") + "]");
        } else {
            props.setProperty("datarowtitle", keyid1ColProps.getProperty("columnlabel", this.sdcProps.getProperty("keycolid1").toUpperCase()) + ": [" + this.sdcProps.getProperty("keycolid1") + "]");
        }
        PropertyListCollection columns = this.addMissingAuditColumns(auditElement, existingColumns);
        if (columns.size() > 0) {
            props.setProperty("columns", columns);
        }
        return newNodeid;
    }

    private String getUniqueNodeId(PropertyTree listElement) {
        String mainName = this.compcode.length() > 0 && this.sdcid.startsWith(this.compcode) ? this.sdcid.substring(4) : this.sdcid;
        String newNodeid = mainName + (this.devMode ? " Product" : (this.compcode.length() > 0 ? " Comp " + this.compcode : ""));
        String checkNodeid = mainName + (this.devMode || this.compcode.length() > 0 ? " Custom" : "");
        int count = 2;
        while (listElement.getNode(checkNodeid) != null) {
            newNodeid = mainName + count + (this.devMode ? " Product" : (this.compcode.length() > 0 ? " Comp " + this.compcode : ""));
            checkNodeid = mainName + count + (this.devMode || this.compcode.length() > 0 ? " Custom" : "");
            ++count;
        }
        return newNodeid;
    }

    private String createNewMenu(PropertyTree layout, boolean createlistpage, String listpageid, String newMenuTitle, PropertyList tabi, PropertyList tab) {
        long sequence;
        PropertyListCollection menusi = tabi == null ? null : tabi.getCollectionNotNull("menus");
        PropertyListCollection menus = tab.getCollectionNotNull("menus");
        String menuid = StringUtil.replaceAll(newMenuTitle.toLowerCase(), " ", "");
        if (menusi == null || menusi.size() == 0) {
            sequence = 1000000L;
        } else {
            PropertyList last = menusi.getPropertyList(menusi.size() - 1);
            sequence = last.getSequence();
            String originalmenuid = menuid;
            int count = 2;
            while (menusi.find("id", menuid) != null) {
                menuid = originalmenuid + count++;
            }
        }
        PropertyList menu = new PropertyList();
        long propertyListId = layout.getUniquePropertyListId(System.currentTimeMillis());
        menu.setId(Long.toString(propertyListId));
        menus.add(menu);
        menu.setProperty("id", menuid);
        menu.setProperty("text", newMenuTitle);
        menu.setSequence(sequence + 1000000L);
        if (createlistpage) {
            menu.setProperty("link", "rc?command=page&page=" + listpageid);
        }
        return menuid;
    }

    private PropertyList findExistingTab(PropertyList tabi, PropertyListCollection tabs) {
        String propertylistid = tabi.getId();
        PropertyList tab = tabs.getPropertyList(propertylistid);
        if (tab == null) {
            tab = new PropertyList();
            tabs.add(tab);
            tab.setId(propertylistid);
        }
        return tab;
    }

    private PropertyList createNewTab(PropertyTree layout, String listpageid, String newTabTitle, PropertyListCollection tabsi, PropertyListCollection tabs) {
        String tabid;
        PropertyList tab = new PropertyList();
        tabs.add(tab);
        long propertyListId = layout.getUniquePropertyListId(System.currentTimeMillis());
        tab.setId(Long.toString(propertyListId));
        long sequence = 0L;
        if (tabsi.size() > 0) {
            PropertyList last = tabsi.getPropertyList(tabsi.size() - 1);
            sequence = last.getSequence();
        }
        String originaltabid = tabid = StringUtil.replaceAll(newTabTitle.toLowerCase(), " ", "");
        int count = 2;
        while (tabs.find("id", tabid) != null) {
            tabid = originaltabid + count++;
        }
        tab.setProperty("id", tabid);
        tab.setProperty("text", newTabTitle);
        tab.setProperty("link", "rc?command=page&page=" + listpageid);
        tab.setProperty("colorcode", "red");
        tab.setSequence(sequence + 100000L);
        return tab;
    }

    public String getChangeRequestId() {
        return this.changeRequestId;
    }

    public void setChangeRequestId(String changeRequestId) {
        this.changeRequestId = changeRequestId;
    }

    public String getCheckedOutToDeptId() {
        return this.checkedOutToDeptId;
    }

    public void setCheckedOutToDeptId(String checkedOutToDeptId) {
        this.checkedOutToDeptId = checkedOutToDeptId;
    }
}

