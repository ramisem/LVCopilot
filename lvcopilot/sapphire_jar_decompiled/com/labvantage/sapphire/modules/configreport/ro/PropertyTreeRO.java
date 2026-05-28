/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeRenderer;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.w3c.dom.Node;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.SDIData;
import sapphire.xml.DOMUtil;

public class PropertyTreeRO
extends BaseSDCRO {
    private Node currentptreenode;
    private PropertyTree currentPropertyTree;

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        if (this.getPrimaryValue("valuetree") != null && this.getPrimaryValue("valuetree").length() > 0) {
            this.currentPropertyTree = new PropertyTree();
            this.currentPropertyTree.setValueXML(this.getPrimaryValue("valuetree"));
            try {
                this.currentptreenode = WebAdminProcessor.getPropertyTreeNode(DOMUtil.getNewDocument(this.getPrimaryValue("valuetree"), false));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to determine the property tree node from valuetree");
            }
            if (this.getPrimaryValue("definitiontree") != null && this.getPrimaryValue("definitiontree").length() > 0) {
                this.currentPropertyTree.setDefinitionXML(this.getPrimaryValue("definitiontree"));
            }
        }
    }

    @Override
    public int gotoSection(SDI sdi) {
        super.gotoSection(sdi);
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1())) continue;
            this.currentPropertyTree = new PropertyTree();
            try {
                if (!this.dataSource.equals("XMLREPORT")) {
                    if (this.getPrimaryValue("valuetree") != null && this.getPrimaryValue("valuetree").length() > 0) {
                        this.currentPropertyTree.setValueXML(this.getPrimaryValue("valuetree"));
                        this.currentptreenode = WebAdminProcessor.getPropertyTreeNode(DOMUtil.getNewDocument(this.getPrimaryValue("valuetree"), false));
                    }
                    if (this.getPrimaryValue("definitiontree") != null && this.getPrimaryValue("definitiontree").length() > 0) {
                        this.currentPropertyTree.setDefinitionXML(this.getPrimaryValue("definitiontree"));
                    }
                    return i;
                }
                String valueTree = this.getValueTreeFromXMLReport(this.refReportFolder, sdi);
                String definitionTree = this.getDefinitionTreeFromXMLReport(this.refReportFolder, sdi);
                if (valueTree != null && valueTree.length() > 0) {
                    this.currentPropertyTree.setValueXML(valueTree);
                    this.currentptreenode = WebAdminProcessor.getPropertyTreeNode(DOMUtil.getNewDocument(valueTree, false));
                    this.currentSDIData.getDataset("primary").setValue(0, "valuetree", valueTree);
                }
                if (definitionTree != null && definitionTree.length() > 0) {
                    this.currentPropertyTree.setDefinitionXML(definitionTree);
                    this.currentSDIData.getDataset("primary").setValue(0, "definitiontree", definitionTree);
                }
                return i;
            }
            catch (Exception e) {
                Trace.log("Failed to create propertytree for:" + sdi.getKeyid1());
                e.printStackTrace();
            }
        }
        this.currentSDI = null;
        this.currentSDIData = null;
        return -1;
    }

    public String getPropertyTreeId() {
        return this.getKeyid1();
    }

    public String getPropertyTreeType() {
        return this.getPrimaryValue("propertytreetype");
    }

    public String getPropertyTreeDesc() {
        return this.getPrimaryValue("propertytreedesc");
    }

    public String getObjectName() {
        return this.getPrimaryValue("objectname");
    }

    public PropertyTree getPropertyTree() {
        return this.currentPropertyTree;
    }

    public Node getNodeTree() throws Exception {
        return this.currentptreenode;
    }

    public ArrayList getSubNodes(Node node) {
        return PropertyTreeRenderer.getChildNodes(node);
    }

    private String getValueTreeFromXMLReport(String refReportFolder, SDI currentSDI) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(currentSDI).replace(".xml", "_valuetree.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("valuetree does not exist in the ref report");
        }
        return null;
    }

    private String getValueTreeFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_valuetree.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("valuetree does not exist in the ref report");
        }
        return null;
    }

    private String getDefinitionTreeFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_deftree.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("definitiontree does not exist in the ref report");
        }
        return "";
    }

    private String getDefinitionTreeFromXMLReport(String refReportFolder, SDI currentSDI) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(currentSDI).replace(".xml", "_deftree.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("definitiontree does not exist in the ref report");
        }
        return "";
    }
}

