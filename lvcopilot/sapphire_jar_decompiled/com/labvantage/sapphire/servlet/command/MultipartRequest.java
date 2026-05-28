/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.servlet.command.fileupload.FileUploadException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public abstract class MultipartRequest {
    protected List getFileItems(HttpServletRequest request, ServletContext servletContext) throws FileUploadException {
        Logger.logDebug(this.getClass().getName(), "getFileItems called...");
        FileUpload fu = new FileUpload();
        return fu.getFileItems(request, servletContext);
    }

    protected HashMap getAddtionalFields(HttpServletRequest request, List fileItems) {
        Map parammap;
        PropertyList out = new PropertyList();
        if (request != null && (parammap = request.getParameterMap()).size() > 0) {
            Iterator it = parammap.keySet().iterator();
            while (it.hasNext()) {
                String param = it.next().toString();
                if (this.isNormalParameter(param)) continue;
                String column = param;
                if (column.startsWith("at") && column.indexOf("_") > 2) {
                    column = column.substring(column.indexOf("_") + 1);
                }
                out.put(column, request.getParameter(param));
            }
        }
        if (fileItems != null) {
            for (int i = 0; i < fileItems.size(); ++i) {
                String param;
                FileItem fileitem;
                Object item = fileItems.get(i);
                if (!(item instanceof FileItem) || !(fileitem = (FileItem)item).isFormField() || this.isNormalParameter(param = fileitem.getFieldName())) continue;
                String column = param;
                if (column.startsWith("at") && column.indexOf("_") > 2) {
                    column = column.substring(column.indexOf("_") + 1);
                }
                out.put(column, FileUpload.getFileItemString(fileitem));
            }
        }
        return out;
    }

    protected boolean isNormalParameter(String paramname) {
        boolean out = true;
        if (!(paramname.startsWith("file1_") || paramname.equalsIgnoreCase("connectionid") || paramname.equalsIgnoreCase("command") || paramname.equalsIgnoreCase("page") || paramname.equalsIgnoreCase("sdcid") || paramname.equalsIgnoreCase("keyid2") || paramname.equalsIgnoreCase("mode") || paramname.equalsIgnoreCase("keyid1") || paramname.equalsIgnoreCase("keyid3") || paramname.equalsIgnoreCase("description") || paramname.equalsIgnoreCase("desc") || paramname.equalsIgnoreCase("attachmentdesc") || paramname.equalsIgnoreCase("attachmentnum") || paramname.equalsIgnoreCase("typeflag") || paramname.equalsIgnoreCase("type") || paramname.equalsIgnoreCase("filename") || paramname.equalsIgnoreCase("attachmentclob") || paramname.equalsIgnoreCase("rownum") || paramname.equalsIgnoreCase("rs") || paramname.equalsIgnoreCase("rownum") || paramname.equalsIgnoreCase("file1"))) {
            out = false;
        }
        return out;
    }

    protected String getRequestParameter(HttpServletRequest request, String parameterName) {
        String out = request.getParameter(parameterName);
        if (out == null || out.length() == 0) {
            out = request.getParameter("file1_" + parameterName);
        }
        if (out != null && out.length() > 0) {
            return out;
        }
        return "";
    }
}

