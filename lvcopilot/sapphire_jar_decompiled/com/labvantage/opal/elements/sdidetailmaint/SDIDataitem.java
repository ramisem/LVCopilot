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

public class SDIDataitem
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 96767 $";
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __VariantID;
    private String __DataSet;

    public SDIDataitem(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdidataitem", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIDataitemPropertyHandler");
    }

    @Override
    public String getScriptVariables() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getScriptVariables());
        sb.append("var ").append(this.element.getId()).append("_defaultparamtype = null;");
        sb.append("var ").append(this.element.getId()).append("_repparamtype = null;");
        if (this.isDatasetModifiable()) {
            sb.append("var ").append(this.element.getId()).append("_modifiable = true;");
        } else {
            sb.append("var ").append(this.element.getId()).append("_modifiable = false;");
        }
        return sb.toString();
    }

    @Override
    public DataSet getDataset(RequestContext context) throws SapphireException {
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        List columnslist = (List)this._ColumnMap.get("columnslist");
        Set customlist = (Set)this._ColumnMap.get("customlist");
        List pseudolist = (List)this._ColumnMap.get("pseudolist");
        boolean isOra = this.getConnectionProcessor().isOra();
        for (int i = 0; i < columnslist.size(); ++i) {
            String column = (String)columnslist.get(i);
            int dotindex = column.indexOf(".");
            if (column == null || column.length() == 0) continue;
            if (column.startsWith("(")) {
                int lastindex = column.lastIndexOf(")");
                columnList.add(new DBColumn(column, false, isOra));
                columnslist.remove(i);
                columnslist.add(i, column.substring(++lastindex).trim());
                customlist.add(column.substring(++lastindex).trim());
                continue;
            }
            if (dotindex != -1) {
                String tableid = column.substring(0, dotindex);
                if (tableid.equalsIgnoreCase("paramid")) {
                    tableid = "param";
                }
                String columnid = column.substring(++dotindex);
                DBColumn c = new DBColumn(columnid, "", columnid, true, isOra);
                c.setPrefix(tableid);
                columnList.add(c);
                columnslist.set(i, columnid);
                continue;
            }
            if (this.doesColumnExists(column)) {
                DBColumn c = new DBColumn(column, "", column, true, isOra);
                c.setPrefix("SDIDATAITEM");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIDataItem'.");
        }
        if (!columnList.contains("enteredtext")) {
            DBColumn c = new DBColumn("enteredtext", "", "enteredtext", true, isOra);
            c.setPrefix("SDIDATAITEM");
            columnList.add(c);
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        sb.append("SDIDATAITEM.SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sb.append(" AND SDIDATAITEM.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIDATAITEM.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIDATAITEM.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND SDIDATAITEM.PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
        sb.append(" AND SDIDATAITEM.PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
        sb.append(" AND SDIDATAITEM.VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
        sb.append(" AND SDIDATAITEM.DATASET = ").append(safeSQL.addVar(this.__DataSet));
        sb.append(" AND SDIDATA.SDCID = SDIDATAITEM.SDCID");
        sb.append(" AND SDIDATA.KEYID1 = SDIDATAITEM.KEYID1");
        sb.append(" AND SDIDATA.KEYID2 = SDIDATAITEM.KEYID2");
        sb.append(" AND SDIDATA.KEYID3 = SDIDATAITEM.KEYID3");
        sb.append(" AND SDIDATA.PARAMLISTID = SDIDATAITEM.PARAMLISTID");
        sb.append(" AND SDIDATA.PARAMLISTVERSIONID = SDIDATAITEM.PARAMLISTVERSIONID");
        sb.append(" AND SDIDATA.VARIANTID = SDIDATAITEM.VARIANTID");
        sb.append(" AND SDIDATA.DATASET = SDIDATAITEM.DATASET");
        sb.append(" AND PARAM.PARAMID = SDIDATAITEM.PARAMID");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIDataitem.getSelectSQL(columnList, "SDIDATAITEM, SDIDATA, PARAM", sb.toString(), "SDIDATAITEM.USERSEQUENCE, SDIDATAITEM.REPLICATEID", false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        if (ds != null && ds.size() > 0 && "Y".equals(this.element.getProperty("disablerrecordondataentry", "Y"))) {
            ds.addColumnValues("_DISABLEDROW", 0, "N", ";");
            ds.padColumn("_DISABLEDROW");
            for (int i = 0; i < ds.size(); ++i) {
                String enteredText = ds.getValue(i, "enteredtext");
                if (enteredText == null || enteredText.length() <= 0 || enteredText.equalsIgnoreCase("(null)")) continue;
                ds.setValue(i, "_DISABLEDROW", "Y");
            }
        }
        return ds;
    }

    @Override
    public String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    if ( !").append(this.element.getId()).append("_modifiable ) {");
        sb.append("        sapphire.alert( 'You are not allowed to modify this Dataset.' );");
        sb.append("        return;");
        sb.append("    }");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIDataItem';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDIDataItem',sapphire.PRODUCTNAME + ' Lookup', url, 800, 600,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    if ( !").append(this.element.getId()).append("_modifiable ) {");
        sb.append("        sapphire.alert( 'You are not allowed to modify this Dataset.' );");
        sb.append("        return;");
        sb.append("    }");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIDataItem';");
        sb.append("    removeDataitem( '").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function addreplicate").append(this.element.getId()).append("() {");
        sb.append("    if ( !").append(this.element.getId()).append("_modifiable ) {");
        sb.append("        sapphire.alert( 'You are not allowed to modify this Dataset.' );");
        sb.append("        return;");
        sb.append("    }");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIDataItem';");
        sb.append("    addReplicate( '").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }

    public List setRequestObjects(PropertyList pagedata) {
        ArrayList<String> list = new ArrayList<String>();
        this.setParamlistid(OpalUtil.isNotEmpty(pagedata.getProperty("paramlistid")) && !pagedata.getProperty("paramlistid").equals("undefined") ? pagedata.getProperty("paramlistid") : pagedata.getProperty("action_paramlistid"));
        this.setParamlistversionid(OpalUtil.isNotEmpty(pagedata.getProperty("paramlistversionid")) && !pagedata.getProperty("paramlistversionid").equals("undefined") ? pagedata.getProperty("paramlistversionid") : pagedata.getProperty("action_paramlistversionid"));
        this.setVariantid(OpalUtil.isNotEmpty(pagedata.getProperty("variantid")) && !pagedata.getProperty("variantid").equals("undefined") ? pagedata.getProperty("variantid") : pagedata.getProperty("action_variantid"));
        this.setDataset(OpalUtil.isNotEmpty(pagedata.getProperty("dataset")) && !pagedata.getProperty("dataset").equals("undefined") ? pagedata.getProperty("dataset") : (OpalUtil.isNotEmpty(pagedata.getProperty("action_dataset")) ? pagedata.getProperty("action_dataset") : pagedata.getProperty("datasetnumber")));
        if (this._SdcID == null) {
            this._SdcID = "Sample";
        }
        if (this._KeyID1 == null || this._KeyID1.equals("(null)")) {
            this._KeyID1 = pagedata.getProperty("keyid1");
            if (this._KeyID1 == null || this._KeyID1.equals("(null)")) {
                list.add("keyid1");
            }
        }
        if (this.__ParamListID == null || this.__ParamListID.equals("null")) {
            list.add("paramlistid");
        }
        if (this.__ParamListVersionID == null || this.__ParamListVersionID.equals("null")) {
            list.add("paramlistversionid");
        }
        if (this.__VariantID == null || this.__VariantID.equals("null")) {
            list.add("variantid");
        }
        if (this.__DataSet == null || this.__DataSet.equals("null")) {
            list.add("dataset");
        }
        return list;
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("paramid");
        list.add("paramtype");
        list.add("replicateid");
        return list;
    }

    private boolean isDatasetModifiable() {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MODIFIABLEFLAG FROM SDIDATA");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(this.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(this.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(this.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(this.getKeyid3()));
        sql.append(" AND PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
        sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
        sql.append(" AND VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
        sql.append(" AND DATASET = ").append(safeSQL.addVar(this.__DataSet));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.size() > 0) {
            if (ds.getValue(0, "MODIFIABLEFLAG").equalsIgnoreCase("Y")) {
                return true;
            }
        } else {
            safeSQL.reset();
            sql.setLength(0);
            sql.append("SELECT MODIFIABLEFLAG FROM PARAMLIST");
            sql.append(" WHERE PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
            sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
            sql.append(" AND VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds.getValue(0, "MODIFIABLEFLAG").equalsIgnoreCase("Y")) {
                return true;
            }
        }
        return false;
    }

    public void setParamlistid(String parParamlistid) {
        this.__ParamListID = parParamlistid;
    }

    public void setParamlistversionid(String parParamlistversionid) {
        this.__ParamListVersionID = parParamlistversionid;
    }

    public void setVariantid(String parVariantid) {
        this.__VariantID = parVariantid;
    }

    public void setDataset(String parDataset) {
        this.__DataSet = parDataset;
    }
}

