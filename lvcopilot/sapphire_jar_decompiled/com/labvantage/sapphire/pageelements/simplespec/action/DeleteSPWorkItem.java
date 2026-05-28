/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.SamplingPlanHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSPWorkItem
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
        ArrayList<String> getItemNumbersParams = new ArrayList<String>();
        getItemNumbersParams.add(samplingPlanId);
        getItemNumbersParams.add(samplingPlanVersionId);
        StringBuilder getItemNumbersWhereFragment = new StringBuilder();
        List<String> workItemIdList = Arrays.asList(StringUtil.split(workItemIds, ";"));
        List<String> workItemVersionIdList = Arrays.asList(StringUtil.split(workItemVersionIds, ";"));
        if (workItemIdList.size() != workItemVersionIdList.size()) {
            throw new SapphireException("Work item ID number is not consistent with work item version ID number");
        }
        for (int i = 0; i < workItemIdList.size(); ++i) {
            getItemNumbersWhereFragment.append(" or (itemkeyid1 = ? and itemkeyid2 = ?)");
            getItemNumbersParams.add(workItemIdList.get(i));
            getItemNumbersParams.add(workItemVersionIdList.get(i));
        }
        String getItemNumbersSql = "SELECT s_samplingplanitemno FROM s_spitem WHERE s_samplingplanid = ? AND s_samplingplanversionid = ? AND itemsdcid = 'WorkItem' AND (" + getItemNumbersWhereFragment.substring(4) + ")";
        DataSet getItemNumbersDs = this.getQueryProcessor().getPreparedSqlDataSet(getItemNumbersSql, getItemNumbersParams.toArray());
        String itemNumbers = getItemNumbersDs.getColumnValues("s_samplingplanitemno", ";");
        if (itemNumbers.length() > 0) {
            PropertyList deleteSamplingPlanDetailProps = new PropertyList();
            deleteSamplingPlanDetailProps.setProperty(PROPERTY_SDCID, "LV_SamplingPlan");
            deleteSamplingPlanDetailProps.setProperty(PROPERTY_KEYID1, samplingPlanId);
            deleteSamplingPlanDetailProps.setProperty(PROPERTY_KEYID2, samplingPlanVersionId);
            deleteSamplingPlanDetailProps.setProperty("linkid", "item");
            deleteSamplingPlanDetailProps.setProperty("s_samplingplanitemno", itemNumbers);
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSamplingPlanDetailProps);
        }
    }
}

