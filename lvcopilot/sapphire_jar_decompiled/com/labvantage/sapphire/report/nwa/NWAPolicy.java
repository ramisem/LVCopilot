/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.nwa;

import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class NWAPolicy
implements Serializable {
    public static final String DEFAULT_NODE = "Sapphire Custom";
    private PropertyList qawsServer;
    private PropertyListCollection nwaGeneratedFiles;
    private String reportFolder;
    public static final String NWA_QAWSPORTNUM = "QAWSPortNum";
    public static final String NWA_QAWSSERVERNAME = "QAWSServerName";
    public static final String NWA_PAGESERVERPORTNUM = "PageServerPortNum";
    public static final String NWA_PAGESERVERLOCATION = "PageServerLocation";
    public static final String NWA_ATTACHMENTS = "Attachments";
    public static final String NWA_REPORTFOLDER = "ReportFolder";
    public static final String NWA_DOWNLOADFILES = "DownloadFiles";

    public NWAPolicy(PropertyList policy) throws SapphireException {
        if (policy == null) {
            throw new SapphireException("Policy is null.");
        }
        this.qawsServer = policy.getPropertyList("NWAServer");
        if (this.qawsServer == null) {
            throw new SapphireException("QAWS Server configuration missing");
        }
        PropertyList attachmentInfo = policy.getPropertyList(NWA_ATTACHMENTS);
        this.reportFolder = attachmentInfo.getProperty(NWA_REPORTFOLDER);
        this.nwaGeneratedFiles = attachmentInfo.getCollection(NWA_DOWNLOADFILES);
    }

    public PropertyList getQAWSServerProps() {
        return this.qawsServer;
    }

    public String getReportFolder() {
        return this.reportFolder;
    }

    public PropertyListCollection getGeneratedFileNames(String reference, String reportEventId) {
        PropertyListCollection ret = new PropertyListCollection();
        if (this.nwaGeneratedFiles != null) {
            for (int i = 0; i < this.nwaGeneratedFiles.size(); ++i) {
                PropertyList modFileInfo = new PropertyList();
                PropertyList currFile = this.nwaGeneratedFiles.getPropertyList(i);
                String currname = currFile.getProperty("filename", "");
                currname = currname.replaceAll("\\[reference\\]", reference);
                currname = currname.replaceAll("\\[reporteventid\\]", reportEventId);
                modFileInfo.setProperty("filename", currname);
                modFileInfo.setProperty("description", currFile.getProperty("description", ""));
                modFileInfo.setProperty("saveascsv", currFile.getProperty("saveascsv", ""));
                ret.add(modFileInfo);
            }
        }
        return ret;
    }
}

