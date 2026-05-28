/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddReplicate
extends BaseAction
implements sapphire.action.AddReplicate {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if ("LV_ReagentLot".equalsIgnoreCase(sdcid)) {
            throw new SapphireException("ADD_REPLICATE_FAILED", "Replicate cannot be added to the ReagentLot.");
        }
        String propsmatch = properties.getProperty("propsmatch");
        boolean applylock = properties.getProperty("applylock").equals("Y");
        String keyid_1 = properties.getProperty("keyid1");
        String keyid_2 = properties.getProperty("keyid2");
        String keyid_3 = properties.getProperty("keyid3");
        String rsetid = BaseSDIDataAction.createRSet(sdcid, keyid_1, keyid_2, keyid_3, this.database, this.connectionInfo, applylock);
        Calendar now = DateTimeUtil.getNowCalendar();
        DataSet dataitem = new DataSet();
        DataSet datalimits = new DataSet();
        DataSet dataspec = new DataSet();
        DataSet existdataitem = new DataSet();
        DataSet existdatalimits = new DataSet();
        DataSet existdataspec = new DataSet();
        HashMap<String, String> replicateMap = null;
        String selectDatasets = new StringBuffer().append("SELECT\tsdidataitem.sdcid, sdidataitem.keyid1, ").append("sdidataitem.keyid2, sdidataitem.keyid3, sdidataitem.paramlistid, ").append("sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.dataset, ").append("sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid,").append("sdidataitem.transformrule, sdidataitem.mandatoryflag, ").append("sdidataitem.datatypes, sdidataitem.entrysdcid, sdidataitem.entryreftypeid, ").append("sdidataitem.displayunits, sdidataitem.displayformat, sdidataitem.instrumentfieldid, sdidataitem.usersequence, ").append("sdidataitem.uncertaintyfunction, sdidataitem.uncertaintydisplayformat, sdidataitem.uncertaintyfunctionupper, sdidataitem.uncertaintydisplayformatupper, sdidataitem.uncertaintyasymmetricflag, ").append("sdidataitem.auditsequence, sdidataitem.calcrule, sdidataitem.transformdeferflag, ").append("sdidataitem.textcolor, sdidataitem.aliasid, sdidataitem.defaultvalue, sdidataitem.reportflag ").append("FROM\tsdidataitem, rsetitems ").append("WHERE\trsetitems.rsetid = ?").append(" AND ").append("\t\trsetitems.sdcid  = sdidataitem.sdcid AND ").append("\t\trsetitems.keyid1 = sdidataitem.keyid1 AND ").append("\t\trsetitems.keyid2 = sdidataitem.keyid2 AND ").append("\t\trsetitems.keyid3 = sdidataitem.keyid3 ").append("ORDER BY replicateid desc").toString();
        try {
            this.database.createPreparedResultSet(selectDatasets, new Object[]{rsetid});
            existdataitem.setResultSet(this.database.getResultSet());
        }
        catch (Exception ex) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectDatasets);
        }
        int rows = existdataitem.getRowCount();
        if (rows > 0) {
            replicateMap = new HashMap<String, String>();
            DataSet ds = existdataitem;
            for (int i = rows - 1; i >= 0; --i) {
                replicateMap.put(ds.getValue(i, "keyid1") + ds.getValue(i, "keyid2") + ds.getValue(i, "keyid3") + ds.getValue(i, "paramlistid") + ds.getValue(i, "paramlistversionid") + ds.getValue(i, "variantid") + ds.getValue(i, "dataset") + ds.getValue(i, "paramid") + ds.getValue(i, "paramtype"), ds.getValue(i, "replicateid"));
            }
        }
        selectDatasets = new StringBuffer().append("SELECT\tsdidataitemlimits.sdcid, sdidataitemlimits.keyid1, ").append("sdidataitemlimits.keyid2, sdidataitemlimits.keyid3, sdidataitemlimits.paramlistid, ").append("sdidataitemlimits.paramlistversionid, sdidataitemlimits.variantid, ").append("sdidataitemlimits.dataset,").append("sdidataitemlimits.paramid,").append(" sdidataitemlimits.paramtype,  sdidataitemlimits.replicateid, ").append("sdidataitemlimits.limittypeid, sdidataitemlimits.operator,").append(" sdidataitemlimits.value1, sdidataitemlimits.value2, sdidataitemlimits.value1num, sdidataitemlimits.value2num, sdidataitemlimits.usersequence,").append("").append("sdidataitemlimits.unitsid, sdidataitemlimits.limitfailedactionid,").append("sdidataitemlimits.limitfailedvalue ").append("FROM\tsdidataitemlimits, rsetitems ").append("WHERE\trsetitems.rsetid = ?").append(" AND ").append("\t\trsetitems.sdcid  = sdidataitemlimits.sdcid AND ").append("\t\trsetitems.keyid1 = sdidataitemlimits.keyid1 AND ").append("\t\trsetitems.keyid2 = sdidataitemlimits.keyid2 AND ").append("\t\trsetitems.keyid3 = sdidataitemlimits.keyid3 ").toString();
        try {
            this.database.createPreparedResultSet(selectDatasets, new Object[]{rsetid});
            existdatalimits.setResultSet(this.database.getResultSet());
        }
        catch (Exception ex) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectDatasets);
        }
        selectDatasets = new StringBuffer().append("SELECT\tsdidataitemspec.sdcid, sdidataitemspec.keyid1,\t").append("sdidataitemspec.keyid2, sdidataitemspec.keyid3,\tsdidataitemspec.paramlistid, ").append("sdidataitemspec.paramlistversionid, sdidataitemspec.variantid, sdidataitemspec.dataset,").append("").append("sdidataitemspec.paramid, sdidataitemspec.paramtype, ").append("sdidataitemspec.replicateid, sdidataitemspec.specid, sdidataitemspec.specversionid, ").append("sdidataitemspec.usersequence, sdidataitemspec.limittypeid,").append(" sdidataitemspec.reportflag ").append("FROM sdidataitemspec, rsetitems ").append("WHERE\trsetitems.rsetid = ?").append(" AND ").append("\t\trsetitems.sdcid  = sdidataitemspec.sdcid AND ").append("\t\trsetitems.keyid1 = sdidataitemspec.keyid1 AND ").append("\t\trsetitems.keyid2 = sdidataitemspec.keyid2 AND ").append("\t\trsetitems.keyid3 = sdidataitemspec.keyid3 ").toString();
        try {
            this.database.createPreparedResultSet(selectDatasets, new Object[]{rsetid});
            existdataspec.setResultSet(this.database.getResultSet());
        }
        catch (Exception ex) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectDatasets);
        }
        String separator = properties.getProperty("separator", ";");
        if (propsmatch.equalsIgnoreCase("Y")) {
            DataSet propsds = new DataSet();
            propsds.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), separator);
            propsds.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), separator, "(null)");
            propsds.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), separator, "(null)");
            propsds.addColumnValues("paramlistid", 0, properties.getProperty("paramlistid"), separator);
            propsds.addColumnValues("paramlistversionid", 0, properties.getProperty("paramlistversionid"), separator);
            propsds.addColumnValues("variantid", 0, properties.getProperty("variantid"), separator);
            propsds.addColumnValues("dataset", 0, properties.getProperty("dataset"), separator);
            propsds.addColumnValues("paramid", 0, properties.getProperty("paramid"), separator);
            propsds.addColumnValues("paramtype", 0, properties.getProperty("paramtype"), separator);
            propsds.addColumnValues("numreplicate", 0, properties.getProperty("numreplicate"), separator);
            if (propsds.size() != StringUtil.split(properties.getProperty("keyid1"), separator).length) throw new SapphireException("INVALID_PROPERTIES", "Action properties not match.");
            propsds.padColumns();
            for (int di = 0; di < propsds.size(); ++di) {
                this.addReplicateForParam(dataitem, datalimits, dataspec, existdataitem, existdatalimits, existdataspec, propsds.getString(di, "keyid1"), propsds.getString(di, "keyid2"), propsds.getString(di, "keyid3"), propsds.getString(di, "paramlistid"), propsds.getString(di, "paramlistversionid"), propsds.getString(di, "variantid"), propsds.getString(di, "dataset"), propsds.getString(di, "paramid"), propsds.getString(di, "paramtype"), propsds.getString(di, "numreplicate"), replicateMap);
            }
        } else {
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
            String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
            String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
            String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), separator);
            String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), separator);
            String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), separator);
            String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), separator);
            String[] paramidprop = StringUtil.split(properties.getProperty("paramid"), separator);
            String[] paramtypeprop = StringUtil.split(properties.getProperty("paramtype"), separator);
            String[] numreplicateprop = StringUtil.split(properties.getProperty("numreplicate"), separator);
            for (int pl = 0; pl < paramlistidprop.length; ++pl) {
                String paramlistid = paramlistidprop[pl];
                String paramlistversionid = paramlistversionidprop.length == 0 || paramlistversionidprop.length < paramlistidprop.length || paramlistversionidprop[pl].length() == 0 ? "1" : paramlistversionidprop[pl];
                String variantid = variantidprop.length == 0 || variantidprop.length < paramlistidprop.length || variantidprop[pl].length() == 0 ? "" : variantidprop[pl];
                String datasetstr = datasetprop.length == 0 || datasetprop.length < paramlistidprop.length || datasetprop[pl].length() == 0 ? "0" : datasetprop[pl];
                for (int i = 0; i < keyid1prop.length; ++i) {
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    for (int di = 0; di < paramidprop.length; ++di) {
                        String paramtype = paramtypeprop.length > di ? paramtypeprop[di] : paramtypeprop[paramtypeprop.length - 1];
                        String numrep = numreplicateprop.length > di ? numreplicateprop[di] : numreplicateprop[numreplicateprop.length - 1];
                        this.addReplicateForParam(dataitem, datalimits, dataspec, existdataitem, existdatalimits, existdataspec, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetstr, paramidprop[di], paramtype, numrep, replicateMap);
                    }
                }
            }
        }
        DataSet[] datasets = new DataSet[]{dataitem, dataspec};
        for (int j = 0; j < datasets.length; ++j) {
            if (j == 0) {
                datasets[j].addColumn("releasedflag", 0);
            }
            if (j == 1) {
                datasets[j].addColumn("waivedflag", 0);
            }
            datasets[j].addColumn("createdt", 2);
            datasets[j].addColumn("createby", 0);
            datasets[j].addColumn("createtool", 0);
            datasets[j].addColumn("moddt", 2);
            datasets[j].addColumn("modby", 0);
            datasets[j].addColumn("modtool", 0);
            for (int i = 0; i < datasets[j].getRowCount(); ++i) {
                if (j == 0) {
                    datasets[j].setString(i, "releasedflag", "N");
                }
                if (j == 1) {
                    datasets[j].setString(i, "waivedflag", "N");
                }
                datasets[j].setDate(i, "createdt", now);
                datasets[j].setString(i, "createby", this.connectionInfo.getSysuserId());
                datasets[j].setString(i, "createtool", this.connectionInfo.getTool());
                datasets[j].setDate(i, "moddt", now);
                datasets[j].setString(i, "modby", this.connectionInfo.getSysuserId());
                datasets[j].setString(i, "modtool", this.connectionInfo.getTool());
            }
        }
        String reason = properties.getProperty("auditreason");
        String activity = properties.getProperty("auditactivity");
        String signedFlag = properties.getProperty("auditsignedflag");
        String auditdt = properties.getProperty("auditdt");
        int tracelogid = 0;
        if (reason.length() > 0) {
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", sdcid);
            tracelogprops.setProperty("description", "Added dataitem replicates");
            tracelogprops.setProperty("auditreason", reason);
            tracelogprops.setProperty("auditactivity", activity);
            tracelogprops.setProperty("auditsignedflag", signedFlag);
            tracelogprops.setProperty("auditdt", auditdt);
            ActionProcessor ac = this.getActionProcessor();
            try {
                ac.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                tracelogid = Integer.parseInt(tracelogprops.getProperty("tracelogid"));
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Error calling LoggerUtil.traceLog. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            dataitem.setString(-1, "tracelogid", String.valueOf(tracelogid));
        }
        try {
            DataSetUtil.insert(this.database, dataitem, "sdidataitem");
            DataSetUtil.insert(this.database, datalimits, "sdidataitemlimits");
            DataSetUtil.insert(this.database, dataspec, "sdidataitemspec");
        }
        catch (SapphireException e) {
            throw new SapphireException("DB_INSERT_FAILED", "Failed to update sdi roles: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        this.getDAMProcessor().clearRSet(rsetid);
        if (dataitem.getRowCount() <= 0) return;
        dataitem.sort("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
        ArrayList<DataSet> sdidataList = dataitem.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
        DataSet dsEdit = new DataSet();
        DataSet syncSDIWI = new DataSet();
        DataSet dsApprovalEdit = new DataSet();
        for (int g = 0; g < sdidataList.size(); ++g) {
            DataSet plApproval;
            DataSet ds = sdidataList.get(g);
            String sdc = ds.getString(0, "sdcid");
            String keyid1 = ds.getString(0, "keyid1");
            String keyid2 = ds.getString(0, "keyid2");
            String keyid3 = ds.getString(0, "keyid3");
            String plId = ds.getString(0, "paramlistid");
            String plVersion = ds.getString(0, "paramlistversionid");
            String plVariant = ds.getString(0, "variantid");
            String plDataset = ds.getValue(0, "dataset");
            DataSet plDS = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, s_datasetstatus, sourceworkitemid, sourceworkiteminstance from sdidata  where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and paramlistid = ? and paramlistversionid = ? and variantid = ? and dataset = ?  and s_datasetstatus IN('Completed','DataEntered','Released')", (Object[])new String[]{sdc, keyid1, keyid2, keyid3, plId, plVersion, plVariant, plDataset});
            if (plDS.getRowCount() == 0) continue;
            dsEdit.copyRow(plDS, 0, 1);
            String sourceWI = plDS.getValue(0, "sourceworkitemid");
            if (sourceWI.length() > 0) {
                HashMap<String, String> findSDIWI = new HashMap<String, String>();
                findSDIWI.put("sdcid", sdc);
                findSDIWI.put("keyid1", keyid1);
                findSDIWI.put("keyid2", keyid2);
                findSDIWI.put("keyid3", keyid3);
                if (syncSDIWI.findRow(findSDIWI) < 0) {
                    syncSDIWI.copyRow(plDS, 0, 1);
                }
            }
            if ((plApproval = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, approvalstep, approvalflag from sdidataapproval  where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and paramlistid = ? and paramlistversionid = ? and variantid = ? and dataset = ? and ( approvalflag != 'U')", (Object[])new String[]{sdc, keyid1, keyid2, keyid3, plId, plVersion, plVariant, plDataset})).getRowCount() <= 0) continue;
            dsApprovalEdit.copyRow(plApproval, -1, 1);
        }
        if (dsEdit.getRowCount() <= 0) return;
        ActionProcessor ap = this.getActionProcessor();
        PropertyList actionProps = new PropertyList();
        dsEdit.setString(-1, "s_datasetstatus", "InProgress");
        actionProps.setProperty("sdcid", dsEdit.getValue(0, "sdcid"));
        actionProps.setProperty("keyid1", dsEdit.getColumnValues("keyid1", ";"));
        actionProps.setProperty("keyid2", dsEdit.getColumnValues("keyid2", ";"));
        actionProps.setProperty("keyid3", dsEdit.getColumnValues("keyid3", ";"));
        actionProps.setProperty("paramlistid", dsEdit.getColumnValues("paramlistid", ";"));
        actionProps.setProperty("paramlistversionid", dsEdit.getColumnValues("paramlistversionid", ";"));
        actionProps.setProperty("variantid", dsEdit.getColumnValues("variantid", ";"));
        actionProps.setProperty("dataset", dsEdit.getColumnValues("dataset", ";"));
        actionProps.setProperty("s_datasetstatus", dsEdit.getColumnValues("s_datasetstatus", ";"));
        actionProps.setProperty("propsmatch", "Y");
        ap.processAction("EditDataSet", "1", actionProps);
        if (dsApprovalEdit.getRowCount() > 0) {
            dsApprovalEdit.setString(-1, "approvalflag", "U");
            actionProps.clear();
            actionProps.setProperty("sdcid", dsApprovalEdit.getValue(0, "sdcid"));
            actionProps.setProperty("keyid1", dsApprovalEdit.getColumnValues("keyid1", ";"));
            actionProps.setProperty("keyid2", dsApprovalEdit.getColumnValues("keyid2", ";"));
            actionProps.setProperty("keyid3", dsApprovalEdit.getColumnValues("keyid3", ";"));
            actionProps.setProperty("paramlistid", dsApprovalEdit.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistversionid", dsApprovalEdit.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantid", dsApprovalEdit.getColumnValues("variantid", ";"));
            actionProps.setProperty("dataset", dsApprovalEdit.getColumnValues("dataset", ";"));
            actionProps.put("approvalstep", dsApprovalEdit.getColumnValues("approvalstep", ";"));
            actionProps.put("approvalflag", dsApprovalEdit.getColumnValues("approvalflag", ";"));
            actionProps.put("propsmatch", "Y");
            ap.processAction("EditDataApproval", "1", actionProps);
        }
        if (syncSDIWI.getRowCount() <= 0) return;
        actionProps.clear();
        actionProps.setProperty("sdcid", syncSDIWI.getValue(0, "sdcid"));
        actionProps.setProperty("keyid1", syncSDIWI.getColumnValues("keyid1", ";"));
        actionProps.setProperty("keyid2", syncSDIWI.getColumnValues("keyid2", ";"));
        actionProps.setProperty("keyid3", syncSDIWI.getColumnValues("keyid3", ";"));
        ap.processAction("SyncSDIWIStatus", "1", actionProps);
    }

    private void addReplicateForParam(DataSet dataitem, DataSet datalimits, DataSet dataspec, DataSet existdataitem, DataSet existdatalimits, DataSet existdataspec, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String datasetstr, String paramid, String paramtype, String reps, HashMap replicateMap) {
        String replicateIdentifier = keyid1 + keyid2 + keyid3 + paramlistid + paramlistversionid + variantid + datasetstr + paramid + paramtype;
        String maxrepid = (String)replicateMap.get(replicateIdentifier);
        if (maxrepid != null && maxrepid.length() > 0) {
            try {
                int maxrep = Integer.parseInt(maxrepid);
                int numrep = 1;
                try {
                    numrep = Integer.parseInt(reps);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                HashMap<String, Object> findmap = new HashMap<String, Object>();
                findmap.put("keyid1", keyid1);
                findmap.put("keyid2", keyid2);
                findmap.put("keyid3", keyid3);
                findmap.put("paramlistid", paramlistid);
                findmap.put("paramlistversionid", paramlistversionid);
                findmap.put("variantid", variantid);
                findmap.put("dataset", new BigDecimal(datasetstr));
                findmap.put("paramid", paramid);
                findmap.put("paramtype", paramtype);
                int rowtocopy = existdataitem.findRow(findmap);
                BigDecimal replicateid = existdataitem.getBigDecimal(rowtocopy, "replicateid");
                dataitem.copyRow(existdataitem, rowtocopy, numrep);
                findmap.put("replicateid", replicateid);
                DataSet limitds = existdatalimits.getFilteredDataSet(findmap);
                DataSet specds = existdataspec.getFilteredDataSet(findmap);
                int lastrows = dataitem.getRowCount() - 1;
                int lastrepid = maxrep + numrep;
                for (int rep = 0; rep < numrep; ++rep) {
                    DataSet copy;
                    int ls;
                    int newdi = lastrows--;
                    int repnum = lastrepid--;
                    dataitem.setNumber(newdi, "replicateid", repnum);
                    for (ls = 0; ls < limitds.getRowCount(); ++ls) {
                        copy = limitds.copy();
                        copy.setNumber(ls, "replicateid", repnum);
                        datalimits.copyRow(copy, ls, 1);
                    }
                    for (ls = 0; ls < specds.getRowCount(); ++ls) {
                        copy = specds.copy();
                        copy.setNumber(ls, "replicateid", repnum);
                        dataspec.copyRow(copy, ls, 1);
                    }
                }
                replicateMap.put(replicateIdentifier, new BigDecimal(maxrep + numrep).toString());
            }
            catch (Exception e) {
                this.logger.error("Error in addReplicateForParam" + e.toString(), e);
            }
        } else {
            this.logger.info(new StringBuffer().append("DataItem not exists. Not Adding Replicate for ").append(keyid1).append(";").append(keyid2).append(";").append(keyid3).append(";").append(paramlistid).append(";").append(paramlistversionid).append(";").append(variantid).append(";").append(datasetstr).append(";").append(paramid).append(";").append(paramtype).toString());
        }
    }
}

