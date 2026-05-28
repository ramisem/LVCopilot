/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.BaseConverter;
import com.labvantage.sapphire.xml.ant.BasePackagerTask;
import org.apache.tools.ant.BuildException;

public class ApplicationNameChangeTask
extends BasePackagerTask {
    private String applicationName;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void execute() {
        try {
            BaseConverter converter = BaseConverter.getInstance(this.applicationServer);
            converter.changeApplicationName(this.source, this.target, this.applicationName);
        }
        catch (Exception e) {
            throw new BuildException("Failed to change application name. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

