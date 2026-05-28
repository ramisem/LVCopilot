/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AssignStrgAndReceive
extends BaseAction
implements sapphire.action.AssignStrgAndReceive {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        sdcid = properties.getProperty("sdcid", "").trim();
        keyid1 = properties.getProperty("keyid1", "").trim();
        keyid2 = properties.getProperty("keyid2", "").trim();
        keyid3 = properties.getProperty("keyid3", "").trim();
        storageunitid = properties.getProperty("storageunitid", "").trim();
        trackitemids = properties.getProperty("trackitemid", "").trim();
        isTrackItem = properties.getProperty("istrackitem", "N").equals("Y");
        clearStorage = properties.getProperty("clearstorage", "N").equals("Y");
        applylock = properties.getProperty("applylock").equals("Y");
        deleterset = false;
        dam = this.getDAMProcessor();
        rsetid = "";
        if (isTrackItem) {
            if (trackitemids.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Missing required action input property - trackitemid"));
            }
        } else {
            if (sdcid.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Missing required action input property - sdcid"));
            }
            if (keyid1.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Missing required action input property - keyid1"));
            }
        }
        if (clearStorage) {
            storageunitid = "";
        } else if (storageunitid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Missing required action input property - storageunitid"));
        }
        storageMap = new HashMap<String, String>();
        addDS = new DataSet();
        editDS = new DataSet();
        key2array = new String[]{};
        key3array = new String[]{};
        if (isTrackItem) {
            trackitemsarray = StringUtil.split(trackitemids, ";");
            if (storageunitid.contains(";")) {
                suarray = StringUtil.split(storageunitid, ";");
                if (suarray.length != trackitemsarray.length) {
                    throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Count of Storage units does not match count of TrackItems to be filed"));
                }
                i = 0;
                for (String key : trackitemsarray) {
                    row = editDS.addRow();
                    editDS.setString(row, "trackitemid", key);
                    editDS.setString(row, "storageunitid", suarray[i]);
                    ++i;
                }
            } else {
                for (String key : trackitemsarray) {
                    row = editDS.addRow();
                    editDS.setString(row, "trackitemid", key);
                    editDS.setString(row, "storageunitid", storageunitid);
                }
            }
        } else {
            key1array = StringUtil.split(keyid1, ";");
            key2array = keyid2.length() > 0 ? StringUtil.split(keyid2, ";") : null;
            v0 = key3array = keyid3.length() > 0 ? StringUtil.split(keyid3, ";") : null;
            if (key2array != null && key2array.length != key1array.length) {
                throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Count of KEYID2 does not match count of KEYID1"));
            }
            if (key3array != null && key3array.length != key1array.length) {
                throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Count of KEYID3 does not match count of KEYID1"));
            }
            if (storageunitid.contains(";")) {
                suarray = StringUtil.split(storageunitid, ";");
                if (suarray.length != key1array.length) {
                    throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Count of Storage units does not match count of SDIs to be filed"));
                }
                i = 0;
                for (String key : key1array) {
                    mapKey = key + (key2array != null ? key2array[i] : "") + (key3array != null ? key3array[i] : "");
                    storageMap.put(mapKey, suarray[i]);
                    ++i;
                }
            } else {
                i = 0;
                for (String key : key1array) {
                    mapKey = key + (key2array != null ? key2array[i] : "") + (key3array != null ? key3array[i] : "");
                    storageMap.put(mapKey, storageunitid);
                    ++i;
                }
            }
            try {
                if (rsetid.length() == 0) {
                    rsetid = applylock != false ? dam.createLockedRSet(sdcid, keyid1, keyid2.length() > 0 ? keyid2 : null, keyid3.length() > 0 ? keyid3 : null) : dam.createRSet(sdcid, keyid1, keyid2.length() > 0 ? keyid2 : null, keyid3.length() > 0 ? keyid3 : null);
                    if (rsetid.length() == 0) {
                        throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
                    }
                    deleterset = true;
                }
                sql = new StringBuilder();
                sql.append("select r.sdcid, r.keyid1, r.keyid2, r.keyid3,");
                if (this.connectionInfo.isOracle()) {
                    sql.append(" (select t.trackitemid");
                    sql.append(" from trackitem t");
                    sql.append(" where t.linksdcid = r.sdcid");
                    sql.append(" and t.linkkeyid1 = r.keyid1");
                    sql.append(" and nvl(t.linkkeyid2, '(null)') = r.keyid2");
                    sql.append(" and nvl(t.linkkeyid3, '(null)') = r.keyid3");
                    sql.append(" and rownum = 1) trackitemid");
                } else {
                    sql.append(" (select top(1) t.trackitemid");
                    sql.append(" from trackitem t");
                    sql.append(" where t.linksdcid = r.sdcid");
                    sql.append(" and t.linkkeyid1 = r.keyid1");
                    sql.append(" and isnull(t.linkkeyid2, '(null)') = r.keyid2");
                    sql.append(" and isnull(t.linkkeyid2, '(null)') = r.keyid3) trackitemid");
                }
                sql.append(" from rsetitems r");
                sql.append(" where r.rsetid = ?");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                if (ds != null && ds.size() > 0) {
                    for (i = 0; i < ds.size(); ++i) {
                        key1value = ds.getString(i, "keyid1", "").trim();
                        key2value = ds.getString(i, "keyid2", "").trim();
                        key3value = ds.getString(i, "keyid3", "").trim();
                        if ("(null)".equals(key2value)) {
                            key2value = "";
                        }
                        if ("(null)".equals(key3value)) {
                            key3value = "";
                        }
                        trackitemid = ds.getString(i, "trackitemid", "").trim();
                        storageunitvalue = (String)storageMap.get(key1value + key2value + key3value);
                        if (trackitemid.length() > 0) {
                            packageStatusSQL = "";
                            packageStatusSQL = this.connectionInfo.isOracle() != false ? "select storageunitid, linksdcid, linkkeyid1, (select p.packagestatus from s_package p where p.s_packageid = su.linkkeyid1 and su.linksdcid = 'LV_Package') packagestatus from storageunit su connect by prior su.parentid = su.storageunitid start with su.storageunitid = (SELECT t.currentstorageunitid from trackitem t where t.trackitemid = ?)" : "WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1) AS ( SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1 FROM storageunit AS su WHERE su.storageunitid = (SELECT t.currentstorageunitid from trackitem t where t.trackitemid = ?) UNION ALL SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1 FROM storageunit AS su INNER JOIN StorageUnitTree AS d ON su.storageunitid = d.parentid ) SELECT st.storageunitid, st.linksdcid, (select p.packagestatus from s_package p where p.s_packageid = st.linkkeyid1 and st.linksdcid = 'LV_Package') packagestatus FROM StorageUnitTree st";
                            d = this.getQueryProcessor().getPreparedSqlDataSet(packageStatusSQL, (Object[])new String[]{trackitemid});
                            if (d != null) {
                                for (row = 0; row < d.size(); ++row) {
                                    if (!"Shipped".equals(d.getString(row, "packagestatus", ""))) continue;
                                    throw new SapphireException(this.getTranslationProcessor().translate("ERROR: Selected item is in a Shipped Package"));
                                }
                            }
                            row = editDS.addRow();
                            editDS.setString(row, "keyid1", key1value);
                            editDS.setString(row, "keyid2", key2value);
                            editDS.setString(row, "keyid3", key3value);
                            editDS.setString(row, "trackitemid", trackitemid);
                            editDS.setString(row, "storageunitid", storageunitvalue);
                            continue;
                        }
                        row = addDS.addRow();
                        addDS.setString(row, "keyid1", key1value);
                        addDS.setString(row, "keyid2", key2value);
                        addDS.setString(row, "keyid3", key3value);
                        addDS.setString(row, "storageunitid", storageunitvalue);
                    }
                } else {
                    throw new SapphireException(this.getTranslationProcessor().translate("Failed to fetch trackitem records for SDIs"));
                }
                if (clearStorage || !"Sample".equals(sdcid)) ** GOTO lbl221
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, samplestatus, disposalstatus, receiveddt from s_sample where s_sampleid in (select keyid1 from rsetitems where rsetid = ?)", (Object[])new String[]{rsetid});
                dsNotReceived = new DataSet();
                dsAlreadyReceived = new DataSet();
                for (i = 0; i < ds.getRowCount(); ++i) {
                    receivedDt = ds.getValue(i, "receiveddt");
                    if (receivedDt.length() > 0) {
                        dsAlreadyReceived.copyRow(ds, i, 1);
                        continue;
                    }
                    dsNotReceived.copyRow(ds, i, 1);
                }
                if (dsNotReceived.getRowCount() > 0) {
                    samplestatus = dsNotReceived.getColumnValues("samplestatus", ";").replaceAll("Initial", "Received");
                    makeRetain = properties.getProperty("makeretain");
                    props = new PropertyList();
                    props.setProperty("sdcid", sdcid);
                    props.setProperty("keyid1", dsNotReceived.getColumnValues("s_sampleid", ";"));
                    props.setProperty("samplestatus", samplestatus);
                    props.setProperty("__sdcruleconfirm", "Y");
                    props.setProperty("receiveddt", "n");
                    props.setProperty("receivedby", this.connectionInfo.getSysuserId());
                    props.setProperty("auditreason", properties.getProperty("auditreason"));
                    props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                    if ("yes".equalsIgnoreCase(makeRetain) || "Y".equalsIgnoreCase(makeRetain)) {
                        props.setProperty("disposalstatus", "Retained");
                        props.setProperty("statusrollup", "true");
                    } else if (("no".equalsIgnoreCase(makeRetain) || "N".equalsIgnoreCase(makeRetain)) && dsNotReceived.getValue(0, "disposalstatus").equals("Retained")) {
                        props.setProperty("disposalstatus", "");
                    }
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
                if (dsAlreadyReceived.getRowCount() <= 0) ** GOTO lbl221
                fireAction = false;
                makeRetain = properties.getProperty("makeretain");
                props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dsAlreadyReceived.getColumnValues("s_sampleid", ";"));
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                if ("yes".equalsIgnoreCase(makeRetain) || "Y".equalsIgnoreCase(makeRetain)) {
                    props.setProperty("disposalstatus", "Retained");
                    props.setProperty("statusrollup", "true");
                    fireAction = true;
                } else if (("no".equalsIgnoreCase(makeRetain) || "N".equalsIgnoreCase(makeRetain)) && dsAlreadyReceived.getValue(0, "disposalstatus").equals("Retained")) {
                    props.setProperty("disposalstatus", "");
                    fireAction = true;
                }
                if (!fireAction) ** GOTO lbl221
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            finally {
                if (rsetid != null && rsetid.length() > 0) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        }
lbl221:
        // 6 sources

        if (!clearStorage && addDS.size() > 0) {
            props = new PropertyList();
            props.setProperty("TrackItemSDC", "TrackItemSDC");
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", addDS.getColumnValues("keyid1", ";"));
            if (key2array != null) {
                props.setProperty("keyid2", addDS.getColumnValues("keyid2", ";"));
            }
            if (key3array != null) {
                props.setProperty("keyid3", addDS.getColumnValues("keyid3", ";"));
            }
            props.setProperty("numoftrackitems", String.valueOf(addDS.size()));
            this.getActionProcessor().processAction("AddTrackItem", "1", props);
            newtrackitemid = props.getProperty("newkeyid1");
            props.clear();
            props.setProperty("trackitemid", newtrackitemid);
            props.setProperty("currentstorageunitid", addDS.getColumnValues("storageunitid", ";"));
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("propsmatch", "Y");
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("__bypasscustodyrules", "Y");
            this.getActionProcessor().processAction("EditTrackItem", "1", props);
        }
        if (editDS.size() > 0) {
            props = new PropertyList();
            props.setProperty("trackitemid", editDS.getColumnValues("trackitemid", ";"));
            props.setProperty("currentstorageunitid", editDS.getColumnValues("storageunitid", ";"));
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("propsmatch", "Y");
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("__bypasscustodyrules", "Y");
            this.getActionProcessor().processAction("EditTrackItem", "1", props);
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

