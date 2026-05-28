/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.servlet.command.UploadRequest;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.file.FileManager;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FileUploader
extends BaseElement {
    public FileUploader(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public String getIncludes() {
        StringBuilder out = new StringBuilder();
        PropertyListCollection plugins = new PropertyListCollection();
        PropertyList plugin = new PropertyList();
        plugin.setProperty("pluginid", "dropzone");
        plugin.setProperty("css", "Y");
        plugin.setProperty("allowminimized", "Y");
        plugins.add(plugin);
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.pageContext);
        boolean devMode = false;
        try {
            devMode = configurationProcessor.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        out.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", devMode, this.pageContext));
        return out.toString();
    }

    public void setCreateTempFile(boolean createTempFile) {
        this.element.setProperty("createtempfile", createTempFile ? "Y" : "N");
    }

    public void setSubDirectory(String subDirectory) {
        this.element.setProperty("subdirectory", subDirectory);
    }

    public void setRenameExpression(String renameExpression) {
        this.element.setProperty("renameexpression", renameExpression);
    }

    public void setUploadCallback(String uploadCallback) {
        this.element.setProperty("uploadcallback", uploadCallback);
    }

    public void setErrorCallback(String uploadErrorCallback) {
        this.element.setProperty("errorcallback", uploadErrorCallback);
    }

    @Override
    public String getHtml() {
        StringBuilder out = new StringBuilder();
        if (this.elementid == null || this.elementid.length() == 0) {
            this.elementid = this.generateId();
        }
        if (this.element.getProperty("renderincludes", "Y").equalsIgnoreCase("Y")) {
            out.append(this.getIncludes());
        }
        if (this.element.getProperty("renderstyle", "Y").equalsIgnoreCase("Y")) {
            out.append("<style>");
            out.append(this.getStyle());
            out.append("</style>");
        }
        out.append("<div id=\"").append(this.elementid).append("\" class=\"dropzone\">");
        out.append("</div>");
        if (this.element.getProperty("renderscript", "Y").equalsIgnoreCase("Y")) {
            out.append("<script>");
            out.append(this.getScript());
            out.append("</script>");
        }
        return out.toString();
    }

    public void setUseFileSystem(boolean useFileSystem) {
        this.element.setProperty("usefilesystem", useFileSystem ? "Y" : "N");
    }

    private String generateId() {
        return "fileupload_" + ((int)(Math.random() * 100.0) + 1);
    }

    public String getStyle() {
        StringBuilder out = new StringBuilder();
        if (this.elementid == null || this.elementid.length() == 0) {
            this.elementid = this.generateId();
        }
        out.append("#").append(this.elementid).append("{");
        out.append("padding-bottom:0;");
        out.append("padding-top:0;");
        out.append("border:solid 2px #C3DAF9;");
        out.append("border-radius:4px;");
        out.append("font-weight: normal;");
        out.append("}");
        return out.toString();
    }

    public void setUploadMultiple(boolean uploadMultiple) {
        this.element.setProperty("uploadmultiple", uploadMultiple ? "Y" : "N");
    }

    public void setLocationPolicy(String locationPolicyNode, String locationPolicyItem) {
        this.element.setProperty("locationpolicynode", locationPolicyNode);
        this.element.setProperty("locationpolicyitem", locationPolicyItem);
    }

    public void addExtension(String extension) {
        String ext;
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        if ((ext = this.element.getProperty("filetypes", "")).length() > 0) {
            this.element.setProperty("filetypes", ext + ";" + extension);
        } else {
            this.element.setProperty("filetypes", extension);
        }
    }

    public void setShowAdvancedThumbnails(boolean show) {
        this.element.setProperty("showthumbnails", show ? "Y" : "N");
    }

    private String getFileTypes() {
        String exts = this.element.getProperty("filetypes", "");
        return FileManager.getExtensions(exts, this.element.getProperty("locationpolicynode", "Upload Custom"), this.getConnectionId());
    }

    public void setMaxFileSize(long maxsize) {
        this.element.setProperty("maxfilesize", "" + maxsize);
    }

    public String getScript() {
        long maxsize;
        StringBuilder out = new StringBuilder();
        if (this.elementid == null || this.elementid.length() == 0) {
            this.elementid = this.generateId();
        }
        if (this.element.getProperty("maxfilesize").length() > 0) {
            try {
                maxsize = Long.parseLong(this.element.getProperty("maxfilesize"));
                if (maxsize <= 0L) {
                    maxsize = FileManager.getUploadMaxFileSizeMB(this.getConnectionId());
                }
            }
            catch (NumberFormatException e) {
                maxsize = FileManager.getUploadMaxFileSizeMB(this.getConnectionId());
            }
        } else {
            maxsize = FileManager.getUploadMaxFileSizeMB(this.getConnectionId());
        }
        String fileTypes = this.getFileTypes();
        out.append("Dropzone.autoDiscover = false;");
        out.append("$(document ).ready(function(){");
        out.append("sapphire.fileUpload.controls.push(");
        out.append("$('#").append(this.elementid).append("').dropzone({");
        out.append("url: 'rc?command=upload',");
        out.append("maxFilesize: ").append(maxsize).append(",");
        out.append("uploadMultiple: ").append(this.element.getProperty("uploadmultiple", "N").equalsIgnoreCase("Y")).append(",");
        if (!this.element.getProperty("uploadmultiple", "N").equalsIgnoreCase("Y")) {
            out.append("maxFiles: ").append(1).append(",");
        }
        out.append("parallelUploads:1,");
        out.append("paramName:'file',");
        out.append("acceptedFiles: '").append(fileTypes).append("',");
        if (this.element.getProperty("maxfilesize").length() > 0) {
            out.append("headers:{");
            out.append("'maxfilesize': '").append(this.element.getProperty("maxfilesize")).append("'");
            out.append("},");
        }
        out.append("autoProcessQueue: true,");
        out.append("dictDefaultMessage: '").append(this.getTranslationProcessor().translate(this.element.getProperty("message", "Click to browse or drag over your file"))).append("',");
        out.append("createImageThumbnails:true,");
        out.append("init: function(){");
        out.append("this.on('sending', function(file, xhr, formData){");
        out.append("formData.append('uploadsource', '").append((Object)UploadRequest.UploadSource.FILEUPLOADER).append("');");
        out.append("formData.append('csrftoken', '").append(this.pageContext.getSession().getAttribute("csrftoken")).append("');");
        if (this.elementid != null && this.elementid.length() > 0) {
            out.append("formData.append('elementid', '").append(this.elementid).append("');");
        }
        if (!this.element.getProperty("usefilesystem", "Y").equalsIgnoreCase("Y")) {
            out.append("formData.append('usefilesystem', '").append("N").append("');");
        }
        if (!this.element.getProperty("createtempfile", "Y").equalsIgnoreCase("Y")) {
            out.append("formData.append('createtempfile', '").append("N").append("');");
        }
        out.append("formData.append('showthumbnails', '").append(this.element.getProperty("showthumbnails", "N")).append("');");
        if (this.element.getProperty("renameexpression").length() > 0) {
            out.append("formData.append('renameexpression', '").append(this.element.getProperty("renameexpression")).append("');");
        }
        if (this.element.getProperty("locationpolicynode").length() > 0) {
            out.append("formData.append('locationpolicynode', '").append(this.element.getProperty("locationpolicynode")).append("');");
        }
        if (this.element.getProperty("locationpolicyitem").length() > 0) {
            out.append("formData.append('locationpolicyitem', '").append(this.element.getProperty("locationpolicyitem")).append("');");
        }
        out.append("formData.append('filename', file.name);");
        if (this.element.getProperty("subdirectory").length() > 0) {
            out.append("formData.append('subdirectory', '").append(this.element.getProperty("subdirectory")).append("');");
        }
        out.append("});");
        if (this.element.getProperty("uploadcallback").length() == 0) {
            out.append("this.on( 'success', sapphire.fileUpload.success);");
        } else {
            out.append("this.on( 'success', function(a,b,c){return sapphire.fileUpload.success(a,b,c,'").append(this.element.getProperty("uploadcallback")).append("')});");
        }
        if (this.element.getProperty("errorcallback").length() == 0) {
            out.append("this.on( 'error', sapphire.fileUpload.error);");
        } else {
            out.append("this.on( 'error', function(a,b,c){return sapphire.fileUpload.error(a,b,c,'").append(this.element.getProperty("errorcallback")).append("')});");
        }
        out.append("}");
        out.append("})");
        out.append(");");
        out.append("});");
        return out.toString();
    }
}

