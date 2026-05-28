/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.math.NumberUtils
 */
package sapphire.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.util.M18NUtil;

public class DataStore {
    private DataSet ds;
    private DataSet dsDelete;
    private DataSet dsUnlink;
    private JsonArray encrypted = new JsonArray();
    private static String __rowstatus = "__rowstatus";
    private static String __sequence = "__sequence";
    public static int INSERT = 1;
    public static int UPDATE = 2;

    public DataStore(DataSet dataSet) {
        String[] columns;
        this.ds = dataSet;
        this.prepareDataSet(this.ds);
        this.dsDelete = new DataSet();
        this.dsUnlink = new DataSet();
        for (String columnid : columns = dataSet.getColumns()) {
            this.dsDelete.addColumn(columnid, dataSet.getColumnType(columnid));
            this.dsUnlink.addColumn(columnid, dataSet.getColumnType(columnid));
        }
    }

    public DataStore(JsonObject jso) {
        this(jso, null);
    }

    public DataStore(JsonObject jso, ConnectionInfo connectionInfo) {
        try {
            M18NUtil m18NUtil;
            M18NUtil m18NUtil2 = m18NUtil = connectionInfo == null ? new M18NUtil() : new M18NUtil(connectionInfo);
            if (jso.get("data") instanceof String) {
                String dataStr = jso.getString("data");
                JSONObject dataJSON = new JSONObject(dataStr);
                this.convertJSONDataToUserFormat(dataJSON, m18NUtil);
                DataSet tempDS = new DataSet();
                tempDS.setM18NUtil(m18NUtil);
                tempDS.setJSONObject(dataJSON);
                this.ds = tempDS;
            } else {
                JSONObject dataJSON = new JSONObject(jso.getJsonObject("data").toString());
                this.convertJSONDataToUserFormat(dataJSON, m18NUtil);
                DataSet tempDS = new DataSet();
                tempDS.setM18NUtil(m18NUtil);
                tempDS.setJSONObject(dataJSON);
                this.ds = tempDS;
            }
            this.dsDelete = jso.get("deleted") instanceof String ? new DataSet(new JSONObject(jso.getString("deleted"))) : new DataSet(new JSONObject(jso.getJsonObject("deleted").toString()));
            this.dsUnlink = jso.get("unlinked") instanceof String ? new DataSet(new JSONObject(jso.getString("unlinked"))) : new DataSet(new JSONObject(jso.getJsonObject("unlinked").toString()));
            this.encrypted = jso.get("encrypted") instanceof String ? new JsonArray(jso.getString("encrypted")) : jso.getJsonArray("encrypted");
        }
        catch (ParseException | JSONException | SapphireException e) {
            e.printStackTrace();
        }
    }

