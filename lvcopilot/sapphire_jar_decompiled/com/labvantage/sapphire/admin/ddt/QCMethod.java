/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCMethod
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProperties) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet linkedQCBatchDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_qcbatchid FROM s_qcbatch q, rsetitems r WHERE q.qcmethodid = r.keyid1 AND q.qcmethodversionid = r.keyid2  AND r.sdcid='QCMethod' AND r.rsetid = " + safeSQL.addVar(rsetid), safeSQL.getValues());
        if (linkedQCBatchDS.size() > 0) {
            this.throwError("QCMethodError", "VALIDATION", this.getTranslationProcessor().translate("One or more QC Method(s) are in use. Cannot be deleted."));
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        try {
            ActionProcessor ap = this.getActionProcessor();
            String templateKeyId1 = actionProps.getProperty("templatekeyid1");
            String templateKeyId2 = actionProps.getProperty("templatekeyid2");
            if (templateKeyId1.trim().equals("")) {
                templateKeyId1 = actionProps.getProperty("templateid");
            }
            if (templateKeyId1 != null && templateKeyId1.length() > 0) {
                String getQCMethodSampleTypes = "select s_qcmethodsampletypeid from s_qcmethodsampletype where qcmethodid = ? AND qcmethodversionid = ?";
                PreparedStatement pstmt = this.database.prepareStatement("getQCMethodSampleTypes", getQCMethodSampleTypes);
                pstmt.setString(1, templateKeyId1);
                pstmt.setString(2, templateKeyId2);
                DataSet qcMethodSampleTypesDS = new DataSet(pstmt.executeQuery());
                pstmt.close();
                HashMap<String, String> mapKeys = new HashMap<String, String>();
                if (qcMethodSampleTypesDS.getRowCount() > 0) {
                    String newKeyId1 = sdiData.getDataset("primary").getColumnValues("s_qcmethodid", ";");
                    String newKeyId2 = sdiData.getDataset("primary").getColumnValues("s_qcmethodversionid", ";");
                    String[] newKeyId1Prop = StringUtil.split(newKeyId1, ";");
                    String copies = "" + newKeyId1Prop.length;
                    for (int childRowsCount = 0; childRowsCount < qcMethodSampleTypesDS.getRowCount(); ++childRowsCount) {
                        String childSourceId = qcMethodSampleTypesDS.getString(childRowsCount, "s_qcmethodsampletypeid");
                        PropertyList qcMethodSampleTypeProp = new PropertyList();
                        qcMethodSampleTypeProp.setProperty("sdcid", "QCMethodSampleType");
                        qcMethodSampleTypeProp.setProperty("qcmethodid", newKeyId1);
                        qcMethodSampleTypeProp.setProperty("qcmethodversionid", newKeyId2);
                        qcMethodSampleTypeProp.setProperty("copies", copies);
                        qcMethodSampleTypeProp.setProperty("templatekeyid1", childSourceId);
                        ap.processAction("AddSDI", "1", qcMethodSampleTypeProp);
                        mapKeys.put(childSourceId, qcMethodSampleTypeProp.getProperty("newkeyid1"));
                    }
                    Iterator itr = mapKeys.keySet().iterator();
                    StringBuffer getDataItems = new StringBuffer();
                    getDataItems.append("select keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, enteredtext").append(" from sdidataitem where sdcid = 'QCMethodSampleType' and keyid1 = ?");
                    pstmt = this.database.prepareStatement("getDataItems", getDataItems.toString());
                    DataSet data = new DataSet();
                    data.addColumn("keyid1", 0);
                    data.addColumn("paramlistid", 0);
                    data.addColumn("paramlistversionid", 0);
                    data.addColumn("variantid", 0);
                    data.addColumn("dataset", 1);
                    data.addColumn("paramid", 0);
                    data.addColumn("paramtype", 0);
                    data.addColumn("replicateid", 1);
                    data.addColumn("enteredtext", 0);
                    while (itr.hasNext()) {
                        String sourceId = (String)itr.next();
                        String newIds = (String)mapKeys.get(sourceId);
                        pstmt.setString(1, sourceId);
                        DataSet dataItems = new DataSet(pstmt.executeQuery());
                        String[] arrNewIds = StringUtil.split(newIds, ";");
                        for (int i = 0; i < arrNewIds.length; ++i) {
                            for (int j = 0; j < dataItems.getRowCount(); ++j) {
                                if (StringUtil.getLen(dataItems.getValue(j, "enteredtext", "")) <= 0L) continue;
                                data.copyRow(dataItems, j, 1);
                                data.setValue(data.size() - 1, "keyid1", arrNewIds[i]);
                            }
                        }
                    }
                    if (data.getRowCount() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "QCMethodSampleType");
                        props.setProperty("keyid1", data.getColumnValues("keyid1", ";"));
                        props.setProperty("paramlistid", data.getColumnValues("paramlistid", ";"));
                        props.setProperty("paramlistversionid", data.getColumnValues("paramlistversionid", ";"));
                        props.setProperty("variantid", data.getColumnValues("variantid", ";"));
                        props.setProperty("dataset", data.getColumnValues("dataset", ";"));
                        props.setProperty("paramid", data.getColumnValues("paramid", ";"));
                        props.setProperty("paramtype", data.getColumnValues("paramtype", ";"));
                        props.setProperty("replicateid", data.getColumnValues("replicateid", ";"));
                        props.setProperty("enteredtext", data.getColumnValues("enteredtext", ";"));
                        props.setProperty("propsmatch", "Y");
                        ap.processAction("EnterDataItem", "1", props);
                    }
                    this.database.closeStatement("getDataItems");
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsWorkItems = sdiData.getDataset("s_qcmethod_workitem");
        if (dsWorkItems != null) {
            for (int r = 0; r < dsWorkItems.getRowCount(); ++r) {
                if (!"C".equals(dsWorkItems.getValue(r, "workitemversionid"))) continue;
                dsWorkItems.setValue(r, "workitemversionid", null);
            }
        }
    }
}

