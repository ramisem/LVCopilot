/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.admin.install.PackagerFile;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.PatchXML;
import com.labvantage.sapphire.xml.ant.InstallerFileTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class InstallerPackageTask
extends Task
implements Logger {
    File file;
    ArrayList installerFiles = new ArrayList();
    String installerid;
    String installerversion = "1.0";
    String description;
    String type = "C";

    public void setFile(File file) {
        this.file = file;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInstallerid(String installerid) {
        this.installerid = installerid;
    }

    public void setInstallerversion(String installerversion) {
        this.installerversion = installerversion;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void execute() throws BuildException {
        if (this.file == null) {
            throw new BuildException("Installer file not defined!");
        }
        if (this.installerFiles.size() == 0) {
            throw new BuildException("No installer files not defined!");
        }
        try {
            PackagerFile installerFile = new PackagerFile(this.file);
            PatchXML patchXML = new PatchXML(new File(this.file.getParentFile(), this.type.equals("C") ? "component.xml" : "patch.xml"));
            PatchXML.Patch patchXMLPatch = patchXML.getPatchInstance(this.type, this.installerid != null && this.installerid.length() > 0 ? this.installerid : this.file.getName(), this.installerversion, this.file.getAbsolutePath(), this.description != null && this.description.length() > 0 ? this.description : this.file.getName() + " installer", DateTimeUtil.getNowCalendar());
            patchXML.addPatch(patchXMLPatch);
            for (int i = 0; i < this.installerFiles.size(); ++i) {
                InstallerFileTask installerFileTask = (InstallerFileTask)this.installerFiles.get(i);
                installerFile.addFile(installerFileTask.getTarget(), installerFileTask.getPath(), new File(installerFileTask.getPath(), installerFileTask.getFile()));
                patchXMLPatch.addPatchFile(patchXML.getPatchFileInstance(installerFileTask.getTarget(), installerFileTask.getJar(), installerFileTask.getFile(), installerFileTask.getType(), installerFileTask.getPath().getAbsolutePath(), installerFileTask.getDescription(), installerFileTask.getAction(), "N", installerFileTask.getDbms(), installerFileTask.getPlatform()));
            }
            patchXML.save();
            installerFile.addFile(patchXML.getFile().getParentFile(), patchXML.getFile());
            installerFile.save();
        }
        catch (Exception e) {
            throw new BuildException("Failed to save patch XML. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public void addConfiguredInstallerFile(InstallerFileTask installerFileTask) {
        this.installerFiles.add(installerFileTask);
    }
}

