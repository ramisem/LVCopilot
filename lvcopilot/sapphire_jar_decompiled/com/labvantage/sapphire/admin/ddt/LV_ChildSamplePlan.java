/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.cmt.SDISnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ChildSamplePlan
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 74765 $";
    public static final String SDCID = "LV_ChildSamplePlan";

    @Override
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) {
        SDISnapshot sdiSnapshot = (SDISnapshot)actionProps.get("sdisnapshot");
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if ("Y".equals(primary.getString(i, "embeddedflag"))) {
                String oldchildsampleplanid = primary.getString(i, "s_childsampleplanid");
                String newchildsampleplanid = sdiSnapshot.containsKey(oldchildsampleplanid) ? sdiSnapshot.getKeyValue(oldchildsampleplanid) : (oldchildsampleplanid.startsWith("ECP-") ? "ECP-" + this.getSequenceProcessor().getSequence(SDCID, "ECP") : "PCP-" + this.getSequenceProcessor().getSequence(SDCID, "PrivateChildPlan"));
                primary.setString(i, "s_childsampleplanid", newchildsampleplanid);
                sdiSnapshot.setKeyValue(oldchildsampleplanid, newchildsampleplanid);
                continue;
            }
            String childsampleplanid = primary.getString(i, "s_childsampleplanid");
            sdiSnapshot.setKeyValue(childsampleplanid, childsampleplanid);
        }
        DataSet childsampleplanitem = sdiData.getDataset("s_childsampleplanitem");
        if (childsampleplanitem != null) {
            DataSet childsampleplanworkitem;
            String oldchildsampleplanitemid;
            String oldchildsampleplanid;
            String year = new SimpleDateFormat("yyyy").format(new Date());
            HashMap<String, String> keyMap = new HashMap<String, String>();
            for (int i = 0; i < childsampleplanitem.size(); ++i) {
                String parentItemId = (String)keyMap.get(childsampleplanitem.getString(i, "parentitemid"));
                if (parentItemId != null) {
                    childsampleplanitem.setValue(i, "parentitemid", parentItemId);
                }
                if (sdiSnapshot.containsKey(oldchildsampleplanid = childsampleplanitem.getString(i, "s_childsampleplanid"))) {
                    childsampleplanitem.setString(i, "s_childsampleplanid", sdiSnapshot.getKeyValue(oldchildsampleplanid));
                }
                oldchildsampleplanitemid = childsampleplanitem.getString(i, "s_childsampleplanitemid");
                String newchildsampleplanitemid = "CSPI-" + year + "-" + StringUtil.padLeft(String.valueOf(this.getSequenceProcessor().getSequence("LV_ChildSamplePlanItem", year)), 6, '0');
                childsampleplanitem.setString(i, "s_childsampleplanitemid", newchildsampleplanitemid);
                keyMap.put(oldchildsampleplanitemid, newchildsampleplanitemid);
                sdiSnapshot.setKeyValue(oldchildsampleplanitemid, newchildsampleplanitemid);
            }
            if (childsampleplanitem.size() > 0 && (childsampleplanworkitem = sdiData.getDataset("s_childsampleplanworkitem")) != null) {
                for (int i = 0; i < childsampleplanworkitem.size(); ++i) {
                    oldchildsampleplanid = childsampleplanworkitem.getString(i, "s_childsampleplanid");
                    oldchildsampleplanitemid = childsampleplanworkitem.getString(i, "s_childsampleplanitemid");
                    childsampleplanworkitem.setString(i, "s_childsampleplanid", sdiSnapshot.getKeyValue(oldchildsampleplanid));
                    childsampleplanworkitem.setString(i, "s_childsampleplanversionid", "1");
                    childsampleplanworkitem.setString(i, "s_childsampleplanitemid", sdiSnapshot.getKeyValue(oldchildsampleplanitemid));
                    String privatechildsampleplanid = childsampleplanworkitem.getString(i, "embedchildsampleplanid", "");
                    String privatechildsampleplanversionid = childsampleplanworkitem.getString(i, "embedchildsampleplanversionid", "");
                    if (privatechildsampleplanid.length() <= 0 || privatechildsampleplanversionid.length() <= 0) continue;
                    String newprivatechildsampleplanid = "PCP-" + this.getSequenceProcessor().getSequence(SDCID, "PrivateChildPlan");
                    childsampleplanworkitem.setString(i, "embedchildsampleplanid", newprivatechildsampleplanid);
                    sdiSnapshot.setKeyValue(privatechildsampleplanid, newprivatechildsampleplanid);
                }
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (this.isCMTImport()) {
            return;
        }
        String templatekeyid1 = actionProps.getProperty("templatekeyid1", "");
        String templatekeyid2 = actionProps.getProperty("templatekeyid2", "");
        if (OpalUtil.isNotEmpty(templatekeyid1) && OpalUtil.isNotEmpty(templatekeyid2)) {
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                DataSet wids;
                String childsampleplanid = primary.getString(i, "s_childsampleplanid");
                String childsampleplanversionid = primary.getString(i, "s_childsampleplanversionid");
                if (childsampleplanid.equals(templatekeyid1)) continue;
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_childsampleplanitemid, parentitemid from s_childsampleplanitem where s_childsampleplanid = ? and s_childsampleplanversionid = ?", (Object[])new String[]{childsampleplanid, childsampleplanversionid});
                if (ds != null && ds.size() > 0) {
                    String newchildsampleplanitemid;
                    String childsampleplanitemid;
                    int row;
                    HashMap<String, String> planitemMap = new HashMap<String, String>();
                    String year = new SimpleDateFormat("yyyy").format(new Date());
                    for (row = 0; row < ds.size(); ++row) {
                        childsampleplanitemid = ds.getString(row, "s_childsampleplanitemid");
                        newchildsampleplanitemid = "CSPI-" + year + "-" + StringUtil.padLeft(String.valueOf(this.getSequenceProcessor().getSequence("LV_ChildSamplePlanItem", year)), 6, '0');
                        planitemMap.put(childsampleplanitemid, newchildsampleplanitemid);
                    }
                    for (row = 0; row < ds.size(); ++row) {
                        childsampleplanitemid = ds.getString(row, "s_childsampleplanitemid");
                        newchildsampleplanitemid = (String)planitemMap.get(childsampleplanitemid);
                        String parentitemid = ds.getString(row, "parentitemid", "");
                        if (OpalUtil.isNotEmpty(parentitemid)) {
                            String newparentitemid = (String)planitemMap.get(parentitemid);
                            this.database.executePreparedUpdate("update s_childsampleplanitem set s_childsampleplanitemid = ?, parentitemid = ? where s_childsampleplanid = ? and s_childsampleplanversionid = '1' and s_childsampleplanitemid = ?", new String[]{newchildsampleplanitemid, newparentitemid, childsampleplanid, childsampleplanitemid});
                        } else {
                            this.database.executePreparedUpdate("update s_childsampleplanitem set s_childsampleplanitemid = ? where s_childsampleplanid = ? and s_childsampleplanversionid = '1' and s_childsampleplanitemid = ?", new String[]{newchildsampleplanitemid, childsampleplanid, childsampleplanitemid});
                        }
                        this.database.executePreparedUpdate("update s_childsampleplanworkitem set s_childsampleplanitemid = ? where s_childsampleplanid = ? and s_childsampleplanversionid = '1' and s_childsampleplanitemid = ?", new String[]{newchildsampleplanitemid, childsampleplanid, childsampleplanitemid});
                    }
                }
                if ((wids = this.getQueryProcessor().getPreparedSqlDataSet("select s_childsampleplanid, s_childsampleplanitemid, workitemid, workiteminstance, embedchildsampleplanid, embedchildsampleplanversionid from s_childsampleplanworkitem where S_CHILDSAMPLEPLANID = ? and S_CHILDSAMPLEPLANVERSIONID = ? and EMBEDCHILDSAMPLEPLANID is not null order by S_CHILDSAMPLEPLANID, s_childsampleplanitemid", (Object[])new String[]{childsampleplanid, childsampleplanversionid})) == null) continue;
                for (int j = 0; j < wids.size(); ++j) {
                    String embedchildsampleplanid = wids.getString(j, "embedchildsampleplanid");
                    String embedchildsampleplanversionid = wids.getString(j, "embedchildsampleplanversionid");
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", SDCID);
                    props.setProperty("keyid1", "PCP-" + this.getSequenceProcessor().getSequence(SDCID, "PrivateChildPlan"));
                    props.setProperty("keyid2", "1");
                    props.setProperty("templatekeyid1", embedchildsampleplanid);
                    props.setProperty("templatekeyid2", embedchildsampleplanversionid);
                    props.setProperty("embeddedflag", "Y");
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    String privatechildsampleplanid = props.getProperty("newkeyid1");
                    String privatechildsampleplanversionid = props.getProperty("newkeyid2");
                    this.database.executePreparedUpdate("update s_childsampleplanworkitem set embedchildsampleplanid = ?, embedchildsampleplanversionid = ? where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ? and workitemid = ? and workiteminstance = ?", new String[]{privatechildsampleplanid, privatechildsampleplanversionid, childsampleplanid, childsampleplanversionid, wids.getString(j, "s_childsampleplanitemid"), wids.getString(j, "workitemid"), wids.getValue(j, "workiteminstance")});
                }
            }
        }
    }
}

