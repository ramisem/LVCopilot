/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.xml.PropertyTree;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.cmt.Snapshot;

public class PropertyTreeSnapshot
extends SDISnapshot {
    private static final String LOGNAME = "PropertyTreeSnapshot";
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    protected Logger logger = new Logger(new LogContext("PropertyTreeSnapshot", "(none)"));
    public static final String NODEID_DEFINITION = "__DEFINITION";
    public static final String NODEID_ROOT = "__root";
    public static final String NODEID_FULL = "__FULL";
    private PropertyTree propertyTree;
    private String extendsNodeId = "";
    private String exists = "";
    private String notExists = "";

    public PropertyTreeSnapshot(PropertyTreeSnapshotItem requestedItem) {
        super(requestedItem);
    }

    public PropertyTree getPropertyTree() throws SapphireException {
        if (NODEID_FULL.equals(this.getSnapshotItem().getNodeId()) || NODEID_DEFINITION.equals(this.getSnapshotItem().getNodeId())) {
            PropertyTree propertyTree1 = null;
            SDIData sdiData = this.getSDIData();
            if (sdiData != null) {
                DataSet primary = sdiData.getDataset("primary");
                if (NODEID_FULL.equals(this.getSnapshotItem().getNodeId())) {
                    propertyTree1 = new PropertyTree(this.getSnapshotItem().getPropertyTreeId());
                    propertyTree1.setDefinitionXML(primary.getClob(0, "definitiontree"));
                    propertyTree1.setValueXML(primary.getClob(0, "valuetree"), true);
                } else if (NODEID_DEFINITION.equals(this.getSnapshotItem().getNodeId())) {
                    propertyTree1 = new PropertyTree(this.getSnapshotItem().getPropertyTreeId());
                    propertyTree1.setDefinitionXML(primary.getClob(0, "definitiontree"));
                }
            }
            return propertyTree1;
        }
        return this.propertyTree;
    }

    public void setPropertyTree(PropertyTree propertyTree) {
        this.propertyTree = propertyTree;
    }

    public String getExtendsNodeId() {
        return this.extendsNodeId;
    }

    public void setExtendsNodeId(String extendsNodeId) {
        this.extendsNodeId = extendsNodeId;
    }

    public String getExists() {
        return this.exists;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public String getNotExists() {
        return this.notExists;
    }

    public void setNotExists(String notExists) {
        this.notExists = notExists;
    }

    @Override
    public String toXML() {
        PropertyTreeSnapshotItem reqSnapshotItem = this.getSnapshotItem();
        StringBuffer xml = new StringBuffer();
        xml.append("<snapshot type='" + (Object)((Object)this.getType()) + "' id='" + reqSnapshotItem.toString() + "'>\n");
        String indentStr = "  ";
        xml.append(indentStr + "<propertytreesnapshot propertytreeid='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getPropertyTreeId()) + "' nodeid='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getNodeId()) + "' devmode='" + (this.isDevMode() ? "Y" : "N") + "' isDeleted='" + (reqSnapshotItem.isDeleted() ? "Y" : "N") + "' compcode='" + StringUtil.escapeXMLAttributeValue(this.getCompCode()) + "'>\n");
        xml.append("<sditree>\n");
        try {
            xml.append(indentStr + "<![CDATA[" + reqSnapshotItem.toJSONObject().toString(4) + "]]>\n");
        }
        catch (Exception e) {
            this.logger.error("Error in serializing SDI Link tree." + e.getMessage(), e);
        }
        xml.append("</sditree>\n");
        if (this.getSDIData() != null) {
            xml.append("<propertytreesdidata>\n");
            xml.append(this.getSDIData().toXML(4, true, true) + "\n");
            xml.append("</propertytreesdidata>\n");
        }
        if (!NODEID_FULL.equals(reqSnapshotItem.getNodeId())) {
            xml.append("<propertytreenode extendnodeid='" + this.extendsNodeId + "' exists='" + this.getExists() + "' notexists='" + this.getNotExists() + "'>\n");
            if (!NODEID_DEFINITION.equals(reqSnapshotItem.getNodeId())) {
                xml.append(this.propertyTree.toXMLString() + "\n");
            }
            xml.append("</propertytreenode>\n");
        }
        xml.append(indentStr + "</propertytreesnapshot>\n");
        xml.append("</snapshot>\n");
        return xml.toString();
    }

    @Override
    public Snapshot.Type getType() {
        return Snapshot.Type.PROPERTYTREE;
    }

    @Override
    public PropertyTreeSnapshotItem getSnapshotItem() {
        return (PropertyTreeSnapshotItem)super.getSnapshotItem();
    }
}

