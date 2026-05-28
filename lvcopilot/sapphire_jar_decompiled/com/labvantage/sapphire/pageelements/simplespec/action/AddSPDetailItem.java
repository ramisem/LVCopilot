/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.SamplingPlanHelper;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSPDetailItem
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SAMPLINGPLANDETAILNO = "samplingplandetailno";
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
        String samplingPlanDetailNumbers = properties.getProperty(PROPERTY_SAMPLINGPLANDETAILNO);
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
        if (samplingPlanDetailNumbers.isEmpty()) {
            throw new SapphireException("Missing mandatory input property sampling plan detail number");
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
        String getItemNumbersSql = "SELECT i.s_samplingplanitemno, i.itemkeyid1, i.itemkeyid2 FROM s_spitem i WHERE i.s_samplingplanid = ? AND i.s_samplingplanversionid = ? AND itemsdcid = 'WorkItem'";
        DataSet getItemNumbersDs = this.getQueryProcessor().getPreparedSqlDataSet(getItemNumbersSql, (Object[])new String[]{samplingPlanId, samplingPlanVersionId});
        StringBuilder itemNumbers = new StringBuilder();
        List<String> workItemIdList = Arrays.asList(StringUtil.split(workItemIds, ";"));
        List<String> workItemVersionIdList = Arrays.asList(StringUtil.split(workItemVersionIds, ";"));
        for (int i = 0; i < workItemIdList.size(); ++i) {
            String workItemId = workItemIdList.get(i);
            String workItemVersionId = workItemVersionIdList.get(i);
            for (int j = 0; j < getItemNumbersDs.getRowCount(); ++j) {
                String dsWorkItemId = getItemNumbersDs.getString(j, "itemkeyid1");
                String dsWorkItemVersionId = getItemNumbersDs.getString(j, "itemkeyid2");
                if (!workItemId.equals(dsWorkItemId) || !workItemVersionId.equals(dsWorkItemVersionId)) continue;
                itemNumbers.append(";").append(getItemNumbersDs.getInt(j, "s_samplingplanitemno"));
            }
        }
        PropertyList addDetailItemProps = new PropertyList();
        addDetailItemProps.setProperty(PROPERTY_SDCID, "LV_SamplingPlan");
        addDetailItemProps.setProperty(PROPERTY_KEYID1, samplingPlanId);
        addDetailItemProps.setProperty(PROPERTY_KEYID2, samplingPlanVersionId);
        addDetailItemProps.setProperty("linkid", "item");
        addDetailItemProps.setProperty("detaillinkid", "detail");
        addDetailItemProps.setProperty("s_samplingplanitemno", itemNumbers.substring(1));
        addDetailItemProps.setProperty("s_samplingplandetailno", samplingPlanDetailNumbers);
        this.getActionProcessor().processAction("AddSDIDetail", "1", addDetailItemProps);
    }
}

