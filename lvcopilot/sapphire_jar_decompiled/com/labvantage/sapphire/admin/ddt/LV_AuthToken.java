/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_AuthToken
extends BaseSDCRules
implements CacheNames {
    public static final String STATUS_PENDINGAPPROVAL = "Pending Approval";
    public static final String STATUS_ACTIVE = "Active";
    public static final String AUTHTOKEN_DATABASE_SEPARATOR = "|at;";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String externalappid = primary.getString(i, "externalappid");
            if (externalappid.length() == 0) {
                throw new SapphireException("You must specific an External App for any token.");
            }
            DataSet externalAppDS = ExternalAuthenticationUtil.getCachedExternalAppDS(externalappid, this.getDatabaseid(), this.getQueryProcessor());
            String externaluserid = primary.getValue(i, "externaluserid");
            if (externaluserid.length() > 0) {
                DataSet externaluser = this.getQueryProcessor().getPreparedSqlDataSet("SELECT externalappid, nameduserflag FROM sysuser WHERE sysuserid=? AND externalappid=? AND nameduserflag=?", (Object[])new String[]{externaluserid, externalappid, "A"});
                if (externaluser.size() == 0) {
                    throw new SapphireException("External User " + externaluserid + " not recognized for access to External App " + externalappid);
                }
            } else {
                DataSet externalusers = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sysuserid FROM sysuser WHERE nameduserflag=? AND externalappid=?", (Object[])new String[]{"A", externalappid});
                if (externalusers.size() == 1) {
                    externaluserid = externalAppDS.getValue(0, "sysuserid");
                    primary.setString(i, "externaluserid", externaluserid);
                }
            }
            if (externaluserid.length() == 0 && primary.getValue(0, "tokenstatus").equals(STATUS_ACTIVE)) {
                throw new SapphireException("You must specific an External User before a token can be made Active.");
            }
            if (primary.isNull(i, "tokenstatus")) {
                primary.setString(i, "tokenstatus", STATUS_PENDINGAPPROVAL);
            }
            if (primary.isNull(i, "expirydt")) {
                int defaultdays = externalAppDS.getInt(0, "defaulttokenexpirydays", 180);
                primary.setDate(i, "expirydt", new DateTimeUtil().getCalendar("n+" + defaultdays + "d"));
            }
            if (!primary.isNull(i, "tokenvalue")) continue;
            String token = LV_AuthToken.getNextTokenId(this.database, this.getDatabaseid());
            primary.setString(i, "tokenvalue", token);
        }
    }

    private static synchronized String getNextTokenId(DBAccess db, String databaseid) throws SapphireException {
        String millis = "" + System.currentTimeMillis();
        String token = databaseid + AUTHTOKEN_DATABASE_SEPARATOR + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0)) + millis.substring(millis.length() - 4);
        token = EncryptDecrypt.encrypt(token);
        while (db.checkPreparedExists("SELECT tokenvalue FROM authtoken WHERE tokenvalue = ?", new Object[]{token})) {
            millis = "" + System.currentTimeMillis();
            token = databaseid + AUTHTOKEN_DATABASE_SEPARATOR + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0)) + millis.substring(millis.length() - 4);
            token = EncryptDecrypt.encrypt(token);
        }
        return token;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            Calendar expirydt;
            if (!primary.getValue(i, "tokenstatus").equals(STATUS_ACTIVE) || (expirydt = primary.getCalendar(i, "expirydt")) == null || !expirydt.before(Calendar.getInstance())) continue;
            throw new SapphireException("Unable to make the token active because the token has expired.");
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            DataSet externaluser;
            String externaluserid = primary.getValue(i, "externaluserid");
            String externalappid = primary.getValue(i, "externalappid");
            if (externaluserid.length() > 0 && (externaluser = this.getQueryProcessor().getPreparedSqlDataSet("SELECT externalappid, nameduserflag FROM sysuser WHERE sysuserid=? AND externalappid=? AND nameduserflag=?", (Object[])new String[]{externaluserid, externalappid, "A"})).size() == 0) {
                throw new SapphireException("External User " + externaluserid + " not recognized for access to External App " + externalappid);
            }
            if (externaluserid.length() != 0 || !primary.getValue(0, "tokenstatus").equals(STATUS_ACTIVE)) continue;
            throw new SapphireException("You must specific an External User before a token can be made Active.");
        }
        DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
        for (int i = 0; i < oldPrimary.size(); ++i) {
            String tokenvalue = oldPrimary.getValue(i, "tokenvalue");
            CacheUtil.remove(this.getDatabaseid(), "AuthToken", tokenvalue);
            CacheUtil.removeAllStartWith(this.getDatabaseid(), "AuthTokenConnectionid", tokenvalue);
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "AuthToken");
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "AuthTokenConnectionid");
    }
}

