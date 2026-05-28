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

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPropertyHandler;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefImageCreator;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class TaskDefAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77833 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                TaskDefMaint.Mode mode;
                PropertyList taskprops = new PropertyList(new JSONObject(props));
                String sdcid = ajaxResponse.getRequestParameter("sdcid", "LV_TaskDef");
                String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                String keyid2 = ajaxResponse.getRequestParameter("keyid2", "1");
                String keyid3 = ajaxResponse.getRequestParameter("keyid3", "3");
                String changeRequestId = ajaxResponse.getRequestParameter("changerequestid", "");
                String checkedOutToDeptId = ajaxResponse.getRequestParameter("checkedouttodepartmentid", "");
                try {
                    mode = TaskDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit").toUpperCase());
                }
                catch (Exception e) {
                    mode = TaskDefMaint.Mode.EDIT;
                }
                boolean descendant = ajaxResponse.getRequestParameter("descendant", "N").equalsIgnoreCase("Y");
                RequestProcessor rp = new RequestProcessor(this.getConnectionId());
                HashMap<String, Object> input = new HashMap<String, Object>();
                try {
                    input.put("properties", taskprops);
                    input.put("sdcid", sdcid);
                    input.put("keyid1", keyid1);
                    input.put("keyid2", keyid2);
                    input.put("keyid3", keyid3);
                    input.put("mode", (Object)mode);
                    input.put("changerequestid", changeRequestId);
                    input.put("checkedouttodepartmentid", checkedOutToDeptId);
                    input.put("descendant", descendant ? "Y" : "N");
                    input.put("skipthumbnail", "Y");
                    rp.processRequest(TaskDefPropertyHandler.class.getName(), input);
                    try {
                        this.storeTaskImage(keyid1, keyid2, keyid3, servletContext);
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to store workflow image", e);
                    }
                    ajaxResponse.addCallbackArgument("rsetid", input.containsKey("rsetid") && input.get("rsetid") != null ? input.get("rsetid") : "");
                    if (taskprops.containsKey("taskdefitemid")) {
                        ajaxResponse.addCallbackArgument("taskdefitemid", taskprops.getProperty("taskdefitemid"));
                        ajaxResponse.addCallbackArgument("newtaskdefitemid", WorkflowDefPainter.generateId(keyid1, keyid2, keyid3, "TD", false));
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate(e.getMessage()));
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeTaskImage(String keyid1, String keyid2, String keyid3, ServletContext servletContext) throws IOException, SapphireException {
        Path ts = FileUtil.createTempFile("taskdefsteps", ".png", true);
        try {
            Path tt = FileUtil.createTempFile("taskdeftask", ".png", true);
            try (OutputStream os = Files.newOutputStream(ts, new OpenOption[0]);
                 OutputStream ot = Files.newOutputStream(tt, new OpenOption[0]);){
                String mime = FileType.getFileTypeByName("PNG", this.getConnectionId()).getMime();
                WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.STEPS, os, servletContext, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                FileManager.FileData fs = new FileManager.FileData(ts, mime);
                WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.TASK, ot, servletContext, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                FileManager.FileData ft = new FileManager.FileData(tt, mime);
                PropertyList editProps = new PropertyList();
                editProps.setProperty("sdcid", "LV_TaskDef");
                editProps.setProperty("keyid1", keyid1);
                editProps.setProperty("keyid2", keyid2);
                editProps.setProperty("keyid3", keyid3);
                editProps.setProperty("thumbnailimagesteps", fs.getBase64());
                editProps.setProperty("thumbnailimageappearance", ft.getBase64());
                this.getActionProcessor().processAction("EditSDI", "1", editProps);
            }
            finally {
                Files.delete(tt);
            }
        }
        finally {
            Files.delete(ts);
        }
    }
}

