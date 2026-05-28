/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.LV_TaskDefRO;
import com.labvantage.sapphire.modules.workflow.StepUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_TaskDefUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderTaskDefInfo(LV_TaskDefRO ro) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Definition");
        info.startTable();
        info.startRow();
        info.addRowItem("Task ID", ro.getKeyid1());
        info.addRowItem("Version", ro.getKeyid2());
        info.endRow();
        info.startRow();
        info.addRowItem("Variant", ro.getKeyid3());
        info.addRowItem("System Task", ro.getSystemTask());
        info.endRow();
        info.startRow();
        info.addRowItem("Description", ro.getDescription());
        info.addRowItem("Execution Title", ro.getPrimaryValue("longtitle"));
        info.endRow();
        info.startRow();
        info.addRowItem("Instructions", ro.getInstructions(), 3);
        info.endRow();
        info.startRow();
        info.addRowItem("Auto Execute", ro.getAutoExec());
        info.addRowItem("Summary Statement", ro.getSummaryStatement());
        info.endRow();
        info.startRow();
        info.addRowItem("Allow Pause", ro.getAllowPause());
        info.addRowItem("Standalone", ro.getStandalone());
        info.endRow();
        info.startRow();
        info.addRowItem("Allow Cancel", ro.getAllowCancel());
        info.addRowItem("Cancel Script", ro.getCancelScript());
        info.endRow();
        info.startRow();
        info.addRowItem("Auto Show Info", ro.getAutoShowInfo(), 3);
        info.endRow();
        info.startRow();
        info.addRowItem("Complete Action", ro.getCompleteAction());
        info.addRowItem("Complete Page", ro.getCompletePage());
        info.endRow();
        info.startRow();
        info.addRowItem("Cancel Action", ro.getCancelAction());
        info.addRowItem("Cancel Page", ro.getCancelPage());
        info.endRow();
        info.startRow();
        info.addRowItem("Confirm Cancel", ro.getConfirmCancel(), 3);
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderTaskAssignment(LV_TaskDefRO ro) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Assignment");
        info.startTable();
        info.startRow();
        info.addRowItem("User", ro.getTaskDefProperty("sysuserid"));
        info.addRowItem("Role", ro.getTaskDefProperty("roleid"));
        info.endRow();
        info.startRow();
        info.addRowItem("Department", ro.getTaskDefProperty("departmentid"));
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderTaskAssignmentDiff(LV_TaskDefRO srcRO, LV_TaskDefRO refRO) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Assignment");
        info.startTable();
        info.startRow();
        info.addDiffRowItem("User", srcRO.getTaskDefProperty("sysuserid"), refRO.getTaskDefProperty("sysuserid"));
        info.addDiffRowItem("Role", srcRO.getTaskDefProperty("roleid"), refRO.getTaskDefProperty("roleid"));
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Department", srcRO.getTaskDefProperty("departmentid"), refRO.getTaskDefProperty("departmentid"));
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderTaskDefDump(LV_TaskDefRO ro) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Definition Dump");
        try {
            PropertyList outProps = ro.getTaskData();
            ConfigReportContent taskdef = new ConfigReportContent(this.config, "Task defintion details");
            taskdef.startSubSection("Rendering Task Def PropertyList", "PropertyList");
            taskdef.renderPropertyList(outProps, true, this.getTranslationProcessor());
            taskdef = taskdef.renderPropertyList(outProps, true, this.getTranslationProcessor());
            info.append(taskdef.toString());
        }
        catch (Exception e) {
            info.append("ERROR: failed to generate taskdef");
        }
        return info;
    }

    public ConfigReportContent renderAppearance(LV_TaskDefRO ro, boolean configreport) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Definition Dump");
        String taskimagefilename = ConfigReportContent.generateSDISectionFileName(ro.currentSDI).replaceAll("html", "png");
        if (ro.dataSource.equals("XMLREPORT")) {
            String includeImg = "";
            ro.getRefTaskDefImage(taskimagefilename);
            includeImg = configreport ? "<img src=\"../images/" + taskimagefilename + "\"  title=\"" + taskimagefilename + "\">" : "<img src=\"" + taskimagefilename + "\"  title=\"" + taskimagefilename + "\">";
            info.append(includeImg);
        } else {
            ro.getTaskDefImage("", this.folder + File.separator + "images" + File.separator + taskimagefilename, this.getConnectionId(), "TASK");
            String includeImg = "";
            includeImg = configreport ? "<img src=\"../images/" + taskimagefilename + "\"  title=\"" + taskimagefilename + "\">" : "<img src=\"" + taskimagefilename + "\"  title=\"" + taskimagefilename + "\">";
            info.append(includeImg);
        }
        info.appendSpecialContent(this.renderAppearanceInfo(ro), this.diffOnly);
        return info;
    }

    public ConfigReportContent renderAppearanceDiff(String applicationurl, LV_TaskDefRO sdcRO, LV_TaskDefRO refSdcRO, boolean configreport) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Definition Dump");
        String url = applicationurl;
        String srctaskimagefilename = ConfigReportContent.generateSDISectionFileName(sdcRO.currentSDI).replaceAll(".html", "_srctaskimage.png");
        String reftaskimagefilename = ConfigReportContent.generateSDISectionFileName(sdcRO.currentSDI).replaceAll(".html", "_reftaskimage.png");
        String path = "";
        path = configreport ? this.folder + File.separator + "images" + File.separator + srctaskimagefilename : "temp" + File.separator + srctaskimagefilename;
        sdcRO.getTaskDefImage(url, path, this.getConnectionId(), "TASK");
        String includeImg = "";
        includeImg = configreport ? "<img src=\"../images/" + srctaskimagefilename + "\"  title=\"" + srctaskimagefilename + "\">" : "<img src=\"" + path + "\"  title=\"" + srctaskimagefilename + "\">";
        info.append(includeImg);
        refSdcRO.getRefTaskDefImage(reftaskimagefilename);
        info.appendSpecialContent(this.renderAppearanceImageDiff(srctaskimagefilename, reftaskimagefilename, sdcRO, refSdcRO), this.diffOnly);
        info.appendSpecialContent(this.renderAppearanceInfoDiff(sdcRO, refSdcRO), this.diffOnly);
        return info;
    }

    public ConfigReportContent renderAppearanceImageDiff(String srcfilename, String reffilename, LV_TaskDefRO srcRO, LV_TaskDefRO refRO) {
        String title = "Appearance";
        ConfigReportContent fileinfo = new ConfigReportContent(this.config, title);
        try {
            if (this.hasAppearanceChanged(srcRO, refRO)) {
                String includeImg = "<img src=\"../images/" + srcfilename + "\"  title=\"" + srcfilename + "\">";
                fileinfo.startSubHeading("New " + title, "");
                fileinfo.append(includeImg);
                fileinfo.startSubHeading("Old " + title, "");
                String refincludeImg = "<img src=\"../images/" + reffilename + "\"  title=\"" + reffilename + "\">";
                fileinfo.append(refincludeImg);
            } else {
                String includeImg = "<img src=\"../images/" + srcfilename + "\"  title=\"" + srcfilename + "\">";
                fileinfo.startSubHeading(title, "");
                fileinfo.append(includeImg);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return fileinfo;
    }

    boolean hasAppearanceChanged(LV_TaskDefRO srcRO, LV_TaskDefRO refRO) {
        PropertyList refAppearance;
        PropertyList srcAppearance = srcRO.getAppearanceInfo();
        if (ConfigReportContent.hasPropertyListChanged(srcAppearance, refAppearance = refRO.getAppearanceInfo())) {
            return true;
        }
        PropertyListCollection srcIO = srcRO.getTaskQueueInfo();
        PropertyListCollection refIO = refRO.getTaskQueueInfo();
        for (int i = 0; i < srcIO.size(); ++i) {
            PropertyList srcPl = srcIO.getPropertyList(i);
            PropertyList refPl = refIO.find("ioid", srcPl.getProperty("ioid"));
            if (refPl == null) {
                return true;
            }
            if (!ConfigReportContent.hasPropertyListChanged(srcPl, refPl)) continue;
            return true;
        }
        return false;
    }

    public ConfigReportContent renderTaskDefInfoDiff(LV_TaskDefRO sdcRO, LV_TaskDefRO refSdcRO) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Task Definition");
        info.startTable();
        info.startRow();
        info.addRowItem("Task ID", sdcRO.getKeyid1());
        info.addRowItem("Version", sdcRO.getKeyid2());
        info.endRow();
        info.startRow();
        info.addRowItem("Variant", sdcRO.getKeyid3());
        info.addDiffRowItem("System Task", sdcRO.getSystemTask(), refSdcRO.getSystemTask());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Description", sdcRO.getDescription(), refSdcRO.getDescription());
        info.addDiffRowItem("Execution Title", sdcRO.getPrimaryValue("longtitle"), refSdcRO.getPrimaryValue("longtitle"));
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Instructions", sdcRO.getInstructions(), refSdcRO.getInstructions(), 3, false, this.getTranslationProcessor(), false);
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Auto Execute", sdcRO.getAutoExec(), refSdcRO.getAutoExec());
        info.addDiffRowItem("Summary Statement", sdcRO.getSummaryStatement(), refSdcRO.getSummaryStatement());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Allow Pause", sdcRO.getAllowPause(), refSdcRO.getAllowPause());
        info.addDiffRowItem("Standalone", sdcRO.getStandalone(), refSdcRO.getStandalone());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Allow Cancel", sdcRO.getAllowCancel(), refSdcRO.getAllowCancel());
        info.addDiffRowItem("Cancel Script", sdcRO.getCancelScript(), refSdcRO.getCancelScript());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Auto Show Info", sdcRO.getAutoShowInfo(), refSdcRO.getAutoShowInfo(), 3, false, this.getTranslationProcessor(), false);
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Complete Action", sdcRO.getCompleteAction(), refSdcRO.getCompleteAction());
        info.addDiffRowItem("Complete Page", sdcRO.getCompletePage(), refSdcRO.getCompletePage());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Cancel Action", sdcRO.getCancelAction(), refSdcRO.getCancelAction());
        info.addDiffRowItem("Cancel Page", sdcRO.getCancelPage(), refSdcRO.getCancelPage());
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Confirm Cancel", sdcRO.getConfirmCancel(), refSdcRO.getConfirmCancel(), 3, false, this.getTranslationProcessor(), false);
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderQueuesInfo(LV_TaskDefRO ro) {
        ConfigReportContent qBuffer = new ConfigReportContent(this.config, "Queues");
        PropertyListCollection taskQueueInfo = ro.getTaskQueueInfo();
        if (taskQueueInfo != null) {
            for (int i = 0; i < taskQueueInfo.size(); ++i) {
                PropertyList currentQ = taskQueueInfo.getPropertyList(i);
                String ioflag = currentQ.getProperty("ioflag");
                if (ioflag.equals("I")) {
                    ConfigReportContent inputQBuffer = new ConfigReportContent(this.config, "QueueInfo");
                    inputQBuffer.startSubHeading("Input Queue (" + currentQ.getProperty("iodesc", currentQ.getProperty("ioid")) + ")", "");
                    inputQBuffer.startTable();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Title", currentQ.getProperty("iodesc"));
                    inputQBuffer.addRowItem("Connector", currentQ.getProperty("connectortypeid"));
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Lookup Page", currentQ.getProperty("lookuppageid"));
                    inputQBuffer.addRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Selection", !currentQ.getProperty("autoselect").equals("Y") ? "Manual Select" : "Auto Select");
                    inputQBuffer.addRowItem("Wait Type", currentQ.getProperty("waittype").equals("event") ? "Based on an Event" : "No Wait");
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Example Keyid1", currentQ.getProperty("exampletaskqueuekeyid1"), 3);
                    inputQBuffer.endRow();
                    inputQBuffer.endTable();
                    qBuffer.append(inputQBuffer.toString());
                    continue;
                }
                ConfigReportContent outputQBuffer = new ConfigReportContent(this.config, "OutputQ");
                outputQBuffer.startSubHeading("Output Queue (" + currentQ.getProperty("iodesc", currentQ.getProperty("ioid")) + ")", "");
                outputQBuffer.startTable();
                outputQBuffer.startRow();
                outputQBuffer.addRowItem("Title", currentQ.getProperty("iodesc"));
                outputQBuffer.addRowItem("Connector", currentQ.getProperty("connectortypeid"));
                outputQBuffer.endRow();
                outputQBuffer.startRow();
                String outputtypeflag = currentQ.getProperty("outputtypeflag");
                if (outputtypeflag.equals("F")) {
                    outputtypeflag = "Full Pass Through";
                } else if (outputtypeflag.equals("I")) {
                    outputtypeflag = "Itemized Pass Through";
                } else if (outputtypeflag.equals("G")) {
                    outputtypeflag = "Generated Items";
                }
                outputQBuffer.addRowItem("Type", outputtypeflag);
                outputQBuffer.addRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                outputQBuffer.endRow();
                outputQBuffer.startRow();
                outputQBuffer.addRowItem("Variable", currentQ.getProperty("variableid"));
                outputQBuffer.endRow();
                outputQBuffer.endTable();
                qBuffer.append(outputQBuffer.toString());
            }
        }
        return qBuffer;
    }

    public ConfigReportContent renderQueuesInfoDiff(LV_TaskDefRO sdcRO, LV_TaskDefRO refSdcRO) {
        String outputtypeflag;
        ConfigReportContent outputQBuffer;
        ConfigReportContent inputQBuffer;
        String ioflag;
        int i;
        ConfigReportContent qBuffer = new ConfigReportContent(this.config, "Queues");
        PropertyListCollection srcTaskQueueInfo = sdcRO.getTaskQueueInfo();
        PropertyListCollection refTaskQueueInfo = refSdcRO.getTaskQueueInfo();
        if (srcTaskQueueInfo != null) {
            for (i = 0; i < srcTaskQueueInfo.size(); ++i) {
                PropertyList match;
                PropertyList currentQ = srcTaskQueueInfo.getPropertyList(i);
                ioflag = currentQ.getProperty("ioflag");
                if (ioflag.equals("I")) {
                    inputQBuffer = new ConfigReportContent(this.config, "QueueInfo");
                    match = refTaskQueueInfo.find("ioid", currentQ.getProperty("ioid"));
                    if (match == null) {
                        inputQBuffer.startSubHeading(ConfigReportContent.getNewString("Input Queue (" + currentQ.getProperty("iodesc", currentQ.getProperty("ioid")) + ")"), "");
                        inputQBuffer.startTable();
                        inputQBuffer.startRow();
                        inputQBuffer.addRowItem("Title", currentQ.getProperty("iodesc"));
                        inputQBuffer.addRowItem("Connector", currentQ.getProperty("connectortypeid"));
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addRowItem("Lookup Page", currentQ.getProperty("lookuppageid"));
                        inputQBuffer.addRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addRowItem("Selection", !currentQ.getProperty("autoselect").equals("Y") ? "Manual Select" : "Auto Select");
                        inputQBuffer.addRowItem("Wait Type", currentQ.getProperty("waittype").equals("event") ? "Based on an Event" : "No Wait");
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addRowItem("Example Keyid1", currentQ.getProperty("exampletaskqueuekeyid1"), 3);
                        inputQBuffer.endRow();
                        inputQBuffer.endTable();
                    } else {
                        inputQBuffer.startSubHeading("Input Queue (" + currentQ.getProperty("iodesc", currentQ.getProperty("ioid")) + ")", "");
                        inputQBuffer.startTable();
                        inputQBuffer.startRow();
                        inputQBuffer.addDiffRowItem("Title", currentQ.getProperty("iodesc"), match.getProperty("iodesc"));
                        inputQBuffer.addDiffRowItem("Connector", currentQ.getProperty("connectortypeid"), match.getProperty("connectortypeid"));
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addDiffRowItem("Lookup Page", currentQ.getProperty("lookuppageid"), match.getProperty("lookuppageid"));
                        inputQBuffer.addDiffRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection", !match.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addDiffRowItem("Selection", !currentQ.getProperty("autoselect").equals("Y") ? "Manual Select" : "Auto Select", !match.getProperty("autoselect").equals("Y") ? "Manual Select" : "Auto Select");
                        inputQBuffer.addDiffRowItem("Wait Type", currentQ.getProperty("waittype").equals("event") ? "Based on an Event" : "No Wait", match.getProperty("waittype").equals("event") ? "Based on an Event" : "No Wait");
                        inputQBuffer.endRow();
                        inputQBuffer.startRow();
                        inputQBuffer.addDiffRowItem("Example Keyid1", currentQ.getProperty("exampletaskqueuekeyid1"), match.getProperty("exampletaskqueuekeyid1"), 3, false, this.getTranslationProcessor(), false);
                        inputQBuffer.endRow();
                        inputQBuffer.endTable();
                    }
                    qBuffer.append(inputQBuffer.toString());
                    continue;
                }
                outputQBuffer = new ConfigReportContent(this.config, "OutputQ");
                match = refTaskQueueInfo.find("ioid", currentQ.getProperty("ioid"));
                if (match == null) {
                    outputQBuffer.startSubHeading(ConfigReportContent.getNewString("Output Queue (" + currentQ.getProperty("ioid") + ")"), "");
                    outputQBuffer.startTable();
                    outputQBuffer.startRow();
                    outputQBuffer.addRowItem("Title", currentQ.getProperty("iodesc"));
                    outputQBuffer.addRowItem("Connector", currentQ.getProperty("connectortypeid"));
                    outputQBuffer.endRow();
                    outputQBuffer.startRow();
                    outputtypeflag = currentQ.getProperty("outputtypeflag");
                    if (outputtypeflag.equals("F")) {
                        outputtypeflag = "Full Pass Through";
                    } else if (outputtypeflag.equals("I")) {
                        outputtypeflag = "Itemized Pass Through";
                    } else if (outputtypeflag.equals("G")) {
                        outputtypeflag = "Generated Items";
                    }
                    outputQBuffer.addRowItem("Type", outputtypeflag);
                    outputQBuffer.addRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                    outputQBuffer.endRow();
                    outputQBuffer.startRow();
                    outputQBuffer.addRowItem("Variable", currentQ.getProperty("variableid"));
                    outputQBuffer.endRow();
                    outputQBuffer.endTable();
                } else {
                    outputQBuffer.startSubHeading("Output Queue (" + currentQ.getProperty("ioid") + ")", "");
                    outputQBuffer.startTable();
                    outputQBuffer.startRow();
                    outputQBuffer.addDiffRowItem("Title", currentQ.getProperty("iodesc"), match.getProperty("iodesc"));
                    outputQBuffer.addDiffRowItem("Connector", currentQ.getProperty("connectortypeid"), match.getProperty("connectortypeid"));
                    outputQBuffer.endRow();
                    outputQBuffer.startRow();
                    String srcoutputtypeflag = currentQ.getProperty("outputtypeflag");
                    if (srcoutputtypeflag.equals("F")) {
                        srcoutputtypeflag = "Full Pass Through";
                    } else if (srcoutputtypeflag.equals("I")) {
                        srcoutputtypeflag = "Itemized Pass Through";
                    } else if (srcoutputtypeflag.equals("G")) {
                        srcoutputtypeflag = "Generated Items";
                    }
                    String refcoutputtypeflag = match.getProperty("outputtypeflag");
                    if (refcoutputtypeflag.equals("F")) {
                        refcoutputtypeflag = "Full Pass Through";
                    } else if (refcoutputtypeflag.equals("I")) {
                        refcoutputtypeflag = "Itemized Pass Through";
                    } else if (refcoutputtypeflag.equals("G")) {
                        refcoutputtypeflag = "Generated Items";
                    }
                    outputQBuffer.addDiffRowItem("Type", srcoutputtypeflag, refcoutputtypeflag);
                    outputQBuffer.addDiffRowItem("Connections", !currentQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection", !match.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                    outputQBuffer.endRow();
                    outputQBuffer.startRow();
                    outputQBuffer.addDiffRowItem("Variable", currentQ.getProperty("variableid"), match.getProperty("variableid"));
                    outputQBuffer.endRow();
                    outputQBuffer.endTable();
                }
                qBuffer.append(outputQBuffer.toString());
            }
        }
        if (refTaskQueueInfo != null) {
            for (i = 0; i < refTaskQueueInfo.size(); ++i) {
                PropertyList refQ = refTaskQueueInfo.getPropertyList(i);
                ioflag = refQ.getProperty("ioflag");
                if (ioflag.equals("I")) {
                    inputQBuffer = new ConfigReportContent(this.config, "QueueInfo");
                    PropertyList srcMatch = srcTaskQueueInfo.find("ioid", refQ.getProperty("ioid"));
                    if (srcMatch != null) continue;
                    inputQBuffer.startSubHeading(ConfigReportContent.getDeletedString("Input Queue (" + refQ.getProperty("ioid") + ")"), "");
                    inputQBuffer.startTable();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Title", refQ.getProperty("iodesc"));
                    inputQBuffer.addRowItem("Connector", refQ.getProperty("connectortypeid"));
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Lookup Page", refQ.getProperty("lookuppageid"));
                    inputQBuffer.addRowItem("Connections", !refQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Selection", !refQ.getProperty("autoselect").equals("Y") ? "Manual Select" : "Auto Select");
                    inputQBuffer.addRowItem("Wait Type", refQ.getProperty("waittype").equals("event") ? "Based on an Event" : "No Wait");
                    inputQBuffer.endRow();
                    inputQBuffer.startRow();
                    inputQBuffer.addRowItem("Example Keyid1", refQ.getProperty("exampletaskqueuekeyid1"), 3);
                    inputQBuffer.endRow();
                    inputQBuffer.endTable();
                    qBuffer.append(inputQBuffer.toString());
                    continue;
                }
                outputQBuffer = new ConfigReportContent(this.config, "OutputQ");
                PropertyList srcQ = refTaskQueueInfo.find("ioid", refQ.getProperty("ioid"));
                if (srcQ != null) continue;
                outputQBuffer.startSubHeading(ConfigReportContent.getDeletedString("Output Queue (" + refQ.getProperty("ioid") + ")"), "");
                outputQBuffer.startTable();
                outputQBuffer.startRow();
                outputQBuffer.addRowItem("Title", refQ.getProperty("iodesc"));
                outputQBuffer.addRowItem("Connector", refQ.getProperty("connectortypeid"));
                outputQBuffer.endRow();
                outputQBuffer.startRow();
                outputtypeflag = refQ.getProperty("outputtypeflag");
                if (outputtypeflag.equals("F")) {
                    outputtypeflag = "Full Pass Through";
                } else if (outputtypeflag.equals("I")) {
                    outputtypeflag = "Itemized Pass Through";
                } else if (outputtypeflag.equals("G")) {
                    outputtypeflag = "Generated Items";
                }
                outputQBuffer.addRowItem("Type", outputtypeflag);
                outputQBuffer.addRowItem("Connections", !refQ.getProperty("singleconnect").equals("Y") ? "Multiple Connections" : "Single Connection");
                outputQBuffer.endRow();
                outputQBuffer.startRow();
                outputQBuffer.addRowItem("Variable", refQ.getProperty("variableid"));
                outputQBuffer.endRow();
                outputQBuffer.endTable();
                qBuffer.append(outputQBuffer.toString());
            }
        }
        return qBuffer;
    }

    public ConfigReportContent renderAppearanceInfo(LV_TaskDefRO ro) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Variables:" + ro.getKeyid1());
        PropertyList info = ro.getAppearanceInfo();
        buffer.startTable();
        buffer.startRow();
        buffer.addRowItem("Icon Text", info.getProperty("shorttitle"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Icon", info.getProperty("icon"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Appearance", info.getProperty("appearance"));
        buffer.addRowItem("Task Color", info.getProperty("taskcolor"));
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderAppearanceInfoDiff(LV_TaskDefRO sdcRO, LV_TaskDefRO refSdcRO) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Variables:" + sdcRO.getKeyid1());
        PropertyList infoSrc = sdcRO.getAppearanceInfo();
        PropertyList infoRef = refSdcRO.getAppearanceInfo();
        buffer.startTable();
        buffer.startRow();
        buffer.addDiffRowItem("Icon Text", infoSrc.getProperty("shorttitle"), infoRef.getProperty("shorttitle"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Icon", infoSrc.getProperty("icon"), infoRef.getProperty("icon"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Appearance", infoSrc.getProperty("appearance"), infoRef.getProperty("appearance"));
        buffer.addDiffRowItem("Task Color", infoSrc.getProperty("taskcolor"), infoRef.getProperty("taskcolor"));
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderVariables(LV_TaskDefRO ro) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Variables:" + ro.getKeyid1());
        DataSet ds = ro.getTaskVariables();
        buffer.renderListTable(ds, this.getTranslationProcessor());
        return buffer;
    }

    public ConfigReportContent renderVariablesDiff(LV_TaskDefRO srcRO, LV_TaskDefRO refSdcRO) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Variables:" + this.sdcRO.getKeyid1());
        DataSet srcds = srcRO.getTaskVariables();
        DataSet refds = refSdcRO.getTaskVariables();
        buffer.renderDiffListTable(srcds, refds, new String[]{"Variable"});
        return buffer;
    }

    public ConfigReportContent renderStages(LV_TaskDefRO ro) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Stages:" + ro.getKeyid1());
        DataSet ds = ro.getStages();
        buffer.renderListTable(ds, this.getTranslationProcessor());
        return buffer;
    }

    public ConfigReportContent renderStagesDiff(LV_TaskDefRO srcRO, LV_TaskDefRO refRO) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task Stages:" + srcRO.getKeyid1());
        DataSet srcds = srcRO.getStages();
        DataSet refds = refRO.getStages();
        buffer.renderDiffListTable(srcds, refds, new String[]{"Stage"});
        return buffer;
    }

    public ConfigReportContent renderSteps(LV_TaskDefRO ro, boolean configreport) {
        return this.renderStepsDiff("", ro, ro, configreport, true);
    }

    public ConfigReportContent renderStepsDiff(String applicationurl, LV_TaskDefRO sdcRO, LV_TaskDefRO refSdcRO, boolean configreport, boolean hideEmptyColumns) {
        String refImageBase64;
        ConfigReportContent info = new ConfigReportContent(this.config, "Steps");
        ConfigReportContent stepDetails = this.renderStepDetailsDiff(sdcRO, refSdcRO, hideEmptyColumns);
        String srcImageBase64 = LV_TaskDefUtil.getPrimaryValue(sdcRO.currentSDIData, "thumbnailimagesteps");
        if (srcImageBase64.equals(refImageBase64 = LV_TaskDefUtil.getPrimaryValue(refSdcRO.currentSDIData, "thumbnailimagesteps"))) {
            info.append("<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ");
        } else {
            info.startNewSubSection("New Task Steps:", "");
            String sourcethumbnailhtml = srcImageBase64.length() == 0 ? "<P>Task steps image is empty." : "<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ";
            String refthumbnailhtml = refImageBase64.length() == 0 ? "<P>Task steps image is empty." : "<img src=\"data:image/gif;base64," + refImageBase64 + "\" />  ";
            info.append("<table style=\"border:3px; border-style:solid; border-color:green; padding: 1em;\"><tr><td>" + sourcethumbnailhtml + "</td></tr></table>");
            if (refImageBase64.length() > 0) {
                info.startDeletedSubSection("Old Task Steps:", "");
                info.append("<table style=\"border:3px; border-style:solid; border-color:red; padding: 1em;\"><tr><td>" + refthumbnailhtml + "</td></tr></table>");
            }
        }
        info.appendSpecialContent(stepDetails, this.diffOnly);
        return info;
    }

    public ConfigReportContent renderStepDetails(LV_TaskDefRO ro) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Step Details");
        DataSet ds = ro.getSteps();
        if (ds != null && ds.getRowCount() > 0) {
            String firststep = ro.getFirstStepId();
            ds.addColumn("processed", 0);
            ds.setValue(0, "processed", "N");
            ds.padColumn("processed");
            info.appendSpecialContent(this.renderIndividualStep(ro, firststep, ds), this.diffOnly);
        }
        return info;
    }

    public ConfigReportContent renderStepDetailsDiff(LV_TaskDefRO srcRO, LV_TaskDefRO refRO, boolean hideEmptyColumns) {
        ConfigReportContent info = new ConfigReportContent(this.config, "Step Details");
        DataSet srcds = srcRO.getSteps();
        DataSet refds = refRO.getSteps();
        if (srcds != null && srcds.getRowCount() > 0) {
            String firststep = srcRO.getFirstStepId();
            srcds.addColumn("processed", 0);
            srcds.setValue(0, "processed", "N");
            srcds.padColumn("processed");
            info.appendSpecialContent(this.renderIndividualStepDiff(srcRO, refRO, firststep, srcds, refds, hideEmptyColumns), this.diffOnly);
        }
        return info;
    }

    public ConfigReportContent renderIndividualStep(LV_TaskDefRO ro, String stepid, DataSet stepStatus) {
        ConfigReportContent bufferAll = new ConfigReportContent(this.config, "Step details");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("Step Id", stepid);
        filter.put("processed", "N");
        int match = stepStatus.findRow(filter);
        if (match != -1) {
            stepStatus.setValue(match, "processed", "Y");
            PropertyList queueSelector = ro.getStepQueueSelectorDetails(stepid);
            String steptitle = "Step: " + queueSelector.getProperty("Execution Title") + "(" + stepid + ")";
            ConfigReportContent currentStep = new ConfigReportContent(this.config, steptitle);
            currentStep.startSubSection(steptitle, "");
            currentStep.startTable();
            currentStep.startRow();
            currentStep.addRowItem("Step Id", queueSelector.getProperty("Step Id"));
            currentStep.addRowItem("Execution Title", queueSelector.getProperty("Execution Title"));
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addRowItem("Icon Text", queueSelector.getProperty("Icon Text"));
            currentStep.addRowItem("Stage Id", queueSelector.getProperty("Stage Id"));
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addRowItem("Instructions", queueSelector.getProperty("Instructions"), 3);
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addRowItem("Summary Statement", queueSelector.getProperty("Summary Statement"));
            currentStep.addRowItem("Loaded Script", queueSelector.getProperty("Loaded Script"));
            currentStep.endRow();
            currentStep.endTable();
            currentStep.startSubHeading("Step Configuration", "");
            currentStep.append(this.renderStepConfiguration(ro, stepid).toString());
            DataSet toolbar = ro.getToolbar(stepid);
            String srcImageDir = this.applicationRoot;
            for (int j = 0; j < toolbar.getRowCount(); ++j) {
                String buttonImageName = toolbar.getString(j, "Image");
                try {
                    ConfigReportContent.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                toolbar.setString(j, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
            }
            currentStep.startSubHeading("Toolbar", "");
            ConfigReportContent toolbarBuffer = new ConfigReportContent(this.config, "toolbar");
            toolbarBuffer.renderListTable(toolbar, this.getTranslationProcessor());
            currentStep.append(toolbarBuffer.toString());
            DataSet transitions = ro.getTransitions(stepid);
            currentStep.startSubHeading("Transitions", "");
            ConfigReportContent transitionsBuffer = new ConfigReportContent(this.config, "transitions");
            transitionsBuffer.renderListTable(transitions, this.getTranslationProcessor());
            currentStep.append(transitionsBuffer.toString());
            bufferAll.appendSubSection(currentStep, steptitle, this.diffOnly);
            DataSet nextsteps = ro.getNextSteps(stepid);
            for (int i = 0; i < nextsteps.getRowCount(); ++i) {
                HashMap<String, String> filternext = new HashMap<String, String>();
                filternext.put("processed", "N");
                filternext.put("Step Id", nextsteps.getString(i, "stepid"));
                if (stepStatus.findRow(filternext) == -1) continue;
                ConfigReportContent nextStep = new ConfigReportContent(this.config, "NextStep");
                nextStep.appendSpecialContent(this.renderIndividualStep(ro, nextsteps.getString(i, "stepid"), stepStatus), this.diffOnly);
                bufferAll.appendSpecialContent(nextStep, this.diffOnly);
            }
        }
        return bufferAll;
    }

    public ConfigReportContent renderStepConfiguration(LV_TaskDefRO ro, String stepid) {
        PropertyList props = ro.getStepConfiguration(stepid);
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Step Configuration");
        if (props != null) {
            PropertyList stepDetails = props.getPropertyListNotNull("steptype");
            if (stepDetails != null) {
                String propertytreeid = ro.getStepPropertyTreeId(stepid);
                String objectname = this.getPropertyTreeObjectName(propertytreeid);
                if (objectname.length() > 0) {
                    String defStr = StepUtil.getStepTypeDefinition(objectname);
                    PropertyTree tree = new PropertyTree();
                    try {
                        tree.setDefinitionXML(defStr);
                        buffer = buffer.renderPropertyList(stepDetails, tree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor());
                    }
                    catch (SapphireException sapphireException) {}
                } else {
                    buffer = buffer.renderPropertyList(stepDetails, true, this.getTranslationProcessor());
                    buffer.append(buffer.toString());
                }
            }
            return buffer;
        }
        buffer.append("none");
        return buffer;
    }

    public ConfigReportContent renderStepConfigurationDiff(LV_TaskDefRO srcro, LV_TaskDefRO refro, String stepid, boolean hideEmptyColumns) {
        PropertyList srcprops = srcro.getStepConfiguration(stepid);
        PropertyList refprops = refro.getStepConfiguration(stepid);
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Step Configuration");
        if (srcprops != null) {
            PropertyList srcStepDetails = srcprops.getPropertyListNotNull("steptype");
            if (refprops == null) {
                refprops = new PropertyList();
            }
            PropertyList refStepDetails = refprops.getPropertyListNotNull("steptype");
            if (srcStepDetails != null) {
                String srcpropertytreeid = srcro.getStepPropertyTreeId(stepid);
                String srcobjectname = this.getPropertyTreeObjectName(srcpropertytreeid);
                if (srcobjectname.length() > 0) {
                    String defStr = StepUtil.getStepTypeDefinition(srcobjectname);
                    PropertyTree tree = new PropertyTree();
                    try {
                        tree.setDefinitionXML(defStr);
                        buffer = buffer.renderPropertyListDiff(srcStepDetails, refStepDetails, tree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor(), hideEmptyColumns);
                    }
                    catch (SapphireException sapphireException) {}
                } else {
                    buffer = buffer.renderPropertyListDiff(srcStepDetails, refStepDetails, true, this.getTranslationProcessor());
                    buffer.append(buffer.toString());
                }
            }
            return buffer;
        }
        buffer.append("none");
        return buffer;
    }

    public ConfigReportContent renderIndividualStepDiff(LV_TaskDefRO srcro, LV_TaskDefRO refro, String stepid, DataSet srcSteps, DataSet refSteps, boolean hideEmptyColumns) {
        ConfigReportContent bufferAll = new ConfigReportContent(this.config, "Step details");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("Step Id", stepid);
        filter.put("processed", "N");
        int match = srcSteps.findRow(filter);
        if (match != -1) {
            String buttonImageName;
            int j;
            srcSteps.setValue(match, "processed", "Y");
            PropertyList srcQueueSelector = srcro.getStepQueueSelectorDetails(stepid);
            PropertyList refQueueSelector = refro.getStepQueueSelectorDetails(stepid);
            String steptitle = "Step: " + srcQueueSelector.getProperty("Execution Title") + "(" + stepid + ")";
            ConfigReportContent currentStep = new ConfigReportContent(this.config, steptitle);
            currentStep.startSubSection(steptitle, "");
            currentStep.startTable();
            currentStep.startRow();
            currentStep.addDiffRowItem("Step Id", srcQueueSelector.getProperty("Step Id"), refQueueSelector.getProperty("Step Id"));
            currentStep.addDiffRowItem("Execution Title", srcQueueSelector.getProperty("Execution Title"), refQueueSelector.getProperty("Execution Title"));
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addDiffRowItem("Icon Text", srcQueueSelector.getProperty("Icon Text"), refQueueSelector.getProperty("Icon Text"));
            currentStep.addDiffRowItem("Stage Id", srcQueueSelector.getProperty("Stage Id"), refQueueSelector.getProperty("Stage Id"));
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addDiffRowItem("Instructions", srcQueueSelector.getProperty("Instructions"), refQueueSelector.getProperty("Instructions"), 3, false, this.getTranslationProcessor(), false);
            currentStep.endRow();
            currentStep.startRow();
            currentStep.addDiffRowItem("Summary Statement", srcQueueSelector.getProperty("Summary Statement"), refQueueSelector.getProperty("Summary Statement"));
            currentStep.addDiffRowItem("Loaded Script", srcQueueSelector.getProperty("Loaded Script"), refQueueSelector.getProperty("Loaded Script"));
            currentStep.endRow();
            currentStep.endTable();
            currentStep.startSubHeading("Step Configuration", "");
            currentStep.append(this.renderStepConfigurationDiff(srcro, refro, stepid, hideEmptyColumns).toString());
            DataSet srctoolbar = srcro.getToolbar(stepid);
            DataSet reftoolbar = refro.getToolbar(stepid);
            String srcImageDir = this.applicationRoot;
            for (j = 0; j < srctoolbar.getRowCount(); ++j) {
                buttonImageName = srctoolbar.getString(j, "Image");
                try {
                    ConfigReportContent.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                srctoolbar.setString(j, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
            }
            for (j = 0; j < reftoolbar.getRowCount(); ++j) {
                buttonImageName = reftoolbar.getString(j, "Image");
                try {
                    ConfigReportContent.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                reftoolbar.setString(j, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
            }
            currentStep.startSubHeading("Toolbar", "");
            ConfigReportContent toolbarBuffer = new ConfigReportContent(this.config, "toolbar");
            toolbarBuffer.renderDiffListTable(srctoolbar, reftoolbar, new String[]{"Id"});
            currentStep.append(toolbarBuffer.toString());
            DataSet srctransitions = srcro.getTransitions(stepid);
            DataSet reftransitions = refro.getTransitions(stepid);
            currentStep.startSubHeading("Transitions", "");
            ConfigReportContent transitionsBuffer = new ConfigReportContent(this.config, "transitions");
            transitionsBuffer.renderDiffListTable(srctransitions, reftransitions, new String[]{"Target Step"});
            currentStep.append(transitionsBuffer.toString());
            bufferAll.appendSubSection(currentStep, steptitle, this.diffOnly);
            DataSet nextsteps = srcro.getNextSteps(stepid);
            for (int i = 0; i < nextsteps.getRowCount(); ++i) {
                HashMap<String, String> filternext = new HashMap<String, String>();
                filternext.put("processed", "N");
                filternext.put("Step Id", nextsteps.getString(i, "stepid"));
                if (srcSteps.findRow(filternext) == -1) continue;
                ConfigReportContent nextStep = new ConfigReportContent(this.config, "NextStep");
                nextStep.appendSpecialContent(this.renderIndividualStepDiff(srcro, refro, nextsteps.getString(i, "stepid"), srcSteps, refSteps, hideEmptyColumns), this.diffOnly);
                bufferAll.appendSpecialContent(nextStep, this.diffOnly);
            }
        }
        return bufferAll;
    }

    public String getPropertyTreeObjectName(String propertytreeid) {
        String sql = "SELECT propertytreetype, objectname, definitiontree FROM propertytree WHERE propertytreeid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{propertytreeid});
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getString(0, "objectname");
        }
        return "";
    }
}

