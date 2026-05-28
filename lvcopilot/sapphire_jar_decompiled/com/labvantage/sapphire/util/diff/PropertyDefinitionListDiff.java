/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.BaseDiff;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import sapphire.util.DataSet;

public class PropertyDefinitionListDiff
extends BaseDiff {
    int curLevel = 0;
    ArrayList sourceHierarchy = new ArrayList();
    ArrayList targetHierarchy = new ArrayList();
    public static final String PROPERTY_DEFINITION = "Property";

    PropertyDefinitionListDiff() {
    }

    public PropertyDefinitionListDiff(PropertyDefinitionList source, PropertyDefinitionList target) {
        this();
        DataSet attrDiffs = new DataSet();
        attrDiffs = this.compareDefinitionListAttributes(source, target, this.curLevel, attrDiffs);
        if (attrDiffs.getRowCount() > 0) {
            this.addPDDiff("(ROOT)", "Changed", attrDiffs);
        }
        this.compareDefinitionLists(source, target, this.curLevel);
    }

    private PropertyDefinition findPropertyDefinition(PropertyDefinitionList defList, String propertyid) {
        for (PropertyDefinition curr : defList) {
            if (!curr.getId().equals(propertyid)) continue;
            return curr;
        }
        return null;
    }

    private void compareDefinitionLists(PropertyDefinitionList sourceDefinitionList, PropertyDefinitionList targetDefinitionList, int curLevel) {
        if (sourceDefinitionList == null && targetDefinitionList == null) {
            return;
        }
        String sourcePrefix = this.getSourceHierarchyPrefix(curLevel);
        String targetPrefix = this.getTargetHierarchyPrefix(curLevel);
        if (sourceDefinitionList == null) {
            this.addPDDiff(targetPrefix, "Added", "PropertyDefinitionList added");
            this.targetHierarchy.add(curLevel, targetDefinitionList.getPropertyDefId());
            this.reportAllChildren(targetDefinitionList, curLevel, "TARGET");
            return;
        }
        if (targetDefinitionList == null) {
            this.addPDDiff(sourcePrefix, "Deleted", "PropertyDefinitionList deleted");
            this.sourceHierarchy.add(curLevel, sourceDefinitionList.getPropertyDefId());
            this.reportAllChildren(sourceDefinitionList, curLevel, "SOURCE");
            return;
        }
        for (PropertyDefinition sourcePropertyDefinition : sourceDefinitionList) {
            String propertyid = sourcePropertyDefinition.getId();
            PropertyDefinition targetPropertyDefinition = this.findPropertyDefinition(targetDefinitionList, propertyid);
            this.sourceHierarchy.add(curLevel, sourcePropertyDefinition.getId());
            if (targetPropertyDefinition != null) {
                this.targetHierarchy.add(curLevel, targetPropertyDefinition.getId());
            }
            ++curLevel;
            if (targetPropertyDefinition != null) {
                DataSet attrDiffs = this.compareDefinitionAttributes(sourcePropertyDefinition, targetPropertyDefinition, curLevel);
                attrDiffs = this.compareDefinitionListAttributes(sourcePropertyDefinition.getPropertyDefinitionList(), targetPropertyDefinition.getPropertyDefinitionList(), curLevel, attrDiffs);
                if (attrDiffs.getRowCount() > 0) {
                    this.addPDDiff(this.getSourceHierarchyPrefix(curLevel), "Changed", attrDiffs);
                }
                this.compareDefinitionLists(sourcePropertyDefinition.getPropertyDefinitionList(), targetPropertyDefinition.getPropertyDefinitionList(), curLevel);
            } else {
                this.addPDDiff(this.getSourceHierarchyPrefix(curLevel), "Deleted", "Property was Deleted");
            }
            --curLevel;
        }
        for (PropertyDefinition targetPropertyDefinition : targetDefinitionList) {
            this.targetHierarchy.add(curLevel, targetPropertyDefinition.getId());
            ++curLevel;
            PropertyDefinition sourcePropertyDefinition = this.findPropertyDefinition(sourceDefinitionList, targetPropertyDefinition.getId());
            if (sourcePropertyDefinition == null) {
                this.addPDDiff(this.getTargetHierarchyPrefix(curLevel), "Added", "Property was Added");
            }
            --curLevel;
        }
    }

    private DataSet compareDefinitionAttributes(PropertyDefinition source, PropertyDefinition target, int curLevel) {
        DataSet attrDiffs = new DataSet();
        String sourcePrefix = this.getSourceHierarchyPrefix(curLevel);
        String targetPrefix = this.getTargetHierarchyPrefix(curLevel);
        if (!source.getType().equals(target.getType())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Type", "Changed", source.getType(), target.getType());
        }
        if (!source.getTitle().equals(target.getTitle())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Title", "Changed", source.getTitle(), target.getTitle());
        }
        if (!source.getHelp().equals(target.getHelp())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Help", "Changed", source.getHelp(), target.getHelp());
        }
        if (!source.getSdcid().equals(target.getSdcid())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "SDCId", "Changed", source.getSdcid(), target.getSdcid());
        }
        if (!source.getEditor().equals(target.getEditor())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Editor", "Changed", source.getEditor(), target.getEditor());
        }
        if (!source.getExtendedWhere().equals(target.getExtendedWhere())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "ExtendedWhere", "Changed", source.getExtendedWhere(), target.getExtendedWhere());
        }
        if (!source.getValues().equals(target.getValues())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Values", "Changed", source.getValues(), target.getValues());
        }
        if (!source.getTranslate().equals(target.getTranslate())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Translate", "Changed", source.getTranslate() + "", target.getTranslate() + "");
        }
        if (!source.getMatchProperty().equals(target.getMatchProperty())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "MatchProperty", "Changed", source.getMatchProperty(), target.getMatchProperty());
        }
        if (source.isDeprecated() != target.isDeprecated()) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "isDeprecated", "Changed", source.isDeprecated() + "", target.isDeprecated() + "");
        }
        if (source.isAdvanced() != target.isAdvanced()) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "isAdvanced", "Changed", source.isAdvanced() + "", target.isAdvanced() + "");
        }
        if (source.isExpression() != target.isExpression()) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "isExpression", "Changed", source.isExpression() + "", target.isExpression() + "");
        }
        return attrDiffs;
    }

    private String getSourceHierarchyPrefix(int curLevel) {
        String prefix = "(ROOT)";
        for (int i = 0; i < curLevel; ++i) {
            prefix = prefix + "/" + this.sourceHierarchy.get(i);
        }
        return prefix;
    }

    private String getTargetHierarchyPrefix(int curLevel) {
        String prefix = "(ROOT)";
        for (int i = 0; i < curLevel; ++i) {
            prefix = prefix + "/" + this.targetHierarchy.get(i);
        }
        return prefix;
    }

    private DataSet compareDefinitionListAttributes(PropertyDefinitionList source, PropertyDefinitionList target, int curLevel, DataSet attrDiffs) {
        if (source == null || target == null) {
            return attrDiffs;
        }
        if (!source.getColor().equals(target.getColor())) {
            String description = "Color changed from " + source.getColor() + " to " + target.getColor() + " ";
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Color", "Changed", source.getColor(), target.getColor());
        }
        if (!source.getDirecttion().equals(target.getDirecttion())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "Direction", "Changed", source.getDirecttion(), target.getDirecttion());
        }
        if (!source.getLabelPlural().equals(target.getLabelPlural())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "LabelPlural", "Changed", source.getLabelPlural(), target.getLabelPlural());
        }
        if (!source.getLabelSingular().equals(target.getLabelSingular())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "LabelSingular", "Changed", source.getLabelSingular(), target.getLabelSingular());
        }
        if (!source.getTableStyle().equals(target.getTableStyle())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "TableStyle", "Changed", source.getTableStyle(), target.getTableStyle());
        }
        if (!source.getTitlePropertyId().equals(target.getTitlePropertyId())) {
            attrDiffs = this.addAttrDiffResult(attrDiffs, "TitlePropertyId", "Changed", source.getTitlePropertyId(), target.getTitlePropertyId());
        }
        return attrDiffs;
    }

    void reportAllChildren(PropertyDefinitionList newList, int curLevel, String sourceOrTarget) {
        if (newList == null) {
            return;
        }
        for (PropertyDefinition newChild : newList) {
            this.targetHierarchy.add(curLevel, newChild.getId());
            ++curLevel;
            if ("TARGET".equals(sourceOrTarget)) {
                this.addDiffResult(PROPERTY_DEFINITION, this.getTargetHierarchyPrefix(curLevel), "Added", "", newChild.getId(), "Property was Added");
            } else {
                this.addDiffResult(PROPERTY_DEFINITION, this.getSourceHierarchyPrefix(curLevel), "Deleted", "", newChild.getId(), "Property was deleted");
            }
            this.reportAllChildren(newChild.getPropertyDefinitionList(), curLevel, sourceOrTarget);
            --curLevel;
        }
    }

    DataSet addAttrDiffResult(DataSet attrDiffs, String identifier, String status, String oldValue, String newValue) {
        int newRow = attrDiffs.addRow();
        attrDiffs.setString(newRow, "level", identifier);
        attrDiffs.setString(newRow, "propertystatus", status);
        attrDiffs.setString(newRow, "oldvalue", oldValue);
        attrDiffs.setString(newRow, "newvalue", newValue);
        return attrDiffs;
    }

    void addPDDiff(String identifier, String status, DataSet attrDiffs) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, "level", identifier);
        this.results.setString(newRow, "propertystatus", status);
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("Attribute");
        titles.add("Status");
        titles.add("From");
        titles.add("To");
        this.results.setString(newRow, "description", attrDiffs.toHTML(titles, "prop"));
    }

    void addPDDiff(String identifier, String status, String description) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, "level", identifier);
        this.results.setString(newRow, "propertystatus", status);
        this.results.setString(newRow, "description", description);
    }
}

