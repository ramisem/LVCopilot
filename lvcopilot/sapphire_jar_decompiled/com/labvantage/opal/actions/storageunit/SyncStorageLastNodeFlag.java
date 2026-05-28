/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import com.labvantage.opal.actions.storageunit.SyncLastNodeCapacity;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SyncStorageLastNodeFlag
extends BaseAction
implements sapphire.action.SyncStorageLastNodeFlag {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 61191 $";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        List<String> lastNodeList = StorageUnitTypeDef.getInstance().getLastNodeList(this.getConnectionProcessor().getSapphireConnection());
        if (lastNodeList.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where storageunittype in (" + safeSQL.addIn(OpalUtil.toDelimitedString(lastNodeList, "','")) + ") and (lastnodeflag is null or lastnodeflag = 'N')", safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                String storageunitid = ds.getColumnValues("storageunitid", ";");
                ds.setString(-1, "lastnodeflag", "Y");
                DataSetUtil.update(this.database, ds, "storageunit", new String[]{"storageunitid"});
                PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                if (!"Manual Only".equals(policy.getPropertyListNotNull("storageexplorer").getProperty("refreshstatistics"))) {
                    PropertyList props = new PropertyList();
                    props.setProperty("storageunitid", storageunitid);
                    props.setProperty("__resetlastnode", actionProps.getProperty("__resetlastnode", "N"));
                    props.setProperty("__postAddUpdate", actionProps.getProperty("__postAddUpdate", "N"));
                    this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
                    if (!"Y".equals(actionProps.getProperty("__resetlastnode", "N"))) {
                        DataSet topds;
                        String toplevelfunction = this.database.isOracle() ? "LV_SUS.FindTopLevel" : "dbo.LV_SUS_FindTopLevel";
                        safeSQL.reset();
                        if (ds.size() > 1000) {
                            String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", storageunitid, null, null);
                            topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + ")", safeSQL.getValues());
                            this.getDAMProcessor().clearRSet(rsetid);
                        } else {
                            topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in (" + safeSQL.addIn(ds.getColumnValues("storageunitid", "','")) + ")", safeSQL.getValues());
                        }
                        if (topds != null) {
                            HashSet<String> topLevelSet = new HashSet<String>();
                            for (int i = 0; i < topds.size(); ++i) {
                                topLevelSet.add(topds.getString(i, "toplevelid"));
                            }
                            if (topLevelSet.size() > 0) {
                                props.clear();
                                props.setProperty("actionid", "PopulateStorageUnitStats");
                                props.setProperty("actionversionid", "1");
                                props.setProperty("storageunitid", OpalUtil.toDelimitedString(topLevelSet, ";"));
                                this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props);
                            }
                        }
                    }
                }
            }
            safeSQL.reset();
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where storageunittype not in (" + safeSQL.addIn(OpalUtil.toDelimitedString(lastNodeList, "','")) + ") and lastnodeflag = 'Y'", safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                ds.setString(-1, "lastnodeflag", "N");
                ds.setString(-1, "lastnodecapacity", "");
                DataSetUtil.update(this.database, ds, "storageunit", new String[]{"storageunitid"});
            }
        }
    }
}

