/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.instrument;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class InstrumentUsed
extends BaseAction
implements sapphire.action.InstrumentUsed {
    private Map<String, Integer> insCountMap;
    private List<Integer> insUsageList;
    private List<String> paramListList;
    private List<String> paramVerList;
    private List<String> paramVarList;
    private Map<String, Integer> insParamListUsageMap;
    private Map<String, Integer> insDefaultUsageMap;
    private Map<String, Integer> insTimeUsageMap;
    private List<String> partUsageList;
    private List<String> validInstrumentList;
    private boolean isParts;
    private static final String PROPERTY_ISPARTS = "ispart";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.isParts = Boolean.parseBoolean(properties.getProperty(PROPERTY_ISPARTS, "false"));
        if (properties.getProperty("instrumentid", "").length() == 0) {
            throw new SapphireException("Mandatory properties not set.");
        }
        String instrumentIds = properties.getProperty("instrumentid", "");
        this.insCountMap = new HashMap<String, Integer>();
        this.insTimeUsageMap = new HashMap<String, Integer>();
        this.validInstrumentList = new ArrayList<String>();
        this.partUsageList = new ArrayList<String>();
        String instRsetId = this.createRset(instrumentIds);
        if (instRsetId != null) {
            String[] insIdArray = StringUtil.split(instrumentIds, ";");
            this.populateInsCountMap(insIdArray);
            String usageStr = properties.getProperty("usagetime", "");
            if (usageStr.length() > 0) {
                this.populateUsageMap(insIdArray, usageStr);
            }
            this.calculateInstrument(instRsetId, insIdArray, properties);
            if (!this.isParts) {
                this.calculateParts(instRsetId, insIdArray, properties.getProperty("usagetype", "None"));
            }
            this.getDAMProcessor().clearRSet(instRsetId);
        }
    }

    private void populateInsCountMap(String[] insIds) {
        for (String insId : insIds) {
            if (this.insCountMap.get(insId) == null) {
                this.insCountMap.put(insId, 1);
                continue;
            }
            this.insCountMap.put(insId, this.insCountMap.get(insId) + 1);
        }
    }

    private void populateUsageMap(String[] insIds, String usageString) {
        if (usageString.length() > 0) {
            this.insUsageList = new ArrayList<Integer>();
            String[] usage = StringUtil.split(usageString, ";");
            for (int i = 0; i < insIds.length; ++i) {
                if (i < usage.length && usage[i].trim().length() > 0) {
                    try {
                        this.insUsageList.add(Integer.parseInt(usage[i]));
                    }
                    catch (NumberFormatException e) {
                        this.insUsageList.add(null);
                    }
                    continue;
                }
                this.insUsageList.add(null);
            }
        }
    }

    private void populateParamListUsageMap(String[] insIds, String paramListIdsString, String versionIdsString, String variantIdsString) {
        String rsetId = null;
        try {
            SafeSQL safeSQL = new SafeSQL();
            rsetId = this.getDAMProcessor().createRSet("ParamList", paramListIdsString, versionIdsString, variantIdsString);
            StringBuffer qry = new StringBuffer();
            qry.append("SELECT p.paramlistid, p.paramlistversionid, p.variantid, p.s_defaultusagetime").append(" FROM paramlist p, rsetitems r").append(" WHERE ").append(" p.paramlistid = r.keyid1").append(" AND p.variantid = r.keyid3").append(" AND ( p.versionstatus='P' OR p.versionstatus='C' )").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId));
            DataSet allParamListDS = this.getQueryProcessor().getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetId);
            HashMap<String, String> filterMap = new HashMap<String, String>();
            String[] paramListIds = StringUtil.split(paramListIdsString, ";");
            this.paramListList = Arrays.asList(paramListIds);
            String[] versionIds = StringUtil.split(versionIdsString, ";");
            this.paramVerList = Arrays.asList(versionIds);
            String[] variantIds = StringUtil.split(variantIdsString, ";");
            this.paramVarList = Arrays.asList(variantIds);
            for (int i = 0; i < insIds.length; ++i) {
                String paramListID = paramListIds[i];
                String versionID = versionIds[i];
                String variantId = variantIds[i];
                if (paramListID.length() <= 0 || this.insParamListUsageMap.get(paramListID + versionID + variantId) != null) continue;
                filterMap.clear();
                filterMap.put("paramlistid", paramListID);
                filterMap.put("paramlistversionid", versionID);
                filterMap.put("variantid", variantId);
                DataSet paramListDS = allParamListDS.getFilteredDataSet(filterMap);
                int usageTime = paramListDS.getInt(0, "s_defaultusagetime", -1);
                if (usageTime <= -1) continue;
                this.insParamListUsageMap.put(paramListID + versionID + variantId, usageTime);
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
    }

    private void updateInsDefaultUsageMap(String[] insIds, DataSet insDataSet) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        for (int i = 0; i < insIds.length; ++i) {
            String insId = insIds[i];
            filterMap.clear();
            filterMap.put("instrumentid", insId);
            DataSet currentDS = insDataSet.getFilteredDataSet(filterMap);
            int defaultTime = currentDS.getInt(0, "defaultusagetime");
            this.insDefaultUsageMap.put(insId, defaultTime);
        }
    }

    private void calculateFinalUsage(String[] insIds, PropertyList properties, DataSet ds) {
        HashMap<String, String> currentInsProp = new HashMap<String, String>();
        for (int i = 0; i < insIds.length; ++i) {
            String insId = insIds[i];
            currentInsProp.clear();
            currentInsProp.put("instrumentid", insId);
            DataSet currentInsDs = ds.getFilteredDataSet(currentInsProp);
            boolean isUpdateTime = this.getUpdateTime(currentInsDs.getValue(0, "usagebytimeflag", "N"));
            if (isUpdateTime) {
                String paramListKey;
                if (this.insUsageList != null && this.insUsageList.size() >= i && this.insUsageList.get(i) != null && this.insUsageList.get(i) != 0) {
                    this.updateFinalUsageMap(insId, this.insUsageList.get(i), true);
                    continue;
                }
                if (this.insParamListUsageMap == null) {
                    this.insParamListUsageMap = new HashMap<String, Integer>();
                    String paramListIdStr = properties.getProperty("paramlistid", "");
                    String versionIdStr = properties.getProperty("versionid", "");
                    String variantIDStr = properties.getProperty("variant", "");
                    if (paramListIdStr.length() > 0) {
                        this.populateParamListUsageMap(insIds, paramListIdStr, versionIdStr, variantIDStr);
                    }
                }
                if (this.insParamListUsageMap.get(paramListKey = this.paramListList != null && this.paramListList.size() >= i ? this.paramListList.get(i) + this.paramVerList.get(i) + this.paramVarList.get(i) : "") != null && this.insParamListUsageMap.get(paramListKey) != 0) {
                    this.updateFinalUsageMap(insId, this.insParamListUsageMap.get(paramListKey), true);
                    continue;
                }
                if (this.insDefaultUsageMap == null) {
                    this.insDefaultUsageMap = new HashMap<String, Integer>();
                    this.updateInsDefaultUsageMap(insIds, ds);
                }
                this.updateFinalUsageMap(insId, this.insDefaultUsageMap.get(insId), false);
                continue;
            }
            this.partUsageList.add(null);
        }
    }

    private int updateFinalUsageMap(String insId, int usage, boolean updatePartTime) {
        Integer use = this.insTimeUsageMap.get(insId);
        if (updatePartTime) {
            this.partUsageList.add(String.valueOf(usage));
        } else {
            this.partUsageList.add(null);
        }
        int updatedUsage = use != null ? use + usage : usage;
        this.insTimeUsageMap.put(insId, updatedUsage);
        return updatedUsage;
    }

    private boolean getUpdateTime(String flag) {
        return flag == null || flag.trim().equalsIgnoreCase("Y") || flag.trim().length() == 0;
    }

    private void calculateParts(String rsetId, String[] insIds, String usagetype) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select instrumentid, parentinstrumentid from instrument,rsetitems where parentinstrumentid=rsetitems.keyid1 and rsetitems.sdcid='Instrument' and rsetitems.rsetid=" + safeSQL.addVar(rsetId);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            PropertyList partsProperty = new PropertyList();
            StringBuilder partIds = new StringBuilder();
            StringBuilder partUsage = new StringBuilder();
            HashMap<String, String> currentInsProp = new HashMap<String, String>();
            for (int i = 0; i < insIds.length; ++i) {
                String parentInstId = insIds[i];
                if (!this.validInstrumentList.contains(parentInstId)) continue;
                currentInsProp.clear();
                currentInsProp.put("parentinstrumentid", parentInstId);
                DataSet partsDs = ds.getFilteredDataSet(currentInsProp);
                if (partsDs.getRowCount() <= 0) continue;
                String usage = this.partUsageList.get(i);
                for (int j = 0; j < partsDs.getRowCount(); ++j) {
                    partIds.append(";").append(partsDs.getValue(j, "instrumentid"));
                    partUsage.append(";").append(usage);
                }
            }
            if (partIds.length() > 0) {
                partsProperty.setProperty("instrumentid", partIds.substring(1));
                partsProperty.setProperty("usagetime", partUsage.substring(1));
                partsProperty.setProperty("usagetype", usagetype);
                partsProperty.setProperty(PROPERTY_ISPARTS, "true");
                this.resetAction();
                this.processAction(partsProperty);
            }
        }
    }

    private void resetAction() {
        this.insUsageList = null;
        this.paramListList = null;
        this.paramVerList = null;
        this.paramVarList = null;
        this.insParamListUsageMap = null;
        this.insDefaultUsageMap = null;
        this.insTimeUsageMap = null;
    }

    private void calculateInstrument(String rsetId, String[] insIdArray, PropertyList properties) {
        String usagetype = properties.getProperty("usagetype", "None");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select instrumentid, defaultusagetime, totalusagetime, totalusagecount, usagebytimeflag  from instrument,rsetitems where instrumentid=rsetitems.keyid1 and rsetitems.sdcid='Instrument' and rsetitems.rsetid=" + safeSQL.addVar(rsetId) + " and trackusageflag = 'Y' and usagetype = " + safeSQL.addVar(usagetype);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            this.calculateFinalUsage(insIdArray, properties, ds);
            safeSQL.reset();
            String planItemSql = "select scheduleplanid, scheduleplanitemid, linkkeyid1 from scheduleplanitem, rsetitems where  linksdcid='Instrument' and calendarflag='N' and linkkeyid1 =rsetitems.keyid1 and rsetitems.sdcid='Instrument' and rsetitems.rsetid=" + safeSQL.addVar(rsetId);
            DataSet planItemDs = this.getQueryProcessor().getPreparedSqlDataSet(planItemSql, safeSQL.getValues());
            StringBuffer uInstrumentId = new StringBuffer();
            StringBuffer uTotalUsageCount = new StringBuffer();
            StringBuffer uTotalUsageTime = new StringBuffer();
            HashMap<String, String> currentInsProp = new HashMap<String, String>();
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            ArrayList<String> doneList = new ArrayList<String>();
            for (int i = 0; i < insIdArray.length; ++i) {
                int planItemIncrementValue;
                int updatedTotalUsageTime;
                String insId = insIdArray[i];
                if (doneList.contains(insId)) continue;
                doneList.add(insId);
                currentInsProp.clear();
                currentInsProp.put("instrumentid", insId);
                DataSet currentInsDs = ds.getFilteredDataSet(currentInsProp);
                this.validInstrumentList.add(insId);
                int totalUsageTime = currentInsDs.getInt(0, "totalusagetime", 0);
                int totalUsageCount = currentInsDs.getInt(0, "totalusagecount", 0);
                boolean isUpdateTime = this.getUpdateTime(currentInsDs.getValue(0, "usagebytimeflag", "N"));
                int updatedTotalUsageCount = totalUsageCount + this.insCountMap.get(insId);
                if (isUpdateTime) {
                    updatedTotalUsageTime = totalUsageTime + this.insTimeUsageMap.get(insId);
                    planItemIncrementValue = this.insTimeUsageMap.get(insId);
                } else {
                    updatedTotalUsageTime = totalUsageTime;
                    planItemIncrementValue = this.insCountMap.get(insId);
                }
                uInstrumentId.append(";").append(insId);
                uTotalUsageCount.append(";").append(updatedTotalUsageCount);
                uTotalUsageTime.append(";").append(updatedTotalUsageTime);
                currentInsProp.clear();
                currentInsProp.put("linkkeyid1", insId);
                DataSet updatedPlanItemDs = planItemDs.getFilteredDataSet(currentInsProp);
                for (int j = 0; j < updatedPlanItemDs.getRowCount(); ++j) {
                    String plan = updatedPlanItemDs.getValue(j, "scheduleplanid");
                    String planItem = updatedPlanItemDs.getValue(j, "scheduleplanitemid");
                    try {
                        ScheduleService.incrementCounter(plan, planItem, planItemIncrementValue, sapphireConnection);
                        continue;
                    }
                    catch (SapphireException e) {
                        this.updateWorkOrder(plan, planItem);
                    }
                }
            }
            if (uInstrumentId.length() > 0) {
                this.updateInstrument(uInstrumentId, uTotalUsageCount, uTotalUsageTime);
            }
        }
    }

    private void updateInstrument(StringBuffer uInstrumentId, StringBuffer usageCount, StringBuffer usageTime) {
        String sdcId = "Instrument";
        PropertyList props = new PropertyList();
        props.put("sdcid", sdcId);
        props.put("keyid1", uInstrumentId.substring(1));
        props.put("totalusagecount", usageCount.substring(1));
        props.put("totalusagetime", usageTime.substring(1));
        try {
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        catch (ActionException e) {
            e.printStackTrace();
        }
    }

    private String createRset(String instrumentIds) {
        String rsetId = null;
        String sdcId = "Instrument";
        try {
            rsetId = this.getDAMProcessor().createRSet(sdcId, instrumentIds, "", "");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return rsetId;
    }

    private void updateWorkOrder(String schedulePlanId, String schedulePlanItemId) {
        String sdcId = "WorkOrderSDC";
        SafeSQL safeSQL = new SafeSQL();
        String workOrderSql = "select workorderid from workorder where scheduleplanid=" + safeSQL.addVar(schedulePlanId) + " and  scheduleplanitemid=" + safeSQL.addVar(schedulePlanItemId);
        StringBuilder workOrderIds = new StringBuilder();
        DataSet workOrderDs = this.getQueryProcessor().getPreparedSqlDataSet(workOrderSql, safeSQL.getValues());
        if (OpalUtil.isNotEmpty(workOrderDs)) {
            for (int i = 0; i < workOrderDs.getRowCount(); ++i) {
                String workOrderId = workOrderDs.getValue(i, "workorderid");
                workOrderIds.append(";").append(workOrderId);
            }
        }
        if (workOrderIds.length() > 0) {
            PropertyList props = new PropertyList();
            props.put("sdcid", sdcId);
            props.put("keyid1", workOrderIds.substring(1));
            props.put("outsidegraceperiodflag", "Y");
            try {
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            catch (ActionException e) {
                e.printStackTrace();
            }
        }
    }
}

