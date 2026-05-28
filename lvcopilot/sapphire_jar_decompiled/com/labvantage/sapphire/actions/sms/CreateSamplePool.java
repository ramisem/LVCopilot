/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.opal.util.ChildSampleUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAlias;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateSamplePool
extends BaseAction
implements sapphire.action.CreateSamplePool {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103133 $";
    public static final String ID = "CreateSamplePool";
    public static final String VERSION = "1";
    public static final String MODE_BIOBANK = "BioBank";
    public static final String MODE_LIMS = "LIMS";

    /*
     * WARNING - void declaration
     */
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        ErrorHandler errorHandler;
        String parentsampleid = actionProps.getProperty("sampleid");
        String disposesampleid = actionProps.getProperty("disposesampleid");
        String childstudyid = actionProps.getProperty("childstudyid");
        String quantity = actionProps.getProperty("quantity");
        String poolcopies = actionProps.getProperty("poolcopies", VERSION);
        String pooltemplateid = actionProps.getProperty("pooltemplateid");
        String poolquantity = actionProps.getProperty("poolquantity");
        String poolunitid = actionProps.getProperty("poolunitid");
        String poolcontainertypeid = actionProps.getProperty("poolcontainertypeid");
        String poolcustodialdepartmentid = actionProps.getProperty("poolcustodialdepartmentid");
        String poolglpflag = actionProps.getProperty("poolglpflag", "N");
        String mode = actionProps.getProperty("poolmode", MODE_BIOBANK);
        boolean quantitycalculation = MODE_BIOBANK.equals(mode) || "Y".equals(actionProps.getProperty("enablequantitycalculation", "Y"));
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        String auditreason = actionProps.getProperty("auditreason", "Created Pool Sample");
        String auditsignedflag = actionProps.getProperty("auditsignedflag", "");
        String auditactivity = actionProps.getProperty("auditactivity", "");
        PropertyList props = new PropertyList();
        DataSet poolDS = new DataSet();
        DataSet sampleMapInsertDS = new DataSet();
        DataSet poolTrackItemEditDS = new DataSet();
        DataSet parentQuantityDS = new DataSet();
        DataSet additionalColumnDS = new DataSet();
        List<String> parentSampleList = OpalUtil.toList(parentsampleid, "|");
        for (String parentsample : parentSampleList) {
            int n;
            int n2;
            String[] stringArray;
            Map<String, String> trackitemCopyDownMap;
            Map<String, String> sampleCopyDownMap;
            DataSet ds;
            String childstoragestatus;
            String sampletypeid;
            String poolGLPFlag;
            String poolCustodialDepartmentID;
            String poolContainerTypeID;
            String poolUnitID;
            String poolQuantity;
            String poolTemplateID;
            String poolCopies;
            String parentQuantity;
            int poolRow = poolDS.addRow();
            parentsampleid = parentsample;
            poolDS.setString(poolRow, "parentsampleid", parentsampleid);
            String childStudy = childstudyid;
            if (childStudy.contains("|")) {
                childStudy = StringUtil.split(childStudy, "|")[poolRow];
            }
            if ((parentQuantity = quantity).contains("|")) {
                parentQuantity = StringUtil.split(parentQuantity, "|")[poolRow];
            }
            if ((poolCopies = poolcopies).contains("|")) {
                poolCopies = StringUtil.split(poolCopies, "|")[poolRow];
            }
            if ((poolTemplateID = pooltemplateid).contains("|")) {
                poolTemplateID = StringUtil.split(poolTemplateID, "|")[poolRow];
            }
            if ((poolQuantity = poolquantity).contains("|")) {
                poolQuantity = StringUtil.split(poolQuantity, "|")[poolRow];
            }
            double poolSampleQuantity = 0.0;
            if (quantitycalculation) {
                try {
                    poolSampleQuantity = Double.parseDouble(poolQuantity) / Double.parseDouble(poolCopies);
                }
                catch (NumberFormatException e) {
                    throw new SapphireException("Invalid number of pool samples/quantity.", e);
                }
            }
            if ((poolUnitID = poolunitid).contains("|")) {
                poolUnitID = StringUtil.split(poolUnitID, "|")[poolRow];
            }
            if ((poolContainerTypeID = poolcontainertypeid).contains("|")) {
                poolContainerTypeID = StringUtil.split(poolContainerTypeID, "|")[poolRow];
            }
            if ((poolCustodialDepartmentID = poolcustodialdepartmentid).contains("|")) {
                poolCustodialDepartmentID = StringUtil.split(poolCustodialDepartmentID, "|")[poolRow];
            }
            if ((poolGLPFlag = poolglpflag).contains("|")) {
                poolGLPFlag = StringUtil.split(poolGLPFlag, "|")[poolRow];
            }
            HashMap<String, String> map = this.getParentInfo(parentsampleid);
            String studyid = "";
            String samplefamilyid = "";
            boolean childHasNewSampleFamily = false;
            if (MODE_BIOBANK.equals(mode)) {
                studyid = childStudy;
                if (OpalUtil.isEmpty(studyid) && (OpalUtil.isEmpty(studyid = map.get("studyid")) || studyid.contains(";"))) {
                    studyid = childStudy.length() > 0 ? childStudy : "POOL";
                }
                if (childStudy.length() > 0) {
                    childHasNewSampleFamily = true;
                } else {
                    samplefamilyid = map.get("samplefamilyid");
                    if (samplefamilyid == null || samplefamilyid.contains(";")) {
                        childHasNewSampleFamily = true;
                    }
                }
            } else {
                samplefamilyid = map.get("samplefamilyid");
                if (samplefamilyid.contains(";")) {
                    childHasNewSampleFamily = true;
                }
            }
            if (childHasNewSampleFamily) {
                String parentsubjectid = "";
                String participantid = "";
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select sf.subjectid from s_samplefamily sf where sf.s_samplefamilyid in (select s.samplefamilyid from s_sample s where s.s_sampleid in (" + safeSQL.addIn(parentsampleid, ";") + "))";
                DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (dataSet != null && dataSet.size() > 0) {
                    parentsubjectid = dataSet.getString(0, "subjectid", "");
                    if (OpalUtil.isNotEmpty(parentsubjectid)) {
                        for (int i = 1; i < dataSet.size(); ++i) {
                            if (parentsubjectid.equals(dataSet.getString(i, "subjectid", ""))) continue;
                            parentsubjectid = "";
                            break;
                        }
                    }
                    if (OpalUtil.isNotEmpty(parentsubjectid)) {
                        DataSet participantds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid from s_participant where sstudyid = ? and subjectid = ?", (Object[])new String[]{studyid, parentsubjectid});
                        if (participantds != null && participantds.size() > 0) {
                            participantid = participantds.getString(0, "s_participantid", "");
                        }
                        if (OpalUtil.isEmpty(participantid)) {
                            PropertyList addProps = new PropertyList();
                            addProps.setProperty("sdcid", "LV_Participant");
                            addProps.setProperty("copies", VERSION);
                            addProps.setProperty("sstudyid", studyid);
                            addProps.setProperty("subjectid", parentsubjectid);
                            addProps.setProperty("participantstatus", "Associated");
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), addProps);
                            participantid = addProps.getProperty("newkeyid1", "");
                        }
                    }
                }
                props.clear();
                props.setProperty("sdcid", "LV_SampleFamily");
                props.setProperty("sstudyid", studyid);
                props.setProperty("copies", VERSION);
                props.setProperty("subjectid", parentsubjectid);
                props.setProperty("participantid", participantid);
                props.setProperty("__ignoreparticipant", "Y");
                props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                Map<String, String> familyCopyDownMap = ChildSampleUtil.getCopyDownValues(this.getConfigurationProcessor(), this.getQueryProcessor(), "LV_SampleFamily", parentsampleid, "pool", null);
                if (familyCopyDownMap != null) {
                    for (String columnid : familyCopyDownMap.keySet()) {
                        props.setProperty(columnid, familyCopyDownMap.get(columnid));
                    }
                }
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                samplefamilyid = props.getProperty("newkeyid1");
            }
            if ((sampletypeid = map.get("sampletypeid")) == null || sampletypeid.contains(";")) {
                sampletypeid = "";
            }
            if (OpalUtil.isEmpty(childstoragestatus = actionProps.getProperty("childstoragestatus"))) {
                childstoragestatus = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("childsamplestatus", "In Prep");
            }
            if ("Inherit".equalsIgnoreCase(childstoragestatus) && (ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "Sample", "select storagestatus from s_sample where s_sampleid in ([])", parentsampleid)) != null && ds.size() > 0) {
                void var48_55;
                String parentstoragestatus = ds.getString(0, "storagestatus");
                childstoragestatus = parentstoragestatus;
                boolean bl = true;
                while (var48_55 < ds.size()) {
                    if (!parentstoragestatus.equals(ds.getString(0, "storagestatus"))) {
                        childstoragestatus = "In Prep";
                        break;
                    }
                    ++var48_55;
                }
            }
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("copies", poolCopies);
            props.setProperty("templateid", poolTemplateID);
            props.setProperty("applyworkitems", "Y");
            props.setProperty("samplefamilyid", samplefamilyid);
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("auditactivity", auditactivity);
            if (MODE_BIOBANK.equals(mode)) {
                props.setProperty("sstudyid", studyid);
                props.setProperty("glpflag", poolGLPFlag);
            } else if (quantitycalculation) {
                props.setProperty("addtrackitem", "Y");
            }
            props.setProperty("storagestatus", childstoragestatus);
            props.setProperty("pooledflag", "Y");
            if (StringUtil.getLen(sampletypeid) > 0L) {
                props.setProperty("sampletypeid", sampletypeid);
            }
            if ((sampleCopyDownMap = ChildSampleUtil.getCopyDownValues(this.getConfigurationProcessor(), this.getQueryProcessor(), "Sample", parentsampleid, "pool", null)) != null) {
                for (String string : sampleCopyDownMap.keySet()) {
                    props.setProperty(string, sampleCopyDownMap.get(string));
                }
            }
            if ((trackitemCopyDownMap = ChildSampleUtil.getCopyDownValues(this.getConfigurationProcessor(), this.getQueryProcessor(), "TrackItemSDC", parentsampleid, "pool", null)) != null) {
                for (Object columnid2 : trackitemCopyDownMap.keySet()) {
                    props.setProperty("__trackitem_" + (String)columnid2, trackitemCopyDownMap.get(columnid2));
                }
            }
            HashMap<String, String> hashMap = new HashMap<String, String>();
            try {
                Object columnid2;
                columnid2 = actionProps.keySet().iterator();
                while (columnid2.hasNext()) {
                    void var53_71;
                    String childColumnID;
                    String property;
                    Object o = columnid2.next();
                    if (!(o instanceof String) || !(property = (String)o).startsWith("childcolumn_") || !OpalUtil.isNotEmpty(childColumnID = property.substring(12))) continue;
                    String string = actionProps.getProperty(property, "");
                    if (string.contains("|")) {
                        String string2 = StringUtil.split(string, "|")[poolRow];
                    }
                    props.setProperty(childColumnID, (String)var53_71);
                    hashMap.put(childColumnID, (String)var53_71);
                }
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                ErrorHandler errorHandler2 = this.getActionProcessor().getErrorHandler();
                if (errorHandler2 != null && errorHandler2.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
            String poolTrackItems = props.getProperty("newtrackitemid");
            String childsampleids = props.getProperty("newkeyid1");
            poolDS.setString(poolRow, "childsampleids", childsampleids);
            poolDS.setString(poolRow, "childsampletrackitemids", poolTrackItems);
            String[] child = StringUtil.split(childsampleids, ";");
            String[] parent = StringUtil.split(parentsampleid, ";");
            if (props.getProperty("templateid").length() > 0) {
                this.updateAdditionalChildSampleColumns(OpalUtil.toList(childsampleids, ";"), hashMap);
            }
            for (String aChild : child) {
                stringArray = parent;
                n2 = stringArray.length;
                for (n = 0; n < n2; ++n) {
                    String aParent = stringArray[n];
                    int sampleMapRow = sampleMapInsertDS.addRow();
                    sampleMapInsertDS.setString(sampleMapRow, "sourcesampleid", aParent);
                    sampleMapInsertDS.setString(sampleMapRow, "destsampleid", aChild);
                }
            }
            String string = actionProps.getProperty("child_aliasdata", "").trim();
            if (string.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    String aliastype = jsonObject.getString("aliastype");
                    String[] aliasid = jsonObject.getString("aliasid");
                    if (OpalUtil.isNotEmpty(aliastype) && OpalUtil.isNotEmpty((String)aliasid)) {
                        stringArray = child;
                        n2 = stringArray.length;
                        for (n = 0; n < n2; ++n) {
                            String aChild = stringArray[n];
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", aChild);
                            props.setProperty("aliastype", aliastype);
                            props.setProperty("aliasid", (String)aliasid);
                            this.getActionProcessor().processActionClass(AddSDIAlias.class.getName(), props);
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!quantitycalculation) continue;
            if (poolTrackItems != null && poolTrackItems.length() > 0) {
                String[] trackitemArray;
                DataSet trackItemDS = new DataSet();
                for (String trackitemid : trackitemArray = StringUtil.split(poolTrackItems, ";")) {
                    int trackItemRow = trackItemDS.addRow();
                    trackItemDS.setString(trackItemRow, "keyid1", trackitemid);
                }
                trackItemDS.setString(-1, "qtycurrent", String.valueOf(poolSampleQuantity));
                trackItemDS.setString(-1, "qtyunits", poolUnitID);
                if (MODE_BIOBANK.equals(mode)) {
                    if ("Allocated".equals(childstoragestatus)) {
                        trackItemDS.setString(-1, "custodialdepartmentid", "");
                        trackItemDS.setString(-1, "custodialuserid", "");
                    } else if ("In Prep".equals(childstoragestatus)) {
                        trackItemDS.setString(-1, "custodialdepartmentid", poolCustodialDepartmentID);
                        trackItemDS.setString(-1, "custodialuserid", "");
                    } else {
                        trackItemDS.setString(-1, "custodialdepartmentid", poolCustodialDepartmentID);
                        trackItemDS.setString(-1, "custodialuserid", this.connectionInfo.getSysuserId());
                    }
                    if (StringUtil.getLen(sampletypeid) > 0L) {
                        String sql = "select s_sampletypeid, freezethawflag, freezethawcountwarn, freezethawcountmax from s_sampletype where s_sampletypeid = ? and freezethawflag = 'Y'";
                        DataSet ftsampletype = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{sampletypeid});
                        if (ftsampletype != null && ftsampletype.size() > 0) {
                            DataSet ds2;
                            String ftcount;
                            DataSet ds22;
                            trackItemDS.setString(-1, "freezethawflag", "Y");
                            PropertyList freezethawcopydown = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getPropertyListNotNull("freezethawcopydown");
                            PropertyList freezethawprops = freezethawcopydown.getPropertyListNotNull("pool");
                            String freezethawcount = freezethawprops.getProperty("freezethawcount");
                            String freezethawwarn = freezethawprops.getProperty("freezethawwarn");
                            String freezethawmax = freezethawprops.getProperty("freezethawmax");
                            SafeSQL safeSQL = new SafeSQL();
                            if ("Minimum".equals(freezethawcount)) {
                                sql = "select min(freezethawcount) freezethawcount from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                            } else if ("Maximum".equals(freezethawcount)) {
                                sql = "select max(freezethawcount) freezethawcount from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                            }
                            if (sql.length() > 0 && (ds22 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) != null && ds22.size() > 0 && StringUtil.getLen(ftcount = ds22.getValue(0, "freezethawcount", "")) > 0L) {
                                trackItemDS.setString(-1, "freezethawcount", ftcount);
                            }
                            safeSQL.reset();
                            if ("Sample Type".equals(freezethawwarn)) {
                                trackItemDS.setString(-1, "freezethawcountwarn", ftsampletype.getValue(0, "freezethawcountwarn"));
                            } else {
                                String freezethawcountwarn;
                                if ("Minimum".equals(freezethawwarn)) {
                                    sql = "select min(freezethawcountwarn) freezethawcountwarn from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                                } else if ("Maximum".equals(freezethawwarn)) {
                                    sql = "select max(freezethawcountwarn) freezethawcountwarn from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                                }
                                if (sql.length() > 0 && (ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) != null && ds2.size() > 0 && StringUtil.getLen(freezethawcountwarn = ds2.getValue(0, "freezethawcountwarn", "")) > 0L) {
                                    trackItemDS.setString(-1, "freezethawcountwarn", freezethawcountwarn);
                                }
                            }
                            safeSQL.reset();
                            if ("Sample Type".equals(freezethawmax)) {
                                trackItemDS.setString(-1, "freezethawcountmax", ftsampletype.getValue(0, "freezethawcountmax"));
                            } else {
                                String freezethawcountmax;
                                if ("Minimum".equals(freezethawmax)) {
                                    sql = "select min(freezethawcountmax) freezethawcountmax from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                                } else if ("Maximum".equals(freezethawmax)) {
                                    sql = "select max(freezethawcountmax) freezethawcountmax from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                                }
                                if (sql.length() > 0 && (ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) != null && ds2.size() > 0 && StringUtil.getLen(freezethawcountmax = ds2.getValue(0, "freezethawcountmax", "")) > 0L) {
                                    trackItemDS.setString(-1, "freezethawcountmax", freezethawcountmax);
                                }
                            }
                        }
                    }
                }
                trackItemDS.setString(-1, "containertypeid", poolContainerTypeID);
                for (int i = 0; i < trackItemDS.size(); ++i) {
                    poolTrackItemEditDS.copyRow(trackItemDS, i, 1);
                }
            }
            if (StringUtil.getLen(parentsampleid) <= 0L || StringUtil.getLen(parentQuantity) <= 0L) continue;
            String[] parentArray = StringUtil.split(parentsampleid, ";");
            String[] parentQuantityArr = StringUtil.split(parentQuantity, ";");
            boolean multipleQuantities = false;
            if (parentQuantity.contains(";")) {
                multipleQuantities = true;
            }
            for (int i = 0; i < parentArray.length; ++i) {
                int row = parentQuantityDS.addRow();
                parentQuantityDS.setString(row, "keyid1", parentArray[i]);
                if (multipleQuantities) {
                    parentQuantityDS.setString(row, "qtycurrent", parentQuantityArr[i]);
                    continue;
                }
                parentQuantityDS.setString(row, "qtycurrent", parentQuantity);
            }
        }
        if (sampleMapInsertDS.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", sampleMapInsertDS.getColumnValues("sourcesampleid", ";"));
            props.setProperty("s_childsampleid", sampleMapInsertDS.getColumnValues("destsampleid", ";"));
            props.setProperty("linkid", "Child Samples");
            this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
        }
        if (poolTrackItemEditDS.size() > 0) {
            try {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                for (int i = 0; i < poolTrackItemEditDS.getColumnCount(); ++i) {
                    String columnid = poolTrackItemEditDS.getColumnId(i);
                    props.setProperty(columnid, poolTrackItemEditDS.getColumnValues(columnid, ";"));
                }
                props.setProperty("auditreason", auditreason);
                props.setProperty("auditsignedflag", auditsignedflag);
                props.setProperty("auditactivity", auditactivity);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props, false);
                errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        if (parentQuantityDS.size() > 0) {
            try {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", parentQuantityDS.getColumnValues("keyid1", ";"));
                props.setProperty("qtycurrent", parentQuantityDS.getColumnValues("qtycurrent", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("auditreason", auditreason);
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props, false);
                errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        if (disposesampleid.length() > 0) {
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", disposesampleid);
            props.setProperty("samplestatus", "Disposed");
            props.setProperty("storagestatus", "Disposed");
            props.setProperty("storagedisposalstatus", "Consumed");
            props.setProperty("disposalstatus", "Disposed");
            props.setProperty("__sdcruleconfirm", "Y");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        actionProps.setProperty("newkeyid1", poolDS.getColumnValues("childsampleids", "|"));
        actionProps.setProperty("newtrackitemid", poolDS.getColumnValues("childsampletrackitemids", "|"));
    }

    private HashMap<String, String> getParentInfo(String sampleid) {
        HashMap<String, String> map = new HashMap<String, String>();
        HashSet<String> study = new HashSet<String>();
        HashSet<String> family = new HashSet<String>();
        HashSet<String> sampletype = new HashSet<String>();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select sstudyid, samplefamilyid, sampletypeid from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                study.add(ds.getValue(i, "sstudyid", ""));
                family.add(ds.getValue(i, "samplefamilyid", ""));
                sampletype.add(ds.getValue(i, "sampletypeid", ""));
            }
        }
        map.put("studyid", study.isEmpty() ? "" : OpalUtil.toDelimitedString(study, ";"));
        map.put("samplefamilyid", family.isEmpty() ? "" : OpalUtil.toDelimitedString(family, ";"));
        map.put("sampletypeid", sampletype.isEmpty() ? "" : OpalUtil.toDelimitedString(sampletype, ";"));
        return map;
    }

    private void updateAdditionalChildSampleColumns(List<String> childsampleList, Map<String, String> additionalColumnMap) throws SapphireException {
        DataSet childColumnDS;
        if (OpalUtil.isEmpty(childsampleList) || additionalColumnMap.size() == 0) {
            return;
        }
        DataSet sampleColumnDS = this.getSDCProcessor().getColumnData("Sample");
        StringBuilder childcolumnsql = new StringBuilder("select s_sampleid");
        for (String columnid : additionalColumnMap.keySet()) {
            if (sampleColumnDS.findRow("columnid", columnid) == -1) continue;
            childcolumnsql.append(",").append(columnid);
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
                    String childSampleValue = childColumnDS.getValue(row, columnid, "");
                    String userEnteredValue = additionalColumnMap.get(columnid);
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
}

