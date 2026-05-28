/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryCriteriaRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryObjectTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPropertyHandler;
import com.labvantage.sapphire.pageelements.advancedsearch.SearchByAdhoc;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;

public class AjaxRequest
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block7: {
            try {
                RequestContext requestContext = this.getRequestContext();
                String mode = request.getParameter("mode");
                if ("expandnodegwt".equals(mode)) {
                    AdhocQueryObjectTreeRenderer render = new AdhocQueryObjectTreeRenderer(request);
                    this.print(render.getPropertyList().toJSONString());
                    break block7;
                }
                if ("addcriteriongwt".equals(mode)) {
                    AdhocQueryCriteriaRenderer render = new AdhocQueryCriteriaRenderer(request);
                    this.print(render.getSingleCriteriaPropertyList().toJSONString());
                    break block7;
                }
                if ("saveadhocquery".equals(mode)) {
                    HashMap props = HttpUtil.getRequestMap((ServletRequest)request);
                    String basedonsdcid = (String)props.get("basedonsdcid");
                    HashMap requestProps = new RequestProcessor(requestContext.getConnectionId()).processRequest(AdhocQueryPropertyHandler.class.getName(), props);
                    String adhocqueryid = (String)requestProps.get("adhocqueryid");
                    DataSet ds = AdhocQueryCriteriaRenderer.getQueryidDataSet(basedonsdcid, request);
                    TranslationProcessor tp = this.getTranslationProcessor();
                    this.print(adhocqueryid + "||" + AdhocQueryCriteriaRenderer.getQueryidDropDown(basedonsdcid, adhocqueryid, request, tp, ds) + "||" + SearchByAdhoc.getQueryidLinks(ds, tp));
                    break block7;
                }
                if ("deleteadhocquery".equals(mode)) {
                    HashMap props = HttpUtil.getRequestMap((ServletRequest)request);
                    HashMap requestProps = new RequestProcessor(requestContext.getConnectionId()).processRequest(AdhocQueryPropertyHandler.class.getName(), props);
                    this.print("Deleted!");
                    break block7;
                }
                if ("setsessionattribute".equals(mode)) {
                    request.getSession().setAttribute(request.getParameter("name"), (Object)request.getParameter("value"));
                    break block7;
                }
                throw new RuntimeException("not supported request");
            }
            catch (Exception e) {
                this.logger.stackTrace(e);
                throw new ServletException((Throwable)e);
            }
        }
    }
}

