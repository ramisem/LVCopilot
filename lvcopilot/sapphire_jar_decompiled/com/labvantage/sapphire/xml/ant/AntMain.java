/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.BuildListener
 *  org.apache.tools.ant.BuildLogger
 *  org.apache.tools.ant.DefaultLogger
 *  org.apache.tools.ant.Main
 *  org.apache.tools.ant.Project
 *  org.apache.tools.ant.ProjectHelper
 *  org.apache.tools.ant.Target
 *  org.apache.tools.ant.input.DefaultInputHandler
 *  org.apache.tools.ant.input.InputHandler
 */
package com.labvantage.sapphire.xml.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import sapphire.SapphireException;

public class AntMain {
    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    private static String antVersion = null;
    private boolean projectHelp = false;
    private boolean emacsMode = false;
    private File buildFile;
    private Vector targets = new Vector(5);
    private Vector listeners = new Vector(5);
    private Properties definedProps = new Properties();
    private String loggerClassname = null;
    private String inputHandlerClassname = null;
    private int msgOutputLevel = 2;

    public void setLogFile(File logFile) throws SapphireException {
        try {
            out = err = new PrintStream(new FileOutputStream(logFile));
        }
        catch (IOException ioe) {
            throw new SapphireException("Cannot write on the specified log file. Make sure the path exists and you have write permissions. Exception: " + ioe.getMessage());
        }
        catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new SapphireException("You must specify a log file when using the -log argument");
        }
    }

    public void setBuildFile(File buildFile) {
        this.buildFile = buildFile;
    }

    public void addListener(Object listener) {
        this.listeners.addElement(listener);
    }

    public void setTarget(String target) {
        this.targets.addElement(target);
    }

    public void setProperty(String name, String value) {
        this.definedProps.put(name, value);
    }

    public static void runFile(String filename, String target, String logfile, HashMap antParams, Object listener) throws Exception {
        AntMain ant = new AntMain();
        ant.setBuildFile(new File(filename));
        ant.setTarget(target);
        ant.addListener(listener);
        if (logfile != null && logfile.length() > 0) {
            ant.setLogFile(new File(logfile));
        }
        for (String param : antParams.keySet()) {
            if (antParams.get(param) == null) continue;
            ant.setProperty(param, (String)antParams.get(param));
        }
        ant.runBuild();
    }

    public void runBuild() throws BuildException {
        Project project = new Project();
        Throwable error = null;
        try {
            this.addBuildListeners(project);
            this.addInputHandler(project);
            if (!this.projectHelp) {
                project.fireBuildStarted();
            }
            project.init();
            project.setUserProperty("ant.version", AntMain.getAntVersion());
            Enumeration<Object> e = this.definedProps.keys();
            while (e.hasMoreElements()) {
                String arg = (String)e.nextElement();
                String value = (String)this.definedProps.get(arg);
                project.setUserProperty(arg, value);
            }
            project.setUserProperty("ant.file", this.buildFile.getAbsolutePath());
            ProjectHelper.configureProject((Project)project, (File)this.buildFile);
            if (this.projectHelp) {
                AntMain.printDescription(project);
                AntMain.printTargets(project, this.msgOutputLevel > 2);
                return;
            }
            if (this.targets.size() == 0) {
                this.targets.addElement(project.getDefaultTarget());
            }
            project.executeTargets(this.targets);
        }
        catch (RuntimeException exc) {
            error = exc;
            exc.printStackTrace();
            throw exc;
        }
        catch (Error err) {
            error = err;
            err.printStackTrace();
            throw err;
        }
        finally {
            if (!this.projectHelp) {
                project.fireBuildFinished(error);
            }
        }
    }

    private void addBuildListeners(Project project) {
        project.addBuildListener((BuildListener)this.createLogger());
        for (int i = 0; i < this.listeners.size(); ++i) {
            Object o = this.listeners.elementAt(i);
            if (o instanceof String) {
                String className = (String)this.listeners.elementAt(i);
                try {
                    BuildListener listener = (BuildListener)Class.forName(className).newInstance();
                    project.addBuildListener(listener);
                    continue;
                }
                catch (Throwable exc) {
                    throw new BuildException("Unable to instantiate listener " + className, exc);
                }
            }
            project.addBuildListener((BuildListener)o);
        }
    }

    private BuildLogger createLogger() {
        DefaultLogger logger = null;
        if (this.loggerClassname != null) {
            try {
                logger = (BuildLogger)Class.forName(this.loggerClassname).newInstance();
            }
            catch (ClassCastException e) {
                System.err.println("The specified logger class " + this.loggerClassname + " does not implement the BuildLogger interface");
                throw new RuntimeException();
            }
            catch (Exception e) {
                System.err.println("Unable to instantiate specified logger class " + this.loggerClassname + " : " + e.getClass().getName());
                throw new RuntimeException();
            }
        } else {
            logger = new DefaultLogger();
        }
        logger.setMessageOutputLevel(this.msgOutputLevel);
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);
        logger.setEmacsMode(this.emacsMode);
        return logger;
    }

    private void addInputHandler(Project project) {
        DefaultInputHandler handler = null;
        if (this.inputHandlerClassname == null) {
            handler = new DefaultInputHandler();
        } else {
            try {
                handler = (InputHandler)Class.forName(this.inputHandlerClassname).newInstance();
            }
            catch (ClassCastException e) {
                String msg = "The specified input handler class " + this.inputHandlerClassname + " does not implement the InputHandler interface";
                throw new BuildException(msg);
            }
            catch (Exception e) {
                String msg = "Unable to instantiate specified input handler class " + this.inputHandlerClassname + " : " + e.getClass().getName();
                throw new BuildException(msg);
            }
        }
        project.setInputHandler((InputHandler)handler);
    }

    public static synchronized String getAntVersion() throws BuildException {
        if (antVersion == null) {
            try {
                Properties props = new Properties();
                InputStream in = Main.class.getResourceAsStream("/org/apache/tools/ant/version.txt");
                props.load(in);
                in.close();
                StringBuffer msg = new StringBuffer();
                msg.append("Apache Ant version ");
                msg.append(props.getProperty("VERSION"));
                msg.append(" compiled on ");
                msg.append(props.getProperty("DATE"));
                antVersion = msg.toString();
            }
            catch (IOException ioe) {
                throw new BuildException("Could not load the version information:" + ioe.getMessage());
            }
            catch (NullPointerException npe) {
                throw new BuildException("Could not load the version information.");
            }
        }
        return antVersion;
    }

    private static void printDescription(Project project) {
        if (project.getDescription() != null) {
            project.log(project.getDescription());
        }
    }

    private static void printTargets(Project project, boolean printSubTargets) {
        String defaultTarget;
        int maxLength = 0;
        Enumeration ptargets = project.getTargets().elements();
        Vector<String> topNames = new Vector<String>();
        Vector<String> topDescriptions = new Vector<String>();
        Vector<String> subNames = new Vector<String>();
        while (ptargets.hasMoreElements()) {
            int pos;
            Target currentTarget = (Target)ptargets.nextElement();
            String targetName = currentTarget.getName();
            String targetDescription = currentTarget.getDescription();
            if (targetDescription == null) {
                pos = AntMain.findTargetPosition(subNames, targetName);
                subNames.insertElementAt(targetName, pos);
                continue;
            }
            pos = AntMain.findTargetPosition(topNames, targetName);
            topNames.insertElementAt(targetName, pos);
            topDescriptions.insertElementAt(targetDescription, pos);
            if (targetName.length() <= maxLength) continue;
            maxLength = targetName.length();
        }
        AntMain.printTargets(project, topNames, topDescriptions, "Main targets:", maxLength);
        if (topNames.size() == 0) {
            printSubTargets = true;
        }
        if (printSubTargets) {
            AntMain.printTargets(project, subNames, null, "Subtargets:", 0);
        }
        if ((defaultTarget = project.getDefaultTarget()) != null && !"".equals(defaultTarget)) {
            project.log("Default target: " + defaultTarget);
        }
    }

    private static void printTargets(Project project, Vector names, Vector descriptions, String heading, int maxlen) {
        String lSep = System.getProperty("line.separator");
        String spaces = "    ";
        while (spaces.length() <= maxlen) {
            spaces = spaces + spaces;
        }
        StringBuffer msg = new StringBuffer();
        msg.append(heading + lSep + lSep);
        for (int i = 0; i < names.size(); ++i) {
            msg.append(" ");
            msg.append(names.elementAt(i));
            if (descriptions != null) {
                msg.append(spaces.substring(0, maxlen - ((String)names.elementAt(i)).length() + 2));
                msg.append(descriptions.elementAt(i));
            }
            msg.append(lSep);
        }
        project.log(msg.toString());
    }

    private static int findTargetPosition(Vector names, String name) {
        int res = names.size();
        for (int i = 0; i < names.size() && res == names.size(); ++i) {
            if (name.compareTo((String)names.elementAt(i)) >= 0) continue;
            res = i;
        }
        return res;
    }
}

