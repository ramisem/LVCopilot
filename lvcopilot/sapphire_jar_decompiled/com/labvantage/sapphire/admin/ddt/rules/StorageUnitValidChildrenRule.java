/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class StorageUnitValidChildrenRule
extends BaseRule {
    protected String LABVANTAGE_CVS_ID = "$Revision: 54978 $";

    public StorageUnitValidChildrenRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String getRuleId() {
        return "StorageUnitValidChildrenRule";
    }

    public void processRule(HashMap parentMap) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        HashSet<String> parentSet = new HashSet<String>();
        HashSet<String> childSet = new HashSet<String>();
        for (Object o : parentMap.keySet()) {
            String storageunitid = (String)o;
            parentSet.add((String)parentMap.get(storageunitid));
            childSet.add(storageunitid);
        }
        HashMap allowedChildMap = StorageUnitSDC.getValidChildCollection(this.getQueryProcessor(), OpalUtil.toDelimitedString(parentSet, ";"));
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select storageunit.storageunitid, storageunit.storageunittype, storageunit.labelpath");
        sql.append(" from storageunit");
        sql.append(" where storageunit.storageunitid in (").append(safeSQL.addIn(childSet)).append(")");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String storageunitid = ds.getValue(i, "storageunitid");
                String storageunittype = ds.getValue(i, "storageunittype");
                String parentid = (String)parentMap.get(storageunitid);
                List allowedChildList = (List)allowedChildMap.get(parentid);
                if (allowedChildList == null || allowedChildList.contains(storageunittype)) continue;
                StringBuilder sb = new StringBuilder();
                sb.append("{{Following movement is not allowed}}<br>");
                sb.append("{{StorageUnit}} (").append(ds.getValue(i, "labelpath")).append(")");
                sb.append(" {{to}} ");
                sb.append("{{StorageUnit}} (").append(OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "labelpath", "storageunitid = ?", new String[]{parentid})).append(")");
                throw new SapphireException("ValidChildrenRule", "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
            }
        }
        for (Object o : parentMap.keySet()) {
            List<String> errors;
            String childstorageunit = (String)o;
            String parentstorageunit = (String)parentMap.get(childstorageunit);
            DataSet trackitems = StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), childstorageunit, this.getConnectionProcessor().isOra());
            if (trackitems == null || trackitems.size() <= 0 || (errors = StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), parentstorageunit, trackitems.getColumnValues("trackitemid", ";"), this.getConnectionProcessor().getSapphireConnection())) == null || errors.size() <= 0) continue;
            throw new SapphireException("Storage Restrictions - Found " + errors.size() + " restriction failures", "VALIDATION", OpalUtil.toDelimitedString(errors, ", "));
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }
}

