/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentCodes;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LoadDocumentAudit
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadDocumentAudit(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT a_document.documentid, a_document.documentversionid, a_document.createdt,a_document.createby, a_document.moddt, a_document.modby, a_document.documentstatus, a_document.tracelogid, tracelog.reason  FROM   a_document LEFT OUTER JOIN tracelog ON a_document.tracelogid = tracelog.tracelogid WHERE  a_document.documentid = " + safeSQL.addVar(requestData.getProperty("documentid")) + " AND a_document.documentversionid = " + safeSQL.addVar(requestData.getProperty("documentversionid", "1")) + " ORDER BY a_document.auditsequence DESC";
        DataSet auditData = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (auditData != null && auditData.size() > 0) {
            auditData.addColumn("documentstatustext", 0);
            for (int i = 0; i < auditData.size(); ++i) {
                auditData.setString(i, "documentstatustext", DocumentCodes.getDocumentStatusText(auditData.getValue(i, "documentstatus")));
            }
        }
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", auditData != null ? JSONUtil.toJSONString(auditData) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, auditData);
        return responseData;
    }
}

