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

public class SDIWorkflowRule
extends BaseSDIDetail {
    protected SDIWorkflowRule(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiworkflowrule", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIWorkflowRulePropertyHandler");
    }

    @Override
    public DataSet getDataset(RequestContext context) throws SapphireException {
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        List columnslist = (List)this._ColumnMap.get("columnslist");
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
                if (tableid.equalsIgnoreCase("workflowdefid")) {
                    tableid = "workflowdef";
                } else if (tableid.equalsIgnoreCase("taskdefid")) {
                    tableid = "taskdef";
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
                c.setPrefix("SDIWORKFLOWRULE");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIWorkflowRule'.");
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.getSdcid());
        sdiRequest.setKeyid1List(this.getResolvedKeyid1());
        sdiRequest.setKeyid2List(this.getResolvedKeyid2());
        sdiRequest.setKeyid3List(this.getResolvedKeyid3());
        sdiRequest.setRequestItem("sdiworkflowrule");
        DataSet dsRequest = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("sdiworkflowrule");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append(" SDIWORKFLOWRULE.SDCID = ").append(safeSQL.addVar(this._SdcID));
        sb.append(" AND SDIWORKFLOWRULE.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIWORKFLOWRULE.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIWORKFLOWRULE.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND WORKFLOWDEF.WORKFLOWDEFID = SDIWORKFLOWRULE.WORKFLOWDEFID");
        sb.append(" AND WORKFLOWDEF.WORKFLOWDEFVERSIONID = SDIWORKFLOWRULE.WORKFLOWDEFVERSIONID");
        sb.append(" AND WORKFLOWDEF.WORKFLOWDEFVARIANTID = SDIWORKFLOWRULE.WORKFLOWDEFVARIANTID");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIWorkflowRule.getSelectSQL(columnList, "SDIWORKFLOWRULE, WORKFLOWDEF", sb.toString(), "SDIWORKFLOWRULE.WORKFLOWDEFID, SDIWORKFLOWRULE.WORKFLOWDEFVARIANTID", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        DataSet _ds = new DataSet();
        for (int i = 0; i < ds.size(); ++i) {
            for (int j = 0; j < dsRequest.size(); ++j) {
                if (!ds.getString(i, "workflowdefid").equals(dsRequest.getString(j, "workflowdefid")) || !ds.getString(i, "workflowdefversionid").equals(dsRequest.getString(j, "workflowdefversionid")) || !ds.getString(i, "workflowdefvariantid").equals(dsRequest.getString(j, "workflowdefvariantid"))) continue;
                _ds.copyRow(ds, i, 1);
            }
        }
        return _ds;
    }

    @Override
    protected List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("workflowdefid");
        list.add("workflowdefversionid");
        list.add("workflowdefvariantid");
        list.add("taskdefitemid");
        list.add("ioitemid");
        list.add("workflowexecid");
        return list;
    }

    @Override
    protected String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    top.showWorkflowEntryPopup( '" + (this.getSdcid().equalsIgnoreCase("WorkItem") ? "" : this.getSdcid()) + "', add").append(this.element.getId()).append("_callback );");
        sb.append("}");
        sb.append("function add").append(this.element.getId()).append("_callback( rowdata ) {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIWorkflowRule';");
        sb.append("    addNewItem_sdidetail( rowdata );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIWorkflowRule';");
        sb.append("    var list = getSelectedRowID( '").append(this.element.getId()).append("' );");
        sb.append("    if ( list.length <= 0 ) {");
        sb.append("        top.showMessage(top.selectAtleastOneItemMsg);");
        sb.append("        return false;");
        sb.append("    }");
        sb.append("    removeSelectedRows( '").append(this.element.getId()).append("', false );");
        sb.append("}");
        return sb.toString();
    }
}

