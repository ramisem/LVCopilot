/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.shared;

import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import java.util.HashMap;

public class DocumentCodes
implements DocumentConstants {
    public static HashMap transMap = new HashMap();
    public static boolean translationsLoaded = false;

    public static String getDocumentStatusText(String status) {
        if (status.equals("DR")) {
            return (String)transMap.get("Draft");
        }
        if (status.equals("PD")) {
            return (String)transMap.get("Pending");
        }
        if (status.equals("PR")) {
            return (String)transMap.get("Pending Reconciliation");
        }
        if (status.equals("PA")) {
            return (String)transMap.get("Pending Approval");
        }
        if (status.equals("RJ")) {
            return (String)transMap.get("Rejected");
        }
        if (status.equals("PP")) {
            return (String)transMap.get("Pending Processing");
        }
        if (status.equals("SM")) {
            return (String)transMap.get("Submitted");
        }
        if (status.equals("DDENEW")) {
            return (String)transMap.get("Submitted / Pending");
        }
        if (status.equals("DDEDR")) {
            return (String)transMap.get("Submitted / Draft");
        }
        if (status.equals("DDESM")) {
            return (String)transMap.get("Submitted / Submitted");
        }
        if (status.equals("DN")) {
            return (String)transMap.get("Done");
        }
        if (status.equals("CN")) {
            return (String)transMap.get("Cancelled");
        }
        if (status.equals("LK")) {
            return (String)transMap.get("Locked");
        }
        if (status.equals("ER")) {
            return (String)transMap.get("Error");
        }
        return (String)transMap.get("New");
    }

    static {
        transMap.put("Draft", "Draft");
        transMap.put("Pending", "Pending");
        transMap.put("Pending Reconciliation", "Pending Reconciliation");
        transMap.put("Pending Approval", "Pending Approval");
        transMap.put("Rejected", "Rejected");
        transMap.put("Pending Processing", "Pending Processing");
        transMap.put("Submitted", "Submitted");
        transMap.put("Submitted / Pending", "Submitted / Pending");
        transMap.put("Submitted / Draft", "Submitted / Draft");
        transMap.put("Submitted / Submitted", "Submitted / Submitted");
        transMap.put("Done", "Done");
        transMap.put("Cancelled", "Cancelled");
        transMap.put("Locked", "Locked");
        transMap.put("Error", "Error");
        transMap.put("New", "New");
    }
}

