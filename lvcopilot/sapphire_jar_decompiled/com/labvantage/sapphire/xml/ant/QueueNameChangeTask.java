/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.ant.BasePackagerTask;
import org.apache.tools.ant.BuildException;

public class QueueNameChangeTask
extends BasePackagerTask {
    public void setApqJndiName(String apqJndiName) {
    }

    public void setTdqJndiName(String tdqJndiName) {
    }

    public void execute() {
        try {
            this.defineStandardSource();
            this.log("JMS is no longer used in LabVantage - QueueNameChangeTask only maintained for backwards compatibility.");
        }
        catch (Exception e) {
            throw new BuildException("Failed to change queue names. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

