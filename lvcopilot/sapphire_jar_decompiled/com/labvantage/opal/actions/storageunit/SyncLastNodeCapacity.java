/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SyncLastNodeCapacity
extends BaseAction
implements sapphire.action.SyncLastNodeCapacity {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 61191 $";
    Map<String, Integer> maxCapacityCache = new HashMap<String, Integer>();

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String storageunitid = actionProps.getProperty("storageunitid");
        boolean fromPopulateStorageUnitStats = "Y".equals(actionProps.getProperty("__populatestorageunitstats"));
        HashMap<String, String> filter = new HashMap<String, String>();
        ArrayList ds = null;
        String sql = "select su.storageunitid, su.storageunittype, su.lastnodecapacity, su.propertytreeid, su.maxtiallowed sumaxtiallowed, childsu.storageunitid childstorageunitid, childsu.storageunittype childstorageunittype, childsu.storageunitsize, childsu.maxtiallowed, (select SUM(gc.maxtiallowed) from storageunit gc where gc.parentid in ( childsu.storageunitid ) ) childmaxtiallowed";
        if (!fromPopulateStorageUnitStats) {
            sql = this.connectionInfo.isOracle() ? sql + " ,LV_SUS.FindTopLevel(su.storageunitid) toplevelid" : sql + " ,dbo.LV_SUS_FindTopLevel(su.storageunitid) toplevelid";
        }
        sql = sql + " from STORAGEUNIT su left outer join storageunit childsu on childsu.parentid = su.storageunitid where su.lastnodeflag = 'Y'";
        if (OpalUtil.isNotEmpty(storageunitid)) {
            SafeSQL safeSQL = new SafeSQL();
            if (StringUtil.split(storageunitid, ";").length > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", storageunitid, null, null);
                sql = sql + " and su.storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                sql = sql + " and su.storageunitid in (" + safeSQL.addIn(storageunitid, ";") + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
        }
        if (ds != null) {
            DataSet data = new DataSet();
            HashSet<String> topLevelSet = new HashSet<String>();
            for (int i = 0; i < ds.size(); ++i) {
                String childstorageunitid;
                int lastnodecapacity = ((DataSet)ds).getInt(i, "lastnodecapacity", -1);
                if (!fromPopulateStorageUnitStats) {
                    topLevelSet.add(((DataSet)ds).getString(i, "toplevelid", ""));
                }
                if (OpalUtil.isNotEmpty(childstorageunitid = ((DataSet)ds).getString(i, "childstorageunitid", ""))) {
                    int capacity;
                    int maxtiallowed = ((DataSet)ds).getInt(i, "maxtiallowed", 0);
                    int childmaxtiallowed = ((DataSet)ds).getInt(i, "childmaxtiallowed", 0);
                    int n = capacity = maxtiallowed == 0 ? childmaxtiallowed : maxtiallowed;
                    if (capacity == 0 || capacity == lastnodecapacity) continue;
                    storageunitid = ((DataSet)ds).getString(i, "storageunitid");
                    filter.put("storageunitid", storageunitid);
                    int lastrow = data.findRow(filter);
                    if (lastrow == -1) {
                        int row = data.addRow();
                        data.setString(row, "storageunitid", ((DataSet)ds).getString(i, "storageunitid"));
                        data.setString(row, "lastnodecapacity", String.valueOf(capacity));
                        continue;
                    }
                    try {
                        int lastcapacity = Integer.parseInt(data.getString(lastrow, "lastnodecapacity"));
                        data.setString(lastrow, "lastnodecapacity", String.valueOf(capacity + lastcapacity));
                    }
                    catch (NumberFormatException numberFormatException) {}
                    continue;
                }
                String storageunittype = ((DataSet)ds).getString(i, "storageunittype", "");
                int maxCapacity = this.getMaxCapacity(storageunittype);
                if (maxCapacity == 0) {
                    maxCapacity = ((DataSet)ds).getInt(i, "sumaxtiallowed", 0);
                }
                int row = data.addRow();
                data.setString(row, "storageunitid", ((DataSet)ds).getString(i, "storageunitid"));
                data.setString(row, "lastnodecapacity", String.valueOf(maxCapacity));
            }
            if (data.size() > 0) {
                DataSetUtil.update(this.database, data, "storageunit", new String[]{"storageunitid"});
                if (!fromPopulateStorageUnitStats && !"Y".equals(actionProps.getProperty("__postAddUpdate"))) {
                    boolean resetLastNodeFlag = "Y".equals(actionProps.getProperty("__resetlastnode"));
                    for (int i = 0; i < data.size(); ++i) {
                        String id = data.getString(i, "storageunitid");
                        if (!OpalUtil.isNotEmpty(id) || resetLastNodeFlag) continue;
                        if (this.connectionInfo.isOracle()) {
                            this.getQueryProcessor().execSQL("call LV_SUS.RefreshBranch('" + id + "', LV_SUS.FindTopLevel('" + id + "'))");
                            continue;
                        }
                        this.getQueryProcessor().execSQL("DECLARE @topsu NVARCHAR(40); SET @topsu = dbo.LV_SUS_FindTopLevel ('" + id + "'); EXEC LV_SUS_RefreshBranch '" + id + "',@topsu;");
                    }
                    if (topLevelSet.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("actionid", "PopulateStorageUnitStats");
                        props.setProperty("actionversionid", "1");
                        props.setProperty("storageunitid", OpalUtil.toDelimitedString(topLevelSet, ";"));
                        this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props);
                    }
                }
            }
        }
    }

    private int getMaxCapacity(String storageunittype) {
        if (!this.maxCapacityCache.containsKey(storageunittype)) {
            PropertyList props = this.getStorageUnitTypeDefinition(storageunittype);
            int maxtiallowed = 0;
            try {
                maxtiallowed = Integer.parseInt(props.getProperty("maxtiallowed", "0"));
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
            ArrayList<String> childList = new ArrayList<String>();
            PropertyListCollection childCollection = props.getCollectionNotNull("childrentypes");
            for (int i = 0; i < childCollection.size(); ++i) {
                childList.add(childCollection.getPropertyList(i).getProperty("type"));
            }
            int childmaxcapacity = 0;
            if (childList.size() > 0) {
                for (String chidStorageUnitType : childList) {
                    int maxcapacity;
                    PropertyList list = this.getStorageUnitTypeDefinition(chidStorageUnitType);
                    int childmaxtiallowed = 0;
                    try {
                        childmaxtiallowed = Integer.parseInt(list.getProperty("maxtiallowed", "0"));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    int childsotrageunitsize = 0;
                    try {
                        childsotrageunitsize = Integer.parseInt(list.getProperty("size", "0"));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    if ((maxcapacity = Math.max(childmaxtiallowed, childsotrageunitsize)) <= childmaxcapacity) continue;
                    childmaxcapacity = maxcapacity;
                }
                this.maxCapacityCache.put(storageunittype, Math.max(childmaxcapacity, maxtiallowed));
            } else {
                this.maxCapacityCache.put(storageunittype, maxtiallowed);
            }
        }
        return this.maxCapacityCache.containsKey(storageunittype) ? this.maxCapacityCache.get(storageunittype) : 0;
    }

    private PropertyList getStorageUnitTypeDefinition(String storageunittype) {
        return StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
    }
}

