/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Workbook
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.aspose.cells.Workbook;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorXLSXRenderer;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SpreadsheetEditorRequest
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77278 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String json = request.getParameter("json");
            if (json != null && json.length() > 0) {
                SpreadsheetEditorModel model = new SpreadsheetEditorModel(json, this.getConnectionProcessor().getSapphireConnection());
                SpreadsheetEditorXLSXRenderer renderer = new SpreadsheetEditorXLSXRenderer(model);
                Workbook workbook = renderer.toWorkbook();
                ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
                workbook.save((OutputStream)outByteStream, 6);
                byte[] outArray = outByteStream.toByteArray();
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0L);
                response.setContentType("application/ms-excel");
                response.setContentLength(outArray.length);
                response.setHeader("Content-Disposition", "attachment; filename=eln.xlsx");
                ServletOutputStream outStream = response.getOutputStream();
                outStream.write(outArray);
                outStream.flush();
            } else {
                PrintWriter out = response.getWriter();
                out.print("Unable to fetch Excel. Missing JSON.");
                out.flush();
                out.close();
            }
        }
        catch (Exception e) {
            try {
                PrintWriter out = response.getWriter();
                out.print("Unable to fetch Excel: " + e.getMessage());
                out.flush();
                out.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

