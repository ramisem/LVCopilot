/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.util.DashboardMeter;
import com.labvantage.sapphire.pageelements.controls.Image;
import java.text.DecimalFormat;
import javax.servlet.http.HttpSession;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MeterGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 91120 $";
    public static final String DRILLDOWNTARGET_PROPERTY = "drilldowntarget";
    public static final String DISPLAYTITLE_PROPERTY = "displaytitle";
    public static final String MONITORID_PROPERTY = "monitorid";
    public static final String MONITORSQL_PROPERTY = "monitorsql";
    public static final String METERSTYLE_PROPERTY = "meterstyle";
    public static final String METERCATEGORY_PROPERTY = "metercategory";
    public static final String METERUNITS_PROPERTY = "meterunits";
    public static final String METERRANGELOW_PROPERTY = "meterrangelow";
    public static final String METERRANGEHIGH_PROPERTY = "meterrangehigh";
    public static final String BACKGROUNDIMAGEURL_PROPERTY = "backgroundimageurl";
    public static final int WIDTH_CORRECTION = 15;
    public static final int HEIGHT_CORRECTION = 30;
    private DashboardMeter dm = null;
    private DataSet monitordata = null;
    private String defaultImageSrc = "";

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(DISPLAYTITLE_PROPERTY, "Y");
        up.setProperty(METERSTYLE_PROPERTY, "Y");
        up.setProperty(METERCATEGORY_PROPERTY, "Y");
        up.setProperty(METERUNITS_PROPERTY, "Y");
        return up;
    }

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.evaluateProps(this.element, new String[]{DISPLAYTITLE_PROPERTY, "subtitle", METERUNITS_PROPERTY}, BaseGizmo.I18NFormat.CLIENT);
        this.evaluateProps(this.element, new String[]{"data", "dataaggregate", MONITORSQL_PROPERTY}, BaseGizmo.I18NFormat.DATABASE);
        this.dm = new DashboardMeter();
        String connid = this.getConnectionId();
        this.dm.setSDCProcessor(new SDCProcessor(connid));
        this.dm.setQp(new QueryProcessor(connid));
        this.dm.setTranslationProcessor(new TranslationProcessor(connid));
        String ms = this.element.getProperty(METERSTYLE_PROPERTY, "");
        String image = "";
        image = ms.equalsIgnoreCase("thermometer") ? "Thermometer" : (ms.equalsIgnoreCase("panel gauge") ? "Odometer" : (ms.equalsIgnoreCase("html") ? "TextRichColored" : (ms.equalsIgnoreCase("circular gauge") ? "Gauge" : "Multimeter")));
        return true;
    }

    private void evaluateProps(PropertyList props, String[] names, BaseGizmo.I18NFormat i18NFormat) {
        for (String name : names) {
            if (!props.containsKey(name)) continue;
            if (props.get(name) instanceof String) {
                props.setProperty(name + "_copy", this.evaluateExpression(props.getProperty(name), i18NFormat));
                continue;
            }
            if (!(props.get(name) instanceof PropertyList)) continue;
            props.setProperty(name + "_copy", (PropertyList)props.getPropertyList(name).clone());
            this.evaluateExpression(props.getPropertyList(name + "_copy"), i18NFormat);
        }
    }

    private String openMeter(int meterHeight) {
        return "<div class=\"gizmo_innercontainer\" style=\"width:100%;height:" + (meterHeight > 0 ? "" + meterHeight : "100%") + "px;overflow-x:hidden;overflow-y:hidden;\">";
    }

    private String closeMeter() {
        return "</div>";
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            block42: {
                int meterWidth = this.getWidth() - 15;
                int meterHeight = this.getHeight() - 30;
                String title = this.getTranslationProcessor().translate(this.element.getProperty("displaytitle_copy", this.element.getProperty(DISPLAYTITLE_PROPERTY, "")).trim());
                String subtitle = this.getTranslationProcessor().translate(this.element.getProperty("subtitle_copy", this.element.getProperty("subtitle", "")).trim());
                if (title.length() > 0) {
                    meterHeight -= 23;
                }
                if (subtitle.length() > 0) {
                    meterHeight -= 16;
                }
                String meterstyle = this.element.getProperty(METERSTYLE_PROPERTY, "").trim();
                if (this.element.getProperty("__meterstyle", "").length() > 0 && !this.element.getProperty("__meterstyle", "").equals(meterstyle)) {
                    meterstyle = this.element.getProperty("__meterstyle", "");
                }
                if (meterstyle.equalsIgnoreCase("thermometer")) {
                    meterHeight -= 10;
                }
                if (this.browser != null && this.browser.isWebkit()) {
                    html.append(this.openMeter(meterstyle.equalsIgnoreCase("thermometer") ? meterHeight + 45 : meterHeight - 10));
                } else {
                    html.append(this.openMeter(0));
                }
                try {
                    String pagename = this.element.getProperty("webpageid", "");
                    if (this.elementType == null || this.elementType.length() == 0) {
                        this.elementType = this.element.getProperty("propertytreeid", "");
                    }
                    String meterId = this.getRandomString(5);
                    String urlpath = "";
                    if (this.request != null) {
                        urlpath = this.request.getRequestURL().toString();
                        urlpath = urlpath.substring(0, urlpath.indexOf(this.request.getServletPath()));
                    }
                    this.element.setProperty("urlpath", urlpath);
                    this.element.setProperty("currentuser", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
                    if (meterstyle.equalsIgnoreCase("circular gauge") || meterstyle.equalsIgnoreCase("thermometer")) {
                        StringBuffer sbImgURL = new StringBuffer();
                        sbImgURL.append("rc?command=operation&operationclass=com.labvantage.sapphire.modules.dashboard.util.DashboardMeterStreamer&pageid=").append(pagename).append("&elementid=").append(this.elementid).append("&meterid=").append(meterId).append("&elementtype=").append(this.elementType).append("&width=").append(meterWidth).append("&height=").append(meterHeight);
                        if (title.length() > 0) {
                            html.append("<div style=\"font-size:14pt;text-weight:bold;\" align=center>").append(title).append("</div>");
                        }
                        if (subtitle.length() > 0) {
                            html.append("<div style=\"font-size:10pt;text-weight:bold;\" align=center>").append(subtitle).append("</div>");
                        }
                        html.append("<img width=\"").append(meterWidth).append("\" height=\"").append(meterHeight).append("\" src=\"").append(sbImgURL.toString()).append("\" border=0>");
                        this.dm.createMeter(this.element);
                        HttpSession session = null;
                        if (this.pageContext != null && this.request == null) {
                            session = this.pageContext.getSession();
                        } else if (this.request != null) {
                            session = this.request.getSession();
                        }
                        if (session != null) {
                            session.setAttribute(meterId, (Object)this.dm.getChart());
                        }
                        break block42;
                    }
                    this.monitordata = this.dm.getMonitorData(this.element);
                    if (this.monitordata != null && this.monitordata.getRowCount() > 0) {
                        double value = 0.0;
                        try {
                            value = Double.parseDouble(this.monitordata.getValue(0, "measurevalue", "0"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        DecimalFormat df = new DecimalFormat("###.#");
                        if (meterstyle.equalsIgnoreCase("numeric")) {
                            int font = 100;
                            try {
                                font = Integer.parseInt(this.element.getProperty("fontsize", "" + font));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            String color = "";
                            String c = this.element.getProperty("meterrangecritical");
                            String w = this.element.getProperty("meterrangewarning");
                            DashboardMeter.RANGE range = this.dm.getRange(c, w, value);
                            switch (range) {
                                case CRITICAL: {
                                    color = "Red";
                                    break;
                                }
                                case WARNING: {
                                    color = "Orange";
                                    break;
                                }
                                default: {
                                    color = "Black";
                                }
                            }
                            if (title.length() > 0) {
                                html.append("<div style=\"font-size:14pt;text-weight:bold;\" align=center>").append(title).append("</div>");
                            }
                            if (subtitle.length() > 0) {
                                html.append("<div style=\"font-size:10pt;text-weight:bold;\" align=center>").append(subtitle).append("</div>");
                            }
                            html.append("<div id=\"_numericPanel\" style=\"color:").append(color).append(";font-size:").append(font).append("pt;\" align=\"center\">");
                            html.append(df.format(value));
                            html.append("</div>");
                            break block42;
                        }
                        if (meterstyle.equalsIgnoreCase("panel gauge")) {
                            String backgroundimageurl = this.element.getProperty(BACKGROUNDIMAGEURL_PROPERTY).trim();
                            if (backgroundimageurl.length() > 0) {
                                Image image = new Image(this.pageContext);
                                image.setImageSrc(backgroundimageurl);
                                backgroundimageurl = image.getImageSrc();
                            }
                            String bgcolor = "#729fcf";
                            String textcolor = "";
                            String c = this.element.getProperty("meterrangecritical");
                            String w = this.element.getProperty("meterrangewarning");
                            DashboardMeter.RANGE range = this.dm.getRange(c, w, value);
                            switch (range) {
                                case CRITICAL: {
                                    bgcolor = "Red";
                                    textcolor = "Red";
                                    break;
                                }
                                case WARNING: {
                                    bgcolor = "Orange";
                                    textcolor = "Orange";
                                    break;
                                }
                                default: {
                                    bgcolor = "#729fcf";
                                    textcolor = "Black";
                                }
                            }
                            int font = 14;
                            try {
                                font = Integer.parseInt(this.element.getProperty("fontsize", "" + font));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            html.append("<style>");
                            html.append("#slider .ui-slider-range { background: ").append(bgcolor).append("; }");
                            html.append("#slider .ui-state-default, .ui-widget-content .ui-state-default, .ui-widget-header .ui-state-default {background: transparent").append(backgroundimageurl.length() > 0 ? " url(" + backgroundimageurl + ") no-repeat scroll 50% 50%" : "").append(";border:none; }");
                            html.append("</style>");
                            if (title.length() > 0) {
                                html.append("<div style=\"font-size:14pt;text-weight:bold;\" align=center>").append(title).append("</div>");
                            }
                            if (subtitle.length() > 0) {
                                html.append("<div style=\"font-size:10pt;text-weight:bold;\" align=center>").append(subtitle).append("</div>");
                            }
                            html.append("<div id=\"_parentPanel\" style=\"color:").append(textcolor).append(";font-size:").append(font).append("pt;padding: 10px;\" align=\"left\">");
                            html.append(this.getTranslationProcessor().translate(this.element.getProperty("meterunits_copy", this.element.getProperty(METERUNITS_PROPERTY, "Value")))).append(": ").append(df.format(value));
                            html.append("<div id=\"_sliderPanel\" style=\"padding: 10px;\" align=\"center\">");
                            html.append("<div id=\"slider\">").append(df.format(value)).append("</div>");
                            html.append("</div>");
                            html.append("</div>");
                            break block42;
                        }
                        if (meterstyle.equalsIgnoreCase("html")) {
                            String custom = StringUtil.replaceAll(this.element.getProperty("customhtml"), "[value]", df.format(value), true);
                            custom = StringUtil.replaceAll(custom, "[measurevalue]", df.format(value), true);
                            String imageUrl = this.element.getProperty(BACKGROUNDIMAGEURL_PROPERTY, "");
                            custom = StringUtil.replaceAll(custom, "[title]", title, true);
                            custom = StringUtil.replaceAll(custom, "[imageurl]", imageUrl, true);
                            custom = this.evaluateExpression(custom, BaseGizmo.I18NFormat.CLIENT);
                            html.append("<div id=\"_parentPanel\" style=\"padding: 10px;\" align=\"left\">");
                            html.append(custom);
                            html.append("</div>");
                        }
                        break block42;
                    }
                    html.append("No data found.");
                }
                catch (Exception e) {
                    html.append("Could not load meter.");
                    this.logger.error("Could not load meter: Reason: " + e.getMessage(), e);
                }
            }
            html.append(this.closeMeter());
        }
        return html.toString();
    }

    private String getRandomString(int length) {
        return StringUtil.getRandomString(length);
    }

    @Override
    public String getScript() {
        StringBuffer html = new StringBuffer();
        String meterstyle = this.element.getProperty(METERSTYLE_PROPERTY, "").trim();
        if (meterstyle.equalsIgnoreCase("panel gauge")) {
            double min = 0.0;
            try {
                min = Double.parseDouble(this.element.getProperty(METERRANGELOW_PROPERTY, "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            double max = 0.0;
            try {
                max = Double.parseDouble(this.element.getProperty(METERRANGEHIGH_PROPERTY, "100"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            html.append("$('#slider').each(function(){");
            html.append("var value = parseInt( $(this).text(), 0 );");
            html.append("$(this).empty().slider({");
            html.append("value: value,");
            html.append("range: 'min',");
            html.append("min: ").append(min).append(",");
            html.append("max: ").append(max).append("");
            html.append("}).on('slide', function (event, ui){return false;});");
            html.append("});");
        }
        return html.toString();
    }

    @Override
    public int getCount() {
        return (int)Math.round(this.getValue());
    }

    @Override
    public String getCountColor() {
        String color = "";
        String c = this.element.getProperty("meterrangecritical");
        String w = this.element.getProperty("meterrangewarning");
        DashboardMeter.RANGE range = this.dm.getRange(c, w, this.getValue());
        switch (range) {
            case CRITICAL: {
                color = "Red";
                break;
            }
            case WARNING: {
                color = "Orange";
                break;
            }
            default: {
                color = "";
            }
        }
        return color;
    }

    @Override
    public String getTitle() {
        DecimalFormat df = new DecimalFormat("###.#");
        return super.getTitle();
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getTranslationProcessor().translate(this.element.getProperty("meterunits_copy", this.element.getProperty(METERUNITS_PROPERTY, "Meter Gizmo"))), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return this.defaultImageSrc;
    }

    public double getValue() {
        if (this.monitordata == null && this.dm != null) {
            this.monitordata = this.dm.getMonitorData(this.element);
        }
        double value = 0.0;
        if (this.monitordata != null && this.monitordata.getRowCount() > 0) {
            try {
                value = Double.parseDouble(this.monitordata.getValue(0, "measurevalue", "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return value;
    }
}

