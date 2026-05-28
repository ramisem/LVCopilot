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

public class WARNameChangeTask
extends BasePackagerTask {
    private String applicationWarName;

    public void setApplicationWarName(String applicationWarName) {
        this.applicationWarName = applicationWarName;
    }

    public void execute() {
        try {
            BaseConverter converter = BaseConverter.getInstance(this.applicationServer);
            converter.changeWARName(this.source, this.target, this.applicationWarName);
        }
        catch (Exception e) {
            throw new BuildException("Failed to change WAR name. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

