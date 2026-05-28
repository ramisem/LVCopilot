/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package com.labvantage.opal.elements.sqlview;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SQLView
extends BaseItem {
    public SQLView() {
        this.setViewOnly(true);
    }

    @Override
    protected String getMainHtml() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbHeader = new StringBuilder();
        StringBuilder sbBody = new StringBuilder();
        try {
            PropertyListCollection columns = this.element.getCollectionNotNull("columns");
            String selector = this.element.getProperty("selector", "none");
            DataSet ds = this.getDataSet();
            if ("Y".equals(this.element.getProperty("showrecordcount"))) {
                sb.append("<div style='font:bold 9 verdana, sans-serif;padding:2px;color:#666666'>[ ").append(ds.size()).append(" record").append(ds.size() > 1 ? "s" : "").append(" ]</div>");
            }
            if (columns != null && columns.size() > 0) {
                if (ds.size() > 0) {
                    GroovyUtil grooyUtil = GroovyUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                    this.setDateFormat(columns, ds, this.pageContext);
                    for (int i = 0; i < ds.size(); ++i) {
                        sbBody.append("<tr height=25>");
                        if ("checkbox".equals(selector)) {
                            sbBody.append("<td class='maintform_field' width='30' align=center height=26><input type=checkbox></td>");
                        } else if ("radio".equals(selector)) {
                            sbBody.append("<td class='maintform_field' width='30' align=center height=26><input type=radio name='").append(this.element.getId()).append("_selector'></td>");
                        }
                        int index = 0;
                        for (String columnid : this._ColumnsList) {
                            String value;
                            PropertyList columnList = columns.getPropertyList(index);
                            String show = columnList.getProperty("show", "Y");
                            if (show.startsWith("$G{")) {
                                HashMap<String, Object> groovyMap = new HashMap<String, Object>();
                                groovyMap.put("user", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getUserAttributeMap());
                                for (int col = 0; col < ds.getColumnCount(); ++col) {
                                    String column = ds.getColumnId(col);
                                    String value2 = ds.getValue(i, column, "");
                                    groovyMap.put(column, value2);
                                }
                                show = grooyUtil.evaluateSecure(show, groovyMap);
                                columnList.setProperty("show", show);
                            }
                            if ("N".equals(show)) {
                                ++index;
                                continue;
                            }
                            if (ds.isValidColumn(columnid)) {
                                String link;
                                value = ds.getValue(i, columnid, "&nbsp;");
                                String displayvalue = (String)this._DisplayValueList.get(index);
                                if (StringUtil.getLen(displayvalue) > 0L) {
                                    value = OpalUtil.parseDisplayValue(value, displayvalue);
                                }
                                String target = (link = (String)this._LinkHrefList.get(index)).startsWith("javascript") ? "" : (String)this._LinkTargetList.get(index);
                                String tip = (String)this._LinkTipList.get(index);
                                if (StringUtil.getLen(target) > 0L) {
                                    target = " target='" + target + "'";
                                }
                                if (StringUtil.getLen(tip) > 0L) {
                                    tip = " title='" + tip + "'";
                                }
                                if (StringUtil.getLen(link) > 0L) {
                                    link = this.parseTokens(ds, i, link);
                                    value = "<a href=\"" + link + "\"" + tip + target + ">" + value + "</a>";
                                }
                                if ("Y".equals(this._TranslateList.get(index))) {
                                    value = this.getTranslationProcessor().translate(value);
                                }
                            } else {
                                String pseudocolumn;
                                value = "[rownum]".equals(columnid) ? String.valueOf(i + 1) : (OpalUtil.isEmpty(columnid) ? (OpalUtil.isNotEmpty(pseudocolumn = (String)this._PseudoList.get(index)) ? (pseudocolumn = this.parseTokens(ds, i, pseudocolumn)) : "&nbsp;") : "&nbsp;");
                            }
                            sbBody.append("<td class='maintform_field' align='").append((String)this._AlignList.get(index)).append("' style='padding:2px'");
                            if (2 == ds.getColumnType(columnid)) {
                                sbBody.append(" NOWRAP");
                            }
                            sbBody.append(">");
                            if (value.contains("</script>")) {
                                sbBody.append(SafeHTML.encodeForHTML(value));
                            } else {
                                sbBody.append(value);
                            }
                            sbBody.append("</td>");
                            ++index;
                        }
                        sbBody.append("</tr>");
                    }
                } else {
                    sbBody.append("<tr><td class='maintform_field' colspan='").append(this._ColumnsList.size()).append("'>").append(this.getTranslationProcessor().translate("No Records Found")).append("</td></tr>");
                }
                sbHeader.append("<tr>");
                if ("checkbox".equals(selector)) {
                    sbHeader.append("<th class='maintform_fieldtitle' width='30' align=center height=26><input type=checkbox></th>");
                } else if ("radio".equals(selector)) {
                    sbHeader.append("<th class='maintform_fieldtitle' width='30'>&nbsp;</th>");
                }
                int index = 0;
                for (String title : this._TitleList) {
                    PropertyList columnList = columns.getPropertyList(index);
                    if ("Y".equals(columnList.getProperty("show", "Y"))) {
                        sbHeader.append("<th class='maintform_fieldtitle' width='").append((String)this._WidthList.get(index)).append("' align=center height=26><b>").append(title).append("</b></th>");
                    }
                    ++index;
                }
                sbHeader.append("</tr>");
            } else {
                int i;
                sbHeader.append("<tr>");
                for (i = 0; i < ds.getColumnCount(); ++i) {
                    String columnid = ds.getColumnId(i);
                    sbHeader.append("<th class='maintform_fieldtitle' align=center height=26><b>").append(columnid.toUpperCase()).append("</b></th>");
                }
                sbHeader.append("</tr>");
                if (ds.size() > 0) {
                    for (i = 0; i < ds.size(); ++i) {
                        sbBody.append("<tr height=25>");
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String columnid = ds.getColumnId(col);
                            String value = ds.getValue(i, columnid, "&nbsp;");
                            sbBody.append("<td class='maintform_field' style='padding:2px'>");
                            sbBody.append(value);
                            sbBody.append("</td>");
                        }
                        sbBody.append("</tr>");
                    }
                } else {
                    sbBody.append("<tr><td class='maintform_field' colspan='").append(ds.getColumnCount()).append("'>").append(this.getTranslationProcessor().translate("No Records Found")).append("</td></tr>");
                }
            }
            sb.append("<table cellpadding=0 cellspacing=0 border=0>");
            if (!this.getButtonPlacement("top").equals("none")) {
                sb.append("<tr><td>");
                sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
                sb.append("<tr><td width='100%' align='").append(this.getButtonPlacement("top")).append("'>");
                sb.append(this.getButtons(this.element.getId()));
                sb.append("</td></tr></table>");
                sb.append("</td></tr>");
            }
            sb.append("<tr><td>");
            sb.append("<table cellpadding=0 cellspacing=0 border=0>");
            sb.append((CharSequence)sbHeader);
            sb.append((CharSequence)sbBody);
            sb.append("</table>");
            sb.append("</td></tr>");
            sb.append("<tr><td>");
            if (!this.getButtonPlacement("bottom").equals("none")) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
                sb.append("<tr><td width='100%' align='").append(this.getButtonPlacement("bottom")).append("'>");
                sb.append(this.getButtons(this.element.getId()));
                sb.append("</td></tr></table>");
            }
            sb.append("</table>");
        }
        catch (SapphireException e) {
            sb.setLength(0);
            sb.append("<div style='color:red;font:normal 12 verdana'>").append(e.getMessage()).append("</div>");
        }
        return sb.toString();
    }

    private String parseTokens(DataSet ds, int row, String str) {
        String[] tokens;
        for (String key : tokens = StringUtil.getTokens(str)) {
            String keyvalue;
            if (ds.isValidColumn(key)) {
                keyvalue = ds.getValue(row, key, "");
            } else if (key.startsWith("columnid=")) {
                String column = key.substring(8);
                keyvalue = ds.getValue(row, column, "");
            } else {
                keyvalue = key.equals("rownum") ? String.valueOf(row + 1) : this.requestContext.getProperty(key);
            }
            str = StringUtil.replaceAll(str, "[" + key + "]", keyvalue);
        }
        return str;
    }

    protected DataSet getDataSet() throws SapphireException {
        String sql = this.element.getProperty("sql");
        int loopCount = 0;
        SafeSQL safeSQL = new SafeSQL();
        int start = sql.indexOf("'[");
        while (start != -1) {
            int end = sql.indexOf("]'");
            String key = sql.substring(start + 2, end);
            String value = "";
            if ("sysuserid".equals(key) || "currentuser".equals(key)) {
                value = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                sql = StringUtils.replaceOnce((String)sql, (String)("'[" + key + "]'"), (String)safeSQL.addVar(value));
            } else {
                value = this.requestContext.getProperty(key);
                sql = value.contains(";") ? StringUtils.replaceOnce((String)sql, (String)("'[" + key + "]'"), (String)safeSQL.addIn(value, ";")) : StringUtils.replaceOnce((String)sql, (String)("'[" + key + "]'"), (String)safeSQL.addVar(value));
            }
            start = sql.indexOf("'[");
            if (++loopCount <= 100) continue;
            throw new SapphireException("Failed to fetch data");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query:<br>" + sql, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }
}