    private void convertJSONDataToUserFormat(JSONObject jsonObject, M18NUtil m18NUtil) throws JSONException, ParseException {
        String[] timeZoneIndependentColumnsArr;
        JSONArray timeZoneIndependentColumns = jsonObject.optJSONArray("timezoneindependent");
        if (timeZoneIndependentColumns == null) {
            timeZoneIndependentColumnsArr = null;
        } else {
            timeZoneIndependentColumnsArr = new String[timeZoneIndependentColumns.length()];
            for (int i = 0; i < timeZoneIndependentColumnsArr.length; ++i) {
                timeZoneIndependentColumnsArr[i] = timeZoneIndependentColumns.optString(i);
            }
        }
        if (jsonObject.has("dataset") && (jsonObject.has("columnmap") || jsonObject.has("columncount"))) {
            JSONObject columns = jsonObject.getJSONObject("columns");
            JSONArray colTypes = jsonObject.getJSONArray("types");
            JSONArray rows = jsonObject.getJSONArray("dataset");
            Iterator itr = columns.keys();
            while (itr.hasNext()) {
                String val;
                JSONArray row;
                int i;
                String colId = (String)itr.next();
                int colIndex = columns.getInt(colId);
                if (2 == colTypes.getInt(colIndex)) {
                    for (i = 0; i < rows.length(); ++i) {
                        row = rows.getJSONArray(i);
                        val = row.getString(colIndex);
                        if (val == null || val.length() <= 0 || val.equals("null")) continue;
                        val = DataStore.convertDateToUserFormat(val, m18NUtil, timeZoneIndependentColumnsArr != null && Arrays.asList(timeZoneIndependentColumnsArr).contains(colId), false);
                        row.put(colIndex, val);
                    }
                    continue;
                }
                if (1 != colTypes.getInt(colIndex)) continue;
                for (i = 0; i < rows.length(); ++i) {
                    row = rows.getJSONArray(i);
                    val = row.getString(colIndex);
                    if (val == null || val.length() <= 0 || val.equals("null")) continue;
                    val = this.convertNumberToUserFormat(val, m18NUtil);
                    row.put(colIndex, val);
                }
            }
        } else {
            try {
                JSONObject columns = jsonObject.getJSONObject("columns");
                JSONArray rows = jsonObject.getJSONArray("rows");
                Iterator itr = columns.keys();
                while (itr.hasNext()) {
                    String colValue;
                    JSONObject row;
                    int i;
                    String columnId = (String)itr.next();
                    int colType = columns.getInt(columnId);
                    if (2 == colType) {
                        for (i = 0; i < rows.length(); ++i) {
                            row = rows.getJSONObject(i);
                            colValue = row.optString(columnId);
                            if (colValue == null || colValue.length() <= 0 || colValue.equals("null")) continue;
                            row.put(columnId, DataStore.convertDateToUserFormat(colValue, m18NUtil, timeZoneIndependentColumnsArr != null && Arrays.asList(timeZoneIndependentColumnsArr).contains(columnId), false));
                        }
                        continue;
                    }
                    if (1 != colType) continue;
                    for (i = 0; i < rows.length(); ++i) {
                        row = rows.getJSONObject(i);
                        colValue = row.optString(columnId);
                        if (colValue == null || colValue.length() <= 0 || colValue.equals("null")) continue;
                        row.put(columnId, this.convertNumberToUserFormat(colValue, m18NUtil));
                    }
                }
            }
            catch (Exception e) {
                Trace.logError("Exception occurred when trying to convert JSON data to user format.", e);
            }
        }
    }

    public static String convertDateToUserFormat(String dateVal, M18NUtil userM18NUtil, boolean isTimeZoneIndependent, boolean isDateOnly) throws ParseException {
        if (dateVal == null || dateVal.equals("") || dateVal.equals("{}")) {
            return "";
        }
        String formattedDt = dateVal;
        GregorianCalendar cal = null;
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(dateVal);
        if (matcher.matches()) {
            cal = (GregorianCalendar)Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(Long.parseLong(dateVal));
            formattedDt = userM18NUtil.format(cal, !isTimeZoneIndependent);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            sdf.setLenient(false);
            Date dt = null;
            try {
                dt = sdf.parse(dateVal);
                cal = (GregorianCalendar)Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTime(dt);
                formattedDt = isDateOnly ? userM18NUtil.formatDateOnly(cal, !isTimeZoneIndependent) : userM18NUtil.format(cal, !isTimeZoneIndependent);
            }
            catch (ParseException e) {
                Trace.logError("Skip Convert Date from ISO. Date not provided in ISO format: " + dateVal);
            }
        }
        return formattedDt;
    }

    private String convertNumberToUserFormat(String numVal, M18NUtil userM18NUtil) {
        if (numVal == null || numVal.length() == 0) {
            return "";
        }
        if (!NumberUtils.isParsable((String)(numVal = numVal.replace(",", ".")))) {
            return "";
        }
        String formattedVal = numVal;
        try {
            BigDecimal numBD = new BigDecimal(numVal);
            formattedVal = userM18NUtil.format(numBD);
        }
        catch (Exception e) {
            Trace.logWarn("Skip Convert Number to BigDecimal due to exception for value: " + numVal, e);
        }
        return formattedVal;
    }

    private void prepareDataSet(DataSet ds) {
        ds.setNumber(-1, __rowstatus, 0);
        ds.setSequence(__sequence);
    }

