/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class AdhocQueryAdmin {
    private SapphireConnection sapphireConnection = null;

    public AdhocQueryAdmin(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    public void processSaveRequest(HashMap props) throws SapphireException {
        String mode = (String)props.get("mode");
        if ("savesdcsearchableflag".equals(mode)) {
            this.saveSearchableFlag((String)props.get("sdcid"), (String)props.get("searchableflag"));
        } else if ("savecolumnsearchableflag".equals(mode)) {
            String tableid = (String)props.get("tableid");
            this.saveColumnSearchableFlag(tableid, (String)props.get("columnid"), (String)props.get("searchableflag"));
            this.saveColumnTitleChange((String)props.get("titletableid"), (String)props.get("titlecolumnid"), (String)props.get("columndesc"));
            this.editExtendedColumns((String)props.get("editextendedcolumns"));
            this.addExtendedColumns((String)props.get("addextendedcolumns"));
            String deletetableid = (String)props.get("deletetableid");
            String deletecolumnid = (String)props.get("deletecolumnid");
            if (deletetableid != null && deletetableid.length() > 0 && deletecolumnid != null && deletecolumnid.length() > 0) {
                this.deleteExtendedColumn(deletetableid, deletecolumnid);
            }
        } else if ("saveadhocquery".equals(mode)) {
            this.saveAdhocRequest(props);
        } else {
            throw new SapphireException("No mode specified.");
        }
    }

    public void saveAdhocRequest(HashMap props) throws SapphireException {
        String adhocquerydesc;
        String basedonsdcid = (String)props.get("basedonsdcid");
        String override = (String)props.get("override");
        String grouprelationflag = ((String)props.get("betweengroupboolean")).indexOf("a") == 0 ? "a" : "o";
        String searchrequestXML = (String)props.get("searchrequest");
        ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
        HashMap<String, String> actionprops = new HashMap<String, String>();
        String adhocqueryid = "";
        String string = adhocquerydesc = props.get("adhocquerydesc") != null ? (String)props.get("adhocquerydesc") : adhocqueryid;
        if ("Y".equals(override) && ((adhocqueryid = (String)props.get("adhocqueryid")) == null || adhocqueryid.length() == 0)) {
            adhocqueryid = new QueryProcessor(this.sapphireConnection.getConnectionId()).getPreparedSqlDataSet("SELECT adhocqueryid from adhocquery where adhocquerydesc=?", new Object[]{adhocquerydesc}).getString(0, "adhocqueryid");
        }
        actionprops.put("sdcid", "AdhocQuery");
        actionprops.put("keyid1", adhocqueryid);
        actionprops.put("adhocqueryid", adhocqueryid);
        actionprops.put("adhocquerydesc", adhocquerydesc);
        actionprops.put("grouprelationflag", grouprelationflag);
        actionprops.put("basedonsdcid", basedonsdcid);
        if ("Y".equals(override)) {
            ap.processAction("EditSDI", "1", actionprops);
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            db.executePreparedUpdate("Delete from adhocqueryarg where adhocqueryid=?" + ("Y".equals(props.get("fromsearch")) ? " and groupflag !='V' and groupflag!='S'" : ""), new Object[]{adhocqueryid});
        } else {
            actionprops.put("keyid1", "(auto)");
            actionprops.put("adhocqueryid", "(auto)");
            actionprops.put("shareableflag", "N");
            ap.processAction("AddSDI", "1", actionprops);
            adhocqueryid = (String)actionprops.get("newkeyid1");
        }
        actionprops.clear();
        actionprops.put("sdcid", "AdhocQueryArg");
        DataSet requestDataSet = new DataSet();
        requestDataSet.setXML(searchrequestXML);
        StringBuffer keyid1 = new StringBuffer();
        StringBuffer adhocqueryargdesc = new StringBuffer();
        StringBuffer columnid = new StringBuffer();
        StringBuffer groupflag = new StringBuffer();
        StringBuffer groupname = new StringBuffer();
        StringBuffer usersequence = new StringBuffer();
        StringBuffer operator = new StringBuffer();
        StringBuffer value = new StringBuffer();
        StringBuffer argrelationflag = new StringBuffer();
        StringBuffer adhocqueryidlist = new StringBuffer();
        for (int i = 0; i < requestDataSet.getRowCount(); ++i) {
            keyid1.append(";" + adhocqueryid + i + requestDataSet.getValue(i, "group"));
            adhocqueryargdesc.append(";" + requestDataSet.getValue(i, "title"));
            columnid.append(";" + requestDataSet.getValue(i, "columnid"));
            groupflag.append(";" + requestDataSet.getValue(i, "group"));
            groupname.append(";" + requestDataSet.getValue(i, "groupname"));
            usersequence.append(";" + requestDataSet.getValue(i, "groupsequence"));
            operator.append(";" + requestDataSet.getValue(i, "operator"));
            argrelationflag.append(";" + requestDataSet.getValue(i, "argrelationflag"));
            adhocqueryidlist.append(";" + adhocqueryid);
            String type = requestDataSet.getValue(i, "columntype");
            value.append("|%|" + requestDataSet.getValue(i, "value"));
        }
        actionprops.put("adhocqueryargid", keyid1.substring(1));
        actionprops.put("adhocqueryargdesc", adhocqueryargdesc.substring(1));
        actionprops.put("columnid", columnid.substring(1));
        actionprops.put("groupflag", groupflag.substring(1));
        actionprops.put("notes", groupname.substring(1));
        actionprops.put("usersequence", usersequence.substring(1));
        actionprops.put("operator", operator.substring(1));
        actionprops.put("argrelationflag", argrelationflag.substring(1));
        actionprops.put("adhocqueryid", adhocqueryidlist.substring(1));
        ap.processAction("AddSDI", "1", actionprops);
        if (value.length() > 3) {
            DataSet ds = new DataSet();
            ds.addColumnValues("adhocqueryargid", 0, keyid1.substring(1), ";");
            ds.addColumnValues("value", 0, value.substring(3), "|%|");
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            DataSetUtil.update(db, ds, "adhocqueryarg", new String[]{"adhocqueryargid"});
        }
        props.put("adhocqueryid", adhocqueryid);
    }

    private void saveSearchableFlag(String sdcid, String searchableflag) throws SapphireException {
        DataSet ds = new DataSet();
        ds.addColumnValues("sdcid", 0, sdcid, ";");
        ds.addColumnValues("searchableflag", 0, searchableflag, ";");
        DBUtil db = new DBUtil();
        db.setConnection(this.sapphireConnection);
        DataSetUtil.update(db, ds, "sdc", new String[]{"sdcid"});
        this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
    }

    private void saveColumnSearchableFlag(String tableid, String columnid, String searchableflag) throws SapphireException {
        if (tableid != null && tableid.length() > 0) {
            DataSet ds = new DataSet();
            ds.addColumnValues("tableid", 0, tableid, ";");
            ds.addColumnValues("columnid", 0, columnid, ";");
            ds.addColumnValues("searchableflag", 0, searchableflag, ";");
            ds.padColumn("tableid");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                ds.setString(i, "tableid", tableid);
            }
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            DataSetUtil.update(db, ds, "syscolumn", new String[]{"tableid", "columnid"});
            this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
        }
    }

    private void saveColumnTitleChange(String tableid, String columnid, String title) throws SapphireException {
        if (tableid != null && tableid.length() > 0) {
            DataSet ds = new DataSet();
            ds.addColumnValues("tableid", 0, tableid, ";");
            ds.addColumnValues("columnid", 0, columnid, ";");
            ds.addColumnValues("columndesc", 0, title, ";");
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            DataSetUtil.update(db, ds, "syscolumn", new String[]{"tableid", "columnid"});
            this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
        }
    }

    private void editExtendedColumns(String editXML) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setXML(editXML);
        DBUtil db = new DBUtil();
        db.setConnection(this.sapphireConnection);
        DataSetUtil.update(db, ds, "sysextendedcolumn", new String[]{"tableid", "columnid"});
        this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
    }

    private void addExtendedColumns(String addXML) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setXML(addXML);
        DBUtil db = new DBUtil();
        db.setConnection(this.sapphireConnection);
        DataSetUtil.insert(db, ds, "sysextendedcolumn");
        this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
    }

    private void deleteExtendedColumn(String tableid, String columnid) throws SapphireException {
        DBUtil db = new DBUtil();
        db.setConnection(this.sapphireConnection);
        String[] tableids = StringUtil.split(tableid, ";");
        String[] columnids = StringUtil.split(columnid, ";");
        for (int i = 0; i < tableids.length; ++i) {
            db.executePreparedUpdate("Delete from sysextendedcolumn where tableid=? and columnid=?", new Object[]{tableids[i], columnids[i]});
        }
        this.resetHibernateCache(this.sapphireConnection.getDatabaseId());
    }

    public void resetHibernateCache(String databaseid) {
        SapphireHibernateUtil.resetHibernateMappingCache(databaseid);
    }
}

