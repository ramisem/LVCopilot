/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.output.FileWriterWithEncoding
 *  org.hibernate.QueryException
 *  org.hibernate.ScrollableResults
 *  org.hibernate.Session
 *  org.hibernate.exception.DataException
 *  org.hibernate.exception.GenericJDBCException
 *  org.hibernate.exception.SQLGrammarException
 *  org.hibernate.query.Query
 *  org.hibernate.type.Type
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocArgument;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteria;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArg;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArgGroup;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryRequest;
import com.labvantage.sapphire.modules.adhocbrowser.AttributeHqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.DataEntryHqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTable;
import com.labvantage.sapphire.modules.adhocbrowser.DocumentFieldHqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.HqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.OrderByArg;
import com.labvantage.sapphire.modules.adhocbrowser.SDCTable;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import com.labvantage.sapphire.modules.adhocbrowser.SearchableColumn;
import com.labvantage.sapphire.modules.adhocbrowser.WorksheetItemFieldHqlBuilder;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.exception.DataException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.query.Query;
import org.hibernate.type.Type;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdhocQueryService
extends BaseService {
    private SapphireHibernateUtil shu;
    private HashMap aliasMap = new HashMap();
    private HashMap aliasTableMap = new HashMap();
    private ArrayList<String> joinsList = new ArrayList();
    private HashMap<String, String> joinsMap = new HashMap();
    private ArrayList<String> joinsCondition = new ArrayList();
    private AdhocQueryRequest adhocRequest;
    private AdhocMetaData adhocmetadata;
    private HqlBuilder hqlBuilder;
    private DDTService ddtService;
    private HashMap detailViewMap = new HashMap();
    private HashMap detailCrMap = new HashMap();
    private HashMap extendedColViewMap = new HashMap();
    private String tableid = null;
    private String sdcid = null;
    private String keycolid1 = null;
    private String keycolid2 = null;
    private String keycolid3 = null;
    private String desccol = null;
    private PropertyList sdcPropertyList = null;
    private ArrayList selectList = null;
    private String whereclause = null;
    private String fromclause = null;
    private String restrictiveWhere = "";
    private DataEntryHqlBuilder dataEntry;
    private DocumentFieldHqlBuilder documentField;
    private WorksheetItemFieldHqlBuilder worksheetItemField;
    private AttributeHqlBuilder sdiattribute;
    private String errormessage = null;
    private final ArrayList<String> resultColumnsList = new ArrayList();
    int rowsProcessed = 0;

    public AdhocQueryService(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
        this.shu = SapphireHibernateUtil.getInstance(sapphireConnection);
        this.adhocmetadata = this.shu.getAdhocMetaData();
        this.hqlBuilder = new HqlBuilder(sapphireConnection);
        this.logName = "AdhocQueryService";
    }

    private void init(AdhocQueryRequest adhocRequest) {
        if (this.adhocRequest == null) {
            this.adhocRequest = adhocRequest;
            this.sdcid = adhocRequest.getSdcid();
            this.ddtService = new DDTService(this.sapphireConnection);
            try {
                this.sdcPropertyList = this.ddtService.getSDCProperties(this.sdcid);
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            this.tableid = this.adhocmetadata.getTableid(this.sdcid);
            this.keycolid1 = this.sdcPropertyList.getProperty("keycolid1");
            this.keycolid2 = this.sdcPropertyList.getProperty("keycolid2");
            this.keycolid3 = this.sdcPropertyList.getProperty("keycolid3");
            this.desccol = this.sdcPropertyList.getProperty("desccol");
            this.restrictiveWhere = adhocRequest.getRestrictiveWhere();
        }
    }

    public DataSet getResultDataSet(AdhocQueryRequest adhocRequest) throws ServiceException {
        String message;
        String sql;
        this.init(adhocRequest);
        long starttime = System.currentTimeMillis();
        int maxresults = adhocRequest.getMaxResults();
        String searchWithinRset = adhocRequest.getSearchWithinRset();
        String searchWithinKeyid1 = adhocRequest.getSearchWithinKeyid1();
        String searchWithinKeyid2 = adhocRequest.getSearchWithinKeyid2();
        String searchWithinKeyid3 = adhocRequest.getSearchWithinKeyid3();
        DataSet ds = null;
        RSet rset = null;
        DataAccessService das = null;
        try {
            if (searchWithinRset != null && searchWithinRset.length() > 0) {
                rset = new RSet(searchWithinRset);
            } else if (searchWithinKeyid1 != null && searchWithinKeyid1.length() > 0) {
                das = new DataAccessService(this.sapphireConnection);
                rset = das.createRSet(this.sdcid, searchWithinKeyid1, searchWithinKeyid2, searchWithinKeyid3);
            }
            String sHQL = this.buildHQLQuery(adhocRequest, rset);
            Session dynamicSession = this.shu.currentSession();
            Query q = dynamicSession.createQuery(sHQL);
            int timeout = adhocRequest.getQueryTimeout() >= 0 ? adhocRequest.getQueryTimeout() : DBUtil.getDefaultQueryTimeout(this.sapphireConnection.getDatabaseId());
            q.setTimeout(timeout);
            this.hqlBuilder.setNamedParameters(q);
            this.logInfo("###!!Done create query" + (System.currentTimeMillis() - starttime) + "ms");
            if (!adhocRequest.isRequestCount()) {
                q.setMaxResults(maxresults);
            }
            List samples = q.list();
            this.logInfo("###!!Done query" + (System.currentTimeMillis() - starttime) + "ms");
            if (adhocRequest.isRequestCount()) {
                DataSet countds = new DataSet();
                String countstr = samples.get(0).toString();
                countds.addColumnValues("count", 1, countstr, ";");
                DataSet dataSet = countds;
                return dataSet;
            }
            Type[] columntypes = q.getReturnTypes();
            ds = this.processListResult(samples, columntypes, dynamicSession);
            this.shu.closeSession();
            Trace.log("#####!!!Done convert to dataset" + (System.currentTimeMillis() - starttime) + "ms");
        }
        catch (SQLGrammarException sqle) {
            sql = sqle.getSQL();
            message = sqle.getMessage();
            SQLException e = sqle.getSQLException();
            this.errormessage = message + ":" + e.getMessage() + " SQL:" + sql;
            this.logError(this.errormessage, sqle);
            throw new ServiceException(sqle);
        }
        catch (QueryException qe) {
            sql = qe.getQueryString();
            message = qe.getMessage();
            this.errormessage = message + ":" + qe.getMessage() + " SQL:" + sql;
            this.logError(this.errormessage, qe);
            throw new ServiceException(qe);
        }
        catch (DataException de) {
            sql = de.getSQL();
            message = de.getMessage();
            SQLException e = de.getSQLException();
            this.errormessage = message + ":" + e.getMessage() + " SQL:" + sql;
            this.logError(this.errormessage, de);
            throw new ServiceException(de);
        }
        catch (GenericJDBCException jdbcException) {
            SQLException e = jdbcException.getSQLException();
            this.errormessage = e.getMessage();
            Trace.logError(this.errormessage, jdbcException);
            throw new ServiceException(e);
        }
        catch (Throwable t) {
            this.errormessage = t.getMessage();
            Trace.logError(this.errormessage, t);
            throw new ServiceException(t);
        }
        finally {
            if (this.shu != null) {
                this.shu.closeSession();
            }
            if (rset != null) {
                if (das == null) {
                    das = new DataAccessService(this.sapphireConnection);
                }
                das.clearRSet(rset);
            }
        }
        return ds;
    }

    public File getResultFile(AdhocQueryRequest adhocRequest) throws ServiceException {
        File rf = null;
        FileWriterWithEncoding fos = null;
        try {
            PropertyList columnDef;
            int i;
            String[] resultColumns;
            DataSet ds;
            this.init(adhocRequest);
            int maxresults = adhocRequest.getMaxResults();
            String sHQL = this.buildHQLQuery(adhocRequest);
            Session dynamicSession = this.shu.currentSession();
            Query q = dynamicSession.createQuery(sHQL);
            int timeout = adhocRequest.getQueryTimeout() >= 0 ? adhocRequest.getQueryTimeout() : DBUtil.getDefaultQueryTimeout(this.sapphireConnection.getDatabaseId());
            q.setTimeout(timeout);
            q.setMaxResults(maxresults);
            this.hqlBuilder.setNamedParameters(q);
            this.logInfo("###!!Done create query");
            long starttime = System.currentTimeMillis();
            ScrollableResults rs = q.scroll();
            this.logInfo("###!!Done getting ScrollableResults" + (System.currentTimeMillis() - starttime) + "ms");
            Type[] returnTypes = q.getReturnTypes();
            rf = File.createTempFile("adhocExportAll", ".csv");
            fos = new FileWriterWithEncoding(rf, "UTF-8");
            int row = 0;
            int processRows = 5000;
            ArrayList<Object[]> list = new ArrayList<Object[]>();
            String currentHeader = "";
            M18NUtil m18NUtil = new M18NUtil(this.sapphireConnection);
            PropertyListCollection sdccolumns = this.sdcPropertyList.getCollection("columns");
            while (rs.next()) {
                if (row > 0 && row % processRows == 0) {
                    ds = this.processListResult(list, returnTypes, null);
                    ds.setM18NUtil(m18NUtil);
                    resultColumns = ds.getColumns();
                    for (i = 0; i < resultColumns.length; ++i) {
                        if (this.resultColumnsList.contains(resultColumns[i])) continue;
                        this.resultColumnsList.add(resultColumns[i]);
                        columnDef = sdccolumns.getPropertyList(resultColumns[i]);
                        if (columnDef == null || !"Y".equals(columnDef.getProperty("timezoneindependent"))) continue;
                        ds.setTimeZoneInsensitive(resultColumns[i]);
                    }
                    currentHeader = this.writeDataSet(ds, (Writer)fos, currentHeader);
                    list.clear();
                }
                Object[] rows = rs.get();
                list.add(rows);
                ++row;
            }
            if (list.size() > 0) {
                ds = this.processListResult(list, returnTypes, null);
                ds.setM18NUtil(m18NUtil);
                resultColumns = ds.getColumns();
                for (i = 0; i < resultColumns.length; ++i) {
                    if (this.resultColumnsList.contains(resultColumns[i])) continue;
                    this.resultColumnsList.add(resultColumns[i]);
                    columnDef = sdccolumns.getPropertyList(resultColumns[i]);
                    if (columnDef == null || !"Y".equals(columnDef.getProperty("timezoneindependent"))) continue;
                    ds.setTimeZoneInsensitive(resultColumns[i]);
                }
                currentHeader = this.writeDataSet(ds, (Writer)fos, currentHeader);
            }
            fos.close();
            this.rowsProcessed = row;
            this.logInfo("###!!Done Generating CSV file for " + row + " in " + (System.currentTimeMillis() - starttime) + "ms");
        }
        catch (GenericJDBCException jdbcException) {
            SQLException e = jdbcException.getSQLException();
            this.errormessage = e.getMessage();
            Trace.logError(this.errormessage, jdbcException);
            throw new ServiceException(e);
        }
        catch (Exception e) {
            this.errormessage = e.getMessage();
            throw new ServiceException(e);
        }
        finally {
            if (this.shu != null) {
                this.shu.closeSession();
            }
            try {
                fos.close();
            }
            catch (Exception exception) {}
        }
        return rf;
    }

    private DataSet processListResult(List samples, Type[] columntypes, Session dynamicSession) throws ServiceException {
        boolean newSession = false;
        DataSet ds = this.getDataSetFromListofArray(samples, this.selectList, columntypes);
        try {
            if (ds.getRowCount() > 0) {
                String fieldid;
                String keyid1;
                DataSet docfieldds;
                Query q;
                StringBuffer wherebuffer;
                StringBuffer frombuffer;
                StringBuffer selectbuffer;
                DataAccessService das;
                String[] keyidlists;
                String columnid;
                ArrayList<String> selectList;
                ArrayList viewItemList;
                String rsetid;
                RSet rset;
                String accesscontrolledflag = this.sdcPropertyList.getProperty("accesscontrolledflag");
                if ("P".equals(accesscontrolledflag) || "B".equals(accesscontrolledflag) || "SDIWorkItem".equals(this.sdcid)) {
                    StringBuffer keyid1list = new StringBuffer();
                    StringBuffer keyid2list = new StringBuffer();
                    StringBuffer keyid3list = new StringBuffer();
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setPropsMatch(true);
                    sdiRequest.setSDCid(this.sdcid);
                    String keycount = this.sdcPropertyList.getProperty("keycolumns");
                    for (int i = 0; i < ds.getRowCount(); ++i) {
                        keyid1list.append(";" + ds.getString(i, this.keycolid1));
                        if ("2".equals(keycount) || "3".equals(keycount)) {
                            keyid2list.append(";" + ds.getString(i, this.keycolid2));
                        }
                        if (!"3".equals(keycount)) continue;
                        keyid3list.append(";" + ds.getString(i, this.keycolid3));
                    }
                    StringBuilder requestitem = new StringBuilder("primary[");
                    requestitem.append(this.keycolid1);
                    if ("2".equals(keycount) || "3".equals(keycount)) {
                        requestitem.append("," + this.keycolid2);
                    }
                    if ("3".equals(keycount)) {
                        requestitem.append("," + this.keycolid3);
                    }
                    if ("SDIWorkItem".equals(this.sdcid)) {
                        requestitem.append(",workitemid, workitemversionid");
                    }
                    if ("DataSet".equals(this.sdcid)) {
                        sdiRequest.setSecurityBypassCode("D".equals(this.sdcPropertyList.getProperty("accesscontrolledflag")) ? 2 : 0);
                    }
                    requestitem.append("]");
                    sdiRequest.setRequestItem(requestitem.toString());
                    sdiRequest.setKeyid1List(keyid1list.substring(1));
                    sdiRequest.setKeyid2List(keyid2list.length() > 0 ? keyid2list.substring(1) : "");
                    sdiRequest.setKeyid3List(keyid3list.length() > 0 ? keyid3list.substring(1) : "");
                    sdiRequest.setRetrieve(true);
                    DataSet rsetDs = new QueryService(this.sapphireConnection).getSDIData(sdiRequest).getDataset("primary");
                    DataSet filteredDs = null;
                    for (int i = 0; i < rsetDs.getRowCount(); ++i) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put(this.keycolid1, rsetDs.getString(i, this.keycolid1));
                        if ("2".equals(this.sdcPropertyList.getProperty("keycolumns")) || "3".equals(this.sdcPropertyList.getProperty("keycolumns"))) {
                            filter.put(this.keycolid2, rsetDs.getString(i, this.keycolid2));
                        }
                        if ("3".equals(this.sdcPropertyList.getProperty("keycolumns"))) {
                            filter.put(this.keycolid3, rsetDs.getString(i, this.keycolid3));
                        }
                        if (filteredDs == null) {
                            filteredDs = ds.getFilteredDataSet(filter);
                            continue;
                        }
                        filteredDs.addAll(ds.getFilteredDataSet(filter));
                    }
                    if (filteredDs == null) {
                        ds.removeAll(ds);
                    } else {
                        ds = filteredDs;
                    }
                }
                if (this.detailViewMap.get("sdidataitem") != null && ((ArrayList)this.detailViewMap.get("sdidataitem")).size() > 0) {
                    DataAccessService das2 = new DataAccessService(this.sapphireConnection);
                    this.logInfo("#!!CreateRSetDSNP");
                    String[] keyidlists2 = this.getKeyidLists(ds);
                    rset = das2.createRSetDSNP(this.sdcid, keyidlists2[0], keyidlists2[1], keyidlists2[2], "", "", "", "", false, false, false);
                    rsetid = rset.getRsetid();
                    viewItemList = (ArrayList)this.detailViewMap.get("sdidataitem");
                    ds.setColidCaseSensitive(true);
                    for (int i = 0; i < viewItemList.size(); ++i) {
                        selectList = new ArrayList();
                        selectList.add("keyid1");
                        if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                            selectList.add("keyid2");
                        }
                        if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                            selectList.add("keyid3");
                        }
                        selectList.add("paramlistid");
                        selectList.add("paramlistversionid");
                        selectList.add("variantid");
                        selectList.add("dataset");
                        selectList.add("paramid");
                        selectList.add("paramtype");
                        selectList.add("replicateid");
                        columnid = (String)viewItemList.get(i);
                        boolean isRootDataEntry = columnid.indexOf("sdidataitem[") == 0;
                        String fromrsetitemds = ", rsetitemsds rsetitemsds ";
                        if (isRootDataEntry && this.fromclause.indexOf(fromrsetitemds) < 0) {
                            this.fromclause = this.fromclause + fromrsetitemds;
                        }
                        String valuecolumnid = "displayvalue";
                        selectList.add(valuecolumnid);
                        StringBuffer selectbuffer2 = new StringBuffer("select distinct ");
                        StringBuffer frombuffer2 = new StringBuffer(this.fromclause);
                        StringBuffer wherebuffer2 = new StringBuffer(this.whereclause);
                        selectbuffer2.append(this.getKeyColumnSelect(true, selectList));
                        if (this.dataEntry == null) {
                            this.dataEntry = new DataEntryHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        int joinNo = i + 100;
                        if (this.detailCrMap.get(columnid) != null) {
                            joinNo = (Integer)this.detailCrMap.get(columnid);
                        } else {
                            frombuffer2.append(this.dataEntry.getDataItemViewJoin(this.tableid, columnid, joinNo));
                        }
                        String dataitemViewJoinWhere = this.dataEntry.getDataItemViewJoinWhere(this.tableid, columnid, joinNo, isRootDataEntry, this.joinsList.contains("sdidata"), this.joinsList.contains("sdidataitem"));
                        if (wherebuffer2.length() == 0) {
                            wherebuffer2.append(" where " + dataitemViewJoinWhere);
                        } else {
                            wherebuffer2.insert(wherebuffer2.indexOf("where") + 5, " (").append(")").append(" and " + dataitemViewJoinWhere);
                        }
                        selectbuffer2.append("," + this.dataEntry.getDataItemViewSelect(joinNo, valuecolumnid));
                        if (isRootDataEntry) {
                            if (wherebuffer2.length() == 0) {
                                wherebuffer2.append(" where (");
                            } else {
                                wherebuffer2.append(" and (");
                            }
                            wherebuffer2.append(" rsetitemsds.rsetid='" + rsetid + "' and rsetitemsds.sdcid='" + this.sdcid + "' and rsetitemsds.keyid1=" + this.tableid + "." + this.keycolid1);
                            if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                                wherebuffer2.append(" and rsetitemsds.keyid2=" + this.tableid + "." + this.keycolid2);
                                if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                                    wherebuffer2.append(" and rsetitemsds.keyid3=" + this.tableid + "." + this.keycolid3);
                                }
                            }
                            wherebuffer2.append(" )");
                        }
                        String dihql = selectbuffer2.toString() + frombuffer2.toString() + wherebuffer2.toString();
                        this.logInfo("#!!DataItem View HQL:" + dihql);
                        if (dynamicSession == null) {
                            dynamicSession = this.shu.openSession();
                            newSession = true;
                        }
                        Query q2 = dynamicSession.createQuery(dihql);
                        this.hqlBuilder.setNamedParameters(q2);
                        List dataitemList = q2.list();
                        this.logInfo("#!!Done");
                        columntypes = q2.getReturnTypes();
                        DataSet dataitemds = this.getDataSetFromListofArray(dataitemList, selectList, columntypes);
                        for (int r = 0; r < ds.getRowCount(); ++r) {
                            String keyid12 = ds.getValue(r, this.keycolid1);
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("keyid1", keyid12);
                            DataSet dataitemForSDI = dataitemds.getFilteredDataSet(filter);
                            for (int di = 0; di < dataitemForSDI.getRowCount(); ++di) {
                                String dataitemKey = dataitemForSDI.getValue(di, "paramlistid") + ";" + dataitemForSDI.getValue(di, "paramlistversionid") + ";" + dataitemForSDI.getValue(di, "variantid") + ";" + dataitemForSDI.getValue(di, "dataset") + ";" + dataitemForSDI.getValue(di, "paramid") + ";" + dataitemForSDI.getValue(di, "paramtype") + ";" + dataitemForSDI.getValue(di, "replicateid");
                                if (!this.isColumnIdMatchDataItemKey(columnid, dataitemKey)) continue;
                                String dataitemValue = dataitemForSDI.getValue(di, valuecolumnid);
                                String dataitemColid = columnid + ";" + dataitemKey;
                                ds.addColumn(dataitemColid, 0);
                                ds.setValue(r, dataitemColid, dataitemValue);
                            }
                        }
                    }
                    das2.clearRSet(rset);
                }
                if (this.detailViewMap.get("documentfield") != null && ((ArrayList)this.detailViewMap.get("documentfield")).size() > 0) {
                    keyidlists = this.getKeyidLists(ds);
                    das = new DataAccessService(this.sapphireConnection);
                    rset = das.createRSet(this.sdcid, keyidlists[0], keyidlists[1], keyidlists[2]);
                    rsetid = rset.getRsetid();
                    viewItemList = (ArrayList)this.detailViewMap.get("documentfield");
                    ds.setColidCaseSensitive(true);
                    for (int i = 0; i < viewItemList.size(); ++i) {
                        selectList = new ArrayList<String>();
                        selectList.add("keyid1");
                        if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                            selectList.add("keyid2");
                        }
                        if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                            selectList.add("keyid3");
                        }
                        selectList.add("formid");
                        selectList.add("documentid");
                        selectList.add("documentversionid");
                        selectList.add("fieldid");
                        selectList.add("fieldinstance");
                        columnid = (String)viewItemList.get(i);
                        String valuecolumnid = "enteredtext";
                        selectList.add(valuecolumnid);
                        selectbuffer = new StringBuffer("select distinct ");
                        frombuffer = new StringBuffer(this.fromclause);
                        wherebuffer = new StringBuffer(this.whereclause);
                        selectbuffer.append(this.getKeyColumnSelect(true, selectList));
                        if (this.documentField == null) {
                            this.documentField = new DocumentFieldHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        int joinNo = i + 100;
                        if (this.detailCrMap.get(columnid) != null) {
                            joinNo = (Integer)this.detailCrMap.get(columnid);
                        } else {
                            frombuffer.append(this.documentField.getDocumentFieldViewJoin(this.tableid, columnid, joinNo, frombuffer.indexOf(".sdidocument as sdidocument") < 0));
                        }
                        String documentFieldViewJoinWhere = this.documentField.getDocumentFieldViewJoinWhere(this.tableid, columnid, joinNo);
                        if (wherebuffer.length() == 0) {
                            wherebuffer.append(" where " + documentFieldViewJoinWhere);
                        } else {
                            wherebuffer.insert(wherebuffer.indexOf("where") + 5, " (").append(")").append(" and " + documentFieldViewJoinWhere);
                        }
                        selectbuffer.append(",(select document.formid from document document where document.documentid=sdidocument.documentid and document.documentversionid=sdidocument.documentversionid) as formid");
                        selectbuffer.append("," + this.documentField.getDocumentFieldViewSelect(joinNo, valuecolumnid));
                        if (wherebuffer.length() == 0) {
                            wherebuffer.append(" where (" + this.tableid + "." + this.keycolid1 + " in (select keyid1 from rsetitems where rsetid='" + rsetid + "')) ");
                        } else {
                            wherebuffer.append(" and (" + this.tableid + "." + this.keycolid1 + " in (select keyid1 from rsetitems where rsetid='" + rsetid + "')) ");
                        }
                        String dihql = selectbuffer.toString() + frombuffer.toString() + wherebuffer.toString();
                        this.logInfo("#!!DocumentField View HQL:" + dihql);
                        if (dynamicSession == null) {
                            dynamicSession = this.shu.openSession();
                            newSession = true;
                        }
                        q = dynamicSession.createQuery(dihql);
                        this.hqlBuilder.setNamedParameters(q);
                        List documentfieldList = q.list();
                        this.logInfo("#!!Done");
                        columntypes = q.getReturnTypes();
                        docfieldds = this.getDataSetFromListofArray(documentfieldList, selectList, columntypes);
                        for (int r = 0; r < ds.getRowCount(); ++r) {
                            keyid1 = ds.getValue(r, this.keycolid1);
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("keyid1", keyid1);
                            DataSet documentfieldForSDI = docfieldds.getFilteredDataSet(filter);
                            for (int df = 0; df < documentfieldForSDI.getRowCount(); ++df) {
                                fieldid = documentfieldForSDI.getValue(df, "fieldid");
                                String formid = documentfieldForSDI.getValue(df, "formid");
                                String documentfieldKey = formid + ";" + fieldid;
                                String documentfieldValue = documentfieldForSDI.getValue(df, valuecolumnid);
                                String documentfieldColid = columnid + ";" + documentfieldKey;
                                if (df == 0) {
                                    ds.addColumn(documentfieldColid, 0);
                                    ds.setValue(r, documentfieldColid, documentfieldValue);
                                    continue;
                                }
                                if (df > 5) continue;
                                ds.setValue(r, documentfieldColid, ds.getValue(r, documentfieldColid) + "," + (df == 5 ? "..." : documentfieldValue));
                            }
                        }
                    }
                    das.clearRSet(rset);
                }
                if (this.detailViewMap.get("worksheetitemfield") != null && ((ArrayList)this.detailViewMap.get("worksheetitemfield")).size() > 0) {
                    keyidlists = this.getKeyidLists(ds);
                    das = new DataAccessService(this.sapphireConnection);
                    rset = das.createRSet(this.sdcid, keyidlists[0], keyidlists[1], keyidlists[2]);
                    rsetid = rset.getRsetid();
                    viewItemList = (ArrayList)this.detailViewMap.get("worksheetitemfield");
                    ds.setColidCaseSensitive(true);
                    for (int i = 0; i < viewItemList.size(); ++i) {
                        selectList = new ArrayList();
                        columnid = (String)viewItemList.get(i);
                        String valuecolumnid = "enteredtext";
                        selectbuffer = new StringBuffer("select distinct ");
                        frombuffer = new StringBuffer(this.fromclause);
                        wherebuffer = new StringBuffer(this.whereclause);
                        selectbuffer.append(this.getKeyColumnSelect(true, selectList));
                        selectList.add("worksheetitemid");
                        selectList.add("worksheetitemversionid");
                        selectList.add("fieldname");
                        selectList.add("fieldtitle");
                        selectList.add("datatype");
                        selectList.add(valuecolumnid);
                        if (this.worksheetItemField == null) {
                            this.worksheetItemField = new WorksheetItemFieldHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        int joinNo = i + 100;
                        if (this.detailCrMap.get(columnid) != null) {
                            joinNo = (Integer)this.detailCrMap.get(columnid);
                        } else {
                            frombuffer.append(this.worksheetItemField.getWorksheetItemFieldViewJoin(this.tableid, columnid, joinNo, frombuffer.indexOf(".worksheetitem_worksheetid as worksheetitem") < 0));
                        }
                        String worksheetItemFieldViewJoinWhere = this.worksheetItemField.getWorksheetItemFieldViewJoinWhere(this.tableid, columnid, joinNo);
                        if (wherebuffer.length() == 0) {
                            wherebuffer.append(" where " + worksheetItemFieldViewJoinWhere);
                        } else {
                            wherebuffer.insert(wherebuffer.indexOf("where") + 5, " (").append(")").append(" and " + worksheetItemFieldViewJoinWhere);
                        }
                        selectbuffer.append("," + this.worksheetItemField.getWorksheetItemFieldViewSelect(joinNo, valuecolumnid));
                        if (wherebuffer.length() == 0) {
                            wherebuffer.append(" where (" + this.tableid + "." + this.keycolid1 + " in (select keyid1 from rsetitems where rsetid='" + rsetid + "')) ");
                        } else {
                            wherebuffer.append(" and (" + this.tableid + "." + this.keycolid1 + " in (select keyid1 from rsetitems where rsetid='" + rsetid + "')) ");
                        }
                        String dihql = selectbuffer.toString() + frombuffer.toString() + wherebuffer.toString();
                        this.logInfo("#!!WorksheetItemField View HQL:" + dihql);
                        if (dynamicSession == null) {
                            dynamicSession = this.shu.openSession();
                            newSession = true;
                        }
                        q = dynamicSession.createQuery(dihql);
                        this.hqlBuilder.setNamedParameters(q);
                        List list = q.list();
                        this.logInfo("#!!Done");
                        columntypes = q.getReturnTypes();
                        DataSet itemfieldds = this.getDataSetFromListofArray(list, selectList, columntypes);
                        for (int r = 0; r < ds.getRowCount(); ++r) {
                            keyid1 = ds.getValue(r, this.keycolid1);
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("worksheetid", keyid1);
                            DataSet itemfieldForSDI = itemfieldds.getFilteredDataSet(filter);
                            for (int df = 0; df < itemfieldForSDI.getRowCount(); ++df) {
                                fieldid = itemfieldForSDI.getValue(df, "fieldname");
                                String worksheetitemid = itemfieldForSDI.getValue(df, "worksheetitemid");
                                String itemfieldKey = fieldid;
                                String itemfieldValue = itemfieldForSDI.getValue(df, valuecolumnid);
                                String itemfieldColid = columnid + ";Field:;" + itemfieldKey;
                                if (df == 0) {
                                    ds.addColumn(itemfieldColid, 0);
                                    ds.setValue(r, itemfieldColid, itemfieldValue);
                                    continue;
                                }
                                if (df > 5) continue;
                                ds.setValue(r, itemfieldColid, ds.getValue(r, itemfieldColid) + "," + (df == 5 ? "..." : itemfieldValue));
                            }
                        }
                    }
                    das.clearRSet(rset);
                }
                if (this.detailViewMap.get("sdiattribute") != null && ((ArrayList)this.detailViewMap.get("sdiattribute")).size() > 0) {
                    keyidlists = this.getKeyidLists(ds);
                    das = new DataAccessService(this.sapphireConnection);
                    rset = das.createRSet(this.sdcid, keyidlists[0], keyidlists[1], keyidlists[2]);
                    rsetid = rset.getRsetid();
                    viewItemList = (ArrayList)this.detailViewMap.get("sdiattribute");
                    ds.setColidCaseSensitive(true);
                    for (int i = 0; i < viewItemList.size(); ++i) {
                        boolean isRootAttribute;
                        selectList = new ArrayList();
                        selectList.add("keyid1");
                        if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                            selectList.add("keyid2");
                        }
                        if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                            selectList.add("keyid3");
                        }
                        selectList.add("attributeid");
                        selectList.add("attributeinstance");
                        selectList.add("datatype");
                        selectList.add("textvalue");
                        selectList.add("numericvalue");
                        selectList.add("datevalue");
                        columnid = (String)viewItemList.get(i);
                        StringBuffer selectbuffer3 = new StringBuffer("select distinct ");
                        StringBuffer frombuffer3 = new StringBuffer(this.fromclause);
                        StringBuffer wherebuffer3 = new StringBuffer(this.whereclause);
                        if ("sdidataitem".equals(this.tableid)) {
                            selectbuffer3.append("sdidataitem.sdidataitemid");
                            selectList.add("sdidataitemid");
                        } else if ("sdidata".equals(this.tableid)) {
                            selectbuffer3.append("sdidata.sdidataid");
                            selectList.add("sdidataid");
                        } else {
                            selectbuffer3.append(this.getKeyColumnSelect(true, selectList));
                        }
                        if (this.sdiattribute == null) {
                            this.sdiattribute = new AttributeHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        int joinNo = i + 100;
                        if (this.detailCrMap.get(columnid) != null) {
                            joinNo = (Integer)this.detailCrMap.get(columnid);
                        } else {
                            frombuffer3.append(this.sdiattribute.getAttributeViewJoin(this.tableid, columnid, joinNo));
                        }
                        String attributeViewJoinWhere = this.sdiattribute.getAttributeViewJoinWhere(this.tableid, columnid, joinNo, this.ddtService);
                        if (wherebuffer3.length() == 0) {
                            wherebuffer3.append(" where " + attributeViewJoinWhere);
                        } else {
                            wherebuffer3.insert(wherebuffer3.indexOf("where") + 5, " (").append(")").append(" and " + attributeViewJoinWhere);
                        }
                        selectbuffer3.append("," + this.sdiattribute.getAttributeViewSelect(joinNo));
                        boolean bl = isRootAttribute = columnid.indexOf("sdiattribute[") == 0;
                        if (isRootAttribute) {
                            wherebuffer3.append((wherebuffer3.length() == 0 ? " where" : " and") + " (" + this.tableid + "." + this.keycolid1 + "=rsetitems.keyid1 " + (this.keycolid2.length() > 0 ? " and " + this.tableid + "." + this.keycolid2 + "=rsetitems.keyid2 " : "") + (this.keycolid3.length() > 0 ? " and " + this.tableid + "." + this.keycolid3 + "=rsetitems.keyid3 " : ""));
                            wherebuffer3.append(" and sdiattribute" + joinNo + ".keyid1=rsetitems.keyid1" + (this.keycolid2.length() > 0 ? " and sdiattribute" + joinNo + ".keyid2=rsetitems.keyid2 " : "") + (this.keycolid3.length() > 0 ? " and sdiattribute" + joinNo + ".keyid3=rsetitems.keyid3 " : ""));
                            wherebuffer3.append(" and rsetitems.rsetid='" + rsetid + "')");
                            if (frombuffer3.indexOf("rsetitems rsetitems") < 0) {
                                frombuffer3.append(",rsetitems rsetitems ");
                            }
                        }
                        String dihql = selectbuffer3.toString() + frombuffer3.toString() + wherebuffer3.toString();
                        this.logInfo("#!!Attribute View HQL:" + dihql);
                        if (dynamicSession == null) {
                            dynamicSession = this.shu.openSession();
                            newSession = true;
                        }
                        q = dynamicSession.createQuery(dihql);
                        this.hqlBuilder.setNamedParameters(q);
                        List attributeList = q.list();
                        this.logInfo("#!!Done");
                        columntypes = q.getReturnTypes();
                        docfieldds = this.getDataSetFromListofArray(attributeList, selectList, columntypes);
                        for (int r = 0; r < ds.getRowCount(); ++r) {
                            keyid1 = ds.getValue(r, this.keycolid1);
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("keyid1", keyid1);
                            if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                                filter.put("keyid2", ds.getValue(r, this.keycolid2));
                                if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                                    filter.put("keyid3", ds.getValue(r, this.keycolid3));
                                }
                            }
                            DataSet attributeForSDI = docfieldds.getFilteredDataSet(filter);
                            for (int df = 0; df < attributeForSDI.getRowCount(); ++df) {
                                String attributeid;
                                String attributeKey = attributeid = attributeForSDI.getValue(df, "attributeid");
                                String datatype = attributeForSDI.getValue(df, "datatype");
                                if ("O".equals(datatype)) {
                                    attributeForSDI.setTimeZoneInsensitive("datevalue");
                                    attributeForSDI.setDateDisplayFormat("datevalue", new M18NUtil(this.sapphireConnection).getDefaultDateOnlyFormat(false));
                                }
                                String attributeValue = "S".equals(datatype) ? attributeForSDI.getValue(df, "textvalue") : ("D".equals(datatype) || "O".equals(datatype) ? attributeForSDI.getValue(df, "datevalue") : ("N".equals(datatype) ? attributeForSDI.getValue(df, "numericvalue") : attributeForSDI.getValue(df, "textvalue")));
                                String attributeColid = columnid + ";" + attributeKey;
                                if (df == 0) {
                                    ds.addColumn(attributeColid, 0);
                                    ds.setValue(r, attributeColid, attributeValue);
                                    continue;
                                }
                                if (df > 5) continue;
                                ds.setValue(r, attributeColid, ds.getValue(r, attributeColid) + "," + (df == 5 ? "..." : attributeValue));
                            }
                        }
                    }
                    das.clearRSet(rset);
                }
            }
        }
        catch (Throwable t) {
            throw new ServiceException(t);
        }
        finally {
            if (newSession && dynamicSession != null) {
                dynamicSession.close();
            }
        }
        return ds;
    }

    private String[] getKeyidLists(DataSet ds) {
        StringBuffer keyid1list = new StringBuffer();
        StringBuffer keyid2list = new StringBuffer();
        StringBuffer keyid3list = new StringBuffer();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String keyid1 = ds.getString(i, this.keycolid1);
            keyid1list.append(";" + keyid1);
            if (this.keycolid2 == null || this.keycolid2.length() <= 0) continue;
            keyid2list.append(";" + ds.getString(i, this.keycolid2));
            if (this.keycolid3 == null || this.keycolid3.length() <= 0) continue;
            keyid3list.append(";" + ds.getString(i, this.keycolid3));
        }
        return new String[]{keyid1list.substring(1), keyid2list.length() > 1 ? keyid2list.substring(1) : "", keyid3list.length() > 1 ? keyid3list.substring(1) : ""};
    }

    private boolean isColumnIdMatchDataItemKey(String columnid, String keys) {
        String[] searchkeys = StringUtil.split(StringUtil.getTokens(columnid)[0], "|");
        String[] itemkeys = StringUtil.split(keys, ";");
        boolean ismatch = true;
        if (searchkeys.length == 5) {
            ismatch = searchkeys[0].equals(itemkeys[0]) && searchkeys[1].equals(itemkeys[1]) && searchkeys[2].equals(itemkeys[2]) && searchkeys[3].equals(itemkeys[4]) && searchkeys[4].equals(itemkeys[5]);
        } else if (searchkeys.length == 2) {
            ismatch = searchkeys[0].equals(itemkeys[4]) && searchkeys[1].equals(itemkeys[5]);
        }
        return ismatch;
    }

    private SearchableColumn getSearchableColumn(String tableid, String columnid) {
        SearchableColumn searchableCol = this.adhocmetadata.getSearchableColumn(tableid, columnid);
        if (searchableCol != null || columnid.indexOf(".") < 0) {
            return searchableCol;
        }
        String jointablename = this.getJoinTableName(columnid);
        String realcolid = columnid.substring(columnid.lastIndexOf(".") + 1);
        return this.getSearchableColumn(jointablename, realcolid);
    }

    public String buildHQLQuery(AdhocQueryRequest adhocQueryRequest) throws ServiceException {
        return this.buildHQLQuery(adhocQueryRequest, null);
    }

    public String buildHQLQuery(AdhocQueryRequest adhocQueryRequest, RSet rset) throws ServiceException {
        this.init(adhocQueryRequest);
        long starttime = System.currentTimeMillis();
        this.setAliasMap();
        AdhocCriteria adhocCriteria = adhocQueryRequest.getCriteria();
        String rsetitemtable = "rsetitems";
        StringBuffer frombuffer = new StringBuffer(" from " + this.tableid + " " + this.tableid);
        if (rset != null) {
            frombuffer.append(", rsetitems rsetitems ");
        }
        StringBuffer wherebuffer = new StringBuffer("");
        int groupcount = adhocCriteria.size();
        String betweenGroupBoolean = adhocQueryRequest.getBetweenGroupBoolean();
        ArrayList<StringBuffer> groupwhereList = new ArrayList<StringBuffer>();
        int dataentryjoinNo = 1;
        int dataentryjoinIndex = 1;
        int documentfieldjoinNo = 1;
        int worksheetitemfieldjoinNo = 1;
        int attributejoinNo = 1;
        for (int group = 0; group < groupcount; ++group) {
            boolean isNamedGroup;
            int reverseFKdetailjoinNo = group;
            AdhocCriteriaArgGroup adhocGroup = (AdhocCriteriaArgGroup)adhocCriteria.get(group);
            String groupName = adhocGroup.getGroupName();
            boolean bl = isNamedGroup = groupName != null && !"|%|".equals(groupName) && groupName.length() > 0;
            if (adhocGroup.size() <= 0) continue;
            StringBuffer groupwhere = new StringBuffer();
            String groupBoolean = adhocGroup.getCriteriaRelation();
            HashMap<String, Integer> desdcidJoinNoMap = new HashMap<String, Integer>();
            for (int col = 0; col < adhocGroup.size(); ++col) {
                String joinStr;
                SearchableColumn searchableCol;
                String dataentryWhere = "";
                String documentfieldWhere = "";
                String worksheetitemfieldWhere = "";
                String sdiattributeWhere = "";
                AdhocCriteriaArg adhocArg = (AdhocCriteriaArg)adhocGroup.get(col);
                if (!adhocArg.isValidCriteria()) continue;
                String columnid = adhocArg.getColumnid();
                String columndefinition = "";
                String columntype = "string";
                boolean isExtendedCol = false;
                boolean isExtendedColOnLinkedTable = false;
                if (adhocArg.getColumntype() != null && adhocArg.getColumntype().length() > 0) {
                    columntype = adhocArg.getColumntype();
                }
                if ((searchableCol = this.adhocmetadata.getSearchableColumn(this.tableid, columnid)) != null) {
                    columntype = searchableCol.getHibernateType();
                    if (searchableCol.getColumndefinition() != null) {
                        columndefinition = searchableCol.getColumndefinition();
                        isExtendedCol = true;
                    }
                }
                String operator = adhocArg.getOperator();
                String value = (String)adhocArg.getValueObject();
                String jointablename = "";
                if (!isExtendedCol && columnid.indexOf(".") > 0 || columnid.indexOf("sdidataitem[") >= 0 || columnid.indexOf("documentfield[") >= 0 || columnid.indexOf("worksheetitemfield[") >= 0 || columnid.indexOf("sdiattribute[") >= 0) {
                    String actualdetableid;
                    String prefix3;
                    String desdcid;
                    String detableid;
                    if (columnid.indexOf("sdidataitem[") >= 0) {
                        jointablename = "sdidataitem";
                    } else if (columnid.indexOf("documentfield[") >= 0) {
                        jointablename = "documentfield";
                    } else if (columnid.indexOf("worksheetitemfield[") >= 0) {
                        jointablename = "worksheetitemfield";
                    } else if (columnid.indexOf("sdiattribute[") >= 0) {
                        jointablename = "sdiattribute";
                    } else {
                        SearchableColumn sCol;
                        String coldef;
                        jointablename = this.getJoinTableName(columnid);
                        String realcolid = columnid.substring(columnid.lastIndexOf(".") + 1);
                        if (this.adhocmetadata.getSdcId(jointablename) == null) {
                            columntype = "string";
                        } else if (this.adhocmetadata.getSearchableColumn(jointablename, realcolid) != null && (coldef = (sCol = this.adhocmetadata.getSearchableColumn(jointablename, realcolid)).getColumndefinition()) != null && coldef.length() > 0) {
                            isExtendedCol = true;
                            isExtendedColOnLinkedTable = true;
                            columndefinition = coldef;
                            if (AdhocQueryService.isDotSyntaxColumn(coldef)) {
                                jointablename = this.getJoinTableName(columnid = columnid.substring(0, columnid.lastIndexOf(".") + 1) + coldef);
                                if (jointablename == null) {
                                    this.setAliasMapCommon(columnid);
                                    jointablename = this.getJoinTableName(columnid);
                                }
                                columntype = this.shu.getTypeName(jointablename, realcolid);
                            } else {
                                columntype = sCol.getHibernateType();
                            }
                        }
                    }
                    joinStr = this.joinsMap.get(jointablename);
                    if (columnid.indexOf("sdidataitem[") >= 0) {
                        detableid = this.tableid;
                        desdcid = this.sdcid;
                        String dekeycolid1 = this.keycolid1;
                        String dekeycolid2 = this.keycolid2;
                        String dekeycolid3 = this.keycolid3;
                        String actualdetableid2 = this.tableid;
                        boolean isDetailDataEntry = false;
                        String prefix2 = "";
                        if (columnid.indexOf(".sdidataitem[") > 0) {
                            try {
                                prefix2 = columnid.substring(0, columnid.indexOf(".sdidataitem["));
                                String refdetailtableid = this.adhocmetadata.getReverseFKTableId(prefix2);
                                actualdetableid2 = this.shu.getReferenceEntityName(this.tableid, prefix2);
                                isDetailDataEntry = refdetailtableid != null && refdetailtableid.equals(actualdetableid2) || prefix2.equals(actualdetableid2);
                                detableid = this.tableid + "." + prefix2;
                                desdcid = this.adhocmetadata.getSdcId(actualdetableid2);
                                if (desdcid == null && actualdetableid2.indexOf("_") > 0) {
                                    desdcid = actualdetableid2.substring(actualdetableid2.indexOf("_") + 1);
                                }
                                PropertyList sdcPropertyList = this.ddtService.getSDCProperties(desdcid);
                                dekeycolid1 = sdcPropertyList.getProperty("keycolid1");
                                dekeycolid2 = sdcPropertyList.getProperty("keycolid2");
                                dekeycolid3 = sdcPropertyList.getProperty("keycolid3");
                            }
                            catch (Exception refdetailtableid) {
                                // empty catch block
                            }
                        }
                        if (this.dataEntry == null) {
                            this.dataEntry = new DataEntryHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        if (groupBoolean.equals("or") && desdcidJoinNoMap.get(desdcid) == null) {
                            desdcidJoinNoMap.put(desdcid, dataentryjoinIndex);
                            ++dataentryjoinIndex;
                        }
                        dataentryjoinNo = desdcidJoinNoMap.get(desdcid) != null ? (Integer)desdcidJoinNoMap.get(desdcid) : dataentryjoinIndex++;
                        if (isDetailDataEntry) {
                            if (isNamedGroup) {
                                String deprimaryTableAlias = prefix2 + reverseFKdetailjoinNo;
                                String primaryJoin = this.joinsMap.get(deprimaryTableAlias);
                                if (primaryJoin == null || primaryJoin.length() == 0) {
                                    primaryJoin = " join " + this.tableid + "." + prefix2 + " as " + deprimaryTableAlias;
                                    this.addToJoinsMap(deprimaryTableAlias, primaryJoin, "");
                                }
                                joinStr = " left join " + deprimaryTableAlias + ".sdidataitem as sdidataitem" + dataentryjoinNo + " ";
                                actualdetableid2 = deprimaryTableAlias;
                            } else {
                                joinStr = " left join " + this.tableid + "." + prefix2 + " as " + actualdetableid2 + " join " + actualdetableid2 + ".sdidataitem as sdidataitem" + dataentryjoinNo + " ";
                            }
                        } else {
                            joinStr = this.dataEntry.getDataEntryJoin(detableid, columnid, dataentryjoinNo);
                        }
                        dataentryWhere = this.dataEntry.getDataEntryWhere(columnid, desdcid, actualdetableid2, dekeycolid1, dekeycolid2, dekeycolid3, dataentryjoinNo, operator, value, columntype);
                        this.addToJoinsMap("sdidataitem" + dataentryjoinNo, joinStr, "");
                        this.detailCrMap.put(columnid, new Integer(dataentryjoinNo));
                    } else if (columnid.indexOf("documentfield[") >= 0) {
                        detableid = this.tableid;
                        desdcid = this.sdcid;
                        if (columnid.indexOf(".documentfield[") > 0) {
                            try {
                                prefix3 = columnid.substring(0, columnid.indexOf(".documentfield["));
                                actualdetableid = this.shu.getReferenceEntityName(this.tableid, prefix3);
                                detableid = this.tableid + "." + prefix3;
                                desdcid = this.adhocmetadata.getSdcId(actualdetableid);
                            }
                            catch (Exception prefix3) {
                                // empty catch block
                            }
                        }
                        if (this.documentField == null) {
                            this.documentField = new DocumentFieldHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        joinStr = this.documentField.getDocumentFieldJoin(detableid, desdcid, documentfieldjoinNo);
                        documentfieldWhere = this.documentField.getDocumentFieldWhere(columnid, desdcid, documentfieldjoinNo, operator, value, columntype);
                        this.addToJoinsMap("documentfield" + documentfieldjoinNo, joinStr, "");
                        this.detailCrMap.put(columnid, new Integer(documentfieldjoinNo));
                        ++documentfieldjoinNo;
                    } else if (columnid.indexOf("worksheetitemfield[") >= 0) {
                        detableid = this.tableid;
                        desdcid = this.sdcid;
                        if (columnid.indexOf(".worksheetitemfield[") > 0) {
                            try {
                                prefix3 = columnid.substring(0, columnid.indexOf(".worksheetitemfield["));
                                actualdetableid = this.shu.getReferenceEntityName(this.tableid, prefix3);
                                detableid = this.tableid + "." + prefix3;
                                desdcid = this.adhocmetadata.getSdcId(actualdetableid);
                            }
                            catch (Exception prefix4) {
                                // empty catch block
                            }
                        }
                        if (this.worksheetItemField == null) {
                            this.worksheetItemField = new WorksheetItemFieldHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        joinStr = this.worksheetItemField.getWorksheetItemFieldJoin(detableid, desdcid, worksheetitemfieldjoinNo);
                        worksheetitemfieldWhere = this.worksheetItemField.getWorksheetItemFieldWhere(columnid, desdcid, worksheetitemfieldjoinNo, operator, value, columntype);
                        this.addToJoinsMap("worksheetitemfield" + worksheetitemfieldjoinNo, joinStr, "");
                        this.detailCrMap.put(columnid, new Integer(worksheetitemfieldjoinNo));
                        ++worksheetitemfieldjoinNo;
                    } else if (columnid.indexOf("sdiattribute[") >= 0) {
                        detableid = this.tableid;
                        desdcid = this.sdcid;
                        String actualdetableid3 = "";
                        if (columnid.indexOf(".sdiattribute[") > 0) {
                            try {
                                String prefix5 = columnid.substring(0, columnid.indexOf(".sdiattribute["));
                                actualdetableid3 = this.shu.getReferenceEntityName(this.tableid, prefix5);
                                detableid = this.tableid + "." + prefix5;
                                desdcid = this.adhocmetadata.getSdcId(actualdetableid3);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (this.sdiattribute == null) {
                            this.sdiattribute = new AttributeHqlBuilder(this.sdcid, this.shu, this.adhocmetadata, this.hqlBuilder);
                        }
                        joinStr = this.sdiattribute.getAttributeJoin(detableid, desdcid, columnid, attributejoinNo, actualdetableid3);
                        sdiattributeWhere = this.sdiattribute.getAttributeWhere(columnid, desdcid, attributejoinNo, operator, value, columntype);
                        this.addToJoinsMap("sdiattribute" + attributejoinNo, joinStr, "");
                        this.detailCrMap.put(columnid, new Integer(attributejoinNo));
                        ++attributejoinNo;
                    } else if (jointablename != null && jointablename.length() > 0 && DetailTable.isDetailTable(jointablename) && StringUtil.split(columnid, ".").length < 3) {
                        String addJoinCondition = "";
                        String joinAlias = jointablename;
                        if (isNamedGroup) {
                            joinAlias = jointablename + reverseFKdetailjoinNo;
                        }
                        if (joinStr == null) {
                            joinStr = DetailTable.getDetailJoinClause(jointablename, joinAlias, this.sdcPropertyList);
                            addJoinCondition = DetailTable.getDetailJoinWhereClause(jointablename, joinAlias, this.sdcPropertyList);
                        }
                        this.addToJoinsMap(joinAlias, joinStr, addJoinCondition);
                    } else if (joinStr == null) {
                        String joinAlias = this.getJoinAliasName(columnid);
                        if (isNamedGroup) {
                            joinAlias = joinAlias + reverseFKdetailjoinNo;
                        }
                        this.buildToJoinsMap(columnid, joinAlias);
                    }
                } else if (columndefinition != null && DetailTable.isDetailColumnWithQualifier(columndefinition)) {
                    jointablename = columndefinition.substring(0, columndefinition.indexOf("."));
                    joinStr = this.joinsMap.get(jointablename);
                    String addJoinCondition = "";
                    if (joinStr == null) {
                        joinStr = DetailTable.getDetailJoinClause(jointablename, jointablename, this.sdcPropertyList);
                        addJoinCondition = DetailTable.getDetailJoinWhereClause(jointablename, jointablename, this.sdcPropertyList);
                    }
                    this.addToJoinsMap(jointablename, joinStr, addJoinCondition);
                }
                if (operator == null || operator.length() <= 0) continue;
                if (col != 0 && groupwhere.length() > 0) {
                    groupwhere.append(" " + groupBoolean + " ");
                }
                if (columnid.indexOf("sdidataitem[") >= 0) {
                    groupwhere.append(dataentryWhere);
                    continue;
                }
                if (columnid.indexOf("documentfield[") >= 0) {
                    groupwhere.append(documentfieldWhere);
                    continue;
                }
                if (columnid.indexOf("worksheetitemfield[") >= 0) {
                    groupwhere.append(worksheetitemfieldWhere);
                    continue;
                }
                if (columnid.indexOf("sdiattribute[") >= 0) {
                    groupwhere.append(sdiattributeWhere);
                    continue;
                }
                if (isExtendedCol && !isExtendedColOnLinkedTable) {
                    if (this.isRequireTableQualifier(columndefinition = columndefinition.trim(), this.tableid)) {
                        columndefinition = this.tableid + "." + columndefinition;
                    }
                    String objectname = "";
                    try {
                        objectname = this.shu.getReferenceEntityName(this.tableid, columndefinition);
                    }
                    catch (Exception addJoinCondition) {
                        // empty catch block
                    }
                    if (objectname != null && objectname.length() > 0) {
                        columndefinition = columndefinition + "_column";
                    }
                    groupwhere.append(this.hqlBuilder.getOperatorValueClause(operator, value, columntype, columndefinition, true));
                    continue;
                }
                String hqlcolid = columnid;
                if (this.aliasMap.get(columnid + "_column") != null) {
                    hqlcolid = (String)this.aliasMap.get(columnid + "_column");
                    if (hqlcolid.indexOf(jointablename + ".") != 0 && this.isRequireTableQualifier(hqlcolid, this.tableid)) {
                        hqlcolid = this.tableid + "." + hqlcolid;
                    }
                } else if (columnid.indexOf(".") > 0) {
                    hqlcolid = (String)this.aliasMap.get(columnid.substring(0, columnid.lastIndexOf("."))) + columnid.substring(columnid.lastIndexOf("."));
                } else if (hqlcolid.indexOf(jointablename + ".") != 0 && this.isRequireTableQualifier(hqlcolid, this.tableid)) {
                    hqlcolid = this.tableid + "." + hqlcolid;
                }
                boolean isTZsensitive = true;
                try {
                    PropertyListCollection columns = this.ddtService.getSDCProperties(this.adhocmetadata.getSdcId(this.tableid)).getCollection("columns");
                    isTZsensitive = !"Y".equals(columns.find("columnid", columnid).getProperty("timezoneindependent"));
                }
                catch (Exception columns) {
                    // empty catch block
                }
                if (isNamedGroup && hqlcolid.indexOf(".") > 0) {
                    String alias = hqlcolid.substring(0, hqlcolid.indexOf("."));
                    hqlcolid = StringUtil.replaceAll(hqlcolid, alias, alias + reverseFKdetailjoinNo);
                }
                groupwhere.append(this.hqlBuilder.getOperatorValueClause(operator, value, columntype, hqlcolid, isTZsensitive));
            }
            groupwhereList.add(groupwhere);
        }
        for (int i = 0; i < groupwhereList.size(); ++i) {
            StringBuffer groupWhere = (StringBuffer)groupwhereList.get(i);
            if (groupWhere.length() <= 0) continue;
            if (wherebuffer.length() == 0) {
                wherebuffer.append("(" + groupWhere + ")");
                continue;
            }
            wherebuffer.append(" " + betweenGroupBoolean + " (" + groupWhere + ")");
        }
        ArrayList view = adhocQueryRequest.getView();
        StringBuffer selectbuffer = this.getSelectClause(view);
        StringBuffer orderby = new StringBuffer(" order by ");
        ArrayList orderbycols = this.adhocRequest.getOrderby();
        int orderbyIndex = -1;
        for (int i = 0; i < orderbycols.size(); ++i) {
            OrderByArg orderByArg = (OrderByArg)orderbycols.get(i);
            String columnid = orderByArg.getColumnid();
            SearchableColumn searchableCol = this.getSearchableColumn(this.tableid, columnid);
            boolean isExtendedCol = false;
            String columndefinition = "";
            if (searchableCol != null && searchableCol.getColumndefinition() != null) {
                columndefinition = searchableCol.getColumndefinition();
                isExtendedCol = true;
            }
            if (i != 0) {
                orderby.append(",");
            }
            String selectcolid = columnid;
            if (isExtendedCol) {
                selectcolid = columndefinition;
            } else if (columnid.indexOf(".") < 0) {
                selectcolid = this.tableid + "." + columnid;
            }
            ArrayList viewlist = this.adhocRequest.getView();
            int indexInView = -1;
            for (int vc = 0; vc < viewlist.size(); ++vc) {
                if (!columnid.equals(((AdhocArgument)viewlist.get(vc)).getColumnid())) continue;
                indexInView = vc;
                break;
            }
            if (indexInView < 0) {
                selectbuffer.append("," + selectcolid);
                orderbyIndex = orderbyIndex < 0 ? this.selectList.size() + 1 : ++orderbyIndex;
                orderby.append(orderbyIndex + " " + orderByArg.getDirection());
                continue;
            }
            orderby.append(indexInView + 1 + " " + orderByArg.getDirection());
        }
        if (orderbycols == null || orderbycols.size() == 0) {
            orderby.append(" 1 ");
        }
        StringBuffer addJoinCondition = new StringBuffer();
        for (int i = 0; i < this.joinsList.size(); ++i) {
            frombuffer.append(this.joinsMap.get(this.joinsList.get(i)));
            if (this.joinsCondition.get(i) == null || this.joinsCondition.get(i).length() <= 0) continue;
            if (addJoinCondition.length() > 0) {
                addJoinCondition.append(" and ");
            }
            addJoinCondition.append("(" + this.joinsCondition.get(i) + ")");
        }
        this.fromclause = frombuffer.toString();
        String securityfilter = "";
        String activefilter = "";
        String deptselect = "";
        String accessflag = this.sdcPropertyList.getProperty("accesscontrolledflag");
        if ("Y".equals(accessflag) || "L".equals(accessflag) || "D".equals(accessflag) || "S".equals(accessflag)) {
            securityfilter = new QueryService(this.sapphireConnection).getSecurityFilterWhere(this.sdcid);
            securityfilter = StringUtil.replaceAll(securityfilter, "SELECT NULL", "");
            securityfilter = StringUtil.replaceAll(securityfilter, "SELECT null", "");
            securityfilter = StringUtil.replaceAll(securityfilter, "." + this.keycolid1.toUpperCase(), "." + this.keycolid1);
        } else if ("Y".equals(this.sdcPropertyList.getProperty("activeableflag")) && !"(system)".equals(this.sapphireConnection.getSysuserId()) && !"Y".equals(new ConfigService(this.sapphireConnection).getProfileProperty(this.sapphireConnection.getSysuserId(), "viewhidden", "N"))) {
            activefilter = this.tableid + ".activeflag!='N' or " + this.tableid + ".activeflag is null";
        }
        if (this.joinsList.contains("sdidata") && this.joinsList.contains("sdidataitem")) {
            if (addJoinCondition.length() > 0) {
                addJoinCondition.append(" and ");
            }
            addJoinCondition.append("sdidata.sdcid=sdidataitem.sdcid and sdidata.keyid1=sdidataitem.keyid1 and sdidata.keyid2=sdidataitem.keyid2 and sdidata.keyid3=sdidataitem.keyid3 and sdidata.paramlistid=sdidataitem.paramlistid and sdidata.paramlistversionid=sdidataitem.paramlistversionid and sdidata.variantid=sdidataitem.variantid and sdidata.dataset=sdidataitem.dataset");
        }
        if (rset != null) {
            if (addJoinCondition.length() > 0) {
                addJoinCondition.append(" and ");
            }
            addJoinCondition.append(rsetitemtable + ".rsetid='" + rset.getRsetid() + "' and " + rsetitemtable + ".sdcid='" + this.sdcid + "' and " + rsetitemtable + ".keyid1=" + this.tableid + "." + this.keycolid1);
            if (this.keycolid2 != null && this.keycolid2.length() > 0) {
                addJoinCondition.append(" and " + rsetitemtable + ".keyid2=" + this.tableid + "." + this.keycolid2);
            }
            if (this.keycolid3 != null && this.keycolid3.length() > 0) {
                addJoinCondition.append(" and " + rsetitemtable + ".keyid3=" + this.tableid + "." + this.keycolid3);
            }
        }
        if (addJoinCondition.length() > 0) {
            if (wherebuffer.length() > 0) {
                wherebuffer.append(" and (" + addJoinCondition + ")");
            } else {
                wherebuffer = addJoinCondition;
            }
        }
        if ("DataSet".equals(this.sdcid) && "B".equals(accessflag) && wherebuffer.indexOf("sdidata.sdcid=") < 0) {
            String string = this.restrictiveWhere = this.restrictiveWhere != null && this.restrictiveWhere.length() > 0 ? this.restrictiveWhere + " and sdidata.sdcid='Sample'" : "sdidata.sdcid='Sample'";
        }
        if ("SDIWorkItem".equals(this.sdcid) && "P".equals(accessflag) && wherebuffer.indexOf("sdiworkitem.sdcid=") < 0) {
            String string = this.restrictiveWhere = this.restrictiveWhere != null && this.restrictiveWhere.length() > 0 ? this.restrictiveWhere + " and sdiworkitem.sdcid='Sample'" : "sdiworkitem.sdcid='Sample'";
        }
        this.whereclause = this.restrictiveWhere != null && this.restrictiveWhere.length() > 0 ? (wherebuffer.length() > 0 && activefilter.length() > 0 ? " where (" + wherebuffer + ") and (" + this.restrictiveWhere + ") and (" + activefilter + ")" : (wherebuffer.length() > 0 ? " where (" + wherebuffer + ") and (" + this.restrictiveWhere + ")" : (activefilter.length() > 0 ? " where (" + activefilter + ") and (" + this.restrictiveWhere + ")" : " where " + this.restrictiveWhere))) : (wherebuffer.length() > 0 && activefilter.length() > 0 ? " where (" + wherebuffer.toString() + ") and (" + activefilter + ")" : (wherebuffer.length() > 0 || activefilter.length() > 0 ? " where (" + wherebuffer.toString() + activefilter + ")" : ""));
        String sHQL = "";
        String fromWhereClause = this.fromclause;
        if (this.whereclause.length() > 0) {
            fromWhereClause = fromWhereClause + this.whereclause + (securityfilter.length() > 0 ? " and " + securityfilter : "");
        } else if (securityfilter.length() > 0) {
            fromWhereClause = fromWhereClause + " where " + securityfilter;
        }
        sHQL = this.adhocRequest.isRequestCount() ? (this.sdcPropertyList != null && "D".equals(this.sdcPropertyList.getProperty("sdctype")) ? "SELECT count( * ) " + fromWhereClause : "SELECT count( distinct " + this.tableid + "." + this.keycolid1 + " ) " + fromWhereClause) : selectbuffer.toString() + deptselect + fromWhereClause + (orderby.length() > 10 ? orderby.toString() : "");
        this.logInfo("###!!!Done building HQL" + (System.currentTimeMillis() - starttime) + "ms:" + sHQL);
        return sHQL;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List testHQL(String sHQL) {
        Session dynamicSession = this.shu.currentSession();
        List list = null;
        long starttime = System.currentTimeMillis();
        try {
            Trace.log("## Start Test HQL:" + sHQL);
            Query q = dynamicSession.createQuery(sHQL);
            Trace.log("## Created Query with HQL:" + q.getQueryString() + "\nExecuting...");
            list = q.list();
            Trace.log("## Done Executing HQL:" + (System.currentTimeMillis() - starttime) + "ms");
        }
        catch (SQLGrammarException sqle) {
            String sql = sqle.getSQL();
            String message = sqle.getMessage();
            SQLException e = sqle.getSQLException();
            this.errormessage = message + ":" + e.getMessage() + " SQL:" + sql + "\nHQL:" + sHQL;
            this.logError(this.errormessage, sqle);
        }
        catch (Exception e) {
            String message = e.getMessage();
            this.errormessage = message + ":" + e.getMessage() + "\nHQL:" + sHQL;
            this.logError(this.errormessage, e);
        }
        finally {
            this.shu.closeSession();
        }
        return list;
    }

    private void setAliasMap() {
        ArrayList view = this.adhocRequest.getView();
        for (int i = 0; i < view.size(); ++i) {
            AdhocArgument viewArg = (AdhocArgument)view.get(i);
            String viewcolid = viewArg.getColumnid();
            this.setAliasMapCommon(viewcolid);
        }
        AdhocCriteria adhocCriteria = this.adhocRequest.getCriteria();
        for (int i = 0; i < adhocCriteria.size(); ++i) {
            AdhocCriteriaArgGroup adhocGroup = (AdhocCriteriaArgGroup)adhocCriteria.get(i);
            for (int g = 0; g < adhocGroup.size(); ++g) {
                AdhocCriteriaArg criteriaArg = (AdhocCriteriaArg)adhocGroup.get(g);
                String columnid = criteriaArg.getColumnid();
                this.setAliasMapCommon(columnid);
            }
        }
    }

    private void setAliasMapCommon(String columnid) {
        if (columnid.indexOf("[") <= 0) {
            if (columnid.indexOf(".") > 0) {
                int lastdotindex = columnid.lastIndexOf(".");
                String aliasid = columnid.substring(0, lastdotindex);
                String objectname = "";
                try {
                    objectname = this.shu.getReferenceEntityName(this.tableid, columnid);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (objectname == null || objectname.length() == 0) {
                    try {
                        objectname = this.shu.getAliasName(this.tableid, columnid);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (this.adhocmetadata.getReverseFKTableId(aliasid) != null) {
                        this.aliasMap.put(aliasid, StringUtil.replaceAll(aliasid, ".", "_"));
                    } else if (objectname != null && objectname.length() > 0) {
                        this.aliasMap.put(aliasid, StringUtil.replaceAll(aliasid, ".", "_"));
                    } else {
                        this.aliasMap.put(aliasid, aliasid);
                    }
                    this.aliasTableMap.put(aliasid, objectname);
                } else {
                    this.aliasMap.put(aliasid, StringUtil.replaceAll(aliasid, ".", "_"));
                    this.aliasMap.put(columnid + "_column", columnid + "_column");
                }
            } else {
                String objectname = "";
                try {
                    objectname = this.shu.getAliasName(this.tableid, columnid);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (objectname.length() > 0) {
                    this.aliasMap.put(columnid + "_column", columnid + "_column");
                }
                this.aliasTableMap.put(columnid, objectname);
            }
        }
    }

    private StringBuffer getSelectClause(ArrayList view) throws ServiceException {
        StringBuffer keyselectsb;
        StringBuffer selectbuffer = new StringBuffer("select distinct ");
        this.selectList = new ArrayList();
        if (view.size() > 0) {
            for (int col = 0; col < view.size(); ++col) {
                String jointablename;
                AdhocArgument viewArg = (AdhocArgument)view.get(col);
                String columnid = viewArg.getColumnid();
                SearchableColumn searchableCol = this.adhocmetadata.getSearchableColumn(this.tableid, columnid);
                if (searchableCol != null && searchableCol.getColumndefinition() != null) {
                    this.selectList.add(columnid);
                    if (this.selectList.size() > 1) {
                        selectbuffer.append(", ");
                    }
                    String columndef = searchableCol.getColumndefinition();
                    String objectname = "";
                    try {
                        objectname = this.shu.getReferenceEntityName(this.tableid, columndef);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (this.isRequireTableQualifier(columndef, this.tableid)) {
                        selectbuffer.append(this.tableid + ".");
                    }
                    if (objectname != null && objectname.length() > 0) {
                        selectbuffer.append(columndef + "_column As " + columnid + " ");
                    } else {
                        selectbuffer.append(columndef + " As " + columnid + " ");
                    }
                    if (columndef.indexOf(".") > 0) {
                        try {
                            objectname = this.shu.getAliasName(this.tableid, columndef);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (objectname != null && objectname.length() > 0) {
                            columnid = columndef;
                            this.setAliasMapCommon(columnid);
                        }
                    }
                } else if (columnid.indexOf(".") < 0 && this.aliasMap.get(columnid + "_column") != null) {
                    this.selectList.add(columnid);
                    if (this.selectList.size() > 1) {
                        selectbuffer.append(", ");
                    }
                    if (this.isRequireTableQualifier(columnid, this.tableid)) {
                        selectbuffer.append(this.tableid + "." + (String)this.aliasMap.get(columnid + "_column"));
                    } else {
                        selectbuffer.append((String)this.aliasMap.get(columnid + "_column"));
                    }
                } else if (columnid.indexOf(".") < 0 && columnid.indexOf("sdidataitem[") < 0 && columnid.indexOf("documentfield[") < 0 && columnid.indexOf("worksheetitemfield[") < 0 && columnid.indexOf("sdiattribute[") < 0) {
                    this.selectList.add(columnid);
                    if (this.selectList.size() > 1) {
                        selectbuffer.append(", ");
                    }
                    selectbuffer.append(this.tableid + "." + columnid);
                } else if (columnid.indexOf("sdidataitem[") >= 0) {
                    if (this.detailViewMap.get("sdidataitem") == null) {
                        this.detailViewMap.put("sdidataitem", new ArrayList());
                    }
                    ((ArrayList)this.detailViewMap.get("sdidataitem")).add(columnid);
                } else if (columnid.indexOf("documentfield[") >= 0) {
                    if (this.detailViewMap.get("documentfield") == null) {
                        this.detailViewMap.put("documentfield", new ArrayList());
                    }
                    ((ArrayList)this.detailViewMap.get("documentfield")).add(columnid);
                } else if (columnid.indexOf("worksheetitemfield[") >= 0) {
                    if (this.detailViewMap.get("worksheetitemfield") == null) {
                        this.detailViewMap.put("worksheetitemfield", new ArrayList());
                    }
                    ((ArrayList)this.detailViewMap.get("worksheetitemfield")).add(columnid);
                } else if (columnid.indexOf("sdiattribute[") >= 0) {
                    if (this.detailViewMap.get("sdiattribute") == null) {
                        this.detailViewMap.put("sdiattribute", new ArrayList());
                    }
                    ((ArrayList)this.detailViewMap.get("sdiattribute")).add(columnid);
                } else {
                    SearchableColumn sCol;
                    jointablename = this.getJoinTableName(columnid);
                    String colaliasid = columnid.substring(columnid.lastIndexOf(".") + 1);
                    String coldef = null;
                    boolean isNestedSelectOrFunction = false;
                    if (this.adhocmetadata.getSearchableColumn(jointablename, colaliasid) != null && (coldef = (sCol = this.adhocmetadata.getSearchableColumn(jointablename, colaliasid)).getColumndefinition()) != null && coldef.length() > 0) {
                        String originalcolid = "" + columnid;
                        if (AdhocQueryService.isDotSyntaxColumn(coldef)) {
                            columnid = columnid.substring(0, columnid.lastIndexOf(".") + 1) + coldef;
                        } else {
                            isNestedSelectOrFunction = true;
                        }
                        this.setAliasMapCommon(columnid);
                        this.extendedColViewMap.put(columnid, originalcolid);
                    }
                    this.selectList.add(columnid);
                    if (this.selectList.size() > 1) {
                        selectbuffer.append(", ");
                    }
                    if (isNestedSelectOrFunction) {
                        selectbuffer.append(coldef);
                    } else {
                        selectbuffer.append(AdhocQueryService.getAliasedSelect(columnid, this.aliasMap));
                    }
                }
                jointablename = "";
                if (columnid.indexOf(".") <= 0 || columnid.indexOf(".sdidataitem[") >= 0 || columnid.indexOf(".documentfield[") >= 0 || (jointablename = this.getJoinAliasName(columnid)) == null) continue;
                if (jointablename.indexOf(".") > 0) {
                    jointablename = jointablename.substring(jointablename.lastIndexOf(".") + 1);
                }
                String joinStr = this.joinsMap.get(jointablename);
                if (jointablename.length() > 0 && DetailTable.isDetailTable(jointablename)) {
                    String addJoinCondition = "";
                    if (joinStr == null) {
                        joinStr = DetailTable.getDetailJoinClause(jointablename, jointablename, this.sdcPropertyList);
                        addJoinCondition = DetailTable.getDetailJoinWhereClause(jointablename, jointablename, this.sdcPropertyList);
                    }
                    this.addToJoinsMap(jointablename, joinStr, addJoinCondition);
                    continue;
                }
                if (joinStr != null) continue;
                this.buildToJoinsMap(columnid, jointablename);
            }
        }
        if ((keyselectsb = this.getKeyColumnSelect(false)).length() > 0) {
            selectbuffer.append((selectbuffer.length() <= 17 ? "" : ",") + (keyselectsb.indexOf(",") == 0 ? keyselectsb.substring(1) : keyselectsb));
        }
        return selectbuffer;
    }

    private void buildToJoinsMap(String columnid, String alias) throws ServiceException {
        if (columnid.indexOf(".") > 0) {
            String prefix = columnid.substring(0, columnid.lastIndexOf("."));
            String anchorObjectRef = "";
            String[] tokens = StringUtil.split(prefix, ".");
            for (int i = 0; i < tokens.length; ++i) {
                String linkcolid2;
                Object linksdcid;
                PropertyList linkProps;
                PropertyListCollection links;
                String keycolumns;
                String joinAnchorObject;
                String addJoinCondition = "";
                String string = joinAnchorObject = i == 0 ? this.tableid : tokens[i - 1];
                if (i == 1) {
                    anchorObjectRef = tokens[0];
                } else if (i > 1) {
                    anchorObjectRef = anchorObjectRef + "." + tokens[i - 1];
                }
                String joinObject = tokens[i];
                String joinAlias = i == tokens.length - 1 ? alias : tokens[i];
                String anchorTableid = i == 0 ? this.tableid : this.shu.getReferenceEntityName(this.tableid, anchorObjectRef);
                String joinTableid = this.shu.getReferenceEntityName(this.tableid, i == 0 ? tokens[0] : anchorObjectRef + "." + joinObject);
                ArrayList rFKChildList = this.adhocmetadata.getReverseFKChildList(anchorTableid);
                if (rFKChildList != null && rFKChildList.contains(joinObject)) {
                    String anchorSDC = this.adhocmetadata.getSdcId(anchorTableid);
                    keycolumns = this.ddtService.getSDCProperties(anchorSDC).getProperty("keycolumns");
                    if ("2".equals(keycolumns) || "3".equals(keycolumns)) {
                        String anchorSDCkeycolid2 = this.ddtService.getSDCProperties(anchorSDC).getProperty("keycolid2");
                        String fkChildSDC = this.adhocmetadata.getSdcId(joinTableid);
                        links = this.ddtService.getSDCProperties(fkChildSDC).getCollection("links");
                        for (int k = 0; k < links.size(); ++k) {
                            linkProps = links.getPropertyList(k);
                            linksdcid = linkProps.getProperty("linksdcid");
                            if (!((String)linksdcid).equals(anchorSDC)) continue;
                            linkcolid2 = linkProps.getProperty("sdccolumnid2");
                            addJoinCondition = "(" + joinAlias + "." + linkcolid2 + " is null or " + joinAlias + "." + linkcolid2 + "=" + joinAnchorObject + "." + anchorSDCkeycolid2 + ")";
                            break;
                        }
                    }
                } else {
                    String joinSDC = this.adhocmetadata.getSdcId(joinTableid);
                    if (joinSDC != null) {
                        keycolumns = this.ddtService.getSDCProperties(joinSDC).getProperty("keycolumns");
                        if ("2".equals(keycolumns) || "3".equals(keycolumns)) {
                            String joinSDCkeycolid2 = this.ddtService.getSDCProperties(joinSDC).getProperty("keycolid2");
                            String anchorSDC = this.adhocmetadata.getSdcId(anchorTableid);
                            links = this.ddtService.getSDCProperties(anchorSDC).getCollection("links");
                            for (int k = 0; k < links.size(); ++k) {
                                linkProps = links.getPropertyList(k);
                                linksdcid = linkProps.getProperty("linksdcid");
                                if (!((String)linksdcid).equals(joinSDC)) continue;
                                linkcolid2 = linkProps.getProperty("sdccolumnid2");
                                addJoinCondition = "(" + joinAnchorObject + "." + linkcolid2 + " is null or " + joinAnchorObject + "." + linkcolid2 + "=" + joinAlias + "." + joinSDCkeycolid2 + ")";
                            }
                        }
                    } else {
                        PropertyList sdcPropertyList;
                        String anchorSDC = this.adhocmetadata.getSdcId(anchorTableid);
                        if (anchorSDC != null && (sdcPropertyList = this.ddtService.getSDCProperties(anchorSDC)) != null && ("2".equals(sdcPropertyList.getProperty("keycolumns")) || "3".equals(sdcPropertyList.getProperty("keycolumns")))) {
                            String keycolid2 = sdcPropertyList.getProperty("keycolid2");
                            String keycolid3 = sdcPropertyList.getProperty("keycolid3");
                            PropertyList detailtablePL = null;
                            PropertyListCollection tables = sdcPropertyList.getCollection("tables");
                            for (Object p : tables) {
                                if (p == null || !(p instanceof PropertyList) || !joinTableid.equals(((PropertyList)p).getProperty("tableid"))) continue;
                                detailtablePL = (PropertyList)p;
                                break;
                            }
                            if (detailtablePL != null) {
                                String detailkeycolid2 = "keyid2";
                                String detailkeycolid3 = "keyid3";
                                if (detailtablePL != null) {
                                    detailkeycolid2 = detailtablePL.getProperty("keycolid2");
                                    detailkeycolid3 = detailtablePL.getProperty("keycolid3");
                                }
                                addJoinCondition = "(" + joinAnchorObject + "." + keycolid2 + "=" + joinAlias + "." + detailkeycolid2 + (keycolid3 != null && keycolid3.length() > 0 ? " and " + joinAnchorObject + "." + keycolid3 + "=" + joinAlias + "." + detailkeycolid3 : "") + ")";
                            }
                        }
                    }
                }
                this.addToJoinsMap(joinAlias, " left join " + joinAnchorObject + "." + joinObject + " as " + joinAlias, addJoinCondition);
            }
        } else {
            this.addToJoinsMap(alias, " left join " + this.tableid + "." + columnid + " as " + alias, "");
        }
    }

    private void addToJoinsMap(String alias, String joinStr, String joinCondition) {
        if (!this.joinsList.contains(alias)) {
            this.joinsList.add(alias);
            this.joinsMap.put(alias, joinStr);
            this.joinsCondition.add(joinCondition);
        }
    }

    private StringBuffer getKeyColumnSelect(boolean isRetrieveDetail, ArrayList selectList) {
        StringBuffer selectbuffer = new StringBuffer();
        if ("sdidataitem".equals(this.tableid) || "sdidata".equals(this.tableid)) {
            DetailTable.getKeyColumnSelect(this.tableid, selectbuffer, selectList, this.shu);
        } else {
            SDCTable.getKeyColumnSelect(this.tableid, this.keycolid1, this.keycolid2, this.keycolid3, this.desccol, selectbuffer, selectList, isRetrieveDetail);
        }
        return selectbuffer;
    }

    private StringBuffer getKeyColumnSelect(boolean isRetrieveDetail) {
        return this.getKeyColumnSelect(isRetrieveDetail, this.selectList);
    }

    private static String getAliasedSelect(String columnid, HashMap aliasMap) {
        if (aliasMap.get(columnid + "_column") != null) {
            columnid = (String)aliasMap.get(columnid + "_column");
        }
        int lastdotindex = columnid.lastIndexOf(".");
        String prefix = columnid.substring(0, lastdotindex);
        String col = columnid.substring(lastdotindex + 1);
        return (String)aliasMap.get(prefix) + "." + col;
    }

    private String getJoinTableName(String columnid) {
        if (columnid.indexOf(".") > 0) {
            int lastdotindex = columnid.lastIndexOf(".");
            String prefix = columnid.substring(0, lastdotindex);
            return (String)this.aliasTableMap.get(prefix);
        }
        return (String)this.aliasTableMap.get(columnid);
    }

    private String getJoinAliasName(String columnid) {
        if (columnid.indexOf(".") > 0) {
            int lastdotindex = columnid.lastIndexOf(".");
            String prefix = columnid.substring(0, lastdotindex);
            return (String)this.aliasMap.get(prefix);
        }
        return (String)this.aliasMap.get(columnid);
    }

    public DataSet getDataSetFromListofArray(List list, ArrayList columnids, Type[] columntypes) {
        DataSet ds = new DataSet();
        for (int i = 0; i < columnids.size(); ++i) {
            if (i >= columntypes.length) continue;
            int columntype = 0;
            String typename = columntypes[i].getName();
            if ("timestamp".equals(typename)) {
                columntype = 2;
            } else if ("big_decimal".equals(typename) || "integer".equals(typename) || "long".equals(typename) || "double".equals(typename)) {
                columntype = 1;
            }
            String columnid2 = (String)columnids.get(i);
            if (this.extendedColViewMap.get(columnid2) != null) {
                columnid2 = (String)this.extendedColViewMap.get(columnid2);
            }
            ds.addColumn(columnid2, columntype);
        }
        boolean isObjectArray = list.size() > 0 && list.get(0) instanceof Object[];
        for (int i = 0; i < list.size(); ++i) {
            ds.addRow();
            if (columntypes.length == 1 && !isObjectArray) {
                String columnid = (String)columnids.get(0);
                if (this.extendedColViewMap.get(columnid) != null) {
                    columnid = (String)this.extendedColViewMap.get(columnid);
                }
                if ("timestamp".equals(columntypes[0].getName())) {
                    ds.setDate(i, columnid, (Timestamp)list.get(i));
                    continue;
                }
                if ("big_decimal".equals(columntypes[0].getName())) {
                    ds.setNumber(i, columnid, (BigDecimal)list.get(i));
                    continue;
                }
                if ("integer".equals(columntypes[0].getName())) {
                    ds.setNumber(i, columnid, (Integer)list.get(i));
                    continue;
                }
                if ("long".equals(columntypes[0].getName())) {
                    ds.setNumber(i, columnid, (Long)list.get(i));
                    continue;
                }
                if ("double".equals(columntypes[0].getName())) {
                    ds.setNumber(i, columnid, (Double)list.get(i));
                    continue;
                }
                if ("string".equals(columntypes[0].getName())) {
                    ds.setString(i, columnid, (String)list.get(i));
                    continue;
                }
                String columnid2 = columntypes[0].getName();
                continue;
            }
            Object[] row = (Object[])list.get(i);
            for (int rowcol = 0; rowcol < columnids.size(); ++rowcol) {
                String columnid = (String)columnids.get(rowcol);
                if (this.extendedColViewMap.get(columnid) != null) {
                    columnid = (String)this.extendedColViewMap.get(columnid);
                }
                if (rowcol >= columntypes.length) continue;
                if ("timestamp".equals(columntypes[rowcol].getName())) {
                    ds.setDate(i, columnid, (Timestamp)row[rowcol]);
                    continue;
                }
                if ("big_decimal".equals(columntypes[rowcol].getName())) {
                    ds.setNumber(i, columnid, (BigDecimal)row[rowcol]);
                    continue;
                }
                if ("integer".equals(columntypes[rowcol].getName())) {
                    ds.setNumber(i, columnid, (Integer)row[rowcol]);
                    continue;
                }
                if ("long".equals(columntypes[rowcol].getName())) {
                    ds.setNumber(i, columnid, (Long)row[rowcol]);
                    continue;
                }
                if ("double".equals(columntypes[rowcol].getName())) {
                    ds.setNumber(i, columnid, (Double)row[rowcol]);
                    continue;
                }
                if ("string".equals(columntypes[rowcol].getName())) {
                    ds.setString(i, columnid, (String)row[rowcol]);
                    continue;
                }
                String entity = columntypes[rowcol].getName();
                if (row[rowcol] != null) {
                    String value = (String)((Map)row[rowcol]).get(entity + "id");
                    ds.setString(i, columnid, value);
                    continue;
                }
                ds.setValue(i, columnid, null);
            }
        }
        return ds;
    }

    private boolean isRequireTableQualifier(String columndef, String tableid) {
        ArrayList list;
        boolean b = true;
        if (columndef.indexOf(tableid + ".") == 0) {
            return false;
        }
        if (DetailTable.isDetailColumnWithQualifier(columndef)) {
            return false;
        }
        if (AdhocQueryService.isFunctionColumn(columndef)) {
            return false;
        }
        if (columndef.indexOf("_") > 0 && (list = this.adhocmetadata.getReverseFKChildList(tableid)) != null) {
            for (int i = 0; i < list.size(); ++i) {
                if (columndef.indexOf(list.get(i) + ".") != 0) continue;
                return false;
            }
        }
        return b;
    }

    private static boolean isFunctionColumn(String columndef) {
        return columndef.indexOf("(") >= 0 || columndef.indexOf("||") >= 0 || columndef.indexOf("*") >= 0 || columndef.indexOf("-") >= 0 || columndef.indexOf("/") >= 0 || columndef.indexOf("+") >= 0;
    }

    private static boolean isDotSyntaxColumn(String columndef) {
        return columndef.indexOf(".") > 0 && !AdhocQueryService.isFunctionColumn(columndef);
    }

    public String getErrorMessage() {
        return this.errormessage;
    }

    public String[] getResultColumns() {
        String[] resultcolumns = new String[this.resultColumnsList.size()];
        for (int i = 0; i < this.resultColumnsList.size(); ++i) {
            resultcolumns[i] = this.resultColumnsList.get(i);
        }
        return resultcolumns;
    }

    private String writeDataSet(DataSet ds, Writer fos, String currentHeader) throws IOException {
        StringBuilder headerColStr = new StringBuilder();
        String[] resultColumns = ds.getColumns();
        for (int c = 0; c < resultColumns.length; ++c) {
            if (c > 0) {
                headerColStr.append(",");
            }
            headerColStr.append("\"" + resultColumns[c] + "\"");
        }
        String header = headerColStr.toString();
        if (!header.equals(currentHeader)) {
            fos.write("##header##" + header);
            fos.write("\r\n");
        }
        for (int r = 0; r < ds.getRowCount(); ++r) {
            for (int c = 0; c < ds.getColumnCount(); ++c) {
                String value;
                if (c > 0) {
                    fos.write(",");
                }
                if ((value = ds.getValue(r, ds.getColumnId(c)).replaceAll("\"", "\"\"")).indexOf("\r\n") >= 0) {
                    value = StringUtil.replaceAll(value, "\r\n", "Char(10)Char(13)");
                } else if (value.indexOf("\r") >= 0) {
                    value = StringUtil.replaceAll(value, "\r", "Char(10)");
                } else if (value.indexOf("\n") >= 0) {
                    value = StringUtil.replaceAll(value, "\n", "Char(13)");
                }
                fos.write(value.indexOf(",") >= 0 ? "\"" + value + "\"" : value);
            }
            fos.write("\r\n");
        }
        return header;
    }
}

