/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import java.io.File;
import org.apache.tools.ant.Task;

public class BasePackagerTask
extends Task {
    protected String applicationServer;
    protected File dir;
    protected File source;
    protected File target;

    public void setApplicationServer(String applicationServer) {
        this.applicationServer = applicationServer;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    protected void defineStandardSource() {
        if (this.source == null && this.target == null && this.dir != null) {
            String ddfile = this.applicationServer.equalsIgnoreCase("weblogic") ? "weblogic-ejb-jar.xml" : (this.applicationServer.equalsIgnoreCase("websphere") ? "ibm-ejb-jar-bnd.xmi" : (this.applicationServer.toLowerCase().startsWith("jboss") ? "jboss.xml" : ""));
            this.source = new File(this.dir, ddfile);
        }
    }
}

