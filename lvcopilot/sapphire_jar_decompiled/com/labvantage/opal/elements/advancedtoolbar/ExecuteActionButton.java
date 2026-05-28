/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedtoolbar;

import com.labvantage.opal.util.ExecuteAction;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.file.FileManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ExecuteActionButton {
    static String LABVANTAGE_CVS_ID = "$Revision: 62122 $";

    public static String execute(PageContext pageContext) {
        String acbFeedback = "";
        HashMap props = ExecuteActionButton.execute(pageContext, false);
        if (props != null && (acbFeedback = (String)props.get("acbfeedback")) == null) {
            acbFeedback = "";
        }
        return acbFeedback;
    }

    public static HashMap execute(PageContext pageContext, boolean needActions) {
        HashMap<String, String> props = new HashMap<String, String>();
        boolean executeActionButton = true;
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        String actionTransactionId = request.getParameter("actiontransactionid");
        if (StringUtil.getLen(actionTransactionId) > 0L) {
            HashSet<String> actionButtonCallsSet = (HashSet<String>)request.getSession().getAttribute("actiontransactionid_set");
            if (actionButtonCallsSet != null) {
                if (actionButtonCallsSet.add(actionTransactionId)) {
                    request.getSession().setAttribute("actiontransactionid_set", actionButtonCallsSet);
                } else {
                    executeActionButton = false;
                }
            } else {
                actionButtonCallsSet = new HashSet<String>();
                actionButtonCallsSet.add(actionTransactionId);
                request.getSession().setAttribute("actiontransactionid_set", actionButtonCallsSet);
            }
        }
        if (executeActionButton) {
            StringBuilder sbHtml;
            String acbstatus;
            block20: {
                String advancedToolbarId = "";
                String actionButtonId = "";
                acbstatus = "false";
                sbHtml = new StringBuilder();
                HashMap hmVariables = new HashMap();
                try {
                    PropertyList plAdvancedToolbar;
                    PropertyList plPagedata = (PropertyList)pageContext.getAttribute("pagedata", 2);
                    if (plPagedata == null) {
                        throw new Exception("OPAL_ERR: ExecuteActionButton.execute. Failed to find request parameters.");
                    }
                    actionButtonId = plPagedata.getProperty("actionbuttonid");
                    if (actionButtonId == null || actionButtonId.equals("")) {
                        return props;
                    }
                    HashMap hmRequest = ExecuteAction.getActionParameters(plPagedata);
                    Iterator keys = hmRequest.keySet().iterator();
                    String cid = RequestContext.getInstance(request).getConnectionId();
                    while (keys.hasNext()) {
                        String[] parts;
                        String key = keys.next().toString();
                        String value = hmRequest.get(key).toString();
                        if (!value.startsWith("##FILEUPLOAD##") || (parts = StringUtil.split(value.substring(14), ";")).length < 2) continue;
                        Path path = Paths.get(FileManager.getFileLocation(parts[0].length() > 0 ? parts[0] : "Upload Custom", parts.length > 2 && parts[1].length() > 0 ? parts[1] : "", cid), new String[0]);
                        path = path.resolve(parts[parts.length - 1]);
                        hmRequest.put(key, path.toString());
                    }
                    hmVariables.putAll(hmRequest);
                    advancedToolbarId = plPagedata.getProperty("advancedtoolbarid");
                    if (advancedToolbarId == null || advancedToolbarId.equals("")) {
                        advancedToolbarId = "advancedtoolbar";
                    }
                    if ((plAdvancedToolbar = (PropertyList)pageContext.getAttribute(advancedToolbarId, 2)) == null) {
                        throw new Exception("Failed to find properties for the advanced toolbar element.");
                    }
                    PropertyListCollection plcButtons = plAdvancedToolbar.getCollection("buttons");
                    if (plcButtons == null) {
                        throw new Exception("Failed to find properties for the buttons collection in the advanced toolbar element.");
                    }
                    if (actionButtonId.equals("")) break block20;
                    props.put("advancedtoolbarid", advancedToolbarId);
                    props.put("actionbuttonid", actionButtonId);
                    for (int i = 0; i < plcButtons.size(); ++i) {
                        PropertyList plButton = plcButtons.getPropertyList(i);
                        String buttonType = plButton.getProperty("buttontype");
                        if (buttonType == null || !buttonType.equalsIgnoreCase("Action")) continue;
                        String buttonText = plButton.getPropertyList("commonprops").getProperty("text");
                        String buttonid = plButton.getProperty("id");
                        if (buttonText == null) {
                            buttonText = "";
                        }
                        if (buttonid == null) {
                            buttonid = "";
                        }
                        if (!buttonid.equalsIgnoreCase(actionButtonId) && !buttonText.equalsIgnoreCase(actionButtonId)) continue;
                        PropertyList plActionButtonProps = plButton.getPropertyList("actionbuttonprops");
                        String acbCallback = plActionButtonProps.getProperty("acbcallback");
                        if (acbCallback.trim().length() == 0) {
                            acbCallback = "showACBFeedback";
                        }
                        props.put("acbcallback", acbCallback);
                        String advancedToolbarIdForm = plActionButtonProps.getProperty("actionbuttonform");
                        props.put("jstoclearactionbuttonid", ExecuteActionButton.getJSToClearToolbarProps(advancedToolbarIdForm));
                        HashMap acbProps = ExecuteAction.getAndRunActions(hmRequest, hmVariables, plActionButtonProps, pageContext, true);
                        props.put("acbfeedback", (String)acbProps.get("acbfeedback"));
                        props.put("jsforacbvariables", (String)acbProps.get("jsforacbvariables"));
                        acbstatus = (String)acbProps.get("acbstatus");
                        break;
                    }
                }
                catch (ActionException e) {
                    sbHtml.append("ActionException caught in Action ").append(e.getActionName()).append(": ").append(e.getMessage());
                }
                catch (SapphireException e) {
                    sbHtml.append("SapphireException caught in Action: ").append(e.getMessage());
                }
                catch (Exception e) {
                    Trace.log("Exception: Exception caught while processing Action Buttons (" + e.getMessage() + ")");
                    sbHtml.append("\n").append(new TranslationProcessor(pageContext).translate("Could not execute the Action Button:") + " ").append(actionButtonId);
                }
            }
            props.put("acbstatus", acbstatus);
            props.put("exceptionmsg", sbHtml.toString());
        }
        return props;
    }

    private static String getJSToClearToolbarProps(String advancedToolbarFormId) {
        StringBuilder html = new StringBuilder();
        if (advancedToolbarFormId == null || advancedToolbarFormId.trim().length() == 0) {
            advancedToolbarFormId = "advancedtoolbarform";
        }
        html.append("<script language='javascript'>\n").append("    try{\n").append("        parent.").append(advancedToolbarFormId).append(".actionbuttonid.value='';\n").append("        parent.").append(advancedToolbarFormId).append(".advancedtoolbarid.value='';\n").append("    }\n").append("    catch(err){}\n").append("</script>\n");
        return html.toString();
    }
}

