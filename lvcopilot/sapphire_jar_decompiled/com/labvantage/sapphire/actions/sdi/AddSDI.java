/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.actions.CopySDIDetail;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.cmt.CheckOutSDI;
import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.actions.eventplans.AddSDIEventPlan;
import com.labvantage.sapphire.actions.sdi.AddSDIAttribute;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdi.CopySDIAttachment;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.ajax.operations.GetForceNewPolicy;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddEventObject;
import com.labvantage.sapphire.modules.sdisecurity.SDISecurity;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SequenceService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddSDI
extends BaseSDIAction
implements sapphire.action.AddSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int i;
        String keyid1;
        DataSet notes;
        String tempcopies;
        PropertyList copyDownPolicyNode;
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        Calendar now = DateTimeUtil.getNowCalendar();
        this.logger.info("Getting SDC information");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
        if (sdc == null) {
            throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
        }
        boolean allowAttributes = sdc.getProperty("allowattributesflag").equalsIgnoreCase("Y");
        sdcid = sdc.getProperty("sdcid");
        String strVersionApprType = sdc.getProperty("versionapprovaltypeid");
        String tableid = sdc.getProperty("tableid");
        String keygenerationrule = properties.getProperty("keygenerationrule", sdc.getProperty("keygenerationrule"));
        boolean autokeygen = StringUtil.getLen(keygenerationrule) > 0L && keygenerationrule.charAt(0) == 'A';
        boolean templatable = sdc.getProperty("templatableflag").equals("Y");
        boolean rolelevel = sdc.getProperty("accesscontrolledflag").equals("Y");
        boolean departmentlevel = sdc.getProperty("accesscontrolledflag").equals("D");
        boolean sdiSecurityEnabled = sdc.getProperty("accesscontrolledflag").equals("S");
        boolean maskableFlag = "Y".equals(sdc.getProperty("maskableflag"));
        boolean allowdataentry = sdc.getProperty("dataentryflag").equals("Y");
        String keycolid1 = sdc.getProperty("keycolid1");
        String keycolid2 = sdc.getProperty("keycolid2");
        String keycolid3 = sdc.getProperty("keycolid3");
        String auditflag = sdc.getProperty("auditedflag");
        String promptflag = sdc.getProperty("auditpromptflag");
        boolean sdc_versioned = "Y".equalsIgnoreCase(sdc.getProperty("versionedflag"));
        PropertyListCollection columns = sdc.getCollection("columns");
        if (columns == null || columns.size() == 0) {
            throw new SapphireException("SDC properties indicate no columns!");
        }
        this.logger.info("table: " + tableid + " keygenerationrule: " + keygenerationrule);
        this.logger.info("keycolumns: " + keycolid1);
        boolean overrideautokey = properties.getProperty("overrideautokey").equals("Y");
        boolean isAddTemplate = properties.getProperty("templateflag").equals("Y");
        if (isAddTemplate && properties.getProperty("autokeytemplate", "N").equals("N")) {
            overrideautokey = true;
        }
        String templatekeyid1 = properties.getProperty("templatekeyid1");
        String templatekeyid2 = properties.getProperty("templatekeyid2");
        String templatekeyid3 = properties.getProperty("templatekeyid3");
        if (templatekeyid1 == null || templatekeyid1.length() == 0) {
            templatekeyid1 = properties.getProperty("templateid");
            templatekeyid2 = "";
            templatekeyid3 = "";
        }
        boolean newVersion = properties.getProperty("newversion", "N").equals("Y");
        boolean isCopySDI = properties.getProperty("copysdi").equals("Y");
        String blockColumnUpdatesMode = properties.getProperty("blockcolumnupdatesmode", "N");
        String blockedColumns = properties.getProperty("blockedcolumns");
        if (blockedColumns.length() > 0) {
            String[] blockColArray;
            for (String columnid : blockColArray = StringUtil.split(blockedColumns, ";")) {
                String newValue;
                if (columnid.length() <= 0 || columns.find("columnid", columnid) == null || (newValue = properties.getProperty(columnid).replace(";", "").replace("(null)", "")).length() <= 0 || newValue.equals("(null)")) continue;
                if (blockColumnUpdatesMode.equals("E")) {
                    this.logger.error("An attempt was made to set blocked column " + columnid + " to '" + newValue + "'. Throwing Error.");
                    throw new ActionException("An attempt was made to set a value in blocked column " + columnid + " to '" + newValue + "'");
                }
                properties.setProperty(columnid, "");
                this.logger.warn("An attempt was made to set blocked column " + columnid + " to '" + newValue + "'. New value ignored.");
            }
        }
        if ((copyDownPolicyNode = this.getConfigurationProcessor().getPolicy("CopyDownPolicy", sdcid + " Custom")).size() == 0) {
            copyDownPolicyNode = this.getConfigurationProcessor().getPolicy("CopyDownPolicy", "Sapphire Custom");
        }
        PropertyList plIntraSDICopying = new PropertyList();
        plIntraSDICopying = newVersion ? copyDownPolicyNode.getPropertyListNotNull("intrasdicopying").getPropertyListNotNull("upversioning") : (isCopySDI ? copyDownPolicyNode.getPropertyListNotNull("intrasdicopying").getPropertyListNotNull("copyfromsdi") : copyDownPolicyNode.getPropertyListNotNull("intrasdicopying").getPropertyListNotNull("fromtemplate"));
        String copySDINotesProperty = properties.getProperty("copysdinotes", "");
        boolean copySDINotes = false;
        copySDINotes = copySDINotesProperty.length() > 0 ? "Y".equalsIgnoreCase(properties.getProperty("copysdinotes", "")) : plIntraSDICopying.getProperty("copysdinotes", "Y").equals("Y");
        if (properties.getProperty("templatepropsascolumn", "").equals("Y")) {
            templatekeyid1 = "";
            templatekeyid2 = "";
            templatekeyid3 = "";
        }
        if ((tempcopies = properties.getProperty("copies")) == null || tempcopies.length() == 0) {
            String keyvalue = properties.getProperty("keyid1");
            if (keyvalue == null || keyvalue.length() == 0) {
                keyvalue = properties.getProperty(keycolid1);
            }
            String[] temp = StringUtil.split(keyvalue, ";");
            tempcopies = String.valueOf(temp.length);
        }
        int copies = Integer.parseInt(tempcopies);
        if (sdc.getProperty("licencemax").length() > 0) {
            try {
                int count = "Y".equals(sdc.getProperty("activeableflag")) ? this.database.getCount("SELECT count(*) FROM " + tableid + " WHERE ( ACTIVEFLAG = 'Y' OR ACTIVEFLAG IS NULL )") : this.database.getCount("SELECT count(*) FROM " + tableid);
                int licensemax = Integer.parseInt(sdc.getProperty("licencemax"));
                if (count + copies > licensemax) {
                    throw new SapphireException("EXCEEDED_SDCCOUNT", "Licensed number of " + sdc.getProperty("plural") + " (" + licensemax + ") exceeded");
                }
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Failed to check number of SDI's in license check. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        boolean adduserroles = properties.getProperty("adduserroles", "Y").equals("Y");
        boolean addtemplateroles = properties.getProperty("addtemplateroles", "Y").equals("Y");
        boolean applyworkitems = properties.getProperty("applyworkitems", "N").equals("Y") && templatekeyid1.length() > 0;
        boolean isAddFromTemplate = false;
        this.logger.info("Beginning to populate the data set with " + copies + " rows");
        DataSet primaryData = new DataSet(this.connectionInfo);
        DataSet attributeData = new DataSet(this.connectionInfo);
        DataSet templatePrimary = null;
        SDIData templateData = null;
        boolean isImport = false;
        SDISnapshotItem sdiSnapshotItem = null;
        if (properties.get("sdisnapshotitem") != null && properties.get("sdisnapshotitem") instanceof SDISnapshotItem) {
            sdiSnapshotItem = (SDISnapshotItem)properties.get("sdisnapshotitem");
            properties.put("sdidata", sdiSnapshotItem.getSDIData());
            this.logger.info("Import from SDIDataSnapshot: " + sdiSnapshotItem.toString(true, true));
            isImport = true;
        }
        if (templatekeyid1 != null && templatekeyid1.length() > 0) {
            this.logger.info("Loading template " + templatekeyid1 + (templatekeyid2.length() > 0 ? "/" + templatekeyid2 : "") + (templatekeyid3.length() > 0 ? "/" + templatekeyid3 : ""));
            SDIRequest sdirequest = new SDIRequest();
            sdirequest.setSDIList(sdcid, templatekeyid1, templatekeyid2, templatekeyid3);
            sdirequest.setRequestItem("all");
            sdirequest.setExtendedDataTypes(true);
            sdirequest.setRetrieveMappedKey(false);
            if (allowdataentry) {
                sdirequest.setRequestItem("dataset");
                sdirequest.setRequestItem("dataitem[paramlistitem.defaultvalue \"__defaultvalue\"]");
            }
            if (allowAttributes) {
                sdirequest.setRequestItem("attribute");
            }
            try {
                QueryService qs = new QueryService(sapphireConnection);
                templateData = qs.getSDIData(sdirequest);
            }
            catch (ServiceException se) {
                throw new SapphireException("Failed to load template: " + templatekeyid1 + (templatekeyid2.length() > 0 ? templatekeyid2 + "/" : "") + (templatekeyid3.length() > 0 ? templatekeyid3 : ""), se);
            }
            if (templateData == null || templateData.getDataset("primary") == null || templateData.getDataset("primary").size() != 1) {
                throw new SapphireException("Failed to load template: " + templatekeyid1 + (templatekeyid2.length() > 0 ? templatekeyid2 + "/" : "") + (templatekeyid3.length() > 0 ? templatekeyid3 : ""));
            }
            templatePrimary = templateData.getDataset("primary");
            this.logger.info("Template loaded: " + templatePrimary);
            primaryData.copyRow(templatePrimary, 0, 1);
            if (templatePrimary.getValue(0, "templateflag", "N").equals("Y") || properties.getProperty("addfromtemplate", "N").equals("Y")) {
                isAddFromTemplate = true;
            }
        } else if (properties.get("sdidata") != null && properties.get("sdidata") instanceof SDIData) {
            templateData = (SDIData)properties.get("sdidata");
            templatePrimary = templateData.getDataset("primary");
            this.logger.info("Import from SDIData: " + templatePrimary);
            primaryData.copyRow(templatePrimary, 0, 1);
            isImport = true;
        } else {
            for (int i2 = 0; i2 < copies; ++i2) {
                primaryData.addRow();
            }
        }
        if (templatable && !isImport) {
            primaryData.setString(-1, "templateflag", "N");
        }
        primaryData.setString(-1, "createby", this.connectionInfo.getSysuserId());
        primaryData.setString(-1, "createtool", (isImport ? "Import/" : "") + "AddSDI");
        primaryData.setDate(-1, "createdt", now);
        primaryData.setString(-1, "modby", this.connectionInfo.getSysuserId());
        primaryData.setString(-1, "modtool", (isImport ? "Import/" : "") + "AddSDI");
        primaryData.setDate(-1, "moddt", now);
        if (!primaryData.isValidColumn("activeflag")) {
            primaryData.addColumn("activeflag", 0);
            primaryData.setString(-1, "activeflag", "Y");
        } else {
            for (int i3 = 0; i3 < primaryData.size(); ++i3) {
                if ("N".equals(primaryData.getString(i3, "activeflag"))) continue;
                primaryData.setString(i3, "activeflag", "Y");
            }
        }
        if (templatePrimary != null && !isImport) {
            primaryData.copyRow(0, copies - 1);
            if (primaryData.isValidColumn("securityuser")) {
                primaryData.setString(-1, "securityuser", "");
            }
            if (primaryData.isValidColumn("securitydepartment")) {
                primaryData.setString(-1, "securitydepartment", "");
            }
            if (primaryData.isValidColumn("versioneffectivedt")) {
                primaryData.setDate(-1, "versioneffectivedt", (Calendar)null);
            }
            if (primaryData.isValidColumn("versionapproveddt")) {
                primaryData.setDate(-1, "versionapproveddt", (Calendar)null);
            }
        }
        this.logger.info("Populate dataset with property values");
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        boolean compPrefix = compcode.length() > 0 && sdc.getProperty("componentableflag").equals("Y");
        ArrayList<String> addedStringColumns = new ArrayList<String>();
        int pkcount = 0;
        for (int sdccol = 0; sdccol < columns.size(); ++sdccol) {
            PropertyList column = columns.getPropertyList(sdccol);
            String columnid = column.getProperty("columnid").toLowerCase();
            String pkflag = column.getProperty("pkflag");
            String datatype = column.getProperty("datatype");
            String value = "";
            boolean addCompCode = false;
            if (datatype.equals("C") && primaryData.getValue(0, columnid).length() > 0) {
                addedStringColumns.add(columnid);
            }
            if (pkflag.equals("Y")) {
                if (!(autokeygen && !overrideautokey && ++pkcount <= 1 || (value = properties.getProperty("keyid" + pkcount).trim()).length() != 0 || "sdcid".equals(columnid))) {
                    value = properties.getProperty(columnid).trim();
                }
                if (compPrefix && keycolid1.equals(columnid) && !value.startsWith(compcode)) {
                    addCompCode = true;
                }
            } else {
                value = properties.getProperty(columnid);
                if (value.equalsIgnoreCase("(null)") && (columnid.equalsIgnoreCase("createby") || columnid.equalsIgnoreCase("createtool") || columnid.equalsIgnoreCase("createdt") || columnid.equalsIgnoreCase("modby") || columnid.equalsIgnoreCase("modtool") || columnid.equalsIgnoreCase("moddt"))) {
                    value = "";
                }
            }
            long length = StringUtil.getLen(value);
            if (columnid.equals("versionstatus") && (length == 0L || "(null)".equalsIgnoreCase(value))) {
                value = "P";
                length = 1L;
            }
            if (length <= 0L) continue;
            if (datatype.equals("C")) {
                if (!addedStringColumns.contains(columnid)) {
                    addedStringColumns.add(columnid);
                }
                primaryData.addColumn(columnid, 0);
            } else if (datatype.equals("T") || datatype.equals("B")) {
                primaryData.addColumn(columnid, 3);
            } else if (datatype.equals("N") || datatype.equals("R")) {
                primaryData.addColumn(columnid, 1);
            } else if (datatype.equals("D")) {
                primaryData.addColumn(columnid, 2);
                if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "timezoneindependent"))) {
                    primaryData.setTimeZoneInsensitive(columnid);
                }
            }
            if (copies > 1 && value.indexOf(";") > -1) {
                String[] splitvalue = StringUtil.split(value, ";");
                int values = splitvalue.length;
                for (int i4 = 0; i4 < values && i4 < copies; ++i4) {
                    if (splitvalue[i4].equalsIgnoreCase("(null)")) {
                        splitvalue[i4] = "";
                    }
                    if (splitvalue[i4].length() <= 0) continue;
                    if ("Y".equals(column.getProperty("timezoneindependent"))) {
                        primaryData.setDate(i4, columnid, m18n.parseCalendar(splitvalue[i4].trim(), false));
                        continue;
                    }
                    primaryData.setValue(i4, columnid, (addCompCode && splitvalue[i4].trim().length() > 0 ? compcode + "_" : "") + splitvalue[i4].trim());
                }
                continue;
            }
            for (int i5 = 0; i5 < copies; ++i5) {
                if (value.equalsIgnoreCase("(null)")) {
                    value = "";
                }
                primaryData.setValue(i5, columnid, (addCompCode && value.trim().length() > 0 ? compcode + "_" : "") + value.trim());
            }
        }
        if (primaryData.isValidColumn("uuid")) {
            primaryData.setValue(-1, "uuid", "");
        }
        if (isCopySDI && sdc_versioned) {
            StringBuffer sqlVersion = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            String versionColumnSql = this.connectionInfo.isOracle() ? "  to_number( " + keycolid2 + " ) version" : "  cast( " + keycolid2 + " AS Integer ) version";
            for (int i6 = 0; i6 < primaryData.getRowCount(); ++i6) {
                int version;
                BaseSDCRules[] keyid12 = primaryData.getValue(i6, keycolid1);
                String keyid2 = primaryData.getValue(i6, keycolid2);
                String keyid3 = primaryData.getValue(i6, keycolid3);
                safeSQL.reset();
                sqlVersion.setLength(0);
                sqlVersion.append("SELECT ").append(versionColumnSql).append(" FROM ").append(tableid).append(" WHERE ").append(keycolid1).append(" = ").append(safeSQL.addVar(keyid12));
                sqlVersion.append(keycolid3.length() > 0 ? " AND " + keycolid3 + " = " + safeSQL.addVar(keyid3) : "").append(" order by version");
                this.database.createPreparedResultSet("getversions", sqlVersion.toString(), safeSQL.getValues());
                DataSet ds = new DataSet(this.database.getResultSet("getversions"));
                int intKeyid2 = new Integer(keyid2);
                for (int v = 0; v < ds.getRowCount() && intKeyid2 != (version = ds.getInt(v, "version")); ++v) {
                    if (intKeyid2 >= version) continue;
                    String errMsg = "A higher version of " + sdcid + " " + (String)keyid12;
                    errMsg = errMsg + (keycolid3.length() > 0 ? ", " + keycolid3 + " " + keyid3 : "");
                    errMsg = errMsg + " exists - choose another name.";
                    this.getErrorHandler().add(sdcid, "AddSDI", "CheckForHigherVersionExistence", "VALIDATION", errMsg);
                }
            }
        }
        if (sdc.getProperty("componentableflag").equals("Y")) {
            primaryData.addColumn("compcode", 0);
            primaryData.setValue(-1, "compcode", compPrefix ? compcode : null);
        }
        if (departmentlevel && !isImport) {
            boolean alwaysCopyTemplateDepartment = false;
            alwaysCopyTemplateDepartment = plIntraSDICopying.getProperty("copysecuritydepartment", "Y").equals("Y");
            boolean useTemplateDepartment = "Y".equals(properties.getProperty("usetemplatedepartment")) && templatePrimary.getString(0, "securitydepartment") != null && templatePrimary.getString(0, "securitydepartment").length() > 0;
            boolean doNotUseTemplateDepartment = "N".equals(properties.getProperty("usetemplatedepartment")) || templatePrimary == null || templatePrimary.getString(0, "securitydepartment") == null || templatePrimary.getString(0, "securitydepartment").length() == 0;
            for (int i7 = 0; i7 < primaryData.getRowCount(); ++i7) {
                if (primaryData.getString(i7, "securityuser") == null || primaryData.getString(i7, "securityuser").length() == 0) {
                    primaryData.setString(i7, "securityuser", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                }
                if (primaryData.getString(i7, "securitydepartment") != null && primaryData.getString(i7, "securitydepartment").length() != 0) continue;
                if (alwaysCopyTemplateDepartment && !doNotUseTemplateDepartment || !alwaysCopyTemplateDepartment && useTemplateDepartment) {
                    primaryData.setString(i7, "securitydepartment", templatePrimary.getString(0, "securitydepartment"));
                    continue;
                }
                primaryData.setString(i7, "securitydepartment", this.connectionInfo.getDefaultDepartment());
            }
        }
        if (rolelevel && adduserroles) {
            try {
                if (templateData == null) {
                    templateData = new SDIData();
                    this.database.createResultSet("sdirole", "select * from sdirole where 1 = 2");
                    templateData.setDataset("role", this.database.getResultSet("sdirole"));
                    this.database.closeResultSet("sdirole");
                } else if (!addtemplateroles) {
                    DataSet sdirole = templateData.getDataset("role");
                    sdirole.reset();
                }
                this.database.createPreparedResultSet("userroles", "select roleid from sysuserrole where sysuserid = ?", new Object[]{this.connectionInfo.getSysuserId()});
                DataSet sdirole = templateData.getDataset("role");
                while (this.database.getNext("userroles")) {
                    if (sdirole.findRow("roleid", this.database.getString("userroles", "roleid")) >= 0) continue;
                    int newrow = sdirole.addRow();
                    sdirole.setString(newrow, "sdcid", sdcid);
                    sdirole.setString(newrow, "roleid", this.database.getString("userroles", "roleid"));
                    sdirole.setString(newrow, "privid", "list");
                    sdirole.setNumber(newrow, "usersequence", newrow);
                }
                this.database.closeResultSet("userroles");
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to retrieve role details", e);
            }
        }
        if (templateData != null && !copySDINotes && (notes = templateData.getDataset("notes")) != null && notes.getRowCount() > 0) {
            templateData.removeDataset("notes");
        }
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid);
        boolean isTriggerSDCRule = isImport ? cmtPolicy.isTriggerBusinessRule() : true;
        HashMap<String, ArrayList<PropertyList>> copyDownPolicy = null;
        if (!isImport && (properties.getProperty("forcecopydownattributes", "N").equals("Y") || !isAddTemplate && (isAddFromTemplate || templatekeyid1.length() == 0)) && (copyDownPolicy = AddSDI.getCopyDownPolicy(addedStringColumns, sdc, new String[]{"eventplans", "attributes", "columnvalues", "attachments", "securityset", "sdidetails", "maskinglevel"}, this.getConfigurationProcessor())).containsKey("columnvalues") && copyDownPolicy.get("columnvalues").size() > 0) {
            SdiInfo.copyDownColumns(sdc, primaryData, null, copyDownPolicy.get("columnvalues"), this.getSDCProcessor(), this.getSDIProcessor());
        }
        if (isTriggerSDCRule) {
            BaseSDCRules sdcPreAddKeyRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PreAddKey");
            sdcPreAddKeyRules.setCMTImport(isImport);
            Trace.startBusinessRule(sdcid + "." + "PreAddKey", true);
            sdcPreAddKeyRules.preAddKey(primaryData, properties);
            Trace.endBusinessRule(sdcid + "." + "PreAddKey", true);
            Trace.startBusinessRule(sdcid + "." + "PreAddKey", false);
            for (BaseSDCRules customRules : sdcPreAddKeyRules.getCustomRuleList()) {
                customRules.preAddKey(primaryData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreAddKey", false);
            sdcPreAddKeyRules.endRule();
        }
        if (!overrideautokey) {
            overrideautokey = properties.getProperty("overrideautokey").equals("Y");
        }
        if (autokeygen && !overrideautokey && !keycolid1.equals("")) {
            this.logger.info("Beginning generate keys");
            SequenceService seqService = new SequenceService(sapphireConnection);
            try {
                seqService.generateKeys(sdcid, keycolid1, keygenerationrule, primaryData);
            }
            catch (ServiceException e) {
                throw new SapphireException("KEYGENERATOR_FAILED", "Failed to generate keys", e);
            }
            this.logger.info("Auto key generation complete");
        }
        if (isTriggerSDCRule) {
            BaseSDCRules sdcPostKeyRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PostAddKey");
            sdcPostKeyRules.setCMTImport(isImport);
            Trace.startBusinessRule(sdcid + "." + "PostAddKey", true);
            sdcPostKeyRules.postAddKey(primaryData, properties);
            Trace.endBusinessRule(sdcid + "." + "PostAddKey", true);
            Trace.startBusinessRule(sdcid + "." + "PostAddKey", false);
            for (BaseSDCRules customRules : sdcPostKeyRules.getCustomRuleList()) {
                customRules.postAddKey(primaryData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostAddKey", false);
            sdcPostKeyRules.endRule();
        }
        if (!auditflag.equalsIgnoreCase("N")) {
            primaryData.addColumn("tracelogid", 0);
            if (properties.getProperty("tracelogid", "").trim().length() == 0) {
                String reason = properties.getProperty("auditreason");
                String activity = properties.getProperty("auditactivity");
                String signedflag = properties.getProperty("auditsignedflag");
                String auditdt = properties.getProperty("auditdt");
                if (reason != null && reason.length() > 0) {
                    keyid1 = "";
                    String keyid2 = "";
                    String keyid3 = "";
                    for (int i8 = 0; i8 < primaryData.getRowCount(); ++i8) {
                        keyid1 = keyid1 + ";" + primaryData.getString(i8, keycolid1);
                        keyid2 = keycolid2 != null && keycolid2.length() > 0 ? keyid2 + ";" + primaryData.getString(i8, keycolid2) : keyid2 + ";(null)";
                        keyid3 = keycolid3 != null && keycolid3.length() > 0 ? keyid3 + ";" + primaryData.getString(i8, keycolid3) : keyid3 + ";(null)";
                    }
                    this.logger.info("Generate the tracelog records");
                    String standard = "Y";
                    if (!promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S")) {
                        standard = "N";
                    }
                    AuditService audit = new AuditService(sapphireConnection);
                    try {
                        int tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(sdcid, keyid1.substring(1), keyid2.substring(1), keyid3.substring(1), reason, activity, signedflag, auditdt, "Data adding", standard.equals("Y")));
                        properties.setProperty("tracelogid", String.valueOf(tracelogid));
                        for (i = 0; i < copies; ++i) {
                            primaryData.setString(i, "tracelogid", String.valueOf(tracelogid + i));
                        }
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("Failed to add audit records", e);
                    }
                } else {
                    primaryData.setString(-1, "tracelogid", null);
                }
            } else {
                primaryData.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
            }
        }
        SDIData sdiData = new SDIData();
        HashMap<String, ArrayList<String>> skippedAttributes = new HashMap<String, ArrayList<String>>();
        if (!isImport) {
            if (properties.getProperty("forcecopydownattributes", "N").equals("Y") || !isAddTemplate && (isAddFromTemplate || templatekeyid1.length() == 0)) {
                if (copyDownPolicy == null) {
                    copyDownPolicy = AddSDI.getCopyDownPolicy(addedStringColumns, sdc, new String[]{"eventplans", "attributes", "columnvalues", "attachments", "securityset", "sdidetails", "maskinglevel"}, this.getConfigurationProcessor());
                }
                if (copyDownPolicy.containsKey("eventplans") && ((ArrayList)copyDownPolicy.get("eventplans")).size() > 0) {
                    DataSet eventplanData = new DataSet(this.connectionInfo);
                    DataSet eventplanitemData = new DataSet(this.connectionInfo);
                    DataSet eventplanitempropertyData = new DataSet(this.connectionInfo);
                    AddSDIEventPlan.copyDownEventPlans(eventplanData, eventplanitemData, eventplanitempropertyData, primaryData, null, sdc, copyDownPolicy.get("eventplans"), this.database, this.connectionInfo, this.logger);
                    sdiData.setDataset("sdieventplan", eventplanData);
                    sdiData.setDataset("sdieventplanitem", eventplanitemData);
                    sdiData.setDataset("sdieventplanitemproperty", eventplanitempropertyData);
                }
                if ((sdiSecurityEnabled || departmentlevel) && copyDownPolicy.containsKey("securityset") && copyDownPolicy.get("securityset").size() > 0) {
                    SDISecurity.copyDownSecuritySet(sdc, primaryData, null, copyDownPolicy.get("securityset"), this.getSDCProcessor(), this.getSDIProcessor(), this.getQueryProcessor(), this.getActionProcessor());
                }
                if (maskableFlag && copyDownPolicy.containsKey("maskinglevel") && copyDownPolicy.get("maskinglevel").size() > 0) {
                    DataMaskUtil.copyDownMaskingLevel(sdcid, primaryData, null, copyDownPolicy, this.getSDCProcessor(), this.getSDIProcessor());
                }
                if (allowAttributes) {
                    if (!properties.getProperty("excludeattribute").equals("Y") && (properties.getProperty("forcecopydownattributes", "N").equals("Y") || isAddFromTemplate) && templateData != null && templateData.getDataset("attribute") != null) {
                        AddSDIAttribute.templateCopyDownAttibutes(attributeData, new DataSet(), primaryData, templateData.getDataset("attribute"), sdc, keycolid1, keycolid2, keycolid3, true, true, skippedAttributes, "", this.getQueryProcessor(), m18n, this.logger);
                    } else {
                        this.logger.debug("Template attributes not available for current sdi.");
                    }
                    String[] addedStringCols = new String[addedStringColumns.size()];
                    addedStringColumns.toArray(addedStringCols);
                    AddSDIAttribute.copyDownAttributes(attributeData, new DataSet(), primaryData, null, addedStringCols, sdc, keycolid1, keycolid2, keycolid3, copyDownPolicy.get("attributes"), skippedAttributes, "", this.getQueryProcessor(), this.getSDIProcessor(), m18n, this.getConfigurationProcessor(), this.getSDCProcessor(), this.getConnectionProcessor(), this.logger);
                } else {
                    this.logger.debug("Attributes not available for current sdi.");
                }
            } else if (!properties.getProperty("excludeattribute").equals("Y") && (properties.getProperty("forcecopyattributes", "N").equals("Y") || !isAddTemplate && !isAddFromTemplate && templatekeyid1.length() > 0 || isAddFromTemplate && isAddTemplate && templatekeyid1.length() > 0)) {
                if (allowAttributes && templateData != null && templateData.getDataset("attribute") != null) {
                    AddSDIAttribute.templateCopyDownAttibutes(attributeData, new DataSet(), primaryData, templateData.getDataset("attribute"), sdc, keycolid1, keycolid2, keycolid3, true, false, skippedAttributes, "", this.getQueryProcessor(), m18n, this.logger);
                } else {
                    this.logger.debug("Attributes not available for current sdi or Template attributes not available for current sdi.");
                }
            } else if (!properties.getProperty("excludeattribute").equals("Y") && isAddTemplate && !isAddFromTemplate) {
                if (allowAttributes && templateData != null && templateData.getDataset("attribute") != null) {
                    AddSDIAttribute.copyAdocToTemplateAttibutes(attributeData, new DataSet(), primaryData, templateData.getDataset("attribute"), sdc, keycolid1, keycolid2, keycolid3, skippedAttributes, "", this.getQueryProcessor(), m18n, this.logger);
                } else {
                    this.logger.debug("Attributes not available for current sdi.");
                }
            }
            if (sdiSecurityEnabled) {
                SDISecurity.setDefaultSecuritySet(primaryData, this.database, sdcid);
            }
            attributeData.setString(-1, "sdiattributeid", "");
            sdiData.setDataset("attribute", attributeData);
        }
        sdiData.setDataset("primary", primaryData);
        PropertyList addToWorkflowProps = null;
        if (templateData != null) {
            int i9;
            this.logger.info("Adding non-primary template data");
            String[] dsnames = SDIData.getDatasetNames();
            String[] dstables = SDIData.getDatasetTables();
            String[] linktables = templateData.getLinkTables();
            String[] detaillinktables = templateData.getDetailLinkTables();
            ArrayList<String> datasetnames = new ArrayList<String>();
            ArrayList<String> datasettables = new ArrayList<String>();
            for (i9 = 0; i9 < dsnames.length; ++i9) {
                datasetnames.add(dsnames[i9]);
                datasettables.add(dstables[i9]);
            }
            if (linktables != null) {
                for (i9 = 0; i9 < linktables.length; ++i9) {
                    if (datasettables.contains(linktables[i9])) continue;
                    datasetnames.add(linktables[i9]);
                    datasettables.add(linktables[i9]);
                }
            }
            if (detaillinktables != null) {
                for (i9 = 0; i9 < detaillinktables.length; ++i9) {
                    if (datasettables.contains(detaillinktables[i9])) continue;
                    datasetnames.add(detaillinktables[i9]);
                    datasettables.add(detaillinktables[i9]);
                }
            }
            if (isImport) {
                datasetnames.add("s_sdicertification");
                datasettables.add("s_sdicertification");
            }
            if (!(isImport || rolelevel && (addtemplateroles || adduserroles))) {
                properties.setProperty("excluderole", "Y");
            }
            if (!newVersion && !isImport) {
                properties.setProperty("excludesdialias", "Y");
            }
            if (templatekeyid1 != null && templatekeyid1.length() > 0 && primaryData.findRow(keycolid1, templatekeyid1) >= 0) {
                DataSet categoryitems;
                DataSet sdirole = templateData.getDataset("role");
                if (sdirole != null) {
                    sdirole.reset();
                }
                if ((categoryitems = templateData.getDataset("category")) != null) {
                    categoryitems.reset();
                }
            }
            ArrayList<String> arrApprovalTypeId = new ArrayList<String>();
            for (i = 0; i < datasetnames.size(); ++i) {
                int c;
                DataSet templatedataset;
                String datasetName = (String)datasetnames.get(i);
                String datasetTable = (String)datasettables.get(i);
                if (datasetName == null || datasetName.equals("primary") || !properties.getProperty("forcesimplecopyattributes", "N").equals("Y") && datasetName.equals("attribute") || datasetName.equals("document") || properties.getProperty("exclude" + datasetName).equals("Y")) continue;
                this.logger.info("Processing template dataset: " + datasetName + ", table: " + datasetTable);
                DataSet dataset = sdiData.getDataset(datasetName);
                if (dataset == null) {
                    dataset = new DataSet();
                }
                if ((templatedataset = templateData.getDataset(datasetName)) == null) continue;
                int rows = primaryData.getRowCount();
                String[] sdiworkitemPseudokeys = null;
                int sdiworkitemPseudokeysCount = 0;
                String[] sdidataPseudokeys = null;
                int sdidataPseudokeysCount = 0;
                if (datasetName.equals("sdiworkitem")) {
                    boolean bl = applyworkitems = applyworkitems && templatedataset.size() > 0;
                    if (templatedataset != null && templatedataset.getRowCount() > 0) {
                        templatedataset.addColumn("__sdiworkitemid", 0);
                        templatedataset.addColumn("__oldsdiworkitemid", 0);
                        c = rows * templatedataset.getRowCount();
                        String[] stringArray = sdiworkitemPseudokeys = rows > 0 ? ((DBUtil)this.database).getUUIDList(c) : new String[]{};
                        if (sdiworkitemPseudokeys == null || sdiworkitemPseudokeys.length != c) {
                            throw new SapphireException("Unable to generate sdiworkitemid.");
                        }
                    }
                }
                if (datasetName.equals("dataset") && templatedataset != null && templatedataset.getRowCount() > 0) {
                    templatedataset.addColumn("__sdidataid", 0);
                    templatedataset.addColumn("__oldsdidataid", 0);
                    c = rows * templatedataset.getRowCount();
                    String[] stringArray = sdidataPseudokeys = rows > 0 ? ((DBUtil)this.database).getUUIDList(c) : new String[]{};
                    if (sdidataPseudokeys == null || sdidataPseudokeys.length != c) {
                        throw new SapphireException("Unable to generate sdidataid.");
                    }
                }
                if (datasetName.equals("dataitem")) {
                    if (isImport) {
                        templatedataset.renameColumn("sdidataitemid", "__oldsdidataitemid");
                    }
                    for (int j = 0; j < templatedataset.getRowCount(); ++j) {
                        if (!templatedataset.isValidColumn("__defaultvalue")) continue;
                        templatedataset.setString(j, "defaultvalue", templatedataset.getString(j, "__defaultvalue"));
                    }
                }
                String[] linkTableKeys = templateData.getLinkTableKeys(datasetTable);
                String[] detailLinkTableKeys = templateData.getDetailLinkTableKeys(datasetTable);
                for (int row = 0; row < rows; ++row) {
                    String newkey;
                    int rowCount;
                    int j;
                    int j2;
                    if (datasetName.equals("sdiworkflowrule") && !newVersion && !isCopySDI) {
                        if (templatedataset.size() <= 0) continue;
                        if (addToWorkflowProps == null) {
                            addToWorkflowProps = new PropertyList();
                            addToWorkflowProps.setProperty("sdcid", sdcid);
                        }
                        addToWorkflowProps.setProperty("keyid1", row == 0 ? primaryData.getString(row, keycolid1) : addToWorkflowProps.getProperty("keyid1") + ";" + primaryData.getString(row, keycolid1));
                        if (keycolid2.length() > 0) {
                            addToWorkflowProps.setProperty("keyid2", row == 0 ? primaryData.getString(row, keycolid2) : addToWorkflowProps.getProperty("keyid2") + ";" + primaryData.getString(row, keycolid2));
                        }
                        if (keycolid3.length() > 0) {
                            addToWorkflowProps.setProperty("keyid3", row == 0 ? primaryData.getString(row, keycolid3) : addToWorkflowProps.getProperty("keyid3") + ";" + primaryData.getString(row, keycolid3));
                        }
                        for (j2 = 0; j2 < templatedataset.getRowCount(); ++j2) {
                            addToWorkflowProps.setProperty("workflowdefid", j2 == 0 ? templatedataset.getString(j2, "workflowdefid") : addToWorkflowProps.getProperty("workflowdefid") + ";" + templatedataset.getString(j2, "workflowdefid"));
                            addToWorkflowProps.setProperty("workflowdefversionid", j2 == 0 ? templatedataset.getString(j2, "workflowdefversionid") : addToWorkflowProps.getProperty("workflowdefversionid") + ";" + templatedataset.getString(j2, "workflowdefversionid"));
                            addToWorkflowProps.setProperty("workflowdefvariantid", j2 == 0 ? templatedataset.getString(j2, "workflowdefvariantid") : addToWorkflowProps.getProperty("workflowdefvariantid") + ";" + templatedataset.getString(j2, "workflowdefvariantid"));
                            addToWorkflowProps.setProperty("workflowexecid", j2 == 0 ? templatedataset.getString(j2, "workflowexecid") : addToWorkflowProps.getProperty("workflowexecid") + ";" + templatedataset.getString(j2, "workflowexecid"));
                            addToWorkflowProps.setProperty("taskdefitemid", j2 == 0 ? templatedataset.getString(j2, "taskdefitemid") : addToWorkflowProps.getProperty("taskdefitemid") + ";" + templatedataset.getString(j2, "taskdefitemid"));
                            addToWorkflowProps.setProperty("ioitemid", j2 == 0 ? templatedataset.getString(j2, "ioitemid") : addToWorkflowProps.getProperty("ioitemid") + ";" + templatedataset.getString(j2, "ioitemid"));
                        }
                        continue;
                    }
                    if (datasetName.equals("pricelistitem")) {
                        for (j2 = 0; j2 < templatedataset.getRowCount(); ++j2) {
                            templatedataset.setString(j2, "pricelistid", primaryData.getString(row, keycolid1));
                            templatedataset.setString(j2, "pricelistitemid", String.valueOf(j2));
                        }
                    } else if (datasetName.equals("chargelistitem")) {
                        for (j2 = 0; j2 < templatedataset.getRowCount(); ++j2) {
                            templatedataset.setString(j2, "chargelistid", primaryData.getString(row, keycolid1));
                            templatedataset.setString(j2, "chargelistitemid", String.valueOf(j2));
                        }
                    } else if (datasetName.equals("workgroupitem") || datasetName.equals("workgroupparamlist")) {
                        for (j2 = 0; j2 < templatedataset.getRowCount(); ++j2) {
                            templatedataset.setString(j2, "workgroupid", primaryData.getString(row, keycolid1));
                        }
                    } else if (datasetName.equals("role") || datasetName.equals("category")) {
                        if (StringUtil.getLen(keycolid1) > 0L) {
                            String keyid13 = primaryData.getString(row, keycolid1);
                            templatedataset.setString(-1, "keyid1", keyid13);
                        }
                    } else if (datasetName.equals("approval")) {
                        if (StringUtil.getLen(keycolid1) > 0L) {
                            templatedataset.setString(-1, "keyid1", primaryData.getString(row, keycolid1));
                        }
                        if (StringUtil.getLen(keycolid2) > 0L) {
                            templatedataset.setString(-1, "keyid2", primaryData.getString(row, keycolid2));
                        }
                        if (StringUtil.getLen(keycolid3) > 0L) {
                            templatedataset.setString(-1, "keyid3", primaryData.getString(row, keycolid3));
                        }
                        if (strVersionApprType.length() > 0) {
                            for (j = rowCount = templatedataset.getRowCount(); j >= 0; --j) {
                                String approvalFunction = templatedataset.getValue(j, "approvalfunction");
                                if (!"Versioned".equals(approvalFunction)) continue;
                                arrApprovalTypeId.add(templatedataset.getValue(j, "approvaltypeid"));
                                templatedataset.deleteRow(j);
                            }
                        }
                    } else if (datasetName.equals("approvalstep")) {
                        if (StringUtil.getLen(keycolid1) > 0L) {
                            templatedataset.setString(-1, "keyid1", primaryData.getString(row, keycolid1));
                        }
                        if (StringUtil.getLen(keycolid2) > 0L) {
                            templatedataset.setString(-1, "keyid2", primaryData.getString(row, keycolid2));
                        }
                        if (StringUtil.getLen(keycolid3) > 0L) {
                            templatedataset.setString(-1, "keyid3", primaryData.getString(row, keycolid3));
                        }
                        if (strVersionApprType.length() > 0) {
                            for (j = rowCount = templatedataset.getRowCount(); j >= 0; --j) {
                                String approvalTypeId = templatedataset.getValue(j, "approvaltypeid");
                                if (!arrApprovalTypeId.contains(approvalTypeId)) continue;
                                templatedataset.deleteRow(j);
                            }
                        }
                    } else if (!datasetName.equals("s_sdicertification")) {
                        if (linkTableKeys != null && linkTableKeys.length > 0) {
                            for (j2 = 0; j2 < linkTableKeys.length; ++j2) {
                                if (StringUtil.getLen(keycolid1) > 0L && linkTableKeys[j2].equals(keycolid1)) {
                                    templatedataset.setString(-1, linkTableKeys[j2], primaryData.getString(row, keycolid1));
                                    continue;
                                }
                                if (StringUtil.getLen(keycolid2) > 0L && linkTableKeys[j2].equals(keycolid2)) {
                                    templatedataset.setString(-1, linkTableKeys[j2], primaryData.getString(row, keycolid2));
                                    continue;
                                }
                                if (StringUtil.getLen(keycolid3) <= 0L || !linkTableKeys[j2].equals(keycolid3)) continue;
                                templatedataset.setString(-1, linkTableKeys[j2], primaryData.getString(row, keycolid3));
                            }
                        } else if (detailLinkTableKeys != null && detailLinkTableKeys.length > 0) {
                            for (j2 = 0; j2 < detailLinkTableKeys.length; ++j2) {
                                if (StringUtil.getLen(keycolid1) > 0L && detailLinkTableKeys[j2].equals(keycolid1)) {
                                    templatedataset.setString(-1, detailLinkTableKeys[j2], primaryData.getString(row, keycolid1));
                                    continue;
                                }
                                if (StringUtil.getLen(keycolid2) > 0L && detailLinkTableKeys[j2].equals(keycolid2)) {
                                    templatedataset.setString(-1, detailLinkTableKeys[j2], primaryData.getString(row, keycolid2));
                                    continue;
                                }
                                if (StringUtil.getLen(keycolid3) <= 0L || !detailLinkTableKeys[j2].equals(keycolid3)) continue;
                                templatedataset.setString(-1, detailLinkTableKeys[j2], primaryData.getString(row, keycolid3));
                            }
                        } else {
                            if (StringUtil.getLen(keycolid1) > 0L) {
                                templatedataset.setString(-1, "keyid1", primaryData.getString(row, keycolid1));
                            }
                            if (StringUtil.getLen(keycolid2) > 0L) {
                                templatedataset.setString(-1, "keyid2", primaryData.getString(row, keycolid2));
                            }
                            if (StringUtil.getLen(keycolid3) > 0L) {
                                templatedataset.setString(-1, "keyid3", primaryData.getString(row, keycolid3));
                            }
                        }
                    }
                    if (datasetName.equals("sdiworkitem") && templatedataset != null && templatedataset.getRowCount() > 0 && sdiworkitemPseudokeys != null && sdiworkitemPseudokeys.length > 0) {
                        for (int t = 0; t < templatedataset.getRowCount(); ++t) {
                            String sdiworkitemid = templatedataset.getValue(t, "sdiworkitemid", "x");
                            newkey = sdiworkitemPseudokeys[sdiworkitemPseudokeysCount];
                            ++sdiworkitemPseudokeysCount;
                            templatedataset.setValue(t, "__sdiworkitemid", newkey);
                            templatedataset.setValue(t, "__oldsdiworkitemid", sdiworkitemid);
                        }
                    }
                    if (datasetName.equals("dataset") && templatedataset != null && templatedataset.getRowCount() > 0 && sdidataPseudokeys != null && sdidataPseudokeys.length > 0) {
                        for (int t = 0; t < templatedataset.getRowCount(); ++t) {
                            String sdidataid = templatedataset.getValue(t, "sdidataid", "x");
                            newkey = sdidataPseudokeys[sdidataPseudokeysCount];
                            ++sdidataPseudokeysCount;
                            templatedataset.setValue(t, "__sdidataid", newkey);
                            templatedataset.setValue(t, "__oldsdidataid", sdidataid);
                        }
                    }
                    dataset.copyRow(templatedataset, -1, 1);
                }
                if (datasetName.equals("sdispec")) {
                    int k;
                    boolean applySpec = plIntraSDICopying.getProperty("specapplyflag", "Use Auto Apply Flag").equalsIgnoreCase("Use Auto Apply Flag");
                    if (applySpec) {
                        for (k = 0; k < dataset.size(); ++k) {
                            String autoapplyFlag = dataset.getValue(k, "autoapplyflag");
                            if (!autoapplyFlag.equals("N")) {
                                dataset.setString(k, "appliedflag", "Y");
                                continue;
                            }
                            dataset.setString(k, "appliedflag", "N");
                        }
                    }
                    if (!(isAddTemplate || newVersion || isCopySDI)) {
                        for (k = 0; k < dataset.size(); ++k) {
                            String specid = dataset.getValue(k, "specid");
                            String specversion = dataset.getValue(k, "specversionid", "C");
                            if (!"C".equalsIgnoreCase(specversion)) continue;
                            String sql = "SELECT specversionid FROM spec WHERE specid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (specversionid as numeric) desc";
                            this.database.createPreparedResultSet("CurrentSpecVersion", sql, new Object[]{specid});
                            if (!this.database.getNext("CurrentSpecVersion")) continue;
                            specversion = this.database.getString("CurrentSpecVersion", "specversionid");
                            dataset.setString(k, "specversionid", specversion);
                        }
                    }
                }
                sdiData.setDataset(datasetName, dataset);
                sdiData.sanitizeDataset(datasetName, this.connectionInfo.getSysuserId(), "AddSDI", now, properties);
                if (datasetName.equals("sdiworkitem") && dataset != null && dataset.getRowCount() > 0) {
                    dataset.addColumnValues("sdiworkitemid", 0, dataset.getColumnValues("__sdiworkitemid", ";"), ";");
                }
                if (!datasetName.equals("dataset") || dataset == null || dataset.getRowCount() <= 0) continue;
                dataset.addColumnValues("sdidataid", 0, dataset.getColumnValues("__sdidataid", ";"), ";");
            }
        }
        for (int i10 = 0; i10 < primaryData.size(); ++i10) {
            keyid1 = primaryData.getValue(i10, keycolid1);
            if (newVersion || AddSDI.isValidKey(keyid1)) continue;
            throw new SapphireException("You have used an illegal character or name trying to create '" + keyid1 + "'");
        }
        if (!isImport) {
            DataSet sdidatadataset;
            if (allowAttributes && sdc.getCollection("attributes") != null && sdc.getCollection("attributes").size() > 0) {
                int rows = primaryData.getRowCount();
                for (int row = 0; row < rows; ++row) {
                    String attkeyid1 = primaryData.getString(row, keycolid1);
                    String attkeyid2 = primaryData.getString(row, keycolid2);
                    String attkeyid3 = primaryData.getString(row, keycolid3);
                    AddSDIAttribute.createAttributeData(attributeData, new DataSet(), sdc, attkeyid1, attkeyid2 == null || attkeyid2.length() == 0 ? "" : attkeyid2, attkeyid3 == null || attkeyid3.length() == 0 ? "" : attkeyid3, "", "", BaseSDIAttributeAction.AttributeType.sdc, "", "", "", "", "", "", "", "", "", "", "", "", skippedAttributes, isAddTemplate, "", ";", sdcProcessor, this.getQueryProcessor(), m18n, this.connectionInfo, this.logger);
                }
                this.logger.debug("Attribute data updated.");
            } else {
                this.logger.debug("SDC is not attributable or does not have any attributes defined.");
            }
            if (templateData != null && (sdidatadataset = sdiData.getDataset("dataset")) != null && sdidatadataset.size() > 0) {
                DataSet sdidata_attributes = new DataSet();
                String curr_sdidataid = sdidatadataset.getColumnValues("__oldsdidataid", "','");
                DataSet currentsdidata_attributes = this.getQueryProcessor().getSqlDataSet("SELECT s.* FROM sdiattribute s WHERE s.keyid1 IN ('" + curr_sdidataid + "')  AND s.sdcid = 'DataSet'");
                if (currentsdidata_attributes.getRowCount() > 0) {
                    for (int sdiw_row = 0; sdiw_row < sdidatadataset.getRowCount(); ++sdiw_row) {
                        String oldkey = sdidatadataset.getValue(sdiw_row, "__oldsdidataid");
                        String newkey = sdidatadataset.getValue(sdiw_row, "sdidataid");
                        HashMap<String, String> currentKeyMap = new HashMap<String, String>();
                        currentKeyMap.put("keyid1", oldkey);
                        DataSet dsCurrentKeyattributes = currentsdidata_attributes.getFilteredDataSet(currentKeyMap);
                        if (dsCurrentKeyattributes.getRowCount() <= 0) continue;
                        DataSet temp = new DataSet();
                        temp.copyRow(dsCurrentKeyattributes, -1, 1);
                        temp.setValue(-1, "keyid1", newkey);
                        temp.setValue(-1, "sdiattributeid", "");
                        sdidata_attributes.copyRow(temp, -1, 1);
                    }
                    sdidata_attributes.setString(-1, "createby", this.connectionInfo.getSysuserId());
                    sdidata_attributes.setString(-1, "createtool", "AddSDI");
                    sdidata_attributes.setDate(-1, "createdt", now);
                    sdidata_attributes.setString(-1, "modby", this.connectionInfo.getSysuserId());
                    sdidata_attributes.setString(-1, "modtool", "AddSDI");
                    sdidata_attributes.setDate(-1, "moddt", now);
                    sdiData.setDataset("sdidata_sdiattribute", sdidata_attributes);
                }
            }
            if (!attributeData.isValidColumn("usersequence")) {
                attributeData.addColumn("usersequence", 1);
            } else {
                attributeData.sort("usersequence");
            }
            for (int ar = 0; ar < attributeData.getRowCount(); ++ar) {
                attributeData.setString(ar, "createby", this.connectionInfo.getSysuserId());
                attributeData.setString(ar, "createtool", "AddSDI");
                attributeData.setDate(ar, "createdt", now);
                attributeData.setString(ar, "modby", this.connectionInfo.getSysuserId());
                attributeData.setString(ar, "modtool", "AddSDI");
                attributeData.setDate(ar, "moddt", now);
                attributeData.setNumber(ar, "usersequence", ar + 1);
                if (properties.getProperty("setattributedefaults").equals("Y")) {
                    attributeData.setString(ar, "defaulttextvalue", attributeData.getString(ar, "textvalue", ""));
                    attributeData.setNumber(ar, "defaultnumericvalue", attributeData.getBigDecimal(ar, "numericvalue"));
                    attributeData.setDate(ar, "defaultdatevalue", attributeData.getCalendar(ar, "datevalue"));
                    attributeData.setClob(ar, "defaultclobvalue", attributeData.getClob(ar, "clobvalue", ""));
                }
                if (!properties.getProperty("clearattributevalues").equals("Y")) continue;
                attributeData.setString(ar, "textvalue", "");
                attributeData.setNumber(ar, "numericvalue", "(null)");
                attributeData.setDate(ar, "datevalue", "(null)");
                attributeData.setString(ar, "clobvalue", "");
            }
            AddSDIAttribute.logSkipped(skippedAttributes, sdcid, this.logger);
        }
        if (strVersionApprType.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT approvaltypeid, sequenceflag, passrule, uniquenessflag, extendableflag FROM approvaltype  WHERE approvaltypeid=" + safeSQL.addVar(strVersionApprType);
            DataSet dsApprovalType_orig = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (dsApprovalType_orig != null && dsApprovalType_orig.size() > 0) {
                safeSQL.reset();
                String sql1 = "SELECT approvaltypeid, approvalstep, roleid, mandatoryflag, usersequence, forcepeerflag FROM approvaltypestep  WHERE approvaltypeid = " + safeSQL.addVar(strVersionApprType);
                DataSet dsApprovalStep_orig = this.getQueryProcessor().getPreparedSqlDataSet(sql1, safeSQL.getValues());
                for (int i11 = 0; i11 < primaryData.getRowCount(); ++i11) {
                    DataSet dsApprovalType = dsApprovalType_orig.copy();
                    DataSet dsApprovalStep = dsApprovalStep_orig.copy();
                    String keyid14 = primaryData.getValue(i11, keycolid1, "");
                    String keyid2 = primaryData.getValue(i11, keycolid2, "");
                    String[] keyid3 = primaryData.getValue(i11, keycolid3, "");
                    dsApprovalType.setString(-1, "sdcid", sdcid);
                    for (int j = 0; j < dsApprovalType.getRowCount(); ++j) {
                        dsApprovalType.setString(j, "keyid1", keyid14);
                        if (StringUtil.getLen(keyid2) > 0L) {
                            dsApprovalType.setString(j, "keyid2", keyid2);
                        } else {
                            dsApprovalType.setString(j, "keyid2", "(null)");
                        }
                        if (StringUtil.getLen((String)keyid3) > 0L) {
                            dsApprovalType.setString(j, "keyid3", (String)keyid3);
                            continue;
                        }
                        dsApprovalType.setString(j, "keyid3", "(null)");
                    }
                    dsApprovalType.setDate(-1, "createdt", now);
                    dsApprovalType.setString(-1, "createtool", this.connectionInfo.getTool());
                    dsApprovalType.setString(-1, "createby", this.connectionInfo.getSysuserId());
                    dsApprovalType.setDate(-1, "moddt", now);
                    dsApprovalType.setString(-1, "modtool", this.connectionInfo.getTool());
                    dsApprovalType.setString(-1, "modby", this.connectionInfo.getSysuserId());
                    dsApprovalType.setString(-1, "approvalflag", "N");
                    dsApprovalType.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
                    dsApprovalType.setString(-1, "approvalfunction", "Versioned");
                    DataSet approvals = sdiData.getDataset("approval");
                    if (approvals != null) {
                        approvals.copyRow(dsApprovalType, -1, 1);
                    } else {
                        approvals = dsApprovalType;
                    }
                    sdiData.setDataset("approval", approvals);
                    if (dsApprovalStep == null || dsApprovalStep.size() <= 0) continue;
                    dsApprovalStep.setString(-1, "sdcid", sdcid);
                    for (int k = 0; k < dsApprovalStep.getRowCount(); ++k) {
                        dsApprovalStep.setString(k, "keyid1", keyid14);
                        if (StringUtil.getLen(keyid2) > 0L) {
                            dsApprovalStep.setString(k, "keyid2", keyid2);
                        }
                        if (StringUtil.getLen((String)keyid3) > 0L) {
                            dsApprovalStep.setString(k, "keyid3", (String)keyid3);
                            continue;
                        }
                        dsApprovalStep.setString(k, "keyid3", "(null)");
                    }
                    dsApprovalStep.setNumber(-1, "approvalstepinstance", 1);
                    dsApprovalStep.setDate(-1, "createdt", now);
                    dsApprovalStep.setString(-1, "createtool", this.connectionInfo.getTool());
                    dsApprovalStep.setString(-1, "createby", this.connectionInfo.getSysuserId());
                    dsApprovalStep.setDate(-1, "moddt", now);
                    dsApprovalStep.setString(-1, "modtool", this.connectionInfo.getTool());
                    dsApprovalStep.setString(-1, "modby", this.connectionInfo.getSysuserId());
                    dsApprovalStep.setString(-1, "approvalflag", "N");
                    dsApprovalStep.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
                    DataSet approvalSteps = sdiData.getDataset("approvalstep");
                    if (approvalSteps != null) {
                        approvalSteps.copyRow(dsApprovalStep, -1, 1);
                    } else {
                        approvalSteps = dsApprovalStep;
                    }
                    sdiData.setDataset("approvalstep", approvalSteps);
                }
            }
        }
        if (isTriggerSDCRule || isImport) {
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PreAdd");
            if (isImport) {
                sdcPreRules.setCMTImport(isImport);
                sdcPreRules.preCMTImport(sdiData, properties, true);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.setCMTImport(isImport);
                    customRules.preCMTImport(sdiData, properties, true);
                }
            }
            if (isTriggerSDCRule) {
                Trace.startBusinessRule(sdcid + "." + "PreAdd", true);
                sdcPreRules.preAdd(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PreAdd", true);
                Trace.startBusinessRule(sdcid + "." + "PreAdd", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.preAdd(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PreAdd", false);
                sdcPreRules.endRule();
            }
        }
        this.logger.info("Issueing insert statements");
        StringBuffer newkeyid1 = new StringBuffer();
        StringBuffer newkeyid2 = new StringBuffer();
        StringBuffer newkeyid3 = new StringBuffer();
        try {
            ImportSnapshot.ImportInstructions instructions = (ImportSnapshot.ImportInstructions)properties.get("importInstructions");
            boolean isIgnoreMissingObjects = instructions != null ? instructions.isIgnoreMissingObjects() : false;
            for (String datasetname : sdiData.getDatasets()) {
                DataSet dataset = sdiData.getDataset(datasetname);
                if (dataset == null || dataset.getRowCount() <= 0) continue;
                if (isImport && isIgnoreMissingObjects) {
                    String[] sourcecolumns;
                    String datasettableid;
                    String string = datasettableid = "primary".equals(datasetname) ? tableid : SDIData.getDatasetTablename(datasetname);
                    if (!this.database.checkPreparedExists("SELECT 1 FROM systable WHERE tableid=?", new Object[]{datasettableid})) {
                        this.writeOutputLogger("Ignore missing table " + datasettableid, properties);
                        continue;
                    }
                    for (String colid : sourcecolumns = dataset.getColumns()) {
                        boolean colexist;
                        if (colid.indexOf("__") == 0 || (colexist = this.database.checkPreparedExists("SELECT 1 FROM syscolumn WHERE tableid=? AND columnid=?", new Object[]{datasettableid, colid}))) continue;
                        dataset.renameColumn(colid, "__" + colid);
                        this.writeOutputLogger("Ignore missing column " + colid + " for table " + datasettableid, properties);
                    }
                }
                if (isImport && (datasetname.equals("attribute") || datasetname.equals("sdiworkitem_sdiattribute") || datasetname.equals("sdidata_sdiattribute")) && dataset.isValidColumn("sdiattributeid")) {
                    dataset.renameColumn("sdiattributeid", "__sdiattributeid");
                }
                if (datasetname.equals("category") || datasetname.equals("role")) {
                    try {
                        DataSetUtil.insert(this.database, dataset, SDIData.getDatasetTablename(datasetname));
                    }
                    catch (Exception datasettableid) {}
                    continue;
                }
                if (datasetname.equals("sdidata_sdiattribute")) {
                    DataSetUtil.insert(this.database, dataset, SDIData.getDatasetTablename("sdiattribute"));
                    continue;
                }
                if (datasetname.equals("s_sdicertification")) {
                    try {
                        DataSetUtil.insert(this.database, dataset, "s_sdicertification");
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Exception occurred when inserting Certification data.", e);
                    }
                }
                if (datasetname.equals("attachment") || datasetname.equals("webpagepropertytree") && !isImport) continue;
                try {
                    if (datasetname.equals("sdiworkitem") && "Sample".equals(sdcid)) {
                        String sdiSiteId = "";
                        String sdiTestingDepartmentId = "";
                        String sdiWorkareaDepartmentId = "";
                        String testingdepartmentid = "";
                        String testinglabType = "";
                        String workareadepartmentid = "";
                        HashMap<String, DataSet> workitemMap = new HashMap<String, DataSet>();
                        HashMap<String, String> findSDIWI = new HashMap<String, String>();
                        PropertyList workItemSDCProps = sdcProcessor.getPropertyList("WorkItem");
                        for (int p = 0; p < primaryData.getRowCount(); ++p) {
                            String keyid15 = primaryData.getString(p, keycolid1);
                            findSDIWI.put("keyid1", keyid15);
                            sdiSiteId = primaryData.getValue(p, "sitedepartmentid");
                            sdiTestingDepartmentId = primaryData.getValue(p, "testingdepartmentid");
                            sdiWorkareaDepartmentId = primaryData.getValue(p, "workareadepartmentid");
                            DataSet sdiwi = dataset.getFilteredDataSet(findSDIWI);
                            for (int w = 0; w < sdiwi.getRowCount(); ++w) {
                                DataSet workItemDS;
                                String workItemId = sdiwi.getValue(w, "workitemid");
                                String workItemVersionId = sdiwi.getValue(w, "workitemversionid", "C");
                                if ("C".equalsIgnoreCase(workItemVersionId)) {
                                    workItemVersionId = SdiInfo.getCurrentVersion("WorkItem", workItemId, null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                                }
                                if ((workItemDS = (DataSet)workitemMap.get(workItemId + ";" + workItemVersionId)) == null) {
                                    this.database.createPreparedResultSet("getworkitem", "select * from workitem where workitemid = ? and workitemversionid = ?", new String[]{workItemId, workItemVersionId});
                                    workItemDS = new DataSet(this.database.getResultSet("getworkitem"));
                                    workitemMap.put(workItemId + ";" + workItemVersionId, workItemDS);
                                }
                                if (workItemDS == null || workItemDS.getRowCount() <= 0) continue;
                                testingdepartmentid = workItemDS.getString(0, "testingdepartmentid", "");
                                workareadepartmentid = workItemDS.getString(0, "workareadepartmentid", "");
                                testinglabType = workItemDS.getString(0, "testinglabtype", "");
                                WorkItemUtil.resolveSDIWorkItemTestingDepartment(sdiwi, w, workItemSDCProps, workItemDS, testingdepartmentid, testinglabType, sdiSiteId, sdiTestingDepartmentId, workareadepartmentid, sdiWorkareaDepartmentId, this.database, this.connectionInfo);
                            }
                        }
                    }
                    DataSetUtil.insert(this.database, dataset, datasetname.equals("primary") ? tableid : SDIData.getDatasetTablename(datasetname));
                }
                catch (SapphireException se) {
                    if (isImport && !"primary".equals(datasetname) && (this.connectionInfo.getDbms().equals("ORA") && se.getMessage().indexOf("ORA-00001") > -1 || this.connectionInfo.getDbms().equals("MSS") && se.getMessage().indexOf("duplicate key") > -1)) {
                        this.writeOutputLogger("Ignore duplicate insert into " + SDIData.getDatasetTablename(datasetname) + " for SDC " + sdcid, properties);
                        continue;
                    }
                    this.writeOutputLogger("ERROR insert into " + SDIData.getDatasetTablename(datasetname) + " for SDC " + sdcid + " " + dataset.toJSONString(false, false), properties);
                    this.writeOutputLogger("ERROR:" + se.getMessage(), properties);
                    throw se;
                }
            }
            int rows = primaryData.getRowCount();
            if (StringUtil.getLen(keycolid1) > 0L) {
                for (int row = 0; row < rows; ++row) {
                    newkeyid1.append(";").append(primaryData.getString(row, keycolid1));
                }
                properties.setProperty("newkeyid1", newkeyid1.substring(1));
            }
            if (StringUtil.getLen(keycolid2) > 0L) {
                for (int row = 0; row < rows; ++row) {
                    newkeyid2.append(";").append(primaryData.getString(row, keycolid2));
                }
                properties.setProperty("newkeyid2", newkeyid2.substring(1));
            }
            if (StringUtil.getLen(keycolid3) > 0L) {
                for (int row = 0; row < rows; ++row) {
                    newkeyid3.append(";").append(primaryData.getString(row, keycolid3));
                }
                properties.setProperty("newkeyid3", newkeyid3.substring(1));
            }
            if (isImport && (templateData.getDataset("datasetattribute") != null || templateData.getDataset("dataitemattribute") != null || templateData.getDataset("sdiworkitemattribute") != null || templateData.getDataset("attachmentattribute") != null)) {
                sdiData.setDataset("datasetattribute", templateData.getDataset("datasetattribute"));
                sdiData.setDataset("dataitemattribute", templateData.getDataset("dataitemattribute"));
                sdiData.setDataset("sdiworkitemattribute", templateData.getDataset("sdiworkitemattribute"));
                sdiData.setDataset("attachmentattribute", templateData.getDataset("attachmentattribute"));
                String rsetid = this.getDAMProcessor().createRSet(sdcid, properties.getProperty("newkeyid1"), properties.getProperty("newkeyid2"), properties.getProperty("newkeyid3"));
                this.repopulateSDIAttributeKeyid1AndInsert(sdiData, rsetid, properties);
            }
            if (applyworkitems) {
                PropertyList newprops = new PropertyList();
                newprops.setProperty("sdcid", sdcid);
                newprops.setProperty("keyid1", properties.getProperty("newkeyid1"));
                newprops.setProperty("keyid2", properties.getProperty("newkeyid2"));
                newprops.setProperty("keyid3", properties.getProperty("newkeyid3"));
                String forceNew = GetForceNewPolicy.analyzeForceNewPolicy("Y", this.getConfigurationProcessor());
                newprops.setProperty("forcenew", properties.getProperty("forcenew", forceNew));
                newprops.setProperty("tracelogid", properties.getProperty("tracelogid", ""));
                ActionProcessor ap = this.getActionProcessor();
                ap.processAction("ApplySDIWorkItem", "1", newprops);
            }
            if (templateData != null) {
                DataSet sdiattributesToAdd = new DataSet();
                DataSet sdiattributesToEdit = new DataSet();
                DataSet allExistingTargetAttributes = new DataSet();
                DataSet dsSourceSDIWIAttributes = this.getQueryProcessor().getPreparedSqlDataSet("select a.*, w.workitemid, w.workitemversionid from sdiattribute a, sdiworkitem w  where a.sdcid = ? and  a.keyid1 = w.sdiworkitemid and  w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? and w.keyid3 = ? ", (Object[])new String[]{"SDIWorkItem", sdcid, templatekeyid1, templatekeyid2 == null || templatekeyid2.length() == 0 ? "(null)" : templatekeyid2, templatekeyid3 == null || templatekeyid3.length() == 0 ? "(null)" : templatekeyid3}, true);
                DataSet sdiwi = sdiData.getDataset("sdiworkitem");
                if (dsSourceSDIWIAttributes.getRowCount() > 0 && sdiwi.getRowCount() > 0) {
                    CopySDIDetail.copyDownSourceAttributes(dsSourceSDIWIAttributes, sdiwi, this.getQueryProcessor(), this.getSDCProcessor(), allExistingTargetAttributes, new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.logger, new M18NUtil(this.connectionInfo), sdiattributesToAdd, sdiattributesToEdit);
                }
                if (sdiattributesToAdd.getRowCount() > 0 || sdiattributesToEdit.getRowCount() > 0) {
                    CopySDIDetail.addEditAttributes(sdiattributesToAdd, sdiattributesToEdit, allExistingTargetAttributes, this.logger, this.database);
                }
            }
            if (copyDownPolicy != null && copyDownPolicy.containsKey("attachments") && copyDownPolicy.get("attachments").size() > 0) {
                CopySDIAttachment.copyDownSDIAttachment(sdc, primaryData, null, copyDownPolicy.get("attachments"), sapphireConnection);
            }
            if (copyDownPolicy != null && copyDownPolicy.containsKey("sdidetails") && copyDownPolicy.get("sdidetails").size() > 0) {
                CopySDIDetail.copyDownSDIDetails(sdc, primaryData, null, (List<PropertyList>)copyDownPolicy.get("sdidetails"), sdcProcessor, this.getActionProcessor(), this.getSDIProcessor());
            }
            String copyAttachmentProperty = properties.getProperty("copyattachment", "");
            boolean copyAttachmentFlag = copyAttachmentProperty.equals("Y");
            PropertyList copydownFullPolicy = this.getConfigurationProcessor().getPolicy("CopyDownPolicy", sdcid + " Custom");
            PropertyList copySDIPropsFromCopyDownPolicy = copydownFullPolicy != null ? copydownFullPolicy.getPropertyListNotNull("copysdi") : new PropertyList();
            String copyAllOption = "E";
            if (!copyAttachmentFlag && copyAttachmentProperty.length() == 0 && (isAddFromTemplate || newVersion || isCopySDI) && plIntraSDICopying.getProperty("copyattachments", "N").equals("Y")) {
                PropertyList plAttachmentDetails = plIntraSDICopying.getPropertyListNotNull("attachmentdetails");
                copyAttachmentFlag = "Y".equalsIgnoreCase(plAttachmentDetails.getProperty("copyallattachments", "N"));
                copyAllOption = plAttachmentDetails.getProperty("copyalloption", "Editable Copy");
                if (copyAllOption.equals("Editable Copy")) {
                    copyAllOption = "E";
                } else if (copyAllOption.equals("Non Editable Copy")) {
                    copyAllOption = "F";
                } else if (copyAllOption.equals("Linked Reference")) {
                    copyAllOption = "L";
                } else if (copyAllOption.equals("Same As Source")) {
                    copyAllOption = "S";
                }
            }
            if (isImport) {
                DataSet attachments = sdiData.getDataset("attachment");
                this.addSDIAttachmentsFromSnapshot(attachments, sdiSnapshotItem, properties);
            } else if (copyAttachmentFlag) {
                PropertyList newprops = new PropertyList();
                newprops.setProperty("fromsdcid", sdcid);
                newprops.setProperty("fromkeyid1", templatekeyid1);
                newprops.setProperty("fromkeyid2", templatekeyid2);
                newprops.setProperty("fromkeyid3", templatekeyid3);
                newprops.setProperty("tokeyid1", properties.getProperty("newkeyid1"));
                newprops.setProperty("tokeyid2", properties.getProperty("newkeyid2"));
                newprops.setProperty("tokeyid3", properties.getProperty("newkeyid3"));
                newprops.setProperty("fromcopymodeflag", properties.getProperty("copyattachmentmode", copyAllOption));
                this.getActionProcessor().processAction("CopySDIAttachment", "1", newprops);
            } else if (templatekeyid1.length() > 0 && copyAttachmentProperty.length() == 0 && plIntraSDICopying.getProperty("copyattachments", "N").equals("Y")) {
                CopySDIAttachment.copyIntraSDIAttachmentClasses(sdcid, plIntraSDICopying, sapphireConnection, templatekeyid1, templatekeyid2, templatekeyid3, properties.getProperty("newkeyid1"), properties.getProperty("newkeyid2"), properties.getProperty("newkeyid3"));
            }
            if (!isAddTemplate) {
                this.createWorkSheetForDatasets(sdiData, properties);
            }
            if (addToWorkflowProps != null) {
                this.getActionProcessor().processAction("AddToWorkflow", "1", addToWorkflowProps);
            }
        }
        catch (SapphireException se) {
            if (this.connectionInfo.getDbms().equals("ORA") && se.getMessage().indexOf("ORA-00001") > -1 || this.connectionInfo.getDbms().equals("MSS") && se.getMessage().indexOf("duplicate key") > -1) {
                this.getErrorHandler().add(sdcid, "AddSDI", this.getTranslationProcessor().translate("CheckUniqueness"), "VALIDATION", this.getTranslationProcessor().translate(sdcid + " already exists - choose another name."));
            }
            throw new SapphireException(this.getTranslationProcessor().translate("DB_ACTION_FAILED"), this.getTranslationProcessor().translate("Failure saving sdiData. " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId()))), se);
        }
        catch (Exception e) {
            throw new SapphireException("GENERAL_ERROR", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), new PostAddEventObject(sdcid, sdc, sdiData, properties));
        if (isTriggerSDCRule || isImport) {
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PostAdd");
            if (isImport) {
                sdcPostRules.setCMTImport(isImport);
                sdcPostRules.postCMTImport(sdiData, properties, true);
                for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                    customRules.setCMTImport(isImport);
                    customRules.postCMTImport(sdiData, properties, true);
                }
            }
            if (isTriggerSDCRule) {
                Trace.startBusinessRule(sdcid + "." + "PostAdd", true);
                sdcPostRules.postAdd(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostAdd", true);
                Trace.startBusinessRule(sdcid + "." + "PostAdd", false);
                for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                    customRules.postAdd(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostAdd", false);
                sdcPostRules.endRule();
            }
        }
        String changecontrolledflag = cmtPolicy.getChangeControlledFlag();
        if (!cmtPolicy.isChangeControlDeferToRepository() && ("Y".equals(changecontrolledflag) || "T".equals(cmtPolicy.getChangeControlledFlag()) && "Y".equals(primaryData.getValue(0, "templateflag")))) {
            String changelogoption;
            String changerequestid = properties.getProperty("cmtchangerequestid", "").trim();
            String departmentid = properties.getProperty("cmtdepartmentid", "").trim();
            if ("Department".equals(sdcid)) {
                departmentid = "";
            }
            if (changerequestid.length() == 0 && !isImport && (changelogoption = this.getConfigurationProcessor().getProfileProperty("changelogoptions")) != null && changelogoption.length() > 0) {
                try {
                    JSONObject o = new JSONObject(changelogoption);
                    String string = changerequestid = o.has("changerequestid") ? o.getString("changerequestid") : "";
                    if (departmentid.length() == 0) {
                        departmentid = o.has("checkedoutbydepartmentid") ? o.getString("checkedoutbydepartmentid") : "";
                    }
                }
                catch (JSONException o) {
                    // empty catch block
                }
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", properties.getProperty("newkeyid1"));
            props.setProperty("keyid2", properties.getProperty("newkeyid2"));
            props.setProperty("keyid3", properties.getProperty("newkeyid3"));
            if ("PropertyTree".equals(sdcid)) {
                props.setProperty("propertytreenodeid", "__FULL");
            }
            props.setProperty("mode", "add");
            props.setProperty("changerequestid", changerequestid);
            if (departmentid.length() > 0) {
                props.setProperty("departmentid", departmentid);
            }
            this.getActionProcessor().processActionClass(CheckOutSDI.class.getName(), props);
            String changelogid = props.getProperty("changelogid");
            properties.setProperty("changelogid", changelogid);
            if ("Y".equals(properties.getProperty("checkinsdiflag")) && OpalUtil.isNotEmpty(changelogid)) {
                props.clear();
                props.setProperty("changelogid", changelogid);
                this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), props);
            }
        }
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.indexSDI(this.connectionInfo, sdcid, properties.getProperty("newkeyid1"), properties.getProperty("newkeyid2"), properties.getProperty("newkeyid3"));
        }
        this.logger.info(sdcid + " created: " + newkeyid1);
    }

    private void createWorkSheetForDatasets(SDIData sdiData, PropertyList properties) throws SapphireException {
        DataSet datasetAdded = sdiData.getDataset("dataset");
        if (datasetAdded != null) {
            DataSet plFormRule = new DataSet();
            StringBuffer sql = new StringBuffer();
            if (datasetAdded.getRowCount() > 10) {
                String rsetid = this.getDAMProcessor().createRSet("ParamList", datasetAdded.getColumnValues("paramlistid", ";"), datasetAdded.getColumnValues("paramlistversionid", ";"), datasetAdded.getColumnValues("variantid", ";"));
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT paramlist.paramlistid, paramlist.paramlistversionid, paramlist.variantid, sdiformrule.formid, sdiformrule.formversionid");
                sql.append(" FROM paramlist, rsetitems, sdiformrule");
                sql.append(" WHERE rsetitems.sdcid = 'ParamList'");
                sql.append(" AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
                sql.append(" AND rsetitems.keyid1 = paramlist.paramlistid");
                sql.append(" AND rsetitems.keyid2 = paramlist.paramlistversionid");
                sql.append(" AND rsetitems.keyid3 = paramlist.variantid");
                sql.append(" AND sdiformrule.sdcid = 'ParamList'");
                sql.append(" AND sdiformrule.formrule = 'default'");
                sql.append(" AND sdiformrule.keyid1 = paramlist.paramlistid");
                sql.append(" AND sdiformrule.keyid2 = paramlist.paramlistversionid");
                sql.append(" AND sdiformrule.keyid3 = paramlist.variantid");
                sql.append(" AND paramlist.createworksheetrule = 'On Creation'");
                DataSet formRule = this.getQueryProcessor().getPreparedSqlDataSet("plformrule", sql.toString(), safeSQL.getValues());
                HashMap<String, String> find = new HashMap<String, String>();
                for (int i = 0; i < datasetAdded.getRowCount(); ++i) {
                    String plId = datasetAdded.getValue(i, "paramlistid");
                    String plVersionId = datasetAdded.getValue(i, "paramlistversionid");
                    String plVariatId = datasetAdded.getValue(i, "variantid");
                    find.put("paramlistid", plId);
                    find.put("paramlistversionid", plVersionId);
                    find.put("variantid", plVariatId);
                    int foundRow = formRule.findRow(find);
                    if (foundRow < 0) continue;
                    int row = plFormRule.addRow();
                    plFormRule.setString(row, "paramlistid", formRule.getValue(foundRow, "paramlistid"));
                    plFormRule.setString(row, "paramlistversionid", formRule.getValue(foundRow, "paramlistversionid"));
                    plFormRule.setString(row, "variantid", formRule.getValue(foundRow, "variantid"));
                    plFormRule.setString(row, "dataset", datasetAdded.getValue(i, "dataset"));
                    plFormRule.setString(row, "formid", formRule.getValue(foundRow, "formid"));
                    plFormRule.setString(row, "formversionid", formRule.getValue(foundRow, "formversionid"));
                }
            } else {
                sql.append("SELECT paramlist.paramlistid, paramlist.paramlistversionid, paramlist.variantid, sdiformrule.formid, sdiformrule.formversionid");
                sql.append(" FROM paramlist, sdiformrule");
                sql.append(" WHERE sdiformrule.sdcid = 'ParamList'");
                sql.append(" AND sdiformrule.keyid1 = paramlist.paramlistid");
                sql.append(" AND sdiformrule.keyid2 = paramlist.paramlistversionid");
                sql.append(" AND sdiformrule.keyid3 = paramlist.variantid");
                sql.append(" AND paramlist.paramlistid = ?");
                sql.append(" AND paramlist.paramlistversionid = ?");
                sql.append(" AND paramlist.variantid = ?");
                sql.append(" AND sdiformrule.formrule = 'default'");
                sql.append(" AND paramlist.createworksheetrule = 'On Creation'");
                PreparedStatement plStmt = this.database.prepareStatement("plformrule", sql.toString());
                try {
                    for (int i = 0; i < datasetAdded.getRowCount(); ++i) {
                        plStmt.setString(1, datasetAdded.getValue(i, "paramlistid"));
                        plStmt.setString(2, datasetAdded.getValue(i, "paramlistversionid"));
                        plStmt.setString(3, datasetAdded.getValue(i, "variantid"));
                        DataSet plDS = new DataSet(plStmt.executeQuery());
                        if (plDS == null || plDS.getRowCount() <= 0) continue;
                        int row = plFormRule.addRow();
                        plFormRule.setString(row, "paramlistid", plDS.getValue(0, "paramlistid"));
                        plFormRule.setString(row, "paramlistversionid", plDS.getValue(0, "paramlistversionid"));
                        plFormRule.setString(row, "variantid", plDS.getValue(0, "variantid"));
                        plFormRule.setString(row, "dataset", datasetAdded.getValue(i, "dataset"));
                        plFormRule.setString(row, "formid", plDS.getValue(0, "formid"));
                        plFormRule.setString(row, "formversionid", plDS.getValue(0, "formversionid"));
                    }
                }
                catch (SQLException e) {
                    throw new SapphireException("DB_ACTION_FAILED", "Failed to fetch sdiformrule");
                }
            }
            this.logger.info("AddSDI: ParamList Form Rule " + plFormRule.toString());
            if (plFormRule.getRowCount() > 0) {
                plFormRule.sort("formid, formversionid");
                ArrayList<DataSet> groupedPlDs = plFormRule.getGroupedDataSets("formid, formversionid");
                Iterator<DataSet> iter = groupedPlDs.iterator();
                String sdcid = properties.getProperty("sdcid");
                while (iter.hasNext()) {
                    DataSet tempDataSet = iter.next();
                    this.logger.info("AddSDI: Call CreateWorksheet for group: " + tempDataSet.toString());
                    PropertyList createWSProps = new PropertyList();
                    createWSProps.setProperty("sdcid", sdcid);
                    createWSProps.setProperty("keyid1", properties.getProperty("newkeyid1", ""));
                    createWSProps.setProperty("keyid2", properties.getProperty("newkeyid2", ""));
                    createWSProps.setProperty("keyid3", properties.getProperty("newkeyid3", ""));
                    createWSProps.setProperty("paramlistid", tempDataSet.getColumnValues("paramlistid", ";"));
                    createWSProps.setProperty("paramlistversionid", tempDataSet.getColumnValues("paramlistversionid", ";"));
                    createWSProps.setProperty("variantid", tempDataSet.getColumnValues("variantid", ";"));
                    createWSProps.setProperty("dataset", tempDataSet.getColumnValues("dataset", ";"));
                    createWSProps.setProperty("formid", tempDataSet.getString(0, "formid", ""));
                    createWSProps.setProperty("formversionid", tempDataSet.getString(0, "formversionid", ""));
                    this.getActionProcessor().processAction("CreateWorksheet", "1", createWSProps);
                }
            }
        }
    }
}

