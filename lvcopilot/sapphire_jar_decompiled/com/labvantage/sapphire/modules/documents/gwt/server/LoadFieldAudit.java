/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LoadFieldAudit
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadFieldAudit(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT a_documentfield.documentid, a_documentfield.documentversionid, a_documentfield.fieldid, a_documentfield.modby, a_documentfield.moddt, a_documentfield.enteredtext, a_documentfield.fieldstatus  FROM   a_documentfield  WHERE  a_documentfield.documentid = " + safeSQL.addVar(requestData.getProperty("documentid")) + " AND    a_documentfield.documentversionid = " + safeSQL.addVar(requestData.getProperty("documentversionid", "1")) + " AND    a_documentfield.fieldid=" + safeSQL.addVar(requestData.getProperty("fieldid")) + " AND    a_documentfield.fieldinstance=" + safeSQL.addVar(requestData.getProperty("fieldinstance")) + " ORDER BY a_documentfield.auditsequence DESC";
        DataSet auditData = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (auditData != null && auditData.size() > 0) {
            auditData.addColumn("fieldstatustext", 0);
            String lastvalue = "__lastvalue";
            String lastmodby = "__lastmodby";
            for (int i = auditData.size() - 1; i >= 0; --i) {
                String value = auditData.getValue(i, "enteredtext");
                String modby = auditData.getValue(i, "modby");
                if (lastmodby.equals(modby) && lastvalue.equals(value)) {
                    auditData.deleteRow(i);
                    continue;
                }
                lastvalue = value;
                lastmodby = modby;
                auditData.setString(i, "fieldstatustext", Document.getFieldStatusText(auditData.getValue(i, "fieldstatus"), this.trans));
            }
        }
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", auditData != null ? JSONUtil.toJSONString(auditData) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, auditData);
        return responseData;
    }
}

