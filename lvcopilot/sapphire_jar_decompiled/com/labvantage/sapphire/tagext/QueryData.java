/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.DateTimeUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;

public class QueryData
extends AbstractList {
    static final String LABVANTAGE_CVS_ID = "$Revision: 62367 $";
    public static final int TEMPLATE_ROW = -9999;
    public static final int HEADER_ROW = -9998;
    private DataSet _data = null;
    private DataSet _templatedata = null;
    private String _datasetName = "primary";
    private int _currentrow = -1;
    private int _currentcol = -1;
    private String _nullvalue = "";
    private String[] _groupcol;
    private String[] _groupstring;
    private BigDecimal[] _groupnumber;
    private Calendar[] _groupdate;
    private boolean _grouping = false;
    private boolean _groupdefined = false;
    private boolean _incrementrow = true;
    private ArrayList grid;

    public void addGridRow() {
        if (this.grid == null) {
            this.grid = new ArrayList();
        }
        this.grid.add(new ArrayList());
    }

    public void addGridColumn(String id) {
        if (this.grid != null && this._currentrow != -9999) {
            ((ArrayList)this.grid.get(this._currentrow)).add(id);
        }
    }

    public ArrayList getGrid() {
        return this.grid;
    }

    @Override
    public Object get(int index) {
        return this._data.get(index);
    }

    @Override
    public int size() {
        return this._data.getRowCount();
    }

    public QueryData() {
    }

    public QueryData(DataSet data) {
        this._data = data;
    }

    public QueryData(String datasetName, DataSet data) {
        this._datasetName = datasetName;
        this._data = data;
    }

    public void setNullValue(String nullvalue) {
        this._nullvalue = nullvalue;
    }

    public String getNullValue() {
        return this._nullvalue;
    }

    public void setQueryData(DataSet querydata) {
        this._data = querydata;
    }

    public void setDatasetName(String datasetName) {
        this._datasetName = datasetName;
    }

    public String getDatasetName() {
        return this._datasetName;
    }

    public void setTemplateGenerate() {
        this._currentrow = -9999;
    }

    public void setHeaderGenerate() {
        this._currentrow = -9998;
    }

    public void setTemplateData(DataSet templatedata) {
        this._templatedata = templatedata;
    }

    public DataSet getQuerydata() {
        return this._data;
    }

    public String getRowStatus(int row) {
        String status = this._data.getString(row, "__rowstatus");
        return status != null ? status : "I";
    }

    public String getRowId(int row) {
        String rowid = this._data.getString(row, "__rowid");
        return rowid != null ? rowid : "[__row]";
    }

    public void resetGroup(String sort, String group) {
        if (sort != null && sort.length() > 0) {
            this._data.sort(sort);
        }
        this._groupdefined = group != null && group.length() > 0;
        this._groupcol = StringUtil.split(group, ",");
        this._groupstring = new String[this._groupcol.length];
        this._groupnumber = new BigDecimal[this._groupcol.length];
        this._groupdate = new Calendar[this._groupcol.length];
        block5: for (int i = 0; i < this._groupcol.length; ++i) {
            this._groupcol[i] = this._groupcol[i].trim();
            switch (this._data.getColumnType(this._groupcol[i])) {
                case 0: {
                    this._groupstring[i] = this._data.getString(0, this._groupcol[i]);
                    continue block5;
                }
                case 1: {
                    this._groupnumber[i] = this._data.getBigDecimal(0, this._groupcol[i]);
                    continue block5;
                }
                case 2: {
                    this._groupdate[i] = this._data.getCalendar(0, this._groupcol[i]);
                }
            }
        }
        this._grouping = true;
        this._incrementrow = false;
        this._currentrow = 0;
    }

    public boolean nextGroup(int endrow) {
        if (endrow < 0 || endrow > this._data.getRowCount()) {
            endrow = this._data.getRowCount();
        }
        ++this._currentrow;
        this._incrementrow = false;
        return this._currentrow < endrow;
    }

    public int getCurrentRow() {
        return this._currentrow;
    }

    public void setCurrentRow(int row) {
        this._currentrow = row;
    }

    public boolean isGrouping() {
        return this._grouping;
    }

    public boolean isGroupDefined() {
        return this._groupdefined;
    }

    public boolean isTemplateRow() {
        return this._currentrow == -9999;
    }

    public int getRowCount() {
        return this._data.getRowCount();
    }

    public void addRows(int rows) {
        for (int i = 0; i < rows; ++i) {
            int newrow = this._data.addRow();
            this._data.setString(newrow, "__rowstatus", "I");
            this._data.setString(newrow, "__rowid", String.valueOf(newrow));
        }
    }

    public void addRows(int rows, DataSet template) {
        int rowid = this._data.size();
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < template.size(); ++j) {
                template.setString(j, "__rowstatus", "I");
                template.setString(j, "__rowid", String.valueOf(rowid++));
            }
            this._data.copyRow(template, -1, 1);
        }
    }

    public void resetRow(int startrow) {
        if (!this._grouping) {
            if (startrow < 0) {
                startrow = -1;
            }
            if (startrow > this._data.getRowCount()) {
                startrow = this._data.getRowCount();
            }
            this._currentrow = startrow;
        }
    }

    public boolean nextRow(int endrow) {
        boolean rc = false;
        if (this._currentrow > -9999) {
            if (endrow < 0 || endrow > this._data.getRowCount()) {
                endrow = this._data.getRowCount();
            }
            this._currentcol = -1;
            if (this._incrementrow) {
                ++this._currentrow;
            }
            if (this._currentrow < endrow) {
                rc = true;
                if (this._grouping) {
                    this._incrementrow = true;
                    block5: for (int i = 0; i < this._groupcol.length; ++i) {
                        switch (this._data.getColumnType(this._groupcol[i])) {
                            case 0: {
                                String tempstr = this._data.getString(this._currentrow, this._groupcol[i]);
                                if (tempstr == null) {
                                    if (this._groupstring[i] == null) continue block5;
                                    this._groupstring[i] = null;
                                    rc = false;
                                    continue block5;
                                }
                                if (tempstr.equals(this._groupstring[i])) continue block5;
                                this._groupstring[i] = tempstr;
                                rc = false;
                                continue block5;
                            }
                            case 1: {
                                BigDecimal tempnum = this._data.getBigDecimal(this._currentrow, this._groupcol[i]);
                                if (tempnum == null) {
                                    if (this._groupnumber[i] == null) continue block5;
                                    this._groupnumber[i] = null;
                                    rc = false;
                                    continue block5;
                                }
                                if (tempnum.equals(this._groupnumber[i])) continue block5;
                                this._groupnumber[i] = tempnum;
                                rc = false;
                                continue block5;
                            }
                            case 2: {
                                Calendar tempcal = this._data.getCalendar(this._currentrow, this._groupcol[i]);
                                if (tempcal == null) {
                                    if (this._groupdate[i] == null) continue block5;
                                    this._groupdate[i] = null;
                                    rc = false;
                                    continue block5;
                                }
                                if (tempcal.equals(this._groupdate[i])) continue block5;
                                this._groupdate[i] = tempcal;
                                rc = false;
                            }
                        }
                    }
                    if (!rc) {
                        --this._currentrow;
                    }
                }
            }
        }
        return rc;
    }

    public int getCurrentCol() {
        return this._currentcol;
    }

    public int getColumnCount() {
        return this._data.getColumnCount();
    }

    public boolean nextCol() {
        ++this._currentcol;
        return this._currentcol < this._data.getColumnCount();
    }

    public int findRow(String find) {
        String[] findparams = StringUtil.split(find, ",");
        HashMap<String, Object> findmap = new HashMap<String, Object>();
        FormatUtil formatutil = FormatUtil.getInstance();
        block5: for (int i = 0; i < findparams.length; ++i) {
            String[] findparts = StringUtil.split(findparams[i].trim(), "=");
            if (findparts.length != 2) continue;
            switch (this._data.getColumnType(findparts[0].trim())) {
                case 0: {
                    findmap.put(findparts[0].trim(), findparts[1].trim());
                    continue block5;
                }
                case 2: {
                    findmap.put(findparts[0].trim(), new DateTimeUtil().getCalendar(findparts[1].trim()));
                    continue block5;
                }
                case 1: {
                    DecimalFormat df = new DecimalFormat();
                    if (df.parse(findparts[1], new ParsePosition(0)) == null) continue block5;
                    BigDecimal bd = formatutil.parseBigDecimal(findparts[1].trim());
                    findmap.put(findparts[0].trim(), bd);
                }
            }
        }
        return this._data.findRow(findmap);
    }

    public String findColValue(String find, String colid, String nullvalue) {
        int findrow = this.findRow(find);
        if (findrow > -1) {
            return this._data.getValue(findrow, colid, nullvalue);
        }
        return nullvalue;
    }

    public String getColumnId() {
        String[] columns = this._data.getColumns();
        return columns[this._currentcol];
    }

    public String getValue(String nullvalue) {
        if (this._currentrow == -9999 && this._templatedata != null) {
            return this._templatedata.getValue(0, this._data.getColumnId(this._currentcol), nullvalue);
        }
        return this._data.getValue(this._currentrow, this._data.getColumnId(this._currentcol), nullvalue);
    }

    public String getValue(int col, String nullvalue) {
        if (this._currentrow == -9999 && this._templatedata != null) {
            return this._templatedata.getValue(0, this._data.getColumnId(col), nullvalue);
        }
        return this._data.getValue(this._currentrow, this._data.getColumnId(col), nullvalue);
    }

    public String getValue(String colid, String nullvalue) {
        if (this._currentrow == -9999 && this._templatedata != null) {
            return this._templatedata.getValue(0, colid, nullvalue);
        }
        return this._data.getValue(this._currentrow, colid, nullvalue);
    }

    public String getValue(int row, int col, String nullvalue) {
        return this._data.getValue(row, this._data.getColumnId(col), nullvalue);
    }

    public String getValue(int row, String colid, String nullvalue) {
        return this._data.getValue(row, colid, nullvalue);
    }

    public boolean isMasked(int row, String colid) {
        return this._data.isMasked(row, colid);
    }

    public void sort(String sort) {
        this._data.sort(sort);
    }
}

