/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.linkedsdimaint;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LinkedSDIItem
extends BaseItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103595 $";
    private PropertyListCollection _ForeignKeyCollection;
    private String _SDCIDKey;
    private String _SDCTableID;
    private String _RSetId;
    private boolean _AutoKeyFlag;

    @Override
    protected void initElement() {
        this.setPropertyHandlerClass("com.labvantage.opal.elements.linkedsdimaint.LinkedSDIItemPropertyHandler");
    }

    @Override
    protected String getMainHtml() {
        StringBuilder sb = new StringBuilder();
        String lookuppage = this.element.getProperty("linkitemlookup");
        String sdcid = this.element.getProperty("sdcid");
        if (sdcid.trim().length() == 0) {
            return this.getTranslationProcessor().translate("Error: No SDCID found.");
        }
        this.setSdcid(sdcid);
        HashMap sdcProperties = this.getSDCProcessor().getSDCProperties(this.getSdcid());
        this._SDCTableID = (String)sdcProperties.get("tableid");
        String auditFlag = (String)sdcProperties.get("auditedflag");
        if (!"N".equalsIgnoreCase(auditFlag)) {
            this.putPropertyHandlerKey("audit", "Y");
        } else {
            this.putPropertyHandlerKey("audit", "N");
        }
        String keyRule = (String)sdcProperties.get("keygenerationrule");
        if (keyRule != null && keyRule.trim().length() > 0 && keyRule.charAt(0) == 'A') {
            this._AutoKeyFlag = true;
        }
        this._SDCIDKey = (String)sdcProperties.get("keycolid1");
        if (this._SDCIDKey == null || this._SDCIDKey.equals("")) {
            return LinkedSDIItem.toErrorString("Element Configuration Error", "No SDC key found");
        }
        if (!this._AutoKeyFlag && !this._ColumnsList.contains(this._SDCIDKey)) {
            if (lookuppage.trim().length() > 0) {
                return LinkedSDIItem.toErrorString("Element Configuration Error", "SDC key id must be included in Column collection");
            }
            return LinkedSDIItem.toErrorString("Element Configuration Error", "SDC Key id is not auto generated and must be included in column collection");
        }
        if (!this._AutoKeyFlag && this._ColumnsList.contains(this._SDCIDKey) && this._ModeList.get(this._ColumnsList.indexOf(this._SDCIDKey)) != null && ((String)this._ModeList.get(this._ColumnsList.indexOf(this._SDCIDKey))).equals("hidden")) {
            return LinkedSDIItem.toErrorString("Element Configuration Error", "Error: SDC Key id is not auto generated and should not be included in 'hidden' mode in column collection.");
        }
        if (this._AutoKeyFlag && !this._ColumnsList.contains(this._SDCIDKey)) {
            this.addToColumnCollection(this._SDCIDKey, "hidden", "");
        }
        this._ForeignKeyCollection = this.element.getCollection("foreignkeycol");
        if (this._ForeignKeyCollection == null || this._ForeignKeyCollection.size() == 0 || !this.validateMasterKeyCollection(this._ForeignKeyCollection)) {
            return LinkedSDIItem.toErrorString("Element Configuration Error", "No or Invalid Foreign Key collection. Missing Request Parameter value.");
        }
        this.putCollectionToPropertyHandler(this._ForeignKeyCollection, "foreignkeys", new String[]{"columnid", "requestparam"});
        if (lookuppage == null) {
            lookuppage = "";
        }
        this._TableMetaData = OpalUtil.getColumnDataTypeMap(this._SDCTableID, this.getQueryProcessor());
        DataSet ds = null;
        try {
            ds = this.getDataSet();
        }
        catch (SapphireException e) {
            return LinkedSDIItem.toErrorString("Data Error", e.getMessage());
        }
        sb.append(this.renderHtml(ds));
        this.putPropertyHandlerKey("sdcid", this.getSdcid());
        this.putPropertyHandlerKey("keycolid1", this._SDCIDKey);
        this.putPropertyHandlerKey("ecolumns", this.convertToPropertyHandlerKey(this._ColumnsList));
        this.putPropertyHandlerKey("eunlink", "");
        this.putPropertyHandlerKey("customcolumns", OpalUtil.toDelimitedString(this._CustomList, ";"));
        this.putPropertyHandlerKey("autokeyflag", this._AutoKeyFlag ? "Y" : "N");
        this.putPropertyHandlerKey("tablemetadata", OpalUtil.map2String(this._TableMetaData));
        sb.append(this.getPropertyHandlerFields());
        sb.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linkelement_").append(this.getSdcid()).append("' value='").append(this.elementid).append("'/>\n");
        sb.append("<input type='hidden' name='forward_rset_");
        sb.append(this.element.getId());
        sb.append("' id='forward_rset_");
        sb.append(this.element.getId());
        sb.append("' value='");
        sb.append(this._RSetId);
        sb.append("'>\n");
        if (this._RSetId != null) {
            sb.append("<script  type=\"text/javascript\">\n");
            sb.append("    sapphire.connection.pingRset('").append(this._RSetId).append("');\n");
            sb.append("</script>\n");
        }
        sb.append(this.getIncludeScripts());
        sb.append("<script language='Javascript'>");
        sb.append(this.getPageScripts());
        sb.append(this.getOperations("linkedsdi"));
        sb.append("</script>");
        return sb.toString();
    }

    public DataSet getDataSet() throws SapphireException {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.getSdcid());
        sdiRequest.setQueryFrom(this._SDCTableID);
        ArrayList<String> columnsWithAlias = new ArrayList<String>();
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < this._ForeignKeyCollection.size(); ++i) {
            PropertyList foreignKeyList = this._ForeignKeyCollection.getPropertyList(i);
            String requestParam = this.requestContext.getProperty(foreignKeyList.getProperty("requestparam"));
            String columnId = foreignKeyList.getProperty("columnid");
            if ("".equals(requestParam)) continue;
            whereClause.append(" ").append(columnId).append(" = '").append(SafeSQL.encodeForSQL(requestParam, this._IsOra)).append("' and");
        }
        if (whereClause.length() > 0) {
            String elementWhereClause = this.element.getProperty("whereclause", "");
            if (elementWhereClause != null && elementWhereClause.trim().length() > 0) {
                whereClause.append(" ").append(elementWhereClause);
            } else {
                whereClause.setLength(whereClause.length() - 3);
            }
        }
        sdiRequest.setQueryWhere(whereClause.toString());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this._ColumnsList.size(); ++i) {
            String c = (String)this._ColumnsList.get(i);
            if (c == null || c.length() == 0) continue;
            String column = c.trim();
            int dotIndex = column.indexOf(".");
            if (column.startsWith("(")) {
                int lastindex = column.lastIndexOf(")");
                sb.append(column).append(",");
                this._ColumnsList.remove(i);
                this._ColumnsList.add(i, column.substring(++lastindex).trim());
                columnsWithAlias.add(column.substring(++lastindex).trim());
                continue;
            }
            if (dotIndex != -1 && dotIndex > 0 && dotIndex < column.length() - 1) {
                String sdccolumnid = column.substring(0, dotIndex);
                int lastIndex = column.lastIndexOf(" ");
                SafeSQL safeSQL = new SafeSQL();
                String sql = " select linksdcid from sdclink where sdcid = " + safeSQL.addVar(this._SdcID) + " and sdccolumnid = " + safeSQL.addVar(sdccolumnid);
                DataSet sdclinkDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (sdclinkDS == null || sdclinkDS.size() < 1) {
                    throw new SapphireException("Invalid Column '" + column.toUpperCase() + "'.");
                }
                sdclinkDS = null;
                sb.append(column).append(",");
                this._ColumnsList.remove(i);
                if (lastIndex != -1) {
                    String col = column.substring(0, lastIndex).trim();
                    String colAlias = column.substring(++lastIndex).trim();
                    if (col.contains(" ")) {
                        throw new SapphireException("Invalid Column '" + column.toUpperCase() + "'");
                    }
                    if (this._ColumnsList.contains(colAlias)) {
                        throw new SapphireException("Column alias of '" + column.toUpperCase() + "' already exists.");
                    }
                    this._ColumnsList.add(i, colAlias);
                    this._CustomList.add(colAlias);
                    continue;
                }
                this._ColumnsList.add(i, column);
                this._CustomList.add(column);
                continue;
            }
            if (this.doesColumnExists(c)) {
                sb.append(c).append(",");
                continue;
            }
            String pseudovalue = (String)this._PseudoList.get(i);
            if (pseudovalue != null && pseudovalue.trim().length() > 0) continue;
            throw new SapphireException("Column '" + c.toUpperCase() + "' does not exists in SDC '" + this.getSdcid() + "'");
        }
        sb.setLength(sb.length() - 1);
        sdiRequest.setShowTemplates(true);
        sdiRequest.setRequestItem("primary[" + sb.toString() + "]");
        PropertyListCollection sortbycol = this.element.getCollection("sortby");
        StringBuilder sbOrderBy = new StringBuilder();
        if (sortbycol != null && sortbycol.size() > 0) {
            for (int i = 0; i < sortbycol.size(); ++i) {
                PropertyList sortbylist = sortbycol.getPropertyList(i);
                if (sortbylist != null) {
                    String sortbycolumnid = sortbylist.getProperty("columnid").trim();
                    String sortbyorder = sortbylist.getProperty("asc_desc").trim();
                    if (sortbycolumnid != null && sortbycolumnid.length() > 0) {
                        if (!this.doesColumnExists(sortbycolumnid) && !columnsWithAlias.contains(sortbycolumnid)) {
                            throw new SapphireException("Invalid Column '" + sortbycolumnid + "' found in Order By clause.");
                        }
                        if (columnsWithAlias.contains(sortbycolumnid)) {
                            sbOrderBy.append(sortbycolumnid);
                        } else {
                            sbOrderBy.append(this._SDCTableID).append(".").append(sortbycolumnid);
                        }
                        if (sortbyorder != null && sortbyorder.equals("a")) {
                            sbOrderBy.append(" ASC");
                        } else {
                            sbOrderBy.append(" DESC");
                        }
                    } else {
                        throw new SapphireException("No Column defined in Order By clause.");
                    }
                }
                sbOrderBy.append(",");
            }
            if (sbOrderBy.length() > 0) {
                sbOrderBy.deleteCharAt(sbOrderBy.length() - 1);
            }
        } else if (this.doesColumnExists("usersequence")) {
            sbOrderBy.append(this._SDCTableID).append(".").append("usersequence");
        }
        sdiRequest.setQueryOrderBy(sbOrderBy.toString());
        DataSet dsPrimary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        if (dsPrimary != null && dsPrimary.size() > 0) {
            String tableid = this.getSDCProcessor().getProperty(this.getSdcid(), "tableid");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select columnid from syscolumnproperty where tableid = " + safeSQL.addVar(tableid) + " and propertyid = 'timezoneindependent' and propertyvalue = 'Y'";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String columnid = ds.getString(i, "columnid");
                    this.timeZoneIndependentColumnList.add(columnid);
                    dsPrimary.setTimeZoneInsensitive(columnid);
                }
            }
            DAMProcessor dam = this.getDAMProcessor();
            String rsetid = this.requestContext.getProperty("forward_rset_" + this.element.getId());
            if (rsetid != null && rsetid.length() > 0) {
                dam.touchRSet(rsetid);
                this._RSetId = rsetid;
            } else if ("View".equals(this.getMode())) {
                this._RSetId = dam.createRSet(this.getSdcid(), dsPrimary.getColumnValues(this._SDCIDKey, ";"), "", "");
            } else {
                try {
                    this._RSetId = dam.createLockedRSet(this.getSdcid(), dsPrimary.getColumnValues(this._SDCIDKey, ";"), "", "");
                }
                catch (SapphireException e) {
                    dsPrimary.addColumnValues("_DISABLEDROW", 0, "Y", ";");
                    dsPrimary.padColumn("_DISABLEDROW");
                }
            }
        }
        return dsPrimary;
    }

    protected String getPageScripts() {
        StringBuilder sb = new StringBuilder();
        String lookuppage = this.element.getProperty("linkitemlookup");
        String editurl = this.element.getProperty("linkitemediturl");
        sb.append("var ").append(this.element.getId()).append("_tableid = '").append(this.element.getId()).append("';");
        sb.append("var ").append(this.element.getId()).append("_appearance = '").append(this.getElementAppearance()).append("';");
        sb.append("var ").append(this.element.getId()).append("_mode = '").append(this.getMode()).append("';");
        sb.append("var ").append(this.element.getId()).append("_modified = false;");
        sb.append("var ").append(this.element.getId()).append("_initflag = false;");
        sb.append("var ").append(this.element.getId()).append("_collapseindex = -1;");
        sb.append("var ").append(this.element.getId()).append("_collapseflag = false;");
        sb.append("var ").append(this.element.getId()).append("_sdcid = '").append(this.getSdcid()).append("';");
        sb.append("var ").append(this.element.getId()).append("_sdckey = '").append(this.getSDCProcessor().getSDCProperties(this.getSdcid()).get("keycolid1")).append("';");
        sb.append("var ").append(this.element.getId()).append("_sdckeydefault = '").append(this.getSDCProcessor().getSDCProperties(this.getSdcid()).get("keycolid1")).append("';");
        sb.append("var ").append(this.element.getId()).append("_editurl = '").append(OpalUtil.parseRequestString(this.requestContext, editurl)).append("';");
        if (this._AutoKeyFlag) {
            sb.append("var ").append(this.element.getId()).append("_autokeyflag = true;");
        } else {
            sb.append("var ").append(this.element.getId()).append("_autokeyflag = false;");
        }
        sb.append("var elementid = '").append(this.element.getId()).append("';");
        sb.append("var isSapphireDialog = '").append(this.isSapphireDialog()).append("';");
        sb.append("function add").append(this.element.getId()).append("() {");
        sb.append("    insertNewItem_linkedsdi( '").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function edit").append(this.element.getId()).append("() {");
        sb.append("    edit_linkedsdi( '").append(this.element.getId()).append("' );");
        sb.append("}");
        sb.append("function link").append(this.element.getId()).append("() {");
        if (lookuppage != null && lookuppage.trim().length() > 0) {
            sb.append("    var url = '").append(OpalUtil.parseRequestString(this.requestContext, lookuppage));
            if (!lookuppage.contains("&lookupcallback=")) {
                sb.append("&lookupcallback=linkNewItem_linkedsdi");
            }
            sb.append("';");
            sb.append("    elementid = '").append(this.element.getId()).append("';");
            sb.append("sapphire.lookup.util.openWindow( 'LinkedSDIItem',sapphire.PRODUCTNAME + ' Lookup', url, 800, 600,").append(this.isSapphireDialog()).append(", null );");
        } else {
            sb.append("    insertNewItem_linkedsdi( '").append(this.element.getId()).append("' );");
        }
        sb.append("}");
        sb.append("function unlink").append(this.element.getId()).append("() {");
        sb.append("    unlinkSelectedRows_linkedsdi( '").append(this.element.getId()).append("');");
        sb.append("}");
        sb.append("function remove").append(this.element.getId()).append("() {");
        sb.append("    removeSelectedRows_linkedsdi( '").append(this.element.getId()).append("');");
        sb.append("}");
        sb.append("var ").append(this.element.getId()).append("_foreignkeycount = '").append(this._ForeignKeyCollection.size()).append("';");
        for (int i = 0; i < this._ForeignKeyCollection.size(); ++i) {
            PropertyList keylist = this._ForeignKeyCollection.getPropertyList(i);
            sb.append("var ").append(this.element.getId()).append("_foreignkey_").append(i).append(" = '").append(keylist.getProperty("columnid")).append("';");
        }
        if (this._RSetId != null) {
            sb.append("sdiAddRSet( '").append(this._RSetId).append("' );");
        }
        return sb.toString();
    }

    @Override
    protected String getIncludeScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getIncludeScripts());
        sb.append("<script language='JavaScript' src='WEB-OPAL/elements/linkedsdimaint/scripts/linkedsdiitem.js'>").append("</script>");
        return sb.toString();
    }

    private boolean isSapphireDialog() {
        return this.element.getProperty("dialogtype", "Browser Popup").equalsIgnoreCase("Sapphire Dialog");
    }
}

