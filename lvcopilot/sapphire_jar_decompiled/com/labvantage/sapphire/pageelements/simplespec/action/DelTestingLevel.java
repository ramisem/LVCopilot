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
import sapphire.xml.PropertyList;

public class DelTestingLevel
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SAMPLINGPLANDETAILNO = "samplingplandetailno";
    public static final String PROPERTY_AUTOMATICVERSIONING = "automaticversioning";
    public static final String PROPERTY_SAMPLINGPLANIDCOLUMN = "samplingplanidcolumn";
    public static final String PROPERTY_SAMPLINGPLANVERSIONIDCOLUMN = "samplingplanversionidcolumn";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty(PROPERTY_SDCID);
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        String keyId2 = properties.getProperty(PROPERTY_KEYID2);
        String keyId3 = properties.getProperty(PROPERTY_KEYID3);
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
        if (samplingPlanDetailNumbers.isEmpty()) {
            throw new SapphireException("Missing mandatory input property sampling plan detail number");
        }
        SamplingPlanHelper samplingPlanHelper = new SamplingPlanHelper(this.getConnectionId());
        PropertyList samplingPlanProps = samplingPlanHelper.prepareSamplingPlanForAddOrEdit(sdcId, keyId1, keyId2, keyId3, automaticVersioning, samplingPlanIdColumn, samplingPlanVersionIdColumn);
        String samplingPlanId = samplingPlanProps.getProperty("samplingplanid");
        String samplingPlanVersionId = samplingPlanProps.getProperty("samplingplanversionid");
        StringBuilder samplingPlanIds = new StringBuilder();
        StringBuilder samplingPlanVersionIds = new StringBuilder();
        List<String> samplingPlanDetailNumberList = Arrays.asList(StringUtil.split(samplingPlanDetailNumbers, ";"));
        for (String ignore : samplingPlanDetailNumberList) {
            samplingPlanIds.append(";").append(samplingPlanId);
            samplingPlanVersionIds.append(";").append(samplingPlanVersionId);
        }
        PropertyList deleteSamplingPlanDetailProps = new PropertyList();
        deleteSamplingPlanDetailProps.setProperty(PROPERTY_SDCID, "LV_SamplingPlan");
        deleteSamplingPlanDetailProps.setProperty(PROPERTY_KEYID1, samplingPlanId);
        deleteSamplingPlanDetailProps.setProperty("s_samplingplanid", samplingPlanIds.substring(1));
        deleteSamplingPlanDetailProps.setProperty("s_samplingplanversionid", samplingPlanVersionIds.substring(1));
        deleteSamplingPlanDetailProps.setProperty(PROPERTY_KEYID2, samplingPlanVersionId);
        deleteSamplingPlanDetailProps.setProperty("linkid", "detail");
        deleteSamplingPlanDetailProps.setProperty("s_samplingplandetailno", samplingPlanDetailNumbers);
        this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSamplingPlanDetailProps);
    }
}

