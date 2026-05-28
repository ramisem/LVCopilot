/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.install;

import com.labvantage.sapphire.Build;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import sapphire.SapphireException;

public class Installer {
    public static int compareBuild(String build) {
        if (build != null && build.length() > 0) {
            if (build.indexOf(46) == -1) {
                build = "0000.000.00";
            }
            return build.compareTo(Build.getBuild());
        }
        return -1;
    }

    public static void checkInstallPrivs(Connection connection) throws SapphireException {
        String privStatement = "DECLARE  dummy VARCHAR2(2);  errmsg VARCHAR2(2000);  defdir1_local VARCHAR2(20) := 'FILEOUTDIR';  overall_fail BOOLEAN := FALSE;  CURSOR d1 IS     SELECT NULL FROM all_directories    WHERE directory_name = defdir1_local;  PROCEDURE SysPrivs (priv_in IN VARCHAR2) IS    CURSOR p1 IS      SELECT NULL FROM user_sys_privs      WHERE privilege = priv_in      UNION      SELECT NULL FROM role_sys_privs      WHERE privilege = priv_in;   BEGIN    OPEN p1;    FETCH p1 INTO dummy;    IF p1%NOTFOUND THEN      overall_fail := TRUE;      errmsg := errmsg || ' (' || priv_in || ' missing)';    END IF;    CLOSE p1;  END SysPrivs;  PROCEDURE TestWrite IS    fnum Utl_File.File_Type;  BEGIN    fnum := Utl_File.FOpen (defdir1_local,'testwrite.txt','w',1024);    Utl_File.PutF (fnum,'test');    Utl_File.FClose (fnum);  EXCEPTION    WHEN others THEN      overall_fail := TRUE;      errmsg := errmsg || ' (' || defdir1_local || ' not writeable)';  END TestWrite;BEGIN  SysPrivs ('CREATE TABLE');  SysPrivs ('CREATE TRIGGER');  SysPrivs ('CREATE PROCEDURE');  SysPrivs ('CREATE SEQUENCE');  SysPrivs ('CREATE VIEW');  SysPrivs ('CREATE TYPE');  OPEN d1;  FETCH d1 INTO dummy;  IF d1%NOTFOUND THEN    overall_fail := TRUE;    errmsg := errmsg || ' (' || defdir1_local || ' not visible)';  ELSE    TestWrite;  END IF;  CLOSE d1;  IF overall_fail THEN    Raise_Application_Error (-20990,'Schema is not set up properly for Sapphire ' || errmsg);  END IF;END;";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(privStatement);
            stmt.close();
        }
        catch (SQLException e) {
            throw new SapphireException(e);
        }
    }

    public static void checkRCSOn(Connection connection) throws SapphireException {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT is_read_committed_snapshot_on FROM sys.databases WHERE name = DB_NAME()");
            rs.next();
            if (rs.getInt(1) != 1) {
                throw new SapphireException("Database is not set up properly - you must have Read Committed Snapshot isolation set ON");
            }
        }
        catch (SQLException e) {
            throw new SapphireException(e);
        }
    }
}

