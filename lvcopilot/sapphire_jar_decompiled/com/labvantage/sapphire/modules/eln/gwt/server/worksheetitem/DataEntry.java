/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.gwt.server.GWTDataEntry;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntry
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        this.setClientRenderer("DataEntry");
        String sdcid = this.config.getProperty("sourcesdcid");
        if (sdcid.length() > 0) {
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), sdcid);
            worksheetItemOptions.setSupportsDataAvailablity(true);
            worksheetItemOptions.setHasExportHTML(this.config.getProperty("dataexportview").length() > 0 && !this.config.getProperty("dataexportview").equals(this.config.getProperty("dataentryview")));
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
            worksheetItemOptions.setEditorMaxSize(this.config.getProperty("openmaximized", "N").equals("Y"));
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-OPAL/pagetypes/dataentry/scripts/dataentry.js");
        worksheetItemIncludes.addScriptInclude("WEB-OPAL/pagetypes/reagent/scripts/advancedtoolbar_dataentry.js");
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        String gDEviewpageid = itemConfig.getProperty("dataentryview");
        if (gDEviewpageid.length() == 0) {
            this.worksheetItemOptions.setRequiresConfig(true, "Data Entry View Not Configured.");
        } else {
            String source = itemConfig.getProperty("source");
            String sourcesdcid = itemConfig.getProperty("sourcesdcid");
            DataSet itemSDIDataSet = null;
            itemSDIDataSet = "Query".equals(source) ? this.getQueryDataSet(sourcesdcid) : this.getItemSDIDataSet();
            if (itemSDIDataSet == null || itemSDIDataSet.getRowCount() == 0) {
                boolean isTemplate = this.isTemplate();
                if (isTemplate) {
                    return this.getViewHTML();
                }
                this.worksheetItemOptions.setRequiresConfig(true, "Query".equals(source) ? "Query execution retrieved no SDIs." : "Not Worksheet Item SDIs Configured. Data Entry Control is available after configuring item SDIs");
            } else {
                return "";
            }
        }
        return "";
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getViewHTML(true);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getViewHTML(false);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String getViewHTML(boolean export) throws SapphireException {
        try {
            PropertyList itemConfig = this.getConfig();
            if (export && itemConfig.getProperty("dataexportview").length() > 0) {
                itemConfig.setProperty("dataentryview", itemConfig.getProperty("dataexportview"));
                itemConfig.setProperty("useworkitempreferredview", "N");
            }
            PropertyList gridelementProps = this.getGridElementProperty();
            if ("Y".equals(itemConfig.getProperty("showunavailabledatainviewmode"))) {
                gridelementProps.setProperty("dataitemdisplayrule", "");
            }
            GWTDataEntry gwtDataEntry = this.getGWTDataEntry(gridelementProps);
            String source = itemConfig.getProperty("source");
            String sourcesdcid = itemConfig.getProperty("sourcesdcid");
            SDIData sdiData = null;
            DataSet itemSDIDataSet = null;
            itemSDIDataSet = "Query".equals(source) ? this.getQueryDataSet(sourcesdcid) : this.getItemSDIDataSet();
            if (itemSDIDataSet == null || itemSDIDataSet.getRowCount() == 0) {
                this.setAvailability("NoData");
                boolean isTemplate = this.isTemplate();
                String noSDIMsg = this.config.getProperty("nosdiavailablemessage");
                if (!isTemplate) return noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : this.getTranslationProcessor().translate("No Item SDIs To View");
                sdiData = this.getTemplateDummySDIData();
            } else {
                sdiData = this.getDataEntrySDIData(itemSDIDataSet, gridelementProps, false);
            }
            if (sdiData != null) {
                gwtDataEntry.setSDIData(sdiData);
                this.setPreferredDataEntryView(itemConfig, sdiData, gwtDataEntry);
                String viewHtml = gwtDataEntry.getViewHTML();
                if (viewHtml.indexOf("No qualified data items found for data entry") >= 0) {
                    this.setAvailability("Unavailable");
                }
                if ("Y".equals(itemConfig.getProperty("showunavailabledatainviewmode"))) {
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("availabilityflag", "N");
                    DataSet alldatset = sdiData.getDataset("dataset");
                    DataSet unavailablesdidataset = alldatset.getFilteredDataSet(filterMap);
                    if (unavailablesdidataset != null && unavailablesdidataset.getRowCount() == alldatset.getRowCount()) {
                        this.setAvailability("Unavailable");
                    }
                }
                StringBuffer viewSelectHtml = new StringBuffer();
                StringBuffer html = new StringBuffer(gwtDataEntry.getViewHTML());
                if (!export) return html.toString();
                HTMLEditorControl.removeAnchors(html);
                return html.toString();
            }
            this.setAvailability("NoData");
            String noSDIMsg = this.config.getProperty("nosdiavailablemessage");
            return noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : this.getTranslationProcessor().translate("No Item SDIs To View");
        }
        catch (SapphireException se) {
            this.logError(se.getMessage(), se);
            return se.getMessage();
        }
    }

    @Override
    public String getContents() {
        try {
            PropertyList gridelementProps = this.getGridElementProperty();
            SDIData sdiData = this.getDataEntrySDIData(gridelementProps, true);
            GWTDataEntry gwtDataEntry = this.getGWTDataEntry(gridelementProps);
            gwtDataEntry.setSDIData(sdiData);
            this.setPreferredDataEntryView(this.getConfig(), sdiData, gwtDataEntry);
            return gwtDataEntry.getJSONString();
        }
        catch (SapphireException se) {
            this.logError("Error get contents:" + se.getMessage(), se);
            return se.getMessage();
        }
    }

    private void setPreferredDataEntryView(PropertyList itemConfig, SDIData sdiData, GWTDataEntry gwtDataEntry) throws SapphireException {
        if (itemConfig.getProperty("useworkitempreferredview").equals("Y")) {
            DataSet sdiworkitemDS = sdiData.getDataset("sdiworkitem");
            DataSet sdidataset = sdiData.getDataset("dataset");
            HashSet<String> sourceWISet = new HashSet<String>();
            for (int i = 0; i < sdidataset.getRowCount(); ++i) {
                if (sdidataset.getValue(i, "sourceworkitemid").length() <= 0) continue;
                sourceWISet.add(sdidataset.getValue(i, "sourceworkitemid") + ";" + sdidataset.getValue(i, "sourceworkiteminstance"));
            }
            String preferredview = "";
            if (sdiworkitemDS != null) {
                for (int i = 0; i < sdiworkitemDS.getRowCount(); ++i) {
                    if (sdiworkitemDS.getValue(i, "preferreddataentryview").length() <= 0 || !sourceWISet.contains(sdiworkitemDS.getValue(i, "workitemid") + ";" + sdiworkitemDS.getValue(i, "workiteminstance"))) continue;
                    if (preferredview.length() == 0) {
                        preferredview = sdiworkitemDS.getValue(i, "preferreddataentryview");
                        continue;
                    }
                    if (preferredview.equals(sdiworkitemDS.getValue(i, "preferreddataentryview"))) continue;
                    preferredview = "";
                    break;
                }
            }
            if (preferredview.length() > 0 && !preferredview.equals(itemConfig.getProperty("dataentryview"))) {
                itemConfig.setProperty("dataentryview", preferredview);
                gwtDataEntry.setElementProperties(this.getGridElementProperty());
            }
        }
    }

    private boolean isCancelledDataItem(DataSet sdidataitem, int indx, DataSet cancelledDS) {
        if (cancelledDS == null || cancelledDS.size() == 0) {
            return false;
        }
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("keyid1", sdidataitem.getValue(indx, "keyid1", ""));
        hm.put("paramlistid", sdidataitem.getValue(indx, "paramlistid", ""));
        hm.put("paramlistversionid", sdidataitem.getValue(indx, "paramlistversionid", ""));
        hm.put("variantid", sdidataitem.getValue(indx, "variantid", ""));
        hm.put("dataset", sdidataitem.getBigDecimal(indx, "dataset"));
        int findrow = cancelledDS.findRow(hm);
        return findrow > -1;
    }

    @Override
    public String validateStateChange(String fromStatus, String toStatus) throws SapphireException {
        try {
            if (!fromStatus.equals(toStatus)) {
                PropertyList element;
                SDIData sdiData;
                if (fromStatus.equals("InProgress") && toStatus.equals("Complete")) {
                    String validateFailMessage = "";
                    PropertyList element2 = this.getGridElementProperty();
                    SDIData sdiData2 = this.getDataEntrySDIData(element2, false);
                    if (sdiData2 != null) {
                        DataSet sdidataitem = sdiData2.getDataset("dataitem");
                        DataSet sdidataset = sdiData2.getDataset("dataset");
                        TranslationProcessor tp = this.getTranslationProcessor();
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("s_datasetstatus", "Cancelled");
                        DataSet cancelledDS = sdidataset.getFilteredDataSet(hm);
                        ArrayList cancelledList = new ArrayList();
                        PropertyList policy = new ConfigurationProcessor(this.getSapphireConnection().getConnectionId()).getPolicy("DataEntryPolicy", "Sapphire Custom");
                        String allowreleaseblank = policy.getProperty("allowreleaseblank");
                        for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
                            if (this.isCancelledDataItem(sdidataitem, i, cancelledDS)) {
                                cancelledList.add(sdidataitem.get(i));
                                continue;
                            }
                            if (!"Y".equals(allowreleaseblank) && "Y".equals(sdidataitem.getValue(i, "mandatoryflag")) && sdidataitem.getValue(i, "enteredtext").length() == 0) {
                                HashMap<String, Object> findds = new HashMap<String, Object>();
                                findds.put("keyid1", sdidataitem.getValue(i, "keyid1", ""));
                                findds.put("paramlistid", sdidataitem.getValue(i, "paramlistid", ""));
                                findds.put("paramlistversionid", sdidataitem.getValue(i, "paramlistversionid", ""));
                                findds.put("variantid", sdidataitem.getValue(i, "variantid", ""));
                                findds.put("dataset", sdidataitem.getBigDecimal(i, "dataset"));
                                int findrow = sdidataset.findRow(findds);
                                if (findrow > -1) {
                                    String sourceWorkItemId = sdidataset.getValue(findrow, "sourceworkitemid");
                                    String sourceWorkItemInstance = sdidataset.getValue(findrow, "sourceworkiteminstance");
                                    if (sourceWorkItemId.length() > 0) {
                                        String sdcid = sdidataset.getValue(findrow, "sdcid");
                                        String keyid1 = sdidataset.getValue(findrow, "keyid1");
                                        String keyid2 = sdidataset.getValue(findrow, "keyid2");
                                        String keyid3 = sdidataset.getValue(findrow, "keyid3");
                                        String paramlistid = sdidataset.getValue(findrow, "paramlistid", "");
                                        String paramlistversionid = sdidataset.getValue(findrow, "paramlistversionid", "");
                                        String variantid = sdidataset.getValue(findrow, "variantid", "");
                                        String dataset = sdidataset.getValue(findrow, "dataset", "");
                                        DataSet workitem = this.getQueryProcessor().getPreparedSqlDataSet("Select wi.workitemstatus,wii.mandatoryflag from sdiworkitem wi,sdiworkitemitem wii  where wi.sdcid = ? and wi.keyid1 = ? and wi.keyid2 = ? and wi.keyid3 = ? and wi.workitemid = ? and wi.workiteminstance = ?  and wii.sdcid=wi.sdcid and wii.keyid1=wi.keyid1 and wii.keyid2=wi.keyid2 and wii.keyid3=wi.keyid3  and wii.itemsdcid='ParamList' and wii.itemkeyid1=?  and wii.itemkeyid2=? and wii.itemkeyid3=? and wii.iteminstance=?", new Object[]{sdcid, keyid1, keyid2, keyid3, sourceWorkItemId, sourceWorkItemInstance, paramlistid, paramlistversionid, variantid, dataset});
                                        if (workitem.getRowCount() > 0 && ("Completed".equalsIgnoreCase(workitem.getValue(0, "workitemstatus")) || !workitem.getValue(0, "mandatoryflag", "").equalsIgnoreCase("Y"))) {
                                            return "";
                                        }
                                    }
                                }
                                validateFailMessage = tp.translate("Mandatory item not filled for") + " " + sdidataitem.getValue(i, "paramlistid") + ":" + sdidataitem.getValue(i, "paramid") + "(rep." + sdidataitem.getValue(i, "replicateid") + ")";
                                break;
                            }
                            if (sdidataitem.getValue(i, "valuestatus").length() <= 0) continue;
                            validateFailMessage = tp.translate("Data item has error for") + " " + sdidataitem.getValue(i, "paramlistid") + ":" + sdidataitem.getValue(i, "paramid") + "(rep." + sdidataitem.getValue(i, "replicateid") + "): " + sdidataitem.getValue(i, "valuestatus");
                            break;
                        }
                        sdidataitem.removeAll(cancelledList);
                        if (validateFailMessage.length() > 0) {
                            return validateFailMessage;
                        }
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("releasedflag", "Y");
                        DataSet notreleasedDataitem = sdidataitem.getFilteredDataSet(filterMap, true);
                        String[] releasedataitemprops = new String[]{"keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                        if (notreleasedDataitem.getRowCount() > 0) {
                            PropertyList props = new PropertyList();
                            props.put("sdcid", sdiData2.getSdcid());
                            for (int p = 0; p < releasedataitemprops.length; ++p) {
                                props.put(releasedataitemprops[p], notreleasedDataitem.getColumnValues(releasedataitemprops[p], ";"));
                            }
                            ActionBlock actionBlock = new ActionBlock();
                            if ("Y".equals(allowreleaseblank)) {
                                props.put("allowmandatorynulls", "Y");
                            }
                            actionBlock.setAction("ReleaseDataItem", "ReleaseDataItem", "1", props);
                            this.addPostDataEntryActionsToBlock(actionBlock, sdiData2.getSdcid(), sdidataset, element2, true, false);
                            this.getActionProcessor().processActionBlock(actionBlock);
                        }
                        return "";
                    }
                } else if (fromStatus.equals("Complete") && toStatus.equals("InProgress") && (sdiData = this.getDataEntrySDIData(element = this.getGridElementProperty(), false)) != null) {
                    DataSet sdidataitem = sdiData.getDataset("dataitem");
                    DataSet sdidataset = sdiData.getDataset("dataset");
                    DataSet primary = sdiData.getDataset("primary");
                    if ("Sample".equals(sdiData.getSdcid()) && sdiData.getDataset("primary").findRow("samplestatus", "Reviewed") > 0) {
                        sdidataitem.addColumn("reviewed", 0);
                        for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
                            String keyid1 = sdidataitem.getValue(i, "keyid1");
                            int row = primary.findRow("s_sampleid", keyid1);
                            if (row <= 0 || "Reviewed".equals(primary.getValue(row, "samplestatus"))) continue;
                            sdidataitem.setValue(i, "reviewed", "N");
                        }
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("reviewed", "N");
                        sdidataitem = sdidataitem.getFilteredDataSet(filterMap);
                    }
                    if (sdidataitem.getRowCount() > 0) {
                        String[] releasedataitemprops = new String[]{"keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                        if (sdidataitem.getRowCount() > 0) {
                            PropertyList props = new PropertyList();
                            props.put("sdcid", sdiData.getSdcid());
                            for (int p = 0; p < releasedataitemprops.length; ++p) {
                                props.put(releasedataitemprops[p], sdidataitem.getColumnValues(releasedataitemprops[p], ";"));
                            }
                            ActionBlock actionBlock = new ActionBlock();
                            actionBlock.setAction("UnReleaseDataItem", "UnReleaseDataItem", "1", props);
                            this.addPostDataEntryActionsToBlock(actionBlock, sdiData.getSdcid(), sdidataset, element, true, false);
                            this.getActionProcessor().processActionBlock(actionBlock);
                        }
                    }
                }
            }
        }
        catch (SapphireException se) {
            return se.getMessage();
        }
        return "";
    }

    @Override
    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        PropertyList element;
        SDIData sdiData;
        if ((toStatus.equals("Complete") || toStatus.equals("PendingApproval")) && "N".equals(this.worksheetItemOptions.getOption("itemcompletion"))) {
            return this.validateStateChange(fromStatus, "Complete");
        }
        if ((toStatus.equals("Complete") || toStatus.equals("PendingApproval") || fromStatus.equals("PendingApproval") && toStatus.equals("Approve")) && (sdiData = this.getDataEntrySDIData(element = this.getGridElementProperty(), false)) != null) {
            DataSet sdidataset = sdiData.getDataset("dataset");
            HashSet<String> completedSet = new HashSet<String>();
            for (int i = 0; i < sdidataset.getRowCount(); ++i) {
                if (!"Completed".equals(sdidataset.getValue(i, "s_datasetstatus")) && !"Cancelled".equals(sdidataset.getValue(i, "s_datasetstatus"))) continue;
                completedSet.add(sdidataset.getValue(i, "keyid1") + ";" + sdidataset.getValue(i, "keyid2") + ";" + sdidataset.getValue(i, "keyid3") + ";" + sdidataset.getValue(i, "paramlistid") + ";" + sdidataset.getValue(i, "paramlistversionid") + ";" + sdidataset.getValue(i, "variantid") + ";" + sdidataset.getValue(i, "dataset"));
            }
            if (!toStatus.equals("PendingApproval")) {
                DataSet dataapproval = sdiData.getDataset("dataapproval");
                boolean approvedDataSetCalled = false;
                if (dataapproval.getRowCount() > 0) {
                    StringBuffer keyid1 = new StringBuffer();
                    StringBuffer keyid2 = new StringBuffer();
                    StringBuffer keyid3 = new StringBuffer();
                    StringBuffer paramlistid = new StringBuffer();
                    StringBuffer paramlistversionid = new StringBuffer();
                    StringBuffer variantid = new StringBuffer();
                    StringBuffer dataset = new StringBuffer();
                    StringBuffer approvalstep = new StringBuffer();
                    StringBuffer approvalflag = new StringBuffer();
                    StringBuffer notes = new StringBuffer();
                    String sdcid = dataapproval.getValue(0, "sdcid");
                    for (int i = 0; i < dataapproval.getRowCount(); ++i) {
                        if (completedSet.contains(dataapproval.getValue(i, "keyid1") + ";" + dataapproval.getValue(i, "keyid2") + ";" + dataapproval.getValue(i, "keyid3") + ";" + dataapproval.getValue(i, "paramlistid") + ";" + dataapproval.getValue(i, "paramlistversionid") + ";" + dataapproval.getValue(i, "variantid") + ";" + dataapproval.getValue(i, "dataset"))) continue;
                        keyid1.append(";" + dataapproval.getValue(i, "keyid1"));
                        keyid2.append(";" + dataapproval.getValue(i, "keyid2"));
                        keyid3.append(";" + dataapproval.getValue(i, "keyid3"));
                        paramlistid.append(";" + dataapproval.getValue(i, "paramlistid"));
                        paramlistversionid.append(";" + dataapproval.getValue(i, "paramlistversionid"));
                        variantid.append(";" + dataapproval.getValue(i, "variantid"));
                        dataset.append(";" + dataapproval.getValue(i, "dataset"));
                        approvalstep.append(";" + dataapproval.getValue(i, "approvalstep"));
                        approvalflag.append(";P");
                        notes.append(";Auto approved by Worksheet Completion");
                    }
                    if (keyid1.length() > 1) {
                        boolean raiseError;
                        PropertyList itemConfig = this.getConfig();
                        boolean bl = raiseError = !"Y".equalsIgnoreCase(itemConfig.getProperty("autoapprovesdidataoncompletion", "Y"));
                        if (raiseError) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Worksheet cannot be completed as there are pending datasets."));
                        }
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", sdcid);
                        props.setProperty("keyid1", keyid1.substring(1));
                        props.setProperty("keyid2", keyid2.substring(1));
                        props.setProperty("keyid3", keyid3.substring(1));
                        props.setProperty("paramlistid", paramlistid.substring(1));
                        props.setProperty("paramlistversionid", paramlistversionid.substring(1));
                        props.setProperty("variantid", variantid.substring(1));
                        props.setProperty("dataset", dataset.substring(1));
                        props.setProperty("approvalstep", approvalstep.substring(1));
                        props.setProperty("approvalflag", approvalflag.substring(1));
                        props.setProperty("notes", notes.substring(1));
                        props.setProperty("propsmatch", "Y");
                        this.getActionProcessor().processAction("EditDataApproval", "1", props);
                        approvedDataSetCalled = true;
                    }
                }
                if (sdidataset.getRowCount() > 0) {
                    ActionBlock actionBlock = new ActionBlock();
                    this.addPostDataEntryActionsToBlock(actionBlock, sdiData.getSdcid(), sdidataset, element, false, true);
                    this.getActionProcessor().processActionBlock(actionBlock);
                }
            }
            sdiData = this.getDataEntrySDIData(element, false);
            sdidataset = sdiData.getDataset("dataset");
            HashMap<String, String> findavailabilityMap = new HashMap<String, String>();
            findavailabilityMap.put("availabilityflag", "Y");
            if (sdidataset.findRow(findavailabilityMap) >= 0) {
                for (int i = 0; i < sdidataset.getRowCount(); ++i) {
                    if ("Completed".equals(sdidataset.getValue(i, "s_datasetstatus")) || "Cancelled".equals(sdidataset.getValue(i, "s_datasetstatus")) || "N".equals(this.worksheetItemOptions.getOption("itemcompletion"))) continue;
                    String sourceWorkItemId = sdidataset.getValue(i, "sourceworkitemid");
                    String sourceWorkItemInstance = sdidataset.getValue(i, "sourceworkiteminstance");
                    if (sourceWorkItemId.length() > 0) {
                        String sdcid = sdidataset.getValue(i, "sdcid");
                        String keyid1 = sdidataset.getValue(i, "keyid1");
                        String keyid2 = sdidataset.getValue(i, "keyid2");
                        String keyid3 = sdidataset.getValue(i, "keyid3");
                        DataSet workitem = this.getQueryProcessor().getPreparedSqlDataSet("Select workitemstatus from sdiworkitem  where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, sourceWorkItemId, sourceWorkItemInstance});
                        if (workitem.getRowCount() > 0 && "Completed".equalsIgnoreCase(workitem.getValue(0, "workitemstatus"))) {
                            return "";
                        }
                    }
                    if (toStatus.equals("PendingApproval")) {
                        if ("Released".equals(sdidataset.getValue(i, "s_datasetstatus"))) continue;
                        return this.getTranslationProcessor().translate("Cannot submit worksheet for approval.") + " " + sdidataset.getValue(i, "keyid1") + "," + sdidataset.getValue(i, "paramlistid") + " " + this.getTranslationProcessor().translate("not in released or complete state");
                    }
                    return this.getTranslationProcessor().translate("Cannot complete worksheet.") + sdidataset.getValue(i, "keyid1") + "," + sdidataset.getValue(i, "paramlistid") + " " + this.getTranslationProcessor().translate("not in complete state");
                }
            }
        }
        return "";
    }

    private void addPostDataEntryActionsToBlock(ActionBlock actionBlock, String sdcid, DataSet sdidataDataset, PropertyList element, boolean postDataentry, boolean wsComplete) throws SapphireException {
        PropertyList props;
        boolean isSyncPrimaryStatus = element != null && !"N".equals(element.getProperty("syncprimarystatus"));
        boolean isSyncAQCStatus = element != null && !"N".equals(element.getProperty("syncaqcstatus"));
        String primarystatusColumn = element != null ? element.getProperty("primarystatuscolumn") : "";
        String keyid1 = sdidataDataset.getColumnValues("keyid1", ";");
        String keyid2 = sdidataDataset.getColumnValues("keyid2", ";");
        String keyid3 = sdidataDataset.getColumnValues("keyid3", ";");
        PropertyList updatedatasetstatusprops = new PropertyList();
        updatedatasetstatusprops.setProperty("sdcid", sdcid);
        updatedatasetstatusprops.setProperty("keyid1", keyid1);
        updatedatasetstatusprops.setProperty("keyid2", keyid2);
        updatedatasetstatusprops.setProperty("keyid3", keyid3);
        actionBlock.setAction("UpdateDataSetStatus", "UpdateDataSetStatus", "1", updatedatasetstatusprops);
        PropertyList syncSDIWIprops = new PropertyList();
        String sourceworkitemid = sdidataDataset.getColumnValues("sourceworkitemid", ";");
        String sourceworkiteminstance = sdidataDataset.getColumnValues("sourceworkiteminstance", ";");
        syncSDIWIprops.setProperty("sdcid", sdcid);
        syncSDIWIprops.setProperty("keyid1", keyid1);
        syncSDIWIprops.setProperty("keyid2", keyid2);
        syncSDIWIprops.setProperty("keyid3", keyid3);
        syncSDIWIprops.setProperty("workitemid", sourceworkitemid);
        syncSDIWIprops.setProperty("workiteminstance", sourceworkiteminstance);
        if (wsComplete) {
            syncSDIWIprops.setProperty("bypassworksheetcompletioncheck", "Y");
        }
        actionBlock.setAction("SyncSDIWIStatus", "SyncSDIWIStatus", "1", syncSDIWIprops);
        if (!"QCBatch".equalsIgnoreCase(sdcid) && isSyncPrimaryStatus) {
            props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", "(null)");
            props.setProperty("keyid3", "(null)");
            props.setProperty("sdistatus", "[sdistatus]");
            if (primarystatusColumn.length() > 0) {
                props.setProperty("statuscolid", primarystatusColumn);
            }
            if (wsComplete) {
                props.setProperty("bypassworksheetcompletioncheck", "Y");
            }
            actionBlock.setAction("SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", props);
        }
        if (isSyncAQCStatus) {
            props = new PropertyList();
            String paramlistid = sdidataDataset.getColumnValues("paramlistid", ";");
            String paramlistversionid = sdidataDataset.getColumnValues("paramlistversionid", ";");
            String variantid = sdidataDataset.getColumnValues("variantid", ";");
            String dataset = sdidataDataset.getColumnValues("dataset", ";");
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("paramlistid", paramlistid);
            props.setProperty("paramlistversionid", paramlistversionid);
            props.setProperty("variantid", variantid);
            props.setProperty("dataset", dataset);
            props.setProperty("saveandrelease", "N");
            actionBlock.setAction("ProcessQCBatch", "ProcessQCBatch", "1", props);
            String qcBatchIds = QCUtil.getLinkedQCBatchIds(sdcid, keyid1, paramlistid, paramlistversionid, variantid, dataset, this.getQueryProcessor());
            if (qcBatchIds.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "QCBatch");
                props.setProperty("keyid1", qcBatchIds);
                props.put("postdataentry", postDataentry ? "Y" : "N");
                actionBlock.setAction("UpdateQCBatchStatus", "UpdateQCBatchStatus", "1", props);
            }
        }
    }

    private SDIData getTemplateDummySDIData() throws SapphireException {
        DataSet sdidataitem;
        SDIData sdiData = new SDIData();
        sdiData.setSdcid("Sample");
        QueryProcessor qp = this.getQueryProcessor();
        DataSet primary = qp.getSqlDataSet(this.getSapphireConnection().isOracle() ? "Select * from s_sample where rownum=1" : "Select top 1 * from s_sample");
        DataSet sdidata = qp.getSqlDataSet(this.getSapphireConnection().isOracle() ? "select * from sdidata where sdcid = 'Sample' and rownum=1" : "select top 1 * from sdidata where sdcid = 'Sample'");
        if (sdidata.getRowCount() == 0) {
            sdidata.addRow();
        }
        if ((sdidataitem = this.getQueryProcessor().getSqlDataSet(this.getSapphireConnection().isOracle() ? "select * from sdidataitem where sdcid = 'Sample' and rownum=1" : "select top 1 * from sdidataitem where sdcid = 'Sample'")).getRowCount() == 0) {
            sdidataitem.addRow();
        }
        String keyid1 = "S-XXXXX-XXXX";
        primary.setValue(0, "s_sampleid", keyid1);
        sdidata.setValue(0, "keyid1", keyid1);
        sdidataitem.setValue(0, "keyid1", keyid1);
        sdidata.setValue(0, "paramlistid", "ParamList ID");
        sdidata.setValue(0, "paramlistversionid", "[ver]");
        sdidata.setValue(0, "variantid", "[var]");
        sdidata.setValue(0, "dataset", "1");
        sdidataitem.setValue(0, "paramlistid", "ParamList ID");
        sdidataitem.setValue(0, "paramlistversionid", "[ver]");
        sdidataitem.setValue(0, "variantid", "[var]");
        sdidataitem.setValue(0, "dataset", "1");
        sdidataitem.setValue(0, "paramid", "Param");
        sdidataitem.setValue(0, "paramtype", "Param Type");
        sdidataitem.setValue(0, "replicate", "1");
        sdidataitem.setValue(0, "displayvalue", "[datavalue]");
        sdiData.setDataset("primary", primary);
        sdiData.setDataset("dataset", sdidata);
        sdiData.setDataset("dataitem", sdidataitem);
        return sdiData;
    }

    private PropertyList getGridElementProperty() throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        String gDEviewpageid = itemConfig.getProperty("dataentryview");
        if (gDEviewpageid.length() == 0) {
            gDEviewpageid = "gDEGridLite";
        } else if (this.getQueryProcessor().getPreparedSqlDataSet("Select webpageid from webpage where webpageid=?", new Object[]{gDEviewpageid}).getRowCount() < 1) {
            gDEviewpageid = "gDEGridLite";
        }
        String connectionid = super.getSapphireConnection().getConnectionId();
        RequestProcessor requestProcessor = new RequestProcessor(connectionid);
        PropertyList pagedata = requestProcessor.getWebPageProperties(gDEviewpageid, new RequestContext(new PropertyList()));
        PropertyList gwtdataentrygridElement = pagedata.getPropertyList("gwtdataentrygrid");
        PropertyListCollection columns = gwtdataentrygridElement.getCollection("columns");
        for (int i = 0; i < columns.size(); ++i) {
            if (!"hasdataitemnotes".equals(columns.getPropertyList(i).getProperty("columnid"))) continue;
            columns.getPropertyList(i).setProperty("mode", "retrievedata");
            break;
        }
        gwtdataentrygridElement.setProperty("initialshowdetail", "N");
        PropertyListCollection toolbars = gwtdataentrygridElement.getCollection("toolbars");
        if (!"Y".equals(itemConfig.getProperty("allowrelease"))) {
            for (int i = 0; i < toolbars.size(); ++i) {
                if (!"Release".equals(toolbars.getPropertyList(i).getProperty("operation"))) continue;
                toolbars.getPropertyList(i).setProperty("show", "N");
            }
        }
        if (!"N".equals(itemConfig.getProperty("allowaddreplicate"))) {
            PropertyList operation = new PropertyList();
            operation.setProperty("operation", "Add Replicate");
            operation.setProperty("img", "WEB-CORE/imageref/flat/16/flat_black_addreplicate.svg");
            operation.setProperty("group", "ReleaseUnrelease");
            if ("E".equals(itemConfig.getProperty("allowaddreplicate"))) {
                operation.setProperty("esig", "Y");
            }
            toolbars.add(operation);
        }
        if (!"N".equals(itemConfig.getProperty("allowremeasure"))) {
            PropertyList operation = new PropertyList();
            operation.setProperty("operation", "Remeasure");
            operation.setProperty("img", "WEB-CORE/images/png/Remeasure.png");
            operation.setProperty("group", "ReleaseUnrelease");
            if ("E".equals(itemConfig.getProperty("allowremeasure"))) {
                operation.setProperty("esig", "Y");
            }
            toolbars.add(operation);
        }
        if (!"N".equals(itemConfig.getProperty("allowchooseconsumable"))) {
            PropertyList operation = new PropertyList();
            operation.setProperty("operation", "Custom");
            operation.setProperty("title", "Choose Consumable");
            operation.setProperty("img", "WEB-CORE/images/png/ChooseReagent.png");
            operation.setProperty("group", "ReleaseUnrelease");
            operation.setProperty("jsfunction", "advanceToolbarReagentDataentry.addReagent();");
            toolbars.add(operation);
        }
        if (!"N".equals(itemConfig.getProperty("allowchooseinstrument"))) {
            PropertyList operation = new PropertyList();
            operation.setProperty("operation", "Custom");
            operation.setProperty("title", "Choose Instrument");
            operation.setProperty("img", "WEB-CORE/images/png/ChooseReagent.png");
            operation.setProperty("group", "ReleaseUnrelease");
            operation.setProperty("jsfunction", "advanceToolbarReagentDataentry.addInstrument();");
            toolbars.add(operation);
        }
        if (itemConfig.getProperty("dataitemdisplayrule").length() > 0) {
            gwtdataentrygridElement.setProperty("dataitemdisplayrule", itemConfig.getProperty("dataitemdisplayrule"));
        }
        gwtdataentrygridElement.setProperty("isautorelease", itemConfig.getProperty("isautorelease"));
        return gwtdataentrygridElement;
    }

    private GWTDataEntry getGWTDataEntry(PropertyList gwtdataentrygridElement) throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        String gDEviewpageid = itemConfig.getProperty("dataentryview");
        String connectionid = super.getSapphireConnection().getConnectionId();
        GWTDataEntry element = new GWTDataEntry();
        element.setWebpageid(gDEviewpageid);
        element.setElementid("gwtdataentrygrid");
        element.setConnectionId(connectionid);
        element.setElementProperties(gwtdataentrygridElement);
        return element;
    }

    private DataSet getItemSDIDataSet() throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        DataSet itemSDIDataSet = null;
        String source = itemConfig.getProperty("source");
        String sourcesdcid = itemConfig.getProperty("sourcesdcid");
        if ("Control".equals(source)) {
            String worksheetItemId = super.getWorksheetItemId();
            String worksheetItemVersionid = super.getWorksheetItemVersionId();
            itemSDIDataSet = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3 FROM worksheetitemsdi WHERE sdcid=? AND worksheetitemid=? AND worksheetitemversionid=?", new Object[]{sourcesdcid, worksheetItemId, worksheetItemVersionid});
        } else {
            String worksheetId = super.getWorksheetId();
            String worksheetVersionid = super.getWorksheetVersionId();
            itemSDIDataSet = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3 FROM worksheetsdi WHERE sdcid=? AND worksheetid=? AND worksheetversionid=? ORDER BY USERSEQUENCE", new Object[]{sourcesdcid, worksheetId, worksheetVersionid});
        }
        return itemSDIDataSet;
    }

    private DataSet getQueryDataSet(String sourcesdcid) throws SapphireException {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sourcesdcid);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryid(this.getDetokenizedConfigProperty("queryid"));
        PropertyListCollection queryparams = this.config.getCollection("queryparams");
        if (queryparams != null) {
            String[] params = new String[queryparams.size()];
            for (int i = 0; i < queryparams.size(); ++i) {
                params[i] = this.getDetokenizedConfigProperty(queryparams.getPropertyList(i), "paramid");
            }
            sdiRequest.setQueryParams(params);
        }
        sdiRequest.setQueryFrom(this.getDetokenizedConfigProperty("queryfrom"));
        sdiRequest.setQueryWhere(this.getDetokenizedConfigProperty("querywhere"));
        SDIData querySDIData = this.getSDIProcessor().getSDIData(sdiRequest);
        querySDIData.getDataset("primary");
        DataSet queryDataSet = querySDIData.getDataset("primary");
        return queryDataSet;
    }

    private SDIData getQueryDataEntrySDIData(String sourcesdcid, DataSet queryDs, PropertyList gridelementProps, boolean lockDataset) throws SapphireException {
        String itemSdcId;
        SDIData sdiData = null;
        DataSet items = new DataSet();
        String string = itemSdcId = "DataSet".equals(sourcesdcid) ? "SDIData" : sourcesdcid;
        if ("DataSet".equals(sourcesdcid) || "SDIWorkItem".equals(sourcesdcid)) {
            for (int d = 0; d < queryDs.getRowCount(); ++d) {
                int r = items.addRow();
                items.setString(r, "sdcid", itemSdcId);
                if ("DataSet".equals(sourcesdcid)) {
                    items.setString(r, "keyid1", queryDs.getValue(d, "sdidataid"));
                    continue;
                }
                items.setString(r, "keyid1", queryDs.getValue(d, "sdiworkitemid"));
            }
            sdiData = this.getBaseDataEntrySDIData(items, gridelementProps, lockDataset, "DataSet".equals(sourcesdcid) ? queryDs : null);
        } else {
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            String keyColId1 = sdcProcessor.getProperty(itemSdcId, "keycolid1");
            String keyColId2 = sdcProcessor.getProperty(itemSdcId, "keycolid2");
            String keyColId3 = sdcProcessor.getProperty(itemSdcId, "keycolid3");
            for (int d = 0; d < queryDs.getRowCount(); ++d) {
                int r = items.addRow();
                items.setString(r, "sdcid", itemSdcId);
                items.setString(r, "keyid1", queryDs.getValue(d, keyColId1));
                items.setString(r, "keyid2", queryDs.getValue(d, keyColId2).length() > 0 ? queryDs.getValue(d, keyColId2) : "(null)");
                items.setString(r, "keyid3", queryDs.getValue(d, keyColId3).length() > 0 ? queryDs.getValue(d, keyColId3) : "(null)");
            }
            sdiData = this.getBaseDataEntrySDIData(items, gridelementProps, lockDataset, null);
        }
        return sdiData;
    }

    private SDIData getDataEntrySDIData(PropertyList element, boolean lockDataset) throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        DataSet itemSDIDataSet = null;
        String source = itemConfig.getProperty("source");
        String sourcesdcid = itemConfig.getProperty("sourcesdcid");
        itemSDIDataSet = "Query".equals(source) ? this.getQueryDataSet(sourcesdcid) : this.getItemSDIDataSet();
        if (itemSDIDataSet != null && itemSDIDataSet.getRowCount() > 0) {
            SDIData sdiData = "Query".equals(source) ? this.getQueryDataEntrySDIData(sourcesdcid, itemSDIDataSet, element, lockDataset) : this.getBaseDataEntrySDIData(itemSDIDataSet, element, lockDataset, null);
            return sdiData;
        }
        return null;
    }

    private SDIData getDataEntrySDIData(DataSet itemSDIDataSet, PropertyList element, boolean lockDataset) throws SapphireException {
        PropertyList itemConfig = this.getConfig();
        String source = itemConfig.getProperty("source");
        String sourcesdcid = itemConfig.getProperty("sourcesdcid");
        SDIData sdiData = "Query".equals(source) ? this.getQueryDataEntrySDIData(sourcesdcid, itemSDIDataSet, element, lockDataset) : this.getBaseDataEntrySDIData(itemSDIDataSet, element, lockDataset, null);
        return sdiData;
    }

    private SDIData getBaseDataEntrySDIData(DataSet itemSDIDataSet, PropertyList gwtdataentrygrid, boolean lockDataset, DataSet dsSdiData) throws SapphireException {
        SDIData sdiData;
        String sdcid = itemSDIDataSet.getValue(0, "sdcid");
        String keyid1 = itemSDIDataSet.getColumnValues("keyid1", ";");
        String keyid2 = itemSDIDataSet.getColumnValues("keyid2", ";");
        String keyid3 = itemSDIDataSet.getColumnValues("keyid3", ";");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setKeyid1List(keyid1);
        sdiRequest.setKeyid2List(keyid2);
        sdiRequest.setKeyid3List(keyid3);
        PropertyList itemConfig = this.getConfig();
        PropertyList datafilter = itemConfig.getPropertyList("datafilter");
        String filterby = datafilter.getProperty("filterby");
        boolean filterByWorkitemid = false;
        if ("SDIWorkItem".equals(sdcid) || "SDIData".equals(sdcid)) {
            String paramlistidfilter;
            String sdidataid;
            String sdiworkitemid = "SDIWorkItem".equals(sdcid) ? keyid1 : "";
            String string = sdidataid = "SDIData".equals(sdcid) ? keyid1 : "";
            if (!"SDIData".equals(sdcid) || dsSdiData == null || dsSdiData.getRowCount() == 0) {
                dsSdiData = this.getSDIDataSet(sdiworkitemid, sdidataid);
            }
            String string2 = paramlistidfilter = !"[paramlistid]".equals(datafilter.getProperty("paramlistid")) && datafilter.getProperty("paramlistid").length() > 0 ? datafilter.getProperty("paramlistid") : "";
            if ("ParamList".equals(filterby) && paramlistidfilter.length() > 0) {
                String[] filterparamlistids = StringUtil.split(paramlistidfilter, ";", true);
                String paramlistversionidfilter = !"[paramlistversionid]".equals(datafilter.getProperty("paramlistversionid")) ? datafilter.getProperty("paramlistversionid") : "";
                String[] filterparamlistversionids = StringUtil.split(paramlistversionidfilter, ";", true);
                String variantfilter = !"[variantid]".equals(datafilter.getProperty("variantid")) ? datafilter.getProperty("variantid") : "";
                String[] filtervariantids = StringUtil.split(variantfilter, ";", true);
                HashSet<String> filterBySet = new HashSet<String>();
                for (int i = 0; i < filterparamlistids.length; ++i) {
                    filterBySet.add(filterparamlistids[i] + ";" + (filterparamlistversionids.length > i ? filterparamlistversionids[i] : "") + ";" + (filtervariantids.length > i ? filtervariantids[i] : ""));
                }
                for (int r = 0; r < dsSdiData.getRowCount(); ++r) {
                    String id = dsSdiData.getValue(r, "paramlistid");
                    String ver = dsSdiData.getValue(r, "paramlistversionid");
                    String var = dsSdiData.getValue(r, "variantid");
                    if (filterBySet.contains(id + ";" + ver + ";" + var) || filterBySet.contains(id + ";;") || filterBySet.contains(id + ";" + ver + ";") || filterBySet.contains(id + ";;" + var)) continue;
                    dsSdiData.setValue(r, "availabilityflag", "NN");
                }
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("availabilityflag", "NN");
                dsSdiData = dsSdiData.getFilteredDataSet(filterMap, true);
            }
            if (dsSdiData.getRowCount() == 0) {
                return null;
            }
            this.populateSDIRequestFromSDIDataSet(sdiRequest, dsSdiData);
        } else if ("ParamList".equals(filterby)) {
            sdiRequest.setParamlistidList(datafilter.getProperty("paramlistid"));
            sdiRequest.setParamlistversionidList(datafilter.getProperty("paramlistversionid"));
            sdiRequest.setVariantidList(datafilter.getProperty("variantid"));
        } else if ("WorkItem".equals(filterby)) {
            String workitemidlist = datafilter.getProperty("workitemid");
            String instancelist = datafilter.getProperty("workiteminstance");
            int keycount = StringUtil.split(keyid1, ";").length;
            if (instancelist.length() == 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet sdidatads = this.getQueryProcessor().getPreparedSqlDataSet("select sourceworkitemid, sourceworkiteminstance from sdidata where sdcid=" + safeSQL.addVar(sdcid) + " and keyid1 in (" + safeSQL.addIn(keyid1) + ") and sourceworkitemid in ( " + safeSQL.addIn(workitemidlist) + " )", safeSQL.getValues());
                workitemidlist = sdidatads.getColumnValues("sourceworkitemid", ";");
                instancelist = sdidatads.getColumnValues("sourceworkiteminstance", ";");
            }
            if (workitemidlist.length() > 0 && instancelist.length() > 0) {
                StringBuilder workitemidSb = new StringBuilder();
                StringBuilder workiteminstanceSb = new StringBuilder();
                for (int i = 0; i < keycount; ++i) {
                    workitemidSb.append(";" + workitemidlist);
                    workiteminstanceSb.append(";" + instancelist);
                }
                sdiRequest.setWorkitemidList(workitemidSb.substring(1));
                sdiRequest.setWorkiteminstanceList(workiteminstanceSb.substring(1));
            }
        } else if ("SDIData ID".equals(filterby) || "SDIWorkItem ID".equals(filterby)) {
            dsSdiData = this.getSDIDataSet(datafilter.getProperty("sdiworkitemid"), datafilter.getProperty("sdidataid"));
            if (dsSdiData == null || dsSdiData.getRowCount() == 0) {
                return null;
            }
            this.populateSDIRequestFromSDIDataSet(sdiRequest, dsSdiData);
        }
        HashMap<String, String> requestMap = GWTDataEntry.buildRequestMap(gwtdataentrygrid, false, false);
        sdiRequest.setRequestItem(requestMap.get("primary"));
        sdiRequest.setRequestItem(requestMap.get("dataset"));
        sdiRequest.setRequestItem(requestMap.get("dataitem"));
        sdiRequest.setRequestItem(requestMap.get("dataspec"));
        sdiRequest.setRequestItem("dataapproval");
        if ("Y".equals(itemConfig.getProperty("useworkitempreferredview"))) {
            sdiRequest.setRequestItem("sdiworkitem[workitemid.preferreddataentryview preferreddataentryview]");
        }
        if (lockDataset) {
            sdiRequest.setPrimaryLockOption("");
            sdiRequest.setDataLockOption("LA");
            sdiRequest.setRetainRsetid(true);
            sdiRequest.setRetrieve(true);
            sdiRequest.setSecurityBypassCode("D".equals(this.getSDCProcessor().getProperty("DataSet", "accesscontrolledflag")) ? 2 : 0);
        }
        if ((sdiData = this.getSDIProcessor().getSDIData(sdiRequest)) == null) {
            throw new SapphireException("Data Entry Control: Error retrieving data!");
        }
        if (lockDataset) {
            String rsetid = sdiData.getRsetid();
            super.setEditRSet(rsetid);
        }
        return sdiData;
    }

    private void populateSDIRequestFromSDIDataSet(SDIRequest sdiRequest, DataSet dsSdiData) {
        String keyid1list = dsSdiData.getColumnValues("keyid1", ";");
        String keyid2list = dsSdiData.getColumnValues("keyid2", ";");
        String keyid3list = dsSdiData.getColumnValues("keyid3", ";");
        String paramlistidlist = dsSdiData.getColumnValues("paramlistid", ";");
        String paramlistversionidlist = dsSdiData.getColumnValues("paramlistversionid", ";");
        String variantidlist = dsSdiData.getColumnValues("variantid", ";");
        String datasetlist = dsSdiData.getColumnValues("dataset", ";");
        String primarysdcid = "Sample";
        try {
            primarysdcid = this.getGridElementProperty().getProperty("sdcid");
        }
        catch (Exception e) {
            this.logWarn("Failed to get sdcid from data entry view definition.");
        }
        sdiRequest.setSDCid(primarysdcid);
        sdiRequest.setKeyid1List(keyid1list);
        sdiRequest.setKeyid2List(keyid2list);
        sdiRequest.setKeyid3List(keyid3list);
        sdiRequest.setParamlistidList(paramlistidlist);
        sdiRequest.setParamlistversionidList(paramlistversionidlist);
        sdiRequest.setVariantidList(variantidlist);
        sdiRequest.setDatasetList(datasetlist);
        sdiRequest.setPropsMatch(true);
    }

    private DataSet getSDIDataSet(String sdiworkitemidlist, String sdidataidlist) throws SapphireException {
        DataSet dsSdiData = null;
        QueryProcessor qp = this.getQueryProcessor();
        boolean isOracle = this.getConnectionProcessor().isOra();
        String idlist = OpalUtil.getSqlWhereClause(sdidataidlist.length() > 0 ? sdidataidlist : sdiworkitemidlist);
        SafeSQL safeSQL = new SafeSQL();
        if (sdidataidlist.length() > 0) {
            dsSdiData = qp.getPreparedSqlDataSet("select keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, availabilityflag from sdidata where sdidataid in (" + safeSQL.addIn(idlist) + ")", safeSQL.getValues());
        } else {
            String fromOrderTab = isOracle ? ", TABLE (LV_OrderTab (" + safeSQL.addVar(sdiworkitemidlist) + ")) t " : ", LV_OrderTab (" + safeSQL.addVar(sdiworkitemidlist) + ",default,default,default) t ";
            dsSdiData = qp.getPreparedSqlDataSet("select sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset, sd.availabilityflag, sd.sourceworkitemid, sd.sourceworkiteminstance \nFROM sdidata sd, sdiworkitem sw, sdiworkitemitem swi" + fromOrderTab + "WHERE sd.sdcid=sw.sdcid\nAND sd.keyid1=sw.keyid1\nAND sd.keyid2=sw.keyid2\nAND sd.keyid3=sw.keyid3\nAND sd.sourceworkitemid=sw.workitemid\nAND sd.sourceworkiteminstance=sw.workiteminstance\nAND sd.sdcid=swi.sdcid\nAND sd.keyid1=swi.keyid1\nAND sd.keyid2=swi.keyid2\nAND sd.keyid3=swi.keyid3\nAND sd.paramlistid=swi.itemkeyid1\nAND sd.paramlistversionid=swi.itemkeyid2\nAND sd.variantid=swi.itemkeyid3\nAND swi.itemsdcid='ParamList'\nAND swi.sdcid = sw.sdcid\nAND swi.keyid1 = sw.keyid1\nAND swi.keyid2 = sw.keyid2\nAND swi.keyid3 = sw.keyid3\nAND swi.workitemid = sw.workitemid\nAND swi.workiteminstance = sw.workiteminstance\n AND sw.sdiworkitemid = t.id_value ORDER BY t.seq_value", safeSQL.getValues());
        }
        return dsSdiData;
    }
}

