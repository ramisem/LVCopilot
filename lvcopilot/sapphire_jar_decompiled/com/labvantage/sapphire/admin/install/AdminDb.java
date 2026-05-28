/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.install;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.EncryptDecrypt;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class AdminDb {
    public static void insertSysConfigDefaults(DBAccess database) throws SapphireException {
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'build', '" + Build.getBuild() + "' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'todolistpoll', '1' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'taskpoll', '60' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'schedulepoll', '3600' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'timeoutpoll', '60' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'connectiontimeout', '3600' )");
        database.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'rsettimeout', '60' )");
    }

    public static String getDBConnectionKey(String username, String password) {
        return EncryptDecrypt.encrypt(username + "|" + password);
    }
}

