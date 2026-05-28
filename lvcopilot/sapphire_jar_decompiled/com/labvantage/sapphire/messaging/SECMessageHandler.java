/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.messaging;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.error.ErrorDetail;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SECMessageHandler
extends SapphireSaxHandler {
    private StringBuffer currentElementChars = new StringBuffer();
    private String parseFlag = "";
    private static final String ZHEADER = "ZHeader";
    private static final String MSGID = "MsgID";
    private static final String MSGREFID = "MsgRefID";
    private static final String MSGFLOW = "MsgFlow";
    private static final String MSGTYPE = "MsgType";
    private static final String MSGNAME = "MsgName";
    private static final String MSGVERSION = "MsgVersion";
    private static final String ZDATA = "ZData";
    private static final String ZCOLUMN = "ZColumn";
    private static final String ZCOLUMNS = "ZColumns";
    private static final String KEY = "Key";
    private static final String REF = "Ref";
    private static final String PARAMNAME = "ParamName";
    private static final String COLUMNKEY = "ColumnKey";
    private static final String COLUMNVAL = "ColumnVal";
    private static final String COLUMNTYPE = "ColumnType";
    private String msglogId = null;
    PropertyList header;
    PropertyList dataBlocks;
    String responseMessage = "";
    String errorMessage = "";
    private String currentParamName;
    private String currentKey;
    private String currentRef;
    private String currentColKey;
    private String currentColType;
    private String currentColVal;
    private DataSet currentDataSet;
    private int currentDataSetRow;
    StringBuffer log;

    @Override
    public void startDocument() throws SAXException {
        this.header = new PropertyList();
        this.dataBlocks = new PropertyList();
        this.log = new StringBuffer();
    }

    @Override
    public void endDocument() throws SAXException {
        SAPMessageTypeUtil smtu = new SAPMessageTypeUtil(this.connectionid, this.header);
        try {
            smtu.process(this.dataBlocks);
            this.responseMessage = smtu.getResponseMessage();
            this.log = smtu.getLog();
        }
        catch (ActionException e) {
            ErrorDetail errorDetail;
            String error = e.getMessage();
            if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors() && (error = (errorDetail = (ErrorDetail)e.getErrorHandler().get(0)).getMessage()).endsWith("|")) {
                error = error.substring(0, error.lastIndexOf("|"));
            }
            this.errorMessage = error;
            throw new SAXException(error);
        }
        catch (SapphireException e) {
            this.errorMessage = e.getMessage();
            throw new SAXException("Failed to process incoming message:" + e.getMessage());
        }
    }

    public PropertyList getHeader() {
        return this.header;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public String getError() {
        return this.errorMessage;
    }

    public StringBuffer getLog() {
        return this.log;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        if (ZHEADER.equals(qName)) {
            this.parseFlag = "headerMessage";
        } else if (ZDATA.equals(qName)) {
            this.parseFlag = "dataSection";
            this.currentParamName = "";
        } else if (ZCOLUMNS.equals(qName)) {
            if (this.currentParamName == null || this.currentParamName.length() == 0 || this.currentKey == null || this.currentKey.length() == 0) {
                throw new SAXException("PARAMNAME and KEY are mandatory elements");
            }
            String dataBlockXML = this.dataBlocks.getProperty(this.currentParamName);
            if (dataBlockXML == null || dataBlockXML.length() == 0) {
                this.currentDataSet = new DataSet();
                this.currentDataSet.addColumn("key", 1);
                this.currentDataSet.addColumn("ref", 1);
                this.currentDataSet.addRow(0);
                this.currentDataSet.setValue(0, "key", this.currentKey);
                this.currentDataSet.setValue(0, "ref", this.currentRef);
                this.dataBlocks.setProperty(this.currentParamName, this.currentDataSet.toXML());
                this.currentDataSetRow = 0;
            } else {
                this.currentDataSet = new DataSet(dataBlockXML);
                this.currentDataSetRow = this.currentDataSet.addRow();
                this.currentDataSet.setValue(this.currentDataSetRow, "key", this.currentKey);
                this.currentDataSet.setValue(this.currentDataSetRow, "ref", this.currentRef);
            }
        } else if (ZCOLUMN.equals(qName)) {
            this.currentColKey = "";
            this.currentColType = "";
            this.currentColVal = "";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (MSGID.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGID, this.currentElementChars.toString());
        } else if (MSGREFID.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGREFID, this.currentElementChars.toString());
        } else if (MSGNAME.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGNAME, this.currentElementChars.toString());
        } else if (MSGVERSION.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGVERSION, this.currentElementChars.toString());
        } else if (MSGFLOW.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGFLOW, this.currentElementChars.toString());
        } else if (MSGTYPE.equals(qName) && this.parseFlag.equals("headerMessage")) {
            this.header.put(MSGTYPE, this.currentElementChars.toString());
        } else if (PARAMNAME.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentParamName = this.currentElementChars.toString();
        } else if (KEY.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentKey = this.currentElementChars.toString();
        } else if (REF.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentRef = this.currentElementChars.toString();
        } else if (COLUMNVAL.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentColVal = this.currentElementChars.toString();
        } else if (COLUMNTYPE.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentColType = this.currentElementChars.toString();
        } else if (COLUMNKEY.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.currentColKey = this.currentElementChars.toString();
        } else if (ZCOLUMN.equals(qName) && this.parseFlag.equals("dataSection")) {
            if (this.currentColKey.length() == 0) {
                throw new SAXException("ColumnKey cannot be emptry");
            }
            int dataType = 0;
            if (this.currentColType.length() == 0) {
                this.currentColType = "STRING";
            }
            if (this.currentColType.equals("NUMBER")) {
                dataType = 1;
            } else if (this.currentColType.equals("DATE")) {
                dataType = 2;
            }
            if (!this.currentDataSet.isValidColumn(this.currentColKey)) {
                this.currentDataSet.addColumn(this.currentColKey, dataType);
            }
            this.currentDataSet.setValue(this.currentDataSetRow, this.currentColKey, this.currentColVal);
        } else if (ZCOLUMNS.equals(qName) && this.parseFlag.equals("dataSection")) {
            this.dataBlocks.setProperty(this.currentParamName, this.currentDataSet.toXML());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    public class SAPMessageTypeUtil {
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
        String responseMessage = "";
        StringBuffer log;

        public SAPMessageTypeUtil(String connectionId, PropertyList header) {
            this.qp = new QueryProcessor(connectionId);
            this.ap = new ActionProcessor(connectionId);
            this.sapMsgTypeId = header.getProperty(SECMessageHandler.MSGNAME);
            this.sapMsgTypeVersion = header.getProperty(SECMessageHandler.MSGVERSION);
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
            this.log = new StringBuffer();
        }

        public String getResponseMessage() {
            return this.responseMessage;
        }

        public StringBuffer getLog() {
            return this.log;
        }

        public PropertyList process(PropertyList data) throws SapphireException {
            this.log.append("Mapping message to Sapphire dataset\n");
            PropertyList mappedData = this.mapSAPToSapphire(data);
            this.log.append("Creating join datasets.\n");
            mappedData = this.createJoinDataSets(mappedData);
            this.log.append("Starting execution of configured actions.\n");
            this.responseMessage = this.executeActions(mappedData);
            return mappedData;
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

        private DataSet getSAPMsgTypeInfo() {
            DataSet result = null;
            String sql = "SELECT SAPMSGTYPEID,SAPMSGTYPEVERSIONID,SAPMSGTYPEDESC,TYPEFLAG,DIRECTIONFLAG FROM SAPMsgType where SAPMsgTypeId = ?  AND sapmsgtypeversionid = ?";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgQueryInfo() {
            DataSet result = null;
            String sql = "SELECT SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPQUERYID, SAPQUERYTEXT, QUERYID, BASEDONID, DATABLOCKNAME FROM SAPMSGQUERY WHERE  SAPMSGTYPEID = ? AND SAPMSGTYPEVERSIONID = ? ";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgQueryArgInfo() {
            DataSet result = null;
            String sql = "SELECT SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPQUERYID, SAPARGID,ARGVALUE,ARGTYPE,SOURCESAPQUERYID,SOURCESAPCOLUMNID,USERSEQUENCE FROM SAPMSGQUERYARG WHERE  SAPMSGTYPEID = ? AND SAPMSGTYPEVERSIONID = ? ";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgQueryColumnMap() {
            DataSet result = null;
            String sql = "SELECT  SAPMSGTYPEID,SAPMSGTYPEVERSIONID,SAPQUERYID, SAPCOLUMNID, EXTERNALCOLUMNID FROM SAPMSGQUERYCOLUMNMAP WHERE  SAPMSGTYPEID = ? AND SAPMSGTYPEVERSIONID = ? ";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgJoinInfo() {
            DataSet result = null;
            String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID FROM SAPMSGJOIN WHERE  SAPMSGTYPEID = ? AND SAPMSGTYPEVERSIONID = ?";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgJoinCriteriaInfo() {
            String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID, SAPCRITERIAID,LEFTSAPQUERYID, LEFTSAPCOLUMNID, RIGHTSAPQUERYID, RIGHTSAPCOLUMNID FROM SAPMSGJOINCRITERIA WHERE  SAPMSGTYPEID = ? AND SAPMSGTYPEVERSIONID = ?";
            return this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        }

        private DataSet getSAPMsgFilterInfo() {
            String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, SAPJOINID,  SAPFILTERID, SAPQUERYID, SAPCOLUMNID FROM SAPMSGJOINFILTER WHERE  SAPMSGTYPEID = ?  AND SAPMSGTYPEVERSIONID = ?";
            return this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
        }

        private DataSet getSAPMsgActionInfo() {
            DataSet result = null;
            String sql = "SELECT  SAPMSGTYPEID, SAPMSGTYPEVERSIONID, ACTIONID, ACTIONVERSIONID, ACTIONINSTANCE FROM SAPMSGACTION WHERE  SAPMSGTYPEID = ?  AND SAPMSGTYPEVERSIONID= ? ORDER BY ACTIONINSTANCE";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private DataSet getSAPMsgActionPropertiesInfo() {
            DataSet result = null;
            String sql = "SELECT  SAPMSGTYPEID,SAPMSGTYPEVERSIONID,ACTIONID,ACTIONVERSIONID,ACTIONINSTANCE,PROPERTYID,PROPERTYVALUE,PROPERTYTYPE FROM SAPMSGACTIONPROPERTY WHERE  SAPMSGTYPEID=? AND SAPMSGTYPEVERSIONID=? ORDER BY ACTIONINSTANCE";
            result = this.qp.getPreparedSqlDataSet(sql, new Object[]{this.sapMsgTypeId, this.sapMsgTypeVersion});
            return result;
        }

        private String executeActions(PropertyList mappedData) throws SapphireException {
            int i;
            ActionBlock actionBlock = new ActionBlock();
            if (this.sapMsgActionInfo.getRowCount() == 0) {
                throw new SapphireException("No actions specified to process the message " + this.sapMsgTypeId);
            }
            for (i = 0; i < this.sapMsgActionInfo.getRowCount(); ++i) {
                this.log.append("Setting up properties for action:" + this.sapMsgActionInfo.getString(i, "actionid"));
                actionBlock.setAction(Integer.toString(this.sapMsgActionInfo.getInt(i, "actioninstance")), this.sapMsgActionInfo.getString(i, "actionid"), this.sapMsgActionInfo.getString(i, "actionversionid"));
                HashMap<String, Object> filter = new HashMap<String, Object>();
                filter.put("actionid", this.sapMsgActionInfo.getString(i, "actionid"));
                filter.put("actionversionid", this.sapMsgActionInfo.getString(i, "actionversionid"));
                filter.put("actioninstance", new BigDecimal(this.sapMsgActionInfo.getValue(i, "actioninstance")));
                DataSet currentActionProps = this.sapMsgActionPropertiesInfo.getFilteredDataSet(filter);
                PropertyList acProps = new PropertyList();
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
                        acProps.setProperty(propertyid, substitutedPropertyValue);
                        continue;
                    }
                    if (propertyValue.startsWith("HEADER")) {
                        String[] tempArr = StringUtil.split(propertyValue, ".");
                        if (tempArr.length != 2 || tempArr[0].length() == 0 || tempArr[1].length() == 0) {
                            throw new SapphireException("Property value specified incorrectly " + propertyValue);
                        }
                        acProps.setProperty(propertyid, this.header.getProperty(tempArr[1]));
                        continue;
                    }
                    acProps.setProperty(propertyid, propertyValue);
                }
                this.log.append("Adding action to actionblock with properties:\n");
                this.log.append(acProps.toString());
                actionBlock.setActionProperties(Integer.toString(this.sapMsgActionInfo.getInt(i, "actioninstance")), acProps);
            }
            this.log.append("Executing Action Block.\n");
            this.ap.processActionBlock(actionBlock);
            this.log.append("Completed Action Block.\n");
            for (i = 0; i < actionBlock.getActionCount(); ++i) {
                String sapResponse = actionBlock.getActionProperty(i, "responsemessage");
                if (sapResponse.length() <= 0) continue;
                this.log.append("Response message is:\n");
                this.log.append(sapResponse);
                return sapResponse;
            }
            return "";
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
                        if (leftvalue == null || leftvalue.equals(rightvalue)) continue;
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
    }
}

