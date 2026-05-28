/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailmaint;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseSDIDetail
extends BaseItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 96767 $";
    protected String _ElementSDC;

    protected BaseSDIDetail(PropertyList element, PageContext pageContext, RequestContext requestContext, String connectionid) {
        this.element = element;
        this.setPageContext(pageContext);
        this.requestContext = requestContext;
        this.setConnectionId(connectionid);
        this.setElementSDC();
    }

    public String getScriptVariables() {
        StringBuffer sb = new StringBuffer();
        PropertyList legend = this.element.getPropertyList("legend");
        sb.append("var ").append(this.element.getId()).append("_appearance = '").append(this.getElementAppearance()).append("';");
        sb.append("var ").append(this.element.getId()).append("_mode = '").append(this.getMode()).append("';");
        sb.append("var ").append(this.element.getId()).append("_sdcid = '").append(this.getSdcid()).append("';");
        sb.append("var ").append(this.element.getId()).append("_keyid1 = '").append(this.getKeyid1()).append("';");
        sb.append("var ").append(this.element.getId()).append("_keyid2 = '").append(this.getKeyid2()).append("';");
        sb.append("var ").append(this.element.getId()).append("_keyid3 = '").append(this.getKeyid3()).append("';");
        sb.append("var ").append(this.element.getId()).append("_modified = false;");
        sb.append("var _addapplyflag = false;");
        sb.append("var ").append(this.element.getId()).append("_newitemsymbol = '");
        sb.append(legend.getProperty("newitemsymbol")).append("';");
        sb.append("var ").append(this.element.getId()).append("_applysymbol = '");
        sb.append(legend.getProperty("applyworkitemsymbol")).append("';");
        sb.append("var ").append(this.element.getId()).append("_isSapphireDialog = '").append(this.isSapphireDialog()).append("';");
        return sb.toString();
    }

    public abstract DataSet getDataset(RequestContext var1) throws SapphireException;

    @Override
    public String getIncludeScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getIncludeScripts());
        String sdc = this._ElementSDC.toLowerCase();
        sb.append("<script type='text/javascript'>var sdidetail_").append(sdc).append("_elementid = '").append(this.element.getId()).append("';</script>");
        sb.append("<script language='JavaScript' src='WEB-OPAL/elements/sdidetailmaint/scripts/sdidetail.js'></script>");
        if (sdc.equals("specsdc")) {
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/sdidetailmaint/scripts/sdispec.js'></script>");
        } else {
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/sdidetailmaint/scripts/sdi").append(sdc).append(".js'></script>");
        }
        return sb.toString();
    }

    protected String getContextVariables() {
        StringBuffer sb = new StringBuffer();
        Object o = this.pageContext.getAttribute("__SDIDetailElement_Context");
        if (o == null) {
            sb.append("var _elementid = '").append(this.element.getId()).append("';");
            sb.append("var _elementtype = null;");
            this.pageContext.setAttribute("__SDIDetailElement_Context", (Object)"true");
        }
        return sb.toString();
    }

    public List<String> setRequestObjects(PropertyList pagedata) {
        ArrayList<String> list = new ArrayList<String>();
        if (this._SdcID == null) {
            this._SdcID = "Sample";
        }
        if (this._KeyID1 == null || this._KeyID1.equals("(null)")) {
            this._KeyID1 = pagedata.getProperty("keyid1");
            if (this._KeyID1 == null || this._KeyID1.equals("(null)")) {
                list.add("keyid1");
            }
        }
        return list;
    }

    protected abstract List getRequiredColumnsList();

    protected abstract String getPageScripts();

    public void setElementSDC() {
        String elementType = this.element.getProperty("elementtype");
        if (elementType.equals("SDIData")) {
            this._ElementSDC = "DataSet";
        } else if (elementType.equals("SDIDataItem")) {
            this._ElementSDC = "DataItem";
        } else if (elementType.equals("SDISpec")) {
            this._ElementSDC = "SpecSDC";
        } else if (elementType.equals("SDIWorkItem")) {
            this._ElementSDC = "WorkItem";
        } else if (elementType.equals("SDIAddress")) {
            this._ElementSDC = "Address";
        } else if (elementType.equals("SDIApproval")) {
            this._ElementSDC = "ApprovalType";
        } else if (elementType.equals("SDIAlias")) {
            this._ElementSDC = "Alias";
        } else if (elementType.equals("SDIDocument")) {
            this._ElementSDC = "LV_Document";
        } else if (elementType.equals("SDIFormRule")) {
            this._ElementSDC = "LV_Form";
        } else if (elementType.equals("SDIWorksheetRule")) {
            this._ElementSDC = "LV_Worksheet";
        } else if (elementType.equals("SDIWorkflowRule")) {
            this._ElementSDC = "LV_WorkflowDef";
        } else if (elementType.equals("SDISecuritySet")) {
            this._ElementSDC = "LV_SecuritySet";
        } else if (elementType.equals("SDISecurityDepartment")) {
            this._ElementSDC = "Department";
        } else if (elementType.equals("SDIResourceRequirement")) {
            this._ElementSDC = "ResourceRequirement";
        }
    }

    @Override
    protected String getMainHtml() {
        DataSet ds;
        StringBuilder sb = new StringBuilder();
        List<String> list = this.setRequestObjects((PropertyList)this.pageContext.getAttribute("pagedata", 2));
        if (list.size() > 0) {
            sb.append("Following required parameters not found in request:<br>");
            for (String aList : list) {
                sb.append("<br>- ").append((Object)aList);
            }
            sb.append("</br>");
            return BaseSDIDetail.toErrorString("Element Configuration Error", sb.toString());
        }
        List mandatoryColumnList = this.getRequiredColumnsList();
        StringBuilder mcsb = new StringBuilder();
        for (Object aMandatoryColumnList : mandatoryColumnList) {
            String columnid = (String)aMandatoryColumnList;
            if (this._ColumnsList.contains(columnid)) continue;
            mcsb.append("<br>&nbsp;-&nbsp;").append(columnid);
        }
        if (mcsb.length() > 0) {
            return BaseSDIDetail.toErrorString("Element Configuration Error", mcsb.toString());
        }
        try {
            ds = this.getDataset(this.requestContext);
            M18NUtil m18n = new M18NUtil(this.pageContext);
            ds.setM18NUtil(m18n);
        }
        catch (SapphireException e) {
            return BaseSDIDetail.toErrorString("Data Error", e.getMessage());
        }
        sb.append(this.getIncludeScripts());
        sb.append("<script language='Javascript'>");
        sb.append(this.getContextVariables());
        sb.append(this.getScriptVariables());
        sb.append("</script>");
        sb.append(this.renderHtml(ds));
        this.putPropertyHandlerKey("ecolumns", this.convertToPropertyHandlerKey(this._ColumnsList));
        this.putPropertyHandlerKey("customcolumns", OpalUtil.toDelimitedString(this._CustomList, ";"));
        this.putPropertyHandlerKey("tablemetadata", OpalUtil.map2String(this._TableMetaData));
        this.putPropertyHandlerKey("paramlistid", OpalUtil.isNotEmpty(this.requestContext.getProperty("paramlistid")) && !this.requestContext.getProperty("paramlistid").equals("undefined") ? this.requestContext.getProperty("paramlistid") : this.requestContext.getProperty("action_paramlistid"));
        this.putPropertyHandlerKey("paramlistversionid", OpalUtil.isNotEmpty(this.requestContext.getProperty("paramlistversionid")) && !this.requestContext.getProperty("paramlistversionid").equals("undefined") ? this.requestContext.getProperty("paramlistversionid") : this.requestContext.getProperty("action_paramlistversionid"));
        String variantid = this.requestContext.getProperty("variantid");
        String dataset = this.requestContext.getProperty("dataset");
        this.putPropertyHandlerKey("variantid", OpalUtil.isNotEmpty(variantid) && !variantid.equals("undefined") ? variantid : this.requestContext.getProperty("action_variantid"));
        this.putPropertyHandlerKey("dataset", OpalUtil.isNotEmpty(dataset) && !dataset.equals("undefined") ? dataset : this.requestContext.getProperty("action_dataset"));
        switch (this.element.getProperty("elementtype")) {
            case "SDIData": {
                this.putPropertyHandlerKey("table", "sdidata");
                this.putPropertyHandlerKey("keycolumns", "paramlistid;paramlistversionid;variantid;dataset");
                break;
            }
            case "SDIDataItem": {
                this.putPropertyHandlerKey("table", "sdidataitem");
                this.putPropertyHandlerKey("keycolumns", "paramlistid;paramlistversionid;variantid;dataset;paramid;paramtype;replicateid");
                break;
            }
            case "SDIWorkItem": {
                this.putPropertyHandlerKey("table", "sdiworkitem");
                this.putPropertyHandlerKey("keycolumns", "workitemid;workiteminstance");
                break;
            }
            case "SDISpec": {
                this.putPropertyHandlerKey("table", "sdispec");
                this.putPropertyHandlerKey("keycolumns", "specid;specversionid");
                break;
            }
            case "SDIWorkflowRule": {
                this.putPropertyHandlerKey("table", "sdiworkflowrule");
                this.putPropertyHandlerKey("keycolumns", "workflowdefid;workflowdefversionid;workflowdefvariantid;taskdefitemid;ioitemid;workflowexecid");
                break;
            }
            case "SDIAddress": {
                this.putPropertyHandlerKey("table", "sdiaddress");
                this.putPropertyHandlerKey("keycolumns", "addressid;addresstype;contactfunction");
                break;
            }
            case "SDIApproval": {
                this.putPropertyHandlerKey("table", "sdiapproval");
                this.putPropertyHandlerKey("keycolumns", "approvaltypeid");
                break;
            }
            case "SDIAlias": {
                this.putPropertyHandlerKey("table", "sdialias");
                this.putPropertyHandlerKey("keycolumns", "aliasid;aliastype");
                break;
            }
            case "SDIDocument": {
                this.putPropertyHandlerKey("table", "sdidocument");
                this.putPropertyHandlerKey("keycolumns", "documentid;documentversionid");
                break;
            }
            case "SDIFormRule": {
                this.putPropertyHandlerKey("table", "sdiformrule");
                this.putPropertyHandlerKey("keycolumns", "formid;forminstance");
                break;
            }
            case "SDIWorksheetRule": {
                this.putPropertyHandlerKey("table", "sdiworksheetrule");
                this.putPropertyHandlerKey("keycolumns", "worksheetid;worksheetinstance");
                break;
            }
            case "SDISecuritySet": {
                this.putPropertyHandlerKey("table", "sdisecurityset");
                this.putPropertyHandlerKey("keycolumns", "operationid;securityset");
                break;
            }
            case "SDISecurityDepartment": {
                this.putPropertyHandlerKey("table", "sdisecuritydepartment");
                this.putPropertyHandlerKey("keycolumns", "operationid;securitydepartment");
                break;
            }
            case "SDIResourceRequirement": {
                this.putPropertyHandlerKey("table", "sdiresourcerequirement");
                this.putPropertyHandlerKey("keycolumns", "resourcenum;resourcetypeflag");
            }
        }
        sb.append(this.getPropertyHandlerFields());
        sb.append("<script language='Javascript'>");
        sb.append(this.getAutoAddNewRowScript()).append("\n");
        sb.append(this.getPageScripts());
        sb.append(this.getOperations("sdidetail"));
        sb.append("</script>");
        return sb.toString();
    }

    @Override
    public String getLinksData(String id) {
        DataSet ds;
        StringBuffer sb = new StringBuffer();
        ArrayList<String> dropdowncolumnlist = new ArrayList<String>();
        String defaultParamType = "";
        HashMap<String, DataSet> rmap = new HashMap<String, DataSet>();
        TranslationProcessor tp = this.getTranslationProcessor();
        sb.append("function ").append(id).append("_dd_getDDOptions( columnid ) {");
        sb.append("    var arr = new Array();");
        sb.append("    arr.push( ';' );");
        sb.append(this.renderDropDownJavaScript(dropdowncolumnlist, rmap));
        if (!"Alias".equals(this._ElementSDC) && !"ResourceRequirement".equals(this._ElementSDC) && (ds = this.getSDCProcessor().getLinksData(this._ElementSDC)) != null) {
            for (int i = 0; i < ds.size(); ++i) {
                boolean translate;
                String linktype = ds.getValue(i, "LINKTYPE");
                String columnid = ds.getValue(i, "SDCCOLUMNID").toLowerCase();
                if (!this._ColumnsList.contains(columnid) || dropdowncolumnlist.contains(columnid)) continue;
                int columnindex = this._ColumnsList.indexOf(columnid);
                String columnmode = (String)this._ModeList.get(columnindex);
                boolean bl = translate = "Y".equals(this._TranslateList.get(columnindex)) && columnmode.equals("dropdownlist");
                if (linktype.equals("R") || ds.getValue(i, "LINKTYPE").equals("V")) {
                    if (columnmode.equals("dropdownlist") || columnmode.equals("dropdowncombo")) {
                        String reftypeid = ds.getValue(i, "REFTYPEID");
                        sb.append("    if ( columnid == '").append(columnid).append("' ) {");
                        DataSet reftypeds = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
                        DataSet keyvalueds = new DataSet();
                        for (int x = 0; x < reftypeds.size(); ++x) {
                            String r1 = reftypeds.getValue(x, "REFVALUEID");
                            String r2 = reftypeds.getValue(x, "REFDISPLAYVALUE");
                            if (r2 == null || r2.equals("null") || r2.trim().length() == 0) {
                                r2 = r1;
                            }
                            r2 = translate ? tp.translate(r2) : r2;
                            sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                            int row = keyvalueds.addRow();
                            keyvalueds.setString(row, "R1", r1);
                            keyvalueds.setString(row, "R2", r2);
                        }
                        sb.append("}");
                        rmap.put(columnid, keyvalueds);
                        continue;
                    }
                    if ("readonly".equals(columnmode) || "hidden".equals(columnmode)) continue;
                    this._ModeList.remove(columnindex);
                    this._ModeList.add(columnindex, "readonly");
                    continue;
                }
                if (!linktype.equals("F") || "lookup".equals(columnmode) || "hidden".equals(columnmode)) continue;
                this._ModeList.remove(columnindex);
                this._ModeList.add(columnindex, "readonly");
            }
        }
        if (this._ElementSDC.equals("DataItem")) {
            String cmode;
            int columnindex;
            if (this._ColumnsList.contains("datatypes")) {
                columnindex = this._ColumnsList.indexOf("datatypes");
                cmode = (String)this._ModeList.get(columnindex);
                if (cmode.equals("dropdownlist")) {
                    sb.append("if ( columnid == 'datatypes' ) {");
                    sb.append("    arr.push( 'T;Text' );");
                    sb.append("    arr.push( 'N;Numeric' );");
                    sb.append("    arr.push( 'NC;Numeric (Calc)' );");
                    sb.append("    arr.push( 'D;Date' );");
                    sb.append("    arr.push( 'O;Date Only' );");
                    sb.append("    arr.push( 'A;Any' );");
                    sb.append("    arr.push( 'R;Reference' );");
                    sb.append("    arr.push( 'V;Validated Reference' );");
                    sb.append("    arr.push( 'S;SDC' );");
                    sb.append("}");
                } else if (!cmode.equals("hidden") && !cmode.equals("readonly")) {
                    this._ModeList.remove(columnindex);
                    this._ModeList.add(columnindex, "readonly");
                }
            }
            if (this._ColumnsList.contains("paramtype")) {
                columnindex = this._ColumnsList.indexOf("paramtype");
                cmode = (String)this._ModeList.get(columnindex);
                boolean translate = "Y".equals(this._TranslateList.get(columnindex)) && cmode.equals("dropdownlist");
                DataSet reftypeds = this.getQueryProcessor().getRefTypeDataSet("Param Type");
                DataSet keyvalueds = new DataSet();
                if (reftypeds.size() > 0) {
                    defaultParamType = reftypeds.getValue(0, "REFVALUEID");
                }
                sb.append("if ( columnid == 'paramtype' ) {");
                for (int x = 0; x < reftypeds.size(); ++x) {
                    String r1 = reftypeds.getValue(x, "REFVALUEID");
                    String r2 = reftypeds.getValue(x, "REFDISPLAYVALUE");
                    if (r2 == null || r2.equals("null") || r2.trim().length() == 0) {
                        r2 = r1;
                    }
                    r2 = translate ? tp.translate(r2) : r2;
                    r1 = SafeHTML.encodeForJavaScript(StringUtil.replaceAll(r1, "'", "\\'"));
                    r2 = SafeHTML.encodeForJavaScript(StringUtil.replaceAll(r2, "'", "\\'"));
                    sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                    int row = keyvalueds.addRow();
                    keyvalueds.setString(row, "R1", r1);
                    keyvalueds.setString(row, "R2", r2);
                }
                sb.append("}");
                rmap.put("paramtype", keyvalueds);
            }
        }
        sb.append("    return arr;");
        sb.append("}");
        StringBuilder _sb = new StringBuilder();
        sb.append("\nfunction ").append(this.element.getId()).append("_dd_getDisplayValue( columnid, value ) {");
        _sb.append("\nfunction ").append(this.element.getId()).append("_dd_getDataValue( columnid, displayvalue ) {");
        Set set = rmap.keySet();
        for (String columnid : set) {
            sb.append("\nif ( columnid == '").append(columnid).append("' ) {");
            _sb.append("\nif ( columnid == '").append(columnid).append("' ) {");
            ds = (DataSet)rmap.get(columnid);
            for (int i = 0; i < ds.size(); ++i) {
                String r1 = ds.getValue(i, "R1");
                String r2 = ds.getValue(i, "R2");
                if (r2 == null || r2.equals("null") || r2.trim().length() == 0) {
                    r2 = r1;
                }
                r2 = StringUtil.replaceAll(r2, "'", "\\'");
                r1 = StringUtil.replaceAll(r1, "'", "\\'");
                sb.append("if ( value == '").append(r1).append("' ) return '").append(r2).append("';");
                _sb.append("if ( displayvalue == '").append(r2).append("' ) return '").append(r1).append("';");
            }
            sb.append("\n}");
            _sb.append("\n}");
        }
        if (this._ElementSDC.equals("DataItem")) {
            sb.append("\nif ( columnid == 'datatypes' ) {");
            sb.append("    \nif ( value == 'T' ) return 'Text';");
            sb.append("    \nif ( value == 'N' ) return 'Numeric';");
            sb.append("    \nif ( value == 'NC' ) return 'Numeric (Calc)';");
            sb.append("    \nif ( value == 'D' ) return 'Date';");
            sb.append("    \nif ( value == 'O' ) return 'Date Only';");
            sb.append("    \nif ( value == 'A' ) return 'Any';");
            sb.append("    \nif ( value == 'R' ) return 'Reference';");
            sb.append("    \nif ( value == 'V' ) return 'Validated Reference';");
            sb.append("    \nif ( value == 'S' ) return 'SDC';");
            sb.append("}");
            _sb.append("\nif ( columnid == 'datatypes' ) {");
            _sb.append("    \nif ( displayvalue == 'Text' ) return 'T';");
            _sb.append("    \nif ( displayvalue == 'Numeric' ) return 'N';");
            _sb.append("    \nif ( displayvalue == 'Numeric (Calc)' ) return 'NC';");
            _sb.append("    \nif ( displayvalue == 'Date' ) return 'D';");
            _sb.append("    \nif ( displayvalue == 'Date Only' ) return 'O';");
            _sb.append("    \nif ( displayvalue == 'Any' ) return 'A';");
            _sb.append("    \nif ( displayvalue == 'Reference' ) return 'R';");
            _sb.append("    \nif ( displayvalue == 'Validated Reference' ) return 'V';");
            _sb.append("    \nif ( displayvalue == 'SDC' ) return 'S';");
            _sb.append("}");
        }
        sb.append("\nreturn value;");
        _sb.append("\nreturn displayvalue;");
        sb.append("\n}");
        _sb.append("\n}");
        sb.append(this.element.getId()).append("_defaultparamtype = '").append(SafeHTML.encodeForJavaScript(defaultParamType)).append("';");
        sb.append(_sb.toString());
        return sb.toString();
    }

    protected String getResolvedKeyid1() {
        String newAddMode = String.valueOf(this.pageContext.getAttribute("newAddMode"));
        String _templateoption = (String)this.pageContext.getAttribute("sdiOptionTemplateoption");
        if ("true".equalsIgnoreCase(newAddMode) && (_templateoption.equals("load") || _templateoption.equals("loadsave"))) {
            String tempId = (String)this.pageContext.getAttribute("sdiOptionTemplateId");
            String tempId1 = (String)this.pageContext.getAttribute("sdiOptionTemplateKeyId1");
            if (tempId.length() > 0) {
                return tempId;
            }
            return tempId1;
        }
        return super.getKeyid1();
    }

    protected String getResolvedKeyid2() {
        String newAddMode = String.valueOf(this.pageContext.getAttribute("newAddMode"));
        String _templateoption = (String)this.pageContext.getAttribute("sdiOptionTemplateoption");
        if ("true".equalsIgnoreCase(newAddMode) && (_templateoption.equals("load") || _templateoption.equals("loadsave"))) {
            String tempId2 = (String)this.pageContext.getAttribute("sdiOptionTemplateKeyId2");
            if (tempId2.length() > 0) {
                return tempId2;
            }
            return "(null)";
        }
        String _sdcid = this.getSdcid();
        String _keyid2 = super.getKeyid2();
        int sdcKeyCount = Integer.parseInt(this.getSDCProcessor().getProperty(_sdcid, "keycolumns"));
        if (sdcKeyCount > 1 && (_keyid2 == null || _keyid2.length() == 0 || "(null)".equals(_keyid2) || "C".equals(_keyid2))) {
            DataSet dataSet;
            String tableid = this.getSDCProcessor().getProperty(_sdcid, "tableid");
            String keycolid1 = this.getSDCProcessor().getProperty(_sdcid, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(_sdcid, "keycolid2");
            _keyid2 = OpalUtil.getColumnValue(this.getQueryProcessor(), tableid, keycolid2, keycolid1 + "=? and versionstatus='C'", new String[]{this.getResolvedKeyid1()});
            if (OpalUtil.isEmpty(_keyid2) && OpalUtil.isNotEmpty(dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select " + keycolid2 + " from " + tableid + " where " + keycolid1 + " = ? and versionstatus = 'P' order by createdt desc", (Object[])new String[]{this.getResolvedKeyid1()}))) {
                _keyid2 = dataSet.getValue(0, keycolid2);
            }
        }
        return _keyid2;
    }

    protected String getResolvedKeyid3() {
        String newAddMode = String.valueOf(this.pageContext.getAttribute("newAddMode"));
        String _templateoption = (String)this.pageContext.getAttribute("sdiOptionTemplateoption");
        if ("true".equalsIgnoreCase(newAddMode) && (_templateoption.equals("load") || _templateoption.equals("loadsave"))) {
            String tempId3 = (String)this.pageContext.getAttribute("sdiOptionTemplateKeyId3");
            if (tempId3.length() > 0) {
                return tempId3;
            }
            return "(null)";
        }
        return super.getKeyid3();
    }

    protected String getAutoAddNewRowScript() {
        StringBuilder sb = new StringBuilder();
        boolean autoaddnewrow = this.element.getProperty("autoaddnewrow", "N").equalsIgnoreCase("Y");
        if (autoaddnewrow) {
            sb.append("\n var ").append("_autoaddnewrow_").append(this.element.getId()).append(" = true;");
            String elementType = this.element.getProperty("elementtype");
            sb.append("sapphire.events.registerLoadListener( ");
            sb.append("function(){ ");
            sb.append("var _originalelementid='' + _elementid;");
            sb.append("    _elementid = '").append(this.element.getId()).append("';");
            sb.append("    _elementtype = '").append(elementType).append("';");
            if (elementType.equalsIgnoreCase("SDISpec")) {
                sb.append("addNewItem_sdidetail('',true);");
            } else if (elementType.equalsIgnoreCase("SDIWorkitem")) {
                sb.append("addNewItem_sdidetail('',true);");
            } else {
                sb.append("addNewItem_sdidetail('',true);");
            }
            sb.append("  _elementid=_originalelementid; }");
            sb.append(", false );\n");
        } else {
            sb.append("\n var ").append("_autoaddnewrow_").append(this.element.getId()).append(" = false;");
        }
        return sb.toString();
    }

    protected void createTimeZoneIndependentColumnList(String tableid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select columnid from syscolumnproperty where tableid = " + safeSQL.addVar(tableid) + " and propertyid = 'timezoneindependent' and propertyvalue = 'Y'";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String columnid = ds.getString(i, "columnid");
                this.timeZoneIndependentColumnList.add(columnid);
            }
        }
    }

    protected boolean isSapphireDialog() {
        return this.element.getProperty("dialogtype", "Browser Popup").equalsIgnoreCase("Sapphire Dialog");
    }
}

