/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.scheduler.ScheduleEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseScheduleTask
extends BaseAction {
    protected ArrayList scheduleEvents;
    protected PropertyList scheduleProperties;
    protected String eventDateList;
    protected String schedulePlanidList;
    protected String schedulePlanitemidList;
    protected String sourceSdcidList;
    protected String sourceKeyid1List;
    protected String sourceKeyid2List;
    protected String sourceKeyid3List;
    protected String monitorGroupId;
    protected boolean createMonitorGroup = true;
    protected Integer instanceCount = null;
    protected HashMap<String, ArrayList<String>> newKeyidsMap;

    public void setScheduleProperties(PropertyList scheduleProperties) {
        this.scheduleProperties = scheduleProperties;
        SimpleDateFormat sdf = new SimpleDateFormat();
        StringBuffer eventDates = new StringBuffer();
        StringBuffer schedulePlanids = new StringBuffer();
        StringBuffer schedulePlanversionids = new StringBuffer();
        StringBuffer schedulePlanitemids = new StringBuffer();
        StringBuffer scheduleSdcids = new StringBuffer();
        StringBuffer scheduleKeyid1s = new StringBuffer();
        StringBuffer scheduleKeyid2s = new StringBuffer();
        StringBuffer scheduleKeyid3s = new StringBuffer();
        if (this.scheduleEvents != null) {
            for (int i = 0; i < this.scheduleEvents.size(); ++i) {
                ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                eventDates.append(";" + sdf.format(event.getEventDt().getTime()));
                schedulePlanids.append(";" + event.getSchedulePlanid());
                schedulePlanitemids.append(";" + event.getSchedulePlanitemid());
                scheduleSdcids.append(";" + event.getLinkSdcid());
                scheduleKeyid1s.append(";" + event.getLinkKeyid1());
                scheduleKeyid2s.append(";" + event.getLinkKeyid2());
                scheduleKeyid3s.append(";" + event.getLinkKeyid3());
            }
            this.eventDateList = eventDates.length() > 0 ? eventDates.substring(1) : "";
            this.schedulePlanidList = schedulePlanids.length() > 0 ? schedulePlanids.substring(1) : "";
            this.schedulePlanitemidList = schedulePlanitemids.length() > 0 ? schedulePlanitemids.substring(1) : "";
            this.sourceSdcidList = scheduleSdcids.length() > 0 ? scheduleSdcids.substring(1) : "";
            this.sourceKeyid1List = scheduleKeyid1s.length() > 0 ? scheduleKeyid1s.substring(1) : "";
            this.sourceKeyid2List = scheduleKeyid2s.length() > 0 ? scheduleKeyid2s.substring(1) : "";
            this.sourceKeyid3List = scheduleKeyid3s.length() > 0 ? scheduleKeyid3s.substring(1) : "";
        }
    }

    public String replaceSourceParentToken(String token, ScheduleEvent event) {
        if (token == null) {
            throw new IllegalArgumentException("Value from is null");
        }
        if (event == null) {
            throw new IllegalArgumentException("Schedule event is null");
        }
        String returnValue = "";
        String schedulePlanId = event.getSchedulePlanid();
        String schedulePlanItemId = event.getSchedulePlanitemid();
        if (!schedulePlanId.isEmpty() && !schedulePlanItemId.isEmpty()) {
            String getPlanItemSql = "SELECT scheduleplanid, scheduleplanitemid, scheduleplannodeid FROM scheduleplanitem WHERE scheduleplanid = ? AND scheduleplanitemid = ?";
            DataSet getPlanItemDs = this.getQueryProcessor().getPreparedSqlDataSet(getPlanItemSql, (Object[])new String[]{schedulePlanId, schedulePlanItemId});
            String schedulePlanNodeId = getPlanItemDs.getString(0, "scheduleplannodeid", "");
            DataSet parentDs = this.getParentDs(schedulePlanId, schedulePlanNodeId);
            if (parentDs.getRowCount() > 0) {
                int parentCount;
                for (parentCount = BaseScheduleTask.count(token, "parent") - 1; parentCount > 0 && !(schedulePlanNodeId = parentDs.getString(0, "parentnodeid", "")).isEmpty() && !schedulePlanNodeId.equals("root"); --parentCount) {
                    parentDs = this.getParentDs(schedulePlanId, schedulePlanNodeId);
                }
                if (parentCount == 0) {
                    String columnId = token.substring(token.lastIndexOf(".") + 1, token.length());
                    if (columnId.equals("parent")) {
                        columnId = "refkeyid1";
                    }
                    returnValue = parentDs.getString(0, columnId, "");
                } else {
                    this.logger.error("Referencing a node that does not exist: " + token);
                }
            }
        } else {
            this.logger.error("Schedule plan Id or schedule plan item Id is empty in schedule event");
        }
        return returnValue;
    }

    public static int count(String str, String find) {
        int index = 0;
        int count = 0;
        while ((index = str.indexOf(find, index)) != -1) {
            index += find.length();
            ++count;
        }
        return count;
    }

    private DataSet getParentDs(String schedulePlanId, String schedulePlanNodeId) {
        DataSet returnValue = new DataSet();
        if (!schedulePlanId.isEmpty() && !schedulePlanNodeId.isEmpty()) {
            String getNodeTreeSql = "SELECT scheduleplanid, scheduleplannodeid, parentnodeid, refsdcid, refkeyid1, refkeyid2, refkeyid3 FROM scheduleplannode WHERE scheduleplanid = ? AND scheduleplannodeid = ?";
            returnValue = this.getQueryProcessor().getPreparedSqlDataSet(getNodeTreeSql, (Object[])new String[]{schedulePlanId, schedulePlanNodeId});
        }
        return returnValue;
    }

    public void setScheduleEvents(ArrayList scheduleEvents) {
        this.scheduleEvents = scheduleEvents;
    }

    public void execute() throws SapphireException {
    }

    @Override
    public final int processAction(String actionid, String actionversionid, HashMap properties) {
        return 0;
    }

    public boolean isComplete(String planid, String planitemid, DBAccess database) throws SapphireException {
        return false;
    }

    public void setAdhocFlag(String adhocFlag) {
    }

    public void setDatabase(DBAccess db) {
    }

    public void setMonitorGroupId(String monitorGroupId) {
    }

    public void setCreateMonitorGroup(boolean createMonitorGroup) {
    }

    public void setInstanceCount(Integer instanceCount) {
    }

    public DBAccess getDatabase() {
        return null;
    }

    public void setNewKeyidsMap(HashMap<String, ArrayList<String>> newKeyids) {
    }

    protected void addNewKeyid(String sdcId, String keyid1) {
        if (this.newKeyidsMap != null) {
            ArrayList<Object> keyids;
            if (this.newKeyidsMap.containsKey(sdcId)) {
                keyids = this.newKeyidsMap.get(sdcId);
            } else {
                keyids = new ArrayList();
                this.newKeyidsMap.put(sdcId, keyids);
            }
            keyids.addAll(Arrays.asList(StringUtil.split(keyid1, ";")));
        }
    }
}

