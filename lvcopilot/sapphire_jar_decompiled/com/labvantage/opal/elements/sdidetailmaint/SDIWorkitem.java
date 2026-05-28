/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailmaint;

import com.labvantage.opal.elements.detailmaint.DBColumn;
import com.labvantage.opal.elements.sdidetailmaint.BaseSDIDetail;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDIWorkitem
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 79326 $";

    public SDIWorkitem(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiworkitem", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIWorkitemPropertyHandler");
    }

    @Override
    public DataSet getDataset(RequestContext context) throws SapphireException {
        PropertyList maintPL;
        PropertyList contextPL;
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        List columnslist = (List)this._ColumnMap.get("columnslist");
        if (!columnslist.contains("appliedflag")) {
            this.addToColumnCollection("appliedflag", "hidden", "N");
            columnslist = (List)this._ColumnMap.get("columnslist");
        }
        Set customlist = (Set)this._ColumnMap.get("customlist");
        List pseudolist = (List)this._ColumnMap.get("pseudolist");
        for (int i = 0; i < columnslist.size(); ++i) {
            String column = (String)columnslist.get(i);
            int dotindex = column.indexOf(".");
            if (StringUtil.getLen(column) == 0L) continue;
            if (column.startsWith("(")) {
                int lastindex = column.lastIndexOf(")");
                columnList.add(new DBColumn(column, false, this._IsOra));
                columnslist.remove(i);
                columnslist.add(i, column.substring(++lastindex).trim());
                customlist.add(column.substring(++lastindex).trim());
                continue;
            }
            if (dotindex != -1) {
                String tableid = column.substring(0, dotindex);
                if (tableid.equalsIgnoreCase("workitemid")) {
                    tableid = "workitem";
                }
                String columnid = column.substring(++dotindex);
                DBColumn c = new DBColumn(columnid, "", columnid, true, this._IsOra);
                c.setPrefix(tableid);
                columnList.add(c);
                columnslist.set(i, columnid);
                continue;
            }
            if (this.doesColumnExists(column)) {
                DBColumn c = new DBColumn(column, "", column, true, this._IsOra);
                c.setPrefix("SDIWORKITEM");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIWorkitem'.");
        }
        String sdcId = this.getSdcid();
        if ((sdcId == null || sdcId.trim().length() == 0) && (contextPL = context.getPropertyList()) != null && (maintPL = contextPL.getPropertyList("maint")) != null) {
            sdcId = maintPL.getProperty("sdcid");
            this.setSdcid(sdcId);
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.getSdcid());
        sdiRequest.setKeyid1List(this.getResolvedKeyid1());
        sdiRequest.setKeyid2List(this.getResolvedKeyid2());
        sdiRequest.setKeyid3List(this.getResolvedKeyid3());
        sdiRequest.setRequestItem("sdiworkitem");
        DataSet dsRequest = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("sdiworkitem");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append(" SDIWORKITEM.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIWORKITEM.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIWORKITEM.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIWORKITEM.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        if (this._IsOra) {
            columnList.add(new DBColumn(" (case  \nwhen SDIWORKITEM.WORKITEMTYPEFLAG = 'W' AND (SDIWORKITEM.GROUPID is not null  OR SDIWORKITEM.GROUPID != '(null)' OR SDIWORKITEM.GROUPID != '') then (Select wi.usersequence from sdiworkitem wi \n  where wi.groupid = SDIWORKITEM.groupid and \n        wi.groupinstance = SDIWORKITEM.groupinstance and \n        wi.workitemtypeflag = 'P' and \n        wi.keyid1 = SDIWORKITEM.keyid1 and wi.keyid2 = SDIWORKITEM.keyid2 AND wi.keyid3 = SDIWORKITEM.keyid3) \nelse SDIWORKITEM.usersequence \nend) modifiedusersequence", false, this._IsOra));
        } else {
            columnList.add(new DBColumn(" (case  \nwhen SDIWORKITEM.WORKITEMTYPEFLAG = 'W' AND (SDIWORKITEM.GROUPID is not null  OR SDIWORKITEM.GROUPID != '(null)' OR SDIWORKITEM.GROUPID != '') then (Select wi.usersequence from sdiworkitem wi \n  where wi.groupid = SDIWORKITEM.groupid and \n        wi.groupinstance = SDIWORKITEM.groupinstance and \n        wi.workitemtypeflag = 'P' and \n        wi.keyid1 = SDIWORKITEM.keyid1 and wi.keyid2 = SDIWORKITEM.keyid2 AND wi.keyid3 = SDIWORKITEM.keyid3) \nelse SDIWORKITEM.usersequence \nend) modifiedusersequence", false, this._IsOra));
        }
        String orderClause = "modifiedusersequence, SDIWORKITEM.workitemtypeflag, SDIWORKITEM.usersequence";
        String query = SDIWorkitem.getSelectSQL(columnList, "SDIWORKITEM LEFT OUTER JOIN WORKITEM ON WORKITEM.WORKITEMID = SDIWORKITEM.WORKITEMID AND WORKITEM.WORKITEMVERSIONID = SDIWORKITEM.WORKITEMVERSIONID ", sb.toString(), orderClause, false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        DataSet _ds = new DataSet();
        for (int i = 0; i < ds.size(); ++i) {
            for (int j = 0; j < dsRequest.size(); ++j) {
                if (!ds.getString(i, "workitemid").equals(dsRequest.getString(j, "workitemid")) || !ds.getBigDecimal(i, "workiteminstance").equals(dsRequest.getBigDecimal(j, "workiteminstance"))) continue;
                _ds.copyRow(ds, i, 1);
            }
        }
        return _ds;
    }

    @Override
    public String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append(" var ").append(this.element.getId()).append("_isWorkitem = true;");
        sb.append("function add").append(this.element.getId()).append("( applyflag ) {");
        sb.append("    if ( applyflag == undefined || applyflag == null ) {");
        sb.append("        _addapplyflag = false;");
        sb.append("    }");
        sb.append("    else {");
        sb.append("        _addapplyflag = applyflag;");
        sb.append("    }");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIWorkitem';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDIWorkitem',sapphire.PRODUCTNAME + ' Lookup', url, 600, 400,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function addinstance").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIWorkitem';");
        sb.append("    addNewWorkitemInstance('").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIWorkitem';");
        sb.append("    removeWorkitem('").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function apply").append(this.element.getId()).append("() {");
        sb.append("    applyWorkitem('").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function retest").append(this.element.getId()).append("() {");
        sb.append("    retestWorkitem('").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("workitemid");
        list.add("workiteminstance");
        list.add("appliedflag");
        return list;
    }
}

