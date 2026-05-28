/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.misc;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.CoverPageRO;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.xml.PropertyList;

public class CoverPageRenderer
extends BaseRenderer {
    private CoverPageRO coverpageRO;
    private CoverPageRO refCoverPageRO;
    private boolean chapterChanged = false;

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO coverpageRO, HashMap sdisIncluded) {
        super.initialize(sapphireConnection, config, coverpageRO, sdisIncluded);
        this.coverpageRO = (CoverPageRO)coverpageRO;
        String srcImageDir = config.getProperty("applicationroot");
        try {
            ConfigReportContent.copyFile(new File(srcImageDir + "/WEB-CORE/modules/configreport/images/logo.JPG"), new File(this.folder + "/images/WEB-CORE/modules/configreport/images/logo.JPG"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO coverpageRO, BaseRO refCoverPageRO, HashMap sdisIncluded, boolean includeDiffReport) {
        super.initialize(sapphireConnection, config, coverpageRO, refCoverPageRO, sdisIncluded, includeDiffReport);
        this.coverpageRO = (CoverPageRO)coverpageRO;
        this.refCoverPageRO = (CoverPageRO)refCoverPageRO;
        String srcImageDir = config.getProperty("applicationroot");
        try {
            ConfigReportContent.copyFile(new File(srcImageDir + "/WEB-CORE/modules/configreport/images/logo.JPG"), new File(this.folder + "/images/WEB-CORE/modules/configreport/images/logo.JPG"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    public void reportNoFrames(OutputStream reportStream) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Cover Page");
        configReportContent.append(this.renderCoverPage(false).toString());
        configReportContent.pageBreak();
        try {
            reportStream.write(configReportContent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public void reportWithFrames() throws SapphireException {
        FileOutputStream sectionFile;
        String sectionFileName = ConfigReportContent.generateSectionFileName("Cover", "Page");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Cover Page");
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Cover", "Page"));
        configReportContent.append(this.renderCoverPage().toString());
        configReportContent.endFile();
        this.createSubSectionInfo("Cover", "Page", configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public ConfigReportContent renderCoverPage() throws SapphireException {
        return this.renderCoverPage("");
    }

    public ConfigReportContent renderCoverPage(boolean frames) throws SapphireException {
        if (!frames) {
            return this.renderCoverPage("");
        }
        return this.renderCoverPage("../");
    }

    private ConfigReportContent renderCoverPage(String imagepath) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "Cover Page");
        PropertyList pl = this.coverpageRO.getReportInfo();
        if (this.frames) {
            content.append("<IMG width=\"250\" height=\"150\" align=\"center\" SRC=\"" + imagepath + "../images/WEB-CORE/modules/configreport/images/logo.JPG\" align=\"center\" />");
        } else {
            content.append("<IMG width=\"250\" height=\"150\" align=\"center\" SRC=\"" + imagepath + "images/WEB-CORE/modules/configreport/images/logo.JPG\" align=\"center\" />");
        }
        content.append("<TABLE width=\"100%\" height=\"70%\" offset>");
        content.append("<TR height=\"50%\" valign=\"Top\"");
        content.append("<TD>");
        content.append("<P style=\"font-size: 14pt;\">Configuration Report</P>\n");
        content.append("<P stype=\"font-size: 12pt;\"><B>Created by:</B> " + pl.getProperty("createdby"));
        content.append("</TD>");
        content.append("</TR>");
        if (this.includeDiffReport) {
            content.append("<TR height=\"50%\">");
        } else {
            content.append("<TR height=\"50%\">");
        }
        content.append("<TD>");
        content.append("<TABLE align=\"right\" vertical-align: bottom; width=\"40%\">");
        content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>Created on: </B> " + pl.getProperty("date"));
        content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>Version/Database: </B> " + pl.getProperty("build") + "/" + pl.getProperty("database"));
        content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>Platform: </B>" + pl.getProperty("serverinfo"));
        if (this.includeDiffReport) {
            content.append("<P style=\"font-size: 10pt; text-align: left; vertical-align: bottom;\"><B>Comparison Report:</B> ");
            PropertyList refPl = this.refCoverPageRO.getReportInfo();
            content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>&nbsp;&nbsp;&nbsp;&nbsp;Created on: </B> " + refPl.getProperty("date"));
            content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>&nbsp;&nbsp;&nbsp;&nbsp;Version/Database: </B> " + refPl.getProperty("build") + "/" + refPl.getProperty("database"));
            content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>&nbsp;&nbsp;&nbsp;&nbsp;Platform: </B> " + refPl.getProperty("serverinfo"));
            content.append("<P style=\"text-align: left; vertical-align: bottom;\"><B>&nbsp;&nbsp;&nbsp;&nbsp;Folder: </B> " + refPl.getProperty("reportfolder"));
            content.append("</TABLE>");
        } else {
            content.append("</TABLE>");
        }
        content.append("</TD>");
        content.append("</TR>");
        content.append("</TABLE>");
        return content;
    }

    @Override
    public ArrayList getSectionList() {
        return new ArrayList();
    }

    public void createXMLReport() throws SapphireException {
        if (this.coverpageRO != null) {
            try {
                FileOutputStream tocFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + "coverpage.xml");
                PropertyList cp = this.coverpageRO.getReportInfo();
                tocFile.write(cp.toXMLString().getBytes());
                tocFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to write coverpage info file", e);
            }
        }
    }
}

