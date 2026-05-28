/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.util;

import com.labvantage.sapphire.Trace;
import java.math.BigDecimal;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DashboardChart {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 64582 $";
    private SDCProcessor sdcprocessor;
    private QueryProcessor qp;
    private TranslationProcessor translationProcessor;
    public static final String MEASUREMENT_SDC = "LV_Measurement";

    private PropertyList getPropertyList(PropertyList props, String name) {
        return props.containsKey(name + "_copy") ? props.getPropertyList(name + "_copy") : props.getPropertyList(name);
    }

    private String getProperty(PropertyList props, String name, String defaultValue) {
        return props.containsKey(name + "_copy") ? props.getProperty(name + "_copy", defaultValue) : props.getProperty(name, defaultValue);
    }

    public DataSet getDataSet(PropertyList props) {
        String charttype = props.getProperty("charttype", "");
        DataSet dsMonitorData = null;
        String querytype = props.getProperty("querytype", "SQL");
        if (querytype.equalsIgnoreCase("SDI List")) {
            String sdcid = "";
            String catcolumnid = "";
            String catcolumnid_2 = "";
            String catcolumnid_3 = "";
            String valuecolumnid = "";
            PropertyList data = this.getPropertyList(props, "data");
            if (data != null) {
                sdcid = data.getProperty("sdcid");
                catcolumnid = data.getProperty("categorycolumnid");
                if (catcolumnid.contains(";")) {
                    String[] split = StringUtil.split(catcolumnid, ";", true);
                    catcolumnid = split[0];
                    if (split.length > 1) {
                        catcolumnid_2 = split[1];
                        if (split.length > 2) {
                            catcolumnid_3 = split[2];
                        }
                    }
                }
                valuecolumnid = data.getProperty("valuecolumnid");
            }
            if (catcolumnid.length() == 0) {
                catcolumnid = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                catcolumnid_2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
                catcolumnid_3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
            }
            if (sdcid.length() > 0) {
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
                String ritem = "primary[" + catcolumnid + " ," + (catcolumnid_2.length() > 0 ? catcolumnid_2 + " ," : "") + (catcolumnid_3.length() > 0 ? catcolumnid_3 + " ," : "") + valuecolumnid + "]";
                sdiRequest.setRequestItem(ritem);
                SDIData sdiData = new SDIProcessor(this.getSDCProcessor().getConnectionid()).getSDIData(sdiRequest);
                if (sdiData != null) {
                    dsMonitorData = new DataSet();
                    dsMonitorData.addColumn("measurecategory", 0);
                    if (catcolumnid_2.length() > 0) {
                        dsMonitorData.addColumn("measurecategory2", 0);
                    }
                    if (catcolumnid_3.length() > 0) {
                        dsMonitorData.addColumn("measurecategory3", 0);
                    }
                    dsMonitorData.addColumn("measurevalue", 1);
                    DataSet temp = sdiData.getDataset("primary");
                    if (temp != null) {
                        int l = valuecolumnid.lastIndexOf(")");
                        if (l > -1) {
                            valuecolumnid = valuecolumnid.substring(l + 1).trim();
                        }
                        if ((l = catcolumnid.lastIndexOf(")")) > -1) {
                            catcolumnid = catcolumnid.substring(l + 1).trim();
                        }
                        for (int r = 0; r < temp.getRowCount(); ++r) {
                            BigDecimal bd;
                            int nr = dsMonitorData.addRow();
                            dsMonitorData.setValue(nr, "measurecategory", temp.getValue(r, catcolumnid, ""));
                            if (catcolumnid_2.length() > 0) {
                                dsMonitorData.setValue(nr, "measurecategory2", temp.getValue(r, catcolumnid_2, ""));
                            }
                            if (catcolumnid_3.length() > 0) {
                                dsMonitorData.setValue(nr, "measurecategory3", temp.getValue(r, catcolumnid_3, ""));
                            }
                            if (temp.getColumnType(valuecolumnid) == 1) {
                                bd = temp.getBigDecimal(r, valuecolumnid);
                            } else {
                                try {
                                    bd = new BigDecimal(temp.getValue(r, valuecolumnid));
                                }
                                catch (Exception e) {
                                    bd = new BigDecimal(0);
                                }
                            }
                            if (bd == null) {
                                Trace.logDebug("Failed to obtain measure value.");
                                bd = new BigDecimal(0);
                            }
                            dsMonitorData.setNumber(nr, "measurevalue", bd);
                        }
                    }
                } else {
                    Trace.logWarn("Could not obtain SDI Data for Chart. Check properties.");
                }
            } else {
                Trace.logWarn("No SDC Id provided for Chart. Check properties.");
            }
        } else if (querytype.equalsIgnoreCase("SDI Aggregate")) {
            PropertyList data = this.getPropertyList(props, "dataaggregate");
            String sdcid = data.getProperty("sdcid");
            String orderby = "";
            if (sdcid.length() > 0) {
                String categoryid = "";
                String category_keyid2 = "";
                String category_keyid3 = "";
                if (data.containsKey("category")) {
                    if (data.getPropertyList("category").getProperty("categorytype", "columnid").equalsIgnoreCase("custom")) {
                        categoryid = data.getPropertyList("category").getProperty("custom");
                    } else {
                        orderby = categoryid = data.getPropertyList("category").getProperty("columnid");
                    }
                }
                if (categoryid.length() == 0) {
                    categoryid = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                    category_keyid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
                    category_keyid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
                } else if (categoryid.indexOf(";") > -1) {
                    String[] split = StringUtil.split(categoryid, ";", true);
                    categoryid = split[0];
                    if (orderby.length() > 0) {
                        orderby = split[0];
                    }
                    if (split.length > 1) {
                        category_keyid2 = split[1];
                        if (split.length > 2) {
                            category_keyid3 = split[2];
                        }
                    }
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
                    sql.append(categoryid).append(" AS measurecategory");
                    sql.append(", ");
                    if (category_keyid2.length() > 0) {
                        sql.append(category_keyid2).append(" AS measurecategory2");
                        sql.append(", ");
                    }
                    if (category_keyid3.length() > 0) {
                        sql.append(category_keyid3).append(" AS measurecategory3");
                        sql.append(", ");
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
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (data.getProperty("querywhere").trim().length() > 0) {
                        sql.append("WHERE ").append(data.getProperty("querywhere"));
                        if (securitywhere.length() > 0) {
                            sql.append(" AND ").append(securitywhere);
                        }
                    } else if (securitywhere.length() > 0) {
                        sql.append("WHERE ").append(securitywhere);
                    }
                    orderby = data.getProperty("queryorderby", orderby);
                    if (groupby) {
                        sql.append(" ").append("GROUP BY ").append(categoryid);
                        if (category_keyid2.length() > 0) {
                            sql.append(", ").append("").append(category_keyid2);
                        }
                        if (category_keyid3.length() > 0) {
                            sql.append(", ").append("").append(category_keyid3);
                        }
                        if (orderby.length() > 0 && !orderby.equalsIgnoreCase(categoryid)) {
                            sql.append(", ").append("").append(orderby);
                        }
                    }
                    if (orderby.length() > 0) {
                        sql.append(" ").append("ORDER BY ").append(orderby);
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
            String monitorsql = this.getProperty(props, "monitorsql", "");
            StringBuffer sql = new StringBuffer();
            if (querytype.equalsIgnoreCase("Monitor")) {
                String monitorid = props.getProperty("monitorid", "");
                String tableid = (String)this.getSDCProcessor().getSDCProperties(MEASUREMENT_SDC).get("tableid");
                String monitorall = props.getProperty("monitorall", "");
                Trace.log("DMS", "Streamer: creating " + charttype + " from dataset");
                if (monitorall.equalsIgnoreCase("all")) {
                    sql.append("select measurevalue, measurecategory from ").append(tableid).append(" where latestflag = 'Y' and monitorid='").append(monitorid).append("' and measurecategory = 'All'");
                } else {
                    sql.append("select measurevalue, measurecategory from ").append(tableid).append(" where latestflag = 'Y' and monitorid='").append(monitorid).append("' and measurecategory != 'All'");
                }
            } else {
                sql.append(monitorsql);
            }
            if (monitorsql.length() > 0) {
                String[] tks = StringUtil.getExpressionTokens(sql.toString());
                String finalsql = sql.toString();
                for (int i = 0; i < tks.length; ++i) {
                    String key = tks[i];
                    if (key.equalsIgnoreCase("%currentuser%")) {
                        key = "currentuser";
                    }
                    if (!props.containsKey(key) || !(props.get(key) instanceof String) || props.getProperty(key, "").length() <= 0) continue;
                    finalsql = StringUtil.replaceAll(finalsql, "[" + tks[i] + "]", props.getProperty(key, ""));
                }
                dsMonitorData = this.getQp().getSqlDataSet(finalsql);
            } else {
                Trace.logWarn("No SQL provided for Chart. Check properties.");
            }
        }
        return dsMonitorData;
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

    public QueryProcessor getQp() {
        return this.qp;
    }

    public void setQp(QueryProcessor qp) {
        this.qp = qp;
    }
}

