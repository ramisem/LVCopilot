/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.sdiapproval.EditSDIApprovalStep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDIApprovalPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 97699 $";

    @Override
    protected void saveData() throws SapphireException {
        String sdcid;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        List columnsList = elementData.getColumnList();
        if (!columnsList.contains("usersequence") && this._TableMD.doesColumnExists("usersequence")) {
            elementData.addColumn("usersequence");
        }
        if (((sdcid = this.getSdcId()) == null || sdcid.equals("")) && this._Props != null && this._Props.get("sdcid") != null) {
            sdcid = (String)this._Props.get("sdcid");
        }
        String keyid1 = this.getKeyid1();
        String keyid2 = this.getKeyid2();
        String keyid3 = this.getKeyid3();
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            String[] removekey = StringUtil.split(removedkeys, ";");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("auditreason", this._AuditReason);
            actionProps.setProperty("auditactivity", this._AuditActivity);
            actionProps.setProperty("auditsignedflag", this._AuditSignedFlag);
            actionProps.setProperty("tracelogid", this._TraceLogId);
            StringBuffer sbApprovalTypes = new StringBuffer();
            StringBuffer sbApprovalSteps = new StringBuffer();
            StringBuffer sbApprovalStepInstances = new StringBuffer();
            StringBuffer sbSdcIds = new StringBuffer();
            StringBuffer sbKeyId1 = new StringBuffer();
            StringBuffer sbKeyId2 = new StringBuffer();
            StringBuffer sbKeyId3 = new StringBuffer();
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                sbSdcIds.append(sdcid).append(";");
                sbKeyId1.append(keyid1).append(";");
                sbKeyId2.append(keyid2).append(";");
                sbKeyId3.append(keyid3).append(";");
                sbApprovalTypes.append(ds[0]).append(";");
                sbApprovalSteps.append(ds[1]).append(";");
                sbApprovalStepInstances.append(ds[2]).append(";");
            }
            if (sbApprovalTypes.length() > 0) {
                sbSdcIds.setLength(sbSdcIds.length() - 1);
                sbKeyId1.setLength(sbKeyId1.length() - 1);
                sbKeyId2.setLength(sbKeyId2.length() - 1);
                sbKeyId3.setLength(sbKeyId3.length() - 1);
                sbApprovalTypes.setLength(sbApprovalTypes.length() - 1);
                sbApprovalSteps.setLength(sbApprovalSteps.length() - 1);
                sbApprovalStepInstances.setLength(sbApprovalStepInstances.length() - 1);
                actionProps.setProperty("sdcid", sbSdcIds.toString());
                actionProps.setProperty("keyid1", sbKeyId1.toString());
                actionProps.setProperty("keyid2", sbKeyId2.toString());
                actionProps.setProperty("keyid3", sbKeyId3.toString());
                actionProps.setProperty("approvaltypeid", sbApprovalTypes.toString());
                actionProps.setProperty("approvalstep", sbApprovalSteps.toString());
                actionProps.setProperty("approvalstepinstance", sbApprovalStepInstances.toString());
                this.executeAction("DeleteSDIApproval", "1", actionProps);
            }
        }
        HashMap<String, String> updateMap = new HashMap<String, String>();
        HashMap<String, String> addMap = new HashMap<String, String>();
        String columnid = "";
        String value = "";
        String currentApprovalTypeId = "";
        HashMap<String, String> dataMap = new HashMap<String, String>();
        boolean resequenceFlag = false;
        for (int i = 0; i < elementData.size(); ++i) {
            String __status = elementData.getColumnData(i, "__status");
            if (elementColumns.size() > 0) {
                for (int row = 0; row < elementColumns.size(); ++row) {
                    columnid = (String)elementColumns.get(row);
                    value = elementData.getColumnData(i, columnid);
                    if (columnid.equalsIgnoreCase("keyid1") || columnid.equalsIgnoreCase("keyid2") || columnid.equalsIgnoreCase("keyid3") || columnid.equalsIgnoreCase("sdcid") || "usersequence".equals(columnid)) continue;
                    if (this._TableMD.doesColumnExists(columnid)) {
                        if ("assignedto".equals(columnid) && "(null)".equals(value)) {
                            value = "";
                        }
                        if (columnid.equalsIgnoreCase("approvaltypeid") || columnid.equalsIgnoreCase("approvalstep") || columnid.equalsIgnoreCase("roleid") || columnid.equalsIgnoreCase("assignedto") || columnid.equalsIgnoreCase("forcepeerflag")) {
                            if (columnid.equalsIgnoreCase("approvalstep") && (value == null || value.trim().equals(""))) {
                                throw new SapphireException("Approval step not defined!!");
                            }
                            dataMap.put(columnid, dataMap.containsKey(columnid) ? dataMap.get(columnid) + ";" + value : value);
                        }
                        if (__status == null) continue;
                        if ("N".equals(__status)) {
                            if (value == null || value.trim().equals("") || value.equalsIgnoreCase("undefined")) {
                                value = columnid.equalsIgnoreCase("mandatoryflag") ? "N" : "";
                            }
                            addMap.put(columnid, addMap.containsKey(columnid) ? addMap.get(columnid) + ";" + value : value);
                            continue;
                        }
                        if (!"E".equals(__status)) continue;
                        updateMap.put(columnid, updateMap.containsKey(columnid) ? updateMap.get(columnid) + ";" + value : value);
                        continue;
                    }
                    if (__status == null || !__status.equals("N") || !"approvalfunction".equalsIgnoreCase(columnid) || addMap.containsKey("approvalfunction")) continue;
                    addMap.put("approvalfunction", value);
                }
            }
            int currentsequence = i;
            if (("N".equals(__status) || "E".equals(__status)) && !elementData.getColumnData(i, "usersequence").equalsIgnoreCase("")) {
                if (elementData.getColumnData(i - 1, "usersequence").equals("")) {
                    elementData.setColumnValue(i, "usersequence", String.valueOf(currentsequence));
                    resequenceFlag = true;
                } else if (!resequenceFlag) {
                    if ("E".equals(__status)) {
                        currentsequence = Integer.parseInt(value);
                    } else {
                        int previoussequence = Integer.parseInt(elementData.getColumnData(i - 1, "usersequence"));
                        if (previoussequence >= i) {
                            currentsequence = previoussequence + 1;
                            elementData.setColumnValue(i, "usersequence", String.valueOf(currentsequence));
                        }
                    }
                }
            }
            if ("N".equals(__status)) {
                addMap.put("sdcid", addMap.containsKey("sdcid") ? addMap.get("sdcid") + ";" + sdcid : sdcid);
                addMap.put("keyid1", addMap.containsKey("keyid1") ? addMap.get("keyid1") + ";" + keyid1 : keyid1);
                addMap.put("keyid2", addMap.containsKey("keyid2") ? addMap.get("keyid2") + ";" + keyid2 : keyid2);
                addMap.put("keyid3", addMap.containsKey("keyid3") ? addMap.get("keyid3") + ";" + keyid3 : keyid3);
                addMap.put("usersequence", addMap.containsKey("usersequence") ? addMap.get("usersequence") + ";" + String.valueOf(currentsequence) : String.valueOf(currentsequence));
                continue;
            }
            if (!"E".equals(__status)) continue;
            updateMap.put("sdcid", updateMap.containsKey("sdcid") ? updateMap.get("sdcid") + ";" + sdcid : sdcid);
            updateMap.put("keyid1", updateMap.containsKey("keyid1") ? updateMap.get("keyid1") + ";" + keyid1 : keyid1);
            updateMap.put("keyid2", updateMap.containsKey("keyid2") ? updateMap.get("keyid2") + ";" + keyid2 : keyid2);
            updateMap.put("keyid3", updateMap.containsKey("keyid3") ? updateMap.get("keyid3") + ";" + keyid3 : keyid3);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(currentsequence) : String.valueOf(currentsequence));
        }
        this.validateData(dataMap);
        if (updateMap.size() > 0) {
            this.addAuditCols(updateMap);
            updateMap.put("__actionclass", EditSDIApprovalStep.class.getName());
            this.updateSDIDetail(updateMap);
        }
        if (addMap.size() > 0) {
            this.addAuditCols(addMap);
            this.addStep(addMap);
        }
    }

    private void validateData(HashMap dataMap) throws SapphireException {
        DataSet data = new DataSet();
        data.addColumnValues("approvaltypeid", 0, (String)dataMap.get("approvaltypeid"), ";");
        data.addColumnValues("approvalstep", 0, (String)dataMap.get("approvalstep"), ";");
        data.addColumnValues("roleid", 0, (String)dataMap.get("roleid"), ";");
        data.addColumnValues("assignedto", 0, (String)dataMap.get("assignedto"), ";");
        data.sort("approvaltypeid,approvalstep,roleid,assignedto");
        ArrayList<DataSet> ds = data.getGroupedDataSets("approvaltypeid,approvalstep,roleid,assignedto");
        for (int i = 0; i < ds.size(); ++i) {
            DataSet groupedDS = ds.get(i);
            if (groupedDS.size() <= 1) continue;
            throw new SapphireException("Action Failure", "VALIDATION", groupedDS.getValue(0, "approvaltypeid") + " has duplicate or blank value in AssignedTo field for approval step: " + groupedDS.getValue(0, "approvalstep"));
        }
    }

    private void addStep(HashMap addMap) throws SapphireException {
        Set keySet = addMap.keySet();
        PropertyList actionProps = new PropertyList();
        for (String columnid : keySet) {
            if (columnid.equals("approvalsepinstance")) continue;
            String value = (String)addMap.get(columnid);
            if (columnid.equalsIgnoreCase("sdcid")) {
                actionProps.setProperty("sdcid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid1")) {
                actionProps.setProperty("keyid1", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid2")) {
                actionProps.setProperty("keyid2", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid3")) {
                actionProps.setProperty("keyid3", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("approvaltypeid")) {
                actionProps.setProperty("approvaltypeid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("approvalstep")) {
                actionProps.setProperty("approvalstep", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("roleid")) {
                actionProps.setProperty("roleid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("mandatoryflag")) {
                actionProps.setProperty("mandatory", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("assignedto")) {
                actionProps.setProperty("assignedto", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("forcepeerflag")) {
                actionProps.setProperty("forcepeerflag", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("usersequence")) {
                actionProps.setProperty("usersequence", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("tracelogid")) {
                actionProps.setProperty("tracelogid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("auditreason")) {
                actionProps.setProperty("auditreason", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("auditsignedflag")) {
                actionProps.setProperty("auditsignedflag", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("auditactivity")) {
                actionProps.setProperty("auditactivity", value);
                continue;
            }
            if (!columnid.equalsIgnoreCase("approvalfunction")) continue;
            actionProps.setProperty("approvalfunction", value);
        }
        this.executeAction("AddSDIApprovalStep", "1", actionProps);
    }

    private void editStep(HashMap updateMap) throws SapphireException {
        Set keySet = updateMap.keySet();
        PropertyList actionProps = new PropertyList();
        for (String columnid : keySet) {
            String value = (String)updateMap.get(columnid);
            if (columnid.equalsIgnoreCase("sdcid")) {
                actionProps.setProperty("sdcid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid1")) {
                actionProps.setProperty("keyid1", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid2")) {
                actionProps.setProperty("keyid2", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("keyid3")) {
                actionProps.setProperty("keyid3", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("approvaltypeid")) {
                actionProps.setProperty("approvaltypeid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("approvalstep")) {
                actionProps.setProperty("approvalstep", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("approvalstepinstance")) {
                actionProps.setProperty("approvalstepinstance", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("roleid")) {
                actionProps.setProperty("roleid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("mandatoryflag")) {
                actionProps.setProperty("mandatoryflag", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("assignedto")) {
                actionProps.setProperty("assignedto", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("usersequence")) {
                actionProps.setProperty("usersequence", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("tracelogid")) {
                actionProps.setProperty("tracelogid", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("auditreason")) {
                actionProps.setProperty("auditreason", value);
                continue;
            }
            if (columnid.equalsIgnoreCase("auditsignedflag")) {
                actionProps.setProperty("auditsignedflag", value);
                continue;
            }
            if (!columnid.equalsIgnoreCase("auditactivity")) continue;
            actionProps.setProperty("auditactivity", value);
        }
        this.executeAction("EditSDIApprovalStep", "1", actionProps);
    }

    private void addAuditCols(Map propsMap) {
        if (this._TraceLogId != null && this._TraceLogId.length() > 0) {
            propsMap.put("tracelogid", this._TraceLogId);
        }
        if (this._AuditReason != null && this._AuditReason.length() > 0) {
            propsMap.put("auditreason", this._AuditReason);
            propsMap.put("auditactivity", this._AuditActivity);
            propsMap.put("auditsignedflag", this._AuditSignedFlag);
        }
    }
}

