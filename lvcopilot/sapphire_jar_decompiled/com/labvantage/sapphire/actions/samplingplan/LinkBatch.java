/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.samplingplan;

import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LinkBatch
extends BaseAction
implements sapphire.action.LinkBatch {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53985 $";
    private static final String PROPERTY_BATCHMODE = "batchmode";
    private static final String BATCHMODE = "Link";
    private static final String BATCHLINKID = "batch genealogy";
    private static final String PRODUCTID = "productid";
    private static final String PRODUCTVERSIONID = "productversionid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            boolean autoKeyGen;
            TranslationProcessor tp = this.getTranslationProcessor();
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            String keyGenerationRule = sdcProcessor.getProperty("Batch", "keygenerationrule");
            boolean bl = autoKeyGen = StringUtil.getLen(keyGenerationRule) > 0L && keyGenerationRule.charAt(0) == 'A';
            if (!autoKeyGen) {
                throw new SapphireException("GENERAL_ERROR", "VALIDATION", tp.translate("Auto Key Generation rule not defined for Batch SDC."));
            }
            String batchids = properties.getProperty("batchid");
            if (batchids.indexOf(";") == -1) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Single Batch cannot be linked."));
            }
            SafeSQL safeSQL = new SafeSQL();
            DataSet batchDetails = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_batchid, batchstatus, productid, productversionid FROM s_Batch where s_batchid IN (" + safeSQL.addIn(batchids.replaceAll(";", "','")) + ")", safeSQL.getValues());
            HashMap<String, String> status = new HashMap<String, String>();
            status.put("batchstatus", "Rejected");
            if (batchDetails.getFilteredDataSet(status).getRowCount() > 0) {
                throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("Rejected Batch cannot be linked."));
            }
            String[] parentBatches = StringUtil.split(batchids, ";");
            String sql = "";
            safeSQL.reset();
            sql = "SELECT DISTINCT productid,productversionid FROM s_batch WHERE s_batchid IN (" + safeSQL.addIn(batchids.replaceAll(";", "','")) + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            PropertyList props = new PropertyList();
            if (ds.getRowCount() == 1 && ds.getString(0, PRODUCTID) != null) {
                props.setProperty(PRODUCTID, ds.getString(0, PRODUCTID));
                props.setProperty(PRODUCTVERSIONID, ds.getString(0, PRODUCTVERSIONID));
            }
            props.putAll(properties);
            props.setProperty("sdcid", "Batch");
            props.setProperty(PROPERTY_BATCHMODE, BATCHMODE);
            props.setProperty("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("active"));
            this.getActionProcessor().processAction("AddSDI", "1", props);
            String newbatchid = props.getProperty("newkeyid1");
            properties.setProperty("newbatchid", newbatchid);
            this.logger.info("Linked Batch created: " + newbatchid);
            this.logger.info("Updating genealogy table with the newly created child batches. ");
            String batchItemId = "";
            String newBatchIds = "";
            for (int i = 0; i < parentBatches.length; ++i) {
                batchItemId = batchItemId + ";";
                newBatchIds = newBatchIds + ";" + newbatchid;
            }
            props.clear();
            props.setProperty("sdcid", "Batch");
            props.setProperty("linkid", BATCHLINKID);
            props.setProperty("s_batchid", newBatchIds.substring(1));
            props.setProperty("parentbatchid", batchDetails.getColumnValues("s_batchid", ";"));
            props.setProperty("s_batchitemid", batchItemId.substring(1));
            props.setProperty("parentproductid", batchDetails.getColumnValues(PRODUCTID, ";"));
            props.setProperty("parentproductversionid", batchDetails.getColumnValues(PRODUCTVERSIONID, ";"));
            props.setProperty("separator", ";");
            this.getActionProcessor().processAction("AddSDIDetail", "1", props);
        }
        catch (SapphireException e) {
            throw new SapphireException(e);
        }
    }
}

