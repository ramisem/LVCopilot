/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.DataLockManagerLocal;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DataAccessService
extends BaseService {
    static final String LABVANTAGE_CVS_ID = "$Revision: 98546 $";
    public static final String LOGNAME = "DataAccessService";
    private static boolean globalLock = false;

    public DataAccessService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public String touchRSet(RSet rset) throws ServiceException {
        return this.touchRSet(rset, false, -1, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String touchRSet(RSet rset, boolean relock, int locktype, String lockoption) throws ServiceException {
        String rsetTimeout = null;
        if (rset.getRsetid() == null) return rsetTimeout;
        this.logInfo("Touching RSET '" + rset + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int status;
            db.setConnection(this.sapphireConnection);
            String rsetid = rset.getRsetid();
            String callstmt = "{ ? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "touchrset( ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            String locktypestr = "";
            try {
                if (relock) {
                    if (locktype == 1) {
                        locktypestr = "sdi";
                        this.logDebug("Will relock sdi type.");
                    } else {
                        if (locktype != 2) throw new ServiceException("CREATE_LOCK_FAILURE", "Can not relock rset as no lock type provided.");
                        locktypestr = "ds";
                    }
                    if (lockoption.length() <= 0 || !lockoption.equals("LA") && !lockoption.equals("LC") && !lockoption.equals("RO") && !lockoption.equals("DS")) throw new ServiceException("CREATE_LOCK_FAILURE", "Cannot relock rset as an invalid lock option was provided.");
                    this.logDebug("Will relock " + locktypestr + " type with lock option " + lockoption + ".");
                } else {
                    locktypestr = "";
                    lockoption = "";
                }
                cs.registerOutParameter(1, 2);
                cs.setString(2, rsetid);
                cs.setInt(3, 0);
                cs.setString(4, locktypestr);
                cs.setString(5, lockoption);
                cs.executeUpdate();
                int n = status = cs.getInt(1) == 1 ? 1 : 2;
                if (relock) {
                    if (locktypestr.equalsIgnoreCase("sdi")) {
                        rset.setPrimaryStatus(status);
                    } else if (locktypestr.equalsIgnoreCase("ds")) {
                        rset.setDatasetStatus(status);
                    }
                }
            }
            finally {
                db.closeCall();
            }
            if (status != 1) {
                if (!relock || locktypestr.length() <= 0) throw new ServiceException("TOUCH_LOCK_FAILURE", "Failed to touch RSet: " + rset);
                throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to relock RSet: " + rset);
            }
            db.createPreparedResultSet("rsettimeout", "SELECT propertyvalue FROM sysconfig WHERE lower( propertyid )= ?", "rsettimeout");
            rsetTimeout = "60";
            if (!db.getNext("rsettimeout")) return rsetTimeout;
            rsetTimeout = db.getString("rsettimeout", "propertyvalue");
            return rsetTimeout;
        }
        catch (SQLException e) {
            this.logWarn("Failed to run touch RSet " + rset + ". Rset ping may be in progress: " + e.getMessage());
            return rsetTimeout;
        }
        catch (SapphireException se) {
            this.logError("Could launch touch RSet for " + rset + ":" + se.getMessage());
            return rsetTimeout;
        }
        catch (Exception e) {
            this.logWarn("Could not touch RSet " + rset + ": " + e.getMessage());
            return rsetTimeout;
        }
        finally {
            db.reset();
        }
    }

    public RSet createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord) throws ServiceException {
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, "", viewHiddenRecord);
    }

    public RSet createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list) throws ServiceException {
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, "", false);
    }

    public RSet createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, int bypassSecurityCode) throws ServiceException {
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, "", viewHiddenRecord, bypassSecurityCode);
    }

    public RSet createRSetAlt(String sdcid, String alternateKeyColumn, String alternateKeyList, boolean viewHiddenRecord) throws ServiceException {
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', alternateKeyColumn '" + alternateKeyColumn + "', alternateKeyList '" + alternateKeyList + "'");
        return this.buildRSet(sdcid, alternateKeyColumn, alternateKeyList, "", viewHiddenRecord);
    }

    public RSet createRSetAlt(String sdcid, String alternateKeyColumn, String alternateKeyList) throws ServiceException {
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', alternateKeyColumn '" + alternateKeyColumn + "', alternateKeyList '" + alternateKeyList + "'");
        return this.buildRSet(sdcid, alternateKeyColumn, alternateKeyList, "", false);
    }

    public RSet createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption, boolean viewHiddenRecord) throws ServiceException {
        this.logInfo("Creating locked RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, lockoption, viewHiddenRecord);
    }

    public RSet createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption) throws ServiceException {
        this.logInfo("Creating locked RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, lockoption, false);
    }

    private RSet buildRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption, boolean viewHiddenRecords) throws ServiceException {
        return this.buildRSet(sdcid, keyid1list, keyid2list, keyid3list, lockoption, viewHiddenRecords, 0);
    }

    private RSet buildRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, String lockoption, boolean viewHiddenRecords, int bypassSecurityCode) throws ServiceException {
        long starttime = System.currentTimeMillis();
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (keyid1list == null || keyid1list.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Keyid1 list not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int rows;
            db.setConnection(this.sapphireConnection);
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "createrset( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            DataSet ds = null;
            int startrow = 0;
            String rsetid = null;
            if (keyid1list.length() > 2000) {
                ds = new DataSet();
                ds.addColumnValues("keyid1", 0, keyid1list, ";");
                ds.addColumnValues("keyid2", 0, keyid2list, ";", "(null)");
                ds.addColumnValues("keyid3", 0, keyid3list, ";", "(null)");
                ds.padColumns();
                rows = ds.getRowCount();
            } else {
                rows = 1;
            }
            int rsetRows = 0;
            while (startrow < rows) {
                if (ds != null) {
                    keyid1list = ds.getColumnValues("keyid1", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid2list = ds.getColumnValues("keyid2", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid3list = ds.getColumnValues("keyid3", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                }
                if (keyid2list != null && keyid2list.length() == 0) {
                    keyid2list = null;
                }
                if (keyid3list != null && keyid3list.length() == 0) {
                    keyid3list = null;
                }
                cs.registerOutParameter(1, 2);
                cs.registerOutParameter(2, 12);
                cs.setString(3, this.sapphireConnection.getConnectionId());
                cs.setString(4, this.sapphireConnection.getSysuserId());
                cs.setString(5, sdcid);
                cs.setString(6, keyid1list);
                cs.setString(7, keyid2list);
                cs.setString(8, keyid3list);
                cs.setInt(9, 0);
                cs.setString(10, rsetid);
                cs.setInt(11, 0);
                cs.setInt(12, bypassSecurityCode);
                cs.setString(13, this.getViewHiddenRecordFlag(viewHiddenRecords));
                cs.executeUpdate();
                if (rsetid == null) {
                    rsetid = cs.getString(2);
                }
                rsetRows = cs.getInt(1);
                startrow += 95;
            }
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            RSet rset = new RSet(rsetid);
            rset.setSdcid(sdcid);
            if (lockoption.length() > 0) {
                rset.setRSet(this.lockRSet(rset, lockoption, 1));
            }
            if (Trace.stats) {
                Trace.setEndRSet(("".equals(lockoption) ? "" : "Locked ") + " Item RSet", rsetRows, starttime, rsetid);
            }
            RSet rSet = rset;
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list, se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list, e);
        }
        finally {
            db.reset();
        }
    }

    private RSet buildRSet(String sdcid, String alternatekeycolumnid, String alternatekeylist, String lockoption, boolean viewHiddenRecords) throws ServiceException {
        long starttime = System.currentTimeMillis();
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (alternatekeylist == null || alternatekeylist.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "alternatekeylist list not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int rows;
            db.setConnection(this.sapphireConnection);
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "createrset( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            DataSet ds = null;
            int startrow = 0;
            String rsetid = null;
            alternatekeylist = alternatekeylist.replaceAll("'", "");
            if (alternatekeylist.length() > 2000) {
                ds = new DataSet();
                ds.addColumnValues(alternatekeycolumnid, 0, alternatekeylist, ";");
                ds.padColumns();
                rows = ds.getRowCount();
            } else {
                rows = 1;
            }
            int maxkeylength = -1;
            int chuncksize = 95;
            if (ds != null) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    int currkeylen = ds.getString(i, alternatekeycolumnid, "").length();
                    if (currkeylen <= maxkeylength) continue;
                    maxkeylength = currkeylen;
                }
                chuncksize = 4000 / (maxkeylength + 1) - 5;
            }
            int rsetRows = 0;
            while (startrow < rows) {
                if (ds != null) {
                    alternatekeylist = ds.getColumnValues(alternatekeycolumnid, startrow, startrow + chuncksize > rows ? rows : startrow + chuncksize, ";");
                }
                cs.registerOutParameter(1, 2);
                cs.registerOutParameter(2, 12);
                cs.setString(3, this.sapphireConnection.getConnectionId());
                cs.setString(4, this.sapphireConnection.getSysuserId());
                cs.setString(5, sdcid);
                cs.setString(6, alternatekeylist);
                cs.setString(7, "");
                cs.setString(8, "");
                cs.setInt(9, 0);
                cs.setString(10, rsetid);
                cs.setInt(11, 0);
                cs.setInt(12, 0);
                cs.setString(13, this.getViewHiddenRecordFlag(viewHiddenRecords));
                cs.setString(14, alternatekeycolumnid);
                cs.executeUpdate();
                if (rsetid == null) {
                    rsetid = cs.getString(2);
                }
                rsetRows = cs.getInt(1);
                startrow += chuncksize;
            }
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            RSet rset = new RSet(rsetid);
            if (lockoption.length() > 0) {
                rset.setRSet(this.lockRSet(rset, lockoption, 1));
            }
            if (Trace.stats) {
                Trace.setEndRSet(("".equals(lockoption) ? "" : "Locked ") + " Item RSet", rsetRows, starttime, rsetid);
            }
            RSet rSet = rset;
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for sdcid: " + sdcid + ", alternatecolumnid: " + alternatekeycolumnid + ", alternatekeylist: " + alternatekeylist, se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for sdcid: " + sdcid + ", alternatecolumnid: " + alternatekeycolumnid + ", alternatekeylist: " + alternatekeylist, e);
        }
        finally {
            db.reset();
        }
    }

    public RSet createRSetQ(String sdcid, String queryid, String[] params, String from, String where, String orderby, String versionstatus, int retrievelimit) throws ServiceException {
        return this.createRSetQ(sdcid, queryid, params, from, where, orderby, versionstatus, retrievelimit, false, false);
    }

    public RSet createRSetQ(String sdcid, String queryid, String[] params, String from, String where, String orderby, String versionstatus, int retrievelimit, boolean useAltTable, boolean viewHiddenRecords) throws ServiceException {
        return this.createRSetQ(sdcid, queryid, params, from, where, orderby, versionstatus, retrievelimit, useAltTable, viewHiddenRecords, null);
    }

    public RSet createRSetQ(String sdcid, String queryid, String[] params, String from, String where, String orderby, String versionstatus, int retrievelimit, boolean useAltTable, boolean viewHiddenRecords, String embedsecurity) throws ServiceException {
        long starttime = System.currentTimeMillis();
        String categoryid = "";
        String[] paramsCopy = null;
        if (params != null && params.length > 0) {
            paramsCopy = new String[params.length];
            System.arraycopy(params, 0, paramsCopy, 0, params.length);
        }
        this.logInfo("Creating RSET for sdcid '" + sdcid + "', queryid '" + queryid + "', queryfrom '" + from + "'");
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (!(queryid != null && queryid.length() != 0 || from != null && from.length() != 0)) {
            throw new ServiceException("INVALID_PARAMETERS", "Queryid or queryfrom clause not specified");
        }
        if (queryid != null && queryid.length() > 0) {
            this.logInfo("Checking access for Query: '" + queryid + "', Based on: '" + sdcid + "'");
            SDI sdi = new SDI();
            sdi.setSdi("Query", queryid, sdcid, "");
            this.checkSDIAccess(sdi, viewHiddenRecords);
            if (!queryid.equalsIgnoreCase(sdi.getKeyid1())) {
                throw new ServiceException("INVALID_PARAMETERS", "Unauthorized access to Query: " + queryid + ", Based on: " + sdcid);
            }
            this.logInfo("Checking access for Query: '" + queryid + "', Based on: '" + sdcid + "' status: Authorized.");
        }
        this.checkSemicolon(where);
        if (versionstatus.length() > 5) {
            String[] vs = StringUtil.split(versionstatus, ";");
            for (int i = 0; i < vs.length; ++i) {
                if (vs[i].length() <= 1) continue;
                throw new ServiceException("INVALID_PARAMETERS", "Illegal versionstatus: " + versionstatus);
            }
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        PreparedStatement cs = null;
        try {
            db.setConnection(this.sapphireConnection);
            if (Trace.stats) {
                if (queryid != null && queryid.length() > 0) {
                    Trace.startQuery(sdcid, queryid, paramsCopy);
                } else if (from != null && where != null && from.contains("categoryitem")) {
                    int i1 = where.indexOf("'");
                    int i2 = where.indexOf("'", i1 + 1);
                    if (i1 > 0 && i2 > 0) {
                        categoryid = where.substring(i1 + 1, i2);
                        Trace.startQuery(sdcid + " Category", categoryid, paramsCopy);
                    }
                }
            }
            if (queryid != null && queryid.length() > 0) {
                db.createPreparedResultSet("SELECT argid, usersequence, argtype, allowquotesflag FROM queryarg WHERE queryid=? AND basedonid=? order by usersequence", new Object[]{queryid, sdcid});
                DataSet argData = new DataSet(db.getResultSet());
                for (int i = 0; i < argData.getRowCount(); ++i) {
                    PropertyList querywherefilter;
                    String connectionLocale = this.connectionInfo.getLocale();
                    if (connectionLocale == null || connectionLocale.length() == 0) {
                        connectionLocale = I18nUtil.getSysLocale().getDisplayName();
                    }
                    String argtype = argData.getValue(i, "argtype");
                    if (paramsCopy[i] == null || paramsCopy[i].length() <= 0) continue;
                    if ("absreldt".equals(argtype) || "dateonly".equals(argtype)) {
                        boolean isTZAware = "absreldt".equals(argData.getValue(i, "argtype"));
                        String convertedvalue = I18nUtil.convertToQueryDateString(paramsCopy[i], this.connectionInfo, isTZAware);
                        if (convertedvalue == null) {
                            throw new ServiceException("INVALID_PARAMETERS", "Illegal query param value: " + paramsCopy[i]);
                        }
                        paramsCopy[i] = convertedvalue;
                        continue;
                    }
                    if ("number".equals(argtype) && !connectionLocale.equals(I18nUtil.getSysLocale().toString())) {
                        paramsCopy[i] = I18nUtil.convertToSysNumberString(paramsCopy[i], this.connectionInfo);
                        continue;
                    }
                    if (!"N".equals(argData.getValue(i, "allowquotesflag"))) continue;
                    this.checkSemicolon(paramsCopy[i].trim().indexOf("'") == 0 ? paramsCopy[i] : "'" + paramsCopy[i] + "'");
                    PropertyList policy = Configuration.getDatabaseSecurityPolicy(this.sapphireConnection.getDatabaseId(), SecurityService.isVirtualUser(this.sapphireConnection.getConnectionId()), SecurityService.isPortalUser(this.sapphireConnection.getConnectionId()));
                    if (policy == null || SecurityPolicyUtil.isQueryWherePermitted(querywherefilter = policy.getPropertyList("querywherefilter"), paramsCopy[i].trim().indexOf("'") == 0 ? paramsCopy[i] : "'" + paramsCopy[i] + "'")) continue;
                    throw new ServiceException("INVALID_PARAMETERS", "Illegal query param value: " + paramsCopy[i]);
                }
            }
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "creatersetq( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            cs = db.prepareCall(callstmt);
            cs.registerOutParameter(1, 2);
            cs.registerOutParameter(2, 12);
            cs.registerOutParameter(3, 2);
            cs.registerOutParameter(4, 12);
            cs.setString(5, this.connectionInfo.getConnectionId());
            cs.setString(6, this.connectionInfo.getSysuserId());
            cs.setString(7, queryid);
            cs.setString(8, sdcid);
            if (from != null && from.length() > 0) {
                PropertyList sdcProps = new DDTService(this.sapphireConnection).getSDCProperties(sdcid);
                String tableidmap = sdcProps.getProperty("tableidmap");
                if (tableidmap.length() > 0) {
                    Trace.logInfo("^^Original from:" + from);
                    Trace.logInfo("^^Original where:" + where);
                    String tableid = sdcProps.getProperty("tableid");
                    String keymap1 = sdcProps.getProperty("keymap1");
                    String keycolid1 = sdcProps.getProperty("keycolid1");
                    from = DataAccessService.modifyFromClause(from, tableidmap, tableid);
                    if (where != null && where.length() > 0) {
                        where = DataAccessService.modifyWhereOrderByClause(where, tableidmap, tableid, keymap1, keycolid1);
                    }
                    Trace.logInfo("^^Modified from:" + from);
                    Trace.logInfo("^^Modified where:" + where);
                }
                where = StringUtil.replaceAll(where, "[currentuser]", this.sapphireConnection.getSysuserId());
            }
            cs.setString(9, from);
            cs.setString(10, db.isSqlServer() ? DBUtil.checkUnicode(where) : where);
            cs.setString(11, orderby);
            cs.setString(12, null);
            cs.setString(13, null);
            cs.setString(14, null);
            cs.setString(15, null);
            cs.setString(16, null);
            cs.setString(17, null);
            cs.setInt(18, 0);
            cs.setString(19, versionstatus);
            cs.setInt(20, retrievelimit);
            String delimiter = "|!|";
            StringBuffer parameterList = new StringBuffer();
            if (queryid != null && queryid.length() > 0 && paramsCopy != null && paramsCopy.length > 0) {
                for (int i = 0; i < paramsCopy.length; ++i) {
                    parameterList.append(i > 0 ? delimiter : "").append(paramsCopy[i] == null || paramsCopy[i].length() == 0 ? "[null]" : paramsCopy[i]);
                }
            }
            String textonly = "";
            cs.setString(21, parameterList.toString());
            cs.setString(22, delimiter);
            cs.setString(23, useAltTable ? "nl" : null);
            cs.setString(24, this.getViewHiddenRecordFlag(viewHiddenRecords));
            cs.setString(25, embedsecurity);
            cs.setString(26, null);
            cs.setString(27, textonly);
            this.logInfo("Calling lv_rset.creatersetq with " + this.connectionInfo.getConnectionId() + ", " + this.connectionInfo.getSysuserId() + ", " + queryid + ", " + sdcid + ", " + from + ", " + where + ", " + orderby + ", " + parameterList + ", " + delimiter);
            cs.executeUpdate();
            String rsetid = cs.getString(2);
            int qualifiedrows = cs.getInt(3);
            String sqlText = cs.getString(4);
            this.logInfo("Created RsetId: " + rsetid);
            this.logDebug("RSet-SQL: " + sqlText);
            RSet rset = new RSet(rsetid);
            if (retrievelimit > 0 && qualifiedrows == retrievelimit) {
                cs.setString(26, "Y");
                cs.executeUpdate();
                qualifiedrows = cs.getInt(1);
                this.logInfo("Max retrieve limit " + retrievelimit + " reached with qualified row count " + qualifiedrows);
            }
            db.closeCall();
            rset.setQualifiedRows(qualifiedrows);
            if (Trace.stats) {
                Trace.setEndRSet("Query RSet", qualifiedrows, starttime, rsetid);
            }
            RSet rSet = rset;
            return rSet;
        }
        catch (SQLException | SapphireException se) {
            if (cs != null) {
                try {
                    cs.setString(27, "T");
                    cs.executeUpdate();
                    String sqlText = cs.getString(4);
                    this.logError("CreateRSetQ Failed. RSet-SQL: " + sqlText);
                }
                catch (SQLException e) {
                    throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", query: " + queryid, se);
                }
            }
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", query: " + queryid, se);
        }
        finally {
            db.reset();
            if (Trace.stats) {
                if (queryid != null && queryid.length() > 0) {
                    Trace.endQuery(sdcid, queryid);
                } else if (categoryid.length() > 0) {
                    Trace.endQuery(sdcid + " Category", categoryid);
                }
            }
        }
    }

    public RSet createRSetQDS(String sdcid, String queryid, String[] params, String from, String where, String orderby, String versionstatus, int retrievelimit, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateboth, boolean calcexpand, boolean viewHiddenRecords) throws ServiceException {
        long starttime = System.currentTimeMillis();
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (!(queryid != null && queryid.length() != 0 || from != null && from.length() != 0)) {
            throw new ServiceException("INVALID_PARAMETERS", "Queryid or queryfrom clause not specified");
        }
        if (paramlistidlist == null || paramlistidlist.length() == 0) {
            paramlistidlist = "%";
        }
        if (paramlistversionidlist == null || paramlistversionidlist.length() == 0) {
            paramlistversionidlist = "%";
        }
        if (variantidlist == null || variantidlist.length() == 0) {
            variantidlist = "%";
        }
        if (datasetlist == null || datasetlist.length() == 0) {
            datasetlist = "%";
        }
        if (calcexpand && !paramlistidlist.equals("%")) {
            RSet temprset = this.createRSetQ(sdcid, queryid, params, from, where, orderby, versionstatus, retrievelimit);
            StringHolder paramlistidlistHolder = new StringHolder(paramlistidlist);
            StringHolder paramlistversionidlistHolder = new StringHolder(paramlistversionidlist);
            StringHolder variantidlistHolder = new StringHolder(variantidlist);
            this.calcExpand(temprset, paramlistidlistHolder, paramlistversionidlistHolder, variantidlistHolder);
            this.clearRSet(temprset);
            return this.createRSetQDS(sdcid, queryid, params, from, where, orderby, versionstatus, retrievelimit, paramlistidlistHolder.value, paramlistversionidlistHolder.value, variantidlistHolder.value, StringUtil.repeat(";%", StringUtil.split(paramlistidlistHolder.value, ";").length).substring(1), populateboth, false, viewHiddenRecords);
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            if (Trace.stats && queryid != null && queryid.length() > 0) {
                Trace.startQuery("DataSet (" + sdcid + ")", queryid, params);
            }
            String wildcardlist = StringUtil.repeat(";%", StringUtil.split(paramlistidlist, ";").length).substring(1);
            db.setConnection(this.sapphireConnection);
            if (!I18nUtil.getConnectionLocale(this.connectionInfo).equals(I18nUtil.getSysLocale()) || !I18nUtil.getConnectionTimeZone(this.connectionInfo).equals(I18nUtil.getSysTimeZone()) || new DateTimeUtil(this.connectionInfo).isCustomQueryFormatDefined()) {
                db.createPreparedResultSet("SELECT argid, usersequence, argtype FROM queryarg WHERE queryid=? AND basedonid=? order by usersequence", new Object[]{queryid, sdcid});
                DataSet argData = new DataSet(db.getResultSet());
                for (int i = 0; i < argData.getRowCount(); ++i) {
                    if ("absreldt".equals(argData.getValue(i, "argtype"))) {
                        params[i] = I18nUtil.convertToQueryDateString(params[i], this.connectionInfo);
                        continue;
                    }
                    if (!"number".equals(argData.getValue(i, "argtype")) || this.connectionInfo.getLocale().equals(I18nUtil.getSysLocale().getDisplayName())) continue;
                    params[i] = I18nUtil.convertToSysNumberString(params[i], this.connectionInfo);
                }
            }
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "creatersetqds( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.registerOutParameter(1, 2);
            cs.registerOutParameter(2, 12);
            cs.setString(3, this.connectionInfo.getConnectionId());
            cs.setString(4, this.connectionInfo.getSysuserId());
            cs.setString(5, queryid);
            cs.setString(6, paramlistidlist == null || paramlistidlist.length() == 0 ? "%" : paramlistidlist);
            cs.setString(7, paramlistversionidlist == null || paramlistversionidlist.length() == 0 || paramlistversionidlist.equals("%") ? wildcardlist : paramlistversionidlist);
            cs.setString(8, variantidlist == null || variantidlist.length() == 0 || variantidlist.equals("%") ? wildcardlist : variantidlist);
            cs.setString(9, datasetlist == null || datasetlist.length() == 0 || datasetlist.equals("%") ? wildcardlist : datasetlist);
            cs.setString(10, sdcid);
            cs.setString(11, from);
            cs.setString(12, where);
            cs.setString(13, orderby);
            cs.setString(14, null);
            cs.setString(15, null);
            cs.setString(16, null);
            cs.setString(17, null);
            cs.setString(18, null);
            cs.setInt(19, populateboth ? 1 : 0);
            cs.setInt(20, 0);
            cs.setString(21, versionstatus);
            cs.setInt(22, retrievelimit);
            String delimiter = "|!|";
            StringBuffer parameterList = new StringBuffer();
            if (queryid != null && queryid.length() > 0 && params != null && params.length > 0) {
                for (int i = 0; i < params.length; ++i) {
                    parameterList.append(i > 0 ? delimiter : "").append(params[i] == null || params[i].length() == 0 ? "[null]" : params[i]);
                }
            }
            cs.setString(23, parameterList.toString());
            cs.setString(24, delimiter);
            cs.setString(25, null);
            cs.setString(26, this.getViewHiddenRecordFlag(viewHiddenRecords));
            this.logInfo("Calling lv_rset.creatersetqds with " + this.connectionInfo.getConnectionId() + ", " + this.connectionInfo.getSysuserId() + ", " + queryid + ", " + sdcid + ", " + from + ", " + where + ", " + orderby + ", " + parameterList.toString() + ", " + delimiter);
            cs.executeUpdate();
            String rsetid = cs.getString(2);
            int qualifiedrows = cs.getInt(1);
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            RSet rset = new RSet(rsetid);
            rset.setQualifiedRows(qualifiedrows);
            if (Trace.stats) {
                Trace.setEndRSet("Query RSetDS", qualifiedrows, starttime, rsetid);
            }
            RSet rSet = rset;
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", query: " + queryid, se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", query: " + queryid, e);
        }
        finally {
            if (Trace.stats && queryid != null && queryid.length() > 0) {
                Trace.startQuery("DataSet (" + sdcid + ")", queryid, params);
            }
            db.reset();
        }
    }

    private void calcExpand(RSet rset, StringHolder paramlistidlistHolder, StringHolder paramlistversionidlistHolder, StringHolder variantidlistHolder) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        DataSet calcs = new DataSet();
        String sql = "SELECT distinct s.paramlistid, s.paramlistversionid, s.variantid, s.calcrule FROM sdidataitem s, rsetitems r WHERE r.rsetid = ? AND s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND s.calcrule IS NOT NULL";
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("calcrules", sql, new Object[]{rset.getRsetid()});
            calcs.setResultSet(db.getResultSet("calcrules"));
            db.closeResultSet("calcrules");
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set to load the calcs using " + sql, se);
        }
        finally {
            db.reset();
        }
        if (calcs.getRowCount() > 0) {
            this.logDebug("Here are all the calcs loaded up:");
            this.logDebug(calcs);
            int calccount = calcs.getRowCount();
            DataSet pl = new DataSet();
            pl.addColumnValues("paramlistid", 0, paramlistidlistHolder.value, ";");
            pl.addColumnValues("paramlistversionid", 0, paramlistversionidlistHolder.value, ";", "%");
            pl.addColumnValues("variantid", 0, variantidlistHolder.value, ";", "%");
            pl.padColumns();
            this.logDebug("Here are all the parameter lists trying to lock:");
            this.logDebug(pl);
            HashMap<String, String> find = new HashMap<String, String>();
            for (int i = 0; i < pl.getRowCount(); ++i) {
                String paramlistid = pl.getValue(i, "paramlistid");
                String paramlistversionid = pl.getValue(i, "paramlistversionid");
                String variantid = pl.getValue(i, "variantid");
                this.logDebug("-------------------------------------------------");
                this.logDebug("Checking: " + paramlistid + "," + variantid + " (Ver: " + paramlistversionid + ")");
                for (int j = 0; j < calccount; ++j) {
                    String calcrule = calcs.getString(j, "calcrule");
                    String calcparamlistid = calcs.getString(j, "paramlistid");
                    String calcparamlistversionid = calcs.getString(j, "paramlistversionid");
                    String calcvariantid = calcs.getString(j, "variantid");
                    this.logDebug("Processing calc rule: " + calcrule);
                    if (calcrule.indexOf("[" + paramlistid + "|") < 0 && calcrule.indexOf("[" + paramlistid + ";" + paramlistversionid + "|") < 0 && calcrule.indexOf("[" + paramlistid + ";" + paramlistversionid + ";" + variantid + "|") < 0) continue;
                    find.put("paramlistid", calcparamlistid);
                    find.put("paramlistversionid", calcparamlistversionid);
                    find.put("variantid", calcvariantid);
                    if (pl.findRow(find) >= 0) continue;
                    this.logDebug("This parameter list found in the calc rule. Adding " + calcparamlistid + "," + calcvariantid + " (Ver:" + calcparamlistversionid + ") to the list of param lists to lock");
                    int newrow = pl.addRow();
                    pl.setString(newrow, "paramlistid", calcparamlistid);
                    pl.setString(newrow, "paramlistversionid", calcparamlistversionid);
                    pl.setString(newrow, "variantid", calcvariantid);
                }
            }
            this.logDebug("--------Final set of param lists to lock ----------");
            this.logDebug(pl);
            this.logDebug("---------------------------------------------------");
            paramlistidlistHolder.value = pl.getColumnValues("paramlistid", ";");
            paramlistversionidlistHolder.value = pl.getColumnValues("paramlistversionid", ";");
            variantidlistHolder.value = pl.getColumnValues("variantid", ";");
        }
    }

    public RSet createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateboth, boolean calcexpand) throws ServiceException {
        boolean hasDataSetSecurity = "D".equals(new DDTService(this.sapphireConnection).getSDCProperties("DataSet").getProperty("accesscontrolledflag"));
        return this.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateboth, calcexpand, hasDataSetSecurity ? 2 : 0, false);
    }

    public RSet createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateboth, boolean calcexpand, int bypasscode, boolean viewHiddenRecords) throws ServiceException {
        long starttime = System.currentTimeMillis();
        this.logInfo("Creating DataSet RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "', paramlistid '" + paramlistidlist + "'");
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (keyid1list == null || keyid1list.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Keyid1 list not specified");
        }
        if (calcexpand && !paramlistidlist.equals("%")) {
            StringHolder keyid1listHolder = new StringHolder(keyid1list);
            StringHolder keyid2listHolder = new StringHolder(keyid2list);
            StringHolder keyid3listHolder = new StringHolder(keyid3list);
            StringHolder paramlistidlistHolder = new StringHolder(paramlistidlist);
            StringHolder paramlistversionidlistHolder = new StringHolder(paramlistversionidlist);
            StringHolder variantidlistHolder = new StringHolder(variantidlist);
            StringHolder datasetlistHolder = new StringHolder(datasetlist);
            this.calcExpandParallel(sdcid, keyid1listHolder, keyid2listHolder, keyid3listHolder, paramlistidlistHolder, paramlistversionidlistHolder, variantidlistHolder, datasetlistHolder);
            return this.createRSetDS(sdcid, keyid1listHolder.value, keyid2listHolder.value, keyid3listHolder.value, paramlistidlistHolder.value, paramlistversionidlistHolder.value, variantidlistHolder.value, datasetlistHolder.value, populateboth, false, bypasscode, viewHiddenRecords);
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int rows;
            db.setConnection(this.sapphireConnection);
            if (keyid2list != null && keyid2list.length() == 0) {
                keyid2list = null;
            }
            if (keyid3list != null && keyid3list.length() == 0) {
                keyid3list = null;
            }
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "creatersetds( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            DataSet ds = null;
            int startrow = 0;
            String rsetid = null;
            if (keyid1list.length() > 2000 || paramlistidlist.length() > 2000 || variantidlist.length() > 2000) {
                ds = new DataSet();
                ds.addColumnValues("keyid1", 0, keyid1list, ";");
                ds.addColumnValues("keyid2", 0, keyid2list, ";", "(null)");
                ds.addColumnValues("keyid3", 0, keyid3list, ";", "(null)");
                ds.addColumnValues("paramlistid", 0, paramlistidlist, ";", "(null)");
                ds.addColumnValues("paramlistversionid", 0, paramlistversionidlist, ";", "(null)");
                ds.addColumnValues("variantid", 0, variantidlist, ";", "(null)");
                ds.addColumnValues("dataset", 0, datasetlist, ";", "(null)");
                ds.padColumns();
                rows = ds.getRowCount();
            } else {
                rows = 1;
            }
            int rsetRows = 0;
            while (startrow < rows) {
                if (ds != null) {
                    keyid1list = ds.getColumnValues("keyid1", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid2list = ds.getColumnValues("keyid2", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid3list = ds.getColumnValues("keyid3", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    paramlistidlist = ds.getColumnValues("paramlistid", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    paramlistversionidlist = ds.getColumnValues("paramlistversionid", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    variantidlist = ds.getColumnValues("variantid", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    datasetlist = ds.getColumnValues("dataset", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                }
                if (keyid2list != null && keyid2list.length() == 0) {
                    keyid2list = null;
                }
                if (keyid3list != null && keyid3list.length() == 0) {
                    keyid3list = null;
                }
                cs.registerOutParameter(1, 2);
                cs.registerOutParameter(2, 12);
                cs.setString(3, this.connectionInfo.getConnectionId());
                cs.setString(4, this.connectionInfo.getSysuserId());
                cs.setString(5, sdcid);
                cs.setString(6, keyid1list);
                cs.setString(7, paramlistidlist);
                cs.setString(8, paramlistversionidlist);
                cs.setString(9, variantidlist);
                cs.setString(10, datasetlist);
                cs.setString(11, keyid2list);
                cs.setString(12, keyid3list);
                cs.setInt(13, 0);
                cs.setInt(14, populateboth ? 1 : 0);
                cs.setString(15, rsetid);
                cs.setInt(16, 0);
                cs.setInt(17, bypasscode);
                cs.setString(18, this.getViewHiddenRecordFlag(viewHiddenRecords));
                cs.executeUpdate();
                if (rsetid == null) {
                    rsetid = cs.getString(2);
                }
                rsetRows = cs.getInt(1);
                startrow += 95;
            }
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            if (Trace.stats) {
                Trace.setEndRSet("Item RSetDS", rsetRows, starttime, rsetid);
            }
            RSet rSet = new RSet(rsetid);
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", paramlistidlist: " + paramlistidlist + ", paramlistversionidlist: " + paramlistversionidlist + ", variantidlist: " + variantidlist + ", datasetlist: " + datasetlist + ". SapphireException: " + se.getMessage(), se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", paramlistidlist: " + paramlistidlist + ", paramlistversionidlist: " + paramlistversionidlist + ", variantidlist: " + variantidlist + ", datasetlist: " + datasetlist + ". SQLException: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public RSet createRSetWI(String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateboth) throws ServiceException {
        boolean hasWorkItemSecurity = "D".equals(new DDTService(this.sapphireConnection).getSDCProperties("WorkItem").getProperty("accesscontrolledflag"));
        return this.createRSetWI(sdcid, keyid1list, keyid2list, keyid3list, workitemidlist, workiteminstancelist, populateboth, hasWorkItemSecurity ? 2 : 0, false);
    }

    public RSet createRSetWI(String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateboth, int bypasscode, boolean viewHiddenRecords) throws ServiceException {
        long starttime = System.currentTimeMillis();
        this.logInfo("Creating DataSet RSET for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "', workitemid '" + workitemidlist + "'");
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (keyid1list == null || keyid1list.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Keyid1 list not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            int rows;
            db.setConnection(this.sapphireConnection);
            if (keyid2list != null && keyid2list.length() == 0) {
                keyid2list = null;
            }
            if (keyid3list != null && keyid3list.length() == 0) {
                keyid3list = null;
            }
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "creatersetwi( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            DataSet ds = null;
            int startrow = 0;
            String rsetid = null;
            if (keyid1list.length() > 2000) {
                ds = new DataSet();
                ds.addColumnValues("keyid1", 0, keyid1list, ";");
                ds.addColumnValues("keyid2", 0, keyid2list, ";", "(null)");
                ds.addColumnValues("keyid3", 0, keyid3list, ";", "(null)");
                ds.addColumnValues("workitemid", 0, workitemidlist, ";", "(null)");
                ds.addColumnValues("workiteminstance", 0, workiteminstancelist, ";", "(null)");
                ds.padColumns();
                rows = ds.getRowCount();
            } else {
                rows = 1;
            }
            int rsetRows = 0;
            while (startrow < rows) {
                if (ds != null) {
                    keyid1list = ds.getColumnValues("keyid1", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid2list = ds.getColumnValues("keyid2", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid3list = ds.getColumnValues("keyid3", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    workitemidlist = ds.getColumnValues("workitemid", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    workiteminstancelist = ds.getColumnValues("workiteminstance", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                }
                if (keyid2list != null && keyid2list.length() == 0) {
                    keyid2list = null;
                }
                if (keyid3list != null && keyid3list.length() == 0) {
                    keyid3list = null;
                }
                cs.registerOutParameter(1, 2);
                cs.registerOutParameter(2, 12);
                cs.setString(3, this.connectionInfo.getConnectionId());
                cs.setString(4, this.connectionInfo.getSysuserId());
                cs.setString(5, sdcid);
                cs.setString(6, keyid1list);
                cs.setString(7, workitemidlist);
                cs.setString(8, workiteminstancelist);
                cs.setString(9, keyid2list);
                cs.setString(10, keyid3list);
                cs.setInt(11, 0);
                cs.setInt(12, populateboth ? 1 : 0);
                cs.setString(13, rsetid);
                cs.setInt(14, 0);
                cs.setInt(15, bypasscode);
                cs.setString(16, this.getViewHiddenRecordFlag(viewHiddenRecords));
                cs.executeUpdate();
                if (rsetid == null) {
                    rsetid = cs.getString(2);
                }
                rsetRows = cs.getInt(1);
                startrow += 95;
            }
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            if (Trace.stats) {
                Trace.setEndRSet("Item RSetWI", rsetRows, starttime, rsetid);
            }
            RSet rSet = new RSet(rsetid);
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", workitemidlist: " + workitemidlist + ", workiteminstancelist: " + workiteminstancelist + ". SapphireException: " + se.getMessage(), se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", workitemidlist: " + workitemidlist + ", workiteminstancelist: " + workiteminstancelist + ". SQLException: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    private void calcExpandParallel(String sdcid, StringHolder keyid1listHolder, StringHolder keyid2listHolder, StringHolder keyid3listHolder, StringHolder paramlistidlistHolder, StringHolder paramlistversionidlistHolder, StringHolder variantidlistHolder, StringHolder datasetlistHolder) throws ServiceException {
        RSet rset = this.createRSet(sdcid, keyid1listHolder.value, keyid2listHolder.value, keyid3listHolder.value);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        DataSet calcs = new DataSet();
        String sql = "SELECT distinct s.keyid1, s.keyid2, s.keyid3, s.paramlistid, s.paramlistversionid, s.variantid, s.calcrule FROM sdidataitem s, rsetitems r WHERE r.rsetid = ? AND s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND s.calcrule IS NOT NULL";
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("calcrules", sql, new Object[]{rset.getRsetid()});
            calcs.setResultSet(db.getResultSet("calcrules"));
            db.closeResultSet("calcrules");
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set to load the calcs using " + sql, se);
        }
        finally {
            db.reset();
        }
        if (calcs.getRowCount() > 0) {
            this.logDebug("Here are all the calcs loaded up:");
            this.logDebug(calcs);
            DataSet pl = new DataSet();
            pl.addColumnValues("keyid1", 0, keyid1listHolder.value, ";");
            pl.addColumnValues("keyid2", 0, keyid2listHolder.value, ";");
            pl.addColumnValues("keyid3", 0, keyid3listHolder.value, ";");
            pl.addColumnValues("paramlistid", 0, paramlistidlistHolder.value, ";");
            pl.addColumnValues("paramlistversionid", 0, paramlistversionidlistHolder.value, ";", "%");
            pl.addColumnValues("variantid", 0, variantidlistHolder.value, ";", "%");
            pl.addColumnValues("dataset", 0, datasetlistHolder.value, ";", "%");
            pl.padColumns();
            this.logDebug("Here are all the parameter lists trying to lock:");
            this.logDebug(pl);
            HashMap<String, String> find = new HashMap<String, String>();
            HashMap<String, String> calcfilter = new HashMap<String, String>();
            for (int i = 0; i < pl.getRowCount(); ++i) {
                String keyid1 = pl.getValue(i, "keyid1");
                String keyid2 = pl.getValue(i, "keyid2");
                String keyid3 = pl.getValue(i, "keyid3");
                String paramlistid = pl.getValue(i, "paramlistid");
                String paramlistversionid = pl.getValue(i, "paramlistversionid");
                String variantid = pl.getValue(i, "variantid");
                String dataset = pl.getValue(i, "dataset");
                this.logDebug("-------------------------------------------------");
                this.logDebug("Checking: " + paramlistid + "," + variantid + " (Ver: " + paramlistversionid + ")");
                calcfilter.put("keyid1", keyid1);
                calcfilter.put("keyid2", keyid2);
                calcfilter.put("keyid3", keyid3);
                DataSet sdicalcs = calcs.getFilteredDataSet(calcfilter);
                int calccount = sdicalcs.getRowCount();
                for (int j = 0; j < calccount; ++j) {
                    String calcrule = sdicalcs.getString(j, "calcrule");
                    String calcparamlistid = sdicalcs.getString(j, "paramlistid");
                    String calcparamlistversionid = sdicalcs.getString(j, "paramlistversionid");
                    String calcvariantid = sdicalcs.getString(j, "variantid");
                    this.logDebug("Processing calc rule: " + calcrule);
                    if (calcrule.indexOf("[" + paramlistid + "|") < 0 && calcrule.indexOf("[" + paramlistid + ";" + paramlistversionid + "|") < 0 && calcrule.indexOf("[" + paramlistid + ";" + paramlistversionid + ";" + variantid + "|") < 0) continue;
                    find.put("keyid1", keyid1);
                    find.put("keyid2", keyid2);
                    find.put("keyid3", keyid3);
                    find.put("paramlistid", calcparamlistid);
                    find.put("paramlistversionid", calcparamlistversionid);
                    find.put("variantid", calcvariantid);
                    find.put("dataset", dataset);
                    if (pl.findRow(find) >= 0) continue;
                    this.logDebug("This parameter list found in the calc rule. Adding " + calcparamlistid + "," + calcvariantid + " (Ver:" + calcparamlistversionid + ") to the list of param lists to lock");
                    int newrow = pl.addRow();
                    pl.setString(newrow, "keyid1", keyid1);
                    pl.setString(newrow, "keyid2", keyid2);
                    pl.setString(newrow, "keyid3", keyid3);
                    pl.setString(newrow, "paramlistid", calcparamlistid);
                    pl.setString(newrow, "paramlistversionid", calcparamlistversionid);
                    pl.setString(newrow, "variantid", calcvariantid);
                    pl.setString(newrow, "dataset", dataset);
                }
            }
            this.logDebug("--------Final set of param lists to lock ----------");
            this.logDebug(pl);
            this.logDebug("---------------------------------------------------");
            keyid1listHolder.value = pl.getColumnValues("keyid1", ";");
            keyid2listHolder.value = pl.getColumnValues("keyid2", ";");
            keyid3listHolder.value = pl.getColumnValues("keyid3", ";");
            paramlistidlistHolder.value = pl.getColumnValues("paramlistid", ";");
            paramlistversionidlistHolder.value = pl.getColumnValues("paramlistversionid", ";");
            variantidlistHolder.value = pl.getColumnValues("variantid", ";");
            datasetlistHolder.value = pl.getColumnValues("dataset", ";");
        }
        this.clearRSet(rset);
    }

    public RSet createRSetDSNP(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateboth, boolean calcexpand, boolean viewHiddenRecords) throws ServiceException {
        boolean hasDataSetSecurity = "D".equals(new DDTService(this.sapphireConnection).getSDCProperties("DataSet").getProperty("accesscontrolledflag"));
        return this.createRSetDSNP(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, populateboth, calcexpand, hasDataSetSecurity ? 2 : 0, viewHiddenRecords);
    }

    public RSet createRSetDSNP(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean populateboth, boolean calcexpand, int bypasssecuritycode, boolean viewHiddenRecords) throws ServiceException {
        long starttime = System.currentTimeMillis();
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCId not specified");
        }
        if (keyid1list == null || keyid1list.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Keyid1 list not specified");
        }
        if (paramlistidlist == null || paramlistidlist.length() == 0) {
            paramlistidlist = "%";
        }
        String wildcardlist = StringUtil.repeat(";%", StringUtil.split(paramlistidlist, ";").length).substring(1);
        if (paramlistversionidlist == null || paramlistversionidlist.length() == 0 || paramlistversionidlist.equals("%")) {
            paramlistversionidlist = wildcardlist;
        }
        if (variantidlist == null || variantidlist.length() == 0 || variantidlist.equals("%")) {
            variantidlist = wildcardlist;
        }
        if (datasetlist == null || datasetlist.length() == 0 || datasetlist.equals("%")) {
            datasetlist = wildcardlist;
        }
        if (calcexpand && !paramlistidlist.equals("%")) {
            RSet temprset = this.createRSet(sdcid, keyid1list, keyid2list, keyid3list);
            StringHolder paramlistidlistHolder = new StringHolder(paramlistidlist);
            StringHolder paramlistversionidlistHolder = new StringHolder(paramlistversionidlist);
            StringHolder variantidlistHolder = new StringHolder(variantidlist);
            this.calcExpand(temprset, paramlistidlistHolder, paramlistversionidlistHolder, variantidlistHolder);
            this.clearRSet(temprset);
            return this.createRSetDSNP(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlistHolder.value, paramlistversionidlistHolder.value, variantidlistHolder.value, StringUtil.repeat(";%", StringUtil.split(paramlistidlistHolder.value, ";").length).substring(1), populateboth, false, bypasssecuritycode, viewHiddenRecords);
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "creatersetdsnp( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.registerOutParameter(1, 2);
            cs.registerOutParameter(2, 12);
            if (keyid2list != null && keyid2list.length() == 0) {
                keyid2list = null;
            }
            if (keyid3list != null && keyid3list.length() == 0) {
                keyid3list = null;
            }
            if (paramlistidlist.length() > 4000 || paramlistversionidlist.length() > 4000 || variantidlist.length() > 4000 || datasetlist.length() > 4000) {
                throw new ServiceException("CREATE_RSET_FAILURE", "Check the lengths of the paramlistid lists (maxlimit 4000). Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", paramlistidlist: " + paramlistidlist + ", paramlistversionidlist: " + paramlistversionidlist + ", variantidlist: " + variantidlist);
            }
            DataSet ds = null;
            int rows = 0;
            int startrow = 0;
            String rsetid = null;
            if (keyid1list.length() > 2000) {
                ds = new DataSet();
                ds.addColumnValues("keyid1", 0, keyid1list, ";");
                ds.addColumnValues("keyid2", 0, keyid2list, ";", "(null)");
                ds.addColumnValues("keyid3", 0, keyid3list, ";", "(null)");
                ds.padColumns();
                rows = ds.getRowCount();
            } else {
                rows = 1;
            }
            int rsetRows = 0;
            while (startrow < rows) {
                if (ds != null) {
                    keyid1list = ds.getColumnValues("keyid1", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid2list = ds.getColumnValues("keyid2", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    keyid3list = ds.getColumnValues("keyid3", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                }
                if (keyid2list != null && keyid2list.length() == 0) {
                    keyid2list = null;
                }
                if (keyid3list != null && keyid3list.length() == 0) {
                    keyid3list = null;
                }
                cs.registerOutParameter(1, 2);
                cs.registerOutParameter(2, 12);
                cs.setString(3, this.connectionInfo.getConnectionId());
                cs.setString(4, this.connectionInfo.getSysuserId());
                cs.setString(5, sdcid);
                cs.setString(6, keyid1list);
                cs.setString(7, paramlistidlist);
                cs.setString(8, paramlistversionidlist);
                cs.setString(9, variantidlist);
                cs.setString(10, datasetlist);
                cs.setString(11, keyid2list);
                cs.setString(12, keyid3list);
                cs.setInt(13, 0);
                cs.setInt(14, populateboth ? 1 : 0);
                cs.setInt(15, 0);
                cs.setString(16, rsetid);
                cs.setString(17, this.getViewHiddenRecordFlag(viewHiddenRecords));
                cs.setInt(18, bypasssecuritycode);
                cs.executeUpdate();
                if (rsetid == null) {
                    rsetid = cs.getString(2);
                }
                rsetRows = cs.getInt(1);
                startrow += 95;
            }
            db.closeCall();
            this.logInfo("Created RsetId: " + rsetid);
            if (Trace.stats) {
                Trace.setEndRSet("Item RSetDSNP", rsetRows, starttime, rsetid);
            }
            RSet rSet = new RSet(rsetid);
            return rSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", paramlistidlist: " + paramlistidlist + ", paramlistversionidlist: " + paramlistversionidlist + ", variantidlist: " + variantidlist, se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create result set for sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list + ", paramlistidlist: " + paramlistidlist + ", paramlistversionidlist: " + paramlistversionidlist + ", variantidlist: " + variantidlist, e);
        }
        finally {
            db.reset();
        }
    }

    public RSet createLockedRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ServiceException {
        RSet rset = this.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, false, calcexpand);
        return this.lockRSet(rset, lockoption, 2);
    }

    public RSet createLockedRSetDSNP(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, String lockoption, boolean calcexpand) throws ServiceException {
        RSet rset = this.createRSetDSNP(sdcid, keyid1list, keyid2list, keyid3list, paramlistidlist, paramlistversionidlist, variantidlist, datasetlist, false, calcexpand, 0, false);
        return this.lockRSet(rset, lockoption, 2);
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope) throws ServiceException {
        return this.lockRSet(rset, lockoption, lockscope, false);
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope, boolean autoTimeout) throws ServiceException {
        boolean isValidateCheckout = false;
        if (rset.getSdcid() != null && "Y".equals(CMTPolicy.getPolicy(this.getConnectionid(), rset.getSdcid()).getChangeControlledFlag())) {
            isValidateCheckout = true;
        }
        return this.lockRSet(rset, lockoption, lockscope, autoTimeout, isValidateCheckout);
    }

    public RSet lockRSet(RSet rset, String lockoption, int lockscope, boolean autoTimeout, boolean isValidateCheckout) throws ServiceException {
        long starttime = System.currentTimeMillis();
        this.logInfo("Locking Rset '" + rset.getRsetid() + "'");
        rset.setPrimaryStatus(1);
        rset.setDatasetStatus(1);
        if (globalLock) {
            throw new ServiceException("CREATE_LOCK_FAILURE", "Error locking rsetid: " + rset.getRsetid() + " because a global lock is in place");
        }
        if (lockoption == null || lockoption.length() == 0) {
            lockoption = "DA";
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            if (this.connectionInfo.isOracle()) {
                db.createPreparedResultSet("rsetitems", "SELECT * FROM rsetitems WHERE rsetid = ?", new Object[]{rset.getRsetid()});
                rset.setRsetitems(new DataSet(db.getResultSet("rsetitems")));
                db.createPreparedResultSet("rsetitemsds", "SELECT * FROM rsetitemsds WHERE rsetid = ?", new Object[]{rset.getRsetid()});
                rset.setRsetitemsds(new DataSet(db.getResultSet("rsetitemsds")));
                RSet originalRSet = new RSet(rset.getRsetid());
                DataLockManagerLocal dlm = ServiceLocator.getInstance().getDataLockManager();
                try {
                    rset.setRSet(dlm.createRSet(this.sapphireConnection.getConnectionId(), rset));
                    rset.setRSet(dlm.lockRSet(this.sapphireConnection.getConnectionId(), rset, lockoption, lockscope, autoTimeout, isValidateCheckout));
                    if (lockoption.equals("DA") && (rset.getPrimaryStatus() == 2 || rset.getDatasetStatus() == 2)) {
                        throw new ServiceException("CREATE_LOCK_FAILURE", "Error locking rsetid: " + rset + " with lock all option");
                    }
                }
                catch (Exception e) {
                    try {
                        dlm.clearRSet(this.sapphireConnection.getConnectionId(), rset);
                    }
                    catch (Exception re) {
                        e = re;
                    }
                    throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to lock rsetid: " + rset.getRsetid(), e);
                }
                this.clearRSet(originalRSet);
                this.logInfo("New RSet generated to get lock on current rset: " + rset + " (" + originalRSet + " has been deleted).");
            } else {
                CallableStatement cs;
                String callstmt;
                if (lockscope == 1 || lockscope == 3) {
                    callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "lockrset( ?, ?, ?, ?, ?, ? ) }";
                    cs = db.prepareCall(callstmt);
                    cs.registerOutParameter(1, 2);
                    cs.setString(2, rset.getRsetid());
                    cs.setString(3, lockoption);
                    cs.setInt(4, 0);
                    cs.setInt(5, 0);
                    cs.setInt(6, autoTimeout ? 0 : -1);
                    cs.setString(7, isValidateCheckout ? "Y" : "N");
                    cs.executeUpdate();
                    rset.setPrimaryStatus(cs.getInt(1) == 1 ? 1 : 2);
                    db.closeCall();
                    if (rset.getPrimaryStatus() == 2 && lockoption.equals("DA")) {
                        throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to lock rsetid: " + rset);
                    }
                }
                if (lockscope == 2 || lockscope == 3) {
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
                    if (rset.getDatasetStatus() == 2 && lockoption.equals("DA")) {
                        throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to lock rsetid: " + rset);
                    }
                }
            }
            if (Trace.stats) {
                boolean ds = false;
                int size = db.getPreparedCount("SELECT count(*) FROM rsetitems WHERE rsetid = ? ", rset.getRsetid());
                if (size == 0) {
                    size = db.getPreparedCount("SELECT count(*) FROM rsetitemsds WHERE rsetid = ? ", rset.getRsetid());
                    ds = true;
                }
                Trace.setEndRSet("Locking RSet" + (ds ? "DS" : ""), size, starttime, rset.getRsetid());
            }
        }
        catch (SapphireException se) {
            throw new ServiceException("CREATE_LOCK_FAILURE", "Sapphire Exception locking rset " + rset.getRsetid() + ". Exception: " + se.getMessage(), se);
        }
        catch (SQLException e) {
            throw new ServiceException("CREATE_LOCK_FAILURE", "Failed to lock rset '" + rset + "'. Exception: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
        return rset;
    }

    public void clearRSet(RSet rset) throws ServiceException {
        if (rset != null) {
            long starttime = System.currentTimeMillis();
            this.logInfo("Clearing RSET '" + rset + "'");
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                int size = 0;
                boolean isDataSet = false;
                if (Trace.stats) {
                    size = db.getPreparedCount("SELECT count(*) FROM rsetitems WHERE rsetid = ? ", rset.getRsetid());
                }
                if (Trace.stats && size == 0) {
                    size = db.getPreparedCount("SELECT count(*) FROM rsetitemsds WHERE rsetid = ? ", rset.getRsetid());
                    isDataSet = size > 0;
                }
                db.createPreparedResultSet("externalflag", "SELECT externalflag FROM rset WHERE rsetid = ?", rset.getRsetid());
                if (db.getNext("externalflag")) {
                    String externalflag = db.getString("externalflag", "externalflag");
                    if (externalflag != null && externalflag.equals("Y")) {
                        ServiceLocator.getInstance().getDataLockManager().clearRSet(this.sapphireConnection.getConnectionId(), rset);
                    } else {
                        String callstmt = "{call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "clearrset( ?, ? ) }";
                        CallableStatement cs = db.prepareCall(callstmt);
                        cs.setString(1, rset.getRsetid());
                        cs.setInt(2, 0);
                        cs.executeUpdate();
                        db.closeCall();
                    }
                }
                db.closeResultSet("externalflag");
                if (Trace.stats) {
                    Trace.setEndRSet("Clear RSet" + (isDataSet ? "DS" : ""), size, starttime, rset.getRsetid());
                }
            }
            catch (Exception e) {
                throw new ServiceException("CLEAR_RSET_FAILURE", "Failed to clear rset '" + rset + "'. Exception: " + e.getMessage(), e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void clearRSets(String rsetlist) throws ServiceException {
        this.logInfo("Clearing rsets");
        String callstmt = "{call lv_rset" + (this.sapphireConnection.isOracle() ? "." : "_") + "clearrset( ?, ? ) }";
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, StringUtil.replaceAll(rsetlist, "|", ";"));
            cs.setInt(2, 0);
            cs.executeUpdate();
            db.closeCall();
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to delete rsets. Exception: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void clearLocks(RSet rset) throws ServiceException {
        if (rset.getRsetid() != null) {
            this.logInfo("Clearing locks for RSET '" + rset + "'");
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("externalflag", "SELECT externalflag FROM rset WHERE rsetid = ?", rset.getRsetid());
                if (db.getNext("externalflag")) {
                    String externalflag = db.getString("externalflag", "externalflag");
                    if (externalflag != null && externalflag.equals("Y")) {
                        ServiceLocator.getInstance().getDataLockManager().clearLocks(this.sapphireConnection.getConnectionId(), rset);
                    } else {
                        String callstmt = "{call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "clearlocks( ?, ? ) }";
                        CallableStatement cs = db.prepareCall(callstmt);
                        cs.setString(1, rset.getRsetid());
                        cs.setInt(2, 0);
                        cs.executeUpdate();
                        db.closeCall();
                        this.logInfo("Cleared locks in RsetId: " + rset);
                    }
                }
                db.closeResultSet("externalflag");
            }
            catch (Exception e) {
                throw new ServiceException("CLEAR_RSET_FAILURE", "Failed to clear locks for rset' " + rset + "'. Exception: " + e.getMessage(), e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void timeoutRSets(int timeouttime) throws ServiceException {
        this.logDebug("Timing out RSets");
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            if (timeouttime > 0) {
                if (dbu.isOracle()) {
                    dbu.createResultSet("SELECT rsetid FROM rset WHERE lastmoddt is not null AND lastmoddt < ( sysdate - " + timeouttime + "/86400 )");
                } else {
                    dbu.createResultSet("SELECT rsetid FROM rset WHERE lastmoddt is not null AND lastmoddt < DateAdd(s,-" + timeouttime + ",GetDate())");
                }
                while (dbu.getNext()) {
                    this.clearLocks(new RSet(dbu.getString("rsetid")));
                }
            }
        }
        catch (Exception e) {
            throw new ServiceException("CLEAR_RSET_FAILURE", "Failed to timeout rset locks for database '" + this.connectionInfo.getDatabaseId() + "'", e);
        }
        finally {
            dbu.reset();
        }
    }

    public synchronized boolean setGlobalLock(boolean lock) throws ServiceException {
        boolean locked = true;
        if (lock) {
            if (!globalLock) {
                DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
                try {
                    dbu.setConnection(this.sapphireConnection);
                    StringBuffer userList = new StringBuffer();
                    dbu.createResultSet("SELECT DISTINCT connection.sysuserid FROM connection, rset, rsetitems WHERE connection.connectionid = rset.connectionid AND rset.rsetid = rsetitems.rsetid AND lockstate = 2 UNION SELECT DISTINCT connection.sysuserid FROM connection, rset, rsetitemsds WHERE connection.connectionid = rset.connectionid AND rset.rsetid = rsetitemsds.rsetid AND lockstate = 2");
                    while (dbu.getNext()) {
                        userList.append(";").append(dbu.getString("sysuserid"));
                    }
                    if (userList.length() == 0) {
                        globalLock = true;
                    }
                    this.logError("Global lock failure. The following users have locks: " + userList);
                    locked = false;
                }
                catch (SapphireException se) {
                    throw new ServiceException("DB_ACTION_FAILED", "Failed to set global lock. Sapphire Exception: " + se.getMessage(), se);
                }
                finally {
                    dbu.reset();
                }
            }
        } else {
            globalLock = false;
        }
        return locked;
    }

    public boolean isGlobalLock() {
        return globalLock;
    }

    public boolean checkRESTAccess(String sdcid, String keyid1list, String keyid2list, String keyid3list, String restrictivewhere, String operation) throws ServiceException {
        this.logInfo("Check REST request for sdcid '" + sdcid + "', keyid1 '" + keyid1list + "', keyid2 '" + keyid2list + "', keyid3 '" + keyid3list + "'");
        String callstmt = "{call lv_rset" + (this.sapphireConnection.isOracle() ? "." : "_") + "RESTCheck( ?, ?, ?, ?, ?, ?, ?, ? ) }";
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            CallableStatement cs = dbu.prepareCall(callstmt);
            cs.registerOutParameter(1, 12);
            cs.setString(2, sdcid);
            cs.setString(3, keyid1list);
            cs.setString(4, keyid2list);
            cs.setString(5, keyid3list);
            cs.setString(6, restrictivewhere);
            cs.setString(7, operation);
            cs.setString(8, this.sapphireConnection.getSysuserId());
            cs.executeUpdate();
            boolean bl = cs.getString(1).equals("Y");
            return bl;
        }
        catch (Exception e) {
            throw new ServiceException("Failed to determine access list for list based request. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            dbu.reset();
        }
    }

    public SDI checkSDIAccess(SDIList sdiList, boolean viewHiddenRecords) throws ServiceException {
        return this.checkSDIAccess(sdiList, viewHiddenRecords, "list");
    }

    public SDI checkSDIAccess(SDIList sdiList, boolean viewHiddenRecords, String operation) throws ServiceException {
        SDI sdi = new SDI(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(), sdiList.getKeyid3());
        this.checkSDIAccess(sdi, viewHiddenRecords, operation);
        return sdi;
    }

    public void checkSDIAccess(SDI sdi, boolean viewHiddenRecords) throws ServiceException {
        this.checkSDIAccess(sdi, viewHiddenRecords, "list");
    }

    public void checkSDIAccess(SDI sdi, boolean viewHiddenRecords, String operation) throws ServiceException {
        if (!this.sapphireConnection.getSysuserId().equals("(system)")) {
            String callstmt = "{? = call lv_rset" + (this.sapphireConnection.isOracle() ? "." : "_") + "sdiaccesslist( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                int rows;
                dbu.setConnection(this.sapphireConnection);
                CallableStatement cs = dbu.prepareCall(callstmt);
                DataSet ds = null;
                int startrow = 0;
                if (sdi.getKeyid1().length() > 2000) {
                    ds = new DataSet();
                    ds.addColumnValues("keyid1", 0, sdi.getKeyid1(), ";");
                    ds.addColumnValues("keyid2", 0, sdi.getKeyid2(), ";", "(null)");
                    ds.addColumnValues("keyid3", 0, sdi.getKeyid3(), ";", "(null)");
                    ds.padColumns();
                    rows = ds.getRowCount();
                } else {
                    rows = 1;
                }
                String keyid1list = "";
                String keyid2list = "";
                String keyid3list = "";
                while (startrow < rows) {
                    if (ds != null) {
                        keyid1list = ds.getColumnValues("keyid1", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                        keyid2list = ds.getColumnValues("keyid2", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                        keyid3list = ds.getColumnValues("keyid3", startrow, startrow + 95 > rows ? rows : startrow + 95, ";");
                    } else {
                        keyid1list = sdi.getKeyid1();
                        keyid2list = sdi.getKeyid2();
                        keyid3list = sdi.getKeyid3();
                    }
                    cs.registerOutParameter(1, 2);
                    cs.setString(2, sdi.getSdcid());
                    cs.setString(3, keyid1list);
                    cs.setString(4, keyid2list);
                    cs.setString(5, keyid3list);
                    cs.registerOutParameter(6, 12);
                    cs.registerOutParameter(7, 12);
                    cs.registerOutParameter(8, 12);
                    cs.setString(9, this.sapphireConnection.getSysuserId());
                    cs.setString(10, operation);
                    cs.setString(11, this.getViewHiddenRecordFlag(viewHiddenRecords));
                    cs.setString(12, this.sapphireConnection.getCurrentJobtype());
                    cs.executeUpdate();
                    String returnedkeyid1list = cs.getString(6);
                    String returnedkeyid2list = cs.getString(7);
                    String returnedkeyid3list = cs.getString(8);
                    if (startrow == 0 || sdi.getKeyid1().length() == 0) {
                        sdi.setSdi(sdi.getSdcid(), returnedkeyid1list == null ? "" : returnedkeyid1list, returnedkeyid2list, returnedkeyid3list);
                    } else if (returnedkeyid1list != null) {
                        sdi.setSdi(sdi.getSdcid(), sdi.getKeyid1() + ";" + returnedkeyid1list, sdi.getKeyid2() + ";" + returnedkeyid2list, sdi.getKeyid3() + ";" + returnedkeyid3list);
                    }
                    startrow += 95;
                }
            }
            catch (Exception e) {
                throw new ServiceException("Failed to determine access list for list based request. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            finally {
                dbu.reset();
            }
        }
    }

    private String getViewHiddenRecordFlag(boolean viewHiddenRecords) throws ServiceException {
        if (!viewHiddenRecords) {
            viewHiddenRecords = "(system)".equals(this.connectionInfo.getSysuserId()) || "Y".equals(new ConfigService(this.sapphireConnection).getProfileProperty(this.connectionInfo.getSysuserId(), "viewhidden", "N"));
        }
        return viewHiddenRecords ? "Y" : null;
    }

    private void checkSemicolon(String where) throws ServiceException {
        if (this.sapphireConnection.isSqlServer() && where != null && where.contains(";")) {
            String[] tokens = StringUtil.getTokens(where, "'", "'");
            for (int t = 0; t < tokens.length; ++t) {
                where = StringUtil.replaceAll(where, "'" + tokens[t] + "'", "");
            }
            if (where.indexOf(";") >= 0) {
                throw new ServiceException("INVALID_PARAMETERS", "Illegal semicolon in Querywhere clause after removing literal: " + where);
            }
        }
    }

    public static String modifyWhereOrderByClause(String value, String oldtableid, String newtableid, String oldkeyid1, String newkeyid1) {
        value = value.replaceAll("(?i)\\(" + oldtableid + "\\.", "\\(" + newtableid + "\\.");
        value = value.replaceAll("(?i) " + oldtableid + "\\.", " " + newtableid + "\\.");
        value = value.replaceAll("(?i)," + oldtableid + "\\.", "," + newtableid + "\\.");
        value = value.replaceAll("(?i)^" + oldtableid + "\\.", "" + newtableid + "\\.");
        value = value.replaceAll("(?i)\\." + oldkeyid1, "\\." + newkeyid1);
        value = value.replaceAll("(?i)," + oldkeyid1, "," + newkeyid1);
        value = value.replaceAll("(?i) " + oldkeyid1, " " + newkeyid1);
        return value;
    }

    public static String modifyFromClause(String value, String oldtableid, String newtableid) {
        value = value.replaceAll("(?i)^" + oldtableid + "$", newtableid);
        value = value.replaceAll("(?i)^" + oldtableid + ",", newtableid + ",");
        value = value.replaceAll("(?i)," + oldtableid + "$", "," + newtableid);
        value = value.replaceAll("(?i)," + oldtableid + ",", "," + newtableid + ",");
        value = value.replaceAll("(?i) " + oldtableid + ",", " " + newtableid + ",");
        value = value.replaceAll("(?i) " + oldtableid + "$", " " + newtableid);
        return value;
    }
}

