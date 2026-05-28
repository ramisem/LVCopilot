/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_EventDef
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 73940 $";
    Map<String, String> localCache = new HashMap<String, String>();

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        PropertyList maxUserSequences = new PropertyList();
        ArrayList<DataSet> clinicalProtocolList = primary.getGroupedDataSets("clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision, cohortid");
        StringBuilder whereClause = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        for (int i = 0; i < clinicalProtocolList.size(); ++i) {
            DataSet ds = (DataSet)clinicalProtocolList.get(i);
            if (i != 0) {
                whereClause.append(" OR ");
            }
            whereClause.append(" ( ").append(" clinicalprotocolid = ").append(safeSQL.addVar(ds.getString(0, "clinicalprotocolid"))).append(" AND clinicalprotocolversionid = ").append(safeSQL.addVar(ds.getString(0, "clinicalprotocolversionid"))).append(" AND clinicalprotocolrevision = ").append(safeSQL.addVar(ds.getString(0, "clinicalprotocolrevision"))).append(" AND cohortid = ").append(safeSQL.addVar(ds.getString(0, "cohortid"))).append(" ) ");
        }
        StringBuilder qry = new StringBuilder();
        qry.append("SELECT s_eventdefid, clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision, ").append(" cohortid, parenteventdefid, usersequence").append(" FROM s_eventdef").append(" WHERE ").append((CharSequence)whereClause);
        DataSet eventDetailsDS = this.getQueryProcessor().getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
        for (int i = 0; i < eventDetailsDS.getRowCount(); ++i) {
            if (!eventDetailsDS.isNull(i, "parenteventdefid")) continue;
            eventDetailsDS.setString(i, "parenteventdefid", "");
        }
        String templateid = actionProps.getProperty("templatekeyid1", actionProps.getProperty("templateid", ""));
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String parentEventId = primary.getString(i, "parenteventdefid", "");
            String clinicalProtocolId = primary.getString(i, "clinicalprotocolid", "");
            String clinicalProtocolVersionId = primary.getString(i, "clinicalprotocolversionid", "");
            String clinicalProtocolRevision = primary.getString(i, "clinicalprotocolrevision", "");
            String cohortId = primary.getString(i, "cohortid", "");
            String key = parentEventId + "-" + clinicalProtocolId + "-" + clinicalProtocolVersionId + "-" + clinicalProtocolRevision + "-" + cohortId;
            int maxUserSequence = maxUserSequences.containsKey(key) ? Integer.parseInt(maxUserSequences.getProperty(key, "0")) : this.getMaxUserSequence(eventDetailsDS, parentEventId, clinicalProtocolId, clinicalProtocolVersionId, clinicalProtocolRevision, cohortId);
            maxUserSequences.setProperty(key, String.valueOf(++maxUserSequence));
            primary.setNumber(i, "usersequence", maxUserSequence);
            if (OpalUtil.isNotEmpty(templateid)) {
                primary.setValue(i, "createdt", "n");
                primary.setValue(i, "moddt", "n");
            }
            if (parentEventId.length() <= 0) continue;
            primary.setString(i, "parenteventdeflabel", this.getEventDefLabel(parentEventId));
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String templateid = actionProps.getProperty("templatekeyid1", actionProps.getProperty("templateid", ""));
        if (OpalUtil.isNotEmpty(templateid)) {
            StringBuilder sql = new StringBuilder();
            DataSet specimendefColumns = this.getSDCProcessor().getTableColumnData("s_eventdefstspecimendef");
            DataSet specimendefWIColumns = this.getSDCProcessor().getTableColumnData("s_eventdefstspecimendefwi");
            for (int i = 0; i < primary.size(); ++i) {
                HashMap<String, String> keymap = new HashMap<String, String>();
                String eventdefid = primary.getString(i, "s_eventdefid");
                String parenteventdefid = primary.getString(i, "parenteventdefid", "");
                String sysuserid = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
                DataSet visitds = this.getQueryProcessor().getPreparedSqlDataSet("select s_eventdefid, s_sampletypeid, s_specimendefid from s_eventdefstspecimendef where s_eventdefid = ? order by usersequence", (Object[])new String[]{templateid});
                if (visitds == null || visitds.size() <= 0) continue;
                this.database.executePreparedUpdate("delete from s_eventdefstspecimendefwi where s_eventdefid = ? or s_eventdefid in (select s_eventdef.s_eventdefid from s_eventdef where s_eventdef.parenteventdefid = ?)", new String[]{eventdefid, eventdefid});
                this.database.executePreparedUpdate("delete from s_eventdefstspecimendef where s_eventdefid = ? or s_eventdefid in (select s_eventdef.s_eventdefid from s_eventdef where s_eventdef.parenteventdefid = ?)", new String[]{eventdefid, eventdefid});
                String sequencekey = "SDF-" + new SimpleDateFormat("yyyy").format(new Date()) + "-";
                int sequence = this.getSequenceProcessor().getSequence("LV_EventDef", sequencekey, visitds.size());
                for (int j = 0; j < visitds.size(); ++j) {
                    String templateeventdefid = visitds.getString(j, "s_eventdefid");
                    String templatesampletypeid = visitds.getString(j, "s_sampletypeid");
                    String templatespecimendefid = visitds.getString(j, "s_specimendefid");
                    String specimendefid = sequencekey + sequence++;
                    sql.setLength(0);
                    sql.append("insert into s_eventdefstspecimendef ( ").append(specimendefColumns.getColumnValues("columnid", ", ")).append(" )");
                    sql.append(" ( select ");
                    for (int column = 0; column < specimendefColumns.size(); ++column) {
                        String columnid = specimendefColumns.getString(column, "columnid");
                        if (column > 0) {
                            sql.append(", ");
                        }
                        if ("s_eventdefid".equals(columnid)) {
                            sql.append("'").append(eventdefid).append("'");
                            continue;
                        }
                        if ("s_specimendefid".equals(columnid)) {
                            sql.append("'").append(specimendefid).append("'");
                            continue;
                        }
                        if ("parenteventdefid".equals(columnid)) {
                            sql.append("'").append(parenteventdefid).append("'");
                            continue;
                        }
                        if ("createby".equals(columnid) || "modby".equals(columnid)) {
                            sql.append("'").append(sysuserid).append("'");
                            continue;
                        }
                        if ("createdt".equals(columnid) || "moddt".equals(columnid)) {
                            if (this.database.isOracle()) {
                                sql.append("sysdate");
                                continue;
                            }
                            sql.append("getdate()");
                            continue;
                        }
                        sql.append(columnid);
                    }
                    sql.append(" from s_eventdefstspecimendef where s_eventdefid = ? and s_sampletypeid = ? and s_specimendefid = ?)");
                    this.database.executePreparedUpdate(sql.toString(), new String[]{templateeventdefid, templatesampletypeid, templatespecimendefid});
                    keymap.put(templatespecimendefid, specimendefid);
                }
                PropertyList props = new PropertyList();
                DataSet visitdswi = this.getQueryProcessor().getPreparedSqlDataSet("select s_eventdefid, s_sampletypeid, s_specimendefid, workitemid, workiteminstance, embedchildsampleplanid, embedchildsampleplanversionid from s_eventdefstspecimendefwi where s_eventdefid = ? order by usersequence", (Object[])new String[]{templateid});
                if (visitdswi == null || visitdswi.size() <= 0) continue;
                for (int j = 0; j < visitdswi.size(); ++j) {
                    String templateeventdefid = visitdswi.getString(j, "s_eventdefid");
                    String templatesampletypeid = visitdswi.getString(j, "s_sampletypeid");
                    String templatespecimendefid = visitdswi.getString(j, "s_specimendefid");
                    String templateworkitemid = visitdswi.getString(j, "workitemid");
                    String templateworkiteminstance = visitdswi.getValue(j, "workiteminstance");
                    String embedchildsampleplanid = visitdswi.getValue(j, "embedchildsampleplanid");
                    String embedchildsampleplanversionid = visitdswi.getValue(j, "embedchildsampleplanversionid");
                    String specimendefid = (String)keymap.get(templatespecimendefid);
                    if (!OpalUtil.isNotEmpty(specimendefid)) continue;
                    if (OpalUtil.isNotEmpty(embedchildsampleplanid) && OpalUtil.isNotEmpty(embedchildsampleplanversionid)) {
                        props.clear();
                        props.setProperty("sdcid", "LV_ChildSamplePlan");
                        props.setProperty("keyid1", "PCP-" + this.getSequenceProcessor().getSequence("LV_ChildSamplePlan", "PrivateChildPlan"));
                        props.setProperty("keyid2", "1");
                        props.setProperty("templatekeyid1", embedchildsampleplanid);
                        props.setProperty("templatekeyid2", embedchildsampleplanversionid);
                        props.setProperty("overrideautokey", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        embedchildsampleplanid = props.getProperty("newkeyid1");
                        embedchildsampleplanversionid = "1";
                    } else {
                        embedchildsampleplanid = "";
                        embedchildsampleplanversionid = "";
                    }
                    sql.setLength(0);
                    sql.append("insert into s_eventdefstspecimendefwi ( ").append(specimendefWIColumns.getColumnValues("columnid", ", ")).append(" )");
                    sql.append(" ( select ");
                    for (int column = 0; column < specimendefWIColumns.size(); ++column) {
                        String columnid = specimendefWIColumns.getString(column, "columnid");
                        if (column > 0) {
                            sql.append(", ");
                        }
                        if ("s_specimendefid".equals(columnid)) {
                            sql.append("'").append(specimendefid).append("'");
                            continue;
                        }
                        if ("s_eventdefid".equals(columnid)) {
                            sql.append("'").append(eventdefid).append("'");
                            continue;
                        }
                        if ("createby".equals(columnid) || "modby".equals(columnid)) {
                            sql.append("'").append(sysuserid).append("'");
                            continue;
                        }
                        if ("createdt".equals(columnid) || "moddt".equals(columnid)) {
                            if (this.database.isOracle()) {
                                sql.append("sysdate");
                                continue;
                            }
                            sql.append("getdate()");
                            continue;
                        }
                        if ("embedchildsampleplanid".equals(columnid)) {
                            sql.append("'").append(embedchildsampleplanid).append("'");
                            continue;
                        }
                        if ("embedchildsampleplanversionid".equals(columnid)) {
                            sql.append("'").append(embedchildsampleplanversionid).append("'");
                            continue;
                        }
                        sql.append(columnid);
                    }
                    sql.append(" from s_eventdefstspecimendefwi where s_eventdefid = ? and s_sampletypeid = ? and s_specimendefid = ? and workitemid = ? and workiteminstance = ?)");
                    this.database.executePreparedUpdate(sql.toString(), new String[]{templateeventdefid, templatesampletypeid, templatespecimendefid, templateworkitemid, templateworkiteminstance});
                }
            }
        }
    }

    private int getMaxUserSequence(DataSet eventDetailsDS, String parentEventId, String clinicalProtocolId, String clinicalProtocolVersionId, String clinicalProtocolRevision, String cohortId) {
        int maxUserSeq = 0;
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("parenteventdefid", parentEventId);
        filterMap.put("clinicalprotocolid", clinicalProtocolId);
        filterMap.put("clinicalprotocolversionid", clinicalProtocolVersionId);
        filterMap.put("clinicalprotocolrevision", clinicalProtocolRevision);
        filterMap.put("cohortid", cohortId);
        DataSet ds = eventDetailsDS.getFilteredDataSet(filterMap);
        if (ds.getRowCount() > 0) {
            ds.sort("usersequence D");
            maxUserSeq = Integer.parseInt(String.valueOf(ds.getBigDecimal(0, "usersequence", new BigDecimal(0))));
        }
        return maxUserSeq;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (BusinessRulesUtil.isEvntDefDependentParticipantEvntExists(rsetid, this.database)) {
            throw new SapphireException(tp.translate("Event(s) already exists for the selected event definition") + " ");
        }
    }

    private String getEventDefLabel(String eventdefid) {
        String key = "eventdeflabel-" + eventdefid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "eventdeflabel", "s_eventdefid = ?", new String[]{eventdefid}));
        }
        return this.localCache.get(key);
    }
}

