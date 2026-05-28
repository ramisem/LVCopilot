/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public abstract class MultiMaintGrid
extends BaseElement {
    public static final String DEFAULT_GROUP = "NA";
    public static final String JS_CLASS = "multiMaintGrid";
    private TreeMap<String, String> x;
    private TreeMap<String, String> xCols;
    private TreeMap<String, String> y;
    private HashMap<String, Map> cells;
    private HashMap<String, Map> columnCells;
    private TreeMap<String, ArrayList<String>> grouping;
    private TreeMap<String, String> childx;
    private TreeMap<String, String> childy;
    private HashMap<String, Map> childcells;
    private TreeMap<String, String> subchildx;
    private TreeMap<String, String> subchildy;
    private HashMap<String, Map> subchildcells;
    ArrayList primaryColumns = new ArrayList();
    private String error;
    private boolean endColumn;
    private boolean endRow;
    protected boolean scrollGrid = false;
    protected String rightBorderStyle = "border-right:2px solid #696969";
    protected boolean viewonly = false;
    protected boolean esigEnabled = false;
    protected boolean collapseGroups = false;
    public static final String PROPERTY_EXPANDEDGROUP = "expandedgroups";
    protected String[] expandedGroupsArray;
    protected String groupClick = "multiMaintGrid.toggleGroup";
    protected int rowHeight = 30;
    private Comparator<String> comparatorX = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return MultiMaintGrid.this.compareX(o1, o2);
        }
    };
    private Comparator<String> comparatorY = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return MultiMaintGrid.this.compareY(o1, o2);
        }
    };
    private Comparator<String> comparatorGroup = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return MultiMaintGrid.this.compareGroup(o1, o2);
        }
    };
    private Comparator<String> comparatorGroupEntry = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return MultiMaintGrid.this.compareGroupEntry(o1, o2);
        }
    };

    public void setError(String error) {
        this.logger.error(error);
        this.error = error;
    }

    public void setEndColumn(boolean endColumn) {
        this.endColumn = endColumn;
    }

    public void setEndRow(boolean endRow) {
        this.endRow = endRow;
    }

    private void renderError(String message, StringBuffer html) {
        this.logger.error(message);
        html.append("<div style=\"color: red;\">").append(message).append("</div>");
    }

    protected int compareY(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    protected int compareX(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    protected int compareGroup(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    protected int compareGroupEntry(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    protected ArrayList<String> getGroups() {
        ArrayList<String> out = new ArrayList<String>();
        for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
            String groupid = groupEntry.getKey();
            out.add(groupid);
        }
        return out;
    }

    public MultiMaintGrid() {
        this.x = new TreeMap(this.comparatorX);
        this.y = new TreeMap(this.comparatorY);
        this.grouping = new TreeMap(this.comparatorGroup);
        this.cells = new HashMap();
        this.columnCells = new HashMap();
        this.childx = new TreeMap(this.comparatorX);
        this.childy = new TreeMap(this.comparatorY);
        this.childcells = new HashMap();
        this.subchildx = new TreeMap(this.comparatorX);
        this.subchildy = new TreeMap(this.comparatorY);
        this.subchildcells = new HashMap();
        this.xCols = new TreeMap(this.comparatorX);
    }

    public void set(String xKey, String yKey, Map cell) {
        this.set(xKey, xKey, yKey, yKey, cell);
    }

    public void set(String xKey, String xTitle, String yKey, String yTitle, Map cell) {
        this.set(xKey, xTitle, yKey, yTitle, DEFAULT_GROUP, cell, false);
    }

    public void setChildKey(String xKey, String xTitle, String yKey, String yTitle, Map cell, int childlevel) {
        this.setChildKey(xKey, xTitle, yKey, yTitle, DEFAULT_GROUP, cell, childlevel);
    }

    public void setChildSDIKeys() {
    }

    public void set(String xKey, String xTitle, String yKey, String yTitle, TreeMap x, TreeMap y, String yGroup, Map cell, HashMap cells) {
        String key;
        if (xKey != null && !x.containsKey(xKey)) {
            x.put(xKey, xTitle);
        }
        if (yKey != null && !y.containsKey(yKey)) {
            y.put(yKey, yTitle == null ? yKey : yTitle);
        }
        String string = key = xKey != null ? xKey : "";
        if (yKey != null) {
            key = key + (xKey != null ? ";" : "") + yKey;
        }
        if (cells.containsKey(key)) {
            cells.remove(key);
        }
        if (yGroup != null && !this.grouping.containsKey(yGroup)) {
            this.grouping.put(yGroup, new ArrayList());
        }
        if (yKey != null && yGroup != null && !this.grouping.get(yGroup).contains(yKey)) {
            this.grouping.get(yGroup).add(yKey);
        }
        if (cell == null) {
            cell = new HashMap();
        }
        cells.put(key, cell);
    }

    public void set(String xKey, String xTitle, String yKey, String yTitle, String yGroup, Map cell) {
        this.set(xKey, xTitle, yKey, yTitle, yGroup, cell, false);
    }

    public void set(String xKey, String xTitle, String yKey, String yTitle, String yGroup, Map cell, boolean column) {
        if (!column) {
            this.set(xKey, xTitle, yKey, yTitle, this.x, this.y, yGroup, cell, this.cells);
        } else {
            this.primaryColumns.add(xKey);
            this.set(xKey, xTitle, yKey, yTitle, this.xCols, this.y, yGroup, cell, this.columnCells);
        }
    }

    public void setChildKey(String xKey, String xTitle, String yKey, String yTitle, String yGroup, Map cell, int childLevel) {
        if (childLevel == 1) {
            String key;
            if (xKey != null && !this.childx.containsKey(xKey)) {
                this.childx.put(xKey, xTitle);
            }
            if (yKey != null && !this.childy.containsKey(yKey)) {
                this.childy.put(yKey, yTitle == null ? yKey : yTitle);
            }
            String string = key = xKey != null ? xKey : "";
            if (yKey != null) {
                key = key + (xKey != null ? ";" : "") + yKey;
            }
            if (this.childcells.containsKey(key)) {
                this.childcells.remove(key);
            }
            if (cell == null) {
                cell = new HashMap();
            }
            this.childcells.put(key, cell);
        } else {
            String key;
            if (xKey != null && !this.childx.containsKey(xKey)) {
                this.subchildx.put(xKey, xTitle);
            }
            if (yKey != null && !this.childy.containsKey(yKey)) {
                this.subchildy.put(yKey, yTitle == null ? yKey : yTitle);
            }
            String string = key = xKey != null ? xKey : "";
            if (yKey != null) {
                key = key + (xKey != null ? ";" : "") + yKey;
            }
            if (this.subchildcells.containsKey(key)) {
                this.subchildcells.remove(key);
            }
            if (cell == null) {
                cell = new HashMap();
            }
            this.subchildcells.put(key, cell);
        }
    }

    public Map get(String x, String y) {
        String key = x + ";" + y;
        return this.cells.get(key);
    }

    public TreeMap<String, Map> getByY(String y) {
        TreeMap<String, Map> out = new TreeMap<String, Map>(new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for (int i = 0; i < this.x.size(); ++i) {
            String key = this.x.get(i) + ";" + y;
            out.put(this.x.get(i), this.cells.get(key));
        }
        return out;
    }

    public TreeMap<String, String> getX() {
        return this.x;
    }

    public TreeMap<String, String> getY() {
        return this.y;
    }

    public abstract String getXHeader(String var1, String var2);

    protected abstract String getSDIAcrossModeParentColumnValue(String var1, String var2, String var3, DataSet var4);

    protected abstract String getSDIAcrossModeColumnValue(String var1, String var2, String var3, DataSet var4);

    protected abstract String getSDIAcrossModeParentWorkItemColumnValue(String var1, String var2, String var3, DataSet var4);

    public String getXHeader(String xKey, String xTitle, boolean useTableHeader) {
        return "";
    }

    public String getColumnXHeader(String xKey, String xTitle, boolean useTableHeader) {
        return "";
    }

    public abstract String getChildSDIXHeader(String var1, String var2, String var3, int var4);

    public abstract boolean isReadOnlyInformationOnly(String var1);

    public abstract String getYHeader(String var1, String var2);

    public abstract String getCell(String var1, String var2, Map var3);

    public abstract String getCell(String var1, String var2, Map var3, String var4, int var5);

    public abstract String getEmptyCell(String var1, String var2);

    public abstract String getEmptyCell(String var1, String var2, String var3, int var4);

    public abstract String getYHeaderEndColumn();

    protected String getSDIDataSortBy() {
        return "";
    }

    protected String getSDIWISortBy() {
        return "";
    }

    protected abstract String getSDCId();

    protected abstract String getParentSDCId();

    protected void getColumnCell(String colKey, String sdiKey, StringBuffer html, String colId, String colValue, String width, String rtBorder) {
        Map cell = null;
        String key = colKey + ";" + sdiKey;
        if (this.columnCells.containsKey(key)) {
            cell = this.columnCells.get(key);
            html.append("<td class=\"maintform_field\" style=\"min-width:100px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:nowrap;border-bottom:none;" + rtBorder + "\" >").append(this.getCell(colKey, sdiKey, cell)).append("</td>");
        }
        if (cell == null || cell.size() == 0) {
            html.append("<td class=\"maintform_field\" valign=\"top\" style=\"min-width:100px;width:" + width + "px;border-bottom:none;text-align:left;padding-left:0px;word-wrap:break-word;" + rtBorder + "\" >").append(colValue).append("</td>");
        }
    }

    protected boolean getColumnHeader(StringBuffer html, String colId, String width, String rtBorder) {
        for (Map.Entry<String, String> xEntry : this.xCols.entrySet()) {
            if (!this.primaryColumns.contains(xEntry.getKey()) || !xEntry.getKey().endsWith("_" + colId)) continue;
            html.append("<th class=\"gridmaint_fieldtitle1\"").append("").append(" style=\"").append("").append(";word-wrap:break-word;width:" + width + "px;" + rtBorder + "\">").append(this.getColumnXHeader(xEntry.getKey(), xEntry.getValue(), true)).append("</th>");
            return true;
        }
        return false;
    }

    protected void getChildSDCColumnHeader(StringBuffer html, String title, String width, String rtBorder) {
        html.append("<th class=\"gridmaint_fieldtitle1\" style=\"width:" + width + "px; word-wrap:break-word;" + rtBorder + "\">");
        html.append("<table style=\"width:100%\"><tr><th>").append(title).append("</th></tr></table>");
        html.append("</th>");
    }

    public abstract String getEndColumn(String var1);

    public abstract String getEndColumn(String var1, String var2);

    public abstract String getEndColumn(String var1, String var2, String var3, String var4);

    public abstract String getCornerHeader();

    public String getHeaderHTML() {
        StringBuffer html = new StringBuffer();
        html.append("<script src=\"WEB-CORE/scripts/tags.js\"></script>");
        html.append("<script src=\"WEB-CORE/scripts/grid.js\"></script>");
        html.append("<script src=\"WEB-CORE/elements/scripts/maint.js\"></script>");
        html.append("<script src=\"WEB-CORE/elements/scripts/multimaintgrid.js\"></script>");
        html.append("<script>");
        html.append(JS_CLASS).append(".elementid = '").append(this.elementid).append("';");
        html.append(JS_CLASS).append(".scrollGrid.enabled = ").append(this.scrollGrid).append(";");
        html.append("</script>");
        return html.toString();
    }

    public abstract String getFooterHTML();

    private void renderGridRow(StringBuffer html, String yKey, String yTitle) {
        this.renderGridRow(html, yKey, yTitle, "", true);
    }

    private void renderYHeader(StringBuffer html, String yKey, String yTitle) {
        html.append("<th class=\"maintform_fieldtitle\" style=\"text-align:left;padding-left:10px\">").append(this.getYHeader(yKey, yTitle)).append("</th>");
    }

    protected abstract boolean isAttributeAcross();

    private void renderAdvancedScrollGridRow(StringBuffer html, String yKey, String yTitle, String styleClass, boolean renderYHeader, DataSet childSDIWI, DataSet childSDIData, int gridrowCnt, int totalChildRowCnt, DataSet sourceDataSet) {
        int thischildSDIWIRowCnt = childSDIWI.getRowCount();
        int thischildSDIDataSetRowCnt = childSDIData.getRowCount();
        boolean childSDIWIFound = this.getChildSDIWIDataSet().getRowCount() > 0;
        boolean childSDIDatasetFound = this.getChildSDIDataDataSet().getRowCount() > 0;
        boolean hideChildSDIWorkItemAttributes = this.hideSDIWorkItemAttributes();
        boolean hideChildSDIDataAttributes = this.hideSDIDataAttributes();
        String sdidataSortBy = this.getSDIDataSortBy();
        childSDIData.sort(sdidataSortBy);
        if (yKey != null) {
            int pcolCnt;
            if (styleClass.length() > 0) {
                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\" " : " ").append("class=\"").append(styleClass).append("\">");
            } else {
                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\"" : "").append(">");
            }
            if (renderYHeader) {
                this.renderYHeader(html, yKey, yTitle);
            }
            ArrayList<Boolean> xArray = new ArrayList<Boolean>();
            for (Map.Entry<String, String> xEntry : this.x.entrySet()) {
                Map cell;
                String key = xEntry.getKey() + ";" + yKey;
                Map map = cell = this.cells.containsKey(key) ? this.cells.get(key) : null;
                if (cell == null || cell.size() == 0) {
                    xArray.add(false);
                    html.append("<td  class=\"maintform_field\" style=\"").append("min-width:100px;vertical-align:top;padding-top: 5px").append(";").append(totalChildRowCnt > 1 ? "border-bottom:none;" : "").append("background-color:#F0F0F0;\">").append(this.getEmptyCell(xEntry.getKey(), yKey)).append("</td>");
                    continue;
                }
                xArray.add(true);
                html.append("<td  class=\"maintform_field\" style=\"").append("min-width:100px;background-color:white;vertical-align:top;padding-top: 5px").append(";").append(";background-color:white;border-bottom:none;").append(";white-space:nowrap;\" nowrap>").append(this.getCell(xEntry.getKey(), yKey, cell)).append("</td>");
            }
            if (this.endRow && !this.viewonly) {
                String sdiworkitemid;
                int findRow;
                boolean showOnlyCurrentWorkItemAttributes;
                String parentSDC = this.getParentSDCId();
                boolean bl = showOnlyCurrentWorkItemAttributes = "LV_RequestItem".equalsIgnoreCase(parentSDC) || "Product".equalsIgnoreCase(parentSDC) || "SamplePoint".equalsIgnoreCase(parentSDC) || "LV_RequestItem".equalsIgnoreCase(parentSDC) || "Location".equalsIgnoreCase(parentSDC) || "LV_ProductStage".equalsIgnoreCase(parentSDC) || "Study".equalsIgnoreCase(parentSDC);
                if ("Sample".equalsIgnoreCase(parentSDC)) {
                    int findRow2 = sourceDataSet.findRow("__key", yKey);
                    String sampleId = sourceDataSet.getValue(findRow2, "__fkey");
                    sampleId = sampleId.substring(0, sampleId.indexOf(";"));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select 1 from s_sample where s_sampleid = ? and templateflag = 'Y'", (Object[])new String[]{sampleId});
                    if (ds.getRowCount() > 0) {
                        showOnlyCurrentWorkItemAttributes = true;
                    }
                }
                String workitemid = "";
                String workitemversionid = "";
                if (showOnlyCurrentWorkItemAttributes && (findRow = sourceDataSet.findRow("__key", yKey)) > -1 && (sdiworkitemid = sourceDataSet.getValue(findRow, "sdiworkitemid") + ";(null);(null)").equals(yKey)) {
                    workitemid = sourceDataSet.getValue(findRow, "workitemid");
                    workitemversionid = sourceDataSet.getValue(findRow, "workitemversionid");
                }
                if (workitemid.length() > 0) {
                    html.append("<td  id=\"td_" + yKey + "\" class=\"maintform_field\" style=\"").append("min-width:35px").append(";white-space:nowrap;border-bottom:none;" + this.rightBorderStyle + "\" nowrap>").append(this.getEndColumn(yKey, "SDIWorkItem", workitemid, workitemversionid)).append("</td>");
                } else {
                    html.append("<td  id=\"td_" + yKey + "\" class=\"maintform_field\" style=\"").append("min-width:35px").append(";white-space:nowrap;border-bottom:none;" + this.rightBorderStyle + "\" nowrap>").append(this.getEndColumn(yKey)).append("</td>");
                }
            }
            int n = pcolCnt = this.viewonly ? xArray.size() : xArray.size() + 1;
            if (thischildSDIWIRowCnt == 0) {
                if (childSDIWIFound) {
                    for (Map.Entry<String, String> xEntry : this.childx.entrySet()) {
                        html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px").append(";background-color:#F0F0F0;\">").append("&nbsp;").append("</td>");
                    }
                    if (this.endRow && !this.viewonly && !hideChildSDIWorkItemAttributes) {
                        html.append("<td class=\"maintform_field\" style=\"").append("min-width:35px").append(";border-top:none;border-bottom:none;white-space:nowrap;background-color:#F0F0F0;" + this.rightBorderStyle + "\" nowrap>").append("&nbsp;").append("</td>");
                    }
                }
                if (thischildSDIDataSetRowCnt == 0) {
                    if (childSDIDatasetFound) {
                        for (Map.Entry<String, String> xEntry : this.subchildx.entrySet()) {
                            html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px").append(";background-color:#F0F0F0;\">").append("&nbsp;").append("</td>");
                        }
                        if (this.endRow && !this.viewonly && !hideChildSDIDataAttributes) {
                            html.append("<td class=\"maintform_field\" style=\"").append("min-width:35px").append(";border-top:none;border-bottom:none;white-space:nowrap;background-color:#F0F0F0;" + this.rightBorderStyle + "\" nowrap>").append("&nbsp;").append("</td>");
                        }
                    }
                    html.append("</tr>");
                } else {
                    for (int j = 0; j < thischildSDIDataSetRowCnt; ++j) {
                        yKey = childSDIData.getValue(j, "sdidataid") + ";(null);(null)";
                        if (j > 0) {
                            if (styleClass.length() > 0) {
                                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\" " : " ").append("class=\"").append(styleClass).append("\">");
                            } else {
                                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\"" : "").append(">");
                            }
                            for (int c = 0; c < pcolCnt; ++c) {
                                String width = "100";
                                if (this.endRow && !this.viewonly && c == pcolCnt - 1) {
                                    width = "30";
                                    html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;").append("border-top:none;border-bottom:none;" + this.rightBorderStyle + "\">").append("&nbsp;").append("</td>");
                                    continue;
                                }
                                html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;background-color:").append((Boolean)xArray.get(c) != false ? "white" : "#F0F0F0").append(";border-top:none;border-bottom:none\">").append("&nbsp;").append("</td>");
                            }
                        }
                        if (this.subchildx.size() > 0) {
                            for (Map.Entry<String, String> xEntry : this.subchildx.entrySet()) {
                                Map cell;
                                String key = xEntry.getKey() + ";" + yKey;
                                Map map = cell = this.subchildcells.containsKey(key) ? this.subchildcells.get(key) : null;
                                if (cell == null || cell.size() == 0) {
                                    html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px;vertical-align:top;padding-top: 5px").append(";background-color:#F0F0F0;\">").append(this.getEmptyCell(xEntry.getKey(), yKey, "DataSet", 2)).append("</td>");
                                    continue;
                                }
                                html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px;background-color:white;vertical-align:top;padding-top: 5px").append(";white-space:nowrap;\" nowrap>").append(this.getCell(xEntry.getKey(), yKey, cell, "DataSet", 2)).append("</td>");
                            }
                        }
                        if (this.endRow && !this.viewonly && !hideChildSDIDataAttributes && childSDIDatasetFound) {
                            html.append("<td class=\"maintform_field\" style=\"").append("min-width:35px").append(";white-space:nowrap;" + this.rightBorderStyle + "\" nowrap>").append(this.getEndColumn(yKey, "DataSet")).append("</td>");
                        }
                        html.append("</tr>");
                    }
                }
            } else {
                childSDIWI.sort(this.getSDIWISortBy());
                int trCnt = 0;
                for (int i = 0; i < thischildSDIWIRowCnt; ++i) {
                    int wicolCnt;
                    int thisWIDataSetRowcnt;
                    String borderBottom;
                    DataSet thisWIDataSet;
                    ArrayList<Boolean> childxArray = new ArrayList<Boolean>();
                    String sdiworkitemid = childSDIWI.getValue(i, "sdiworkitemid");
                    if (sdiworkitemid.length() == 0 && hideChildSDIDataAttributes && hideChildSDIWorkItemAttributes) continue;
                    yKey = sdiworkitemid + ";(null);(null)";
                    String sdc_id = childSDIWI.getString(i, "sdcid");
                    String sourceWID = childSDIWI.getString(i, "workitemid");
                    String sourceWIVersionID = childSDIWI.getString(i, "workitemversionid");
                    BigDecimal sourceWIinstance = childSDIWI.getBigDecimal(i, "workiteminstance");
                    HashMap<String, Object> findDataset = new HashMap<String, Object>();
                    findDataset.put("sourceworkitemid", sourceWID);
                    findDataset.put("sourceworkiteminstance", sourceWIinstance);
                    DataSet dataSet = thisWIDataSet = hideChildSDIDataAttributes ? new DataSet() : childSDIData.getFilteredDataSet(findDataset);
                    if (thisWIDataSet.getRowCount() == 0 && hideChildSDIWorkItemAttributes) continue;
                    ++trCnt;
                    if (thisWIDataSet.getRowCount() > 0) {
                        sdidataSortBy = this.getSDIDataSortBy();
                        thisWIDataSet.sort(sdidataSortBy);
                    }
                    if (trCnt > 1) {
                        if (styleClass.length() > 0) {
                            html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\" " : " ").append("class=\"").append(styleClass).append("\">");
                        } else {
                            html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\"" : "").append(">");
                        }
                        for (int c = 0; c < pcolCnt; ++c) {
                            String width = "100";
                            if (this.endRow && !this.viewonly && c == pcolCnt - 1) {
                                width = "30";
                                html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;border-top:none;border-bottom:none;" + this.rightBorderStyle + "\">").append("&nbsp;").append("</td>");
                                continue;
                            }
                            html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;background-color:").append((Boolean)xArray.get(c) != false ? "white" : "#F0F0F0").append(";border-top:none;border-bottom:none\">").append("&nbsp;").append("</td>");
                        }
                    }
                    String string = borderBottom = (thisWIDataSetRowcnt = thisWIDataSet.getRowCount()) > 1 ? "border-bottom:none" : "";
                    if (this.childx.size() > 0) {
                        for (Map.Entry<String, String> xEntry : this.childx.entrySet()) {
                            Map cell;
                            String key = xEntry.getKey() + ";" + yKey;
                            Map map = cell = this.childcells.containsKey(key) ? this.childcells.get(key) : null;
                            if (cell == null || cell.size() == 0) {
                                childxArray.add(false);
                                html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px").append(";background-color:#F0F0F0;vertical-align:top;padding-top: 5px;").append(borderBottom + ";").append("\">").append(this.getEmptyCell(xEntry.getKey(), yKey, "SDIWorkItem", 1)).append("</td>");
                                continue;
                            }
                            html.append("<td  class=\"maintform_field\" style=\"").append("min-width:100px;background-color:white;").append(";white-space:nowrap;vertical-align:top;padding-top: 5px;").append(borderBottom).append(";\" nowrap>").append(this.getCell(xEntry.getKey(), yKey, cell, "SDIWorkItem", 1)).append("</td>");
                            childxArray.add(true);
                        }
                    }
                    if (this.endRow && !this.viewonly && !hideChildSDIWorkItemAttributes && childSDIWIFound) {
                        String backgroundColor = sdiworkitemid.length() == 0 ? "background-color:#F0F0F0" : "";
                        boolean showOnlyCurrentWorkItemAttributes = "LV_RequestItem".equalsIgnoreCase(sdc_id) || "Product".equalsIgnoreCase(sdc_id) || "SamplePoint".equalsIgnoreCase(sdc_id) || "LV_RequestItem".equalsIgnoreCase(sdc_id) || "Location".equalsIgnoreCase(sdc_id) || "Study".equalsIgnoreCase(sdc_id) || "LV_ProductStage".equalsIgnoreCase(sdc_id);
                        html.append("<td  class=\"maintform_field\" style=\"").append("min-width:35px;").append("").append(";white-space:nowrap;").append(backgroundColor).append(";" + borderBottom + ";" + this.rightBorderStyle + "\"").append(" nowrap>").append(sdiworkitemid.length() > 0 ? this.getEndColumn(yKey, "SDIWorkItem", showOnlyCurrentWorkItemAttributes ? sourceWID : "", showOnlyCurrentWorkItemAttributes ? sourceWIVersionID : "") : "&nbsp;").append("</td>");
                    }
                    int n2 = wicolCnt = this.viewonly ? childxArray.size() : childxArray.size() + 1;
                    if (thisWIDataSet.getRowCount() == 0) {
                        if (this.getChildSDIDataDataSet().getRowCount() > 0) {
                            for (Map.Entry<String, String> xEntry : this.subchildx.entrySet()) {
                                html.append("<td class=\"maintform_field\" style=\"background-color:#F0F0F0\">").append("&nbsp;").append("</td>");
                            }
                            if (this.endRow && !this.viewonly && !hideChildSDIDataAttributes) {
                                html.append("<td class=\"maintform_field\" style=\"background-color:#F0F0F0\"").append("min-width:35px").append(";border-top:none;white-space:nowrap;" + this.rightBorderStyle + "\" nowrap>").append("&nbsp;").append("</td>");
                            }
                        }
                        html.append("</tr>");
                        continue;
                    }
                    for (int j = 0; j < thisWIDataSet.getRowCount(); ++j) {
                        String sdidataid = thisWIDataSet.getValue(j, "sdidataid");
                        yKey = sdidataid + ";(null);(null)";
                        if (j > 0) {
                            String width;
                            int c;
                            if (styleClass.length() > 0) {
                                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\" " : " ").append("class=\"").append(styleClass).append("\">");
                            } else {
                                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\"" : "").append(">");
                            }
                            for (c = 0; c < pcolCnt; ++c) {
                                width = "100";
                                if (this.endRow && !this.viewonly && c == pcolCnt - 1) {
                                    width = "30";
                                    html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;border-top:none;border-bottom:none;" + this.rightBorderStyle + "\">").append("&nbsp;").append("</td>");
                                    continue;
                                }
                                html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;background-color:").append((Boolean)xArray.get(c) != false ? "white" : "#F0F0F0").append(";border-top:none;border-bottom:none\">").append("&nbsp;").append("</td>");
                            }
                            if (!hideChildSDIWorkItemAttributes && childSDIWIFound) {
                                for (c = 0; c < wicolCnt; ++c) {
                                    width = "100";
                                    if (this.endRow && !this.viewonly && c == wicolCnt - 1) {
                                        width = "30";
                                        String backGroundColor = sdiworkitemid.length() == 0 ? "background-color:#F0F0F0;" : "";
                                        String bottomborder = sdiworkitemid.length() == 0 ? "" : "border-bottom:none;";
                                        html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;border-top:none;" + bottomborder + backGroundColor + ";" + this.rightBorderStyle + "\">").append("&nbsp;").append("</td>");
                                        continue;
                                    }
                                    html.append("<td  class=\"maintform_field\" style=\"").append("min-width:" + width + "px;background-color:").append((Boolean)childxArray.get(c) != false ? "white" : "#F0F0F0").append(";border-top:none;border-bottom:none\">").append("&nbsp;").append("</td>");
                                }
                            }
                        }
                        if (this.subchildx.size() > 0) {
                            for (Map.Entry<String, String> xEntry : this.subchildx.entrySet()) {
                                Map cell;
                                String key = xEntry.getKey() + ";" + yKey;
                                Map map = cell = this.subchildcells.containsKey(key) ? this.subchildcells.get(key) : null;
                                if (cell == null || cell.size() == 0) {
                                    html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px").append(";background-color:#F0F0F0;vertical-align:top;padding-top: 5px\">").append(this.getEmptyCell(xEntry.getKey(), yKey, "DataSet", 2)).append("</td>");
                                    continue;
                                }
                                html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px;background-color:white;").append(";white-space:nowrap;vertical-align:top;padding-top: 5px\" nowrap>").append(this.getCell(xEntry.getKey(), yKey, cell, "DataSet", 2)).append("</td>");
                            }
                        }
                        if (this.endRow && !this.viewonly && !hideChildSDIDataAttributes && childSDIDatasetFound) {
                            String backgroundColor = sdidataid.length() == 0 ? "background-color:#F0F0F0" : "";
                            html.append("<td class=\"maintform_field\" style=\"").append("min-width:35px").append(";white-space:nowrap;").append(backgroundColor).append(";" + this.rightBorderStyle + "\" nowrap>").append(sdidataid.length() > 0 ? this.getEndColumn(yKey, "DataSet") : "&nbsp;").append("</td>");
                        }
                        html.append("</tr>");
                    }
                }
                if (trCnt == 0) {
                    html.append("</tr>");
                }
            }
        }
    }

    private void renderGridRow(StringBuffer html, String yKey, String yTitle, String styleClass, boolean renderYHeader) {
        if (yKey != null) {
            if (styleClass.length() > 0) {
                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\" " : " ").append("class=\"").append(styleClass).append("\">");
            } else {
                html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px;\"" : "").append(">");
            }
            if (renderYHeader) {
                this.renderYHeader(html, yKey, yTitle);
            }
            for (Map.Entry<String, String> xEntry : this.x.entrySet()) {
                Map cell;
                String key = xEntry.getKey() + ";" + yKey;
                Map map = cell = this.cells.containsKey(key) ? this.cells.get(key) : null;
                if (cell == null || cell.size() == 0) {
                    html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px").append(";background-color:#F0F0F0;\">").append(this.getEmptyCell(xEntry.getKey(), yKey)).append("</td>");
                    continue;
                }
                html.append("<td class=\"maintform_field\" style=\"").append("min-width:100px;background-color:white;").append(";white-space:nowrap;\" nowrap>").append(this.getCell(xEntry.getKey(), yKey, cell)).append("</td>");
            }
            if (this.endRow) {
                html.append("<td class=\"maintform_field\" style=\"").append("min-width:50px").append(";white-space:nowrap;\" nowrap>").append(this.getEndColumn(yKey)).append("</td>");
            }
            html.append("</tr>");
        }
    }

    private void renderCornerHead(StringBuffer html, String id) {
        html.append("<th ").append(id.length() > 0 ? "id=\"" + id + "\" " : "").append("class=\"gridmaint_fieldtitle\" style=\"").append(this.scrollGrid ? "width:100px;border-bottom:0px;" : "").append("text-align:left;\">").append(this.getTranslationProcessor().translate(this.getCornerHeader())).append("</th>");
    }

    protected void renderAdvancedScrollGridSDIAcrossHeader(StringBuffer html, String id) {
    }

    public int renderAdvancedScrollGridHeaderCols(StringBuffer html, String id) {
        return 1;
    }

    public void renderAdvancedScrollGridYHeader(StringBuffer html, String styleclass, int totalDispCols) {
    }

    public DataSet sortAdvancedScrollGridRows() {
        return new DataSet();
    }

    protected DataSet getThisSDIChildSDIWIRows(String key) {
        return new DataSet();
    }

    protected DataSet getChildSDIWIDataSet() {
        return new DataSet();
    }

    protected DataSet getThisSDIChildSDIDataRows(String key, DataSet dsCurrentPrimaryWI) {
        return new DataSet();
    }

    protected boolean hideSDIWorkItemAttributes() {
        return false;
    }

    protected boolean hideSDIDataAttributes() {
        return false;
    }

    protected String getHeaderText(String propertyid) {
        return "";
    }

    protected DataSet getChildSDIDataDataSet() {
        return new DataSet();
    }

    private void renderAdvancedScrollGridHeader(StringBuffer html, HashMap colCount) {
        int c = 0;
        for (Map.Entry<String, String> xEntry : this.x.entrySet()) {
            ++c;
            html.append("<th class=\"maintform_fieldtitle\"").append("").append(" style=\"").append("min-width:100px").append(";word-wrap:break-word\">").append(this.getXHeader(xEntry.getKey(), xEntry.getValue())).append("</th>");
        }
        if (this.endRow && !this.viewonly) {
            ++c;
            html.append("<th class=\"maintform_fieldtitle\"").append("").append(" style=\"").append("min-width:35px;" + this.rightBorderStyle + ";\">").append("&nbsp;").append("</th>");
        }
        colCount.put("attributesdc", c);
        this.renderChildSDIHeader(html, colCount);
        html.append("</tr>");
    }

    protected void renderChildSDIHeader(StringBuffer html, HashMap colCount) {
        boolean hideChildSDIWorkItemAttributes = this.hideSDIWorkItemAttributes();
        boolean hideChildSDIDataAttributes = this.hideSDIDataAttributes();
        boolean childSDIWIFound = this.getChildSDIWIDataSet().getRowCount() > 0;
        int attribCnt = 0;
        int colIndex = 0;
        for (Map.Entry<String, String> xEntry : this.childx.entrySet()) {
            ++attribCnt;
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:100px").append(";overflow-wrap:anywhere;\">").append(this.getChildSDIXHeader(xEntry.getKey(), xEntry.getValue(), "SDIWorkItem", colIndex)).append("</th>");
            if (this.isReadOnlyInformationOnly(xEntry.getKey())) continue;
            ++colIndex;
        }
        if (!this.viewonly && !hideChildSDIWorkItemAttributes && this.endRow && (this.childx.size() > 0 || childSDIWIFound)) {
            ++attribCnt;
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:35px;" + this.rightBorderStyle).append(";\">").append("&nbsp;").append("</th>");
        }
        colCount.put("child1", attribCnt);
        attribCnt = 0;
        boolean childSDIDataFound = this.getChildSDIDataDataSet().getRowCount() > 0;
        colIndex = 0;
        for (Map.Entry<String, String> xEntry : this.subchildx.entrySet()) {
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:100px").append(";overflow-wrap:anywhere;\">").append(this.getChildSDIXHeader(xEntry.getKey(), xEntry.getValue(), "DataSet", colIndex)).append("</th>");
            if (!this.isReadOnlyInformationOnly(xEntry.getKey())) {
                ++colIndex;
            }
            ++attribCnt;
        }
        if (!this.viewonly && !hideChildSDIDataAttributes && this.endRow && (this.subchildx.size() > 0 || childSDIDataFound)) {
            ++attribCnt;
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:35px;" + this.rightBorderStyle + ";\">").append("&nbsp;").append("</th>");
        }
        colCount.put("child2", attribCnt);
    }

    protected void renderAdvancedScrollGridSDIAcrossHeaderColumns(StringBuffer html, DataSet ds, String colId, String parent) {
        for (Map.Entry<String, String> xEntry : this.x.entrySet()) {
            if (parent.equalsIgnoreCase("sdiworkitem")) {
                html.append("<td class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:100px").append(";word-wrap:break-word;\">").append(this.getSDIAcrossModeParentWorkItemColumnValue(xEntry.getKey(), xEntry.getValue(), colId, ds)).append("</td>");
                continue;
            }
            if (parent.length() == 0) {
                html.append("<td class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:100px").append(";word-wrap:break-word;\">").append(this.getSDIAcrossModeColumnValue(xEntry.getKey(), xEntry.getValue(), colId, ds)).append("</td>");
                continue;
            }
            html.append("<td class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:100px").append(";word-wrap:break-word;\">").append(this.getSDIAcrossModeParentColumnValue(xEntry.getKey(), xEntry.getValue(), colId, ds)).append("</td>");
        }
    }

    private void renderHeader(StringBuffer html, boolean renderCorner) {
        html.append("<tr").append(this.scrollGrid ? " style=\"height:" + this.rowHeight + "px\"" : "").append(">");
        if (renderCorner) {
            this.renderCornerHead(html, "");
        }
        for (Map.Entry<String, String> xEntry : this.x.entrySet()) {
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:80px").append(";overflow-wrap:anywhere;\">").append(this.getXHeader(xEntry.getKey(), xEntry.getValue())).append("</th>");
        }
        if (this.endRow) {
            html.append("<th class=\"maintform_fieldtitle\"").append("").append("style=\"").append("min-width:50px").append(";\">").append("&nbsp;").append("</th>");
        }
        html.append("</tr>");
    }

    private void renderNormalGrid(StringBuffer html) {
        html.append("<table class=\"maintform_table\" border=\"0\" id=\"").append(this.elementid).append("_maintable\">");
        html.append("<thead>");
        this.renderHeader(html, true);
        html.append("</thead>");
        if (this.grouping.size() == 0) {
            html.append("<tbody>");
            for (Map.Entry<String, String> entry : this.y.entrySet()) {
                this.renderGridRow(html, entry.getKey(), entry.getValue());
            }
            html.append("</tbody>");
        } else {
            StringBuffer style = new StringBuffer();
            for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
                String groupid = groupEntry.getKey();
                html.append("<tbody>");
                boolean def = groupid.equalsIgnoreCase(DEFAULT_GROUP);
                String gid = StringUtil.replaceAll(groupid, " ", "_").trim();
                String styleclass = "multiattributemaint_" + gid;
                String click = "";
                if (this.groupClick.length() > 0) {
                    click = this.groupClick + "(this,'" + styleclass + "');";
                }
                if (!def || this.grouping.size() != 1) {
                    html.append("<tr>");
                    html.append("<td id=\"").append(this.elementid).append("_groupcell_").append(gid).append("\" colspan=\"").append(this.x.size() + 1).append("\" class=\"list_grouptitle\" style=\"padding-left:2px;");
                    style.append("tr.").append(styleclass).append("{display:").append(!this.browser.isIE() && this.collapseGroups ? "none" : "table-row").append(";}\n");
                    if (click.length() > 0) {
                        html.append("cursor:pointer;\" onclick=\"").append(click).append("\">");
                        html.append("<img src=\"WEB-CORE/pagetypes/list/images/").append(!this.browser.isIE() && this.collapseGroups ? "plus.gif" : "minus.gif").append("\">");
                        html.append("&nbsp;");
                    } else {
                        html.append("\">");
                    }
                    html.append("&nbsp;");
                    html.append(def ? this.getTranslationProcessor().translate("No Grouping") : groupid);
                    html.append("</td>");
                    html.append("</tr>");
                }
                if (groupEntry.getValue() != null) {
                    ArrayList<String> groupEntries = groupEntry.getValue();
                    Collections.sort(groupEntries, this.comparatorGroupEntry);
                    for (int i = 0; i < groupEntries.size(); ++i) {
                        String yKey = groupEntries.get(i);
                        if (yKey == null) continue;
                        String yValue = this.y.get(yKey);
                        this.renderGridRow(html, yKey, yValue, styleclass, true);
                    }
                }
                html.append("</tbody>");
            }
            if (style.length() > 0) {
                html.append("<style id=\"").append("__").append(this.elementid).append("_style").append("\">");
                html.append(style);
                html.append("</style>");
            }
        }
        if (this.endColumn) {
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<th class=\"maintform_fieldtitle\">").append(this.getYHeaderEndColumn()).append("</th>");
            for (Map.Entry<String, String> entry : this.x.entrySet()) {
                String xKey = entry.getKey();
                html.append("<th class=\"maintform_fieldtitle\"").append(this.browser.isIE() ? " align=\"center\" " : " ").append(">").append(this.getEndColumn(xKey)).append("</th>");
            }
            html.append("</tr>");
            html.append("</tbody>");
        }
        html.append("</table>");
    }

    private void renderScrollGrid(StringBuffer html) {
        String yValue;
        String yKey;
        int i;
        int l;
        String click;
        String styleclass;
        String gid;
        boolean def;
        String groupid;
        Object style;
        html.append("<table class=\"maintform_table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"").append(this.elementid).append("_maintable\">");
        html.append("<tbody>");
        String sdcId = this.getSDCId();
        this.renderAdvancedScrollGridSDIAcrossHeader(html, this.elementid + "_firstTd");
        html.append("<tr>");
        html.append("<td valign=\"top\">");
        html.append("<div id=\"" + this.elementid + "_firstcol\" style=\"overflow:hidden;height:80px;min-width:100px;white-space:nowrap\">");
        html.append("<table class=\"maintform_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%;\">");
        if (this.grouping.size() == 0) {
            html.append("<tbody>");
            for (Map.Entry<String, String> entry : this.y.entrySet()) {
                this.renderGridRow(html, entry.getKey(), entry.getValue());
            }
            html.append("</tbody>");
        } else {
            style = new StringBuffer();
            for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
                groupid = groupEntry.getKey();
                html.append("<tbody>");
                def = groupid.equalsIgnoreCase(DEFAULT_GROUP);
                gid = StringUtil.replaceAll(groupid, " ", "_").trim();
                styleclass = "multiattributemaint_" + gid;
                click = "";
                if (this.groupClick.length() > 0) {
                    click = this.groupClick + "(this,'" + styleclass + "');";
                }
                if (!def || this.grouping.size() != 1) {
                    boolean expanded = false;
                    if (this.expandedGroupsArray != null && this.expandedGroupsArray.length > 0) {
                        for (l = 0; l < this.expandedGroupsArray.length; ++l) {
                            if (!this.expandedGroupsArray[l].equalsIgnoreCase(this.elementid + "_groupcell_" + gid)) continue;
                            expanded = true;
                            break;
                        }
                    }
                    html.append("<tr>");
                    html.append("<td id=\"").append(this.elementid).append("_groupcell_").append(gid).append("\" nowrap colspan=\"").append(this.x.size() + 1).append("\" class=\"list_grouptitle\" style=\"padding-left:2px;");
                    ((StringBuffer)style).append("tr.").append(styleclass).append("{display:").append(!this.browser.isIE() && this.collapseGroups && !expanded ? "none" : "table-row").append(";}\n");
                    if (click.length() > 0) {
                        html.append("cursor:pointer;\" onclick=\"").append(click).append("\">");
                        html.append("<img src=\"WEB-CORE/pagetypes/list/images/").append(!this.browser.isIE() && this.collapseGroups && !expanded ? "plus.gif" : "minus.gif").append("\">");
                        html.append("&nbsp;");
                    } else {
                        html.append("\">");
                    }
                    html.append("&nbsp;");
                    html.append(def ? this.getTranslationProcessor().translate("No Grouping") : groupid);
                    html.append("</td>");
                    html.append("</tr>");
                }
                if (groupEntry.getValue() != null) {
                    ArrayList<String> groupEntries = groupEntry.getValue();
                    Collections.sort(groupEntries, this.comparatorGroupEntry);
                    for (i = 0; i < groupEntries.size(); ++i) {
                        yKey = groupEntries.get(i);
                        if (yKey == null) continue;
                        yValue = this.y.get(yKey);
                        String heightN = "height";
                        if (this.browser.isIE()) {
                            heightN = "min-height";
                        }
                        if (styleclass.length() > 0) {
                            html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\" class=\"").append(styleclass).append("\">");
                        } else {
                            html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\">");
                        }
                        this.renderYHeader(html, yKey, yValue);
                        html.append("</tr>");
                    }
                }
                html.append("</tbody>");
            }
        }
        if (this.endColumn) {
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<th class=\"maintform_fieldtitle\" style=\"height:" + this.rowHeight + "px;\">").append(this.getYHeaderEndColumn()).append("</th>");
            html.append("</tr>");
            html.append("</tbody>");
        }
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("<td valign=\"top\">");
        html.append("<div id=\"" + this.elementid + "_tablediv\" style=\"overflow:scroll;width:100px;height:100px;").append(this.browser.isIE() ? "" : "position:relative").append(";\" onscroll=\"").append(JS_CLASS).append(".scrollGrid.scroll();").append("\">");
        html.append("<table class=\"maintform_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        if (this.grouping.size() == 0) {
            html.append("<tbody>");
            for (Map.Entry entry : this.y.entrySet()) {
                this.renderGridRow(html, (String)entry.getKey(), (String)entry.getValue());
            }
            html.append("</tbody>");
        } else {
            style = new StringBuffer();
            for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
                groupid = groupEntry.getKey();
                html.append("<tbody>");
                def = groupid.equalsIgnoreCase(DEFAULT_GROUP);
                gid = StringUtil.replaceAll(groupid, " ", "_").trim();
                styleclass = "multiattributemaint_" + gid;
                click = "";
                if (this.groupClick.length() > 0) {
                    click = this.groupClick + "(" + this.elementid + "_groupcell_" + gid + ",'" + styleclass + "');";
                }
                if (!def || this.grouping.size() != 1) {
                    boolean expanded = false;
                    if (this.expandedGroupsArray != null && this.expandedGroupsArray.length > 0) {
                        for (l = 0; l < this.expandedGroupsArray.length; ++l) {
                            if (!this.expandedGroupsArray[l].equalsIgnoreCase(this.elementid + "_groupcell_" + gid)) continue;
                            expanded = true;
                            break;
                        }
                    }
                    html.append("<tr>");
                    html.append("<td colspan=\"").append(this.x.size() + 1).append("\" class=\"list_grouptitle\" style=\"padding-left:2px;");
                    ((StringBuffer)style).append("tr.").append(styleclass).append("{display:").append(!this.browser.isIE() && this.collapseGroups && !expanded ? "none" : "table-row").append(";}\n");
                    if (click.length() > 0) {
                        html.append("cursor:pointer;\" onclick=\"").append(click).append("\">");
                        html.append("&nbsp;");
                    } else {
                        html.append("\">");
                    }
                    html.append("&nbsp;");
                    html.append("</td>");
                    html.append("</tr>");
                }
                if (groupEntry.getValue() != null) {
                    ArrayList<String> groupEntries = groupEntry.getValue();
                    Collections.sort(groupEntries, this.comparatorGroupEntry);
                    for (i = 0; i < groupEntries.size(); ++i) {
                        yKey = groupEntries.get(i);
                        if (yKey == null) continue;
                        yValue = this.y.get(yKey);
                        this.renderGridRow(html, yKey, yValue, styleclass, false);
                    }
                }
                html.append("</tbody>");
            }
            if (((StringBuffer)style).length() > 0) {
                html.append("<style id=\"").append("__").append(this.elementid).append("_style").append("\">");
                html.append((StringBuffer)style);
                html.append("</style>");
            }
        }
        if (this.endColumn) {
            html.append("<tbody>");
            html.append("<tr>");
            for (Map.Entry<String, String> entry : this.x.entrySet()) {
                html.append("<th class=\"maintform_fieldtitle\" ").append("").append(" style=\"").append("min-width:100px").append(";height:" + this.rowHeight + "px;\">").append(this.getEndColumn(entry.getKey())).append("</th>");
            }
            html.append("</tr>");
            html.append("</tbody>");
        }
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
    }

    private void renderAdvancedScrollGrid(StringBuffer html) {
        ArrayList<String> groupEntries;
        String click;
        String styleclass;
        String gid;
        boolean def;
        String groupid;
        Object style;
        Object text;
        html.append("<table class=\"maintform_table\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=red border=\"0\" id=\"").append(this.elementid).append("_maintable\">");
        html.append("<tbody>");
        html.append("<tr style=\"height:" + this.rowHeight + "px;\">");
        html.append("<td width=\"auto\" valign=top>");
        html.append("<div id=\"ycolheader1_div\" style=\"overflow:hidden;\">");
        html.append("<table id=\"ycolheader1\" class=\"maintform_table\" border=1  cellspacing=0 cellpadding=0 >");
        StringBuffer colHeaderHtml = new StringBuffer();
        colHeaderHtml.append("<tr style=\"max-height: 20px;\">");
        int displayColCnt = this.renderAdvancedScrollGridHeaderCols(colHeaderHtml, this.elementid + "_firstTd");
        colHeaderHtml.append("</tr>");
        SDCProcessor sdcProc = this.getSDCProcessor();
        String colHeaderText = this.getHeaderText("columnheader");
        colHeaderText = StringUtil.replaceAll(colHeaderText, "[sdcid]", this.getSDCId());
        colHeaderText = StringUtil.replaceAll(colHeaderText, "[parentsdcid]", this.getParentSDCId());
        colHeaderText = StringUtil.replaceAll(colHeaderText, "[primarysdcid]", this.getParentSDCId());
        html.append("<tr style=\"height: 10px;\"><th class=\"gridmaint_fieldtitle1\" style=\"border:2px solid #696969\" colspan =" + displayColCnt + ">" + colHeaderText + "</th>");
        html.append("</tr>");
        html.append(colHeaderHtml);
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("<td width=\"*\" valign=top >");
        html.append("<div id=\"").append(this.elementid).append("_divHeader\" style=\"overflow:hidden;width:284px;padding-right:0px\">");
        html.append("<table  id=\"attributeheader\" class=\"maintform_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" ").append(" style=\"width:100%;height:100%\"").append(">");
        StringBuffer attrHeadHtml = new StringBuffer();
        attrHeadHtml.append("<tr").append(" style=\"max-height:20px\"").append(">");
        HashMap<String, Integer> colCountMap = new HashMap<String, Integer>();
        colCountMap.put("attributesdc", 0);
        colCountMap.put("child1", 0);
        colCountMap.put("child2", 0);
        this.renderAdvancedScrollGridHeader(attrHeadHtml, colCountMap);
        html.append("<tr style=\"height: 10px;\">");
        int attrSDCColCnt = (Integer)colCountMap.get("attributesdc");
        int child1ColCnt = (Integer)colCountMap.get("child1");
        int child2ColCnt = (Integer)colCountMap.get("child2");
        String attributeHeaderText = this.getHeaderText("sdcattributeheader");
        attributeHeaderText = StringUtil.replaceAll(attributeHeaderText, "[sdcid]", this.getSDCId());
        attributeHeaderText = StringUtil.replaceAll(attributeHeaderText, "[parentsdcid]", this.getParentSDCId());
        attributeHeaderText = StringUtil.replaceAll(attributeHeaderText, "[primarydcid]", this.getParentSDCId());
        String childWorkItemAttributeText = this.getHeaderText("childworkitemattributeheader");
        String childDataSetAttributeText = this.getHeaderText("childdatasetattributeheader");
        String sdcid = this.getSDCId();
        html.append("<th class=\"gridmaint_fieldtitle1\" style=\"border:2px solid #696969\" colspan =" + attrSDCColCnt + ">" + attributeHeaderText + "</th>");
        if (child1ColCnt > 0) {
            text = "";
            if (sdcid != "SDIWorkItem" && sdcid != "DataSet") {
                text = childWorkItemAttributeText;
            } else if (sdcid != "SDIWorkItem") {
                text = childDataSetAttributeText;
            }
            html.append("<th class=\"gridmaint_fieldtitle1\" style=\"border:2px solid #696969\" colspan =" + child1ColCnt + ">" + (String)text + "</th>");
        }
        if (child2ColCnt > 0) {
            text = "";
            if (sdcid != "SDIWorkItem") {
                text = childDataSetAttributeText;
            }
            html.append("<th class=\"gridmaint_fieldtitle1\" style=\"border:2px solid #696969\" colspan =" + child2ColCnt + ">" + (String)text + "</th>");
        }
        html.append("</tr>");
        html.append(attrHeadHtml);
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td  width=auto valign=\"top\">");
        html.append("<div id=\"" + this.elementid + "_firstcol\" style=\"overflow:hidden;\">");
        html.append("<table id=\"ycolheader2\" class=\"maintform_table\" border=0 cellspacing=0 cellpadding=0>");
        if (this.grouping.size() == 0) {
            html.append("<tbody>");
            for (Map.Entry entry : this.y.entrySet()) {
                this.renderGridRow(html, (String)entry.getKey(), (String)entry.getValue());
            }
            html.append("</tbody>");
        } else {
            style = new StringBuffer();
            for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
                groupid = groupEntry.getKey();
                html.append("<tbody>");
                def = groupid.equalsIgnoreCase(DEFAULT_GROUP);
                gid = StringUtil.replaceAll(groupid, " ", "_").trim();
                styleclass = "multiattributemaint_" + gid;
                click = "";
                if (this.groupClick.length() > 0) {
                    click = this.groupClick + "(this,'" + styleclass + "');";
                }
                if (!def || this.grouping.size() != 1) {
                    html.append("<tr>");
                    html.append("<td id=\"").append(this.elementid).append("_groupcell_").append(gid).append("\" nowrap colspan=\"").append(this.x.size() + 1).append("\" class=\"list_grouptitle\" style=\"padding-left:2px;");
                    ((StringBuffer)style).append("tr.").append(styleclass).append("{display:").append(!this.browser.isIE() && this.collapseGroups ? "none" : "table-row").append(";}\n");
                    if (click.length() > 0) {
                        html.append("cursor:pointer;\" onclick=\"").append(click).append("\">");
                        html.append("<img src=\"WEB-CORE/pagetypes/list/images/").append(!this.browser.isIE() && this.collapseGroups ? "plus.gif" : "minus.gif").append("\">");
                        html.append("&nbsp;");
                    } else {
                        html.append("\">");
                    }
                    html.append("&nbsp;");
                    html.append(def ? this.getTranslationProcessor().translate("No Grouping") : groupid);
                    html.append("</td>");
                    html.append("</tr>");
                }
                if (groupEntry.getValue() != null) {
                    groupEntries = groupEntry.getValue();
                    Collections.sort(groupEntries, this.comparatorGroupEntry);
                    this.renderAdvancedScrollGridYHeader(html, styleclass, displayColCnt);
                }
                html.append("</tbody>");
            }
        }
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("<td valign=\"top\">");
        html.append("<div id=\"" + this.elementid + "_tablediv\" style=\"overflow:scroll;  overflow-y:visible;width:99%;height:99%;").append(this.browser.isIE() ? "" : "position:relative").append(";\" onscroll=\"").append(JS_CLASS).append(".scrollGrid.scroll();").append("\">");
        html.append("<table class=\"maintform_table\" border=\"0\" bordercolor=red cellpadding=\"0\" cellspacing=\"0\">");
        if (this.grouping.size() == 0) {
            html.append("<tbody>");
            for (Map.Entry entry : this.y.entrySet()) {
                this.renderGridRow(html, (String)entry.getKey(), (String)entry.getValue());
            }
            html.append("</tbody>");
        } else {
            style = new StringBuffer();
            for (Map.Entry<String, ArrayList<String>> groupEntry : this.grouping.entrySet()) {
                groupid = groupEntry.getKey();
                html.append("<tbody>");
                def = groupid.equalsIgnoreCase(DEFAULT_GROUP);
                gid = StringUtil.replaceAll(groupid, " ", "_").trim();
                styleclass = "multiattributemaint_" + gid;
                click = "";
                if (this.groupClick.length() > 0) {
                    click = this.groupClick + "(" + this.elementid + "_groupcell_" + gid + ",'" + styleclass + "');";
                }
                if (!def || this.grouping.size() != 1) {
                    html.append("<tr>");
                    html.append("<td colspan=\"").append(this.x.size() + 1).append("\" class=\"list_grouptitle\" style=\"padding-left:2px;");
                    ((StringBuffer)style).append("tr.").append(styleclass).append("{display:").append(!this.browser.isIE() && this.collapseGroups ? "none" : "table-row").append(";}\n");
                    if (click.length() > 0) {
                        html.append("cursor:pointer;\" onclick=\"").append(click).append("\">");
                        html.append("&nbsp;");
                    } else {
                        html.append("\">");
                    }
                    html.append("&nbsp;");
                    html.append("</td>");
                    html.append("</tr>");
                }
                if (groupEntry.getValue() != null) {
                    groupEntries = groupEntry.getValue();
                    Collections.sort(groupEntries, this.comparatorGroupEntry);
                    DataSet sorted = this.sortAdvancedScrollGridRows();
                    int gridRowCnt = 1;
                    if (sorted.getRowCount() == 0) {
                        sorted.addRow();
                        this.setError(this.getTranslationProcessor().translate("No rows to display!"));
                    }
                    block4: for (int k = 0; k < sorted.getRowCount(); ++k) {
                        String __key = sorted.getValue(k, "__key");
                        for (int i = 0; i < groupEntries.size(); ++i) {
                            int totalWIRowcnt;
                            String yKey = groupEntries.get(i);
                            if (yKey == null) continue;
                            String yValue = this.y.get(yKey);
                            if (!__key.equals(yKey)) continue;
                            DataSet childSDIWIDS = this.getThisSDIChildSDIWIRows(yKey);
                            DataSet childSDIDataDS = this.getThisSDIChildSDIDataRows(yKey, childSDIWIDS);
                            int totalDSRowcnt = this.getChildSDIDataDataSet().getRowCount();
                            int toRowCnt = totalDSRowcnt > (totalWIRowcnt = this.getChildSDIWIDataSet().getRowCount()) ? totalDSRowcnt : totalWIRowcnt;
                            this.renderAdvancedScrollGridRow(html, yKey, yValue, styleclass, false, childSDIWIDS, childSDIDataDS, gridRowCnt, toRowCnt, sorted);
                            ++gridRowCnt;
                            continue block4;
                        }
                    }
                }
                html.append("</tbody>");
            }
            if (((StringBuffer)style).length() > 0) {
                html.append("<style id=\"").append("__").append(this.elementid).append("_style").append("\">");
                html.append((StringBuffer)style);
                html.append("</style>");
            }
        }
        if (this.endColumn) {
            html.append("<tbody>");
            html.append("<tr>");
            for (Map.Entry<String, String> entry : this.x.entrySet()) {
                html.append("<th class=\"maintform_fieldtitle\" ").append("").append(" style=\"").append("min-width:100px").append(";height:" + this.rowHeight + "px;\">").append(this.getEndColumn(entry.getKey())).append("</th>");
            }
            html.append("</tr>");
            html.append("</tbody>");
        }
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
    }

    private StringBuffer getGrid() {
        StringBuffer html = new StringBuffer();
        if (this.error != null && this.error.length() > 0) {
            this.renderError(this.error, html);
        } else {
            html.append(this.getHeaderHTML());
            html.append("<div id=\"multiMaintGridDiv\" style=\"padding-left:10px; padding-top:10px;position:relative;\">");
            if (this.scrollGrid && this.isAttributeAcross()) {
                this.renderAdvancedScrollGrid(html);
            } else if (this.scrollGrid) {
                this.renderScrollGrid(html);
            } else {
                this.renderNormalGrid(html);
            }
            html.append("</div>");
            html.append(this.getFooterHTML());
        }
        return html;
    }

    @Override
    public String getHtml() {
        return this.getGrid().toString();
    }
}

