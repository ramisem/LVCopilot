/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.trackitemlist;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.tagext.QueryData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TrackItemList
extends BaseElement {
    private static Set<String> trackitemcolumns = null;
    StringBuffer sc = new StringBuffer();

    @Override
    public String getHtml() {
        String rowsperpage = this.element.getProperty("rowsperpage", "").trim();
        this.element.setProperty("enablefixedheader", "N");
        StringBuilder sb = new StringBuilder();
        sb.append("<style>");
        sb.append("div.tilist_pager { display:inline;padding:0;margin:0;float:right;margin-right:10px; }");
        sb.append("div.tilist_pager div { display:inline;padding-left:2px;padding-right:2px; }");
        sb.append("div.tilist_pager div:hover { display:inline;background:#efefef; }");
        sb.append("</style>");
        sb.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/scripts/trackitemlist.js\"></script>");
        sb.append("<div style='position:relative'>");
        sb.append("<div id='trackitemlist_datainfo' style='padding-left:4px;float:left;'>");
        sb.append("<div style='display:inline' id='trackitemlist_totalcount'>0</div>&nbsp;<span style='color:gray'>Total [</span>&nbsp;<div style='display:inline' id='trackitemlist_selectedcount'>0</div>&nbsp;<span style='color:gray'>selected ]</span>");
        sb.append("</div>");
        sb.append("<div class='tilist_pager' id='tilist_pager_top'></div>");
        sb.append("</div>");
        PropertyListCollection includes = this.element.getCollectionNotNull("jsincludes");
        for (int i = 0; i < includes.size(); ++i) {
            String src = includes.getPropertyList(i).getProperty("src");
            if (StringUtil.getLen(src) <= 0L) continue;
            sb.append("<script language=\"JavaScript\" type=\"text/javascript\" src=\"").append(src).append("\"></script>");
        }
        this.sc.append("\nvar til = new trackitemlist();");
        this.sc.append("\ntil.setId( \"").append(this.elementid).append("\" );");
        if (this.element == null) {
            sb.append("No element data found for the sdilist tag.");
        } else {
            DataSet ds;
            String rsetid;
            String keyid1;
            String sdcid;
            String storageUnitId;
            HashMap<Object, Object> reservetrackitems;
            String trackitems;
            block36: {
                trackitems = "";
                reservetrackitems = new HashMap();
                PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
                String elementid = this.element.getId();
                storageUnitId = TrackItemList.getProperty(pagedata, elementid + "_storageunitid", "");
                sdcid = TrackItemList.getProperty(pagedata, elementid + "_sdcid", "");
                keyid1 = TrackItemList.getProperty(pagedata, elementid + "_keyid1", "");
                try {
                    if (storageUnitId.length() > 0) {
                        rsetid = this.getStorageUnitRSetID(storageUnitId);
                        if (rsetid != null && rsetid.length() > 0) {
                            trackitems = this.populateTrackItems(rsetid);
                            reservetrackitems = this.populateReserveTrackItemsRset(trackitems, rsetid);
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                        this.sc.append("\ntil.setStorageunitid( '").append(storageUnitId).append("' );");
                        break block36;
                    }
                    if (sdcid.length() <= 0 || keyid1.length() <= 0) break block36;
                    try {
                        trackitems = this.populateTrackItems(sdcid, keyid1);
                    }
                    catch (SapphireException e) {
                        this.logger.error("Error", e);
                    }
                    reservetrackitems = this.populateReserveTrackItems(sdcid, keyid1);
                }
                catch (SapphireException e) {
                    this.logger.error("Error", e);
                }
            }
            if (OpalUtil.isNotEmpty(trackitems)) {
                rsetid = null;
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                if (StringUtil.getLen(trackitems) <= 2000L) {
                    sql.append("select ").append(OpalUtil.toDelimitedString(this.getElementColumns(), ","));
                    sql.append(" from trackitem");
                    sql.append(" where trackitem.trackitemid in (").append(safeSQL.addIn(trackitems, ";")).append(")");
                } else {
                    try {
                        rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitems, null, null);
                    }
                    catch (SapphireException e) {
                        this.logger.error("Error", e);
                    }
                    sql.append("select ").append(OpalUtil.toDelimitedString(this.getElementColumns(), ","));
                    sql.append(" from trackitem");
                    sql.append(" where trackitem.trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                }
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                if (StringUtil.getLen(sdcid) > 0L && StringUtil.getLen(keyid1) > 0L && ds.isValidColumn("linkkeyid1")) {
                    String[] keys;
                    DataSet sortedDS = new DataSet();
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    for (String key : keys = StringUtil.split(keyid1, ";")) {
                        if ("TrackItemSDC".equals(sdcid)) {
                            findMap.put("trackitemid", key);
                        } else {
                            findMap.put("linkkeyid1", key);
                        }
                        int row = ds.findRow(findMap);
                        if (row == -1) continue;
                        sortedDS.copyRow(ds, row, 1);
                    }
                    ds.clear();
                    ds = sortedDS;
                }
            } else {
                this.getElementColumns();
                ds = new DataSet();
            }
            if (ds != null) {
                if (ds.size() == 0) {
                    ds.addRow();
                    this.sc.append("\ntil.getTable().deleteRow( 1 );");
                } else {
                    if (!ds.isValidColumn("__rowid")) {
                        ds.addColumn("__rowid", 0);
                    }
                    for (int row = 0; row < ds.size(); ++row) {
                        ds.setValue(row, "__rowid", String.valueOf(row));
                        String trackitemid = ds.getValue(row, "trackitemid", "");
                        if (ds.isValidColumn("reservestorageunitid") && reservetrackitems.containsKey(trackitemid)) {
                            ds.setValue(row, "reservestorageunitid", ((ReserveItem)reservetrackitems.get(trackitemid)).getReservestorageunitid());
                            reservetrackitems.remove(trackitemid);
                        }
                        this.sc.append("\nvar _row = [];");
                        this.sc.append("\n_row['trackitemid'] = '").append(ds.getValue(row, "trackitemid", "")).append("';");
                        this.sc.append("\n_row['linksdcid'] = '").append(ds.getValue(row, "linksdcid", "")).append("';");
                        this.sc.append("\n_row['linkkeyid1'] = '").append(ds.getValue(row, "linkkeyid1", "")).append("';");
                        this.sc.append("\n_row['currentstorageunitid'] = '").append(ds.getValue(row, "currentstorageunitid", "")).append("';");
                        this.sc.append("\n_row['labelpath'] = '").append(SafeHTML.encodeForJavaScript(ds.getValue(row, "labelpath", ""))).append("';");
                        this.sc.append("\n_row['custodialuserid'] = '").append(ds.getValue(row, "custodialuserid", "")).append("';");
                        this.sc.append("\n_row['reservestorageunitid'] = '").append(ds.getValue(row, "reservestorageunitid", "")).append("';");
                        this.sc.append("\n_row['__newrow'] = 'N';");
                        this.sc.append("\ntil.addData( _row );");
                    }
                }
            } else {
                ds = new DataSet();
            }
            if (reservetrackitems.size() > 0 && ds.isValidColumn("reservestorageunitid")) {
                for (Object o : reservetrackitems.keySet()) {
                    String trackitemid = (String)o;
                    ReserveItem reserveItem = (ReserveItem)reservetrackitems.get(trackitemid);
                    int row = ds.addRow();
                    ds.setValue(row, "trackitemid", reserveItem.getTrackitemid());
                    ds.setValue(row, "reservestorageunitid", reserveItem.getReservestorageunitid());
                    if (ds.isValidColumn("linksdcid")) {
                        ds.setValue(row, "linksdcid", reserveItem.getSdcid());
                    }
                    if (ds.isValidColumn("linkkeyid1")) {
                        ds.setValue(row, "linkkeyid1", reserveItem.getKeyid1());
                    }
                    if (ds.isValidColumn("currentstorageunitid")) {
                        ds.setValue(row, "currentstorageunitid", reserveItem.getCurrentstorageunitid());
                    }
                    if (ds.isValidColumn("custodialuserid")) {
                        ds.setValue(row, "custodialuserid", reserveItem.getCustodialuserid());
                    }
                    this.sc.append("\nvar _row = [];");
                    this.sc.append("\n_row['trackitemid'] = '").append(ds.getValue(row, "trackitemid", "")).append("';");
                    this.sc.append("\n_row['linksdcid'] = '").append(ds.getValue(row, "linksdcid", "")).append("';");
                    this.sc.append("\n_row['linkkeyid1'] = '").append(ds.getValue(row, "linkkeyid1", "")).append("';");
                    this.sc.append("\n_row['currentstorageunitid'] = '").append(ds.getValue(row, "currentstorageunitid", "")).append("';");
                    this.sc.append("\n_row['labelpath'] = '").append(SafeHTML.encodeForJavaScript(ds.getValue(row, "labelpath", ""))).append("';");
                    this.sc.append("\n_row['custodialuserid'] = '").append(ds.getValue(row, "custodialuserid", "")).append("';");
                    this.sc.append("\n_row['reservestorageunitid'] = '").append(ds.getValue(row, "reservestorageunitid", "")).append("';");
                    this.sc.append("\n_row['__newrow'] = 'N';");
                    this.sc.append("\ntil.addData( _row );");
                }
            }
            this.pageContext.removeAttribute("returncolids");
            this.pageContext.removeAttribute(this.element.getId() + "_returncolidlist");
            QueryData queryData = new QueryData("primary", ds);
            HashMap<String, QueryData> queryDataMap = new HashMap<String, QueryData>();
            queryDataMap.put("primary", queryData);
            SDITagInfo sdiinfo = new SDITagInfo(queryDataMap);
            List list = new List(this.pageContext, sdiinfo, this.getSDCProcessor());
            PropertyListCollection sortby = this.element.getCollectionNotNull("sortby");
            if (sortby.size() == 0) {
                list.setOverridepageorder("Y");
            }
            this.element.setProperty("rowsperpage", "");
            this.element.setProperty("retrievelimit", "");
            list.setElementProperties(this.element);
            sb.append(list.getHtml());
            sb.append("<div class='tilist_pager' id='tilist_pager_bottom'></div>");
            sb.append(this.renderStyleSheet());
            this.sc.append("\ntil.setLabelpath(\"").append(OpalUtil.isNotEmpty(storageUnitId) ? SafeHTML.encodeForJavaScript(OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "labelpath", "storageunitid = ?", new String[]{storageUnitId})) : "").append("\")");
            if (rowsperpage.length() > 0) {
                this.sc.append("\ntil.setPager(\"").append(rowsperpage).append("\", \"").append(ds.size()).append("\")");
            }
        }
        if ("none".equals(this.element.getProperty("selectortype"))) {
            this.sc.append("\ntil.setShowSelector( false );");
        }
        this.sc.append("\ntil.init();");
        sb.append("\n<script>").append(this.sc).append("</script>");
        return sb.toString();
    }

    private static String getProperty(PropertyList pagedata, String property, String defaultValue) {
        String value = pagedata.getProperty(property);
        if (value == null || value.length() == 0) {
            value = defaultValue;
        }
        return value;
    }

    private ArrayList<String> getElementColumns() {
        ArrayList<String> columnList = new ArrayList<String>();
        PropertyListCollection columnCollection = this.element.getCollection("columns");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        for (int i = 0; i < columnCollection.size(); ++i) {
            PropertyList column = columnCollection.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            if (columnid.length() <= 0) continue;
            boolean isProductColumn = "Y".equals(column.getProperty("productflag", "N"));
            if (!columnid.contains("(")) {
                if (!this.isValidColumn(columnid)) continue;
                columnList.add("trackitem." + columnid);
                if (!"Hidden Value".equalsIgnoreCase(column.getProperty("mode"))) {
                    this.sc.append("\ntil.addColumn( \"").append(columnid).append("\", \"display\" );");
                    if (isProductColumn) continue;
                    this.sc.append("\ntil.addCustomColumn( \"").append(columnid).append("\", \"display\" );");
                    continue;
                }
                this.sc.append("\ntil.addColumn( \"").append(columnid).append("\", \"hidden\" );");
                continue;
            }
            columnid = StringUtil.replaceAll(columnid, "[%currentuser%]", sysuserid);
            columnid = StringUtil.replaceAll(columnid, "[currentuser]", sysuserid);
            columnList.add(columnid);
            this.sc.append("\ntil.addColumn( \"").append(columnid.substring(columnid.lastIndexOf(")") + 1).trim()).append("\", \"display\" );");
            if (isProductColumn || "Hidden Value".equalsIgnoreCase(column.getProperty("mode"))) continue;
            this.sc.append("\ntil.addCustomColumn( \"").append(columnid).append("\", \"display\" );");
        }
        return columnList;
    }

    private String renderStyleSheet() {
        return "<style type=\"text/css\">.list_tablerowdisabled {background:#cdcdcd;}.list_tablestatuscell {background:gainsboro;width:20px;text-align:left;padding:2px;}</style>";
    }

    private String populateTrackItems(String sdcid, String keyid1) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        String rsetid = null;
        if (StringUtil.getLen(keyid1) <= 2000L) {
            if (keyid1.contains("%3B")) {
                keyid1 = StringUtil.replaceAll(keyid1, "%3B", "','");
            } else if (keyid1.contains(";")) {
                keyid1 = StringUtil.replaceAll(keyid1, ";", "','");
            }
            if ("TrackItemSDC".equals(sdcid)) {
                sql.append("select t.trackitemid");
                sql.append(" from trackitem t");
                sql.append(" where t.trackitemid in (").append(safeSQL.addIn(keyid1)).append(")");
            } else {
                sql.append("select t.trackitemid");
                sql.append(" from trackitem t");
                sql.append(" where t.linksdcid = ").append(safeSQL.addVar(sdcid));
                sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(keyid1)).append(" )");
            }
        } else if ("TrackItemSDC".equals(sdcid)) {
            rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", keyid1, null, null);
            sql.append("select t.trackitemid");
            sql.append(" from trackitem t");
            sql.append(" where t.trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
        } else {
            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, null, null);
            sql.append("select t.trackitemid");
            sql.append(" from trackitem t");
            sql.append(" where t.linksdcid = '").append(sdcid).append("'");
            sql.append(" and t.linkkeyid1 in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            return ds.getColumnValues("trackitemid", ";");
        }
        return "";
    }

    private String populateTrackItems(String storageunitrsetid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitem.trackitemid from trackitem where trackitem.currentstorageunitid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)", (Object[])new String[]{storageunitrsetid});
        return ds != null ? ds.getColumnValues("trackitemid", ";") : "";
    }

    private String getStorageUnitRSetID(String storageunitid) throws SapphireException {
        ArrayList<String> list = new ArrayList<String>();
        this.populateStorageUnitHeirarchy(storageunitid, list);
        return list.size() > 0 ? this.getDAMProcessor().createRSet("StorageUnitSDC", OpalUtil.toDelimitedString(list, ";"), "", "") : "";
    }

    private void populateStorageUnitHeirarchy(String storageunitid, ArrayList<String> list) {
        if (this.areTrackItemsAllowed(storageunitid)) {
            list.add(storageunitid);
        } else {
            ArrayList<String> children = this.getChildren(storageunitid);
            for (String aChildren : children) {
                list.add(aChildren);
            }
        }
    }

    private ArrayList<String> getChildren(String storageunitid) {
        ArrayList<String> childs = new ArrayList<String>();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunit.storageunitid from storageunit where storageunit.parentid = ?", (Object[])new String[]{storageunitid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                childs.add(ds.getValue(i, "storageunitid"));
            }
        }
        return childs;
    }

    private boolean areTrackItemsAllowed(String storageunitid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunit.trackitemallowedflag, storageunit.storageunittype from storageunit where storageunit.storageunitid = ?", (Object[])new String[]{storageunitid});
        return ds != null && ds.size() > 0 && "Y".equals(ds.getString(0, "trackitemallowedflag"));
    }

    public HashMap<String, ReserveItem> populateReserveTrackItemsRset(String trackitems, String rsetid) throws SapphireException {
        HashMap<String, ReserveItem> map = new HashMap<String, ReserveItem>();
        if (OpalUtil.isNotEmpty(trackitems)) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            String temprsetid = null;
            if (StringUtil.getLen(trackitems) <= 2000L) {
                sql.append("select t.storageunitid, t.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.currentstorageunitid, ti.custodialuserid,");
                sql.append(" (select s.labelpath from storageunit s where s.storageunitid=t.storageunitid) labelpath");
                sql.append(" from reservestorageunit t, trackitem ti");
                sql.append(" where ti.trackitemid in ( ").append(safeSQL.addIn(trackitems, ";")).append(" )");
                sql.append(" and t.trackitemid = ti.trackitemid");
                sql.append(" union ");
                sql.append("select t.storageunitid, t.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.currentstorageunitid, ti.custodialuserid,");
                sql.append(" (select s.labelpath from storageunit s where s.storageunitid=t.storageunitid) labelpath");
                sql.append(" from reservestorageunit t, trackitem ti");
                sql.append(" where t.storageunitid in ( select rsetitems.keyid1 ");
                sql.append(" from rsetitems ");
                sql.append(" where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" ) ");
                sql.append(" and t.trackitemid = ti.trackitemid");
            } else {
                temprsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitems, null, null);
                sql.append("select t.storageunitid, t.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.currentstorageunitid, ti.custodialuserid,");
                sql.append(" (select s.labelpath from storageunit s where s.storageunitid=t.storageunitid) labelpath");
                sql.append(" from reservestorageunit t, trackitem ti");
                sql.append(" where ti.trackitemid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(temprsetid)).append(" )");
                sql.append(" and t.trackitemid = ti.trackitemid");
                sql.append(" union ");
                sql.append("select t.storageunitid, t.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.currentstorageunitid, ti.custodialuserid,");
                sql.append(" (select s.labelpath from storageunit s where s.storageunitid=t.storageunitid) labelpath");
                sql.append(" from reservestorageunit t, trackitem ti");
                sql.append(" where t.storageunitid in ( select rsetitems.keyid1 ");
                sql.append(" from rsetitems ");
                sql.append(" where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql.append(" and t.trackitemid = ti.trackitemid");
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (StringUtil.getLen(temprsetid) > 0L) {
                this.getDAMProcessor().clearRSet(temprsetid);
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    String trackitemid = ds.getValue(i, "trackitemid");
                    String labelpath = ds.getValue(i, "labelpath");
                    if (map.containsKey(trackitemid)) {
                        map.get(trackitemid).setReservestorageunitid(labelpath);
                        continue;
                    }
                    ReserveItem reserveItem = new ReserveItem(trackitemid);
                    reserveItem.setSdcid(ds.getValue(i, "linksdcid"));
                    reserveItem.setKeyid1(ds.getValue(i, "linkkeyid1"));
                    reserveItem.setCurrentstorageunitid(ds.getValue(i, "currentstorageunitid"));
                    reserveItem.setCustodialuserid(ds.getValue(i, "custodialuserid"));
                    reserveItem.setReservestorageunitid(labelpath);
                    map.put(trackitemid, reserveItem);
                }
            }
        }
        return map;
    }

    public HashMap<String, ReserveItem> populateReserveTrackItems(String sdcid, String keyid1) throws SapphireException {
        String rsetid = null;
        HashMap<String, ReserveItem> map = new HashMap<String, ReserveItem>();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.storageunitid, t.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.currentstorageunitid, ti.custodialuserid,");
        sql.append(" (select s.labelpath from storageunit s where s.storageunitid=t.storageunitid) labelpath");
        sql.append(" from reservestorageunit t, trackitem ti");
        sql.append(" where ti.linksdcid = ").append(safeSQL.addVar(sdcid));
        if (StringUtil.getLen(keyid1) <= 2000L) {
            if (keyid1.contains("%3B")) {
                keyid1 = StringUtil.replaceAll(keyid1, "%3B", "','");
            } else if (keyid1.contains(";")) {
                keyid1 = StringUtil.replaceAll(keyid1, ";", "','");
            }
            sql.append(" and ti.linkkeyid1 in ( ").append(safeSQL.addIn(keyid1)).append(" )");
            sql.append(" and t.trackitemid = ti.trackitemid");
        } else {
            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, null, null);
            sql.append(" and ti.linkkeyid1 in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            sql.append(" and t.trackitemid = ti.trackitemid");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String trackitemid = ds.getValue(i, "trackitemid");
                String labelpath = ds.getValue(i, "labelpath");
                if (map.containsKey(trackitemid)) {
                    map.get(trackitemid).setReservestorageunitid(labelpath);
                    continue;
                }
                ReserveItem reserveItem = new ReserveItem(trackitemid);
                reserveItem.setSdcid(ds.getValue(i, "linksdcid"));
                reserveItem.setKeyid1(ds.getValue(i, "linkkeyid1"));
                reserveItem.setCurrentstorageunitid(ds.getValue(i, "currentstorageunitid"));
                reserveItem.setCustodialuserid(ds.getValue(i, "custodialuserid"));
                reserveItem.setReservestorageunitid(labelpath);
                map.put(trackitemid, reserveItem);
            }
        }
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return map;
    }

    private boolean isValidColumn(String columnid) {
        if (trackitemcolumns == null) {
            trackitemcolumns = new HashSet<String>();
            DataSet ds = this.getSDCProcessor().getColumnData("TrackItemSDC");
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    trackitemcolumns.add(ds.getValue(i, "columnid"));
                }
            }
        }
        return trackitemcolumns.contains(columnid);
    }

    class ReserveItem {
        private String trackitemid;
        private String sdcid;
        private String keyid1;
        private String currentstorageunitid;
        private String custodialuserid;
        private StringBuffer sb;

        public ReserveItem(String trackitemid) {
            this.trackitemid = trackitemid;
            this.sb = new StringBuffer();
        }

        public String getTrackitemid() {
            return this.trackitemid;
        }

        public String getSdcid() {
            return this.sdcid;
        }

        public void setSdcid(String sdcid) {
            this.sdcid = sdcid;
        }

        public String getKeyid1() {
            return this.keyid1;
        }

        public void setKeyid1(String keyid1) {
            this.keyid1 = keyid1;
        }

        public String getCurrentstorageunitid() {
            return this.currentstorageunitid;
        }

        public void setCurrentstorageunitid(String currentstorageunitid) {
            this.currentstorageunitid = currentstorageunitid;
        }

        public String getCustodialuserid() {
            return this.custodialuserid;
        }

        public void setCustodialuserid(String custodialuserid) {
            this.custodialuserid = custodialuserid;
        }

        public String getReservestorageunitid() {
            return this.sb.toString();
        }

        public void setReservestorageunitid(String reservestorageunitid) {
            if (this.sb.length() > 0) {
                this.sb.append(";").append(reservestorageunitid);
            } else {
                this.sb.append(reservestorageunitid);
            }
        }
    }
}

