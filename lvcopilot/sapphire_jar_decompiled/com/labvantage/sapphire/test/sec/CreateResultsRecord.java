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

public class CreateResultsRecord
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sampleId = propertyList.getProperty("sampleid");
        if (sampleId == null) {
            throw new ActionException("Sample id not specified. Cannot create results record");
        }
        String sql = "select u_batch inspectionlotnumber, reviewdisposition, sdidata.u_inspcharno inspcharno, displayvalue from s_sample, sdidata, sdidataitem  where s_sampleid = '" + sampleId + "' and sdidata.sdcid = 'Sample' and sdidata.keyid1 = s_sampleid  and sdidataitem.sdcid='Sample' and sdidata.keyid1=sdidataitem.keyid1 and  sdidata.paramlistid= sdidataitem.paramlistid and sdidataitem.dataset = sdidata.dataset";
        DataSet results = this.getQueryProcessor().getSqlDataSet(sql);
        if (results == null) {
            Trace.logError("No results found for specified sample " + sampleId);
            return;
        }
        DataSet sendResults = new DataSet(this.connectionInfo);
        sendResults.addColumn("inspectionlotnumber", 0);
        sendResults.addColumn("validval", 0);
        sendResults.addColumn("evalutaion", 0);
        sendResults.addColumn("value", 0);
        String insplotnumlist = results.getColumnValues("inspectionlotnumber", ";");
        sendResults.addColumnValues("inspectionlotnumber", 0, insplotnumlist, ";");
        String validvals = "1";
        sendResults.setString(0, "validval", validvals);
        sendResults.padColumn("validval");
        String evaluation = "A";
        String reviewDisposition = results.getValue(0, "reviewDisposition");
        if (reviewDisposition.equals("Approved")) {
            evaluation = "A";
        } else if (reviewDisposition.equals("Rejected")) {
            evaluation = "R";
        } else if (reviewDisposition.equals("Resampled")) {
            evaluation = "R";
        } else if (reviewDisposition.equals("Restricted")) {
            evaluation = "A";
        } else if (reviewDisposition.equals("OnHold")) {
            evaluation = "R";
        }
        sendResults.setString(0, "evaluation", evaluation);
        sendResults.padColumn("evaluation");
        String valueList = results.getColumnValues("displayvalue", ";");
        sendResults.addColumnValues("value", 0, valueList, ";");
        String inspCharNoList = results.getColumnValues("inspcharno", ";");
        sendResults.addColumnValues("inspcharno", 0, inspCharNoList, ";");
        DataSet insplot = new DataSet(this.connectionInfo);
        String inspectionLotNo = results.getValue(0, "inspectionlotnumber");
        if (inspectionLotNo == null || inspectionLotNo.length() == 0) {
            Trace.logError("Inspection lot number is null in sample " + sampleId);
            return;
        }
        String inspcharsql = "SELECT u_inspoperationno, reviewremarks remark FROM s_sample WHERE s_sampleid='" + sampleId + "'";
        DataSet inspcharDS = this.getQueryProcessor().getSqlDataSet(inspcharsql);
        if (inspcharDS == null || inspcharDS.getRowCount() == 0) {
            throw new ActionException("Inspection lot characteristics not found in paramlist");
        }
        insplot.addColumn("inspectionlotnumber", 0);
        insplot.addColumn("inspoperation", 0);
        insplot.addColumn("remark", 0);
        insplot.addRow();
        insplot.setValue(0, "inspectionlotnumber", inspectionLotNo);
        insplot.setValue(0, "inspoperation", inspcharDS.getValue(0, "u_inspoperationno"));
        insplot.setValue(0, "remark", inspcharDS.getValue(0, "remark"));
        PropertyList props = new PropertyList();
        props.setProperty("messagetypeid", "RESULTS_RECORD");
        props.setProperty("processedby", this.connectionInfo.getSysuserId());
        props.setProperty("INSPLOT", insplot.toXML());
        props.setProperty("CHAR_RESULTS", sendResults.toXML());
        this.getActionProcessor().processAction("ProcessOutMessage", "1", props);
        Trace.logInfo("::::Sending response to SAP!!!!!");
        String message = (String)props.get("sapresponse");
        Trace.logInfo("Message being sent is: " + message);
        if (!"SUCCESS".equals(props.getProperty("status"))) {
            throw new ActionException(props.getProperty("error"));
        }
    }
}

