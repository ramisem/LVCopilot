/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.SDIData;

public class LV_EventPlanRO
extends BaseSDCRO {
    private DataSet eventplanitems;

    @Override
    public int gotoSection(SDI sdi) {
        int ret = super.gotoSection(sdi);
        if (this.dataSource.equals("XMLREPORT")) {
            this.eventplanitems = super.getDataSet("eventplanitem");
            for (int i = 0; i < this.eventplanitems.getRowCount(); ++i) {
                if (!this.eventplanitems.getString(i, "itemtypeflag").equals("F")) continue;
                String s = this.readProcessingScriptFromFile(this.eventplanitems.getString(i, "eventplanitemid"));
                this.eventplanitems.setString(i, "processingscript", s);
            }
        } else {
            this.eventplanitems = super.getDataSet("eventplanitem");
        }
        return ret;
    }

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        this.eventplanitems = super.getDataSet("eventplanitem");
    }

    @Override
    public DataSet getDataSet(String dsName) {
        if (!dsName.equals("eventplanitem")) {
            return super.getDataSet(dsName);
        }
        return this.eventplanitems;
    }

    private String readProcessingScriptFromFile(String eventplanitemid) {
        String xmlSdiFileName = this.generateSDISectionXMLFileName(this.currentSDI);
        String xmlProcessingScriptFileName = this.refReportFolder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_processingscript_" + eventplanitemid + ".xml");
        File f = new File(xmlProcessingScriptFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("processing script does not exist in the ref report");
        }
        return "";
    }
}

