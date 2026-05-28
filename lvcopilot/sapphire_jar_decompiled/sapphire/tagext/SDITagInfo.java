/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.tagext.BaseTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class SDITagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "sdiinfo";
    private String sdcid;
    private HashMap _querydatamap = null;
    private HashMap _childtagdata = new HashMap();
    private String _sdierror = "";
    private String[] keycols;
    private SDIData sdiData;
    private SDIRequest sdiRequest;
    private PageContext pageContext;

    public SDITagInfo(HashMap querydatamap) {
        this._querydatamap = querydatamap;
    }

    public SDIData getSDIData() {
        return this.sdiData;
    }

    public void setSDIData(SDIData sdiData) {
        this.sdiData = sdiData;
    }

    public SDIRequest getSDIRequest() {
        return this.sdiRequest;
    }

    public void setSDIRequest(SDIRequest sdiRequest) {
        this.sdiRequest = sdiRequest;
    }

    public PageContext getPageContext() {
        return this.pageContext;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setError(String error) {
        this._sdierror = error;
    }

    public String getError() {
        return this._sdierror;
    }

    public String[] getKeycols() {
        return this.keycols;
    }

    public void setKeycols(String[] keycols) {
        this.keycols = keycols;
    }

    public int getRowCount() {
        return this.getRowCount("primary");
    }

    public int getRowCount(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getRowCount();
    }

    public int getCurrentRow() {
        return this.getCurrentRow("primary");
    }

    public int getCurrentRow(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getCurrentRow();
    }

    public int getColumnCount(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getColumnCount();
    }

    public int getCurrentCol(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getCurrentCol();
    }

    public String getCurrentColId(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getColumnId();
    }

    public Object getObject(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getObject(qd.getCurrentRow(), columnid);
    }

    public Object getObject(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getObject(row, columnid);
    }

    public String getValue(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getValue(qd.getCurrentRow(), columnid, qd.getNullValue());
    }

    public String getValue(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getValue(row, columnid, qd.getNullValue());
    }

    public String getString(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getString(qd.getCurrentRow(), columnid);
    }

    public String getString(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getString(row, columnid);
    }

    public int getInt(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getInt(qd.getCurrentRow(), columnid);
    }

    public int getInt(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getInt(row, columnid);
    }

    public BigDecimal getBigDecimal(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getBigDecimal(qd.getCurrentRow(), columnid);
    }

    public BigDecimal getBigDecimal(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getBigDecimal(row, columnid);
    }

    public Calendar getCalendar(String dataset, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getCalendar(qd.getCurrentRow(), columnid);
    }

    public Calendar getCalendar(String dataset, int row, String columnid) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getQuerydata().getCalendar(row, columnid);
    }

    public int findRow(String dataset, String find) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.findRow(find);
    }

    public String getRowStatus(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getRowStatus(qd.getCurrentRow());
    }

    public String getRowId(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.getRowId(qd.getCurrentRow());
    }

    public DataSet getDataSet(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd != null ? qd.getQuerydata() : null;
    }

    public boolean isTemplateRow() {
        return this.isTemplateRow("primary");
    }

    public boolean isGrouping(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.isGrouping();
    }

    public boolean isTemplateRow(String dataset) {
        QueryData qd = (QueryData)this._querydatamap.get(dataset);
        return qd.isTemplateRow();
    }

    public QueryData getQueryData(String dataset) {
        return (QueryData)this._querydatamap.get(dataset);
    }

    public void setChildTagData(String dataname, Object data) {
        this._childtagdata.put(dataname, data);
    }

    public Object getChildTagData(String dataname) {
        return this._childtagdata.get(dataname);
    }

    public void setDataSet(String datasetName, DataSet dataset) {
        if (!dataset.isValidColumn("__rowstatus")) {
            dataset.addColumn("__rowstatus", 0);
            dataset.setString(-1, "__rowstatus", "S");
        }
        if (!dataset.isValidColumn("__rowid")) {
            dataset.addColumn("__rowid", 0);
            for (int row = 0; row < dataset.getRowCount(); ++row) {
                dataset.setString(row, "__rowid", "" + row);
            }
        }
        if (this._querydatamap.containsKey(datasetName)) {
            this.getQueryData(datasetName).setQueryData(dataset);
        } else {
            QueryData qd = new QueryData(datasetName, dataset);
            this._querydatamap.put(datasetName, qd);
        }
        this.getSDIData().setDataset(datasetName, dataset);
    }
}

