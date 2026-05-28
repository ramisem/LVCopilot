/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.services.DDTConstants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddActivityLogPropertyHandler
extends BasePropertyHandler
implements DDTConstants {
    private static HashMap<String, String[]> detailParentKeyColumnMap = new HashMap();
    private static HashMap<String, String[]> detailTableKeyColumnMap = new HashMap();
    private static HashMap<String, String> detailTableSDCMap = new HashMap();

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        DataSet activityDataSet = (DataSet)props.get("activityDataSet");
        SDCProcessor sdcProcessor = this.getSdcProcessor();
        activityDataSet.addColumn("activitylogid", 0);
        activityDataSet.addColumn("refauditsequence", 1);
        activityDataSet.addColumn("activityby", 0);
        activityDataSet.addColumn("activitydt", 2);
        String reason = (String)props.get("reason");
        Long serverclienttimeoffset = (Long)props.get("serverclienttimeoffset");
        if (reason != null && reason.length() > 0) {
            activityDataSet.addColumn("reason", 0);
        }
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        Timestamp defaultactivitydt = DateTimeUtil.getNowTimestamp();
        HashMap<String, Integer> keyAuditSeqMap = new HashMap<String, Integer>();
        for (int a = 0; a < activityDataSet.getRowCount(); ++a) {
            String sdcid = activityDataSet.getValue(a, "sdcid");
            String keyid1 = activityDataSet.getValue(a, "keyid1");
            String keyid2 = activityDataSet.getValue(a, "keyid2", "(null)");
            String keyid3 = activityDataSet.getValue(a, "keyid3", "(null)");
            String primarycolumnid = activityDataSet.getValue(a, "columnid");
            if (primarycolumnid.indexOf(".") > 0) {
                PropertyListCollection linkdata = sdcProcessor.getLinks(sdcid);
                for (int link = 0; link < (linkdata != null ? linkdata.size() : 0); ++link) {
                    String fkcolid;
                    String linkcolid;
                    PropertyList linkProps = linkdata.getPropertyList(link);
                    if (linkProps == null || !linkProps.getProperty("linktype").equals("F") || !primarycolumnid.startsWith((linkcolid = linkProps.getProperty("sdccolumnid")) + ".") || (fkcolid = RequestParser.parseColumn(primarycolumnid).substring(linkcolid.length() + 1)).length() <= 0 || fkcolid.startsWith("_")) continue;
                    String linkkeycolid1 = linkProps.getProperty("sdccolumnid");
                    String linkkeycolid2 = linkProps.getProperty("sdccolumnid2");
                    String linkkeycolid3 = linkProps.getProperty("sdccolumnid3");
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "select " + linkkeycolid1 + (linkkeycolid2.length() > 0 ? ", " + linkkeycolid2 : "") + (linkkeycolid3.length() > 0 ? ", " + linkkeycolid3 : "") + " from " + sdcProcessor.getProperty(sdcid, "tableid") + " WHERE " + sdcProcessor.getProperty(sdcid, "keycolid1") + "=" + safeSQL.addVar(keyid1) + ("(null)".equals(keyid2) ? "" : " AND " + sdcProcessor.getProperty(sdcid, "keycolid2") + "=" + safeSQL.addVar(keyid2)) + ("(null)".equals(keyid3) ? "" : " AND " + sdcProcessor.getProperty(sdcid, "keycolid3") + "=" + safeSQL.addVar(keyid3));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    sdcid = linkProps.getProperty("linksdcid");
                    keyid1 = ds.getValue(0, linkkeycolid1);
                    if (linkkeycolid2.length() > 0) {
                        keyid2 = ds.getValue(0, linkkeycolid2);
                        keyid3 = linkkeycolid3.length() > 0 ? ds.getValue(0, linkkeycolid3) : "(null)";
                    } else {
                        keyid2 = "(null)";
                    }
                    activityDataSet.setValue(a, "sdcid", sdcid);
                    activityDataSet.setValue(a, "keyid1", keyid1);
                    activityDataSet.setValue(a, "keyid2", keyid2);
                    activityDataSet.setValue(a, "keyid3", keyid3);
                    activityDataSet.setValue(a, "columnid", primarycolumnid.substring(primarycolumnid.indexOf(".") + 1));
                    break;
                }
            }
            String primaryKeyValues = keyid1 + ";" + keyid2 + ";" + keyid3;
            String detailtableid = activityDataSet.getValue(a, "detailtableid");
            String detailkeycolumns = activityDataSet.getValue(a, "detailkeycolumns");
            String detailkeyvalues = activityDataSet.getValue(a, "detailkeyvalues");
            int sequence = this.getSequenceProcessor().getSequence("(System)", "_activitylog", 1);
            activityDataSet.setValue(a, "activitylogid", "" + sequence);
            int auditsequence = -1;
            String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
            String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
            if (detailtableid.length() == 0) {
                if (keyAuditSeqMap.get(primaryKeyValues) != null) {
                    auditsequence = (Integer)keyAuditSeqMap.get(primaryKeyValues);
                } else {
                    if (!"N".equals(sdcProcessor.getProperty(sdcid, "auditedflag"))) {
                        DataSet ds;
                        String table = sdcProcessor.getProperty(sdcid, "tableid");
                        int keyCount = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                        ArrayList<String> args = new ArrayList<String>();
                        String sql = "SELECT auditsequence FROM " + table + " WHERE " + keycolid1 + "=?";
                        args.add(keyid1);
                        if (keyCount > 1) {
                            sql = sql + " AND " + keycolid2 + "=?";
                            args.add(keyid2);
                        }
                        if (keyCount > 2) {
                            sql = sql + " AND " + keycolid3 + "=?";
                            args.add(keyid3);
                        }
                        int n = auditsequence = (ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, args.toArray())).size() == 0 ? 0 : ds.getInt(0, "auditsequence");
                        if (auditsequence == -999999999) {
                            auditsequence = -1;
                        }
                    }
                    keyAuditSeqMap.put(keyid1 + ";" + keyid2 + ";" + keyid3, auditsequence);
                }
            } else {
                String[] ddtdetailkeycolumns = detailTableKeyColumnMap.get(detailtableid);
                if (ddtdetailkeycolumns == null) {
                    DataSet ddtKeyColumnDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * from syscolumn where tableid=? and pkflag='Y' order by COLUMNSEQUENCE", new Object[]{detailtableid});
                    StringBuilder keySb = new StringBuilder();
                    StringBuilder parentKeySb = new StringBuilder();
                    for (int i = 0; i < ddtKeyColumnDs.getRowCount(); ++i) {
                        String columnid = ddtKeyColumnDs.getValue(i, "columnid");
                        if (!("sdcid".equals(columnid) || "keyid1".equals(columnid) || "keyid2".equals(columnid) || "keyid3".equals(columnid) || keycolid1.equals(columnid) || keycolid2.equals(columnid) || keycolid3.equals(columnid))) {
                            keySb.append(";" + columnid);
                            continue;
                        }
                        parentKeySb.append(";" + columnid);
                    }
                    detailTableKeyColumnMap.put(detailtableid, StringUtil.split(keySb.substring(1), ";"));
                    detailParentKeyColumnMap.put(detailtableid, StringUtil.split(parentKeySb.substring(1), ";"));
                }
                ddtdetailkeycolumns = detailTableKeyColumnMap.get(detailtableid);
                String detailSDC = detailTableSDCMap.get(detailtableid);
                if (detailSDC == null) {
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid from sdc where tableid=? and sdctype != 'D'", new Object[]{detailtableid});
                    detailSDC = ds.getRowCount() == 1 ? ds.getValue(0, "sdcid") : "";
                    detailTableSDCMap.put(detailtableid, detailSDC);
                }
                String[] dekeys = StringUtil.split(detailkeyvalues, ";");
                if (detailkeycolumns != null && detailkeycolumns.length() > 0) {
                    int i;
                    HashMap<String, String> keyValueMap = new HashMap<String, String>();
                    String[] decols = StringUtil.split(detailkeycolumns, ";");
                    int dekeyIndex = dekeys.length;
                    int dekeyColIndex = decols.length;
                    for (i = dekeyColIndex - dekeyIndex; i < decols.length; ++i) {
                        String keyvalue = dekeys[i - (dekeyColIndex - dekeyIndex)];
                        if (keyvalue == null || keyvalue.length() <= 0) continue;
                        keyValueMap.put(decols[i], dekeys[i - (dekeyColIndex - dekeyIndex)]);
                    }
                    detailkeyvalues = "";
                    for (i = 0; i < ddtdetailkeycolumns.length; ++i) {
                        detailkeyvalues = detailkeyvalues + (i > 0 ? ";" : "") + (String)keyValueMap.get(ddtdetailkeycolumns[i]);
                    }
                    activityDataSet.setValue(a, "detailkeyvalues", detailkeyvalues);
                    dekeys = StringUtil.split(detailkeyvalues, ";");
                }
                if (keyAuditSeqMap.get(primaryKeyValues + ";" + detailkeyvalues) != null) {
                    auditsequence = (Integer)keyAuditSeqMap.get(primaryKeyValues + ";" + detailkeyvalues);
                } else if (!"N".equals(sdcProcessor.getProperty(sdcid, "auditedflag"))) {
                    int i;
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuilder sqlSB = new StringBuilder("SELECT auditsequence FROM " + detailtableid + " WHERE ");
                    StringBuilder commonWhere = new StringBuilder();
                    String[] parentkeycolumnids = detailParentKeyColumnMap.get(detailtableid) != null ? detailParentKeyColumnMap.get(detailtableid) : ddtdetailkeycolumns;
                    boolean isFirst = true;
                    for (i = 0; i < parentkeycolumnids.length; ++i) {
                        String columnid = parentkeycolumnids[i];
                        if ("sdcid".equals(columnid)) {
                            commonWhere.append((isFirst ? "" : " AND ") + "sdcid=" + safeSQL.addVar(sdcid));
                            isFirst = false;
                            continue;
                        }
                        if ("keyid1".equals(columnid) || keycolid1.equals(columnid)) {
                            commonWhere.append((isFirst ? "" : " AND ") + columnid + "=" + safeSQL.addVar(keyid1));
                            isFirst = false;
                            continue;
                        }
                        if (!"(null)".equals(keyid2) && ("keyid2".equals(columnid) || keycolid2.equals(columnid))) {
                            commonWhere.append((isFirst ? "" : " AND ") + columnid + "=" + safeSQL.addVar(keyid2));
                            isFirst = false;
                            continue;
                        }
                        if ("(null)".equals(keyid3) || !"keyid3".equals(columnid) && !keycolid3.equals(columnid)) continue;
                        commonWhere.append((isFirst ? "" : " AND ") + columnid + "=" + safeSQL.addVar(keyid3));
                        isFirst = false;
                    }
                    for (i = 0; i < ddtdetailkeycolumns.length; ++i) {
                        sqlSB.append((i == 0 && commonWhere.length() > 0 ? commonWhere + " AND " : "") + (i > 0 ? " AND " : "") + ddtdetailkeycolumns[i] + "=" + safeSQL.addVar(dekeys[i]));
                    }
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlSB.toString(), safeSQL.getValues());
                    int n = auditsequence = ds.size() == 0 ? 0 : ds.getInt(0, "auditsequence");
                    if (auditsequence < 0) {
                        auditsequence = -1;
                    }
                    keyAuditSeqMap.put(primaryKeyValues + ";" + detailkeyvalues, auditsequence);
                }
            }
            activityDataSet.setNumber(a, "refauditsequence", auditsequence);
            activityDataSet.setValue(a, "activityby", sysuserid);
            String __activitydt = activityDataSet.getValue(a, "__activitydt");
            Timestamp activitydt = defaultactivitydt;
            if (__activitydt != null && __activitydt.length() > 0) {
                long activitytimemillis = Long.parseLong(__activitydt) - serverclienttimeoffset;
                activitydt.setTime(activitytimemillis);
            }
            activityDataSet.setDate(a, "activitydt", activitydt);
            activityDataSet.setValue(a, "reason", reason);
            if (detailTableSDCMap.get(detailtableid) == null || detailTableSDCMap.get(detailtableid).length() <= 0) continue;
            for (int i = 0; i < activityDataSet.getRowCount(); ++i) {
                activityDataSet.setValue(i, "sdcid", detailTableSDCMap.get(detailtableid));
                String[] detailkeys = StringUtil.split(activityDataSet.getValue(i, "detailkeyvalues"), ";");
                if (detailkeys.length > 0) {
                    activityDataSet.setValue(i, "keyid1", detailkeys[0]);
                }
                if (detailkeys.length > 1) {
                    activityDataSet.setValue(i, "keyid1", detailkeys[1]);
                }
                if (detailkeys.length <= 2) continue;
                activityDataSet.setValue(i, "keyid1", detailkeys[2]);
            }
        }
        DBUtil database = new DBUtil();
        database.setConnection(this.sapphireConnection.getDbms(), this.sapphireConnection.getConnection());
        activityDataSet.renameColumn("detailkeycolumns", "__detailkeycolumns");
        DataSetUtil.insert(database, activityDataSet, "activitylog");
    }

    private String parseColumnid(String input) {
        if (input.indexOf("_") > 0) {
            return input.substring(0, input.lastIndexOf("_"));
        }
        return input;
    }
}

