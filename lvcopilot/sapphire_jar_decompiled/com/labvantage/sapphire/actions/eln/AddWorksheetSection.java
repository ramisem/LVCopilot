/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.AddWorksheetItemRef;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SectionBehaviorResolver;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class AddWorksheetSection
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        PropertyList wsOptions;
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"), wsOptions = new PropertyList())) {
            SDIData templateData;
            String templateid = properties.getProperty("templateid");
            String templateversionid = properties.getProperty("templateversionid");
            String sectiontemplateid = properties.getProperty("sectiontemplateid");
            String sectiontemplateversionid = properties.getProperty("sectiontemplateversionid");
            ArrayList templatesections = null;
            DataSet templateitems = null;
            if (templateid.length() > 0) {
                if (templateversionid.length() == 0 || templateversionid.equalsIgnoreCase("C")) {
                    templateversionid = BaseELNAction.resolveVersion(this.getQueryProcessor(), templateid, templateversionid, "worksheet");
                }
                templateData = this.loadWorksheet(templateid, templateversionid, new PropertyList());
                templatesections = templateData.getSDIData("sections").getDataset("primary");
                templateitems = templateData.getSDIData("items").getDataset("primary");
            } else if (sectiontemplateid.length() > 0) {
                templateData = this.loadSection(properties.getProperty("fromworksheetid"), properties.getProperty("fromworksheetversionid"), sectiontemplateid, sectiontemplateversionid, true, false);
                templatesections = templateData.getDataset("primary");
                templateitems = templateData.getSDIData("items").getDataset("primary");
            }
            int maxseq = 0;
            int seqinc = templatesections == null ? 1 : templatesections.size();
            this.database.createPreparedResultSet("SELECT max(usersequence) maxseq FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
            if (this.database.getNext()) {
                maxseq = this.database.getInt("maxseq");
            }
            int afterusersequence = -1;
            try {
                afterusersequence = Integer.parseInt(properties.getProperty("afterusersequence", "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            int usersequence = -1;
            try {
                usersequence = Integer.parseInt(properties.getProperty("usersequence", "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (usersequence == -1) {
                usersequence = maxseq + 1;
            }
            if (maxseq > 0 && usersequence <= maxseq) {
                this.database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = usersequence + " + seqinc + " WHERE worksheetid = ? AND worksheetversionid = ? AND usersequence >= ?", new Object[]{worksheetid, worksheetversionid, usersequence});
                properties.setProperty("usersequenceupdate", "Y");
            }
            int level = 1;
            try {
                level = Integer.parseInt(properties.getProperty("sectionlevel", "1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (level == 0 && properties.getProperty("worksheetdefaultsection", "N").equals("Y")) {
                throw new SapphireException("You cannot create a level 0 section");
            }
            SectionBehaviorResolver sbr = new SectionBehaviorResolver(this.getQueryProcessor());
            ActionBlock ab = new ActionBlock();
            String sectiondesc = properties.getProperty("sectiondesc");
            if (templateid.length() == 0 && sectiontemplateid.length() == 0) {
                PropertyList wssProps = new PropertyList();
                wssProps.setProperty("sdcid", "LV_WorksheetSection");
                wssProps.setProperty("worksheetsectionversionid", "1");
                if (wsOptions.getProperty("keygenrule").equals(TEMPLATE_KEYGENRULE)) {
                    wssProps.setProperty("worksheetsectionid", this.getTemplateDetailKey(worksheetid));
                    wssProps.setProperty("overrideautokey", "Y");
                }
                wssProps.setProperty("worksheetsectiondesc", sectiondesc);
                wssProps.setProperty("worksheetid", worksheetid);
                wssProps.setProperty("worksheetversionid", worksheetversionid);
                wssProps.setProperty("sectionlevel", String.valueOf(level));
                wssProps.setProperty("usersequence", String.valueOf(usersequence));
                wssProps.setProperty("sectionstatus", properties.getProperty("sectionstatus", "InProgress"));
                wssProps.setProperty("availabilityflag", "Y");
                PropertyList sectionDefaults = wsOptions.getPropertyList("sectiondefaults");
                wssProps.setProperty("options", sectionDefaults != null ? new PropertyList(sectionDefaults).toXMLString() : "");
                wssProps.setProperty("worksheet_action", "Y");
                wssProps.setProperty("excludeapproval", "Y");
                wssProps.setProperty("excludeapprovalstep", "Y");
                ab.setAction("AddWorksheetSection", "AddSDI", "1", wssProps);
                String approvaltypeid = properties.getProperty("approvaltypeid", wsOptions.getProperty("sectionapprovaltype"));
                if (approvaltypeid.length() > 0) {
                    PropertyList approvalProps = new PropertyList();
                    approvalProps.put("sdcid", "LV_WorksheetSection");
                    approvalProps.put("keyid1", "[$G{AddWorksheetSection.newkeyid1}]");
                    approvalProps.put("keyid2", "[$G{AddWorksheetSection.newkeyid2}]");
                    approvalProps.put("approvaltypeid", approvaltypeid);
                    ab.setAction("SDIApproval", "AddSDIApproval", "1", approvalProps);
                }
            } else {
                for (int i = 0; i < templatesections.size(); ++i) {
                    int sourceSectionLevel = ((DataSet)templatesections).getInt(i, "sectionlevel");
                    if (sourceSectionLevel <= 0) continue;
                    int sectionSequence = usersequence + i;
                    String fromSectionid = ((DataSet)templatesections).getValue(i, "worksheetsectionid");
                    String fromSectionVersionid = ((DataSet)templatesections).getValue(i, "worksheetsectionversionid");
                    sbr.addSectionToSequence(fromSectionid, fromSectionVersionid, sectionSequence);
                    PropertyList wssProps = new PropertyList();
                    wssProps.setProperty("sdcid", "LV_WorksheetSection");
                    wssProps.setProperty("worksheetsectionversionid", "1");
                    wssProps.setProperty("worksheetid", worksheetid);
                    wssProps.setProperty("worksheetversionid", worksheetversionid);
                    wssProps.setProperty("usersequence", String.valueOf(sectionSequence));
                    wssProps.setProperty("sectionstatus", properties.getProperty("sectionstatus", "InProgress"));
                    wssProps.setProperty("availabilityflag", "Y");
                    wssProps.setProperty("templatekeyid1", fromSectionid);
                    wssProps.setProperty("templatekeyid2", fromSectionVersionid);
                    wssProps.setProperty("templateid", templateid);
                    wssProps.setProperty("templateversionid", templateversionid);
                    wssProps.setProperty("worksheet_action", "Y");
                    wssProps.setProperty("setattributedefaults", "Y");
                    wssProps.setProperty("forcecopydownattributes", "Y");
                    wssProps.setProperty("copyattachment", "Y");
                    wssProps.setProperty("excludeapproval", "Y");
                    wssProps.setProperty("excludeapprovalstep", "Y");
                    ab.setAction("AddWorksheetSection_" + i, "AddSDI", "1", wssProps);
                    PropertyList options = new PropertyList();
                    try {
                        options.setJSONString(((DataSet)templatesections).getValue(i, "options", "{}"));
                    }
                    catch (JSONException jSONException) {
                        // empty catch block
                    }
                    String approvaltypeid = properties.getProperty("approvaltypeid", options.getProperty("sectionapprovaltype", wsOptions.getProperty("sectionapprovaltype")));
                    if (approvaltypeid.length() <= 0) continue;
                    PropertyList approvalProps = new PropertyList();
                    approvalProps.put("sdcid", "LV_WorksheetSection");
                    approvalProps.put("keyid1", "[$G{AddWorksheetSection_" + i + ".newkeyid1}]");
                    approvalProps.put("keyid2", "[$G{AddWorksheetSection_" + i + ".newkeyid2}]");
                    approvalProps.put("approvaltypeid", approvaltypeid);
                    ab.setAction("SDIApproval_" + i, "AddSDIApproval", "1", approvalProps);
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int i = 0; i < templatesections.size(); ++i) {
                    int sourceSectionLevel = ((DataSet)templatesections).getInt(i, "sectionlevel");
                    filter.put("worksheetsectionid", ((DataSet)templatesections).getValue(i, "worksheetsectionid"));
                    filter.put("worksheetsectionversionid", ((DataSet)templatesections).getValue(i, "worksheetsectionversionid"));
                    DataSet sectionitems = templateitems.getFilteredDataSet(filter);
                    int sectionSequence = usersequence + i;
                    String targetworksheetsectionid = "[$G{AddWorksheetSection_" + i + ".newkeyid1}]";
                    String targetworksheetsectionversionid = "[$G{AddWorksheetSection_" + i + ".newkeyid2}]";
                    if (sectionitems.size() > 0 && sourceSectionLevel == 0) {
                        DataSet targetSectionDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=? AND " + (afterusersequence >= 0 ? "usersequence=" + afterusersequence : "sectionlevel=0"), new Object[]{worksheetid, worksheetversionid});
                        if (targetSectionDS.size() > 0) {
                            targetworksheetsectionid = targetSectionDS.getValue(0, "worksheetsectionid");
                            targetworksheetsectionversionid = targetSectionDS.getValue(0, "worksheetsectionversionid");
                        } else {
                            sectionitems.clear();
                        }
                    }
                    for (int j = 0; j < sectionitems.size(); ++j) {
                        String fromItemid = sectionitems.getValue(j, "worksheetitemid");
                        String fromItemVersionid = sectionitems.getValue(j, "worksheetitemversionid");
                        int itemSequence = sectionitems.getInt(j, "usersequence");
                        sbr.addItemToSequence(fromItemid, fromItemVersionid, sectionSequence, itemSequence);
                        PropertyList wsiProps = new PropertyList();
                        wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                        wsiProps.setProperty("worksheetitemversionid", "1");
                        wsiProps.setProperty("worksheetid", worksheetid);
                        wsiProps.setProperty("worksheetversionid", worksheetversionid);
                        wsiProps.setProperty("worksheetsectionid", targetworksheetsectionid);
                        wsiProps.setProperty("worksheetsectionversionid", targetworksheetsectionversionid);
                        wsiProps.setProperty("itemstatus", properties.getProperty("itemstatus", "InProgress"));
                        wsiProps.setProperty("availabilityflag", "Y");
                        wsiProps.setProperty("templatekeyid1", fromItemid);
                        wsiProps.setProperty("templatekeyid2", fromItemVersionid);
                        wsiProps.setProperty("templateid", templateid);
                        wsiProps.setProperty("templateversionid", templateversionid);
                        ab.setBlockProperty("WSI_templateid_" + i + "_" + j, fromItemid);
                        wsiProps.setProperty("worksheet_action", "Y");
                        wsiProps.setProperty("setattributedefaults", "Y");
                        wsiProps.setProperty("forcecopydownattributes", "Y");
                        wsiProps.setProperty("addfromtemplate", "Y");
                        wsiProps.setProperty("copyattachment", "Y");
                        ab.setAction("AddWorksheetItem_" + i + "_" + j, "AddSDI", "1", wsiProps);
                        if (sectiontemplateid.length() <= 0) continue;
                        PropertyList refProps = new PropertyList();
                        refProps.setProperty("worksheetid", worksheetid);
                        refProps.setProperty("worksheetversionid", worksheetversionid);
                        refProps.setProperty("worksheetitemid", "[$G{AddWorksheetItem_" + i + "_" + j + ".newkeyid1}]");
                        refProps.setProperty("worksheetitemversionid", "[$G{AddWorksheetItem_" + i + "_" + j + ".newkeyid2}]");
                        refProps.setProperty("refworksheetid", properties.getProperty("fromworksheetid"));
                        refProps.setProperty("refworksheetversionid", properties.getProperty("fromworksheetversionid"));
                        refProps.setProperty("refsdcid", "LV_WorksheetItem");
                        refProps.setProperty("refkeyid1", fromItemid);
                        refProps.setProperty("refkeyid2", fromItemVersionid);
                        refProps.setProperty("reffunction", "copy");
                        ab.setActionClass("AddRef_" + i + "_" + j, AddWorksheetItemRef.class.getName(), refProps);
                    }
                }
            }
            this.getActionProcessor().processActionBlock(ab);
            sbr.resolveBehaviorReferences(worksheetid, worksheetversionid);
            if (templateid.length() == 0 && sectiontemplateid.length() == 0) {
                properties.setProperty("worksheetsectionid", ab.getActionProperty("AddWorksheetSection", "newkeyid1"));
                properties.setProperty("worksheetsectionversionid", ab.getActionProperty("AddWorksheetSection", "newkeyid2"));
            } else {
                StringBuffer sectionid = new StringBuffer();
                StringBuffer sectionversion = new StringBuffer();
                for (int i = 0; i < templatesections.size(); ++i) {
                    int sourceSectionLevel = ((DataSet)templatesections).getInt(i, "sectionlevel");
                    if (sourceSectionLevel <= 0) continue;
                    sectionid.append(";").append(ab.getActionProperty("AddWorksheetSection_" + i, "newkeyid1"));
                    sectionversion.append(";").append(ab.getActionProperty("AddWorksheetSection_" + i, "newkeyid2"));
                }
                properties.setProperty("worksheetsectionid", sectionid.length() > 0 ? sectionid.substring(1) : "");
                properties.setProperty("worksheetsectionversionid", sectionversion.length() > 0 ? sectionversion.substring(1) : "");
            }
            properties.setProperty("sectionsadded", String.valueOf(seqinc));
            String newsectionid = properties.getProperty("worksheetsectionid");
            String newsectionversion = properties.getProperty("worksheetsectionversionid");
            if (newsectionid.length() == 0) {
                this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_Worksheet", worksheetid, worksheetversionid, "Added new items from section template " + AddWorksheetSection.getIdVersionText(templateid, templateversionid));
            } else if (newsectionid.contains(";")) {
                newsectionid = newsectionid.substring(0, newsectionid.indexOf(";") - 1);
                newsectionversion = newsectionversion.substring(0, newsectionversion.indexOf(";") - 1);
                this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_WorksheetSection", newsectionid, newsectionversion, "Added new section" + (seqinc > 1 ? "s" : "") + " from section template " + AddWorksheetSection.getIdVersionText(templateid, templateversionid));
            } else {
                this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_WorksheetSection", newsectionid, newsectionversion, templateid.length() == 0 ? "Added new section" + (sectiondesc.length() > 0 ? " '" + sectiondesc + "'" : "") : "Added new section" + (seqinc > 1 ? "s" : "") + " from template " + AddWorksheetSection.getIdVersionText(templateid, templateversionid));
            }
        }
    }
}

