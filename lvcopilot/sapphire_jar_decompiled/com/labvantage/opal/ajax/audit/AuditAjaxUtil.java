/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.audit;

import com.labvantage.opal.elements.auditdetails.AuditConstants;
import com.labvantage.opal.elements.auditdetails.AuditController;
import com.labvantage.opal.elements.auditdetails.AuditDetails;
import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.util.ElementInfo;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AuditAjaxUtil
extends BaseAjaxRequest
implements AuditConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 102381 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String status = "";
        String method = "";
        method = ajaxResponse.getRequestParameter("method");
        String divId = ajaxResponse.getRequestParameter("divId");
        if ("getElementDivData".equalsIgnoreCase(method)) {
            try {
                QueryProcessor qp = this.getQueryProcessor();
                TranslationProcessor tp = this.getTranslationProcessor();
                RequestContext requestContext = this.getRequestContext();
                HashMap keyFilter = null;
                String[] divSplit = StringUtil.split(divId, "|");
                String elementId = divSplit[0];
                String key = divSplit[1];
                if ("null".equals(key)) {
                    key = "";
                }
                String inputRequestDataStr = ajaxResponse.getRequestParameter("requestData");
                PropertyList inputRequestData = new PropertyList(new JSONObject(inputRequestDataStr));
                PropertyList pageData = inputRequestData.getPropertyList("pagedata");
                String dbms = inputRequestData.getProperty("dbms");
                String webPageId = inputRequestData.getProperty("page", "");
                Browser browser = new Browser(request);
                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
                RequestProcessor requestProcessor = new RequestProcessor(requestContext.getConnectionId());
                if (webPageId.length() == 0) {
                    throw new SapphireException("No Webpage id found.");
                }
                PropertyList requestData = requestProcessor.getWebPageProperties(webPageId, requestContext);
                for (String propertyId : inputRequestData.keySet()) {
                    requestData.put(propertyId, inputRequestData.get(propertyId));
                }
                AuditElementsContainer elementsContainer = new AuditElementsContainer(qp, tp);
                elementsContainer.createAuditElementsInfoPool(requestData, qp);
                AuditController.setInputProperties(pageData);
                ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
                elementInfo.setExpanded(true);
                String tableId = pageData.getProperty("tableid", "");
                String parentElementId = elementsContainer.getParentElementId(elementId);
                if (parentElementId.length() > 0) {
                    AuditDetails parentElement;
                    ElementInfo parentElementInfo;
                    pageData.setProperty("parentkeyid", divSplit[1]);
                    if (tableId.length() == 0) {
                        parentElementInfo = elementsContainer.getElementInfo(parentElementId);
                        parentElement = parentElementInfo.getElement();
                        parentElement.initData(this.getTranslationProcessor(), connectionInfo, browser);
                        parentElement.getElementProperties().setProperty("parentkeyid", divSplit[1]);
                        keyFilter = parentElement.createTopKeyFilter(key, elementInfo);
                        AuditElementsContainer.populateChainElementAuditData(elementId, elementsContainer, pageData, dbms, qp, this.logger, connectionInfo, true, keyFilter);
                        if (elementInfo.isInAdvancedMode() || parentElementInfo.isInAdvancedMode()) {
                            elementsContainer.syncAdvKeyColsType();
                            keyFilter = parentElement.createTopKeyFilter(key, elementInfo);
                        }
                    } else if (key.length() == 0) {
                        AuditElementsContainer.populateChainElementAuditData(elementId, elementsContainer, pageData, dbms, qp, this.logger, connectionInfo, true, null);
                    } else {
                        parentElementInfo = elementsContainer.getElementInfo(parentElementId);
                        parentElement = parentElementInfo.getElement();
                        parentElement.initData(this.getTranslationProcessor(), connectionInfo, browser);
                        keyFilter = parentElement.createTopKeyFilter(key, elementInfo);
                        AuditElementsContainer.populateChainElementAuditData(elementId, elementsContainer, pageData, dbms, qp, this.logger, connectionInfo, true, keyFilter);
                        if (elementInfo.isInAdvancedMode() || parentElementInfo.isInAdvancedMode()) {
                            elementsContainer.syncAdvKeyColsType();
                            keyFilter = parentElement.createTopKeyFilter(key, elementInfo);
                        }
                    }
                } else {
                    AuditElementsContainer.populateChainElementAuditData(elementId, elementsContainer, pageData, dbms, qp, this.logger, connectionInfo, true, null);
                }
                AuditDetails element = elementInfo.getElement();
                if (key.length() == 0) {
                    element.initData(this.getTranslationProcessor(), connectionInfo, browser);
                } else {
                    element.initData(this.getTranslationProcessor(), connectionInfo, browser, key, keyFilter);
                }
                String html = element.getDataRows();
                ajaxResponse.addCallbackArgument("status", "SUCCESS");
                ajaxResponse.addCallbackArgument("divId", divId);
                ajaxResponse.addCallbackArgument("html", html);
            }
            catch (Exception e) {
                this.logError("AuditAjax Error", e);
                ajaxResponse.addCallbackArgument("status", "FAILURE");
                ajaxResponse.addCallbackArgument("divId", divId);
                if (e instanceof SapphireException) {
                    ajaxResponse.addCallbackArgument("html", "<font color=red><B>" + ((SapphireException)e).getErrorType() + ":</B> </font>" + e.getMessage());
                }
                ajaxResponse.addCallbackArgument("html", "<font color=red><B>Operation Unsuccessful:</B> </font>" + e.getMessage());
            }
        }
        ajaxResponse.print();
    }
}

