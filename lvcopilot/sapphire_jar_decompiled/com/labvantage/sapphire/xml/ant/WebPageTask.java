/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.ant.SDITask;

public class WebPageTask
extends SDITask
implements TransferConstants {
    private String exportNodes = "true";
    private String forcePageOverrides = "true";
    private String nodeExists = "replace";
    private String nodePropertyExists = "replace";

    public void setExportNodes(String exportNodes) {
        this.exportNodes = exportNodes;
    }

    public void setNodeExists(String nodeExists) {
        this.nodeExists = nodeExists;
    }

    public void setNodePropertyExists(String nodePropertyExists) {
        this.nodePropertyExists = nodePropertyExists;
    }

    public void setForcePageOverrides(String forcePageOverrides) {
        this.forcePageOverrides = forcePageOverrides;
    }

    public SDITransfer getWebPageTransfer() {
        SDITransfer sdi = this.getSDITransfer();
        sdi.setSdcid("WebPage");
        sdi.setDetailForceUpdate(this.forcePageOverrides);
        sdi.setTransferOption("includenodes", this.exportNodes.equals("true") ? "Y" : "N");
        sdi.setTransferOption("nodeexists", this.nodeExists);
        sdi.setTransferOption("nodepropertyexists", this.nodePropertyExists);
        return sdi;
    }
}

