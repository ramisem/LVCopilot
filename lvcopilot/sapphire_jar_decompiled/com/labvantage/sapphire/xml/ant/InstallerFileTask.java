/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.Logger;
import java.io.File;
import org.apache.tools.ant.Task;

public class InstallerFileTask
extends Task
implements Logger {
    String target = "none";
    String jar;
    String file;
    String type = "F";
    File path;
    String description;
    String action;
    String dbms;
    String platform;

    public String getTarget() {
        return this.target.equals("none") ? "misc" : this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getJar() {
        return this.jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getPath() {
        return this.path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDbms() {
        return this.dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return this.platform;
    }
}

