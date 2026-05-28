/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.ddt.AddSDCOperation;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.sql.CallableStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;

public class SDC
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85004 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary.size() > 1) {
            throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "Only 1 SDC may be maintained in any edit!");
        }
        String sdcid = primary.getString(0, "sdcid");
        if (sdcid == null || sdcid.length() > 40) {
            throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "Invalid SDC - id must be 1 - 40 alpha-numeric characters");
        }
        String tableid = primary.getString(0, "tableid");
        if (tableid == null) {
            throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "No table name specified");
        }
        try {
            String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
            String sdctype = "U";
            if (compcode.length() > 0) {
                sdctype = "S";
                tableid = compcode + "_" + tableid;
            } else {
                String string = sdctype = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "S" : "U";
                if (sdctype.equals("U") && (sdcid.startsWith("LV_") || sdcid.endsWith("SDC"))) {
                    throw new SapphireException("Invalid name given to User SDC. LabVantage has reserved SDC names starting with 'LV_' or ending in 'SDC'.");
                }
                tableid = (sdctype.equals("U") ? "u_" : "") + sdcid.toLowerCase();
            }
            StringBuffer newtableid = new StringBuffer();
            for (int i = 0; i < tableid.length(); ++i) {
                char c = tableid.charAt(i);
                if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') && c != '_') continue;
                newtableid.append(c);
            }
            tableid = newtableid.toString();
            String versionedFlag = actionProps.getProperty("versionedflag", "N");
            int typeChars = sdctype.equals("U") ? 2 : 0;
            int versionChars = versionedFlag.equals("Y") ? 9 : 4;
            int compChars = compcode.length() > 0 ? 4 : 0;
            int len = 30 - typeChars - compChars - versionChars;
            if (tableid.length() > len) {
                throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "Invalid table name - tableid must be 1 - " + len + " alpha-numeric characters");
            }
            if (actionProps.getProperty("templatekeyid1").length() == 0) {
                String callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addnewusersdc( ?, ?, ?, ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.setString(1, tableid);
                cs.registerOutParameter(2, 12);
                cs.setString(3, versionedFlag);
                cs.setString(4, sdctype.equals("U") ? "N" : "Y");
                cs.setString(5, compcode);
                cs.executeUpdate();
                tableid = cs.getString(2);
            }
            primary.setString(0, "tableid", tableid);
            primary.setString(0, "sdctype", sdctype);
            primary.setNumber(0, "keycolumns", versionedFlag.equals("N") ? 1 : 2);
            primary.setString(0, "versionedflag", versionedFlag);
            primary.setString(0, "versionsequenceflag", "Y");
            primary.setString(0, "auditedflag", "N");
            primary.setString(0, "auditpromptflag", "O");
            primary.setString(0, "accesscontrolledflag", "N");
            primary.setString(0, "standardmaintflag", "X");
            primary.setString(0, "multimaintflag", "Y");
            primary.setString(0, "linkableflag", "Y");
            primary.setString(0, "categoriesflag", "Y");
            primary.setString(0, "templatableflag", "Y");
            primary.setString(0, "notesflag", "Y");
            primary.setString(0, "attachmentsflag", "N");
            primary.setString(0, "addressesflag", "N");
            primary.setString(0, "workflowflag", "N");
            primary.setString(0, "dataentryflag", "N");
            primary.setString(0, "securityflag", "N");
            primary.setString(0, "chargeoptionflag", "N");
            primary.setString(0, "pricelistflag", "N");
            primary.setString(0, "cocableflag", "N");
            primary.setString(0, "orderableflag", "N");
            primary.setString(0, "specflag", "N");
            primary.setString(0, "workitemflag", "N");
            primary.setString(0, "aliasableflag", "N");
            primary.setString(0, "scheduleableflag", "N");
            primary.setString(0, "searchableflag", "N");
            primary.setString(0, "coordinatableflag", "N");
            primary.setString(0, "plannableflag", "N");
            String exportscript = "<?xml version=\"1.0\"?>\n<transferpackage exportscript=\"true\">\n  <propertylist>\n    <property id=\"export.keyid3\" type=\"simple\"></property>\n    <property id=\"export.rsetid\" type=\"simple\"></property>\n    <property id=\"export.keyid1\" type=\"simple\"></property>\n    <property id=\"export.sdcid\" type=\"simple\"></property>\n    <property id=\"export.keyid2\" type=\"simple\"></property>\n  </propertylist>\n  <transfer id=\"exportscript\" type=\"export\">\n    <exportXML  forceupdate=\"false\" forcenullupdate=\"false\">\n      <sdi sdcid=\"" + sdcid + "\" rsetid=\"[export.rsetid]\" exportdetails=\"true\" exportfkdetails=\"false\" exportsdidetails=\"true\" exportroles=\"true\" exportcategories=\"true\" primaryforceupdate=\"false\" primaryforcenullupdate=\"false\" detailforceupdate=\"false\" detailforcenullupdate=\"false\" syncdatamodel=\"false\" exportid=\"\"/>\n    </exportXML>\n  </transfer>\n</transferpackage>";
            this.database.executePreparedUpdate("INSERT INTO sdcexport (sdcid, exportid, exportdesc, exportscript) VALUES ( ?, 'Standard', ?, ? )", new Object[]{sdcid, "Standard export script for " + sdcid, exportscript});
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to create default SDC table. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        this.doMaskingEnableTasks(primary);
        this.doChangeControlFlagTasks(primary);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        boolean isAutoKey;
        CallableStatement cs;
        DataSet primary = sdiData.getDataset("primary");
        if (primary.size() > 1) {
            throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "Only 1 SDC may be maintained in any edit!");
        }
        String sdcid = primary.getString(0, "sdcid");
        String accesscontrolledflag = primary.getValue(0, "accesscontrolledflag");
        if (this.hasPrimaryValueChanged(primary, 0, "accesscontrolledflag") && accesscontrolledflag.length() == 0) {
            primary.setString(0, "accesscontrolledflag", "N");
            accesscontrolledflag = "N";
        }
        if (accesscontrolledflag.equals("D") || "DataSet".equals(sdcid) && "B".equals(accesscontrolledflag)) {
            try {
                cs = this.database.prepareCall("{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addsdisecurity( ? ) }");
                cs.setString(1, primary.getString(0, "tableid"));
                cs.executeUpdate();
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to setup departmental security columns. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            this.addOperationToSDC(sdcid, "list");
        } else if (accesscontrolledflag.equals("S")) {
            try {
                cs = this.database.prepareCall("{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addsdisecurity( ?, ? ) }");
                cs.setString(1, primary.getString(0, "tableid"));
                cs.setString(2, "S");
                cs.executeUpdate();
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to setup SDI security columns. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            this.addOperationToSDC(sdcid, "list");
        }
        String uuidflag = primary.getValue(0, "uuidflag");
        if (this.hasPrimaryValueChanged(primary, 0, "uuidflag") && uuidflag.length() == 0) {
            primary.setString(0, "uuidflag", "N");
            uuidflag = "N";
        }
        boolean bl = isAutoKey = primary.getValue(0, "keygenerationrule").trim().length() > 0;
        if ("Y".equals(primary.getValue(0, "changecontrolledflag")) && ("Y".equals(primary.getValue(0, "versionedflag")) && !"Action".equals(sdcid) || isAutoKey)) {
            primary.setString(0, "uuidflag", "Y");
            uuidflag = "Y";
        }
        if (uuidflag.equals("Y")) {
            try {
                CallableStatement cs2 = this.database.prepareCall("{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "AddUUID( ? ) }");
                cs2.setString(1, primary.getString(0, "tableid"));
                cs2.executeUpdate();
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to setup uuid column. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        String auditedflag = primary.getValue(0, "auditedflag");
        if (this.hasPrimaryValueChanged(primary, 0, "auditedflag") && auditedflag.length() == 0) {
            primary.setString(0, "auditedflag", "N");
            auditedflag = "N";
        }
        String auditpromptflag = primary.getValue(0, "auditpromptflag");
        if (this.hasPrimaryValueChanged(primary, 0, "auditpromptflag") && auditpromptflag.length() == 0 && (auditedflag.equals("P") || auditedflag.equals("W"))) {
            throw new SapphireException("Audit prompt option required for specified audit method!");
        }
        if (this.hasPrimaryValueChanged(primary, 0, "reftypeid") && (auditpromptflag.equals("R") || auditpromptflag.equals("S") || auditpromptflag.equals("B") || auditpromptflag.equals("T")) && primary.getValue(0, "reftypeid").length() == 0) {
            throw new SapphireException("Reason reference type required for specified audit prompt option!");
        }
        if ("Y".equals(primary.getValue(0, "uuidflag")) && "N".equals(primary.getValue(0, "auditedflag", "N"))) {
            primary.setString(0, "auditedflag", "B");
        }
        this.doMaskingEnableTasks(primary);
        this.addWAPColumns(primary);
        this.doChangeControlFlagTasks(primary);
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDCLinkData");
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SysConfigProperties");
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "TableColumns");
        if (this.hasPrimaryValueChanged(primary, 0, "changecontrolledflag")) {
            CMTPolicy.resetCache(this.connectionInfo.getDatabaseId());
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String sdcid = primary.getString(0, "sdcid");
        String auditedflag = primary.getValue(0, "auditedflag");
        try {
            CallableStatement cs;
            String callstmt;
            if (!auditedflag.equals("N")) {
                callstmt = "{call lv_audit" + (this.connectionInfo.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                cs = this.database.prepareCall(callstmt);
                cs.setString(1, sdcid);
                cs.setString(2, "Both");
                cs.executeUpdate();
            } else {
                callstmt = "{call lv_audit" + (this.connectionInfo.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                cs = this.database.prepareCall(callstmt);
                cs.setString(1, sdcid);
                cs.setString(2, "Off");
                cs.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to setup auditing options. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        if (this.hasPrimaryValueChanged(primary, 0, "coordinatableflag")) {
            String coordinatableflag = primary.getValue(0, "coordinatableflag");
            try {
                if (coordinatableflag.equals("Y")) {
                    String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
                    String callstmt = "{call LV_Tab" + (this.connectionInfo.isOracle() ? "." : "_") + "AddCoordinateCols( ? ) }";
                    CallableStatement cs = this.database.prepareCall(callstmt);
                    cs.setString(1, tableid);
                    cs.executeUpdate();
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to add coordinate columns. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            throw new SapphireException("SDC's cannot be deleted using DeleteSDI!");
        }
        this.database.createPreparedResultSet("SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{actionProps.getProperty("keyid1")});
        if (this.database.getNext()) {
            actionProps.setProperty("tableid", this.database.getString("tableid"));
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (Configuration.isDevmode(this.connectionInfo.getDatabaseId()) && actionProps.containsKey("tableid")) {
            CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
            String tableid = actionProps.getProperty("tableid");
            String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "droptablewithclear( ? ) }";
            CallableStatement cs = this.database.prepareCall(callstmt);
            try {
                cs.setString(1, tableid);
                cs.executeUpdate();
            }
            catch (SQLException e) {
                throw new SapphireException("Drop table '" + tableid + "'failed when deleting SDC '" + actionProps.getProperty("keyid1") + "'!");
            }
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void doMaskingEnableTasks(DataSet primary) throws SapphireException {
        String sdcid = primary.getString(0, "sdcid");
        if (this.hasPrimaryValueChanged(primary, 0, "maskableflag") && "Y".equals(primary.getString(0, "maskableflag", "N"))) {
            this.addOperationToSDC(sdcid, "ViewMaskedData");
            this.addMaskingLevelColumn(primary.getString(0, "tableid", this.getOldPrimaryValue(primary, 0, "tableid")));
        }
    }

    private void addOperationToSDC(String sdcid, String operation) throws SapphireException {
        try {
            this.database.createPreparedResultSet("sdcoperation_mask", "SELECT usersequence, operationid FROM sdcoperation WHERE sdcid = ? ORDER BY usersequence desc", new Object[]{sdcid});
            DataSet dsOperation = new DataSet(this.database.getResultSet("sdcoperation_mask"));
            int userSequence = 1;
            int rowCount = dsOperation.getRowCount();
            if (dsOperation.findRow("operationid", operation) == -1) {
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcid);
                props.put("operationid", operation);
                if (rowCount > 0) {
                    userSequence = dsOperation.getInt(0, "usersequence", 0) + 1;
                }
                props.put("usersequence", userSequence);
                ActionProcessor ap = this.getActionProcessor();
                ap.processActionClass(AddSDCOperation.class.getName(), props, false);
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("addOperationToSDC", "FAILURE", this.getTranslationProcessor().translate("Exception occurred while updating SDC Operation: ") + operation);
        }
    }

    private void addMaskingLevelColumn(String tableId) throws SapphireException {
        try {
            CallableStatement cs = this.database.prepareCall("{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addmaskinglevel( ? ) }");
            cs.setString(1, tableId);
            cs.executeUpdate();
            this.database.closeCall();
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to setup data masking columns. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void addWAPColumns(DataSet primary) throws SapphireException {
        if (this.hasPrimaryValueChanged(primary, 0, "plannableflag") && "Y".equals(primary.getString(0, "plannableflag", "N"))) {
            String tableId = primary.getString(0, "tableid");
            try {
                CallableStatement cs = this.database.prepareCall("{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "AddWAPColumns( ? ) }");
                cs.setString(1, tableId);
                cs.executeUpdate();
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to add wap columns. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    @Override
    public void postGenerateSnapshot(Snapshot snapshot, boolean isPackaging) throws SapphireException {
        SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
        SDISnapshotItem snapshotItem = sdiSnapshot.getSnapshotItem();
        SDIData sdiData = sdiSnapshot.getSDIData();
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(snapshotItem.getKeyId1());
        String sql = "SELECT * FROM systable WHERE tableid = ? OR tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()});
        sdiData.setDataset("systable", ds);
        sql = "SELECT * FROM syscolumn WHERE tableid = ? OR tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') )";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()});
        sdiData.setDataset("syscolumn", ds);
        sql = "SELECT * FROM syscolumnproperty WHERE tableid = ? OR tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') )";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()});
        sdiData.setDataset("syscolumnproperty", ds);
        sql = "SELECT * FROM sysref WHERE tableid = ? OR tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') )";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()});
        sdiData.setDataset("sysref", ds);
        sql = "SELECT * FROM sysrefcolumn WHERE sysrefcolumn.refid IN (SELECT sysref.refid FROM sysref WHERE sysref.tableid = ? OR sysref.tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR sysref.tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') ) )" + (sdiSnapshot.getCompCode().length() > 0 ? " AND ( LOWER( sysrefcolumn.columnid ) LIKE '" + sdiSnapshot.getCompCode().toLowerCase() + "_%' OR sysrefcolumn.columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : "");
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()}, true);
        sdiData.setDataset("sysrefcolumn", ds);
        sql = "SELECT * FROM sysextendedcolumn WHERE tableid = ? OR tableid IN (SELECT linktableid FROM sdclink WHERE sdcid = ? AND linktype IN ('D','M') ) OR tableid IN (SELECT linktableid FROM sdcdetaillink WHERE sdcid = ? AND linktype IN ('D') )" + (sdiSnapshot.getCompCode().length() > 0 ? " AND ( LOWER( columnid ) LIKE '" + sdiSnapshot.getCompCode().toLowerCase() + "_%' OR columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : "");
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcProps.getProperty("tableid"), snapshotItem.getKeyId1(), snapshotItem.getKeyId1()});
        sdiData.setDataset("sysextendedcolumn", ds);
        sql = "SELECT * FROM sdcproperty WHERE sdcid = ?";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{snapshotItem.getKeyId1()});
        sdiData.setDataset("sdcproperty", ds);
        sdiData.removeDataset("sdclink");
        sql = "SELECT * FROM sdclink WHERE sdcid = ?";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{snapshotItem.getKeyId1()});
        sdiData.setDataset("sdclink", ds);
        sdiData.removeDataset("sdcdetaillink");
        sql = "SELECT * FROM sdcdetaillink WHERE sdcid = ?";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{snapshotItem.getKeyId1()});
        sdiData.setDataset("sdcdetaillink", ds);
        if (sdiSnapshot.getCompCode() != null && sdiSnapshot.getCompCode().length() > 0) {
            this.handleCompCode(sdiData.getDataset("syscolumn"), sdiSnapshot.getCompCode());
            this.handleCompCode(sdiData.getDataset("syscolumnproperty"), sdiSnapshot.getCompCode());
        }
    }

    private void handleCompCode(DataSet dataset, String compCode) {
        if (dataset != null && dataset.getRowCount() > 0) {
            for (int i = dataset.getRowCount() - 1; i >= 0; --i) {
                String columnId = dataset.getString(i, "columnid", "").toLowerCase();
                if (columnId.startsWith(compCode + "_") || columnId.length() > 4 && columnId.charAt(3) == '_') continue;
                dataset.deleteRow(i);
            }
        }
    }

    private void doChangeControlFlagTasks(DataSet primary) throws SapphireException {
        String sdcType = primary.getString(0, "sdctype", "");
        boolean isDevMode = Configuration.isDevmode(this.getConnectionInfo().getDatabaseId());
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        boolean isChangeControlFlagChanged = this.hasPrimaryValueChanged(primary, 0, "changecontrolledflag");
        if (isChangeControlFlagChanged) {
            String newChangeControlFlag = primary.getString(0, "changecontrolledflag", "");
            String sdcId = primary.getString(0, "sdcid", "");
            String sdcCompCode = primary.getString(0, "compcode", "");
            if ("C".equals(sdcType)) {
                if (!isDevMode) {
                    if ("Y".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_YES_SDC_LIST.contains(sdcId)) {
                        throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Full change control not allowed on this SDC."));
                    }
                    if ("T".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_YES_SDC_LIST.contains(sdcId) && !CMTUtil.OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.contains(sdcId)) {
                        throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Template change control not allowed on this SDC."));
                    }
                    if ("P".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.contains(sdcId)) {
                        throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Parent change control not allowed on this SDC."));
                    }
                    if ("N".equals(newChangeControlFlag) && CMTUtil.OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.contains(sdcId)) {
                        throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("This SDC requires to be under Parent change control only."));
                    }
                }
            } else if ("S".equals(sdcType)) {
                if (!isDevMode) {
                    if (sdcCompCode.length() > 0) {
                        if ("P".equals(newChangeControlFlag)) {
                            throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Parent change control not allowed on Component SDCs."));
                        }
                    } else {
                        if ("Y".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_YES_SDC_LIST.contains(sdcId)) {
                            throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Full change control not allowed on this SDC."));
                        }
                        if ("T".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_YES_SDC_LIST.contains(sdcId) && !CMTUtil.OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.contains(sdcId)) {
                            throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Template change control not allowed on this SDC."));
                        }
                        if ("P".equals(newChangeControlFlag) && !CMTUtil.OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.contains(sdcId)) {
                            throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Parent change control not allowed on this SDC."));
                        }
                        if ("N".equals(newChangeControlFlag) && CMTUtil.OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.contains(sdcId)) {
                            throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("This SDC requires to be under Parent change control only."));
                        }
                    }
                }
            } else if ("D".equals(sdcType)) {
                if (!"N".equals(newChangeControlFlag)) {
                    throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Change Control not allowed on D-type SDC."));
                }
            } else if ("U".equals(sdcType)) {
                if ("P".equals(newChangeControlFlag)) {
                    throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Parent change control not allowed on User Defined SDCs."));
                }
            } else {
                throw new SapphireException("ChangeControlFlagCheck", "VALIDATION", this.getTranslationProcessor().translate("Unknown SDC type. Change control not allowed."));
            }
        }
        if (primary.isValidColumn("changecontrolledflag") && primary.getString(0, "changecontrolledflag", "").length() == 0) {
            primary.setString(0, "changecontrolledflag", "N");
        }
    }
}

