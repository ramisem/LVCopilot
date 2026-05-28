/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenericElementRenderer
extends BaseElementRenderer {
    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProperties, PropertyList refElementProps, PropertyDefinitionList elementDefList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "genericelement report");
        content.startSubSection(elementId + " Summary", "");
        content.appendSubSection(this.renderSummaryDiff(elementId, elementProperties, refElementProps, elementDefList, reportAdvancedProperties, includeDiffReport), elementId + " Summary", this.diffOnly);
        content.endSubSection("", elementId);
        return content;
    }

    public ConfigReportContent reportDetails(String elementId, PropertyList elementProperties, PropertyDefinitionList elementDefList, boolean reportAdvancedProperties, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent details = new ConfigReportContent(this.config, "generic element details");
        details.startSubSection(elementId + " Element Summary", "");
        details.appendSubSection(this.renderSummary(elementId, elementProperties, elementDefList, reportAdvancedProperties, includeDiffReport), elementId + " Element Summary", this.diffOnly);
        details.startSubSection(elementId + " Element Collections", "");
        details.appendSubSection(this.renderCollectionProperties(elementId, elementProperties, elementDefList, "", reportAdvancedProperties, includeDiffReport), elementId + " Element Collections", this.diffOnly);
        details.endSubSection("", elementId);
        return details;
    }

    public ConfigReportContent reportDetailsDiff(String elementId, PropertyList elementProperties, PropertyList refElementProps, PropertyDefinitionList elementDefList, boolean reportAdvancedProperties, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "generic element detailsdiff");
        buffer.startSubSection(elementId + " Element Summary", "");
        buffer.appendSubSection(this.renderSummaryDiff(elementId, elementProperties, refElementProps, elementDefList, reportAdvancedProperties, includeDiffReport), elementId + " Element Summary", this.diffOnly);
        buffer.startSubSection(elementId + " Element Collections", "");
        buffer.appendSubSection(this.renderCollectionPropertiesDiff(elementId, elementProperties, refElementProps, elementDefList, reportAdvancedProperties, includeDiffReport), elementId + " Element Collections", this.diffOnly);
        buffer.endSubSection("", elementId);
        return buffer;
    }

    private ConfigReportContent renderSummary(String elementId, PropertyList elementProperties, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean includeDiffReport) {
        if (defList == null) {
            return new ConfigReportContent(this.config, "generic element summary");
        }
        Iterator iter = defList.iterator();
        boolean first = true;
        ConfigReportContent section = new ConfigReportContent(this.config, "generic element summary");
        while (iter.hasNext()) {
            PropertyDefinition propDef = (PropertyDefinition)iter.next();
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (elementProperties.isSimple(currPropId)) {
                String prop;
                if (first) {
                    section.startSubHeading("List of Properties", "");
                    section.startTable();
                    first = false;
                }
                if ((prop = elementProperties.getProperty(currPropId, "")).length() <= 0) continue;
                section.startRow();
                section.addRowItem(currPropTitle, prop, this.translationProcessor);
                section.endRow();
                continue;
            }
            if (elementProperties.isPropertyList(currPropId)) {
                PropertyList pl;
                ConfigReportContent plVal;
                if (first) {
                    section.startSubHeading("List of Properties", "");
                    section.startTable();
                    first = false;
                }
                if ((plVal = section.renderPropertyList(pl = elementProperties.getPropertyList(currPropId), false)).length() <= 0) continue;
                section.startRow();
                section.addRowItem(currPropTitle, plVal.toString(), this.translationProcessor);
                section.endRow();
                continue;
            }
            if (!elementProperties.isCollection(currPropId)) continue;
            if (first) {
                section.startSubHeading("List of Properties", "");
                section.startTable();
                first = false;
            }
            PropertyListCollection coll = elementProperties.getCollectionNotNull(currPropId);
            section.startRow();
            section.addRowItem(currPropTitle, "Collection has " + coll.size() + " items.", this.translationProcessor);
            section.endRow();
        }
        if (!first) {
            section.endTable();
        }
        return section;
    }

    private ConfigReportContent renderSummaryDiff(String elementId, PropertyList elementProperties, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean includeDiffReport) {
        if (!includeDiffReport) {
            refElementProps = elementProperties;
        } else if (refElementProps == null) {
            refElementProps = new PropertyList();
        }
        ConfigReportContent content = new ConfigReportContent(this.config, "generic element summary");
        if (defList == null) {
            Trace.log("Definition list is empty");
            return content;
        }
        Iterator iter = defList.iterator();
        boolean first = true;
        while (iter.hasNext()) {
            PropertyDefinition propDef = (PropertyDefinition)iter.next();
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced) continue;
            if (elementProperties.isSimple(currPropId)) {
                if (first) {
                    content.startSubHeading("List of Properties", "");
                    content.startTable();
                    first = false;
                }
                String p1 = elementProperties.getProperty(currPropId, "");
                String p2 = refElementProps.getProperty(currPropId, "");
                if (p1.length() <= 0 && p2.length() <= 0) continue;
                content.startRow();
                content.addDiffRowItem(currPropTitle, p1, p2);
                content.endRow();
                continue;
            }
            if (elementProperties.isPropertyList(currPropId)) {
                PropertyList refPl;
                PropertyList pl;
                ConfigReportContent plVal;
                if (first) {
                    content.startSubHeading("List of Properties", "");
                    content.startTable();
                    first = false;
                }
                if ((plVal = content.renderPropertyListDiff(pl = elementProperties.getPropertyList(currPropId), refPl = refElementProps.getPropertyList(currPropId), false, this.translationProcessor)).length() <= 0) continue;
                content.startRow();
                content.addRowItem(currPropTitle, plVal.toString(), this.translationProcessor);
                content.endRow();
                continue;
            }
            if (!elementProperties.isCollection(currPropId)) continue;
            if (first) {
                content.startSubHeading("List of Properties", "");
                content.startTable();
                first = false;
            }
            content.startRow();
            PropertyListCollection coll = elementProperties.getCollectionNotNull(currPropId);
            PropertyListCollection refColl = refElementProps.getCollectionNotNull(currPropId);
            content.addRowItem(currPropTitle, "Collection has " + ConfigReportContent.getDiffString("" + coll.size(), "" + refColl.size()) + " items.", this.translationProcessor);
            content.endRow();
        }
        if (!first) {
            content.endTable();
        }
        return content;
    }

    private ConfigReportContent renderCollectionProperties(String elementId, PropertyList elementProperties, PropertyDefinitionList defList, String sectionPrefix, boolean reportAdvancedProperties, boolean includeDiffReport) {
        ConfigReportContent content = new ConfigReportContent(this.config, "generic element collection");
        if (defList == null) {
            return content;
        }
        for (PropertyDefinition propDef : defList) {
            PropertyListCollection coll;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced || !elementProperties.isCollection(currPropId) || (coll = elementProperties.getCollection(currPropId)).size() <= 0) continue;
            PropertyDefinitionList collPropDefList = propDef.getPropertyDefinitionList();
            content.startSubHeading(currPropTitle, "<P>" + currPropTitle + " includes " + coll.size() + " items. The details are as shown in the table below. ");
            content.append(content.renderCollection(coll, collPropDefList, reportAdvancedProperties, true, this.translationProcessor).toString());
            DataSet roleMatrix = this.getCollectionRoleMatrix(coll, collPropDefList);
            if (roleMatrix.getRowCount() <= 0 || roleMatrix.getColumnCount() <= 2) continue;
            content.startSubHeading(currPropTitle + " Role Matrix", "");
            content.renderRoleMatrix(roleMatrix, 1);
        }
        if (content.length() == 0) {
            content.append("<P>None");
        }
        return content;
    }

    private ConfigReportContent renderCollectionPropertiesDiff(String elementId, PropertyList elementProperties, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean includeDiffReport) {
        ConfigReportContent content = new ConfigReportContent(this.config, "generic element collection");
        if (defList == null) {
            return content;
        }
        for (PropertyDefinition propDef : defList) {
            PropertyListCollection coll;
            String currPropId = propDef.getId();
            String currPropTitle = propDef.getTitle();
            boolean isAdvanced = propDef.isAdvanced();
            if (!reportAdvancedProperties && isAdvanced || !elementProperties.isCollection(currPropId) || (coll = elementProperties.getCollection(currPropId)).size() <= 0) continue;
            PropertyDefinitionList collPropDefList = propDef.getPropertyDefinitionList();
            content.startSubHeading(currPropTitle, "<P>" + currPropTitle + " includes " + coll.size() + " items. The details are as shown in the table below. ");
            PropertyListCollection refColl = refElementProps.getCollectionNotNull(currPropId);
            content.appendSubSection(content.renderCollectionDiff(coll, refColl, collPropDefList, true, reportAdvancedProperties, this.translationProcessor, true), currPropTitle, this.diffOnly);
            DataSet roleMatrix = this.getCollectionRoleMatrix(coll, collPropDefList);
            DataSet refRoleMatrix = this.getCollectionRoleMatrix(refColl, collPropDefList);
            content.startSubHeading(currPropTitle + " Role Matrix", "");
            String[] keycols = new String[]{roleMatrix.getColumns()[0]};
            content.renderDiffRoleMatrix(roleMatrix, refRoleMatrix, keycols);
        }
        if (content.length() == 0) {
            content.append("<P>None");
        }
        return content;
    }

    private DataSet getCollectionRoleMatrix(PropertyListCollection coll, PropertyDefinitionList collPropDefList) {
        DataSet matrix = new DataSet();
        matrix.setColidCaseSensitive(true);
        for (int i = 0; i < coll.size(); ++i) {
            String titleProperty = collPropDefList.getTitlePropertyId();
            if (titleProperty == null || titleProperty.length() == 0) {
                titleProperty = "id";
            }
            PropertyList props = coll.getPropertyList(i);
            String identity = props.getProperty(titleProperty, props.getProperty("id"));
            matrix.addRow();
            matrix.setString(i, titleProperty, identity);
            String roleList = props.getAttribute("rolelist");
            if (roleList.length() > 0) {
                String[] roles = StringUtil.split(roleList, ";");
                for (int roleitem = 0; roleitem < roles.length; ++roleitem) {
                    if (!matrix.isValidColumn(roles[roleitem])) {
                        matrix.addColumn(roles[roleitem], 0);
                    }
                    String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roles[roleitem] + "\" title=\"" + roles[roleitem] + "\">";
                    matrix.setString(i, roles[roleitem], includeImg);
                }
                continue;
            }
            if (!matrix.isValidColumn("All")) {
                matrix.addColumn("All", 0);
            }
            String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"All\" title=\"All\">";
            matrix.setString(i, "All", includeImg);
        }
        return matrix;
    }
}

