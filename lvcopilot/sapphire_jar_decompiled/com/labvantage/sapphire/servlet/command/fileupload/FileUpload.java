/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletInputStream
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.servlet.command.fileupload;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.command.fileupload.DefaultFileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUploadException;
import com.labvantage.sapphire.servlet.command.fileupload.MultipartStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class FileUpload {
    public static final long MAXSIZE = 10000000L;
    public static final long BYTESPERMB = 1000000L;
    public static final long HIGHMB = 0x10000000000L;
    private static long defaultmaxsize = -1L;
    public static final String CONTENT_TYPE = "Content-type";
    public static final String CONTENT_DISPOSITION = "Content-disposition";
    public static final String FORM_DATA = "form-data";
    public static final String ATTACHMENT = "attachment";
    private static final String MULTIPART = "multipart/";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MULTIPART_MIXED = "multipart/mixed";
    public static final int MAX_HEADER_SIZE = 1024;
    private long sizeMax;
    private long sizeThreshold;
    private String repositoryPath;
    private String fileItemClassName = "org.apache.commons.fileupload.DefaultFileItem";
    private Method newInstanceMethod;

    public static void setDefaultMaxsize(long ms) {
        defaultmaxsize = ms;
    }

    public static long getDefaultmaxsize() {
        return defaultmaxsize;
    }

    public static long getServletMaxsize(ServletContext servletContext) {
        long maxsize = 10000000L;
        if (servletContext != null) {
            String maxUploadSize = servletContext.getInitParameter("maxuploadsize");
            try {
                maxsize = Integer.parseInt(maxUploadSize);
                if (maxsize < 0L) {
                    maxsize = 10000000L;
                }
            }
            catch (NumberFormatException nfe) {
                maxsize = 10000000L;
            }
        } else {
            maxsize = 10000000L;
        }
        return maxsize;
    }

    public static final boolean isMultipartContent(HttpServletRequest req) {
        String contentType = req.getHeader(CONTENT_TYPE);
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith(MULTIPART);
    }

    public static long getMaxUploadSize(String attachmentpolicynode, String connectionid) {
        long maxsize = 10000000L;
        boolean found = false;
        String maxUploadSize = "";
        if (attachmentpolicynode != null && attachmentpolicynode.length() > 0) {
            ConfigurationProcessor cp = new ConfigurationProcessor(connectionid);
            try {
                PropertyList attachmentPolicy = cp.getPolicy("AttachmentPolicy", attachmentpolicynode);
                if (attachmentPolicy != null && attachmentPolicy.getProperty("maxsize", "").length() > 0) {
                    maxUploadSize = attachmentPolicy.getProperty("maxsize", "10");
                    Logger.logDebug("Max size obtained from policy: " + maxUploadSize);
                    try {
                        maxsize = 1000000L * Long.parseLong(maxUploadSize);
                        found = true;
                    }
                    catch (NumberFormatException nfe) {
                        Logger.logWarn("Could not parse size number with message(2): " + nfe.getMessage());
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!found || maxsize < 0L) {
            com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
            try {
                maxUploadSize = configuration.getSysConfigProperty("fileuploadmaxsize");
                if (maxUploadSize.length() > 0) {
                    Logger.logDebug("Max size obtained from sysconfig: " + maxUploadSize);
                    maxsize = 1000000L * Long.parseLong(maxUploadSize);
                    found = true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (!found) {
                maxsize = FileUpload.getDefaultmaxsize();
                Logger.logDebug("Max size defaulted (from servlet init props): " + maxsize);
            }
        }
        if (maxsize < 0L) {
            maxsize = 0x10000000000L;
        }
        return maxsize;
    }

    public List<FileItem> getFileItems(HttpServletRequest request, ServletContext servletContext) throws FileUploadException {
        long maxsize = FileUpload.getMaxUploadSize(request.getHeader("attachmentpolicynode") != null && request.getHeader("attachmentpolicynode").length() > 0 ? request.getHeader("attachmentpolicynode") : (String)servletContext.getAttribute("attachmentpolicynode"), RequestContext.getInstance(request).getConnectionId());
        return this.getFileItems(request, servletContext, maxsize);
    }

    public List<FileItem> getFileItems(HttpServletRequest request, ServletContext servletContext, long maxsize) throws FileUploadException {
        File filePath;
        this.setSizeMax(maxsize);
        this.setSizeThreshold(10000000L);
        String uploadRepository = servletContext.getInitParameter("uploadrepository");
        if (uploadRepository == null || uploadRepository.length() == 0) {
            try {
                String sapphirehome = Configuration.getInstance().getSapphireHome();
                Logger.logDebug("No upload rep found thus defule to " + sapphirehome + ".");
                uploadRepository = sapphirehome;
            }
            catch (Exception e) {
                Logger.logError("Could not locate Sapphire Home.");
                uploadRepository = "\\";
            }
        }
        if (!(filePath = new File(uploadRepository)).exists()) {
            filePath.mkdirs();
        }
        this.setRepositoryPath(uploadRepository);
        List fileItems = this.parseRequest(request);
        return fileItems;
    }

    public long getSizeMax() {
        return this.sizeMax;
    }

    public void setSizeMax(long sizeMax) {
        this.sizeMax = sizeMax;
    }

    public long getSizeThreshold() {
        return this.sizeThreshold;
    }

    public void setSizeThreshold(long sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public String getRepositoryPath() {
        return this.repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getFileItemClassName() {
        return this.fileItemClassName;
    }

    public void setFileItemClassName(String fileItemClassName) {
        this.fileItemClassName = fileItemClassName;
        this.newInstanceMethod = null;
    }

    public List parseRequest(HttpServletRequest req) throws FileUploadException {
        return this.parseRequest(req, this.getSizeThreshold(), this.getSizeMax(), this.getRepositoryPath());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List parseRequest(HttpServletRequest req, long sizeThreshold, long sizeMax, String path) throws FileUploadException {
        if (null == req) {
            throw new NullPointerException("req parameter");
        }
        ArrayList<FileItem> items = new ArrayList<FileItem>();
        String contentType = req.getHeader(CONTENT_TYPE);
        if (null == contentType || !contentType.startsWith(MULTIPART)) {
            throw new FileUploadException("the request doesn't contain a multipart/form-data or multipart/mixed stream, content type header is " + contentType);
        }
        Long requestSize = req.getContentLengthLong();
        if (requestSize == -1L) {
            throw new FileUploadException("The request was rejected because it's size is unknown");
        }
        if (sizeMax >= 0L && requestSize > sizeMax) {
            throw new FileUploadException("The request was rejected because it's size exceeds allowed range.");
        }
        try {
            byte[] boundary = contentType.substring(contentType.indexOf("boundary=") + 9).getBytes();
            ServletInputStream input = req.getInputStream();
            MultipartStream multi = new MultipartStream((InputStream)input, boundary);
            boolean nextPart = multi.skipPreamble();
            while (nextPart) {
                Map headers = this.parseHeaders(multi.readHeaders());
                String fieldName = this.getFieldName(headers);
                if (fieldName != null) {
                    FileItem item;
                    String subContentType = this.getHeader(headers, CONTENT_TYPE);
                    if (subContentType != null && subContentType.startsWith(MULTIPART_MIXED)) {
                        byte[] subBoundary = subContentType.substring(subContentType.indexOf("boundary=") + 9).getBytes();
                        multi.setBoundary(subBoundary);
                        boolean nextSubPart = multi.skipPreamble();
                        while (nextSubPart) {
                            headers = this.parseHeaders(multi.readHeaders());
                            if (this.getFileName(headers) != null) {
                                FileItem item2 = this.createItem(sizeThreshold, path, headers, requestSize);
                                try (OutputStream os = item2.getOutputStream();){
                                    multi.readBodyData(os);
                                }
                                item2.setFieldName(this.getFieldName(headers));
                                items.add(item2);
                            } else {
                                multi.discardBodyData();
                            }
                            nextSubPart = multi.readBoundary();
                        }
                        multi.setBoundary(boundary);
                    } else if (this.getFileName(headers) != null) {
                        item = this.createItem(sizeThreshold, path, headers, requestSize);
                        try (OutputStream os = item.getOutputStream();){
                            multi.readBodyData(os);
                        }
                        item.setFieldName(this.getFieldName(headers));
                        items.add(item);
                    } else {
                        item = this.createItem(sizeThreshold, path, headers, requestSize);
                        try (OutputStream os = item.getOutputStream();){
                            multi.readBodyData(os);
                        }
                        item.setFieldName(this.getFieldName(headers));
                        item.setIsFormField(true);
                        items.add(item);
                    }
                } else {
                    multi.discardBodyData();
                }
                nextPart = multi.readBoundary();
            }
        }
        catch (IOException e) {
            Logger.logStackTrace(e);
            throw new FileUploadException("Processing of multipart/form-data request failed. " + e.getMessage());
        }
        return items;
    }

    protected String getFileName(Map headers) {
        String fileName = null;
        String cd = this.getHeader(headers, CONTENT_DISPOSITION);
        if (cd.startsWith(FORM_DATA) || cd.startsWith(ATTACHMENT)) {
            int start = cd.indexOf("filename=\"");
            int end = cd.indexOf(34, start + 10);
            if (start != -1 && end != -1) {
                fileName = cd.substring(start + 10, end).trim();
            }
        }
        return fileName;
    }

    protected String getFieldName(Map headers) {
        String fieldName = null;
        String cd = this.getHeader(headers, CONTENT_DISPOSITION);
        if (cd != null && cd.startsWith(FORM_DATA)) {
            int start = cd.indexOf("name=\"");
            int end = cd.indexOf(34, start + 6);
            if (start != -1 && end != -1) {
                fieldName = cd.substring(start + 6, end);
            }
        }
        return fieldName;
    }

    protected FileItem createItem(long sizeThreshold, String path, Map headers, long requestSize) throws FileUploadException {
        Object[] args = new Object[]{path, this.getFileName(headers), this.getHeader(headers, CONTENT_TYPE), new Long(requestSize), new Long(sizeThreshold)};
        FileItem fileItem = null;
        try {
            fileItem = DefaultFileItem.newInstance(path, this.getFileName(headers), this.getHeader(headers, CONTENT_TYPE), requestSize, sizeThreshold);
        }
        catch (Exception e) {
            throw new FileUploadException(e.toString());
        }
        return fileItem;
    }

    protected Map parseHeaders(String headerPart) {
        HashMap<String, String> headers = new HashMap<String, String>();
        char[] buffer = new char[1024];
        boolean done = false;
        int j = 0;
        try {
            while (!done) {
                int i = 0;
                while (i < 2 || buffer[i - 2] != '\r' || buffer[i - 1] != '\n') {
                    buffer[i++] = headerPart.charAt(j++);
                }
                String header = new String(buffer, 0, i - 2);
                if (header.equals("")) {
                    done = true;
                    continue;
                }
                if (header.indexOf(58) == -1) continue;
                String headerName = header.substring(0, header.indexOf(58)).trim().toLowerCase();
                String headerValue = header.substring(header.indexOf(58) + 1).trim();
                if (this.getHeader(headers, headerName) != null) {
                    headers.put(headerName, this.getHeader(headers, headerName) + ',' + headerValue);
                    continue;
                }
                headers.put(headerName, headerValue);
            }
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            // empty catch block
        }
        return headers;
    }

    protected final String getHeader(Map headers, String name) {
        return (String)headers.get(name.toLowerCase());
    }

    public static FileItem getFileItem(List fileItems, String name) {
        FileItem value = null;
        for (Object that : fileItems) {
            FileItem item;
            if (!(that instanceof FileItem) || !(item = (FileItem)that).getFieldName().equals(name)) continue;
            value = item;
            break;
        }
        return value;
    }

    public static FileItem getFile(List fileItems) {
        for (int i = 0; i < fileItems.size(); ++i) {
            FileItem fileItem = (FileItem)fileItems.get(i);
            if (fileItem.isFormField() || fileItem.getSize() <= 0L) continue;
            return fileItem;
        }
        return null;
    }

    public static String getFileItemValue(List fileItems, String name) {
        String value = "";
        FileItem item = FileUpload.getFileItem(fileItems, name);
        if (item == null) {
            item = FileUpload.getFileItem(fileItems, "file1_" + name);
        }
        if (item != null && item.isFormField()) {
            value = FileUpload.getFileItemString(item);
        }
        return value;
    }

    public static String getFileItemString(FileItem fi) {
        try {
            return new String(fi.get(), "UTF-8");
        }
        catch (Exception e) {
            return fi.getString();
        }
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        int offset;
        int numRead;
        FileInputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // empty if block
        }
        byte[] bytes = new byte[(int)length];
        for (offset = 0; offset < bytes.length && (numRead = ((InputStream)is).read(bytes, offset, bytes.length - offset)) >= 0; offset += numRead) {
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + FileUpload.getFileName(file));
        }
        ((InputStream)is).close();
        return bytes;
    }

    public static String getFileName(FileItem file) {
        if (file != null) {
            String sName = file.getName();
            try {
                return sName == null ? null : new String(sName.getBytes("UTF-8"), "UTF-8");
            }
            catch (Exception e) {
                return sName;
            }
        }
        return null;
    }

    public static String getFileName(File file) {
        if (file != null) {
            try {
                return new String(file.getName().getBytes("UTF-8"), "UTF-8");
            }
            catch (Exception e) {
                return file.getName();
            }
        }
        return "";
    }
}

