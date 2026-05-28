/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CreateUsageDecision
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sampleId = propertyList.getProperty("sampleid");
        String reviewDisposition = propertyList.getProperty("reviewdisposition");
        if (sampleId == null) {
            throw new ActionException("Sample id not specified. Cannot create results record");
        }
        String sql = "select u_batch inspectionlotnumber, u_plant plant, reviewdisposition, reviewremarks, reviewedby from s_sample  where s_sampleid = '" + sampleId + "'";
        DataSet results = this.getQueryProcessor().getSqlDataSet(sql);
        DataSet usageDecision = new DataSet(this.connectionInfo);
        usageDecision.addRow();
        usageDecision.addColumn("inspectionlotnumber", 0);
        usageDecision.setString(0, "inspectionlotnumber", results.getString(0, "inspectionlotnumber"));
        usageDecision.addColumn("plant", 0);
        usageDecision.setString(0, "plant", results.getString(0, "plant"));
        usageDecision.addColumn("codegroup", 0);
        usageDecision.addColumn("selectsetofud", 0);
        usageDecision.setString(0, "codegroup", "LV");
        usageDecision.setString(0, "selectsetofud", "01");
        usageDecision.addColumn("remark", 0);
        usageDecision.setString(0, "remark", results.getString(0, "reviewremarks", ""));
        usageDecision.addColumn("reviewedby", 0);
        usageDecision.setString(0, "reviewedby", results.getString(0, "reviewedby", ""));
        String fromDBReviewDisposition = results.getValue(0, "reviewdisposition");
        usageDecision.addColumn("code", 0);
        if ("Cancelled".equals(reviewDisposition)) {
            usageDecision.setString(0, "code", "SCAN");
            usageDecision.setString(0, "reviewedby", this.connectionInfo.getSysuserId());
        } else {
            reviewDisposition = fromDBReviewDisposition;
            if (reviewDisposition.equals("Approved")) {
                usageDecision.setString(0, "code", "SA");
            } else if (reviewDisposition.equals("Rejected")) {
                usageDecision.setString(0, "code", "SR");
            } else if (reviewDisposition.equals("Resampled")) {
                usageDecision.setString(0, "code", "SRR");
            } else if (reviewDisposition.equals("Restricted")) {
                usageDecision.setString(0, "code", "SRES");
            } else if (reviewDisposition.equals("OnHold")) {
                usageDecision.setString(0, "code", "SONH");
            }
        }
        PropertyList props = new PropertyList();
        props.setProperty("messagetypeid", "USAGE_DECISION");
        props.setProperty("processedby", this.connectionInfo.getSysuserId());
        props.setProperty("USAGEDECISION", usageDecision.toXML());
        this.getActionProcessor().processAction("ProcessOutMessage", "1", props);
        Trace.logInfo("::::Sending response to SAP!!!!!");
        String message = props.getProperty("message");
        Trace.logInfo("Usage Decision Message being sent is: " + message);
        if (!"SUCCESS".equals(props.getProperty("status"))) {
            throw new ActionException(props.getProperty("error"));
        }
    }
}

