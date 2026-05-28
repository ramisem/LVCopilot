/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.DataSetIndex;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.xml.DataSetHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class DataSet
extends ArrayList
implements Serializable,
JSONable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 102213 $";
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int UNKNOWN = -1;
    public static final int STRING = 0;
    public static final int NUMBER = 1;
    public static final int DATE = 2;
    public static final int CLOB = 3;
    private LinkedList _columns = new LinkedList();
    private HashMap _coltype = new HashMap();
    private HashMap _collength = new HashMap();
    private HashMap _dateFormatters = new HashMap();
    private String _currentsortstring;
    private Set<String> _keycolumns = new HashSet<String>();
    private DataSetIndex index = null;
    private M18NUtil m18n = new M18NUtil();
    private boolean isColidCaseSensitive = false;
    private Locale locale = null;
    private TimeZone timezone = null;
    private ZoneOffset timezoneOffset = null;
    private String id = "";
    public static final int NULLNUMBER = -999999999;
    private static final BigDecimal zero = new BigDecimal("0");
    private String cdataEscape = "";
    private static final String MASK_PREFIX_VALUE = "__mask_";
    private static final String MASK_PREFIX_FLAG = "__mask__flag_";
    private boolean forceISOFormat = false;
    private SimpleDateFormat isoLocaleSDF = null;
    private FormatUtil isoLocaleFU = null;

    public DataSet() {
    }

    public DataSet(ConnectionInfo connectionInfo) {
        this.setConnectionInfo(connectionInfo);
    }

    public DataSet(ResultSet rs) {
        this.setResultSet(rs);
    }

    public DataSet(ResultSet rs, ConnectionInfo connectionInfo) {
        this.setConnectionInfo(connectionInfo);
        this.setResultSet(rs);
    }

    public DataSet(String xml) {
        this.setXML(xml);
    }

    public DataSet(JSONObject job) {
        this.setJSONObject(job);
    }

    public DataSet(String xml, ConnectionInfo connectionInfo) {
        this.setConnectionInfo(connectionInfo);
        this.setXML(xml);
    }

    public DataSet(String columnnames, String rowdata) {
        this.setColumnRowString(columnnames, rowdata);
    }

    public DataSet(Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
        this.m18n = new M18NUtil(locale, timezone);
    }

    public DataSet(ResultSet rs, Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
        this.m18n = new M18NUtil(locale, timezone);
        this.setResultSet(rs);
    }

    public DataSet(String xml, Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
        this.m18n = new M18NUtil(locale, timezone);
        this.setXML(xml);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setCdataEscape(String escape) {
        this.cdataEscape = escape;
    }

    public String getCdataEscape() {
        return this.cdataEscape;
    }

    public String toXML() {
        return this.toXML(false, true);
    }

    public String toXML(boolean includeClobs) {
        return this.toXML(includeClobs, true);
    }

    public JSONObject toJSONObject() {
        return this.toJSONObject(true, true);
    }

    public JSONObject toJSONObject(boolean includeClobs, boolean includeUnknows) {
        return this.toJSONObject(false, includeClobs, includeUnknows);
    }

    public JSONObject toJSONObject(boolean optimizedFormat, boolean includeClobs, boolean includeUnknows) {
        return this.toJSONObject(optimizedFormat, this.getColumns(), true, includeClobs, includeUnknows);
    }

    public JSONObject toJSONObject(boolean optimizedFormat, String[] columns, boolean useDataTypes, boolean includeClobs, boolean includeUnknows) {
        if (columns == null) {
            columns = this.getColumns();
        }
        if (optimizedFormat) {
            return JSONUtil.toJSONObject(this, columns, useDataTypes, includeClobs, includeUnknows);
        }
        JSONObject jsonObj = new JSONObject();
        JSONObject jscolumns = new JSONObject();
        block20: for (int i = 0; i < columns.length; ++i) {
            int columnType = this.getColumnType(columns[i]);
            try {
                switch (columnType) {
                    case 0: {
                        jscolumns.put(columns[i], columnType);
                        break;
                    }
                    case 1: {
                        jscolumns.put(columns[i], columnType);
                        break;
                    }
                    case 2: {
                        jscolumns.put(columns[i], columnType);
                        break;
                    }
                    case 3: {
                        if (!includeClobs) continue block20;
                        jscolumns.put(columns[i], columnType);
                        break;
                    }
                    default: {
                        if (!includeUnknows) continue block20;
                        jscolumns.put(columns[i], columnType);
                        break;
                    }
                }
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            jsonObj.put("columns", jscolumns);
        }
        catch (Exception i) {
            // empty catch block
        }
        JSONArray jsrows = new JSONArray();
        int rows = this.getRowCount();
        for (int row = 0; row < rows; ++row) {
            JSONObject jsrow = new JSONObject();
            block22: for (int i = 0; i < columns.length; ++i) {
                String columnid = columns[i];
                try {
                    switch (this.getColumnType(columnid)) {
                        case 0: {
                            jsrow.put(columnid, useDataTypes ? this.getString(row, columnid, "") : this.getValue(row, columnid, ""));
                            break;
                        }
                        case 1: {
                            if (useDataTypes) {
                                BigDecimal bd = this.getBigDecimal(row, columnid);
                                if (bd == null || bd.intValue() == -999999999) {
                                    jsrow.put(columnid, JSONObject.NULL);
                                    break;
                                }
                                jsrow.put(columnid, bd.doubleValue());
                                break;
                            }
                            jsrow.put(columnid, this.getValue(row, columnid, ""));
                            break;
                        }
                        case 2: {
                            if (useDataTypes) {
                                Calendar o = this.getCalendar(row, columnid);
                                jsrow.put(columnid, o.getTimeInMillis());
                                break;
                            }
                            jsrow.put(columnid, this.getValue(row, columnid, ""));
                            break;
                        }
                        case 3: {
                            if (!includeClobs) continue block22;
                            jsrow.put(columnid, useDataTypes ? this.getClob(row, columnid, "") : this.getValue(row, columnid, ""));
                            break;
                        }
                        default: {
                            if (!includeUnknows) continue block22;
                            jsrow.put(columnid, useDataTypes ? this.getObject(row, columnid).toString() : this.getValue(row, columnid, ""));
                        }
                    }
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            jsrows.put(jsrow);
        }
        try {
            jsonObj.put("rows", jsrows);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return jsonObj;
    }

    public DataSet copy(String[] columns, boolean includeClobs) {
        DataSet copy = new DataSet();
        copy.setM18NUtil(this.m18n);
        if (columns == null) {
            columns = this.getColumns();
        }
        block14: for (int i = 0; i < columns.length; ++i) {
            int columnType = this.getColumnType(columns[i]);
            try {
                switch (columnType) {
                    case 0: 
                    case 1: 
                    case 2: {
                        copy.addColumn(columns[i], columnType);
                        break;
                    }
                    case 3: {
                        if (!includeClobs) continue block14;
                        copy.addColumn(columns[i], columnType);
                        break;
                    }
                    default: {
                        copy.addColumn(columns[i], columnType);
                        break;
                    }
                }
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        int rows = this.getRowCount();
        for (int row = 0; row < rows; ++row) {
            int newrow = copy.addRow();
            block16: for (int i = 0; i < columns.length; ++i) {
                String columnid = this.getColumnId(i);
                try {
                    switch (this.getColumnType(columns[i])) {
                        case 0: {
                            copy.setString(newrow, columnid, this.getString(row, columnid));
                            break;
                        }
                        case 1: {
                            copy.setNumber(newrow, columnid, this.getBigDecimal(row, columnid));
                            break;
                        }
                        case 2: {
                            copy.setDate(newrow, columnid, this.getCalendar(row, columnid));
                            break;
                        }
                        case 3: {
                            if (!includeClobs) continue block16;
                            copy.setClob(newrow, columnid, this.getClob(row, columnid));
                            break;
                        }
                        default: {
                            copy.setObject(newrow, columnid, this.getObject(row, columnid));
                            break;
                        }
                    }
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        copy.index = this.getIndex().copy(copy);
        return copy;
    }

    public DataSet getRows(int from, int to) {
        if (from < this.size()) {
            if (to >= this.size()) {
                to = this.size();
            }
            DataSet out = new DataSet();
            out.addAll(this.subList(from, to));
            out._columns = this._columns;
            out._collength = this._collength;
            out._coltype = this._coltype;
            out._currentsortstring = this._currentsortstring;
            out._dateFormatters = this._dateFormatters;
            out.isColidCaseSensitive = this.isColidCaseSensitive;
            out.id = this.id;
            out.locale = this.locale;
            out.timezone = this.timezone;
            out.timezoneOffset = this.timezoneOffset;
            out.setM18NUtil(this.m18n);
            return out;
        }
        return null;
    }

    public boolean setJSONObject(JSONObject json) {
        if (json.has("dataset") && (json.has("columnmap") || json.has("columncount"))) {
            try {
                JSONArray dataRows = json.getJSONArray("dataset");
                JSONArray dataTypes = json.has("types") ? json.getJSONArray("types") : null;
                JSONObject columnmap = json.has("columnmap") && json.getJSONObject("columnmap") != null ? json.getJSONObject("columnmap") : json.getJSONObject("columns");
                Iterator it = columnmap.keys();
                while (it.hasNext()) {
                    String columnid = (String)it.next();
                    int colindex = columnmap.getInt(columnid);
                    if (dataTypes == null || dataTypes.length() < colindex) {
                        this.addColumn(columnid, 0);
                        continue;
                    }
                    this.addColumn(columnid, dataTypes.getInt(colindex));
                }
                for (int i = 0; i < dataRows.length(); ++i) {
                    int row = this.addRow();
                    JSONArray dataRow = dataRows.getJSONArray(i);
                    Iterator it2 = columnmap.keys();
                    while (it2.hasNext()) {
                        String columnid = (String)it2.next();
                        int colindex = columnmap.getInt(columnid);
                        if (dataRow.isNull(colindex)) continue;
                        this.setValue(row, columnid, dataRow.getString(colindex));
                    }
                }
            }
            catch (Exception dataRows) {}
        } else {
            if (json.has("columns")) {
                try {
                    JSONObject cols = json.getJSONObject("columns");
                    Iterator keys = cols.keys();
                    while (keys.hasNext()) {
                        String columnid = keys.next().toString();
                        int type = cols.getInt(columnid);
                        this.addColumn(columnid, type);
                    }
                }
                catch (Exception cols) {
                    // empty catch block
                }
            }
            if (json.has("rows")) {
                try {
                    JSONArray rows = json.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); ++i) {
                        int addrow = this.addRow();
                        try {
                            JSONObject cols = rows.getJSONObject(i);
                            Iterator keys = cols.keys();
                            while (keys.hasNext()) {
                                String value;
                                String columnid = keys.next().toString();
                                Object jso = cols.get(columnid);
                                String string = value = jso instanceof JSONObject && ((JSONObject)jso).length() == 0 ? null : jso.toString();
                                if (!this.isValidColumn(columnid)) continue;
                                this.setValue(addrow, columnid, value);
                            }
                            continue;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
        return true;
    }

    @Override
    public String toJSONString() {
        return this.toJSONString(true, true, true);
    }

    public String toJSONString(boolean includeClobs, boolean includeUnknows) {
        return this.toJSONString(false, includeClobs, includeUnknows);
    }

    public String toJSONString(boolean optimizedFormat, boolean includeClobs, boolean includeUnknows) {
        return this.toJSONObject(optimizedFormat, includeClobs, includeUnknows).toString();
    }

    public String toJSONString(boolean optimizedFormat, String[] columns, boolean useDataTypes, boolean includeClobs, boolean includeUnknows) {
        return this.toJSONObject(optimizedFormat, columns, useDataTypes, includeClobs, includeUnknows).toString();
    }

    public String toXML(boolean includeClobs, boolean includeColumnDefinitions) {
        StringBuffer output = new StringBuffer();
        String cdataEscapeAttribute = this.cdataEscape == null || this.cdataEscape.length() == 0 ? "" : " cdataescape=\"" + this.cdataEscape + "\"";
        output.append("<dataset");
        if (this.id != null && this.id.length() > 0) {
            output.append(" id=\"").append(this.id).append("\"");
        }
        output.append(cdataEscapeAttribute);
        if (this.forceISOFormat) {
            output.append(" forceISOFormat=\"Y\"");
        }
        output.append(" >\n");
        String[] column = this.getColumns();
        if (includeColumnDefinitions) {
            output.append("  <columns>\n");
            for (int i = 0; i < column.length; ++i) {
                int columnType = this.getColumnType(column[i]);
                if (!includeClobs && columnType == 3) continue;
                output.append("    <coldef id=\"").append(column[i]).append("\" type=\"").append(columnType == 3 ? "CLOB" : (columnType == 2 ? "DATE" : (columnType == 1 ? "NUMBER" : "STRING"))).append("\" />\n");
            }
            output.append("  </columns>\n");
        }
        output.append("  <rows>\n");
        int rows = this.getRowCount();
        for (int row = 0; row < rows; ++row) {
            output.append("    <row>\n");
            for (int i = 0; i < column.length; ++i) {
                String value;
                int columnType = this.getColumnType(column[i]);
                if (!includeClobs && columnType == 3 || (value = this.getValue(row, column[i])).equals("")) continue;
                if (this.cdataEscape != null && this.cdataEscape.length() > 0) {
                    value = value.replaceAll("]]>", this.cdataEscape);
                }
                output.append("      <col id=\"").append(column[i]).append("\"><![CDATA[").append(value).append("]]></col>\n");
            }
            output.append("    </row>\n");
        }
        output.append("  </rows>\n");
        output.append("</dataset>\n");
        return output.toString();
    }

    public String toHTML(List titles) {
        return this.toHTML(titles, "");
    }

    public String toHTML() {
        return this.toHTML((List)this._columns, "");
    }

    public String toHTML(List titles, List columnIds) {
        return this.toHTML(titles, "", columnIds);
    }

    public String toHTML(List titles, String stylePrefix, List<String> columnIds) {
        if (stylePrefix != null && stylePrefix.length() > 0) {
            stylePrefix = stylePrefix + "_";
        }
        StringBuffer output = new StringBuffer();
        output.append("<table border=\"1\" class=\"").append(stylePrefix).append("gridtable\" cellspacing=\"0\">\n");
        if (titles != null && titles.size() > 0) {
            output.append("<tr>\n");
            for (int col = 0; col < titles.size(); ++col) {
                output.append("<td class=\"").append(stylePrefix).append("gridtitle\">").append(titles.get(col)).append("</td>");
            }
            output.append("</tr>\n");
        }
        int rows = this.getRowCount();
        String[] column = this.getColumns();
        if (columnIds == null) {
            columnIds = Arrays.asList(column);
        }
        for (int row = 0; row < rows; ++row) {
            output.append("<tr>\n");
            for (String columnid : columnIds) {
                String value = this.getValue(row, columnid);
                output.append("<td class=\"").append(stylePrefix).append("gridcell\">").append(value.equals("") ? "&nbsp;" : value).append("</td>");
            }
            output.append("</tr>\n");
        }
        output.append("</table>\n");
        return output.toString();
    }

    public String toHTML(List titles, String stylePrefix) {
        return this.toHTML(titles, stylePrefix, null);
    }

    @Override
    public String toString() {
        int rows = this.getRowCount();
        String[] column = this.getColumns();
        StringBuffer rowout = new StringBuffer();
        for (int row = 0; row < rows; ++row) {
            HashMap rowdata = (HashMap)this.get(row);
            if (rowdata != null) {
                block7: for (int col = 0; col < column.length; ++col) {
                    if (col > 0) {
                        rowout.append("\t");
                    }
                    switch (this.getColumnType(column[col])) {
                        case 0: {
                            rowout.append(column[col].toUpperCase()).append(": ").append((String)rowdata.get(column[col]));
                            continue block7;
                        }
                        case 3: {
                            String value = (String)rowdata.get(column[col]);
                            if (value != null && value.length() > 100) {
                                value = value.substring(100);
                            }
                            rowout.append(column[col].toUpperCase()).append(": ").append(value);
                            continue block7;
                        }
                        case 1: {
                            BigDecimal bd = (BigDecimal)rowdata.get(column[col]);
                            rowout.append(column[col].toUpperCase()).append(": ").append(bd != null ? String.valueOf(bd.floatValue()) : "null");
                            continue block7;
                        }
                        case 2: {
                            rowout.append("Date");
                            continue block7;
                        }
                        default: {
                            rowout.append("Unknown datatype");
                        }
                    }
                }
            } else {
                rowout.append("Null row");
            }
            rowout.append("\n");
        }
        return rowout.toString();
    }

    public void showData() {
        int rows = this.getRowCount();
        String[] column = this.getColumns();
        for (int row = 0; row < rows; ++row) {
            HashMap rowdata = (HashMap)this.get(row);
            StringBuffer rowout = new StringBuffer();
            if (rowdata != null) {
                block7: for (int col = 0; col < column.length; ++col) {
                    switch (this.getColumnType(column[col])) {
                        case 0: {
                            rowout.append("\t").append(column[col].toUpperCase()).append(": ").append((String)rowdata.get(column[col]));
                            continue block7;
                        }
                        case 3: {
                            String value = (String)rowdata.get(column[col]);
                            if (value != null && value.length() > 100) {
                                value = value.substring(100);
                            }
                            rowout.append("\t").append(column[col].toUpperCase()).append(": ").append(value);
                            continue block7;
                        }
                        case 1: {
                            BigDecimal bd = (BigDecimal)rowdata.get(column[col]);
                            rowout.append("\t").append(column[col].toUpperCase()).append(": ").append(bd != null ? String.valueOf(bd.floatValue()) : "null");
                            continue block7;
                        }
                        case 2: {
                            rowout.append("\tDate");
                            continue block7;
                        }
                        default: {
                            rowout.append("\tUnknown datatype");
                        }
                    }
                }
            } else {
                rowout.append("Null row");
            }
            Trace.log("DEBUG:", rowout.toString());
        }
    }

    public void reset() {
        this.clear();
        this._columns.clear();
        this._coltype.clear();
        this._collength.clear();
        this._currentsortstring = "";
        this._keycolumns.clear();
        this.index = null;
    }

    public void deleteRow(int row) {
        this.remove(row);
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
    }

    public void moveRow(int from, int amount) {
        int to;
        if (from > -1 && from < this.size() && (to = from + amount) > -1 && to < this.size() && from != to) {
            if (amount > 0) {
                for (int i = 0; i < amount; ++i) {
                    Collections.swap(this, from + i, from + i + 1);
                }
            } else {
                for (int i = 0; i < amount * -1; ++i) {
                    Collections.swap(this, from + i * -1, from + i * -1 - 1);
                }
            }
            if (this.isIndexing()) {
                this.getIndex().flushAllIndexes();
            }
        }
    }

    public boolean setResultSet(ResultSet rs) {
        return this.setResultSet(rs, false, "ORA");
    }

    public boolean setResultSet(ResultSet rs, boolean extendedDataTypes, String dbms) {
        return this.setResultSet(rs, extendedDataTypes, dbms, null);
    }

    public boolean setResultSet(ResultSet rs, boolean extendedDataTypes, String dbms, ResultSetRowProcessor rowProcessor) {
        boolean rc = true;
        if (rs != null) {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int cols = rsmd.getColumnCount();
                String[] colname = new String[cols];
                int[] coltype = new int[cols];
                block15: for (int col = 0; col < cols; ++col) {
                    colname[col] = this.isColidCaseSensitive ? rsmd.getColumnName(col + 1) : rsmd.getColumnName(col + 1).toLowerCase();
                    switch (rsmd.getColumnType(col + 1)) {
                        case -9: 
                        case 1: 
                        case 12: {
                            coltype[col] = 0;
                            this.addColumn(colname[col], 0);
                            this._collength.put(colname[col], new Integer(rsmd.getColumnDisplaySize(col + 1)));
                            continue block15;
                        }
                        case -16: 
                        case -1: 
                        case 2005: {
                            coltype[col] = 3;
                            if (!extendedDataTypes) continue block15;
                            this.addColumn(colname[col], 3);
                            this._collength.put(colname[col], new Integer(4000));
                            continue block15;
                        }
                        case 2: 
                        case 3: 
                        case 4: {
                            coltype[col] = 1;
                            this.addColumn(colname[col], 1);
                            continue block15;
                        }
                        case 91: 
                        case 93: {
                            coltype[col] = 2;
                            this.addColumn(colname[col], 2);
                            continue block15;
                        }
                        case 2004: {
                            coltype[col] = -1;
                        }
                    }
                }
                while (rs.next()) {
                    HashMap<String, Object> newrow = new HashMap<String, Object>();
                    block17: for (int col = 0; col < cols; ++col) {
                        switch (coltype[col]) {
                            case 0: {
                                String val = rs.getString(colname[col]);
                                if (XSS.isMock()) {
                                    val = XSS.mock(val, colname[col]);
                                }
                                newrow.put(colname[col], val);
                                continue block17;
                            }
                            case 3: {
                                if (!extendedDataTypes) continue block17;
                                String value = "";
                                if (dbms.equals("ORA")) {
                                    int length;
                                    Clob clob = rs.getClob(colname[col]);
                                    if (clob != null && (length = (int)clob.length()) > 0) {
                                        value = clob.getSubString(1L, length);
                                    }
                                } else {
                                    value = rs.getString(colname[col]);
                                }
                                newrow.put(colname[col], value == null ? null : value.trim());
                                continue block17;
                            }
                            case 1: {
                                BigDecimal b = rs.getBigDecimal(colname[col]);
                                if (dbms.equals("MSS") && b != null && b.toPlainString().indexOf(".") > 0) {
                                    b = b.compareTo(zero) == 0 ? zero : b.stripTrailingZeros();
                                }
                                newrow.put(colname[col], b);
                                continue block17;
                            }
                            case 2: {
                                String temptimestamp = rs.getString(colname[col]);
                                Calendar c = null;
                                if (temptimestamp != null) {
                                    Date tempdate = new Date();
                                    tempdate.setTime(rs.getTimestamp(colname[col]).getTime());
                                    c = Calendar.getInstance();
                                    c.setTime(tempdate);
                                }
                                newrow.put(colname[col], c);
                            }
                        }
                    }
                    if (rowProcessor != null) {
                        rowProcessor.processRow(newrow, rs, this);
                    }
                    this.add(newrow);
                }
            }
            catch (Exception e) {
                rc = false;
                Trace.log("ERROR:", "Dataset exception: " + e.getMessage());
            }
        } else {
            Trace.log("ResultSet passed to dataset is null!");
        }
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
        return rc;
    }

    public boolean setXML(String xml) {
        boolean rc = true;
        try {
            DataSetHandler handler = new DataSetHandler(this);
            handler.setXMLString(xml);
            SaxUtil.parseString(handler);
        }
        catch (Exception e) {
            rc = false;
        }
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
        return rc;
    }

    public boolean setColumnRowString(String columns, String rowdata) {
        return this.setColumnRowString(columns, rowdata, ";", "|");
    }

    public boolean setColumnRowString(String columns, String rowdata, String delim1, String delim2) {
        boolean rc;
        block10: {
            rc = true;
            try {
                String[] columnnames = StringUtil.split(columns, delim1);
                String[] rows = StringUtil.split(rowdata, delim1);
                if (rowdata != null && rowdata.length() > 0 && rows.length > 0) {
                    if (columnnames.length > 0) {
                        for (int r = 0; r < rows.length; ++r) {
                            String row = rows[r];
                            if (row.length() <= 0) continue;
                            int newrow = this.addRow();
                            String[] data = StringUtil.split(row, delim2);
                            if (data.length == columnnames.length) {
                                for (int c = 0; c < columnnames.length; ++c) {
                                    String colname = columnnames[c];
                                    if (columnnames.length <= 0) continue;
                                    if (!this.isValidColumn(colname)) {
                                        this.addColumn(colname, 0);
                                    }
                                    this.setValue(newrow, colname, data[c]);
                                }
                                continue;
                            }
                            return false;
                        }
                        break block10;
                    }
                    return false;
                }
                if (columns != null && columns.length() > 0 && columnnames.length > 0) {
                    for (int c = 0; c < columnnames.length; ++c) {
                        String colname = columnnames[c];
                        if (columnnames.length <= 0) continue;
                        this.addColumn(colname, 0);
                    }
                    break block10;
                }
                return false;
            }
            catch (Exception e) {
                rc = false;
            }
        }
        return rc;
    }

    public int addRow() {
        this.add(new HashMap());
        return this.size() - 1;
    }

    public int addRow(int newrow) {
        this.add(newrow, new HashMap());
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
        return newrow;
    }

    public void copyRow(int copyrow, int copies) {
        HashMap initial = (HashMap)this.get(copyrow);
        for (int copy = 0; copy < copies; ++copy) {
            HashMap newrow = new HashMap(initial);
            this.add(newrow);
        }
    }

    public void copyRow(DataSet from, int copyrow, int copies) {
        if (copyrow < from.getRowCount() && copies > 0) {
            int startrow = copyrow == -1 ? 0 : copyrow;
            int endrow = copyrow == -1 ? from.getRowCount() : copyrow + 1;
            String[] columns = from.getColumns();
            for (int i = 0; i < columns.length; ++i) {
                String columnid = columns[i];
                this.addColumn(columnid, from.getColumnType(columnid), from.getColumnLength(columnid));
            }
            for (int row = startrow; row < endrow; ++row) {
                HashMap initial = (HashMap)from.get(row);
                for (int copy = 0; copy < copies; ++copy) {
                    HashMap newrow = new HashMap(initial);
                    this.add(newrow);
                }
            }
        }
    }

    public int getRowCount() {
        return this.size();
    }

    public boolean addColumn(String columnid, int type) {
        return this.addColumn(columnid, type, -1);
    }

    public boolean addColumn(String columnid, int type, int length) {
        String string = columnid = this.isColidCaseSensitive ? columnid : columnid.toLowerCase();
        if (!this._columns.contains(columnid) && this.isValidColumnType(type)) {
            this._columns.add(columnid);
            this._coltype.put(columnid, new Integer(type));
            if (length >= 0) {
                this._collength.put(columnid, new Integer(length));
            }
            return true;
        }
        return false;
    }

    public boolean addColumnValues(String columnid, int type, String valueslist, String delimeter, String defaultvalue) {
        if (valueslist == null || valueslist.length() == 0) {
            valueslist = defaultvalue;
        }
        return this.addColumnValues(columnid, type, valueslist, delimeter);
    }

    public boolean addColumnValues(String columnid, int type, String valueslist, String delimeter) {
        String string = columnid = this.isColidCaseSensitive ? columnid : columnid.toLowerCase();
        if (!this._columns.contains(columnid) && this.isValidColumnType(type)) {
            this._columns.add(columnid);
            this._coltype.put(columnid, new Integer(type));
        }
        if (valueslist == null) {
            valueslist = "";
        }
        String[] values = StringUtil.split(valueslist, delimeter);
        int rows = this.getRowCount();
        for (int i = 0; i < values.length; ++i) {
            if (i >= rows) {
                this.addRow();
            }
            this.setValue(i, columnid, values[i]);
        }
        return true;
    }

    public void padColumns() {
        String[] columns = this.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            this.padColumn(columns[i]);
        }
    }

    public void padColumn(String columnid) {
        int rows = this.getRowCount();
        Object padvalue = null;
        for (int i = 0; i < rows; ++i) {
            if (this.isNull(i, columnid)) {
                this.setObject(i, columnid, padvalue);
                continue;
            }
            padvalue = this.getObject(i, columnid);
        }
    }

    private boolean isValidColumnType(int type) {
        return type == 0 || type == 3 || type == 1 || type == 2;
    }

    public boolean isValidColumn(String columnid) {
        return this._columns.contains(this.isColidCaseSensitive ? columnid : columnid.toLowerCase());
    }

    public int getColumnCount() {
        return this._columns.size();
    }

    public String[] getColumns() {
        return this._columns.toArray(new String[0]);
    }

    public int getColumnType(String columnid) {
        Integer i = (Integer)this._coltype.get(this.isColidCaseSensitive ? columnid : columnid.toLowerCase());
        if (i == null) {
            return -1;
        }
        return i;
    }

    public int getColumnLength(String columnid) {
        Integer i = (Integer)this._collength.get(this.isColidCaseSensitive ? columnid : columnid.toLowerCase());
        if (i == null) {
            return -1;
        }
        return i;
    }

    public String getColumnId(int col) {
        return (String)this._columns.get(col);
    }

    public String getColumnValues(String columnid, int startrow, int endrow, String delimeter) {
        return this.getColumnValues(columnid, startrow, endrow, delimeter, false);
    }

    public String getColumnValues(String columnid, int startrow, int endrow, String delimeter, boolean distinct) {
        int i;
        ArrayList<String> valueslist = new ArrayList<String>();
        StringBuffer output = new StringBuffer((endrow - startrow) * 20);
        for (i = startrow; i < endrow; ++i) {
            String value;
            if (i != startrow) {
                output.append(delimeter);
            }
            if ((value = this.getValue(i, columnid)).indexOf(";") >= 0 && ";".equals(delimeter)) {
                value = StringUtil.replaceAll(value, ";", "#semicolon#");
            }
            if (distinct && (valueslist.size() == 0 || !valueslist.contains(value))) {
                valueslist.add(value);
            }
            output.append(value);
        }
        if (distinct) {
            output = new StringBuffer();
            for (i = 0; i < valueslist.size(); ++i) {
                if (i > 0) {
                    output.append(delimeter);
                }
                output.append((String)valueslist.get(i));
            }
            return output.toString();
        }
        return output.toString();
    }

    public String getColumnValues(String columnid, String delimeter) {
        return this.getColumnValues(columnid, 0, this.getRowCount(), delimeter);
    }

    public boolean setObject(int row, String columnid, Object o) {
        int rows = this.getRowCount();
        if (row == -1) {
            for (int i = 0; i < rows; ++i) {
                if (this.isIndexing(columnid)) {
                    this.getIndex().weakProtectIndex(columnid, i);
                }
                HashMap rowdata = (HashMap)this.get(i);
                rowdata.put(this.isColidCaseSensitive ? columnid : columnid.toLowerCase(), o);
            }
            return true;
        }
        if (row < rows) {
            if (this.isIndexing(columnid)) {
                this.getIndex().weakProtectIndex(columnid, row);
            }
            HashMap rowdata = (HashMap)this.get(row);
            rowdata.put(this.isColidCaseSensitive ? columnid : columnid.toLowerCase(), o);
            return true;
        }
        return false;
    }

    public boolean setString(int row, String columnid, String value) {
        this.addColumn(columnid, 0);
        return this.setObject(row, columnid, value);
    }

    public boolean setClob(int row, String columnid, String value) {
        this.addColumn(columnid, 3);
        return this.setObject(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, int value) {
        this.addColumn(columnid, 1);
        return this.setObject(row, columnid, new BigDecimal(value));
    }

    public boolean setNumber(int row, String columnid, long value) {
        this.addColumn(columnid, 1);
        return this.setObject(row, columnid, new BigDecimal(value));
    }

    public boolean setNumber(int row, String columnid, double value) {
        this.addColumn(columnid, 1);
        return this.setObject(row, columnid, new BigDecimal(value));
    }

    public boolean setNumber(int row, String columnid, BigDecimal value) {
        this.addColumn(columnid, 1);
        return this.setObject(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, String value) {
        this.addColumn(columnid, 1);
        BigDecimal o = null;
        if (value != null && value.length() > 0) {
            try {
                o = this.forceISOFormat ? this.isoLocaleFU.parseBigDecimal(value) : this.m18n.parseBigDecimal(value);
            }
            catch (Exception ignore) {
                o = null;
                Trace.logDebug("DataSet failed to parse number value: " + value + ". Assuming null instead.");
            }
        }
        return this.setObject(row, columnid, o);
    }

    public boolean setDate(int row, String columnid, long value) {
        this.addColumn(columnid, 2);
        Calendar o = this.m18n.getNowCalendar();
        o.setTimeInMillis(value);
        return this.setObject(row, columnid, o);
    }

    public boolean setDate(int row, String columnid, String value) {
        this.addColumn(columnid, 2);
        Calendar cal = null;
        if (value != null && value.length() > 0) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                try {
                    cal = this.m18n.getNowCalendar();
                    cal.setTimeInMillis(Long.parseLong(value));
                }
                catch (NumberFormatException e) {
                    Logger.logDebug("Failed to process UTC long date " + value);
                    cal = null;
                }
            } else if (this.forceISOFormat) {
                try {
                    Date dt = this.isoLocaleSDF.parse(value);
                    cal = Calendar.getInstance();
                    cal.setTimeZone(this.isoLocaleSDF.getTimeZone());
                    cal.setTime(dt);
                }
                catch (ParseException e) {
                    Logger.logDebug("Failed to parse UTC Date Format: " + value);
                }
            } else {
                cal = this._dateFormatters.get(columnid) != null && !((DateFormat)this._dateFormatters.get(columnid)).getTimeZone().equals(this.timezone) ? this.m18n.parseCalendar(value, false) : this.m18n.parseCalendar(value);
                if (this.timezoneOffset != null) {
                    int seconds = this.timezoneOffset.getTotalSeconds();
                    cal.setTimeInMillis(cal.getTimeInMillis() - (long)seconds * 1000L);
                }
            }
        }
        return this.setObject(row, columnid, cal);
    }

    public boolean setDate(int row, String columnid, Calendar value) {
        this.addColumn(columnid, 2);
        return this.setObject(row, columnid, value);
    }

    public boolean setDate(int row, String columnid, Timestamp value) {
        this.addColumn(columnid, 2);
        Calendar c = null;
        if (value != null) {
            Date tempdate = new Date();
            tempdate.setTime(value.getTime());
            c = Calendar.getInstance();
            c.setTime(tempdate);
        }
        return this.setDate(row, columnid, c);
    }

    public void setDateDisplayFormat(String columnid, DateFormat dateformat) {
        this._dateFormatters.put(columnid, dateformat);
    }

    public DateFormat getDateDisplayFormat(String columnid) {
        if (this.forceISOFormat) {
            return this.isoLocaleSDF;
        }
        if (this._dateFormatters.containsKey(columnid) && this._dateFormatters.get(columnid) != null) {
            return (DateFormat)this._dateFormatters.get(columnid);
        }
        return this.m18n.getDefaultDateFormat();
    }

    public void setTimeZoneInsensitive(String columnid) {
        this.addColumn(columnid, 2);
        if (this._dateFormatters.get(columnid) == null) {
            this._dateFormatters.put(columnid, this.m18n.getDefaultDateFormat(false));
        }
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            this.locale = locale;
        }
    }

    public Locale getLocale() {
        return this.locale == null ? Locale.getDefault() : this.locale;
    }

    public void setTimeZone(TimeZone timezone) {
        if (timezone != null) {
            this.timezone = timezone;
        }
    }

    public TimeZone getTimeZone() {
        return this.timezone == null ? TimeZone.getDefault() : this.timezone;
    }

    public void setTimezoneOffset(String zoneid) {
        this.setTimezoneOffset(I18nUtil.getZoneIdFromString(zoneid));
    }

    public void setTimezoneOffset(ZoneId zoneId) {
        if (zoneId != null) {
            this.timezoneOffset = zoneId.getRules().getOffset(Instant.now());
        }
    }

    public void setM18NUtil(M18NUtil m18NUtil) {
        this.m18n = m18NUtil;
        this.locale = this.m18n.getLocale();
        this.timezone = this.m18n.getTimezone();
    }

    public M18NUtil getM18n() {
        return this.m18n;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.setM18NUtil(new M18NUtil(connectionInfo));
    }

    public DataSet copy() {
        DataSet copy = new DataSet();
        copy.setM18NUtil(this.m18n);
        copy.setXML(this.toXML());
        copy.index = this.getIndex().copy(copy);
        return copy;
    }

    public void setColidCaseSensitive(boolean isColidCaseSensitive) {
        this.isColidCaseSensitive = isColidCaseSensitive;
    }

    public boolean getColidCaseSensitive() {
        return this.isColidCaseSensitive;
    }

    public boolean setValue(int row, String columnid, String value) {
        boolean rc = true;
        switch (this.getColumnType(columnid)) {
            case 0: 
            case 3: {
                rc = this.setObject(row, columnid, value);
                break;
            }
            case 1: {
                rc = this.setNumber(row, columnid, value);
                break;
            }
            case 2: {
                rc = this.setDate(row, columnid, value);
            }
        }
        return rc;
    }

    public boolean setSequence(int startrow, int endrow, String columnid, int startseq) {
        boolean setstatus = true;
        columnid = this.isColidCaseSensitive ? columnid : columnid.toLowerCase();
        this.addColumn(columnid, 1);
        int rows = this.getRowCount();
        if (startrow >= 0 && startrow <= rows && endrow >= 0 && endrow <= rows) {
            for (int row = startrow; row < endrow && setstatus; ++row) {
                HashMap rowdata = (HashMap)this.get(row);
                rowdata.put(columnid, new BigDecimal(startseq++));
            }
            if (this.isIndexing(columnid)) {
                this.getIndex().flushIndex(columnid);
            }
        } else {
            setstatus = false;
        }
        return setstatus;
    }

    public boolean setSequence(String columnid) {
        return this.setSequence(0, this.getRowCount(), columnid, 0);
    }

    public String getString(int row, String columnid) {
        String value = (String)this.getObject(row, columnid);
        if (value != null && value.indexOf("#semicolon#") != -1) {
            value = value.replaceAll("#semicolon#", ";");
        }
        return value;
    }

    public String getString(int row, String columnid, String defaultvalue) {
        String temp = this.getString(row, columnid);
        return temp == null || temp.length() == 0 ? defaultvalue : temp;
    }

    public String getClob(int row, String columnid) {
        return this.getString(row, columnid);
    }

    public String getClob(int row, String columnid, String defaultvalue) {
        return this.getString(row, columnid, defaultvalue);
    }

    public BigDecimal getBigDecimal(int row, String columnid) {
        Object bd = this.getObject(row, columnid);
        if (bd != null && bd instanceof BigDecimal) {
            return (BigDecimal)bd;
        }
        return null;
    }

    public BigDecimal getBigDecimal(int row, String columnid, BigDecimal defaultvalue) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        return temp == null ? defaultvalue : temp;
    }

    public double getDouble(int row, String columnid) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        if (temp == null) {
            return -9.99999999E8;
        }
        return temp.doubleValue();
    }

    public double getDouble(int row, String columnid, double defaultvalue) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        if (temp == null) {
            return defaultvalue;
        }
        return ((BigDecimal)this.getObject(row, columnid)).doubleValue();
    }

    public int getInt(int row, String columnid) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        if (temp == null) {
            return -999999999;
        }
        return temp.intValue();
    }

    public long getLong(int row, String columnid) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        if (temp == null) {
            return -999999999L;
        }
        return temp.longValue();
    }

    public int getInt(int row, String columnid, int defaultvalue) {
        BigDecimal temp = (BigDecimal)this.getObject(row, columnid);
        if (temp == null) {
            return defaultvalue;
        }
        return ((BigDecimal)this.getObject(row, columnid)).intValue();
    }

    public Calendar getCalendar(int row, String columnid) {
        return (Calendar)this.getObject(row, columnid);
    }

    public Calendar getCalendar(int row, String columnid, Calendar defaultvalue) {
        Calendar temp = (Calendar)this.getObject(row, columnid);
        return temp == null ? defaultvalue : temp;
    }

    public Timestamp getTimestamp(int row, String columnid) {
        Calendar cal = (Calendar)this.getObject(row, columnid);
        return cal != null ? new Timestamp(cal.getTime().getTime()) : null;
    }

    public Timestamp getTimestamp(int row, String columnid, Timestamp defaultvalue) {
        Calendar cal = (Calendar)this.getObject(row, columnid);
        Timestamp temp = cal != null ? new Timestamp(((Calendar)this.getObject(row, columnid)).getTime().getTime()) : null;
        return temp == null ? defaultvalue : temp;
    }

    public Object getObject(int row, String columnid) {
        if (row >= 0 && row < this.size()) {
            HashMap rowdata = (HashMap)this.get(row);
            return rowdata.get(this.isColidCaseSensitive ? columnid : columnid.toLowerCase());
        }
        return null;
    }

    public String getValue(int row, String columnid) {
        return this.getValue(row, columnid, false);
    }

    public String getValue(int row, String columnid, boolean ignoreMasking) {
        String value = "";
        if (!this.isNull(row, columnid)) {
            if (!ignoreMasking && this.isMasked(row, columnid)) {
                value = this.getMaskedString(row, columnid);
            } else {
                switch (this.getColumnType(columnid)) {
                    case 0: 
                    case 3: {
                        value = this.getString(row, columnid);
                        break;
                    }
                    case 1: {
                        BigDecimal bd = this.getBigDecimal(row, columnid);
                        if (bd != null) {
                            if (this.forceISOFormat) {
                                value = this.isoLocaleFU.format(bd);
                                break;
                            }
                            value = this.m18n.format(bd);
                            break;
                        }
                        value = "";
                        break;
                    }
                    case 2: {
                        Calendar cal = this.getCalendar(row, columnid);
                        if (this.timezoneOffset != null) {
                            long newMillis = cal.getTimeInMillis() + (long)this.timezoneOffset.getTotalSeconds() * 1000L;
                            cal = Calendar.getInstance();
                            cal.setTimeInMillis(newMillis);
                        }
                        value = this.getDateDisplayFormat(columnid).format(cal.getTime());
                    }
                }
            }
        }
        return value;
    }

    public String getValue(int row, String columnid, String nullvalue) {
        String value = this.getValue(row, columnid);
        return value == null || value.length() == 0 ? nullvalue : value;
    }

    public boolean isNull(int row, String columnid) {
        boolean b = false;
        Object o = this.getObject(row, columnid);
        if (o == null) {
            b = true;
        } else if (this.getColumnType(columnid) == 0 && o.toString().equals("")) {
            b = true;
        } else if (this.getColumnType(columnid) == 3 && o.toString().equals("")) {
            b = true;
        }
        return b;
    }

    public void sort(String sortstring) {
        this.sort(sortstring, false);
    }

    public void sort(String sortstring, boolean ignoreMaskedData) {
        DataSetComparator c = new DataSetComparator(sortstring, ignoreMaskedData);
        Collections.sort(this, c);
        this._currentsortstring = sortstring;
        if (this.isIndexing()) {
            this.getIndex().flushAllIndexes();
        }
    }

    public DataSet getFilteredDataSet(HashMap filtermap) {
        return this.getFilteredDataSet(filtermap, false);
    }

    public DataSet getFilteredDataSet(HashMap filtermap, boolean exclusive) {
        Set filterset = filtermap.entrySet();
        DataSet ds = new DataSet();
        if (this.useIndex(filtermap) && !exclusive) {
            ds = this.getIndex().getFilteredDataSet(filtermap);
        } else {
            for (HashMap rowhashmap : this) {
                Set rowset = rowhashmap.entrySet();
                if (rowset.containsAll(filterset)) {
                    if (exclusive) continue;
                    ds.add(rowhashmap);
                    continue;
                }
                if (!exclusive) continue;
                ds.add(rowhashmap);
            }
        }
        ds._columns = new LinkedList(this._columns);
        ds._coltype = new HashMap(this._coltype);
        ds._collength = this._collength;
        ds._currentsortstring = this._currentsortstring;
        ds._dateFormatters = this._dateFormatters;
        ds.isColidCaseSensitive = this.isColidCaseSensitive;
        ds.locale = this.locale;
        ds.timezone = this.timezone;
        ds.timezoneOffset = this.timezoneOffset;
        ds.m18n = this.m18n;
        ds._keycolumns = this._keycolumns;
        ds.sort(this._currentsortstring);
        if (this.isIndexing()) {
            ds.index = this.getIndex().copy(ds);
        }
        return ds;
    }

    public ArrayList<DataSet> getSplitDataSets(int maxRows) {
        if (maxRows <= 0) {
            maxRows = 1;
        }
        ArrayList<DataSet> splitList = new ArrayList<DataSet>();
        DataSet ds = this.getClonedDefinition();
        for (int i = 0; i < this.size(); ++i) {
            ds.add(this.get(i));
            if (ds.size() < maxRows) continue;
            splitList.add(ds);
            ds = this.getClonedDefinition();
        }
        if (ds.size() > 0) {
            splitList.add(ds);
        }
        return splitList;
    }

    public ArrayList<DataSet> getGroupedDataSets(String columnList) {
        ArrayList<DataSet> groupedList = new ArrayList<DataSet>();
        String[] columnid = StringUtil.split(columnList, ",");
        int columns = columnid.length;
        DataSet ds = this.getClonedDefinition();
        Object[] oldValue = new Object[columns];
        int rows = this.size();
        if (rows > 0) {
            for (int j = 0; j < columns; ++j) {
                columnid[j] = columnid[j].trim();
                oldValue[j] = this.getObject(0, columnid[j]);
            }
        }
        for (int i = 0; i < rows; ++i) {
            int j;
            boolean changed = false;
            for (j = 0; j < columns; ++j) {
                Object currentValue = this.getObject(i, columnid[j]);
                if (!(currentValue == null && oldValue[j] != null || currentValue != null && oldValue[j] == null) && (currentValue == null || currentValue.equals(oldValue[j]))) continue;
                changed = true;
                break;
            }
            if (changed) {
                groupedList.add(ds);
                ds = this.getClonedDefinition();
                for (j = 0; j < columns; ++j) {
                    oldValue[j] = this.getObject(i, columnid[j]);
                }
            }
            ds.add(this.get(i));
        }
        if (ds.size() > 0) {
            groupedList.add(ds);
        }
        return groupedList;
    }

    private DataSet getClonedDefinition() {
        DataSet ds = new DataSet();
        ds._columns = new LinkedList(this._columns);
        ds._coltype = new HashMap(this._coltype);
        ds._collength = this._collength;
        ds._currentsortstring = this._currentsortstring;
        ds._dateFormatters = this._dateFormatters;
        ds.isColidCaseSensitive = this.isColidCaseSensitive;
        ds.locale = this.locale;
        ds.timezone = this.timezone;
        ds.timezoneOffset = this.timezoneOffset;
        ds.m18n = this.m18n;
        ds._keycolumns = this._keycolumns;
        if (this.isIndexing()) {
            ds.index = this.getIndex().copy(ds);
        }
        return ds;
    }

    public int findRow(HashMap findmap, int start) {
        int index = -1;
        Set findset = findmap.entrySet();
        if (findset.size() > 0) {
            if (this.useIndex(findmap) && start == 0) {
                index = this.getIndex().findRow(findmap);
            } else {
                for (int i = start; i < this.size() && index < 0; ++i) {
                    HashMap rowhashmap = (HashMap)this.get(i);
                    Set rowset = rowhashmap.entrySet();
                    if (!rowset.containsAll(findset)) continue;
                    index = i;
                }
            }
        }
        return index;
    }

    public int findRow(HashMap findmap) {
        return this.findRow(findmap, 0);
    }

    public int findRow(String find) {
        String[] paramCount = StringUtil.split(find, "=");
        if (paramCount.length == 2) {
            return this.findRow(paramCount[0], paramCount[1]);
        }
        String[] findparams = StringUtil.split(find, ",");
        HashMap findmap = new HashMap();
        for (int i = 0; i < findparams.length; ++i) {
            String[] findparts = StringUtil.split(findparams[i].trim(), "=");
            if (findparts.length != 2) continue;
            this.populateFindMap(findparts[0], findparts[1], findmap);
        }
        return this.findRow(findmap);
    }

    public int findRow(String columnId, String value) {
        HashMap findmap = new HashMap();
        this.populateFindMap(columnId, value, findmap);
        return this.findRow(findmap);
    }

    public void populateFindMap(String columnId, String value, HashMap findmap) {
        if (columnId != null && value != null) {
            switch (this.getColumnType(columnId.trim())) {
                case 0: 
                case 3: {
                    findmap.put(columnId.trim(), value.trim());
                    break;
                }
                case 2: {
                    findmap.put(columnId.trim(), this.m18n.parseCalendar(value.trim()));
                    break;
                }
                case 1: {
                    DecimalFormat df = new DecimalFormat();
                    if (df.parse(value, new ParsePosition(0)) != null) {
                        BigDecimal bd = this.m18n.parseBigDecimal(value.trim());
                        findmap.put(columnId.trim(), bd);
                        break;
                    }
                    findmap.put(columnId.trim(), value);
                }
            }
        }
    }

    public void renameColumn(String oldColumnid, String newColumnid) {
        this._columns.remove(oldColumnid);
        this._columns.add(newColumnid);
        this._coltype.put(newColumnid, this._coltype.get(oldColumnid));
        this._coltype.remove(oldColumnid);
        this._collength.put(newColumnid, this._collength.get(oldColumnid));
        this._collength.remove(oldColumnid);
        this._dateFormatters.put(newColumnid, this._dateFormatters.get(oldColumnid));
        this._dateFormatters.remove(oldColumnid);
        int rows = this.getRowCount();
        for (int i = 0; i < rows; ++i) {
            HashMap rowdata = (HashMap)this.get(i);
            rowdata.put(newColumnid, rowdata.get(oldColumnid));
            rowdata.remove(oldColumnid);
        }
        if (this.isIndexing(oldColumnid)) {
            this.getIndex().renameIndexedColumn(oldColumnid, newColumnid);
            this.getIndex().flushIndex(oldColumnid);
        }
    }

    public void removeColumn(String oldColumnid) {
        this._columns.remove(oldColumnid);
        this._coltype.remove(oldColumnid);
        this._collength.remove(oldColumnid);
        this._dateFormatters.remove(oldColumnid);
        int rows = this.getRowCount();
        for (int i = 0; i < rows; ++i) {
            HashMap rowdata = (HashMap)this.get(i);
            rowdata.remove(oldColumnid);
        }
        if (this.isIndexing(oldColumnid)) {
            this.getIndex().flushIndex(oldColumnid);
        }
    }

    public DataSetIndex getIndex() {
        if (this.index == null) {
            this.index = new DataSetIndex(this);
        }
        return this.index;
    }

    public boolean isIndexing() {
        return this.index != null && this.index.isIndexing();
    }

    public boolean isIndexing(String column) {
        return this.index != null && this.index.isIndexing(column);
    }

    public boolean useIndex(HashMap filter) {
        return this.index != null && this.size() >= this.index.getMinimumSizeForIndexing() && this.index.useIndex(filter);
    }

    public void setKeyColumns(String columnIds) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Column ids is null");
        }
        this._keycolumns = new HashSet<String>(Arrays.asList(columnIds.split(";")));
    }

    public void setKeyColumns(Set<String> columnIds) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Column ids is null");
        }
        this._keycolumns.clear();
        HashSet<String> caseAwareColumnIds = new HashSet<String>();
        if (!this.isColidCaseSensitive) {
            for (String columnId : columnIds) {
                caseAwareColumnIds.add(columnId.toLowerCase());
            }
            this._keycolumns.addAll(caseAwareColumnIds);
        } else {
            this._keycolumns.addAll(columnIds);
        }
    }

    public Set<String> getKeyColumnSet() {
        return this._keycolumns;
    }

    public boolean isMasked(int row, String columnid) {
        boolean b = false;
        if ("Y".equals(this.getString(row, MASK_PREFIX_FLAG + columnid, "N"))) {
            b = true;
        }
        return b;
    }

    private String getMaskedString(int row, String columnid) {
        columnid = this.isColidCaseSensitive ? columnid : columnid.toLowerCase();
        String value = (String)this.getObject(row, MASK_PREFIX_VALUE + columnid);
        if (value != null && value.indexOf("#semicolon#") != -1) {
            value = value.replaceAll("#semicolon#", ";");
        }
        return value == null || value.length() == 0 ? "" : value;
    }

    public boolean setMaskedString(int row, String columnid, String value) {
        this.setObject(row, MASK_PREFIX_FLAG + columnid, "Y");
        return this.setObject(row, MASK_PREFIX_VALUE + columnid, value);
    }

    public void setForceISOFormat(boolean forceISOFormat) {
        this.forceISOFormat = forceISOFormat;
        if (forceISOFormat) {
            this.initStandardLocaleObjects();
        }
    }

    public boolean isForceISOFormat() {
        return this.forceISOFormat;
    }

    private void initStandardLocaleObjects() {
        this.isoLocaleSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        this.isoLocaleSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.isoLocaleSDF.setLenient(false);
        this.isoLocaleFU = FormatUtil.getInstance(Locale.US);
    }

    class DataSetComparator
    implements Comparator {
        private String[] _sortcolumn;
        private boolean[] _ascending;
        private int[] _forcesorttype;
        private HashMap<String, String> _sortcolumnValuelistMap;
        private int _sortcolumns;
        private int[] _sortcoltype;
        private boolean ignoreMaskedData;

        public DataSetComparator() {
        }

        public DataSetComparator(String sortstring, boolean ignoreMaskedData) {
            this.setSort(sortstring);
            this.ignoreMaskedData = ignoreMaskedData;
        }

        public void setSort(String sortstring) {
            String[] columns = StringUtil.split(sortstring, ",");
            this._sortcolumns = columns.length;
            this._sortcolumn = new String[this._sortcolumns];
            this._sortcoltype = new int[this._sortcolumns];
            this._forcesorttype = new int[this._sortcolumns];
            this._ascending = new boolean[this._sortcolumns];
            for (int i = 0; i < this._sortcolumns; ++i) {
                if (columns[i].trim().toLowerCase().endsWith(" a")) {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].substring(0, columns[i].length() - 2).trim() : columns[i].substring(0, columns[i].length() - 2).trim().toLowerCase();
                    this._ascending[i] = true;
                } else if (columns[i].trim().toLowerCase().endsWith(" d")) {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].substring(0, columns[i].length() - 2).trim() : columns[i].substring(0, columns[i].length() - 2).trim().toLowerCase();
                    this._ascending[i] = false;
                } else if (columns[i].trim().toLowerCase().endsWith(" na")) {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].substring(0, columns[i].length() - 3).trim() : columns[i].substring(0, columns[i].length() - 3).trim().toLowerCase();
                    this._ascending[i] = true;
                    this._forcesorttype[i] = 1;
                } else if (columns[i].trim().toLowerCase().endsWith(" nd")) {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].substring(0, columns[i].length() - 3).trim() : columns[i].substring(0, columns[i].length() - 3).trim().toLowerCase();
                    this._ascending[i] = false;
                    this._forcesorttype[i] = 1;
                } else if (columns[i].trim().toLowerCase().indexOf(" ") > 0) {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].substring(0, columns[i].indexOf(" ")).trim() : columns[i].substring(0, columns[i].indexOf(" ")).trim().toLowerCase();
                    this._ascending[i] = false;
                    if (this._sortcolumnValuelistMap == null) {
                        this._sortcolumnValuelistMap = new HashMap();
                    }
                    this._sortcolumnValuelistMap.put(this._sortcolumn[i], columns[i].substring(columns[i].indexOf(" ") + 1));
                } else {
                    this._sortcolumn[i] = DataSet.this.isColidCaseSensitive ? columns[i].trim() : columns[i].trim().toLowerCase();
                    this._ascending[i] = true;
                }
                this._sortcoltype[i] = DataSet.this.getColumnType(this._sortcolumn[i]);
            }
        }

        public int compare(Object object1, Object object2) {
            HashMap hm1 = (HashMap)object1;
            HashMap hm2 = (HashMap)object2;
            int compare = 0;
            boolean actualValueCompareNotReqd = false;
            for (int i = 0; i < this._sortcolumns && compare == 0; ++i) {
                actualValueCompareNotReqd = false;
                if (!this.ignoreMaskedData && (hm1.containsKey(DataSet.MASK_PREFIX_FLAG + this._sortcolumn[i]) || hm2.containsKey(DataSet.MASK_PREFIX_FLAG + this._sortcolumn[i]))) {
                    String s2MaskFlag;
                    String s1MaskFlag = (String)hm1.get(DataSet.MASK_PREFIX_FLAG + this._sortcolumn[i]);
                    if (s1MaskFlag == null) {
                        s1MaskFlag = "N";
                    }
                    if ((s2MaskFlag = (String)hm2.get(DataSet.MASK_PREFIX_FLAG + this._sortcolumn[i])) == null) {
                        s2MaskFlag = "N";
                    }
                    if ("Y".equals(s1MaskFlag) && "Y".equals(s2MaskFlag)) {
                        compare = 0;
                        actualValueCompareNotReqd = true;
                    } else if ("N".equals(s1MaskFlag) && "Y".equals(s2MaskFlag)) {
                        compare = -1;
                        actualValueCompareNotReqd = true;
                    } else if ("Y".equals(s1MaskFlag) && "N".equals(s2MaskFlag)) {
                        compare = 1;
                        actualValueCompareNotReqd = true;
                    } else {
                        actualValueCompareNotReqd = false;
                    }
                }
                if (!actualValueCompareNotReqd) {
                    switch (this._sortcoltype[i]) {
                        case 0: 
                        case 3: {
                            BigDecimal bd2;
                            BigDecimal bd1;
                            if (this._forcesorttype[i] == 1) {
                                bd1 = null;
                                bd2 = null;
                                try {
                                    bd1 = new BigDecimal((String)hm1.get(this._sortcolumn[i]));
                                }
                                catch (NumberFormatException numberFormatException) {
                                    // empty catch block
                                }
                                try {
                                    bd2 = new BigDecimal((String)hm2.get(this._sortcolumn[i]));
                                }
                                catch (NumberFormatException numberFormatException) {
                                    // empty catch block
                                }
                                if (bd1 == null && bd2 == null) {
                                    compare = 0;
                                    break;
                                }
                                if (bd1 == null) {
                                    compare = -1;
                                    break;
                                }
                                if (bd2 == null) {
                                    compare = 1;
                                    break;
                                }
                                compare = bd1.compareTo(bd2);
                                break;
                            }
                            String s1 = (String)hm1.get(this._sortcolumn[i]);
                            String s2 = (String)hm2.get(this._sortcolumn[i]);
                            if (this._sortcolumnValuelistMap != null && this._sortcolumnValuelistMap.get(this._sortcolumn[i]) != null) {
                                int s2Index;
                                if ("m".equals(this._sortcolumnValuelistMap.get(this._sortcolumn[i]))) {
                                    compare = 0;
                                    break;
                                }
                                String sortvaluelist = ";" + this._sortcolumnValuelistMap.get(this._sortcolumn[i]) + ";";
                                int s1Index = sortvaluelist.indexOf(";" + s1 + ";");
                                compare = s1Index == (s2Index = sortvaluelist.indexOf(";" + s2 + ";")) ? 0 : (s1Index < s2Index ? 1 : -1);
                                break;
                            }
                            if (s1 == null && s2 != null) {
                                compare = -1;
                                break;
                            }
                            if (s2 == null && s1 != null) {
                                compare = 1;
                                break;
                            }
                            if (s1 == null && s2 == null) {
                                compare = 0;
                                break;
                            }
                            compare = s1.compareToIgnoreCase(s2);
                            break;
                        }
                        case 1: {
                            BigDecimal bd1 = (BigDecimal)hm1.get(this._sortcolumn[i]);
                            BigDecimal bd2 = (BigDecimal)hm2.get(this._sortcolumn[i]);
                            if (bd1 == null && bd2 == null) {
                                compare = 0;
                                break;
                            }
                            if (bd1 == null) {
                                compare = -1;
                                break;
                            }
                            if (bd2 == null) {
                                compare = 1;
                                break;
                            }
                            compare = bd1.compareTo(bd2);
                            break;
                        }
                        case 2: {
                            Calendar c1 = (Calendar)hm1.get(this._sortcolumn[i]);
                            Calendar c2 = (Calendar)hm2.get(this._sortcolumn[i]);
                            compare = c1 == null && c2 == null ? 0 : (c1 == null ? -1 : (c2 == null ? 1 : new Long(c1.getTime().getTime()).compareTo(new Long(c2.getTime().getTime()))));
                        }
                    }
                }
                if (this._ascending[i]) continue;
                compare = -compare;
            }
            return compare;
        }
    }

    public static interface ResultSetRowProcessor {
        public void processRow(HashMap var1, ResultSet var2, DataSet var3);
    }
}

