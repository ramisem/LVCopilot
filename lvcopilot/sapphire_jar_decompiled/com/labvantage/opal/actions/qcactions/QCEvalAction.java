/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCBaseAction;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchEvalRule;
import com.labvantage.opal.qcbatch.QCBatchParamSet;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.stats.Stats;
import com.labvantage.opal.stats.exception.RuleException;
import com.labvantage.opal.stats.rule.PatternRule;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCEvalAction
extends QCBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53519 $";
    private int rc = 1;
    private SQLGenerator __SqlGenerator;
    private PropertyList __SpecInterpretation = new PropertyList();

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        try {
            this.__SpecInterpretation = OpalUtil.getSpecInterpretationMap(this.getConfigurationProcessor());
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
        }
        if (actionid.equals("QCEvalAction")) {
            return this.rc;
        }
        String qcBatchId = (String)props.get("qcbatchid");
        String qcBatchSampleTypeId = (String)props.get("qcbatchsampletypeid");
        if (qcBatchId != null && qcBatchId.length() > 0 && qcBatchSampleTypeId != null && qcBatchSampleTypeId.length() > 0) {
            if (actionid.equals("QCRuleEvaluation")) {
                this.evaluateQCBatchSampleTypeRuleSpec(qcBatchSampleTypeId, qcBatchId);
            } else if (actionid.equals("QCSpecRuleEvaluation")) {
                this.evaluateQCBatchSampleTypeRuleSpec(qcBatchSampleTypeId, qcBatchId);
            }
        }
        return this.rc;
    }

    public static boolean runQCValidation(String data, String target, String sd, String ucl, String lcl, String violationCount, String windowSize, String sigmaAboveCL, String sigmaBelowCL, String rulePattern, String withinLimits, String[] arrDataPointsResult) throws SapphireException, RuleException {
        String datadelimiter = ";";
        String doNotCalculate = "DONOTCALCULATE";
        String ascending = "Ascending";
        String descending = "Descending";
        String alternating = "Alternating";
        String difference = "Difference";
        String sameSide = "SameSide";
        String[] rundata = null;
        Stats stats = null;
        PatternRule rule = new PatternRule(1);
        if (data != null && data.length() > 0) {
            rundata = StringUtil.split(data, ";");
            if (rundata.length < 1) {
                throw new SapphireException("Not enough data points.");
            }
        } else {
            throw new SapphireException("Invalid input (data)");
        }
        if (sigmaAboveCL != null && sigmaAboveCL.length() > 0) {
            if (sigmaAboveCL.equalsIgnoreCase("DONOTCALCULATE")) {
                rule.setSigmaAboveCL(9.99999999E8);
            } else {
                try {
                    rule.setSigmaAboveCL(Double.parseDouble(sigmaAboveCL));
                }
                catch (NumberFormatException e) {
                    throw new SapphireException("Invalid input (Sigma Above CL => " + sigmaAboveCL + ")");
                }
            }
        }
        if (sigmaBelowCL != null && sigmaBelowCL.length() > 0) {
            if (sigmaBelowCL.equalsIgnoreCase("DONOTCALCULATE")) {
                rule.setSigmaBelowCL(9.99999999E8);
            } else {
                try {
                    rule.setSigmaBelowCL(Double.parseDouble(sigmaBelowCL));
                }
                catch (NumberFormatException e) {
                    throw new SapphireException("Invalid input (Sigma Below CL => " + sigmaBelowCL + ")");
                }
            }
        }
        if (violationCount != null && violationCount.length() > 0) {
            try {
                rule.setTriggerSubgroup(Integer.parseInt(violationCount));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (Violation Count => " + violationCount + ")");
            }
        }
        if (windowSize != null && windowSize.length() > 0) {
            try {
                double dblWindowSize = Double.parseDouble(windowSize);
                int intWindowSize = new Double(dblWindowSize).intValue();
                rule.setTotalSubgroup(intWindowSize);
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (Violation Count => " + violationCount + ")");
            }
        }
        if (withinLimits != null && withinLimits.equalsIgnoreCase("Y")) {
            rule.setTriggerlocation(0);
            rule.setWithinlimits(true);
        } else {
            rule.setTriggerlocation(1);
            rule.setWithinlimits(false);
        }
        if (rulePattern == null || rulePattern.equals("None")) {
            rule.setRulePattern(0);
        } else if (rulePattern.equals("Ascending")) {
            rule.setRulePattern(1);
        } else if (rulePattern.equals("Descending")) {
            rule.setRulePattern(2);
        } else if (rulePattern.equals("Alternating")) {
            rule.setRulePattern(3);
        } else if (rulePattern.equals("Difference")) {
            rule.setRulePattern(4);
        } else if (rulePattern.equals("SameSide")) {
            rule.setRulePattern(5);
        }
        try {
            stats = new Stats(data, ";");
        }
        catch (NumberFormatException e) {
            throw new SapphireException("Invalid data point. (" + e.getMessage() + ")");
        }
        if (target != null && target.length() > 0) {
            try {
                stats.setCL(Double.parseDouble(target));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (Target => " + target + ")");
            }
        }
        if (sd != null && sd.length() > 0) {
            try {
                stats.setSD(Double.parseDouble(sd));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (SD => " + sd + ")");
            }
        }
        if (ucl != null && ucl.length() > 0) {
            try {
                stats.setUCL(Double.parseDouble(ucl));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (UCL => " + ucl + ")");
            }
        }
        if (lcl != null && lcl.length() > 0) {
            try {
                stats.setLCL(Double.parseDouble(lcl));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid input (LCL => " + lcl + ")");
            }
        }
        stats.addRule(rule);
        stats.validate(arrDataPointsResult);
        Logger.logInfo("=============================================================================");
        Logger.logInfo("Rule Pattern: " + rulePattern);
        Logger.logInfo("Window Size: " + windowSize);
        Logger.logInfo("Violation Count: " + violationCount);
        Logger.logInfo("Sigma Above CL: " + sigmaAboveCL);
        Logger.logInfo("Sigma Below CL: " + sigmaBelowCL);
        Logger.logInfo("Within Limits: " + withinLimits);
        Logger.logInfo("Data Points: " + stats.getResultlist());
        Logger.logInfo("Min: " + stats.getMin());
        Logger.logInfo("Max: " + stats.getMax());
        Logger.logInfo("CL: " + stats.getCL());
        Logger.logInfo("Calculated Target: " + stats.getMean());
        Logger.logInfo("SD: " + stats.getSD());
        Logger.logInfo("Failed Patterns: " + stats.getFailedPatterns());
        Logger.logInfo("=============================================================================");
        return stats.getFailedPatterns() == null;
    }

    private void evaluateQCBatchSampleTypeRuleSpec(String qcBatchSampleTypeId, String keys) {
        if (qcBatchSampleTypeId == null || keys == null) {
            this.logger.debug("Invalid input.");
            return;
        }
        try {
            QueryProcessor qp = this.getQueryProcessor();
            StringBuffer sql = new StringBuffer();
            StringBuffer sb = new StringBuffer();
            StringBuffer batchitemids = new StringBuffer();
            StringBuffer batchids = new StringBuffer();
            QCBatch qcBatch = new QCBatch(qp);
            QCBatchSampleType qcBatchSampleType = new QCBatchSampleType(qp, qcBatchSampleTypeId);
            qcBatch.setQCBatchID(qcBatchSampleType.getColumnValue("qcbatchid"));
            int qcBatchQueryCount = qcBatch.getBatchQueryCount();
            String parameterType = qcBatchSampleType.getColumnValue("evaluateparamtype");
            String qcbatchSDCId = qcBatch.getQCBatchSDC();
            if (qcbatchSDCId == null || qcbatchSDCId.equals("")) {
                qcbatchSDCId = "Sample";
            }
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            String tableid = sdcProcessor.getProperty(qcbatchSDCId, "tableid");
            String keycolid1 = sdcProcessor.getProperty(qcbatchSDCId, "keycolid1");
            if (parameterType == null) {
                this.logger.error("Unable to find Parameter type for QCBatchSampleType: " + qcBatchSampleTypeId);
                return;
            }
            this.logger.debug("Got Parameter Type: " + parameterType);
            ArrayList<String> passQCBatchItemIdHolder = new ArrayList<String>();
            StringBuffer sbsdcid = new StringBuffer();
            StringBuffer sbkeyid1 = new StringBuffer();
            StringBuffer sbkeyid2 = new StringBuffer();
            StringBuffer sbkeyid3 = new StringBuffer();
            StringBuffer sbparamlistid = new StringBuffer();
            StringBuffer sbparamlistversionid = new StringBuffer();
            StringBuffer sbvariantid = new StringBuffer();
            StringBuffer sbdataset = new StringBuffer();
            StringBuffer sbreplicateid = new StringBuffer();
            StringBuffer sbparamid = new StringBuffer();
            StringBuffer sbparamtype = new StringBuffer();
            StringBuffer sbevalstatus = new StringBuffer();
            DataSet dsUpdateQCEvalRuleParam = new DataSet();
            dsUpdateQCEvalRuleParam.addColumn("s_qcbatchsampletypeid", 0);
            dsUpdateQCEvalRuleParam.addColumn("s_qcbatchevalruleid", 0);
            dsUpdateQCEvalRuleParam.addColumn("s_qcbatchparamsetid", 0);
            dsUpdateQCEvalRuleParam.addColumn("evalstatus", 0);
            List evalRuleList = qcBatchSampleType.getEvalRules();
            List paramSetList = qcBatchSampleType.getParamSet();
            DataSet dsParamSet = new DataSet();
            dsParamSet.addColumn("s_qcbatchparamsetid", 0);
            dsParamSet.addColumn("s_qcbatchsampletypeid", 0);
            dsParamSet.addColumn("paramid", 0);
            dsParamSet.addColumn("evalstatus", 0);
            dsParamSet.addColumn("speccondition", 0);
            HashMap<String, String> hmParamSetDetails = new HashMap<String, String>();
            for (int i = 0; i < paramSetList.size(); ++i) {
                QCBatchParamSet qcBatchParamSet = (QCBatchParamSet)paramSetList.get(i);
                String qcBatchParamSetId = qcBatchParamSet.getQCBatchParamSetId();
                String target = qcBatchParamSet.getTargetValue();
                String sd = qcBatchParamSet.getSD();
                String param = qcBatchParamSet.getParamId();
                hmParamSetDetails.put(param, qcBatchParamSetId + ";" + target + ";" + sd);
                int row = dsParamSet.addRow();
                dsParamSet.setString(row, "s_qcbatchsampletypeid", qcBatchSampleTypeId);
                dsParamSet.setString(row, "s_qcbatchparamsetid", qcBatchParamSetId);
                dsParamSet.setString(row, "paramid", param);
                dsParamSet.setString(row, "evalstatus", "");
                dsParamSet.setString(row, "speccondition", "");
            }
            DataSet dsQCBatchItems = new DataSet();
            dsQCBatchItems.addColumn("s_qcbatchid", 0);
            dsQCBatchItems.addColumn("s_qcbatchitemid", 0);
            dsQCBatchItems.addColumn("evaluationstatus", 0);
            dsQCBatchItems.addColumn("speccondition", 0);
            String mapKey = "";
            LinkedHashMap<String, Double> hmSumValue = new LinkedHashMap<String, Double>();
            HashMap<String, Integer> hmReplicateCount = new HashMap<String, Integer>();
            StringBuffer finalSb = new StringBuffer();
            if (evalRuleList != null && evalRuleList.size() > 0 && paramSetList != null && paramSetList.size() > 0) {
                if (qcBatchQueryCount < 0) {
                    qcBatchQueryCount = 10;
                }
                sql.setLength(0);
                keys = keys.replaceAll(";", "','");
                String currentBatchId = qcBatch.getQCBatchID();
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = qp.getPreparedSqlDataSet(this.__SqlGenerator.getQCDataPointsForEvaluation(qcBatchSampleTypeId, new Integer(qcBatchQueryCount).toString(), parameterType, keys, currentBatchId, false, qcbatchSDCId, tableid, keycolid1, safeSQL), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    this.logger.debug("No  Data Points found for evaluation of the QCBatchSampleType " + qcBatchSampleType);
                    return;
                }
                for (int i = 0; i < evalRuleList.size(); ++i) {
                    String currentRuleEvalStatus = "";
                    boolean valid = false;
                    QCBatchEvalRule qcBatchEvalRule = (QCBatchEvalRule)evalRuleList.get(i);
                    String qcBatchEvalRuleId = qcBatchEvalRule.getQCBatchEvalRuleId();
                    String violationCount = qcBatchEvalRule.getViolationCount();
                    String windowSize = qcBatchEvalRule.getWindowSize();
                    String sigmaAboveCL = qcBatchEvalRule.getSigmaAboveCL();
                    String sigmaBelowCL = qcBatchEvalRule.getSigmaBelowCL();
                    String rulePatternFlag = qcBatchEvalRule.getRulePatternFlag();
                    String insideLimitFlag = qcBatchEvalRule.getInsideLimitFlag();
                    String warningFlag = qcBatchEvalRule.getWarningFlag();
                    if (rulePatternFlag != null && !rulePatternFlag.trim().equals("")) {
                        if (rulePatternFlag.equals("A")) {
                            rulePatternFlag = "Ascending";
                        } else if (rulePatternFlag.equals("D")) {
                            rulePatternFlag = "Descending";
                        } else if (rulePatternFlag.equals("L")) {
                            rulePatternFlag = "Alternating";
                        } else if (rulePatternFlag.equals("I")) {
                            rulePatternFlag = "Difference";
                        } else if (rulePatternFlag.equals("S")) {
                            rulePatternFlag = "SameSide";
                        }
                    }
                    StringBuffer dataitemsdcid = new StringBuffer();
                    StringBuffer dataitemkeyid1 = new StringBuffer();
                    StringBuffer dataitemkeyid2 = new StringBuffer();
                    StringBuffer dataitemkeyid3 = new StringBuffer();
                    StringBuffer dataitemparamlistid = new StringBuffer();
                    StringBuffer dataitemparamlistversionid = new StringBuffer();
                    StringBuffer dataitemvariantid = new StringBuffer();
                    StringBuffer dataitemdataset = new StringBuffer();
                    StringBuffer dataitemparamid = new StringBuffer();
                    StringBuffer dataitemreplicateid = new StringBuffer();
                    for (int k = 0; k < ds.size(); ++k) {
                        String ucl = null;
                        String lcl = null;
                        String paramid = ds.getValue(k, "paramid");
                        String displayValue = ds.getValue(k, "transformvalue");
                        if (displayValue != null && displayValue.trim().length() > 0) {
                            sb.append(ds.getBigDecimal(k, "transformvalue")).append(";");
                            batchids.append(ds.getValue(k, "s_qcbatchid") + ";");
                            batchitemids.append(ds.getValue(k, "s_qcbatchitemid") + ";");
                            dataitemsdcid.append(ds.getValue(k, "sdcid") + ";");
                            dataitemkeyid1.append(ds.getValue(k, "keyid1") + ";");
                            dataitemkeyid2.append(ds.getValue(k, "keyid2") + ";");
                            dataitemkeyid3.append(ds.getValue(k, "keyid3") + ";");
                            dataitemparamlistid.append(ds.getValue(k, "paramlistid") + ";");
                            dataitemparamlistversionid.append(ds.getValue(k, "paramlistversionid") + ";");
                            dataitemvariantid.append(ds.getValue(k, "variantid") + ";");
                            dataitemdataset.append(ds.getValue(k, "dataset") + ";");
                            dataitemparamid.append(ds.getValue(k, "paramid") + ";");
                            dataitemreplicateid.append(ds.getValue(k, "replicateid") + ";");
                        }
                        if (k != ds.size() - 1 && ds.getValue(k + 1, "paramid").equals(paramid)) continue;
                        int currentBatchFailCtr = 0;
                        int currentBatchPassCtr = 0;
                        int currentBatchWarningCtr = 0;
                        if (sb.length() > 0 && hmParamSetDetails.containsKey(paramid)) {
                            try {
                                int idx;
                                sb.setLength(sb.length() - 1);
                                batchids.setLength(batchids.length() - 1);
                                batchitemids.setLength(batchitemids.length() - 1);
                                dataitemsdcid.setLength(dataitemsdcid.length() - 1);
                                dataitemkeyid1.setLength(dataitemkeyid1.length() - 1);
                                dataitemkeyid2.setLength(dataitemkeyid2.length() - 1);
                                dataitemkeyid3.setLength(dataitemkeyid3.length() - 1);
                                dataitemparamlistid.setLength(dataitemparamlistid.length() - 1);
                                dataitemparamlistversionid.setLength(dataitemparamlistversionid.length() - 1);
                                dataitemvariantid.setLength(dataitemvariantid.length() - 1);
                                dataitemdataset.setLength(dataitemdataset.length() - 1);
                                dataitemparamid.setLength(dataitemparamid.length() - 1);
                                dataitemreplicateid.setLength(dataitemreplicateid.length() - 1);
                                valid = false;
                                String[] arrParamSetDetails = StringUtil.split((String)hmParamSetDetails.get(paramid), ";");
                                String qcBatchParamSetId = arrParamSetDetails[0];
                                String target = arrParamSetDetails[1];
                                String sd = arrParamSetDetails[2];
                                String[] arrDataPointsResult = StringUtil.split(sb.toString(), ";");
                                String[] arrDataPoints = StringUtil.split(sb.toString(), ";");
                                for (int idx2 = 0; idx2 < arrDataPointsResult.length; ++idx2) {
                                    arrDataPointsResult[idx2] = "Pass";
                                }
                                String[] arrBatchIds = StringUtil.split(batchids.toString(), ";");
                                String[] arrBatchItemIds = StringUtil.split(batchitemids.toString(), ";");
                                String[] arrdataitemsdcid = StringUtil.split(dataitemsdcid.toString(), ";");
                                String[] arrdataitemkeyid1 = StringUtil.split(dataitemkeyid1.toString(), ";");
                                String[] arrdataitemkeyid2 = StringUtil.split(dataitemkeyid2.toString(), ";");
                                String[] arrdataitemkeyid3 = StringUtil.split(dataitemkeyid3.toString(), ";");
                                String[] arrdataitemparamlistid = StringUtil.split(dataitemparamlistid.toString(), ";");
                                String[] arrdataitemparamlistversionid = StringUtil.split(dataitemparamlistversionid.toString(), ";");
                                String[] arrdataitemvariantid = StringUtil.split(dataitemvariantid.toString(), ";");
                                String[] arrdataitemdataset = StringUtil.split(dataitemdataset.toString(), ";");
                                String[] arrdataitemreplicateid = StringUtil.split(dataitemreplicateid.toString(), ";");
                                String[] arrdataitemparamid = StringUtil.split(dataitemparamid.toString(), ";");
                                finalSb.setLength(0);
                                hmSumValue.clear();
                                hmReplicateCount.clear();
                                for (int j = 0; j < arrDataPoints.length; ++j) {
                                    mapKey = arrdataitemsdcid[j] + arrdataitemkeyid1[j] + arrdataitemkeyid2[j] + arrdataitemkeyid3[j] + arrdataitemparamlistid[j] + arrdataitemparamlistversionid[j] + arrdataitemvariantid[j] + arrdataitemdataset[j] + arrdataitemparamid[j];
                                    double data = Double.parseDouble(arrDataPoints[j]);
                                    if (!hmSumValue.containsKey(mapKey)) {
                                        hmSumValue.put(mapKey, new Double(data));
                                        hmReplicateCount.put(mapKey, new Integer(1));
                                        continue;
                                    }
                                    double mapdata = (Double)hmSumValue.get(mapKey);
                                    int mapreplicatecount = (Integer)hmReplicateCount.get(mapKey);
                                    hmSumValue.put(mapKey, new Double(data += mapdata));
                                    hmReplicateCount.put(mapKey, new Integer(++mapreplicatecount));
                                }
                                Set keyset = hmSumValue.keySet();
                                Iterator iterator = keyset.iterator();
                                String[] keyArray = new String[hmSumValue.size()];
                                String[] resultArray = new String[hmSumValue.size()];
                                int ctr = 0;
                                while (iterator.hasNext()) {
                                    String key = (String)iterator.next();
                                    double avgOfReplicates = (Double)hmSumValue.get(key) / (double)((Integer)hmReplicateCount.get(key)).intValue();
                                    finalSb.append(avgOfReplicates + ";");
                                    keyArray[ctr] = key;
                                    resultArray[ctr] = "Pass";
                                    ++ctr;
                                }
                                if (finalSb.length() > 0) {
                                    finalSb.setLength(finalSb.length() - 1);
                                }
                                this.logger.debug("Param : " + paramid);
                                this.logger.debug("ParamType : " + parameterType);
                                this.logger.debug("Data points passed for evaluation:" + finalSb.toString());
                                valid = QCEvalAction.runQCValidation(finalSb.toString(), target, sd, ucl, lcl, violationCount, windowSize, sigmaAboveCL, sigmaBelowCL, rulePatternFlag, insideLimitFlag, resultArray);
                                block11: for (int j = 0; j < arrDataPoints.length; ++j) {
                                    mapKey = arrdataitemsdcid[j] + arrdataitemkeyid1[j] + arrdataitemkeyid2[j] + arrdataitemkeyid3[j] + arrdataitemparamlistid[j] + arrdataitemparamlistversionid[j] + arrdataitemvariantid[j] + arrdataitemdataset[j] + arrdataitemparamid[j];
                                    for (int m = 0; m < keyArray.length; ++m) {
                                        if (!keyArray[m].equals(mapKey)) continue;
                                        arrDataPointsResult[j] = resultArray[m];
                                        if (!"Fail".equals(arrDataPointsResult[j]) || warningFlag == null || !"Y".equals(warningFlag)) continue block11;
                                        arrDataPointsResult[j] = "Warning";
                                        continue block11;
                                    }
                                }
                                if (valid) {
                                    for (idx = 0; idx < arrDataPointsResult.length; ++idx) {
                                        if (!arrDataPointsResult[idx].equals("Pass") || !arrBatchIds[idx].equals(qcBatch.getQCBatchID())) continue;
                                        dsQCBatchItems.sort("s_qcbatchitemid");
                                        String result = "Pass";
                                        int findrow = dsQCBatchItems.findRow("s_qcbatchitemid", arrBatchItemIds[idx]);
                                        if (findrow == -1) {
                                            int addrow = dsQCBatchItems.addRow();
                                            dsQCBatchItems.setString(addrow, "s_qcbatchid", qcBatch.getQCBatchID());
                                            dsQCBatchItems.setString(addrow, "s_qcbatchitemid", arrBatchItemIds[idx]);
                                            dsQCBatchItems.setString(addrow, "evaluationstatus", result);
                                        }
                                        ++currentBatchPassCtr;
                                        sbsdcid.append(arrdataitemsdcid[idx] + ";");
                                        sbkeyid1.append(arrdataitemkeyid1[idx] + ";");
                                        sbkeyid2.append(arrdataitemkeyid2[idx] + ";");
                                        sbkeyid3.append(arrdataitemkeyid3[idx] + ";");
                                        sbparamlistid.append(arrdataitemparamlistid[idx] + ";");
                                        sbparamlistversionid.append(arrdataitemparamlistversionid[idx] + ";");
                                        sbvariantid.append(arrdataitemvariantid[idx] + ";");
                                        sbdataset.append(arrdataitemdataset[idx] + ";");
                                        sbreplicateid.append(arrdataitemreplicateid[idx] + ";");
                                        sbparamid.append(arrdataitemparamid[idx] + ";");
                                        sbparamtype.append(parameterType + ";");
                                        sbevalstatus.append(result + ";");
                                    }
                                } else {
                                    for (idx = 0; idx < arrDataPointsResult.length; ++idx) {
                                        if (!arrBatchIds[idx].equals(qcBatch.getQCBatchID())) continue;
                                        String result = arrDataPointsResult[idx];
                                        if ("Fail".equals(arrDataPointsResult[idx]) || "Warning".equals(arrDataPointsResult[idx])) {
                                            dsQCBatchItems.sort("s_qcbatchitemid");
                                            int findrow = dsQCBatchItems.findRow("s_qcbatchitemid", arrBatchItemIds[idx]);
                                            if (findrow == -1) {
                                                int addrow = dsQCBatchItems.addRow();
                                                dsQCBatchItems.setString(addrow, "s_qcbatchid", qcBatch.getQCBatchID());
                                                dsQCBatchItems.setString(addrow, "s_qcbatchitemid", arrBatchItemIds[idx]);
                                                dsQCBatchItems.setString(addrow, "evaluationstatus", result);
                                            } else {
                                                String evalStatus = dsQCBatchItems.getString(findrow, "evaluationstatus", "");
                                                if (!"Fail".equals(evalStatus)) {
                                                    dsQCBatchItems.setString(findrow, "evaluationstatus", result);
                                                }
                                            }
                                            if ("Fail".equals(arrDataPointsResult[idx])) {
                                                ++currentBatchFailCtr;
                                            } else {
                                                ++currentBatchWarningCtr;
                                            }
                                        } else {
                                            passQCBatchItemIdHolder.add(arrBatchItemIds[idx]);
                                            ++currentBatchPassCtr;
                                        }
                                        sbsdcid.append(arrdataitemsdcid[idx] + ";");
                                        sbkeyid1.append(arrdataitemkeyid1[idx] + ";");
                                        sbkeyid2.append(arrdataitemkeyid2[idx] + ";");
                                        sbkeyid3.append(arrdataitemkeyid3[idx] + ";");
                                        sbparamlistid.append(arrdataitemparamlistid[idx] + ";");
                                        sbparamlistversionid.append(arrdataitemparamlistversionid[idx] + ";");
                                        sbvariantid.append(arrdataitemvariantid[idx] + ";");
                                        sbdataset.append(arrdataitemdataset[idx] + ";");
                                        sbreplicateid.append(arrdataitemreplicateid[idx] + ";");
                                        sbparamid.append(arrdataitemparamid[idx] + ";");
                                        sbparamtype.append(parameterType + ";");
                                        sbevalstatus.append(result + ";");
                                    }
                                }
                                String paramEvalStatus = "";
                                if (currentBatchFailCtr > 0) {
                                    paramEvalStatus = "Fail";
                                } else if (currentBatchWarningCtr > 0) {
                                    paramEvalStatus = "Warning";
                                } else if (currentBatchPassCtr > 0) {
                                    paramEvalStatus = "Pass";
                                }
                                if (paramEvalStatus.length() > 0) {
                                    for (int d = 0; d < dsParamSet.size(); ++d) {
                                        if (!dsParamSet.getString(d, "s_qcbatchparamsetid", "").equals(qcBatchParamSetId)) continue;
                                        String evalStat = dsParamSet.getString(d, "evalstatus", "");
                                        if (evalStat.equals("") || evalStat.equals("Pass")) {
                                            dsParamSet.setString(d, "evalstatus", paramEvalStatus);
                                            break;
                                        }
                                        if (!evalStat.equals("Warning") || !paramEvalStatus.equals("Fail")) break;
                                        dsParamSet.setString(d, "evalstatus", paramEvalStatus);
                                        break;
                                    }
                                    int newrow = dsUpdateQCEvalRuleParam.addRow();
                                    dsUpdateQCEvalRuleParam.setString(newrow, "s_qcbatchevalruleid", qcBatchEvalRuleId);
                                    dsUpdateQCEvalRuleParam.setString(newrow, "s_qcbatchsampletypeid", qcBatchSampleTypeId);
                                    dsUpdateQCEvalRuleParam.setString(newrow, "s_qcbatchparamsetid", qcBatchParamSetId);
                                    dsUpdateQCEvalRuleParam.setString(newrow, "evalstatus", paramEvalStatus);
                                }
                            }
                            catch (SapphireException e) {
                                this.logger.error(e.getMessage(), e);
                            }
                            catch (RuleException e) {
                                this.logger.error(e.getMessage(), e);
                            }
                        }
                        sb.setLength(0);
                        batchids.setLength(0);
                        batchitemids.setLength(0);
                        dataitemsdcid.setLength(0);
                        dataitemkeyid1.setLength(0);
                        dataitemkeyid2.setLength(0);
                        dataitemkeyid3.setLength(0);
                        dataitemparamlistid.setLength(0);
                        dataitemparamlistversionid.setLength(0);
                        dataitemvariantid.setLength(0);
                        dataitemdataset.setLength(0);
                        dataitemreplicateid.setLength(0);
                        dataitemparamid.setLength(0);
                        if (currentBatchFailCtr > 0) {
                            currentRuleEvalStatus = "Fail";
                            continue;
                        }
                        if (currentBatchWarningCtr > 0 && !currentRuleEvalStatus.equals("Fail")) {
                            currentRuleEvalStatus = "Warning";
                            continue;
                        }
                        if (currentBatchPassCtr <= 0 || currentRuleEvalStatus.equals("Warning") || currentRuleEvalStatus.equals("Fail")) continue;
                        currentRuleEvalStatus = "Pass";
                    }
                    if (currentRuleEvalStatus.equals("Fail") || currentRuleEvalStatus.equals("Warning")) break;
                }
            }
            for (int x = 0; x < passQCBatchItemIdHolder.size(); ++x) {
                dsQCBatchItems.sort("s_qcbatchitemid");
                if (dsQCBatchItems.findRow("s_qcbatchitemid", (String)passQCBatchItemIdHolder.get(x)) >= 0) continue;
                int addrow = dsQCBatchItems.addRow();
                dsQCBatchItems.setString(addrow, "s_qcbatchid", qcBatch.getQCBatchID());
                dsQCBatchItems.setString(addrow, "s_qcbatchitemid", (String)passQCBatchItemIdHolder.get(x));
                dsQCBatchItems.setString(addrow, "evaluationstatus", "Pass");
            }
            if (sbsdcid != null && sbsdcid.length() > 0) {
                DataSet dsupdate = new DataSet();
                sbsdcid.setLength(sbsdcid.length() - 1);
                sbkeyid1.setLength(sbkeyid1.length() - 1);
                sbkeyid2.setLength(sbkeyid2.length() - 1);
                sbkeyid3.setLength(sbkeyid3.length() - 1);
                sbparamlistid.setLength(sbparamlistid.length() - 1);
                sbparamlistversionid.setLength(sbparamlistversionid.length() - 1);
                sbvariantid.setLength(sbvariantid.length() - 1);
                sbdataset.setLength(sbdataset.length() - 1);
                sbreplicateid.setLength(sbreplicateid.length() - 1);
                sbparamid.setLength(sbparamid.length() - 1);
                sbparamtype.setLength(sbparamtype.length() - 1);
                sbevalstatus.setLength(sbevalstatus.length() - 1);
                dsupdate.addColumnValues("sdcid", 0, sbsdcid.toString(), ";");
                dsupdate.addColumnValues("keyid1", 0, sbkeyid1.toString(), ";");
                dsupdate.addColumnValues("keyid2", 0, sbkeyid2.toString(), ";");
                dsupdate.addColumnValues("keyid3", 0, sbkeyid3.toString(), ";");
                dsupdate.addColumnValues("paramlistid", 0, sbparamlistid.toString(), ";");
                dsupdate.addColumnValues("paramlistversionid", 0, sbparamlistversionid.toString(), ";");
                dsupdate.addColumnValues("variantid", 0, sbvariantid.toString(), ";");
                dsupdate.addColumnValues("dataset", 0, sbdataset.toString(), ";");
                dsupdate.addColumnValues("paramid", 0, sbparamid.toString(), ";");
                dsupdate.addColumnValues("paramtype", 0, sbparamtype.toString(), ";");
                dsupdate.addColumnValues("replicateid", 0, sbreplicateid.toString(), ";");
                dsupdate.addColumnValues("s_qcevalstatus", 0, sbevalstatus.toString(), ";");
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                DataSetUtil.update(this.database, dsupdate, "sdidataitem", keycols);
            }
            this.updateEvaluationStatus(qp, dsUpdateQCEvalRuleParam, dsParamSet, dsQCBatchItems, qcBatchSampleType);
            this.updateQCBatchSampleTypeEvalSpecStatus(qp, qcBatchSampleType);
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    private void updateEvaluationStatus(QueryProcessor qp, DataSet dsUpdateQCEvalRuleParam, DataSet dsParamSet, DataSet dsQCBatchItems, QCBatchSampleType qcBatchSampleType) throws SapphireException {
        String[] keycols;
        if (dsUpdateQCEvalRuleParam.size() > 0) {
            String[] keycols2 = new String[]{"s_qcbatchsampletypeid", "s_qcbatchevalruleid", "s_qcbatchparamsetid"};
            DataSetUtil.update(this.database, dsUpdateQCEvalRuleParam, "s_qcbatchevalruleparam", keycols2);
        }
        String specid = qcBatchSampleType.getColumnValue("specid");
        String specversionid = qcBatchSampleType.getColumnValue("specversionid");
        String evalparamtype = qcBatchSampleType.getColumnValue("evaluateparamtype");
        String qcBatchsampleTypeId = qcBatchSampleType.getQCBatchSampleTypeID();
        String qcBatchId = qcBatchSampleType.getColumnValue("qcbatchid");
        SafeSQL safeSQL = new SafeSQL();
        if (specid != null && specid.trim().length() > 0) {
            DataSet specVersionDS;
            if ((specversionid == null || specversionid.trim().length() == 0 || specversionid.equalsIgnoreCase("C")) && (specVersionDS = qp.getPreparedSqlDataSet("SELECT specversionid FROM spec WHERE specid = " + safeSQL.addVar(specid) + " AND ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (specversionid as numeric) desc", safeSQL.getValues())) != null) {
                specversionid = specVersionDS.getValue(0, "specversionid");
            }
            StringBuffer sql = new StringBuffer();
            safeSQL.reset();
            sql.append(" SELECT DISTINCT sp.paramid, sp.condition, sd.s_qcbatchitemid ").append(" FROM sdidata sd, sdidataitemspec sp, s_qcbatchitem  sq ").append(" WHERE sp.sdcid = sd.sdcid AND sp.keyid1 = sd.keyid1 AND sp.keyid2 = sd.keyid2 AND sp.keyid3 = sd.keyid3").append(" AND sp.paramlistid = sd.paramlistid AND sp.paramlistversionid = sd.paramlistversionid AND sp.variantid = sd.variantid ").append(" AND sp.dataset = sd.dataset AND sd.s_qcbatchitemid = sq.s_qcbatchitemid AND sd.s_qcbatchid = sq.s_qcbatchid ").append(" AND sq.s_qcbatchid = " + safeSQL.addVar(qcBatchId) + " AND sq.qcbatchsampletypeid = " + safeSQL.addVar(qcBatchsampleTypeId)).append(" AND sp.specid = " + safeSQL.addVar(specid) + " AND sp.specversionid = " + safeSQL.addVar(specversionid) + " AND sp.paramtype = " + safeSQL.addVar(evalparamtype));
            dsParamSet.sort("paramid");
            dsQCBatchItems.sort("s_qcbatchitemid");
            DataSet dsSpec = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (dsSpec != null) {
                ConfigurationProcessor cfg = this.getConfigurationProcessor();
                for (int i = 0; i < dsSpec.size(); ++i) {
                    String specCondInterpretation;
                    String speccond;
                    String paramid = dsSpec.getValue(i, "paramid", "");
                    String condition = dsSpec.getValue(i, "condition", "");
                    String dispResult = OpalUtil.getSpecCondition(this.__SpecInterpretation, condition);
                    if (dispResult == null || dispResult.trim().length() == 0) {
                        dispResult = condition;
                    }
                    String qcbatchitemid = dsSpec.getValue(i, "s_qcbatchitemid", "");
                    if (condition.trim().length() <= 0) continue;
                    int findrow = dsParamSet.findRow("paramid", paramid);
                    if (findrow > -1) {
                        speccond = dsParamSet.getValue(findrow, "speccondition", "");
                        specCondInterpretation = this.__SpecInterpretation.getProperty(speccond);
                        if (specCondInterpretation == null || specCondInterpretation.trim().length() == 0) {
                            specCondInterpretation = speccond;
                        }
                        if ("Fail".equals(condition)) {
                            dsParamSet.setValue(findrow, "speccondition", dispResult);
                        } else if ("Warning".equals(condition) && !"Fail".equals(specCondInterpretation)) {
                            dsParamSet.setValue(findrow, "speccondition", dispResult);
                        } else if ("Pass".equals(condition) && !"Fail".equals(specCondInterpretation) && !"Warning".equals(specCondInterpretation)) {
                            dsParamSet.setValue(findrow, "speccondition", dispResult);
                        }
                    }
                    if ((findrow = dsQCBatchItems.findRow("s_qcbatchitemid", qcbatchitemid)) > -1) {
                        speccond = dsQCBatchItems.getValue(findrow, "speccondition", "");
                        specCondInterpretation = this.__SpecInterpretation.getProperty(speccond);
                        if (specCondInterpretation == null || specCondInterpretation.trim().length() == 0) {
                            specCondInterpretation = speccond;
                        }
                        if ("Fail".equals(condition)) {
                            dsQCBatchItems.setValue(findrow, "speccondition", dispResult);
                            continue;
                        }
                        if ("Warning".equals(condition) && !"Fail".equals(specCondInterpretation)) {
                            dsQCBatchItems.setValue(findrow, "speccondition", dispResult);
                            continue;
                        }
                        if (!"Pass".equals(condition) || "Fail".equals(specCondInterpretation) || "Warning".equals(specCondInterpretation)) continue;
                        dsQCBatchItems.setValue(findrow, "speccondition", dispResult);
                        continue;
                    }
                    int r = dsQCBatchItems.addRow();
                    dsQCBatchItems.setString(r, "s_qcbatchid", qcBatchId);
                    dsQCBatchItems.setString(r, "s_qcbatchitemid", qcbatchitemid);
                    dsQCBatchItems.setString(r, "evaluationstatus", "");
                    dsQCBatchItems.setString(r, "speccondition", dispResult);
                }
            }
        }
        if (dsParamSet != null && dsParamSet.size() > 0) {
            keycols = new String[]{"s_qcbatchsampletypeid", "s_qcbatchparamsetid"};
            DataSetUtil.update(this.database, dsParamSet, "s_qcbatchparamset", keycols);
        }
        if (dsQCBatchItems != null && dsQCBatchItems.size() > 0) {
            keycols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
            DataSetUtil.update(this.database, dsQCBatchItems, "s_qcbatchitem", keycols);
        }
    }

    private void updateQCBatchSampleTypeEvalSpecStatus(QueryProcessor qp, QCBatchSampleType qcBatchSampleType) {
        String qcBatchSampleTypeId = qcBatchSampleType.getQCBatchSampleTypeID();
        String qcEvalStatus = qcBatchSampleType.getColumnValue("evalstatus");
        String qcSpecCondition = qcBatchSampleType.getColumnValue("speccondition");
        boolean needToUpdateEvalStatus = true;
        HashMap<String, String> actionProps = new HashMap<String, String>();
        actionProps.put("sdcid", "QCBatchSampleType");
        actionProps.put("keyid1", qcBatchSampleTypeId);
        actionProps.put("columnid", "evalstatus;speccondition");
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsBatchItems = qp.getPreparedSqlDataSet("SELECT EVALUATIONSTATUS, SPECCONDITION  FROM s_qcbatchitem WHERE qcbatchsampletypeid = " + safeSQL.addVar(qcBatchSampleTypeId), safeSQL.getValues());
        int failctr = 0;
        int warningctr = 0;
        int passctr = 0;
        int failSpctr = 0;
        int warningSpctr = 0;
        int passSpctr = 0;
        if (dsBatchItems != null) {
            String specCondition;
            for (int i = 0; i < dsBatchItems.size(); ++i) {
                String evaluationStatus = dsBatchItems.getValue(i, "evaluationstatus", "");
                String speccond = dsBatchItems.getValue(i, "speccondition", "");
                String specCondInterpretation = this.__SpecInterpretation.getProperty(speccond);
                if (specCondInterpretation.trim().length() == 0) {
                    specCondInterpretation = speccond;
                }
                if ("Fail".equals(evaluationStatus)) {
                    ++failctr;
                } else if ("Warning".equals(evaluationStatus)) {
                    ++warningctr;
                } else if ("Pass".equals(evaluationStatus)) {
                    ++passctr;
                }
                if ("Fail".equals(specCondInterpretation)) {
                    ++failSpctr;
                    continue;
                }
                if ("Warning".equals(specCondInterpretation)) {
                    ++warningSpctr;
                    continue;
                }
                if (!"Pass".equals(specCondInterpretation)) continue;
                ++passSpctr;
            }
            String batchSampleTypeEvalStatus = "";
            if (failctr > 0) {
                batchSampleTypeEvalStatus = "Fail";
            } else if (warningctr > 0) {
                batchSampleTypeEvalStatus = "Warning";
            } else if (passctr > 0) {
                batchSampleTypeEvalStatus = "Pass";
            }
            String batchSampleTypeSpecStatus = "";
            if (failSpctr > 0) {
                specCondition = OpalUtil.getSpecCondition(this.__SpecInterpretation, "Fail");
                batchSampleTypeSpecStatus = specCondition.trim().length() == 0 ? "Fail" : specCondition;
            } else if (warningSpctr > 0) {
                specCondition = OpalUtil.getSpecCondition(this.__SpecInterpretation, "Warning");
                batchSampleTypeSpecStatus = specCondition.trim().length() == 0 ? "Warning" : specCondition;
            } else if (passSpctr > 0) {
                specCondition = OpalUtil.getSpecCondition(this.__SpecInterpretation, "Pass");
                String string = batchSampleTypeSpecStatus = specCondition.trim().length() == 0 ? "Pass" : specCondition;
            }
            if (qcEvalStatus != null && qcEvalStatus.equalsIgnoreCase(batchSampleTypeEvalStatus) && qcSpecCondition != null && qcSpecCondition.equalsIgnoreCase(batchSampleTypeSpecStatus)) {
                needToUpdateEvalStatus = false;
            } else {
                actionProps.put("value", batchSampleTypeEvalStatus + ";" + batchSampleTypeSpecStatus);
            }
            try {
                if (needToUpdateEvalStatus) {
                    this.getActionProcessor().processAction("SetSDIString", "1", actionProps);
                }
            }
            catch (ActionException e) {
                this.logger.error("Error running SetSDIString", e);
            }
        }
    }
}

