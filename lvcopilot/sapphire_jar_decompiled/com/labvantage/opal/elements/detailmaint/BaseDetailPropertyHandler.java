/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.detailmaint;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sdidata.EnterDataItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseDetailPropertyHandler
extends BasePropertyHandler {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 90260 $";
    public static final int MAXTABLENAMELENGTH = 20;
    public static final String COLUMN_DATATYPE_CHAR = "C";
    public static final String COLUMN_DATATYPE_DATE = "D";
    public static final String COLUMN_DATATYPE_NUMERIC = "N";
    public static final String COLUMN_DATATYPE_REAL = "R";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String COLUMN_SDCID = "sdcid";
    public static final String COLUMN_KEYID1 = "keyid1";
    public static final String COLUMN_KEYID2 = "keyid2";
    public static final String COLUMN_KEYID3 = "keyid3";
    public static final String COLUMN_MODDT = "moddt";
    public static final String COLUMN_MODBY = "modby";
    public static final String COLUMN_MODTOOL = "modtool";
    public static final String COLUMN_USERSEQUENCE = "usersequence";
    private String __SdcId;
    private String __Keyid1;
    private String __Keyid2;
    private String __Keyid3;
    private static List<String> __CoreColumnsList = new ArrayList<String>();
    protected HashMap _Props;
    protected String _ExtraProps;
    protected String _AuditReason;
    protected String _AuditActivity;
    protected String _AuditSignedFlag;
    protected String _TraceLogId;
    protected HashMap _ElementProps;
    private List _ActionItems;
    protected TableMetaData _TableMD;
    protected String _Edata;
    protected String _Ecolumns;
    protected String _Modified;
    protected String _ElementId;
    protected String _SdcRuleConfirm = "N";
    protected static List<String> updateExcludeColumnList;
    Map masterKeyValueMap;

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        this._Props = props;
        this._ElementProps = BaseDetailPropertyHandler.filterProps(props);
        this._ActionItems = new ArrayList();
        this.__SdcId = this.getProperty(COLUMN_SDCID);
        this.__Keyid1 = this.getProperty(COLUMN_KEYID1);
        this.__Keyid2 = this.getProperty(COLUMN_KEYID2);
        this.__Keyid3 = this.getProperty(COLUMN_KEYID3);
        this._ElementId = (String)props.get("__propertyhandler_elementid");
        this._ExtraProps = (String)props.get("__pr_extraprops");
        HashMap extraPropsMap = OpalUtil.parseExtraProps(this._ExtraProps);
        if (extraPropsMap.containsKey("auditreason")) {
            this._AuditReason = (String)extraPropsMap.get("auditreason");
        }
        if (extraPropsMap.containsKey("__sdcruleconfirm")) {
            this._SdcRuleConfirm = (String)extraPropsMap.get("__sdcruleconfirm");
        }
        if (extraPropsMap.containsKey("auditactivity")) {
            this._AuditActivity = (String)extraPropsMap.get("auditactivity");
        }
        if (extraPropsMap.containsKey("auditsignedflag")) {
            this._AuditSignedFlag = (String)extraPropsMap.get("auditsignedflag");
        }
        if (this._Props.containsKey("tracelogid")) {
            this._TraceLogId = (String)this._Props.get("tracelogid");
        }
        this._Edata = (String)this._ElementProps.get("edata");
        this._Ecolumns = (String)this._ElementProps.get("ecolumns");
        this._Modified = (String)this._ElementProps.get("emodified");
        if (this._Modified != null && this._Modified.equalsIgnoreCase(YES)) {
            this._TableMD = new TableMetaData((String)this._ElementProps.get("tablemetadata"));
            this.saveData();
        }
    }

    protected String getProperty(String property) {
        String s = (String)this._ElementProps.get(property);
        return s == null ? "" : s;
    }

    protected abstract void saveData() throws SapphireException;

    public static HashMap filterProps(HashMap props) {
        String elementid = (String)props.get("__propertyhandler_elementid");
        String prefix = "__" + elementid + "_";
        String suffix = "_" + elementid;
        HashMap<String, String> requestParamMap = new HashMap<String, String>();
        int index = prefix.length();
        for (String key : props.keySet()) {
            Object valueObject = props.get(key);
            if (valueObject == null || !(valueObject instanceof String)) continue;
            String value = (String)props.get(key);
            if (key.startsWith("forward_") && key.endsWith(suffix)) {
                key = key.substring(8, key.lastIndexOf("_"));
                requestParamMap.put(key, value);
                continue;
            }
            if (!key.startsWith(prefix)) continue;
            key = key.substring(index);
            requestParamMap.put(key, value);
        }
        return requestParamMap;
    }

    protected HashMap executeAction(String actionid, String actionversionid, HashMap actionProps) throws SapphireException {
        try {
            actionProps.put("__sdcruleconfirm", this._SdcRuleConfirm);
            this.getActionProcessor().processAction(actionid, actionversionid, actionProps);
        }
        catch (ActionException e) {
            this.logError("Action (" + actionid + ", " + actionversionid + ") Failure: " + e.getMessage());
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors()) {
                ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(0);
                throw new SapphireException(errorDetail.getErrorid(), errorDetail.getErrorType(), errorDetail.getMessage());
            }
            throw new SapphireException("Action Failure", "VALIDATION", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (Exception e) {
            this.logError("Action (" + actionid + ", " + actionversionid + ") Failure: " + e.getMessage());
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        return actionProps;
    }

    protected HashMap executeActionClass(String actionClassName, HashMap actionProps) throws SapphireException {
        try {
            actionProps.put("__sdcruleconfirm", this._SdcRuleConfirm);
            this.getActionProcessor().processActionClass(actionClassName, actionProps, false);
        }
        catch (ActionException e) {
            this.logError("Action (" + actionClassName + ") Failure: " + e.getMessage());
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors()) {
                ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(0);
                throw new SapphireException(errorDetail.getErrorid(), errorDetail.getErrorType(), errorDetail.getMessage());
            }
            throw new SapphireException("Action Failure", "VALIDATION", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (Exception e) {
            this.logError("Action (" + actionClassName + ") Failure: " + e.getMessage());
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        return actionProps;
    }

    protected void executeActionBlock(ActionBlock actionBlock) throws SapphireException {
        try {
            if (actionBlock.getActionCount() > 0) {
                if (YES.equals(this._SdcRuleConfirm)) {
                    for (int i = 0; i < actionBlock.getActionCount(); ++i) {
                        actionBlock.getActionProperties(i).put("__sdcruleconfirm", YES);
                    }
                }
                this.getActionProcessor().processActionBlock(actionBlock);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < errorHandler.size(); ++i) {
                        ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(i);
                        if (!"INFORMATION".equals(errorDetail.getErrorType())) continue;
                        if (i > 0) {
                            sb.append("<br>");
                        }
                        sb.append(errorDetail.getMessage());
                    }
                    if (sb.length() > 0) {
                        throw new SapphireException("Action Information", "INFORMATION", sb.toString());
                    }
                }
            }
        }
        catch (ActionException e) {
            this.logError("Action Block Failure: " + e.getMessage());
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors()) {
                ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(0);
                throw new SapphireException(errorDetail.getErrorid(), errorDetail.getErrorType(), errorDetail.getMessage());
            }
            throw new SapphireException("Action Failure", "VALIDATION", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (SapphireException e) {
            throw new SapphireException(e.getErrorid(), e.getErrorType(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (Exception e) {
            this.logError("Action Block Failure: " + e.getMessage());
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    public String getMasterKeyValue(String key) {
        if (this.masterKeyValueMap == null) {
            String s = (String)this._ElementProps.get("masterkeyvalue");
            this.masterKeyValueMap = OpalUtil.string2Map(s, ";", "|");
        }
        return (String)this.masterKeyValueMap.get(key);
    }

    protected void putKeysInActionItem(ActionItem actionItem) {
        actionItem.put(COLUMN_KEYID1, this.getKeyid1());
        actionItem.put(COLUMN_KEYID2, this.getKeyid2());
        actionItem.put(COLUMN_KEYID3, this.getKeyid3());
    }

    public String getSdcId() {
        return this.__SdcId;
    }

    public String getKeyid1() {
        if (OpalUtil.isEmpty(this.__Keyid1) || this.__Keyid1.equals("null")) {
            return "(null)";
        }
        return this.__Keyid1;
    }

    public String getKeyid2() {
        if (OpalUtil.isEmpty(this.__Keyid2) || this.__Keyid2.equals("null")) {
            return "(null)";
        }
        return this.__Keyid2;
    }

    public String getKeyid3() {
        if (OpalUtil.isEmpty(this.__Keyid3) || this.__Keyid3.equals("null")) {
            return "(null)";
        }
        return this.__Keyid3;
    }

    public static List getCoreColumnsList() {
        return __CoreColumnsList;
    }

    protected ActionItem addActionItem(ActionItem actionItem) {
        this._ActionItems.add(actionItem);
        return actionItem;
    }

    protected ActionItem addActionItem(String actionClass) {
        ActionItem actionItem = new ActionItem(actionClass);
        this._ActionItems.add(actionItem);
        return actionItem;
    }

    protected ActionItem addActionItem(String actionid, String actionversionid) {
        ActionItem actionItem = new ActionItem(actionid, actionversionid);
        this._ActionItems.add(actionItem);
        return actionItem;
    }

    protected void processActionItems() throws SapphireException {
        this.processActionItems(true);
    }

    protected void processActionItems(boolean propsmatch) throws SapphireException {
        HashSet<String> infoSet = new HashSet<String>();
        for (int i = 0; i < this._ActionItems.size(); ++i) {
            ActionItem actionItem;
            Object obj = this._ActionItems.get(i);
            if (!(obj instanceof ActionItem) || (actionItem = (ActionItem)obj).size() <= 0) continue;
            HashMap actionProps = actionItem.getActionProps();
            actionProps.put(COLUMN_SDCID, this.getSdcId());
            actionProps.put("propsmatch", propsmatch ? YES : "N");
            actionProps.put("auditreason", this._AuditReason);
            actionProps.put("auditactivity", this._AuditActivity);
            actionProps.put("auditsignedflag", this._AuditSignedFlag);
            actionProps.put("__sdcruleconfirm", this._SdcRuleConfirm);
            if (this._TraceLogId != null && this._TraceLogId.length() > 0) {
                actionProps.put("tracelogid", this._TraceLogId);
            }
            try {
                if (actionItem.isActionClassName()) {
                    this.getActionProcessor().processActionClass(actionItem.getActionClass(), actionProps, false);
                } else {
                    this.getActionProcessor().processAction(actionItem.getActionid(), actionItem.getActionversionid(), actionProps);
                }
                actionItem.setActionReturnProps(actionProps);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler == null || !errorHandler.hasInfoErrors()) continue;
                for (int j = 0; j < errorHandler.size(); ++j) {
                    ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(j);
                    if (!"INFORMATION".equals(errorDetail.getErrorType())) continue;
                    infoSet.add(errorDetail.getMessage());
                }
                continue;
            }
            catch (ActionException e) {
                this.logError("Action (" + actionItem.getActionInfo() + ") Failure: " + e.getMessage());
                ArrayList errorStack = this.getActionProcessor().getErrorStack();
                if (errorStack != null && errorStack.size() > 0) {
                    throw new SapphireException("Action Failure", "VALIDATION", errorStack.get(0).toString(), e);
                }
                throw new SapphireException("Action Failure", "VALIDATION", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
            catch (Exception e) {
                this.logError("Action (" + actionItem.getActionInfo() + ") Failure: " + e.getMessage());
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (infoSet.size() > 0) {
            throw new SapphireException("Action Information", "INFORMATION", OpalUtil.toDelimitedString(infoSet, "<br>"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void updateSDIDetail(Map updateMap) throws SapphireException {
        String columnid;
        HashSet<String> infoSet = new HashSet<String>();
        String sdiDetailTable = (String)this._ElementProps.get("table");
        if ("sdiapproval".equals(sdiDetailTable)) {
            sdiDetailTable = "sdiapprovalstep";
        }
        ArrayList<String> keyColList = new ArrayList<String>();
        keyColList.add(COLUMN_SDCID);
        keyColList.add(COLUMN_KEYID1);
        keyColList.add(COLUMN_KEYID2);
        keyColList.add(COLUMN_KEYID3);
        keyColList.addAll(OpalUtil.toList((String)this._ElementProps.get("keycolumns"), ";"));
        String __actionclass = "";
        String __actionClassPropsMatch = "";
        if (updateMap.containsKey("__actionclass")) {
            __actionclass = (String)updateMap.get("__actionclass");
            updateMap.remove("__actionclass");
            if (updateMap.containsKey("__actionclasspropsmatch")) {
                __actionClassPropsMatch = (String)updateMap.get("__actionclasspropsmatch");
                updateMap.remove("__actionclasspropsmatch");
            }
        }
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        DataSet dsupdate = new DataSet(this.connectionInfo);
        block16: for (Object o : updateMap.keySet()) {
            columnid = (String)o;
            if (!this._TableMD.doesColumnExists(columnid)) continue;
            String columnType = this._TableMD.getDataType(columnid);
            String value = (String)updateMap.get(columnid);
            if (OpalUtil.isNotEmpty(value)) {
                value = StringUtil.replaceAll(value, "(null)", "");
            }
            switch (columnType) {
                case "C": {
                    dsupdate.addColumnValues(columnid, 0, value, ";");
                    break;
                }
                case "D": {
                    dsupdate.addColumn(columnid, 2);
                    boolean dateonly = false;
                    if ("sdiworkitem".equalsIgnoreCase(sdiDetailTable) && YES.equals(this.getSdcProcessor().getSDCColumnProperty("SDIWorkitem", columnid, "timezoneindependent"))) {
                        dateonly = true;
                        dsupdate.setTimeZoneInsensitive(columnid);
                    }
                    if ("sdispec".equalsIgnoreCase(sdiDetailTable) && YES.equals(this.getSdcProcessor().getSDCColumnProperty("SDISpec", columnid, "timezoneindependent"))) {
                        dateonly = true;
                        dsupdate.setTimeZoneInsensitive(columnid);
                    }
                    String[] values = StringUtil.split(value, ";");
                    for (int i = 0; i < dsupdate.size(); ++i) {
                        String date;
                        String string = date = values.length > i ? values[i] : "";
                        if (date.equalsIgnoreCase("(null)") || date.trim().length() == 0) {
                            dsupdate.setDate(i, columnid, (Calendar)null);
                            continue;
                        }
                        if (dateonly) {
                            dsupdate.setDate(i, columnid, m18n.parseCalendar(date, false));
                            continue;
                        }
                        dsupdate.setDate(i, columnid, m18n.parseCalendar(date));
                    }
                    continue block16;
                }
                case "N": 
                case "R": {
                    dsupdate.addColumnValues(columnid, 1, value, ";");
                }
            }
        }
        dsupdate.setString(-1, COLUMN_SDCID, this.getSdcId());
        dsupdate.setString(-1, COLUMN_KEYID1, this.getKeyid1());
        dsupdate.setString(-1, COLUMN_KEYID2, this.getKeyid2());
        dsupdate.setString(-1, COLUMN_KEYID3, this.getKeyid3());
        if (this._TableMD.doesColumnExists(COLUMN_MODBY)) {
            dsupdate.setString(-1, COLUMN_MODBY, this.connectionInfo.getSysuserId());
            dsupdate.setString(-1, COLUMN_MODTOOL, "sdidetail");
            dsupdate.setDate(-1, COLUMN_MODDT, Calendar.getInstance());
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        for (int i = 0; i < dsupdate.getColumnCount(); ++i) {
            columnid = dsupdate.getColumnId(i);
            if (updateExcludeColumnList.contains(columnid) || !this._TableMD.doesColumnExists(columnid)) continue;
            sql.append(columnid).append(",");
        }
        sql.setLength(sql.length() - 1);
        sql.append(" from ").append(sdiDetailTable);
        sql.append(" where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ?");
        DataSet currentData = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{this.getSdcId(), this.getKeyid1(), this.getKeyid2(), this.getKeyid3()});
        DataSet updateDataSet = new DataSet(this.connectionInfo);
        DataSet enterDataDataSet = new DataSet();
        if (currentData != null && currentData.size() > 0) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            for (int i = 0; i < dsupdate.size(); ++i) {
                boolean updateRow = false;
                filter.clear();
                for (String key : keyColList) {
                    if (this._TableMD.getDataType(key).equals("N")) {
                        filter.put(key, dsupdate.getBigDecimal(i, key));
                        continue;
                    }
                    filter.put(key, dsupdate.getValue(i, key));
                }
                int row = currentData.findRow(filter);
                if (row != -1) {
                    for (int col = 0; col < dsupdate.getColumnCount(); ++col) {
                        String columnid2 = dsupdate.getColumnId(col);
                        if ("enteredtext".equals(columnid2) && "sdidataitem".equalsIgnoreCase(sdiDetailTable) || keyColList.contains(columnid2) || updateExcludeColumnList.contains(columnid2) || currentData.getValue(row, columnid2).equals(dsupdate.getValue(i, columnid2))) continue;
                        updateRow = true;
                        break;
                    }
                    if (updateRow) {
                        updateDataSet.copyRow(dsupdate, i, 1);
                    }
                    if (!"sdidataitem".equalsIgnoreCase(sdiDetailTable) || currentData.getValue(row, "enteredtext").equals(dsupdate.getValue(i, "enteredtext"))) continue;
                    enterDataDataSet.copyRow(dsupdate, i, 1);
                    continue;
                }
                updateDataSet.copyRow(dsupdate, i, 1);
            }
        } else {
            for (int i = 0; i < dsupdate.size(); ++i) {
                updateDataSet.copyRow(dsupdate, i, 1);
            }
        }
        if (updateDataSet.size() > 0) {
            DBUtil dbutil = null;
            try {
                if ("sdiworksheetrule".equals(sdiDetailTable)) {
                    for (int i = 0; i < updateDataSet.size(); ++i) {
                        String worksheetversionid = updateDataSet.getString(i, "worksheetversionid", "");
                        if (!COLUMN_DATATYPE_CHAR.equalsIgnoreCase(worksheetversionid) && !"(null)".equalsIgnoreCase(worksheetversionid) && worksheetversionid.trim().length() != 0) continue;
                        updateDataSet.setString(i, "worksheetversionid", "");
                    }
                } else if ("sdiformrule".equals(sdiDetailTable)) {
                    for (int i = 0; i < updateDataSet.getRowCount(); ++i) {
                        String formversionid = updateDataSet.getString(i, "formversionid", "");
                        if (!formversionid.equalsIgnoreCase(COLUMN_DATATYPE_CHAR) && !formversionid.equalsIgnoreCase("(null)") && formversionid.trim().length() != 0) continue;
                        updateDataSet.setString(i, "formversionid", "");
                    }
                }
                if (OpalUtil.isNotEmpty(__actionclass)) {
                    PropertyList props = new PropertyList();
                    for (int col = 0; col < updateDataSet.getColumnCount(); ++col) {
                        String columnid3 = updateDataSet.getColumnId(col);
                        if (COLUMN_SDCID.equalsIgnoreCase(columnid3)) {
                            props.setProperty(COLUMN_SDCID, this.getSdcId());
                            continue;
                        }
                        props.setProperty(columnid3, updateDataSet.getColumnValues(columnid3, ";"));
                    }
                    if (OpalUtil.isNotEmpty(__actionClassPropsMatch)) {
                        props.setProperty("propsmatch", StringUtil.getYN(__actionClassPropsMatch, "N"));
                    }
                    if (this._AuditReason != null && this._AuditReason.length() > 0) {
                        props.setProperty("auditreason", this._AuditReason);
                        props.setProperty("auditactivity", this._AuditActivity);
                        props.setProperty("auditsignedflag", this._AuditSignedFlag);
                    }
                    this.getActionProcessor().processActionClass(__actionclass, props);
                    ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                    if (errorHandler != null && errorHandler.hasInfoErrors()) {
                        for (int j = 0; j < errorHandler.size(); ++j) {
                            ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(j);
                            if (!"INFORMATION".equals(errorDetail.getErrorType())) continue;
                            infoSet.add(errorDetail.getMessage());
                        }
                    }
                } else {
                    String[] keyColArray = new String[keyColList.size()];
                    int i = 0;
                    for (String keyCol : keyColList) {
                        keyColArray[i++] = keyCol;
                    }
                    dbutil = new DBUtil();
                    dbutil.setConnection(this.sapphireConnection);
                    DataSetUtil.update(dbutil, updateDataSet, sdiDetailTable, keyColArray);
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            finally {
                if (dbutil != null) {
                    dbutil.reset();
                }
            }
        }
        if (enterDataDataSet.size() > 0) {
            PropertyList props = new PropertyList();
            for (int col = 0; col < enterDataDataSet.getColumnCount(); ++col) {
                String columnid4 = enterDataDataSet.getColumnId(col);
                if (COLUMN_SDCID.equalsIgnoreCase(columnid4)) {
                    props.setProperty(COLUMN_SDCID, this.getSdcId());
                    continue;
                }
                props.setProperty(columnid4, enterDataDataSet.getColumnValues(columnid4, ";"));
            }
            this.getActionProcessor().processActionClass(EnterDataItem.class.getName(), props);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                for (int j = 0; j < errorHandler.size(); ++j) {
                    ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(j);
                    if (!"INFORMATION".equals(errorDetail.getErrorType())) continue;
                    infoSet.add(errorDetail.getMessage());
                }
            }
        }
        if (infoSet.size() > 0) {
            throw new SapphireException("Action Information", "INFORMATION", OpalUtil.toDelimitedString(infoSet, "<br>"));
        }
    }

    static {
        __CoreColumnsList.add(COLUMN_SDCID);
        __CoreColumnsList.add(COLUMN_KEYID1);
        __CoreColumnsList.add(COLUMN_KEYID2);
        __CoreColumnsList.add(COLUMN_KEYID3);
        __CoreColumnsList.add("paramlistid");
        __CoreColumnsList.add("paramlistversionid");
        __CoreColumnsList.add("paramlistvariant");
        __CoreColumnsList.add("variantid");
        __CoreColumnsList.add("dataset");
        __CoreColumnsList.add("paramid");
        __CoreColumnsList.add("paramtype");
        __CoreColumnsList.add("replicateid");
        __CoreColumnsList.add("s_datasetstatus");
        __CoreColumnsList.add("specid");
        __CoreColumnsList.add("specversionid");
        __CoreColumnsList.add("workitemid");
        __CoreColumnsList.add("workitemversionid");
        __CoreColumnsList.add("workiteminstance");
        __CoreColumnsList.add("documentid");
        __CoreColumnsList.add("documentversionid");
        __CoreColumnsList.add("formid");
        __CoreColumnsList.add("forminstance");
        __CoreColumnsList.add("worksheetid");
        __CoreColumnsList.add("worksheetinstance");
        __CoreColumnsList.add("resourcenum");
        __CoreColumnsList.add("createdt");
        __CoreColumnsList.add("createby");
        __CoreColumnsList.add(COLUMN_MODDT);
        __CoreColumnsList.add(COLUMN_MODBY);
        updateExcludeColumnList = new ArrayList<String>();
        updateExcludeColumnList.add("createdt");
        updateExcludeColumnList.add("createby");
        updateExcludeColumnList.add("createtool");
        updateExcludeColumnList.add(COLUMN_MODDT);
        updateExcludeColumnList.add(COLUMN_MODBY);
        updateExcludeColumnList.add(COLUMN_MODTOOL);
        updateExcludeColumnList.add("tracelogid");
    }

    public class ActionItem {
        private String actionid;
        private String actionversionid;
        private String actionClass;
        private HashMap map = new HashMap();
        private String delimiter = ";";
        private StringBuffer sb = new StringBuffer();
        private boolean isClass;

        public ActionItem() {
        }

        public ActionItem(String actionClass) {
            this();
            this.actionClass = actionClass;
            this.isClass = true;
        }

        public ActionItem(String actionid, String actionversionid) {
            this();
            this.actionid = actionid;
            this.actionversionid = actionversionid;
        }

        public void put(String key, String value) {
            if (this.map.containsKey(key)) {
                ((ArrayList)this.map.get(key)).add(value);
            } else {
                this.map.put(key, new ArrayList());
                this.put(key, value);
            }
        }

        public void putColumns(List columnList, ElementData elementData, int row) {
            for (Object aColumnList : columnList) {
                String column = (String)aColumnList;
                if (!BaseDetailPropertyHandler.this._TableMD.doesColumnExists(column)) continue;
                String value = elementData.getColumnData(row, column);
                this.put(column, value == null ? "" : value);
            }
        }

        public String get(String key) {
            this.sb.setLength(0);
            if (this.map.containsKey(key)) {
                ArrayList list = (ArrayList)this.map.get(key);
                for (int i = 0; i < list.size(); ++i) {
                    this.sb.append(list.get(i)).append(this.delimiter);
                }
                if (this.sb.length() > 0) {
                    this.sb.setLength(this.sb.length() - this.delimiter.length());
                }
            }
            return this.sb.toString();
        }

        public String getDelimiter() {
            return this.delimiter;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        public HashMap getActionProps() {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            Set keys = this.map.keySet();
            for (Object key1 : keys) {
                String key = (String)key1;
                actionProps.put(key, this.get(key));
            }
            return actionProps;
        }

        public void setActionReturnProps(Map actionProps) {
            Set keys = actionProps.keySet();
            for (Object key1 : keys) {
                String key = (String)key1;
                Object returnValue = actionProps.get(key);
                if (returnValue instanceof String && returnValue != null && OpalUtil.isNotEmpty(returnValue.toString())) {
                    this.map.put(key, new ArrayList<String>(Arrays.asList(StringUtil.split(returnValue.toString(), ";"))));
                }
                if (!(returnValue instanceof DataSet)) continue;
                this.map.put(key, new ArrayList((DataSet)returnValue));
            }
        }

        public Map findRow(String columnId, String columnValue) {
            int row;
            HashMap actionProps = new HashMap();
            ArrayList columnValueList = (ArrayList)this.map.get(columnId);
            if (columnValueList != null && (row = columnValueList.indexOf(columnValue)) >= 0) {
                Set keys = this.map.keySet();
                for (Object key1 : keys) {
                    String key;
                    ArrayList valueList = (ArrayList)this.map.get(key = (String)key1);
                    actionProps.put(key, valueList.size() > row ? valueList.get(row) : valueList.get(0));
                }
            }
            return actionProps;
        }

        public int size() {
            return this.map.size();
        }

        public int getValueSize() {
            return ((ArrayList)this.map.get(this.map.keySet().iterator().next())).size();
        }

        public String getActionid() {
            return this.actionid;
        }

        public String getActionversionid() {
            return this.actionversionid;
        }

        public String getActionClass() {
            return this.actionClass;
        }

        public boolean isActionClassName() {
            return this.isClass;
        }

        public String getActionInfo() {
            if (this.isActionClassName()) {
                return this.actionClass;
            }
            return this.actionid + ", " + this.actionversionid;
        }
    }

    public class TableMetaData {
        private Map __Map;

        public TableMetaData(String str) {
            this.__Map = OpalUtil.string2Map(str);
        }

        public String getDataType(String column) {
            return (String)this.__Map.get(column);
        }

        public boolean doesColumnExists(String column) {
            return this.__Map.containsKey(column);
        }
    }

    public class Pair
    extends HashMap<String, String> {
        public Pair(String str) {
            this.putAll(OpalUtil.string2Map(str));
        }

        public String getValue(String key) {
            return (String)super.get(key);
        }

        public String getKeysAsString(String delimiter) {
            StringBuilder sb = new StringBuilder();
            Set keySet = this.keySet();
            for (String aKeySet : keySet) {
                sb.append(aKeySet).append(delimiter);
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - delimiter.length());
            }
            return sb.toString();
        }

        public List getKeysAsList() {
            ArrayList<String> list = new ArrayList<String>();
            Set keySet = this.keySet();
            for (String aKeySet : keySet) {
                list.add(aKeySet);
            }
            return list;
        }

        public String getWhereClause(ElementData elementData) {
            StringBuilder sb = new StringBuilder();
            Set keySet = this.keySet();
            for (String aKeySet : keySet) {
                String keyMap = (String)this.get(aKeySet);
                String mapvalue = BaseDetailPropertyHandler.this.getMasterKeyValue(keyMap);
                sb.append(aKeySet).append(" = '").append(StringUtil.replaceAll(mapvalue, "'", "''")).append("'").append(" AND ");
                elementData.addColumn(aKeySet, mapvalue);
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 5);
            }
            return sb.toString();
        }
    }

    public class Key
    extends ArrayList<String> {
        String keys;

        public Key(String keys) {
            this.keys = keys;
            this.addAll(OpalUtil.toList(keys, ";"));
        }

        public String getKeys() {
            return this.keys;
        }
    }
}

