/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.lucene.analysis.Analyzer
 *  org.apache.lucene.analysis.standard.StandardAnalyzer
 *  org.apache.lucene.analysis.util.CharArraySet
 *  org.apache.lucene.analysis.util.StopwordAnalyzerBase
 *  org.apache.lucene.index.DirectoryReader
 *  org.apache.lucene.index.IndexWriter
 *  org.apache.lucene.index.IndexWriterConfig
 *  org.apache.lucene.index.IndexWriterConfig$OpenMode
 *  org.apache.lucene.store.Directory
 *  org.apache.lucene.store.FSDirectory
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.modules.search.IndexRequest;
import com.labvantage.sapphire.modules.search.indexers.AttachmentIndexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import com.labvantage.sapphire.modules.search.indexers.DocumentIndexer;
import com.labvantage.sapphire.modules.search.indexers.NoteIndexer;
import com.labvantage.sapphire.modules.search.indexers.SDIIndexer;
import com.labvantage.sapphire.modules.search.indexers.TaskExecIndexer;
import com.labvantage.sapphire.modules.search.indexers.WorksheetIndexer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.IndexerTask;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Indexer {
    public static final String INDEXINGSTATUS = "indexingstatus";
    public static final String INDEXINGSTATUS_RUNNING = "running";
    public static final String INDEXINGSTATUS_STOPPED = "stopped";
    public static final String INDEXINGSTATUS_BACKLOG = "backlog";
    public static final String INDEXINGSTATUS_ERROR = "error";
    public static final String DEFAULT_INDEXLOCATION = "[sapphirehome]/indexer/index";
    public static final String DEFAULT_SEARCHBOXTEXT = "Search System";
    public static final String DEFAULT_SEARCHBOXPAGE = "rc?command=page&page=Search";
    public static final String DEFAULT_MAXCRAWL = "100";
    public static final String DEFAULT_BACKLOGPROCESSINGROWS = "1000";
    public static final int DEFAULT_POLLINTERVAL = 3600;
    public static final String INDEXFLAG_NONE = "";
    public static final String INDEXFLAG_UPDATE = "U";
    public static final String INDEXFLAG_DELETE = "D";
    public static final String INDEXFLAG_BACKLOG = "B";
    public static final String INDEXFLAG_ERROR = "E";
    public static final String LOGNAME = "INDEXER";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TYPE_SDI = "SDI";
    public static final String FIELD_TYPE_PRIMARY = "PRIMARY";
    public static final String FIELD_TYPE_ATTACHMENT = "ATTACHMENT";
    public static final String FIELD_TYPE_NOTE = "NOTE";
    public static final String FIELD_SDCID = "sdcid";
    public static final String FIELD_KEYID1 = "keyid1";
    public static final String FIELD_KEYID2 = "keyid2";
    public static final String FIELD_KEYID3 = "keyid3";
    public static final String FIELD_DESCCOL = "desccol";
    public static final String FIELD_CREATEBY = "createby";
    public static final String FIELD_MODBY = "modby";
    public static final String FIELD_CREATEDT = "createdt";
    public static final String FIELD_MODDT = "moddt";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_FILENAME = "filename";
    public static final String SUBST_MINUS = "__";
    public static final String SUBST_PLUS = "##";
    public static final String DATEFORMAT = "yyyyMMddhhmmss";
    public static final String ID_SEPARATOR = ";";
    private static TreeMap<String, Indexer> indexerMap = new TreeMap();
    private boolean remoteIndexer = false;
    private boolean indexing = false;
    private boolean searching = false;
    private boolean updatingIndex = false;
    private SapphireDatabase sapphireDatabase;
    private String jndiDataSourcePrefix;
    private String jndiLocalEJBPrefix;
    private String jndiPrefix;
    private String sapphireHome;
    private TreeMap<String, PropertyList> sdcPolicyMap = new TreeMap();
    private HashMap<String, PropertyList> sdcPropsMap = new HashMap();
    private Map<String, Set<String>> excludedSDCColMap = new HashMap<String, Set<String>>();
    private int pollInterval = 3600;
    private String maxCrawl = "100";
    private String analyzer = "org.apache.lucene.analysis.standard.StandardAnalyzer";
    private boolean extendStopWords = false;
    private boolean indexSensitiveData = false;
    private String[] stopWords;
    private PropertyList indexingPolicy;
    private PropertyList searchingPolicy;
    private File indexDir;
    private String connectionid = null;

    public static Indexer createInstance(String connectionid, SapphireDatabase sapphireDatabase, Configuration configuration) throws SapphireException {
        Indexer indexer = indexerMap.get(sapphireDatabase.getDatabaseId());
        if (indexer != null) {
            throw new SapphireException("Instance for database '" + sapphireDatabase.getDatabaseId() + "' already created.");
        }
        indexer = new Indexer();
        indexer.connectionid = connectionid;
        indexer.sapphireDatabase = sapphireDatabase;
        indexer.sapphireHome = configuration.getSapphireHome();
        indexer.jndiDataSourcePrefix = configuration.getJNDIDataSourcePrefix();
        indexer.jndiLocalEJBPrefix = configuration.getJNDILocalEJBPrefix();
        indexer.jndiPrefix = configuration.getJNDIEJBPrefix();
        indexerMap.put(sapphireDatabase.getDatabaseId(), indexer);
        Indexer.loadPolicy(indexer);
        return indexer;
    }

    public static Indexer refreshInstance(String databaseid) throws SapphireException {
        Indexer indexer = indexerMap.get(databaseid);
        if (indexer == null) {
            throw new SapphireException("Indexer instance for database '" + databaseid + "' has not been created.");
        }
        Indexer.loadPolicy(indexer);
        return indexer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadPolicy(Indexer indexer) {
        block29: {
            DBUtil dbu = new DBUtil();
            try {
                dbu.setConnection(indexer.sapphireDatabase.getDbms(), indexer.getDatabaseConnection());
                String searchPolicyXML = PropertyTreeUtil.getPropertyTreeValue(dbu, "SearchPolicy", true);
                String searchPolicyDefXML = PropertyTreeUtil.getPropertyTreeDefinition(dbu, "SearchPolicy", true);
                boolean exists = PropertyTreeUtil.propertyTreeExists(dbu, "SearchPolicy");
                PropertyTree searchPolicy = null;
                if (exists) {
                    searchPolicy = new PropertyTree();
                    searchPolicy.setValueXML(searchPolicyXML);
                    searchPolicy.setDefinitionXML(searchPolicyDefXML);
                }
                if (searchPolicy != null) {
                    PropertyList policy = searchPolicy.getNodePropertyList("Sapphire Custom", true);
                    indexer.indexingPolicy = policy.getPropertyList("indexing");
                    if (indexer.indexingPolicy != null) {
                        indexer.indexing = indexer.indexingPolicy.getProperty("enabled").equals("Y");
                        String indexlocation = StringUtil.replaceAll(indexer.indexingPolicy.getProperty("indexlocation", DEFAULT_INDEXLOCATION), "[sapphirehome]", indexer.sapphireHome);
                        indexer.indexDir = new File(indexlocation, indexer.sapphireDatabase.getDatabaseId());
                        indexer.indexDir.mkdirs();
                        indexer.analyzer = indexer.indexingPolicy.getProperty("analyzer", indexer.analyzer);
                        indexer.extendStopWords = indexer.indexingPolicy.getProperty("extendstopwords", "N").equals("Y");
                        indexer.stopWords = indexer.indexingPolicy.getProperty("stopwords").length() > 0 ? StringUtil.split(indexer.indexingPolicy.getProperty("stopwords"), " ") : null;
                        try {
                            indexer.pollInterval = Integer.parseInt(indexer.indexingPolicy.getProperty("pollinterval"));
                            indexer.maxCrawl = String.valueOf(Integer.parseInt(indexer.indexingPolicy.getProperty("maxcrawl")));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        indexer.indexSensitiveData = "Y".equals(indexer.indexingPolicy.getProperty("indexsensitivedata", "N"));
                    } else {
                        indexer.indexingPolicy = new PropertyList();
                        indexer.indexingPolicy.setProperty("enabled", "N");
                        indexer.indexingPolicy.setProperty("statusmessage", "No indexer policy defined");
                    }
                    indexer.searchingPolicy = policy.getPropertyList("searching");
                    if (indexer.searchingPolicy != null) {
                        indexer.searching = indexer.searchingPolicy.getProperty("enabled").equals("Y");
                    } else {
                        indexer.searchingPolicy = new PropertyList();
                        indexer.searchingPolicy.setProperty("enabled", "N");
                        indexer.searchingPolicy.setProperty("statusmessage", "No searching policy defined");
                    }
                    indexer.sdcPolicyMap.clear();
                    indexer.sdcPropsMap.clear();
                    PropertyListCollection sdcs = policy.getCollection("sdcs");
                    if (sdcs == null || sdcs.size() <= 0) break block29;
                    PropertyList maskingPolicy = new PropertyList();
                    if (!indexer.indexSensitiveData) {
                        String maskingPolicyXML = PropertyTreeUtil.getPropertyTreeValue(dbu, "MaskingPolicy", true);
                        String maskingPolicyDefXML = PropertyTreeUtil.getPropertyTreeDefinition(dbu, "MaskingPolicy", true);
                        boolean maskingPolicyExists = PropertyTreeUtil.propertyTreeExists(dbu, "MaskingPolicy");
                        if (maskingPolicyExists) {
                            PropertyTree maskingPolicyTree = new PropertyTree();
                            maskingPolicyTree.setValueXML(maskingPolicyXML);
                            maskingPolicyTree.setDefinitionXML(maskingPolicyDefXML);
                            maskingPolicy = maskingPolicyTree.getNodePropertyList("Sapphire Custom", true);
                            indexer.excludedSDCColMap = DataMaskUtil.getAllSensitivePrimaryColumns(maskingPolicy);
                        }
                    }
                    for (int i = 0; i < sdcs.size(); ++i) {
                        CallableStatement cs;
                        String callstmt;
                        PropertyList sdc = sdcs.getPropertyList(i);
                        String sdcid = sdc.getProperty(FIELD_SDCID);
                        if (!sdc.getProperty("enabled").equals("Y") || sdcid.equals("LV_WorksheetSection") || sdcid.equals("LV_WorksheetItem")) continue;
                        PropertyListCollection datasets = sdc.getCollection("datasets");
                        PropertyList indexDatasets = new PropertyList();
                        sdc.setProperty("indexdatasets", indexDatasets);
                        if (sdcid.equals("LV_Worksheet")) {
                            indexDatasets.setProperty("all", "Y");
                            sdc.setProperty("searchcolumns", new PropertyListCollection());
                            sdc.setProperty("resultcolumns", new PropertyListCollection());
                            sdc.setProperty("resultcolmaxlen", "80");
                            sdc.setProperty("resultcols", "1");
                            sdc.setProperty("linkpage", "WorksheetManager");
                            PropertyListCollection children = new PropertyListCollection();
                            sdc.setProperty("children", children);
                            PropertyList section = new PropertyList();
                            section.setProperty(FIELD_SDCID, "LV_WorksheetSection");
                            section.setProperty("childsdc", "Y");
                            section.setProperty("parentsdcid", "LV_Worksheet");
                            section.setProperty("parentkeycolid1", "worksheetid");
                            section.setProperty("parentkeycolid2", "worksheetversionid");
                            section.setProperty("parentdesccol", "worksheetname");
                            section.setProperty("singular", "section");
                            section.setProperty("matchtitle", "Section description match");
                            section.setProperty("linkpage", "WorksheetManager");
                            children.add(section);
                            indexer.sdcPolicyMap.put("LV_WorksheetSection", section);
                            PropertyList item = new PropertyList();
                            item.setProperty(FIELD_SDCID, "LV_WorksheetItem");
                            item.setProperty("childsdc", "Y");
                            item.setProperty("parentsdcid", "LV_Worksheet");
                            item.setProperty("parentkeycolid1", "worksheetid");
                            item.setProperty("parentkeycolid2", "worksheetversionid");
                            item.setProperty("parentdesccol", "worksheetname");
                            item.setProperty("singular", "control");
                            item.setProperty("matchtitle", "Control content match");
                            item.setProperty("linkpage", "WorksheetManager");
                            children.add(item);
                            indexer.sdcPolicyMap.put("LV_WorksheetItem", item);
                        } else if (datasets == null || datasets.size() == 0) {
                            indexDatasets.setProperty("all", "Y");
                            sdc.setProperty("searchcolumns", new PropertyListCollection());
                            sdc.setProperty("resultcolumns", new PropertyListCollection());
                            sdc.setProperty("resultcolmaxlen", "80");
                            sdc.setProperty("resultcols", "1");
                        } else {
                            for (int j = 0; j < datasets.size(); ++j) {
                                PropertyList dataset = datasets.getPropertyList(j);
                                String datasetname = dataset.getProperty("datasetname");
                                indexDatasets.setProperty(datasetname, "Y");
                                if (!datasetname.equals("primary") && !datasetname.equals("all")) continue;
                                PropertyListCollection searchColumns = dataset.getCollection("searchcolumns");
                                if (searchColumns == null) {
                                    searchColumns = new PropertyListCollection();
                                }
                                searchColumns.index("columnid");
                                sdc.setProperty("searchcolumns", searchColumns);
                                PropertyListCollection resultColumns = dataset.getCollection("resultcolumns");
                                if (resultColumns == null) {
                                    resultColumns = new PropertyListCollection();
                                }
                                resultColumns.index("columnid");
                                sdc.setProperty("resultcolumns", resultColumns);
                                try {
                                    sdc.setProperty("resultcolmaxlen", String.valueOf(Integer.parseInt(dataset.getProperty("resultcolmaxlen", "80"))));
                                }
                                catch (Exception e) {
                                    sdc.setProperty("resultcolmaxlen", "80");
                                }
                                try {
                                    sdc.setProperty("resultcols", String.valueOf(Integer.parseInt(dataset.getProperty("resultcols", "1"))));
                                    continue;
                                }
                                catch (Exception e) {
                                    sdc.setProperty("resultcols", "1");
                                }
                            }
                        }
                        indexer.sdcPolicyMap.put(sdcid, sdc);
                        if (indexer.requiresIndexing(sdcid, "primary")) {
                            callstmt = "{call lv_tab" + (dbu.isOracle() ? "." : "_") + "ModDtIndexSDC( ? ) }";
                            cs = dbu.prepareCall(callstmt);
                            cs.setString(1, sdcid);
                            cs.executeUpdate();
                        }
                        if (indexer.requiresIndexing(sdcid, "notes")) {
                            callstmt = "{call lv_tab" + (dbu.isOracle() ? "." : "_") + "ModDtIndex( ? ) }";
                            cs = dbu.prepareCall(callstmt);
                            cs.setString(1, "sdinote");
                            cs.executeUpdate();
                        }
                        if (!indexer.requiresIndexing(sdcid, "attachment")) continue;
                        callstmt = "{call lv_tab" + (dbu.isOracle() ? "." : "_") + "ModDtIndex( ? ) }";
                        cs = dbu.prepareCall(callstmt);
                        cs.setString(1, "sdiattachment");
                        cs.executeUpdate();
                    }
                    break block29;
                }
                throw new SapphireException();
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("Failed to load SearchPolicy. Reason: " + e.getMessage()), e);
                indexer.indexingPolicy = new PropertyList();
                indexer.indexingPolicy.setProperty("enabled", "N");
                indexer.indexingPolicy.setProperty("statusmessage", "SearchPolicy not defined in database");
                indexer.searchingPolicy = new PropertyList();
                indexer.searchingPolicy.setProperty("enabled", "N");
                indexer.searchingPolicy.setProperty("statusmessage", "SearchPolicy not defined in database");
                indexer.indexing = false;
                indexer.searching = false;
            }
            finally {
                dbu.reset();
                dbu.releaseConnection();
            }
        }
    }

    public static Indexer getInstance(String databaseid) throws SapphireException {
        Indexer indexer = indexerMap.get(databaseid);
        if (indexer == null) {
            throw new SapphireException("Indexer instance for database '" + databaseid + "' has not been created.");
        }
        return indexer;
    }

    public static Indexer[] getIndexers() {
        Indexer[] indexes = new Indexer[indexerMap.size()];
        int count = 0;
        Iterator<String> iterator = indexerMap.keySet().iterator();
        while (iterator.hasNext()) {
            indexes[count++] = indexerMap.get(iterator.next());
        }
        return indexes;
    }

    public static void index(ConnectionInfo connectionInfo, IndexRequest indexRequest) {
        block10: {
            try {
                if (indexRequest.isUnlock()) {
                    Indexer.unlock(connectionInfo);
                }
                if (indexRequest.isReset()) {
                    Indexer.reset(connectionInfo, true, true);
                }
                if (indexRequest.hasAllDatasets()) {
                    Indexer.indexSDI(connectionInfo, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3());
                } else {
                    String[] datasets = indexRequest.getDatasets();
                    for (int i = 0; i < datasets.length; ++i) {
                        if (datasets[i].equals("primary") && indexRequest.hasDataset("primary")) {
                            Indexer.indexPrimaryAndAttributes(connectionInfo, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3());
                            continue;
                        }
                        if (datasets[i].equals("notes") && indexRequest.hasDataset("notes")) {
                            Indexer.indexNote(connectionInfo, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3(), -1);
                            continue;
                        }
                        if (!datasets[i].equals("attachment") || !indexRequest.hasDataset("attachment")) continue;
                        Indexer.indexAttachment(connectionInfo, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3(), -1);
                    }
                }
                if (!indexRequest.isProcessImmediate()) break block10;
                Indexer indexer = Indexer.getInstance(connectionInfo.getDatabaseId());
                if (!indexer.isIndexLocked()) {
                    indexer.processQueue();
                    break block10;
                }
                throw new SapphireException("Indexer locked - giving up");
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("Failed to index. Reason: " + e.getMessage()), e);
            }
        }
    }

    public static void stopIndexing(ConnectionInfo connectionInfo) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            indexer.setIndexingStatus(INDEXINGSTATUS_STOPPED);
        }
    }

    public static void startIndexing(ConnectionInfo connectionInfo) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            indexer.setIndexingStatus(INDEXINGSTATUS_RUNNING);
        }
    }

    public static boolean startBacklogIndexing(ConnectionInfo connectionInfo) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            indexer.setIndexingStatus(INDEXINGSTATUS_BACKLOG);
            try {
                for (int attempts = 0; indexer.isIndexLocked() && attempts < 5; ++attempts) {
                    Thread.sleep(1000L);
                }
                return !indexer.isIndexLocked();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return false;
    }

    public static void stopBacklogIndexing(ConnectionInfo connectionInfo) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            String lastStatus = indexer.getLastIndexingStatus();
            indexer.setIndexingStatus(lastStatus.equals(INDEXINGSTATUS_BACKLOG) ? INDEXINGSTATUS_STOPPED : lastStatus);
        }
    }

    public static void crawl(ConnectionInfo connectionInfo) throws SapphireException {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            indexer.crawlDatabase(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setIndexingStatus(String indexingStatus) {
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            String lastStatus = INDEXFLAG_NONE;
            dbu.createPreparedResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = ?", new Object[]{INDEXINGSTATUS});
            if (dbu.getNext()) {
                lastStatus = dbu.getValue("propertyvalue");
            }
            if (dbu.executePreparedUpdate("UPDATE sysconfig SET propertyvalue = ? WHERE propertyid = 'lastindexingstatus'", new Object[]{lastStatus}) != 1) {
                dbu.executePreparedUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'lastindexingstatus', ? )", new Object[]{lastStatus});
            }
            if (dbu.executePreparedUpdate("UPDATE sysconfig SET propertyvalue = ? WHERE propertyid = 'indexingstatus'", new Object[]{indexingStatus}) != 1) {
                dbu.executePreparedUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'indexingstatus', ? )", new Object[]{indexingStatus});
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to set indexing status in sysconfig. Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
        }
    }

    public String getCurrentIndexingStatus() {
        return this.getIndexingStatus(false);
    }

    public String getLastIndexingStatus() {
        return this.getIndexingStatus(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String getIndexingStatus(boolean last) {
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            dbu.createPreparedResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = ?", new Object[]{(last ? "last" : INDEXFLAG_NONE) + INDEXINGSTATUS});
            if (dbu.getNext()) {
                String string = dbu.getValue("propertyvalue");
                return string;
            }
            String string = INDEXFLAG_NONE;
            return string;
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to get indexing status in sysconfig. Reason: " + e.getMessage()), e);
            String string = INDEXFLAG_NONE;
            return string;
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean indexExists() {
        DirectoryReader indexReader = null;
        try {
            indexReader = DirectoryReader.open((Directory)this.getIndexDirectory());
            boolean bl = true;
            return bl;
        }
        catch (Exception e) {
            boolean bl = false;
            return bl;
        }
        finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public static void indexSDI(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3) {
        Indexer.indexSDI(connectionInfo, sdcid, keyid1, keyid2, keyid3, INDEXFLAG_UPDATE);
    }

    public static void indexSDI(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3, String indexFlag) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing()) {
            String[] keyid1list = StringUtil.split(keyid1, ID_SEPARATOR);
            String[] keyid2list = keyid2.length() > 0 && !keyid2.equals("(null)") ? StringUtil.split(keyid2, ID_SEPARATOR) : new String[]{};
            String[] keyid3list = keyid3.length() > 0 && !keyid3.equals("(null)") ? StringUtil.split(keyid3, ID_SEPARATOR) : new String[]{};
            for (int i = 0; i < keyid1list.length; ++i) {
                indexer.addToQueue(sdcid, "all", "SDI;" + sdcid + ID_SEPARATOR + keyid1list[i] + ID_SEPARATOR + (keyid2list.length > 0 ? keyid2list[i] : INDEXFLAG_NONE) + ID_SEPARATOR + (keyid3list.length > 0 ? keyid3list[i] : INDEXFLAG_NONE), indexFlag);
            }
        }
    }

    public static void removeSDI(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing()) {
            String[] keyid1list = StringUtil.split(keyid1, ID_SEPARATOR);
            String[] keyid2list = keyid2.length() > 0 && !keyid2.equals("(null)") ? StringUtil.split(keyid2, ID_SEPARATOR) : new String[]{};
            String[] keyid3list = keyid3.length() > 0 && !keyid3.equals("(null)") ? StringUtil.split(keyid3, ID_SEPARATOR) : new String[]{};
            for (int i = 0; i < keyid1list.length; ++i) {
                indexer.addToQueue(sdcid, INDEXFLAG_NONE, "SDI;" + sdcid + ID_SEPARATOR + keyid1list[i] + ID_SEPARATOR + (keyid2list.length > 0 ? keyid2list[i] : INDEXFLAG_NONE) + ID_SEPARATOR + (keyid3list.length > 0 ? keyid3list[i] : INDEXFLAG_NONE), INDEXFLAG_DELETE);
            }
        }
    }

    public static void indexPrimaryAndAttributes(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing()) {
            String[] keyid1list = StringUtil.split(keyid1, ID_SEPARATOR);
            String[] keyid2list = keyid2.length() > 0 && !keyid2.equals("(null)") ? StringUtil.split(keyid2, ID_SEPARATOR) : new String[]{};
            String[] keyid3list = keyid3.length() > 0 && !keyid3.equals("(null)") ? StringUtil.split(keyid3, ID_SEPARATOR) : new String[]{};
            for (int i = 0; i < keyid1list.length; ++i) {
                String k3;
                String k1 = keyid1list[i];
                String k2 = keyid2list.length > 0 ? keyid2list[i] : INDEXFLAG_NONE;
                String string = k3 = keyid3list.length > 0 ? keyid3list[i] : INDEXFLAG_NONE;
                if (Indexer.sdiQueueEntryExists(indexer, sdcid, k1, k2, k3)) continue;
                indexer.addToQueue(sdcid, "primary", "PRIMARY;" + sdcid + ID_SEPARATOR + k1 + ID_SEPARATOR + k2 + ID_SEPARATOR + k3, INDEXFLAG_UPDATE);
            }
        }
    }

    public static void indexAttachment(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing() && !Indexer.sdiQueueEntryExists(indexer, sdcid, keyid1, keyid2, keyid3)) {
            indexer.addToQueue(sdcid, "attachment", "ATTACHMENT;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + keyid2 + ID_SEPARATOR + keyid3 + ID_SEPARATOR + attachmentnum, INDEXFLAG_UPDATE);
        }
    }

    public static void removeAttachment(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing()) {
            indexer.addToQueue(sdcid, "attachment", "ATTACHMENT;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + keyid2 + ID_SEPARATOR + keyid3 + ID_SEPARATOR + attachmentnum, INDEXFLAG_DELETE);
        }
    }

    public static void indexNote(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3, int notenum) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing() && !Indexer.sdiQueueEntryExists(indexer, sdcid, keyid1, keyid2, keyid3)) {
            indexer.addToQueue(sdcid, "notes", "NOTE;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + keyid2 + ID_SEPARATOR + keyid3 + ID_SEPARATOR + notenum, INDEXFLAG_UPDATE);
        }
    }

    public static void removeNote(ConnectionInfo connectionInfo, String sdcid, String keyid1, String keyid2, String keyid3, int notenum) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null && indexer.isIndexing()) {
            indexer.addToQueue(sdcid, "notes", "NOTE;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + keyid2 + ID_SEPARATOR + keyid3 + ID_SEPARATOR + notenum, INDEXFLAG_DELETE);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void reset(ConnectionInfo connectionInfo, boolean index, boolean map) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            IndexWriter indexWriter = null;
            DBUtil dbu = new DBUtil();
            try {
                if (index) {
                    indexWriter = indexer.getIndexWriter(true);
                    indexWriter.deleteAll();
                    indexWriter.deleteUnusedFiles();
                    Trace.log("Reset Lucene index");
                }
                if (map) {
                    dbu.setConnection(connectionInfo.getDbms(), indexer.getDatabaseConnection());
                    int deletes = dbu.executeUpdate("DELETE FROM indexmap");
                    Trace.log("Deleted " + deletes + " entries from INDEXMAP table");
                }
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("Failed to reset index. Reason: " + e.getMessage()), e);
            }
            finally {
                dbu.reset();
                dbu.releaseConnection();
                if (indexWriter != null) {
                    try {
                        indexWriter.close();
                    }
                    catch (IOException iOException) {}
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void resetPending(ConnectionInfo connectionInfo, String indexflag) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            DBUtil dbu = new DBUtil();
            try {
                dbu.setConnection(connectionInfo.getDbms(), indexer.getDatabaseConnection());
                dbu.executePreparedUpdate("DELETE FROM indexmap WHERE indexflag = ?", indexflag);
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("Failed to delete index queue items. Reason: " + e.getMessage()), e);
            }
            finally {
                dbu.reset();
                dbu.releaseConnection();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setLastIndexDt(ConnectionInfo connectionInfo, Calendar lastindexdt) {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            DBUtil dbu = new DBUtil();
            try {
                dbu.setConnection(connectionInfo.getDbms(), indexer.getDatabaseConnection());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dbu.executePreparedUpdate("UPDATE sysconfig SET propertyvalue = ? WHERE propertyid = 'lastindexdt'", new Object[]{sdf.format(lastindexdt.getTime())});
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("Failed to update last index date. Reason: " + e.getMessage()), e);
            }
            finally {
                dbu.reset();
                dbu.releaseConnection();
            }
        }
    }

    public static void unlock(ConnectionInfo connectionInfo) throws Exception {
        Indexer indexer = indexerMap.get(connectionInfo.getDatabaseId());
        if (indexer != null) {
            // empty if block
        }
    }

    private Connection getDatabaseConnection() throws SQLException, SapphireException {
        return ServiceLocator.getInstance(this.jndiDataSourcePrefix, this.jndiLocalEJBPrefix, this.jndiPrefix).getDataSource(this.sapphireDatabase.getJndiname()).getConnection();
    }

    public IndexerTask getIndexerTask() {
        IndexerTask indexerTask = new IndexerTask();
        indexerTask.setDatabaseid(this.sapphireDatabase.getDatabaseId());
        indexerTask.setInterval(this.pollInterval);
        return indexerTask;
    }

    public boolean isIndexing() {
        return this.indexing;
    }

    public boolean isRemoteIndexer() {
        return this.remoteIndexer;
    }

    public boolean isSearching() {
        return this.searching;
    }

    public boolean requiresIndexing(String sdcid, String datasetname) {
        PropertyList sdc = this.sdcPolicyMap.get(sdcid);
        if (sdc != null) {
            PropertyListCollection datasets = sdc.getCollection("datasets");
            return datasets == null || datasets.size() == 0 || sdc.getPropertyList("indexdatasets").containsKey("all") || sdc.getPropertyList("indexdatasets").containsKey(datasetname);
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean sdiQueueEntryExists(Indexer indexer, String sdcid, String keyid1, String keyid2, String keyid3) {
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(indexer.sapphireDatabase.getDbms(), indexer.getDatabaseConnection());
            String indexitem = "SDI;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + keyid2 + ID_SEPARATOR + keyid3;
            dbu.createPreparedResultSet("SELECT indexitem FROM indexmap WHERE indexitem = ? AND indexflag = ?", new Object[]{indexitem, INDEXFLAG_UPDATE});
            if (dbu.getNext()) {
                boolean bl = true;
                return bl;
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to check if SDI queue entry exists for " + sdcid + " " + keyid1 + ". Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
        }
        return false;
    }

    private boolean sdiQueueEntryExists(DBUtil dbu, String sdcid, String keyid1, String keyid2, String keyid3) {
        try {
            PropertyList sdcProps = this.getSDCProps(dbu, sdcid);
            if (sdcProps != null) {
                int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                String indexitem = "SDI;" + sdcid + ID_SEPARATOR + keyid1 + ID_SEPARATOR + (keycols >= 2 ? keyid2 : INDEXFLAG_NONE) + ID_SEPARATOR + (keycols >= 3 ? keyid3 : INDEXFLAG_NONE);
                dbu.createPreparedResultSet("sdiQueueEntryExists", "SELECT indexitem FROM indexmap WHERE indexitem = ? AND indexflag = ?", new Object[]{indexitem, INDEXFLAG_UPDATE});
                if (dbu.getNext("sdiQueueEntryExists")) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to check if SDI queue entry exists for " + sdcid + " " + keyid1 + ". Reason: " + e.getMessage()), e);
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addToQueue(String sdcid, String datasetname, String id, String mode) {
        DBUtil dbu = new DBUtil();
        try {
            if (sdcid.equals("(all)") || sdcid.equals("SDINote") || datasetname.equals("(all)") || this.requiresIndexing(sdcid, datasetname)) {
                dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
                dbu.executePreparedUpdate("INSERT INTO indexmap ( indexitem, indexflag, indexdt ) VALUES ( ?, ?, ? )", new Object[]{id, mode, DateTimeUtil.getNowTimestamp()});
            }
        }
        catch (Exception e) {
            if (this.sapphireDatabase.isOracle() && e.getMessage().contains("ORA-00001") || this.sapphireDatabase.isSqlServer() && e.getMessage().contains("duplicate key")) {
                if (mode.length() > 0) {
                    try {
                        dbu.executePreparedUpdate("UPDATE indexmap SET indexflag = ? WHERE indexitem = ?", new Object[]{mode, id});
                    }
                    catch (Exception e1) {
                        Trace.logError(LOGNAME, (Object)("Failed to update " + sdcid + " " + datasetname + " indexer entry. Reason: " + e.getMessage()), e);
                    }
                } else {
                    Trace.logDebug(LOGNAME, "Duplicate indexitem '" + id + "' added to indexmap - ignoring");
                }
            } else {
                Trace.logError(LOGNAME, (Object)("Failed to add " + sdcid + " " + datasetname + " indexer entry. Reason: " + e.getMessage()), e);
            }
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
        }
    }

    public boolean crawlDatabase() throws SapphireException {
        return this.crawlDatabase(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean crawlDatabase(boolean testMode) throws SapphireException {
        if (!this.updatingIndex && this.getCurrentIndexingStatus().equals(INDEXINGSTATUS_RUNNING) || testMode) {
            try {
                this.updatingIndex = true;
                Trace.setStartCodeBlock("Indexer.crawlDatabase");
                IndexWriter indexWriter = null;
                DBUtil dbu = new DBUtil();
                try {
                    String[] sdcs = this.getIndexedSDCs();
                    dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
                    DateTimeUtil dtu = new DateTimeUtil();
                    Timestamp lastindexdt = null;
                    dbu.createPreparedResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = ?", new Object[]{"lastindexdt"});
                    if (dbu.getNext()) {
                        lastindexdt = dtu.getTimestamp(dbu.getValue("propertyvalue"));
                    } else {
                        lastindexdt = dtu.getTimestamp("now");
                        dbu.executePreparedUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'lastindexdt', ? )", new Object[]{lastindexdt});
                    }
                    Calendar startindexdt = DateTimeUtil.getNowCalendar();
                    int primarycount = 0;
                    int notecount = 0;
                    int attachmentcount = 0;
                    for (int i = 0; i < sdcs.length; ++i) {
                        String sdcid = sdcs[i];
                        PropertyList sdcProps = this.getSDCProps(dbu, sdcid);
                        PropertyList sdcPolicy = this.getSDCPolicy(sdcid);
                        if (sdcProps == null || sdcPolicy == null) continue;
                        if (this.requiresIndexing(sdcid, "primary")) {
                            int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                            String tableid = sdcProps.getProperty("tableid");
                            String keycolid1 = sdcProps.getProperty("keycolid1");
                            String keycolid2 = sdcProps.getProperty("keycolid2");
                            String keycolid3 = sdcProps.getProperty("keycolid3");
                            String select = keycolid1 + (keycols >= 2 ? ", " + keycolid2 : INDEXFLAG_NONE) + (keycols >= 3 ? ", " + keycolid3 : INDEXFLAG_NONE);
                            String where = "(" + tableid + ".moddt > ?)";
                            String extendedWhere = sdcPolicy.getProperty("extended");
                            where = where + (extendedWhere.length() > 0 ? " AND (" + extendedWhere + ") " : INDEXFLAG_NONE);
                            dbu.createPreparedResultSet("crawlset", "SELECT " + select + " FROM " + tableid + " WHERE " + where + " ORDER BY " + tableid + ".moddt ASC", lastindexdt);
                            while (dbu.getNext("crawlset")) {
                                ++primarycount;
                                if (this.sdiQueueEntryExists(dbu, sdcid, dbu.getValue("crawlset", keycolid1), keycols >= 2 ? dbu.getValue("crawlset", keycolid2) : INDEXFLAG_NONE, keycols >= 3 ? dbu.getValue("crawlset", keycolid3) : INDEXFLAG_NONE)) continue;
                                this.addToQueue(sdcid, "primary", "PRIMARY;" + sdcid + ID_SEPARATOR + dbu.getValue("crawlset", keycolid1) + ID_SEPARATOR + (keycols >= 2 ? dbu.getValue("crawlset", keycolid2) : INDEXFLAG_NONE) + ID_SEPARATOR + (keycols >= 3 ? dbu.getValue("crawlset", keycolid2) : INDEXFLAG_NONE), INDEXFLAG_UPDATE);
                            }
                        }
                        if (this.requiresIndexing(sdcid, "notes")) {
                            String where = "(sdinote.moddt > ?)";
                            dbu.createPreparedResultSet("crawlset", "SELECT sdcid, keyid1, keyid2, keyid3, notenum FROM sdinote WHERE sdcid = ? AND " + where, new Object[]{sdcid, lastindexdt});
                            while (dbu.getNext("crawlset")) {
                                if (indexWriter == null) {
                                    indexWriter = this.getIndexWriter();
                                }
                                ++notecount;
                                if (this.sdiQueueEntryExists(dbu, sdcid, dbu.getValue("crawlset", FIELD_KEYID1), dbu.getValue("crawlset", FIELD_KEYID2), dbu.getValue("crawlset", FIELD_KEYID3))) continue;
                                this.addToQueue(sdcid, "notes", "NOTE;" + sdcid + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID1) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID2) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID3) + ID_SEPARATOR + dbu.getValue("crawlset", "notenum"), INDEXFLAG_UPDATE);
                            }
                        }
                        if (!this.requiresIndexing(sdcid, "attachment")) continue;
                        String indexitem = "'ATTACHMENT;" + sdcid + ";' " + (dbu.isOracle() ? "||" : "+") + " keyid1 " + (dbu.isOracle() ? "||" : "+") + " ';' " + (dbu.isOracle() ? "||" : "+") + " keyid2 " + (dbu.isOracle() ? "||" : "+") + " ';' " + (dbu.isOracle() ? "||" : "+") + " keyid3 " + (dbu.isOracle() ? "||" : "+") + " ';' " + (dbu.isOracle() ? "||attachmentnum" : "+cast(attachmentnum as NVARCHAR(20))");
                        String where = "(sdiattachment.moddt > ?)";
                        dbu.createPreparedResultSet("crawlset", "SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum FROM sdiattachment WHERE sdcid = ? AND " + where, new Object[]{sdcid, lastindexdt});
                        while (dbu.getNext("crawlset")) {
                            if (indexWriter == null) {
                                indexWriter = this.getIndexWriter();
                            }
                            ++attachmentcount;
                            if (this.sdiQueueEntryExists(dbu, sdcid, dbu.getValue("crawlset", FIELD_KEYID1), dbu.getValue("crawlset", FIELD_KEYID2), dbu.getValue("crawlset", FIELD_KEYID3))) continue;
                            this.addToQueue(sdcid, "notes", "ATTACHMENT;" + sdcid + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID1) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID2) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID3) + ID_SEPARATOR + dbu.getValue("crawlset", "attachmentnum"), INDEXFLAG_UPDATE);
                        }
                        where = "(EXISTS (SELECT indexitem FROM indexmap WHERE indexitem = " + indexitem + " AND attachmenttypeflag='R' ) )";
                        if (dbu.isOracle()) {
                            dbu.createPreparedResultSet("crawlset", "SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum, filename, ( SELECT attachmentfilesize FROM indexmap WHERE indexitem = " + indexitem + " ) filesize FROM sdiattachment WHERE sdcid = ? AND " + where + " AND ROWNUM < " + this.maxCrawl, new Object[]{sdcid});
                        } else {
                            dbu.createPreparedResultSet("crawlset", "SELECT TOP " + this.maxCrawl + " sdcid, keyid1, keyid2, keyid3, attachmentnum, filename, ( SELECT attachmentfilesize FROM indexmap WHERE indexitem = " + indexitem + " ) filesize FROM sdiattachment WHERE sdcid = ? AND " + where, new Object[]{sdcid});
                        }
                        while (dbu.getNext("crawlset")) {
                            if (indexWriter == null) {
                                indexWriter = this.getIndexWriter();
                            }
                            String attachmentfile = dbu.getValue("crawlset", FIELD_FILENAME);
                            int attachmentfilesize = dbu.getInt("crawlset", "filesize");
                            File f = new File(attachmentfile);
                            if (!f.exists() || f.length() == (long)attachmentfilesize) continue;
                            ++attachmentcount;
                            if (this.sdiQueueEntryExists(dbu, sdcid, dbu.getValue("crawlset", FIELD_KEYID1), dbu.getValue("crawlset", FIELD_KEYID2), dbu.getValue("crawlset", FIELD_KEYID3))) continue;
                            this.addToQueue(sdcid, "notes", "ATTACHMENT;" + sdcid + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID1) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID2) + ID_SEPARATOR + dbu.getValue("crawlset", FIELD_KEYID3) + ID_SEPARATOR + dbu.getValue("crawlset", "attachmentnum"), INDEXFLAG_UPDATE);
                        }
                    }
                    if (indexWriter == null) {
                        Trace.logInfo(LOGNAME, "No entries found in crawl of database '" + this.sapphireDatabase.getDatabaseId() + "'");
                    } else {
                        Trace.logInfo(LOGNAME, "Added " + primarycount + " primary SDI records to index queue found crawling database '" + this.sapphireDatabase.getDatabaseId() + "'");
                        Trace.logInfo(LOGNAME, "Added " + notecount + " notes to index queue found crawling database '" + this.sapphireDatabase.getDatabaseId() + "'");
                        Trace.logInfo(LOGNAME, "Added " + attachmentcount + " attachments to index queue found crawling database '" + this.sapphireDatabase.getDatabaseId() + "'");
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dbu.executePreparedUpdate("UPDATE sysconfig SET propertyvalue = ? WHERE propertyid = 'lastindexdt'", new Object[]{sdf.format(startindexdt.getTime())});
                }
                catch (Exception e) {
                    Trace.logError(LOGNAME, (Object)("Failed to crawl database '" + this.sapphireDatabase.getDatabaseId() + "'. Reason: " + e.getMessage()), e);
                }
                finally {
                    dbu.reset();
                    dbu.releaseConnection();
                    if (indexWriter != null) {
                        try {
                            indexWriter.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                Trace.setEndCodeBlock("Indexer.crawlDatabase");
                this.processQueue();
            }
            finally {
                this.updatingIndex = false;
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createBacklog(String sdcid, String aftercreatedt, String extendedwhere, int rowcount, String orderby) throws SapphireException {
        Trace.setStartCodeBlock("Indexer.createBacklog");
        DBUtil dbu = new DBUtil();
        try {
            String[] sdcs = this.getIndexedSDCs();
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            orderby = orderby != null && orderby.length() > 0 ? orderby : "createdt ASC";
            DateTimeUtil dtu = new DateTimeUtil();
            Timestamp createdt = aftercreatedt != null && aftercreatedt.length() > 0 ? dtu.getTimestamp(aftercreatedt) : null;
            for (int i = 0; i < sdcs.length; ++i) {
                Object[] objectArray;
                if (sdcid.length() != 0 && !sdcid.equals(sdcs[i])) continue;
                PropertyList sdcProps = this.getSDCProps(dbu, sdcs[i]);
                PropertyList sdcPolicy = this.getSDCPolicy(sdcs[i]);
                if (sdcProps == null || sdcPolicy == null) continue;
                int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                String tableid = sdcProps.getProperty("tableid");
                String keycolid1 = sdcProps.getProperty("keycolid1");
                String keycolid2 = sdcProps.getProperty("keycolid2");
                String keycolid3 = sdcProps.getProperty("keycolid3");
                String indexitem = "'SDI;" + sdcs[i] + ";' " + (dbu.isOracle() ? "||" : "+") + " " + keycolid1 + " " + (dbu.isOracle() ? "||" : "+") + " ';' " + (dbu.isOracle() ? "||" : "+") + " " + (keycols >= 2 ? keycolid2 : "''") + " " + (dbu.isOracle() ? "||" : "+") + " ';'" + (keycols >= 3 ? " " + (dbu.isOracle() ? "||" : "+") + " " + keycolid3 : INDEXFLAG_NONE);
                String where = "(NOT EXISTS (SELECT indexitem FROM indexmap WHERE indexitem = " + indexitem + ") )" + (createdt != null ? " AND ( " + tableid + ".createdt > ? )" : INDEXFLAG_NONE) + (extendedwhere.length() > 0 ? " AND (" + extendedwhere + ")" : INDEXFLAG_NONE);
                Timestamp now = DateTimeUtil.getNowTimestamp();
                if (rowcount > -1) {
                    Object[] objectArray2;
                    String sql = INDEXFLAG_NONE;
                    sql = dbu.isOracle() ? "INSERT INTO indexmap ( indexitem, indexdt, indexflag ) SELECT * FROM ( SELECT " + indexitem + ", ?, '" + INDEXFLAG_BACKLOG + "' FROM " + tableid + " WHERE " + where + " ORDER BY " + orderby + " ) WHERE ROWNUM < " + (rowcount + 1) : "INSERT INTO indexmap ( indexitem, indexdt, indexflag ) SELECT TOP " + rowcount + " " + indexitem + ", ?, '" + INDEXFLAG_BACKLOG + "' FROM " + tableid + " WHERE " + where + " ORDER BY " + orderby;
                    if (createdt != null) {
                        Object[] objectArray3 = new Object[2];
                        objectArray3[0] = now;
                        objectArray2 = objectArray3;
                        objectArray3[1] = createdt;
                    } else {
                        Object[] objectArray4 = new Object[1];
                        objectArray2 = objectArray4;
                        objectArray4[0] = now;
                    }
                    dbu.executePreparedUpdate(sql, objectArray2);
                    continue;
                }
                String string = "INSERT INTO indexmap ( indexitem, indexdt, indexflag ) SELECT " + indexitem + ", ?, '" + INDEXFLAG_BACKLOG + "' FROM " + tableid + " WHERE " + where + " ORDER BY " + orderby;
                if (createdt != null) {
                    Object[] objectArray5 = new Object[2];
                    objectArray5[0] = now;
                    objectArray = objectArray5;
                    objectArray5[1] = createdt;
                } else {
                    Object[] objectArray6 = new Object[1];
                    objectArray = objectArray6;
                    objectArray6[0] = now;
                }
                dbu.executePreparedUpdate(string, objectArray);
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to process backlog for database '" + this.sapphireDatabase.getDatabaseId() + "'. Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
        }
        Trace.setEndCodeBlock("Indexer.createBacklog");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void processBacklog(int rowcount) throws SapphireException {
        Trace.setStartCodeBlock("Indexer.processBacklog" + rowcount);
        IndexWriter indexWriter = null;
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            SafeSQL safeSQL = new SafeSQL();
            String sql = dbu.isOracle() ? "SELECT indexitem FROM ( SELECT indexitem FROM indexmap WHERE indexflag = " + safeSQL.addVar(INDEXFLAG_BACKLOG) + " ORDER BY indexdt ) WHERE ROWNUM < " + safeSQL.addVar(String.valueOf(rowcount + 1)) : "SELECT TOP " + rowcount + " indexitem FROM indexmap WHERE indexflag = " + safeSQL.addVar(INDEXFLAG_BACKLOG) + " ORDER BY indexdt";
            dbu.createPreparedResultSet("indexmap", sql, safeSQL.getValues());
            int count = 0;
            while (dbu.getNext("indexmap")) {
                if (indexWriter == null) {
                    indexWriter = this.getIndexWriter();
                }
                ++count;
                String indexItem = dbu.getString("indexmap", "indexitem");
                String[] indexItemParts = StringUtil.split(indexItem, ID_SEPARATOR);
                String sdcid = indexItemParts.length >= 2 ? indexItemParts[1] : INDEXFLAG_NONE;
                String keyid1 = indexItemParts.length >= 3 ? indexItemParts[2] : INDEXFLAG_NONE;
                String keyid2 = indexItemParts.length >= 4 ? indexItemParts[3] : "(null)";
                String keyid3 = indexItemParts.length >= 5 ? indexItemParts[4] : "(null)";
                this.index(indexWriter, dbu, sdcid, keyid1, keyid2, keyid3, indexItemParts);
                dbu.executePreparedUpdate("DELETE FROM indexmap WHERE indexitem = ? AND indexflag = ?", new Object[]{indexItem, INDEXFLAG_BACKLOG});
            }
            if (indexWriter == null) {
                Trace.logDebug(LOGNAME, "No backlog entries found on indexmap");
            } else {
                Trace.logDebug(LOGNAME, "Processed " + count + " backlog items");
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to process backlog for database '" + this.sapphireDatabase.getDatabaseId() + "'. Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                }
                catch (IOException iOException) {}
            }
        }
        Trace.setEndCodeBlock("Indexer.processBacklog" + rowcount);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean processQueue() throws SapphireException {
        Trace.setStartCodeBlock("Indexer.processQueue");
        IndexWriter indexWriter = null;
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            int processQueueTries = 0;
            int remainingQueueItemsCount = 0;
            int processedCount = 0;
            do {
                Trace.logDebug(INDEXFLAG_NONE);
                dbu.createPreparedResultSet("indexmap", "SELECT indexitem, indexflag FROM indexmap WHERE indexflag IN ( ?, ? ) ORDER BY indexdt", new Object[]{INDEXFLAG_UPDATE, INDEXFLAG_DELETE});
                while (dbu.getNext("indexmap")) {
                    String keyid3;
                    if (indexWriter == null) {
                        indexWriter = this.getIndexWriter();
                    }
                    ++processedCount;
                    String indexflag = dbu.getValue("indexmap", "indexflag");
                    boolean delete = indexflag.equals(INDEXFLAG_DELETE);
                    String indexItem = dbu.getString("indexmap", "indexitem");
                    String[] indexItemParts = StringUtil.split(indexItem, ID_SEPARATOR);
                    String sdcid = indexItemParts.length >= 2 ? indexItemParts[1] : INDEXFLAG_NONE;
                    String keyid1 = indexItemParts.length >= 3 ? indexItemParts[2] : INDEXFLAG_NONE;
                    String keyid2 = indexItemParts.length >= 4 ? indexItemParts[3] : "(null)";
                    String string = keyid3 = indexItemParts.length >= 5 ? indexItemParts[4] : "(null)";
                    if (sdcid.equals("(all)")) {
                        String[] sdcs = this.getIndexedSDCs();
                        for (int i = 0; i < sdcs.length; ++i) {
                            if (delete) {
                                this.delete(indexWriter, dbu, sdcs[i], keyid1, keyid2, keyid3, indexItemParts);
                                continue;
                            }
                            this.index(indexWriter, dbu, sdcs[i], keyid1, keyid2, keyid3, indexItemParts);
                        }
                    } else if (delete) {
                        this.delete(indexWriter, dbu, sdcid, keyid1, keyid2, keyid3, indexItemParts);
                    } else {
                        this.index(indexWriter, dbu, sdcid, keyid1, keyid2, keyid3, indexItemParts);
                    }
                    dbu.executePreparedUpdate("DELETE FROM indexmap WHERE indexitem = ? AND indexflag = ?", new Object[]{indexItem, indexflag});
                }
                ++processQueueTries;
                remainingQueueItemsCount = dbu.getPreparedCount("SELECT COUNT(indexitem) FROM indexmap WHERE indexflag IN (?,?)", new Object[]{INDEXFLAG_UPDATE, INDEXFLAG_DELETE});
                if (remainingQueueItemsCount <= 0) continue;
                if (processQueueTries > 5) {
                    Trace.logDebug("More Items found in Queue. Reached max Process Queue Re-run: " + processQueueTries + ". No more tries now. Will wait for next poll.");
                    continue;
                }
                Trace.logDebug("More Items found in Queue. Start processing them. Process Queue Re-run#: " + processQueueTries);
            } while (remainingQueueItemsCount > 0 && processQueueTries <= 5);
            if (indexWriter == null) {
                Trace.logDebug(LOGNAME, "No entries found on indexmap");
            } else {
                Trace.logDebug(LOGNAME, "Processed " + processedCount + " indexmap items");
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to processQueue for database '" + this.sapphireDatabase.getDatabaseId() + "'. Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                }
                catch (IOException iOException) {}
            }
        }
        Trace.setEndCodeBlock("Indexer.processQueue");
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void index(IndexRequest indexRequest) {
        Trace.setStartCodeBlock("Indexer.index");
        IndexWriter indexWriter = null;
        DBUtil dbu = new DBUtil();
        try {
            sapphire.util.ConnectionInfo connectionInfo = new sapphire.util.ConnectionInfo(this.sapphireDatabase);
            if (indexRequest.isUnlock()) {
                Indexer.unlock(connectionInfo);
            }
            if (indexRequest.isReset()) {
                Indexer.reset(connectionInfo, true, true);
            }
            dbu.setConnection(this.sapphireDatabase.getDbms(), this.getDatabaseConnection());
            indexWriter = this.getIndexWriter();
            if (indexRequest.hasAllDatasets()) {
                this.indexSDI(dbu, indexWriter, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3());
            } else {
                String[] datasets = indexRequest.getDatasets();
                for (int i = 0; i < datasets.length; ++i) {
                    if (datasets[i].equals("primary") && indexRequest.hasDataset("primary")) {
                        this.indexSDIPrimary(dbu, indexWriter, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3());
                        continue;
                    }
                    if (datasets[i].equals("notes") && indexRequest.hasDataset("notes")) {
                        this.indexSDINote(dbu, indexWriter, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3(), -1);
                        continue;
                    }
                    if (!datasets[i].equals("attachment") || !indexRequest.hasDataset("attachment")) continue;
                    this.indexSDIAttachment(dbu, indexWriter, indexRequest.getSdcid(), indexRequest.getKeyid1(), indexRequest.getKeyid2(), indexRequest.getKeyid3(), -1);
                }
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to index. Reason: " + e.getMessage()), e);
        }
        finally {
            dbu.reset();
            dbu.releaseConnection();
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                }
                catch (IOException iOException) {}
            }
        }
        Trace.setEndCodeBlock("Indexer.index");
    }

    private void index(IndexWriter indexWriter, DBUtil dbu, String sdcid, String keyid1, String keyid2, String keyid3, String[] indexItemParts) {
        if (indexItemParts[0].equals(FIELD_TYPE_SDI)) {
            this.indexSDI(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3);
        } else if (indexItemParts[0].equals(FIELD_TYPE_PRIMARY)) {
            this.indexSDIPrimary(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3);
        } else if (indexItemParts[0].equals(FIELD_TYPE_ATTACHMENT)) {
            this.indexSDIAttachment(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, indexItemParts.length >= 6 ? Integer.parseInt(indexItemParts[5]) : -1);
        } else if (indexItemParts[0].equals(FIELD_TYPE_NOTE)) {
            this.indexSDINote(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, indexItemParts.length >= 6 ? Integer.parseInt(indexItemParts[5]) : -1);
        }
    }

    private void delete(IndexWriter indexWriter, DBUtil dbu, String sdcid, String keyid1, String keyid2, String keyid3, String[] indexItemParts) {
        if (indexItemParts[0].equals(FIELD_TYPE_SDI)) {
            this.deleteSDI(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3);
        } else if (indexItemParts[0].equals(FIELD_TYPE_PRIMARY)) {
            this.deleteSDIPrimary(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3);
        } else if (indexItemParts[0].equals(FIELD_TYPE_ATTACHMENT)) {
            this.deleteSDIAttachment(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, Integer.parseInt(indexItemParts[5]));
        } else if (indexItemParts[0].equals(FIELD_TYPE_NOTE)) {
            this.deleteSDINote(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, Integer.parseInt(indexItemParts[5]));
        }
    }

    private PropertyList getSDCProps(DBUtil dbu, String sdcid) throws SapphireException {
        PropertyList sdcProps = this.sdcPropsMap.get(sdcid);
        if (sdcProps == null) {
            dbu.createPreparedResultSet("sdcprops", "SELECT tableid, sdctype, compcode, singular, plural FROM sdc WHERE sdcid = ?", new Object[]{sdcid});
            if (dbu.getNext("sdcprops")) {
                sdcProps = new PropertyList();
                String tableid = dbu.getValue("sdcprops", "tableid");
                sdcProps.setProperty(FIELD_SDCID, sdcid);
                sdcProps.setProperty("tableid", tableid);
                sdcProps.setProperty("singular", dbu.getValue("sdcprops", "singular"));
                sdcProps.setProperty("plural", dbu.getValue("sdcprops", "plural"));
                String[] keys = DDTService.getKeyColumns(dbu, tableid);
                sdcProps.setProperty("keycolumns", String.valueOf(keys.length));
                for (int i = 0; i < keys.length; ++i) {
                    sdcProps.setProperty("keycolid" + (i + 1), keys[i]);
                }
                sdcProps.setProperty(FIELD_DESCCOL, DDTService.getDescCol(sdcid, dbu.getValue("sdcprops", "sdctype"), dbu.getValue("sdcprops", "compcode"), tableid, keys[0]));
                this.sdcPropsMap.put(sdcid, sdcProps);
            }
        }
        return sdcProps;
    }

    private void indexSDI(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3) {
        block13: {
            Trace.setStartCodeBlock("IndexSDI");
            String resultsetName = "indexsdi";
            try {
                if (sdcid.length() <= 0) break block13;
                PropertyList sdcProps = this.getSDCProps(dbu, sdcid);
                if (sdcProps != null) {
                    int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                    String tableid = sdcProps.getProperty("tableid");
                    String keycolid1 = sdcProps.getProperty("keycolid1");
                    String keycolid2 = sdcProps.getProperty("keycolid2");
                    String keycolid3 = sdcProps.getProperty("keycolid3");
                    String select = "SELECT " + keycolid1 + (keycols >= 2 ? "," + keycolid2 : INDEXFLAG_NONE) + (keycols >= 3 ? "," + keycolid3 : INDEXFLAG_NONE) + " FROM " + tableid;
                    if (keyid1.length() == 0 || keyid1.equals("(all)")) {
                        dbu.createResultSet(resultsetName, select);
                    } else if (keycols == 1) {
                        if (keyid1.endsWith("%")) {
                            dbu.createPreparedResultSet(resultsetName, select + " WHERE " + keycolid1 + " like ?", new Object[]{keyid1});
                        } else {
                            dbu.createPreparedResultSet(resultsetName, select + " WHERE " + keycolid1 + " = ?", new Object[]{keyid1});
                        }
                    } else if (keycols == 2) {
                        dbu.createPreparedResultSet(resultsetName, select + " WHERE " + keycolid1 + " = ? AND " + keycolid2 + " = ?", new Object[]{keyid1, keyid2});
                    } else if (keycols == 3) {
                        dbu.createPreparedResultSet(resultsetName, select + " WHERE " + keycolid1 + " = ? AND " + keycolid2 + " = ? AND " + keycolid3 + " = ?", new Object[]{keyid1, keyid2, keyid3});
                    }
                    while (dbu.getNext(resultsetName)) {
                        String rsKeyid1 = dbu.getValue(resultsetName, keycolid1);
                        String rsKeyid2 = keycols > 1 ? dbu.getValue(resultsetName, keycolid2) : INDEXFLAG_NONE;
                        String rsKeyid3 = keycols > 2 ? dbu.getValue(resultsetName, keycolid3) : INDEXFLAG_NONE;
                        this.indexSDIPrimary(dbu, indexWriter, sdcid, rsKeyid1, rsKeyid2, rsKeyid3);
                        this.indexSDIAttachment(dbu, indexWriter, sdcid, rsKeyid1, rsKeyid2.length() > 0 ? rsKeyid2 : "(null)", rsKeyid3.length() > 0 ? rsKeyid3 : "(null)", -1);
                        this.indexSDINote(dbu, indexWriter, sdcid, rsKeyid1, rsKeyid2.length() > 0 ? rsKeyid2 : "(null)", rsKeyid3.length() > 0 ? rsKeyid3 : "(null)", -1);
                    }
                    break block13;
                }
                throw new SapphireException("Invalid sdcid '" + sdcid + "'");
            }
            catch (SapphireException e) {
                Trace.logError(LOGNAME, (Object)("Failure indexing SDI sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ". Reason: " + e.getMessage()), e);
            }
        }
        Trace.setEndCodeBlock("IndexSDI");
    }

    private void deleteSDI(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3) {
        Trace.setStartCodeBlock("DeleteSDI");
        this.deleteSDIPrimary(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3);
        this.deleteSDIAttachment(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, -1);
        this.deleteSDINote(dbu, indexWriter, sdcid, keyid1, keyid2, keyid3, -1);
        Trace.setEndCodeBlock("DeleteSDI");
    }

    private void indexSDIPrimary(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3) {
        block13: {
            Trace.setStartCodeBlock("IndexSDIPrimary");
            try {
                String resultsetName = "indexsdiprimary";
                if (sdcid.length() <= 0) break block13;
                PropertyList sdcProps = this.getSDCProps(dbu, sdcid);
                if (sdcProps != null) {
                    int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                    String tableid = sdcProps.getProperty("tableid");
                    String keycolid1 = sdcProps.getProperty("keycolid1");
                    String keycolid2 = sdcProps.getProperty("keycolid2");
                    String keycolid3 = sdcProps.getProperty("keycolid3");
                    if (keyid1.length() == 0 || keyid1.equals("(all)")) {
                        dbu.createResultSet(resultsetName, "SELECT * FROM " + tableid);
                    } else if (keycols == 1) {
                        if (keyid1.endsWith("%")) {
                            dbu.createPreparedResultSet(resultsetName, "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " like ?", new Object[]{keyid1});
                        } else {
                            dbu.createPreparedResultSet(resultsetName, "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " = ?", new Object[]{keyid1});
                        }
                    } else if (keycols == 2) {
                        dbu.createPreparedResultSet(resultsetName, "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " = ? AND " + keycolid2 + " = ?", new Object[]{keyid1, keyid2});
                    } else if (keycols == 3) {
                        dbu.createPreparedResultSet(resultsetName, "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " = ? AND " + keycolid2 + " = ? AND " + keycolid3 + " = ?", new Object[]{keyid1, keyid2, keyid3});
                    }
                    BaseIndexer indexer = null;
                    indexer = sdcid.equalsIgnoreCase("LV_Document") ? new DocumentIndexer(this, dbu, indexWriter, sdcProps, resultsetName) : (sdcid.equalsIgnoreCase("LV_Worksheet") || sdcid.equalsIgnoreCase("LV_WorksheetSection") || sdcid.equalsIgnoreCase("LV_WorksheetItem") ? new WorksheetIndexer(this, dbu, indexWriter, sdcProps, resultsetName) : (sdcid.equalsIgnoreCase("LV_TaskExec") ? new TaskExecIndexer(this, dbu, indexWriter, sdcProps, resultsetName) : new SDIIndexer(this, dbu, indexWriter, sdcProps, resultsetName)));
                    while (dbu.getNext(resultsetName)) {
                        indexer.indexSet();
                    }
                    break block13;
                }
                throw new SapphireException("Invalid sdcid '" + sdcid + "'");
            }
            catch (SapphireException e) {
                Trace.logError(LOGNAME, (Object)("Failure indexing primary records for sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ". Reason: " + e.getMessage()), e);
            }
        }
        Trace.setEndCodeBlock("IndexSDIPrimary");
    }

    private void deleteSDIPrimary(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3) {
        Trace.setStartCodeBlock("DeleteSDIPrimary");
        try {
            PropertyList sdcProps = this.getSDCProps(dbu, sdcid);
            if (sdcid.equalsIgnoreCase("LV_Document")) {
                new DocumentIndexer(this, dbu, indexWriter, sdcProps).delete(keyid1, keyid2);
            } else if (sdcid.equalsIgnoreCase("LV_Worksheet") || sdcid.equalsIgnoreCase("LV_WorksheetSection") || sdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                new WorksheetIndexer(this, dbu, indexWriter, sdcProps).delete(keyid1, keyid2);
            } else if (sdcid.equalsIgnoreCase("LV_TaskExec")) {
                new TaskExecIndexer(this, dbu, indexWriter, sdcProps).delete(keyid1);
            } else {
                new SDIIndexer(this, dbu, indexWriter, sdcProps).delete(keyid1, keyid2, keyid3);
            }
        }
        catch (SapphireException e) {
            Trace.logError(LOGNAME, (Object)("Failure deleting primary record for sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ". Reason: " + e.getMessage()), e);
        }
        Trace.setEndCodeBlock("DeleteSDIPrimary");
    }

    private void indexSDIAttachment(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        Trace.setStartCodeBlock("IndexSDIAttachment");
        String resultsetName = "indexsdiattachment";
        try {
            String keycolid3;
            String keycolid2;
            String keycolid1;
            String tableid;
            PropertyList sdcProps;
            if (sdcid.length() == 0 || sdcid.equals("(all)")) {
                dbu.createResultSet(resultsetName, "SELECT * FROM sdiattachment");
            } else if (sdcid.length() > 0 && (keyid1.length() == 0 || keyid1.equals("(all)"))) {
                dbu.createPreparedResultSet(resultsetName, "SELECT * FROM sdiattachment WHERE sdcid = ?", new Object[]{sdcid});
            } else if (sdcid.length() > 0 && keyid1.length() > 0 && attachmentnum == -1) {
                sdcProps = this.getSDCProps(dbu, sdcid);
                tableid = sdcProps.getProperty("tableid");
                keycolid1 = sdcProps.getProperty("keycolid1");
                keycolid2 = sdcProps.getProperty("keycolid2");
                keycolid3 = sdcProps.getProperty("keycolid3");
                dbu.createPreparedResultSet(resultsetName, "SELECT sdiattachment.*, " + tableid + ".* FROM sdiattachment, " + tableid + " WHERE sdiattachment.sdcid = ? AND sdiattachment.keyid1 = ? AND sdiattachment.keyid2 = ? AND sdiattachment.keyid3 = ? AND sdiattachment.keyid1 = " + tableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND sdiattachment.keyid2 = " + tableid + "." + keycolid2 : INDEXFLAG_NONE) + (keycolid3.length() > 0 ? " AND sdiattachment.keyid3 = " + tableid + "." + keycolid3 : INDEXFLAG_NONE), new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)"});
            } else {
                sdcProps = this.getSDCProps(dbu, sdcid);
                tableid = sdcProps.getProperty("tableid");
                keycolid1 = sdcProps.getProperty("keycolid1");
                keycolid2 = sdcProps.getProperty("keycolid2");
                keycolid3 = sdcProps.getProperty("keycolid3");
                dbu.createPreparedResultSet(resultsetName, "SELECT sdiattachment.*, " + tableid + ".* FROM sdiattachment, " + tableid + " WHERE sdiattachment.sdcid = ? AND sdiattachment.keyid1 = ? AND sdiattachment.keyid2 = ? AND sdiattachment.keyid3 = ? AND sdiattachment.attachmentnum = ? AND sdiattachment.keyid1 = " + tableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND sdiattachment.keyid2 = " + tableid + "." + keycolid2 : INDEXFLAG_NONE) + (keycolid3.length() > 0 ? " AND sdiattachment.keyid3 = " + tableid + "." + keycolid3 : INDEXFLAG_NONE), new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)", attachmentnum});
            }
            AttachmentIndexer indexer = new AttachmentIndexer(this, dbu, indexWriter, sdcid, resultsetName);
            while (dbu.getNext(resultsetName)) {
                indexer.indexSet();
            }
        }
        catch (SapphireException e) {
            Trace.logError(LOGNAME, (Object)("Failure indexing sdiattachment sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ", attachmentnum=" + attachmentnum + ". Reason: " + e.getMessage()), e);
        }
        Trace.setEndCodeBlock("IndexSDIAttachment");
    }

    private void deleteSDIAttachment(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        Trace.setStartCodeBlock("DeleteSDIAttachment");
        AttachmentIndexer indexer = new AttachmentIndexer(this, dbu, indexWriter);
        if (attachmentnum == -1) {
            try {
                String resultsetName = "deletesdiattachment";
                dbu.createPreparedResultSet(resultsetName, "SELECT attachmentnum FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)"});
                while (dbu.getNext(resultsetName)) {
                    indexer.delete(sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)", dbu.getInt(resultsetName, "attachmentnum"));
                }
            }
            catch (SapphireException e) {
                Trace.logError(LOGNAME, (Object)("Failure deleting sdiattachment index sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ", attachmentnum=" + attachmentnum + ". Reason: " + e.getMessage()), e);
            }
        } else {
            indexer.delete(sdcid, keyid1, keyid2, keyid3, attachmentnum);
        }
        Trace.setEndCodeBlock("DeleteSDIAttachment");
    }

    private void indexSDINote(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3, int notenum) {
        Trace.setStartCodeBlock("IndexSDINote");
        String resultsetName = "indexsdinote";
        try {
            String keycolid3;
            String keycolid2;
            String keycolid1;
            String tableid;
            PropertyList sdcProps;
            if (sdcid.length() == 0 || sdcid.equals("(all)")) {
                dbu.createResultSet(resultsetName, "SELECT * FROM sdinote");
            } else if (sdcid.length() > 0 && (keyid1.length() == 0 || keyid1.equals("(all)"))) {
                dbu.createPreparedResultSet(resultsetName, "SELECT * FROM sdinote WHERE sdcid = ?", new Object[]{sdcid});
            } else if (sdcid.length() > 0 && keyid1.length() > 0 && notenum == -1) {
                sdcProps = this.getSDCProps(dbu, sdcid);
                tableid = sdcProps.getProperty("tableid");
                keycolid1 = sdcProps.getProperty("keycolid1");
                keycolid2 = sdcProps.getProperty("keycolid2");
                keycolid3 = sdcProps.getProperty("keycolid3");
                dbu.createPreparedResultSet(resultsetName, "SELECT sdinote.*, " + tableid + ".* FROM sdinote, " + tableid + " WHERE sdinote.sdcid = ? AND sdinote.keyid1 = ? AND sdinote.keyid2 = ? AND sdinote.keyid3 = ? AND sdinote.keyid1 = " + tableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND sdinote.keyid2 = " + tableid + "." + keycolid2 : INDEXFLAG_NONE) + (keycolid3.length() > 0 ? " AND sdinote.keyid3 = " + tableid + "." + keycolid3 : INDEXFLAG_NONE), new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)"});
            } else {
                sdcProps = this.getSDCProps(dbu, sdcid);
                tableid = sdcProps.getProperty("tableid");
                keycolid1 = sdcProps.getProperty("keycolid1");
                keycolid2 = sdcProps.getProperty("keycolid2");
                keycolid3 = sdcProps.getProperty("keycolid3");
                dbu.createPreparedResultSet(resultsetName, "SELECT sdinote.*, " + tableid + ".* FROM sdinote, " + tableid + " WHERE sdinote.sdcid = ? AND sdinote.keyid1 = ? AND sdinote.keyid2 = ? AND sdinote.keyid3 = ? AND sdinote.notenum = ? AND sdinote.keyid1 = " + tableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND sdinote.keyid2 = " + tableid + "." + keycolid2 : INDEXFLAG_NONE) + (keycolid3.length() > 0 ? " AND sdinote.keyid3 = " + tableid + "." + keycolid3 : INDEXFLAG_NONE), new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)", notenum});
            }
            NoteIndexer indexer = new NoteIndexer(this, dbu, indexWriter, sdcid, resultsetName);
            while (dbu.getNext(resultsetName)) {
                indexer.indexSet();
            }
        }
        catch (SapphireException e) {
            Trace.logError(LOGNAME, (Object)("Failure indexing sdinote sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ", notenum=" + notenum + ". Reason: " + e.getMessage()), e);
        }
        Trace.setEndCodeBlock("IndexSDINote");
    }

    private void deleteSDINote(DBUtil dbu, IndexWriter indexWriter, String sdcid, String keyid1, String keyid2, String keyid3, int notenum) {
        Trace.setStartCodeBlock("DeleteSDINote");
        NoteIndexer indexer = new NoteIndexer(this, dbu, indexWriter);
        if (notenum == -1) {
            try {
                String resultsetName = "deletesdinote";
                dbu.createPreparedResultSet(resultsetName, "SELECT notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)"});
                while (dbu.getNext(resultsetName)) {
                    indexer.delete(sdcid, keyid1, keyid2, keyid3, dbu.getInt(resultsetName, "notenum"));
                }
            }
            catch (SapphireException e) {
                Trace.logError(LOGNAME, (Object)("Failure deleting sdinote index sdcid=" + sdcid + ", keyid1=" + keyid1 + ", keyid2=" + keyid2 + ", keyid3=" + keyid3 + ", notenum=" + notenum + ". Reason: " + e.getMessage()), e);
            }
        } else {
            indexer.delete(sdcid, keyid1, keyid2, keyid3, notenum);
        }
        Trace.setEndCodeBlock("DeleteSDINote");
    }

    public PropertyList getIndexingPolicy() {
        return this.indexingPolicy;
    }

    public PropertyList getSearchPolicy() {
        return this.searchingPolicy;
    }

    public String[] getIndexedSDCs() {
        return this.getIndexedSDCs(true);
    }

    public String[] getIndexedSDCs(boolean includeChildSDCs) {
        Set<String> keySet = this.sdcPolicyMap.keySet();
        ArrayList<String> sdcs = new ArrayList<String>();
        for (String sdcid : keySet) {
            if (!includeChildSDCs && !this.sdcPolicyMap.get(sdcid).getProperty("childsdc", "N").equals("N")) continue;
            sdcs.add(sdcid);
        }
        return sdcs.toArray(new String[sdcs.size()]);
    }

    public PropertyList getSDCPolicy(String sdcid) {
        return this.sdcPolicyMap.get(sdcid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isIndexLocked() throws SapphireException {
        Trace.setStartCodeBlock("IndexWriterLocked");
        try {
            Directory dir = this.getIndexDirectory();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig((Analyzer)new StandardAnalyzer());
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            new IndexWriter(dir, indexWriterConfig).close();
            boolean bl = false;
            return bl;
        }
        catch (Exception e) {
            boolean bl = true;
            return bl;
        }
        finally {
            Trace.setEndCodeBlock("IndexWriterLocked");
        }
    }

    private IndexWriter getIndexWriter() throws SapphireException {
        return this.getIndexWriter(false);
    }

    private IndexWriter getIndexWriter(boolean reset) throws SapphireException {
        Trace.setStartCodeBlock("GetIndexWriter");
        try {
            Directory dir = this.getIndexDirectory();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.getAnalyzer());
            indexWriterConfig.setOpenMode(reset ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);
            return indexWriter;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get index writer. Reason: " + e.getMessage(), e);
        }
        finally {
            Trace.setEndCodeBlock("GetIndexWriter");
        }
    }

    public Analyzer getAnalyzer() throws SapphireException {
        try {
            Class<?> c = Class.forName(this.analyzer);
            Analyzer analyzer = (Analyzer)c.newInstance();
            if (this.stopWords != null && analyzer instanceof StopwordAnalyzerBase) {
                CharArraySet stopwordSet;
                if (this.extendStopWords) {
                    CharArraySet stopwordset = ((StopwordAnalyzerBase)analyzer).getStopwordSet();
                    stopwordSet = new CharArraySet(stopwordset.size() + this.stopWords.length, true);
                    Iterator iterator = stopwordset.iterator();
                    while (iterator.hasNext()) {
                        stopwordSet.add(iterator.next());
                    }
                    for (int i = 0; i < this.stopWords.length; ++i) {
                        stopwordSet.add(this.stopWords[i]);
                    }
                } else {
                    stopwordSet = new CharArraySet(Arrays.asList(this.stopWords), true);
                }
                Constructor<?> ctor = c.getConstructor(CharArraySet.class);
                return (Analyzer)ctor.newInstance(stopwordSet);
            }
            return analyzer;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get analyzer. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public Directory getIndexDirectory() throws IOException {
        return FSDirectory.open((Path)this.indexDir.toPath());
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    public File getIndexDir() {
        return this.indexDir;
    }

    public SapphireDatabase getSapphireDatabase() {
        return this.sapphireDatabase;
    }

    public int getPollingInterval() {
        return this.pollInterval;
    }

    public boolean isColumnExcluded(String sdcId, String columnId) {
        Set<String> sensitiveColSet;
        boolean sensitiveColFlag = false;
        if (!this.indexSensitiveData && this.excludedSDCColMap.containsKey(sdcId) && (sensitiveColSet = this.excludedSDCColMap.get(sdcId)).contains(columnId.toLowerCase())) {
            sensitiveColFlag = true;
        }
        return sensitiveColFlag;
    }
}

