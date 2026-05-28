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

public class EjbJndiNameChangeTask
extends BasePackagerTask {
    private String ejbPrefix;

    public void setEjbPrefix(String ejbPrefix) {
        this.ejbPrefix = ejbPrefix;
    }

    public void execute() {
        try {
            this.defineStandardSource();
            BaseConverter converter = BaseConverter.getInstance(this.applicationServer);
            converter.convertEjbJndiName(this.source, this.target, this.ejbPrefix);
        }
        catch (Exception e) {
            throw new BuildException("Failed to change ejb jndi names. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

