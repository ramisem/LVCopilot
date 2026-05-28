/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.CriteriaEditor;
import java.util.ArrayList;

public class TreeNode
implements Comparable {
    private String nodeid = "";
    private String nodeimage = "";
    private String nodeimage2 = "";
    private String nodelabel = "";
    private String nodelabeltrans = "";
    private String columntitle = "";
    private String columntype = "";
    private String additionalLabelHtml = "";
    private boolean canDrillDown = false;
    private boolean isRoot = false;
    private boolean isLastNode = false;
    private ArrayList childNodes = null;
    private boolean isDragable = true;
    private boolean isCriteriaOnly = false;
    private boolean prependAdditional = false;

    public TreeNode() {
    }

    public TreeNode(String nodeid, String nodelabel, String nodeimage, boolean canDrillDown) {
        this.setNodeid(nodeid);
        this.setNodeimage(nodeimage);
        this.setNodelabel(nodelabel);
        this.setCanDrillDown(canDrillDown);
    }

    public boolean isDragable() {
        return this.isDragable;
    }

    public boolean getPrependAdditional() {
        return this.prependAdditional;
    }

    public void setDragable(boolean dragable) {
        this.isDragable = dragable;
    }

    public boolean isCriteriaOnly() {
        return this.isCriteriaOnly;
    }

    public void setIsCriteriaOnly(boolean isCriteriaOnly) {
        this.isCriteriaOnly = isCriteriaOnly;
    }

    public void setAdditionalLabelHtml(String additionalLabelHtml) {
        this.additionalLabelHtml = additionalLabelHtml;
    }

    public void setAdditionalLabelHtml(String additionalLabelHtml, boolean prepend) {
        this.additionalLabelHtml = additionalLabelHtml;
        this.prependAdditional = prepend;
    }

    public String getAdditionalLabelHtml() {
        return this.additionalLabelHtml;
    }

    public void AddChildNode(TreeNode node) {
        if (this.childNodes == null) {
            this.childNodes = new ArrayList();
        }
        this.childNodes.add(node);
    }

    public String getNodeid() {
        return this.nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = CriteriaEditor.getUniqueId() + "_" + nodeid;
    }

    public String getNodeimage() {
        return this.nodeimage;
    }

    public void setNodeimage(String nodeimage) {
        this.nodeimage = nodeimage;
    }

    public String getNodelabel() {
        return this.nodelabel;
    }

    public void setNodelabel(String nodelabel) {
        this.nodelabel = nodelabel;
    }

    public String getNodelabeltrans() {
        return this.nodelabeltrans;
    }

    public void setNodelabeltrans(String nodelabeltrans) {
        this.nodelabeltrans = nodelabeltrans;
    }

    public boolean isCanDrillDown() {
        return this.canDrillDown;
    }

    public void setCanDrillDown(boolean canDrillDown) {
        this.canDrillDown = canDrillDown;
    }

    public String getNodeimage2() {
        return this.nodeimage2;
    }

    public void setNodeimage2(String nodeimage2) {
        this.nodeimage2 = nodeimage2;
    }

    public boolean isLastNode() {
        return this.isLastNode;
    }

    public void setLastNode(boolean lastNode) {
        this.isLastNode = lastNode;
    }

    public boolean isRoot() {
        return this.isRoot;
    }

    public void setRoot(boolean root) {
        this.isRoot = root;
    }

    public ArrayList getChildNodes() {
        return this.childNodes;
    }

    public void setChildNodes(ArrayList childNodes) {
        this.childNodes = childNodes;
    }

    public String getLabelHtml() {
        return this.prependAdditional ? this.additionalLabelHtml + this.nodelabel : this.nodelabel + this.additionalLabelHtml;
    }

    public String getColumntitle() {
        if (this.columntitle.length() == 0) {
            return this.nodelabel;
        }
        return this.columntitle;
    }

    public void setColumntitle(String columntitle) {
        this.columntitle = columntitle;
    }

    public String getColumntype() {
        return this.columntype;
    }

    public void setColumntype(String columntype) {
        this.columntype = columntype;
    }

    public int compareTo(Object o) {
        return this.nodelabel.compareToIgnoreCase(((TreeNode)o).nodelabel);
    }
}

