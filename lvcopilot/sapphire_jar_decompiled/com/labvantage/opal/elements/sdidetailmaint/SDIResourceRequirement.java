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
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SDIResourceRequirement
extends BaseSDIDetail {
    public SDIResourceRequirement(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiresourcerequirement", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIResourceRequirementPropertyHandler");
    }

    @Override
    public DataSet getDataset(RequestContext context) throws SapphireException {
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        List columnslist = (List)this._ColumnMap.get("columnslist");
        Set customlist = (Set)this._ColumnMap.get("customlist");
        List pseudolist = (List)this._ColumnMap.get("pseudolist");
        for (int i = 0; i < columnslist.size(); ++i) {
            String column = (String)columnslist.get(i);
            if (column.length() == 0) continue;
            if (column.startsWith("(")) {
                int lastindex = column.lastIndexOf(")");
                columnList.add(new DBColumn(column, false, this._IsOra));
                columnslist.remove(i);
                columnslist.add(i, column.substring(++lastindex).trim());
                customlist.add(column.substring(++lastindex).trim());
                continue;
            }
            if (this.doesColumnExists(column)) {
                DBColumn c = new DBColumn(column, "", column, true, this._IsOra);
                c.setPrefix("SDIRESOURCEREQUIREMENT");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIResourceRequirement'.");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append("SDIRESOURCEREQUIREMENT.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIRESOURCEREQUIREMENT.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIRESOURCEREQUIREMENT.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIRESOURCEREQUIREMENT.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIResourceRequirement.getSelectSQL(columnList, "SDIRESOURCEREQUIREMENT", sb.toString(), "SDIRESOURCEREQUIREMENT.USERSEQUENCE", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    @Override
    public String getPageScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append("function add").append(this.element.getId()).append("(){");
        sb.append("_elementid='").append(this.element.getId()).append("';");
        sb.append("_elementtype='SDIResourceRequirement';");
        sb.append("addNewItem_sdidetail('',false);");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIResourceRequirement';");
        sb.append("    var list = getSelectedRowID( '").append(this.element.getId()).append("' );");
        sb.append("    if ( list.length <= 0 ) {");
        sb.append("        top.showMessage(top.selectAtleastOneItemMsg);");
        sb.append("        return false;");
        sb.append("    }");
        sb.append("    removeSelectedRows( '").append(this.element.getId()).append("', false );");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("resourcenum");
        return list;
    }
}

