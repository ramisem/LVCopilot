/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.IncidentUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_Incdt
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103378 $";

    @Override
    public void preAdd(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        String incidentStatus = actionProps.getProperty("incidentstatus");
        if (incidentStatus == null || incidentStatus.length() == 0) {
            incidentStatus = "Initial";
            primary.setValue(-1, "incidentstatus", incidentStatus);
        }
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String incidentDt = primary.getValue(i, "incidentdt", "");
            if (incidentDt.length() != 0 || primary.getValue(i, "templateflag", "N").equals("Y")) continue;
            primary.setDate(i, "incidentdt", DateTimeUtil.getNowCalendar());
        }
    }

    @Override
    public void postEdit(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet dsPrimary = sdidata.getDataset("primary");
        if (dsPrimary != null) {
            for (int i = 0; i < dsPrimary.size(); ++i) {
                String incidentId = dsPrimary.getValue(i, "incidentid", "");
                String incidentStatus = dsPrimary.getValue(i, "incidentstatus", "");
                if (incidentStatus.trim().length() > 0 && incidentStatus.equalsIgnoreCase("Cancelled")) {
                    if (incidentId.trim().length() <= 0) continue;
                    IncidentUtil.incidentCancelled(incidentId, this.getQueryProcessor(), this.getActionProcessor());
                    continue;
                }
                if (incidentStatus.trim().length() <= 0 || !incidentStatus.equalsIgnoreCase("Investigated")) continue;
                IncidentUtil.incidentInvestigated(incidentId, this.getQueryProcessor(), this.getActionProcessor());
            }
        }
    }

    @Override
    public void postApprove(DataSet dsApproval) {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String incidentStatus = "Pass".equalsIgnoreCase(approvalFlag) ? "Approved" : "Rejected";
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, "incidentstatus", incidentStatus);
            }
            if (dsProp.size() > 0) {
                String sdcId = "LV_Incdt";
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcId);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put("incidentstatus", dsProp.getColumnValues("incidentstatus", ";"));
                props.put("tracelogid", dsApproval.getString(0, "tracelogid", ""));
                actionProcessor.processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet incidentItem = sdiData.getDataset("incidentitem");
        if (OpalUtil.isNotEmpty(incidentItem)) {
            ArrayList<DataSet> groups = incidentItem.getGroupedDataSets("incidentid");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select max(incidentitemid) maxitemid, incidentid from incidentitem where incidentid in( " + safeSQL.addIn(incidentItem.getColumnValues("incidentid", 0, incidentItem.getRowCount(), ";", true), ";") + " ) group by incidentid";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            for (int grp = 0; grp < groups.size(); ++grp) {
                int seq = 0;
                DataSet temp = groups.get(grp);
                for (int i = 0; i < temp.size(); ++i) {
                    String sourcesdcid = temp.getString(i, "sourcesdcid");
                    if (!OpalUtil.isNotEmpty(sourcesdcid)) continue;
                    String incidentId = temp.getString(i, "incidentId");
                    String sequenceVal = "";
                    String sequence = "";
                    if (OpalUtil.isNotEmpty(incidentId) && seq == 0 && OpalUtil.isNotEmpty(ds)) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put("incidentid", incidentId);
                        DataSet temp2 = ds.getFilteredDataSet(filter);
                        if (OpalUtil.isNotEmpty(temp2)) {
                            sequenceVal = temp2.getValue(0, "maxitemid", "0");
                            sequence = sequenceVal.substring(sequenceVal.lastIndexOf("-") + 1).trim();
                            try {
                                seq = Integer.parseInt(sequence);
                            }
                            catch (NumberFormatException e) {
                                seq = 0;
                            }
                        }
                    }
                    StringBuffer incidentItemid = new StringBuffer();
                    sequence = String.valueOf(++seq);
                    int obtainedLength = sequence.length();
                    for (int seqlength = 5; seqlength > obtainedLength; --seqlength) {
                        sequence = "0" + sequence;
                    }
                    String generatedSequence = sequence;
                    incidentItemid.append(incidentId).append("-").append(generatedSequence);
                    temp.setString(i, "incidentItemid", incidentItemid.toString());
                }
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

