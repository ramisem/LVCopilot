/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpdateInspectionLot
extends BaseAction {
    public static final String PROPERTY_ENDDATE = "enddate";
    public static final String PROPERTY_SHORTTEXT = "shorttext";
    public static final String PROPERTY_STARTDATE = "startdate";
    public static final String PROPERTY_INSPECTIONLOTNO = "inspectionlotnumber";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String inspectionLotNo = propertyList.getProperty(PROPERTY_INSPECTIONLOTNO);
        String endDate = propertyList.getProperty(PROPERTY_ENDDATE);
        String shortText = propertyList.getProperty(PROPERTY_SHORTTEXT);
        String startDate = propertyList.getProperty(PROPERTY_STARTDATE);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Sample");
        String sql = "SELECT s_sampleid from s_sample where u_batch = '" + inspectionLotNo + "'";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        if (ds == null || ds.getRowCount() == 0) {
            throw new ActionException("Did not find a matching sample for inspectionlotno: " + inspectionLotNo);
        }
        String response = "Updating sample with id: " + ds.getColumnValues("s_sampleid", ";");
        props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
        if (startDate != null && startDate.length() > 0) {
            props.setProperty("u_inspecstartdt", startDate);
        }
        if (endDate != null && endDate.length() > 0) {
            props.setProperty("u_inspecenddt", endDate);
        }
        if (shortText != null && shortText.length() > 0) {
            props.setProperty("u_shorttext", shortText);
        }
        props.setProperty("sampletypeid", "Research");
        this.getActionProcessor().processAction("EditSDI", "1", props);
        propertyList.setProperty("responsemessage", response);
    }
}

