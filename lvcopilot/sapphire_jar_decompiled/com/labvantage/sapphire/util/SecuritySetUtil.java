/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SecuritySetUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String SECURITYSET_ADMIN_OPERATION = "Admin";
    public static final String SECURITYSET_SDC = "LV_SecuritySet";

    public static boolean isSecuritySetOperationPermitted(String securitySetId, String currentUser, String currentUserJobType, String sdcId, String operation, QueryProcessor qp, SapphireConnection sc) throws SapphireException {
        boolean editable = false;
        try {
            editable = SecurityService.isSystemUser(sc.getDatabaseId(), currentUser);
        }
        catch (ServiceException e) {
            throw new SapphireException("An exception generated while checking for system user." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sc.getConnectionId())));
        }
        if (!editable) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT securitysetitemid, itemtypeflag FROM securitysetitem WHERE securitysetid=" + safeSQL.addVar(securitySetId) + " AND securitysetsdcid=" + safeSQL.addVar(sdcId) + "  AND operationid = " + safeSQL.addVar(operation);
            DataSet dsSecuritySetItems = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (dsSecuritySetItems.getRowCount() > 0) {
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("securitysetitemid", currentUser);
                findMap.put("itemtypeflag", "U");
                boolean bl = editable = dsSecuritySetItems.findRow(findMap) > -1;
                if (!editable && currentUserJobType != null && currentUserJobType.length() > 0) {
                    HashMap<String, String> findMap2 = new HashMap<String, String>();
                    findMap2.put("securitysetitemid", currentUserJobType);
                    findMap2.put("itemtypeflag", "J");
                    editable = dsSecuritySetItems.findRow(findMap2) > -1;
                }
            }
        }
        return editable;
    }

    public static String checkForSecuritySetReference(DataSet dsSecuritySets, DBAccess db) throws SapphireException {
        db.createResultSet("ssTables", "select tableid from sdc where accesscontrolledflag = 'S'");
        DataSet tables = new DataSet(db.getResultSet("ssTables"));
        StringBuffer refSS = new StringBuffer();
        block0: for (int s = 0; s < dsSecuritySets.getRowCount(); ++s) {
            String securitySetId = dsSecuritySets.getString(s, "securitysetid", "");
            for (int i = 0; i < tables.getRowCount(); ++i) {
                String tableId = tables.getString(i, "tableid", "");
                String sql = " select 1 from " + tableId + " where securityset = ?";
                db.createPreparedResultSet("sdiswithss", sql, new Object[]{securitySetId});
                DataSet ds = new DataSet(db.getResultSet("sdiswithss"));
                if (ds.getRowCount() <= 0) continue;
                refSS.append(",").append(securitySetId);
                continue block0;
            }
        }
        db.closeResultSet("sdiswithss");
        db.closeResultSet("ssTables");
        return refSS.length() > 0 ? refSS.substring(1) : refSS.toString();
    }

    public static String findSecuritySetsNonPermittedForAnOperation(String securitySetId, String currentUser, String currentUserJobType, String sdcId, String operation, DAMProcessor dam, QueryProcessor qp, SapphireConnection sc) throws SapphireException {
        StringBuffer nonPermittedSSet = new StringBuffer();
        String rsetid = "";
        boolean godUser = false;
        try {
            godUser = SecurityService.isSystemUser(sc.getDatabaseId(), currentUser);
        }
        catch (ServiceException e) {
            throw new SapphireException("An exception generated while checking for system user." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sc.getConnectionId())));
        }
        if (!godUser) {
            String sql;
            String[] ssIds = StringUtil.split(securitySetId, ";");
            DataSet dsSecuritySetItems = null;
            SafeSQL safeSQL = new SafeSQL();
            if (ssIds.length > 750) {
                try {
                    rsetid = dam.createRSet(SECURITYSET_SDC, securitySetId, null, null);
                    sql = "SELECT securitysetid, securitysetitemid, itemtypeflag FROM securitysetitem, rsetitems rs  where securitysetitem.securitysetid = rs.keyid1  AND securitysetitem.securitysetsdcid=" + safeSQL.addVar(sdcId) + "  AND securitysetitem.operationid = " + safeSQL.addVar(operation) + " and rs.rsetid =" + safeSQL.addVar(rsetid) + " and rs.sdcid='LV_SecuritySet'";
                    dsSecuritySetItems = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                }
                catch (SapphireException e) {
                    throw new SapphireException(e.getMessage());
                }
                finally {
                    dam.clearRSet(rsetid);
                }
            } else {
                sql = "SELECT securitysetid, securitysetitemid, itemtypeflag FROM securitysetitem  WHERE securitysetsdcid=" + safeSQL.addVar(sdcId) + "  AND operationid = " + safeSQL.addVar(operation) + " AND securitysetid in (" + safeSQL.addIn(securitySetId, ";") + ")";
                dsSecuritySetItems = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < ssIds.length; ++i) {
                boolean editable;
                filter.put("securitysetid", ssIds[i]);
                DataSet dsFilter = dsSecuritySetItems.getFilteredDataSet(filter);
                if (dsFilter.getRowCount() <= 0) continue;
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("securitysetitemid", currentUser);
                findMap.put("itemtypeflag", "U");
                boolean bl = editable = dsFilter.findRow(findMap) > -1;
                if (!editable && currentUserJobType != null && currentUserJobType.length() > 0) {
                    HashMap<String, String> findMap2 = new HashMap<String, String>();
                    findMap2.put("securitysetitemid", currentUserJobType);
                    findMap2.put("itemtypeflag", "J");
                    editable = dsFilter.findRow(findMap2) > -1;
                }
                nonPermittedSSet.append(editable ? "" : "," + ssIds[i]);
            }
        }
        if (nonPermittedSSet.length() > 0) {
            return nonPermittedSSet.substring(1);
        }
        return nonPermittedSSet.toString();
    }
}

