/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.qcbatchitemmaint;

import com.labvantage.opal.elements.detailmaint.SDIItem;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QCBatchItem
extends SDIItem {
    @Override
    protected void initElement() {
        String blockFlag;
        this.setPropertyHandlerClass("com.labvantage.opal.elements.qcbatchitemmaint.QCBatchItemPropertyHandler");
        this.setMandatoryProperties();
        String qcBatchId = (String)this.pageContext.getAttribute("keyid1");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("GetQCBatchBlockFlag", "SELECT blockflag FROM s_qcbatch WHERE s_qcbatchid = ?", new Object[]{qcBatchId});
        if (ds != null && ds.getRowCount() > 0 && "Y".equalsIgnoreCase(blockFlag = ds.getString(0, "blockflag", ""))) {
            this._Mode = "View";
        }
    }

    protected void setMandatoryProperties() {
        this.element.setProperty("autokey", "Y");
        this.element.setProperty("tableid", "s_qcbatchitem");
        PropertyListCollection collection = new PropertyListCollection();
        PropertyList list = new PropertyList();
        list.setProperty("columnid", "s_qcbatchitemid");
        collection.add(0, list);
        this.element.setProperty("detailkeycol", collection);
        collection = new PropertyListCollection();
        list = new PropertyList();
        list.setProperty("columnid", "s_qcbatchid");
        list.setProperty("requestparam", "keyid1");
        collection.add(0, list);
        this.element.setProperty("masterkeycol", collection);
    }

    @Override
    protected String getPageScripts() {
        PropertyList keylist;
        int i;
        StringBuffer sb = new StringBuffer();
        String id = this.element.getId();
        String lookuppage = this.element.getProperty("addnewitempage");
        String lookupSelFromPriorBatchPage = this.element.getProperty("addfrompriorqcbatchpage");
        String lookupQCSamplePage = this.element.getProperty("addnewqcitempage");
        String qcMethodId = (String)this.pageContext.getAttribute("qcmethodid");
        String qcMethodVersionId = (String)this.pageContext.getAttribute("qcmethodversionid");
        String paramListType = "";
        if (qcMethodId != null && qcMethodId.length() > 0) {
            try {
                qcMethodId = HttpUtil.decodeURIComponent(qcMethodId);
                paramListType = this.getQCMethodParamListType(qcMethodId, qcMethodVersionId);
            }
            catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
        }
        if (qcMethodId == null || qcMethodId.length() == 0) {
            HashMap hm = this.getQCMethodIdAndParamListType();
            qcMethodId = (String)hm.get("qcmethodid");
            paramListType = (String)hm.get("paramlisttype");
            qcMethodVersionId = (String)hm.get("qcmethodversionid");
        }
        if (qcMethodId == null) {
            qcMethodId = "";
        }
        sb.append("var ").append(id).append("_tableid = '").append(id).append("';");
        sb.append("var ").append(id).append("_appearance = '").append(this.getElementAppearance()).append("';");
        sb.append("var ").append(id).append("_mode = '").append(this.getMode()).append("';");
        sb.append("var ").append(id).append("_modified = false;");
        sb.append("var ").append(id).append("_initflag = false;");
        sb.append("var ").append(id).append("_collapseindex = -1;");
        sb.append("var ").append(id).append("_collapseflag = false;");
        if (this._AutoKeyFlag) {
            sb.append("var ").append(id).append("_autokeyflag = 'true';");
        } else {
            sb.append("var ").append(id).append("_autokeyflag = 'false';");
        }
        sb.append("var elementid = '").append(id).append("';");
        sb.append("function add").append(id).append("() {\n");
        sb.append("var sel = getSelectedRowID( '").append(id).append("' );\n").append("if ( sel.length > 0 ) { \n").append("    var selected = sel.split( ';' );\n").append("    if ( selected.length > 1 ) {\n").append("        sapphire.alert('").append(this.getTranslationProcessor().translate("Select only one row to add sample.")).append("'); \n").append("        return; \n").append("    } \n").append("} \n");
        if (lookuppage != null && lookuppage.trim().length() > 0) {
            sb.append("    var url = '").append(lookuppage).append("&param1=").append(qcMethodId);
            if (paramListType != null && paramListType.length() > 0) {
                try {
                    sb.append("&param2=").append(SafeHTML.encodeForURL(paramListType));
                }
                catch (Exception e) {
                    this.logger.error(e.getMessage(), e);
                }
            }
            if (qcMethodId.length() > 0) {
                sb.append("&param3=").append(qcMethodVersionId);
            }
            sb.append("&lookupcallback=qcbatchitemmaint.addNewItem_sdiitem';\n");
            sb.append("    elementid = '").append(id).append("';");
            sb.append("   sapphire.ui.dialog.open('").append("").append("', url, false, 800, 600);\n");
        } else {
            sb.append("    qcbatchitemmaint.insertNewItem_sdiitem( '").append(id).append("' );");
        }
        sb.append("}\n");
        sb.append("function addFromPriorQCBatch").append(id).append("() {\n");
        sb.append("var sel = getSelectedRowID( '").append(id).append("' );\n").append("if ( sel.length > 0 ) { \n").append("    var selected = sel.split( ';' );\n").append("    if ( selected.length > 1 ) {\n").append("        sapphire.alert('").append(this.getTranslationProcessor().translate("Select only one row to add sample.")).append("'); \n").append("        return; \n").append("    } \n").append("} \n");
        if (lookupSelFromPriorBatchPage != null && lookupSelFromPriorBatchPage.trim().length() > 0) {
            sb.append("    var url = '").append(lookupSelFromPriorBatchPage).append("&param1=").append(qcMethodId).append("&param2=" + qcMethodVersionId);
            if (paramListType != null && paramListType.length() > 0) {
                try {
                    sb.append("&param3=").append(SafeHTML.encodeForURL(paramListType));
                }
                catch (Exception e) {
                    this.logger.error(e.getMessage(), e);
                }
            }
            sb.append("&lookupcallback=qcbatchitemmaint.addNewItem_sdiitem';\n");
            sb.append("    elementid = '").append(id).append("';");
            sb.append("   sapphire.ui.dialog.open('").append("").append("', url, false, 800, 600);\n");
        } else {
            sb.append("    qcbatchitemmaint.insertNewItem_sdiitem( '").append(id).append("' );");
        }
        sb.append("}\n");
        sb.append("function addQC").append(id).append("() {\n");
        sb.append("var sel = getSelectedRowID( '").append(id).append("' );\n").append("if ( sel.length > 0 ) { \n").append("    var selected = sel.split( ';' );\n").append("    if ( selected.length > 1 ) {\n").append("        sapphire.alert('").append(this.getTranslationProcessor().translate("Select only one row to add qc sample.")).append("'); \n").append("        return; \n").append("    } \n").append("} \n");
        if (lookupQCSamplePage != null && lookupQCSamplePage.trim().length() > 0) {
            if (lookupQCSamplePage.indexOf("MST") == -1) {
                qcMethodId = (String)this.pageContext.getAttribute("keyid1");
            }
            if (qcMethodId == null) {
                qcMethodId = "";
            }
            sb.append("    var url = '").append(lookupQCSamplePage).append("&param1=").append(qcMethodId);
            if (qcMethodId.length() > 0 && lookupQCSamplePage.indexOf("MST") != -1) {
                sb.append("&param2=").append(qcMethodVersionId);
            }
            sb.append("&lookupcallback=qcbatchitemmaint.addNewQCItem_sdiitem';\n");
            sb.append("    elementid = '").append(id).append("';");
            sb.append("   sapphire.ui.dialog.open('").append("").append("', url, false, 800, 600);\n");
        } else {
            sb.append("    qcbatchitemmaint.insertNewItem_sdiitem( '").append(id).append("' );");
        }
        sb.append("}\n");
        sb.append("function remove").append(id).append("() {\n");
        sb.append("    qcbatchitemmaint.removeSelectedRows_sdiitem( '").append(id).append("');");
        sb.append("}\n");
        if (this.doesColumnExists("linktoqcbatchitemid") && !this._ColumnsList.contains("linktoqcbatchitemid") || this.doesColumnExists("linkedto") && !this._ColumnsList.contains("linkedto")) {
            sb.append("function moveUp").append(id).append("() {\n");
            sb.append("    if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'U' ) ) {");
            sb.append("        qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'U' ); \n");
            sb.append("        setChangesMade_this( '").append(id).append("', true );");
            sb.append("    }");
            sb.append("}\n");
            sb.append("function moveDown").append(id).append("() {\n");
            sb.append("    if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'D' ) ) {");
            sb.append("        qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'D' ); \n");
            sb.append("        setChangesMade_this( '").append(id).append("', true );");
            sb.append("    }");
            sb.append("}\n");
        } else if (this.doesColumnExists("linktoqcbatchitemid") && this._ColumnsList.contains("linktoqcbatchitemid") && this.doesColumnExists("linkedto") && this._ColumnsList.contains("linkedto")) {
            sb.append("function moveUp").append(id).append("() {\n");
            sb.append("    if ( fnCalculateRelativeLinkToValue( '").append(id).append("', a2_").append(id).append(", 'U' ) ) { \n");
            sb.append("        if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'U' ) ) {");
            sb.append("            qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'U' ); \n");
            sb.append("            setChangesMade_this( '").append(id).append("', true );");
            sb.append("        }");
            sb.append("    }");
            sb.append("}\n");
            sb.append("function moveDown").append(id).append("() {\n");
            sb.append("    if ( fnCalculateRelativeLinkToValue( '").append(id).append("', a2_").append(id).append(", 'D' ) ) {");
            sb.append("        if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'D' ) ) {");
            sb.append("            qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'D' ); \n");
            sb.append("            setChangesMade_this( '").append(id).append("', true );");
            sb.append("        }");
            sb.append("    }");
            sb.append("}\n");
        } else {
            sb.append("function moveUp").append(id).append("() {\n");
            sb.append("    if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'U' ) ) {");
            sb.append("        qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'D' ); \n");
            sb.append("    } ");
            sb.append("}\n");
            sb.append("function moveDown").append(id).append("() {\n");
            sb.append("   if ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'D' ) ) {");
            sb.append("        qcbatchitemmaint.swapRowSequence( '").append(id).append("', a2_").append(id).append(", 'D' ); \n");
            sb.append("   } ");
            sb.append("}\n");
        }
        sb.append("var ").append(id).append("_masterkeycount = '").append(this._MasterKeyCollection.size()).append("';\n");
        for (i = 0; i < this._MasterKeyCollection.size(); ++i) {
            keylist = this._MasterKeyCollection.getPropertyList(i);
            sb.append("var ").append(id).append("_masterkey_").append(i).append(" = '").append(keylist.getProperty("columnid")).append("';\n");
        }
        sb.append("var ").append(id).append("_detailkeycount = '").append(this._DetailKeyCollection.size()).append("';\n");
        for (i = 0; i < this._DetailKeyCollection.size(); ++i) {
            keylist = this._DetailKeyCollection.getPropertyList(i);
            sb.append("var ").append(id).append("_detailkey_").append(i).append(" = '").append(keylist.getProperty("columnid")).append("';\n");
            sb.append("var ").append(id).append("_detaildefault_").append(i).append(" = '").append(keylist.getProperty("default")).append("';\n");
        }
        PropertyList legendProperties = this.element.getPropertyList("legend");
        if (legendProperties != null) {
            sb.append("var ").append(id).append("_legendtext = '").append(legendProperties.getProperty("text")).append("';\n");
            sb.append("var ").append(id).append("_legendsymbol = '").append(legendProperties.getProperty("symbol")).append("';\n");
            sb.append("var ").append(id).append("_legendcolumn = '").append(legendProperties.getProperty("column")).append("';\n");
            legendProperties = null;
        }
        return sb.toString();
    }

    private HashMap getQCMethodIdAndParamListType() {
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select qcmethodid, qcmethodversionid, paramlisttype from s_qcbatch where s_qcbatchid = " + safeSQL.addVar(this.getKeyid1()), safeSQL.getValues());
        HashMap<String, String> hm = new HashMap<String, String>();
        if (ds != null && ds.size() > 0) {
            hm.put("qcmethodid", ds.getValue(0, "qcmethodid", ""));
            hm.put("qcmethodversionid", ds.getValue(0, "qcmethodversionid", ""));
            hm.put("paramlisttype", ds.getValue(0, "paramlisttype", ""));
        }
        return hm;
    }

    private String getQCMethodParamListType(String qcMethodId, String qcMethodVersionId) {
        String paramListType = null;
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select paramlisttype from s_qcmethod where s_qcmethodid = " + safeSQL.addVar(qcMethodId) + " and s_qcmethodversionid = " + safeSQL.addVar(qcMethodVersionId), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            paramListType = ds.getValue(0, "paramlisttype", "");
        }
        return paramListType;
    }

    @Override
    protected String getOperations(String elementType) {
        StringBuffer sb = new StringBuffer();
        elementType = "qcbatchitemmaint";
        sb.append("document.getElementById( '__").append(this.element.getId()).append("_ecolumns' ).value = getColumnIDBuffer( c1_").append(this.element.getId()).append(", '|', false );");
        sb.append("function reset").append(this.element.getId()).append("() {");
        sb.append("    resetDetailData( '").append(elementType).append("', '").append(this.element.getId()).append("' );");
        sb.append("}\n");
        if (this.pageContext.getAttribute("__DetailMaint_CommonFunction") == null) {
            sb.append("function getParamValue( param ) {");
            sb.append("    if ( param == 'sdcid' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("sdcid")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid1' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid1")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid2' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid2")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid3' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid3")).append("';");
            sb.append("    }");
            sb.append("}");
            this.pageContext.setAttribute("__DetailMaint_CommonAttributes", (Object)"1");
        }
        sb.append("_elementtype = '").append(this.element.getProperty("elementtype")).append("';");
        sb.append("initDetail( '").append(elementType).append("', '").append(this.element.getId()).append("' );");
        return sb.toString();
    }

    @Override
    protected String getIncludeScripts() {
        StringBuffer sb = new StringBuffer();
        sb.append("<script language='JavaScript' src='WEB-OPAL/elements/scripts/dd.js'></script>\n");
        sb.append("<script language='JavaScript' src='WEB-OPAL/elements/scripts/sdibase.js'></script>\n");
        sb.append("<script language='JavaScript' src='WEB-OPAL/elements/qcbatchitemmaint/scripts/qcbatchitem.js'></script>\n");
        return sb.toString();
    }
}

