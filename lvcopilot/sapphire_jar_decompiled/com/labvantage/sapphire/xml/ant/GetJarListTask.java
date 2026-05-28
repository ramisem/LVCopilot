/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.platform.Configuration;
import org.apache.tools.ant.Task;
import sapphire.util.StringUtil;

public class GetJarListTask
extends Task {
    private String propertyName;
    private String customJars;
    private boolean prefix = true;
    private String separator = ";";

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setCustomJars(String customJars) {
        this.customJars = customJars;
    }

    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void execute() {
        this.getProject().setProperty(this.propertyName, this.getJarList());
    }

    public String getJarList() {
        StringBuffer value = new StringBuffer();
        if (this.prefix) {
            this.getCustomJars(value);
        }
        String[] jarList = Configuration.getCommonJarList();
        for (int i = 0; i < jarList.length; ++i) {
            value.append(" ").append(jarList[i]);
        }
        if (!this.prefix) {
            this.getCustomJars(value);
        }
        return value.substring(1);
    }

    private void getCustomJars(StringBuffer value) {
        if (this.customJars != null && this.customJars.length() > 0) {
            String[] jarList = StringUtil.split(this.customJars, this.separator);
            for (int i = 0; i < jarList.length; ++i) {
                if (jarList[i].length() <= 0) continue;
                value.append(" ").append(jarList[i]);
            }
        }
    }
}

