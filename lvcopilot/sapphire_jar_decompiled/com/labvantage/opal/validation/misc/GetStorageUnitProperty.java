/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetStorageUnitProperty
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String value = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        if (StringUtil.getLen(storageunitid) > 0L) {
            String propertyid;
            if (storageunitid.contains(";")) {
                storageunitid = storageunitid.substring(0, storageunitid.indexOf(";"));
            }
            if (StringUtil.getLen(propertyid = ajaxResponse.getRequestParameter("propertyid")) > 0L) {
                String[] s = StringUtil.split(propertyid, ".");
                int length = s.length;
                PropertyList valuetree = StorageUnitSDC.getSUValueTree(this.getQueryProcessor(), storageunitid);
                if (valuetree != null) {
                    for (int i = 0; i < length; ++i) {
                        if (i + 1 == length) {
                            value = valuetree.getProperty(s[i]);
                            continue;
                        }
                        if ((valuetree = valuetree.getPropertyList(s[i])) == null) break;
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("value", value);
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.print();
    }
}

