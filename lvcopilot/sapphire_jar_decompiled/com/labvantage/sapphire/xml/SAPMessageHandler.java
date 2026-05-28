/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.error.ErrorDetail;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SAPMessageHandler
extends SapphireSaxHandler {
    private StringBuffer currentElementChars = new StringBuffer();
    private DataSet tempData = new DataSet();
    private HashMap actionProps = new HashMap();
    private String parseFlag = "";
    private static final String ZHEADER = "ZHeader";
    private static final String MSGID = "MsgID";
    private static final String MSGNAME = "MsgName";
    private static final String MSGVERSION = "MsgVersion";
    private static final String ZDATA = "ZData";
    private static final String ZCOLUMN = "ZColumn";
    private static final String KEY = "Key";
    private static final String REF = "Ref";
    private static final String PARAMNAME = "ParamName";
    private static final String COLUMNKEY = "ColumnKey";
    private static final String COLUMNVAL = "ColumnVal";
    private static final String COLUMNTYPE = "ColumnType";
    private String msglogId = null;
    private String tempColName;
    private String tempColVal;
    private String tempBlockName;
    private DataSet msgColMapDS1;
    private DataSet msgQuery;
    private HashMap datasetList;
    private String sapResponse;

    public String getSAPResponse() {
        return this.sapResponse;
    }

    public void setMsglogId(String msglogId) {
        this.msglogId = msglogId;
    }

    @Override
    public void startDocument() throws SAXException {
        this.tempData = new DataSet();
        this.datasetList = new HashMap();
    }

    @Override
    public void endDocument() throws SAXException {
        DataSet actionProps;
        DataSet actions;
        try {
            this.joinDataSet();
        }
        catch (SapphireException e) {
            throw new SAXException(e.getMessage());
        }
        try {
            actions = this.getActionList();
            actionProps = this.getActionPropList();
        }
        catch (SapphireException e) {
            throw new SAXException(e);
        }
        if (actions == null || actionProps == null || actions.getRowCount() <= 0 || actionProps.getRowCount() <= 0) {
            return;
        }
        try {
            int i;
            ActionProcessor ap = new ActionProcessor(this.connectionid);
            ActionBlock actionBlock = new ActionBlock();
            for (i = 0; i < actions.getRowCount(); ++i) {
                actionBlock.setAction(Integer.toString(actions.getInt(i, "actioninstance")), actions.getString(i, "actionid"), actions.getString(i, "actionversionid"));
                HashMap<String, Object> filter = new HashMap<String, Object>();
                filter.put("actionid", actions.getString(i, "actionid"));
                filter.put("actionversionid", actions.getString(i, "actionversionid"));
                filter.put("actioninstance", new BigDecimal(actions.getValue(i, "actioninstance")));
                DataSet ds = actionProps.getFilteredDataSet(filter);
                HashMap<String, String> acProps = new HashMap<String, String>();
                for (int j = 0; j < ds.getRowCount(); ++j) {
                    String propertyid = ds.getString(j, "propertyid");
                    String propertyValue = ds.getString(j, "propertyvalue");
                    if (propertyValue.startsWith("{")) {
                        String tempPropertyValue = propertyValue.substring(propertyValue.indexOf("{") + 1, propertyValue.indexOf("}"));
                        String[] temPropArrDS = StringUtil.split(tempPropertyValue, ".");
                        DataSet ds1 = (DataSet)this.datasetList.get(temPropArrDS[0]);
                        if (ds1 != null) {
                            acProps.put(propertyid, ds1.getColumnValues(temPropArrDS[1], ";"));
                            continue;
                        }
                        throw new SAXException("Invalid configuration.");
                    }
                    acProps.put(propertyid, propertyValue);
                }
                actionBlock.setActionProperties(Integer.toString(actions.getInt(i, "actioninstance")), acProps);
            }
            ap.processActionBlock(actionBlock);
            for (i = 0; i < actionBlock.getActionCount(); ++i) {
                String sapResponse = actionBlock.getActionProperty(i, "sapresponse");
                if (sapResponse.length() <= 0) continue;
                this.sapResponse = sapResponse;
                Trace.log("Got a response from the actions:" + sapResponse);
                break;
            }
        }
        catch (ActionException e) {
            ErrorDetail errorDetail;
            Trace.logError("Error Processing actionblock for SAP Message:" + e.getMessage());
            String message = e.getMessage();
            if (e.getErrorHandler().hasErrors() && (message = (errorDetail = (ErrorDetail)e.getErrorHandler().get(0)).getMessage()).endsWith("|")) {
                message = message.substring(0, message.lastIndexOf("|"));
            }
            throw new SAXException(message);
        }
        catch (Exception e) {
            Trace.logError("Unexpected exception:" + e.getMessage());
            throw new SAXException("Unexpected Exception:" + e.getMessage());
        }
    }

    private void joinDataSet() throws SapphireException {
        DataSet dsJoin = this.getJoinList();
        DataSet mainCriteriaDS = this.getCriteriaList();
        if (dsJoin.getRowCount() <= 0) {
            return;
        }
        for (int i = 0; i < dsJoin.getRowCount(); ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sapjoinid", dsJoin.getString(i, "sapjoinid"));
            DataSet tempDataSet = new DataSet();
            DataSet criteriaDS = mainCriteriaDS.getFilteredDataSet(filter);
            ArrayList<String> participatingDSList = new ArrayList<String>();
            for (int j = 0; j < criteriaDS.getRowCount(); ++j) {
                String condition = criteriaDS.getString(j, "leftsapqueryid") + ";" + criteriaDS.getString(j, "rightsapqueryid");
                if (participatingDSList.isEmpty()) {
                    participatingDSList.add(condition);
                    continue;
                }
                if (participatingDSList.contains(condition)) continue;
                participatingDSList.add(condition);
            }
            Iterator it = participatingDSList.iterator();
            while (it.hasNext()) {
                boolean index = false;
                String[] arr = ((String)it.next()).split(";");
                filter = new HashMap();
                filter.put("leftsapqueryid", arr[0]);
                filter.put("rightsapqueryid", arr[1]);
                if (!index) {
                    tempDataSet = this.cartesian((DataSet)this.datasetList.get(arr[0]), (DataSet)this.datasetList.get(arr[1]), criteriaDS, true, arr[0], arr[1]);
                    continue;
                }
                tempDataSet = this.cartesian(tempDataSet, (DataSet)this.datasetList.get("rightsapqueryid"), criteriaDS, false, arr[0], arr[1]);
            }
            tempDataSet = this.filterDataSet(tempDataSet, dsJoin.getString(i, "sapjoinid"));
            this.datasetList.put(dsJoin.getString(i, "sapjoinid"), tempDataSet);
        }
    }

    private DataSet filterDataSet(DataSet tempDataSet, String joinId) throws SapphireException {
        int i;
        DataSet filterDS = this.getFilterConfiguration(joinId);
        DataSet finalDS = new DataSet();
        if (filterDS.getRowCount() <= 0) {
            return tempDataSet;
        }
        for (i = 0; i < filterDS.getRowCount(); ++i) {
            String queryid = filterDS.getString(i, "sapqueryid");
            String columnid = filterDS.getString(i, "sapcolumnid");
            finalDS.addColumn(columnid, tempDataSet.getColumnType(queryid + "." + columnid));
        }
        for (i = 0; i < tempDataSet.getRowCount(); ++i) {
            finalDS.addRow();
            for (int j = 0; j < filterDS.getRowCount(); ++j) {
                String queryid = filterDS.getString(j, "sapqueryid");
                String columnid = filterDS.getString(j, "sapcolumnid");
                String value = tempDataSet.getValue(i, queryid + "." + columnid);
                finalDS.setValue(i, columnid, value);
            }
        }
        return finalDS;
    }

    private DataSet getFilterConfiguration(String joinId) throws SapphireException {
        DataSet filterDS;
        StringBuffer sql = new StringBuffer("Select * from sapmsgjoinfilter where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("' and sapjoinid='");
        sql.append(joinId);
        sql.append("'");
        try {
            this._dbu.createResultSet(sql.toString());
            filterDS = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw new SapphireException("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"), e);
        }
        return filterDS;
    }

    private DataSet cartesian(DataSet leftDataSet, DataSet rightDataSet, DataSet criteriaDS, boolean joinfirst, String leftDSName, String rightDSName) {
        int i;
        DataSet tempDS = new DataSet();
        String[] columnNames = leftDataSet.getColumns();
        for (i = 0; i < columnNames.length; ++i) {
            if (joinfirst) {
                tempDS.addColumn(leftDSName + "." + columnNames[i], leftDataSet.getColumnType(columnNames[i]));
                continue;
            }
            tempDS.addColumn(columnNames[i], leftDataSet.getColumnType(columnNames[i]));
        }
        columnNames = rightDataSet.getColumns();
        for (i = 0; i < columnNames.length; ++i) {
            tempDS.addColumn(rightDSName + "." + columnNames[i], rightDataSet.getColumnType(columnNames[i]));
        }
        int newIndex = 0;
        for (int i2 = 0; i2 < leftDataSet.getRowCount(); ++i2) {
            for (int j = 0; j < rightDataSet.getRowCount(); ++j) {
                String value;
                int icopy;
                boolean flag = true;
                for (int k = 0; k < criteriaDS.getRowCount(); ++k) {
                    String leftvalue = null;
                    if (joinfirst) {
                        leftvalue = leftDataSet.getValue(i2, criteriaDS.getString(k, "leftsapcolumnid"));
                    } else {
                        leftDataSet.getValue(i2, leftDSName + "." + criteriaDS.getString(k, "leftsapcolumnid"));
                    }
                    String rightvalue = rightDataSet.getValue(j, criteriaDS.getString(k, "rightsapcolumnid"));
                    if (leftvalue.equals(rightvalue)) continue;
                    flag = false;
                    break;
                }
                if (!flag) continue;
                tempDS.addRow();
                columnNames = leftDataSet.getColumns();
                for (icopy = 0; icopy < columnNames.length; ++icopy) {
                    value = leftDataSet.getValue(i2, columnNames[icopy]);
                    if (joinfirst) {
                        tempDS.setValue(newIndex, leftDSName + "." + columnNames[icopy], value);
                        continue;
                    }
                    tempDS.setValue(newIndex, columnNames[icopy], value);
                }
                columnNames = rightDataSet.getColumns();
                for (icopy = 0; icopy < columnNames.length; ++icopy) {
                    value = rightDataSet.getValue(j, columnNames[icopy]);
                    if (!joinfirst) continue;
                    tempDS.setValue(newIndex, rightDSName + "." + columnNames[icopy], value);
                }
                ++newIndex;
            }
        }
        return tempDS;
    }

    private DataSet getCriteriaList() {
        DataSet actionList = null;
        StringBuffer sql = new StringBuffer("Select * from sapmsgjoincriteria where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("'");
        try {
            this._dbu.createResultSet(sql.toString());
            actionList = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
        }
        return actionList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        if (ZHEADER.equals(qName)) {
            this.parseFlag = "headerMessage";
        } else if (ZDATA.equals(qName)) {
            this.parseFlag = "dataSection";
            if (this.tempData != null && this.tempData.getRowCount() > 0) {
                this.tempData.deleteRow(0);
                this.tempData.reset();
            }
            this.tempData.addColumn("__key", 0);
            this.tempData.addColumn("__ref", 0);
            this.tempData.addRow();
            this.tempData.setString(0, "__key", "");
            this.tempData.setString(0, "__ref", "");
        } else if (ZCOLUMN.equals(qName)) {
            this.tempColName = "<NULL>";
            this.tempColVal = "<NULL>";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (MSGID.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.actionProps.put("msginstanceid", this.currentElementChars.toString());
        } else if (MSGNAME.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.actionProps.put("sapmsgtypeid", this.currentElementChars.toString());
        } else if (MSGVERSION.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.actionProps.put("sapmsgtypeversionid", this.currentElementChars.toString());
        } else if (ZHEADER.equals(qName) && this.parseFlag.equals("headerMessage")) {
            if (this.actionProps.get("msginstanceid") == null || this.actionProps.get("msginstanceid").toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgID");
            }
            if (this.actionProps.get("sapmsgtypeid") == null || this.actionProps.get("sapmsgtypeid").toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgName");
            }
            if (this.actionProps.get("sapmsgtypeversionid") == null || this.actionProps.get("sapmsgtypeversionid").toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgVersion");
            }
            try {
                this.getMsgConfiguration();
                this.updateMsgLogEntry();
            }
            catch (SapphireException e) {
                throw new SAXException("Failed to read header message:" + e.getMessage());
            }
        } else if (KEY.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.tempData.setString(0, "__key", this.currentElementChars.toString());
        } else if (REF.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.tempData.setString(0, "__ref", this.currentElementChars.toString());
        } else if (PARAMNAME.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.tempBlockName = this.currentElementChars.toString();
        } else if (COLUMNKEY.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.tempColName = this.currentElementChars.toString();
            if (this.tempColName == null || this.tempColName.length() == 0) {
                throw new SAXException("ColumnKey is null or empty");
            }
        } else if (COLUMNVAL.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.tempColVal = this.currentElementChars.toString();
        } else if (COLUMNTYPE.equals(qName) && this.parseFlag.equals("dataSection")) {
            int coltype;
            String tempcoltype = this.currentElementChars.toString();
            int n = "STRING".equals(tempcoltype) ? 0 : ("NUMBER".equals(tempcoltype) ? 1 : (coltype = "DATE".equals(tempcoltype) ? 2 : -1));
            if (this.tempColName.equals("<NULL>")) {
                throw new SAXException("ColumnKey is null or empty");
            }
            if (this.tempColVal.equals("<NULL>")) {
                throw new SAXException("ColumnVal and ColumnKey should be defined before ColumnType");
            }
            try {
                this.tempData.addColumn(this.tempColName, coltype);
                this.tempData.setValue(0, this.tempColName, this.tempColVal);
            }
            catch (Exception e) {
                Trace.log("Error evaluating input data for column:" + this.tempColName);
                throw new SAXException("Error evaluating input data for column:" + this.tempColName);
            }
        } else if (ZDATA.equals(qName) && this.parseFlag.equals("dataSection")) {
            for (int i = 0; i < this.msgQuery.getRowCount(); ++i) {
                DataSet ds1;
                if (!this.msgQuery.getString(i, "datablockname").equals(this.tempBlockName)) continue;
                String queryid = this.msgQuery.getString(i, "sapqueryid");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sapqueryid", queryid);
                DataSet filterDS = this.msgColMapDS1.getFilteredDataSet(filter);
                if (!this.datasetList.containsKey(this.msgQuery.getString(i, "sapqueryid"))) {
                    ds1 = this.createDataSet(filterDS);
                    this.datasetList.put(queryid, ds1);
                } else {
                    ds1 = (DataSet)this.datasetList.get(this.msgQuery.getString(i, "sapqueryid"));
                }
                this.addRowToDataSet(ds1, filterDS);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    private void addRowToDataSet(DataSet ds1, DataSet filterDS) {
        ds1.addRow();
        for (int i = 0; i < this.tempData.getColumnCount(); ++i) {
            String colId = null;
            if (this.tempData.getColumnId(i).equals("__key") || this.tempData.getColumnId(i).equals("__ref")) {
                ds1.setString(ds1.getRowCount() - 1, this.tempData.getColumnId(i), this.tempData.getString(0, this.tempData.getColumnId(i)));
                continue;
            }
            for (int j = 0; j < filterDS.getRowCount(); ++j) {
                if (!this.tempData.getColumnId(i).equals(filterDS.getString(j, "externalcolumnid").toLowerCase())) continue;
                colId = filterDS.getString(j, "sapcolumnid");
                break;
            }
            ds1.setValue(ds1.getRowCount() - 1, colId, this.tempData.getValue(0, this.tempData.getColumnId(i)));
        }
    }

    private DataSet createDataSet(DataSet filterDS) {
        DataSet ds1 = new DataSet();
        for (int i = 0; i < this.tempData.getColumnCount(); ++i) {
            String columnName = null;
            String tempColumnName = this.tempData.getColumnId(i);
            if (tempColumnName.equals("__key") || tempColumnName.equals("__ref")) {
                columnName = tempColumnName;
            } else {
                for (int j = 0; j < filterDS.getRowCount(); ++j) {
                    if (!filterDS.getString(j, "externalcolumnid").equalsIgnoreCase(tempColumnName)) continue;
                    columnName = filterDS.getString(j, "sapcolumnid");
                    break;
                }
            }
            if (columnName == null) continue;
            ds1.addColumn(columnName, this.tempData.getColumnType(this.tempData.getColumnId(i)));
        }
        return ds1;
    }

    private boolean matchCol(DataSet filterDS) {
        boolean flag = true;
        for (int indexi = 0; indexi < filterDS.getRowCount(); ++indexi) {
            for (int indexj = 0; indexj < this.tempData.getColumnCount(); ++indexj) {
                flag = false;
                if (!this.tempData.getColumnId(indexj).equalsIgnoreCase(filterDS.getString(indexi, "externalcolumnid"))) continue;
                flag = true;
                break;
            }
            if (flag) continue;
            return flag;
        }
        return flag;
    }

    private void updateMsgLogEntry() throws SapphireException {
        try {
            if (this.msglogId == null) {
                return;
            }
            ActionProcessor ap = new ActionProcessor(this.connectionid);
            this.actionProps.put("sdcid", "LV_SAPMsgLog");
            this.actionProps.put("keyid1", this.msglogId);
            this.actionProps.put("processstatus", "InProgress");
            ap.processAction("EditSDI", "1", this.actionProps, true);
        }
        catch (SapphireException se) {
            Trace.logError("Fail to save log for the file");
            throw se;
        }
    }

    private void getMsgConfiguration() throws SapphireException {
        StringBuffer sql = new StringBuffer("Select * from sapmsgquerycolumnmap  where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("' order by sapqueryid ");
        try {
            this._dbu.createResultSet(sql.toString());
            this.msgColMapDS1 = new DataSet(this._dbu.getResultSet());
            if (this.msgColMapDS1.getRowCount() < 1) {
                Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
                throw new SapphireException("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            }
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw e;
        }
        sql.delete(0, sql.length());
        sql = new StringBuffer("select sapqueryid, datablockname  from sapmsgquery where sapmsgtypeid='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("' order by sapquerysequence");
        try {
            this._dbu.createResultSet(sql.toString());
            this.msgQuery = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw e;
        }
    }

    private DataSet getActionList() throws SapphireException {
        DataSet actionList;
        StringBuffer sql = new StringBuffer("Select * from sapmsgaction where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("'");
        sql.append(" order by actioninstance");
        try {
            this._dbu.createResultSet(sql.toString());
            actionList = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw new SapphireException("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"), e);
        }
        return actionList;
    }

    private DataSet getActionPropList() throws SapphireException {
        DataSet actionList;
        StringBuffer sql = new StringBuffer("Select * from sapmsgactionproperty where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("'");
        try {
            this._dbu.createResultSet(sql.toString());
            actionList = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw new SapphireException("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"), e);
        }
        return actionList;
    }

    private DataSet getJoinList() throws SapphireException {
        DataSet actionList = null;
        StringBuffer sql = new StringBuffer("Select * from sapmsgjoin where sapmsgtypeid ='");
        sql.append(this.actionProps.get("sapmsgtypeid"));
        sql.append("' and sapmsgtypeversionid='");
        sql.append(this.actionProps.get("sapmsgtypeversionid"));
        sql.append("'");
        try {
            this._dbu.createResultSet(sql.toString());
            actionList = new DataSet(this._dbu.getResultSet());
        }
        catch (SapphireException e) {
            Trace.logError("Fail to get configuration for msgtype=" + this.actionProps.get("msgtypeid") + "and msgtypeversionid=" + this.actionProps.get("msgtypeversionid"));
            throw e;
        }
        return actionList;
    }
}

