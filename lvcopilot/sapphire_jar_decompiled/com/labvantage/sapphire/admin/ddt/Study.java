/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.rules.AmbiguousSubjectRule;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.ddt.rules.COCRule;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.admin.ddt.rules.HipaaRule;
import com.labvantage.sapphire.admin.ddt.rules.StudyHasProtocolRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Study
extends BaseSDCRules {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 86903 $";
    public static final String SDC = "Study";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        int i;
        DataSet primary = sdiData.getDataset("primary");
        DataSet approvalstep = sdiData.getDataset("approvalstep");
        if (!primary.isValidColumn("clinicalflag ")) {
            primary.addColumn("clinicalflag", 0);
        }
        for (i = 0; i < primary.size(); ++i) {
            if (!"Y".equalsIgnoreCase(primary.getValue(i, "templateflag")) && primary.getValue(i, "studyalias", "").trim().length() > 0) {
                String templateid;
                String string = templateid = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
                if (templateid != null && templateid.trim().length() > 0 && actionProps.getProperty("studyalias").trim().length() == 0) {
                    primary.setValue(i, "studyalias", "");
                    continue;
                }
                DataSet countds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT count(s_studyid) studycount FROM s_study where studyalias = ?", (Object[])new String[]{primary.getString(i, "studyalias", "")});
                if (countds != null && countds.size() > 0 && countds.getInt(0, "studycount") > 0) {
                    throw new SapphireException("Duplicate Study Code", "VALIDATION", this.getTranslationProcessor().translate("Another study found with same Study Code. Please choose another code for this Study."));
                }
            }
            if (primary.getValue(i, "templateflag").equalsIgnoreCase("Y")) continue;
            if (!primary.isValidColumn("studystatus")) {
                primary.addColumn("studystatus", 0);
            }
            primary.setValue(i, "studystatus", approvalstep == null || approvalstep.size() == 0 ? "Active" : "Initial");
        }
        if (this.connectionInfo.hasModule("SMS") && actionProps.getProperty("templateid").length() == 0 && BaseBioBankRule.isRuleActive("Study Has Protocol Rule", this.getConfigurationProcessor())) {
            for (i = 0; i < primary.size(); ++i) {
                String protocol;
                String studytype = primary.getValue(i, "studytype");
                if (!"Clinical".equals(studytype) || "Y".equalsIgnoreCase(primary.getValue(i, "templateflag", "N")) || (protocol = primary.getValue(i, "protocolname")) != null && protocol.trim().length() != 0) continue;
                throw new SapphireException("StudyHasProtocolRule", "VALIDATION", this.getTranslationProcessor().translate("Study of type 'Clinical' must have 'External Protocol Id' or the 'Study Has Protocol Rule' must be deactivated in the biobanking policy"));
            }
        }
        this.setClinicalStudyActiveFlag(primary);
    }

    private void setClinicalStudyActiveFlag(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            String clinicalFlag = primary.getValue(i, "clinicalflag", "N");
            String activeFlag = primary.getValue(i, "activeflag", "N");
            if ("Y".equalsIgnoreCase(clinicalFlag)) {
                primary.addColumn("activeflag", 0);
                primary.setValue(i, "activeflag", activeFlag);
                continue;
            }
            primary.setValue(i, "clinicalflag", clinicalFlag);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.copyBioBankingPolicies(primary);
        this.copyStudyDetails(primary.getColumnValues("s_studyid", ";"), actionProps);
    }

    private void copyStudyDetails(String keyid1, PropertyList actionProps) throws SapphireException {
        String templateid;
        String string = templateid = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        if (templateid != null && templateid.trim().length() > 0) {
            String[] studyIdArr;
            ActionBlock actionBlock = new ActionBlock();
            Object[] templateKey = new String[]{templateid};
            DataSet clinicalEventsDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_clinicaleventid FROM s_clinicalevent WHERE sstudyid=?", templateKey);
            DataSet studysiteDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_studysiteid FROM s_studysite WHERE sstudyid=?", templateKey);
            DataSet restclassDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_restrictclassid FROM s_restrictclass WHERE sstudyid=?", templateKey);
            DataSet protocolDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision,versionstatus from s_clinicalprotocol WHERE sstudyid=?", templateKey);
            for (String aStudyIdArr : studyIdArr = StringUtil.split(keyid1, ";")) {
                this.copyProtocolDetails(protocolDS, aStudyIdArr, actionBlock);
                this.addToActionBlock(clinicalEventsDS, aStudyIdArr, actionBlock, "LV_ClinicalEvnt", "s_clinicaleventid");
                this.addToActionBlock(studysiteDS, aStudyIdArr, actionBlock, "LV_StudySite", "s_studysiteid");
                this.addToActionBlock(restclassDS, aStudyIdArr, actionBlock, "LV_RestClass", "s_restrictclassid");
            }
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    private void addToActionBlock(DataSet detailDS, String studyid, ActionBlock actionBlock, String sdcid, String keyid1) throws SapphireException {
        if (detailDS != null && detailDS.size() > 0) {
            for (int i = 0; i < detailDS.size(); ++i) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("templateid", detailDS.getValue(i, keyid1));
                props.setProperty("sstudyid", studyid);
                actionBlock.setAction("AddSDI" + sdcid + studyid + (i + 1), "AddSDI", "1", props);
            }
        }
    }

    private void copyProtocolDetails(DataSet detailDS, String studyid, ActionBlock actionBlock) throws SapphireException {
        if (detailDS != null && detailDS.size() > 0) {
            for (int i = 0; i < detailDS.size(); ++i) {
                PropertyList props = new PropertyList();
                props.setProperty("clinicalstudyid", studyid);
                props.setProperty("clinicalprotocolid", detailDS.getValue(i, "s_clinicalprotocolid"));
                props.setProperty("clinicalprotocolversionid", detailDS.getValue(i, "s_clinicalprotocolversionid"));
                props.setProperty("clinicalprotocolrevision", detailDS.getValue(i, "s_clinicalprotocolrevision"));
                props.setProperty("versionstatus", detailDS.getValue(i, "versionstatus"));
                props.setProperty("copyprotocolflag", "Y");
                actionBlock.setAction("CreateProtocol" + studyid + (i + 1), "CreateProtocol", "1", props);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (this.connectionInfo.hasModule("SMS")) {
            boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
            DataSet primary = sdiData.getDataset("primary");
            this.checkGLPRule(primary, forceUpdate);
            this.checkCOCRule(primary, forceUpdate);
            this.checkAmbiguousSubjectRule(primary);
            this.checkStudyHasProtocolRule(primary, forceUpdate);
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "hipaaflag") || !"Y".equals(primary.getString(i, "hipaaflag"))) continue;
                try {
                    new HipaaRule(this.database, this.connectionInfo).processRule(primary.getString(i, "s_studyid"), forceUpdate);
                    continue;
                }
                catch (Exception e) {
                    this.setError("HIPAA Rule", "CONFIRM", e.getMessage());
                }
            }
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setActiveFlag(primary);
        for (int i = 0; i < primary.size(); ++i) {
            DataSet countds;
            if (!"Y".equalsIgnoreCase(primary.getValue(i, "templateflag")) && primary.getString(i, "studyalias", "").trim().length() > 0 && (countds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT count(s_studyid) studycount FROM s_study where studyalias = ? and s_studyid != ?", (Object[])new String[]{primary.getString(i, "studyalias", ""), primary.getString(i, "s_studyid")})) != null && countds.size() > 0 && countds.getInt(0, "studycount") > 0) {
                throw new SapphireException("Duplicate Study Code", "VALIDATION", this.getTranslationProcessor().translate("Another study found with same Study Code. Please choose another code for this Study."));
            }
            String clinicalFlag = primary.getValue(i, "clinicalflag", "N");
            primary.setValue(i, "clinicalflag", clinicalFlag);
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet ds;
        if (this.connectionInfo.hasModule("SMS") && (ds = sdiData.getDataset("s_studysampleapproval")) != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String rulename;
                String studyid = ds.getValue(i, "s_studyid");
                if (this.database.getPreparedCount("select count(s_studyid) study from s_study where s_studyid = ? and studytype = 'Externally Submitted'", new Object[]{studyid}) <= 0 || !"Subject information required".equals(rulename = ds.getValue(i, "rulename")) && !"Collection date required".equals(rulename)) continue;
                ds.setValue(i, "fullapprovalflag", "N");
                ds.setValue(i, "conditionalapprovalflag", "N");
            }
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet ds;
        if (this.connectionInfo.hasModule("SMS") && (ds = sdiData.getDataset("s_studysampleapproval")) != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String studyid = ds.getValue(i, "s_studyid");
                String rulename = ds.getValue(i, "rulename");
                if (this.database.getPreparedCount("select count(s_studyid) study from s_study where s_studyid = ? and studytype = 'Externally Submitted'", new Object[]{studyid}) > 0 && ("Subject information required".equals(rulename) || "Collection Date required".equals(rulename)) && ("Y".equals(ds.getValue(i, "fullapprovalflag")) || "Y".equals(ds.getValue(i, "conditionalapprovalflag")))) {
                    throw new SapphireException("<b>" + rulename + "</b> :: " + this.getTranslationProcessor().translate("This Approval can not require Full or Conditional Approval for externally submitted studies."));
                }
                if (!"Subject information required".equals(rulename) || !"Y".equals(ds.getValue(i, "fullapprovalflag")) && !"Y".equals(ds.getValue(i, "conditionalapprovalflag")) || this.database.getPreparedCount("select count(s_samplefamilyid) from s_samplefamily where sstudyid = ? and (subjectid is null or subjectid = '')", new Object[]{studyid}) <= 0) continue;
                throw new SapphireException("<b>" + rulename + "</b> :: " + this.getTranslationProcessor().translate("This Approval can not require Full or Conditional Approval as some of the Samples for this Study needs Subject information."));
            }
        }
    }

    private void setActiveFlag(DataSet primary) {
        String currentUser = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "studystatus")) continue;
            String studystatus = primary.getValue(i, "studystatus", "");
            primary.addColumn("activeflag", 0);
            if (studystatus.equalsIgnoreCase("Active")) {
                primary.setString(i, "activeflag", "Y");
                continue;
            }
            if (studystatus.equalsIgnoreCase("Approved")) {
                primary.setString(i, "studystatus", "Active");
                primary.setString(i, "activeflag", "Y");
                continue;
            }
            primary.setString(i, "activeflag", "N");
            if (studystatus.equalsIgnoreCase("Completed")) {
                primary.addColumn("completedby", 0);
                primary.addColumn("completeddt", 2);
                primary.setString(i, "completedby", currentUser);
                primary.setDate(i, "completeddt", Calendar.getInstance());
                continue;
            }
            if (!studystatus.equalsIgnoreCase("Cancelled")) continue;
            primary.addColumn("cancelledby", 0);
            primary.addColumn("cancelleddt", 2);
            primary.setString(i, "cancelledby", currentUser);
            primary.setDate(i, "cancelleddt", Calendar.getInstance());
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (this.isSampleExistsInStudy(rsetid)) {
            throw new SapphireException("Study Has Sample(s)", "VALIDATION", "There are samples associated to the selected Study(s)");
        }
        DataSet ds = this.getProtocolsOfTheStudy(rsetid, actionProps.getProperty("keyid1"));
        if (ds != null && ds.size() > 0) {
            String clinicalprotocolid = ds.getColumnValues("s_clinicalprotocolid", ";");
            String clinicalprotocolrevision = ds.getColumnValues("s_clinicalprotocolrevision", ";");
            if (StringUtil.getLen(clinicalprotocolid) > 0L && StringUtil.getLen(clinicalprotocolrevision) > 0L) {
                PropertyList props = new PropertyList();
                props.setProperty("clinicalprotocolid", clinicalprotocolid);
                props.setProperty("clinicalprotocolrevision", clinicalprotocolrevision);
                this.getActionProcessor().processAction("DeleteProtocolRev", "1", props);
            }
        }
    }

    boolean isSampleExistsInStudy(String rsetid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT count(1) count from s_sample WHERE sstudyid IN ( SELECT keyid1 FROM rsetitems  WHERE rsetid = " + safeSQL.addVar(rsetid) + " )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getInt(i, "count") <= 0) continue;
                return true;
            }
        }
        return false;
    }

    private DataSet getProtocolsOfTheStudy(String rsetid, String studyid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = StringUtil.split(studyid, ";").length > 750 ? "SELECT s_clinicalprotocolid, s_clinicalprotocolrevision from s_clinicalprotocol WHERE sstudyid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "SELECT s_clinicalprotocolid, s_clinicalprotocolrevision from s_clinicalprotocol WHERE sstudyid IN ( " + safeSQL.addIn(studyid, ";") + " )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds;
    }

    private void checkGLPRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("GLP Rule", this.getConfigurationProcessor())) {
            GLPRule rule = new GLPRule(this.database, this.connectionInfo);
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "defaultglpflag") || !"N".equals(primary.getValue(i, "defaultglpflag"))) continue;
                rule.processStudyGLPRule(primary.getString(i, "s_studyid"), forceUpdate);
            }
        }
    }

    private void checkStudyHasProtocolRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("Study Has Protocol Rule", this.getConfigurationProcessor())) {
            StringBuffer problems = new StringBuffer();
            StudyHasProtocolRule rule = new StudyHasProtocolRule(this.database, this.connectionInfo);
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "studytype") && !this.hasPrimaryValueChanged(primary, i, "protocolname")) continue;
                String studyid = primary.getString(i, "s_studyid");
                try {
                    rule.processRule(studyid, forceUpdate);
                    continue;
                }
                catch (SapphireException ex) {
                    if (problems.length() > 0) {
                        problems.append(", ");
                    }
                    problems.append(studyid);
                }
            }
            if (problems.length() > 0) {
                this.setError(rule.getClass().getName(), "VALIDATION", "Studies (" + problems + ") are of type 'Clinical'. They should have 'External Protocol Id'.");
            }
        }
    }

    private void checkCOCRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        COCRule rule = new COCRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "conservativecocflag") && !this.hasPrimaryValueChanged(primary, i, "conservativerestrictionsflag")) continue;
            ArrayList familyList = Study.getSampleFamilyListWithNoRC(this.database, primary.getString(i, "s_studyid"));
            rule.processRule(familyList, 1, forceUpdate);
        }
    }

    private void checkAmbiguousSubjectRule(DataSet primary) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("Ambiguous Subject Rule", this.getConfigurationProcessor())) {
            AmbiguousSubjectRule rule = new AmbiguousSubjectRule(this.database, this.connectionInfo);
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "protocolname")) continue;
                String studyid = primary.getString(i, "s_studyid");
                ArrayList families = Study.getSampleFamilyList(this.database, studyid);
                try {
                    rule.processRule(families);
                    continue;
                }
                catch (SapphireException e) {
                    this.setError(rule.getClass().getName(), "CONFIRM", e.getMessage());
                }
            }
        }
    }

    public static boolean isHipaa(DBAccess database, String studyid) throws SapphireException {
        return database.getPreparedCount("SELECT count(*) FROM s_study WHERE s_studyid=? AND hipaaflag='Y'", new Object[]{studyid}) > 0;
    }

    public static boolean isGLP(QueryProcessor qp, String studyid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT count(s_studyid) count FROM s_study WHERE s_studyid=" + safeSQL.addVar(studyid) + " AND defaultglpflag='Y'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds != null && ds.size() > 0 && ds.getInt(0, "count") > 0;
    }

    public static String getStudyType(DBAccess database, String studyid) throws SapphireException {
        String studyType = "";
        String sql = "SELECT studytype FROM s_study WHERE s_studyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        if (database.getNext()) {
            studyType = database.getString("studytype");
        }
        database.closeResultSet();
        return studyType;
    }

    public static boolean isProtocolDriven(DBAccess database, String studyid) throws SapphireException {
        String clinicalflag = "";
        String sql = "SELECT clinicalflag FROM s_study WHERE s_studyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        if (database.getNext()) {
            clinicalflag = database.getString("clinicalflag");
        }
        database.closeResultSet();
        return "Y".equals(clinicalflag);
    }

    public static boolean isProtocolDriven(QueryProcessor queryProcessor, String studyid) {
        return "Y".equals(OpalUtil.getColumnValue(queryProcessor, "s_study", "clinicalflag", "s_studyid = ?", new String[]{studyid}));
    }

    public static String getProtocolName(DBAccess database, String studyid) throws SapphireException {
        String protocolName = "";
        String sql = "SELECT protocolname FROM s_study WHERE s_studyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        if (database.getNext()) {
            protocolName = database.getString("protocolname");
        }
        database.closeResultSet();
        return protocolName;
    }

    public static ArrayList getSubjectList(DBAccess database, String studyid) throws SapphireException {
        ArrayList<String> subjects = new ArrayList<String>();
        String sql = "SELECT distinct subjectid FROM s_samplefamily WHERE sstudyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        while (database.getNext()) {
            subjects.add(database.getString("subjectid"));
        }
        database.closeResultSet();
        return subjects;
    }

    public static ArrayList getSampleList(DBAccess database, String studyid) throws SapphireException {
        ArrayList<String> samples = new ArrayList<String>();
        String sql = "SELECT s_sampleid FROM s_sample WHERE sstudyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        while (database.getNext()) {
            samples.add(database.getString("s_sampleid"));
        }
        database.closeResultSet();
        return samples;
    }

    public static ArrayList getSampleFamilyList(DBAccess database, String studyid) throws SapphireException {
        return Study.getSampleFamilyList(database, studyid, false);
    }

    public static ArrayList getSampleFamilyListWithNoRC(DBAccess database, String studyid) throws SapphireException {
        return Study.getSampleFamilyList(database, studyid, true);
    }

    private static ArrayList getSampleFamilyList(DBAccess database, String studyid, boolean noRC) throws SapphireException {
        ArrayList<String> sampleFamilies = new ArrayList<String>();
        String sql = "SELECT s_samplefamilyid FROM s_samplefamily WHERE sstudyid=?" + (noRC ? " AND (restrictclassid is null or restrictclassid = '')" : "");
        database.createPreparedResultSet(sql, new Object[]{studyid});
        while (database.getNext()) {
            sampleFamilies.add(database.getString("s_samplefamilyid"));
        }
        database.closeResultSet();
        return sampleFamilies;
    }

    public static boolean hasRequireLinkToPatient(DBAccess database, String studyid) throws SapphireException {
        return database.getPreparedCount("SELECT count(*) FROM s_study where s_studyid = ? and subjectrequiredflag='Y'", new Object[]{studyid}) > 0;
    }

    public static boolean hasCollectionInformation(DBAccess database, String studyid) throws SapphireException {
        return database.getPreparedCount("SELECT count(*) FROM s_study where s_studyid = ? and collectinforequiredflag='Y'", new Object[]{studyid}) > 0;
    }

    public static boolean hasClinicalEvents(DBAccess database, String studyid) throws SapphireException {
        return database.getPreparedCount("SELECT count(*) FROM s_clinicalevent where sstudyid = ?", new Object[]{studyid}) > 0;
    }

    public static boolean hasClinicalSites(DBAccess database, String studyid) throws SapphireException {
        return database.getPreparedCount("SELECT count(*) FROM s_studysite where sstudyid = ?", new Object[]{studyid}) > 0;
    }

    public static void setConsCOCDR(ConnectionInfo connectionInfo, String studyid, boolean coc, boolean dr, boolean forceUpdate) throws SapphireException {
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDC);
        props.put("keyid1", studyid);
        props.put("conservativecocflag", coc ? "Y" : "N");
        props.put("conservativerestrictionsflag", dr ? "Y" : "N");
        props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        ap.processAction("EditSDI", "1", props);
    }

    public static boolean getConsCOC(DBAccess database, String studyid) throws SapphireException {
        boolean coc = false;
        String sql = "SELECT conservativecocflag FROM s_study WHERE s_studyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        if (database.getNext()) {
            String s = database.getString("conservativecocflag");
            coc = s != null && s.equals("Y");
        }
        database.closeResultSet();
        return coc;
    }

    public static boolean getConsDR(DBAccess database, String studyid) throws SapphireException {
        boolean dr = false;
        String sql = "SELECT conservativerestrictionsflag FROM s_study WHERE s_studyid=?";
        database.createPreparedResultSet(sql, new Object[]{studyid});
        if (database.getNext()) {
            String s = database.getString("conservativerestrictionsflag");
            dr = s != null && s.equals("Y");
        }
        database.closeResultSet();
        return dr;
    }

    public static boolean hasRestClassWithCOC(DBAccess database, String studyid) throws SapphireException {
        return database.checkPreparedExists("SELECT cocflag FROM s_restrictclass WHERE sstudyid = ? AND cocflag = 'Y' AND activeflag = 'Y'", new Object[]{studyid});
    }

    public static boolean hasRestClassWithDR(DBAccess database, String studyid) throws SapphireException {
        return database.checkPreparedExists("SELECT restrictionsflag FROM s_restrictclass WHERE sstudyid = ? AND restrictionsflag = 'Y' AND activeflag = 'Y'", new Object[]{studyid});
    }

    public static boolean hasActiveRC(DBAccess database, String studyid) throws SapphireException {
        return database.checkPreparedExists("SELECT s_restrictclassid FROM s_restrictclass WHERE sstudyid = ? AND activeflag = 'Y'", new Object[]{studyid});
    }

    @Override
    public void postApprove(DataSet dsApproval) throws SapphireException {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String studyStatus = "Pass".equalsIgnoreCase(approvalFlag) ? "Active" : "Rejected";
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, "studystatus", studyStatus);
            }
            if (dsProp.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC);
                props.setProperty("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.setProperty("studystatus", dsProp.getColumnValues("studystatus", ";"));
                props.setProperty("tracelogid", dsApproval.getString(0, "tracelogid", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    private void copyBioBankingPolicies(DataSet primary) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        if (policy != null) {
            try {
                HashMap<String, String> studymap = new HashMap<String, String>();
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select s_studyid, clinicalflag,");
                sql.append(" (select count(ssa.s_studyid) from s_studysampleapproval ssa where ssa.s_studyid = s_study.s_studyid) rulecount");
                sql.append(" from s_study");
                sql.append(" where s_studyid in (").append(safeSQL.addIn(primary.getColumnValues("s_studyid", "','"))).append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    for (int i = 0; i < ds.size(); ++i) {
                        if (ds.getInt(i, "rulecount") != 0) continue;
                        studymap.put(ds.getValue(i, "s_studyid"), ds.getValue(i, "clinicalflag", "N"));
                    }
                }
                if (studymap.size() > 0) {
                    PreparedStatement insertStudySampleApproval = this.database.prepareStatement("insertStudySampleApproval", "INSERT INTO s_studysampleapproval (s_studyid, rulename, fullapprovalflag, conditionalapprovalflag) values( ?, ?, ?, ? )");
                    PropertyListCollection approvalrules = policy.getCollectionNotNull("sampleapprovalrules");
                    for (int i = 0; i < approvalrules.size(); ++i) {
                        PropertyList approvalrule = approvalrules.getPropertyList(i);
                        boolean active = "Y".equals(approvalrule.getProperty("active", "N"));
                        if (!active) continue;
                        String rulename = approvalrule.getProperty("rulename");
                        String studytype = approvalrule.getProperty("studytype", "Both");
                        String fullapproval = approvalrule.getProperty("fullapproval", "N");
                        String conditionalapproval = approvalrule.getProperty("conditionalapproval", "N");
                        for (String studyid : studymap.keySet()) {
                            String clinicalflag = (String)studymap.get(studyid);
                            boolean addrule = false;
                            if ("Both".equals(studytype)) {
                                addrule = true;
                            } else if ("Protocol driven only".equals(studytype) && "Y".equalsIgnoreCase(clinicalflag)) {
                                addrule = true;
                            } else if ("Non-Protocol driven only".equals(studytype) && !"Y".equalsIgnoreCase(clinicalflag)) {
                                addrule = true;
                            }
                            if (!addrule) continue;
                            insertStudySampleApproval.setString(1, studyid);
                            insertStudySampleApproval.setString(2, rulename);
                            insertStudySampleApproval.setString(3, fullapproval);
                            insertStudySampleApproval.setString(4, conditionalapproval);
                            insertStudySampleApproval.executeUpdate();
                        }
                    }
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Study: PostAdd", "Error copying policies: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

