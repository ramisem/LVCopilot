/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.wap.RemoveActivityWorkSDI;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostDeleteEventObject;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.SecuritySetUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DeleteSDI
extends BaseSDIAction
implements sapphire.action.DeleteSDI,
DDTConstants {
    private boolean runOld;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String old = properties.getProperty("old");
        this.runOld = "Y".equalsIgnoreCase(old);
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
        if (sdc == null) {
            throw new SapphireException("GET_SDCDATA_FAILED", "Failed to get meta data for sdc " + sdcid);
        }
        sdcid = sdc.getProperty("sdcid");
        String tableid = sdc.getProperty("tableid");
        int keycols = Integer.parseInt(sdc.getProperty("keycolumns"));
        String keycolid1 = sdc.getProperty("keycolid1");
        String keycolid2 = sdc.getProperty("keycolid2");
        String keycolid3 = sdc.getProperty("keycolid3");
        String keyid1list = properties.getProperty("keyid1");
        String keyid2list = properties.getProperty("keyid2");
        String keyid3list = properties.getProperty("keyid3");
        String rsetid = "";
        DAMProcessor dam = this.getDAMProcessor();
        boolean applylock = properties.getProperty("applylock").equals("Y");
        if (applylock) {
            if (StringUtil.split(keyid1list, ";").length > 2000) {
                throw new SapphireException("DB_ACTION_FAILED", "Failed to delete sdi. You cannot delete with locks more than 2000 items at a time.");
            }
            rsetid = dam.createRSetDS(sdcid, keyid1list, keyid2list, keyid3list, "", "", "", "", false, true, false);
            if (rsetid.length() > 0) {
                rsetid = dam.lockRSet(rsetid);
            }
        } else {
            rsetid = dam.createRSet(sdcid, keyid1list, keyid2list, keyid3list);
        }
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Could not create RSET for delete");
        }
        this.checkWorksheetReferences(rsetid);
        if (sdc.getProperty("componentableflag").equals("Y") && !Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
            this.database.createPreparedResultSet("SELECT compcode FROM " + tableid + ", rsetitems WHERE " + tableid + "." + keycolid1 + " = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
            while (this.database.getNext()) {
                String primaryCompcode = this.database.getValue("compcode");
                if (primaryCompcode.length() <= 0) continue;
                if (compcode.length() > 0) {
                    if (compcode.equals(primaryCompcode)) continue;
                    throw new SapphireException("You cannot delete a component " + sdc.getProperty("singular") + " unless you are in that component development mode.");
                }
                throw new SapphireException("You cannot delete a component " + sdc.getProperty("singular") + " unless you are in that component development mode.");
            }
        }
        String sql2 = "SELECT mv.* FROM rsetitems r, (  SELECT changelogid, changelogstatus, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3,  Row_Number () OVER (PARTITION BY linksdcid,linkkeyid1,linkkeyid2,linkkeyid3 ORDER BY moddt DESC) row_num  FROM changelog" + ("PropertyTree".equals(sdcid) ? " WHERE propertytreenodeid = '__FULL'" : "") + " ) mv WHERE mv.row_num = 1 AND r.rsetid = ? AND r.sdcid = mv.linksdcid AND r.keyid1 = mv.linkkeyid1 AND r.keyid2 = mv.linkkeyid2 AND r.keyid3 = mv.linkkeyid3";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, (Object[])new String[]{rsetid});
        if (ds != null && ds.getRowCount() > 0) {
            BaseSDCRules[] props;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("changelogstatus", "Checked Out");
            DataSet checkInMarkDeleteDS = ds.getFilteredDataSet(filter);
            if (checkInMarkDeleteDS != null && checkInMarkDeleteDS.size() > 0) {
                props = new PropertyList();
                props.setProperty("changelogid", checkInMarkDeleteDS.getColumnValues("changelogid", ";"));
                props.setProperty("deleteflag", "Y");
                props.setProperty("auditreason", "SDI Deleted by " + this.connectionInfo.getSysuserId());
                this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), (PropertyList)props);
            }
            props = new PropertyList();
            props.setProperty("sdcid", "LV_ChangeLog");
            props.setProperty("keyid1", ds.getColumnValues("changelogid", ";"));
            props.setProperty("changelogstatus", "Deleted");
            props.setProperty("auditreason", "SDI Deleted by " + this.connectionInfo.getSysuserId());
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), (PropertyList)props);
        }
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PreDelete");
        Trace.startBusinessRule(sdcid + "." + "PreDelete", true);
        sdcPreRules.preDelete(rsetid, properties);
        Trace.endBusinessRule(sdcid + "." + "PreDelete", true);
        Trace.startBusinessRule(sdcid + "." + "PreDelete", false);
        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
            customRules.preDelete(rsetid, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PreDelete", false);
        sdcPreRules.endRule();
        try {
            String sql;
            String keyclause;
            CharSequence keyclause1;
            SafeSQL safeSQLKeyClause = new SafeSQL();
            if (this.connectionInfo.isOracle()) {
                keyclause1 = new StringBuffer(keycolid1);
                StringBuffer keyclause2 = new StringBuffer("keyid1");
                if (keycols > 1) {
                    ((StringBuffer)keyclause1).append(",").append(keycolid2);
                    keyclause2.append(", keyid2");
                }
                if (keycols > 2) {
                    ((StringBuffer)keyclause1).append(",").append(keycolid3);
                    keyclause2.append(", keyid3");
                }
                keyclause = "(" + ((StringBuffer)keyclause1).toString() + ") IN (SELECT " + keyclause2.toString() + " FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + ")";
            } else {
                keyclause1 = keycolid1 + " IN ( SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " )";
                if (keycols == 2) {
                    keyclause1 = (String)keyclause1 + " AND " + keycolid1 + " + ';' + " + keycolid2 + " IN ( SELECT keyid1 + ';' + keyid2 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " ) ";
                } else if (keycols == 3) {
                    keyclause1 = (String)keyclause1 + " AND " + keycolid1 + " + ';' + " + keycolid2 + " + ';' + " + keycolid3 + " IN ( SELECT keyid1 + ';' + keyid2 + ';' + keyid3 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " ) ";
                }
                keyclause = "(" + (String)keyclause1 + ")";
            }
            PropertyListCollection sdcdetaillinks = sdc.getCollection("detaillinks");
            for (int i = sdcdetaillinks.size() - 1; i >= 0; --i) {
                PropertyList link = sdcdetaillinks.getPropertyList(i);
                String linktype = link.getProperty("linktype");
                String linktableid = link.getProperty("linktableid");
                if (!linktype.equals("D") || linktableid.length() <= 0) continue;
                String sql3 = "DELETE FROM " + linktableid + " WHERE " + keyclause;
                this.logger.info("Deleting SDCDETAILLINK D Links: " + sql3);
                this.database.executePreparedUpdate(sql3, safeSQLKeyClause.getValues());
            }
            PropertyListCollection sdclinks = sdc.getCollection("links");
            for (int i = sdclinks.size() - 1; i >= 0; --i) {
                PropertyList link = sdclinks.getPropertyList(i);
                String linktype = link.getProperty("linktype");
                String linktableid = link.getProperty("linktableid");
                if (linktableid.length() <= 0 || !linktype.equals("D") && !linktype.equals("M") || linktableid.equals("systable") || linktableid.equals("syscolumn") || linktableid.equals("syscolumnproperty")) continue;
                sql = "DELETE FROM " + linktableid + " WHERE " + keyclause;
                this.logger.info("Deleting SDCLINK " + linktype + " Links: " + sql);
                this.database.executePreparedUpdate(sql, safeSQLKeyClause.getValues());
            }
            PropertyListCollection reverseLinks = sdc.getCollection("reverselinks");
            for (int link = 0; link < reverseLinks.size(); ++link) {
                PropertyList reverseLink = reverseLinks.getPropertyList(link);
                String linktype = reverseLink.getProperty("linktype");
                if (linktype.equals("F")) {
                    this.resolveReverseFK(properties, sdcid, keycols, rsetid, reverseLink, false);
                    continue;
                }
                if (!linktype.equals("M")) continue;
                String linktableid = reverseLink.getProperty("linktableid");
                String sql4 = "DELETE FROM " + linktableid + " WHERE " + keycolid1 + " IN  (SELECT keyid1 FROM rsetitems WHERE rsetid=?)";
                this.logger.info("Updating reverse MM links: " + sql4);
                this.database.executePreparedUpdate(sql4, new Object[]{rsetid});
            }
            PropertyListCollection reverseDetailLinks = sdc.getCollection("reversedetaillinks");
            for (int link = 0; link < reverseDetailLinks.size(); ++link) {
                PropertyList reverseLink = reverseDetailLinks.getPropertyList(link);
                String linktype = reverseLink.getProperty("linktype");
                if (!linktype.equals("F")) continue;
                this.resolveReverseFK(properties, sdcid, keycols, rsetid, reverseLink, true);
            }
            DataSet dsOwnedSecuritySets = null;
            if ("S".equalsIgnoreCase(sdc.getProperty("accesscontrolledflag"))) {
                dsOwnedSecuritySets = this.getOwnedSecuritySets(rsetid, sdcid, keycols);
            }
            SafeSQL safeSQL = new SafeSQL();
            sql = this.connectionInfo.getDbms().equals("ORA") ? "DELETE FROM sdirole WHERE ( sdcid, keyid1 ) IN (SELECT sdcid, keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "DELETE FROM sdirole WHERE \tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") ";
            this.logger.info("Deleting from roles: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = this.connectionInfo.getDbms().equals("ORA") ? "DELETE FROM scheduleevent where ( scheduleplanid, scheduleplanitemid ) in ( SELECT scheduleplanid, scheduleplanitemid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " )" : "DELETE FROM scheduleevent where scheduleplanid in ( SELECT scheduleplanid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " ) AND scheduleplanitemid in ( SELECT scheduleplanitemid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " )";
            this.logger.info("Deleting from scheduleevent: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = this.connectionInfo.getDbms().equals("ORA") ? "DELETE FROM scheduleplanitemexec where ( scheduleplanid, scheduleplanitemid ) in ( SELECT scheduleplanid, scheduleplanitemid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " )" : "DELETE FROM scheduleplanitemexec where scheduleplanid in ( SELECT scheduleplanid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " ) AND scheduleplanitemid in ( SELECT scheduleplanitemid FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL) + " )";
            this.logger.info("Deleting from scheduleplanitemexec: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM scheduleplanitem where " + this.getLinkSDCClause("scheduleplanitem", rsetid, safeSQL);
            this.logger.info("Deleting from scheduleplanitem: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "UPDATE scheduleplannode set refsdcid=null, refkeyid1=null, refkeyid2=null, refkeyid3=null where " + this.getRefSDCClause("scheduleplannode", rsetid, safeSQL);
            this.logger.info("Updating scheduleplannode: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sysuserfolderitem where " + this.getLinkSDCClause("sysuserfolderitem", rsetid, safeSQL);
            this.logger.info("Updating sysuserfolderitem: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = this.connectionInfo.isOracle() ? "DELETE FROM categoryitem WHERE ( sdcid, keyid1 ) IN (SELECT sdcid, keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "DELETE from categoryitem WHERE \tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") ";
            this.logger.info("Deleting from categories: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "SELECT * FROM sdiattachment WHERE " + this.getLinkSDCClause("sdiattachment", rsetid, safeSQL);
            DataSet attachments = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            this.logger.info("Deleting child attachment files");
            try {
                this.deleteSDIAttachments(attachments, properties);
            }
            catch (Exception e) {
                this.logger.error("Error occurred when deleting physical child attachment file: " + e.getMessage());
            }
            safeSQL.reset();
            sql = "DELETE FROM sdiattachment WHERE " + this.getLinkSDCClause("sdiattachment", rsetid, safeSQL);
            this.logger.info("Deleting child from attachments: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiattribute  WHERE sdiattribute.sdcid = 'SDIAttachment' AND EXISTS (SELECT 1 FROM sdiattachment s, rsetitems r  WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND r.rsetid = " + safeSQL.addVar(rsetid) + " AND s.sdiattachmentid = sdiattribute.keyid1 )";
            this.logger.info("Deleting sdiattachment attributes from sdiattribute: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "SELECT * FROM sdiattachment WHERE " + this.getSDCClause("sdiattachment", rsetid, safeSQL);
            attachments = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            this.logger.info("Deleting attachment files");
            try {
                this.deleteSDIAttachments(attachments, properties);
            }
            catch (Exception e) {
                this.logger.error("Error occurred when deleting physical attachment file: " + e.getMessage());
            }
            safeSQL.reset();
            sql = "DELETE FROM sdiattachment WHERE " + this.getSDCClause("sdiattachment", rsetid, safeSQL);
            this.logger.info("Deleting from attachments: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidataitemspec WHERE " + this.getSDCClause("sdidataitemspec", rsetid, safeSQL);
            this.logger.info("Deleting from sdidataitemspec: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidataitemlimits WHERE " + this.getSDCClause("sdidataitemlimits", rsetid, safeSQL);
            this.logger.info("Deleting from sdidataitemlimits: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiattribute  WHERE sdiattribute.sdcid = 'DataItem' AND EXISTS (SELECT 1 FROM sdidataitem s, rsetitems r  WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND r.rsetid = " + safeSQL.addVar(rsetid) + " AND s.sdidataitemid = sdiattribute.keyid1 )";
            this.logger.info("Deleting dataitem attributes from sdiattribute: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidataitem WHERE " + this.getSDCClause("sdidataitem", rsetid, safeSQL);
            this.logger.info("Deleting from sdidataitem: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidataapproval WHERE " + this.getSDCClause("sdidataapproval", rsetid, safeSQL);
            this.logger.info("Deleting from sdidataapproval: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidatarelation WHERE " + this.getSDCClause("sdidatarelation", rsetid, safeSQL);
            this.logger.info("Deleting from sdidatarelation: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidatacrosssdicalc WHERE " + this.getSDCClause("sdidatacrosssdicalc", rsetid, safeSQL);
            this.logger.info("Deleting from sdidatacrosssdicalc: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiattribute  WHERE sdiattribute.sdcid = 'DataSet' AND EXISTS (SELECT 1 FROM sdidata s, rsetitems r  WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND r.rsetid = " + safeSQL.addVar(rsetid) + " AND s.sdidataid = sdiattribute.keyid1 )";
            this.logger.info("Deleting dataset attributes from sdiattribute: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidata WHERE " + this.getSDCClause("sdidata", rsetid, safeSQL);
            this.logger.info("Deleting from sdidata: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiaddress WHERE " + this.getSDCClause("sdiaddress", rsetid, safeSQL);
            this.logger.info("Deleting from sdiaddress: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiapprovalstep WHERE " + this.getSDCClause("sdiapprovalstep", rsetid, safeSQL);
            this.logger.info("Deleting from sdiapprovalstep: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiapproval WHERE " + this.getSDCClause("sdiapproval", rsetid, safeSQL);
            this.logger.info("Deleting from sdiapproval: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, "queuesdcid='SDIWorkItem' AND queuekeyid1 IN (SELECT sdiworkitemid FROM sdiworkitem WHERE " + this.getSDCClause("sdiworkitem", rsetid, safeSQL) + ")", safeSQL, true, true);
            safeSQL.reset();
            sql = "DELETE FROM sdiworkitemrelation WHERE " + this.getSDCClause("sdiworkitemrelation", rsetid, safeSQL);
            this.logger.info("Deleting from sdiworkitemrelation: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiattribute  WHERE sdiattribute.sdcid = 'SDIWorkItem' AND EXISTS (SELECT 1 FROM sdiworkitem s, rsetitems r  WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.keyid3 = r.keyid3 AND r.rsetid = " + safeSQL.addVar(rsetid) + " AND s.sdiworkitemid = sdiattribute.keyid1 )";
            this.logger.info("Deleting sdiworkitem attributes from sdiattribute: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiworkitemitem WHERE " + this.getSDCClause("sdiworkitemitem", rsetid, safeSQL);
            this.logger.info("Deleting from sdiworkitemitem : " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiworkitem WHERE " + this.getSDCClause("sdiworkitem", rsetid, safeSQL);
            this.logger.info("Deleting from sdiworkitem: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdicoc WHERE " + this.getSDCClause("sdicoc", rsetid, safeSQL);
            this.logger.info("Deleting from sdicoc: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdispecrule WHERE " + this.getSDCClause("sdispecrule", rsetid, safeSQL);
            this.logger.info("Deleting from sdispecrule: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdispec WHERE " + this.getSDCClause("sdispec", rsetid, safeSQL);
            this.logger.info("Deleting from sdispec: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdipricelist WHERE " + this.getSDCClause("sdipricelist", rsetid, safeSQL);
            this.logger.info("Deleting from sdipricelist: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdialias WHERE " + this.getSDCClause("sdialias", rsetid, safeSQL);
            this.logger.info("Deleting from sdialias: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdicalendar WHERE " + this.getSDCClause("sdicalendar", rsetid, safeSQL);
            this.logger.info("Deleting from sdicalendar: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sysuserbucket WHERE " + this.getSDCClause("sysuserbucket", rsetid, safeSQL);
            this.logger.info("Deleting from sysuserbucket: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdidocument WHERE " + this.getSDCClause("sdidocument", rsetid, safeSQL);
            this.logger.info("Deleting from sdidocument: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiformrule WHERE " + this.getSDCClause("sdiformrule", rsetid, safeSQL);
            this.logger.info("Deleting from sdiformrule: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiworksheetrule WHERE " + this.getSDCClause("sdiworksheetrule", rsetid, safeSQL);
            this.logger.info("Deleting from sdiworksheetrule: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiworkflowrule WHERE " + this.getSDCClause("sdiworkflowrule", rsetid, safeSQL);
            this.logger.info("Deleting from sdiworkflowrule: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiattribute WHERE " + this.getSDCClause("sdiattribute", rsetid, safeSQL);
            this.logger.info("Deleting from sdiattribute: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            PostDeleteEventObject postDeleteEventObject = new PostDeleteEventObject(sdcid, sdc, rsetid, properties);
            if (EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postDeleteEventObject)) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRsetid(rsetid);
                sdiRequest.setRetainRsetid(true);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("sdieventplan");
                sdiRequest.setRequestItem("sdieventplanitem");
                sdiRequest.setRequestItem("sdieventplanitemproperty");
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                postDeleteEventObject.setSupplementalData(sdiProcessor.getSDIData(sdiRequest));
            }
            safeSQL.reset();
            sql = "DELETE FROM sdieventplanitemproperty WHERE " + this.getSDCClause("sdieventplanitemproperty", rsetid, safeSQL);
            this.logger.info("Deleting from sdieventplanitemproperty: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdieventplanitem WHERE " + this.getSDCClause("sdieventplanitem", rsetid, safeSQL);
            this.logger.info("Deleting from sdieventplanitem: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdieventplan WHERE " + this.getSDCClause("sdieventplan", rsetid, safeSQL);
            this.logger.info("Deleting from sdieventplan: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdinote WHERE " + this.getSDCClause("sdinote", rsetid, safeSQL);
            this.logger.info("Deleting from sdinote: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            if (!sdcid.equals("Department")) {
                safeSQL.reset();
                sql = "DELETE FROM sdisecuritydepartment WHERE " + this.getSDCClause("sdisecuritydepartment", rsetid, safeSQL);
                this.logger.info("Deleting from sdisecuritydepartment: " + sql);
                this.database.executePreparedUpdate(sql, safeSQL.getValues());
            }
            safeSQL.reset();
            sql = "DELETE FROM sdisecurityset WHERE " + this.getSDCClause("sdisecurityset", rsetid, safeSQL);
            this.logger.info("Deleting from sdisecurityset: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "DELETE FROM sdiresourcerequirement WHERE " + this.getSDCClause("sdiresourcerequirement", rsetid, safeSQL);
            this.logger.info("Deleting from sdiresourcerequirement: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            safeSQL.reset();
            WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, this.connectionInfo.getDbms().equals("ORA") ? "( queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \tqueuesdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tqueuekeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tqueuekeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tqueuekeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )", safeSQL, true, true);
            DataSet dsActivityWorkSDIs = new DataSet();
            if ("WorkOrderSDC".equals(sdcid) || "Sample".equals(sdcid)) {
                sql = "select * from activityworksdi a, rsetitems r where a.worksdcid = r.sdcid and a.workkeyid1 = r.keyid1 and a.workkeyid2 = r.keyid2 and a.workkeyid3 = r.keyid3 and rsetid = ? ";
                this.database.createPreparedResultSet("selectactivityworksdis", sql, new String[]{rsetid});
                dsActivityWorkSDIs = new DataSet(this.database.getResultSet("selectactivityworksdis"));
            }
            sql = "DELETE FROM " + tableid + " WHERE " + keyclause;
            this.logger.info("Deleting from " + tableid + sql);
            this.database.executePreparedUpdate(sql, safeSQLKeyClause.getValues());
            if (dsActivityWorkSDIs.getRowCount() > 0) {
                dsActivityWorkSDIs.sort("activityid");
                ArrayList<DataSet> activityGrps = dsActivityWorkSDIs.getGroupedDataSets("activityid");
                for (int g = 0; g < activityGrps.size(); ++g) {
                    DataSet activityWorkSDIs = activityGrps.get(g);
                    PropertyList props = new PropertyList();
                    props.setProperty("activityid", activityWorkSDIs.getValue(0, "activityid"));
                    props.setProperty("worksdcid", sdcid);
                    props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues("workkeyid1", ";"));
                    this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
                }
            }
            if (!sdc.getProperty("auditedflag").equalsIgnoreCase("N")) {
                this.logger.info("Generate the tracelog records");
                String promptflag = sdc.getProperty("auditpromptflag");
                String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                AuditService audit = new AuditService(sapphireConnection);
                try {
                    String tracelogid = null;
                    boolean multiTracelogCreated = false;
                    if (properties.getProperty("tracelogid", "").trim().length() == 0) {
                        String reason = properties.getProperty("auditreason", "");
                        if (reason.length() > 0) {
                            tracelogid = audit.addSDITraceLogEntry(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), reason, properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"), "Data deleting", standard.equals("Y"));
                            multiTracelogCreated = true;
                        }
                        properties.setProperty("tracelogid", tracelogid);
                    } else {
                        tracelogid = properties.getProperty("tracelogid", "").trim();
                    }
                    int keycolcount = 1;
                    String[] keyid1prop = StringUtil.split(keyid1list, ";");
                    String[] keyid2prop = StringUtil.split(keyid2list, ";");
                    String[] keyid3prop = StringUtil.split(keyid3list, ";");
                    String updateSQL = "";
                    updateSQL = tracelogid != null && tracelogid.length() > 0 ? updateSQL + "UPDATE a_" + tableid + " SET tracelogid = ?, modtool = ?  WHERE " + keycolid1 + "= ? AND tracelogid = 'DELETED'" : updateSQL + "UPDATE a_" + tableid + " SET modtool = ?  WHERE " + keycolid1 + "= ? AND tracelogid = 'DELETED'";
                    if (keycolid2 != null && keycolid2.length() > 0) {
                        keycolcount = 2;
                        updateSQL = updateSQL + " AND " + keycolid2 + "= ?";
                    }
                    if (keycolid3 != null && keycolid3.length() > 0) {
                        keycolcount = 3;
                        updateSQL = updateSQL + " AND " + keycolid3 + "= ?";
                    }
                    try {
                        this.logger.info("Updating audit records using: " + updateSQL);
                        PreparedStatement updateAuditPS = this.database.prepareStatement(updateSQL);
                        for (int i = 0; i < keyid1prop.length; ++i) {
                            if (tracelogid != null && tracelogid.length() > 0) {
                                updateAuditPS.setString(1, multiTracelogCreated ? String.valueOf(Integer.parseInt(tracelogid) + i) : tracelogid);
                                updateAuditPS.setString(2, "DeleteSDI");
                                updateAuditPS.setString(3, keyid1prop[i]);
                                if (keycolcount > 1) {
                                    updateAuditPS.setString(4, keyid2prop[i]);
                                }
                                if (keycolcount > 2) {
                                    updateAuditPS.setString(5, keyid3prop[i]);
                                }
                            } else {
                                updateAuditPS.setString(1, "DeleteSDI");
                                updateAuditPS.setString(2, keyid1prop[i]);
                                if (keycolcount > 1) {
                                    updateAuditPS.setString(3, keyid2prop[i]);
                                }
                                if (keycolcount > 2) {
                                    updateAuditPS.setString(4, keyid3prop[i]);
                                }
                            }
                            try {
                                int rows = updateAuditPS.executeUpdate();
                                if (rows == 1) continue;
                                this.logger.error("Update the tracelogid in the audit table, update returned: " + String.valueOf(rows));
                                continue;
                            }
                            catch (SQLException e) {
                                throw new SapphireException("EXECUTE_STMT_FAILED", "Error Updating the audit record. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())) + " executing " + updateSQL, e);
                            }
                        }
                        this.database.closeStatement();
                    }
                    catch (Exception e) {
                        this.logger.info("DeleteSDI", "Error Updating the audit record. Exception: " + e.getMessage() + " executing " + updateSQL);
                    }
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postDeleteEventObject);
            if (dsOwnedSecuritySets != null && dsOwnedSecuritySets.getRowCount() > 0) {
                this.deleteOwnedSecuritySet(dsOwnedSecuritySets);
            }
        }
        catch (SapphireException se) {
            throw new SapphireException("DB_ACTION_FAILED", "Failed to delete sdi. Exception: " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
        }
        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdc, "PostDelete");
        Trace.startBusinessRule(sdcid + "." + "PostDelete", true);
        sdcPostRules.postDelete(rsetid, properties);
        Trace.endBusinessRule(sdcid + "." + "PostDelete", true);
        Trace.startBusinessRule(sdcid + "." + "PostDelete", false);
        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
            customRules.postDelete(rsetid, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PostDelete", false);
        sdcPostRules.endRule();
        if (rsetid != null && dam != null) {
            dam.clearRSet(rsetid);
        }
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.removeSDI(this.connectionInfo, sdcid, keyid1list, keyid2list, keyid3list);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void resolveReverseFK(PropertyList properties, String sdcid, int keycols, String rsetid, PropertyList reverseLink, boolean sdcdetaillink) throws SapphireException {
        String where;
        String fromsdcid = reverseLink.getProperty("sdcid");
        String deleteflag = reverseLink.getProperty("deleteflag");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList fromsdc = sdcProcessor.getPropertyList(fromsdcid);
        int fromkeycols = Integer.parseInt(fromsdc.getProperty("keycolumns"));
        String fromtableid = sdcdetaillink ? reverseLink.getProperty("linktableid") : fromsdc.getProperty("tableid");
        PropertyListCollection fromtables = fromsdc.getCollection("tables");
        PropertyList fromtable = fromtables.getPropertyList(fromtableid);
        int fromtablekeycols = fromtable != null ? Integer.parseInt(fromtable.getProperty("keycolumns", "0")) : 0;
        boolean partOfKey = false;
        for (int i = 1; !partOfKey && i <= fromtablekeycols; ++i) {
            if (!fromtable.getProperty("keycolid" + i).equals(reverseLink.getProperty("sdccolumnid")) && !fromtable.getProperty("keycolid" + i).equals(reverseLink.getProperty("sdccolumnid2")) && !fromtable.getProperty("keycolid" + i).equals(reverseLink.getProperty("sdccolumnid3"))) continue;
            partOfKey = true;
        }
        if (deleteflag.length() == 0) {
            deleteflag = partOfKey ? "B" : "N";
        } else if (partOfKey && deleteflag.equals("N")) {
            throw new SapphireException("DB_ACTION_FAILED", "Illegal delete rule on foreign key link on table '" + fromtableid + "' in the '" + fromsdcid + "' SDC - cannot null out key fields!");
        }
        SafeSQL safeSQL = new SafeSQL();
        String string = this.connectionInfo.isOracle() ? " WHERE (" + reverseLink.getProperty("sdccolumnid") + (keycols > 1 && reverseLink.getProperty("sdccolumnid2").length() > 0 ? ", " + reverseLink.getProperty("sdccolumnid2") : "") + (keycols > 2 && reverseLink.getProperty("sdccolumnid3").length() > 0 ? ", " + reverseLink.getProperty("sdccolumnid3") : "") + ") IN (SELECT keyid1" + (keycols > 1 && reverseLink.getProperty("sdccolumnid2").length() > 0 ? ", keyid2" : "") + (keycols > 2 && reverseLink.getProperty("sdccolumnid3").length() > 0 ? ", keyid3" : "") + " FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") " : (where = " WHERE (" + reverseLink.getProperty("sdccolumnid") + " IN (SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ")" + (keycols > 1 && reverseLink.getProperty("sdccolumnid2").length() > 0 ? " AND " + reverseLink.getProperty("sdccolumnid2") + " IN (SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ")" : "") + (keycols > 2 && reverseLink.getProperty("sdccolumnid3").length() > 0 ? " AND " + reverseLink.getProperty("sdccolumnid3") + " IN (SELECT keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ")" : "") + ")");
        if (deleteflag.equals("N")) {
            String sql = "UPDATE " + fromtableid + " SET " + reverseLink.getProperty("sdccolumnid") + " = NULL " + (reverseLink.getProperty("sdccolumnid2").length() > 0 ? ", " + reverseLink.getProperty("sdccolumnid2") + " = NULL " : "") + (reverseLink.getProperty("sdccolumnid3").length() > 0 ? ", " + reverseLink.getProperty("sdccolumnid3") + " = NULL " : "") + where;
            this.logger.info("Updating reverse FK links: " + sql);
            this.database.executePreparedUpdate(sql, safeSQL.getValues());
            return;
        } else {
            boolean isDetailOfDType;
            if ("Y".equals(properties.getProperty("donotvalidate"))) return;
            String keyidColumns = fromsdc.getProperty("keycolid1");
            boolean bl = isDetailOfDType = fromsdc.getProperty("sdctype").equals("D") && sdcdetaillink;
            if (isDetailOfDType) {
                keyidColumns = "sdcid, keyid1, keyid2, keyid3";
            }
            String sql1 = "SELECT " + keyidColumns + (fromkeycols > 1 ? ", " + fromsdc.getProperty("keycolid2") : " ") + (fromkeycols > 2 ? ", " + fromsdc.getProperty("keycolid3") : " ") + " FROM   " + fromtableid + " " + where;
            this.database.createPreparedResultSet("reverseFKSDIs", sql1, safeSQL.getValues());
            StringBuffer keyid1 = new StringBuffer();
            StringBuffer keyid2 = new StringBuffer();
            StringBuffer keyid3 = new StringBuffer();
            while (this.database.getNext("reverseFKSDIs")) {
                keyid1.append(";").append(this.database.getString("reverseFKSDIs", isDetailOfDType ? "keyid1" : fromsdc.getProperty("keycolid1")));
                if (fromkeycols > 1) {
                    keyid2.append(";").append(this.database.getString("reverseFKSDIs", isDetailOfDType ? "keyid2" : fromsdc.getProperty("keycolid2")));
                }
                if (fromkeycols <= 2) continue;
                keyid3.append(";").append(this.database.getString("reverseFKSDIs", isDetailOfDType ? "keyid3" : fromsdc.getProperty("keycolid3")));
            }
            if (keyid1.length() <= 0) return;
            if (deleteflag.equals("D")) {
                if (sdcdetaillink) {
                    this.deleteChildRows(fromtables, fromtableid, where, safeSQL.getValues());
                    String sql = "DELETE FROM " + fromtableid + where;
                    this.logger.info("Deleting reverse FK links: " + sql);
                    this.database.executePreparedUpdate(sql, safeSQL.getValues());
                    return;
                } else {
                    HashMap<String, String> deleteProps = new HashMap<String, String>();
                    deleteProps.put("sdcid", fromsdcid);
                    deleteProps.put("keyid1", keyid1.substring(1));
                    deleteProps.put("keyid2", fromkeycols > 1 && keyid2.length() > 0 ? keyid2.substring(1) : "");
                    deleteProps.put("keyid3", fromkeycols > 2 && keyid3.length() > 0 ? keyid3.substring(1) : "");
                    deleteProps.put("applylock", properties.getProperty("applylock"));
                    deleteProps.put("old", properties.getProperty("old"));
                    deleteProps.put("tracelogid", properties.getProperty("tracelogid", ""));
                    deleteProps.put("__sdcruleconfirm", properties.getProperty("__sdcruleconfirm", "N"));
                    try {
                        this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
                        if (!this.getActionProcessor().hasInfoErrors()) return;
                        this.setErrors(this.getActionProcessor().getErrorHandler());
                        return;
                    }
                    catch (ActionException e) {
                        ErrorHandler eh = e.getErrorHandler();
                        if (eh != null && eh.size() > 0) {
                            this.setErrors(e.getErrorHandler());
                            return;
                        }
                        String error = e.getMessage();
                        throw new SapphireException("DB_ACTION_FAILED", error.substring(error.lastIndexOf("Exception:") + "Exception:".length()));
                    }
                }
            } else {
                PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
                StringBuffer sb = new StringBuffer();
                sb.append("{{Delete not allowed for}} ").append(sdc.getProperty("plural")).append(".");
                sb.append(" {{Child records found in}} ").append(fromsdc.getProperty("plural"));
                sb.append(" (").append(StringUtil.replaceAll(keyid1.substring(1), ";", ", ")).append(")");
                this.setError(this.getTranslationProcessor().translate("Delete not allowed"), "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
            }
        }
    }

    private void deleteChildRows(PropertyListCollection tables, String parenttableid, String where, Object[] bindvalues) throws SapphireException {
        for (int i = 0; i < tables.size(); ++i) {
            PropertyList table = tables.getPropertyList(i);
            if (!table.getProperty("parenttableid").equals(parenttableid)) continue;
            this.deleteChildRows(tables, table.getProperty("tableid"), where, bindvalues);
            String sql = "DELETE FROM " + table.getProperty("tableid") + where;
            this.logger.info("Deleting reverse FK links: " + sql);
            this.database.executePreparedUpdate(sql, bindvalues);
        }
    }

    private String getSDCClause(String tname, String rsetid, SafeSQL safeSQL) {
        String sdcclause = "";
        sdcclause = this.runOld ? (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )") : (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + tname + ".sdcid = r.sdcid AND " + tname + ".keyid1= r.keyid1 AND " + tname + ".keyid2 = r.keyid2 AND " + tname + ".keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ")");
        return sdcclause;
    }

    private String getFromSDCClause(String tname, String rsetid) {
        String fromsdcclause = "";
        fromsdcclause = this.runOld ? (this.connectionInfo.getDbms().equals("ORA") ? "( fromsdcid, fromkeyid1, fromkeyid2, fromkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid='" + rsetid + "')" : "( \tfromsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid='" + rsetid + "') AND \tfromkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid='" + rsetid + "') AND \tfromkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid='" + rsetid + "') AND \tfromkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid='" + rsetid + "') )") : (this.connectionInfo.getDbms().equals("ORA") ? "( fromsdcid, fromkeyid1, fromkeyid2, fromkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid='" + rsetid + "')" : " EXISTS ( SELECT null FROM rsetitems r WHERE " + tname + ".fromsdcid = r.sdcid AND " + tname + ".fromkeyid1 = r.keyid1 AND " + tname + ".fromkeyid2 = r.keyid2 AND " + tname + ".fromkeyid3 = r.keyid3 AND \tr.rsetid='" + rsetid + "')");
        return fromsdcclause;
    }

    private String getLinkSDCClause(String tname, String rsetid, SafeSQL safeSQL) {
        String linksdcclause = "";
        linksdcclause = this.runOld ? (this.connectionInfo.getDbms().equals("ORA") ? "( linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \tlinksdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )") : (this.connectionInfo.getDbms().equals("ORA") ? "( linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r WHERE " + tname + ".linksdcid = r.sdcid AND " + tname + ".linkkeyid1 = r.keyid1 AND " + tname + ".linkkeyid2 = r.keyid2 AND " + tname + ".linkkeyid3 = r.keyid3 AND \tr.rsetid=" + safeSQL.addVar(rsetid) + ")");
        return linksdcclause;
    }

    private String getRefSDCClause(String tname, String rsetid, SafeSQL safeSQL) {
        String refsdcclause = "";
        refsdcclause = this.runOld ? (this.connectionInfo.getDbms().equals("ORA") ? "( refsdcid, refkeyid1, refkeyid2, refkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \trefsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \trefkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \trefkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \trefkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )") : (this.connectionInfo.getDbms().equals("ORA") ? "( refsdcid, refkeyid1, refkeyid2, refkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r WHERE " + tname + ".refsdcid = r.sdcid AND " + tname + ".refkeyid1 = r.keyid1 AND " + tname + ".refkeyid2 = r.keyid2 AND " + tname + ".refkeyid3 = r.keyid3 AND r.rsetid=" + safeSQL.addVar(rsetid) + ")");
        return refsdcclause;
    }

    private void deleteOwnedSecuritySet(DataSet dsSecuritySets) throws SapphireException {
        String referredSets;
        String currentUser = this.connectionInfo.getSysuserId();
        String currentUserJobType = this.connectionInfo.getCurrentJobtype();
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        String[] nonPermittedSS = new String[]{};
        String[] referredSS = new String[]{};
        DataSet editOwnerDetails = new DataSet();
        String nonPermitted = SecuritySetUtil.findSecuritySetsNonPermittedForAnOperation(dsSecuritySets.getColumnValues("securitysetid", ";"), currentUser, currentUserJobType, "LV_SecuritySet", "Admin", this.getDAMProcessor(), this.getQueryProcessor(), sapphireConnection);
        if (nonPermitted.length() > 0) {
            nonPermittedSS = StringUtil.split(nonPermitted, ",");
            for (int i = 0; i < nonPermittedSS.length; ++i) {
                int r = dsSecuritySets.findRow("securitysetid", nonPermittedSS[i]);
                if (r <= -1) continue;
                editOwnerDetails.copyRow(dsSecuritySets, r, 1);
                dsSecuritySets.remove(r);
            }
        }
        if (dsSecuritySets.getRowCount() > 0 && (referredSets = SecuritySetUtil.checkForSecuritySetReference(dsSecuritySets, this.database)).length() > 0) {
            referredSS = StringUtil.split(referredSets, ",");
            for (int i = 0; i < referredSS.length; ++i) {
                int r = dsSecuritySets.findRow("securitysetid", referredSS[i]);
                if (r <= -1) continue;
                editOwnerDetails.copyRow(dsSecuritySets, r, 1);
                dsSecuritySets.remove(r);
            }
        }
        ActionProcessor ap = this.getActionProcessor();
        if (dsSecuritySets.getRowCount() > 0) {
            PropertyList delProps = new PropertyList();
            delProps.setProperty("sdcid", "LV_SecuritySet");
            delProps.setProperty("keyid1", dsSecuritySets.getColumnValues("securitysetid", ";"));
            delProps.setProperty("predeletecheck", "N");
            ap.processAction("DeleteSDI", "1", delProps);
        }
        if (editOwnerDetails.getRowCount() > 0) {
            String updateSS = "update securityset set ownersdcid = null, ownerkeyid1 = null, ownerkeyid2 = null,ownerkeyid3 = null, ownerchackflag = null where securitysetid = ?";
            PreparedStatement dslPsmt = this.database.prepareStatement("updateownerdetails", "update securityset set ownersdcid = null, ownerkeyid1 = null, ownerkeyid2 = null,ownerkeyid3 = null, ownercheckflag = null where securitysetid = ?");
            try {
                for (int i = 0; i < editOwnerDetails.getRowCount(); ++i) {
                    dslPsmt.setString(1, editOwnerDetails.getString(i, "securitysetid", ""));
                    dslPsmt.executeUpdate();
                }
            }
            catch (Exception e) {
                throw new SapphireException("EXECUTE_STMT_FAILED", "Error in updating the securityset. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())) + " executing " + updateSS, e);
            }
            this.database.closeStatement("editownerdetails");
        }
    }

    private DataSet getOwnedSecuritySets(String rsetid, String sdcid, int keycols) throws SapphireException {
        StringBuffer sqlSS = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlSS.append("select s.securitysetid, s.ownersdcid, s.ownerkeyid1, s.ownerkeyid2, s.ownerkeyid3 ").append(" from securityset s, rsetitems r ").append(" where s.ownersdcid = r.sdcid and s.ownerkeyid1 = r.keyid1 ");
        if (keycols > 1) {
            sqlSS.append(" and ").append(" s.ownerkeyid2 = r.keyid2");
        }
        if (keycols > 2) {
            sqlSS.append(" and ").append(" s.ownerkeyid3 = r.keyid3");
        }
        sqlSS.append(" and r.rsetid = " + safeSQL.addVar(rsetid) + " and r.sdcid = " + safeSQL.addVar(sdcid));
        this.database.createPreparedResultSet("ownedsecuritysets", sqlSS.toString(), safeSQL.getValues());
        DataSet dsOwnedSecuritySets = new DataSet(this.database.getResultSet("ownedsecuritysets"));
        return dsOwnedSecuritySets;
    }

    private void checkWorksheetReferences(String rsetid) throws SapphireException {
        String check = "SELECT distinct wi.worksheetid from worksheetitem wi, worksheetitemsdi wsdi, rsetitems r WHERE  r.rsetid = ? AND    wsdi.sdcid = r.sdcid AND    wsdi.keyid1 = r.keyid1 AND    wsdi.keyid2 = r.keyid2 AND    wsdi.keyid3 = r.keyid3 AND    wi.worksheetitemid = wsdi.worksheetitemid AND    wi.worksheetitemversionid = wsdi.worksheetitemversionid ";
        this.database.createPreparedResultSet(check, new Object[]{rsetid});
        StringBuffer refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            if (i == 0) {
                refs.append("Worksheet: ");
            }
            refs.append(this.database.getString("worksheetid")).append(",");
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            throw new SapphireException("VALIDATION", "SDI(s) references are found in the following worksheet(s) :" + refs.substring(0, refs.length() - 1));
        }
    }
}

