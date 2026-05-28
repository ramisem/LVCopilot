/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import java.math.BigDecimal;
import java.util.Calendar;
import sapphire.tagext.BaseTagInfo;

public class QueryTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "queryinfo";
    private QueryData _querydata = null;

    public QueryTagInfo(QueryData querydata) {
        this._querydata = querydata;
    }

    public int getRowCount() {
        return this._querydata.getRowCount();
    }

    public int getCurrentRow() {
        return this._querydata.getCurrentRow();
    }

    public int getColumnCount() {
        return this._querydata.getColumnCount();
    }

    public int getCurrentCol() {
        return this._querydata.getCurrentCol();
    }

    public String getCurrentColId() {
        return this._querydata.getColumnId();
    }

    public Object getObject(String columnid) {
        return this._querydata.getQuerydata().getObject(this._querydata.getCurrentRow(), columnid);
    }

    public Object getObject(int row, String columnid) {
        return this._querydata.getQuerydata().getObject(row, columnid);
    }

    public String getValue(String columnid) {
        return this._querydata.getValue(columnid, this._querydata.getNullValue());
    }

    public String getValue(int col) {
        return this._querydata.getValue(col, this._querydata.getNullValue());
    }

    public String getValue(int row, String columnid) {
        return this._querydata.getValue(row, columnid, this._querydata.getNullValue());
    }

    public String getValue(int row, int col) {
        return this._querydata.getValue(row, col, this._querydata.getNullValue());
    }

    public int getInt(String columnid) {
        return this._querydata.getQuerydata().getInt(this._querydata.getCurrentRow(), columnid);
    }

    public int getInt(int row, String columnid) {
        return this._querydata.getQuerydata().getInt(row, columnid);
    }

    public String getString(String columnid) {
        return this._querydata.getQuerydata().getString(this._querydata.getCurrentRow(), columnid);
    }

    public String getString(int row, String columnid) {
        return this._querydata.getQuerydata().getString(row, columnid);
    }

    public BigDecimal getBigDecimal(String columnid) {
        return this._querydata.getQuerydata().getBigDecimal(this._querydata.getCurrentRow(), columnid);
    }

    public BigDecimal getBigDecimal(int row, String columnid) {
        return this._querydata.getQuerydata().getBigDecimal(row, columnid);
    }

    public Calendar getCalendar(String columnid) {
        return this._querydata.getQuerydata().getCalendar(this._querydata.getCurrentRow(), columnid);
    }

    public Calendar getCalendar(int row, String columnid) {
        return this._querydata.getQuerydata().getCalendar(row, columnid);
    }

    public int findRow(String find) {
        return this._querydata.findRow(find);
    }
}

