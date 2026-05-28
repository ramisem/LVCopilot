/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.empower;

import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.modules.empower.EmpowerUploadProcessor;
import com.labvantage.sapphire.webservices.messages.EmpowerMessage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EmpowerUpload
extends BaseAction
implements sapphire.action.EmpowerUpload {
    public static final String EMPOWER_POLICY = "EmpowerPolicy";
    public static final String EMPOWER_DEFAULT_NODE = "Sapphire Product";
    public static final String DELIMITER = ";";

    private void correctDates(DataSet ds, int offset) {
        boolean dateFound = false;
        try {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                for (int c = 0; c < ds.getColumnCount(); ++c) {
                    if (ds.getColumnType(ds.getColumnId(c)) != 2) continue;
                    if (!dateFound) {
                        dateFound = true;
                    }
                    long olddate = ds.getCalendar(i, ds.getColumnId(c)).toInstant().toEpochMilli();
                    this.logger.debug("Date Column - " + ds.getColumnId(c) + " value before offset = " + ds.getValue(i, ds.getColumnId(c)) + " (raw ms = " + olddate + ")");
                    long newdate = olddate + (long)offset;
                    ds.setDate(i, ds.getColumnId(c), newdate);
                    this.logger.debug("Date Column - " + ds.getColumnId(c) + " value after offset = " + ds.getValue(i, ds.getColumnId(c)) + " (raw ms = " + newdate + ")");
                }
            }
            if (!dateFound) {
                this.logger.debug("No date columns found in dataset");
            }
        }
        catch (Exception e) {
            this.logger.warn("Error parsing date values");
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String log = properties.getProperty("log", "");
        String message = properties.getProperty("message", "");
        if (message.length() == 0) {
            String ss = properties.getProperty("sampleset", "");
            String ssl = properties.getProperty("samplesetlines", "");
            String results = properties.getProperty("results", "");
            String peaks = properties.getProperty("peaks", "");
            String node = properties.getProperty("policynode", "");
            String analyst = properties.getProperty("analyst", "");
            if (analyst.length() > 0) {
                DataSet ds = this.getQueryProcessor().getSqlDataSet("SELECT sysuserid FROM sysuser");
                if (ds == null) throw new SapphireException("Failed to upload results as could not obtain user information.");
                String found = "";
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    if (!ds.getValue(i, "sysuserid", "").equalsIgnoreCase(analyst)) continue;
                    found = ds.getValue(i, "sysuserid", "");
                    break;
                }
                if (found.length() <= 0) throw new SapphireException("Analyst provided does not match valid user in system.");
                if (!found.equals(analyst)) {
                    this.logger.info("Analyst provided does not match case. Case adjusted.");
                    analyst = found;
                    properties.setProperty("analyst", analyst);
                }
            }
            String projectname = properties.getProperty("empowerproject", "");
            String databasename = properties.getProperty("empowerdatabase", "");
            ConnectionInfo conn = this.getConnectionProcessor().getConnectionInfo(this.getConnectionProcessor().getConnectionid());
            PropertyList policy = this.getConfigurationProcessor().getPolicy(EMPOWER_POLICY, node);
            EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
            TimeZone tz = conn.getTimeZone() != null ? TimeZone.getTimeZone(this.connectionInfo.getTimeZone()) : TimeZone.getDefault();
            this.logger.debug("Timezone From Connection - " + tz.getDisplayName() + " (offset = " + tz.getRawOffset() + ")");
            int policyoffset = 0;
            policyoffset = policyDef.getTimezoneOffset();
            if (policyoffset == 0) {
                try {
                    ZoneId zoneId = ZoneId.of(policyDef.getTimezone());
                    ZonedDateTime now = ZonedDateTime.now(zoneId);
                    int offsetInSeconds = now.getOffset().getTotalSeconds();
                    int offsetInMilliseconds = offsetInSeconds * 1000;
                    policyoffset = offsetInMilliseconds > 0 ? -offsetInMilliseconds : offsetInMilliseconds;
                }
                catch (Exception e) {
                    policyoffset = 0;
                }
            }
            DataSet sampleSetDS = new DataSet(ss, (com.labvantage.sapphire.services.ConnectionInfo)conn);
            DataSet sampleSetLinesDS = new DataSet(ssl, (com.labvantage.sapphire.services.ConnectionInfo)conn);
            DataSet resultsDS = new DataSet(results, (com.labvantage.sapphire.services.ConnectionInfo)conn);
            DataSet peaksDS = new DataSet(peaks, (com.labvantage.sapphire.services.ConnectionInfo)conn);
            if (policyoffset != 0) {
                double polcycyoffsetH = policyoffset != 0 ? (double)policyoffset / 3600000.0 : 0.0;
                this.logger.debug("Policy Offset - " + policyoffset + "(in hours = " + polcycyoffsetH + ")");
                this.logger.debug("Correcting dates for SampleSet with offset " + policyoffset + " (hours=" + polcycyoffsetH + "):");
                this.correctDates(sampleSetDS, policyoffset);
                this.logger.debug("Correcting dates for SampleSetLines with offset " + policyoffset + " (hours=" + polcycyoffsetH + "):");
                this.correctDates(sampleSetLinesDS, policyoffset);
                this.logger.debug("Correcting dates for Results with offset " + policyoffset + " (hours=" + polcycyoffsetH + "):");
                this.correctDates(resultsDS, policyoffset);
                this.logger.debug("Correcting dates for Peaks with offset " + policyoffset + " (hours=" + polcycyoffsetH + "):");
                this.correctDates(peaksDS, policyoffset);
                this.logger.debug("Dates corrected!");
            }
            EmpowerUploadProcessor processor = this.createUploadProcessor(node);
            processor.processUploadData(analyst, projectname, databasename, sampleSetDS, sampleSetLinesDS, resultsDS, peaksDS);
            properties.setProperty("log", log + processor.getLog());
            properties.setProperty("responsemessage", processor.getResponse());
            return;
        }
        EmpowerMessage msg = new EmpowerMessage(message);
        EmpowerUploadProcessor processor = this.createUploadProcessor(msg.getPolicyNode());
        processor.processMessage(msg);
        properties.setProperty("log", log + processor.getLog());
        properties.setProperty("responsemessage", processor.getResponse());
    }

    private EmpowerUploadProcessor createUploadProcessor(String node) throws SapphireException {
        PropertyList policy;
        if (node == null || node.length() == 0) {
            node = EMPOWER_DEFAULT_NODE;
        }
        if ((policy = this.getConfigurationProcessor().getPolicy(EMPOWER_POLICY, node)) == null) {
            throw new SapphireException("Failed to get policy");
        }
        EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
        return new EmpowerUploadProcessor(policyDef, this.database, this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionProcessor());
    }
}

