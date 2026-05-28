/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.Trace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class QCMethodSampleType
extends BaseSDCRules {
    private String LABVANTAGE_CVS_ID;

    public QCMethodSampleType() {
        Trace.log(" QCMethodSampleType Rules Constructor ");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void preDelete(String rsetId, PropertyList actionProperties) throws SapphireException {
        Trace.log(" QCMethodSampleType Rules PreDelete ");
        ResultSet linkedQCBatchSampleTypeRS = null;
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("SELECT count(*) count FROM s_qcbatchsampletype ");
            sql.append("WHERE qcmethodsampletypeid IN( ");
            sql.append("SELECT keyid1 FROM rsetitems ");
            sql.append("WHERE rsetid = ? ");
            sql.append(") ");
            this.database.createPreparedResultSet(sql.toString(), new Object[]{rsetId});
            linkedQCBatchSampleTypeRS = this.database.getResultSet();
            linkedQCBatchSampleTypeRS.next();
            if (linkedQCBatchSampleTypeRS.getInt("count") > 0) {
                this.throwError("QCMethodSampleTypeError", "VALIDATION", "One or more QCMethodSampleType(s)," + actionProperties.getProperty("keyid1") + ", are in use. Cannot be deleted.");
            }
        }
        catch (SQLException sqlException) {
            this.logTrace(" SQLException in QCMethodSampleType Rules : " + sqlException.getMessage());
        }
        finally {
            if (linkedQCBatchSampleTypeRS != null) {
                linkedQCBatchSampleTypeRS = null;
            }
            if (sql != null) {
                sql = null;
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.addDataSet(primary);
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setQCEvalAction(primary);
        this.setCurrentSpecWorkItemVersion(primary);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setQCEvalAction(primary);
        this.setCurrentSpecWorkItemVersion(primary);
    }

    private void addDataSet(DataSet primary) throws SapphireException {
        StringBuffer sbKeyid1 = new StringBuffer();
        for (int i = 0; i < primary.size(); ++i) {
            sbKeyid1.append(primary.getString(i, "s_qcmethodsampletypeid")).append(";");
        }
        if (sbKeyid1.length() > 0) {
            sbKeyid1.setLength(sbKeyid1.length() - 1);
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "QCMethodSampleType");
            props.put("keyid1", sbKeyid1.toString());
            props.put("paramlistid", "QCParams");
            props.put("paramlistversionid", "1");
            props.put("variantid", "1");
            props.put("addnewonly", "Y");
            this.getActionProcessor().processAction("AddDataSet", "1", props);
        }
    }

    private void setQCEvalAction(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "qcevalruleid", "").length() <= 0 && primary.getValue(i, "specid", "").length() <= 0 || primary.getValue(i, "actioneval", "").length() != 0) continue;
            primary.setValue(i, "actioneval", "QCRuleEvaluation");
        }
    }

    private void setCurrentSpecWorkItemVersion(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            String specId = primary.getValue(i, "specid", "");
            String workitemId = primary.getValue(i, "workitemid", "");
            if (specId.length() > 0 && primary.getValue(i, "specversionid", "").equalsIgnoreCase("C")) {
                primary.setValue(i, "specversionid", null);
            }
            if (workitemId.length() <= 0 || !primary.getValue(i, "workitemversionid", "").equalsIgnoreCase("C")) continue;
            primary.setValue(i, "workitemversionid", null);
        }
    }
}

