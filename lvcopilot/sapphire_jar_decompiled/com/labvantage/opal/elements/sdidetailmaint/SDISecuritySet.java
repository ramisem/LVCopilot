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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDISecuritySet
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 79326 $";

    public SDISecuritySet(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdisecurityset", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDISecuritySetPropertyHandler");
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
                String columnid = column.substring(++dotindex);
                DBColumn c = new DBColumn(columnid, "", columnid, true, this._IsOra);
                c.setPrefix(tableid);
                columnList.add(c);
                columnslist.set(i, columnid);
                continue;
            }
            if (this.doesColumnExists(column)) {
                DBColumn c = new DBColumn(column, "", column, true, this._IsOra);
                c.setPrefix("SDISECURITYSET");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDISECURITYSET'.");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append(" SDISECURITYSET.SDCID = ").append(safeSQL.addVar(this._SdcID));
        sb.append(" AND SDISECURITYSET.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDISECURITYSET.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDISECURITYSET.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND SECURITYSET.SECURITYSETID = SDISECURITYSET.SECURITYSET");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDISecuritySet.getSelectSQL(columnList, "SDISECURITYSET, SECURITYSET", sb.toString(), "SDISECURITYSET.SECURITYSET", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    @Override
    protected String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDISecuritySet';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDISecuritySet',sapphire.PRODUCTNAME + ' Lookup', url, 800, 600,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDISecuritySet';");
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
    protected List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("securityset");
        return list;
    }
}

