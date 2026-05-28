/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.actions.DSApproval;
import com.labvantage.opal.util.DataItem2;
import com.labvantage.opal.util.DataSet2;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDIDataSet2
extends BaseCustom {
    public static String LABVANTAGE_CVS_ID = "$Revision: 79167 $";
    public static final String CONSTANT_NULL = "(null)";
    public static final int MODE_DATASET = 0;
    public static final int MODE_SDI = 1;
    private String __Sdcid;
    private String __Keyid1;
    private String __Keyid2;
    private String __Keyid3;
    private HashMap<String, DataSet2> __DatasetMap = new HashMap();
    boolean mandatoryonly;
    boolean enteredOptionalToBReleased;

    public SDIDataSet2(ConnectionInfo connectionInfo) {
        this.setConnectionId(connectionInfo.getConnectionId());
    }

    public String getSdcId() {
        return this.__Sdcid;
    }

    public void setSdcId(String sdcId) {
        this.__Sdcid = sdcId;
    }

    public String getKeyid1() {
        return this.__Keyid1;
    }

    public void setKeyid1(String keyid1) {
        this.__Keyid1 = keyid1;
    }

    public String getKeyid2() {
        if (this.__Keyid2 == null || this.__Keyid2.trim().length() == 0) {
            this.__Keyid2 = CONSTANT_NULL;
        }
        return this.__Keyid2;
    }

    public void setKeyid2(String keyid2) {
        this.__Keyid2 = keyid2;
    }

    public String getKeyid3() {
        if (this.__Keyid3 == null || this.__Keyid3.trim().length() == 0) {
            this.__Keyid3 = CONSTANT_NULL;
        }
        return this.__Keyid3;
    }

    public void setKeyid3(String keyid3) {
        this.__Keyid3 = keyid3;
    }

    public void populate() {
        int i;
        String id;
        String dataset;
        String variantID;
        String paramListVersionID;
        String paramListID;
        String keyId3;
        String keyId2;
        String keyId1;
        String sdcId;
        String id2;
        String dataset2;
        String variantID2;
        String paramListVersionID2;
        String paramListID2;
        String keyId32;
        String keyId22;
        String keyId12;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdc = sdcProcessor.getPropertyList(this.__Sdcid);
        String keycolid1 = sdc.getProperty("keycolid1");
        String keycolid2 = sdc.getProperty("keycolid2");
        String keycolid3 = sdc.getProperty("keycolid3");
        String rset_id = "";
        if (StringUtil.getLen(keycolid1) > 0L && StringUtil.getLen(keycolid2) == 0L && StringUtil.getLen(keycolid3) == 0L) {
            try {
                rset_id = this.getDAMProcessor().createRSet(this.__Sdcid, this.getKeyid1(), null, null);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to create RSET " + e.getMessage());
            }
        }
        if (rset_id.length() > 0) {
            sql.append("select ds.approvalflag,di.sdcid, di.keyid1, di.keyid2,di.keyid3,di.paramlistid, di.paramlistversionid, di.variantid, di.dataset, di.paramid, di.replicateid, di.enteredvalue, di.enteredtext, di.mandatoryflag, di.releasedflag, ds.s_datasetstatus");
            sql.append(" from sdidataitem di, sdidata ds, rsetitems r");
            sql.append(" where di.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and di.keyid1 = r.keyid1 ");
            sql.append(" and r.rsetid = " + safeSQL.addVar(rset_id));
            sql.append(" and di.sdcid = ds.sdcid");
            sql.append(" and di.keyid1 = ds.keyid1");
            sql.append(" and di.keyid2 = ds.keyid2");
            sql.append(" and di.keyid3 = ds.keyid3");
            sql.append(" and di.paramlistid = ds.paramlistid");
            sql.append(" and di.paramlistversionid = ds.paramlistversionid");
            sql.append(" and di.variantid = ds.variantid");
            sql.append(" and di.dataset = ds.dataset");
            sql.append(" and (s_datasetstatus!='Cancelled' or s_datasetstatus is null or s_datasetstatus='')");
            sql.append(" order by di.keyid1, di.usersequence");
        } else {
            sql.append("select ds.approvalflag,di.sdcid, di.keyid1, di.keyid2,di.keyid3,di.paramlistid, di.paramlistversionid, di.variantid, di.dataset, di.paramid, di.replicateid, di.enteredvalue, di.enteredtext, di.mandatoryflag, di.releasedflag, ds.s_datasetstatus");
            sql.append(" from sdidataitem di, sdidata ds");
            sql.append(" where di.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and di.keyid1 in ( ").append(safeSQL.addIn(this.getKeyid1(), ";")).append(" )");
            sql.append(" and di.keyid2 in ( ").append(safeSQL.addIn(this.getKeyid2(), ";")).append(" )");
            sql.append(" and di.keyid3 in ( ").append(safeSQL.addIn(this.getKeyid3(), ";")).append(" )");
            sql.append(" and di.sdcid = ds.sdcid");
            sql.append(" and di.keyid1 = ds.keyid1");
            sql.append(" and di.keyid2 = ds.keyid2");
            sql.append(" and di.keyid3 = ds.keyid3");
            sql.append(" and di.paramlistid = ds.paramlistid");
            sql.append(" and di.paramlistversionid = ds.paramlistversionid");
            sql.append(" and di.variantid = ds.variantid");
            sql.append(" and di.dataset = ds.dataset");
            sql.append(" and (s_datasetstatus!='Cancelled' or s_datasetstatus is null or s_datasetstatus='')");
            sql.append(" order by di.keyid1, di.usersequence");
        }
        DataSet ds_dataitem = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        sql.setLength(0);
        safeSQL.reset();
        if (rset_id.length() > 0) {
            sql.append("select sdr.sdcid, sdr.keyid1, sdr.keyid2, sdr.keyid3, sdr.paramlistid, sdr.paramlistversionid, sdr.variantid, sdr.dataset, sdr.mandatoryflag");
            sql.append(" from sdidatarelation sdr, rsetitems r");
            sql.append(" where sdr.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and sdr.keyid1 = r.keyid1 ");
            sql.append(" and r.rsetid = " + safeSQL.addVar(rset_id));
            sql.append(" and sdr.mandatoryflag = 'Y' and ( sdr.tokeyid1 is null or sdr.tokeyid1 = '' )");
            sql.append(" order by sdr.sdcid, sdr.keyid1, sdr.keyid2, sdr.keyid3, sdr.paramlistid, sdr.paramlistversionid, sdr.variantid, sdr.dataset");
        } else {
            sql.append("select sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, mandatoryflag");
            sql.append(" from sdidatarelation");
            sql.append(" where sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and keyid1 in ( ").append(safeSQL.addIn(this.getKeyid1(), ";")).append(" )");
            sql.append(" and keyid2 in ( ").append(safeSQL.addIn(this.getKeyid2(), ";")).append(" )");
            sql.append(" and keyid3 in ( ").append(safeSQL.addIn(this.getKeyid3(), ";")).append(" )");
            sql.append(" and mandatoryflag = 'Y' and ( tokeyid1 is null or tokeyid1 = '' )");
            sql.append(" order by sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset");
        }
        DataSet ds_incompleterelation = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        sql.setLength(0);
        safeSQL.reset();
        if (rset_id.length() > 0) {
            sql.append("select a.*, d.sdcid ds_sdcid, d.keyid1 ds_keyid1, d.keyid2 ds_keyid2, d.keyid3 ds_keyid3, d.paramlistid ds_paramlistid, d.paramlistversionid ds_paramlistversionid, d.variantid ds_variantid, d.dataset ds_dataset");
            sql.append(" from sdiattribute a, sdidata d, rsetitems r");
            sql.append(" where d.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and d.keyid1 = r.keyid1 ");
            sql.append(" and r.rsetid = " + safeSQL.addVar(rset_id));
            sql.append(" and a.sdcid = 'DataSet' and a.keyid1 = d.sdidataid and a.mandatoryflag = 'Y' ");
            sql.append(" order by a.keyid1");
        } else {
            sql.append("select a.*, d.sdcid ds_sdcid, d.keyid1 ds_keyid1, d.keyid2 ds_keyid2, d.keyid3 ds_keyid3, d.paramlistid ds_paramlistid, d.paramlistversionid ds_paramlistversionid, d.variantid ds_variantid, d.dataset ds_dataset");
            sql.append(" from sdiattribute a, sdidata d");
            sql.append(" where d.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
            sql.append(" and d.keyid1 in ( ").append(safeSQL.addIn(this.getKeyid1(), ";")).append(" )");
            sql.append(" and d.keyid2 in ( ").append(safeSQL.addIn(this.getKeyid2(), ";")).append(" )");
            sql.append(" and d.keyid3 in ( ").append(safeSQL.addIn(this.getKeyid3(), ";")).append(" )");
            sql.append(" and a.sdcid = 'DataSet' and a.keyid1 = d.sdidataid and a.mandatoryflag = 'Y' ");
            sql.append(" order by a.keyid1");
        }
        DataSet ds_mandatoryAttributes = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
        if (ds_dataitem != null && ds_dataitem.size() > 0) {
            for (int i2 = 0; i2 < ds_dataitem.size(); ++i2) {
                String sdcId2 = ds_dataitem.getValue(i2, "sdcid");
                keyId12 = ds_dataitem.getValue(i2, "keyid1");
                keyId22 = ds_dataitem.getValue(i2, "keyid2");
                keyId32 = ds_dataitem.getValue(i2, "keyid3");
                paramListID2 = ds_dataitem.getValue(i2, "paramlistid");
                paramListVersionID2 = ds_dataitem.getValue(i2, "paramlistversionid");
                variantID2 = ds_dataitem.getValue(i2, "variantid");
                dataset2 = ds_dataitem.getValue(i2, "dataset");
                id2 = sdcId2 + "|" + keyId12 + "|" + keyId22 + "|" + keyId32 + "|" + paramListID2 + "|" + paramListVersionID2 + "|" + variantID2 + "|" + dataset2;
                DataSet2 ds = null;
                if (this.__DatasetMap.containsKey(id2)) {
                    ds = this.__DatasetMap.get(id2);
                } else {
                    String status = ds_dataitem.getValue(i2, "s_datasetstatus");
                    String approvalflag = ds_dataitem.getString(i2, "approvalflag", "");
                    if (status == null || status.trim().length() == 0) {
                        status = "";
                    }
                    ds = new DataSet2(sdcId2, keyId12, keyId22, keyId32, paramListID2, paramListVersionID2, variantID2, dataset2);
                    ds.setStatus(status);
                    ds.setApprovalFlag(approvalflag);
                    this.__DatasetMap.put(id2, ds);
                }
                DataItem2 di = new DataItem2();
                di.setDataSet2(ds);
                di.setParamID(ds_dataitem.getValue(i2, "paramid"));
                di.setParamType(ds_dataitem.getValue(i2, "paramtype"));
                di.setReplicate(ds_dataitem.getValue(i2, "replicateid"));
                di.setEnteredText(ds_dataitem.getValue(i2, "enteredtext"));
                String mandatory = ds_dataitem.getValue(i2, "mandatoryflag");
                if (mandatory == null) {
                    mandatory = "N";
                }
                di.setMandatory(mandatory.equals("Y"));
                String released = ds_dataitem.getValue(i2, "releasedflag");
                if (released == null) {
                    released = "N";
                }
                di.setReleased(released.equals("Y"));
                ds.add(di);
            }
            sql.setLength(0);
            safeSQL.reset();
            if (rset_id.length() > 0) {
                sql.append("select sda.sdcid, sda.keyid1, sda.keyid2, sda.keyid3, sda.paramlistid, sda.paramlistversionid, sda.variantid, sda.dataset, sda.mandatoryflag, sda.approvalflag");
                sql.append(" from sdidataapproval sda, rsetitems r");
                sql.append(" where sda.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
                sql.append(" and sda.keyid1 = r.keyid1 ");
                sql.append(" and r.rsetid = " + safeSQL.addVar(rset_id));
                sql.append(" order by sda.usersequence");
            } else {
                sql.append("select sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, mandatoryflag, approvalflag");
                sql.append(" from sdidataapproval");
                sql.append(" where sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
                sql.append(" and keyid1 in ( ").append(safeSQL.addIn(this.getKeyid1(), ";")).append(" )");
                sql.append(" and keyid2 in ( ").append(safeSQL.addIn(this.getKeyid2(), ";")).append(" )");
                sql.append(" and keyid3 in ( ").append(safeSQL.addIn(this.getKeyid3(), ";")).append(" )");
                sql.append(" order by usersequence");
            }
            DataSet ds_approval = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds_approval != null) {
                for (int i3 = 0; i3 < ds_approval.size(); ++i3) {
                    sdcId = ds_approval.getValue(i3, "sdcid");
                    keyId1 = ds_approval.getValue(i3, "keyid1");
                    keyId2 = ds_approval.getValue(i3, "keyid2");
                    keyId3 = ds_approval.getValue(i3, "keyid3");
                    paramListID = ds_approval.getValue(i3, "paramlistid");
                    paramListVersionID = ds_approval.getValue(i3, "paramlistversionid");
                    variantID = ds_approval.getValue(i3, "variantid");
                    dataset = ds_approval.getValue(i3, "dataset");
                    String mandatoryFlag = ds_approval.getValue(i3, "mandatoryflag");
                    String approvalFlag = ds_approval.getValue(i3, "approvalflag");
                    id = sdcId + "|" + keyId1 + "|" + keyId2 + "|" + keyId3 + "|" + paramListID + "|" + paramListVersionID + "|" + variantID + "|" + dataset;
                    DataSet2 ds = null;
                    if (this.__DatasetMap.containsKey(id)) {
                        ds = this.__DatasetMap.get(id);
                    }
                    if (ds == null) continue;
                    DSApproval dsApproval = new DSApproval();
                    if (mandatoryFlag != null && mandatoryFlag.equals("Y")) {
                        dsApproval.setMandatory(true);
                    } else {
                        dsApproval.setMandatory(false);
                    }
                    if (approvalFlag == null || approvalFlag.length() == 0 || approvalFlag.equals("U")) {
                        dsApproval.setApproved(false);
                    } else {
                        dsApproval.setApproved(true);
                    }
                    ds.addApproval(dsApproval);
                }
            }
        } else {
            sql.setLength(0);
            safeSQL.reset();
            if (rset_id.length() > 0) {
                sql.append("select sd.sdcid, sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset, sd.s_datasetstatus");
                sql.append(" from sdidata sd, rsetitems r");
                sql.append(" where sd.sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
                sql.append(" and sd.keyid1 = r.keyid1 ");
                sql.append(" and r.rsetid = " + safeSQL.addVar(rset_id));
                sql.append(" and (sd.s_datasetstatus!='Cancelled' or sd.s_datasetstatus is null or sd.s_datasetstatus='')");
                sql.append(" order by sd.keyid1, sd.paramlistid");
            } else {
                sql.append("select sdcid, keyid1,keyid2,keyid3, paramlistid, paramlistversionid, variantid, dataset, s_datasetstatus");
                sql.append(" from sdidata");
                sql.append(" where sdcid = ").append(safeSQL.addVar(this.__Sdcid)).append("");
                sql.append(" and keyid1 in ( ").append(safeSQL.addIn(this.getKeyid1(), ";")).append(" )");
                sql.append(" and keyid2 in ( ").append(safeSQL.addIn(this.getKeyid2(), ";")).append(" )");
                sql.append(" and keyid3 in ( ").append(safeSQL.addIn(this.getKeyid3(), ";")).append(" )");
                sql.append(" and (s_datasetstatus!='Cancelled' or s_datasetstatus is null or s_datasetstatus='')");
                sql.append(" order by keyid1, paramlistid");
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i4 = 0; i4 < ds.size(); ++i4) {
                    sdcId = ds.getValue(i4, "sdcid");
                    keyId1 = ds.getValue(i4, "keyid1");
                    keyId2 = ds.getValue(i4, "keyid2");
                    keyId3 = ds.getValue(i4, "keyid3");
                    paramListID = ds.getValue(i4, "paramlistid");
                    paramListVersionID = ds.getValue(i4, "paramlistversionid");
                    variantID = ds.getValue(i4, "variantid");
                    dataset = ds.getValue(i4, "dataset");
                    String id3 = sdcId + "|" + keyId1 + "|" + keyId2 + "|" + keyId3 + "|" + paramListID + "|" + paramListVersionID + "|" + variantID + "|" + dataset;
                    DataSet2 dataset22 = null;
                    if (this.__DatasetMap.containsKey(id3)) {
                        dataset22 = this.__DatasetMap.get(id3);
                        continue;
                    }
                    String status = ds.getValue(i4, "s_datasetstatus", "");
                    dataset22 = new DataSet2(sdcId, keyId1, keyId2, keyId3, paramListID, paramListVersionID, variantID, dataset);
                    dataset22.setStatus(status);
                    this.__DatasetMap.put(id3, dataset22);
                }
            }
        }
        for (i = 0; i < ds_incompleterelation.getRowCount(); ++i) {
            String sdcId3 = ds_incompleterelation.getValue(i, "sdcid");
            keyId12 = ds_incompleterelation.getValue(i, "keyid1");
            keyId22 = ds_incompleterelation.getValue(i, "keyid2");
            keyId32 = ds_incompleterelation.getValue(i, "keyid3");
            paramListID2 = ds_incompleterelation.getValue(i, "paramlistid");
            paramListVersionID2 = ds_incompleterelation.getValue(i, "paramlistversionid");
            variantID2 = ds_incompleterelation.getValue(i, "variantid");
            dataset2 = ds_incompleterelation.getValue(i, "dataset");
            id2 = sdcId3 + "|" + keyId12 + "|" + keyId22 + "|" + keyId32 + "|" + paramListID2 + "|" + paramListVersionID2 + "|" + variantID2 + "|" + dataset2;
            if (!this.__DatasetMap.containsKey(id2)) continue;
            DataSet2 dataset23 = this.__DatasetMap.get(id2);
            dataset23.setMandatoryDatasetRelationIncomplete(true);
        }
        for (i = 0; i < ds_mandatoryAttributes.getRowCount(); ++i) {
            String dataType = ds_mandatoryAttributes.getValue(i, "datatype");
            String attributeColumn = "";
            if ("D".equals(dataType) || "O".equals(dataType)) {
                attributeColumn = "datevalue";
            } else if ("S".equals(dataType)) {
                attributeColumn = "textvalue";
            } else if ("C".equals(dataType)) {
                attributeColumn = "clobvalue";
            } else if ("N".equals(dataType)) {
                attributeColumn = "numericvalue";
            }
            String attributeValue = ds_mandatoryAttributes.getValue(i, attributeColumn);
            if (attributeValue.length() > 0) continue;
            String sdcId4 = ds_mandatoryAttributes.getValue(i, "ds_sdcid");
            String keyId13 = ds_mandatoryAttributes.getValue(i, "ds_keyid1");
            String keyId23 = ds_mandatoryAttributes.getValue(i, "ds_keyid2");
            String keyId33 = ds_mandatoryAttributes.getValue(i, "ds_keyid3");
            String paramListID3 = ds_mandatoryAttributes.getValue(i, "ds_paramlistid");
            String paramListVersionID3 = ds_mandatoryAttributes.getValue(i, "ds_paramlistversionid");
            String variantID3 = ds_mandatoryAttributes.getValue(i, "ds_variantid");
            String dataset3 = ds_mandatoryAttributes.getValue(i, "ds_dataset");
            id = sdcId4 + "|" + keyId13 + "|" + keyId23 + "|" + keyId33 + "|" + paramListID3 + "|" + paramListVersionID3 + "|" + variantID3 + "|" + dataset3;
            if (!this.__DatasetMap.containsKey(id)) continue;
            DataSet2 dataset24 = this.__DatasetMap.get(id);
            dataset24.set__MandatoryAttributeNotFilledIn(true);
        }
        if (rset_id != null) {
            this.getDAMProcessor().clearRSet(rset_id);
        }
    }

    public List<DataSet2> evaluate() {
        this.populate();
        ArrayList<DataSet2> modifiedDatasets = new ArrayList<DataSet2>();
        if (this.__DatasetMap.size() > 0) {
            Set<String> keySet = this.__DatasetMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String aKeySet;
                String key = aKeySet = iterator.next();
                DataSet2 ds = this.__DatasetMap.get(key);
                ds.set_EnterOptionalToBereleased(this.isEnteredOptionalDataItemRequireRelease());
                if (!ds.evaluateStatus()) continue;
                modifiedDatasets.add(ds);
            }
        }
        return modifiedDatasets;
    }

    public boolean isMandatoryonly() {
        return this.mandatoryonly;
    }

    public void setMandatoryonly(boolean mandatoryonly) {
        this.mandatoryonly = mandatoryonly;
    }

    public void setEnteredOptionToBeReleased(boolean enterOptionalToRelease) {
        this.enteredOptionalToBReleased = enterOptionalToRelease;
    }

    public boolean isEnteredOptionalDataItemRequireRelease() {
        return this.enteredOptionalToBReleased;
    }
}

