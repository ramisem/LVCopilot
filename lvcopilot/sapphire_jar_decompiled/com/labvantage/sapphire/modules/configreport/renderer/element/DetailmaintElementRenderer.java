/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DetailmaintElementRenderer
extends BaseElementRenderer {
    boolean includeDiffReport = false;
    boolean reportHiddenColumns = false;
    boolean diffOnly = false;

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Detailmaint ");
        this.includeDiffReport = includeDiffReport;
        this.reportHiddenColumns = reportHiddenColumns;
        PropertyList tabInfo = elementProps.getPropertyListNotNull("tab");
        String tabText = tabInfo.getProperty("text", elementId);
        configReportContent.startSubSection("Element: detailmaint ( " + elementId + " , Tab: " + tabText + " )", "");
        configReportContent.startSubHeading("Columns", "");
        configReportContent.appendSubSection(this.renderColumnsInfo(elementProps, refElementProps, reportAdvancedProperties, includeDiffReport), "Columns", this.diffOnly);
        configReportContent.appendSubSection(this.renderColumnsRoleMatrix(elementProps, refElementProps, includeDiffReport), "Columns Role Matrix", this.diffOnly);
        configReportContent.startSubHeading("Buttons", "");
        configReportContent.appendSubSection(this.renderButtonsInfo(elementProps, refElementProps, reportAdvancedProperties, includeDiffReport), "Buttons", this.diffOnly);
        configReportContent.appendSubSection(this.renderButtonsRoleMatrix(elementProps, refElementProps, includeDiffReport), "Buttons Role Matrix", this.diffOnly);
        configReportContent.endSubSection("", tabText);
        return configReportContent;
    }

    private ConfigReportContent renderColumnsRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Detailmaint column role matrix");
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
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "detailmaint buttons role matrix");
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

    private ConfigReportContent renderColumnsInfo(PropertyList elementProps, PropertyList refElementProps, boolean reportAdvancedProeprties, boolean includeDiffReport) {
        ConfigReportContent content = new ConfigReportContent(this.config, "detailsmaint column info");
        DataSet detailCols = this.getColumnsInfo(elementProps);
        String tableid = elementProps.getProperty("tableid");
        if (!includeDiffReport || refElementProps == null) {
            String preamble = "<P>This includes details from table " + tableid + " and has " + detailCols.getRowCount() + " columns.</P>";
            content.append(preamble);
            content.append("<BR>");
            content.renderListTable(detailCols, this.translationProcessor);
        } else {
            DataSet refDetailCols = this.getColumnsInfo(refElementProps);
            String preamble = "<P>This includes details from table " + tableid + " and has " + ConfigReportContent.getDiffString("" + detailCols.getRowCount(), "" + refDetailCols.getRowCount()) + " columns.</P>";
            content.append(preamble);
            content.append("<BR>");
            String[] keycols = new String[]{"Column"};
            content.renderDiffListTable(detailCols, refDetailCols, keycols);
        }
        return content;
    }

    private DataSet getColumnsInfo(PropertyList elementProps) {
        DataSet detailCols = new DataSet();
        detailCols.setColidCaseSensitive(true);
        PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
        detailCols.addColumn("Title", 0);
        detailCols.addColumn("Detail table", 0);
        detailCols.addColumn("Column", 0);
        detailCols.addColumn("Link", 0);
        detailCols.addColumn("Mode", 0);
        detailCols.addColumn("Value", 0);
        boolean hasColumns = false;
        if (this.reportHiddenColumns) {
            if (columns.size() > 0) {
                hasColumns = true;
            }
        } else {
            for (PropertyList column : columns) {
                hasColumns |= !column.getProperty("mode").equals("hidden");
                if (column.getProperty("mode").equals("hidden")) continue;
            }
        }
        if (hasColumns) {
            String tableid = elementProps.getProperty("tableid");
            int row = -1;
            for (PropertyList columnProps : columns) {
                String roles;
                String mode = columnProps.getProperty("mode");
                boolean include = this.reportHiddenColumns | !mode.equals("hidden");
                if (!include) continue;
                detailCols.addRow();
                detailCols.setString(++row, "Detail table", tableid);
                detailCols.setString(row, "Column", columnProps.getProperty("column"));
                detailCols.setString(row, "Title", columnProps.getProperty("title"));
                String link = columnProps.getProperty("link");
                detailCols.setString(row, "Link", ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection));
                detailCols.setString(row, "Mode", columnProps.getProperty("mode"));
                String dropDownSql = columnProps.getProperty("dropdownsql");
                String linkreftypeid = columnProps.getProperty("linkreftypeid");
                if (dropDownSql != null && dropDownSql.length() > 0) {
                    detailCols.setString(row, "Value", "Results of SQL Script: " + dropDownSql);
                } else if (linkreftypeid != null && linkreftypeid.length() > 0) {
                    if (linkreftypeid.indexOf(59) > 0) {
                        detailCols.setString(row, "Value", "List of values: " + linkreftypeid);
                    } else {
                        String fklink = ConfigReportContent.createHyperLink("RefType", linkreftypeid, "", "", this.sdisIncluded, this.frames);
                        detailCols.setString(row, "Value", "Reference Type: " + fklink);
                    }
                }
                if ((roles = columnProps.getAttribute("rolelist")).length() <= 0) continue;
                if (!detailCols.isValidColumn("Roles")) {
                    detailCols.addColumn("Roles", 0);
                }
                detailCols.setString(row, "Roles", roles.replaceAll(";", ", "));
            }
        }
        return detailCols;
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

    private ConfigReportContent renderButtonsInfo(PropertyList elementProps, PropertyList refElementProps, boolean reportAdvancedProperties, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "detailmaint buttons info");
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

    private DataSet getButtonsRoleMatrix(PropertyList elementProps) throws SapphireException {
        PropertyListCollection buttons = elementProps.getCollectionNotNull("buttons");
        boolean hasButtons = false;
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
                ++row;
                String roleList = buttonProps.getAttribute("rolelist");
                if (roleList.length() > 0) {
                    int currRow = matrix.addRow();
                    matrix.setString(currRow, "Text", buttonProps.getProperty("text", buttonProps.getProperty("id")));
                    String[] roles = StringUtil.split(roleList, ";");
                    for (int roleitem = 0; roleitem < roles.length; ++roleitem) {
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