    public JsonObject toJsonObject() throws SapphireException {
        JsonObject jso = new JsonObject();
        jso.put("data", this.ds.toJSONString(true, true, true));
        jso.put("deleted", this.dsDelete.toJSONString(true, true, true));
        jso.put("unlinked", this.dsUnlink.toJSONString(true, true, true));
        jso.put("encrypted", this.encrypted);
        return jso;
    }

    public void setRowInsert(int row) {
        this.ds.setNumber(row, __rowstatus, INSERT);
    }

    public void setRowUpdate(int row) {
        this.ds.setNumber(row, __rowstatus, UPDATE);
    }

    private void internalSetRowUpdate(int row) {
        if (row == -1) {
            for (int i = 0; i < this.ds.size(); ++i) {
                this.internalSetRowUpdate(i);
            }
        } else if (!this.isRowInsert(row)) {
            this.ds.setNumber(row, __rowstatus, UPDATE);
        }
    }

    public boolean isRowInsert(int row) {
        return this.ds.getInt(row, __rowstatus) == INSERT;
    }

    public boolean isRowUpdate(int row) {
        return this.ds.getInt(row, __rowstatus) == UPDATE;
    }

    public DataSet getDataSet() {
        return this.ds;
    }

    public DataSet getInsertDataSet() {
        HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
        filter.put(__rowstatus, new BigDecimal(INSERT));
        DataSet insert = this.getFilteredDataSet(filter);
        return insert;
    }

    public DataSet getUpdateDataSet() {
        HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
        filter.put(__rowstatus, new BigDecimal(UPDATE));
        DataSet update = this.getFilteredDataSet(filter);
        return update;
    }

    public DataSet getDeleteDataSet() {
        DataSet delete = this.dsDelete.copy();
        return delete;
    }

    public void deleteRow(int row) {
        if (this.isRowInsert(row)) {
            this.ds.deleteRow(row);
        } else {
            this.dsDelete.copyRow(this.ds, row, 1);
            this.ds.deleteRow(row);
        }
    }

    public DataSet getUnlinkDataSet() {
        return this.dsUnlink.copy();
    }

    public void unlinkRow(int row) {
        if (!this.isRowInsert(row)) {
            this.dsUnlink.copyRow(this.ds, row, 1);
        }
        this.ds.deleteRow(row);
    }

    public int addRow() {
        int row = this.ds.addRow();
        this.ds.setNumber(row, __rowstatus, INSERT);
        return row;
    }

    public int addRow(int newrow) {
        int row = this.ds.addRow(newrow);
        this.setRowInsert(row);
        return row;
    }

    public int getRowCount() {
        return this.ds.size();
    }

    public int size() {
        return this.ds.size();
    }

    public boolean addColumn(String columnid, int type) {
        return this.ds.addColumn(columnid, type);
    }

    public int getColumnCount() {
        return this.ds.getColumnCount();
    }

    public String[] getColumns() {
        return this.ds.getColumns();
    }

    public int getColumnType(String columnid) {
        return this.ds.getColumnType(columnid);
    }

    public String getColumnValues(String columnid, int startrow, int endrow, String delimeter) {
        return this.ds.getColumnValues(columnid, startrow, endrow, delimeter);
    }

    public boolean setObject(int row, String columnid, Object o) {
        this.internalSetRowUpdate(row);
        return this.ds.setObject(row, columnid, o);
    }

