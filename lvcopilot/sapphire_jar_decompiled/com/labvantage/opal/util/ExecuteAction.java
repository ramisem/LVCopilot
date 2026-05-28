/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ExecuteAction {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 75115 $";
    public static final String MODULE_POST_SAVE = "Post-Save";
    public static final String MODULE_ACTION_BUTTONS = "Action Buttons";
    public static final int ACTION_BLOCK_SUCCESS = 1;
    public static final int ACTION_BLOCK_FAIL = 2;
    public static final int ACTION_BLOCK_FAIL_UNIDENTIFIED_ERROR = 3;
    public static final String ACTIONSUCCESSURL_PROP = "gotopage";

    public static HashMap getAndRunActions(HashMap hmRequest, HashMap hmVariables, PropertyList plActionProps, PageContext pageContext, boolean needActionJS) throws ActionException {
        StringBuffer sbHtml = new StringBuffer();
        PropertyListCollection plcActions = plActionProps.getCollection("actions");
        ActionProcessor ap = new ActionProcessor(pageContext);
        boolean errorHandlerFound = false;
        HashMap<String, String> props = new HashMap<String, String>();
        HashMap actionBlockInfo = ExecuteAction.processActions(ap, plcActions, hmRequest, pageContext, MODULE_ACTION_BUTTONS);
        ActionBlock actionblock = (ActionBlock)actionBlockInfo.get("actionblock");
        boolean blnSuccess = (Boolean)actionBlockInfo.get("successflag");
        String errMsg = (String)actionBlockInfo.get("errmsg");
        HashMap actionBlockProps = ExecuteAction.getActionBlockProperties(actionblock, plcActions);
        hmVariables.putAll(actionBlockProps);
        if (needActionJS) {
            props.put("jsforacbvariables", ExecuteAction.getJSForACBProps(actionblock));
        }
        sbHtml.append(ExecuteAction.getResetACBIdScript());
        if (blnSuccess) {
            sbHtml.append(ExecuteAction.setAndGetGotoPage(plActionProps, hmVariables, pageContext));
        }
        String feedBackMessage = null;
        if (!errorHandlerFound) {
            feedBackMessage = ExecuteAction.setAndGetFeedback(plActionProps, hmVariables, blnSuccess, ap, pageContext, errMsg);
        }
        String feedbackMsgWithoutHtml = (String)pageContext.getAttribute("message");
        sbHtml.append(ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, feedbackMsgWithoutHtml));
        props.put("acbfeedback", sbHtml.toString());
        String acbstatus = (String)actionBlockInfo.get("actionblockstatus");
        acbstatus = acbstatus != null ? (acbstatus.equalsIgnoreCase("1") ? "true" : "false") : "false";
        props.put("acbstatus", acbstatus);
        return props;
    }

    public static String getRedirectScript(String gotoPage, String releaselock, HashMap hmVariables, PageContext pageContext) {
        StringBuffer sbHtml = new StringBuffer("");
        if (!gotoPage.equalsIgnoreCase("")) {
            ArrayList alTokens = OpalUtil.getKeywordTokens(gotoPage);
            if ((gotoPage = OpalUtil.searchAndReplaceTokens(gotoPage, alTokens, hmVariables, false)).trim().toLowerCase().startsWith("javascript:")) {
                sbHtml.append("<script type=\"text/javascript\">\n");
                String script = gotoPage.trim().substring("javascript:".length()).trim();
                sbHtml.append("try{");
                sbHtml.append("top.sapphire.page.getTop().window.eval('").append(StringUtil.replaceAll(script, "'", "\\'")).append("');");
                sbHtml.append("}catch(__ae1){");
                sbHtml.append("try{");
                sbHtml.append("").append(script).append(!script.endsWith(";") ? ";" : "").append("");
                sbHtml.append("}catch(__ae2){");
                sbHtml.append("top.sapphire.alert('Failed to process post action script.\\nError:' + __ae1.message);");
                sbHtml.append("}");
                sbHtml.append("}\n");
                sbHtml.append("</script>\n");
            } else {
                pageContext.setAttribute(ACTIONSUCCESSURL_PROP, (Object)gotoPage);
                sbHtml.append("Redirecting to page:").append(StringUtil.replaceAll(gotoPage, "<", "&lt;")).append(". Please wait..\n");
                sbHtml.append("<script type=\"text/javascript\">\n");
                sbHtml.append("top.sapphire.page.navigate( '").append(gotoPage).append("', '").append(releaselock).append("' );\n");
                sbHtml.append("</script>\n");
            }
        }
        if (sbHtml.length() > 0) {
            return sbHtml.toString();
        }
        return "";
    }

    private static String setAndGetGotoPage(PropertyList plActionButtonProps, HashMap hmVariables, PageContext pageContext) {
        String gotoPage = plActionButtonProps.getProperty(ACTIONSUCCESSURL_PROP, "");
        String releaselock = plActionButtonProps.getProperty("releaselock", "N");
        return ExecuteAction.getRedirectScript(gotoPage, releaselock, hmVariables, pageContext);
    }

    private static String setAndGetFeedback(PropertyList plActionButtonProps, HashMap hmVariables, boolean blnSuccess, ActionProcessor ap, PageContext pageContext, String errMsg) {
        String feedback = "";
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        StringBuffer sbHtml = new StringBuffer();
        if (pageContext.getAttribute("message") != null && !pageContext.getAttribute("message").equals("")) {
            feedback = (String)pageContext.getAttribute("message") + "<br>";
        }
        if (blnSuccess) {
            feedback = plActionButtonProps.getProperty("successmsg");
            if (feedback == null || feedback.trim().length() == 0) {
                feedback = tp.translate("Operation Successful");
            }
        } else {
            feedback = plActionButtonProps.getProperty("failmsg");
            if (feedback == null || feedback.trim().length() == 0) {
                feedback = tp.translate("Operation Unsuccessful");
            }
            feedback = StringUtil.replaceAll(feedback, "[error]", errMsg);
            feedback = StringUtil.replaceAll(feedback, "[errormsg]", errMsg);
        }
        ArrayList alTokens = OpalUtil.getKeywordTokens(feedback);
        feedback = OpalUtil.searchAndReplaceTokens(feedback, alTokens, hmVariables, false);
        pageContext.setAttribute("message", (Object)feedback);
        if (feedback.length() > 0) {
            sbHtml.append("<table cellspacing=0 cellpadding=10 border=1 bordercolor=\"#b0c4de\" style=\"margin:10px\">\n<tr><td>");
            sbHtml.append(feedback).append("</td></tr></table>");
            feedback = sbHtml.toString();
        }
        return feedback;
    }

    public static ActionBlock processActions(PropertyListCollection actions, HashMap actionParams, PageContext pageContext, String module) throws ActionException {
        HashMap actionBlockInfo = ExecuteAction.processActions(new ActionProcessor(pageContext), actions, actionParams, pageContext, module);
        ActionBlock returnActionBlock = (ActionBlock)actionBlockInfo.get("actionblock");
        return returnActionBlock;
    }

    private static HashMap processActions(ActionProcessor actionProcessor, PropertyListCollection actions, HashMap actionParams, PageContext pageContext, String module) throws ActionException {
        boolean processFlag = true;
        ArrayList<String> outputPropsVarList = new ArrayList<String>();
        HashMap<String, Object> actionBlockInfo = new HashMap<String, Object>();
        ActionBlock actionBlock = new ActionBlock();
        boolean blnSuccess = false;
        String errMsg = "";
        if (actions == null || actions.equals("")) {
            Logger.logError(module + ": 'actions' is null");
        } else {
            PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
            DataSet selected = null;
            if (actionParams != null && actionParams.containsKey("selectedvalues") && actionParams.containsKey("selectedcolumns")) {
                selected = new DataSet(actionParams.get("selectedcolumns").toString(), actionParams.get("selectedvalues").toString());
                Logger.logDebug("Selected Values and Selected Columns provided and used to build dataset (rowcount = " + selected.getRowCount() + ").");
                if (actionParams.containsKey("keyid1") && actionParams.get("keyid1").toString().length() > 0) {
                    String sdcid;
                    String string = sdcid = actionParams.containsKey("sdcid") ? actionParams.get("sdcid").toString() : "";
                    if (sdcid.length() > 0) {
                        SDCProcessor sdc = new SDCProcessor(pageContext);
                        String keycolid1 = sdc.getProperty(sdcid, "keycolid1");
                        if (keycolid1.length() > 0 && selected.isValidColumn(keycolid1) && actionParams.containsKey("keyid1")) {
                            String keycolid3;
                            String keycolid2;
                            String selkeys = selected.getColumnValues(keycolid1, ";");
                            if (!selkeys.equalsIgnoreCase(actionParams.get("keyid1").toString())) {
                                Logger.logWarn("Selected keyid1 and action_keyid1 do not match.");
                                Logger.logDebug("selkeys 1 = " + selkeys);
                                Logger.logDebug("action_keyid1 = " + actionParams.get("keyid1").toString());
                            }
                            if ((keycolid2 = sdc.getProperty(sdcid, "keycolid2")).length() > 0 && selected.isValidColumn(keycolid2) && actionParams.containsKey("keyid2") && !(selkeys = selected.getColumnValues(keycolid2, ";")).equalsIgnoreCase(actionParams.get("keyid2").toString())) {
                                Logger.logWarn("Selected keyid2 and action_keyid2 do not match.");
                                Logger.logDebug("selkeys 2 = " + selkeys);
                                Logger.logDebug("action_keyid2 = " + actionParams.get("keyid2").toString());
                            }
                            if ((keycolid3 = sdc.getProperty(sdcid, "keycolid3")).length() > 0 && selected.isValidColumn(keycolid3) && actionParams.containsKey("keyid3") && !(selkeys = selected.getColumnValues(keycolid3, ";")).equalsIgnoreCase(actionParams.get("keyid3").toString())) {
                                Logger.logWarn("Selected keyid3 and action_keyid3 do not match.");
                                Logger.logDebug("selkeys 3 = " + selkeys);
                                Logger.logDebug("action_keyid3 = " + actionParams.get("keyid3").toString());
                            }
                        } else {
                            Logger.logDebug("No key column detected so selection not checked.");
                        }
                    } else {
                        Logger.logDebug("No sdcid detected so selection not checked.");
                    }
                }
            }
            for (int i = 0; i < actions.size(); ++i) {
                String auditSignedFlag;
                String auditActivity;
                String auditReason;
                HashMap<String, String> inputmap = new HashMap<String, String>();
                PropertyList action = actions.getPropertyList(i);
                String actionid = action.getProperty("actionid");
                String versionid = action.getProperty("versionid");
                if (actionid.length() == 0) continue;
                boolean actionClass = actionid.startsWith("class:");
                if (OpalUtil.isEmpty(actionid) || !actionClass && OpalUtil.isEmpty(versionid)) {
                    Logger.logError(module + " 'actionid' " + (actionClass ? "or 'versionid' " : "") + "is null");
                    processFlag = false;
                    break;
                }
                String asynchronous = action.getProperty("asynchronous");
                String delay = action.getProperty("delay");
                if (asynchronous == null || asynchronous.equals("")) {
                    asynchronous = "N";
                }
                if (delay == null || delay.equals("")) {
                    delay = "0";
                }
                if (!OpalUtil.isEmpty(actionid)) {
                    PropertyListCollection outputprops;
                    PropertyListCollection inputprops = action.getCollection("properties");
                    if (inputprops == null) {
                        inputprops = action.getCollection("inputprops");
                    }
                    if (inputprops != null) {
                        for (int _i = 0; _i < inputprops.size(); ++_i) {
                            ArrayList tokens;
                            PropertyList input = inputprops.getPropertyList(_i);
                            String propertyid = input.getProperty("propertyid");
                            String value = input.getProperty("value");
                            if (propertyid == null || propertyid.equals("") || value == null || value.equals("")) continue;
                            if (value.indexOf("[") >= 0 && (value = OpalUtil.searchAndReplaceTokens(value, tokens = OpalUtil.getKeywordTokens(value), actionParams, false)).indexOf("[") > -1) {
                                tokens = OpalUtil.getKeywordTokens(value);
                                for (int count = 0; count < tokens.size(); ++count) {
                                    String token = (String)tokens.get(count);
                                    if (outputPropsVarList.contains("[" + token + "]")) continue;
                                    value = selected != null && selected.isValidColumn(token) ? StringUtil.replaceAll(value, "[" + token + "]", selected.getColumnValues(token, ";"), true) : StringUtil.replaceAll(value, "[" + token + "]", "", true);
                                }
                            }
                            inputmap.put(propertyid, value);
                        }
                    } else {
                        Logger.logError("Missing 'inputprops'");
                        processFlag = false;
                    }
                    if (asynchronous.equalsIgnoreCase("N") && (outputprops = action.getCollection("outputprops")) != null) {
                        for (int _i = 0; _i < outputprops.size(); ++_i) {
                            PropertyList output = outputprops.getPropertyList(_i);
                            String propertyid = output.getProperty("propertyid");
                            String variable = output.getProperty("variable");
                            if (propertyid == null || propertyid.equals("") || variable == null || variable.equals("")) continue;
                            inputmap.put(propertyid, variable);
                            outputPropsVarList.add(variable);
                        }
                    }
                }
                ExecuteAction.getActionExtraProps(inputmap, pagedata);
                String traceLogId = (String)actionParams.get("tracelogid");
                if (traceLogId != null && traceLogId.length() > 0) {
                    inputmap.put("tracelogid", traceLogId);
                }
                if ((auditReason = (String)actionParams.get("auditreason")) != null && auditReason.length() > 0 && !inputmap.containsKey("auditreason")) {
                    inputmap.put("auditreason", auditReason);
                }
                if ((auditActivity = (String)actionParams.get("auditactivity")) != null && auditActivity.length() > 0 && !inputmap.containsKey("auditactivity")) {
                    inputmap.put("auditactivity", auditActivity);
                }
                if ((auditSignedFlag = (String)actionParams.get("auditsignedflag")) != null && auditSignedFlag.length() > 0 && !inputmap.containsKey("auditsignedflag")) {
                    inputmap.put("auditsignedflag", auditSignedFlag);
                }
                if (asynchronous.equalsIgnoreCase("Y")) {
                    if (actionClass) {
                        inputmap.put("actionclass", actionid.substring(6));
                    } else {
                        inputmap.put("actionid", actionid);
                        inputmap.put("actionversionid", versionid);
                    }
                    inputmap.put("duedate", "n+" + delay + "s");
                    inputmap.put("delete", "Y");
                    actionBlock.setAction("action" + i, "AddToDoListEntry", "1", inputmap);
                    continue;
                }
                if (actionClass) {
                    actionBlock.setActionClass("action" + i, actionid.substring(6), inputmap);
                    continue;
                }
                actionBlock.setAction("action" + i, actionid, versionid, inputmap);
            }
            if (processFlag) {
                try {
                    actionProcessor.processActionBlock(actionBlock);
                    blnSuccess = true;
                    actionBlockInfo.put("actionblockstatus", Integer.toString(1));
                    actionBlockInfo.put("errorhandler", actionProcessor.getErrorHandler());
                }
                catch (ActionException ae) {
                    blnSuccess = false;
                    ErrorHandler errorHandler = ae.getErrorHandler();
                    int errorStackIndex = 0;
                    if (errorHandler != null) {
                        errorStackIndex = errorHandler.size();
                    }
                    errMsg = actionProcessor.getErrorStack() != null && actionProcessor.getErrorStack().size() > 0 ? (errorStackIndex < actionProcessor.getErrorStack().size() ? actionProcessor.getErrorStack().get(errorStackIndex).toString() : actionProcessor.getErrorStack().get(0).toString()) : ae.getMessage();
                    if (errorHandler != null && errorHandler.hasErrors()) {
                        actionBlockInfo.put("actionblockstatus", Integer.toString(2));
                        actionBlockInfo.put("errorhandler", errorHandler);
                    }
                    actionBlockInfo.put("actionblockstatus", Integer.toString(3));
                    actionBlockInfo.put("errorhandler", actionProcessor.getErrorHandler());
                    actionBlockInfo.put("errormsg", actionProcessor.getErrorStack().get(0).toString());
                }
            }
        }
        actionBlockInfo.put("actionblock", actionBlock);
        actionBlockInfo.put("successflag", blnSuccess);
        actionBlockInfo.put("errmsg", errMsg);
        return actionBlockInfo;
    }

    public static HashMap getActionParameters(PropertyList pagedata) {
        String value;
        HashMap<String, String> map = new HashMap<String, String>();
        for (String key : pagedata.keySet()) {
            if (key.startsWith("action_")) {
                map.put(key.substring(7), pagedata.getProperty(key));
                continue;
            }
            if (!"tracelogid".equals(key)) continue;
            map.put("tracelogid", pagedata.getProperty(key));
        }
        if (map.containsKey("keyid2")) {
            value = (String)map.get("keyid2");
            if (value == null || value.length() == 0) {
                map.put("keyid2", "(null)");
            }
        } else {
            map.put("keyid2", "(null)");
        }
        if (map.containsKey("keyid3")) {
            value = (String)map.get("keyid3");
            if (value == null || value.length() == 0) {
                map.put("keyid3", "(null)");
            }
        } else {
            map.put("keyid3", "(null)");
        }
        String extraProps = pagedata.getProperty("__pr_extraprops");
        if (extraProps != null) {
            String auditSignedFlag;
            String auditActivity;
            HashMap extraPropsMap = OpalUtil.parseExtraProps(extraProps);
            String auditReason = (String)extraPropsMap.get("auditreason");
            if (auditReason != null) {
                map.put("auditreason", auditReason);
            }
            if ((auditActivity = (String)extraPropsMap.get("auditactivity")) != null) {
                map.put("auditactivity", auditActivity);
            }
            if ((auditSignedFlag = (String)extraPropsMap.get("auditsignedflag")) != null) {
                map.put("auditsignedflag", auditSignedFlag);
            }
        }
        return map;
    }

    public static HashMap getActionExtraProps(HashMap map, PropertyList pagedata) {
        String extraProps = pagedata.getProperty("__pr_extraprops");
        if (!OpalUtil.isEmpty(extraProps)) {
            String[] props = StringUtil.split(extraProps, ";");
            try {
                for (int i = 0; i < props.length; ++i) {
                    String s = props[i];
                    if (OpalUtil.isEmpty(s)) continue;
                    String key = s.substring(0, s.indexOf("="));
                    String value = s.substring(s.indexOf("=") + 1, s.length());
                    if (map.containsKey(key)) continue;
                    map.put(key, value);
                }
            }
            catch (Exception e) {
                Logger.logError("Invalid \"__pr_extraprops\"", e);
            }
        }
        return map;
    }

    public static String executePostSaveActions(PageContext pageContext) {
        HashMap props = ExecuteAction.executePostSaveActions(pageContext, false);
        String feedback = (String)props.get("message");
        pageContext.setAttribute("message", (Object)feedback);
        return feedback;
    }

    public static HashMap executePostSaveActions(PageContext pageContext, boolean needErrorHandler) {
        ActionProcessor actionProcessor = new ActionProcessor(pageContext);
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        HashMap<String, Object> returnProps = new HashMap<String, Object>();
        returnProps.put("message", "");
        ActionBlock actionblock = null;
        String feedback = "";
        String postSaveSubHeader = tp.translate("Save");
        HashMap actionBlockInfo = new HashMap();
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
        PropertyListCollection actions = pagedata.getCollection("actions");
        actions = ExecuteAction.getValidActions(actions);
        if (actions != null && actions.size() > 0) {
            String errMsg;
            boolean blnSuccess;
            HashMap actionParams = ExecuteAction.getActionParameters(pagedata);
            try {
                actionBlockInfo = ExecuteAction.processActions(actionProcessor, actions, actionParams, pageContext, MODULE_POST_SAVE);
                actionblock = (ActionBlock)actionBlockInfo.get("actionblock");
                blnSuccess = (Boolean)actionBlockInfo.get("successflag");
                errMsg = (String)actionBlockInfo.get("errmsg");
                returnProps.put("errorhandler", actionBlockInfo.get("errorhandler"));
            }
            catch (ActionException ae) {
                actionBlockInfo.put("actionblockstatus", Integer.toString(2));
                Logger.logError(ae.getMessage(), ae);
                errMsg = ae.getMessage();
                blnSuccess = false;
                returnProps.put("errorhandler", ae.getErrorHandler());
            }
            HashMap actionBlockProps = ExecuteAction.getActionBlockProperties(actionblock, actions);
            actionParams.putAll(actionBlockProps);
            PropertyList postSaveActionProps = pagedata.getPropertyList("postsaveactionprops");
            feedback = ExecuteAction.getPostSaveSuccessFailMsg(postSaveActionProps, blnSuccess, tp);
            feedback = ExecuteAction.getFeedback(feedback, actionParams, blnSuccess, pageContext, errMsg);
            ErrorHandler errorHandler = (ErrorHandler)returnProps.get("errorhandler");
            String feedBackWithoutHtml = (String)pageContext.getAttribute("message");
            HashMap props = ExecuteAction.addMsgToErrorHandler(pageContext, actionBlockInfo, feedBackWithoutHtml, postSaveSubHeader, postSaveSubHeader);
            errorHandler = (ErrorHandler)props.get("errorhandler");
            pageContext.setAttribute("message", (Object)feedback);
            returnProps.put("errorhandler", errorHandler);
            returnProps.put("message", feedback);
            returnProps.put("errormsg", errMsg);
        }
        return returnProps;
    }

    private static String getFeedback(String feedback, HashMap hmVariables, boolean blnSuccess, PageContext pageContext, String errMsg) {
        String evaluatedFeedBack = "";
        StringBuffer sbHtml = new StringBuffer("");
        if (pageContext.getAttribute("message") != null && !pageContext.getAttribute("message").equals("")) {
            feedback = pageContext.getAttribute("message") + "<br>" + feedback;
        }
        if (!blnSuccess) {
            feedback = StringUtil.replaceAll(feedback, "[error]", errMsg);
        }
        ArrayList alTokens = OpalUtil.getKeywordTokens(feedback);
        evaluatedFeedBack = OpalUtil.searchAndReplaceTokens(feedback, alTokens, hmVariables, false);
        pageContext.setAttribute("message", (Object)evaluatedFeedBack);
        if (!evaluatedFeedBack.equalsIgnoreCase("")) {
            sbHtml.append("<table cellspacing=0 cellpadding=10 border=1 bordercolor=\"#b0c4de\" style=\"margin:10px\">\n<tr><td>");
            sbHtml.append(evaluatedFeedBack).append("</td></tr>\n</table>\n");
            evaluatedFeedBack = sbHtml.toString();
        }
        return evaluatedFeedBack;
    }

    private static PropertyListCollection getValidActions(PropertyListCollection actions) {
        PropertyListCollection validActions = new PropertyListCollection();
        if (actions != null && actions.size() > 0) {
            for (int count = 0; count < actions.size(); ++count) {
                PropertyList action = actions.getPropertyList(count);
                String actionId = action.getProperty("actionid");
                String actionVersionId = action.getProperty("versionid");
                if (actionId == null || actionId.trim().length() <= 0 || actionVersionId == null || actionVersionId.trim().length() <= 0) continue;
                validActions.add(action);
            }
        }
        return validActions;
    }

    private static String getJSForACBProps(ActionBlock actionblock) {
        StringBuffer html = new StringBuffer();
        html.append("\n<script language='javascript' type='text/javascript' >\n").append("\tvar actionsArr = new Array(); \n").append("\tvar actionProps; \n").append("\tvar action; \n");
        try {
            if (actionblock != null) {
                for (int actionCount = 0; actionCount < actionblock.getActionCount(); ++actionCount) {
                    String actionid = actionblock.getActionid(actionCount);
                    String actionversionid = actionblock.getVersionid(actionCount);
                    String actionname = "action" + actionCount;
                    HashMap props = actionblock.getActionProperties(actionCount);
                    html.append("actionProps = new ActionProperties();\n");
                    Set testSet = props.entrySet();
                    for (Map.Entry entry : testSet) {
                        html.append("actionProps.put( '").append(entry.getKey()).append("', '").append(entry.getValue()).append("' ); \n");
                    }
                    html.append("action = new Action( '").append(actionid).append("', '").append(actionversionid).append("', actionProps, '").append(actionname).append("' ); \n ");
                    html.append("actionsArr[ actionsArr.length ] = action; \n ");
                }
            }
        }
        catch (Exception ex) {
            Trace.logError("Exception caught: ", (Object)ex.getMessage(), ex);
        }
        html.append("</script>");
        return html.toString();
    }

    public static String getErrorMessage(PageContext pageContext, HashMap actionBlockInfo, String msg) {
        StringBuffer html = new StringBuffer();
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        ErrorHandler errorHandler = (ErrorHandler)actionBlockInfo.get("errorhandler");
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String successMsg = tp.translate("Action");
        String failMsg = tp.translate("Action");
        HashMap props = ExecuteAction.addMsgToErrorHandler(pageContext, actionBlockInfo, msg, successMsg, failMsg);
        errorHandler = (ErrorHandler)props.get("errorhandler");
        html.append("<div id='errordiv' style='max-height:300px;overflow-y:auto;'>");
        if (actionBlockStatus == 1) {
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                html.append(ErrorUtil.getErrorHTML(pageContext, errorHandler, true, null));
            }
        } else if (actionBlockStatus == 2) {
            if (errorHandler != null && errorHandler.hasErrors()) {
                html.append(ErrorUtil.getErrorHTML(pageContext, errorHandler, true, null));
            }
        } else {
            html.append(ErrorUtil.getErrorHTML(pageContext, errorHandler, true, null));
        }
        html.append("</div>");
        return html.toString();
    }

    private static int getErrorCount(ErrorHandler errorHandler, String type) {
        int errorCount = 0;
        if ("all".equalsIgnoreCase(type)) {
            return errorHandler.size();
        }
        for (int count = 0; count < errorHandler.size(); ++count) {
            ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(count);
            if (!type.equalsIgnoreCase(errorDetail.getErrorType())) continue;
            ++errorCount;
        }
        return errorCount;
    }

    private static String getResetACBIdScript() {
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<script language='javascript'>\n");
        sbHtml.append("    try{\n");
        sbHtml.append("        parent.submitdata.actionbuttonid.value='';\n");
        sbHtml.append("        parent.submitdata.mode.value='';\n");
        sbHtml.append("    }\n");
        sbHtml.append("    catch(err){}\n");
        sbHtml.append("</script>\n");
        return sbHtml.toString();
    }

    private static String getPostSaveSuccessFailMsg(PropertyList postSaveActionProps, boolean blnSuccess, TranslationProcessor tp) {
        String feedback = "";
        if (postSaveActionProps != null) {
            if (blnSuccess) {
                feedback = postSaveActionProps.getProperty("successmsg");
                if (feedback == null || feedback.trim().length() == 0) {
                    feedback = tp.translate("Operation Successful");
                }
            } else {
                feedback = postSaveActionProps.getProperty("failmsg");
                if (feedback == null || feedback.trim().length() == 0) {
                    feedback = tp.translate("Operation Unsuccessful");
                }
            }
        }
        return feedback;
    }

    private static HashMap getActionBlockProperties(ActionBlock actionblock, PropertyListCollection actions) {
        HashMap actionBlockProps = new HashMap();
        if (actionblock != null) {
            actionBlockProps.putAll(actionblock.getBlockProperties());
            try {
                for (int i = 0; i < actions.size(); ++i) {
                    actionBlockProps.putAll(actionblock.getActionProperties("action" + i));
                }
            }
            catch (Exception ex) {
                Trace.logError(" Exception caught in getting action Properties: ", (Object)ex.getMessage(), ex);
            }
        }
        return actionBlockProps;
    }

    private static HashMap addMsgToErrorHandler(PageContext pageContext, HashMap actionBlockInfo, String msg, String successMsgTitle, String failMsgTitle) {
        HashMap<String, ErrorHandler> props = new HashMap<String, ErrorHandler>();
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        ErrorHandler errorHandler = (ErrorHandler)actionBlockInfo.get("errorhandler");
        if (errorHandler == null) {
            errorHandler = new ErrorHandler();
        }
        if (actionBlockStatus == 1) {
            if (msg != null && msg.trim().length() > 0) {
                errorHandler.add("", "", successMsgTitle, "INFORMATION", msg);
            }
        } else if (actionBlockStatus == 2) {
            if (msg != null && msg.trim().length() > 0) {
                int validationErrorCount = ExecuteAction.getErrorCount(errorHandler, "VALIDATION");
                int confirmErrorCount = ExecuteAction.getErrorCount(errorHandler, "CONFIRM");
                if (validationErrorCount != 0 || confirmErrorCount <= 0) {
                    errorHandler.add("", "", failMsgTitle, "VALIDATION", msg);
                }
            }
        } else {
            errorHandler.add("", "", failMsgTitle, "VALIDATION", msg);
        }
        props.put("errorhandler", errorHandler);
        return props;
    }
}

