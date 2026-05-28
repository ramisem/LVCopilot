/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.detailmaint;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SDIItemPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 62756 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    @Override
    protected void saveData() throws SapphireException {
        block55: {
            String _tableid = (String)this._ElementProps.get("tableid");
            StringBuffer keycols = new StringBuffer();
            Iterator<Object> iterator = null;
            ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
            ElementData elementData = new ElementData(elementColumns, this._Edata);
            SapphireException exception = null;
            BaseDetailPropertyHandler.Pair masterKey = new BaseDetailPropertyHandler.Pair((String)this._ElementProps.get("masterkeys"));
            List masterkeylist = masterKey.getKeysAsList();
            BaseDetailPropertyHandler.Key detailKey = new BaseDetailPropertyHandler.Key((String)this._ElementProps.get("detailkeys"));
            String autoKeyFlag = (String)this._ElementProps.get("autokeyflag");
            ArrayList<Object> keys = new ArrayList<Object>();
            for (Object masterColumn : masterKey.keySet()) {
                if (keys.contains(masterColumn)) continue;
                keycols.append((String)masterColumn).append(";");
                keys.add(masterColumn);
            }
            for (String aDetailKey1 : detailKey) {
                if (keys.contains(aDetailKey1)) continue;
                keycols.append(aDetailKey1).append(";");
                keys.add(aDetailKey1);
            }
            keycols.deleteCharAt(keycols.length() - 1);
            List<String> __CustomList = OpalUtil.toList((String)this._ElementProps.get("customcolumns"), ";");
            iterator = __CustomList.iterator();
            while (iterator.hasNext()) {
                elementData.removeColumn((String)iterator.next());
            }
            elementData.removeAll(masterkeylist);
            elementData.removeColumn("createdt");
            elementData.removeColumn("createby");
            elementData.removeColumn("createtool");
            elementData.removeColumn("moddt");
            elementData.removeColumn("modby");
            elementData.removeColumn("modtool");
            elementData.removeColumn("usersequence");
            DBUtil db = new DBUtil();
            try {
                db.setConnection(this.sapphireConnection);
                StringBuilder sql = new StringBuilder();
                StringBuilder whereclause = new StringBuilder();
                whereclause.append(masterKey.getWhereClause(elementData));
                String[] removekey = StringUtil.split(HttpUtil.decodeURIComponent((String)this._ElementProps.get("eremove")), ";");
                for (int i = 0; i < removekey.length - 1; ++i) {
                    String _removekey = StringUtil.replaceAll(removekey[i], "-!-q-!-", ";");
                    if (_removekey == null || _removekey.trim().length() <= 0) continue;
                    ArrayList<String> keylist = new ArrayList<String>();
                    sql.delete(0, sql.length());
                    sql.append("DELETE FROM ").append(_tableid).append(" WHERE ");
                    sql.append(whereclause.toString());
                    if (!_removekey.contains("|")) {
                        for (Object aDetailKey : detailKey) {
                            sql.append(" AND ").append(aDetailKey).append(" = ?");
                            keylist.add(_removekey);
                        }
                    } else {
                        String[] _removekeyarray = StringUtil.split(_removekey, "|");
                        for (int j = 0; j < detailKey.size(); ++j) {
                            sql.append(" AND ").append((String)detailKey.get(j)).append(" = ?");
                            keylist.add(_removekeyarray[j]);
                        }
                    }
                    db.executePreparedUpdate(sql.toString(), keylist.toArray());
                    if (!this._TableMD.doesColumnExists("tracelogid") || this._TraceLogId == null || this._TraceLogId.length() <= 0) continue;
                    keylist.clear();
                    StringBuilder updQry = new StringBuilder();
                    updQry.append("UPDATE a_").append(_tableid);
                    updQry.append(" SET tracelogid = '").append(this._TraceLogId).append("'");
                    updQry.append(" , modtool = 'detailmaint'");
                    updQry.append(" WHERE ");
                    updQry.append(whereclause.toString());
                    if (!_removekey.contains("|")) {
                        for (Object aDetailKey : detailKey) {
                            updQry.append(" AND ").append(aDetailKey).append(" = ?");
                            keylist.add(_removekey);
                        }
                    } else {
                        String[] _removekeyarray = StringUtil.split(_removekey, "|");
                        for (int j = 0; j < detailKey.size(); ++j) {
                            updQry.append(" AND ").append((String)detailKey.get(j)).append(" = ?");
                            keylist.add(_removekeyarray[j]);
                        }
                    }
                    updQry.append(" AND tracelogid = 'DELETED' ");
                    try {
                        db.executePreparedUpdate(updQry.toString(), keylist.toArray());
                        continue;
                    }
                    catch (SapphireException e) {
                        this.logError("Unable to update audit table" + e);
                    }
                }
                DataSet dsinsert = new DataSet(this.connectionInfo);
                DataSet dsupdate = new DataSet(this.connectionInfo);
                List columnList = elementData.getColumnList();
                int updateRowCount = elementData.getStatusRowCount("E");
                int insertRowCount = elementData.getStatusRowCount("N");
                for (Object aColumnList : columnList) {
                    String updatevaluelist;
                    String insertvaluelist;
                    String columnid = (String)aColumnList;
                    if (columnid.equals("__status") || columnid.length() <= 0) continue;
                    String columndatatype = this._TableMD.getDataType(columnid);
                    if (columndatatype.equals("C")) {
                        if (insertRowCount > 0) {
                            insertvaluelist = elementData.getColumnDataBuffer(columnid, ";", "N");
                            if (!keys.contains(columnid)) {
                                insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "");
                            }
                            dsinsert.addColumnValues(columnid, 0, insertvaluelist, ";");
                        }
                        if (updateRowCount <= 0) continue;
                        updatevaluelist = elementData.getColumnDataBuffer(columnid, ";", "E");
                        if (!keys.contains(columnid)) {
                            updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "");
                        }
                        dsupdate.addColumnValues(columnid, 0, updatevaluelist, ";");
                        continue;
                    }
                    if (columndatatype.equals("D")) {
                        if (insertRowCount > 0) {
                            dsinsert.addColumnValues(columnid, 2, elementData.getColumnDataBuffer(columnid, ";", "N"), ";");
                        }
                        if (updateRowCount <= 0) continue;
                        dsupdate.addColumnValues(columnid, 2, elementData.getColumnDataBuffer(columnid, ";", "E"), ";");
                        continue;
                    }
                    if (!columndatatype.equals("N") && !columndatatype.equalsIgnoreCase("R")) continue;
                    if (insertRowCount > 0) {
                        insertvaluelist = elementData.getColumnDataBuffer(columnid, ";", "N");
                        insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "0");
                        dsinsert.addColumnValues(columnid, 1, insertvaluelist, ";");
                    }
                    if (updateRowCount <= 0) continue;
                    updatevaluelist = elementData.getColumnDataBuffer(columnid, ";", "E");
                    updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "0");
                    dsupdate.addColumnValues(columnid, 1, updatevaluelist, ";");
                }
                if (autoKeyFlag.equals("Y") && dsinsert != null && dsinsert.size() > 0) {
                    StringBuffer key = new StringBuffer();
                    String temp = _tableid;
                    if (temp.length() > 20) {
                        temp = temp.substring(0, 20);
                    }
                    SequenceProcessor sequenceProcessor = new SequenceProcessor(this.connectionInfo.getConnectionId());
                    for (int i = 0; i < dsinsert.size(); ++i) {
                        key.append(OpalUtil.getNextSequence(temp, sequenceProcessor)).append(";");
                    }
                    key = key.deleteCharAt(key.length() - 1);
                    dsinsert.addColumnValues((String)detailKey.get(0), 0, key.toString(), ";");
                }
                if (this._TableMD.doesColumnExists("createdt") && dsinsert.size() > 0) {
                    dsinsert.addColumn("createdt", 2);
                    dsinsert.setDate(0, "createdt", DateTimeUtil.getNowCalendar());
                    dsinsert.padColumn("createdt");
                }
                if (this._TableMD.doesColumnExists("createby") && dsinsert.size() > 0) {
                    dsinsert.addColumn("createby", 0);
                    dsinsert.setString(0, "createby", this.connectionInfo.getSysuserId());
                    dsinsert.padColumn("createby");
                }
                if (this._TableMD.doesColumnExists("createtool") && dsinsert.size() > 0) {
                    dsinsert.addColumn("createtool", 0);
                    dsinsert.setString(0, "createtool", "detailmaint");
                    dsinsert.padColumn("createtool");
                }
                if (this._TableMD.doesColumnExists("moddt") && dsupdate.size() > 0) {
                    dsupdate.addColumn("moddt", 2);
                    dsupdate.setDate(0, "moddt", DateTimeUtil.getNowCalendar());
                    dsupdate.padColumn("moddt");
                }
                if (this._TableMD.doesColumnExists("modby") && dsupdate.size() > 0) {
                    dsupdate.addColumn("modby", 0);
                    dsupdate.setString(0, "modby", this.connectionInfo.getSysuserId());
                    dsupdate.padColumn("modby");
                }
                if (this._TableMD.doesColumnExists("modtool") && dsupdate.size() > 0) {
                    dsupdate.addColumn("modtool", 0);
                    dsupdate.setString(0, "modtool", "detailmaint");
                    dsupdate.padColumn("modtool");
                }
                if (this._TableMD.doesColumnExists("tracelogid") && this._TraceLogId != null && this._TraceLogId.length() > 0) {
                    if (dsupdate.size() > 0) {
                        dsupdate.addColumn("tracelogid", 0);
                        dsupdate.setString(0, "tracelogid", this._TraceLogId);
                        dsupdate.padColumn("tracelogid");
                    }
                    if (dsinsert.size() > 0) {
                        dsinsert.addColumn("tracelogid", 0);
                        dsinsert.setString(0, "tracelogid", this._TraceLogId);
                        dsinsert.padColumn("tracelogid");
                    }
                }
                if (dsinsert.size() > 0) {
                    if (this._TableMD.doesColumnExists("usersequence")) {
                        dsinsert.addColumnValues("usersequence", 0, elementData.getSequenceBuffer("N", ";"), ";");
                    }
                    DataSetUtil.insert(db, dsinsert, _tableid);
                }
                if (dsupdate.size() <= 0) break block55;
                if (this._TableMD.doesColumnExists("usersequence")) {
                    dsupdate.addColumnValues("usersequence", 0, elementData.getSequenceBuffer("E", ";"), ";");
                }
                sql.setLength(0);
                sql.append("select ");
                for (int i = 0; i < dsupdate.getColumnCount(); ++i) {
                    String columnid = dsupdate.getColumnId(i);
                    if (updateExcludeColumnList.contains(columnid)) continue;
                    sql.append(columnid).append(",");
                }
                sql.setLength(sql.length() - 1);
                sql.append(" from ").append(_tableid);
                sql.append(" where ");
                boolean prependAnd = false;
                SafeSQL safeSQL = new SafeSQL();
                for (Object aMasterkeylist : masterkeylist) {
                    String masterkey = (String)aMasterkeylist;
                    if (prependAnd) {
                        sql.append(" and ");
                    }
                    sql.append(" ").append(masterkey).append(" = ").append(safeSQL.addVar(dsupdate.getValue(0, masterkey)));
                    prependAnd = true;
                }
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                DataSet updateDataSet = new DataSet();
                if (ds != null && ds.size() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (int i = 0; i < dsupdate.size(); ++i) {
                        boolean updateRow = false;
                        filter.clear();
                        for (String string : keys) {
                            filter.put(string, dsupdate.getValue(i, string));
                        }
                        int row = ds.findRow(filter);
                        if (row != -1) {
                            void var30_47;
                            boolean bl = false;
                            while (var30_47 < dsupdate.getColumnCount()) {
                                String columnid = dsupdate.getColumnId((int)var30_47);
                                if (!(keys.contains(columnid) || updateExcludeColumnList.contains(columnid) || ds.getValue(row, columnid).equals(dsupdate.getValue(i, columnid)))) {
                                    updateRow = true;
                                    break;
                                }
                                ++var30_47;
                            }
                            if (!updateRow) continue;
                            updateDataSet.copyRow(dsupdate, i, 1);
                            continue;
                        }
                        updateDataSet.copyRow(dsupdate, i, 1);
                    }
                } else {
                    for (int i = 0; i < dsupdate.size(); ++i) {
                        updateDataSet.copyRow(dsupdate, i, 1);
                    }
                }
                if (updateDataSet.size() > 0) {
                    DataSetUtil.update(db, updateDataSet, _tableid, StringUtil.split(keycols.toString(), ";"));
                }
            }
            catch (SapphireException e) {
                if (e.getMessage().contains("unique constraint")) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Duplicate entries found"));
                }
                exception = e;
            }
            finally {
                db.reset();
                if (exception == null) break block55;
                throw new SapphireException(exception);
            }
        }
    }
}

