/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RedoStage
extends BaseAction
implements sapphire.action.RedoStage {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77320 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("SDC needs to be specified."));
        }
        String stageIds = properties.getProperty("stageid");
        String stageId2s = properties.getProperty("stageid2");
        String stageId3s = properties.getProperty("stageid3");
        if (stageIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No Stage Id specified."));
        }
        String auditReason = properties.getProperty("auditreason", "");
        String auditActivity = properties.getProperty("auditactivity", "");
        String auditSignedFlag = properties.getProperty("auditsignedflag", "");
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String stageSDCId = SamplingPlanUtil.getIntermediateStageSDCFromPolicy(sdcid, configProcessor);
        if (stageSDCId == null || stageSDCId.equals("")) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, SamplingPlan policy property \"Intermediate Stage SDC\" is not defined."));
        }
        String stageTableId = sdcProcessor.getProperty(stageSDCId, "tableid");
        String descColumnName = sdcProcessor.getProperty(stageSDCId, "desccol");
        tokenMap.put("stageSDCId", stageSDCId);
        if (stageTableId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, \"Intermediate Stage SDC\" [stageSDCId] does not exist.", tokenMap));
        }
        String stageKeyColId = sdcProcessor.getProperty(stageSDCId, "keycolid1");
        String stageKeyColId2 = sdcProcessor.getProperty(stageSDCId, "keycolid2");
        String stageKeyColId3 = sdcProcessor.getProperty(stageSDCId, "keycolid3");
        if (stageKeyColId2.length() > 0 && stageId2s.trim().length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No stage keyid2 specified"));
        }
        if (stageKeyColId3.length() > 0 && stageId3s.trim().length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No stage keyid3 specified"));
        }
        String countInst = properties.getProperty("instancecount", "1");
        String copy = properties.getProperty("copy", "Y");
        DataSet dsStages = new DataSet();
        dsStages.addColumnValues("stageid", 0, stageIds, ";");
        dsStages.addColumnValues("stageid2", 0, stageId2s, ";");
        dsStages.addColumnValues("stageid3", 0, stageId3s, ";");
        dsStages.addColumnValues("instancecount", 1, countInst, ";");
        dsStages.padColumn("instancecount");
        String sdcTableId = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        HashMap<String, String> linkFilter = new HashMap<String, String>();
        linkFilter.put("linksdcid", sdcid);
        DataSet linkData = sdcProcessor.getLinksData(stageSDCId).getFilteredDataSet(linkFilter);
        String linkKey1 = "";
        String linkKey2 = "";
        String linkKey3 = "";
        tokenMap.put("sdcid", sdcid);
        if (linkData.getRowCount() > 0) {
            linkKey1 = linkData.getValue(0, "sdccolumnid", "");
            if (linkKey1.length() == 0) {
                tokenMap.put("keycolid1", keycolid1);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column [keycolid1] of SDC [sdcid].", tokenMap));
            }
            linkKey2 = linkData.getValue(0, "sdccolumnid2", "");
            if (keycolid2.length() > 0 && linkKey2.length() == 0) {
                tokenMap.put("keycolid2", keycolid2);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column [keycolid2] of SDC [sdcid].", tokenMap));
            }
            linkKey3 = linkData.getValue(0, "sdccolumnid3", "");
            if (keycolid3.length() > 0 && linkKey3.length() == 0) {
                tokenMap.put("keycolid3", keycolid3);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [stageSDCId] has no link for primary key column [keycolid3] of SDC [sdcid].", tokenMap));
            }
        } else {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, SDC [stageSDCId] is not linked to the SDC [sdcid].", tokenMap));
        }
        String processStageLinkKeyColname = "processstageid";
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT pcst.s_processstageid, sdist." + descColumnName + ", sdist.label, sdist." + linkKey1);
        if (keycolid2.length() > 0) {
            sql.append(", sdist." + linkKey2);
        }
        if (keycolid3.length() > 0) {
            sql.append(", sdist." + linkKey3);
        }
        sql.append(", pcst.s_samplingplanid, pcst.s_samplingplanversionid ");
        sql.append(" FROM " + stageTableId + " sdist LEFT OUTER JOIN s_processstage pcst ON ");
        sql.append(" sdist.samplingplanid = pcst.s_samplingplanid AND sdist.samplingplanversionid = pcst.s_samplingplanversionid AND sdist." + processStageLinkKeyColname + " = pcst.s_processstageid ").append(" WHERE sdist." + stageKeyColId + " = ?");
        if (stageKeyColId2.length() > 0) {
            sql.append(" AND sdist." + stageKeyColId2 + " = ?");
        }
        if (stageKeyColId3.length() > 0) {
            sql.append(" AND sdist." + stageKeyColId3 + " = ?");
        }
        DataSet dsApplySamplPlanProps = new DataSet();
        dsApplySamplPlanProps.addColumn("samplingplanid", 0);
        dsApplySamplPlanProps.addColumn("samplingplanversionid", 0);
        dsApplySamplPlanProps.addColumn("samplingplanlevel", 0);
        dsApplySamplPlanProps.addColumn("processstageid", 0);
        dsApplySamplPlanProps.addColumn("keyid1", 0);
        dsApplySamplPlanProps.addColumn("keyid2", 0);
        dsApplySamplPlanProps.addColumn("keyid3", 0);
        dsApplySamplPlanProps.addColumn("keyids", 0);
        dsApplySamplPlanProps.addColumn("levelid", 0);
        dsApplySamplPlanProps.addColumn("instancecount", 1);
        DataSet dsAddStage = new DataSet();
        PreparedStatement getProcessStageDetails = this.database.prepareStatement("getProcessStageDetails", sql.toString());
        sql.setLength(0);
        sql.append("SELECT levelid FROM " + sdcTableId + " WHERE " + keycolid1 + " = ?");
        if (keycolid2.length() > 0) {
            sql.append(" AND " + keycolid2 + "= ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND " + keycolid3 + "= ?");
        }
        PreparedStatement getSDILevel = this.database.prepareStatement("getSDILevel", sql.toString());
        DataSet mapSourceReturnKey = new DataSet();
        mapSourceReturnKey.addColumn("sourcestageid", 0);
        mapSourceReturnKey.addColumn("sourcestageid2", 0);
        mapSourceReturnKey.addColumn("sourcestageid3", 0);
        mapSourceReturnKey.addColumn("adhoc", 0);
        mapSourceReturnKey.addColumn("returnstageid1", 0);
        mapSourceReturnKey.addColumn("returnstageid2", 0);
        mapSourceReturnKey.addColumn("returnstageid3", 0);
        mapSourceReturnKey.addColumn("instancecount", 1);
        try {
            String[] newStageId2s;
            String newStageId3;
            String newStageId2;
            String newStageId1;
            int totStageInst = 0;
            int totAdhocStageInst = 0;
            StringBuffer returnStageIds = new StringBuffer();
            StringBuffer returnStageId2s = new StringBuffer();
            StringBuffer returnStageId3s = new StringBuffer();
            ActionProcessor ap = this.getActionProcessor();
            for (int i = 0; i < dsStages.getRowCount(); ++i) {
                DataSet dsProcessDetails;
                String stageId = dsStages.getString(i, "stageid");
                String stageId2 = dsStages.getString(i, "stageid2");
                String stageId3 = dsStages.getString(i, "stageid3");
                int instanceCnt = dsStages.getInt(i, "instancecount", 1);
                if (instanceCnt <= 0) continue;
                getProcessStageDetails.setString(1, stageId);
                if (stageKeyColId2.length() > 0) {
                    getProcessStageDetails.setString(2, stageId2);
                }
                if (stageKeyColId3.length() > 0) {
                    getProcessStageDetails.setString(3, stageId3);
                }
                if ((dsProcessDetails = new DataSet(getProcessStageDetails.executeQuery())) != null && dsProcessDetails.getRowCount() > 0) {
                    int r = mapSourceReturnKey.addRow();
                    mapSourceReturnKey.setString(r, "sourcestageid", stageId);
                    mapSourceReturnKey.setString(r, "sourcestageid2", stageId2);
                    mapSourceReturnKey.setString(r, "sourcestageid3", stageId3);
                    mapSourceReturnKey.setNumber(r, "instancecount", instanceCnt);
                    String samplingPlanId = dsProcessDetails.getString(0, "s_samplingplanid", "");
                    String samplingPlanVerId = dsProcessDetails.getString(0, "s_samplingplanversionid", "");
                    String pcStageId = dsProcessDetails.getString(0, "s_processstageid", "");
                    String stageDesc = dsProcessDetails.getString(0, descColumnName, "");
                    String stageLabel = dsProcessDetails.getString(0, "label", "");
                    String keyId1 = dsProcessDetails.getString(0, linkKey1, "");
                    StringBuffer keys = new StringBuffer(keyId1);
                    getSDILevel.setString(1, keyId1);
                    String keyId2 = "";
                    String keyId3 = "";
                    if (keycolid2.length() > 0) {
                        keyId2 = dsProcessDetails.getString(0, linkKey2, "");
                        keys.append("/").append(keyId2);
                        getSDILevel.setString(2, keyId2);
                    }
                    if (keycolid3.length() > 0) {
                        keyId3 = dsProcessDetails.getString(0, linkKey3, "");
                        keys.append("/").append(keyId3);
                        getSDILevel.setString(3, keyId3);
                    }
                    if (samplingPlanId.length() > 0) {
                        DataSet dsLevel = new DataSet(getSDILevel.executeQuery());
                        String level = "";
                        if (dsLevel != null && dsLevel.getRowCount() > 0) {
                            level = dsLevel.getString(0, "levelid", "");
                        }
                        if (level.length() == 0) {
                            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Cannot proceed. No Sampling Plan level set for the SDI:") + " " + keys);
                        }
                        int row = dsApplySamplPlanProps.addRow();
                        dsApplySamplPlanProps.setString(row, "samplingplanid", samplingPlanId);
                        dsApplySamplPlanProps.setString(row, "samplingplanversionid", samplingPlanVerId);
                        dsApplySamplPlanProps.setString(row, "keyid1", keyId1);
                        dsApplySamplPlanProps.setString(row, "keyid2", keyId2);
                        dsApplySamplPlanProps.setString(row, "keyid3", keyId3);
                        dsApplySamplPlanProps.setString(row, "processstageid", pcStageId);
                        dsApplySamplPlanProps.setString(row, "processstagedesc", stageDesc);
                        dsApplySamplPlanProps.setString(row, "processstagelabel", stageLabel);
                        dsApplySamplPlanProps.setString(row, "levelid", level);
                        dsApplySamplPlanProps.setNumber(row, "instancecount", instanceCnt);
                        totStageInst += instanceCnt;
                        mapSourceReturnKey.setString(r, "adhoc", "N");
                        continue;
                    }
                    int row = dsAddStage.addRow();
                    dsAddStage.setString(row, "stageid", stageId);
                    dsAddStage.setString(row, "stageid2", stageId2);
                    dsAddStage.setString(row, "stageid3", stageId3);
                    dsAddStage.setString(row, "stagedesc", stageDesc);
                    dsAddStage.setString(row, "stagelabel", stageLabel);
                    dsAddStage.setString(row, "keyid1", keyId1);
                    dsAddStage.setNumber(row, "instancecount", instanceCnt);
                    if (keycolid2.length() > 0) {
                        dsAddStage.setString(row, "keyid2", keyId2);
                    }
                    if (keycolid3.length() > 0) {
                        dsAddStage.setString(row, "keyid3", keyId3);
                    }
                    dsAddStage.setString(row, "templatesdcid", stageSDCId);
                    totAdhocStageInst += instanceCnt;
                    mapSourceReturnKey.setString(r, "adhoc", "Y");
                    continue;
                }
                if (stageKeyColId2.length() > 0) {
                    stageId = stageId + "/" + stageId2;
                }
                if (stageKeyColId3.length() > 0) {
                    stageId = stageId + "/" + stageId3;
                }
                tokenMap.put("stageId", stageId);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. Stage SDI: [stageId] is not available.", tokenMap));
            }
            if (dsApplySamplPlanProps.getRowCount() > 0) {
                PropertyList aspProp = new PropertyList();
                aspProp.setProperty("sdcid", sdcid);
                aspProp.setProperty("samplingplanid", dsApplySamplPlanProps.getColumnValues("samplingplanid", ";"));
                aspProp.setProperty("samplingplanversionid", dsApplySamplPlanProps.getColumnValues("samplingplanversionid", ";"));
                aspProp.setProperty("keyid1", dsApplySamplPlanProps.getColumnValues("keyid1", ";"));
                if (keycolid2.length() > 0) {
                    aspProp.setProperty("keyid2", dsApplySamplPlanProps.getColumnValues("keyid2", ";"));
                }
                if (keycolid3.length() > 0) {
                    aspProp.setProperty("keyid3", dsApplySamplPlanProps.getColumnValues("keyid3", ";"));
                }
                aspProp.setProperty("processstageid", dsApplySamplPlanProps.getColumnValues("processstageid", ";"));
                aspProp.setProperty("processstagedesc", dsApplySamplPlanProps.getColumnValues("processstagedesc", ";"));
                aspProp.setProperty("processstagelabel", dsApplySamplPlanProps.getColumnValues("processstagelabel", ";"));
                aspProp.setProperty("processstageinstancecount", dsApplySamplPlanProps.getColumnValues("instancecount", ";"));
                aspProp.setProperty("samplingplanlevel", dsApplySamplPlanProps.getColumnValues("levelid", ";"));
                aspProp.setProperty("auditactivity", auditActivity);
                aspProp.setProperty("auditreason", auditReason);
                aspProp.setProperty("auditsignedflag", auditSignedFlag);
                ap.processAction("ApplySamplingPlan", "1", aspProp);
                newStageId1 = aspProp.getProperty("newkeyid1");
                newStageId2 = aspProp.getProperty("newkeyid2");
                newStageId3 = aspProp.getProperty("newkeyid3");
                String[] newStageId1s = StringUtil.split(newStageId1, ";");
                newStageId2s = StringUtil.split(newStageId2, ";");
                String[] newStageId3s = StringUtil.split(newStageId3, ";");
                if (newStageId1.length() == 0 || totStageInst != newStageId1s.length) {
                    throw new SapphireException(tp.translate("Required no. of stage SDI(s) could not be created."));
                }
                if (stageKeyColId2.length() > 0 && (newStageId2.length() == 0 || newStageId2s.length != totStageInst)) {
                    this.logger.info("Required no. of Stage SDI(s) could not be created");
                }
                if (stageKeyColId3.length() > 0 && (newStageId3.length() == 0 || newStageId3s.length != totStageInst)) {
                    this.logger.info("Required no. of Stage SDI(s) could not be created");
                }
                this.generateReturnStageId(mapSourceReturnKey, stageKeyColId2, stageKeyColId3, "N", newStageId1s, newStageId2s, newStageId3s);
            }
            if (dsAddStage.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dsAddStage.getColumnValues("keyid1", ";"));
                if (keycolid2.length() > 0) {
                    props.setProperty("keyid2", dsAddStage.getColumnValues("keyid2", ";"));
                }
                if (keycolid3.length() > 0) {
                    props.setProperty("keyid3", dsAddStage.getColumnValues("keyid3", ";"));
                }
                props.setProperty("processstagelabel", dsAddStage.getColumnValues("stagelabel", ";"));
                props.setProperty("repeatcount", dsAddStage.getColumnValues("instancecount", ";"));
                props.setProperty("templatesdcid", dsAddStage.getColumnValues("templatesdcid", ";"));
                props.setProperty("templatekeyid1", dsAddStage.getColumnValues("stageid", ";"));
                if (stageKeyColId2.length() > 0) {
                    props.setProperty("templatekeyid2", dsAddStage.getColumnValues("stageid2", ";"));
                }
                if (stageKeyColId3.length() > 0) {
                    props.setProperty("templatekeyid3", dsAddStage.getColumnValues("stageid3", ";"));
                }
                ap.processAction("AddStage", "1", props);
                newStageId1 = props.getProperty("newkeyid1");
                newStageId2 = props.getProperty("newkeyid2");
                newStageId3 = props.getProperty("newkeyid3");
                String[] newStageId1s = StringUtil.split(newStageId1, ";");
                newStageId2s = StringUtil.split(newStageId2, ";");
                String[] newStageId3s = StringUtil.split(newStageId3, ";");
                if (newStageId1.length() == 0 || newStageId1s.length != totAdhocStageInst) {
                    throw new SapphireException(tp.translate("Required no. of stage SDI(s) could not be created."));
                }
                if (stageKeyColId2.length() > 0 && (newStageId2.length() == 0 || newStageId2s.length != totAdhocStageInst)) {
                    this.logger.info("Required no. of Stage SDI(s) could not be created");
                }
                if (stageKeyColId3.length() > 0 && (newStageId3.length() == 0 || newStageId3s.length != totAdhocStageInst)) {
                    this.logger.info("Required no. of Stage SDI(s) could not be created");
                }
                dsAddStage.addColumn("newstageids", 0);
                dsAddStage.addColumn("newstageid2s", 0);
                dsAddStage.addColumn("newstageid3s", 0);
                int stageIndx = 0;
                for (int d = 0; d < dsAddStage.getRowCount(); ++d) {
                    int instKnt = dsAddStage.getInt(d, "instancecount");
                    StringBuffer newStageKeyIds = new StringBuffer();
                    StringBuffer newStageKeyId2s = new StringBuffer();
                    StringBuffer newStageKeyId3s = new StringBuffer();
                    for (int k = 0; k < instKnt; ++k) {
                        newStageKeyIds.append(";" + newStageId1s[stageIndx]);
                        if (stageKeyColId2.length() > 0) {
                            newStageKeyId2s.append(";" + newStageId2s[stageIndx]);
                        }
                        if (stageKeyColId2.length() > 0) {
                            newStageKeyId3s.append(";" + newStageId3s[stageIndx]);
                        }
                        ++stageIndx;
                    }
                    dsAddStage.setString(d, "newstageids", newStageKeyIds.substring(1));
                    if (newStageKeyId2s.length() > 0) {
                        dsAddStage.setString(d, "newstageid2s", newStageKeyId2s.substring(1));
                    }
                    if (newStageKeyId3s.length() <= 0) continue;
                    dsAddStage.setString(d, "newstageid3s", newStageKeyId3s.substring(1));
                }
                if (copy.equalsIgnoreCase("yes") || copy.equalsIgnoreCase("Y")) {
                    this.createStageSamples(sdcid, linkData, dsAddStage, keycolid1, stageSDCId, keycolid2, keycolid3, tp, configProcessor, sdcProcessor);
                }
                this.generateReturnStageId(mapSourceReturnKey, stageKeyColId2, stageKeyColId3, "Y", newStageId1s, newStageId2s, newStageId3s);
            }
            properties.setProperty("newkeyid1", mapSourceReturnKey.getColumnValues("returnstageid1", ";").replaceAll("#semicolon#", ";"));
            this.logger.info("RedoStage stageid :: " + properties.getProperty("stageid"));
            this.logger.info("RedoStage newkeyid1 :: " + properties.getProperty("newkeyid1"));
            if (stageKeyColId2.length() > 0) {
                properties.setProperty("newkeyid2", mapSourceReturnKey.getColumnValues("returnstageid2", ";").replaceAll("#semicolon#", ";"));
            }
            if (stageKeyColId3.length() > 0) {
                properties.setProperty("newkeyid3", mapSourceReturnKey.getColumnValues("returnstageid3", ";").replaceAll("#semicolon#", ";"));
            }
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("getProcessStageDetails");
            this.database.closeStatement("getSDILevel");
            this.database.closeStatement("getCreateSDIs");
        }
    }

    private void createStageSamples(String sdcid, DataSet linkData, DataSet dsAddStage, String keycolid1, String stageSDCId, String keycolid2, String keycolid3, TranslationProcessor tp, ConfigurationProcessor configProcessor, SDCProcessor sdcProcessor) throws SapphireException {
        String SDIToCreate = SamplingPlanUtil.getSamplingPlanSDIToCreateFromPolicy(sdcid, configProcessor);
        if (SDIToCreate.length() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Cannot proceed, SamplingPlan policy property \"SDI created for SDC by this SamplingPlan\" is not defined for SDC") + " " + sdcid);
        }
        String createSDITableId = sdcProcessor.getProperty(SDIToCreate, "tableid");
        String createSDIKeyCol1 = sdcProcessor.getProperty(SDIToCreate, "keycolid1");
        String createSDIKeyCol2 = sdcProcessor.getProperty(SDIToCreate, "keycolid2");
        String createSDIKeyCol3 = sdcProcessor.getProperty(SDIToCreate, "keycolid3");
        HashMap<String, String> linkFilter = new HashMap<String, String>();
        linkFilter.put("linksdcid", sdcid);
        DataSet createSDILinkData = sdcProcessor.getLinksData(SDIToCreate).getFilteredDataSet(linkFilter);
        String createSDILinkKey1 = "";
        String createSDILinkKey2 = "";
        String createSDILinkKey3 = "";
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("sdcid", sdcid);
        tokenMap.put("SDIToCreate", SDIToCreate);
        tokenMap.put("stageSDCId", stageSDCId);
        if (linkData.getRowCount() > 0) {
            createSDILinkKey1 = createSDILinkData.getValue(0, "sdccolumnid", "");
            if (createSDILinkKey1.length() == 0) {
                tokenMap.put("keycolid1", keycolid1);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [SDIToCreate] has no link for primary key column \"[keycolid1]\" of SDC [sdcid].", tokenMap));
            }
            createSDILinkKey2 = createSDILinkData.getValue(0, "sdccolumnid2", "");
            if (keycolid2.length() > 0 && createSDILinkKey2.length() == 0) {
                tokenMap.put("keycolid2", keycolid2);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [SDIToCreate] has no link for primary key column \"[keycolid2]\" of SDC [sdcid].", tokenMap));
            }
            createSDILinkKey3 = createSDILinkData.getValue(0, "sdccolumnid3", "");
            if (keycolid3.length() > 0 && createSDILinkKey3.length() == 0) {
                tokenMap.put("keycolid3", keycolid3);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [SDIToCreate] has no link for primary key column \"[keycolid3]\" of SDC [sdcid].", tokenMap));
            }
        }
        linkFilter.put("linksdcid", stageSDCId);
        DataSet stageLinkData = sdcProcessor.getLinksData(SDIToCreate).getFilteredDataSet(linkFilter);
        String stageLinkColumnId = "";
        String stageLinkColumnId2 = "";
        String stageLinkColumnId3 = "";
        if (stageLinkData.getRowCount() > 0) {
            stageLinkColumnId = stageLinkData.getValue(0, "sdccolumnid", "");
            stageLinkColumnId2 = stageLinkData.getValue(0, "sdccolumnid2", "");
            stageLinkColumnId3 = stageLinkData.getValue(0, "sdccolumnid3", "");
        }
        if (stageLinkColumnId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [SDIToCreate] has no link for the intermediate stage SDC [stageSDCId].", tokenMap));
        }
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT " + createSDIKeyCol1);
        if (createSDIKeyCol2.length() > 0) {
            sql.append("," + createSDIKeyCol2);
        }
        if (createSDIKeyCol3.length() > 0) {
            sql.append("," + createSDIKeyCol3);
        }
        sql.append(" FROM " + createSDITableId + " WHERE " + stageLinkColumnId + " = ?");
        if (stageLinkColumnId2.length() > 0) {
            sql.append(" AND " + stageLinkColumnId2 + " = ? ");
        }
        if (stageLinkColumnId2.length() > 0) {
            sql.append(" AND " + stageLinkColumnId2 + " = ? ");
        }
        PreparedStatement getCreateSDIs = this.database.prepareStatement("getCreateSDIs", sql.toString());
        PropertyList addSDIProps = new PropertyList();
        try {
            for (int i = 0; i < dsAddStage.getRowCount(); ++i) {
                String stageId = dsAddStage.getString(i, "stageid", "");
                String stageId2 = dsAddStage.getString(i, "stageid2", "");
                String stageId3 = dsAddStage.getString(i, "stageid3", "");
                int instCnt = dsAddStage.getInt(i, "instancecount", 1);
                getCreateSDIs.setString(1, stageId);
                if (stageLinkColumnId2.length() > 0) {
                    getCreateSDIs.setString(2, stageId2);
                }
                if (stageLinkColumnId3.length() > 0) {
                    getCreateSDIs.setString(3, stageId3);
                }
                DataSet dsSamples = new DataSet(getCreateSDIs.executeQuery());
                for (int s = 0; s < dsSamples.getRowCount(); ++s) {
                    addSDIProps.setProperty("sdcid", SDIToCreate);
                    addSDIProps.setProperty("templatekeyid1", dsSamples.getString(s, createSDIKeyCol1));
                    if (createSDIKeyCol2.length() > 0) {
                        addSDIProps.setProperty("templatekeyid2", dsSamples.getString(s, createSDIKeyCol2));
                    }
                    if (createSDIKeyCol3.length() > 0) {
                        addSDIProps.setProperty("templatekeyid3", dsSamples.getString(s, createSDIKeyCol3));
                    }
                    addSDIProps.setProperty("copies", "" + instCnt);
                    addSDIProps.setProperty(stageLinkColumnId, dsAddStage.getString(i, "newstageids"));
                    if (stageLinkColumnId2.length() > 0) {
                        addSDIProps.setProperty(stageLinkColumnId2, dsAddStage.getString(i, "newstageid2s"));
                    }
                    if (stageLinkColumnId3.length() > 0) {
                        addSDIProps.setProperty(stageLinkColumnId3, dsAddStage.getString(i, "newstageid3s"));
                    }
                    this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private void generateReturnStageId(DataSet mapSourceReturnKey, String stageKeyColId2, String stageKeyColId3, String adHoc, String[] newStageId1s, String[] newStageId2s, String[] newStageId3s) {
        int stageIndx = 0;
        for (int i = 0; i < mapSourceReturnKey.getRowCount(); ++i) {
            if (!adHoc.equals(mapSourceReturnKey.getString(i, "adhoc"))) continue;
            int instKnt = mapSourceReturnKey.getInt(i, "instancecount");
            StringBuffer newStageKeyId1s = new StringBuffer();
            StringBuffer newStageKeyId2s = new StringBuffer();
            StringBuffer newStageKeyId3s = new StringBuffer();
            for (int k = 0; k < instKnt; ++k) {
                newStageKeyId1s.append(";" + newStageId1s[stageIndx]);
                if (stageKeyColId2.length() > 0) {
                    newStageKeyId2s.append(";" + newStageId2s[stageIndx]);
                }
                if (stageKeyColId3.length() > 0) {
                    newStageKeyId3s.append(";" + newStageId3s[stageIndx]);
                }
                ++stageIndx;
            }
            mapSourceReturnKey.setString(i, "returnstageid1", newStageKeyId1s.substring(1));
            if (newStageKeyId2s.length() > 0) {
                mapSourceReturnKey.setString(i, "returnstageid2", newStageKeyId2s.substring(1));
            }
            if (newStageKeyId3s.length() <= 0) continue;
            mapSourceReturnKey.setString(i, "returnstageid3", newStageKeyId3s.substring(1));
        }
    }
}

