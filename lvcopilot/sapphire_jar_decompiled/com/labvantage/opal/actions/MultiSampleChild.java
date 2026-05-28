/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.ajax.util.SetActionProgressStatus;
import com.labvantage.opal.util.ChildSampleUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAlias;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.AddTrackItem;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.actions.workitem.EditSDIWorkItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MultiSampleChild
extends BaseAction
implements sapphire.action.MultiSampleChild {
    public static final String MODE_CHILD = "Child";
    public static final String MODE_ALIQUOT = "Aliquot";
    public static final String MODE_DERIVATIVE = "Derivative";
    private String accesscontrolledflag = "";
    Map<String, Map<String, String>> copyDownDataCache = new HashMap<String, Map<String, String>>();
    private Map<String, String> sampleTypeFTWarnCache = new HashMap<String, String>();
    private Map<String, String> sampleTypeFTMaxCache = new HashMap<String, String>();

    /*
     * WARNING - void declaration
     */
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String[] parent_depleteflag;
        void var60_109;
        int i;
        HashMap parentMap;
        int input_childpropertycount;
        String parentsamples = actionProps.getProperty("parent_sampleid", "").trim();
        String input_childsampleid = actionProps.getProperty("child_sampleid", "").trim();
        String input_childcopies = actionProps.getProperty("child_copies", "").trim();
        String input_childquantity = actionProps.getProperty("child_quantity", "").trim();
        String input_childunit = actionProps.getProperty("child_unit", "").trim();
        String input_childsamplestatus = actionProps.getProperty("childsamplestatus", "").trim();
        String input_childstoragestatus = actionProps.getProperty("child_storagestatus", "").trim();
        String input_childtemplateid = actionProps.getProperty("child_templateid", "").trim();
        String input_childsampletypeid = actionProps.getProperty("child_sampletypeid", "").trim();
        String input_childpreptypeid = actionProps.getProperty("child_preptypeid", "").trim();
        String input_childtreatmentid = actionProps.getProperty("child_treatmentid", "").trim();
        String input_childftflag = actionProps.getProperty("child_freezethawflag", "").trim();
        String input_childftcount = actionProps.getProperty("child_freezethawcount", "").trim();
        String input_childftwarn = actionProps.getProperty("child_freezethawwarn", "").trim();
        String input_childftmax = actionProps.getProperty("child_freezethawmax", "").trim();
        String input_childstudyid = actionProps.getProperty("child_studyid", "").trim();
        String input_containertypeid = actionProps.getProperty("containertypeid", "").trim();
        String[] sdiworkitemid = StringUtil.split(actionProps.getProperty("sdiworkitemid", "").trim(), ";");
        String sdiworkitemcompletionstatus = actionProps.getProperty("sdiworkitemcompletionstatus", "").trim();
        String trackProgressID = actionProps.getProperty("__trackprogressid", "").trim();
        if (OpalUtil.isEmpty(input_childcopies) && OpalUtil.isEmpty(input_childsampleid)) {
            this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
            throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("Either number of child sample copies or child sample id must be given."));
        }
        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Validating action inputs..."));
        String[] policyChildStorageStatus = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("childsamplestatus", "In Prep");
        ArrayList<String> invalidprops = new ArrayList<String>();
        int n = input_childpropertycount = input_childcopies.length() > 0 ? StringUtil.split(input_childcopies, ";").length : StringUtil.split(input_childsampleid, ";").length;
        if (input_childquantity.length() > 0 && StringUtil.split(input_childquantity, ";").length != input_childpropertycount) {
            invalidprops.add("child_quantity");
        }
        if (input_childunit.length() > 0 && StringUtil.split(input_childunit, ";").length != input_childpropertycount) {
            invalidprops.add("child_unit");
        }
        if (input_childsamplestatus.length() > 0 && StringUtil.split(input_childsamplestatus, ";").length != input_childpropertycount) {
            invalidprops.add("childsamplestatus");
        }
        if (input_childstoragestatus.length() > 0 && StringUtil.split(input_childstoragestatus, ";").length != input_childpropertycount) {
            invalidprops.add("child_storagestatus");
        }
        if (input_childtemplateid.length() > 0 && StringUtil.split(input_childtemplateid, ";").length != input_childpropertycount) {
            invalidprops.add("child_templateid");
        }
        if (input_childsampletypeid.length() > 0 && StringUtil.split(input_childsampletypeid, ";").length != input_childpropertycount) {
            invalidprops.add("child_sampletypeid");
        }
        if (input_childpreptypeid.length() > 0 && StringUtil.split(input_childpreptypeid, ";").length != input_childpropertycount) {
            invalidprops.add("child_preptypeid");
        }
        if (input_childtreatmentid.length() > 0 && StringUtil.split(input_childtreatmentid, ";").length != input_childpropertycount) {
            invalidprops.add("child_treatmentid");
        }
        if (input_childftflag.length() > 0 && StringUtil.split(input_childftflag, ";").length != input_childpropertycount) {
            invalidprops.add("child_freezethawflag");
        }
        if (input_childftcount.length() > 0 && StringUtil.split(input_childftcount, ";").length != input_childpropertycount) {
            invalidprops.add("child_freezethawcount");
        }
        if (input_childftwarn.length() > 0 && StringUtil.split(input_childftwarn, ";").length != input_childpropertycount) {
            invalidprops.add("child_freezethawwarn");
        }
        if (input_childftmax.length() > 0 && StringUtil.split(input_childftmax, ";").length != input_childpropertycount) {
            invalidprops.add("child_freezethawmax");
        }
        if (input_childstudyid.length() > 0 && StringUtil.split(input_childstudyid, ";").length != input_childpropertycount) {
            invalidprops.add("child_studyid");
        }
        if (invalidprops.size() > 0) {
            this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
            throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("Number of inputs for property does not match number of child sample inputs") + ": " + OpalUtil.toDelimitedString(invalidprops, ", "));
        }
        boolean isChildSampleIDPassedIn = input_childsampleid.length() > 0;
        String[] parent_sampleid = StringUtil.split(parentsamples, ";");
        String[] child_sampleid = StringUtil.split(input_childsampleid, ";");
        String[] child_copies = StringUtil.split(input_childcopies, ";");
        String[] child_samplestatus = StringUtil.split(input_childsamplestatus, ";");
        String[] child_storagestatus = StringUtil.split(input_childstoragestatus, ";");
        String[] child_templateid = StringUtil.split(input_childtemplateid, ";");
        String[] child_quantity = StringUtil.split(input_childquantity, ";");
        String[] child_unit = StringUtil.split(input_childunit, ";");
        String[] child_sampletypeid = StringUtil.split(input_childsampletypeid, ";");
        String[] child_preptypeid = StringUtil.split(input_childpreptypeid, ";");
        String[] child_treatmentid = StringUtil.split(input_childtreatmentid, ";");
        String[] child_ftflag = StringUtil.split(input_childftflag, ";");
        String[] child_ftcount = StringUtil.split(input_childftcount, ";");
        String[] child_ftwarn = StringUtil.split(input_childftwarn, ";");
        String[] child_ftmax = StringUtil.split(input_childftmax, ";");
        String[] child_studyid = StringUtil.split(input_childstudyid, ";");
        String[] child_containertypeid = StringUtil.split(input_containertypeid, ";");
        String copyDownColumns = actionProps.getProperty("copydowncolumns", "");
        copyDownColumns = StringUtil.replaceAll(copyDownColumns, ",", ";");
        String mode = actionProps.getProperty("mode", MODE_ALIQUOT);
        boolean propsmatch = "Y".equals(actionProps.getProperty("propsmatch", "N"));
        PropertyList props = new PropertyList();
        if (StringUtil.getLen(parentsamples) == 0L) {
            this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
            throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("Parent sample is missing."));
        }
        if (StringUtil.getLen(input_childsampleid) > 0L) {
            this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
            if (parent_sampleid.length > 1 && parent_sampleid.length != child_sampleid.length) {
                throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("If child sample id is given, then either there should be only one parent sample or number of parent samples must match the number of child samples."));
            }
        }
        if (propsmatch && parent_sampleid.length != child_copies.length) {
            this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
            throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("In PROPSMATCH mode, the number of child sample copies must match the number of parent samples."));
        }
        if (StringUtil.getLen(copyDownColumns) > 0L) {
            String[] copyDownColumnArray = StringUtil.split(copyDownColumns, ";");
            List<String> sampleColumnList = OpalUtil.toList(this.getSDCProcessor().getColumnData("Sample").getColumnValues("columnid", ";"), ";");
            for (String columnid : copyDownColumnArray) {
                if (sampleColumnList.contains(columnid)) continue;
                this.trackActionProgress(trackProgressID, "||ERROR||Missing action input");
                throw new SapphireException("MultiSampleChild: Missing action input", "VALIDATION", this.getTranslationProcessor().translate("Invalid copy down column given") + " (" + columnid + ")");
            }
        }
        this.accesscontrolledflag = (String)this.getSDCProcessor().getSDCProperties("Sample").get("accesscontrolledflag");
        DataSet secondarySecurityInfo = this.getSecondarySecurityInfo(this.accesscontrolledflag, parentsamples + ";" + input_childsampleid);
        PropertyList freezethawcopydown = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getPropertyListNotNull("freezethawcopydown");
        DataSet childColumnDataset = new DataSet();
        for (Object o : actionProps.keySet()) {
            String property;
            if (!(o instanceof String) || (property = (String)o) == null || !property.startsWith("childcolumn_")) continue;
            childColumnDataset.addColumnValues(property.substring(12), 0, actionProps.getProperty(property, ""), ";");
        }
        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Inputs ok. Processing child samples..."));
        DataSet parentData = this.getParentSampleData(parentsamples);
        HashMap parentDataMap = new HashMap();
        for (int i2 = 0; i2 < parentData.size(); ++i2) {
            HashMap<String, String> map = new HashMap<String, String>();
            for (int col = 0; col < parentData.getColumnCount(); ++col) {
                String columnid = parentData.getColumnId(col);
                map.put(columnid, parentData.getValue(i2, columnid, ""));
            }
            parentDataMap.put(parentData.getString(i2, "s_sampleid"), map);
        }
        String sysuserid = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
        Calendar now = DateTimeUtil.getNowCalendar();
        boolean expandCopies = false;
        DataSet childSampleDataSet = new DataSet();
        if (isChildSampleIDPassedIn) {
            for (int childSampleIndex = 0; childSampleIndex < child_sampleid.length; ++childSampleIndex) {
                String columnid;
                void var60_81;
                void var66_160;
                Iterator<String> _childstudyid;
                String string;
                String childSampleId = child_sampleid[childSampleIndex];
                String parentSampleId = parent_sampleid.length == 1 ? parent_sampleid[0] : parent_sampleid[childSampleIndex];
                if (StringUtil.getLen(childSampleId) <= 0L || StringUtil.getLen(parentSampleId) <= 0L) continue;
                int childInDifferentStudy = 0;
                Object var60_82 = null;
                Map<String, String> trackitemCopyDownMap = null;
                Map<String, String> familyCopyDownMap = null;
                parentMap = (HashMap)parentDataMap.get(parentSampleId);
                String parentSampleStatus = (String)parentMap.get("samplestatus");
                String parentStorageStatus = (String)parentMap.get("storagestatus");
                String string2 = string = "Initial".equals(parentSampleStatus) ? "Initial" : "Received";
                if (child_samplestatus != null && child_samplestatus.length > childSampleIndex && StringUtil.getLen(child_samplestatus[childSampleIndex]) > 0L) {
                    if ("inherit".equalsIgnoreCase(child_samplestatus[childSampleIndex])) {
                        String string3 = parentSampleStatus;
                    } else {
                        String string4 = child_samplestatus[childSampleIndex];
                    }
                }
                Object childStorageStatus = policyChildStorageStatus;
                if (child_storagestatus != null && child_storagestatus.length > childSampleIndex && StringUtil.getLen(child_storagestatus[childSampleIndex]) > 0L) {
                    childStorageStatus = "inherit".equalsIgnoreCase(child_storagestatus[childSampleIndex]) ? parentStorageStatus : child_storagestatus[childSampleIndex];
                }
                String childSampleTypeid = "";
                int row = childSampleDataSet.addRow();
                String childstudyid = (String)parentMap.get("sstudyid");
                String childsamplefamilyid = (String)parentMap.get("samplefamilyid");
                if (input_childstudyid.length() > 0) {
                    Iterator<String> iterator = _childstudyid = child_studyid.length > childSampleIndex ? child_studyid[childSampleIndex] : "";
                    if (!OpalUtil.isEmpty((String)((Object)_childstudyid))) {
                        childstudyid = _childstudyid;
                        childsamplefamilyid = "";
                        childInDifferentStudy = 1;
                    }
                }
                childSampleDataSet.setString(row, "sstudyid", childstudyid);
                if (OpalUtil.isNotEmpty(childsamplefamilyid)) {
                    childSampleDataSet.setString(row, "samplefamilyid", childsamplefamilyid);
                }
                childSampleDataSet.setString(row, "parentsampleid", parentSampleId);
                childSampleDataSet.setString(row, "parent_sampleid", parentSampleId);
                childSampleDataSet.setString(row, "childsampleid", childSampleId);
                childSampleDataSet.setString(row, "samplestatus", (String)var66_160);
                childSampleDataSet.setString(row, "storagestatus", (String)childStorageStatus);
                if (!"Allocated".equals(childStorageStatus) && !"In Prep".equals(childStorageStatus)) {
                    childSampleDataSet.setString(row, "confirmeddt", "n");
                    childSampleDataSet.setString(row, "confirmedby", sysuserid);
                }
                childSampleDataSet.setString(row, "glpflag", parentMap.get("glpflag") != null ? (String)parentMap.get("glpflag") : "");
                childSampleDataSet.setString(row, "cocflag", parentMap.get("cocflag") != null ? (String)parentMap.get("cocflag") : "");
                childSampleDataSet.setString(row, "restrictionsflag", parentMap.get("restrictionsflag") != null ? (String)parentMap.get("restrictionsflag") : "");
                childSampleDataSet.setString(row, "templateid", child_templateid.length > childSampleIndex && child_templateid[childSampleIndex] != null && !"-1".equals(child_templateid[childSampleIndex]) ? child_templateid[childSampleIndex] : "");
                childSampleDataSet.setString(row, "applyworkitems", "Y");
                childSampleDataSet.setString(row, "__trackitem_qtycurrent", child_quantity.length > childSampleIndex && child_quantity[childSampleIndex] != null ? child_quantity[childSampleIndex] : "");
                childSampleDataSet.setString(row, "__trackitem_qtyunits", child_unit.length > childSampleIndex && child_unit[childSampleIndex] != null ? child_unit[childSampleIndex] : "");
                childSampleDataSet.setString(row, "__trackitem_containertypeid", child_containertypeid.length > childSampleIndex && child_containertypeid[childSampleIndex] != null ? child_containertypeid[childSampleIndex] : "");
                if (MODE_ALIQUOT.equals(mode)) {
                    childSampleTypeid = (String)parentMap.get("sampletypeid");
                    if (childInDifferentStudy != 0) {
                        Map<String, String> map = this.getCopyDownDataMap("childstudy", "Sample", parentSampleId, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentSampleId, null);
                        familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentSampleId, null);
                    } else {
                        Map<String, String> map = this.getCopyDownDataMap("aliquot", "Sample", parentSampleId, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", parentSampleId, null);
                    }
                } else if (MODE_DERIVATIVE.equals(mode)) {
                    childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                    childSampleDataSet.setString(row, "preptypeid", child_preptypeid.length > childSampleIndex && child_preptypeid[childSampleIndex] != null && !"-1".equals(child_preptypeid[childSampleIndex]) ? child_preptypeid[childSampleIndex] : "");
                    childSampleDataSet.setString(row, "child_treatmentid", child_treatmentid.length > childSampleIndex && child_treatmentid[childSampleIndex] != null && !"-1".equals(child_treatmentid[childSampleIndex]) ? child_treatmentid[childSampleIndex] : "");
                    if (childInDifferentStudy != 0) {
                        Map<String, String> map = this.getCopyDownDataMap("childstudy", "Sample", parentSampleId, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentSampleId, null);
                        familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentSampleId, null);
                    } else {
                        Map<String, String> map = this.getCopyDownDataMap("derivative", "Sample", parentSampleId, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("derivative", "TrackItemSDC", parentSampleId, null);
                    }
                } else if (MODE_CHILD.equals(mode)) {
                    childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                    Map<String, String> map = this.getCopyDownDataMap("childstudy", "Sample", parentSampleId, copyDownColumns);
                    trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentSampleId, null);
                }
                if (childSampleTypeid == null || childSampleTypeid.trim().equals("-1")) {
                    childSampleTypeid = "";
                }
                childSampleDataSet.setString(row, "sampletypeid", childSampleTypeid);
                if (var60_81 != null) {
                    _childstudyid = var60_81.keySet().iterator();
                    while (_childstudyid.hasNext()) {
                        columnid = _childstudyid.next();
                        if ("sampletypeid".equalsIgnoreCase(columnid)) {
                            if (!OpalUtil.isEmpty(childSampleTypeid)) continue;
                            childSampleDataSet.setString(row, columnid, (String)var60_81.get(columnid));
                            continue;
                        }
                        childSampleDataSet.setString(row, columnid, (String)var60_81.get(columnid));
                    }
                }
                if (trackitemCopyDownMap != null) {
                    _childstudyid = trackitemCopyDownMap.keySet().iterator();
                    while (_childstudyid.hasNext()) {
                        columnid = _childstudyid.next();
                        childSampleDataSet.setString(row, "__trackitem_" + columnid, trackitemCopyDownMap.get(columnid));
                    }
                }
                if (childInDifferentStudy != 0 && familyCopyDownMap != null) {
                    _childstudyid = familyCopyDownMap.keySet().iterator();
                    while (_childstudyid.hasNext()) {
                        columnid = _childstudyid.next();
                        childSampleDataSet.setString(row, "__samplefamily_" + columnid, familyCopyDownMap.get(columnid));
                    }
                }
                if (childColumnDataset.size() > 0) {
                    for (int col = 0; col < childColumnDataset.getColumnCount(); ++col) {
                        columnid = childColumnDataset.getColumnId(col);
                        childSampleDataSet.setString(row, columnid, childColumnDataset.getString(childSampleIndex, columnid, ""));
                    }
                }
                if (MODE_ALIQUOT.equals(mode) || MODE_DERIVATIVE.equals(mode)) {
                    String childFreezeThawFlag;
                    String string5 = childFreezeThawFlag = child_ftflag.length > childSampleIndex && child_ftflag[childSampleIndex] != null ? child_ftflag[childSampleIndex] : "";
                    if ("Y".equals(childFreezeThawFlag)) {
                        childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcount", child_ftcount.length > childSampleIndex && child_ftcount[childSampleIndex] != null ? child_ftcount[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", child_ftwarn.length > childSampleIndex && child_ftwarn[childSampleIndex] != null ? child_ftwarn[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", child_ftmax.length > childSampleIndex && child_ftmax[childSampleIndex] != null ? child_ftmax[childSampleIndex] : "");
                    } else if ("Y".equals(parentMap.get("freezethawflag"))) {
                        PropertyList freezethawprops = new PropertyList();
                        if (MODE_DERIVATIVE.equals(mode)) {
                            freezethawprops = freezethawcopydown.getPropertyListNotNull("derivative");
                        } else if (MODE_ALIQUOT.equals(mode)) {
                            freezethawprops = freezethawcopydown.getPropertyListNotNull("aliquot");
                        }
                        if ("Y".equals(freezethawprops.getProperty("freezethawcount"))) {
                            childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                            childSampleDataSet.setString(row, "__trackitem_freezethawcount", (String)parentMap.get("freezethawcount"));
                            String freezethawwarn = freezethawprops.getProperty("freezethawwarn");
                            if ("Parent".equals(freezethawwarn)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", (String)parentMap.get("freezethawcountwarn"));
                            } else if ("Sample Type".equals(freezethawwarn)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", this.getSampleTypeFreezeThawWarn(childSampleTypeid));
                            }
                            String freezethawmax = freezethawprops.getProperty("freezethawmax");
                            if ("Parent".equals(freezethawmax)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", (String)parentMap.get("freezethawcountmax"));
                            } else if ("Sample Type".equals(freezethawmax)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", this.getSampleTypeFreezeThawMax(childSampleTypeid));
                            }
                        }
                    }
                }
                if ("D".equals(this.accesscontrolledflag)) {
                    childSampleDataSet.setString(row, "securityuser", parentMap.get("securityuser") != null ? (String)parentMap.get("securityuser") : "");
                    childSampleDataSet.setString(row, "securitydepartment", parentMap.get("securitydepartment") != null ? (String)parentMap.get("securitydepartment") : "");
                }
                if ("S".equals(this.accesscontrolledflag)) {
                    childSampleDataSet.setString(row, "securityset", parentMap.get("securityset") != null ? (String)parentMap.get("securityset") : "");
                }
                this.copyValuesFromActionInput(childSampleIndex, childSampleDataSet, row, actionProps);
            }
        } else if (propsmatch) {
            int childSampleIndex = 0;
            for (String string : parent_sampleid) {
                String childSampleStatus;
                boolean childInDifferentStudy = false;
                Map<String, String> sampleCopyDownMap = null;
                Map<String, String> trackitemCopyDownMap = null;
                Map<String, String> familyCopyDownMap = null;
                HashMap parentMap2 = (HashMap)parentDataMap.get(string);
                String string6 = (String)parentMap2.get("samplestatus");
                String[] parentStorageStatus = (String[])parentMap2.get("storagestatus");
                String string7 = childSampleStatus = "Initial".equals(string6) ? "Initial" : "Received";
                if (child_samplestatus != null && child_samplestatus.length > childSampleIndex && StringUtil.getLen(child_samplestatus[childSampleIndex]) > 0L) {
                    childSampleStatus = "inherit".equalsIgnoreCase(child_samplestatus[childSampleIndex]) ? string6 : child_samplestatus[childSampleIndex];
                }
                Object childStorageStatus = policyChildStorageStatus;
                if (child_storagestatus != null && child_storagestatus.length > childSampleIndex && StringUtil.getLen(child_storagestatus[childSampleIndex]) > 0L) {
                    childStorageStatus = "inherit".equalsIgnoreCase(child_storagestatus[childSampleIndex]) ? parentStorageStatus : child_storagestatus[childSampleIndex];
                }
                String childCopies = "1";
                if (child_copies != null && child_copies.length > childSampleIndex && StringUtil.getLen(child_copies[childSampleIndex]) > 0L) {
                    childCopies = child_copies[childSampleIndex];
                }
                String childSampleTypeid = "";
                int row = childSampleDataSet.addRow();
                String childstudyid = (String)parentMap2.get("sstudyid");
                String childsamplefamilyid = (String)parentMap2.get("samplefamilyid");
                if (input_childstudyid.length() > 0) {
                    String _childstudyid;
                    String string8 = _childstudyid = child_studyid.length > childSampleIndex ? child_studyid[childSampleIndex] : "";
                    if (!OpalUtil.isEmpty(_childstudyid)) {
                        childstudyid = _childstudyid;
                        childsamplefamilyid = "";
                        childInDifferentStudy = true;
                        String parentsamplefamilyid = (String)parentMap2.get("samplefamilyid");
                        this.addOrUpdateSampleFamilyParticipant(actionProps, parentsamplefamilyid, childstudyid, childSampleDataSet, row);
                    }
                }
                childSampleDataSet.setString(row, "sstudyid", childstudyid);
                if (!OpalUtil.isEmpty(childsamplefamilyid)) {
                    childSampleDataSet.setString(row, "samplefamilyid", childsamplefamilyid);
                }
                childSampleDataSet.setString(row, "parentsampleid", string);
                childSampleDataSet.setString(row, "parent_sampleid", string);
                childSampleDataSet.setString(row, "copies", childCopies);
                childSampleDataSet.setString(row, "samplestatus", childSampleStatus);
                childSampleDataSet.setString(row, "storagestatus", (String)childStorageStatus);
                if (!"Allocated".equals(childStorageStatus) && !"In Prep".equals(childStorageStatus)) {
                    childSampleDataSet.setString(row, "confirmeddt", "n");
                    childSampleDataSet.setString(row, "confirmedby", sysuserid);
                }
                childSampleDataSet.setString(row, "glpflag", parentMap2.get("glpflag") != null ? (String)parentMap2.get("glpflag") : "");
                childSampleDataSet.setString(row, "cocflag", parentMap2.get("cocflag") != null ? (String)parentMap2.get("cocflag") : "");
                childSampleDataSet.setString(row, "restrictionsflag", parentMap2.get("restrictionsflag") != null ? (String)parentMap2.get("restrictionsflag") : "");
                childSampleDataSet.setString(row, "templateid", child_templateid.length > childSampleIndex && child_templateid[childSampleIndex] != null && !"-1".equals(child_templateid[childSampleIndex]) ? child_templateid[childSampleIndex] : "");
                childSampleDataSet.setString(row, "applyworkitems", "Y");
                childSampleDataSet.setString(row, "__trackitem_qtycurrent", child_quantity.length > childSampleIndex && child_quantity[childSampleIndex] != null ? child_quantity[childSampleIndex] : "");
                childSampleDataSet.setString(row, "__trackitem_qtyunits", child_unit.length > childSampleIndex && child_unit[childSampleIndex] != null ? child_unit[childSampleIndex] : "");
                childSampleDataSet.setString(row, "__trackitem_containertypeid", child_containertypeid.length > childSampleIndex && child_containertypeid[childSampleIndex] != null ? child_containertypeid[childSampleIndex] : "");
                if (MODE_ALIQUOT.equals(mode)) {
                    childSampleTypeid = (String)parentMap2.get("sampletypeid");
                    if (childInDifferentStudy) {
                        sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", string, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", string, null);
                        familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", string, null);
                    } else {
                        sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", string, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", string, null);
                    }
                } else if (MODE_DERIVATIVE.equals(mode)) {
                    childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                    childSampleDataSet.setString(row, "preptypeid", child_preptypeid.length > childSampleIndex && child_preptypeid[childSampleIndex] != null && !"-1".equals(child_preptypeid[childSampleIndex]) ? child_preptypeid[childSampleIndex] : "");
                    childSampleDataSet.setString(row, "child_treatmentid", child_treatmentid.length > childSampleIndex && child_treatmentid[childSampleIndex] != null && !"-1".equals(child_treatmentid[childSampleIndex]) ? child_treatmentid[childSampleIndex] : "");
                    if (childInDifferentStudy) {
                        sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", string, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", string, null);
                        familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", string, null);
                    } else {
                        sampleCopyDownMap = this.getCopyDownDataMap("derivative", "Sample", string, copyDownColumns);
                        trackitemCopyDownMap = this.getCopyDownDataMap("derivative", "TrackItemSDC", string, null);
                    }
                } else if (MODE_CHILD.equals(mode)) {
                    childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                    sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", string, copyDownColumns);
                    trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", string, null);
                }
                if (childSampleTypeid == null || childSampleTypeid.trim().equals("-1")) {
                    childSampleTypeid = "";
                }
                childSampleDataSet.setString(row, "sampletypeid", childSampleTypeid);
                if (sampleCopyDownMap != null) {
                    for (String columnid : sampleCopyDownMap.keySet()) {
                        if ("sampletypeid".equalsIgnoreCase(columnid)) {
                            if (!OpalUtil.isEmpty(childSampleTypeid)) continue;
                            childSampleDataSet.setString(row, columnid, sampleCopyDownMap.get(columnid));
                            continue;
                        }
                        childSampleDataSet.setString(row, columnid, sampleCopyDownMap.get(columnid));
                    }
                }
                if (trackitemCopyDownMap != null) {
                    for (String columnid : trackitemCopyDownMap.keySet()) {
                        childSampleDataSet.setString(row, "__trackitem_" + columnid, trackitemCopyDownMap.get(columnid));
                    }
                }
                if (childInDifferentStudy && familyCopyDownMap != null) {
                    for (String columnid : familyCopyDownMap.keySet()) {
                        childSampleDataSet.setString(row, "__samplefamily_" + columnid, familyCopyDownMap.get(columnid));
                    }
                }
                if (childColumnDataset.size() > 0) {
                    for (int col = 0; col < childColumnDataset.getColumnCount(); ++col) {
                        String columnid;
                        columnid = childColumnDataset.getColumnId(col);
                        childSampleDataSet.setString(row, columnid, childColumnDataset.getString(childSampleIndex, columnid, ""));
                    }
                }
                if ("D".equals(this.accesscontrolledflag)) {
                    childSampleDataSet.setString(row, "securityuser", parentMap2.get("securityuser") != null ? (String)parentMap2.get("securityuser") : "");
                    childSampleDataSet.setString(row, "securitydepartment", parentMap2.get("securitydepartment") != null ? (String)parentMap2.get("securitydepartment") : "");
                }
                if ("S".equals(this.accesscontrolledflag)) {
                    childSampleDataSet.setString(row, "securityset", parentMap2.get("securityset") != null ? (String)parentMap2.get("securityset") : "");
                }
                if (MODE_ALIQUOT.equals(mode) || MODE_DERIVATIVE.equals(mode)) {
                    String childFreezeThawFlag;
                    String string9 = childFreezeThawFlag = child_ftflag.length > childSampleIndex && child_ftflag[childSampleIndex] != null ? child_ftflag[childSampleIndex] : "";
                    if ("Y".equals(childFreezeThawFlag)) {
                        childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcount", child_ftcount.length > childSampleIndex && child_ftcount[childSampleIndex] != null ? child_ftcount[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", child_ftwarn.length > childSampleIndex && child_ftwarn[childSampleIndex] != null ? child_ftwarn[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", child_ftmax.length > childSampleIndex && child_ftmax[childSampleIndex] != null ? child_ftmax[childSampleIndex] : "");
                    } else if ("Y".equals(parentMap2.get("freezethawflag"))) {
                        PropertyList freezethawprops = new PropertyList();
                        if (MODE_DERIVATIVE.equals(mode)) {
                            freezethawprops = freezethawcopydown.getPropertyListNotNull("derivative");
                        } else if (MODE_ALIQUOT.equals(mode)) {
                            freezethawprops = freezethawcopydown.getPropertyListNotNull("aliquot");
                        }
                        if ("Y".equals(freezethawprops.getProperty("freezethawcount"))) {
                            childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                            childSampleDataSet.setString(row, "__trackitem_freezethawcount", (String)parentMap2.get("freezethawcount"));
                            String freezethawwarn = freezethawprops.getProperty("freezethawwarn");
                            if ("Parent".equals(freezethawwarn)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", (String)parentMap2.get("freezethawcountwarn"));
                            } else if ("Sample Type".equals(freezethawwarn)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", this.getSampleTypeFreezeThawWarn(childSampleTypeid));
                            }
                            String freezethawmax = freezethawprops.getProperty("freezethawmax");
                            if ("Parent".equals(freezethawmax)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", (String)parentMap2.get("freezethawcountmax"));
                            } else if ("Sample Type".equals(freezethawmax)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", this.getSampleTypeFreezeThawMax(childSampleTypeid));
                            }
                        }
                    }
                }
                this.copyValuesFromActionInput(childSampleIndex, childSampleDataSet, row, actionProps);
                ++childSampleIndex;
            }
        } else {
            List<String> diluentVolumeList = OpalUtil.toList(actionProps.getProperty("child_diluentvolume", ""), "|");
            int count = 0;
            if (child_templateid.length > 1) {
                String first = child_templateid[0];
                for (i = 1; i < child_templateid.length; ++i) {
                    if (child_templateid[i].equals(first)) continue;
                    ++count;
                    break;
                }
            }
            if (count == 0) {
                expandCopies = true;
            }
            int index = 0;
            boolean setSDIWorkItem = sdiworkitemid.length == 1 || sdiworkitemid.length == parent_sampleid.length;
            for (String parentsampleid : parent_sampleid) {
                if (StringUtil.getLen(parentsampleid) > 0L) {
                    List<String> childDiluentVolume = null;
                    if (diluentVolumeList.size() > 0 && diluentVolumeList.size() == parent_sampleid.length) {
                        childDiluentVolume = OpalUtil.toList(diluentVolumeList.get(index), ";");
                    }
                    int childSampleIndex = 0;
                    for (String childcopies : child_copies) {
                        String columnid;
                        Iterator<String> sourcesdiworkitemid;
                        String childSampleTypeid;
                        Object childStorageStatus;
                        String childSampleStatus;
                        String parentStorageStatus;
                        String parentSampleStatus;
                        Map<String, String> familyCopyDownMap;
                        Map<String, String> trackitemCopyDownMap;
                        Map<String, String> sampleCopyDownMap;
                        int n2;
                        try {
                            n2 = Integer.parseInt(childcopies);
                            if (n2 <= 0) {
                            }
                        }
                        catch (NumberFormatException e) {}
                        continue;
                        if (expandCopies) {
                            boolean childInDifferentStudy = false;
                            sampleCopyDownMap = null;
                            trackitemCopyDownMap = null;
                            familyCopyDownMap = null;
                            HashMap parentMap3 = (HashMap)parentDataMap.get(parentsampleid);
                            parentSampleStatus = (String)parentMap3.get("samplestatus");
                            parentStorageStatus = (String)parentMap3.get("storagestatus");
                            String string = childSampleStatus = "Initial".equals(parentSampleStatus) ? "Initial" : "Received";
                            if (child_samplestatus != null && child_samplestatus.length > childSampleIndex && StringUtil.getLen(child_samplestatus[childSampleIndex]) > 0L) {
                                childSampleStatus = "inherit".equalsIgnoreCase(child_samplestatus[childSampleIndex]) ? parentSampleStatus : child_samplestatus[childSampleIndex];
                            }
                            childStorageStatus = policyChildStorageStatus;
                            if (child_storagestatus != null && child_storagestatus.length > childSampleIndex && StringUtil.getLen(child_storagestatus[childSampleIndex]) > 0L) {
                                childStorageStatus = "inherit".equalsIgnoreCase(child_storagestatus[childSampleIndex]) ? parentStorageStatus : child_storagestatus[childSampleIndex];
                            }
                            childSampleTypeid = "";
                            if (MODE_ALIQUOT.equals(mode)) {
                                childSampleTypeid = (String)parentMap3.get("sampletypeid");
                                if (childInDifferentStudy) {
                                    sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", parentsampleid, copyDownColumns);
                                    trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentsampleid, null);
                                    familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentsampleid, null);
                                } else {
                                    sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", parentsampleid, copyDownColumns);
                                    trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", parentsampleid, null);
                                }
                            } else if (MODE_DERIVATIVE.equals(mode)) {
                                String string10 = childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                                if (childInDifferentStudy) {
                                    sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", parentsampleid, copyDownColumns);
                                    trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentsampleid, null);
                                    familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentsampleid, null);
                                } else {
                                    sampleCopyDownMap = this.getCopyDownDataMap("derivative", "Sample", parentsampleid, copyDownColumns);
                                    trackitemCopyDownMap = this.getCopyDownDataMap("derivative", "TrackItemSDC", parentsampleid, null);
                                }
                            } else if (MODE_CHILD.equals(mode)) {
                                childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                                sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", parentsampleid, copyDownColumns);
                                trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", parentsampleid, null);
                            }
                            if (childSampleTypeid == null || childSampleTypeid.trim().equals("-1")) {
                                childSampleTypeid = "";
                            }
                            String qtycurrent = child_quantity.length > childSampleIndex && child_quantity[childSampleIndex] != null ? child_quantity[childSampleIndex] : "";
                            String qtycurrentunits = child_unit.length > childSampleIndex && child_unit[childSampleIndex] != null ? child_unit[childSampleIndex] : "";
                            String childContainerType = child_containertypeid.length > childSampleIndex && child_containertypeid[childSampleIndex] != null ? child_containertypeid[childSampleIndex] : "";
                            String childTemplateID = child_templateid.length > childSampleIndex && child_templateid[childSampleIndex] != null && !"-1".equals(child_templateid[childSampleIndex]) ? child_templateid[childSampleIndex] : "";
                            String childPreptypeID = child_preptypeid.length > childSampleIndex && child_preptypeid[childSampleIndex] != null && !"-1".equals(child_preptypeid[childSampleIndex]) ? child_preptypeid[childSampleIndex] : "";
                            String childTreatmentID = child_treatmentid.length > childSampleIndex && child_treatmentid[childSampleIndex] != null && !"-1".equals(child_treatmentid[childSampleIndex]) ? child_treatmentid[childSampleIndex] : "";
                            String diluentVolume = childDiluentVolume != null && childDiluentVolume.size() > childSampleIndex ? childDiluentVolume.get(childSampleIndex) : "";
                            String childFreezeThawFlag = child_ftflag.length > childSampleIndex && child_ftflag[childSampleIndex] != null ? child_ftflag[childSampleIndex] : "";
                            String childFTCount = child_ftcount.length > childSampleIndex && child_ftcount[childSampleIndex] != null ? child_ftcount[childSampleIndex] : "";
                            String childFTCountWarn = child_ftwarn.length > childSampleIndex && child_ftwarn[childSampleIndex] != null ? child_ftwarn[childSampleIndex] : "";
                            String childFTCountMax = child_ftmax.length > childSampleIndex && child_ftmax[childSampleIndex] != null ? child_ftmax[childSampleIndex] : "";
                            for (int i3 = 0; i3 < n2; ++i3) {
                                String columnid2;
                                Iterator<String> sourcesdiworkitemid2;
                                int row = childSampleDataSet.addRow();
                                String childstudyid = (String)parentMap3.get("sstudyid");
                                String childsamplefamilyid = (String)parentMap3.get("samplefamilyid");
                                if (input_childstudyid.length() > 0) {
                                    String _childstudyid;
                                    String string11 = _childstudyid = child_studyid.length > childSampleIndex ? child_studyid[childSampleIndex] : "";
                                    if (!OpalUtil.isEmpty(_childstudyid)) {
                                        childstudyid = _childstudyid;
                                        childsamplefamilyid = "";
                                        childInDifferentStudy = true;
                                        String parentsamplefamilyid = (String)parentMap3.get("samplefamilyid");
                                        this.addOrUpdateSampleFamilyParticipant(actionProps, parentsamplefamilyid, childstudyid, childSampleDataSet, row);
                                    }
                                }
                                childSampleDataSet.setString(row, "sstudyid", childstudyid);
                                if (!OpalUtil.isEmpty(childsamplefamilyid)) {
                                    childSampleDataSet.setString(row, "samplefamilyid", childsamplefamilyid);
                                }
                                childSampleDataSet.setString(row, "parentsampleid", parentsampleid);
                                childSampleDataSet.setString(row, "parent_sampleid", parentsampleid);
                                childSampleDataSet.setString(row, "copies", childcopies);
                                childSampleDataSet.setString(row, "samplestatus", childSampleStatus);
                                childSampleDataSet.setString(row, "storagestatus", (String)childStorageStatus);
                                if (!"Allocated".equals(childStorageStatus) && !"In Prep".equals(childStorageStatus)) {
                                    childSampleDataSet.setString(row, "confirmeddt", "n");
                                    childSampleDataSet.setString(row, "confirmedby", sysuserid);
                                }
                                childSampleDataSet.setString(row, "glpflag", parentMap3.get("glpflag") != null ? (String)parentMap3.get("glpflag") : "");
                                childSampleDataSet.setString(row, "cocflag", parentMap3.get("cocflag") != null ? (String)parentMap3.get("cocflag") : "");
                                childSampleDataSet.setString(row, "restrictionsflag", parentMap3.get("restrictionsflag") != null ? (String)parentMap3.get("restrictionsflag") : "");
                                childSampleDataSet.setString(row, "templateid", childTemplateID);
                                childSampleDataSet.setString(row, "applyworkitems", "Y");
                                childSampleDataSet.setString(row, "__trackitem_qtycurrent", qtycurrent);
                                childSampleDataSet.setString(row, "__trackitem_qtyunits", qtycurrentunits);
                                childSampleDataSet.setString(row, "__trackitem_containertypeid", childContainerType);
                                childSampleDataSet.setString(row, "sampletypeid", childSampleTypeid);
                                if (MODE_ALIQUOT.equals(mode)) {
                                    if (childDiluentVolume != null && childDiluentVolume.size() > childSampleIndex) {
                                        childSampleDataSet.setString(row, "__trackitem_diluentvolume", diluentVolume);
                                        childSampleDataSet.setString(row, "__trackitem_diluentvolumeunits", childSampleDataSet.getString(row, "__trackitem_qtyunits", ""));
                                    }
                                } else if (MODE_DERIVATIVE.equals(mode)) {
                                    childSampleDataSet.setString(row, "preptypeid", childPreptypeID);
                                    childSampleDataSet.setString(row, "child_treatmentid", childTreatmentID);
                                }
                                if (setSDIWorkItem) {
                                    Iterator<String> iterator = sourcesdiworkitemid2 = sdiworkitemid.length > index ? sdiworkitemid[index] : sdiworkitemid[0];
                                    if (StringUtil.getLen(sourcesdiworkitemid2) > 0L) {
                                        childSampleDataSet.setString(row, "sourcesdiworkitemid", (String)((Object)sourcesdiworkitemid2));
                                    }
                                }
                                if (sampleCopyDownMap != null) {
                                    sourcesdiworkitemid2 = sampleCopyDownMap.keySet().iterator();
                                    while (sourcesdiworkitemid2.hasNext()) {
                                        String columnid22 = sourcesdiworkitemid2.next();
                                        if ("sampletypeid".equalsIgnoreCase(columnid22)) {
                                            if (!OpalUtil.isEmpty(childSampleTypeid)) continue;
                                            childSampleDataSet.setString(row, columnid22, sampleCopyDownMap.get(columnid22));
                                            continue;
                                        }
                                        childSampleDataSet.setString(row, columnid22, sampleCopyDownMap.get(columnid22));
                                    }
                                }
                                if (trackitemCopyDownMap != null) {
                                    sourcesdiworkitemid2 = trackitemCopyDownMap.keySet().iterator();
                                    while (sourcesdiworkitemid2.hasNext()) {
                                        columnid2 = sourcesdiworkitemid2.next();
                                        childSampleDataSet.setString(row, "__trackitem_" + columnid2, trackitemCopyDownMap.get(columnid2));
                                    }
                                }
                                if (childInDifferentStudy && familyCopyDownMap != null) {
                                    sourcesdiworkitemid2 = familyCopyDownMap.keySet().iterator();
                                    while (sourcesdiworkitemid2.hasNext()) {
                                        columnid2 = sourcesdiworkitemid2.next();
                                        childSampleDataSet.setString(row, "__samplefamily_" + columnid2, familyCopyDownMap.get(columnid2));
                                    }
                                }
                                if (childColumnDataset.size() > 0) {
                                    for (int col = 0; col < childColumnDataset.getColumnCount(); ++col) {
                                        columnid2 = childColumnDataset.getColumnId(col);
                                        childSampleDataSet.setString(row, columnid2, childColumnDataset.getString(childSampleIndex, columnid2, ""));
                                    }
                                }
                                if ("D".equals(this.accesscontrolledflag)) {
                                    childSampleDataSet.setString(row, "securityuser", parentMap3.get("securityuser") != null ? (String)parentMap3.get("securityuser") : "");
                                    childSampleDataSet.setString(row, "securitydepartment", parentMap3.get("securitydepartment") != null ? (String)parentMap3.get("securitydepartment") : "");
                                }
                                if ("S".equals(this.accesscontrolledflag)) {
                                    childSampleDataSet.setString(row, "securityset", parentMap3.get("securityset") != null ? (String)parentMap3.get("securityset") : "");
                                }
                                if (MODE_ALIQUOT.equals(mode) || MODE_DERIVATIVE.equals(mode)) {
                                    if ("Y".equals(childFreezeThawFlag)) {
                                        childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcount", childFTCount);
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", childFTCountWarn);
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", childFTCountMax);
                                    } else if ("Y".equals(parentMap3.get("freezethawflag"))) {
                                        PropertyList freezethawprops = new PropertyList();
                                        if (MODE_DERIVATIVE.equals(mode)) {
                                            freezethawprops = freezethawcopydown.getPropertyListNotNull("derivative");
                                        } else if (MODE_ALIQUOT.equals(mode)) {
                                            freezethawprops = freezethawcopydown.getPropertyListNotNull("aliquot");
                                        }
                                        if ("Y".equals(freezethawprops.getProperty("freezethawcount"))) {
                                            childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                                            childSampleDataSet.setString(row, "__trackitem_freezethawcount", (String)parentMap3.get("freezethawcount"));
                                            String freezethawwarn = freezethawprops.getProperty("freezethawwarn");
                                            if ("Parent".equals(freezethawwarn)) {
                                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", (String)parentMap3.get("freezethawcountwarn"));
                                            } else if ("Sample Type".equals(freezethawwarn)) {
                                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", this.getSampleTypeFreezeThawWarn(childSampleTypeid));
                                            }
                                            String freezethawmax = freezethawprops.getProperty("freezethawmax");
                                            if ("Parent".equals(freezethawmax)) {
                                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", (String)parentMap3.get("freezethawcountmax"));
                                            } else if ("Sample Type".equals(freezethawmax)) {
                                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", this.getSampleTypeFreezeThawMax(childSampleTypeid));
                                            }
                                        }
                                    }
                                }
                                this.copyValuesFromActionInput(childSampleIndex, childSampleDataSet, row, actionProps);
                            }
                            ++childSampleIndex;
                            continue;
                        }
                        boolean childInDifferentStudy = false;
                        sampleCopyDownMap = null;
                        trackitemCopyDownMap = null;
                        familyCopyDownMap = null;
                        HashMap parentMap2 = (HashMap)parentDataMap.get(parentsampleid);
                        parentSampleStatus = (String)parentMap2.get("samplestatus");
                        parentStorageStatus = (String)parentMap2.get("storagestatus");
                        String string = childSampleStatus = "Initial".equals(parentSampleStatus) ? "Initial" : "Received";
                        if (child_samplestatus != null && child_samplestatus.length > childSampleIndex && StringUtil.getLen(child_samplestatus[childSampleIndex]) > 0L) {
                            childSampleStatus = "inherit".equalsIgnoreCase(child_samplestatus[childSampleIndex]) ? parentSampleStatus : child_samplestatus[childSampleIndex];
                        }
                        childStorageStatus = policyChildStorageStatus;
                        if (child_storagestatus != null && child_storagestatus.length > childSampleIndex && StringUtil.getLen(child_storagestatus[childSampleIndex]) > 0L) {
                            childStorageStatus = "inherit".equalsIgnoreCase(child_storagestatus[childSampleIndex]) ? parentStorageStatus : child_storagestatus[childSampleIndex];
                        }
                        childSampleTypeid = "";
                        int row = childSampleDataSet.addRow();
                        String childstudyid = (String)parentMap2.get("sstudyid");
                        String childsamplefamilyid = (String)parentMap2.get("samplefamilyid");
                        if (input_childstudyid.length() > 0) {
                            String _childstudyid;
                            String string12 = _childstudyid = child_studyid.length > childSampleIndex ? child_studyid[childSampleIndex] : "";
                            if (!OpalUtil.isEmpty(_childstudyid)) {
                                childstudyid = _childstudyid;
                                childsamplefamilyid = "";
                                childInDifferentStudy = true;
                                String parentsamplefamilyid = (String)parentMap2.get("samplefamilyid");
                                this.addOrUpdateSampleFamilyParticipant(actionProps, parentsamplefamilyid, childstudyid, childSampleDataSet, row);
                            }
                        }
                        childSampleDataSet.setString(row, "sstudyid", childstudyid);
                        if (!OpalUtil.isEmpty(childsamplefamilyid)) {
                            childSampleDataSet.setString(row, "samplefamilyid", childsamplefamilyid);
                        }
                        childSampleDataSet.setString(row, "parentsampleid", parentsampleid);
                        childSampleDataSet.setString(row, "parent_sampleid", parentsampleid);
                        childSampleDataSet.setString(row, "copies", childcopies);
                        childSampleDataSet.setString(row, "samplestatus", childSampleStatus);
                        childSampleDataSet.setString(row, "storagestatus", (String)childStorageStatus);
                        if (!"Allocated".equals(childStorageStatus) && !"In Prep".equals(childStorageStatus)) {
                            childSampleDataSet.setString(row, "confirmeddt", "n");
                            childSampleDataSet.setString(row, "confirmedby", sysuserid);
                        }
                        childSampleDataSet.setString(row, "glpflag", parentMap2.get("glpflag") != null ? (String)parentMap2.get("glpflag") : "");
                        childSampleDataSet.setString(row, "cocflag", parentMap2.get("cocflag") != null ? (String)parentMap2.get("cocflag") : "");
                        childSampleDataSet.setString(row, "restrictionsflag", parentMap2.get("restrictionsflag") != null ? (String)parentMap2.get("restrictionsflag") : "");
                        childSampleDataSet.setString(row, "templateid", child_templateid.length > childSampleIndex && child_templateid[childSampleIndex] != null && !"-1".equals(child_templateid[childSampleIndex]) ? child_templateid[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "applyworkitems", "Y");
                        childSampleDataSet.setString(row, "__trackitem_qtycurrent", child_quantity.length > childSampleIndex && child_quantity[childSampleIndex] != null ? child_quantity[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_qtyunits", child_unit.length > childSampleIndex && child_unit[childSampleIndex] != null ? child_unit[childSampleIndex] : "");
                        childSampleDataSet.setString(row, "__trackitem_containertypeid", child_containertypeid.length > childSampleIndex && child_containertypeid[childSampleIndex] != null ? child_containertypeid[childSampleIndex] : "");
                        if (MODE_ALIQUOT.equals(mode)) {
                            childSampleTypeid = (String)parentMap2.get("sampletypeid");
                            if (childInDifferentStudy) {
                                sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", parentsampleid, copyDownColumns);
                                trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentsampleid, null);
                                familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentsampleid, null);
                            } else {
                                sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", parentsampleid, copyDownColumns);
                                trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", parentsampleid, null);
                            }
                            if (childDiluentVolume != null && childDiluentVolume.size() > childSampleIndex) {
                                childSampleDataSet.setString(row, "__trackitem_diluentvolume", childDiluentVolume.get(childSampleIndex));
                                childSampleDataSet.setString(row, "__trackitem_diluentvolumeunits", childSampleDataSet.getString(row, "__trackitem_qtyunits", ""));
                            }
                        } else if (MODE_DERIVATIVE.equals(mode)) {
                            childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                            childSampleDataSet.setString(row, "preptypeid", child_preptypeid.length > childSampleIndex && child_preptypeid[childSampleIndex] != null && !"-1".equals(child_preptypeid[childSampleIndex]) ? child_preptypeid[childSampleIndex] : "");
                            childSampleDataSet.setString(row, "child_treatmentid", child_treatmentid.length > childSampleIndex && child_treatmentid[childSampleIndex] != null && !"-1".equals(child_treatmentid[childSampleIndex]) ? child_treatmentid[childSampleIndex] : "");
                            if (childInDifferentStudy) {
                                sampleCopyDownMap = this.getCopyDownDataMap("childstudy", "Sample", parentsampleid, copyDownColumns);
                                trackitemCopyDownMap = this.getCopyDownDataMap("childstudy", "TrackItemSDC", parentsampleid, null);
                                familyCopyDownMap = this.getCopyDownDataMap("childstudy", "LV_SampleFamily", parentsampleid, null);
                            } else {
                                sampleCopyDownMap = this.getCopyDownDataMap("derivative", "Sample", parentsampleid, copyDownColumns);
                                trackitemCopyDownMap = this.getCopyDownDataMap("derivative", "TrackItemSDC", parentsampleid, null);
                            }
                        } else if (MODE_CHILD.equals(mode)) {
                            childSampleTypeid = child_sampletypeid.length > childSampleIndex && child_sampletypeid[childSampleIndex] != null && !"-1".equals(child_sampletypeid[childSampleIndex]) ? child_sampletypeid[childSampleIndex] : "";
                            sampleCopyDownMap = this.getCopyDownDataMap("aliquot", "Sample", parentsampleid, copyDownColumns);
                            trackitemCopyDownMap = this.getCopyDownDataMap("aliquot", "TrackItemSDC", parentsampleid, null);
                        }
                        if (childSampleTypeid == null || childSampleTypeid.trim().equals("-1")) {
                            childSampleTypeid = "";
                        }
                        childSampleDataSet.setString(row, "sampletypeid", childSampleTypeid);
                        if (setSDIWorkItem) {
                            Iterator<String> iterator = sourcesdiworkitemid = sdiworkitemid.length > index ? sdiworkitemid[index] : sdiworkitemid[0];
                            if (StringUtil.getLen(sourcesdiworkitemid) > 0L) {
                                childSampleDataSet.setString(row, "sourcesdiworkitemid", (String)((Object)sourcesdiworkitemid));
                            }
                        }
                        if (sampleCopyDownMap != null) {
                            sourcesdiworkitemid = sampleCopyDownMap.keySet().iterator();
                            while (sourcesdiworkitemid.hasNext()) {
                                columnid = sourcesdiworkitemid.next();
                                if ("sampletypeid".equalsIgnoreCase(columnid)) {
                                    if (!OpalUtil.isEmpty(childSampleTypeid)) continue;
                                    childSampleDataSet.setString(row, columnid, sampleCopyDownMap.get(columnid));
                                    continue;
                                }
                                childSampleDataSet.setString(row, columnid, sampleCopyDownMap.get(columnid));
                            }
                        }
                        if (trackitemCopyDownMap != null) {
                            sourcesdiworkitemid = trackitemCopyDownMap.keySet().iterator();
                            while (sourcesdiworkitemid.hasNext()) {
                                columnid = sourcesdiworkitemid.next();
                                childSampleDataSet.setString(row, "__trackitem_" + columnid, trackitemCopyDownMap.get(columnid));
                            }
                        }
                        if (childInDifferentStudy && familyCopyDownMap != null) {
                            sourcesdiworkitemid = familyCopyDownMap.keySet().iterator();
                            while (sourcesdiworkitemid.hasNext()) {
                                columnid = sourcesdiworkitemid.next();
                                childSampleDataSet.setString(row, "__samplefamily_" + columnid, familyCopyDownMap.get(columnid));
                            }
                        }
                        if (childColumnDataset.size() > 0) {
                            for (int col = 0; col < childColumnDataset.getColumnCount(); ++col) {
                                columnid = childColumnDataset.getColumnId(col);
                                childSampleDataSet.setString(row, columnid, childColumnDataset.getString(childSampleIndex, columnid, ""));
                            }
                        }
                        if ("D".equals(this.accesscontrolledflag)) {
                            childSampleDataSet.setString(row, "securityuser", parentMap2.get("securityuser") != null ? (String)parentMap2.get("securityuser") : "");
                            childSampleDataSet.setString(row, "securitydepartment", parentMap2.get("securitydepartment") != null ? (String)parentMap2.get("securitydepartment") : "");
                        }
                        if ("S".equals(this.accesscontrolledflag)) {
                            childSampleDataSet.setString(row, "securityset", parentMap2.get("securityset") != null ? (String)parentMap2.get("securityset") : "");
                        }
                        if (MODE_ALIQUOT.equals(mode) || MODE_DERIVATIVE.equals(mode)) {
                            String childFreezeThawFlag;
                            String string13 = childFreezeThawFlag = child_ftflag.length > childSampleIndex && child_ftflag[childSampleIndex] != null ? child_ftflag[childSampleIndex] : "";
                            if ("Y".equals(childFreezeThawFlag)) {
                                childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                                childSampleDataSet.setString(row, "__trackitem_freezethawcount", child_ftcount.length > childSampleIndex && child_ftcount[childSampleIndex] != null ? child_ftcount[childSampleIndex] : "");
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", child_ftwarn.length > childSampleIndex && child_ftwarn[childSampleIndex] != null ? child_ftwarn[childSampleIndex] : "");
                                childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", child_ftmax.length > childSampleIndex && child_ftmax[childSampleIndex] != null ? child_ftmax[childSampleIndex] : "");
                            } else if ("Y".equals(parentMap2.get("freezethawflag"))) {
                                PropertyList freezethawprops = new PropertyList();
                                if (MODE_DERIVATIVE.equals(mode)) {
                                    freezethawprops = freezethawcopydown.getPropertyListNotNull("derivative");
                                } else if (MODE_ALIQUOT.equals(mode)) {
                                    freezethawprops = freezethawcopydown.getPropertyListNotNull("aliquot");
                                }
                                if ("Y".equals(freezethawprops.getProperty("freezethawcount"))) {
                                    childSampleDataSet.setString(row, "__trackitem_freezethawflag", "Y");
                                    childSampleDataSet.setString(row, "__trackitem_freezethawcount", (String)parentMap2.get("freezethawcount"));
                                    String freezethawwarn = freezethawprops.getProperty("freezethawwarn");
                                    if ("Parent".equals(freezethawwarn)) {
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", (String)parentMap2.get("freezethawcountwarn"));
                                    } else if ("Sample Type".equals(freezethawwarn)) {
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountwarn", this.getSampleTypeFreezeThawWarn(childSampleTypeid));
                                    }
                                    String freezethawmax = freezethawprops.getProperty("freezethawmax");
                                    if ("Parent".equals(freezethawmax)) {
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", (String)parentMap2.get("freezethawcountmax"));
                                    } else if ("Sample Type".equals(freezethawmax)) {
                                        childSampleDataSet.setString(row, "__trackitem_freezethawcountmax", this.getSampleTypeFreezeThawMax(childSampleTypeid));
                                    }
                                }
                            }
                        }
                        this.copyValuesFromActionInput(childSampleIndex, childSampleDataSet, row, actionProps);
                        ++childSampleIndex;
                    }
                }
                ++index;
            }
        }
        boolean optimizechildsamplecreation = "Y".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("optimizechildsamplecreation", "Y"));
        DataSet sampleMapDataSet = new DataSet();
        if (!isChildSampleIDPassedIn) {
            ArrayList<String> updateWorkItemList = new ArrayList<String>();
            if (!optimizechildsamplecreation) {
                for (i = 0; i < childSampleDataSet.size(); ++i) {
                    DataSet dataSet = new DataSet();
                    String sourceWorkItemID = "";
                    String parentsampleid = childSampleDataSet.getString(i, "parentsampleid");
                    parentMap = (HashMap)parentDataMap.get(parentsampleid);
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    for (int col = 0; col < childSampleDataSet.getColumnCount(); ++col) {
                        String columnid = childSampleDataSet.getColumnId(col);
                        String string = childSampleDataSet.getValue(i, columnid);
                        props.setProperty(columnid, string);
                        if (!"sourcesdiworkitemid".equals(columnid) || !OpalUtil.isNotEmpty(string)) continue;
                        sourceWorkItemID = string;
                    }
                    String storagestatus = childSampleDataSet.getString(i, "storagestatus");
                    if ("Allocated".equals(storagestatus)) {
                        props.setProperty("__trackitem_custodialdepartmentid", "");
                        props.setProperty("__trackitem_custodialuserid", "");
                    } else if ("In Prep".equals(storagestatus)) {
                        props.setProperty("__trackitem_custodialdepartmentid", (String)parentMap.get("custodialdepartmentid"));
                        props.setProperty("__trackitem_custodialuserid", "");
                    } else {
                        props.setProperty("__trackitem_custodialdepartmentid", (String)parentMap.get("custodialdepartmentid"));
                        props.setProperty("__trackitem_custodialuserid", (String)parentMap.get("custodialuserid"));
                        if (OpalUtil.isNotEmpty(sourceWorkItemID)) {
                            updateWorkItemList.add(sourceWorkItemID);
                        }
                    }
                    props.setProperty("__sdcruleconfirm", "Y");
                    props.setProperty("__childsampleplanid", actionProps.getProperty("__childsampleplanid", ""));
                    props.setProperty("__childsampleplanversionid", actionProps.getProperty("__childsampleplanversionid", ""));
                    props.setProperty("sdiworkitemcompletionstatus", sdiworkitemcompletionstatus);
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    dataSet.addColumnValues("childsampleid", 0, props.getProperty("newkeyid1"), ";");
                    dataSet.addColumnValues("childtrackitemid", 0, props.getProperty("newtrackitemid"), ";");
                    dataSet.setString(-1, "child_treatmentid", childSampleDataSet.getString(i, "child_treatmentid", ""));
                    dataSet.setString(-1, "parentsampleid", childSampleDataSet.getString(i, "parentsampleid"));
                    for (int j = 0; j < dataSet.size(); ++j) {
                        sampleMapDataSet.copyRow(dataSet, j, 1);
                    }
                    if (props.getProperty("templateid").length() <= 0) continue;
                    this.updateAdditionalChildSampleColumns(i, OpalUtil.toList(props.getProperty("newkeyid1"), ";"), childColumnDataset);
                }
            } else {
                String columnid;
                for (i = 0; i < childSampleDataSet.size(); ++i) {
                    String string = "";
                    String parentsampleid = childSampleDataSet.getString(i, "parentsampleid");
                    HashMap parentMap4 = (HashMap)parentDataMap.get(parentsampleid);
                    for (int col = 0; col < childSampleDataSet.getColumnCount(); ++col) {
                        columnid = childSampleDataSet.getColumnId(col);
                        String value = childSampleDataSet.getValue(i, columnid);
                        if (!"sourcesdiworkitemid".equals(columnid) || !OpalUtil.isNotEmpty(value)) continue;
                        String string14 = value;
                    }
                    String storagestatus = childSampleDataSet.getString(i, "storagestatus");
                    if ("Allocated".equals(storagestatus)) {
                        childSampleDataSet.setString(i, "__trackitem_custodialdepartmentid", "");
                        childSampleDataSet.setString(i, "__trackitem_custodialuserid", "");
                    } else if ("In Prep".equals(storagestatus)) {
                        childSampleDataSet.setString(i, "__trackitem_custodialdepartmentid", (String)parentMap4.get("custodialdepartmentid"));
                        childSampleDataSet.setString(i, "__trackitem_custodialuserid", "");
                    } else {
                        void var60_95;
                        childSampleDataSet.setString(i, "__trackitem_custodialdepartmentid", (String)parentMap4.get("custodialdepartmentid"));
                        childSampleDataSet.setString(i, "__trackitem_custodialuserid", (String)parentMap4.get("custodialuserid"));
                        if (OpalUtil.isNotEmpty((String)var60_95)) {
                            updateWorkItemList.add((String)var60_95);
                        }
                    }
                    childSampleDataSet.setString(i, "__childsampleplanid", actionProps.getProperty("__childsampleplanid", ""));
                    childSampleDataSet.setString(i, "__childsampleplanversionid", actionProps.getProperty("__childsampleplanversionid", ""));
                    childSampleDataSet.setString(i, "sdiworkitemcompletionstatus", sdiworkitemcompletionstatus);
                }
                if (childSampleDataSet.size() > 0) {
                    if (expandCopies) {
                        void var60_102;
                        void var60_100;
                        void var60_98;
                        DataSet tempds = new DataSet();
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        boolean bl = false;
                        while (var60_98 < childSampleDataSet.getColumnCount()) {
                            String columnid3 = childSampleDataSet.getColumnId((int)var60_98);
                            if ("templateid".equals(columnid3)) {
                                String templateid = childSampleDataSet.getString(0, "templateid", "");
                                if (templateid.length() > 0) {
                                    props.setProperty(columnid3, childSampleDataSet.getColumnValues(columnid3, ";"));
                                }
                            } else if ("applyworkitems".equals(columnid3)) {
                                props.setProperty(columnid3, childSampleDataSet.getValue(0, columnid3));
                            } else {
                                props.setProperty(columnid3, childSampleDataSet.getColumnValues(columnid3, ";"));
                            }
                            ++var60_98;
                        }
                        props.setProperty("__sdcruleconfirm", "Y");
                        props.setProperty("copies", String.valueOf(childSampleDataSet.size()));
                        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                        props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                        props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Adding child samples...") + " (" + childSampleDataSet.size() + ")");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        tempds.addColumnValues("childsampleid", 0, props.getProperty("newkeyid1"), ";");
                        tempds.addColumnValues("childtrackitemid", 0, props.getProperty("newtrackitemid"), ";");
                        tempds.addColumnValues("child_treatmentid", 0, childSampleDataSet.getColumnValues("child_treatmentid", ";"), ";");
                        tempds.addColumnValues("parentsampleid", 0, childSampleDataSet.getColumnValues("parentsampleid", ";"), ";");
                        boolean bl2 = false;
                        while (var60_100 < tempds.size()) {
                            sampleMapDataSet.copyRow(tempds, (int)var60_100, 1);
                            ++var60_100;
                        }
                        boolean bl3 = false;
                        while (var60_102 < childSampleDataSet.size()) {
                            if (props.getProperty("templateid").length() > 0) {
                                this.updateAdditionalChildSampleColumns((int)var60_102, OpalUtil.toList(sampleMapDataSet.getString((int)var60_102, "childsampleid"), ";"), childColumnDataset);
                            }
                            ++var60_102;
                        }
                    } else {
                        for (i = 0; i < childSampleDataSet.size(); ++i) {
                            DataSet dataSet = new DataSet();
                            DataSet tempds = new DataSet();
                            int copies = Integer.parseInt(childSampleDataSet.getString(i, "copies"));
                            dataSet.copyRow(childSampleDataSet, i, copies);
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            for (int col = 0; col < dataSet.getColumnCount(); ++col) {
                                columnid = dataSet.getColumnId(col);
                                if ("templateid".equals(columnid)) {
                                    String templateid = dataSet.getString(0, "templateid", "");
                                    if (templateid.length() <= 0) continue;
                                    props.setProperty(columnid, dataSet.getColumnValues(columnid, ";"));
                                    continue;
                                }
                                if ("applyworkitems".equals(columnid)) {
                                    props.setProperty(columnid, dataSet.getValue(0, columnid));
                                    continue;
                                }
                                props.setProperty(columnid, dataSet.getColumnValues(columnid, ";"));
                            }
                            props.setProperty("__sdcruleconfirm", "Y");
                            props.setProperty("copies", String.valueOf(dataSet.size()));
                            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                            tempds.addColumnValues("childsampleid", 0, props.getProperty("newkeyid1"), ";");
                            tempds.addColumnValues("childtrackitemid", 0, props.getProperty("newtrackitemid"), ";");
                            tempds.addColumnValues("child_treatmentid", 0, dataSet.getColumnValues("child_treatmentid", ";"), ";");
                            tempds.addColumnValues("parentsampleid", 0, dataSet.getColumnValues("parentsampleid", ";"), ";");
                            for (int j = 0; j < tempds.size(); ++j) {
                                sampleMapDataSet.copyRow(tempds, j, 1);
                            }
                            if (props.getProperty("templateid").length() <= 0) continue;
                            this.updateAdditionalChildSampleColumns(i, OpalUtil.toList(props.getProperty("newkeyid1"), ";"), childColumnDataset);
                        }
                    }
                }
            }
            if (updateWorkItemList.size() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select keyid1, workitemid, workiteminstance from sdiworkitem where sdiworkitemid in (" + safeSQL.addIn(updateWorkItemList) + ") and sdcid = 'Sample' and workitemstatus != 'Completed'", safeSQL.getValues());
                if (dataSet != null && dataSet.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", dataSet.getColumnValues("keyid1", ";"));
                    props.setProperty("workitemid", dataSet.getColumnValues("workitemid", ";"));
                    props.setProperty("workiteminstance", dataSet.getColumnValues("workiteminstance", ";"));
                    props.setProperty("workitemstatus", "Completed");
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                    this.getActionProcessor().processActionClass(EditSDIWorkItem.class.getName(), props);
                }
            }
        } else {
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select s_sample.s_sampleid, s_sample.storagestatus, (select t.trackitemid from trackitem t where t.linksdcid = 'Sample' and t.linkkeyid1 = s_sample.s_sampleid) trackitemid");
            sql.append(" ,(select count(sm.sourcesampleid) from s_samplemap sm where sm.sourcesampleid = s_sample.s_sampleid or sm.destsampleid = s_sample.s_sampleid) smcount");
            sql.append(" from s_sample");
            sql.append(" where s_sampleid in (").append(safeSQL.addIn(childSampleDataSet.getColumnValues("childsampleid", "','"))).append(")");
            DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (dataSet != null) {
                int col;
                ArrayList<String> existingSampleList = new ArrayList<String>();
                for (int i6 = 0; i6 < dataSet.size(); ++i6) {
                    existingSampleList.add(dataSet.getString(i6, "s_sampleid"));
                    if ("Disposed".equals(dataSet.getString(i6, "storagestatus", ""))) {
                        throw new SapphireException("MultiSampleChild", "VALIDATION", this.getTranslationProcessor().translate("The passed in child sample is disposed") + " [" + dataSet.getString(i6, "s_sampleid") + "]");
                    }
                    if (dataSet.getInt(i6, "smcount") <= 0) continue;
                    throw new SapphireException("MultiSampleChild", "VALIDATION", this.getTranslationProcessor().translate("The passed in child sample is either a child of another sample or have child samples") + " [" + dataSet.getString(i6, "s_sampleid") + "]");
                }
                DataSet newSampleDS = new DataSet();
                DataSet familyds = new DataSet();
                for (int i7 = 0; i7 < childSampleDataSet.size(); ++i7) {
                    if (!existingSampleList.contains(childSampleDataSet.getString(i7, "childsampleid"))) {
                        newSampleDS.copyRow(childSampleDataSet, i7, 1);
                        continue;
                    }
                    familyds.copyRow(childSampleDataSet, i7, 1);
                }
                if (newSampleDS.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("overrideautokey", "Y");
                    for (col = 0; col < newSampleDS.getColumnCount(); ++col) {
                        String columnid = newSampleDS.getColumnId(col);
                        if (columnid.startsWith("trackitem_")) continue;
                        if ("childsampleid".equals(columnid)) {
                            props.setProperty("keyid1", newSampleDS.getColumnValues(columnid, ";"));
                            continue;
                        }
                        props.setProperty(columnid, newSampleDS.getColumnValues(columnid, ";"));
                    }
                    props.setProperty("__childsampleplanid", actionProps.getProperty("__childsampleplanid", ""));
                    props.setProperty("__childsampleplanversionid", actionProps.getProperty("__childsampleplanversionid", ""));
                    props.setProperty("sdiworkitemcompletionstatus", sdiworkitemcompletionstatus);
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                }
                if (familyds.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    for (col = 0; col < familyds.getColumnCount(); ++col) {
                        String columnid = familyds.getColumnId(col);
                        if (columnid.startsWith("trackitem_")) continue;
                        props.setProperty(columnid, familyds.getColumnValues(columnid, ";"));
                    }
                    props.setProperty("keyid1", familyds.getColumnValues("childsampleid", ";"));
                    props.setProperty("samplefamilyid", familyds.getColumnValues("samplefamilyid", ";"));
                    props.setProperty("__sdcruleconfirm", "Y");
                    props.setProperty("__samplePreEditRuleIgnore", "Y");
                    props.setProperty("__samplePostEditRuleIgnore", "Y");
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
                DataSet dataSet2 = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                HashMap<String, String> sampleTrackItemMap = new HashMap<String, String>();
                for (int i5 = 0; i5 < dataSet2.size(); ++i5) {
                    if (dataSet2.getString(i5, "trackitemid", "").length() == 0) {
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", dataSet2.getString(i5, "s_sampleid"));
                        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                        props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                        props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                        this.getActionProcessor().processActionClass(AddTrackItem.class.getName(), props);
                        dataSet2.setString(i5, "trackitemid", props.getProperty("newkeyid1"));
                    }
                    sampleTrackItemMap.put(dataSet2.getString(i5, "s_sampleid"), dataSet2.getString(i5, "trackitemid"));
                }
                for (int i2 = 0; i2 < childSampleDataSet.size(); ++i2) {
                    String string = childSampleDataSet.getString(i2, "parentsampleid");
                    int row = sampleMapDataSet.addRow();
                    String sampleid = childSampleDataSet.getString(i2, "childsampleid");
                    sampleMapDataSet.setString(row, "childsampleid", sampleid);
                    sampleMapDataSet.setString(row, "childtrackitemid", (String)sampleTrackItemMap.get(sampleid));
                    HashMap parentMap5 = (HashMap)parentDataMap.get(string);
                    sampleMapDataSet.setString(row, "child_treatmentid", childSampleDataSet.getString(i2, "child_treatmentid", ""));
                    sampleMapDataSet.setString(row, "parentsampleid", childSampleDataSet.getString(i2, "parentsampleid"));
                    sampleMapDataSet.setString(row, "qtycurrent", childSampleDataSet.getString(i2, "__trackitem_qtycurrent"));
                    sampleMapDataSet.setString(row, "qtyunits", childSampleDataSet.getString(i2, "__trackitem_qtyunits"));
                    String storagestatus = childSampleDataSet.getString(i2, "storagestatus");
                    if ("Allocated".equals(storagestatus)) {
                        sampleMapDataSet.setString(row, "custodialdepartmentid", "");
                        sampleMapDataSet.setString(row, "custodialuserid", "");
                    } else if ("In Prep".equals(storagestatus)) {
                        sampleMapDataSet.setString(row, "custodialdepartmentid", (String)parentMap5.get("custodialdepartmentid"));
                        sampleMapDataSet.setString(row, "custodialuserid", "");
                    } else {
                        sampleMapDataSet.setString(row, "custodialdepartmentid", (String)parentMap5.get("custodialdepartmentid"));
                        sampleMapDataSet.setString(row, "custodialuserid", (String)parentMap5.get("custodialuserid"));
                    }
                    if (!"Y".equals(childSampleDataSet.getString(i2, "__trackitem_freezethawflag", "N"))) continue;
                    sampleMapDataSet.setString(row, "freezethawflag", "Y");
                    sampleMapDataSet.setString(row, "freezethawcount", childSampleDataSet.getString(i2, "__trackitem_freezethawcount", ""));
                    sampleMapDataSet.setString(row, "freezethawcountmax", childSampleDataSet.getString(i2, "__trackitem_freezethawcountmax", ""));
                    sampleMapDataSet.setString(row, "freezethawcountwarn", childSampleDataSet.getString(i2, "__trackitem_freezethawcountwarn", ""));
                }
                props.clear();
                props.setProperty("trackitemid", sampleMapDataSet.getColumnValues("childtrackitemid", ";"));
                props.setProperty("qtycurrent", sampleMapDataSet.getColumnValues("qtycurrent", ";"));
                props.setProperty("qtyunits", sampleMapDataSet.getColumnValues("qtyunits", ";"));
                props.setProperty("freezethawflag", sampleMapDataSet.getColumnValues("freezethawflag", ";"));
                props.setProperty("freezethawcount", sampleMapDataSet.getColumnValues("freezethawcount", ";"));
                props.setProperty("freezethawcountwarn", sampleMapDataSet.getColumnValues("freezethawcountwarn", ";"));
                props.setProperty("freezethawcountmax", sampleMapDataSet.getColumnValues("freezethawcountmax", ";"));
                props.setProperty("custodialdepartmentid", sampleMapDataSet.getColumnValues("custodialdepartmentid", ";"));
                props.setProperty("custodialuserid", sampleMapDataSet.getColumnValues("custodialuserid", ";"));
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
        }
        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Making parent-child connection..."));
        DataSet dsinsert = new DataSet();
        DataSet secondarySecurityDS = new DataSet();
        boolean bl = false;
        while (var60_109 < sampleMapDataSet.size()) {
            String sourcesampleid = sampleMapDataSet.getString((int)var60_109, "parentsampleid");
            String destsampleid = sampleMapDataSet.getString((int)var60_109, "childsampleid");
            boolean insert = true;
            if (isChildSampleIDPassedIn && this.database.getPreparedCount("select count(sourcesampleid) from s_samplemap where sourcesampleid = ? and destsampleid = ?", new Object[]{sourcesampleid, destsampleid}) > 0) {
                insert = false;
            }
            if (insert) {
                int row = dsinsert.addRow();
                dsinsert.setString(row, "sourcesampleid", sourcesampleid);
                dsinsert.setString(row, "destsampleid", destsampleid);
            }
            this.populateSecondarySecurityRow(secondarySecurityDS, secondarySecurityInfo, sourcesampleid, destsampleid, this.accesscontrolledflag);
            ++var60_109;
        }
        String string = actionProps.getProperty("__childsampleplanid");
        String childsampleplanversionid = actionProps.getProperty("__childsampleplanversionid");
        String childsampleplanitemid = actionProps.getProperty("__childsampleplanitemid");
        if (OpalUtil.isNotEmpty(string) && OpalUtil.isNotEmpty(childsampleplanversionid) && OpalUtil.isNotEmpty(childsampleplanitemid)) {
            dsinsert.setString(-1, "childsampleplanid", string);
            dsinsert.setString(-1, "childsampleplanversionid", childsampleplanversionid);
            dsinsert.setString(-1, "childsampleplanitemid", childsampleplanitemid);
        }
        dsinsert.setString(-1, "createby", sysuserid);
        dsinsert.setString(-1, "createtool", "MultiSampleChild");
        dsinsert.setDate(-1, "createdt", now);
        props.clear();
        props.setProperty("sdcid", "Sample");
        props.setProperty("keyid1", dsinsert.getColumnValues("sourcesampleid", ";"));
        props.setProperty("s_childsampleid", dsinsert.getColumnValues("destsampleid", ";"));
        props.setProperty("linkid", "Child Samples");
        props.setProperty("childsampleplanid", dsinsert.getColumnValues("childsampleplanid", ";"));
        props.setProperty("childsampleplanversionid", dsinsert.getColumnValues("childsampleplanversionid", ";"));
        props.setProperty("childsampleplanitemid", dsinsert.getColumnValues("childsampleplanitemid", ";"));
        this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
        this.insertSecondarySecurityDS(secondarySecurityDS, this.accesscontrolledflag);
        if (MODE_DERIVATIVE.equals(mode)) {
            String sequenceid = now.get(2) + "" + now.get(1);
            for (int i9 = 0; i9 < sampleMapDataSet.size(); ++i9) {
                String treatmentid = sampleMapDataSet.getString(i9, "child_treatmentid");
                if (StringUtil.getLen(treatmentid) <= 0L) continue;
                String string15 = sequenceid + "-" + this.getSequenceProcessor().getSequence("s_sampledetail", sequenceid);
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleMapDataSet.getValue(i9, "childsampleid"));
                props.setProperty("linkid", "SampleDetail");
                props.setProperty("s_sampledetailid", string15);
                props.setProperty("detailtype", "Treatment");
                props.setProperty("detailvalue", "Treatment");
                props.setProperty("detailsdcid", "LV_Treatment");
                props.setProperty("detailkeyid1", treatmentid);
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
            }
        }
        if ((parent_depleteflag = StringUtil.split(actionProps.getProperty("parent_depleteflag", "N"), ";")).length != parent_sampleid.length && parent_depleteflag.length == 1) {
            String parent_deplete_flag = parent_depleteflag[0];
            parent_depleteflag = new String[parent_sampleid.length];
            for (int i10 = 0; i10 < parent_sampleid.length; ++i10) {
                parent_depleteflag[i10] = parent_deplete_flag;
            }
        }
        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Editing parent sample data..."));
        ArrayList<String> depleteList = new ArrayList<String>();
        int i11 = 0;
        for (String depleteflag : parent_depleteflag) {
            if ("Y".equals(depleteflag)) {
                depleteList.add(parent_sampleid[i11]);
            }
            ++i11;
        }
        if (depleteList.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", OpalUtil.toDelimitedString(depleteList, ";"));
            props.setProperty("samplestatus", "Disposed");
            props.setProperty("storagestatus", "Disposed");
            props.setProperty("storagedisposalstatus", "Consumed");
            props.setProperty("disposalstatus", "Disposed");
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        boolean bl4 = "Y".equals(actionProps.getProperty("syncparent", "N"));
        String[] parent_quantity = StringUtil.split(actionProps.getProperty("parent_quantity", ""), ";");
        i11 = 0;
        DataSet parentQuantityDataSet = new DataSet();
        for (String parentsampleid : parent_sampleid) {
            if (!depleteList.contains(parentsampleid)) {
                if (bl4) {
                    String trackitemid;
                    String parentQuantity;
                    String string16 = parentQuantity = parent_quantity.length > i11 && parent_quantity[i11] != null ? parent_quantity[i11] : "";
                    if (StringUtil.getLen(parentQuantity) > 0L && OpalUtil.isNotEmpty(trackitemid = (String)((HashMap)parentDataMap.get(parentsampleid)).get("trackitemid"))) {
                        int row = parentQuantityDataSet.addRow();
                        parentQuantityDataSet.setString(row, "trackitemid", trackitemid);
                        parentQuantityDataSet.setString(row, "qtycurrent", parentQuantity);
                    }
                }
            } else {
                String trackitemid = (String)((HashMap)parentDataMap.get(parentsampleid)).get("trackitemid");
                if (OpalUtil.isNotEmpty(trackitemid)) {
                    int row = parentQuantityDataSet.addRow();
                    parentQuantityDataSet.setString(row, "trackitemid", trackitemid);
                    parentQuantityDataSet.setString(row, "qtycurrent", "0");
                }
            }
            ++i11;
        }
        if (parentQuantityDataSet.size() > 0) {
            props.clear();
            props.setProperty("trackitemid", parentQuantityDataSet.getColumnValues("trackitemid", ";"));
            props.setProperty("qtycurrent", parentQuantityDataSet.getColumnValues("qtycurrent", ";"));
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
        }
        this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Adding child sample aliases..."));
        try {
            JSONArray childAliasData;
            String input_aliasdata = actionProps.getProperty("child_aliasdata", "").trim();
            if (OpalUtil.isNotEmpty(input_aliasdata) && (childAliasData = new JSONArray(input_aliasdata)) != null && childAliasData.length() > 0) {
                ArrayList<DataSet> parentDataSet = sampleMapDataSet.getGroupedDataSets("parentsampleid");
                DataSet aliasDS = new DataSet();
                for (DataSet ds : parentDataSet) {
                    if (ds.size() != childAliasData.length()) continue;
                    for (int dsrow = 0; dsrow < ds.size(); ++dsrow) {
                        String[] a2;
                        String[] a1;
                        JSONObject jsonObject = childAliasData.getJSONObject(dsrow);
                        if (jsonObject == null) continue;
                        String aliastype = jsonObject.getString("aliastype");
                        String aliasid = jsonObject.getString("aliasid");
                        if (!OpalUtil.isNotEmpty(aliastype) || !OpalUtil.isNotEmpty(aliasid) || (a1 = StringUtil.split(aliastype, "|")).length != (a2 = StringUtil.split(aliasid, "|")).length) continue;
                        for (int r = 0; r < a1.length; ++r) {
                            if (!OpalUtil.isNotEmpty(a1[r]) || !OpalUtil.isNotEmpty(a2[r])) continue;
                            int aliasrow = aliasDS.addRow();
                            aliasDS.setString(aliasrow, "keyid1", ds.getString(dsrow, "childsampleid"));
                            aliasDS.setString(aliasrow, "aliastype", a1[r]);
                            aliasDS.setString(aliasrow, "aliasid", a2[r]);
                        }
                    }
                }
                if (aliasDS.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", aliasDS.getColumnValues("keyid1", ";"));
                    props.setProperty("aliastype", aliasDS.getColumnValues("aliastype", ";"));
                    props.setProperty("aliasid", aliasDS.getColumnValues("aliasid", ";"));
                    this.getActionProcessor().processActionClass(AddSDIAlias.class.getName(), props);
                }
            }
        }
        catch (JSONException e) {
            this.trackActionProgress(trackProgressID, "||ERROR||" + e.getMessage());
            e.printStackTrace();
        }
        actionProps.setProperty("newkeyid1", sampleMapDataSet.getColumnValues("childsampleid", ";"));
        actionProps.setProperty("newtrackitemid", sampleMapDataSet.getColumnValues("childtrackitemid", ";"));
        actionProps.setProperty("parentsampleid", sampleMapDataSet.getColumnValues("parentsampleid", ";"));
        String newkeyid1 = actionProps.getProperty("newkeyid1");
        if (newkeyid1.length() > 3900) {
            String rsetid = this.getDAMProcessor().createRSet("Sample", newkeyid1, null, null);
            this.trackActionProgress(trackProgressID, "||COMPLETED||RSET||" + rsetid);
        } else {
            this.trackActionProgress(trackProgressID, "||COMPLETED||" + actionProps.getProperty("newkeyid1"));
        }
    }

    private void updateAdditionalChildSampleColumns(int childColumnDataSetRow, List<String> childsampleList, DataSet childColumnDataset) throws SapphireException {
        DataSet childColumnDS;
        if (OpalUtil.isEmpty(childsampleList) || OpalUtil.isEmpty(childColumnDataset)) {
            return;
        }
        DataSet sampleColumnDS = this.getSDCProcessor().getColumnData("Sample");
        HashMap<String, String> valMap = new HashMap<String, String>();
        StringBuilder childcolumnsql = new StringBuilder("select s_sampleid");
        for (int col = 0; col < childColumnDataset.getColumnCount(); ++col) {
            String columnid = childColumnDataset.getColumnId(col);
            if (sampleColumnDS.findRow("columnid", columnid) == -1) continue;
            childcolumnsql.append(",").append(columnid);
            String value = childColumnDataset.getValue(childColumnDataSetRow, columnid);
            valMap.put(columnid, value);
        }
        childcolumnsql.append(" from s_sample");
        if (childsampleList.size() < 1000) {
            SafeSQL safeSQL = new SafeSQL();
            childcolumnsql.append(" where s_sampleid in (").append(safeSQL.addIn(childsampleList)).append(")");
            childColumnDS = this.getQueryProcessor().getPreparedSqlDataSet(childcolumnsql.toString(), safeSQL.getValues());
        } else {
            String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(childsampleList, ";"), null, null);
            childcolumnsql.append(" where s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
            childColumnDS = this.getQueryProcessor().getPreparedSqlDataSet(childcolumnsql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (childColumnDS != null && childColumnDS.size() > 0) {
            DataSet childUpdateDS = new DataSet();
            for (int row = 0; row < childColumnDS.size(); ++row) {
                boolean update = false;
                DataSet tempUpdateDS = new DataSet();
                String childcolumnsampleid = childColumnDS.getString(row, "s_sampleid");
                tempUpdateDS.addRow();
                tempUpdateDS.setString(0, "s_sampleid", childcolumnsampleid);
                for (int col = 0; col < childColumnDS.getColumnCount(); ++col) {
                    String columnid = childColumnDS.getColumnId(col);
                    if ("s_sampleid".equals(columnid)) continue;
                    String childSampleValue = childColumnDS.getValue(row, columnid, "");
                    String userEnteredValue = childColumnDataset.getValue(childColumnDataSetRow, columnid);
                    if (!OpalUtil.isNotEmpty(userEnteredValue) || userEnteredValue.equals(childSampleValue)) continue;
                    tempUpdateDS.setString(0, columnid, userEnteredValue);
                    update = true;
                }
                if (!update) continue;
                childUpdateDS.copyRow(tempUpdateDS, 0, 1);
            }
            if (childUpdateDS.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", childUpdateDS.getColumnValues("s_sampleid", ";"));
                for (int col = 0; col < childUpdateDS.getColumnCount(); ++col) {
                    String columnid = childUpdateDS.getColumnId(col);
                    if ("s_sampleid".equals(columnid)) continue;
                    props.setProperty(columnid, childUpdateDS.getColumnValues(columnid, ";"));
                }
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }

    private void copyValuesFromActionInput(int childSampleIndex, DataSet childSampleDataSet, int datasetRow, PropertyList actionProps) {
        for (Object key : actionProps.keySet()) {
            String property = String.valueOf(key);
            String column = "";
            String columnvalue = "";
            if (property.startsWith("childcolumn_")) {
                column = property.substring(12);
            } else if (property.startsWith("__trackitem_") || property.startsWith("__samplefamily_")) {
                column = property;
            }
            if (!OpalUtil.isNotEmpty(column)) continue;
            String value = actionProps.getProperty(property);
            if (value.contains(";")) {
                String[] valueArray = StringUtil.split(value, ";");
                if (valueArray != null && valueArray.length > childSampleIndex && valueArray[childSampleIndex] != null) {
                    columnvalue = valueArray[childSampleIndex];
                }
            } else {
                columnvalue = value;
            }
            if (!OpalUtil.isNotEmpty(columnvalue)) continue;
            childSampleDataSet.setString(datasetRow, column, columnvalue);
        }
    }

    private void addOrUpdateSampleFamilyParticipant(PropertyList actionProps, String parentsamplefamilyid, String childstudyid, DataSet childSampleDataSet, int row) throws ActionException {
        String parentsubjectid;
        if (OpalUtil.isEmpty(parentsamplefamilyid)) {
            return;
        }
        DataSet parentfamilyds = this.getQueryProcessor().getPreparedSqlDataSet("select subjectid from s_samplefamily where s_samplefamilyid = ?", (Object[])new String[]{parentsamplefamilyid});
        if (parentfamilyds != null && parentfamilyds.size() > 0 && (parentsubjectid = parentfamilyds.getString(0, "subjectid", "")).length() > 0) {
            String participantid = null;
            DataSet participantds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid from s_participant where sstudyid = ? and subjectid = ?", (Object[])new String[]{childstudyid, parentsubjectid});
            if (participantds != null && participantds.size() > 0) {
                participantid = participantds.getString(0, "s_participantid", "");
            }
            if (OpalUtil.isEmpty(participantid)) {
                PropertyList addProps = new PropertyList();
                addProps.setProperty("sdcid", "LV_Participant");
                addProps.setProperty("copies", "1");
                addProps.setProperty("sstudyid", childstudyid);
                addProps.setProperty("subjectid", parentsubjectid);
                addProps.setProperty("participantstatus", "Associated");
                addProps.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                addProps.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                addProps.setProperty("auditreason", actionProps.getProperty("auditreason"));
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), addProps);
                participantid = addProps.getProperty("newkeyid1", "");
            }
            childSampleDataSet.setString(row, "__samplefamily_subjectid", parentsubjectid);
            childSampleDataSet.setString(row, "__samplefamily_participantid", participantid);
            childSampleDataSet.setString(row, "__samplefamily_ignoreparticipant", "Y");
        }
    }

    private Map<String, String> getCopyDownDataMap(String childtype, String sdcid, String parentsampleid, String copyDownColumns) {
        String key = childtype + sdcid + parentsampleid;
        if (!this.copyDownDataCache.containsKey(key)) {
            this.copyDownDataCache.put(key, ChildSampleUtil.getCopyDownValues(this.getConfigurationProcessor(), this.getQueryProcessor(), sdcid, parentsampleid, childtype, copyDownColumns));
        }
        return this.copyDownDataCache.get(key);
    }

    private DataSet getParentSampleData(String parentSampleid) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s.s_sampleid, s.samplefamilyid, s.preptypeid, s.sampletypeid, s.sstudyid, s.concentration, s.concentrationunits, s.glpflag, s.cocflag, s.restrictionsflag,");
        sql.append(" s.storagestatus, s.samplestatus,");
        if ("D".equals(this.accesscontrolledflag)) {
            sql.append("s.securityuser, s.securitydepartment,");
        }
        if ("S".equals(this.accesscontrolledflag)) {
            sql.append("s.securityset,");
        }
        sql.append(" t.trackitemid, t.custodialdepartmentid, t.custodialuserid, t.qtycurrent, t.qtyunits, t.freezethawflag, t.freezethawcount, t.freezethawcountwarn, t.freezethawcountmax");
        sql.append(" from s_sample s left outer join trackitem t on t.linkkeyid1 = s.s_sampleid and t.linksdcid = 'Sample'");
        sql.append(" where s.s_sampleid in ( ").append(safeSQL.addIn(parentSampleid, ";")).append(" )");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private String getSampleTypeFreezeThawWarn(String sampletypeid) {
        if (StringUtil.getLen(sampletypeid) > 0L) {
            if (!this.sampleTypeFTWarnCache.containsKey(sampletypeid)) {
                this.sampleTypeFTWarnCache.put(sampletypeid, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sampletype", "freezethawcountwarn", "s_sampletypeid = ?", new String[]{sampletypeid}));
            }
            return this.sampleTypeFTWarnCache.get(sampletypeid);
        }
        return "";
    }

    private String getSampleTypeFreezeThawMax(String sampletypeid) {
        if (StringUtil.getLen(sampletypeid) > 0L) {
            if (!this.sampleTypeFTMaxCache.containsKey(sampletypeid)) {
                this.sampleTypeFTMaxCache.put(sampletypeid, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sampletype", "freezethawcountmax", "s_sampletypeid = ?", new String[]{sampletypeid}));
            }
            return this.sampleTypeFTMaxCache.get(sampletypeid);
        }
        return "";
    }

    private DataSet getSecondarySecurityInfo(String accessControlFlag, String sampleIds) throws SapphireException {
        DataSet ds = new DataSet();
        if ("S".equalsIgnoreCase(accessControlFlag) || "D".equalsIgnoreCase(accessControlFlag)) {
            if (StringUtil.split(sampleIds, ";").length > 1000) {
                String rsetId = this.getDAMProcessor().createRSet("Sample", sampleIds, "", "");
                String sql = "SELECT * FROM " + ("S".equalsIgnoreCase(accessControlFlag) ? "sdisecurityset" : "sdisecuritydepartment") + " s JOIN rsetitems r ON s.sdcid = 'Sample' AND s.keyid1 = r.keyid1 WHERE r.sdcid = 'Sample' AND r.rsetid = ?";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetId});
            } else {
                String sql = "SELECT * FROM " + ("S".equalsIgnoreCase(accessControlFlag) ? "sdisecurityset" : "sdisecuritydepartment") + " s WHERE s.sdcid = 'Sample' AND s.keyid1 IN ('" + StringUtil.replaceAll(sampleIds, ";", "','") + "')";
                ds = this.getQueryProcessor().getSqlDataSet(sql);
            }
        }
        return ds;
    }

    private void populateSecondarySecurityRow(DataSet secondarySecurityDS, DataSet secondarySecurityInfo, String parentSampleId, String childSampleId, String accessControlFlag) {
        String checkingForCol = "";
        if ("S".equalsIgnoreCase(accessControlFlag)) {
            checkingForCol = "securityset";
        } else if ("D".equalsIgnoreCase(accessControlFlag)) {
            checkingForCol = "securitydepartment";
        } else {
            return;
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("keyid1", parentSampleId);
        DataSet existingRowsParent = secondarySecurityInfo.getFilteredDataSet(filterMap);
        filterMap.clear();
        filterMap.put("keyid1", childSampleId);
        DataSet existingRowsChild = secondarySecurityInfo.getFilteredDataSet(filterMap);
        for (int i = 0; i < existingRowsParent.getRowCount(); ++i) {
            String operationId = existingRowsParent.getString(i, "operationid", "");
            String checkingForColVal = existingRowsParent.getString(i, checkingForCol, "");
            filterMap.clear();
            filterMap.put(operationId, operationId);
            filterMap.put(checkingForCol, checkingForColVal);
            filterMap.put("keyid1", childSampleId);
            if (existingRowsChild.getFilteredDataSet(filterMap).getRowCount() != 0 || secondarySecurityDS.getFilteredDataSet(filterMap).getRowCount() != 0) continue;
            int newRow = secondarySecurityDS.addRow();
            secondarySecurityDS.setString(newRow, "sdcid", "Sample");
            secondarySecurityDS.setString(newRow, "keyid1", childSampleId);
            secondarySecurityDS.setString(newRow, checkingForCol, checkingForColVal);
            secondarySecurityDS.setString(newRow, "operationid", operationId);
        }
    }

    private void insertSecondarySecurityDS(DataSet secondarySecurityDS, String accessControlFlag) throws ActionException {
        if (secondarySecurityDS == null || secondarySecurityDS.getRowCount() == 0) {
            return;
        }
        PropertyList actionProps = new PropertyList();
        if ("S".equalsIgnoreCase(accessControlFlag)) {
            actionProps.setProperty("sdcid", "Sample");
            actionProps.setProperty("keyid1", secondarySecurityDS.getColumnValues("keyid1", ";"));
            actionProps.setProperty("securityset", secondarySecurityDS.getColumnValues("securityset", ";"));
            actionProps.setProperty("operationid", secondarySecurityDS.getColumnValues("operationid", ";"));
            actionProps.setProperty("propsmatch", "Y");
            this.getActionProcessor().processAction("AddSDISecuritySet", "1", actionProps);
        } else if ("D".equalsIgnoreCase(accessControlFlag)) {
            actionProps.setProperty("sdcid", "Sample");
            actionProps.setProperty("keyid1", secondarySecurityDS.getColumnValues("keyid1", ";"));
            actionProps.setProperty("departmentid", secondarySecurityDS.getColumnValues("securitydepartment", ";"));
            actionProps.setProperty("operationid", secondarySecurityDS.getColumnValues("operationid", ";"));
            actionProps.setProperty("propsmatch", "Y");
            this.getActionProcessor().processAction("AddSDISecurityDept", "1", actionProps);
        }
    }

    private void trackActionProgress(String trackProgressID, String message) {
        if (trackProgressID.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("actionprogressid", trackProgressID);
            props.setProperty("message", message);
            try {
                this.getActionProcessor().processActionClass(SetActionProgressStatus.class.getName(), props, true);
            }
            catch (ActionException e) {
                e.printStackTrace();
            }
        }
    }
}

