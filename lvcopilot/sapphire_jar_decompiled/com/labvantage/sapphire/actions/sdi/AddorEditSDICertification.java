/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddorEditSDICertification
extends BaseAction
implements sapphire.action.AddorEditSDICertification {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int i;
        List<String> resourceSdcidProp = Arrays.asList(StringUtil.split(properties.getProperty("resourcesdcid"), ";"));
        List<String> resourceKeyid1Prop = Arrays.asList(StringUtil.split(properties.getProperty("resourcekeyid1"), ";"));
        List<String> resourceKeyid2Prop = Arrays.asList(StringUtil.split(properties.getProperty("resourcekeyid2"), ";"));
        List<String> resourceKeyid3Prop = Arrays.asList(StringUtil.split(properties.getProperty("resourcekeyid3"), ";"));
        List<String> certifiedForSdcidProp = Arrays.asList(StringUtil.split(properties.getProperty("certifiedforsdcid"), ";"));
        List<String> certificationTypeProp = Arrays.asList(StringUtil.split(properties.getProperty("certificationtype"), ";"));
        List<String> certificationStatusProp = Arrays.asList(StringUtil.split(properties.getProperty("certificationstatus"), ";"));
        List<String> certifiedForKeyId1Prop = Arrays.asList(StringUtil.split(properties.getProperty("certifiedforkeyid1"), ";"));
        List<String> certifiedForKeyId2Prop = Arrays.asList(StringUtil.split(properties.getProperty("certifiedforkeyid2"), ";"));
        List<String> certifiedForKeyId3Prop = Arrays.asList(StringUtil.split(properties.getProperty("certifiedforkeyid3"), ";"));
        List<String> expirationDateProp = Arrays.asList(StringUtil.split(properties.getProperty("expirationdt"), ";"));
        List<String> gracePeriodProp = Arrays.asList(StringUtil.split(properties.getProperty("graceperiod"), ";"));
        List<String> gracePeriodUnitsProp = Arrays.asList(StringUtil.split(properties.getProperty("graceperiodunits"), ";"));
        List<String> actionProp = Arrays.asList(StringUtil.split(properties.getProperty("action"), ";"));
        DataSet dsinsert = new DataSet(this.connectionInfo);
        DataSet dsupdate = new DataSet(this.connectionInfo);
        ArrayList keyColsList = new ArrayList();
        DataSet dsdelete = new DataSet(this.connectionInfo);
        SafeSQL safeSQL = new SafeSQL();
        if (actionProp.size() == 0 || actionProp.size() != resourceSdcidProp.size() || actionProp.size() != resourceKeyid1Prop.size()) {
            throw new SapphireException("Mismatch between number and its corresponding details of items requested to be modified");
        }
        DataSet certStatusRef = this.getQueryProcessor().getRefTypeDataSet("Certification Status");
        DataSet gracePeriodRef = this.getQueryProcessor().getRefTypeDataSet("Grace Period Units");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        for (i = 0; i < actionProp.size(); ++i) {
            PropertyList sdc;
            String action = actionProp.get(i);
            boolean shouldAdd = action.equalsIgnoreCase("add");
            boolean shouldEdit = action.equalsIgnoreCase("edit");
            boolean shouldDelete = action.equalsIgnoreCase("delete");
            String resourceSdcid = null;
            String resourceKeyid1 = null;
            String resourceKeyid2 = null;
            String resourceKeyid3 = null;
            String certifiedForSdcid = null;
            String certificationType = null;
            String certificationStatus = null;
            String certifiedForKeyId1 = null;
            String certifiedForKeyId2 = null;
            String certifiedForKeyId3 = null;
            String expirationDate = null;
            String gracePeriod = null;
            String gracePeriodUnit = null;
            boolean error = false;
            if (i >= resourceSdcidProp.size() || resourceSdcidProp.get(i) == null || resourceSdcidProp.get(i).trim().length() == 0 || resourceSdcidProp.get(i).trim().equalsIgnoreCase("null")) {
                this.logger.error("Resource SDC ID can not be empty");
                error = true;
                resourceSdcid = "(null)";
            } else if (i < resourceSdcidProp.size() && (sdc = sdcProcessor.getPropertyList(resourceSdcid = resourceSdcidProp.get(i))) == null) {
                this.logger.error("Unrecognized Resource SDC: " + resourceSdcid);
                error = true;
            }
            if (i >= resourceKeyid1Prop.size() || resourceKeyid1Prop.get(i) == null || resourceKeyid1Prop.get(i).trim().length() == 0 || resourceKeyid1Prop.get(i).trim().equalsIgnoreCase("null")) {
                this.logger.error("Resource KeyID1 can not be empty");
                error = true;
                resourceKeyid1 = "(null)";
            } else if (i < resourceKeyid1Prop.size()) {
                resourceKeyid1 = resourceKeyid1Prop.get(i);
            }
            if (i >= resourceKeyid2Prop.size() || resourceKeyid2Prop.get(i) == null || resourceKeyid2Prop.get(i).trim().length() == 0 || resourceKeyid2Prop.get(i).trim().equalsIgnoreCase("null")) {
                resourceKeyid2 = "(null)";
                resourceKeyid3 = "(null)";
            } else if (i < resourceKeyid2Prop.size()) {
                resourceKeyid2 = resourceKeyid2Prop.get(i);
                if (i >= resourceKeyid3Prop.size() || resourceKeyid3Prop.get(i) == null || resourceKeyid3Prop.get(i).trim().length() == 0 || resourceKeyid3Prop.get(i).trim().equalsIgnoreCase("null")) {
                    resourceKeyid3 = "(null)";
                } else if (i < resourceKeyid3Prop.size()) {
                    resourceKeyid3 = resourceKeyid3Prop.get(i);
                }
            }
            if (i >= certifiedForSdcidProp.size() || certifiedForSdcidProp.get(i) == null || certifiedForSdcidProp.get(i).trim().length() == 0 || certifiedForSdcidProp.get(i).trim().equalsIgnoreCase("null")) {
                certifiedForSdcid = "(null)";
            } else if (i < certifiedForSdcidProp.size() && (sdc = sdcProcessor.getPropertyList(certifiedForSdcid = certifiedForSdcidProp.get(i))) == null) {
                this.logger.error("Unrecognized Resource SDC: " + certifiedForSdcid);
                error = true;
            }
            if (shouldAdd && (i >= certificationTypeProp.size() || certificationTypeProp.get(i) == null || certificationTypeProp.get(i).trim().length() == 0 || certificationTypeProp.get(i).trim().equalsIgnoreCase("null"))) {
                this.logger.error("Certification type can not be empty");
                error = true;
                certificationType = "(null)";
            } else if (i >= certificationTypeProp.size() || certificationTypeProp.get(i) == null || certificationTypeProp.get(i).trim().length() == 0 || certificationTypeProp.get(i).trim().equalsIgnoreCase("null")) {
                certificationType = "(null)";
            } else if (i < certificationTypeProp.size()) {
                certificationType = certificationTypeProp.get(i);
            }
            if (shouldAdd && (i >= certificationStatusProp.size() || certificationStatusProp.get(i) == null || certificationStatusProp.get(i).trim().length() == 0 || certificationStatusProp.get(i).trim().equalsIgnoreCase("null"))) {
                this.logger.error("Certification status can not be empty");
                error = true;
                certificationStatus = "(null)";
            } else if (i >= certificationStatusProp.size() || certificationStatusProp.get(i) == null || certificationStatusProp.get(i).trim().length() == 0 || certificationStatusProp.get(i).trim().equalsIgnoreCase("null")) {
                certificationStatus = "(null)";
            } else if (i < certificationStatusProp.size()) {
                if (certStatusRef.findRow("refvalueid", certificationStatusProp.get(i)) > -1) {
                    certificationStatus = certificationStatusProp.get(i);
                } else {
                    this.logger.error("Invalid certification status: " + certificationStatusProp.get(i));
                    error = true;
                }
            }
            if (i >= certifiedForKeyId1Prop.size() || certifiedForKeyId1Prop.get(i) == null || certifiedForKeyId1Prop.get(i).trim().length() == 0 || certifiedForKeyId1Prop.get(i).trim().equalsIgnoreCase("null")) {
                certifiedForKeyId1 = "(null)";
            } else if (i < certifiedForKeyId1Prop.size()) {
                certifiedForKeyId1 = certifiedForKeyId1Prop.get(i);
            }
            if (i >= certifiedForKeyId2Prop.size() || certifiedForKeyId2Prop.get(i) == null || certifiedForKeyId2Prop.get(i).trim().length() == 0 || certifiedForKeyId2Prop.get(i).trim().equalsIgnoreCase("null")) {
                certifiedForKeyId2 = "(null)";
                certifiedForKeyId3 = "(null)";
            } else if (i < certifiedForKeyId2Prop.size()) {
                certifiedForKeyId2 = certifiedForKeyId2Prop.get(i);
                if (i >= certifiedForKeyId3Prop.size() || certifiedForKeyId3Prop.get(i) == null || certifiedForKeyId3Prop.get(i).trim().length() == 0 || certifiedForKeyId3Prop.get(i).trim().equalsIgnoreCase("null")) {
                    certifiedForKeyId3 = "(null)";
                } else if (i < certifiedForKeyId3Prop.size()) {
                    certifiedForKeyId3 = certifiedForKeyId3Prop.get(i);
                }
            }
            if (i >= expirationDateProp.size() || expirationDateProp.get(i) == null || expirationDateProp.get(i).trim().length() == 0 || expirationDateProp.get(i).trim().equalsIgnoreCase("null")) {
                expirationDate = "(null)";
            } else if (i < expirationDateProp.size()) {
                expirationDate = expirationDateProp.get(i);
            }
            if (i >= gracePeriodProp.size() || gracePeriodProp.get(i) == null || gracePeriodProp.get(i).trim().length() == 0 || gracePeriodProp.get(i).trim().equalsIgnoreCase("null")) {
                gracePeriod = "(null)";
            } else if (i < gracePeriodProp.size()) {
                gracePeriod = gracePeriodProp.get(i);
            }
            if (shouldAdd && !gracePeriod.equals("(null)") && gracePeriod.trim().length() > 0 && (i >= gracePeriodUnitsProp.size() || gracePeriodUnitsProp.get(i) == null || gracePeriodUnitsProp.get(i).trim().length() == 0 || gracePeriodUnitsProp.get(i).trim().equalsIgnoreCase("null"))) {
                this.logger.error("Grace Period Unit can not be empty");
                error = true;
                gracePeriodUnit = "(null)";
            } else if (i >= gracePeriodUnitsProp.size() || gracePeriodUnitsProp.get(i) == null || gracePeriodUnitsProp.get(i).trim().length() == 0 || gracePeriodUnitsProp.get(i).trim().equalsIgnoreCase("null")) {
                gracePeriodUnit = "(null)";
            } else if (i < gracePeriodUnitsProp.size()) {
                if (gracePeriodRef.findRow("refvalueid", gracePeriodUnitsProp.get(i)) > -1) {
                    gracePeriodUnit = gracePeriodRef.getString(gracePeriodRef.findRow("refvalueid", gracePeriodUnitsProp.get(i)), "refvalueid");
                } else {
                    this.logger.error("Invalid grace period unit: " + gracePeriodUnitsProp.get(i));
                    error = true;
                    gracePeriodUnit = gracePeriodUnitsProp.get(i);
                }
            }
            if (error) {
                StringBuilder errorMssg = new StringBuilder("Ignoring improper record :[");
                errorMssg.append("action=").append(actionProp.get(i));
                errorMssg.append("|resourcesdcid=").append(resourceSdcid);
                errorMssg.append("|resourcekeyid1=").append(resourceKeyid1);
                errorMssg.append("|resourcekeyid2=").append(resourceKeyid2);
                errorMssg.append("|resourcekeyid3=").append(resourceKeyid3);
                errorMssg.append("|certifiedforsdcid=").append(certifiedForSdcid);
                errorMssg.append("|certificationtype=").append(certificationType);
                errorMssg.append("|certificationstatus=").append(certificationStatus);
                errorMssg.append("|certifiedforkeyid1=").append(certifiedForKeyId1);
                errorMssg.append("|certifiedforkeyid2=").append(certifiedForKeyId2);
                errorMssg.append("|certifiedforkeyid3=").append(certifiedForKeyId3);
                errorMssg.append("|certifiedforkeyid3=").append(certifiedForKeyId3);
                errorMssg.append("|expirationdt=").append(expirationDate);
                errorMssg.append("|graceperiod=").append(gracePeriod);
                errorMssg.append("|graceperiodunits=").append(gracePeriodUnit);
                errorMssg.append("]");
                this.logger.error(errorMssg.toString());
                throw new SapphireException(errorMssg.toString());
            }
            if (shouldAdd) {
                int newRow = dsinsert.addRow();
                dsinsert.setString(newRow, "resourcesdcid", resourceSdcid);
                dsinsert.setString(newRow, "resourcekeyid1", resourceKeyid1);
                dsinsert.setString(newRow, "resourcekeyid2", resourceKeyid2);
                dsinsert.setString(newRow, "resourcekeyid3", resourceKeyid3);
                dsinsert.setString(newRow, "certifiedforsdcid", certifiedForSdcid);
                dsinsert.setString(newRow, "certificationtype", certificationType);
                dsinsert.setString(newRow, "certifiedforkeyid1", certifiedForKeyId1);
                dsinsert.setString(newRow, "certifiedforkeyid2", certifiedForKeyId2);
                dsinsert.setString(newRow, "certifiedforkeyid3", certifiedForKeyId3);
                dsinsert.setString(newRow, "certificationstatus", certificationStatus);
                dsinsert.setDate(newRow, "expirationdt", expirationDate);
                if (!gracePeriod.equals("(null)")) {
                    dsinsert.setString(newRow, "graceperiod", gracePeriod);
                    dsinsert.setString(newRow, "graceperiodunits", gracePeriodUnit);
                }
                this.addOrEditUserColsToDataset(properties, dsinsert, i, newRow);
            }
            if (shouldEdit) {
                ArrayList<String> keyCols = new ArrayList<String>();
                int newRow = dsupdate.addRow();
                dsupdate.setString(newRow, "resourcesdcid", resourceSdcid);
                dsupdate.setString(newRow, "resourcekeyid1", resourceKeyid1);
                keyCols.add("resourcesdcid");
                keyCols.add("resourcekeyid1");
                if (!resourceKeyid2.equals("(null)")) {
                    dsupdate.setString(newRow, "resourcekeyid2", resourceKeyid2);
                    keyCols.add("resourcekeyid2");
                    if (!resourceKeyid3.equals("(null)")) {
                        dsupdate.setString(newRow, "resourcekeyid3", resourceKeyid3);
                        keyCols.add("resourcekeyid3");
                    }
                }
                if (!certifiedForSdcid.equals("(null)")) {
                    dsupdate.setString(newRow, "certifiedforsdcid", certifiedForSdcid);
                    keyCols.add("certifiedforsdcid");
                }
                if (!certificationType.equals("(null)")) {
                    dsupdate.setString(newRow, "certificationtype", certificationType);
                }
                if (!certifiedForKeyId1.equals("(null)")) {
                    dsupdate.setString(newRow, "certifiedforkeyid1", certifiedForKeyId1);
                    keyCols.add("certifiedforkeyid1");
                }
                if (!certifiedForKeyId2.equals("(null)")) {
                    dsupdate.setString(newRow, "certifiedforkeyid2", certifiedForKeyId2);
                    keyCols.add("certifiedforkeyid2");
                }
                if (!certifiedForKeyId3.equals("(null)")) {
                    dsupdate.setString(newRow, "certifiedforkeyid3", certifiedForKeyId3);
                    keyCols.add("certifiedforkeyid3");
                }
                if (!certificationStatus.equals("(null)")) {
                    dsupdate.setString(newRow, "certificationstatus", certificationStatus);
                }
                if (!expirationDate.equals("(null)")) {
                    dsupdate.setDate(newRow, "expirationdt", expirationDate);
                }
                if (!gracePeriod.equals("(null)")) {
                    dsupdate.setString(newRow, "graceperiod", gracePeriod);
                }
                if (!gracePeriodUnit.equals("(null)")) {
                    dsupdate.setString(newRow, "graceperiodunits", gracePeriodUnit);
                }
                dsupdate.setString(newRow, "modby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                dsupdate.setString(newRow, "modtool", "AddOrEditSDICert");
                dsupdate.setDate(newRow, "moddt", DateTimeUtil.getNowCalendar());
                this.addOrEditUserColsToDataset(properties, dsupdate, i, newRow);
                keyColsList.add(keyCols);
            }
            if (!shouldDelete) continue;
            int newRow = dsdelete.addRow();
            dsdelete.setString(newRow, "resourcesdcid", resourceSdcid);
            dsdelete.setString(newRow, "resourcekeyid1", resourceKeyid1);
            dsdelete.setString(newRow, "resourcekeyid2", resourceKeyid2);
            dsdelete.setString(newRow, "resourcekeyid3", resourceKeyid3);
            dsdelete.setString(newRow, "certifiedforsdcid", certifiedForSdcid);
            dsdelete.setString(newRow, "certifiedforkeyid1", certifiedForKeyId1);
            dsdelete.setString(newRow, "certifiedforkeyid2", certifiedForKeyId2);
            dsdelete.setString(newRow, "certifiedforkeyid3", certifiedForKeyId3);
        }
        if (dsinsert.size() > 0) {
            try {
                DataSetUtil.insert(this.database, dsinsert, "s_sdicertification");
            }
            catch (Exception e) {
                throw new SapphireException("Not able to Add SDI Certification" + e);
            }
        }
        if (dsupdate.size() > 0) {
            try {
                for (i = 0; i < dsupdate.getRowCount(); ++i) {
                    DataSetUtil.update(this.database, dsupdate.getRows(i, i + 1), "s_sdicertification", ((List)keyColsList.get(i)).toArray(new String[((List)keyColsList.get(i)).size()]));
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        if (dsdelete.size() > 0) {
            for (i = 0; i < dsdelete.size(); ++i) {
                try {
                    safeSQL.reset();
                    StringBuffer sql = new StringBuffer("delete from s_sdicertification where resourcesdcid=" + safeSQL.addVar(dsdelete.getString(i, "resourcesdcid")) + " and " + "resourcekeyid1" + "=" + safeSQL.addVar(dsdelete.getString(i, "resourcekeyid1")));
                    if (!dsdelete.getString(i, "resourcekeyid2").equals("(null)")) {
                        sql.append(" and ").append("resourcekeyid2").append("=").append(safeSQL.addVar(dsdelete.getString(i, "resourcekeyid2")));
                        if (!dsdelete.getString(i, "resourcekeyid3").equals("(null)")) {
                            sql.append(" and ").append("resourcekeyid3").append("=").append(safeSQL.addVar(dsdelete.getString(i, "resourcekeyid3")));
                        }
                    }
                    if (!dsdelete.getString(i, "certifiedforsdcid").equals("(null)")) {
                        sql.append(" and ").append("certifiedforsdcid").append("=").append(safeSQL.addVar(dsdelete.getString(i, "certifiedforsdcid")));
                    }
                    if (!dsdelete.getString(i, "certifiedforkeyid1").equals("(null)")) {
                        sql.append(" and ").append("certifiedforkeyid1").append("=").append(safeSQL.addVar(dsdelete.getString(i, "certifiedforkeyid1")));
                    }
                    if (!dsdelete.getString(i, "certifiedforkeyid2").equals("(null)")) {
                        sql.append(" and ").append("certifiedforkeyid2").append("=").append(safeSQL.addVar(dsdelete.getString(i, "certifiedforkeyid2")));
                    }
                    if (!dsdelete.getString(i, "certifiedforkeyid3").equals("(null)")) {
                        sql.append(" and ").append("certifiedforkeyid3").append("=").append(safeSQL.addVar(dsdelete.getString(i, "certifiedforkeyid3")));
                    }
                    this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
                    continue;
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        }
    }

    private void addOrEditUserColsToDataset(PropertyList properties, DataSet ds, int index, int newRow) throws SapphireException {
        int rowCnt;
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        DataSet filteredUserDefCols = this.getUserColList(properties);
        int n = rowCnt = filteredUserDefCols != null ? filteredUserDefCols.getRowCount() : 0;
        if (rowCnt > 0) {
            String colId = null;
            List<String> colVals = null;
            String colVal = null;
            block6: for (int x = 0; x < rowCnt; ++x) {
                colId = filteredUserDefCols.getValue(x, "columnid");
                colVals = Arrays.asList(StringUtil.split(properties.getProperty(colId), ";"));
                colVal = index >= colVals.size() || colVals.get(index) == null || colVals.get(index).trim().length() == 0 || colVals.get(index).trim().equalsIgnoreCase("null") ? "(null)" : colVals.get(index);
                switch (filteredUserDefCols.getValue(x, "datatype").charAt(0)) {
                    case 'C': {
                        ds.setString(newRow, colId, colVal);
                        continue block6;
                    }
                    case 'D': {
                        ds.setDate(newRow, colId, dtu.getCalendar(colVal));
                        continue block6;
                    }
                    case 'R': {
                        ds.setNumber(newRow, colId, colVal);
                        continue block6;
                    }
                    case 'N': {
                        ds.setNumber(newRow, colId, colVal);
                    }
                }
            }
        }
    }

    private DataSet getUserColList(PropertyList props) throws SapphireException {
        DataSet userCols = null;
        String sql = "select columnid, datatype from syscolumn where tableid = 's_sdicertification' and columnid not in ('resourcesdcid', 'resourcekeyid1', 'resourcekeyid2', 'resourcekeyid3', 'certifiedforsdcid', 'certifiedforkeyid1', 'certifiedforkeyid2', 'certifiedforkeyid3', 'certificationtype', 'expirationdt', 'graceperiod', 'graceperiodunits', 'certificationstatus', 'auditsequence', 'usersequence', 'createdt', 'createby', 'createtool', 'modby', 'moddt', 'modtool', 'tracelogid', 'activefag')";
        try {
            int rowCnt;
            userCols = this.getQueryProcessor().getSqlDataSet(sql);
            for (int i = rowCnt = userCols != null ? userCols.getRowCount() : 0; i > 0; --i) {
                if (props.containsKey(userCols.getValue(i - 1, "columnid"))) continue;
                userCols.deleteRow(i - 1);
            }
        }
        catch (Exception ex) {
            throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
        return userCols;
    }
}

