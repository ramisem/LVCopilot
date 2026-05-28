/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddStage
extends BaseAction
implements sapphire.action.AddStage {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77312 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("SDC needs to be specified."));
        }
        String keyId1 = properties.getProperty("keyid1");
        if (keyId1.trim().length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No KeyId1 specified"));
        }
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String[] keyid1prop = StringUtil.split(keyId1, ";");
        String[] keyid2prop = StringUtil.split(keyId2, ";");
        String[] keyid3prop = StringUtil.split(keyId3, ";");
        if (keycolid2.length() > 0) {
            if (keyId2.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid2 specified"));
            }
            if (keyid1prop.length != keyid2prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid2 not matching with count of keyid1"));
            }
        }
        if (keycolid3.length() > 0) {
            if (keyId3.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid3 specified"));
            }
            if (keyid1prop.length != keyid3prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid3 not matching with count of keyid1"));
            }
        }
        String processStageId = properties.getProperty("processstageid");
        String planId = properties.getProperty("samplingplanid");
        String planVerId = properties.getProperty("samplingplanversionid");
        String processStageLabel = properties.getProperty("processstagelabel");
        String repeatCount = properties.getProperty("repeatcount", "");
        String templateSDCIds = properties.getProperty("templatesdcid");
        String templateKeyId1s = properties.getProperty("templatekeyid1");
        String templateKeyId2s = properties.getProperty("templatekeyid2");
        String templateKeyId3s = properties.getProperty("templatekeyid3");
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        String stageSDCId = SamplingPlanUtil.getIntermediateStageSDCFromPolicy(sdcid, configProcessor);
        if (stageSDCId == null || stageSDCId.equals("")) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, SamplingPlan policy property \"Intermediate Stage SDC\" is not defined."));
        }
        tokenMap.put("stageSDCId", stageSDCId);
        tokenMap.put("sdcid", sdcid);
        String stageTableId = sdcProcessor.getProperty(stageSDCId, "tableid");
        if (stageTableId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, \"Intermediate Stage SDC\" [stageSDCId] does not exist.", tokenMap));
        }
        String descColumnName = sdcProcessor.getProperty(stageSDCId, "desccol");
        String stKeycolid2 = sdcProcessor.getProperty(stageSDCId, "keycolid2");
        String stKeycolid3 = sdcProcessor.getProperty(stageSDCId, "keycolid3");
        HashMap<String, String> linkFilter = new HashMap<String, String>();
        linkFilter.put("linksdcid", sdcid);
        DataSet linkData = sdcProcessor.getLinksData(stageSDCId).getFilteredDataSet(linkFilter);
        String linkKey1 = "";
        String linkKey2 = "";
        String linkKey3 = "";
        if (linkData.getRowCount() > 0) {
            linkKey1 = linkData.getValue(0, "sdccolumnid", "");
            if (linkKey1.length() == 0) {
                tokenMap.put("keycolid1", keycolid1);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column \"[keycolid1]\" of SDC [sdcid]", tokenMap));
            }
            linkKey2 = linkData.getValue(0, "sdccolumnid2", "");
            if (keycolid2.length() > 0 && linkKey2.length() == 0) {
                tokenMap.put("keycolid2", keycolid2);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column \"[keycolid2]\" of SDC [sdcid]", tokenMap));
            }
            linkKey3 = linkData.getValue(0, "sdccolumnid3", "");
            if (keycolid3.length() > 0 && linkKey3.length() == 0) {
                tokenMap.put("keycolid3", keycolid3);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column \"[keycolid3]\" of SDC [sdcid]", tokenMap));
            }
        } else {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, SDC [stageSDCId] is not linked to the SDC [sdcid]", tokenMap));
        }
        String processStageLinkKeyColname = "processstageid";
        DataSet ds = new DataSet();
        ds.addColumnValues("keyid1", 0, keyId1, ";");
        ds.addColumnValues("keyid2", 0, keyId2, ";");
        ds.addColumnValues("keyid3", 0, keyId3, ";");
        ds.addColumnValues("processstageid", 0, processStageId, ";");
        ds.addColumnValues("samplingplanid", 0, planId, ";");
        ds.addColumnValues("samplingplanversionid", 0, planVerId, ";");
        ds.addColumnValues("processstagelabel", 0, processStageLabel, ";");
        ds.padColumn("keyid1");
        ds.padColumn("keyid2");
        ds.padColumn("keyid3");
        ds.addColumnValues("repeatcount", 0, repeatCount, ";");
        ds.addColumnValues("templatesdcid", 0, templateSDCIds, ";");
        ds.addColumnValues("templatekeyid1", 0, templateKeyId1s, ";");
        ds.addColumnValues("templatekeyid2", 0, templateKeyId2s, ";");
        ds.addColumnValues("templatekeyid3", 0, templateKeyId3s, ";");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT max(processstageinstance) maxinst  FROM " + stageTableId + "  WHERE " + processStageLinkKeyColname + " = ? AND " + linkKey1 + " = ?");
        if (keycolid2.length() > 0) {
            sql.append(" AND " + linkKey2 + " = ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND " + linkKey3 + " = ?");
        }
        PreparedStatement getMaxInstance = this.database.prepareStatement("getmaxinstance", sql.toString());
        sql.setLength(0);
        sql.append("SELECT max(processstageinstance) maxinst  FROM " + stageTableId + "  WHERE (" + processStageLinkKeyColname + " IS NULL OR " + processStageLinkKeyColname + " = '' )  AND " + linkKey1 + " = ?   AND label = ? ");
        if (keycolid2.length() > 0) {
            sql.append(" AND " + linkKey2 + " = ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND " + linkKey3 + " = ?");
        }
        PreparedStatement getAdhocStageMaxInstance = this.database.prepareStatement("getadhocstagemaxinstance", sql.toString());
        PreparedStatement getPCStageDetails = this.database.prepareStatement("getpcstagedetails", "SELECT s_processstageid, s_samplingplanid, s_samplingplanversionid, processstagedesc, label, repeatcount, templatesdcid, templatekeyid1, templatekeyid2, templatekeyid3  FROM s_processstage WHERE s_processstageid = ? AND s_samplingplanid = ? AND s_samplingplanversionid = ?");
        DataSet dsPcStageDetails = new DataSet();
        StringBuffer newStageId1 = new StringBuffer();
        StringBuffer newStageId2 = new StringBuffer();
        StringBuffer newStageId3 = new StringBuffer();
        try {
            DataSet dsMaxSeqTracking = new DataSet();
            dsMaxSeqTracking.addColumn("keyids", 0);
            dsMaxSeqTracking.addColumn("processstageid", 0);
            dsMaxSeqTracking.addColumn("instancecount", 1);
            dsMaxSeqTracking.addColumn("stagelabel", 0);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String key1 = "";
                String key2 = "";
                String key3 = "";
                String stageId = "";
                String spId = "";
                String spVerId = "";
                key1 = ds.getString(i, "keyid1");
                key2 = ds.getString(i, "keyid2");
                key3 = ds.getString(i, "keyid3");
                String keys = key1;
                if (keycolid2.length() > 0) {
                    keys = keys + key2;
                }
                if (keycolid3.length() > 0) {
                    keys = keys + key3;
                }
                stageId = ds.getString(i, "processstageid", "");
                spId = ds.getString(i, "samplingplanid", "");
                spVerId = ds.getString(i, "samplingplanversionid", "");
                String stageLabel = ds.getString(i, "processstagelabel", "");
                String templateKey1 = ds.getString(i, "templatekeyid1", "");
                String templateKey2 = ds.getString(i, "templatekeyid2", "");
                String templateKey3 = ds.getString(i, "templatekeyid3", "");
                StringBuffer stgInstances = new StringBuffer();
                int repeatCnt = 0;
                if (stageId.length() > 0) {
                    String templateSDCId;
                    String strRCount;
                    tokenMap.put("stageId", stageId);
                    if (spId.length() == 0 || spVerId.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("No Sampling Plan Id/Version specified for the ProcessStage Id: [stageId]", tokenMap));
                    }
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("s_processstageid", stageId);
                    findMap.put("s_samplingplanid", spId);
                    findMap.put("s_samplingplanversionid", spVerId);
                    int findRow = dsPcStageDetails.findRow(findMap);
                    if (findRow < 0) {
                        getPCStageDetails.setString(1, stageId);
                        getPCStageDetails.setString(2, spId);
                        getPCStageDetails.setString(3, spVerId);
                        DataSet pcStgDetails = new DataSet(getPCStageDetails.executeQuery());
                        dsPcStageDetails.copyRow(pcStgDetails, -1, 1);
                        findRow = dsPcStageDetails.findRow(findMap);
                    }
                    if ((strRCount = ds.getString(i, "repeatcount", "")).length() == 0) {
                        strRCount = dsPcStageDetails.getValue(findRow, "repeatcount", "");
                    }
                    try {
                        repeatCnt = Integer.parseInt(strRCount);
                    }
                    catch (NumberFormatException ne) {
                        tokenMap.put("strRCount", strRCount);
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("Invalid repeat count: [strRCount] specified for Stage Id: [stageId]", tokenMap));
                    }
                    if (stageLabel.length() == 0) {
                        stageLabel = dsPcStageDetails.getValue(findRow, "label", "");
                    }
                    if ((templateSDCId = ds.getString(i, "templatesdcid", "")).length() == 0) {
                        templateSDCId = dsPcStageDetails.getValue(findRow, "templatesdcid", "");
                    }
                    if (templateKey1.length() == 0) {
                        templateKey1 = dsPcStageDetails.getValue(findRow, "templatekeyid1", "");
                    }
                    if (templateKey2.length() == 0) {
                        templateKey2 = dsPcStageDetails.getValue(findRow, "templatekeyid2", "");
                    }
                    if (templateKey3.length() == 0) {
                        templateKey3 = dsPcStageDetails.getValue(findRow, "templatekeyid3", "");
                    }
                    if (templateSDCId.length() > 0 && !stageSDCId.equals(templateSDCId)) {
                        tokenMap.put("templateSDCId", templateSDCId);
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. Template SDC ID [templateSDCId] found for the Process Stage Id '[stageId]' does not match with the SDC '[stageSDCId]' defined in the policy.", tokenMap));
                    }
                    int processStageInstance = 0;
                    HashMap<String, String> findMap2 = new HashMap<String, String>();
                    findMap2.put("keyids", keys);
                    findMap2.put("processstageid", stageId);
                    int fndRow = dsMaxSeqTracking.findRow(findMap2);
                    if (fndRow > -1) {
                        processStageInstance = dsMaxSeqTracking.getInt(fndRow, "instancecount");
                    } else {
                        DataSet dsMaxInst;
                        getMaxInstance.setString(1, stageId);
                        getMaxInstance.setString(2, key1);
                        if (keycolid2.length() > 0) {
                            getMaxInstance.setString(3, key2);
                        }
                        if (keycolid3.length() > 0) {
                            getMaxInstance.setString(4, key3);
                        }
                        if ((dsMaxInst = new DataSet(getMaxInstance.executeQuery())) != null && dsMaxInst.getRowCount() > 0) {
                            processStageInstance = dsMaxInst.getInt(0, "maxinst", 0);
                        }
                    }
                    for (int c = 0; c < repeatCnt; ++c) {
                        stgInstances.append(";").append("" + ++processStageInstance);
                    }
                    if (fndRow < 0) {
                        fndRow = dsMaxSeqTracking.addRow();
                        dsMaxSeqTracking.setString(fndRow, "keyids", keys);
                        dsMaxSeqTracking.setString(fndRow, "processstageid", stageId);
                    }
                    dsMaxSeqTracking.setNumber(fndRow, "instancecount", processStageInstance);
                } else {
                    if (stageLabel.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("Need to provide either Stage label or combination of ProcessStage Id, SamplingPlan Id and Version Id"));
                    }
                    tokenMap.put("stageLabel", stageLabel);
                    if (templateKey1.length() == 0) {
                        this.logger.warn(" " + tp.translate("No template KeyId specified for stage label [stageLabel]", tokenMap));
                    }
                    if (stKeycolid2.length() > 0 && templateKey2.length() == 0) {
                        this.logger.warn(" " + tp.translate("No template KeyId2 specified for stage label [stageLabel]", tokenMap));
                    }
                    if (stKeycolid3.length() > 0 && templateKey3.length() == 0) {
                        this.logger.warn(" " + tp.translate("No template KeyId3 specified for stage label [stageLabel]", tokenMap));
                    }
                    int processStageInstance = 0;
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("keyids", keys);
                    findMap.put("stagelabel", stageLabel);
                    int fndRow = dsMaxSeqTracking.findRow(findMap);
                    if (fndRow > -1) {
                        processStageInstance = dsMaxSeqTracking.getInt(fndRow, "instancecount");
                    } else {
                        DataSet dsMaxInst;
                        getAdhocStageMaxInstance.setString(1, key1);
                        getAdhocStageMaxInstance.setString(2, stageLabel);
                        if (keycolid2.length() > 0) {
                            getAdhocStageMaxInstance.setString(3, key2);
                        }
                        if (keycolid3.length() > 0) {
                            getAdhocStageMaxInstance.setString(4, key3);
                        }
                        if ((dsMaxInst = new DataSet(getAdhocStageMaxInstance.executeQuery())) != null && dsMaxInst.getRowCount() > 0) {
                            processStageInstance = dsMaxInst.getInt(0, "maxinst", 0);
                        }
                    }
                    String strCount = ds.getString(i, "repeatcount", "1");
                    try {
                        repeatCnt = Integer.parseInt(strCount);
                    }
                    catch (Exception ne) {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("count", strCount);
                        valueMap.put("stageLabel", stageLabel);
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("Invalid repeat count: [count] specified with  Stage Label: [stageLabel]", valueMap));
                    }
                    for (int inst = 0; inst < repeatCnt; ++inst) {
                        stgInstances.append(";").append("" + ++processStageInstance);
                    }
                    if (fndRow < 0) {
                        fndRow = dsMaxSeqTracking.addRow();
                        dsMaxSeqTracking.setString(fndRow, "keyids", keys);
                        dsMaxSeqTracking.setString(fndRow, "stagelabel", stageLabel);
                    }
                    dsMaxSeqTracking.setNumber(fndRow, "instancecount", processStageInstance);
                }
                if (stgInstances.length() <= 0) continue;
                PropertyList addSDIProps = new PropertyList();
                addSDIProps.setProperty("sdcid", stageSDCId);
                addSDIProps.setProperty(linkKey1, key1);
                if (keycolid2.length() > 0) {
                    addSDIProps.setProperty(linkKey2, key2);
                }
                if (keycolid3.length() > 0) {
                    addSDIProps.setProperty(linkKey3, key3);
                }
                addSDIProps.setProperty("copies", "" + repeatCnt);
                addSDIProps.setProperty("templateflag", "N");
                if (templateKey1.length() > 0) {
                    addSDIProps.setProperty("templatekeyid1", templateKey1);
                }
                if (stKeycolid2.length() > 0 && templateKey2.length() > 0) {
                    addSDIProps.setProperty("templatekeyid2", templateKey2);
                }
                if (stKeycolid3.length() > 0 && templateKey3.length() > 0) {
                    addSDIProps.setProperty("templatekeyid3", templateKey3);
                }
                addSDIProps.setProperty("processstageinstance", stgInstances.substring(1));
                addSDIProps.setProperty("label", stageLabel);
                if (stageId.length() > 0) {
                    addSDIProps.setProperty(processStageLinkKeyColname, stageId);
                    addSDIProps.setProperty("samplingplanid", spId);
                    addSDIProps.setProperty("samplingplanversionid", spVerId);
                }
                addSDIProps.setProperty("batchstagestatus", "Initial");
                addSDIProps.setProperty("auditreason", properties.getProperty("auditreason"));
                addSDIProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                addSDIProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
                String returnKeyId1 = addSDIProps.getProperty("newkeyid1", "");
                if (returnKeyId1.length() > 0) {
                    newStageId1.append(";").append(returnKeyId1);
                } else {
                    this.logger.info("Required no. of SDI(s) could not be created");
                }
                if (stKeycolid2.length() > 0) {
                    String returnKeyId2 = addSDIProps.getProperty("newkeyid2", "");
                    if (returnKeyId2.length() > 0) {
                        newStageId2.append(";").append(returnKeyId2);
                    } else {
                        this.logger.info("Required no. of SDI(s) could not be created");
                    }
                }
                if (stKeycolid3.length() <= 0) continue;
                String returnKeyId3 = addSDIProps.getProperty("newkeyid3", "");
                if (returnKeyId3.length() > 0) {
                    newStageId3.append(";").append(returnKeyId3);
                    continue;
                }
                this.logger.info("Required no. of SDI(s) could not be created");
            }
            properties.setProperty("newkeyid1", newStageId1.substring(1));
            if (stKeycolid2.length() > 0) {
                properties.setProperty("newkeyid2", newStageId2.substring(1));
            }
            if (stKeycolid3.length() > 0) {
                properties.setProperty("newkeyid3", newStageId3.substring(1));
            }
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("getmaxinstance");
            this.database.closeStatement("getpcstagedetails");
            this.database.closeStatement("getadhocstagemaxinstance");
        }
    }
}

