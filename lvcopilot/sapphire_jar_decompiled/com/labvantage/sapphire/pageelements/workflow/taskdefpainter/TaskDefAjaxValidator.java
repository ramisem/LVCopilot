/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.workflow.StepUtil;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import com.labvantage.sapphire.util.MiscUtil;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefAjaxValidator
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54887 $";
    boolean debugmsg = false;

    private void appendMessage(StringBuffer msgBuff, String msg, MsgType type) {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (msgBuff.length() > 0) {
            msgBuff.append("<br>");
        }
        if (this.debugmsg) {
            switch (type) {
                case WARNING: {
                    msgBuff.append("<font style=\"color:orange;\">" + tp.translate("WARN:") + "</font> ").append(msg);
                    break;
                }
                case ERROR: {
                    msgBuff.append("<font style=\"color:red;\">" + tp.translate("ERROR:") + "</font> ").append(msg);
                }
            }
        } else {
            msgBuff.append(msg);
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (props.length() > 0) {
            try {
                PropertyListCollection taskios;
                TaskDefMaint.Mode mode;
                PropertyList taskprops = new PropertyList(new JSONObject(props));
                String sdcid = ajaxResponse.getRequestParameter("sdcid", "LV_TaskDef");
                String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                String keyid2 = ajaxResponse.getRequestParameter("keyid2", "1");
                String keyid3 = ajaxResponse.getRequestParameter("keyid3", "1");
                String callback = ajaxResponse.getRequestParameter("continuecallback", "");
                this.debugmsg = ajaxResponse.getRequestParameter("debugmsg", "N").equalsIgnoreCase("Y");
                boolean keysonly = ajaxResponse.getRequestParameter("keysonly", "N").equalsIgnoreCase("Y");
                try {
                    mode = TaskDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit").toUpperCase());
                }
                catch (Exception e) {
                    mode = TaskDefMaint.Mode.EDIT;
                }
                StringBuffer errMsg = new StringBuffer();
                StringBuffer warMsg = new StringBuffer();
                StringBuffer failedfields = new StringBuffer();
                StringBuffer passedfields = new StringBuffer();
                if (mode == TaskDefMaint.Mode.ADD) {
                    if (keyid1.length() == 0) {
                        this.appendMessage(errMsg, tp.translate("Please provide a task id."), MsgType.ERROR);
                        failedfields.append("taskdefid");
                    } else {
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT taskdefid FROM taskdef WHERE taskdefid=? AND taskdefversionid=? AND taskdefvariantid=?", new Object[]{keyid1, keyid2, keyid3});
                        if (ds == null) {
                            ajaxResponse.setError(tp.translate("Could not query existing tasks."));
                        } else if (ds.getRowCount() > 0) {
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("keyid", "\"keyid1 (" + keyid2 + " - " + keyid3 + ")\"");
                            this.appendMessage(errMsg, tp.translate("The entered task id [keyid] already exists. Please enter an alternative.", token), MsgType.ERROR);
                            failedfields.append("taskdefid");
                        } else {
                            passedfields.append("taskdefid");
                        }
                    }
                }
                if (!keysonly) {
                    PropertyListCollection steps;
                    String title;
                    if (!taskprops.getProperty("standaloneflag", "N").equalsIgnoreCase("Y") && (title = taskprops.getProperty("shorttitle", taskprops.getProperty("longtitle", ""))).length() == 0) {
                        this.appendMessage(warMsg, tp.translate("You have not provided a execution title or icon text, thus the task will not be available to workflows."), MsgType.WARNING);
                    }
                    if ((steps = taskprops.getCollection("steps")) != null && steps.size() > 0) {
                        if (taskprops.getProperty("startstepid", "").length() == 0) {
                            this.appendMessage(errMsg, tp.translate("You have not provided a start step."), MsgType.ERROR);
                        }
                        PropertyListCollection steptypes = TaskDefPainter.getStepTypes(this.getSDIProcessor(), new WebAdminProcessor(this.getConnectionId()), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                        String[] autoexec = StepUtil.getAutoExecuteStepTypes();
                        for (int i = 0; i < steps.size(); ++i) {
                            PropertyList step = steps.getPropertyList(i);
                            String steptypeid = step.getProperty("propertytreeid", "");
                            PropertyList steptype = steptypes.find("steptypeid", steptypeid);
                            if (steptype != null && MiscUtil.MiscArray.isStringInArray(autoexec, steptype.getProperty("steptype"), false)) continue;
                            String text = step.getProperty("shorttitle", step.getProperty("title", ""));
                            text = text.length() > 0 ? text + " (" + step.getProperty("stepid", "") + ")" : step.getProperty("stepid", "");
                            if (step.getProperty("title", "").length() != 0) continue;
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("text", text);
                            this.appendMessage(warMsg, tp.translate("You have not provided an execution title for step [text].", token), MsgType.WARNING);
                        }
                    }
                }
                if ((taskios = taskprops.getCollection("taskio")) != null && taskios.size() > 0) {
                    for (int i = 0; i < taskios.size(); ++i) {
                        String text;
                        PropertyList taskio = taskios.getPropertyList(i);
                        String string = text = taskio.getProperty("ioflag", "o").equalsIgnoreCase("i") ? "Input" : "Output";
                        if (taskio.getProperty("connectortypeid", "").length() == 0) {
                            this.appendMessage(errMsg, tp.translate("You have not provided a connector for") + " " + text + " " + taskio.getProperty("iodesc", "Untitled " + text + " " + i), MsgType.ERROR);
                        }
                        if (taskio.getProperty("ioid", "").length() != 0) continue;
                        this.appendMessage(errMsg, tp.translate("You have not provided an Id for") + " " + text + " " + taskio.getProperty("iodesc", "Untitled " + text + " " + i), MsgType.ERROR);
                    }
                }
                ajaxResponse.addCallbackArgument("errmsg", errMsg.toString());
                ajaxResponse.addCallbackArgument("warmsg", warMsg.toString());
                ajaxResponse.addCallbackArgument("failedfields", failedfields.toString());
                ajaxResponse.addCallbackArgument("passedfields", passedfields.toString());
                ajaxResponse.addCallbackArgument("continuecallback", callback);
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }

    private static enum MsgType {
        ERROR,
        WARNING;

    }
}

