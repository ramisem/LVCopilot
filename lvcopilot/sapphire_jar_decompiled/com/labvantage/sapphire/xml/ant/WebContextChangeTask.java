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

public class WebContextChangeTask
extends BasePackagerTask {
    private String applicationWebContext;
    private String applicationWarName;

    public void setApplicationWebContext(String applicationWebContext) {
        this.applicationWebContext = applicationWebContext;
    }

    public void setApplicationWarName(String applicationWarName) {
        this.applicationWarName = applicationWarName;
    }

    public void execute() {
        try {
            BaseConverter converter = BaseConverter.getInstance(this.applicationServer);
            converter.changeWebContextName(this.source, this.target, this.applicationWebContext, this.applicationWarName);
        }
        catch (Exception e) {
            throw new BuildException("Failed to change web context name. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

