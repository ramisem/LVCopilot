/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.SamplingPlanHelper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSPWorkItem
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_WORKITEMID = "workitemid";
    public static final String PROPERTY_WORKITEMVERSIONID = "workitemversionid";
    public static final String PROPERTY_AUTOMATICVERSIONING = "automaticversioning";
    public static final String PROPERTY_SAMPLINGPLANIDCOLUMN = "samplingplanidcolumn";
    public static final String PROPERTY_SAMPLINGPLANVERSIONIDCOLUMN = "samplingplanversionidcolumn";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty(PROPERTY_SDCID);
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        String keyId2 = properties.getProperty(PROPERTY_KEYID2);
        String keyId3 = properties.getProperty(PROPERTY_KEYID3);
        String workItemIds = properties.getProperty(PROPERTY_WORKITEMID);
        String workItemVersionIds = properties.getProperty(PROPERTY_WORKITEMVERSIONID);
        AutomaticVersioning automaticVersioning = AutomaticVersioning.fromString(properties.getProperty(PROPERTY_AUTOMATICVERSIONING));
        String samplingPlanIdColumn = properties.getProperty(PROPERTY_SAMPLINGPLANIDCOLUMN);
        String samplingPlanVersionIdColumn = properties.getProperty(PROPERTY_SAMPLINGPLANVERSIONIDCOLUMN);
        if (sdcId.isEmpty() || sdcId.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property SDC ID: " + sdcId);
        }
        if (keyId1.isEmpty() || keyId1.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property Key ID1: " + keyId1);
        }
        if (workItemIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property work item ID");
        }
        if (workItemVersionIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property work item version ID");
        }
        if (samplingPlanIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property sampling plan ID column");
        }
        if (samplingPlanVersionIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property sampling plan version ID column");
        }
        SamplingPlanHelper samplingPlanHelper = new SamplingPlanHelper(this.getConnectionId());
        PropertyList samplingPlanProps = samplingPlanHelper.prepareSamplingPlanForAddOrEdit(sdcId, keyId1, keyId2, keyId3, automaticVersioning, samplingPlanIdColumn, samplingPlanVersionIdColumn);
        String samplingPlanId = samplingPlanProps.getProperty("samplingplanid");
        String samplingPlanVersionId = samplingPlanProps.getProperty("samplingplanversionid");
        String getMaxItemNoSql = "SELECT MAX(s_samplingplanitemno) maxitemno FROM s_spitem i WHERE i.s_samplingplanid = ? AND i.s_samplingplanversionid = ?";
        Object[] getMaxItemNoParams = new Object[]{samplingPlanId, samplingPlanVersionId};
        DataSet getMaxItemNoDs = this.getQueryProcessor().getPreparedSqlDataSet(getMaxItemNoSql, getMaxItemNoParams);
        int maxItemNo = getMaxItemNoDs.getBigDecimal(0, "maxitemno", BigDecimal.ZERO).intValue();
        StringBuilder itemNumbers = new StringBuilder();
        StringBuilder itemSdcId = new StringBuilder();
        List<String> workItemList = Arrays.asList(StringUtil.split(workItemIds, ";"));
        for (String ignored : workItemList) {
            itemSdcId.append(";").append("WorkItem");
            itemNumbers.append(";").append(++maxItemNo);
        }
        PropertyList addSamplingPlanDetailProps = new PropertyList();
        addSamplingPlanDetailProps.setProperty(PROPERTY_SDCID, "LV_SamplingPlan");
        addSamplingPlanDetailProps.setProperty(PROPERTY_KEYID1, samplingPlanId);
        addSamplingPlanDetailProps.setProperty(PROPERTY_KEYID2, samplingPlanVersionId);
        addSamplingPlanDetailProps.setProperty("linkid", "item");
        addSamplingPlanDetailProps.setProperty("itemsdcid", itemSdcId.substring(1));
        addSamplingPlanDetailProps.setProperty("itemkeyid1", workItemIds);
        addSamplingPlanDetailProps.setProperty("itemkeyid2", workItemVersionIds);
        addSamplingPlanDetailProps.setProperty("s_samplingplanitemno", itemNumbers.substring(1));
        this.getActionProcessor().processAction("AddSDIDetail", "1", addSamplingPlanDetailProps);
    }
}

