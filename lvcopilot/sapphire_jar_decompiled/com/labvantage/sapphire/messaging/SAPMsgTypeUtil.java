/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.messaging;

import com.labvantage.sapphire.Trace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SAPMsgTypeUtil {
    private String sapMsgTypeId = "";
    private String sapMsgTypeVersion = "";
    private PropertyList header = new PropertyList();
    DataSet sapMsgTypeInfo = null;
    DataSet sapMsgQueryInfo = null;
    DataSet sapMsgQueryArgInfo = null;
    DataSet sapMsgQueryColumnMapInfo = null;
    DataSet sapMsgJoinInfo = null;
    DataSet sapMsgJoinCriteriaInfo = null;
    DataSet sapMsgJoinFilterInfo = null;
    DataSet sapMsgActionInfo = null;
    DataSet sapMsgActionPropertiesInfo = null;
    private QueryProcessor qp = null;
    private ActionProcessor ap = null;
    private SDIProcessor sdip = null;

    public SAPMsgTypeUtil(String connectionId, PropertyList header) {
        this.qp = new QueryProcessor(connectionId);
        this.ap = new ActionProcessor(connectionId);
        this.sapMsgTypeId = header.getProperty("MsgName");
        this.sapMsgTypeVersion = header.getProperty("MsgVersion");
        this.header = header;
        this.sapMsgTypeInfo = this.getSAPMsgTypeInfo();
        this.sapMsgQueryInfo = this.getSAPMsgQueryInfo();
        this.sapMsgQueryArgInfo = this.getSAPMsgQueryArgInfo();
        this.sapMsgQueryColumnMapInfo = this.getSAPMsgQueryColumnMap();
        this.sapMsgJoinInfo = this.getSAPMsgJoinInfo();
        this.sapMsgJoinCriteriaInfo = this.getSAPMsgJoinCriteriaInfo();
        this.sapMsgJoinFilterInfo = this.getSAPMsgFilterInfo();
        this.sapMsgActionInfo = this.getSAPMsgActionInfo();
        this.sapMsgActionPropertiesInfo = this.getSAPMsgActionPropertiesInfo();
    }

    public SAPMsgTypeUtil(QueryProcessor qp, ActionProcessor ap, SDIProcessor sdip, String sapMsgTypeId, String sapMsgTypeVersion) {
        this.qp = qp;
        this.ap = ap;
        this.sdip = sdip;
        this.sapMsgTypeId = sapMsgTypeId;
        this.sapMsgTypeVersion = sapMsgTypeVersion;
        this.sapMsgTypeInfo = this.getSAPMsgTypeInfo();
        this.sapMsgQueryInfo = this.getSAPMsgQueryInfo();
        this.sapMsgQueryArgInfo = this.getSAPMsgQueryArgInfo();
        this.sapMsgQueryColumnMapInfo = this.getSAPMsgQueryColumnMap();
        this.sapMsgJoinInfo = this.getSAPMsgJoinInfo();
        this.sapMsgJoinCriteriaInfo = this.getSAPMsgJoinCriteriaInfo();
        this.sapMsgJoinFilterInfo = this.getSAPMsgFilterInfo();
        this.sapMsgActionInfo = this.getSAPMsgActionInfo();
        this.sapMsgActionPropertiesInfo = this.getSAPMsgActionPropertiesInfo();
    }

    public PropertyList process(PropertyList data) throws SapphireException {
        PropertyList mappedData = this.mapSAPToSapphire(data);
        mappedData = this.createJoinDataSets(mappedData);
        return this.executeActions(mappedData);
    }

    private PropertyList createJoinDataSets(PropertyList mappedData) throws SapphireException {
        if (this.sapMsgJoinInfo == null || this.sapMsgJoinInfo.getRowCount() == 0 || this.sapMsgJoinCriteriaInfo == null || this.sapMsgJoinCriteriaInfo.getRowCount() == 0) {
            return mappedData;
        }
        for (int i = 0; i < this.sapMsgJoinInfo.getRowCount(); ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sapjoinid", this.sapMsgJoinInfo.getString(i, "sapjoinid"));
            DataSet joinDataSet = new DataSet();
            DataSet criteriaDS = this.sapMsgJoinCriteriaInfo.getFilteredDataSet(filter);
            ArrayList<String> participatingDSList = new ArrayList<String>();
            for (int j = 0; j < criteriaDS.getRowCount(); ++j) {
                String condition = criteriaDS.getString(j, "leftsapqueryid") + ";" + criteriaDS.getString(j, "rightsapqueryid");
                if (!participatingDSList.isEmpty() && participatingDSList.contains(condition)) continue;
                participatingDSList.add(condition);
            }
            Iterator it = participatingDSList.iterator();
            while (it.hasNext()) {
                boolean index = false;
                String[] arr = ((String)it.next()).split(";");
                String leftQueryId = arr[0];
                String rightQueryId = arr[1];
                joinDataSet = this.cartesian(new DataSet(mappedData.getProperty(leftQueryId)), new DataSet(mappedData.getProperty(rightQueryId)), criteriaDS, true, leftQueryId, rightQueryId);
                joinDataSet = this.filterDataSet(joinDataSet, this.sapMsgJoinInfo.getString(i, "sapjoinid"));
                mappedData.setProperty(this.sapMsgJoinInfo.getString(i, "sapjoinid"), joinDataSet.toXML());
            }
        }
        return mappedData;
    }

    private PropertyList mapSAPToSapphire(PropertyList sapData) throws SapphireException {
        int i;
        PropertyList sapphireData = new PropertyList();
        Set propertyNames = sapData.keySet();
        Object[] sapDataBlockNames = propertyNames.toArray();
        String[] sapphireQueryIdList = new String[sapDataBlockNames.length];
        String[] sapphireMappedDataSet = new String[sapDataBlockNames.length];
        for (i = 0; i < sapData.size(); ++i) {
            String sapBlockName = (String)sapDataBlockNames[i];
            String sapphireQueryId = "";
            for (int j = 0; j < this.sapMsgQueryInfo.getRowCount(); ++j) {
                String currSAPBlockName = this.sapMsgQueryInfo.getValue(j, "DATABLOCKNAME");
                String currSapphireQueryId = this.sapMsgQueryInfo.getValue(j, "SAPQUERYID");
                if (!sapBlockName.trim().equals(currSAPBlockName.trim())) continue;
                sapphireQueryId = currSapphireQueryId;
                break;
            }
            if (sapphireQueryId == null || sapphireQueryId.length() == 0) {
                throw new SapphireException("SapQueryId not defined for data block name:" + sapBlockName);
            }
            sapphireQueryIdList[i] = sapphireQueryId;
            sapphireMappedDataSet[i] = this.mapSAPDataSet(sapphireQueryId, sapData.getProperty(sapBlockName));
        }
        for (i = 0; i < sapphireQueryIdList.length; ++i) {
            sapphireData.setProperty(sapphireQueryIdList[i], sapphireMappedDataSet[i]);
        }
        return sapphireData;
    }

    private String mapSAPDataSet(String sapphireQueryId, String sapDataSetXML) throws SapphireException {
        DataSet sapDS = new DataSet(sapDataSetXML);
        String[] sapColumns = sapDS.getColumns();
        String[] sapphireColumns = new String[sapColumns.length];
        DataSet sapphireDS = new DataSet();
        for (int i = 0; i < sapColumns.length; ++i) {
            if ("key".equals(sapColumns[i]) || "ref".equals(sapColumns[i]) || "param".equals(sapColumns[i])) {
                sapphireColumns[i] = sapColumns[i];
                sapphireDS.addColumn(sapColumns[i], sapDS.getColumnType(sapColumns[i]));
                continue;
            }
            String sappCol = this.findSapphireColumnName(sapphireQueryId, sapColumns[i]);
            if (sappCol == null || sappCol.length() == 0) {
                Trace.log("Did not find the sapphireColumnId for " + sapColumns[i]);
                throw new SapphireException("Mapping not specified for SAP column " + sapColumns[i]);
            }
            sapphireColumns[i] = sappCol;
            sapphireDS.addColumn(sapphireColumns[i], sapDS.getColumnType(sapColumns[i]));
        }
        for (int row = 0; row < sapDS.getRowCount(); ++row) {
            sapphireDS.addRow(row);
            for (int col = 0; col < sapColumns.length; ++col) {
                String sapColVal = sapDS.getValue(row, sapColumns[col]);
                sapphireDS.setValue(row, sapphireColumns[col], sapColVal);
            }
        }
        return sapphireDS.toXML();
    }

    private String findSapphireColumnName(String sapphireQueryId, String sapColumnName) {
        for (int row = 0; row < this.sapMsgQueryColumnMapInfo.getRowCount(); ++row) {
            String currSapphireQueryId = this.sapMsgQueryColumnMapInfo.getValue(row, "SAPQUERYID");
            String currSapColumnName = this.sapMsgQueryColumnMapInfo.getValue(row, "EXTERNALCOLUMNID");
            if (!sapphireQueryId.equals(currSapphireQueryId) || !sapColumnName.equalsIgnoreCase(currSapColumnName)) continue;
            return this.sapMsgQueryColumnMapInfo.getValue(row, "SAPCOLUMNID");
        }
        return "";
    }

    public DataSet getSAPMsgTypeInfo() {
        DataSet result = null;
        String sql = "SELECT SAPMSGTYPEID,SAPMSGTYPEVERSIONID,SAPMSGTYPEDESC,TYPEFLAG,DIRECTIONFLAG FROM SAPMsgType where SAPMsgTypeId = ?  AND sapmsgtypeversionid= ?";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgQueryInfo() {
        DataSet result = null;
        String sql = "SELECT SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPQUERYID, SAPQUERYTEXT, QUERYID, BASEDONID, DATABLOCKNAME FROM SAPMSGQUERY WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgQueryArgInfo() {
        DataSet result = null;
        String sql = "SELECT SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPQUERYID, SAPARGID,ARGVALUE,ARGTYPE,SOURCESAPQUERYID,SOURCESAPCOLUMNID,USERSEQUENCE FROM SAPMSGQUERYARG WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgQueryColumnMap() {
        DataSet result = null;
        String sql = "SELECT  SAPMSGTYPEID,SAPMSGTYPEVERSIONID,SAPQUERYID, SAPCOLUMNID, EXTERNALCOLUMNID FROM SAPMSGQUERYCOLUMNMAP WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgJoinInfo() {
        DataSet result = null;
        String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID FROM SAPMSGJOIN WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgJoinCriteriaInfo() {
        String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID, SAPCRITERIAID,LEFTSAPQUERYID, LEFTSAPCOLUMNID, RIGHTSAPQUERYID, RIGHTSAPCOLUMNID FROM SAPMSGJOINCRITERIA WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        return this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
    }

    public DataSet getSAPMsgFilterInfo() {
        String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID,  SAPFILTERID, SAPQUERYID, SAPCOLUMNID FROM SAPMSGJOINFILTER WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=?";
        return this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
    }

    public DataSet getSAPMsgActionInfo() {
        DataSet result = null;
        String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, ACTIONID, ACTIONVERSIONID, ACTIONINSTANCE FROM SAPMSGACTION WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=? ORDER BY ACTIONINSTANCE";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public DataSet getSAPMsgActionPropertiesInfo() {
        DataSet result = null;
        String sql = "SELECT  SAPMSGTYPEID,SAPMSGTYPEVERSIONID,ACTIONID,ACTIONVERSIONID,ACTIONINSTANCE,PROPERTYID,PROPERTYVALUE,PROPERTYTYPE FROM SAPMSGACTIONPROPERTY WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=? ORDER BY ACTIONINSTANCE";
        result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        return result;
    }

    public PropertyList executeActions(PropertyList mappedData) throws SapphireException {
        int i;
        PropertyList returnProps = new PropertyList();
        ActionBlock actionBlock = new ActionBlock();
        if (this.sapMsgActionInfo.getRowCount() == 0) {
            returnProps.setProperty("error", "No actions specified to process the message " + this.sapMsgTypeId);
            returnProps.setProperty("status", "FAILED");
            return returnProps;
        }
        for (i = 0; i < this.sapMsgActionInfo.getRowCount(); ++i) {
            actionBlock.setAction(Integer.toString(this.sapMsgActionInfo.getInt(i, "actioninstance")), this.sapMsgActionInfo.getString(i, "actionid"), this.sapMsgActionInfo.getString(i, "actionversionid"));
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("actionid", this.sapMsgActionInfo.getString(i, "actionid"));
            filter.put("actionversionid", this.sapMsgActionInfo.getString(i, "actionversionid"));
            filter.put("actioninstance", new BigDecimal(this.sapMsgActionInfo.getValue(i, "actioninstance")));
            DataSet currentActionProps = this.sapMsgActionPropertiesInfo.getFilteredDataSet(filter);
            HashMap<String, String> acProps = new HashMap<String, String>();
            for (int j = 0; j < currentActionProps.getRowCount(); ++j) {
                String propertyid = currentActionProps.getString(j, "propertyid");
                String propertyValue = currentActionProps.getString(j, "propertyvalue");
                if (propertyValue.startsWith("{")) {
                    String tempPropertyValue = propertyValue.substring(propertyValue.indexOf("{") + 1, propertyValue.indexOf("}"));
                    String[] tempArr = StringUtil.split(tempPropertyValue, ".");
                    String sapphireQueryId = tempArr[0];
                    String sapphireColumnId = tempArr[1];
                    if (tempArr.length != 2 || tempArr[0].length() == 0 || tempArr[1].length() == 0) {
                        throw new SapphireException("Property value specified incorrectly " + tempPropertyValue);
                    }
                    String curDataSetStr = mappedData.getProperty(sapphireQueryId);
                    if (curDataSetStr == null || curDataSetStr.length() == 0) {
                        throw new SapphireException("DataSet is empty");
                    }
                    DataSet curDataSet = new DataSet(curDataSetStr);
                    String substitutedPropertyValue = curDataSet.getColumnValues(sapphireColumnId, ";");
                    acProps.put(propertyid, substitutedPropertyValue);
                    continue;
                }
                if (propertyValue.startsWith("HEADER")) {
                    String[] tempArr = StringUtil.split(propertyValue, ".");
                    if (tempArr.length != 2 || tempArr[0].length() == 0 || tempArr[1].length() == 0) {
                        throw new SapphireException("Property value specified incorrectly " + propertyValue);
                    }
                    acProps.put(propertyid, this.header.getProperty(tempArr[1]));
                    continue;
                }
                acProps.put(propertyid, propertyValue);
            }
            actionBlock.setActionProperties(Integer.toString(this.sapMsgActionInfo.getInt(i, "actioninstance")), acProps);
        }
        this.ap.processActionBlock(actionBlock);
        for (i = 0; i < actionBlock.getActionCount(); ++i) {
            String sapResponse = actionBlock.getActionProperty(i, "response");
            if (sapResponse.length() <= 0) continue;
            returnProps.setProperty("response", sapResponse);
            break;
        }
        returnProps.setProperty("status", "SUCCESS");
        return returnProps;
    }

    private DataSet filterDataSet(DataSet tempDataSet, String joinId) throws SapphireException {
        int i;
        HashMap<String, String> filt = new HashMap<String, String>();
        filt.put("sapjoinid", joinId);
        DataSet filterDS = this.sapMsgJoinFilterInfo.getFilteredDataSet(filt);
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

    public PropertyList createData(PropertyList pl) throws SapphireException {
        PropertyList dataSetList = new PropertyList();
        for (int i = 0; i < this.sapMsgQueryInfo.getRowCount(); ++i) {
            DataSet sourceDS;
            String sourceDSXML;
            String sqlString = this.sapMsgQueryInfo.getString(i, "sapquerytext");
            String dataSetName = this.sapMsgQueryInfo.getString(i, "datablockname");
            String queryid = this.sapMsgQueryInfo.getString(i, "sapqueryid");
            String sappQueryid = this.sapMsgQueryInfo.getString(i, "queryid");
            if (sappQueryid != null && sappQueryid.length() > 0) {
                String basedonid = this.sapMsgQueryInfo.getString(i, "basedonid");
                String[] params = new String[]{"", "", "", "", ""};
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sapmsgtypeid", this.sapMsgTypeId);
                filter.put("sapmsgtypeversionid", this.sapMsgTypeVersion);
                filter.put("sapqueryid", queryid);
                DataSet filterDS = this.sapMsgQueryArgInfo.getFilteredDataSet(filter);
                filterDS.sort("usersequence");
                for (int j = 0; j < filterDS.getRowCount(); ++j) {
                    String sourceSapQueryid = filterDS.getValue(i, "sourcesapqueryid");
                    String sourceColumnId = filterDS.getValue(i, "sourcesapcolumnid");
                    String argValue = this.sapMsgQueryArgInfo.getValue(i, "argvalue");
                    if (sourceSapQueryid != null && !sourceSapQueryid.trim().equals("")) {
                        sourceDSXML = dataSetList.getProperty(sourceSapQueryid);
                        if (sourceDSXML == null || sourceDSXML.length() == 0) {
                            throw new SapphireException("DataSource:" + sourceSapQueryid + " not found in memory");
                        }
                        sourceDS = new DataSet(dataSetList.getProperty(sourceSapQueryid));
                        if (sourceColumnId == null || sourceColumnId.equals("")) {
                            throw new SapphireException("Source columid not defined ");
                        }
                        if (!sourceDS.isValidColumn(sourceColumnId)) {
                            throw new SapphireException("Source columid" + sourceColumnId + " not found in the dataset ");
                        }
                        params[j] = sourceDS.getColumnValues(sourceColumnId, ";");
                        continue;
                    }
                    params[j] = argValue != null && !argValue.trim().equals("") ? argValue : pl.getProperty(this.sapMsgQueryArgInfo.getValue(j, "sapargid"));
                }
                this.createDataSet(dataSetList, params, basedonid, sappQueryid, dataSetName, queryid);
                continue;
            }
            if (sqlString != null && sqlString.length() > 0) {
                while (sqlString.indexOf("[") >= 0) {
                    String variable = sqlString.substring(sqlString.indexOf("[") + 1, sqlString.indexOf("]"));
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sapmsgtypeid", this.sapMsgTypeId);
                    filter.put("sapmsgtypeversionid", this.sapMsgTypeVersion);
                    filter.put("sapqueryid", queryid);
                    filter.put("sapargid", variable);
                    DataSet filterVariable = this.sapMsgQueryArgInfo.getFilteredDataSet(filter);
                    if (filterVariable.getRowCount() <= 0) {
                        throw new SapphireException("Variable not defined");
                    }
                    String argId = filterVariable.getString(0, "sapargid", "");
                    String argVal = filterVariable.getString(0, "argvalue", "");
                    String argType = filterVariable.getString(0, "argtype", "");
                    String sourceQueryId = filterVariable.getString(0, "sourcesapqueryid", "");
                    String sourceColumnId = filterVariable.getString(0, "sourcesapcolumnid", "");
                    if (!argVal.equals("")) {
                        sqlString = this.replaceVariable(argId, argVal, argType, sqlString);
                        continue;
                    }
                    if (!sourceQueryId.equals("")) {
                        sourceDSXML = dataSetList.getProperty(sourceQueryId);
                        if (sourceDSXML == null || sourceDSXML.length() == 0) {
                            throw new SapphireException("DataSource:" + sourceQueryId + " not found in memory");
                        }
                        sourceDS = new DataSet(sourceDSXML);
                        if (sourceColumnId == null || sourceColumnId.equals("")) {
                            throw new SapphireException("Source columid not defined ");
                        }
                        if (!sourceDS.isValidColumn(sourceColumnId)) {
                            throw new SapphireException("Source columid" + sourceColumnId + " not found in the dataset ");
                        }
                        argVal = sourceDS.getColumnValues(sourceColumnId, ";");
                        sqlString = this.replaceVariable(argId, argVal, argType, sqlString);
                        continue;
                    }
                    argVal = pl.getProperty(variable);
                    if (argVal == null || argVal.trim().equals("")) {
                        throw new SapphireException("Variable " + variable + " is not present in Action Property!");
                    }
                    sqlString = this.replaceVariable(argId, argVal, argType, sqlString);
                }
                this.createDataSet(sqlString, dataSetList, dataSetName, queryid);
                continue;
            }
            String datasetstr = pl.getProperty(queryid);
            if (datasetstr != null && datasetstr.length() != 0) {
                DataSet ds = new DataSet(datasetstr);
                dataSetList.setProperty(queryid, ds.toXML());
                continue;
            }
            throw new SapphireException("No input property or sql found for: " + queryid);
        }
        dataSetList = this.setJoinRefs(dataSetList);
        return this.mapSapphireToSAP(dataSetList);
    }

    private PropertyList setJoinRefs(PropertyList dataSetList) {
        return dataSetList;
    }

    private PropertyList mapSapphireToSAP(PropertyList sapphireData) throws SapphireException {
        int i;
        PropertyList sap = new PropertyList();
        Set propertyNames = sapphireData.keySet();
        Object[] sapphireQueryIdList = propertyNames.toArray();
        String[] sapDataBlocks = new String[sapphireQueryIdList.length];
        String[] sapMappedDataSet = new String[sapphireQueryIdList.length];
        int offset = 1;
        for (i = 0; i < sapphireData.size(); ++i) {
            String sapphireQueryId = (String)sapphireQueryIdList[i];
            String sapBlockName = "";
            for (int j = 0; j < this.sapMsgQueryInfo.getRowCount(); ++j) {
                String currSAPBlockName = this.sapMsgQueryInfo.getValue(j, "DATABLOCKNAME");
                String currSapphireQueryId = this.sapMsgQueryInfo.getValue(j, "SAPQUERYID");
                if (!sapphireQueryId.trim().equals(currSapphireQueryId.trim())) continue;
                sapBlockName = currSAPBlockName;
                break;
            }
            if (sapBlockName == null || sapBlockName.length() == 0) {
                throw new SapphireException("Sap BlockName not defined for sapphire query id:" + sapphireQueryId);
            }
            sapDataBlocks[i] = sapBlockName;
            sapMappedDataSet[i] = this.mapSapphireDataSet(sapBlockName, sapphireData.getProperty(sapphireQueryId), offset);
            offset += new DataSet(sapMappedDataSet[i]).getRowCount();
        }
        for (i = 0; i < sapMappedDataSet.length; ++i) {
            sapphireData.setProperty(sapDataBlocks[i], sapMappedDataSet[i]);
        }
        return sapphireData;
    }

    private String mapSapphireDataSet(String sapBlockName, String sapphireDataSetXML, int offset) throws SapphireException {
        int i;
        DataSet sapphireDS = new DataSet(sapphireDataSetXML);
        String[] sapphireColumns = sapphireDS.getColumns();
        String[] mappedSapphireColumns = new String[sapphireColumns.length];
        String[] sapColumns = new String[sapphireColumns.length];
        DataSet sapDS = new DataSet();
        int sapColCount = 0;
        for (i = 0; i < sapphireColumns.length; ++i) {
            String sapcol = this.findSAPColumnName(sapBlockName, sapphireColumns[i]);
            if (sapcol == null || sapcol.length() == 0) {
                Trace.log("Did not find the sapColumnId for " + sapphireColumns[i]);
                continue;
            }
            sapColumns[sapColCount] = sapcol;
            mappedSapphireColumns[sapColCount] = sapphireColumns[i];
            sapDS.addColumn(sapColumns[sapColCount++], sapphireDS.getColumnType(sapphireColumns[i]));
        }
        for (int row = 0; row < sapphireDS.getRowCount(); ++row) {
            sapDS.addRow(row);
            for (int col = 0; col < sapColCount; ++col) {
                String sapphireColVal = sapphireDS.getValue(row, mappedSapphireColumns[col]);
                sapDS.setValue(row, sapColumns[col], sapphireColVal);
            }
        }
        sapDS.addColumn("key", 1);
        for (i = 0; i < sapDS.getRowCount(); ++i) {
            sapDS.setNumber(i, "key", offset + i);
        }
        return sapDS.toXML();
    }

    private String findSAPColumnName(String sapphireQueryId, String sapphireColumnName) {
        for (int row = 0; row < this.sapMsgQueryColumnMapInfo.getRowCount(); ++row) {
            String currSapphireQueryId = this.sapMsgQueryColumnMapInfo.getValue(row, "SAPQUERYID");
            String currSapphireColumnName = this.sapMsgQueryColumnMapInfo.getValue(row, "SAPCOLUMNID");
            if (!sapphireQueryId.equals(currSapphireQueryId) || !sapphireColumnName.equalsIgnoreCase(currSapphireColumnName)) continue;
            return this.sapMsgQueryColumnMapInfo.getValue(row, "EXTERNALCOLUMNID");
        }
        return "";
    }

    private void createDataSet(String sqlString, PropertyList dataSetList, String paramName, String queryId) throws SapphireException {
        DataSet ds = this.qp.getSqlDataSet(sqlString);
        dataSetList.setProperty(queryId, ds.toXML());
    }

    private void createDataSet(PropertyList datasetList, String[] param, String sdcId, String sappQueryid, String paramName, String queryId) {
        SDIRequest request = new SDIRequest();
        request.setSDCid(sdcId);
        request.setQueryid(sappQueryid);
        request.setRequestItem("primary");
        request.setQueryParams(param);
        SDIData sdidata = this.sdip.getSDIData(request);
        DataSet ds = sdidata.getDataset("primary");
        datasetList.put(queryId, ds.toXML());
    }

    private String replaceVariable(String argId, String argVal, String argType, String sqlString) {
        String match = "[" + argId + "]";
        StringBuffer sqlBuffer = new StringBuffer(sqlString);
        while (sqlBuffer.indexOf(match) >= 0) {
            String leftSide = this.stripExtraCharacter(sqlBuffer.substring(0, sqlBuffer.indexOf(match) - 1), "left");
            String rightSide = this.stripExtraCharacter(sqlBuffer.substring(sqlBuffer.indexOf(match) + match.length()), "right");
            String[] argValArr = argVal.split(";");
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
}

