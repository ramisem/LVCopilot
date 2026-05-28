/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.xml.ant.PropertiesFileEntryTask;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PropertiesFileTask
extends Task {
    File file;
    String comment;
    ArrayList properties = new ArrayList();

    public void setFile(File file) {
        this.file = file;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addConfiguredPropertiesEntry(PropertiesFileEntryTask entryTask) {
        this.properties.add(entryTask);
    }

    public void execute() throws BuildException {
        if (this.file == null || !this.file.exists()) {
            throw new BuildException("Properties file not found");
        }
        try {
            FileInputStream fis = new FileInputStream(this.file);
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            for (int i = 0; i < this.properties.size(); ++i) {
                PropertiesFileEntryTask entryTask = (PropertiesFileEntryTask)((Object)this.properties.get(i));
                if (entryTask.getKey().equals("application.webxml") && entryTask.getValue().contains("tempwork")) {
                    entryTask.setValue("");
                }
                if (entryTask.getKey().equals("application.applicationxml") && entryTask.getValue().contains("tempwork")) {
                    entryTask.setValue("");
                }
                props.setProperty(entryTask.getKey(), entryTask.getValue());
            }
            TreeMap<Object, Object> sortedProps = new TreeMap<Object, Object>();
            sortedProps.putAll(props);
            FileUtil.storeProperties(this.file, sortedProps, this.comment);
        }
        catch (Exception e) {
            throw new BuildException("Failed to manage properties file '" + this.file.getAbsolutePath() + "'");
        }
    }
}

