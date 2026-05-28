/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StellarQuickPageBuilder {
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
    private long plcounter;
    private String changeRequestId = "";
    private String checkedOutToDeptId = "";
    private boolean isChangeControlled_WebPage = false;
    private boolean isChangeControlled_PropertyTree = false;

    public StellarQuickPageBuilder(PropertyList pageinfo, WebAdminProcessor wp, SDCProcessor sdcp, boolean devMode, String compcode, SapphireConnection sapphireConnection) {
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
        this.plcounter = System.currentTimeMillis();
        CMTPolicy cmtPolicy_WebPage = CMTPolicy.getPolicy(sapphireConnection.getConnectionId(), "WebPage");
        this.isChangeControlled_WebPage = "Y".equals(cmtPolicy_WebPage.getChangeControlledFlag());
        CMTPolicy cmtPolicy_Propertytree = CMTPolicy.getPolicy(sapphireConnection.getConnectionId(), "PropertyTree");
        this.isChangeControlled_PropertyTree = "Y".equals(cmtPolicy_Propertytree.getChangeControlledFlag());
    }

    public void createStellarQuickPage() throws Exception {
        boolean createlistpage = this.pageinfo.getProperty("createlistpage").equals("Y");
        boolean createmaintpage = this.pageinfo.getProperty("createmaintpage").equals("Y");
        String listpageid = this.pageinfo.getProperty("listpageid");
        String maintpageid = this.pageinfo.getProperty("maintpageid");
        String appid = this.pageinfo.getProperty("stellar_appid");
        String categoryList = this.pageinfo.getProperty("categorylist");
        String edition = "Stellar";
        StringBuffer allPageIds = new StringBuffer();
        StringBuffer allPageEditions = new StringBuffer();
        StringBuffer allPTreeIds = new StringBuffer();
        StringBuffer allNodes = new StringBuffer();
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        if (createlistpage) {
            PropertyTree listElement;
            NodeList elementCandidates;
            String listElementNodeid;
            PropertyTree listPage = this.wp.getPropertyTree("ListStellarPageType");
            NodeList pageCandidates = listPage.getNode("Sapphire Custom").getNodeList();
            String listPageNodeId = this.findSDCNode(pageCandidates);
            if (listPageNodeId.length() == 0) {
                listPageNodeId = this.createStellarListPageTypeNode(listPage, maintpageid);
                allPTreeIds.append(";").append("ListStellarPageType");
                allNodes.append(";").append(listPageNodeId);
                listPage.setId("ListStellarPageType");
                this.wp.savePropertyTree(listPage);
                listPage = this.wp.getPropertyTree("ListStellarPageType");
            }
            if ((listElementNodeid = this.findSDCNode(elementCandidates = (listElement = this.wp.getPropertyTree("ListStellarElement")).getNode("Sapphire Custom").getNodeList())).length() == 0) {
                listElementNodeid = this.createStellarListElementNode(listElement);
                allPTreeIds.append(";").append("ListStellarElement");
                allNodes.append(";").append(listElementNodeid);
                listElement.setId("ListStellarElement");
                this.wp.savePropertyTree(listElement);
                listElement = this.wp.getPropertyTree("ListStellarElement");
            }
            listElementNodeid = StringUtil.replaceAll(listElementNodeid, " Product", " Custom");
            if (createlistpage) {
                this.createStellarListPage(listPage, listElement, listpageid, edition, listPageNodeId, listElementNodeid, categoryList, createmaintpage ? maintpageid : "");
                allPageIds.append(";").append(listpageid);
                allPageEditions.append(";").append(edition);
            }
            if (appid.length() > 0) {
                this.addPageToApp(appid, listpageid, true);
            }
        }
        if (createmaintpage) {
            PropertyTree maintElement = this.wp.getPropertyTree("MaintStellarElement");
            NodeList candidates = maintElement.getNode("Sapphire Custom").getNodeList();
            String maintNodeid = this.findSDCNode(candidates);
            if (maintNodeid.length() == 0) {
                maintNodeid = this.createStellarMaintNode(maintElement);
                allPTreeIds.append(";").append("MaintStellarElement");
                allNodes.append(";").append(maintNodeid);
                maintElement.setId("MaintStellarElement");
                this.wp.savePropertyTree(maintElement);
                maintElement = this.wp.getPropertyTree("MaintStellarElement");
            }
            maintNodeid = StringUtil.replaceAll(maintNodeid, " Product", " Custom");
            if (createmaintpage) {
                this.createStellarMaintPage(maintElement, maintpageid, edition, maintNodeid, categoryList);
                allPageIds.append(";").append(maintpageid);
                allPageEditions.append(";").append(edition);
            }
            if (appid.length() > 0) {
                this.addPageToApp(appid, maintpageid, false);
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

    private void addPageToApp(String appid, String pageid, boolean showInList) throws SapphireException {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
        DataSet app = qp.getPreparedSqlDataSet("SELECT appid, productvaluetree, valuetree FROM app WHERE appid =?", new Object[]{appid}, true);
        String productvaluetree = app.getString(0, "productvaluetree", "<propertylist></propertylist>");
        String valuetree = app.getString(0, "valuetree", "<propertylist></propertylist>");
        PropertyList appProps = new PropertyList();
        appProps.setPropertyList(this.devMode ? productvaluetree : valuetree);
        PropertyListCollection pages = appProps.getCollectionNotNull("pages");
        long maxsequence = 0L;
        for (int i = 0; i < pages.size(); ++i) {
            PropertyList temp = pages.getPropertyList(i);
            if (temp.getSequence() <= maxsequence) continue;
            maxsequence = temp.getSequence();
        }
        PropertyList newpl = new PropertyList();
        newpl.setProperty("show", showInList ? "Y" : "N");
        newpl.setProperty("pageid", pageid);
        newpl.setProperty("title", pageid);
        newpl.setAttribute("sequence", "" + (maxsequence + 100000L));
        newpl.setAttribute("id", "pl" + this.plcounter++);
        pages.add(newpl);
        PropertyList update = new PropertyList();
        update.setProperty("sdcid", "LV_App");
        update.setProperty("keyid1", appid);
        update.setProperty(this.devMode ? "productvaluetree" : "valuetree", appProps.toXMLString());
        ap.processAction("EditSDI", "1", update);
    }

    private void createStellarListPage(PropertyTree listPage, PropertyTree listElement, String pageid, String edition, String listPageNodeId, String listElementNodeId, String categoryList, String maintpageid) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " List Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "ListStellarPageType", "pagedata", listPageNodeId, 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "ListStellarElement", "list", listElementNodeId, 3);
            PropertyListCollection existingColumns = listElement.getNodePropertyList(listElementNodeId, true).getCollection("columns");
            PropertyListCollection columns = this.addMissingStellarListColumns(listElement, existingColumns, true);
            if (columns.size() > 0) {
                PropertyList listProps = new PropertyList();
                listProps.setProperty("columns", columns);
                this.wp.savePageValues(pageid, edition, "ListStellarElement", "list", listProps, null);
            }
            PropertyList pageTypeOverrides = new PropertyList();
            PropertyListCollection existingOperations = listPage.getNodePropertyList(listPageNodeId, true).getCollection("operations");
            PropertyListCollection operations = new PropertyListCollection();
            operations.setId(existingOperations.getId());
            pageTypeOverrides.setProperty("operations", operations);
            this.setOperationMaintPage(maintpageid, "add", existingOperations, operations);
            this.setOperationMaintPage(maintpageid, "copy", existingOperations, operations);
            this.setOperationMaintPage(maintpageid, "view", existingOperations, operations);
            this.setOperationMaintPage(maintpageid, "edit", existingOperations, operations);
            this.wp.savePageValues(pageid, edition, "ListStellarPageType", "pagedata", pageTypeOverrides, null);
        }
    }

    private void createStellarMaintPage(PropertyTree maintElement, String pageid, String edition, String maintNodeid, String categoryList) throws Exception {
        String expresspageflag;
        String virtualpageflag;
        String pagedesc = this.sdcid + " Maint Page";
        if (this.wp.saveWebPage(pageid, edition, pagedesc, "", virtualpageflag = "N", expresspageflag = "N", null, null) == 1) {
            if (categoryList.length() > 0) {
                this.wp.saveWebPageCategories(pageid, categoryList);
            }
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintStellarPageType", "pagedata", "Sapphire Custom", 2);
            this.wp.addWebPagePropertyTree(pageid, edition, "MaintStellarElement", "maint", maintNodeid, 3);
            PropertyListCollection existingColumns = maintElement.getNodePropertyList(maintNodeid, true).getCollectionNotNull("columns");
            PropertyListCollection columns = this.addMissingStellarMaintColumns(maintElement, existingColumns);
            if (columns.size() > 0) {
                PropertyList listProps = new PropertyList();
                listProps.setProperty("columns", columns);
                this.wp.savePageValues(pageid, edition, "MaintStellarElement", "maint", listProps, null);
            }
            PropertyList pageTypeProps = new PropertyList();
            pageTypeProps.setProperty("sdcid", this.sdcid);
            pageTypeProps.setProperty("title", pagedesc);
            this.wp.savePageValues(pageid, edition, "MaintStellarPageType", "pagedata", pageTypeProps, null);
        }
    }

    private String createStellarListPageTypeNode(PropertyTree listPage, String maintpageid) throws Exception {
        Node customNode;
        Node sapphireCustom = listPage.getNode("Sapphire Custom");
        String extendsPageNodeId = this.getUniqueNodeId(listPage);
        Node newNode = new Node(extendsPageNodeId);
        sapphireCustom.getNodeList().add(newNode);
        PropertyList pageTypeProps = new PropertyList();
        if (this.devMode) {
            extendsPageNodeId = StringUtil.replaceAll(extendsPageNodeId, " Product", " Custom");
            customNode = new Node(extendsPageNodeId);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        } else if (this.compcode.length() > 0) {
            extendsPageNodeId = StringUtil.replaceAll(extendsPageNodeId, " Comp " + this.compcode, " Custom");
            customNode = new Node(extendsPageNodeId);
            newNode.getNodeList().add(customNode);
            newNode.setLocked(true);
        }
        newNode.setPropertyList(pageTypeProps);
        pageTypeProps.setProperty("sdcid", this.sdcid);
        pageTypeProps.setProperty("title", this.sdcid + " List Page");
        PropertyList queries = new PropertyList();
        queries.setProperty("querymode", "single");
        queries.setProperty("querytype", "fromwhere");
        queries.setProperty("queryfrom", this.sdcp.getProperty(this.sdcid, "tableid"));
        pageTypeProps.setProperty("queries", queries);
        PropertyListCollection existingOperations = listPage.getNodePropertyList("Sapphire Custom", true).getCollectionNotNull("operations");
        PropertyListCollection operations = new PropertyListCollection();
        operations.setId(existingOperations.getId());
        pageTypeProps.setProperty("operations", operations);
        this.setOperationMaintPage(maintpageid, "add", existingOperations, operations);
        this.setOperationMaintPage(maintpageid, "edit", existingOperations, operations);
        this.setOperationMaintPage(maintpageid, "copy", existingOperations, operations);
        this.setOperationMaintPage(maintpageid, "view", existingOperations, operations);
        return extendsPageNodeId;
    }

    private void setOperationMaintPage(String maintpageid, String operationid, PropertyListCollection existingOperations, PropertyListCollection operations) {
        PropertyList existingEdit = existingOperations.find("id", operationid);
        PropertyList operation = new PropertyList();
        operations.add(operation);
        operation.setId(existingEdit.getId());
        operation.setProperty("page", maintpageid);
    }

    private String createStellarListElementNode(PropertyTree listElement) throws Exception {
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
        props.setProperty("title", "My " + this.sdcid);
        props.setProperty("gridstyle", "comfortable");
        props.setProperty("selectionmode", "single");
        PropertyListCollection columns = this.addMissingStellarListColumns(listElement, existingColumns, true);
        if (columns.size() > 0) {
            props.setProperty("columns", columns);
        }
        return newNodeid;
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
                    overrideColumn.setSequence(sequence += 100000L);
                } else {
                    overrideColumn.setId(existingColumn.getId());
                }
                this.setOverrideValue("columntype", "primary", "", existingColumn, overrideColumn);
                this.setOverrideValue("primarycolumnid", aliasColumnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", aliasColumnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("retrieve", "Y", "Y", existingColumn, overrideColumn);
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

    private PropertyListCollection addMissingStellarMaintColumns(PropertyTree maintElement, PropertyListCollection existingColumns) {
        PropertyListCollection columns = new PropertyListCollection();
        columns.setId(existingColumns.getId());
        long sequence = this.getMaxSequence(existingColumns);
        DataSet columnids = this.sdcp.getColumnData(this.sdcid);
        long propertyListIdCount = System.currentTimeMillis();
        for (int i = 0; i < columnids.size(); ++i) {
            String columnid = columnids.getString(i, "columnid");
            boolean show = this.pageinfo.getProperty("maint__" + columnid).equals("Y");
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
                this.setOverrideValue("columntype", "primary", "", existingColumn, overrideColumn);
                this.setOverrideValue("primarycolumnid", columnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("columnid", columnid, "", existingColumn, overrideColumn);
                this.setOverrideValue("title", title, "", existingColumn, overrideColumn);
                this.setOverrideValue("mode", "default", "", existingColumn, overrideColumn);
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

    private void setOverrideValue(String propertyid, String value, String defaultValue, PropertyList existingColumn, PropertyList overrideColumn) {
        if (!existingColumn.getProperty(propertyid, defaultValue).equals(value) && value.length() > 0) {
            overrideColumn.setProperty(propertyid, value);
        }
    }

    private String createStellarMaintNode(PropertyTree maintElement) throws Exception {
        PropertyListCollection existingSections;
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
        PropertyListCollection columns = this.addMissingStellarMaintColumns(maintElement, existingColumns);
        if (columns.size() > 0) {
            props.setProperty("columns", columns);
        }
        if ((existingSections = maintElement.getNodePropertyList("Sapphire Custom", true).getCollectionNotNull("sections")).size() == 0) {
            PropertyList col;
            int i;
            PropertyListCollection sections = new PropertyListCollection();
            props.setProperty("sections", sections);
            PropertyList section = new PropertyList();
            long newSectionId = maintElement.getUniquePropertyListId(System.currentTimeMillis());
            section.setId(Long.toString(newSectionId));
            sections.add(section);
            section.setProperty("sectionid", "section1");
            section.setProperty("title", "Default Section");
            section.setProperty("sectiontype", "group");
            PropertyList inputStyle = new PropertyList();
            section.setProperty("inputstyle", inputStyle);
            inputStyle.setProperty("variant", "outlined");
            inputStyle.setProperty("size", "small");
            inputStyle.setProperty("spacing", "2");
            JsonObject gridLayout = new JsonObject();
            JsonArray gridColumns = new JsonArray();
            gridLayout.put("columns", gridColumns);
            ArrayList<String> ctemp = new ArrayList<String>();
            for (i = 0; i < existingColumns.size(); ++i) {
                col = existingColumns.getPropertyList(i);
                if (!col.getProperty("show", "Y").equals("Y")) continue;
                ctemp.add(col.getProperty("columnid"));
            }
            for (i = 0; i < columns.size(); ++i) {
                col = columns.getPropertyList(i);
                String columnid = col.getProperty("columnid");
                if (columnid.length() <= 0) continue;
                if (!ctemp.contains(columnid)) {
                    ctemp.add(columnid);
                    continue;
                }
                if (!col.getProperty("show", "Y").equals("N")) continue;
                ctemp.remove(col.getProperty("columnid"));
            }
            for (i = 0; i < ctemp.size(); ++i) {
                String columnid = (String)ctemp.get(i);
                JsonObject gridColumn = new JsonObject().put("columnid", columnid).put("sequence", i);
                gridColumns.put(gridColumn);
            }
            section.setProperty("gridlayout", HttpUtil.encodeURIComponent(gridLayout.toString()));
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

    public String getStellarCopyListMaintPageColumns(StringBuffer output, String copyListPage, String copyMaintPage) throws SapphireException {
        String checked;
        String val;
        boolean include;
        String title;
        String displayColumnid;
        String columnid;
        PropertyList column;
        int row;
        PropertyListCollection columns;
        PropertyList pagedata;
        PropertyList requestProps;
        RequestProcessor requestProcessor;
        HashSet<String> excludeList = new HashSet<String>();
        excludeList.add("auditsequence");
        excludeList.add("tracelogid");
        excludeList.add("usersequence");
        excludeList.add("createdt");
        excludeList.add("createby");
        excludeList.add("createtool");
        excludeList.add("moddt");
        excludeList.add("modby");
        excludeList.add("modtool");
        excludeList.add("templateflag");
        excludeList.add("versionstatus");
        excludeList.add("activeflag");
        excludeList.add("compcode");
        StringBuilder js = new StringBuilder();
        output.append("<tr><td colspan=\"4\"><div style=\"color:red\">FUNCTION NOT YET IMPLEMENTED...</div></td></tr>");
        output.append("<tr><td style=\"vertical-align:top\">");
        if (copyListPage.length() > 0) {
            output.append("<table border=\"1\" class=\"gridtable\" cellspacing=\"0\">\n");
            output.append("<tr>\n");
            output.append("<td colspan=\"3\" class=\"gridtitle\">List Page</td>");
            output.append("</tr>\n");
            requestProcessor = new RequestProcessor(this.sapphireConnection.getConnectionId());
            requestProps = new PropertyList();
            pagedata = requestProcessor.getWebPageProperties(StringUtil.split(copyListPage, ";")[0], StringUtil.split(copyListPage, ";")[1], requestProps, true);
            PropertyList list = pagedata.getPropertyList("list");
            columns = list.getCollection("columns");
            for (row = 0; row < columns.size(); ++row) {
                column = columns.getPropertyList(row);
                output.append("<tr>\n");
                columnid = column.getProperty("columnid");
                displayColumnid = "";
                if (columnid.trim().contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                    displayColumnid = "(nested-select)";
                }
                title = column.getProperty("title", columnid);
                output.append("<td class=\"gridcell\">").append(columnid + (displayColumnid.length() > 0 ? " " + displayColumnid : "")).append("</td>");
                boolean bl = include = !excludeList.contains(columnid);
                if (title.length() == 0) {
                    title = columnid;
                    if (title.endsWith("id") && title.length() > 3) {
                        title = title.substring(0, title.length() - 2);
                    }
                    if ((title.startsWith("s_") || title.startsWith("u_")) && title.length() > 3) {
                        title = title.substring(2);
                    }
                    title = StringUtil.initCaps(title);
                }
                checked = (val = this.pageinfo.getProperty("list__" + columnid)).equals("Y") ? " checked " : (val.equals("N") ? "" : (include ? " checked " : ""));
                output.append("<td class=\"gridcell\"><input name=\"__" + columnid + "\" mode=\"list\" type=\"checkbox\" " + checked + " id=\"list__" + columnid + "\"></td>");
                js.append("parent.setProperty( \"list__" + columnid + "\", document.getElementById( \"list__" + columnid + "\" ).checked ? \"Y\" : \"N\" );");
                output.append("<td class=\"gridcell\">").append("<input type=\"text\" id=\"" + columnid + "_title\" value=\"" + title + "\"/> ").append("</td>");
                js.append("parent.setProperty( \"" + columnid + "_title\", document.getElementById( \"" + columnid + "_title\" ).value );");
                output.append("</tr>\n");
            }
            output.append("</table>");
        }
        output.append("</td><td style=\"vertical-align:top\">");
        if (copyMaintPage.length() > 0) {
            output.append("<table border=\"1\" class=\"gridtable\" cellspacing=\"0\">\n");
            output.append("<tr>\n");
            output.append("<td colspan=\"3\" class=\"gridtitle\">Maint Page</td>");
            output.append("</tr>\n");
            requestProcessor = new RequestProcessor(this.sapphireConnection.getConnectionId());
            requestProps = new PropertyList();
            pagedata = requestProcessor.getWebPageProperties(StringUtil.split(copyMaintPage, ";")[0], StringUtil.split(copyListPage, ";")[1], requestProps, true);
            PropertyList maint = pagedata.getPropertyList("maint");
            columns = maint.getCollection("columns");
            for (row = 0; row < columns.size(); ++row) {
                column = columns.getPropertyList(row);
                output.append("<tr>\n");
                columnid = column.getProperty("columnid");
                displayColumnid = "";
                if (columnid.trim().contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                    displayColumnid = "(nested-select)";
                }
                title = column.getProperty("title", columnid);
                output.append("<td class=\"gridcell\">").append(columnid + (displayColumnid.length() > 0 ? " " + displayColumnid : "")).append("</td>");
                boolean bl = include = !excludeList.contains(columnid);
                if (title.length() == 0) {
                    title = columnid;
                    if (title.endsWith("id") && title.length() > 3) {
                        title = title.substring(0, title.length() - 2);
                    }
                    if ((title.startsWith("s_") || title.startsWith("u_")) && title.length() > 3) {
                        title = title.substring(2);
                    }
                    title = StringUtil.initCaps(title);
                }
                checked = (val = this.pageinfo.getProperty("list__" + columnid)).equals("Y") ? " checked " : (val.equals("N") ? "" : (include ? " checked " : ""));
                output.append("<td class=\"gridcell\"><input name=\"__" + columnid + "\" mode=\"list\" type=\"checkbox\" " + checked + " id=\"list__" + columnid + "\"></td>");
                js.append("parent.setProperty( \"list__" + columnid + "\", document.getElementById( \"list__" + columnid + "\" ).checked ? \"Y\" : \"N\" );");
                output.append("<td class=\"gridcell\">").append("<input type=\"text\" id=\"" + columnid + "_title\" value=\"" + title + "\"/> ").append("</td>");
                js.append("parent.setProperty( \"" + columnid + "_title\", document.getElementById( \"" + columnid + "_title\" ).value );");
                output.append("</tr>\n");
            }
            output.append("</table>");
        }
        output.append("</td></tr>");
        return js.toString();
    }
}

