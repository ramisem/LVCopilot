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

public class QCCalcSpike
extends QCCalcBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 67311 $";
    public static final String PARAMETER_TYPE = "ExpectedRecovery;%Recovery";
    int rc = 1;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        if (actionid.equals("QCCalcSpike")) {
            return this.rc;
        }
        if (actionid.equals("QCCalcSpikePRecovery")) {
            this.rc = this.doQCCalcSpike(props, actionid);
        } else if (actionid.equals("QCCalcSpikeIUPACMeth")) {
            this.rc = this.doQCCalcSpike(props, actionid);
        } else if (actionid.equals("QCCalcSpikeConcMeth")) {
            this.rc = this.doQCCalcSpike(props, actionid);
        }
        return this.rc;
    }

    @Override
    public Boolean hasBracket() {
        return Boolean.FALSE;
    }

    private int doQCCalcSpike(HashMap props, String actionId) {
        block84: {
            String saveAndRelease = "";
            if (props.get("saveandrelease") != null) {
                saveAndRelease = (String)props.get("saveandrelease");
            }
            QCBatch qcBatch = null;
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
            HashMap<String, Double> linkEnterDataAverageMap = new HashMap<String, Double>();
            HashMap<String, Integer> linkEnterDataReplicateMap = new HashMap<String, Integer>();
            HashMap<String, Integer> add_paramidnumrepmap = new HashMap<String, Integer>();
            HashMap<String, Integer> addreplicate_paramidnumrepmap = new HashMap<String, Integer>();
            StringBuffer numreplicates = new StringBuffer();
            StringBuffer tempAddParamid = new StringBuffer();
            StringBuffer tempReplicateParamid = new StringBuffer();
            StringBuffer tempAddParamType = new StringBuffer();
            StringBuffer tempReplicateParamType = new StringBuffer();
            ArrayList<SDIDataSet> sdiDatasetHolder = new ArrayList<SDIDataSet>();
            HashMap<String, QCBatch> qcBatchHolder = new HashMap<String, QCBatch>();
            String[] paramTypes = StringUtil.split(this.getQCParameterType(), ";");
            try {
                QueryProcessor qp = this.getQueryProcessor();
                for (int ind = 0; ind < arrqcbatchid.length; ++ind) {
                    String[] paramtypes;
                    int i;
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
                    String linktobatchitemid = "";
                    SDIDataSet linksdidataset = null;
                    linktobatchitemid = qcBatchItem.getLinkedToBatchItemID();
                    if (linktobatchitemid == null || linktobatchitemid.length() == 0) {
                        this.logger.debug("Spike QcBatchItem " + qcBatchItem + " is not linked with any QcBatchItem .");
                        continue;
                    }
                    String qcBatchSampleTypeId = qcBatchItem.getQCBatchSampleTypeID();
                    if (qcBatchSampleTypeId == null || qcBatchSampleTypeId.length() == 0) {
                        this.logger.debug("Invalid QC Sample : " + qcbatchitemid);
                        continue;
                    }
                    SDIDataSet qcBatchSampleTypeDataset = new SDIDataSet("QCBatchSampleType", qcBatchSampleTypeId, null, null, "QCParams", "1", "1", "1", qp);
                    List allBSTDDataItemList = qcBatchSampleTypeDataset.getDataItems("All");
                    ArrayList<DataItem> concBSTDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> stdBSTDataItemList = new ArrayList<DataItem>();
                    for (int i2 = 0; i2 < allBSTDDataItemList.size(); ++i2) {
                        DataItem dataItem = (DataItem)allBSTDDataItemList.get(i2);
                        if (dataItem.getParamType().equals("Concentration")) {
                            concBSTDataItemList.add(dataItem);
                            continue;
                        }
                        if (!dataItem.getParamType().equals("Standard")) continue;
                        stdBSTDataItemList.add(dataItem);
                    }
                    if (concBSTDataItemList == null || concBSTDataItemList.size() == 0) {
                        this.logger.debug(" No dataitem available with parameter type \"Concentration\" for the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    if (stdBSTDataItemList == null || stdBSTDataItemList.size() == 0) {
                        this.logger.debug(" No dataitem available with parameter type \"Standard\" for the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    QCBatchItem linktoqcbatchitem = new QCBatchItem(this.getQueryProcessor(), qcbatchid, linktobatchitemid);
                    linksdidataset = linktoqcbatchitem.getSDIDataSet(paramlistid, paramlistversionid, variantid, null);
                    if (linksdidataset == null) {
                        this.logger.debug("Source dataset not available for the QcBatchItem linked to QcBatchItem : " + qcBatchItem);
                        continue;
                    }
                    ArrayList<DataItem> blanklinkdataitemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> stdlinkdataitemList = new ArrayList<DataItem>();
                    List allLinkDataItemList = linksdidataset.getDataItems("All");
                    for (int i3 = 0; i3 < allLinkDataItemList.size(); ++i3) {
                        DataItem dataItem = (DataItem)allLinkDataItemList.get(i3);
                        if (dataItem.getParamType().equals("Standard")) {
                            stdlinkdataitemList.add(dataItem);
                            continue;
                        }
                        if (!dataItem.getParamType().equals("BlankCorrected")) continue;
                        blanklinkdataitemList.add(dataItem);
                    }
                    if (stdlinkdataitemList == null || stdlinkdataitemList.size() == 0) {
                        this.logger.debug(" No dataitem available for the QcBatchItem linked to QcBatchItem : " + qcBatchItem);
                        continue;
                    }
                    SDIDataSet sdidataset = new SDIDataSet(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, qp);
                    ArrayList<DataItem> blankDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> stdDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> calcPRecoveryDataItemList = new ArrayList<DataItem>();
                    ArrayList<DataItem> calcERecoveryDataItemList = new ArrayList<DataItem>();
                    List allDataItemList = sdidataset.getDataItems("All");
                    for (i = 0; i < allDataItemList.size(); ++i) {
                        DataItem dataItem = (DataItem)allDataItemList.get(i);
                        if (dataItem.getParamType().equals("Standard")) {
                            stdDataItemList.add(dataItem);
                            continue;
                        }
                        if (dataItem.getParamType().equals("BlankCorrected")) {
                            blankDataItemList.add(dataItem);
                            continue;
                        }
                        if (dataItem.getParamType().equals(paramTypes[1])) {
                            calcPRecoveryDataItemList.add(dataItem);
                            continue;
                        }
                        if (!dataItem.getParamType().equals(paramTypes[0])) continue;
                        calcERecoveryDataItemList.add(dataItem);
                    }
                    if (stdDataItemList == null || stdDataItemList.size() == 0) {
                        this.logger.debug(" No dataitem available for QcBatchItem : " + qcBatchItem);
                        continue;
                    }
                    for (i = 0; i < stdlinkdataitemList.size(); ++i) {
                        DataItem linkdataItem = (DataItem)stdlinkdataitemList.get(i);
                        String enteredData = linkdataItem.getTransformValue();
                        if (blanklinkdataitemList != null) {
                            for (int x = 0; x < blanklinkdataitemList.size(); ++x) {
                                DataItem blanklinkdataItem = (DataItem)blanklinkdataitemList.get(x);
                                if (!blanklinkdataItem.getParamID().equals(linkdataItem.getParamID()) || !blanklinkdataItem.getReplicate().equals(linkdataItem.getReplicate())) continue;
                                if (blanklinkdataItem.getTransformValue() == null || blanklinkdataItem.getTransformValue().length() <= 0) break;
                                enteredData = blanklinkdataItem.getTransformValue();
                                break;
                            }
                        }
                        if (enteredData == null || enteredData.length() <= 0) continue;
                        double data = Double.parseDouble(enteredData);
                        if (!linkEnterDataAverageMap.containsKey(linkdataItem.getParamID())) {
                            linkEnterDataAverageMap.put(linkdataItem.getParamID(), new Double(data));
                            linkEnterDataReplicateMap.put(linkdataItem.getParamID(), new Integer(1));
                            continue;
                        }
                        double mapdata = (Double)linkEnterDataAverageMap.get(linkdataItem.getParamID());
                        int mapreplicatecount = (Integer)linkEnterDataReplicateMap.get(linkdataItem.getParamID());
                        linkEnterDataAverageMap.put(linkdataItem.getParamID(), new Double(data += mapdata));
                        linkEnterDataReplicateMap.put(linkdataItem.getParamID(), new Integer(++mapreplicatecount));
                    }
                    String spikeVolume = "";
                    String sampleVolume = "";
                    double v = 0.0;
                    double V = 0.0;
                    int volcount = 0;
                    for (int j = 0; j < stdBSTDataItemList.size(); ++j) {
                        DataItem bstDataItem = (DataItem)stdBSTDataItemList.get(j);
                        if (bstDataItem.getParamID().equalsIgnoreCase("SpikeVolume")) {
                            spikeVolume = bstDataItem.getTransformValue();
                            ++volcount;
                        }
                        if (bstDataItem.getParamID().equalsIgnoreCase("SampleVolume")) {
                            sampleVolume = bstDataItem.getTransformValue();
                            ++volcount;
                        }
                        if (volcount == 2) break;
                    }
                    if (volcount == 0) {
                        this.logger.debug(" SpikeVolume , SampleVolume not specified for the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    try {
                        v = Double.parseDouble(spikeVolume);
                    }
                    catch (NumberFormatException ne) {
                        this.logger.debug(" Invalid SpikeVolume in the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    try {
                        V = Double.parseDouble(sampleVolume);
                    }
                    catch (NumberFormatException ne) {
                        this.logger.debug(" Invalid SampleVolume in the QCBatchSampleType :" + qcBatchSampleTypeId);
                        continue;
                    }
                    HashMap<String, String> concParamMap = new HashMap<String, String>();
                    for (int j = 0; j < concBSTDataItemList.size(); ++j) {
                        DataItem bstDataItem = (DataItem)concBSTDataItemList.get(j);
                        if (bstDataItem.getTransformValue() == null || bstDataItem.getTransformValue().length() <= 0) continue;
                        try {
                            Double.parseDouble(bstDataItem.getTransformValue());
                            concParamMap.put(bstDataItem.getParamID(), bstDataItem.getEnteredValue());
                            continue;
                        }
                        catch (NumberFormatException ne) {
                            this.logger.debug(" Invalid concentration exists for the param id :" + bstDataItem.getParamID() + " in the QCBatchSampleType :" + qcBatchSampleTypeId);
                        }
                    }
                    ArrayList<String> expectedRecoveryParamList = new ArrayList<String>();
                    for (int i4 = 0; i4 < stdDataItemList.size(); ++i4) {
                        double PR;
                        double x;
                        double E;
                        DataItem dataItem = (DataItem)stdDataItemList.get(i4);
                        String enteredData = dataItem.getTransformValue();
                        if (blankDataItemList != null) {
                            for (int x2 = 0; x2 < blankDataItemList.size(); ++x2) {
                                DataItem blankdataItem = (DataItem)blankDataItemList.get(x2);
                                if (!blankdataItem.getParamID().equals(dataItem.getParamID()) || !blankdataItem.getReplicate().equals(dataItem.getReplicate())) continue;
                                if (blankdataItem.getTransformValue() == null || blankdataItem.getTransformValue().length() <= 0) break;
                                enteredData = blankdataItem.getTransformValue();
                                break;
                            }
                        }
                        if (enteredData == null || enteredData.length() <= 0 || !linkEnterDataAverageMap.containsKey(dataItem.getParamID()) || !concParamMap.containsKey(dataItem.getParamID())) continue;
                        double mapdata = (Double)linkEnterDataAverageMap.get(dataItem.getParamID());
                        int replicatecount = (Integer)linkEnterDataReplicateMap.get(dataItem.getParamID());
                        double U = mapdata / (double)replicatecount;
                        double C = Double.parseDouble((String)concParamMap.get(dataItem.getParamID()));
                        double SPV = Double.parseDouble(enteredData);
                        if (actionId.equals("QCCalcSpikeIUPACMeth")) {
                            E = v * C / (V + v);
                            x = SPV - U * V / (V + v);
                            PR = 100.0 * (x / E);
                        } else if (actionId.equals("QCCalcSpikeConcMeth")) {
                            E = v * C / (V + v);
                            x = SPV - U;
                            PR = 100.0 * (x / E);
                        } else {
                            E = v * (C - U) / (V + v);
                            x = SPV - U;
                            PR = 100.0 * (x / E);
                        }
                        if (add_paramidnumrepmap.containsKey(dataItem.getParamID() + paramTypes[1])) {
                            int replicatecnt = 1;
                            Integer numrep = (Integer)add_paramidnumrepmap.get(dataItem.getParamID() + paramTypes[1]);
                            try {
                                replicatecnt = new Integer(dataItem.getReplicate()) - numrep;
                            }
                            catch (NumberFormatException ne) {
                                this.logger.debug(" Invalid replicate found for dataitem :" + dataItem);
                            }
                            numrep = new Integer(numrep + replicatecnt);
                            add_paramidnumrepmap.put(dataItem.getParamID() + paramTypes[1], numrep);
                        } else if (addreplicate_paramidnumrepmap.containsKey(dataItem.getParamID() + paramTypes[1])) {
                            Integer numrep = (Integer)addreplicate_paramidnumrepmap.get(dataItem.getParamID() + paramTypes[1]);
                            numrep = new Integer(numrep + 1);
                            addreplicate_paramidnumrepmap.put(dataItem.getParamID() + paramTypes[1], numrep);
                        } else {
                            boolean paramfound = false;
                            boolean paramreplicatefound = false;
                            String replicatefound = "";
                            for (int j = 0; j < calcPRecoveryDataItemList.size(); ++j) {
                                DataItem calcDataItem = (DataItem)calcPRecoveryDataItemList.get(j);
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
                                addparamtype.append(paramTypes[1] + "~");
                                String dispformat = dataItem.getDisplayFormat();
                                if (dispformat == null) {
                                    dispformat = "";
                                }
                                adddisplayformat.append(dispformat + "~");
                                tempAddParamid.append(dataItem.getParamID() + ";");
                                tempAddParamType.append(paramTypes[1] + ";");
                                add_paramidnumrepmap.put(dataItem.getParamID() + paramTypes[1], new Integer(dataItem.getReplicate()));
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
                                addreplicateparamtype.append(paramTypes[1] + ";");
                                tempReplicateParamid.append(dataItem.getParamID() + ";");
                                tempReplicateParamType.append(paramTypes[1] + ";");
                                addreplicate_paramidnumrepmap.put(dataItem.getParamID() + paramTypes[1], new Integer(addreplicatecnt));
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
                        editparamtype.append(paramTypes[1] + ";");
                        editenteredvalue.append(PR + ";");
                        replicateid.append(dataItem.getReplicate() + ";");
                        if (expectedRecoveryParamList.contains(dataItem.getParamID())) continue;
                        boolean paramfound = false;
                        for (int j = 0; j < calcERecoveryDataItemList.size(); ++j) {
                            DataItem calcDataItem = (DataItem)calcERecoveryDataItemList.get(j);
                            if (!calcDataItem.getParamID().equals(dataItem.getParamID())) continue;
                            paramfound = true;
                            break;
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
                            addparamtype.append(paramTypes[0] + "~");
                            tempAddParamid.append(dataItem.getParamID() + ";");
                            tempAddParamType.append(paramTypes[0] + ";");
                            String dispformat = dataItem.getDisplayFormat();
                            if (dispformat == null) {
                                dispformat = "";
                            }
                            adddisplayformat.append(dispformat + "~");
                            add_paramidnumrepmap.put(dataItem.getParamID() + paramTypes[0], new Integer(1));
                            if (!sdiDatasetHolder.contains(sdidataset)) {
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
                        editparamtype.append(paramTypes[0] + ";");
                        editenteredvalue.append(E + ";");
                        replicateid.append("1;");
                        expectedRecoveryParamList.add(dataItem.getParamID());
                    }
                    if (tempAddParamid.length() > 0) {
                        tempAddParamid.setLength(tempAddParamid.length() - 1);
                        tempAddParamType.setLength(tempAddParamType.length() - 1);
                        String[] paramids = StringUtil.split(tempAddParamid.toString(), ";");
                        paramtypes = StringUtil.split(tempAddParamType.toString(), ";");
                        numreplicates.setLength(0);
                        for (int l = 0; l < paramids.length; ++l) {
                            numreplicates.append(add_paramidnumrepmap.get(paramids[l] + paramtypes[l]) + "~");
                        }
                        add_numreplicates.append(numreplicates);
                    }
                    if (tempReplicateParamid.length() > 0) {
                        tempReplicateParamid.setLength(tempReplicateParamid.length() - 1);
                        tempReplicateParamType.setLength(tempReplicateParamType.length() - 1);
                        String[] paramids = StringUtil.split(tempReplicateParamid.toString(), ";");
                        paramtypes = StringUtil.split(tempReplicateParamType.toString(), ";");
                        numreplicates.setLength(0);
                        for (int l = 0; l < paramids.length; ++l) {
                            numreplicates.append(addreplicate_paramidnumrepmap.get(paramids[l] + paramtypes[l]) + ";");
                        }
                        addReplicate_numreplicates.append(numreplicates);
                    }
                    add_paramidnumrepmap.clear();
                    addreplicate_paramidnumrepmap.clear();
                    tempAddParamid.setLength(0);
                    tempReplicateParamid.setLength(0);
                    tempAddParamType.setLength(0);
                    tempReplicateParamType.setLength(0);
                    expectedRecoveryParamList.clear();
                    linkEnterDataAverageMap.clear();
                    linkEnterDataReplicateMap.clear();
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
                if (sdiDatasetHolder.size() <= 0) break block84;
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
                            this.logger.error("doQCCalcSpike error", exc);
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
                this.logger.error("doQCCalcSpike error", ex);
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
        String calcRuleER = "";
        String calcRulePR = "";
        if ("QCCalcSpikePRecovery".equalsIgnoreCase(actionId)) {
            calcRuleER = "(([#;CURRENTITEM;#]*0)+(([AQC:QCParam|SpikeVolume;Standard]*([AQC:QCParam|#;Concentration]-avg([AQC:Linked|#;LINKEDITEM;*])))/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))";
            calcRulePR = "100*(([#;CURRENTITEM;#]-avg([AQC:Linked|#;LINKEDITEM;*]))/(([AQC:QCParam|SpikeVolume;Standard]*([AQC:QCParam|#;Concentration]-avg([AQC:Linked|#;LINKEDITEM;*])))/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))";
        } else if ("QCCalcSpikeIUPACMeth".equalsIgnoreCase(actionId)) {
            calcRuleER = "(([#;CURRENTITEM;#]*0)+(([AQC:QCParam|SpikeVolume;Standard]*[AQC:QCParam|#;Concentration])/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))";
            calcRulePR = "100*(([#;CURRENTITEM;#] - ((avg([AQC:Linked|#;LINKEDITEM;*])*[AQC:QCParam|SampleVolume;Standard])/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))/(([AQC:QCParam|SpikeVolume;Standard]*[AQC:QCParam|#;Concentration])/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))";
        } else if ("QCCalcSpikeConcMeth".equalsIgnoreCase(actionId)) {
            calcRuleER = "(([#;CURRENTITEM;#]*0)+(([AQC:QCParam|SpikeVolume;Standard]*[AQC:QCParam|#;Concentration])/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard])))";
            calcRulePR = "100*(([#;CURRENTITEM;#] - avg([AQC:Linked|#;LINKEDITEM;*]))/(([AQC:QCParam|SpikeVolume;Standard]*[AQC:QCParam|#;Concentration])/([AQC:QCParam|SampleVolume;Standard]+[AQC:QCParam|SpikeVolume;Standard]))) ";
        }
        DataSet calcDataItems = new DataSet();
        if (qcBatchDetails != null && qcBatchDetails.getRowCount() > 0) {
            QCBatch qcBatch = QCBatchPool.getQCBatch(qp, qcBatchId);
            String qcBatchItemIds = "'" + qcBatchDetails.getColumnValues("s_qcbatchitemid", "','") + "'";
            String[] linkedBatchItemIds = StringUtil.split(qcBatchDetails.getColumnValues("linktoqcbatchitemid", ";"), ";");
            String[] paramTypes = StringUtil.split(PARAMETER_TYPE, ";");
            for (int pt = 0; pt < paramTypes.length; ++pt) {
                DataSet calcDataItemPR;
                String[] batchItemIds;
                DataSet calcDataItemER;
                if ("ExpectedRecovery".equals(paramTypes[pt]) && (calcDataItemER = QCUtil.getCalcDataItems(qp, qcBatchId, qcBatchItemIds, paramTypes[pt])) != null && calcDataItemER.getRowCount() > 0) {
                    batchItemIds = StringUtil.split(qcBatchDetails.getColumnValues("s_qcbatchitemid", ";"), ";");
                    QCUtil.setCalcRule(calcDataItemER, calcRuleER, qcBatch, batchItemIds, linkedBatchItemIds);
                    calcDataItemER.sort("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid");
                    ArrayList<DataSet> grpList = calcDataItemER.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid");
                    for (int i = 0; i < grpList.size(); ++i) {
                        DataSet dsGrp = grpList.get(i);
                        dsGrp.sort("replicateid");
                        calcDataItems.copyRow(dsGrp, 0, 1);
                    }
                }
                if (!"%Recovery".equals(paramTypes[pt]) || (calcDataItemPR = QCUtil.getCalcDataItems(qp, qcBatchId, qcBatchItemIds, paramTypes[pt])) == null || calcDataItemPR.getRowCount() <= 0) continue;
                batchItemIds = StringUtil.split(qcBatchDetails.getColumnValues("s_qcbatchitemid", ";"), ";");
                QCUtil.setCalcRule(calcDataItemPR, calcRulePR, qcBatch, batchItemIds, linkedBatchItemIds);
                for (int i = 0; i < calcDataItemPR.getRowCount(); ++i) {
                    calcDataItems.copyRow(calcDataItemPR, i, 1);
                }
            }
        }
        return calcDataItems;
    }

    public boolean isCalcRuleAdded(String qcBatchId, String qcBatchItemId, QueryProcessor qp, ConnectionProcessor cp) {
        String[] paramTypes = StringUtil.split(PARAMETER_TYPE, ";");
        DataSet ds = QCUtil.getDataItemsWithCalcRuleDefined(qcBatchId, "'" + qcBatchItemId + "'", paramTypes[0], qp, cp.isOra());
        return ds != null && ds.getRowCount() > 0;
    }
}

