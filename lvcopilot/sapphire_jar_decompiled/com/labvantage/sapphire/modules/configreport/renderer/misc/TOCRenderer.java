/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.misc;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TOCRenderer
extends BaseRenderer {
    private ArrayList sdcList;
    private boolean chapterChanged = false;
    private boolean hideSubsections = false;
    private DataSet hiddenChapters;
    private String chapterRendering = "SDCList";
    private PropertyListCollection categoryListForChaptersColl = null;

    public TOCRenderer(ArrayList sdcList) {
        this.sdcList = sdcList;
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, HashMap sdisIncluded, boolean includediffreport) {
        super.initialize(sapphireConnection, config, null, null, sdisIncluded, includediffreport);
    }

    public void setOptions(PropertyList tocRendererOptions) {
        if (tocRendererOptions != null) {
            this.chapterRendering = tocRendererOptions.getProperty("chapterrendering", "SDCList");
            this.hideSubsections = tocRendererOptions.getProperty("hidesubsections", "N").equals("Y");
            if (this.chapterRendering.equals("CategoryList")) {
                this.categoryListForChaptersColl = tocRendererOptions.getCollection("categorylist");
            }
        }
    }

    public String getChapterDesc(String chapter) {
        if (chapter.equals("Menu System")) {
            return "This chapter includes the details of various Menus and Menu Items";
        }
        if (chapter.equals("System Configuration")) {
            return "This Chapter includes the System Configuration Details";
        }
        PropertyList p = this.getSDCProcessor().getProperties(chapter);
        if (p != null) {
            return p.getProperty("description");
        }
        return chapter;
    }

    public String getCategorizedDesc(String category) {
        if (this.categoryListForChaptersColl != null) {
            for (int i = 0; i < this.categoryListForChaptersColl.size(); ++i) {
                PropertyList curr = this.categoryListForChaptersColl.getPropertyList(i);
                if (!curr.getProperty("category").equals(category)) continue;
                return curr.getProperty("categorydesc");
            }
        }
        return "";
    }

    public boolean hideSubsections() {
        return this.hideSubsections;
    }

    public boolean hasSectionChanged() {
        return false;
    }

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    public void reportNoFrames(OutputStream reportStream, ArrayList chapterList) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "TOC");
        if (this.sdcList == null || this.sdcList.size() == 0) {
            throw new SapphireException("No sdcs specified to generate TOC");
        }
        configReportContent.pageBreak();
        configReportContent.append("<H1>Table of Contents</H1><P>");
        configReportContent.append("<BR>");
        configReportContent.append("<TABLE>");
        for (int i = 0; i < chapterList.size(); ++i) {
            String chapterName = chapterList.get(i).toString();
            String chapter = "<TR><td>" + (i + 1) + "</td><td><A HREF=\"#CHAPTER" + chapterName + "\">" + chapterName + "</A></td> </TR>";
            configReportContent.append(chapter);
        }
        configReportContent.append("</TABLE>");
        configReportContent.append("<BR>");
        configReportContent.append("<BR>");
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(reportStream, "UTF8"));
            out.write(configReportContent.toString());
            ((Writer)out).flush();
            ((Writer)out).close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to append TOC " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    private String getTOCPreamble() {
        String prefix = "<HTML>\n<HEAD>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE></TITLE>\n<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\"></HEAD>\n";
        prefix = prefix + "<script>\n";
        prefix = prefix + "function togglehighlight( num, tabsize ) {\n\t\tfor( var i = 1; i < num; i++ ) {\n\t\t\tvar\tbcellid = 'row' + i;\n\t\t\tvar bcell = document.getElementById( bcellid );\n\n\t\t\tif ( bcell != undefined ) {\n\t\t\t\tbcell.style.background = 'White';\n\t\t\t}\n\t\t}\n\n\t//alert('clearing from' + (num + 1 ) );\n\t\tfor( var j = num + 1 ; j <= tabsize; j++ ) {\n\t\t\tvar\tacellid = 'row' + j;\n\t\t\t//alert('clearing ell' + acellid );\n\t\t\tvar acell = document.getElementById( acellid );\n\t\t\tif ( acell != undefined ) {\n\t\t\t\t\tacell.style.background = 'White';\n\t\t\t}\n\t\t}\n\t\tcellid = 'row' + num;\n\t\tvar cell = document.getElementById( cellid );\n\t\tcell.style.background = 'lightGrey';\n\n\n}\n</script>";
        return prefix;
    }

    public void reportWithFrames(ArrayList chapterList, ArrayList rendererList) throws SapphireException {
        StringBuffer toc = new StringBuffer();
        String prefix = this.getTOCPreamble();
        toc.append(prefix);
        toc.append("<H1>Table of Contents</H1><P>");
        toc.append("<TABLE>");
        int displayno = 0;
        this.hiddenChapters = new DataSet();
        boolean hasEntries = false;
        for (int i = 0; i < chapterList.size(); ++i) {
            String chapterName = chapterList.get(i).toString();
            String chapterFileName = ConfigReportContent.generateTOCFileName(chapterName);
            BaseRenderer r = (BaseRenderer)rendererList.get(i);
            boolean hide = false;
            if (this.includeDiffReport) {
                if (r.isNewChapter()) {
                    chapterName = ConfigReportContent.getNewString(chapterName);
                } else if (r.isDeletedChapter()) {
                    chapterName = ConfigReportContent.getDeletedString(chapterName);
                }
                if (r.hasChapterChanged()) {
                    chapterName = ConfigReportContent.getModifiedString(chapterName);
                } else {
                    hide = this.diffOnly;
                    chapterName = "<font color=black>" + chapterName + "</black>";
                    int row = this.hiddenChapters.addRow();
                    this.hiddenChapters.setString(row, "chapter", chapterName);
                }
            } else {
                chapterName = "<font color=black>" + chapterName + "</black>";
            }
            if (hide) continue;
            String chapter = "<TR><td>" + ++displayno + "</td><td id=\"row" + (i + 1) + "\" onclick=\"togglehighlight( " + (i + 1) + "," + chapterList.size() + " )\"><A HREF=\"" + chapterFileName + "\"  target=\"Section\">" + chapterName + "</A></td> </TR>";
            toc.append(chapter);
            hasEntries = true;
        }
        String chapter = "<TR><TD></TD><TD><A HREF=\"Cover_Page.html\"  target=\"ChapterContent\">Top</A></td> </TR>";
        if (!hasEntries) {
            toc.append("<TR><TD>None</TD></TR>");
        } else {
            toc.append(chapter);
        }
        toc.append("</TABLE>");
        toc.append("<BR>");
        toc.append("<BR>");
        toc.append("</html>");
        try {
            FileOutputStream tocFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + "TOC.html");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)tocFile, "UTF8"));
            out.write(toc.toString());
            ((Writer)out).flush();
            ((Writer)out).close();
            tocFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to write toc file", e);
        }
    }

    public void generateChapterTOC(ArrayList chapterList, ArrayList rendererList) throws SapphireException {
        if (this.chapterRendering.equals("CategoryList")) {
            this.generateCategorizedTOC(chapterList, rendererList);
        } else {
            int displayedChapterNo = 0;
            for (int chapterNo = 0; chapterNo < chapterList.size(); ++chapterNo) {
                String currentChapter = chapterList.get(chapterNo).toString();
                if (this.hiddenChapters != null && this.hiddenChapters.getRowCount() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("chapter", currentChapter);
                    DataSet match = this.hiddenChapters.getFilteredDataSet(filter);
                    if (match.getRowCount() > 0) continue;
                }
                ++displayedChapterNo;
                BaseRenderer renderer = (BaseRenderer)rendererList.get(chapterNo);
                DataSet sectionChangeInfo = renderer.getSectionChangeInfo();
                ArrayList sectionList = renderer.getSectionList();
                ArrayList sectionTitleList = renderer.getSectionTitleList();
                boolean isNewChapter = renderer.isNewChapter();
                boolean hasEntries = false;
                int displayedSectionNo = 0;
                StringBuffer toc = new StringBuffer();
                String chapterCoverPageFileName = ConfigReportContent.generateSectionFileName(currentChapter, "Cover Page");
                this.createChapterCoverPage(currentChapter, this.getChapterDesc(currentChapter));
                toc = new StringBuffer();
                String prefix = this.getTOCPreamble();
                toc.append(prefix);
                toc.append("<H1>Sections</H1><P>");
                if (chapterNo == 0) {
                    toc.append("<script> window.onload = function ( e ) {  if ( parent._firstloading == true) { parent.ChapterContent.location='Cover_Page.html'; parent._firstloading=false; } else {   parent.ChapterContent.location='" + chapterCoverPageFileName + "';} }</script> ");
                } else {
                    toc.append("<script>window.onload = function ( e ) {  parent.ChapterContent.location='" + chapterCoverPageFileName + "';}</script> ");
                }
                toc.append("<TABLE>");
                for (int i = 0; i < sectionList.size(); ++i) {
                    String currSection = (String)sectionList.get(i);
                    String sectionAnchor = ConfigReportContent.generateSectionAnchor(currSection);
                    String sectionTitle = sectionTitleList.get(i).toString();
                    String sectionFileName = ConfigReportContent.generateSectionFileName(currentChapter, currSection);
                    boolean hide = false;
                    if (this.includeDiffReport) {
                        String status = this.fetchSectionStatus(sectionTitle, sectionChangeInfo);
                        if ("Modified".equals(status)) {
                            sectionTitle = ConfigReportContent.getModifiedString(sectionTitle);
                        } else if ("Deleted".equals(status)) {
                            sectionTitle = ConfigReportContent.getDeletedString(sectionTitle);
                        } else if ("New".equals(status) || isNewChapter) {
                            sectionTitle = ConfigReportContent.getNewString(sectionTitle);
                        } else {
                            hide = this.diffOnly;
                            sectionTitle = "<font color=black>" + sectionTitle + "</font>";
                        }
                    } else {
                        sectionTitle = "<font color=black>" + sectionTitle + "</font>";
                    }
                    if (hide) continue;
                    String sectionNo = displayedChapterNo + "." + ++displayedSectionNo;
                    String section = "<TR><td valign=\"top\" >" + sectionNo + "</td><td id=\"row" + (i + 1) + "\" onclick=\"togglehighlight( " + (i + 1) + "," + sectionList.size() + " )\"><A HREF=\"" + sectionFileName + "#" + sectionAnchor + "\" target=\"ChapterContent\">" + sectionTitle + "</A></td> </TR>";
                    toc.append(section);
                    hasEntries = true;
                }
                if (chapterList.size() == 1) {
                    toc.append("<TR><TD></TD><TD><A HREF=\"Cover_Page.html\"  target=\"ChapterContent\">Top</A></td> </TR>");
                }
                toc.append("</TABLE>");
                toc.append("<BR>");
                toc.append("<BR>");
                toc.append("</html>");
                try {
                    FileOutputStream fos = new FileOutputStream(this.folder + File.separator + "html" + File.separator + ConfigReportContent.generateTOCFileName(currentChapter));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)fos, "UTF8"));
                    out.write(toc.toString());
                    ((Writer)out).flush();
                    ((Writer)out).close();
                    fos.close();
                    continue;
                }
                catch (IOException e) {
                    throw new SapphireException("Failed to create TOC for chapters " + e.getMessage());
                }
            }
        }
    }

    public void generateCategorizedTOC(ArrayList sdcChapterList, ArrayList rendererList) throws SapphireException {
        if (this.categoryListForChaptersColl == null || this.categoryListForChaptersColl.size() == 0) {
            throw new SapphireException("Categories not specified in the TOC Renderer options. Check policy definition");
        }
        DataSet ds = TOCRenderer.getCategorizedChapterList(this.categoryListForChaptersColl);
        HashMap categorySDIMap = new HashMap();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String currentCategory = ds.getString(i, "category");
            ArrayList<SDI> currentCategorySDIList = new ArrayList<SDI>();
            for (int chapter = 0; chapter < sdcChapterList.size(); ++chapter) {
                String currentSDC = sdcChapterList.get(chapter).toString();
                try {
                    BaseSDCRenderer renderer = (BaseSDCRenderer)rendererList.get(chapter);
                    ArrayList sdisIncluded = renderer.getSDIsIncluded();
                    String sql2 = "SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?";
                    DataSet categoryitems = this.getQueryProcessor().getPreparedSqlDataSet(sql2, new Object[]{currentCategory, currentSDC});
                    if (categoryitems == null || categoryitems.getRowCount() <= 0) continue;
                    for (int sdi = 0; sdi < sdisIncluded.size(); ++sdi) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        SDI currentSDI = (SDI)sdisIncluded.get(sdi);
                        filter.put("keyid1", currentSDI.getKeyid1());
                        if (categoryitems.findRow(filter) <= -1) continue;
                        currentCategorySDIList.add(currentSDI);
                    }
                    continue;
                }
                catch (ClassCastException renderer) {
                    // empty catch block
                }
            }
            if (currentCategorySDIList.size() > 0) {
                categorySDIMap.put(currentCategory, currentCategorySDIList);
                ds.setString(i, "hasentries", "Y");
                continue;
            }
            ds.setString(i, "hasentries", "N");
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("hasentries", "Y");
        ds = ds.getFilteredDataSet(filter);
        StringBuffer toc = new StringBuffer();
        String prefix = this.getTOCPreamble();
        toc.append(prefix);
        toc.append("<H1>Table of Contents</H1><P>");
        toc.append("<TABLE>");
        int chaptercount = 0;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String categoryChapterName = ds.getString(i, "category");
            String categoryChapterFileName = ConfigReportContent.generateTOCFileName(categoryChapterName);
            BaseRenderer r = (BaseRenderer)rendererList.get(0);
            boolean hide = false;
            if (this.includeDiffReport) {
                ArrayList sectionList = (ArrayList)categorySDIMap.get(categoryChapterName);
                for (int j = 0; j < sectionList.size(); ++j) {
                    SDI currSDI = (SDI)sectionList.get(j);
                    String sdiSectionTitle = ConfigReportContent.generateSDISectionTitle(currSDI);
                    String status = this.fetchSectionStatus(sdiSectionTitle, r.getSectionChangeInfo());
                    if (!"None".equals(status)) {
                        categoryChapterName = ConfigReportContent.getModifiedString(categoryChapterName);
                        break;
                    }
                    hide = this.diffOnly;
                    categoryChapterName = "<font color=black>" + categoryChapterName + "</black>";
                }
            } else {
                categoryChapterName = "<font color=black>" + categoryChapterName + "</black>";
            }
            if (hide) continue;
            String chapter = "<TR><td>" + (chaptercount + 1) + "</td><td id=\"row" + (chaptercount + 1) + "\" onclick=\"togglehighlight( " + (chaptercount + 1) + "," + ds.getRowCount() + " )\"><A HREF=\"" + categoryChapterFileName + "\"  target=\"Section\">" + categoryChapterName + "</A></td> </TR>";
            ++chaptercount;
            toc.append(chapter);
        }
        String chapterHtml = "<TR><TD></TD><TD><A HREF=\"Cover_Page.html\"  target=\"ChapterContent\">Top</A></td> </TR>";
        toc.append(chapterHtml);
        toc.append("</TABLE>");
        toc.append("<BR>");
        toc.append("<BR>");
        toc.append("</html>");
        try {
            FileOutputStream tocFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + "TOC.html");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)tocFile, "UTF8"));
            out.write(toc.toString());
            ((Writer)out).flush();
            ((Writer)out).close();
            tocFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to write toc file", e);
        }
        int secchaptercount = 0;
        for (int chapterNo = 0; chapterNo < ds.getRowCount(); ++chapterNo) {
            String currentCategory = ds.getString(chapterNo, "category");
            String currentCategoryDesc = this.getCategorizedDesc(currentCategory);
            ArrayList sectionList = (ArrayList)categorySDIMap.get(currentCategory);
            BaseRenderer r = (BaseRenderer)rendererList.get(0);
            String chapterCoverPageFileName = ConfigReportContent.generateSectionFileName(currentCategory, "Cover Page");
            this.createChapterCoverPage(currentCategory, currentCategoryDesc);
            toc = new StringBuffer();
            prefix = this.getTOCPreamble();
            toc.append(prefix);
            toc.append("<H1>Sections</H1><P>");
            toc.append("<TABLE>");
            if (chapterNo == 0) {
                toc.append("<script> window.onload = function ( e ) {  if ( parent._firstloading == true) { parent.ChapterContent.location='Cover_Page.html'; parent._firstloading=false; } else {   parent.ChapterContent.location='" + chapterCoverPageFileName + "';} }</script> ");
            } else {
                toc.append("<script>window.onload = function ( e ) {  parent.ChapterContent.location='" + chapterCoverPageFileName + "';}</script> ");
            }
            int sectioncount = 0;
            boolean showchapter = false;
            for (int j = 0; j < sectionList.size(); ++j) {
                SDI currSDI = (SDI)sectionList.get(j);
                String sdiSectionTitle = ConfigReportContent.generateSDISectionTitle(currSDI);
                String sectionAnchor = ConfigReportContent.generateSectionAnchor(sdiSectionTitle);
                String sectionFileName = ConfigReportContent.generateSectionFileName(currSDI.getSdcid(), sdiSectionTitle);
                boolean hidesection = false;
                if (this.includeDiffReport) {
                    String status = this.fetchSectionStatus(sdiSectionTitle, r.getSectionChangeInfo());
                    if ("Modified".equals(status)) {
                        sdiSectionTitle = ConfigReportContent.getModifiedString(sdiSectionTitle);
                        showchapter = true;
                    } else if ("Deleted".equals(status)) {
                        sdiSectionTitle = ConfigReportContent.getDeletedString(sdiSectionTitle);
                        showchapter = true;
                    } else if ("New".equals(status) || r.isNewChapter()) {
                        sdiSectionTitle = ConfigReportContent.getNewString(sdiSectionTitle);
                        showchapter = true;
                    } else {
                        hidesection = this.diffOnly;
                        showchapter &= this.diffOnly;
                        sdiSectionTitle = "<font color=black>" + sdiSectionTitle + "</font>";
                    }
                } else {
                    sdiSectionTitle = "<font color=black>" + sdiSectionTitle + "</font>";
                }
                if (hidesection) continue;
                String sectionNo = secchaptercount + 1 + "." + (sectioncount + 1);
                String section = "<TR><td valign=\"top\" >" + sectionNo + "</td><td id=\"row" + (sectioncount + 1) + "\" onclick=\"togglehighlight( " + (sectioncount + 1) + "," + sectionList.size() + " )\"><A HREF=\"" + sectionFileName + "#" + sectionAnchor + "\" target=\"ChapterContent\">" + sdiSectionTitle + "</A></td> </TR>";
                toc.append(section);
                ++sectioncount;
            }
            if (showchapter) {
                ++secchaptercount;
            }
            toc.append("</TABLE>");
            toc.append("<BR>");
            toc.append("<BR>");
            toc.append("</html>");
            if (sectionList.size() <= 0) continue;
            try {
                FileOutputStream fos = new FileOutputStream(this.folder + File.separator + "html" + File.separator + ConfigReportContent.generateTOCFileName(currentCategory));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)fos, "UTF8"));
                out.write(toc.toString());
                ((Writer)out).flush();
                ((Writer)out).close();
                fos.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create TOC for chapters " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
    }

    private ConfigReportContent getFramesReportCategorized(String firstCategory) {
        ConfigReportContent str = new ConfigReportContent(this.config, "Start Report");
        str.append("<HTML>\n");
        str.append("<HEAD>\n");
        str.append("<TITLE>Configuration Report</TITLE>\n");
        str.append("<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\">\n");
        str.append("</HEAD>\n");
        str.append("<script>var _firstloading=true;</script><FRAMESET cols=\"20%, 80%\">");
        str.append("  <FRAMESET rows=\"100, 200\">\n");
        str.append("      <FRAME name=\"Chapter\" src=\"html" + File.separator + "CategorizedTOC.html\">\n");
        String sub = "      <FRAME name=\"Section\" src=\"html" + File.separator + ConfigReportContent.generateTOCFileName(firstCategory) + "\">\n";
        str.append(sub);
        str.append("  </FRAMESET>\n");
        str.append("<FRAMESET rows=\"40, 160\">\n");
        sub = "<FRAME name=\"SubSection\" style=\"frame-border=0\" src=\"html" + File.separator + ConfigReportContent.generateSubSectionFileName("Cover", "Page") + "\">\n";
        str.append(sub);
        str.append("<FRAME name=\"ChapterContent\" style=\"frame-border=0\" src=\"html" + File.separator + "Cover_Page.html\"> ");
        str.append("</FRAMESET>\n");
        str.append("  </FRAMESET>\n");
        str.append("</HTML>");
        return str;
    }

    private FileOutputStream createCategorizedReportFile() throws SapphireException {
        FileOutputStream fos;
        String filePath = this.folder + File.separator + "categorized.html";
        try {
            fos = new FileOutputStream(filePath);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create the categorized report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return fos;
    }

    public void generateDiffChapterTOC(ArrayList chapterList, ArrayList refChapterList, ArrayList rendererList) throws SapphireException {
        for (int chapterNo = 0; chapterNo < chapterList.size(); ++chapterNo) {
            String currentChapter = chapterList.get(chapterNo).toString();
            StringBuffer toc = new StringBuffer();
            String prefix = this.getTOCPreamble();
            toc.append(prefix);
            toc.append("<H1>Sections</H1><P>");
            toc.append("<BR>");
            toc.append("<TABLE>");
            BaseRenderer renderer = (BaseRenderer)rendererList.get(chapterNo);
            ArrayList sectionList = renderer.getSectionList();
            ArrayList sectionTitleList = renderer.getSectionTitleList();
            for (int i = 0; i < sectionList.size(); ++i) {
                String currSection = (String)sectionList.get(i);
                String sectionAnchor = ConfigReportContent.generateSectionAnchor(currSection);
                String sectionNo = chapterNo + 1 + "." + (i + 1);
                String sectionTitle = sectionTitleList.get(i).toString();
                String sectionFileName = ConfigReportContent.generateSectionFileName(currentChapter, currSection);
                String section = "<TR><td valign=\"top\" >" + sectionNo + "</td><td id=\"row" + (i + 1) + "\" onclick=\"togglehighlight( " + (i + 1) + "," + sectionList.size() + " )\"><A HREF=\"" + sectionFileName + "#" + sectionAnchor + "\" target=\"ChapterContent\">" + sectionTitle + "</A></td> </TR>";
                toc.append(section);
            }
            toc.append("</TABLE>");
            toc.append("<BR>");
            toc.append("<BR>");
            toc.append("</html>");
            try {
                FileOutputStream fos = new FileOutputStream(this.folder + File.separator + "html" + File.separator + ConfigReportContent.generateTOCFileName(currentChapter));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)fos, "UTF8"));
                out.write(toc.toString());
                ((Writer)out).flush();
                ((Writer)out).close();
                fos.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create TOC for chapters " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
    }

    private ConfigReportContent renderChapterCoverPage(String chaptername, String desc) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "Cover Page");
        if (this.frames) {
            content.append("<IMG width=\"250\" height=\"150\" align=\"center\" SRC=\"../images/WEB-CORE/modules/configreport/images/logo.JPG\" align=\"center\" />");
        } else {
            content.append("<IMG width=\"250\" height=\"150\" align=\"center\" SRC=\"images/WEB-CORE/modules/configreport/images/logo.JPG\" align=\"center\" />");
        }
        content.append("<TABLE width=\"100%\">");
        content.append("<TR height=\"40%\" valign=\"Top\"");
        content.append("<TD>");
        content.append("<P style=\"text-align: left; vertical-align: bottom;\"><H2>" + chaptername + "</H2><P>");
        content.append("<P style=\"text-align: left; vertical-align: bottom;\"><H4>" + desc + "</H4> ");
        content.append("</TABLE>");
        content.append("</TD>");
        content.append("</TR>");
        content.append("</TABLE>");
        return content;
    }

    private void createChapterCoverPage(String chapterName, String chapterDescription) throws SapphireException {
        FileOutputStream sectionFile;
        String fileName = ConfigReportContent.generateSectionFileName(chapterName, "Cover Page");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + fileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + fileName);
        }
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Cover Page");
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Cover", "Page"));
        configReportContent.append(this.renderChapterCoverPage(chapterName, chapterDescription).toString());
        configReportContent.endFile();
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a file");
        }
    }

    @Override
    public ArrayList getSectionList() {
        return new ArrayList();
    }

    @Override
    public ArrayList getSectionTitleList() {
        return new ArrayList();
    }

    public void createXMLReport(ArrayList chapterList, ArrayList roList, ArrayList rendererList) throws SapphireException {
        DataSet tocChapters = new DataSet();
        for (int i = 0; i < chapterList.size(); ++i) {
            String chapterName = chapterList.get(i).toString();
            tocChapters.addRow();
            tocChapters.setString(i, "chapter", chapterName);
        }
        try {
            FileOutputStream tocFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + "toc.xml");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)tocFile, "UTF8"));
            out.write(tocChapters.toXML());
            ((Writer)out).flush();
            ((Writer)out).close();
            tocFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to write toc file", e);
        }
        this.generateChapterXMLTOC(chapterList, roList, rendererList);
    }

    public void generateChapterXMLTOC(ArrayList chapterList, ArrayList roList, ArrayList rendererList) throws SapphireException {
        for (int chapterNo = 0; chapterNo < chapterList.size(); ++chapterNo) {
            String currentChapter = chapterList.get(chapterNo).toString();
            BaseRenderer renderer = (BaseRenderer)rendererList.get(chapterNo);
            ArrayList sectionList = renderer.getSectionList();
            ArrayList sectionTitleList = renderer.getSectionTitleList();
            DataSet toc = new DataSet();
            for (int i = 0; i < sectionList.size(); ++i) {
                String currSection = (String)sectionList.get(i);
                String sectionTitle = sectionTitleList.get(i).toString();
                toc.addRow();
                toc.setString(i, "section", currSection);
                toc.setString(i, "title", sectionTitle);
            }
            try {
                FileOutputStream fos = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + ConfigReportContent.generateTOCXMLFileName(currentChapter));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)fos, "UTF8"));
                out.write(toc.toXML());
                ((Writer)out).flush();
                ((Writer)out).close();
                fos.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create TOC for chapters " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
    }

    private String fetchSectionStatus(String sectionName, DataSet sectionChangeInfo) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("section", sectionName);
        DataSet filtered = sectionChangeInfo.getFilteredDataSet(filter);
        if (filtered.size() == 0) {
            return "Deleted";
        }
        return filtered.getString(0, "status");
    }

    public static DataSet getCategorizedChapterList(PropertyListCollection categoryListForChaptersColl) {
        DataSet ds = new DataSet();
        for (int i = 0; i < categoryListForChaptersColl.size(); ++i) {
            ds.addRow();
            ds.setString(i, "category", categoryListForChaptersColl.getPropertyList(i).getProperty("category"));
            ds.setString(i, "categorydesc", categoryListForChaptersColl.getPropertyList(i).getProperty("categorydesc"));
        }
        ds.sort("category");
        return ds;
    }
}

