/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.BaseCustom;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public abstract class BaseRequest
extends BaseCustom {
    private HashMap requestProperties = new HashMap();
    private RequestContext requestContext;

    public abstract void processRequest(HttpServletRequest var1, HttpServletResponse var2, ServletContext var3) throws ServletException;

    public void setProperty(String propertyId, String propertyValue) {
        this.requestProperties.put(propertyId, propertyValue);
    }

    public void setProperties(PropertyList propertyList) {
        this.requestProperties.putAll(propertyList);
    }

    public String getProperty(String propertyId, String defaultValue) {
        String value = (String)this.requestProperties.get(propertyId);
        return value == null ? defaultValue : value;
    }

    public String getProperty(String propertyId) {
        return this.getProperty(propertyId, "");
    }

    public HashMap getProperties() {
        return this.requestProperties;
    }

    public RequestContext getRequestContext() {
        return this.requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
}

