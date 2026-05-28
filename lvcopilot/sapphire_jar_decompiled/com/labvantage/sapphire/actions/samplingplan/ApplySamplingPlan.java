/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ApplySamplingPlan
extends BaseAction
implements sapphire.action.ApplySamplingPlan {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89107 $";
    String SDIToCreate = "";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sizeColId;
        if (!properties.containsKey("sdcid")) {
            this.applySamplingPlanToSamples(properties);
            return;
        }
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String return_message = "success";
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        String returnKeys = "";
        long startTime = System.currentTimeMillis();
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No keyid1 specified");
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String singleSDIName = sdcProcessor.getProperty(sdcid, "singular");
        singleSDIName = StringUtil.initCaps(singleSDIName);
        DataSet dsBatches = new DataSet();
        dsBatches.addColumn("samplingplanid", 0);
        dsBatches.addColumn("samplingplanversionid", 0);
        dsBatches.addColumn("samplingplanlevel", 0);
        dsBatches.addColumnValues("keyid1", 0, keyid1, ";");
        String[] keyid1prop = StringUtil.split(keyid1, ";");
        String[] keyid2prop = StringUtil.split(keyid2, ";");
        String[] keyid3prop = StringUtil.split(keyid3, ";");
        if (keycolid2.length() > 0) {
            if (keyid2.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid2 specified"));
            }
            if (keyid1prop.length != keyid2prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid2 not matching with count of keyid1"));
            }
            dsBatches.addColumnValues("keyid2", 0, keyid2, ";");
        }
        if (keycolid3.length() > 0) {
            if (keyid3.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid3 specified"));
            }
            if (keyid1prop.length != keyid3prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid3 not matching with count of keyid1"));
            }
            dsBatches.addColumnValues("keyid3", 0, keyid3, ";");
        }
        String prodVariantType = properties.getProperty("prodvarianttype");
        String samplingPlanLevel = properties.getProperty("samplingplanlevel");
        String spId = properties.getProperty("samplingplanid");
        String spVersionId = properties.getProperty("samplingplanversionid");
        String procesStageId = properties.getProperty("processstageid");
        boolean redoStageMode = procesStageId.length() > 0;
        String procesStageInstanceCount = properties.getProperty("processstageinstancecount");
        String processStageDesc = properties.getProperty("processstagedesc");
        String processStageLabel = properties.getProperty("processstagelabel");
        if (samplingPlanLevel.length() > 0) {
            if (spId.length() == 0 || spVersionId.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("SamplingPlan Id/Version Id missing for the Sampling Plan level."));
            }
            dsBatches.addColumnValues("samplingplanid", 0, spId, ";");
            dsBatches.addColumnValues("samplingplanversionid", 0, spVersionId, ";");
            dsBatches.addColumnValues("samplingplanlevel", 0, samplingPlanLevel, ";");
            dsBatches.padColumn("samplingplanlevel");
            dsBatches.padColumn("samplingplanid");
            dsBatches.padColumn("samplingplanversionid");
            if (redoStageMode) {
                dsBatches.addColumnValues("processstageid", 0, procesStageId, ";");
                dsBatches.addColumnValues("processstageinstancecount", 0, procesStageInstanceCount, ";");
                dsBatches.addColumnValues("processstagedesc", 0, processStageDesc, ";");
                dsBatches.addColumnValues("processstagelabel", 0, processStageLabel, ";");
                dsBatches.padColumn("processstageid");
                dsBatches.padColumn("processstageinstancecount");
                dsBatches.padColumn("processstagelabel");
                dsBatches.padColumn("processstagedesc");
            }
        } else if (prodVariantType.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No ProdVariantType specified"));
        }
        String prodVariantId = properties.getProperty("prodvariantid", "");
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        this.SDIToCreate = SamplingPlanUtil.getSamplingPlanSDIToCreateFromPolicy(sdcid, configProcessor);
        String basedOnSDC = SamplingPlanUtil.getBaseSDCFromPolicy(sdcid, configProcessor);
        if (this.SDIToCreate.length() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Cannot proceed, SamplingPlan policy property \"SDI created for SDC by this SamplingPlan\" is not defined.") + " ");
        }
        String SDIToCreateDescCol = this.getDescColForSDI(this.SDIToCreate);
        String createSDIKeyCol2 = sdcProcessor.getProperty(this.SDIToCreate, "keycolid2");
        String createSDIKeyCol3 = sdcProcessor.getProperty(this.SDIToCreate, "keycolid3");
        String createSDITableId = sdcProcessor.getProperty(this.SDIToCreate, "tableid");
        HashMap<String, String> linkFilter = new HashMap<String, String>();
        String stageSDCId = SamplingPlanUtil.getIntermediateStageSDCFromPolicy(sdcid, configProcessor);
        linkFilter.put("linksdcid", stageSDCId);
        DataSet stLinkData = sdcProcessor.getLinksData(this.SDIToCreate).getFilteredDataSet(linkFilter);
        String stLinkKey1 = "";
        String stLinkKey2 = "";
        String stLinkKey3 = "";
        if (stLinkData.getRowCount() > 0) {
            stLinkKey1 = stLinkData.getValue(0, "sdccolumnid", "");
            stLinkKey2 = stLinkData.getValue(0, "sdccolumnid2", "");
            stLinkKey3 = stLinkData.getValue(0, "sdccolumnid3", "");
        }
        if ((sizeColId = SamplingPlanUtil.getSamplingPlanSizeColumnId(sdcid, configProcessor)).length() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Cannot proceed, SamplingPlan policy property \"Size Column\" is not defined.") + " ");
        }
        StringBuffer sql = new StringBuffer();
        StringBuffer orderBy = new StringBuffer();
        sql.setLength(0);
        sql.append("SELECT DISTINCT spd.*").append(" ,pcst.label, pcst.processstagedesc, pcst.repeatcount, ").append("  pcst.templatesdcid stagetemplatesdcid, pcst.templatekeyid1 stagetemplatekeyid1, pcst.templatekeyid2 stagetemplatekeyid2, pcst.templatekeyid3 stagetemplatekeyid3 FROM s_spdetail spd ").append(" LEFT OUTER JOIN s_processstage pcst ON spd.processstageid = pcst.s_processstageid AND spd.s_samplingplanid = pcst.s_samplingplanid AND spd.s_samplingplanversionid = pcst.s_samplingplanversionid").append(" WHERE spd.s_samplingplanid = ? AND spd.s_samplingplanversionid = ?  AND spd.levelid = ? ORDER BY spd.usersequence");
        PreparedStatement selectSPDetails = this.database.prepareStatement("spdetails", sql.toString());
        sql.setLength(0);
        sql.append("SELECT DISTINCT spi.itemsdcid, spi.itemkeyid1, spi.itemkeyid2, spi.itemkeyid3, spdi.s_samplingplandetailno, spi.usersequence spitemusersequence, spi.s_samplingplanitemno ").append(" FROM s_spitem spi, s_spdetailitem spdi ").append(" WHERE spdi.s_samplingplanid = ? AND spdi.s_samplingplanversionid = ? ").append(" AND spi.s_samplingplanid = spdi.s_samplingplanid AND spi.s_samplingplanversionid = spdi.s_samplingplanversionid").append(" AND spi.s_samplingplanitemno = spdi.s_samplingplanitemno order by spitemusersequence, spi.s_samplingplanitemno");
        PreparedStatement selectSPItems = this.database.prepareStatement("spitems", sql.toString());
        PreparedStatement selectPCStages = this.database.prepareStatement("getPCStages", "SELECT * FROM s_processstage where s_samplingplanid =? and s_samplingplanversionid = ?");
        DataSet columnMap = SamplingPlanUtil.getSamplingPlanColumnMapFromPolicy(sdcid, prodVariantType, configProcessor);
        tokenMap.put("sdcid", sdcid);
        if (columnMap.size() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Cannot proceed, column mapping between ProdVariant and [sdcid] is not defined in the SamplingPlan policy.", tokenMap) + " ");
        }
        linkFilter.put("linksdcid", sdcid);
        DataSet linkData = sdcProcessor.getLinksData(this.SDIToCreate).getFilteredDataSet(linkFilter);
        String linkKey1 = "";
        String linkKey2 = "";
        String linkKey3 = "";
        if (linkData.getRowCount() > 0) {
            linkKey1 = linkData.getValue(0, "sdccolumnid", "");
            linkKey2 = linkData.getValue(0, "sdccolumnid2", "");
            linkKey3 = linkData.getValue(0, "sdccolumnid3", "");
        }
        sql.setLength(0);
        sql.append("SELECT max(usersequence) maxseq FROM " + createSDITableId + " WHERE " + linkKey1 + "= ?");
        if (linkKey2.length() > 0) {
            sql.append(" AND " + linkKey2 + "= ?");
        }
        if (linkKey3.length() > 0) {
            sql.append(" AND " + linkKey3 + "= ?");
        }
        PreparedStatement getMaxUserSequence = this.database.prepareStatement("maxuseq", sql.toString());
        HashMap<String, Integer> mapUseq = new HashMap<String, Integer>();
        DataSet sampPlanInfoColMap = SamplingPlanUtil.getSamplingPlanUpdateColumnMapFromPolicy(sdcid, configProcessor, "columnstobepopulated");
        ArrayList<String> columnNames = new ArrayList<String>();
        sql.setLength(0);
        sql.append("SELECT  " + sizeColId + ", prodvariantid");
        for (int i = 0; i < columnMap.getRowCount(); ++i) {
            String sdcColId = columnMap.getString(i, "sdccolumnid", "");
            if (sdcColId.length() <= 0) continue;
            sql.append(" ," + sdcColId);
        }
        sql.append(" FROM " + tableid + " WHERE " + keycolid1 + "= ?");
        if (keycolid2.length() > 0) {
            sql.append(" AND " + keycolid2).append("= ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND " + keycolid3).append("= ?");
        }
        PreparedStatement getSDIDetails = this.database.prepareStatement("sdidetail", sql.toString());
        DataSet prodVariantRuleLevel = this.getQueryProcessor().getRefTypeDataSet("ProdVariantRuleLevel");
        sql.setLength(0);
        try {
            int i;
            DataSet dsActionProps = new DataSet();
            dsActionProps.addColumn("sdcid", 0);
            dsActionProps.addColumn("keyid1", 0);
            dsActionProps.addColumn("keyid2", 0);
            dsActionProps.addColumn("keyid3", 0);
            dsActionProps.addColumn("templatekeyid1", 0);
            dsActionProps.addColumn("templatekeyid2", 0);
            dsActionProps.addColumn("templatekeyid3", 0);
            dsActionProps.addColumn("samplingplanid", 0);
            dsActionProps.addColumn("samplingplanversionid", 0);
            dsActionProps.addColumn("stagecounter", 0);
            dsActionProps.addColumn("s_samplingplandetailno", 0);
            dsActionProps.addColumn("copies", 1);
            dsActionProps.addColumn("processstageid", 0);
            dsActionProps.addColumn("securitydepartment", 0);
            DataSet dsMultiActionProps = new DataSet();
            dsMultiActionProps.addColumn("sdcid", 0);
            dsMultiActionProps.addColumn("keyid1", 0);
            dsMultiActionProps.addColumn("keyid2", 0);
            dsMultiActionProps.addColumn("keyid3", 0);
            dsMultiActionProps.addColumn("itemsdcid", 0);
            dsMultiActionProps.addColumn("itemkeyid1", 0);
            dsMultiActionProps.addColumn("itemkeyid2", 0);
            dsMultiActionProps.addColumn("itemkeyid3", 0);
            DataSet dsIndivSDIProps = new DataSet();
            dsIndivSDIProps.addColumn("sdcid", 0);
            dsIndivSDIProps.addColumn("keyid1", 0);
            dsIndivSDIProps.addColumn("keyid2", 0);
            dsIndivSDIProps.addColumn("keyid3", 0);
            dsIndivSDIProps.addColumn("samplingplanid", 0);
            dsIndivSDIProps.addColumn("samplingplanversionid", 0);
            dsIndivSDIProps.addColumn("prodvariantid", 0);
            dsIndivSDIProps.addColumn("levelid", 0);
            DataSet dsBatchStage = new DataSet();
            dsBatchStage.addColumn("sdcid", 0);
            dsBatchStage.addColumn("keyid1", 0);
            dsBatchStage.addColumn("keyid2", 0);
            dsBatchStage.addColumn("keyid3", 0);
            dsBatchStage.addColumn("processstageid", 0);
            dsBatchStage.addColumn("processstagedesc", 0);
            dsBatchStage.addColumn("processstagelabel", 0);
            dsBatchStage.addColumn("repeatcount", 1);
            dsBatchStage.addColumn("samplingplanid", 0);
            dsBatchStage.addColumn("samplingplanversionid", 0);
            dsBatchStage.addColumn("stagecounter", 0);
            HashMap<String, DataSet> spItemMap = new HashMap<String, DataSet>();
            HashMap<String, DataSet> spProcessStagesMap = new HashMap<String, DataSet>();
            for (i = 0; i < sampPlanInfoColMap.getRowCount(); ++i) {
                String sdiColumnId = sampPlanInfoColMap.getString(i, "sdicolumn", "");
                dsActionProps.addColumn(sdiColumnId, 0);
                if (columnNames.contains(sdiColumnId)) continue;
                columnNames.add(sdiColumnId);
            }
            for (i = 0; i < columnMap.getRowCount(); ++i) {
                String sdccolumnid = columnMap.getString(i, "sdccolumnid", "");
                dsActionProps.addColumn(sdccolumnid, 0);
                if (columnNames.contains(sdccolumnid)) continue;
                columnNames.add(sdccolumnid);
                dsIndivSDIProps.addColumn(sdccolumnid, 0);
            }
            if (linkData.getRowCount() > 0) {
                String linkColId3;
                String linkColId2;
                String linkColId1 = linkData.getValue(0, "sdccolumnid", "");
                if (linkColId1.length() > 0) {
                    dsActionProps.addColumn(linkColId1, 0);
                    if (!columnNames.contains(linkColId1)) {
                        columnNames.add(linkColId1);
                    }
                }
                if ((linkColId2 = linkData.getValue(0, "sdccolumnid2", "")).length() > 0 && keycolid2.length() > 0) {
                    dsActionProps.addColumn(linkColId2, 0);
                    if (!columnNames.contains(linkColId2)) {
                        columnNames.add(linkColId2);
                    }
                }
                if ((linkColId3 = linkData.getValue(0, "sdccolumnid3", "")).length() > 0 && keycolid3.length() > 0) {
                    dsActionProps.addColumn(linkColId3, 0);
                    if (!columnNames.contains(linkColId3)) {
                        columnNames.add(linkColId3);
                    }
                }
            }
            ActionProcessor ap = this.getActionProcessor();
            HashMap<String, Integer> pcStageInputTrack = new HashMap<String, Integer>();
            for (int i2 = 0; i2 < dsBatches.getRowCount(); ++i2) {
                DataSet dsProdVars = new DataSet();
                String levelForProdVar = "";
                String keyIds = keyid1prop[i2];
                String prodVarId = "";
                String samplingPlanId = dsBatches.getString(i2, "samplingplanid", "");
                String samplingPlanVersionId = dsBatches.getString(i2, "samplingplanversionid");
                String levelLabel = dsBatches.getString(i2, "samplingplanlevel", "");
                String processStageInputId = dsBatches.getString(i2, "processstageid", "");
                String processStageInstCnt = dsBatches.getString(i2, "processstageinstancecount", "");
                int pcStageInstKnt = 0;
                if (processStageInputId.length() > 0) {
                    try {
                        pcStageInstKnt = Integer.parseInt(processStageInstCnt);
                    }
                    catch (NumberFormatException ne) {
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("Invalid process stage instance count in the action input"));
                    }
                }
                String inputStageDesc = dsBatches.getString(i2, "processstagedesc", "");
                String inputStageLabel = dsBatches.getString(i2, "processstagelabel", "");
                String size = "";
                String keyId2 = "";
                String keyId3 = "";
                if (keycolid2.length() > 0) {
                    keyIds = keyIds + "/" + keyid2prop[i2];
                    keyId2 = keyid2prop[i2];
                }
                if (keycolid3.length() > 0) {
                    keyIds = keyIds + "/" + keyid3prop[i2];
                    keyId3 = keyid3prop[i2];
                }
                getSDIDetails.setString(1, keyid1prop[i2]);
                if (keycolid2.length() > 0) {
                    getSDIDetails.setString(2, keyid2prop[i2]);
                }
                if (keycolid3.length() > 0) {
                    getSDIDetails.setString(3, keyid3prop[i2]);
                }
                if (levelLabel.length() == 0) {
                    SDIData prodVariantSDIData;
                    DataSet dsSDIDetails = new DataSet(getSDIDetails.executeQuery());
                    SDIRequest prodVariantRequest = new SDIRequest();
                    prodVariantRequest.setSDCid("LV_ProdVariant");
                    prodVariantRequest.setRequestItem("primary");
                    prodVariantRequest.setQueryFrom("s_prodvariant");
                    StringBuilder whereClause = new StringBuilder();
                    StringBuilder orderByClause = new StringBuilder();
                    whereClause.append("prodvarianttype = '").append(SafeSQL.encodeForSQL(prodVariantType, this.database.isOracle())).append("'");
                    if (prodVariantId.trim().length() > 0) {
                        whereClause.append(" AND s_prodvariantid = '").append(SafeSQL.encodeForSQL(prodVariantId, this.database.isOracle())).append("'");
                    }
                    for (int j = 0; j < columnMap.getRowCount(); ++j) {
                        String prodVarColId = columnMap.getString(j, "prodvariantcolumnid", "");
                        String sdcColId = columnMap.getString(j, "sdccolumnid", "");
                        if (prodVarColId.length() <= 0) continue;
                        if (prodVariantId.trim().length() == 0) {
                            whereClause.append(" AND ( ").append(prodVarColId).append(" = '").append(SafeSQL.encodeForSQL(dsSDIDetails.getString(0, sdcColId), this.database.isOracle())).append("'");
                            whereClause.append(" OR (").append(prodVarColId).append(" IS NULL OR ").append(prodVarColId).append("= ''))");
                        }
                        orderByClause.append(orderByClause.length() == 0 ? " " : ",");
                        orderByClause.append(" case when ").append(prodVarColId).append(" is null then 100 else 0 end ");
                    }
                    if (whereClause.length() > 0) {
                        prodVariantRequest.setQueryWhere(whereClause.toString());
                    }
                    if (orderByClause.length() > 0) {
                        prodVariantRequest.setQueryOrderBy(orderByClause.toString());
                    }
                    if ((dsProdVars = (prodVariantSDIData = this.getSDIProcessor().getSDIData(prodVariantRequest)).getDataset("primary")).getRowCount() == 0) {
                        tokenMap.put("singleSDIName", singleSDIName);
                        tokenMap.put("keyIds", keyIds);
                        this.logger.info("EMPTY_RESULTSET", tp.translate("No matching ProdVariant found for the [singleSDIName]: [keyIds]", tokenMap));
                    } else {
                        int idx = 0;
                        if (idx < 1) {
                            prodVarId = dsProdVars.getValue(idx, "s_prodvariantid", "");
                            String ruleId = dsProdVars.getValue(idx, "prodvariantruleid", "");
                            String stateId = dsProdVars.getValue(idx, "currentstateid", "");
                            samplingPlanId = dsProdVars.getValue(idx, "samplingplanid", "");
                            samplingPlanVersionId = dsProdVars.getValue(idx, "samplingplanversionid", "");
                            if (samplingPlanVersionId.length() == 0) {
                                samplingPlanVersionId = SamplingPlanUtil.getCurrentOrMaxPVersion(samplingPlanId, this.getQueryProcessor());
                            }
                            Calendar lastTransactiondate = dsProdVars.getCalendar(idx, "lasttransitiondt");
                            this.logger.info("sampling plan for prodvariant : " + prodVarId + " is - " + samplingPlanId);
                            size = dsSDIDetails.getValue(idx, sizeColId, "");
                            HashMap sdcColProdVarValMap = SamplingPlanUtil.createSDCColIdProdVariantValueMap(columnMap, dsProdVars);
                            StringBuffer sqlStr = new StringBuffer();
                            SafeSQL safeSQL = new SafeSQL();
                            sqlStr.append("SELECT * FROM( ").append(" SELECT DISTINCT 0 AS OrdKey, s_levelruleid, levelrule, levelruletype, levellabel, usersequence ").append(" FROM s_pvrlevelrule WHERE s_prodvariantruleid = ").append(safeSQL.addVar(ruleId)).append(" AND ").append(" s_stateid = ").append(safeSQL.addVar(stateId)).append(" and levelruletype != 'Otherwise' ").append(" union ").append(" SELECT DISTINCT 1 AS OrdKey, s_levelruleid, levelrule, levelruletype, levellabel, usersequence ").append(" FROM s_pvrlevelrule WHERE s_prodvariantruleid = ").append(safeSQL.addVar(ruleId)).append(" AND ").append("s_stateid = ").append(safeSQL.addVar(stateId)).append(" and levelruletype = 'Otherwise' ").append(" ) ").append(" TBL  ORDER BY  OrdKey, usersequence");
                            DataSet dsLevels = this.getQueryProcessor().getPreparedSqlDataSet(sqlStr.toString(), safeSQL.getValues());
                            if (dsLevels.getRowCount() == 0) {
                                tokenMap.put("prodVarId", prodVarId);
                                this.logger.info("EMPTY_RESULTSET", tp.translate("No levels defined for the ProdVariant : '[prodVarId]'", tokenMap));
                            } else {
                                int k;
                                this.logger.info("starting the execution of level rules");
                                for (k = 0; k < dsLevels.getRowCount(); ++k) {
                                    String levelRuleid = dsLevels.getValue(k, "s_levelruleid", "");
                                    String levelRule = dsLevels.getValue(k, "levelrule", "");
                                    String levelRuleType = dsLevels.getValue(k, "levelruletype", "");
                                    levelLabel = dsLevels.getValue(k, "levellabel", "");
                                    this.logger.info("evaluating level rule : " + levelRule);
                                    if (!SamplingPlanUtil.evaluateLevelRule(levelLabel, levelRuleType, levelRule, sdcid, tableid, sdcColProdVarValMap, this.getConfigurationProcessor(), this.getQueryProcessor(), tp, lastTransactiondate, this.connectionInfo.getDbms())) continue;
                                    this.logger.info("evaluation of level rule returned true");
                                    levelForProdVar = levelLabel;
                                    break;
                                }
                                if (k == dsLevels.getRowCount()) {
                                    tokenMap.put("singleSDIName", singleSDIName);
                                    tokenMap.put("keyIds", keyIds);
                                    this.logger.info(tp.translate("Level could not be determined for the supplier of the [singleSDIName]: [keyIds]", tokenMap));
                                }
                            }
                        }
                    }
                } else {
                    dsProdVars = new DataSet(getSDIDetails.executeQuery());
                    size = dsProdVars.getValue(0, sizeColId, "");
                    prodVarId = dsProdVars.getValue(0, "prodvariantid", "");
                    levelForProdVar = levelLabel;
                }
                if (levelForProdVar.length() == 0) continue;
                int findLevel = prodVariantRuleLevel.findRow("refdisplayvalue", levelLabel);
                String levelId = "";
                if (findLevel > -1) {
                    levelId = prodVariantRuleLevel.getValue(findLevel, "refvalueid");
                }
                selectSPDetails.setString(1, samplingPlanId);
                selectSPDetails.setString(2, samplingPlanVersionId);
                selectSPDetails.setString(3, levelId.length() > 0 ? levelId : levelLabel);
                DataSet dsSPDetails = new DataSet(selectSPDetails.executeQuery());
                ArrayList<String> pcStageInserted = new ArrayList<String>();
                if (dsSPDetails.getRowCount() == 0) {
                    tokenMap.put("samplingPlanId", samplingPlanId);
                    tokenMap.put("samplingPlanVersionId", samplingPlanVersionId);
                    tokenMap.put("levelLabel", levelLabel);
                    return_message = tp.translate("No sampling plan information available for SamplingPlan Id: '[samplingPlanId]' version: '[samplingPlanVersionId]' and  Level: '[levelLabel]'", tokenMap);
                    this.logger.info("EMPTY_RESULTSET", tp.translate("No sampling plan information available for SamplingPlan Id: '[samplingPlanId]' version: '[samplingPlanVersionId]' and  Level: '[levelLabel]'", tokenMap));
                } else {
                    if (!spProcessStagesMap.containsKey(samplingPlanId + ";" + samplingPlanVersionId)) {
                        selectPCStages.setString(1, samplingPlanId);
                        selectPCStages.setString(2, samplingPlanVersionId);
                        DataSet dsProcessStages = new DataSet(selectPCStages.executeQuery());
                        if (dsProcessStages.getRowCount() > 0) {
                            spProcessStagesMap.put(samplingPlanId + ";" + samplingPlanVersionId, dsProcessStages);
                        }
                    }
                    if (!spItemMap.containsKey(samplingPlanId + ";" + samplingPlanVersionId)) {
                        selectSPItems.setString(1, samplingPlanId);
                        selectSPItems.setString(2, samplingPlanVersionId);
                        DataSet dsSPItem = new DataSet(selectSPItems.executeQuery());
                        ArrayList<DataSet> spListUseqGrps = dsSPItem.getGroupedDataSets("spitemusersequence");
                        int seq = 0;
                        DataSet dsNullGrp = new DataSet();
                        for (int u = 0; u < spListUseqGrps.size(); ++u) {
                            DataSet dsGrp = spListUseqGrps.get(u);
                            if (dsGrp.isNull(0, "spitemusersequence")) {
                                dsNullGrp = dsGrp;
                                continue;
                            }
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("itemsdcid", dsGrp.getString(0, "itemsdcid"));
                            filter.put("itemkeyid1", dsGrp.getString(0, "itemkeyid1"));
                            filter.put("itemkeyid2", dsGrp.getString(0, "itemkeyid2"));
                            filter.put("itemkeyid3", dsGrp.getString(0, "itemkeyid3"));
                            int filterCount = dsGrp.getFilteredDataSet(filter).getRowCount();
                            if (dsGrp.getRowCount() != filterCount) {
                                for (int g = 0; g < dsGrp.getRowCount(); ++g) {
                                    dsGrp.setNumber(g, "spitemusersequence", ++seq);
                                }
                                continue;
                            }
                            dsGrp.setNumber(-1, "spitemusersequence", ++seq);
                        }
                        if (dsNullGrp.getRowCount() > 0) {
                            for (int g = 0; g < dsNullGrp.getRowCount(); ++g) {
                                dsNullGrp.setNumber(g, "spitemusersequence", ++seq);
                            }
                        }
                        spItemMap.put(samplingPlanId + ";" + samplingPlanVersionId, dsSPItem);
                    }
                    int pcStageCtr = 0;
                    if (processStageInputId.length() > 0) {
                        if (pcStageInputTrack.containsKey(processStageInputId)) {
                            pcStageCtr = (Integer)pcStageInputTrack.get(processStageInputId);
                            pcStageInputTrack.put(processStageInputId, ++pcStageCtr);
                        } else {
                            pcStageCtr = 1;
                            pcStageInputTrack.put(processStageInputId, pcStageCtr);
                        }
                    }
                    for (int j = 0; j < dsSPDetails.getRowCount(); ++j) {
                        String sdcColId3;
                        String sdcColId2;
                        int ctr;
                        String countRule = dsSPDetails.getValue(j, "countrule", "");
                        String countRuleType = dsSPDetails.getValue(j, "countruletype", "");
                        String templateSDCId = dsSPDetails.getValue(j, "templatesdcid", "");
                        String templKey1 = dsSPDetails.getValue(j, "templatekeyid1", "");
                        String templKey2 = dsSPDetails.getValue(j, "templatekeyid2", "");
                        String templKey3 = dsSPDetails.getValue(j, "templatekeyid3", "");
                        String sourceLabel = dsSPDetails.getValue(j, "sourcelabel", "");
                        String department = dsSPDetails.getValue(j, "defaultdepartmentid", "");
                        String detailNo = dsSPDetails.getValue(j, "s_samplingplandetailno", "");
                        String processStageid = dsSPDetails.getValue(j, "processstageid", "");
                        String stageTemplateSDCId = dsSPDetails.getValue(j, "stagetemplatesdcid", "");
                        String stageTemplateKeyId1 = dsSPDetails.getValue(j, "stagetemplatekeyid1", "");
                        String stageTemplateKeyId2 = dsSPDetails.getValue(j, "stagetemplatekeyid2", "");
                        String stageTemplateKeyId3 = dsSPDetails.getValue(j, "stagetemplatekeyid3", "");
                        if (processStageInputId.length() > 0 && !processStageInputId.equals(processStageid)) continue;
                        if (processStageInputId.equals(processStageid)) {
                            dsSPDetails.setNumber(j, "repeatcount", pcStageInstKnt);
                            dsSPDetails.setString(j, "processstagedesc", inputStageDesc);
                            dsSPDetails.setString(j, "label", inputStageLabel);
                        }
                        int copies = SamplingPlanUtil.evaluateCountRule(this.getQueryProcessor(), countRule, countRuleType, size, "", this.getConfigurationProcessor(), this.getSDCProcessor(), prodVarId, properties, keyid1prop[i2], keyId2, keyId3);
                        int row = dsActionProps.addRow();
                        dsActionProps.setString(row, "sdcid", sdcid);
                        dsActionProps.setString(row, "keyid1", keyid1prop[i2]);
                        if (keycolid2.length() > 0) {
                            dsActionProps.setString(row, "keyid2", keyid2prop[i2]);
                        }
                        if (keycolid3.length() > 0) {
                            dsActionProps.setString(row, "keyid3", keyid3prop[i2]);
                        }
                        dsActionProps.setString(row, "keyids", keyIds);
                        if (processStageid.length() > 0) {
                            String repeats = dsSPDetails.getValue(j, "repeatcount", "1");
                            int repeatCnt = Integer.parseInt(repeats);
                            String stageDesc = dsSPDetails.getValue(j, "processstagedesc", "");
                            String stageLabel = dsSPDetails.getValue(j, "label", "");
                            HashMap<String, String> findMap = new HashMap<String, String>();
                            findMap.put("keyids", keyIds);
                            findMap.put("processstageid", processStageid);
                            int fndRow = dsBatchStage.findRow(findMap);
                            if (repeatCnt > 0 && (pcStageCtr == 0 && fndRow < 0 || !pcStageInserted.contains(processStageid))) {
                                int bstRow = dsBatchStage.addRow();
                                dsBatchStage.setString(bstRow, "sdcid", sdcid);
                                dsBatchStage.setString(bstRow, "keyid1", keyid1prop[i2]);
                                dsBatchStage.setString(bstRow, "keyid2", keyId2);
                                dsBatchStage.setString(bstRow, "keyid3", keyId3);
                                dsBatchStage.setString(bstRow, "processstageid", processStageid);
                                dsBatchStage.setString(bstRow, "processstagedesc", stageDesc);
                                dsBatchStage.setString(bstRow, "processstagelabel", stageLabel);
                                dsBatchStage.setNumber(bstRow, "repeatcount", repeatCnt);
                                dsBatchStage.setString(bstRow, "keyids", keyIds);
                                dsBatchStage.setString(bstRow, "samplingplanid", samplingPlanId);
                                dsBatchStage.setString(bstRow, "samplingplanversionid", samplingPlanVersionId);
                                dsBatchStage.setString(bstRow, "stagecounter", "" + pcStageCtr);
                                dsBatchStage.setString(bstRow, "stagetemplatesdcid", stageTemplateSDCId);
                                dsBatchStage.setString(bstRow, "stagetemplatekeyid1", stageTemplateKeyId1);
                                dsBatchStage.setString(bstRow, "stagetemplatekeyid2", stageTemplateKeyId2);
                                dsBatchStage.setString(bstRow, "stagetemplatekeyid3", stageTemplateKeyId3);
                                pcStageInserted.add(processStageid);
                            }
                            dsActionProps.setString(row, "processstageid", processStageid);
                            dsActionProps.setNumber(row, "repeatcount", repeatCnt);
                            dsActionProps.setString(row, "stagecounter", "" + pcStageCtr);
                        }
                        dsActionProps.setString(row, "templatekeyid1", templKey1);
                        dsActionProps.setString(row, "templatekeyid2", templKey2);
                        dsActionProps.setString(row, "templatekeyid3", templKey3);
                        dsActionProps.setString(row, "samplingplanid", samplingPlanId);
                        dsActionProps.setString(row, "samplingplanversionid", samplingPlanVersionId);
                        dsActionProps.setNumber(row, "copies", copies);
                        dsActionProps.setString(row, "s_samplingplandetailno", detailNo);
                        dsActionProps.setString(row, "securitydepartment", department);
                        for (ctr = 0; ctr < sampPlanInfoColMap.getRowCount(); ++ctr) {
                            String sdiColumnId = sampPlanInfoColMap.getString(ctr, "sdicolumn", "");
                            String planColumnId = sampPlanInfoColMap.getString(ctr, "column", "");
                            if (planColumnId.equalsIgnoreCase("samplingplanid")) {
                                dsActionProps.setString(row, sdiColumnId, samplingPlanId);
                                continue;
                            }
                            if (planColumnId.equalsIgnoreCase("samplingplanversionid")) {
                                dsActionProps.setString(row, sdiColumnId, samplingPlanVersionId);
                                continue;
                            }
                            dsActionProps.setString(row, sdiColumnId, dsSPDetails.getValue(j, planColumnId));
                        }
                        if (dsProdVars.getRowCount() > 0) {
                            for (ctr = 0; ctr < columnMap.getRowCount(); ++ctr) {
                                String sdiColumnId = columnMap.getString(ctr, "sdccolumnid", "");
                                dsActionProps.setString(row, sdiColumnId, dsProdVars.getValue(0, sdiColumnId, ""));
                            }
                        }
                        if (linkData.getRowCount() <= 0) continue;
                        String sdcColId1 = linkData.getValue(0, "sdccolumnid", "");
                        if (sdcColId1.length() > 0) {
                            dsActionProps.setString(row, sdcColId1, keyid1prop[i2]);
                        }
                        if ((sdcColId2 = linkData.getValue(0, "sdccolumnid2", "")).length() > 0 && keycolid2.length() > 0) {
                            dsActionProps.setString(row, sdcColId2, keyid2prop[i2]);
                        }
                        if ((sdcColId3 = linkData.getValue(0, "sdccolumnid3", "")).length() <= 0 || keycolid3.length() <= 0) continue;
                        dsActionProps.setString(row, sdcColId2, keyid3prop[i2]);
                    }
                }
                if (!redoStageMode) {
                    HashMap<String, String> actionProps = new HashMap<String, String>();
                    actionProps.put("sdcid", sdcid);
                    actionProps.put("keyid1", keyid1prop[i2]);
                    if (keycolid2.length() > 0) {
                        actionProps.put("keyid2", keyid2prop[i2]);
                    }
                    if (keycolid3.length() > 0) {
                        actionProps.put("keyid3", keyid3prop[i2]);
                    }
                    actionProps.put("levelid", levelForProdVar);
                    actionProps.put("prodvariantid", prodVarId);
                    actionProps.put("samplingplanid", samplingPlanId);
                    actionProps.put("samplingplanversionid", samplingPlanVersionId);
                    ap.processAction("EditSDI", "1", actionProps);
                }
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("keyid1", keyid1prop[i2]);
                if (keycolid2.length() > 0) {
                    find.put("keyid2", keyid2prop[i2]);
                }
                if (keycolid3.length() > 0) {
                    find.put("keyid3", keyid3prop[i2]);
                }
                if (dsIndivSDIProps.findRow(find) < 0) {
                    int r = dsIndivSDIProps.addRow();
                    dsIndivSDIProps.setString(r, "sdcid", sdcid);
                    dsIndivSDIProps.setString(r, "keyid1", keyid1prop[i2]);
                    getMaxUserSequence.setString(1, keyid1prop[i2]);
                    if (keycolid2.length() > 0) {
                        dsIndivSDIProps.setString(r, "keyid2", keyid2prop[i2]);
                        getMaxUserSequence.setString(2, keyid2prop[i2]);
                    }
                    if (keycolid3.length() > 0) {
                        dsIndivSDIProps.setString(r, "keyid3", keyid3prop[i2]);
                        getMaxUserSequence.setString(3, keyid3prop[i2]);
                    }
                    DataSet dsMaxSeq = new DataSet(getMaxUserSequence.executeQuery());
                    mapUseq.put(keyIds, new Integer(dsMaxSeq.getInt(0, "maxseq", 0)));
                    dsIndivSDIProps.setString(r, "samplingplanid", samplingPlanId);
                    dsIndivSDIProps.setString(r, "samplingplanversionid", samplingPlanVersionId);
                    dsIndivSDIProps.setString(r, "prodvariantid", prodVarId);
                    dsIndivSDIProps.setString(r, "levelid", levelForProdVar);
                    if (dsProdVars.getRowCount() > 0) {
                        for (int ctr = 0; ctr < columnMap.getRowCount(); ++ctr) {
                            String sdiColumnId = columnMap.getString(ctr, "sdccolumnid", "");
                            dsIndivSDIProps.setString(r, sdiColumnId, dsProdVars.getValue(0, sdiColumnId, ""));
                        }
                    }
                }
                if (!spProcessStagesMap.containsKey(samplingPlanId + ";" + samplingPlanVersionId)) continue;
                DataSet dsStages = (DataSet)spProcessStagesMap.get(samplingPlanId + ";" + samplingPlanVersionId);
                if (processStageInputId.length() == 0) {
                    for (int s = 0; s < dsStages.getRowCount(); ++s) {
                        String pcStageId = dsStages.getString(s, "s_processstageid", "");
                        int rcnt = dsStages.getInt(s, "repeatcount", 0);
                        HashMap<String, String> findMap = new HashMap<String, String>();
                        findMap.put("keyids", keyIds);
                        findMap.put("processstageid", pcStageId);
                        if (rcnt <= 0 || dsBatchStage.findRow(findMap) >= 0) continue;
                        int bstRow = dsBatchStage.addRow();
                        dsBatchStage.setString(bstRow, "sdcid", sdcid);
                        dsBatchStage.setString(bstRow, "keyid1", keyid1prop[i2]);
                        dsBatchStage.setString(bstRow, "keyid2", keyId2);
                        dsBatchStage.setString(bstRow, "keyid3", keyId3);
                        dsBatchStage.setString(bstRow, "processstageid", pcStageId);
                        dsBatchStage.setString(bstRow, "processstagedesc", dsStages.getString(s, "processstagedesc", ""));
                        dsBatchStage.setString(bstRow, "processstagelabel", dsStages.getString(s, "label", ""));
                        dsBatchStage.setNumber(bstRow, "repeatcount", rcnt);
                        dsBatchStage.setString(bstRow, "keyids", keyIds);
                        dsBatchStage.setString(bstRow, "samplingplanid", samplingPlanId);
                        dsBatchStage.setString(bstRow, "samplingplanversionid", samplingPlanVersionId);
                        dsBatchStage.setString(bstRow, "stagecounter", "0");
                        dsBatchStage.setString(bstRow, "stagetemplatesdcid", dsStages.getString(s, "templatesdcid", ""));
                        dsBatchStage.setString(bstRow, "stagetemplatekeyid1", dsStages.getString(s, "templatekeyid1", ""));
                        dsBatchStage.setString(bstRow, "stagetemplatekeyid2", dsStages.getString(s, "templatekeyid2", ""));
                        dsBatchStage.setString(bstRow, "stagetemplatekeyid3", dsStages.getString(s, "templatekeyid3", ""));
                    }
                    continue;
                }
                if (pcStageInstKnt <= 0 || pcStageInserted.contains(processStageInputId)) continue;
                int stRow = dsStages.findRow("s_processstageid", processStageInputId);
                int bstRow = dsBatchStage.addRow();
                dsBatchStage.setString(bstRow, "sdcid", sdcid);
                dsBatchStage.setString(bstRow, "keyid1", keyid1prop[i2]);
                dsBatchStage.setString(bstRow, "keyid2", keyId2);
                dsBatchStage.setString(bstRow, "keyid3", keyId3);
                dsBatchStage.setString(bstRow, "processstageid", processStageInputId);
                dsBatchStage.setString(bstRow, "processstagedesc", inputStageDesc);
                dsBatchStage.setString(bstRow, "processstagelabel", inputStageLabel);
                dsBatchStage.setNumber(bstRow, "repeatcount", pcStageInstKnt);
                dsBatchStage.setString(bstRow, "keyids", keyIds);
                dsBatchStage.setString(bstRow, "samplingplanid", samplingPlanId);
                dsBatchStage.setString(bstRow, "samplingplanversionid", samplingPlanVersionId);
                dsBatchStage.setString(bstRow, "stagecounter", "0");
                if (stRow <= -1) continue;
                dsBatchStage.setString(bstRow, "stagetemplatesdcid", dsStages.getString(stRow, "templatesdcid", ""));
                dsBatchStage.setString(bstRow, "stagetemplatekeyid1", dsStages.getString(stRow, "templatekeyid1", ""));
                dsBatchStage.setString(bstRow, "stagetemplatekeyid2", dsStages.getString(stRow, "templatekeyid2", ""));
                dsBatchStage.setString(bstRow, "stagetemplatekeyid3", dsStages.getString(stRow, "templatekeyid3", ""));
            }
            if (dsBatchStage.getRowCount() > 0) {
                int totalInstCnt = 0;
                for (int b = 0; b < dsBatchStage.getRowCount(); ++b) {
                    int rc = dsBatchStage.getInt(b, "repeatcount");
                    totalInstCnt += rc;
                }
                PropertyList addStageProps = new PropertyList();
                addStageProps.setProperty("sdcid", sdcid);
                addStageProps.setProperty("keyid1", dsBatchStage.getColumnValues("keyid1", ";"));
                if (keycolid2.length() > 0) {
                    addStageProps.setProperty("keyid2", dsBatchStage.getColumnValues("keyid2", ";"));
                }
                if (keycolid3.length() > 0) {
                    addStageProps.setProperty("keyid3", dsBatchStage.getColumnValues("keyid3", ";"));
                }
                addStageProps.setProperty("processstageid", dsBatchStage.getColumnValues("processstageid", ";"));
                addStageProps.setProperty("processstagelabel", dsBatchStage.getColumnValues("processstagelabel", ";"));
                addStageProps.setProperty("samplingplanid", dsBatchStage.getColumnValues("samplingplanid", ";"));
                addStageProps.setProperty("samplingplanversionid", dsBatchStage.getColumnValues("samplingplanversionid", ";"));
                addStageProps.setProperty("repeatcount", dsBatchStage.getColumnValues("repeatcount", ";"));
                addStageProps.setProperty("templatesdcid", dsBatchStage.getColumnValues("stagetemplatesdcid", ";"));
                addStageProps.setProperty("templatekeyid1", dsBatchStage.getColumnValues("stagetemplatekeyid1", ";"));
                addStageProps.setProperty("templatekeyid2", dsBatchStage.getColumnValues("stagetemplatekeyid2", ";"));
                addStageProps.setProperty("templatekeyid3", dsBatchStage.getColumnValues("stagetemplatekeyid3", ";"));
                addStageProps.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
                addStageProps.setProperty("auditreason", properties.getProperty("auditreason", ""));
                addStageProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", ""));
                ap.processAction("AddStage", "1", addStageProps);
                String newIds = addStageProps.getProperty("newkeyid1");
                String[] newStageIds = StringUtil.split(newIds, ";");
                String newId2 = addStageProps.getProperty("newkeyid2");
                String[] newStageId2 = StringUtil.split(newId2, ";");
                String newId3 = addStageProps.getProperty("newkeyid3");
                String[] newStageId3 = StringUtil.split(newId3, ";");
                if (newIds.trim().length() == 0 || newStageIds.length != totalInstCnt) {
                    throw new SapphireException(tp.translate("Required no. of stages could not be created."));
                }
                properties.setProperty("newkeyid1", newIds);
                if (stLinkKey2.length() > 0 && (newId2.trim().length() == 0 || newStageId2.length != totalInstCnt)) {
                    throw new SapphireException(tp.translate("Required no. of stages could not be created."));
                }
                if (stLinkKey3.length() > 0 && (newId3.trim().length() == 0 || newStageId3.length != totalInstCnt)) {
                    throw new SapphireException(tp.translate("Required no. of stages could not be created."));
                }
                properties.setProperty("newkeyid2", newId2);
                properties.setProperty("newkeyid3", newId3);
                int stgIndx = 0;
                for (int b = 0; b < dsBatchStage.getRowCount(); ++b) {
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("processstageid", dsBatchStage.getString(b, "processstageid"));
                    filterMap.put("stagecounter", dsBatchStage.getString(b, "stagecounter"));
                    filterMap.put("keyids", dsBatchStage.getString(b, "keyids"));
                    int bstRepeatCnt = dsBatchStage.getInt(b, "repeatcount");
                    DataSet filteredDS = dsActionProps.getFilteredDataSet(filterMap);
                    if (filteredDS != null && filteredDS.getRowCount() > 0) {
                        StringBuffer stageIds = new StringBuffer();
                        StringBuffer stageId2s = new StringBuffer();
                        StringBuffer stageId3s = new StringBuffer();
                        for (int d = 0; d < filteredDS.getRowCount(); ++d) {
                            if (d == 0) {
                                for (int repeatCnt = filteredDS.getInt(d, "repeatcount"); repeatCnt > 0; --repeatCnt) {
                                    stageIds.append(";").append(newStageIds[stgIndx]);
                                    if (stLinkKey2.length() > 0) {
                                        stageId2s.append(";").append(newStageId2[stgIndx]);
                                    }
                                    if (stLinkKey3.length() > 0) {
                                        stageId3s.append(";").append(newStageId3[stgIndx]);
                                    }
                                    ++stgIndx;
                                }
                            }
                            filteredDS.setString(d, "newstageids", stageIds.substring(1));
                            if (stLinkKey2.length() > 0) {
                                filteredDS.setString(d, "newstageids2", stageId2s.substring(1));
                            }
                            if (stLinkKey3.length() <= 0) continue;
                            filteredDS.setString(d, "newstageids3", stageId3s.substring(1));
                        }
                        continue;
                    }
                    for (int rcnt = 0; rcnt < bstRepeatCnt; ++rcnt) {
                        ++stgIndx;
                    }
                }
            }
            PropertyList actionProps = new PropertyList();
            DataSet dsProductSpecs = new DataSet();
            if (dsActionProps.getRowCount() > 0) {
                if (basedOnSDC.length() > 0) {
                    this.getBaseSDCDescColumnValue(dsActionProps, basedOnSDC, sdcProcessor, tp, sdcid, tableid, keycolid1, keycolid2, keycolid3, dsProductSpecs);
                }
                dsMultiActionProps.addColumn("primarykeyid1", 0);
                dsMultiActionProps.addColumn("primarykeyid2", 0);
                dsMultiActionProps.addColumn("primarykeyid3", 0);
                for (int i3 = 0; i3 < dsActionProps.getRowCount(); ++i3) {
                    int userSequence = (Integer)mapUseq.get(dsActionProps.getString(i3, "keyids"));
                    StringBuffer strUseq = new StringBuffer();
                    StringBuffer strDesc = new StringBuffer();
                    String baseSDCDesc = dsActionProps.getString(i3, "basesdcdesc", "");
                    String templatekeyid1 = dsActionProps.getString(i3, "templatekeyid1", "");
                    String templatekeyid2 = dsActionProps.getString(i3, "templatekeyid2", "");
                    String templatekeyid3 = dsActionProps.getString(i3, "templatekeyid3", "");
                    String processStageId = dsActionProps.getString(i3, "processstageid", "");
                    String securityDept = dsActionProps.getString(i3, "securitydepartment", "");
                    actionProps.clear();
                    int copies = dsActionProps.getInt(i3, "copies", 0);
                    if (copies <= 0) continue;
                    StringBuffer batchstageid = new StringBuffer();
                    StringBuffer batchstageid2 = new StringBuffer();
                    StringBuffer batchstageid3 = new StringBuffer();
                    if (processStageId.length() > 0) {
                        int repeat = dsActionProps.getInt(i3, "repeatcount", 1);
                        if (repeat <= 0) continue;
                        String newStageIds = dsActionProps.getString(i3, "newstageids", "");
                        String[] arrStageIds = StringUtil.split(newStageIds, ";");
                        String newStageId2s = dsActionProps.getString(i3, "newstageids2", "");
                        String[] arrStageIds2 = StringUtil.split(newStageId2s, ";");
                        String newStageId3s = dsActionProps.getString(i3, "newstageids3", "");
                        String[] arrStageIds3 = StringUtil.split(newStageId3s, ";");
                        for (int stg = 0; stg < arrStageIds.length; ++stg) {
                            for (int c = 0; c < copies; ++c) {
                                batchstageid.append(";").append(arrStageIds[stg]);
                                if (stLinkKey2.length() > 0) {
                                    batchstageid2.append(";").append(arrStageIds2[stg]);
                                }
                                if (stLinkKey3.length() <= 0) continue;
                                batchstageid3.append(";").append(arrStageIds3[stg]);
                            }
                        }
                        dsActionProps.setNumber(i3, "copies", copies *= repeat);
                    }
                    for (int sdiCnt = copies; sdiCnt > 0; --sdiCnt) {
                        for (int ctr = 0; ctr < columnNames.size(); ++ctr) {
                            String sdiColumnId = (String)columnNames.get(ctr);
                            if (!dsActionProps.isValidColumn(sdiColumnId)) continue;
                            String columnValue = dsActionProps.getValue(i3, sdiColumnId, "");
                            String propertyValue = actionProps.getProperty(sdiColumnId);
                            String string = propertyValue = propertyValue.length() > 0 ? propertyValue + ";" + columnValue : columnValue;
                            if (propertyValue.length() <= 0) continue;
                            actionProps.setProperty(sdiColumnId, propertyValue);
                        }
                        strUseq.append(";").append(++userSequence);
                        strDesc.append(";").append(baseSDCDesc);
                    }
                    if (basedOnSDC.length() > 0) {
                        // empty if block
                    }
                    actionProps.setProperty("sdcid", this.SDIToCreate);
                    actionProps.setProperty("templatekeyid1", templatekeyid1);
                    if (createSDIKeyCol2.length() > 0) {
                        actionProps.setProperty("templatekeyid2", templatekeyid2);
                    }
                    if (createSDIKeyCol3.length() > 0) {
                        actionProps.setProperty("templatekeyid3", templatekeyid3);
                    }
                    actionProps.setProperty("copies", "" + copies);
                    actionProps.setProperty("usersequence", strUseq.substring(1));
                    if (batchstageid.length() > 0) {
                        actionProps.setProperty(stLinkKey1, batchstageid.substring(1));
                    }
                    if (batchstageid2.length() > 0) {
                        actionProps.setProperty(stLinkKey2, batchstageid2.substring(1));
                    }
                    if (batchstageid3.length() > 0) {
                        actionProps.setProperty(stLinkKey3, batchstageid3.substring(1));
                    }
                    actionProps.setProperty("applyworkitems", "Y");
                    if (securityDept.length() > 0) {
                        actionProps.setProperty("securitydepartment", securityDept);
                    }
                    ap.processAction("AddSDI", "1", actionProps);
                    String[] returnKeyId1 = StringUtil.split(actionProps.getProperty("newkeyid1", ""), ";");
                    String[] returnKeyId2 = StringUtil.split(actionProps.getProperty("newkeyid2", ""), ";");
                    String[] returnKeyId3 = StringUtil.split(actionProps.getProperty("newkeyid3", ""), ";");
                    strUseq.setLength(0);
                    mapUseq.put(dsActionProps.getString(i3, "keyids"), new Integer(userSequence));
                    if (copies == returnKeyId1.length) {
                        returnKeys = returnKeys + ";" + actionProps.getProperty("newkeyid1", "");
                        int idx = 0;
                        String detailNo = dsActionProps.getString(i3, "s_samplingplandetailno", "");
                        String planId = dsActionProps.getString(i3, "samplingplanid", "");
                        String planVersionId = dsActionProps.getString(i3, "samplingplanversionid", "");
                        HashMap<String, BigDecimal> filterMap = new HashMap<String, BigDecimal>();
                        filterMap.put("s_samplingplandetailno", new BigDecimal(detailNo));
                        DataSet dsSPItem = (DataSet)spItemMap.get(planId + ";" + planVersionId);
                        DataSet dsSPItems = dsSPItem.getFilteredDataSet(filterMap);
                        if (dsSPItems.getRowCount() <= 0) continue;
                        for (int sdiCount = dsActionProps.getInt(i3, "copies", 0); sdiCount > 0; --sdiCount) {
                            for (int k = 0; k < dsSPItems.getRowCount(); ++k) {
                                int row = dsMultiActionProps.addRow();
                                dsMultiActionProps.setString(row, "itemsdcid", dsSPItems.getString(k, "itemsdcid", ""));
                                dsMultiActionProps.setString(row, "itemkeyid1", dsSPItems.getString(k, "itemkeyid1", ""));
                                dsMultiActionProps.setString(row, "itemkeyid2", dsSPItems.getString(k, "itemkeyid2", ""));
                                dsMultiActionProps.setString(row, "itemkeyid3", dsSPItems.getString(k, "itemkeyid3", ""));
                                dsMultiActionProps.setNumber(row, "spitemusersequence", dsSPItems.getValue(k, "spitemusersequence", "0"));
                                dsMultiActionProps.setString(row, "primarykeyid1", returnKeyId1[idx]);
                                if (createSDIKeyCol2.length() > 0) {
                                    dsMultiActionProps.setString(row, "primarykeyid2", returnKeyId2[idx]);
                                }
                                if (createSDIKeyCol2.length() > 0) {
                                    dsMultiActionProps.setString(row, "primarykeyid3", returnKeyId3[idx]);
                                }
                                dsMultiActionProps.setString(row, "sdcid", sdcid);
                                dsMultiActionProps.setString(row, "keyid1", dsActionProps.getString(i3, "keyid1", ""));
                                dsMultiActionProps.setString(row, "keyid2", dsActionProps.getString(i3, "keyid2", ""));
                                dsMultiActionProps.setString(row, "keyid3", dsActionProps.getString(i3, "keyid3", ""));
                            }
                            ++idx;
                        }
                        continue;
                    }
                    this.logger.info(tp.translate("Required no. of SDI(s) could not be created") + " ");
                }
            }
            PropertyList policy = this.getConfigurationProcessor().getPolicy("SamplingPlanPolicy", "Sapphire Custom");
            ArrayList<String> itemProps = new ArrayList<String>();
            itemProps.add("primarykeyid1");
            itemProps.add("primarykeyid2");
            itemProps.add("primarykeyid3");
            itemProps.add("itemsdcid");
            itemProps.add("itemkeyid1");
            itemProps.add("itemkeyid2");
            itemProps.add("itemkeyid3");
            if (policy != null) {
                PropertyListCollection sdcCollections = policy.getCollectionNotNull("templatesdc");
                for (int i4 = 0; i4 < sdcCollections.size(); ++i4) {
                    PropertyList sdcPL = sdcCollections.getPropertyList(i4);
                    String itemSdcId = sdcPL.getProperty("sdcid").trim();
                    String actionId = sdcPL.getProperty("actionid").trim();
                    if (actionId.length() == 0) continue;
                    String actionVersionId = sdcPL.getProperty("actionversionid", "1").trim();
                    PropertyListCollection actionPropsCollection = sdcPL.getCollectionNotNull("actioninputproperties");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    DataSet dsItem = new DataSet();
                    if (itemSdcId.length() > 0) {
                        filterMap.put("itemsdcid", itemSdcId);
                        dsItem = dsMultiActionProps.getFilteredDataSet(filterMap);
                    }
                    for (int r = 0; r < dsIndivSDIProps.getRowCount(); ++r) {
                        DataSet dsFilter = new DataSet();
                        HashMap<String, String> filter = new HashMap<String, String>();
                        if (dsItem.getRowCount() > 0) {
                            filter.put("keyid1", dsIndivSDIProps.getString(r, "keyid1", ""));
                            filter.put("keyid2", dsIndivSDIProps.getString(r, "keyid2", ""));
                            filter.put("keyid3", dsIndivSDIProps.getString(r, "keyid3", ""));
                            dsFilter = dsItem.getFilteredDataSet(filter);
                        }
                        DataSet prodSpecs = dsProductSpecs.getFilteredDataSet(filter);
                        if (dsFilter.getRowCount() > 0 || actionId.equalsIgnoreCase("AddSDISpec") && prodSpecs.getRowCount() > 0) {
                            if (actionId.equalsIgnoreCase("AddSDIWorkItem")) {
                                actionProps.clear();
                                if (!"Y".equalsIgnoreCase(actionProps.getProperty("propsmatch"))) {
                                    actionProps.setProperty("propsmatch", "Y");
                                }
                                dsFilter.sort("spitemusersequence");
                                actionProps.setProperty("propsmatchtestmethodorder", dsFilter.getColumnValues("spitemusersequence", ";"));
                                this.setActionProperties(actionPropsCollection, actionProps, itemProps, dsFilter, dsIndivSDIProps, r);
                                ap.processAction(actionId, actionVersionId, actionProps);
                                continue;
                            }
                            dsFilter.sort("spitemusersequence,itemkeyid1,itemkeyid2,itemkeyid3");
                            ArrayList<DataSet> groups = dsFilter.getGroupedDataSets("spitemusersequence,itemkeyid1,itemkeyid2,itemkeyid3");
                            for (int grp = 0; grp < groups.size(); ++grp) {
                                DataSet dsGrp = groups.get(grp);
                                actionProps.clear();
                                this.setActionProperties(actionPropsCollection, actionProps, itemProps, dsGrp, dsIndivSDIProps, r);
                                if (actionProps.size() <= 0) continue;
                                if (actionId.equals("AddSDISpec")) {
                                    String actionSpecs = actionProps.getProperty("specid");
                                    String actionSpecVersions = actionProps.getProperty("specversionid");
                                    if (prodSpecs.getRowCount() > 0) {
                                        DataSet planSpecs = new DataSet();
                                        planSpecs.addColumnValues("specid", 0, actionSpecs, ";");
                                        planSpecs.addColumnValues("specversionid", 0, actionSpecVersions, ";");
                                        actionSpecs = "";
                                        actionSpecVersions = "";
                                        for (int p = 0; p < planSpecs.getRowCount(); ++p) {
                                            HashMap<String, String> findSpec = new HashMap<String, String>();
                                            findSpec.put("specid", planSpecs.getValue(p, "specid"));
                                            findSpec.put("specversionid", planSpecs.getValue(p, "specversionid"));
                                            if (prodSpecs.findRow(findSpec) >= 0) continue;
                                            actionSpecs = actionSpecs + ";" + planSpecs.getValue(p, "specid");
                                            actionSpecVersions = actionSpecVersions + ";" + planSpecs.getValue(p, "specversionid");
                                        }
                                    }
                                    if (actionSpecs.length() <= 0) continue;
                                    String rsetid = BaseSDIDataAction.createBypassSecurityRSet(actionProps.getProperty("sdcid"), actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2"), actionProps.getProperty("keyid3"), this.database, this.connectionInfo, false);
                                    if (rsetid != null && rsetid.length() > 0) {
                                        actionProps.setProperty("rsetid", rsetid);
                                    }
                                    if (actionSpecs.startsWith(";")) {
                                        actionSpecs = actionSpecs.substring(1);
                                    }
                                    if (actionSpecVersions.startsWith(";")) {
                                        actionSpecVersions = actionSpecVersions.substring(1);
                                    }
                                    actionProps.setProperty("specid", actionSpecs);
                                    actionProps.setProperty("specversionid", actionSpecVersions);
                                    ap.processAction(actionId, actionVersionId, actionProps);
                                    if (rsetid.length() <= 0) continue;
                                    this.getDAMProcessor().clearRSet(rsetid);
                                    continue;
                                }
                                ap.processAction(actionId, actionVersionId, actionProps);
                            }
                            if (prodSpecs.getRowCount() <= 0) continue;
                            DataSet batchSamples = new DataSet();
                            batchSamples = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM " + createSDITableId + " WHERE " + linkKey1 + " = ? and sourcespid is not null", (Object[])new String[]{prodSpecs.getValue(0, "keyid1")});
                            if (batchSamples.getRowCount() <= 0) continue;
                            actionProps.clear();
                            actionProps.setProperty("sdcid", this.SDIToCreate);
                            actionProps.setProperty("keyid1", batchSamples.getColumnValues("s_sampleid", ";"));
                            actionProps.setProperty("specid", prodSpecs.getColumnValues("specid", ";"));
                            actionProps.setProperty("specversionid", prodSpecs.getColumnValues("specversionid", ";"));
                            actionProps.setProperty("applyspec", prodSpecs.getColumnValues("autoapplyflag", ";"));
                            String rsetid = BaseSDIDataAction.createBypassSecurityRSet(actionProps.getProperty("sdcid"), actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2"), actionProps.getProperty("keyid3"), this.database, this.connectionInfo, false);
                            if (rsetid != null && rsetid.length() > 0) {
                                actionProps.setProperty("rsetid", rsetid);
                            }
                            ap.processAction("AddSDISpec", "1", actionProps);
                            if (rsetid.length() <= 0) continue;
                            this.getDAMProcessor().clearRSet(rsetid);
                            continue;
                        }
                        if (!redoStageMode && itemSdcId.length() == 0) {
                            actionProps.clear();
                            this.setActionProperties(actionPropsCollection, actionProps, itemProps, dsFilter, dsIndivSDIProps, r);
                            if (actionProps.size() <= 0) continue;
                            ap.processAction(actionId, actionVersionId, actionProps);
                            continue;
                        }
                        tokenMap.put("itemSdcId", itemSdcId);
                        this.logger.warn(tp.translate("No data found for the Item SDC Id: [itemSdcId]", tokenMap));
                    }
                }
            }
            if (returnKeys.length() > 0) {
                properties.setProperty("newsampleid", returnKeys.substring(1));
            }
            this.logger.info("Time elapsed after multi action call: " + (System.currentTimeMillis() - startTime));
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("prodvariantdetail");
            this.database.closeStatement("prodvariantlevels");
            this.database.closeStatement("spdetails");
            this.database.closeStatement("spitems");
            this.database.closeStatement("getPCStages");
        }
        properties.setProperty("return_message", return_message);
    }

    private void getBaseSDCDescColumnValue(DataSet dsActionProps, String baseSDC, SDCProcessor sdcProcessor, TranslationProcessor tp, String sdcid, String tableid, String keycolid1, String keycolid2, String keycolid3, DataSet dsProductSpecs) throws SapphireException {
        String basetableid = sdcProcessor.getProperty(baseSDC, "tableid");
        String basekeycolid1 = sdcProcessor.getProperty(baseSDC, "keycolid1");
        String basekeycolid2 = sdcProcessor.getProperty(baseSDC, "keycolid2");
        String basekeycolid3 = sdcProcessor.getProperty(baseSDC, "keycolid3");
        String descCol = sdcProcessor.getProperty(baseSDC, "desccol");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("linksdcid", baseSDC);
        DataSet linkBaseSDCData = sdcProcessor.getLinksData(sdcid).getFilteredDataSet(filter);
        String linkBaseSDCKey1 = "";
        String linkBaseSDCKey2 = "";
        String linkBaseSDCKey3 = "";
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("baseSDC", baseSDC);
        tokenMap.put("sdcid", sdcid);
        if (linkBaseSDCData.getRowCount() > 0) {
            linkBaseSDCKey1 = linkBaseSDCData.getValue(0, "sdccolumnid", "");
            if (linkBaseSDCKey1.length() == 0) {
                tokenMap.put("basekeycolid1", basekeycolid1);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [sdcid] has no link for primary key column [basekeycolid1] of SDC [baseSDC]", tokenMap));
            }
            linkBaseSDCKey2 = linkBaseSDCData.getValue(0, "sdccolumnid2", "");
            if (basekeycolid2.length() > 0 && linkBaseSDCKey2.length() == 0) {
                tokenMap.put("basekeycolid2", basekeycolid2);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [sdcid] has no link for primary key column [basekeycolid2] of SDC [baseSDC]", tokenMap));
            }
            linkBaseSDCKey3 = linkBaseSDCData.getValue(0, "sdccolumnid3", "");
            if (basekeycolid3.length() > 0 && linkBaseSDCKey3.length() == 0) {
                tokenMap.put("basekeycolid3", basekeycolid3);
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed. SDC [sdcid] has no link for primary key column [basekeycolid3] of SDC [baseSDC]", tokenMap));
            }
        } else {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Cannot proceed, SDC [sdcid] is not linked to the SDC [baseSDC]", tokenMap));
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT b.* ").append(" FROM " + basetableid).append(" b, ").append(tableid).append(" s ").append(" WHERE   s.").append(linkBaseSDCKey1).append(" = b.").append(basekeycolid1);
        if (basekeycolid2.length() > 0) {
            sql.append(" AND s.").append(linkBaseSDCKey2).append(" =  b.").append(basekeycolid2);
        }
        if (basekeycolid3.length() > 0) {
            sql.append(" AND s.").append(linkBaseSDCKey3).append(" =  b.").append(basekeycolid3);
        }
        sql.append(" AND s.").append(keycolid1).append(" = ?");
        if (keycolid2.length() > 0) {
            sql.append(" AND s.").append(keycolid2).append(" = ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND s.").append(keycolid3).append(" = ?");
        }
        PreparedStatement psmtDesc = this.database.prepareStatement("getProdDesc", sql.toString());
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        boolean applyProductSpecs = "Y".equalsIgnoreCase(policy.getProperty("addproductspecstosamplingplansamples"));
        ArrayList<DataSet> grps = dsActionProps.getGroupedDataSets("keyids");
        try {
            for (int i = 0; i < grps.size(); ++i) {
                DataSet ds = grps.get(i);
                String key1 = ds.getString(0, "keyid1");
                String key2 = ds.getString(0, "keyid2", "");
                String key3 = ds.getString(0, "keyid3", "");
                psmtDesc.setString(1, key1);
                if (keycolid2.length() > 0) {
                    psmtDesc.setString(2, key2);
                }
                if (keycolid3.length() > 0) {
                    psmtDesc.setString(3, key3);
                }
                DataSet dsDesc = new DataSet(psmtDesc.executeQuery());
                String desc = dsDesc.getString(0, descCol, "");
                String baseKeyid1 = dsDesc.getValue(0, basekeycolid1);
                ds.setString(-1, "basesdcdesc", desc);
                if (!applyProductSpecs || !"Product".equals(baseSDC) || !"Batch".equals(sdcid)) continue;
                ArrayList<String> args = new ArrayList<String>();
                StringBuffer sdispecSql = new StringBuffer("select * from sdispec where sdcid = ? and keyid1 = ?");
                args.add(baseSDC);
                args.add(baseKeyid1);
                if (basekeycolid2.length() > 0) {
                    sdispecSql.append(" and keyid2 = ?");
                    args.add(dsDesc.getValue(0, basekeycolid2));
                }
                if (basekeycolid3.length() > 0) {
                    sdispecSql.append(" and keyid3 = ?");
                    args.add(dsDesc.getValue(0, basekeycolid3));
                }
                this.database.createPreparedResultSet("productspecs", sdispecSql.toString(), args.toArray());
                DataSet dsSDISpecs = new DataSet(this.database.getResultSet("productspecs"));
                dsSDISpecs.setString(-1, "sdcid", sdcid);
                dsSDISpecs.setString(-1, "keyid1", key1);
                dsSDISpecs.setString(-1, "keyid2", key2);
                dsSDISpecs.setString(-1, "keyid3", key3);
                dsProductSpecs.copyRow(dsSDISpecs, -1, 1);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private String getDescColForSDI(String sdiToCreate) {
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        return sdcProcessor.getProperty(sdiToCreate, "desccol");
    }

    private void setActionProperties(PropertyListCollection actionPropsCollection, PropertyList actionProps, ArrayList itemProps, DataSet dsItem, DataSet dsOtherProps, int currentRow) {
        for (int j = 0; j < actionPropsCollection.size(); ++j) {
            PropertyList propertyLst = actionPropsCollection.getPropertyList(j);
            String propertyId = propertyLst.getProperty("propertyid").trim();
            String propertyValue = propertyLst.getProperty("propertyvalue").trim();
            if (propertyId.length() <= 0 || propertyValue.length() <= 0) continue;
            int index = propertyValue.indexOf(91);
            while (index > -1) {
                String oldValue = propertyValue.substring(index, propertyValue.indexOf(93) + 1);
                String toSubstitute = propertyValue.substring(index + 1, propertyValue.indexOf(93)).trim();
                String substitutedValue = "";
                if (toSubstitute.equalsIgnoreCase("primarysdcid")) {
                    substitutedValue = this.SDIToCreate;
                } else if (itemProps.contains(toSubstitute) && dsItem.getRowCount() > 0) {
                    substitutedValue = actionProps.getProperty("propsmatch", "N").equals("N") && toSubstitute.startsWith("itemkeyid") ? dsItem.getString(0, toSubstitute) : dsItem.getColumnValues(toSubstitute, ";");
                } else if (dsOtherProps.getRowCount() > 0) {
                    substitutedValue = dsOtherProps.getString(currentRow, toSubstitute, "");
                }
                if (substitutedValue.equals("")) {
                    this.logger.warn("INVALID_PROPERTY", "No substitution found for the Property Value \"" + oldValue + "\".");
                }
                propertyValue = propertyValue.replace(oldValue, substitutedValue);
                index = propertyValue.indexOf(91);
            }
            actionProps.setProperty(propertyId, propertyValue);
        }
    }

    private void applySamplingPlanToSamples(PropertyList properties) throws SapphireException {
        String samplingPlanLevel = properties.getProperty("samplingplanlevel");
        String spId = properties.getProperty("samplingplanid");
        String spVersionId = properties.getProperty("samplingplanversionid");
        TranslationProcessor tp = this.getTranslationProcessor();
        String returnKeys = "";
        if (samplingPlanLevel.length() > 0 && (spId.length() == 0 || spVersionId.length() == 0)) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("SamplingPlan Id/Version Id missing for the Sampling Plan level."));
        }
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        DataSet dsProps = new DataSet();
        dsProps.addColumnValues("planid", 0, spId, delimeter);
        dsProps.addColumnValues("planversionid", 0, spVersionId, delimeter);
        dsProps.addColumnValues("level", 0, samplingPlanLevel, delimeter);
        dsProps.padColumns();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyListCollection columns = sdcProcessor.getColumns("Sample");
        ArrayList nonUpdatableSampleCols = this.getNonUpdatableCols();
        ArrayList<String> propColIds = new ArrayList<String>();
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (!properties.containsKey(colid) || nonUpdatableSampleCols.contains(colid)) continue;
            dsProps.addColumnValues(colid, 0, properties.getProperty(colid), delimeter);
            propColIds.add(colid);
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct spd.sourcelabel, spd.countrule, spd.countruletype, spd.templatesdcid, spd.templatekeyid1, spd.templatekeyid2, spd.templatekeyid2, spd.s_samplingplandetailno, spd.usersequence, spd.defaultdepartmentid").append(" FROM s_spdetail spd WHERE spd.s_samplingplanid = ? AND spd.s_samplingplanversionid = ? AND spd.levelid = ? order by spd.usersequence");
        PreparedStatement getSPDetails = this.database.prepareStatement("getspdetail", sql.toString());
        sql.setLength(0);
        sql.append("SELECT distinct spi.itemsdcid, spi.itemkeyid1, spi.itemkeyid2, spi.itemkeyid3, spi.usersequence spitemusersequence, spi.s_samplingplanitemno ").append(" FROM s_spitem spi, s_spdetailitem spdi ").append(" WHERE spdi.s_samplingplanid = spi.s_samplingplanid AND spdi.s_samplingplanversionid = spi.s_samplingplanversionid ").append(" AND spdi.s_samplingplandetailno = ? AND spdi.s_samplingplanitemno = spi.s_samplingplanitemno ").append(" AND spi.s_samplingplanid = ? AND spi.s_samplingplanversionid = ?  order by spitemusersequence, spi.s_samplingplanitemno");
        PreparedStatement getItemData = this.database.prepareStatement("getitemdata", sql.toString());
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        ActionProcessor ap = this.getActionProcessor();
        for (int i = 0; i < dsProps.getRowCount(); ++i) {
            String planId = dsProps.getString(i, "planid");
            String planVerId = dsProps.getString(i, "planversionid");
            String level = dsProps.getString(i, "level");
            try {
                getSPDetails.setString(1, planId);
                getSPDetails.setString(2, planVerId);
                getSPDetails.setString(3, level);
                DataSet dsLevelDetail = new DataSet(getSPDetails.executeQuery());
                for (int d = 0; d < dsLevelDetail.getRowCount(); ++d) {
                    String ruleType = dsLevelDetail.getString(d, "countruletype", "");
                    tokenMap.put("ruletype", ruleType);
                    tokenMap.put("samplingplan", planId);
                    tokenMap.put("samplingplanversion", planVerId);
                    tokenMap.put("level", level);
                    if ("Number".equalsIgnoreCase(ruleType)) {
                        String countRule = dsLevelDetail.getValue(d, "countrule", "");
                        String sourceLabel = dsLevelDetail.getValue(d, "sourcelabel", "");
                        String templateSDC = dsLevelDetail.getValue(d, "templatesdcid", "");
                        String templateKeyId1 = dsLevelDetail.getValue(d, "templatekeyid1", "");
                        String detailNo = dsLevelDetail.getValue(d, "s_samplingplandetailno", "");
                        String department = dsLevelDetail.getValue(d, "defaultdepartmentid", "");
                        if (countRule.length() > 0) {
                            int sdiCnt;
                            PropertyList addsDIProps = new PropertyList();
                            addsDIProps.setProperty("sdcid", templateSDC);
                            addsDIProps.setProperty("templateid", templateKeyId1);
                            addsDIProps.setProperty("copies", countRule);
                            if (department.length() > 0) {
                                addsDIProps.setProperty("securitydepartment", department);
                            }
                            if ((sdiCnt = Integer.parseInt(countRule)) == 0) continue;
                            StringBuffer planIds = new StringBuffer();
                            StringBuffer planVerIds = new StringBuffer();
                            StringBuffer levels = new StringBuffer();
                            StringBuffer labels = new StringBuffer();
                            while (sdiCnt-- > 0) {
                                planIds.append(";").append(planId);
                                planVerIds.append(";").append(planVerId);
                                levels.append(";").append(level);
                                labels.append(";").append(sourceLabel);
                                for (int c = 0; c < propColIds.size(); ++c) {
                                    String col = (String)propColIds.get(c);
                                    String colValue = dsProps.getString(i, col, "");
                                    String propValue = addsDIProps.getProperty(col);
                                    propValue = propValue + (propValue.length() > 0 ? ";" + colValue : colValue);
                                    addsDIProps.setProperty(col, propValue);
                                }
                            }
                            addsDIProps.setProperty("sourcespid", planIds.substring(1));
                            addsDIProps.setProperty("sourcespversionid", planVerIds.substring(1));
                            addsDIProps.setProperty("sourcesplevelid", levels.substring(1));
                            addsDIProps.setProperty("sourcespsourcelabel", labels.substring(1));
                            this.getActionProcessor().processAction("AddSDI", "1", addsDIProps);
                            String keyIds = addsDIProps.getProperty("newkeyid1");
                            if (keyIds.length() <= 0) continue;
                            getItemData.setString(1, detailNo);
                            getItemData.setString(2, planId);
                            getItemData.setString(3, planVerId);
                            DataSet dsItems = new DataSet(getItemData.executeQuery());
                            DataSet addSDIWorkItem = new DataSet();
                            DataSet addSDISpec = new DataSet();
                            for (int s = 0; s < dsItems.getRowCount(); ++s) {
                                int r;
                                String itemSDCId = dsItems.getString(s, "itemsdcid", "");
                                String itemKeyid1 = dsItems.getString(s, "itemkeyid1", "");
                                String itemKeyid2 = dsItems.getString(s, "itemkeyid2", "");
                                if (itemSDCId.equals("WorkItem")) {
                                    r = addSDIWorkItem.addRow();
                                    addSDIWorkItem.setString(r, "workitemid", itemKeyid1);
                                    addSDIWorkItem.setString(r, "workitemversionid", itemKeyid2);
                                    continue;
                                }
                                if (!itemSDCId.equals("SpecSDC")) continue;
                                r = addSDISpec.addRow();
                                addSDISpec.setString(r, "specid", itemKeyid1);
                                addSDISpec.setString(r, "specversionid", itemKeyid2);
                            }
                            if (addSDIWorkItem.getRowCount() > 0) {
                                PropertyList addSDIWorkItemProps = new PropertyList();
                                addSDIWorkItemProps.setProperty("sdcid", "Sample");
                                addSDIWorkItemProps.setProperty("keyid1", keyIds);
                                addSDIWorkItemProps.setProperty("workitemid", addSDIWorkItem.getColumnValues("workitemid", ";"));
                                addSDIWorkItemProps.setProperty("workitemversionid", addSDIWorkItem.getColumnValues("workitemversionid", ";"));
                                ap.processAction("AddSDIWorkItem", "1", addSDIWorkItemProps);
                            }
                            if (addSDISpec.getRowCount() > 0) {
                                String rsetid = BaseSDIDataAction.createBypassSecurityRSet("Sample", keyIds, "", "", this.database, this.connectionInfo, false);
                                PropertyList addSDISpecProps = new PropertyList();
                                addSDISpecProps.setProperty("sdcid", "Sample");
                                addSDISpecProps.setProperty("keyid1", keyIds);
                                addSDISpecProps.setProperty("specid", addSDISpec.getColumnValues("specid", ";"));
                                addSDISpecProps.setProperty("specversionid", addSDISpec.getColumnValues("specversionid", ";"));
                                addSDISpecProps.setProperty("rsetid", rsetid);
                                ap.processAction("AddSDISpec", "1", addSDISpecProps);
                                if (rsetid.length() > 0) {
                                    this.getDAMProcessor().clearRSet(rsetid);
                                }
                            }
                            returnKeys = returnKeys + ";" + keyIds;
                            continue;
                        }
                        String msg = "Count rule is not defined for SamplingPlan: [samplingplan] version:[samplingplanversion] Level: [level]";
                        if (sourceLabel.length() > 0) {
                            tokenMap.put("sourceLabel", sourceLabel);
                            msg = msg + " source label: [sourceLabel]";
                        }
                        Logger.logInfo(tp.translate(msg, tokenMap));
                        continue;
                    }
                    throw new SapphireException(tp.translate("This mode of action call can not evaluate the count rule of type [ruletype]", tokenMap));
                }
                continue;
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        if (returnKeys.length() > 0) {
            properties.setProperty("newsampleid", returnKeys.substring(1));
        }
        this.database.closeStatement("getspdetail");
        this.database.closeStatement("getitemdata");
    }

    private ArrayList getNonUpdatableCols() {
        ArrayList<String> nonUpdatableCols = new ArrayList<String>();
        nonUpdatableCols.add("moddt");
        nonUpdatableCols.add("modby");
        nonUpdatableCols.add("modtool");
        nonUpdatableCols.add("createdt");
        nonUpdatableCols.add("createby");
        nonUpdatableCols.add("createtool");
        nonUpdatableCols.add("auditsequence");
        nonUpdatableCols.add("templateflag");
        return nonUpdatableCols;
    }
}

