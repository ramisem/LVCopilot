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

public class SDIFormRule
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    protected SDIFormRule(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiformrule", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIFormRulePropertyHandler");
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
                if (tableid.equalsIgnoreCase("formid")) {
                    tableid = "FORM";
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
                c.setPrefix("SDIFORMRULE");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIFormRule'.");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append(" SDIFORMRULE.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIFORMRULE.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIFORMRULE.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIFORMRULE.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND FORM.FORMID = SDIFORMRULE.FORMID");
        sb.append(" AND ( FORM.FORMVERSIONID = SDIFORMRULE.FORMVERSIONID");
        sb.append(" OR SDIFORMRULE.FORMVERSIONID IS NULL )");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIFormRule.getSelectSQL(columnList, "SDIFORMRULE, FORM", sb.toString(), "SDIFORMRULE.USERSEQUENCE, SDIFORMRULE.FORMID", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    @Override
    protected String getPageScripts() {
        StringBuilder sb = new StringBuilder();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("var _elementid_SDIFormRule = '").append(this.element.getId()).append("';");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIFormRule';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDIFormRule',sapphire.PRODUCTNAME + ' Lookup', url, 800, 600,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIFormRule';");
        sb.append("    removeSDIFormRule('").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }

    @Override
    protected List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("formid");
        list.add("formversionid");
        return list;
    }
}

