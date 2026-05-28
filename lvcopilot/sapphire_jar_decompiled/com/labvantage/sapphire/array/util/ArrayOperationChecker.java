/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.array.util;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ArrayOperationChecker
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String arrayOp = ajaxResponse.getRequestParameter("arrayop");
        String arrayID = ajaxResponse.getRequestParameter("arrayid");
        String dataentrypage = ajaxResponse.getRequestParameter("dataentrypage");
        String target = ajaxResponse.getRequestParameter("target");
        String queryid = ajaxResponse.getRequestParameter("queryid");
        if (arrayOp != null && arrayOp.length() > 0) {
            if (arrayID == null || arrayID.length() == 0) {
                ajaxResponse.setError("Array ID not specified");
            } else {
                String[] arrayidList = StringUtil.split(arrayID, DELIMITER);
                for (int i = 0; i < arrayidList.length; ++i) {
                    try {
                        String last = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayidList[i]);
                        if ((last == null || last.length() == 0) && (arrayOp.equals("SendToInstrument") || arrayOp.equals("ArrayDataEntry"))) {
                            ajaxResponse.setError("Array " + arrayidList[i] + " does not have valid ArrayMethod. Apply an ArrayMethod first.");
                            break;
                        }
                        boolean status = ArrayUtil.validateArrayOperation(this.getQueryProcessor(), arrayidList[i], arrayOp);
                        if (status) continue;
                        throw new SapphireException("Validation failed for operation:" + arrayOp);
                    }
                    catch (SapphireException e) {
                        ajaxResponse.setError("Cannot perform " + arrayOp + " on " + arrayidList[i]);
                        break;
                    }
                }
            }
        } else {
            ajaxResponse.setError("Array operation not specified");
        }
        ajaxResponse.addCallbackArgument("arrayids", arrayID);
        ajaxResponse.addCallbackArgument("dataentrypage", dataentrypage);
        ajaxResponse.addCallbackArgument("target", target);
        ajaxResponse.addCallbackArgument("queryid", queryid);
        ajaxResponse.print();
    }
}

