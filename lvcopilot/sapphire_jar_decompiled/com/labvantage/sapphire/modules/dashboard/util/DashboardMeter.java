/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.math.NumberUtils
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.encoders.ImageEncoder
 *  org.jfree.chart.encoders.ImageEncoderFactory
 *  org.jfree.chart.plot.DialShape
 *  org.jfree.chart.plot.MeterPlot
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.ThermometerPlot
 *  org.jfree.data.Range
 *  org.jfree.data.general.DefaultValueDataset
 *  org.jfree.data.general.ValueDataset
 *  org.jfree.ui.RectangleInsets
 */
package com.labvantage.sapphire.modules.dashboard.util;

import com.labvantage.sapphire.Trace;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.apache.commons.lang.math.NumberUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DashboardMeter {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 91193 $";
    private SDCProcessor sdcprocessor;
    private QueryProcessor qp;
    private TranslationProcessor translationProcessor;
    private JFreeChart chart;
    private ChartRenderingInfo info = new ChartRenderingInfo();
    private String userlanguage = "";
    public static final String MEASUREMENT_SDC = "LV_Measurement";

    public RANGE getRange(String criticalRange, String warningRange, double value) {
        return this.getRange(criticalRange, warningRange, value, 0.0, 0.0, null);
    }

    private RANGE getRange(String criticalRange, String warningRange, double value, double min, double max, ThermometerPlot plot) {
        RANGE out = RANGE.NORMAL;
        boolean ranged = false;
        boolean c_lessthan = true;
        if (criticalRange.length() > 0 && criticalRange.startsWith(">")) {
            c_lessthan = false;
            criticalRange = criticalRange.substring(1);
        } else if (criticalRange.length() > 0 && criticalRange.startsWith("<")) {
            c_lessthan = true;
            criticalRange = criticalRange.substring(1);
        }
        double critical = 0.0;
        if (criticalRange.length() > 0) {
            try {
                critical = Double.parseDouble(criticalRange);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean w_lessthan = true;
        if (warningRange.length() > 0 && warningRange.startsWith(">")) {
            w_lessthan = false;
            warningRange = warningRange.substring(1);
        } else if (warningRange.length() > 0 && warningRange.startsWith("<")) {
            w_lessthan = true;
            warningRange = warningRange.substring(1);
        }
        double warning = 0.0;
        if (warningRange.length() > 0) {
            try {
                warning = Double.parseDouble(warningRange);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (criticalRange.length() > 0 && warningRange.length() > 0) {
            if (c_lessthan && value <= critical) {
                out = RANGE.CRITICAL;
                ranged = true;
            } else if (!c_lessthan && value > critical) {
                out = RANGE.CRITICAL;
                ranged = true;
            }
            if (!ranged) {
                if (w_lessthan && value <= warning) {
                    out = RANGE.WARNING;
                    ranged = true;
                } else if (!w_lessthan && value > warning) {
                    out = RANGE.WARNING;
                    ranged = true;
                }
            }
            if (!ranged) {
                out = RANGE.NORMAL;
                ranged = true;
            }
            if (plot != null) {
                if (c_lessthan) {
                    plot.setSubrange(2, min, critical);
                    plot.setSubrange(1, critical, warning);
                    plot.setSubrange(0, warning, max);
                } else {
                    plot.setSubrange(2, critical, warning);
                    plot.setSubrange(1, warning, max);
                    plot.setSubrange(0, min, critical);
                }
            }
        } else if (criticalRange.length() > 0) {
            if (c_lessthan && value <= critical) {
                out = RANGE.CRITICAL;
                ranged = true;
            } else if (!c_lessthan && value > critical) {
                out = RANGE.CRITICAL;
                ranged = true;
            }
            if (!ranged) {
                out = RANGE.NORMAL;
                ranged = true;
            }
            if (plot != null) {
                if (c_lessthan) {
                    plot.setSubrange(2, min, critical);
                    plot.setSubrange(0, critical, max);
                } else {
                    plot.setSubrange(2, critical, max);
                    plot.setSubrange(0, min, critical);
                }
            }
        } else if (warningRange.length() > 0) {
            if (w_lessthan && value <= warning) {
                out = RANGE.WARNING;
                ranged = true;
            } else if (!w_lessthan && value > warning) {
                out = RANGE.WARNING;
                ranged = true;
            }
            if (!ranged) {
                out = RANGE.NORMAL;
                ranged = true;
            }
            if (plot != null) {
                if (w_lessthan) {
                    plot.setSubrange(1, min, warning);
                    plot.setSubrange(0, warning, max);
                } else {
                    plot.setSubrange(1, warning, max);
                    plot.setSubrange(0, min, warning);
                }
            }
        } else {
            out = RANGE.NORMAL;
            ranged = true;
        }
        return out;
    }

    public DataSet getMonitorData(PropertyList props) {
        try {
            String meterstyle = props.getProperty("meterstyle", "");
            DataSet dsMonitorData = null;
            String querytype = props.getProperty("querytype", "SQL");
            if (querytype.equalsIgnoreCase("SDI List")) {
                PropertyList data = this.getPropertyList(props, "data");
                String sdcid = "";
                String columnid = "";
                if (data != null) {
                    sdcid = data.getProperty("sdcid");
                    columnid = data.getProperty("columnid");
                }
                if (sdcid.length() > 0 && columnid.length() > 0) {
                    SDIData sdiData;
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    if (data.getProperty("queryid").length() > 0) {
                        sdiRequest.setQueryid(data.getProperty("queryid"));
                        PropertyListCollection params = data.getCollection("params");
                        if (params != null) {
                            String[] paramarr = new String[params.size()];
                            for (int i = 0; i < params.size(); ++i) {
                                paramarr[i] = params.getPropertyList(i).getProperty("value");
                            }
                            sdiRequest.setQueryParams(paramarr);
                        }
                    } else {
                        sdiRequest.setQueryFrom(data.getProperty("queryfrom", this.getSDCProcessor().getProperty(sdcid, "tableid", "")));
                        if (data.getProperty("querywhere", "").length() > 0) {
                            sdiRequest.setQueryWhere(data.getProperty("querywhere", ""));
                        }
                    }
                    boolean distinct = data.getProperty("distinct", "N").equalsIgnoreCase("Y");
                    boolean count = data.getProperty("count", "N").equalsIgnoreCase("Y");
                    sdiRequest.setRequestItem("primary[" + columnid + "]");
                    if (!count) {
                        sdiRequest.setRetrieveLimit(1);
                    }
                    if ((sdiData = new SDIProcessor(this.getSDCProcessor().getConnectionid()).getSDIData(sdiRequest)) != null) {
                        dsMonitorData = new DataSet();
                        dsMonitorData.addColumn("measurecategory", 0);
                        dsMonitorData.addColumn("measurevalue", 1);
                        DataSet temp = sdiData.getDataset("primary");
                        if (temp != null) {
                            int l = columnid.lastIndexOf(")");
                            if (l > -1) {
                                columnid = columnid.substring(l + 1).trim();
                            }
                            if (count) {
                                int counter = 0;
                                if (distinct) {
                                    ArrayList<String> dis = new ArrayList<String>();
                                    for (int r = 0; r < temp.getRowCount(); ++r) {
                                        String v = temp.getValue(r, columnid, "");
                                        if (dis.contains(v)) continue;
                                        ++counter;
                                        dis.add(v);
                                    }
                                } else {
                                    counter = temp.getRowCount();
                                }
                                dsMonitorData.addRow();
                                dsMonitorData.setValue(0, "measurecategory", temp.getValue(0, this.getSDCProcessor().getProperty(sdcid, "keycolid1"), ""));
                                dsMonitorData.setNumber(0, "measurevalue", counter);
                            } else if (temp.getRowCount() > 0) {
                                BigDecimal bd;
                                dsMonitorData.addRow();
                                dsMonitorData.setValue(0, "measurecategory", temp.getValue(0, this.getSDCProcessor().getProperty(sdcid, "keycolid1"), ""));
                                if (temp.getColumnType(columnid) == 1) {
                                    bd = temp.getBigDecimal(0, columnid);
                                } else {
                                    try {
                                        bd = new BigDecimal(temp.getValue(0, columnid));
                                    }
                                    catch (Exception e) {
                                        bd = new BigDecimal(0);
                                    }
                                }
                                if (bd == null) {
                                    Trace.logDebug("Failed to obtain measure value.");
                                    bd = new BigDecimal(0);
                                }
                                dsMonitorData.setNumber(0, "measurevalue", bd);
                            }
                        }
                    } else {
                        Trace.logWarn("Could not obtain SDI Data for Meter. Check properties.");
                    }
                } else {
                    Trace.logWarn("No SDC Id or column provided for Chart. Check properties.");
                }
            } else if (querytype.equalsIgnoreCase("SDI Aggregate")) {
                PropertyList data = this.getPropertyList(props, "dataaggregate");
                String sdcid = data.getProperty("sdcid");
                if (sdcid.length() > 0) {
                    String categoryid = "";
                    if (data.containsKey("category")) {
                        categoryid = data.getPropertyList("category").getProperty("categorytype", "columnid").equalsIgnoreCase("custom") ? data.getPropertyList("category").getProperty("custom") : data.getPropertyList("category").getProperty("columnid");
                    }
                    String valueid = "";
                    String vtype = "";
                    boolean groupby = true;
                    if (data.containsKey("value")) {
                        vtype = data.getPropertyList("value").getProperty("valuetype", "columnid");
                        if (vtype.equalsIgnoreCase("custom")) {
                            valueid = data.getPropertyList("value").getProperty("custom");
                        } else {
                            valueid = data.getPropertyList("value").getProperty("columnid");
                            if (!vtype.equalsIgnoreCase("columnid")) {
                                if (valueid.length() == 0) {
                                    valueid = "*";
                                }
                                valueid = vtype.equalsIgnoreCase("sum") ? "SUM(" + valueid + ")" : (vtype.equalsIgnoreCase("average") ? "AVG(" + valueid + ")" : "COUNT(" + valueid + ")");
                            } else {
                                groupby = false;
                            }
                        }
                    }
                    if (valueid.length() > 0) {
                        String queryfrom = data.getProperty("queryfrom");
                        if (queryfrom.length() > 0 && !queryfrom.toLowerCase().contains(this.getSDCProcessor().getProperty(sdcid, "tableid"))) {
                            queryfrom = this.getSDCProcessor().getProperty(sdcid, "tableid") + ", " + queryfrom;
                        } else if (queryfrom.length() == 0) {
                            queryfrom = this.getSDCProcessor().getProperty(sdcid, "tableid");
                        }
                        StringBuffer sql = new StringBuffer();
                        sql.append("SELECT ");
                        if (categoryid.length() > 0) {
                            sql.append(categoryid).append(" AS measurecategory");
                            sql.append(", ");
                        } else {
                            groupby = false;
                        }
                        sql.append(valueid).append(" AS measurevalue ");
                        sql.append("FROM ").append(queryfrom).append(" ");
                        String securitywhere = "";
                        try {
                            PropertyList inprops = new PropertyList();
                            inprops.setProperty("sdcid", sdcid);
                            new ActionProcessor(this.sdcprocessor.getConnectionid()).processActionClass("com.labvantage.sapphire.modules.dashboard.util.DashboardSecurityWhereClause", inprops);
                            securitywhere = inprops.getProperty("whereclause");
                        }
                        catch (Exception inprops) {
                            // empty catch block
                        }
                        if (data.getProperty("querywhere").trim().length() > 0) {
                            String where = data.getProperty("querywhere").trim();
                            sql.append("WHERE ").append(where);
                            if (securitywhere.length() > 0) {
                                sql.append(" AND ").append(securitywhere);
                            }
                        } else if (securitywhere.length() > 0) {
                            sql.append("WHERE ").append(securitywhere);
                        }
                        if (groupby) {
                            sql.append(" ").append("GROUP BY ").append(categoryid);
                        }
                        Trace.logDebug("Aggregate SQL: " + sql.toString());
                        dsMonitorData = this.getQp().getSqlDataSet(sql.toString());
                        if (dsMonitorData == null) {
                            Trace.logWarn("Could not obtain Aggregate Data for Chart. Check properties.");
                        }
                    } else {
                        Trace.logWarn("No value Id provided. Check properties.");
                    }
                } else {
                    Trace.logWarn("No SDC Id provided for Chart. Check properties.");
                }
            } else {
                String metercategory = props.getProperty("metercategory", "");
                String tableid = (String)this.getSDCProcessor().getSDCProperties(MEASUREMENT_SDC).get("tableid");
                Trace.logDebug("DMS", "streamer: creating " + meterstyle + " from dataset");
                StringBuffer sql = new StringBuffer();
                if (querytype.equalsIgnoreCase("Monitor")) {
                    String monitorid = props.getProperty("monitorid", "");
                    sql.append("select measurevalue, measurecategory from ").append(tableid).append(" where latestflag = 'Y' and monitorid='").append(monitorid).append("' and measurecategory = '").append(metercategory).append("'");
                } else {
                    String monitorsql = this.getProperty(props, "monitorsql", "");
                    sql.append(monitorsql);
                }
                if (sql.length() < 1) {
                    sql.append("select 0 as \"measurevalue\" from dual");
                }
                String finalsql = sql.toString();
                String[] tks = StringUtil.getExpressionTokens(finalsql.toString());
                for (int i = 0; i < tks.length; ++i) {
                    String key = tks[i];
                    if (key.equalsIgnoreCase("%currentuser%")) {
                        key = "currentuser";
                    }
                    if (!props.containsKey(key) || !(props.get(key) instanceof String) || props.getProperty(key, "").length() <= 0) continue;
                    finalsql = StringUtil.replaceAll(finalsql, "[" + tks[i] + "]", props.getProperty(key, ""));
                }
                dsMonitorData = this.qp.getSqlDataSet(finalsql);
            }
            return dsMonitorData;
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public void createMeter(PropertyList props) throws SapphireException {
        block38: {
            try {
                BigDecimal v;
                String requesturl = props.getProperty("urlpath", "");
                String color = props.getProperty("color", "Light");
                String meterstyle = props.getProperty("meterstyle", "");
                String backgroundimageurl = props.getProperty("backgroundimageurl", "");
                DataSet dsMonitorData = this.getMonitorData(props);
                if (dsMonitorData == null) break block38;
                DefaultValueDataset dataset = new DefaultValueDataset();
                double value = dsMonitorData.getRowCount() > 0 ? ((v = dsMonitorData.getBigDecimal(0, "measurevalue")) != null ? v.doubleValue() : 0.0) : 0.0;
                dataset = new DefaultValueDataset(value);
                String meterstylenontrans = props.getProperty("__meterstyle", "");
                if (meterstylenontrans.trim().equalsIgnoreCase("thermometer")) {
                    ThermometerPlot plot = new ThermometerPlot((ValueDataset)dataset);
                    double min = 0.0;
                    try {
                        min = Double.parseDouble(props.getProperty("meterrangelow", "0"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    double max = 0.0;
                    try {
                        max = Double.parseDouble(props.getProperty("meterrangehigh", "20"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (min > max) {
                        max = 20.0;
                        min = 0.0;
                    }
                    plot.setLowerBound(min);
                    plot.setUpperBound(max);
                    double critical = min;
                    double warning = max / 2.0;
                    double normal = max;
                    String meterrangecritical = props.getProperty("meterrangecritical").trim();
                    String meterrangewarning = props.getProperty("meterrangewarning").trim();
                    this.getRange(meterrangecritical, meterrangewarning, value, min, max, plot);
                    if (meterrangecritical.length() > 0 || meterrangewarning.length() > 0) {
                        if (color.equalsIgnoreCase("dark")) {
                            plot.setSubrangePaint(2, (Paint)Color.red.darker());
                            plot.setSubrangePaint(1, (Paint)Color.orange.darker());
                            plot.setSubrangePaint(0, (Paint)Color.green.darker());
                            plot.setMercuryPaint((Paint)Color.green.darker());
                        } else {
                            plot.setSubrangePaint(2, (Paint)Color.red.brighter());
                            plot.setSubrangePaint(1, (Paint)Color.orange.brighter());
                            plot.setSubrangePaint(0, (Paint)Color.green.brighter());
                            plot.setMercuryPaint((Paint)Color.green.brighter());
                        }
                        plot.setUseSubrangePaint(true);
                    } else {
                        if (color.equalsIgnoreCase("dark")) {
                            plot.setMercuryPaint((Paint)new GradientPaint(0.0f, 0.0f, Color.decode("#660000"), 0.0f, 1000.0f, Color.red));
                            plot.setValuePaint((Paint)Color.white);
                            plot.setOutlinePaint((Paint)Color.white);
                        } else {
                            plot.setMercuryPaint((Paint)new GradientPaint(0.0f, 0.0f, Color.white, 0.0f, 1000.0f, Color.red));
                            plot.setValuePaint((Paint)Color.black);
                            plot.setOutlinePaint((Paint)Color.white);
                        }
                        plot.setUseSubrangePaint(false);
                    }
                    plot.setUnits(this.getTranslationProcessor().translate(this.getProperty(props, "meterunits", "")));
                    if (props.getProperty("showranges", "N").equalsIgnoreCase("Y")) {
                        plot.setSubrangePaint(0, (Paint)Color.green.darker());
                        if (meterrangewarning.length() > 0) {
                            plot.setSubrangePaint(1, (Paint)Color.orange);
                        } else {
                            plot.setSubrangePaint(1, (Paint)Color.white);
                        }
                        if (meterrangecritical.length() > 0) {
                            plot.setSubrangePaint(2, (Paint)Color.red.darker());
                        } else {
                            plot.setSubrangePaint(2, (Paint)Color.white);
                        }
                    } else {
                        plot.setSubrangePaint(0, (Paint)Color.white);
                        plot.setSubrangePaint(1, (Paint)Color.white);
                        plot.setSubrangePaint(2, (Paint)Color.white);
                    }
                    if (this.translationProcessor != null) {
                        plot.setNoDataMessage(this.translationProcessor.translate("No data"));
                    } else {
                        plot.setNoDataMessage("No data");
                    }
                    JFreeChart achart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, (Plot)plot, false);
                    achart.setBackgroundPaint((Paint)Color.white);
                    this.setChart(achart);
                    break block38;
                }
                if (!meterstylenontrans.equalsIgnoreCase("circular gauge")) break block38;
                MeterPlot plot = new MeterPlot((ValueDataset)dataset);
                double min = 0.0;
                try {
                    min = Double.parseDouble(NumberUtils.isNumber((String)props.getProperty("meterrangelow", "0")) ? props.getProperty("meterrangelow", "0") : props.getProperty("__meterrangelow", "0"));
                }
                catch (Exception max) {
                    // empty catch block
                }
                double max = 0.0;
                try {
                    max = Double.parseDouble(NumberUtils.isNumber((String)props.getProperty("meterrangehigh", "20")) ? props.getProperty("meterrangehigh", "20") : props.getProperty("__meterrangehigh", "20"));
                }
                catch (Exception critical) {
                    // empty catch block
                }
                if (min > max) {
                    max = 20.0;
                    min = 0.0;
                }
                Range meterRange = new Range(min, max);
                plot.setRange(meterRange);
                plot.setDialOutlinePaint((Paint)new GradientPaint(0.0f, 0.0f, Color.black, 0.0f, 1000.0f, Color.blue));
                plot.setTickLabelsVisible(true);
                plot.setTickLabelsVisible(true);
                plot.setTickSize(meterRange.getLength() / 10.0);
                plot.setDialShape(DialShape.CHORD);
                plot.setUnits(this.getTranslationProcessor().translate(this.getProperty(props, "meterunits", "")));
                plot.setBackgroundPaint((Paint)Color.white);
                plot.setTickPaint((Paint)Color.gray);
                if (color.equalsIgnoreCase("dark")) {
                    plot.setDialBackgroundPaint((Paint)new GradientPaint(0.0f, 0.0f, Color.decode("#000066"), 0.0f, 1000.0f, Color.blue));
                    plot.setValuePaint((Paint)Color.white);
                    plot.setNeedlePaint((Paint)Color.white);
                    plot.setTickLabelPaint((Paint)Color.white);
                } else {
                    plot.setDialBackgroundPaint((Paint)new GradientPaint(0.0f, 0.0f, Color.white, 0.0f, 1000.0f, Color.blue));
                    plot.setValuePaint((Paint)Color.black);
                    plot.setNeedlePaint((Paint)Color.black);
                    plot.setTickLabelPaint((Paint)Color.black);
                    plot.setTickPaint((Paint)Color.gray);
                }
                JFreeChart achart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, (Plot)plot, false);
                achart.setBackgroundPaint((Paint)Color.white);
                if (requesturl.length() > 0 && backgroundimageurl.length() > 0) {
                    String surl = backgroundimageurl.startsWith("/") ? requesturl + backgroundimageurl : requesturl + "/" + backgroundimageurl;
                    try {
                        RectangleInsets ri;
                        URL url = new URL(surl);
                        ImageIcon ic = new ImageIcon(url);
                        double h = Double.parseDouble(props.getPropertyList("gizmoprops").getProperty("height", "200")) - 30.0;
                        double w = Double.parseDouble(props.getPropertyList("gizmoprops").getProperty("width", "200")) - 15.0;
                        String title = this.getProperty(props, "displaytitle", "").trim();
                        String subtitle = this.getProperty(props, "subtitle", "").trim();
                        if (title.length() > 0) {
                            h -= 23.0;
                        }
                        if (subtitle.length() > 0) {
                            w -= 16.0;
                        }
                        if (h < w) {
                            ri = new RectangleInsets(h * 0.1, h * 0.1, h * 0.1, h * 0.1);
                            w = h;
                        } else {
                            h = w;
                            ri = new RectangleInsets(w * 0.1, w * 0.1, w * 0.1, w * 0.1);
                        }
                        achart.getPlot().setInsets(ri);
                        int type = 1;
                        BufferedImage image = new BufferedImage((int)w, (int)h, type);
                        Graphics2D g2 = image.createGraphics();
                        Image im = ic.getImage();
                        AffineTransform t = AffineTransform.getScaleInstance(w / (double)im.getWidth(null), h / (double)im.getHeight(null));
                        g2.drawImage(ic.getImage(), t, null);
                        achart.setBackgroundImage((Image)image);
                        achart.setBackgroundImageAlignment(0);
                        ((MeterPlot)achart.getPlot()).setDialBackgroundPaint(null);
                        achart.getPlot().setBackgroundPaint(null);
                        if (color.equalsIgnoreCase("dark")) {
                            ((MeterPlot)achart.getPlot()).setTickLabelPaint((Paint)Color.white);
                            ((MeterPlot)achart.getPlot()).setNeedlePaint((Paint)new GradientPaint(0.0f, 0.0f, Color.decode("#CEE7F2"), 0.0f, 1000.0f, Color.blue));
                            ((MeterPlot)achart.getPlot()).setValuePaint((Paint)Color.white);
                        } else {
                            ((MeterPlot)achart.getPlot()).setTickLabelPaint((Paint)Color.black);
                            ((MeterPlot)achart.getPlot()).setNeedlePaint((Paint)new GradientPaint(0.0f, 0.0f, Color.decode("#4A0000"), 0.0f, 1000.0f, Color.red));
                            ((MeterPlot)achart.getPlot()).setValuePaint((Paint)Color.black);
                        }
                        achart.setBackgroundImageAlpha(1.0f);
                        achart.getPlot().setForegroundAlpha(1.0f);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                this.setChart(achart);
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int streamMeter(OutputStream out, JFreeChart chart, int height, int width, boolean png) throws SapphireException {
        int output = 0;
        try {
            try {
                ImageEncoder imageEncoder;
                BufferedImage image;
                Trace.log("DCS", "DashboardChart: streaming chart as PNG ");
                if (png) {
                    image = chart.createBufferedImage(width, height, this.info);
                    Logger.logDebug("png buffered image created  ");
                    imageEncoder = ImageEncoderFactory.newInstance((String)"png");
                    Logger.logDebug("png image encoder created  ");
                } else {
                    image = chart.createBufferedImage(width, height, 1, this.info);
                    Logger.logDebug("jpg buffered image created  ");
                    imageEncoder = ImageEncoderFactory.newInstance((String)"jpeg");
                    Logger.logDebug("png image encoder created  ");
                }
                byte[] data = imageEncoder.encode(image);
                Logger.logDebug("image encoded to byte[]  ");
                output = data.length;
                Logger.logDebug("image size = " + output);
                if (output > 0) {
                    out.write(data);
                    Logger.logDebug("data written to output stream");
                } else {
                    Logger.logWarn("no data stream");
                }
            }
            finally {
                out.flush();
            }
        }
        catch (IOException ioe) {
            Trace.logError("SSL", (Object)("SapphireMeter: streamPNG " + ioe.getMessage()), ioe);
            throw new SapphireException(ioe.getMessage(), ioe);
        }
        return output;
    }

    public JFreeChart getChart() {
        return this.chart;
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    public SDCProcessor getSDCProcessor() {
        return this.sdcprocessor;
    }

    public void setSDCProcessor(SDCProcessor processor) {
        this.sdcprocessor = processor;
    }

    public TranslationProcessor getTranslationProcessor() {
        return this.translationProcessor;
    }

    public void setTranslationProcessor(TranslationProcessor translationProcessor) {
        this.translationProcessor = translationProcessor;
    }

    private PropertyList getPropertyList(PropertyList props, String name) {
        return props.containsKey(name + "_copy") ? props.getPropertyList(name + "_copy") : props.getPropertyList(name);
    }

    private String getProperty(PropertyList props, String name, String defaultValue) {
        return props.containsKey(name + "_copy") ? props.getProperty(name + "_copy", defaultValue) : props.getProperty(name, defaultValue);
    }

    public QueryProcessor getQp() {
        return this.qp;
    }

    public void setQp(QueryProcessor qp) {
        this.qp = qp;
    }

    public String getUserlanguage() {
        return this.userlanguage;
    }

    public void setUserlanguage(String userlanguage) {
        this.userlanguage = userlanguage;
    }

    public static enum RANGE {
        CRITICAL,
        WARNING,
        NORMAL;

    }
}