    public boolean setString(int row, String columnid, String value) {
        this.internalSetRowUpdate(row);
        return this.ds.setString(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, int value) {
        this.internalSetRowUpdate(row);
        return this.ds.setNumber(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, long value) {
        this.internalSetRowUpdate(row);
        return this.ds.setNumber(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, double value) {
        this.internalSetRowUpdate(row);
        return this.ds.setNumber(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, BigDecimal value) {
        this.internalSetRowUpdate(row);
        return this.ds.setNumber(row, columnid, value);
    }

    public boolean setNumber(int row, String columnid, String value) {
        this.internalSetRowUpdate(row);
        return this.ds.setNumber(row, columnid, value);
    }

    public boolean setDate(int row, String columnid, long value) {
        this.internalSetRowUpdate(row);
        return this.ds.setDate(row, columnid, value);
    }

    public boolean setDate(int row, String columnid, String value) {
        this.internalSetRowUpdate(row);
        return this.ds.setDate(row, columnid, value);
    }

    public boolean setDate(int row, String columnid, Calendar value) {
        this.internalSetRowUpdate(row);
        return this.ds.setDate(row, columnid, value);
    }

    public boolean setDate(int row, String columnid, Timestamp value) {
        this.internalSetRowUpdate(row);
        return this.ds.setDate(row, columnid, value);
    }

    public boolean setValue(int row, String columnid, String value) {
        this.internalSetRowUpdate(row);
        return this.ds.setValue(row, columnid, value);
    }

    public DateFormat getDateDisplayFormat(String columnid) {
        return this.ds.getDateDisplayFormat(columnid);
    }

    public void setTimeZoneInsensitive(String columnid) {
        this.ds.setTimeZoneInsensitive(columnid);
    }

    public void setM18NUtil(M18NUtil m18NUtil) {
        this.ds.setM18NUtil(m18NUtil);
    }

    public M18NUtil getM18n() {
        return this.ds.getM18n();
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.ds.setConnectionInfo(connectionInfo);
    }

    public boolean setSequence(String columnid) {
        this.internalSetRowUpdate(-1);
        return this.ds.setSequence(columnid);
    }

    public String getString(int row, String columnid) {
        return this.ds.getString(row, columnid);
    }

    public String getString(int row, String columnid, String defaultvalue) {
        return this.ds.getString(row, columnid, defaultvalue);
    }

    public BigDecimal getBigDecimal(int row, String columnid) {
        return this.ds.getBigDecimal(row, columnid);
    }

    public BigDecimal getBigDecimal(int row, String columnid, BigDecimal defaultvalue) {
        return this.ds.getBigDecimal(row, columnid, defaultvalue);
    }

    public double getDouble(int row, String columnid) {
        return this.ds.getDouble(row, columnid);
    }

    public double getDouble(int row, String columnid, double defaultvalue) {
        return this.ds.getDouble(row, columnid, defaultvalue);
    }

    public int getInt(int row, String columnid) {
        return this.ds.getInt(row, columnid);
    }

    public int getInt(int row, String columnid, int defaultvalue) {
        return this.ds.getInt(row, columnid, defaultvalue);
    }

    public long getLong(int row, String columnid) {
        return this.ds.getLong(row, columnid);
    }

    public Calendar getCalendar(int row, String columnid) {
        return this.ds.getCalendar(row, columnid);
    }

    public Calendar getCalendar(int row, String columnid, Calendar defaultvalue) {
        return this.ds.getCalendar(row, columnid, defaultvalue);
    }

    public Object getObject(int row, String columnid) {
        return this.ds.getObject(row, columnid);
    }

    public String getValue(int row, String columnid) {
        return this.ds.getValue(row, columnid);
    }

    public String getValue(int row, String columnid, String nullvalue) {
        return this.ds.getValue(row, columnid, nullvalue);
    }

    public boolean isNull(int row, String columnid) {
        return this.ds.isNull(row, columnid);
    }

    public void sort(String sortstring) {
        this.ds.sort(sortstring);
    }

    public DataSet getFilteredDataSet(HashMap filtermap) {
        DataSet filteredDataSet = this.ds.getFilteredDataSet(filtermap);
        return filteredDataSet;
    }

    public DataSet getFilteredDataSet(HashMap filtermap, boolean exclusive) {
        DataSet filteredDataSet = this.ds.getFilteredDataSet(filtermap, exclusive);
        return filteredDataSet;
    }

    public ArrayList<DataSet> getGroupedDataSets(String columnList) {
        ArrayList<DataSet> groupedDataSets = this.ds.getGroupedDataSets(columnList);
        return groupedDataSets;
    }

    public int findRow(HashMap findmap, int start) {
        return this.ds.findRow(findmap, start);
    }

    public int findRow(HashMap findmap) {
        return this.ds.findRow(findmap);
    }

    public int findRow(String columnId, String value) {
        return this.ds.findRow(columnId, value);
    }

    public void setEncrypted(String columnid) {
        this.encrypted.put(columnid);
    }

    public JsonArray getEncryptedColumns() {
        return this.encrypted;
    }
}

