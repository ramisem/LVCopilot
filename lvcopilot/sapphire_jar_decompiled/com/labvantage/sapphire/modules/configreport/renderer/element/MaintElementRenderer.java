/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintElementRenderer
extends BaseElementRenderer {
    boolean includeDiffReport = false;
    boolean reportHiddenColumns = false;

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean renderAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "maint element");
        this.includeDiffReport = includeDiffReport;
        this.reportHiddenColumns = reportHiddenColumns;
        try {
            configReportContent.startSubSection(elementId, "");
            configReportContent.appendSubSection(this.renderSummary(elementProps, refElementProps, includeDiffReport), elementId + " Summary", this.diffOnly);
            String style = elementProps.getProperty("style");
            boolean grouped = false;
            if (style.equals("FormWithTabGroups") || style.equals("FormWithFieldGroups")) {
                grouped = true;
            }
            configReportContent.append(this.renderMaintPageColumns(elementProps, refElementProps, grouped, includeDiffReport).toString());
            configReportContent.append(this.renderRoleMatrix(elementProps, refElementProps, includeDiffReport).toString());
            configReportContent.endSubSection("", elementId);
        }
        catch (SapphireException e) {
            Trace.logError("Failed to report maint element", e);
            configReportContent.append("<P color=red>Error: failed to render toolbar info</P>");
        }
        return configReportContent;
    }

    private ConfigReportContent renderRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "maint element column role matrix");
        DataSet columnRoles = this.getColumnRoleMatrix(elementProps);
        configReportContent.startSubHeading("Role Matrix", "");
        if (!includeDiffReport || refElementProps == null) {
            if (columnRoles.getRowCount() > 0 && columnRoles.getColumnCount() > 2) {
                configReportContent.renderListTable(columnRoles, this.translationProcessor);
            }
        } else {
            DataSet refColumnRoles = this.getColumnRoleMatrix(refElementProps);
            String[] keycols = new String[]{"Column"};
            configReportContent.renderDiffRoleMatrix(columnRoles, refColumnRoles, keycols);
        }
        configReportContent.endSubSection("", "");
        return configReportContent;
    }

    private ConfigReportContent renderSummary(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "maint element summary");
        String viewonly = this.getViewOnly(elementProps);
        if (refElementProps != null && includeDiffReport) {
            String refviewonly = this.getViewOnly(refElementProps);
            ConfigReportContent.getDiffString(viewonly, refviewonly);
        } else {
            configReportContent.append(viewonly);
        }
        return configReportContent;
    }

    private String getViewOnly(PropertyList props) {
        String viewonly = props.getProperty("viewonly");
        String sdcid = props.getProperty("sdcid");
        if ("Y".equals(viewonly)) {
            return "<P>This is a View page for " + sdcid + " SDC.<P> ";
        }
        return "<P>This is a Edit page for " + sdcid + " SDC.<P> ";
    }

    private Object[] getGroupList(PropertyListCollection columns) {
        HashSet<String> groups = new HashSet<String>();
        for (PropertyList columnProps : columns) {
            String mode = columnProps.getProperty("mode");
            boolean include = this.reportHiddenColumns | !mode.equals("hidden");
            if (!include || columnProps.getProperty("groupid", "").length() <= 0) continue;
            groups.add(columnProps.getProperty("groupid", ""));
        }
        return groups.toArray();
    }

    private ConfigReportContent renderMaintPageColumns(PropertyList maint, PropertyList refMaint, boolean grouped, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "maint element columns");
        PropertyListCollection columns = maint.getCollectionNotNull("columns");
        PropertyListCollection refColumns = null;
        if (includeDiffReport && refMaint != null) {
            refColumns = refMaint.getCollectionNotNull("columns");
        }
        DataSet maintPageCols = new DataSet();
        maintPageCols.setColidCaseSensitive(true);
        maintPageCols.addColumn("Column", 0);
        maintPageCols.addColumn("Title", 0);
        maintPageCols.addColumn("Default value", 0);
        maintPageCols.addColumn("Validation", 0);
        maintPageCols.addColumn("Mode", 0);
        maintPageCols.addColumn("Display value", 0);
        Object[] groupList = this.getGroupList(columns);
        if (!grouped) {
            if (!includeDiffReport || refColumns == null) {
                configReportContent.appendSubSection(this.renderGroupColumns(maint, columns, "all"), "Maint Element Columns", this.diffOnly);
            } else {
                DataSet colsDS = this.getGroupColumns(maint, columns, "all");
                DataSet refColsDS = this.getGroupColumns(maint, refColumns, "all");
                String[] keycols = new String[]{"Column"};
                configReportContent.renderDiffListTable(colsDS, refColsDS, keycols);
            }
        } else if (!includeDiffReport || refColumns == null) {
            configReportContent.append(this.renderGroupColumns(maint, columns, "").toString());
            for (int i = 0; i < groupList.length; ++i) {
                configReportContent.append(this.renderGroupColumns(maint, columns, groupList[i].toString()).toString());
            }
        } else {
            String[] keycols = new String[]{"Column"};
            DataSet colsDS = this.getGroupColumns(maint, columns, "");
            DataSet refColsDS = this.getGroupColumns(maint, refColumns, "");
            configReportContent.renderDiffListTable(colsDS, refColsDS, keycols);
            for (int i = 0; i < groupList.length; ++i) {
                colsDS = this.getGroupColumns(maint, columns, groupList[i].toString());
                refColsDS = this.getGroupColumns(maint, refColumns, groupList[i].toString());
                configReportContent.renderDiffListTable(colsDS, refColsDS, keycols);
            }
        }
        return configReportContent;
    }

    private ConfigReportContent renderGroupColumns(PropertyList elementProps, PropertyListCollection columns, String groupid) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "maint element columns");
        DataSet maintPageCols = this.getGroupColumns(elementProps, columns, groupid);
        if (groupid.length() > 0 && !groupid.equals("all")) {
            configReportContent.append("<B><P>Tab:" + groupid + "</B><P>");
        }
        configReportContent.renderListTable(maintPageCols, this.translationProcessor);
        return configReportContent;
    }

    private DataSet getGroupColumns(PropertyList elementProps, PropertyListCollection columns, String groupid) throws SapphireException {
        DataSet maintPageCols = new DataSet();
        maintPageCols.setColidCaseSensitive(true);
        for (PropertyList columnProps : columns) {
            String mode = columnProps.getProperty("mode");
            String currgroupid = columnProps.getProperty("groupid", "");
            boolean include = this.reportHiddenColumns | !mode.equals("hidden");
            if (!include || !groupid.equals("all") && !currgroupid.equals(groupid)) continue;
            int row = maintPageCols.addRow();
            maintPageCols.setString(row, "Column", columnProps.getProperty("columnid"));
            maintPageCols.setString(row, "Title", columnProps.getProperty("title"));
            String roles = columnProps.getAttribute("rolelist").replaceAll(";", ", ");
            if (roles.length() > 0) {
                if (!maintPageCols.isValidColumn("Roles")) {
                    maintPageCols.addColumn("Roles", 0);
                }
                maintPageCols.setString(row, "Roles", roles);
            }
            String modeVal = mode;
            if (mode.equals("dropdownlist")) {
                String refid = columnProps.getProperty("reftypeid", "");
                String ddsql = columnProps.getProperty("sql", "");
                String ddvals = columnProps.getProperty("dropdownvalues", "");
                String val = "";
                if (refid.length() > 0) {
                    String fk = ConfigReportContent.createHyperLink("RefType", refid, "", "", this.sdisIncluded, this.frames);
                    val = "Reference Type:" + fk;
                } else if (ddsql.length() > 0) {
                    val = "SQL: " + ddsql;
                } else if (ddvals.length() > 0) {
                    val = "Values: " + ddvals.replaceAll(";", ", ");
                }
                if (val.length() > 0) {
                    modeVal = mode + "(" + val + ")";
                }
            } else if (mode.equals("lookup")) {
                String link = columnProps.getPropertyListNotNull("lookuplink").getProperty("href");
                modeVal = mode + "(" + ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection) + ")";
            } else {
                String pseudo = columnProps.getProperty("pseudocolumn", "");
                if (pseudo.length() > 0) {
                    modeVal = "(Custom)";
                }
            }
            maintPageCols.setString(row, "Mode", modeVal);
            String displayVal = columnProps.getProperty("displayvalue");
            if (displayVal.length() > 0) {
                displayVal = ConfigReportContent.parseDisplayValues(displayVal, this.folder, this.applicationRoot).toXMLString();
                try {
                    FileWriter file = new FileWriter("C:\\temp\\displayvalues.txt", true);
                    Trace.logDebug("PARSEDISPLAYVALUE: sdcid:" + elementProps.getProperty("sdcid", "") + " pageid:" + elementProps.getProperty("pageid", elementProps.getProperty("webpageid", "")) + " columnid:" + columnProps.getProperty("columnid"));
                    String line = "PARSEDISPLAYVALUE: sdcid:" + elementProps.getProperty("sdcid", "") + "| pageid:" + elementProps.getProperty("pageid", elementProps.getProperty("webpageid", "")) + "| columnid:" + columnProps.getProperty("columnid") + "|displaystring:" + columnProps.getProperty("displayvalue");
                    file.append(line + "\n");
                    file.close();
                }
                catch (IOException e) {
                    Trace.logDebug("Cannot write to file");
                }
            }
            maintPageCols.setString(row, "Display value", displayVal);
            maintPageCols.setString(row, "Default value", columnProps.getProperty("defaultvalue"));
            String validation = columnProps.getProperty("validation", "");
            if (validation.length() > 0) {
                PropertyList validationProps = ConfigReportContent.parseValidationValues(validation);
                validation = validationProps.getProperty("Groovy", "").length() > 0 ? "Groovy:" + validationProps.getProperty("Groovy") : validationProps.toXMLString();
            }
            maintPageCols.setString(row, "Validation", validation);
        }
        return maintPageCols;
    }

    private String renderDisplayValue(String displayValue) throws SapphireException {
        if (displayValue == null || displayValue.length() == 0) {
            return "";
        }
        ConfigReportContent buffer = new ConfigReportContent(this.config, "maint element display value");
        String[] options = StringUtil.split(displayValue, ";");
        buffer.startTableInner();
        for (int i = 0; i < options.length; ++i) {
            if (options[i].indexOf("=") > -1) {
                String rhs;
                String lhs = "Others";
                if (options[i].indexOf("=") != 0) {
                    lhs = options[i].substring(0, options[i].indexOf("="));
                }
                if ((rhs = options[i].substring(options[i].indexOf("=") + 1)).indexOf("img") > -1 || rhs.indexOf("IMG") > -1) {
                    rhs = ConfigReportContent.changeImageFolder(rhs, this.folder, this.applicationRoot);
                }
                buffer.startRow();
                buffer.addRowItem(lhs, rhs, this.translationProcessor);
                buffer.endRow();
                continue;
            }
            buffer.startRow();
            buffer.addRowItem(options[i], "", this.translationProcessor);
            buffer.endRow();
        }
        buffer.endTable();
        return buffer.toString();
    }

    public DataSet getColumnRoleMatrix(PropertyList elementProps) throws SapphireException {
        PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
        boolean hasColumns = false;
        for (PropertyList column : columns) {
            hasColumns |= !column.getProperty("mode").equals("Hidden Value");
        }
        DataSet columnRoles = new DataSet();
        columnRoles.setColidCaseSensitive(true);
        columnRoles.addColumn("Column", 0);
        columnRoles.addColumn("All", 0);
        if (hasColumns) {
            int row = -1;
            for (PropertyList columnProps : columns) {
                String mode = columnProps.getProperty("mode");
                if (mode.equals("Hidden Value")) continue;
                ++row;
                columnRoles.addRow();
                String id = "( " + columnProps.getProperty("id") + " )";
                String title = columnProps.getProperty("title", id);
                if (title == null || title.trim().length() == 0) {
                    title = id;
                }
                columnRoles.setString(row, "Column", title);
                String roles = columnProps.getAttribute("rolelist");
                if (roles.length() > 0) {
                    String[] rolesArr = StringUtil.split(roles, ";");
                    for (int i = 0; i < rolesArr.length; ++i) {
                        if (!columnRoles.isValidColumn(rolesArr[i])) {
                            columnRoles.addColumn(rolesArr[i], 0);
                        }
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + rolesArr[i] + "\" title=\"" + rolesArr[i] + "\">";
                        columnRoles.setString(columnRoles.size() - 1, rolesArr[i], includeImg);
                    }
                    continue;
                }
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"All\" title=\"All\">";
                columnRoles.setString(columnRoles.size() - 1, "All", includeImg);
            }
        }
        return columnRoles;
    }
}

