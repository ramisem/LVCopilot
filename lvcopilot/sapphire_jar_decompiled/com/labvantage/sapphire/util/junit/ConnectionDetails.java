/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.junit;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConnectionDetails {
    private static String PROPS_FILE;
    public static String REMOTEACCESSKEY;
    public static String SAPPHIRE_HOME;
    public static String CACH_CONNECTION;
    public static String JAR_FOLDER;
    public static String TESTDB_DATABASEID;
    public static String TESTDB_USERNAME;
    public static String TESTDB_PASSWORD;
    public static String TESTDB_DBMS;
    public static String TESTDB_INSTANCENAME;
    public static String TESTDB_SERVERNAME;
    public static String TESTDB_SQLDATABASE;
    public static String TESTDB_HOSTSTRING;
    public static String TESTDB_PORT;
    public static String TESTDB_SID;
    public static String SAPPHIRE_USERNAME;
    public static String SAPPHIRE_PASSWORD;
    public static String WEBAPP_URL;
    public static String TEMPDIR;

    public static void setSource(String source) {
        String string = PROPS_FILE = System.getProperty("PROPS_FILE") != null && !System.getProperty("PROPS_FILE").isEmpty() ? System.getProperty("PROPS_FILE") : "c:\\development\\sapphire\\Connections\\ConnectionDetails";
        if (source == null || source.equals("")) {
            source = "default";
        }
        Properties props = new Properties();
        try {
            File propsFile = new File(PROPS_FILE + "_" + source + ".props");
            props.load(new FileInputStream(propsFile));
            REMOTEACCESSKEY = props.getProperty("REMOTEACCESSKEY");
            SAPPHIRE_HOME = props.getProperty("SAPPHIRE_HOME");
            CACH_CONNECTION = props.getProperty("CACH_CONNECTION");
            JAR_FOLDER = props.getProperty("JAR_FOLDER");
            TESTDB_DATABASEID = props.getProperty("TESTDB_DATABASEID").trim();
            TESTDB_USERNAME = props.getProperty("TESTDB_USERNAME").trim();
            TESTDB_PASSWORD = props.getProperty("TESTDB_PASSWORD").trim();
            TESTDB_DBMS = props.getProperty("TESTDB_DBMS");
            TESTDB_INSTANCENAME = props.getProperty("TESTDB_INSTANCENAME");
            TESTDB_SERVERNAME = props.getProperty("TESTDB_SERVERNAME");
            TESTDB_SQLDATABASE = props.getProperty("TESTDB_SQLDATABASE");
            TESTDB_HOSTSTRING = props.getProperty("TESTDB_HOSTSTRING");
            TESTDB_PORT = props.getProperty("TESTDB_PORT");
            TESTDB_SID = props.getProperty("TESTDB_SID");
            SAPPHIRE_USERNAME = props.getProperty("SAPPHIRE_USERNAME", TESTDB_USERNAME).trim();
            SAPPHIRE_PASSWORD = props.getProperty("SAPPHIRE_PASSWORD", TESTDB_PASSWORD).trim();
            WEBAPP_URL = props.getProperty("WEBAPP_URL");
            if (WEBAPP_URL != null && !(WEBAPP_URL = WEBAPP_URL.trim()).endsWith("/")) {
                WEBAPP_URL = WEBAPP_URL + "/";
            }
            TEMPDIR = (TEMPDIR = props.getProperty("TEMPDIR")) == null || TEMPDIR.length() == 0 ? "c:\\temp\\" : (!TEMPDIR.endsWith(File.separator) ? TEMPDIR.trim() + File.separator : TEMPDIR.trim());
        }
        catch (Exception e) {
            System.out.println("Unable to load properties file from " + PROPS_FILE + "_" + source + ".props");
        }
    }

    static {
        ConnectionDetails.setSource("");
    }
}

