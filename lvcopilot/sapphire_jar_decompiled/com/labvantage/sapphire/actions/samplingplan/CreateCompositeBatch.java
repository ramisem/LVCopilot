/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateCompositeBatch
extends BaseAction
implements sapphire.action.CreateCompositeBatch {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53787 $";
    private static final String PROPERTY_BATCHMODE = "batchmode";
    private static final String BATCHMODE = "Composite";
    private static final String BATCHLINKID = "batch genealogy";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        try {
            boolean autoKeyGen;
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            String keyGenerationRule = sdcProcessor.getProperty("Batch", "keygenerationrule");
            boolean bl = autoKeyGen = StringUtil.getLen(keyGenerationRule) > 0L && keyGenerationRule.charAt(0) == 'A';
            if (!autoKeyGen) {
                throw new SapphireException("GENERAL_ERROR", "VALIDATION", tp.translate("Auto Key Generation rule not defined for Batch SDC."));
            }
            String sourchBatchIds = properties.getProperty("batchid", "");
            String productid = "";
            String productversionid = "";
            String samplingplanid = properties.getProperty("samplingplanid", "");
            String samplingplanversionid = properties.getProperty("samplingplanversionid", "");
            String levelid = properties.getProperty("samplingplanlevelid", "");
            String applysamplingplan = properties.getProperty("applysamplingplanflag", "");
            String prodvariantid = "";
            properties.setProperty("sdcid", "Batch");
            if (sourchBatchIds.length() == 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Source Batch Ids not supplied."));
            }
            if (sourchBatchIds.indexOf(";") < 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Source Batch Ids must be at least two."));
            }
            if (levelid.length() > 0 && (samplingplanid.length() == 0 || samplingplanversionid.length() == 0)) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("SamplingPlan must be provided with level id."));
            }
            String rejectedBatches = this.getRejectedBatches(sourchBatchIds);
            if (rejectedBatches.length() > 0) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("batchid", rejectedBatches);
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Source Batch Id(s)-[batchid] are Rejected Batch(s).Cannot proceed", valueMap));
            }
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer("SELECT distinct productid,productversionid,samplingplanid,samplingplanversionid,prodvariantid FROM s_batch");
            sql.append(" WHERE s_batchid in (").append(safeSQL.addIn(sourchBatchIds.replaceAll(";", "','"))).append(")");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            ArrayList<DataSet> prodSamplingPlanList = ds.getGroupedDataSets("productid,productversionid");
            if (prodSamplingPlanList.size() > 1) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("All the Batches must belong to the same Product."));
            }
            if (prodSamplingPlanList.size() == 1) {
                productid = ds.getString(0, "productid", "");
                productversionid = ds.getString(0, "productversionid", "");
                properties.setProperty("productid", productid);
                properties.setProperty("productversionid", productversionid);
                if ("Y".equalsIgnoreCase(applysamplingplan) && levelid.length() == 0) {
                    ArrayList<DataSet> samplingplanList = ds.getGroupedDataSets("samplingplanid,samplingplanversionid");
                    if (samplingplanList.size() > 1) {
                        throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("All the Batches do not have same SamplingPlan."));
                    }
                    if (samplingplanList.size() == 1) {
                        samplingplanid = ds.getString(0, "samplingplanid", "");
                        samplingplanversionid = ds.getString(0, "samplingplanversionid", "");
                        ArrayList<DataSet> prodVariantList = ds.getGroupedDataSets("prodvariantid");
                        if (prodVariantList.size() == 1) {
                            prodvariantid = ds.getString(0, "prodvariantid", "");
                        }
                        safeSQL.reset();
                        StringBuffer sqlLevel = new StringBuffer("select distinct levelid from s_spdetail");
                        sqlLevel.append(" WHERE s_samplingplanid = ").append(safeSQL.addVar(samplingplanid));
                        sqlLevel.append(" AND s_samplingplanversionid = ").append(safeSQL.addVar(samplingplanversionid));
                        DataSet levelDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        if (levelDS == null || levelDS.size() == 0) {
                            throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Level not found in the Common SamplingPlan of the source Batches.Cannot proceed."));
                        }
                        String levelids = levelDS.getColumnValues("levelid", ";");
                        if (!levelids.contains(BATCHMODE)) throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Composite level is missing in the Common SamplingPlan of the source Batches.Cannot proceed."));
                        properties.setProperty("samplingplanid", samplingplanid);
                        properties.setProperty("samplingplanversionid", samplingplanversionid);
                        properties.setProperty("samplingplanlevelid", BATCHMODE);
                        properties.setProperty("prodvariantid", prodvariantid);
                    }
                } else {
                    ArrayList<DataSet> prodvariantidList = ds.getGroupedDataSets("prodvariantid");
                    if (prodvariantidList.size() == 1) {
                        prodvariantid = ds.getString(0, "prodvariantid", "");
                    }
                    properties.setProperty("prodvariantid", prodvariantid);
                }
            }
            this.logger.info("Creating Composite Batch from " + sourchBatchIds.replaceAll(";", ","));
            properties.setProperty(PROPERTY_BATCHMODE, BATCHMODE);
            properties.setProperty("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("active"));
            this.getActionProcessor().processAction("AddSDI", "1", properties);
            String newBatchId = properties.getProperty("newkeyid1", "");
            if (newBatchId.length() == 0) {
                throw new SapphireException("GENERAL_ERROR", tp.translate("Unable to create new Composite Batch"));
            }
            this.logger.info("Composite Batch created: " + newBatchId);
            properties.setProperty("newbatchid", newBatchId);
            this.logger.info("Updating s_batchgenealogy table with the newly created Composite Batch. ");
            String batchItemId = "";
            String newBatchIds = "";
            int parentBatchCount = StringUtil.split(sourchBatchIds, ";").length;
            for (int i = 0; i < parentBatchCount; ++i) {
                newBatchIds = newBatchIds + ";" + newBatchId;
                batchItemId = batchItemId + ";";
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Batch");
            props.setProperty("linkid", BATCHLINKID);
            props.setProperty("s_batchid", newBatchIds.substring(1));
            props.setProperty("parentbatchid", sourchBatchIds);
            props.setProperty("s_batchitemid", batchItemId.substring(1));
            props.setProperty("parentproductid", productid);
            props.setProperty("parentproductversionid", productversionid);
            this.getActionProcessor().processAction("AddSDIDetail", "1", props);
            return;
        }
        catch (SapphireException e) {
            throw new SapphireException(e);
        }
    }

    private String getRejectedBatches(String batchids) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT * FROM s_batch");
        sql.append(" WHERE s_batchid in (").append(safeSQL.addIn(batchids.replaceAll(";", "','"))).append(")");
        sql.append(" AND batchstatus='Rejected'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String rejectedBatchids = "";
        if (ds != null && ds.size() > 0) {
            rejectedBatchids = ds.getColumnValues("s_batchid", ",");
        }
        return rejectedBatchids;
    }
}

