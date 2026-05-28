/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.actions;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ContextMap;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateReservationActivities
extends BaseAction
implements WAPConstants {
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_TYPE_REQUEST = "request";
    public static final String PROPERTY_REQUESTID = "requestid";
    public static final String RETURN_ACTIVITYIID = "activityid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String type = properties.getProperty(PROPERTY_TYPE);
        if (type.equals(PROPERTY_TYPE_REQUEST)) {
            String requestidprop = properties.getProperty(PROPERTY_REQUESTID);
            if (requestidprop.length() == 0) {
                throw new ActionException("No request provided");
            }
            CalendarConverter calendarConverter = new CalendarConverter(new DateTimeUtil(this.connectionInfo));
            String[] requestList = StringUtil.split(requestidprop, ";");
            String outActivityid = "";
            for (int activitynumber = 0; activitynumber < requestList.length; ++activitynumber) {
                String requestid = requestList[activitynumber];
                if (this.database.getPreparedCount("SELECT count(*) FROM activity WHERE activitycontextsdcid=? AND activitycontextkeyid1=?", new String[]{"Request", requestidprop}) > 0) {
                    throw new ActionException("Reservations for Request " + requestid + " already exists. Aborting");
                }
                DataSet requestDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM s_request WHERE s_requestid=?", (Object[])new String[]{requestid});
                DataSet requestitemDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM s_requestitem WHERE requestid=?", (Object[])new String[]{requestid});
                Calendar requestDate = requestDS.getCalendar(0, "requestdt", DateTimeUtil.getNowCalendar());
                Calendar dueDate = requestDS.getCalendar(0, "duedt");
                if (dueDate == null) {
                    dueDate = (Calendar)requestDate.clone();
                    dueDate.add(6, 7);
                }
                for (int i = 0; i < requestitemDS.size(); ++i) {
                    String createactivityrule;
                    String requestitemid = requestitemDS.getString(i, "s_requestitemid");
                    String productid = requestitemDS.getValue(i, "productid");
                    String productversionid = requestitemDS.getValue(i, "productversionid");
                    String samplepointid = requestitemDS.getValue(i, "samplepointid");
                    String locationid = requestitemDS.getValue(i, "locationid");
                    int itemcount = requestitemDS.getInt(i, "itemcount");
                    DataSet sampleMasterData = null;
                    boolean createPerSampleProduct = false;
                    boolean createPerSampleSamplePoint = false;
                    boolean createPerSampleLocation = false;
                    if (productid.length() > 0) {
                        sampleMasterData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT createactivityrule, maxactivitysize, testingdepartmentid FROM s_product WHERE s_productid=? AND s_productversionid=?", (Object[])new String[]{productid, productversionid});
                        createactivityrule = sampleMasterData.getValue(0, "createactivityrule");
                        boolean bl = createPerSampleProduct = createactivityrule.length() > 0 && !createactivityrule.toLowerCase().equals("never");
                    }
                    if (!createPerSampleProduct && samplepointid.length() > 0) {
                        sampleMasterData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT createactivityrule, maxactivitysize, testingdepartmentid FROM s_samplepoint WHERE s_samplepoint=?", (Object[])new String[]{samplepointid});
                        createactivityrule = sampleMasterData.getValue(0, "createactivityrule");
                        boolean bl = createPerSampleSamplePoint = createactivityrule.length() > 0 && !createactivityrule.toLowerCase().equals("never");
                    }
                    if (!createPerSampleProduct && !createPerSampleSamplePoint && locationid.length() > 0) {
                        sampleMasterData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT createactivityrule, maxactivitysize, testingdepartmentid FROM s_location WHERE s_location=?", (Object[])new String[]{locationid});
                        createactivityrule = sampleMasterData.getValue(0, "createactivityrule");
                        boolean bl = createPerSampleLocation = createactivityrule.length() > 0 && !createactivityrule.toLowerCase().equals("never");
                    }
                    if (createPerSampleProduct || createPerSampleSamplePoint || createPerSampleLocation) {
                        int maxActivitySize = sampleMasterData.getInt(0, "maxactivitysize", 1000);
                        String testingdepartmentid = sampleMasterData.getValue(0, "testingdepartmentid");
                        WAPCommands wapCommands = new WAPCommands(this.getConnectionid());
                        String labelTemplate = "[requestid] - [itemcount] Samples";
                        Activity newActivity = this.getBaseNewActivity(labelTemplate, requestDS, requestitemDS, requestDate, dueDate, i, maxActivitySize, calendarConverter);
                        newActivity.setActivityContextSdcid("Request");
                        newActivity.setActivityContextKeyid1(requestid);
                        newActivity.setReservationType("RequestItemSample");
                        ContextMap reservationContext = new ContextMap();
                        reservationContext.put("RI", requestitemid);
                        newActivity.setReservationContext(reservationContext.toString());
                        ContextMap workContext = new ContextMap();
                        workContext.put("WSDC", "Sample");
                        workContext.put("MDSDI", createPerSampleProduct ? productid + ";" + productversionid : (createPerSampleSamplePoint ? samplepointid : locationid));
                        newActivity.setWorkContext(workContext.toString());
                        newActivity.setWorksdcid("Sample");
                        newActivity.setTestingDepartmentid(testingdepartmentid);
                        DataSet resourceRequirements = createPerSampleProduct ? wapCommands.retrieveSDIResourceRequirements("Product", productid, productversionid, "") : (createPerSampleSamplePoint ? wapCommands.retrieveSDIResourceRequirements("SamplePoint", samplepointid, "", "") : wapCommands.retrieveSDIResourceRequirements("Location", locationid, "", ""));
                        String newActivityid = this.createActivities(itemcount, maxActivitySize, wapCommands, newActivity, resourceRequirements);
                        if (newActivityid.length() <= 0) continue;
                        outActivityid = outActivityid + ";" + newActivityid;
                        continue;
                    }
                    DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workitem.workitemid, workitem.workitemversionid, workitem.createactivityrule, workitem.maxactivitysize, sdiworkitem.testingdepartmentid FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid=workitem.workitemversionid AND sdiworkitem.sdcid='LV_RequestItem' AND sdiworkitem.keyid1=?", (Object[])new String[]{requestitemid});
                    for (int j = 0; j < workitems.size(); ++j) {
                        String workitemid = workitems.getString(j, "workitemid");
                        String workitemversionid = workitems.getString(j, "workitemversionid");
                        String createactivityrule2 = workitems.getValue(j, "createactivityrule");
                        int maxActivitySize = workitems.getInt(j, "maxactivitysize");
                        String testingdepartmentid = workitems.getValue(j, "testingdepartmentid");
                        boolean createPerSDIWorkitem = createactivityrule2.toLowerCase().contains("workitem");
                        boolean createPerDataSet = createactivityrule2.toLowerCase().contains("dataset");
                        WAPCommands wapCommands = new WAPCommands(this.getConnectionid());
                        if (createPerSDIWorkitem) {
                            String labelTemplate = "[requestid] - [itemcount] " + workitemid;
                            Activity newActivity = this.getBaseNewActivity(labelTemplate, requestDS, requestitemDS, requestDate, dueDate, i, maxActivitySize, calendarConverter);
                            newActivity.setActivityContextSdcid("Request");
                            newActivity.setActivityContextKeyid1(requestid);
                            newActivity.setReservationType("RequestItemWorkItem");
                            ContextMap contextMap = new ContextMap();
                            contextMap.put("RI", requestitemid);
                            contextMap.put("WI", workitemid);
                            contextMap.put("WIV", workitemversionid);
                            newActivity.setReservationContext(contextMap.toString());
                            ContextMap workContext = new ContextMap();
                            workContext.put("WSDC", "SDIWorkItem");
                            workContext.put("MDSDI", workitemid + ";" + workitemversionid);
                            newActivity.setWorkContext(workContext.toString());
                            newActivity.setTestingDepartmentid(testingdepartmentid);
                            newActivity.setWorksdcid("SDIWorkItem");
                            DataSet resourceRequirements = wapCommands.retrieveWorkitemResourceRequirements(workitemid, workitemversionid);
                            String newActivityid = this.createActivities(itemcount, maxActivitySize, wapCommands, newActivity, resourceRequirements);
                            if (newActivityid.length() <= 0) continue;
                            outActivityid = outActivityid + ";" + newActivityid;
                            continue;
                        }
                        if (!createPerDataSet) continue;
                        DataSet workitemitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workitemitemid, keyid1, keyid2, keyid3 FROM workitemitem WHERE sdcid='ParamList' and workitemid=? AND workitemversionid=? ORDER BY usersequence, workitemitemid", (Object[])new String[]{workitemid, workitemversionid});
                        for (int k = 0; k < workitemitems.size(); ++k) {
                            String workitemitemid = workitemitems.getValue(k, "workitemitemid");
                            String paramlistid = workitemitems.getValue(k, "keyid1");
                            String paramlistversionid = workitemitems.getValue(k, "keyid2");
                            String variantid = workitemitems.getValue(k, "keyid3");
                            String labelTemplate = "[requestid] - [itemcount] " + paramlistid + " (" + variantid + ")";
                            Activity newActivity = this.getBaseNewActivity(labelTemplate, requestDS, requestitemDS, requestDate, dueDate, i, maxActivitySize, calendarConverter);
                            newActivity.setActivityContextSdcid("Request");
                            newActivity.setActivityContextKeyid1(requestid);
                            newActivity.setReservationType("RequestItemWorkItemItem");
                            ContextMap contextMap = new ContextMap();
                            contextMap.put("RI", requestitemid);
                            contextMap.put("WI", workitemid);
                            contextMap.put("WIV", workitemversionid);
                            contextMap.put("WII", workitemitemid);
                            contextMap.put("PL", paramlistid);
                            contextMap.put("PLV", paramlistversionid);
                            contextMap.put("V", variantid);
                            newActivity.setReservationContext(contextMap.toString());
                            ContextMap workContext = new ContextMap();
                            workContext.put("WSDC", "DataSet");
                            workContext.put("MDSDI", workitemid + ";" + workitemversionid + ";" + workitemitemid);
                            newActivity.setWorkContext(workContext.toString());
                            newActivity.setTestingDepartmentid(testingdepartmentid);
                            newActivity.setWorksdcid("DataSet");
                            DataSet resourceRequirements = wapCommands.retrieveWorkitemItemResourceRequirements(workitemid, workitemversionid, workitemitemid);
                            String newActivityid = this.createActivities(itemcount, maxActivitySize, wapCommands, newActivity, resourceRequirements);
                            if (newActivityid.length() <= 0) continue;
                            outActivityid = outActivityid + ";" + newActivityid;
                        }
                    }
                }
            }
            if (outActivityid.length() > 0) {
                properties.setProperty(RETURN_ACTIVITYIID, outActivityid.substring(1));
            }
        } else {
            throw new ActionException("Unrecognized reservation type");
        }
    }

    public String createActivities(int itemcount, int maxActivitySize, WAPCommands wapCommands, Activity newActivity, DataSet resourceRequirements) throws SapphireException {
        String newActivityid = "";
        do {
            int thisSize = itemcount > maxActivitySize ? maxActivitySize : itemcount;
            newActivity.setActivitySize(thisSize);
            String activityid = wapCommands.createActivity(newActivity);
            wapCommands.addActivityResources(activityid, resourceRequirements, thisSize);
            newActivityid = newActivityid + ";" + activityid;
        } while ((itemcount -= maxActivitySize) > 0);
        return newActivityid.length() > 0 ? newActivityid.substring(1) : "";
    }

    public Activity getBaseNewActivity(String labelTemplate, DataSet requestDS, DataSet requestitemDS, Calendar requestDate, Calendar dueDate, int i, int maxActivitySize, CalendarConverter calendarConverter) {
        Activity newActivity = new Activity();
        newActivity.setLabel(this.getLabel(labelTemplate, requestDS, requestitemDS, i));
        newActivity.setMaxActivitySize(maxActivitySize);
        newActivity.setTimeMode("Floating");
        newActivity.setStartRangeInstantUTC(calendarConverter.convertDatabaseCalendarToInstantUtc(requestDate));
        newActivity.setEndRangeInstantUTC(calendarConverter.convertDatabaseCalendarToInstantUtc(dueDate));
        newActivity.setIsReservation(true);
        return newActivity;
    }

    private String getLabel(String labelTemplate, DataSet requestDS, DataSet requestitemDS, int requestItemRow) {
        String label = labelTemplate;
        String[] tokens = StringUtil.getTokens(labelTemplate);
        for (int i = 0; i < tokens.length; ++i) {
            String requestValue = requestDS.getValue(0, tokens[i]);
            String requestItemValue = requestitemDS.getValue(requestItemRow, tokens[i]);
            if (requestValue.length() > 0) {
                label = StringUtil.replaceAll(label, "[" + tokens[i] + "]", requestValue);
                continue;
            }
            if (requestItemValue.length() <= 0) continue;
            label = StringUtil.replaceAll(label, "[" + tokens[i] + "]", requestItemValue);
        }
        label = StringUtil.replaceAll(label, "[requestid]", requestDS.getValue(0, "s_requestid"));
        return label;
    }
}

