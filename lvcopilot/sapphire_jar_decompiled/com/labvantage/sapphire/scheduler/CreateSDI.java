/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.modules.em.MonitorGroup;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseScheduleTask;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CreateSDI
extends BaseScheduleTask {
    public static final String LOG_NAME = "CreateSDI";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String instancenumberStr = "";

    @Override
    public void execute() {
        block52: {
            HashMap<String, String> props = new HashMap<String, String>();
            String sdcid = this.scheduleProperties.getProperty("sdcid");
            props.put("sdcid", sdcid);
            props.put("keyid1", "(auto)");
            PropertyList template = this.scheduleProperties.getPropertyListNotNull("template");
            String templateid = template.getProperty("templateid");
            if (templateid.length() > 0) {
                props.put("templateid", templateid);
            }
            if ("Y".equals(template.getProperty("excludedataset"))) {
                props.put("excludedataset", "Y");
                props.put("excludedatalimit", "Y");
                props.put("excludedataitem", "Y");
                props.put("excludedataset", "Y");
                props.put("excludedataapproval", "Y");
                props.put("excludedataspec", "Y");
            }
            if ("Y".equals(template.getProperty("excludeworkflow"))) {
                props.put("excludesdiworkflowrule", "Y");
            }
            if ("Y".equals(template.getProperty("excludeworkitem"))) {
                props.put("excludesdiworkitem", "Y");
            }
            if ("Y".equals(template.getProperty("applyworkitem"))) {
                props.put("applyworkitems", "Y");
            }
            if ("Y".equals(template.getProperty("excludespec"))) {
                props.put("excludesdispec", "Y");
                props.put("excludesdispecrule", "Y");
            }
            String copiesStr = this.scheduleProperties.getProperty("copies");
            String sdcId = this.scheduleProperties.getProperty("sdcid");
            int uniqueCopies = 1;
            if (copiesStr.length() > 0) {
                try {
                    uniqueCopies = Integer.parseInt(copiesStr);
                }
                catch (NumberFormatException nfe) {
                    copiesStr = "1";
                }
            } else {
                copiesStr = "1";
            }
            int copies = uniqueCopies;
            int currentCopies = 0;
            String schedulePlanId = "";
            PropertyList copySecurityDepartmentProps = this.scheduleProperties.getPropertyListNotNull("copysecuritycolumnprops");
            boolean copySecurityDepartment = copySecurityDepartmentProps.getProperty("copysecuritycolumns", "N").startsWith("Y");
            PropertyList instanceprops = this.scheduleProperties.getPropertyListNotNull("instanceprops");
            boolean instancesActive = instanceprops.getProperty("populateinstances", "N").startsWith("Y");
            String instanceNumberColumnId = instanceprops.getProperty("instancenumbercolumnid");
            if (instancesActive && (instanceNumberColumnId == null || instanceNumberColumnId.isEmpty())) {
                instancesActive = false;
            }
            int events = 1;
            boolean copySecurityDepartmentFromMonitorGroup = false;
            if (this.scheduleEvents != null) {
                SDCProcessor sdcProcessor;
                boolean isDepartmental;
                ArrayList<String> columnidlist = new ArrayList<String>();
                HashMap<String, StringBuffer> valuemap = new HashMap<String, StringBuffer>();
                events = this.scheduleEvents.size();
                this.logger.info("Event List:" + this.scheduleEvents.toString());
                this.logger.info("Processing " + events + " Events with properties:" + this.scheduleProperties.toString());
                PropertyListCollection columns = this.scheduleProperties.getCollection("columnvalues");
                if (columns != null && columns.size() > 0) {
                    for (int i = 0; i < events; ++i) {
                        ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                        schedulePlanId = event.getSchedulePlanid();
                        Integer instanceCnt = this.instanceCount;
                        int thisInstanceCopies = uniqueCopies;
                        if (instancesActive && (instanceCnt = Integer.valueOf(this.getInstanceData(sdcId, uniqueCopies, instanceprops, instanceNumberColumnId, event, instanceCnt))) != null) {
                            thisInstanceCopies = uniqueCopies * instanceCnt;
                        }
                        currentCopies = copies = currentCopies + thisInstanceCopies;
                        for (int copy = 0; copy < thisInstanceCopies; ++copy) {
                            for (int col = 0; col < columns.size(); ++col) {
                                String colid = columns.getPropertyList(col).getProperty("columnid");
                                String valuefrom = columns.getPropertyList(col).getProperty("valuefrom");
                                String value = columns.getPropertyList(col).getProperty("value");
                                String overridetemplate = columns.getPropertyList(col).getProperty("overridetemplate");
                                boolean enabled = columns.getPropertyList(col).getProperty("enabled", "Y").startsWith("Y");
                                if (!enabled || templateid.length() != 0 && overridetemplate.length() != 0 && !"Y".equals(overridetemplate)) continue;
                                if (i == 0 && copy == 0) {
                                    columnidlist.add(colid);
                                    valuemap.put(colid, new StringBuffer());
                                }
                                String tempvalue = "";
                                tempvalue = valuefrom.length() > 0 ? this.getTokenValue(valuefrom, event) : this.replaceTokens(value, event);
                                ((StringBuffer)valuemap.get(colid)).append(";" + tempvalue);
                            }
                        }
                    }
                }
                if (copySecurityDepartment && (isDepartmental = (sdcProcessor = this.getSDCProcessor()).getProperty(sdcid, "accesscontrolledflag", "N").equals("D"))) {
                    for (int i = 0; i < events; ++i) {
                        ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                        String copyFrom = copySecurityDepartmentProps.getProperty("copyfrom", "");
                        PropertyList securityProps = new PropertyList();
                        boolean securityOk = false;
                        if (copyFrom.equals("Template") && templateid != null && !templateid.isEmpty()) {
                            securityOk = this.getSecurityColumns(securityProps, sdcId, templateid, null, null);
                        } else if (copyFrom.equals("Source")) {
                            securityOk = this.getSecurityColumns(securityProps, event.getLinkSdcid(), event.getLinkKeyid1(), event.getLinkKeyid2(), event.getLinkKeyid3());
                        } else if (copyFrom.equals("Schedule Plan Item")) {
                            securityOk = this.getSecurityColumns(securityProps, "SchedulePlanItem", event.getSchedulePlanid(), event.getSchedulePlanitemid(), null);
                        } else if (copyFrom.equals("Schedule Plan")) {
                            securityOk = this.getSecurityColumns(securityProps, "SchedulePlan", event.getSchedulePlanid(), null, null);
                        } else if (copyFrom.equals("Monitor Group")) {
                            if (this.monitorGroupId != null && !this.monitorGroupId.isEmpty()) {
                                securityOk = this.getSecurityColumns(securityProps, "LV_MonitorGroup", this.monitorGroupId, null, null);
                            } else {
                                copySecurityDepartmentFromMonitorGroup = true;
                            }
                        }
                        if (!securityOk) continue;
                        String securityDepartment = securityProps.getProperty("securitydepartment", "");
                        String securityUser = securityProps.getProperty("securityuser", "");
                        StringBuffer securityDepStr = new StringBuffer();
                        StringBuffer securityUserStr = new StringBuffer();
                        for (int copy = 0; copy < uniqueCopies; ++copy) {
                            securityDepStr.append(";" + securityDepartment);
                            securityUserStr.append(";" + securityUser);
                        }
                        columnidlist.add("securityuser");
                        columnidlist.add("securitydepartment");
                        valuemap.put("securitydepartment", securityDepStr);
                        valuemap.put("securityuser", securityUserStr);
                    }
                }
                if (columnidlist != null && columnidlist.size() > 0 && ((StringBuffer)valuemap.get(columnidlist.get(0))).length() > 0) {
                    for (int col = 0; col < columnidlist.size(); ++col) {
                        String colid = (String)columnidlist.get(col);
                        props.put(colid, ((StringBuffer)valuemap.get(colid)).substring(1).toString());
                    }
                }
                props.put("copies", "" + copies);
            } else {
                props.put("copies", copiesStr);
            }
            if (instancesActive) {
                props.put(instanceNumberColumnId, this.instancenumberStr);
            }
            try {
                PropertyListCollection specs;
                PropertyListCollection workitems;
                ActionProcessor ap = this.getActionProcessor();
                ap.processAction("AddSDI", "1", props);
                String newkeyid1 = (String)props.get("newkeyid1");
                if (newkeyid1 != null && this.newKeyidsMap != null) {
                    this.addNewKeyid(sdcId, newkeyid1);
                }
                HashMap<String, String> datasetProps = new HashMap<String, String>();
                datasetProps.put("sdcid", sdcid);
                datasetProps.put("keyid1", newkeyid1);
                PropertyListCollection datasets = this.scheduleProperties.getCollection("DataSets");
                StringBuffer paramlistid = new StringBuffer();
                StringBuffer paramlistversionid = new StringBuffer();
                StringBuffer variantid = new StringBuffer();
                StringBuffer param = new StringBuffer();
                if (datasets != null) {
                    for (int i = 0; i < datasets.size(); ++i) {
                        String paramlistprops;
                        if (datasets.getPropertyList(i).getProperty("dataset").length() <= 0) continue;
                        String paramliststr = paramlistprops = datasets.getPropertyList(i).getProperty("dataset");
                        String params = "";
                        if (paramlistprops.indexOf("[") > 0) {
                            paramliststr = paramlistprops.substring(0, paramlistprops.indexOf("["));
                            params = paramlistprops.substring(paramlistprops.indexOf("[")).trim();
                        }
                        String[] paramlist = StringUtil.split(paramliststr, ";");
                        if (i == 0) {
                            paramlistid.append(paramlist[0]);
                            paramlistversionid.append(paramlist[1]);
                            variantid.append(paramlist[2]);
                            param.append(params);
                            continue;
                        }
                        paramlistid.append(";" + paramlist[0]);
                        paramlistversionid.append(";" + paramlist[1]);
                        variantid.append(";" + paramlist[2]);
                        param.append(";" + params);
                    }
                }
                if (paramlistid.length() > 0) {
                    datasetProps.put("paramlistid", paramlistid.toString());
                    datasetProps.put("paramlistversionid", paramlistversionid.toString());
                    datasetProps.put("variantid", variantid.toString());
                    datasetProps.put("param", param.toString());
                    ap.processAction("AddDataSet", "1", datasetProps);
                }
                if ((workitems = this.scheduleProperties.getCollection("WorkItems")) != null && workitems.size() > 0) {
                    HashMap<String, String> workitemProps = new HashMap<String, String>();
                    StringBuffer workitemids = new StringBuffer();
                    StringBuffer applyworkitems = new StringBuffer();
                    StringBuffer workitemversionids = new StringBuffer();
                    for (int w = 0; w < workitems.size(); ++w) {
                        if (workitems.getPropertyList(w).getProperty("workitemid").length() <= 0) continue;
                        String[] workitemlist = StringUtil.split(workitems.getPropertyList(w).getProperty("workitemid"), "|");
                        workitemids.append(";" + workitemlist[0]);
                        workitemversionids.append(";" + (workitemlist.length == 2 ? workitemlist[1] : "C"));
                        applyworkitems.append(";" + workitems.getPropertyList(w).getProperty("applyworkitem"));
                    }
                    if (workitemids.length() > 1) {
                        workitemProps.put("sdcid", sdcid);
                        workitemProps.put("keyid1", newkeyid1);
                        workitemProps.put("workitemid", workitemids.substring(1));
                        workitemProps.put("workitemversionid", workitemversionids.substring(1));
                        workitemProps.put("applyworkitem", applyworkitems.substring(1));
                        ap.processAction("AddSDIWorkItem", "1", workitemProps);
                    }
                }
                if ((specs = this.scheduleProperties.getCollection("Specs")) != null && specs.size() > 0) {
                    HashMap<String, String> specProps = new HashMap<String, String>();
                    StringBuffer specids = new StringBuffer();
                    StringBuffer specversionids = new StringBuffer();
                    for (int w = 0; w < specs.size(); ++w) {
                        if (specs.getPropertyList(w).getProperty("specid").length() <= 0) continue;
                        String[] spec = StringUtil.split(specs.getPropertyList(w).getProperty("specid"), "|");
                        specids.append(";" + spec[0]);
                        specversionids.append(";" + spec[1]);
                    }
                    if (specids.length() > 1) {
                        specProps.put("sdcid", sdcid);
                        specProps.put("keyid1", newkeyid1);
                        specProps.put("specid", specids.substring(1));
                        specProps.put("specversionid", specversionids.substring(1));
                        ap.processAction("AddSDISpec", "1", specProps);
                    }
                }
                if (!this.createMonitorGroup || this.monitorGroupId != null && !this.monitorGroupId.isEmpty()) break block52;
                try {
                    MonitorGroup monitorGroup = new MonitorGroup(this.getConnectionId());
                    monitorGroup.setParent("SchedulePlan", schedulePlanId, null, null);
                    monitorGroup.linkMonitorGroup(sdcid, newkeyid1, copySecurityDepartmentFromMonitorGroup);
                }
                catch (SapphireException e) {
                    this.logger.stackTrace(e);
                    throw new RuntimeException("Cannot link to monitor group", e);
                }
            }
            catch (ActionException ae) {
                this.logger.stackTrace(ae);
                throw new RuntimeException(ae);
            }
        }
    }

    private int getInstanceData(String sdcId, int uniqueCopies, PropertyList instanceprops, String instanceNumberColumnId, ScheduleEvent event, Integer instanceCnt) {
        String instanceCountColumnInSource = instanceprops.getProperty("instancecountcolumnid");
        boolean populateInstances = false;
        if (!instanceCountColumnInSource.isEmpty()) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(event.getLinkSdcid());
            sdiRequest.setKeyid1List(event.getLinkKeyid1());
            sdiRequest.setKeyid2List(event.getLinkKeyid2());
            sdiRequest.setKeyid3List(event.getLinkKeyid3());
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(false);
            DataSet sourcePrimary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            if (sourcePrimary.getRowCount() == 1 && sourcePrimary.isValidColumn(instanceCountColumnInSource)) {
                BigDecimal instanceBd = sourcePrimary.getBigDecimal(0, instanceCountColumnInSource);
                if (instanceBd != null) {
                    if (instanceCnt == null) {
                        instanceCnt = instanceBd.intValue();
                    }
                    populateInstances = true;
                } else {
                    instanceCnt = 1;
                }
            }
        }
        int oldInstances = 0;
        if (populateInstances) {
            BigDecimal maxInstance;
            PropertyListCollection matchInstanceColumnCollection = instanceprops.getCollectionNotNull("groupbycolumns");
            StringBuilder where = new StringBuilder();
            ArrayList<Object> params = new ArrayList<Object>();
            for (int j = 0; j < matchInstanceColumnCollection.size(); ++j) {
                PropertyList matchColumnProps = matchInstanceColumnCollection.getPropertyList(j);
                String columnId = matchColumnProps.getProperty("columnid");
                String matchToColumn = matchColumnProps.getProperty("comparetocolumn");
                if (matchToColumn.equals("Event Date")) {
                    where.append(" AND ").append(columnId).append(" = ?");
                    params.add(new Timestamp(event.getEventDt().getTime().getTime()));
                    continue;
                }
                String matchToValue = this.getTokenValue(matchToColumn, event);
                if (columnId.isEmpty() || matchToValue.isEmpty()) continue;
                where.append(" AND ").append(columnId).append(" = ?");
                params.add(matchToValue);
            }
            String sql = "SELECT max(" + instanceNumberColumnId + ") maxinstance from " + this.getSDCProcessor().getProperty(sdcId, "tableid") + " WHERE " + where.toString().substring(4);
            DataSet maxInstanceDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params.toArray());
            if (maxInstanceDs.getRowCount() > 0 && (maxInstance = maxInstanceDs.getBigDecimal(0, "maxinstance")) != null) {
                oldInstances = maxInstance.intValue();
            }
        }
        this.instancenumberStr = "";
        for (int j = 0; j < instanceCnt; ++j) {
            for (int k = 0; k < uniqueCopies; ++k) {
                this.instancenumberStr = this.instancenumberStr + ";" + (populateInstances ? String.valueOf(oldInstances + j + 1) : "");
            }
        }
        this.instancenumberStr = this.instancenumberStr.substring(1);
        return instanceCnt;
    }

    public String replaceTokens(String value, ScheduleEvent event) {
        if (value.indexOf("[") >= 0) {
            String[] tokens = StringUtil.getTokens(value);
            for (int i = 0; i < tokens.length; ++i) {
                value = StringUtil.replaceAll(value, "[" + tokens[i] + "]", this.getTokenValue(tokens[i], event));
            }
        }
        return value;
    }

    private String getTokenValue(String valuefrom, ScheduleEvent event) {
        String tempvalue = "";
        if ("Event Date".equals(valuefrom)) {
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            tempvalue = m18n.format(event.getEventDt());
        } else if ("Source Keyid1".equals(valuefrom)) {
            tempvalue = event.getLinkKeyid1();
        } else if ("Source Keyid2".equals(valuefrom)) {
            tempvalue = event.getLinkKeyid2();
        } else if ("Source Keyid3".equals(valuefrom)) {
            tempvalue = event.getLinkKeyid3();
        } else if ("Source Sdcid".equals(valuefrom)) {
            tempvalue = event.getLinkSdcid();
        } else if (valuefrom.startsWith("planitem") && valuefrom.contains(".")) {
            tempvalue = this.replaceSourceParentToken(valuefrom, event);
        } else if ("Plan Id".equals(valuefrom)) {
            tempvalue = event.getSchedulePlanid();
        } else if ("Event Number".equals(valuefrom)) {
            tempvalue = String.valueOf(event.getEventNum());
        } else if ("Plan Item Description".equals(valuefrom)) {
            tempvalue = event.getSchedulePlanItemDesc();
        } else if ("Monitor Group Id".equals(valuefrom)) {
            tempvalue = this.monitorGroupId != null ? this.monitorGroupId : "(null)";
        } else if ("Plan Item Id".equals(valuefrom)) {
            tempvalue = event.getSchedulePlanitemid();
        }
        return tempvalue;
    }

    private boolean getSecurityColumns(PropertyList addSdiProps, String sdcId, String keyid1, String keyid2, String keyid3) {
        boolean isDepartmental = this.getSDCProcessor().getProperty(sdcId, "accesscontrolledflag", "N").equals("D");
        boolean ok = false;
        if (isDepartmental) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcId);
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2);
            sdiRequest.setKeyid3List(keyid3);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(false);
            DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            String securityDepartment = "";
            String securityUser = "";
            if (primary.isValidColumn("securitydepartment")) {
                if (primary.getRowCount() > 0) {
                    securityDepartment = primary.getValue(0, "securitydepartment");
                    securityUser = primary.getValue(0, "securityuser");
                }
                addSdiProps.setProperty("securitydepartment", securityDepartment);
                addSdiProps.setProperty("securityuser", securityUser);
                ok = true;
            }
        }
        return ok;
    }

    @Override
    public void setMonitorGroupId(String monitorGroupId) {
        this.monitorGroupId = monitorGroupId;
    }

    @Override
    public void setCreateMonitorGroup(boolean createMonitorGroup) {
        this.createMonitorGroup = createMonitorGroup;
    }

    @Override
    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    @Override
    public void setNewKeyidsMap(HashMap<String, ArrayList<String>> newKeyids) {
        this.newKeyidsMap = newKeyids;
    }
}

