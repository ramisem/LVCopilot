/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

import com.labvantage.opal.actions.tasks.AdvancedPullSample;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseAdvancedPullSample
extends AdvancedPullSample {
    @Override
    protected DataSet loadTestProperties(String scheduleplanid, String scheduleplanitemid, boolean isTimeZeroTask) throws SapphireException {
        return super.loadTestProperties(scheduleplanid, scheduleplanitemid, isTimeZeroTask);
    }

    @Override
    protected DataSet loadStudy(String schedulePlanId, String schedulePlanItemId) throws SapphireException {
        return super.loadStudy(schedulePlanId, schedulePlanItemId);
    }

    @Override
    protected void applySpec(String keyIdList, PropertyListCollection specs, String schedulePlanId, String schedulePlanItemId, String sdcid) throws SapphireException {
        super.applySpec(keyIdList, specs, schedulePlanId, schedulePlanItemId, sdcid);
    }

    @Override
    protected boolean isTimeZeroTask(String scheduleplanid, String scheduleplanitemid) throws SapphireException {
        return super.isTimeZeroTask(scheduleplanid, scheduleplanitemid);
    }

    @Override
    protected void addCustomSampleColumns(String schedulePlanId, String schedulePlanItemId, String conditionid, DataSet studyDS, PropertyList sampleProps) throws SapphireException {
        super.addCustomSampleColumns(schedulePlanId, schedulePlanItemId, conditionid, studyDS, sampleProps);
    }

    @Override
    protected DataSet getFirstPlanItemSample(String schedulePlanId, String firstPlanItemId, String workItemId) throws SapphireException {
        return super.getFirstPlanItemSample(schedulePlanId, firstPlanItemId, workItemId);
    }

    @Override
    protected String getFirstPlanItemId(String schedulePlanId, String schedulePlanItemId, String workItemId) throws SapphireException {
        return super.getFirstPlanItemId(schedulePlanId, schedulePlanItemId, workItemId);
    }

    @Override
    protected void postAddStabilitySample(String sampleIds, String schedulePlanId, String schedulePlanItemId) throws SapphireException {
        super.postAddStabilitySample(sampleIds, schedulePlanId, schedulePlanItemId);
    }

    @Override
    protected void preAddStabilitySample(String schedulePlanId, String schedulePlanItemId) throws SapphireException {
        super.preAddStabilitySample(schedulePlanId, schedulePlanItemId);
    }

    @Override
    protected void processReuseSample(String sampleId, String workItemId, String schedulePlanId, String schedulePlanItemId, boolean firstTimePoint) throws SapphireException {
        super.processReuseSample(sampleId, workItemId, schedulePlanId, schedulePlanItemId, firstTimePoint);
    }

    @Override
    public String getSummaryText(PropertyList propertyList, String detailLevel) {
        return super.getSummaryText(propertyList, detailLevel);
    }

    @Override
    public HashMap getDetails(PropertyList properties) {
        return super.getDetails(properties);
    }

    @Override
    protected StringBuffer getSummary(PropertyList propertyList) {
        return super.getSummary(propertyList);
    }

    @Override
    protected StringBuffer getPullAmounts(PropertyList propertyList) {
        return super.getPullAmounts(propertyList);
    }

    @Override
    protected StringBuffer getWorkorder(PropertyList propertyList) {
        return super.getWorkorder(propertyList);
    }

    @Override
    public boolean isComplete(String planid, String planitemid, DBAccess database) throws SapphireException {
        return super.isComplete(planid, planitemid, database);
    }

    @Override
    protected boolean isSampleLogged(String scheduleplanid, String scheduleplanitemid) throws SapphireException {
        return super.isSampleLogged(scheduleplanid, scheduleplanitemid);
    }

    @Override
    protected boolean isTestLogged(String scheduleplanid, String scheduleplanitemid, String workitemid) throws SapphireException {
        return super.isTestLogged(scheduleplanid, scheduleplanitemid, workitemid);
    }

    @Override
    protected String getLogName() {
        return super.getLogName();
    }

    @Override
    public String getColor() {
        return super.getColor();
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public String[] getDetailLevels() {
        return super.getDetailLevels();
    }

    @Override
    public String getSummaryHTML(PropertyList propertyList, String detailLevel) {
        return super.getSummaryHTML(propertyList, detailLevel);
    }
}

