/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ScheduleNodeUtil
extends BaseCustom {
    static final String NODEID_COL = "scheduleplannodeid";
    static final String PARENTNODEID_COL = "parentnodeid";
    static final String DESC_COL = "scheduleplannodedesc";
    static final String SEQUENCE_COL = "usersequence";
    static final String IMAGE_COL = "img";
    static final String REFSDC_COL = "refsdcid";
    static final String REFKEYID1_COL = "refkeyid1";
    static final String REFKEYID2_COL = "refkeyid2";
    static final String REFKEYID3_COL = "refkeyid3";
    static final String FORMPAGEID_COL = "formpageid";
    static final String STATUS_COL = "nodestatus";
    private DBUtil db;
    private boolean isOracle = true;
    private String scheduleplanid;
    private PropertyList props = null;

    public ScheduleNodeUtil(DBUtil db, String scheduleplanid) {
        this.db = db;
        this.scheduleplanid = scheduleplanid;
        if (!db.getDbms().equals("ORA")) {
            this.isOracle = false;
        }
    }

    public ScheduleNodeUtil(DBUtil db, PropertyList props) {
        this.db = db;
        this.props = props;
        this.scheduleplanid = props.getProperty("scheduleplanid");
        if (!db.getDbms().equals("ORA")) {
            this.isOracle = false;
        }
    }

    public void handleRequest() throws SapphireException {
        String scheduleplanid = this.props.getProperty("scheduleplanid");
        String selectednodeid = this.props.getProperty("selectednodeid");
        String selectednodestatus = this.props.getProperty("selectednodestatus");
        String selectednodeparent = this.props.getProperty("selectednodeparent");
        String parentstatus = this.props.getProperty("parentstatus");
        String refsdcid = this.props.getProperty(REFSDC_COL);
        String formpageid = this.props.getProperty(FORMPAGEID_COL);
        String refkeyid1 = this.props.getProperty(REFKEYID1_COL);
        String refkeyid2 = this.props.getProperty(REFKEYID2_COL);
        String refkeyid3 = this.props.getProperty(REFKEYID3_COL);
        String scheduleplannodedesc = this.props.getProperty(DESC_COL);
        String nodesequence = this.props.getProperty("nodesequence");
        String tobemoved = this.props.getProperty("tobemoved");
        String mode = this.props.getProperty("mode");
        String valisdi = this.props.getProperty("valisdi");
        try {
            ScheduleNodeUtil treeUtil = new ScheduleNodeUtil(this.db, scheduleplanid);
            treeUtil.setConnectionId(this.getConnectionId());
            if (valisdi.equals("false")) {
                treeUtil.delinkNodeFromSDIs(selectednodeid);
            } else if (mode.equals("AddNode")) {
                treeUtil.addNodes(selectednodeid, selectednodestatus, scheduleplannodedesc, nodesequence, refsdcid, refkeyid1, refkeyid2, refkeyid3, formpageid);
            } else if (mode.equals("TurnAllOff") || mode.equals("TurnAllOn")) {
                String status = mode.equals("TurnAllOff") ? "X" : "A";
                treeUtil.setStatusForAll(selectednodeid, status);
            } else if (mode.equals("RenameFolder")) {
                treeUtil.renameNode(selectednodeid, scheduleplannodedesc);
            } else if (mode.equals("DeleteFolder")) {
                treeUtil.deleteNode(selectednodeid);
            } else if (mode.indexOf("MoveFolder") == 0) {
                treeUtil.moveNodes(mode, selectednodeid, selectednodestatus, selectednodeparent, parentstatus, tobemoved);
            }
        }
        catch (SapphireException se) {
            Logger.logStackTrace(se);
            throw se;
        }
    }

    private void getAllChildNodes(DataSet ds, String nodeid, ArrayList childlist) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(PARENTNODEID_COL, nodeid);
        DataSet childnodes = ds.getFilteredDataSet(filter);
        for (int i = 0; i < childnodes.getRowCount(); ++i) {
            String templabel = childnodes.getString(i, NODEID_COL);
            childlist.add(templabel);
            this.getAllChildNodes(ds, templabel, childlist);
        }
    }

    private DataSet getChildDataSet(String selectednodeid, boolean hasDesc, boolean hasStatus) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT scheduleplanid, scheduleplannodeid, " + (hasDesc ? "scheduleplannodedesc," : "") + (hasStatus ? "nodestatus," : "") + " parentnodeid, usersequence FROM scheduleplannode WHERE scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND parentnodeid=" + safeSQL.addVar(selectednodeid) + " order by usersequence";
        this.db.createPreparedResultSet("movefolder", sql, safeSQL.getValues());
        DataSet ds = new DataSet(this.db.getResultSet("movefolder"));
        this.db.closeResultSet("movefolder");
        return ds;
    }

    private DataSet getTreeDataSet() throws SapphireException {
        DataSet ds = null;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT scheduleplanid, scheduleplannodeid, scheduleplannodedesc, " + (this.isOracle ? "NVL( parentnodeid, 'root' )" : "isnull( parentnodeid, 'root' )") + " parentnodeid, refsdcid, refkeyid1, refkeyid2, refkeyid3, formpageid, usersequence, nodestatus, (SELECT count(*) FROM scheduleplanitem WHERE scheduleplanitem.scheduleplannodeid = scheduleplannode.scheduleplannodeid ) itemcount FROM scheduleplannode WHERE scheduleplanid=" + safeSQL.addVar(this.scheduleplanid);
        this.db.createPreparedResultSet("nodedataset", sql, safeSQL.getValues());
        ds = new DataSet(this.db.getResultSet("nodedataset"));
        this.db.closeResultSet("nodedataset");
        return ds;
    }

    private void setStatusForAll(String selectednodeid, String status) throws SapphireException {
        ArrayList allchildnodes = new ArrayList();
        this.getAllChildNodes(this.getTreeDataSet(), selectednodeid, allchildnodes);
        DataSet updateDataSet = new DataSet();
        updateDataSet.addColumn("scheduleplanid", 0);
        updateDataSet.addColumn(NODEID_COL, 0);
        updateDataSet.addColumn(STATUS_COL, 0);
        updateDataSet.addRow();
        updateDataSet.setString(0, "scheduleplanid", this.scheduleplanid);
        updateDataSet.setString(0, NODEID_COL, selectednodeid);
        updateDataSet.setString(0, STATUS_COL, status);
        for (int i = 0; i < allchildnodes.size(); ++i) {
            int row = updateDataSet.addRow();
            updateDataSet.setString(row, "scheduleplanid", this.scheduleplanid);
            updateDataSet.setString(row, NODEID_COL, (String)allchildnodes.get(i));
            updateDataSet.setString(row, STATUS_COL, status);
        }
        this.saveDataSet(false, updateDataSet);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < updateDataSet.size(); ++i) {
            if (i == 0) {
                sb.append(updateDataSet.getString(i, NODEID_COL));
                continue;
            }
            sb.append(";" + updateDataSet.getString(i, NODEID_COL));
        }
        String nodelist = sb.toString();
        if (status.equals("X")) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = this.getDeleteEventSqlForNodes(nodelist, safeSQL);
            this.db.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "UPDATE scheduleplanitemexec SET lastscheduledt=null, nextscheduledt=null, scheduledtodt=null, lasteventdt=null WHERE scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND scheduleplanitemid in  ( select scheduleplanitemid from scheduleplanitem where scheduleplanid = " + safeSQL.addVar(this.scheduleplanid) + " and scheduleplannodeid IN (" + safeSQL.addIn(nodelist, ";") + ") )";
            this.db.executePreparedUpdate(sql, safeSQL.getValues());
        } else if (status.equals("A")) {
            SafeSQL safeSQL = new SafeSQL();
            ArrayList<Object> varList = new ArrayList<Object>();
            varList.add(DateTimeUtil.getNowTimestamp());
            varList.add(this.scheduleplanid);
            varList.add(this.scheduleplanid);
            String invarsplaceHolder = safeSQL.addIn(nodelist, ";");
            varList.addAll(Arrays.asList(safeSQL.getValues()));
            this.db.executePreparedUpdate("UPDATE scheduleplanitemexec SET nextscheduledt=? WHERE scheduleplanid=? AND nextscheduledt IS NULL AND scheduleplanitemid in ( select scheduleplanitemid from scheduleplanitem where scheduleplanid = ? and  planitemstatus='A' AND startdt IS NOT NULL AND scheduleplannodeid IN (" + invarsplaceHolder + "))", varList.toArray());
        }
    }

    private String addNodes(String selectednodeid, String selectednodestatus, String scheduleplannodedesc, String nodesequence, String refsdcid, String refkeyid1, String refkeyid2, String refkeyid3, String formpageid) throws SapphireException {
        String nodeadded = "";
        DataSet existingChilds = this.getChildDataSet(selectednodeid, true, true);
        DataSet insertDs = new DataSet();
        insertDs.addColumn("scheduleplanid", 0);
        insertDs.addColumn(PARENTNODEID_COL, 0);
        insertDs.addColumn(FORMPAGEID_COL, 0);
        insertDs.addColumn(REFSDC_COL, 0);
        insertDs.addColumn(DESC_COL, 0);
        insertDs.addColumn(NODEID_COL, 0);
        insertDs.addColumn(STATUS_COL, 0);
        insertDs.addColumnValues(SEQUENCE_COL, 1, nodesequence, ";", "");
        insertDs.addColumnValues(REFKEYID1_COL, 0, refkeyid1, ";", "");
        String[] refkeyid1s = StringUtil.split(refkeyid1, ";");
        String[] refkeyid2s = StringUtil.split(refkeyid2, ";");
        String[] refkeyid3s = StringUtil.split(refkeyid3, ";");
        insertDs.addColumn(REFKEYID2_COL, 0);
        insertDs.addColumn(REFKEYID3_COL, 0);
        String[] names = StringUtil.split(scheduleplannodedesc, ";");
        int rowstoadd = insertDs.getRowCount();
        ArrayList<String> duplist = new ArrayList<String>();
        for (int i = 0; i < rowstoadd; ++i) {
            String tempnodeid = "N" + (System.currentTimeMillis() + (long)i);
            String nodename = names[i].length() != 0 ? names[i] : refkeyid1s[i];
            insertDs.setString(i, NODEID_COL, tempnodeid);
            insertDs.setString(i, DESC_COL, nodename);
            insertDs.setString(i, "scheduleplanid", this.scheduleplanid);
            insertDs.setString(i, PARENTNODEID_COL, selectednodeid);
            insertDs.setString(i, FORMPAGEID_COL, formpageid);
            insertDs.setString(i, REFSDC_COL, refsdcid);
            insertDs.setString(i, STATUS_COL, selectednodeid.equals("root") ? "A" : selectednodestatus);
            if (refsdcid.length() > 0) {
                insertDs.setString(i, REFKEYID2_COL, refkeyid2s.length <= i || refkeyid2s[i].length() == 0 ? "(null)" : refkeyid2s[i]);
                insertDs.setString(i, REFKEYID3_COL, refkeyid3s.length <= i || refkeyid3s[i].length() == 0 ? "(null)" : refkeyid3s[i]);
            }
            nodeadded = nodeadded + ';' + tempnodeid;
            if (existingChilds.findRow(DESC_COL, nodename) >= 0) {
                duplist.add(nodename);
                continue;
            }
            nodeadded = nodeadded + ';' + tempnodeid;
        }
        if (duplist.size() > 0) {
            for (int j = 0; j < duplist.size(); ++j) {
                insertDs.remove(insertDs.findRow(DESC_COL, (String)duplist.get(j)));
            }
        }
        this.saveDataSet(true, insertDs);
        if (nodeadded.indexOf(";") == 0) {
            nodeadded = nodeadded.substring(1);
        }
        return nodeadded;
    }

    private void renameNode(String selectednodeid, String scheduleplannodedesc) throws SapphireException {
        DataSet updateDataSet = new DataSet();
        updateDataSet.addColumn("scheduleplanid", 0);
        updateDataSet.addColumn(NODEID_COL, 0);
        updateDataSet.addColumn(DESC_COL, 0);
        updateDataSet.addRow();
        updateDataSet.setString(0, "scheduleplanid", this.scheduleplanid);
        updateDataSet.setString(0, NODEID_COL, selectednodeid);
        updateDataSet.setString(0, DESC_COL, scheduleplannodedesc);
        this.saveDataSet(false, updateDataSet);
    }

    private void moveNodes(String mode, String selectednodeid, String nodestatus, String selectednodeparent, String parentstatus, String tobemoved) throws SapphireException {
        DataSet updateDataSet = new DataSet();
        updateDataSet.addColumn("scheduleplanid", 0);
        updateDataSet.addColumn(NODEID_COL, 0);
        updateDataSet.addColumn(PARENTNODEID_COL, 0);
        String[] tobemovedlist = StringUtil.split(tobemoved, ";");
        if (mode.equals("MoveFolder")) {
            for (int i = 0; i < tobemovedlist.length; ++i) {
                updateDataSet.addRow();
                updateDataSet.setString(i, "scheduleplanid", this.scheduleplanid);
                updateDataSet.setString(i, NODEID_COL, tobemovedlist[i]);
                updateDataSet.setString(i, PARENTNODEID_COL, selectednodeid);
                if (!nodestatus.equals("X")) continue;
                this.setStatusForAll(tobemovedlist[i], "X");
            }
        } else if (mode.equals("MoveFolderA") || mode.equals("MoveFolderB")) {
            updateDataSet.addColumn(SEQUENCE_COL, 1);
            DataSet existDataSet = null;
            existDataSet = parentstatus.equals("X") ? this.getChildDataSet(selectednodeparent, false, true) : this.getChildDataSet(selectednodeparent, false, false);
            for (int i = 0; i < tobemovedlist.length; ++i) {
                int temprow;
                updateDataSet.addRow();
                updateDataSet.setString(i, "scheduleplanid", this.scheduleplanid);
                updateDataSet.setString(i, NODEID_COL, tobemovedlist[i]);
                updateDataSet.setString(i, PARENTNODEID_COL, selectednodeparent);
                if (parentstatus.equals("X") && !selectednodeparent.equals("root")) {
                    this.setStatusForAll(tobemovedlist[i], "X");
                    updateDataSet.setString(i, STATUS_COL, parentstatus);
                }
                if ((temprow = existDataSet.findRow(NODEID_COL, tobemovedlist[i])) < 0) continue;
                existDataSet.remove(temprow);
            }
            HashMap<String, String> findmap = new HashMap<String, String>();
            findmap.put(NODEID_COL, selectednodeid);
            int row = existDataSet.findRow(findmap);
            if (row >= 0) {
                if (mode.equals("MoveFolderB")) {
                    existDataSet.addAll(row, updateDataSet);
                } else {
                    existDataSet.addAll(row + 1, updateDataSet);
                }
            }
            for (int i = 0; i < existDataSet.getRowCount(); ++i) {
                existDataSet.setNumber(i, SEQUENCE_COL, i);
            }
            updateDataSet = existDataSet;
        }
        this.saveDataSet(false, updateDataSet);
    }

    private void delinkNodeFromSDIs(String selectednodeid) throws SapphireException {
        DataSet updateDataSet = new DataSet();
        updateDataSet.addColumn("scheduleplanid", 0);
        updateDataSet.addColumn(NODEID_COL, 0);
        updateDataSet.addColumn(REFSDC_COL, 0);
        updateDataSet.addColumn(REFKEYID1_COL, 0);
        updateDataSet.addColumn(REFKEYID2_COL, 0);
        updateDataSet.addColumn(REFKEYID3_COL, 0);
        String[] selectednodelist = StringUtil.split(selectednodeid, ";");
        for (int i = 0; i < selectednodelist.length; ++i) {
            updateDataSet.addRow();
            updateDataSet.setString(i, "scheduleplanid", this.scheduleplanid);
            updateDataSet.setString(i, NODEID_COL, selectednodelist[i]);
            updateDataSet.setString(i, REFSDC_COL, "");
            updateDataSet.setString(i, REFKEYID1_COL, "");
            updateDataSet.setString(i, REFKEYID2_COL, "");
            updateDataSet.setString(i, REFKEYID3_COL, "");
        }
        this.saveDataSet(false, updateDataSet);
    }

    private void saveDataSet(boolean isInsert, DataSet ds) throws SapphireException {
        if (isInsert) {
            DataSetUtil.insert(this.db, ds, "scheduleplannode");
        } else {
            DataSetUtil.update(this.db, ds, "scheduleplannode", new String[]{"scheduleplanid", NODEID_COL});
        }
    }

    private void deleteNode(String nodeid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String planItemSql = "select scheduleplanid, scheduleplanitemid FROM scheduleplanitem WHERE scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND scheduleplannodeid in (" + safeSQL.addIn(nodeid, ";") + ")";
        DataSet planItems = this.getQueryProcessor().getPreparedSqlDataSet(planItemSql, safeSQL.getValues());
        if (planItems.getRowCount() > 0) {
            PropertyList deletePlanItems = new PropertyList();
            deletePlanItems.setProperty("sdcid", "SchedulePlanItem");
            deletePlanItems.setProperty("keyid1", planItems.getColumnValues("scheduleplanid", ";"));
            deletePlanItems.setProperty("keyid2", planItems.getColumnValues("scheduleplanitemid", ";"));
            this.getActionProcessor().processAction("DeleteSDI", "1", deletePlanItems);
        }
        safeSQL.reset();
        this.db.executePreparedUpdate("DELETE FROM scheduleplannode WHERE scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND scheduleplannodeid in (" + safeSQL.addIn(nodeid, ";") + ")", safeSQL.getValues());
    }

    private String getDeleteEventSqlForNodes(String nodelist, SafeSQL safeSQL) {
        String sql = null;
        sql = this.isOracle ? "DELETE from scheduleevent where (scheduleplanid, scheduleplanitemid, eventdt) IN (SELECT se.scheduleplanid, se.scheduleplanitemid, se.eventdt from scheduleevent se, scheduleplanitem si WHERE se.scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND si.scheduleplannodeid IN ( " + safeSQL.addIn(nodelist, ";") + ")AND se.scheduleplanid = si.scheduleplanid AND se.scheduleplanitemid = si.scheduleplanitemid AND se.eventstatus='S')" : "DELETE FROM scheduleevent WHERE scheduleplanid in (select se.scheduleplanid from scheduleevent se, scheduleplanitem si WHERE se.scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND si.scheduleplannodeid IN ( " + safeSQL.addIn(nodelist, ";") + ")AND se.scheduleplanid = si.scheduleplanid AND se.scheduleplanitemid = si.scheduleplanitemid AND se.eventstatus='S')AND scheduleplanitemid in (select se.scheduleplanitemid from scheduleevent se, scheduleplanitem si WHERE se.scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND si.scheduleplannodeid IN ( " + safeSQL.addIn(nodelist, ";") + ")AND se.scheduleplanid = si.scheduleplanid AND se.scheduleplanitemid = si.scheduleplanitemid AND se.eventstatus='S')AND eventdt in (select se.eventdt from scheduleevent se, scheduleplanitem si WHERE se.scheduleplanid=" + safeSQL.addVar(this.scheduleplanid) + " AND si.scheduleplannodeid IN ( " + safeSQL.addIn(nodelist, ";") + ")AND se.scheduleplanid = si.scheduleplanid AND se.scheduleplanitemid = si.scheduleplanitemid AND se.eventstatus='S')";
        return sql;
    }
}

