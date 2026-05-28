/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import sapphire.util.DataSet;

public class DataLockService
extends BaseService {
    public static final String LOGNAME = "DataLockService";

    public DataLockService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public RSet createRSet(RSet rset) throws ServiceException {
        this.logInfo("Creating new RSet from rsetid '" + rset.getRsetid() + "");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            rset.setRsetid(rset.getRsetid() + "_t");
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            db.executeSQL("INSERT INTO rset ( rsetid, createdt, connectionid, externalflag ) VALUES ( '" + rset.getRsetid() + "', {ts '" + now + "'}, '" + this.sapphireConnection.getConnectionId() + "', 'Y' )");
            PreparedStatement ps1 = db.prepareStatement("INSERT INTO rsetitems ( rsetid, sdcid, keyid1, keyid2, keyid3, rsetseq, lockstate ) VALUES ( '" + rset.getRsetid() + "', ?, ?, ?, ?, ?, ? )");
            DataSet rsetitems = rset.getRsetitems();
            for (int i = 0; i < rsetitems.size(); ++i) {
                ps1.setString(1, rsetitems.getString(i, "sdcid"));
                ps1.setString(2, rsetitems.getString(i, "keyid1"));
                ps1.setString(3, rsetitems.getString(i, "keyid2"));
                ps1.setString(4, rsetitems.getString(i, "keyid3"));
                ps1.setInt(5, rsetitems.getInt(i, "rsetseq"));
                ps1.setInt(6, rsetitems.getInt(i, "lockstate"));
                ps1.executeUpdate();
            }
            db.closeStatement();
            PreparedStatement ps2 = db.prepareStatement("INSERT INTO rsetitemsds ( rsetid, sdcid, keyid1, keyid2, keyid3, rsetseq, lockstate, paramlistid, paramlistversionid, variantid, dataset ) VALUES ( '" + rset.getRsetid() + "', ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
            DataSet rsetitemsds = rset.getRsetitemsds();
            for (int i = 0; i < rsetitemsds.size(); ++i) {
                ps2.setString(1, rsetitemsds.getString(i, "sdcid"));
                ps2.setString(2, rsetitemsds.getString(i, "keyid1"));
                ps2.setString(3, rsetitemsds.getString(i, "keyid2"));
                ps2.setString(4, rsetitemsds.getString(i, "keyid3"));
                ps2.setInt(5, rsetitemsds.getInt(i, "rsetseq"));
                ps2.setInt(6, rsetitemsds.getInt(i, "lockstate"));
                ps2.setString(7, rsetitemsds.getString(i, "paramlistid"));
                ps2.setString(8, rsetitemsds.getString(i, "paramlistversionid"));
                ps2.setString(9, rsetitemsds.getString(i, "variantid"));
                ps2.setInt(10, rsetitemsds.getInt(i, "dataset"));
                ps2.executeUpdate();
            }
            db.closeStatement();
        }
        catch (Exception e) {
            throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to copy result set " + rset.getRsetid(), e);
        }
        finally {
            db.reset();
        }
        return rset;
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope) throws ServiceException {
        return this.lockRSet(rset, lockoption, lockscope, false);
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope, boolean autoTimeout) throws ServiceException {
        return this.lockRSet(rset, lockoption, lockscope, autoTimeout, false);
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope, boolean autoTimeout, boolean validateCheckout) throws ServiceException {
        this.logInfo("Locking rset '" + rset.getRsetid() + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            CallableStatement cs;
            String callstmt;
            db.setConnection(this.sapphireConnection);
            rset.setPrimaryStatus(1);
            if (lockscope == 1 || lockscope == 3) {
                callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "lockrset( ?, ?, ?, ?, ?, ? ) }";
                cs = db.prepareCall(callstmt);
                cs.registerOutParameter(1, 2);
                cs.setString(2, rset.getRsetid());
                cs.setString(3, lockoption);
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setInt(6, autoTimeout ? 0 : -1);
                cs.setString(7, validateCheckout ? "Y" : "N");
                cs.executeUpdate();
                rset.setPrimaryStatus(cs.getInt(1) == 1 ? 1 : 2);
                db.closeCall();
            }
            if (rset.getPrimaryStatus() == 1 && (lockscope == 2 || lockscope == 3)) {
                callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "lockrsetds( ?, ?, ?, ?, ? ) }";
                cs = db.prepareCall(callstmt);
                cs.registerOutParameter(1, 2);
                cs.setString(2, rset.getRsetid());
                cs.setString(3, lockoption);
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setInt(6, autoTimeout ? 0 : -1);
                cs.executeUpdate();
                rset.setDatasetStatus(cs.getInt(1) == 1 ? 1 : 2);
                db.closeCall();
            }
            RSet rSet = rset;
            return rSet;
        }
        catch (Exception e) {
            throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to lock result set " + rset.getRsetid(), e);
        }
        finally {
            db.reset();
        }
    }

    public void clearRSet(RSet rset) throws ServiceException {
        this.logInfo("Clearing rset '" + rset.getRsetid() + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String callstmt = "{call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "clearrset( ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, rset.getRsetid());
            cs.setInt(2, 0);
            cs.executeUpdate();
            db.closeCall();
        }
        catch (Exception e) {
            throw new ServiceException("CLEAR_RSET_FAILURE", "Failed to clear rset " + rset.getRsetid(), e);
        }
        finally {
            db.reset();
        }
    }

    public void clearLocks(RSet rset) throws ServiceException {
        this.logInfo("Clearing locks for rset '" + rset.getRsetid() + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String callstmt = "{call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "clearlocks( ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, rset.getRsetid());
            cs.setInt(2, 0);
            cs.executeUpdate();
            db.closeCall();
            this.logInfo("Cleared locks in RsetId: " + rset.getRsetid());
        }
        catch (Exception e) {
            throw new ServiceException("CLEAR_RSET_FAILURE", "Failed to clear locks on rset " + rset.getRsetid(), e);
        }
        finally {
            db.reset();
        }
    }
}

