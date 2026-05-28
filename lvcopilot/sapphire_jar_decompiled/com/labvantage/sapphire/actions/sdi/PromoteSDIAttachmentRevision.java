/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PromoteSDIAttachmentRevision
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77320 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            PreparedStatement update;
            String sdcId = properties.getProperty("sdcid");
            String keyId1 = properties.getProperty("keyid1");
            String keyId2 = properties.getProperty("keyid2");
            String keyId3 = properties.getProperty("keyid3");
            String attachmentNum = properties.getProperty("attachmentnum");
            String auditSequence = properties.getProperty("auditsequence");
            String auditReason = properties.getProperty("auditreason");
            String auditActivity = properties.getProperty("auditactivity");
            String auditSignedFlag = properties.getProperty("auditsignedflag");
            String traceLogId = "";
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            ArrayList<String> excludedCols = new ArrayList<String>();
            excludedCols.add("sdcid");
            excludedCols.add("keyid1");
            excludedCols.add("keyid2");
            excludedCols.add("keyid3");
            excludedCols.add("attachmentnum");
            excludedCols.add("createby");
            excludedCols.add("createdt");
            excludedCols.add("createtool");
            excludedCols.add("modby");
            excludedCols.add("moddt");
            excludedCols.add("lockedflag");
            excludedCols.add("lockedby");
            excludedCols.add("auditsequence");
            PropertyListCollection sdcColCollection = sdcProcessor.getColumns("SDIAttachment");
            if (auditActivity.length() > 0 || auditReason.length() > 0) {
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                try {
                    traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, "", "", true);
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT a.* ");
            sql.append(" ,(SELECT s.contentrevision FROM sdiattachment s WHERE ").append(" s.sdcid=").append(safeSQL.addVar(sdcId)).append("").append(" AND s.keyid1=").append(safeSQL.addVar(keyId1)).append("").append(" AND s.attachmentnum=").append(safeSQL.addVar(attachmentNum)).append("");
            if (keyId2 != null && !keyId2.equalsIgnoreCase("(null)") && keyId2.length() > 0) {
                sql.append(" AND s.keyid2=").append(safeSQL.addVar(keyId2)).append("");
            }
            if (keyId3 != null && !keyId3.equalsIgnoreCase("(null)") && keyId3.length() > 0) {
                sql.append(" AND s.keyid3=").append(safeSQL.addVar(keyId3)).append("");
            }
            sql.append(") as currentrevision");
            sql.append(" FROM a_sdiattachment a WHERE ").append(" a.sdcid=").append(safeSQL.addVar(sdcId)).append("").append(" AND a.keyid1=").append(safeSQL.addVar(keyId1)).append("").append(" AND a.attachmentnum=").append(safeSQL.addVar(attachmentNum)).append("").append(" AND a.auditsequence=").append(safeSQL.addVar(auditSequence)).append("");
            if (keyId2 != null && !keyId2.equalsIgnoreCase("(null)") && keyId2.length() > 0) {
                sql.append(" AND a.keyid2=").append(safeSQL.addVar(keyId2)).append("");
            }
            if (keyId3 != null && !keyId3.equalsIgnoreCase("(null)") && keyId3.length() > 0) {
                sql.append(" AND a.keyid3=").append(safeSQL.addVar(keyId3)).append("");
            }
            this.database.createPreparedResultSet("a_sdiattachment", sql.toString(), safeSQL.getValues());
            if (this.database.getNext("a_sdiattachment")) {
                int currentRevision = this.database.getInt("a_sdiattachment", "currentrevision");
                int contentRevision = this.database.getInt("a_sdiattachment", "contentrevision");
                if (contentRevision == currentRevision) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Content Revision"), "FAILURE", this.getTranslationProcessor().translate("Selected revision is already the Current revision."));
                }
                if (currentRevision == 0) {
                    currentRevision = 1;
                }
                sql.setLength(0);
                sql.append("UPDATE sdiattachment SET");
                for (int i = 0; i < sdcColCollection.size(); ++i) {
                    PropertyList colProps = sdcColCollection.getPropertyList(i);
                    String columnId = colProps.getProperty("columnid");
                    if (excludedCols.contains(columnId)) continue;
                    if ("contentrevision".equalsIgnoreCase(columnId)) {
                        sql.append(" ").append(columnId).append(" = ").append(currentRevision + 1).append(",");
                        continue;
                    }
                    if ("tracelogid".equalsIgnoreCase(columnId)) {
                        sql.append(" ").append(columnId).append(" = '").append(traceLogId).append("',");
                        continue;
                    }
                    sql.append(" ").append(columnId).append(" = ?,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(" WHERE ").append(" sdcid='").append(sdcId).append("'").append(" AND keyid1='").append(keyId1).append("'").append(" AND attachmentnum='").append(attachmentNum).append("'");
                if (keyId2 != null && !keyId2.equalsIgnoreCase("(null)") && keyId2.length() > 0) {
                    sql.append(" AND keyid2='").append(keyId2).append("'");
                }
                if (keyId3 != null && !keyId3.equalsIgnoreCase("(null)") && keyId3.length() > 0) {
                    sql.append(" AND keyid3='").append(keyId3).append("'");
                }
                this.logger.info("Update SQL: " + sql.toString());
                update = this.database.prepareStatement("sdiattachment", sql.toString());
                int currentParamIndex = 1;
                block10: for (int i = 0; i < sdcColCollection.size(); ++i) {
                    PropertyList colProps = sdcColCollection.getPropertyList(i);
                    String columnId = colProps.getProperty("columnid");
                    if (excludedCols.contains(columnId) || "contentrevision".equalsIgnoreCase(columnId) || "tracelogid".equalsIgnoreCase(columnId)) continue;
                    String dataType = colProps.getProperty("datatype");
                    switch (dataType.charAt(0)) {
                        case 'C': 
                        case 'D': 
                        case 'T': {
                            String stringvalue = this.database.getString("a_sdiattachment", columnId);
                            if (stringvalue == null || stringvalue.length() == 0) {
                                update.setNull(currentParamIndex++, 12);
                                continue block10;
                            }
                            if ("T".equals(dataType) && this.database.isSqlServer()) {
                                update.setCharacterStream(currentParamIndex++, (Reader)new StringReader(stringvalue), stringvalue.length());
                                continue block10;
                            }
                            update.setString(currentParamIndex++, stringvalue);
                            continue block10;
                        }
                        case 'N': 
                        case 'R': {
                            BigDecimal numbervalue = this.database.getBigDecimal("a_sdiattachment", columnId);
                            if (numbervalue == null) {
                                update.setNull(currentParamIndex++, 8);
                                continue block10;
                            }
                            update.setBigDecimal(currentParamIndex++, numbervalue);
                            continue block10;
                        }
                        case 'B': {
                            Blob blobValue = this.database.getBlob("a_sdiattachment", columnId);
                            if (blobValue != null && blobValue.length() > 0L) {
                                update.setBlob(currentParamIndex++, blobValue);
                                continue block10;
                            }
                            update.setNull(currentParamIndex++, 2004);
                        }
                    }
                }
            } else {
                throw new SapphireException(this.getTranslationProcessor().translate("Record not found"), "FAILURE", "Audit Record not found");
            }
            update.executeUpdate();
            this.database.closeStatement("sdiattachment");
            this.database.closeResultSet("a_sdiattachment");
        }
        catch (SQLException e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Processing Error"), "FAILURE", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }
}

