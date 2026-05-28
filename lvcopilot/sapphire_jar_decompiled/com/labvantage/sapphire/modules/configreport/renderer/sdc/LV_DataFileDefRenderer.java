/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.LV_DataFileDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_DataFileDefUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class LV_DataFileDefRenderer
extends LV_DataFileDefUtil {
    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "DataFileDef: ");
        configReportContent.startSubSection("DataFileDef Summary", "The following is the summary of the DataFileDefinition:");
        configReportContent.appendSubSection(this.renderDFDInfo((LV_DataFileDefRO)sdcRO, this.getTranslationProcessor()), "DataFileDef Summary", this.diffOnly);
        if (((LV_DataFileDefRO)sdcRO).getStyle().equals("Composite")) {
            configReportContent.appendSpecialContent(this.renderDataFileDefItemsDiff(sdcRO.currentSDIData, sdcRO.currentSDIData, true));
        }
        configReportContent.startSubSection("Example File", "Content of the example Excel/txt file");
        configReportContent.appendSubSection(this.renderExampleFile((LV_DataFileDefRO)sdcRO, this.getTranslationProcessor()), "Example File", this.diffOnly);
        configReportContent.startSubSection("Field Definitions", "");
        configReportContent.appendSubSection(this.renderFieldDefinitions((LV_DataFileDefRO)sdcRO, this.getTranslationProcessor()), "Field Definitions", this.diffOnly);
        configReportContent.startSubSection("Processing Script", "");
        configReportContent.appendSubSection(this.renderProcessingScript(configReportContent.getApplicationRoot(), configReportContent.getFolder(), (LV_DataFileDefRO)sdcRO, true), "Processing Script", this.diffOnly);
        configReportContent.endSubSection("", "Summary");
        return configReportContent;
    }

    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO srcRO, BaseSDCRO refRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "DataFileDef: ");
        configReportContent.startSubSection("DataFileDef", "The following is the summary of the DataFileDefinition:");
        configReportContent.appendSubSection(this.renderDFDInfoDiff((LV_DataFileDefRO)srcRO, (LV_DataFileDefRO)refRO, this.getTranslationProcessor()), "DataFileDef", this.diffOnly);
        if (((LV_DataFileDefRO)this.sdcRO).getStyle().equals("Composite")) {
            configReportContent.appendSpecialContent(this.renderDataFileDefItemsDiff(this.sdcRO.currentSDIData, refRO.currentSDIData, true));
        }
        configReportContent.startSubSection("Example File", "Content of the example Excel/txt file");
        configReportContent.appendSubSection(this.renderExampleFileDiff((LV_DataFileDefRO)srcRO, (LV_DataFileDefRO)refRO, this.getTranslationProcessor()), "Example File", this.diffOnly);
        configReportContent.startSubSection("Field Definitions", "");
        configReportContent.appendSubSection(this.renderFieldDefinitionsDiff((LV_DataFileDefRO)srcRO, (LV_DataFileDefRO)refRO), "Field Definitions", this.diffOnly);
        configReportContent.startSubSection("Processing Script", "");
        configReportContent.appendSubSection(this.renderProcessingScriptDiff(configReportContent.getApplicationRoot(), configReportContent.getFolder(), (LV_DataFileDefRO)srcRO, (LV_DataFileDefRO)refRO, true), "Processing Script", this.diffOnly);
        return configReportContent;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        if (this.sdcRO != null && this.sdcRO.currentSDIData != null) {
            FileOutputStream datafileExampleFile;
            FileOutputStream datafileObjectsFile;
            FileOutputStream processingScriptFile;
            String xmlProcessingScript = this.sdcRO.getPrimaryValue("processingscript");
            String xmlDataFileObjects = this.sdcRO.getPrimaryValue("datafileobjects");
            String xmlDataFileExample = this.sdcRO.getPrimaryValue("datafileexample");
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            String xmlProcessingScriptFileName = xmlSdiFileName.replace(".xml", "_processingscript.xml");
            String xmlDataFileObjectsFileName = xmlSdiFileName.replace(".xml", "_datafileobjects.xml");
            String xmlDataFileExampleFileName = xmlSdiFileName.replace(".xml", "_datafileexample.xml");
            try {
                processingScriptFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlProcessingScriptFileName);
                datafileObjectsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlDataFileObjectsFileName);
                datafileExampleFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlDataFileExampleFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
            }
            try {
                processingScriptFile.write(xmlProcessingScript.getBytes());
                processingScriptFile.close();
                datafileObjectsFile.write(xmlDataFileObjects.getBytes());
                datafileObjectsFile.close();
                datafileExampleFile.write(xmlDataFileExample.getBytes());
                datafileExampleFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }
}

