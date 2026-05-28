/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.sql.PreparedStatement;
import java.util.Calendar;
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

public class EvalProdVariantState
extends BaseAction
implements sapphire.action.EvalProdVariantState {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String prodVariantType;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No keyid1 specified");
        }
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String singleSDIName = sdcProcessor.getProperty(sdcid, "singular");
        singleSDIName = StringUtil.initCaps(singleSDIName);
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
        }
        if (keycolid3.length() > 0) {
            if (keyid3.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid3 specified"));
            }
            if (keyid1prop.length != keyid3prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid3 not matching with count of keyid1"));
            }
        }
        if ((prodVariantType = properties.getProperty("prodvarianttype")).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No ProdVariantType specified"));
        }
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        DataSet columnMap = SamplingPlanUtil.getSamplingPlanColumnMapFromPolicy(sdcid, prodVariantType, configProcessor);
        String sizeColId = SamplingPlanUtil.getSamplingPlanSizeColumnId(sdcid, configProcessor);
        if (columnMap.size() == 0) {
            tokenMap.put("sdcid", sdcid);
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Cannot proceed, column mapping between ProdVariant and SDC [sdcid] is not defined in the SamplingPlan policy.", tokenMap));
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT pr.s_prodvariantid, pr.prodvariantruleid, pr.currentstateid, pr.samplingplanid, pr.samplingplanversionid, pr.lasttransitiondt ");
        if (sizeColId != null && sizeColId.length() > 0) {
            sql.append(", sdc." + sizeColId);
        }
        for (int i = 0; i < columnMap.getRowCount(); ++i) {
            String colName = columnMap.getString(i, "sdccolumnid", "");
            if (colName.length() <= 0) continue;
            sql.append(", sdc." + colName);
        }
        sql.append(" FROM s_prodvariant pr, " + tableid + " sdc").append(" WHERE pr.prodvarianttype= ? ");
        sql.append(" AND pr.s_prodvariantid = sdc.prodvariantid ");
        sql.append(" AND sdc." + keycolid1).append("= ?");
        if (keycolid2.length() > 0) {
            sql.append(" AND sdc." + keycolid2).append("= ?");
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND sdc." + keycolid3).append("= ?");
        }
        PreparedStatement selectProdVars = this.database.prepareStatement("prodvariantdetail", sql.toString());
        sql.setLength(0);
        sql.append("SELECT s_transitionruleid, transitionruletype, transitionrule, nextstateid, autoflag FROM s_pvrtransitionrule ").append(" WHERE s_prodvariantruleid = ? AND s_stateid = ? ");
        PreparedStatement selectTransitionRules = this.database.prepareStatement("transitionrules", sql.toString());
        try {
            DataSet dsActionProps = new DataSet();
            dsActionProps.addColumn("s_prodvariantid", 0);
            dsActionProps.addColumn("currentstateid", 0);
            dsActionProps.addColumn("transitionstateid", 0);
            dsActionProps.addColumn("transitionapprovalflag", 0);
            dsActionProps.addColumn("lasttransitiondt", 2);
            selectProdVars.setString(1, prodVariantType);
            for (int i = 0; i < keyid1prop.length; ++i) {
                DataSet dsProdVars;
                String keyIds = keyid1prop[i];
                selectProdVars.setString(2, keyid1prop[i]);
                if (keycolid2.length() > 0) {
                    selectProdVars.setString(3, keyid2prop[i]);
                    keyIds = keyIds + "/" + keyid2prop[i];
                }
                if (keycolid3.length() > 0) {
                    selectProdVars.setString(4, keyid3prop[i]);
                    keyIds = keyIds + "/" + keyid3prop[i];
                }
                if ((dsProdVars = new DataSet(selectProdVars.executeQuery())).getRowCount() == 0) {
                    this.logger.info("EMPTY_RESULTSET", tp.translate("No matching ProdVariant found for the") + " " + singleSDIName + ": " + keyIds);
                    continue;
                }
                block7: for (int idx = 0; idx < dsProdVars.getRowCount(); ++idx) {
                    String prodVariantId = dsProdVars.getValue(idx, "s_prodvariantid", "");
                    String ruleId = dsProdVars.getValue(idx, "prodvariantruleid", "");
                    String stateId = dsProdVars.getValue(idx, "currentstateid", "");
                    Calendar lastTransitionDate = dsProdVars.getCalendar(idx, "lasttransitiondt");
                    selectTransitionRules.setString(1, ruleId);
                    selectTransitionRules.setString(2, stateId);
                    HashMap sdcColProdVarValMap = SamplingPlanUtil.createSDCColIdProdVariantValueMap(columnMap, dsProdVars);
                    DataSet dsTransitionRules = new DataSet(selectTransitionRules.executeQuery());
                    for (int rule = 0; rule < dsTransitionRules.getRowCount(); ++rule) {
                        String transitionId = dsTransitionRules.getString(rule, "s_transitionruleid", "");
                        String transitionRuleType = dsTransitionRules.getString(rule, "transitionruletype", "");
                        String transitionRule = dsTransitionRules.getString(rule, "transitionrule", "");
                        String nxtState = dsTransitionRules.getString(rule, "nextstateid", "");
                        String autoFlag = dsTransitionRules.getString(rule, "autoflag", "");
                        if (!SamplingPlanUtil.evaluateTransitionRule(transitionId, transitionRuleType, transitionRule, sdcid, tableid, configProcessor, this.getQueryProcessor(), tp, sdcColProdVarValMap, lastTransitionDate, this.connectionInfo.getDbms())) continue;
                        int row = dsActionProps.addRow();
                        dsActionProps.setString(row, "s_prodvariantid", prodVariantId);
                        if (autoFlag.equalsIgnoreCase("Y")) {
                            dsActionProps.setString(row, "currentstateid", nxtState);
                            dsActionProps.setDate(row, "lasttransitiondt", "n");
                            continue block7;
                        }
                        dsActionProps.setString(row, "currentstateid", stateId);
                        dsActionProps.setString(row, "transitionstateid", nxtState);
                        dsActionProps.setString(row, "transitionapprovalflag", "Y");
                        continue block7;
                    }
                }
            }
            ActionProcessor ap = this.getActionProcessor();
            if (dsActionProps.getRowCount() > 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_ProdVariant");
                actionProps.setProperty("keyid1", dsActionProps.getColumnValues("s_prodvariantid", ";"));
                actionProps.setProperty("currentstateid", dsActionProps.getColumnValues("currentstateid", ";"));
                actionProps.setProperty("transitionstateid", dsActionProps.getColumnValues("transitionstateid", ";"));
                actionProps.setProperty("transitionapprovalflag", dsActionProps.getColumnValues("transitionapprovalflag", ";"));
                actionProps.setProperty("lasttransitiondt", dsActionProps.getColumnValues("lasttransitiondt", ";"));
                ap.processAction("EditSDI", "1", actionProps);
            }
        }
        catch (Exception sq) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(sq, ErrorUtil.isUserAdmin(this.getConnectionId())), sq);
        }
        finally {
            this.database.closeStatement("prodvariantdetail");
            this.database.closeStatement("transitionrules");
        }
    }
}

