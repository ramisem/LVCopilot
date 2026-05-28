/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.SecuritySetUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_SecuritySet
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String securitysetid = "securitysetid";
    private static final String sdcid = "securitysetsdcid";
    private static final String createdby = "createby";
    private static final String operation = "operationid";
    private static final String item = "securitysetitemid";
    private static final String itemtypeflag = "itemtypeflag";
    private static final String securitysetsdc_link = "Security Set SDCs";
    private static final String securitysetitems_link = "Security Set Items";
    private static final String sdc = "LV_SecuritySet";
    private static final String ownersdcid = "ownersdcid";
    private String typeflag = "U";
    String operationEdit = "Admin";
    DataSet templateSSetSDCs = new DataSet();

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!this.isCMTImport()) {
            boolean fromTemplate;
            DataSet primary = sdiData.getDataset("primary");
            boolean bl = fromTemplate = actionProps.getProperty("templateid").length() > 0 || actionProps.getProperty("templatekeyid1").length() > 0;
            if (!fromTemplate) {
                this.populateSecuritySetSDC(primary, sdc);
                this.populateSecuritySetItems(primary);
            } else {
                String ssTemplate = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1"));
                String createdBy = primary.getValue(0, createdby);
                this.database.createPreparedResultSet("templateSSSDCs", "SELECT securitysetsdcid FROM securitysetsdc WHERE securitysetid=?", new Object[]{ssTemplate});
                this.templateSSetSDCs = new DataSet(this.database.getResultSet("templateSSSDCs"));
                if (this.templateSSetSDCs.findRow(sdcid, sdc) < 0) {
                    this.populateSecuritySetSDC(primary, sdc);
                }
                if (!this.database.checkPreparedExists("SELECT 1 FROM securitysetitem WHERE securitysetsdcid='LV_SecuritySet' AND securitysetid=? and securitysetitemid=? AND operationid=?", new Object[]{ssTemplate, createdBy, this.operationEdit})) {
                    this.populateSecuritySetItems(primary);
                }
            }
            DataSet dsAddOwnerSDC = new DataSet();
            for (int i = 0; i < primary.size(); ++i) {
                String ownerSdcId = primary.getValue(i, ownersdcid, "");
                if (ownerSdcId.length() <= 0 || fromTemplate && (!fromTemplate || this.templateSSetSDCs.findRow(sdcid, ownerSdcId) >= 0)) continue;
                dsAddOwnerSDC.copyRow(primary, i, 1);
            }
            if (dsAddOwnerSDC.getRowCount() > 0) {
                this.populateSecuritySetSDC(dsAddOwnerSDC, null);
                this.populateItems(dsAddOwnerSDC);
            }
        }
    }

    private void populateItems(DataSet ds) throws ActionException {
        for (int j = 0; j < ds.size(); ++j) {
            String ownersdc = ds.getValue(j, ownersdcid);
            String securitySetId = ds.getValue(j, securitysetid);
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select operationid from sdcoperation where sdcid = " + safeSQL.addVar(ownersdc) + " order by usersequence";
            DataSet sqlDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            DataSet securitysetitemsdata = new DataSet();
            securitysetitemsdata.addColumn(securitysetid, 0);
            securitysetitemsdata.addColumn(sdcid, 0);
            securitysetitemsdata.addColumn(operation, 0);
            securitysetitemsdata.addColumn(item, 0);
            securitysetitemsdata.addColumn(itemtypeflag, 0);
            for (int i = 0; i < sqlDataSet.size(); ++i) {
                int rowindex = securitysetitemsdata.addRow();
                securitysetitemsdata.setString(rowindex, securitysetid, securitySetId);
                securitysetitemsdata.setString(rowindex, sdcid, ownersdc);
                securitysetitemsdata.setString(rowindex, operation, sqlDataSet.getValue(i, operation));
                securitysetitemsdata.setString(rowindex, item, ds.getValue(j, createdby));
                securitysetitemsdata.setString(rowindex, itemtypeflag, this.typeflag);
            }
            PropertyList securitysetitemsprop = new PropertyList();
            securitysetitemsprop.setProperty("sdcid", sdc);
            securitysetitemsprop.setProperty("keyid1", securitySetId);
            securitysetitemsprop.setProperty("linkid", securitysetsdc_link);
            securitysetitemsprop.setProperty("detaillinkid", securitysetitems_link);
            securitysetitemsprop.setProperty(sdcid, securitysetitemsdata.getColumnValues(sdcid, ";"));
            securitysetitemsprop.setProperty(securitysetid, securitysetitemsdata.getColumnValues(securitysetid, ";"));
            securitysetitemsprop.setProperty(operation, securitysetitemsdata.getColumnValues(operation, ";"));
            securitysetitemsprop.setProperty(item, securitysetitemsdata.getColumnValues(item, ";"));
            securitysetitemsprop.setProperty(itemtypeflag, securitysetitemsdata.getColumnValues(itemtypeflag, ";"));
            this.getActionProcessor().processAction("AddSDIDetail", "1", securitysetitemsprop);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("securitysets", "select keyid1 securitysetid from rsetitems where sdcid='LV_SecuritySet' and rsetid=?", new Object[]{rsetid});
        DataSet ds = new DataSet(this.database.getResultSet("securitysets"));
        if (ds.getRowCount() > 0 && !"N".equals(actionProps.getProperty("predeletecheck"))) {
            this.authenticateUser(ds.getColumnValues(securitysetid, ";"), "delete");
        }
    }

    private void populateSecuritySetItems(DataSet primary) throws ActionException {
        DataSet securitysetitemsdata = new DataSet();
        securitysetitemsdata.addColumn(securitysetid, 0);
        securitysetitemsdata.addColumn(sdcid, 0);
        securitysetitemsdata.addColumn(operation, 0);
        securitysetitemsdata.addColumn(item, 0);
        securitysetitemsdata.addColumn(itemtypeflag, 0);
        for (int i = 0; i < primary.size(); ++i) {
            int rowindex = securitysetitemsdata.addRow();
            securitysetitemsdata.setString(rowindex, securitysetid, primary.getValue(i, securitysetid));
            securitysetitemsdata.setString(rowindex, sdcid, sdc);
            securitysetitemsdata.setString(rowindex, operation, this.operationEdit);
            securitysetitemsdata.setString(rowindex, item, primary.getValue(i, createdby));
            securitysetitemsdata.setString(rowindex, itemtypeflag, this.typeflag);
        }
        PropertyList securitysetitemsprop = new PropertyList();
        securitysetitemsprop.setProperty("keyid1", securitysetitemsdata.getColumnValues(securitysetid, ";"));
        securitysetitemsprop.setProperty("sdcid", sdc);
        securitysetitemsprop.setProperty("linkid", securitysetsdc_link);
        securitysetitemsprop.setProperty("detaillinkid", securitysetitems_link);
        securitysetitemsprop.setProperty(sdcid, securitysetitemsdata.getColumnValues(sdcid, ";"));
        securitysetitemsprop.setProperty(operation, securitysetitemsdata.getColumnValues(operation, ";"));
        securitysetitemsprop.setProperty(item, securitysetitemsdata.getColumnValues(item, ";"));
        securitysetitemsprop.setProperty(itemtypeflag, securitysetitemsdata.getColumnValues(itemtypeflag, ";"));
        this.getActionProcessor().processAction("AddSDIDetail", "1", securitysetitemsprop);
    }

    private void populateSecuritySetSDC(DataSet ds, String sdcName) throws ActionException {
        DataSet securitySetData = new DataSet();
        securitySetData.addColumn(securitysetid, 0);
        securitySetData.addColumn(sdcid, 0);
        for (int i = 0; i < ds.size(); ++i) {
            int rowindex = securitySetData.addRow();
            securitySetData.setString(rowindex, securitysetid, ds.getValue(i, securitysetid));
            securitySetData.setString(rowindex, sdcid, sdcName != null ? sdcName : ds.getValue(i, ownersdcid));
        }
        PropertyList securitysetsdcprops = new PropertyList();
        securitysetsdcprops.setProperty("sdcid", sdc);
        securitysetsdcprops.setProperty("keyid1", securitySetData.getColumnValues(securitysetid, ";"));
        securitysetsdcprops.setProperty("linkid", securitysetsdc_link);
        securitysetsdcprops.setProperty(securitysetid, securitySetData.getColumnValues(securitysetid, ";"));
        securitysetsdcprops.setProperty(sdcid, securitySetData.getColumnValues(sdcid, ";"));
        this.getActionProcessor().processAction("AddSDIDetail", "1", securitysetsdcprops);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null && primary.getRowCount() > 0) {
            this.authenticateUser(primary.getColumnValues(securitysetid, ";"), "edit");
        }
    }

    private void authenticateUser(String securitySet, String operation) throws SapphireException {
        ConnectionInfo connectionInfo = this.getConnectionInfo();
        String currentUser = connectionInfo.getSysuserId();
        String currentUserJobType = connectionInfo.getCurrentJobtype();
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        String notPermitted = SecuritySetUtil.findSecuritySetsNonPermittedForAnOperation(securitySet, currentUser, currentUserJobType, sdc, "Admin", this.getDAMProcessor(), this.getQueryProcessor(), sapphireConnection);
        if (notPermitted.length() > 0) {
            HashMap<String, String> tokenMap = new HashMap<String, String>();
            tokenMap.put("operation", this.getTranslationProcessor().translate(operation));
            throw new SapphireException(this.getTranslationProcessor().translate("You are not allowed to [operation] the following Security Set(s)", tokenMap) + ": " + notPermitted);
        }
    }
}

