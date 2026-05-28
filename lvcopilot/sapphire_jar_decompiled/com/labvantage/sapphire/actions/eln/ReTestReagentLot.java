/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemStatus;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReTestReagentLot
extends BaseSDIAction
implements sapphire.action.ReTestReagentLot {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String reagentlotid = properties.getProperty("reagentlotid", "");
        if (reagentlotid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No ReagentLot specified");
        }
        this.cancelQualitySample(reagentlotid);
        String newQualitySample = this.addNewQualitySample(reagentlotid);
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select worksheetid,worksheetversionid from worksheetsdi where keyid1=" + safeSQL.addVar(reagentlotid) + " and sdcid='LV_ReagentLot'";
        DataSet wsDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (wsDS != null && wsDS.size() > 0) {
            String worksheetid = wsDS.getString(0, "worksheetid", "");
            String worksheetversionid = wsDS.getString(0, "worksheetversionid");
            this.addQualitySampleToWS(newQualitySample, worksheetid, worksheetversionid);
            this.resetWorksheet(worksheetid, worksheetversionid);
        }
    }

    private void cancelQualitySample(String reagentlot) throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select s_sampleid from s_sample where reagentlotid=" + safeSQL.addVar(reagentlot) + " and samplestatus!='Cancelled'";
            DataSet qualitySampleDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (qualitySampleDS != null && qualitySampleDS.size() > 0) {
                PropertyList sampleProps = new PropertyList();
                sampleProps.setProperty("sdcid", "Sample");
                sampleProps.setProperty("keyid1", qualitySampleDS.getColumnValues("s_sampleid", ";"));
                sampleProps.setProperty("samplestatus", "Cancelled");
                this.getActionProcessor().processAction("EditSDI", "1", sampleProps);
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_UPDATE_FAILED", "Failed to cancel Quality Sample");
        }
    }

    private String addNewQualitySample(String reagentlot) throws SapphireException {
        try {
            PropertyList sampleProps = new PropertyList();
            sampleProps.setProperty("reagentlotid", reagentlot);
            sampleProps.setProperty("samplestatus", "Initial");
            this.getActionProcessor().processAction("AddReagentSample", "1", sampleProps);
            return sampleProps.getProperty("newkeyid1", "");
        }
        catch (Exception e) {
            throw new SapphireException("DB_INSERT_FAILED", "Failed to add Quality Sample");
        }
    }

    private void addQualitySampleToWS(String sampleid, String worksheetid, String worksheetversionid) throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet("select sdiworkitemid from sdiworkitem where keyid1=" + safeSQL.addVar(sampleid) + " and sdcid='Sample' ", safeSQL.getValues());
            if (workitems != null && workitems.size() > 0) {
                PropertyList wssdiProps = new PropertyList();
                wssdiProps.setProperty("worksheetid", worksheetid);
                wssdiProps.setProperty("worksheetversionid", worksheetversionid);
                wssdiProps.setProperty("sdcid", "SDIWorkItem");
                wssdiProps.setProperty("keyid1", workitems.getValue(0, "sdiworkitemid"));
                this.getActionProcessor().processActionClass(AddWorksheetSDI.class.getName(), wssdiProps);
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_INSERT_FAILED", "Failed to add Quality Sample to WS");
        }
    }

    private void resetWorksheet(String worksheetid, String worksheetversionid) throws SapphireException {
        String ITEMSTATUS_INPROGRESS = "InProgress";
        String ITEMSTATUS_COMPLETE = "Complete";
        SafeSQL safeSQL = new SafeSQL();
        DataSet worksheetitems = this.getQueryProcessor().getPreparedSqlDataSet("select * from worksheetitem where worksheetid=" + safeSQL.addVar(worksheetid) + " and worksheetversionid=" + safeSQL.addVar(worksheetversionid) + " and itemstatus=" + safeSQL.addVar(ITEMSTATUS_COMPLETE), safeSQL.getValues());
        ActionBlock ab = new ActionBlock();
        if (worksheetitems != null && worksheetitems.size() > 0) {
            for (int i = 0; i < worksheetitems.size(); ++i) {
                String worksheetitemid = worksheetitems.getValue(i, "worksheetitemid");
                String worksheetitemversionid = worksheetitems.getValue(i, "worksheetitemversionid");
                PropertyList wsiProps = new PropertyList();
                wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                wsiProps.setProperty("status", ITEMSTATUS_INPROGRESS);
                wsiProps.setProperty("bypassstatuscheck", "Y");
                wsiProps.setProperty("bypassavailabilitycheck", "Y");
                wsiProps.setProperty("worksheetid", worksheetid);
                wsiProps.setProperty("worksheetversionid", worksheetversionid);
                wsiProps.setProperty("worksheetitemid", worksheetitemid);
                wsiProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                ab.setActionClass("EditItem_" + worksheetitemid + worksheetitemversionid, SetWorksheetItemStatus.class.getName(), wsiProps);
            }
            this.getActionProcessor().processActionBlock(ab);
        }
    }
}

