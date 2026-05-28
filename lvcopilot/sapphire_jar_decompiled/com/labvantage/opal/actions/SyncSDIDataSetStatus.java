/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.actions.SDIStatus;
import com.labvantage.opal.actions.Sdi;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SyncSDIDataSetStatus
extends BaseAction
implements sapphire.action.SyncSDIDataSetStatus {
    public static String LABVANTAGE_CVS_ID = "$Revision: 99450 $";
    private String STATUSCOLID = "samplestatus";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String statuscolid = properties.getProperty("statuscolid");
        String auditreason = properties.getProperty("auditreason");
        String auditactivity = properties.getProperty("auditactivity");
        String auditsignedflag = properties.getProperty("auditsignedflag");
        boolean bypassWSCompleteCheck = "Y".equalsIgnoreCase(properties.getProperty("bypassworksheetcompletioncheck"));
        if (sdcid == null || sdcid.length() == 0 || keyid1 == null || keyid1.length() == 0) {
            throw new SapphireException("[SyncSDIDataSetStatus] Invalid action input.");
        }
        if (statuscolid != null && statuscolid.length() > 0) {
            this.STATUSCOLID = statuscolid;
        }
        SDIStatus sdiStatus = new SDIStatus(this.connectionInfo, this.database, bypassWSCompleteCheck);
        sdiStatus.setSdcid(sdcid);
        sdiStatus.setKeyid1(keyid1);
        sdiStatus.setStatuscolid(this.STATUSCOLID);
        List list = sdiStatus.evaluate();
        if (list.size() > 0) {
            StringBuffer sb_keyid1 = new StringBuffer();
            StringBuffer sb_status = new StringBuffer();
            StringBuffer co_keyid1 = new StringBuffer();
            StringBuffer co_status = new StringBuffer();
            StringBuffer co_completedt = new StringBuffer();
            StringBuffer ip_keyid1 = new StringBuffer();
            StringBuffer ip_status = new StringBuffer();
            StringBuffer ip_starttestingdt = new StringBuffer();
            for (int i = 0; i < list.size(); ++i) {
                Sdi sdi = (Sdi)list.get(i);
                String status = sdi.getStatus();
                if (status.equals("Completed")) {
                    co_keyid1.append(sdi.getID()).append(";");
                    co_status.append(sdi.getStatus()).append(";");
                    co_completedt.append("n").append(";");
                    continue;
                }
                if (status.equals("InProgress")) {
                    ip_keyid1.append(sdi.getID()).append(";");
                    ip_status.append(sdi.getStatus()).append(";");
                    ip_starttestingdt.append("n").append(";");
                    continue;
                }
                sb_keyid1.append(sdi.getID()).append(";");
                sb_status.append(sdi.getStatus()).append(";");
            }
            ActionProcessor ap = this.getActionProcessor();
            HashMap<String, String> props = new HashMap<String, String>();
            try {
                if (co_keyid1.length() > 0) {
                    co_keyid1.setLength(co_keyid1.length() - 1);
                    co_status.setLength(co_status.length() - 1);
                    co_completedt.setLength(co_completedt.length() - 1);
                    props.put("sdcid", sdcid);
                    props.put("auditreason", auditreason);
                    props.put("auditactivity", auditactivity);
                    props.put("auditsignedflag", auditsignedflag);
                    props.put("keyid1", co_keyid1.toString());
                    props.put(this.STATUSCOLID, co_status.toString());
                    props.put("completedt", co_completedt.toString());
                    ap.processAction("EditSDI", "1", props);
                }
                if (ip_keyid1.length() > 0) {
                    ip_keyid1.setLength(ip_keyid1.length() - 1);
                    ip_status.setLength(ip_status.length() - 1);
                    ip_starttestingdt.setLength(ip_starttestingdt.length() - 1);
                    props.clear();
                    props.put("sdcid", sdcid);
                    props.put("auditreason", auditreason);
                    props.put("auditactivity", auditactivity);
                    props.put("auditsignedflag", auditsignedflag);
                    props.put("keyid1", ip_keyid1.toString());
                    props.put(this.STATUSCOLID, ip_status.toString());
                    props.put("starttestingdt", ip_starttestingdt.toString());
                    ap.processAction("EditSDI", "1", props);
                }
                if (sb_keyid1.length() > 0) {
                    sb_keyid1.setLength(sb_keyid1.length() - 1);
                    sb_status.setLength(sb_status.length() - 1);
                    String actualStatus = this.resetStatusForReceivedSample(sb_keyid1.toString(), sb_status.toString(), sdiStatus);
                    props.clear();
                    props.put("sdcid", sdcid);
                    props.put("auditreason", auditreason);
                    props.put("auditactivity", auditactivity);
                    props.put("auditsignedflag", auditsignedflag);
                    props.put("keyid1", sb_keyid1.toString());
                    props.put(this.STATUSCOLID, actualStatus);
                    ap.processAction("EditSDI", "1", props);
                }
                if (ap.hasInfoErrors()) {
                    ErrorHandler tempErrorHandler = null;
                    ErrorDetail ruleError = null;
                    tempErrorHandler = ap.getErrorHandler();
                    if (tempErrorHandler != null) {
                        for (int count = 0; count < tempErrorHandler.size(); ++count) {
                            ruleError = (ErrorDetail)tempErrorHandler.get(count);
                            this.setError(ruleError.getErrorid(), ruleError.getErrorType(), ruleError.getMessage());
                        }
                    }
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
    }

    private String resetStatusForReceivedSample(String keyids, String status, SDIStatus sdiStatus) {
        String[] keyidArr = StringUtil.split(keyids, ";");
        String[] statusArr = StringUtil.split(status, ";");
        StringBuffer actualStatus = new StringBuffer();
        for (int i = 0; i < statusArr.length; ++i) {
            if (statusArr[i].equalsIgnoreCase("Initial") && sdiStatus.getReceivedDT(keyidArr[i]) != null && sdiStatus.getReceivedDT(keyidArr[i]).length() > 0) {
                actualStatus.append(";Received");
                continue;
            }
            actualStatus.append(";" + statusArr[i]);
        }
        return actualStatus.substring(1);
    }
}

