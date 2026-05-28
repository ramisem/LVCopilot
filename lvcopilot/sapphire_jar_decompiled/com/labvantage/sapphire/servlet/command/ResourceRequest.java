/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.servlet.command.AttachmentRequest;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.ActionConstants;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class ResourceRequest
extends BaseRequest
implements ActionConstants {
    private static final String LOGGER_ID = "APP_RESOURCE";
    private static final String CONTENT_TYPE = "contenttype";
    private static final String APP_RESOURCE_ID = "appresourceid";
    public static final String APP_RESOURCE = "appresource";
    static final String CONTENT_SOURCE = "contentsource";
    private static final ContentSource DEFAULT_CONTENT_SOURCE = ContentSource.ATTACHMENT;
    private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.JAVASCRIPT;
    static final String CONTENT_URL = "contenturl";
    static final String PREFIX_CSS = "css_";
    static final String PREFIX_JS = "js_";
    public static final String MESSAGE_COULD_NOT_WRITE_RESPONSE = "Could not write response.";
    static final String COLUMN_ATTACHMENTCLASS = "attachmentclass";
    static final String COLUMN_USEORIGINAL = "useoriginalflag";

    public static String getResourceTag(PageContext pageContext, String databaseId, PropertyList include, String type) throws SapphireException {
        ConnectionProcessor conn = new ConnectionProcessor(pageContext);
        SDIProcessor sdiProcessor = new SDIProcessor(conn.getConnectionid());
        return ResourceRequest.getResourceTag(sdiProcessor, databaseId, include, type);
    }

    public static String getResourceTag(SDIProcessor sdiProcessor, String databaseId, PropertyList include, String type) throws SapphireException {
        String resourceTag = "";
        ContentType contentType = ContentType.fromString(type);
        if (include.getProperty("type").equals("AppResource")) {
            String appResourceId = include.getProperty(APP_RESOURCE);
            resourceTag = (String)CacheUtil.get(databaseId, "AppResourceTags", appResourceId);
            if (resourceTag == null || resourceTag.isEmpty()) {
                resourceTag = ResourceRequest.getResourceHtmlTag(sdiProcessor, databaseId, include, type);
                CacheUtil.put(databaseId, "AppResourceTags", appResourceId, resourceTag);
            }
        } else {
            switch (contentType) {
                case JAVASCRIPT: {
                    resourceTag = ResourceRequest.getJavaScriptURLHtmlTag(null, include).toString();
                    break;
                }
                case CSS: {
                    resourceTag = ResourceRequest.getCSSURLHtmlTag(null, include).toString();
                    break;
                }
            }
        }
        return resourceTag;
    }

    static String getResourceHtmlTag(SDIProcessor sdiProcessor, String databaseId, PropertyList include, String type) throws SapphireException {
        String appResourceId = include.getProperty(APP_RESOURCE);
        StringBuilder html = new StringBuilder();
        SDIData sdiData = ResourceRequest.getAppResource(databaseId, sdiProcessor, appResourceId);
        ContentType contentType = ContentType.fromString(sdiData.getDataset("primary").getString(0, CONTENT_TYPE, DEFAULT_CONTENT_TYPE.getType()));
        if (!ContentType.fromString(type).equals((Object)contentType)) {
            throw new SapphireException("ResourceRequest: requested resource if of wrong type: " + contentType.getType() + " is not " + type);
        }
        ContentSource contentSource = ContentSource.fromString(sdiData.getDataset("primary").getString(0, CONTENT_SOURCE, DEFAULT_CONTENT_SOURCE.getSource()));
        if (contentSource == ContentSource.URL) {
            switch (contentType) {
                case JAVASCRIPT: {
                    html.append((CharSequence)ResourceRequest.getJavaScriptURLHtmlTag(sdiData, include));
                    break;
                }
                case CSS: {
                    html.append((CharSequence)ResourceRequest.getCSSURLHtmlTag(sdiData, include));
                    break;
                }
            }
        } else {
            switch (contentType) {
                case JAVASCRIPT: {
                    html.append((CharSequence)ResourceRequest.getJavaScriptResourceTag(sdiData, include));
                    break;
                }
                case CSS: {
                    html.append((CharSequence)ResourceRequest.getCssResourceTag(sdiData, include));
                    break;
                }
            }
        }
        return html.toString();
    }

    public static StringBuilder getJavaScriptURLHtmlTag(SDIData sdiData, PropertyList include) {
        String url = sdiData == null ? include.getProperty("url", "") : sdiData.getDataset("primary").getString(0, CONTENT_URL, "");
        StringBuilder html = new StringBuilder();
        html.append("<script ");
        if (!include.getProperty("id", "").isEmpty()) {
            html.append("id=\"").append(PREFIX_JS).append(include.getProperty("id", "")).append("\" ");
        }
        html.append("type=\"text/javascript\" src=\"");
        html.append(url);
        html.append("\"></script>");
        return html;
    }

    public static StringBuilder getCSSURLHtmlTag(SDIData sdiData, PropertyList include) {
        String url = sdiData == null ? include.getProperty("url", "") : sdiData.getDataset("primary").getString(0, CONTENT_URL, "");
        StringBuilder html = new StringBuilder();
        html.append("<link ");
        if (!include.getProperty("id", "").isEmpty()) {
            html.append("id=\"").append(PREFIX_CSS).append(include.getProperty("id", "")).append("\" ");
        }
        html.append("rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(url);
        html.append("\"/>");
        return html;
    }

    static StringBuilder getModified(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        DataSet attachment = sdiData.getDataset("attachment");
        DataSet originalFiles = attachment.getFilteredDataSet(new HashMap<String, String>(Collections.singletonMap(COLUMN_ATTACHMENTCLASS, "AppResource")));
        DataSet minifiedFiles = attachment.getFilteredDataSet(new HashMap<String, String>(Collections.singletonMap(COLUMN_ATTACHMENTCLASS, "AppResourceMinify")));
        SimpleDateFormat modifiedFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder html = new StringBuilder();
        ContentSource contentSource = ContentSource.fromString(primary.getString(0, CONTENT_SOURCE, DEFAULT_CONTENT_SOURCE.getSource()));
        html.append("&modified=");
        switch (contentSource) {
            case ATTACHMENT: {
                if (primary.getString(0, COLUMN_USEORIGINAL, "N").equals("N") && minifiedFiles.getRowCount() > 0) {
                    html.append(modifiedFormat.format(minifiedFiles.getCalendar(0, "moddt", Calendar.getInstance()).getTime()));
                    break;
                }
                html.append(modifiedFormat.format(originalFiles.getCalendar(0, "moddt", Calendar.getInstance()).getTime()));
                break;
            }
            case URL: {
                html.append(modifiedFormat.format(primary.getCalendar(0, "moddt", Calendar.getInstance()).getTime()));
                break;
            }
        }
        return html;
    }

    static StringBuilder getJavaScriptResourceTag(SDIData sdiData, PropertyList include) {
        StringBuilder html = new StringBuilder();
        html.append("<script ");
        if (!include.getProperty("id", "").isEmpty()) {
            html.append("id=\"").append(PREFIX_JS).append(include.getProperty("id", "")).append("\" ");
        }
        html.append("type=\"text/javascript\" src=\"").append("rc?command=").append(APP_RESOURCE).append("&").append(APP_RESOURCE_ID).append("=").append(include.getProperty(APP_RESOURCE)).append((CharSequence)ResourceRequest.getModified(sdiData)).append("\"></script>");
        return html;
    }

    static StringBuilder getCssResourceTag(SDIData sdiData, PropertyList include) {
        StringBuilder html = new StringBuilder();
        html.append("<link ");
        if (!include.getProperty("id", "").isEmpty()) {
            html.append("id=\"").append(PREFIX_CSS).append(include.getProperty("id", "")).append("\" ");
        }
        html.append("rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append("rc?command=").append(APP_RESOURCE);
        html.append("&").append(APP_RESOURCE_ID).append("=");
        html.append(include.getProperty(APP_RESOURCE));
        html.append((CharSequence)ResourceRequest.getModified(sdiData));
        html.append("\"/>");
        return html;
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        SDIData sdiData;
        String connectionid;
        response.setHeader("Cache-Control", "max-age=3600");
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        ConnectionProcessor cp = new ConnectionProcessor();
        cp.setConnectionid(requestContext.getConnectionId());
        String string = connectionid = requestContext == null ? "" : requestContext.getConnectionId();
        if (connectionid.isEmpty()) {
            throw new LoginException(request.getQueryString(), "In order to access this resource, you must login.");
        }
        Logger.logDebug(LOGGER_ID, "Get appresource connection: " + connectionid);
        this.setConnectionId(connectionid);
        boolean useFullIncludes = cp.getSapphireConnection().getUseFullIncludes();
        String appResourceId = request.getParameter(APP_RESOURCE_ID);
        try {
            sdiData = ResourceRequest.getAppResource(cp.getSapphireConnection().getDatabaseId(), this.getSDIProcessor(), appResourceId);
        }
        catch (SapphireException e) {
            throw new ServletException(e.getMessage());
        }
        DataSet primary = sdiData.getDataset("primary");
        DataSet attachment = sdiData.getDataset("attachment");
        DataSet originalFiles = attachment.getFilteredDataSet(new HashMap<String, String>(Collections.singletonMap(COLUMN_ATTACHMENTCLASS, "AppResource")));
        DataSet minifiedFiles = attachment.getFilteredDataSet(new HashMap<String, String>(Collections.singletonMap(COLUMN_ATTACHMENTCLASS, "AppResourceMinify")));
        response.setStatus(200);
        response.setContentType(primary.getString(0, CONTENT_TYPE, DEFAULT_CONTENT_TYPE.getType()));
        response.setCharacterEncoding("UTF-8");
        try {
            ContentSource contentSource = ContentSource.fromString(primary.getString(0, CONTENT_SOURCE, DEFAULT_CONTENT_SOURCE.getSource()));
            switch (contentSource) {
                case ATTACHMENT: {
                    if (primary.getString(0, COLUMN_USEORIGINAL, "N").equals("N") && minifiedFiles.getRowCount() > 0 && !useFullIncludes) {
                        PropertyList a1 = new PropertyList((HashMap)minifiedFiles.stream().findFirst().get());
                        this.streamAttachmentToResponse(response, connectionid, a1);
                        break;
                    }
                    if (originalFiles.getRowCount() > 0) {
                        PropertyList a1 = new PropertyList((HashMap)originalFiles.stream().findFirst().get());
                        this.streamAttachmentToResponse(response, connectionid, a1);
                        break;
                    }
                    response.setStatus(204);
                    break;
                }
                case URL: {
                    response.sendRedirect(primary.getString(0, CONTENT_URL, ""));
                    break;
                }
                default: {
                    StringBuilder message = new StringBuilder();
                    message.append("Content source not supported ").append(contentSource.toString()).append(" for ").append(APP_RESOURCE).append(" ").append(appResourceId);
                    Logger.logError(LOGGER_ID, message.toString());
                    throw new ServletException(message.toString());
                }
            }
        }
        catch (IOException e) {
            Logger.logError(LOGGER_ID, MESSAGE_COULD_NOT_WRITE_RESPONSE, e);
            try {
                response.sendError(400, MESSAGE_COULD_NOT_WRITE_RESPONSE);
            }
            catch (IOException ex) {
                Logger.logError(LOGGER_ID, MESSAGE_COULD_NOT_WRITE_RESPONSE, ex);
                throw new ServletException(e.getMessage(), (Throwable)ex);
            }
        }
    }

    private void streamAttachmentToResponse(HttpServletResponse response, String connectionId, PropertyList attachment) throws IOException {
        Attachment sdiAttachment = new AttachmentProcessor(connectionId).getSDIAttachment(attachment.getProperty("sdcid"), attachment.getProperty("keyid1"), attachment.getProperty("keyid2"), attachment.getProperty("keyid3"), Integer.parseInt(attachment.getProperty("attachmentnum")));
        try (ReadableByteChannel inputChannel = Channels.newChannel(AttachmentRequest.getInputStream(sdiAttachment, true));
             WritableByteChannel outputChannel = Channels.newChannel((OutputStream)response.getOutputStream());){
            ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }
    }

    static SDIData getAppResource(String databaseid, SDIProcessor sdiProcessor, String appResourceId) throws SapphireException {
        SDIData sdiData = (SDIData)CacheUtil.get(databaseid, "AppResourceSdis", appResourceId);
        if (sdiData == null) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setRequestItem("primary");
            Logger.logDebug(LOGGER_ID, sdiRequest.getRequestItems().length);
            sdiRequest.setRequestItem("attachment");
            Logger.logDebug(LOGGER_ID, sdiRequest.getRequestItems().length);
            sdiRequest.setSDCid("LV_AppResource");
            sdiRequest.setKeyid1List(appResourceId);
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            DataSet primary = sdiData.getDataset("primary");
            if (primary.getRowCount() == 0) {
                StringBuilder message = new StringBuilder();
                message.append("AppResource not found. ").append(appResourceId);
                Logger.logError(LOGGER_ID, message.toString());
                throw new SapphireException(message.toString());
            }
            ContentSource.fromString(primary.getString(0, CONTENT_SOURCE, DEFAULT_CONTENT_SOURCE.getSource()));
            CacheUtil.put(databaseid, "AppResourceSdis", appResourceId, sdiData);
        }
        return sdiData;
    }

    public static enum ContentType {
        JAVASCRIPT("text/javascript"),
        CSS("text/css");

        private String type;

        private ContentType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public static ContentType fromString(String type) {
            for (ContentType ct : ContentType.values()) {
                if (!ct.type.equalsIgnoreCase(type)) continue;
                return ct;
            }
            throw new IllegalArgumentException("No constant with text " + type + " found");
        }
    }

    public static enum ContentSource {
        ATTACHMENT("attachment"),
        URL("url");

        private String type;

        private ContentSource(String type) {
            this.type = type;
        }

        public String getSource() {
            return this.type;
        }

        public static ContentSource fromString(String type) {
            for (ContentSource ct : ContentSource.values()) {
                if (!ct.type.equalsIgnoreCase(type)) continue;
                return ct;
            }
            throw new IllegalArgumentException("No constant with text " + type + " found");
        }
    }
}

