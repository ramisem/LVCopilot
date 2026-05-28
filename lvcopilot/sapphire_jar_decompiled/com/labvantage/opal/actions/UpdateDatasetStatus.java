/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.util.DataSet2;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SDIDataSet2;
import com.labvantage.sapphire.actions.sdidata.EditDataSet;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UpdateDatasetStatus
extends BaseAction
implements sapphire.action.UpdateDatasetStatus {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String auditreason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag", "N");
        if (OpalUtil.isSapphireNull(sdcid) || OpalUtil.isSapphireNull(keyid1)) {
            throw new SapphireException("[UpdateDataSetStatus] Invalid action input(s).");
        }
        keyid1 = OpalUtil.getUniqueValues(keyid1, ";");
        SDIDataSet2 dataSet2 = new SDIDataSet2(this.connectionInfo);
        dataSet2.setSdcId(sdcid);
        dataSet2.setKeyid1(keyid1);
        dataSet2.setKeyid2(keyid2);
        dataSet2.setKeyid3(keyid3);
        PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
        boolean enterOptionalToBeReleased = "Y".equals(policy.getProperty("enteredoptionalresultrequirerelease", "Y"));
        dataSet2.setEnteredOptionToBeReleased(enterOptionalToBeReleased);
        List<DataSet2> list = dataSet2.evaluate();
        if (list.size() > 0) {
            DataSet ds = new DataSet();
            ds.addColumn("keyid1", 0);
            ds.addColumn("keyid2", 0);
            ds.addColumn("keyid3", 0);
            ds.addColumn("paramlistid", 0);
            ds.addColumn("paramlistversionid", 0);
            ds.addColumn("variantid", 0);
            ds.addColumn("dataset", 0);
            ds.addColumn("s_datasetstatus", 0);
            for (DataSet2 dataset : list) {
                int row = ds.addRow();
                ds.setValue(row, "keyid1", dataset.getKeyId1());
                ds.setValue(row, "keyid2", dataset.getKeyId2());
                ds.setValue(row, "keyid3", dataset.getKeyId3());
                ds.setValue(row, "paramlistid", dataset.getParamListID());
                ds.setValue(row, "paramlistversionid", dataset.getParamListVersionID());
                ds.setValue(row, "variantid", dataset.getVariantID());
                ds.setValue(row, "dataset", dataset.getDataset());
                ds.setValue(row, "s_datasetstatus", dataset.getStatus());
            }
            ds.sort("s_datasetstatus");
            ArrayList<DataSet> dslist = ds.getGroupedDataSets("s_datasetstatus");
            for (DataSet dataset : dslist) {
                if (dataset.size() <= 0) continue;
                PropertyList props = new PropertyList();
                String datasetstatus = dataset.getString(0, "s_datasetstatus");
                if ("Completed".equals(datasetstatus)) {
                    dataset.setString(-1, "completedby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                    dataset.setString(-1, "completeddt", "n");
                    props.setProperty("completedby", dataset.getColumnValues("completedby", ";"));
                    props.setProperty("completeddt", dataset.getColumnValues("completeddt", ";"));
                } else if ("Cancelled".equals(datasetstatus)) {
                    dataset.setString(-1, "cancelledby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                    dataset.setString(-1, "cancelleddt", "n");
                    props.setProperty("cancelledby", dataset.getColumnValues("cancelledby", ";"));
                    props.setProperty("cancelleddt", dataset.getColumnValues("cancelleddt", ";"));
                }
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dataset.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", dataset.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", dataset.getColumnValues("keyid3", ";"));
                props.setProperty("paramlistid", dataset.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", dataset.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", dataset.getColumnValues("variantid", ";"));
                props.setProperty("dataset", dataset.getColumnValues("dataset", ";"));
                props.setProperty("s_datasetstatus", dataset.getColumnValues("s_datasetstatus", ";"));
                props.setProperty("propsmatch", "Y");
                if (StringUtil.getLen(auditreason) > 0L) {
                    props.setProperty("auditreason", auditreason);
                    props.setProperty("auditactivity", auditActivity);
                    props.setProperty("auditsignedflag", auditSignedFlag);
                }
                this.getActionProcessor().processActionClass(EditDataSet.class.getName(), props);
            }
        }
        properties.setProperty("sdistatus", "");
    }
}

