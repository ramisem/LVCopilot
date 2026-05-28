/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BuildOptionsTask
extends Task {
    private String earproperty = "";
    private String earname = "";
    private boolean appendversion = false;
    private String majorproperty = "";
    private String minorproperty = "";
    private int majornum = 0;
    private int minornum = 1;
    private int majorinc = 1;
    private int minorinc = 1;
    private String versionupdate = "FIXED";

    public void setEarproperty(String earproperty) {
        this.earproperty = earproperty;
    }

    public void setEarname(String earname) {
        this.earname = earname;
    }

    public void setAppendversion(boolean appendversion) {
        this.appendversion = appendversion;
    }

    public void setMajorproperty(String majorproperty) {
        this.majorproperty = majorproperty;
    }

    public void setMinorproperty(String minorproperty) {
        this.minorproperty = minorproperty;
    }

    public void setMajornum(String majornum) {
        try {
            this.majornum = Integer.parseInt(majornum);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void setMinornum(String minornum) {
        try {
            this.minornum = Integer.parseInt(minornum);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void setMajorinc(String majorinc) {
        try {
            this.majorinc = Integer.parseInt(majorinc);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void setMinorinc(String minorinc) {
        try {
            this.minorinc = Integer.parseInt(minorinc);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void setVersionupdate(String versionupdate) {
        this.versionupdate = versionupdate;
    }

    public void execute() throws BuildException {
        if (this.appendversion && this.earname.length() > 0 && this.earproperty.length() > 0) {
            this.getProject().setNewProperty(this.earproperty, this.earname.substring(0, this.earname.toLowerCase().indexOf(".ear")) + "-" + this.majornum + "." + this.minornum + ".ear");
        } else {
            this.getProject().setNewProperty(this.earproperty, this.earname);
        }
        if (this.versionupdate.equals("MINOR") && this.minorproperty.length() > 0) {
            this.minornum += this.minorinc;
        } else if (this.versionupdate.equals("MAJOR") && this.majorproperty.length() > 0) {
            this.majornum += this.majorinc;
            this.minornum = 0;
        }
        this.getProject().setNewProperty(this.majorproperty, String.valueOf(this.majornum));
        this.getProject().setNewProperty(this.minorproperty, String.valueOf(this.minornum));
    }
}

