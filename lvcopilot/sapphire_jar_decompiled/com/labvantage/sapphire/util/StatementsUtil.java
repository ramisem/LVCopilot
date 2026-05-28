/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jsoup.nodes.Element
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.services.SapphireConnection;
import org.jsoup.nodes.Element;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class StatementsUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    public static final String TYPE_AGREEMENT = "Agreement";
    public static final String TYPE_ANNOUNCEMENT = "Announcement";

    private StatementsUtil() {
    }

    public static DataSet getUserPendingStatements(String sysUserId, QueryProcessor qp) {
        return StatementsUtil.getUserPendingStatements(sysUserId, "", qp);
    }

    public static DataSet getUserPendingStatements(String sysUserId, String portalId, QueryProcessor qp) {
        ConnectionProcessor cp = new ConnectionProcessor();
        cp.setConnectionid(qp.getConnectionid());
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT st.statementid, st.statementversionid, statementtype, (CASE WHEN stl.statementtitle IS NOT NULL THEN stl.statementtitle ELSE st.statementtitle END) statementtitle, (CASE WHEN stl.statementtext IS NOT NULL THEN stl.statementtext ELSE st.statementtext END) statementtext, persistentflag, portalflag, portalid, (SELECT MAX(ssu.statementversionid) FROM sysuserstatement ssu WHERE lower(ssu.sysuserid) = " + safeSQL.addVar(sysUserId.toLowerCase()) + " AND ssu.statementid = st.statementid ) lastsignedversion FROM statement st LEFT OUTER JOIN statementlanguage stl ON stl.statementid = st.statementid AND stl.statementversionid = st.statementversionid AND stl.languageid = (SELECT u.languageid FROM sysuser u WHERE lower(u.sysuserid) = " + safeSQL.addVar(sysUserId.toLowerCase()) + ") WHERE st.versionstatus = 'C' AND st.statementtype IN ('" + TYPE_AGREEMENT + "','" + TYPE_ANNOUNCEMENT + "') AND st.activeflag != 'N' AND (st.displayuntildt IS NULL OR st.displayuntildt > " + (sapphireConnection.isOracle() ? "sysdate" : "getDate()") + ")" + (portalId != null && portalId.length() > 0 ? " AND  portalflag = 'Y' AND  portalid = " + safeSQL.addVar(portalId) : " AND limsflag = 'Y'") + " ORDER BY st.statementtype";
        DataSet statements = qp.getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
        for (int i = statements.getRowCount() - 1; i >= 0; --i) {
            int stmtVersion;
            int lastSignedVer = Integer.parseInt(statements.getString(i, "lastsignedversion", "0"));
            if (lastSignedVer >= (stmtVersion = Integer.parseInt(statements.getString(i, "statementversionid")))) {
                statements.deleteRow(i);
                continue;
            }
            String text = statements.getString(i, "statementtext", "");
            statements.setString(i, "statementtext", StatementsUtil.sanitizeHtml(text));
        }
        return statements;
    }

    private static String sanitizeHtml(String text) {
        StringBuilder html = new StringBuilder(text);
        HTMLEditorControl.processHTML(html, "a", new HTMLEditorControl.ElementProcessor(){

            @Override
            public void process(Element element) {
                element.attr("target", "_blank");
                element.attr("rel", "noreferrer");
            }

            @Override
            public void complete() {
            }
        });
        return html.toString();
    }

    public static void addUserStatements(DataSet signedStatements, ActionProcessor ap) throws ActionException {
        if (signedStatements != null) {
            QueryProcessor queryProcessor = new QueryProcessor(ap.getConnectionid());
            for (int i = 0; i < signedStatements.size(); ++i) {
                DataSet ds = queryProcessor.getPreparedSqlDataSet("select sysuserid from sysuser where lower(sysuserid) = ?", (Object[])new String[]{signedStatements.getString(i, "sysuserid", "").toLowerCase()});
                if (ds == null || ds.size() <= 0) continue;
                signedStatements.setString(i, "sysuserid", ds.getString(0, "sysuserid", ""));
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("linkid", "User Statement");
            props.setProperty("keyid1", signedStatements.getColumnValues("sysuserid", ";"));
            props.setProperty("statementid", signedStatements.getColumnValues("statementid", ";"));
            props.setProperty("statementversionid", signedStatements.getColumnValues("statementversionid", ";"));
            props.setProperty("acknowledgeddt", signedStatements.getColumnValues("acknowledgeddt", ";"));
            ap.processAction("AddSDIDetail", "1", props);
        }
    }
}

