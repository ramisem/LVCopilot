/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.RuleUtil;
import com.labvantage.sapphire.admin.ddt.rules.BoxStateRule;
import com.labvantage.sapphire.admin.ddt.rules.DisposeRule;
import com.labvantage.sapphire.admin.ddt.rules.SampleStateRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_Box
extends BaseSDCRules {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 86120 $";
    public static final String SDC_LV_BOX = "LV_Box";
    public static final String STATUS_EMPTY = "Empty";
    public static final String STATUS_PARTIAL = "Partial";
    public static final String STATUS_FULL = "Full";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.processBoxTrackItemRule(sdiData.getDataset("primary"), actionProps);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__sdcruleignore"))) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        if (!"Y".equals(actionProps.getProperty("__overrule"))) {
            boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
            if (this.connectionInfo.hasModule("ASL")) {
                this.checkBoxStateRule(primary, forceUpdate, actionProps);
                if (this.connectionInfo.hasModule("SMS")) {
                    this.checkSampleStateRule(primary, forceUpdate, actionProps);
                }
            }
            this.checkBoxDisposeRule(primary);
        }
        this.updateStorageUnitProperty(actionProps, primary);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1");
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select t.trackitemid, t.linkkeyid1, t.custodialdepartmentid, t.currentstorageunitid");
        sql.append(",(select s.linksdcid from storageunit s where s.storageunitid = t.currentstorageunitid) sulinksdcid");
        sql.append(" from trackitem t");
        sql.append(" where t.linksdcid = 'LV_Box'");
        sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < ds.size(); ++i) {
                String boxid = ds.getValue(i, "linkkeyid1");
                if ("Transit".equals(ds.getValue(i, "custodialdepartmentid"))) {
                    list.add(boxid);
                    continue;
                }
                if (!"LV_Package".equals(ds.getValue(i, "sulinksdcid"))) continue;
                list.add(boxid);
            }
            if (list.size() > 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Delete not allowed"), "VALIDATION", this.getTranslationProcessor().translate("The following Boxes are packed in a Package and can not be deleted") + "<ul><li>" + OpalUtil.toDelimitedString(list, "</li><li>") + "</li></ul>");
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm", "N"));
            try {
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
                if (this.getActionProcessor().hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        if (!"Y".equals(actionProps.getProperty("__sudeleteflag"))) {
            sql.setLength(0);
            safeSQL.reset();
            sql.append("select storageunitid from storageunit");
            sql.append(" where linksdcid = ").append(safeSQL.addVar(SDC_LV_BOX));
            sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm", "N"));
                props.setProperty("_deletelinksdi", "N");
                try {
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
                    if (this.getActionProcessor().hasInfoErrors()) {
                        this.setErrors(this.getActionProcessor().getErrorHandler());
                    }
                }
                catch (ActionException e) {
                    ErrorHandler errorHandler = e.getErrorHandler();
                    for (int i = 0; i < errorHandler.size(); ++i) {
                        ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(i);
                        this.logger.error(errorDetail.toString());
                    }
                    this.setErrors(e.getErrorHandler());
                }
            }
        }
    }

    private void updateStorageUnitProperty(PropertyList actionProps, DataSet primary) throws SapphireException {
        if (actionProps.containsKey("maxtiallowed") || actionProps.containsKey("_arraylayoutid")) {
            String maxtiallowed = actionProps.getProperty("maxtiallowed");
            String arraylayoutid = actionProps.getProperty("_arraylayoutid");
            String arraylayoutversionid = actionProps.getProperty("_arraylayoutversionid");
            if (OpalUtil.isNotEmpty(maxtiallowed) || OpalUtil.isNotEmpty(arraylayoutid)) {
                DataSet ds;
                if (primary.size() > 1000) {
                    String rsetid = this.getDAMProcessor().createRSet(SDC_LV_BOX, primary.getColumnValues("s_boxid", ";"), null, null);
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, linkkeyid1, maxtiallowed, arraylayoutid from storageunit where linksdcid = 'LV_Box' and linkkeyid1 in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                } else {
                    SafeSQL safeSQL = new SafeSQL();
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, linkkeyid1, maxtiallowed, arraylayoutid from storageunit where linksdcid = 'LV_Box' and linkkeyid1 in (" + safeSQL.addIn(primary.getColumnValues("s_boxid", "','")) + ")", safeSQL.getValues());
                }
                if (ds != null) {
                    DataSet dsUpdate = new DataSet();
                    HashMap<String, String> filter = new HashMap<String, String>();
                    primary.addColumnValues("__maxtiallowed", 0, maxtiallowed, ";");
                    primary.addColumnValues("__arraylayoutid", 0, arraylayoutid, ";");
                    primary.addColumnValues("__arraylayoutversionid", 0, arraylayoutversionid, ";");
                    for (int i = 0; i < primary.size(); ++i) {
                        filter.clear();
                        filter.put("linkkeyid1", primary.getString(i, "s_boxid"));
                        int row = ds.findRow(filter);
                        if (row == -1 || ds.getValue(row, "maxtiallowed").equals(primary.getString(i, "__maxtiallowed")) && ds.getValue(row, "arraylayoutid").equals(primary.getString(i, "__arraylayoutid")) && ds.getValue(row, "arraylayoutid").equals(primary.getString(i, "__arraylayoutversionid"))) continue;
                        int r = dsUpdate.addRow();
                        dsUpdate.setString(r, "storageunitid", ds.getString(row, "storageunitid"));
                        if (!ds.getValue(row, "maxtiallowed").equals(primary.getString(i, "__maxtiallowed"))) {
                            dsUpdate.setString(r, "maxtiallowed", primary.getString(row, "__maxtiallowed"));
                        }
                        if (ds.getValue(row, "arraylayoutid").equals(primary.getString(i, "__arraylayoutid"))) continue;
                        dsUpdate.setString(r, "arraylayoutid", primary.getString(i, "__arraylayoutid"));
                        dsUpdate.setString(r, "arraylayoutversionid", primary.getString(i, "__arraylayoutversionid", "1"));
                    }
                    if (dsUpdate.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "StorageUnitSDC");
                        props.setProperty("keyid1", dsUpdate.getColumnValues("storageunitid", ";"));
                        if (-1 != dsUpdate.getColumnType("maxtiallowed")) {
                            props.setProperty("maxtiallowed", dsUpdate.getColumnValues("maxtiallowed", ";"));
                        }
                        if (-1 != dsUpdate.getColumnType("arraylayoutid")) {
                            props.setProperty("arraylayoutid", dsUpdate.getColumnValues("arraylayoutid", ";"));
                            props.setProperty("arraylayoutversionid", dsUpdate.getColumnValues("arraylayoutversionid", ";"));
                        }
                        props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                }
            }
        }
    }

    public void checkBoxStateRule(DataSet primary, boolean forceUpdate, PropertyList actionProps) {
        String tracelogid = primary.size() > 0 ? primary.getString(0, "tracelogid", "") : "";
        BoxStateRule rule = new BoxStateRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", ""));
        for (int count = 0; count < primary.size(); ++count) {
            String boxid = primary.getValue(count, "s_boxid");
            try {
                rule.processRule(boxid, forceUpdate);
                continue;
            }
            catch (ActionException aex) {
                this.setErrors(aex.getErrorHandler());
                continue;
            }
            catch (SapphireException saphEx) {
                this.setError(rule.getClass().getName(), "VALIDATION", saphEx.getMessage());
            }
        }
    }

    public void checkSampleStateRule(DataSet primary, boolean forceUpdate, PropertyList actionProps) {
        List boxSamplesList = RuleUtil.getList(primary, "s_boxid");
        String tracelogid = primary.size() > 0 ? primary.getString(0, "tracelogid", "") : "";
        SampleStateRule rule = new SampleStateRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", ""));
        try {
            rule.processRule(boxSamplesList, forceUpdate);
        }
        catch (SapphireException saphEx) {
            this.setError(rule.getClass().getName(), "VALIDATION", saphEx.getMessage());
        }
    }

    private boolean processBoxTrackItemRule(DataSet primary, PropertyList actionProps) {
        boolean flag = true;
        String forceUpdate = actionProps.getProperty("__sdcruleconfirm");
        String auditReason = actionProps.getProperty("auditreason");
        String auditActivity = actionProps.getProperty("auditactivity", "");
        String auditSignedFlag = actionProps.getProperty("auditsignedflag", "N");
        StringBuilder boxSDIs = new StringBuilder();
        StringBuilder boxSDC = new StringBuilder();
        int boxCount = 0;
        for (int count = 0; count < primary.size(); ++count) {
            String keyid1 = primary.getValue(count, "s_boxid");
            if (keyid1.trim().length() <= 0) continue;
            ++boxCount;
            boxSDC.append(SDC_LV_BOX).append(";");
            boxSDIs.append(keyid1).append(";");
        }
        if (boxCount > 0) {
            try {
                String currentStorageLocation = actionProps.getProperty("currentstoragelocation");
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("copies", Integer.toString(boxCount));
                props.setProperty("auditreason", auditReason);
                props.setProperty("auditactivity", auditActivity);
                props.setProperty("auditsignedflag", auditSignedFlag);
                props.setProperty("linksdcid", boxSDC.substring(0, boxSDC.length() - 1));
                props.setProperty("linkkeyid1", boxSDIs.substring(0, boxSDIs.length() - 1));
                props.setProperty("currentstorageunitid", currentStorageLocation);
                props.setProperty("__sdcruleconfirm", forceUpdate);
                props.setProperty("__cmtimportflag", actionProps.getProperty("__cmtimportflag", "N"));
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(errorHandler);
                }
            }
            catch (ActionException ex) {
                flag = false;
                this.setErrors(ex.getErrorHandler());
            }
        }
        return flag;
    }

    private void checkBoxDisposeRule(DataSet primary) throws SapphireException {
        if (primary == null || primary.size() == 0 || !primary.isValidColumn("activeflag")) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int count = 0; count < primary.size(); ++count) {
            String boxid = primary.getValue(count, "s_boxid");
            if (!this.hasPrimaryValueChanged(primary, count, "activeflag") || !"N".equalsIgnoreCase(primary.getValue(count, "activeflag"))) continue;
            sb.append(";").append(boxid);
        }
        if (sb.length() > 0) {
            DisposeRule rule = new DisposeRule(this.database, this.connectionInfo);
            rule.processRule(SDC_LV_BOX, sb.substring(1));
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    public static DataSet getTrackItems(QueryProcessor queryProcessor, String boxid) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.currentstorageunitid");
        sql.append(" from trackitem t");
        sql.append(" where t.currentstorageunitid in (select s.storageunitid");
        sql.append(" from storageunit s");
        sql.append(" where s.linksdcid = ").append(safeSQL.addVar(SDC_LV_BOX));
        sql.append(" and s.linkkeyid1 = ").append(safeSQL.addVar(boxid));
        sql.append(" union");
        sql.append(" select s.storageunitid");
        sql.append(" from storageunit s");
        sql.append(" where s.parentid = (select s.storageunitid");
        sql.append(" from storageunit s");
        sql.append(" where s.linksdcid = ").append(safeSQL.addVar(SDC_LV_BOX));
        sql.append(" and s.linkkeyid1 = ").append(safeSQL.addVar(boxid));
        sql.append(" ) )");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            return ds;
        }
        return new DataSet();
    }

    public static String getCustodialDepartmentId(QueryProcessor queryProcessor, String boxid) {
        return OpalUtil.getColumnValue(queryProcessor, "trackitem", "custodialdepartmentid", "linksdcid = 'LV_Box' and linkkeyid1 = ?", new String[]{boxid});
    }
}

