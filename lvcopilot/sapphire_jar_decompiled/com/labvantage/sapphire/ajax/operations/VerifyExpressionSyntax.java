/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.util.calculations.ExpressionPrefix;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class VerifyExpressionSyntax
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 81927 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String def;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String typeflag = ajaxResponse.getRequestParameter("typeflag");
        String implementation = ajaxResponse.getRequestParameter("implementation");
        String expression = ajaxResponse.getRequestParameter("expression");
        String namespace = ajaxResponse.getRequestParameter("namespace");
        ajaxResponse.addCallbackArgument("newexpression", namespace + "." + expression);
        if (typeflag.equals("J")) {
            def = "def static " + expression + "{" + implementation.substring(implementation.lastIndexOf(".") + 1) + "." + expression + " };";
        } else {
            def = "def static " + expression + " ";
            if (!implementation.startsWith("{")) {
                def = def + "{";
                def = def + implementation;
                def = def + "}";
            } else {
                def = def + implementation;
            }
            def = def + ";";
        }
        def = ExpressionPrefix.prependCalcDefs(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), this.getQueryProcessor(), "$G{" + def + "}");
        if (typeflag.equals("J") && implementation.contains(".")) {
            String packagename = implementation.substring(0, implementation.lastIndexOf("."));
            def = "import " + packagename + ".*;\n" + def;
        }
        try {
            GroovyUtil.parseScript(def);
            ajaxResponse.addCallbackArgument("parseerror", "");
        }
        catch (SapphireException e) {
            ajaxResponse.addCallbackArgument("parseerror", e.getMessage());
        }
        ajaxResponse.print();
    }
}

