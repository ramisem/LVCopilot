/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.admin.ddt.DDTUtil;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.LV_App;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;

public class User
extends BaseSDCRules
implements CacheNames {
    public static final String TYPE_NAMEDUSER = "S";
    public static final String TYPE_CONCURRENTUSER = "C";
    public static final String TYPE_VIRTUALUSER = "V";
    public static final String TYPE_CONCURRENTVIRTUALUSER = "I";
    public static final String TYPE_EXPRESSUSER = "E";
    public static final String TYPE_CONCURRENTEXPRESSUSER = "X";
    public static final String TYPE_EXTERNALAPPUSER = "A";
    public static final String TYPE_PORTALNAMEDUSER = "P";
    public static final String TYPE_PORTALCONCURRENTUSER = "Q";
    private static String[] profileProperties = new String[]{"startuptype", "defaultworkbook", "bourl", "bodocumentdomain", "bodomain", "boexchangemode", "bouniverse", "bocmsname", "bousername", "bopassword", "borootfoldername", "boauthenticationtype", "boconnectiontimeout", "bocrystaldateformat", "bodeskidateformat", "bowebidateformat", "menuanimation", "fadeanimation", "resizeanimation", "notificationformat"};
    private static String[] guiProfileProperties = new String[]{"logonpageurl", "logonmenu", "logongroup"};

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String templateid = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        this.doPortalUsersChecks(primary, true);
        for (int i = 0; i < primary.size(); ++i) {
            String templateflag;
            String namedUserFlag;
            String sysuserid = primary.getString(i, "sysuserid");
            String password = primary.getString(i, "password");
            if (templateid.length() > 0 && actionProps.getProperty("password").length() == 0) {
                password = this.getDefaultPassword(i, primary);
            }
            if (templateid.length() > 0 && !TYPE_PORTALNAMEDUSER.equals(namedUserFlag = primary.getString(i, "nameduserflag", this.getOldPrimaryValue(primary, i, "nameduserflag"))) && !TYPE_PORTALCONCURRENTUSER.equals(namedUserFlag)) {
                primary.setString(i, "logonname", actionProps.getProperty("logonname").length() == 0 || "(null)".equals(actionProps.getProperty("logonname")) ? sysuserid : actionProps.getProperty("logonname"));
            }
            if (password != null && password.indexOf("{|}") == 0 && "(storedpassword)".equals(password = EncryptDecrypt.decryptRSA(password.substring("{|}".length())))) {
                if (templateid.length() > 0) {
                    password = this.getDefaultPassword(i, primary);
                } else {
                    this.throwError("IsValidPassword", "VALIDATION", this.getTranslationProcessor().translate("Please enter a valid password."));
                }
            }
            String usertype = primary.getString(i, "nameduserflag");
            if (Configuration.getInstance().getLicense(this.getDatabaseid()).isExpress() && (usertype.equals(TYPE_NAMEDUSER) || usertype.equals(TYPE_CONCURRENTUSER))) {
                this.throwError("ExpressLicense", "VALIDATION", this.getTranslationProcessor().translate("You cannot create named or concurrent users with an Express license."));
            }
            if ((templateflag = primary.getValue(i, "templateflag")).equalsIgnoreCase("Y")) {
                primary.setString(i, "logonname", "");
            }
            if (primary.getValue(i, "sysuserdesc").length() == 0 && !templateflag.equalsIgnoreCase("Y")) {
                this.setError("CheckUserDescription", "INFORMATION", this.getTranslationProcessor().translate("Description not defined for user:") + " " + sysuserid);
            }
            this.checkNotExists(sysuserid);
            this.checkLicense(sysuserid, usertype, true, templateflag != null && templateflag.equals("Y"));
            this.checkPasswordExists(sysuserid, password);
            if (!"Y".equals(actionProps.getProperty("bypassformatcheck")) && !this.isCMTImport()) {
                this.checkPasswordFormat(sysuserid, password);
            }
            primary.setString(i, "password", EncryptDecrypt.encodePassword(password));
            primary.setString(i, "casesensitivepasswordflag", "Y");
            primary.setString(i, "lastjobtype", "");
            primary.setString(i, "mfasecretkey", "");
            if (templateid.length() > 0 && primary.getString(i, "calendarid") != null) {
                this.copyWorkHours(primary, i);
            }
            if (!TYPE_EXPRESSUSER.equals(primary.getValue(i, "authenticationtypeflag")) && !"Y".equals(actionProps.getProperty("isautocreatedfromldap"))) continue;
            primary.setString(i, "changepasswordflag", "N");
        }
        sdiData.setDataset("departmentsysuser", new DataSet());
    }

    private String getDefaultPassword(int row, DataSet primary) throws SapphireException {
        ConfigurationProcessor configuration = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
        String password = configuration.getSysConfigProperty("defaultpassword", "");
        if (password.length() == 0) {
            password = TYPE_NAMEDUSER + EncryptDecrypt.generateRandomAESKey() + "#1";
        }
        primary.setString(row, "changepasswordflag", "Y");
        return password;
    }

    private void copyWorkHours(DataSet primary, int index) throws SapphireException {
        this.database.createPreparedResultSet("SELECT * FROM calendar WHERE calendarid=?", new String[]{primary.getString(index, "calendarid")});
        DataSet calenderDS = new DataSet(this.database.getResultSet());
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", TYPE_CONCURRENTVIRTUALUSER);
        props.setProperty("corehours", calenderDS.getValue(0, "corehours"));
        props.setProperty("calendardesc", "User " + primary.getString(index, "logonname") + " Calendar");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        primary.setString(index, "calendarid", calenderid);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("N".equals(actionProps.getProperty("isUserCreatedFromTemplate", "N"))) {
            Department.addUserAppAccess(sdiData.getDataset("departmentsysuser"), this.getQueryProcessor(), this.getActionProcessor(), this.logger);
        }
        this.flushCaches(actionProps);
        if (sdiData.getDatasets().contains("appsysuser")) {
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
        }
        Department.checkIfUserAlreadyPortalAdmin(sdiData.getDataset("departmentsysuser"), this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor());
    }

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            boolean autokeygen;
            String namedUserFlag = primary.getString(i, "nameduserflag", "");
            String templateFlag = primary.getValue(i, "templateflag", "N");
            if (!TYPE_PORTALNAMEDUSER.equals(namedUserFlag) && !TYPE_PORTALCONCURRENTUSER.equals(namedUserFlag) || !"N".equals(templateFlag)) continue;
            String keygenerationrule = this.getSDCProcessor().getProperty("User", "keygenerationrule");
            boolean bl = autokeygen = StringUtil.getLen(keygenerationrule) > 0L && keygenerationrule.charAt(0) == 'A';
            if (autokeygen) continue;
            int nextSeq = this.getSequenceProcessor().getSequence("User", "portaluserseq", 1, 1);
            primary.setString(i, "sysuserid", "P-" + String.format("%06d", nextSeq));
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (sdiData.getDatasets().contains("appsysuser")) {
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
        }
        Department.checkIfUserAlreadyPortalAdmin(sdiData.getDataset("departmentsysuser"), this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor());
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.flushCaches(actionProps);
    }

    public void flushCaches(PropertyList actionProps) {
        HashSet<String> departmentSet = new HashSet<String>();
        departmentSet.addAll(Arrays.asList(StringUtil.split(actionProps.getProperty("departmentid"), ";")));
        for (String departmentid : departmentSet) {
            CacheUtil.remove(this.getDatabaseid(), "DepartmentUsers", departmentid);
        }
        HashSet<String> userSet = new HashSet<String>();
        userSet.addAll(Arrays.asList(StringUtil.split(actionProps.getProperty("sysuserid"), ";")));
        for (String userid : userSet) {
            CacheUtil.removeAllStartWith(this.getDatabaseid(), "UserSchedules", userid);
            CacheUtil.removeAllEndWith(this.getDatabaseid(), "AuthTokenConnectionid", userid);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templateid = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        DataSet primary = sdiData.getDataset("primary");
        if (templateid != null && templateid.length() > 0) {
            int j;
            int i;
            if (primary != null) {
                this.database.createPreparedResultSet("SELECT * from sdcsecurity where sysuserid=?", new Object[]{templateid});
                DataSet sdcsecurity = new DataSet(this.database.getResultSet());
                if (sdcsecurity.getRowCount() > 0) {
                    for (i = 0; i < primary.getRowCount(); ++i) {
                        for (j = 0; j < sdcsecurity.getRowCount(); ++j) {
                            sdcsecurity.setValue(j, "sysuserid", primary.getString(i, "sysuserid"));
                            sdcsecurity.setDate(j, "createdt", DateTimeUtil.getNowCalendar());
                            sdcsecurity.setValue(j, "createby", this.connectionInfo.getSysuserId());
                        }
                        DataSetUtil.insert(this.database, sdcsecurity, "sdcsecurity");
                    }
                }
            }
            this.database.createPreparedResultSet("SELECT * from departmentsysuser where sysuserid=?", new Object[]{templateid});
            DataSet departmentsysuser = new DataSet(this.database.getResultSet());
            if (departmentsysuser.getRowCount() > 0) {
                for (i = 0; i < primary.getRowCount(); ++i) {
                    for (j = 0; j < departmentsysuser.getRowCount(); ++j) {
                        departmentsysuser.setValue(j, "sysuserid", primary.getString(i, "sysuserid"));
                        departmentsysuser.setDate(j, "createdt", DateTimeUtil.getNowCalendar());
                        departmentsysuser.setValue(j, "createby", this.connectionInfo.getSysuserId());
                    }
                    DataSetUtil.insert(this.database, departmentsysuser, "departmentsysuser");
                }
            }
            this.database.createPreparedResultSet("SELECT * from modulesysuser where sysuserid=? order by moduleid", new Object[]{templateid});
            DataSet modulesysuser = new DataSet(this.database.getResultSet());
            this.checkModuleLicense(modulesysuser, primary.getRowCount());
            if (modulesysuser.getRowCount() > 0) {
                for (int i2 = 0; i2 < primary.getRowCount(); ++i2) {
                    for (int j2 = 0; j2 < modulesysuser.getRowCount(); ++j2) {
                        modulesysuser.setValue(j2, "sysuserid", primary.getString(i2, "sysuserid"));
                        modulesysuser.setDate(j2, "createdt", DateTimeUtil.getNowCalendar());
                    }
                    DataSetUtil.insert(this.database, modulesysuser, "modulesysuser");
                }
            }
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
            StringBuffer inclause = new StringBuffer();
            for (int i3 = 0; i3 < profileProperties.length; ++i3) {
                inclause.append(",'").append(profileProperties[i3]).append("'");
            }
            StringBuffer guiprops = new StringBuffer();
            for (int i4 = 0; i4 < guiProfileProperties.length; ++i4) {
                guiprops.append(" OR propertyid LIKE '%").append(guiProfileProperties[i4]).append("'");
            }
            this.database.createPreparedResultSet("SELECT * FROM profileproperty WHERE sysuserid = ? AND ( propertyid IN (" + inclause.substring(1) + ")" + guiprops.toString() + " )", new Object[]{templateid});
            DataSet profileproperties = new DataSet(this.database.getResultSet());
            for (int i5 = 0; i5 < primary.getRowCount(); ++i5) {
                profileproperties.setValue(-1, "sysuserid", primary.getString(i5, "sysuserid"));
                DataSetUtil.insert(this.database, profileproperties, "profileproperty");
            }
        }
        this.copyBaseDepartment(primary, actionProps);
        this.validateLogonName(primary);
        this.addAppUserRole(primary.getColumnValues("sysuserid", ";"));
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String sysuserid;
        ConnectionInfo conInfo;
        DataSet primary = sdiData.getDataset("primary");
        String action = actionProps.getProperty("operation");
        try {
            CacheUtil.clear(this.connectionInfo.getDatabaseId(), "WebPageAccess");
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        if (!(!"enable".equalsIgnoreCase(action) && !"disable".equalsIgnoreCase(action) || (conInfo = new ConnectionProcessor(this.getConnectionid()).getConnectionInfo(this.getConnectionid())).hasRole("Administrator") || conInfo.hasRole("WebPage-Admin") || conInfo.hasModule("Security") || this.isPortalAdminUser(conInfo.getSysuserId()))) {
            this.throwError("Unauthorized", "VALIDATION", this.getTranslationProcessor().translate("You are not authorized to do this operation!"));
        }
        if ("enable".equalsIgnoreCase(action)) {
            for (int i = 0; i < primary.size(); ++i) {
                sysuserid = primary.getValue(i, "sysuserid");
                DBUtil db = new DBUtil(this.getConnectionId());
                SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                db.setConnection(sapphireConnection);
                db.createPreparedResultSet("SELECT nameduserflag FROM sysuser WHERE sysuserid = ?", sysuserid);
                if (db.getNext()) {
                    String nameduserflag = db.getString("nameduserflag");
                    if (!nameduserflag.equals(TYPE_NAMEDUSER) && !nameduserflag.equals(TYPE_VIRTUALUSER) && !nameduserflag.equals(TYPE_PORTALNAMEDUSER)) continue;
                    Configuration config = Configuration.getInstance();
                    License license = config.getLicense(sapphireConnection.getDatabaseId());
                    db.createPreparedResultSet("SELECT count(*) \"count\" FROM sysuser WHERE nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", nameduserflag);
                    if (!db.getNext()) continue;
                    int dbCount = db.getInt("count");
                    int newCount = 0;
                    String userType = "";
                    if (nameduserflag.equals(TYPE_NAMEDUSER)) {
                        newCount = license.getNamedUsers() + 1;
                        userType = "standard";
                    } else if (nameduserflag.equals(TYPE_PORTALNAMEDUSER)) {
                        newCount = license.getPortalNamedUsers() + 1;
                        userType = "portal";
                    } else {
                        newCount = license.getVirtualUsers() + 1;
                        userType = "virtual";
                    }
                    if (dbCount <= newCount) continue;
                    this.throwError("SETUSERSTATUS_FAILURE", "SETUSERSTATUS_FAILURE", this.getTranslationProcessor().translate("Failed to enable user '" + sysuserid + "' because the max " + userType + " named users has been met"));
                    continue;
                }
                throw new SapphireException("SETUSERSTATUS_FAILURE", "Failed to find user '" + sysuserid + "'");
            }
        }
        for (int i = 0; i < primary.size(); ++i) {
            sysuserid = primary.getValue(i, "sysuserid");
            String password = primary.getValue(i, "password");
            if (password != null && password.indexOf("{|}") == 0) {
                password = EncryptDecrypt.decryptRSA(password.substring("{|}".length()));
                primary.setString(i, "password", password);
            }
            String nameduserflag = primary.getValue(i, "nameduserflag");
            String usertype = primary.getValue(i, "nameduserflag");
            String beforeusertype = this.getOldPrimaryValue(primary, i, "nameduserflag");
            if (Configuration.getInstance().getLicense(this.getDatabaseid()).isExpress() && !beforeusertype.equals(TYPE_NAMEDUSER) && (usertype.equals(TYPE_NAMEDUSER) || usertype.equals(TYPE_CONCURRENTUSER))) {
                this.throwError("ExpressLicense", "VALIDATION", this.getTranslationProcessor().translate("You cannot create named or concurrent users with an Express license."));
            }
            String templateflag = primary.getValue(i, "templateflag");
            this.database.createPreparedResultSet("SELECT nameduserflag, password FROM sysuser WHERE sysuserid = ?", new Object[]{sysuserid});
            String oldPassword = "";
            String oldNameduserflag = "";
            if (this.database.getNext()) {
                oldPassword = this.database.getString("password");
                oldNameduserflag = this.database.getString("nameduserflag");
            }
            if (nameduserflag == null || nameduserflag.length() == 0) {
                nameduserflag = oldNameduserflag;
            }
            this.checkLicense(sysuserid, nameduserflag, !nameduserflag.equals(oldNameduserflag) && (TYPE_NAMEDUSER.equals(nameduserflag) || TYPE_VIRTUALUSER.equals(nameduserflag) || TYPE_PORTALNAMEDUSER.equals(nameduserflag) || TYPE_EXPRESSUSER.equals(nameduserflag)), templateflag != null && "Y".equals(templateflag));
            if (password != null && password.length() > 0 && !password.equals(oldPassword)) {
                if (password.equals("(storedpassword)")) {
                    primary.setString(i, "password", oldPassword);
                } else {
                    this.checkPasswordFormat(sysuserid, password);
                    primary.setString(i, "password", EncryptDecrypt.encodePassword(password));
                    primary.setString(i, "casesensitivepasswordflag", "Y");
                }
            }
            if (this.hasPrimaryValueChanged(primary, i, "activeflag") && "N".equals(primary.getString(i, "activeflag"))) {
                primary.addColumn("disabledflag", 0);
                primary.setString(i, "disabledflag", "Y");
            }
            if (!primary.getValue(i, "disabledflag").equals("Y")) continue;
            CacheUtil.clear(this.getDatabaseid(), "DepartmentUsers");
        }
        this.doPortalUsersChecks(primary, false);
    }

    private void doPortalUsersChecks(DataSet primary, boolean isAddMode) throws SapphireException {
        int noOfPortalUsers = 0;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String namedUserFlag = primary.getString(i, "nameduserflag", this.getOldPrimaryValue(primary, i, "nameduserflag"));
            String templateFlag = primary.getValue(i, "templateflag", this.getOldPrimaryValue(primary, i, "templateflag"));
            if (!"N".equals(templateFlag) || !TYPE_PORTALNAMEDUSER.equals(namedUserFlag) && !TYPE_PORTALCONCURRENTUSER.equals(namedUserFlag)) continue;
            ++noOfPortalUsers;
            if (isAddMode || this.hasPrimaryValueChanged(primary, i, "authenticationtypeflag") || this.hasPrimaryValueChanged(primary, i, "email") || this.hasPrimaryValueChanged(primary, i, "logonname")) {
                String authType = primary.getString(i, "authenticationtypeflag", TYPE_CONCURRENTVIRTUALUSER);
                if (TYPE_EXPRESSUSER.equals(authType)) {
                    String logonName = primary.getString(i, "logonname", "");
                    if (logonName.trim().length() == 0) {
                        throw new SapphireException("LogonNameRequired", "VALIDATION", this.getTranslationProcessor().translate("Logon name is mandatory for LDAP authenticated Portal Users."));
                    }
                } else {
                    String email = primary.getString(i, "email", "");
                    if (email.trim().length() == 0) {
                        throw new SapphireException("EmailRequired", "VALIDATION", this.getTranslationProcessor().translate("Email is mandatory for Portal Users."));
                    }
                    primary.setString(i, "logonname", email);
                }
            }
            if (!isAddMode && !this.hasPrimaryValueChanged(primary, i, "basedepartment") || primary.getString(i, "basedepartment", "").length() != 0) continue;
            throw new SapphireException("BaseClientRequired", "VALIDATION", this.getTranslationProcessor().translate("Base Portal Client is mandatory for Portal Users."));
        }
        if (noOfPortalUsers > 0 && primary.getRowCount() != noOfPortalUsers && isAddMode) {
            throw new SapphireException("PortalUserAdd", "VALIDATION", this.getTranslationProcessor().translate("Portal Users can't be added along with non-Portal users. Please add them separately."));
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String action = actionProps.getProperty("operation");
        boolean isUserActivated = false;
        boolean isLogonNameChanged = false;
        for (int i = 0; i < primary.size(); ++i) {
            if (this.hasPrimaryValueChanged(primary, i, "activeflag") && "Y".equals(primary.getString(i, "activeflag", ""))) {
                isUserActivated = true;
            }
            if (this.hasPrimaryValueChanged(primary, i, "logonname")) {
                isLogonNameChanged = true;
            }
            if (!this.hasPrimaryValueChanged(primary, i, "email")) continue;
            String oldEmail = this.getOldPrimaryValue(primary, i, "email");
            String languageid = this.getOldPrimaryValue(primary, i, "languageid");
            if (oldEmail.isEmpty()) continue;
            try {
                PropertyList mailProps = new PropertyList();
                mailProps.setProperty("actionid", "SendMail");
                mailProps.setProperty("actionversionid", "1");
                mailProps.setProperty("from", ConfigService.getConfigProperty("com.labvantage.sapphire.server.emailfromaddress"));
                mailProps.setProperty("to", oldEmail);
                if (!languageid.isEmpty()) {
                    TranslationProcessor translationProcessor = this.getTranslationProcessor();
                    mailProps.setProperty("subject", translationProcessor.translate("LabVantage Email Address Updated", languageid));
                    mailProps.setProperty("message", translationProcessor.translate("The email address for your LabVantage account has been changed. If you did not request this change please contact your Administrator.", languageid));
                } else {
                    mailProps.setProperty("subject", "LabVantage Email Address Updated");
                    mailProps.setProperty("message", "The email address for your LabVantage account has been changed. If you did not request this change please contact your Administrator.");
                }
                this.getActionProcessor().processAction("AddToDoListEntry", "1", mailProps);
                continue;
            }
            catch (Exception exception) {
                this.throwError("Send Email Error", "VALIDATION", this.getTranslationProcessor().translate("Email service has failed to send an email. Please contact your administrator."));
            }
        }
        if (isLogonNameChanged) {
            this.validateLogonName(primary);
        }
        if ("enable".equalsIgnoreCase(action) || isUserActivated) {
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
        }
        this.copyBaseDepartment(primary, actionProps);
    }

    private void validateLogonName(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String logonname = primary.getValue(i, "logonname");
            String sysuserid = primary.getValue(i, "sysuserid");
            this.database.createPreparedResultSet("SELECT count(*) count FROM sysuser WHERE upper( sysuserid ) = ? OR upper( logonname ) = ?", new String[]{sysuserid.toUpperCase(), sysuserid.toUpperCase()});
            if (this.database.getNext() && this.database.getInt("count") > 1) {
                this.throwError("CheckExists", "VALIDATION", this.getTranslationProcessor().translate("User Id is already used as another user's Logon Name."));
            }
            if (logonname.length() > 0) {
                this.database.createPreparedResultSet("SELECT count(*) count FROM sysuser WHERE ( activeflag!='N' OR activeflag is null ) AND ( upper( sysuserid ) = ? OR upper( logonname ) = ? )", new String[]{logonname.toUpperCase(), logonname.toUpperCase()});
                if (this.database.getNext() && this.database.getInt("count") > 1) {
                    this.throwError("CheckExists", "VALIDATION", this.getTranslationProcessor().translate("Logon Name is already used as another user's User Id or Logon Name."));
                }
            }
            if (!this.connectionInfo.getSysuserId().equalsIgnoreCase(sysuserid)) continue;
            SapphireConnection sc = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "Connection Properties", this.getConnectionId());
        }
    }

    private void checkPasswordExists(String sysuserid, String password) throws SapphireException {
        if (password == null || password.length() == 0) {
            this.throwError("CheckPasswordExists", "VALIDATION", this.getTranslationProcessor().translate("Password not defined for user:") + " " + sysuserid);
        }
    }

    private void checkPasswordFormat(String sysuserid, String password) throws SapphireException {
        try {
            SecurityService.isValidPassword((DBUtil)this.database, sysuserid, password);
        }
        catch (ServiceException e) {
            this.throwError("IsValidPassword", "VALIDATION", e.getMessage());
        }
    }

    private void checkNotExists(String sysuserid) throws SapphireException {
        this.database.createPreparedResultSet("SELECT sysuserid FROM sysuser WHERE upper( sysuserid ) = ?", new Object[]{sysuserid.toUpperCase()});
        if (this.database.getNext()) {
            String existingid = this.database.getString("sysuserid");
            this.throwError("CheckExists", "VALIDATION", this.getTranslationProcessor().translate("User Id already exists as") + " " + existingid);
        }
    }

    private void checkLicense(String sysuserid, String usertype, boolean add, boolean template) throws SapphireException {
        if (usertype == null || usertype.length() == 0) {
            this.throwError("CheckUserTypeExists", "VALIDATION", this.getTranslationProcessor().translate("Usertype not defined for user id:") + " " + sysuserid + ".");
        }
        if (TYPE_NAMEDUSER.equals(usertype) || TYPE_VIRTUALUSER.equals(usertype) || TYPE_PORTALNAMEDUSER.equals(usertype) || TYPE_EXPRESSUSER.equals(usertype)) {
            this.database.createPreparedResultSet("SELECT\tcount(*) \"count\" FROM\tsysuser WHERE\tnameduserflag = ? AND ( templateflag is null or templateflag = 'N' ) AND ( ACTIVEFLAG = 'Y' OR ACTIVEFLAG IS NULL )", new Object[]{usertype});
            this.database.getNext();
            int count = add && !template ? this.database.getInt("count") + 1 : this.database.getInt("count");
            ConnectionProcessor cp = this.getConnectionProcessor();
            if (TYPE_NAMEDUSER.equals(usertype)) {
                if (count > Integer.parseInt(cp.getLicenseProperty("namedusers"))) {
                    this.throwError("NamedUserCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of allowable standard named users."));
                }
            } else if (TYPE_EXPRESSUSER.equals(usertype)) {
                if (count > Integer.parseInt(cp.getLicenseProperty("expressusers"))) {
                    this.throwError("NamedExpressUserCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of allowable Express named users."));
                }
            } else if (TYPE_PORTALNAMEDUSER.equals(usertype)) {
                if (count > Integer.parseInt(cp.getLicenseProperty("portalnamedusers"))) {
                    this.throwError("NamedExpressUserCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of allowable portal named users."));
                }
            } else if (count > Integer.parseInt(cp.getLicenseProperty("virtualusers"))) {
                this.throwError("VirtualUserCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maxium number of allowable virtual named users."));
            }
        }
    }

    private void checkModuleLicense(DataSet templateUserModules, int noOfUsers) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT").append("  m.moduleid, m.maxusers,").append("  (").append("    SELECT COUNT(ms.sysuserid)").append("    FROM modulesysuser ms, sysuser s").append("    WHERE ms.moduleid=m.moduleid").append("    AND ms.sysuserid=s.sysuserid").append("    AND (s.templateflag IS NULL OR s.templateflag='N')").append("    AND (s.activeflag = 'Y' OR s.activeflag IS NULL)").append("  ) \"assignedusers\"").append(" FROM module m, modulesysuser msu").append(" WHERE msu.sysuserid = ").append(safeSQL.addVar(templateUserModules.getString(0, "sysuserid"))).append(" AND msu.moduleid=m.moduleid").append(" ORDER BY msu.moduleid");
        DataSet modules = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        int templateUserModulesCount = templateUserModules.getRowCount();
        for (int i = 0; i < templateUserModulesCount; ++i) {
            String moduleId = templateUserModules.getString(i, "moduleid");
            int row = modules.findRow("moduleid", moduleId);
            int maxUser = this.getMaxUser(modules, row);
            if (maxUser == -1 || maxUser >= modules.getInt(row, "assignedusers", 0) + noOfUsers) continue;
            this.throwError("ModuleUserCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of licensed users for module -") + " " + moduleId);
        }
    }

    private int getMaxUser(DataSet modules, int row) throws SapphireException {
        int maxUser;
        String maxUserStr = modules.getString(row, "maxusers", "");
        try {
            maxUser = Integer.parseInt(maxUserStr);
        }
        catch (NumberFormatException e) {
            maxUser = -1;
        }
        return maxUser;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("SELECT keyid1 from rsetitems WHERE rsetid=? AND keyid1=?", new Object[]{rsetid, this.connectionInfo.getSysuserId()});
        if (this.database.getNext()) {
            this.throwError("DeleteUserSelf", "VALIDATION", this.getTranslationProcessor().translate("You are trying to delete yourself."));
        }
        Object[] vars = new Object[]{rsetid};
        if (DDTUtil.checkTableExists(this.database, "s_sdicertification")) {
            this.database.executePreparedUpdate("DELETE FROM s_sdicertification WHERE resourcesdcid = 'User' AND resourcekeyid1 IN ( SELECT keyid1 from rsetitems WHERE rsetid = ? ) ", vars);
        }
        this.database.executePreparedUpdate("DELETE FROM profileproperty WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM webpagelogtitle WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM webpagelog WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM sysuserfolderitem WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM sysuserfolder WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM sysuserbucket WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM sysuserfavorite WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM sdcsecurity WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM bulletinsysuser WHERE sysuserid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executeUpdate("DELETE FROM bulletin WHERE NOT EXISTS ( SELECT bulletinid FROM bulletinsysuser WHERE bulletinsysuser.bulletinid = bulletin.bulletinid ) ");
        this.database.executePreparedUpdate("UPDATE profile SET ownerid = '(system)' WHERE ownerid IS NULL OR ownerid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("UPDATE sdiapprovalstep SET assignedto = '' WHERE assignedto IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String[] sysuserids = StringUtil.split(actionProps.getProperty("sysuserid"), ";");
        String[] jobtypeids = StringUtil.split(actionProps.getProperty("jobtypeid"), ";");
        if (sysuserids.length > 0 && sysuserids.length == jobtypeids.length) {
            for (int i = 0; i < sysuserids.length; ++i) {
                this.database.executePreparedUpdate("UPDATE sysuser SET lastjobtype='' WHERE sysuserid=? AND lastjobtype=?", new Object[]{sysuserids[i], jobtypeids[i]});
            }
        }
    }

    @Override
    public void postGenerateSnapshot(Snapshot snapshot, boolean isPackaging) throws SapphireException {
        if (snapshot == null) {
            return;
        }
        SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
        SDISnapshotItem item = sdiSnapshot.getSnapshotItem();
        SDIData sdiData = sdiSnapshot.getSDIData(item);
        String userId = item.getKeyId1();
        String sql = "SELECT * FROM sdcsecurity WHERE sysuserid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{userId});
        if (ds != null && ds.getRowCount() > 0) {
            sdiData.setDataset("sdcsecurity", ds);
        }
        sql = "SELECT * FROM profileproperty WHERE sysuserid = ? AND propertyid IN ('boauthenticationtype','bocmsname','boconnectiontimeout','bocrystaldateformat','bodeskidateformat','borootfoldername','bourl','bousername','bowebidateformat','desktoplogongroup','desktoplogonmenu','desktoplogonpageurl','notificationformat','phonelogongroup','phonelogonmenu','phonelogonpageurl','startuptype','tabletlogongroup','tabletlogonmenu','tabletlogonpageurl')";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{userId});
        if (ds != null && ds.getRowCount() > 0) {
            int findRow = ds.findRow("propertyid", "desktoplogonpageurl");
            if (ds.getString(findRow, "propertyvalue", "").indexOf("command=state") > -1) {
                ds.deleteRow(findRow);
            }
            if (ds.getString(findRow = ds.findRow("propertyid", "phonelogonpageurl"), "propertyvalue", "").indexOf("command=state") > -1) {
                ds.deleteRow(findRow);
            }
            if (ds.getString(findRow = ds.findRow("propertyid", "tabletlogonpageurl"), "propertyvalue", "").indexOf("command=state") > -1) {
                ds.deleteRow(findRow);
            }
            sdiData.setDataset("profileproperty", ds);
        }
    }

    @Override
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        SDIData templateData;
        DataSet sdcSecurity;
        if (actionProps.get("sdidata") != null && actionProps.get("sdidata") instanceof SDIData && (sdcSecurity = (templateData = (SDIData)actionProps.get("sdidata")).getDataset("sdcsecurity")) != null && sdcSecurity.getRowCount() > 0) {
            sdiData.setDataset("sdcsecurity", sdcSecurity.copy());
        }
        if (isAddSDI && actionProps.get("sdisnapshot") != null && actionProps.get("sdisnapshot") instanceof SDISnapshot) {
            SDISnapshot sourceSnapshot = (SDISnapshot)actionProps.get("sdisnapshot");
            SDISnapshotItem sourceSnapshotItem = (SDISnapshotItem)actionProps.get("sdisnapshotitem");
            DataSet primary = sdiData.getDataset("primary");
            CMTPolicy sourcePolicy = CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), "User", sourceSnapshotItem.getPolicyNodeId(), sourceSnapshot.getPolicyNodeMap());
            if (sourcePolicy.isImportUserAsDisabled()) {
                primary.setString(-1, "disabledflag", "Y");
                primary.setString(-1, "disabledreason", "Disabled on Import.");
                actionProps.setProperty("disabledflag", "Y");
                actionProps.setProperty("disabledreason", "Disabled on Import.");
            }
            String password = this.getDefaultPassword(-1, primary);
            primary.setString(-1, "password", password);
        }
    }

    @Override
    public void postCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet profileProperty = sdiData.getDataset("profileproperty");
        if (profileProperty != null && profileProperty.getRowCount() > 0) {
            DataSet addRows = new DataSet();
            DataSet editRows = new DataSet();
            if (isAddSDI) {
                addRows = profileProperty;
            } else {
                String sql = "SELECT * FROM profileproperty WHERE sysuserid IN ('" + primary.getColumnValues("sysuserid", "','") + "') AND propertyid NOT LIKE ('userconfig%')";
                DataSet existingProperties = this.getQueryProcessor().getSqlDataSet(sql);
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (int i = 0; i < profileProperty.getRowCount(); ++i) {
                    findMap.clear();
                    findMap.put("propertyid", profileProperty.getString(i, "propertyid"));
                    findMap.put("sysuserid", profileProperty.getString(i, "sysuserid"));
                    if (existingProperties.findRow(findMap) > -1) {
                        editRows.copyRow(profileProperty, i, 1);
                        continue;
                    }
                    addRows.copyRow(profileProperty, i, 1);
                }
            }
            PropertyList profileProps = new PropertyList();
            if (addRows.getRowCount() > 0) {
                this.updateProfileProps(addRows, true);
            }
            if (editRows.getRowCount() > 0) {
                this.updateProfileProps(editRows, false);
            }
        }
    }

    private void updateProfileProps(DataSet source, boolean isAdd) throws ActionException {
        PropertyList profileProps = new PropertyList();
        profileProps.setProperty("sdcid", "Profile");
        profileProps.setProperty("keyid1", "System");
        profileProps.setProperty("linkid", "profile properties");
        profileProps.setProperty("propertyid", source.getColumnValues("propertyid", ";"));
        profileProps.setProperty("sysuserid", source.getColumnValues("sysuserid", ";"));
        String propertyValues = source.getColumnValues("propertyvalue", "#*#@#!#");
        propertyValues = propertyValues.replaceAll(";", "#semicolon#");
        propertyValues = propertyValues.replaceAll("#*#@#1#", ";");
        profileProps.setProperty("propertyvalue", propertyValues);
        if (isAdd) {
            this.getActionProcessor().processAction("AddSDIDetail", "1", profileProps);
        } else {
            this.getActionProcessor().processAction("EditSDIDetail", "1", profileProps);
        }
    }

    private void copyBaseDepartment(DataSet primary, PropertyList actionProps) throws ActionException {
        boolean isUserCreatedFromTemplate = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1")).length() > 0;
        String sql = "SELECT departmentid FROM departmentsysuser WHERE sysuserid = ? AND departmentid = ?";
        for (int i = 0; i < primary.getRowCount(); ++i) {
            DataSet dsu;
            String namedUserFlag = primary.getString(i, "nameduserflag", this.getOldPrimaryValue(primary, i, "nameduserflag"));
            if (!TYPE_PORTALNAMEDUSER.equals(namedUserFlag) && !TYPE_PORTALCONCURRENTUSER.equals(namedUserFlag) || !this.hasPrimaryValueChanged(primary, i, "basedepartment") || primary.getString(i, "basedepartment", "").length() <= 0 || (dsu = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{primary.getString(i, "sysuserid"), primary.getString(i, "basedepartment")})).getRowCount() != 0) continue;
            PropertyList addSDIDetailProps = new PropertyList();
            addSDIDetailProps.setProperty("sdcid", "User");
            addSDIDetailProps.setProperty("linkid", "user departments");
            addSDIDetailProps.setProperty("sysuserid", primary.getString(i, "sysuserid"));
            addSDIDetailProps.setProperty("departmentid", primary.getString(i, "basedepartment"));
            addSDIDetailProps.setProperty("isUserCreatedFromTemplate", isUserCreatedFromTemplate ? "Y" : "N");
            this.getActionProcessor().processAction("AddSDIDetail", "1", addSDIDetailProps);
        }
    }

    private boolean isPortalAdminUser(String sysuserId) {
        String sql = "SELECT s.sysuserid, dsu.departmentid, dsu.portaladministratorflag FROM sysuser s, departmentsysuser dsu WHERE  dsu.sysuserid = s.sysuserid  and dsu.departmentid = s.BASEDEPARTMENT  AND dsu.portaladministratorflag = 'Y'  and s.sysuserid = ?  and s.nameduserflag IN ('P','Q')";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sysuserId});
        return ds.getRowCount() > 0;
    }

    private void addAppUserRole(String sysUserIds) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String userRoleSql = "SELECT u.sysuserid, 'App_User' roleid FROM sysuser u WHERE u.sysuserid IN (" + safeSQL.addIn(sysUserIds, ";") + ") AND u.nameduserflag IN ('" + TYPE_PORTALNAMEDUSER + "','" + TYPE_PORTALCONCURRENTUSER + "') AND (SELECT 1 FROM sysuserrole ur WHERE u.sysuserid = ur.sysuserid AND ur.roleid = 'App_User') IS NULL";
        DataSet userRoleDs = this.getQueryProcessor().getPreparedSqlDataSet(userRoleSql, safeSQL.getValues());
        if (!userRoleDs.isEmpty()) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("linkid", "user roles");
            props.setProperty("sysuserid", userRoleDs.getColumnValues("sysuserid", ";"));
            props.setProperty("roleid", userRoleDs.getColumnValues("roleid", ";"));
            try {
                this.getActionProcessor().processAction("AddSDIDetail", "1", props);
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Failed to add App_User role for Portal users: " + userRoleDs.getColumnValues("sysuserid", ","), e);
            }
        }
    }
}

