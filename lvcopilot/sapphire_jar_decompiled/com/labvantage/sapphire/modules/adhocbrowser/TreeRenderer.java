/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNodeRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class TreeRenderer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90675 $";
    protected String rootsdcid = "";
    protected String linkcolumnid = "";
    protected AdhocMetaData adhocmetadata;
    protected QueryProcessor queryProcessor;
    protected SDCProcessor sdcProcessor;
    protected TranslationProcessor tp;
    protected ArrayList nodes;
    protected PropertyList pagedata;

    public TreeRenderer(PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        this.pagedata = pagedata;
        this.adhocmetadata = adhocmetadata;
        this.queryProcessor = queryProcessor;
        this.sdcProcessor = sdcProcessor;
        this.tp = tp;
    }

    public void setRootsdcid(String rootsdcid) {
        this.rootsdcid = rootsdcid;
    }

    public void setLinkcolumnid(String linkcolumnid) {
        this.linkcolumnid = linkcolumnid;
    }

    protected ArrayList getChildNodes() throws SapphireException {
        return new ArrayList();
    }

    public PropertyList getNodePropertyList() throws SapphireException {
        this.nodes = this.getChildNodes();
        PropertyList pl = new PropertyList();
        pl.setProperty("childnodes", TreeNodeRenderer.getChildNodesCollection(this.nodes));
        return pl;
    }

    protected TreeNode getTreeNode(String columnidprefix, PropertyList column, DataSet links) {
        String nodelabeltrans;
        String columnid = column.getProperty("columnid");
        String columntype = column.getProperty("datatype");
        String columndefinition = column.getProperty("columndefinition");
        String linksdcid = "";
        String linksdcimage = "";
        String nodeimg = column.getProperty("img");
        boolean canDrillDown = false;
        String nodeid = columnid;
        if (columnidprefix.length() > 0) {
            nodeid = columnidprefix + "." + columnid;
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("sdccolumnid", columnid);
        filterMap.put("linktype", "F");
        if (links != null && links.findRow(filterMap) >= 0) {
            nodeimg = "dropdownlist".equals(column.getProperty("mode")) ? "WEB-CORE/imageref/flat/16/flat_black_sort_down_dropdown.svg" : "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg";
            linksdcid = links.getString(links.findRow(filterMap), "linksdcid");
            linksdcimage = "WEB-CORE/pagetypes/adhocbrowser/images/sdc.gif";
            String linktableid = this.adhocmetadata.getTableid(linksdcid);
            if (linksdcid.equals(this.rootsdcid)) {
                canDrillDown = false;
            } else if (columnidprefix.length() > 0 && columnidprefix.indexOf(linktableid) >= 0) {
                canDrillDown = true;
            } else if (this.adhocmetadata.isSearchableTable(linktableid)) {
                canDrillDown = true;
            }
        }
        if (nodeimg.length() == 0) {
            filterMap.put("linktype", "V");
            if (links != null && (links.findRow(filterMap) >= 0 || "dropdownlist".equals(column.getProperty("mode")))) {
                nodeimg = "WEB-CORE/imageref/flat/16/flat_black_sort_down_dropdown.svg";
            } else if ("D".equals(columntype)) {
                nodeimg = "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg";
            } else if ("N".equals(columntype) || "R".equals(columntype)) {
                nodeimg = "WEB-CORE/imageref/flat/16/flat_black_type_bit.svg";
                columntype = "N";
            } else {
                nodeimg = "C".equals(columntype) ? "WEB-CORE/imageref/flat/16/flat_black_page.svg" : "WEB-CORE/imageref/flat/16/flat_black_page.svg";
            }
        }
        if (column.getProperty("img").length() > 0) {
            nodeimg = column.getProperty("img");
        }
        String nodelabel = column.getProperty("columnlabel").length() > 0 ? column.getProperty("columnlabel") : column.getProperty("columndesc");
        String string = nodelabeltrans = column.getProperty("columndesctrans").length() > 0 ? column.getProperty("columndesctrans") : column.getProperty("columnlabel");
        if (nodelabel.length() == 0) {
            nodelabel = columnid;
        }
        TreeNode treeNode = new TreeNode();
        treeNode.setNodeid(nodeid);
        treeNode.setNodeimage(nodeimg);
        treeNode.setNodeimage2(linksdcimage);
        treeNode.setNodelabel(nodelabel);
        treeNode.setNodelabeltrans(nodelabeltrans);
        treeNode.setCanDrillDown(canDrillDown);
        treeNode.setColumntype(columntype);
        return treeNode;
    }
}

