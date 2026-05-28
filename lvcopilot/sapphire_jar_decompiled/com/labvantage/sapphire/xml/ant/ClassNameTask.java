/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import org.apache.tools.ant.Task;

public class ClassNameTask
extends Task {
    String classname;

    public String getClassname() {
        return this.classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }
}

