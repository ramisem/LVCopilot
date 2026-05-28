/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.indexers.AttachmentIndexer;
import com.labvantage.sapphire.services.Attachment;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.StringUtil;

public class SearchAjaxRequest
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String attachmentKey = ajaxResponse.getRequestParameter("attachmentkey");
        String[] attachKeys = StringUtil.split(attachmentKey, ";");
        String enteredquery = ajaxResponse.getRequestParameter("enteredquery");
        StringBuffer contentExtract = new StringBuffer();
        try {
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionid);
            Attachment attachment = (Attachment)Attachment.getAttachment(attachKeys[0], attachKeys[1], attachKeys[2], attachKeys[3], Integer.parseInt(attachKeys[4]));
            attachment.setTriggerBusinessRule(false);
            attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
            byte[] byteData = null;
            if (attachment.getType().equals("M")) {
                String clob = attachment.getClob();
                if (clob != null && clob.length() > 0) {
                    Document jdoc = Jsoup.parse((String)clob);
                    byteData = jdoc.body().text().getBytes();
                }
            } else {
                byteData = attachment.getData(100000000);
            }
            if (byteData == null || byteData.length == 0) {
                contentExtract.append(new TranslationProcessor("Attachment not found or is empty"));
            } else if (byteData.length > 100000000) {
                contentExtract.append(new TranslationProcessor("File too large to show matches"));
            } else {
                AttachmentIndexer.extractAttachmentContent(attachment, contentExtract, byteData, this.getConnectionId());
            }
            SearchResults results = new SearchResults(new SearchRequest(enteredquery));
            String sections = results.highlightMatchLines(contentExtract.toString(), "\n", 5).replaceAll("\n", "<br/>");
            ajaxResponse.addCallbackArgument("attachmenttext", sections);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ajaxResponse.print();
    }
}

