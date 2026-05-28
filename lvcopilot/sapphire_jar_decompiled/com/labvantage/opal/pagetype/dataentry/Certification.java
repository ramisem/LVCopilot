/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.pagetype.dataentry;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Certification {
    private boolean _debug = false;

    public String getCertificationArrays(PageContext pageContext, String resourcetomanage, String certificationCheck) {
        StringBuffer returnSb = new StringBuffer();
        if (!(certificationCheck != null && certificationCheck.trim().length() != 0 || resourcetomanage != null && resourcetomanage.trim().length() != 0)) {
            return returnSb.toString();
        }
        String currentUser = ((PageTagInfo)pageContext.getAttribute("pageinfo")).getProperty("sysuserid");
        boolean needDSAnalystCertificationCheck = false;
        boolean needDIAnalystCertificationCheck = false;
        boolean needInstrumentCertificationCheck = false;
        DataSet ds = null;
        if (resourcetomanage != null && resourcetomanage.equals("AnalystAssignment")) {
            needDSAnalystCertificationCheck = true;
        }
        if (certificationCheck != null) {
            if (certificationCheck.equals("Analyst")) {
                needDIAnalystCertificationCheck = true;
            } else if (certificationCheck.equals("Instrument")) {
                needInstrumentCertificationCheck = true;
            } else if (certificationCheck.equals("InstrumentAndAnalyst")) {
                needInstrumentCertificationCheck = true;
                needDIAnalystCertificationCheck = true;
            }
        }
        DataSet displayedSDIs = null;
        DataSet displayedDatasets = null;
        DataSet displayedDataItems = null;
        String certificationArray = null;
        if (needDSAnalystCertificationCheck || needDIAnalystCertificationCheck || needInstrumentCertificationCheck) {
            displayedSDIs = (DataSet)pageContext.getAttribute("displayedSDIs");
        }
        if (needDSAnalystCertificationCheck || needInstrumentCertificationCheck) {
            displayedDatasets = (DataSet)pageContext.getAttribute("displayedDatasets");
        }
        if (needDIAnalystCertificationCheck) {
            displayedDataItems = (DataSet)pageContext.getAttribute("displayedDataItems");
        }
        if (this._debug) {
            this.logCertificationInputs(needDSAnalystCertificationCheck, needDIAnalystCertificationCheck, needInstrumentCertificationCheck, displayedSDIs, displayedDatasets, displayedDataItems);
        }
        if (needDSAnalystCertificationCheck) {
            ds = this.getAnalystCertification(displayedSDIs, displayedDatasets, pageContext, "s_assignedanalyst", "dataset");
            ds = this.validateAnalystFunction(ds);
            certificationArray = this.getJsArray(ds, "IsValidAnalyst", "s_assignedanalyst", "ds", "certificationValidationArray");
            returnSb.append(certificationArray);
        }
        if (needDIAnalystCertificationCheck) {
            ds = this.getAnalystCertification(displayedSDIs, displayedDataItems, pageContext, currentUser, "dataitem");
            ds = this.validateDIAnalystFunction(ds);
            certificationArray = this.getDIJsArray(ds, "userCertifications");
            returnSb.append(certificationArray);
        }
        if (needInstrumentCertificationCheck) {
            certificationArray = this.getInstrumentCertification(displayedSDIs, displayedDatasets, pageContext, "instrumentCertificationValidationArray");
            returnSb.append(certificationArray);
        }
        return returnSb.toString();
    }

    public DataSet getAnalystCertification(DataSet displayedSDIs, DataSet displayedDatasets, PageContext pageContext, String analystColumnName, String secondaryInfo) {
        int count;
        int primaryCount = displayedSDIs.getRowCount();
        int datasetCount = displayedDatasets.getRowCount();
        int row = -1;
        boolean isTrained = false;
        String sampleId = null;
        String paramlistId = null;
        String paramlistversionId = null;
        String variantId = null;
        String filteredParamlistversionId = null;
        String filteredVaraintId = null;
        String assignedAnalystId = null;
        String trainingReqFlag = null;
        String overrideAllowedFlag = null;
        String inClauseSampleIds = null;
        String inClauseParamListIds = null;
        String inClauseAssignedAnalystIds = null;
        DataSet primaryInfoDS = null;
        DataSet paramlistInfoDS = null;
        DataSet certificationInfoDS = null;
        DataSet filteredParamListInfoDS = null;
        DataSet filteredCertificationInfoDS = null;
        Iterator it = null;
        HashMap<String, String> hmProps = null;
        TreeSet<String> distinctDisplayedSDIs = new TreeSet<String>();
        TreeSet<String> distinctDisplayedParamListIds = new TreeSet<String>();
        TreeSet<String> distinctDisplayedAssignedAnalystIds = new TreeSet<String>();
        QueryProcessor queryProcessor = new QueryProcessor(pageContext);
        for (count = 0; count < primaryCount; ++count) {
            sampleId = displayedSDIs.getValue(count, "s_sampleid");
            distinctDisplayedSDIs.add(sampleId);
        }
        for (count = 0; count < datasetCount; ++count) {
            paramlistId = displayedDatasets.getValue(count, "paramlistid");
            if (secondaryInfo.equalsIgnoreCase("dataset") && (assignedAnalystId = displayedDatasets.getValue(count, analystColumnName)) != null && assignedAnalystId.trim().length() > 0) {
                distinctDisplayedAssignedAnalystIds.add(assignedAnalystId);
            }
            distinctDisplayedParamListIds.add(paramlistId);
        }
        if (secondaryInfo.equalsIgnoreCase("dataitem")) {
            distinctDisplayedAssignedAnalystIds.add(analystColumnName);
        }
        if (this._debug) {
            Trace.logDebug("Distinct SDI(s) size: " + distinctDisplayedSDIs.size());
            Trace.logDebug("Treeset distinct SDI(s): " + distinctDisplayedSDIs);
            Trace.logDebug("Distinct Paramlist size: " + distinctDisplayedParamListIds.size());
            Trace.logDebug("Treeset distinct paramlists: " + distinctDisplayedParamListIds);
            Trace.logDebug("Distinct assigned analysts size: " + distinctDisplayedAssignedAnalystIds.size());
            Trace.logDebug("Treeset distinct assigned analysts: " + distinctDisplayedAssignedAnalystIds);
        }
        if (distinctDisplayedSDIs.size() == 0 || distinctDisplayedParamListIds.size() == 0 || distinctDisplayedAssignedAnalystIds.size() == 0) {
            return null;
        }
        it = distinctDisplayedSDIs.iterator();
        inClauseSampleIds = this.getInclauseString(it);
        it = distinctDisplayedParamListIds.iterator();
        inClauseParamListIds = this.getInclauseString(it);
        it = distinctDisplayedAssignedAnalystIds.iterator();
        inClauseAssignedAnalystIds = this.getInclauseString(it);
        try {
            SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator(pageContext);
            if (sqlGenerator == null) {
                throw new Exception("Couldnot get SQLGenerator. Unsupported database.");
            }
            SafeSQL primaryInfoSQLStmt = sqlGenerator.getSampleDetails(inClauseSampleIds);
            SafeSQL paramlistInfoSQLStmt = sqlGenerator.getParamlistDetails(inClauseParamListIds);
            SafeSQL certificationInfoSQLStmt = sqlGenerator.getUserCertificationDetails(inClauseAssignedAnalystIds, inClauseParamListIds);
            if (primaryInfoSQLStmt == null || primaryInfoSQLStmt.getPreparedSQL().length() == 0 || paramlistInfoSQLStmt == null || paramlistInfoSQLStmt.getPreparedSQL().length() == 0 || certificationInfoSQLStmt == null || certificationInfoSQLStmt.getPreparedSQL().length() == 0) {
                throw new Exception("This feature is not supported on this database.");
            }
            primaryInfoDS = queryProcessor.getPreparedSqlDataSet(primaryInfoSQLStmt.getPreparedSQL(), primaryInfoSQLStmt.getValues());
            paramlistInfoDS = queryProcessor.getPreparedSqlDataSet(paramlistInfoSQLStmt.getPreparedSQL(), paramlistInfoSQLStmt.getValues());
            certificationInfoDS = queryProcessor.getPreparedSqlDataSet(certificationInfoSQLStmt.getPreparedSQL(), certificationInfoSQLStmt.getValues());
        }
        catch (Exception ex) {
            Trace.logError(ex.getMessage(), ex);
            return displayedDatasets;
        }
        if (this._debug) {
            if (primaryInfoDS != null) {
                Trace.logDebug("Primary Info DS count : " + primaryInfoDS.getRowCount());
                Trace.logDebug("Primary DS: " + primaryInfoDS);
            }
            if (paramlistInfoDS != null) {
                Trace.logDebug("ParamListInfo DS count : " + paramlistInfoDS.getRowCount());
                Trace.logDebug("ParamListInfo DS: " + paramlistInfoDS);
            }
            if (certificationInfoDS != null) {
                Trace.logDebug("Certification DS count : " + certificationInfoDS.getRowCount());
                Trace.logDebug("Certification DS: " + certificationInfoDS);
            }
        }
        try {
            boolean isColumnAdded = false;
            isColumnAdded = displayedDatasets.addColumn("IsUserAssigned", 0);
            isColumnAdded = isColumnAdded && displayedDatasets.addColumn("CSFlag", 0);
            isColumnAdded = isColumnAdded && displayedDatasets.addColumn("CSCertFlag", 0);
            isColumnAdded = isColumnAdded && displayedDatasets.addColumn("TrainingReqFlag", 0);
            isColumnAdded = isColumnAdded && displayedDatasets.addColumn("ACCertFlag", 0);
            boolean bl = isColumnAdded = isColumnAdded && displayedDatasets.addColumn("ACOverrideAllowedFlag", 0);
            if (!isColumnAdded) {
                Trace.logDebug("Couldnot add column to the dataset");
                throw new Exception("Analyst assignment validation: Couldnot add columns");
            }
            for (int count2 = 0; count2 < datasetCount; ++count2) {
                assignedAnalystId = secondaryInfo.equalsIgnoreCase("dataset") ? displayedDatasets.getValue(count2, analystColumnName) : analystColumnName;
                paramlistId = displayedDatasets.getValue(count2, "paramlistid");
                paramlistversionId = displayedDatasets.getValue(count2, "paramlistversionid");
                variantId = displayedDatasets.getValue(count2, "variantid");
                hmProps = new HashMap<String, String>();
                hmProps.put("paramlistid", paramlistId);
                hmProps.put("paramlistversionid", paramlistversionId);
                hmProps.put("variantid", variantId);
                filteredParamListInfoDS = paramlistInfoDS.getFilteredDataSet(hmProps);
                if (filteredParamListInfoDS.getRowCount() == 0) {
                    Trace.logDebug("Analyst Assignment -- > ParamList doesnot exist ");
                    Trace.logDebug("ParamlistId:" + paramlistId + " ParamListVersionId: " + paramlistversionId + " VariantId: " + variantId);
                    continue;
                }
                trainingReqFlag = filteredParamListInfoDS.getValue(0, "S_TRAININGREQFLAG", "N");
                overrideAllowedFlag = filteredParamListInfoDS.getValue(0, "S_OVERRIDEALLOWEDFLAG", "Y");
                displayedDatasets.setString(count2, "TrainingReqFlag", trainingReqFlag);
                displayedDatasets.setString(count2, "ACOverrideAllowedFlag", overrideAllowedFlag);
                if (assignedAnalystId == null || assignedAnalystId.trim().length() == 0) {
                    displayedDatasets.setString(count2, "IsUserAssigned", "N");
                    continue;
                }
                displayedDatasets.setString(count2, "IsUserAssigned", "Y");
                hmProps = new HashMap();
                hmProps.put("s_sampleid", displayedDatasets.getValue(count2, "keyid1"));
                row = primaryInfoDS.findRow(hmProps);
                if (row >= 0) {
                    displayedDatasets.setString(count2, "CSFlag", "Y");
                } else {
                    displayedDatasets.setString(count2, "CSFlag", "N");
                }
                if (assignedAnalystId == null || assignedAnalystId.trim().length() == 0) continue;
                if (row >= 0) {
                    hmProps = new HashMap();
                    hmProps.put("certificationtype", "Control Substance");
                    hmProps.put("resourcesdcid", "User");
                    hmProps.put("resourcekeyid1", assignedAnalystId);
                    row = certificationInfoDS.findRow(hmProps);
                    if (row == -1) {
                        displayedDatasets.setString(count2, "CSCertFlag", "N");
                    } else {
                        displayedDatasets.setString(count2, "CSCertFlag", "Y");
                    }
                }
                if (!trainingReqFlag.equals("Y")) continue;
                isTrained = false;
                hmProps = new HashMap();
                hmProps.put("certificationtype", "Analyst Training");
                hmProps.put("resourcesdcid", "User");
                hmProps.put("resourcekeyid1", assignedAnalystId);
                hmProps.put("certifiedforsdcid", "ParamList");
                hmProps.put("certifiedforkeyid1", paramlistId);
                filteredCertificationInfoDS = certificationInfoDS.getFilteredDataSet(hmProps);
                for (int i = 0; i < filteredCertificationInfoDS.getRowCount(); ++i) {
                    filteredParamlistversionId = filteredCertificationInfoDS.getValue(i, "CERTIFIEDFORKEYID2");
                    filteredVaraintId = filteredCertificationInfoDS.getValue(i, "CERTIFIEDFORKEYID3");
                    if (filteredParamlistversionId != null && filteredParamlistversionId.equalsIgnoreCase("(null)") && filteredVaraintId != null && filteredVaraintId.equalsIgnoreCase("(null)")) {
                        isTrained = true;
                        break;
                    }
                    if (filteredParamlistversionId != null && filteredParamlistversionId.equalsIgnoreCase("(null)") && variantId.equals(filteredVaraintId)) {
                        isTrained = true;
                        break;
                    }
                    if (filteredVaraintId != null && filteredVaraintId.equalsIgnoreCase("(null)") && paramlistversionId.equals(filteredParamlistversionId)) {
                        isTrained = true;
                        break;
                    }
                    if (!paramlistversionId.equals(filteredParamlistversionId) || !variantId.equals(filteredVaraintId)) continue;
                    isTrained = true;
                    break;
                }
                if (isTrained) {
                    displayedDatasets.setString(count2, "ACCertFlag", "Y");
                    continue;
                }
                displayedDatasets.setString(count2, "ACCertFlag", "N");
            }
            if (this._debug) {
                Trace.logDebug("After analyst validation (displayed datasets) : " + displayedDatasets);
            }
        }
        catch (Exception ex) {
            Trace.logError("Exception caught: " + ex, ex);
        }
        return displayedDatasets;
    }

    public String getInstrumentCertification(DataSet displayedSDIs, DataSet displayedDatasets, PageContext pageContext, String jsArrayName) {
        StringBuffer returnSb = new StringBuffer();
        int datasetCount = displayedDatasets.getRowCount();
        int row = -1;
        String assignedInstrumentId = null;
        String paramlistId = null;
        String paramlistversionId = null;
        String variantId = null;
        String instrumentType = null;
        String inClauseParamListIds = null;
        String inClauseAssignedInstrumentIds = null;
        String sbCertificationValidationArrayString = null;
        Iterator it = null;
        HashMap<String, String> hmProps = null;
        DataSet paramlistInfoDS = null;
        DataSet instrumentInfoDS = null;
        DataSet instrumentCertificationInfoDS = null;
        DataSet filteredParamListInfoDS = null;
        TreeSet<String> distinctDisplayedParamListIds = new TreeSet<String>();
        TreeSet<String> distinctDisplayedAssignedInstruments = new TreeSet<String>();
        QueryProcessor queryProcessor = new QueryProcessor(pageContext);
        for (int count = 0; count < datasetCount; ++count) {
            paramlistId = displayedDatasets.getValue(count, "paramlistid");
            distinctDisplayedParamListIds.add(paramlistId);
            assignedInstrumentId = displayedDatasets.getValue(count, "s_instrumentid");
            if (assignedInstrumentId == null || assignedInstrumentId.trim().length() <= 0) continue;
            distinctDisplayedAssignedInstruments.add(assignedInstrumentId);
        }
        if (distinctDisplayedParamListIds.size() == 0 || distinctDisplayedAssignedInstruments.size() == 0) {
            return returnSb.toString();
        }
        it = distinctDisplayedParamListIds.iterator();
        inClauseParamListIds = this.getInclauseString(it);
        it = distinctDisplayedAssignedInstruments.iterator();
        inClauseAssignedInstrumentIds = this.getInclauseString(it);
        try {
            SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator(pageContext);
            if (sqlGenerator == null) {
                throw new Exception("Couldnot get SQLGenerator. Unsupported database.");
            }
            SafeSQL instrumentInfoSQLStmt = sqlGenerator.getInstrumentDetails(inClauseAssignedInstrumentIds);
            SafeSQL paramlistInfoSQLStmt = sqlGenerator.getParamlistDetails(inClauseParamListIds);
            SafeSQL instrumentCertificationInfoSQLStmt = sqlGenerator.getInstrumentCertificationDetails(inClauseAssignedInstrumentIds);
            if (instrumentInfoSQLStmt == null || instrumentInfoSQLStmt.getPreparedSQL().length() == 0 || paramlistInfoSQLStmt == null || paramlistInfoSQLStmt.getPreparedSQL().length() == 0 || instrumentCertificationInfoSQLStmt == null || instrumentCertificationInfoSQLStmt.getPreparedSQL().length() == 0) {
                throw new Exception("This feature is not supported on this database.");
            }
            instrumentInfoDS = queryProcessor.getPreparedSqlDataSet(instrumentInfoSQLStmt.getPreparedSQL(), instrumentInfoSQLStmt.getValues());
            paramlistInfoDS = queryProcessor.getPreparedSqlDataSet(paramlistInfoSQLStmt.getPreparedSQL(), paramlistInfoSQLStmt.getValues());
            instrumentCertificationInfoDS = queryProcessor.getPreparedSqlDataSet(instrumentCertificationInfoSQLStmt.getPreparedSQL(), instrumentCertificationInfoSQLStmt.getValues());
        }
        catch (Exception sqlGenerator) {
            // empty catch block
        }
        if (this._debug) {
            if (instrumentInfoDS != null) {
                Trace.logDebug("Instrument DS count : " + instrumentInfoDS.getRowCount());
                Trace.logDebug("Instrument DS: " + instrumentInfoDS);
            }
            if (paramlistInfoDS != null) {
                Trace.logDebug("ParamListInfo DS count : " + paramlistInfoDS.getRowCount());
                Trace.logDebug("ParamListInfo DS: " + paramlistInfoDS);
            }
            if (instrumentCertificationInfoDS != null) {
                Trace.logDebug("Instrument Certification DS count : " + instrumentCertificationInfoDS.getRowCount());
                Trace.logDebug("Instrument Certification DS: " + instrumentCertificationInfoDS);
            }
        }
        try {
            boolean isColumnAdded = displayedDatasets.addColumn("IsInstrumentValid", 0);
            if (!isColumnAdded) {
                Trace.logDebug("Couldnot add column to the dataset");
                throw new Exception("Instrument validation: Couldnot add column to the dataset");
            }
            for (int count = 0; count < datasetCount; ++count) {
                assignedInstrumentId = displayedDatasets.getValue(count, "s_instrumentid");
                if (assignedInstrumentId == null || assignedInstrumentId.trim().length() == 0) continue;
                paramlistId = displayedDatasets.getValue(count, "paramlistid");
                paramlistversionId = displayedDatasets.getValue(count, "paramlistversionid");
                variantId = displayedDatasets.getValue(count, "variantid");
                hmProps = new HashMap<String, String>();
                hmProps.put("paramlistid", paramlistId);
                hmProps.put("paramlistversionid", paramlistversionId);
                hmProps.put("variantid", variantId);
                filteredParamListInfoDS = paramlistInfoDS.getFilteredDataSet(hmProps);
                if (filteredParamListInfoDS == null || filteredParamListInfoDS.getRowCount() == 0) {
                    Trace.logDebug("Instrument Assignment -- > ParamList doesnot exist ");
                    Trace.logDebug("ParamlistId:" + paramlistId + " ParamListVersionId: " + paramlistversionId + " VariantId: " + variantId);
                    displayedDatasets.setString(count, "IsInstrumentValid", "N");
                    continue;
                }
                instrumentType = filteredParamListInfoDS.getValue(0, "S_INSTRUMENTTYPE");
                hmProps.clear();
                hmProps.put("resourcekeyid1", assignedInstrumentId);
                if (instrumentType != null && instrumentType.trim().length() > 0) {
                    hmProps.put("instrumenttype", instrumentType);
                }
                if ((row = instrumentCertificationInfoDS.findRow(hmProps)) == -1) {
                    hmProps.clear();
                    hmProps.put("instrumentid", assignedInstrumentId);
                    if (instrumentType != null && instrumentType.trim().length() > 0) {
                        hmProps.put("instrumenttype", instrumentType);
                    }
                    hmProps.put("overrideallowedflag", "Y");
                    row = instrumentInfoDS.findRow(hmProps);
                    if (row == -1) {
                        displayedDatasets.setString(count, "IsInstrumentValid", "N");
                        continue;
                    }
                    displayedDatasets.setString(count, "IsInstrumentValid", "O");
                    continue;
                }
                displayedDatasets.setString(count, "IsInstrumentValid", "Y");
            }
            sbCertificationValidationArrayString = this.getJsArray(displayedDatasets, "IsInstrumentValid", "s_instrumentid", "ds", "instrumentCertificationValidationArray");
            returnSb.append(sbCertificationValidationArrayString);
        }
        catch (Exception ex) {
            Trace.logError("Exception caught: " + ex, ex);
        }
        return returnSb.toString();
    }

    public String getInclauseString(Iterator it) {
        String inClauseString = new String();
        boolean isFirst = true;
        while (it.hasNext()) {
            if (isFirst) {
                inClauseString = " '" + it.next() + "' ";
                isFirst = false;
                continue;
            }
            inClauseString = inClauseString + ", '" + it.next() + "' ";
        }
        return inClauseString;
    }

    public boolean isColumnPresent(PropertyListCollection columnListCollection, String searchColumnName) {
        boolean retflag = false;
        int columnCount = 0;
        if (this._debug) {
            Trace.logDebug("isColumnPresent --> columnListCollection: " + columnListCollection);
            Trace.logDebug("isColumnPresent --> searchColumnName: " + searchColumnName);
        }
        if (searchColumnName == null || searchColumnName.trim().length() == 0) {
            Trace.logDebug("isColumnPresent -->Search column is empty. Returning false");
            return retflag;
        }
        if (columnListCollection != null) {
            columnCount = columnListCollection.size();
            for (int count = 0; count < columnCount; ++count) {
                PropertyList columnProps = columnListCollection.getPropertyList(count);
                String columnName = columnProps.getProperty("columnid");
                if (columnName == null || !columnName.equalsIgnoreCase(searchColumnName)) continue;
                retflag = true;
                break;
            }
        }
        if (this._debug) {
            Trace.logDebug("isColumnPresent --> return: " + retflag);
        }
        return retflag;
    }

    public String getJsArray(DataSet ds, String columnName, String certificationColumnName, String prefix, String jsArrayName) {
        StringBuffer jsArraySb = new StringBuffer();
        if (ds == null) {
            return jsArraySb.toString();
        }
        int datasetCount = ds.getRowCount();
        if (datasetCount > 0) {
            jsArraySb.append("<script language=javascript > \n ");
            jsArraySb.append(jsArrayName).append(" = new Array(); \n");
        }
        for (int count = 0; count < datasetCount; ++count) {
            String fieldId = prefix + count + "_" + certificationColumnName;
            String isValid = ds.getValue(count, columnName);
            jsArraySb.append(jsArrayName);
            jsArraySb.append("[" + count + "] = new Array('" + fieldId + "', '" + isValid + "'); \n ");
        }
        if (datasetCount > 0) {
            jsArraySb.append("</script> \n");
        }
        return jsArraySb.toString();
    }

    public DataSet validateAnalystFunction(DataSet ds) {
        int datasetCount = 0;
        if (ds == null) {
            return null;
        }
        datasetCount = ds.getRowCount();
        boolean isColumnAdded = false;
        isColumnAdded = ds.addColumn("IsValidAnalyst", 0);
        if (!isColumnAdded) {
            Trace.logDebug("validateAnalystFunction : IsValidAnalyst column couldnot be added");
            return ds;
        }
        for (int count = 0; count < datasetCount; ++count) {
            String isValidAnalystFlag = "N";
            String csFlag = ds.getValue(count, "CSFlag");
            String csCertFlag = ds.getValue(count, "CSCertFlag");
            String trainingReqFlag = ds.getValue(count, "TrainingReqFlag");
            String acCertFlag = ds.getValue(count, "ACCertFlag");
            String acOverrideAllowedFlag = ds.getValue(count, "ACOverrideAllowedFlag");
            String isUserAssigned = ds.getValue(count, "IsUserAssigned");
            if (isUserAssigned == null || !isUserAssigned.equalsIgnoreCase("Y")) continue;
            if (csFlag.equalsIgnoreCase("Y") && !csCertFlag.equalsIgnoreCase("Y")) {
                isValidAnalystFlag = "N";
            } else if (trainingReqFlag.equalsIgnoreCase("Y")) {
                if (acCertFlag.equalsIgnoreCase("Y")) {
                    isValidAnalystFlag = "Y";
                } else if (acOverrideAllowedFlag.equalsIgnoreCase("Y")) {
                    isValidAnalystFlag = "O";
                }
            } else {
                isValidAnalystFlag = "Y";
            }
            ds.setString(count, "IsValidAnalyst", isValidAnalystFlag);
        }
        return ds;
    }

    public DataSet validateDIAnalystFunction(DataSet ds) {
        int datasetCount = 0;
        if (ds == null) {
            return null;
        }
        datasetCount = ds.getRowCount();
        boolean isColumnAdded = false;
        isColumnAdded = ds.addColumn("IsValidAnalyst", 0);
        isColumnAdded = isColumnAdded && ds.addColumn("CanTestSample", 0);
        boolean bl = isColumnAdded = isColumnAdded && ds.addColumn("CanTestPL", 0);
        if (!isColumnAdded) {
            Trace.logDebug("validateDIAnalystFunction:  Columns couldnot be added. Returning without setting flags.");
            return ds;
        }
        for (int count = 0; count < datasetCount; ++count) {
            String isValidAnalystFlag = "N";
            String csFlag = ds.getValue(count, "CSFlag");
            String csCertFlag = ds.getValue(count, "CSCertFlag");
            String trainingReqFlag = ds.getValue(count, "TrainingReqFlag");
            String acCertFlag = ds.getValue(count, "ACCertFlag");
            String acOverrideAllowedFlag = ds.getValue(count, "ACOverrideAllowedFlag");
            String isUserAssigned = ds.getValue(count, "IsUserAssigned");
            if (isUserAssigned == null || !isUserAssigned.equalsIgnoreCase("Y")) continue;
            if (csFlag.equalsIgnoreCase("Y") && csCertFlag.equalsIgnoreCase("Y") || csFlag.equalsIgnoreCase("N")) {
                ds.setString(count, "CanTestSample", "Y");
            } else {
                ds.setString(count, "CanTestSample", "N");
            }
            if (trainingReqFlag.equalsIgnoreCase("Y") && acCertFlag.equalsIgnoreCase("N")) {
                ds.setString(count, "CanTestPL", "N");
            } else {
                ds.setString(count, "CanTestPL", "Y");
            }
            if (csFlag.equalsIgnoreCase("Y") && !csCertFlag.equalsIgnoreCase("Y")) {
                isValidAnalystFlag = "N";
            } else if (trainingReqFlag.equalsIgnoreCase("Y")) {
                if (acCertFlag.equalsIgnoreCase("Y")) {
                    isValidAnalystFlag = "Y";
                } else if (acOverrideAllowedFlag.equalsIgnoreCase("Y")) {
                    isValidAnalystFlag = "O";
                }
            } else {
                isValidAnalystFlag = "Y";
            }
            ds.setString(count, "IsValidAnalyst", isValidAnalystFlag);
        }
        return ds;
    }

    public String getDIJsArray(DataSet ds, String jsArrayName) {
        StringBuffer jsArraySb = new StringBuffer();
        if (ds == null) {
            return jsArraySb.toString();
        }
        int datasetCount = ds.getRowCount();
        if (datasetCount > 0) {
            jsArraySb.append("<script language=javascript > \n ");
            jsArraySb.append(jsArrayName).append(" = new Array(); \n ");
            jsArraySb.append("\t\t// dataitemrowid, canTestSample, canTestPL,");
            jsArraySb.append("canOverride \n");
        }
        for (int count = 0; count < datasetCount; ++count) {
            String rowid = ds.getValue(count, "__ROWID");
            String canTestSample = ds.getValue(count, "CanTestSample");
            String canTestPL = ds.getValue(count, "CanTestPL");
            String canOverride = ds.getValue(count, "ACOverrideAllowedFlag");
            String keyid1 = ds.getValue(count, "keyid1");
            String paramlistid = ds.getValue(count, "paramlistid");
            String paramlistversionid = ds.getValue(count, "paramlistversionid");
            String variantid = ds.getValue(count, "variantid");
            String dataset = ds.getValue(count, "dataset");
            String paramid = ds.getValue(count, "paramid");
            String replicateid = ds.getValue(count, "replicateid");
            String key = "count=" + count + " rowid=" + rowid + "  " + keyid1 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset + ";" + paramid + ";" + replicateid;
            jsArraySb.append("\t\t" + jsArrayName);
            jsArraySb.append("[" + count + "] = new Array('" + rowid + "', '" + canTestSample + "', '" + canTestPL + "','" + canOverride + "' ); // " + key + " \n");
        }
        if (datasetCount > 0) {
            jsArraySb.append("</script> \n");
        }
        return jsArraySb.toString();
    }

    private void logCertificationInputs(boolean needDSAnalystCertificationCheck, boolean needDIAnalystCertificationCheck, boolean needInstrumentCertificationCheck, DataSet displayedSDIs, DataSet displayedDatasets, DataSet displayedDataItems) {
        if (this._debug) {
            int count;
            Trace.logDebug("needDSAnalystCertificationCheck: " + needDSAnalystCertificationCheck);
            Trace.logDebug("needDIAnalystCertificationCheck: " + needDIAnalystCertificationCheck);
            Trace.logDebug("needInstrumentCertificationCheck: " + needInstrumentCertificationCheck);
            if (displayedSDIs != null) {
                Trace.logDebug("getCertificationArrays ----> Displayed SDI count: " + displayedSDIs.size());
                Trace.logDebug("getCertificationArrays ----> Displayed SDI(s): ");
                Trace.logDebug(displayedSDIs);
                StringBuffer sbDisplayedPrimaryColumns = new StringBuffer();
                String[] displayedPrimaryColumns = displayedSDIs.getColumns();
                for (count = 0; count < displayedPrimaryColumns.length; ++count) {
                    sbDisplayedPrimaryColumns.append(displayedPrimaryColumns[count] + " ");
                }
                Trace.logDebug("Displayed primary columns: " + sbDisplayedPrimaryColumns);
            }
            if (displayedDatasets != null) {
                Trace.logDebug("getCertificationArrays ----> Displayed Dataset count: " + displayedDatasets.size());
                Trace.logDebug("getCertificationArrays ----> Displayed Dataset(s): ");
                Trace.logDebug(displayedDatasets);
                StringBuffer sbDispalyedDatasetColumns = new StringBuffer();
                String[] displayedDatasetColumns = displayedDatasets.getColumns();
                for (count = 0; count < displayedDatasetColumns.length; ++count) {
                    sbDispalyedDatasetColumns.append(displayedDatasetColumns[count] + " ");
                }
                Trace.logDebug("Displayed dataset columns: " + sbDispalyedDatasetColumns);
            }
            if (displayedDataItems != null) {
                Trace.logDebug("getCertificationArrays ----> Displayed DataItem count: " + displayedDataItems.size());
                Trace.logDebug("getCertificationArrays ----> Displayed displayedDataItems(s): ");
                Trace.logDebug(displayedDataItems);
                StringBuffer sbDispalyedDataItemColumns = new StringBuffer();
                String[] displayedDataItemColumns = displayedDataItems.getColumns();
                for (count = 0; count < displayedDataItemColumns.length; ++count) {
                    sbDispalyedDataItemColumns.append(displayedDataItemColumns[count] + " ");
                }
                Trace.logDebug("Displayed dataset columns: " + sbDispalyedDataItemColumns);
            }
        }
    }
}

