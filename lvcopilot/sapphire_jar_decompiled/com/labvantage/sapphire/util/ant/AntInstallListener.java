/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 *  org.apache.tools.ant.BuildEvent
 *  org.apache.tools.ant.BuildListener
 */
package com.labvantage.sapphire.util.ant;

import com.labvantage.sapphire.Trace;
import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

public class AntInstallListener
implements BuildListener {
    private JspWriter jspOut;
    private long jspScrollCounter = 0L;
    private long jspScrollRate = 10L;
    private boolean logBuild = true;
    private boolean logTargets = true;
    private boolean logTasks = false;
    private boolean logMessages = true;
    private boolean logEcho = true;
    private boolean verboseTaskOutput = true;

    public AntInstallListener() {
    }

    public AntInstallListener(boolean logBuild, boolean logTargets, boolean logTasks, boolean logMessages, boolean logEcho) {
        this.logBuild = logBuild;
        this.logTargets = logTargets;
        this.logTasks = logTasks;
        this.logMessages = logMessages;
        this.logEcho = logEcho;
    }

    public AntInstallListener(boolean logBuild, boolean logTargets, boolean logTasks, boolean logMessages, boolean logEcho, int scrollRate) {
        this.logBuild = logBuild;
        this.logTargets = logTargets;
        this.logTasks = logTasks;
        this.logMessages = logMessages;
        this.logEcho = logEcho;
        this.jspScrollRate = scrollRate;
    }

    public AntInstallListener(JspWriter jspOut) {
        this.jspOut = jspOut;
    }

    public AntInstallListener(JspWriter jspOut, boolean verboseTaskOutput) {
        this.jspOut = jspOut;
        this.verboseTaskOutput = verboseTaskOutput;
    }

    public void buildStarted(BuildEvent event) {
        if (this.logBuild) {
            this.log("Started build.");
        }
    }

    public void buildFinished(BuildEvent event) {
        if (this.logBuild) {
            this.log("Finished build.");
        }
    }

    public void targetStarted(BuildEvent event) {
        if (this.logTargets) {
            String desc = event.getTarget().getDescription();
            this.log("");
            this.log((this.jspOut != null ? "<b>" : "") + "Started target: " + event.getTarget() + (desc != null && desc.length() > 0 ? " (" + desc + ")" : "") + (this.jspOut != null ? "</b>" : ""));
        }
    }

    public void targetFinished(BuildEvent event) {
        if (this.logTargets) {
            this.log("Finished target: " + event.getTarget());
        }
    }

    public void taskStarted(BuildEvent event) {
        if (this.logTasks) {
            this.log("Started task: " + event.getTask().getTaskName());
        }
    }

    public void taskFinished(BuildEvent event) {
        if (this.logTasks) {
            this.log("Finshed task: " + event.getTask().getTaskName());
        }
    }

    public void messageLogged(BuildEvent event) {
        if (event.getTarget() != null && event.getTask() != null) {
            if (this.verboseTaskOutput) {
                if (this.logMessages) {
                    this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
                }
                if (!this.logMessages && this.logEcho && event.getTask().getTaskName().equalsIgnoreCase("echo")) {
                    this.log(event.getMessage());
                }
            } else if (event.getTask().getTaskName().equalsIgnoreCase("unjar") || event.getTask().getTaskName().equalsIgnoreCase("unwar")) {
                if (!event.getMessage().startsWith("expanding ") && !event.getMessage().startsWith("extracting ")) {
                    this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
                }
            } else if (event.getTask().getTaskName().equalsIgnoreCase("jar") || event.getTask().getTaskName().equalsIgnoreCase("war") || event.getTask().getTaskName().equalsIgnoreCase("ear")) {
                if (!event.getMessage().startsWith("adding ")) {
                    this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
                }
            } else if (event.getTask().getTaskName().equalsIgnoreCase("delete")) {
                if (event.getMessage().contains("files from")) {
                    this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
                }
            } else if (event.getTask().getTaskName().equalsIgnoreCase("antcall")) {
                if (event.getMessage().contains("files from")) {
                    this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
                }
            } else {
                this.log("\t[" + event.getTask().getTaskName() + "] " + event.getMessage());
            }
        }
    }

    public void log(String message) {
        try {
            if (this.jspOut != null) {
                if (this.jspScrollCounter % this.jspScrollRate == 0L || message.contains("NOTE") || message.contains("WARNING")) {
                    if (message.contains("NOTE")) {
                        message = "<font color=\"blue\">" + message + "</font>";
                    }
                    if (message.contains("WARNING")) {
                        message = "<font color=\"orange\">" + message + "</font>";
                    }
                    this.jspOut.println(message + "<br id=\"" + this.jspScrollCounter + "\"/><script>document.getElementById( \"" + this.jspScrollCounter + "\" ).scrollIntoView( true )</script>");
                } else {
                    this.jspOut.println(message + "<br/>");
                }
                this.jspOut.flush();
                ++this.jspScrollCounter;
            } else {
                System.out.println(message);
            }
            Trace.logInfo(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

