/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.shared;

import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import java.util.HashMap;

public class WorkflowManagerCodes
implements WorkflowManagerConstants {
    public static HashMap transMap = new HashMap();

    public static String getWorkflowExecStatusText(String status) {
        if (status.equals("A")) {
            return (String)transMap.get("Active");
        }
        if (status.equals("P")) {
            return (String)transMap.get("Suspended");
        }
        if (status.equals("C")) {
            return (String)transMap.get("Complete");
        }
        if (status.equals("X")) {
            return (String)transMap.get("Cancelled");
        }
        return (String)transMap.get("Active");
    }

    public static String getWorkflowExecStatusImage(String status) {
        if (status.equals("A")) {
            return "WEB-CORE/images/png/BatchStage.png";
        }
        if (status.equals("P")) {
            return "WEB-CORE/images/png/Pause.png";
        }
        if (status.equals("C")) {
            return "WEB-CORE/images/gif/ChequeredFlag.gif";
        }
        if (status.equals("X")) {
            return "WEB-CORE/images/png/Cancel.png";
        }
        return "WEB-CORE/images/png/BatchStage.png";
    }

    public static String getWorkflowExecutionsText(String status) {
        if (status.equals("A")) {
            return (String)transMap.get("Active Executions");
        }
        if (status.equals("P")) {
            return (String)transMap.get("Suspended Executions");
        }
        if (status.equals("C")) {
            return (String)transMap.get("Complete Executions");
        }
        if (status.equals("X")) {
            return (String)transMap.get("Cancelled Executions");
        }
        return (String)transMap.get("Active Executions");
    }

    public static String getWorkflowExecTypeText(String exectype) {
        if (exectype.equals("A")) {
            return (String)transMap.get("Auto");
        }
        if (exectype.equals("N")) {
            return (String)transMap.get("Named");
        }
        return (String)transMap.get("Single");
    }

    static {
        transMap.put("Active", "Active");
        transMap.put("Suspended", "Suspended");
        transMap.put("Complete", "Complete");
        transMap.put("Cancelled", "Cancelled");
        transMap.put("Active Executions", "Active Executions");
        transMap.put("Suspended Executions", "Suspended Executions");
        transMap.put("Complete Executions", "Complete Executions");
        transMap.put("Cancelled Executions", "Cancelled Executions");
        transMap.put("Auto", "Auto");
        transMap.put("Named", "Named");
        transMap.put("Single", "Single");
    }
}

