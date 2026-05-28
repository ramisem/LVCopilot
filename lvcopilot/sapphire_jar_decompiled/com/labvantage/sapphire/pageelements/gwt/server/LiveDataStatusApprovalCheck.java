/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class LiveDataStatusApprovalCheck
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            JSONObject jsonObject = new JSONObject(request.getParameter("dataapprovalJSON"));
            PropertyList props = new PropertyList();
            Iterator itr = jsonObject.keys();
            while (itr.hasNext()) {
                String key = (String)itr.next();
                props.setProperty(key, jsonObject.getString(key));
                if (!"approvalsteps".equals(key)) continue;
                JSONArray approvalsteps = jsonObject.getJSONArray(key);
                for (int i = 0; i < approvalsteps.length(); ++i) {
                    JSONArray currentstep = approvalsteps.getJSONArray(i);
                    props.put(currentstep.getString(8), currentstep.getString(11));
                }
            }
            if (props.getProperty("sdcid").length() == 0) {
                props.setProperty("sdcid", "Sample");
            }
            this.getActionProcessor().processAction("GetApprovalFlag", "1", props);
            String returnflag = props.getProperty("approvalflag");
            response.getWriter().write(returnflag);
        }
        catch (Exception e) {
            throw new ServletException((Throwable)e);
        }
    }
}

