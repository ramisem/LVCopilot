/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SplitBatch
extends BaseAction
implements sapphire.action.SplitBatch {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53788 $";
    private static final String PROPERTY_BATCHMODE = "batchmode";
    private static final String BATCHMODE = "Split";
    private static final String BATCHLINKID = "batch genealogy";
    private static final String BATCHSTATUS_REJECTED = "Rejected";
    private static final String POLICY = "BatchSamplePolicy";
    private static final String POLICY_NODE = "Sapphire Custom";
    private static final String POLICY_MAXSPLITBATCH = "maxsplitbatch";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        try {
            boolean autoKeyGen;
            String keyGenerationRule = this.getSDCProcessor().getProperty("Batch", "keygenerationrule");
            boolean bl = autoKeyGen = StringUtil.getLen(keyGenerationRule) > 0L && keyGenerationRule.charAt(0) == 'A';
            if (!autoKeyGen) {
                throw new SapphireException("GENERAL_ERROR", "VALIDATION", tp.translate("Auto Key Generation rule not defined for Batch SDC."));
            }
            String sourceBatchId = properties.getProperty("batchid", "");
            if (sourceBatchId.length() == 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Source Batch ID cannot be blank."));
            }
            if (sourceBatchId.indexOf(";") > -1) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Only Single Batch ID is supported."));
            }
            int splitCount = 0;
            try {
                splitCount = Integer.parseInt(properties.getProperty("splitcount", "0"));
            }
            catch (NumberFormatException e) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Invalid Split Count."));
            }
            if (splitCount <= 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("No of Batches to create cannot be Zero or -ve."));
            }
            PropertyList policyProps = this.getConfigurationProcessor().getPolicy(POLICY, POLICY_NODE);
            String maxSplitBatchStr = policyProps.getProperty(POLICY_MAXSPLITBATCH, "-1");
            int maxSplitBatch = 0;
            try {
                maxSplitBatch = Integer.parseInt(maxSplitBatchStr);
            }
            catch (NumberFormatException e) {
                throw new SapphireException("GENERAL_ERROR", "VALIDATION", tp.translate("Max Split Batch property not properly defined in BatchSamplePolicy"));
            }
            if (maxSplitBatch > -1 && splitCount > maxSplitBatch) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Number of splits cannot be more than that defined in BatchSamplePolicy"));
            }
            String samplingPlanId = properties.getProperty("samplingplanid", "");
            String samplingPlanVersionId = properties.getProperty("samplingplanversionid", "");
            String levelId = properties.getProperty("samplingplanlevelid", "").trim();
            String batchSize = properties.getProperty("batchsize", "0");
            this.logger.info("Retrieving info. of '" + sourceBatchId + "'");
            SafeSQL safeSQL = new SafeSQL();
            DataSet batchDetails = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_batchid, productid, productversionid, batchtype, prodvariantid, batchstatus FROM s_Batch where s_batchid = " + safeSQL.addVar(sourceBatchId), safeSQL.getValues());
            if (batchDetails.getRowCount() == 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Could not retrieve Souce Batch Information."));
            }
            if (BATCHSTATUS_REJECTED.equalsIgnoreCase(batchDetails.getString(0, "batchstatus", ""))) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Rejected Batch cannot be split."));
            }
            String sourceProductId = batchDetails.getString(0, "productid", "");
            String sourceProductVersionId = batchDetails.getString(0, "productversionid", "");
            this.logger.info("Creating " + splitCount + " nos. of Batch SDIs with SamplingPlanId-" + samplingPlanId + ", SamplingPlanVersionId-" + samplingPlanVersionId + ", LevelId-" + levelId);
            PropertyList props = new PropertyList();
            props.putAll(properties);
            props.setProperty("sdcid", "Batch");
            props.setProperty("copies", String.valueOf(splitCount));
            if (levelId.length() == 0) {
                props.deleteProperty("samplingplanid");
                props.deleteProperty("samplingplanversionid");
                props.deleteProperty("samplingplanlevelid");
            }
            props.setProperty(PROPERTY_BATCHMODE, BATCHMODE);
            props.setProperty("batchsize", batchSize);
            props.setProperty("productid", sourceProductId);
            props.setProperty("productversionid", sourceProductVersionId);
            props.setProperty("batchtype", batchDetails.getString(0, "batchtype", ""));
            props.setProperty("prodvariantid", batchDetails.getString(0, "prodvariantid", ""));
            props.setProperty("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("active"));
            this.getActionProcessor().processAction("AddSDI", "1", props);
            String newBatchIds = props.getProperty("newkeyid1");
            if (newBatchIds == null || newBatchIds.length() == 0) {
                throw new SapphireException("GENERAL_ERROR", tp.translate("Unable to create new Batch"));
            }
            this.logger.info("Split Batches created: " + newBatchIds);
            properties.setProperty("newbatchid", newBatchIds);
            this.logger.info("Updating genealogy table with the newly created child batches. ");
            String parentBatchId = "";
            String parentProductId = "";
            String parentProductVersionId = "";
            String batchItemId = "";
            for (int i = 0; i < splitCount; ++i) {
                parentBatchId = parentBatchId + ";" + sourceBatchId;
                parentProductId = parentProductId + ";" + sourceProductId;
                parentProductVersionId = parentProductVersionId + ";" + sourceProductVersionId;
                batchItemId = batchItemId + ";";
            }
            props.clear();
            props.setProperty("sdcid", "Batch");
            props.setProperty("linkid", BATCHLINKID);
            props.setProperty("s_batchid", newBatchIds);
            props.setProperty("parentbatchid", parentBatchId.substring(1));
            props.setProperty("parentproductid", parentProductId.substring(1));
            props.setProperty("parentproductversionid", parentProductVersionId.substring(1));
            props.setProperty("separator", ";");
            props.setProperty("s_batchitemid", batchItemId.substring(1));
            this.getActionProcessor().processAction("AddSDIDetail", "1", props);
        }
        catch (SapphireException e) {
            throw new SapphireException(e);
        }
    }
}

