/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.ant.PropertyListTask;
import java.util.ArrayList;
import org.apache.tools.ant.Task;
import sapphire.xml.PropertyListCollection;

public class CollectionTask
extends Task {
    private ArrayList propertyLists = new ArrayList();

    public void addConfiguredPropertyList(PropertyListTask propertyList) {
        this.propertyLists.add(propertyList);
    }

    public PropertyListCollection getCollection() {
        PropertyListCollection collection = new PropertyListCollection();
        for (int i = 0; i < this.propertyLists.size(); ++i) {
            collection.add(((PropertyListTask)((Object)this.propertyLists.get(i))).getPropertyListTransfer());
        }
        return collection;
    }
}

