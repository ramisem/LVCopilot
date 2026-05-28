/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Workbook
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.aspose.cells.Workbook;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorHtmlRenderer;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorJSONRenderer;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorRendererOptions;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class SpreadsheetEditorAjaxRequest
extends BaseAjaxRequest {
    public static int MAX_SPREADSHEET_SIZE = 40000;
    public static String MAX_SPREADSHEET_SIZE_TEXT = "40,000";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String command = ajaxResponse.getRequestParameter("command");
        if (command.equals("tohtml")) {
            try {
                String json = ajaxResponse.getRequestParameter("json");
                SpreadsheetEditorRendererOptions options = new SpreadsheetEditorRendererOptions();
                SpreadsheetEditorModel model = new SpreadsheetEditorModel(json, this.getConnectionProcessor().getSapphireConnection());
                SpreadsheetEditorHtmlRenderer renderer = new SpreadsheetEditorHtmlRenderer(model, options);
                ajaxResponse.addCallbackArgument("html", renderer.toHTML());
            }
            catch (JSONException e) {
                ajaxResponse.setError("Error: " + e.getMessage());
            }
        } else if (command.equals("upload")) {
            byte[] data;
            int i;
            String spreadid = ajaxResponse.getRequestParameter("spreadid");
            String file = ajaxResponse.getRequestParameter("file");
            boolean multisheet = "true".equals(ajaxResponse.getRequestParameter("multisheet"));
            boolean csv = "csv".equals(ajaxResponse.getRequestParameter("filetype"));
            boolean tab = "tab".equals(ajaxResponse.getRequestParameter("filetype"));
            boolean space = "space".equals(ajaxResponse.getRequestParameter("filetype"));
            boolean excel = "xls".equals(ajaxResponse.getRequestParameter("filetype")) || "xlsx".equals(ajaxResponse.getRequestParameter("filetype")) || file.contains("openxmlformats");
            String type = "";
            if (file.length() > 0 && (i = file.indexOf(",")) > -1) {
                type = file.substring(0, i);
                file = file.substring(i + 1);
            }
            SpreadsheetEditorModel model = null;
            if ((csv || tab || space) && (type.contains("data:text") || type.contains("data:;") || type.contains("excel"))) {
                data = Base64.decodeBase64((String)file);
                try {
                    String contents = new String(data, "UTF-8");
                    String[] rows = StringUtil.split(contents, "\n");
                    model = new SpreadsheetEditorModel(rows, this.getConnectionProcessor().getSapphireConnection(), tab ? "\t" : (space ? " " : ","));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (excel) {
                try {
                    data = Base64.decodeBase64((String)file);
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    Workbook workbook = new Workbook((InputStream)bis);
                    model = new SpreadsheetEditorModel(workbook, this.getConnectionProcessor().getSapphireConnection(), multisheet);
                }
                catch (Exception e) {
                    ajaxResponse.setError("Failed to process excel file: " + e.getMessage());
                }
            }
            if (model != null) {
                boolean tooLarge = false;
                for (SpreadsheetEditorModel.Sheet sheet : model.sheets) {
                    if (sheet.rowCount * sheet.colCount <= MAX_SPREADSHEET_SIZE) continue;
                    tooLarge = true;
                }
                if (!tooLarge) {
                    try {
                        SpreadsheetEditorJSONRenderer renderer = new SpreadsheetEditorJSONRenderer(model);
                        JSONObject json = renderer.toJSON();
                        String sJson = json.toString();
                        ajaxResponse.addCallbackArgument("spreadid", spreadid);
                        ajaxResponse.addCallbackArgument("json", sJson);
                    }
                    catch (Exception e) {
                        ajaxResponse.setError("Failed to render spreadsheet json: " + e.getMessage());
                    }
                } else {
                    ajaxResponse.setError("You have reached the maximum size of the spreadsheet. You can only create sheets up to " + MAX_SPREADSHEET_SIZE_TEXT + " cells.");
                }
            } else {
                ajaxResponse.setError("Unrecognized file type");
            }
        } else if (!command.equals("download")) {
            ajaxResponse.setError("Unrecognized command: " + command);
        }
        ajaxResponse.print();
    }
}

