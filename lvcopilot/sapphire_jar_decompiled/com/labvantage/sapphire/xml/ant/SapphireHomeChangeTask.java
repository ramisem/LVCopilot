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

public class SapphireHomeChangeTask
extends BasePackagerTask {
    private String sapphireHome;

    public void setSapphireHome(String sapphireHome) {
        this.sapphireHome = sapphireHome;
    }

    public void execute() {
        try {
            BaseConverter converter = BaseConverter.getInstance(this.applicationServer);
            converter.changeSapphireHome(this.source, this.target, this.sapphireHome);
        }
        catch (Exception e) {
            throw new BuildException("Failed to change SAPPHIRE HOME. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

