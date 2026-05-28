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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetRuleTypeProcessor
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String rule = ajaxResponse.getRequestParameter("rule");
        String ruletype = ajaxResponse.getRequestParameter("ruletype");
        String result = ajaxResponse.getRequestParameter("result");
        String editor = "";
        String className = ruletype + rule + "RuleProcessor";
        try {
            Class<?> processorclass = null;
            try {
                processorclass = Class.forName("com.labvantage.sapphire.pages.prodvariantruleeditor." + className);
            }
            catch (ClassNotFoundException e) {
                try {
                    processorclass = Class.forName(this.getCustomRuleClassName(rule, ruletype));
                }
                catch (ClassNotFoundException e1) {
                    throw new ServletException((Throwable)e1);
                }
                catch (SapphireException e1) {
                    throw new ServletException((Throwable)e1);
                }
            }
            Constructor<?> c = processorclass.getConstructor(new Class[0]);
            Object o = c.newInstance(new Object[0]);
            Class[] partypes = new Class[]{QueryProcessor.class, TranslationProcessor.class};
            Method m = processorclass.getMethod("getEditor", partypes);
            editor = (String)m.invoke(o, this.getQueryProcessor(), this.getTranslationProcessor());
        }
        catch (NoSuchMethodException e) {
            throw new ServletException((Throwable)e);
        }
        catch (InvocationTargetException e) {
            throw new ServletException((Throwable)e);
        }
        catch (IllegalAccessException e) {
            throw new ServletException((Throwable)e);
        }
        catch (InstantiationException e) {
            throw new ServletException((Throwable)e);
        }
        ajaxResponse.addCallbackArgument("editor", editor);
        ajaxResponse.print();
    }

    private String getCustomRuleClassName(String rule, String ruletype) throws SapphireException {
        String className = "";
        PropertyList policy = this.getConfigurationProcessor().getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection customRules = policy.getCollectionNotNull("customrules");
            for (int i = 0; i < customRules.size(); ++i) {
                PropertyList ruleDef = customRules.getPropertyList(i);
                String ruleTypeValueFromPolicy = ruleDef.getProperty("ruletype");
                String ruleFromPolicy = ruleDef.getProperty("rule");
                if (!ruleTypeValueFromPolicy.equals(ruletype) || ruleFromPolicy.indexOf(rule) == -1) continue;
                className = ruleDef.getProperty("classname");
                break;
            }
        }
        return className;
    }
}

