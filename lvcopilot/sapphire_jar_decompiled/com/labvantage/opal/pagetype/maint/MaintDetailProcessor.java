/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.pagetype.maint;

import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.ActionBlock;

public class MaintDetailProcessor {
    public static HashMap processMaintDetail(PageContext pageContext) {
        HashMap retHashMap = new HashMap();
        ArrayList actionProps = new ArrayList();
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        String action = req.getParameter("action1");
        String actionId = "";
        if (action == null || action.length() == 0) {
            return retHashMap;
        }
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", req.getParameter("sdcid"));
        keyid1 = req.getParameter("detailkeyid1");
        props.put("keyid1", keyid1);
        keyid2 = req.getParameter("detailkeyid2");
        keyid3 = req.getParameter("detailkeyid3");
        keyid2 = keyid2 == null || keyid2.equalsIgnoreCase("null") || keyid2.equals("") ? "(null)" : keyid2;
        keyid3 = keyid3 == null || keyid3.equalsIgnoreCase("null") || keyid3.equals("") ? "(null)" : keyid3;
        props.put("keyid2", keyid2);
        props.put("keyid3", keyid3);
        actionId = MaintDetailProcessor.getActionId(action);
        if (action.equalsIgnoreCase("deletedataset")) {
            props.put("paramlistid", req.getParameter("paramlistid"));
            props.put("paramlistversionid", req.getParameter("paramlistversionid"));
            props.put("variantid", req.getParameter("variantid"));
            props.put("dataset", req.getParameter("dataset"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("deletespec")) {
            String specId = req.getParameter("specid");
            String specVersionId = req.getParameter("specversionid");
            String[] specIdArray = specId.split(";");
            String[] specVersionIdArray = specVersionId.split(";");
            for (int specCount = 0; specCount < specIdArray.length && specCount < specVersionIdArray.length; ++specCount) {
                props = new HashMap();
                props.put("sdcid", req.getParameter("sdcid"));
                props.put("keyid1", keyid1);
                props.put("keyid2", keyid2);
                props.put("keyid3", keyid3);
                props.put("specid", specIdArray[specCount]);
                props.put("specversionid", specVersionIdArray[specCount]);
                actionProps.add(props);
            }
        } else if (action.equalsIgnoreCase("deleteaddress")) {
            props.put("addressid", req.getParameter("addressid"));
            props.put("addresstype", req.getParameter("addresstype"));
            props.put("contactfunction", req.getParameter("contactfunction"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("deleteattachment")) {
            props.put("attachmentnum", req.getParameter("attachmentnumber"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("deleteworkitem")) {
            props.put("workitemid", req.getParameter("workitemid"));
            props.put("workiteminstance", req.getParameter("workiteminstance"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("adddataset")) {
            props.put("paramlistid", req.getParameter("paramlistid"));
            props.put("paramlistversionid", req.getParameter("paramlistversionid"));
            props.put("variantid", req.getParameter("variantid"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("addspec")) {
            props.put("specid", req.getParameter("specid"));
            props.put("specversionid", req.getParameter("specversionid"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("addaddress")) {
            props.put("addressid", req.getParameter("addressid"));
            props.put("addresstype", req.getParameter("addresstype"));
            props.put("contactfunction", req.getParameter("contactfunction"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("addworkitem")) {
            props.put("workitemid", req.getParameter("workitemid"));
            props.put("workitemversionid", req.getParameter("workitemversionid"));
            props.put("applyworkitem", req.getParameter("applyworkitem"));
            actionProps.add(props);
        } else if (action.equalsIgnoreCase("applyworkitem")) {
            props.put("workitemid", req.getParameter("workitemid"));
            props.put("workiteminstance", req.getParameter("workiteminstance"));
            actionProps.add(props);
        }
        retHashMap = MaintDetailProcessor.executeAction(pageContext, actionId, actionProps);
        return retHashMap;
    }

    public static String getActionId(String action) {
        String actionId = "";
        if (action.equalsIgnoreCase("deletedataset")) {
            actionId = "DeleteDataSet";
        } else if (action.equalsIgnoreCase("deletespec")) {
            actionId = "RemoveSDISpec";
        } else if (action.equalsIgnoreCase("deleteattachment")) {
            actionId = "DeleteSDIAttachment";
        } else if (action.equalsIgnoreCase("deleteaddress")) {
            actionId = "DeleteSDIAddress";
        } else if (action.equalsIgnoreCase("deleteworkitem")) {
            actionId = "DeleteSDIWorkItem";
        } else if (action.equalsIgnoreCase("addspec")) {
            actionId = "AddSDISpec";
        } else if (action.equalsIgnoreCase("adddataset")) {
            actionId = "AddDataSet";
        } else if (action.equalsIgnoreCase("addaddress")) {
            actionId = "AddSDIAddress";
        } else if (action.equalsIgnoreCase("addworkitem")) {
            actionId = "AddSDIWorkitem";
        } else if (action.equalsIgnoreCase("applyworkitem")) {
            actionId = "ApplySDIWorkItem";
        }
        return actionId;
    }

    public static HashMap executeAction(PageContext pageContext, String actionId, ArrayList actionProps) {
        HashMap<String, String> retHashMap = new HashMap<String, String>();
        ActionProcessor ap = new ActionProcessor(pageContext);
        ActionBlock ab = new ActionBlock();
        StringBuffer msg = new StringBuffer();
        HashMap props = new HashMap();
        boolean anyActionFailed = false;
        String retCode = "";
        try {
            int actionCount;
            for (actionCount = 0; actionCount < actionProps.size(); ++actionCount) {
                ab.setAction(actionId + actionCount, actionId, "1");
                ab.setActionProperties(actionId + actionCount, (HashMap)actionProps.get(actionCount));
            }
            ap.processActionBlock(ab);
            for (actionCount = 0; actionCount < actionProps.size(); ++actionCount) {
                retCode = ab.getActionProperty(actionId + actionCount, "(return)");
                if (retCode == null || retCode.equals("1")) continue;
                anyActionFailed = true;
                msg.append(actionId + actionCount + " Action returned " + retCode + "\n ");
            }
        }
        catch (ActionException e) {
            msg.append("Action Exception caught.  Check log for more details");
            Trace.logError("Action Exception: " + e.toString(), e);
        }
        if (anyActionFailed) {
            retHashMap.put("(return)", "2");
            retHashMap.put("retMsg", msg.toString());
        } else {
            retHashMap.put("(return)", "1");
        }
        return retHashMap;
    }
}

