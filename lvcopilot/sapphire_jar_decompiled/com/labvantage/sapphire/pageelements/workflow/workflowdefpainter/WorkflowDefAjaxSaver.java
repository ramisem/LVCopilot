/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefImageCreator;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
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

public class WorkflowDefAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77802 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList workflowprops = new PropertyList(new JSONObject(props));
                try {
                    WorkflowDefMaint.Mode mode;
                    String sdcid = ajaxResponse.getRequestParameter("sdcid", "LV_WorkflowDef");
                    String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                    String keyid2 = ajaxResponse.getRequestParameter("keyid2", "1");
                    String keyid3 = ajaxResponse.getRequestParameter("keyid3", "1");
                    String changeRequestId = ajaxResponse.getRequestParameter("changerequestid", "");
                    String checkedOutToDeptId = ajaxResponse.getRequestParameter("checkedouttodepartmentid", "");
                    try {
                        mode = WorkflowDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit").toUpperCase());
                    }
                    catch (Exception e) {
                        mode = WorkflowDefMaint.Mode.EDIT;
                    }
                    try {
                        RequestProcessor rp = new RequestProcessor(this.getConnectionId());
                        HashMap<String, Object> input = new HashMap<String, Object>();
                        input.put("properties", workflowprops);
                        input.put("sdcid", sdcid);
                        input.put("keyid1", keyid1);
                        input.put("keyid2", keyid2);
                        input.put("keyid3", keyid3);
                        input.put("changerequestid", changeRequestId);
                        input.put("checkedouttodepartmentid", checkedOutToDeptId);
                        input.put("mode", (Object)mode);
                        input.put("skipthumbnail", "Y");
                        rp.processRequest("com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPropertyHandler", input);
                        try {
                            this.storeWorkflowImage(keyid1, keyid2, keyid3, servletContext);
                        }
                        catch (Exception e) {
                            this.logger.error("Failed to store workflow image", e);
                        }
                        ajaxResponse.addCallbackArgument("rsetid", input.containsKey("rsetid") && input.get("rsetid") != null ? input.get("rsetid") : "");
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate(e.getMessage()));
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No type string provided."));
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
    private void storeWorkflowImage(String keyid1, String keyid2, String keyid3, ServletContext servletContext) throws IOException, SapphireException {
        Path t = FileUtil.createTempFile("workflowdef", ".png", true);
        try (OutputStream o = Files.newOutputStream(t, new OpenOption[0]);){
            WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.WORKFLOW, o, servletContext, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
            FileManager.FileData f = new FileManager.FileData(t, FileType.getFileTypeByName("PNG", this.getConnectionId()).getMime());
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorkflowDef");
            editProps.setProperty("keyid1", keyid1);
            editProps.setProperty("keyid2", keyid2);
            editProps.setProperty("keyid3", keyid3);
            editProps.setProperty("thumbnailimagesteps", f.getBase64());
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
        finally {
            Files.delete(t);
        }
    }
}

