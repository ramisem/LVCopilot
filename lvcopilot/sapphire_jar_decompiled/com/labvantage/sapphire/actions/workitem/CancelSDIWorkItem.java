/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CancelSDIWorkItem
extends BaseAction
implements sapphire.action.CancelSDIWorkItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 84531 $";
    public static final String DSSTATUS_CANCELLED = "Cancelled";
    public static final String DSSTATUSCOLUMN = "s_datasetstatus";
    public static final String SDIWORKITEMSTATUSCOLUMN = "workitemstatus";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        String workItemId = properties.getProperty("workitemid");
        String workItemInstance = properties.getProperty("workiteminstance");
        String sdcStatusColumnId = properties.getProperty("sdcstatuscolumn", "samplestatus");
        boolean syncSDIStatus = StringUtil.getYN(properties.getProperty("syncsdistatus"), "Y").equals("Y");
        boolean syncSDIWorkItemGroupStatus = StringUtil.getYN(properties.getProperty("syncsdiworkitemgroupstatus"), "Y").equals("Y");
        String auditreason = properties.getProperty("auditreason");
        String auditactivity = properties.getProperty("auditactivity");
        String auditsignedflag = properties.getProperty("auditsignedflag");
        boolean propsMatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (sdcId.length() == 0 || keyId1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No SDC/KeyId1 specified in action input."));
        }
        if (workItemId.length() == 0 || workItemInstance.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Missing mandatory input WorkItemId/WorkItemInstance"));
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcId, "keycolid3");
        String[] keyid1prop = StringUtil.split(keyId1, ";");
        String[] keyid2prop = null;
        String[] keyid3prop = null;
        if (keycolid2.length() > 0) {
            if (keyId2.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No Keyid2 specified"));
            }
            keyid2prop = StringUtil.split(keyId2, ";");
            if (keyid1prop.length != keyid2prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of Keyid2 not matching with count of Keyid1"));
            }
        } else {
            keyId2 = null;
        }
        if (keycolid3.length() > 0) {
            if (keyId3.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No Keyid3 specified"));
            }
            keyid3prop = StringUtil.split(keyId3, ";");
            if (keyid1prop.length != keyid3prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of Keyid3 not matching with count of Keyid1"));
            }
        } else {
            keyId3 = null;
        }
        ActionProcessor ap = this.getActionProcessor();
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", sdcId);
        props.setProperty("keyid1", keyId1);
        props.setProperty("keyid2", keyId2);
        props.setProperty("keyid3", keyId3);
        props.setProperty("workitemid", workItemId);
        props.setProperty("workiteminstance", workItemInstance);
        props.setProperty(SDIWORKITEMSTATUSCOLUMN, DSSTATUS_CANCELLED);
        props.setProperty("cancelleddt", "n");
        props.setProperty("cancelledby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
        props.setProperty("propsmatch", properties.getProperty("propsmatch"));
        props.setProperty("auditactivity", auditactivity);
        props.setProperty("auditreason", auditreason);
        props.setProperty("auditsignedflag", auditsignedflag);
        ap.processAction("EditSDIWorkItem", "1", props);
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        boolean cancelIncomplete = false;
        boolean cancel = false;
        if (policy != null) {
            String cancelOption = policy.getProperty("cancelactionbehavior", "");
            if (cancelOption.equalsIgnoreCase("Cancel all Children")) {
                cancel = true;
            } else if (cancelOption.equalsIgnoreCase("Cancel Incomplete Children")) {
                cancelIncomplete = true;
                cancel = true;
            }
        }
        String[] workitemIdProp = StringUtil.split(workItemId, ";");
        String[] workItemInstanceProp = StringUtil.split(workItemInstance, ";");
        DAMProcessor dam = this.getDAMProcessor();
        String rsetId = BaseSDIDataAction.createBypassSecurityRSet(sdcId, keyId1, keyId2, keyId3, this.database, this.connectionInfo, false);
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT distinct swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, swi.workiteminstance, swi.groupid, swi.groupinstance, swi.workitemtypeflag, swi.workitemstatus  FROM sdiworkitem swi, rsetitems rs").append(" WHERE swi.sdcid = rs.sdcid and swi.keyid1 = rs.keyid1 and swi.keyid2 = rs.keyid2 and swi.keyid3 = rs.keyid3 and rs.rsetid = " + safeSQL.addVar(rsetId));
        DataSet dsSDIWItems = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        sql.setLength(0);
        DataSet dsCancellable = null;
        if (cancel) {
            safeSQL.reset();
            sql.append("SELECT distinct ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, ds.sourceworkitemid, ds.sourceworkiteminstance ").append(" FROM sdidata ds, sdiworkitemitem swii, sdiworkitem swi, rsetitems rs ").append("  WHERE  ds.sdcid = swii.sdcid").append(" and ds.keyid1 = swii.keyid1").append(" and ds.keyid2 = swii.keyid2").append(" and ds.keyid3 = swii.keyid3").append(" and ds.paramlistid = swii.itemkeyid1").append(" and ds.paramlistversionid = swii.itemkeyid2").append(" and ds.variantid = swii.itemkeyid3").append(" and ds.sourceworkitemid = swii.workitemid").append(" and ds.sourceworkiteminstance = swii.workiteminstance").append(" and swii.sdcid = swi.sdcid and swii.keyid1 = swi.keyid1 and swii.keyid2 = swi.keyid2 and swii.keyid3 = swi.keyid3").append(" and swii.workitemid = swi.workitemid and swii.workiteminstance = swi.workiteminstance ").append(" and swi.sdcid = rs.sdcid and swi.keyid1 = rs.keyid1 and swi.keyid2 = rs.keyid2 ").append(" and swi.keyid3 = rs.keyid3 and rs.rsetid=" + safeSQL.addVar(rsetId) + " and ds.s_datasetstatus != " + safeSQL.addVar(DSSTATUS_CANCELLED));
            if (cancelIncomplete) {
                sql.append(" and ds.s_datasetstatus != " + safeSQL.addVar("Completed"));
            }
            dsCancellable = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        HashMap<String, Object> filter = new HashMap<String, Object>();
        DataSet dsToCancel = new DataSet();
        dam.clearRSet(rsetId);
        DataSet dsSWIWithGrp = new DataSet();
        DataSet dsSWIParentGrp = new DataSet();
        for (int i = 0; i < keyid1prop.length; ++i) {
            String keyid1 = keyid1prop[i];
            String keyid2 = keyid2prop == null || keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
            String keyid3 = keyid3prop == null || keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
            for (int wi = 0; wi < workitemIdProp.length; ++wi) {
                if (propsMatch) {
                    wi = i;
                }
                String wItemId = workitemIdProp[wi];
                String wItemInstance = workItemInstanceProp.length == 0 || workItemInstanceProp.length < workitemIdProp.length || workItemInstanceProp[wi].length() == 0 ? "" : workItemInstanceProp[wi];
                filter.put("sdcid", sdcId);
                filter.put("keyid1", keyid1);
                filter.put("keyid2", keyid2);
                filter.put("keyid3", keyid3);
                if (cancel && dsCancellable != null && dsCancellable.getRowCount() > 0) {
                    filter.put("sourceworkitemid", wItemId);
                    filter.put("sourceworkiteminstance", new BigDecimal(wItemInstance));
                    DataSet dsFilter = dsCancellable.getFilteredDataSet(filter);
                    if (dsFilter.getRowCount() > 0 && cancel) {
                        dsToCancel.copyRow(dsFilter, -1, 1);
                    }
                    filter.remove("sourceworkitemid");
                    filter.remove("sourceworkiteminstance");
                }
                filter.put("workitemid", wItemId);
                filter.put("workiteminstance", new BigDecimal(wItemInstance));
                int findRow = dsSDIWItems.findRow(filter);
                if (findRow > -1) {
                    String groupId = dsSDIWItems.getValue(findRow, "groupid");
                    String groupInstance = dsSDIWItems.getValue(findRow, "groupinstance");
                    if (groupId.length() > 0) {
                        String workItemTypeFlag = dsSDIWItems.getValue(findRow, "workitemtypeflag");
                        if (syncSDIWorkItemGroupStatus && !"P".equalsIgnoreCase(workItemTypeFlag)) {
                            HashMap<String, Object> findGroup = new HashMap<String, Object>();
                            findGroup.put("sdcid", sdcId);
                            findGroup.put("keyid1", keyid1);
                            findGroup.put("keyid2", keyid2);
                            findGroup.put("keyid3", keyid3);
                            findGroup.put("groupid", groupId);
                            findGroup.put("groupinstance", new BigDecimal(groupInstance));
                            findGroup.put("workitemtypeflag", "P");
                            int findGrpRow = dsSDIWItems.findRow(findGroup);
                            if (findGrpRow > -1 && !DSSTATUS_CANCELLED.equals(dsSDIWItems.getValue(findGrpRow, SDIWORKITEMSTATUSCOLUMN))) {
                                dsSWIWithGrp.copyRow(dsSDIWItems, findRow, 1);
                            }
                        }
                        if (cancel && "P".equalsIgnoreCase(workItemTypeFlag)) {
                            dsSWIParentGrp.copyRow(dsSDIWItems, findRow, 1);
                        }
                    }
                }
                if (propsMatch) {
                    wi = workitemIdProp.length;
                }
                filter.clear();
            }
        }
        DataSet childSWIToCancel = new DataSet();
        if (dsSWIParentGrp.getRowCount() > 0) {
            for (int i = 0; i < dsSWIParentGrp.getRowCount(); ++i) {
                filter.clear();
                filter.put("sdcid", dsSWIParentGrp.getValue(i, "sdcid"));
                filter.put("keyid1", dsSWIParentGrp.getValue(i, "keyid1"));
                filter.put("keyid2", dsSWIParentGrp.getValue(i, "keyid2"));
                filter.put("keyid3", dsSWIParentGrp.getValue(i, "keyid3"));
                filter.put("groupid", dsSWIParentGrp.getValue(i, "groupid"));
                filter.put("groupinstance", new BigDecimal(dsSWIParentGrp.getValue(i, "groupinstance")));
                DataSet allMembers = dsSDIWItems.getFilteredDataSet(filter);
                for (int k = 0; k < allMembers.getRowCount(); ++k) {
                    String status;
                    if ("P".equalsIgnoreCase(allMembers.getValue(k, "workitemtypeflag")) || DSSTATUS_CANCELLED.equalsIgnoreCase(status = allMembers.getValue(k, SDIWORKITEMSTATUSCOLUMN)) || cancelIncomplete && "Completed".equalsIgnoreCase(status)) continue;
                    childSWIToCancel.copyRow(allMembers, k, 1);
                }
            }
        }
        if (dsToCancel.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", sdcId);
            props.setProperty("keyid1", dsToCancel.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsToCancel.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", dsToCancel.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", dsToCancel.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", dsToCancel.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", dsToCancel.getColumnValues("variantid", ";"));
            props.setProperty("dataset", dsToCancel.getColumnValues("dataset", ";"));
            props.setProperty("propsmatch", "Y");
            props.setProperty(DSSTATUSCOLUMN, DSSTATUS_CANCELLED);
            props.setProperty("cancelleddt", "n");
            props.setProperty("cancelledby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditsignedflag", auditsignedflag);
            ap.processAction("EditDataSet", "1", props);
            if (syncSDIStatus) {
                props.clear();
                props.setProperty("sdcid", sdcId);
                props.setProperty("keyid1", dsToCancel.getColumnValues("keyid1", ";"));
                props.setProperty("statuscolid", sdcStatusColumnId);
                ap.processAction("SyncSDIDataSetStatus", "1", props);
            }
        }
        if (childSWIToCancel.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", sdcId);
            props.setProperty("keyid1", childSWIToCancel.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", childSWIToCancel.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", childSWIToCancel.getColumnValues("keyid2", ";"));
            props.setProperty("workitemid", childSWIToCancel.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", childSWIToCancel.getColumnValues("workiteminstance", ";"));
            props.setProperty("propsmatch", "Y");
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("syncsdiworkitemgroupstatus", "N");
            props.setProperty("sdcstatuscolumn", sdcStatusColumnId);
            this.processAction(props);
        }
        if (dsSWIWithGrp.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", sdcId);
            props.setProperty("keyid1", dsSWIWithGrp.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsSWIWithGrp.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", dsSWIWithGrp.getColumnValues("keyid3", ";"));
            props.setProperty("workitemid", dsSWIWithGrp.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", dsSWIWithGrp.getColumnValues("workiteminstance", ";"));
            props.setProperty("groupid", dsSWIWithGrp.getColumnValues("groupid", ";"));
            props.setProperty("groupinstance", dsSWIWithGrp.getColumnValues("groupinstance", ";"));
            props.setProperty("syncsdiworkitemgroupstatusonly", "Y");
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditsignedflag", auditsignedflag);
            ap.processAction("SyncSDIWIStatus", "1", props);
        }
    }
}

