/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.util.format.NumericFormatter;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateSECMessage
extends BaseAction
implements sapphire.action.CreateSECMessage {
    private StringBuffer xmlString = new StringBuffer();
    private DataSet query;
    private DataSet queryArg;
    private DataSet colMaps;
    private DataSet joinCriteria;
    private String uuid;
    private final String KEY = "__Key";
    private final String REF = "__Ref";
    private final String PARAM = "__param";
    private int keySeq = 0;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        HashMap<String, DataSet> datasetList = new HashMap<String, DataSet>();
        String messageTypeId = properties.getProperty("messagetypeid");
        String inlog = properties.getProperty("log", "");
        StringBuffer log = new StringBuffer(inlog);
        if (messageTypeId.equals("")) {
            throw new SapphireException("INVALID_PROPERTY The msgtypeid messagetypeid is invalid.");
        }
        DataSet messageTypeInfo = this.getMessageTypeDetails(this.getQueryProcessor(), messageTypeId);
        if (messageTypeInfo.getRowCount() == 0) {
            throw new SapphireException("INVALID_PROPERTY The msgtypeid messagetypeid is invalid.");
        }
        String sapmsgtypeid = messageTypeId;
        String sapmsgtypeversionid = "1";
        if (!"LV_SAPMsgType".equals(messageTypeInfo.getString(0, "definitionsdcid", "LV_SAPMsgType"))) {
            throw new SapphireException("Invalid definitionsdcid specified in the messagetype");
        }
        if (messageTypeInfo.getString(0, "definitionkeyid1", "").length() > 0) {
            sapmsgtypeid = messageTypeInfo.getString(0, "definitionkeyid1");
        }
        if (messageTypeInfo.getString(0, "definitionkeyid2", "").length() > 0) {
            sapmsgtypeversionid = messageTypeInfo.getString(0, "definitionkeyid2");
        }
        log.append("Fetching SAPMsgType details for ").append(sapmsgtypeid).append(" version:").append(sapmsgtypeversionid);
        this.getSAPMsgTypeDetails(sapmsgtypeid, sapmsgtypeversionid);
        for (int i = 0; i < this.query.getRowCount(); ++i) {
            String sqlString = this.query.getString(i, "sapquerytext");
            String dataSetName = this.query.getString(i, "datablockname");
            String queryid = this.query.getString(i, "sapqueryid");
            String sappQueryid = this.query.getString(i, "queryid");
            this.logger.info("Executing query : " + queryid);
            log.append("Executing query : ").append(queryid);
            if (sappQueryid != null && sappQueryid.length() > 0) {
                String basedonid = this.query.getString(i, "basedonid");
                String[] params = new String[]{"", "", "", "", ""};
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sapmsgtypeid", sapmsgtypeid);
                filter.put("sapmsgtypeversionid", sapmsgtypeversionid);
                filter.put("sapqueryid", queryid);
                DataSet filterDS = this.queryArg.getFilteredDataSet(filter);
                filterDS.sort("usersequence");
                for (int j = 0; j < filterDS.getRowCount(); ++j) {
                    String sourceSapQueryid = filterDS.getValue(i, "sourcesapqueryid");
                    String sourceColumnId = filterDS.getValue(i, "sourcesapcolumnid");
                    String argValue = this.queryArg.getValue(i, "argvalue");
                    if (sourceSapQueryid != null && !sourceSapQueryid.trim().equals("")) {
                        DataSet sourceDS = (DataSet)datasetList.get(sourceSapQueryid);
                        if (sourceDS == null) {
                            this.logger.error("DataSource:" + sourceSapQueryid + " not found in memory");
                            throw new SapphireException("DataSource:" + sourceSapQueryid + " not found in memory");
                        }
                        if (sourceColumnId == null || sourceColumnId.equals("")) {
                            this.logger.error("Source columid not defined ");
                            throw new SapphireException("Source columid not defined ");
                        }
                        if (!sourceDS.isValidColumn(sourceColumnId)) {
                            this.logger.error("Source columid" + sourceColumnId + " not found in the dataset ");
                            throw new SapphireException("Source columid" + sourceColumnId + " not found in the dataset ");
                        }
                        params[j] = sourceDS.getColumnValues(sourceColumnId, ";");
                        continue;
                    }
                    params[j] = argValue != null && !argValue.trim().equals("") ? argValue : properties.getProperty(this.queryArg.getValue(j, "sapargid"));
                }
                this.createDataSet(datasetList, params, basedonid, sappQueryid, dataSetName, queryid);
                continue;
            }
            if (sqlString != null && sqlString.length() > 0) {
                this.logger.info("executing sec query:" + queryid);
                log.append("Executing SQL query: ").append(sqlString);
                while (sqlString.indexOf("[") >= 0) {
                    String variable = sqlString.substring(sqlString.indexOf("[") + 1, sqlString.indexOf("]"));
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sapmsgtypeid", sapmsgtypeid);
                    filter.put("sapmsgtypeversionid", sapmsgtypeversionid);
                    filter.put("sapqueryid", queryid);
                    filter.put("sapargid", variable);
                    DataSet filterVariable = this.queryArg.getFilteredDataSet(filter);
                    if (filterVariable.getRowCount() <= 0) {
                        this.logger.error("Variable mot defined ");
                        throw new SapphireException("Variable not defined" + variable);
                    }
                    String argId = filterVariable.getString(0, "sapargid", "");
                    String argDefaultVal = filterVariable.getString(0, "argvalue", "");
                    String argType = filterVariable.getString(0, "argtype", "");
                    String sourceQueryId = filterVariable.getString(0, "sourcesapqueryid", "");
                    String sourceColumnId = filterVariable.getString(0, "sourcesapcolumnid", "");
                    String inputVarValue = properties.getProperty(variable, argDefaultVal);
                    if (!inputVarValue.equals("")) {
                        sqlString = this.replaceVariable(argId, inputVarValue, argType, sqlString);
                        continue;
                    }
                    if (!sourceQueryId.equals("")) {
                        DataSet sourceDS = (DataSet)datasetList.get(sourceQueryId);
                        if (sourceDS == null) {
                            this.logger.error("DataSource:" + sourceQueryId + " not found in memory");
                            throw new SapphireException("DataSource:" + sourceQueryId + " not found in memory");
                        }
                        if (sourceColumnId == null || sourceColumnId.equals("")) {
                            this.logger.error("Source columid not defined ");
                            throw new SapphireException("Source columid not defined ");
                        }
                        if (!sourceDS.isValidColumn(sourceColumnId)) {
                            this.logger.error("Source columid" + sourceColumnId + " not found in the dataset ");
                            throw new SapphireException("Source columid" + sourceColumnId + " not found in the dataset ");
                        }
                        inputVarValue = sourceDS.getColumnValues(sourceColumnId, ";");
                        sqlString = this.replaceVariable(argId, inputVarValue, argType, sqlString);
                        continue;
                    }
                    inputVarValue = properties.getProperty(variable);
                    if (inputVarValue == null || inputVarValue.trim().equals("")) {
                        throw new SapphireException("Variable " + variable + " is not present in Action Property!");
                    }
                    sqlString = this.replaceVariable(argId, inputVarValue, argType, sqlString);
                }
                this.createDataSet(sqlString, datasetList, dataSetName, queryid);
                continue;
            }
            String datasetstr = properties.getProperty(queryid);
            if (datasetstr != null) {
                DataSet ds = new DataSet(datasetstr, this.connectionInfo);
                ds.addColumn("__param", 0);
                ds.setValue(0, "__param", dataSetName);
                ds.padColumn("__param");
                datasetList.put(queryid, ds);
                continue;
            }
            throw new SapphireException("Missing property " + queryid);
        }
        log.append("Building outgoing message header");
        this.buildHeader(sapmsgtypeid, sapmsgtypeversionid);
        log.append("Building data blocks in the outgoing message");
        this.buildData(this.query, datasetList, this.joinCriteria, this.colMaps);
        this.buildFooter();
        properties.setProperty("message", this.xmlString.toString());
        properties.setProperty("messagetag", this.uuid);
        properties.setProperty("log", log.toString());
    }

    private void buildFooter() {
        this.xmlString.append("</ZSEC>");
    }

    private DataSet convertColumnName(String queryid, DataSet ds1, DataSet colMaps) throws SapphireException {
        DataSet ds = ds1.copy();
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put("sapqueryid", queryid);
        DataSet colMap = colMaps.getFilteredDataSet(temp);
        for (int i = 0; i < colMap.getRowCount(); ++i) {
            String sapcolumnid = colMap.getString(i, "sapcolumnid");
            String externalcolumnid = colMap.getString(i, "externalcolumnid");
            ds.addColumn(externalcolumnid, ds.getColumnType(sapcolumnid));
            String tempcolvalue = ds.getColumnValues(sapcolumnid, ";");
            ds.addColumnValues(externalcolumnid, ds.getColumnType(sapcolumnid), tempcolvalue, ";");
        }
        this.fillKeyIds(ds);
        return ds;
    }

    private DataSet getColumnMapping(String sapmsgtypeid, String sapmsgtypeversionid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("Select * from sapmsgquerycolumnmap where sapmsgtypeid=");
        sql.append(safeSQL.addVar(sapmsgtypeid));
        sql.append(" and sapmsgtypeversionid=");
        sql.append(safeSQL.addVar(sapmsgtypeversionid));
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        return ds;
    }

    private String replaceVariable(String argId, String argVal, String argType, String sqlString) {
        String match = "[" + argId + "]";
        StringBuffer sqlBuffer = new StringBuffer(sqlString);
        while (sqlBuffer.indexOf(match) >= 0) {
            String leftSide = this.stripExtraCharacter(sqlBuffer.substring(0, sqlBuffer.indexOf(match) - 1), "left");
            String rightSide = this.stripExtraCharacter(sqlBuffer.substring(sqlBuffer.indexOf(match) + match.length()), "right");
            String[] argValArr = StringUtil.split(argVal, ";");
            sqlBuffer.delete(0, sqlBuffer.length());
            if (argValArr.length > 1) {
                sqlBuffer.append(leftSide);
                sqlBuffer.append(" in (");
                if (argType.equals("STRING")) {
                    for (int j = 0; j < argValArr.length; ++j) {
                        sqlBuffer.append(j != 0 ? "," : "").append("'").append(argValArr[j]).append("'");
                    }
                    sqlBuffer.append(")");
                }
                sqlBuffer.append(rightSide);
                continue;
            }
            sqlBuffer.append(leftSide);
            sqlBuffer.append(" ='");
            if (argType.equals("STRING") || argType.equals("DATE")) {
                sqlBuffer.append(argVal);
                sqlBuffer.append("'");
            } else if (argType.equals("NUMBER")) {
                sqlBuffer.append(argVal);
            }
            sqlBuffer.append(rightSide);
        }
        return sqlBuffer.toString();
    }

    private String stripExtraCharacter(String s, String direction) {
        if ((s = s.trim()).equals("'")) {
            return "";
        }
        if (direction.equals("left")) {
            for (int i = s.length() - 1; i > 0; ++i) {
                if (s.charAt(i) != '=') continue;
                return s.substring(0, i);
            }
        } else if (s.startsWith("'")) {
            return s.substring(1);
        }
        return s;
    }

    private void createDataSet(String sqlString, HashMap datasetList, String paramName, String queryId) throws SapphireException {
        this.database.createResultSet(sqlString);
        DataSet ds = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        ds.addColumn("__param", 0);
        ds.addColumnValues("__param", 0, "", ";", paramName);
        ds.addColumn("__Key", 0);
        ds.addColumn("__Ref", 0);
        datasetList.put(queryId, ds);
    }

    private void createDataSet(HashMap datasetList, String[] param, String sdcId, String sappQueryid, String paramName, String queryId) {
        SDIRequest request = new SDIRequest();
        request.setSDCid(sdcId);
        request.setQueryid(sappQueryid);
        request.setRequestItem("primary");
        request.setQueryParams(param);
        SDIData sdidata = this.getSDIProcessor().getSDIData(request);
        DataSet ds = sdidata.getDataset("primary");
        ds.addColumn("__param", 0);
        ds.addColumnValues("__param", 0, "", ";", paramName);
        ds.addColumn("__Key", 0);
        ds.addColumn("__Ref", 0);
        datasetList.put(queryId, ds);
    }

    private DataSet getQueryArgConfiguration(String sapmsgtypeid, String sapmsgtypeversionid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("Select * from sapmsgqueryarg where sapmsgtypeid=");
        sql.append(safeSQL.addVar(sapmsgtypeid));
        sql.append(" and sapmsgtypeversionid=");
        sql.append(safeSQL.addVar(sapmsgtypeversionid));
        sql.append(" order by usersequence");
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        return ds;
    }

    private DataSet getQueryConfiguration(String sapmsgtypeid, String sapmsgtypeversionid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("Select * from sapmsgquery where sapmsgtypeid=");
        sql.append(safeSQL.addVar(sapmsgtypeid));
        sql.append(" and sapmsgtypeversionid=");
        sql.append(safeSQL.addVar(sapmsgtypeversionid));
        sql.append(" order by sapquerysequence");
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        return ds;
    }

    private void getSAPMsgTypeDetails(String sapmsgtypeid, String sapmsgtypeversionid) throws SapphireException {
        this.logger.info("Getting msg configuration detail ");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("Select * from sapmsgtype where sapmsgtypeid=");
        sql.append(safeSQL.addVar(sapmsgtypeid));
        sql.append(" and sapmsgtypeversionid=");
        sql.append(safeSQL.addVar(sapmsgtypeversionid));
        sql.append(" and directionflag='O'");
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet msgDetail = new DataSet(this.database.getResultSet());
        if (msgDetail.getRowCount() == 0) {
            throw new SapphireException("Error getting Configuration detail of the Sapmsgtype= " + sapmsgtypeid + " and versionid= " + sapmsgtypeid);
        }
        this.query = this.getQueryConfiguration(sapmsgtypeid, sapmsgtypeversionid);
        this.queryArg = this.getQueryArgConfiguration(sapmsgtypeid, sapmsgtypeversionid);
        this.colMaps = this.getColumnMapping(sapmsgtypeid, sapmsgtypeversionid);
        this.joinCriteria = this.getJoinCriteriaConfiguration(sapmsgtypeid, sapmsgtypeversionid);
    }

    private DataSet getJoinCriteriaConfiguration(String sapmsgtypeid, String sapmsgtypeversionid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("Select * from sapmsgjoincriteria where sapmsgtypeid=");
        sql.append(safeSQL.addVar(sapmsgtypeid));
        sql.append(" and sapmsgtypeversionid=");
        sql.append(safeSQL.addVar(sapmsgtypeversionid));
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        return ds;
    }

    private String getUUID(String messageType) throws SapphireException {
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        int id = sequenceProcessor.getSequence("messagetag", "messagetag");
        if (id == -1) {
            throw new SapphireException("Error getting sequence for tracelog");
        }
        String num = NumericFormatter.formatNumber(id, "00000");
        return messageType + "-" + num;
    }

    private void buildHeader(String msgtypeId, String msgtypeVersionId) throws SapphireException {
        this.xmlString.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        this.xmlString.append("<ZSEC xsi:noNamespaceSchemaLocation=\"sec.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        this.xmlString.append("<ZHeader>\n");
        this.xmlString.append("<ZMessage>\n");
        this.xmlString.append("<MsgID>");
        this.uuid = this.getUUID(msgtypeId);
        this.xmlString.append(this.uuid);
        this.xmlString.append("</MsgID>\n");
        this.xmlString.append("<MsgType>");
        this.xmlString.append("GEN");
        this.xmlString.append("</MsgType>\n");
        this.xmlString.append("<MsgName>");
        this.xmlString.append(msgtypeId);
        this.xmlString.append("</MsgName>\n");
        this.xmlString.append("<MsgFlow>");
        this.xmlString.append("OUT");
        this.xmlString.append("</MsgFlow>\n");
        this.xmlString.append("<MsgDBID>");
        this.xmlString.append("");
        this.xmlString.append("</MsgDBID>\n");
        this.xmlString.append("<MsgVersion>");
        this.xmlString.append(msgtypeVersionId);
        this.xmlString.append("</MsgVersion>\n");
        this.xmlString.append("</ZMessage>\n<ZMeta-Data>\n<PrevID />\n<Error />\n<Acknowledge />\n<Success />\n</ZMeta-Data>\n</ZHeader>\n");
    }

    private void buildData(DataSet query, HashMap dataSetList, DataSet joinCriteria, DataSet colMaps) throws SapphireException {
        HashMap<String, DataSet> hm = new HashMap<String, DataSet>();
        for (int i = 0; i < query.getRowCount(); ++i) {
            HashMap<String, String> temp;
            String queryId = query.getString(i, "sapqueryid");
            DataSet queryDS1 = (DataSet)dataSetList.get(queryId);
            DataSet queryDS = this.convertColumnName(queryId, queryDS1, colMaps);
            hm.put(queryId, queryDS);
            if (dataSetList.containsKey(queryId)) {
                temp = new HashMap<String, String>();
                temp.put("rightsapqueryid", queryId);
                DataSet criterias = joinCriteria.getFilteredDataSet(temp);
                if (criterias.getRowCount() > 0) {
                    DataSet leftQuery = (DataSet)dataSetList.get(criterias.getString(0, "leftsapqueryid"));
                    DataSet leftChangedQuery = (DataSet)hm.get(criterias.getString(0, "leftsapqueryid"));
                    for (int iIndex = 0; iIndex < leftQuery.getRowCount(); ++iIndex) {
                        for (int kIndex = 0; kIndex < queryDS.getRowCount(); ++kIndex) {
                            boolean match = true;
                            for (int jIndex = 0; jIndex < criterias.getRowCount(); ++jIndex) {
                                if (leftQuery.getValue(iIndex, criterias.getString(jIndex, "leftsapcolumnid")).equals(queryDS1.getValue(kIndex, criterias.getString(jIndex, "rightsapcolumnid")))) continue;
                                match = false;
                            }
                            if (!match) continue;
                            queryDS.setString(kIndex, "__Ref", leftChangedQuery.getValue(iIndex, "__Key"));
                        }
                    }
                }
            } else {
                throw new SapphireException("Invalid configuration, Query not processed!");
            }
            temp = new HashMap();
            temp.put("sapqueryid", queryId);
            this.writeDataSet(queryDS, colMaps.getFilteredDataSet(temp));
        }
    }

    private void fillKeyIds(DataSet qDS) {
        for (int i = 0; i < qDS.getRowCount(); ++i) {
            qDS.setString(i, "__Key", ++this.keySeq + "");
        }
    }

    private void writeDataSet(DataSet queryDS, DataSet colMap) {
        String param = queryDS.getString(0, "__param");
        if (colMap == null && colMap.getRowCount() <= 0) {
            return;
        }
        for (int i = 0; i < queryDS.getRowCount(); ++i) {
            this.xmlString.append("<ZData>\n");
            this.xmlString.append("<Key>");
            this.xmlString.append(queryDS.getValue(i, "__Key"));
            this.xmlString.append("</Key>\n");
            this.xmlString.append("<Ref>");
            this.xmlString.append(queryDS.getValue(i, "__Ref", ""));
            this.xmlString.append("</Ref>\n");
            this.xmlString.append("<ParamName>");
            this.xmlString.append(param);
            this.xmlString.append("</ParamName>\n");
            this.xmlString.append("<ZColumns>\n");
            for (int j = 0; j < colMap.getRowCount(); ++j) {
                String colname = colMap.getValue(j, "externalcolumnid");
                this.xmlString.append("<ZColumn>\n");
                this.xmlString.append("<ColumnKey>");
                this.xmlString.append(colname);
                this.xmlString.append("</ColumnKey>\n");
                this.xmlString.append("<ColumnVal><![CDATA[");
                String val = queryDS.getValue(i, colname, "");
                this.xmlString.append((Object)val);
                this.xmlString.append("]]></ColumnVal>\n");
                this.xmlString.append("<ColumnType>");
                this.xmlString.append(queryDS.getColumnType(colname) == 2 ? "DATE" : (queryDS.getColumnType(colname) == 0 ? "STRING" : (queryDS.getColumnType(colname) == 1 ? "NUMBER" : "UNKNOWN")));
                this.xmlString.append("</ColumnType>\n");
                this.xmlString.append("</ZColumn>\n");
            }
            this.xmlString.append("</ZColumns>\n");
            this.xmlString.append("</ZData>\n\n");
        }
    }

    private DataSet getMessageTypeDetails(QueryProcessor qp, String messageTypeId) throws ActionException {
        String sql = "SELECT * FROM messagetype WHERE messagetypeid=?";
        DataSet result = qp.getPreparedSqlDataSet(sql, new Object[]{messageTypeId});
        if (result == null || result.getRowCount() == 0) {
            throw new ActionException("Message Type details not found for messagetypeid: " + messageTypeId);
        }
        return result;
    }
}

