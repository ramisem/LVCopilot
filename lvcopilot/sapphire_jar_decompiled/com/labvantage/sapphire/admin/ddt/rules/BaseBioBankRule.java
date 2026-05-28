/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseBioBankRule
extends BaseRule {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private HashMap parentSDIBySDCMap;
    private HashMap custodialDepartmentMap;
    private HashMap glpMap;
    private HashMap sdiPropertyMap;
    private HashMap sdiTrackItemMap;
    private HashMap sdiStorageUnitMap;
    private HashMap parentStorageUnitMap;
    private HashMap studyActiveRCMap;
    public static final String ACTIVE = "Active";
    public static final String NOTACTIVE = "Not Active";
    public static final String POLICY_STUDYHASPROTOCOLRULE = "Study Has Protocol Rule";
    public static final String POLICY_GLPRULE = "GLP Rule";
    public static final String POLICY_AMBIGUOUSSUBJECTRULE = "Ambiguous Subject Rule";
    public static final String POLICY_ATIVERCRULE = "Active RC Rule";
    public static final String POLICY_DISPOSERULE = "Dispose Rule";

    public BaseBioBankRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    protected boolean isRuleActive() throws SapphireException {
        return BaseBioBankRule.isRuleActive(this.getRuleId(), this.getConfigurationProcessor());
    }

    public abstract String getRuleId();

    public static boolean isRuleActive(String ruleid, ConfigurationProcessor configurationProcessor) throws SapphireException {
        boolean active = true;
        PropertyList policy = configurationProcessor.getPolicy("BioBankingPolicy", "Sapphire Custom");
        if (policy != null) {
            active = ACTIVE.equals(policy.getPropertyListNotNull("rules").getProperty(ruleid, ACTIVE));
        }
        return active;
    }

    protected String getParentSDIBySDC(String storageunitid, String sdcid) {
        if (this.parentSDIBySDCMap == null) {
            this.parentSDIBySDCMap = new HashMap();
        }
        if (!this.parentSDIBySDCMap.containsKey(storageunitid)) {
            this.parentSDIBySDCMap.put(storageunitid, StorageUnitSDC.getParentSDIBySDC(this.getQueryProcessor(), storageunitid, sdcid));
        }
        return (String)this.parentSDIBySDCMap.get(storageunitid);
    }

    protected String getCustodialDepartmentId(String sdcid, String keyid1) {
        HashMap map;
        if (this.custodialDepartmentMap == null) {
            this.custodialDepartmentMap = new HashMap();
        }
        if (!this.custodialDepartmentMap.containsKey(sdcid)) {
            this.custodialDepartmentMap.put(sdcid, new HashMap());
        }
        if (!(map = (HashMap)this.custodialDepartmentMap.get(sdcid)).containsKey(keyid1)) {
            if ("PhysicalStore".equals(sdcid)) {
                map.put(keyid1, PhysicalStore.getCustodialDepartmentId(this.getQueryProcessor(), keyid1));
            } else {
                map.put(keyid1, OpalUtil.getColumnValue(this.getQueryProcessor(), "trackitem", "custodialdepartmentid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, keyid1}));
            }
        }
        return (String)map.get(keyid1);
    }

    protected boolean isGLP(String sdcid, String keyid1) throws SapphireException {
        HashMap map;
        if (this.glpMap == null) {
            this.glpMap = new HashMap();
        }
        if (!this.glpMap.containsKey(sdcid)) {
            this.glpMap.put(sdcid, new HashMap());
        }
        if (!(map = (HashMap)this.glpMap.get(sdcid)).containsKey(keyid1)) {
            if ("PhysicalStore".equals(sdcid)) {
                map.put(keyid1, PhysicalStore.isGLP(this.database, keyid1) ? "Y" : "N");
            } else if ("Department".equals(sdcid)) {
                map.put(keyid1, Department.isGLP(this.database, keyid1) ? "Y" : "N");
            }
        }
        return "Y".equals(map.get(keyid1));
    }

    protected boolean isPhysicalStoreTemporaryStorage(String physicalstoreid) {
        return "Temporary".equals(this.getColumnValue("PhysicalStore", physicalstoreid, "storageclass"));
    }

    protected boolean isDepartmentRepository(String departmentid) {
        return "Y".equals(this.getColumnValue("Department", departmentid, "repositoryflag", "N"));
    }

    protected String getColumnValue(String sdcid, String keyid1, String columnid) {
        return this.getColumnValue(sdcid, keyid1, columnid, "");
    }

    protected String getColumnValue(String sdcid, String keyid1, String columnid, String defaultvalue) {
        String value = this.getSDIProperty(sdcid, keyid1).getProperty(columnid);
        if (StringUtil.getLen(value) == 0L) {
            value = defaultvalue;
        }
        return value;
    }

    protected PropertyList getSDIProperty(String sdcid, String keyid1) {
        return this.getSDIProperty(sdcid, keyid1, null, null);
    }

    protected PropertyList getSDIProperty(String sdcid, String keyid1, String keyid2, String keyid3) {
        HashMap sdcmap;
        if (this.sdiPropertyMap == null) {
            this.sdiPropertyMap = new HashMap();
        }
        if (!this.sdiPropertyMap.containsKey(sdcid)) {
            this.sdiPropertyMap.put(sdcid, new HashMap());
        }
        if (!(sdcmap = (HashMap)this.sdiPropertyMap.get(sdcid)).containsKey(keyid1)) {
            PropertyList list = new PropertyList();
            HashMap map = this.getSDCProcessor().getSDCProperties(sdcid);
            if (map != null) {
                DataSet ds;
                String tableid = (String)map.get("tableid");
                String keycolid1 = (String)map.get("keycolid1");
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select * from ").append(tableid);
                sql.append(" where ").append(keycolid1).append(" = ").append(safeSQL.addVar(keyid1));
                if (StringUtil.getLen(keyid2) > 0L) {
                    sql.append(" and ").append(map.get("keycolid2")).append(" = ").append(safeSQL.addVar(keyid2));
                }
                if (StringUtil.getLen(keyid3) > 0L) {
                    sql.append(" and ").append(map.get("keycolid3")).append(" = ").append(safeSQL.addVar(keyid3));
                }
                if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 0) {
                    for (int i = 0; i < ds.getColumnCount(); ++i) {
                        String column = ds.getColumnId(i);
                        list.setProperty(column, ds.getValue(0, column, ""));
                    }
                }
            }
            sdcmap.put(keyid1, list);
        }
        return (PropertyList)sdcmap.get(keyid1);
    }

    protected String getSDITrackItemID(String sdcid, String keyid1) {
        PropertyList list;
        if (this.sdiTrackItemMap == null) {
            this.sdiTrackItemMap = new HashMap();
        }
        if (!this.sdiTrackItemMap.containsKey(sdcid)) {
            this.sdiTrackItemMap.put(sdcid, new PropertyList());
        }
        if (!(list = (PropertyList)this.sdiTrackItemMap.get(sdcid)).containsKey(keyid1)) {
            list.setProperty(keyid1, OpalUtil.getColumnValue(this.getQueryProcessor(), "trackitem", "trackitemid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, keyid1}));
        }
        return list.getProperty(keyid1);
    }

    protected String getSDIStorageUnitID(String sdcid, String keyid1) {
        PropertyList list;
        if (this.sdiStorageUnitMap == null) {
            this.sdiStorageUnitMap = new HashMap();
        }
        if (!this.sdiStorageUnitMap.containsKey(sdcid)) {
            this.sdiStorageUnitMap.put(sdcid, new PropertyList());
        }
        if (!(list = (PropertyList)this.sdiStorageUnitMap.get(sdcid)).containsKey(keyid1)) {
            list.setProperty(keyid1, OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, keyid1}));
        }
        return list.getProperty(keyid1);
    }

    protected String getDefaultDepartment(String userid) {
        return this.getColumnValue("User", userid, "defaultdepartment", "");
    }

    protected String getDefaultDepartment() {
        return this.getDefaultDepartment(this.connectionInfo.getSysuserId());
    }

    protected String getParentStorageUnitBySDC(String storageunitid, String sdcid) {
        PropertyList list;
        if (this.parentStorageUnitMap == null) {
            this.parentStorageUnitMap = new HashMap();
        }
        if (!this.parentStorageUnitMap.containsKey(storageunitid)) {
            this.parentStorageUnitMap.put(storageunitid, new PropertyList());
        }
        if (!(list = (PropertyList)this.parentStorageUnitMap.get(storageunitid)).containsKey(sdcid)) {
            list.setProperty(sdcid, StorageUnitSDC.getParentSDIBySDC(this.getQueryProcessor(), storageunitid, sdcid));
        }
        return list.getProperty(sdcid);
    }

    protected boolean studyHasActiveRC(String studyid) throws SapphireException {
        if (this.studyActiveRCMap == null) {
            this.studyActiveRCMap = new HashMap();
        }
        if (!this.studyActiveRCMap.containsKey(studyid)) {
            this.studyActiveRCMap.put(studyid, Study.hasActiveRC(this.database, studyid) ? "Y" : "N");
        }
        return "Y".equals(this.studyActiveRCMap.get(studyid));
    }
}

