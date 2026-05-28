/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCCalcBaseAction;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.util.DataItem;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.SDI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCCalcCtrlPRecovery
extends QCCalcBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 67311 $";
    public static final String PARAMETER_TYPE = "%Recovery";
    public static final String PARAMTYPE_CONCENTRATION = "Concentration";
    int rc = 1;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        if (actionid.equals("QCCalcCtrlPRecovery")) {
            this.rc = this.doQCCalcCtrlPRecovery(props);
        }
        return this.rc;
    }

    @Override
    public Boolean hasBracket() {
        return Boolean.FALSE;
    }

    private int doQCCalcCtrlPRecovery(HashMap props) {
        block53: {
            QCBatch qcBatch = null;
            String saveAndRelease = "";
            if (props.get("saveandrelease") != null) {
                saveAndRelease = (String)props.get("saveandrelease");
            }
            String qcbatchid = (String)props.get("qcbatchid");
            String qcbatchitemid = (String)props.get("qcbatchitemid");
            String sdcid = (String)props.get("sdcid");
            String keyid1 = (String)props.get("keyid1");
            String keyid2 = (String)props.get("keyid2");
            String keyid3 = (String)props.get("keyid3");
            String paramlistid = (String)props.get("paramlistid");
            String paramlistversionid = (String)props.get("paramlistversionid");
            String variantid = (String)props.get("variantid");
            String dataset = (String)props.get("dataset");
            String auditreason = (String)props.get("auditreason");
            String auditactivity = (String)props.get("auditactivity");
            String auditsignedflag = (String)props.get("auditsignedflag");
            String[] arrqcbatchid = StringUtil.split(qcbatchid, ";");
            String[] arrqcbatchiemid = StringUtil.split(qcbatchitemid, ";");
            String[] arrsdcid = StringUtil.split(sdcid, ";");
            String[] arrkeyid1 = StringUtil.split(keyid1, ";");
            String[] arrkeyid2 = StringUtil.split(keyid2, ";");
            String[] arrkeyid3 = StringUtil.split(keyid3, ";");
            String[] arrparamlistid = StringUtil.split(paramlistid, ";");
            String[] arrparamlistversionid = StringUtil.split(paramlistversionid, ";");
            String[] arrvariantid = StringUtil.split(variantid, ";");
            String[] arrdataset = StringUtil.split(dataset, ";");
            if (arrqcbatchid == null || arrqcbatchiemid == null || arrsdcid == null || arrkeyid1 == null || arrkeyid2 == null || arrkeyid3 == null || arrparamlistid == null || arrparamlistversionid == null || arrvariantid == null || arrdataset == null) {
                return this.setError("Invalid input properties. ");
            }
            if (arrqcbatchid.length != arrqcbatchiemid.length || arrqcbatchid.length != arrsdcid.length || arrqcbatchid.length != arrkeyid1.length || arrqcbatchid.length != arrkeyid2.length || arrqcbatchid.length != arrkeyid3.length || arrqcbatchid.length != arrparamlistid.length || arrqcbatchid.length != arrparamlistversionid.length || arrqcbatchid.length != arrvariantid.length || arrqcbatchid.length != arrdataset.length) {
                return this.setError("Invalid input: count of values sent in action properties not matching !");
            }
            ActionProcessor ap = this.getActionProcessor();
            HashMap<String, String> actionprops = new HashMap<String, String>();
            StringBuffer addkeyid1 = new StringBuffer();
            StringBuffer addkeyid2 = new StringBuffer();
            StringBuffer addkeyid3 = new StringBuffer();
            StringBuffer addparamlistid = new StringBuffer();
            StringBuffer addparamlistversionid = new StringBuffer();
            StringBuffer addvariantid = new StringBuffer();
            StringBuffer adddataset = new StringBuffer();
            StringBuffer addparamid = new StringBuffer();
            StringBuffer addparamtype = new StringBuffer();
            StringBuffer add_numreplicates = new StringBuffer();
            StringBuffer adddisplayformat = new StringBuffer();
            StringBuffer addreplicatekeyid1 = new StringBuffer();
            StringBuffer addreplicatekeyid2 = new StringBuffer();
            StringBuffer addreplicatekeyid3 = new StringBuffer();
            StringBuffer addreplicateparamlistid = new StringBuffer();
            StringBuffer addreplicateparamlistversionid = new StringBuffer();
            StringBuffer addreplicatevariantid = new StringBuffer();
            StringBuffer addreplicatedataset = new StringBuffer();
            StringBuffer addreplicateparamid = new StringBuffer();
            StringBuffer addreplicateparamtype = new StringBuffer();
            StringBuffer addReplicate_numreplicates = new StringBuffer();
            StringBuffer editkeyid1 = new StringBuffer();
            StringBuffer editkeyid2 = new StringBuffer();
            StringBuffer editkeyid3 = new StringBuffer();
            StringBuffer editparamlistid = new StringBuffer();
            StringBuffer editparamlistversionid = new StringBuffer();
            StringBuffer editvariantid = new StringBuffer();
            StringBuffer editdataset = new StringBuffer();
            StringBuffer editparamid = new StringBuffer();
            StringBuffer editparamtype = new StringBuffer();
            StringBuffer editenteredvalue = new StringBuffer();
            StringBuffer replicateid = new StringBuffer();
            HashMap<String, Integer> add_paramidnumrepmap = new HashMap<String, Integer>();
            HashMap<String, Integer> addreplicate_paramidnumrepmap = new HashMap<String, Integer>();
            StringBuffer numreplicates = new StringBuffer();
            StringBuffer tempAddParamid = new StringBuffer();
            StringBuffer tempReplicateParamid = new StringBuffer();
            ArrayList<SDIDataSet> sdiDatasetHolder = new ArrayList<SDIDataSet>();
            HashMap<String, QCBatch> qcBatchHolder = new HashMap<String, QCBatch>();
            try {
                QueryProcessor qp = this.getQueryProcessor();
                for (int ind = 0; ind < arrqcbatchid.length; ++ind) {
                    qcbatchid = arrqcbatchid[ind];
                    if (qcbatchid != null && qcbatchid.length() > 0) {
                        if (qcBatchHolder.containsKey(qcbatchid)) {
                            qcBatch = (QCBatch)qcBatchHolder.get(qcbatchid);
                        } else {
                            qcBatch = QCBatchPool.getQCBatch(qp, qcbatchid);
                            if (qcBatch != null) {
                                qcBatchHolder.put(qcbatchid, qcBatch);
                            }
                        }
                        if (qcBatch == null) {
                            this.logger.debug("QC Batch does not exists: " + qcbatchid);
                            continue;
                        }
                    }
                    sdcid = arrsdcid[ind];
                    keyid1 = arrkeyid1[ind];
                    keyid2 = arrkeyid2[ind];
                    keyid3 = arrkeyid3[ind];
                    paramlistid = arrparamlistid[ind];
                    paramlistversionid = arrparamlistversionid[ind];
                    variantid = arrvariantid[ind];
                    dataset = arrdataset[ind];
                    if (paramlistid == null || paramlistid.length() == 0 || paramlistversionid == null || paramlistversionid.length() == 0 || variantid == null || variantid.length() == 0 || dataset == null || dataset.length() == 0) {
                        this.logger.debug("Missing mandatory input for " + qcBatch);
                        continue;
                    }
                    qcbatchitemid = arrqcbatchiemid[ind];
                    QCBatchItem qcBatchItem = qcBatch.getQCBatchItem(qcbatchitemid);
                    if (qcBatchItem == null) {
                        this.logger.debug("Invalid QC Batch Item Id :" + qcbatchitemid);
                        continue;
                    }
                    String qcBatchSampleTypeId = qcBatchItem.getQCBatchSampleTypeID();
                    if (qcBatchSampleTypeId == null || qcBatchSampleTypeId.length() == 0) {
                        this.logger.debug("Invalid QC Sample : " + qcbatchitemid);
                        continue;
                    }
                    SDIDataSet qcBatchSampleTypeDataset = new SDIDataSet("QCBatchSampleType", qcBatchSampleTypeId, null, null, "QCParams", "1", "1", "1", qp);
                    SDIDataSet sdidataset = new SDIDataSet(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, qp);
                    ArrayList<DataItem> blankDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> stdDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> calcDataItemList = new ArrayList<DataItem>();
                    List allDataItemList = sdidataset.getDataItems("All");
                    for (int i = 0; i < allDataItemList.size(); ++i) {
                        DataItem dataItem = (DataItem)allDataItemList.get(i);
                        if (dataItem.getParamType().equals("Standard")) {
                            stdDataItemList.add(dataItem);
                            continue;
                        }
                        if (dataItem.getParamType().equals("BlankCorrected")) {
                            blankDataItemList.add(dataItem);
                            continue;
                        }
                        if (!dataItem.getParamType().equals(this.getQCParameterType())) continue;
                        calcDataItemList.add(dataItem);
                    }
                    List concDataItemList = qcBatchSampleTypeDataset.getDataItems(PARAMTYPE_CONCENTRATION);
                    if (stdDataItemList == null || stdDataItemList.size() == 0) {
                        this.logger.debug("No dataitem available for QcBatchItem : " + qcbatchitemid);
                        continue;
                    }
                    if (concDataItemList == null || concDataItemList.size() == 0) {
                        this.logger.debug("No dataitem available with parameter type \"Concentration\" for the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    for (int i = 0; i < stdDataItemList.size(); ++i) {
                        DataItem dataItem = (DataItem)stdDataItemList.get(i);
                        String enteredData = dataItem.getTransformValue();
                        if (blankDataItemList != null) {
                            for (int k = 0; k < blankDataItemList.size(); ++k) {
                                DataItem blankDataItem = (DataItem)blankDataItemList.get(k);
                                if (!blankDataItem.getParamID().equals(dataItem.getParamID()) || !blankDataItem.getReplicate().equals(dataItem.getReplicate())) continue;
                                if (blankDataItem.getTransformValue() == null || blankDataItem.getTransformValue().length() <= 0) break;
                                enteredData = blankDataItem.getTransformValue();
                                break;
                            }
                        }
                        if (enteredData == null || enteredData.length() <= 0) continue;
                        String concEnteredData = "";
                        for (int k = 0; k < concDataItemList.size(); ++k) {
                            DataItem concDataItem = (DataItem)concDataItemList.get(k);
                            if (!concDataItem.getParamID().equals(dataItem.getParamID())) continue;
                            concEnteredData = concDataItem.getTransformValue();
                            break;
                        }
                        if (concEnteredData == null || concEnteredData.length() <= 0) continue;
                        double R = Double.parseDouble(enteredData);
                        double T = Double.parseDouble(concEnteredData);
                        double P = 100.0 * (R / T);
                        if (add_paramidnumrepmap.containsKey(dataItem.getParamID())) {
                            int replicatecnt = 1;
                            Integer numrep = (Integer)add_paramidnumrepmap.get(dataItem.getParamID());
                            try {
                                replicatecnt = new Integer(dataItem.getReplicate()) - numrep;
                            }
                            catch (NumberFormatException nmt) {
                                this.logger.debug(" Invalid replicate found for dataitem :" + dataItem);
                            }
                            numrep = new Integer(numrep + replicatecnt);
                            add_paramidnumrepmap.put(dataItem.getParamID(), numrep);
                        } else if (addreplicate_paramidnumrepmap.containsKey(dataItem.getParamID())) {
                            Integer numrep = (Integer)addreplicate_paramidnumrepmap.get(dataItem.getParamID());
                            numrep = new Integer(numrep + 1);
                            addreplicate_paramidnumrepmap.put(dataItem.getParamID(), numrep);
                        } else {
                            boolean paramfound = false;
                            boolean paramreplicatefound = false;
                            String replicatefound = "";
                            for (int j = 0; j < calcDataItemList.size(); ++j) {
                                DataItem calcDataItem = (DataItem)calcDataItemList.get(j);
                                if (calcDataItem.getParamID().equals(dataItem.getParamID()) && calcDataItem.getReplicate().equals(dataItem.getReplicate())) {
                                    paramfound = true;
                                    paramreplicatefound = true;
                                    break;
                                }
                                if (!calcDataItem.getParamID().equals(dataItem.getParamID())) continue;
                                paramfound = true;
                                replicatefound = calcDataItem.getReplicate();
                            }
                            if (!paramfound) {
                                addkeyid1.append(keyid1 + "~");
                                addkeyid2.append(keyid2 + "~");
                                addkeyid3.append(keyid3 + "~");
                                addparamlistid.append(paramlistid + "~");
                                addparamlistversionid.append(paramlistversionid + "~");
                                addvariantid.append(variantid + "~");
                                adddataset.append(dataset + "~");
                                addparamid.append(dataItem.getParamID() + "~");
                                tempAddParamid.append(dataItem.getParamID() + ";");
                                String dispformat = dataItem.getDisplayFormat();
                                if (dispformat == null) {
                                    dispformat = "";
                                }
                                adddisplayformat.append(dispformat + "~");
                                addparamtype.append(this.getQCParameterType() + "~");
                                add_paramidnumrepmap.put(dataItem.getParamID(), new Integer(dataItem.getReplicate()));
                                sdiDatasetHolder.add(sdidataset);
                            } else if (!paramreplicatefound) {
                                int addreplicatecnt = 1;
                                String currentreplicate = dataItem.getReplicate();
                                try {
                                    addreplicatecnt = Integer.parseInt(currentreplicate) - Integer.parseInt(replicatefound);
                                }
                                catch (NumberFormatException nmt) {
                                    this.logger.debug(" Invalid replicate found for dataitem :" + dataItem);
                                }
                                addreplicatekeyid1.append(keyid1 + ";");
                                addreplicatekeyid2.append(keyid2 + ";");
                                addreplicatekeyid3.append(keyid3 + ";");
                                addreplicateparamlistid.append(paramlistid + ";");
                                addreplicateparamlistversionid.append(paramlistversionid + ";");
                                addreplicatevariantid.append(variantid + ";");
                                addreplicatedataset.append(dataset + ";");
                                addreplicateparamid.append(dataItem.getParamID() + ";");
                                tempReplicateParamid.append(dataItem.getParamID() + ";");
                                addreplicateparamtype.append(this.getQCParameterType() + ";");
                                addreplicate_paramidnumrepmap.put(dataItem.getParamID(), new Integer(addreplicatecnt));
                                sdiDatasetHolder.add(sdidataset);
                            }
                        }
                        editkeyid1.append(keyid1 + ";");
                        editkeyid2.append(keyid2 + ";");
                        editkeyid3.append(keyid3 + ";");
                        editparamlistid.append(paramlistid + ";");
                        editparamlistversionid.append(paramlistversionid + ";");
                        editvariantid.append(variantid + ";");
                        editdataset.append(dataset + ";");
                        editparamid.append(dataItem.getParamID() + ";");
                        editparamtype.append(this.getQCParameterType() + ";");
                        editenteredvalue.append(P + ";");
                        replicateid.append(dataItem.getReplicate() + ";");
                    }
                    if (tempAddParamid.length() > 0) {
                        tempAddParamid.setLength(tempAddParamid.length() - 1);
                        String[] paramids = StringUtil.split(tempAddParamid.toString(), ";");
                        numreplicates.setLength(0);
                        for (int l = 0; l < paramids.length; ++l) {
                            numreplicates.append(add_paramidnumrepmap.get(paramids[l]) + "~");
                        }
                        add_numreplicates.append(numreplicates);
                    }
                    if (tempReplicateParamid.length() > 0) {
                        tempReplicateParamid.setLength(tempReplicateParamid.length() - 1);
                        String[] paramids = StringUtil.split(tempReplicateParamid.toString(), ";");
                        numreplicates.setLength(0);
                        for (int l = 0; l < paramids.length; ++l) {
                            numreplicates.append(addreplicate_paramidnumrepmap.get(paramids[l]) + ";");
                        }
                        addReplicate_numreplicates.append(numreplicates);
                    }
                    add_paramidnumrepmap.clear();
                    addreplicate_paramidnumrepmap.clear();
                    tempAddParamid.setLength(0);
                    tempReplicateParamid.setLength(0);
                }
                actionprops.clear();
                actionprops.put("sdcid", arrsdcid[0]);
                actionprops.put("auditreason", auditreason);
                actionprops.put("auditactivity", auditactivity);
                actionprops.put("auditsignedflag", auditsignedflag == null ? "N" : auditsignedflag);
                if (addparamid.length() > 0) {
                    addkeyid1.setLength(addkeyid1.length() - 1);
                    addkeyid2.setLength(addkeyid2.length() - 1);
                    addkeyid3.setLength(addkeyid3.length() - 1);
                    addparamlistid.setLength(addparamlistid.length() - 1);
                    addparamlistversionid.setLength(addparamlistversionid.length() - 1);
                    addvariantid.setLength(addvariantid.length() - 1);
                    adddataset.setLength(adddataset.length() - 1);
                    addparamid.setLength(addparamid.length() - 1);
                    addparamtype.setLength(addparamtype.length() - 1);
                    add_numreplicates.setLength(add_numreplicates.length() - 1);
                    adddisplayformat.setLength(adddisplayformat.length() - 1);
                    actionprops.put("keyid1", addkeyid1.toString());
                    actionprops.put("keyid2", addkeyid2.toString());
                    actionprops.put("keyid3", addkeyid3.toString());
                    actionprops.put("paramlistid", addparamlistid.toString());
                    actionprops.put("paramlistversionid", addparamlistversionid.toString());
                    actionprops.put("variantid", addvariantid.toString());
                    actionprops.put("dataset", adddataset.toString());
                    actionprops.put("paramid", addparamid.toString());
                    actionprops.put("paramtype", addparamtype.toString());
                    actionprops.put("numreplicate", add_numreplicates.toString());
                    actionprops.put("displayformat", adddisplayformat.toString());
                    actionprops.put("datatypes", "NC");
                    actionprops.put("propsmatch", "Y");
                    actionprops.put("paramlistcheck", "N");
                    actionprops.put("delimeter", "~");
                    ap.processAction("ExtendDataSet", "1", actionprops);
                }
                if (addreplicateparamid.length() > 0) {
                    addreplicatekeyid1.setLength(addreplicatekeyid1.length() - 1);
                    addreplicatekeyid2.setLength(addreplicatekeyid2.length() - 1);
                    addreplicatekeyid3.setLength(addreplicatekeyid3.length() - 1);
                    addreplicateparamlistid.setLength(addreplicateparamlistid.length() - 1);
                    addreplicateparamlistversionid.setLength(addreplicateparamlistversionid.length() - 1);
                    addreplicatevariantid.setLength(addreplicatevariantid.length() - 1);
                    addreplicatedataset.setLength(addreplicatedataset.length() - 1);
                    addreplicateparamid.setLength(addreplicateparamid.length() - 1);
                    addreplicateparamtype.setLength(addreplicateparamtype.length() - 1);
                    addReplicate_numreplicates.setLength(addReplicate_numreplicates.length() - 1);
                    actionprops.put("keyid1", addreplicatekeyid1.toString());
                    actionprops.put("keyid2", addreplicatekeyid2.toString());
                    actionprops.put("keyid3", addreplicatekeyid3.toString());
                    actionprops.put("paramlistid", addreplicateparamlistid.toString());
                    actionprops.put("paramlistversionid", addreplicateparamlistversionid.toString());
                    actionprops.put("variantid", addreplicatevariantid.toString());
                    actionprops.put("dataset", addreplicatedataset.toString());
                    actionprops.put("paramid", addreplicateparamid.toString());
                    actionprops.put("paramtype", addreplicateparamtype.toString());
                    actionprops.put("numreplicate", addReplicate_numreplicates.toString());
                    actionprops.put("propsmatch", "Y");
                    ap.processAction("AddReplicate", "1", actionprops);
                }
                if (editparamid.length() > 0) {
                    editkeyid1.setLength(editkeyid1.length() - 1);
                    editkeyid2.setLength(editkeyid2.length() - 1);
                    editkeyid3.setLength(editkeyid3.length() - 1);
                    editparamlistid.setLength(editparamlistid.length() - 1);
                    editparamlistversionid.setLength(editparamlistversionid.length() - 1);
                    editvariantid.setLength(editvariantid.length() - 1);
                    editdataset.setLength(editdataset.length() - 1);
                    editparamid.setLength(editparamid.length() - 1);
                    editparamtype.setLength(editparamtype.length() - 1);
                    editenteredvalue.setLength(editenteredvalue.length() - 1);
                    replicateid.setLength(replicateid.length() - 1);
                    actionprops.put("keyid1", editkeyid1.toString());
                    actionprops.put("keyid2", editkeyid2.toString());
                    actionprops.put("keyid3", editkeyid3.toString());
                    actionprops.put("paramlistid", editparamlistid.toString());
                    actionprops.put("paramlistversionid", editparamlistversionid.toString());
                    actionprops.put("variantid", editvariantid.toString());
                    actionprops.put("dataset", editdataset.toString());
                    actionprops.put("paramid", editparamid.toString());
                    actionprops.put("paramtype", editparamtype.toString());
                    actionprops.put("replicateid", replicateid.toString());
                    actionprops.put("enteredtext", editenteredvalue.toString());
                    ap.processAction("EnterDataItem", "1", actionprops);
                    if (saveAndRelease != null && saveAndRelease.equalsIgnoreCase("Y")) {
                        PropertyList releaseDataItemPl = new PropertyList();
                        releaseDataItemPl.setProperty("sdcid", arrsdcid[0]);
                        releaseDataItemPl.setProperty("propsmatch", "Y");
                        releaseDataItemPl.setProperty("keyid1", editkeyid1.toString());
                        releaseDataItemPl.setProperty("keyid2", editkeyid2.toString());
                        releaseDataItemPl.setProperty("keyid3", editkeyid3.toString());
                        releaseDataItemPl.setProperty("paramlistid", editparamlistid.toString());
                        releaseDataItemPl.setProperty("paramlistversionid", editparamlistversionid.toString());
                        releaseDataItemPl.setProperty("variantid", editvariantid.toString());
                        releaseDataItemPl.setProperty("dataset", editdataset.toString());
                        releaseDataItemPl.setProperty("paramid", editparamid.toString());
                        releaseDataItemPl.setProperty("paramtype", editparamtype.toString());
                        releaseDataItemPl.setProperty("replicateid", replicateid.toString());
                        releaseDataItemPl.setProperty("auditreason", auditreason);
                        releaseDataItemPl.setProperty("auditactivity", auditactivity);
                        releaseDataItemPl.setProperty("auditsignedflag", auditsignedflag == null || auditsignedflag.equals("") ? "N" : auditsignedflag);
                        this.getActionProcessor().processAction("ReleaseDataItem", "1", releaseDataItemPl);
                    }
                }
                if (sdiDatasetHolder.size() <= 0) break block53;
                StringBuffer sbsdcid = new StringBuffer();
                StringBuffer sbkeyid1 = new StringBuffer();
                StringBuffer sbkeyid2 = new StringBuffer();
                StringBuffer sbkeyid3 = new StringBuffer();
                StringBuffer sbparamlistid = new StringBuffer();
                StringBuffer sbparamlistversionid = new StringBuffer();
                StringBuffer sbvariantid = new StringBuffer();
                StringBuffer sbdataset = new StringBuffer();
                StringBuffer sbparamid = new StringBuffer();
                StringBuffer sbparamtype = new StringBuffer();
                StringBuffer sbreplicateid = new StringBuffer();
                StringBuffer sbuseq = new StringBuffer();
                for (int k = 0; k < sdiDatasetHolder.size(); ++k) {
                    SDIDataSet sdiDataset = (SDIDataSet)sdiDatasetHolder.get(k);
                    SDI sdi = sdiDataset.getSDI();
                    int maxseq = 0;
                    int seq = 0;
                    List dataitemList = sdiDataset.getDataItems("All");
                    for (int i = 0; i < dataitemList.size(); ++i) {
                        DataItem dataitem = (DataItem)dataitemList.get(i);
                        String paramtype = dataitem.getParamType();
                        if (paramtype == null || !paramtype.equalsIgnoreCase("Standard") && !paramtype.equalsIgnoreCase("Average")) continue;
                        try {
                            seq = Integer.parseInt(dataitem.getUserSequence());
                        }
                        catch (Exception exc) {
                            this.logger.error("doQCCalcCtrlPRecovery error", exc);
                        }
                        if (seq <= maxseq) continue;
                        maxseq = seq;
                    }
                    String prevparamtype = "";
                    String prevparamid = "";
                    for (int i = 0; i < dataitemList.size(); ++i) {
                        DataItem dataitem = (DataItem)dataitemList.get(i);
                        String paramtype = dataitem.getParamType();
                        if (paramtype != null && !paramtype.equalsIgnoreCase("Standard") && !paramtype.equalsIgnoreCase("Average")) {
                            String paramid = dataitem.getParamID();
                            if (!paramtype.equals(prevparamtype) || !paramid.equals(prevparamid)) {
                                ++maxseq;
                            }
                            prevparamtype = paramtype;
                            prevparamid = paramid;
                            sbsdcid.append(sdi.getSdcid() + ";");
                            sbkeyid1.append(sdi.getKeyid1() + ";");
                            sbkeyid2.append(sdi.getKeyid2() + ";");
                            sbkeyid3.append(sdi.getKeyid3() + ";");
                            sbparamlistid.append(sdiDataset.getParamListID() + ";");
                            sbparamlistversionid.append(sdiDataset.getParamListVersionID() + ";");
                            sbvariantid.append(sdiDataset.getVariantID() + ";");
                            sbdataset.append(sdiDataset.getDataSet() + ";");
                            sbparamid.append(paramid + ";");
                            sbparamtype.append(paramtype + ";");
                            sbreplicateid.append(dataitem.getReplicate() + ";");
                            sbuseq.append(maxseq + ";");
                            continue;
                        }
                        paramtype = "";
                    }
                }
                if (sbsdcid.length() > 0) {
                    DataSet dsupdate = new DataSet();
                    sbsdcid.setLength(sbsdcid.length() - 1);
                    sbkeyid1.setLength(sbkeyid1.length() - 1);
                    sbkeyid2.setLength(sbkeyid2.length() - 1);
                    sbkeyid3.setLength(sbkeyid3.length() - 1);
                    sbparamlistid.setLength(sbparamlistid.length() - 1);
                    sbparamlistversionid.setLength(sbparamlistversionid.length() - 1);
                    sbvariantid.setLength(sbvariantid.length() - 1);
                    sbdataset.setLength(sbdataset.length() - 1);
                    sbparamid.setLength(sbparamid.length() - 1);
                    sbparamtype.setLength(sbparamtype.length() - 1);
                    sbreplicateid.setLength(sbreplicateid.length() - 1);
                    sbuseq.setLength(sbuseq.length() - 1);
                    dsupdate.addColumnValues("sdcid", 0, sbsdcid.toString(), ";");
                    dsupdate.addColumnValues("keyid1", 0, sbkeyid1.toString(), ";");
                    dsupdate.addColumnValues("keyid2", 0, sbkeyid2.toString(), ";");
                    dsupdate.addColumnValues("keyid3", 0, sbkeyid3.toString(), ";");
                    dsupdate.addColumnValues("paramlistid", 0, sbparamlistid.toString(), ";");
                    dsupdate.addColumnValues("paramlistversionid", 0, sbparamlistversionid.toString(), ";");
                    dsupdate.addColumnValues("variantid", 0, sbvariantid.toString(), ";");
                    dsupdate.addColumnValues("dataset", 0, sbdataset.toString(), ";");
                    dsupdate.addColumnValues("paramid", 0, sbparamid.toString(), ";");
                    dsupdate.addColumnValues("paramtype", 0, sbparamtype.toString(), ";");
                    dsupdate.addColumnValues("replicateid", 0, sbreplicateid.toString(), ";");
                    dsupdate.addColumnValues("usersequence", 0, sbuseq.toString(), ";");
                    String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                    DataSetUtil.update(this.database, dsupdate, "sdidataitem", keycols);
                }
            }
            catch (Exception ex) {
                this.logger.error("doQCCalcCtrlPRecovery error", ex);
            }
        }
        return this.rc;
    }

    @Override
    public String getQCParameterType() {
        return PARAMETER_TYPE;
    }

    @Override
    public DataSet addQCCalcDataItems(String qcBatchId, String bstId, String actionId, QueryProcessor qp) {
        DataSet qcBatchDetails = QCUtil.getQCBatchItems(bstId, qp);
        String calcRule = "100*([#;CURRENTITEM;#]/[AQC:QCParam|#;Concentration])";
        DataSet calcDataItems = new DataSet();
        if (qcBatchDetails != null && qcBatchDetails.getRowCount() > 0) {
            QCBatch qcBatch = QCBatchPool.getQCBatch(qp, qcBatchId);
            String qcBatchItemIds = "'" + qcBatchDetails.getColumnValues("s_qcbatchitemid", "','") + "'";
            calcDataItems = QCUtil.getCalcDataItems(qp, qcBatchId, qcBatchItemIds, PARAMETER_TYPE);
            if (calcDataItems != null && calcDataItems.getRowCount() > 0) {
                String[] batchItemIds = StringUtil.split(qcBatchDetails.getColumnValues("s_qcbatchitemid", ";"), ";");
                QCUtil.setCalcRule(calcDataItems, calcRule, qcBatch, batchItemIds);
            }
        }
        return calcDataItems;
    }

    public boolean isCalcRuleAdded(String qcBatchId, String qcBatchItemId, QueryProcessor qp, ConnectionProcessor cp) {
        DataSet ds = QCUtil.getDataItemsWithCalcRuleDefined(qcBatchId, "'" + qcBatchItemId + "'", PARAMETER_TYPE, qp, cp.isOra());
        return ds != null && ds.getRowCount() > 0;
    }
}

