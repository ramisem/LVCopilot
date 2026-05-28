/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import com.labvantage.sapphire.modules.adhocbrowser.TreeRenderer;
import java.util.ArrayList;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DetailTreeRenderer
extends TreeRenderer {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    protected String detailname;
    protected String sdcid;
    protected boolean isCriteriaOnly = false;

    public DetailTreeRenderer(String sdcid, String detailname, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
        this.detailname = detailname;
        this.sdcid = sdcid;
    }

    public boolean isCriteriaOnly() {
        return this.isCriteriaOnly;
    }

    public void setIsCriteriaOnly(boolean isCriteriaOnly) {
        this.isCriteriaOnly = isCriteriaOnly;
    }

    @Override
    protected ArrayList getChildNodes() {
        String columnid;
        int i;
        SafeSQL safeSQL;
        ArrayList<TreeNode> childNodes = new ArrayList<TreeNode>();
        PropertyList sdcPropertyList = null;
        DataSet links = null;
        if (this.sdcid != null) {
            sdcPropertyList = this.sdcProcessor.getPropertyList(this.sdcid);
            links = this.sdcProcessor.getLinksData(this.sdcid);
        }
        String tableid = "";
        tableid = this.linkcolumnid;
        if (tableid.indexOf(".") > 0) {
            if (this.adhocmetadata.getReverseFKTableId(tableid = tableid.substring(0, tableid.indexOf("."))) != null) {
                tableid = this.detailname;
            } else if (this.adhocmetadata.getSdcId(tableid) == null) {
                tableid = this.linkcolumnid;
            }
        }
        PropertyListCollection columns = null;
        DataSet extendedcolumns = new DataSet();
        if (sdcPropertyList != null) {
            columns = sdcPropertyList.getCollection("columns");
            safeSQL = new SafeSQL();
            extendedcolumns = this.queryProcessor.getPreparedSqlDataSet("select * from sysextendedcolumn where tableid=" + safeSQL.addVar(sdcPropertyList.getProperty("tableid")), safeSQL.getValues());
        } else {
            safeSQL = new SafeSQL();
            DataSet columnds = this.queryProcessor.getPreparedSqlDataSet("select * from syscolumn where tableid=" + safeSQL.addVar(tableid) + " order by columnsequence", safeSQL.getValues());
            columns = new PropertyListCollection();
            int colcount = columnds.getColumnCount();
            for (int i2 = 0; i2 < columnds.getRowCount(); ++i2) {
                PropertyList column = new PropertyList();
                for (int c = 0; c < colcount; ++c) {
                    column.setProperty(columnds.getColumnId(c), columnds.getValue(i2, columnds.getColumnId(c)));
                }
                columns.add(column);
            }
        }
        ArrayList displaycolumns = null;
        if (this.sdcid != null) {
            displaycolumns = AdhocQueryPageUtil.getSDCColumnPLCollection(this.sdcid, this.pagedata, sdcPropertyList != null ? sdcPropertyList.getCollection("columns") : null, this.tp);
        }
        boolean isPageDefined = false;
        if (displaycolumns == null || displaycolumns.size() == 0) {
            displaycolumns = new PropertyListCollection();
            for (i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                column.setProperty("columndesc", column.getProperty("columnlabel"));
                columnid = column.getProperty("columnid");
                if (this.sdcid == null) {
                    displaycolumns.add(column);
                    continue;
                }
                if (sdcPropertyList != null && this.adhocmetadata.isSearchableColumn(sdcPropertyList.getProperty("tableid"), columnid) && !"Y".equals(column.getProperty("pkflag"))) {
                    displaycolumns.add(column);
                    continue;
                }
                if ("sdidatacapture".equals(tableid)) {
                    if (!"sdcid".equals(columnid) && !"keyid1".equals(columnid) && !"keyid2".equals(columnid) && !"keyid3".equals(columnid) && !"datacaptureid".equals(columnid)) continue;
                    displaycolumns.add(column);
                    continue;
                }
                displaycolumns.add(column);
            }
            for (i = 0; i < extendedcolumns.getRowCount(); ++i) {
                if (!"Y".equals(extendedcolumns.getString(i, "searchableflag"))) continue;
                PropertyList extcolumn = new PropertyList();
                extcolumn.setProperty("columnid", extendedcolumns.getString(i, "columnid"));
                extcolumn.setProperty("columndesc", extendedcolumns.getString(i, "columndesc"));
                extcolumn.setProperty("datatype", extendedcolumns.getString(i, "datatype"));
                displaycolumns.add(extcolumn);
            }
        } else {
            isPageDefined = true;
        }
        for (i = 0; i < displaycolumns.size(); ++i) {
            PropertyList columnPropertyList = ((PropertyListCollection)displaycolumns).getPropertyList(i);
            columnid = columnPropertyList.getProperty("columnid");
            String mode = columnPropertyList.getProperty("mode");
            if ("hidden".equals(mode)) continue;
            if (isPageDefined) {
                String title;
                if (columnid.length() > 0) {
                    TreeNode treenode;
                    int c;
                    String columntitle = columnPropertyList.getProperty("title");
                    boolean isRealColumn = false;
                    for (c = 0; c < columns.size(); ++c) {
                        if (!columnid.equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                        columnPropertyList = columns.getPropertyList(c);
                        columnPropertyList.setProperty("mode", mode);
                        if (columntitle.length() > 0) {
                            columnPropertyList.setProperty("columndesc", columntitle);
                        } else {
                            columnPropertyList.setProperty("columndesc", this.tp.translate(columnPropertyList.getProperty("columndesc")));
                        }
                        isRealColumn = true;
                        break;
                    }
                    if (!isRealColumn) {
                        for (c = 0; c < extendedcolumns.getRowCount(); ++c) {
                            if (!columnid.equals(extendedcolumns.getString(c, "columnid"))) continue;
                            columnPropertyList.setProperty("columnid", columnid);
                            columnPropertyList.setProperty("columndefinition", extendedcolumns.getString(c, "columndefinition"));
                            if (columntitle.length() > 0) {
                                columnPropertyList.setProperty("columndesc", columntitle);
                            } else {
                                columnPropertyList.setProperty("columndesc", this.tp.translate(extendedcolumns.getString(c, "columndesc")));
                            }
                            columnPropertyList.setProperty("datatype", extendedcolumns.getString(c, "datatype"));
                            break;
                        }
                    }
                    if ((treenode = this.getTreeNode(tableid, columnPropertyList, links)) == null) continue;
                    childNodes.add(treenode);
                    continue;
                }
                if (columnPropertyList.getProperty("sdidetail").length() <= 0) continue;
                String detailname = columnPropertyList.getProperty("sdidetail");
                String string = title = columnPropertyList.getProperty("title").length() == 0 ? detailname : columnPropertyList.getProperty("title");
                if (!"Data Entry".equals(detailname)) continue;
                childNodes.add(this.getDetailDetailRootNode(title, "dataentryroot", "WEB-CORE/images/gif/Tests.gif"));
                continue;
            }
            childNodes.add(this.getTreeNode(tableid, columnPropertyList, links));
        }
        return childNodes;
    }

    protected TreeNode getDetailDetailRootNode(String title, String rootname, String nodeImage) {
        TreeNode rootnode = new TreeNode();
        rootnode.setNodeimage(nodeImage);
        String nodeid = this.detailname + "." + rootname;
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
        } else {
            throw new AssertionError((Object)"Should be dataentryroot, fieldroot or attributeroot");
        }
        return rootnode;
    }
}

