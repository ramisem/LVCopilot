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

public class SDIDataset
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 79326 $";

    public SDIDataset(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdidata", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIDatasetPropertyHandler");
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
                if (tableid.equalsIgnoreCase("paramlistid")) {
                    tableid = "paramlist";
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
                c.setPrefix("SDIDATA");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIData'.");
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.getSdcid());
        sdiRequest.setKeyid1List(this.getResolvedKeyid1());
        sdiRequest.setKeyid2List(this.getResolvedKeyid2());
        sdiRequest.setKeyid3List(this.getResolvedKeyid3());
        sdiRequest.setRequestItem("dataset");
        DataSet dsRequest = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("dataset");
        StringBuffer sb = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sb.append(" SDIDATA.SDCID = ").append(safeSQL.addVar(this._SdcID)).append("");
        sb.append(" AND SDIDATA.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1())).append("");
        sb.append(" AND SDIDATA.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2())).append("");
        sb.append(" AND SDIDATA.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3())).append("");
        sb.append(" AND PARAMLIST.PARAMLISTID = SDIDATA.PARAMLISTID");
        sb.append(" AND PARAMLIST.PARAMLISTVERSIONID = SDIDATA.PARAMLISTVERSIONID");
        sb.append(" AND PARAMLIST.VARIANTID = SDIDATA.VARIANTID");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIDataset.getSelectSQL(columnList, "SDIDATA, PARAMLIST", sb.toString(), "SDIDATA.USERSEQUENCE, SDIDATA.DATASET", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        DataSet _ds = new DataSet();
        for (int i = 0; i < ds.size(); ++i) {
            for (int j = 0; j < dsRequest.size(); ++j) {
                if (!ds.getString(i, "paramlistid").equals(dsRequest.getString(j, "paramlistid")) || !ds.getString(i, "paramlistversionid").equals(dsRequest.getString(j, "paramlistversionid")) || !ds.getString(i, "variantid").equals(dsRequest.getString(j, "variantid")) || !ds.getValue(i, "dataset").equals(dsRequest.getValue(j, "dataset"))) continue;
                _ds.copyRow(ds, i, 1);
            }
        }
        return _ds;
    }

    @Override
    public String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        String edititempage = this.element.getProperty("edititempage");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIData';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDIData',sapphire.PRODUCTNAME + ' Lookup', url, 800, 600,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function copy").append(this.element.getId()).append("() {");
        sb.append("    copyDataset( '").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIData';");
        sb.append("    removeDataset('").append(this.element.getId()).append("');");
        sb.append("}");
        sb.append("function edit").append(this.element.getId()).append("() {");
        sb.append("    editDataset( '").append(this.element.getId()).append("', '").append(edititempage).append("' );");
        sb.append("}");
        sb.append("function retest").append(this.element.getId()).append("() {");
        sb.append("    retestDataset('").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function remeasure").append(this.element.getId()).append("() {");
        sb.append("    remasureDataset('").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("paramlistid");
        list.add("paramlistversionid");
        list.add("variantid");
        list.add("dataset");
        return list;
    }
}

