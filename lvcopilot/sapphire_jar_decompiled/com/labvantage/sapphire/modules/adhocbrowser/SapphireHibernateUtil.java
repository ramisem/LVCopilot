/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 *  org.hibernate.SessionFactory
 *  org.hibernate.cfg.Configuration
 *  org.hibernate.dialect.function.SQLFunction
 *  org.hibernate.dialect.function.StandardSQLFunction
 *  org.hibernate.engine.spi.SessionFactoryImplementor
 *  org.hibernate.metadata.ClassMetadata
 *  org.hibernate.type.ManyToOneType
 *  org.hibernate.type.SetType
 *  org.hibernate.type.Type
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateMapping;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.junit.ConnectionDetails;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.w3c.dom.Document;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;

public class SapphireHibernateUtil {
    private static HashMap instanceCache = new HashMap();
    private SessionFactory sessionFactory;
    private AdhocMetaData adhocmetadata = new AdhocMetaData();
    private HashMap typemaps = new HashMap();
    public boolean isDataSource = true;
    private static final String HIBERNATE_DIALECT_ORACLE = "org.hibernate.dialect.Oracle10gDialect";
    private static final String HIBERNATE_DIALECT_SQLServer = "org.hibernate.dialect.SQLServerDialect";
    public static final ThreadLocal session = new ThreadLocal();

    public AdhocMetaData getAdhocMetaData() {
        return this.adhocmetadata;
    }

