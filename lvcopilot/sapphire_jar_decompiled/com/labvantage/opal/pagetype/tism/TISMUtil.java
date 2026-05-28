/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.pagetype.tism;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TISMUtil {
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String IMAGE_LOOKUP = "WEB-CORE/elements/images/lookup.gif";
    public static final String NEWLINE = "\n";
    private PageContext pageContext;
    private PropertyList pagedata;
    private QueryProcessor queryProcessor;
    private TranslationProcessor translationProcessor;
    private SDCProcessor sdcProcessor;
    private ConnectionProcessor connectionProcessor;
    private String sysuserid;
    private String hostFrameId = "";

    public TISMUtil(PageContext pageContext) {
        this.pageContext = pageContext;
        this.pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
        this.sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        if (YES.equalsIgnoreCase(this.pagedata.getProperty("taskpage"))) {
            this.hostFrameId = this.pagedata.getProperty("__hostframeid", "");
        } else {
            TaskContext taskContext = null;
            try {
                taskContext = new TaskContext(this.pagedata.getProperty("taskcontext"));
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
            this.hostFrameId = taskContext != null && taskContext.isTaskPage() ? taskContext.getHostFrameId() : "";
        }
    }

    private QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.pageContext);
        }
        return this.queryProcessor;
    }

    public ConnectionProcessor getConnectionProcessor() {
        if (this.connectionProcessor == null) {
            this.connectionProcessor = new ConnectionProcessor(this.pageContext);
        }
        return this.connectionProcessor;
    }

    public TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = new TranslationProcessor(this.pageContext);
        }
        return this.translationProcessor;
    }

    public SDCProcessor getSdcProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = new SDCProcessor(this.pageContext);
        }
        return this.sdcProcessor;
    }

    public PropertyList getPagedata() {
        return this.pagedata;
    }

    public HashMap<String, Serializable> getStorageUnitContentInfo(String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        boolean isStorageUnit = false;
        boolean isStorageUnitContent = false;
        boolean isFreezeThawCandidate = false;
        HashMap<String, Serializable> storageUnitContentMap = new HashMap<String, Serializable>();
        if (StringUtil.getLen(sdcId) > 0L && StringUtil.getLen(keyId1) > 0L) {
            if ("StorageUnitSDC".equals(sdcId)) {
                StringBuilder sql = new StringBuilder();
                sql.append("select storageunitid, labelpath, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3,");
                sql.append(" (select se.freezethawcandidateflag from storageenv se where se.storageenvid = storageunit.storageenvid ) freezethawcandidateflag,");
                sql.append(" (select e.freezethawcandidateflag from storageenv e where e.storageenvid = (select su.storageenvid from storageunit su where su.storageunitid = storageunit.ancestorid)) ancestorftflag");
                sql.append(" from storageunit");
                sql.append(" where storageunitid = ?");
                DataSet contentDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{keyId1});
                if (contentDs.size() > 1) {
                    throw new SapphireException(" Invalid data exist in StorageUnitSDC: Multiple StorageUnit location found for  SdcId:" + sdcId + " KeyId1:" + keyId1 + " KeyId2:" + keyId2 + " KeyId3:" + keyId3);
                }
                if (contentDs.size() == 1) {
                    isStorageUnit = true;
                    if (!sdcId.equals("StorageUnitSDC")) {
                        isStorageUnitContent = true;
                    }
                    storageUnitContentMap.put("storageunitid", (Serializable)((Object)contentDs.getValue(0, "storageunitid")));
                    storageUnitContentMap.put("linksdcid", (Serializable)((Object)contentDs.getValue(0, "linksdcid")));
                    storageUnitContentMap.put("linkkeyid1", (Serializable)((Object)contentDs.getValue(0, "linkkeyid1")));
                    storageUnitContentMap.put("labelpath", (Serializable)((Object)contentDs.getValue(0, "labelpath")));
                    isFreezeThawCandidate = YES.equals(contentDs.getValue(0, "freezethawcandidateflag"));
                    if (!isFreezeThawCandidate) {
                        isFreezeThawCandidate = YES.equals(contentDs.getValue(0, "ancestorftflag"));
                    }
                } else {
                    storageUnitContentMap.put("storageunitid", (Serializable)((Object)""));
                    storageUnitContentMap.put("linksdcid", (Serializable)((Object)""));
                    storageUnitContentMap.put("linkkeyid1", (Serializable)((Object)""));
                }
            } else {
                int sucount = this.getQueryProcessor().getPreparedSqlDataSet("select count(storageunitid) sucount from storageunit where linksdcid = ?", (Object[])new String[]{sdcId}).getInt(0, "sucount");
                if (!keyId1.contains(";") && sucount > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuilder sql = new StringBuilder();
                    sql.append("select e.freezethawcandidateflag");
                    sql.append(" from storageenv e");
                    sql.append(" where storageenvid = (select su.storageenvid from storageunit su where su.storageunitid = (");
                    sql.append(" select su1.ancestorid");
                    sql.append(" from storageunit su1");
                    sql.append(" where su1.linksdcid = ").append(safeSQL.addVar(sdcId));
                    sql.append(" and su1.linkkeyid1 = ").append(safeSQL.addVar(keyId1));
                    if (StringUtil.getLen(keyId2) > 0L) {
                        sql.append(" and su1.linkkeyid2 = ").append(safeSQL.addVar(keyId2));
                    }
                    if (StringUtil.getLen(keyId3) > 0L) {
                        sql.append(" and su1.linkkeyid3 = ").append(safeSQL.addVar(keyId3));
                    }
                    sql.append(") )");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        isFreezeThawCandidate = YES.equals(ds.getValue(0, "freezethawcandidateflag"));
                    }
                } else {
                    isFreezeThawCandidate = false;
                }
                storageUnitContentMap.put("storageunitid", (Serializable)((Object)""));
                storageUnitContentMap.put("linksdcid", (Serializable)((Object)""));
                storageUnitContentMap.put("linkkeyid1", (Serializable)((Object)""));
            }
        }
        storageUnitContentMap.put("isStorageUnit", Boolean.valueOf(isStorageUnit));
        storageUnitContentMap.put("isStorageUnitContent", Boolean.valueOf(isStorageUnitContent));
        storageUnitContentMap.put("freezethawcandidateflag", (Serializable)((Object)(isFreezeThawCandidate ? YES : NO)));
        return storageUnitContentMap;
    }

    public static String getErrorTable(String errorMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='100%'>");
        sb.append("<tr><td valign='top' NOWRAP><b><font color='red'>");
        sb.append(errorMsg);
        sb.append("</font></b></td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    public String getScanInput() throws SapphireException {
        DataSet ds;
        String targetstorageunitvalidationclass;
        PropertyList targetSUSdiList;
        PropertyList sourceSUSdiList;
        PropertyList plList = this.pagedata.getPropertyList("inputscan");
        StringBuilder sb = new StringBuilder();
        String show = plList.getProperty("show");
        if (show.length() < 1 || show.equals(NO)) {
            sb.append(NEWLINE).append("<script language=\"javascript\">");
            sb.append(NEWLINE).append("__SourceSUValidationClass = \"com.labvantage.opal.validation.tism.ValidateSourceStorageUnit\";");
            sb.append(NEWLINE).append("__TargetSUValidationClass = \"com.labvantage.opal.validation.tism.ValidateTargetStorageUnit\";");
            sb.append(NEWLINE).append("__topFrame = eval( top").append(StringUtil.getLen(this.hostFrameId) > 0L ? "." + this.hostFrameId : "").append(");");
            sb.append(NEWLINE).append("__hostFrame = \"").append(StringUtil.getLen(this.hostFrameId) > 0L ? this.hostFrameId + "." : "").append("\";");
            sb.append(NEWLINE).append("</script>");
            return sb.toString();
        }
        Button button = new Button(this.pageContext);
        button.setAppearance("standard");
        button.setWidth("60");
        button.setText(this.getTranslationProcessor().translate("Scan"));
        button.setAction("scan()");
        button.setImg("WEB-CORE/images/gif/BarCode.gif");
        sb.append("<div id='tismscanbar' style='display:flex;justify-content:center;align-items:center;padding:2px;'>");
        if (plList.getProperty("title").length() > 0) {
            sb.append(NEWLINE).append("<div>").append(plList.getProperty("title")).append("</div>");
        }
        sb.append(NEWLINE).append("<div><input type=\"input\" id=\"scan\" name=\"scan\" maxlength=\"80\" onkeypress=\"scanValidation(event,this)\" style=\"width:220px;\" autocomplete='off'></div><div>").append(button.getHtml()).append("</div>");
        PropertyList sdiscan = plList.getPropertyList("sdiscan");
        if (YES.equals(sdiscan.getProperty("showsdiscan"))) {
            String prescanvalidationclass = sdiscan.getProperty("prescanvalidationclass", "");
            String validationClass = sdiscan.getProperty("sdivalidationclass", "com.labvantage.opal.pagetype.tism.ScanValidation");
            sb.append(NEWLINE).append("<script>__TismPreScanValidationClass = \"").append(prescanvalidationclass).append("\";</script>");
            sb.append(NEWLINE).append("<script>__TismScanningClass = \"").append(validationClass).append("\";</script>");
            sb.append(NEWLINE).append("<div>").append(this.getSDIScan(plList)).append("</div>");
            if (StringUtil.getLen(prescanvalidationclass) > 0L) {
                try {
                    Class.forName(prescanvalidationclass);
                }
                catch (ClassNotFoundException e) {
                    return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Pre Scan Validation class not found") + " (\"" + prescanvalidationclass + "\").</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
                }
            }
            try {
                Class.forName(validationClass);
            }
            catch (ClassNotFoundException e) {
                return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Scanning class not found") + " (\"" + validationClass + "\").</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
            }
        }
        if (YES.equals((sourceSUSdiList = plList.getPropertyList("sourcestorageunitscan")).getProperty("showsourcestorageunitscan"))) {
            sb.append("<div>");
            sb.append("<input type=\"radio\" name=\"scanoptions\" value=\"sourcestorageunitscan\">").append(sourceSUSdiList.getProperty("text"));
            sb.append("</div>");
        }
        if (YES.equals((targetSUSdiList = plList.getPropertyList("targetstorageunitscan")).getProperty("showtargetstorageunitscan"))) {
            sb.append("<div>");
            sb.append("<input type=\"radio\" name=\"scanoptions\" value=\"targetstorageunitscan\">").append(targetSUSdiList.getProperty("text"));
            sb.append("</div>");
        }
        if (YES.equals(plList.getProperty("showalias"))) {
            String defaultaliastype = plList.getProperty("defaultaliastype", "All");
            boolean selected = YES.equals(plList.getProperty("aliasselected", NO));
            if (selected) {
                String lookupsdcid = "Sample";
                PropertyListCollection collection = plList.getCollectionNotNull("sdclookup");
                if (collection.size() > 0) {
                    lookupsdcid = collection.getPropertyList(0).getProperty("sdcid");
                }
                sb.append("<div><input type=\"checkbox\" id=\"aliascheckbx\" name=\"aliascheck\"  checked>");
                sb.append(this.getTranslationProcessor().translate("Scan Alias")).append("</div>");
                sb.append("<div>");
                sb.append("<select id=\"aliastypeselect\" defaulttype=\"").append(defaultaliastype).append("\">");
                sb.append("<option value='All'>All</option>");
                DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet("select coalesce(refdisplayvalue, refvalueid) refdisplayvalue, refvalueid from refvalue where reftypeid = ? and (activeflag is null or activeflag = 'Y') order by usersequence", (Object[])new String[]{lookupsdcid + "AliasType"});
                if (ds2 != null) {
                    for (int i = 0; i < ds2.size(); ++i) {
                        String refvalueid = ds2.getString(i, "refvalueid", "");
                        String refdisplayvalue = ds2.getString(i, "refdisplayvalue", "");
                        sb.append("<option value=\"").append(refvalueid).append("\"").append(refvalueid.equals(defaultaliastype) ? " selected" : "").append(">").append(refdisplayvalue).append("</option>");
                    }
                }
                sb.append("</select>");
                sb.append("&nbsp;(").append(this.getTranslationProcessor().translate("Select Alias Type")).append(")</div>");
                sb.append("<script>__AliasCheck=\"Y\";</script>");
            } else {
                sb.append(NEWLINE).append("<div>&nbsp;&nbsp;<input type=\"checkbox\" id=\"aliascheckbx\" name=\"aliascheck\">");
                sb.append(this.getTranslationProcessor().translate("Scan Alias")).append("</div>");
                sb.append(NEWLINE).append("<div style=\"display:none\" valign=\"middle\"><select id=\"aliastypeselect\" defaulttype=\"").append(defaultaliastype).append("\"></select>");
                sb.append(NEWLINE).append("&nbsp;(").append(this.getTranslationProcessor().translate("Select Alias Type")).append(")</div>");
                sb.append(NEWLINE).append("<script>__AliasCheck=\"Y\";</script>");
            }
        } else {
            sb.append(NEWLINE).append("<div>&nbsp;&nbsp;<input type=\"checkbox\" name=\"aliascheck\" value=\"false\" style='display:none'></div>");
            sb.append(NEWLINE).append("<script>__AliasCheck=\"N\";</script>");
        }
        sb.append("</div>");
        String sourcestorageunitvalidationclass = sourceSUSdiList.getProperty("sourcestorageunitvalidationclass", "").trim();
        if (sourcestorageunitvalidationclass.length() > 0) {
            try {
                Class.forName(sourcestorageunitvalidationclass);
            }
            catch (ClassNotFoundException e) {
                return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration: ") + this.getTranslationProcessor().translate("Source Storage Unit Validation class not found") + " (\"" + sourcestorageunitvalidationclass + "\").</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
            }
        }
        if ((targetstorageunitvalidationclass = targetSUSdiList.getProperty("targetstorageunitvalidationclass", "").trim()).length() > 0) {
            try {
                Class.forName(targetstorageunitvalidationclass);
            }
            catch (ClassNotFoundException e) {
                return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration: ") + this.getTranslationProcessor().translate("Target Storage Unit Validation class not found") + " (\"" + targetstorageunitvalidationclass + "\").</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
            }
        }
        sb.append(NEWLINE).append("<script language=\"javascript\">");
        sb.append(NEWLINE).append("__SDIValidationClass = \"").append(sdiscan.getProperty("sdivalidationclass")).append("\";");
        sb.append(NEWLINE).append("__SourceSUValidationClass = \"").append(sourcestorageunitvalidationclass.length() > 0 ? sourcestorageunitvalidationclass : "com.labvantage.opal.validation.tism.ValidateSourceStorageUnit").append("\";");
        sb.append(NEWLINE).append("__TargetSUValidationClass = \"").append(targetstorageunitvalidationclass.length() > 0 ? targetstorageunitvalidationclass : "com.labvantage.opal.validation.tism.ValidateTargetStorageUnit").append("\";");
        sb.append(NEWLINE).append(this.getSdcLookUpList(plList));
        String scanitemnotfound = plList.getProperty("scanitemnotfound", "Show Error");
        sb.append(NEWLINE).append("__tismaddnewsdi = ").append("Add new SDI".equals(scanitemnotfound) || "Confirm".equals(scanitemnotfound)).append(";");
        sb.append(NEWLINE).append("__tismaddnewsdiconfirm = ").append("Confirm".equals(scanitemnotfound)).append(";");
        String discrepancyrefvalue = "";
        String discrepancyreftype = this.pagedata.getProperty("discrepancyreftype", "");
        if (StringUtil.getLen(discrepancyreftype) > 0L && (ds = this.getQueryProcessor().getRefTypeDataSet(discrepancyreftype)) != null && ds.size() > 0) {
            discrepancyrefvalue = ds.getColumnValues("refvalueid", ";");
        }
        sb.append(NEWLINE).append("tism.setDiscrepancyRefType( '").append(SafeHTML.encodeForJavaScript(discrepancyrefvalue)).append("' );");
        sb.append(NEWLINE).append("tism.setCAPAIncidentTemplate( '").append(this.pagedata.getProperty("capaincidenttemplate")).append("' );");
        sb.append(NEWLINE).append("__topFrame = sapphire.page.getTop();");
        sb.append(NEWLINE).append("__hostFrame = \"").append(StringUtil.getLen(this.hostFrameId) > 0L ? this.hostFrameId + "." : "").append("\";");
        sb.append(NEWLINE).append("</script>");
        sb.append(this.getAdditionalDataHtml(plList));
        return sb.toString();
    }

    public String getAdditionalDataHtml(PropertyList plList) {
        StringBuilder sb = new StringBuilder();
        PropertyList extrascanprops = plList.getPropertyListNotNull("extrascanprops");
        String show = extrascanprops.getProperty("show", NO);
        if (YES.equals(show) || show.startsWith("$G{")) {
            int i;
            PropertyList discripancy;
            StringBuilder script = new StringBuilder();
            JSONObject go = new JSONObject();
            PropertyListCollection groovyscanparser = extrascanprops.getCollectionNotNull("groovyscanparser");
            for (int i2 = 0; i2 < groovyscanparser.size(); ++i2) {
                PropertyList list = groovyscanparser.getPropertyList(i2);
                try {
                    go.put(list.getProperty("property"), list.getProperty("expression"));
                    continue;
                }
                catch (JSONException e) {
                    Trace.logError("Error", e);
                }
            }
            script.append(NEWLINE).append("__ExtraScanProps = true;");
            script.append(NEWLINE).append("__ExtraScanProps_show = \"").append(StringUtil.replaceAll(show, "\"", "||quot||")).append("\";");
            String scanparseclass = extrascanprops.getProperty("scanparseclass");
            if (StringUtil.getLen(scanparseclass) > 0L) {
                try {
                    Class<?> klass = Class.forName(scanparseclass);
                    String parentClass = klass.getSuperclass().getName();
                    if (!"sapphire.parser.BaseTismScanParser".equals(parentClass)) {
                        return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Scan Parsing Class must extend BaseTismScanParser class.") + "</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
                    }
                }
                catch (ClassNotFoundException e) {
                    return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Unable to find Scan Parsing Class.") + "</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
                }
                script.append(NEWLINE).append("__TismParseScanClass = \"").append(extrascanprops.getProperty("scanparseclass")).append("\";");
            }
            if (YES.equals((discripancy = extrascanprops.getPropertyListNotNull("discripancy")).getProperty("show"))) {
                String discripancymode = discripancy.getProperty("mode", "");
                if ("CAPA Deviations".equals(discripancymode)) {
                    script.append(NEWLINE).append("__ExtraScanProps_discripancy = \"capa\";");
                    String discripancyattr = this.pagedata.getProperty("capaincidenttemplate", "");
                    if (StringUtil.getLen(discripancyattr) == 0L) {
                        DataSet rt = this.getQueryProcessor().getSqlDataSet("select incidentid from incident where templateflag = 'Y' and incidentcategory = 'UnPlanned' order by incidentid");
                        if (rt != null) {
                            for (i = 0; i < rt.size(); ++i) {
                                script.append(NEWLINE).append("__ExtraScanProps_disattr.push(\"").append(rt.getValue(i, "incidentid", "")).append("\");");
                            }
                        }
                    } else {
                        script.append(NEWLINE).append("__ExtraScanProps_disattr.push(\"").append(discripancyattr).append("\");");
                    }
                } else if ("Discrepancy".equals(discripancymode)) {
                    script.append(NEWLINE).append("__ExtraScanProps_discripancy = \"deviation\";");
                }
            }
            PropertyListCollection extraprops = extrascanprops.getCollectionNotNull("extraprops");
            JSONArray o = new JSONArray();
            Map<String, String> additionalDataMap = TISMUtil.getAdditionalDataMap();
            for (i = 0; i < extraprops.size(); ++i) {
                PropertyList pl = extraprops.getPropertyList(i);
                String extrapropsid = pl.getProperty("extrapropsid");
                if (StringUtil.getLen(extrapropsid) <= 0L) continue;
                if (additionalDataMap.containsKey(extrapropsid)) {
                    pl.setProperty("otherpropid", additionalDataMap.get(extrapropsid));
                }
                String otherpropertyid = pl.getProperty("otherpropid").trim();
                if ("Other".equals(extrapropsid) && StringUtil.getLen(otherpropertyid) == 0L) {
                    return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Other Property ID is empty for \"Other\" property in Extra Scan Properties.") + "</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
                }
                if (!NO.equals(pl.getProperty("show", YES))) {
                    String dropdownsql = pl.getProperty("dropdownsql", "").trim();
                    if ("Volume".equals(extrapropsid)) {
                        script.append(NEWLINE).append("var __dd_qtyunits = new Array();");
                        DataSet dropdownds = this.getQueryProcessor().getSqlDataSet(StringUtil.getLen(dropdownsql) > 0L ? dropdownsql : "select unitsid from units order by unitsid");
                        for (int row = 0; row < dropdownds.size(); ++row) {
                            script.append(NEWLINE).append("__dd_qtyunits.push(\"").append(dropdownds.getValue(row, "unitsid", "")).append("\");");
                        }
                    } else if (StringUtil.getLen(dropdownsql) > 0L) {
                        dropdownsql = StringUtil.replaceAll(dropdownsql, "[%currentuser%]", this.sysuserid);
                        dropdownsql = StringUtil.replaceAll(dropdownsql, "[%CURRENTUSER%]", this.sysuserid);
                        dropdownsql = StringUtil.replaceAll(dropdownsql, "[%CurrentUser%]", this.sysuserid);
                        script.append(NEWLINE).append("var __dd_").append(otherpropertyid).append(" = new Array();");
                        if (dropdownsql.matches(".+;.+")) {
                            String[] s;
                            for (String value : s = StringUtil.split(dropdownsql, ";")) {
                                script.append(NEWLINE).append("__dd_").append(otherpropertyid).append(".push(\"").append(value).append("|").append(value).append("\");");
                            }
                        } else {
                            boolean dsflag = true;
                            String reftypeid = "";
                            if (!dropdownsql.matches("[sS][eE][lL][eE][cC][tT].+")) {
                                reftypeid = dropdownsql;
                                DataSet cnt = this.getQueryProcessor().getPreparedSqlDataSet("select count(reftypeid) cnt from reftype where reftypeid = ?", (Object[])new String[]{reftypeid});
                                if (cnt != null && cnt.getInt(0, "cnt") == 0) {
                                    return "<div style='padding:3px;background:#ffe7ba;vertical-align:top;color:red;font:bold 14px Calibri, sans-serif;border-top:1px solid gray'><img src='WEB-CORE/images/gif/error.gif' width='16px'>&nbsp;" + this.getTranslationProcessor().translate("Invalid Page Configuration:") + " " + this.getTranslationProcessor().translate("Reference Type not found") + " (" + dropdownsql + ").</div><script>document.getElementById( \"Save\" ).disabled = true;</script>";
                                }
                            } else if (dropdownsql.indexOf("[") != -1) {
                                String[] tokens;
                                dsflag = false;
                                for (String token : tokens = StringUtil.getTokens(dropdownsql)) {
                                    String tokenvalue = additionalDataMap.containsKey(token) ? additionalDataMap.get(token) : token;
                                    dropdownsql = StringUtil.replaceAll(dropdownsql, "[" + token + "]", "[" + tokenvalue + "]");
                                    script.append(NEWLINE).append("tismdd.add( '").append(tokenvalue).append("', '").append(otherpropertyid).append("' );");
                                }
                                script.append(NEWLINE).append("tismdd.setTargetSQL( '").append(otherpropertyid).append("', \"").append(dropdownsql).append("\" );");
                                pl.setProperty("rememberlastinput", NO);
                            }
                            if (dsflag) {
                                DataSet dropdownds;
                                if (reftypeid.length() == 0) {
                                    dropdownds = this.getQueryProcessor().getSqlDataSet(dropdownsql);
                                } else {
                                    dropdownsql = "select refvalueid, refdisplayvalue from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = ? order by usersequence, refvalueid";
                                    dropdownds = this.getQueryProcessor().getPreparedSqlDataSet(dropdownsql, (Object[])new String[]{reftypeid});
                                }
                                if (dropdownds != null) {
                                    for (int row = 0; row < dropdownds.size(); ++row) {
                                        String value;
                                        String displayvalue = value = dropdownds.getValue(row, dropdownds.getColumnId(0), "");
                                        if (dropdownds.getColumnCount() > 1) {
                                            displayvalue = dropdownds.getValue(row, dropdownds.getColumnId(1), "");
                                        }
                                        script.append(NEWLINE).append("__dd_").append(otherpropertyid).append(".push(\"").append(value).append("|").append(displayvalue).append("\");");
                                    }
                                }
                            }
                        }
                    }
                    if (pl.getProperty("title").length() == 0) {
                        pl.setProperty("title", "Other".equals(extrapropsid) ? otherpropertyid : extrapropsid);
                    }
                }
                o.put(new JSONObject(pl));
            }
            script.append(NEWLINE).append("// JSON START");
            script.append(NEWLINE).append("__ExtraScanProps_columns = ").append(o.toString()).append(";");
            script.append(NEWLINE).append("// JSON END");
            script.append(NEWLINE);
            PropertyListCollection extraparams = extrascanprops.getCollectionNotNull("extraparams");
            script.append(NEWLINE).append("var __ExtraScanProps_extraparam = new Array();");
            script.append(NEWLINE).append("var __ExtraScanProps_extraparamvalue = new Array();");
            if (extraparams.size() > 0) {
                for (int i3 = 0; i3 < extraparams.size(); ++i3) {
                    PropertyList list = extraparams.getPropertyList(i3);
                    String param = list.getProperty("parameter");
                    String value = list.getProperty("value");
                    if (StringUtil.getLen(param) <= 0L || StringUtil.getLen(value) <= 0L) continue;
                    script.append(NEWLINE).append("__ExtraScanProps_extraparam.push(\"").append(param).append("\");");
                    script.append(NEWLINE).append("__ExtraScanProps_extraparamvalue.push(\"").append(value).append("\");");
                }
            }
            sb.append(NEWLINE).append("<script language=\"javascript\">");
            sb.append((CharSequence)script);
            sb.append(NEWLINE).append("</script>");
        } else {
            sb.append(NEWLINE).append("<script>var __ExtraScanProps = false;</script>");
        }
        return sb.toString();
    }

    private static Map<String, String> getAdditionalDataMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Volume", "qtycurrent");
        map.put("Study", "studyid");
        map.put("Study Alias", "studycode");
        map.put("Sample Type", "sampletypeid");
        map.put("Site", "enrollingsiteid");
        map.put("Site Name", "enrollingsitename");
        map.put("Subject", "subjectid");
        map.put("Participant", "participantid");
        map.put("Cohort", "cohortid");
        map.put("External Participant", "externalparticipantid");
        map.put("Event Label", "eventlabel");
        map.put("Timepoint Label", "timepointlabel");
        map.put("Event Date", "eventdt");
        map.put("Specimen Type", "specimentype");
        map.put("Kit", "kitid");
        return map;
    }

    public String getMaintElementId(String sdcId) {
        PropertyListCollection plCollection = this.pagedata.getCollection("sdccollection");
        for (int incr = 0; incr < plCollection.size(); ++incr) {
            PropertyList tempList = plCollection.getPropertyList(incr);
            if (!tempList.getProperty("sdcid").equals(sdcId)) continue;
            return tempList.getProperty("elementid");
        }
        return "";
    }

    private String getSDIScan(PropertyList plList) {
        PropertyList sdiscan = plList.getPropertyList("sdiscan");
        PropertyListCollection plCollection = plList.getCollection("sdclookup");
        StringBuilder sb = new StringBuilder();
        sb.append("<script> ");
        sb.append("var __ScanFilesSdi='").append(YES.equals(sdiscan.getProperty("scanfilessdi")) ? YES : NO).append("';");
        sb.append("var __lookup_sdcid = '';");
        sb.append("</script>");
        sb.append("<table cellpadding=1 cellspacing=0 border=0><tr>");
        sb.append("<td style=\"padding-left:2px;padding-right:4px;font:normal 12 Verdana\">");
        sb.append("<input type=\"radio\" name=\"scanoptions\" checked value=\"sdiscan\">").append(sdiscan.getProperty("text")).append("</td>");
        sb.append("<td style=\"padding-left:3px;padding-right:3px;font:normal 12 Verdana\">");
        if (plCollection.size() == 1) {
            PropertyList plist = plCollection.getPropertyList(0);
            String sdcid = plist.getProperty("sdcid");
            String text = plist.getProperty("text");
            if (text.length() == 0) {
                text = sdcid;
            }
            sb.append(" (").append(this.getTranslationProcessor().translate(text)).append(")");
            sb.append("<script>__lookup_sdcid = '").append(sdcid).append("';</script>");
        } else {
            String defaultsdcid = "";
            sb.append(" ( <select name=\"sdclist\" id=\"sdclist\" onchange='setLookupSDC()'>");
            for (int i = 0; i < plCollection.size(); ++i) {
                PropertyList plist = plCollection.getPropertyList(i);
                String sdcid = plist.getProperty("sdcid");
                String text = plist.getProperty("text");
                if (text.length() == 0) {
                    text = sdcid;
                }
                sb.append("<option value=\"").append(plist.getProperty("sdcid")).append("\">").append(this.getTranslationProcessor().translate(text)).append("</option>");
                if (i != 0) continue;
                defaultsdcid = sdcid;
            }
            sb.append("</select> )");
            sb.append("<script>__lookup_sdcid = '").append(defaultsdcid).append("';</script>");
        }
        sb.append("</td>");
        sb.append("<td style=\"padding-left:2px;padding-right:2px;font:normal 10 Verdana valign=\"middle\">");
        if (YES.equals(sdiscan.getProperty("showlookup"))) {
            sb.append("<a href=\"/Lookup\" onClick=\"createMultiSelectLookUp();return false\">");
            sb.append("<img title=\"Select SDIs\" border=\"0\" src=\"WEB-CORE/elements/images/lookup.gif\"></a>");
        }
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private String getSdcLookUpList(PropertyList plList) {
        PropertyListCollection plCollection = plList.getCollection("sdclookup");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < plCollection.size(); ++i) {
            PropertyList plist = plCollection.getPropertyList(i);
            sb.append("var __").append(plist.getProperty("sdcid")).append("_lookup = \"").append(plist.getProperty("lookup")).append("\";");
        }
        return sb.toString();
    }

    public boolean showGraphicalRenderer(String storageUnitId) {
        String sql = "select storageunitid,propertytreeid from storageunit where storageunitid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{storageUnitId});
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "propertytreeid").length() >= 1 && !ds.getValue(0, "propertytreeid").equals("No Layout");
        }
        return false;
    }

    public int getAvailablePosition(String storageunitid) {
        StringBuilder sql = new StringBuilder();
        int allowedTrackItem = this.allowedTI(storageunitid);
        if (allowedTrackItem > -1) {
            sql.append("select count(*) usedposition from  trackitem ti where ti.currentstorageunitid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                int usedposition = ds.getInt(0, "usedposition", 0);
                return allowedTrackItem - usedposition;
            }
        }
        return -1;
    }

    private int allowedTI(String storageunitid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select maxtiallowed from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            return ds.getInt(0, "maxtiallowed", 0);
        }
        return -1;
    }

    public static String getFrameScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append("<script language=\"javascript\" src=\"WEB-OPAL/pagetypes/trackitemsumaint/scripts/tism_content.js\"></script>\n");
        sb.append("<script language=\"JavaScript\" src=\"WEB-OPAL/scripts/util.js\"></script>\n");
        sb.append("<script language=\"JavaScript\" src=\"WEB-OPAL/pagetypes/list/scripts/maintenance_list_content.js\"></script>\n");
        return sb.toString();
    }

    public String getLookupFunction(PropertyList propertyList, String frame) {
        String taskContextFrame = "sapphire.page.getTop()." + (StringUtil.getLen(this.hostFrameId) > 0L ? this.hostFrameId + "." : "");
        PropertyList lookuplist = propertyList.getPropertyList(frame);
        StringBuilder sb = new StringBuilder();
        StringBuilder _sb = new StringBuilder();
        sb.append("<script>");
        sb.append("var __sutype = '';");
        sb.append("var __showsutypediv = false;");
        sb.append("var __sulookupurl = '").append(lookuplist.getProperty("url", "rc?command=page&page=LV_SUSDCLookup")).append("';");
        sb.append("var __taskContextFrame = '").append(taskContextFrame).append("';");
        PropertyListCollection lookuptype = lookuplist.getCollection("type");
        if (lookuptype != null && lookuptype.size() > 0) {
            if (lookuptype.size() > 1) {
                boolean showlookupdiv = false;
                _sb.append("<div id=\"storageunitlookupdiv\" style=\"display:none\"><table cellpadding=0 cellspacing=0 style='width:100%'>");
                for (int i = 0; i < lookuptype.size(); ++i) {
                    if (!YES.equals(lookuptype.getPropertyList(i).getProperty("show", YES))) continue;
                    showlookupdiv = true;
                    String sutype = lookuptype.getPropertyList(i).getProperty("sutype", "");
                    String url = lookuptype.getPropertyList(i).getProperty("url", "rc?command=page&page=LV_TISMSULookup");
                    if (url.toLowerCase().startsWith("javascript:")) {
                        String js = url.substring(url.indexOf(":") + 1);
                        _sb.append("<tr><td valign=middle onclick=\"").append(taskContextFrame).append(frame).append("frame_iframe.").append(js).append(";\"");
                    } else {
                        _sb.append("<tr><td valign=middle onclick='").append(taskContextFrame).append(frame).append("frame_iframe.lookupstorageunit(\"").append(url).append("\")'");
                    }
                    _sb.append(" style='cursor:pointer;color:blue;'");
                    _sb.append(" onmouseover='").append(taskContextFrame).append(frame).append("frame_iframe.tism_rowMouseOver(this, true)'");
                    _sb.append(" onmouseout='").append(taskContextFrame).append(frame).append("frame_iframe.tism_rowMouseOut(this, true)'>");
                    _sb.append("&nbsp;<img src='WEB-OPAL/images/dot_black.gif'>&nbsp;&nbsp;&nbsp;<span style='text-decoration:underline'>").append(this.getTranslationProcessor().translate(sutype)).append("</span>");
                    _sb.append("</td></tr>");
                }
                _sb.append("</table></div>");
                if (showlookupdiv) {
                    sb.append("__showsutypediv = true;");
                }
            } else {
                sb.append("__sutype = '").append(lookuptype.getPropertyList(0).getProperty("sutype", "")).append("';");
            }
        }
        sb.append("</script>");
        sb.append((CharSequence)_sb);
        return sb.toString();
    }

    public String getSDCName(String sdcid) {
        return (String)this.getSdcProcessor().getSDCProperties(sdcid).get("singular");
    }

    public static void saveUserSelectedStorageUnit(String storageunitid, boolean target, String connectionid) {
        String userid = new ConnectionProcessor(connectionid).getSapphireConnection().getSysuserId();
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(connectionid);
        try {
            List<String> unpinned = OpalUtil.toList(configurationProcessor.getProfileProperty(userid, target ? "userconfig_tism_targetsu" : "userconfig_tism_sourcesu"), ";");
            List<String> pinned = OpalUtil.toList(configurationProcessor.getProfileProperty(userid, target ? "userconfig_tism_targetsu_pinned" : "userconfig_tism_sourcesu_pinned"), ";");
            if (unpinned.contains(storageunitid)) {
                unpinned.remove(storageunitid);
            }
            unpinned.add(0, storageunitid);
            while (unpinned.size() > 5) {
                unpinned.remove(5);
            }
            configurationProcessor.setProfileProperty(userid, target ? "userconfig_tism_targetsu" : "userconfig_tism_sourcesu", OpalUtil.toDelimitedString(unpinned, ";"));
        }
        catch (SapphireException e) {
            Trace.logError("Error", e);
        }
    }
}

