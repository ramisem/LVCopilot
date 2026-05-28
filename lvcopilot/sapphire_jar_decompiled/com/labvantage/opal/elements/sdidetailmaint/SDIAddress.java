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

public class SDIAddress
extends BaseSDIDetail {
    public static String LABVANTAGE_CVS_ID = "$Revision: 97180 $";

    protected SDIAddress(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiaddress", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIAddressPropertyHandler");
    }

    @Override
    public DataSet getDataset(RequestContext context) throws SapphireException {
        PropertyList maintPL;
        PropertyList contextPL;
        DataSet ds = null;
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        List columnslist = (List)this._ColumnMap.get("columnslist");
        Set customlist = (Set)this._ColumnMap.get("customlist");
        List pseudolist = (List)this._ColumnMap.get("pseudolist");
        for (int i = 0; i < columnslist.size(); ++i) {
            String column = (String)columnslist.get(i);
            int dotindex = column.indexOf(".");
            if (column == null || column.length() == 0) continue;
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
                if (tableid.equalsIgnoreCase("addressid")) {
                    tableid = "ADDRESS";
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
                c.setPrefix("SDIADDRESS");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIAddress'.");
        }
        String sdcId = this.getSdcid();
        if ((sdcId == null || sdcId.trim().length() == 0) && (contextPL = context.getPropertyList()) != null && (maintPL = contextPL.getPropertyList("maint")) != null) {
            sdcId = maintPL.getProperty("sdcid");
            this.setSdcid(sdcId);
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append("SDIADDRESS.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIADDRESS.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIADDRESS.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIADDRESS.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND ADDRESS.ADDRESSID = SDIADDRESS.ADDRESSID");
        sb.append(" AND ADDRESS.ADDRESSTYPE = SDIADDRESS.ADDRESSTYPE");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIAddress.getSelectSQL(columnList, "SDIADDRESS, ADDRESS", sb.toString(), "SDIADDRESS.USERSEQUENCE", false);
        ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    @Override
    protected List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("addressid");
        list.add("addresstype");
        list.add("contactfunction");
        return list;
    }

    @Override
    protected String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("function add").append(this.element.getId()).append("( width, height ) {");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIAddress';");
        sb.append("    var _width = typeof( width ) != 'undefined'? width:800;");
        sb.append("    var _height = typeof( height ) != 'undefined'? height:600;");
        sb.append("    sapphire.lookup.util.openWindow( 'Add',sapphire.PRODUCTNAME + ' Lookup', url, _width, _height,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function edit").append(this.element.getId()).append("( width, height ) {");
        sb.append("    editSDIAddress( '").append(this.element.getId()).append("', '").append(this.element.getProperty("edititempage", "rc?command=page&page=LV_AddAddress")).append("', width, height );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIAddress';");
        sb.append("    removeSDIAddress('").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }
}

