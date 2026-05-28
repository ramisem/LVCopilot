/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EditSDIWorkItemRelation
extends BaseAction
implements sapphire.action.EditSDIWorkItemRelation {
    private static final String PROPERTY_SEPARATOR = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String instrumentid;
        String amountadjusted;
        String mandatoryFlag;
        String sourceKeyid3;
        String sourceKeyid2;
        String sourceKeyid1;
        String sourceSdcid;
        String amountUnitsType;
        String amountUnits;
        String amount;
        String refKeyId3;
        String refKeyId2;
        String refKeyId1;
        String refSdcId;
        String toKeyId3;
        String toKeyId2;
        String toKeyId1;
        String toSdcId;
        String relationFunction;
        String sdcId = properties.getProperty("sdcid");
        DataSet input = new DataSet(this.connectionInfo);
        input.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), PROPERTY_SEPARATOR);
        input.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), PROPERTY_SEPARATOR);
        String keyId2 = properties.getProperty("keyid2");
        if (keyId2 == null || keyId2.length() == 0) {
            keyId2 = "(null)";
        }
        input.addColumnValues("keyid2", 0, keyId2, PROPERTY_SEPARATOR);
        String keyId3 = properties.getProperty("keyid3");
        if (keyId3 == null || keyId3.length() == 0) {
            keyId3 = "(null)";
        }
        input.addColumnValues("keyid3", 0, keyId3, PROPERTY_SEPARATOR);
        input.addColumnValues("workitemid", 0, properties.getProperty("workitemid"), PROPERTY_SEPARATOR);
        input.addColumnValues("workiteminstance", 0, properties.getProperty("workiteminstance"), PROPERTY_SEPARATOR);
        input.addColumnValues("relationid", 0, properties.getProperty("relationid"), PROPERTY_SEPARATOR);
        input.padColumn("sdcid");
        input.padColumn("keyid1");
        input.padColumn("keyid2");
        input.padColumn("keyid3");
        input.padColumn("workitemid");
        input.padColumn("workiteminstance");
        input.padColumn("relationid");
        String relationType = properties.getProperty("relationtype");
        if (relationType != null && relationType.length() > 0) {
            input.addColumnValues("relationtype", 0, relationType, PROPERTY_SEPARATOR);
        }
        if ((relationFunction = properties.getProperty("relationfunction")) != null && relationFunction.length() > 0) {
            input.addColumnValues("relationfunction", 0, relationFunction, PROPERTY_SEPARATOR);
        }
        if ((toSdcId = properties.getProperty("tosdcid")) != null && toSdcId.length() > 0) {
            input.addColumnValues("tosdcid", 0, toSdcId, PROPERTY_SEPARATOR);
        }
        if ((toKeyId1 = properties.getProperty("tokeyid1")) != null && toKeyId1.length() > 0) {
            input.addColumnValues("tokeyid1", 0, toKeyId1, PROPERTY_SEPARATOR);
        }
        if ((toKeyId2 = properties.getProperty("tokeyid2")) != null && toKeyId2.length() > 0) {
            input.addColumnValues("tokeyid2", 0, toKeyId2, PROPERTY_SEPARATOR);
        }
        if ((toKeyId3 = properties.getProperty("tokeyid3")) != null && toKeyId3.length() > 0) {
            input.addColumnValues("tokeyid3", 0, toKeyId3, PROPERTY_SEPARATOR);
        }
        if ((refSdcId = properties.getProperty("refsdcid")) != null && refSdcId.length() > 0) {
            input.addColumnValues("refsdcid", 0, refSdcId, PROPERTY_SEPARATOR);
        }
        if ((refKeyId1 = properties.getProperty("refkeyid1")) != null && refKeyId1.length() > 0) {
            input.addColumnValues("refkeyid1", 0, refKeyId1, PROPERTY_SEPARATOR);
        }
        if ((refKeyId2 = properties.getProperty("refkeyid2")) != null && refKeyId2.length() > 0) {
            input.addColumnValues("refkeyid2", 0, refKeyId2, PROPERTY_SEPARATOR);
        }
        if ((refKeyId3 = properties.getProperty("refkeyid3")) != null && refKeyId3.length() > 0) {
            input.addColumnValues("refkeyid3", 0, refKeyId3, PROPERTY_SEPARATOR);
        }
        if ((amount = properties.getProperty("amount")) != null && amount.length() > 0) {
            input.addColumnValues("amount", 1, amount, PROPERTY_SEPARATOR);
        }
        if ((amountUnits = properties.getProperty("amountunits")) != null && amountUnits.length() > 0) {
            input.addColumnValues("amountunits", 0, amountUnits, PROPERTY_SEPARATOR);
        }
        if ((amountUnitsType = properties.getProperty("amountunitstype")) != null && amountUnitsType.length() > 0) {
            input.addColumnValues("amountunitstype", 0, amountUnitsType, PROPERTY_SEPARATOR);
        }
        if ((sourceSdcid = properties.getProperty("sourcesdcid")) != null && sourceSdcid.length() > 0) {
            input.addColumnValues("sourcesdcid", 0, sourceSdcid, PROPERTY_SEPARATOR);
        }
        if ((sourceKeyid1 = properties.getProperty("sourcekeyid1")) != null && sourceKeyid1.length() > 0) {
            input.addColumnValues("sourcekeyid1", 0, sourceKeyid1, PROPERTY_SEPARATOR);
        }
        if ((sourceKeyid2 = properties.getProperty("sourcekeyid2")) != null && sourceKeyid2.length() > 0) {
            input.addColumnValues("sourcekeyid2", 0, sourceKeyid2, PROPERTY_SEPARATOR);
        }
        if ((sourceKeyid3 = properties.getProperty("sourcekeyid3")) != null && sourceKeyid3.length() > 0) {
            input.addColumnValues("sourcekeyid3", 0, sourceKeyid3, PROPERTY_SEPARATOR);
        }
        if ((mandatoryFlag = properties.getProperty("mandatoryflag")) != null && mandatoryFlag.length() > 0) {
            input.addColumnValues("mandatoryflag", 0, mandatoryFlag, PROPERTY_SEPARATOR);
        }
        if ((amountadjusted = properties.getProperty("amountadjusted")) != null && amountadjusted.length() > 0) {
            input.addColumnValues("amountadjusted", 1, amountadjusted, PROPERTY_SEPARATOR);
        }
        if ((instrumentid = properties.getProperty("instrumentid")) != null && instrumentid.length() > 0) {
            input.addColumnValues("instrumentid", 0, instrumentid, PROPERTY_SEPARATOR);
        }
        if (properties.getProperty("tracelogid", "").length() > 0 || properties.getProperty("auditreason", "").length() > 0) {
            input.addColumnValues("tracelogid", 0, this.getTracelogid(properties), PROPERTY_SEPARATOR);
            input.padColumn("tracelogid");
        }
        if (refKeyId1 != null && refKeyId1.length() > 0) {
            input.addColumnValues("usedexpiredconsumableflag", 0, ReagentUtil.getUsedExpiredConsumableFlag(refKeyId1, this.getQueryProcessor()), PROPERTY_SEPARATOR);
        }
        for (int i = 0; i < input.size(); ++i) {
            if (input.getValue(i, "tokeyid1").equals("(null)")) {
                input.setString(i, "tokeyid1", null);
                input.setString(i, "tokeyid2", null);
                input.setString(i, "tokeyid3", null);
            }
            if (input.getValue(i, "refkeyid1").equals("(null)")) {
                input.setString(i, "refkeyid1", null);
                input.setString(i, "refkeyid2", null);
                input.setString(i, "refkeyid3", null);
            }
            if (input.getValue(i, "sourcekeyid1").equals("(null)")) {
                input.setString(i, "sourcekeyid1", null);
                input.setString(i, "sourcekeyid2", null);
                input.setString(i, "sourcekeyid3", null);
            }
            if (input.getValue(i, "amountunits").equals("(null)")) {
                input.setString(i, "amountunits", null);
            }
            if (!input.getValue(i, "amountunitstype").equals("(null)")) continue;
            input.setString(i, "amountunitstype", null);
        }
        String[] keyColumns = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance", "relationid"};
        DataSetUtil.update(this.database, input, "sdiworkitemrelation", keyColumns);
    }

    private String getTracelogid(PropertyList properties) throws SapphireException {
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        return traceLogId;
    }
}

