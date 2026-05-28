/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.LV_EventPlanRO;
import com.labvantage.sapphire.modules.configreport.util.LV_EventPlanUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;

public class LV_EventPlanRenderer
extends LV_EventPlanUtil {
    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/eventplan.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/eventplan.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/event.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/event.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/function.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/function.gif"));
        }
        catch (Exception e) {
            Trace.log("Failed to copy images for event plan rendering");
        }
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Event Plan");
        configReportContent.startSubSection("Event Plan", "");
        configReportContent.appendSubSection(this.renderEventPlanTree((LV_EventPlanRO)sdcRO, true), "Event Plan", this.diffOnly);
        configReportContent.startSubSection("Event Plan Details", "");
        configReportContent.appendSubSection(this.renderEventPlanInfo((LV_EventPlanRO)sdcRO), "Event Plan Details", this.diffOnly);
        configReportContent.startSubSection("Event Plan Properties", "");
        configReportContent.appendSubSection(this.renderEventPlanProperties((LV_EventPlanRO)sdcRO), "Event Plan Properties", this.diffOnly);
        configReportContent.startSubSection("Event Details", "");
        configReportContent.appendSubSection(this.renderEventDetails(this.applicationRoot, this.folder, (LV_EventPlanRO)sdcRO, true), "Event Details", this.diffOnly);
        return configReportContent;
    }

    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/eventplan.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/eventplan.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/event.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/event.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/eventmanager/images/function.gif"), new File(this.folder + "/images/WEB-CORE/modules/eventmanager/images/function.gif"));
        }
        catch (Exception e) {
            Trace.log("Failed to copy images for event plan rendering");
        }
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Event Plan");
        configReportContent.startSubSection("Event Plan", "");
        configReportContent.appendSubSection(this.renderEventPlanTreeDiff((LV_EventPlanRO)sdcRO, (LV_EventPlanRO)refSdcRO, true), "Event Plan", this.diffOnly);
        configReportContent.startSubSection("Event Plan Details", "");
        configReportContent.appendSubSection(this.renderEventPlanInfoDiff((LV_EventPlanRO)sdcRO, (LV_EventPlanRO)refSdcRO), "Event Plan Details", this.diffOnly);
        configReportContent.startSubSection("Event Plan Properties", "");
        configReportContent.appendSubSection(this.renderEventPlanPropertiesDiff((LV_EventPlanRO)sdcRO, (LV_EventPlanRO)refSdcRO), "Event Plan Properties", this.diffOnly);
        configReportContent.startSubSection("Event Details", "");
        configReportContent.appendSubSection(this.renderEventDetailsDiff(this.applicationRoot, this.folder, (LV_EventPlanRO)sdcRO, (LV_EventPlanRO)refSdcRO, true), "Event Details", this.diffOnly);
        return configReportContent;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        if (this.sdcRO != null && this.sdcRO.currentSDIData != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("itemtypeflag", "F");
            DataSet functions = ((LV_EventPlanRO)this.sdcRO).getDataSet("eventplanitem").getFilteredDataSet(filter);
            for (int i = 0; i < functions.getRowCount(); ++i) {
                FileOutputStream processingScriptFile;
                String xmlProcessingScript = functions.getString(i, "processingscript", "");
                String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
                String xmlProcessingScriptFileName = xmlSdiFileName.replace(".xml", "_processingscript_" + functions.getString(i, "eventplanitemid") + ".xml");
                try {
                    processingScriptFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlProcessingScriptFileName);
                }
                catch (FileNotFoundException e) {
                    throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
                }
                try {
                    processingScriptFile.write(xmlProcessingScript.getBytes());
                    processingScriptFile.close();
                    continue;
                }
                catch (IOException e) {
                    throw new SapphireException("Failed to create a section file");
                }
            }
        }
    }
}

