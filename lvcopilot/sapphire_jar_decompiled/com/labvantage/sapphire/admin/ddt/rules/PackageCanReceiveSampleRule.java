/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.LV_Package;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class PackageCanReceiveSampleRule
extends BaseRule {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public PackageCanReceiveSampleRule() {
    }

    public PackageCanReceiveSampleRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(String packageid) throws SapphireException {
        String status;
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        StringBuffer error = new StringBuffer();
        if (StringUtil.getLen(packageid) > 0L && (status = LV_Package.getStatus(this.getQueryProcessor(), packageid)) != null && status.trim().length() > 0) {
            String senderDepartmentId;
            if (status.equals("Shipped") || status.equals("Cancelled") || status.equals("Emptied")) {
                error.append("Sample(s) can not be added to ").append(status).append(" Package");
            } else if (status.equals("Received") || status.equals("On Hold")) {
                if (!this.connectionInfo.isDepartmentMember(LV_Package.getRecipientDepartmentId(this.getQueryProcessor(), packageid))) {
                    error.append("User must be a member of Destination custodial domain of the Package");
                }
                if (!this.connectionInfo.hasRole("Repository User")) {
                    error.append("User must be a Repository User.");
                }
            } else if (status.equals("Created") && StringUtil.getLen(senderDepartmentId = LV_Package.getSenderDepartmentId(this.getQueryProcessor(), packageid)) > 0L && !this.connectionInfo.isDepartmentMember(senderDepartmentId)) {
                error.append("User must be a member of Source custodial domain of the Package");
            }
            if (error.length() > 0) {
                throw new SapphireException("PackageCanReceiveSampleRule", "VALIDATION", this.getTranslationProcessor().translate(error.toString()));
            }
        }
    }

    public void processRule(String packageid, String packagestatus, String senderdepartmentid, String recipientdepartmentid) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        StringBuffer error = new StringBuffer();
        if (StringUtil.getLen(packageid) > 0L && packagestatus != null && packagestatus.trim().length() > 0) {
            if (packagestatus.equals("Shipped") || packagestatus.equals("Cancelled") || packagestatus.equals("Emptied")) {
                error.append("Sample(s) can not be added to ").append(packagestatus).append(" Package");
            } else if (packagestatus.equals("Received") || packagestatus.equals("On Hold")) {
                if (!this.connectionInfo.isDepartmentMember(recipientdepartmentid)) {
                    error.append("User must be a member of Destination custodial domain of the Package");
                }
                if (!this.connectionInfo.hasRole("Repository User")) {
                    error.append("User must be a Repository User.");
                }
            } else if (packagestatus.equals("Created") && StringUtil.getLen(senderdepartmentid) > 0L && !this.connectionInfo.isDepartmentMember(senderdepartmentid)) {
                error.append("User must be a member of Source custodial domain of the Package");
            }
            if (error.length() > 0) {
                throw new SapphireException("PackageCanReceiveSampleRule", "VALIDATION", this.getTranslationProcessor().translate(error.toString()));
            }
        }
    }
}

