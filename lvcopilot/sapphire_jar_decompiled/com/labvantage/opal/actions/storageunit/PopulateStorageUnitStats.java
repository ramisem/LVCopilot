/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import com.labvantage.opal.actions.storageunit.SyncLastNodeCapacity;
import com.labvantage.opal.util.OpalUtil;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class PopulateStorageUnitStats
extends BaseAction
implements sapphire.action.PopulateStorageUnitStats {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 66833 $";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        DataSet queryds;
        String toplevelfunction;
        String storageunitid = actionProps.getProperty("storageunitid");
        String querywhere = actionProps.getProperty("querywhere");
        HashSet<String> topLevelSet = new HashSet<String>();
        String string = toplevelfunction = this.database.isOracle() ? "LV_SUS.FindTopLevel" : "dbo.LV_SUS_FindTopLevel";
        if (OpalUtil.isNotEmpty(storageunitid)) {
            DataSet topds;
            if (storageunitid.split(";").length > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", storageunitid, null, null);
                topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in (" + safeSQL.addIn(storageunitid, ";") + ")", safeSQL.getValues());
            }
            if (topds != null) {
                for (int i = 0; i < topds.size(); ++i) {
                    topLevelSet.add(topds.getString(i, "toplevelid"));
                }
            }
        } else if (OpalUtil.isNotEmpty(querywhere) && (queryds = this.getQueryProcessor().getSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where " + querywhere)) != null) {
            for (int i = 0; i < queryds.size(); ++i) {
                topLevelSet.add(queryds.getString(i, "toplevelid"));
            }
        }
        if (topLevelSet.size() > 0) {
            String topLevelStorageUnits = OpalUtil.toDelimitedString(topLevelSet, ";");
            PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
            if ("Manual Only".equals(policy.getPropertyListNotNull("storageexplorer").getProperty("refreshstatistics"))) {
                DataSet lastNodeDS;
                if (topLevelSet.size() > 1000) {
                    String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", topLevelStorageUnits, null, null);
                    lastNodeDS = this.connectionInfo.isOracle() ? this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where lastnodeflag = 'Y' connect by prior storageunitid = parentid start with storageunitid in (SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ?)", (Object[])new String[]{rsetid}) : this.getQueryProcessor().getPreparedSqlDataSet("WITH StorageUnitTree (storageunitid) AS (   SELECT su.storageunitid   FROM storageunit AS su   WHERE su.storageunitid in (SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ?)   UNION ALL   SELECT su.storageunitid   FROM storageunit AS su   INNER JOIN StorageUnitTree AS d   ON su.parentid = d.storageunitid   ) select s.storageunitid from storageunit s where s.lastnodeflag = 'Y' and s.storageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)", (Object[])new String[]{rsetid});
                } else {
                    SafeSQL safeSQL = new SafeSQL();
                    lastNodeDS = this.connectionInfo.isOracle() ? this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where lastnodeflag = 'Y' connect by prior storageunitid = parentid start with storageunitid in (" + safeSQL.addIn(topLevelSet) + ")", safeSQL.getValues()) : this.getQueryProcessor().getPreparedSqlDataSet("WITH StorageUnitTree (storageunitid) AS (   SELECT su.storageunitid   FROM storageunit AS su   WHERE su.storageunitid in (" + safeSQL.addIn(topLevelSet) + ")   UNION ALL   SELECT su.storageunitid   FROM storageunit AS su   INNER JOIN StorageUnitTree AS d   ON su.parentid = d.storageunitid   ) select s.storageunitid from storageunit s where s.lastnodeflag = 'Y' and s.storageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)", safeSQL.getValues());
                }
                if (lastNodeDS != null && lastNodeDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("storageunitid", lastNodeDS.getColumnValues("storageunitid", ";"));
                    props.setProperty("__populatestorageunitstats", "Y");
                    this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
                }
            }
            if (topLevelSet.size() > 5) {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", topLevelStorageUnits, null, null);
                if (this.database.isOracle()) {
                    this.database.executeSQL("call LV_SUS.CollectTopQuery('storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ''" + rsetid + "'')')");
                } else if (this.database.isSqlServer()) {
                    this.database.executeSQL("EXEC LV_SUS_CollectTopQuery 'storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ''" + rsetid + "'')';");
                }
                this.getDAMProcessor().clearRSet(rsetid);
            } else if (this.database.isOracle()) {
                this.database.executePreparedUpdate("call LV_SUS.CollectTopList(?)", new Object[]{topLevelStorageUnits});
            } else if (this.database.isSqlServer()) {
                this.database.executePreparedUpdate("EXEC LV_SUS_CollectTopList ?;", new Object[]{topLevelStorageUnits});
            }
        }
    }
}

