/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.ant.PropertyTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class PropertyListTask
extends Task {
    private String propertylistid;
    private File file;
    private String exists = "replace";
    private String notexists = "add";
    ArrayList properties = new ArrayList();

    public void setId(String propertylistid) {
        this.propertylistid = propertylistid;
    }

    public void setPropertylistid(String propertylistid) {
        this.propertylistid = propertylistid;
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

    public void addConfiguredProperty(PropertyTask property) {
        this.properties.add(property);
    }

    public PropertyListTransfer getPropertyListTransfer() {
        PropertyListTransfer propertyListTransfer = new PropertyListTransfer();
        propertyListTransfer.setId(this.propertylistid);
        propertyListTransfer.setFile(this.file);
        propertyListTransfer.setExists(this.exists);
        propertyListTransfer.setNotexists(this.notexists);
        for (int i = 0; i < this.properties.size(); ++i) {
            PropertyTask propertyTask = (PropertyTask)((Object)this.properties.get(i));
            if (propertyTask.getCollection() == null && propertyTask.getPropertyList() == null) {
                propertyListTransfer.setProperty(propertyTask.getPropertyid(), propertyTask.getPropertyid());
                continue;
            }
            if (propertyTask.getCollection() != null) {
                propertyListTransfer.setProperty(propertyTask.getPropertyid(), propertyTask.getCollection());
                continue;
            }
            if (propertyTask.getPropertyList() == null) continue;
            propertyListTransfer.setProperty(propertyTask.getPropertyid(), propertyTask.getPropertyList());
        }
        return propertyListTransfer;
    }
}

