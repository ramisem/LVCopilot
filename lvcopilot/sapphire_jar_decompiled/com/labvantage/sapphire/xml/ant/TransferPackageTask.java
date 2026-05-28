/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.File;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;

public class TransferPackageTask
extends Task
implements Logger {
    ConnectionTask connection;
    File file;
    File zipFile;
    String target;
    String transferpackageid;
    String transferpackageversionid = "1";
    boolean inheritRefs = true;
    boolean logimport = false;
    boolean esigpassword = false;
    boolean esigreason = false;
    boolean checksum = false;

    public void setTransferpackageid(String transferpackageid) {
        this.transferpackageid = transferpackageid;
    }

    public void setTransferpackageversionid(String transferpackageversionid) {
        this.transferpackageversionid = transferpackageversionid;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setInheritRefs(boolean inheritRefs) {
        this.inheritRefs = inheritRefs;
    }

    public void setLogimport(boolean logimport) {
        this.logimport = logimport;
    }

    public void setEsigpassword(boolean esigpassword) {
        this.esigpassword = esigpassword;
    }

    public void setEsigreason(boolean esigreason) {
        this.esigreason = esigreason;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        this.log("Transfering...");
        DBUtil dbu = this.connection.getConnection();
        try {
            TransferPackage transferPackage = new TransferPackage();
            transferPackage.setHeaderAttribute("esigpassword", this.esigpassword ? "Y" : "N");
            transferPackage.setHeaderAttribute("esigreason", this.esigreason ? "Y" : "N");
            transferPackage.setHeaderAttribute("logimport", this.logimport ? "Y" : "N");
            transferPackage.setHeaderAttribute("checksum", this.checksum ? "Y" : "N");
            if (this.transferpackageid.length() > 0 && this.transferpackageversionid.length() > 0) {
                transferPackage.loadTransferPackage(dbu, this.transferpackageid, this.transferpackageversionid);
            } else if (this.file != null) {
                transferPackage.loadFile(this.file);
            } else {
                throw new BuildException("Transfer package source not specified!");
            }
            if (this.inheritRefs) {
                Hashtable antProps = this.getProject().getProperties();
                for (String propertyid : antProps.keySet()) {
                    transferPackage.addProperty(propertyid, (String)antProps.get(propertyid));
                }
            }
            transferPackage.run(dbu, this.target, this, this.zipFile);
        }
        catch (SapphireException e) {
            throw new BuildException("SapphireException: " + e.getMessage());
        }
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

