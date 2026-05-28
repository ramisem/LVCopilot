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

public class SdidetailmaintElementRenderer
extends BaseElementRenderer {
    boolean includeDiffReport = false;
    boolean reportHiddenColumns = false;

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdidetailmaint element");
        this.includeDiffReport = includeDiffReport;
        this.reportHiddenColumns = reportHiddenColumns;
        PropertyList tabInfo = elementProps.getPropertyListNotNull("tab");
        String tabText = tabInfo.getProperty("text", elementId);
        configReportContent.startSubSection("sdidetailmaint ( " + elementId + " , Tab: " + tabText + " )", "");
        ConfigReportContent summary = new ConfigReportContent(this.config, "Element");
        summary.append(this.renderColumnsInfo(elementProps, refElementProps, includeDiffReport).toString());
        summary.append(this.renderColumnsRoleMatrix(elementProps, refElementProps, includeDiffReport).toString());
        summary.append(this.renderButtonsInfo(elementProps, refElementProps, includeDiffReport).toString());
        summary.append(this.renderButtonsRoleMatrix(elementProps, refElementProps, includeDiffReport).toString());
        configReportContent.appendSubSection(summary, "sdidetailmaint ( " + elementId + " , Tab: " + tabText + " )", this.diffOnly);
        configReportContent.endSubSection("", tabText);
        return configReportContent;
    }

    private ConfigReportContent renderColumnsRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdidetailmaint element columns role matrix");
        DataSet roleMatrix = this.getColumnsRoleMatrix(elementProps);
        if (!includeDiffReport || refElementProps == null) {
            if (roleMatrix != null && roleMatrix.getRowCount() > 0 && roleMatrix.getColumnCount() > 2) {
                configReportContent.startSubHeading("Columns Role Matrix", "");
                configReportContent.renderListTable(roleMatrix, this.translationProcessor);
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
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdidetailmaint element buttons role matrix");
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

    private ConfigReportContent renderColumnsInfo(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdidetailmaint element columns");
        DataSet detailCols = this.getColumnsInfo(elementProps);
        String detailtableid = elementProps.getProperty("elementtype").toLowerCase();
        if (!includeDiffReport || refElementProps == null) {
            String preamble = "<P>This includes details from table " + detailtableid + " and has " + detailCols.getRowCount() + " columns.</P>";
            configReportContent.startSubHeading("Columns", preamble);
            configReportContent.renderListTable(detailCols, this.translationProcessor);
        } else {
            DataSet refDetailCols = this.getColumnsInfo(refElementProps);
            String[] keycols = new String[]{"Column"};
            String preamble = "<P>This includes details from table " + detailtableid + " and has " + detailCols.getRowCount() + " columns.</P>";
            configReportContent.startSubHeading("Columns", preamble);
            configReportContent.renderDiffListTable(detailCols, refDetailCols, keycols);
        }
        return configReportContent;
    }

    private DataSet getColumnsInfo(PropertyList elementProps) throws SapphireException {
        DataSet detailCols = new DataSet();
        detailCols.setColidCaseSensitive(true);
        PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
        detailCols.addColumn("Title", 0);
        detailCols.addColumn("Detail table", 0);
        detailCols.addColumn("Column", 0);
        detailCols.addColumn("Link", 0);
        detailCols.addColumn("Mode", 0);
        detailCols.addColumn("Pseudo", 0);
        String detailtableid = elementProps.getProperty("elementtype").toLowerCase();
        int row = -1;
        for (PropertyList columnProps : columns) {
            String roles;
            String mode = columnProps.getProperty("mode");
            boolean include = this.reportHiddenColumns | !mode.equals("hidden");
            if (!include) continue;
            detailCols.addRow();
            detailCols.setString(++row, "Detail table", detailtableid);
            detailCols.setString(row, "Column", columnProps.getProperty("column").trim());
            detailCols.setString(row, "Title", columnProps.getProperty("title"));
            String link = columnProps.getPropertyList("link").getProperty("href");
            detailCols.setString(row, "Link", ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection));
            detailCols.setString(row, "Mode", columnProps.getProperty("mode"));
            String pseudo = columnProps.getProperty("Pseudo");
            if (pseudo != null && (pseudo.indexOf("img") > 0 || pseudo.indexOf("IMG") > 0)) {
                int startloc = pseudo.indexOf("'") + 1;
                int endloc = pseudo.lastIndexOf("'");
                String srcval = pseudo.substring(startloc, endloc);
                String srcImageDir = this.applicationRoot;
                try {
                    ConfigReportContent.copyFile(new File(srcImageDir + srcval), new File(this.folder + "/images/" + srcval));
                    detailCols.setString(row, "Pseudo", "<img src=\"../images/" + srcval + "\"/>");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
            if ((roles = columnProps.getAttribute("rolelist")).length() <= 0) continue;
            if (!detailCols.isValidColumn("Roles")) {
                detailCols.addColumn("Roles", 0);
            }
            detailCols.setString(row, "Roles", roles.replaceAll(";", ", "));
        }
        return detailCols;
    }

    private ConfigReportContent renderButtonsInfo(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sdidetailmaint element buttons");
        DataSet buttonList = this.getButtonsInfo(elementProps);
        configReportContent.startSubHeading("Buttons", "");
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
        DataSet buttonList = new DataSet();
        buttonList.setColidCaseSensitive(true);
        buttonList.addColumn("Text", 0);
        buttonList.addColumn("Image", 0);
        buttonList.addColumn("Operation", 0);
        for (PropertyList buttonProps : buttons) {
            String show = buttonProps.getProperty("show");
            if (show.equals("N")) continue;
            int row = buttonList.addRow();
            buttonList.setString(row, "Text", buttonProps.getProperty("text", buttonProps.getProperty("id")));
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
        return buttonList;
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

