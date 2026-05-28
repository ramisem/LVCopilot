/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListElementRenderer
extends BaseElementRenderer {
    private String sdcid;
    boolean includeDiffReport = false;
    boolean reportHiddenColumns = false;

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        boolean subsection = true;
        this.includeDiffReport = includeDiffReport;
        this.reportHiddenColumns = reportHiddenColumns;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "list element");
        this.sdcid = elementProps.getProperty("sdcid");
        configReportContent.startSubSection(elementId, "");
        configReportContent.appendSubSection(this.renderListSummary(elementProps, refElementProps), elementId + " Summary", this.diffOnly);
        configReportContent.appendSubSection(this.renderListPageColumns(elementProps, refElementProps, reportAdvancedProperties), elementId + " Columns", this.diffOnly);
        ConfigReportContent roleMatrix = this.renderRoleMatrix(elementProps, refElementProps, includeDiffReport);
        if (roleMatrix.length() > 0) {
            configReportContent.appendSubSection(roleMatrix, elementId + " Columns Role Matrix", this.diffOnly);
        }
        return configReportContent;
    }

    private ConfigReportContent renderRoleMatrix(PropertyList elementProps, PropertyList refElementProps, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "list element column role matrix");
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

    private ConfigReportContent renderListSummary(PropertyList elementProps, PropertyList refElementProps) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "list element summary");
        String sdcid = elementProps.getProperty("sdcid", "");
        String selectorType = elementProps.getProperty("selectortype", "");
        String maxRows = elementProps.getProperty("rowsperpage", "");
        String maxRowsRetrieved = elementProps.getProperty("retrievelimit", "");
        String refSdcid = "";
        String refSelectorType = "";
        String refMaxRows = "";
        String refMaxRowsRetrieved = "";
        if (this.includeDiffReport && refElementProps != null) {
            refSdcid = refElementProps.getProperty("sdcid", "");
            refSelectorType = refElementProps.getProperty("selectortype", "");
            refMaxRows = refElementProps.getProperty("rowsperpage", "");
            refMaxRowsRetrieved = refElementProps.getProperty("retrievelimit", "");
        }
        configReportContent.startBulletList();
        String fklink = ConfigReportContent.createHyperLink("SDC", sdcid, "", "", this.sdisIncluded, this.frames);
        if (!this.includeDiffReport || sdcid.equals(refSdcid)) {
            configReportContent.addBullet("Displays columns from SDC:" + fklink + ". ");
        } else {
            configReportContent.addDiffBullet("Displays columns from SDC:" + fklink, " " + refSdcid);
        }
        if ("checkbox".equals(selectorType)) {
            if (!this.includeDiffReport || selectorType.equals(refSelectorType)) {
                configReportContent.addBullet("Supports multiple-selection using checkboxes.");
            } else {
                configReportContent.addDiffBullet("Supports multiple-selection using checkboxes.", "Does not support multiple-selection.");
            }
        } else if (!this.includeDiffReport || selectorType.equals(refSelectorType)) {
            configReportContent.addBullet("Does not support multiple-selection.");
        } else {
            configReportContent.addDiffBullet("Does not support multiple-selection.", "Supports multiple-selection using checkboxes.");
        }
        if (this.includeDiffReport && !maxRows.equals(refMaxRows)) {
            configReportContent.addDiffBullet("It displays " + maxRows + " sdi's per page", "It displays " + refMaxRows + " sdi's per page");
        } else {
            configReportContent.addBullet("It displays " + maxRows + " sdi's per page");
        }
        if (this.includeDiffReport && !maxRowsRetrieved.equals(refMaxRowsRetrieved)) {
            configReportContent.addDiffBullet("It retrieves a maximum of " + maxRowsRetrieved + " sdi's", "It retrieves a maximum of " + refMaxRowsRetrieved + " sdi's");
        } else {
            configReportContent.addBullet("It retrieves a maximum of " + maxRowsRetrieved + " sdi's");
        }
        PropertyListCollection sortBy = elementProps.getCollectionNotNull("sortby");
        String sortByField = "";
        for (int i = 0; i < sortBy.size(); ++i) {
            PropertyList pl = sortBy.getPropertyList(i);
            String currCol = pl.getProperty("columnid", "");
            if (currCol.length() <= 0) continue;
            sortByField = sortByField.length() > 0 ? sortByField + "," + currCol : sortByField + currCol;
        }
        String refSortByField = "";
        if (this.includeDiffReport && refElementProps != null) {
            PropertyListCollection refsortBy = refElementProps.getCollectionNotNull("sortby");
            for (int i = 0; i < refsortBy.size(); ++i) {
                PropertyList pl = refsortBy.getPropertyList(i);
                String currCol = pl.getProperty("columnid", "");
                if (currCol.length() <= 0) continue;
                refSortByField = refSortByField.length() > 0 ? refSortByField + "," + currCol : refSortByField + currCol;
            }
        }
        if (!this.includeDiffReport || sortByField.equals(refSortByField)) {
            configReportContent.addBullet("The list is sorted by column(s): " + sortByField);
        } else {
            configReportContent.addDiffBullet("The list is sorted by columns(s): " + sortByField, refSortByField);
        }
        PropertyListCollection groupBy = elementProps.getCollectionNotNull("groupby");
        String groupByField = "";
        for (int i = 0; i < groupBy.size(); ++i) {
            PropertyList pl = groupBy.getPropertyList(i);
            String currCol = pl.getProperty("columnid", "");
            if (currCol.length() <= 0) continue;
            groupByField = groupByField.length() > 0 ? groupByField + "," + currCol : groupByField + currCol;
        }
        String refGroupByField = "";
        if (this.includeDiffReport && refElementProps != null) {
            PropertyListCollection refgroupBy = refElementProps.getCollectionNotNull("groupby");
            for (int i = 0; i < refgroupBy.size(); ++i) {
                PropertyList pl = refgroupBy.getPropertyList(i);
                String currCol = pl.getProperty("columnid", "");
                if (currCol.length() <= 0) continue;
                refGroupByField = refGroupByField.length() > 0 ? refGroupByField + "," + currCol : refGroupByField + currCol;
            }
        }
        if (!this.includeDiffReport || groupByField.equals(refGroupByField)) {
            configReportContent.addBullet("The sdi's are groupby: " + groupByField);
        } else {
            configReportContent.addDiffBullet("The sdi's are groupby: " + groupByField, refGroupByField);
        }
        configReportContent.endBulletList();
        return configReportContent;
    }

    private DataSet getListPageColumns(PropertyList list, boolean reportAdvancedProperties) throws SapphireException {
        DataSet listPageCols = new DataSet();
        listPageCols.setColidCaseSensitive(true);
        PropertyListCollection columns = list.getCollectionNotNull("columns");
        listPageCols.addColumn("Title", 0);
        listPageCols.addColumn("Column", 0);
        boolean hasColumns = false;
        if (this.reportHiddenColumns) {
            listPageCols.addColumn("Mode", 0);
            if (columns.size() > 0) {
                hasColumns = true;
            }
        } else {
            for (PropertyList column : columns) {
                hasColumns |= !column.getProperty("mode").equals("Hidden Value");
            }
        }
        listPageCols.addColumn("Link", 0);
        if (hasColumns) {
            int row = -1;
            for (PropertyList columnProps : columns) {
                String rolelist;
                PropertyList linkProps = columnProps.getPropertyListNotNull("link");
                String mode = columnProps.getProperty("mode", "");
                boolean include = this.reportHiddenColumns | !mode.equals("Hidden Value");
                if (!include) continue;
                listPageCols.addRow();
                String id = "( " + columnProps.getProperty("id") + " )";
                listPageCols.setString(++row, "Column", columnProps.getProperty("columnid"));
                String title = columnProps.getProperty("title", id);
                if (title == null || title.trim().length() == 0) {
                    title = id;
                }
                listPageCols.setString(row, "Title", title);
                if (this.reportHiddenColumns) {
                    listPageCols.setString(row, "Mode", mode);
                }
                if ((rolelist = columnProps.getAttribute("rolelist")).length() > 0) {
                    rolelist = rolelist.replaceAll(";", ", ");
                    listPageCols.setString(row, "Roles", rolelist);
                }
                String link = linkProps.getProperty("href");
                String currColLink = "";
                if (link.trim().length() == 0) {
                    currColLink = "";
                } else {
                    int amp;
                    link = StringUtil.replaceAll(link, "[sdcid]", this.sdcid);
                    String target = linkProps.getProperty("target");
                    boolean popup = target.trim().length() == 0 || target.trim().equalsIgnoreCase("_self");
                    int pagePos = link.toLowerCase().indexOf("page=");
                    int filePos = link.toLowerCase().indexOf("file=");
                    if (link.trim().toLowerCase().startsWith("javascript")) {
                        currColLink = link;
                    } else if (pagePos >= 0) {
                        amp = link.indexOf("&", pagePos);
                        String webpageId = amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp);
                        currColLink = "Page: " + ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection);
                        if (popup) {
                            currColLink = currColLink + " (Popup)";
                        }
                    } else if (filePos >= 0) {
                        amp = link.indexOf("&", filePos);
                        currColLink = "File: " + (amp == -1 ? link.substring(filePos + 5) : link.substring(filePos + 5, amp));
                        if (popup) {
                            currColLink = currColLink + " (Popup)";
                        }
                    } else {
                        currColLink = "Unrecognized link";
                        if (popup) {
                            currColLink = currColLink + " (Popup)";
                        }
                    }
                }
                listPageCols.setString(row, "Link", currColLink);
                String displayValue = this.renderDisplayValue(columnProps.getProperty("displayvalue"));
                if (displayValue.length() <= 0) continue;
                listPageCols.setString(row, "Display value", displayValue);
            }
        }
        return listPageCols;
    }

    private PropertyList findColumnProperties(PropertyListCollection collection, String id, String columnid, String mode) {
        for (int i = 0; i < collection.size(); ++i) {
            PropertyList currColumnPropertyList = (PropertyList)collection.get(i);
            if (!id.equals(currColumnPropertyList.getProperty("id")) || !columnid.equals(currColumnPropertyList.getProperty("columnid")) || !mode.equals(currColumnPropertyList.getProperty("mode"))) continue;
            return currColumnPropertyList;
        }
        return null;
    }

    private PropertyList findPseudoColumn(String pseudo, PropertyListCollection collection) {
        for (int i = 0; i < collection.size(); ++i) {
            PropertyList currColumnPropertyList = (PropertyList)collection.get(i);
            if (currColumnPropertyList.getProperty("pseudocolumn") == null || currColumnPropertyList.getProperty("pseudocolumn").length() <= 0 || !pseudo.equals(currColumnPropertyList.getProperty("pseudocolumn"))) continue;
            return currColumnPropertyList;
        }
        return null;
    }

    private DataSet getDiffListPageColumns(PropertyList list, PropertyList refList) throws SapphireException {
        String mode;
        PropertyList linkProps;
        PropertyList refColumnProps;
        DataSet difflistPageCols = new DataSet();
        difflistPageCols.setColidCaseSensitive(true);
        PropertyListCollection columns = list.getCollectionNotNull("columns");
        PropertyListCollection refColumns = refList.getCollectionNotNull("columns");
        difflistPageCols.addColumn("Title", 0);
        difflistPageCols.addColumn("Column", 0);
        if (this.reportHiddenColumns) {
            difflistPageCols.addColumn("Mode", 0);
        }
        difflistPageCols.addColumn("Link", 0);
        for (PropertyList columnProps : columns) {
            String rolelist;
            refColumnProps = null;
            refColumnProps = columnProps.getProperty("pseudocolumn") != null && columnProps.getProperty("pseudocolumn").length() > 0 ? this.findPseudoColumn(columnProps.getProperty("pseudocolumn"), refColumns) : this.findColumnProperties(refColumns, columnProps.getProperty("id"), columnProps.getProperty("columnid"), columnProps.getProperty("mode"));
            linkProps = columnProps.getPropertyListNotNull("link");
            mode = columnProps.getProperty("mode", "");
            boolean includeColumn = !mode.equals("Hidden Value") | this.reportHiddenColumns;
            if (!includeColumn) continue;
            int currRow = difflistPageCols.addRow();
            String id = "( " + columnProps.getProperty("id") + " )";
            String title = columnProps.getProperty("title", id);
            if (title == null || title.trim().length() == 0) {
                title = id;
            }
            if ((rolelist = columnProps.getAttribute("rolelist")).length() > 0) {
                rolelist = rolelist.replaceAll(";", ", ");
            }
            if (refColumnProps == null) {
                difflistPageCols.setString(currRow, "Column", ConfigReportContent.getNewString(columnProps.getProperty("columnid")));
                difflistPageCols.setString(currRow, "Title", ConfigReportContent.getNewString(title));
                difflistPageCols.setString(currRow, "Roles", ConfigReportContent.getNewString(rolelist));
                if (this.reportHiddenColumns) {
                    difflistPageCols.setString(currRow, "Mode", ConfigReportContent.getNewString(mode));
                }
            } else {
                difflistPageCols.setString(currRow, "Column", columnProps.getProperty("columnid"));
                String reftitle = refColumnProps.getProperty("title", id);
                if (reftitle == null || reftitle.trim().length() == 0) {
                    reftitle = id;
                }
                String refRoleList = refColumnProps.getAttribute("rolelist");
                String refMode = refColumnProps.getProperty("mode", "");
                if (refRoleList.length() > 0) {
                    refRoleList = refRoleList.replaceAll(";", ", ");
                }
                if (title.equals(reftitle)) {
                    difflistPageCols.setString(currRow, "Title", title);
                } else {
                    difflistPageCols.setString(currRow, "Title", ConfigReportContent.getDiffString(title, reftitle));
                }
                if (rolelist.equals(refRoleList)) {
                    difflistPageCols.setString(currRow, "Roles", rolelist);
                } else {
                    difflistPageCols.setString(currRow, "Roles", ConfigReportContent.getDiffString(rolelist, refRoleList));
                }
                if (this.reportHiddenColumns) {
                    if (mode.equals(refMode)) {
                        difflistPageCols.setString(currRow, "Mode", mode);
                    } else {
                        difflistPageCols.setString(currRow, "Mode", ConfigReportContent.getDiffString(mode, refMode));
                    }
                }
            }
            String link = this.getLinkInfo(linkProps);
            String refLink = "";
            if (refColumnProps != null) {
                PropertyList refLinkProps = refColumnProps.getPropertyListNotNull("link");
                refLink = this.getLinkInfo(refLinkProps);
            }
            difflistPageCols.setString(currRow, "Link", ConfigReportContent.getDiffString(link, refLink));
            String displayValue = "";
            displayValue = refColumnProps != null ? this.renderDiffDisplayValue(columnProps.getProperty("displayvalue"), refColumnProps.getProperty("displayvalue")).toString() : this.renderDisplayValue(columnProps.getProperty("displayvalue"));
            if (displayValue.length() <= 0) continue;
            difflistPageCols.setString(currRow, "Display value", displayValue);
        }
        for (PropertyList columnProps : refColumns) {
            String rolelist;
            refColumnProps = null;
            refColumnProps = columnProps.getProperty("pseudocolumn") != null && columnProps.getProperty("pseudocolumn").length() > 0 ? this.findPseudoColumn(columnProps.getProperty("pseudocolumn"), refColumns) : this.findColumnProperties(refColumns, columnProps.getProperty("id"), columnProps.getProperty("columnid"), columnProps.getProperty("mode"));
            linkProps = columnProps.getPropertyListNotNull("link");
            mode = columnProps.getProperty("mode", "");
            if (mode.equals("Hidden Value")) continue;
            String id = "( " + columnProps.getProperty("id") + " )";
            String title = columnProps.getProperty("title", id);
            if (title == null || title.trim().length() == 0) {
                title = id;
            }
            if ((rolelist = columnProps.getAttribute("rolelist")).length() > 0) {
                rolelist = rolelist.replaceAll(";", ", ");
            }
            if (refColumnProps != null) continue;
            int currRow = difflistPageCols.addRow();
            difflistPageCols.setString(currRow, "Column", ConfigReportContent.getDeletedString(columnProps.getProperty("columnid")));
            difflistPageCols.setString(currRow, "Title", ConfigReportContent.getDeletedString(title));
            difflistPageCols.setString(currRow, "Roles", ConfigReportContent.getDeletedString(rolelist));
            String link = this.getLinkInfo(linkProps);
            String refLink = "";
            difflistPageCols.setString(currRow, "Link", ConfigReportContent.getDiffString(link, refLink));
            String displayValue = this.renderDisplayValue(columnProps.getProperty("displayvalue"));
            if (displayValue.length() <= 0) continue;
            difflistPageCols.setString(currRow, "Display value", displayValue);
        }
        return difflistPageCols;
    }

    private ConfigReportContent renderListPageColumns(PropertyList list, PropertyList refList, boolean reportAdvancedProperties) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "list element columns");
        if (this.includeDiffReport && refList != null) {
            DataSet diffListPageCols = this.getDiffListPageColumns(list, refList);
            configReportContent.renderListTable(diffListPageCols, this.translationProcessor);
        } else {
            DataSet listPageCols = this.getListPageColumns(list, reportAdvancedProperties);
            configReportContent.renderListTable(listPageCols, this.translationProcessor);
        }
        return configReportContent;
    }

    private String renderDisplayValue(String displayValue) throws SapphireException {
        if (displayValue == null || displayValue.length() == 0) {
            return "";
        }
        ConfigReportContent buffer = new ConfigReportContent(this.config, "");
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

    private PropertyList parseOptions(String[] options) throws SapphireException {
        PropertyList ret = new PropertyList();
        for (int i = 0; i < options.length; ++i) {
            String lhs = options[i];
            String rhs = "";
            if (options[i].indexOf("=") > -1) {
                lhs = "Others";
                if (options[i].indexOf("=") != 0) {
                    lhs = options[i].substring(0, options[i].indexOf("="));
                }
                if ((rhs = options[i].substring(options[i].indexOf("=") + 1)).indexOf("img") > -1 || rhs.indexOf("IMG") > -1) {
                    rhs = ConfigReportContent.changeImageFolder(rhs, this.folder, this.applicationRoot);
                }
            }
            ret.setProperty(lhs, rhs);
        }
        return ret;
    }

    private ConfigReportContent renderDiffDisplayValue(String displayValue, String refDisplayValue) throws SapphireException {
        if (displayValue == null) {
            displayValue = "";
        }
        if (refDisplayValue == null) {
            refDisplayValue = "";
        }
        String[] options = StringUtil.split(displayValue, ";");
        String[] refOptions = StringUtil.split(refDisplayValue, ";");
        PropertyList optionsPl = this.parseOptions(options);
        PropertyList refOptionsPl = this.parseOptions(refOptions);
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "list element");
        configReportContent.renderPropertyListDiff(optionsPl, refOptionsPl, false, this.translationProcessor).toString();
        return configReportContent;
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

    private String getLinkInfo(PropertyList linkProps) {
        String link = linkProps.getProperty("href");
        String currColLink = "";
        if (link.trim().length() == 0) {
            currColLink = "";
        } else {
            link = StringUtil.replaceAll(link, "[sdcid]", this.sdcid);
            String target = linkProps.getProperty("target");
            boolean popup = target.trim().length() == 0 || target.trim().equalsIgnoreCase("_self");
            int pagePos = link.toLowerCase().indexOf("page=");
            int filePos = link.toLowerCase().indexOf("file=");
            if (link.trim().toLowerCase().startsWith("javascript")) {
                currColLink = link;
            } else if (pagePos >= 0) {
                int amp = link.indexOf("&", pagePos);
                String webpageId = amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp);
                currColLink = "Page: " + ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection);
                if (popup) {
                    currColLink = currColLink + " (Popup)";
                }
            } else if (filePos >= 0) {
                int amp = link.indexOf("&", filePos);
                currColLink = "File: " + (amp == -1 ? link.substring(filePos + 5) : link.substring(filePos + 5, amp));
                if (popup) {
                    currColLink = currColLink + " (Popup)";
                }
            } else {
                currColLink = "Unrecognized link";
                if (popup) {
                    currColLink = currColLink + " (Popup)";
                }
            }
        }
        return currColLink;
    }
}

