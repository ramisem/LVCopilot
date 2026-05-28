/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class URLGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String URL_PROPERTY = "url";

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(URL_PROPERTY, "Y");
        return up;
    }

    @Override
    public PropertyList getParameters() {
        PropertyList out = super.getParameters();
        if (!out.containsKey("paramitems")) {
            out.setProperty("paramitems", new PropertyListCollection());
        }
        if (out.getCollection("paramitems").find("paramid", "close") == null) {
            PropertyList param = new PropertyList();
            param.setProperty("paramid", "close");
            param.setProperty("value", "[close]");
            out.getCollection("paramitems").add(param);
        }
        return out;
    }

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        this.setCount(this.evalCount());
        return true;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        if (this.element != null) {
            String url = LABVANTAGE_CVS_ID;
            String defaulturl = this.evaluateExpression(this.getURL(), BaseGizmo.I18NFormat.CLIENT);
            PropertyListCollection params = this.element.getCollection("parameters");
            if (params != null) {
                for (int i = 0; i < params.size(); ++i) {
                    String purl;
                    PropertyList param = params.getPropertyList(i);
                    String paramvalue = this.evaluateExpression("[" + param.getProperty("paramid") + "]", BaseGizmo.I18NFormat.SERVER);
                    if (paramvalue.startsWith("[") && paramvalue.endsWith("]")) {
                        paramvalue = LABVANTAGE_CVS_ID;
                    }
                    String expression = param.getProperty("expression", "Not Null");
                    String value = this.evaluateExpression(param.getProperty("value", LABVANTAGE_CVS_ID), BaseGizmo.I18NFormat.CLIENT);
                    if (value.startsWith("[") && value.endsWith("]")) {
                        value = LABVANTAGE_CVS_ID;
                    }
                    if ((purl = this.evaluateExpression(this.getURL(param, this.element.getProperty("target")), BaseGizmo.I18NFormat.CLIENT)).startsWith("[") && purl.endsWith("]")) {
                        purl = LABVANTAGE_CVS_ID;
                    }
                    if (purl.length() <= 0) continue;
                    if (expression.equalsIgnoreCase("Not Null") && paramvalue.length() > 0) {
                        url = purl;
                        break;
                    }
                    if (expression.equalsIgnoreCase("Equal To") && paramvalue.equalsIgnoreCase(value)) {
                        url = purl;
                        break;
                    }
                    if (expression.equalsIgnoreCase("Null") && paramvalue.length() == 0) {
                        url = purl;
                        break;
                    }
                    if (expression.equalsIgnoreCase("Not Equal To") && !paramvalue.equalsIgnoreCase(value)) {
                        url = purl;
                        break;
                    }
                    if (!expression.equalsIgnoreCase("Contains") || !paramvalue.toLowerCase().contains(value.toLowerCase())) continue;
                    url = purl;
                    break;
                }
                if (url.length() == 0) {
                    url = defaulturl;
                }
            }
            if (url.startsWith("[") && url.endsWith("]") || url.length() == 0) {
                String htmltext = this.element.getProperty("defaulthtml", LABVANTAGE_CVS_ID);
                htmltext = htmltext.length() > 0 ? this.evaluateExpression(htmltext, BaseGizmo.I18NFormat.CLIENT) : "No URL or HTML provided.";
                html.append("<font size=2>").append(htmltext).append("</font>");
            } else if (url.startsWith("javascript:") || url.startsWith("sapphire.lookup.") || url.startsWith("sapphire.ui.") || url.startsWith("sapphire.page.")) {
                this.setGizmoStyle(BaseGizmo.GizmoStyle.LARGETEXT);
                html.append("<div style=\"width:100%;height:100%;text-align:center;margin-top:15%;\">");
                html.append(this.getIconHtml());
                html.append("</div>");
            } else {
                boolean cancont;
                if (url.toLowerCase().indexOf("rc?") == 0) {
                    cancont = true;
                } else {
                    try {
                        URLConnection urlConnection = new URL(url).openConnection();
                        urlConnection.connect();
                        cancont = true;
                    }
                    catch (IOException eException2) {
                        cancont = false;
                    }
                }
                if (cancont) {
                    html.append(this.opencloseURLResultFrame(url));
                } else {
                    html.append("<font size=2>").append(translationProcessor.translate("Cannot connect to URL")).append(url).append("</font>");
                }
            }
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    private String opencloseURLResultFrame(String sURL) {
        StringBuffer sb = new StringBuffer(LABVANTAGE_CVS_ID);
        sb.append("<iframe frameborder=0 src=\"").append(sURL).append("\" style=\"width: 100%; height: 100%;\" name=\"").append(this.elementid).append("_iframe\" id=\"").append(this.elementid).append("_iframe\"></iframe>");
        return sb.toString();
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getURL() {
        String t = this.element.getProperty("target", LABVANTAGE_CVS_ID);
        return this.getURL(this.element, t);
    }

    public String getURL(PropertyList pl, String target) {
        String t = target;
        if (t.length() == 0) {
            return pl.getProperty(URL_PROPERTY, LABVANTAGE_CVS_ID);
        }
        if (!pl.getProperty(URL_PROPERTY, LABVANTAGE_CVS_ID).startsWith("javascript:")) {
            if (t.equalsIgnoreCase("_blank")) {
                return "javascript:sapphire.ui.dialog.open('LabVantage','" + pl.getProperty(URL_PROPERTY, LABVANTAGE_CVS_ID) + "','" + t + "');";
            }
            return "javascript:sapphire.page.navigate('" + pl.getProperty(URL_PROPERTY, LABVANTAGE_CVS_ID) + "','" + t + "');";
        }
        return pl.getProperty(URL_PROPERTY, LABVANTAGE_CVS_ID);
    }

    @Override
    public String getIcon() {
        return this.getImage(this.element.getPropertyList("gizmoprops") != null ? this.element.getPropertyList("gizmoprops").getProperty("title", this.getURL()) : this.getURL(), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "FlatBlackFile1";
    }

    @Override
    public int evalCount() {
        String querytype;
        PropertyList count = this.element.getPropertyList("count");
        String string = querytype = count != null ? count.getProperty("querytype") : LABVANTAGE_CVS_ID;
        if (querytype.length() > 0) {
            if (querytype.equals("sdirequest")) {
                PropertyList sdirequest = count.getPropertyList("sdirequest");
                return URLGizmo.getSDICount(this.getSDIProcessor(), sdirequest.getProperty("sdcid"), sdirequest.getProperty("queryid"), sdirequest.getCollection("params"), sdirequest.getProperty("queryfrom"), sdirequest.getProperty("querywhere"));
            }
            if (querytype.equals("sql")) {
                String sql = count.getPropertyList("sql").getProperty("sql");
                if (sql.length() > 0) {
                    DataSet countdataset = this.getQueryProcessor().getSqlDataSet(LABVANTAGE_CVS_ID, StringUtil.replaceAll(StringUtil.replaceAll(sql, "[%currentuser%]", this.connectionInfo.getSysuserId()), "[currentuser]", this.connectionInfo.getSysuserId()), false, -1, false);
                    if (countdataset == null || countdataset.size() != 1) {
                        this.logger.error("Count SQL must return a single row with a single column called 'count' (" + this.getGizmoDefId() + ")");
                    }
                    return countdataset != null && countdataset.size() == 1 ? countdataset.getInt(0, countdataset.getColumnId(0)) : -1;
                }
                return -1;
            }
            return -1;
        }
        return -1;
    }
}

