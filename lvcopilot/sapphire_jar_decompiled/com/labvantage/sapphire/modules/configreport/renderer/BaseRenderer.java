/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseRenderer
extends BaseCustom {
    protected SapphireConnection sapphireConnection;
    protected DBAccess database;
    protected boolean frames;
    protected BaseRO sourceRO;
    protected BaseRO refRO;
    protected String folder;
    protected String applicationRoot;
    protected boolean includeDiffReport;
    protected boolean diffOnly = false;
    protected DataSet sectionChangeInfo;
    protected boolean chapterChanged = false;
    protected String connection = "";
    public static final String OPTIONID = "optionid";
    public static final String OPTIONTITLE = "title";
    public HashMap sdisIncluded;
    protected PropertyList config;
    protected PropertyListCollection ignorePrimaryDiffs;
    protected PropertyListCollection ignoreDetailsDiffs;
    public static final String COLUMN_OPTIONNO = "optionno";
    public static final String COLUMN_DISPLAYTITLE = "displaytitle";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_VALUES = "values";
    public static final String COLUMN_SELECTEDVALUE = "selectedvalue";
    public static final String COLUMNVALUE_TYPE_TEXT = "T";
    public static final String COLUMNVALUE_TYPE_DROPDOWN = "D";
    public static final String COLUMNVALUE_TYPE_FOLDER = "F";

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO srcro, BaseRO refRO, HashMap sdisIncluded, boolean includeDiffReport) {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.connection = sapphireConnection.getConnectionId();
        this.sapphireConnection = sapphireConnection;
        DBUtil dbUtil = new DBUtil(sapphireConnection.getConnectionId());
        dbUtil.setConnection(sapphireConnection);
        this.database = dbUtil;
        this.sourceRO = srcro;
        this.refRO = refRO;
        if (config != null) {
            this.frames = "Y".equalsIgnoreCase(config.getProperty("frames", "Y"));
            this.folder = config.getProperty("folder");
            this.applicationRoot = config.getProperty("applicationroot");
            this.diffOnly = includeDiffReport && "Y".equals(config.getProperty("diffonlyreport", "N"));
            this.config = config;
        } else {
            this.frames = false;
            this.folder = "";
            this.applicationRoot = "";
            this.diffOnly = false;
            this.config = new PropertyList();
        }
        this.sdisIncluded = sdisIncluded;
        this.includeDiffReport = includeDiffReport;
        this.sectionChangeInfo = new DataSet();
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded) {
        this.initialize(sapphireConnection, config, ro, null, sdisIncluded, false);
    }

    public abstract ArrayList getSectionList() throws SapphireException;

    public DataSet getSectionChangeInfo() {
        return this.sectionChangeInfo;
    }

    public abstract boolean hasChapterChanged();

    public ArrayList getSectionTitleList() throws SapphireException {
        return this.getSectionList();
    }

    public void updateSectionChangeInfo(String chapter, String sectionName, ConfigReportContent content) {
        int currRow = this.sectionChangeInfo.addRow();
        this.sectionChangeInfo.setString(currRow, "Section", sectionName);
        if (!this.includeDiffReport) {
            this.sectionChangeInfo.setString(currRow, "Status", "None");
            return;
        }
        if (this.refRO == null) {
            this.sectionChangeInfo.setString(currRow, "Status", "New");
            this.chapterChanged = true;
            return;
        }
        if (this.sourceRO == null) {
            this.sectionChangeInfo.setString(currRow, "Status", "Deleted");
            this.chapterChanged = true;
            return;
        }
        if (this.hasSectionChanged(content)) {
            this.sectionChangeInfo.setString(currRow, "Status", "Modified");
            this.chapterChanged = true;
            return;
        }
        if (this.sourceRO instanceof BaseSDCRO && ((BaseSDCRO)this.sourceRO).currentSDI == null) {
            this.sectionChangeInfo.setString(currRow, "Status", "Deleted");
            this.chapterChanged = true;
            return;
        }
        if (this.refRO instanceof BaseSDCRO && ((BaseSDCRO)this.refRO).currentSDI == null) {
            this.sectionChangeInfo.setString(currRow, "Status", "New");
            this.chapterChanged = true;
            return;
        }
        this.sectionChangeInfo.setString(currRow, "Status", "None");
    }

    public boolean hasSectionChanged(ConfigReportContent configReportContent) {
        return configReportContent.getFoundDiff();
    }

    public boolean isNewChapter() {
        return this.includeDiffReport && this.refRO == null;
    }

    public boolean isDeletedChapter() {
        return this.includeDiffReport && this.sourceRO == null;
    }

    public void createSubSectionInfo(String chapter, String sectionTitle, DataSet alldiffInfo) throws SapphireException {
        FileOutputStream outputStream;
        if (alldiffInfo == null) {
            alldiffInfo = new DataSet();
        }
        String diffReportFileName = ConfigReportContent.generateSubSectionFileName(chapter, sectionTitle);
        try {
            outputStream = new FileOutputStream(this.folder + File.separator + "html" + File.separator + diffReportFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + diffReportFileName);
        }
        try {
            StringBuffer s = new StringBuffer();
            ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
            String prefix = "<HTML>\n<HEAD>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE></TITLE>\n<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("../stylesheets/configreport.css", connectionInfo.isRtl(), connectionInfo.getUseFullIncludes()) + "\" type=\"text/css\"></HEAD>\n";
            s.append(prefix);
            DataSet diffInfo = new DataSet();
            for (int i = 0; i < alldiffInfo.getRowCount(); ++i) {
                if (alldiffInfo.getValue(i, "subsection", "").trim().length() <= 0) continue;
                diffInfo.copyRow(alldiffInfo, i, 1);
            }
            if (diffInfo.size() > 0) {
                s.append("<H1 align='center'>Summary</H1><P>");
                int displayColCount = diffInfo.size() / 5;
                s.append("<TABLE>");
                s.append("<TR valign=top>");
                for (int col = 0; col <= displayColCount; ++col) {
                    s.append("<TD>");
                    s.append("<UL>");
                    for (int row = col * 5; row < (col + 1) * 5 && row < diffInfo.getRowCount(); ++row) {
                        String status = diffInfo.getString(row, "status");
                        String href = ConfigReportContent.generateSectionFileName(chapter, sectionTitle) + diffInfo.getString(row, "subsectionurl");
                        String subsectionName = diffInfo.getString(row, "subsection", "").trim();
                        if (subsectionName.length() <= 0) continue;
                        if ("None".equals(status)) {
                            subsectionName = "<font color=black>" + subsectionName + "</black>";
                            s.append("<LI><A href=\"" + href + "\" target=\"ChapterContent\">" + subsectionName + "\n");
                            continue;
                        }
                        s.append("<LI><A href=\"" + href + "\" target=\"ChapterContent\">" + ConfigReportContent.getModifiedString(subsectionName) + "</A>\n");
                    }
                    s.append("</UL>");
                    s.append("</TD>");
                }
                s.append("</TR>");
                s.append("</TABLE>");
                s.append("</html>");
            }
            outputStream.write(s.toString().getBytes());
            outputStream.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a diff file");
        }
    }

    public PropertyListCollection getIgnoreDetailsDiffCols(String linkid) {
        PropertyListCollection ret = new PropertyListCollection();
        if (this.ignoreDetailsDiffs != null) {
            for (int i = 0; i < this.ignoreDetailsDiffs.size(); ++i) {
                PropertyList curr = this.ignoreDetailsDiffs.getPropertyList(i);
                String currdet = curr.getProperty("linkid");
                if (currdet == null || currdet.length() <= 0 || !currdet.equals(linkid)) continue;
                ret.add(curr);
            }
        }
        return ret;
    }
}

