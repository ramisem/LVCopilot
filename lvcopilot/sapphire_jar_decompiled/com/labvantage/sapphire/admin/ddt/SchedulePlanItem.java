/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.search.SearchUtil;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SchedulePlanItem
extends BaseSDCRules {
    public static final String PROPERTY_COPY_PLANITEMS = "copyplanitems";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet categories;
        DataSet schduleRandomizations;
        DataSet primary = sdiData.getDataset("primary");
        DataSet scheduleEvents = sdiData.getDataset("scheduleevent");
        this.checkValidRules(primary, false);
        DataSet scheduleplanitemexec = this.getSchedulePlanItemExecDs(sdiData);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            scheduleplanitemexec.addRow();
            scheduleplanitemexec.setValue(i, "scheduleplanid", primary.getString(i, "scheduleplanid"));
            scheduleplanitemexec.setValue(i, "scheduleplanitemid", primary.getString(i, "scheduleplanitemid"));
        }
        boolean isFromTemplate = !actionProps.getProperty("templatekeyid1", "").isEmpty();
        boolean deepCopyPlanItems = actionProps.getProperty(PROPERTY_COPY_PLANITEMS, "N").startsWith("Y");
        boolean copyTemplateExclusively = actionProps.getProperty("copyplanitems_template_exclusive_copy", "N").startsWith("Y");
        if (scheduleEvents != null && scheduleEvents.getRowCount() > 0) {
            sdiData.setDataset("scheduleevent", new DataSet());
        }
        if ((schduleRandomizations = sdiData.getDataset("schedulerandomization")) != null && schduleRandomizations.getRowCount() > 0) {
            for (int i = 0; i < schduleRandomizations.getRowCount(); ++i) {
                schduleRandomizations.setValue(i, "executedflag", "N");
            }
        }
        this.correctSoftLinkedKeyidFields(schduleRandomizations, "randomizationsdcid", "randomizationkeyid1", "randomizationkeyid2", "randomizationkeyid3", actionProps);
        if (isFromTemplate && (categories = sdiData.getDataset("category")) != null && categories.getRowCount() > 0) {
            sdiData.setDataset("category", new DataSet());
        }
        boolean convertTimeZones = actionProps.getProperty("convert_usertimezones", "N").startsWith("Y");
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        TimeZone userTimeZone = m18n.getTimezone();
        for (int i = 0; i < primary.size(); ++i) {
            if (convertTimeZones) {
                int timeZoneCorrection;
                String schedulePlanId = primary.getString(i, "scheduleplanid");
                String sql = "select timezone from scheduleplan where scheduleplanid = ?";
                Object[] params = new String[]{schedulePlanId};
                DataSet tzDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                String timeZoneStr = tzDs.getString(0, "timezone");
                TimeZone planTimeZone = timeZoneStr != null && !timeZoneStr.isEmpty() ? TimeZone.getTimeZone(timeZoneStr) : TimeZone.getDefault();
                Calendar startdt = primary.getCalendar(i, "startdt");
                Calendar stopdt = primary.getCalendar(i, "stopdt");
                if (startdt != null) {
                    timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, startdt);
                    startdt.add(14, -timeZoneCorrection);
                    startdt.set(13, 0);
                }
                if (stopdt != null) {
                    timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, stopdt);
                    stopdt.add(14, -timeZoneCorrection);
                    stopdt.set(13, 0);
                }
            }
            if ("A".equals(primary.getString(i, "planitemstatus"))) {
                scheduleplanitemexec.setValue(i, "nextscheduledt", primary.getValue(i, "startdt"));
            }
            if (primary.getString(i, "linkkeyid2") == null || primary.getString(i, "linkkeyid2").length() == 0) {
                primary.setValue(i, "linkkeyid2", "(null)");
            }
            if (primary.getString(i, "linkkeyid3") != null && primary.getString(i, "linkkeyid3").length() != 0) continue;
            primary.setValue(i, "linkkeyid3", "(null)");
        }
        if (!this.isCMTImport()) {
            SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
            for (int i = 0; i < primary.size(); ++i) {
                String currentSourceKeyid3;
                String currentSourceKeyid2;
                String currentSourceKeyid1;
                String currentSourceSdcId;
                String schedulePlanId = primary.getString(i, "scheduleplanid");
                String schedulePlanType = schedulerUtil.getSchedulePlanTypeFlag(schedulePlanId);
                if (!schedulePlanType.equals("M")) continue;
                String policyNode = schedulerUtil.getPolicyNode(schedulePlanId);
                PropertyList schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNode);
                String nodeId = schedulerUtil.generateNodes(schedulerPolicy, schedulePlanId, currentSourceSdcId = primary.getString(i, "linksdcid", ""), currentSourceKeyid1 = primary.getString(i, "linkkeyid1", ""), currentSourceKeyid2 = primary.getString(i, "linkkeyid2", ""), currentSourceKeyid3 = primary.getString(i, "linkkeyid3", ""));
                if (!nodeId.equals("")) {
                    primary.setString(i, "scheduleplannodeid", nodeId);
                }
                String templateSdcId = primary.getString(i, "scheduletemplatesdcid", "");
                String templateKeyId1 = primary.getString(i, "scheduletemplatekeyid1", "");
                String templateKeyId2 = primary.getString(i, "scheduletemplatekeyid2", "");
                String templateKeyId3 = primary.getString(i, "scheduletemplatekeyid3", "");
                String linkField1 = "";
                String linkField2 = "";
                if (!templateSdcId.equals("") && !templateKeyId1.equals("")) {
                    PropertyList newSource = schedulerUtil.getTemplateSource(templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3, currentSourceSdcId, schedulerPolicy);
                    String sourceSdcId = newSource.getProperty("sdcid", "");
                    String newSourceKeyid1 = newSource.getProperty("keyid1", "");
                    String newSourceKeyid2 = newSource.getProperty("keyid2", "");
                    String newSourceKeyid3 = newSource.getProperty("keyid3", "");
                    linkField1 = newSource.getProperty("linkfield1", "");
                    linkField2 = newSource.getProperty("linkfield2", "");
                    boolean copied = false;
                    if (!(newSourceKeyid1.equals("") || newSourceKeyid1.equals(currentSourceKeyid1) && newSourceKeyid2.equals(currentSourceKeyid2) && newSourceKeyid3.equals(currentSourceKeyid3))) {
                        if (!currentSourceSdcId.isEmpty() && !currentSourceKeyid1.isEmpty() && primary.getString(i, "linksdcid", "").equals(sourceSdcId) && !primary.getString(i, "linkkeyid1", "").equals(newSourceKeyid1)) {
                            if (deepCopyPlanItems) {
                                this.copyScheduleTemplate(true, primary, schedulerUtil, i, templateSdcId, linkField1, linkField2, currentSourceKeyid1, currentSourceKeyid2);
                                sourceSdcId = currentSourceSdcId;
                                newSourceKeyid1 = currentSourceKeyid1;
                                newSourceKeyid2 = currentSourceKeyid2;
                                newSourceKeyid3 = currentSourceKeyid3;
                                copied = true;
                            } else {
                                throw new SapphireException(this.getTranslationProcessor().translate("Template source must be the same as the Schedule Plan Item source."));
                            }
                        }
                        primary.setString(i, "linksdcid", sourceSdcId);
                        primary.setString(i, "linkkeyid1", newSourceKeyid1);
                        primary.setString(i, "linkkeyid2", newSourceKeyid2);
                        primary.setString(i, "linkkeyid3", newSourceKeyid3);
                    }
                    if (deepCopyPlanItems && copyTemplateExclusively && !copied) {
                        this.copyScheduleTemplate(false, primary, schedulerUtil, i, templateSdcId, linkField1, linkField2, newSourceKeyid1, newSourceKeyid2);
                    }
                    if (!(nodeId = schedulerUtil.generateNodes(schedulerPolicy, schedulePlanId, sourceSdcId, newSourceKeyid1, newSourceKeyid2, newSourceKeyid3)).equals("")) {
                        primary.setString(i, "scheduleplannodeid", nodeId);
                    }
                }
                String sourceSdcId = primary.getString(i, "linksdcid", "");
                String sourceKeyid1 = primary.getString(i, "linkkeyid1", "");
                if (!sourceSdcId.equals("") && !sourceKeyid1.equals("")) continue;
                String errorMessage = this.getTranslationProcessor().translate("You must define a source for the Schedule Plan Item. ");
                if (!linkField1.isEmpty()) {
                    errorMessage = errorMessage + this.getTranslationProcessor().translate("Source: ");
                    errorMessage = errorMessage + linkField1;
                    if (!linkField2.isEmpty()) {
                        errorMessage = errorMessage + " (" + linkField2 + ")";
                    }
                }
                throw new SapphireException(errorMessage);
            }
        } else {
            HashMap oldnewkeyMap = (HashMap)actionProps.get("oldnewkeymap");
            SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
            for (int i = 0; i < primary.size(); ++i) {
                String schedulePlanId = primary.getString(i, "scheduleplanid");
                String schedulePlanType = schedulerUtil.getSchedulePlanTypeFlag(schedulePlanId);
                if (!schedulePlanType.equals("M")) continue;
                DataSet plannodeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT scheduleplanid, scheduleplannodeid, refsdcid, refkeyid1, refkeyid2, refkeyid3 from scheduleplannode where scheduleplanid=?", new Object[]{schedulePlanId});
                for (int j = 0; j < plannodeDS.getRowCount(); ++j) {
                    String refsdcid = plannodeDS.getValue(j, "refsdcid");
                    if (oldnewkeyMap.get(refsdcid) == null) continue;
                    HashMap oldnewmapforsdc = (HashMap)oldnewkeyMap.get(refsdcid);
                    String refkeyid1 = plannodeDS.getValue(j, "refkeyid1");
                    String refkeyid2 = plannodeDS.getValue(j, "refkeyid2");
                    String refkeyid3 = plannodeDS.getValue(j, "refkeyid3");
                    String scheduleplannodeid = plannodeDS.getValue(j, "scheduleplannodeid");
                    String oldkey = refkeyid1 + ";" + refkeyid2 + ";" + refkeyid3;
                    if (oldnewmapforsdc.get(oldkey) == null || oldkey.equals(oldnewmapforsdc.get(oldkey))) continue;
                    String[] newkeys = StringUtil.split((String)oldnewmapforsdc.get(oldkey), ";");
                    this.database.executePreparedUpdate("UPDATE scheduleplannode set refkeyid1=?, refkeyid2=?, refkeyid3=? WHERE scheduleplanid=? AND scheduleplannodeid=?", new Object[]{newkeys[0], newkeys[1], newkeys[2], schedulePlanId, scheduleplannodeid});
                }
            }
        }
    }

    private DataSet getSchedulePlanItemExecDs(SDIData sdiData) {
        DataSet scheduleplanitemexec = new DataSet(this.getConnectionInfo());
        scheduleplanitemexec.addColumn("scheduleplanitemid", 0);
        scheduleplanitemexec.addColumn("scheduleplanid", 0);
        scheduleplanitemexec.addColumn("currentcount", 1);
        scheduleplanitemexec.addColumn("executedflag", 0);
        scheduleplanitemexec.addColumn("inprocessflag", 0);
        scheduleplanitemexec.addColumn("scheduledtodt", 2);
        scheduleplanitemexec.addColumn("nextscheduledt", 2);
        scheduleplanitemexec.addColumn("lastscheduledt", 2);
        scheduleplanitemexec.addColumn("lasteventdt", 2);
        sdiData.setDataset("scheduleplanitemexec", scheduleplanitemexec);
        return scheduleplanitemexec;
    }

    private void correctSoftLinkedKeyidFields(DataSet tableDataSet, String sdcidcolumnid, String keyid1columnid, String keyid2columnid, String keyid3columnid, HashMap actionProps) {
        if (this.isCMTImport() && tableDataSet != null) {
            for (int i = 0; i < tableDataSet.getRowCount(); ++i) {
                String[] newkeys;
                HashMap oldnewkeyforsdcMap;
                String sdcid = tableDataSet.getValue(i, sdcidcolumnid);
                String keyid1 = tableDataSet.getValue(i, keyid1columnid);
                String keyid2 = tableDataSet.getValue(i, keyid2columnid);
                String keyid3 = tableDataSet.getValue(i, keyid3columnid);
                HashMap oldnewkeyMap = (HashMap)actionProps.get("oldnewkeymap");
                if (oldnewkeyMap.get(sdcid) == null || (oldnewkeyforsdcMap = (HashMap)oldnewkeyMap.get(sdcid)).get(keyid1 + ";" + keyid2 + ";" + keyid3) == null || (newkeys = StringUtil.split((String)oldnewkeyforsdcMap.get(keyid1 + ";" + keyid2 + ";" + keyid3), ";")).length != 3) continue;
                tableDataSet.setValue(i, "randomizationkeyid1", newkeys[0]);
                tableDataSet.setValue(i, "randomizationkeyid2", newkeys[1]);
                tableDataSet.setValue(i, "randomizationkeyid3", newkeys[2]);
            }
        }
    }

    private void copyScheduleTemplate(boolean setSourceToTemplate, DataSet primary, SchedulerUtil schedulerUtil, int i, String templateSdcId, String linkField1, String linkField2, String newSourceKeyid1, String newSourceKeyid2) throws SapphireException {
        PropertyList copyTemplateProps = new PropertyList();
        copyTemplateProps.setProperty("sdcid", templateSdcId);
        PropertyList embeddedTemplateIdProps = new PropertyList();
        embeddedTemplateIdProps.setProperty("sdcid", templateSdcId);
        schedulerUtil.getNewEmbeddedTemplateId(embeddedTemplateIdProps);
        String newkeyid1 = embeddedTemplateIdProps.getProperty("newkeyid1");
        copyTemplateProps.setProperty("keyid1", newkeyid1);
        String newkeyid2 = embeddedTemplateIdProps.getProperty("newkeyid2");
        if (!newkeyid2.isEmpty()) {
            copyTemplateProps.setProperty("keyid2", newkeyid2);
        }
        if (setSourceToTemplate) {
            copyTemplateProps.setProperty(linkField1, newSourceKeyid1);
            if (!linkField2.isEmpty()) {
                copyTemplateProps.setProperty(linkField2, newSourceKeyid2);
            }
        }
        this.getActionProcessor().processAction("AddSDI", "1", copyTemplateProps);
        primary.setString(i, "scheduletemplatesdcid", templateSdcId);
        primary.setString(i, "scheduletemplatekeyid1", newkeyid1);
        primary.setString(i, "scheduletemplatekeyid2", newkeyid2);
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        List<String> schedulePlanIdList = Arrays.asList(actionProps.getProperty("keyid1", "").split(";"));
        List<String> schedulePlanItemIdList = Arrays.asList(actionProps.getProperty("keyid2", "").split(";"));
        SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
        for (int i = 0; i < schedulePlanIdList.size(); ++i) {
            String schedulePlanId = schedulePlanIdList.get(i);
            String schedulePlanItemId = schedulePlanItemIdList.get(i);
            if (schedulerUtil.getSchedulePlanTypeFlag(schedulePlanId).equals("M")) {
                schedulerUtil.cleanupEmptyNodes(schedulePlanId);
            }
            Object[] deleteParams = new String[]{schedulePlanId, schedulePlanItemId};
            this.database.executePreparedUpdate("UPDATE s_sample set eventplan=null, eventplanitem=null, eventdt=null, eventnum=null WHERE eventplan = ? AND eventplanitem = ?", deleteParams);
        }
        schedulerUtil.deleteEmbeddedScheduleTemplate(schedulePlanIdList, schedulePlanItemIdList);
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        void var11_16;
        this.correctSoftLinkedKeyidFields(sdiData.getDataset("schedulerandomization"), "randomizationsdcid", "randomizationkeyid1", "randomizationkeyid2", "randomizationkeyid3", actionProps);
        DataSet primary = sdiData.getDataset("primary");
        DataSet scheduleplanitemexec = this.getSchedulePlanItemExecDs(sdiData);
        boolean convertTimeZones = actionProps.getProperty("convert_usertimezones", "N").startsWith("Y");
        HashMap<String, String> planItems = new HashMap<String, String>();
        boolean isAjax = actionProps.getProperty("__ajaxedit", "N").startsWith("Y");
        this.checkValidRules(primary, isAjax);
        for (int i = 0; i < primary.size(); ++i) {
            String planId = primary.getString(i, "scheduleplanid");
            String itemId = primary.getString(i, "scheduleplanitemid");
            if (!planItems.containsKey(planId)) {
                planItems.put(planId, ";" + (String)itemId);
                continue;
            }
            String string = (String)planItems.get(planId);
            planItems.put(planId, string + ";" + (String)itemId);
        }
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        TimeZone userTimeZone = m18n.getTimezone();
        for (Map.Entry entry : planItems.entrySet()) {
            String scheduleplanid = (String)entry.getKey();
            TimeZone planTimeZone = null;
            if (convertTimeZones) {
                String sql = "select timezone from scheduleplan where scheduleplanid = ?";
                Object[] params = new String[]{scheduleplanid};
                DataSet tzDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                String timeZoneStr = tzDs.getString(0, "timezone");
                planTimeZone = timeZoneStr != null && !timeZoneStr.isEmpty() ? TimeZone.getTimeZone(timeZoneStr) : TimeZone.getDefault();
            }
            StringBuilder fromAtoXitemlist = new StringBuilder();
            StringBuilder timingChangedItemList = new StringBuilder();
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("scheduleplanid", scheduleplanid);
            DataSet filteredDs = primary.getFilteredDataSet(filterMap);
            for (int i = 0; i < filteredDs.size(); ++i) {
                String activeFlag;
                boolean isHidden;
                if (this.hasPrimaryValueChanged(filteredDs, i, "activeflag") && (isHidden = (activeFlag = filteredDs.getValue(i, "activeflag")).equals("N"))) {
                    if (!primary.isValidColumn("planitemstatus")) {
                        primary.addColumn("planitemstatus", 0);
                    }
                    filteredDs.setString(i, "planitemstatus", "X");
                }
                String newstatus = filteredDs.getString(i, "planitemstatus");
                String oldstatus = this.getOldPrimaryValue(filteredDs, i, "planitemstatus");
                if (newstatus == null) {
                    newstatus = oldstatus;
                }
                Calendar startdt = filteredDs.getCalendar(i, "startdt");
                Calendar stopdt = filteredDs.getCalendar(i, "stopdt");
                if (convertTimeZones) {
                    int timeZoneCorrection;
                    if (startdt != null) {
                        startdt.set(13, 0);
                        timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, startdt);
                        startdt.add(14, -timeZoneCorrection);
                    }
                    if (stopdt != null) {
                        stopdt.set(13, 0);
                        timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, stopdt);
                        stopdt.add(14, -timeZoneCorrection);
                    }
                }
                if (!this.hasPrimaryValueChanged(filteredDs, i, "startdt")) {
                    startdt = this.getOldPrimaryCalendar(filteredDs, i, "startdt");
                }
                Calendar now = DateTimeUtil.getNowCalendar();
                String scheduleplanitemid = filteredDs.getString(i, "scheduleplanitemid");
                if ("X".equals(newstatus) && "A".equals(oldstatus)) {
                    fromAtoXitemlist.append(";").append(scheduleplanitemid);
                    int row = this.getSchedulePlanItemExecRow(scheduleplanitemexec, scheduleplanid, scheduleplanitemid);
                    scheduleplanitemexec.setValue(row, "nextscheduledt", "(null)");
                    scheduleplanitemexec.setValue(row, "scheduledtodt", "(null)");
                    scheduleplanitemexec.setValue(row, "lastscheduledt", "(null)");
                    scheduleplanitemexec.setValue(row, "lasteventdt", "(null)");
                } else if ("A".equals(newstatus) && ("X".equals(oldstatus) || oldstatus.isEmpty())) {
                    int row = this.getSchedulePlanItemExecRow(scheduleplanitemexec, scheduleplanid, scheduleplanitemid);
                    if (startdt == null || startdt.after(now)) {
                        scheduleplanitemexec.setDate(row, "nextscheduledt", startdt);
                    } else {
                        scheduleplanitemexec.setDate(row, "nextscheduledt", now);
                    }
                }
                String oldRule = this.getOldPrimaryValue(filteredDs, i, "schedulerule");
                String oldStartDt = this.getOldPrimaryValue(filteredDs, i, "startdt");
                String oldStopDt = this.getOldPrimaryValue(filteredDs, i, "stopdt");
                if (!"A".equals(newstatus) || !"A".equals(oldstatus) || filteredDs.getString(i, "schedulerule", "").equals(oldRule) && filteredDs.getValue(i, "startdt", "").equals(oldStartDt) && filteredDs.getValue(i, "stopdt", "").equals(oldStopDt)) continue;
                timingChangedItemList.append(";").append(scheduleplanitemid);
                int row = this.getSchedulePlanItemExecRow(scheduleplanitemexec, scheduleplanid, scheduleplanitemid);
                if (startdt == null || startdt.after(now)) {
                    scheduleplanitemexec.setDate(row, "nextscheduledt", startdt);
                } else {
                    scheduleplanitemexec.setDate(row, "nextscheduledt", now);
                }
                scheduleplanitemexec.setValue(row, "scheduledtodt", "(null)");
                scheduleplanitemexec.setValue(row, "lastscheduledt", "(null)");
                scheduleplanitemexec.setValue(row, "lasteventdt", "(null)");
            }
            if (fromAtoXitemlist.length() > 1) {
                SchedulePlanItem.clearUnExecutedEvent(this.database, scheduleplanid, fromAtoXitemlist.substring(1));
            }
            if (timingChangedItemList.length() <= 1) continue;
            SchedulePlanItem.clearUnExecutedEvent(this.database, scheduleplanid, timingChangedItemList.substring(1));
        }
        SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
        boolean bl = false;
        while (var11_16 < primary.getRowCount()) {
            String schedulePlanId = primary.getString((int)var11_16, "scheduleplanid", "");
            String schedulePlanType = schedulerUtil.getSchedulePlanTypeFlag(schedulePlanId);
            if (schedulePlanType.equals("M")) {
                String policyNode = schedulerUtil.getPolicyNode(schedulePlanId);
                PropertyList schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNode);
                if (actionProps.containsKey("linkkeyid1")) {
                    PropertyList templateSource;
                    String sourceKeyid3;
                    String sourceKeyid2;
                    String sourceKeyid1;
                    String sourceSdc = primary.getString((int)var11_16, "linksdcid", this.getOldPrimaryValue(primary, (int)var11_16, "linksdcid"));
                    String nodeId = schedulerUtil.generateNodes(schedulerPolicy, schedulePlanId, sourceSdc, sourceKeyid1 = primary.getString((int)var11_16, "linkkeyid1", ""), sourceKeyid2 = primary.getString((int)var11_16, "linkkeyid2", ""), sourceKeyid3 = primary.getString((int)var11_16, "linkkeyid3", ""));
                    if (!nodeId.equals("")) {
                        primary.setString((int)var11_16, "scheduleplannodeid", nodeId);
                    }
                    String templateSdcId = primary.getString((int)var11_16, "scheduletemplatesdcid", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatesdcid"));
                    String templateKeyId1 = primary.getString((int)var11_16, "scheduletemplatekeyid1", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid1"));
                    String templateKeyId2 = primary.getString((int)var11_16, "scheduletemplatekeyid2", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid2"));
                    String templateKeyId3 = primary.getString((int)var11_16, "scheduletemplatekeyid3", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid3"));
                    if (!sourceKeyid1.equals("") && schedulerUtil.isSingleItemTemplate(templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3) && !(templateSource = schedulerUtil.getTemplateSource(templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3, sourceSdc, schedulerPolicy)).getProperty("keyid1", "").equals("") && !templateSource.getProperty("keyid1", "").equals(sourceKeyid1)) {
                        String sourceField1 = templateSource.getProperty("linkfield1");
                        String sourceField2 = templateSource.getProperty("linkfield2");
                        schedulerUtil.editTemplateSource(templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3, sourceField1, sourceKeyid1, sourceField2, sourceKeyid2);
                    }
                }
                if (actionProps.containsKey("scheduletemplatesdcid") || actionProps.containsKey("scheduletemplatekeyid1") || actionProps.containsKey("scheduletemplatekeyid2") || actionProps.containsKey("scheduletemplatekeyid3")) {
                    String currentSourceSdc = primary.getString((int)var11_16, "linksdcid", this.getOldPrimaryValue(primary, (int)var11_16, "linksdcid"));
                    String currentSourceKeyid1 = primary.getString((int)var11_16, "linkkeyid1", this.getOldPrimaryValue(primary, (int)var11_16, "linkkeyid1"));
                    String currentSourceKeyid2 = primary.getString((int)var11_16, "linkkeyid2", this.getOldPrimaryValue(primary, (int)var11_16, "linkkeyid2"));
                    String currentSourceKeyid3 = primary.getString((int)var11_16, "linkkeyid3", this.getOldPrimaryValue(primary, (int)var11_16, "linkkeyid3"));
                    String templateSdcId = primary.getString((int)var11_16, "scheduletemplatesdcid", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatesdcid"));
                    String templateKeyId1 = primary.getString((int)var11_16, "scheduletemplatekeyid1", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid1"));
                    String templateKeyId2 = primary.getString((int)var11_16, "scheduletemplatekeyid2", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid2"));
                    String templateKeyId3 = primary.getString((int)var11_16, "scheduletemplatekeyid3", this.getOldPrimaryValue(primary, (int)var11_16, "scheduletemplatekeyid3"));
                    if (!templateKeyId1.equals("")) {
                        PropertyList newSource = schedulerUtil.getTemplateSource(templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3, currentSourceSdc, schedulerPolicy);
                        String sourceSdc = newSource.getProperty("sdcid", "");
                        String sourceKeyid1 = newSource.getProperty("keyid1", "");
                        String sourceKeyid2 = newSource.getProperty("keyid2", "");
                        String sourceKeyid3 = newSource.getProperty("keyid3", "");
                        if (!(sourceKeyid1.equals("") || sourceKeyid1.equals(currentSourceKeyid1) && sourceKeyid2.equals(currentSourceKeyid2) && sourceKeyid3.equals(currentSourceKeyid3))) {
                            primary.setString((int)var11_16, "linksdcid", sourceSdc);
                            primary.setString((int)var11_16, "linkkeyid1", sourceKeyid1);
                            primary.setString((int)var11_16, "linkkeyid2", sourceKeyid2);
                            primary.setString((int)var11_16, "linkkeyid3", sourceKeyid3);
                            String nodeId = schedulerUtil.generateNodes(schedulerPolicy, schedulePlanId, sourceSdc, sourceKeyid1, sourceKeyid2, sourceKeyid3);
                            primary.setString((int)var11_16, "scheduleplannodeid", nodeId);
                        }
                    }
                }
            }
            ++var11_16;
        }
    }

    private int getSchedulePlanItemExecRow(DataSet scheduleplanitemexec, String scheduleplanid, String scheduleplanitemid) throws SapphireException {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("scheduleplanid", scheduleplanid);
        filterMap.put("scheduleplanitemid", scheduleplanitemid);
        int row = scheduleplanitemexec.findRow(filterMap);
        if (row < 0) {
            String speSql = "select spe.* from scheduleplanitemexec spe where scheduleplanid = ? and scheduleplanitemid = ?";
            DataSet speDs = this.getQueryProcessor().getPreparedSqlDataSet(speSql, (Object[])new String[]{scheduleplanid, scheduleplanitemid});
            if (speDs.getRowCount() == 1) {
                scheduleplanitemexec.copyRow(speDs, 0, 1);
                row = scheduleplanitemexec.getRowCount() - 1;
            } else if (speDs.getRowCount() == 0) {
                String sql = "INSERT INTO SCHEDULEPLANITEMEXEC (SCHEDULEPLANID, SCHEDULEPLANITEMID) VALUES (?, ?)";
                this.getQueryProcessor().execPreparedUpdate(sql, new String[]{scheduleplanid, scheduleplanitemid});
                row = scheduleplanitemexec.addRow();
                scheduleplanitemexec.setValue(row, "scheduleplanid", scheduleplanid);
                scheduleplanitemexec.setValue(row, "scheduleplanitemid", scheduleplanitemid);
            } else {
                throw new SapphireException("Error finding correct scheduleplanitemexec-row");
            }
        }
        return row;
    }

    public void checkValidRules(DataSet primary, boolean throwError) throws SapphireException {
        if (this.isCMTImport()) {
            return;
        }
        StringBuilder notValidRules = new StringBuilder();
        for (int i = 0; i < primary.size(); ++i) {
            String calendarFlag;
            boolean isCountBasedRule;
            ScheduleRule rule;
            boolean valid;
            if (!this.hasPrimaryValueChanged(primary, i, "schedulerule") && !this.hasPrimaryValueChanged(primary, i, "planitemstatus")) continue;
            String scheduleRule = primary.getString(i, "schedulerule");
            if (scheduleRule == null && !primary.isValidColumn("schedulerule")) {
                scheduleRule = this.getOldPrimaryValue(primary, i, "schedulerule");
            }
            if (valid = (rule = new ScheduleRule(scheduleRule)).isValidRule(!(isCountBasedRule = (calendarFlag = primary.isValidColumn("calendarflag") ? primary.getString(i, "calendarflag", "") : this.getOldPrimaryValue(primary, i, "calendarflag")).equals("N")))) continue;
            String planId = primary.getString(i, "scheduleplanid");
            String itemId = primary.getString(i, "scheduleplanitemid");
            String itemdesc = primary.getString(i, "scheduleplanitemdesc", "");
            if (!itemdesc.isEmpty()) {
                itemdesc = "(" + itemdesc + ")";
            }
            notValidRules.append(planId).append(", ").append(this.getTranslationProcessor().translate("item")).append(" ").append(itemId).append(itemdesc).append(": ").append(scheduleRule).append("\n");
            primary.setValue(i, "planitemstatus", "X");
        }
        if (notValidRules.length() > 0) {
            if (throwError) {
                throw new SapphireException(this.getTranslationProcessor().translate("The following Schedule Plan Items have invalid Schedule Rules and have been turned off:\n") + notValidRules.toString());
            }
            this.logger.error("The following Schedule Plan Items have invalid Schedule Rules and have been turned off: " + notValidRules.toString());
            this.setError("CheckValidScheduleRules", "INFORMATION", this.getTranslationProcessor().translate("The following Schedule Plan Items have invalid Schedule Rules and have been turned off:\n") + notValidRules.toString());
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet planitemExec = sdiData.getDataset("scheduleplanitemexec");
        if (planitemExec.getRowCount() > 0) {
            DataSetUtil.update(this.database, planitemExec, "scheduleplanitemexec", new String[]{"scheduleplanid", "scheduleplanitemid"});
        }
        if (!this.isCMTImport()) {
            DataSet primary = sdiData.getDataset("primary");
            HashMap<String, String> planItems = new HashMap<String, String>();
            for (int i = 0; i < primary.size(); ++i) {
                String planId = primary.getString(i, "scheduleplanid");
                String itemId = primary.getString(i, "scheduleplanitemid");
                if (!planItems.containsKey(planId)) {
                    planItems.put(planId, ";" + itemId);
                    continue;
                }
                String oldItems = (String)planItems.get(planId);
                planItems.put(planId, oldItems + ";" + itemId);
            }
            for (Map.Entry planItem : planItems.entrySet()) {
                String schedulePlanId = (String)planItem.getKey();
                SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
                if (!schedulerUtil.getSchedulePlanTypeFlag(schedulePlanId).equals("M")) continue;
                schedulerUtil.cleanupEmptyNodes(schedulePlanId);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        List<String> schedulePlanIdList = Arrays.asList(actionProps.getProperty("keyid1", "").split(";"));
        List<String> schedulePlanItemIdList = Arrays.asList(actionProps.getProperty("keyid2", "").split(";"));
        for (int i = 0; i < schedulePlanIdList.size(); ++i) {
            String schedulePlanId = schedulePlanIdList.get(i);
            String schedulePlanItemId = schedulePlanItemIdList.get(i);
            Object[] confirmDeleteParams = new String[]{schedulePlanId, schedulePlanItemId};
            int nofSamples = this.getQueryProcessor().getPreparedCount("select count(s_sampleid) from s_sample where eventplan = ? and eventplanitem = ?", confirmDeleteParams);
            if (nofSamples > 0) {
                throw new SapphireException("Deleting schedule plan items not allowed. Plan item(s) have samples associated. Use hide functionality instead.");
            }
            int nofWorkOrders = this.getQueryProcessor().getPreparedCount("select count(workorderid) from workorder where scheduleplanid = ? and scheduleplanitemid = ?", confirmDeleteParams);
            if (nofWorkOrders <= 0) continue;
            throw new SapphireException("Deleting schedule plan items not allowed. Plan item(s) have workorders associated. Use hide functionality instead.");
        }
        DataSet schedulePlanIdsDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid = ? ", (Object[])new String[]{rsetid});
        for (int i = 0; i < schedulePlanIdsDs.getRowCount(); ++i) {
            String schedulePlanId = schedulePlanIdsDs.getString(i, "keyid1");
            Object[] vars = new Object[]{schedulePlanId, rsetid, schedulePlanId};
            this.database.executePreparedUpdate("UPDATE sdidata set scheduleplanid=null, scheduleplanitemid=null WHERE scheduleplanid = ? AND scheduleplanitemid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = ? and keyid1 = ? )", vars);
            this.database.executePreparedUpdate("UPDATE sdiworkitem set scheduleplanid=null, scheduleplanitemid=null WHERE scheduleplanid = ? AND scheduleplanitemid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = ? and keyid1 = ? )", vars);
            this.database.executePreparedUpdate("DELETE FROM scheduleplanitemdetails WHERE scheduleplanid = ? AND scheduleplanitemid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = ? and keyid1 = ? )", vars);
            this.database.executePreparedUpdate("DELETE FROM sdiworkitem WHERE keyid1 = ? AND keyid2 IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = ? and keyid1 = ? )", vars);
            this.database.executePreparedUpdate("DELETE FROM scheduleplanitemexec WHERE scheduleplanid = ? AND scheduleplanitemid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = ? and keyid1 = ?  )", vars);
        }
    }

    public static void clearUnExecutedEvent(DBAccess database, String scheduleplanid, String itemidlist) throws SapphireException {
        String whereclause = SchedulePlanItem.getCommonWhereClause(scheduleplanid, itemidlist);
        String sql = "delete from scheduleevent " + whereclause + " and eventstatus in ('S', 'F', 'I')";
        int rowdeleted = database.executeUpdate(sql);
        if (Trace.on) {
            Trace.log("Delete scheduled events:" + rowdeleted + " events cleared using " + sql);
        }
    }

    private static String getCommonWhereClause(String scheduleplanid, String itemidlist) {
        String sql = "where scheduleplanid='" + scheduleplanid + "' ";
        if (!itemidlist.equals("(all)")) {
            String inclause = SearchUtil.toQueryInClause(itemidlist);
            sql = sql + " and scheduleplanitemid in " + inclause;
        }
        return sql;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

