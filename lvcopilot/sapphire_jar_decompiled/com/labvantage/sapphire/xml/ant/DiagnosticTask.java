/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.build.diagnostics.BaseBuildDiagnostic;
import com.labvantage.sapphire.xml.ant.ClassNameTask;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class DiagnosticTask
extends Task {
    ConnectionTask connection;
    ArrayList classnames = new ArrayList();
    String classroot;
    String buildhome;

    public void setClassroot(String classroot) {
        this.classroot = classroot;
    }

    public void setbuildhome(String buildhome) {
        this.buildhome = buildhome;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.classnames.size() == 0) {
            throw new BuildException("No diagnostics classes has been defined");
        }
        DBUtil dbu = this.connection.getConnection(true);
        String classname = "";
        try {
            for (int i = 0; i < this.classnames.size(); ++i) {
                classname = ((ClassNameTask)((Object)this.classnames.get(i))).getClassname();
                Class<?> c = Class.forName(this.classroot != null && this.classroot.length() > 0 ? this.classroot + "." + classname : classname);
                BaseBuildDiagnostic diagnostic = (BaseBuildDiagnostic)c.newInstance();
                diagnostic.setDatabase(dbu);
                diagnostic.setbuildhome(this.buildhome);
                diagnostic.run();
            }
        }
        catch (Exception e) {
            throw new BuildException("Error running diagnostic class '" + (this.classroot != null && this.classroot.length() > 0 ? this.classroot + "." + classname : classname) + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
        finally {
            dbu.reset();
        }
        this.log("Execute diagnostic '" + this.classroot + "' complete");
        this.log("Execute diagnostic buildhome : '" + this.buildhome + "'");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    public void addConfiguredClassName(ClassNameTask classname) {
        this.classnames.add(classname);
    }
}

