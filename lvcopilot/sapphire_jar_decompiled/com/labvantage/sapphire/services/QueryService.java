/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.IntHolder;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QueryService
extends BaseService
implements DDTConstants,
CacheNames {
    public static final String LOGNAME = "QueryService";

    public QueryService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public DataSet getSqlDataSet(String sql) throws ServiceException {
        return this.getSqlDataSet("", sql, false);
    }

    public DataSet getSqlDataSet(String name, String sql) throws ServiceException {
        return this.getSqlDataSet(name, sql, false);
    }

    public DataSet getSqlDataSet(String sql, boolean extendedDataTypes) throws ServiceException {
        return this.getSqlDataSet("", sql, extendedDataTypes);
    }

    public DataSet getSqlDataSet(String name, String sql, boolean extendedDataTypes) throws ServiceException {
        return this.getSqlDataSet(name, sql, extendedDataTypes, -1);
    }

    public DataSet getSqlDataSet(String name, String sql, boolean extendedDataTypes, int queryTimeOut) throws ServiceException {
        if (name == null || name.equals("")) {
            name = "LV_QueryData";
        }
        this.logInfo("Getting DataSet (" + name + ") for sql: " + sql);
        if (sql == null || sql.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SQL not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            if (queryTimeOut >= 0) {
                db.setQueryTimeout(queryTimeOut);
            }
            db.createResultSet(name, sql);
            DataSet ds = new DataSet();
            ds.setResultSet(db.getResultSet(name), extendedDataTypes, db.getDbms());
            DataSet dataSet = ds;
            return dataSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get sql result set. Exception: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public DataSet getSqlDataSet(int sqlCode, Object[] bindVars, boolean extendedDataTypes) throws ServiceException {
        String sql = SQLRegister.getSQL(this.sapphireConnection.getDatabaseId(), sqlCode);
        return this.getPreparedSqlDataSet(sql, bindVars, extendedDataTypes);
    }

    public DataSet getPreparedSqlDataSet(String sql, Object[] params) throws ServiceException {
        return this.getPreparedSqlDataSet("", sql, params, false);
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params) throws ServiceException {
        return this.getPreparedSqlDataSet(name, sql, params, false);
    }

    public DataSet getPreparedSqlDataSet(String sql, Object[] params, boolean extendedDataTypes) throws ServiceException {
        return this.getPreparedSqlDataSet("", sql, params, extendedDataTypes);
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes) throws ServiceException {
        return this.getPreparedSqlDataSet(name, sql, params, extendedDataTypes, -1);
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout) throws ServiceException {
        if (name == null || name.equals("")) {
            name = "LV_QueryData";
        }
        if (name.contains("_debug")) {
            name = StringUtil.replaceAll(name, "_debug", "");
            this.logDebug("Getting DataSet (" + name + ") for sql: " + sql + this.logParams(params));
        } else if (!name.contains("_nolog")) {
            this.logInfo("Getting DataSet (" + name + ") for sql: " + sql + this.logParams(params));
        }
        if (sql == null || sql.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SQL not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            if (queryTimeout >= 0) {
                db.setQueryTimeout(queryTimeout);
            }
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet(name, sql, params);
            DataSet ds = new DataSet();
            ds.setResultSet(db.getResultSet(name), extendedDataTypes, db.getDbms());
            DataSet dataSet = ds;
            return dataSet;
        }
        catch (SapphireException se) {
            if (name.contains("_noexception")) {
                DataSet dataSet = null;
                return dataSet;
            }
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get sql result set. Exception: " + se.getMessage(), se);
        }
        finally {
            db.reset();
        }
    }

    public DataSet getPreparedSqlDataSet(int sqlCode, Object[] params, boolean extendedDataTypes) throws ServiceException {
        String sql = SQLRegister.getSQL(this.sapphireConnection.getDatabaseId(), sqlCode);
        return this.getPreparedSqlDataSet(sql, params, extendedDataTypes);
    }

    public DataSet getRefTypeDataSet(String reftypeid) throws ServiceException {
        DataSet returnValues;
        this.logInfo("Getting DataSet for reftype '" + reftypeid + "'");
        if (reftypeid == null || reftypeid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "RefTypeId not specified");
        }
        String cacheXML = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "RefValues", reftypeid);
        if (cacheXML == null) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                String sql = "select reftypeid, refvalueid, refvaluedesc, refdisplayvalue, usersequence, refdisplayicon from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = ? order by usersequence, refvalueid";
                db.createPreparedResultSet(sql, reftypeid);
                this.logInfo("SQL: " + sql + ". Reftypeid: " + reftypeid);
                returnValues = new DataSet(db.getResultSet());
                if (XSS.isMock()) {
                    for (int i = 0; i < returnValues.size(); ++i) {
                        String displayValue = returnValues.getValue(i, "refdisplayvalue");
                        if (displayValue.length() > 0) {
                            returnValues.setString(i, "refdisplayvalue", XSS.mock(displayValue, "R: " + reftypeid));
                        }
                        returnValues.setString(i, "refvalueid", XSS.mock(returnValues.getValue(i, "refvalueid"), "R: " + reftypeid));
                        returnValues.setString(i, "refvaluedesc", XSS.mock(returnValues.getValue(i, "refvaluedesc"), "R: " + reftypeid));
                    }
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "RefValues", reftypeid, returnValues.toXML());
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get reftype result set. Exception: " + se.getMessage(), se);
            }
            finally {
                db.reset();
            }
        } else {
            returnValues = new DataSet(cacheXML);
        }
        return returnValues;
    }

    public int execPreparedUpdate(String sql, Object[] bindVars) throws ServiceException {
        this.logInfo("Executing sql '" + sql + "'");
        if (sql != null && sql.length() > 0) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                int n = db.executePreparedUpdate(sql, bindVars);
                return n;
            }
            catch (SapphireException se) {
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to execute the SQL '" + sql + "'", se);
            }
            finally {
                db.reset();
            }
        }
        return 0;
    }

    public int execSQL(String sql) throws ServiceException {
        this.logInfo("Executing sql '" + sql + "'");
        if (sql != null && sql.length() > 0) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                int n = db.executeUpdate(sql);
                return n;
            }
            catch (SapphireException se) {
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to execute the SQL '" + sql + "'", se);
            }
            finally {
                db.reset();
            }
        }
        return 0;
    }

    public int execSQL(int sqlCode, Object[] bindVars) throws ServiceException {
        String sql = SQLRegister.getSQL(this.sapphireConnection.getDatabaseId(), sqlCode);
        this.logInfo("Executing sql '" + sql + "'");
        if (sql != null && sql.length() > 0) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                if (bindVars == null) {
                    int n = db.executeUpdate(sql);
                    return n;
                }
                int n = db.executePreparedUpdate(sql, bindVars);
                return n;
            }
            catch (SapphireException se) {
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to execute the SQL '" + sql + "'", se);
            }
            finally {
                db.reset();
            }
        }
        return 0;
    }

    public String getKeyid1List(String sdcid, String queryid, String[] params) throws ServiceException {
        String rsetid;
        this.logInfo("Getting keyid1 list for sdcid '" + sdcid + "', qyeryid '" + queryid + "'");
        if (queryid == null || queryid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Queryid not specified");
        }
        DAMProcessor dam = this.getDAMProcessor();
        try {
            rsetid = dam.createRSetQ(sdcid, queryid, params);
        }
        catch (SapphireException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for query " + queryid, e);
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String select = "SELECT keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid = ? ORDER BY rsetseq";
            db.createPreparedResultSet(select, rsetid);
            StringBuffer keyid1 = new StringBuffer();
            StringBuffer keyid2 = new StringBuffer();
            StringBuffer keyid3 = new StringBuffer();
            if (db.getNext()) {
                keyid1.append(db.getString("keyid1"));
                keyid2.append(db.getString("keyid2"));
                keyid3.append(db.getString("keyid3"));
                while (db.getNext()) {
                    keyid1.append(";").append(db.getString("keyid1"));
                    keyid2.append(";").append(db.getString("keyid2"));
                    keyid3.append(";").append(db.getString("keyid3"));
                }
            }
            String string = keyid1.toString();
            return string;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get keyids from rset. Exception: " + se.getMessage(), se);
        }
        finally {
            db.reset();
            dam.clearRSet(rsetid);
        }
    }

    public String getSecurityFilterWhere(String sdcid) throws ServiceException {
        String securityfilter = "";
        if (this.sapphireConnection.isOracle()) {
            String sql = "SELECT LV_RSET.GetSecuritySQL( '" + sdcid + "', '" + this.sapphireConnection.getSysuserId() + "', 'list', '', '" + this.sapphireConnection.getCurrentJobtype() + "' ) sf FROM dual";
            DataSet sds = new QueryService(this.sapphireConnection).getSqlDataSet(sql);
            securityfilter = sds.getValue(0, "sf");
        } else if (this.sapphireConnection.isSqlServer()) {
            try {
                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                db.setConnection(this.sapphireConnection);
                String callstmt = "{ call LV_RSET_GetSecuritySQL( ?, ?, ?, ?, ?, ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                cs.setString(1, sdcid);
                cs.setString(2, this.sapphireConnection.getSysuserId());
                cs.setString(3, "list");
                cs.setString(4, null);
                cs.registerOutParameter(5, 12);
                cs.setString(6, this.sapphireConnection.getCurrentJobtype());
                cs.executeUpdate();
                securityfilter = cs.getString(5);
                if (securityfilter == null) {
                    securityfilter = "";
                }
            }
            catch (Exception se) {
                throw new ServiceException(se);
            }
        }
        return securityfilter;
    }

    public int getSDICount(SDIRequest sdiRequest) throws ServiceException {
        sdiRequest.setCountRequest(true);
        SDIData sdiData = this.getSDIData(sdiRequest);
        DataSet primary = sdiData != null ? sdiData.getDataset("primary") : null;
        return primary != null && primary.size() == 1 ? primary.getInt(0, "count") : -1;
    }

    public SDIData getSDIData(SDIRequest sdiRequest) throws ServiceException {
        this.logInfo("Getting SDI Data");
        DataAccessService das = new DataAccessService(this.sapphireConnection);
        String rsetid = sdiRequest.getRsetid();
        if (rsetid.indexOf("|") > -1) {
            String[] rsetids = StringUtil.split(rsetid, "|");
            for (int i = 0; i < rsetids.length; ++i) {
                if (i == 0) {
                    rsetid = rsetids[i];
                    continue;
                }
                try {
                    das.clearRSet(new RSet(rsetids[i]));
                    continue;
                }
                catch (Exception e) {
                    this.logger.warn("Failed to clear rset " + rsetids[i]);
                }
            }
        }
        HashMap<String, String> fkrsetid = new HashMap<String, String>();
        int requeststatus = 1;
        int qualifiedrows = 0;
        DDTService ddtService = new DDTService(this.sapphireConnection);
        PropertyList sdcProps = ddtService.getSDCProperties(sdiRequest.getSDCid());
        if (sdcProps == null) {
            throw new ServiceException("GET_SDCDATA_FAILED", "Failed to get sdc data for sdc: " + sdiRequest.getSDCid());
        }
        sdiRequest.setSDCid(sdcProps.getProperty("sdcid"));
        PropertyListCollection sdcCols = sdcProps.getCollection("columns");
        PropertyListCollection sdcLinks = sdcProps.getCollection("links");
        PropertyListCollection detailLinks = sdcProps.getCollection("detaillinks");
        String sdcid = sdcProps.getProperty("sdcid");
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        boolean rsetQuery = true;
        int maxListItems = 100;
        int listItems = 1;
        String listFrom = "";
        String listWhere = "";
        String rsetitemsTable = "rsetitems";
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        if (sdiRequest.getQueryTimeout() >= 0) {
            db.setQueryTimeout(sdiRequest.getQueryTimeout());
        }
        try {
            SDIRequest[] extRequests;
            String workitemRequest;
            String pricelistRequest;
            String request;
            String specRequest;
            StringBuffer select;
            String primaryRequest;
            int oraVersion;
            db.setConnection(this.sapphireConnection);
            ArrayList<String> listFromParamList = null;
            ArrayList<String> listWhereParamList = null;
            ArrayList<String> listQueryParamList = null;
            if (sdiRequest.getRetrieve() && (sdiRequest.getRsetid() == null || sdiRequest.getRsetid().length() == 0)) {
                listItems = StringUtil.split(sdiRequest.getKeyid1List(), ";").length;
                if (!(sdiRequest.isQueryRequest() || listItems > maxListItems || sdiRequest.getRetainRsetid() || sdiRequest.isLockRequest() || sdiRequest.containsDataRequest() || sdiRequest.getAltKeyList() != null && sdiRequest.getAltKeyList().length() != 0 || sdiRequest.getQueryWhere() != null && sdiRequest.getQueryWhere().length() != 0 || sdiRequest.getSDCid().equals("SDC") || sdiRequest.getSDIRequests().length != 0)) {
                    listFromParamList = new ArrayList<String>();
                    listWhereParamList = new ArrayList<String>();
                    listQueryParamList = new ArrayList<String>();
                    this.logInfo("Bypassing RSET for list based request: " + sdiRequest.getKeyid1List());
                    SDIList sdiListTemp = new SDIList();
                    sdiListTemp.setSdcid(sdcid);
                    sdiListTemp.setAllowDups(false);
                    sdiListTemp.addSDIList(sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List());
                    SDI sdiList = sdiRequest.isShowHiddenRecords() && sdiRequest.getSecurityBypassCode() == 1 ? new SDI(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List()) : das.checkSDIAccess(sdiListTemp, sdiRequest.isShowHiddenRecords());
                    rsetQuery = false;
                    StringBuffer listWhereBuilder = new StringBuffer();
                    if (listItems == 1) {
                        listWhereBuilder.append(tableid + "." + keycolid1 + "=?");
                        listWhereParamList.add(sdiList.getKeyid1());
                        if (keycols > 1) {
                            listWhereBuilder.append(" AND " + tableid + "." + keycolid2 + "=?");
                            listWhereParamList.add(sdiList.getKeyid2());
                        }
                        if (keycols > 2) {
                            listWhereBuilder.append(" AND " + tableid + "." + keycolid3 + "=?");
                            listWhereParamList.add(sdiList.getKeyid3());
                        }
                        listQueryParamList.addAll(listWhereParamList);
                        listFrom = tableid;
                    } else {
                        listWhereBuilder.append("id_value = " + tableid + "." + keycolid1 + (keycols > 1 ? " AND id2_value = " + tableid + "." + keycolid2 : "") + (keycols > 2 ? " AND id3_value = " + tableid + "." + keycolid3 : ""));
                        listFromParamList.add(sdiList.getKeyid1());
                        if (db.isOracle()) {
                            StringBuffer keyId2Temp = new StringBuffer();
                            if (keycols == 1) {
                                for (int i = 0; i < listItems; ++i) {
                                    keyId2Temp.append(i > 0 ? ";" : "").append("(null)");
                                }
                            }
                            String tableOrder = "TABLE( LV_orderTab( ?, ?" + (keycols > 2 ? ", ?" : "") + " ))";
                            listFrom = tableOrder + ", " + tableid;
                            listFromParamList.add(keycols > 1 ? sdiList.getKeyid2() : keyId2Temp.toString());
                            if (keycols > 2) {
                                listFromParamList.add(sdiList.getKeyid3());
                            }
                            listWhereBuilder.append(" AND ( ").append(tableid).append(".").append(keycolid1).append(keycols > 1 ? "," + tableid + "." + keycolid2 : ", '(null)'").append(keycols > 2 ? "," + tableid + "." + keycolid3 : "").append(") IN ( SELECT id_value , id2_value").append(keycols > 2 ? ", id3_value" : "").append(" FROM ").append(tableOrder).append(" )");
                        } else {
                            String tableOrder = "LV_orderTab( ? " + (keycols > 1 ? ", ?" : ", default") + (keycols > 2 ? ", ?" : ", default") + ", default )";
                            listFrom = tableOrder + ", " + tableid;
                            if (keycols > 1) {
                                listFromParamList.add(sdiList.getKeyid2());
                                if (keycols > 2) {
                                    listFromParamList.add(sdiList.getKeyid3());
                                }
                            }
                            listWhereBuilder.append(" AND ").append(tableid).append(".").append(keycolid1).append(" IN ( SELECT id_value FROM ").append(tableOrder).append(" )");
                            if (keycols > 1) {
                                listWhereBuilder.append(" AND ").append(tableid).append(".").append(keycolid1).append(keycols > 1 ? " + " + tableid + "." + keycolid2 : "").append(keycols > 2 ? " + " + tableid + "." + keycolid3 : "").append(" IN ( SELECT id_value ").append(keycols > 1 ? " + id2_value" : "").append(keycols > 2 ? " + id3_value" : "").append(" FROM ").append(tableOrder).append(" )");
                                listWhereParamList.addAll(listFromParamList);
                            }
                        }
                        listWhereParamList.addAll(listFromParamList);
                        listQueryParamList.addAll(listFromParamList);
                        listQueryParamList.addAll(listWhereParamList);
                    }
                    listWhere = listWhereBuilder.toString();
                } else {
                    if (!(sdiRequest.getQueryid().length() <= 0 || sdiRequest.getRetainRsetid() || sdiRequest.isLockRequest() || sdiRequest.getSDCid().equals("SDC") || sdiRequest.containsDataRequest())) {
                        this.logInfo("Alternate RSET table used for query based request: " + sdiRequest.getQueryid());
                        rsetitemsTable = "rsetitemsnl";
                    }
                    RSet rset = this.createRSet(sdiRequest, sdcProps, rsetitemsTable);
                    rsetid = rset.getRsetid();
                    qualifiedrows = rset.getQualifiedRows();
                    requeststatus = rset.getRequestStatus();
                }
            }
            String RSETITEMS_COLS = " " + rsetitemsTable + ".rsetseq \"__rsetseq\", " + rsetitemsTable + ".lockstate \"__lockstate\", " + rsetitemsTable + ".sysuserid \"__lockedby\"" + ("rsetitems".equals(rsetitemsTable) ? ", " + rsetitemsTable + ".checkedoutbyuserid \"__checkedoutbyuser\", " + rsetitemsTable + ".checkedoutbydepartmentid \"__checkedoutbydepartment\" " : "");
            String RSETITEMSDS_COLS = " rsetitemsds.rsetseq \"__rsetseq\", rsetitemsds.lockstate \"__lockstate\", rsetitemsds.sysuserid \"__lockedby\" ";
            String TRACELOGAUDIT_COLS = " tracelog.reason \"auditreason\", tracelog.activity \"auditactivity\", tracelog.signedflag \"auditsignedflag\", tracelog.createdt \"auditdt\" ";
            String SUBST_RSETITEMS_COLS = (listFrom.equals(tableid) ? " 0" : " seq_value") + " \"__rsetseq\", 0 \"__lockstate\", NULL \"__lockedby\" ";
            StringBuffer extendedSelect = new StringBuffer();
            StringBuffer extendedFrom = new StringBuffer();
            StringBuffer extendedWhere = new StringBuffer();
            try {
                oraVersion = db.isOracle() ? Integer.parseInt(System.getProperty("sapphire.oracle.version." + this.sapphireConnection.getDatabaseId())) : -1;
            }
            catch (Exception e) {
                oraVersion = 9;
            }
            SDIData sdiData = new SDIData(sdcid, keycolid1, keycolid2, keycolid3);
            String keycolumnsRequest = sdiRequest.getRequest("primarykeys");
            if (keycolumnsRequest.length() > 0 && !keycolumnsRequest.equals("all")) {
                sdiRequest.setRequestItem("primary[" + keycolid1 + (keycolid2.length() > 0 ? ", " + keycolid2 : "") + (keycolid3.length() > 0 ? ", " + keycolid3 : "") + (sdcid.equals("SDIWorkItem") ? ",workitemid, workitemversionid" : "") + "]");
            }
            if ((primaryRequest = sdiRequest.getRequest("primary")).length() > 0) {
                this.logDebug("PRIMARY: " + primaryRequest);
                boolean isGetAll = false;
                boolean hasUserCols = false;
                boolean extendedDataTypes = false;
                if (primaryRequest.trim().equalsIgnoreCase("primary") || primaryRequest.indexOf("[") < 0 || primaryRequest.indexOf("primary[*,") == 0) {
                    isGetAll = true;
                    primaryRequest = StringUtil.replaceAll(primaryRequest, "primary[*,", "primary[");
                    hasUserCols = true;
                }
                Object[] cols = null;
                ArrayList<String> fklock = new ArrayList<String>(Arrays.asList(StringUtil.split(sdiRequest.getPrimaryFKColumnLock(), ";")));
                if ((!isGetAll || hasUserCols) && (cols = RequestParser.parseColItem(primaryRequest)) != null && cols.length > 0) {
                    this.resetSqlBuffers(extendedSelect, extendedFrom, extendedWhere);
                    for (int col = 0; col < cols.length; ++col) {
                        this.getColumnLinkSql(ddtService, (String[])cols, col, sdcProps, sdcLinks, extendedSelect, extendedFrom);
                    }
                }
                if (sdiRequest.isCountRequest()) {
                    select = new StringBuffer("SELECT count( * ) count ");
                    if (rsetQuery) {
                        select.append("FROM ").append(rsetitemsTable).append(" WHERE rsetid = ?");
                        db.createPreparedResultSet("getSDIData Primary", select.toString(), new Object[]{rsetid});
                    } else {
                        select.append("FROM ").append(listFrom).append(extendedFrom.toString()).append(" ");
                        select.append("WHERE ").append(listWhere).append(" ");
                        db.createPreparedResultSet("getSDIData Primary", select.toString(), listQueryParamList.toArray());
                    }
                } else {
                    select = new StringBuffer("SELECT " + tableid + "." + sdcCols.getPropertyList(keycolid1).getProperty("columnid"));
                    if (sdcProps.getProperty("componentableflag").equals("Y")) {
                        select.append(",").append(tableid).append(".compcode");
                    }
                    if ("SDIWorkItem".equals(sdcid)) {
                        select.append(",sdiworkitem.workitemid, sdiworkitem.workitemversionid");
                    }
                    if (cols != null) {
                        Arrays.sort(cols);
                    }
                    int columnCount = sdcCols.size();
                    for (int col = 0; col < columnCount; ++col) {
                        int colIndex;
                        PropertyList column = sdcCols.getPropertyList(col);
                        String columnid = column.getProperty("columnid");
                        if (keycolid1.equalsIgnoreCase(columnid)) continue;
                        int n = colIndex = cols != null ? Arrays.binarySearch(cols, columnid) : -1;
                        if (colIndex >= 0 && (column.getProperty("datatype").equals("T") || column.getProperty("datatype").equals("B"))) {
                            extendedDataTypes = true;
                        }
                        if (!isGetAll && colIndex < 0 && !column.getProperty("pkflag").equals("Y") && !columnid.equals(sdcProps.getProperty("desccol"))) continue;
                        select.append(", ");
                        select.append(tableid);
                        select.append(".");
                        select.append(columnid);
                        select.append(" ");
                    }
                    if (cols != null) {
                        select.append(this.getUserColSelect((String[])cols, tableid, sdcCols, ddtService, sdcid));
                    }
                    if (sdiRequest.isRetrieveMappedKey() && sdcProps.getProperty("tableidmap").length() > 0) {
                        String keymap1 = sdcProps.getProperty("keymap1");
                        select.append(", ");
                        select.append(tableid);
                        select.append(".");
                        select.append(sdcProps.getProperty("keycolid1"));
                        select.append(" ").append(keymap1).append(" ");
                    }
                    if (rsetQuery) {
                        String orderby;
                        select.append(",").append(RSETITEMS_COLS).append(extendedSelect.toString()).append(" ");
                        if (sdiRequest.isExtendedAudit()) {
                            select.append(",").append(TRACELOGAUDIT_COLS).append(" ");
                        }
                        select.append("FROM\t").append(rsetitemsTable).append(", ").append(tableid).append(extendedFrom.toString()).append(" ");
                        if (sdiRequest.isExtendedAudit()) {
                            select.append("LEFT OUTER JOIN tracelog ON ").append(tableid).append(".tracelogid = tracelog.tracelogid ");
                        }
                        select.append("WHERE\t").append(rsetitemsTable).append(".rsetid = ? AND ");
                        select.append("\t\t").append(rsetitemsTable).append(".sdcid = ? AND ");
                        if ("SDIAttachment".equals(sdcid)) {
                            select.append("\t\t").append(rsetitemsTable).append(".keyid1 = ").append(tableid).append(".").append("sdiattachmentid");
                        } else {
                            select.append("\t\t").append(rsetitemsTable).append(".keyid1 = ").append(tableid).append(".").append(keycolid1);
                            for (int i = 1; i < keycols; ++i) {
                                select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = ").append(tableid).append(".").append(sdcProps.getProperty("keycolid" + String.valueOf(i + 1)));
                            }
                        }
                        select.append(extendedWhere.toString());
                        if ("WorkItem".equals(sdcid) && !Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
                            select.append(" AND ( workitem.workitemid != '_Reflex' ) ");
                        }
                        String string = orderby = sdiRequest.isUseRSetOrderBy() ? "" : sdiRequest.getQueryOrderBy();
                        if (!sdiRequest.isUseRSetOrderBy() && orderby != null && orderby.length() > 0) {
                            String tableidmap = sdcProps.getProperty("tableidmap");
                            if (tableidmap != null && tableidmap.length() > 0) {
                                this.logInfo("^^Original order by:" + orderby);
                                String keymap1 = sdcProps.getProperty("keymap1");
                                orderby = DataAccessService.modifyWhereOrderByClause(orderby, tableidmap, tableid, keymap1, keycolid1);
                                this.logInfo("^^Modified order by:" + orderby);
                            }
                            select.append(" ORDER BY ").append(orderby);
                        } else {
                            select.append(" ORDER BY ").append(rsetitemsTable).append(".rsetseq");
                        }
                        db.createPreparedResultSet("getSDIData Primary", select.toString(), new Object[]{rsetid, sdcid});
                    } else {
                        select.append(",").append(SUBST_RSETITEMS_COLS).append(extendedSelect.toString()).append(" ");
                        select.append("FROM ").append(listFrom).append(extendedFrom.toString()).append(" ");
                        select.append("WHERE ").append(listWhere).append(" ");
                        if (!listFrom.equals(tableid)) {
                            select.append("ORDER BY seq_value");
                        }
                        db.createPreparedResultSet("getSDIData Primary", select.toString(), listQueryParamList.toArray());
                    }
                }
                this.logInfo("Primary Select=" + select + " params:" + listQueryParamList);
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("getSDIData Primary"), extendedDataTypes || sdiRequest.isExtendedDataTypes(), db.getDbms());
                if ("SDIWorkItem".equals(sdcid)) {
                    int originalrowcount = ds.getRowCount();
                    ds = this.filterByWISecurity(ds, ddtService);
                    if (originalrowcount == qualifiedrows && originalrowcount != ds.getRowCount()) {
                        qualifiedrows = ds.getRowCount();
                    }
                }
                if ("WorkItem".equals(sdcid) && qualifiedrows != ds.getRowCount()) {
                    --qualifiedrows;
                }
                if (sdiRequest.isReturnMaskedData()) {
                    DataMaskUtil dataMaskUtil = new DataMaskUtil(this.sapphireConnection);
                    dataMaskUtil.maskPrimaryDataSet(ds, sdiRequest);
                }
                sdiData.setDataset("primary", ds);
                if ((sdiRequest.getPrimaryLockOption() != null && sdiRequest.getPrimaryLockOption().length() > 0 || sdiRequest.getLockOption() != null && sdiRequest.getLockOption().length() > 0) && cols != null && cols.length > 0 && sdiRequest.getPrimaryFKColumnLock() != null && sdiRequest.getPrimaryFKColumnLock().length() > 0 && fklock.size() > 0) {
                    if (fklock.contains("trackitem")) {
                        String lockoption = sdiRequest.getDataLockOption() != null && sdiRequest.getDataLockOption().length() > 0 ? sdiRequest.getDataLockOption() : (sdiRequest.getPrimaryLockOption() != null && sdiRequest.getPrimaryLockOption().length() > 0 ? sdiRequest.getPrimaryLockOption() : sdiRequest.getLockOption());
                        try {
                            RSet tirset;
                            StringBuffer tikeys = new StringBuffer();
                            for (int i = 0; i < ds.getRowCount(); ++i) {
                                if (ds.getBigDecimal(i, "__ticount", new BigDecimal(0)).intValue() != 1) continue;
                                if (tikeys.length() > 0) {
                                    tikeys.append(";");
                                }
                                tikeys.append(ds.getValue(i, sdcProps.getProperty("keycolid1"), ""));
                            }
                            if (tikeys.length() > 0 && (tirset = das.createRSetAlt("TrackItemSDC", "linkkeyid1", tikeys.toString(), sdiRequest.isShowHiddenRecords())) != null) {
                                tirset.setRSet(das.lockRSet(tirset, lockoption, 1, sdiRequest.getAutoLockTimeout()));
                                if (tirset.getPrimaryStatus() == 1) {
                                    fkrsetid.put("trackitem", tirset.getRsetid());
                                } else {
                                    this.logger.warn("Could not create FK lock.");
                                }
                            }
                        }
                        catch (ServiceException se) {
                            this.logger.error("Failed to create FK lock.", se);
                        }
                    }
                    if (fklock.contains("sdialias")) {
                        // empty if block
                    }
                    for (int link = 0; link < (sdcLinks != null ? sdcLinks.size() : 0); ++link) {
                        PropertyList linkProps = sdcLinks.getPropertyList(link);
                        String linkcolid = linkProps.getProperty("sdccolumnid");
                        if (!fklock.contains(linkcolid) || fkrsetid.containsKey(linkcolid)) continue;
                        String fkkey1 = ds.getColumnValues(linkcolid, ";");
                        String fkkey2 = "";
                        if (linkProps.getProperty("sdccolumnid2").length() > 0) {
                            fkkey2 = ds.getColumnValues(linkProps.getProperty("sdccolumnid2"), ";");
                        }
                        if (fkkey1.length() <= 0) continue;
                        String lockoption = sdiRequest.getDataLockOption() != null && sdiRequest.getDataLockOption().length() > 0 ? sdiRequest.getDataLockOption() : (sdiRequest.getPrimaryLockOption() != null && sdiRequest.getPrimaryLockOption().length() > 0 ? sdiRequest.getPrimaryLockOption() : sdiRequest.getLockOption());
                        try {
                            boolean fkchecked;
                            RSet fkrset = das.createRSet(linkProps.getProperty("linksdcid"), fkkey1, fkkey2.length() > 0 ? fkkey2 : "", "", sdiRequest.isShowHiddenRecords());
                            if (fkrset == null || !(fkchecked = true)) continue;
                            fkrset.setRSet(das.lockRSet(fkrset, lockoption, 1, sdiRequest.getAutoLockTimeout()));
                            if (fkrset.getPrimaryStatus() == 1) {
                                fkrsetid.put(linkcolid, fkrset.getRsetid());
                                continue;
                            }
                            this.logger.warn("Could not create FK lock.");
                            continue;
                        }
                        catch (ServiceException se) {
                            this.logger.error("Failed to create FK lock.", se);
                        }
                    }
                    if (!fklock.contains("trackitem") || !fkrsetid.containsKey("trackitem")) {
                        // empty if block
                    }
                }
            }
            int links = sdcLinks.size();
            for (int link = 0; link < links; ++link) {
                PropertyList linkProps = sdcLinks.getPropertyList(link);
                String linktableid = linkProps.getProperty("linktableid");
                if (!linkProps.getProperty("linktype").equals("D") && !linkProps.getProperty("linktype").equals("M") || !linkProps.getProperty("loadflag").equals("Y") && !sdiRequest.isOverrideLoadFlag() || linktableid.length() <= 0 || !sdiRequest.isRequestItem(linktableid.toLowerCase())) continue;
                this.logInfo("Getting Detail data for " + linktableid);
                if (linktableid.equalsIgnoreCase("syscolumn")) {
                    select = new StringBuffer("SELECT syscolumn.columnsequence, syscolumn.*," + RSETITEMS_COLS + "FROM syscolumn, sdclink, " + rsetitemsTable + " WHERE\tsyscolumn.tableid = sdclink.linktableid AND        sdclink.linktype IN ( 'D', 'M' ) AND        " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = sdclink.sdcid UNION SELECT syscolumn.columnsequence, syscolumn.*," + RSETITEMS_COLS + "FROM syscolumn, sdcdetaillink, " + rsetitemsTable + " WHERE\tsyscolumn.tableid = sdcdetaillink.linktableid AND        sdcdetaillink.linktype IN ( 'D', 'M' ) AND        " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = sdcdetaillink.sdcid UNION SELECT syscolumn.columnsequence, syscolumn.*," + RSETITEMS_COLS + "FROM syscolumn, sdc, " + rsetitemsTable + " WHERE\tsyscolumn.tableid = sdc.tableid AND        " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = sdc.sdcid ORDER BY 1");
                } else if (linktableid.equalsIgnoreCase("syscolumnproperty")) {
                    select = new StringBuffer("SELECT syscolumnproperty.*," + RSETITEMS_COLS + "FROM syscolumnproperty, sdc, " + rsetitemsTable + " WHERE\tsyscolumnproperty.tableid = sdc.tableid AND        " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = sdc.sdcid ORDER BY syscolumnproperty.propertyid");
                } else if (linktableid.equalsIgnoreCase("systable")) {
                    select = new StringBuffer("");
                } else if (linktableid.equalsIgnoreCase("sdclink")) {
                    select = new StringBuffer("SELECT sdclink.linksequence, sdclink.sdcid, sdclink.linkid, 'Primary' \"detaillinkid\", sdclink.linktype, sdclink.linktableid, sdc.tableid \"parenttableid\", sdclink.linksdcid, sdclink.reftypeid, sdclink.sdccolumnid, sdclink.sdccolumnid2, sdclink.sdccolumnid3, sdclink.loadflag, sdclink.userflag, sdclink.deleteflag, sdclink.compcode, sdc.tableid, " + RSETITEMS_COLS + "FROM   sdclink, " + rsetitemsTable + ", sdc WHERE  " + rsetitemsTable + ".rsetid = ? AND        " + rsetitemsTable + ".sdcid = ? AND        " + rsetitemsTable + ".keyid1 = sdclink.sdcid AND        sdclink.linktype <> 'D' AND        sdclink.sdcid = sdc.sdcid UNION SELECT sdcdetaillink.linksequence, sdcdetaillink.sdcid, sdcdetaillink.linkid, sdcdetaillink.detaillinkid, sdcdetaillink.linktype, sdcdetaillink.linktableid, sdcdetaillink.parenttableid, sdcdetaillink.linksdcid, sdcdetaillink.reftypeid, sdcdetaillink.sdccolumnid, sdcdetaillink.sdccolumnid2, sdcdetaillink.sdccolumnid3, 'N' \"loadflag\", sdcdetaillink.userflag, sdcdetaillink.deleteflag, sdcdetaillink.compcode, sdcdetaillink.linktableid \"tableid\", " + RSETITEMS_COLS + "FROM   sdcdetaillink, " + rsetitemsTable + ", sdclink WHERE  " + rsetitemsTable + ".rsetid = ? AND        " + rsetitemsTable + ".sdcid = ? AND       " + rsetitemsTable + ".keyid1 = sdcdetaillink.sdcid AND        sdcdetaillink.linktype <> 'D' AND        sdcdetaillink.sdcid = sdclink.sdcid AND        sdcdetaillink.linkid = sdclink.linkid ORDER BY 12, 1");
                } else {
                    String[] cols;
                    StringBuffer linkcolsquery = new StringBuffer(linktableid + ".* ");
                    String linkRequest = sdiRequest.getRequest(linktableid);
                    if (linkRequest != null && linkRequest.length() > 0 && linkRequest.toLowerCase().indexOf("select") > -1 && (cols = RequestParser.parseColItem(linkRequest)) != null && cols.length > 0) {
                        for (int col = 0; col < cols.length; ++col) {
                            String ncol = cols[col].trim();
                            if (!ncol.startsWith("(") || ncol.indexOf(")") <= -1 || ncol.toLowerCase().indexOf("select") <= -1) continue;
                            linkcolsquery.append(",").append(ncol);
                        }
                    }
                    if (rsetQuery) {
                        linkcolsquery.append(",").append(RSETITEMS_COLS).append(" ");
                        select = new StringBuffer("SELECT " + linkcolsquery.toString() + "FROM " + linktableid + ", " + rsetitemsTable + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = " + linktableid + "." + keycolid1);
                        for (int i = 1; i < keycols; ++i) {
                            select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = ").append(linktableid).append(".").append(sdcProps.getProperty("keycolid" + String.valueOf(i + 1)));
                        }
                        select.append(" ORDER BY ").append(rsetitemsTable).append(".rsetseq ").append(linkProps.getProperty("hasusersequence").equals("Y") ? ", " + linktableid + ".usersequence " : "");
                    } else {
                        linkcolsquery.append(",").append(SUBST_RSETITEMS_COLS).append(" ");
                        select = new StringBuffer("SELECT " + linkcolsquery.toString() + "FROM " + linktableid + ", " + listFrom + " WHERE " + linktableid + "." + keycolid1 + "=" + tableid + "." + keycolid1 + (keycols >= 2 ? " AND " + linktableid + "." + keycolid2 + "=" + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND " + linktableid + "." + keycolid3 + "=" + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")" + (listItems == 1 && linkProps.getProperty("hasusersequence").equals("Y") ? " ORDER BY " + linktableid + ".usersequence " : ""));
                    }
                }
                if (select.length() <= 0) continue;
                if (rsetQuery) {
                    if (linktableid.equalsIgnoreCase("syscolumn")) {
                        db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), new Object[]{rsetid, sdcid, rsetid, sdcid, rsetid, sdcid});
                    } else if (linktableid.equalsIgnoreCase("sdclink")) {
                        db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), new Object[]{rsetid, sdcid, rsetid, sdcid});
                    } else {
                        db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), new Object[]{rsetid, sdcid});
                    }
                } else {
                    db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), listQueryParamList.toArray());
                }
                this.logInfo("Detail Select=" + select);
                DataSet data = new DataSet();
                data.setResultSet(db.getResultSet("getSDIData Link " + linktableid), sdiRequest.isExtendedDataTypes() || linktableid.equalsIgnoreCase("sdcexport") || linktableid.equalsIgnoreCase("webpagepropertytree"), db.getDbms());
                sdiData.setDataset(linktableid, data);
            }
            int detaillinks = detailLinks.size();
            for (int link = 0; link < detaillinks; ++link) {
                PropertyList linkProps = detailLinks.getPropertyList(link);
                String linktableid = linkProps.getProperty("linktableid");
                if (!linkProps.getProperty("linktype").equals("D") || !sdiRequest.isRequestItem(linktableid.toLowerCase())) continue;
                this.logInfo("Getting Detail data for " + linktableid);
                if (rsetQuery) {
                    select = new StringBuffer("SELECT " + linktableid + ".*," + RSETITEMS_COLS + "FROM " + linktableid + ", " + rsetitemsTable + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = " + linktableid + "." + keycolid1);
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = ").append(linktableid).append(".").append(sdcProps.getProperty("keycolid" + String.valueOf(i + 1)));
                    }
                    select.append(" ORDER BY ").append(rsetitemsTable).append(".rsetseq ").append(linkProps.getProperty("hasusersequence").equals("Y") ? ", " + linktableid + ".usersequence " : "");
                } else {
                    select = new StringBuffer("SELECT " + linktableid + ".*," + SUBST_RSETITEMS_COLS + "FROM " + linktableid + ", " + listFrom + " WHERE " + linktableid + "." + keycolid1 + "=" + tableid + "." + keycolid1 + (keycols >= 2 ? " AND " + linktableid + "." + keycolid2 + "=" + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND " + linktableid + "." + keycolid3 + "=" + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                }
                if (select.length() <= 0) continue;
                if (rsetQuery) {
                    db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    db.createPreparedResultSet("getSDIData Link " + linktableid, select.toString(), listQueryParamList.toArray());
                }
                this.logInfo("Detail Select=" + select);
                DataSet data = new DataSet();
                data.setResultSet(db.getResultSet("getSDIData Link " + linktableid), linktableid.equalsIgnoreCase("sdcexport") || linktableid.equalsIgnoreCase("webpagepropertytree"), db.getDbms());
                sdiData.setDataset(linktableid, data);
            }
            ArrayList<String> detaillistQueryParamList = null;
            Object[] detailBindVars = null;
            if (listFromParamList != null) {
                detaillistQueryParamList = new ArrayList<String>();
                detaillistQueryParamList.addAll(listFromParamList);
                detaillistQueryParamList.add(sdcid);
                detaillistQueryParamList.addAll(listWhereParamList);
                detailBindVars = detaillistQueryParamList.toArray();
            }
            if (sdiRequest.isRequestItem("attachment")) {
                this.logInfo("Getting attachment data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattachment.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattachment WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiattachment.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiattachment.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiattachment.keyid").append(String.valueOf(i + 1));
                    }
                    select.append(" ORDER BY sdiattachment.usersequence, sdiattachment.attachmentnum ");
                    db.createPreparedResultSet("getSDIData Attachment", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattachment.*," + SUBST_RSETITEMS_COLS + "FROM sdiattachment, " + listFrom + " WHERE sdiattachment.sdcid = ? AND sdiattachment.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiattachment.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiattachment.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    select.append(" ORDER BY sdiattachment.usersequence, sdiattachment.attachmentnum ");
                    db.createPreparedResultSet("getSDIData Attachment", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved attachment data");
                this.logInfo("Attachment Select=" + select);
                DataSet attachments = new DataSet();
                attachments.setResultSet(db.getResultSet("getSDIData Attachment"), true, db.getDbms());
                sdiData.setDataset("attachment", attachments);
            }
            if (sdiRequest.isRequestItem("notes")) {
                String notesRequest = sdiRequest.getRequest("notes");
                String selectColumn = "";
                if (notesRequest.indexOf("[") > 0) {
                    String[] cols = RequestParser.parseColItem(notesRequest);
                    for (int c = 0; c < cols.length; ++c) {
                        selectColumn = selectColumn + (c > 0 ? "," : "") + "sdinote." + cols[c];
                    }
                } else {
                    selectColumn = "sdinote.*";
                }
                this.logInfo("Getting notes data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT " + selectColumn + ", " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdinote WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdinote.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdinote.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdinote.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData Notes", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT " + selectColumn + "," + SUBST_RSETITEMS_COLS + "FROM sdinote, " + listFrom + " WHERE sdinote.sdcid = ? AND sdinote.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdinote.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdinote.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Notes", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved notes data");
                this.logInfo("Notes Select=" + select);
                DataSet notes = new DataSet();
                notes.setResultSet(db.getResultSet("getSDIData Notes"), true, db.getDbms());
                sdiData.setDataset("notes", notes);
            }
            String datasetRequest = sdiRequest.getRequest("dataset");
            if (rsetQuery && datasetRequest.length() > 0) {
                this.logInfo("DATASET: " + datasetRequest);
                this.resetSqlBuffers(extendedSelect, extendedFrom, extendedWhere);
                String[] cols = RequestParser.parseColItem(datasetRequest);
                if (cols != null && cols.length > 0) {
                    PropertyListCollection datasetLinks = ddtService.getSDCProperties("DataSet").getCollection("links");
                    this.resetSqlBuffers(extendedSelect, extendedFrom, extendedWhere);
                    for (int col = 0; col < cols.length; ++col) {
                        if (this.getColumnKeyidJoin(cols[col], keycolid1, keycolid2, keycolid3, "sdidata", tableid, extendedSelect, extendedFrom) || this.getColumnFkJoin(cols[col], "paramlistid", "paramlistversionid", "variantid", "sdidata", "paramlist", extendedSelect, extendedFrom)) continue;
                        this.getColumnLinkSql(ddtService, cols, col, this.getSDCProcessor().getPropertyList("DataSet"), datasetLinks, extendedSelect, extendedFrom);
                    }
                    extendedSelect.append(this.getUserColSelect(cols, "sdidata", null, ddtService, "DataSet"));
                }
                this.logInfo("Getting dataset data");
                select = new StringBuffer("SELECT sdidata.*, " + RSETITEMSDS_COLS + extendedSelect.toString() + " FROM\trsetitemsds, sdidata " + extendedFrom.toString() + " WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.paramlistid = sdidata.paramlistid " + (this.connectionInfo.isOracle() && oraVersion <= 9 ? "|| ''" : "") + " AND \t\trsetitemsds.paramlistversionid = sdidata.paramlistversionid AND \t\trsetitemsds.variantid = sdidata.variantid AND \t\trsetitemsds.dataset = sdidata.dataset AND \t\trsetitemsds.sdcid = sdidata.sdcid AND \t\trsetitemsds.keyid1 = sdidata.keyid1 AND \t\trsetitemsds.keyid2 = sdidata.keyid2 AND \t\trsetitemsds.keyid3 = sdidata.keyid3 ");
                select.append(extendedWhere.toString());
                db.createPreparedResultSet("getSDIData DataSet", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATASET data");
                this.logInfo("Dataset Select=" + select);
                sdiData.setDataset("dataset", new DataSet(db.getResultSet("getSDIData DataSet")));
            }
            String dataitemRequest = sdiRequest.getRequest("dataitem");
            if (rsetQuery && dataitemRequest.length() > 0) {
                this.logInfo("DATAITEM: " + dataitemRequest);
                this.resetSqlBuffers(extendedSelect, extendedFrom, extendedWhere);
                String[] cols = RequestParser.parseColItem(dataitemRequest);
                if (cols != null && cols.length > 0) {
                    PropertyListCollection dataitemLinks = ddtService.getSDCProperties("DataItem").getCollection("links");
                    for (int col = 0; col < cols.length; ++col) {
                        if (cols[col].indexOf("paramlistitem.") == 0) {
                            boolean hasnoalias = RequestParser.parseAlias(cols[col]).equals(cols[col]);
                            if (hasnoalias) {
                                extendedSelect.append(", ").append(cols[col]).append(" \"").append(cols[col]).append("\"");
                            } else {
                                extendedSelect.append(", ").append(cols[col]);
                            }
                            if (extendedFrom.indexOf(" LEFT OUTER JOIN paramlistitem ") != -1) continue;
                            extendedFrom.append(" LEFT OUTER JOIN paramlistitem ON paramlistitem.paramlistid = sdidataitem.paramlistid AND paramlistitem.paramlistversionid=sdidataitem.paramlistversionid AND paramlistitem.variantid=sdidataitem.variantid AND paramlistitem.paramid=sdidataitem.paramid AND paramlistitem.paramtype=sdidataitem.paramtype ");
                            continue;
                        }
                        if (this.getColumnKeyidJoin(cols[col], keycolid1, keycolid2, keycolid3, "sdidataitem", tableid, extendedSelect, extendedFrom) || this.getColumnFkJoin(cols[col], "paramlistid", "paramlistversionid", "variantid", "sdidataitem", "paramlist", extendedSelect, extendedFrom) || this.getColumnFkJoin(cols[col], "paramid", "", "", "sdidataitem", "param", extendedSelect, extendedFrom)) continue;
                        this.getColumnLinkSql(ddtService, cols, col, this.getSDCProcessor().getPropertyList("DataItem"), dataitemLinks, extendedSelect, extendedFrom);
                    }
                }
                extendedSelect.append(this.getUserColSelect(cols, "sdidataitem", null, ddtService, "DataItem"));
                this.logInfo("Getting dataitem data");
                select = new StringBuffer("SELECT sdidataitem.*, sdidata.usersequence \"__sdidata_usersequence\", sdidata.s_qcbatchitemid \"__sdidata_s_qcbatchitemid\", " + RSETITEMSDS_COLS + extendedSelect.toString() + " FROM\trsetitemsds, sdidata, sdidataitem" + extendedFrom.toString() + " WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.sdcid = sdidata.sdcid AND \t\trsetitemsds.keyid1 = sdidata.keyid1 AND \t\trsetitemsds.keyid2 = sdidata.keyid2 AND \t\trsetitemsds.keyid3 = sdidata.keyid3 AND        rsetitemsds.paramlistid = sdidata.paramlistid " + (this.connectionInfo.isOracle() && oraVersion <= 9 ? "|| ''" : "") + " AND        rsetitemsds.paramlistversionid = sdidata.paramlistversionid AND        rsetitemsds.variantid = sdidata.variantid AND        rsetitemsds.dataset = sdidata.dataset AND        sdidataitem.sdcid = sdidata.sdcid AND        sdidataitem.keyid1 = sdidata.keyid1 AND        sdidataitem.keyid2 = sdidata.keyid2 AND        sdidataitem.keyid3 = sdidata.keyid3 AND        sdidataitem.paramlistid = sdidata.paramlistid AND        sdidataitem.paramlistversionid = sdidata.paramlistversionid AND        sdidataitem.variantid = sdidata.variantid AND        sdidataitem.dataset = sdidata.dataset ");
                select.append(extendedWhere.toString());
                db.createPreparedResultSet("getSDIData DataItem", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATAITEM data using: " + select);
                DataSet dataitemDS = new DataSet(db.getResultSet("getSDIData DataItem"));
                if (sdiRequest.isReturnMaskedData()) {
                    DataMaskUtil dataMaskUtil = new DataMaskUtil(this.sapphireConnection);
                    dataMaskUtil.maskSDIDataItemDataSet(dataitemDS, dataitemRequest, sdiRequest.isShowHiddenRecords());
                }
                sdiData.setDataset("dataitem", dataitemDS);
            }
            if (rsetQuery && sdiRequest.isRequestItem("datalimit")) {
                this.logInfo("Getting datalimit data");
                select = new StringBuffer("SELECT sdidataitemlimits.*, " + RSETITEMSDS_COLS + "FROM\trsetitemsds, sdidataitemlimits WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.paramlistid = sdidataitemlimits.paramlistid AND \t\trsetitemsds.paramlistversionid = sdidataitemlimits.paramlistversionid AND \t\trsetitemsds.variantid = sdidataitemlimits.variantid AND \t\trsetitemsds.dataset = sdidataitemlimits.dataset AND \t\trsetitemsds.sdcid = sdidataitemlimits.sdcid AND \t\trsetitemsds.keyid1 = sdidataitemlimits.keyid1 AND \t\trsetitemsds.keyid2 = sdidataitemlimits.keyid2 AND \t\trsetitemsds.keyid3 = sdidataitemlimits.keyid3 ");
                db.createPreparedResultSet("getSDIData DataItemLimit", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATAITEMLIMIT data");
                this.logInfo("Dataitemlimit Select=" + select);
                sdiData.setDataset("datalimit", new DataSet(db.getResultSet("getSDIData DataItemLimit")));
            }
            if (rsetQuery && sdiRequest.isRequestItem("dataapproval")) {
                this.logInfo("Getting dataapproval data");
                select = new StringBuffer("SELECT sdidataapproval.*, " + RSETITEMSDS_COLS + "FROM\trsetitemsds, sdidataapproval WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.paramlistid = sdidataapproval.paramlistid AND \t\trsetitemsds.paramlistversionid = sdidataapproval.paramlistversionid AND \t\trsetitemsds.variantid = sdidataapproval.variantid AND \t\trsetitemsds.dataset = sdidataapproval.dataset AND \t\trsetitemsds.sdcid = sdidataapproval.sdcid AND \t\trsetitemsds.keyid1 = sdidataapproval.keyid1 AND \t\trsetitemsds.keyid2 = sdidataapproval.keyid2 AND \t\trsetitemsds.keyid3 = sdidataapproval.keyid3 ");
                db.createPreparedResultSet("getSDIData DataApproval", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATAAPPROVAL data");
                this.logInfo("Dataapproval Select=" + select);
                sdiData.setDataset("dataapproval", new DataSet(db.getResultSet("getSDIData DataApproval")));
            }
            if (rsetQuery && sdiRequest.isRequestItem("reagentrelation")) {
                this.logInfo("Getting datarelation data");
                select = new StringBuffer("SELECT        sdidatarelation.sdcid, sdidatarelation.keyid1, sdidatarelation.keyid2, sdidatarelation.keyid3,        sdidatarelation.paramlistid, sdidatarelation.paramlistversionid, sdidatarelation.variantid, sdidatarelation.dataset, sdidatarelation.relationid,        sdidatarelation.relationtype \"reagenttypeid\", sdidatarelation.tokeyid1 \"reagentlotid\", sdidatarelation.refkeyid1 \"containerid\",        sdidatarelation.amount \"amountused\", sdidatarelation.amountunits \"amountusedunits\", sdidatarelation.amountunitstype \"amountusedunitstype\",        case when sdidatarelation.amountunitstype = 'C' then '(Containers)' else sdidatarelation.amountunits end \"unitsused\",        paramlistreagenttype.amount \"recommendedamount\", paramlistreagenttype.amountunits \"recommendedamountunits\", paramlistreagenttype.amountunitstype \"recommendedamountunitstype\",        case when paramlistreagenttype.amountunitstype = 'C' then '(Containers)' else paramlistreagenttype.amountunits end \"recommendedunits\",        paramlistreagenttype.reagenttypeversionid \"reagenttypeversionid\",        sdidata.usersequence \"__sdidata_usersequence\", " + RSETITEMSDS_COLS + " FROM\trsetitemsds, sdidata, paramlistreagenttype, sdidatarelation WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.sdcid = sdidata.sdcid AND \t\trsetitemsds.keyid1 = sdidata.keyid1 AND \t\trsetitemsds.keyid2 = sdidata.keyid2 AND \t\trsetitemsds.keyid3 = sdidata.keyid3 AND        rsetitemsds.paramlistid = sdidata.paramlistid " + (this.connectionInfo.isOracle() && oraVersion <= 9 ? "|| ''" : "") + " AND        rsetitemsds.paramlistversionid = sdidata.paramlistversionid AND        rsetitemsds.variantid = sdidata.variantid AND        rsetitemsds.dataset = sdidata.dataset AND        sdidatarelation.sdcid = sdidata.sdcid AND        sdidatarelation.keyid1 = sdidata.keyid1 AND        sdidatarelation.keyid2 = sdidata.keyid2 AND        sdidatarelation.keyid3 = sdidata.keyid3 AND        sdidatarelation.paramlistid = sdidata.paramlistid AND        sdidatarelation.paramlistversionid = sdidata.paramlistversionid AND        sdidatarelation.variantid = sdidata.variantid AND        sdidatarelation.dataset = sdidata.dataset AND        sdidatarelation.relationfunction = 'Reagent' AND        sdidatarelation.paramlistid = paramlistreagenttype.paramlistid AND        sdidatarelation.paramlistversionid = paramlistreagenttype.paramlistversionid AND        sdidatarelation.variantid = paramlistreagenttype.variantid AND        sdidatarelation.relationtype = paramlistreagenttype.reagenttypeid ");
                db.createPreparedResultSet("getSDIData ReagentRelation", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved REAGENTRELATION data");
                this.logInfo("Datarelation Select=" + select);
                sdiData.setDataset("reagentrelation", new DataSet(db.getResultSet("getSDIData ReagentRelation")));
            }
            if (rsetQuery && sdiRequest.isRequestItem("datarelation")) {
                this.logInfo("Getting datarelation data");
                select = new StringBuffer("SELECT sdidatarelation.*, sdidata.usersequence \"__sdidata_usersequence\", " + RSETITEMSDS_COLS + " FROM\trsetitemsds, sdidata, sdidatarelation WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.sdcid = sdidata.sdcid AND \t\trsetitemsds.keyid1 = sdidata.keyid1 AND \t\trsetitemsds.keyid2 = sdidata.keyid2 AND \t\trsetitemsds.keyid3 = sdidata.keyid3 AND        rsetitemsds.paramlistid = sdidata.paramlistid " + (this.connectionInfo.isOracle() && oraVersion <= 9 ? "|| ''" : "") + " AND        rsetitemsds.paramlistversionid = sdidata.paramlistversionid AND        rsetitemsds.variantid = sdidata.variantid AND        rsetitemsds.dataset = sdidata.dataset AND        sdidatarelation.sdcid = sdidata.sdcid AND        sdidatarelation.keyid1 = sdidata.keyid1 AND        sdidatarelation.keyid2 = sdidata.keyid2 AND        sdidatarelation.keyid3 = sdidata.keyid3 AND        sdidatarelation.paramlistid = sdidata.paramlistid AND        sdidatarelation.paramlistversionid = sdidata.paramlistversionid AND        sdidatarelation.variantid = sdidata.variantid AND        sdidatarelation.dataset = sdidata.dataset ");
                db.createPreparedResultSet("getSDIData DataRelation", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATARELATION data");
                this.logInfo("Datarelation Select=" + select);
                sdiData.setDataset("datarelation", new DataSet(db.getResultSet("getSDIData DataRelation")));
            }
            if (rsetQuery && sdiRequest.isRequestItem("dataspec")) {
                String extendedselect = "";
                if (sdiRequest.getRequest("dataspec").indexOf("[") > 0) {
                    String[] cols = RequestParser.parseColItem(sdiRequest.getRequest("dataspec"));
                    for (int i = 0; i < cols.length; ++i) {
                        extendedselect = extendedselect + (extendedselect.length() > 0 ? "," : "") + cols[i];
                    }
                }
                this.logInfo("Getting dataitemspec data");
                select = new StringBuffer("SELECT sdidataitemspec.*, " + (extendedselect.length() > 0 ? extendedselect + ", " : "") + RSETITEMSDS_COLS + "FROM\trsetitemsds, sdidataitemspec, sdispec WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.paramlistid = sdidataitemspec.paramlistid AND \t\trsetitemsds.paramlistversionid = sdidataitemspec.paramlistversionid AND \t\trsetitemsds.variantid = sdidataitemspec.variantid AND \t\trsetitemsds.dataset = sdidataitemspec.dataset AND \t\trsetitemsds.sdcid = sdidataitemspec.sdcid AND \t\trsetitemsds.keyid1 = sdidataitemspec.keyid1 AND \t\trsetitemsds.keyid2 = sdidataitemspec.keyid2 AND \t\trsetitemsds.keyid3 = sdidataitemspec.keyid3 AND \t\tsdidataitemspec.sdcid = sdispec.sdcid AND \t\tsdidataitemspec.keyid1 = sdispec.keyid1 AND \t\tsdidataitemspec.keyid2 = sdispec.keyid2 AND \t\tsdidataitemspec.keyid3 = sdispec.keyid3 AND \t\tsdidataitemspec.specid = sdispec.specid AND \t\tsdidataitemspec.specversionid = sdispec.specversionid order by sdispec.usersequence");
                db.createPreparedResultSet("getSDIData DataItemSpec", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved DATAITEMSPEC data");
                this.logInfo("Dataitemspec Select=" + select);
                sdiData.setDataset("dataspec", new DataSet(db.getResultSet("getSDIData DataItemSpec")));
            }
            if ((specRequest = sdiRequest.getRequest("sdispec")).length() > 0) {
                this.getExtendedRequest(specRequest, "specid", "specversionid", "", "sdispec", "spec", tableid, keycolid1, keycolid2, keycolid3, extendedSelect, extendedFrom, extendedWhere);
                this.logInfo("Getting specification data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdispec.*, " + RSETITEMS_COLS + extendedSelect.toString() + " FROM\t" + rsetitemsTable + ", sdispec " + extendedFrom.toString() + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdispec.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdispec.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid");
                        select.append(String.valueOf(i + 1));
                        select.append(" = sdispec.keyid");
                        select.append(String.valueOf(i + 1));
                    }
                    select.append(extendedWhere.toString());
                    db.createPreparedResultSet("getSDIData Specification", select.toString(), new Object[]{rsetid});
                } else {
                    select = new StringBuffer("SELECT sdispec.*," + SUBST_RSETITEMS_COLS + "FROM sdispec, " + listFrom + " WHERE sdispec.sdcid = ? AND sdispec.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdispec.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdispec.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Specification", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SPECIFICATION data");
                this.logInfo("Specification Select=" + select);
                sdiData.setDataset("sdispec", new DataSet(db.getResultSet("getSDIData Specification")));
            }
            if (sdiRequest.isRequestItem("sdispecrule")) {
                this.logInfo("Getting specification rule data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdispecrule.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdispecrule WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdispecrule.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdispecrule.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdispecrule.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SpecRules", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdispecrule.*," + SUBST_RSETITEMS_COLS + "FROM sdispecrule, " + listFrom + " WHERE sdispecrule.sdcid = ? AND sdispecrule.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdispecrule.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdispecrule.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SpecRules", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SPECIFICATION RULE data");
                this.logInfo("Specification rule Select=" + select);
                sdiData.setDataset("sdispecrule", new DataSet(db.getResultSet("getSDIData SpecRules")));
            }
            if (sdiRequest.isRequestItem("approval")) {
                this.logInfo("Getting sdiapproval data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiapproval.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiapproval WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiapproval.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiapproval.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiapproval.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIApprovals", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiapproval.*," + SUBST_RSETITEMS_COLS + "FROM sdiapproval, " + listFrom + " WHERE sdiapproval.sdcid = ? AND sdiapproval.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiapproval.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiapproval.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIApprovals", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIApproval data");
                this.logInfo("SDIApproval Select=" + select);
                sdiData.setDataset("approval", new DataSet(db.getResultSet("getSDIData SDIApprovals")));
            }
            if (sdiRequest.isRequestItem("approvalstep")) {
                this.logInfo("Getting sdiapprovalstep data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiapprovalstep.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiapprovalstep WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiapprovalstep.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiapprovalstep.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiapprovalstep.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIApprovalSteps", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiapprovalstep.*," + SUBST_RSETITEMS_COLS + "FROM sdiapprovalstep, " + listFrom + " WHERE sdiapprovalstep.sdcid = ? AND sdiapprovalstep.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiapprovalstep.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiapprovalstep.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIApprovalSteps", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIApprovalStep data");
                this.logInfo("SDIApprovalStep Select=" + select);
                sdiData.setDataset("approvalstep", new DataSet(db.getResultSet("getSDIData SDIApprovalSteps")));
            }
            if ((request = sdiRequest.getRequest("address")).length() > 0) {
                this.logInfo("Getting address data");
                if (rsetQuery) {
                    if (request.indexOf(91) > 0) {
                        this.getExtendedRequest(request, "addressid", "addresstype", "", "sdiaddress", "address", tableid, keycolid1, keycolid2, keycolid3, extendedSelect, extendedFrom, extendedWhere);
                        select = new StringBuffer("SELECT sdiaddress.*, " + RSETITEMS_COLS + extendedSelect.toString() + " FROM\t" + rsetitemsTable + ", sdiaddress " + extendedFrom.toString() + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiaddress.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiaddress.keyid1 ");
                        for (int i = 1; i < keycols; ++i) {
                            select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiaddress.keyid").append(String.valueOf(i + 1));
                        }
                        select.append(extendedWhere.toString());
                    } else {
                        boolean extended = request.indexOf(43) > -1;
                        select = new StringBuffer("SELECT sdiaddress.*, " + RSETITEMS_COLS + (extended ? ", address.* " : "") + "FROM\t" + rsetitemsTable + ", sdiaddress " + (extended ? ", address " : "") + "WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiaddress.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiaddress.keyid1 ");
                        for (int i = 1; i < keycols; ++i) {
                            select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiaddress.keyid").append(String.valueOf(i + 1));
                        }
                        if (extended) {
                            select.append("AND \t\taddress.addressid = sdiaddress.addressid AND \t\taddress.addresstype = sdiaddress.addresstype ");
                        }
                    }
                    db.createPreparedResultSet("getSDIData Address", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    if (request.indexOf(91) > 0) {
                        this.getExtendedRequest(request, "addressid", "addresstype", "", "sdiaddress", "address", tableid, keycolid1, keycolid2, keycolid3, extendedSelect, extendedFrom, extendedWhere);
                        select = new StringBuffer("SELECT sdiaddress.*," + SUBST_RSETITEMS_COLS + extendedSelect.toString() + " FROM  " + listFrom + " ,sdiaddress " + extendedFrom.toString() + " WHERE sdiaddress.sdcid = ? AND sdiaddress.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiaddress.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiaddress.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ") ");
                        select.append(extendedWhere.toString());
                    } else {
                        boolean extended = request.indexOf(43) > -1;
                        select = new StringBuffer("SELECT sdiaddress.*," + SUBST_RSETITEMS_COLS + (extended ? ", address.* " : " ") + "FROM sdiaddress, " + (extended ? " address, " : "") + listFrom + " WHERE sdiaddress.sdcid = ? AND sdiaddress.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiaddress.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiaddress.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                        if (extended) {
                            select.append(" AND \t\taddress.addressid = sdiaddress.addressid AND \t\taddress.addresstype = sdiaddress.addresstype ");
                        }
                    }
                    db.createPreparedResultSet("getSDIData Address", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved ADDRESS data");
                this.logInfo("Address Select=" + select);
                sdiData.setDataset("address", new DataSet(db.getResultSet("getSDIData Address")));
            }
            if (sdiRequest.isRequestItem("calendar")) {
                this.logInfo("Getting sdicalendar data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdicalendar.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdicalendar WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdicalendar.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdicalendar.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdicalendar.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDICalendar", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdicalendar.*," + SUBST_RSETITEMS_COLS + "FROM sdicalendar, " + listFrom + " WHERE sdicalendar.sdcid = ? AND sdicalendar.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdicalendar.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdicalendar.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDICalendar", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDICalendar data");
                this.logInfo("SDICalendar Select=" + select);
                sdiData.setDataset("calendar", new DataSet(db.getResultSet("getSDIData SDICalendar")));
            }
            if (sdiRequest.isRequestItem("sdiresourcerequirement")) {
                this.logInfo("Getting sdiresourcerequirement data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiresourcerequirement.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiresourcerequirement WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiresourcerequirement.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiresourcerequirement.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiresourcerequirement.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIResourceRequirement", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiresourcerequirement.*," + SUBST_RSETITEMS_COLS + "FROM sdiresourcerequirement, " + listFrom + " WHERE sdiresourcerequirement.sdcid = ? AND sdiresourcerequirement.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiresourcerequirement.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiresourcerequirement.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIResourceRequirement", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIResourceRequirement data");
                this.logInfo("SDIResourceRequirement Select=" + select);
                sdiData.setDataset("sdiresourcerequirement", new DataSet(db.getResultSet("getSDIData SDIResourceRequirement")));
            }
            if (sdiRequest.isRequestItem("attachmentoperation") && (sdiRequest.getSDCid().equalsIgnoreCase("LV_DataCapture") || sdiRequest.getSDCid().equalsIgnoreCase("Instrument") || sdiRequest.getSDCid().equalsIgnoreCase("LV_InstrumentModel"))) {
                this.logInfo("Getting sdiattachmentoperation data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattachmentoperation.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattachmentoperation WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiattachmentoperation.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiattachmentoperation.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiattachmentoperation.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData sdiattachmentoperation", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattachmentoperation.*," + SUBST_RSETITEMS_COLS + "FROM sdiattachmentoperation, " + listFrom + " WHERE sdiattachmentoperation.sdcid = ? AND sdiattachmentoperation.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiattachmentoperation.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiattachmentoperation.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData sdiattachmentoperation", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved sdiattachmentoperation data");
                this.logInfo("sdiattachmentoperation Select=" + select);
                sdiData.setDataset("attachmentoperation", new DataSet(db.getResultSet("getSDIData sdiattachmentoperation")));
            }
            if (sdiRequest.isRequestItem("datacapture")) {
                this.logInfo("Getting sdidatacapture data");
                if (rsetQuery) {
                    if (sdcid.equalsIgnoreCase("LV_DataCapture")) {
                        select = new StringBuffer("SELECT sdidatacapture.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdidatacapture WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".keyid1 = sdidatacapture.datacaptureid");
                        db.createPreparedResultSet("getSDIData SDIDataCapture", select.toString(), new Object[]{rsetid, sdcid});
                    } else {
                        select = new StringBuffer("SELECT sdidatacapture.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdidatacapture WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdidatacapture.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdidatacapture.keyid1");
                        for (int i = 1; i < keycols; ++i) {
                            select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdidatacapture.keyid").append(String.valueOf(i + 1));
                        }
                        db.createPreparedResultSet("getSDIData SDIDataCapture", select.toString(), new Object[]{rsetid, sdcid});
                    }
                } else if (sdcid.equalsIgnoreCase("LV_DataCapture")) {
                    select = new StringBuffer("SELECT sdidatacapture.*," + SUBST_RSETITEMS_COLS + "FROM sdidatacapture, " + listFrom + " WHERE  sdidatacapture.datacaptureid = " + tableid + "." + keycolid1 + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIDataCapture", select.toString(), detailBindVars);
                } else {
                    select = new StringBuffer("SELECT sdidatacapture.*," + SUBST_RSETITEMS_COLS + "FROM sdidatacapture, " + listFrom + " WHERE sdidatacapture.sdcid = ? AND sdidatacapture.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdidatacapture.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdidatacapture.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIDataCapture", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIDataCapture data");
                this.logInfo("SDIDataCapture Select=" + select);
                sdiData.setDataset("datacapture", new DataSet(db.getResultSet("getSDIData SDIDataCapture")));
            }
            if (sdiRequest.isRequestItem("coc")) {
                this.logInfo("Getting coc data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdicoc.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdicoc WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdicoc.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdicoc.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdicoc.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData COC", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdicoc.*," + SUBST_RSETITEMS_COLS + "FROM sdicoc, " + listFrom + " WHERE sdicoc.sdcid = ? AND sdicoc.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdicoc.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdicoc.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData COC", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved COC data");
                this.logInfo("COC Select=" + select);
                sdiData.setDataset("coc", new DataSet(db.getResultSet("getSDIData COC")));
            }
            if ((pricelistRequest = sdiRequest.getRequest("pricelist")).length() > 0) {
                this.getExtendedRequest(pricelistRequest, "pricelistid", "", "", "sdipricelist", "pricelist", tableid, keycolid1, keycolid2, keycolid3, extendedSelect, extendedFrom, extendedWhere);
                this.logInfo("Getting pricelist data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdipricelist.*, " + RSETITEMS_COLS + extendedSelect.toString() + " FROM\t" + rsetitemsTable + ", sdipricelist " + extendedFrom.toString() + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdipricelist.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdipricelist.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdipricelist.keyid").append(String.valueOf(i + 1));
                    }
                    select.append(extendedWhere.toString());
                    db.createPreparedResultSet("getSDIData Pricelist", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdipricelist.*," + SUBST_RSETITEMS_COLS + extendedSelect.toString() + " FROM " + listFrom + ", sdipricelist " + extendedFrom.toString() + " WHERE sdipricelist.sdcid = ? AND sdipricelist.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdipricelist.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdipricelist.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Pricelist", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved PRICELIST data");
                this.logInfo("Pricelist Select=" + select);
                sdiData.setDataset("pricelist", new DataSet(db.getResultSet("getSDIData Pricelist")));
            }
            if ((workitemRequest = sdiRequest.getRequest("sdiworkitem")).length() > 0) {
                this.getExtendedRequest(workitemRequest, "workitemid", "workitemversionid", "", "sdiworkitem", "workitem", tableid, keycolid1, keycolid2, keycolid3, extendedSelect, extendedFrom, extendedWhere);
                this.logInfo("Getting workitem data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiworkitem.*, " + RSETITEMS_COLS + extendedSelect.toString() + " FROM\t" + rsetitemsTable + ", sdiworkitem " + extendedFrom.toString() + " WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworkitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworkitem.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiworkitem.keyid").append(String.valueOf(i + 1));
                    }
                    select.append(extendedWhere.toString());
                    select.append(" AND sdiworkitem.workitemid != '_Reflex' ");
                    db.createPreparedResultSet("getSDIData Workitem", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiworkitem.*," + SUBST_RSETITEMS_COLS + extendedSelect.toString() + " FROM " + listFrom + ", sdiworkitem " + extendedFrom.toString() + " WHERE sdiworkitem.sdcid = ? AND sdiworkitem.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiworkitem.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiworkitem.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    select.append(" AND sdiworkitem.workitemid != '_Reflex' ");
                    db.createPreparedResultSet("getSDIData Workitem", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved WORKITEM data");
                this.logInfo("Workitem Select=" + select);
                DataSet sdiworkitemDs = this.filterByWISecurity(new DataSet(db.getResultSet("getSDIData Workitem")), ddtService);
                sdiData.setDataset("sdiworkitem", sdiworkitemDs);
            }
            if (sdiRequest.isRequestItem("sdiworkitemitem")) {
                this.logInfo("Getting workitemitem data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiworkitemitem.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiworkitemitem WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworkitemitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworkitemitem.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiworkitemitem.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData workitemitem", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiworkitemitem.*," + SUBST_RSETITEMS_COLS + "FROM sdiworkitemitem, " + listFrom + " WHERE sdiworkitemitem.sdcid = ? AND sdiworkitemitem.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiworkitemitem.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiworkitemitem.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData workitemitem", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved WORKITEMITEM data");
                this.logInfo("WORKITEMITEM Select=" + select);
                DataSet sdiworkitemitemDs = this.filterByWISecurity(new DataSet(db.getResultSet("getSDIData workitemitem")), ddtService);
                sdiData.setDataset("sdiworkitemitem", sdiworkitemitemDs);
            }
            if (rsetQuery && sdiRequest.isRequestItem("workitemrelation")) {
                this.logInfo("Getting workitemrelation data");
                select = new StringBuffer("SELECT sdiworkitemrelation.*, sdiworkitem.usersequence \"__sdiworkitem_usersequence\", " + RSETITEMS_COLS + " FROM\t" + rsetitemsTable + ", sdiworkitem, sdiworkitemrelation WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworkitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworkitem.keyid1 AND \t\t" + rsetitemsTable + ".keyid2 = sdiworkitem.keyid2 AND \t\t" + rsetitemsTable + ".keyid3 = sdiworkitem.keyid3 AND        sdiworkitemrelation.sdcid = sdiworkitem.sdcid AND        sdiworkitemrelation.keyid1 = sdiworkitem.keyid1 AND        sdiworkitemrelation.keyid2 = sdiworkitem.keyid2 AND        sdiworkitemrelation.keyid3 = sdiworkitem.keyid3 AND        sdiworkitemrelation.workitemid = sdiworkitem.workitemid AND        sdiworkitemrelation.workiteminstance = sdiworkitem.workiteminstance ");
                db.createPreparedResultSet("getSDIData WorkItemRelation", select.toString(), new Object[]{rsetid});
                this.logInfo("Retrieved WORKITEMRELATION data");
                this.logInfo("WorkItemrelation Select=" + select);
                sdiData.setDataset("workitemrelation", new DataSet(db.getResultSet("getSDIData WorkItemRelation")));
            }
            if (sdiRequest.isRequestItem("document")) {
                this.logInfo("Getting sdidocument data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdidocument.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdidocument WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdidocument.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdidocument.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdidocument.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIDocument", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdidocument.*," + SUBST_RSETITEMS_COLS + "FROM sdidocument, " + listFrom + " WHERE sdidocument.sdcid = ? AND sdidocument.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdidocument.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdidocument.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIDocument", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIDocument data");
                this.logInfo("SDIDocument Select=" + select);
                sdiData.setDataset("document", new DataSet(db.getResultSet("getSDIData SDIDocument")));
            }
            if (sdiRequest.isRequestItem("formrule")) {
                this.logInfo("Getting sdiformrule data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiformrule.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiformrule WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiformrule.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiformrule.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiformrule.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIFormrule", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiformrule.*," + SUBST_RSETITEMS_COLS + "FROM sdiformrule, " + listFrom + " WHERE sdiformrule.sdcid = ? AND sdiformrule.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiformrule.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiformrule.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIFormrule", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIFormrule data");
                this.logInfo("SDIFormrule Select=" + select);
                sdiData.setDataset("formrule", new DataSet(db.getResultSet("getSDIData SDIFormrule")));
            }
            if (sdiRequest.isRequestItem("sdiworkflowrule")) {
                this.logInfo("Getting sdiworkflowrule data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiworkflowrule.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiworkflowrule WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworkflowrule.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworkflowrule.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiworkflowrule.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIWorkflowRule", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiworkflowrule.*," + SUBST_RSETITEMS_COLS + "FROM sdiworkflowrule, " + listFrom + " WHERE sdiworkflowrule.sdcid = ? AND sdiworkflowrule.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiworkflowrule.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiworkflowrule.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIWorkflowRule", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIWorkflowRule data");
                this.logInfo("SDIWorkflowRule Select=" + select);
                sdiData.setDataset("sdiworkflowrule", new DataSet(db.getResultSet("getSDIData SDIWorkflowRule")));
            }
            if (sdiRequest.isRequestItem("category")) {
                this.logInfo("Getting category data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT categoryitem.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", categoryitem WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = categoryitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = categoryitem.keyid1");
                    db.createPreparedResultSet("getSDIData Category", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT categoryitem.*," + SUBST_RSETITEMS_COLS + "FROM categoryitem, " + listFrom + " WHERE categoryitem.sdcid = ? AND categoryitem.keyid1 = " + tableid + "." + keycolid1 + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Category", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved CATEGORY data");
                this.logInfo("Category Select=" + select);
                sdiData.setDataset("category", new DataSet(db.getResultSet("getSDIData Category")));
            }
            if (sdiRequest.isRequestItem("role")) {
                this.logInfo("Getting role data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdirole.*," + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdirole WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdirole.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdirole.keyid1");
                    db.createPreparedResultSet("getSDIData Roles", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdirole.*," + SUBST_RSETITEMS_COLS + "FROM sdirole, " + listFrom + " WHERE sdirole.sdcid = ? AND sdirole.keyid1 = " + tableid + "." + keycolid1 + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Roles", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved ROLE data");
                this.logInfo("Role Select=" + select);
                sdiData.setDataset("role", new DataSet(db.getResultSet("getSDIData Roles")));
            }
            if (sdiRequest.isRequestItem("sdialias")) {
                this.logInfo("Getting sdialias data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdialias.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdialias WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdialias.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdialias.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(i + 1).append(" = sdialias.keyid").append(i + 1);
                    }
                    db.createPreparedResultSet("getSDIData SDIAlias", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdialias.*," + SUBST_RSETITEMS_COLS + "FROM sdialias, " + listFrom + " WHERE sdialias.sdcid = ? AND sdialias.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdialias.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdialias.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIAlias", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIAlias data");
                this.logInfo("SDIAlias Select=" + select);
                DataSet aliasDS = new DataSet(db.getResultSet("getSDIData SDIAlias"));
                if (sdiRequest.isReturnMaskedData()) {
                    DataMaskUtil dataMaskUtil = new DataMaskUtil(this.sapphireConnection);
                    dataMaskUtil.maskSDIAliasDataSet(aliasDS, sdiRequest.isShowHiddenRecords());
                }
                sdiData.setDataset("sdialias", aliasDS);
            }
            if (sdiRequest.isRequestItem("attribute")) {
                String[] cols;
                this.logInfo("Getting sdiattribute data");
                String attributeRequest = sdiRequest.getRequest("attribute");
                StringBuilder includeAttributes = new StringBuilder();
                if (attributeRequest.contains("attribute[") && (cols = RequestParser.parseColItem(attributeRequest)) != null && cols.length > 0 && cols.length <= 1000) {
                    includeAttributes.append(" sdiattribute.attributesdcid='" + sdcid + "' AND sdiattribute.attributeid in ( ");
                    for (int col = 0; col < cols.length; ++col) {
                        includeAttributes.append((col > 0 ? "," : "") + "'" + cols[col] + "'");
                    }
                    includeAttributes.append(" ) ");
                }
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattribute.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattribute WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND " + (includeAttributes.length() > 0 ? includeAttributes + " AND " : "") + "\t\t" + rsetitemsTable + ".sdcid = sdiattribute.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiattribute.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(i + 1).append(" = sdiattribute.keyid").append(i + 1);
                    }
                    db.createPreparedResultSet("getSDIData SDIAttribute", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattribute.*," + SUBST_RSETITEMS_COLS + "FROM sdiattribute, " + listFrom + " WHERE sdiattribute.sdcid = ? AND " + (includeAttributes.length() > 0 ? includeAttributes + " AND " : "") + "sdiattribute.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiattribute.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiattribute.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIAttribute", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIAttribute data");
                this.logInfo("SDIAttribute Select=" + select);
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("getSDIData SDIAttribute"), true, db.getDbms());
                sdiData.setDataset("attribute", ds);
            }
            if (sdiRequest.isRequestItem("datasetattribute")) {
                this.logInfo("Getting DATASET sdiattribute data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattribute.*, sdidata.sdcid sourcesdcid, sdidata.keyid1 sourcekeyid1, sdidata.keyid2 sourcekeyid2, sdidata.keyid3 sourcekeyid3, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattribute, sdidata WHERE\t         sdiattribute.sdcid = 'DataSet'         AND sdiattribute.keyid1 = sdidata.sdidataid       AND " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdidata.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdidata.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdidata.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData Dataset Attribute", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattribute.*, sdidata.sdcid sourcesdcid, sdidata.keyid1 sourcekeyid1, sdidata.keyid2 sourcekeyid2, sdidata.keyid3 sourcekeyid3," + SUBST_RSETITEMS_COLS + "FROM sdiattribute, sdidata, " + listFrom + " WHERE  sdiattribute.sdcid = 'DataSet' AND sdiattribute.keyid1 = sdidata.sdidataid AND sdidata.sdcid = ?  AND sdidata.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdidata.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdidata.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Dataset Attribute", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved Dataset SDIAttribute data");
                this.logInfo("Dataset SDIAttribute Select=" + select);
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("getSDIData Dataset Attribute"), true, db.getDbms());
                sdiData.setDataset("datasetattribute", ds);
            }
            if (sdiRequest.isRequestItem("dataitemattribute")) {
                this.logInfo("Getting DATAITEM sdiattribute data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattribute.*, sdidataitem.sdcid sourcesdcid, sdidataitem.keyid1 sourcekeyid1, sdidataitem.keyid2 sourcekeyid2, sdidataitem.keyid3 sourcekeyid3, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattribute, sdidataitem WHERE\t         sdiattribute.sdcid = 'DataItem'         AND sdiattribute.keyid1 = sdidataitem.sdidataitemid       AND " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdidataitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdidataitem.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdidataitem.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData Dataitem Attribute", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattribute.*, sdidataitem.sdcid sourcesdcid, sdidataitem.keyid1 sourcekeyid1, sdidataitem.keyid2 sourcekeyid2, sdidataitem.keyid3 sourcekeyid3," + SUBST_RSETITEMS_COLS + "FROM sdiattribute, sdidataitem, " + listFrom + " WHERE  sdiattribute.sdcid = 'DataItem' AND sdiattribute.keyid1 = sdidataitem.sdidataitemid AND sdidataitem.sdcid = ? AND  sdidataitem.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdidataitem.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdidataitem.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Dataitem Attribute", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved Dataitem SDIAttribute data");
                this.logInfo("DataItem SDIAttribute Select=" + select);
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("getSDIData Dataitem Attribute"), true, db.getDbms());
                sdiData.setDataset("dataitemattribute", ds);
            }
            if (sdiRequest.isRequestItem("sdiworkitemattribute")) {
                this.logInfo("Getting Workitem sdiattribute data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiattribute.*, sdiworkitem.sdcid sourcesdcid, sdiworkitem.keyid1 sourcekeyid1, sdiworkitem.keyid2 sourcekeyid2, sdiworkitem.keyid3 sourcekeyid3, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiattribute, sdiworkitem WHERE\t         sdiattribute.sdcid = 'SDIWorkItem'         AND sdiattribute.keyid1 = sdiworkitem.sdiworkitemid       AND " + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworkitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworkitem.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiworkitem.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData Workitem Attribute", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiattribute.*, sdiworkitem.sdcid sourcesdcid, sdiworkitem.keyid1 sourcekeyid1, sdiworkitem.keyid2 sourcekeyid2, sdiworkitem.keyid3 sourcekeyid3," + SUBST_RSETITEMS_COLS + "FROM sdiattribute, sdiworkitem, " + listFrom + " WHERE  sdiattribute.sdcid = 'SDIWorkItem' AND sdiattribute.keyid1 = sdiworkitem.sdiworkitemid AND sdiworkitem.sdcid = ? AND  sdiworkitem.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiworkitem.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiworkitem.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData Workitem Attribute", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved Workitem SDIAttribute data");
                this.logInfo("Workitem SDIAttribute Select=" + select);
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("getSDIData Workitem Attribute"), true, db.getDbms());
                sdiData.setDataset("sdiworkitemattribute", ds);
            }
            if (sdiRequest.isRequestItem("sdieventplan")) {
                this.logInfo("Getting sdieventplan data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdieventplan.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdieventplan WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdieventplan.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdieventplan.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdieventplan.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIEventPlan", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdieventplan.*," + SUBST_RSETITEMS_COLS + "FROM sdieventplan, " + listFrom + " WHERE sdieventplan.sdcid = ? AND sdieventplan.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdieventplan.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdieventplan.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIEventPlan", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIEventPlan data");
                this.logInfo("SDIEventPlan Select=" + select);
                sdiData.setDataset("sdieventplan", new DataSet(db.getResultSet("getSDIData SDIEventPlan")));
            }
            if (sdiRequest.isRequestItem("sdieventplanitem")) {
                this.logInfo("Getting sdieventplanitem data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdieventplanitem.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdieventplanitem WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdieventplanitem.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdieventplanitem.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdieventplanitem.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIEventPlanItem", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdieventplanitem.*," + SUBST_RSETITEMS_COLS + "FROM sdieventplanitem, " + listFrom + " WHERE sdieventplanitem.sdcid = ? AND sdieventplanitem.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdieventplanitem.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdieventplanitem.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIEventPlanItem", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIEventPlanItem data");
                this.logInfo("SDIEventPlanItem Select=" + select);
                sdiData.setDataset("sdieventplanitem", new DataSet(db.getResultSet("getSDIData SDIEventPlanItem")));
            }
            if (sdiRequest.isRequestItem("sdieventplanitemproperty")) {
                this.logInfo("Getting sdieventplanitemproperty data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdieventplanitemproperty.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdieventplanitemproperty WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdieventplanitemproperty.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdieventplanitemproperty.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdieventplanitemproperty.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIEventPlanItemProperty", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdieventplanitemproperty.*," + SUBST_RSETITEMS_COLS + "FROM sdieventplanitemproperty, " + listFrom + " WHERE sdieventplanitemproperty.sdcid = ? AND sdieventplanitemproperty.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdieventplanitemproperty.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdieventplanitemproperty.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIEventPlanItemProperty", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIEventPlanItemProperty data");
                this.logInfo("SDIEventPlanItemProperty Select=" + select);
                sdiData.setDataset("sdieventplanitemproperty", new DataSet(db.getResultSet("getSDIData SDIEventPlanItemProperty")));
            }
            if (sdiRequest.isRequestItem("sdiworksheetrule")) {
                this.logInfo("Getting sdiworksheetrule data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdiworksheetrule.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdiworksheetrule WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdiworksheetrule.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdiworksheetrule.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdiworksheetrule.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData SDIWORKSHEETRULE", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdiworksheetrule.*," + SUBST_RSETITEMS_COLS + "FROM sdiworksheetrule, " + listFrom + " WHERE sdiworksheetrule.sdcid = ? AND sdiworksheetrule.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdiworksheetrule.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdiworksheetrule.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData SDIWORKSHEETRULE", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved SDIWORKSHEETRULE data");
                this.logInfo("SDIWORKSHEETRULE Select=" + select);
                sdiData.setDataset("sdiworksheetrule", new DataSet(db.getResultSet("getSDIData SDIWORKSHEETRULE")));
            }
            if (sdiRequest.isRequestItem("eventplanhistory")) {
                this.logInfo("Getting eventplanhistory data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT eventplanhistory.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", eventplanhistory WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = eventplanhistory.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = eventplanhistory.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = eventplanhistory.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData EventPlanHistory", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT eventplanhistory.*," + SUBST_RSETITEMS_COLS + "FROM eventplanhistory, " + listFrom + " WHERE eventplanhistory.sdcid = ? AND eventplanhistory.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND eventplanhistory.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND eventplanhistory.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData EventPlanHistory", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved EventPlanHistory data");
                this.logInfo("EventPlanHistory Select=" + select);
                sdiData.setDataset("eventplanhistory", new DataSet(db.getResultSet("getSDIData EventPlanHistory")));
            }
            if (sdiRequest.isRequestItem("sdidatacrosssdicalc")) {
                this.logInfo("Getting sdidatacrosssdicalc data");
                if (rsetQuery) {
                    select = new StringBuffer("SELECT sdidatacrosssdicalc.*, " + RSETITEMS_COLS + "FROM\t" + rsetitemsTable + ", sdidatacrosssdicalc WHERE\t" + rsetitemsTable + ".rsetid = ? AND \t\t" + rsetitemsTable + ".sdcid = ? AND \t\t" + rsetitemsTable + ".sdcid = sdidatacrosssdicalc.sdcid AND \t\t" + rsetitemsTable + ".keyid1 = sdidatacrosssdicalc.keyid1");
                    for (int i = 1; i < keycols; ++i) {
                        select.append(" AND ").append(rsetitemsTable).append(".keyid").append(String.valueOf(i + 1)).append(" = sdidatacrosssdicalc.keyid").append(String.valueOf(i + 1));
                    }
                    db.createPreparedResultSet("getSDIData sdidatacrosssdicalc", select.toString(), new Object[]{rsetid, sdcid});
                } else {
                    select = new StringBuffer("SELECT sdidatacrosssdicalc.*," + SUBST_RSETITEMS_COLS + "FROM sdidatacrosssdicalc, " + listFrom + " WHERE sdidatacrosssdicalc.sdcid = ? AND sdidatacrosssdicalc.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND sdidatacrosssdicalc.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND sdidatacrosssdicalc.keyid3 = " + tableid + "." + keycolid3 : "") + " AND (" + listWhere + ")");
                    db.createPreparedResultSet("getSDIData sdidatacrosssdicalc", select.toString(), detailBindVars);
                }
                this.logInfo("Retrieved sdidatacrosssdicalc data");
                this.logInfo("sdidatacrosssdicalc Select=" + select);
                sdiData.setDataset("sdidatacrosssdicalc", new DataSet(db.getResultSet("getSDIData sdidatacrosssdicalc")));
            }
            if ((extRequests = sdiRequest.getSDIRequests()).length > 0) {
                for (int i = 0; i < extRequests.length; ++i) {
                    SDIRequest extRequest = extRequests[i];
                    PropertyList extSdc = ddtService.getSDCProperties(extRequest.getSDCid());
                    PropertyListCollection extLinks = extSdc.getCollection("links");
                    PropertyList extLink = null;
                    if (extRequest.getLinkId() == null || extRequest.getLinkId().length() == 0) {
                        for (int j = 0; j < extLinks.size(); ++j) {
                            PropertyList extLinkTemp = extLinks.getPropertyList(j);
                            if (!extLinkTemp.getProperty("linktype").equals("F") || !extLinkTemp.getProperty("linksdcid").equals(sdcid)) continue;
                            extLink = extLinkTemp;
                            break;
                        }
                    } else {
                        extLink = extLinks.find("linkid", extRequest.getLinkId());
                    }
                    if (extLink != null) {
                        extRequest.setQueryFrom(extSdc.getProperty("tableid"));
                        String additionalQueryWhere = extRequest.getQueryWhere();
                        String queryWhere = "";
                        queryWhere = db.isOracle() ? "(" + extLink.getProperty("sdccolumnid") + (keycols >= 2 ? ", " + extLink.getProperty("sdccolumnid2") : "") + (keycols >= 3 ? ", " + extLink.getProperty("sdccolumnid3") : "") + ") IN ( SELECT keyid1" + (keycols >= 2 ? ", keyid2" : "") + (keycols >= 3 ? ", keyid3" : "") + " FROM " + rsetitemsTable + " WHERE rsetid = '" + rsetid + "' )" : extLink.getProperty("sdccolumnid") + (keycols >= 2 ? " + ';' + " + extLink.getProperty("sdccolumnid2") : "") + (keycols >= 3 ? " + ';' + " + extLink.getProperty("sdccolumnid3") : "") + " IN ( SELECT keyid1" + (keycols >= 2 ? " + ';' + keyid2" : "") + (keycols >= 3 ? " + ';' + keyid3" : "") + " FROM " + rsetitemsTable + " WHERE rsetid = '" + rsetid + "' )";
                        extRequest.setQueryWhere(queryWhere + (additionalQueryWhere != null && additionalQueryWhere.length() > 0 ? " AND (" + additionalQueryWhere + ")" : ""));
                    }
                    if (extRequest.getQueryFrom().length() > 0 && extRequest.getQueryWhere().length() > 0) {
                        SDIData additionalData = this.getSDIData(extRequest);
                        additionalData.setLinkid(extLink.getProperty("linkid"));
                        sdiData.setSDIData(extRequest.getRequestid().length() > 0 ? extRequest.getRequestid() : extRequest.getSDCid(), additionalData);
                        continue;
                    }
                    this.logError("Failed to find SDC link from " + extRequest.getSDCid() + " to " + sdcid + " - ignoring additional SDIData request");
                }
            }
            String[] linkids = new String[links];
            String[] linktables = new String[links];
            String[][] linktablekeys = new String[links][];
            for (int link = 0; link < links; ++link) {
                PropertyList linkProps = sdcLinks.getPropertyList(link);
                linkids[link] = linkProps.getProperty("linkid");
                linktables[link] = linkProps.getProperty("linktableid");
                int keycolcount = Integer.parseInt(linkProps.getProperty("keycolcount"));
                linktablekeys[link] = new String[keycolcount];
                for (int key = 1; key <= keycolcount; ++key) {
                    linktablekeys[link][key - 1] = linkProps.getProperty("keycolid" + String.valueOf(key));
                }
            }
            sdiData.setLinks(linkids, linktables);
            if (linktables != null) {
                for (int i = 0; i < linktables.length; ++i) {
                    sdiData.setLinkTableKeys(linktables[i], linktablekeys[i]);
                }
            }
            int sizeofArr = 0;
            for (int link = 0; link < detaillinks; ++link) {
                PropertyList linkProps = detailLinks.getPropertyList(link);
                if (!linkProps.getProperty("linktype").equals("D")) continue;
                ++sizeofArr;
            }
            String[] detaillinkids = new String[sizeofArr];
            String[] detaildetaillinkids = new String[sizeofArr];
            String[] detaillinktables = new String[sizeofArr];
            String[][] detaillinktablekeys = new String[sizeofArr][];
            int arrele = -1;
            for (int link = 0; link < detaillinks; ++link) {
                PropertyList linkProps = detailLinks.getPropertyList(link);
                if (!linkProps.getProperty("linktype").equals("D")) continue;
                detaillinkids[++arrele] = linkProps.getProperty("linkid");
                detaildetaillinkids[arrele] = linkProps.getProperty("detaillinkid");
                detaillinktables[arrele] = linkProps.getProperty("linktableid");
                int keycolcount = Integer.parseInt(linkProps.getProperty("keycolcount"));
                detaillinktablekeys[arrele] = new String[keycolcount];
                for (int key = 1; key <= keycolcount; ++key) {
                    detaillinktablekeys[arrele][key - 1] = linkProps.getProperty("keycolid" + String.valueOf(key));
                }
            }
            if (detaildetaillinkids != null) {
                sdiData.setDetailLinks(detaillinkids, detaildetaillinkids, detaillinktables);
            }
            if (detaillinktables != null) {
                for (int i = 0; i < detaillinktables.length; ++i) {
                    sdiData.setDetailLinkTableKeys(detaillinktables[i], detaillinktablekeys[i]);
                }
            }
            sdiData.setRequestStatus(requeststatus);
            sdiData.setQualifiedRows(qualifiedrows);
            if (sdiRequest.getRetainRsetid()) {
                if (fkrsetid.size() > 0) {
                    for (String k : fkrsetid.keySet()) {
                        sdiData.setPrimaryFKRsetid(k, (String)fkrsetid.get(k));
                    }
                }
                sdiData.setRsetid(rsetid);
            } else if (sdiRequest.getRetrieve() && rsetQuery) {
                das.clearRSet(new RSet(rsetid));
                for (String v : fkrsetid.values()) {
                    das.clearRSet(new RSet(v));
                }
            }
            SDIData sDIData = sdiData;
            return sDIData;
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get sdi data", e);
        }
        finally {
            db.reset();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private RSet createRSet(SDIRequest sdiRequest, PropertyList sdcprops, String rsetitemsTable) throws ServiceException {
        RSet rset = null;
        if (sdiRequest.getRetrieve()) {
            if (sdiRequest.getRsetid() != null && sdiRequest.getRsetid().length() != 0) return rset;
            try {
                DataAccessService das = new DataAccessService(this.sapphireConnection);
                String tableid = (String)sdcprops.get("tableid");
                boolean isQueryRset = false;
                if (sdiRequest.getQueryid() != null && sdiRequest.getQueryid().length() > 0 || sdiRequest.getQueryFrom() != null && sdiRequest.getQueryFrom().length() > 0) {
                    isQueryRset = true;
                    if (sdiRequest.getQueryFrom().equalsIgnoreCase("(default)")) {
                        sdiRequest.setQueryFrom(tableid);
                    }
                    if (sdiRequest.getQueryFrom() != null && sdiRequest.getQueryFrom().length() > 0) {
                        sdiRequest.setQueryParams(new String[12]);
                    }
                    if (sdcprops.get("templatableflag").equals("Y")) {
                        if (sdiRequest.getShowTemplatesOnly()) {
                            if (sdiRequest.getQueryWhere() == null || sdiRequest.getQueryWhere().length() == 0) {
                                sdiRequest.setQueryWhere(" ( " + tableid + ".templateflag = 'Y' ) ");
                            } else {
                                sdiRequest.setQueryWhere("(" + sdiRequest.getQueryWhere() + ") AND ( " + tableid + ".templateflag = 'Y' ) ");
                            }
                        } else if (!sdiRequest.getShowTemplates()) {
                            if (sdiRequest.getQueryWhere() == null || sdiRequest.getQueryWhere().length() == 0) {
                                sdiRequest.setQueryWhere(" ( " + tableid + ".templateflag = 'N' OR " + tableid + ".templateflag IS NULL ) ");
                            } else {
                                sdiRequest.setQueryWhere("(" + sdiRequest.getQueryWhere() + ") AND ( " + tableid + ".templateflag = 'N' OR " + tableid + ".templateflag IS NULL ) ");
                            }
                        }
                    }
                    if (sdiRequest.containsDataRequest()) {
                        rset = das.createRSetQDS(sdiRequest.getSDCid(), sdiRequest.getQueryid(), sdiRequest.getQueryParams(), sdiRequest.getQueryFrom(), sdiRequest.getQueryWhere(), null, sdiRequest.getVersionStatus(), sdiRequest.getRetrieveLimit(), sdiRequest.getParamlistidList(), sdiRequest.getParamlistversionidList(), sdiRequest.getVariantidList(), sdiRequest.getDatasetList(), sdiRequest.containsNonDataRequest(), false, sdiRequest.isShowHiddenRecords());
                    } else {
                        String embedsecurityflag = null;
                        if (sdiRequest.getQueryid().length() == 0 && sdiRequest.getQueryFrom() != null && (sdiRequest.getQueryFrom().trim().equalsIgnoreCase(tableid) || sdiRequest.getQueryFrom().trim().equalsIgnoreCase(tableid + ", categoryitem"))) {
                            embedsecurityflag = "Y";
                        }
                        rset = das.createRSetQ(sdiRequest.getSDCid(), sdiRequest.getQueryid(), sdiRequest.getQueryParams(), sdiRequest.getQueryFrom(), sdiRequest.getQueryWhere(), sdiRequest.isUseRSetOrderBy() ? sdiRequest.getQueryOrderBy() : null, sdiRequest.getVersionStatus(), sdiRequest.getRetrieveLimit(), rsetitemsTable != null && !rsetitemsTable.equalsIgnoreCase("rsetitems"), sdiRequest.isShowHiddenRecords(), embedsecurityflag);
                    }
                } else {
                    rset = sdiRequest.containsDataRequest() ? (sdiRequest.getWorkitemidList().length() > 0 && sdiRequest.getWorkiteminstanceList().length() > 0 ? das.createRSetWI(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List(), sdiRequest.getWorkitemidList(), sdiRequest.getWorkiteminstanceList(), sdiRequest.containsNonDataRequest(), sdiRequest.getSecurityBypassCode(), sdiRequest.isShowHiddenRecords()) : (sdiRequest.getPropsMatch() ? das.createRSetDS(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List(), sdiRequest.getParamlistidList(), sdiRequest.getParamlistversionidList(), sdiRequest.getVariantidList(), sdiRequest.getDatasetList(), sdiRequest.containsNonDataRequest(), false, sdiRequest.getSecurityBypassCode(), sdiRequest.isShowHiddenRecords()) : das.createRSetDSNP(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List(), sdiRequest.getParamlistidList(), sdiRequest.getParamlistversionidList(), sdiRequest.getVariantidList(), sdiRequest.getDatasetList(), sdiRequest.containsNonDataRequest(), false, sdiRequest.isShowHiddenRecords()))) : (sdiRequest.getAltKeyIdCol() != null && sdiRequest.getAltKeyIdCol().length() > 0 && !sdcprops.getProperty("keycolid1").equals(sdiRequest.getAltKeyIdCol()) && sdiRequest.getAltKeyList() != null && sdiRequest.getAltKeyList().length() > 0 ? das.createRSetAlt(sdiRequest.getSDCid(), sdiRequest.getAltKeyIdCol(), sdiRequest.getAltKeyList()) : das.createRSet(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List(), sdiRequest.isShowHiddenRecords(), sdiRequest.getSecurityBypassCode()));
                    if (sdiRequest.getQueryWhere() != null && sdiRequest.getQueryWhere().length() > 0) {
                        DBUtil db = new DBUtil();
                        db.setConnection(this.sapphireConnection);
                        try {
                            String querywhere = sdiRequest.getQueryWhere();
                            if (querywhere != null && querywhere.indexOf("[%currentuser%]") >= 0) {
                                querywhere = StringUtil.replaceAll(querywhere, "[%currentuser%]", this.sapphireConnection.getSysuserId());
                            }
                            db.executeSQL("DELETE FROM rsetitems WHERE rsetid='" + rset.getRsetid() + "' AND keyid1 not in ( SELECT " + sdcprops.getProperty("keycolid1") + " FROM " + sdcprops.getProperty("tableid") + " WHERE " + querywhere + ")");
                            if (sdiRequest.containsDataRequest()) {
                                db.executeSQL("DELETE FROM rsetitemsds WHERE rsetid='" + rset.getRsetid() + "' AND keyid1 not in ( SELECT " + sdcprops.getProperty("keycolid1") + " FROM " + sdcprops.getProperty("tableid") + " WHERE " + querywhere + ")");
                            }
                        }
                        catch (SapphireException se) {
                            throw new ServiceException(se);
                        }
                    }
                }
                rset.setRequestStatus(1);
                int lockscope = -1;
                if (sdiRequest.getPrimaryLockOption() != null && sdiRequest.getPrimaryLockOption().length() > 0 || sdiRequest.getLockOption() != null && sdiRequest.getLockOption().length() > 0) {
                    lockscope = 1;
                }
                if (sdiRequest.getDataLockOption() != null && sdiRequest.getDataLockOption().length() > 0 || sdiRequest.getLockOption() != null && sdiRequest.getLockOption().length() > 0) {
                    lockscope = lockscope == -1 ? 2 : 3;
                }
                if (lockscope <= -1 || isQueryRset && (!isQueryRset || rset.getQualifiedRows() <= 0)) return rset;
                IntHolder primarystatusHolder = new IntHolder();
                IntHolder datasetstatusHolder = new IntHolder();
                rset.setRequestStatus(2);
                rset.setRSet(das.lockRSet(rset, sdiRequest.getDataLockOption() != null && sdiRequest.getDataLockOption().length() > 0 ? sdiRequest.getDataLockOption() : (sdiRequest.getPrimaryLockOption() != null && sdiRequest.getPrimaryLockOption().length() > 0 ? sdiRequest.getPrimaryLockOption() : sdiRequest.getLockOption()), lockscope, sdiRequest.getAutoLockTimeout(), sdiRequest.getValidateCheckout()));
                rset.setRequestStatus(1);
                if (primarystatusHolder.value == 2 && datasetstatusHolder.value == 2) {
                    rset.setRequestStatus(100);
                    return rset;
                }
                if (primarystatusHolder.value == 2) {
                    rset.setRequestStatus(101);
                    return rset;
                }
                if (datasetstatusHolder.value != 2) return rset;
                rset.setRequestStatus(102);
                return rset;
            }
            catch (ServiceException e) {
                throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create rset for request", e);
            }
        }
        this.logInfo("Retrieving meta data only - no data to be returned");
        sdiRequest.setRsetid("xxx");
        return rset;
    }

    private void getColumnLinkSql(DDTService ddtService, String[] columns, int column, PropertyList sdc, PropertyListCollection linkdata, StringBuffer extendedSelect, StringBuffer extendedFrom) throws ServiceException {
        String columnid = columns[column];
        if (columnid.startsWith("trackitem.")) {
            boolean hasnoalias = RequestParser.parseAlias(columnid).equals(columnid);
            if (hasnoalias) {
                extendedSelect.append(", ").append(columnid).append(" \"").append(columnid).append("\"");
            } else {
                extendedSelect.append(", ").append(columnid).append("");
            }
            if (extendedSelect.indexOf("(SELECT COUNT(trackitemid) FROM trackitem WHERE ") == -1) {
                extendedSelect.append(", ").append("(SELECT COUNT(trackitemid) FROM trackitem WHERE ");
                extendedSelect.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid1")).append(" = ");
                extendedSelect.append("trackitem").append(".").append("linkkeyid1 ");
                extendedSelect.append("AND ");
                if (sdc.getProperty("keycolid2").length() > 0) {
                    extendedSelect.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid2")).append(" = ");
                    extendedSelect.append("trackitem").append(".").append("linkkeyid2 ");
                    extendedSelect.append("AND ");
                }
                if (sdc.getProperty("keycolid3").length() > 0) {
                    extendedSelect.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid3")).append(" = ");
                    extendedSelect.append("trackitem").append(".").append("linkkeyid3 ");
                    extendedSelect.append("AND ");
                }
                extendedSelect.append("trackitem").append(".").append("linksdcid =").append("'").append(sdc.getProperty("sdcid")).append("' ");
                extendedSelect.append(") \"__ticount\"");
            }
            if (extendedFrom.indexOf(" LEFT OUTER JOIN trackitem") == -1) {
                extendedFrom.append(" LEFT OUTER JOIN trackitem");
                extendedFrom.append(" ON ").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid1")).append(" = ");
                extendedFrom.append("trackitem").append(".").append("linkkeyid1 ");
                extendedFrom.append("AND ");
                if (sdc.getProperty("keycolid2").length() > 0) {
                    extendedFrom.append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid2")).append(" = ");
                    extendedFrom.append("trackitem").append(".").append("linkkeyid2 ");
                    extendedFrom.append("AND ");
                }
                if (sdc.getProperty("keycolid3").length() > 0) {
                    extendedFrom.append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid3")).append(" = ");
                    extendedFrom.append("trackitem").append(".").append("linkkeyid3 ");
                    extendedFrom.append("AND ");
                }
                extendedFrom.append("trackitem").append(".").append("linksdcid =").append("'").append(sdc.getProperty("sdcid")).append("' ");
                extendedFrom.append("AND ");
                extendedFrom.append("(SELECT COUNT(trackitemid) FROM trackitem WHERE ");
                extendedFrom.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid1")).append(" = ");
                extendedFrom.append("trackitem").append(".").append("linkkeyid1 ");
                extendedFrom.append("AND ");
                if (sdc.getProperty("keycolid2").length() > 0) {
                    extendedFrom.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid2")).append(" = ");
                    extendedFrom.append("trackitem").append(".").append("linkkeyid2 ");
                    extendedFrom.append("AND ");
                }
                if (sdc.getProperty("keycolid3").length() > 0) {
                    extendedFrom.append("").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid3")).append(" = ");
                    extendedFrom.append("trackitem").append(".").append("linkkeyid3 ");
                    extendedFrom.append("AND ");
                }
                extendedFrom.append("trackitem").append(".").append("linksdcid =").append("'").append(sdc.getProperty("sdcid")).append("' ");
                extendedFrom.append(")");
                extendedFrom.append("").append(" <= ").append("1 ");
            }
        } else if (columnid.startsWith("sdialias.")) {
            String aliastype = "";
            aliastype = StringUtil.split(columnid, ".")[1];
            extendedSelect.append(", ").append("(SELECT ");
            if (this.sapphireConnection.isSqlServer()) {
                extendedSelect.append("TOP 1 ");
            }
            extendedSelect.append("sdialias.aliasid FROM sdialias WHERE ");
            extendedSelect.append("sdialias.keyid1 = ").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid1")).append(" AND ");
            if (sdc.getProperty("keycolid2").length() > 0) {
                extendedSelect.append("sdialias.keyid2 = ").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid2")).append(" AND ");
            }
            if (sdc.getProperty("keycolid3").length() > 0) {
                extendedSelect.append("sdialias.keyid3 = ").append(sdc.getProperty("tableid")).append(".").append(sdc.getProperty("keycolid3")).append(" AND ");
            }
            extendedSelect.append("sdialias.sdcid = '").append(sdc.getProperty("sdcid")).append("' AND ");
            extendedSelect.append("sdialias.aliastype = '").append(aliastype).append("'");
            if (this.sapphireConnection.isOracle()) {
                extendedSelect.append(" AND ROWNUM <= 1) ");
            } else {
                extendedSelect.append(")");
            }
            extendedSelect.append("\"").append(columnid).append("\"");
        } else {
            for (int link = 0; link < (linkdata != null ? linkdata.size() : 0); ++link) {
                boolean fkversioned;
                String fkcolid;
                String linkcolid;
                PropertyList linkProps = linkdata.getPropertyList(link);
                if (linkProps == null || !linkProps.getProperty("linktype").equals("F") || !columnid.startsWith((linkcolid = linkProps.getProperty("sdccolumnid")) + ".") || (fkcolid = RequestParser.parseColumn(columnid).substring(linkcolid.length() + 1)).length() <= 0 || fkcolid.startsWith("_")) continue;
                boolean hasnoalias = RequestParser.parseAlias(columnid).equals(columnid);
                if (hasnoalias) {
                    extendedSelect.append(", ").append(linkProps.getProperty("tableid")).append(".").append(columnid.substring(linkcolid.length() + 1)).append(" \"").append(columnid).append("\"");
                } else {
                    extendedSelect.append(", ").append(StringUtil.replaceAll(columnid, linkcolid + ".", linkProps.getProperty("tableid") + ".", false));
                }
                PropertyList sdcProps = ddtService.getSDCProperties(linkProps.getProperty("linksdcid"));
                if (extendedFrom.indexOf(" LEFT OUTER JOIN " + linkProps.getProperty("tableid")) == -1) {
                    extendedFrom.append(" LEFT OUTER JOIN ").append(linkProps.getProperty("tableid"));
                    extendedFrom.append(" ON ").append(sdc.getProperty("tableid")).append(".").append(linkProps.getProperty("sdccolumnid")).append(" = ");
                    extendedFrom.append(linkProps.getProperty("tableid")).append(".").append(linkProps.getProperty("tableid")).append("id ");
                    if (linkProps.getProperty("sdccolumnid2").length() > 0) {
                        extendedFrom.append(" AND ").append(sdc.getProperty("tableid")).append(".").append(linkProps.getProperty("sdccolumnid2")).append(" = ");
                        extendedFrom.append(linkProps.getProperty("tableid")).append(".").append(sdcProps.getProperty("keycolid2")).append(" ");
                    }
                }
                if (!(fkversioned = sdcProps.getProperty("versionedflag", "N").equalsIgnoreCase("Y")) || extendedFrom.indexOf(", " + linkProps.getProperty("tableid") + ".versionstatus ") != -1) continue;
                extendedSelect.append(", ").append(linkProps.getProperty("tableid")).append(".").append("versionstatus").append(" \"").append(linkProps.getProperty("tableid")).append(".").append("versionstatus").append("\"");
            }
        }
    }

    private void getExtendedRequest(String request, String keycol1, String keycol2, String keycol3, String innertableid, String outertableid, String primarytableid, String primarykeycol1, String primarykeycol2, String primarykeycol3, StringBuffer extendedSelect, StringBuffer extendedFrom, StringBuffer extendedWhere) {
        this.logInfo("DATASET REQUEST: " + request);
        this.resetSqlBuffers(extendedSelect, extendedFrom, extendedWhere);
        String[] cols = RequestParser.parseColItem(request);
        if (cols != null && cols.length > 0) {
            for (int col = 0; col < cols.length; ++col) {
                if (this.getColumnKeyidJoin(cols[col], primarykeycol1, primarykeycol2, primarykeycol3, innertableid, primarytableid, extendedSelect, extendedFrom)) continue;
                this.getColumnFkJoin(cols[col], keycol1, keycol2, keycol3, innertableid, outertableid, extendedSelect, extendedFrom);
            }
        }
    }

    private boolean getColumnKeyidJoin(String columnid, String keycol1, String keycol2, String keycol3, String innertableid, String outertableid, StringBuffer extendedSelect, StringBuffer extendedFrom) {
        boolean rc = false;
        if (columnid.startsWith("keyid1.")) {
            boolean hasnoalias = RequestParser.parseAlias(columnid).equals(columnid);
            if (hasnoalias) {
                extendedSelect.append(", ").append(outertableid).append(".").append(columnid.substring(7)).append(" \"").append(columnid).append("\"");
            } else {
                extendedSelect.append(", ").append(outertableid).append(".").append(columnid.substring(7));
            }
            if (extendedFrom.indexOf(" LEFT OUTER JOIN " + outertableid + " ") == -1) {
                extendedFrom.append(" LEFT OUTER JOIN ").append(outertableid).append(" ");
                extendedFrom.append(" ON ").append(innertableid).append(".keyid1 = ").append(outertableid).append(".").append(keycol1);
                if (keycol2 != null && keycol2.length() > 0) {
                    extendedFrom.append(" AND ").append(innertableid).append(".keyid2 = ").append(outertableid).append(".").append(keycol2);
                }
                if (keycol3 != null && keycol3.length() > 0) {
                    extendedFrom.append(" AND ").append(innertableid).append(".keyid3 = ").append(outertableid).append(".").append(keycol3);
                }
            }
        }
        return rc;
    }

    private boolean getColumnFkJoin(String columnid, String keycol1, String keycol2, String keycol3, String innertableid, String outertableid, StringBuffer extendedSelect, StringBuffer extendedFrom) {
        boolean rc = false;
        if (columnid.startsWith(keycol1 + ".")) {
            boolean hasnoalias = RequestParser.parseAlias(columnid).equals(columnid);
            if (hasnoalias) {
                extendedSelect.append(", ").append(outertableid).append(".").append(columnid.substring(keycol1.length() + 1)).append(" \"").append(columnid).append("\"");
            } else {
                extendedSelect.append(", ").append(outertableid).append(".").append(columnid.substring(keycol1.length() + 1));
            }
            if (extendedFrom.indexOf(" LEFT OUTER JOIN " + outertableid + " ") == -1) {
                extendedFrom.append(" LEFT OUTER JOIN ").append(outertableid).append(" ");
                extendedFrom.append(" ON ").append(innertableid).append(".").append(keycol1).append(" = ").append(outertableid).append(".").append(keycol1);
                if (keycol2 != null && keycol2.length() > 0) {
                    extendedFrom.append(" AND ").append(innertableid).append(".").append(keycol2).append(" = ").append(outertableid).append(".").append(keycol2);
                }
                if (keycol3 != null && keycol3.length() > 0) {
                    extendedFrom.append(" AND ").append(innertableid).append(".").append(keycol3).append(" = ").append(outertableid).append(".").append(keycol3);
                }
            }
            rc = true;
        }
        return rc;
    }

    private String getUserColSelect(String[] cols, String tableid, PropertyListCollection sdcCols, DDTService ddtService, String sdcid) throws ServiceException {
        StringBuffer select = new StringBuffer();
        block0: for (int usercol = 0; usercol < cols.length; ++usercol) {
            if (RequestParser.isSelect(cols[usercol])) {
                select.append(", ");
                String alias = RequestParser.parseAlias(cols[usercol]);
                if (!alias.equalsIgnoreCase(cols[usercol]) && (alias.startsWith("_") || alias.indexOf(".") > -1)) {
                    select.append(RequestParser.parseColumn(cols[usercol])).append(" \"").append(alias).append("\"");
                } else {
                    select.append(cols[usercol]);
                }
                select.append(" ");
                continue;
            }
            if (RequestParser.isColidWithAlias(cols[usercol])) {
                select.append(", ");
                select.append(tableid).append(".").append(cols[usercol]);
                select.append(" ");
                continue;
            }
            if (cols[usercol].indexOf("'") < 0 && cols[usercol].indexOf("(") < 0 && cols[usercol].indexOf("||") <= 0 && cols[usercol].indexOf("+") <= 0 && cols[usercol].indexOf("-") <= 0 && cols[usercol].indexOf("*") <= 0 && cols[usercol].indexOf("/") <= 0) continue;
            if (sdcCols == null) {
                PropertyList sdcProps = ddtService.getSDCProperties(sdcid);
                sdcCols = sdcProps.getCollection("columns");
            }
            if (sdcCols != null) {
                for (int col = 0; col < sdcCols.size(); ++col) {
                    String tempcolid = sdcCols.getPropertyList(col).getProperty("columnid");
                    if (cols[usercol].indexOf(tempcolid + ".") > 0) continue block0;
                    if (cols[usercol].indexOf(tempcolid) < 0) continue;
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], " " + tempcolid, " " + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "(" + tempcolid, "(" + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "," + tempcolid, "," + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "|" + tempcolid, "|" + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "+" + tempcolid, "+" + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "-" + tempcolid, "-" + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "*" + tempcolid, "*" + tableid + "." + tempcolid, false);
                    cols[usercol] = StringUtil.replaceAll(cols[usercol], "/" + tempcolid, "/" + tableid + "." + tempcolid, false);
                }
            }
            select.append(", ");
            select.append(cols[usercol]);
            select.append(" ");
        }
        String returnselect = select.toString();
        if (returnselect.indexOf(tableid + "." + tableid + ".") >= 0) {
            returnselect = StringUtil.replaceAll(returnselect, tableid + "." + tableid + ".", tableid + ".", false);
        }
        return returnselect;
    }

    private void resetSqlBuffers(StringBuffer extendedSelect, StringBuffer extendedFrom, StringBuffer extendedWhere) {
        extendedSelect.delete(0, extendedSelect.length());
        extendedFrom.delete(0, extendedFrom.length());
        extendedWhere.delete(0, extendedWhere.length());
    }

    private DataSet filterByWISecurity(DataSet sdiworkitemDs, DDTService ddtService) throws ServiceException {
        boolean hasWISecurity;
        if (sdiworkitemDs != null && sdiworkitemDs.getRowCount() > 0 && (hasWISecurity = "D".equals(ddtService.getSDCProperties("WorkItem").getProperty("accesscontrolledflag")))) {
            this.logInfo("Filter by WORKITEM departmental security");
            HashSet<String> set = new HashSet<String>();
            StringBuffer workitemids = new StringBuffer();
            StringBuffer workitemversionids = new StringBuffer();
            boolean hasCurrentVersion = false;
            for (int i = 0; i < sdiworkitemDs.getRowCount(); ++i) {
                if (sdiworkitemDs.getValue(i, "workitemversionid").length() > 0) {
                    String idversion = sdiworkitemDs.getValue(i, "workitemid") + ";" + sdiworkitemDs.getValue(i, "workitemversionid");
                    if (set.contains(idversion)) continue;
                    set.add(idversion);
                    workitemids.append(";" + sdiworkitemDs.getValue(i, "workitemid"));
                    workitemversionids.append(";" + sdiworkitemDs.getValue(i, "workitemversionid"));
                    continue;
                }
                hasCurrentVersion = true;
            }
            String filteredkeyid1 = "";
            String filteredkeyid2 = "";
            if (workitemids.length() > 0) {
                SDI workitemSDI = new SDI("WorkItem", workitemids.substring(1), workitemversionids.substring(1), null);
                DataAccessService das = new DataAccessService(this.sapphireConnection);
                das.checkSDIAccess(workitemSDI, true);
                filteredkeyid1 = workitemSDI.getKeyid1();
                filteredkeyid2 = workitemSDI.getKeyid2();
            }
            if (!(hasCurrentVersion || filteredkeyid1 != null && filteredkeyid1.length() != 0)) {
                sdiworkitemDs.clear();
            } else if (workitemids.length() - 1 > filteredkeyid1.length()) {
                String[] filteredworkitemids = StringUtil.split(filteredkeyid1, ";");
                String[] filteredworkitemversionids = StringUtil.split(filteredkeyid2, ";");
                set.clear();
                for (int i = 0; i < filteredworkitemids.length; ++i) {
                    set.add(filteredworkitemids[i] + ";" + filteredworkitemversionids[i]);
                }
                ArrayList removeList = new ArrayList();
                for (int i = 0; i < sdiworkitemDs.getRowCount(); ++i) {
                    if (sdiworkitemDs.getValue(i, "workitemversionid").length() <= 0 || set.contains(sdiworkitemDs.getValue(i, "workitemid") + ";" + sdiworkitemDs.getValue(i, "workitemversionid"))) continue;
                    removeList.add(sdiworkitemDs.get(i));
                }
                sdiworkitemDs.removeAll(removeList);
            }
        }
        return sdiworkitemDs;
    }

    private String logParams(Object[] params) {
        StringBuilder sb = new StringBuilder(" params: [");
        if (params != null) {
            for (int i = 0; i < params.length; ++i) {
                if (i != 0) {
                    sb.append(",");
                }
                if (params[i] instanceof Calendar) {
                    sb.append(DateFormat.getDateTimeInstance().format(((Calendar)params[i]).getTime()));
                    continue;
                }
                sb.append(params[i]);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

