/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddPrivatePrivateSamplePlanAction
extends BaseAction {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String sdiworkitemid = actionProps.getProperty("sdiworkitemid", "");
        String childsampleplanitemid = actionProps.getProperty("childsampleplanitemid", "");
        String eventdefid = actionProps.getProperty("eventdefid", "");
        String specimendefid = actionProps.getProperty("specimendefid");
        String sampletypeid = actionProps.getProperty("sampletypeid");
        String workitemid = actionProps.getProperty("workitemid");
        String workitemversionid = actionProps.getProperty("workitemversionid");
        String workiteminstance = actionProps.getProperty("workiteminstance");
        String childsampleplanid = "";
        String childsampleplanversionid = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        if (sdiworkitemid.length() > 0) {
            sql.append("select embedchildsampleplanid, embedchildsampleplanversionid");
            sql.append(" from sdiworkitem");
            sql.append(" where sdiworkitemid = ").append(safeSQL.addVar(sdiworkitemid)).append(" and embedchildsampleplanid is not null");
        }
        if (childsampleplanitemid.length() > 0) {
            sql.append("SELECT embedchildsampleplanid, embedchildsampleplanversionid");
            sql.append(" FROM s_childsampleplanworkitem");
            sql.append(" WHERE s_childsampleplanitemid = ").append(safeSQL.addVar(childsampleplanitemid));
            sql.append(" AND workitemid = ").append(safeSQL.addVar(workitemid));
            sql.append(" AND workiteminstance = ").append(safeSQL.addVar(workiteminstance));
            sql.append(" AND embedchildsampleplanid is not null");
        } else if (eventdefid.length() > 0) {
            sql.append("SELECT embedchildsampleplanid, embedchildsampleplanversionid");
            sql.append(" FROM s_eventdefstspecimendefwi");
            sql.append(" WHERE s_eventdefid = ").append(safeSQL.addVar(eventdefid));
            sql.append(" AND s_sampletypeid = ").append(safeSQL.addVar(sampletypeid));
            sql.append(" AND s_specimendefid = ").append(safeSQL.addVar(specimendefid));
            sql.append(" AND workitemid = ").append(safeSQL.addVar(workitemid));
            sql.append(" AND workiteminstance = ").append(safeSQL.addVar(workiteminstance));
            sql.append(" AND embedchildsampleplanid is not null");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            actionProps.setProperty("childsampleplanid", ds.getString(0, "embedchildsampleplanid"));
            actionProps.setProperty("childsampleplanversionid", ds.getString(0, "embedchildsampleplanversionid"));
        } else {
            String sdiworkitemsampletypeid = "";
            sql.setLength(0);
            if (sdiworkitemid.length() > 0 && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select workitemid, workitemversionid, s_sampletypeid from sdiworkitem where sdiworkitemid = ?", (Object[])new String[]{sdiworkitemid})) != null && ds.size() > 0) {
                DataSet _ds;
                workitemid = ds.getString(0, "workitemid");
                workitemversionid = ds.getString(0, "workitemversionid");
                sdiworkitemsampletypeid = ds.getString(0, "s_sampletypeid", "");
                if (OpalUtil.isEmpty(workitemversionid) && (_ds = this.getQueryProcessor().getPreparedSqlDataSet("select workitemid, workitemversionid, versionstatus from workitem where workitemid = ? and versionstatus in ( 'P', 'C' ) order by createdt desc", (Object[])new String[]{workitemid})) != null && _ds.size() > 0) {
                    workitemversionid = _ds.getString(0, "workitemversionid");
                    for (int i = 0; i < _ds.size(); ++i) {
                        if (!"C".equals(_ds.getString(i, "versionstatus"))) continue;
                        workitemversionid = _ds.getString(i, "workitemversionid");
                        break;
                    }
                }
            }
            safeSQL.reset();
            sql.append("select embedchildsampleplanid, embedchildsampleplanversionid");
            sql.append(" from workitem");
            sql.append(" where workitemid = ").append(safeSQL.addVar(workitemid));
            sql.append(" and workitemversionid = ").append(safeSQL.addVar(workitemversionid));
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                String embedchildsampleplanid = ds.getString(0, "embedchildsampleplanid", "");
                String embedchildsampleplanversionid = ds.getString(0, "embedchildsampleplanversionid", "");
                if (embedchildsampleplanid.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_ChildSamplePlan");
                    props.setProperty("keyid1", "PCP-" + this.getSequenceProcessor().getSequence("LV_ChildSamplePlan", "PrivateChildPlan"));
                    props.setProperty("keyid2", "1");
                    props.setProperty("templatekeyid1", embedchildsampleplanid);
                    props.setProperty("templatekeyid2", embedchildsampleplanversionid);
                    props.setProperty("embeddedflag", "Y");
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    childsampleplanid = props.getProperty("newkeyid1");
                    childsampleplanversionid = props.getProperty("newkeyid2");
                    if (sdiworkitemid.length() > 0) {
                        String pcpsampletypeid;
                        ds = new DataSet();
                        ds.addRow();
                        ds.setString(0, "sdiworkitemid", sdiworkitemid);
                        ds.setString(0, "embedchildsampleplanid", childsampleplanid);
                        ds.setString(0, "embedchildsampleplanversionid", childsampleplanversionid);
                        ds.setString(0, "modby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        ds.setString(0, "modtool", "ChildSamplePlan");
                        ds.setDate(0, "moddt", DateTimeUtil.getNowCalendar());
                        String[] keycols = new String[]{"sdiworkitemid"};
                        DataSetUtil.update(this.database, ds, "sdiworkitem", keycols);
                        if (OpalUtil.isNotEmpty(sdiworkitemsampletypeid) && OpalUtil.isEmpty(pcpsampletypeid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_childsampleplan", "sampletypeid", "s_childsampleplanid = ? and s_childsampleplanversionid = ?", new String[]{childsampleplanid, childsampleplanversionid}))) {
                            props.clear();
                            props.setProperty("sdcid", "LV_ChildSamplePlan");
                            props.setProperty("keyid1", childsampleplanid);
                            props.setProperty("keyid2", childsampleplanversionid);
                            props.setProperty("sampletypeid", sdiworkitemsampletypeid);
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        }
                    } else if (childsampleplanitemid.length() > 0) {
                        ds = new DataSet();
                        ds.addRow();
                        ds.setString(0, "s_childsampleplanid", actionProps.getProperty("childsampleplanid"));
                        ds.setString(0, "s_childsampleplanversionid", actionProps.getProperty("childsampleplanversionid"));
                        ds.setString(0, "s_childsampleplanitemid", childsampleplanitemid);
                        ds.setString(0, "workitemid", workitemid);
                        ds.setNumber(0, "workiteminstance", workiteminstance);
                        ds.setString(0, "embedchildsampleplanid", childsampleplanid);
                        ds.setString(0, "embedchildsampleplanversionid", childsampleplanversionid);
                        ds.setString(0, "modby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        ds.setString(0, "modtool", "ChildSamplePlan");
                        ds.setDate(0, "moddt", DateTimeUtil.getNowCalendar());
                        String[] keycols = new String[]{"s_childsampleplanid", "s_childsampleplanversionid", "s_childsampleplanitemid", "workitemid", "workiteminstance"};
                        DataSetUtil.update(this.database, ds, "s_childsampleplanworkitem", keycols);
                    } else if (eventdefid.length() > 0) {
                        ds = new DataSet();
                        ds.addRow();
                        ds.setString(0, "s_eventdefid", eventdefid);
                        ds.setString(0, "s_sampletypeid", sampletypeid);
                        ds.setString(0, "s_specimendefid", specimendefid);
                        ds.setString(0, "workitemid", workitemid);
                        ds.setNumber(0, "workiteminstance", workiteminstance);
                        ds.setString(0, "embedchildsampleplanid", childsampleplanid);
                        ds.setString(0, "embedchildsampleplanversionid", childsampleplanversionid);
                        ds.setString(0, "modby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        ds.setString(0, "modtool", "ChildSamplePlan");
                        ds.setDate(0, "moddt", DateTimeUtil.getNowCalendar());
                        String[] keycols = new String[]{"s_eventdefid", "s_sampletypeid", "s_specimendefid", "workitemid", "workiteminstance"};
                        DataSetUtil.update(this.database, ds, "s_eventdefstspecimendefwi", keycols);
                    }
                    actionProps.setProperty("childsampleplanid", childsampleplanid);
                    actionProps.setProperty("childsampleplanversionid", childsampleplanversionid);
                } else {
                    this.setError(this.getTranslationProcessor().translate("Add Specimen Private Sample Plan"), "VALIDATION", this.getTranslationProcessor().translate("No Embedded Child Sample Plan found in WorkItem") + " " + workitemid + " (v" + workitemversionid + ")");
                }
            }
        }
    }
}

