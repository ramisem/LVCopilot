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

public class SDIApproval
extends BaseSDIDetail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 79326 $";

    public SDIApproval(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        super(element, pageContext, requestContext, connectionid);
        this._TableMetaData = OpalUtil.getColumnDataTypeMap("sdiapprovalstep", this.getQueryProcessor());
        this.setPropertyHandlerClass("com.labvantage.opal.elements.sdidetailmaint.handler.SDIApprovalPropertyHandler");
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
                String columnid = column.substring(++dotindex);
                DBColumn c = new DBColumn(columnid, "", columnid, true, this._IsOra);
                c.setPrefix(tableid);
                columnList.add(c);
                columnslist.set(i, columnid);
                continue;
            }
            if (this.doesColumnExists(column)) {
                DBColumn c = new DBColumn(column, "", column, true, this._IsOra);
                c.setPrefix("SDIAPPROVALSTEP");
                columnList.add(c);
                continue;
            }
            String pseudovalue = (String)pseudolist.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + column + "' does not exists in 'SDIApprovalStep'.");
        }
        String sdcId = this.getSdcid();
        if ((sdcId == null || sdcId.trim().length() == 0) && (contextPL = context.getPropertyList()) != null && (maintPL = contextPL.getPropertyList("maint")) != null) {
            sdcId = maintPL.getProperty("sdcid");
            this.setSdcid(sdcId);
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sb = new StringBuffer();
        sb.append(" SDIAPPROVALSTEP.SDCID = ").append(safeSQL.addVar(sdcId));
        sb.append(" AND SDIAPPROVALSTEP.KEYID1 = ").append(safeSQL.addVar(this.getResolvedKeyid1()));
        sb.append(" AND SDIAPPROVALSTEP.KEYID2 = ").append(safeSQL.addVar(this.getResolvedKeyid2()));
        sb.append(" AND SDIAPPROVALSTEP.KEYID3 = ").append(safeSQL.addVar(this.getResolvedKeyid3()));
        sb.append(" AND SDIAPPROVALSTEP.SDCID = SDIAPPROVAL.SDCID");
        sb.append(" AND SDIAPPROVALSTEP.KEYID1 = SDIAPPROVAL.KEYID1");
        sb.append(" AND SDIAPPROVALSTEP.KEYID2 = SDIAPPROVAL.KEYID2");
        sb.append(" AND SDIAPPROVALSTEP.KEYID3 = SDIAPPROVAL.KEYID3");
        sb.append(" AND SDIAPPROVALSTEP.APPROVALTYPEID = SDIAPPROVAL.APPROVALTYPEID");
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            sb.append(" AND ").append(whereClause);
        }
        String query = SDIApproval.getSelectSQL(columnList, "SDIAPPROVAL, SDIAPPROVALSTEP", sb.toString(), "SDIAPPROVALSTEP.USERSEQUENCE", false);
        ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    @Override
    public String getPageScripts() {
        StringBuffer sb = new StringBuffer();
        String lookuppage = this.element.getProperty("additempage");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdidetail';");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIApproval';");
        sb.append("    sapphire.lookup.util.openWindow( 'SDIApproval',sapphire.PRODUCTNAME + ' Lookup', url, 600, 400,").append(this.isSapphireDialog()).append(" , null );");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIApproval';");
        sb.append("    removeSDIApproval('").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function addStep").append(this.element.getId()).append("() {");
        sb.append("    _elementid = '").append(this.element.getId()).append("';");
        sb.append("    _elementtype = 'SDIApproval';");
        sb.append("    addApprovalStep('").append(this.element.getId()).append("' );");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List getRequiredColumnsList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("approvaltypeid");
        list.add("approvalstep");
        list.add("approvalstepinstance");
        return list;
    }
}

