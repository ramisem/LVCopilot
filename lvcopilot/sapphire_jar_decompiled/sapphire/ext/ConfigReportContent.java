/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class ConfigReportContent {
    private StringBuffer content;
    private String applicationRoot;
    private String folder;
    private boolean frames = true;
    private PropertyList config;
    private boolean foundDiff = false;
    public DataSet diffInfo;
    public DataSet nodeInfo;
    private String context;
    private TranslationProcessor translationProcessor;
    private static final String COMMANDTYPE = "command";
    private static final String COMMANDTYPE_ACTION = "action";
    private static final String COMMANDTYPE_RETURNPROPERTY = "returnproperty";
    private static final String COMMANDTYPE_STARTACTIONBLOCK = "startactionblock";
    private static final String COMMANDTYPE_ENDACTIONBLOCK = "endactionblock";
    private static final String COMMANDTYPE_BLOCKPROPERTY = "blockproperty";
    private static final String COMMANDID = "commandid";
    private static final String LABEL = "commandlabel";
    private static final String NAME = "name";
    private static final String LEVEL = "level";
    private static final String TESTCONDITION = "testcondition";

    public ConfigReportContent(PropertyList config, String context) {
        this.context = context;
        this.content = new StringBuffer();
        if (config != null) {
            this.config = config;
            this.applicationRoot = config.getProperty("applicationroot", "");
            this.folder = config.getProperty("folder", "");
            this.frames = "Y".equals(config.getProperty("frames", "Y"));
        } else {
            config = new PropertyList();
            this.frames = true;
            this.applicationRoot = "";
            this.folder = "";
            this.config = config;
        }
        try {
            if (this.applicationRoot.length() > 0) {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Delete.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Delete.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Add.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Add.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Hand.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Hand.gif"));
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
        this.diffInfo = new DataSet();
        this.nodeInfo = new DataSet();
        String connectionid = config.getProperty("connection");
        if (connectionid != null && connectionid.length() > 0) {
            this.translationProcessor = new TranslationProcessor(connectionid);
        } else {
            Trace.log("Connection id is null");
        }
    }

    public ConfigReportContent(String context, TranslationProcessor translationProcessor) {
        this.context = context;
        this.content = new StringBuffer();
        this.diffInfo = new DataSet();
        this.nodeInfo = new DataSet();
        this.nodeInfo.addColumn("nodeid", 0);
        this.nodeInfo.addColumn("nodelabel", 0);
        this.nodeInfo.addColumn("status", 0);
        this.translationProcessor = translationProcessor;
    }

    public StringBuffer append(StringBuffer buffer) {
        if (buffer.indexOf("<diffpoint") > -1) {
            this.foundDiff = true;
        }
        this.content.append(buffer);
        return this.content;
    }

    public StringBuffer append(String string) {
        if (string.contains("<diffpoint")) {
            this.foundDiff = true;
        }
        this.content.append(string);
        return this.content;
    }

    public String toString() {
        return this.content.toString();
    }

    public int length() {
        return this.content.length();
    }

    public int indexOf(String pattern) {
        return this.content.indexOf(pattern);
    }

    public boolean getFoundDiff() {
        return this.foundDiff | this.content.indexOf("class=\"diffreport") > -1;
    }

    public void appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel) {
        this.foundDiff |= nodecontent.getFoundDiff();
        String status = nodecontent.getFoundDiff() ? "Modified" : "None";
        this.appendNodeContent(nodecontent, nodeId, nodelabel, status);
    }

    public void appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status) {
        this.appendNodeContent(nodecontent, nodeId, nodelabel, status, "div");
    }

    public void appendInnerNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status) {
        this.foundDiff |= nodecontent.getFoundDiff();
        this.appendNodeContent(nodecontent, nodeId, nodelabel, status, "");
    }

    public void appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status, String nodetag) {
        DataSet diffs = nodecontent.diffInfo;
        for (int i = 0; i < diffs.size(); ++i) {
            this.diffInfo.copyRow(diffs, i, 1);
        }
        int row = this.diffInfo.addRow();
        if (nodelabel == null || nodelabel.length() == 0) {
            nodelabel = nodeId;
        }
        this.diffInfo.setString(row, "nodelabel", nodelabel);
        this.diffInfo.setString(row, "nodeid", nodeId);
        this.diffInfo.setString(row, "status", status);
        if (nodetag.length() > 0) {
            String divstr = "\n<" + nodetag + " style=\"background-color:white;display:block\" id=\"" + nodeId + "\">" + nodecontent.toString() + "</" + nodetag + ">";
            this.content.append(divstr);
        } else {
            this.content.append(nodecontent);
        }
    }

    public void appendSubSection(ConfigReportContent subSectionContent, String subSection) {
        this.foundDiff |= subSectionContent.getFoundDiff();
        int row = this.diffInfo.addRow();
        this.diffInfo.setString(row, "SubSection", ConfigReportContent.generateSectionTitle(subSection));
        this.diffInfo.setString(row, "SubSectionURL", "#" + ConfigReportContent.convertToID(subSection));
        this.diffInfo.setString(row, "Status", subSectionContent.getFoundDiff() ? "Modified" : "None");
        this.content.append(subSectionContent.toString());
    }

    public void appendSpecialContent(ConfigReportContent content) {
        DataSet diffs = content.diffInfo;
        for (int i = 0; i < diffs.size(); ++i) {
            this.diffInfo.copyRow(diffs, i, 1);
        }
        this.foundDiff |= content.getFoundDiff();
        this.content.append(content.toString());
    }

    public DataSet getNodeInfo() {
        return this.diffInfo;
    }

    public void clearContent() {
        this.foundDiff = false;
        this.content = new StringBuffer();
        this.diffInfo = new DataSet();
        this.nodeInfo = new DataSet();
    }

    public static String getModifiedString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        orig = ConfigReportContent.removeStrangeChars(orig);
        String ret = "<diffpoint/><font class=\"diffreportmodifieditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static String getDeletedString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        orig = ConfigReportContent.removeStrangeChars(orig);
        String ret = "<diffpoint/><font class=\"diffreportdeleteditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static String getNewString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        orig = ConfigReportContent.removeStrangeChars(orig);
        String ret = "<diffpoint/><font class=\"diffreportnewitem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static DataSet removeAuditColumns(DataSet orig) {
        if (orig == null) {
            return orig;
        }
        DataSet clean = new DataSet();
        String[] cols = orig.getColumns();
        for (int i = 0; i < cols.length; ++i) {
            if (cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("usersequence") || cols[i].equals("tracelogid") || cols[i].equals("")) continue;
            String values = orig.getColumnValues(cols[i], "|!|");
            clean.addColumn(cols[i], orig.getColumnType(cols[i]));
            clean.addColumnValues(cols[i], orig.getColumnType(cols[i]), values, "|!|");
        }
        return clean;
    }

    public void startNewSubSection(String title, String desc) {
        String modTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        String subsectionId = ConfigReportContent.convertToID(title);
        String str = "<A name='" + subsectionId + "'></A>";
        str = str + "<H3 id=\"" + subsectionId + "\">" + ConfigReportContent.getNewString(modTitle) + "</H3>\n";
        str = str + "<P>\n";
        str = str + ConfigReportContent.getNewString(desc);
        str = str + "</P>\n";
        this.content.append(str);
    }

    public void startDeletedSubSection(String title, String desc) {
        String modTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        String subsectionId = ConfigReportContent.convertToID(title);
        String str = "<A name='" + subsectionId + "'></A>";
        str = str + "<H3 id=\"" + subsectionId + "\">" + ConfigReportContent.getDeletedString(modTitle) + "</H3>\n";
        str = str + "<P>\n";
        str = str + ConfigReportContent.getDeletedString(desc);
        str = str + "</P>\n";
        this.content.append(str);
    }

    public void startSubSection(String title, String desc) {
        String modTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        String subsectionId = ConfigReportContent.convertToID(title);
        String str = "<A name='" + subsectionId + "'></A>";
        str = str + "<H3 id=\"" + subsectionId + "\">" + modTitle + "</H3>\n";
        str = str + "<P>\n";
        str = str + desc;
        str = str + "</P>\n";
        this.content.append(str);
    }

    public void endSubSection(String title, String desc) {
        String str = "";
        this.content.append(str);
    }

    public void startTable() {
        this.content.append(this.getStartTableTop());
    }

    public void startTableInner() {
        this.content.append(this.getStartTableInner());
    }

    public void endTable() {
        this.content.append(this.getEndTable());
    }

    public StringBuffer addDiffRowItem(String columnName, String columnVal, String refVal, TranslationProcessor translationProcessor) {
        return this.addDiffRowItem(columnName, columnVal, refVal, 1, false, translationProcessor, false);
    }

    public StringBuffer addDiffRowItem(String columnName, String columnVal, String refVal, boolean ignoreDiff, TranslationProcessor translationProcessor) {
        return this.addDiffRowItem(columnName, columnVal, refVal, 1, ignoreDiff, translationProcessor, false);
    }

    public StringBuffer addDiffRowItem(String columnName, String columnVal, String refVal, int colspan, TranslationProcessor translationProcessor) {
        return this.addDiffRowItem(columnName, columnVal, refVal, colspan, false, translationProcessor, false);
    }

    public StringBuffer addDiffRowItem(String columnName, String columnVal, String refVal, int colspan, boolean ignoreDiff, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        return this.addDiffRowItem(columnName, "viewlhs", "viewrhs", columnVal, refVal, colspan, ignoreDiff, translationProcessor, hideEmptyColumns);
    }

    public StringBuffer addDiffRowItem(String columnName, String lhsClass, String rhsClass, String columnVal, String refVal, int colspan, boolean ignoreDiff, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        if (ignoreDiff) {
            if (hideEmptyColumns) {
                if (columnVal.length() > 0) {
                    return this.addRowItem(columnName, columnVal, colspan);
                }
                return new StringBuffer("");
            }
            return this.addRowItem(columnName, columnVal, colspan);
        }
        if (columnVal == null) {
            columnVal = "";
        }
        if (refVal == null) {
            refVal = "";
        }
        columnVal = ConfigReportContent.removeStrangeChars(columnVal);
        refVal = ConfigReportContent.removeStrangeChars(refVal);
        if (hideEmptyColumns) {
            if (columnVal.length() > 0 || refVal.length() > 0) {
                String diffVal = this.getFormattedDiffVal(columnName, columnVal, refVal, false, translationProcessor);
                return this.addRowItem(columnName, diffVal, colspan);
            }
            return new StringBuffer("");
        }
        String diffVal = this.getFormattedDiffVal(columnName, columnVal, refVal, false, translationProcessor);
        return this.addRowItem(columnName, diffVal, colspan);
    }

    public void renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        this.renderDiffListTable(ds, ref, keycols, true, translationProcessor, hideEmptyColumns);
    }

    public void renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            if (top) {
                this.startListTableTop();
            } else {
                this.startListTableInner();
            }
            if (ref == null) {
                this.addListItems(ds, translationProcessor, "New", hideEmptyColumns);
            } else {
                this.addDiffListItems(ds, ref, keycols, translationProcessor, hideEmptyColumns);
            }
            this.endListTable();
        } else if (ref != null && ref.getRowCount() > 0 && !this.checkIfEmpty(ref)) {
            if (top) {
                this.startListTableTop();
            } else {
                this.startListTableInner();
            }
            this.addDiffListItems(ds, ref, keycols, translationProcessor, hideEmptyColumns);
            this.endListTable();
        }
    }

    public void renderListTable(DataSet ds, TranslationProcessor translationProcessor) {
        this.renderListTable(ds, true, translationProcessor, "", false);
    }

    public void renderDetailTable(HashMap<String, String> columnTitleMap, String detailtable, String tablelabel, String itemdisplay, DataSet src, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) throws SapphireException {
        this.renderDetailTablesDiff(columnTitleMap, detailtable, tablelabel, itemdisplay, src, null, keycols, translationProcessor, hideEmptyColumns);
    }

    public void renderDetailTablesDiff(HashMap<String, String> columnTitleMap, String detailtable, String tablelabel, String itemdisplay, DataSet src, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) throws SapphireException {
        if (src.getRowCount() > 0 && !this.checkIfEmpty(src) || ref.getRowCount() > 0 && !this.checkIfEmpty(ref)) {
            this.startListTableTop();
            if (ref == null) {
                this.appendSpecialContent(this.addDetailListItems(columnTitleMap, detailtable, itemdisplay, src, keycols, translationProcessor, hideEmptyColumns));
            } else {
                this.appendSpecialContent(this.addDiffDetailListItems(columnTitleMap, detailtable, itemdisplay, src, ref, keycols, translationProcessor, hideEmptyColumns));
            }
            this.endListTable();
        } else if (ref.getRowCount() > 0 && !this.checkIfEmpty(ref)) {
            this.startSubSection(tablelabel, "There are " + ref.getRowCount() + " items. ");
            this.startListTableTop();
            if (ref == null) {
                this.appendSpecialContent(this.addDetailListItems(columnTitleMap, detailtable, itemdisplay, src, keycols, translationProcessor, hideEmptyColumns));
            } else {
                this.appendSpecialContent(this.addDiffDetailListItems(columnTitleMap, detailtable, itemdisplay, src, ref, keycols, translationProcessor, hideEmptyColumns));
            }
            this.endListTable();
        }
    }

    public ConfigReportContent renderPropertyListDiff(PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        return this.renderPropertyListDiff("", false, false, pl, refPl, defList, reportAdvancedProperties, top, translationProcessor, hideEmptyColumns);
    }

    public ConfigReportContent renderOverridePropertyListDiff(String basenode, PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, boolean hideInheritedProperties, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        return this.renderPropertyListDiff(basenode, true, hideInheritedProperties, pl, refPl, defList, reportAdvancedProperties, top, translationProcessor, hideEmptyColumns);
    }

    public ConfigReportContent renderPropertyListAttributesDiff(PropertyList pl, PropertyList refPl) {
        HashMap refAttributes;
        HashMap srcAttributes = pl == null ? new HashMap() : pl.getAttributes();
        HashMap hashMap = refAttributes = refPl == null ? new HashMap() : refPl.getAttributes();
        if (srcAttributes == null) {
            srcAttributes = new HashMap();
        }
        if (refAttributes == null) {
            refAttributes = new HashMap();
        }
        ConfigReportContent ret = new ConfigReportContent("Attributes", this.translationProcessor);
        if (srcAttributes != null && srcAttributes.containsKey("rolelist") || refAttributes != null && refAttributes.containsKey("rolelist")) {
            String srcRoles = (String)srcAttributes.get("rolelist");
            String refRoles = (String)refAttributes.get("rolelist");
            ret.append("<P>Roles: " + ConfigReportContent.getDiffString(srcRoles, refRoles));
        }
        return ret;
    }

    public ConfigReportContent renderPropertyListDiff(String basenode, boolean highlightoverride, boolean hideInheritedProperties, PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        if (defList == null || defList.size() == 0) {
            return this.renderPropertyListDiff(pl, refPl, top, translationProcessor);
        }
        if (refPl == null) {
            refPl = new PropertyList();
        }
        ConfigReportContent tempTable = new ConfigReportContent(this.context + " PropertyList ", translationProcessor);
        if (pl.size() == 0 && refPl.size() == 0) {
            return tempTable;
        }
        ConfigReportContent attrContent = this.renderPropertyListAttributesDiff(pl, refPl);
        for (PropertyDefinition propDef : defList) {
            String refp;
            PropertyDefinitionList currPropDefList;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isPassword = false;
            if (propDef.getAttributes() != null && propDef.getAttributes().get("password") != null) {
                isPassword = "Y".equals(propDef.getAttributes().get("password"));
            }
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (pl.isPropertyList(currPropId)) {
                PropertyList refCld;
                currPropDefList = propDef.getPropertyDefinitionList();
                PropertyList cld = pl.getPropertyList(currPropId);
                ConfigReportContent propertylist = this.renderPropertyListDiff(basenode, highlightoverride, hideInheritedProperties, cld, refCld = refPl.getPropertyList(currPropId), currPropDefList, reportAdvancedProperties, false, translationProcessor, hideEmptyColumns);
                if (propertylist.length() <= 0) continue;
                tempTable.startRow();
                if (translationProcessor != null) {
                    currPropTitle = translationProcessor.translate(currPropTitle);
                }
                tempTable.addRowItem(currPropTitle, propertylist.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (pl.isCollection(currPropId)) {
                currPropDefList = propDef.getPropertyDefinitionList();
                ConfigReportContent collection = this.renderCollectionDiff(basenode, highlightoverride, hideInheritedProperties, pl.getCollection(currPropId), refPl.getCollection(currPropId), currPropDefList, reportAdvancedProperties, false, translationProcessor, hideEmptyColumns);
                if (collection.length() <= 0) continue;
                tempTable.startRow();
                if (translationProcessor != null) {
                    currPropTitle = translationProcessor.translate(currPropTitle);
                }
                tempTable.addRowItem(currPropTitle, collection.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (currPropId.startsWith("__")) continue;
            PropertyValue propertyValue = pl.getPropertyValue(currPropId);
            PropertyValue refpropertyValue = refPl.getPropertyValue(currPropId);
            boolean isOverridingProperty = false;
            if (highlightoverride && (basenode.equals(propertyValue.getPropertyTreeNodeId()) || basenode.equals(refpropertyValue.getPropertyTreeNodeId()))) {
                isOverridingProperty = true;
            }
            if (basenode.equals("__root")) {
                isOverridingProperty = true;
            }
            String p = isPassword ? "*****" : pl.getProperty(currPropId, "");
            String string = refp = isPassword ? "*****" : refPl.getProperty(currPropId, "");
            if (p.equals("[]") || p.equals("[PropertyList]")) {
                p = "";
            }
            if (refp.equals("[]") || refp.equals("[PropertyList]")) {
                refp = "";
            }
            if (p.length() <= 0 && refp.length() <= 0) continue;
            String diffVal = this.getFormattedDiffVal(currPropId, p, refp, top, translationProcessor);
            if (highlightoverride && !isOverridingProperty) {
                if (hideInheritedProperties) continue;
                tempTable.startRow();
                p = HttpUtil.htmlEncode(p);
                tempTable.addRowItemEmphasis(currPropTitle, p, translationProcessor);
                tempTable.endRow();
                continue;
            }
            tempTable.startRow();
            tempTable.addRowItem(currPropTitle, diffVal, translationProcessor);
            tempTable.endRow();
        }
        ConfigReportContent table = new ConfigReportContent(this.context + " PropertyList ", translationProcessor);
        if (!highlightoverride || tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            if (attrContent.length() > 0) {
                table.append(attrContent.toString());
            }
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public ConfigReportContent renderPropertyListDiff(PropertyList pl, PropertyList refPl, boolean top, TranslationProcessor translationProcessor) {
        ConfigReportContent temp = new ConfigReportContent(this.context + " PropertyList", translationProcessor);
        if (refPl == null) {
            refPl = new PropertyList();
        }
        if (pl.size() == 0) {
            return temp;
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            String p1;
            String currlabel;
            if (pl.isPropertyList(keyes[i].toString())) {
                PropertyList childRefPl = refPl.getPropertyList(keyes[i].toString());
                String propertylist = this.renderPropertyListDiff(pl.getPropertyList(keyes[i].toString()), childRefPl, false, translationProcessor).toString();
                if (propertylist.length() <= 0) continue;
                temp.startRow();
                currlabel = keyes[i].toString();
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
                temp.addRowItem(currlabel, propertylist, translationProcessor);
                temp.endRow();
                continue;
            }
            if (pl.isCollection(keyes[i].toString())) {
                PropertyListCollection childRefCollection = refPl.getCollectionNotNull(keyes[i].toString());
                String collection = this.renderCollectionDiff(pl.getCollection(keyes[i].toString()), childRefCollection, false, translationProcessor, false).toString();
                if (collection.length() <= 0) continue;
                temp.startRow();
                currlabel = keyes[i].toString();
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
                temp.addRowItem(currlabel, collection, translationProcessor);
                temp.endRow();
                continue;
            }
            if (keyes[i].toString().startsWith("__")) continue;
            if (refPl != null) {
                p1 = pl.getProperty(keyes[i].toString(), "");
                String p2 = refPl.getProperty(keyes[i].toString(), "");
                if (p1.equals("[]") || p1.equals("[PropertyList]")) {
                    p1 = "";
                }
                if (p2.equals("[]") || p2.equals("[PropertyList]")) {
                    p2 = "";
                }
                if (p1.length() <= 0 && p2.length() <= 0) continue;
                temp.startRow();
                currlabel = keyes[i].toString();
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
                temp.addDiffRowItem(currlabel, p1, p2, translationProcessor);
                temp.endRow();
                continue;
            }
            p1 = pl.getProperty(keyes[i].toString(), "");
            if (p1.equals("[]") || p1.equals("[PropertyList]")) {
                p1 = "";
            }
            if (p1.length() <= 0) continue;
            temp.startRow();
            String currlabel2 = keyes[i].toString();
            if (translationProcessor != null) {
                currlabel2 = translationProcessor.translate(currlabel2);
            }
            temp.addRowItem(currlabel2, p1, translationProcessor);
            temp.endRow();
        }
        ConfigReportContent table = new ConfigReportContent(this.context + " PropertyList", translationProcessor);
        if (temp.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(temp.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public boolean hasDiffDetailTables(DataSet src, DataSet ref, String[] keycols) {
        if (ref == null) {
            return true;
        }
        if (src.getRowCount() != ref.getRowCount()) {
            return true;
        }
        if (keycols == null) {
            return false;
        }
        src = this.addDiffInfo(src, ref, keycols);
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("_status", "None");
        DataSet unchanged = src.getFilteredDataSet(filter);
        return unchanged.getRowCount() != src.getRowCount();
    }

    public static String createHyperLink(String source, String ref) {
        return "<A HREF=\"#" + ConfigReportContent.generateSectionAnchor(ref) + "\">" + source + "</A>";
    }

    public static String generateSDISectionAnchor(SDI currentSDI) {
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

    public static String removeIllegalChars(String input) {
        StringBuffer outputString = new StringBuffer(input.length());
        boolean count = true;
        for (int i = 0; i < input.length(); ++i) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(input.charAt(i));
            if (input.charAt(i) == '/' || input.charAt(i) == ',' || input.charAt(i) == '\\' || input.charAt(i) == '%' || input.charAt(i) == ':' || input.charAt(i) == '*' || input.charAt(i) == '?' || input.charAt(i) == '<' || input.charAt(i) == '>' || input.charAt(i) == '|' || !block.equals(Character.UnicodeBlock.BASIC_LATIN)) continue;
            outputString.append(input.charAt(i));
        }
        if (outputString.toString().toLowerCase().contains("<script>")) {
            outputString = new StringBuffer(HttpUtil.htmlEncode(outputString.toString()));
        }
        return outputString.toString();
    }

    public void startSDISection(SDI currentSDI, String desc) {
        String sectionTitle = ConfigReportContent.generateSDISectionTitle(currentSDI);
        this.startSDISection(currentSDI, sectionTitle, desc);
    }

    public void startSDISectionDiff(SDI currentSDI, String desc, String refDesc) {
        String sectionTitle = ConfigReportContent.generateSDISectionTitle(currentSDI);
        this.startSDISectionDiff(currentSDI, sectionTitle, desc, refDesc);
    }

    public void startSDISectionDiff(SDCProcessor sdcProcessor, SDIData sdiData, SDI currentSDI, String desc, String refDesc) {
        String sectionTitle = this.getFormattedItemLabel(sdiData, currentSDI, this.getSDITableLabelInfo(sdcProcessor, currentSDI.getSdcid())[1]);
        this.startSDISectionDiff(currentSDI, sectionTitle, desc, refDesc);
    }

    protected String[] getSDITableLabelInfo(SDCProcessor sdcProcessor, String sdcid) {
        HashMap propertyList = sdcProcessor.getSDCProperties(sdcid);
        String[] labelinfo = new String[]{(String)propertyList.get("tablelabel"), (String)propertyList.get("itemdisplay")};
        Trace.logDebug("LinkLabelinfo for " + sdcid + " tablelabel:" + labelinfo[0] + " itemdisplay:" + labelinfo[1]);
        return labelinfo;
    }

    public String getFormattedItemLabel(SDIData sdiData, SDI sdi, String labelformat) {
        if (labelformat == null || labelformat.length() == 0) {
            return sdi.toString();
        }
        DataSet primary = sdiData.getDataset("primary");
        return this.getFormattedItemLabel(primary, labelformat);
    }

    protected static SDI getSDI(SDIData sdiData) {
        String sdcid = sdiData.getSdcid();
        String[] keyes = sdiData.getKeys("primary");
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, keyes[0]);
        String keyid2 = keyes.length > 1 ? primary.getValue(0, keyes[1]) : "";
        String keyid3 = keyes.length > 2 ? primary.getValue(0, keyes[2]) : "";
        return new SDI(sdcid, keyid1, keyid2, keyid3);
    }

    private String getFormattedItemLabel(DataSet dataSet, String labelformat) {
        String[] colnames = dataSet.getColumns();
        for (int i = 0; i < colnames.length; ++i) {
            if (!labelformat.contains(colnames[i])) continue;
            labelformat = labelformat.replace('[' + colnames[i] + ']', dataSet.getValue(0, colnames[i]));
        }
        return labelformat;
    }

    public void startSection(String sectionIdentifier) {
        String anchor = ConfigReportContent.generateSectionAnchor(sectionIdentifier);
        String sectionTitle = ConfigReportContent.generateSectionTitle(sectionIdentifier);
        String str = "<H2 class='sdiheader' id=\"" + anchor + "\">" + sectionTitle + "</H2>\n";
        str = str + "<P>\n";
        str = str + "</P>\n";
        this.content.append(str);
    }

    public void endSection() {
        this.insertDiffAnchors();
        String str = "<P>";
        this.content.append(str);
    }

    public static String generateSectionTitle(String layout) {
        return layout.substring(0, 1).toUpperCase() + layout.substring(1);
    }

    public void startSDISection(SDI currentSDI, String sectionTitle, String desc) {
        String anchor = ConfigReportContent.generateSDISectionAnchor(currentSDI);
        String str = "<H2 class='sdiheader' id=\"" + anchor + "\">" + sectionTitle + "</H2>";
        ConfigReportContent descCont = new ConfigReportContent("Description", this.translationProcessor);
        if (desc != null && desc.length() > 0) {
            descCont.startSubSection("Description", desc);
            str = str + descCont.toString();
        }
        this.content.append(str);
    }

    public void startSDISectionDiff(SDI currentSDI, String sectionTitle, String desc, String refDesc) {
        String anchor = ConfigReportContent.generateSDISectionAnchor(currentSDI);
        String str = "<H2 class='sdiheader' id=\"" + anchor + "\">" + sectionTitle + "</H2>";
        ConfigReportContent descCont = new ConfigReportContent("Description", this.translationProcessor);
        if (desc != null && desc.length() > 0) {
            descCont.startSubSection("Description", desc);
            str = str + descCont.toString();
        }
        this.content.append(str);
    }

    public static String generateSDISectionTitle(SDI currentSDI) {
        if (currentSDI != null) {
            String sdcid = currentSDI.getSdcid();
            String keyid1 = currentSDI.getKeyid1();
            String keyid2 = currentSDI.getKeyid2();
            String keyid3 = currentSDI.getKeyid3();
            String sectionTitle = keyid1;
            if (keyid2 != null && keyid2.length() > 0 && !"(null)".equals(keyid2)) {
                sectionTitle = sectionTitle + ", " + keyid2;
            }
            if (keyid3 != null && keyid3.length() > 0 && !"(null)".equals(keyid3)) {
                sectionTitle = sectionTitle + ", " + keyid3;
            }
            return sectionTitle;
        }
        return "null";
    }

    public static void copyFile(File in, File out) throws Exception {
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

    public static boolean hasPropertyListChanged(PropertyList pl, PropertyList refPl) {
        if (pl.size() != refPl.size()) {
            return true;
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            if (pl.isPropertyList(keyes[i].toString())) {
                PropertyList childRefPl = refPl.getPropertyList(keyes[i].toString());
                if (!ConfigReportContent.hasPropertyListChanged(pl.getPropertyList(keyes[i].toString()), childRefPl)) continue;
                return true;
            }
            if (pl.isCollection(keyes[i].toString())) {
                PropertyListCollection childRefCollection = refPl.getCollectionNotNull(keyes[i].toString());
                if (!ConfigReportContent.hasCollectionChanged(pl.getCollection(keyes[i].toString()), childRefCollection)) continue;
                return true;
            }
            String p1 = pl.getProperty(keyes[i].toString(), "");
            String p2 = refPl.getProperty(keyes[i].toString(), "");
            if (p1.length() != p2.length()) {
                return true;
            }
            if (!ConfigReportContent.hasPropertyChanged(p1, p2)) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCollectionChanged(PropertyListCollection src, PropertyListCollection ref) {
        if (src.size() != ref.size()) {
            return true;
        }
        for (int i = 0; i < src.size(); ++i) {
            PropertyList pl = src.getPropertyList(i);
            PropertyList refPl = ConfigReportContent.find(i, src, ref);
            if (refPl == null) {
                return true;
            }
            if (!ConfigReportContent.hasPropertyListChanged(pl, refPl)) continue;
            return true;
        }
        return false;
    }

    public static boolean hasPropertyChanged(String val1, String val2) {
        return !val1.trim().equals(val2.trim());
    }

    public String getStartTableTop() {
        return "<TABLE class=\"view\" >\n";
    }

    public String getStartTableInner() {
        return "<TABLE width=\"100%\" class=\"viewinner\" >\n";
    }

    public String getEndTable() {
        return "</TABLE>\n";
    }

    public void startRow() {
        this.content.append("<TR VALIGN=TOP >\n");
    }

    public void endRow() {
        this.content.append("</TR>\n");
    }

    public StringBuffer addRowItemCheckbox(String columnName, String columnVal) {
        String colTitle = "";
        if (columnName.length() > 0) {
            colTitle = columnName.substring(0, 1).toUpperCase();
            if (columnName.length() > 1) {
                colTitle = colTitle + columnName.substring(1);
            }
        }
        this.content.append("<TD class=\"viewlhs\" >\n");
        this.content.append(colTitle);
        this.content.append("</TD>\n");
        this.content.append("<TD class=\"viewrhs\" colspan = \"1\">\n");
        if (columnVal.equals("Y")) {
            this.content.append("<input type=\"checkbox\" value=Y  checked></input>");
        } else {
            this.content.append("<input type=\"checkbox\" value=N \"></input>");
        }
        this.content.append("</TD>\n");
        return this.content;
    }

    public StringBuffer addDiffRowItemCheckbox(String columnName, String columnVal, String refVal) {
        if (columnVal == null) {
            columnVal = "";
        }
        if (refVal == null) {
            refVal = "";
        }
        if ((columnVal = ConfigReportContent.removeStrangeChars(columnVal)).equals(refVal = ConfigReportContent.removeStrangeChars(refVal))) {
            return this.addRowItemCheckbox(columnName, columnVal);
        }
        if (columnVal.equals("Y")) {
            return this.addRowItem(columnName, ConfigReportContent.changeHTMLtags("<img src=\"WEB-CORE/imageref/basic_application_icons/status_and_signs/16/checkbox.png\"/>"), false);
        }
        return this.addRowItem(columnName, ConfigReportContent.changeHTMLtags("<img src=\"WEB-CORE/imageref/basic_application_icons/status_and_signs/16/checkbox_unchecked.png\" style=\"border:5px solid red\"/>"), false);
    }

    public StringBuffer addRowItem(String columnName, String columnVal, int colspan) {
        String colTitle = "";
        if (columnName.length() > 0) {
            colTitle = columnName.substring(0, 1).toUpperCase();
            if (columnName.length() > 1) {
                colTitle = colTitle + columnName.substring(1);
            }
        }
        this.content.append("<TD class=\"viewlhs\" >\n");
        this.content.append(colTitle);
        this.content.append("</TD>\n");
        this.content.append("<TD class=\"viewrhs\" colspan = \" " + colspan + "\">\n");
        if (columnVal.indexOf("<xml") > -1 || columnVal.indexOf("<?xml") > -1 || columnVal.startsWith("<propertytree")) {
            columnVal = HttpUtil.htmlEncode(columnVal);
        }
        if ((columnVal = ConfigReportContent.removeStrangeChars(columnVal)).equals("actionblock")) {
            this.content.append("Action Block");
        } else {
            this.content.append(columnVal);
        }
        this.content.append("</TD>\n");
        return this.content;
    }

    public StringBuffer addRowItem(String columnName, String columnVal, boolean icon) {
        String colTitle = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
        this.content.append("<TD class=\"viewlhs\" >\n");
        this.content.append(colTitle);
        this.content.append("</TD>\n");
        if (!icon) {
            this.content.append("<TD class=\"viewrhs\" >\n");
        } else {
            this.content.append("<TD align=CENTER>\n");
        }
        columnVal = ConfigReportContent.removeStrangeChars(columnVal);
        if (columnVal.equals("actionblock")) {
            this.content.append("Action Block");
        } else {
            this.content.append(columnVal);
        }
        this.content.append("</TD>\n");
        return this.content;
    }

    public StringBuffer addRowItem(String columnName, String columnVal) {
        return this.addRowItem(columnName, columnVal, "viewlhs", "viewrhs", this.translationProcessor);
    }

    public StringBuffer addRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        return this.addRowItem(columnName, columnVal, "viewlhs", "viewrhs", translationProcessor);
    }

    public StringBuffer addRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        columnVal = "{|" + columnVal + "|}";
        return this.addRowItem(columnName, columnVal, "viewlhsem", "viewrhsem", translationProcessor);
    }

    public StringBuffer addDiffRowItemEmphasis(String columnName, String oldVal, String newVal, TranslationProcessor translationProcessor) {
        oldVal = "{|" + oldVal + "|}";
        newVal = "{|" + newVal + "|}";
        return this.addDiffRowItem(columnName, "viewlhsem", "viewrhsem", oldVal, newVal, 1, false, translationProcessor, true);
    }

    public StringBuffer addRowItem(String columnName, String columnVal, String lhsClass, String rhsClass, TranslationProcessor translationProcessor) {
        String colTitle = columnName;
        if (columnName.length() > 1) {
            colTitle = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
        }
        if (columnVal == null) {
            columnVal = "";
        }
        this.content.append("<TD class=\"" + lhsClass + "\" >\n");
        if (lhsClass.equals("diffreportnewlhs")) {
            colTitle = ConfigReportContent.getNewString(colTitle);
        } else if (lhsClass.equals("diffreportdeletedlhs")) {
            colTitle = ConfigReportContent.getDeletedString(colTitle);
        } else if (lhsClass.equals("diffreportmodifiedlhs")) {
            colTitle = ConfigReportContent.getModifiedString(colTitle);
        }
        this.content.append(colTitle);
        this.content.append("</TD>\n");
        this.content.append("<TD class=\"" + rhsClass + "\" >\n");
        if (rhsClass.equals("diffreportnewrhs")) {
            this.content.append(ConfigReportContent.getNewString(this.getFormattedValue(columnName, columnVal)));
        } else if (rhsClass.equals("diffreportdeletedrhs")) {
            this.content.append(ConfigReportContent.getDeletedString(this.getFormattedValue(columnName, columnVal)));
        } else if (rhsClass.equals("diffreportmodifiedrhs")) {
            this.content.append(ConfigReportContent.getModifiedString(this.getFormattedValue(columnName, columnVal)));
        } else {
            this.content.append(this.getFormattedValue(columnName, columnVal));
        }
        this.content.append("</TD>\n");
        return this.content;
    }

    public String getFormattedValue(String columnName, String columnVal) {
        if (this.isValidDataSet(columnVal)) {
            try {
                DataSet temp = new DataSet(columnVal);
                columnVal = temp.toHTML();
            }
            catch (Exception e) {
                Trace.logError("Failed to add row item", e);
            }
        } else if (this.isValidActionBlock(columnVal)) {
            DataSet ab = new DataSet();
            try {
                ActionBlock actionBlock = new ActionBlock(columnVal);
                ab = this.addActionBlockItems(ab, actionBlock, 0, "1", "");
                columnVal = this.renderActionBlockDiagram(ab, this.applicationRoot != null && this.applicationRoot.length() > 0).toString();
            }
            catch (SapphireException e) {
                Trace.log("ActionBlock rendering failed:" + e.getMessage());
            }
        } else if (columnVal.startsWith("<?xml") || columnVal.startsWith("<xml") || columnVal.contains("<propertytree>")) {
            columnVal = HttpUtil.htmlEncode(columnVal);
        } else if (columnName.equalsIgnoreCase("Groovy") || columnVal.startsWith("$G")) {
            columnVal = columnVal.substring(3, columnVal.length() - 1).replaceAll("\\n", "<P>").replaceAll(" ", "&nbsp;");
        }
        columnVal = ConfigReportContent.removeStrangeChars(columnVal);
        return columnVal;
    }

    public String getFormattedDiffVal(String columnName, String columnVal, String refVal, boolean top, TranslationProcessor translationProcessor) {
        String diffVal;
        block31: {
            diffVal = "";
            if (columnVal.startsWith("<dataset") || refVal.startsWith("<dataset")) {
                DataSet src = new DataSet(columnVal);
                DataSet ref = new DataSet(refVal);
                ConfigReportContent diff = new ConfigReportContent(this.context + " columname:" + columnName, translationProcessor);
                diff.renderDiffListTable(src, ref, src.getColumns(), translationProcessor, true);
                diffVal = diff.toString();
            } else if (this.isPropertyList(columnVal) || this.isPropertyList(refVal)) {
                try {
                    PropertyList src = new PropertyList(columnVal);
                    src.setPropertyList(columnVal);
                    PropertyList ref = new PropertyList(refVal);
                    ref.setPropertyList(refVal);
                    if (src.isEmpty() && ref.isEmpty()) {
                        diffVal = "<P>No properties";
                        break block31;
                    }
                    ConfigReportContent diff = this.renderPropertyListDiff(src, ref, top, translationProcessor);
                    diffVal = diff.toString();
                }
                catch (SapphireException e) {
                    diffVal = ConfigReportContent.getDiffString(columnVal, refVal);
                }
            } else if (this.isValidActionBlock(columnVal) || this.isValidActionBlock(refVal)) {
                try {
                    ActionBlock actionBlock = new ActionBlock(columnVal);
                    DataSet ab = new DataSet();
                    ab = this.addActionBlockItems(ab, actionBlock, 0, "1", "");
                    ActionBlock refActionBlock = null;
                    if (refVal.length() > 0) {
                        refActionBlock = new ActionBlock(refVal);
                    }
                    ConfigReportContent source = this.renderActionBlockDiagram(ab, this.applicationRoot != null && this.applicationRoot.length() > 0);
                    ConfigReportContent ref = new ConfigReportContent("ref", translationProcessor);
                    if (refVal.length() > 0) {
                        DataSet refab = new DataSet();
                        refab = this.addActionBlockItems(refab, refActionBlock, 0, "1", "");
                        ref = this.renderActionBlockDiagram(refab, this.applicationRoot != null && this.applicationRoot.length() > 0);
                    }
                    if (source.toString().equals(ref.toString())) {
                        diffVal = source.toString();
                        break block31;
                    }
                    ConfigReportContent content = new ConfigReportContent("", translationProcessor);
                    content.append(ConfigReportContent.getNewString("New:") + "<P>" + source);
                    content.append(ConfigReportContent.getDeletedString("Old:") + "<P>" + ref);
                    diffVal = content.toString();
                }
                catch (SapphireException e) {
                    Trace.log("Failed to do actionblock diff:" + e.getMessage());
                }
            } else if (columnVal.startsWith("{") && columnVal.endsWith("}")) {
                try {
                    PropertyList src = new PropertyList();
                    src.setJSONString(columnVal);
                    PropertyList ref = new PropertyList();
                    if (refVal.length() > 0) {
                        ref.setJSONString(refVal);
                    }
                    ConfigReportContent diff = this.renderPropertyListDiff(src, ref, top, translationProcessor);
                    diffVal = diff.toString();
                }
                catch (JSONException e) {
                    diffVal = ConfigReportContent.getDiffString(columnVal, refVal);
                }
            } else if (columnVal.startsWith("<html") || refVal.startsWith("<html")) {
                ConfigReportContent diffhtmls = new ConfigReportContent("diff html", translationProcessor);
                diffhtmls.startSubHeading(ConfigReportContent.getNewString("New"), "");
                diffhtmls.append("<P>");
                diffhtmls.append(columnVal);
                if (refVal.length() > 0) {
                    diffhtmls.startSubHeading(ConfigReportContent.getDeletedString("Old"), "");
                    diffhtmls.append("<P>");
                    diffhtmls.append(refVal);
                }
                diffVal = diffhtmls.toString();
            } else if (this.isPropertyTree(columnVal) || this.isPropertyTree(refVal)) {
                try {
                    PropertyTree src = new PropertyTree(columnVal);
                    src.setValueXML(columnVal);
                    PropertyTree ref = new PropertyTree(refVal);
                    ref.setValueXML(refVal);
                    diffVal = ConfigReportContent.getDiffString(src.toXMLString(), ref.toXMLString());
                }
                catch (SapphireException e) {
                    diffVal = ConfigReportContent.getDiffString(columnVal, refVal);
                }
            } else if (columnName.contains("thumbnailimage")) {
                diffVal = ConfigReportContent.getThumbnailDiff(columnVal, refVal, translationProcessor);
            } else if (columnName.contains("thumbnailhtml")) {
                ConfigReportContent diffhtmls = new ConfigReportContent("diff html", translationProcessor);
                diffhtmls.startSubHeading(ConfigReportContent.getNewString("New"), "");
                diffhtmls.append("<P>");
                diffhtmls.append(columnVal);
                if (refVal.length() > 0) {
                    diffhtmls.startSubHeading(ConfigReportContent.getDeletedString("Old"), "");
                    diffhtmls.append("<P>");
                    diffhtmls.append(refVal);
                }
                diffVal = diffhtmls.toString();
            } else {
                diffVal = ConfigReportContent.getDiffString(columnVal, refVal);
            }
        }
        return diffVal;
    }

    public StringBuffer addNewRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        this.foundDiff = true;
        return this.addRowItem(columnName, columnVal, "diffreportnewlhs", "diffreportnewrhs", translationProcessor);
    }

    public StringBuffer addNewRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        this.foundDiff = true;
        return this.addRowItem(columnName, columnVal, "diffreportnewlhsem", "diffreportnewrhsem", translationProcessor);
    }

    public StringBuffer addDeletedRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        this.foundDiff = true;
        return this.addRowItem(columnName, columnVal, "diffreportdeletedlhs", "diffreportdeletedrhs", translationProcessor);
    }

    public StringBuffer addDeletedRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor) {
        this.foundDiff = true;
        return this.addRowItem(columnName, columnVal, "diffreportdeletedlhsem", "diffreportdeletedrhsem", translationProcessor);
    }

    public void startSubHeading(String title, String desc) {
        String str = "";
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        str = str + "<H4>" + title + "</H4><P>";
        str = str + desc;
        str = str + "<P>";
        this.content.append(str);
    }

    public void startSubHeading(String title, String desc, String anchor) {
        String str = "";
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        str = str + "<H4 id=\"" + anchor + "\">" + title + "</H4><P>";
        str = str + desc;
        str = str + "<P>";
        this.content.append(str);
    }

    public void renderListTable(DataSet ds, boolean top, TranslationProcessor translationProcessor, String status, boolean hideEmptyColumns) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            if (top) {
                this.startListTableTop();
            } else {
                this.startListTableInner();
            }
            ds.sort("refid");
            this.addListItems(ds, translationProcessor, status, hideEmptyColumns);
            this.endListTable();
        }
    }

    public void startListTableTop() {
        this.content.append("<TABLE class=\"viewlist\" >\n");
    }

    public void startListTableInner() {
        this.content.append("<TABLE width=\"100%\" class=\"viewlistinner\" >\n");
    }

    protected boolean checkIfEmpty(DataSet ds) {
        String[] columns = ds.getColumns();
        for (int row = 0; row < ds.getRowCount(); ++row) {
            for (int column = 0; column < columns.length; ++column) {
                if (ds.getValue(row, columns[column]) == null || ds.getValue(row, columns[column]).length() <= 0) continue;
                return false;
            }
        }
        return true;
    }

    protected boolean isValidActionBlock(String xml) {
        try {
            if (xml == null || xml.length() == 0) {
                return false;
            }
            if (!xml.contains("<actionblock")) {
                return false;
            }
            ActionBlock actionBlock = new ActionBlock(xml);
        }
        catch (SapphireException e) {
            return false;
        }
        return true;
    }

    protected boolean isValidDataSet(String xml) {
        DataSet ds = new DataSet();
        return ds.setXML(xml);
    }

    protected void addListItems(DataSet listItems, TranslationProcessor translationProcessor) {
        this.addListItems(listItems, translationProcessor, "", false);
    }

    protected void addListItems(DataSet listItems, TranslationProcessor translationProcessor, String status, boolean hideEmptyColumns) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader();
        ArrayList<String> hideList = new ArrayList<String>();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            String currlabel = colList[i];
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            boolean hideColumn = true;
            if (hideEmptyColumns) {
                for (int c = 0; c < listItems.size(); ++c) {
                    if (listItems.getValue(c, colList[i], "").length() <= 0) continue;
                    hideColumn = false;
                    break;
                }
                if (hideColumn) {
                    hideList.add(colList[i]);
                }
            }
            if (hideEmptyColumns && hideColumn) continue;
            this.addColumnHeader(currlabel);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            this.content.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("_") || hideList.contains(colList[j])) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                if (this.isDataSet(columnValue)) {
                    columnValue = columnValue.replaceAll("!]!]!>", "]]>");
                    DataSet inner = new DataSet();
                    inner.setXML(columnValue);
                    ConfigReportContent colval = new ConfigReportContent("inner", translationProcessor);
                    colval.renderListTable(inner, false, translationProcessor, status, hideEmptyColumns);
                    columnValue = colval.toString();
                } else {
                    this.content.append("<td class=\"viewlistcol\">\n");
                }
                if (status.equals("New")) {
                    this.content.append(ConfigReportContent.getNewString(columnValue));
                } else if (status.equals("Deleted")) {
                    this.content.append(ConfigReportContent.getDeletedString(columnValue));
                } else {
                    this.content.append(columnValue);
                }
                this.content.append("</td>\n");
            }
            this.content.append("</TR>\n");
        }
    }

    public void startHeader() {
        this.content.append("<THEAD>\n");
    }

    public void addColumnHeader(String colHeader, boolean rotateHeader) {
        String modTitle = colHeader;
        if (colHeader.length() > 1) {
            modTitle = colHeader.substring(0, 1).toUpperCase() + colHeader.substring(1);
        }
        if (rotateHeader) {
            modTitle = "<div STYLE=\"writing-mode:vertical-rl;\" >" + modTitle + "</div>\n";
        }
        String str = "<th class=\"viewlisthead\">" + modTitle + "</th>\n";
        this.content.append(str);
    }

    public void addColumnHeader(String colHeader) {
        String modTitle = colHeader;
        if (colHeader.length() > 1) {
            modTitle = colHeader.substring(0, 1).toUpperCase() + colHeader.substring(1);
        }
        String str = "<th class=\"viewlisthead\">" + modTitle + "</th>\n";
        this.content.append(str);
    }

    public void addColumnHeader(String columnid, HashMap<String, String> columnTitleMap, TranslationProcessor translationProcessor) {
        String modTitle = columnTitleMap.get(columnid);
        if (modTitle == null) {
            modTitle = columnid;
        }
        if (modTitle.equals(columnid)) {
            modTitle = modTitle.substring(0, 1).toUpperCase() + modTitle.substring(1);
        }
        if (translationProcessor != null) {
            modTitle = translationProcessor.translate(modTitle);
        }
        String str = "<th class=\"viewlisthead\">" + modTitle + "</th>\n";
        this.content.append(str);
    }

    public void endHeader() {
        this.content.append("</THEAD>\n");
    }

    protected void endListTable() {
        this.content.append("</TABLE>\n");
    }

    public static String convertToID(String title) {
        title = title.trim();
        title = title.replaceAll(" ", "_");
        title = title.replaceAll(",", "_");
        title = title.toUpperCase();
        return title;
    }

    protected static boolean isImageObselete(String val) {
        return (val = val.toUpperCase()).toUpperCase().indexOf("IMG") > 0 && val.indexOf("SRC") > 0;
    }

    protected static String removeStrangeChars(String value) {
        value = value.replace("\ufffd", "");
        value = value.replace("#65533", "");
        value = value.replace("#160", "");
        value = value.replace("\u00a0", "");
        if ((value = value.replaceAll("\\r", "")).equals("&#91;&#93;")) {
            value = "";
        }
        if (value.toLowerCase().contains("<script>")) {
            value = SafeHTML.encodeForHTML(value);
        }
        value = value.trim();
        return value;
    }

    public static String getThumbnailDiff(String value, String refValue, TranslationProcessor translationProcessor) {
        if (value == null) {
            value = "";
        }
        if (refValue == null) {
            refValue = "";
        }
        ConfigReportContent info = new ConfigReportContent("Thumbnail", translationProcessor);
        if (value.equals(refValue)) {
            info.append(ConfigReportContent.changeHTMLtags("<img src=\"data:image/gif;base64," + value + "\" />  "));
        } else {
            info.append(ConfigReportContent.getNewString(" New:"));
            info.append(ConfigReportContent.changeHTMLtags("<img src=\"data:image/gif;base64," + value + "\" />  "));
            info.append(ConfigReportContent.getDeletedString("Old:"));
            info.append(ConfigReportContent.changeHTMLtags("<img src=\"data:image/gif;base64," + refValue + "\" />  "));
        }
        return info.toString();
    }

    public static String getDiffString(String value, String refValue) {
        if (value == null) {
            value = "";
        }
        if (refValue == null) {
            refValue = "";
        }
        value = ConfigReportContent.removeStrangeChars(value);
        refValue = ConfigReportContent.removeStrangeChars(refValue);
        if (value.trim().equals(refValue.trim())) {
            if (value.startsWith("<propertytree") || value.startsWith("<?xml") || value.startsWith("<xml")) {
                return HttpUtil.htmlEncode(value);
            }
            return HttpUtil.htmlEncode(value);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<diffpoint/>");
        if (value.startsWith("<propertytree") || value.startsWith("<?xml") || value.startsWith("<xml")) {
            buffer.append(ConfigReportContent.getDiffString(HttpUtil.htmlEncode(value), HttpUtil.htmlEncode(refValue)));
        } else {
            buffer.append("<font class=\"diffreportnewitem\">");
            buffer.append(value);
            buffer.append("</font>");
            if (value.length() > 20 || refValue.length() > 20) {
                buffer.append("<P>");
            }
            buffer.append("<font class=\"diffreportdeleteditem\">");
            buffer.append(refValue);
            buffer.append("</font>");
        }
        return buffer.toString();
    }

    protected void addDiffListItems(DataSet srcItems, DataSet refItems, String[] keycols, PropertyListCollection ignoreCols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        if (keycols == null) {
            keycols = new String[1];
            String[] allCols = srcItems.getColumns();
            keycols[0] = allCols[0];
        }
        srcItems = this.addDiffInfo(srcItems, refItems, keycols, ignoreCols);
        int rows = srcItems.getRowCount();
        int cols = srcItems.getColumnCount();
        String[] colList = srcItems.getColumns();
        this.startHeader();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("_")) continue;
            String currlabel = colList[i];
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            this.addColumnHeader(currlabel);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            this.content.append("<TR VALIGN=TOP >\n");
            String status = srcItems.getString(i, "_status");
            String changedCols = srcItems.getString(i, "_changedcols");
            DataSet changedColInfo = new DataSet(changedCols);
            for (int j = 0; j < cols; ++j) {
                String columnValue = srcItems.getValue(i, colList[j], "");
                if (colList[j].startsWith("_")) continue;
                this.content.append("<td class=\"viewlistcol\">\n");
                if (status.equals("New")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderNewPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = ConfigReportContent.getNewString(columnValue);
                        }
                    }
                } else if (status.equals("Deleted")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderDeletedPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDeletedString(columnValue);
                            }
                        } else {
                            columnValue = ConfigReportContent.getDeletedString(columnValue);
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
                                columnValue = this.renderPropertyListDiff(pl, oldPl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                            }
                        } else {
                            columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                        }
                    } else if (this.isPropertyList(columnValue)) {
                        PropertyList pl = new PropertyList();
                        try {
                            pl.setPropertyList(columnValue, false, false);
                            columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                        }
                        catch (SapphireException sapphireException) {}
                    }
                } else if (this.isPropertyList(columnValue)) {
                    PropertyList pl = new PropertyList();
                    try {
                        pl.setPropertyList(columnValue, false, false);
                        columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                }
                if (columnValue.startsWith("<?xml")) {
                    columnValue = HttpUtil.htmlEncode(columnValue);
                }
                this.content.append(columnValue);
                this.content.append("</td>\n");
            }
            this.content.append("</TR>\n");
        }
    }

    private ConfigReportContent addDiffDetailListItems(HashMap<String, String> columnTitleMap, String listName, String itemdisplay, DataSet listItems, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        ConfigReportContent allRows = new ConfigReportContent(listName, translationProcessor);
        if (keycols == null) {
            keycols = new String[1];
            String[] allCols = listItems.getColumns();
            keycols[0] = allCols[0];
        }
        ArrayList<String> hideList = new ArrayList<String>();
        listItems = this.addDiffInfo(listItems, ref, keycols);
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("_")) continue;
            boolean hideColumn = true;
            if (hideEmptyColumns) {
                for (int c = 0; c < listItems.size(); ++c) {
                    if (listItems.getValue(c, colList[i], "").length() <= 0) continue;
                    hideColumn = false;
                    break;
                }
                if (hideColumn) {
                    hideList.add(colList[i]);
                }
            }
            if (hideEmptyColumns && hideColumn) continue;
            this.addColumnHeader(colList[i], columnTitleMap, translationProcessor);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            ConfigReportContent rowContent = new ConfigReportContent("row:" + i, translationProcessor);
            String nodeId = listName + "_" + i;
            if (listName.equals("primary")) {
                nodeId = listItems.getValue(i, "_snapshotitemkeys");
            }
            rowContent.append("<TR VALIGN=TOP id='" + nodeId + "'>\n");
            String status = listItems.getString(i, "_status");
            String changedCols = listItems.getString(i, "_changedcols");
            DataSet changedColInfo = new DataSet(changedCols);
            for (int j = 0; j < cols; ++j) {
                if (hideList.contains(colList[j])) continue;
                String columnValue = "";
                try {
                    columnValue = listItems.getValue(i, colList[j], "");
                }
                catch (Exception e) {
                    columnValue = "!!Failed Fetching Value!!";
                }
                if (colList[j].startsWith("_")) continue;
                rowContent.append("<td class=\"viewlistcol\">\n");
                if (status.equals("New")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderNewPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = ConfigReportContent.getNewString(columnValue);
                        }
                    }
                } else if (status.equals("Deleted")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderDeletedPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDeletedString(columnValue);
                            }
                        } else {
                            columnValue = ConfigReportContent.getDeletedString(columnValue);
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
                                columnValue = this.renderPropertyListDiff(pl, oldPl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                            }
                        } else {
                            columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                        }
                    } else if (this.isPropertyList(columnValue)) {
                        PropertyList pl = new PropertyList();
                        try {
                            pl.setPropertyList(columnValue, false, false);
                            columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                        }
                        catch (SapphireException sapphireException) {}
                    }
                } else if (this.isPropertyList(columnValue)) {
                    PropertyList pl = new PropertyList();
                    try {
                        pl.setPropertyList(columnValue, false, false);
                        columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                }
                if (columnValue.startsWith("<?xml")) {
                    columnValue = HttpUtil.htmlEncode(columnValue);
                }
                rowContent.append(columnValue);
                rowContent.append("</td>\n");
            }
            rowContent.append("</TR>\n");
            String nodelabel = "";
            if (itemdisplay != null) {
                nodelabel = this.getActualFormattedLabel(itemdisplay, listItems, i, keycols);
            }
            allRows.appendInnerNodeContent(rowContent, nodeId, nodelabel, status);
        }
        return allRows;
    }

    private String getActualFormattedLabel(String labelformat, DataSet ds, int position, String[] keycols) {
        if (labelformat == null || labelformat.length() == 0) {
            labelformat = "";
            for (int i = 0; i < keycols.length; ++i) {
                if (labelformat.length() > 0) {
                    labelformat = labelformat + ", ";
                }
                labelformat = labelformat + ds.getValue(position, keycols[i]);
            }
        } else {
            String[] colnames = ds.getColumns();
            for (int i = 0; i < colnames.length; ++i) {
                if (!labelformat.contains(colnames[i])) continue;
                labelformat = labelformat.replace('[' + colnames[i] + ']', ds.getValue(position, colnames[i]));
            }
        }
        return labelformat;
    }

    protected void addDiffListItems(DataSet listItems, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
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
        this.startHeader();
        ArrayList<String> hideList = new ArrayList<String>();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("_") || colList[i].equals("ChildPropertyDefList")) continue;
            boolean hideColumn = true;
            if (hideEmptyColumns) {
                for (int c = 0; c < listItems.size(); ++c) {
                    if (listItems.getValue(c, colList[i], "").length() <= 0) continue;
                    hideColumn = false;
                    break;
                }
                if (hideColumn) {
                    hideList.add(colList[i]);
                }
            }
            String currlabel = colList[i];
            if (translationProcessor != null) {
                if (currlabel == null) {
                    currlabel = "";
                }
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
            }
            if (hideEmptyColumns && hideColumn) continue;
            this.addColumnHeader(currlabel);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            this.content.append("<TR VALIGN=TOP >\n");
            String status = listItems.getString(i, "_status");
            String changedCols = listItems.getString(i, "_changedcols");
            DataSet changedColInfo = new DataSet(changedCols);
            for (int j = 0; j < cols; ++j) {
                String columnValue = listItems.getValue(i, colList[j], "");
                if (colList[j].startsWith("_") || hideList.contains(colList[j])) continue;
                this.content.append("<td class=\"viewlistcol\">\n");
                if (status.equals("New")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderNewPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else if (this.isPropertyDefList(columnValue)) {
                            PropertyTree propertyTree = new PropertyTree();
                            try {
                                propertyTree.setDefinitionXML(columnValue);
                                columnValue = this.renderPropertyDefinitionList("root", "", propertyTree.getPropertyDefinitionList(), propertyTree.getPropertyDefinitionList(), translationProcessor, true).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = ConfigReportContent.getNewString(columnValue);
                        }
                    }
                } else if (status.equals("Deleted")) {
                    if (columnValue.length() > 0) {
                        if (this.isPropertyList(columnValue)) {
                            PropertyList pl = new PropertyList();
                            try {
                                pl.setPropertyList(columnValue, false, false);
                                columnValue = this.renderDeletedPropertyList(pl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDeletedString(columnValue);
                            }
                        } else if (this.isPropertyDefList(columnValue)) {
                            PropertyTree propertyTree = new PropertyTree();
                            try {
                                propertyTree.setDefinitionXML(columnValue);
                                columnValue = this.renderPropertyDefinitionList("root", "", propertyTree.getPropertyDefinitionList(), propertyTree.getPropertyDefinitionList(), translationProcessor, true).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = ConfigReportContent.getDeletedString(columnValue);
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
                                columnValue = this.renderPropertyListDiff(pl, oldPl, false, translationProcessor).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                            }
                        } else if (this.isPropertyDefList(columnValue)) {
                            PropertyTree srcpropertyTree = new PropertyTree();
                            PropertyTree refpropertyTree = new PropertyTree();
                            try {
                                srcpropertyTree.setDefinitionXML(columnValue);
                                refpropertyTree.setDefinitionXML(oldVal);
                                columnValue = this.renderPropertyDefinitionList("root", "", srcpropertyTree.getPropertyDefinitionList(), refpropertyTree.getPropertyDefinitionList(), translationProcessor, true).toString();
                            }
                            catch (SapphireException e) {
                                columnValue = "<diffpoint/><font class=\"diffreportnewitem\">" + columnValue + "</font>";
                            }
                        } else {
                            columnValue = ConfigReportContent.getDiffString(columnValue, oldVal);
                        }
                    } else if (this.isPropertyList(columnValue)) {
                        PropertyList pl = new PropertyList();
                        try {
                            pl.setPropertyList(columnValue, false, false);
                            columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                        }
                        catch (SapphireException sapphireException) {}
                    }
                } else if (this.isPropertyList(columnValue)) {
                    PropertyList pl = new PropertyList();
                    try {
                        pl.setPropertyList(columnValue, false, false);
                        columnValue = this.renderPropertyList(pl, false, translationProcessor).toString();
                    }
                    catch (SapphireException sapphireException) {}
                } else if (this.isPropertyDefList(columnValue)) {
                    PropertyTree pl = new PropertyTree();
                    try {
                        pl.setDefinitionXML(columnValue);
                        columnValue = this.renderPropertyDefinitionList("root", "", pl.getPropertyDefinitionList(), pl.getPropertyDefinitionList(), translationProcessor, true).toString();
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                }
                if (columnValue.startsWith("<?xml")) {
                    columnValue = HttpUtil.htmlEncode(columnValue);
                }
                this.content.append(columnValue);
                this.content.append("</td>\n");
            }
            this.content.append("</TR>\n");
        }
    }

    public ConfigReportContent renderPropertyDefinitionList(String defpropertyid, String defpropertytitle, PropertyDefinitionList srcdeflist, PropertyDefinitionList refdeflist, TranslationProcessor translationProcessor, boolean inner) {
        String listtitle;
        int i;
        ConfigReportContent tablefull = new ConfigReportContent("Rendering Property Definition Tree", translationProcessor);
        DataSet src = this.getDefinitionDataSet(srcdeflist);
        DataSet ref = this.getDefinitionDataSet(refdeflist);
        String[] colList = src.getColumns();
        tablefull.append("<script>\n\tfunction expand_collapse(elementid, defbodyid, pdrowprefix, pdheadprefix) {\n\tel = document.getElementById(elementid);\n\tif (el != undefined) {\n\t\tvar checked = el.innerHTML.indexOf(\"minus.gif\") > 0;\n\t\tif (checked) {\n\t\t\tvar html = el.innerHTML;\n\t\t\thtml = html.replace( 'minus.gif', 'plus.gif' );\n\t\t\tel.innerHTML = html;\t\t\tchangeStyle(pdrowprefix, pdheadprefix, \"none\");\n\t\t} else {\n\t\t\tvar html = el.innerHTML;\n\t\t\thtml = html.replace( 'plus', 'minus');\n\t\t\tel.innerHTML = html;\n\t\t\tchangeStyle(pdrowprefix, pdheadprefix, \"\");\n\t\t}\n\t}\n}\n\nfunction changeStyle(rowprefix, pdheadprefix, displaystr) {\n\tvar num = 0;\n\tvar e = document.getElementById(rowprefix + num);\n\twhile (e != undefined) {\n\t\tvar currstyle = e.style.display;\n\t\te.style.display= displaystr;\n\t\tnum++;\n\t\te = document.getElementById( rowprefix + num);\n\t}\n\tnum = 0;\n\te = document.getElementById(pdheadprefix + num);\n\twhile (e != undefined) {\n\t\te.style.display= displaystr;\n\t\tnum++;\n\t\te = document.getElementById(pdheadprefix + num);\n\t}\n}\n</script>\n");
        ConfigReportContent tabledetails = new ConfigReportContent("Table details", translationProcessor);
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].equals("ChildPropertyDefList")) continue;
            String currlabel = colList[i];
            if (currlabel == null) {
                currlabel = "";
            }
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            String id = "pdhead" + defpropertyid + i;
            tabledetails.append("<th  id=\"" + id + "\"  style=\"background: gainsboro;border: 1px solid black;display:\";>" + currlabel + "</th>\n");
        }
        this.endHeader();
        for (i = 0; i < src.getRowCount(); ++i) {
            if (src.getValue(i, "Editor").equals("PropertyListEditor")) {
                tabledetails.append("<TR VALIGN=TOP id=\"pdrow" + defpropertyid + i + "\">\n");
                tabledetails.append("<td class=\"viewlistcol\" >");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Property ID", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Title", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Show If", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Editor", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Editor Attributes", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Default Value", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Adv", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Trans", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Depr", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Expr", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Res", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i, "Help", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("</tr>");
                tabledetails.append("<tr>");
                tabledetails.append("<td>");
                tabledetails.append("<tr>");
                tabledetails.append("<td></td>");
                tabledetails.append("<td colspan=11 style=\" border: 1px solid black;\">");
                PropertyTree childTree = new PropertyTree();
                PropertyTree refTree = new PropertyTree();
                try {
                    childTree.setDefinitionXML(src.getValue(i, "ChildPropertyDefList"));
                    int refrow = ref.findRow("Property ID", src.getValue(i, "Property ID"));
                    if (refrow != -1) {
                        refTree.setDefinitionXML(ref.getValue(refrow, "ChildPropertyDefList", ""));
                    }
                    tabledetails.append(this.renderPropertyDefinitionList(src.getValue(i, "Property ID"), src.getValue(i, "Title"), childTree.getPropertyDefinitionList(), refTree.getPropertyDefinitionList(), translationProcessor, true).toString());
                }
                catch (SapphireException e) {
                    Trace.log("Failed to parse child definition properties");
                }
                tabledetails.append("</td>");
                tabledetails.append("</tr>");
                tabledetails.append("</tr>");
                continue;
            }
            tabledetails.append("<TR VALIGN=TOP id=\"pdrow" + defpropertyid + i + "\">\n");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Property ID", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Title", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Show If", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Editor", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Editor Attributes", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Default Value", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Adv", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Trans", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Depr", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Expr", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Res", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i, "Help", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("</TR>\n");
        }
        int count = 0;
        for (int i2 = 0; i2 < ref.getRowCount(); ++i2) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("Property ID", ref.getValue(i2, "Property ID"));
            if (src.findRow(filter) != -1) continue;
            if (ref.getValue(i2, "Editor").equals("PropertyListEditor")) {
                tabledetails.append("<TR VALIGN=TOP id=\"pdrow" + defpropertyid + (src.getRowCount() + count++) + "\">\n");
                tabledetails.append("<td class=\"viewlistcol\">");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Property ID", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Title", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Show If", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Editor", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Editor Attributes", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Adv", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Trans", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Depr", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Expr", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Res", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("<td class=\"viewlistcol\">\n");
                tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Help", src, ref));
                tabledetails.append("</td>");
                tabledetails.append("</tr>");
                tabledetails.append("<tr>");
                tabledetails.append("<td></td>");
                tabledetails.append("<td colspan=11  style=\"border: 1px solid black;\">");
                PropertyTree childTree = new PropertyTree();
                try {
                    childTree.setDefinitionXML(ref.getValue(i2, "ChildPropertyDefList"));
                    tabledetails.append(this.renderPropertyDefinitionList(ref.getValue(i2, "Property ID"), ref.getValue(i2, "Title"), null, childTree.getPropertyDefinitionList(), translationProcessor, true).toString());
                }
                catch (SapphireException e) {
                    Trace.log("Failed to parse child definition properties");
                }
                tabledetails.append("</td>");
                tabledetails.append("</tr>");
                tabledetails.append("</tr>");
                continue;
            }
            tabledetails.append("<TR VALIGN=TOP id=\"pdrow" + defpropertyid + (src.getRowCount() + count++) + "\">\n");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Property ID", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Title", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Show If", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Editor", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Editor Attributes", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Adv", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Trans", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Depr", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Expr", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Res", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("<td class=\"viewlistcol\">\n");
            tabledetails.append(this.getPropertyDefValueDiff(i2, false, "Help", src, ref));
            tabledetails.append("</td>");
            tabledetails.append("</TR>\n");
        }
        tabledetails.endTable();
        ConfigReportContent tablepreamble = new ConfigReportContent("PropertyTreeDef table start", translationProcessor);
        if (inner) {
            tablepreamble.startTableInner();
        } else {
            tablepreamble.startTable();
        }
        String string = listtitle = defpropertytitle.length() == 0 ? defpropertyid : defpropertytitle;
        if (tabledetails.getFoundDiff()) {
            listtitle = ConfigReportContent.getModifiedString(listtitle);
            String image = "";
            if (listtitle.contains("diffreportmodifieditem")) {
                image = "rc?command=image&image=NoteEdit&color=%23FF4300";
                listtitle = ConfigReportContent.changeHTMLtags("<img src=\"" + image + "\"/>&nbsp;&nbsp;") + listtitle;
            }
        }
        tablepreamble.append("<tbody id=\"list_tablebody\"><tr id=\"propertytreeheaderow" + defpropertyid + "\"  class=\"listgrouphead\">\n<td class=\"viewlisthead\" id=\"propertytreegrouptitle" + defpropertyid + "\"  colspan=\"42\" onclick=\"expand_collapse( 'propertytreegrouptitle" + defpropertyid + "', 'propertydefbody" + defpropertyid + "', 'pdrow" + defpropertyid + "', 'pdhead" + defpropertyid + "' );\">\n" + ConfigReportContent.changeHTMLtags("<img src=\"WEB-CORE/elements/images/minus.gif\" id=\"propertytreeimg" + defpropertyid + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\"/>&nbsp;\n") + "\n" + listtitle + "</td>\n</tr>");
        tablefull.appendSpecialContent(tablepreamble);
        tablefull.appendSpecialContent(tabledetails);
        return tablefull;
    }

    private String getPropertyDefValueDiff(int row, String columnid, DataSet first, DataSet sec) {
        return this.getPropertyDefValueDiff(row, true, columnid, first, sec);
    }

    private String getPropertyDefValueDiff(int row, boolean forward, String columnid, DataSet first, DataSet sec) {
        HashMap<String, String> filter = new HashMap<String, String>();
        int srcrow = -1;
        int refrow = -1;
        if (forward) {
            srcrow = row;
        } else {
            refrow = row;
        }
        if (forward) {
            filter.put("Property ID", first.getValue(srcrow, "Property ID", ""));
            refrow = sec.findRow(filter);
        } else {
            filter.put("Property ID", sec.getValue(srcrow, "Property ID", ""));
            srcrow = sec.findRow(filter);
        }
        if (srcrow != -1 && refrow == -1) {
            String currentvalue = first.getValue(srcrow, columnid, "");
            if (columnid.equals("Editor Attributes")) {
                PropertyList list = new PropertyList();
                try {
                    list.setJSONString(currentvalue);
                    ConfigReportContent configReportContent = this.renderNewPropertyList(list, false, this.translationProcessor);
                    return configReportContent.toString();
                }
                catch (JSONException configReportContent) {
                    // empty catch block
                }
            }
            return ConfigReportContent.getNewString(currentvalue);
        }
        if (srcrow == -1 && refrow != -1) {
            String currentvalue = sec.getValue(refrow, columnid, "");
            if (columnid.equals("Editor Attributes")) {
                PropertyList list = new PropertyList();
                try {
                    list.setJSONString(currentvalue);
                    ConfigReportContent configReportContent = this.renderDeletedPropertyList(list, false, this.translationProcessor);
                    return configReportContent.toString();
                }
                catch (JSONException configReportContent) {
                    // empty catch block
                }
            }
            return ConfigReportContent.getDeletedString(currentvalue);
        }
        String currentvalue = first.getValue(srcrow, columnid);
        String oldVal = sec.getValue(refrow, columnid);
        if (columnid.equals("Editor Attributes")) {
            PropertyList srclist = new PropertyList();
            PropertyList targetlist = new PropertyList();
            try {
                srclist.setJSONString(currentvalue);
                targetlist.setJSONString(oldVal);
                ConfigReportContent configReportContent = this.renderPropertyListDiff(srclist, targetlist, false, this.translationProcessor);
                return configReportContent.toString();
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
        return ConfigReportContent.getDiffString(currentvalue, oldVal);
    }

    private DataSet getDefinitionDataSet(PropertyDefinitionList propDefList) {
        DataSet dataSet = new DataSet();
        dataSet.setColidCaseSensitive(true);
        dataSet.addColumn("Property ID", 0);
        dataSet.addColumn("Title", 0);
        dataSet.addColumn("Show If", 0);
        dataSet.addColumn("Editor", 0);
        dataSet.addColumn("Editor Attributes", 0);
        dataSet.addColumn("Default Value", 0);
        dataSet.addColumn("Adv", 0);
        dataSet.addColumn("Trans", 0);
        dataSet.addColumn("Dep", 0);
        dataSet.addColumn("Expr", 0);
        dataSet.addColumn("Res", 0);
        dataSet.addColumn("Help", 0);
        dataSet.addColumn("ChildPropertyDefList", 0);
        if (propDefList != null) {
            Iterator iter = propDefList.iterator();
            int i = 0;
            while (iter.hasNext()) {
                PropertyDefinition propDef = (PropertyDefinition)iter.next();
                dataSet.addRow();
                dataSet.setString(i, "Property ID", propDef.getId());
                dataSet.setString(i, "Title", propDef.getTitle());
                dataSet.setString(i, "Show If", propDef.getShowIf());
                dataSet.setString(i, "Editor", propDef.getEditor());
                HashMap attributes = propDef.getAttributes();
                PropertyList propertyList = new PropertyList(attributes);
                dataSet.setString(i, "Editor Attributes", propertyList.toJSONString());
                dataSet.setString(i, "Default Value", propDef.getDefaultValue());
                dataSet.setString(i, "Adv", propDef.isAdvanced() ? "Y" : "N");
                dataSet.setString(i, "Trans", propDef.getTranslate());
                dataSet.setString(i, "Dep", propDef.isDeprecated() ? "Y" : "N");
                dataSet.setString(i, "Expr", propDef.isExpression() ? "Y" : "N");
                dataSet.setString(i, "Res", propDef.isResolution() ? "Y" : "N");
                dataSet.setString(i, "Help", propDef.getHelp());
                if (propDef.getPropertyDefinitionList() != null) {
                    dataSet.setString(i, "ChildPropertyDefList", propDef.getPropertyDefinitionList().toXMLString());
                }
                ++i;
            }
        }
        return dataSet;
    }

    public ConfigReportContent renderPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor) {
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (pl == null || pl.size() == 0) {
            return tempTable;
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            String prop;
            String currlabel;
            if (pl.isPropertyList(keyes[i].toString())) {
                ConfigReportContent propertylist = this.renderPropertyList(pl.getPropertyList(keyes[i].toString()), false, translationProcessor);
                if (propertylist.length() <= 0) continue;
                tempTable.startRow();
                currlabel = keyes[i].toString();
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
                tempTable.addRowItem(currlabel, propertylist.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (pl.isCollection(keyes[i].toString())) {
                ConfigReportContent collection = this.renderCollection(pl.getCollection(keyes[i].toString()), false, translationProcessor);
                if (collection.length() <= 0) continue;
                tempTable.startRow();
                currlabel = keyes[i].toString();
                if (translationProcessor != null) {
                    currlabel = translationProcessor.translate(currlabel);
                }
                tempTable.addRowItem(currlabel, collection.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (keyes[i].toString().startsWith("__") || (prop = pl.getProperty(keyes[i].toString(), "")).length() <= 0) continue;
            tempTable.startRow();
            currlabel = keyes[i].toString();
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            tempTable.addRowItem(currlabel, prop, translationProcessor);
            tempTable.endRow();
        }
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    private boolean isPropertyList(String val) {
        if (val != null && val.toUpperCase().indexOf("<PROPERTYLIST") > -1) {
            PropertyList p = new PropertyList();
            try {
                p.setPropertyList(val);
                return true;
            }
            catch (SapphireException e) {
                return false;
            }
        }
        return false;
    }

    private boolean isPropertyTree(String val) {
        if (val != null && val.toUpperCase().indexOf("<PROPERTYTREE") > -1) {
            PropertyTree p = new PropertyTree();
            try {
                p.setValueXML(val);
                return true;
            }
            catch (SapphireException e) {
                return false;
            }
        }
        return false;
    }

    private boolean isPropertyDefList(String val) {
        return val != null && val.toUpperCase().indexOf("<PROPERTYDEFLIST") > -1;
    }

    private boolean isDataSet(String val) {
        return val != null && val.toUpperCase().indexOf("<DATASET") > -1;
    }

    public DataSet addDiffInfo(DataSet listItems, DataSet ref, String[] keyColumns) {
        return this.addDiffInfo(listItems, ref, keyColumns, new PropertyListCollection());
    }

    public DataSet addDiffInfo(DataSet srcItemsOriginal, DataSet refItemsOriginal, String[] keyColumns, PropertyListCollection ignoreCols) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        DataSet srcItems = new DataSet();
        srcItems.setColidCaseSensitive(true);
        DataSet refItems = new DataSet();
        refItems.setColidCaseSensitive(true);
        if (srcItemsOriginal != null) {
            srcItems.copyRow(srcItemsOriginal, -1, 1);
        }
        if (refItemsOriginal != null) {
            refItems.copyRow(refItemsOriginal, -1, 1);
        }
        for (int srcRow = 0; srcRow < srcItems.size(); ++srcRow) {
            for (int keycol = 0; keycol < keyColumns.length; ++keycol) {
                String keyColumnid = keyColumns[keycol];
                String val = srcItems.getValue(srcRow, keyColumnid);
                if (val.length() > 0 && srcItems.getColumnType(keyColumnid) == 1) {
                    filter.put(keyColumnid, new BigDecimal(val));
                    continue;
                }
                filter.put(keyColumnid, val);
            }
            int refRow = this.findRow(refItems, filter);
            if (refRow == -1) {
                if (!this.isIgnoreRow(srcRow, srcItems, ignoreCols)) {
                    this.foundDiff = true;
                    srcItems.setString(srcRow, "_status", "New");
                    continue;
                }
                srcItems.setString(srcRow, "_status", "None");
                continue;
            }
            DataSet changes = new DataSet();
            if (!this.isIgnoreRow(srcRow, srcItems, ignoreCols)) {
                changes = this.getChangedColumns(srcItems, srcRow, refItems, refRow, ignoreCols);
            }
            if (changes.getRowCount() > 0) {
                this.foundDiff = true;
                srcItems.setString(srcRow, "_status", "Modified");
                srcItems.setString(srcRow, "_changedcols", changes.toXML());
            } else {
                srcItems.setString(srcRow, "_status", "None");
            }
            refItems.deleteRow(refRow);
        }
        for (int delitems = 0; delitems < refItems.getRowCount(); ++delitems) {
            int newRow = srcItems.addRow();
            String[] columnNames = refItems.getColumns();
            for (int col = 0; col < columnNames.length; ++col) {
                if (!srcItems.isValidColumn(columnNames[col])) {
                    srcItems.addColumn(columnNames[col], refItems.getColumnType(columnNames[col]));
                }
                srcItems.setValue(newRow, columnNames[col], refItems.getValue(delitems, columnNames[col], ""));
            }
            this.foundDiff = true;
            srcItems.setString(newRow, "_status", "Deleted");
        }
        return srcItems;
    }

    public ConfigReportContent renderCollectionDiff(String basenode, boolean highlightOverride, boolean hideInheritedProperties, PropertyListCollection coll, PropertyListCollection refColl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        if (defList == null || defList.size() == 0) {
            return this.renderCollectionDiff(coll, refColl, top, translationProcessor, hideEmptyColumns);
        }
        if (coll == null) {
            coll = new PropertyListCollection();
        }
        if (refColl == null) {
            refColl = new PropertyListCollection();
        }
        if (!highlightOverride && this.checkIfSimple(coll) && this.checkIfSimple(refColl)) {
            DataSet srcCollDS = ConfigReportContent.convertSimpleCollToDS(coll, defList);
            DataSet refCollDS = ConfigReportContent.convertSimpleCollToDS(refColl, defList);
            ConfigReportContent content = new ConfigReportContent("Diff Simple Collections", translationProcessor);
            content.renderDiffListTable(srcCollDS, refCollDS, new String[]{"id"}, top, translationProcessor, false);
            return content;
        }
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (coll.size() == 0 && refColl.size() == 0) {
            return tempTable;
        }
        for (i = 0; i < coll.size(); ++i) {
            PropertyList pl = coll.getPropertyList(i);
            if (pl == null || pl.size() <= 0) continue;
            String title = this.getTitle(defList, pl);
            PropertyList refPl = ConfigReportContent.find(i, coll, refColl);
            if (refPl == null || refPl.size() == 0) {
                ConfigReportContent propertylistcontent = this.renderNewPropertyList(basenode, highlightOverride, hideInheritedProperties, pl, defList, false, translationProcessor);
                if (propertylistcontent.length() <= 0) continue;
                tempTable.startRow();
                if (translationProcessor != null) {
                    title = translationProcessor.translate(title);
                }
                if (propertylistcontent.getFoundDiff()) {
                    tempTable.addNewRowItem(title, propertylistcontent.toString(), translationProcessor);
                } else {
                    tempTable.addRowItem(title, propertylistcontent.toString(), translationProcessor);
                }
                tempTable.endRow();
                continue;
            }
            String propertylist = this.renderPropertyListDiff(basenode, highlightOverride, hideInheritedProperties, pl, refPl, defList, reportAdvancedProperties, false, translationProcessor, hideEmptyColumns).toString();
            if (propertylist.length() <= 0) continue;
            tempTable.startRow();
            tempTable.addRowItem(title, propertylist, translationProcessor);
            tempTable.endRow();
        }
        if (refColl != null) {
            for (i = 0; i < refColl.size(); ++i) {
                PropertyList currpl;
                PropertyList refpl = refColl.getPropertyList(i);
                if (refpl == null || refpl.size() <= 0 || (currpl = ConfigReportContent.find(i, refColl, coll)) != null && currpl.size() != 0) continue;
                String title = this.getTitle(defList, refpl);
                ConfigReportContent c = this.renderDeletedPropertyList(basenode, highlightOverride, hideInheritedProperties, refpl, false, translationProcessor);
                if (c.length() <= 0) continue;
                tempTable.startRow();
                tempTable.addDeletedRowItem(title, c.toString(), translationProcessor);
                tempTable.endRow();
            }
        }
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public ConfigReportContent renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        if (defList == null || defList.size() == 0) {
            return this.renderCollectionDiff(coll, refColl, top, translationProcessor, hideEmptyColumns);
        }
        if (coll == null) {
            coll = new PropertyListCollection();
        }
        if (refColl == null) {
            refColl = new PropertyListCollection();
        }
        if (this.checkIfSimple(coll) && this.checkIfSimple(refColl)) {
            DataSet srcCollDS = ConfigReportContent.convertSimpleCollToDS(coll, defList);
            DataSet refCollDS = ConfigReportContent.convertSimpleCollToDS(refColl, defList);
            ConfigReportContent content = new ConfigReportContent("Diff Simple Collections", translationProcessor);
            content.renderDiffListTable(srcCollDS, refCollDS, new String[]{"id"}, top, translationProcessor, false);
            return content;
        }
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (coll.size() == 0 && refColl.size() == 0) {
            return tempTable;
        }
        for (i = 0; i < coll.size(); ++i) {
            String propertylist;
            PropertyList pl = coll.getPropertyList(i);
            if (pl == null || pl.size() <= 0) continue;
            String title = this.getTitle(defList, pl);
            PropertyList refPl = ConfigReportContent.find(i, coll, refColl);
            if (refPl == null || refPl.size() == 0) {
                propertylist = this.renderNewPropertyList(pl, false, translationProcessor).toString();
                if (propertylist.length() <= 0) continue;
                tempTable.startRow();
                if (translationProcessor != null) {
                    title = translationProcessor.translate(title);
                }
                tempTable.addNewRowItem(title, propertylist, translationProcessor);
                tempTable.endRow();
                continue;
            }
            propertylist = this.renderPropertyListDiff(pl, refPl, defList, reportAdvancedProperties, false, translationProcessor, hideEmptyColumns).toString();
            if (propertylist.length() <= 0) continue;
            tempTable.startRow();
            tempTable.addRowItem(title, propertylist, translationProcessor);
            tempTable.endRow();
        }
        if (refColl != null) {
            for (i = 0; i < refColl.size(); ++i) {
                PropertyList currpl;
                PropertyList refpl = refColl.getPropertyList(i);
                if (refpl == null || refpl.size() <= 0 || (currpl = ConfigReportContent.find(i, refColl, coll)) != null && currpl.size() != 0) continue;
                String title = this.getTitle(defList, refpl);
                ConfigReportContent c = this.renderDeletedPropertyList(refpl, false, translationProcessor);
                if (c.length() <= 0) continue;
                tempTable.startRow();
                tempTable.addDeletedRowItem(title, c.toString(), translationProcessor);
                tempTable.endRow();
            }
        }
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable.toString());
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

    public ConfigReportContent renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        if (coll == null) {
            coll = new PropertyListCollection();
        }
        if (refColl == null) {
            refColl = new PropertyListCollection();
        }
        if (this.checkIfSimple(coll) && this.checkIfSimple(refColl)) {
            DataSet srcCollDS = ConfigReportContent.convertSimpleCollToDS(coll, null);
            DataSet refCollDS = ConfigReportContent.convertSimpleCollToDS(refColl, null);
            ConfigReportContent content = new ConfigReportContent("Diff Simple Collections", translationProcessor);
            content.renderDiffListTable(srcCollDS, refCollDS, srcCollDS.getColumns(), top, translationProcessor, hideEmptyColumns);
            return content;
        }
        ConfigReportContent collVal = new ConfigReportContent(this.context, translationProcessor);
        if (coll.size() == 0 && refColl.size() == 0) {
            return collVal;
        }
        if (top) {
            collVal.startTable();
        } else {
            collVal.startTableInner();
        }
        for (i = 0; i < coll.size(); ++i) {
            ConfigReportContent diff;
            PropertyList refPl;
            Set s;
            PropertyList pl = coll.getPropertyList(i);
            String currId = pl.getAttribute("id");
            if (currId == null || currId.length() == 0) {
                currId = pl.getProperty("id", "");
            }
            if (currId.length() == 0 && (s = pl.keySet()).size() > 0) {
                currId = pl.getProperty(s.toArray()[0].toString());
            }
            if ((refPl = ConfigReportContent.find(i, coll, refColl)) != null) {
                diff = this.renderPropertyListDiff(pl, refPl, false, translationProcessor);
                collVal.startRow();
                if (diff.indexOf("<strike>") > -1) {
                    this.foundDiff = true;
                }
                collVal.addRowItem(currId, diff.toString(), translationProcessor);
                collVal.endRow();
                continue;
            }
            diff = this.renderNewPropertyList(pl, false, translationProcessor);
            collVal.startRow();
            this.foundDiff = true;
            collVal.addNewRowItem(currId, diff.toString(), translationProcessor);
            collVal.endRow();
        }
        for (i = 0; i < refColl.size(); ++i) {
            ConfigReportContent item;
            PropertyList match;
            PropertyList refpl = refColl.getPropertyList(i);
            String refcurrId = refpl.getAttribute("id");
            if (refcurrId == null || refcurrId.length() == 0) {
                refcurrId = refpl.getProperty("id", "");
            }
            if ((match = ConfigReportContent.find(i, refColl, coll)).size() != 0 || (item = this.renderDeletedPropertyList(refpl, false, translationProcessor)).length() <= 0) continue;
            collVal.startRow();
            this.foundDiff = true;
            collVal.addDeletedRowItem(refcurrId, item.toString(), translationProcessor);
            collVal.endRow();
        }
        collVal.endTable();
        return collVal;
    }

    static PropertyList find(int pos, PropertyListCollection coll, PropertyListCollection refColl) {
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
        if (idAttribute.length() > 0) {
            for (int j = 0; j < refColl.size(); ++j) {
                PropertyList temp = refColl.getPropertyList(j);
                if (temp == null || !temp.getId().equals(idAttribute)) continue;
                return temp;
            }
        } else {
            String idProperty = currPl.getProperty("id", "");
            if (idProperty.length() > 0) {
                for (int j = 0; j < refColl.size(); ++j) {
                    PropertyList temp = refColl.getPropertyList(j);
                    if (temp == null || !idProperty.equals(temp.getProperty("id"))) continue;
                    return temp;
                }
            } else {
                for (int j = 0; j < refColl.size(); ++j) {
                    PropertyList temp = refColl.getPropertyList(j);
                    if (temp == null) continue;
                    Object[] propsList = currPl.keySet().toArray();
                    boolean found = true;
                    for (int i = 0; i < propsList.length; ++i) {
                        String propname = propsList[i].toString();
                        if (!currPl.isSimple(propname) || temp.getProperty(propname, "").equals(currPl.getProperty(propname, ""))) continue;
                        found = false;
                    }
                    if (!found) continue;
                    return temp;
                }
                Trace.logError("Did not find matching id attribute or property for the collection items");
            }
        }
        return new PropertyList();
    }

    public ConfigReportContent renderNewPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor) {
        return this.renderNewPropertyList("", false, false, pl, null, top, translationProcessor);
    }

    public ConfigReportContent renderNewPropertyList(String currnodeid, boolean highlightoverride, boolean hideinheritedproperties, PropertyList pl, PropertyDefinitionList defList, boolean top, TranslationProcessor translationProcessor) {
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (pl.size() == 0) {
            return table;
        }
        if (top) {
            table.startTable();
        } else {
            table.startTableInner();
        }
        ConfigReportContent attrContent = this.renderPropertyListAttributesDiff(pl, new PropertyList());
        if (attrContent.length() > 0) {
            table.append(attrContent.toString());
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            table.startRow();
            String currlabel = keyes[i].toString();
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            if (pl.isPropertyList(keyes[i].toString())) {
                ConfigReportContent newPl = this.renderPropertyListDiff(currnodeid, highlightoverride, hideinheritedproperties, pl.getPropertyList(keyes[i].toString()), new PropertyList(), defList, true, top, translationProcessor, true);
                if (newPl.getFoundDiff()) {
                    table.addNewRowItem(currlabel, newPl.toString(), translationProcessor);
                } else {
                    table.addRowItem(currlabel, newPl.toString(), translationProcessor);
                }
            } else if (pl.isCollection(keyes[i].toString())) {
                ConfigReportContent newCollection = this.renderCollectionDiff(currnodeid, highlightoverride, hideinheritedproperties, pl.getCollectionNotNull(keyes[i].toString()), new PropertyListCollection(), defList, true, top, translationProcessor, true);
                if (newCollection.getFoundDiff()) {
                    table.addNewRowItem(currlabel, newCollection.toString(), translationProcessor);
                } else {
                    table.addRowItem(currlabel, newCollection.toString(), translationProcessor);
                }
            } else if (!keyes[i].toString().startsWith("__")) {
                PropertyValue propertyValue = pl.getPropertyValue(keyes[i].toString());
                boolean isOverridingProperty = false;
                String definedInNode = propertyValue.getPropertyTreeNodeId();
                if (highlightoverride && currnodeid.equals(definedInNode)) {
                    isOverridingProperty = true;
                }
                if (highlightoverride && !isOverridingProperty) {
                    if (!hideinheritedproperties) {
                        table.addRowItemEmphasis(currlabel, pl.getProperty(keyes[i].toString(), ""), translationProcessor);
                    }
                } else {
                    table.addNewRowItem(currlabel, pl.getProperty(keyes[i].toString(), ""), translationProcessor);
                }
            }
            table.endRow();
        }
        table.endTable();
        return table;
    }

    public ConfigReportContent renderDeletedPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor) {
        return this.renderDeletedPropertyList("", false, false, pl, top, translationProcessor);
    }

    public ConfigReportContent renderDeletedPropertyList(String currnodeid, boolean highlightOverride, boolean hideInheritedProperties, PropertyList pl, boolean top, TranslationProcessor translationProcessor) {
        int rows = 0;
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (pl.size() == 0) {
            return table;
        }
        if (top) {
            table.startTable();
        } else {
            table.startTableInner();
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            ConfigReportContent c;
            table.startRow();
            String currlabel = keyes[i].toString();
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            if (pl.isPropertyList(keyes[i].toString())) {
                c = this.renderDeletedPropertyList(pl.getPropertyList(keyes[i].toString()), false, translationProcessor);
                if (c.length() > 0) {
                    table.addDeletedRowItem(currlabel, c.toString(), translationProcessor);
                    ++rows;
                }
            } else if (pl.isCollection(keyes[i].toString())) {
                c = this.renderCollection(pl.getCollection(keyes[i].toString()), false, translationProcessor);
                if (c.length() > 0) {
                    table.addDeletedRowItem(currlabel, c.toString(), translationProcessor);
                    ++rows;
                }
            } else {
                PropertyValue propertyValue = pl.getPropertyValue(keyes[i].toString());
                boolean isOverridingProperty = false;
                String definedInNode = propertyValue.getPropertyTreeNodeId();
                if (highlightOverride && currnodeid.equals(definedInNode)) {
                    isOverridingProperty = true;
                }
                if (highlightOverride && !isOverridingProperty) {
                    if (!hideInheritedProperties) {
                        table.addDeletedRowItemEmphasis(currlabel, pl.getProperty(keyes[i].toString(), ""), translationProcessor);
                        ++rows;
                    }
                } else {
                    table.addRowItem(currlabel, pl.getProperty(keyes[i].toString(), ""), translationProcessor);
                    ++rows;
                }
            }
            table.endRow();
        }
        table.endTable();
        if (rows > 0) {
            return table;
        }
        return new ConfigReportContent("empty", translationProcessor);
    }

    public boolean checkIfSimple(PropertyListCollection coll) {
        if (coll != null) {
            for (int curr = 0; curr < coll.size(); ++curr) {
                PropertyList currPropertyList = coll.getPropertyList(curr);
                Object[] keyes = currPropertyList.keySet().toArray();
                for (int i = 0; i < keyes.length; ++i) {
                    String property = (String)keyes[i];
                    if (property.startsWith("__") || currPropertyList.isSimple(property)) continue;
                    if (currPropertyList.isCollection(property)) {
                        PropertyListCollection currColl = currPropertyList.getCollection(property);
                        if (currColl == null || currColl.size() <= 0) continue;
                        return false;
                    }
                    if (!currPropertyList.isPropertyList(property) || currPropertyList.getPropertyList(property) == null || currPropertyList.getPropertyList(property).isEmpty()) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public static DataSet convertSimpleCollToDS(PropertyListCollection coll, PropertyDefinitionList defList) {
        DataSet ds = new DataSet();
        if (coll != null) {
            for (int curr1 = 0; curr1 < coll.size(); ++curr1) {
                PropertyList currPropertyList = coll.getPropertyList(curr1);
                Object[] keyes = currPropertyList.keySet().toArray();
                String titleProperty = "id";
                if (defList != null) {
                    titleProperty = defList.getTitlePropertyId();
                }
                boolean newRow = true;
                int row = -1;
                for (int i = 0; i < keyes.length; ++i) {
                    String currProp = (String)keyes[i];
                    if (!currPropertyList.isSimple(currProp) || currPropertyList.getProperty(currProp, "").trim().length() <= 0) continue;
                    if (newRow) {
                        row = ds.addRow();
                        newRow = false;
                    }
                    ds.setString(row, currProp, currPropertyList.getProperty(currProp));
                }
            }
        }
        return ds;
    }

    public ConfigReportContent renderCollection(PropertyListCollection coll, boolean top, TranslationProcessor translationProcessor) {
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (coll.size() == 0) {
            return tempTable;
        }
        boolean isSimpleCollection = this.checkIfSimple(coll);
        if (!isSimpleCollection) {
            for (int i = 0; i < coll.size(); ++i) {
                PropertyList pl = coll.getPropertyList(i);
                ConfigReportContent properytlist = this.renderPropertyList(pl, false, translationProcessor);
                if (properytlist.length() <= 0) continue;
                tempTable.startRow();
                tempTable.addRowItem(pl.getProperty("id", ""), properytlist.toString(), translationProcessor);
                tempTable.endRow();
            }
        } else {
            DataSet ds = ConfigReportContent.convertSimpleCollToDS(coll, null);
            ConfigReportContent collStr = new ConfigReportContent("Simple Collection", translationProcessor);
            collStr.renderListTable(ds, top, translationProcessor, "", false);
            tempTable.append(collStr.toString());
            return tempTable;
        }
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public ConfigReportContent renderCollection(PropertyListCollection coll, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor) {
        if (defList == null || defList.size() == 0) {
            return this.renderCollection(coll, top, translationProcessor);
        }
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (coll.size() == 0) {
            return tempTable;
        }
        boolean ifSimpleCollection = this.checkIfSimple(coll);
        if (!ifSimpleCollection) {
            for (int i = 0; i < coll.size(); ++i) {
                ConfigReportContent propertyList;
                String title;
                PropertyList pl = coll.getPropertyList(i);
                String titleProperty = defList.getTitlePropertyId();
                if (titleProperty == null || titleProperty.trim().length() == 0) {
                    titleProperty = "id";
                }
                if ((title = pl.getProperty(titleProperty)) == null || title.trim().length() == 0) {
                    title = pl.getProperty("id");
                }
                if ((propertyList = this.renderPropertyList(pl, defList, reportAdvancedProperties, false, translationProcessor)).length() <= 0) continue;
                tempTable.startRow();
                if (translationProcessor != null) {
                    title = translationProcessor.translate(title);
                }
                tempTable.addRowItem(title, propertyList.toString(), translationProcessor);
                tempTable.endRow();
            }
        } else {
            DataSet ds = ConfigReportContent.convertSimpleCollToDS(coll, defList);
            ConfigReportContent collStr = new ConfigReportContent("Simple Collection", translationProcessor);
            collStr.renderListTable(ds, top, translationProcessor, "", true);
            tempTable.append(collStr.toString());
            return tempTable;
        }
        ConfigReportContent table = new ConfigReportContent(this.context, translationProcessor);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public void renderMatrix(DataSet matrix, int keycols) {
        if (matrix == null || matrix.getRowCount() == 0) {
            this.content.append(" Not found.");
            return;
        }
        this.addMatrix(matrix, keycols);
    }

    public void addMatrix(DataSet roleMatrix, int keycols) {
        this.startListTableTop();
        this.addRotatedHeaderListItems(roleMatrix, keycols, this.translationProcessor, false);
        this.endListTable();
    }

    protected void addRotatedHeaderListItems(DataSet listItems, int keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        this.startHeader();
        ArrayList<String> hideList = new ArrayList<String>();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            boolean hideColumn = true;
            if (hideEmptyColumns) {
                for (int c = 0; c < listItems.size(); ++c) {
                    if (listItems.getValue(c, colList[i], "").length() <= 0) continue;
                    hideColumn = false;
                    break;
                }
                if (hideColumn) {
                    hideList.add(colList[i]);
                }
            }
            if (hideEmptyColumns && hideColumn) continue;
            String currlabel = colList[i];
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            if (i < keycols) {
                this.addColumnHeader(currlabel);
                continue;
            }
            this.addColumnHeader(currlabel, true);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            HashMap currrow = (HashMap)listItems.get(i);
            if (currrow == null || currrow.size() <= 0) continue;
            this.content.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__") || hideList.contains(colList[j])) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                this.content.append("<td class=\"viewlistcol\">\n");
                this.content.append(columnValue);
                this.content.append("</td>\n");
            }
            this.content.append("</TR>\n");
        }
    }

    private ConfigReportContent addDetailListItems(HashMap<String, String> columnTitleMap, String detailtable, String itemdisplay, DataSet listItems, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        int i;
        ConfigReportContent allRows = new ConfigReportContent(detailtable, translationProcessor);
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        ArrayList<String> hideList = new ArrayList<String>();
        this.startHeader();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("_")) continue;
            String currlabel = colList[i];
            boolean hideColumn = true;
            if (hideEmptyColumns) {
                for (int c = 0; c < listItems.size(); ++c) {
                    if (listItems.getValue(c, colList[i], "").length() <= 0) continue;
                    hideColumn = false;
                    break;
                }
                if (hideColumn) {
                    hideList.add(colList[i]);
                }
            }
            if (hideEmptyColumns && hideColumn) continue;
            if (translationProcessor != null) {
                currlabel = translationProcessor.translate(currlabel);
            }
            this.addColumnHeader(currlabel, columnTitleMap, translationProcessor);
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            HashMap currrow = (HashMap)listItems.get(i);
            ConfigReportContent rowContent = new ConfigReportContent("row" + i, translationProcessor);
            String status = listItems.getString(i, "_status");
            if (currrow == null || currrow.size() <= 0) continue;
            String currRowNodeId = detailtable + "_" + i;
            if (detailtable.equals("primary")) {
                currRowNodeId = listItems.getValue(i, "_snapshotitemkeys");
            }
            rowContent.append("<TR VALIGN=TOP id=" + currRowNodeId + ">\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__") || hideList.contains(colList[j])) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                rowContent.append("<td class=\"viewlistcol\">\n");
                columnValue = ConfigReportContent.removeStrangeChars(columnValue);
                rowContent.append(columnValue);
                rowContent.append("</td>\n");
            }
            rowContent.append("</TR>\n");
            if (itemdisplay != null && itemdisplay.length() > 0) {
                String nodelabel = this.getActualFormattedLabel(itemdisplay, listItems, i, keycols);
                allRows.appendInnerNodeContent(rowContent, currRowNodeId, nodelabel, status);
                continue;
            }
            allRows.appendInnerNodeContent(rowContent, currRowNodeId, currRowNodeId, status);
        }
        return allRows;
    }

    private boolean isIgnoreCol(String colName, PropertyListCollection ignoreCols) {
        for (int i = 0; i < ignoreCols.size(); ++i) {
            PropertyList curr = ignoreCols.getPropertyList(i);
            String type = curr.getProperty("roworcolumn");
            if (!type.equals("Column") || !colName.equals(curr.getProperty("columnid")) && !colName.equals(curr.getProperty("columnid"))) continue;
            return true;
        }
        return false;
    }

    private boolean isIgnoreRow(int rowid, DataSet list, PropertyListCollection ignoreCols) {
        if (ignoreCols != null) {
            for (int i = 0; i < ignoreCols.size(); ++i) {
                PropertyList curr = ignoreCols.getPropertyList(i);
                String type = curr.getProperty("roworcolumn");
                if (!type.equals("Row")) continue;
                String colid = curr.getProperty("columnid");
                String collabel = curr.getProperty("columnlabel");
                String colval = curr.getProperty("columnvalue");
                if (colid == null || colval == null) continue;
                String currval = list.getString(rowid, colid);
                if (currval == null) {
                    currval = list.getString(rowid, collabel, "");
                }
                if (!currval.equals(colval)) continue;
                return true;
            }
        }
        return false;
    }

    private DataSet getChangedColumns(DataSet srcItems, int srcRow, DataSet refItems, int refRow, PropertyListCollection ignoreCols) {
        DataSet changedCols = new DataSet();
        changedCols.setColidCaseSensitive(true);
        String[] columnNames = srcItems.getColumns();
        for (int i = 0; i < columnNames.length; ++i) {
            if (columnNames[i].equals("_status") || columnNames[i].equals("_changedcols") || columnNames[i].equals("_position")) continue;
            String newval = srcItems.getValue(srcRow, columnNames[i], "");
            newval = ConfigReportContent.removeStrangeChars(newval);
            String oldval = refItems.getValue(refRow, columnNames[i], "");
            if (newval.equals(oldval = ConfigReportContent.removeStrangeChars(oldval)) || this.isIgnoreCol(columnNames[i], ignoreCols)) continue;
            int currrow = changedCols.addRow();
            changedCols.setString(currrow, "colname", columnNames[i]);
            changedCols.setString(currrow, "oldval", oldval.replaceAll("]]>", "!]!]!>"));
            changedCols.setString(currrow, "newval", newval);
        }
        return changedCols;
    }

    public static String generateSectionAnchor(String layout) {
        return layout.toUpperCase().replaceAll(" ", "_");
    }

    public static String generateNodeAnchor(String node) {
        node = node + "_NODEPOSITION";
        return StringUtil.replaceAll(node.toUpperCase(), " ", "_");
    }

    public static boolean isFKIncluded(String sdcId, String keyid1, String keyid2, String keyid3, HashMap sdisIncluded) {
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

    public static boolean isFKIncluded(String sdcId, String keyid1, HashMap sdisIncluded) {
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

    public static String getPageEdition(String connectionid, String webpageId) {
        try {
            return new WebAdminProcessor(connectionid).getDefaultPageEdition(webpageId);
        }
        catch (Exception e) {
            return "R5";
        }
    }

    public static String generateSectionXMLFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + ".xml";
    }

    public void renderCategories(DataSet categories) {
        if (categories == null || categories.getRowCount() == 0) {
            this.content.append("No categories specified");
            return;
        }
        this.addCategories(categories);
    }

    public void renderCategoriesDiff(DataSet categories, DataSet refCategories) {
        this.addCategoriesDiff(categories, refCategories);
    }

    public void addCategories(DataSet categories) {
        for (int i = 0; i < categories.getRowCount(); ++i) {
            if (i != 0) {
                this.content.append(", ");
            } else {
                this.content.append("<P>");
            }
            this.content.append(categories.getString(i, "Category ID"));
        }
    }

    public void addCategoriesDiff(DataSet categories, DataSet refCategories) {
        DataSet find;
        HashMap<String, String> filter;
        String currCategory;
        int i;
        for (i = 0; i < categories.getRowCount(); ++i) {
            currCategory = categories.getString(i, "Category ID");
            this.content.append(" ");
            filter = new HashMap<String, String>();
            filter.put("Category ID", currCategory);
            find = refCategories.getFilteredDataSet(filter);
            if (find.getRowCount() > 0) {
                this.content.append(currCategory);
                continue;
            }
            this.content.append(ConfigReportContent.getNewString(currCategory));
        }
        for (i = 0; i < refCategories.getRowCount(); ++i) {
            currCategory = refCategories.getString(i, "Category ID");
            filter = new HashMap();
            filter.put("Category ID", currCategory);
            find = categories.getFilteredDataSet(filter);
            if (find.getRowCount() != 0) continue;
            this.content.append(" ");
            this.content.append(ConfigReportContent.getDeletedString(currCategory));
        }
    }

    public static String generateSDISectionXMLFileName(SDI currentSDI) {
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
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + ".xml";
    }

    public void renderProcessingScript(String processingScript, TranslationProcessor translationProcessor) throws SapphireException {
        this.renderProcessingScript("", "", processingScript, translationProcessor, false);
    }

    public void renderProcessingScript(String applicationRoot, String folder, String processingScript, TranslationProcessor translationProcessor, boolean configreport) throws SapphireException {
        try {
            if (configreport) {
                ConfigReportContent.copyFile(new File(applicationRoot + "/WEB-CORE/gwt/pageentry/images/StartEnd.png"), new File(folder + "/images/WEB-CORE/gwt/pageentry/images/StartEnd.png"));
                ConfigReportContent.copyFile(new File(applicationRoot + "/WEB-CORE/images/png/decision.png"), new File(folder + "/images/WEB-CORE/images/png/decision.png"));
            }
        }
        catch (Exception e) {
            Trace.log("Failed to copy file /WEB-CORE/images/png/line.png");
        }
        if (processingScript != null && processingScript.length() > 0) {
            if (this.isValidActionBlock(processingScript)) {
                ActionBlock actionBlock = new ActionBlock(processingScript);
                DataSet ab = new DataSet();
                ab = this.addActionBlockItems(ab, actionBlock, 0, "1", "");
                this.content.append(this.renderActionBlockDiagram(ab, configreport));
                this.content.append(this.renderActionBlockDetails(actionBlock, translationProcessor));
            } else {
                this.content.append("<P>" + processingScript + "</P>");
            }
        }
    }

    public String getApplicationRoot() {
        return this.applicationRoot;
    }

    public String getFolder() {
        return this.folder;
    }

    public void renderProcessingScriptDiff(String srcProcessingScript, String refProcessingScript, boolean showTranslation, TranslationProcessor translationProcessor) throws SapphireException {
        this.renderProcessingScriptDiff("", "", srcProcessingScript, refProcessingScript, showTranslation, translationProcessor, false);
    }

    public void renderProcessingScriptDiff(String applicationRoot, String folder, String srcProcessingScript, String refProcessingScript, boolean showTranslation, TranslationProcessor translationProcessor, boolean configreport) throws SapphireException {
        try {
            if (configreport) {
                ConfigReportContent.copyFile(new File(applicationRoot + "/WEB-CORE/gwt/pageentry/images/StartEnd.png"), new File(folder + "/images/WEB-CORE/gwt/pageentry/images/StartEnd.png"));
                ConfigReportContent.copyFile(new File(applicationRoot + "/WEB-CORE/images/png/decision.png"), new File(folder + "/images/WEB-CORE/images/png/decision.png"));
            }
        }
        catch (Exception e) {
            Trace.log("Failed to copy file /WEB-CORE/images/png/line.png");
        }
        if (srcProcessingScript != null && refProcessingScript != null) {
            if (this.isValidActionBlock(srcProcessingScript) && this.isValidActionBlock(refProcessingScript)) {
                ActionBlock refactionBlock = new ActionBlock();
                if (refProcessingScript.length() > 0) {
                    refactionBlock = new ActionBlock(refProcessingScript);
                }
                ActionBlock srcactionBlock = new ActionBlock(srcProcessingScript);
                DataSet srcds = new DataSet();
                srcds = this.addActionBlockItems(srcds, srcactionBlock, 0, "1", "");
                DataSet refds = new DataSet();
                if (refProcessingScript.length() > 0) {
                    refds = this.addActionBlockItems(refds, refactionBlock, 0, "1", "");
                }
                ConfigReportContent src = this.renderActionBlockDiagram(srcds, configreport);
                ConfigReportContent ref = new ConfigReportContent("refactionblock", translationProcessor);
                if (refProcessingScript.length() > 0) {
                    ref = this.renderActionBlockDiagram(refds, configreport);
                }
                if (src.toString().equals(ref.toString())) {
                    this.content.append(src);
                    this.content.append(this.renderActionBlockDetails(srcactionBlock, translationProcessor));
                } else {
                    this.content.append(ConfigReportContent.getNewString("New:") + (src.length() == 0 ? "Not defined" : src));
                    this.content.append("<P>" + ConfigReportContent.getDeletedString("Old:") + (ref.length() == 0 ? "Not defined" : ref));
                    this.content.append(this.renderActionBlockDetailsDiff(srcactionBlock, refactionBlock, translationProcessor));
                }
            } else if (this.isValidActionBlock(srcProcessingScript)) {
                ActionBlock srcactionBlock = new ActionBlock(srcProcessingScript);
                DataSet srcds = new DataSet();
                srcds = this.addActionBlockItems(srcds, srcactionBlock, 0, "1", "");
                ConfigReportContent src = this.renderActionBlockDiagram(srcds, configreport);
                this.content.append("<P>" + ConfigReportContent.getDeletedString("Old:"));
                this.content.append(refProcessingScript);
                this.content.append("<P>" + ConfigReportContent.getNewString("New:"));
                this.content.append(src);
            } else if (this.isValidActionBlock(refProcessingScript)) {
                ActionBlock refactionBlock = new ActionBlock(refProcessingScript);
                DataSet refds = new DataSet();
                refds = this.addActionBlockItems(refds, refactionBlock, 0, "1", "");
                ConfigReportContent ref = this.renderActionBlockDiagram(refds, configreport);
                this.content.append("<P>" + ConfigReportContent.getDeletedString("Old:"));
                this.content.append(ref);
                this.content.append("<P>" + ConfigReportContent.getNewString("New:"));
                this.content.append(srcProcessingScript);
            } else {
                this.content.append("<P>" + ConfigReportContent.getDiffString(srcProcessingScript, refProcessingScript) + "</P>");
            }
        }
    }

    private PropertyList findActionInActionBlock(String id, String label, String branch, DataSet actionsData) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(COMMANDID, id);
        filter.put("branch", branch);
        DataSet find = actionsData.getFilteredDataSet(filter);
        if (find.getRowCount() > 0) {
            return new PropertyList(find.getString(0, "properties"));
        }
        return null;
    }

    private ConfigReportContent renderActionBlockDetailsDiff(ActionBlock srcActionBlock, ActionBlock refActionBlock, TranslationProcessor translationProcessor) {
        ConfigReportContent s;
        String desc;
        PropertyList properties;
        String label;
        String branch;
        String actionid;
        String name;
        int i;
        ConfigReportContent buffer = new ConfigReportContent("DataFileDef action block", translationProcessor);
        DataSet srcActionBlockData = new DataSet();
        DataSet refActionBlockData = new DataSet();
        this.addActionBlockItems(srcActionBlockData, srcActionBlock, 0, "1", "");
        this.addActionBlockItems(refActionBlockData, refActionBlock, 0, "1", "");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(COMMANDTYPE, COMMANDTYPE_ACTION);
        DataSet srcActions = srcActionBlockData.getFilteredDataSet(filter);
        DataSet refActions = refActionBlockData.getFilteredDataSet(filter);
        for (i = 0; i < srcActions.getRowCount(); ++i) {
            name = srcActions.getString(i, COMMANDID);
            actionid = srcActions.getString(i, COMMANDID);
            branch = srcActions.getString(i, "branch");
            label = srcActions.getString(i, LABEL);
            if (label == null || label.length() == 0) {
                label = actionid;
            }
            properties = new PropertyList();
            try {
                properties.setPropertyList(srcActions.getString(i, "properties"));
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
            PropertyList refPropertyList = this.findActionInActionBlock(name, actionid, branch, refActions);
            desc = "<P>This command invokes action:" + actionid + " with the following properties:";
            if (refPropertyList != null) {
                buffer.startSubHeading(label, desc, ConfigReportContent.generateSectionAnchor(actionid));
                s = buffer.renderPropertyListDiff(properties, refPropertyList, true, translationProcessor);
            } else {
                buffer.startSubHeading(ConfigReportContent.getNewString(label), desc, ConfigReportContent.generateSectionAnchor(actionid));
                s = buffer.renderPropertyListDiff(properties, refPropertyList, true, translationProcessor);
            }
            if (s.length() == 0) {
                s.append("<P>No Properties</P>");
            }
            buffer.append(s.toString());
        }
        for (i = 0; i < refActions.getRowCount(); ++i) {
            name = refActions.getString(i, COMMANDID);
            actionid = refActions.getString(i, COMMANDID);
            branch = refActions.getString(i, "branch");
            label = refActions.getString(i, LABEL);
            if (label == null || label.length() == 0) {
                label = actionid;
            }
            properties = new PropertyList(refActions.getString(i, "properties"));
            PropertyList srcPropertyList = this.findActionInActionBlock(name, actionid, branch, srcActions);
            desc = "<P>This command invokes action:" + actionid + " with the following properties:";
            if (srcPropertyList != null) continue;
            buffer.startSubHeading(ConfigReportContent.getDeletedString(label), desc, ConfigReportContent.generateSectionAnchor(actionid));
            s = buffer.renderPropertyList(properties, true, translationProcessor);
            if (s.length() == 0) {
                s.append("<P>No Properties</P>");
            }
            buffer.append(s.toString());
        }
        return buffer;
    }

    private int getMaxLevel(DataSet actionBlock) {
        DataSet copy;
        if (actionBlock != null && (copy = actionBlock.copy()) != null) {
            copy.sort(LEVEL);
            BigDecimal val = copy.getBigDecimal(copy.getRowCount() - 1, LEVEL);
            if (val != null) {
                return val.intValue();
            }
        }
        return 0;
    }

    private DataSet addActionBlockItems(DataSet dataset, ActionBlock actionBlock, int level, String branch, String currcase) {
        String abTest = actionBlock.getTest();
        String currbranch = branch;
        if (abTest != null && abTest.length() > 0) {
            int r = dataset.addRow();
            dataset.setString(r, COMMANDTYPE, COMMANDTYPE_STARTACTIONBLOCK);
            dataset.setString(r, COMMANDID, COMMANDTYPE_STARTACTIONBLOCK + level);
            dataset.setString(r, TESTCONDITION, abTest);
            dataset.setNumber(r, LEVEL, level + 1);
            dataset.setString(r, "branch", currbranch);
            dataset.setString(r, NAME, actionBlock.getTestName());
            dataset.setString(r, "currcase", currcase);
        }
        int commands = actionBlock.getCommandCount();
        for (int i = 0; i < commands; ++i) {
            Object o = actionBlock.getCommand(i);
            if (o instanceof ActionBlock.Action) {
                ActionBlock.Action action = (ActionBlock.Action)o;
                String name = action.name;
                String actionid = action.actionid;
                String actionlabel = action.actionlabel;
                if (actionlabel == null || actionlabel.length() == 0) {
                    actionlabel = actionid;
                }
                int r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_ACTION);
                dataset.setString(r, COMMANDID, actionid);
                dataset.setString(r, LABEL, actionlabel);
                dataset.setString(r, NAME, name);
                dataset.setNumber(r, LEVEL, level);
                dataset.setString(r, "branch", currbranch);
                dataset.setString(r, "properties", action.properties.toXMLString());
                dataset.setString(r, "currcase", currcase);
                if (i != commands - 1 || level <= 0) continue;
                r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_ENDACTIONBLOCK);
                dataset.setString(r, COMMANDID, COMMANDTYPE_ENDACTIONBLOCK + level);
                dataset.setNumber(r, LEVEL, level);
                dataset.setString(r, "branch", currbranch);
                dataset.setString(r, "currcase", currcase);
                continue;
            }
            if (o instanceof ActionBlock) {
                ActionBlock ab = (ActionBlock)o;
                if (ab.getCaseValue().equals("true")) {
                    currbranch = currbranch + "1";
                    this.addActionBlockItems(dataset, ab, level + 1, currbranch, "true");
                    continue;
                }
                if (ab.getCaseValue().equals("false")) {
                    currbranch = currbranch.substring(0, currbranch.length() - 1) + "0";
                    this.addActionBlockItems(dataset, ab, level + 1, currbranch, "false");
                    continue;
                }
                this.addActionBlockItems(dataset, ab, level, currbranch, "none");
                continue;
            }
            if (o instanceof ActionBlock.BlockProperty) {
                ActionBlock.BlockProperty bp = (ActionBlock.BlockProperty)o;
                int r = dataset.addRow();
                dataset.setString(r, COMMANDTYPE, COMMANDTYPE_BLOCKPROPERTY);
                dataset.setString(r, COMMANDID, bp.propertyid);
                dataset.setString(r, LABEL, bp.value);
                dataset.setNumber(r, LEVEL, level);
                dataset.setString(r, "branch", currbranch);
                continue;
            }
            if (!(o instanceof ActionBlock.ReturnProperty)) continue;
            ActionBlock.ReturnProperty rp = (ActionBlock.ReturnProperty)o;
            int r = dataset.addRow();
            dataset.setString(r, COMMANDTYPE, COMMANDTYPE_RETURNPROPERTY);
            dataset.setString(r, COMMANDID, rp.propertyid);
            dataset.setString(r, LABEL, rp.value);
            dataset.setNumber(r, LEVEL, level);
            dataset.setString(r, "branch", currbranch);
        }
        return dataset;
    }

    private ConfigReportContent renderActionBlockDetails(ActionBlock actionBlock, TranslationProcessor translationProcessor) {
        ConfigReportContent buffer = new ConfigReportContent("DataFileDef actionblock", translationProcessor);
        int commands = actionBlock.getCommandCount();
        for (int i = 0; i < commands; ++i) {
            Object o = actionBlock.getCommand(i);
            if (o instanceof ActionBlock.Action) {
                ActionBlock.Action action = (ActionBlock.Action)o;
                String name = action.name;
                String actionid = action.actionid;
                String versionid = action.versionid;
                String label = action.actionlabel;
                if (label == null || label.length() == 0) {
                    label = actionid;
                }
                PropertyList properties = action.properties;
                String desc = "<P>This command invokes action:" + actionid + " version:" + versionid + " with the following properties:";
                buffer.startSubHeading(label, desc, ConfigReportContent.generateSectionAnchor(actionid));
                buffer.append(buffer.renderPropertyList(properties, true, translationProcessor).toString());
                continue;
            }
            if (!(o instanceof ActionBlock)) continue;
            ActionBlock ab = (ActionBlock)o;
            buffer.append(this.renderActionBlockDetails(ab, translationProcessor).toString());
        }
        return buffer;
    }

    private ConfigReportContent renderActionBlockDiagram(DataSet actionBlock, boolean configreport) {
        ConfigReportContent buffer = new ConfigReportContent("DataFileDef action block diagram", this.translationProcessor);
        buffer.append("<table class=\"gridtable\">\n");
        int maxlevel = this.getMaxLevel(actionBlock);
        if (!configreport) {
            buffer.append("<tr>\n<td colspan=2  height=34 width=200 style=\"border:0px; background: url(WEB-CORE/gwt/pageentry/images/StartEnd.png) no-repeat 0 0;background-width:100%; background-position: center\" valign=\"top\" align=\"center\">&nbsp;&nbsp;&nbsp;&nbsp;Start</td></tr>\n");
        } else {
            buffer.append("<tr>\n<td colspan=2  height=34 width=200 style=\"border:0px; background: url(../images/WEB-CORE/gwt/pageentry/images/StartEnd.png) no-repeat 0 0;background-width:100%; background-position: center\" valign=\"top\" align=\"center\">&nbsp;&nbsp;&nbsp;&nbsp;Start</td></tr>\n");
        }
        buffer.append("<tr><td style=\"border-left:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td></tr>\n");
        for (int i = 0; i < actionBlock.getRowCount(); ++i) {
            String commandType = actionBlock.getString(i, COMMANDTYPE);
            int level = actionBlock.getBigDecimal(i, LEVEL).intValue();
            buffer.append("<TR>");
            if (commandType.equals(COMMANDTYPE_ACTION) || commandType.equals(COMMANDTYPE_BLOCKPROPERTY) || commandType.equals(COMMANDTYPE_RETURNPROPERTY)) {
                int j;
                int j2;
                buffer.append("<!----- rendering a row for -----" + commandType + " ------- -->\n ");
                String currcase = actionBlock.getValue(i, "currcase", "");
                if (currcase.equals("true")) {
                    for (j2 = 0; j2 < level; ++j2) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>false</td>\n");
                    }
                } else {
                    for (j2 = 0; j2 < level - 1; ++j2) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                }
                if (commandType.equals(COMMANDTYPE_ACTION)) {
                    buffer.append("<TD colspan=2 style=\"border-color:lightsteelblue\" width=\"200\">");
                    buffer.append("<table style=\"border-color:lightsteelblue\" width=\"100%\">");
                    String label = actionBlock.getString(i, LABEL);
                    if (label == null || label.length() == 0) {
                        label = actionBlock.getString(i, COMMANDID);
                    }
                    label = ConfigReportContent.createHyperLink(label, actionBlock.getString(i, COMMANDID));
                    buffer.append("<tr><td style=\"border-width:0px\" align=\"center\"><B>" + label + "</B></td></tr>\n");
                    buffer.append("<tr><td style=\"border-width:0px\" align=\"center\"> <I>( " + actionBlock.getString(i, COMMANDID) + ") </I></td></tr>\n");
                    buffer.append("</table>");
                    if (currcase.equals("false")) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                    buffer.append("</td>");
                } else if (commandType.equals(COMMANDTYPE_RETURNPROPERTY)) {
                    buffer.append("<TD colspan=2 style=\"border-color:lightsteelblue;background-color:lightgreen \" width=\"200\">");
                    buffer.append("<table style=\"border-color:lightsteelblue\" width=\"100%\">");
                    buffer.append("<tr><td style=\"border-width:0px;background-color:lightgreen\"  align=\"center\"><B>" + actionBlock.getString(i, COMMANDID) + "</B></td></tr>\n");
                    buffer.append("<tr><td style=\"border-width:0px;background-color:lightgreen\"  align=\"center\"><I>Value " + actionBlock.getString(i, LABEL) + "</I></td></tr>\n");
                    buffer.append("</table>");
                    buffer.append("</td>");
                } else {
                    buffer.append("<TD colspan=2 style=\"border-color:lightsteelblue;background-color:cornsilk \" width=\"200\">");
                    buffer.append("<table style=\"border-color:lightsteelblue\" width=\"100%\">");
                    buffer.append("<tr><td style=\"border-width:0px;background-color:cornsilk\"  align=\"center\"><B>" + actionBlock.getString(i, COMMANDID) + "</B></td></tr>\n");
                    buffer.append("<tr><td style=\"border-width:0px;background-color:cornsilk\"  align=\"center\"><I>Value " + actionBlock.getString(i, LABEL) + "</I></td></tr>\n");
                    buffer.append("</table>");
                    buffer.append("</td>");
                }
                for (j = level; j < maxlevel + 1; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
                if (i + 1 < actionBlock.getRowCount() && actionBlock.getString(i + 1, COMMANDTYPE).equals(COMMANDTYPE_STARTACTIONBLOCK)) {
                    buffer.append("<TR>\n");
                    for (j = 0; j < level - 1; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        if (j == level - 2) {
                            buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                            continue;
                        }
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                    }
                    buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
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
                    buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                    if (j == level - 1) {
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        continue;
                    }
                    buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                }
                String testname = actionBlock.getString(i, NAME);
                String condition = actionBlock.getString(i, TESTCONDITION);
                if ((testname == null || testname.length() == 0) && condition.length() > 0 && condition.startsWith("$G{")) {
                    testname = condition.substring(3, condition.length() - 1);
                }
                if (!configreport) {
                    buffer.append("<td colspan=4 width=400 height=100 style=\"border:0px; background: url(WEB-CORE/images/png/decision.png) no-repeat 0 0;background-width:100%\" align='left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + testname + "</td>\n");
                } else {
                    buffer.append("<td colspan=4 width=400 height=100 style=\"border:0px; background: url(../images/WEB-CORE/images/png/decision.png) no-repeat 0 0;background-width:100%\" align='left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + testname + "</td>\n");
                }
                for (int j = level; j < maxlevel; ++j) {
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                }
            } else if (commandType.equals(COMMANDTYPE_ENDACTIONBLOCK)) {
                buffer.append("<!----- rendering a row for -----" + commandType + " ------- -->\n ");
                if (!actionBlock.getValue(i, "currcase", "").equals("true")) {
                    int j;
                    for (j = 0; j < level - 1; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
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
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                    buffer.append("<td style=\"border-left:0px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>");
                    for (j = level; j < maxlevel; ++j) {
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    }
                } else if (i + 1 == actionBlock.getRowCount() || !actionBlock.getValue(i + 1, "currcase", "").equals("false")) {
                    int j;
                    for (j = 0; j < level - 1; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                    buffer.append("<td style=\"border-left:0px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-left:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-left:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>");
                    buffer.append("<!----- rendering a spacing row for -----" + commandType + " ------- -->\n ");
                    buffer.append("</TR>\n");
                    buffer.append("<TR>\n");
                    for (j = 0; j < level; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                } else {
                    int j;
                    for (j = 0; j < level + 1; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                    buffer.append("<!----- rendering a spacing row for -----" + commandType + " ------- -->\n ");
                    buffer.append("</TR>\n");
                    buffer.append("<TR>\n");
                    for (j = 0; j < level; ++j) {
                        buffer.append("<td style=\"border-left:0px; border-right:1px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100></td>\n");
                    }
                    buffer.append("<td style=\"border-left:0px; border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>\n\t<td style=\"border:0px\" width=100>&nbsp;</td>");
                    for (j = level; j < maxlevel; ++j) {
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                        buffer.append("<td style=\"border:0px\" width=100>&nbsp;</td>\n");
                    }
                }
            }
            buffer.append("</TR>\n");
        }
        buffer.append("<tr><td style=\"border-left:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td>\n");
        buffer.append("<td style=\"border-right:0px;border-bottom:0px;border-top:0px\" width=100>&nbsp;</td></tr>\n");
        if (!configreport) {
            buffer.append("<tr>\n<td colspan=2 width=200 height=34 style=\"border:0px; background: url(WEB-CORE/gwt/pageentry/images/StartEnd.png) no-repeat 0 0;background-width:100%; background-position: center\" valign=\"top\" align=\"center\">&nbsp;&nbsp;&nbsp;&nbsp;End</td></tr>\n");
        } else {
            buffer.append("<tr>\n<td colspan=2 width=200 height=34 style=\"border:0px; background: url(../images/WEB-CORE/gwt/pageentry/images/StartEnd.png) no-repeat 0 0;background-width:100%; background-position: center\" valign=\"top\" align=\"center\">&nbsp;&nbsp;&nbsp;&nbsp;End</td></tr>\n");
        }
        buffer.append("</table>\n");
        return buffer;
    }

    public int findRow(DataSet ds, HashMap filter) {
        Object[] columnsToCompare = filter.keySet().toArray();
        boolean found = true;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            boolean match = true;
            for (int j = 0; j < columnsToCompare.length; ++j) {
                String dscolumnValue;
                String filtercolumnValue = filter.get(columnsToCompare[j]).toString();
                if (filtercolumnValue.equals(dscolumnValue = ds.getValue(i, columnsToCompare[j].toString(), ""))) continue;
                match = false;
                break;
            }
            if (!match) continue;
            return i;
        }
        return -1;
    }

    public void startBulletList() {
        this.content.append("<UL>\n");
    }

    public void addBullet(String str) {
        this.content.append("<LI >\n");
        this.content.append(str);
        this.content.append("</LI>\n");
    }

    public void addDiffBullet(String str, String refStr) {
        if (str.equals(refStr)) {
            this.addBullet(str);
            return;
        }
        this.foundDiff = true;
        this.content.append("<LI >\n");
        this.content.append("<diffpoint/><font class=\"diffreportnewitem\">");
        this.content.append(str);
        this.content.append("</font>");
        this.content.append("<font class=\"diffreportdeleteditem\">");
        this.content.append(refStr);
        this.content.append("</font>");
        this.content.append("</LI>\n");
    }

    public void endBulletList() {
        this.content.append("</UL>\n");
    }

    public static String generateSubSectionFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + "_SubSections.html";
    }

    public static String generateSectionFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + ".html";
    }

    public void appendSpecialContent(ConfigReportContent content, boolean diffonly) {
        DataSet diffs = content.diffInfo;
        for (int i = 0; i < diffs.size(); ++i) {
            this.diffInfo.copyRow(diffs, i, 1);
        }
        this.foundDiff |= content.getFoundDiff();
        this.content.append(content.toString());
    }

    public void appendSubSection(ConfigReportContent subSectionContent, String subSection, boolean diffOnly) {
        this.foundDiff |= subSectionContent.getFoundDiff();
        int row = this.diffInfo.addRow();
        this.diffInfo.setString(row, "SubSection", ConfigReportContent.generateSectionTitle(subSection));
        this.diffInfo.setString(row, "SubSectionURL", "#" + ConfigReportContent.convertToID(subSection));
        this.diffInfo.setString(row, "Status", subSectionContent.getFoundDiff() ? "Modified" : "None");
        this.content.append(subSectionContent.toString());
    }

    public void renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, PropertyListCollection ignoreCols, TranslationProcessor translationProcessor) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            this.startListTableTop();
            if (ref == null) {
                this.addListItems(ds, translationProcessor);
            } else {
                this.addDiffListItems(ds, ref, keycols, ignoreCols, translationProcessor, false);
            }
            this.endListTable();
        }
    }

    public void renderDiffListTable(DataSet ds, DataSet ref, String[] keycols) {
        this.renderDiffListTable(ds, ref, keycols, true, false);
    }

    public void renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, boolean top, boolean hideEmptyColumns) {
        if (ds != null && ds.getRowCount() > 0 && !this.checkIfEmpty(ds)) {
            if (top) {
                this.startListTableTop();
            } else {
                this.startListTableInner();
            }
            if (ref == null) {
                this.addListItems(ds, this.translationProcessor, "", hideEmptyColumns);
            } else {
                this.addDiffListItems(ds, ref, keycols, this.translationProcessor, hideEmptyColumns);
            }
            this.endListTable();
        }
    }

    public void renderDiffMatrix(DataSet matrix, DataSet refMatrix, String[] keycols) {
        if (matrix == null || matrix.getRowCount() == 0) {
            this.content.append("Not found.");
            return;
        }
        this.startListTableTop();
        this.addDiffListItems(matrix, refMatrix == null ? new DataSet() : refMatrix, keycols, this.translationProcessor, false);
        this.endListTable();
    }

    public ConfigReportContent startChapter(String chapterNo, String sdcId, String preamble) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, this.context + " Chapter :" + sdcId);
        buffer.pageBreak();
        String str = "";
        str = str + "<H1 id=\"CHAPTER" + sdcId + "\">Chapter " + chapterNo + " " + sdcId + "</H1>\n";
        str = str + "<P>\n";
        str = str + preamble;
        str = str + "</P>\n";
        buffer.append(str);
        return buffer;
    }

    public void pageBreak() {
        this.content.append("<DIV style=\"page-break-before: always;\" /> \n ");
    }

    public StringBuffer endChapter(String sdcId) {
        String str = "";
        return new StringBuffer(str);
    }

    public void startFile() {
        String str = "";
        str = "<HTML>\n<HEAD>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE></TITLE>\n<link rel=\"stylesheet\" href=\"stylesheets/configreport.css\" type=\"text/css\">\n</HEAD>\n<BODY>";
        this.content.append(str);
    }

    public void startFile(String subSectionFileName) {
        String str = "";
        str = "<HTML>\n<HEAD>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE></TITLE>\n<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\">\n</HEAD>\n<BODY onload=\"onLoad()\">\n<script>\nfunction onLoad() { parent.SubSection.location = \"" + subSectionFileName + "\";  }</script>";
        this.content.append(str);
    }

    public void endFile() {
        String str = "</BODY>\n";
        str = str + "</HTML>\n";
        this.content.append(str);
    }

    public String insertDiffAnchors() {
        StringBuilder content = new StringBuilder(this.content.toString());
        int count = 0;
        int pos = 0;
        while (pos >= 0) {
            String anchor = "<diffpoint/>";
            if ((pos = content.indexOf(anchor, pos + 1)) < 0) continue;
            content.replace(pos, pos + anchor.length(), "<diffpoint id=\"diffpoint" + count + "\"/>");
            ++count;
        }
        if (count == 0) {
            this.content = new StringBuffer(content.toString());
            return this.content.toString();
        }
        String javascript = "<script>var diffpointcounter=-1;function doonload() {  window.focus(); onLoad();}function bodykeydown() {if ( event.keyCode==40&&event.ctrlKey) {diffpointcounter ++;var e = document.getElementById( 'diffpoint' + diffpointcounter );if (e!=null){e.scrollIntoView();}else {diffpointcounter--}}if ( event.keyCode==38&&event.ctrlKey) {diffpointcounter --;var e = document.getElementById( 'diffpoint' + diffpointcounter );if (e!=null){e.scrollIntoView();}else {diffpointcounter++}}}function nextdiffpoint() {  diffpointcounter ++;  var e = document.getElementById( 'diffpoint_' + diffpointcounter );  if (e!=null) e.scrollIntoView();}document.body.onkeydown=bodykeydown;window.onload=doonload;</script>";
        this.content = new StringBuffer(javascript + content.toString());
        return this.content.toString();
    }

    public DataSet getMenuMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns) {
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

    public void renderRoleMatrix(DataSet roleMatrix, int keycols) {
        if (roleMatrix == null || roleMatrix.getRowCount() == 0) {
            this.content.append("No roles found.");
            return;
        }
        this.addRoleMatrix(roleMatrix, keycols);
    }

    public void addRoleMatrix(DataSet roleMatrix, int keycols) {
        int i;
        this.startListTableTop();
        int rows = roleMatrix.getRowCount();
        int cols = roleMatrix.getColumnCount();
        String[] colList = roleMatrix.getColumns();
        this.startHeader();
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            String modTitle = colList[i];
            modTitle = i < keycols ? colList[i].substring(0, 1).toUpperCase() + colList[i].substring(1) : "<div STYLE=\"writing-mode:vertical-rl;\" >" + modTitle + "</div>\n";
            this.content.append("<th class=\"viewlisthead\">" + modTitle + "</th>\n");
        }
        this.endHeader();
        for (i = 0; i < rows; ++i) {
            HashMap currrow = (HashMap)roleMatrix.get(i);
            if (currrow == null || currrow.size() <= 0) continue;
            this.content.append("<TR VALIGN=TOP >\n");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = roleMatrix.getValue(i, colList[j]);
                this.content.append("<td class=\"viewlistcol\">\n");
                this.content.append(columnValue);
                this.content.append("</td>\n");
            }
            this.content.append("</TR>\n");
        }
        this.endListTable();
    }

    public static String renderLink(String link, HashMap sdisIncluded, boolean frames, String connectionid) {
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
            if (ConfigReportContent.isFKIncluded("WebPage", webpageId, sdisIncluded)) {
                String href;
                SDI sdi = new SDI("WebPage", webpageId, ConfigReportContent.getPageEdition(connectionid, webpageId), "");
                String anchor = ConfigReportContent.generateSDISectionAnchor(sdi);
                if (!frames) {
                    href = "#" + anchor;
                } else {
                    String sectionFileName = ConfigReportContent.generateSDISectionFileName(sdi);
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

    public static String generateSDISectionFileName(SDI currentSDI) {
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
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + ".html";
    }

    public void renderDiffRoleMatrix(DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols) {
        this.startListTableTop();
        roleMatrix = this.getMatrixDiffInfo(roleMatrix, refRoleMatrix, keycols);
        this.renderRoleMatrix(roleMatrix, keycols.length);
        this.endListTable();
    }

    public void renderDiffRoleMatrix(DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols, boolean sortby) {
        this.startListTableTop();
        roleMatrix = this.getMatrixDiffInfo(roleMatrix, refRoleMatrix, keycols, sortby);
        this.renderRoleMatrix(roleMatrix, keycols.length);
        this.endListTable();
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
                    diffRoleMatrix.setString(currRow, keyColumns[j], ConfigReportContent.getNewString(roleMatrix.getString(i, keyColumns[j], "")));
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
                diffRoleMatrix.setString(currRow, keyColumns[j], ConfigReportContent.getDeletedString(refRoleMatrix.getString(i, keyColumns[j], "")));
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

    public StringBuffer addDiffRowItem(String columnName, String columnVal, String refVal) {
        return this.addDiffRowItem(columnName, columnVal, refVal, this.translationProcessor);
    }

    public ConfigReportContent renderPropertyList(PropertyList pl, boolean top) {
        return this.renderPropertyList(pl, top, this.translationProcessor);
    }

    public ConfigReportContent renderPropertyListDiff(PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, boolean hideEmptyColumns) {
        return this.renderPropertyListDiff(pl, refPl, defList, reportAdvancedProperties, top, this.translationProcessor, hideEmptyColumns);
    }

    public static String createHyperLink(String sdcid, String fkkeyid1, String fkkeyid2, String fkkeyid3, HashMap sdisIncluded, boolean frames) {
        if (fkkeyid1 == null || fkkeyid1.length() == 0) {
            return "";
        }
        if (sdisIncluded == null) {
            return fkkeyid1;
        }
        if (ConfigReportContent.isFKIncluded(sdcid, fkkeyid1, fkkeyid2, fkkeyid3, sdisIncluded)) {
            SDI sdi = new SDI(sdcid, fkkeyid1, fkkeyid2, fkkeyid3);
            String anchor = ConfigReportContent.generateSDISectionAnchor(sdi);
            String href = "";
            if (!frames) {
                href = "#" + anchor;
            } else {
                String sectionFileName = ConfigReportContent.generateSDISectionFileName(sdi);
                href = sectionFileName + "#" + anchor;
            }
            return "<A HREF=\"" + href + "\">" + fkkeyid1 + "</A>";
        }
        return fkkeyid1;
    }

    public static String getPageName(String link) {
        int pagePos = link.toLowerCase().indexOf("page=");
        if (pagePos >= 0) {
            int amp = link.indexOf("&", pagePos);
            return amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp);
        }
        return "";
    }

    public static String getWizardName(String link) {
        int wizPos = link.toLowerCase().indexOf("wizard=");
        if (wizPos >= 0) {
            int amp = link.indexOf("&", wizPos);
            return amp == -1 ? link.substring(wizPos + 7) : link.substring(wizPos + 7, amp);
        }
        return "";
    }

    public static String getFileName(String link) {
        int filepos = link.toLowerCase().indexOf("file=");
        if (filepos >= 0) {
            int amp = link.indexOf("&", filepos);
            return amp == -1 ? link.substring(filepos + 5) : link.substring(filepos + 5, amp);
        }
        return "";
    }

    public void setFoundDiff(boolean val) {
        this.foundDiff = val;
    }

    public ConfigReportContent renderCollection(PropertyListCollection coll, boolean top) {
        return this.renderCollection(coll, top, this.translationProcessor);
    }

    public ConfigReportContent renderPropertyList(PropertyList pl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor) {
        if (defList == null || defList.size() == 0) {
            return this.renderPropertyList(pl, top);
        }
        ConfigReportContent tempTable = new ConfigReportContent(this.context, translationProcessor);
        if (pl.size() == 0) {
            return tempTable;
        }
        ConfigReportContent attrContent = this.renderPropertyListAttributesDiff(pl, pl);
        for (PropertyDefinition propDef : defList) {
            String p;
            PropertyDefinitionList currPropDefList;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (pl.isPropertyList(currPropId)) {
                ConfigReportContent propertylist;
                currPropDefList = propDef.getPropertyDefinitionList();
                PropertyList cld = pl.getPropertyList(currPropId);
                if (cld.size() <= 0 || (propertylist = this.renderPropertyList(cld, currPropDefList, reportAdvancedProperties, false, translationProcessor)).length() <= 0) continue;
                tempTable.startRow();
                tempTable.addRowItem(currPropTitle, propertylist.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (pl.isCollection(currPropId)) {
                currPropDefList = propDef.getPropertyDefinitionList();
                ConfigReportContent collection = this.renderCollection(pl.getCollection(currPropId), currPropDefList, reportAdvancedProperties, false, translationProcessor);
                if (collection.length() <= 0) continue;
                tempTable.startRow();
                tempTable.addRowItem(currPropTitle, collection.toString(), translationProcessor);
                tempTable.endRow();
                continue;
            }
            if (currPropId.startsWith("__") || (p = pl.getProperty(currPropId, "")).length() <= 0) continue;
            tempTable.startRow();
            tempTable.addRowItem(currPropTitle, p, translationProcessor);
            tempTable.endRow();
        }
        ConfigReportContent table = new ConfigReportContent(this.config, this.context);
        if (tempTable.length() > 0) {
            table.append(top ? this.getStartTableTop() : this.getStartTableInner());
            if (attrContent.length() > 0) {
                table.append(attrContent.toString());
            }
            table.append(tempTable.toString());
            table.append(this.getEndTable());
        }
        return table;
    }

    public static String generateTOCFileName(String chapterName) {
        String tocFileName = chapterName.trim().replaceAll(" ", "_");
        tocFileName = tocFileName.trim().replaceAll("/", "");
        return tocFileName + "_TOC.html";
    }

    public static String generateTOCXMLFileName(String chapterName) {
        String tocFileName = chapterName.trim().replaceAll(" ", "_");
        return tocFileName + "_TOC.xml";
    }

    public void markAsDeleted() {
        this.content = new StringBuffer(ConfigReportContent.getDeletedString(this.content.toString()));
    }

    public void markAsNew() {
        this.content = new StringBuffer(ConfigReportContent.getNewString(this.content.toString()));
    }

    public static String generateSDISubSectionFileName(SDI currentSDI) {
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
        sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
        return sectionFileName + "_SubSections.html";
    }

    public static String changeImageFolder(String imageURL, String folder, String applicationRoot) throws SapphireException {
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
            String srcImageDir = applicationRoot;
            try {
                ConfigReportContent.copyFile(new File(srcImageDir + srcval), new File(folder + "/images/" + srcval));
                newURL = ConfigReportContent.changeHTMLtags("<img src=\"../images/" + srcval + "\"/>");
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return newURL;
    }

    public static PropertyList parseDisplayValues(String displayValue, String folder, String applicationRoot) throws SapphireException {
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
                    rhs = ConfigReportContent.changeImageFolder(rhs, folder, applicationRoot);
                }
                p.setProperty(lhs, rhs);
                continue;
            }
            if (options[i].length() <= 0) continue;
            p.setProperty("Default", options[i]);
        }
        return p;
    }

    public void startReport(int chapterCount, String firstChapter, boolean hideChapterTOC, boolean hideSubsections) throws SapphireException {
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/modules/configreport/stylesheets/configreport.css"), new File(this.folder + "/stylesheets/configreport.css"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy file", e);
        }
        if (!this.frames) {
            this.startFile();
        } else {
            this.content.append(this.getFramesReport(chapterCount, firstChapter, hideChapterTOC, hideSubsections));
        }
    }

    public void endReport(StringBuffer buffer) throws SapphireException {
        buffer.append("</BODY></HTML>");
    }

    private ConfigReportContent getFramesReport(int chapterCount, String firstChapter, boolean hideChapter, boolean hideSubsections) {
        String sub;
        ConfigReportContent str = new ConfigReportContent(this.config, "Start Report");
        str.append("<HTML>\n");
        str.append("<HEAD>\n");
        str.append("<TITLE>Configuration Report</TITLE>\n");
        str.append("<link rel=\"stylesheet\" href=\"../stylesheets/configreport.css\" type=\"text/css\">\n");
        str.append("</HEAD>\n");
        str.append("<script>var _firstloading=true;</script><FRAMESET cols=\"20%, 80%\">");
        if (!hideChapter || chapterCount > 1) {
            str.append("  <FRAMESET rows=\"100, 200\">\n");
            str.append("      <FRAME name=\"Chapter\" src=\"html" + File.separator + "TOC.html\">\n");
            sub = "      <FRAME name=\"Section\" src=\"html" + File.separator + ConfigReportContent.generateTOCFileName(firstChapter) + "\">\n";
            str.append(sub);
            str.append("  </FRAMESET>\n");
        } else {
            sub = " <FRAME name=\"Section\" src=\"html" + File.separator + ConfigReportContent.generateTOCFileName(firstChapter) + "\">\n";
            str.append(sub);
        }
        if (!hideSubsections) {
            str.append("<FRAMESET rows=\"40, 160\">\n");
            sub = "<FRAME name=\"SubSection\" style=\"frame-border:0\" src=\"html" + File.separator + ConfigReportContent.generateSubSectionFileName("Cover", "Page") + "\">\n";
            str.append(sub);
            str.append("<FRAME name=\"ChapterContent\" style=\"frame-border:0\" src=\"html" + File.separator + "Cover_Page.html\"> ");
            str.append("</FRAMESET>\n");
        } else {
            str.append("<FRAME name=\"ChapterContent\" style=\"frame-border:0\" src=\"html" + File.separator + "Cover_Page.html\"> ");
        }
        str.append("  </FRAMESET>\n");
        str.append("</HTML>");
        return str;
    }

    public static PropertyList parseValidationValues(String validation) {
        PropertyList ret = new PropertyList();
        if (validation.trim().length() > 0) {
            if (!validation.startsWith("$G")) {
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
                    if (curr.indexOf("(") > -1) {
                        String lhs = curr.substring(0, curr.indexOf("("));
                        String rhs = curr.substring(curr.indexOf("(") + 1, curr.indexOf(")"));
                        ret.setProperty("Range:", lhs + " range: " + rhs);
                        continue;
                    }
                    Trace.log("range does not have a (");
                }
            } else {
                ret.setProperty("Groovy", HttpUtil.htmlEncode(validation.substring(3, validation.length() - 1)));
            }
        }
        return ret;
    }

    public static DataSet parseDisplayValues(String displayValue) throws SapphireException {
        DataSet p = new DataSet();
        p.addColumn("value", 0);
        p.addColumn("displayvalue", 0);
        String[] options = StringUtil.split(displayValue, ";");
        for (int i = 0; i < options.length; ++i) {
            if (options[i].indexOf("=") > -1) {
                String lhs = "Others";
                if (options[i].indexOf("=") != 0) {
                    lhs = options[i].substring(0, options[i].indexOf("="));
                }
                String rhs = options[i].substring(options[i].indexOf("=") + 1);
                int r = p.addRow();
                p.setValue(r, "value", lhs);
                p.setValue(r, "displayvalue", rhs);
                continue;
            }
            if (options[i].length() <= 0) continue;
            int r = p.addRow();
            p.setValue(r, "value", "Default");
            p.setValue(r, "displayvalue", options[i]);
        }
        return p;
    }

    public static String getRefTypeValue(QueryProcessor queryProcessor, String reftypeid, String value) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select refvalueid, refdisplayvalue from refvalue where reftypeid = " + safeSQL.addVar(reftypeid) + " and refvalueid=" + safeSQL.addVar(value);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "refdisplayvalue", value);
        }
        return value;
    }

    public static String changeHTMLtags(String value) {
        return value.replaceAll("<img", "&#60;img");
    }
}

