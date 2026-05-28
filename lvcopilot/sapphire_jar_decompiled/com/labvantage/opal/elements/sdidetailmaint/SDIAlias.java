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
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SDIAlias
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77321 $";

    public SDIAlias(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdialias", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIAliasPropertyHandler");
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
                c.setPrefix("SDIALIAS");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIAlias'.");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append("SDIALIAS.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIALIAS.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIALIAS.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIALIAS.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIAlias.getSelectSQL(columnList, "SDIALIAS", sb.toString(), "SDIALIAS.USERSEQUENCE", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        this.maskAliasData(ds, this.getSdcid(), this.getResolvedKeyid1(), this.getResolvedKeyid2(), this.getResolvedKeyid3());
        return ds;
    }

    @Override
    public String getPageScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append("function add").append(this.element.getId()).append("(){");
        sb.append("_elementid='").append(this.element.getId()).append("';");
        sb.append("_elementtype='SDIAlias';");
        sb.append("addSDIAlias(_elementid);");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("(){");
        sb.append("_elementid='").append(this.element.getId()).append("';");
        sb.append("_elementtype='SDIAlias';");
        sb.append("removeSDIAlias(_elementid);");
        sb.append("}");
        sb.append("sapphire.events.attachEvent(window,'onload',new Function( 'setUpAliasGrid(\"").append(this.element.getId()).append("\");' ));");
        return sb.toString();
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("aliasid");
        list.add("aliastype");
        return list;
    }

    private void maskAliasData(DataSet ds, String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        if (!ds.isValidColumn("sdcid")) {
            ds.setString(-1, "sdcid", sdcId);
        }
        if (!ds.isValidColumn("keyid1")) {
            ds.setString(-1, "keyid1", keyId1);
        }
        if (!ds.isValidColumn("keyid2")) {
            ds.setString(-1, "keyid2", keyId2);
        }
        if (!ds.isValidColumn("keyid3")) {
            ds.setString(-1, "keyid3", keyId3);
        }
        DataMaskUtil dmu = new DataMaskUtil(this.pageContext);
        dmu.maskSDIAliasDataSet(ds, true);
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.isMasked(i, "aliasid")) {
                ds.setString(i, "_DISABLEDROW", "Y");
                continue;
            }
            ds.setString(i, "_DISABLEDROW", "N");
        }
    }
}

