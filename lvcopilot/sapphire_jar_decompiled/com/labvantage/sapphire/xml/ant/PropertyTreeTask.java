/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.ant.NodeTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class PropertyTreeTask
extends Task {
    private String propertytreeid;
    private File file;
    private String exists;
    private String notexists;
    private String explode;
    ArrayList nodes = new ArrayList();

    public void setPropertytreeid(String propertytreeid) {
        this.propertytreeid = propertytreeid;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public void setNotexists(String notexists) {
        this.notexists = notexists;
    }

    public void setExplode(String explode) {
        this.explode = explode;
    }

    public void addConfiguredNode(NodeTask node) {
        this.nodes.add(node);
    }

    public PropertyTreeTransfer getPropertyTreeTransfer() {
        PropertyTreeTransfer propertyTreeTransfer = new PropertyTreeTransfer();
        propertyTreeTransfer.setId(this.propertytreeid);
        propertyTreeTransfer.setFile(this.file);
        propertyTreeTransfer.setExplode(this.explode);
        if (this.exists != null && this.exists.length() > 0) {
            propertyTreeTransfer.setExists(this.exists.toLowerCase());
        }
        if (this.notexists != null && this.notexists.length() > 0) {
            propertyTreeTransfer.setNotexists(this.notexists.toLowerCase());
        }
        NodeList nodeList = new NodeList();
        for (int i = 0; i < this.nodes.size(); ++i) {
            nodeList.add(((NodeTask)((Object)this.nodes.get(i))).getNode());
        }
        propertyTreeTransfer.setNodeList(nodeList);
        return propertyTreeTransfer;
    }
}

