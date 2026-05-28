/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.treelist;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TreeList
extends BaseElement {
    public static String LABVANTAGE_CVS_ID = "$Revision: 87028 $";
    public static final String CHECKBOX = "checkbox";
    public static final String RADIOBUTTON = "radiobutton";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String NBSP = "&nbsp;";

    @Override
    public String getHtml() {
        ArrayList<String> expandlist = new ArrayList<String>();
        PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        TranslationProcessor tp = this.getTranslationProcessor();
        String expandlevel = this.element.getProperty("expandlevel").trim();
        if (StringUtil.getLen(expandlevel) == 0L) {
            expandlevel = YES.equals(this.element.getProperty("expanded")) ? "-1" : "0";
        }
        int expand_level = 0;
        try {
            expand_level = Integer.parseInt(expandlevel);
            this.element.setProperty("expandlevel", String.valueOf(expand_level));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        String sdcid = this.element.getProperty("sdcid");
        if (sdcid.length() == 0) {
            return BaseItem.toErrorString(tp.translate("Element configuration error"), tp.translate("SDC is not defined"));
        }
        PropertyListCollection columns = this.element.getCollection("columns");
        if (columns == null || columns.size() == 0) {
            return BaseItem.toErrorString(tp.translate("Element configuration error"), tp.translate("No Columns defined"));
        }
        String parentcolumnid = this.element.getProperty("parentcolumnid");
        if (parentcolumnid.length() == 0) {
            return BaseItem.toErrorString(tp.translate("Element configuration error"), tp.translate("Missing value for \"Parent\" column"));
        }
        HashMap sdcProps = this.getSDCProcessor().getSDCProperties(sdcid);
        String keycolid1 = (String)sdcProps.get("keycolid1");
        this.element.setProperty("tableid", (String)sdcProps.get("tableid"));
        this.element.setProperty("keycolid1", keycolid1);
        this.element.setProperty("desccol", (String)sdcProps.get("desccol"));
        this.element.setProperty("activeableflag", (String)sdcProps.get("activeableflag"));
        this.element.setProperty("accesscontrolledflag", (String)sdcProps.get("accesscontrolledflag"));
        TreeList.setElementDefault(this.element);
        ArrayList<String> columnlist = new ArrayList<String>();
        PropertyList keycolumnlist = null;
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList list = columns.getPropertyList(i);
            String columnid = list.getProperty("columnid");
            if (columnid.trim().length() <= 0) continue;
            columnlist.add(columnid);
            if (!columnid.equalsIgnoreCase(keycolid1)) continue;
            keycolumnlist = list;
        }
        if (!columnlist.contains(parentcolumnid)) {
            columnlist.add(parentcolumnid);
        }
        this.element.setProperty("pagedata", pagedata);
        if (keycolumnlist == null) {
            return BaseItem.toErrorString(tp.translate("Element configuration error"), tp.translate("Key Column is not defined in column collection"));
        }
        String selectorcolumnid = this.element.getProperty("selectorcolumnid", this.element.getProperty("keycolid1"));
        if (!columnlist.contains(selectorcolumnid)) {
            return BaseItem.toErrorString(tp.translate("Element configuration error"), tp.translate("Selector Column not defined in columns collection"));
        }
        this.element.setProperty("selectorcolumnindex", String.valueOf(columnlist.indexOf(selectorcolumnid)));
        PropertyList selectorcolumnlist = columns.getPropertyList(columnlist.indexOf(selectorcolumnid));
        StringBuilder sb = new StringBuilder();
        int size = 0;
        try {
            DataSet ds = this.getDataSet(columnlist);
            String spinnerimgurl = "WEB-CORE/images/spinners/flat_blue_spinner.svg";
            PropertyList GUIPolicy = this.getConfigurationProcessor().getPolicy("GUIPolicy", "Sapphire Custom");
            if (GUIPolicy != null) {
                spinnerimgurl = GUIPolicy.getPropertyListNotNull("loadingpanel").getProperty("image", spinnerimgurl);
            }
            sb.append("<script>var spinnerimgurl = '").append(spinnerimgurl).append("';</script>");
            sb.append("\n<style>");
            sb.append("\n#treelist_body {height:400px;width:100%;overflow-y:auto;padding-bottom:10px;}");
            sb.append("\n#treeloaddiv {position:absolute;padding:10px;border:1px solid #005E8A;background:beige;opacity:0.9;z-index:9999;text-align:center;}");
            sb.append("\n.treelist_tableheadcell {background-color: #245C91;color:white;text-align:left;text-decoration:none;border-left:1px solid rgba(200,200,200,0.3);padding-left:3px;padding-top:3px;height:29px;font-size:9pt;font-weight:bold;}");
            sb.append("\n</style>");
            sb.append(JavaScriptAPITag.getJQueryAPI(false, false, null, this.pageContext));
            sb.append("<script type=\"text/javascript\" src=\"WEB-OPAL/pagetypes/treelist/scripts/treelist.js\"></script>");
            sb.append("<script type=\"text/javascript\" src=\"WEB-OPAL/elements/treelist/scripts/treelist.js\"></script>");
            sb.append("<script type=\"text/javascript\" src=\"WEB-CORE/scripts/sapphirecore.js\"></script>");
            sb.append("<script type=\"text/javascript\">var __treelist_elementid = '").append(this.element.getId()).append("';</script>");
            sb.append("<div id='treelist_header'>");
            sb.append("<div class=\"list_selectedcount\">");
            sb.append("&nbsp;[&nbsp;<span id=\"selectedcount\">0</span>&nbsp;selected&nbsp;]");
            sb.append("<span style='font-weight:normal;color:black;'>&nbsp;[&nbsp;<span id=\"totalcount\">").append(ds != null ? Integer.valueOf(ds.size()) : "0").append("</span>&nbsp;total&nbsp;]</span>");
            sb.append("<div id='treelist_body'>");
            sb.append("<table class='list_table' cellspacing='0' cellpadding='3' id='treelist_").append(this.element.getId()).append("' border='0'>");
            sb.append("<thead><tr>").append(TreeList.renderTableHeader(this.element, selectorcolumnlist, this.getTranslationProcessor())).append("</tr></thead>");
            if (ds != null) {
                boolean even = true;
                size = ds.size();
                HashSet<String> keyset = new HashSet<String>();
                for (int row = 0; row < size; ++row) {
                    String keyvalue = ds.getValue(row, keycolid1);
                    if (keyset.contains(keyvalue)) continue;
                    String nodeid = "tn_0_" + row;
                    keyset.add(keyvalue);
                    sb.append("<tr name='list_tablerow' class='list_tablerow");
                    sb.append(even ? "even" : "odd").append("'");
                    sb.append(" expanded=0 level='0'");
                    sb.append(" imgid=\"").append(nodeid).append("\"");
                    sb.append(">");
                    sb.append(TreeList.getRowHtml(nodeid, this.element, ds, row, 0, this.getTranslationProcessor()));
                    sb.append("</tr>");
                    boolean bl = even = !even;
                    if (expand_level != -1 && 0 >= expand_level) continue;
                    expandlist.add(nodeid);
                }
                if (ds.size() == 0) {
                    sb.append("<tr class='list_tableroweven'><td colspan='");
                    sb.append(columns.size() + 1).append("'>");
                    sb.append(NBSP).append(tp.translate("No Records Found")).append("...</td></tr>");
                }
                sb.append("</table>");
                sb.append("<div style='height:22px;'>&nbsp;</div>");
                sb.append("</div>");
                sb.append("<script>var __treelist_rootnodecount = ").append(ds.size()).append(";</script>");
                sb.append(TreeList.getPagingHTML(this.pageContext, this.element));
                sb.append(TreeList.getColumnsInfo(this.element));
            }
        }
        catch (Exception e) {
            this.logger.error("Error", e);
            return BaseItem.toErrorString("DataSet creation error", e.getMessage());
        }
        JSONObject job = this.element.toJSONObject(false);
        job.remove("pagedata");
        job.remove("sortby");
        job.remove("propertytreetype");
        job.remove("propertytreeid");
        job.remove("defaultquery");
        job.remove("elementid");
        job.remove("nodeid");
        job.remove("webpageid");
        job.remove("objectname");
        String jsonstring = job.toString();
        sb.append("<script>");
        try {
            sb.append("\nvar __element = \"").append(URLEncoder.encode(jsonstring, "utf-8")).append("\";");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("\nvar __treelistinitrowcount = ").append(size).append(";");
        if (expandlist.size() > 0) {
            sb.append("\ntreelist_expandnode( '").append(OpalUtil.toDelimitedString(expandlist, ";")).append("' );");
        }
        sb.append("\n</script>");
        sb.append("<div style='display:none' id='treelist_tempdiv'></div>");
        return sb.toString();
    }

    public static String getRowHtml(String uuid, PropertyList element, DataSet ds, int currentrow, int level, TranslationProcessor tp) throws SapphireException {
        StringBuilder sb = new StringBuilder();
        sb.append(TreeList.buildSelector(uuid, element, ds, currentrow, level, tp));
        PropertyListCollection columns = element.getCollection("columns");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String mode = column.getProperty("mode");
            if (mode.startsWith("$G{")) {
                HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
                bindMap.put("element", element);
                bindMap.put("sdc", new SDCProcessor(tp.getConnectionid()).getSDCProperties(element.getProperty("sdcid")));
                try {
                    mode = GroovyUtil.evaluate(mode, bindMap);
                }
                catch (SapphireException e) {
                    throw new SapphireException("Exception raised evaluating Groovy expression: " + mode);
                }
            }
            if ("Hidden Value".equals(mode)) continue;
            sb.append(TreeList.getColumnHtml(element, ds, currentrow, column, tp));
        }
        return sb.toString();
    }

    public DataSet getDataSet(List columnlist) throws SapphireException {
        String selectclause;
        PropertyList pagedata = this.element.getPropertyListNotNull("pagedata");
        ArrayList<String> rsetkeys = new ArrayList<String>();
        String sdcid = this.element.getProperty("sdcid");
        String keyid1 = pagedata.getProperty("keyid1", "").trim();
        String queryid = pagedata.getProperty("queryid", "").trim();
        String querywhere = pagedata.getProperty("querywhere", "").trim();
        String keycolid1 = this.element.getProperty("keycolid1");
        String parentcolumnid = this.element.getProperty("parentcolumnid");
        if ("Delete".equals(pagedata.getProperty("mode"))) {
            keyid1 = "";
        }
        if (keyid1.length() == 0 && queryid.length() == 0 && querywhere.length() == 0) {
            return new DataSet();
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRetainRsetid(true);
        sdiRequest.setSDCid(sdcid);
        if (keyid1.length() > 0) {
            sdiRequest.setKeyid1List(keyid1);
            if (pagedata.getProperty("keyid2", "").trim().length() > 0) {
                sdiRequest.setKeyid2List(pagedata.getProperty("keyid2"));
            }
            if (pagedata.getProperty("keyid3", "").trim().length() > 0) {
                sdiRequest.setKeyid3List(pagedata.getProperty("keyid3"));
            }
            if (querywhere.length() > 0) {
                sdiRequest.setQueryWhere(querywhere);
            }
        } else if (queryid.length() > 0) {
            sdiRequest.setQueryid(queryid);
            ArrayList<String> params = new ArrayList<String>();
            for (int i = 1; i <= 12 && pagedata.containsKey("param" + i); ++i) {
                params.add(pagedata.getProperty("param" + i, ""));
            }
            if (params.size() > 0) {
                String[] s = new String[params.size()];
                int index = 0;
                for (String p : params) {
                    s[index++] = p;
                }
                sdiRequest.setQueryParams(s);
            }
            if (querywhere.length() > 0) {
                sdiRequest.setQueryWhere(querywhere);
            } else {
                String queryWhereClause = OpalUtil.getColumnValue(this.getQueryProcessor(), "query", "whereclause", "queryid = ?", new String[]{queryid});
                if (queryWhereClause == null || StringUtil.getLen(queryWhereClause.trim()) == 0L) {
                    sdiRequest.setQueryWhere(parentcolumnid + " is null");
                }
            }
        } else if (StringUtil.getLen(querywhere) > 0L) {
            if (querywhere.endsWith("like lower( '%%' )")) {
                sdiRequest.setQueryFrom(this.element.getProperty("tableid"));
                sdiRequest.setQueryWhere(parentcolumnid + " is null");
            } else if (querywhere.contains("sufi.")) {
                sdiRequest.setQueryFrom(this.element.getProperty("tableid") + ", sysuserfolderitem sufi");
                querywhere = querywhere + " and sysuserid = '" + this.connectionInfo.getSysuserId() + "'";
                sdiRequest.setQueryWhere(querywhere);
            } else if (querywhere.contains("categoryitem.keyid1")) {
                sdiRequest.setQueryFrom(this.element.getProperty("tableid") + ", categoryitem");
                sdiRequest.setQueryWhere(querywhere);
            } else {
                sdiRequest.setQueryFrom(this.element.getProperty("tableid"));
                if (querywhere.contains("like lower( '%%' )")) {
                    querywhere = querywhere + " and " + parentcolumnid + " is null";
                }
                sdiRequest.setQueryWhere(querywhere);
            }
        }
        this.element.setProperty("expandrestrictivewhere", pagedata.getProperty("expandrestrictivewhere", ""));
        StringBuilder sql = new StringBuilder();
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        String rsetid = sdiData.getRsetid();
        if (sdiData == null) {
            throw new SapphireException("[ER-TL-001] " + this.getTranslationProcessor().translate("An error occured while fetching data. Please contact your Administrator if problem persists."));
        }
        DataSet ds = sdiData.getDataset("primary");
        int maxinitialrows = Integer.parseInt(this.element.getProperty("maxinitialrows", "1000"));
        if (ds.size() > maxinitialrows) {
            if (StringUtil.getLen(rsetid) > 0L) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
            throw new SapphireException("[ER-TL-002] " + this.getTranslationProcessor().translate("Number of records exceeded configured maximum number of records allowed to be rendered at root node") + " (" + maxinitialrows + ")");
        }
        this.element.setProperty("recordcount", String.valueOf(ds.size()));
        HashMap<Object, String> map = new HashMap<Object, String>();
        for (int i = 0; i < ds.size(); ++i) {
            String keycolvalue = ds.getString(i, keycolid1);
            rsetkeys.add(keycolvalue);
            map.put(keycolvalue, ds.getString(i, parentcolumnid));
        }
        HashSet<String> set = new HashSet<String>();
        for (String childid : map.keySet()) {
            if (!map.containsKey(map.get(childid))) continue;
            set.add(childid);
        }
        if (set.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            this.getQueryProcessor().execPreparedUpdate("delete from rsetitems where rsetid = " + safeSQL.addVar(rsetid) + " and keyid1 in (" + safeSQL.addIn(OpalUtil.toDelimitedString(set, "','")) + ")", safeSQL.getValues());
        }
        if (StringUtil.getLen(rsetid) > 0L) {
            sql.setLength(0);
            sql.append("select ");
            for (Object aColumnlist : columnlist) {
                sql.append(aColumnlist).append(",");
            }
            sql.append(parentcolumnid).append(" treelistparentid,");
            sql.append(keycolid1).append(",");
            sql.append(this.getChildCountSQL());
            selectclause = sql.toString();
            selectclause = this.evalTokens(pagedata, selectclause);
            this.element.setProperty("selectclause", selectclause);
            sql.setLength(0);
            PropertyListCollection sortby = this.element.getCollection("sortby");
            if (sortby != null) {
                StringBuilder sborder = new StringBuilder();
                for (int i = 0; i < sortby.size(); ++i) {
                    String columnid;
                    PropertyList list = sortby.getPropertyList(i);
                    if (list == null || (columnid = list.getProperty("columnid")).length() <= 0) continue;
                    String direction = list.getProperty("asc_desc", "a");
                    direction = direction.equals("a") ? "asc" : "desc";
                    sborder.append(sborder.length() > 0 ? ", " : "").append(columnid).append(" ").append(direction);
                }
                if (sborder.length() > 0) {
                    sql.append(" order by ").append(sborder.toString());
                }
            }
        } else {
            if (StringUtil.getLen(rsetid) > 0L) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
            throw new SapphireException("[ER-TL-003] " + this.getTranslationProcessor().translate("An error occured while fetching data. Please contact your Administrator if problem persists."));
        }
        String orderbyclause = sql.toString();
        this.element.setProperty("orderbyclause", orderbyclause);
        SafeSQL safeSQL = new SafeSQL();
        sql.setLength(0);
        sql.append(selectclause);
        sql.append(" from ").append(this.element.getProperty("tableid"));
        sql.append(" where ").append(keycolid1).append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
        sql.append(StringUtil.getLen(orderbyclause) > 0L ? orderbyclause : "");
        DataSet data = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (data != null) {
            data.addColumn("searchresult", 0);
            int size = data.size();
            for (int i = 0; i < size; ++i) {
                String s = data.getString(i, keycolid1);
                if (rsetkeys.contains(s)) {
                    data.setString(i, "searchresult", YES);
                    rsetkeys.remove(s);
                    continue;
                }
                data.setString(i, "searchresult", NO);
            }
            this.element.setProperty("rsetkeys", OpalUtil.toDelimitedString(rsetkeys, ";"));
        }
        return data;
    }

    private Object getChildCountSQL() {
        String accessControlledFlag;
        String sdcid = this.element.getProperty("sdcid");
        String tableid = this.element.getProperty("tableid");
        String keycolid1 = this.element.getProperty("keycolid1");
        StringBuilder sb = new StringBuilder();
        sb.append("( select count(t.").append(keycolid1).append(")");
        sb.append(" from ").append(tableid).append(" t");
        sb.append(" where t.").append(this.element.getProperty("parentcolumnid")).append(" = ").append(tableid).append(".").append(keycolid1);
        if (YES.equals(this.element.getProperty("activeableflag")) && !YES.equals(this.getConfigurationProcessor().getProfileProperty("viewhidden"))) {
            sb.append(" and ( t.activeflag is null or t.activeflag != 'N' )");
        }
        if ("D".equals(accessControlledFlag = this.element.getProperty("accesscontrolledflag"))) {
            sb.append(" and (");
            sb.append(" t.securityuser IS NULL OR t.securitydepartment IS NULL");
            sb.append(" OR EXISTS (");
            sb.append(" SELECT NULL FROM sdcsecurity ss");
            sb.append(" WHERE ss.sdcid = '").append(sdcid).append("'");
            sb.append(" AND ss.operationid = 'list' AND ss.sysuserid = '").append(this.connectionInfo.getSysuserId()).append("'");
            sb.append(" AND ( ( ss.accesstype = 'owner' AND ss.sysuserid = t.securityuser )");
            sb.append("  OR ( ss.accesstype = t.securitydepartment )");
            sb.append("  OR ( ss.accesstype = 'world' ) )");
            sb.append(" )");
            sb.append(" OR EXISTS (");
            sb.append(" SELECT NULL FROM sdcsecurity ss, departmentsysuser dsu");
            sb.append(" WHERE ss.sdcid = '").append(sdcid).append("'");
            sb.append(" AND ss.operationid = 'list'");
            sb.append(" AND ss.sysuserid = '").append(this.connectionInfo.getSysuserId()).append("'");
            sb.append(" AND ss.accesstype = 'member'");
            sb.append(" AND dsu.departmentid = t.securitydepartment");
            sb.append(" AND dsu.sysuserid = ss.sysuserid");
            sb.append(")");
            sb.append(" OR EXISTS (");
            sb.append(" SELECT NULL FROM sdcsecurity ss, departmentsysuser dsu, sdisecuritydepartment sd");
            sb.append(" WHERE ss.sdcid = '").append(sdcid).append("'");
            sb.append(" AND ss.operationid = 'list'");
            sb.append(" AND ss.sysuserid = '").append(this.connectionInfo.getSysuserId()).append("'");
            sb.append(" AND ss.accesstype = 'member'");
            sb.append(" AND dsu.departmentid = sd.securitydepartment");
            sb.append(" AND sd.sdcid = '").append(sdcid).append("'");
            sb.append(" AND sd.keyid1 = t.").append(keycolid1);
            sb.append(" AND dsu.sysuserid = ss.sysuserid");
            sb.append(") )");
        } else if (YES.equals(accessControlledFlag)) {
            sb.append(" and (");
            sb.append(" EXISTS ( ");
            sb.append(" SELECT null ");
            sb.append(" FROM sdirole sr,sysuserrole sur ");
            sb.append(" WHERE sur.sysuserid = '").append(this.connectionInfo.getSysuserId()).append("'");
            sb.append(" AND sur.roleid = sr.roleid ");
            sb.append(" AND sr.sdcid = '").append(sdcid).append("'");
            sb.append(" AND sr.keyid1 = t.").append(keycolid1);
            sb.append(") )");
        } else if ("L".equals(accessControlledFlag)) {
            sb.append(" and ( not exists ( select null from sdirole sr1 where sr1.sdcid = '").append(sdcid).append("' and sr1.keyid1 = t.").append(keycolid1).append(" )");
            sb.append(" or exists ( SELECT null FROM sdirole sr, sysuserrole sur WHERE sur.sysuserid = '").append(this.connectionInfo.getSysuserId()).append("' AND sur.roleid = sr.roleid  AND sr.sdcid = '").append(sdcid).append("' AND sr.keyid1 = t.").append(keycolid1).append(" ) )");
        }
        sb.append(") childcount");
        return StringUtil.replaceAll(sb.toString(), "\n", "");
    }

    private String evalTokens(PropertyList pagedata, String value) {
        String newValue = value;
        String[] tokens = StringUtil.getTokens(value);
        if (tokens != null && tokens.length > 0) {
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            M18NUtil m18n = new M18NUtil(sapphireConnection);
            for (String token : tokens) {
                newValue = token.equalsIgnoreCase("currentuser") ? StringUtil.replaceAll(newValue, "[currentuser]", sapphireConnection.getSysuserId()) : (token.equalsIgnoreCase("currentdatetime") ? StringUtil.replaceAll(newValue, "[currentdatetime]", m18n.format(m18n.getNowCalendar())) : (token.equalsIgnoreCase("currentdate") ? StringUtil.replaceAll(newValue, "[currentdate]", m18n.formatDateOnly(m18n.getNowCalendar())) : (token.startsWith("request.") ? StringUtil.replaceAll(newValue, "[" + token + "]", pagedata.getProperty(token.substring(8))) : StringUtil.replaceAll(newValue, "[" + token + "]", pagedata.getProperty(token)))));
            }
        }
        return newValue;
    }

    public static String getColumnHtml(PropertyList element, DataSet ds, int currentrow, PropertyList column, TranslationProcessor tp) {
        String value;
        StringBuilder html = new StringBuilder();
        String mode = column.getProperty("mode");
        String columnid = column.getProperty("columnid");
        if (columnid.indexOf(" ") > 0) {
            columnid = RequestParser.parseAlias(columnid);
            column.setProperty("columnid", columnid);
        }
        if (!(value = column.getProperty("pseudocolumn").length() > 0 ? TreeList.evaluateExpression(element, ds, currentrow, columnid, column.getProperty("pseudocolumn")) : ds.getValue(currentrow, columnid)).toLowerCase().startsWith("<img") || value.toLowerCase().contains("<script")) {
            value = SafeHTML.encodeForHTML(value);
        }
        boolean translate = column.getProperty("translatevalue", NO).equalsIgnoreCase(YES);
        if ("Hidden Value".equals(mode)) {
            if (translate) {
                value = tp.translate(value);
            }
            html.append("<input type='hidden' name='").append(SDIData.getDatasetCode("primary"));
            html.append(currentrow).append("_").append(column.getProperty("columnid"));
            html.append("' id='").append(SDIData.getDatasetCode("primary"));
            html.append(currentrow).append("_").append(column.getProperty("columnid"));
            html.append("' value=\"").append(value).append("\" />");
        } else {
            value = SDITagUtil.getDisplayValue(value, column.getProperty("displayvalue"));
            if (translate) {
                value = tp.translate(value);
            }
            if (value.contains("[") && value.contains("]")) {
                value = TreeList.parseExpression(null, ds, currentrow, value, tp);
            }
            PropertyList link = column.getPropertyList("link");
            html.append("<td align='").append(column.getProperty("align")).append("' class='list_tablebodycell' valign='middle'>");
            if (value == null || value.trim().length() == 0) {
                html.append(NBSP);
            } else if (link != null && link.getProperty("href").length() > 0) {
                PropertyList pagedata = element.getPropertyListNotNull("pagedata");
                html.append(TreeList.getLink(pagedata, ds, currentrow, value, link, "", translate ? tp : null));
            } else {
                html.append(column.getProperty("prefix")).append(value).append(column.getProperty("suffix"));
            }
            html.append("</td>");
        }
        return html.toString();
    }

    public static String renderTableHeader(PropertyList element, PropertyList selectorcolumnlist, TranslationProcessor translationProcessor) throws SapphireException {
        StringBuilder sb = new StringBuilder();
        String selectortype = element.getProperty("selectortype", CHECKBOX);
        String headerclass = "treelist_tableheadcell";
        String title = selectorcolumnlist.getProperty("title");
        title = translationProcessor.translate(title.length() > 0 ? title : selectorcolumnlist.getProperty("columnid"));
        if (selectortype.equalsIgnoreCase(CHECKBOX)) {
            sb.append("<td class='").append(headerclass).append("'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append("<input id='headerselector' type='checkbox' onclick=\"setallfor( this.checked );");
            sb.append(element.getProperty("checkedclause")).append("\"/>");
            sb.append(NBSP).append(title).append("</td>");
            sb.append("</td>");
        } else {
            sb.append("<td class='").append(headerclass).append("'>&nbsp;&nbsp;&nbsp;");
            sb.append(NBSP).append(title).append("</td>");
            sb.append("</td>");
        }
        PropertyListCollection columns = element.getCollectionNotNull("columns");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String mode = column.getProperty("mode");
            if (mode.startsWith("$G{")) {
                HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
                bindMap.put("element", element);
                bindMap.put("sdc", new SDCProcessor(translationProcessor.getConnectionid()).getSDCProperties(element.getProperty("sdcid")));
                try {
                    mode = GroovyUtil.evaluate(mode, bindMap);
                }
                catch (SapphireException e) {
                    throw new SapphireException("Exception raised evaluating Groovy expression: " + mode);
                }
            }
            if ("Hidden Value".equals(mode)) continue;
            String columnid = column.getProperty("columnid");
            if (columnid.indexOf(" ") > 0) {
                columnid = RequestParser.parseAlias(columnid);
            }
            String columntitle = column.getProperty("title").length() > 0 ? column.getProperty("title") : columnid;
            String width = column.getProperty("width");
            String widthclause = width != null && width.length() > 0 ? " width=\"" + width + "\"" : "";
            String align = column.getProperty("align");
            String alignclause = align != null && align.length() > 0 ? " align=\"" + align + "\"" : "";
            sb.append("<td class='").append(headerclass).append("' ").append(widthclause);
            sb.append(alignclause).append(">").append(columntitle).append("</td>\n");
        }
        return sb.toString();
    }

    public static String getReturnValue(PropertyList element, DataSet ds, int currentrow) {
        PropertyListCollection columns = element.getCollectionNotNull("columns");
        String sdcid = element.getProperty("sdcid");
        String keycolid1 = element.getProperty("keycolid1");
        String keycolid2 = element.getProperty("keycolid2");
        String keycolid3 = element.getProperty("keycolid3");
        ArrayList<String> list = new ArrayList<String>();
        if (columns != null) {
            for (int i = 0; i < columns.size(); ++i) {
                if (!YES.equals(columns.getPropertyList(i).getProperty("returnvalue", NO))) continue;
                String columnid = RequestParser.parseAlias((String)columns.getPropertyList(i).get("columnid"));
                list.add(columnid);
            }
        }
        if (list.size() == 0) {
            if (sdcid.length() > 0) {
                list.add(keycolid1);
                if (keycolid2.length() > 0) {
                    list.add(keycolid2);
                }
                if (keycolid3.length() > 0) {
                    list.add(keycolid3);
                }
            } else if (columns != null && columns.size() > 0) {
                String firstcolid = columns.getPropertyList(0).getProperty("columnid");
                list.add(firstcolid);
            }
        }
        String s = OpalUtil.toDelimitedString(list, ";");
        element.setProperty("returncolids", s);
        StringBuilder returnvalue = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                returnvalue.append("|");
            }
            returnvalue.append(ds.getValue(currentrow, (String)list.get(i)));
        }
        if (returnvalue.length() == 0) {
            returnvalue.append(ds.getValue(currentrow, keycolid1));
        }
        return returnvalue.toString();
    }

    private static String buildSelector(String uuid, PropertyList element, DataSet ds, int currentrow, int level, TranslationProcessor tp) {
        int i;
        StringBuilder html = new StringBuilder();
        String selectorType = element.getProperty("selectortype", CHECKBOX);
        String selectorcolumnid = element.getProperty("selectorcolumnid", element.getProperty("keycolid1"));
        PropertyListCollection columns = element.getCollection("columns");
        PropertyList selectorcolumnlist = columns.getPropertyList(Integer.parseInt(element.getProperty("selectorcolumnindex")));
        int childcount = ds.getInt(currentrow, "childcount");
        String keyid1 = ds.getValue(currentrow, element.getProperty("keycolid1"));
        String keyid2 = ds.getValue(currentrow, element.getProperty("keycolid2"));
        String keyid3 = ds.getValue(currentrow, element.getProperty("keycolid2"));
        String returnvalue = TreeList.getReturnValue(element, ds, currentrow);
        boolean searchResult = YES.equals(ds.getValue(currentrow, "searchresult"));
        String value = ds.getValue(currentrow, selectorcolumnid, "");
        value = SafeHTML.encodeForHTML(value);
        PropertyList link = selectorcolumnlist.getPropertyList("link");
        String width = selectorcolumnlist.getProperty("width", "200");
        value = SDITagUtil.getDisplayValue(value, selectorcolumnlist.getProperty("displayvalue"));
        boolean translate = selectorcolumnlist.getProperty("translatevalue", NO).equalsIgnoreCase(YES);
        if (translate) {
            value = tp.translate(value);
        }
        if (value == null || value.length() == 0) {
            value = NBSP;
        } else if (link != null && link.getProperty("href").length() > 0) {
            PropertyList pagedata = element.getPropertyListNotNull("pagedata");
            value = TreeList.getLink(pagedata, ds, currentrow, value, link, searchResult ? "treelinkselect" : "treelink", translate ? tp : null);
        }
        html.append("<td class='list_tablebodycell' align='left' valign='middle' level='").append(level).append("' width='").append(width).append("'>");
        for (i = 0; i < level; ++i) {
            html.append("<img src='WEB-OPAL/images/blank16.gif' class='treenode' />");
        }
        if (childcount > 0) {
            html.append("<img id='").append(uuid).append("' src='WEB-OPAL/images/plus16.gif' ");
            html.append("onclick=\"treelist_en( this )\" style='cursor:pointer' ");
            html.append(" cc='").append(childcount).append("' dataloaded='N' datalevel='").append(level).append("' datakeyid1='").append(keyid1).append("'/>");
        } else {
            html.append("<img src='WEB-OPAL/images/node16.gif' />");
        }
        if (selectorType.equals(CHECKBOX)) {
            html.append("<input type=\"checkbox\" onclick=\"selectorclicked( this, event )\" name=\"selector\" id=\"");
            html.append(returnvalue).append("\" primaryrow=\"").append(currentrow).append("\" ");
            html.append(element.get("checkedclause"));
            html.append(" keyid1=\"").append(keyid1).append("\"  keyid2=\"").append(keyid2).append("\"  keyid3=\"").append(keyid3);
            html.append("\">");
        } else if (selectorType.equals(RADIOBUTTON)) {
            html.append("<input type=\"radio\" onclick=\"selectorclicked( this, event )\" name=\"selector\" id=\"");
            html.append(returnvalue).append("\"");
            html.append(" keyid1=\"").append(keyid1).append("\"  keyid2=\"").append(keyid2).append("\"  keyid3=\"").append(keyid3);
            html.append("\">");
        }
        if (searchResult) {
            html.append("<b><font color='#1071AD'>");
            html.append(NBSP.equals(value) ? "<i>" + keyid1 + "</i>" : value);
            html.append("</font></b>");
        } else {
            html.append("<font color='gray'><i>");
            html.append(NBSP.equals(value) ? "<i>" + keyid1 + "</i>" : value);
            html.append("</i></font>");
        }
        for (i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            if (!"Hidden Value".equals(column.getProperty("mode"))) continue;
            String columnid = column.getProperty("columnid");
            if (columnid.indexOf(" ") > 0) {
                columnid = RequestParser.parseAlias(columnid);
                column.setProperty("columnid", columnid);
            }
            value = column.getProperty("pseudocolumn").length() > 0 ? TreeList.evaluateExpression(element, ds, currentrow, columnid, column.getProperty("pseudocolumn")) : ds.getValue(currentrow, columnid);
            translate = column.getProperty("translatevalue", NO).equalsIgnoreCase(YES);
            if (translate) {
                value = tp.translate(value);
            }
            html.append("<input type='hidden' name='").append(SDIData.getDatasetCode("primary"));
            html.append(currentrow).append("_").append(column.getProperty("columnid"));
            html.append("' id='").append(SDIData.getDatasetCode("primary"));
            html.append(currentrow).append("_").append(column.getProperty("columnid"));
            html.append("' value=\"").append(value).append("\" />");
        }
        html.append("</td>");
        return html.toString();
    }

    private static void setElementDefault(PropertyList element) {
        PropertyListCollection columns = element.getCollectionNotNull("columns");
        String keycolid1 = element.getProperty("keycolid1");
        String desccol = element.getProperty("desccol");
        if (columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (columnid.equals("keycolid1")) {
                    column.setProperty("columnid", keycolid1);
                    continue;
                }
                if (!columnid.equals("desccolid") && !columnid.equals("desccol") || desccol == null || desccol.length() <= 0) continue;
                column.setProperty("columnid", desccol);
            }
        }
    }

    public static String getLink(PropertyList pagedata, DataSet ds, int currentrow, String value, PropertyList link, String className, TranslationProcessor tp) {
        StringBuilder html = new StringBuilder();
        String target = link.getProperty("target");
        if (target == null || target.length() == 0) {
            target = "_self";
        }
        String href = link.getProperty("href");
        href = TreeList.parseExpression(pagedata, ds, currentrow, href, null);
        String title = link.getProperty("tip");
        title = TreeList.parseExpression(pagedata, ds, currentrow, title, tp);
        html.append("<a href=\"").append(href).append("\" target=\"").append(target).append("\"");
        if (title != null && title.length() > 0) {
            html.append(" title=\"").append(title).append("\"");
        }
        if (StringUtil.getLen(className) > 0L) {
            html.append(" class='").append(className).append("'>").append(value).append("</a>");
        } else {
            html.append(">").append(value).append("</a>");
        }
        return html.toString();
    }

    public static String parseExpression(PropertyList pagedata, DataSet ds, int currentrow, String expression, TranslationProcessor tp) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(expression)) {
            String value = "";
            if (token.contains("=")) {
                int equalindex = token.indexOf("=");
                String columntype = token.substring(0, equalindex);
                String column = token.substring(equalindex + 1);
                if ("columnid".equals(columntype)) {
                    value = ds.getValue(currentrow, column);
                } else if ("param".equals(columntype)) {
                    value = pagedata != null ? pagedata.getProperty(column) : "";
                }
            } else if ("rowid".equals(token)) {
                value = String.valueOf(currentrow);
            } else if (ds.isValidColumn(token)) {
                value = ds.getValue(currentrow, token);
            }
            if (value.length() <= 0) continue;
            value = tp != null ? tp.translate(value) : value;
            expression = StringUtil.replaceAll(expression, "[" + token + "]", value);
        }
        return expression;
    }

    public static String evaluateExpression(PropertyList element, DataSet ds, int currentrow, String columnid, String expression) {
        String[] tokens = StringUtil.getTokens(expression);
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                expression = token.equals("columnid") ? StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, columnid)) : (token.startsWith("columnid=") ? StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, token.substring(token.indexOf(61) + 1))) : (token.equalsIgnoreCase("sdcid") || token.equalsIgnoreCase("sdc") ? StringUtil.replaceAll(expression, "[" + token + "]", element.getProperty("sdcid")) : (token.equalsIgnoreCase("keycolid1") ? StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, element.getProperty("keycolid1"))) : (token.equalsIgnoreCase("keycolid2") ? StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, element.getProperty("keycolid2"))) : (token.equalsIgnoreCase("keycolid3") ? StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, element.getProperty("keycolid3"))) : StringUtil.replaceAll(expression, "[" + token + "]", ds.getValue(currentrow, token)))))));
            }
        }
        return expression;
    }

    private static String getPagingHTML(PageContext pageContext, PropertyList element) {
        StringBuilder paginghtml = new StringBuilder();
        PropertyList pagedata = element.getPropertyListNotNull("pagedata");
        paginghtml.append("\n<form id=\"pagingform\" name=\"pagingform\" method=\"post\" action=\"rc?command=");
        paginghtml.append(pagedata.getProperty("command")).append("&").append(pagedata.getProperty("command")).append("=").append(pagedata.getProperty("page"));
        if (pageContext.getRequest().getParameter("_iframename") != null) {
            paginghtml.append("&_iframename=").append(pageContext.getRequest().getParameter("_iframename"));
        }
        paginghtml.append("\">\n");
        paginghtml.append("<input name=\"pageno\" type=\"hidden\" value=\"0\"/>\n");
        paginghtml.append("<input name=\"rowsperpage\" type=\"hidden\" value=\"100\"/>\n");
        paginghtml.append("<input name=\"sortby\" type=\"hidden\" value=\"\"/>\n");
        paginghtml.append("<input name=\"groupby\" type=\"hidden\" value=\"\"/>\n");
        String allselected = pageContext.getRequest().getParameter("allselected") == null ? "" : pageContext.getRequest().getParameter("allselected");
        paginghtml.append("<input name=\"allselected\" type=\"hidden\" value=\"").append(allselected).append("\"/>\n");
        paginghtml.append("<input name=\"sdcid\" type=\"hidden\" value=\"").append(element.getProperty("sdcid")).append("\"/>\n");
        paginghtml.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(element.getProperty("keyid1")).append("\"/>\n");
        paginghtml.append("<input name=\"keyid2\" type=\"hidden\" value=\"").append(element.getProperty("keyid2")).append("\">\n");
        paginghtml.append("<input name=\"keyid3\" type=\"hidden\" value=\"").append(element.getProperty("keyid3")).append("\">\n");
        paginghtml.append("<input name=\"totalrows\" type=\"hidden\" value=\"\"/>\n");
        paginghtml.append("<input name=\"qualifiedrows\" type=\"hidden\" value=\"\"/>\n");
        paginghtml.append("</form>");
        return paginghtml.toString();
    }

    private static String getColumnsInfo(PropertyList element) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<script>");
        sb.append("\nvar columnlistmap = new Array();");
        sb.append("\ncolumnlistmap['list_keycolidlist']='").append(TreeList.getKeyColumnsIds(element)).append("';");
        sb.append("\ncolumnlistmap['list_returncolidlist']='").append(TreeList.getReturnColumnIds(element)).append("';");
        sb.append("\ncolumnlistmap['list_columnidlist']='").append(TreeList.getDisplayedColumnIds(element)).append("';");
        sb.append("\ncolumnlistmap['").append(element.getId()).append("_keycolidlist']='").append(TreeList.getKeyColumnsIds(element)).append("';");
        sb.append("\ncolumnlistmap['").append(element.getId()).append("_returncolidlist']='").append(TreeList.getReturnColumnIds(element)).append("';");
        sb.append("\ncolumnlistmap['").append(element.getId()).append("_columnidlist']='").append(TreeList.getDisplayedColumnIds(element)).append("';");
        sb.append("\n</script>\n");
        return sb.toString();
    }

    private static String getKeyColumnsIds(PropertyList element) {
        return element.getProperty("keycolid1");
    }

    private static String getReturnColumnIds(PropertyList element) {
        return element.getProperty("returncolids");
    }

    private static String getDisplayedColumnIds(PropertyList element) {
        StringBuilder sb = new StringBuilder();
        PropertyListCollection columns = element.getCollection("columns");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList list = columns.getPropertyList(i);
            String columnid = list.getProperty("columnid");
            if (columnid.contains(" ")) continue;
            sb.append(columnid).append(";");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}