    private SapphireHibernateUtil(SapphireConnection sapphireConnection) {
        String databasejndiname = "";
        try {
            databasejndiname = Configuration.getInstance().getSapphireDatabase(sapphireConnection.getSapphireDatabase().getDatabaseId()).getJndiname();
            String dialect = sapphireConnection.getSapphireDatabase().isOracle() ? HIBERNATE_DIALECT_ORACLE : HIBERNATE_DIALECT_SQLServer;
            org.hibernate.cfg.Configuration cfg = new org.hibernate.cfg.Configuration();
            if ("JUNIT".equals(sapphireConnection.getTool())) {
                if ("ORA".equals(sapphireConnection.getDbms())) {
                    cfg.setProperty("hibernate.connection.driver_class", "oracle.jdbc.driver.OracleDriver");
                    cfg.setProperty("hibernate.connection.url", "jdbc:oracle:thin:@" + ConnectionDetails.TESTDB_SERVERNAME + ":" + ConnectionDetails.TESTDB_PORT + "/" + ConnectionDetails.TESTDB_SID);
                    cfg.setProperty("hibernate.dialect", HIBERNATE_DIALECT_ORACLE);
                } else {
                    cfg.setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    cfg.setProperty("hibernate.connection.url", "jdbc:sqlserver://" + ConnectionDetails.TESTDB_SERVERNAME + ":" + ConnectionDetails.TESTDB_PORT + ";databaseName=" + ConnectionDetails.TESTDB_SQLDATABASE);
                    cfg.setProperty("hibernate.dialect", HIBERNATE_DIALECT_SQLServer);
                }
                cfg.setProperty("hibernate.connection.username", ConnectionDetails.TESTDB_USERNAME);
                cfg.setProperty("hibernate.connection.password", ConnectionDetails.TESTDB_PASSWORD);
                cfg.setProperty("hibernate.show_sql", "true");
                this.isDataSource = false;
            } else {
                String datasourcejndiname = Configuration.getInstance().getJNDIDataSourcePrefix() + databasejndiname;
                cfg.setProperty("hibernate.connection.datasource", datasourcejndiname);
                cfg.setProperty("hibernate.dialect", dialect);
            }
            if (Trace.isDebugEnabled()) {
                cfg.setProperty("hibernate.show_sql", "true");
            }
            cfg.setProperty("hibernate.format_sql", "true").setProperty("hibernate.generate_statistics", "true").setProperty("hibernate.use_sql_comments", "true").setProperty("hibernate.cglib.use_reflection_optimizer", "true").setProperty("hibernate.default_entity_mode", "dynamic-map");
            cfg.addSqlFunction("slowdown", (SQLFunction)new StandardSQLFunction("slowdown"));
            cfg.addSqlFunction(sapphireConnection.getDatabaseId() + ".slowdown", (SQLFunction)new StandardSQLFunction(sapphireConnection.getDatabaseId() + ".slowdown"));
            SapphireHibernateMapping shm = new SapphireHibernateMapping(sapphireConnection);
            this.adhocmetadata = shm.getAdhocMetaData();
            Trace.log("Try non-validated parsing ...");
            cfg.addDocument(SapphireHibernateUtil.parseConfiguration(shm.generateXMLMapping(false))).buildSessionFactory();
            Trace.log("Non-validated parsing ...DONE!");
            this.sessionFactory = cfg.buildSessionFactory();
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            Trace.logError("Initial SessionFactory creation failed for database: " + databasejndiname + ":" + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SapphireHibernateUtil getInstance(SapphireConnection sapphireConnection) {
        String databaseid = sapphireConnection.getSapphireDatabase().getDatabaseId();
        SapphireHibernateUtil me = (SapphireHibernateUtil)instanceCache.get(databaseid);
        if (me == null) {
            me = new SapphireHibernateUtil(sapphireConnection);
            instanceCache.put(databaseid, me);
        }
        return me;
    }

    static void resetHibernateMappingCache(String databaseid) {
        instanceCache.put(databaseid, null);
    }

    public Session currentSession() {
        Session s = (Session)session.get();
        if (s == null) {
            s = this.sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    public Session openSession() {
        return this.sessionFactory.openSession();
    }

    public void closeSession() {
        Session s = (Session)session.get();
        if (s != null) {
            s.close();
        }
        session.set(null);
    }

    public String getReferenceEntityName(String tableid, String columnid) {
        String[] tokens = StringUtil.split(columnid, ".");
        ClassMetadata classMetadata = this.sessionFactory.getClassMetadata(tableid);
        String entityname = "";
        try {
            Type type;
            int index = columnid.indexOf(tableid + ".") == 0 ? 1 : 0;
            Type propertyValue = type = classMetadata.getPropertyType(tokens[index]);
            if (propertyValue instanceof ManyToOneType) {
                entityname = ((ManyToOneType)propertyValue).getAssociatedEntityName();
            } else if (propertyValue instanceof SetType) {
                entityname = ((SetType)propertyValue).getAssociatedEntityName((SessionFactoryImplementor)this.sessionFactory);
            }
            for (int i = index + 1; i < tokens.length; ++i) {
                if (entityname.length() <= 0) continue;
                entityname = this.getReferenceEntityName(entityname, tokens[i]);
            }
        }
        catch (Exception e) {
            Trace.log("Not an entity:" + columnid);
        }
        return entityname;
    }

    public String getAliasName(String tableid, String columnid) {
        if (columnid.indexOf(".") > 0) {
            columnid = columnid.substring(0, columnid.lastIndexOf("."));
        }
        return this.getReferenceEntityName(tableid, columnid);
    }

    public String getTypeName(String entityname, String columnid) {
        String typename;
        if (columnid.indexOf(".") > 0 && this.sessionFactory.getClassMetadata(columnid.substring(0, columnid.indexOf("."))) != null) {
            entityname = columnid.substring(0, columnid.indexOf("."));
            columnid = columnid.substring(columnid.indexOf(".") + 1);
        }
        if (this.typemaps.get(entityname) == null) {
            ClassMetadata md = this.sessionFactory.getClassMetadata(entityname);
            HashMap<String, String> typemap = new HashMap<String, String>();
            typemap.put(md.getIdentifierPropertyName(), md.getIdentifierType().getName());
            String[] names = md.getPropertyNames();
            for (int i = 0; i < names.length; ++i) {
                typemap.put(names[i], md.getPropertyType(names[i]).getName());
            }
            this.typemaps.put(entityname, typemap);
        }
        if ((typename = (String)((Map)this.typemaps.get(entityname)).get(columnid)) == null && columnid.indexOf(".") > 0) {
            String tempentity = this.getReferenceEntityName(entityname, columnid.substring(0, columnid.indexOf(".")));
            String tempcolid = columnid.substring(columnid.indexOf(".") + 1);
            return this.getTypeName(tempentity, tempcolid);
        }
        if (!(typename == null || typename.equals("string") || typename.equals("big_decimal") || typename.equals("long") || typename.equals("integer") || typename.equals("double") || typename.equals("timestamp"))) {
            return this.getTypeName(typename, typename + "id");
        }
        if (typename == null || typename.length() == 0) {
            typename = "string";
        }
        return typename;
    }

    private static Document parseConfiguration(String xml) throws Exception {
        return DOMUtil.getNewDocument(xml);
    }
}

