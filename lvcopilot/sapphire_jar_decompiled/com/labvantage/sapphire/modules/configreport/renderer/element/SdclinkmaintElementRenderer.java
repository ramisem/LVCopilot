/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SdclinkmaintElementRenderer
extends BaseElementRenderer {
    boolean includeDiffReport = false;
    boolean reportHiddenColumns = false;

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaintelement");
        this.includeDiffReport = includeDiffReport;
        this.reportHiddenColumns = reportHiddenColumns;
        PropertyList tabInfo = elementProps.getPropertyListNotNull("tab");
        String tabText = tabInfo.getProperty("text", elementId);
        configReportContent.startSubSection("sdclinkmaint ( " + elementId + " , Tab: " + tabText + " ) ", "");
        ConfigReportContent summary = new ConfigReportContent(this.config, "summary");
        summary.startSubHeading("Columns", "");
        summary.append(this.renderColumnsInfo(elementProps, refElementProps, reportAdvancedProperties, includeDiffReport).toString());
        summary.append(this.renderColumnsRoleMatrix(elementProps, refElementProps, includeDiffReport).toString());
        summary.startSubHeading("Buttons", "");
        summary.append(this.renderButtonsInfo(elementProps, refElementProps, reportAdvancedProperties, includeDiffReport).toString());
        summary.append(this.renderButtonsRoleMatrix(elementProps, refElementProps, includeDiffReport).toString());
        configReportContent.appendSubSection(summary, "sdclinkmaint ( " + elementId + " , Tab: " + tabText + " ) ", this.diffOnly);
        configReportContent.endSubSection("", tabText);
        return configReportContent;
    }

    private ConfigReportContent renderColumnsRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaint element columns role matrix ");
        DataSet roleMatrix = this.getColumnsRoleMatrix(elementProps);
        if (!includeDiffReport || refElementProps == null) {
            if (roleMatrix != null && roleMatrix.getRowCount() > 0 && roleMatrix.getColumnCount() > 2) {
                configReportContent.startSubHeading("Columns Role Matrix", "");
                configReportContent.renderRoleMatrix(roleMatrix, 1);
            }
        } else {
            DataSet refRoleMatrix = this.getColumnsRoleMatrix(refElementProps);
            configReportContent.startSubHeading("Columns Role Matrix", "");
            String[] keycols = new String[]{"Title"};
            configReportContent.renderDiffRoleMatrix(roleMatrix, refRoleMatrix, keycols);
        }
        return configReportContent;
    }

    private ConfigReportContent renderButtonsRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaint element buttons role matrix");
        DataSet roleMatrix = this.getButtonsRoleMatrix(elementProps);
        if (!includeDiffReport || refElementProps == null) {
            if (roleMatrix != null && roleMatrix.getRowCount() > 0 && roleMatrix.getColumnCount() > 2) {
                configReportContent.startSubHeading("Buttons Role Matrix", "");
                configReportContent.renderListTable(roleMatrix, this.translationProcessor);
            }
        } else {
            DataSet refRoleMatrix = this.getButtonsRoleMatrix(refElementProps);
            configReportContent.startSubHeading("Buttons Role Matrix", "");
            String[] keycols = new String[]{"Text"};
            configReportContent.renderDiffRoleMatrix(roleMatrix, refRoleMatrix, keycols);
        }
        return configReportContent;
    }

    private ConfigReportContent renderColumnsInfo(PropertyList elementProps, PropertyList refElementProps, boolean reportAdvancedProeprties, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaint element columns");
        DataSet detailCols = this.getColumnsInfo(elementProps);
        String sdcid = elementProps.getProperty("sdcid");
        if (!includeDiffReport || refElementProps == null) {
            String preamble = "<P>This includes details from linked SDC " + sdcid + " and has " + detailCols.getRowCount() + " columns.</P>";
            configReportContent.append(preamble);
            configReportContent.append("<BR>");
            configReportContent.renderListTable(detailCols, this.translationProcessor);
        } else {
            DataSet refDetailCols = this.getColumnsInfo(refElementProps);
            String preamble = "<P>This includes details from linked SDC " + sdcid + " and has " + ConfigReportContent.getDiffString("" + detailCols.getRowCount(), "" + refDetailCols.getRowCount()) + " columns.</P>";
            configReportContent.append(preamble);
            configReportContent.append("<BR>");
            String[] keycols = new String[]{"Column"};
            configReportContent.renderDiffListTable(detailCols, refDetailCols, keycols);
        }
        return configReportContent;
    }

    private DataSet getColumnsInfo(PropertyList elementProps) throws SapphireException {
        DataSet detailCols = new DataSet();
        detailCols.setColidCaseSensitive(true);
        PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
        detailCols.addColumn("Title", 0);
        detailCols.addColumn("SDC", 0);
        detailCols.addColumn("Column", 0);
        detailCols.addColumn("Link", 0);
        detailCols.addColumn("Mode", 0);
        detailCols.addColumn("Validation", 0);
        detailCols.addColumn("Lookuplink", 0);
        detailCols.addColumn("Display value", 0);
        boolean hasColumns = false;
        String sdcid = elementProps.getProperty("sdcid");
        if (this.reportHiddenColumns) {
            hasColumns = columns.size() > 0;
        } else {
            for (PropertyList column : columns) {
                hasColumns |= !column.getProperty("mode").equals("hidden");
            }
        }
        if (hasColumns) {
            int row = -1;
            for (PropertyList columnProps : columns) {
                String pseudo;
                String mode = columnProps.getProperty("mode");
                boolean include = this.reportHiddenColumns | !mode.equals("hidden");
                if (!include) continue;
                detailCols.addRow();
                detailCols.setString(++row, "SDC", sdcid);
                detailCols.setString(row, "Column", columnProps.getProperty("columnid"));
                detailCols.setString(row, "Title", columnProps.getProperty("title"));
                String link = columnProps.getPropertyListNotNull("link").getProperty("href", "");
                detailCols.setString(row, "Link", ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection));
                String modeVal = mode;
                if (mode.equals("dropdownlist")) {
                    String refid = columnProps.getProperty("reftypeid", "");
                    String ddsql = columnProps.getProperty("sql", "");
                    String ddvals = columnProps.getProperty("dropdownvalues", "");
                    String val = "";
                    if (refid.length() > 0) {
                        String fklink = ConfigReportContent.createHyperLink("RefType", refid, "", "", this.sdisIncluded, this.frames);
                        val = "Reference Type: " + fklink;
                    } else if (ddsql.length() > 0) {
                        val = "SQL: " + ddsql;
                    } else if (ddvals.length() > 0) {
                        val = "Values: " + ddvals.replaceAll(";", ", ");
                    }
                    if (val.length() > 0) {
                        modeVal = mode + "(" + val + ")";
                    }
                } else if (mode.equals("lookup")) {
                    String lookupLink = columnProps.getPropertyListNotNull("lookuplink").getProperty("href");
                    modeVal = mode + "(" + ConfigReportContent.renderLink(lookupLink, this.sdisIncluded, this.frames, this.connection) + ")";
                } else if (!mode.equals("hidden") && (pseudo = columnProps.getProperty("pseudocolumn", "")).length() > 0) {
                    modeVal = "(Custom)";
                }
                detailCols.setString(row, "Mode", modeVal);
                String displayVal = ConfigReportContent.parseDisplayValues(columnProps.getProperty("displayvalue"), this.folder, this.applicationRoot).toXMLString();
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
                detailCols.setString(row, "Display value", displayVal);
                String validation = columnProps.getProperty("validation", "");
                if (validation.length() > 0) {
                    PropertyList validationProps = ConfigReportContent.parseValidationValues(validation);
                    validation = validationProps.getProperty("Groovy", "").length() > 0 ? "Groovy:" + validationProps.getProperty("Groovy") : validationProps.toXMLString();
                }
                detailCols.setString(row, "Validation", validation);
                String lookuplink = columnProps.getPropertyListNotNull("lookuplink").getProperty("href");
                detailCols.setString(row, "Lookuplink", ConfigReportContent.renderLink(lookuplink, this.sdisIncluded, this.frames, this.connection));
                String roles = columnProps.getAttribute("rolelist");
                if (roles.length() <= 0) continue;
                if (!detailCols.isValidColumn("Roles")) {
                    detailCols.addColumn("Roles", 0);
                }
                detailCols.setString(row, "Roles", roles.replaceAll(";", ", "));
            }
        }
        return detailCols;
    }

    private ConfigReportContent renderButtonsInfo(PropertyList elementProps, PropertyList refElementProps, boolean reportAdvancedProperties, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaint element buttons");
        DataSet buttonList = this.getButtonsInfo(elementProps);
        if (!includeDiffReport || refElementProps == null) {
            configReportContent.renderListTable(buttonList, this.translationProcessor);
        } else {
            DataSet refButtonList = this.getButtonsInfo(refElementProps);
            String[] keycols = new String[]{"Text"};
            configReportContent.renderDiffListTable(buttonList, refButtonList, keycols);
        }
        return configReportContent;
    }

    private DataSet getButtonsInfo(PropertyList elementProps) throws SapphireException {
        PropertyListCollection buttons = elementProps.getCollectionNotNull("buttons");
        boolean hasButtons = false;
        for (PropertyList button : buttons) {
            String show = button.getProperty("show", "");
            hasButtons |= !show.equals("N");
        }
        DataSet buttonList = new DataSet();
        buttonList.setColidCaseSensitive(true);
        buttonList.addColumn("Text", 0);
        buttonList.addColumn("Image", 0);
        buttonList.addColumn("Operation", 0);
        if (hasButtons) {
            int row = -1;
            for (PropertyList buttonProps : buttons) {
                String show = buttonProps.getProperty("show");
                if (show.equals("N")) continue;
                buttonList.addRow();
                buttonList.setString(++row, "Text", buttonProps.getProperty("text", buttonProps.getProperty("id")));
                buttonList.setString(row, "Image", buttonProps.getProperty("img"));
                String roles = buttonProps.getAttribute("rolelist");
                if (roles.length() > 0) {
                    if (!buttonList.isValidColumn("Roles")) {
                        buttonList.addColumn("Roles", 0);
                    }
                    buttonList.setString(row, "Roles", roles.replaceAll(";", ", "));
                }
                buttonList.setString(row, "Operation", "JavaScript:  " + buttonProps.getProperty("js"));
                if (buttonList.getString(row, "Image").length() <= 0) continue;
                String srcImageDir = this.applicationRoot;
                try {
                    String buttonImageName = buttonList.getString(row, "Image");
                    ConfigReportContent.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                    buttonList.setString(row, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        }
        return buttonList;
    }

    private ConfigReportContent renderDisplayValue(String displayValue) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdclinkmaint element");
        if (displayValue == null || displayValue.length() == 0) {
            return configReportContent;
        }
        String[] options = StringUtil.split(displayValue, ";");
        configReportContent.startTable();
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
                configReportContent.startRow();
                configReportContent.addRowItem(lhs, rhs);
                configReportContent.endRow();
                continue;
            }
            configReportContent.startRow();
            configReportContent.append("<TD>options[i]</TD>");
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    private DataSet getColumnsRoleMatrix(PropertyList elementProps) {
        DataSet matrix = new DataSet();
        matrix.setColidCaseSensitive(true);
        PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
        matrix.addColumn("Title", 0);
        boolean hasColumns = false;
        for (PropertyList column : columns) {
            hasColumns |= !column.getProperty("mode").equals("hidden");
        }
        if (hasColumns) {
            int item = -1;
            for (PropertyList columnProps : columns) {
                String mode = columnProps.getProperty("mode");
                if (mode.equals("hidden")) continue;
                matrix.addRow();
                matrix.setString(++item, "Title", columnProps.getProperty("title"));
                String roleList = columnProps.getAttribute("rolelist");
                if (roleList.length() > 0) {
                    String[] roles = StringUtil.split(roleList, ";");
                    for (int roleitem = 0; roleitem < roles.length; ++roleitem) {
                        if (!matrix.isValidColumn(roles[roleitem])) {
                            matrix.addColumn(roles[roleitem], 0);
                        }
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roles[roleitem] + "\" title=\"" + roles[roleitem] + "\">";
                        matrix.setString(item, roles[roleitem], includeImg);
                    }
                    continue;
                }
                if (!matrix.isValidColumn("All")) {
                    matrix.addColumn("All", 0);
                }
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"All\" title=\"All\">";
                matrix.setString(item, "All", includeImg);
            }
        }
        return matrix;
    }

    private DataSet getButtonsRoleMatrix(PropertyList elementProps) throws SapphireException {
        PropertyListCollection buttons = elementProps.getCollectionNotNull("buttons");
        boolean hasButtons = false;
        boolean visibleButtonCount = false;
        for (PropertyList button : buttons) {
            String show = button.getProperty("show", "");
            hasButtons |= !show.equals("N");
        }
        DataSet matrix = new DataSet();
        matrix.setColidCaseSensitive(true);
        matrix.addColumn("Text", 0);
        matrix.addColumn("All", 0);
        if (hasButtons) {
            int row = -1;
            for (PropertyList buttonProps : buttons) {
                String show = buttonProps.getProperty("show");
                if (show.equals("N")) continue;
                matrix.addRow();
                matrix.setString(++row, "Text", buttonProps.getProperty("text", buttonProps.getProperty("id")));
                String roleList = buttonProps.getAttribute("rolelist");
                if (roleList.length() > 0) {
                    String[] roles = StringUtil.split(roleList, ";");
                    for (int roleitem = 0; roleitem < roles.length; ++roleitem) {
                        if (!matrix.isValidColumn(roles[roleitem])) {
                            matrix.addColumn(roles[roleitem], 0);
                        }
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roles[roleitem] + "\" title=\"" + roles[roleitem] + "\">";
                        matrix.setString(row, roles[roleitem], includeImg);
                    }
                    continue;
                }
                if (!matrix.isValidColumn("All")) {
                    matrix.addColumn("All", 0);
                }
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"All\" title=\"All\">";
                matrix.setString(row, "All", includeImg);
            }
        }
        return matrix;
    }
}

