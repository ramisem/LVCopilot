/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.detailmaint;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.elements.detailmaint.DBColumn;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIItem
extends BaseItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 97450 $";
    protected boolean _AutoKeyFlag;
    protected String _ErrorMsg = "";
    protected PropertyListCollection _MasterKeyCollection;
    protected PropertyListCollection _DetailKeyCollection;

    @Override
    protected void initElement() {
        this.setPropertyHandlerClass("com.labvantage.opal.elements.detailmaint.SDIItemPropertyHandler");
    }

    @Override
    protected String getMainHtml() {
        PropertyList keylist;
        String autokey;
        CMTPolicy cmtPolicy;
        String enablechangecontrol;
        StringBuffer sb = new StringBuffer();
        String tableid = this.element.getProperty("tableid");
        String lookuppage = this.element.getProperty("addnewitempage");
        if (!this.getSdcid().equals("Instrument") && "s_sdicertification".equalsIgnoreCase(tableid) && "Y".equals(enablechangecontrol = (cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "")).getPolicyPropertyList().getProperty("enablechangecontrol"))) {
            String whereclause = this.element.getProperty("whereclause");
            if ("User".equals(this.getSdcid())) {
                if (!"User".equalsIgnoreCase(cmtPolicy.getCertificationsOwner()) && whereclause.toLowerCase().startsWith("certifiedforsdcid='")) {
                    this.setMode("View");
                }
            } else if ("User".equalsIgnoreCase(cmtPolicy.getCertificationsOwner())) {
                this.setMode("View");
            }
        }
        if ((autokey = this.element.getProperty("autokey")) != null && autokey.equals("Y")) {
            this._AutoKeyFlag = true;
        }
        this._MasterKeyCollection = this.element.getCollection("masterkeycol");
        if (this._MasterKeyCollection == null || this._MasterKeyCollection.size() == 0) {
            return SDIItem.toErrorString("Element Configuration Error", "No Master key defined");
        }
        if (!this.validateMasterKeyCollection(this._MasterKeyCollection)) {
            return SDIItem.toErrorString("Element Configuration Error", "No or Invalid Master Key collection. Missing Request Parameter value.");
        }
        this.putCollectionToPropertyHandler(this._MasterKeyCollection, "masterkeys", new String[]{"columnid", "requestparam"});
        this._DetailKeyCollection = this.element.getCollection("detailkeycol");
        if (this._DetailKeyCollection == null || this._DetailKeyCollection.size() == 0) {
            return SDIItem.toErrorString("Element Configuration Error", "No Detail key defined");
        }
        this.putCollectionToPropertyHandler(this._DetailKeyCollection, "detailkeys", new String[]{"columnid"});
        if (this._AutoKeyFlag && this._ColumnsList.contains((keylist = this._DetailKeyCollection.getPropertyList(0)).getProperty("columnid"))) {
            return SDIItem.toErrorString("Element Configuration Error", "Detail key is auto generated and can not be included in column collection");
        }
        if (!this.validateDetailKeyCollection()) {
            if (!this._AutoKeyFlag) {
                if (lookuppage.trim().length() > 0) {
                    return SDIItem.toErrorString("Element Configuration Error", "Detail key must be included in Column collection");
                }
                return SDIItem.toErrorString("Element Configuration Error", "Detail is not auto generated and must be included in column collection");
            }
            return SDIItem.toErrorString("Element Configuration Error", "Auto generated Detail key can not have more than one column");
        }
        if (lookuppage == null) {
            lookuppage = "";
        }
        this._TableMetaData = OpalUtil.getColumnDataTypeMap(tableid, this.getQueryProcessor());
        DataSet ds = null;
        try {
            ds = this.getDataSet();
        }
        catch (SapphireException e) {
            return SDIItem.toErrorString("Data Error", e.getMessage());
        }
        sb.append(this.getIncludeScripts());
        sb.append(this.renderHtml(ds));
        this.putPropertyHandlerKey("ecolumns", this.convertToPropertyHandlerKey(this._ColumnsList));
        this.putPropertyHandlerKey("tableid", tableid);
        this.putPropertyHandlerKey("customcolumns", OpalUtil.toDelimitedString(this._CustomList, ";"));
        this.putPropertyHandlerKey("defaultvalues", OpalUtil.toDelimitedString(this._DefaultValueList, ";"));
        this.putPropertyHandlerKey("autokeyflag", this._AutoKeyFlag ? "Y" : "N");
        this.putPropertyHandlerKey("tablemetadata", OpalUtil.map2String(this._TableMetaData));
        sb.append(this.getPropertyHandlerFields());
        sb.append("<script language='Javascript'>");
        sb.append(this.getPageScripts());
        sb.append(this.getOperations("sdiitem"));
        sb.append("</script>");
        return sb.toString();
    }

    protected DataSet getDataSet() throws SapphireException {
        String columnid;
        int i;
        String tableid = this.element.getProperty("tableid");
        ArrayList<DBColumn> columnList = new ArrayList<DBColumn>();
        ArrayList<String> whereList = new ArrayList<String>();
        SafeSQL safeSQL = new SafeSQL();
        for (i = 0; i < this._ColumnsList.size(); ++i) {
            columnid = (String)this._ColumnsList.get(i);
            if (columnid == null || columnid.length() == 0) continue;
            if (columnid.startsWith("(")) {
                int lastindex = columnid.lastIndexOf(")");
                columnList.add(new DBColumn(columnid, false, this._IsOra));
                this._ColumnsList.remove(i);
                this._ColumnsList.add(i, columnid.substring(++lastindex).trim());
                continue;
            }
            if (this.doesColumnExists(columnid)) {
                columnList.add(new DBColumn(columnid, "", columnid, true, this._IsOra));
                continue;
            }
            String pseudovalue = (String)this._PseudoList.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + columnid.toUpperCase() + "' does not exists in table '" + tableid.toUpperCase() + "'");
        }
        for (i = 0; i < this._MasterKeyCollection.size(); ++i) {
            columnid = this._MasterKeyCollection.getPropertyList(i).getProperty("columnid");
            String requestparam = this._MasterKeyCollection.getPropertyList(i).getProperty("requestparam");
            if (requestparam == null || requestparam.trim().length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translatePartial("{{No mapping found for Master Key}} '" + columnid + "'"));
            }
            String columnvalue = this.requestContext.getProperty(requestparam);
            if (columnvalue == null || columnvalue.length() == 0) {
                if ("keyid1".equals(requestparam)) {
                    throw new SapphireException(this.getTranslationProcessor().translatePartial("{{No value found for request parameter}} '" + requestparam + "'"));
                }
                if ("sdcid".equals(requestparam)) {
                    columnvalue = this.requestContext.getProperty("__sdcid");
                    if (columnvalue == null || columnvalue.length() == 0) {
                        throw new SapphireException(this.getTranslationProcessor().translatePartial("{{No value found for request parameter}} '" + requestparam + "'"));
                    }
                } else {
                    columnvalue = "(null)";
                }
            }
            whereList.add(columnid + "=" + safeSQL.addVar(columnvalue));
        }
        String whereClause = this.element.getProperty("whereclause");
        if (whereClause != null && whereClause.trim().length() > 0) {
            HashMap<String, String> hmContent = new HashMap<String, String>();
            hmContent.put("currentuser", currentUser);
            whereClause = SDIItem.getSubstitutedContent(whereClause, hmContent);
            whereList.add(whereClause);
        }
        PropertyListCollection sortbycol = this.element.getCollection("sortby");
        StringBuilder sbOrderBy = new StringBuilder();
        if (sortbycol != null && sortbycol.size() > 0) {
            for (int i2 = 0; i2 < sortbycol.size(); ++i2) {
                PropertyList sortbylist = sortbycol.getPropertyList(i2);
                if (sortbylist != null) {
                    String sortbycolumnid = sortbylist.getProperty("columnid").trim();
                    String sortbyorder = sortbylist.getProperty("asc_desc").trim();
                    if (sortbycolumnid.length() > 0) {
                        sbOrderBy.append(sortbycolumnid);
                        if ("a".equals(sortbyorder)) {
                            sbOrderBy.append(" ASC");
                        } else {
                            sbOrderBy.append(" DESC");
                        }
                    } else {
                        throw new SapphireException("No Column defined in Order By clause");
                    }
                }
                sbOrderBy.append(",");
            }
            if (sbOrderBy.length() > 0) {
                sbOrderBy.deleteCharAt(sbOrderBy.length() - 1);
            }
        } else if (this.doesColumnExists("usersequence")) {
            sbOrderBy.append("USERSEQUENCE");
        }
        if (tableid.equalsIgnoreCase("sdidataitem") && this._ColumnsList.contains("displayvalue")) {
            if (!this._ColumnsList.contains("datatypes")) {
                columnList.add(new DBColumn("datatypes", "", "datatypes", true, this._IsOra));
            }
            if (!this._ColumnsList.contains("transformvalue")) {
                columnList.add(new DBColumn("transformvalue", "", "transformvalue", true, this._IsOra));
            }
            if (!this._ColumnsList.contains("transformdt")) {
                columnList.add(new DBColumn("transformdt", "", "transformdt", true, this._IsOra));
            }
            if (!this._ColumnsList.contains("displayformat")) {
                columnList.add(new DBColumn("displayformat", "", "displayformat", true, this._IsOra));
            }
            if (!this._ColumnsList.contains("displayvalueformat")) {
                columnList.add(new DBColumn("displayvalueformat", "", "displayvalueformat", true, this._IsOra));
            }
        }
        String query = SDIItem.getSelectSQL(columnList, tableid, whereList, sbOrderBy.toString(), false);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query, safeSQL.getValues());
        if (tableid.equalsIgnoreCase("sdidataitem") && this._ColumnsList.contains("displayvalue")) {
            I18nUtil.localizeDisplayValues(ds, HttpUtil.getConnectionInfo(this.pageContext));
        }
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + query, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        safeSQL = new SafeSQL();
        String sql = "select columnid from syscolumnproperty where tableid = " + safeSQL.addVar(tableid) + " and propertyid = 'timezoneindependent' and propertyvalue = 'Y'";
        DataSet columnds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (OpalUtil.isNotEmpty(columnds)) {
            for (int i3 = 0; i3 < columnds.getRowCount(); ++i3) {
                String columnid2 = columnds.getString(i3, "columnid");
                this.timeZoneIndependentColumnList.add(columnid2);
                ds.setTimeZoneInsensitive(columnid2);
            }
        }
        return ds;
    }

    protected String getPageScripts() {
        PropertyList keylist;
        int i;
        StringBuffer sb = new StringBuffer();
        String id = this.element.getId();
        String lookuppage = this.element.getProperty("addnewitempage");
        sb.append("var ").append(id).append("_tableid = '").append(id).append("';");
        sb.append("var ").append(id).append("_appearance = '").append(this.getElementAppearance()).append("';");
        sb.append("var ").append(id).append("_mode = '").append(this.getMode()).append("';");
        sb.append("var ").append(id).append("_modified = false;");
        sb.append("var ").append(id).append("_initflag = false;");
        sb.append("var ").append(id).append("_collapseindex = -1;");
        sb.append("var ").append(id).append("_collapseflag = false;");
        sb.append("var isSapphireDialog = '").append(this.isSapphireDialog()).append("';");
        if (this._AutoKeyFlag) {
            sb.append("var ").append(id).append("_autokeyflag = 'true';");
        } else {
            sb.append("var ").append(id).append("_autokeyflag = 'false';");
        }
        sb.append("var elementid = '';");
        sb.append("function add").append(id).append("() {\n");
        if (lookuppage != null && lookuppage.trim().length() > 0) {
            sb.append("    var url = '").append(lookuppage).append("&lookupcallback=addNewItem_sdiitem';\n");
            sb.append("    elementid = '").append(id).append("';");
            sb.append("    sapphire.lookup.util.openWindow( 'SDIItem',sapphire.PRODUCTNAME + ' Lookup', url, 600, 400,").append(this.isSapphireDialog()).append(" , null );");
        } else {
            sb.append("    insertNewItem_sdiitem( '").append(id).append("' );");
        }
        sb.append("}\n");
        sb.append("function remove").append(this.element.getId()).append("() {\n");
        sb.append("    removeSelectedRows_sdiitem( '").append(this.element.getId()).append("');");
        sb.append("}\n");
        sb.append("var ").append(id).append("_masterkeycount = '").append(this._MasterKeyCollection.size()).append("';");
        for (i = 0; i < this._MasterKeyCollection.size(); ++i) {
            keylist = this._MasterKeyCollection.getPropertyList(i);
            sb.append("var ").append(id).append("_masterkey_").append(i).append(" = '").append(keylist.getProperty("columnid")).append("';");
        }
        sb.append("var ").append(id).append("_detailkeycount = '").append(this._DetailKeyCollection.size()).append("';");
        for (i = 0; i < this._DetailKeyCollection.size(); ++i) {
            keylist = this._DetailKeyCollection.getPropertyList(i);
            sb.append("var ");
            sb.append(id);
            sb.append("_detailkey_");
            sb.append(i);
            sb.append(" = '");
            sb.append(keylist.getProperty("columnid"));
            sb.append("';");
            sb.append("var ");
            sb.append(id);
            sb.append("_detaildefault_");
            sb.append(i);
            sb.append(" = '");
            sb.append(keylist.getProperty("default"));
            sb.append("';");
        }
        return sb.toString();
    }

    @Override
    protected String getIncludeScripts() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getIncludeScripts());
        if (!"Y".equals(this.pageContext.getAttribute("sdiitem.jsinclude"))) {
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/detailmaint/scripts/sdiitem.js'></script>");
            this.pageContext.setAttribute("sdiitem.jsinclude", (Object)"Y");
        }
        return sb.toString();
    }

    protected boolean validateDetailKeyCollection() {
        if (!this._AutoKeyFlag) {
            for (int i = 0; i < this._DetailKeyCollection.size(); ++i) {
                PropertyList keylist = this._DetailKeyCollection.getPropertyList(i);
                if (this._ColumnsList.contains(keylist.getProperty("columnid"))) continue;
                return false;
            }
        } else {
            if (this._DetailKeyCollection.size() != 1) {
                return false;
            }
            this.addToColumnCollection(this._DetailKeyCollection.getPropertyList(0).getProperty("columnid"), "hidden", "");
        }
        return true;
    }

    private boolean isSapphireDialog() {
        return this.element.getProperty("dialogtype", "Browser Popup").equalsIgnoreCase("Sapphire Dialog");
    }
}

