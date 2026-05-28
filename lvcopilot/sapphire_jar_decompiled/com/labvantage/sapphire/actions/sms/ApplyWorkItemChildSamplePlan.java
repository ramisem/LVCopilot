/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sms.ApplyChildSamplePlan;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApplyWorkItemChildSamplePlan
extends BaseAction
implements sapphire.action.ApplyWorkItemChildSamplePlan {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String sdiworkitemid = actionProps.getProperty("sdiworkitemid");
        String auditreason = actionProps.getProperty("auditreason", "");
        if (OpalUtil.isEmpty(sdiworkitemid)) {
            String sdcid = actionProps.getProperty("sdcid");
            String keyid1 = actionProps.getProperty("keyid1");
            String keyid2 = actionProps.getProperty("keyid2");
            String keyid3 = actionProps.getProperty("keyid3");
            String workitemid = actionProps.getProperty("workitemid");
            String workiteminstance = actionProps.getProperty("workiteminstance");
            if (OpalUtil.isNotEmpty(sdcid) && OpalUtil.isNotEmpty(keyid1) && OpalUtil.isNotEmpty(workitemid)) {
                ArrayList<String> list = new ArrayList<String>();
                DataSet ds = new DataSet();
                ds.addColumnValues("keyid1", 0, keyid1, ";");
                if (OpalUtil.isNotEmpty(keyid2)) {
                    ds.addColumnValues("keyid2", 0, keyid2, ";");
                    if (OpalUtil.isNotEmpty(keyid3)) {
                        ds.addColumnValues("keyid3", 0, keyid3, ";");
                    }
                }
                ds.addColumnValues("workitemid", 0, workitemid, ";");
                ds.addColumnValues("workiteminstance", 0, workiteminstance, ";");
                for (int i = 0; i < ds.size(); ++i) {
                    keyid1 = ds.getString(i, "keyid1", "");
                    keyid2 = ds.getString(i, "keyid2", "");
                    keyid3 = ds.getString(i, "keyid3", "");
                    workitemid = ds.getString(i, "workitemid", "");
                    workiteminstance = ds.getString(i, "workiteminstance", "");
                    if (!OpalUtil.isNotEmpty(keyid1) || !OpalUtil.isNotEmpty(workitemid) || !OpalUtil.isNotEmpty(workiteminstance)) continue;
                    Object[] keys = new String[]{sdcid, keyid1, workitemid, workiteminstance};
                    String sql = "select sdiworkitemid from sdiworkitem where sdcid = ? and keyid1 = ?";
                    if (OpalUtil.isNotEmpty(keyid2)) {
                        sql = sql + " and keyid2 = ?";
                        keys = new String[]{sdcid, keyid1, keyid2, workitemid, workiteminstance};
                        if (OpalUtil.isNotEmpty(keyid3)) {
                            sql = sql + " and keyid3 = ?";
                            keys = new String[]{sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance};
                        }
                    }
                    sql = sql + " and workitemid = ? and workiteminstance = ?";
                    DataSet d = this.getQueryProcessor().getPreparedSqlDataSet(sql, keys);
                    if (d == null || d.size() <= 0) continue;
                    list.add(d.getString(0, "sdiworkitemid"));
                }
                sdiworkitemid = OpalUtil.toDelimitedString(list, ";");
            }
        }
        if (StringUtil.getLen(sdiworkitemid) > 0L) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select sdiworkitem.sdiworkitemid, sdiworkitem.keyid1, workitem.embedchildsampleplanid, workitem.embedchildsampleplanversionid, sdiworkitem.embedchildsampleplanid sdiwichildplanid, sdiworkitem.embedchildsampleplanversionid sdiwichildplanversionid from workitem, sdiworkitem where workitem.workitemid = sdiworkitem.workitemid and workitem.workitemversionid = sdiworkitem.workitemversionid and sdiworkitem.sdiworkitemid in (" + safeSQL.addIn(StringUtil.replaceAll(sdiworkitemid, ";", "','")) + ") and sdiworkitem.sdcid = 'Sample' and sdiworkitem.appliedflag = 'Y' and workitem.supportembeddedchildplanflag = 'Y' and (sdiworkitem.childsampleplanappliedflag is null OR sdiworkitem.childsampleplanappliedflag != 'Y')";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String sdiwichildplanid = ds.getString(i, "sdiwichildplanid", "").trim();
                    String sdiwichildplanversionid = ds.getString(i, "sdiwichildplanversionid", "").trim();
                    if (sdiwichildplanid.length() <= 0) continue;
                    ds.setString(i, "embedchildsampleplanid", sdiwichildplanid);
                    ds.setString(i, "embedchildsampleplanversionid", sdiwichildplanversionid);
                }
                ds.sort("embedchildsampleplanid,embedchildsampleplanversionid");
                ArrayList<DataSet> list = ds.getGroupedDataSets("embedchildsampleplanid,embedchildsampleplanversionid");
                ArrayList<String> childsampleidlist = new ArrayList<String>();
                for (DataSet dataSet : list) {
                    dataSet.sort("keyid1");
                    String childsampleplanid = dataSet.getString(0, "embedchildsampleplanid", "");
                    String childsampleplanversionid = dataSet.getString(0, "embedchildsampleplanversionid", "");
                    if (childsampleplanid.length() <= 0 || childsampleplanversionid.length() <= 0) continue;
                    PropertyList props = new PropertyList();
                    props.setProperty("childsampleplanid", childsampleplanid);
                    props.setProperty("childsampleplanversionid", childsampleplanversionid);
                    props.setProperty("sampleid", dataSet.getColumnValues("keyid1", ";"));
                    props.setProperty("sdiworkitemid", dataSet.getColumnValues("sdiworkitemid", ";"));
                    props.setProperty("auditreason", auditreason);
                    this.getActionProcessor().processActionClass(ApplyChildSamplePlan.class.getName(), props);
                    childsampleidlist.add(StringUtil.replaceAll(props.getProperty("newkeyid1", ""), "#semicolon#", ";"));
                    props.clear();
                    DataSet dsupdate = new DataSet();
                    dsupdate.addColumnValues("sdiworkitemid", 0, dataSet.getColumnValues("sdiworkitemid", ";"), ";");
                    dsupdate.setString(-1, "childsampleplanappliedflag", "Y");
                    if (dsupdate.size() <= 0) continue;
                    DataSetUtil.update(this.database, dsupdate, "sdiworkitem", StringUtil.split("sdiworkitemid", ";"));
                }
                actionProps.setProperty("newkeyid1", OpalUtil.toDelimitedString(childsampleidlist, ";"));
            }
        }
    }
}

