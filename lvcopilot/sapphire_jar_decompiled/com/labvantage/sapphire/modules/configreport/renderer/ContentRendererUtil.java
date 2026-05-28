/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ContentRendererUtil {
    private String applicationRoot;
    private String folder;
    private boolean frames = true;
    private PropertyList config;
    private boolean foundDiff = false;
    private static final String COMMANDTYPE = "command";
    private static final String COMMANDTYPE_ACTION = "action";
    private static final String COMMANDTYPE_RETURNPROPERTY = "returnproperty";
    private static final String COMMANDTYPE_STARTACTIONBLOCK = "startactionblock";
    private static final String COMMANDTYPE_ENDACTIONBLOCK = "endactionblock";
    private static final String COMMANDTYPE_BLOCKPROPERTY = "blockproperty";
    private static final String COMMANDID = "commandid";
    private static final String LABEL = "commandlabel";
    private static final String LEVEL = "level";
    private static final String TESTCONDITION = "testcondition";

    public void initialize(PropertyList config) {
        this.config = config;
        this.applicationRoot = config.getProperty("applicationroot", "");
        this.folder = config.getProperty("folder", "");
        this.frames = "Y".equals(config.getProperty("frames", "Y"));
        try {
            if (this.applicationRoot.length() > 0) {
                this.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
                this.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Delete.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Delete.gif"));
                this.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Add.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Add.gif"));
                this.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Hand.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Hand.gif"));
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    public void setFoundDiff(boolean diff) {
        this.foundDiff = diff;
    }

    public boolean getFoundDiff() {
        return this.foundDiff;
    }

    public void startReport(StringBuffer buffer, ArrayList sdcList, int chapterCount, boolean includeGenericLayout) throws SapphireException {
        try {
            this.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/configreport/stylesheets/configreport.css"), new File(this.folder + "/stylesheets/configreport.css"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy file", e);
        }
        if (!this.frames) {
            this.startFile(buffer);
        } else {
            buffer.append(this.getFramesReport(chapterCount, sdcList, includeGenericLayout));
        }
    }

    public void endReport(StringBuffer buffer) throws SapphireException {
        buffer.append("</BODY></HTML>");
    }

    public StringBuffer startChapter(String chapterNo, String sdcId, String preamble) {
        StringBuffer buffer = new StringBuffer();
        this.pageBreak(buffer);
        String str = "";
        str = str + "<H1 id=\"CHAPTER" + sdcId + "\">Chapter " + chapterNo + " " + sdcId + "</H1>\n";
        str = str + "<P>\n";
        str = str + preamble;
        str = str + "</P>\n";
        buffer.append(str);
        return buffer;
    }

    public void pageBreak(StringBuffer buffer) {
        buffer.append("<DIV style=\"page-break-before: always;\" /> \n ");
    }

    public void startFile(StringBuffer buffer) {
        String str = "";
        str = "<HTML>\n<HEAD>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE></TITLE>\n<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\">\n</HEAD>\n<BODY>";
        buffer.append(str);
    }

    public void startSDISection(StringBuffer buffer, String sectionNo, SDI currentSDI, String desc) {
        String sectionTitle = this.generateSDISectionTitle(currentSDI);
        this.startSDISection(buffer, sectionNo, currentSDI, sectionTitle, desc);
    }

    public void startSDISectionDiff(StringBuffer buffer, String sectionNo, SDI currentSDI, String desc, String refDesc) {
        String sectionTitle = this.generateSDISectionTitle(currentSDI);
        this.startSDISectionDiff(buffer, sectionNo, currentSDI, sectionTitle, desc, refDesc);
    }

    public void startSDISection(StringBuffer buffer, String sectionNo, SDI currentSDI, String sectionTitle, String desc) {
        String anchor = this.generateSDISectionAnchor(currentSDI);
        String str = "<H2 id=\"" + anchor + "\">" + sectionTitle + "</H2>\n";
        if (desc != null && desc.length() > 0) {
            str = str + "<P>\n";
            str = str + "<B>Description:</B>\n" + desc;
            str = str + "</P>\n";
        }
        buffer.append(str);
    }

    public void startSDISectionDiff(StringBuffer buffer, String sectionNo, SDI currentSDI, String sectionTitle, String desc, String refDesc) {
        String anchor = this.generateSDISectionAnchor(currentSDI);
        String str = "<H2 id=\"" + anchor + "\">" + sectionTitle + "</H2>\n";
        if (desc != null && desc.length() > 0) {
            str = str + "<P>\n";
            str = str + "<B>Description:</B>\n" + this.getDiffString(desc, refDesc);
            str = str + "</P>\n";
        }
        buffer.append(str);
    }

    public void startSection(StringBuffer buffer, String sectionNo, String sectionIdentifier) {
        String anchor = this.generateSectionAnchor(sectionIdentifier);
        String sectionTitle = this.generateSectionTitle(sectionIdentifier);
        String str = "<H2 id=\"" + anchor + "\">" + sectionTitle + "</H2>\n";
        str = str + "<P>\n";
        str = str + "</P>\n";
        buffer.append(str);
    }

    public StringBuffer endChapter(String sdcId) {
        String str = "";
        return new StringBuffer(str);
    }

    public String generateSDISectionAnchor(SDI currentSDI) {
        String sdcid = currentSDI.getSdcid();
        String keyid1 = currentSDI.getKeyid1();
        String keyid2 = currentSDI.getKeyid2();
        String keyid3 = currentSDI.getKeyid3();
        String anchor = sdcid.toUpperCase() + "_" + keyid1.toUpperCase();
        if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
            anchor = anchor + "_" + keyid2.toUpperCase();
        }
        if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
            anchor = anchor + "_" + keyid3.toUpperCase();
        }
        return anchor;
    }

    public String generateSectionAnchor(String layout) {
        return layout.toUpperCase().replaceAll(" ", "_");
    }

    public String generateSDISectionTitle(SDI currentSDI) {
        String sdcid = currentSDI.getSdcid();
        String keyid1 = currentSDI.getKeyid1();
        String keyid2 = currentSDI.getKeyid2();
        String keyid3 = currentSDI.getKeyid3();
        String sectionTitle = keyid1;
        if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
            sectionTitle = sectionTitle + ", " + keyid2;
        }
        if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
            sectionTitle = sectionTitle + ", " + keyid3;
        }
        return sectionTitle;
    }

    public String generateSectionTitle(String layout) {
        return layout.substring(0, 1).toUpperCase() + layout.substring(1);
    }

    public static String removeIllegalChars(String input) {
        StringBuffer outputString = new StringBuffer(input.length());
        boolean count = true;
        for (int i = 0; i < input.length(); ++i) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(input.charAt(i));
            if (input.charAt(i) == '/' || input.charAt(i) == ',' || input.charAt(i) == '\\' || input.charAt(i) == '%' || input.charAt(i) == ':' || input.charAt(i) == '*' || input.charAt(i) == '?' || input.charAt(i) == '<' || input.charAt(i) == '>' || input.charAt(i) == '|' || !block.equals(Character.UnicodeBlock.BASIC_LATIN)) continue;
            outputString.append(input.charAt(i));
        }
        return outputString.toString();
    }

    public String generateSDISectionFileName(SDI currentSDI) {
        String sdcId = currentSDI.getSdcid();
        String keyid1 = currentSDI.getKeyid1();
        String keyid2 = currentSDI.getKeyid2();
        String keyid3 = currentSDI.getKeyid3();
        String sectionFileName = sdcId + "_" + keyid1;
        if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
            sectionFileName = sectionFileName + "_" + keyid2;
        }
        if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
            sectionFileName = sectionFileName + "_" + keyid3;
        }
        sectionFileName = sectionFileName.replaceAll(" ", "_");
        sectionFileName = ContentRendererUtil.removeIllegalChars(sectionFileName);
        return sectionFileName + ".html";
    }

    public String generatePagePropsXMLFileName(SDI currentSDI) {
        String sdcId = currentSDI.getSdcid();
        String keyid1 = currentSDI.getKeyid1();
        String keyid2 = currentSDI.getKeyid2();
        String keyid3 = currentSDI.getKeyid3();
        String sectionFileName = sdcId + "_" + keyid1;
        if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
            sectionFileName = sectionFileName + "_" + keyid2;
        }
        if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
            sectionFileName = sectionFileName + "_" + keyid3;
        }
        sectionFileName = sectionFileName + "_Props";
        sectionFileName = sectionFileName.replaceAll(" ", "_");
        sectionFileName = ContentRendererUtil.removeIllegalChars(sectionFileName);
        return sectionFileName + ".xml";
    }

    public String generateSDISectionXMLFileName(SDI currentSDI) {
        String sdcId = currentSDI.getSdcid();
        String keyid1 = currentSDI.getKeyid1().trim();
        String keyid2 = currentSDI.getKeyid2().trim();
        String keyid3 = currentSDI.getKeyid3().trim();
        String sectionFileName = sdcId + "_" + keyid1;
        if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
            sectionFileName = sectionFileName + "_" + keyid2;
        }
        if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
            sectionFileName = sectionFileName + "_" + keyid3;
        }
        sectionFileName = sectionFileName.replaceAll(" ", "_");
        sectionFileName = ContentRendererUtil.removeIllegalChars(sectionFileName);
        return sectionFileName + ".xml";
    }

    public String generateSectionFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        sectionFileName = ContentRendererUtil.removeIllegalChars(sectionFileName);
        return sectionFileName + ".html";
    }

    public String generateSectionXMLFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        sectionFileName = ContentRendererUtil.removeIllegalChars(sectionFileName);
        return sectionFileName + ".xml";
    }

    public void endSection(StringBuffer buffer) {
        String str = "<P>";
        buffer.append(str);
    }

    public void endFile(StringBuffer buffer) {
        String str = "</BODY>\n";
        str = str + "</HTML>\n";
        buffer.append(str);
    }

    public void startSubSection(StringBuffer buffer, String sectionNo, String title, String desc) {
        String modTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        String subsectionId = this.convertToID(title);
        String str = "<H3 id=\"" + subsectionId + "\">" + modTitle + "</H3>\n";
        str = str + "<P>\n";
        str = str + desc;
        str = str + "</P>\n";
        buffer.append(str);
    }

    public void endSubSection(StringBuffer buffer, String title, String desc) {
        String str = "";
        buffer.append(str);
    }

    public void startTableTop(StringBuffer buffer) {
        buffer.append(this.getStartTableTop());
    }

    public void startTableInner(StringBuffer buffer) {
        buffer.append(this.getStartTableInner());
    }

    public void endTable(StringBuffer buffer) {
        buffer.append(this.getEndTable());
    }

    public String getStartTableTop() {
        return "<TABLE class=\"view\" >\n";
    }

    public String getStartTableInner() {
        return "<TABLE width=\"100%\"class=\"view\" >\n";
    }

    public String getEndTable() {
        return "</TABLE>\n";
    }

    public void startRow(StringBuffer buffer) {
        buffer.append("<TR VALIGN=TOP >\n");
    }

    public void endRow(StringBuffer buffer) {
        buffer.append("</TR>\n");
    }

    public StringBuffer addRowItem(StringBuffer buffer, String columnName, String columnVal, boolean icon) {
        String colTitle = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
        buffer.append("<TD class=\"viewlhs\" >\n");
        buffer.append(colTitle);
        buffer.append("</TD>\n");
        if (!icon) {
            buffer.append("<TD class=\"viewrhs\" >\n");
        } else {
            buffer.append("<TD align=CENTER>\n");
        }
        buffer.append(columnVal);
        buffer.append("</TD>\n");
        return buffer;
    }

    public StringBuffer addRowItem(StringBuffer buffer, String columnName, String columnVal) {
        return this.addRowItem(buffer, columnName, columnVal, "viewlhs", "viewrhs");
    }

    public StringBuffer addRowItem(StringBuffer buffer, String columnName, String columnVal, String lhsClass, String rhsClass) {
        String colTitle = columnName;
        if (columnName.length() > 1) {
            colTitle = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
        }
        if (columnVal == null) {
            columnVal = "";
        }
        buffer.append("<TD class=\"" + lhsClass + "\" >\n");
        if (lhsClass.equals("diffreportnewlhs")) {
            colTitle = this.getNewString(colTitle);
        } else if (lhsClass.equals("diffreportdeletedlhs")) {
            colTitle = this.getDeletedString(colTitle);
        } else if (lhsClass.equals("diffreportmodifiedlhs")) {
            colTitle = this.getModifiedString(colTitle);
        }
        buffer.append(colTitle);
        buffer.append("</TD>\n");
        buffer.append("<TD class=\"" + rhsClass + "\" >\n");
        if (columnVal.startsWith("<dataset")) {
            try {
                DataSet temp = new DataSet(columnVal);
                columnVal = temp.toHTML();
            }
            catch (Exception e) {
                Trace.logError("Failed to add row item", e);
            }
        } else if (columnVal.startsWith("<?xml") || columnVal.contains("<propertytree>")) {
            columnVal = HttpUtil.htmlEncode(columnVal);
        }
        buffer.append(columnVal);
        buffer.append("</TD>\n");
        return buffer;
    }

    public StringBuffer addNewRowItem(StringBuffer buffer, String columnName, String columnVal) {
        this.foundDiff = true;
        return this.addRowItem(buffer, columnName, columnVal, "diffreportnewlhs", "diffreportnewrhs");
    }

    public StringBuffer addDeletedRowItem(StringBuffer buffer, String columnName, String columnVal) {
        this.foundDiff = true;
        return this.addRowItem(buffer, columnName, columnVal, "diffreportdeletedlhs", "diffreportdeletedrhs");
    }

    public StringBuffer renderPropertyDefinitionList(PropertyDefinitionList def) {
        StringBuffer buffer = new StringBuffer();
        this.startTableInner(buffer);
        this.addRowItem(buffer, "propertydefid", def.getPropertyDefId());
        this.addRowItem(buffer, "color", def.getColor());
        this.addRowItem(buffer, "labelsingular", def.getLabelSingular());
        this.addRowItem(buffer, "labelplural", def.getLabelPlural());
        this.addRowItem(buffer, "direction", def.getDirecttion());
        this.addRowItem(buffer, "titlepropertyid", def.getTitlePropertyId());
        this.addRowItem(buffer, "tablestyle", def.getTableStyle());
        this.addRowItem(buffer, "showhide", def.isShowhide() ? "Y" : "N");
        this.addRowItem(buffer, "allowRoles", def.isAllowRoles() ? "Y" : "N");
        this.addRowItem(buffer, "deprecated", def.isDeprecated() ? "Y" : "N");
        this.addRowItem(buffer, "advanced", def.isAdvanced() ? "Y" : "N");
        this.addRowItem(buffer, "addMethod", def.getAddMethod());
        for (int i = 0; i < def.size(); ++i) {
            PropertyDefinition propertyDefinition = (PropertyDefinition)def.get(i);
            this.addRowItem(buffer, "PropertyDefintion" + i, this.renderPropertyDefinition(propertyDefinition).toString());
        }
        this.endTable(buffer);
        return buffer;
    }

    public StringBuffer renderPropertyDefinition(PropertyDefinition def) {
        StringBuffer buffer = new StringBuffer();
        this.startTableInner(buffer);
        this.addRowItem(buffer, "Id", def.getId());
        this.addRowItem(buffer, "Type", def.getType());
        this.addRowItem(buffer, "Title", def.getTitle());
        this.addRowItem(buffer, "Editor", def.getEditor());
        this.addRowItem(buffer, "Advanced", def.isAdvanced() ? "Y" : "N");
        this.addRowItem(buffer, "Deprecated", def.isDeprecated() ? "Y" : "N");
        this.addRowItem(buffer, "Expression", def.isExpression() ? "Y" : "N");
        this.addRowItem(buffer, "Translate", def.getTranslate());
        this.addRowItem(buffer, "Help", def.getHelp());
        this.addRowItem(buffer, "sdcid", def.getSdcid());
        this.addRowItem(buffer, "values", def.getValues());
        this.addRowItem(buffer, "extendedwhere", def.getExtendedWhere());
        this.addRowItem(buffer, "propertydeflist", this.renderPropertyDefinitionList(def.getPropertyDefinitionList()).toString());
        this.endTable(buffer);
        return buffer;
    }

    public StringBuffer addRowItem(StringBuffer buffer, String columnName, String columnVal, int colspan) {
        String colTitle = "";
        if (columnName.length() > 0) {
            colTitle = columnName.substring(0, 1).toUpperCase();
            if (columnName.length() > 1) {
                colTitle = colTitle + columnName.substring(1);
            }
        }
        buffer.append("<TD class=\"viewlhs\" >\n");
        buffer.append(colTitle);
        buffer.append("</TD>\n");
        buffer.append("<TD class=\"viewrhs\" colspan = \" " + colspan + "\">\n");
        buffer.append(columnVal);
        buffer.append("</TD>\n");
        return buffer;
    }

    private void startListTableTop(StringBuffer buffer) {
        buffer.append("<TABLE  class=\"viewlist\" >\n");
    }

    private void startListTableInner(StringBuffer buffer) {
        buffer.append("<TABLE width=\"100%\" class=\"viewlist\" >\n");
    }

    public void startBulletList(StringBuffer buffer) {
        buffer.append("<UL>\n");
    }

    public void addBullet(StringBuffer buffer, String str) {
        buffer.append("<LI >\n");
        buffer.append(str);
        buffer.append("</LI>\n");
    }

    public void addDiffBullet(StringBuffer buffer, String str, String refStr) {
        if (!str.equals(refStr)) {
            this.foundDiff = true;
        }
        buffer.append("<LI >\n");
        buffer.append("<diffpoint/><font class=\"diffreportnewitem\">");
        buffer.append(str);
        buffer.append("</font>");
        buffer.append("<font class=\"diffreportdeleteditem\">");
        buffer.append(refStr);
        buffer.append("</font>");
        buffer.append("</LI>\n");
    }

    public void endBulletList(StringBuffer buffer) {
        buffer.append("</UL>\n");
    }

    public void startHeader(StringBuffer buffer) {
        buffer.append("<THEAD>\n");
    }

    public void addColumnHeader(StringBuffer buffer, String colHeader, boolean rotateHeader) {
        String modTitle = colHeader;
        if (colHeader.length() > 1) {
            modTitle = colHeader.substring(0, 1).toUpperCase() + colHeader.substring(1);
        }
        if (rotateHeader) {
            modTitle = "<div STYLE=\"writing-mode:vertical-rl;\" >" + modTitle + "</div>\n";
        }
        String str = "<th class=\"viewlisthead\">" + modTitle + "</th>\n";
        buffer.append(str);
    }

    public void addColumnHeader(StringBuffer buffer, String colHeader) {
        String modTitle = colHeader;
        if (colHeader.length() > 1) {
            modTitle = colHeader.substring(0, 1).toUpperCase() + colHeader.substring(1);
        }
        String str = "<th class=\"viewlisthead\">" + modTitle + "</th>\n";
        buffer.append(str);
    }

    public void endHeader(StringBuffer buffer) {
        buffer.append("</THEAD>\n");
    }

    private boolean isImage(String val) {
        return (val = val.toUpperCase()).toUpperCase().indexOf("IMG") > 0 && val.indexOf("SRC") > 0;
    }

    private boolean isPropertyList(String val) {
        return val != null && val.toUpperCase().indexOf("<PROPERTYLIST") > -1;
    }

    private boolean isDataSet(String val) {
        return val != null && val.toUpperCase().indexOf("<DATASET") > -1;
    }

    private void addListItems(StringBuffer buffer, DataSet listItems, boolean rotateHeader, int keycols) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader(buffer);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            if (i < keycols) {
                this.addColumnHeader(buffer, colList[i]);
                continue;
            }
            this.addColumnHeader(buffer, colList[i], rotateHeader);
        }
        this.endHeader(buffer);
        for (i = 0; i < rows; ++i) {
            HashMap currrow = (HashMap)listItems.get(i);
            if (currrow == null || currrow.size() <= 0) continue;
            buffer.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                if (this.isImage(columnValue)) {
                    buffer.append("<td class=\"viewlistcol\" align=\"center\" >\n");
                } else {
                    buffer.append("<td class=\"viewlistcol\">\n");
                }
                buffer.append(columnValue);
                buffer.append("</td>\n");
            }
            buffer.append("</TR>\n");
        }
    }

    private void addListItems(StringBuffer buffer, DataSet listItems) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader(buffer);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            this.addColumnHeader(buffer, colList[i]);
        }
        this.endHeader(buffer);
        for (i = 0; i < rows; ++i) {
            buffer.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                if (this.isImage(columnValue)) {
                    buffer.append("<td class=\"viewlistcol\" align=\"center\" >\n");
                } else {
                    buffer.append("<td class=\"viewlistcol\">\n");
                }
                buffer.append(DOMUtil.convertChars(columnValue));
                buffer.append("</td>\n");
            }
            buffer.append("</TR>\n");
        }
    }

    private void addListItemsHighlighted(StringBuffer buffer, DataSet listItems, int startrow, int endrow, int startcol, int endcol) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader(buffer);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            this.addColumnHeader(buffer, colList[i]);
        }
        this.endHeader(buffer);
        for (i = 0; i < rows; ++i) {
            buffer.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                if (this.isImage(columnValue)) {
                    buffer.append("<td class=\"viewlistcol\" align=\"center\" >\n");
                } else if (i >= startrow && i <= endrow && j >= startcol && j <= endcol) {
                    buffer.append("<td class=\"viewlistcolhighlight\">\n");
                } else {
                    buffer.append("<td class=\"viewlistcol\">\n");
                }
                buffer.append(columnValue);
                buffer.append("</td>\n");
            }
            buffer.append("</TR>\n");
        }
    }

    private void endListTable(StringBuffer buffer) {
        buffer.append("</TABLE>\n");
    }

    public void renderCategories(StringBuffer buffer, DataSet categories) {
        if (categories == null || categories.getRowCount() == 0) {
            buffer.append("No categories specified");
            return;
        }
        this.addCategories(buffer, categories);
    }

    public void renderCategoriesDiff(StringBuffer buffer, DataSet categories, DataSet refCategories) {
        this.addCategoriesDiff(buffer, categories, refCategories);
    }

    public void renderRoleMatrix(StringBuffer buffer, DataSet roleMatrix, int keycols) {
        if (roleMatrix == null || roleMatrix.getRowCount() == 0) {
            buffer.append("No roles found.");
            return;
        }
        this.addRoleMatrix(buffer, roleMatrix, keycols);
    }

    public void renderDiffRoleMatrix(StringBuffer buffer, DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols) {
        this.startListTableTop(buffer);
        roleMatrix = this.getMatrixDiffInfo(roleMatrix, refRoleMatrix, keycols);
        this.renderRoleMatrix(buffer, roleMatrix, keycols.length);
        this.endListTable(buffer);
    }

    public void renderDiffRoleMatrix(StringBuffer buffer, DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols, boolean sortby) {
        this.startListTableTop(buffer);
        roleMatrix = this.getMatrixDiffInfo(roleMatrix, refRoleMatrix, keycols, sortby);
        this.renderRoleMatrix(buffer, roleMatrix, keycols.length);
        this.endListTable(buffer);
    }

    public boolean isFKIncluded(String sdcId, String keyid1, String keyid2, String keyid3, HashMap sdisIncluded) {
        ArrayList sdiList = (ArrayList)sdisIncluded.get(sdcId);
        if (keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        if (sdiList == null) {
            return false;
        }
        for (int i = 0; i < sdiList.size(); ++i) {
            SDI currSDI = (SDI)sdiList.get(i);
            if (!currSDI.getKeyid1().equals(keyid1) || !currSDI.getKeyid2().equals(keyid2) || !currSDI.getKeyid3().equals(keyid3)) continue;
            return true;
        }
        return false;
    }

    public boolean isFKIncluded(String sdcId, String keyid1, HashMap sdisIncluded) {
        ArrayList sdiList = (ArrayList)sdisIncluded.get(sdcId);
        if (sdiList == null) {
            return false;
        }
        for (int i = 0; i < sdiList.size(); ++i) {
            SDI currSDI = (SDI)sdiList.get(i);
            if (!currSDI.getKeyid1().equals(keyid1)) continue;
            return true;
        }
        return false;
    }

    private String convertToID(String title) {
        title = title.replaceAll(" ", "_");
        title = title.replaceAll(",", "_");
        title = title.toUpperCase();
        return title;
    }

    public void addCategories(StringBuffer buffer, DataSet categories) {
        for (int i = 0; i < categories.getRowCount(); ++i) {
            if (i != 0) {
                buffer.append(", ");
            } else {
                buffer.append("<P>");
            }
            buffer.append(categories.getString(i, "Category ID"));
        }
    }

    public void addCategoriesDiff(StringBuffer buffer, DataSet categories, DataSet refCategories) {
        DataSet find;
        HashMap<String, String> filter;
        String currCategory;
        int i;
        buffer.append("<P>");
        for (i = 0; i < categories.getRowCount(); ++i) {
            currCategory = categories.getString(i, "Category ID");
            buffer.append(" ");
            filter = new HashMap<String, String>();
            filter.put("Category ID", currCategory);
            find = refCategories.getFilteredDataSet(filter);
            if (find.getRowCount() > 0) {
                buffer.append(currCategory);
                continue;
            }
            buffer.append(this.getNewString(currCategory));
        }
        for (i = 0; i < refCategories.getRowCount(); ++i) {
            currCategory = refCategories.getString(i, "Category ID");
            filter = new HashMap();
            filter.put("Category ID", currCategory);
            find = categories.getFilteredDataSet(filter);
            if (find.getRowCount() != 0) continue;
            buffer.append(" ");
            buffer.append(this.getDeletedString(currCategory));
        }
    }

    public void addRoleMatrix(StringBuffer buffer, DataSet roleMatrix, int keycols) {
        int i;
        this.startListTableTop(buffer);
        int rows = roleMatrix.getRowCount();
        int cols = roleMatrix.getColumnCount();
        String[] colList = roleMatrix.getColumns();
        this.startHeader(buffer);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            String modTitle = colList[i];
            modTitle = i < keycols ? colList[i].substring(0, 1).toUpperCase() + colList[i].substring(1) : "<div STYLE=\"writing-mode:vertical-rl;\" >" + modTitle + "</div>\n";
            buffer.append("<th class=\"viewlisthead\">" + modTitle + "</th>\n");
        }
        this.endHeader(buffer);
        for (i = 0; i < rows; ++i) {
            HashMap currrow = (HashMap)roleMatrix.get(i);
            if (currrow == null || currrow.size() <= 0) continue;
            buffer.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = roleMatrix.getValue(i, colList[j]);
                if (this.isImage(columnValue)) {
                    buffer.append("<td class=\"viewlistcol\" align=\"center\" >\n");
                } else {
                    buffer.append("<td class=\"viewlistcol\">\n");
                }
                buffer.append(columnValue);
                buffer.append("</td>\n");
            }
            buffer.append("</TR>\n");
        }
        this.endListTable(buffer);
    }

    public void renderListTableTop(StringBuffer buffer, DataSet ds) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            this.startListTableTop(buffer);
            this.addListItems(buffer, ds);
            this.endListTable(buffer);
        } else {
            buffer.append("No entries.");
        }
    }

    public void startTable(StringBuffer buffer) {
        this.startTableTop(buffer);
    }

    public void renderHighlightedListTableTop(StringBuffer buffer, DataSet ds, int startrow, int endrow, int startcol, int endcol) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            this.startListTableTop(buffer);
            this.addListItemsHighlighted(buffer, ds, startrow, endrow, startcol, endcol);
            this.endListTable(buffer);
        } else {
            buffer.append("No entries.");
        }
    }

    private boolean checkIfEmpty(DataSet ds) {
        String[] columns = ds.getColumns();
        for (int row = 0; row < ds.getRowCount(); ++row) {
            for (int column = 0; column < columns.length; ++column) {
                if (ds.getValue(row, columns[column]) == null || ds.getValue(row, columns[column]).length() <= 0) continue;
                return false;
            }
        }
        return true;
    }

    public void renderListTableInner(StringBuffer buffer, DataSet ds) {
        if (ds != null && ds.getRowCount() > 0) {
            if (!this.checkIfEmpty(ds)) {
                this.startListTableInner(buffer);
                this.addListItems(buffer, ds);
                this.endListTable(buffer);
            } else {
                buffer.append("No entries");
            }
        } else {
            buffer.append("No entries.");
        }
    }

    public void renderProcessingScript(StringBuffer buffer, String processingScript) throws SapphireException {
        try {
            this.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/png/decision.png"), new File(this.folder + "/images/WEB-CORE/images/png/decision.png"));
        }
        catch (Exception e) {
            Trace.log("Failed to copy file /WEB-CORE/images/png/line.png");
        }
        if (processingScript != null && processingScript.length() > 0) {
            ActionBlock actionBlock = new ActionBlock(processingScript);
            this.renderActionBlock(buffer, actionBlock);
        }
    }

    private DataSet addActionBlockItems(DataSet dataset, ActionBlock actionBlock, int level) {
        String abTest = actionBlock.getTest();
        if (abTest != null && abTest.length() > 0) {
            int r = dataset.addRow();
            dataset.setString(r, COMMANDTYPE, COMMANDTYPE_STARTACTIONBLOCK);
            dataset.setString(r, TESTCONDITION, abTest);
            dataset.setNumber(r, LEVEL, level);
        }
        int commands = actionBlock.getCommandCount();
        for (int i = 0; i < commands; ++i) {
            Object o = actionBlock.getCommand(i);
            if (o instanceof ActionBlock.Action) {
                ActionBlock.Action action = (ActionBlock.Action)o;
                String name = action.name;
                String actionid = action.actionid;
                int r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_ACTION);
                dataset.setString(r, COMMANDID, name);
                dataset.setString(r, LABEL, actionid);
                dataset.setNumber(r, LEVEL, level);
                PropertyList properties = action.properties;
                dataset.setString(r, "Properties", this.renderPropertyList(properties, false).toString());
                if (i != commands - 1 || level <= 0) continue;
                r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_ENDACTIONBLOCK);
                dataset.setNumber(r, LEVEL, level);
                continue;
            }
            if (o instanceof ActionBlock) {
                ActionBlock ab = (ActionBlock)o;
                this.addActionBlockItems(dataset, ab, level + 1);
                continue;
            }
            if (o instanceof ActionBlock.BlockProperty) {
                ActionBlock.BlockProperty bp = (ActionBlock.BlockProperty)o;
                int r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_BLOCKPROPERTY);
                dataset.setString(r, COMMANDID, bp.propertyid);
                dataset.setString(r, LABEL, bp.value);
                dataset.setNumber(r, LEVEL, level);
                continue;
            }
            if (!(o instanceof ActionBlock.ReturnProperty)) continue;
            ActionBlock.ReturnProperty rp = (ActionBlock.ReturnProperty)o;
            int r = dataset.addRow();
            dataset.setString(r, COMMANDTYPE, COMMANDTYPE_RETURNPROPERTY);
            dataset.setString(r, COMMANDID, rp.propertyid);
            dataset.setString(r, LABEL, rp.value);
            dataset.setNumber(r, LEVEL, level);
        }
        return dataset;
    }

    public void renderActionBlock(StringBuffer buffer, ActionBlock ab) {
        DataSet actionBlock = this.addActionBlockItems(new DataSet(), ab, 0);
        buffer.append("<table class=\"gridtable\">\n");
        DataSet copy = actionBlock.copy();
        copy.sort(LEVEL);
        int maxlevel = copy.getBigDecimal(copy.getRowCount() - 1, LEVEL).intValue();
        for (int i = 0; i < actionBlock.getRowCount(); ++i) {
            buffer.append("<TR>");
            String commandType = actionBlock.getString(i, COMMANDTYPE);
            int level = actionBlock.getBigDecimal(i, LEVEL).intValue();
            if (commandType.equals(COMMANDTYPE_ACTION) || commandType.equals(COMMANDTYPE_BLOCKPROPERTY) || commandType.equals(COMMANDTYPE_RETURNPROPERTY)) {
                int j;
                buffer.append("<!----- rendering a row for -----" + commandType + " ------- -->\n ");
                for (j = 0; j < level; ++j) {
                    buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100></td>\n");
                }
                buffer.append("<td  width=100>" + actionBlock.getString(i, COMMANDID) + "</td>\n");
                if (commandType.equals(COMMANDTYPE_ACTION)) {
                    buffer.append("<td style=\"background-color:gainsboro\" width=100>" + this.createHyperLink(actionBlock.getString(i, LABEL), actionBlock.getString(i, COMMANDID)) + "</td>\n");
                } else if (commandType.equals(COMMANDTYPE_RETURNPROPERTY)) {
                    buffer.append("<td style=\"background-color:lightgreen\" width=100>" + actionBlock.getString(i, LABEL) + "</td>\n");
                } else {
                    buffer.append("<td style=\"background-color:cornsilk\" width=100>" + actionBlock.getString(i, LABEL) + "</td>\n");
                }
                for (j = level; j < maxlevel + 1; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
                if (i + 1 < actionBlock.getRowCount() && actionBlock.getString(i + 1, COMMANDTYPE).equals(COMMANDTYPE_STARTACTIONBLOCK)) {
                    buffer.append("<TR>\n");
                    for (j = 0; j < level; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                        if (j == level - 2) {
                            buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100>false</td>\n");
                            continue;
                        }
                        buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    }
                    buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100></td>\n");
                    for (j = level; j < maxlevel + 1; ++j) {
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    }
                    buffer.append("</tr>\n");
                }
            } else if (commandType.equals(COMMANDTYPE_STARTACTIONBLOCK)) {
                buffer.append("<!----- rendering a row for -----" + commandType + " ------- -->\n ");
                buffer.append("<TR>");
                for (int j = 0; j < level - 1; ++j) {
                    buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    if (j == level - 2) {
                        buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100>false</td>\n");
                        continue;
                    }
                    buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                }
                String condition = actionBlock.getString(i, TESTCONDITION);
                if (condition.length() > 0 && condition.startsWith("$G{")) {
                    condition = condition.substring(3, condition.length() - 1);
                }
                buffer.append("<td colspan=4 width=400 height=100 style=\"border=0px; background: url(images/WEB-CORE/images/png/decision.png) no-repeat 0 0;background-width:100%\" align='left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + condition + "</td>\n");
                for (int j = level; j < maxlevel; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
            } else if (commandType.equals(COMMANDTYPE_ENDACTIONBLOCK)) {
                int j;
                buffer.append("<!----- rendering a row for -----" + commandType + " ------- -->\n ");
                for (j = 0; j < level - 1; ++j) {
                    buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100></td>\n");
                }
                buffer.append("<td style=\"border-left:0px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-left:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-left:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>");
                for (j = level; j < maxlevel; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
                buffer.append("<!----- rendering a spacing row for -----" + commandType + " ------- -->\n ");
                buffer.append("</TR>\n");
                buffer.append("<TR>\n");
                for (j = 0; j < level - 1; ++j) {
                    buffer.append("<td style=\"border-left:0px; border-right=1px; border-bottom=0px;border-top=0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border-right=0px;border-bottom=0px;border-top=0px\" width=100></td>\n");
                }
                buffer.append("<td style=\"border-left:0px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>");
                for (j = level; j < maxlevel; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
            }
            buffer.append("</TR>\n");
        }
        buffer.append("</table>\n");
    }

    public void addCheckBox(StringBuffer buffer, String label, boolean selected) {
        String checked = "no";
        if (selected) {
            checked = "yes";
        }
        String html = "<input type = \"checkbox\" disabled=\"disabled\" value=\"" + label + "\" checked=\"" + checked + "\">" + label + "</input>";
        buffer.append(html);
    }

    public StringBuffer renderPropertyList(PropertyList pl, boolean top) {
        StringBuffer tempTable = new StringBuffer();
        if (pl.size() == 0) {
            return tempTable;
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            if (pl.isPropertyList(keyes[i].toString())) {
                StringBuffer propertylist = this.renderPropertyList(pl.getPropertyList(keyes[i].toString()), false);
                if (propertylist.length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, keyes[i].toString(), propertylist.toString());
                this.endRow(tempTable);
                continue;
            }
            if (pl.isCollection(keyes[i].toString())) {
                StringBuffer collection = this.renderCollection(pl.getCollection(keyes[i].toString()), false);
                if (collection.length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, keyes[i].toString(), collection.toString());
                this.endRow(tempTable);
                continue;
            }
            String prop = pl.getProperty(keyes[i].toString(), "");
            if (prop.length() <= 0) continue;
            this.startRow(tempTable);
            if (this.isImage(prop)) {
                this.addRowItem(tempTable, keyes[i].toString(), prop);
            } else {
                this.addRowItem(tempTable, keyes[i].toString(), HttpUtil.htmlEncode(prop));
            }
            this.endRow(tempTable);
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    public StringBuffer renderPropertyList(PropertyList pl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top) {
        if (defList == null || defList.size() == 0) {
            return this.renderPropertyList(pl, top);
        }
        StringBuffer tempTable = new StringBuffer();
        if (pl.size() == 0) {
            return tempTable;
        }
        for (PropertyDefinition propDef : defList) {
            PropertyDefinitionList currPropDefList;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (pl.isPropertyList(currPropId)) {
                StringBuffer propertylist;
                currPropDefList = propDef.getPropertyDefinitionList();
                PropertyList cld = pl.getPropertyList(currPropId);
                if (cld.size() <= 0 || (propertylist = this.renderPropertyList(cld, currPropDefList, reportAdvancedProperties, false)).length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, currPropTitle, propertylist.toString());
                this.endRow(tempTable);
                continue;
            }
            if (pl.isCollection(currPropId)) {
                currPropDefList = propDef.getPropertyDefinitionList();
                StringBuffer collection = this.renderCollection(pl.getCollection(currPropId), currPropDefList, reportAdvancedProperties, false);
                if (collection.length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, currPropTitle, collection.toString());
                this.endRow(tempTable);
                continue;
            }
            String p = pl.getProperty(currPropId, "");
            if (p.length() <= 0) continue;
            this.startRow(tempTable);
            if (!this.isImage(p)) {
                this.addRowItem(tempTable, currPropTitle, HttpUtil.htmlEncode(p));
            } else {
                this.addRowItem(tempTable, currPropTitle, p);
            }
            this.endRow(tempTable);
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    public StringBuffer renderPropertyListDiff(PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top) {
        if (defList == null || defList.size() == 0) {
            return this.renderPropertyListDiff(pl, refPl, top);
        }
        if (refPl == null) {
            return this.renderPropertyList(pl, defList, reportAdvancedProperties, top);
        }
        StringBuffer tempTable = new StringBuffer();
        if (pl.size() == 0) {
            return tempTable;
        }
        for (PropertyDefinition propDef : defList) {
            PropertyDefinitionList currPropDefList;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (pl.isPropertyList(currPropId)) {
                PropertyList refCld;
                currPropDefList = propDef.getPropertyDefinitionList();
                PropertyList cld = pl.getPropertyList(currPropId);
                StringBuffer propertylist = this.renderPropertyListDiff(cld, refCld = refPl.getPropertyList(currPropId), currPropDefList, reportAdvancedProperties, false);
                if (propertylist.length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, currPropTitle, propertylist.toString());
                this.endRow(tempTable);
                continue;
            }
            if (pl.isCollection(currPropId)) {
                currPropDefList = propDef.getPropertyDefinitionList();
                StringBuffer collection = this.renderCollectionDiff(pl.getCollection(currPropId), refPl.getCollection(currPropId), currPropDefList, reportAdvancedProperties, false);
                if (collection.length() <= 0) continue;
                this.startRow(tempTable);
                this.addRowItem(tempTable, currPropTitle, collection.toString());
                this.endRow(tempTable);
                continue;
            }
            String p = pl.getProperty(currPropId, "");
            String refp = refPl.getProperty(currPropId, "");
            if (p.length() <= 0 && refp.length() <= 0) continue;
            this.startRow(tempTable);
            if (!this.isImage(p)) {
                this.addDiffRowItem(tempTable, currPropTitle, HttpUtil.htmlEncode(p), HttpUtil.htmlEncode(refp));
            } else {
                this.addDiffRowItem(tempTable, currPropTitle, p, refp);
            }
            this.endRow(tempTable);
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    public StringBuffer renderCollection(PropertyListCollection coll, boolean top) {
        StringBuffer tempTable = new StringBuffer();
        if (coll.size() == 0) {
            return tempTable;
        }
        for (int i = 0; i < coll.size(); ++i) {
            PropertyList pl = coll.getPropertyList(i);
            StringBuffer properytlist = this.renderPropertyList(pl, false);
            if (properytlist.length() <= 0) continue;
            this.startRow(tempTable);
            this.addRowItem(tempTable, pl.getProperty("id", "not defined"), properytlist.toString());
            this.endRow(tempTable);
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    public StringBuffer renderCollection(PropertyListCollection coll, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top) {
        if (defList == null || defList.size() == 0) {
            return this.renderCollection(coll, top);
        }
        StringBuffer tempTable = new StringBuffer();
        if (coll.size() == 0) {
            return tempTable;
        }
        for (int i = 0; i < coll.size(); ++i) {
            StringBuffer propertyList;
            String title;
            PropertyList pl = coll.getPropertyList(i);
            String titleProperty = defList.getTitlePropertyId();
            if (titleProperty == null || titleProperty.trim().length() == 0) {
                titleProperty = "id";
            }
            if ((title = pl.getProperty(titleProperty)) == null || title.trim().length() == 0) {
                title = pl.getProperty("id");
            }
            if ((propertyList = this.renderPropertyList(pl, defList, reportAdvancedProperties, false)).length() <= 0) continue;
            this.startRow(tempTable);
            this.addRowItem(tempTable, title, propertyList.toString());
            this.endRow(tempTable);
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    private String getTitle(PropertyDefinitionList defList, PropertyList pl) {
        String title;
        String titleProperty = defList.getTitlePropertyId();
        if (titleProperty == null || titleProperty.trim().length() == 0) {
            titleProperty = "id";
        }
        if ((title = pl.getProperty(titleProperty)) == null || title.trim().length() == 0) {
            title = pl.getProperty("id");
        }
        return title;
    }

    public StringBuffer renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top) {
        int i;
        if (defList == null || defList.size() == 0) {
            return this.renderCollectionDiff(coll, refColl, top);
        }
        StringBuffer tempTable = new StringBuffer();
        if (coll.size() == 0) {
            return tempTable;
        }
        for (i = 0; i < coll.size(); ++i) {
            String propertylist;
            PropertyList pl = coll.getPropertyList(i);
            if (pl == null || pl.size() <= 0) continue;
            String title = this.getTitle(defList, pl);
            PropertyList refPl = this.find(i, coll, refColl);
            if (refPl == null || refPl.size() == 0) {
                propertylist = this.renderNewPropertyList(pl, false).toString();
                if (propertylist.length() <= 0) continue;
                this.startRow(tempTable);
                this.addNewRowItem(tempTable, title, propertylist);
                this.endRow(tempTable);
                continue;
            }
            propertylist = this.renderPropertyListDiff(pl, refPl, defList, reportAdvancedProperties, false).toString();
            if (propertylist.length() <= 0) continue;
            this.startRow(tempTable);
            this.addRowItem(tempTable, title, propertylist);
            this.endRow(tempTable);
        }
        if (refColl != null) {
            for (i = 0; i < refColl.size(); ++i) {
                PropertyList currpl;
                PropertyList refpl = refColl.getPropertyList(i);
                if (refpl == null || refpl.size() <= 0 || (currpl = this.find(i, refColl, coll)) != null && currpl.size() != 0) continue;
                String title = this.getTitle(defList, refpl);
                this.startRow(tempTable);
                this.addDeletedRowItem(tempTable, title, this.renderDeletedPropertyList(refpl, false).toString());
                this.endRow(tempTable);
            }
        }
        StringBuffer table = new StringBuffer();
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable);
            table.append(this.getEndTable());
        }
        return table;
    }

    public void startSubHeading(StringBuffer buffer, String title, String desc) {
        String str = "";
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        str = str + "<H4>" + title + "</H4><P>";
        str = str + desc;
        str = str + "<P>";
        buffer.append(str);
    }

    public void startSubHeading(StringBuffer buffer, String title, String desc, String anchor) {
        String str = "";
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        str = str + "<H4 id=\"" + anchor + "\">" + title + "</H4><P>";
        str = str + desc;
        str = str + "<P>";
        buffer.append(str);
    }

    public void copyFile(File in, File out) throws Exception {
        try {
            if (in.exists()) {
                out.getParentFile().mkdirs();
                FileChannel sourceChannel = new FileInputStream(in).getChannel();
                FileChannel destinationChannel = new FileOutputStream(out).getChannel();
                sourceChannel.transferTo(0L, sourceChannel.size(), destinationChannel);
                sourceChannel.close();
                destinationChannel.close();
            } else {
                Trace.logError(in.getAbsolutePath() + " does not exist.");
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to copy file: " + e.getMessage());
        }
    }

    public PropertyList parseValidationValues(String validation) {
        PropertyList ret = new PropertyList();
        if (validation.trim().length() > 0) {
            String[] parts = StringUtil.split(validation, ";");
            for (int i = 0; i < parts.length; ++i) {
                String curr = parts[i];
                if (curr.trim().equals("Mandatory")) {
                    ret.setProperty("Mandatory", "Y");
                    continue;
                }
                if (curr.trim().equals("Date")) {
                    ret.setProperty("Date", "Y");
                    continue;
                }
                if (curr.trim().equals("Number")) {
                    ret.setProperty("Number", "Y");
                    continue;
                }
                String lhs = curr.substring(0, curr.indexOf("("));
                String rhs = curr.substring(curr.indexOf("(") + 1, curr.indexOf(")"));
                ret.setProperty("Range:", lhs + " range: " + rhs);
            }
        }
        return ret;
    }

    public String renderValidationDetails(String validation) {
        StringBuffer ret = new StringBuffer();
        if (validation.trim().length() > 0) {
            if (validation.indexOf(";") > -1) {
                String[] parts = StringUtil.split(validation, ";");
                this.startTableInner(ret);
                for (int i = 0; i < parts.length; ++i) {
                    String curr = parts[i];
                    this.startRow(ret);
                    if (curr.trim().equals("Mandatory")) {
                        this.addRowItem(ret, "Mandatory", "Y");
                    } else if (curr.trim().equals("Date")) {
                        this.addRowItem(ret, "Date", "Y");
                    } else {
                        String lhs = curr.substring(0, curr.indexOf("("));
                        String rhs = curr.substring(curr.indexOf("(") + 1, curr.indexOf(")"));
                        this.addRowItem(ret, lhs, "range: " + rhs);
                    }
                    this.endRow(ret);
                }
                this.endTable(ret);
            } else {
                ret.append(validation.replace("(", " range: ").replace(")", ""));
            }
        }
        return ret.toString();
    }

    public PropertyList parseDisplayValues(String displayValue) throws SapphireException {
        PropertyList p = new PropertyList();
        String[] options = StringUtil.split(displayValue, ";");
        for (int i = 0; i < options.length; ++i) {
            if (options[i].indexOf("=") > -1) {
                String rhs;
                String lhs = "Others";
                if (options[i].indexOf("=") != 0) {
                    lhs = options[i].substring(0, options[i].indexOf("="));
                }
                if ((rhs = options[i].substring(options[i].indexOf("=") + 1)).indexOf("img") > -1 || rhs.indexOf("IMG") > -1) {
                    rhs = this.changeImageFolder(rhs);
                }
                p.setProperty(lhs, rhs);
                continue;
            }
            if (options[i].length() <= 0) continue;
            p.setProperty("Default", options[i]);
        }
        return p;
    }

    public String changeImageFolder(String imageURL) throws SapphireException {
        String newURL = imageURL;
        if (imageURL != null && (imageURL.indexOf("img") > 0 || imageURL.indexOf("IMG") > 0)) {
            int startpos = imageURL.indexOf("src=") + 4;
            int endpos = imageURL.substring(startpos).indexOf(" ");
            if (endpos == -1) {
                endpos = imageURL.substring(startpos).indexOf(">");
            }
            String srcval = imageURL.substring(startpos, endpos += startpos);
            srcval = srcval.replaceAll("'", "");
            srcval = srcval.replaceAll("\"", "");
            String srcImageDir = this.applicationRoot;
            try {
                this.copyFile(new File(srcImageDir + srcval), new File(this.folder + "/images/" + srcval));
                newURL = "<img src=\"../images/" + srcval + "\"/>";
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return newURL;
    }

    public String getPageName(String link) {
        int pagePos = link.toLowerCase().indexOf("page=");
        if (pagePos >= 0) {
            int amp = link.indexOf("&", pagePos);
            return amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp);
        }
        return "";
    }

    public String getWizardName(String link) {
        int wizPos = link.toLowerCase().indexOf("wizard=");
        if (wizPos >= 0) {
            int amp = link.indexOf("&", wizPos);
            return amp == -1 ? link.substring(wizPos + 7) : link.substring(wizPos + 7, amp);
        }
        return "";
    }

    public String getFileName(String link) {
        int filepos = link.toLowerCase().indexOf("file=");
        if (filepos >= 0) {
            int amp = link.indexOf("&", filepos);
            return amp == -1 ? link.substring(filepos + 5) : link.substring(filepos + 5, amp);
        }
        return "";
    }

    public String createHyperLink(String source, String ref) {
        return "<A HREF=\"#" + this.generateSectionAnchor(ref) + "\">" + source + "</A>";
    }

    public String renderLink(String link, HashMap sdisIncluded) {
        String currColLink = link;
        int pagePos = link.toLowerCase().indexOf("page=");
        int filePos = link.toLowerCase().indexOf("file=");
        int wizardPos = link.toLowerCase().indexOf("wizard=");
        if (link.trim().toLowerCase().startsWith("javascript")) {
            currColLink = link;
        } else if (pagePos >= 0) {
            String webpageId;
            int amp = link.indexOf("&", pagePos);
            String string = webpageId = amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp);
            if (this.isFKIncluded("WebPage", webpageId, sdisIncluded)) {
                String href;
                SDI sdi = new SDI("WebPage", webpageId, this.getPageEdition(webpageId), "");
                String anchor = this.generateSDISectionAnchor(sdi);
                if (!this.frames) {
                    href = "#" + anchor;
                } else {
                    String sectionFileName = this.generateSDISectionFileName(sdi);
                    href = sectionFileName + "#" + anchor;
                }
                currColLink = "<A HREF=\"" + href + "\">" + webpageId + "</A>";
            } else {
                currColLink = "" + (amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp));
            }
        } else if (filePos >= 0) {
            int amp = link.indexOf("&", filePos);
            currColLink = "File: " + (amp == -1 ? link.substring(filePos + 5) : link.substring(filePos + 5, amp));
        } else if (wizardPos >= 0) {
            int amp = link.indexOf("&", wizardPos);
            currColLink = "Wizard: " + (amp == -1 ? link.substring(wizardPos + 7) : link.substring(wizardPos + 7, amp));
        }
        return currColLink;
    }

    public String createHyperLink(String sdcid, String fkkeyid1, String fkkeyid2, String fkkeyid3, HashMap sdisIncluded) {
        if (fkkeyid1 == null || fkkeyid1.length() == 0) {
            return "";
        }
        if (this.isFKIncluded(sdcid, fkkeyid1, fkkeyid2, fkkeyid3, sdisIncluded)) {
            SDI sdi = new SDI(sdcid, fkkeyid1, fkkeyid2, fkkeyid3);
            String anchor = this.generateSDISectionAnchor(sdi);
            String href = "";
            if (!this.frames) {
                href = "#" + anchor;
            } else {
                String sectionFileName = this.generateSDISectionFileName(sdi);
                href = sectionFileName + "#" + anchor;
            }
            return "<A HREF=\"" + href + "\">" + fkkeyid1 + "</A>";
        }
        return fkkeyid1;
    }

    public String getPageEdition(String webpageId) {
        try {
            return new WebAdminProcessor(this.config.getProperty("connection")).getDefaultPageEdition(webpageId);
        }
        catch (Exception e) {
            return "R5";
        }
    }

    private StringBuffer getFramesReport(int chapterCount, ArrayList sdcList, boolean includeGenericLayout) {
        String firstItem = "";
        firstItem = includeGenericLayout ? "Generic_Layout" : sdcList.get(0).toString();
        StringBuffer str = new StringBuffer();
        str.append("<HTML>\n");
        str.append("<HEAD>\n");
        str.append("<TITLE>Configuration Report</TITLE>\n");
        str.append("<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\">\n");
        str.append("</HEAD>\n");
        str.append("<script>var _firstloading=true;</script><FRAMESET cols=\"20%, 80%\">");
        if (chapterCount > 1) {
            str.append("  <FRAMESET rows=\"100, 200\">\n");
            str.append("      <FRAME name=\"TOC\" src=\"TOC.html\">\n");
            String sub = "      <FRAME name=\"Sub\" src=\"" + firstItem + "_TOC.html\">\n";
            str.append(sub);
            str.append("  </FRAMESET>\n");
        } else {
            String sub = "      <FRAME name=\"Sub\" src=\"" + firstItem + "_TOC.html\">\n";
            str.append(sub);
        }
        str.append("  <FRAME name=\"Chapter\" src=\"Cover_Page.html\">\n");
        str.append("</FRAMESET>\n");
        str.append("</HTML>");
        return str;
    }

    public void renderDiffListTable(StringBuffer buffer, DataSet ds, DataSet ref, String[] keycols) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            this.startListTableTop(buffer);
            if (ref == null) {
                this.addListItems(buffer, ds);
            } else {
                this.addDiffListItems(buffer, ds, ref, keycols);
            }
            this.endListTable(buffer);
        } else {
            buffer.append("No entries.");
        }
    }

    private void addDiffListItems(StringBuffer buffer, DataSet listItems, DataSet ref, String[] keycols) {
        int i;
        if (keycols == null) {
            keycols = new String[1];
            String[] allCols = listItems.getColumns();
            keycols[0] = allCols[0];
        }
        listItems = this.addDiffInfo(listItems, ref, keycols);
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader(buffer);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("_")) continue;
            this.addColumnHeader(buffer, colList[i]);
        }
        this.endHeader(buffer);
        for (i = 0; i < rows; ++i) {
            buffer.append("<TR VALIGN=TOP >\n");
            String status = listItems.getString(i, "_status");
            String changedCols = listItems.getString(i, "_changedcols");
            DataSet changedColInfo = new DataSet(changedCols);
            for (int j = 0; j < cols; ++j) {
                String columnValue = listItems.getValue(i, colList[j], "");
                if (colList[j].startsWith("_")) continue;
                buffer.append("<td class=\"viewlistcol\">\n");
                if (status.equals("New")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderNewPropertyList(pl, false).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = this.isImage(columnValue) ? "New: " + columnValue : this.getNewString(columnValue);
                        }
                    }
                } else if (status.equals("Deleted")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderDeletedPropertyList(pl, false).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = this.getDeletedString(columnValue);
                            }
                        } else {
                            columnValue = this.isImage(columnValue) ? "Deleted: " + columnValue : this.getDeletedString(columnValue);
                        }
                    }
                } else if (status.equals("Modified")) {
                    boolean isModifiedCol = false;
                    String oldVal = "";
                    for (int x = 0; x < changedColInfo.getRowCount(); ++x) {
                        String colname = changedColInfo.getString(x, "colname", "");
                        if (!colname.equals(colList[j])) continue;
                        isModifiedCol = true;
                        oldVal = changedColInfo.getString(x, "oldval", "").replaceAll("!]!]!>", "]]>");
                    }
                    if (isModifiedCol) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            PropertyList oldPl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                oldPl.setPropertyList(oldVal, false, false);
                                columnValue = this.renderPropertyListDiff(pl, oldPl, false).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = this.getDiffString(columnValue, oldVal);
                            }
                        } else if (this.isImage(columnValue)) {
                            columnValue = "New Value:" + columnValue;
                            columnValue = columnValue + "<P>Old Value:" + oldVal;
                        } else {
                            columnValue = this.getDiffString(columnValue, oldVal);
                        }
                    } else if (this.isPropertyList(columnValue)) {
                        PropertyList pl = new PropertyList();
                        try {
                            pl.setPropertyList(columnValue, false, false);
                            columnValue = this.renderPropertyList(pl, false).toString();
                        }
                        catch (SapphireException sapphireException) {}
                    }
                } else if (this.isPropertyList(columnValue)) {
                    PropertyList pl = new PropertyList();
                    try {
                        pl.setPropertyList(columnValue, false, false);
                        columnValue = this.renderPropertyList(pl, false).toString();
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                }
                buffer.append(columnValue);
                buffer.append("</td>\n");
            }
            buffer.append("</TR>\n");
        }
    }

    public String[] getRolesFromMatrix(DataSet roleMatrix, String[] keyColumns) {
        String[] allcolumns = roleMatrix.getColumns();
        ArrayList<String> roles = new ArrayList<String>();
        boolean rolecount = false;
        for (int i = 0; i < allcolumns.length; ++i) {
            boolean isKeyCol = false;
            for (int j = 0; j < keyColumns.length; ++j) {
                if (!allcolumns[i].equals(keyColumns[j])) continue;
                isKeyCol = true;
            }
            if (isKeyCol) continue;
            roles.add(allcolumns[i]);
        }
        Object[] o = roles.toArray();
        String[] r = new String[o.length];
        for (int i = 0; i < o.length; ++i) {
            r[i] = (String)o[i];
        }
        return r;
    }

    private int findItemInDS(DataSet source, int sourceitem, DataSet compare, String[] keyCols) {
        for (int item = 0; item < compare.getRowCount(); ++item) {
            boolean match = true;
            for (int keycolid = 0; keycolid < keyCols.length; ++keycolid) {
                String comp;
                String src = source.getString(sourceitem, keyCols[keycolid], "").trim();
                if (src.equals(comp = compare.getString(item, keyCols[keycolid], "").trim())) continue;
                match = false;
            }
            if (!match) continue;
            return item;
        }
        return -1;
    }

    public DataSet getMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns) {
        return this.getMatrixDiffInfo(roleMatrix, refRoleMatrix, keyColumns, false);
    }

    public DataSet getMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns, boolean sortby) {
        String curr;
        int j;
        int i;
        DataSet diffRoleMatrix = new DataSet();
        diffRoleMatrix.setColidCaseSensitive(true);
        if (sortby) {
            diffRoleMatrix.addColumn("__sortby", 0);
        }
        for (int i2 = 0; i2 < keyColumns.length; ++i2) {
            diffRoleMatrix.addColumn(keyColumns[i2], 0);
        }
        String[] roles = this.getRolesFromMatrix(roleMatrix, keyColumns);
        String[] refRoles = this.getRolesFromMatrix(refRoleMatrix, keyColumns);
        for (i = 0; i < roles.length; ++i) {
            String currRole = roles[i];
            diffRoleMatrix.addColumn(currRole, 0);
        }
        for (i = 0; i < refRoles.length; ++i) {
            String currRefRole = refRoles[i];
            diffRoleMatrix.addColumn(currRefRole, 0);
        }
        for (i = 0; i < roleMatrix.getRowCount(); ++i) {
            int refItem;
            int currRow = diffRoleMatrix.addRow();
            if (sortby) {
                diffRoleMatrix.setString(currRow, "__sortby", roleMatrix.getString(i, keyColumns[0], ""));
            }
            if ((refItem = this.findItemInDS(roleMatrix, i, refRoleMatrix, keyColumns)) != -1) {
                for (j = 0; j < keyColumns.length; ++j) {
                    diffRoleMatrix.setString(currRow, keyColumns[j], roleMatrix.getString(i, keyColumns[j], ""));
                }
            } else {
                for (j = 0; j < keyColumns.length; ++j) {
                    diffRoleMatrix.setString(currRow, keyColumns[j], this.getNewString(roleMatrix.getString(i, keyColumns[j], "")));
                }
            }
            for (j = 0; j < roles.length; ++j) {
                curr = roleMatrix.getString(i, roles[j], "");
                String ref = "";
                if (refItem != -1) {
                    ref = refRoleMatrix.getString(refItem, roles[j], "");
                }
                if (curr.length() > 0 && ref.length() > 0) {
                    diffRoleMatrix.setString(currRow, roles[j], curr);
                    continue;
                }
                if (curr.length() == 0 && ref.length() > 0) {
                    this.foundDiff = true;
                    diffRoleMatrix.setString(currRow, roles[j], "<diffpoint/>" + ref.replace("Confirm", "Delete"));
                    continue;
                }
                if (ref.length() != 0 || curr.length() <= 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, roles[j], "<diffpoint/>" + curr.replace("Confirm", "Add"));
            }
            if (refItem == -1) continue;
            for (j = 0; j < refRoles.length; ++j) {
                String refrole = refRoleMatrix.getString(refItem, refRoles[j], "");
                String currrole = roleMatrix.getString(i, refRoles[j], "");
                if (refrole.length() <= 0 || currrole.length() != 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, refRoles[j], "<diffpoint/>" + refrole.replace("Confirm", "Delete"));
            }
        }
        for (i = 0; i < refRoleMatrix.getRowCount(); ++i) {
            int refItem = this.findItemInDS(refRoleMatrix, i, roleMatrix, keyColumns);
            if (refItem != -1) continue;
            int currRow = diffRoleMatrix.addRow();
            if (sortby) {
                diffRoleMatrix.setString(currRow, "__sortby", refRoleMatrix.getString(i, keyColumns[0], ""));
            }
            for (j = 0; j < keyColumns.length; ++j) {
                diffRoleMatrix.setString(currRow, keyColumns[j], this.getDeletedString(refRoleMatrix.getString(i, keyColumns[j], "")));
            }
            for (j = 0; j < refRoles.length; ++j) {
                curr = refRoleMatrix.getString(i, refRoles[j], "");
                if (curr.length() <= 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, refRoles[j], "<diffpoint/>" + curr.replace("Confirm", "Delete"));
            }
        }
        if (sortby) {
            diffRoleMatrix.sort("__sortby");
        }
        return diffRoleMatrix;
    }

    public DataSet getSitemapMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns) {
        String curr;
        int j;
        int i;
        DataSet diffRoleMatrix = new DataSet();
        diffRoleMatrix.setColidCaseSensitive(true);
        for (int i2 = 0; i2 < keyColumns.length; ++i2) {
            diffRoleMatrix.addColumn(keyColumns[i2], 0);
        }
        String[] roles = this.getRolesFromMatrix(roleMatrix, keyColumns);
        String[] refRoles = this.getRolesFromMatrix(refRoleMatrix, keyColumns);
        for (i = 0; i < roles.length; ++i) {
            String currRole = roles[i];
            diffRoleMatrix.addColumn(currRole, 0);
        }
        for (i = 0; i < refRoles.length; ++i) {
            String currRefRole = refRoles[i];
            if (diffRoleMatrix.isValidColumn(currRefRole)) continue;
            diffRoleMatrix.addColumn(currRefRole, 0);
        }
        for (i = 0; i < roleMatrix.getRowCount(); ++i) {
            int currRow = diffRoleMatrix.addRow();
            diffRoleMatrix.setString(currRow, "__status", "S");
            for (int j2 = 0; j2 < keyColumns.length; ++j2) {
                diffRoleMatrix.setString(currRow, keyColumns[j2], roleMatrix.getString(i, keyColumns[j2], ""));
            }
            int refItem = this.findItemInDS(roleMatrix, i, refRoleMatrix, keyColumns);
            if (refItem == -1) {
                diffRoleMatrix.setString(currRow, "__status", "N");
            }
            for (j = 0; j < roles.length; ++j) {
                curr = roleMatrix.getString(i, roles[j], "");
                String ref = "";
                if (refItem != -1) {
                    ref = refRoleMatrix.getString(refItem, roles[j], "");
                }
                if (curr.length() > 0 && ref.length() > 0) {
                    diffRoleMatrix.setString(currRow, roles[j], curr);
                    continue;
                }
                if (curr.length() == 0 && ref.length() > 0) {
                    this.foundDiff = true;
                    diffRoleMatrix.setString(currRow, roles[j], ref.replace("Confirm", "Delete"));
                    continue;
                }
                if (ref.length() != 0 || curr.length() <= 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, roles[j], curr.replace("Confirm", "Add"));
            }
            if (refItem == -1) continue;
            for (j = 0; j < refRoles.length; ++j) {
                String refrole = refRoleMatrix.getString(refItem, refRoles[j], "");
                String currrole = roleMatrix.getString(i, refRoles[j], "");
                if (refrole.length() <= 0 || currrole.length() != 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, refRoles[j], refrole.replace("Confirm", "Delete"));
            }
        }
        for (i = 0; i < refRoleMatrix.getRowCount(); ++i) {
            int refItem = this.findItemInDS(refRoleMatrix, i, roleMatrix, keyColumns);
            if (refItem != -1) continue;
            int currRow = diffRoleMatrix.addRow();
            diffRoleMatrix.setString(currRow, "__status", "D");
            for (j = 0; j < keyColumns.length; ++j) {
                diffRoleMatrix.setString(currRow, keyColumns[j], refRoleMatrix.getString(i, keyColumns[j], ""));
            }
            for (j = 0; j < refRoles.length; ++j) {
                curr = refRoleMatrix.getString(i, refRoles[j], "");
                if (curr.length() <= 0) continue;
                this.foundDiff = true;
                diffRoleMatrix.setString(currRow, refRoles[j], curr.replace("Confirm", "Delete"));
            }
        }
        return diffRoleMatrix;
    }

    public DataSet addDiffInfo(DataSet listItems, DataSet ref, String[] keyColumns) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        if (listItems == null) {
            listItems = new DataSet();
        }
        if (ref == null) {
            ref = new DataSet();
        }
        for (int item = 0; item < listItems.size(); ++item) {
            for (int keycol = 0; keycol < keyColumns.length; ++keycol) {
                String keyColumnid = keyColumns[keycol];
                String val = listItems.getValue(item, keyColumnid);
                if (listItems.getColumnType(keyColumnid) == 1) {
                    filter.put(keyColumnid, new BigDecimal(val));
                    continue;
                }
                filter.put(keyColumnid, val);
            }
            int refRow = ref.findRow(filter);
            if (refRow == -1) {
                this.foundDiff = true;
                listItems.setString(item, "_status", "New");
                continue;
            }
            DataSet changes = this.getChangedColumns(listItems, item, ref, refRow);
            if (changes.getRowCount() > 0) {
                this.foundDiff = true;
                listItems.setString(item, "_status", "Modified");
                listItems.setString(item, "_changedcols", changes.toXML());
            } else {
                listItems.setString(item, "_status", "None");
            }
            ref.deleteRow(refRow);
        }
        for (int delitems = 0; delitems < ref.getRowCount(); ++delitems) {
            int newRow = listItems.addRow();
            String[] columnNames = listItems.getColumns();
            for (int col = 0; col < columnNames.length; ++col) {
                listItems.setValue(newRow, columnNames[col], ref.getValue(delitems, columnNames[col], ""));
            }
            this.foundDiff = true;
            listItems.setString(newRow, "_status", "Deleted");
        }
        return listItems;
    }

    private String removeStrangeChars(String value) {
        value = value.replace("\ufffd", "");
        value = value.replace("#65533", "");
        value = value.replace("#160", "");
        value = value.replace("\u00a0", "");
        value = value.replace("\u00a0", "");
        value = value.replace("Y", "");
        if ((value = value.replace("Y", "")).equals("&#91;&#93;")) {
            value = "";
        }
        value = value.trim();
        return value;
    }

    private DataSet getChangedColumns(DataSet listItems, int itemno, DataSet ref, int refRow) {
        DataSet changedCols = new DataSet();
        changedCols.setColidCaseSensitive(true);
        String[] columnNames = listItems.getColumns();
        for (int i = 0; i < columnNames.length; ++i) {
            if (columnNames[i].equals("_status") || columnNames[i].equals("_changedcols") || columnNames[i].equals("_position")) continue;
            String newval = listItems.getValue(itemno, columnNames[i], "");
            newval = this.removeStrangeChars(newval);
            String oldval = ref.getValue(refRow, columnNames[i], "");
            if (newval.equals(oldval = this.removeStrangeChars(oldval))) continue;
            int currrow = changedCols.addRow();
            changedCols.setString(currrow, "colname", columnNames[i]);
            changedCols.setString(currrow, "oldval", oldval.replaceAll("]]>", "!]!]!>"));
            changedCols.setString(currrow, "newval", newval);
        }
        return changedCols;
    }

    public String getModifiedString(String orig) {
        this.foundDiff = true;
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportmodifieditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public String getDeletedString(String orig) {
        this.foundDiff = true;
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportdeleteditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public String getNewString(String orig) {
        this.foundDiff = true;
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportnewitem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public StringBuffer addDiffRowItem(StringBuffer buffer, String columnName, String columnVal, String refVal) {
        return this.addDiffRowItem(buffer, columnName, columnVal, refVal, 1);
    }

    public StringBuffer addDiffRowItem(StringBuffer buffer, String columnName, String columnVal, String refVal, int colspan) {
        if (columnVal == null) {
            columnVal = "";
        }
        if (refVal == null) {
            refVal = "";
        }
        if ((columnVal = this.removeStrangeChars(columnVal)).equals(refVal = this.removeStrangeChars(refVal))) {
            return this.addRowItem(buffer, columnName, columnVal, colspan);
        }
        String diffVal = "";
        if (columnVal.startsWith("<dataset") && refVal.startsWith("<dataset")) {
            DataSet src = new DataSet(columnVal);
            DataSet ref = new DataSet(refVal);
            StringBuffer diff = new StringBuffer();
            this.renderDiffListTable(diff, src, ref, src.getColumns());
            diffVal = diff.toString();
        } else if (columnVal.startsWith("<propertylist") && refVal.startsWith("<propertylist")) {
            PropertyList src = new PropertyList(columnVal);
            PropertyList ref = new PropertyList(refVal);
            StringBuffer diff = this.renderPropertyListDiff(src, ref, false);
            diffVal = diff.toString();
        } else {
            diffVal = this.getDiffString(columnVal, refVal);
        }
        return this.addRowItem(buffer, columnName, diffVal, colspan);
    }

    public String getDiffString(String value, String refValue) {
        if (value == null) {
            value = "";
        }
        if (refValue == null) {
            refValue = "";
        }
        if ((value = this.removeStrangeChars(value)).equals(refValue = this.removeStrangeChars(refValue))) {
            return value;
        }
        this.foundDiff = true;
        StringBuffer buffer = new StringBuffer();
        buffer.append("<diffpoint/>");
        if (!this.isImage(value)) {
            buffer.append("<font class=\"diffreportnewitem\">");
            buffer.append(value);
            buffer.append("</font>");
        } else {
            buffer.append("<font class=\"diffreportnewitem\">New: </font>" + value + "<br>");
        }
        if (!this.isImage(refValue)) {
            buffer.append("<font class=\"diffreportdeleteditem\">");
            buffer.append(refValue);
            buffer.append("</font>");
        } else {
            buffer.append("<font class=\"red\"> Old: </font>" + refValue);
        }
        return buffer.toString();
    }

    public StringBuffer renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, boolean top) {
        int i;
        StringBuffer collVal = new StringBuffer();
        if (coll.size() == 0) {
            return collVal;
        }
        if (top) {
            this.startTableTop(collVal);
        } else {
            this.startTableInner(collVal);
        }
        for (i = 0; i < coll.size(); ++i) {
            StringBuffer diff;
            PropertyList refPl;
            PropertyList pl = coll.getPropertyList(i);
            String currId = pl.getAttribute("id");
            if (currId == null || currId.length() == 0) {
                currId = pl.getProperty("id", "");
            }
            if ((refPl = this.find(i, coll, refColl)) != null) {
                diff = this.renderPropertyListDiff(pl, refPl, false);
                this.startRow(collVal);
                String lhs = pl.getProperty("id", "not defined");
                if (diff.indexOf("<strike>") > -1) {
                    this.foundDiff = true;
                }
                this.addRowItem(collVal, lhs, diff.toString());
                this.endRow(collVal);
                continue;
            }
            diff = this.renderNewPropertyList(pl, false);
            this.startRow(collVal);
            this.foundDiff = true;
            this.addNewRowItem(collVal, pl.getProperty("id", "not defined"), diff.toString());
            this.endRow(collVal);
        }
        for (i = 0; i < refColl.size(); ++i) {
            PropertyList refpl = refColl.getPropertyList(i);
            String refcurrId = refpl.getProperty("id", refpl.getAttribute("id"));
            Object pl = null;
            boolean found = false;
            if (refcurrId.length() <= 0) continue;
            for (int j = 0; j < coll.size(); ++j) {
                PropertyList tpl = coll.getPropertyList(j);
                String currId = tpl.getProperty("id", tpl.getAttribute("id"));
                if (!currId.equals(refcurrId)) continue;
                found = true;
            }
            if (found) continue;
            this.startRow(collVal);
            this.foundDiff = true;
            StringBuffer item = this.renderDeletedPropertyList(refpl, false);
            this.addDeletedRowItem(collVal, refpl.getProperty("id", refpl.getAttribute("id")), item.toString());
            this.endRow(collVal);
        }
        this.endTable(collVal);
        return collVal;
    }

    public StringBuffer renderPropertyListDiff(PropertyList pl, PropertyList refPl, boolean top) {
        StringBuffer temp = new StringBuffer();
        if (refPl == null) {
            refPl = new PropertyList();
        }
        if (pl.size() == 0) {
            return temp;
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            String p1;
            if (pl.isPropertyList(keyes[i].toString())) {
                PropertyList childRefPl = refPl.getPropertyList(keyes[i].toString());
                String propertylist = this.renderPropertyListDiff(pl.getPropertyList(keyes[i].toString()), childRefPl, false).toString();
                if (propertylist.length() <= 0) continue;
                this.startRow(temp);
                this.addRowItem(temp, keyes[i].toString(), propertylist);
                this.endRow(temp);
                continue;
            }
            if (pl.isCollection(keyes[i].toString())) {
                PropertyListCollection childRefCollection = refPl.getCollectionNotNull(keyes[i].toString());
                String collection = this.renderCollectionDiff(pl.getCollection(keyes[i].toString()), childRefCollection, false).toString();
                if (collection.length() <= 0) continue;
                this.startRow(temp);
                this.addRowItem(temp, keyes[i].toString(), collection);
                this.endRow(temp);
                continue;
            }
            if (refPl != null) {
                p1 = pl.getProperty(keyes[i].toString(), "");
                String p2 = refPl.getProperty(keyes[i].toString(), "");
                if (p1.length() <= 0 && p2.length() <= 0) continue;
                this.startRow(temp);
                this.addDiffRowItem(temp, keyes[i].toString(), p1, p2);
                this.endRow(temp);
                continue;
            }
            p1 = pl.getProperty(keyes[i].toString(), "");
            if (p1.length() <= 0) continue;
            this.startRow(temp);
            this.addRowItem(temp, keyes[i].toString(), p1);
            this.endRow(temp);
        }
        StringBuffer table = new StringBuffer();
        if (temp.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(temp);
            table.append(this.getEndTable());
        }
        return table;
    }

    public StringBuffer renderNewPropertyList(PropertyList pl, boolean top) {
        this.foundDiff = true;
        StringBuffer table = new StringBuffer();
        if (pl.size() == 0) {
            return table;
        }
        if (top) {
            this.startTableTop(table);
        } else {
            this.startTableInner(table);
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            this.startRow(table);
            if (pl.isPropertyList(keyes[i].toString())) {
                this.addNewRowItem(table, keyes[i].toString(), this.renderPropertyList(pl.getPropertyList(keyes[i].toString()), false).toString());
            } else if (pl.isCollection(keyes[i].toString())) {
                this.addNewRowItem(table, keyes[i].toString(), this.renderCollection(pl.getCollection(keyes[i].toString()), false).toString());
            } else {
                this.addNewRowItem(table, keyes[i].toString(), HttpUtil.htmlEncode(pl.getProperty(keyes[i].toString(), "")));
            }
            this.endRow(table);
        }
        this.endTable(table);
        return table;
    }

    public StringBuffer renderDeletedPropertyList(PropertyList pl, boolean top) {
        this.foundDiff = true;
        StringBuffer table = new StringBuffer();
        if (pl.size() == 0) {
            return table;
        }
        if (top) {
            this.startTableTop(table);
        } else {
            this.startTableInner(table);
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            this.startRow(table);
            if (pl.isPropertyList(keyes[i].toString())) {
                this.addDeletedRowItem(table, keyes[i].toString(), this.renderDeletedPropertyList(pl.getPropertyList(keyes[i].toString()), false).toString());
            } else if (pl.isCollection(keyes[i].toString())) {
                this.addDeletedRowItem(table, keyes[i].toString(), this.renderCollection(pl.getCollection(keyes[i].toString()), false).toString());
            } else {
                this.addDeletedRowItem(table, keyes[i].toString(), HttpUtil.htmlEncode(pl.getProperty(keyes[i].toString(), "")));
            }
            this.endRow(table);
        }
        this.endTable(table);
        return table;
    }

    PropertyList find(int pos, PropertyListCollection coll, PropertyListCollection refColl) {
        PropertyList currPl = coll.getPropertyList(pos);
        if (currPl.size() == 0) {
            Trace.log("currPl is empty.");
            return new PropertyList();
        }
        if (refColl == null) {
            Trace.log("refColl is empty.");
            return new PropertyList();
        }
        String idAttribute = currPl.getAttribute("id");
        if (idAttribute != null && idAttribute.length() > 0) {
            for (int j = 0; j < refColl.size(); ++j) {
                String refIdAttribute;
                PropertyList temp = refColl.getPropertyList(j);
                if (temp == null || !(refIdAttribute = temp.getAttribute("id")).equals(idAttribute)) continue;
                return temp;
            }
        }
        String idPropertyName = currPl.getProperty("id");
        return refColl.find("id", idPropertyName);
    }

    public void renderMatrix(StringBuffer buffer, DataSet matrix, int keycols) {
        if (matrix == null || matrix.getRowCount() == 0) {
            buffer.append(" Not found.");
            return;
        }
        this.addMatrix(buffer, matrix, keycols);
    }

    public void addMatrix(StringBuffer buffer, DataSet roleMatrix, int keycols) {
        this.startListTableTop(buffer);
        this.addListItems(buffer, roleMatrix, false, keycols);
        this.endListTable(buffer);
    }

    public void renderDiffMatrix(StringBuffer buffer, DataSet matrix, DataSet refMatrix, String[] keycols) {
        if (matrix == null || matrix.getRowCount() == 0) {
            buffer.append("Not found.");
            return;
        }
        if (refMatrix == null) {
            this.addMatrix(buffer, matrix, matrix.getColumnCount());
            return;
        }
        this.startListTableTop(buffer);
        this.addDiffListItems(buffer, matrix, refMatrix, keycols);
        this.endListTable(buffer);
    }

    public String insertDiffAnchors(String originalContent) {
        StringBuilder content = new StringBuilder(originalContent);
        int count = 0;
        int pos = 0;
        while (pos >= 0) {
            String anchor = "<diffpoint/>";
            if ((pos = content.indexOf(anchor, pos + 1)) < 0) continue;
            content.replace(pos, pos + anchor.length(), "<diffpoint id=\"diffpoint" + count + "\"/>");
            ++count;
        }
        if (count == 0) {
            return content.toString();
        }
        String javascript = "<script>var diffpointcounter=-1;function doonload() {   window.focus();}function bodykeydown() { if ( event.keyCode==40&&event.ctrlKey) {diffpointcounter ++;var e = document.getElementById( 'diffpoint' + diffpointcounter );if (e!=null){e.scrollIntoView();}else {diffpointcounter--}}if ( event.keyCode==38&&event.ctrlKey) {diffpointcounter --;var e = document.getElementById( 'diffpoint' + diffpointcounter );if (e!=null){e.scrollIntoView();}else {diffpointcounter++}}}function nextdiffpoint() {  diffpointcounter ++;  var e = document.getElementById( 'diffpoint_' + diffpointcounter );  if (e!=null) e.scrollIntoView();}document.body.onkeydown=bodykeydown;window.onload=doonload;</script>";
        return javascript + content.toString();
    }

    public boolean isClob(SDCProcessor sdcProcessor, SDIData sdiData, String columnName) {
        String type = sdcProcessor.getSDCColumnProperty(sdiData.getSdcid(), columnName, "datatype");
        return type.equals("T");
    }
}

