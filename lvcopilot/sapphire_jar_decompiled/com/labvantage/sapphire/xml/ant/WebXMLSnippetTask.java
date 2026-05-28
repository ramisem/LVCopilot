/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.servlet.ConsoleController;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class WebXMLSnippetTask
extends Task {
    private String snippetid = "";
    private String webxml;
    private String snippet;
    private String action = "add";

    public void setSnippetid(String snippetid) {
        this.snippetid = snippetid;
    }

    public void setWebxml(String webxml) {
        this.webxml = webxml;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void execute() {
        try {
            if (this.snippet != null && this.snippet.length() > 0) {
                File webxmlFile = new File(this.webxml);
                File snippetFile = new File(this.snippet);
                ConsoleController.applyWebXMLSnippet(this.snippetid, webxmlFile, snippetFile, this.action);
            }
        }
        catch (Exception e) {
            throw new BuildException("Failed to change application name. Reason: " + e.getMessage(), (Throwable)e);
        }
    }
}

