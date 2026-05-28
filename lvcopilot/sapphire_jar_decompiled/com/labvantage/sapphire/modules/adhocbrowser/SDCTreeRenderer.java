/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import com.labvantage.sapphire.modules.adhocbrowser.TreeRenderer;
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCTreeRenderer
extends TreeRenderer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90432 $";
    protected String sdcid;
    protected PropertyList sdcPropertyList;
    protected DataSet links;
    protected PropertyList dataentryPL;
    protected String filtertext;
    protected boolean isRoot = false;

    public SDCTreeRenderer(String sdcid, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
        this.sdcid = sdcid;
        this.sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        this.links = sdcProcessor.getLinksData(sdcid);
        this.dataentryPL = AdhocQueryPageUtil.getDataEntryPropertyList(sdcid, pagedata);
    }

    @Override
    protected ArrayList getChildNodes() throws SapphireException {
        String columnid;
        ArrayList<TreeNode> childNodes = new ArrayList<TreeNode>();
        PropertyListCollection columns = this.sdcPropertyList.getCollection("columns");
        String keycolid1 = this.sdcPropertyList.getProperty("keycolid1");
        SafeSQL safeSQL = new SafeSQL();
        DataSet extendedcolumns = this.queryProcessor.getPreparedSqlDataSet("select * from sysextendedcolumn where tableid=" + safeSQL.addVar(this.sdcPropertyList.getProperty("tableid")), safeSQL.getValues());
        PropertyListCollection displaycolumns = AdhocQueryPageUtil.getSDCColumnPLCollection(this.sdcid, this.pagedata, columns, this.tp);
        boolean showAllSearchableColumns = AdhocQueryPageUtil.isShowAllSearchableColumns(this.sdcid, this.pagedata);
        DataMaskUtil dataMaskUtil = new DataMaskUtil(this.queryProcessor);
        HashSet<String> definedColumnIdSet = new HashSet<String>();
        if (displaycolumns != null && displaycolumns.size() > 0) {
            for (Object columnPL : displaycolumns) {
                columnid = ((PropertyList)columnPL).getProperty("columnid");
                String sdidetail = ((PropertyList)columnPL).getProperty("sdidetail");
                if (columnid.length() > 0) {
                    definedColumnIdSet.add(columnid);
                    continue;
                }
                if (sdidetail.length() <= 0) continue;
                definedColumnIdSet.add(sdidetail);
            }
        }
        PropertyListCollection defaultdisplaycolumns = new PropertyListCollection();
        if (displaycolumns == null || displaycolumns.size() == 0 || showAllSearchableColumns) {
            int i;
            for (i = 0; i < columns.size(); ++i) {
                columnid = columns.getPropertyList(i).getProperty("columnid");
                if (definedColumnIdSet.contains(columnid)) continue;
                if (this.sdcid == null) {
                    defaultdisplaycolumns.add(columns.getPropertyList(i));
                    continue;
                }
                if (!this.adhocmetadata.isSearchableColumn(this.sdcPropertyList.getProperty("tableid"), columnid) || columnid.equals(keycolid1) && !this.isRoot && !"D".equals(this.sdcPropertyList.getProperty("sdctype"))) continue;
                defaultdisplaycolumns.add(columns.getPropertyList(i));
            }
            for (i = 0; i < extendedcolumns.getRowCount(); ++i) {
                if (definedColumnIdSet.contains(extendedcolumns.getString(i, "columnid")) || !"Y".equals(extendedcolumns.getString(i, "searchableflag"))) continue;
                PropertyList extcolumn = new PropertyList();
                extcolumn.setProperty("columnid", extendedcolumns.getString(i, "columnid"));
                extcolumn.setProperty("columndesc", extendedcolumns.getString(i, "columndesc"));
                extcolumn.setProperty("datatype", extendedcolumns.getString(i, "datatype"));
                defaultdisplaycolumns.add(extcolumn);
            }
            if ("Y".equals(this.sdcPropertyList.getProperty("allowattributesflag")) && !definedColumnIdSet.contains("Attribute")) {
                PropertyList column = new PropertyList();
                column.setProperty("sdidetail", "Attribute");
                column.setProperty("title", this.tp.translate("Other Attributes"));
                defaultdisplaycolumns.add(column);
            }
            if ("Y".equals(this.sdcPropertyList.getProperty("dataentryflag")) && !definedColumnIdSet.contains("Data Entry")) {
                PropertyList column = new PropertyList();
                column.setProperty("sdidetail", "Data Entry");
                column.setProperty("title", this.tp.translate("Test Results"));
                defaultdisplaycolumns.add(column);
            }
        }
        if (displaycolumns == null || displaycolumns.size() == 0) {
            displaycolumns = defaultdisplaycolumns;
        } else if (showAllSearchableColumns) {
            displaycolumns.addAll(defaultdisplaycolumns);
        }
        ElementUtil.setColumnDefaultTitle(displaycolumns, columns, this.tp);
        for (int i = 0; i < displaycolumns.size(); ++i) {
            PropertyList columnPropertyList = displaycolumns.getPropertyList(i);
            String columnid2 = columnPropertyList.getProperty("columnid");
            String mode = columnPropertyList.getProperty("mode");
            if ("hidden".equals(mode)) continue;
            if (columnid2.length() > 0) {
                TreeNode treenode;
                int c;
                String columntitle = columnPropertyList.getProperty("title");
                boolean isRealColumn = false;
                for (c = 0; c < columns.size(); ++c) {
                    if (!columnid2.equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                    PropertyList sdccolumnPropertyList = columns.getPropertyList(c);
                    columnPropertyList.setProperty("mode", mode);
                    columnPropertyList.putAll(sdccolumnPropertyList);
                    if (columntitle.length() > 0) {
                        columnPropertyList.setProperty("columndesctrans", columntitle);
                    } else {
                        columnPropertyList.setProperty("columndesctrans", this.tp.translate(sdccolumnPropertyList.getProperty("columndesc")));
                    }
                    isRealColumn = true;
                    break;
                }
                if (!isRealColumn) {
                    for (c = 0; c < extendedcolumns.getRowCount(); ++c) {
                        if (!columnid2.equals(extendedcolumns.getString(c, "columnid"))) continue;
                        columnPropertyList.setProperty("columnid", columnid2);
                        columnPropertyList.setProperty("columndefinition", extendedcolumns.getString(c, "columndefinition"));
                        if (columntitle.length() > 0) {
                            columnPropertyList.setProperty("columndesctrans", columntitle);
                        } else {
                            columnPropertyList.setProperty("columndesctrans", this.tp.translate(extendedcolumns.getString(c, "columndesc")));
                        }
                        columnPropertyList.setProperty("datatype", extendedcolumns.getString(c, "datatype"));
                        break;
                    }
                    if (dataMaskUtil.isColumnSensitiveForAdhocQuery(this.sdcid, columnPropertyList.getProperty("columndefinition"))) {
                        continue;
                    }
                } else if (dataMaskUtil.isColumnSensitiveForAdhocQuery(this.sdcid, columnid2)) continue;
                if ((treenode = this.getTreeNode(this.linkcolumnid, columnPropertyList, this.links)) == null) continue;
                childNodes.add(treenode);
                continue;
            }
            if (columnPropertyList.getProperty("sdidetail").length() > 0) {
                String title;
                String detailname = columnPropertyList.getProperty("sdidetail");
                String string = title = columnPropertyList.getProperty("title").length() == 0 ? detailname : columnPropertyList.getProperty("title");
                if ("Data Entry".equals(detailname)) {
                    childNodes.add(this.getDetailRootNode(title, "dataentryroot", "WEB-CORE/images/gif/Tests.gif"));
                    continue;
                }
                if ("Document Field".equals(detailname)) {
                    childNodes.add(this.getDetailRootNode(title, "fieldroot", "WEB-CORE/imageref/flat/16/flat_black_search.svg"));
                    continue;
                }
                if ("Worksheet Field".equals(detailname)) {
                    childNodes.add(this.getDetailRootNode(title, "worksheetitemfieldroot", "WEB-CORE/imageref/flat/16/flat_black_search.svg"));
                    continue;
                }
                if ("Attribute".equals(detailname)) {
                    childNodes.add(this.getDetailRootNode(title, "attributeroot", "WEB-CORE/imageref/flat/16/flat_black_search.svg"));
                    continue;
                }
                if (!this.isRoot) continue;
                if (detailname.indexOf("_") < 0) {
                    childNodes.add(this.getDetailNode(columnPropertyList));
                    continue;
                }
                if (detailname.indexOf("_") <= 0) continue;
                String detailtableid = this.adhocmetadata.getReverseFKTableId(detailname);
                if (detailtableid == null) {
                    childNodes.add(this.getDetailNode(columnPropertyList));
                    continue;
                }
                PropertyList column = new PropertyList();
                column.setProperty("columnid", detailtableid + "id");
                column.setProperty("title", title);
                childNodes.add(this.getReverseFKDetailTreeNode(detailname, column));
                continue;
            }
            childNodes.add(this.getTreeNode(this.linkcolumnid, columnPropertyList, this.links));
        }
        return childNodes;
    }

    protected TreeNode getDetailRootNode(String title, String rootname, String nodeImage) {
        TreeNode rootnode = new TreeNode();
        rootnode.setNodeimage(nodeImage);
        String objectprefix = "";
        if (this.linkcolumnid != null && this.linkcolumnid.length() > 0) {
            objectprefix = this.linkcolumnid + ".";
        }
        String nodeid = objectprefix + rootname;
        rootnode.setNodeid(nodeid);
        rootnode.setCanDrillDown(true);
        rootnode.setNodelabel(title);
        rootnode.setDragable(false);
        if ("dataentryroot".equals(rootname)) {
            rootnode.setAdditionalLabelHtml("&nbsp;&nbsp;<input title=\"" + this.tp.translate("Type in a few letters to find the name of the tests you want to search on") + "\" name=\"dataentryfilter\" id=\"dataentryfilter\" onkeyup=\"if( true ) { filterParamList( '" + nodeid + "', this.value ) };\" size=\"10\"/>");
        } else if ("fieldroot".equals(rootname)) {
            rootnode.setAdditionalLabelHtml("&nbsp;&nbsp;<input title=\"" + this.tp.translate("Type in a few letters to find the name of the document fields you want to search on") + "\" name=\"dataentryfilter\" id=\"dataentryfilter\" onkeyup=\"if( true ) { filterParamList( '" + nodeid + "', this.value ) };\" size=\"10\"/>");
        } else if ("attributeroot".equals(rootname)) {
            rootnode.setAdditionalLabelHtml("&nbsp;&nbsp;<input title=\"" + this.tp.translate("Type in a few letters to find the name of the document fields you want to search on") + "\" name=\"dataentryfilter\" id=\"dataentryfilter\" onkeyup=\"if( true ) { filterParamList( '" + nodeid + "', this.value ) };\" size=\"10\"/>");
        } else if ("worksheetitemfieldroot".equals(rootname)) {
            rootnode.setAdditionalLabelHtml("&nbsp;&nbsp;<input title=\"" + this.tp.translate("Type in a few letters to find the name of the document fields you want to search on") + "\" name=\"dataentryfilter\" id=\"dataentryfilter\" onkeyup=\"if( true ) { filterParamList( '" + nodeid + "', this.value ) };\" size=\"10\"/>");
        } else {
            throw new AssertionError((Object)"Should be dataentryroot, fieldroot or attributeroot");
        }
        return rootnode;
    }

    protected TreeNode getDetailNode(PropertyList column) {
        TreeNode treeNode = new TreeNode();
        String detailtableid = column.getProperty("sdidetail");
        String title = column.getProperty("title");
        if (title == null || title.length() == 0) {
            title = detailtableid;
        }
        if ("Data Set".equals(detailtableid)) {
            detailtableid = "sdidata";
            treeNode.setNodeimage("WEB-OPAL/images/datasets.gif");
        } else if ("Data Item".equals(detailtableid)) {
            detailtableid = "sdidataitem";
            treeNode.setNodeimage("WEB-OPAL/images/tests.gif");
        } else if ("Track Item".equals(detailtableid)) {
            detailtableid = "trackitem";
            treeNode.setNodeimage("WEB-CORE/images/gif/Storage.gif");
        } else if ("Work Item".equals(detailtableid)) {
            detailtableid = "sdiworkitem";
            treeNode.setNodeimage("WEB-CORE/images/gif/Storage.gif");
        } else if ("Data Capture".equals(detailtableid)) {
            detailtableid = "sdidatacapture";
        }
        String objectprefix = "";
        if (this.linkcolumnid != null && this.linkcolumnid.length() > 0) {
            objectprefix = this.linkcolumnid + ".";
        }
        treeNode.setNodeid(objectprefix + detailtableid);
        treeNode.setCanDrillDown(true);
        treeNode.setNodelabel(title);
        treeNode.setDragable(false);
        return treeNode;
    }

    private TreeNode getReverseFKDetailTreeNode(String columnidprefix, PropertyList column) {
        String columnid;
        String nodeid = columnid = column.getProperty("columnid");
        if (columnidprefix.length() > 0) {
            nodeid = columnidprefix + "." + columnid;
        }
        TreeNode node = new TreeNode();
        node.setNodeid(nodeid);
        String linksdcid = "";
        linksdcid = this.adhocmetadata.getSdcId(this.adhocmetadata.getReverseFKTableId(columnidprefix));
        String linktableid = this.adhocmetadata.getTableid(linksdcid);
        if (this.adhocmetadata.isSearchableTable(linktableid)) {
            node.setCanDrillDown(true);
        }
        node.setDragable(false);
        String nodelabel = column.getProperty("title");
        if (nodelabel.length() == 0) {
            nodelabel = linksdcid.length() > 0 ? linksdcid : columnid;
        }
        node.setNodelabel(nodelabel);
        return node;
    }
}

