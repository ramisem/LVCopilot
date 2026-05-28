/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MaintAjaxRequest
extends BaseAjaxRequest {
    private static Cache dynamicCodeMap = new Cache("Dynamic Code Cache", 5000);

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String connectionid = requestContext.getConnectionId();
            ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String dynamiccode = ajaxResponse.getRequestParameter("dynamiccode");
            if (dynamiccode != null && dynamiccode.length() > 0) {
                String[] propertyids;
                Map params = ajaxResponse.getRequestParameters();
                String columnids = ajaxResponse.getRequestParameter("columnids");
                String columnvalues = ajaxResponse.getRequestParameter("columnvalues");
                String fieldid = ajaxResponse.getRequestParameter("fieldid");
                String[] ids = StringUtil.split(columnids, ";");
                String[] values = StringUtil.split(columnvalues, ";");
                for (int i = 0; i < ids.length && i < values.length; ++i) {
                    params.put(ids[i], values[i]);
                }
                JSONObject responseJSONObject = new JSONObject();
                PropertyList dynamicrenderingPL = (PropertyList)dynamicCodeMap.get(dynamiccode);
                PropertyList evaluatedPL = new PropertyList();
                HashMap<String, Map> bindMap = new HashMap<String, Map>();
                bindMap.put("primary", params);
                bindMap.put("user", connectionInfo.getUserAttributeMap());
                for (String propertyid : propertyids = new String[]{"mandatoryif", "hiddenif", "readonlyif"}) {
                    if (dynamicrenderingPL.getProperty(propertyid).length() <= 0) continue;
                    String result = GroovyUtil.getInstance(connectionInfo).evaluateSecure(dynamicrenderingPL.getProperty(propertyid), bindMap);
                    evaluatedPL.setProperty(propertyid, "true".equals(result) ? "Y" : ("false".equals(result) ? "N" : result));
                }
                responseJSONObject.put("dynamicrendering", evaluatedPL.toJSONObject(false, false));
                responseJSONObject.put("dynamiccode", ajaxResponse.getRequestParameter("dynamiccode"));
                responseJSONObject.put("fieldid", fieldid);
                responseJSONObject.put("columnvalues", columnvalues);
                ajaxResponse.addCallbackArgument("data", responseJSONObject);
                ajaxResponse.addCallbackArgument("infieldid", fieldid);
                ajaxResponse.print();
            }
        }
        catch (Exception e) {
            throw new ServletException((Throwable)e);
        }
    }

    public static String registerDynamicCode(PropertyList propertyList) {
        String code = propertyList.toJSONString(false, false);
        String dynamicCode = DigestUtils.md5Hex((String)code);
        dynamicCodeMap.put(dynamicCode, propertyList);
        return dynamicCode;
    }
}

