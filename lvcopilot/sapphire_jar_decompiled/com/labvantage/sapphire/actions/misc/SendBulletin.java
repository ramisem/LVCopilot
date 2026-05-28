/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SendBulletinEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SendBulletin
extends BaseAction
implements sapphire.action.SendBulletin {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        HashMap<String, String> actionProps = new HashMap<String, String>();
        actionProps.put("sdcid", "Bulletin");
        actionProps.put("copies", "1");
        actionProps.put("bulletindesc", properties.getProperty("description"));
        actionProps.put("bulletintext", properties.getProperty("body"));
        actionProps.put("url", properties.getProperty("url"));
        actionProps.put("priorityflag", StringUtil.getYN(properties.getProperty("priority"), "N"));
        actionProps.put("source", properties.getProperty("source"));
        ActionProcessor ap = this.getActionProcessor();
        try {
            ap.processAction("AddSDI", "1", actionProps);
            String bulletinid = (String)actionProps.get("newkeyid1");
            properties.put("newkeyid1", bulletinid);
            properties.put("bulletinid", bulletinid);
            this.logger.info("New bulletin " + bulletinid + " created");
            String[] users = StringUtil.split(properties.getProperty("user"), ";");
            HashSet<String> u = new HashSet<String>(Arrays.asList(users));
            HashSet<String> roleUsers = new HashSet<String>();
            String rolelist = properties.getProperty("role");
            if (StringUtil.getLen(rolelist) > 0L) {
                String[] roles = StringUtil.split(rolelist, ";");
                for (int i = 0; i < roles.length; ++i) {
                    this.database.createPreparedResultSet("select sysuserid from sysuserrole where roleid=?", new Object[]{roles[i]});
                    while (this.database.getNext()) {
                        roleUsers.add(this.database.getString("sysuserid"));
                    }
                    this.database.createPreparedResultSet("SELECT DISTINCT sysuserid FROM sysuserjobtype sjt, jobtyperole jtr WHERE sjt.jobtypeid = jtr.jobtypeid AND jtr.roleid = ?", new Object[]{roles[i]});
                    while (this.database.getNext()) {
                        roleUsers.add(this.database.getString("sysuserid"));
                    }
                }
            }
            HashSet<String> departmentUsers = new HashSet<String>();
            String deptlist = properties.getProperty("department");
            if (StringUtil.getLen(deptlist) > 0L) {
                String[] departments = StringUtil.split(deptlist, ";");
                StringBuilder sql = new StringBuilder();
                if ("".equals(rolelist)) {
                    sql.append("select sysuserid deptuser from departmentsysuser where departmentid =? UNION ");
                    sql.append("SELECT departmentadmin deptuser FROM department WHERE departmentid =?");
                } else {
                    sql.append(" select departmentsysuser.sysuserid deptuser from departmentsysuser,sysuserrole where departmentsysuser.departmentid = ? ");
                    sql.append(" and sysuserrole.sysuserid= departmentsysuser.sysuserid and sysuserrole.roleid in ('").append(StringUtil.replaceAll(rolelist, ";", "','")).append("') UNION ");
                    sql.append(" SELECT departmentadmin deptuser FROM department,sysuserrole WHERE department.departmentid = ? ");
                    sql.append(" and sysuserrole.sysuserid= department.departmentadmin and sysuserrole.roleid in ('");
                    sql.append(StringUtil.replaceAll(rolelist, ";", "','")).append("')");
                }
                for (int i = 0; i < departments.length; ++i) {
                    this.database.createPreparedResultSet(sql.toString(), new Object[]{departments[i], departments[i]});
                    while (this.database.getNext()) {
                        departmentUsers.add(this.database.getString("deptuser"));
                    }
                }
            }
            if (properties.getProperty("joinroleanddepartment", "N").equals("Y") && rolelist.length() > 0 && deptlist.length() > 0) {
                roleUsers.retainAll(departmentUsers);
                u.addAll(roleUsers);
            } else {
                u.addAll(departmentUsers);
                u.addAll(roleUsers);
            }
            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
            String inlist = "";
            StringBuffer mailUsers = new StringBuffer();
            for (String user : u) {
                if (user == null || user.length() <= 0) continue;
                if (cp.getProfileProperty(user, "notificationformat", "Bulletin").equalsIgnoreCase("Bulletin")) {
                    inlist = inlist + ",'" + user.toLowerCase() + "'";
                    continue;
                }
                mailUsers.append(",'").append(user.toLowerCase()).append("'");
            }
            if (inlist.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "INSERT INTO bulletinsysuser ( bulletinid, sysuserid, readflag, deletedflag ) select " + safeSQL.addVar(bulletinid) + ", sysuserid, 'N', 'N' from sysuser where lower(sysuserid) in (" + safeSQL.addIn(inlist.substring(1)) + ")";
                this.database.executePreparedUpdate(sql, safeSQL.getValues());
            }
            if (mailUsers.length() > 0) {
                String fromUserTemp;
                String searchingIds = mailUsers.substring(1);
                String fromUser = properties.getProperty("source", "");
                boolean isFromRecipient = true;
                if (fromUser.length() > 0 && mailUsers.indexOf(fromUserTemp = "'" + fromUser.toLowerCase() + "'") == -1) {
                    searchingIds = fromUserTemp + mailUsers.toString();
                    isFromRecipient = false;
                }
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT sysuserid, email FROM sysuser WHERE LOWER(sysuserid) IN (" + safeSQL.addIn(searchingIds) + ") AND (email IS NOT NULL OR email != '')";
                DataSet emailIds = this.getQueryProcessor().getPreparedSqlDataSet("findtoemail", sql, safeSQL.getValues());
                int row = emailIds.findRow("sysuserid", fromUser.toLowerCase());
                String fromUserEmail = "";
                if (row >= 0) {
                    fromUserEmail = emailIds.getValue(row, "email");
                    if (!isFromRecipient) {
                        emailIds.deleteRow(row);
                    }
                }
                if (emailIds.getRowCount() > 0) {
                    actionProps = new HashMap();
                    actionProps.put("address", emailIds.getColumnValues("email", ";"));
                    actionProps.put("from", fromUserEmail);
                    actionProps.put("subject", properties.getProperty("description", ""));
                    actionProps.put("message", this.getEmailBody(properties, cp));
                    actionProps.put("mailformat", "html");
                    if (StringUtil.getYN(properties.getProperty("priority"), "N").equals("Y")) {
                        actionProps.put("messageheader", "X-Priority=1;Importance=high");
                    }
                    ap.processAction("SendMail", "1", actionProps);
                } else {
                    this.logger.info("SendBulletin could not sent notification in email format. Reason: No email id found");
                }
            }
            EventManager.generateEvent(new SapphireConnection(this.database.getConnection(), this.connectionInfo), null, new SendBulletinEventObject(u.toArray(new String[u.size()]), properties.getProperty("description"), properties.getProperty("body"), StringUtil.getYN(properties.getProperty("priority"), "N"), properties.getProperty("url")));
        }
        catch (ActionException ae) {
            this.setErrors(ae.getErrorHandler());
            throw new SapphireException("Failed to add send bulletin SDI");
        }
    }

    private String getEmailBody(PropertyList properties, ConfigurationProcessor cp) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer message = new StringBuffer();
        message.append("<table cellpadding=0 cellspacing=3 border=0 width=80% style=\"font-family:Arial, Verdana, sans-serif, serif; font-size:12; margin-left:10px; margin-top:10px\">");
        message.append("<tr><td>");
        message.append(tp.translate("This is an automated notification from LabVantage."));
        message.append("</td></tr>");
        message.append("<tr><td><hr>");
        message.append("<table style=\"font-family:Arial, Verdana, sans-serif, serif; font-size:12;1px solid #b034de\" cellpadding=\"8\" cellspacing=\"0\" width=\"100%\">");
        message.append("<tr><td>").append(properties.getProperty("body", "")).append("</td></tr>");
        message.append("</table>");
        message.append("</td></tr>");
        String url = properties.getProperty("url", "");
        if (url.length() > 0) {
            if (!url.toLowerCase().startsWith("http")) {
                String baseUrl = cp.getConfigProperty("com.labvantage.sapphire.server.webappbaseurl", "");
                baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
                url = baseUrl + url;
            }
            message.append("<tr><td><hr>");
            message.append(tp.translate("This bulletin contains a"));
            message.append(" <a href=\"").append(url).append("\" target=_blank> ");
            message.append(tp.translate("related link"));
            message.append("</a>");
            message.append("</td></tr>");
        }
        message.append("</table>");
        return message.toString();
    }
}

